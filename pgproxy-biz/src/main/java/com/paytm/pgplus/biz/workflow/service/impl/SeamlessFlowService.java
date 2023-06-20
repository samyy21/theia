/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.AddMoneySourceEnum;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.aoatimeoutcenter.helper.AoaTimeoutCenterServiceHelper;
import com.paytm.pgplus.biz.workflow.model.SubscriptionRenewalResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER;

@Service("seamlessflowservice")
public class SeamlessFlowService implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessFlowService.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    MappingUtil mapUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private AoaTimeoutCenterServiceHelper aoaTimeoutCenterServiceHelper;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Entered Seamless Flow");
        LOGGER.info("WorkFlowRequestBean {}", flowRequestBean);

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        /*
         * // Setting consult Fee Response if
         * (flowRequestBean.isPostConvenience()) {
         * workFlowTransBean.setConsultFeeResponse
         * (flowRequestBean.getConsultFeeResponse());
         * workFlowTransBean.setPostConvenienceFeeModel(true); }
         */
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
            if (flowRequestBean.getUserDetailsBiz() != null) {
                userDetails = new GenericCoreResponseBean<>(flowRequestBean.getUserDetailsBiz());
            } else {
                userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            }

            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            flowRequestBean.setUserDetailsBiz(userDetails.getResponse());
            // workFlowHelper.setLinkPaymentsUserDetails(workFlowTransBean);
        }
        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        workFlowHelper.setLinkPaymentsUserDetails(workFlowTransBean);
        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            StringBuilder key = new StringBuilder(flowRequestBean.getRequestType().getType()).append(flowRequestBean
                    .getTxnToken());
            if (flowRequestBean.isFromAoaMerchant()) {
                LOGGER.error("AOA subscription client call is being used");
                // AoaSubscriptionCreateResponse aoaSubscriptionCreateResponse =
                // (AoaSubscriptionCreateResponse)
                // theiaTransactionalRedisUtil.get(
                // key.toString());
                //
                // if (aoaSubscriptionCreateResponse == null) {
                // LOGGER.error("Exception occurred while fetching aoa subscription details ");
                // return new
                // GenericCoreResponseBean<>("Exception occurred while fetching aoa subscription details ",
                // ResponseConstants.SYSTEM_ERROR);
                // }
                // SubscriptionResponse subscriptionResponse = new
                // SubscriptionResponse();
                // mapUtils.mapAOASubsResponseIntoSubsResponse(aoaSubscriptionCreateResponse,
                // subscriptionResponse);
                // workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
                // workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
            } else {
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                        .toString());
                workFlowHelper.cacheUPIRecurringInfo(flowRequestBean);

                if (subscriptionResponse == null) {
                    LOGGER.error("Exception occurred while fetching subscription details ");
                    return new GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                            ResponseConstants.SYSTEM_ERROR);
                }
                subscriptionResponse.setSavedCardID(flowRequestBean.getSavedCardID());
                if (SubsPaymentMode.UNKNOWN.equals(subscriptionResponse.getPaymentMode())) {
                    subscriptionResponse.setPaymentMode(flowRequestBean.getSubsPayMode());
                }
                workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
                // required at the time of activate subscription in ppp.
                workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
            }
        }

        // Update subscription and get subscription details in case of Mutual
        // fund Payment
        // through Saved subscription card or Mandate
        GenericCoreResponseBean<WorkFlowResponseBean> invalidRenewalResponseBean = processSubscriptionOrMandate(
                flowRequestBean, workFlowTransBean);
        if (invalidRenewalResponseBean != null) {
            return invalidRenewalResponseBean;
        }

        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {

            /**
             * Needed to support Zomato usecase as for txn with cardIndexNo is
             * already processed
             */
            if (flowRequestBean.getExpiryMonth() == null) {
                if (isSavedCardTxn && !isTxnByCardIndexNo) {
                    EventUtils.pushTheiaEvents(EventNameEnum.PAYMENT_USING_SAVED_CARD);
                    if (userDetails != null) {

                        userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(),
                                userDetails.getResponse(), flowRequestBean);

                    } else if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                            || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())
                            || ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())
                            || ERequestType.NATIVE_MF.equals(flowRequestBean.getRequestType())
                            || ERequestType.NATIVE_ST.equals(flowRequestBean.getRequestType())
                            || ERequestType.NATIVE_ST.equals(flowRequestBean.getSubRequestType()) || ERequestType.NATIVE_MF
                                .equals(flowRequestBean.getSubRequestType()))
                            && StringUtils.isNotBlank(flowRequestBean.getCustID())) {

                        userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(), null,
                                flowRequestBean);
                    } else {
                        return new GenericCoreResponseBean<WorkFlowResponseBean>("Invalid request data",
                                ResponseConstants.SYSTEM_ERROR);
                    }
                    if (!userDetails.isSuccessfullyProcessed()) {
                        LOGGER.error("Exception occurred while fetching saved card details: ",
                                userDetails.getFailureMessage());
                        return new GenericCoreResponseBean<>(userDetails.getFailureMessage(),
                                userDetails.getResponseConstant());
                    }

                } else if (isTxnByCardIndexNo) {
                    final GenericCoreResponseBean<WorkFlowResponseBean> mapWorkFlowBeanResponse = mapFlowRequestBeanForPaymentFromCardIndexNo(
                            flowRequestBean, isTxnByCardIndexNo);
                    if (mapWorkFlowBeanResponse != null)
                        return mapWorkFlowBeanResponse;
                }

            }
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

        // Cache CC card/IMPS info
        // Cache NB/Mandate In case Of NATIVE_MF AND NATIVE_ST
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);

        if (createCacheCardResponse != null)
            return createCacheCardResponse;

        /*
         * If add Money case consult wallet
         */
        boolean addMoneyPcfEnabled = isAddMoneyPcfEnabled(flowRequestBean, workFlowTransBean);
        if (addMoneyPcfEnabled
                || (EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid()) && !isNativeOrSubscriptionTxn(flowRequestBean))) {

            final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                    .consultAddMoneyV2(workFlowTransBean);
            if (!consultAddMoney.isSuccessfullyProcessed()
                    || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                    || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney
                            .getResponse().isLimitApplicable())) {
                return new GenericCoreResponseBean<>("AddMoneyNotAllowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
            if (StringUtils.isNotBlank(consultAddMoney.getResponse().getAddMoneyDestination())) {
                workFlowTransBean.setAddMoneyDestination(consultAddMoney.getResponse().getAddMoneyDestination());
            }
        }

        // Code change for savedcard payment
        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            boolean isEligible = workFlowHelper.checkSubscriptionPaymentModeEligibility(flowRequestBean);
            if (!isEligible) {
                return new GenericCoreResponseBean<>("Entered Payment Details is Not valid for subscription",
                        ResponseConstants.SYSTEM_ERROR);
            }
        }

        // Setting pcf
        if (addMoneyPcfEnabled || flowRequestBean.isPostConvenience()) {
            if (!workFlowHelper.isSimplifiedFlow(workFlowTransBean.getWorkFlowBean())) {
                GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, null);
                workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            }
            if (flowRequestBean.isPostConvenience()) {
                workFlowTransBean.setPostConvenienceFeeModel(true);
            }
        }
        LOGGER.info("createOrderAndPay called from SeamlessFlowService:301");
        // Create Order And Pay
        GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                .createOrderAndPay(workFlowTransBean);

        /*
         * this has been done especially for uber as a hack, kindly do not use
         * this for new implementations, please! we will be removing this hack
         * shortly.
         */
        LOGGER.info("createOrderAndPay called from SeamlessFlowService");
        if (isDuplicateOrderIdError(flowRequestBean, createOrderAndPayResponse)) {
            createOrderAndPayResponse = workFlowHelper.createOrderAndPay(workFlowTransBean);
        }

        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
            LOGGER.error("createOrderAndPay failed: {}", createOrderAndPayResponse.getFailureMessage());

            // closing order as retry is not possible in this flow
            if (ERequestType.OFFLINE == flowRequestBean.getRequestType()
                    && createOrderAndPayResponse.getResponse() != null) {
                workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
                closeOrder(workFlowTransBean);
            }

            if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                    || ERequestType.NATIVE_MF.equals(flowRequestBean.getRequestType())
                    || ERequestType.NATIVE_ST.equals(flowRequestBean.getRequestType())
                    || ERequestType.NATIVE_MF.equals(flowRequestBean.getSubRequestType())
                    || ERequestType.NATIVE_ST.equals(flowRequestBean.getSubRequestType())
                    || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION
                        .equals(flowRequestBean.getRequestType()))
                    && createOrderAndPayResponse.getResponseConstant() != null
                    && ResponseConstants.RISK_REJECT.equals(createOrderAndPayResponse.getResponseConstant())) {
                workFlowTransBean.getWorkFlowBean().setPaymentFailureType(NativePaymentFailureType.RISK_REJECT);
                if (StringUtils.isNotBlank(createOrderAndPayResponse.getAcquirementId())) {
                    nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(),
                            createOrderAndPayResponse.getAcquirementId());
                }
            }
            if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                        createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant(),
                        createOrderAndPayResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
                return responseBean;
            }
            GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                    createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant());
            if (ResponseConstants.NEED_RISK_CHALLENGE.getAlipayResultCode().equals(
                    createOrderAndPayResponse.getResponseConstant().getAlipayResultCode())) {
                nativeRetryPaymentUtil.setTransIdInCache(workFlowTransBean.getWorkFlowBean().getTxnToken(),
                        createOrderAndPayResponse.getAcquirementId());
                responseBean.setAcquirementId(createOrderAndPayResponse.getAcquirementId());
            }

            return responseBean;

        }
        EventUtils.pushTheiaEvents(EventNameEnum.ORDER_CREATED_FOR_REQUEST_TYPE, new ImmutablePair<>("ORDER CREATED",
                flowRequestBean.getRequestType().getType()));
        workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
        if (createOrderAndPayResponse.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(createOrderAndPayResponse.getResponse().getSecurityPolicyResult()
                    .getRiskResult());
        }
        workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
        flowRequestBean.setTransID(workFlowTransBean.getTransID());
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        if (workFlowTransBean.getWorkFlowBean().isFromAoaMerchant()
                && org.apache.commons.lang3.StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaytmMID())
                && ff4JUtil.isFeatureEnabled(PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER, workFlowTransBean.getWorkFlowBean()
                        .getPaytmMID())) {
            try {
                aoaTimeoutCenterServiceHelper.persistOrderInfoAtAoaTimeoutCenter(workFlowTransBean.getWorkFlowBean(),
                        createOrderAndPayResponse.getResponse().getAcquirementId());
            } catch (FacadeCheckedException e) {
                LOGGER.error("Error while calling aoa timeout center");
            }
        }
        // Set TxnId in Cache for native Txn
        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.equals(flowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(flowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(flowRequestBean.getRequestType())
                || ERequestType.NATIVE_ST.equals(flowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())) {
            nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(), workFlowTransBean.getTransID());
            nativeRetryPaymentUtil.setField(workFlowTransBean.getTransID(), TheiaConstant.ExtraConstants.PAYMENT_MID,
                    workFlowTransBean.getWorkFlowBean().getPaymentMid());
        }

        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            workFlowHelper.setOrderTimeOutForSubscription(workFlowTransBean.getWorkFlowBean());
        }

        // Caching card details in Redis
        seamlessCoreService
                .cacheCardInRedis(flowRequestBean, workFlowTransBean, userDetails, isSavedCardTxn, storeCard);

        /*
         * modifing order for onus rent payment as need to send riskInfo to cart
         * team
         */
        modifyOrderForOnusRentPayment(createOrderAndPayResponse.getResponse(), flowRequestBean);

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;

        if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || (PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId()) && !flowRequestBean
                        .isMotoPaymentBySubscription())
                || PaymentTypeIdEnum.NB.value.equals(flowRequestBean.getPaymentTypeId())
                || EPayMethod.PPBL.getOldName().equals(flowRequestBean.getBankCode())
                || PaymentTypeIdEnum.UPI.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.WALLET.value.equals(flowRequestBean.getPaymentTypeId())) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);

            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant(),
                        queryPayResultResponse.getRetryStatus());
                if (queryPayResultResponse.getResponse() != null) {
                    responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
                }
                return responseBean;
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);
            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(null);

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {

                LOGGER.info("Closing order due to payment status : {}", queryPayResultResponse.getResponse()
                        .getPaymentStatusValue());

                setFailureTypeForNative(flowRequestBean, workFlowTransBean, queryPayResultResponse);

                if (!ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                    if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                        return closeOrderAndReturnWithFailureReason(workFlowTransBean, queryPayResultResponse);
                }

            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.info("Closing order due to webFormContext is blank");
                if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                            NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
                }
                if (!ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                    if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                        closeOrder(workFlowTransBean);

                    return new GenericCoreResponseBean<>(ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.getMessage(),
                            ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED, queryPayResultResponse.getRetryStatus());
                }
            }
        }

        // Check payment status & Transaction status
        if (PaymentTypeIdEnum.IMPS.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.PPI.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.COD.value.equals(flowRequestBean.getPaymentTypeId())
                || EPayMethod.MP_COD.getMethod().equalsIgnoreCase(flowRequestBean.getPaymentTypeId())
                || EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod().equalsIgnoreCase(flowRequestBean.getPaymentTypeId())
                || EPayMethod.GIFT_VOUCHER.getMethod().equalsIgnoreCase(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.BANK_MANDATE.value.equals(flowRequestBean.getPaymentTypeId())
                || flowRequestBean.isMotoPaymentBySubscription()) {
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL or when TimeOut
            // occurs in case of Offline
            String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

            if (PaymentStatus.FAIL.name().equals(paymentStatus)) {
                closeOrder(workFlowTransBean);
            } else if (PaymentStatus.PROCESSING.toString().equals(paymentStatus)
                    && (ERequestType.OFFLINE.equals(flowRequestBean.getRequestType()) || workFlowTransBean
                            .getWorkFlowBean().isOfflineFlow())) {
                PaymentStatus paymentStatusInt = workFlowHelper.triggerCloseOrderPulses(workFlowTransBean);
                LOGGER.info("PaymentStatus after close order : {}", paymentStatusInt);

                queryPayResultResponse.getResponse().setPaymentStatusValue(paymentStatusInt.name());
                workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            } else {
                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                if (queryByAcquirementIdResponse.getResponse() != null) {
                    queryByAcquirementIdResponse.getResponse().setMerchantTransId(flowRequestBean.getOrderID());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }
        }

        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        LOGGER.info("workFlowTransBean.getWorkFlowBean().isDirectBankCardFlow() {}", workFlowTransBean
                .getWorkFlowBean().isDirectBankCardFlow());

        // Saving WorkflowRequestBean For DirectBankCard Request
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

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "SeamlessFlowService",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean, queryPayResultResponse.getRetryStatus());
    }

    private boolean isDuplicateOrderIdError(WorkFlowRequestBean flowRequestBean,
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse) {
        try {
            if (createOrderAndPayResponse != null && !createOrderAndPayResponse.isSuccessfullyProcessed()
                    && ResponseConstants.REPEAT_REQ_INCONSISTENT == createOrderAndPayResponse.getResponseConstant()) {
                String mids = ConfigurationUtil.getProperty("mid.createorder.retry.channel.id");
                if (org.apache.commons.lang3.StringUtils.contains(mids, flowRequestBean.getPaytmMID())) {
                    if (StringUtils.equals("WAP", flowRequestBean.getChannelID())) {
                        flowRequestBean.setChannelID("WEB");

                        if (flowRequestBean.getTerminalType() != null) {
                            flowRequestBean.setTerminalType(ETerminalType.WEB);
                        }
                        if (flowRequestBean.getEnvInfoReqBean() != null) {
                            flowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.WEB);
                        }
                        if (flowRequestBean.getPaymentRequestBean() != null) {
                            flowRequestBean.getPaymentRequestBean().setChannelId("WEB");
                        }
                        LOGGER.info("retrying createOrderAndPay again with channelId WEB now");

                        return true;
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("Exception in retrying createOrderAndPay");
        }
        return false;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> mapFlowRequestBeanForPaymentFromCardIndexNo(
            WorkFlowRequestBean flowRequestBean, boolean isTxnByCardIndexNo) {
        if (!isTxnByCardIndexNo) {
            return null;
        }

        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {

            String cardIndexNo = flowRequestBean.getPaymentDetails().split(Pattern.quote("|"), -1)[0];
            if (StringUtils.isBlank(cardIndexNo)) {
                return new GenericCoreResponseBean<>("CardIndex No is null", ResponseConstants.SYSTEM_ERROR);
            }

            String expiryDate = (String) theiaSessionRedisUtil.hget(cardIndexNo, "cardExpiryDetails");
            String maskedCardNo = (String) theiaSessionRedisUtil.hget(cardIndexNo, "maskedCardNo");
            if (StringUtils.isBlank(expiryDate) || StringUtils.isBlank(maskedCardNo)) {
                LOGGER.error("Unable to find expiryDate or maskedCardNo in cache for cardIndexNo {}", cardIndexNo);
                return new GenericCoreResponseBean<>("expiryDate is null", ResponseConstants.SYSTEM_ERROR);
            }

            String[] cardExpiryDetails = expiryDate.split("/");
            Short expiryMonth = Short.parseShort(cardExpiryDetails[0]);
            Short expiryYear = Short.parseShort(cardExpiryDetails[1]);

            flowRequestBean.setCardIndexNo(cardIndexNo);
            flowRequestBean.setExpiryMonth(expiryMonth);
            flowRequestBean.setExpiryYear(expiryYear);
            flowRequestBean.setCardNo(maskedCardNo);
            // Need to fetch BIN dedtails for this cardId
            Long firstSixDigit = Long.parseLong(maskedCardNo.substring(0, 6));
            BinDetail binDetails = mapUtils.getBinDetail(firstSixDigit);
            if (binDetails != null) {
                flowRequestBean.setInstId(binDetails.getBankCode());
                flowRequestBean.setBankName(binDetails.getBank());
                flowRequestBean.setCardType(binDetails.getCardType());
                flowRequestBean.setCardScheme(binDetails.getCardName());
                if (!(flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
                    flowRequestBean.setPayMethod(flowRequestBean.getCardType());
                    flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_" + flowRequestBean.getCardScheme());
                }
            } else {
                return new GenericCoreResponseBean<>("Exception occured while fetching BIN details",
                        ResponseConstants.SYSTEM_ERROR);
            }

        }
        return null;
    }

    /**
     * Below method applies to trx whose request type is NATIVE_MF and trx is
     * happening through saved subscription
     *
     * @param flowRequestBean
     * @param workFlowTransBean
     * @return
     */
    private GenericCoreResponseBean<WorkFlowResponseBean> processSubscriptionOrMandate(
            WorkFlowRequestBean flowRequestBean, WorkFlowTransactionBean workFlowTransBean) {
        if ((ERequestType.NATIVE_MF == flowRequestBean.getRequestType() || ERequestType.NATIVE_MF == flowRequestBean
                .getSubRequestType())
                && (PaymentTypeIdEnum.BANK_MANDATE.value.equals(flowRequestBean.getPaymentTypeId()) || flowRequestBean
                        .isMotoPaymentBySubscription())) {
            SubscriptionResponse subscriptionServiceResponse = workFlowHelper
                    .processRenewSubscription(workFlowTransBean);

            if (!subscriptionServiceResponse.getRespCode().equals(RENEWAL_SUCCESS_CODE)) {
                LOGGER.error("Subscription renewal failed due to ::{}", subscriptionServiceResponse.getRespMsg());
                return returnResponseForInvalidRenewal(flowRequestBean, subscriptionServiceResponse);
            }

            if (PaymentTypeIdEnum.BANK_MANDATE.value.equals(flowRequestBean.getPaymentTypeId())) {
                workFlowHelper.setAdditionParamsForMandate(workFlowTransBean, subscriptionServiceResponse);
            } else if (PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())) {
                workFlowTransBean.getWorkFlowBean().setSavedCardID(subscriptionServiceResponse.getSavedCardID());
            } else {
                ResponseConstants invalidPaymentmode = ResponseConstants.INVALID_PAYMENTMODE;
                LOGGER.error("Invalid Payment Mode: {}", flowRequestBean.getPaymentTypeId());
                return new GenericCoreResponseBean<WorkFlowResponseBean>(invalidPaymentmode.getMessage(),
                        invalidPaymentmode);
            }
            workFlowTransBean.setSubscriptionServiceResponse(subscriptionServiceResponse);
            workFlowHelper.setAdditionalParamsForSubsRenewal(workFlowTransBean);
        }
        return null;
    }

    private void setFailureTypeForNative(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {

        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                && ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.name().equals(
                        queryPayResultResponse.getResponse().getInstErrorCode())) {

            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                    NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
        }
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> closeOrderAndReturnWithFailureReason(
            WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {

        closeOrder(workFlowTransBean);

        ResponseConstants responseConstant = ResponseConstants.fetchResponseConstantByName(queryPayResultResponse
                .getResponse().getInstErrorCode());

        if (Objects.isNull(responseConstant))
            responseConstant = ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED;

        return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant,
                queryPayResultResponse.getRetryStatus());
    }

    public void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }

    private boolean isNativeOrSubscriptionTxn(WorkFlowRequestBean workFlowRequestBean) {
        return (workFlowRequestBean != null && workFlowRequestBean.getRequestType() != null && (ERequestType.NATIVE
                .equals(workFlowRequestBean.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION
                .equals(workFlowRequestBean.getRequestType())));
    }

    private boolean isAddMoneyPcfEnabled(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean flowTransactionBean) {
        boolean isVersionSupported = checkIfVersionSupported(flowRequestBean);
        if (isVersionSupported
                && EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())
                && flowRequestBean.isAddMoneyPcfEnabled()
                && Objects.nonNull(flowRequestBean.getUserDetailsBiz())
                && StringUtils.isNotEmpty(flowRequestBean.getUserDetailsBiz().getUserId())
                && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_ADD_MONEY_SURCHARGE,
                        flowRequestBean.getPaytmMID(), null, flowRequestBean.getUserDetailsBiz().getUserId(), false)) {
            flowTransactionBean.setAddMoneyPcfEnabled(true);
            flowRequestBean.setAddMoneySource(AddMoneySourceEnum.THIRD_PARTY.getValue());
            return true;
        }
        return false;
    }

    private boolean checkIfVersionSupported(WorkFlowRequestBean flowRequestBean) {
        String token = flowRequestBean.getTxnToken();
        if (StringUtils.equals(CHECKOUT, flowRequestBean.getWorkFlow())
                || StringUtils.equals(ENHANCED_CASHIER_FLOW, flowRequestBean.getWorkFlow())) {
            return true;
        } else if (StringUtils.isNotBlank(token) && theiaSessionRedisUtil.hget(token, APP_NEW_VERSION_FLOW) != null) {
            return true;
        }
        return false;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForInvalidRenewal(
            final WorkFlowRequestBean flowRequestBean, final SubscriptionResponse subscriptionServiceResponse) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());
        renewalResponse.setRespCode(subscriptionServiceResponse.getRespCode());
        renewalResponse.setRespMsg(subscriptionServiceResponse.getRespMsg());
        renewalResponse.setStatus(subscriptionServiceResponse.getStatus().getName());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void modifyOrderForOnusRentPayment(CreateOrderAndPayResponseBean createOrderAndPayResponseBean,
            WorkFlowRequestBean flowRequestBean) {

        if (flowRequestBean.isOnusRentPaymentMerchant()) {
            SecurityPolicyResult securityPolicyResult = createOrderAndPayResponseBean.getSecurityPolicyResult();

            if (securityPolicyResult != null && securityPolicyResult.getRiskResult() != null) {
                Map<String, String> riskInfoMap = new HashMap<>();
                riskInfoMap.put(RISK_INFO, securityPolicyResult.getRiskResult().getRiskInfo());
                workFlowHelper.modifyOrderForOnusRentPayment(flowRequestBean, riskInfoMap);

            }
        }
    }

}