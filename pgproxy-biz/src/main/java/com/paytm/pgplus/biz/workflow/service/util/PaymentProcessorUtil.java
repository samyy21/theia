package com.paytm.pgplus.biz.workflow.service.util;

import com.paytm.pgplus.biz.core.model.oauth.AuthUserInfoResponse;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.notification.service.IPayRequestNotifierService;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.promo.PromoCheckoutFeatureFF4jData;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskUtil;
import com.paytm.pgplus.biz.core.risk.RiskVerifierCacheProcessingPayload;
import com.paytm.pgplus.biz.core.user.service.IAuthService;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.OrderType;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.exception.*;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder.CheckoutPaymentPromoReqBuilderFactory;
import com.paytm.pgplus.biz.workflow.edc.Service.IEdcLinkService;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.biz.workflow.valservice.service.IValidationService;
import com.paytm.pgplus.biz.workflow.walletconsult.WalletConsultService;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.util.CryptoUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.MerchantLimitBreachedResponse;
import com.paytm.pgplus.common.model.link.EdcEmiChannelDetail;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequest;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.*;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.OrderInfo;
import com.paytm.pgplus.facade.affordabilityPlatform.models.response.APOrderCheckoutResponse;
import com.paytm.pgplus.facade.affordabilityPlatform.service.IAffordabilityService;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;
import com.paytm.pgplus.facade.emisubvention.enums.EmiType;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.AmountBearer;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.ItemBreakUp;
import com.paytm.pgplus.facade.emisubvention.models.response.CheckOutResponse;
import com.paytm.pgplus.facade.enums.EnumCurrency;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.merchantlimit.constants.Constants;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentPromoServiceNotifyMsg;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentPromoServiceNotifyMsgV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.CheckoutPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.CheckoutPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.*;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoServiceNotify;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoServiceNotifyV2;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoSevice;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.models.OrderPricingInfo;
import com.paytm.pgplus.models.PaymentOfferDetails;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;

@Service("paymentProcessorUtil")
public class PaymentProcessorUtil {

