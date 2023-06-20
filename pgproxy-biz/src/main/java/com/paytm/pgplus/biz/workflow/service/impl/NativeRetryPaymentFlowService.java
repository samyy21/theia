package com.paytm.pgplus.biz.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.AddMoneySourceEnum;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
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
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.pgproxycommon.enums.CardScheme.AMEX;

/**
 * @author kartik
 * @date 24-May-2018
 */
@Service("nativeRetryPaymentFlowService")
public class NativeRetryPaymentFlowService implements IWorkFlow {

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
    @Qualifier("nativeSeamlessflowservice")
    private NativeSeamlessFlowService nativeSeamlessflowservice;

    @Autowired
    MappingUtil mapUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRetryPaymentFlowService.class);

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Initiating NativeRetryPaymentFlow : {}", flowRequestBean);

        /*
         * validating workFlowRequestBean
         */
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
         * Store_Card: 0 Card is not to be saved | 1 Card is to be saved
         */
        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean storeCard = (flowRequestBean.getStoreCard() != null)
                && "1".equals(flowRequestBean.getStoreCard().trim());

        final boolean isTxnByCardIndexNo = workFlowHelper.checkForCardIndexNo(flowRequestBean);

        /*
         * If merchant has sent paytmSSOToken in request then fetch userDetails
         * from OAuth
         */
        if (isUserLoggedIn(flowRequestBean)) {
            /*
             * fetch userDetails
             */

            if (flowRequestBean.getUserDetailsBiz() != null) {
                userDetails = new GenericCoreResponseBean<>(flowRequestBean.getUserDetailsBiz());
            } else {
                userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            }

            LOGGER.info("userDetails : {}", userDetails);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            flowRequestBean.setUserDetailsBiz(userDetails.getResponse());
            if (flowRequestBean.getExtendInfo() == null)
                flowRequestBean.setExtendInfo(new ExtendedInfoRequestBean());
            flowRequestBean.getExtendInfo().setPaytmUserId(userDetails.getResponse().getUserId()); // For
                                                                                                   // PG2
        }

        /*
         * if RequestType is NATIVE_SUBSCRIPTION, get SubscriptionResponse from
         * Cache
         */
        if (!checkForNativeSubscription(flowRequestBean, workFlowTransBean)) {
            LOGGER.error("Exception occurred while fetching subscription details ");
            return new GenericCoreResponseBean<>("Exception occurred while fetching subscription details ",
                    ResponseConstants.SYSTEM_ERROR);
        }

        /**
         * Needed to support Zomato usecase as for txn with cardIndexNo is
         * already processed
         */
        if (flowRequestBean.getExpiryMonth() == null) {
            if (isSavedCardTxn && !isTxnByCardIndexNo) {
                if (isAllowedPaymentModeForSavedCard(flowRequestBean)) {
                    LOGGER.info("Transaction through CC/DC/IMPS/UPI collect/EMI for SavedCard");
                    EventUtils.pushTheiaEvents(EventNameEnum.PAYMENT_USING_SAVED_CARD);
                    userDetails = fetchSavedCardByCardId(flowRequestBean, userDetails);
                    if (!userDetails.isSuccessfullyProcessed()) {
                        LOGGER.error("Exception occurred while fetching saved card details: {}",
                                userDetails.getFailureMessage());
                        return new GenericCoreResponseBean<>(userDetails.getFailureMessage(),
                                userDetails.getResponseConstant());
                    }
                    // setting payOption for amex cards for saved-card-emi txns
                    if (PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId())) {
                        if (StringUtils.isNotBlank(flowRequestBean.getCardScheme())
                                && flowRequestBean.getCardScheme().equals(AMEX.name())) {
                            flowRequestBean.setPayOption(TheiaConstant.ExtraConstants.EMI.concat("_").concat(
                                    AMEX.name()));
                        }
                    }
                }
            } else if (isTxnByCardIndexNo) {
                final GenericCoreResponseBean<WorkFlowResponseBean> mapWorkFlowBeanResponse = mapFlowRequestBeanForPaymentFromCardIndexNo(
                        flowRequestBean, isTxnByCardIndexNo);
                if (mapWorkFlowBeanResponse != null)
                    return mapWorkFlowBeanResponse;
            }
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);
        // Cache CC card/IMPS info
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
        if (createCacheCardResponse != null)
            return createCacheCardResponse;

        /*
         * If add Money case, consult wallet
         */
        boolean addMoneyPcfEnabled = isAddMoneyPcfEnabled(flowRequestBean, workFlowTransBean);
        if (addMoneyPcfEnabled
                || (EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid()) && !ERequestType
                        .isNativeOrNativeSubscriptionRequest(flowRequestBean.getRequestType().getType()))) {
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

        // Code change for savedcard payment
        if (ERequestType.NATIVE_SUBSCRIPTION.equals(flowRequestBean.getRequestType())) {
            boolean isEligible = workFlowHelper.checkSubscriptionPaymentModeEligibility(flowRequestBean);
            if (!isEligible) {
                return new GenericCoreResponseBean<>("Entered Payment Details is Not valid for subscription",
                        ResponseConstants.SYSTEM_ERROR);
            }
        }

        LOGGER.debug("NativeRetryPaymentFlow:WorkFlowTransBean, Updated : {}", workFlowTransBean);

        // consult fee
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
        /*
         * call pay API
         */
        LOGGER.info("pay from NativeRetry");
        final GenericCoreResponseBean<BizPayResponse> payResponse = callPayAPI(flowRequestBean, workFlowTransBean);

        GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = validatePayAPIResponse(payResponse,
                flowRequestBean, workFlowTransBean);
        /*
         * if validatePayResponse is not null, it means Pay API was not
         * processed successfully, so return the error received
         */
        if (validatePayResponse != null) {
            return validatePayResponse;
        }

        setCashierRequestIdInWorkFlowTransBean(payResponse, flowRequestBean, workFlowTransBean);

        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            workFlowHelper.setOrderTimeOutForSubscription(workFlowTransBean.getWorkFlowBean());
        }

        // Caching card details in Redis
        seamlessCoreService
                .cacheCardInRedis(flowRequestBean, workFlowTransBean, userDetails, isSavedCardTxn, storeCard);

        modifyOrderForOnusRentPayment(payResponse.getResponse(), flowRequestBean);

        RetryStatus retryStatus = null;
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        /*
         * Fetch Bank Form using Query_PayResult API for paymentMode
         */
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
                        || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                            NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
                }
            }
        }

        /*
         * checks payment status and txn status for paymentMode whose status
         * needs to be known here itself
         */
        if (isAllowedPaymentModeForFetchPaymentStatus(flowRequestBean, workFlowTransBean)) {

            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);

            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL or when TimeOut
            // occurs in case of Offline
            String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

            if (PaymentStatus.FAIL.name().equals(paymentStatus)) {
                /*
                 * closeOrder after checking if retry is allowed
                 */
                nativeSeamlessflowservice.closeOrder(workFlowTransBean);

            } else if (PaymentStatus.PROCESSING.toString().equals(paymentStatus)) {
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
                    return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                if (queryByAcquirementIdResponse.getResponse() != null) {
                    queryByAcquirementIdResponse.getResponse().setMerchantTransId(flowRequestBean.getOrderID());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }
        }

        /*
         * Setting data in workFlowResponse bean
         */
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        workFlowResponseBean.setIdempotent(workFlowTransBean.isIdempotent());

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
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "NativeRetryPaymentFlow",
                workFlowResponseBean.getTransID());

        return new GenericCoreResponseBean<>(workFlowResponseBean, retryStatus);
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

    private boolean isUserLoggedIn(WorkFlowRequestBean workFlowRequestBean) {
        return StringUtils.isNotBlank(workFlowRequestBean.getToken());
    }

    private boolean isAddMoneyPcfEnabled(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean flowTransactionBean) {
        boolean isVersionSupported = checkIfVersionSupported(flowRequestBean);
        if (isVersionSupported
                && flowRequestBean.isAddMoneyPcfEnabled()
                && EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())
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

    private boolean isAllowedPaymentModeForSavedCard(WorkFlowRequestBean flowRequestBean) {
        return flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value);

    }

    private boolean isAllowedPaymentModeForFetchBankForm(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean) {

        if (workFlowTransactionBean.isPaymentDone())
            return false;

        return (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.NB.value.equals(flowRequestBean.getPaymentTypeId())
                || EPayMethod.PPBL.getOldName().equals(flowRequestBean.getBankCode())
                || PaymentTypeIdEnum.UPI.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.WALLET.value
                    .equals(flowRequestBean.getPaymentTypeId()));
    }

    private boolean isAllowedPaymentModeForFetchPaymentStatus(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean) {
        return (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || PaymentTypeIdEnum.PPI.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.COD.value.equals(flowRequestBean.getPaymentTypeId())
                || EPayMethod.MP_COD.getMethod().equalsIgnoreCase(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.GIFT_VOUCHER.value.equals(flowRequestBean.getPaymentTypeId()) || workFlowTransactionBean
                    .isPaymentDone());
    }

    private GenericCoreResponseBean<UserDetailsBiz> fetchSavedCardByCardId(WorkFlowRequestBean flowRequestBean,
            GenericCoreResponseBean<UserDetailsBiz> userDetails) {
        if (userDetails != null) {
            userDetails = savedCardsService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(),
                    userDetails.getResponse(), flowRequestBean);

        } else if (StringUtils.isNotBlank(flowRequestBean.getCustID())) {
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

    private boolean checkForNativeSubscription(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {

        if (ERequestType.isSubscriptionCreationRequest(workFlowTransBean.getWorkFlowBean().getRequestType().getType())) {
            StringBuilder key = new StringBuilder(workFlowTransBean.getWorkFlowBean().getRequestType().getType())
                    .append(workFlowTransBean.getWorkFlowBean().getTxnToken());
            if (flowRequestBean.isFromAoaMerchant()) {
                LOGGER.error("AOA subscription client call is being used");
                // AoaSubscriptionCreateResponse aoaSubscriptionCreateResponse =
                // (AoaSubscriptionCreateResponse)
                // theiaTransactionalRedisUtil.get(
                // key.toString());
                //
                // if (aoaSubscriptionCreateResponse == null) {
                // return false;
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
                    return false;
                }
                // cache upi recurring details
                workFlowHelper.cacheUPIRecurringInfo(flowRequestBean);

                subscriptionResponse.setSavedCardID(flowRequestBean.getSavedCardID());
                if (SubsPaymentMode.UNKNOWN.equals(subscriptionResponse.getPaymentMode())) {
                    subscriptionResponse.setPaymentMode(flowRequestBean.getSubsPayMode());
                }
                workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            }
            // required at the time of activate subscription in ppp.
            workFlowHelper.setAdditionParamsForNativeSubs(workFlowTransBean);
        }
        return true;
    }

    private void setFailureTypeForNative(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType()) || ERequestType.UNI_PAY
                .equals(flowRequestBean.getRequestType()))
                && (ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED.name().equals(
                        queryPayResultResponse.getResponse().getInstErrorCode())
                        || ResponseConstants.FGW_PREFERRED_OTP_PAGE_BANK_FAILURE.name().equals(
                                queryPayResultResponse.getResponse().getInstErrorCode()) || ResponseConstants.FGW_PREFERRED_OTP_PAGE_MERCHANT_FAILURE
                        .name().equals(queryPayResultResponse.getResponse().getInstErrorCode()))) {

            workFlowTransBean.getWorkFlowBean().setPaymentFailureType(
                    NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED);
        }
    }

    public GenericCoreResponseBean<BizPayResponse> callPayAPI(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {

        final GenericCoreResponseBean<BizPayResponse> payResponse;
        LOGGER.info("Pay called from NativeRetryPaymentFlowService");
        payResponse = workFlowHelper.pay(workFlowTransBean);
        return payResponse;
    }

    public GenericCoreResponseBean<WorkFlowResponseBean> validatePayAPIResponse(
            GenericCoreResponseBean<BizPayResponse> payResponse, WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {
        LOGGER.info("BizPayResponse : {}", payResponse);
        if (!payResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Pay API call failed due to : {}", payResponse.getFailureMessage());

            if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType()) || ERequestType.UNI_PAY
                    .equals(flowRequestBean.getRequestType()))
                    && payResponse.getResponseConstant() != null
                    && ResponseConstants.RISK_REJECT.equals(payResponse.getResponseConstant())) {
                workFlowTransBean.getWorkFlowBean().setPaymentFailureType(NativePaymentFailureType.RISK_REJECT);
            }

            if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {

                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                        payResponse.getResponseConstant(), payResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                return responseBean;
            }
            if (ResponseConstants.TRANS_PAID.equals(payResponse.getResponseConstant())
                    || ResponseConstants.ORDER_IS_PAID.equals(payResponse.getResponseConstant())) {
                workFlowTransBean.setPaymentDone(true);
                workFlowTransBean.setIdempotent(true);
                LOGGER.info("{} received in pay API", payResponse.getResponseConstant().getAlipayResultMsg());
                return null;
            }
            return new GenericCoreResponseBean<>(payResponse.getFailureMessage(), payResponse.getResponseConstant());
        }
        LOGGER.info("Pay API called successfully");
        return null;
    }

    private boolean isNativeTxn(WorkFlowRequestBean workFlowRequestBean) {
        return (workFlowRequestBean != null && workFlowRequestBean.getRequestType() != null && ERequestType.NATIVE
                .equals(workFlowRequestBean.getRequestType()));
    }

    private void modifyOrderForOnusRentPayment(BizPayResponse createOrderAndPayResponseBean,
            WorkFlowRequestBean flowRequestBean) {

        if (flowRequestBean.isOnusRentPaymentMerchant() && createOrderAndPayResponseBean != null) {
            SecurityPolicyResult securityPolicyResult = createOrderAndPayResponseBean.getSecurityPolicyResult();

            if (securityPolicyResult != null && securityPolicyResult.getRiskResult() != null) {

                Map<String, String> riskInfoMap = new HashMap<>();
                riskInfoMap.put(RISK_INFO, securityPolicyResult.getRiskResult().getRiskInfo());
                workFlowHelper.modifyOrderForOnusRentPayment(flowRequestBean, riskInfoMap);

            }
        }
    }

    public void setCashierRequestIdInWorkFlowTransBean(GenericCoreResponseBean<BizPayResponse> payResponse,
            WorkFlowRequestBean flowRequestBean, WorkFlowTransactionBean flowTransactionBean) {
        if (payResponse.getResponse() == null) {
            String cashierRequestId = (String) theiaSessionRedisUtil.hget(flowRequestBean.getTxnToken(),
                    "cashierRequestId");
            flowTransactionBean.setCashierRequestId(cashierRequestId);
        } else {
            flowTransactionBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                flowTransactionBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
        }
    }

}
