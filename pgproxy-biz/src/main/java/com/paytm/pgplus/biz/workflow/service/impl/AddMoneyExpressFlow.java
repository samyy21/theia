package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.BizProdHelper;
import com.paytm.pgplus.biz.workflow.service.helper.MerchantBizProdHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.enums.Route;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.enums.AddMoneySourceEnum.THIRD_PARTY;
import static com.paytm.pgplus.biz.utils.BizConstant.ENABLE_ADD_MONEY_SOURCE_IN_CONSULT;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_FLOW;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_MID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_ORDER_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_RETRY_COUNT;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_TXN_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_USER_ID;

@Service("addMoneyExpressFlow")
public class AddMoneyExpressFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyExpressFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("addMoneyExpressValidator")
    private IValidator validatorService;

    @Autowired
    private ISavedCards savedCardService;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("BizProdHelper")
    private BizProdHelper bizProdHelper;

    @Autowired
    private MerchantBizProdHelper merchantBizProdHelper;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private PG2Util pg2Util;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean isStoreCard = (flowRequestBean.getStoreCard() != null && TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1
                .equals(flowRequestBean.getStoreCard())) ? true : false;
        final boolean isTxnByCardIndexNo = checkForCardIndexNo(flowRequestBean);

        Map<String, String> extendInfo = getExtendInfoFromExtendBean(flowRequestBean);

        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());

        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            extendInfo.put(BizConstant.ExtendedInfoKeys.PAYTM_USER_ID, userDetails.getResponse().getUserId());

            if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                    || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())) {

                if (isSavedCardTxn) {
                    // Fetch details from DB and decrypt them then put details
                    // in UserDetailsBiz
                    if (!isTxnByCardIndexNo) {
                        userDetails = savedCardService.fetchSavedCardsByCardId(flowRequestBean.getSavedCardID(),
                                userDetails.getResponse(), flowRequestBean);
                        if (!userDetails.isSuccessfullyProcessed()) {
                            LOGGER.error("Exception occured while fetching saved card detail : ",
                                    userDetails.getFailureMessage());
                            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(),
                                    userDetails.getResponseConstant());

                        }
                    } else {
                        SavedAssetInfo savedAssetInfo = bizProdHelper.getSavedCardByUserIdAndCardId(
                                flowRequestBean.getSavedCardID(), userDetails.getResponse().getUserId());

                        if (null == savedAssetInfo) {

                            savedAssetInfo = merchantBizProdHelper.getSavedCardByMidCustIdAndCardId(
                                    flowRequestBean.getSavedCardID(), flowRequestBean.getPaytmMID(),
                                    flowRequestBean.getCustID());

                        }
                        if (null == savedAssetInfo) {
                            return new GenericCoreResponseBean<>(
                                    "Unable to fetch saved card data from bizProd/merchantProd ",
                                    ResponseConstants.SYSTEM_ERROR);
                        }
                        setDataFromBizProdMerchantProd(savedAssetInfo, flowRequestBean);
                    }
                    extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_NAME, flowRequestBean.getBankName());
                    extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_ID, flowRequestBean.getInstId());
                }

            }

        } else if (StringUtils.isBlank(flowRequestBean.getToken())) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>("Invalid Request data ",
                    ResponseConstants.SYSTEM_ERROR);
        }

        workFlowTransBean.setExtendInfo(extendInfo);

        workFlowTransBean.getWorkFlowBean().setTransType(ETransType.TOP_UP);
        workFlowTransBean.getWorkFlowBean().setRoute(
                pg2Util.getRouteForTopUpRequest(workFlowTransBean.getWorkFlowBean().getPaytmMID()));

        // cache CC card
        if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())) {
            final GenericCoreResponseBean<CacheCardResponseBean> cacheCard = workFlowHelper.cacheCard(
                    workFlowTransBean, true);
            if (!cacheCard.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(cacheCard.getFailureMessage(), cacheCard.getResponseConstant());
            }
            workFlowTransBean.setCacheCardToken(cacheCard.getResponse().getTokenId());
            if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled()) {
                setCardHashAndCardIndexNo(workFlowTransBean, cacheCard.getResponse(), isTxnByCardIndexNo);
            }
        }

        /*
         * Check for wallet limits Return a generic response code for all limit
         * failure scenarios
         */
        if (StringUtils.isNotBlank(flowRequestBean.getPaytmMID())
                && ff4JUtil.isFeatureEnabled(ENABLE_ADD_MONEY_SOURCE_IN_CONSULT, flowRequestBean.getPaytmMID())) {
            workFlowTransBean.getWorkFlowBean().setAddMoneySource(THIRD_PARTY.getValue());
        }
        final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                .consultAddMoneyV2(workFlowTransBean);
        if (!consultAddMoney.isSuccessfullyProcessed()
                || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney.getResponse()
                        .isLimitApplicable())) {
            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }

        String userKYCType = consultAddMoney.getResponse().getWalletRbiType();
        if ((StringUtils.equals(BizConstant.BASIC_KYC, userKYCType) || StringUtils.equals(BizConstant.PRIMITIVE_KYC,
                userKYCType))
                && StringUtils.equals(consultAddMoney.getResponse().getAddMoneyDestination(),
                        BizConstant.AddMoneyDestination.MAIN)) {
            workFlowTransBean.setOnTheFlyKYCRequired(true);
        }

        String addMoneyDestination = consultAddMoney.getResponse().getAddMoneyDestination();

        workFlowTransBean.setAddMoneyDestination(addMoneyDestination);

        if (UserSubWalletType.GIFT_VOUCHER.getType().equals(addMoneyDestination) && !flowRequestBean.isGvConsentFlow()) {
            LOGGER.info("GV consent required.");
            return new GenericCoreResponseBean<>(ResponseConstants.GV_CONSENT_REQUIRED.getMessage(),
                    ResponseConstants.GV_CONSENT_REQUIRED);
        }
        workFlowTransBean.setTrustFactor(consultAddMoney.getResponse().getTrustFactor());

        workFlowTransBean.setTrustFactor(consultAddMoney.getResponse().getTrustFactor());

        workFlowTransBean.setTrustFactor(consultAddMoney.getResponse().getTrustFactor());

        if (flowRequestBean.getCurrentInternalPaymentRetryCount() == 0) {
            // Create topup
            final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUpResponse = workFlowHelper.createTopUp(
                    workFlowTransBean, true);
            if (!createTopUpResponse.isSuccessfullyProcessed()) {
                LOGGER.debug("TopUp api call failed due to : ", createTopUpResponse.getFailureMessage());
                return new GenericCoreResponseBean<>(createTopUpResponse.getFailureMessage(),
                        createTopUpResponse.getResponseConstant());
            }
            workFlowTransBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());
            flowRequestBean.setTransID(workFlowTransBean.getTransID());
            LOGGER.info("Top up created with response", createTopUpResponse);
        } else {
            LOGGER.info("Top up already creted, Fund OrderId : {}", flowRequestBean.getTransID());
            workFlowTransBean.setTransID(flowRequestBean.getTransID());
        }

        if (workFlowHelper.isMidAllowedChargeFeeOnAddMoneyByWallet(flowRequestBean.getPaytmMID())) {
            String rejectMsg = getRejectMsgIfUserRisky(consultAddMoney, workFlowTransBean);
            if (StringUtils.isNotBlank(rejectMsg)) {
                closeFundOrderDueToRiskReject(workFlowTransBean);
                return new GenericCoreResponseBean<>("Add money not allowed , User is risky ",
                        ResponseConstants.RISK_REJECT, rejectMsg);
            }
        }
        LOGGER.info("Pay called from AddMoneyExpressFlow");

        // Pay
        final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
        if (!payResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Pay API call failed due to : ", payResponse.getFailureMessage());
            if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                        payResponse.getResponseConstant(), payResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                return responseBean;
            } else {
                return new GenericCoreResponseBean<>(payResponse.getFailureMessage(), payResponse.getResponseConstant());
            }
        }
        workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
        if (payResponse.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
        }
        LOGGER.debug("Pay API called successfully with response : ", payResponse);

        // Caching card details in Redis
        if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())) {
            if (!isSavedCardTxn && isStoreCard && userDetails != null) {

                userDetails = savedCardService.cacheCardDetails(flowRequestBean, userDetails.getResponse(),
                        workFlowTransBean.getTransID());
                if (!userDetails.isSuccessfullyProcessed()) {
                    LOGGER.error("Exception occurred while saving card in cache : ", userDetails.getFailureMessage());

                }
            }
        }

        // AddMoneyExpress-KYC
        String kycValidate = ConfigurationUtil.getProperty("KYC.VALIDATE", "N");
        String userId = workFlowTransBean.getUserDetails().getUserId();
        String userKycKey = "KYC_" + userId;
        boolean isKYCDone = theiaTransactionalRedisUtil.get(userKycKey) != null ? true : false;

        if (kycValidate.equalsIgnoreCase("Y") && !isKYCDone && workFlowTransBean.isOnTheFlyKYCRequired()) {
            HttpServletRequest request = workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getRequest();

            request.setAttribute(KYC_MID, workFlowTransBean.getWorkFlowBean().getPaytmMID());
            request.setAttribute(KYC_ORDER_ID, workFlowTransBean.getWorkFlowBean().getOrderID());
            request.setAttribute(KYC_FLOW, "YES");
            request.setAttribute(KYC_TXN_ID, workFlowTransBean.getTransID());
            request.setAttribute(KYC_USER_ID, userId);
            request.setAttribute(KYC_RETRY_COUNT, "0");

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MID=").append(workFlowTransBean.getWorkFlowBean().getPaytmMID());
            queryBuilder.append("&ORDER_ID=").append(workFlowTransBean.getWorkFlowBean().getOrderID());
            queryBuilder.append("&route=");

            request.setAttribute("queryStringForSession", queryBuilder.toString());
            String TRANS_KYC_KEY = "TRANS_KYC_" + workFlowTransBean.getTransID();

            theiaTransactionalRedisUtil.set(TRANS_KYC_KEY, workFlowTransBean, 900);

            return new GenericCoreResponseBean<>("KYC required for this transaction",
                    ResponseConstants.KYC_VALIDATION_REQUIRED);

        }

        // Fetch bank form using Query_PayResult API in case of CC/DC
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;

        if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.NB.value.equals(flowRequestBean.getPaymentTypeId())) {

            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {

                LOGGER.error("Query pay result failed due to : ", queryPayResultResponse.getFailureMessage());
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant(),
                        queryPayResultResponse.getRetryStatus());
                if (queryPayResultResponse.getResponse() != null) {
                    responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
                }
                return responseBean;
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case of payment fail
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                if (flowRequestBean.isTerminalStateForInternalRetry(queryPayResultResponse.getRetryStatus()))
                    closeFundOrder(workFlowTransBean);
            }

        }

        // Setting data in response
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setExtendedInfo(workFlowTransBean.getExtendInfo());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        return new GenericCoreResponseBean<>(workFlowResponseBean, queryPayResultResponse.getRetryStatus());
    }

    private void closeFundOrderDueToRiskReject(WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = workFlowHelper
                .closeFundOrderWithReason(workFlowTransBean, "RISK_REJECT");
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close or cancel order failed due to : {} ", cancelFundOrder.getFailureMessage());
        }

    }

    private String getRejectMsgIfUserRisky(GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney,
            WorkFlowTransactionBean workFlowTransactionBean) {
        if (workFlowTransactionBean == null || workFlowTransactionBean.getWorkFlowBean() == null
                || StringUtils.isBlank(workFlowTransactionBean.getWorkFlowBean().getPayMethod())) {
            return null;
        }
        WorkFlowRequestBean flowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse = workFlowHelper
                .consultAddMoneyV2(workFlowTransactionBean);
        if (!walletConsultResponse.isSuccessfullyProcessed()) {
            return null;
        }
        List<Map<String, Object>> feeDetails = Optional.ofNullable(walletConsultResponse.getResponse())
                .map(BizWalletConsultResponse::getFeeDetails).orElse(null);
        if (CollectionUtils.isNotEmpty(feeDetails)) {
            List<Map<String, Object>> feeDetailFromWallet = feeDetails.stream()
                    .filter(feeDetail -> flowRequestBean.getPayMethod().equals(feeDetail.get("payMethod")))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(feeDetailFromWallet)) {
                return (String) feeDetailFromWallet.get(0).get("rejectMsg");
            }
        }

        return null;
    }

    private void setCardHashAndCardIndexNo(WorkFlowTransactionBean workFlowTransBean, CacheCardResponseBean response,
            boolean isTxnByCardIndexNo) {

        if (workFlowTransBean == null || workFlowTransBean.getWorkFlowBean() == null || response == null) {
            return;
        }
        workFlowTransBean.getWorkFlowBean().setCardIndexNo(response.getCardIndexNo());
        if (isTxnByCardIndexNo) {
            return;
        }
        try {
            String cardHash = SignatureUtilWrapper.signApiRequest(workFlowTransBean.getWorkFlowBean().getCardNo());
            workFlowTransBean.getWorkFlowBean().setCardHash(cardHash);
        } catch (Exception e) {
            LOGGER.error("Exception in creating cardHash {} ", e);
        }
    }

    private boolean isUserAllowed(String walletRbiType) {
        if (StringUtils.isNotBlank(walletRbiType)) {
            switch (walletRbiType) {
            case BizConstant.WalletRbiType.E_KYC:
            case BizConstant.WalletRbiType.FULL_KYC:
            case BizConstant.WalletRbiType.MIN_KYC:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    private void closeFundOrder(final WorkFlowTransactionBean workFlowTransBean) {

        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = workFlowHelper
                .closeFundOrder(workFlowTransBean);
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close or cancel order failed due to : ", cancelFundOrder.getFailureMessage());
        }
    }

    private Map<String, String> getExtendInfoFromExtendBean(WorkFlowRequestBean flowRequestBean) {

        Map<String, String> extendInfo = new HashMap<>();
        ExtendedInfoRequestBean extendedInfoRequestBean = flowRequestBean.getExtendInfo();

        extendInfo.put(BizConstant.ExtendedInfoKeys.TOTAL_TXN_AMOUNT, flowRequestBean.getTxnAmount());
        extendInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_NAME, extendedInfoRequestBean.getMerchantName());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ALIPAY_MERCHANT_ID, extendedInfoRequestBean.getAlipayMerchantId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_NAME, flowRequestBean.getBankName());
        extendInfo.put(BizConstant.ExtendedInfoKeys.MCC_CODE, extendedInfoRequestBean.getMccCode());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_ID, flowRequestBean.getInstId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.PAYTM_MERCHANT_ID, extendedInfoRequestBean.getPaytmMerchantId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.PRODUCT_CODE, extendedInfoRequestBean.getProductCode());
        extendInfo.put(BizConstant.ExtendedInfoKeys.EMI_PLAN_ID, extendedInfoRequestBean.getEmiPlanId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.TOPUP_AND_PAY, "false");
        extendInfo.put(BizConstant.ExtendedInfoKeys.TXNTYPE, "ONLY_PG");
        extendInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_TRANS_ID, extendedInfoRequestBean.getMerchantTransId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.BIN_NUMBER, extendedInfoRequestBean.getBinNumber());

        return extendInfo;
    }

    private boolean checkForCardIndexNo(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean == null || StringUtils.isEmpty(flowRequestBean.getSavedCardID()))
            return false;

        String savedardId = flowRequestBean.getSavedCardID();
        if (savedardId.length() > 15) {
            LOGGER.info("Txn is via Card Index No");
            flowRequestBean.setTxnFromCardIndexNo(true);
            return true;
        }
        return false;

    }

    private void setDataFromBizProdMerchantProd(SavedAssetInfo savedAssetInfo, WorkFlowRequestBean flowRequestBean) {
        flowRequestBean.setCardIndexNo(flowRequestBean.getSavedCardID());
        flowRequestBean.setExpiryYear(Short.parseShort(savedAssetInfo.getExpiryYear()));
        flowRequestBean.setExpiryMonth(Short.parseShort(savedAssetInfo.getExpiryMonth()));
        flowRequestBean.setInstId(savedAssetInfo.getInstId());
        flowRequestBean.setBankName(savedAssetInfo.getInstOfficialName());
        flowRequestBean.setCardType(EPayMethod.getPayMethodByOldName(savedAssetInfo.getAssetType()).getMethod());
        flowRequestBean.setCardNo(StringUtils.substring(savedAssetInfo.getMaskedCardNo(), 0, 6));
        flowRequestBean.setCardScheme(savedAssetInfo.getCardScheme());
        if (!(flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
            flowRequestBean.setPayMethod(flowRequestBean.getCardType());
            flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_" + flowRequestBean.getCardScheme());
        }

    }
}
