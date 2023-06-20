package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.enums.AddMoneySourceEnum;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.NativeRetryPaymentUtil;
import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;

@Service("nativeAddMoneyFlow")
public class NativeAddMoneyFlow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(NativeAddMoneyFlow.class);

    @Autowired
    @Qualifier("nativeCreateTopupFlow")
    private NativeCreateTopupFlow nativeCreateTopupFlow;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        if (flowRequestBean.isUpiPushFlow()) {
            if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                flowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
                flowRequestBean.setUpiPushExpressSupported(true);
            }
        }
        // Creating topup, if txn id not present
        if (StringUtils.isBlank(flowRequestBean.getTransID())) {
            GenericCoreResponseBean<WorkFlowResponseBean> createTopupResponse = nativeCreateTopupFlow
                    .process(flowRequestBean);
            if (!createTopupResponse.isSuccessfullyProcessed()
                    || (createTopupResponse.getResponse() != null && StringUtils.isBlank(createTopupResponse
                            .getResponse().getTransID()))) {
                LOGGER.error("Create Topup failed");
                return new GenericCoreResponseBean<WorkFlowResponseBean>(createTopupResponse.getFailureMessage(),
                        createTopupResponse.getResponseConstant());
            }
            nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(), flowRequestBean.getTransID());
        }

        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());

        Map<String, String> extendInfo = workFlowHelper.getExtendInfoFromExtendBean(flowRequestBean);

        // Fetch User Details
        if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                LOGGER.info("User details could not be fetched because ::{}", userDetails.getFailureMessage());
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            extendInfo.put(BizConstant.ExtendedInfoKeys.PAYTM_USER_ID, userDetails.getResponse().getUserId());
        } else {
            return new GenericCoreResponseBean<>(ResponseConstants.BALANCE_ACCOUNT_NOT_EXIST.getMessage(),
                    ResponseConstants.BALANCE_ACCOUNT_NOT_EXIST);
        }

        workFlowTransBean.setTransID(flowRequestBean.getTransID());
        workFlowTransBean.setExtendInfo(extendInfo);

        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean storeCard = (flowRequestBean.getStoreCard() != null)
                && "1".equals(flowRequestBean.getStoreCard().trim());
        final boolean isTxnByCardIndexNo = workFlowHelper.checkForCardIndexNo(flowRequestBean);

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

        // cache card
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
        if (createCacheCardResponse != null) {
            return createCacheCardResponse;
        }

        /*
         * Check for wallet limits Return a generic response code for all limit
         * failure scenarios
         */
        final boolean addMoneyPcfEnabled = isAddMoneyPcfEnabled(flowRequestBean, workFlowTransBean);
        final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                .consultAddMoneyV2(workFlowTransBean);
        if (!consultAddMoney.isSuccessfullyProcessed()
                || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney.getResponse()
                        .isLimitApplicable())) {
            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }

        String addMoneyDestination = consultAddMoney.getResponse().getAddMoneyDestination();
        workFlowTransBean.setAddMoneyDestination(addMoneyDestination);

        if (UserSubWalletType.GIFT_VOUCHER.getType().equals(addMoneyDestination) && !bypassGvConsentPage()
                && !flowRequestBean.isEnhancedCashierPaymentRequest() && !flowRequestBean.isGvConsentFlow()) {
            LOGGER.info("GV consent required.");
            return new GenericCoreResponseBean<>(ResponseConstants.GV_CONSENT_REQUIRED.getMessage(),
                    ResponseConstants.GV_CONSENT_REQUIRED);
        }

        if (isSavedCardTxn && !isTxnByCardIndexNo) {
            if (isAllowedPaymentModeForSavedCard(flowRequestBean)) {
                LOGGER.info("Transaction through CC/DC/IMPS/UPI collect for SavedCard");
                userDetails = fetchSavedCardByCardId(flowRequestBean, userDetails);
                if (!userDetails.isSuccessfullyProcessed()) {
                    LOGGER.error("Exception occurred while fetching saved card details: {}",
                            userDetails.getFailureMessage());
                    return new GenericCoreResponseBean<>(userDetails.getFailureMessage(),
                            userDetails.getResponseConstant());
                }
            }
        }
        if (addMoneyPcfEnabled) {
            if (!workFlowHelper.isSimplifiedFlow(workFlowTransBean.getWorkFlowBean())) {
                GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, null);
                workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            }
        }

        LOGGER.info("Pay called from NativeAddMoneyFlow");
        // Pay
        final GenericCoreResponseBean<BizPayResponse> bizPayResponse = workFlowHelper.pay(workFlowTransBean);
        if (!bizPayResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Pay API call failed due to ::{}", bizPayResponse.getFailureMessage());

            if (org.apache.commons.lang.StringUtils.isNotBlank(bizPayResponse.getRiskRejectUserMessage())) {
                return new GenericCoreResponseBean<>(bizPayResponse.getFailureMessage(),
                        bizPayResponse.getResponseConstant(), bizPayResponse.getRiskRejectUserMessage());
            } else {
                return new GenericCoreResponseBean<>(bizPayResponse.getFailureMessage(),
                        bizPayResponse.getResponseConstant());
            }
        }
        workFlowTransBean.setCashierRequestId(bizPayResponse.getResponse().getCashierRequestID());
        if (bizPayResponse.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(bizPayResponse.getResponse().getSecurityPolicyResult().getRiskResult());
        }
        LOGGER.info("Pay API called successfully with response ::{}", bizPayResponse);

        // Caching card details in Redis
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)) {
            if (!isSavedCardTxn && storeCard && userDetails != null) {
                /*
                 * Encrypt card data & then caching card details in Redis & in
                 * UserDetailsBiz also
                 */
                userDetails = savedCardsService.cacheCardDetails(flowRequestBean, userDetails.getResponse(),
                        workFlowTransBean.getTransID());
                if (!userDetails.isSuccessfullyProcessed()) {
                    LOGGER.error("Exception occured while saving card details in cache: ",
                            userDetails.getFailureMessage());
                }
            }
        }

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Query Payy Result failed due to ::{}", queryPayResultResponse.getFailureMessage());
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);

            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                LOGGER.info("paymentStatus : {}", queryPayResultResponse.getResponse().getPaymentStatusValue());

                setFailureTypeForNative(flowRequestBean, workFlowTransBean, queryPayResultResponse);

            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.error("webFormContext is blank");
                if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                            NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
                }
            }
        }

        // Check payment status & Transaction status in case of IMPS
        if (PaymentTypeIdEnum.IMPS.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.COD.value.equals(flowRequestBean.getPaymentTypeId())) {
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeFundOrder(workFlowTransBean);
            } else {
                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }
        }

        // Setting data in workFlowResponse bean for processing
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setExtendedInfo(extendInfo);
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        // Saving WorkflowRequestBean For DirectBankCard Request
        if ((workFlowTransBean.getWorkFlowBean().isDirectBankCardFlow())
                && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowRequestBean.getPaymentTypeId()))) {
            LOGGER.info("Saving Data For DirectBankCard Flow");
            flowRequestBean.setDirectBankCardFlow(true);
            theiaSessionRedisUtil.set(
                    com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty("directBankRequestBeanKey")
                            + workFlowTransBean.getCashierRequestId(), flowRequestBean, Long
                            .parseLong(com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty(
                                    "directBankRedisTimeOut", "300")));
        }

        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void closeFundOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = workFlowHelper
                .closeFundOrder(workFlowTransBean);
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelFundOrder.getFailureMessage());
        }
    }

    private boolean isAddMoneyPcfEnabled(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean flowTransactionBean) {
        if (flowRequestBean.isAddMoneyPcfEnabled()
                && Objects.nonNull(flowTransactionBean.getUserDetails())
                && StringUtils.isNotEmpty(flowTransactionBean.getUserDetails().getUserId())
                && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_ADD_MONEY_SURCHARGE,
                        flowRequestBean.getPaytmMID(), null, flowTransactionBean.getUserDetails().getUserId(), false)) {
            flowTransactionBean.setAddMoneyPcfEnabled(true);
            flowRequestBean.setAddMoneySource(AddMoneySourceEnum.THIRD_PARTY.getValue());
            return true;
        }
        return false;
    }

    private boolean isAllowedPaymentModeForSavedCard(WorkFlowRequestBean flowRequestBean) {
        return flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value);
    }

    private GenericCoreResponseBean<UserDetailsBiz> fetchSavedCardByCardId(WorkFlowRequestBean flowRequestBean,
            GenericCoreResponseBean<UserDetailsBiz> userDetails) {
        if (userDetails != null) {
            userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(),
                    userDetails.getResponse(), flowRequestBean);

        } else if (org.apache.commons.lang.StringUtils.isNotBlank(flowRequestBean.getCustID())) {
            userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(), null,
                    flowRequestBean);
        } else {
            /*
             * this is when custId and paytmUserId are not present, invalid
             * request data!
             */
            return new GenericCoreResponseBean<>("Invalid Request Data", ResponseConstants.SYSTEM_ERROR);
        }
        return userDetails;
    }

    private void setFailureTypeForNative(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                && ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.getCode().equals(
                        queryPayResultResponse.getResponse().getInstErrorCode())) {

            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                    NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
        }
    }

    private boolean bypassGvConsentPage() {
        return Boolean.TRUE.toString().equals(
                ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.BYPASS_GV_CONSENT_PAGE));
    }
}
