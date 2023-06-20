package com.paytm.pgplus.biz.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.AddMoneySourceEnum;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.aoatimeoutcenter.helper.AoaTimeoutCenterServiceHelper;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER;

@Service("coftPaymentService")
public class CoftPaymentService implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(CoftPaymentService.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    NativeRetryPaymentFlowService nativeRetryPaymentFlowService;

    @Autowired
    @Qualifier("nativeSeamlessflowservice")
    private NativeSeamlessFlowService nativeSeamlessflowservice;

    @Autowired
    MappingUtil mapUtils;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private AoaTimeoutCenterServiceHelper aoaTimeoutCenterServiceHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {
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
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());

        // If merchant sent token in request then fetch userDetails from OAuth
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

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
        }

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
                // LOGGER.error("Exception occurred while fetching subscription details ");
                // return new
                // GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                // ResponseConstants.SESSION_EXPIRY);
                // }
                //
                // SubscriptionResponse subscriptionResponse = new
                // SubscriptionResponse();
                // mapUtils.mapAOASubsResponseIntoSubsResponse(aoaSubscriptionCreateResponse,
                // subscriptionResponse);
                // workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            } else {
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                        .toString());

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

            }
            workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);
        workFlowHelper.setLinkPaymentsUserDetails(workFlowTransBean);

        // Cache CC card/IMPS info
        // Cache NB/Mandate In case Of NATIVE_MF AND NATIVE_ST
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);

        if (createCacheCardResponse != null)
            return createCacheCardResponse;

        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);

        boolean addMoneyPcfEnabled = isAddMoneyPcfEnabled(flowRequestBean, workFlowTransBean);

        if (addMoneyPcfEnabled) {
            final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                    .consultAddMoneyV2(workFlowTransBean);
            if (!consultAddMoney.isSuccessfullyProcessed()
                    || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                    || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney
                            .getResponse().isLimitApplicable())) {
                return new GenericCoreResponseBean<>("AddMoneyNotAllowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }
        // Setting pcf
        if (addMoneyPcfEnabled || flowRequestBean.isPostConvenience()) {
            if (!workFlowHelper.isSimplifiedFlow(workFlowTransBean.getWorkFlowBean())) {
                GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, null);
                if (flowRequestBean.isPostConvenience()) {
                    workFlowTransBean.setPostConvenienceFeeModel(true);
                }
                workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
            }
        }

        // Code change for savedcard payment
        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            boolean isEligible = workFlowHelper.checkSubscriptionPaymentModeEligibility(flowRequestBean);
            if (!isEligible) {
                LOGGER.error("Entered Payment Details is Not valid for subscription");
                return new GenericCoreResponseBean<>("Entered Payment Details is Not valid for subscription",
                        ResponseConstants.SYSTEM_ERROR);
            }
        }

        /*
         * if TransId/AcquirementId is present, this means order has been
         * created for this payment, so now we'll call Pay API
         */
        if (StringUtils.isNotBlank(flowRequestBean.getTransID())) {
            // Setting transId
            workFlowTransBean.setTransID(flowRequestBean.getTransID());
            /*
             * call pay API
             */
            LOGGER.info("pay from CoftpayService");
            final GenericCoreResponseBean<BizPayResponse> payResponse = nativeRetryPaymentFlowService.callPayAPI(
                    flowRequestBean, workFlowTransBean);
            GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = nativeRetryPaymentFlowService
                    .validatePayAPIResponse(payResponse, flowRequestBean, workFlowTransBean);
            /*
             * if validatePayResponse is not null, it means Pay API was not
             * processed successfully, so return the error received
             */
            if (validatePayResponse != null) {
                return validatePayResponse;
            }
            nativeRetryPaymentFlowService.setCashierRequestIdInWorkFlowTransBean(payResponse, flowRequestBean,
                    workFlowTransBean);

        } else {
            // Create Order And Pay
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);

            if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                LOGGER.error("createOrderAndPay failed: {}", createOrderAndPayResponse.getFailureMessage());

                if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType()) || ERequestType
                        .isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType()))
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
                            createOrderAndPayResponse.getFailureMessage(),
                            createOrderAndPayResponse.getResponseConstant(),
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

            workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
            workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
            modifyOrderForOnusRentPayment(createOrderAndPayResponse.getResponse(), flowRequestBean);

            if (workFlowTransBean.getWorkFlowBean().isFromAoaMerchant()
                    && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaytmMID())
                    && ff4JUtil.isFeatureEnabled(PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER, workFlowTransBean
                            .getWorkFlowBean().getPaytmMID())) {
                try {
                    aoaTimeoutCenterServiceHelper.persistOrderInfoAtAoaTimeoutCenter(workFlowTransBean
                            .getWorkFlowBean(), createOrderAndPayResponse.getResponse().getAcquirementId());
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Error while calling aoa timeout center");
                }
            }
        }

        flowRequestBean.setTransID(workFlowTransBean.getTransID());
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        // Set TxnId in Cache for native Txn
        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())
                || ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(), workFlowTransBean.getTransID());
        }

        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            workFlowHelper.setOrderTimeOutForSubscription(workFlowTransBean.getWorkFlowBean());
        }

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        RetryStatus retryStatus = null;

        if (isAllowedPaymentModeForFetchBankForm(flowRequestBean, workFlowTransBean)) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            retryStatus = queryPayResultResponse != null ? queryPayResultResponse.getRetryStatus() : null;

            if (queryPayResultResponse != null && !queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant(), retryStatus);
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);
            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(null);

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                LOGGER.info("paymentStatus : {}", queryPayResultResponse.getResponse().getPaymentStatusValue());
                setFailureTypeForNative(flowRequestBean, workFlowTransBean, queryPayResultResponse);
            } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
                LOGGER.error("webFormContext is blank");
                if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                        || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())
                        || ERequestType.isSubscriptionRequest(flowRequestBean.getRequestType().getType())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                            NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
                }

            }
        }

        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        workFlowResponseBean.setIdempotent(workFlowTransBean.isIdempotent());

        if (ff4JUtil.isFeatureEnabled("theia.setDirectBankCardFlowDataForCoftTxn", workFlowTransBean.getWorkFlowBean()
                .getPaytmMID())
                && (workFlowTransBean.getWorkFlowBean().isDirectBankCardFlow())
                && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowRequestBean.getPaymentTypeId()))) {
            LOGGER.info("Saving Data For DirectBankCard Flow");
            flowRequestBean.setDirectBankCardFlow(true);
            theiaSessionRedisUtil
                    .set(ConfigurationUtil.getProperty("directBankRequestBeanKey")
                            + workFlowTransBean.getCashierRequestId(), flowRequestBean,
                            Long.parseLong(ConfigurationUtil.getProperty("directBankRedisTimeOut", "300")));
        }

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "CoftFlowService",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean, queryPayResultResponse.getRetryStatus());
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

    private boolean isAllowedPaymentModeForFetchBankForm(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean) {

        if (workFlowTransactionBean.isPaymentDone())
            return false;

        return (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.EMI.value
                    .equals(flowRequestBean.getPaymentTypeId()));
    }

    private void setFailureTypeForNative(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType()) || ERequestType
                    .isSubscriptionRequest(flowRequestBean.getRequestType().getType()))
                && (ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.name().equals(
                        queryPayResultResponse.getResponse().getInstErrorCode())
                        || ResponseConstants.FGW_PREFERRED_OTP_PAGE_BANK_FAILURE.name().equals(
                                queryPayResultResponse.getResponse().getInstErrorCode()) || ResponseConstants.FGW_PREFERRED_OTP_PAGE_MERCHANT_FAILURE
                        .name().equals(queryPayResultResponse.getResponse().getInstErrorCode()))) {

            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                    NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
        }
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
