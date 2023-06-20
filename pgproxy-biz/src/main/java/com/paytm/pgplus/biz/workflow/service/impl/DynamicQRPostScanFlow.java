/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.exception.AmountMismatchException;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.AOA_DQR;

@Service("dynamicQRPostScanFlow")
public class DynamicQRPostScanFlow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRPostScanFlow.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(DynamicQRPostScanFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("dynamicQrPostScanValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();

        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
        workFlowTransBean.setTransID(flowRequestBean.getTransID());
        workFlowTransBean.setProductCode(flowRequestBean.getProductCode());

        // If merchant sent token in request then fetch userDetails from OAuth
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        /*
         * Store_Card: 0 � Card is not to be saved | 1 � Card is to be saved
         */
        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean storeCard = ((flowRequestBean.getStoreCard() != null) && "1".equals(flowRequestBean
                .getStoreCard().trim())) ? true : false;
        final boolean isTxnByCardIndexNo = workFlowHelper.checkForCardIndexNo(flowRequestBean);
        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);

            LOGGER.info("userDetails : {}", userDetails);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(userDetails.getFailureMessage(),
                        userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());

            if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                    || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                    || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                    || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)
                    || PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPayMethod())) {

                LOGGER.info("Transaction through CC/DC/IMPS/UPI/EMI");
                if (isSavedCardTxn && !isTxnByCardIndexNo) {
                    // Fetch details from DB and decrypt them then put details
                    // in UserDetailsBiz
                    userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(),
                            userDetails.getResponse(), flowRequestBean);
                    if (!userDetails.isSuccessfullyProcessed()) {
                        LOGGER.error("Exception occurred while fetching saved card details: ",
                                userDetails.getFailureMessage());
                        return new GenericCoreResponseBean<WorkFlowResponseBean>(userDetails.getFailureMessage(),
                                userDetails.getResponseConstant());
                    }
                }

            }

        } else if (StringUtils.isBlank(flowRequestBean.getToken()) && flowRequestBean.getIsSavedCard()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>("Invalid request data",
                    ResponseConstants.SYSTEM_ERROR);
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);
        // Cache CC card/IMPS info
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = dynamicQRCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
        if (createCacheCardResponse != null)
            return createCacheCardResponse;

        /*
         * If add Money case consult wallet
         */
        if (EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())) {

            final GenericCoreResponseBean<Boolean> consultAddMoney = workFlowHelper.consultAddMoney(workFlowTransBean);
            if (!consultAddMoney.isSuccessfullyProcessed() || consultAddMoney.getResponse().equals(false)) {
                return new GenericCoreResponseBean<>("AddMoneyNotAllowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }

        LOGGER.info("DynamicQRPostScanFlow:WorkFlowTransBean, Updated : {}", workFlowTransBean);

        // fetch pcf details for payment payMethod.
        if (flowRequestBean.isPostConvenience()) {
            GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, null);
            workFlowTransBean.setPostConvenienceFeeModel(true);
            workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            if (consultFeeResponse.isSuccessfullyProcessed()) {
                final ConsultDetails consultDetails = consultFeeResponse.getResponse().getConsultDetails()
                        .get(EPayMethod.getPayMethodByMethod(flowRequestBean.getPayMethod()));
                workFlowTransBean.getWorkFlowBean().setChargeAmount(
                        workFlowHelper.calculateChargeAmountInPaise(consultDetails));
                if (workFlowTransBean.getWorkFlowBean().isQRIdFlowOnly()) {
                    if (StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getTxnAmount())
                            && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getQrTxnAmount())
                            && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getChargeAmount())) {
                        verifyAmountPostFeeConsult(workFlowTransBean);
                    }
                    workFlowTransBean.getWorkFlowBean().setTxnAmount(
                            workFlowTransBean.getWorkFlowBean().getQrTxnAmount());
                    ExtendedInfoRequestBean extendInfo = workFlowTransBean.getWorkFlowBean().getExtendInfo();
                    String additionalInfoExtendInfo = extendInfo.getAdditionalInfo();
                    if (StringUtils.isNotBlank(additionalInfoExtendInfo)) {
                        additionalInfoExtendInfo = workFlowHelper.updateAdditionalInfo(extendInfo.getAdditionalInfo(),
                                "TXN_AMOUNT:", workFlowTransBean.getWorkFlowBean().getPaymentRequestBean()
                                        .getQrTxnAmountInRupees());
                        extendInfo.setAdditionalInfo(additionalInfoExtendInfo);
                    }
                }
            }
        }
        long startTime = System.currentTimeMillis();

        // Create Order And Pay if transId was not found on PG2/P+ but DQR order
        // was created on AOA.
        if (StringUtils.equals(AOA_DQR, flowRequestBean.getTxnFlow())) {
            LOGGER.info("Create Order And Pay called from DynamicQRPostScanFlow for AOA Dynamic QR");
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> copResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);
            LOGGER.info("CreateOrderAndPayResponseBean: {}", copResponse);

            if (!copResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Create Order And Pay call failed: {}", copResponse.getFailureMessage());
                GenericCoreResponseBean<WorkFlowResponseBean> responseBean = new GenericCoreResponseBean<>(
                        copResponse.getFailureMessage(), copResponse.getResponseConstant());
                responseBean.setInternalErrorCode(copResponse.getInternalErrorCode());
                return responseBean;
            }

            workFlowTransBean.setCashierRequestId(copResponse.getResponse().getCashierRequestId());
            if (copResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(copResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            workFlowTransBean.setTransID(copResponse.getResponse().getAcquirementId());
            workFlowResponseBean.setTransID(copResponse.getResponse().getAcquirementId());
            workFlowHelper.pushDynamicQrPaymentEvent(flowRequestBean.getPayMethod());
            LOGGER.info("Create Order And Pay API called successfully");
        } else {
            // Pay

            if (StringUtils.isNotBlank(flowRequestBean.getTipAmount()))
                workFlowTransBean.setModifyOrderRequired(true);
            LOGGER.info("Pay called from DynamicQRPostScanFlow");
            final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
            LOGGER.info("BizPayResponse : {}", payResponse);

            if (!payResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Pay API call failed due to : ", payResponse.getFailureMessage());
                if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
                    GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                            payResponse.getFailureMessage(), payResponse.getResponseConstant(),
                            payResponse.getRiskRejectUserMessage());
                    responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                    return responseBean;
                }
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                        payResponse.getResponseConstant());
                responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                LOGGER.info("Pay API response bean : {} ", responseBean);
                return responseBean;
            }
            workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            workFlowTransBean.setTransID(flowRequestBean.getTransID());
            workFlowResponseBean.setTransID(flowRequestBean.getTransID());
            workFlowHelper.pushDynamicQrPaymentEvent(flowRequestBean.getPayMethod());
            LOGGER.info("Pay API called successfully");
        }

        // Pushing payment flag to kafka
        /**
         * Commented Due to Issue with EDC
         * dynamicQRCoreService.pushPostPaymentPayload(workFlowTransBean,
         * flowRequestBean);
         */

        // Caching card details in Redis
        dynamicQRCoreService.cacheCardInRedis(flowRequestBean, workFlowTransBean, userDetails, isSavedCardTxn,
                storeCard);

        // to support v1/transactionStatus API
        dynamicQRCoreService.putCashierRequestIdAndPaymentTypeIdInCache(workFlowTransBean);

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;

        String paymentTypeId = flowRequestBean.getPaymentTypeId();

        LOGGER.info("PaymentTypeId : {}", paymentTypeId);

        if (PaymentTypeIdEnum.UPI.value.equals(paymentTypeId)
                && workFlowTransBean.getWorkFlowBean().isUpiPushExpressSupported()) {

            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeOrderAfterCheck(workFlowTransBean);
                // Setting the payment status as FAIL
                workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());
            } else {
                workFlowTransBean.setPaymentDone(true);
            }
            // Check txn status (Query by acquirement id)
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
            prepareWorkFlowResponseBean(workFlowTransBean, workFlowResponseBean);
            dynamicQRCoreService.putQRDataINCache(workFlowTransBean, workFlowResponseBean);
            // dynamicQRCoreService.pushPostTransactionPayload(workFlowTransBean,
            // flowRequestBean);
        } else if (PaymentTypeIdEnum.CC.value.equals(paymentTypeId) || PaymentTypeIdEnum.DC.value.equals(paymentTypeId)
                || PaymentTypeIdEnum.NB.value.equals(paymentTypeId)
                || PaymentTypeIdEnum.UPI.value.equals(paymentTypeId)
                || PaymentTypeIdEnum.EMI.value.equals(paymentTypeId)) {

            // LOGGER.info("Invoking BankFrom API");

            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);

            LOGGER.info("BankForm Response Received :{}", queryPayResultResponse);

            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                LOGGER.info("BankForm Response Error");

                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant());
                if (queryPayResultResponse.getResponse() != null) {
                    responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
                }
                return responseBean;
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false); // Render 3D form

            String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

            LOGGER.info("PaymentStatus :{}", paymentStatus);

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
                EXT_LOGGER.customInfo("Closing order due to payment status is FAIL");

                closeOrderAfterCheck(workFlowTransBean);

                // Setting the payment status as FAIL
                workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());

                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                LOGGER.info("Transaction status received : {}", queryByAcquirementIdResponse);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);

                prepareWorkFlowResponseBean(workFlowTransBean, workFlowResponseBean);
                dynamicQRCoreService.putQRDataINCache(workFlowTransBean, workFlowResponseBean);
                // dynamicQRCoreService.pushPostTransactionPayload(workFlowTransBean,
                // flowRequestBean);

            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.info("Closing order due to webFormContext is blank");

                closeOrderAfterCheck(workFlowTransBean);

                // Setting the payment status as FAIL
                workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());

                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                LOGGER.info("Transaction status received : {}", queryByAcquirementIdResponse);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);

                prepareWorkFlowResponseBean(workFlowTransBean, workFlowResponseBean);
                dynamicQRCoreService.putQRDataINCache(workFlowTransBean, workFlowResponseBean);
                // dynamicQRCoreService.pushPostTransactionPayload(workFlowTransBean,
                // flowRequestBean);

            } else {
                LOGGER.info("Processing 3D FORM");

                prepareWorkFlowResponseBean(workFlowTransBean, workFlowResponseBean);
                dynamicQRCoreService.putTransTypeDataInCache(workFlowTransBean, workFlowResponseBean);
            }
        }

        // Check payment status & Transaction status
        if (PaymentTypeIdEnum.IMPS.value.equals(paymentTypeId) || PaymentTypeIdEnum.PPI.value.equals(paymentTypeId)
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(paymentTypeId)
                || PaymentTypeIdEnum.GIFT_VOUCHER.value.equals(paymentTypeId)) {

            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            LOGGER.info("Payment Status Received:{}", queryPayResultResponse);

            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Error occurred for Payment Status");

                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL or when TimeOut
            // occurs in case of Offline
            String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();
            if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
                EXT_LOGGER.customInfo("Processing close order due to payment status is FAIL");

                closeOrderAfterCheck(workFlowTransBean);

                // Setting the payment status as FAIL
                workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());

                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                LOGGER.info("Transaction status received : {}", queryByAcquirementIdResponse);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);

            } else if (PaymentStatus.PROCESSING.toString().equals(paymentStatus)
                    && flowRequestBean.getRequestType().equals(ERequestType.DYNAMIC_QR)
                    && System.currentTimeMillis() - startTime > Long
                            .parseLong(BizConstant.PLATFORM_PLUS_CLOSE_ORDER_TIME) * 1000) {

                if (flowRequestBean.isDynamicQREdcRequest()
                        && nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean)) {
                    LOGGER.info("not closing order isDynamicQREdcRequest PaymentStatus.PROCESSING");
                } else {
                    LOGGER.info("Processing close order due to  payment status is PROCESSING");
                    workFlowHelper.triggerCloseOrderPulses(workFlowTransBean);
                }

                // Setting the payment status as FAIL
                workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());

                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                LOGGER.info("Transaction status received : {}", queryByAcquirementIdResponse);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            } else {
                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                LOGGER.info("Transaction status received : {}", queryByAcquirementIdResponse);

                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }

            prepareWorkFlowResponseBean(workFlowTransBean, workFlowResponseBean);
            dynamicQRCoreService.putQRDataINCache(workFlowTransBean, workFlowResponseBean);
            // dynamicQRCoreService.pushPostTransactionPayload(workFlowTransBean,
            // flowRequestBean);
        }
        if ((workFlowTransBean.getWorkFlowBean().isDirectBankCardFlow())
                && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowRequestBean.getPaymentTypeId()))) {
            LOGGER.info("Saving Data For DirectBankCard Flow");
            flowRequestBean.setDirectBankCardFlow(true);
            theiaSessionRedisUtil
                    .set(ConfigurationUtil.getProperty("directBankRequestBeanKey")
                            + workFlowTransBean.getCashierRequestId(), flowRequestBean,
                            Long.parseLong(ConfigurationUtil.getProperty("directBankRedisTimeOut", "300")));
        }
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "DynamicPostScanFLow",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);

    }

    private void prepareWorkFlowResponseBean(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowResponseBean workFlowResponseBean) {

        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
    }

    private void closeOrderAfterCheck(final WorkFlowTransactionBean workFlowTransBean) {

        if (workFlowTransBean.getWorkFlowBean().isDynamicQREdcRequest()
                || (ff4JUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED,
                        false) && workFlowTransBean.getWorkFlowBean().isUpiDynamicQrPaymentExceptEnhanceQR())
                && nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean)) {
            LOGGER.info("not closing order isDynamicQREdcRequest");
            return;
        }

        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }

    private void verifyAmountPostFeeConsult(WorkFlowTransactionBean workFlowTransBean) {
        Double requestTxnAmount = Double.parseDouble(workFlowTransBean.getWorkFlowBean().getTxnAmount());
        Double amtFromQrResponse = Double.parseDouble(workFlowTransBean.getWorkFlowBean().getQrTxnAmount());
        Double chargeAmt = Double.parseDouble(workFlowTransBean.getWorkFlowBean().getChargeAmount());
        Double sum = Double.sum(amtFromQrResponse, chargeAmt);
        if (Double.compare(requestTxnAmount, sum) != 0) {
            Map<String, String> metaData = new LinkedHashMap<>();
            metaData.put("TXN_AMOUNT_FROM_QR_SERVICE", workFlowTransBean.getWorkFlowBean().getQrTxnAmount());
            metaData.put("TXN_AMOUNT_IN_REQUEST", workFlowTransBean.getWorkFlowBean().getTxnAmount());
            EventUtils.pushTheiaEvents(EventNameEnum.AMOUNT_MISMATCH, metaData);
            if (com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty("qr.amount.mismatch.security.fix.enable",
                    "false").equalsIgnoreCase("true")) {
                throw new AmountMismatchException(
                        "Amount Mismatch  TXN_AMOUNT_FROM_QR_SERVICE and TXN_AMOUNT_IN_REQUEST");
            }
        }
    }

}
