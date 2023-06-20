/**
 * Subscription payment request without user token, Browser Call refer sequence
 * diagram - 42
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.workflow.model.SubscriptionRenewalResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceRequest;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceRequestData;
import com.paytm.pgplus.facade.wallet.services.IFetchWalletBalanceService;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput.UserOwner;
import com.paytm.pgplus.subscriptionClient.enums.SubscriptionRequestType;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.RENEWAL_SUCCESS_CODE;

/**
 * @author namanjain
 */
@SuppressWarnings("Duplicates")
@Service("subscriptionRenewalWorkflow")
public class SubscriptionRenewalWorkflow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRenewalWorkflow.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SubscriptionRenewalWorkflow.class);
    @Autowired
    @Qualifier("userMappingServiceImpl")
    IUserMappingService userMappingService;
    @Autowired
    @Qualifier("subscriptionRenewalCCOnlyValidator")
    private IValidator validatorService;
    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    private IFetchWalletBalanceService fetchWalletBalanceService;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("mapUtilsBiz")
    MappingUtil mappingUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.specificBeanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setDebitDate(flowRequestBean.getDebitDate());

        // Subscription service

        SubscriptionResponse subscriptionServiceResponse = null;

        if (ERequestType.SUBSCRIPTION_PARTIAL_RENEWAL.equals(workFlowTransBean.getWorkFlowBean().getRequestType()))
            subscriptionServiceResponse = workFlowHelper.processPartialRenewSubscription(workFlowTransBean);
        else
            subscriptionServiceResponse = workFlowHelper.processRenewSubscription(workFlowTransBean);

        if (!(RENEWAL_SUCCESS_CODE.equals(subscriptionServiceResponse.getRespCode()))) {
            LOGGER.error("Subscription renewal failed due to ::{}", subscriptionServiceResponse.getRespMsg());
            return returnResponseForInvalidRenewal(flowRequestBean, subscriptionServiceResponse);
        }
        workFlowTransBean.setSubscriptionServiceResponse(subscriptionServiceResponse);

        workFlowHelper.setAdditionalParamsForSubsRenewal(workFlowTransBean);

        if (StringUtils.isNotBlank(subscriptionServiceResponse.getPayerUserID())) {
            try {
                UserDetailsBiz usDetailsBiz = new UserDetailsBiz();
                UserInfo mappingUserData = userMappingService.getUserData(subscriptionServiceResponse.getPayerUserID(),
                        UserOwner.ALIPAY);
                usDetailsBiz.setUserId(mappingUserData.getPaytmId());
                usDetailsBiz.setInternalUserId(mappingUserData.getAlipayId());
                workFlowTransBean.setUserDetails(usDetailsBiz);
            } catch (MappingServiceClientException e) {
                LOGGER.error(
                        "Subscription renewal failed because user mapping was not obatined for id : {} , owner : {} ",
                        subscriptionServiceResponse.getPayerUserID(), UserOwner.ALIPAY.name());
                return returnResponseForSystemError(flowRequestBean, subscriptionServiceResponse);
            }
        }

        /**
         * When SubsPaymentMode = NORMAL, in SUBS_PPI_ONLY = N case, savedCardId
         * will come in subscriptionServiceResponse before savedCardMigration
         * and CIN in subscriptionServiceResponse post savedCardMigration, So in
         * order to complete the txn successfully, CIN too have to be checked in
         * subscriptionServiceResponse.
         */

        if (!SubsPaymentMode.PPI.equals(subscriptionServiceResponse.getPaymentMode())
                && !SubsPaymentMode.PPBL.equals(subscriptionServiceResponse.getPaymentMode())
                && !SubsPaymentMode.BANK_MANDATE.equals(subscriptionServiceResponse.getPaymentMode())
                && !SubsPaymentMode.UPI.equals(subscriptionServiceResponse.getPaymentMode())
                && !(SubsPaymentMode.NORMAL.equals(subscriptionServiceResponse.getPaymentMode()) && (StringUtils
                        .isBlank(subscriptionServiceResponse.getSavedCardID()) && StringUtils
                        .isBlank(subscriptionServiceResponse.getCardIndexNumber())))) {

            final GenericCoreResponseBean<CardBeanBiz> savedCard;
            // integrate fetch card info from platform non sensitive info api
            if (StringUtils.isNotBlank(subscriptionServiceResponse.getCardIndexNumber())
                    && ff4JUtil.useCINForSubsRenewal(workFlowTransBean.getWorkFlowBean().getPaytmMID())) {
                LOGGER.info("Renewal via CIN {}, for subs id {}", subscriptionServiceResponse.getCardIndexNumber(),
                        subscriptionServiceResponse.getSubscriptionId());
                savedCard = getCardBeanBizFromNonSensitiveCardInfo(subscriptionServiceResponse.getCardIndexNumber());
                workFlowTransBean.getWorkFlowBean().setTxnFromCardIndexNo(true);
                if (savedCard != null && savedCard.getResponse() != null) {
                    EXT_LOGGER.customInfo("savedCard to WorkflowTransbean");
                    workFlowTransBean.getWorkFlowBean().setBinDetail(
                            workFlowHelper.getBinDetail(savedCard.getResponse().getFirstSixDigit()));
                }
            } else if (StringUtils.isNotBlank(subscriptionServiceResponse.getPayerUserID())) {
                LOGGER.info("Renewal via savedCardId {}, for subs id {}", subscriptionServiceResponse.getSavedCardID(),
                        subscriptionServiceResponse.getSubscriptionId());
                savedCard = workFlowHelper.fetchSavedCardByID(workFlowTransBean);
            } else {
                LOGGER.info("Renewal via savedCardId {}, for subs id {}", subscriptionServiceResponse.getSavedCardID(),
                        subscriptionServiceResponse.getSubscriptionId());
                savedCard = workFlowHelper.fetchSavedCardByCustIdMid(workFlowTransBean);
            }

            if (!savedCard.isSuccessfullyProcessed()) {
                return returnResponseForInvalidSavedCard(flowRequestBean);
            }

            if (!workFlowHelper.validateSavedCardForSubscription(flowRequestBean.isFromAoaMerchant(),
                    savedCard.getResponse(), subscriptionServiceResponse.getPaymentMode(),
                    SubscriptionRequestType.RENEW)) {
                LOGGER.info("Saved Card ID : {} is not allowed anymore for the SubsPayment Mode : {} ", savedCard
                        .getResponse().getCardId(), subscriptionServiceResponse.getPaymentMode());
                return returnResponseForInvalidSavedCard(flowRequestBean);
            }
            if (SubsPaymentMode.DC.equals(subscriptionServiceResponse.getPaymentMode())) {
                workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.DC.value);
                savedCard.getResponse().setCardType(PayMethod.DEBIT_CARD.name());
                workFlowTransBean.getWorkFlowBean().setPayOption(
                        PayMethod.DEBIT_CARD + "_" + savedCard.getResponse().getCardScheme());
            } else if (SubsPaymentMode.NORMAL.equals(subscriptionServiceResponse.getPaymentMode())) {
                if (PayMethod.DEBIT_CARD.name().equals(savedCard.getResponse().getCardType())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.DC.value);
                    workFlowTransBean.getWorkFlowBean().setPayOption(
                            PayMethod.DEBIT_CARD + "_" + savedCard.getResponse().getCardScheme());
                } else {
                    workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.CC.value);
                    savedCard.getResponse().setCardType(PayMethod.CREDIT_CARD.name());
                    workFlowTransBean.getWorkFlowBean().setPayOption(
                            PayMethod.CREDIT_CARD + "_" + savedCard.getResponse().getCardScheme());
                }
            } else {
                workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.CC.value);
                savedCard.getResponse().setCardType(PayMethod.CREDIT_CARD.name());
                workFlowTransBean.getWorkFlowBean().setPayOption(
                        PayMethod.CREDIT_CARD + "_" + savedCard.getResponse().getCardScheme());
            }
            workFlowTransBean.setSavedCard(savedCard.getResponse());

            // Cache CC Card to Alipay
            workFlowTransBean.getWorkFlowBean().setCardIndexNo(savedCard.getResponse().getCardIndexNo());
            GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.NORMAL);

            if (!cacheCardResponse.isSuccessfullyProcessed()) {
                return returnResponseForInvalidSavedCard(flowRequestBean);
            }
            workFlowTransBean.setCacheCardToken(cacheCardResponse.getResponse().getTokenId());
        } else if (SubsPaymentMode.BANK_MANDATE.equals(subscriptionServiceResponse.getPaymentMode())) {
            workFlowHelper.setAdditionParamsForMandate(workFlowTransBean, subscriptionServiceResponse);
            workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.BANK_MANDATE.value);

            // Cache Mandate to Alipay
            GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.MANDATE);
            if (!cacheCardResponse.isSuccessfullyProcessed()) {
                return returnResponseForInvalidSavedCard(flowRequestBean);
            }
            workFlowTransBean.setCacheCardToken(cacheCardResponse.getResponse().getTokenId());
        } else if (SubsPaymentMode.UPI.equals(subscriptionServiceResponse.getPaymentMode())) {
            workFlowHelper.setAdditionParamsForUPIRecurringMandate(workFlowTransBean, subscriptionServiceResponse);
            workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.UPI.value);
        } else if (SubsPaymentMode.PPI.equals(subscriptionServiceResponse.getPaymentMode())) {
            workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.PPI.value);
            flowRequestBean.setPayMethod(TheiaConstant.ExtraConstants.WALLET_TYPE);
            flowRequestBean.setPayOption(TheiaConstant.ExtraConstants.WALLET_TYPE);
        }
        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        // initiate pay if it is for subscription auto retry case.
        if (workFlowTransBean.getWorkFlowBean().getExtendInfo().isSubsRenewOrderAlreadyCreated()) {
            workFlowTransBean.setTransID(flowRequestBean.getTransID());
            workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
            // since normal (add n pay) is not supported in case of auto retry
            // hence setting epaymode to be none
            workFlowTransBean.getWorkFlowBean().setPaytmExpressAddOrHybrid(EPayMode.NONE);
            LOGGER.info("Pay called from SubscriptionRenewalWorkFlow");
            GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
            if (!payResponse.isSuccessfullyProcessed()) {
                return returnResponseForPayNotSuccessfull(flowRequestBean, payResponse);
            }
            workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }

        } else {
            // Create Order iff future debitDate exists
            if (isCreateOrderRequiredForPreDebit(flowRequestBean, subscriptionServiceResponse)) {
                GenericCoreResponseBean<WorkFlowResponseBean> createOrderResponse = getCreateOrderResponseForPreDebit(
                        flowRequestBean, workFlowTransBean);
                if (createOrderResponse != null) {
                    return createOrderResponse;
                }
            } else {
                // Create Order And Pay
                final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay = workFlowHelper
                        .createOrderAndPay(workFlowTransBean, true);
                if (!createOrderAndPay.isSuccessfullyProcessed()) {
                    return returnResponseForOrderNotCreated(flowRequestBean, createOrderAndPay);
                }
                workFlowTransBean.setCashierRequestId(createOrderAndPay.getResponse().getCashierRequestId());
                if (createOrderAndPay.getResponse().getSecurityPolicyResult() != null) {
                    workFlowTransBean.setRiskResult(createOrderAndPay.getResponse().getSecurityPolicyResult()
                            .getRiskResult());
                }
                workFlowTransBean.setTransID(createOrderAndPay.getResponse().getAcquirementId());
            }
        }

        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        SubscriptionRenewalResponse renewalResponse = createRenewalResponse(flowRequestBean, workFlowTransBean,
                subscriptionServiceResponse);

        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);

        LOGGER.info("Returning Response Bean From SubscriptionRenewalCCOnly, trans Id : {} ",
                renewalResponse.getTxnId());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> getCreateOrderResponseForPreDebit(
            WorkFlowRequestBean flowRequestBean, WorkFlowTransactionBean workFlowTransBean) {

        LOGGER.info("PreDebit case: Initiating createOrder");
        GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            LOGGER.info("PreDebit case: createOrder failed due to: {}", createOrderResponse.getFailureMessage());
            return returnResponseForFailedCreateOrder(flowRequestBean, createOrderResponse);
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());
        return null;
    }

    /**
     * This method checks if current Renewal is case of PreDebit by checking
     * debitDate is present && debitDate is after today i.e. neither
     * orderActiveDays nor isOrderInactiveTimeOutEnabled are null
     * 
     * @param flowRequestBean
     * @param subscriptionResponse
     * @return
     */
    private boolean isCreateOrderRequiredForPreDebit(WorkFlowRequestBean flowRequestBean,
            SubscriptionResponse subscriptionResponse) {
        if (null == flowRequestBean.getDebitDate() || null == subscriptionResponse.getOrderInactiveTimeOutEnabled()
                || null == subscriptionResponse.getOrderActiveDays()) {
            return false;
        }
        return true;
    }

    private SubscriptionRenewalResponse createRenewalResponse(final WorkFlowRequestBean flowRequestBean,
            final WorkFlowTransactionBean workFlowTransBean, final SubscriptionResponse subscriptionServiceResponse) {
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowTransBean.getTransID());
        renewalResponse.setRespCode(subscriptionServiceResponse.getRespCode());
        renewalResponse.setRespMsg(subscriptionServiceResponse.getRespMsg());
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_ACCEPTED.name());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        return renewalResponse;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForFailedCreateOrder(
            final WorkFlowRequestBean flowRequestBean,
            final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse) {

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());

        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        ResponseConstants responseCode = ResponseConstants.SYSTEM_ERROR;
        if (createOrderResponse != null && createOrderResponse.getResponseConstant() != null) {
            responseCode = createOrderResponse.getResponseConstant();
        }

        renewalResponse.setRespCode(responseCode.getCode());
        if (createOrderResponse != null && StringUtils.isNotBlank(createOrderResponse.getRiskRejectUserMessage())) {
            renewalResponse.setRespMsg(createOrderResponse.getRiskRejectUserMessage());
        } else {
            renewalResponse.setRespMsg(responseCode.getMessage());
        }
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForOrderNotCreated(
            final WorkFlowRequestBean flowRequestBean,
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        ResponseConstants responseCode = null;
        if (createOrderAndPay != null && createOrderAndPay.getResponseConstant() != null) {
            responseCode = createOrderAndPay.getResponseConstant();
        } else {
            responseCode = ResponseConstants.SYSTEM_ERROR;
        }

        renewalResponse.setRespCode(responseCode.getCode());
        if (createOrderAndPay != null && StringUtils.isNotBlank(createOrderAndPay.getRiskRejectUserMessage())) {
            renewalResponse.setRespMsg(createOrderAndPay.getRiskRejectUserMessage());
        } else {
            renewalResponse.setRespMsg(responseCode.getMessage());
        }
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForPayNotSuccessfull(
            final WorkFlowRequestBean flowRequestBean, final GenericCoreResponseBean<BizPayResponse> payResponse) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        ResponseConstants responseCode = payResponse.getResponseConstant() != null ? payResponse.getResponseConstant()
                : ResponseConstants.SYSTEM_ERROR;
        renewalResponse.setRespCode(responseCode.getCode());
        if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
            renewalResponse.setRespMsg(payResponse.getRiskRejectUserMessage());
        } else {
            renewalResponse.setRespMsg(responseCode.getMessage());
        }
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
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

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForSystemError(
            final WorkFlowRequestBean flowRequestBean, final SubscriptionResponse subscriptionServiceResponse) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());
        renewalResponse.setRespCode(ResponseConstants.SYSTEM_ERROR.name());
        renewalResponse.setRespMsg(ResponseConstants.SYSTEM_ERROR.getMessage());
        renewalResponse.setStatus(subscriptionServiceResponse.getStatus().getName());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForInvalidSavedCard(
            final WorkFlowRequestBean flowRequestBean) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setOrderId(flowRequestBean.getOrderID());
        renewalResponse.setMid(flowRequestBean.getPaytmMID());
        renewalResponse.setSubsId(flowRequestBean.getSubscriptionID());
        renewalResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        renewalResponse.setTxnId(workFlowResponseBean.getTransID());
        renewalResponse.setRespCode(ResponseConstants.INVALID_SAVED_CARD_ID.getCode());
        renewalResponse.setRespMsg(workFlowHelper
                .getResponseForResponseConstant(ResponseConstants.INVALID_SAVED_CARD_ID));
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (flowRequestBean.getExtendInfo() != null) {
            renewalResponse.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        workFlowResponseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private WalletBalanceRequest getWalletBalanceRequest(String isDetailInfo, String isClubSubwalletsRequired,
            WorkFlowTransactionBean workFlowTransactionBean) {
        WalletBalanceRequest walletBalanceRequest = new WalletBalanceRequest();
        WalletBalanceRequestData request = new WalletBalanceRequestData();
        request.setIsDetailInfo(isDetailInfo);
        request.setIsClubSubwalletsRequired(isClubSubwalletsRequired);
        request.setMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        // setting subwallet details.
        Map<UserSubWalletType, BigDecimal> subWalletAmountDetails = workFlowTransactionBean.getWorkFlowBean()
                .getSubWalletOrderAmountDetails();
        Map<String, BigDecimal> subWalletDetails = new HashMap<>();
        if (null != subWalletAmountDetails) {
            for (Map.Entry<UserSubWalletType, BigDecimal> subWallet : subWalletAmountDetails.entrySet()) {
                subWalletDetails.put(subWallet.getKey().getType(), subWallet.getValue());
            }
            request.setSubWalletAmount(subWalletDetails);
        }
        walletBalanceRequest.setRequest(request);
        return walletBalanceRequest;
    }

    private GenericCoreResponseBean<CardBeanBiz> getCardBeanBizFromNonSensitiveCardInfo(String cardIndexNumber) {
        QueryNonSensitiveAssetInfoResponse response = cardCenterHelper
                .queryNonSensitiveAssetInfo(null, cardIndexNumber);
        if (response != null && response.getCardInfo() != null) {
            return mappingUtil.mapNonSensitiveAssetInfoToCardBeanBiz(response.getCardInfo());
        }
        return new GenericCoreResponseBean<CardBeanBiz>("Card Details could not be fetched");
    }

}