    private static final String PASSCODE_VALIDATION_ERRMSG_CHECK1 = "attempts";
    private static final String PASSCODE_VALIDATION_ERRMSG_CHECK2 = "disabled";
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PaymentProcessorUtil.class);

    @Autowired
    private MerchantVelocityUtil merchantVelocityUtil;

    @Autowired
    @Qualifier("paymentPromoService")
    private IPaymentPromoSevice paymentPromoService;

    @Autowired
    @Qualifier("walletConsultServiceImpl")
    private WalletConsultService walletConsultService;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    private IAuthService authService;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    private ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private IAffordabilityService affordabilityService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("paymentPromoServiceNotifyV2")
    private IPaymentPromoServiceNotifyV2 paymentPromoServiceNotifyV2;

    @Autowired
    @Qualifier("paymentPromoServiceNotify")
    private IPaymentPromoServiceNotify paymentPromoServiceNotify;

    @Autowired
    private WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    @Autowired
    @Qualifier("payRequestNotifierService")
    private IPayRequestNotifierService payRequestNotifierService;

    @Autowired
    private RiskUtil riskUtil;

    @Autowired
    @Qualifier("edcLinkServiceImpl")
    IEdcLinkService edcLinkService;

    @Autowired
    @Qualifier("validationServiceImpl")
    IValidationService validationService;

    @Autowired
    private Ff4jUtils ff4jUtil;

    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorUtil.class);

    public void setMerchantVeloctyLimitInExtendInfo(WorkFlowRequestBean workFlowBean) {
        if (workFlowBean == null || workFlowBean.getExtendInfo() == null) {
            return;
        }
        ExtendedInfoRequestBean extendedInfoRequestBean = workFlowBean.getExtendInfo();

        if (isNotExemptedPayMode(workFlowBean)) {
            LOGGER.info("updating merchant velocity for enahanced for mid = {} , txnAmount = {} ",
                    workFlowBean.getPaytmMID(), workFlowBean.getTxnAmount());
            try {
                validateAndUpdateMerchantVelocity(workFlowBean.getPaytmMID(), workFlowBean.getTxnAmount(),
                        workFlowBean.getExtendInfo(), true);
            } catch (BizMerchantVelocityBreachedException e) {

                EPayMode ePayMode = workFlowBean.getPaytmExpressAddOrHybrid();
                if (ePayMode != null && (ePayMode == EPayMode.ADDANDPAY || ePayMode == EPayMode.ADDANDPAY_KYC)) {
                    LOGGER.info("setting isAddnPay in");
                    e.setAddnPayTransaction(true);
                }
                throw e;
            }

        }
        // using these two new variable to maintain backward compatiablity.
        final String isMerchantLimitEnabled = Boolean.valueOf(extendedInfoRequestBean.isMerchantLimitEnabled())
                .toString();
        final String isMerchantLimitUpdated = Boolean.valueOf(extendedInfoRequestBean.isMerchantLimitUpdated())
                .toString();
        LOGGER.info("isMerchantLimitEnabled = {} , isMerchantLimitUpdated= {} ", isMerchantLimitEnabled,
                isMerchantLimitUpdated);
        extendedInfoRequestBean.setIsMerchantLimitEnabledForPay(isMerchantLimitEnabled);
        extendedInfoRequestBean.setIsMerchantLimitUpdatedForPay(isMerchantLimitUpdated);
    }

    public boolean isNotExemptedPayMode(WorkFlowRequestBean workFlowRequestBean) {
        try {
            EPayMode ePayMode = workFlowRequestBean.getPaytmExpressAddOrHybrid();
            if (ePayMode != null) {
                EXT_LOGGER.customInfo("isExemptedPayMode  ePayMode = {}", ePayMode);
                switch (ePayMode) {
                case HYBRID:
                case ADDANDPAY:
                case ADDANDPAY_KYC:
                    return true;
                }
            }

            EXT_LOGGER.customInfo("enhanced native isExemptedPayMode  payMode = {},bankCoe= {}",
                    workFlowRequestBean.getPaymentTypeId(), workFlowRequestBean.getBankCode());

            return !(PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId()) || (PaymentTypeIdEnum.NB.value
                    .equals(workFlowRequestBean.getPaymentTypeId()) && EPayMethod.PPBL.getOldName().equals(
                    workFlowRequestBean.getBankCode())));
        } catch (Exception e) {
            LOGGER.warn("some thing wentt wrong whilce checking exempted pay mode or nor");
        }
        return true;
    }

    public void validateAndUpdateMerchantVelocity(final String mid, final String txnAmt,
            final ExtendedInfoRequestBean extendedInfoRequestBean, boolean isTxnAmtInPaise) {
        if (bypassVelocity(mid)) {
            LOGGER.info("Bypassing velocity for mid: " + mid);
            return;
        }
        try {
            String txnAmtInRupees = (isTxnAmtInPaise) ? AmountUtils.getTransactionAmountInRupee(txnAmt) : txnAmt;
            MerchantLimitBreachedResponse merchantLimitBreachedResponse = merchantVelocityUtil
                    .checkMerchantLimitBreached(mid, txnAmtInRupees, extendedInfoRequestBean);
            if (merchantLimitBreachedResponse.isLimitBreached()) {
                throw new BizMerchantVelocityBreachedException("Merchant limit is breached for MID = " + mid,
                        merchantLimitBreachedResponse.getLimitType(), merchantLimitBreachedResponse.getLimitDuration());
            }
        } catch (BizMerchantVelocityBreachedException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception in validating and updating merchant velociy {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public boolean bypassVelocity(String mid) {
        return AllowedMidCustidPropertyUtil.isMidCustIdEligible(mid, null,
                BizConstant.MerchantVelocity.BYPASS_VELOCITY_MIDS, BizConstant.MerchantVelocity.NONE, false);
    }

    public GenericCoreResponseBean<BizPayResponse> validatePayFeeOnAddnPayTxn(WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<Object> objectGenericCoreResponseBean = validateAddnPayFee(workFlowTransBean);
        if (objectGenericCoreResponseBean != null)
            return new GenericCoreResponseBean<>(objectGenericCoreResponseBean.getFailureMessage(),
                    objectGenericCoreResponseBean.getResponseConstant());
        else
            return null;
    }

    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> validateCopFeeOnAddnPayTxn(
            WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<Object> objectGenericCoreResponseBean = validateAddnPayFee(workFlowTransBean);
        if (objectGenericCoreResponseBean != null)
            return new GenericCoreResponseBean<>(objectGenericCoreResponseBean.getFailureMessage(),
                    objectGenericCoreResponseBean.getResponseConstant());
        else
            return null;
    }

    @Nullable
    public GenericCoreResponseBean<Object> validateAddnPayFee(WorkFlowTransactionBean workFlowTransBean) {
        if (isFeeApplicableOnAddAndPayTxn(workFlowTransBean)) {
            GenericCoreResponseBean<?> walletConsultResponse = getForFeeOnAddNPayTransaction(workFlowTransBean);
            if (!walletConsultResponse.isSuccessfullyProcessed()
                    && ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.equals(walletConsultResponse
                            .getResponseConstant())) {
                throw new MerchantLimitBreachedException(Constants.LIMIT_BREACHED, Constants.AMOUNT,
                        Constants.ADD_N_PAY_MONTHLY);
            } else if (!walletConsultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>("AddMoney Not Allowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }
        return null;
    }

    private boolean isFeeApplicableOnAddAndPayTxn(WorkFlowTransactionBean flowTransactionBean) {
        boolean ret = false;
        if ((EPayMode.ADDANDPAY.equals(flowTransactionBean.getWorkFlowBean().getPaytmExpressAddOrHybrid()))
                && (ff4JUtils.isFeatureEnabledOnMid(flowTransactionBean.getWorkFlowBean().getPaytmMID(),
                        BizConstant.Ff4jFeature.ADD_MONEY_FEE_ON_ADDNPAY_TXN, false))) {
            ret = true;
        }
        return ret;
    }

    private GenericCoreResponseBean<?> getForFeeOnAddNPayTransaction(WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<BizWalletConsultResponse> response = walletConsultService
                .doWalletConsult(workFlowTransBean);
        if (!response.isSuccessfullyProcessed()) {
            return response;
        } else {
            GenericCoreResponseBean<WorkFlowTransactionBean> workFlowTransactionBeanGenericCoreResponseBean = walletConsultService
                    .applyFeeIfApplicableForAddNPayTransaction(response.getResponse(), workFlowTransBean);
            return workFlowTransactionBeanGenericCoreResponseBean;
        }
    }

    public void modifyOrder(WorkFlowTransactionBean workFlowTransactionBean) {
        if (workFlowTransactionBean.isModifyOrderRequired()) {
            WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
            OrderModifyRequest orderModifyRequest = workRequestCreator
                    .getOrderModifyRequestToUpdateDetailExtendInfo(workFlowRequestBean);
            try {
                orderService.modifyOrder(orderModifyRequest);
                theiaSessionRedisUtil.hsetIfExist(workFlowRequestBean.getTxnToken(), "orderModified", true);
            } catch (FacadeCheckedException exception) {
                LOGGER.error("Exception Occurred while modifying order : {}", exception);
                if (workFlowTransactionBean.isFailTxnIfModifyOrderFails()) {
                    throw new BizPaymentOfferCheckoutException();
                }
            }
        }
    }

    public boolean isSimplifiedFlow(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.isProcessSimplifiedEmi()
                || Objects.nonNull(workFlowRequestBean.getSimplifiedPaymentOffers());
    }

    public Object buildPaymentRequest(WorkFlowTransactionBean workFlowTransactionBean) throws PaytmValidationException,
            MappingServiceClientException, BaseException {
        if (workFlowTransactionBean.getWorkFlowBean().isFromAoaMerchant())
            return workRequestCreator.createBizAoaPayRequest(workFlowTransactionBean);
        else
            return workRequestCreator.createBizPayRequest(workFlowTransactionBean);
    }

    public void rollbackMerchantVelocityLimitUpdate(final String mid, final String txnAmt,
            final ExtendedInfoRequestBean extendedInfoRequestBean, boolean isTxnAmtInPaise) {
        try {
            EXT_LOGGER.customInfo(
                    "rolling back merchant velocity limit for mid={} ,txnAmount={} , isTxnAmtInPaise = {}", mid,
                    txnAmt, isTxnAmtInPaise);
            String txnAmtInRupees = (isTxnAmtInPaise) ? AmountUtils.getTransactionAmountInRupee(txnAmt) : txnAmt;
            boolean isRolledBack = merchantVelocityUtil.rollbackMerchantVelocityLimitUpdate(mid, txnAmtInRupees,
                    extendedInfoRequestBean);
            LOGGER.info("rollback status " + isRolledBack);
            if (!isRolledBack && extendedInfoRequestBean.isMerchantLimitUpdated()) {
                LOGGER.error("Not able to rollback merchant velocity limit update");
            }
        } catch (Exception e) {
            LOGGER.error("Exception in rolling back merchant velocity limit update {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public String getErrorMsgToReturnPaytmValidationExp(PaytmValidationException pve) {
        StringBuilder errorMsg = new StringBuilder();
        // To show maximum attempts message.
        if (StringUtils.isNotBlank(pve.getMessage())
                && (pve.getMessage().contains(PASSCODE_VALIDATION_ERRMSG_CHECK1) || pve.getMessage().contains(
                        PASSCODE_VALIDATION_ERRMSG_CHECK2))) {
            errorMsg.append(pve.getMessage());
        } else if (pve.getType() != null && pve.getType().getValidationFailedMsg() != null) {
            errorMsg.append(pve.getType().getValidationFailedMsg());
        } else if (null != pve.getMessage()) {
            errorMsg.append(pve.getMessage());
        } else {
            errorMsg.append(ResponseConstants.INVALID_PAYMENT_DETAILS.getMessage());
        }
        return errorMsg.toString();
    }

    public void enrichPayOrCopRequestBeanForInternationalCardPayment(Object request,
            WorkFlowTransactionBean workFlowTransactionBean) {
        if (request instanceof BizPayRequest) {
            setInternationalCardPaymentFlag(((BizPayRequest) request).getPayOptionBills(), workFlowTransactionBean,
                    ((BizPayRequest) request).getRiskExtendInfo());
        } else if (request instanceof CreateOrderAndPayRequestBean) {
            setInternationalCardPaymentFlag(((CreateOrderAndPayRequestBean) request).getPaymentInfo()
                    .getPayOptionBills(), workFlowTransactionBean,
                    ((CreateOrderAndPayRequestBean) request).getRiskExtendInfo());
        }
    }

    public void enrichPayRequestBeanForDirectChannelTxn(WorkFlowTransactionBean workFlowTransactionBean,
            BizPayRequest bizPayRequest) {
        if (checkForceDirectChannel(workFlowTransactionBean.getWorkFlowBean().getForceDirectChannel())) {
            setForceDirectChannel(bizPayRequest, workFlowTransactionBean);
        } else {
            // if DirectBank redirect request
            if (workFlowTransactionBean.getWorkFlowBean().isDirectBankCardFlow()) {
                disableDirectChannelFlag(bizPayRequest);
            }
            // Setting Flag if DirectBank Request
            setDirectChannelRequest(bizPayRequest, workFlowTransactionBean);
        }
    }

    private void setInternationalCardPaymentFlag(List<BizPayOptionBill> payOptionBillList,
            WorkFlowTransactionBean transactionBean, Map<String, String> riskExtendedInfo) {
        if (payOptionBillList != null && StringUtils.isNotBlank(transactionBean.getWorkFlowBean().getCardNo())) {
            try {
                BinDetail binDetail = null;

                if (transactionBean.getWorkFlowBean().isTxnFromCardIndexNo()) {
                    binDetail = transactionBean.getWorkFlowBean().getBinDetail();
                } else {
                    binDetail = cardUtils.fetchBinDetails(transactionBean.getWorkFlowBean().getCardNo());
                }
                if (binDetail != null && !binDetail.getIsIndian()) {
                    for (BizPayOptionBill payOptionBill : payOptionBillList) {
                        if (EPayMethod.CREDIT_CARD.getMethod().equals(payOptionBill.getPayMethod().getMethod())
                                || EPayMethod.DEBIT_CARD.getMethod().equals(payOptionBill.getPayMethod().getMethod())) {
                            payOptionBill.getChannelInfo().put(BizConstant.INTERNATIONAL_PAYMENT_KEY,
                                    BizConstant.INTERNATIONAL_PAYMENT_VALUE);
                            payOptionBill.getExtendInfo().put(INTERNATIONAL_CARD_PAYMENT, Boolean.TRUE.toString());
                            this.workFlowHelper.buildPayOptionBillsForDcc(payOptionBill, transactionBean);
                            // Setting riskExtendedInfo for International
                            // Payments
                            riskExtendedInfo.put(BizConstant.INTERNATIONAL_PAYMENT_KEY,
                                    BizConstant.INTERNATIONAL_PAYMENT_VALUE);
                            EXT_LOGGER.customInfo("international card request received with bin{}, orderId{}, mid{}",
                                    binDetail.getBin(), transactionBean.getWorkFlowBean().getOrderID(), transactionBean
                                            .getWorkFlowBean().getPaytmMID());
                            pushInternationalCardPaymentEvent(transactionBean, binDetail);
                        }
                    }
                } else {
                    // Setting riskExtendedInfo for National Payments
                    riskExtendedInfo.put(BizConstant.INTERNATIONAL_PAYMENT_KEY, BizConstant.NATIONAL_PAYMENT_VALUE);
                }
            } catch (PaytmValidationException e) {
                LOGGER.info("Not able to fetch Bin Details for bin{}", transactionBean.getWorkFlowBean().getCardNo()
                        .substring(0, 6));
            }
        }
    }

    private void pushInternationalCardPaymentEvent(WorkFlowTransactionBean transactionBean, BinDetail binDetail) {
        Map keyMap = new HashMap();

        keyMap.put("international", "INTERNATIONAL_CARD_PAYMENT");
        keyMap.put("requestType", transactionBean.getWorkFlowBean().getRequestType());
        keyMap.put("txnAmount", transactionBean.getWorkFlowBean().getTxnAmount());
        keyMap.put("bin", binDetail.getBin());
        keyMap.put("cardType", binDetail.getCardType());
        keyMap.put("bank", binDetail.getBank());

        EventUtils.pushTheiaEvents(EventNameEnum.INTERNATIONAL_CARD_PAYMENT, keyMap);
    }

    private void disableDirectChannelFlag(BizPayRequest payRequest) {
        List<BizPayOptionBill> payOptionBillList = payRequest.getPayOptionBills();
        if (payOptionBillList != null) {
            for (BizPayOptionBill payOptionBill : payOptionBillList) {
                if ((payOptionBill.getChannelInfo() != null)
                        && (StringUtils.isNotBlank(payOptionBill.getChannelInfo().get(
                                TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)))
                        && payOptionBill.getChannelInfo().get(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)
                                .equalsIgnoreCase("true")) {
                    payOptionBill.getChannelInfo().put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");

                }
            }
        }
    }

    public void setDirectChannelRequest(BizPayRequest payRequest, WorkFlowTransactionBean flowTransBean) {
        if (flowTransBean.getWorkFlowBean().isDirectBankCardFlow()) {
            flowTransBean.getWorkFlowBean().setDirectBankCardFlow(false);
            return;
        }
        List<BizPayOptionBill> payOptionBillList = payRequest.getPayOptionBills();
        if (payOptionBillList != null) {
            for (BizPayOptionBill payOptionBill : payOptionBillList) {
                if ((payOptionBill.getChannelInfo() != null)
                        && (StringUtils.isNotBlank(payOptionBill.getChannelInfo().get(
                                TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)))
                        && payOptionBill.getChannelInfo().get(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)
                                .equalsIgnoreCase("true")) {
                    flowTransBean.getWorkFlowBean().setDirectBankCardFlow(true);
                    break;

                }
            }
        }
    }

    public void setDirectChannelRequest(CreateOrderAndPayRequestBean createOrderAndPayRequestBean,
            WorkFlowTransactionBean flowTransBean) {
        BizPaymentInfo paymentInfo = createOrderAndPayRequestBean.getPaymentInfo();
        if (paymentInfo == null) {
            return;
        }
        List<BizPayOptionBill> payOptionBillList = paymentInfo.getPayOptionBills();
        if (payOptionBillList != null) {
            for (BizPayOptionBill payOptionBill : payOptionBillList) {
                if ((payOptionBill.getChannelInfo() != null)
                        && (StringUtils.isNotBlank(payOptionBill.getChannelInfo().get(
                                TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)))
                        && payOptionBill.getChannelInfo().get(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)
                                .equalsIgnoreCase("true")) {
                    flowTransBean.getWorkFlowBean().setDirectBankCardFlow(true);
                    break;
                }
            }
        }
    }

    private void setForceDirectChannel(BizPayRequest payRequest, WorkFlowTransactionBean flowTransBean) {
        List<BizPayOptionBill> payOptionBillList = payRequest.getPayOptionBills();
        if (payOptionBillList != null) {
            for (BizPayOptionBill payOptionBill : payOptionBillList) {
                if ((payOptionBill.getChannelInfo() != null)
                        && (StringUtils.isNotBlank(payOptionBill.getChannelInfo().get(
                                TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT)))) {
                    payOptionBill.getChannelInfo().put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT,
                            "forceDirect");

                }
            }
        }
    }

    public boolean checkForceDirectChannel(String forceDirectChannel) {
        if ((StringUtils.isNotBlank(forceDirectChannel)) && StringUtils.equals("forceDirect", forceDirectChannel)) {
            return true;
        }

        return false;
    }

    public PromoCheckoutFeatureFF4jData checkoutPaymentOfferUpdateOrderAmountAndExtendInfoV2(
            WorkFlowTransactionBean workFlowTransactionBean, int retryCount, boolean modifyCreatedOrder) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        PromoCheckoutFeatureFF4jData featureFF4jData = new PromoCheckoutFeatureFF4jData();
        if (workFlowRequestBean.isPaymentResumed()) {
            retryCount = 1;
        }
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                || workFlowRequestBean.getPaymentOffersAppliedV2() != null) {
            boolean txnFailureOnPromoCheckoutFailure = false;
            if ((workFlowRequestBean.getSimplifiedPaymentOffers() == null)
                    || (workFlowRequestBean.getSimplifiedPaymentOffers() != null && workFlowRequestBean
                            .getSimplifiedPaymentOffers().isValidatePromo())) {
                txnFailureOnPromoCheckoutFailure = true;
            }
            boolean ff4jCheckoutRetry = ff4JUtil.isFeatureEnabled(BizConstant.DISABLE_PROMO_CHECKOUT_RETRY,
                    workFlowRequestBean.getPaytmMID());
            featureFF4jData.setFeaturePromoCheckoutRetryFlagEnabled(ff4jCheckoutRetry);
            if (ff4jCheckoutRetry
                    && theiaSessionRedisUtil.hget(workFlowRequestBean.getTxnToken(), PROMO_CHECKOUT_RETRY_COUNT) != null) {
                LOGGER.error("promo checkout limit breached");
                throw new BizPaymentOfferCheckoutException("Retry count breached");
            }
            LOGGER.info("Request with payment offer");
            com.paytm.pgplus.facade.enums.PayMethod payMethod = null;
            CheckoutPromoServiceResponseV2 response = null;
            try {
                payMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(workFlowRequestBean
                        .getPayMethod());
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                throw new BizPaymentOfferCheckoutException();
            }

            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = CheckoutPaymentPromoReqBuilderFactory
                    .getApplyPromoPaymentOptionBuilder(payMethod).buildV2(workFlowRequestBean);
            Map<String, String> queryParam = getPromoCheckoutQueryParams(workFlowRequestBean);
            if (workFlowRequestBean.getPaymentRequestBean() != null
                    && StringUtils.isNotBlank(workFlowRequestBean.getPaymentRequestBean().getMerchantDisplayName())) {
                checkoutPromoServiceRequest.setMerchantDisplayName(workFlowRequestBean.getPaymentRequestBean()
                        .getMerchantDisplayName());
            }
            response = checkoutPaymentOfferV2(checkoutPromoServiceRequest, queryParam, retryCount, workFlowRequestBean);
            if (!isPromoCheckoutSuccessResponseV2(response)) {
                LOGGER.error("Payment Offer checkout failure");
                if (txnFailureOnPromoCheckoutFailure) {
                    throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsgV2(response));
                } else {
                    return featureFF4jData;
                }
            }
            CheckoutPromoServiceResponse responseV1 = getPromoCheckoutResponseV1FromV2(response);
            setMobileNumberInPaymentPromoCheckoutData(workFlowRequestBean, responseV1);
            responseV1.getData().setTotalTransactionAmount(
                    String.valueOf(checkoutPromoServiceRequest.getPaymentDetails().getTotalTransactionAmount()));
            try {
                workFlowRequestBean.getExtendInfo().setPaymentPromoCheckoutData(
                        JsonMapper.mapObjectToJson(responseV1.getData()));
            } catch (FacadeCheckedException e) {
                LOGGER.error("ApplyPromoResponseData map to json Exception : " + ExceptionUtils.getStackTrace(e));
                // Rollback is important here for cases where we
                // are not able to set PaymentPromoCheckoutData in extendInfo
                rollbackPaymentOfferCheckout(workFlowRequestBean, true);
                if (txnFailureOnPromoCheckoutFailure) {
                    throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsgV2(response));
                } else {
                    return featureFF4jData;
                }
            }

            boolean isPromoDataInMerchantStatusServiceAllowed = ff4JUtils.isFeatureEnabledOnMid(
                    workFlowRequestBean.getPaytmMID(), ALLOW_PROMO_DATA_IN_MERCHANT_STATUS_SERVICE, false);

            if (workFlowRequestBean.getSimplifiedPaymentOffers() != null || isPromoDataInMerchantStatusServiceAllowed) {
                workFlowRequestBean.setPayableAmount(workFlowRequestBean.getTxnAmount());
            }

            List<PromoSaving> promoSavings = null;
            if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null) {
                promoSavings = getPromoSavings(workFlowRequestBean.getPaymentOfferCheckoutReqData().getOfferBreakup()
                        .get(0));
            } else {
                promoSavings = responseV1.getData().getSavings();
            }
            workFlowRequestBean.setTxnAmount(calculateTxnAmountAfterOffer(promoSavings,
                    workFlowRequestBean.getTxnAmount(), workFlowRequestBean.getPromoAmount()));
            if (modifyCreatedOrder && workFlowRequestBean.getSimplifiedPaymentOffers() != null
                    && (theiaSessionRedisUtil.hget(workFlowRequestBean.getTxnToken(), "orderModified") == null)
                    && !workFlowRequestBean.getPayableAmount().equals(workFlowRequestBean.getTxnAmount())) {
                workFlowTransactionBean.setModifyOrderRequired(true);
                workFlowTransactionBean.setFailTxnIfModifyOrderFails(txnFailureOnPromoCheckoutFailure);
            }
            featureFF4jData.setSuccess(true);
            LOGGER.info("successfully checkoutPaymentOfferUpdateOrderAmountAndExtendInfo");
            return featureFF4jData;
        }
        return featureFF4jData;
    }

    private Map<String, String> getPromoCheckoutQueryParams(WorkFlowRequestBean workFlowRequestBean) {
        Map<String, String> queryParam = new HashMap<String, String>();
        queryParam.put("customer-id", custIdFromWorkflowReqBean(workFlowRequestBean));
        queryParam.put("merchant-id", workFlowRequestBean.getPaytmMID());
        queryParam.put("order-id", workFlowRequestBean.getOrderID());
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                && StringUtils.isNotBlank(workFlowRequestBean.getPaymentOfferCheckoutReqData().getEncUserId())) {
            try {
                queryParam.put("paytm-user-id",
                        CryptoUtils.decryptAES(workFlowRequestBean.getPaymentOfferCheckoutReqData().getEncUserId()));
            } catch (Exception e) {
                LOGGER.error("Error while decrypting user id corresponding to vpa :{}", e.getMessage());
            }
        } else if (workFlowRequestBean.getUserDetailsBiz() != null
                && workFlowRequestBean.getSimplifiedPaymentOffers() == null
                && ff4JUtil.isFeatureEnabled(TheiaConstant.ExtraConstants.THEIA_ENABLE_PROMO_WALLET_CASHBACK,
                        workFlowRequestBean.getPaytmMID())) {
            queryParam.put("paytm-user-id", workFlowRequestBean.getUserDetailsBiz().getUserId());
        }
        return queryParam;
    }

    public CheckoutPromoServiceResponseV2 checkoutPaymentOfferV2(
            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest, Map<String, String> queryParam, int retryCount,
            WorkFlowRequestBean workFlowRequestBean) {
        int retryAllowed = Integer.parseInt(ConfigurationUtil.getProperty("payment.offer.checkout.retries.count", "2"))
                + retryCount;
        while (retryCount < retryAllowed + 1) {
            try {
                queryParam.put("request-retry", String.valueOf(retryCount++));
                CheckoutPromoServiceResponseV2 response = paymentPromoService.checkoutPromoV2(
                        checkoutPromoServiceRequest, queryParam);
                // if (response != null && response.getData() != null) {
                // response.getData().setTotalTransactionAmount(
                // String.valueOf(checkoutPromoServiceRequest.getTotalTransactionAmount()));
                // }
                return response;
            } catch (FacadeCheckedException e) {
                LOGGER.error("checkoutPaymentOffer failure for retry {} with StackTrace : {}", retryCount,
                        ExceptionUtils.getStackTrace(e));
            }
        }
        rollbackPaymentOfferCheckoutV2(workFlowRequestBean, true);
        return null;
    }

    public void rollbackPaymentOfferCheckoutV2(WorkFlowRequestBean workFlowRequestBean, boolean rollBackPaymentOffer) {
        if ((workFlowRequestBean.getPaymentOfferCheckoutReqData() != null || workFlowRequestBean
                .getPaymentOffersAppliedV2() != null) && rollBackPaymentOffer) {
            LOGGER.info("Rolling back Payment offer checkout v2");
            PaymentPromoServiceNotifyMsgV2 paymentPromoServiceNotifyMsgV2 = preparePromoServiceNotifyMsgV2(
                    workFlowRequestBean, BizConstant.CLIENT_PG);
            paymentPromoServiceNotifyV2.pushFailureMsg(paymentPromoServiceNotifyMsgV2);
        } else if ((workFlowRequestBean.getPaymentOfferCheckoutReqData() != null || workFlowRequestBean
                .getPaymentOffersAppliedV2() != null) && !rollBackPaymentOffer) {
            LOGGER.info("not doing rollback for payment failure v2");
        }
    }

    private PaymentPromoServiceNotifyMsgV2 preparePromoServiceNotifyMsgV2(WorkFlowRequestBean workFlowRequestBean,
            String client) {
        PaymentPromoServiceNotifyMsgV2 msg = new PaymentPromoServiceNotifyMsgV2(workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), custIdFromWorkflowReqBean(workFlowRequestBean));
        msg.setClient(client);
        return msg;
    }

    private String custIdFromWorkflowReqBean(WorkFlowRequestBean workFlowRequestBean) {
        return StringUtils.isNotBlank(workFlowRequestBean.getCustID()) ? workFlowRequestBean.getCustID()
                : (workFlowRequestBean.getUserDetailsBiz() != null) ? workFlowRequestBean.getUserDetailsBiz()
                        .getUserId() : null;
    }

    private String calculateTxnAmountAfterOffer(List<PromoSaving> promoSavings, String txnAmount, String promoAmount) {
        for (PromoSaving promoSaving : promoSavings) {
            if (REDEMPTION_TYPE_DISCOUNT.equalsIgnoreCase(promoSaving.getRedemptionType())) {

                EXT_LOGGER.customInfo("processing promoType {}", REDEMPTION_TYPE_DISCOUNT);
                /*
                 * promoAmount is the full amount to be paid, this does not have
                 * the discounted amount removed from it, txnAmount amount is
                 * the amount which has discounted amount applied to it
                 */
                long originalTxnAmount = 0;
                if (StringUtils.isNotBlank(promoAmount)) {
                    originalTxnAmount = Long.parseLong(promoAmount);
                } else {
                    originalTxnAmount = Long.parseLong(txnAmount);
                }

                if (promoSaving.getSavings() > originalTxnAmount) {
                    LOGGER.error("Saving can't exceed originalTxnAmount : originalTxnAmount = {}, savings = {}",
                            originalTxnAmount, promoSaving.getSavings());
                    throw new BizPaymentOfferCheckoutException("Saving/Discount can't exceed txnAmount");
                }

                long amountToBePaid = 0;

                if (StringUtils.isNotBlank(promoAmount)) {
                    // LOGGER.info("using discountedAmount got from merchant");
                    amountToBePaid = Long.parseLong(txnAmount);
                } else {
                    LOGGER.info("applying promo savings minus to originalTxnAmount");
                    amountToBePaid = originalTxnAmount - promoSaving.getSavings();
                }

                long finalAmount = amountToBePaid;
                LOGGER.info("PaymentOffer of discount type : originalTxnAmount = {}, payableAmount = {}",
                        originalTxnAmount, finalAmount);
                return String.valueOf(finalAmount);
            } else if (BizConstant.REDEMPTION_TYPE_CASHBACK.equalsIgnoreCase(promoSaving.getRedemptionType())
                    || BizConstant.REDEMPTION_TYPE_PAYTM_CASHBACK.equalsIgnoreCase(promoSaving.getRedemptionType())) {
                LOGGER.info("processing promoType {}", BizConstant.REDEMPTION_TYPE_CASHBACK);
                if (StringUtils.isNotBlank(promoAmount)) {
                    LOGGER.info("using discountedAmount got from merchant");
                    return String.valueOf(Long.parseLong(txnAmount));
                }
            }
        }
        return txnAmount;
    }

    private List<PromoSaving> getPromoSavings(PaymentOfferDetails paymentOfferDetails) {
        List<PromoSaving> promoSavings = new ArrayList<>();
        if (StringUtils.isNotBlank(paymentOfferDetails.getCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(BizConstant.REDEMPTION_TYPE_CASHBACK);
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getCashbackAmount())));
            promoSavings.add(promoSaving);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getInstantDiscount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(REDEMPTION_TYPE_DISCOUNT);
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getInstantDiscount())));
            promoSavings.add(promoSaving);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getPaytmCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(BizConstant.REDEMPTION_TYPE_PAYTM_CASHBACK);
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getPaytmCashbackAmount())));
            promoSavings.add(promoSaving);
        }
        return promoSavings;
    }

    private boolean isPromoCheckoutSuccessResponseV2(CheckoutPromoServiceResponseV2 response) {
        if (response != null
                && isPromoServiceSeccessResponse(response)
                && response.getData() != null
                && isPromoCheckoutSuccess(response.getData())
                && (MapUtils.isNotEmpty(response.getData().getPromoResponse()) || CollectionUtils.isNotEmpty(response
                        .getData().getSavings()))) {
            return true;
        }
        return false;
    }

    private boolean isPromoCheckoutSuccess(ApplyPromoResponseData data) {
        return data.getStatus() == 1;
    }

    private boolean isPromoCheckoutSuccess(ApplyPromoResponseDataV2 data) {
        return data.getStatus() == 1;
    }

    public <T extends PromoServiceResponseBase> boolean isPromoServiceSeccessResponse(T response) {
        return response.getStatus() == 1;
    }

    private void rollbackPaymentOfferCheckout(WorkFlowRequestBean workFlowRequestBean, boolean rollBackPaymentOffer) {
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null && rollBackPaymentOffer) {
            LOGGER.info("Rolling back Payment offer checkout");
            PaymentPromoServiceNotifyMsg msg = new PaymentPromoServiceNotifyMsg(workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getOrderID(), custIdFromWorkflowReqBean(workFlowRequestBean));
            if (workFlowRequestBean.getExtendInfo().getPaymentPromoCheckoutData() != null) {
                ApplyPromoResponseData promoCheckoutData = null;
                try {
                    promoCheckoutData = JsonMapper.mapJsonToObject(workFlowRequestBean.getExtendInfo()
                            .getPaymentPromoCheckoutData(), ApplyPromoResponseData.class);
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Json Parsing Exception in ApplyPromoResponseData : "
                            + ExceptionUtils.getStackTrace(e));
                }
                if (promoCheckoutData != null) {
                    // LOGGER.info("setting message in rollback packet");
                    msg.setPromocode(promoCheckoutData.getPromocode());
                }
            }
            paymentPromoServiceNotify.pushFailureMsg(msg);
        } else if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null && !rollBackPaymentOffer) {
            LOGGER.info("not doing rollback for payment failure");
        }
    }

    private String getPromoCheckoutErrorMsgV2(CheckoutPromoServiceResponseV2 response) {
        if (response != null && response.getData() != null && StringUtils.isNotBlank(response.getData().getPromotext())) {
            return response.getData().getPromotext();
        }
        return ConfigurationUtil.getProperty("payment.offer.checkout.failure.msg",
                "There seems to be a problem in processing. Please try again");
    }

    private CheckoutPromoServiceResponse getPromoCheckoutResponseV1FromV2(CheckoutPromoServiceResponseV2 response) {
        CheckoutPromoServiceResponse responseV1 = new CheckoutPromoServiceResponse();
        ApplyPromoResponseData data = new ApplyPromoResponseData();
        data.setStatus(1);
        data.setPromotext(response.getData().getPromotext());
        data.setPromoVisibility(response.getData().isPromoVisibility());
        data.setResponseCode(response.getData().getResponseCode());
        List<PromoSaving> savings = new ArrayList<PromoSaving>();
        String promoCode = null;
        if (response.getData().getPromoResponse() != null) {
            for (Map.Entry<String, PromoResponseData> entry : response.getData().getPromoResponse().entrySet()) {
                if (entry.getValue() != null && MapUtils.isNotEmpty(entry.getValue().getItems())
                        && entry.getValue().getItems().get("item001") != null) {
                    promoCode = entry.getKey();
                    Items items = entry.getValue().getItems().get("item001");
                    if (CollectionUtils.isNotEmpty(items.getUsage_data())) {
                        PromoUsageData promoUsageData = items.getUsage_data().get(0);
                        PromoSaving ps = new PromoSaving();
                        ps.setRedemptionType(promoUsageData.getRedemptionType());
                        ps.setSavings(promoUsageData.getAmount());
                        savings.add(ps);
                    }
                }
            }
        } else {
            savings.addAll(response.getData().getSavings());
        }
        data.setSavings(savings);
        data.setPromocode(promoCode);
        responseV1.setData(data);
        return responseV1;
    }

    private void setMobileNumberInPaymentPromoCheckoutData(WorkFlowRequestBean workFlowRequestBean,
            CheckoutPromoServiceResponse responseV1) {
        List<PromoSaving> promoSavings = responseV1.getData().getSavings();
        if (BizConstant.REDEMPTION_TYPE_PAYTM_CASHBACK.equalsIgnoreCase(promoSavings.get(0).getRedemptionType())) {
            String userMobileNumber = null;
            try {
                if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                        && StringUtils.isNotBlank(workFlowRequestBean.getPaymentOfferCheckoutReqData().getEncUserId())) {
                    String decryptUserId = CryptoUtils.decryptAES(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                            .getEncUserId());
                    userMobileNumber = (workFlowRequestBean.getUserDetailsBiz() != null && decryptUserId
                            .equals(workFlowRequestBean.getUserDetailsBiz().getUserId())) ? workFlowRequestBean
                            .getUserDetailsBiz().getMobileNo() : getMobileNumberFromOauth(decryptUserId,
                            workFlowRequestBean);
                } else if (workFlowRequestBean.getUserDetailsBiz() != null) {
                    userMobileNumber = workFlowRequestBean.getUserDetailsBiz().getMobileNo();
                }
                if (StringUtils.isNotBlank(userMobileNumber)) {
                    responseV1.getData().setMobileNumber(MaskingUtil.getMaskedString(userMobileNumber, 3, 3));
                }
                // Will remove after testing
                LOGGER.info("checkout data is: {}", responseV1);
            } catch (Exception e) {
                LOGGER.error("Error while decrypting user id corresponding to vpa :{}", e.getMessage());
            }
        }
    }

    public String getMobileNumberFromOauth(String userId, WorkFlowRequestBean workFlowRequestBean) {
        GenericCoreResponseBean<AuthUserInfoResponse> authUserInfoResponse = authService.fetchUserInfoByUserId(userId,
                workFlowRequestBean);
        if (!authUserInfoResponse.isSuccessfullyProcessed())
            return null;
        else {
            String phone = authUserInfoResponse.getResponse().getBasicInfo() != null ? authUserInfoResponse
                    .getResponse().getBasicInfo().getPhone() : null;
            return phone;
        }
    }

    public void populateChargesForSimplifiedFlow(WorkFlowTransactionBean workFlowTransactionBean) {
        workFlowTransactionBean.setConsultFeeResponse(workFlowHelper.consultBulkFeeResponseForPay(
                workFlowTransactionBean, null).getResponse());
        workFlowTransactionBean.getWorkFlowBean().setChargeAmount(
                workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(workFlowTransactionBean));

    }

    public void rollbackMerchantVelocityLimit(WorkFlowTransactionBean workFlowTransactionBean) {
        if (isNotExemptedPayMode(workFlowTransactionBean.getWorkFlowBean())) {
            rollbackMerchantVelocityLimitUpdate(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                    workFlowTransactionBean.getWorkFlowBean().getTxnAmount(), workFlowTransactionBean.getWorkFlowBean()
                            .getExtendInfo(), true);
        }
    }

    public void pushPayOrCopRequestToCheckoutKafka(Object requestBean, String mid) {
        boolean ff4jNotifyPaymentRequestData = ff4JUtil.isFeatureEnabled(
                BizConstant.Ff4jFeature.NOTIFY_PAYMENT_REQUEST_DATA, mid);
        boolean logStackTrace = ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.NOTIFY_PAYMENT_DWH_STACKTRACE, mid);
        if (ff4jNotifyPaymentRequestData) {
            if (requestBean instanceof BizPayRequest) {
                pushPayRequestToDwhKafkaTopic((BizPayRequest) requestBean, logStackTrace);
            } else if (requestBean instanceof CreateOrderAndPayRequestBean) {
                pushCopRequestToDwhKafkaTopic((CreateOrderAndPayRequestBean) requestBean, logStackTrace);
            }
        }
    }

    private void pushPayRequestToDwhKafkaTopic(BizPayRequest payRequest, boolean logStackTrace) {
        try {
            long startTime = System.currentTimeMillis();
            payRequestNotifierService.pushPayRequestToKafkaTopic(payRequest);
            long endTime = System.currentTimeMillis();
            LOGGER.info("Total time to push Pay Request on DWH Kafka Topic {}", (endTime - startTime));
        } catch (Exception e) {
            if (logStackTrace)
                LOGGER.error("Error While sending pay Request of DWH Kafka Topic", e);
            else {
                EventUtils.pushTheiaEvents(EventNameEnum.PAY_KAFKA_ERROR, new ImmutablePair<>(
                        "Error While sending pay Request of DWH Kafka Topic", e.getMessage()));
            }
        }
    }

    private void pushCopRequestToDwhKafkaTopic(CreateOrderAndPayRequestBean createOrderAndPayRequestBean,
            boolean logStackTrace) {
        try {
            long startTime = System.currentTimeMillis();
            payRequestNotifierService.pushCopRequestToKafkaTopic(createOrderAndPayRequestBean);
            long endTime = System.currentTimeMillis();
            EXT_LOGGER.customInfo("Total time to push COP Request on DWH Kafka Topic {}", (endTime - startTime));
        } catch (Exception e) {
            if (logStackTrace)
                LOGGER.error("Error While sending Cop Request of DWH Kafka Topic", e);
            else {
                EventUtils.pushTheiaEvents(EventNameEnum.COP_KAFKA_ERROR, new ImmutablePair<>(
                        "Error While sending Cop Request of DWH Kafka Topic", e.getMessage()));
            }
        }
    }

    public GenericCoreResponseBean<BizPayResponse> handlePayRiskRejectOrRiskVerifyFailure(
            GenericCoreResponseBean<BizPayResponse> payResponse, WorkFlowTransactionBean workFlowTransactionBean) {

        if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
            GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                    payResponse.getResponseConstant(), payResponse.getRiskRejectUserMessage());
            responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
            return responseBean;
        } else if (payResponse.getResponseConstant() != null
                && ResponseConstants.RISK_VERIFICATION.equals(payResponse.getResponseConstant())) {
            if (ERequestType.NATIVE != workFlowTransactionBean.getWorkFlowBean().getRequestType()) {
                return convertRiskVerificationToRiskReject();
            }
            RiskVerifierCacheProcessingPayload riskDoViewResponse = setRiskDoViewResponseInCache(
                    workFlowTransactionBean, workFlowTransactionBean.getWorkFlowBean().getTransID(), payResponse
                            .getResponse().getSecurityPolicyResult());
            if (!riskDoViewResponse.isSuccessful()) {
                return convertRiskVerificationToRiskReject(riskDoViewResponse.getMessage());
            }
        }
        return new GenericCoreResponseBean<>(payResponse.getFailureMessage(), payResponse.getResponseConstant());
    }

    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> handleCopRiskRejectOrRiskVerifyFailure(
            WorkFlowTransactionBean workFlowTransactionBean,
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse) {

        GenericCoreResponseBean<CreateOrderAndPayResponseBean> responseBean = null;
        if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
            responseBean = new GenericCoreResponseBean<>(createOrderAndPayResponse.getFailureMessage(),
                    createOrderAndPayResponse.getResponseConstant(),
                    createOrderAndPayResponse.getRiskRejectUserMessage());
            responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
        } else {
            responseBean = new GenericCoreResponseBean<>(createOrderAndPayResponse.getFailureMessage(),
                    createOrderAndPayResponse.getResponseConstant());
        }
        if (ERequestType.NATIVE != workFlowTransactionBean.getWorkFlowBean().getRequestType()) {
            return convertRiskVerificationToRiskReject();
        }
        if (createOrderAndPayResponse.getResponseConstant() != null
                && ResponseConstants.NEED_RISK_CHALLENGE.equals(createOrderAndPayResponse.getResponseConstant())) {
            if (ERequestType.NATIVE != workFlowTransactionBean.getWorkFlowBean().getRequestType()) {
                return convertRiskVerificationToRiskReject();
            }
            RiskVerifierCacheProcessingPayload riskDoViewResponse = setRiskDoViewResponseInCache(
                    workFlowTransactionBean, createOrderAndPayResponse.getResponse().getAcquirementId(),
                    createOrderAndPayResponse.getResponse().getSecurityPolicyResult());
            if (riskDoViewResponse.isSuccessful()) {
                responseBean.setAcquirementId(createOrderAndPayResponse.getResponse().getAcquirementId());
            } else {
                return convertRiskVerificationToRiskReject(riskDoViewResponse.getMessage());
            }
        }
        RiskVerifierCacheProcessingPayload riskDoViewResponse = setRiskDoViewResponseInCache(workFlowTransactionBean,
                createOrderAndPayResponse.getResponse().getAcquirementId(), createOrderAndPayResponse.getResponse()
                        .getSecurityPolicyResult());
        if (riskDoViewResponse.isSuccessful()) {
            createOrderAndPayResponse.setAcquirementId(createOrderAndPayResponse.getResponse().getAcquirementId());
        } else {
            return convertRiskVerificationToRiskReject(riskDoViewResponse.getMessage());
        }
        if (StringUtils.isNotBlank(createOrderAndPayResponse.getAcquirementId())) {
            responseBean.setAcquirementId(createOrderAndPayResponse.getAcquirementId());
        }
        return responseBean;
    }

    private GenericCoreResponseBean convertRiskVerificationToRiskReject() {
        return convertRiskVerificationToRiskReject(null);
    }

    private GenericCoreResponseBean convertRiskVerificationToRiskReject(String message) {

        if (StringUtils.isEmpty(message)) {
            message = RiskConstants.RISK_REJECT_MESSAGE;
        }
        return new GenericCoreResponseBean<>(ResponseConstants.RISK_REJECT.getMessage(), ResponseConstants.RISK_REJECT,
                message);
    }

    private RiskVerifierCacheProcessingPayload setRiskDoViewResponseInCache(
            WorkFlowTransactionBean workFlowTransactionBean, String transId, SecurityPolicyResult securityPolicyResult) {
        String mid = workFlowTransactionBean.getWorkFlowBean().getPaytmMID();
        String orderId = workFlowTransactionBean.getWorkFlowBean().getOrderID();
        String verifyId = securityPolicyResult.getSecurityId();
        String method = securityPolicyResult.getRiskResult().getVerificationMethod().getMethod();
        return riskUtil.setRiskDoViewResponseInCache(mid, orderId, transId, verifyId, method, workFlowTransactionBean
                .getWorkFlowBean().getRiskVerifyTxnToken());
    }

    public void setPromoRetryCount(WorkFlowTransactionBean workFlowTransactionBean,
            PromoCheckoutFeatureFF4jData checkoutFeatureFF4jData) {
        if (checkoutFeatureFF4jData != null && checkoutFeatureFF4jData.isSuccess()
                && checkoutFeatureFF4jData.isFeaturePromoCheckoutRetryFlagEnabled()) {
            LOGGER.info("setting promo checkout retry count 1");
            theiaSessionRedisUtil.hsetIfExist(workFlowTransactionBean.getWorkFlowBean().getTxnToken(),
                    PROMO_CHECKOUT_RETRY_COUNT, 1);
        }
    }

    public boolean isInternalPaymentRetry(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.getCurrentInternalPaymentRetryCount() > 0;
    }

    public void applyEmiSubvention(WorkFlowRequestBean workFlowRequestBean) {
        workFlowHelper.applyEmiSubventionOfferV2(workFlowRequestBean);
    }

    public CreateOrderAndPayRequestBean buildCopRequestBean(WorkFlowTransactionBean workFlowTransactionBean,
            boolean isRenewSubscriptionRequest) throws PaytmValidationException, BaseException,
            MappingServiceClientException {
        return workRequestCreator.createOrderAndPayRequestBean(workFlowTransactionBean, isRenewSubscriptionRequest);
    }

    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> handleCopPaymentValidationException(
            PaytmValidationException e) {
        StringBuilder errorMessage = new StringBuilder();
        // To show maximum attempts message.
        if (null != e.getMessage()
                && (e.getMessage().contains(PASSCODE_VALIDATION_ERRMSG_CHECK1) || e.getMessage().contains(
                        PASSCODE_VALIDATION_ERRMSG_CHECK2))) {
            errorMessage.append(e.getMessage());
        } else if (null != e.getType() && null != e.getType().getValidationFailedMsg()) {
            errorMessage.append(e.getType().getValidationFailedMsg());
        } else if (null != e.getMessage()) {
            errorMessage.append(e.getMessage());
        } else {
            errorMessage.append(ResponseConstants.INVALID_PAYMENT_DETAILS.getMessage());
        }
        LOGGER.error("PaytmValidationException occured while creating createOrderAndPay request ", e);
        return new GenericCoreResponseBean<>(errorMessage.toString(), ResponseConstants.INVALID_PAYMENT_DETAILS);
    }

    public boolean isBrandEmiOffer(WorkFlowTransactionBean workFlowTransactionBean) {
        if (Boolean.TRUE.equals(workFlowTransactionBean.getWorkFlowBean().getEdcLinkTxn())
                && workFlowTransactionBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null) {
            EdcEmiDetails edcEmiFields = workFlowTransactionBean.getWorkFlowBean().getPaymentRequestBean()
                    .getLinkDetailsData().getEdcEmiFields();
            if (edcEmiFields != null && StringUtils.isNotBlank(edcEmiFields.getProductId()))
                return true;
            else
                return false;
        }
        return false;
    }

    public void applyEdcLinkTxnOffers(WorkFlowTransactionBean workFlowTransactionBean) {
        String bankVerificationCode = null;
        String brandVerificationCode = null;
        List<String> velocityOfferId = null;
        if (!isInternalPaymentRetry(workFlowTransactionBean.getWorkFlowBean())) {
            final boolean validationSkipFlag = getValidationSkipFlag(workFlowTransactionBean.getWorkFlowBean());
            if (isBrandEmiOffer(workFlowTransactionBean)) {
                GenericCoreResponseBean<BrandEmiResponse> brandEmiResponse = edcLinkService
                        .getBrandEmiResponse(workFlowTransactionBean);
                if (!brandEmiResponse.isSuccessfullyProcessed()) {
                    throw new EdcLinkBankAndBrandEmiCheckoutException(brandEmiResponse.getFailureDescription(),
                            brandEmiResponse.getResponseConstant());
                }
                if (ff4JUtils.isFeatureEnabledOnMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                        THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                    if (brandEmiResponse.getResponse() != null && brandEmiResponse.getResponse().getBody() != null
                            && brandEmiResponse.getResponse().getBody().getEmiDetail() != null
                            && brandEmiResponse.getResponse().getBody().getEmiDetail().getEmiChannelDetails() != null) {
                        String pgPlanId = getPgPlanIdFromWorkFlowTxnBean(workFlowTransactionBean);
                        Optional<EmiChannelDetails> emiChannelDetail = brandEmiResponse.getResponse().getBody()
                                .getEmiDetail().getEmiChannelDetails().stream()
                                .filter(emiChannel -> pgPlanId.equals(emiChannel.getPgPlanId())).findAny();
                        if (emiChannelDetail.isPresent()) {
                            bankVerificationCode = emiChannelDetail.get().getBankVerificationCode();
                        }
                    }
                    GenericCoreResponseBean<ValidateVelocityResponse> validateVelocityResponse = edcLinkService
                            .validateVelocity(workFlowTransactionBean);
                    if (BooleanUtils.isFalse(validateVelocityResponse.isSuccessfullyProcessed())) {
                        throw new EdcLinkBankAndBrandEmiCheckoutException(
                                validateVelocityResponse.getFailureDescription(),
                                validateVelocityResponse.getResponseConstant());
                    }
                    if (Objects.nonNull(validateVelocityResponse.getResponse())) {
                        brandVerificationCode = validateVelocityResponse.getResponse().getBrandVerificationCode();
                        velocityOfferId = validateVelocityResponse.getResponse().getVelocityOfferId();
                    }
                }
            } else {
                GenericCoreResponseBean<BankEmiResponse> bankEmiResponse = edcLinkService
                        .getBankEmiResponse(workFlowTransactionBean);
                if (!bankEmiResponse.isSuccessfullyProcessed()) {
                    throw new EdcLinkBankAndBrandEmiCheckoutException(bankEmiResponse.getFailureDescription(),
                            bankEmiResponse.getResponseConstant());
                }
                if (bankEmiResponse.getResponse() != null
                        && bankEmiResponse.getResponse().getBody() != null
                        && ff4JUtils.isFeatureEnabledOnMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                                THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                    if (bankEmiResponse.getResponse() != null && bankEmiResponse.getResponse().getBody() != null
                            && bankEmiResponse.getResponse().getBody().getEmiDetail() != null
                            && bankEmiResponse.getResponse().getBody().getEmiDetail().getEmiChannelInfos() != null) {
                        String pgPlanId = getPgPlanIdFromWorkFlowTxnBean(workFlowTransactionBean);
                        Optional<EmiChannelInfos> emiChannelInfo = bankEmiResponse.getResponse().getBody()
                                .getEmiDetail().getEmiChannelInfos().stream()
                                .filter(emiChannel -> pgPlanId.equals(emiChannel.getPlanId())).findAny();
                        if (emiChannelInfo.isPresent()) {
                            bankVerificationCode = emiChannelInfo.get().getBankVerificationCode();
                        }
                    }
                }
            }
            if (!validationSkipFlag) {
                GenericCoreResponseBean<ValidationServicePreTxnResponse> validationServicePreTxnResponse = validationService
                        .executePreTxnValidationModel(workFlowTransactionBean);
                if (!validationServicePreTxnResponse.isSuccessfullyProcessed()) {
                    throw new EdcLinkBankAndBrandEmiCheckoutException(
                            validationServicePreTxnResponse.getFailureDescription(),
                            validationServicePreTxnResponse.getResponseConstant());
                }
            }

            GenericCoreResponseBean<OfferCheckoutResponse> offerCheckoutResponse = edcLinkService
                    .getOfferCheckoutResponse(workFlowTransactionBean, bankVerificationCode, brandVerificationCode,
                            velocityOfferId);
            if (!offerCheckoutResponse.isSuccessfullyProcessed()
                    && ff4JUtils.isFeatureEnabledOnMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                            THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                // Check to revert bank offer only in case of below condition,
                // here 3 & 2 denotes :
                // 3: Both subvention and bank offer was sent in request to
                // checkout - Input flag.
                // 2: Only bank offer was checked out successfully - Output
                // flag.
                if ("3".equals(offerCheckoutResponse.getResponse().getInputFlag())
                        && "2".equals(offerCheckoutResponse.getResponse().getOutputFlag())) {
                    LOGGER.info("Input flag & Output flag mismatch for revamp flow, performing Bank offer Rollback for Edc Link Txn");
                    rollbackEdcLinkTxnBankBankOffer(workFlowTransactionBean.getWorkFlowBean());
                }
                throw new EdcLinkBankAndBrandEmiCheckoutException(offerCheckoutResponse.getFailureDescription(),
                        offerCheckoutResponse.getResponseConstant());

            } else if (!offerCheckoutResponse.isSuccessfullyProcessed()) {
                LOGGER.info("OfferCheckout failed, performing Bank offer Rollback for Edc Link Txn");
                rollbackEdcLinkTxnBankBankOffer(workFlowTransactionBean.getWorkFlowBean());
                throw new EdcLinkBankAndBrandEmiCheckoutException(offerCheckoutResponse.getFailureDescription(),
                        offerCheckoutResponse.getResponseConstant());
            }

            if (isICBTransaction(workFlowTransactionBean, offerCheckoutResponse.getResponse())) {
                consultAffordabilityOrderCheckout(workFlowTransactionBean.getWorkFlowBean(),
                        offerCheckoutResponse.getResponse());
                workFlowTransactionBean.setModifyOrderRequired(true);
                modifyOrder(workFlowTransactionBean);
            }

            if (offerCheckoutResponse.getResponse() != null
                    && StringUtils.isNotBlank(offerCheckoutResponse.getResponse().getOutputFlag())) {
                workFlowTransactionBean
                        .getWorkFlowBean()
                        .getExtendInfo()
                        .setIsSubventionCreated(
                                "1".equals(offerCheckoutResponse.getResponse().getOutputFlag())
                                        || "3".equals(offerCheckoutResponse.getResponse().getOutputFlag()));
                workFlowTransactionBean
                        .getWorkFlowBean()
                        .getExtendInfo()
                        .setIsBankOfferApplied(
                                "2".equals(offerCheckoutResponse.getResponse().getOutputFlag())
                                        || "3".equals(offerCheckoutResponse.getResponse().getOutputFlag()));
            }
        }
    }

    private boolean isICBTransaction(WorkFlowTransactionBean workFlowTransactionBean,
            OfferCheckoutResponse offerCheckoutResponse) {
        boolean isICB = false;
        if (!(ff4jUtil.isFeatureEnabledOnMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                ENABLE_THEIA_EDC_EMI_ICB, false)
                && (workFlowTransactionBean.getWorkFlowBean().isFullPg2TrafficEnabled()) && (workFlowTransactionBean
                .getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)))) {
            return isICB;
        }
        boolean redemptionTypeDiscount = false;
        String orderType = null;
        boolean brandEmiOrder = isBrandEmiOffer(workFlowTransactionBean);
        if (brandEmiOrder) {
            orderType = OrderType.BRAND_EMI_ORDER.getValue();
        } else {
            orderType = OrderType.BANK_EMI_ORDER.getValue();
        }
        if (offerCheckoutResponse.getBankOfferDetails() != null
                && offerCheckoutResponse.getBankOfferDetails().getData() != null) {
            List<PromoSaving> savings = offerCheckoutResponse.getBankOfferDetails().getData().getSavings();
            if (CollectionUtils.isNotEmpty(savings)) {
                for (PromoSaving saving : savings) {
                    if (REDEMPTION_TYPE_DISCOUNT.equals(saving.getRedemptionType())) {
                        redemptionTypeDiscount = true;
                        break;
                    }
                }
            }
            if (redemptionTypeDiscount) {
                if (offerCheckoutResponse.getBankOfferDetails().getData().getPromoResponse() != null) {
                    Map<String, PromoResponseData> promoResponseDataMap = offerCheckoutResponse.getBankOfferDetails()
                            .getData().getPromoResponse();
                    for (PromoResponseData promoResponse : promoResponseDataMap.values()) {
                        BankOfferContriInfo bankOfferContriInfo = promoResponse.getBankOfferContriInfo();
                        if (bankOfferContriInfo.getBank() > 0 || bankOfferContriInfo.getBrand() > 0
                                || bankOfferContriInfo.getPlatform() > 0) {
                            workFlowTransactionBean.getWorkFlowBean().setICBFlow(true);
                            isICB = true;
                            workFlowTransactionBean.getWorkFlowBean().setOrderType(orderType);
                            return isICB;
                        }
                    }
                } else if (offerCheckoutResponse.getBankOfferDetails().getData().getPromoContext() != null) {
                    String promoResponse = offerCheckoutResponse.getBankOfferDetails().getData().getPromoContext()
                            .get("promoResponse");
                    if (promoResponse != null) {
                        try {
                            Map<String, String> promoResponseMap = JsonMapper.mapJsonToObject(promoResponse, Map.class);
                            for (String promoResponseMapData : promoResponseMap.values()) {
                                PromoResponseData data = JsonMapper.mapJsonToObject(promoResponseMapData,
                                        PromoResponseData.class);
                                BankOfferContriInfo bankOfferContriInfo = data.getBankOfferContriInfo();
                                if (bankOfferContriInfo.getBank() > 0 || bankOfferContriInfo.getBrand() > 0
                                        || bankOfferContriInfo.getPlatform() > 0) {
                                    workFlowTransactionBean.getWorkFlowBean().setICBFlow(true);
                                    isICB = true;
                                    workFlowTransactionBean.getWorkFlowBean().setOrderType(orderType);
                                    return isICB;
                                }
                            }
                        } catch (FacadeCheckedException ex) {
                            LOGGER.error("Error while mapping PromoResponseData to check BankOfferContriInfo");

                        }
                    }
                }
            }
        }

        redemptionTypeDiscount = false;
        if (offerCheckoutResponse.getSubventionDetail() != null
                && CollectionUtils.isNotEmpty(offerCheckoutResponse.getSubventionDetail().getItemBreakUp())) {
            List<ItemBreakUp> itemBreakUp = offerCheckoutResponse.getSubventionDetail().getItemBreakUp();
            for (ItemBreakUp item : itemBreakUp) {
                List<Gratification> gratifications = item.getGratifications();
                if (CollectionUtils.isNotEmpty(gratifications)) {
                    for (Gratification gratification : gratifications) {
                        if (GratificationType.DISCOUNT.equals(gratification.getType())) {
                            redemptionTypeDiscount = true;
                            break;
                        }
                    }
                }
                if (item.getAmountBearer() != null) {
                    AmountBearer amountBearer = item.getAmountBearer();
                    if (redemptionTypeDiscount && (amountBearer.getBrand() > 0 || amountBearer.getPlatform() > 0)) {
                        workFlowTransactionBean.getWorkFlowBean().setICBFlow(true);
                        isICB = true;
                        workFlowTransactionBean.getWorkFlowBean().setOrderType(orderType);
                        return isICB;
                    }
                }
            }
        }
        return isICB;
    }

    public void consultAffordabilityOrderCheckout(WorkFlowRequestBean workFlowRequestBean,
            OfferCheckoutResponse offerCheckoutResponse) {

        APOrderCheckoutRequest orderCheckoutRequest = new APOrderCheckoutRequest();
        orderCheckoutRequest.setProductCode(workFlowRequestBean.getProductCode());

        if (Objects.nonNull(workFlowRequestBean.getUserDetailsBiz())) {
            UserDetailsBiz userDetailsBiz = workFlowRequestBean.getUserDetailsBiz();
            UserInfo userInfo = new UserInfo();
            userInfo.setPaytmUserId(userDetailsBiz.getUserId());
            userInfo.setPplusUserId(userDetailsBiz.getInternalUserId());
            orderCheckoutRequest.setUserInfo(userInfo);
        } else if ((Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest())
                && Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody())
                && Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody().getUserInfo()) && Objects
                    .nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody().getUserInfo().getCustId()))
                || Objects.nonNull(workFlowRequestBean.getCustID())) {
            UserInfo userInfo = new UserInfo();
            String custId = Objects.nonNull(workFlowRequestBean.getCustID()) ? workFlowRequestBean.getCustID()
                    : workFlowRequestBean.getInitiateTransactionRequest().getBody().getUserInfo().getCustId();
            userInfo.setExternalUserId(custId);
            orderCheckoutRequest.setUserInfo(userInfo);
        }

        MerchantInfoBase merchantInfoBase = new MerchantInfoBase();
        merchantInfoBase.setMid(workFlowRequestBean.getPaytmMID());
        merchantInfoBase.setPplusMerchantId(workFlowRequestBean.getAlipayMID());
        if (workFlowRequestBean.getExtendInfo() != null) {
            merchantInfoBase.setDisplayName(workFlowRequestBean.getExtendInfo().getMerchantName());
            String merchantType = workFlowRequestBean.getExtendInfo().isMerchantOnPaytm() ? "ONUS" : "OFFUS";
            merchantInfoBase.setMerchantType(merchantType);
        }
        orderCheckoutRequest.setMerchantInfo(merchantInfoBase);

        try {
            orderCheckoutRequest.setEnvInfo(AlipayRequestUtils.createEnvInfo(workFlowRequestBean.getEnvInfoReqBean()));
        } catch (FacadeInvalidParameterException e) {
            EXT_LOGGER.customWarn("Exception in creating env info", e);
        }

        try {
            orderCheckoutRequest.setRiskExtendedInfo(JsonMapper.mapObjectToJson(workFlowRequestBean
                    .getRiskExtendedInfo()));
            orderCheckoutRequest.setExtendedInfo(JsonMapper.mapObjectToJson(workFlowRequestBean.getExtendInfo()));
        } catch (FacadeCheckedException e) {
            EXT_LOGGER.customWarn("Exception in serializing extendInfo", e);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(workFlowRequestBean.getOrderID());
        orderInfo.setOrderAmount(new Money(workFlowRequestBean.getTxnAmount()));
        setOrderCheckoutAmountInOrderInfo(orderInfo, workFlowRequestBean);
        orderCheckoutRequest.setOrderInfo(orderInfo);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setIssuingBankName(workFlowRequestBean.getBankName());

        PayOption payOption = new PayOption();
        payOption.setPayMode(workFlowRequestBean.getPayMethod());
        payOption.setPayAmount(new Money(workFlowRequestBean.getTxnAmount()));
        payOption.setPaymentDetails(paymentDetails);
        APPaymentInfo paymentInfo = new APPaymentInfo();
        List<PayOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(payOption);
        paymentInfo.setPayOptions(paymentOptions);
        orderCheckoutRequest.setPaymentInfo(paymentInfo);
        if (Objects.nonNull(offerCheckoutResponse.getBankOfferDetails())
                && Objects.nonNull(offerCheckoutResponse.getBankOfferDetails().getData())) {
            orderCheckoutRequest.setPromoCheckoutInfo(mapBankOfferToPromoResponseV2(offerCheckoutResponse
                    .getBankOfferDetails().getData()));
        }
        if (Objects.nonNull(offerCheckoutResponse.getSubventionDetail())) {
            orderCheckoutRequest.setSubventionCheckoutInfo(mapEdcSubventionToCheckoutSubvention(offerCheckoutResponse
                    .getSubventionDetail()));
        }
        if (Objects.nonNull(workFlowRequestBean.getOrderType())) {
            if (StringUtils.equals(OrderType.BRAND_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
                populateProductInfo(orderInfo, workFlowRequestBean);
            }
            orderInfo.setOrderType(workFlowRequestBean.getOrderType());
        }

        LOGGER.info("WorkFlowRequestBean {}", workFlowRequestBean);
        APOrderCheckoutResponse checkoutResponse = null;
        try {
            checkoutResponse = affordabilityService.orderCheckOut(orderCheckoutRequest);
        } catch (FacadeCheckedException e) {
            EXT_LOGGER.customError("Exception in order checkout at affordability platform", e);
        }
        if (Objects.nonNull(checkoutResponse) && Objects.nonNull(checkoutResponse.getResultInfo())
                && "S".equals(checkoutResponse.getResultInfo().getStatus())
                && Objects.nonNull(checkoutResponse.getCheckoutInfo())
                && Objects.nonNull(checkoutResponse.getCheckoutInfo().getCheckoutStatus())
                && "S".equals(checkoutResponse.getCheckoutInfo().getCheckoutStatus().getStatus())) {

            OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
            orderPricingInfo.setPricingAmountInfoList(checkoutResponse.getCheckoutInfo().getPricingAmountInfoList());
            workFlowRequestBean.setOrderPricingInfo(orderPricingInfo);

            if (workFlowRequestBean.getAdditionalOrderExtendInfo() == null) {
                workFlowRequestBean.setAdditionalOrderExtendInfo(new HashMap<>());
            }

            Money billAmount = checkoutResponse.getOrderInfo().getBillAmount();
            if (Objects.nonNull(billAmount)) {
                workFlowRequestBean.getExtendInfo().setBillAmount(billAmount.getAmount());
            }
            workFlowRequestBean.setDetailExtendInfo(checkoutResponse.getCheckoutInfo().getCheckoutExtendInfo());
        } else {
            throw new AffordabilityCheckoutException("Affordability checkout failed");
        }
    }

    private void setOrderCheckoutAmountInOrderInfo(OrderInfo orderInfo, WorkFlowRequestBean requestBean) {
        if (Boolean.TRUE.equals(requestBean.getEdcLinkTxn())
                && requestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            EdcEmiDetails edcEmiFields = requestBean.getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields();
            Money amount = new Money();
            if (edcEmiFields != null && StringUtils.isNotEmpty(edcEmiFields.getProductAmount())) {
                amount.setValue(AmountUtils.getTransactionAmountInPaise(edcEmiFields.getProductAmount()));
                amount.setCurrency(EnumCurrency.INR);
                orderInfo.setCheckoutOrderAmount(amount);
            }
        }
    }

    private void populateProductInfo(OrderInfo orderInfo, WorkFlowRequestBean requestBean) {
        EdcEmiDetails edcEmiFields = requestBean.getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields();
        List<ProductInfo> productInfoList = new ArrayList<>();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId(edcEmiFields.getProductId());
        productInfo.setPrice(edcEmiFields.getProductAmount());
        productInfo.setModel(edcEmiFields.getModel());
        productInfo.setBrandId(edcEmiFields.getBrandId());
        productInfo.setBrandName(edcEmiFields.getBrandName());
        productInfo.setProductName(edcEmiFields.getProductName());
        productInfo.setSkuIdentifier(edcEmiFields.getSkuCode());
        productInfoList.add(productInfo);
        orderInfo.setProductInfo(productInfoList);
    }

    public CheckoutPromoServiceResponseV2 mapBankOfferToPromoResponseV2(
            EdcLinkEmiBankOfferCheckoutData bankOfferCheckoutData) {
        CheckoutPromoServiceResponseV2 checkoutPromoResponseV2 = new CheckoutPromoServiceResponseV2();
        ApplyPromoResponseDataV2 applyPromoResponseDataV2 = new ApplyPromoResponseDataV2();
        applyPromoResponseDataV2.setPromoResponse(bankOfferCheckoutData.getPromoResponse());
        // applyPromoResponseDataV2.setPromoVisibility(bankOfferCheckoutData.getPromoVisibility());
        applyPromoResponseDataV2.setPromoContext(bankOfferCheckoutData.getPromoContext());
        applyPromoResponseDataV2.setEffectivePromoDeduction(bankOfferCheckoutData.getEffectivePromoDeduction());
        applyPromoResponseDataV2.setEffectivePromoSaving(bankOfferCheckoutData.getEffectivePromoSaving());
        applyPromoResponseDataV2.setPayText(bankOfferCheckoutData.getPayText());
        applyPromoResponseDataV2.setStatus(bankOfferCheckoutData.getStatus());
        applyPromoResponseDataV2.setPromotext(bankOfferCheckoutData.getPromotext());
        applyPromoResponseDataV2.setPrePromoText(bankOfferCheckoutData.getPrePromoText());
        applyPromoResponseDataV2.setResponseCode(bankOfferCheckoutData.getResponseCode());
        applyPromoResponseDataV2.setVerificationCode(bankOfferCheckoutData.getVerificationCode());
        applyPromoResponseDataV2.setTncUrl(bankOfferCheckoutData.getTncUrl());
        applyPromoResponseDataV2.setSavings(bankOfferCheckoutData.getSavings());
        applyPromoResponseDataV2.setPromocode(bankOfferCheckoutData.getPromocode());
        checkoutPromoResponseV2.setData(applyPromoResponseDataV2);
        return checkoutPromoResponseV2;
    }

    public GenericEmiSubventionResponse<CheckOutResponse> mapEdcSubventionToCheckoutSubvention(
            EdcLinkEmiSubventionDetail edcLinkEmiSubventionDetail) {

        CheckOutResponse checkOutResponse = new CheckOutResponse();
        checkOutResponse.setStatus(edcLinkEmiSubventionDetail.getStatus());
        checkOutResponse.setBankId(edcLinkEmiSubventionDetail.getBankId());
        checkOutResponse.setBankName(edcLinkEmiSubventionDetail.getBankName());
        checkOutResponse.setBankCode(edcLinkEmiSubventionDetail.getBankCode());
        checkOutResponse.setCardType(edcLinkEmiSubventionDetail.getCardType());
        checkOutResponse.setBankLogoUrl(edcLinkEmiSubventionDetail.getBankLogoUrl());
        checkOutResponse.setPlanId(edcLinkEmiSubventionDetail.getPlanId());
        checkOutResponse.setPgPlanId(edcLinkEmiSubventionDetail.getPgPlanId());
        checkOutResponse.setItemBreakUp(edcLinkEmiSubventionDetail.getItemBreakUp());
        checkOutResponse.setMessage(edcLinkEmiSubventionDetail.getMessage());
        checkOutResponse.setEmiLabel(edcLinkEmiSubventionDetail.getEmiLabel());
        checkOutResponse.setGratifications(edcLinkEmiSubventionDetail.getGratifications());

        checkOutResponse.setRate(edcLinkEmiSubventionDetail.getRate() != null ? Double
                .parseDouble(edcLinkEmiSubventionDetail.getRate()) : null);
        checkOutResponse.setInterest(edcLinkEmiSubventionDetail.getInterest() != null ? Double
                .parseDouble(edcLinkEmiSubventionDetail.getInterest()) : null);
        checkOutResponse.setInterval(edcLinkEmiSubventionDetail.getInterval() != null ? Integer
                .parseInt(edcLinkEmiSubventionDetail.getInterval()) : null);
        checkOutResponse.setEmi(edcLinkEmiSubventionDetail.getEmi() != null ? Double
                .parseDouble(edcLinkEmiSubventionDetail.getEmi()) : null);
        checkOutResponse.setEmiType(EmiType.SUBVENTION.getType().equalsIgnoreCase(
                edcLinkEmiSubventionDetail.getEmiType()) ? EmiType.SUBVENTION : EmiType.STANDARD);

        GenericEmiSubventionResponse<CheckOutResponse> subventionResponse = new GenericEmiSubventionResponse<>();
        subventionResponse.setData(checkOutResponse);

        return subventionResponse;
    }

    public void rollbackEdcLinkTxnBankBankOffer(WorkFlowRequestBean workFlowRequestBean) {
        if (Boolean.TRUE.equals(workFlowRequestBean.getEdcLinkTxn())
                && workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            EdcEmiChannelDetail emiChannelDetail = workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getEdcEmiFields().getEmiChannelDetail();
            if (CollectionUtils.isNotEmpty(emiChannelDetail.getOfferDetails())) {
                PaymentPromoServiceNotifyMsgV2 paymentPromoServiceNotifyMsgV2 = preparePromoServiceNotifyMsgV2(
                        workFlowRequestBean, BizConstant.CLIENT_EDC);
                paymentPromoServiceNotifyV2.pushFailureMsg(paymentPromoServiceNotifyMsgV2);
            }
        }

    }

    public boolean getValidationSkipFlag(WorkFlowRequestBean workFlowRequestBean) {
        if (Boolean.TRUE.equals(workFlowRequestBean.getEdcLinkTxn())
                && workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            EdcEmiDetails edcEmiFields = workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getEdcEmiFields();
            if (edcEmiFields != null && StringUtils.isNotBlank(edcEmiFields.getProductId()))
                return edcEmiFields.isValidationSkipFlag();
            else
                return true;
        }
        return true;
    }

    private String getPgPlanIdFromWorkFlowTxnBean(WorkFlowTransactionBean workflowTxnBean) {
        if (workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null
                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields() != null
                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                        .getEmiChannelDetail() != null) {
            return workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                    .getEmiChannelDetail().getPgPlanId();
        } else {
            return null;
        }
    }
}
