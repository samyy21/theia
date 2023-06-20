/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.core.merchant.service.IMerchantMappingService;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDRequestBizBean;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.model.wallet.PGPlusWalletDecisionMakerRequestBizBean;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.biz.core.user.service.IWallet;
import com.paytm.pgplus.biz.enums.*;
import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.ppbl.IBizAccessTokenPaymentRequestBuilder;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cache.model.routing.manual.BaseLine;
import com.paytm.pgplus.cache.model.routing.manual.ManualRouting;
import com.paytm.pgplus.cache.model.routing.manual.ManualRoutingConfigInfoResponse;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.AggregatorMidKeyUtil;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.*;
import com.paytm.pgplus.common.model.link.EdcEmiBankOfferDetails;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.PreferredOtpPage;
import com.paytm.pgplus.enums.SplitTypeEnum;
import com.paytm.pgplus.facade.acquiring.enums.TimeoutTypeEnum;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequest;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequestBody;
import com.paytm.pgplus.facade.bankOauth.models.*;
import com.paytm.pgplus.facade.coft.model.FetchPanUniqueReferenceRequest;
import com.paytm.pgplus.facade.coft.model.FetchPanUniqueReferenceRequestBody;
import com.paytm.pgplus.facade.coft.model.FetchPanUniqueReferenceRequestHead;
import com.paytm.pgplus.facade.coft.model.FetchPanUniqueReferenceResponse;
import com.paytm.pgplus.facade.coft.service.ICoftService;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.OfferDetail;
import com.paytm.pgplus.facade.common.model.*;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.models.request.BanksRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.OrderStampRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.UserSummaryBulkPaymentOptionsRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.UserSummaryBulkPaymentOptionsResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.emisubvention.utils.SubventionUtils;
import com.paytm.pgplus.facade.enums.EnumCurrency;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.enums.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.integration.enums.PPBLAccountType;
import com.paytm.pgplus.facade.payment.enums.UserType;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.paymentpromotion.models.response.CheckoutPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.service.IRouteClient;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody;
import com.paytm.pgplus.facade.user.models.request.SaveAssetRequest;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.utils.*;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IManualRoutingDataService;
import com.paytm.pgplus.mappingserviceclient.service.impl.GlobalConfigImpl;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.models.CoftConsent;
import com.paytm.pgplus.models.RiskFeeDetails;
import com.paytm.pgplus.payloadvault.subscription.request.ListSubscriptionRequest;
import com.paytm.pgplus.payloadvault.subscription.request.ListSubscriptionRequestBody;
import com.paytm.pgplus.payloadvault.subscription.response.ValidationHead;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.enums.CardType;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput.UserOwner;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.subscriptionClient.model.request.FreshSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.request.ModifySubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.request.RenewSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.enums.AddMoneySourceEnum.THIRD_PARTY;
import static com.paytm.pgplus.biz.utils.BizConstant.CUST_ID;
import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.IS_SAVED_CARD;
import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.common.constant.CommonConstant.ADD_MONEY_PHASE2_ENABLE_FLAG;
import static com.paytm.pgplus.common.enums.ERequestType.ADD_MONEY;
import static com.paytm.pgplus.common.enums.ERequestType.*;
import static com.paytm.pgplus.common.util.CommonConstants.*;
import static com.paytm.pgplus.facade.constants.FacadeConstants.ExtendInfo.ROUTE;
import static com.paytm.pgplus.facade.constants.FacadeConstants.RiskExtendedInfo.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.PAYER_CMID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ADD_MONEY_DESTINATION;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.PAYER_NAME;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPIPSPKeys.QR_SUBSCRIPTION;

/**
 * @author namanjain
 */
@SuppressWarnings("Duplicates")
@Component("commonFlowHelper")
public class WorkFlowRequestCreationHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowRequestCreationHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(WorkFlowRequestCreationHelper.class);

    @Autowired
    @Qualifier("walletServiceImpl")
    private IWallet walletService;

    @Autowired
    @Qualifier("userMappingServiceImpl")
    IUserMappingService userMappingService;

    @Autowired
    @Qualifier("merchantMappingServiceImpl")
    IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("bizAccessTokenPaymentRequestBuilder")
    private IBizAccessTokenPaymentRequestBuilder bizAccessTokenPaymentRequestBuilder;

    @Autowired
    @Qualifier("nativeCoreService")
    NativeCoreService nativeCoreService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    IManualRoutingDataService manualRoutingDataService;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardsService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private MerchantDataServiceImpl merchantDataService;

    @Autowired
    @Qualifier("bizRouteRedisCache")
    IRouteClient routeClient;

    @Autowired
    private PG2Util pg2Util;

    private static final String BALANCE_FETCH_ERR_MSG = "Error fetching walletBalance";
    private static final String SD_MERCHANT = "SD_MERCHANT";
    private static final String FIRST_REQUEST = "FIRST_REQUEST";
    private static final String PROMO_CHECKOUT_RESPONSE_DETAILS = "promoCheckoutResponseDetails";
    private static final String TXN_TYPE_ADDNPAY = "ADDNPAY";
    private static int ALL_CHAR_IN_NICKNAME = 60;
    private static final int NICKNAME_MAX_LENGTH = 60;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("CoftService")
    private ICoftService coftService;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    private GlobalConfigImpl globalConfigHelper;

    /*
     * if isTimeOutZero gets true , we'l make the OrderMiliSecond param as 1 as
     * we have to timeout createOrder instantly. if susbcription request is true
     * then payerUserID will be ""(blank) if we getting OrderMiliSecond in in
     * request then we'l use it else we'l get from property file
     */
    public BizCreateOrderRequest createOrderRequestBean(final WorkFlowTransactionBean flowTransBean,
            final boolean isTimeOutZero) {

        final BizCreateOrderRequest request = new BizCreateOrderRequest();
        LOGGER.debug("Calling create order, with iTimeOutZero :: {}", isTimeOutZero);
        Long orderTimeout = null;
        String paymentPendingTimeout = "";
        if (flowTransBean.getWorkFlowBean().isUpiDirectSettlementEnabled()
                && flowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled()) {
            EXT_LOGGER.info("Setting order time-out for P2PM");
            orderTimeout = Long.valueOf(Long.valueOf(ConfigurationUtil.getProperty(
                    "instant.settlement.close.order.days", "30")) * 24 * 60 * 60 * 1000L);
            setOrderTimeOutConfig(request, orderTimeout);
        } else {
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getOrderTimeOutInMilliSecond())
                    && NumberUtils.isNumber(flowTransBean.getWorkFlowBean().getOrderTimeOutInMilliSecond())) {
                orderTimeout = Long.valueOf(flowTransBean.getWorkFlowBean().getOrderTimeOutInMilliSecond());
            }

            if (SplitTypeEnum.POST_TXN.equals(flowTransBean.getWorkFlowBean().getSplitType())) {
                List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<>();
                Long splitTimeout = Long.valueOf("0");
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPostTxnSplitTimeout())
                        && StringUtils.isNumeric(flowTransBean.getWorkFlowBean().getPostTxnSplitTimeout())) {
                    splitTimeout = Long
                            .valueOf(Long.valueOf(flowTransBean.getWorkFlowBean().getPostTxnSplitTimeout()) * 24 * 60 * 60L);
                }
                timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.SPLIT_TIMEOUT, "false", null,
                        splitTimeout.toString()));
                request.setTimeoutConfigRuleList(timeoutConfigRuleList);
            }

            // Retry-Payment [PGP-18958]
            /*
             * if ((ERequestType.OFFLINE.equals(flowTransBean.getWorkFlowBean().
             * getRequestType()) ||
             * flowTransBean.getWorkFlowBean().isOfflineFastForwardRequest() ||
             * flowTransBean.getWorkFlowBean() .isOfflineFlow()) &&
             * PaymentTypeIdEnum
             * .PPI.value.equals(flowTransBean.getWorkFlowBean(
             * ).getPaymentTypeId ())) { paymentPendingTimeout =
             * BizConstant.PLATFORM_PLUS_CLOSE_ORDER_TIME; }
             */

            if (ERequestType.LINK_BASED_PAYMENT_INVOICE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || ERequestType.LINK_BASED_PAYMENT.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
                orderTimeout = Long.valueOf(Long.valueOf(ConfigurationUtil.getProperty(
                        "link.payment.close.order.second", "600")) * 1000L);
            }

            if (PaymentTypeIdEnum.BANK_MANDATE.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
                orderTimeout = Long
                        .valueOf(Long.valueOf(ConfigurationUtil.getProperty("mandate.order.close.days", "7")) * 24 * 60
                                * 60 * 1000L);
            }

            // for PreDebit case:
            if (null != flowTransBean.getSubscriptionServiceResponse()
                    && null != flowTransBean.getSubscriptionServiceResponse().getOrderActiveDays()
                    && flowTransBean.getSubscriptionServiceResponse().getOrderActiveDays().intValue() != 0) {
                orderTimeout = Long.valueOf(flowTransBean.getSubscriptionServiceResponse().getOrderActiveDays() * 24
                        * 60 * 60 * 1000L);
                LOGGER.info("PreDebit case: orderTimeout is: {}", orderTimeout);
            }
            if (null != flowTransBean.getSubscriptionServiceResponse()
                    && BooleanUtils.isTrue(flowTransBean.getSubscriptionServiceResponse()
                            .getOrderInactiveTimeOutEnabled())) {

                List<TimeoutConfigRule> timeoutConfigRuleList = request.getTimeoutConfigRuleList();
                if (null == timeoutConfigRuleList) {
                    timeoutConfigRuleList = new ArrayList<>();
                }
                if (null == orderTimeout) {
                    LOGGER.info("PreDebit case: orderTimeout is null, so setting it to default value");
                    orderTimeout = Long.valueOf(Long.valueOf(ConfigurationUtil.getProperty("mandate.order.close.days",
                            "7")) * 24 * 60 * 60 * 1000L);
                }
                LOGGER.info("PreDebit case: orderTimeout is: {}", orderTimeout);

                timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.INACTIVE_TIMEOUT, "true", String
                        .valueOf(orderTimeout / 1000)));
                timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false", String
                        .valueOf(orderTimeout / 1000)));
                request.setTimeoutConfigRuleList(timeoutConfigRuleList);
            } else if ((flowTransBean.getWorkFlowBean().getExtendInfo().isAutoRenewal() || flowTransBean
                    .getWorkFlowBean().getExtendInfo().isAutoRetry())
                    && flowTransBean.getWorkFlowBean().getExtendInfo().getGraceDays() > 0) {
                orderTimeout = Long.valueOf((flowTransBean.getWorkFlowBean().getExtendInfo().getGraceDays() + 1) * 24
                        * 60 * 60 * 1000L);
                List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<>();
                timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.INACTIVE_TIMEOUT, "true", String
                        .valueOf(orderTimeout / 1000)));
                timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false", String
                        .valueOf(orderTimeout / 1000)));
                request.setTimeoutConfigRuleList(timeoutConfigRuleList);

            } else if (flowTransBean.getWorkFlowBean().isOrderPSPRequest()) {
                List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<>();
                if (ff4jUtils.isFeatureEnabled(BizConstant.Ff4jFeature.ENABLE_ORDER_EXPIRY_LOGIC_FOR_P2PM, false)
                        && flowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled()
                        && !ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                                BizConstant.Ff4jFeature.DISABLE_PHASE_3_PG2, false)
                        && flowTransBean.getWorkFlowBean().isInstantSettlementEnabled()) {
                    timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false",
                            ConfigurationUtil.getProperty(BizConstant.UPI_INTENT_DIRECT_SETTLEMENT_TIMEOUT_IN_SECS,
                                    "2592000")));
                } else if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getUpiOrderTimeOutInSeconds())) {
                    timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false",
                            flowTransBean.getWorkFlowBean().getUpiOrderTimeOutInSeconds()));
                } else {
                    timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false",
                            ConfigurationUtil.getProperty("upi.intent.order.timeout.seconds", "1800")));
                }
                request.setTimeoutConfigRuleList(timeoutConfigRuleList);
            } else if (orderTimeout != null) {
                // orderTimeout field is in milliseconds
                setOrderTimeOutConfig(request, orderTimeout);
            }
        }

        EPayMethod payMethod = getEPayMethod(flowTransBean);
        String timeout = flowTransBean.getWorkFlowBean().getOrderExpiryTimeInMerchantContract();
        if (EPayMethod.BANK_TRANSFER.equals(payMethod) && StringUtils.isNotBlank(timeout)) {
            orderTimeout = setOrderTimeOutConfigForBankTransfer(request, flowTransBean, orderTimeout);
        }

        String amount = flowTransBean.getWorkFlowBean().getTxnAmount().trim();
        final String payerUserID = flowTransBean.getUserDetails() != null ? flowTransBean.getUserDetails()
                .getInternalUserId() : null;

        final BigDecimal txnAmount = new BigDecimal(amount);

        setRequestType(flowTransBean, request);
        request.setExternalUserId(flowTransBean.getWorkFlowBean().getCustID());

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentMid())
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentOrderId())) {
            try {
                MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(flowTransBean.getWorkFlowBean()
                        .getPaymentMid());
                EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
                request.setInternalMerchantId(merchantInfo.getAlipayId());
                flowTransBean.getWorkFlowBean().setDummyAlipayMid(merchantInfo.getAlipayId());
            } catch (Exception ex) {
                LOGGER.error("Exception in fetching merchant mapping data", ex);
            }
        } else {
            request.setInternalMerchantId(flowTransBean.getWorkFlowBean().getAlipayMID());
        }
        request.setOrderAmount(txnAmount);
        request.setOrderId(flowTransBean.getWorkFlowBean().getOrderID());

        if (orderTimeout != null
                && (ERequestType.isSubscriptionCreationRequest(flowTransBean.getWorkFlowBean().getRequestType()
                        .getType()))) {
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getTxnToken())) {
                theiaSessionRedisUtil.hsetIfExist(flowTransBean.getWorkFlowBean().getTxnToken(), "orderTimeOut",
                        String.valueOf(orderTimeout));
            }
        }
        request.setOrderTimeoutInMilliseconds(orderTimeout);
        request.setIndustryCode(flowTransBean.getWorkFlowBean().getIndustryTypeID());
        request.setClientIp(flowTransBean.getWorkFlowBean().getClientIP());

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getChannelID())) {
            request.setChannel(EChannelId.valueOf(flowTransBean.getWorkFlowBean().getChannelID()));
        }
        if (StringUtils.isNotBlank(paymentPendingTimeout)) {
            request.setPaymentPendingTimeout(paymentPendingTimeout);
        }
        request.setTransactionType(flowTransBean.getWorkFlowBean().getTransType());
        request.setOsType(flowTransBean.getWorkFlowBean().getEnvInfoReqBean().getOsType());
        request.setClientIp(flowTransBean.getWorkFlowBean().getClientIP());
        request.setPostConvenienceFee(flowTransBean.isPostConvenienceFeeModel());
        request.setSlabBasedMDR(flowTransBean.getWorkFlowBean().isSlabBasedMDR());
        request.setDynamicFeeMerchant(flowTransBean.getWorkFlowBean().isDynamicFeeMerchant());
        if (workFlowHelper.getProductCodeForDynamicChargePayment(flowTransBean.getWorkFlowBean().getProductCode()) == null) {
            request.setDefaultDynamicFeeMerchantPayment(true);
        }

        request.setCreationDate(new Date());
        request.setExtendInfo(createExtendedInfo(flowTransBean));
        if (flowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled()
                && !ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                        BizConstant.Ff4jFeature.DISABLE_PHASE_3_PG2, false)) {
            request.getExtendInfo().setRoute(Routes.PG2);
            setInactiveTimeoutforInstantSettlement(flowTransBean, request);
        }

        if (ObjectUtils.notEqual(flowTransBean.getUserDetails(), null)
                && StringUtils.isNotBlank(flowTransBean.getUserDetails().getUserName())) {
            String userName = null;
            userName = flowTransBean.getUserDetails().getUserName();
            if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                userName = userName.substring(0, NICKNAME_MAX_LENGTH);
            }
            request.setFirstName(userName);
        } else if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && flowTransBean.getWorkFlowBean().getPaymentRequestBean().getUserInfo() != null) {
            String userName = null;
            com.paytm.pgplus.models.UserInfo userInfo = flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                    .getUserInfo();
            if (StringUtils.isNotBlank(userInfo.getFirstName())
                    && !userInfo.getFirstName().trim().equalsIgnoreCase("NA")) {
                userName = userInfo.getFirstName();
            } else if (StringUtils.isNotBlank(userInfo.getLastName())
                    && !userInfo.getLastName().trim().equalsIgnoreCase("NA")) {
                userName = userInfo.getLastName();
            }
            if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                userName = userName.substring(0, NICKNAME_MAX_LENGTH);
            } else if (StringUtils.isNotBlank(userInfo.getLastName())
                    && !userInfo.getLastName().trim().equalsIgnoreCase("NA")) {
                userName = userInfo.getLastName();
            }
            if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                userName = userName.substring(0, NICKNAME_MAX_LENGTH);
            }
            EXT_LOGGER.info("User Name fetch from Merchant Provided User Info is : {}", userName);
            request.setFirstName(userName);
        } else {
            String userName = getUserNameFromOrderDetails(flowTransBean);
            if (StringUtils.isNotBlank(userName)) {
                request.setFirstName(userName);
            }
        }
        request.setAdditionalOrderExtendInfo(flowTransBean.getWorkFlowBean().getAdditionalOrderExtendInfo());

        if (flowTransBean.getWorkFlowBean().getRequestType().equals(ERequestType.SEAMLESS)) {
            request.setInternalUserId(null);
        } else if (ERequestType
                .isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType().getType())) {
            request.setInternalUserId(flowTransBean.getSubscriptionServiceResponse().getPayerUserID());
        } else {
            request.setInternalUserId(payerUserID);
        }
        request.setTokenId(flowTransBean.getWorkFlowBean().getToken());

        // Added for risk.
        request.setGoodsInfo(flowTransBean.getWorkFlowBean().getGoodsInfo());
        request.setShippingInfo(flowTransBean.getWorkFlowBean().getShippingInfo());
        request.setRiskToken(flowTransBean.getWorkFlowBean().getEnvInfoReqBean().getTokenId());
        request.setEnvInfoRequestBean(flowTransBean.getWorkFlowBean().getEnvInfoReqBean());
        request.setFromAoaMerchant(flowTransBean.getWorkFlowBean().isFromAoaMerchant());
        request.setCreateOrderForInitiateTxnRequest(flowTransBean.getWorkFlowBean()
                .isCreateOrderForInitiateTxnRequest());
        request.setOrderPricingInfo(flowTransBean.getWorkFlowBean().getOrderPricingInfo());
        request.setSplitCommandInfoList(flowTransBean.getWorkFlowBean().getSplitCommandInfoList());
        if (MapUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getEmiDetailInfo())) {
            request.setEmiDetailInfo(flowTransBean.getWorkFlowBean().getEmiDetailInfo());
        }
        request.setDealsTransaction(flowTransBean.getWorkFlowBean().isDealsFlow());

        if (SplitTypeEnum.POST_TXN.equals(flowTransBean.getWorkFlowBean().getSplitType())) {
            request.setSplitType(flowTransBean.getWorkFlowBean().getSplitType());
        }

        LOGGER.debug("Created request for Create Order as ::{}", request);
        return request;
    }

    private void setOrderTimeOutConfig(BizCreateOrderRequest request, Long orderTimeoutMilliseconds) {
        if (orderTimeoutMilliseconds == null || orderTimeoutMilliseconds < 1000) {
            return;
        }
        try {
            LOGGER.info("Setting order timeout config: orderTimeout is: {} s", orderTimeoutMilliseconds);
            List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<TimeoutConfigRule>();
            timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false", String
                    .valueOf(orderTimeoutMilliseconds / 1000)));
            request.setTimeoutConfigRuleList(timeoutConfigRuleList);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while setting Order timeout for BankTransfer {}", e);
        }
    }

    private Long setOrderTimeOutConfigForBankTransfer(BizCreateOrderRequest request,
            WorkFlowTransactionBean flowTransBean, Long orderTimeoutMilliseconds) {

        String timeout = flowTransBean.getWorkFlowBean().getOrderExpiryTimeInMerchantContract();
        EXT_LOGGER.customInfo("BankTransfer case: orderTimeout is: {} s", timeout);
        try {
            Long orderTimeoutSeconds = Long.valueOf(Long.valueOf(timeout));

            List<TimeoutConfigRule> timeoutConfigRuleList = request.getTimeoutConfigRuleList();
            if (null == timeoutConfigRuleList) {
                timeoutConfigRuleList = new ArrayList<>();
            }
            timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.INACTIVE_TIMEOUT, "false", String
                    .valueOf(orderTimeoutSeconds)));
            timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false", String
                    .valueOf(orderTimeoutSeconds)));
            request.setTimeoutConfigRuleList(timeoutConfigRuleList);
            orderTimeoutMilliseconds = orderTimeoutSeconds * 1000L;
            LOGGER.info("BankTransfer case: orderTimeoutMilliseconds is: {}", orderTimeoutMilliseconds);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while setting Order timeout for BankTransfer {}", e);
        }
        return orderTimeoutMilliseconds;
    }

    private void setRequestType(WorkFlowTransactionBean flowTransBean, BizCreateOrderRequest request) {

        if (ERequestType.DYNAMIC_QR_2FA.equals(flowTransBean.getWorkFlowBean().getRequestType()))
            request.setRequestType(ERequestType.DYNAMIC_QR_2FA);
        else if (flowTransBean.getWorkFlowBean().isPrnEnabled()) {
            request.setRequestType(ERequestType.QR_PRN_ENABLED);
        } else {
            request.setRequestType(flowTransBean.getWorkFlowBean().getRequestType());
        }
    }

    /*
     * In case of seamless we get cardInfo from Request Bean
     */
    public CacheCardRequestBean createCacheCardRequestBean(final WorkFlowTransactionBean flowTransBean,
            final CacheCardType cacheCardType) {

        if (CacheCardType.SEAMLESS.equals(cacheCardType)) {
            return getCacheCardRequestBeanForSeamless(flowTransBean);
        }

        if (CacheCardType.STOCK_TRADING.equals(cacheCardType)) {
            return createCacheCardRequestBeanForStockMerchantFlow(flowTransBean);
        }

        if (CacheCardType.MANDATE.equals(cacheCardType)) {
            return createCacheCardForMandateFlow(flowTransBean);
        }

        return createCacheCardRequestBean(flowTransBean);
    }

    private CacheCardRequestBean createCacheCardForMandateFlow(WorkFlowTransactionBean flowTransBean) {
        return new CacheCardRequestBean.CacheCardRequestBeanBuilder(flowTransBean.getWorkFlowBean().getAccountNumber(),
                flowTransBean.getWorkFlowBean().getBankCode(), flowTransBean.getWorkFlowBean().getIfsc(), flowTransBean
                        .getWorkFlowBean().getHolderName()).unMaskedAccountNo(
                flowTransBean.getWorkFlowBean().getAccountNumber()).build();
    }

    public CacheCardRequestBean createCacheCardRequestBean(final WorkFlowTransactionBean flowTransBean,
            final boolean isSeamlessRequest) {
        if (!isSeamlessRequest) {
            return createCacheCardRequestBean(flowTransBean);
        }
        String cardNoParam = flowTransBean.getWorkFlowBean().getCardNo();
        if (PaymentTypeIdEnum.IMPS.toString().equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            cardNoParam = flowTransBean.getWorkFlowBean().getMmid();
        }
        return new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNoParam, flowTransBean.getWorkFlowBean()
                .getInstNetworkType(), flowTransBean.getWorkFlowBean().getInstNetworkCode(), flowTransBean
                .getWorkFlowBean().getCardType(), flowTransBean.getWorkFlowBean().getCvv2(), flowTransBean
                .getWorkFlowBean().getCardScheme(), flowTransBean.getWorkFlowBean().getExpiryYear(), flowTransBean
                .getWorkFlowBean().getExpiryMonth())
                .instId(flowTransBean.getWorkFlowBean().getInstId())
                .holderMobileNo(
                        flowTransBean.getWorkFlowBean().getMobileNo() == null ? flowTransBean.getWorkFlowBean()
                                .getHolderMobileNo() : flowTransBean.getWorkFlowBean().getMobileNo())
                .otp(flowTransBean.getWorkFlowBean().getOtp()).build();
    }

    public CacheCardRequestBean getCacheCardRequestBeanForSeamless(WorkFlowTransactionBean flowTransBean) {
        String cardNoParam = flowTransBean.getWorkFlowBean().getCardNo();
        if (PaymentTypeIdEnum.IMPS.toString().equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            cardNoParam = flowTransBean.getWorkFlowBean().getMmid();
        }
        return new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNoParam, flowTransBean.getWorkFlowBean()
                .getInstNetworkType(), flowTransBean.getWorkFlowBean().getInstNetworkCode(), flowTransBean
                .getWorkFlowBean().getCardType(), flowTransBean.getWorkFlowBean().getCvv2(), flowTransBean
                .getWorkFlowBean().getCardScheme(), flowTransBean.getWorkFlowBean().getExpiryYear(), flowTransBean
                .getWorkFlowBean().getExpiryMonth())
                .instId(flowTransBean.getWorkFlowBean().getInstId())
                .holderMobileNo(
                        flowTransBean.getWorkFlowBean().getMobileNo() == null ? flowTransBean.getWorkFlowBean()
                                .getHolderMobileNo() : flowTransBean.getWorkFlowBean().getMobileNo())
                .otp(flowTransBean.getWorkFlowBean().getOtp())
                .cardIndexNo(flowTransBean.getWorkFlowBean().getCardIndexNo()).build();
    }

    /*
     * In case of seamless we get cardInfo from Request Bean
     */
    public CacheCardRequestBean createCacheCardRequestBeanForStockMerchantFlow(
            final WorkFlowTransactionBean flowTransBean) {

        return new CacheCardRequestBean.CacheCardRequestBeanBuilder(
                flowTransBean.getWorkFlowBean().getPaymentDetails(), flowTransBean.getWorkFlowBean().getBankCode(),
                InstNetworkType.SMA.getNetworkType()).build();
    }

    /*
     * In normal cases , we get information from SavedCardBean in transBean
     */
    public CacheCardRequestBean createCacheCardRequestBean(final WorkFlowTransactionBean flowTransBean) {

        String cardNumber;
        String insNetworkType;
        String instNetworkCode;
        String cardType;
        String cvv2;
        String cardScheme;
        Short expiryYear;
        Short expiryMonth;
        String cardIndexNo;
        String holderName;
        String holderMobileNo;
        String instId;
        String instBranchId;
        String otp;
        if (flowTransBean.getSavedCard() != null) {
            cardNumber = flowTransBean.getSavedCard().getCardNumber();
            insNetworkType = flowTransBean.getSavedCard().getInstNetworkType();
            instNetworkCode = flowTransBean.getSavedCard().getInstNetworkCode();
            cardType = flowTransBean.getSavedCard().getCardType();
            cvv2 = flowTransBean.getSavedCard().getCvv2();
            cardScheme = flowTransBean.getSavedCard().getCardScheme();

            expiryYear = Short.valueOf(StringUtils.substring(flowTransBean.getSavedCard().getExpiryDate(), 2, 6));
            expiryMonth = Short.valueOf(StringUtils.substring(flowTransBean.getSavedCard().getExpiryDate(), 0, 2));

            cardIndexNo = flowTransBean.getSavedCard().getCardIndexNo();

        } else {
            cardNumber = flowTransBean.getWorkFlowBean().getCardNo();
            insNetworkType = flowTransBean.getWorkFlowBean().getInstNetworkType();
            instNetworkCode = flowTransBean.getWorkFlowBean().getInstNetworkCode();
            cardType = flowTransBean.getWorkFlowBean().getCardType();
            cvv2 = flowTransBean.getWorkFlowBean().getCvv2();
            cardScheme = flowTransBean.getWorkFlowBean().getCardScheme();
            expiryYear = flowTransBean.getWorkFlowBean().getExpiryYear();
            expiryMonth = flowTransBean.getWorkFlowBean().getExpiryMonth();
            cardIndexNo = flowTransBean.getWorkFlowBean().getCardIndexNo();
        }
        holderName = flowTransBean.getSavedCard().getHolderName();
        holderMobileNo = flowTransBean.getSavedCard().getHolderMobileNo();
        instId = flowTransBean.getSavedCard().getInstId();
        instBranchId = flowTransBean.getSavedCard().getInstBranchId();
        otp = flowTransBean.getSavedCard().getOtp();
        return new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNumber, insNetworkType, instNetworkCode,
                cardType, cvv2, cardScheme, expiryYear, expiryMonth).cardIndexNo(cardIndexNo).holderName(holderName)
                .holderMobileNo(holderMobileNo).instId(instId).instBranchId(instBranchId).otp(otp).build();
    }

    /*
     * if isTerminalTypeSystem is true , then we have to set Terminal Type as
     * system(For Subscription)
     */
    public CreateOrderAndPayRequestBean createOrderAndPayRequestBean(final WorkFlowTransactionBean flowTransBean,
            final boolean isTerminalTypeSystem) throws BaseException, MappingServiceClientException,
            PaytmValidationException {
        WorkFlowRequestBean workFlowRequestBean = flowTransBean.getWorkFlowBean();
        enrichInExtendInfoForSeamlessPayment(flowTransBean);

        // Creating Order details
        final BizCreateOrderRequest bizCreateOrderRequest = createOrderRequestBean(flowTransBean, false);

        // Creating Pay methods
        EPayMethod payMethod = getEPayMethod(flowTransBean);

        // Creating extendedInfo
        Map<String, String> extendInfo = AlipayRequestUtils.getExtendeInfoMap(bizCreateOrderRequest.getExtendInfo());
        if (MapUtils.isNotEmpty(bizCreateOrderRequest.getAdditionalOrderExtendInfo())) {
            extendInfo.putAll(bizCreateOrderRequest.getAdditionalOrderExtendInfo());
        }

        // Creating riskExtendedInfo
        Map<String, String> riskExtendInfo = createRiskExtendedInfo(flowTransBean);

        String payOption;
        String chargeAmount = null;
        Long walletBalance = 0l;
        String differenceAmount;
        Long diffAmountInDouble = 0l;
        String payOption2 = null;
        EPayMethod payMethod2 = null;
        // Creating Pay Option Bills
        final List<BizPayOptionBill> payOptionBills = new ArrayList<>();
        BizPayOptionBill bizPayOption1 = null;
        BizPayOptionBill bizPayOption2 = null;
        Map<String, String> channelInfo = new HashMap<>();
        Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();

        setPassThroughChannelExtendedInfo(flowTransBean, channelInfo);
        setRedisKeyForQueryParams(workFlowRequestBean);

        if (StringUtils.equals(AOA_DQR, workFlowRequestBean.getTxnFlow())) {
            if (PaymentTypeIdEnum.NB.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
                if ((EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode()))) {
                    bizAccessTokenPaymentRequestBuilder.buildPPBPaymentRequestParameters(flowTransBean, channelInfo);
                } else {
                    addPassThroughExtendedInfoForAOADqr(flowTransBean, channelInfo);
                }
            } else if (PaymentTypeIdEnum.EMI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                    && ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                            THEIA_AOA_DQR_EMI_PASS_THROUGH_ENABLED, false)) {
                addPassThroughExtendedInfoForAOADqr(flowTransBean, channelInfo);
            }
        }

        // Passing expiry time in passthroughExtendInfo In UpiCollectPayments
        if (isUpiCollectPayment(flowTransBean)) {
            setPassThroughExtendInfoForUpiCollect(flowTransBean, channelInfo);
        } else if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            setPassThroughExtendInfoForOfflineUPI(flowTransBean, channelInfo);
        }

        // passing merchant dispaly name in channelinfo
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && !StringUtils.isBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName())) {
            channelInfo.put(ExtendedInfoKeys.MERCHANT_DISPLAY_NAME, flowTransBean.getWorkFlowBean().getExtendInfo()
                    .getMerchantName());
        }
        if (StringUtils.isNotBlank(workFlowRequestBean.getFromAOARequest())
                && workFlowRequestBean.getFromAOARequest().equalsIgnoreCase("true")) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
        } else {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
        }

        channelInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE, flowTransBean.getWorkFlowBean().getChannelInfo()
                .get(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE));

        if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()
                && ERequestType.isSubscriptionCreationRequest(flowTransBean.getWorkFlowBean().getRequestType()
                        .getType())) {
            channelInfo.put(IS_MANDATE, "true");
        }

        String txnAmountStr = bizCreateOrderRequest.getOrderAmount().toPlainString();

        if (EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode())) {
            setDeepIntegrationFieldsToPassThroughExtendedInfo(flowTransBean, passThroughExtendInfoMap);
        }

        if (EPayMethod.BANK_TRANSFER.equals(payMethod)) {
            bizPayOption1 = getBizPayOptionBillForBankTransfer(flowTransBean, payMethod, channelInfo, extendInfo);
        } else if (ERequestType
                .isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType().getType())
                || (FIRST_REQUEST.equals(flowTransBean.getWorkFlowBean().getSubsPaymentType()) && flowTransBean
                        .getWorkFlowBean().isInternalRequestForSubsPayment())) {

            switch (flowTransBean.getSubscriptionServiceResponse().getPaymentMode()) {
            case CC:
                payMethod = EPayMethod.CREDIT_CARD;
                payOption = CardType.CREDIT_CARD.name() + "_" + flowTransBean.getSavedCard().getCardScheme();
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
                break;
            case DC:
                payMethod = EPayMethod.DEBIT_CARD;
                payOption = CardType.DEBIT_CARD.name() + "_" + flowTransBean.getSavedCard().getCardScheme();
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
                break;
            case NORMAL:
                if (StringUtils.isBlank(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber())) {
                    throw new BaseException("Payer Account Number can't be blank in case of Noraml Susbcription");
                }
                UserInfo userData = userMappingService.getUserData(flowTransBean.getSubscriptionServiceResponse()
                        .getPayerUserID(), UserOwner.ALIPAY);

                GenericCoreResponseBean<Money> walletBalanceResponse = walletService.fetchWalletBalance(userData
                        .getPaytmId(), flowTransBean.getWorkFlowBean().getAdminToken(), flowTransBean.getWorkFlowBean()
                        .getOrderID());

                if (!walletBalanceResponse.isSuccessfullyProcessed()) {
                    LOGGER.error(BALANCE_FETCH_ERR_MSG);
                    throw new BaseException(BALANCE_FETCH_ERR_MSG);
                }

                walletBalance = Long.valueOf(walletBalanceResponse.getResponse().getAmount());
                differenceAmount = differenceAmount(txnAmountStr, walletBalance);
                diffAmountInDouble = Long.parseLong(differenceAmount);

                if (diffAmountInDouble <= 0) {
                    payMethod = EPayMethod.BALANCE;
                    payOption = "BALANCE";
                    bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                    bizPayOption1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse()
                            .getPayerAccountNumber());
                } else {
                    payMethod = EPayMethod.BALANCE;
                    payOption = "BALANCE";
                    if (walletBalance > 0) {
                        bizPayOption1 = new BizPayOptionBill(payOption, payMethod, walletBalance.toString(), "0");
                        bizPayOption1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse()
                                .getPayerAccountNumber());
                    }

                    /**
                     * In subscription renew flow, After savedCardMigration CIN
                     * will start coming in subscriptionResponse So we need to
                     * check either savedCardId or CIN to be able to complete
                     * txn
                     */

                    if (StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse().getSavedCardID())
                            || StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse()
                                    .getCardIndexNumber())) {

                        if (CardType.CREDIT_CARD.name().equalsIgnoreCase(flowTransBean.getSavedCard().getCardType())) {
                            payMethod2 = EPayMethod.CREDIT_CARD;
                            payOption2 = CardType.CREDIT_CARD.name() + "_"
                                    + flowTransBean.getSavedCard().getCardScheme();
                        } else if (CardType.DEBIT_CARD.name().equalsIgnoreCase(
                                flowTransBean.getSavedCard().getCardType())) {
                            payMethod2 = EPayMethod.DEBIT_CARD;
                            payOption2 = CardType.DEBIT_CARD.name() + "_"
                                    + flowTransBean.getSavedCard().getCardScheme();
                        }
                        bizPayOption2 = new BizPayOptionBill(payOption2, payMethod2, differenceAmount, "0");
                        bizPayOption2.setCardCacheToken(flowTransBean.getCacheCardToken());
                    }
                    // Add money through ppb
                    else {
                        payMethod2 = EPayMethod.RENEW_PPBL;
                        payOption2 = "PPBL";
                        passThroughExtendInfoMap.put(BizConstant.userId, userData.getPaytmId());
                        String passThroughExtendInfo = preparePassThroughExtendInfoForPPBL(passThroughExtendInfoMap);
                        channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, passThroughExtendInfo);
                        bizPayOption2 = new BizPayOptionBill(payOption2, payMethod2, differenceAmount, "0");

                    }
                    bizPayOption2.setTopupAndPay(true);
                }
                break;
            case PPI:
                payMethod = EPayMethod.BALANCE;
                payOption = "BALANCE";
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber());
                break;
            case PPBL:
                UserInfo userInfo = userMappingService.getUserData(flowTransBean.getSubscriptionServiceResponse()
                        .getPayerUserID(), UserOwner.ALIPAY);
                payMethod = EPayMethod.RENEW_PPBL;
                payOption = "PPBL";
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber());
                passThroughExtendInfoMap.put(BizConstant.userId, userInfo.getPaytmId());
                passThroughExtendInfoMap.put(BizConstant.accountType, PPBLAccountType.SAVING_ACCOUNT.getType());
                if (StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse().getAccountType())) {
                    passThroughExtendInfoMap.put(BizConstant.accountType, flowTransBean
                            .getSubscriptionServiceResponse().getAccountType());
                }
                String passThroughExtendInfo = preparePassThroughExtendInfoForPPBL(passThroughExtendInfoMap);
                channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, passThroughExtendInfo);
                break;
            case BANK_MANDATE:
                LOGGER.info("Preparing BizPayOptions for Bank Mandate Flow");
                bizPayOption1 = new BizPayOptionBill(null, null, txnAmountStr, "0");
                populatePayOptionBillForMandate(bizPayOption1, channelInfo, extendInfo, flowTransBean,
                        workFlowRequestBean);
                break;
            case UPI:
                bizPayOption1 = new BizPayOptionBill(null, null, txnAmountStr, "0");
                populatePayOptionBillForUPIRecurringMandate(bizPayOption1, channelInfo, extendInfo, flowTransBean,
                        workFlowRequestBean);
                bizPayOption1.setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVpa());
                break;
            default:
                LOGGER.error("Invalid Susbcription Type");
                throw new BaseException("System Error");
            }
        } else if (ERequestType.PAYTM_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE
                        .equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean.getWorkFlowBean()
                        .getRequestType())
                || ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean.getWorkFlowBean()
                        .getSubRequestType())
                || LINK_BASED_PAYMENT.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || LINK_BASED_PAYMENT_INVOICE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || NATIVE_MF_SIP.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || isSeamlessEMIPayment(flowTransBean.getWorkFlowBean())) {
            chargeAmount = fetchChargeAmountIfApplicable(flowTransBean);
            flowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);

            if (EPayMode.NONE.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {

                setPayOptionForUpiPushExpressOrIntent(flowTransBean);
                payOption = flowTransBean.getWorkFlowBean().getPayOption();
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, chargeAmount);

                if ((flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                        || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                        || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value) || flowTransBean
                        .getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value))) {

                    if (ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean.getWorkFlowBean()
                            .getRequestType())
                            || ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean
                                    .getWorkFlowBean().getSubRequestType())
                            || NATIVE_MF_SIP == flowTransBean.getWorkFlowBean().getRequestType()
                            || ERequestType.SEAMLESS.equals(flowTransBean.getWorkFlowBean().getRequestType())) {

                        bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());

                        if (PreferredOtpPage.BANK.getValue().equalsIgnoreCase(
                                flowTransBean.getWorkFlowBean().getPreferredOtpPage())) {
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
                        } else if (StringUtils.equalsIgnoreCase("true", flowTransBean.getWorkFlowBean()
                                .getiDebitEnabled())) {
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                                    DirectPaymentVerificationMethod.ATM.getValue());
                        } else if (isDirectChannel(flowTransBean)) {
                            // check if channel is direct
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                                    DirectPaymentVerificationMethod.OTP.getValue());
                        }
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.PREFERRED_OTP_PAGE, flowTransBean
                                .getWorkFlowBean().getPreferredOtpPage());
                    } else {
                        bizPayOption1.setCardCacheToken(flowTransBean.getWorkFlowBean().getPaymentDetails());
                    }
                } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.PPI.value)) {

                    bizPayOption1.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());

                } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId()
                        .equals(PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value)) {

                    bizPayOption1.setPayerAccountNo(workFlowRequestBean.getAdvanceDepositId());

                } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId()
                        .equals(PaymentTypeIdEnum.GIFT_VOUCHER.value)) {

                    bizPayOption1.setTemplateIds(flowTransBean.getWorkFlowBean().getMgvTemplateIds());
                    bizPayOption1.setChargeAmount("0");

                } else if (PaymentTypeIdEnum.NB.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                        && (ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean
                                .getWorkFlowBean().getRequestType()) || ERequestType
                                .isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(flowTransBean.getWorkFlowBean()
                                        .getSubRequestType()))) {
                    try {
                        if (EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode())) {
                            bizAccessTokenPaymentRequestBuilder.buildPPBPaymentRequestParameters(flowTransBean,
                                    channelInfo);
                        } else {
                            addPassThroughExtendedInfo(flowTransBean, channelInfo);
                            bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
                        }
                    } catch (FacadeUncheckedException | FacadeCheckedException e) {
                        throw new PaytmValidationException(
                                PaytmValidationExceptionType.INVALID_PAYMODE_NB.getValidationFailedMsg(), e);
                    }

                } else if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowTransBean.getWorkFlowBean()
                        .getPaymentTypeId())
                        && ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())) {
                    bizAccessTokenPaymentRequestBuilder.buildDigitalCreditRequestParameters(flowTransBean, channelInfo);

                } else if (PaymentTypeIdEnum.WALLET.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {

                    payMethod = EPayMethod.WALLET;
                    payOption = EPayMethod.WALLET.getMethod() + "_" + flowTransBean.getWorkFlowBean().getPayOption();
                    bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                }

            } else {

                List<BizPayOptionBill> paytmExpressPayOptionBills = generatePayOptionBillsForPaytmExpress(
                        flowTransBean, txnAmountStr, chargeAmount, channelInfo);
                bizPayOption1 = paytmExpressPayOptionBills.get(0);
                bizPayOption2 = paytmExpressPayOptionBills.get(1);
                if (bizPayOption1 != null
                        && (!NumberUtils.isNumber(bizPayOption1.getTransAmount())
                                || Integer.parseInt(bizPayOption1.getTransAmount()) <= 0 || flowTransBean
                                .getWorkFlowBean().isUpiConvertedToAddNPay())) {
                    bizPayOption1 = bizPayOption2;
                    bizPayOption2 = null;
                }

            }

            if (((workFlowRequestBean.isMerchantEligibleForMultipleMBIDFlow() && ff4jUtils.isFeatureEnabledOnMid(
                    workFlowRequestBean.getPaytmMID(), ENABLE_TPV_FOR_ALL_REQUEST_TYPES, false)) || (ERequestType.NATIVE_MF == workFlowRequestBean
                    .getRequestType()
                    || ERequestType.NATIVE_MF == workFlowRequestBean.getSubRequestType()
                    || ERequestType.NATIVE_ST == workFlowRequestBean.getSubRequestType()
                    || ERequestType.NATIVE_ST == workFlowRequestBean.getRequestType() || NATIVE_MF_SIP == workFlowRequestBean
                    .getRequestType()))
                    && (PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId()) || (PaymentTypeIdEnum.NB.value
                            .equals(workFlowRequestBean.getPaymentTypeId()) && !EPayMethod.PPBL.getOldName().equals(
                            flowTransBean.getWorkFlowBean().getBankCode())))) {
                workFlowHelper.setAccountInfoInChannelInfo(channelInfo, workFlowRequestBean);
                LOGGER.info("Adding Account verification Related Info in ChannelInfo");
                channelInfo.put(TheiaConstant.ChannelInfoKeys.EXPECTED_USER_FUND_SOURCE,
                        workFlowRequestBean.getAccountNumber());
                channelInfo.put(TheiaConstant.ChannelInfoKeys.NEED_FUND_SOURCE_VERIFICATION,
                        StringUtils.isBlank(workFlowRequestBean.getValidateAccountNumber()) ? "false"
                                : workFlowRequestBean.getValidateAccountNumber());
                channelInfo.put(TheiaConstant.ChannelInfoKeys.ALLOW_UNVERIFIED_FUND_SOURCE,
                        StringUtils.isBlank(workFlowRequestBean.getAllowUnverifiedAccount()) ? "false"
                                : workFlowRequestBean.getAllowUnverifiedAccount());
            } else if (((workFlowRequestBean.isMerchantEligibleForMultipleMBIDFlow() && ff4jUtils
                    .isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), ENABLE_TPV_FOR_ALL_REQUEST_TYPES, false)) || (ERequestType.NATIVE_MF == workFlowRequestBean
                    .getRequestType() || ERequestType.NATIVE_MF == workFlowRequestBean.getSubRequestType()))
                    && StringUtils.isNotEmpty(workFlowRequestBean.getSubscriptionID())) {
                if (PaymentTypeIdEnum.BANK_MANDATE.value.equals(workFlowRequestBean.getPaymentTypeId())) {
                    populatePayOptionBillForMandate(bizPayOption1, channelInfo, extendInfo, flowTransBean,
                            workFlowRequestBean);
                } else if (PaymentTypeIdEnum.DC.value.equals(workFlowRequestBean.getPaymentTypeId())
                        && workFlowRequestBean.isMotoPaymentBySubscription()) {
                    bizPayOption1.setPayMode("MOTO");
                    extendInfo.put(TheiaConstant.ExtendedInfoPay.SUBSCRIPTION_ID,
                            workFlowRequestBean.getSubscriptionID());
                    extendInfo.put(TheiaConstant.ExtendedInfoPay.ISSUING_BANK_NAME, workFlowRequestBean.getBankName());
                }
            }
        } else if (flowTransBean.getWorkFlowBean().getRequestType().equals(ERequestType.MOTO_CHANNEL)) {
            switch (flowTransBean.getWorkFlowBean().getPaymentTypeId()) {
            case "CC":
                payMethod = EPayMethod.CREDIT_CARD;
                payOption = CardType.CREDIT_CARD.name() + "_" + flowTransBean.getSavedCard().getCardScheme();
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
                break;
            case "DC":
                payMethod = EPayMethod.DEBIT_CARD;
                payOption = CardType.DEBIT_CARD.name() + "_" + flowTransBean.getSavedCard().getCardScheme();
                bizPayOption1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
                break;
            default:
                LOGGER.error("Invalid Pay Method Type");
                throw new BaseException("Invalid Pay Method Type");
            }
        } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {
            bizPayOption1 = new BizPayOptionBill(flowTransBean.getWorkFlowBean().getPayOption(), payMethod,
                    flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean().getChargeAmount());
            bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
            setDirectPayMode(flowTransBean, channelInfo);
        } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.PPI.value)) {
            payMethod = EPayMethod.BALANCE;
            bizPayOption1 = new BizPayOptionBill("BALANCE", payMethod, flowTransBean.getWorkFlowBean().getTxnAmount(),
                    flowTransBean.getWorkFlowBean().getChargeAmount());
            bizPayOption1.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());
        } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.LOYALTY_POINT.value)) {
            payMethod = EPayMethod.LOYALTY_POINT;
            bizPayOption1 = new BizPayOptionBill("LOYALTY_POINT", payMethod, flowTransBean.getWorkFlowBean()
                    .getTxnAmount(), flowTransBean.getWorkFlowBean().getChargeAmount());
            bizPayOption1.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());
            /**
             * support for allowing 3P merchants to avail loyalty points JIRA ID
             * : PGP-23578
             */
            setUserInfoForRedemptionOf3PMerchant(bizPayOption1, flowTransBean, bizCreateOrderRequest, extendInfo);
        } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)
                || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            setPayOptionForUpiPushExpressOrIntent(flowTransBean);
            if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                    && flowTransBean.getWorkFlowBean().isNativeDeepLinkReqd()) {
                setParamInChannelInfo(flowTransBean, channelInfo);
            }
            bizPayOption1 = new BizPayOptionBill(flowTransBean.getWorkFlowBean().getPayOption(), payMethod,
                    flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean().getChargeAmount());
            bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
        }

        channelInfo.put(BizConstant.BROWSER_USER_AGENT, flowTransBean.getWorkFlowBean().getEnvInfoReqBean()
                .getBrowserUserAgent());
        if (workFlowRequestBean.isNativeDeepLinkReqd() || "true".equals(workFlowRequestBean.getIsDeepLinkReq())) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ, "true");
        }
        channelInfo.putAll(buildDefaultChannelInfo());

        Map<String, String> workFlowBeanChannelInfo = flowTransBean.getWorkFlowBean().getChannelInfo();
        if ((ERequestType.isNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType()) || ERequestType.SEAMLESS
                .equals(flowTransBean.getWorkFlowBean().getRequestType()))
                && null != workFlowBeanChannelInfo
                && "Y".equals(workFlowBeanChannelInfo.get(BizConstant.IS_EMI))) {
            channelInfo.putAll(workFlowBeanChannelInfo);
        } else if (flowTransBean.getWorkFlowBean().getRequestType().equals(ERequestType.PAYTM_EXPRESS)
                && workFlowBeanChannelInfo != null
                && StringUtils.equalsIgnoreCase(workFlowBeanChannelInfo.get(BizConstant.IS_EMI), "Y")) {
            channelInfo.putAll(workFlowBeanChannelInfo);
        } else {
            channelInfo.put(BizConstant.IS_EMI, "N");
        }
        if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            channelInfo.put(BizConstant.VIRTUAL_PAYMENT_ADDRESS, flowTransBean.getWorkFlowBean()
                    .getVirtualPaymentAddress());

            setVPAforUPIPushAndCollect(bizPayOption1, bizPayOption2, flowTransBean.getWorkFlowBean()
                    .getVirtualPaymentAddress(), flowTransBean.getUserDetails());
            riskExtendInfo.put(BizConstant.VPA, flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());
            flowTransBean.getWorkFlowBean().getExtendInfo()
                    .setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());

            if (ERequestType.NATIVE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                    || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType())
                    || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                    || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                    || ERequestType.isSubscriptionCreationRequest(workFlowRequestBean.getRequestType().getType())
                    || ERequestType.SEAMLESS.equals(workFlowRequestBean.getRequestType())
                    || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())) {

                if (ERequestType.isSubscriptionCreationRequest(workFlowRequestBean.getRequestType().getType())
                        && !EPayMode.ADDANDPAY.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
                    channelInfo.put(UPI_MANDATE, "true");
                }
                if (flowTransBean.getWorkFlowBean().isUpiPushExpressSupported()) {
                    nativeCoreService.ceatePassThroughExtendedInfoNativeUPI(flowTransBean, channelInfo);
                } else if (flowTransBean.getWorkFlowBean().isNativeDeepLinkReqd()) {
                    nativeCoreService.ceatePassThroughExtendedInfoNativeUPIIntent(flowTransBean, channelInfo);
                }

                setAddNPayInPassThroughInfo(channelInfo, workFlowRequestBean);
                setZeroSubsInfoInPassThroughInfo(channelInfo, workFlowRequestBean);
            }

            if ((ERequestType.OFFLINE.equals(flowTransBean.getWorkFlowBean().getRequestType()) || flowTransBean
                    .getWorkFlowBean().isOfflineFlow()) && flowTransBean.getWorkFlowBean().isUpiPushExpressSupported()) {
                Map<String, String> passThroughExtendInfo = new HashMap<String, String>();
                EXT_LOGGER.info("upiRefId for upiPushTxn exists {}",
                        StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getUpiAccRefId()));
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getUpiAccRefId())) {
                    passThroughExtendInfo.put(FacadeConstants.UPI_ACC_REF_ID, flowTransBean.getWorkFlowBean()
                            .getUpiAccRefId());
                    passThroughExtendInfo.put(FacadeConstants.ORIGIN_CHANNEL, flowTransBean.getWorkFlowBean()
                            .getOriginChannel());
                } else {
                    passThroughExtendInfo.put(FacadeConstants.IFSC, flowTransBean.getWorkFlowBean().getIfsc());
                    passThroughExtendInfo.put(FacadeConstants.ACCOUNT_NUMBER, flowTransBean.getWorkFlowBean()
                            .getAccountNumber());
                }
                passThroughExtendInfo.put(FacadeConstants.DEVICE_ID, flowTransBean.getWorkFlowBean().getDeviceId());
                if (flowTransBean.getUserDetails() != null) {
                    passThroughExtendInfo.put(FacadeConstants.MOBILE_NO, flowTransBean.getUserDetails().getMobileNo());
                    passThroughExtendInfoMap.put(FacadeConstants.CUSTID, flowTransBean.getUserDetails().getUserId());
                }
                passThroughExtendInfo.put(FacadeConstants.SEQUENCE_NUMBER, flowTransBean.getWorkFlowBean().getSeqNo());
                passThroughExtendInfo.put(FacadeConstants.MPIN, flowTransBean.getWorkFlowBean().getMpin());
                passThroughExtendInfo.put(FacadeConstants.PAYER_VPA, flowTransBean.getWorkFlowBean()
                        .getVirtualPaymentAddress());
                passThroughExtendInfo.put(FacadeConstants.ORDER_ID, flowTransBean.getWorkFlowBean().getOrderID());
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getAppId())) {
                    passThroughExtendInfo.put(FacadeConstants.APP_ID, flowTransBean.getWorkFlowBean().getAppId());
                }
                if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                        && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                                .getMerchantVpa())) {
                    passThroughExtendInfo.put(ExtendedInfoKeys.MERCHANT_VPA, flowTransBean.getWorkFlowBean()
                            .getPaymentRequestBean().getMerchantVpa());
                }

                setEncodedPassThrough(channelInfo, passThroughExtendInfo);
            }
            setPassThroughExtendInfoForOfflineUPI(flowTransBean, channelInfo);
        }

        setPassThroughExtendInfoForUpiLite(flowTransBean, channelInfo);

        setBankFormOptimizedFlow(flowTransBean, channelInfo);
        setPayModeForVisaSingleClick(bizPayOption1, bizPayOption2, workFlowRequestBean);

        if (StringUtils.isNotBlank(workFlowRequestBean.getCardNo())) {
            extendInfo.put(TheiaConstant.RequestParams.BIN_NUMBER, workFlowRequestBean.getCardNo().substring(0, 6));
        }

        setBinDetailInExtendedInfo(flowTransBean, extendInfo);

        if (flowTransBean.getUserDetails() != null) {
            if (StringUtils.isBlank(extendInfo.get(TheiaConstant.ExtendedInfoSubscription.USER_MOBILE))) {
                extendInfo.put(TheiaConstant.ExtendedInfoSubscription.USER_MOBILE, flowTransBean.getUserDetails()
                        .getMobileNo());
            }
            if (StringUtils.isNotBlank(flowTransBean.getUserDetails().getUserName())) {
                String userName = null;
                userName = flowTransBean.getUserDetails().getUserName();
                if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                    userName = userName.substring(0, NICKNAME_MAX_LENGTH);
                }
                extendInfo.put(NICKNAME, userName);
            }
        } else {
            String userName = getUserNameFromOrderDetails(flowTransBean);
            if (StringUtils.isNotBlank(userName)) {
                extendInfo.put(NICKNAME, userName);
            }
        }

        if (StringUtils.equals(AOA_DQR, workFlowRequestBean.getTxnFlow())) {
            extendInfo.put(TXN_FLOW, AOA_DQR);
        }

        settingSubsDetailsInChannelInfo(flowTransBean, channelInfo);

        if (bizPayOption1 != null) {
            bizPayOption1.setChannelInfo(channelInfo);
            bizPayOption1.setExtendInfo(extendInfo);
            payOptionBills.add(bizPayOption1);
        }
        if (bizPayOption2 != null) {
            bizPayOption2.setChannelInfo(channelInfo);
            bizPayOption2.setExtendInfo(extendInfo);
            payOptionBills.add(bizPayOption2);
        }
        // setting flag to save card at platform
        saveCardAtPlatformIfApplicable(flowTransBean, payOptionBills);
        updatePayOptionBillsForCoft(flowTransBean, payOptionBills);

        setFeeRateFactorsInfo(bizPayOption1, bizPayOption2, workFlowRequestBean);

        // Creating paymentInfo from PayMethod & PayOptions
        final BizPaymentInfo paymentInfo;
        if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()) {
            BizChannelPreference bizChannelPreference = calculateChannelPreferenceForAoaMerchant(flowTransBean);
            paymentInfo = new BizPaymentInfo(flowTransBean.getWorkFlowBean().getContractId(), payOptionBills,
                    bizChannelPreference);

        } else {
            paymentInfo = new BizPaymentInfo(flowTransBean.getWorkFlowBean().getContractId(), payOptionBills,
                    flowTransBean.getWorkFlowBean().getPwpCategory(), flowTransBean.getWorkFlowBean()
                            .isAddAndPayWithUPICollectSupported());
        }
        if ((EPayMethod.UPI.equals(payMethod) || EPayMethod.UPI_INTENT.equals(payMethod))
                && ObjectUtils.notEqual(flowTransBean.getWorkFlowBean().getUltimateBeneficiaryDetails(), null)) {
            paymentInfo.setUltimateBeneficiaryDetails(flowTransBean.getWorkFlowBean().getUltimateBeneficiaryDetails());
        }
        String productCode = null;
        try {
            String executeGetProductById = String.valueOf(ProductCodes.getProductById(flowTransBean.getWorkFlowBean()
                    .getExtendInfo().getProductCode()));
            if (workFlowRequestBean.isDealsFlow())
                productCode = ProductCodes.StandardDirectPayDealAcquiringProd.getProductCode();
            else if ((StringUtils.isNotBlank(bizCreateOrderRequest.getExtendInfo().getDummyMerchantId()) && StringUtils
                    .isNotBlank(bizCreateOrderRequest.getExtendInfo().getDummyOrderId()))
                    || ProductCodes.StandardDirectPayPennyDropProd.getId().equals(
                            flowTransBean.getWorkFlowBean().getExtendInfo().getProductCode())) {
                productCode = executeGetProductById;
            } else if (ERequestType.NATIVE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    && flowTransBean.getWorkFlowBean().getExtendInfo().getProductCode() != null) {
                productCode = executeGetProductById;
            } else if (flowTransBean.isPostConvenienceFeeModel()
                    && flowTransBean.getWorkFlowBean().getExtendInfo().getProductCode() != null) {
                productCode = executeGetProductById;
            } else if (ERequestType.PAYTM_EXPRESS.getType().equals(
                    flowTransBean.getWorkFlowBean().getRequestType().getType())
                    && flowTransBean.getWorkFlowBean().getExtendInfo().getProductCode() != null
                    && ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                            Ff4jFeature.ENABLE_SET_PAYTM_EXPRESS_PRODUCT_CODE_MAPPING, false)) {
                if (ProductCodes.StandardAcquiringProdByJSChargePayee.getProductCode().equals(executeGetProductById)) {
                    productCode = String.valueOf(ProductCodes.StandardDirectPayAcquiringProd.getProductCode());
                    // updating productCode in extendInfo
                    flowTransBean.getWorkFlowBean().getExtendInfo()
                            .setProductCode(ProductCodes.StandardDirectPayAcquiringProd.getId());
                } else if (ProductCodes.StandardAcquiringProdByJSChargePayer.getProductCode().equals(
                        executeGetProductById)) {
                    productCode = String.valueOf(ProductCodes.StandardDirectPayAcquiringProdChargePayer
                            .getProductCode());
                    // updating productCode in extendInfo
                    flowTransBean.getWorkFlowBean().getExtendInfo()
                            .setProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer.getId());
                } else {
                    productCode = executeGetProductById;
                }
            } else {
                productCode = bizCreateOrderRequest.getRequestType().getProductCode();
            }
        } catch (FacadeInvalidParameterException e) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PRODUCT_CODE);
        }

        updateGcinForCoftInCOP(flowTransBean, payOptionBills);

        if (EPayMethod.PPBL.getOldName().equalsIgnoreCase(flowTransBean.getWorkFlowBean().getBankCode())) {
            setPPBLAccountTypeInPassThrough(paymentInfo.getPayOptionBills(), flowTransBean.getWorkFlowBean());
        }

        String alipayMid = flowTransBean.getWorkFlowBean().getAlipayMID();
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getDummyAlipayMid())) {
            alipayMid = flowTransBean.getWorkFlowBean().getDummyAlipayMid();
        }
        // Creating CreateOrderAndPay request data
        final CreateOrderAndPayRequestBean createOrderAndPayRequestBean = new CreateOrderAndPayRequestBean(
                bizCreateOrderRequest, alipayMid, flowTransBean.getWorkFlowBean().getExtendInfo().getMccCode(),
                productCode, RequestIdGenerator.generateRequestId(), paymentInfo, flowTransBean.getWorkFlowBean()
                        .getExtendInfo(), riskExtendInfo, flowTransBean.getWorkFlowBean()
                        .isCostBasedPreferenceEnabled(), flowTransBean.getWorkFlowBean().getPaytmMID());

        if (isTerminalTypeSystem) {
            flowTransBean.getWorkFlowBean().getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);
        }

        EXT_LOGGER.customInfo("Route received {} before COP", flowTransBean.getWorkFlowBean().getRoute());
        Map<String, String> distributionMap = new HashMap<>();
        distributionMap.put("env", Routes.PLATFORM.getName());
        distributionMap.put("payMethod", flowTransBean.getWorkFlowBean().getPayMethod());
        distributionMap.put("payOption", flowTransBean.getWorkFlowBean().getPayOption());
        Routes route = null;
        if (flowTransBean.getWorkFlowBean().getRoute() != null)
            route = flowTransBean.getWorkFlowBean().getRoute();
        else if (flowTransBean.getWorkFlowBean().getTransType() == ETransType.TOP_UP)
            route = pg2Util.getRouteForTopUpRequest(flowTransBean.getWorkFlowBean().getPaytmMID());
        else if (flowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled()
                && ff4jUtils.isFeatureEnabledOnMid(createOrderAndPayRequestBean.getPaytmMerchantId(),
                        BizConstant.Ff4jFeature.DISABLE_PHASE_3_PG2, false))
            route = Routes.PLATFORM;
        else if (flowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled())
            route = Routes.PG2;
        else if (isPartiallyMigratedMerchantEligibleForPg2(flowTransBean.getWorkFlowBean()))
            route = Routes.PG2;

        if (route == null)
            route = Routes.PLATFORM;
        createOrderAndPayRequestBean.getExtendInfo().setRoute(route);
        distributionMap.put("env", route.getName());
        flowTransBean.getWorkFlowBean().setRoute(route);
        EventUtils.pushTheiaEvents(EventNameEnum.TRAFFIC_DISTRIBUTION, distributionMap);

        updateRiskExtendedInfoForPG2(flowTransBean, createOrderAndPayRequestBean.getRiskExtendInfo(), route.getName());

        createOrderAndPayRequestBean.setEnvInfo(flowTransBean.getWorkFlowBean().getEnvInfoReqBean());
        createOrderAndPayRequestBean.setExtendInfo(flowTransBean.getWorkFlowBean().getExtendInfo());
        createOrderAndPayRequestBean.setFromAoaMerchant(flowTransBean.getWorkFlowBean().isFromAoaMerchant());
        createOrderAndPayRequestBean.setPaytmMerchantId(flowTransBean.getWorkFlowBean().getPaytmMID());
        setSubwalletAmountDetails(createOrderAndPayRequestBean, flowTransBean);

        if (ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                Ff4jFeature.ENABLE_SET_PAYTM_EXPRESS_PRODUCT_CODE_MAPPING, false)) {
            verifyProductCodeInPaymentInfo(createOrderAndPayRequestBean.getPaymentInfo());
        }

        if (PaymentAdapterUtil.eligibleForPG2(createOrderAndPayRequestBean.getExtendInfo().getRoute())) {
            updateSettleTypeinPayOptionBills(flowTransBean, createOrderAndPayRequestBean.getPaymentInfo()
                    .getPayOptionBills());
            updateDeepLinkReqdinPayOptionBills(flowTransBean, createOrderAndPayRequestBean.getPaymentInfo()
                    .getPayOptionBills());
            updateCCOnUPIReqInPayOptionBills(flowTransBean, createOrderAndPayRequestBean.getPaymentInfo()
                    .getPayOptionBills());
            setInactiveTimeoutforInstantSettlement(flowTransBean, createOrderAndPayRequestBean.getOrder());
            updatePayOptionBillsForBankTransferPG2(workFlowRequestBean, createOrderAndPayRequestBean.getPaymentInfo()
                    .getPayOptionBills());
        } else if (createOrderAndPayRequestBean.getExtendInfo() != null
                && (createOrderAndPayRequestBean.getExtendInfo().getRoute() == null || Routes.PLATFORM
                        .equals(createOrderAndPayRequestBean.getExtendInfo().getRoute()))
                && SettlementType.DIRECT_SETTLEMENT.getValue().equals(flowTransBean.getWorkFlowBean().getSettleType())) {
            LOGGER.error("Direct Settlement is not allowed on this route");
            throw new PaytmValidationException("Direct Settlement is not allowed on this route");
        }

        /***
         * WillUserChange will be set false if user is not changed ( At present-
         * Static QR and autodebit cases).
         */
        if (!flowTransBean.getWorkFlowBean().isWillUserChange() || flowTransBean.getWorkFlowBean().isOfflineFlow()) {
            createOrderAndPayRequestBean.setWillUserChange(false);
        }

        if (StringUtils.isNotBlank(workFlowRequestBean.getPaymentMid())
                && StringUtils.isNotBlank(workFlowRequestBean.getPaymentOrderId())) {
            createOrderAndPayRequestBean.getExtendInfo().setPaytmMerchantId(workFlowRequestBean.getPaymentMid());
        }

        if (StringUtils.isNotBlank(workFlowRequestBean.getPaymentMid())
                && StringUtils.isNotBlank(workFlowRequestBean.getPaymentOrderId())) {
            createOrderAndPayRequestBean.getExtendInfo().setAlipayMerchantId(alipayMid);
        }

        createOrderAndPayRequestBean.setPaymentBizInfo(AlipayRequestUtils.getPaymentBizinfo(flowTransBean));
        setOfferDetailsInPaymentBizData(flowTransBean, createOrderAndPayRequestBean.getPaymentBizInfo());

        if (flowTransBean.getWorkFlowBean() != null
                && StringUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getRequestFlow()))
            createOrderAndPayRequestBean.setRequestFlow(flowTransBean.getWorkFlowBean().getRequestFlow());

        if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().isMerchantEligibleForMultipleMBIDFlow())) {
            VerificationType verificationType = workFlowHelper.getVerificationType(flowTransBean.getWorkFlowBean());
            if (createOrderAndPayRequestBean.getPaymentInfo() != null)
                createOrderAndPayRequestBean.getPaymentInfo().setVerificationType(
                        verificationType == null ? null : verificationType.getType());
            EXT_LOGGER.customInfo("Verification Type is added in COP");

        }
        createOrderAndPayRequestBean.setDetailExtendInfo(workFlowRequestBean.getDetailExtendInfo());
        if (StringUtils.equals(OrderType.BRAND_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            createOrderAndPayRequestBean.getExtendInfo().setEmiCategory(BRAND_EMI);
        } else if (StringUtils.equals(OrderType.BANK_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            createOrderAndPayRequestBean.getExtendInfo().setEmiCategory(BANK_EMI);
        }
        if (flowTransBean.isAddMoneyPcfEnabled()) {
            if (bizPayOption2 != null) {
                Map<String, String> bizPayOption2ExtendInfo = new HashMap<>();
                bizPayOption2ExtendInfo = bizPayOption2.getExtendInfo();
                bizPayOption2ExtendInfo.put(ADD_MONEY_SURCHARGE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
                bizPayOption2.setExtendInfo(bizPayOption2ExtendInfo);
            } else if (bizPayOption1 != null) {
                Map<String, String> bizPayOption1ExtendInfo = new HashMap<>();
                bizPayOption1ExtendInfo = bizPayOption1.getExtendInfo();
                bizPayOption1ExtendInfo.put(ADD_MONEY_SURCHARGE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
                bizPayOption1.setExtendInfo(bizPayOption1ExtendInfo);
            }
            createOrderAndPayRequestBean.getExtendInfo().setThreePconvFee(ADD_MONEY_SURCHARGE_FLAG_TRUE);
            createOrderAndPayRequestBean.getExtendInfo().setThreePconvFeeAmount(
                    flowTransBean.getWorkFlowBean().getChargeAmount());
        }
        return createOrderAndPayRequestBean;
    }

    public void verifyProductCodeInPaymentInfo(BizPaymentInfo paymentInfo) {
        for (BizPayOptionBill payOptionBill : paymentInfo.getPayOptionBills()) {
            if (MapUtils.isNotEmpty(payOptionBill.getExtendInfo())) {
                String productCode = payOptionBill.getExtendInfo().get("productCode");
                if (ProductCodes.StandardAcquiringProdByJSChargePayee.getId().equals(productCode)) {
                    payOptionBill.getExtendInfo().put("productCode",
                            ProductCodes.StandardDirectPayAcquiringProd.getId());
                } else if (ProductCodes.StandardAcquiringProdByJSChargePayer.getId().equals(productCode)) {
                    payOptionBill.getExtendInfo().put("productCode",
                            ProductCodes.StandardDirectPayAcquiringProdChargePayer.getId());
                }
            }
        }
    }

    private void setPassThroughExtendInfoForUpiLite(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) {
        if (null == channelInfo) {
            return;
        }

        if (flowTransBean.getWorkFlowBean().getUpiLiteRequestData() == null)
            return;

        String encodedPassThrough = channelInfo.get(PASS_THROUGH_EXTEND_INFO_KEY);
        HashMap<String, String> passThroughChannelExtendedInfoMap = new HashMap<>();

        if (StringUtils.isNotBlank(encodedPassThrough)) {
            String passThroughJson = new String(Base64.getDecoder().decode(encodedPassThrough));
            if (StringUtils.isNotBlank(passThroughJson)) {
                try {
                    passThroughChannelExtendedInfoMap = (HashMap<String, String>) JsonMapper.mapJsonToObject(
                            passThroughJson, Map.class);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                }
            }
        }
        // This is to pass to instaproxy in case of UPI-LITE
        if (flowTransBean.getWorkFlowBean().getUpiLiteRequestData() != null) {
            try {
                passThroughChannelExtendedInfoMap.put(UPILITE_REQUEST_DATA,
                        JsonMapper.mapObjectToJson(flowTransBean.getWorkFlowBean().getUpiLiteRequestData()));
            } catch (Exception e) {
                LOGGER.warn("Upi Lite Data Parsing failed", e);
                return;
            }
        }
        setEncodedPassThroughExtendInfo(channelInfo, passThroughChannelExtendedInfoMap);
    }

    private boolean isPartiallyMigratedMerchantEligibleForPg2(WorkFlowRequestBean requestBean) {
        EPayMode paymentFlow = requestBean.getPaytmExpressAddOrHybrid();
        LOGGER.info("PAYMENT FLOW {}", paymentFlow);
        if ((paymentFlow == null || EPayMode.NONE.equals(paymentFlow))
                || ((EPayMode.ADDANDPAY.equals(paymentFlow) || EPayMode.ADDANDPAY_GV.equals(paymentFlow)) && ff4jUtils
                        .isFeatureEnabledOnMid(requestBean.getPaytmMID(), Ff4jFeature.ENABLE_PG2_ADD_N_PAY, false))) {
            return true;
        }
        return false;
    }

    private boolean isSeamlessEMIPayment(WorkFlowRequestBean workFlowBean) {
        return (ERequestType.SEAMLESS.equals(workFlowBean.getRequestType()) && PaymentTypeIdEnum.EMI.value
                .equals(workFlowBean.getPaymentTypeId()));
    }

    private EPayMethod getEPayMethod(WorkFlowTransactionBean flowTransBean) {
        return EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod());
    }

    private BizPayOptionBill getBizPayOptionBillForBankTransfer(WorkFlowTransactionBean flowTransBean,
            EPayMethod payMethod, Map<String, String> channelInfo, Map<String, String> extendInfo) {

        BizPayOptionBill bizPayOptionBill;
        final WorkFlowRequestBean workFlowBean = flowTransBean.getWorkFlowBean();

        bizPayOptionBill = new BizPayOptionBill(workFlowBean.getPayOption(), payMethod, workFlowBean.getTxnAmount(),
                "0");
        bizPayOptionBill.setChannelInfo(channelInfo);
        bizPayOptionBill.setExtendInfo(extendInfo);
        bizPayOptionBill.setCardCacheToken(flowTransBean.getCacheCardToken());
        LOGGER.info("WorkFlowBean: {}", workFlowBean);
        if (workFlowBean.getExtendedInfo() == null) {
            LOGGER.info("Extended Info is null");
        }
        if (workFlowBean.getExtendedInfo() != null && workFlowBean.getExtendedInfo().containsKey(VAN_INFO)
                && StringUtils.isEmpty(workFlowBean.getExtendedInfo().get(VAN_INFO)) == false) {
            // bank transfer phase 3
            try {
                VanInfo vanInfo = JsonMapper.mapJsonToObject(workFlowBean.getExtendedInfo().get(VAN_INFO),
                        VanInfo.class);
                if (null == vanInfo.getCheckoutFlow() || (flowTransBean.getConsultFeeResponse() != null)) {
                    if (flowTransBean.getWorkFlowBean().getFeesAmount() != null) {
                        bizPayOptionBill.setChargeAmount(AmountUtils.getTransactionAmountInPaise(flowTransBean
                                .getWorkFlowBean().getFeesAmount()));
                    } else if (null == flowTransBean.getWorkFlowBean().getFeesAmount()
                            && null != workFlowBean.getChannelInfo()
                            && null != workFlowBean.getChannelInfo().get("failure")) {
                        bizPayOptionBill.setChargeAmount(fetchChargeAmountIfApplicable(flowTransBean));
                    }
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception occurred while getting van info from request: {}", e);
            }
        }

        channelInfo = (null == channelInfo) ? new HashMap<>() : bizPayOptionBill.getChannelInfo();
        channelInfo.put("utr", workFlowBean.getChannelInfo().get("utr"));
        channelInfo.put("transactionRequestId", workFlowBean.getChannelInfo().get("transactionRequestId"));
        channelInfo.put("transactionDate", workFlowBean.getChannelInfo().get("transactionDate"));
        channelInfo.put("transferMode", workFlowBean.getChannelInfo().get("transferMode"));
        channelInfo.put("tpvErrorCode", workFlowBean.getChannelInfo().get("tpvErrorCode"));
        channelInfo.put("tpvDescription", workFlowBean.getChannelInfo().get("tpvDescription"));
        channelInfo.put("isTPVFailure", workFlowBean.getChannelInfo().get("isTPVFailure"));
        bizPayOptionBill.setChannelInfo(channelInfo);

        extendInfo = (null == extendInfo) ? new HashMap<>() : bizPayOptionBill.getExtendInfo();
        if (workFlowBean.getExtendedInfo() != null) {
            extendInfo.put("vanInfo", workFlowBean.getExtendedInfo().get("vanInfo"));
        }
        extendInfo.put("issuingBankId", workFlowBean.getBankCode());
        bizPayOptionBill.setExtendInfo(extendInfo);

        return bizPayOptionBill;
    }

    private void setUserInfoForRedemptionOf3PMerchant(BizPayOptionBill bizPayOption1,
            WorkFlowTransactionBean flowTransBean, BizCreateOrderRequest bizCreateOrderRequest,
            Map<String, String> extendInfo) throws MappingServiceClientException, BaseException {
        String midsforRedemption = ConfigurationUtil.getProperty("mids.loyaltyPointRedemption", null);
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getLoyaltyPointRootUserId())
                && StringUtils.isNotEmpty(midsforRedemption)) {
            String[] mids = midsforRedemption.split(BizConstant.COMMA);
            List<String> midList = Arrays.asList(mids);
            if (midList.contains(flowTransBean.getWorkFlowBean().getPaytmMID())) {
                // get payer account number for root user id
                UserInfo mappingUserData = userMappingService.getUserData(flowTransBean.getWorkFlowBean()
                        .getLoyaltyPointRootUserId(), UserOwner.PAYTM);
                if (mappingUserData != null) {
                    bizPayOption1.setPayerAccountNo(mappingUserData.getAlipayAccountId());
                    bizCreateOrderRequest.setInternalUserId(mappingUserData.getAlipayId());
                    bizCreateOrderRequest.setExternalUserId(mappingUserData.getPaytmId());
                    if (extendInfo.get(ExtendedInfoKeys.PAYTM_USER_ID) != null) {
                        extendInfo.put(ExtendedInfoKeys.PAYTM_USER_ID, mappingUserData.getPaytmId());
                    }
                } else {
                    LOGGER.error("User not Exist for loyalty Point Redemption");
                    throw new BaseException("User not Exist for loyalty Point Redemption");
                }
            } else {
                LOGGER.error("Mid not allowed for loyalty Point Redemption");
                throw new BaseException("Mid not allowed for loyalty Point Redemption");
            }
        }
    }

    private void setEncodedPassThrough(Map<String, String> channelInfo, Map<String, String> passThroughExtendInfo) {
        String passThroughJson = StringUtils.EMPTY;
        try {
            passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfo);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
        }
        String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
        channelInfo
                .put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
    }

    private void populatePayOptionBillForMandate(BizPayOptionBill bizPayOption1, Map<String, String> channelInfo,
            Map<String, String> extendInfo, WorkFlowTransactionBean flowTransBean,
            WorkFlowRequestBean workFlowRequestBean) {
        bizPayOption1.setPayMethod(EPayMethod.BANK_MANDATE);
        bizPayOption1.setPayOption(EPayMethod.BANK_MANDATE.name());
        bizPayOption1.setCardCacheToken(flowTransBean.getCacheCardToken());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.SUBSCRIPTION_ID, workFlowRequestBean.getSubscriptionID());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.ISSUING_BANK_NAME, workFlowRequestBean.getBankName());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.UMRN, workFlowRequestBean.getUmrn());
        extendInfo.put(TheiaConstant.ExtendedInfoPay.SUBSCRIPTION_ID, workFlowRequestBean.getSubscriptionID());
        extendInfo.put(TheiaConstant.ExtendedInfoPay.ISSUING_BANK_NAME, workFlowRequestBean.getBankName());
    }

    private void populatePayOptionBillForUPIRecurringMandate(BizPayOptionBill bizPayOption1,
            Map<String, String> channelInfo, Map<String, String> extendInfo, WorkFlowTransactionBean flowTransBean,
            WorkFlowRequestBean workFlowRequestBean) {
        bizPayOption1.setPayMethod(EPayMethod.UPI);
        bizPayOption1.setPayOption(workFlowRequestBean.getPayOption());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.SUBSCRIPTION_ID, workFlowRequestBean.getSubscriptionID());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.UMN, workFlowRequestBean.getUmn());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.VIRTUAL_PAYMENT_ADDRESS, workFlowRequestBean.getVpa());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.REF_SERVICE_INSTID, workFlowRequestBean.getRefServiceInstId());
        extendInfo.put(TheiaConstant.ExtendedInfoPay.SUBSCRIPTION_ID, workFlowRequestBean.getSubscriptionID());
    }

    private BizChannelPreference calculateChannelPreferenceForAoaMerchant(WorkFlowTransactionBean flowTransBean) {
        LOGGER.info("In calculateChannelPreferenceForAoaMerchant");
        Set<String> enableGateways = new HashSet<>();
        ManualRoutingConfigInfoResponse manualRoutingConfigInfoResponse;
        try {
            manualRoutingConfigInfoResponse = getManualRoutingConfigInfoResponse(flowTransBean);
            // LOGGER.info("manualRoutings from mapping : {}",
            // (manualRoutingConfigInfoResponse != null ?
            // manualRoutingConfigInfoResponse.getManualRoutings()
            // : null));
            LOGGER.info("manualRoutings from mapping : {}",
                    (manualRoutingConfigInfoResponse != null ? manualRoutingConfigInfoResponse.getManualRoutings()
                            : null));
            List<GatewayDTO> gatewayDTOList = manualRoutingDataService.getPaymentGatewayInfo();
            EXT_LOGGER.customInfo("Mapping response - GatewayDTOList :: {}", gatewayDTOList);
            for (GatewayDTO gatewayDTO : gatewayDTOList) {
                enableGateways.add(gatewayDTO.getGatewayName());
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while ChannelPreference fetching data from mapping {}", ex);
            return null;
        }
        if (manualRoutingConfigInfoResponse == null || manualRoutingConfigInfoResponse.getManualRoutings() == null
                || CollectionUtils.isEmpty(enableGateways)) {
            LOGGER.info("Skipping score calculation, mapping reponse {} {}", manualRoutingConfigInfoResponse,
                    enableGateways);
            return null;
        }
        Set<ManualRouting> gatewayScorePreferencesSet = manualRoutingConfigInfoResponse.getManualRoutings();
        Optional<BaseLine> baseLine = null;
        if (CollectionUtils.isNotEmpty(manualRoutingConfigInfoResponse.getBaseLines())) {
            baseLine = manualRoutingConfigInfoResponse.getBaseLines().stream().findFirst();
        }
        AOAGatewayScoreCalculator aoaGatewayScoreCalculator = new AOAGatewayScoreCalculator(enableGateways,
                gatewayScorePreferencesSet);
        List<BizPreferenceValue> bizPreferenceValues = aoaGatewayScoreCalculator.calculate();
        BizChannelPreference bizChannelPreference = new BizChannelPreference();
        bizChannelPreference.setPreferenceValues(bizPreferenceValues);
        Map<String, String> extendInfo = new HashMap<>();
        if (baseLine != null && baseLine.get() != null) {
            extendInfo.put(TheiaConstant.AOAConstants.BASE_LINE, baseLine.get().getBaseLine());
        }
        bizChannelPreference.setExtendInfo(extendInfo);
        return bizChannelPreference;
    }

    private ManualRoutingConfigInfoResponse getManualRoutingConfigInfoResponse(WorkFlowTransactionBean flowTransBean)
            throws MappingServiceClientException {
        ManualRoutingConfigInfoResponse manualRoutingConfigInfoResponse = null;
        String mid = "";
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getAlipayMID())) {
            mid = flowTransBean.getWorkFlowBean().getAlipayMID();
        }
        if (EPayMethod.NET_BANKING.equals(EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean()
                .getPayMethod()))) {
            manualRoutingConfigInfoResponse = manualRoutingDataService.getManualRoutingConfigByBankName(mid,
                    flowTransBean.getWorkFlowBean().getBankCode());
            EXT_LOGGER
                    .customInfo(
                            "Mapping response - ManualRoutingConfigInfoResponse by bank name :: {} for MID :: {} BankCode :: {}",
                            manualRoutingConfigInfoResponse, mid, flowTransBean.getWorkFlowBean().getBankCode());
        } else {
            String cardBin = "";
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardNo())) {
                cardBin = flowTransBean.getWorkFlowBean().getCardNo().substring(0, 6);
                manualRoutingConfigInfoResponse = manualRoutingDataService
                        .getManualRoutingConfigByCardBin(mid, cardBin);
                EXT_LOGGER
                        .customInfo(
                                "Mapping response - ManualRoutingConfigInfoResponse by card bin :: {} for MID :: {} BankCode :: {}",
                                manualRoutingConfigInfoResponse, mid, cardBin);
            }
        }
        return manualRoutingConfigInfoResponse;
    }

    private void setDirectPayMode(WorkFlowTransactionBean flowTransBean, Map<String, String> channelInfo) {
        if ((flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value) || flowTransBean
                .getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value))
                && DirectPaymentVerificationMethod.ATM.getValue().equals(
                        flowTransBean.getWorkFlowBean().getDirectPayModeType())) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
            channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                    DirectPaymentVerificationMethod.ATM.getValue());
        }
    }

    /**
     * populating the userdetails, mcc and ipaddr in the channelinfo
     *
     * @param flowTransBean
     * @return a map containing usermobile, useremail, mcc, and ipaddress
     */
    private void setPassThroughChannelExtendedInfo(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) {

        String channelPassThroughJson = StringUtils.EMPTY;
        Map<String, String> passThroughChannelExtendedInfo = null;

        passThroughChannelExtendedInfo = getPassThroughChannelExtendedInfoMap(flowTransBean);
        String paymentTypeId = flowTransBean.getWorkFlowBean().getPaymentTypeId();
        boolean isAddNPay = "1".equals(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getIsAddMoney());
        if (!isAddNPay
                && (PaymentTypeIdEnum.CC.value.equals(paymentTypeId) || PaymentTypeIdEnum.DC.value
                        .equals(paymentTypeId))) {
            passSiHubModeIfAvailable(channelInfo, flowTransBean.getSubscriptionServiceResponse());
        }
        passSiHubDetailsIfAvailable(channelInfo, passThroughChannelExtendedInfo,
                flowTransBean.getSubscriptionServiceResponse());
        passTwoFAConfigData(passThroughChannelExtendedInfo, flowTransBean);
        passDeviceBindingDetails(passThroughChannelExtendedInfo, flowTransBean);

        try {
            channelPassThroughJson = JsonMapper.mapObjectToJson(passThroughChannelExtendedInfo);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughChannelInfoMap {}", e);
        }
        String encodedChannelPassThrough = new String(Base64.getEncoder().encode(channelPassThroughJson.getBytes()));
        channelInfo.put(ExtendedInfoKeys.CHANNEL_INFO_PASS_THROUGH_KEY, encodedChannelPassThrough);
    }

    private void passDeviceBindingDetails(Map<String, String> passThroughChannelExtendedInfo,
            WorkFlowTransactionBean flowTransBean) {
        Map<String, String> queryParams = getQueryParams(flowTransBean.getWorkFlowBean().getQueryParams());
        if (MapUtils.isNotEmpty(queryParams) && StringUtils.isNotBlank(queryParams.get(DEVICE_IDENTIFIER))) {
            passThroughChannelExtendedInfo.put(FacadeConstants.DEVICE_ID_LONG, queryParams.get(DEVICE_IDENTIFIER));
        } else if (flowTransBean.getWorkFlowBean() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getDeviceId())) {
            passThroughChannelExtendedInfo.put(FacadeConstants.DEVICE_ID_LONG, flowTransBean.getWorkFlowBean()
                    .getDeviceId());
        }
        if (MapUtils.isNotEmpty(queryParams) && StringUtils.isNotBlank(queryParams.get(SIM_SUBSCRIPTION_ID))) {
            passThroughChannelExtendedInfo.put(SIM_SUBSCRIPTION_ID, queryParams.get(SIM_SUBSCRIPTION_ID));
        } else if (flowTransBean.getWorkFlowBean() != null
                && flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                        .getSimSubscriptionId())) {
            passThroughChannelExtendedInfo.put(SIM_SUBSCRIPTION_ID, flowTransBean.getWorkFlowBean()
                    .getPaymentRequestBean().getSimSubscriptionId());
        }
    }

    private void passTwoFAConfigData(Map<String, String> passThroughExtendInfo, WorkFlowTransactionBean flowTransBean) {
        boolean isAddNPayOrHybrid = "1".equals(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getIsAddMoney())
                || "0".equals(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getIsAddMoney());
        if ((EPayMethod.BALANCE.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod()) || isAddNPayOrHybrid)
                && flowTransBean.getWorkFlowBean().getTwoFAConfig() != null) {
            try {
                flowTransBean.getWorkFlowBean().getTwoFAConfig().setTwoFAPasscode(null);
                String twoFAConfigJson = JsonMapper.mapObjectToJson(flowTransBean.getWorkFlowBean().getTwoFAConfig());
                passThroughExtendInfo.put(TheiaConstant.RequestParams.TWO_FA_CONFIG_DATA, twoFAConfigJson);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while transforming twoFAConfigJson to string: ", e);
            }
        }
    }

    private void passSiHubDetailsIfAvailable(Map<String, String> channelInfo,
            Map<String, String> passThroughExtendInfo, SubscriptionResponse subscriptionResponse) {
        if (subscriptionResponse != null && subscriptionResponse.getSiHubDetails() != null) {
            channelInfo.put(SI_HUB_ID, subscriptionResponse.getSiHubDetails().getSiHubId());
            channelInfo.put(REF_SERVICE_INST_ID, subscriptionResponse.getSiHubDetails().getRefServiceInstId());
            channelInfo.put(SI_HUB_MODE, Boolean.TRUE.toString());
            passThroughExtendInfo.put(SI_ENTITY, subscriptionResponse.getSiHubDetails().getSiEntity());
            passThroughExtendInfo.put(SI_INVOICE_ID, subscriptionResponse.getSiHubDetails().getSiInvoiceId());
            passThroughExtendInfo.put(SUBS_FREQ_UNIT, subscriptionResponse.getSubsFreqUnit());
        }
    }

    private void passSiHubModeIfAvailable(Map<String, String> channelInfo, SubscriptionResponse subscriptionResponse) {
        if (subscriptionResponse != null && subscriptionResponse.isSiHubMode()) {
            channelInfo.put(SI_HUB_MODE, "true");
        }
    }

    private Map<String, String> getPassThroughChannelExtendedInfoMap(WorkFlowTransactionBean flowTransBean) {
        Map<String, String> passThroughChannelExtendedInfo = new HashMap<>();

        if (flowTransBean.getUserDetails() != null) {
            passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.CUSTOMER_PHONE_NO, flowTransBean
                    .getUserDetails().getMobileNo());
            passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.CUSTOMER_EMAIL_ID, flowTransBean
                    .getUserDetails().getEmail());
            passThroughChannelExtendedInfo.put(TheiaConstant.PaytmDigitalCreditConstant.CUSTOMER_ID, flowTransBean
                    .getUserDetails().getUserId());
        }
        if (flowTransBean.getWorkFlowBean() != null) {
            passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.MCC, flowTransBean.getWorkFlowBean()
                    .getIndustryTypeID());

            // This is to pass to instaproxy in case of Direct Bank request
            if (flowTransBean.getWorkFlowBean().getRequestType() == NATIVE
                    || flowTransBean.getWorkFlowBean().getRequestType() == DYNAMIC_QR
                    || flowTransBean.getWorkFlowBean().getRequestType() == DYNAMIC_QR_2FA) {
                passThroughChannelExtendedInfo.put(TXN_TOKEN, flowTransBean.getWorkFlowBean().getTxnToken());
            }

            /*
             * set authentication and networkTokenRequestId for EcomToken
             * Transactions
             */
            String authenticationValue = null;
            String networkTokenRequestorId = null;
            passThroughChannelExtendedInfo.put(IS_SAVED_CARD,
                    Boolean.toString(flowTransBean.getWorkFlowBean().getIsSavedCard()));

            try {
                getCustIdUserIdOnCoftConsentCondition(flowTransBean, passThroughChannelExtendedInfo);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while getting custId And UserId", e);
            }

            if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().getUserInfo() != null) {
                if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().getUserInfo().getFirstName() != null) {
                    passThroughChannelExtendedInfo.put(FIRST_NAME, flowTransBean.getWorkFlowBean()
                            .getPaymentRequestBean().getUserInfo().getFirstName());
                }
                if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().getUserInfo().getLastName() != null) {
                    passThroughChannelExtendedInfo.put(LAST_NAME, flowTransBean.getWorkFlowBean()
                            .getPaymentRequestBean().getUserInfo().getLastName());
                }
                if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().getUserInfo().getMobile() != null) {
                    passThroughChannelExtendedInfo.put(CommonConstants.MOBILE_NO, flowTransBean.getWorkFlowBean()
                            .getPaymentRequestBean().getUserInfo().getMobile());
                }
            }
            if (flowTransBean.getWorkFlowBean().getCoftConsent() != null) {
                String coftConsentRedisKey = setRedisKeyForCoftConsent(flowTransBean.getWorkFlowBean());
                LOGGER.info("Redis Key for Single Consent : {}", coftConsentRedisKey);
                passThroughChannelExtendedInfo.put(COFT_CONSENT_CACHE_KEY, coftConsentRedisKey);

                // todo add latestTimeStamp for tokenized payments
                // final boolean storeCard =
                // ((flowTransBean.getWorkFlowBean().getStoreCard() != null) &&
                // "1".equals(flowTransBean
                // .getWorkFlowBean().getStoreCard().trim())) ? true : false;
                if (flowTransBean.getWorkFlowBean().getCoftConsent().getUserConsent() == 1) {
                    String userId = flowTransBean.getUserDetails() != null ? flowTransBean.getUserDetails().getUserId()
                            : null;
                    if (!StringUtils.equals(flowTransBean.getWorkFlowBean().getCustID(), userId)) {
                        passThroughChannelExtendedInfo.put(SAVE_ASSET_FOR_MERCHANT,
                                Boolean.toString(flowTransBean.getWorkFlowBean().isStoreCardPrefEnabled()));
                    }
                    if (flowTransBean.getUserDetails() != null
                            && StringUtils.isNotEmpty(flowTransBean.getUserDetails().getUserId())) {
                        passThroughChannelExtendedInfo.put(SAVE_ASSET_FOR_USER, "true");
                    }
                }
            }
            // TODO remove later as hardcoded for pre consent
            String coftConsentRedisKeyDirect = setRedisKeyForCoftConsentDirect(flowTransBean.getWorkFlowBean());
            LOGGER.info("Redis Key for Single Consent direct:", coftConsentRedisKeyDirect);
            passThroughChannelExtendedInfo.put(COFT_CONSENT_CACHE_KEY_DIRECT, coftConsentRedisKeyDirect);

            if (flowTransBean.getWorkFlowBean().isCoftTokenTxn()) {
                if (DINERS.equals(flowTransBean.getWorkFlowBean().getCardScheme())) {
                    EXT_LOGGER
                            .customInfo("For DINERS scheme, setting TOKEN_UNIQUE_REFERENCE & TOKEN_REQUESTOR_ID in directPassThroughInfo");
                    passThroughChannelExtendedInfo.put(TOKEN_UNIQUE_REFERENCE, flowTransBean.getWorkFlowBean()
                            .getCardTokenInfo().getTokenUniqueReference());
                    passThroughChannelExtendedInfo.put(TOKEN_REQUESTOR_ID, flowTransBean.getWorkFlowBean()
                            .getCardTokenInfo().getMerchantTokenRequestorId());
                } else {
                    passThroughChannelExtendedInfo.put(TAVV, flowTransBean.getWorkFlowBean().getCardTokenInfo()
                            .getTAVV());
                }
                if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()) {
                    // For DINERS scheme card - We will NOT set PAR in
                    // directPassThroughInfo
                    if (!StringUtils.equals(DINERS, flowTransBean.getWorkFlowBean().getCardScheme())) {
                        passThroughChannelExtendedInfo.put(PAN_UNIQUE_REFERENCE, flowTransBean.getWorkFlowBean()
                                .getCardTokenInfo().getPanUniqueReference());
                    }
                    passThroughChannelExtendedInfo.put(CARD_SUFFIX, flowTransBean.getWorkFlowBean().getCardTokenInfo()
                            .getCardSuffix());
                }
            }
            passThroughChannelExtendedInfo.put(BUSINESS_FLOW_TYPE, BusinessFlowType.getBusinessType(flowTransBean
                    .getWorkFlowBean().getWorkFlow(), flowTransBean.getWorkFlowBean().isNativeJsonRequest()));
            if (flowTransBean.getWorkFlowBean().isEcomTokenTxn()) {
                try {
                    EcomTokenInfo ecomTokenInfo = JsonMapper.mapJsonToObject(flowTransBean.getWorkFlowBean()
                            .getPaymentDetails(), EcomTokenInfo.class);
                    authenticationValue = ecomTokenInfo.getAuthenticationValue();
                    networkTokenRequestorId = ecomTokenInfo.getNetworkTokenRequestorId();
                } catch (Exception e) {
                    LOGGER.error("Invalid EcomTokenInfo {}", flowTransBean.getWorkFlowBean().getPaymentDetails());
                }

                LOGGER.info("authenticationValue is {} and networkTokenRequestorId {}for eComTokenTransaction ",
                        authenticationValue, networkTokenRequestorId);
                passThroughChannelExtendedInfo.put(AUTHENTICATION_VALUE, authenticationValue);
                passThroughChannelExtendedInfo.put(NETWORK_TOKEN_REQUESTOR_ID, networkTokenRequestorId);
            }

            if (flowTransBean.getWorkFlowBean().getOneClickInfo() != null) {
                String redisKey = setRedisKeyForOneClickInfo(flowTransBean.getWorkFlowBean());
                LOGGER.info("redisKey is " + redisKey);
                passThroughChannelExtendedInfo.put(ONE_CLICK_INFO_REDIS_KEY, redisKey);
            }

            if (flowTransBean.getSubscriptionServiceResponse() != null) {
                Integer totalPayCount = flowTransBean.getSubscriptionServiceResponse().getTotalPaymentCount();
                passThroughChannelExtendedInfo.put(TOTAL_PAYMENT_COUNT, totalPayCount.toString());
            }

            if (flowTransBean.getSubscriptionServiceResponse() != null
                    && flowTransBean.getSubscriptionServiceResponse().isSiHubMode()) {
                StringBuilder redisKey = new StringBuilder(flowTransBean.getWorkFlowBean().getRequestType().getType())
                        .append(flowTransBean.getWorkFlowBean().getTxnToken());
                passThroughChannelExtendedInfo.put(SUBS_RESPONSE_REDIS_KEY, redisKey.toString());
            }

            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getKycCode())) {
                passThroughChannelExtendedInfo.put(TheiaConstant.PaytmDigitalCreditConstant.KYC_CODE, flowTransBean
                        .getWorkFlowBean().getKycCode());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getKycVersion())) {
                passThroughChannelExtendedInfo.put(TheiaConstant.PaytmDigitalCreditConstant.KYC_VERSION, flowTransBean
                        .getWorkFlowBean().getKycVersion());
            }
            if (null != flowTransBean.getWorkFlowBean().getExtendInfo()) {
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName())) {
                    passThroughChannelExtendedInfo.put(TheiaConstant.PaytmDigitalCreditConstant.MERCHANT_DISPLAY_NAME,
                            flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName());
                }
                String merchantType = TheiaConstant.PaytmDigitalCreditConstant.MERCHANT_TYPE_OFF_US;
                if (flowTransBean.getWorkFlowBean().getExtendInfo().isMerchantOnPaytm()) {
                    merchantType = TheiaConstant.PaytmDigitalCreditConstant.MERCHANT_TYPE_ON_US;
                }
                passThroughChannelExtendedInfo
                        .put(TheiaConstant.PaytmDigitalCreditConstant.MERCHANT_TYPE, merchantType);
            }
            if (null != flowTransBean.getWorkFlowBean().getEnvInfoReqBean()) {
                passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.IP_ADDRESS, flowTransBean
                        .getWorkFlowBean().getEnvInfoReqBean().getClientIp());
            }
            if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                    && !flowTransBean.getWorkFlowBean().isUpiPushFlow()
                    && !flowTransBean.getWorkFlowBean().isNativeDeepLinkReqd()) {
                passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.PAYMENT_TIMEOUT_UPI_MINS,
                        flowTransBean.getWorkFlowBean().getPaymentTimeoutInMinsForUpi());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getMerchantLogo())) {
                passThroughChannelExtendedInfo.put(MERCHANT_LOGO, flowTransBean.getWorkFlowBean().getMerchantLogo());
            }
            if (flowTransBean.getWorkFlowBean() != null) {
                passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.IS_OFFLINE_TXN, flowTransBean
                        .getWorkFlowBean().getIsOfflineTxn());
            }
            setChannelInfoForNativeJsonRequest(flowTransBean, passThroughChannelExtendedInfo);

            if (flowTransBean.getWorkFlowBean() != null
                    && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getFromAOARequest())
                    && flowTransBean.getWorkFlowBean().getFromAOARequest().equalsIgnoreCase("true")) {
                passThroughChannelExtendedInfo.put(FROMAOAREQUEST, flowTransBean.getWorkFlowBean().getFromAOARequest());
            }

            LOGGER.info("Adding paytmMerchantId:{} ,paytmMerchantOrderId:{} in directPassThroughInfo", flowTransBean
                    .getWorkFlowBean().getPaytmMID(), flowTransBean.getWorkFlowBean().getOrderID());
            passThroughChannelExtendedInfo.put(TheiaConstant.ExtraConstants.PAYTM_MID, flowTransBean.getWorkFlowBean()
                    .getPaytmMID());
            passThroughChannelExtendedInfo.put(TheiaConstant.ExtraConstants.PAYTM_MERCHANT_ORDER_ID, flowTransBean
                    .getWorkFlowBean().getOrderID());
            if (PaymentTypeIdEnum.LOYALTY_POINT.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
                populateLoyaltyPointData(passThroughChannelExtendedInfo, flowTransBean);
            }

            if ("true".equals(flowTransBean.getWorkFlowBean().getFromAOARequest())) {
                passThroughChannelExtendedInfo.put("flowType", "AOA_DQR");
            }
            if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()) {
                passThroughChannelExtendedInfo.put(TheiaConstant.ExtraConstants.DEVICE_ID, flowTransBean
                        .getWorkFlowBean().getDeviceId());
                passThroughChannelExtendedInfo.put(BizConstant.SSO_TOKEN, flowTransBean.getWorkFlowBean().getToken());
                passThroughChannelExtendedInfo.put("AOA_DATA", fetchAOADataKey(flowTransBean.getWorkFlowBean()));
            }
            if (flowTransBean.getWorkFlowBean().isSubsAoaPgMidTxn()) {
                passThroughChannelExtendedInfo.put(TheiaConstant.ExtraConstants.SUB_AOA_PGMID_TXN,
                        String.valueOf(flowTransBean.getWorkFlowBean().isSubsAoaPgMidTxn()));
            }
            // TODO : Reverting changes for PGP-37201 - Until LMS contract
            // changes are live in prod.
            // if (Objects.nonNull(flowTransBean.getWorkFlowBean()) &&
            // PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId()))
            // {
            //
            // if
            // (Objects.nonNull(flowTransBean.getWorkFlowBean().getPaymentRequestBean())
            // &&
            // "1".equals(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getIsAddMoney()))
            // {
            //
            // passThroughChannelExtendedInfo.put(TheiaConstant.ExtendedInfoPay.TXN_TYPE,
            // "ADDNPAY");
            // }
            // }

            if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()
                    && ERequestType.isSubscriptionCreationRequest(flowTransBean.getWorkFlowBean().getRequestType()
                            .getType())
                    && Double.parseDouble(flowTransBean.getWorkFlowBean().getSubsOriginalAmount()) == Double
                            .parseDouble("0")) {
                passThroughChannelExtendedInfo.put("isZeroINRSubscription", "true");
            }
        }
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPpblAccountType())) {
            passThroughChannelExtendedInfo.put(accountType, flowTransBean.getWorkFlowBean().getPpblAccountType());
        }
        /*
         * In case of EMI_DC, CUSTOMER_PHONE_NO is populated using customer info
         * passed in Initiate TXN request.
         */
        if (EPayMethod.EMI_DC.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod())
                && flowTransBean.getWorkFlowBean().getPaymentRequestBean().getMobileNo() != null) {
            passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.CUSTOMER_PHONE_NO, flowTransBean
                    .getWorkFlowBean().getPaymentRequestBean().getMobileNo());
        }

        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().isVariableLengthOtpSupported() != null) {
            passThroughChannelExtendedInfo.put(
                    VARIABLE_LENGTH_OTP_SUPPORTED,
                    String.valueOf(flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                            .isVariableLengthOtpSupported()));
        }
        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean().isDeepLinkRequired()
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getUpiIntentAllowedRetries())) {
            passThroughChannelExtendedInfo.put(MerchantExtendInfoKeys.UPI_INTENT_ALLOWED_RETRIES_ON_PAYTM,
                    String.valueOf(flowTransBean.getWorkFlowBean().getUpiIntentAllowedRetries()));
        }

        return passThroughChannelExtendedInfo;
    }

    private void setPassThroughExtendInfoForUpiCollect(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) {
        Map<String, String> passThroughChannelExtendedInfo = new HashMap<>();
        String channelPassThroughJson = StringUtils.EMPTY;
        passThroughChannelExtendedInfo.put(ExtendedInfoKeys.PassThroughKeys.PAYMENT_TIMEOUT_UPI_MINS, flowTransBean
                .getWorkFlowBean().getPaymentTimeoutInMinsForUpi());
        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getMerchantVpa())) {
            passThroughChannelExtendedInfo.put(ExtendedInfoKeys.MERCHANT_VPA, flowTransBean.getWorkFlowBean()
                    .getPaymentRequestBean().getMerchantVpa());
        }
        setChannelInfoForNativeJsonRequest(flowTransBean, passThroughChannelExtendedInfo);

        if (flowTransBean.getUserDetails() != null) {
            passThroughChannelExtendedInfo.put(REQ_MOBILE_NO, flowTransBean.getUserDetails().getMobileNo());
            passThroughChannelExtendedInfo.put(CUST_ID, flowTransBean.getUserDetails().getUserId());
        }

        setPassThroughExtendInfoForOfflineUPIInMap(flowTransBean, passThroughChannelExtendedInfo);

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPreAuthExpiryDate())) {
            passThroughChannelExtendedInfo.put(FacadeConstants.VALIDITY_END_DATE, flowTransBean.getWorkFlowBean()
                    .getPreAuthExpiryDate());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubscriptionID())) {
            passThroughChannelExtendedInfo.put(FacadeConstants.UPI_RECURRING_KEY, "UPI_SUBSCRIPTION_"
                    + flowTransBean.getWorkFlowBean().getPaytmMID() + "_"
                    + flowTransBean.getWorkFlowBean().getOrderID());

            if (flowTransBean.getWorkFlowBean().getTxnToken() != null) {
                String payerVpa = (String) theiaSessionRedisUtil.hget(flowTransBean.getWorkFlowBean().getTxnToken(),
                        PAYER_NAME);
                passThroughChannelExtendedInfo.put(PAYER_NAME, payerVpa);
            }
        }
        if (flowTransBean != null || flowTransBean.getWorkFlowBean() != null
                || flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                || flowTransBean.getWorkFlowBean().getPaymentRequestBean().getPayerCmid() != null) {
            passThroughChannelExtendedInfo.put(PAYER_CMID, flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                    .getPayerCmid());
            // Additionally adding Payer CMID to channelInfo for analytics
            // purpose.
            channelInfo.put(PAYER_CMID, flowTransBean.getWorkFlowBean().getPaymentRequestBean().getPayerCmid());
        }
        try {
            channelPassThroughJson = JsonMapper.mapObjectToJson(passThroughChannelExtendedInfo);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughChannelInfoMap {}", e);
        }
        String encodedChannelPassThrough = new String(Base64.getEncoder().encode(channelPassThroughJson.getBytes()));
        channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedChannelPassThrough);
    }

    private void setPassThroughExtendInfoForOfflineUPIInMap(WorkFlowTransactionBean flowTransBean,
            Map<String, String> passThroughChannelExtendedInfoMap) {

        WorkFlowRequestBean workFlowBean = flowTransBean.getWorkFlowBean();

        if (isOffline(workFlowBean)) {
            LOGGER.info("OFFLINE FLAG is set to: Y.");
            passThroughChannelExtendedInfoMap.put(TheiaConstant.OFFLINE, "Y");
        } else {
            LOGGER.info("OFFLINE FLAG is set to: N.");
            passThroughChannelExtendedInfoMap.put(TheiaConstant.OFFLINE, "N");
        }
    }

    private void setPassThroughExtendInfoForOfflineUPI(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) {

        if (null == channelInfo) {
            return;
        }

        String encodedPassThrough = channelInfo.get(PASS_THROUGH_EXTEND_INFO_KEY);
        HashMap<String, String> passThroughChannelExtendedInfoMap = new HashMap<>();

        if (StringUtils.isNotBlank(encodedPassThrough)) {
            String passThroughJson = new String(Base64.getDecoder().decode(encodedPassThrough));
            if (StringUtils.isNotBlank(passThroughJson)) {
                try {
                    passThroughChannelExtendedInfoMap = (HashMap<String, String>) JsonMapper.mapJsonToObject(
                            passThroughJson, Map.class);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                }
            }
        }

        if (passThroughChannelExtendedInfoMap.containsKey(TheiaConstant.OFFLINE)) {
            return;
        }

        WorkFlowRequestBean workFlowBean = flowTransBean.getWorkFlowBean();

        if (isOffline(workFlowBean)) {
            passThroughChannelExtendedInfoMap.put(TheiaConstant.OFFLINE, "Y");
            LOGGER.info("OFFLINE Flag is set to: Y.");
        } else {
            passThroughChannelExtendedInfoMap.put(TheiaConstant.OFFLINE, "N");
            LOGGER.info("OFFLINE Flag is set to: N.");
        }
        setEncodedPassThroughExtendInfo(channelInfo, passThroughChannelExtendedInfoMap);
    }

    private boolean isOffline(WorkFlowRequestBean workFlowBean) {
        return (StringUtils.equals(ERequestType.OFFLINE.getType(), workFlowBean.getRequestType().getType())
                || workFlowBean.isOfflineFastForwardRequest() || workFlowBean.isOfflineFlow() || StringUtils.equals(
                ERequestType.SEAMLESS_3D_FORM.getType(), workFlowBean.getRequestType().getType()));
    }

    private void setEncodedPassThroughExtendInfo(Map<String, String> channelInfo,
            HashMap<String, String> passThroughChannelExtendedInfoMap) {
        String passThroughJson = StringUtils.EMPTY;

        try {
            passThroughJson = JsonMapper.mapObjectToJson(passThroughChannelExtendedInfoMap);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
        }
        String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
        channelInfo
                .put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
    }

    private boolean isDirectChannel(WorkFlowTransactionBean flowTransBean) {
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBizBean = workFlowHelper
                .getLitePayViewConsultResponse(flowTransBean.getWorkFlowBean().getPaytmMID());

        if (flowTransBean.getWorkFlowBean().isNativeAddMoney()) {
            litePayviewConsultResponseBizBean = workFlowHelper.getAddAndPayLitePayviewConsultResponse();
        }

        String bankCode = flowTransBean.getWorkFlowBean().getInstId();
        String cardType = flowTransBean.getWorkFlowBean().getCardType();
        if (flowTransBean.getSavedCard() == null) {
            LOGGER.warn("Parameter missing for direct channel payments");
            return false;
        }
        String cardInstId = flowTransBean.getSavedCard().getCardScheme();

        if (StringUtils.isEmpty(bankCode) || StringUtils.isEmpty(cardType) || StringUtils.isEmpty(cardInstId)) {
            LOGGER.warn("Parameter missing for direct channel payments");
            return false;
        }

        for (DirectChannelBank directChannel : DirectChannelBank.values()) {
            if (!bankCode.equals(directChannel.getBankCode()) || litePayviewConsultResponseBizBean == null) {
                continue;
            }

            for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getResponse()
                    .getPayMethodViews()) {
                if (!cardType.equals(payMethodViewsBiz.getPayMethod())) {
                    continue;
                }

                for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                    if (payChannelOptionViewBiz != null
                            && CollectionUtils.isNotEmpty(payChannelOptionViewBiz.getDirectServiceInsts())
                            && cardInstId.equals(payChannelOptionViewBiz.getInstId())
                            && payChannelOptionViewBiz.getDirectServiceInsts().contains(directChannel.toString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String setRedisKeyForOneClickInfo(WorkFlowRequestBean workFlowRequestBean) {
        String mid = workFlowRequestBean.getPaytmMID();
        String orderId = workFlowRequestBean.getOrderID();
        SecureRandom secureRandom = new SecureRandom();
        String random = String.valueOf((1 + (int) (secureRandom.nextDouble() * 100000)));
        String rediskey = ONE_CLICK_INFO_REDIS_KEY + "_" + mid + "_" + orderId + "_" + random;
        redisUtil.pushOneClickInfoForPaymentRequest(rediskey, workFlowRequestBean.getOneClickInfo());
        return rediskey;
    }

    private String setRedisKeyForCoftConsent(WorkFlowRequestBean workFlowRequestBean) {
        String mid = workFlowRequestBean.getPaytmMID();
        String orderId = workFlowRequestBean.getOrderID();
        if (StringUtils.isBlank(workFlowRequestBean.getCoftConsent().getCreatedAt())) {
            workFlowRequestBean.getCoftConsent().setCreatedAt(new Date(System.currentTimeMillis()).toString());
        }
        String rediskey = COFT_CONSENT_CACHE_KEY + "_" + mid + "_" + orderId;
        redisUtil.pushCoftConsentForPaymentRequest(rediskey, workFlowRequestBean.getCoftConsent());
        return rediskey;
    }

    // TODO remove later as hardcoded for pre consent
    private String setRedisKeyForCoftConsentDirect(WorkFlowRequestBean workFlowRequestBean) {
        CoftConsent coftConsent = new CoftConsent();
        coftConsent.setUserConsent(0);
        coftConsent.setCreatedAt(new Date(System.currentTimeMillis()).toString());
        if (workFlowRequestBean.getEnvInfoReqBean() != null) {
            coftConsent.setDeviceId(workFlowRequestBean.getEnvInfoReqBean().getDeviceId());
            coftConsent.setAppVersion(workFlowRequestBean.getEnvInfoReqBean().getAppVersion());
            coftConsent.setDeviceName(workFlowRequestBean.getEnvInfoReqBean().getDeviceType());
            coftConsent.setIp(workFlowRequestBean.getEnvInfoReqBean().getClientIp());
            coftConsent.setLanguage(workFlowRequestBean.getEnvInfoReqBean().getLanguage());
            coftConsent.setLocale(workFlowRequestBean.getEnvInfoReqBean().getWebsiteLanguage());
            coftConsent.setOs(workFlowRequestBean.getEnvInfoReqBean().getOsType());
            coftConsent.setPlatform(workFlowRequestBean.getEnvInfoReqBean().getPlatform());
        }
        workFlowRequestBean.setCoftConsent(coftConsent);
        String mid = workFlowRequestBean.getPaytmMID();
        String orderId = workFlowRequestBean.getOrderID();
        String rediskey = COFT_CONSENT_CACHE_KEY_DIRECT + "_" + mid + "_" + orderId;
        redisUtil.pushCoftConsentForPaymentRequest(rediskey, coftConsent);
        return rediskey;
    }

    private void enrichInExtendInfoForSeamlessPayment(WorkFlowTransactionBean flowTransBean) {
        WorkFlowRequestBean workFlowRequestBean = flowTransBean.getWorkFlowBean();
        if (workFlowRequestBean == null || workFlowRequestBean.getExtendInfo() == null) {
            return;
        }
        ExtendedInfoRequestBean extendedInfoRequestBean = workFlowRequestBean.getExtendInfo();

        extendedInfoRequestBean.setIssuingBankId(workFlowRequestBean.getInstId());
        extendedInfoRequestBean.setIssuingBankName(workFlowRequestBean.getBankName());

        UserDetailsBiz userDetailsBiz = flowTransBean.getUserDetails();
        if (userDetailsBiz != null) {
            extendedInfoRequestBean.setUserMobile(userDetailsBiz.getMobileNo());
            extendedInfoRequestBean.setUserEmail(userDetailsBiz.getEmail());
            extendedInfoRequestBean.setPaytmUserId(userDetailsBiz.getUserId());
            if (StringUtils.isBlank(workFlowRequestBean.getCustID())) {
                workFlowRequestBean.setCustID(userDetailsBiz.getUserId());
            }
        }
        extendedInfoRequestBean.setCustID(workFlowRequestBean.getCustID());

        if ((ERequestType.SEAMLESS.equals(flowTransBean.getWorkFlowBean().getRequestType()) || (SEAMLESS_NB
                .equals(flowTransBean.getWorkFlowBean().getRequestType())))
                && flowTransBean.getConsultFeeResponse() != null) {
            String chargeAmount = fetchChargeAmountIfApplicable(flowTransBean);
            flowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);
        }

    }

    public QueryPayResultRequestBean createQueryPayResultRequestBean(final WorkFlowTransactionBean flowTransBean) {
        return new QueryPayResultRequestBean(flowTransBean.getCashierRequestId(), flowTransBean.getWorkFlowBean()
                .isFromAoaMerchant(), getRoute(flowTransBean, "payResultQuery"));
    }

    public UPIPushInitiateRequestBean createUpiPushInitiateRequestBean(final WorkFlowTransactionBean flowTransBean) {
        WorkFlowRequestBean flowRequestBean = flowTransBean.getWorkFlowBean();
        UPIPushTxnRequestParams txnRequestParams = new UPIPushTxnRequestParams();

        MerchantVpaTxnInfo merchantVpaTxnInfo = getVpaInfoFromWebFormContext(flowTransBean.getQueryPaymentStatus()
                .getWebFormContext());

        LOGGER.info("MerchantVpaTxnInfo : {}", merchantVpaTxnInfo);

        txnRequestParams.setAccountNumber(flowRequestBean.getAccountNumber());
        txnRequestParams.setCredBlock(flowRequestBean.getCreditBlock());
        txnRequestParams.setBankName(flowRequestBean.getBank());
        txnRequestParams.setMobileNo(flowTransBean.getUserDetails().getMobileNo());
        txnRequestParams.setMpin(flowRequestBean.getMpin());
        txnRequestParams.setPayerVpa(flowRequestBean.getVirtualPaymentAddress());
        if (StringUtils.isNotBlank(flowRequestBean.getAppId())) {
            txnRequestParams.setAppId(flowRequestBean.getAppId());
        }

        if (merchantVpaTxnInfo != null) {
            txnRequestParams.setExternalSrNo(merchantVpaTxnInfo.getExternalSrNo());
        }

        txnRequestParams.setDeviceId(flowRequestBean.getDeviceId());
        txnRequestParams.setSeqNo(flowRequestBean.getSeqNo());

        final UPIPushInitiateRequestBean initiateRequestBean = new UPIPushInitiateRequestBean(txnRequestParams,
                flowRequestBean.getTxnAmount());

        if (merchantVpaTxnInfo != null) {
            initiateRequestBean.setRequestUrl(merchantVpaTxnInfo.getRU());
            initiateRequestBean.setExternalSrNo(merchantVpaTxnInfo.getExternalSrNo());
        }

        LOGGER.info("UPIPushInitiateRequestBean : {}", initiateRequestBean);

        return initiateRequestBean;
    }

    public QueryByAcquirementIdRequestBean createqueryByAcquirementIdRequestBean(
            final WorkFlowTransactionBean flowTransBean) {
        final QueryByAcquirementIdRequestBean queryByAcquirementIdRequestBean = new QueryByAcquirementIdRequestBean();
        queryByAcquirementIdRequestBean.setAcquirementId(flowTransBean.getTransID());
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getDummyAlipayMid())) {
            queryByAcquirementIdRequestBean.setMerchantId(flowTransBean.getWorkFlowBean().getDummyAlipayMid());
        } else {
            queryByAcquirementIdRequestBean.setMerchantId(flowTransBean.getWorkFlowBean().getAlipayMID());
        }
        queryByAcquirementIdRequestBean.setFromAoaMerchant(flowTransBean.getWorkFlowBean().isFromAoaMerchant());
        queryByAcquirementIdRequestBean.setRoutes(getRoute(flowTransBean, "queryByAcquirementId"));

        queryByAcquirementIdRequestBean.setPaytmMerchantId(flowTransBean.getWorkFlowBean().getPaytmMID());
        return queryByAcquirementIdRequestBean;
    }

    public ConsultPayViewRequestBizBean createConsultRequestBean(final WorkFlowTransactionBean flowTransBean) {
        return createConsultRequestBean(flowTransBean, false);
    }

    /*
     * If addAndPay True then , we'l send product code different(AddAndPay).
     */
    public ConsultPayViewRequestBizBean createConsultRequestBean(final WorkFlowTransactionBean flowTransBean,
            final boolean isAddAndPay) {
        String payerUserID = flowTransBean.getUserDetails() != null ? flowTransBean.getUserDetails()
                .getInternalUserId() : null;
        String transID = flowTransBean.getTransID();
        ETransType transType = flowTransBean.getWorkFlowBean().getTransType();
        ETerminalType terminalType = flowTransBean.getWorkFlowBean().getTerminalType();
        EnvInfoRequestBean envInfoRequestBean = flowTransBean.getWorkFlowBean().getEnvInfoReqBean();
        ERequestType requestTypeForProductCode = isAddAndPay ? ERequestType.ADD_AND_PAY : flowTransBean
                .getWorkFlowBean().getRequestType();
        Map<String, String> riskExtendInfo = AlipayRequestUtils.selectRiskExtendedInfo(flowTransBean.getUserDetails());

        // both parameters are related for postpaid checkbalance flow via
        // instaProxy
        if (null != flowTransBean.getUserDetails() && null != flowTransBean.getWorkFlowBean().getExtendInfo()) {
            flowTransBean.getWorkFlowBean().getExtendInfo().setPaytmUserId(flowTransBean.getUserDetails().getUserId());
            flowTransBean.getWorkFlowBean().getExtendInfo()
                    .setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        }

        List<String> exclusionPayMethods = new ArrayList<String>();
        if (flowTransBean.getUserDetails() != null && !flowTransBean.getUserDetails().isPaytmCCEnabled()) {
            LOGGER.debug("PAYTM_DIGITAL_CREDIT disabled due to user is null or feature not enabled");
            exclusionPayMethods.add(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        } else if (isAddAndPay && !flowTransBean.getWorkFlowBean().isPostPaidOnAddnPay()) {
            LOGGER.info("PAYTM_DIGITAL_CREDIT disabled due to ADD_AND_PAY and pospaid is enabled on this merchnt");
            exclusionPayMethods.add(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        }
        /* Check if user is eligible for Paytm CC */

        final ConsultPayViewRequestBizBean consultRequest = new ConsultPayViewRequestBizBean(payerUserID, transID,
                transType, requestTypeForProductCode, terminalType, envInfoRequestBean, riskExtendInfo,
                AlipayRequestUtils.getExtendeInfoMap(flowTransBean.getWorkFlowBean().getExtendInfo()),
                exclusionPayMethods);
        consultRequest.setPostConvenienceFee(flowTransBean.isPostConvenienceFeeModel());
        consultRequest.setDynamicFeeMerchant(flowTransBean.getWorkFlowBean().isDynamicFeeMerchant());

        String token = null;

        if (flowTransBean.getUserDetails() != null) {
            token = StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getToken()) ? flowTransBean
                    .getWorkFlowBean().getToken() : (StringUtils.isNotBlank(flowTransBean.getUserDetails()
                    .getUserToken()) ? flowTransBean.getUserDetails().getUserToken() : null);
        }

        if (StringUtils.isNotBlank(token)) {
            putSubWalletDetails(flowTransBean, consultRequest.getExtendInfo());
        }

        if (exclusionPayMethods.isEmpty() && (flowTransBean.getUserDetails() != null)) {

            if (StringUtils.isNotBlank(token)) {
                Map<String, String> extendInfo = new HashMap<String, String>();
                if (consultRequest.getExtendInfo() != null) {
                    extendInfo = consultRequest.getExtendInfo();
                }
                consultRequest.setExtendInfo(extendInfo);
            }
        }

        LOGGER.debug("Created request for Consult payview as ::{}", consultRequest);
        return consultRequest;
    }

    public LitePayviewConsultRequestBizBean createLitePayviewConsultRequestBean(
            final WorkFlowTransactionBean flowTransBean) {
        return createLitePayviewConsultRequestBean(flowTransBean, false);
    }

    public LitePayviewConsultRequestBizBean createLitePayviewConsultRequestBean(
            final WorkFlowTransactionBean flowTransBean, final boolean isAddAndPay) {
        String payerUserID = null;
        String paytmUserId = null;
        String pwpCategory = null;

        if (!isCachingConfiguredForOfflineFlow(flowTransBean) && flowTransBean.getUserDetails() != null) {
            payerUserID = flowTransBean.getUserDetails().getInternalUserId();
            paytmUserId = flowTransBean.getUserDetails().getUserId();
        }
        // for flipkart flow since we are caching so we dont want to send user
        // id in LPV request
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getAccessToken())
                && ff4jUtils.featureEnabledOnMultipleKeys(flowTransBean.getWorkFlowBean().getPaytmMID(), payerUserID,
                        Ff4jFeature.BLACKLIST_LPV_FPOV2_WITH_ACCESS_TOKEN, false)) {
            payerUserID = null;
            paytmUserId = null;
        }

        EnvInfoRequestBean envInfoRequestBean = flowTransBean.getWorkFlowBean().getEnvInfoReqBean();

        ERequestType requestTypeForProductCode;

        String productCode = null;

        if (isAddAndPay) {
            requestTypeForProductCode = ERequestType.ADD_AND_PAY;
            if (flowTransBean.getWorkFlowBean().getExtendInfo() != null)
                flowTransBean.getWorkFlowBean().getExtendInfo().setAcquiringType(null);
        } else {
            if (NATIVE_PAY.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    && flowTransBean.getWorkFlowBean().getProductCode() != null) {
                try {
                    productCode = String.valueOf(ProductCodes.getProductById(flowTransBean.getWorkFlowBean()
                            .getProductCode()));
                } catch (FacadeInvalidParameterException e) {
                    LOGGER.error("Could not set product code because : ", e);
                }
                requestTypeForProductCode = ERequestType.getByProductCode(productCode);
            } else
                requestTypeForProductCode = flowTransBean.getWorkFlowBean().getRequestType();
        }

        /*
         * Hack for support PRN feature
         */
        if (flowTransBean.getWorkFlowBean().isPrnEnabled() && !isAddAndPay)
            requestTypeForProductCode = ERequestType.QR_PRN_ENABLED;

        String customizeCode = flowTransBean.getWorkFlowBean().getCustomizeCode();

        // both parameters are related for postpaid checkbalance flow via
        // instaProxy
        if (null != flowTransBean.getUserDetails() && null != flowTransBean.getWorkFlowBean().getExtendInfo()) {
            flowTransBean.getWorkFlowBean().getExtendInfo().setPaytmUserId(flowTransBean.getUserDetails().getUserId());
            flowTransBean.getWorkFlowBean().getExtendInfo()
                    .setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        }
        /* Check if user is eligible for Paytm CC */
        List<String> exclusionPayMethods = new ArrayList<String>();
        if (flowTransBean.getUserDetails() != null && !flowTransBean.getUserDetails().isPaytmCCEnabled()
                && !flowTransBean.getWorkFlowBean().isPostpaidEnabledOnMerchant()) {
            LOGGER.info("PAYTM_DIGITAL_CREDIT disabled due to user is null or feature not enabled");
            exclusionPayMethods.add(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        } else if (isAddAndPay && !flowTransBean.getWorkFlowBean().isPostPaidOnAddnPay()) {
            LOGGER.info("PAYTM_DIGITAL_CREDIT disabled due to ADD_AND_PAY and pospaid is enabled on this merchnt");
            exclusionPayMethods.add(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        }
        /**
         * This is for disabling paytm instruments for corporate Advance Deposit
         * which include BALANCE, PPBL, PAYTM_DIGITAL_CREDIT
         */
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCorporateCustId())
                && CollectionUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getDisabledPaymentModes())) {
            exclusionPayMethods = flowTransBean.getWorkFlowBean().getDisabledPaymentModes();
        }

        String alipayMid = "";
        if (isAddAndPay) {
            if (ff4JUtil.isFeatureEnabled(
                    TheiaConstant.ExtraConstants.THEIA_SEND_MERCHANT_ID_IN_ADD_AND_PAY_LITEPAYVIEWTASK, flowTransBean
                            .getWorkFlowBean().getPaytmMID())) {
                alipayMid = flowTransBean.getWorkFlowBean().getAlipayMID();
            }
        } else {
            alipayMid = flowTransBean.getWorkFlowBean().getAlipayMID();
        }

        if (StringUtils.isNotBlank(payerUserID)
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPwpCategory())) {
            pwpCategory = flowTransBean.getWorkFlowBean().getPwpCategory();
        }

        final LitePayviewConsultRequestBizBean consultRequest = new LitePayviewConsultRequestBizBean(payerUserID,
                envInfoRequestBean, requestTypeForProductCode, customizeCode, alipayMid,
                flowTransBean.isPostConvenienceFeeModel(), exclusionPayMethods,
                AlipayRequestUtils.getExtendeInfoMap(flowTransBean.getWorkFlowBean().getExtendInfo()), pwpCategory,
                flowTransBean.getWorkFlowBean().isDynamicFeeMerchant(), flowTransBean.getWorkFlowBean().getPaytmMID(),
                paytmUserId, isAddAndPay);

        String token = null;

        if (flowTransBean.getUserDetails() != null) {

            token = StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getToken()) ? flowTransBean
                    .getWorkFlowBean().getToken() : (StringUtils.isNotBlank(flowTransBean.getUserDetails()
                    .getUserToken()) ? flowTransBean.getUserDetails().getUserToken() : null);
        }

        if (StringUtils.isNotBlank(token)) {
            putSubWalletDetails(flowTransBean, consultRequest.getExtendInfo());
        }

        if (exclusionPayMethods.isEmpty() && (flowTransBean.getUserDetails() != null)) {

            if (StringUtils.isNotBlank(token)) {

                Map<String, String> extendInfo = new HashMap<String, String>();

                if (consultRequest.getExtendInfo() != null) {
                    extendInfo = consultRequest.getExtendInfo();
                }
                consultRequest.setExtendInfo(extendInfo);
            }
        }

        consultRequest.setFromAoaMerchant(flowTransBean.getWorkFlowBean().isFromAoaMerchant());
        consultRequest.setDefaultLitePayView(flowTransBean.isDefaultLiteViewFlow());
        consultRequest.setRequestType(flowTransBean.getWorkFlowBean().getRequestType().getType());
        consultRequest.setIncludeDisabledAssets(flowTransBean.getWorkFlowBean().isReturnDisabledChannelInFpo());
        // check isFeatureEnabled to fetchSavedCardFrom platform +;
        String userId = null != flowTransBean.getUserDetails() ? flowTransBean.getUserDetails().getUserId()
                : null != flowTransBean.getWorkFlowBean().getUserDetailsBiz() ? flowTransBean.getWorkFlowBean()
                        .getUserDetailsBiz().getUserId() : null;
        if (ff4JUtil.fetchSavedCardFromPlatform(flowTransBean.getWorkFlowBean(), userId)) {
            consultRequest.setExternalUserId(flowTransBean.getWorkFlowBean().getCustID());
            if (!StringUtils.equals(userId, flowTransBean.getWorkFlowBean().getCustID())
                    && flowTransBean.getWorkFlowBean().isStoreCardPrefEnabled()) {
                consultRequest.setFetchSavedAssetForMerchant(Boolean.TRUE.toString());
                consultRequest.setFetchSavedAsset(Boolean.TRUE.toString());
            }
            if (payerUserID != null && null != flowTransBean.getUserDetails()) {
                consultRequest.setFetchSavedAssetForUser(Boolean.TRUE.toString());
                consultRequest.setFetchSavedAsset(Boolean.TRUE.toString());
            }
        }
        if (isAddAndPay) {
            consultRequest.setAddAndPayMigration(String.valueOf(flowTransBean.getWorkFlowBean()
                    .isAddAndPayWithUPICollectSupported()));
        }

        if (workFlowHelper.getProductCodeForDynamicChargePayment(flowTransBean.getWorkFlowBean().getProductCode()) == null) {
            consultRequest.setDefaultDynamicFeeMerchantPayment(true);
        }

        if (Objects.nonNull(flowTransBean.getWorkFlowBean().getCardPreAuthType())) {
            consultRequest.setPayConfirmFlowType(flowTransBean.getWorkFlowBean().getCardPreAuthType().getValue());
            if (Objects.nonNull(flowTransBean.getWorkFlowBean().getPreAuthBlockSeconds())) {
                consultRequest.setBlockPeriodInSeconds(String.valueOf(flowTransBean.getWorkFlowBean()
                        .getPreAuthBlockSeconds()));
            }
        }

        if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().isMerchantEligibleForMultipleMBIDFlow())) {
            VerificationType verificationType = workFlowHelper.getVerificationType(flowTransBean.getWorkFlowBean());
            consultRequest.setVerificationType(verificationType == null ? null : verificationType.getType());
        }

        consultRequest.setDealsFlow(flowTransBean.getWorkFlowBean().isDealsFlow());

        if (null != flowTransBean.getWorkFlowBean().getBrandCodes()) {
            EmiPlanParam emiPlanParam = new EmiPlanParam();
            emiPlanParam.setBrandCode(flowTransBean.getWorkFlowBean().getBrandCodes());
            consultRequest.setEmiPlanParam(emiPlanParam);
        }

        LOGGER.debug("Created request for lite payview Consult as ::{}", consultRequest);
        return consultRequest;
    }

    private void putSubWalletDetails(WorkFlowTransactionBean flowTransBean, Map<String, String> extendInfo) {

        if (Objects.nonNull(extendInfo) && Objects.nonNull(flowTransBean)
                && Objects.nonNull(flowTransBean.getWorkFlowBean())
                && Objects.nonNull(flowTransBean.getWorkFlowBean().getSubWalletOrderAmountDetails())) {

            try {
                extendInfo.put(ExtendedInfoKeys.SUBWALLET_AMOUNT_DETAILS,
                        JsonMapper.mapObjectToJson(flowTransBean.getWorkFlowBean().getSubWalletOrderAmountDetails()));
            } catch (Exception exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }

    }

    private boolean isCachingConfiguredForOfflineFlow(WorkFlowTransactionBean workFlowTransBean) {
        String isCachingConfigured = ConfigurationUtil.getProperty(BizConstant.IS_LITEPAYVIEW_CONSULT_CACHE_ENABLED,
                "false");
        return workFlowTransBean.getWorkFlowBean().isOfflineFetchPayApi()
                && BooleanUtils.toBoolean(isCachingConfigured);
    }

    public CreateTopUpRequestBizBean createTopUpRequestBizBean(final WorkFlowTransactionBean flowTransBean) {

        final CreateTopUpRequestBizBean createTopUpRequestBean = new CreateTopUpRequestBizBean(flowTransBean
                .getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean().getCurrency(), flowTransBean
                .getWorkFlowBean().getFundType(), flowTransBean.getUserDetails().getInternalUserId(), flowTransBean
                .getWorkFlowBean().getExtendInfo(), flowTransBean.getWorkFlowBean().getOrderID(), flowTransBean
                .getWorkFlowBean().getNotificationUrl(), flowTransBean.getWorkFlowBean().getAlipayMID(), flowTransBean
                .getWorkFlowBean().getEnvInfoReqBean());
        LOGGER.debug("Created request for Create top Up request as ::{}", createTopUpRequestBean);
        return createTopUpRequestBean;
    }

    private void setFeeRateFactorsInfo(BizPayOptionBill bizPayOption1, BizPayOptionBill bizPayOption2,
            WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.getFeeRateFactors() != null) {
            if (bizPayOption1 != null && bizPayOption2 == null) {
                bizPayOption1.setPrepaidCard(workFlowRequestBean.isPrepaidCard());
                bizPayOption1.setFeeRateFactorsInfo(workFlowRequestBean.getFeeRateFactors().getFeeRateFactorsMap());
                if (workFlowRequestBean.getFeeRateCode() != null) {
                    bizPayOption1.getFeeRateFactorsInfo().put(FEE_RATE_CODE, workFlowRequestBean.getFeeRateCode());
                }
            }

            if (bizPayOption2 != null) {
                bizPayOption2.setPrepaidCard(workFlowRequestBean.isPrepaidCard());
                bizPayOption2.setFeeRateFactorsInfo(workFlowRequestBean.getFeeRateFactors().getFeeRateFactorsMap());
                if (workFlowRequestBean.getFeeRateCode() != null) {
                    bizPayOption2.getFeeRateFactorsInfo().put(FEE_RATE_CODE, workFlowRequestBean.getFeeRateCode());
                }
            }
        }
    }

    private void setPayModeForVisaSingleClick(BizPayOptionBill bizPayOption1, BizPayOptionBill bizPayOption2,
            WorkFlowRequestBean workFlowRequestBean) {

        /*
         * Whether to apply check for debit card and credit card
         */
        if (workFlowRequestBean.getOneClickInfo() != null) {
            OneClickInfo oneClickInfo = null;
            try {
                oneClickInfo = JsonMapper.mapJsonToObject(workFlowRequestBean.getOneClickInfo(), OneClickInfo.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Unable to map oneClickInfo info from request");
            }

            if (bizPayOption1 != null && bizPayOption2 == null) {
                bizPayOption1.setPayMode(oneClickInfo.getType());

            }

            if (bizPayOption2 != null) {
                bizPayOption2.setPayMode(oneClickInfo.getType());
            }
        }
    }

    public BizPayRequest createBizPayRequest(final WorkFlowTransactionBean flowTransBean)
            throws PaytmValidationException, BaseException, MappingServiceClientException {
        EPayMethod payMethod2 = null;
        Long walletBalance = 0L;
        Long diffAmountInDouble = 0L;
        String differenceAmount;
        String payOption;
        String payOption2 = null;
        String passThroughExtendInfo;
        boolean isDynamicQRAddNPay = false;
        Map<String, String> passThroughExtendInfoMap = new HashMap<>();
        final List<BizPayOptionBill> payOptionBills = new ArrayList<>();
        Map<String, String> channelInfo = new HashMap<>();
        setBankFormOptimizedFlow(flowTransBean, flowTransBean.getChannelInfo());
        channelInfo.putAll(buildDefaultChannelInfo());
        if (flowTransBean.getChannelInfo() != null)
            channelInfo.putAll(flowTransBean.getChannelInfo());

        String securityId = null;

        BizPayOptionBill bizPayOptionBill1 = null;
        BizPayOptionBill bizPayOptionBill2 = null;
        // setting direct pass through info
        setPassThroughChannelExtendedInfo(flowTransBean, channelInfo);
        setRedisKeyForQueryParams(flowTransBean.getWorkFlowBean());
        // Passing expiry time in passthroughExtendInfo In UpiCollectPayments
        if (isUpiCollectPayment(flowTransBean)) {
            setPassThroughExtendInfoForUpiCollect(flowTransBean, channelInfo);
        } else if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            setPassThroughExtendInfoForOfflineUPI(flowTransBean, channelInfo);
        }
        // passing merchant dispaly name in channelinfo
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && !StringUtils.isBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName())) {
            channelInfo.put(ExtendedInfoKeys.MERCHANT_DISPLAY_NAME, flowTransBean.getWorkFlowBean().getExtendInfo()
                    .getMerchantName());
        }
        if (null != flowTransBean.getMerchantViewConsult()) {
            securityId = flowTransBean.getMerchantViewConsult().getSecurityId();
        }

        EPayMethod payMethod = EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod());
        if (payMethod == null) {
            payMethod = EPayMethod.getPayMethodByOldName(flowTransBean.getWorkFlowBean().getPaymentTypeId());
        }

        if (flowTransBean.isFastForwardRequest()
                && ERequestType.OFFLINE.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
            setParamInChannelInfo(flowTransBean, channelInfo);
        }
        if (flowTransBean.getWorkFlowBean().getUpiLiteRequestData() != null) {
            setParamInChannelInfo(flowTransBean, channelInfo);
        }

        String cacheCardToken = "";

        if (ADDMONEY_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(flowTransBean.getWorkFlowBean()
                        .getRequestType().getType()) || isSubscriptionRenewalCase(flowTransBean)
                || NATIVE_MF_SIP.equals(flowTransBean.getWorkFlowBean().getRequestType())) {

            if (ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(flowTransBean.getWorkFlowBean()
                    .getRequestType().getType())) {
                String chargeAmount = fetchChargeAmountIfApplicable(flowTransBean);
                flowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);
            }

            if (StringUtils.isNotBlank(flowTransBean.getCacheCardToken())) {
                cacheCardToken = flowTransBean.getCacheCardToken();
            }
        } else if (!(payMethod.equals(EPayMethod.NET_BANKING) || payMethod.equals(EPayMethod.UPI) || payMethod
                .equals(EPayMethod.PAYTM_DIGITAL_CREDIT))) {
            cacheCardToken = flowTransBean.getWorkFlowBean().getPaymentDetails();
        }

        /* Hack for Dynamic QR to support PPBL and Postpaid */
        if (ERequestType.DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
            if (StringUtils.isNotBlank(flowTransBean.getCacheCardToken())) {
                cacheCardToken = flowTransBean.getCacheCardToken();
            }
            setParamInChannelInfo(flowTransBean, channelInfo);
            String chargeAmount = fetchChargeAmountIfApplicable(flowTransBean);
            flowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);

        }

        if (null == flowTransBean.getExtendInfo()) {
            Map<String, String> extInfo = new HashMap<>();
            prepareExtendedInfo(flowTransBean, extInfo);
            flowTransBean.setExtendInfo(extInfo);
        } else {
            prepareExtendedInfo(flowTransBean, flowTransBean.getExtendInfo());
        }

        if (EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode())) {
            setDeepIntegrationFieldsToPassThroughExtendedInfo(flowTransBean, passThroughExtendInfoMap);
        }

        ExtendedInfoRequestBean extendInfo = createExtendedInfo(flowTransBean);
        Map<String, String> extendeInfoMap = AlipayRequestUtils.getExtendeInfoMap(extendInfo);
        if (flowTransBean.getExtendInfo() == null) {
            flowTransBean.setExtendInfo(extendeInfoMap);
        } else {
            flowTransBean.getExtendInfo().putAll(extendeInfoMap);
        }

        if (ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(flowTransBean.getWorkFlowBean()
                .getRequestType().getType())
                || isSubscriptionRenewalCase(flowTransBean)
                || ERequestType.NATIVE_MF_SIP == flowTransBean.getWorkFlowBean().getRequestType()
                || (FIRST_REQUEST.equals(flowTransBean.getWorkFlowBean().getSubsPaymentType()) && flowTransBean
                        .getWorkFlowBean().isInternalRequestForSubsPayment())) {

            enrichInExtendInfoForSeamlessPayment(flowTransBean);

            setSubWalletAmountToExtendedInfo(flowTransBean);

            // to support appinvoke in flipkart
            setcollectAppInvokeToExtendedInfo(flowTransBean);

            setParamInChannelInfo(flowTransBean, channelInfo);

            if (EPayMode.NONE.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())
                    || isSubscriptionRenewalCase(flowTransBean)) {

                setPayOptionForUpiIntent(flowTransBean);

                WorkFlowRequestBean workFlowBean = flowTransBean.getWorkFlowBean();
                String txnAmountStr = workFlowBean.getTxnAmount();
                SubsPaymentMode paymentMode = null;
                if (isBankMandateCase(flowTransBean)) {
                    paymentMode = SubsPaymentMode.BANK_MANDATE;
                    LOGGER.info("Upront Payment case: WorkFlowBean is: {}", workFlowBean);
                    bizPayOptionBill1 = new BizPayOptionBill(null, null, txnAmountStr, "0");
                    populatePayOptionBillForMandate(bizPayOptionBill1, channelInfo, extendeInfoMap, flowTransBean,
                            workFlowBean);
                } else if (ERequestType.isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType()
                        .getType())
                        && flowTransBean.getSubscriptionServiceResponse() != null) {
                    if (paymentMode == null) {
                        paymentMode = flowTransBean.getSubscriptionServiceResponse().getPaymentMode();
                    }
                    LOGGER.info("Setting BizPayOptionBill for Subscription PaymentMode: {}", paymentMode);
                    switch (paymentMode) {
                    case PPBL:
                        LOGGER.info("/Pay for PPBL");
                        UserInfo userInfo = userMappingService.getUserData(flowTransBean
                                .getSubscriptionServiceResponse().getPayerUserID(), UserOwner.ALIPAY);
                        payMethod = EPayMethod.PPBL;
                        payOption = "PPBL";

                        bizPayOptionBill1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                        bizPayOptionBill1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse()
                                .getPayerAccountNumber());
                        passThroughExtendInfoMap.put(BizConstant.userId, userInfo.getPaytmId());
                        passThroughExtendInfoMap.put(BizConstant.accountType, PPBLAccountType.SAVING_ACCOUNT.getType());
                        if (StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse().getAccountType())) {
                            passThroughExtendInfoMap.put(BizConstant.accountType, flowTransBean
                                    .getSubscriptionServiceResponse().getAccountType());
                        }
                        passThroughExtendInfo = preparePassThroughExtendInfoForPPBL(passThroughExtendInfoMap);
                        channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, passThroughExtendInfo);
                        break;
                    case UPI:
                        bizPayOptionBill1 = new BizPayOptionBill(null, null, txnAmountStr, "0");
                        populatePayOptionBillForUPIRecurringMandate(bizPayOptionBill1, channelInfo, extendeInfoMap,
                                flowTransBean, workFlowBean);
                        bizPayOptionBill1.setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVpa());
                        break;
                    case NORMAL:
                        if (StringUtils.isBlank(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber())) {
                            throw new BaseException(
                                    "Payer Account Number can't be blank in case of NORMAL Subscription");
                        }
                        UserInfo userData = userMappingService.getUserData(flowTransBean
                                .getSubscriptionServiceResponse().getPayerUserID(), UserOwner.ALIPAY);
                        GenericCoreResponseBean<Money> walletBalanceResponse = walletService.fetchWalletBalance(
                                userData.getPaytmId(), flowTransBean.getWorkFlowBean().getAdminToken(), flowTransBean
                                        .getWorkFlowBean().getOrderID());

                        if (!walletBalanceResponse.isSuccessfullyProcessed()) {
                            LOGGER.error(BALANCE_FETCH_ERR_MSG);
                            throw new BaseException(BALANCE_FETCH_ERR_MSG);
                        }
                        walletBalance = Long.valueOf(walletBalanceResponse.getResponse().getAmount());
                        differenceAmount = differenceAmount(txnAmountStr, walletBalance);
                        diffAmountInDouble = Long.parseLong(differenceAmount);

                        payMethod = EPayMethod.BALANCE;
                        payOption = "BALANCE";

                        if (diffAmountInDouble <= 0) {
                            bizPayOptionBill1 = new BizPayOptionBill(payOption, payMethod, txnAmountStr, "0");
                            bizPayOptionBill1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse()
                                    .getPayerAccountNumber());
                        } else {
                            if (walletBalance > 0) {
                                bizPayOptionBill1 = new BizPayOptionBill(payOption, payMethod,
                                        walletBalance.toString(), "0");
                                bizPayOptionBill1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse()
                                        .getPayerAccountNumber());
                            }

                            if (StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse().getSavedCardID())) {
                                if (CardType.CREDIT_CARD.name().equalsIgnoreCase(
                                        flowTransBean.getSavedCard().getCardType())) {
                                    payMethod2 = EPayMethod.CREDIT_CARD;
                                    payOption2 = CardType.CREDIT_CARD.name() + "_"
                                            + flowTransBean.getSavedCard().getCardScheme();
                                } else if (CardType.DEBIT_CARD.name().equalsIgnoreCase(
                                        flowTransBean.getSavedCard().getCardType())) {
                                    payMethod2 = EPayMethod.DEBIT_CARD;
                                    payOption2 = CardType.DEBIT_CARD.name() + "_"
                                            + flowTransBean.getSavedCard().getCardScheme();
                                }
                                bizPayOptionBill2 = new BizPayOptionBill(payOption2, payMethod2, differenceAmount, "0");
                                bizPayOptionBill2.setCardCacheToken(flowTransBean.getCacheCardToken());
                            } else {
                                payMethod2 = EPayMethod.RENEW_PPBL;
                                payOption2 = "PPBL";
                                passThroughExtendInfoMap.put(BizConstant.userId, userData.getPaytmId());
                                passThroughExtendInfo = preparePassThroughExtendInfoForPPBL(passThroughExtendInfoMap);
                                channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, passThroughExtendInfo);
                                bizPayOptionBill2 = new BizPayOptionBill(payOption2, payMethod2, differenceAmount, "0");
                            }
                            bizPayOptionBill2.setTopupAndPay(true);
                        }
                        break;
                    default:
                        bizPayOptionBill1 = getBizPayOptionBill(flowTransBean, payMethod, cacheCardToken);
                    }
                } else {
                    LOGGER.info("Non-subscription flow.");
                    bizPayOptionBill1 = getBizPayOptionBillNonSubscriptionFlow(flowTransBean, payMethod, cacheCardToken);
                }
                enrichBizPayOptionBill(flowTransBean, bizPayOptionBill1, payMethod);
                LOGGER.info("PaybillOption {}", bizPayOptionBill1);
            } else {

                try {

                    List<BizPayOptionBill> paytmExpressPayOptionBills = generatePayOptionBillsForPaytmExpress(
                            flowTransBean, flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean
                                    .getWorkFlowBean().getChargeAmount(), channelInfo);
                    bizPayOptionBill1 = paytmExpressPayOptionBills.get(0);
                    bizPayOptionBill2 = paytmExpressPayOptionBills.get(1);
                    if (bizPayOptionBill1 != null
                            && (!NumberUtils.isNumber(bizPayOptionBill1.getTransAmount())
                                    || Integer.parseInt(bizPayOptionBill1.getTransAmount()) <= 0 || flowTransBean
                                    .getWorkFlowBean().isUpiConvertedToAddNPay())) {
                        bizPayOptionBill1 = bizPayOptionBill2;
                        bizPayOptionBill2 = null;
                    }

                } catch (Exception e) {
                    LOGGER.error("Exception occured while creating createBizPayRequest request ", e);
                }

            }

            setPayModeForVisaSingleClick(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean());

            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardNo())) {
                flowTransBean.getExtendInfo().put(TheiaConstant.RequestParams.BIN_NUMBER,
                        flowTransBean.getWorkFlowBean().getCardNo().substring(0, 6));
            } else if (null != flowTransBean.getSavedCard()
                    && StringUtils.isNotBlank(flowTransBean.getSavedCard().getCardNumber())) {
                flowTransBean.getExtendInfo().put(TheiaConstant.RequestParams.BIN_NUMBER,
                        flowTransBean.getSavedCard().getCardNumber().substring(0, 6));
            }
            setBinDetailInExtendedInfo(flowTransBean, flowTransBean.getExtendInfo());

            setVPAforUPIPushAndCollect(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean()
                    .getVirtualPaymentAddress(), flowTransBean.getUserDetails());

            settingSubsDetailsInChannelInfo(flowTransBean, flowTransBean.getChannelInfo());

            if (bizPayOptionBill1 != null) {
                bizPayOptionBill1.setChannelInfo(flowTransBean.getChannelInfo());
                bizPayOptionBill1.setExtendInfo(flowTransBean.getExtendInfo());
                payOptionBills.add(bizPayOptionBill1);
            }
            if (bizPayOptionBill2 != null) {
                bizPayOptionBill2.setChannelInfo(flowTransBean.getChannelInfo());
                bizPayOptionBill2.setExtendInfo(flowTransBean.getExtendInfo());
                payOptionBills.add(bizPayOptionBill2);
            }
            setFeeRateFactorsInfo(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean());
            setVanInfoForCheckoutFlow(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean());

            if (QR_SUBSCRIPTION.equals(flowTransBean.getWorkFlowBean().getType())) {
                setWorkflowToExtendedInfo(flowTransBean);
            }

        } else {

            // to support subwallet in Dynamic QR and All in one payment request
            // flow
            setSubWalletAmountToExtendedInfo(flowTransBean);

            // to support checkoutjs flow
            if (ERequestType.DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || DYNAMIC_QR_2FA.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || SEAMLESS_3D_FORM.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
                setWorkflowToExtendedInfo(flowTransBean);
                setSdkTypeToExtendInfo(flowTransBean);
            }

            BizPayOptionBill bizPayOptionBill = null;
            if (EPayMethod.BANK_TRANSFER.equals(payMethod)) {
                bizPayOptionBill = getBizPayOptionBillForBankTransfer(flowTransBean, payMethod, channelInfo,
                        flowTransBean.getExtendInfo());
                flowTransBean.getWorkFlowBean().setChargeAmount(fetchChargeAmountIfApplicable(flowTransBean));
                if (ERequestType.NATIVE_MF == flowTransBean.getWorkFlowBean().getRequestType()) {
                    setVanInfoForCheckoutFlow(bizPayOptionBill, null, flowTransBean.getWorkFlowBean());
                    bizPayOptionBill.setTpvInfos(flowTransBean.getWorkFlowBean().getTpvInfos());
                }
            } else if (DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    && EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())
                    && ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                            ENABLE_ADDNPAY_ON_DYNAMIC_QR, false)) {
                try {
                    isDynamicQRAddNPay = true;
                    List<BizPayOptionBill> paytmExpressPayOptionBills = generatePayOptionBillsForPaytmExpress(
                            flowTransBean, flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean
                                    .getWorkFlowBean().getChargeAmount(), channelInfo);
                    bizPayOptionBill1 = paytmExpressPayOptionBills.get(0);
                    bizPayOptionBill2 = paytmExpressPayOptionBills.get(1);
                    if (flowTransBean.getExtendInfo() != null) {
                        flowTransBean.getExtendInfo().put(TXN_TYPE, TXN_TYPE_ADDNPAY);
                    }
                    if (bizPayOptionBill1 != null
                            && (!NumberUtils.isNumber(bizPayOptionBill1.getTransAmount())
                                    || Integer.parseInt(bizPayOptionBill1.getTransAmount()) <= 0 || flowTransBean
                                    .getWorkFlowBean().isUpiConvertedToAddNPay())) {
                        bizPayOptionBill1 = bizPayOptionBill2;
                        bizPayOptionBill2 = null;
                    }

                    if (bizPayOptionBill1 != null) {
                        bizPayOptionBill1.setChannelInfo(flowTransBean.getChannelInfo());
                        bizPayOptionBill1.setExtendInfo(flowTransBean.getExtendInfo());
                        payOptionBills.add(bizPayOptionBill1);
                    }
                    if (bizPayOptionBill2 != null) {
                        bizPayOptionBill2.setChannelInfo(flowTransBean.getChannelInfo());
                        bizPayOptionBill2.setExtendInfo(flowTransBean.getExtendInfo());
                        payOptionBills.add(bizPayOptionBill2);
                    }
                    setFeeRateFactorsInfo(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean());
                    setVPAforUPIPushAndCollect(bizPayOptionBill1, bizPayOptionBill2, flowTransBean.getWorkFlowBean()
                            .getVirtualPaymentAddress(), flowTransBean.getUserDetails());

                } catch (Exception e) {
                    LOGGER.error("Exception occured while creating createBizPayRequest request ", e);
                }
            } else {
                bizPayOptionBill = new BizPayOptionBill(flowTransBean.getWorkFlowBean().getPayOption(), payMethod,
                        flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean()
                                .getChargeAmount(), flowTransBean.getChannelInfo(), flowTransBean.getExtendInfo(),
                        cacheCardToken);
            }
            if (null != flowTransBean.getUserDetails() && !isDynamicQRAddNPay) {
                bizPayOptionBill.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());
            }
            if (!isDynamicQRAddNPay) {
                setFeeRateFactorsInfo(bizPayOptionBill, null, flowTransBean.getWorkFlowBean());
                payOptionBills.add(bizPayOptionBill);
            }
            // To send bin number in additional info so that cardscheme can be
            // sent from theiaresponsegenerator in callback
            // when doing offline v1/PTC (Scan&Pay)
            if (ERequestType.DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())) {

                if (flowTransBean.getWorkFlowBean().getPaymentTypeId()
                        .equals(PaymentTypeIdEnum.GIFT_VOUCHER.getValue())
                        && !isDynamicQRAddNPay) {
                    bizPayOptionBill.setTemplateIds(flowTransBean.getWorkFlowBean().getMgvTemplateIds());
                    bizPayOptionBill.setChargeAmount("0");
                    payOptionBills.add(bizPayOptionBill);
                }

                LOGGER.info("Adding Bank Name and Bin Number in extend info for Dynamic_QR");
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getBankName())) {
                    flowTransBean.getExtendInfo().put(
                            TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys.ISSUING_BANK_NAME,
                            flowTransBean.getWorkFlowBean().getBankName());
                }
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardNo())) {
                    flowTransBean.getExtendInfo().put(TheiaConstant.RequestParams.BIN_NUMBER,
                            flowTransBean.getWorkFlowBean().getCardNo().substring(0, 6));
                } else if (null != flowTransBean.getSavedCard()
                        && StringUtils.isNotBlank(flowTransBean.getSavedCard().getCardNumber())) {
                    flowTransBean.getExtendInfo().put(TheiaConstant.RequestParams.BIN_NUMBER,
                            flowTransBean.getSavedCard().getCardNumber().substring(0, 6));
                }
            }
            if (!isDynamicQRAddNPay) {
                bizPayOptionBill.setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());
            }
        }

        setPassThroughExtendInfoForUpiLite(flowTransBean, channelInfo);

        // checking to save card at platform plus
        saveCardAtPlatformIfApplicable(flowTransBean, payOptionBills);
        updatePayOptionBillsForCoft(flowTransBean, payOptionBills);

        Map<String, String> riskExtendInfo = new HashMap<>();
        if (!StringUtils.isBlank(flowTransBean.getWorkFlowBean().getChannelID())) {
            riskExtendInfo.put(TheiaConstant.RequestParams.CHANNEL_ID, flowTransBean.getWorkFlowBean().getChannelID());
        }
        if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            riskExtendInfo.put(BizConstant.VPA, flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());
        }

        if (PaymentAdapterUtil.eligibleForPG2(flowTransBean.getExtendInfo())) {
            updateSettleTypeinPayOptionBills(flowTransBean, payOptionBills);
            updateDeepLinkReqdinPayOptionBills(flowTransBean, payOptionBills);
            updateCCOnUPIReqInPayOptionBills(flowTransBean, payOptionBills);
        } else if (flowTransBean.getExtendInfo() != null
                && (flowTransBean.getExtendInfo().get(ROUTE) == null || Routes.PLATFORM.getName().equals(
                        flowTransBean.getExtendInfo().get(ROUTE)))
                && SettlementType.DIRECT_SETTLEMENT.getValue().equals(flowTransBean.getWorkFlowBean().getSettleType())) {
            LOGGER.error("Direct Settlement is not allowed on this route");
            throw new PaytmValidationException("Direct Settlement is not allowed on this route");
        }

        updateRiskExtendInfoForAddMoney(flowTransBean, riskExtendInfo);
        updateGcinForCoftWalletTxn(flowTransBean);
        updateGcinForCoftInPay(flowTransBean, payOptionBills);
        // Set merchant-userId in risk-extend-info
        setMerchantUserIdInRiskExtendInfo(flowTransBean, riskExtendInfo);
        updateRiskExtendedInfoForPG2(flowTransBean, riskExtendInfo, flowTransBean.getExtendInfo().get(ROUTE));
        setNickname(flowTransBean);

        PaymentScenario paymentScenario = null;
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSecurityId())) {
            paymentScenario = PaymentScenario.PAY_VERIFICATION;
            securityId = flowTransBean.getWorkFlowBean().getSecurityId();
        }
        if (StringUtils.isBlank(flowTransBean.getTransID()) && flowTransBean.getWorkFlowBean() != null) {
            flowTransBean.setTransID(flowTransBean.getWorkFlowBean().getTransID());
        }
        if (EPayMethod.PPBL.getOldName().equalsIgnoreCase(flowTransBean.getWorkFlowBean().getBankCode())) {
            setPPBLAccountTypeInPassThrough(payOptionBills, flowTransBean.getWorkFlowBean());
        }

        BizPayRequest bizPayRequest = new BizPayRequest(flowTransBean.getTransID(), flowTransBean.getWorkFlowBean()
                .getTransType(), flowTransBean.getUserDetails() != null ? flowTransBean.getUserDetails()
                .getInternalUserId() : null, payOptionBills, securityId, RequestIdGenerator.generateRequestId(),
                flowTransBean.getWorkFlowBean().getRequestType(), flowTransBean.getExtendInfo(), paymentScenario,
                flowTransBean.getWorkFlowBean().getPwpCategory(), flowTransBean.getWorkFlowBean()
                        .isAddAndPayWithUPICollectSupported(), flowTransBean.getWorkFlowBean()
                        .isCostBasedPreferenceEnabled(), flowTransBean.getWorkFlowBean().getPaytmMID());

        if (ObjectUtils.notEqual(flowTransBean.getWorkFlowBean().getUltimateBeneficiaryDetails(), null)) {
            bizPayRequest
                    .setUltimateBeneficiaryDetails(flowTransBean.getWorkFlowBean().getUltimateBeneficiaryDetails());
        }

        if (MapUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getRiskExtendedInfo())) {
            riskExtendInfo.putAll(flowTransBean.getWorkFlowBean().getRiskExtendedInfo());
        }

        bizPayRequest.setRiskExtendInfo(riskExtendInfo);

        if (bizPayRequest.getEnvInfo() == null) {
            try {
                if (isSubscriptionRenewalCase(flowTransBean)) {
                    flowTransBean.getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);
                }
                final EnvInfo envInfo = AlipayRequestUtils
                        .createEnvInfo(flowTransBean.getEnvInfoReqBean() != null ? flowTransBean.getEnvInfoReqBean()
                                : (flowTransBean.getWorkFlowBean() != null ? flowTransBean.getWorkFlowBean()
                                        .getEnvInfoReqBean() : null));
                bizPayRequest.setEnvInfo(envInfo);
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Error while creating envInfo ", e);
            }
        }

        boolean isAddNPayTransaction = isAddNPayTransaction(flowTransBean);
        boolean isTopUpTransaction = isTopUpTransaction(flowTransBean.getWorkFlowBean());
        if ((CollectionUtils.isNotEmpty(bizPayRequest.getPayOptionBills()) && bizPayRequest.getPayOptionBills()
                .stream().anyMatch(s -> s.getPayMethod().equals(EPayMethod.EMI)))
                && (isAddNPayTransaction || isTopUpTransaction || bizPayRequest.isCostBasedPreferenceEnabled())) {
            setBinDetailInExtendedInfo(flowTransBean, flowTransBean.getExtendInfo());
        }

        if (QR_SUBSCRIPTION.equals(flowTransBean.getWorkFlowBean().getType())
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getPaytmMerchantId())
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getDummyOrderId())) {
            flowTransBean.getExtendInfo().put("custID", flowTransBean.getExtendInfo().get("customerId"));
        }

        bizPayRequest.setExtInfo(flowTransBean.getExtendInfo());

        bizPayRequest.setPaymentBizInfo(AlipayRequestUtils.getPaymentBizinfo(flowTransBean));
        setOfferDetailsInPaymentBizData(flowTransBean, bizPayRequest.getPaymentBizInfo());
        bizPayRequest.setPaytmMerchantId(flowTransBean.getWorkFlowBean().getPaytmMID());

        if (flowTransBean.getWorkFlowBean() != null
                && StringUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getRequestFlow()))
            bizPayRequest.setRequestFlow(flowTransBean.getWorkFlowBean().getRequestFlow());

        if (bizPayRequest.getPayOptionBills() != null
                && Objects.nonNull(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getCardPreAuthType())) {
            bizPayRequest.getPayOptionBills().forEach(
                    payOptionBill -> payOptionBill.setPayConfirmFlowType(flowTransBean.getWorkFlowBean()
                            .getPaymentRequestBean().getCardPreAuthType()));
        }

        if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().isMerchantEligibleForMultipleMBIDFlow())) {
            VerificationType verificationType = workFlowHelper.getVerificationType(flowTransBean.getWorkFlowBean());
            bizPayRequest.setVerificationType(verificationType == null ? null : verificationType.getType());
            EXT_LOGGER.customInfo("Verification Type is added in Pay request");
        }
        if (PaymentAdapterUtil.eligibleForPG2(flowTransBean.getExtendInfo())) {
            updatePayOptionBillsForBankTransferPG2(flowTransBean.getWorkFlowBean(), bizPayRequest.getPayOptionBills());
        }

        if (flowTransBean.isAddMoneyPcfEnabled()) {
            if (flowTransBean.getWorkFlowBean().isNativeAddMoney() && bizPayOptionBill1 != null) {
                Map<String, String> bizPayOption1ExtendInfo = bizPayOptionBill1.getExtendInfo();
                bizPayOption1ExtendInfo.put(ADD_MONEY_SURCHARGE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
                bizPayOptionBill1.setExtendInfo(bizPayOption1ExtendInfo);
            } else if (bizPayOptionBill2 != null) {
                Map<String, String> bizPayOption2ExtendInfo = bizPayOptionBill2.getExtendInfo();
                bizPayOption2ExtendInfo.put(ADD_MONEY_SURCHARGE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
                bizPayOptionBill2.setExtendInfo(bizPayOption2ExtendInfo);
            } else if (bizPayOptionBill1 != null) {
                Map<String, String> bizPayOption1ExtendInfo = bizPayOptionBill1.getExtendInfo();
                bizPayOption1ExtendInfo.put(ADD_MONEY_SURCHARGE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
                bizPayOptionBill1.setExtendInfo(bizPayOption1ExtendInfo);
            }
            bizPayRequest.getExtInfo().put(THIRD_PARTY_CONV_FEE, ADD_MONEY_SURCHARGE_FLAG_TRUE);
            bizPayRequest.getExtInfo().put(THIRD_PARTY_CONV_FEE_AMOUNT,
                    flowTransBean.getWorkFlowBean().getChargeAmount());
        }

        LOGGER.info("BizPayRequest {}", bizPayRequest);
        return bizPayRequest;
    }

    private void updatePayOptionBillsForCoft(WorkFlowTransactionBean flowTransBean,
            List<BizPayOptionBill> payOptionBills) {
        String lastFourDigits = flowTransBean.getWorkFlowBean().isCoftTokenTxn() ? flowTransBean.getWorkFlowBean()
                .getCardTokenInfo().getCardSuffix() : flowTransBean.getWorkFlowBean().getLastFourDigits();
        payOptionBills.forEach(payOptionBill -> payOptionBill.setLastFourDigits(lastFourDigits));
    }

    private void setVanInfoForCheckoutFlow(BizPayOptionBill bizPayOptionBill1, BizPayOptionBill bizPayOptionBill2,
            WorkFlowRequestBean workFlowRequestBean) {
        LOGGER.info("Setting VanInfo {}", workFlowRequestBean.getVanInfo());
        if (workFlowRequestBean.getVanInfo() != null) {
            if (bizPayOptionBill1 != null && bizPayOptionBill2 == null) {
                bizPayOptionBill1.setPrepaidCard(workFlowRequestBean.isPrepaidCard());
                bizPayOptionBill1.setVanInfo(workFlowRequestBean.getVanInfo());
            }

            if (bizPayOptionBill2 != null) {
                bizPayOptionBill2.setPrepaidCard(workFlowRequestBean.isPrepaidCard());
                bizPayOptionBill2.setVanInfo(workFlowRequestBean.getVanInfo());
            }
        }
    }

    private void setDeepIntegrationFieldsToPassThroughExtendedInfo(WorkFlowTransactionBean flowTransBean,
            Map<String, String> passThroughExtendInfoMap) {
        try {
            final Map<String, String> extendedInfo = flowTransBean.getWorkFlowBean().getEnvInfoReqBean()
                    .getExtendInfo();
            final Map<String, String> riskExtendInfo = flowTransBean.getWorkFlowBean().getRiskExtendedInfo();
            if (extendedInfo == null) {
                LOGGER.info("ExtendInfo is null");
            } else {
                if (extendedInfo.containsKey(LATITUDE))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CUSTOMER_LAT, extendedInfo.get(LATITUDE));
                if (extendedInfo.containsKey(LONGITUDE))
                    passThroughExtendInfoMap
                            .put(TheiaConstant.RequestParams.CUSTOMER_LONG, extendedInfo.get(LONGITUDE));
                if (extendedInfo.containsKey(CLIENT_IP))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.SOURCE_IP, extendedInfo.get(CLIENT_IP));
                if (extendedInfo.containsKey(OS_VERSION))
                    passThroughExtendInfoMap.put(OS_VERSION, extendedInfo.get(TheiaConstant.RequestParams.OS_VERSION));
                if (extendedInfo.containsKey(TheiaConstant.RequestParams.DEVICE_ID_FOR_RISK))
                    passThroughExtendInfoMap.put(DEVICE_ID_FOR_RISK, extendedInfo.get(DEVICE_ID_FOR_RISK));
                if (extendedInfo.containsKey(OS_TYPE))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CLIENT_2, extendedInfo.get(OS_TYPE));
                if (extendedInfo.containsKey(PLATFORM))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CHANNEL_2, extendedInfo.get(PLATFORM));
                if (extendedInfo.containsKey(APP_VERSION))
                    passThroughExtendInfoMap.put(APP_VERSION, extendedInfo.get(APP_VERSION));
            }

            if (riskExtendInfo == null) {
                LOGGER.info("riskExtendInfo is null");
            } else {
                if (riskExtendInfo.containsKey(DEVICE_IDENTIFIER))
                    passThroughExtendInfoMap.put(DEVICE_IDENTIFIER,
                            riskExtendInfo.get(TheiaConstant.RequestParams.DEVICE_IDENTIFIER));
                if (riskExtendInfo.containsKey(NETWORK_TYPE))
                    passThroughExtendInfoMap.put(NETWORK_TYPE,
                            riskExtendInfo.get(TheiaConstant.RequestParams.NETWORK_TYPE));
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while setting values to passThroughChannelInfoMap {}", e);
        }
    }

    private void setSdkTypeToExtendInfo(WorkFlowTransactionBean flowTransBean) {
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getSdkType())) {
            flowTransBean.getExtendInfo().put("sdkType", flowTransBean.getWorkFlowBean().getExtendInfo().getSdkType());
        }
    }

    private void setOfferDetailsInPaymentBizData(WorkFlowTransactionBean flowTransBean, PaymentBizInfo paymentBizInfo) {

        try {

            List<OfferDetail> offerDetails = new ArrayList<>();
            OfferDetail offerDetail = new OfferDetail();
            CheckoutPromoServiceResponse savedCheckoutResponse = flowTransBean.getWorkFlowBean()
                    .getCheckoutPromoServiceResponse();
            if (savedCheckoutResponse != null && savedCheckoutResponse.getData() != null && paymentBizInfo != null) {

                List<PromoSaving> savings = savedCheckoutResponse.getData().getSavings();

                offerDetail.setOfferCode(savedCheckoutResponse.getData().getPromocode());
                offerDetail.setOfferStatus(String.valueOf(savedCheckoutResponse.getData().getStatus()));

                if (CollectionUtils.isNotEmpty(savings)) {
                    PromoSaving promoSaving = savings.get(0);
                    if (promoSaving != null) {
                        if (BizConstant.DISCOUNT_PROMOCODE.equalsIgnoreCase(promoSaving.getRedemptionType())) {
                            offerDetail.setDiscount(String.valueOf(promoSaving.getSavings()));
                        } else if (CASHBACK_PROMOCODE.equalsIgnoreCase(promoSaving.getRedemptionType())) {
                            offerDetail.setDiscount(String.valueOf(promoSaving.getSavings()));
                        }
                    }
                }

                offerDetails.add(offerDetail);
                paymentBizInfo.setOfferDetails(offerDetails);
            }
        } catch (Exception e) {
            LOGGER.error("Error While Setting OfferDetails in PaymentBizdata"
                    + "In WorkFlowRequestCreationHelper.setOfferDetailsInPaymentBizData() - {} ", e.getMessage());
        }
    }

    private void enrichBizPayOptionBill(WorkFlowTransactionBean flowTransBean, BizPayOptionBill bizPayOptionBill1,
            EPayMethod payMethod) {
        if (EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.equals(payMethod)
                && flowTransBean.getWorkFlowBean().getAdvanceDepositId() != null) {
            bizPayOptionBill1.setPayerAccountNo(flowTransBean.getWorkFlowBean().getAdvanceDepositId());
        } else if (PaymentTypeIdEnum.GIFT_VOUCHER.getValue().equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            bizPayOptionBill1.setTemplateIds(flowTransBean.getWorkFlowBean().getMgvTemplateIds());
            bizPayOptionBill1.setChargeAmount("0");
        } else if (flowTransBean.getSubscriptionServiceResponse() != null
                && StringUtils.isNotBlank(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber())) {
            bizPayOptionBill1.setPayerAccountNo(flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber());
        } else if (null != flowTransBean.getUserDetails()) {
            bizPayOptionBill1.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());
        }
        if (flowTransBean.isAddMoneyPcfEnabled() && flowTransBean.getWorkFlowBean().isNativeAddMoney()) {
            String chargeAmount = fetchChargeAmountIfApplicable(flowTransBean);
            bizPayOptionBill1.setChargeAmount(chargeAmount);
            flowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);
        }
    }

    private BizPayOptionBill getBizPayOptionBill(WorkFlowTransactionBean flowTransBean, EPayMethod payMethod,
            String cacheCardToken) {
        BizPayOptionBill bizPayOptionBill1;
        bizPayOptionBill1 = new BizPayOptionBill(flowTransBean.getWorkFlowBean().getPayOption(), payMethod,
                flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean().getChargeAmount(),
                flowTransBean.getChannelInfo(), flowTransBean.getExtendInfo(), cacheCardToken);
        return bizPayOptionBill1;
    }

    private BizPayOptionBill getBizPayOptionBillNonSubscriptionFlow(WorkFlowTransactionBean flowTransBean,
            EPayMethod payMethod, String cacheCardToken) {
        BizPayOptionBill bizPayOptionBill1;
        bizPayOptionBill1 = new BizPayOptionBill(flowTransBean.getWorkFlowBean().getPayOption(), payMethod,
                flowTransBean.getWorkFlowBean().getTxnAmount(), flowTransBean.getWorkFlowBean().getChargeAmount(),
                flowTransBean.getChannelInfo(), flowTransBean.getExtendInfo(), flowTransBean.getWorkFlowBean()
                        .getVanInfo(), flowTransBean.getWorkFlowBean().getTpvInfos(), cacheCardToken);
        return bizPayOptionBill1;
    }

    private boolean isSubscriptionRenewalCase(WorkFlowTransactionBean flowTransBean) {
        boolean result = ERequestType.SUBSCRIPTION_RENEWAL == flowTransBean.getWorkFlowBean().getRequestType();
        LOGGER.info("isSubscriptionRenewalCase: {}", result);
        return result;
    }

    private boolean isBankMandateCase(WorkFlowTransactionBean flowTransBean) {

        if ((null != flowTransBean.getSubscriptionServiceResponse() && SubsPaymentMode.BANK_MANDATE
                .equals(flowTransBean.getSubscriptionServiceResponse().getPaymentMode()))
                || PaymentTypeIdEnum.BANK_MANDATE.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            return true;
        }
        return false;
    }

    public BizAoaPayRequest createBizAoaPayRequest(final WorkFlowTransactionBean flowTransBean)
            throws PaytmValidationException {
        final List<BizAoaPayOptionBill> aoaPayOptionBills = new ArrayList<>();

        Map<String, String> channelInfo = new HashMap<>();
        channelInfo.putAll(buildDefaultChannelInfoForAoa());
        if (flowTransBean.getChannelInfo() != null)
            channelInfo.putAll(flowTransBean.getChannelInfo());

        BizAoaPayOptionBill bizPayOptionBill1 = null;
        cacheDetailsForWalletAndUPIForInsta(flowTransBean.getWorkFlowBean());
        // setting direct pass through info
        setPassThroughChannelExtendedInfo(flowTransBean, channelInfo);
        nativeCoreService.ceatePassThroughExtendedInfoNativeUPI(flowTransBean, channelInfo);
        setRedisKeyForQueryParams(flowTransBean.getWorkFlowBean());
        // Passing expiry time in passthroughExtendInfo In UpiCollectPayments
        if (isUpiCollectPayment(flowTransBean)) {
            setPassThroughExtendInfoForUpiCollect(flowTransBean, channelInfo);
        } else if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            setPassThroughExtendInfoForOfflineUPI(flowTransBean, channelInfo);
        }

        // passing merchant dispaly name in channelinfo
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && !StringUtils.isBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName())) {
            channelInfo.put(ExtendedInfoKeys.MERCHANT_DISPLAY_NAME, flowTransBean.getWorkFlowBean().getExtendInfo()
                    .getMerchantName());
        }
        EPayMethod payMethod = EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod());
        if (payMethod == null) {
            payMethod = EPayMethod.getPayMethodByOldName(flowTransBean.getWorkFlowBean().getPaymentTypeId());
        }

        String cacheCardToken = "";

        if (StringUtils.isNotBlank(flowTransBean.getCacheCardToken())) {
            cacheCardToken = flowTransBean.getCacheCardToken();
        }
        if (null == flowTransBean.getExtendInfo()) {
            Map<String, String> extInfo = new HashMap<>();
            prepareExtendedInfo(flowTransBean, extInfo);
            flowTransBean.setExtendInfo(extInfo);
        } else {
            prepareExtendedInfo(flowTransBean, flowTransBean.getExtendInfo());
        }

        enrichInExtendInfoForSeamlessPayment(flowTransBean);

        ExtendedInfoRequestBean extendInfo = createExtendedInfo(flowTransBean);
        setSubWalletAmountToExtendedInfo(extendInfo, flowTransBean);
        Map<String, String> extendeInfoMap = AlipayRequestUtils.getExtendeInfoMap(extendInfo);
        if (flowTransBean.getExtendInfo() == null) {
            flowTransBean.setExtendInfo(extendeInfoMap);
        } else {
            flowTransBean.getExtendInfo().putAll(extendeInfoMap);
        }

        setParamInChannelInfo(flowTransBean, channelInfo);
        setNickname(flowTransBean);
        if (EPayMode.NONE.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {
            setPayOptionForUpiPushExpressOrIntent(flowTransBean);
            String payOption = null;
            if (payMethod == EPayMethod.WALLET) {
                payOption = TheiaConstant.BasicPayOption.AOA_WALLET_PAY_OPTION;
            } else {
                payOption = flowTransBean.getWorkFlowBean().getPayOption();
            }
            bizPayOptionBill1 = new BizAoaPayOptionBill(payOption, payMethod, flowTransBean.getWorkFlowBean()
                    .getTxnAmount(), cacheCardToken, flowTransBean.getChannelInfo(), flowTransBean.getExtendInfo());

        }
        bizPayOptionBill1.setChannelInfo(flowTransBean.getChannelInfo());
        bizPayOptionBill1.setExtendInfo(flowTransBean.getExtendInfo());
        aoaPayOptionBills.add(bizPayOptionBill1);

        if (flowTransBean.getWorkFlowBean().isPrepaidCard()) {
            bizPayOptionBill1.setPrepaidCard(flowTransBean.getWorkFlowBean().isPrepaidCard());
        }
        if (flowTransBean.getWorkFlowBean().getFeeRateFactors() != null) {
            bizPayOptionBill1.setFeeRateFactorsInfo(flowTransBean.getWorkFlowBean().getFeeRateFactors()
                    .getFeeRateFactorsMap());
        }
        BizChannelPreference bizChannelPreference = null;

        bizChannelPreference = calculateChannelPreferenceForAoaMerchant(flowTransBean);

        if (ff4jUtils.isCOFTEnabledOnAOA(flowTransBean.getWorkFlowBean().getPaytmMID())
                && StringUtils.isBlank(flowTransBean.getTransID()) && flowTransBean.getWorkFlowBean() != null) {
            flowTransBean.setTransID(flowTransBean.getWorkFlowBean().getTransID());
        }

        BizAoaPayRequest aoaPayRequest = new BizAoaPayRequest(flowTransBean.getTransID(), aoaPayOptionBills,
                RequestIdGenerator.generateRequestId(), flowTransBean.getExtendInfo(), bizChannelPreference);
        if (aoaPayRequest.getEnvInfo() == null) {
            try {
                final EnvInfo envInfo = AlipayRequestUtils
                        .createEnvInfo(flowTransBean.getEnvInfoReqBean() != null ? flowTransBean.getEnvInfoReqBean()
                                : (flowTransBean.getWorkFlowBean() != null ? flowTransBean.getWorkFlowBean()
                                        .getEnvInfoReqBean() : null));
                aoaPayRequest.setEnvInfo(envInfo);
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Error while creating envInfo ", e);
            }
        }

        aoaPayRequest.setExtInfo(flowTransBean.getExtendInfo());
        if (ERequestType.DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || SEAMLESS_3D_FORM.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || UNI_PAY.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || NATIVE_SUBSCRIPTION.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
            setWorkflowToExtendedInfo(flowTransBean);
        }
        return aoaPayRequest;
    }

    private void cacheDetailsForWalletAndUPIForInsta(WorkFlowRequestBean workFlowBean) {
        if (EPayMethod.WALLET.getMethod().equals(workFlowBean.getPayMethod())
                || EPayMethod.UPI.getMethod().equals(workFlowBean.getPayMethod())) {
            String cacheKey = fetchAOADataKey(workFlowBean);
            if (workFlowBean.getExtendInfo() != null && workFlowBean.getExtendInfo().getAdditionalInfo() != null) {
                Map<String, Object> extendInfo = new HashMap<>();
                String additionalInfo = workFlowBean.getExtendInfo().getAdditionalInfo();
                additionalInfo = StringUtils.replace(additionalInfo, "|orderAlreadyCreated:true", "");
                extendInfo.put("additionalInfo", additionalInfo);
                theiaTransactionalRedisUtil.hset(cacheKey, "EXTEND_INFO", extendInfo, 900);

            }
            if (workFlowBean.getCreditBlock() != null) {
                theiaTransactionalRedisUtil.hset(cacheKey, "CREDIT_BLOCK", workFlowBean.getCreditBlock(), 900);
            }
        }

    }

    public String fetchAOADataKey(WorkFlowRequestBean workFlowRequestBean) {
        return "AOA_" + workFlowRequestBean.getPaytmMID() + "_" + workFlowRequestBean.getOrderID();
    }

    private void prepareExtendedInfo(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        if (flowTransBean.getUserDetails() != null) {
            extInfo.put(ExtendedInfoKeys.PAYTM_USER_ID, flowTransBean.getUserDetails().getUserId());
            flowTransBean.getWorkFlowBean().getExtendInfo().setPaytmUserId(flowTransBean.getUserDetails().getUserId());
            // Add money destination received from Wallet
            if (ADDMONEY_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || TOPUP_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || flowTransBean.getWorkFlowBean().isNativeAddMoney()) {
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaytmMID())
                        && ff4JUtil.isFeatureEnabled(ENABLE_ADD_MONEY_SOURCE_IN_CONSULT, flowTransBean
                                .getWorkFlowBean().getPaytmMID())) {
                    extInfo.put(ADD_MONEY_SOURCE, THIRD_PARTY.getValue());
                }
                if (StringUtils.isNotBlank(flowTransBean.getAddMoneyDestination())) {
                    extInfo.put(ExtendedInfoKeys.ADD_MONEY_DESTINATION, flowTransBean.getAddMoneyDestination());
                    extInfo.put(ExtendedInfoKeys.TARGET_PHONE_NO, flowTransBean.getWorkFlowBean().getTargetPhoneNo());
                }
                if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled()) {
                    String payMethod = flowTransBean.getWorkFlowBean().getPayMethod();
                    String userId = flowTransBean.getUserDetails().getUserId();
                    if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardIndexNo())) {
                        extInfo.put(ExtendedInfoKeys.IS_CARD_TOKEN_REQUIRED,
                                Boolean.toString(flowTransBean.getWorkFlowBean().isCardTokenRequired()));
                        extInfo.put(ExtendedInfoKeys.CARD_INDEX_NUMBER, flowTransBean.getWorkFlowBean()
                                .getCardIndexNo());
                    }
                    if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardHash())) {
                        extInfo.put(ExtendedInfoKeys.CARD_HASH, flowTransBean.getWorkFlowBean().getCardHash());
                    }
                    extInfo.put(ExtendedInfoKeys.PAYMENT_MODE, flowTransBean.getWorkFlowBean().getPaymentTypeId());
                    if (PayMethod.DEBIT_CARD.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod())) {
                        extInfo.put(ExtendedInfoKeys.PAYMENT_MODE, PayMethod.DEBIT_CARD.getOldName());
                    }
                }
            }
        }
        if (QR_SUBSCRIPTION.equals(flowTransBean.getWorkFlowBean().getType())) {
            extInfo.put(ExtendedInfoKeys.CURRENT_SUBSCRIPTION_ID, flowTransBean.getWorkFlowBean().getExtendInfo()
                    .getCurrentSubscriptionId());
            extInfo.put("custID", flowTransBean.getWorkFlowBean().getExtendInfo().getCustID());
            extInfo.put("CUST_ID", flowTransBean.getWorkFlowBean().getExtendInfo().getCustID());
            extInfo.put("custId", flowTransBean.getWorkFlowBean().getExtendInfo().getCustID());
            extInfo.put("customerId", flowTransBean.getWorkFlowBean().getExtendInfo().getCustID());
            extInfo.put("SUBS_PAY_MODE", PayMethod.UPI.getMethod());
            extInfo.put(COLLECT_API_INVOKE, "true");
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getDummyMerchantId())) {
                extInfo.put(DUMMY_MERCHANT_ID, flowTransBean.getWorkFlowBean().getExtendInfo().getDummyMerchantId());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getDummyOrderId())) {
                extInfo.put(DUMMY_ORDER_ID, flowTransBean.getWorkFlowBean().getExtendInfo().getDummyOrderId());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getPaytmMerchantId())) {
                extInfo.put(ExtendedInfoKeys.PAYTM_MERCHANT_ID, flowTransBean.getWorkFlowBean().getExtendInfo()
                        .getPaytmMerchantId());
            }
        }

        if (SEAMLESS_3D_FORM.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
            extInfo.put(ExtendedInfoKeys.UDF1, flowTransBean.getWorkFlowBean().getExtendInfo().getUdf1());
        }

        extInfo.put(ExtendedInfoKeys.ALIPAY_MERCHANT_ID, flowTransBean.getWorkFlowBean().getAlipayMID());
        extInfo.put(ExtendedInfoKeys.PAYTM_MERCHANT_ID, flowTransBean.getWorkFlowBean().getPaytmMID());
        extInfo.put(ExtendedInfoKeys.TOTAL_TXN_AMOUNT, flowTransBean.getWorkFlowBean().getTxnAmount());
        extInfo.put(ExtendedInfoKeys.PRODUCT_CODE, flowTransBean.getWorkFlowBean().getExtendInfo().getProductCode());
        extInfo.put(ExtendedInfoKeys.CALL_BACK_URL, flowTransBean.getWorkFlowBean().getCallBackURL());
        extInfo.put(ExtendedInfoKeys.MERCHANT_TRANS_ID, flowTransBean.getWorkFlowBean().getOrderID());
        extInfo.put(ExtendedInfoKeys.REQUEST_TYPE, flowTransBean.getWorkFlowBean().getRequestType().getType());
        extInfo.put(ExtendedInfoKeys.WORKFLOW, flowTransBean.getWorkFlowBean().getWorkFlow());
        extInfo.put(ExtendedInfoKeys.GUEST_TOKEN, flowTransBean.getWorkFlowBean().getGuestToken());
        if (flowTransBean.getWorkFlowBean().isPushDataToDynamicQR()) {
            extInfo.put(BizConstant.PUSH_DATA_TO_DYNAMIC_QR_KEY,
                    String.valueOf(flowTransBean.getWorkFlowBean().isPushDataToDynamicQR()));
        }
        if (flowTransBean.getWorkFlowBean().isUpiConvertedToAddNPay()) {
            String upiToAddnPayPromoCode = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(BizConstant.UPI_TO_ADDNPAY_PROMO_CODE);
            if (null != upiToAddnPayPromoCode) {
                extInfo.put(BizConstant.UPI_TO_ADDNPAY_PROMO_CODE_KEY, com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(BizConstant.UPI_TO_ADDNPAY_PROMO_CODE, null));
            } else {
                LOGGER.info("Could not load the property upi.to.addnpay.promo.code hence upiToAddnPayPromoCode is not set");
            }
        }
        if (ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(flowTransBean.getWorkFlowBean()
                .getRequestType().getType())) {
            /*
             * if
             * (!StringUtils.isEmpty(flowTransBean.getWorkFlowBean().getExtendInfo
             * ().getCardIndexNo())) {
             * extInfo.put(ExtendedInfoKeys.CARD_INDEX_NUMBER,
             * flowTransBean.getWorkFlowBean().getExtendInfo()
             * .getCardIndexNo());
             * extInfo.put(ExtendedInfoKeys.IS_CARD_TOKEN_REQUIRED,
             * Boolean.toString
             * (flowTransBean.getWorkFlowBean().isCardTokenRequired())); }
             */
            if (!StringUtils.isEmpty(flowTransBean.getWorkFlowBean().getExtendInfo().getPromoCode())) {
                extInfo.put(ExtendedInfoKeys.PROMO_CODE, flowTransBean.getWorkFlowBean().getExtendInfo().getPromoCode());
                extInfo.put(ExtendedInfoKeys.PROMO_RESPONSE_CODE, flowTransBean.getWorkFlowBean().getExtendInfo()
                        .getPromoResponseCode());
                extInfo.put(ExtendedInfoKeys.PROMO_APPLY_RESULT_STATUS, flowTransBean.getWorkFlowBean().getExtendInfo()
                        .getPromoApplyResultStatus());
            }
            if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled()
                    && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardHash())) {
                extInfo.put(ExtendedInfoKeys.CARD_HASH, flowTransBean.getWorkFlowBean().getCardHash());
            }
        }
        if (flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() != null
                && EPayMode.ADDANDPAY.name()
                        .equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid().name())) {

            extInfo.put(ExtendedInfoKeys.PAYMENT_MODE, flowTransBean.getWorkFlowBean().getPaymentTypeId());

            String payMethod = flowTransBean.getWorkFlowBean().getPayMethod();
            String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails()
                    .getUserId() : null;
            if ((EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.EMI.getMethod().equals(payMethod) || EPayMethod.EMI_DC.getMethod().equals(payMethod))
                    && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                            .getWorkFlowBean().getPaytmMID(), null, userId, false)) {
                extInfo.put(ExtendedInfoKeys.CARD_INDEX_NUMBER, flowTransBean.getWorkFlowBean().getGcin());
            } else {
                extInfo.put(ExtendedInfoKeys.CARD_INDEX_NUMBER, flowTransBean.getWorkFlowBean().getCardIndexNo());
            }

            extInfo.put(ExtendedInfoKeys.CARD_HASH, flowTransBean.getWorkFlowBean().getCardHash());
            extInfo.put(ExtendedInfoKeys.BANK_ID, flowTransBean.getWorkFlowBean().getBankName());
            extInfo.put(ExtendedInfoKeys.ADD_MONEY_SOURCE, AddMoneySourceEnum.THIRD_PARTY.getValue());

            if (flowTransBean.getWorkFlowBean().getBinDetail() != null) {
                extInfo.put(ExtendedInfoKeys.PREPAID_CARD,
                        String.valueOf(flowTransBean.getWorkFlowBean().getBinDetail().isPrepaidCard()));
                extInfo.put(ExtendedInfoKeys.CORPORATE_CARD,
                        String.valueOf(flowTransBean.getWorkFlowBean().getBinDetail().isCorporateCard()));
                extInfo.put(
                        ExtendedInfoKeys.BIN_NUMBER,
                        flowTransBean.getWorkFlowBean().getBinDetail().getBin() != null ? String.valueOf(flowTransBean
                                .getWorkFlowBean().getBinDetail().getBin()) : null);
            }

        }

        enrichCobrandedCardData(flowTransBean, extInfo);
        // To set REQUEST_TYPE when UPI_POS_ORDER is coming
        // PGP-21651,PGP-21902
        updateExtendInfoForPosOrder(flowTransBean, extInfo);

        /***
         * [PGP-25132] setting user mobile no and user email
         */
        updateMobNoAndEmailInExtendInfo(flowTransBean, extInfo);
        updateExtendInfoForOfflineIntent(flowTransBean, extInfo);

        extInfo.put(ExtendedInfoKeys.FROMAOAMERCHANT,
                String.valueOf(flowTransBean.getWorkFlowBean().isFromAoaMerchant()));
        extInfo.put(ExtendedInfoKeys.FROMAOAMERCHANT,
                String.valueOf(flowTransBean.getWorkFlowBean().isFromAoaMerchant()));

        if (EMI.equalsIgnoreCase(flowTransBean.getWorkFlowBean().getPayMethod())) {
            try {
                extInfo.put(EMI_INFO, JsonMapper.mapObjectToJson(getEmiInfoFromTransBean(flowTransBean)));
            } catch (Exception e) {
                LOGGER.error("Could not convert and set emiInfo in ExtendInfo");
            }
        }
        updateExtendInfoForPg2(flowTransBean, extInfo);
    }

    private void updateExtendInfoForPg2(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        Routes routes = getRoute(flowTransBean, "cashierPay");
        if (routes != null)
            extInfo.put(FacadeConstants.ExtendInfo.ROUTE, routes.getName());
    }

    private Map<String, String> getEmiInfoFromTransBean(WorkFlowTransactionBean flowTransBean) {

        Map<String, String> emiInfo = new HashMap<String, String>();
        if (flowTransBean.getWorkFlowBean() == null) {
            return emiInfo;
        }
        ValidateResponse validateResponse = flowTransBean.getWorkFlowBean().getEmiSubventionOfferCheckoutReqData();

        String tempCardNo = "";
        if (flowTransBean.getWorkFlowBean().getCardNo() != null) {
            tempCardNo = flowTransBean.getWorkFlowBean().getCardNo();
        }

        String cardNo = "";
        try {
            // last four digits is stored in cardNo
            if (tempCardNo.length() <= 6) {
                // check and set last four digits
                if (null != flowTransBean.getSavedCard() && null != flowTransBean.getSavedCard().getCardNumber()
                        && flowTransBean.getSavedCard().getCardNumber().length() > 6) {
                    cardNo = flowTransBean.getSavedCard().getCardNumber()
                            .substring(flowTransBean.getSavedCard().getCardNumber().length() - 4);
                }
                if (StringUtils.isBlank(cardNo)) {
                    String custId = "";
                    String userId = "";
                    String mmid = "";

                    if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null) {
                        mmid = flowTransBean.getWorkFlowBean().getPaymentRequestBean().getMid();
                    }
                    if (flowTransBean.getSavedCard() != null) {
                        custId = flowTransBean.getSavedCard().getCustId();
                    }
                    if (flowTransBean.getUserDetails() != null) {
                        userId = flowTransBean.getUserDetails().getUserId();
                    }
                    if (StringUtils.isNotBlank(custId) && StringUtils.isNotBlank(userId)
                            && StringUtils.isNotBlank(mmid)) {
                        List<SavedCardVO> list = savedCardsService.getAllSavedCardsByMidCustIdUserId(mmid, custId,
                                userId).getResponseData();
                        String cardIndexNo = flowTransBean.getCardIndexNo();
                        if (StringUtils.isBlank(cardIndexNo)) {
                            cardIndexNo = flowTransBean.getWorkFlowBean().getCardIndexNo();
                        }
                        if (null != list) {
                            for (SavedCardVO card : list) {
                                if (card != null && card.getCardIndexNumber() != null
                                        && card.getCardIndexNumber().equals(cardIndexNo)) {
                                    if (card.getCardNumber() != null && card.getCardNumber().length() > 6) {
                                        cardNo = card.getCardNumber().substring(card.getCardNumber().length() - 4);
                                    }
                                } else if (card != null
                                        && ((card.getCardNumber() != null && tempCardNo.length() >= 6 && card
                                                .getCardNumber().substring(0, 6).equals(tempCardNo)) || (card
                                                .getFirstSixDigit() != null && tempCardNo.length() >= 6 && card
                                                .getFirstSixDigit().toString().equals(tempCardNo.substring(0, 6))))) {
                                    if (StringUtils.isNotBlank(card.getCardNumber())) {
                                        cardNo = card.getCardNumber().substring(card.getCardNumber().length() - 4);
                                    } else if (StringUtils.isNotBlank(card.getLastFourDigit().toString())) {
                                        cardNo = "0000" + card.getLastFourDigit().toString();
                                        cardNo = cardNo.substring(cardNo.length() - 4);

                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                cardNo = tempCardNo.substring(tempCardNo.length() - 4);
            }
        } catch (Exception e) {
            LOGGER.error("Could not get last four digits of card no. ", e);
        }
        String acquiringBank = flowTransBean.getWorkFlowBean().getInstId();
        String cardType = flowTransBean.getWorkFlowBean().getCardType();

        String merchantName = "";
        if (flowTransBean.getWorkFlowBean().getExtendInfo() != null) {
            merchantName = flowTransBean.getWorkFlowBean().getExtendInfo().getMerchantName();
        }
        String cardIssuer = flowTransBean.getWorkFlowBean().getInstId();

        String mid = "";
        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null) {
            mid = flowTransBean.getWorkFlowBean().getPaymentRequestBean().getMid();
        }
        String orderId = flowTransBean.getWorkFlowBean().getOrderID();
        String txnId = flowTransBean.getTransID();

        String emiPlanId = null;
        if (null != flowTransBean.getWorkFlowBean().getPaymentRequestBean()) {
            emiPlanId = flowTransBean.getWorkFlowBean().getPaymentRequestBean().getEmiPlanID();
        }
        if (StringUtils.isBlank(emiPlanId)) {
            if (null != flowTransBean.getChannelInfo()
                    && flowTransBean.getChannelInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_PLAN_ID)) {
                emiPlanId = flowTransBean.getChannelInfo().get(BizConstant.DataEnrichmentKey.EMI_PLAN_ID);
            } else if (flowTransBean.getExtendInfo() != null
                    && flowTransBean.getExtendInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_PLAN_ID)) {
                emiPlanId = flowTransBean.getExtendInfo().get(BizConstant.DataEnrichmentKey.EMI_PLAN_ID);
            }
        }

        String emiMonths = "";
        if (validateResponse != null && validateResponse.getInterval() != null) {
            emiMonths = validateResponse.getInterval().toString();
        } else if (null != flowTransBean.getChannelInfo()
                && flowTransBean.getChannelInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_MONTHS)) {
            emiMonths = flowTransBean.getChannelInfo().get(BizConstant.DataEnrichmentKey.EMI_MONTHS);
        } else if (flowTransBean.getExtendInfo() != null
                && flowTransBean.getExtendInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_MONTHS)) {
            emiMonths = flowTransBean.getExtendInfo().get(BizConstant.DataEnrichmentKey.EMI_MONTHS);
        }

        String emiIntrestRate = "";
        if (validateResponse != null && validateResponse.getRate() != null) {
            emiIntrestRate = validateResponse.getRate().toString();
        } else if (null != flowTransBean.getChannelInfo()
                && flowTransBean.getChannelInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_INTEREST)) {
            emiIntrestRate = flowTransBean.getChannelInfo().get(BizConstant.DataEnrichmentKey.EMI_INTEREST);
        } else if (flowTransBean.getExtendInfo() != null
                && flowTransBean.getExtendInfo().containsKey(BizConstant.DataEnrichmentKey.EMI_INTEREST)) {
            emiIntrestRate = flowTransBean.getExtendInfo().get(BizConstant.DataEnrichmentKey.EMI_INTEREST);
        }
        String emiAmount = "";
        if (validateResponse != null && validateResponse.getEmi() != null) {
            emiAmount = validateResponse.getEmi().toString();
        } else if (null != flowTransBean.getChannelInfo()
                && flowTransBean.getChannelInfo().containsKey(DataEnrichmentKey.EMI_AMOUNT)) {
            emiAmount = flowTransBean.getChannelInfo().get(DataEnrichmentKey.EMI_AMOUNT);
        } else if (flowTransBean.getExtendInfo() != null
                && flowTransBean.getExtendInfo().containsKey(DataEnrichmentKey.EMI_AMOUNT)) {
            emiAmount = flowTransBean.getExtendInfo().get(DataEnrichmentKey.EMI_AMOUNT);
        }

        emiInfo.put(BizConstant.DataEnrichmentKey.EMI_PLAN_ID, emiPlanId);
        emiInfo.put(BizConstant.DataEnrichmentKey.EMI_MONTHS, emiMonths);
        emiInfo.put(DataEnrichmentKey.EMI_INTEREST, emiIntrestRate);
        emiInfo.put(DataEnrichmentKey.EMI_AMOUNT, emiAmount);
        emiInfo.put(DataEnrichmentKey.CARD_TYPE, cardType);
        emiInfo.put(DataEnrichmentKey.MERCHANT_NAME, merchantName);
        emiInfo.put(DataEnrichmentKey.MASKED_CARD_NO, cardNo);
        emiInfo.put(DataEnrichmentKey.CARD_TOKEN, ""); // card token related to
                                                       // coft related changes
                                                       // to be added in future.
        emiInfo.put(DataEnrichmentKey.AQUIRING_BANK, acquiringBank);
        emiInfo.put(DataEnrichmentKey.CARD_ISSUER, cardIssuer);
        emiInfo.put(BizConstant.MID, mid);
        emiInfo.put(BizConstant.ORDER_ID, orderId);
        emiInfo.put(BizConstant.TXN_ID, txnId);

        return emiInfo;

    }

    private void updateExtendInfoForOfflineIntent(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        ExtendedInfoRequestBean extendInfo = flowTransBean.getWorkFlowBean().getExtendInfo();

        if (StringUtils.isNotBlank(extendInfo.getPayerName())) {
            extInfo.put(ExtendedInfoKeys.PAYER_NAME, extendInfo.getPayerName());
        }
        if (StringUtils.isNotBlank(extendInfo.getPayerPSP())) {
            extInfo.put(ExtendedInfoKeys.PAYER_PSP, extendInfo.getPayerPSP());
        }
    }

    public ConsultWalletLimitsRequest createConsultWalletLimitRequest(final WorkFlowTransactionBean flowTransBean) {
        Double addMoneyAmount;
        if (flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY) {
            String walletAmount = flowTransBean.getWorkFlowBean().getWalletAmount();
            Double walletAmountToDeduct = StringUtils.isNotBlank(walletAmount) ? Double.valueOf(walletAmount) : 0;
            addMoneyAmount = Double.valueOf(flowTransBean.getWorkFlowBean().getTxnAmount()) - walletAmountToDeduct;
            if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().getAddOneRupee()) && addMoneyAmount < 100D) {
                addMoneyAmount = 100D;
            }
        } else {
            addMoneyAmount = Double.valueOf(flowTransBean.getWorkFlowBean().getTxnAmount());
        }
        final ConsultWalletLimitsRequest walletConsultRequest = new ConsultWalletLimitsRequest(addMoneyAmount,
                flowTransBean.getUserDetails().getUserId(), flowTransBean.getWorkFlowBean().getOrderID());
        walletConsultRequest.setTargetPhoneNo(flowTransBean.getWorkFlowBean().getTargetPhoneNo());
        walletConsultRequest.setGvFlag(flowTransBean.getWorkFlowBean().isGvFlag());
        walletConsultRequest.setTransitWallet(flowTransBean.getWorkFlowBean().isTransitWallet());
        walletConsultRequest.setTotalTxnAmount(Double.valueOf(flowTransBean.getWorkFlowBean().getTxnAmount()));
        walletConsultRequest
                .setAddAndPay(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY ? true
                        : false);
        walletConsultRequest.setCardHash(flowTransBean.getWorkFlowBean().getCardHash());
        if (StringUtils.equals(EPayMethod.NET_BANKING.getMethod(), flowTransBean.getWorkFlowBean().getPayMethod())) {
            walletConsultRequest.setCardIndexNo(flowTransBean.getWorkFlowBean().getBankCode());
        } else {
            String payMethod = flowTransBean.getWorkFlowBean().getPayMethod();
            String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails()
                    .getUserId() : null;
            if ((EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.EMI.getMethod().equals(payMethod) || EPayMethod.EMI_DC.getMethod().equals(payMethod))
                    && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                            .getWorkFlowBean().getPaytmMID(), null, userId, false)) {
                walletConsultRequest.setCardIndexNo(flowTransBean.getWorkFlowBean().getGcin());
            } else {
                walletConsultRequest.setCardIndexNo(flowTransBean.getWorkFlowBean().getCardIndexNo());
            }
        }
        walletConsultRequest.setPaymentMode(flowTransBean.getWorkFlowBean().getPayMethod());
        walletConsultRequest.setSource(flowTransBean.getWorkFlowBean().getAddMoneySource());
        walletConsultRequest.setCorporateCard(flowTransBean.getWorkFlowBean().isCorporateCard());
        walletConsultRequest.setPrepaidCard(flowTransBean.getWorkFlowBean().isPrepaidCard());
        walletConsultRequest.setBankId(flowTransBean.getWorkFlowBean().getBankName());
        LOGGER.debug("Created request for consult wallet limit as ::{}", walletConsultRequest);
        return walletConsultRequest;
    }

    public ConsultWalletLimitsRequest createConsultWalletLimitRequest(String userId, String orderId, String txnAmnt,
            String addMoneySource) {
        ConsultWalletLimitsRequest walletConsultRequest = new ConsultWalletLimitsRequest(Double.valueOf(txnAmnt),
                userId, orderId);
        walletConsultRequest.setSource(addMoneySource);
        LOGGER.debug("Created request for consult wallet limit as ::{}", walletConsultRequest);
        return walletConsultRequest;
    }

    public VerifyLoginRequestBizBean createVerifyLoginRequestBean(final WorkFlowTransactionBean flowTransBean) {
        final VerifyLoginRequestBizBean verfifyLoginRequestBean = new VerifyLoginRequestBizBean();
        verfifyLoginRequestBean.setoAuthCode(flowTransBean.getWorkFlowBean().getoAuthCode());
        verfifyLoginRequestBean.setClientID(flowTransBean.getWorkFlowBean().getOauthClientId());
        verfifyLoginRequestBean.setSecretKey(flowTransBean.getWorkFlowBean().getOauthSecretKey());
        verfifyLoginRequestBean.setmId(flowTransBean.getWorkFlowBean().getPaytmMID());
        verfifyLoginRequestBean.setCustId(flowTransBean.getWorkFlowBean().getCustID());
        verfifyLoginRequestBean.setStoreCardPrefEnabled(flowTransBean.getWorkFlowBean().isStoreCardPrefEnabled());
        LOGGER.debug("Created request for VerifyLogin as ::{}", verfifyLoginRequestBean);
        return verfifyLoginRequestBean;
    }

    public PGPlusWalletDecisionMakerRequestBizBean createPGPlusWalletRequestBean(
            final WorkFlowTransactionBean flowTransBean) {
        String txnAmount = getTxnAmountForConsult(flowTransBean);
        final PGPlusWalletDecisionMakerRequestBizBean requestBean = new PGPlusWalletDecisionMakerRequestBizBean(
                flowTransBean.getMerchantViewConsult(), txnAmount, flowTransBean.getUserDetails().getUserId(),
                flowTransBean.getWorkFlowBean().getOrderID(), flowTransBean.getWorkFlowBean().getPaytmMID());
        // Added for offline flow
        requestBean.setLitePayviewConsultResponseBizBean(flowTransBean.getMerchantLiteViewConsult());

        // For GV
        requestBean.setGvFlag(flowTransBean.getWorkFlowBean().isGvFlag());
        requestBean.setTransitWallet(flowTransBean.getWorkFlowBean().isTransitWallet());
        requestBean.setTargetPhoneNo(flowTransBean.getWorkFlowBean().getTargetPhoneNo());
        LOGGER.debug("Created request for  PGPlus Wallet Decision As :::{}", requestBean);
        return requestBean;

    }

    private String getTxnAmountForConsult(final WorkFlowTransactionBean flowTransBean) {
        String txnAmount = flowTransBean.getWorkFlowBean().getTxnAmount();
        if (flowTransBean.getConsultFeeResponse() != null) {
            ConsultDetails consultDetails = flowTransBean.getConsultFeeResponse().getConsultDetails()
                    .get(EPayMethod.BALANCE);
            if (consultDetails != null) {
                txnAmount = AmountUtils.getTransactionAmountInPaise(consultDetails.getTotalTransactionAmount()
                        .toPlainString());
            }
        }
        return txnAmount;
    }

    public ConsultFeeRequest createConsultFeeRequest(final WorkFlowTransactionBean flowTransBean) {
        final ConsultFeeRequest consultFeeRequest = new ConsultFeeRequest();
        final BigDecimal txnAmount = new BigDecimal(flowTransBean.getWorkFlowBean().getTxnAmount());
        consultFeeRequest.setMerchantId(flowTransBean.getWorkFlowBean().getAlipayMID());
        consultFeeRequest.setTransactionAmount(txnAmount);
        consultFeeRequest.setTransactionType(flowTransBean.getWorkFlowBean().getRequestType());
        consultFeeRequest.setPayMethods(getPayMethodsForConsultCharges(flowTransBean));
        consultFeeRequest.setWalletBalance(fetchWalletBalance(flowTransBean.getMerchantViewConsult()));
        consultFeeRequest.setTransCreatedTime(flowTransBean.getTransCreatedTime());
        consultFeeRequest.setSlabBasedMDR(flowTransBean.getWorkFlowBean().isSlabBasedMDR());

        List<String> instId = new ArrayList<String>();

        if (!EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {
            EPayMethod payMethod = EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod());
            if (EPayMethod.DEBIT_CARD.equals(payMethod) || EPayMethod.CREDIT_CARD.equals(payMethod)) {
                instId.add(flowTransBean.getWorkFlowBean().getCardScheme());
                if (flowTransBean.getWorkFlowBean().getFeeRateFactors() == null) {
                    flowTransBean.getWorkFlowBean().setFeeRateFactors(new FeeRateFactors());
                }
                flowTransBean.getWorkFlowBean().getFeeRateFactors()
                        .setInstId(flowTransBean.getWorkFlowBean().getCardScheme());
            } else if (EPayMethod.EMI.equals(payMethod) || (EPayMethod.NET_BANKING.equals(payMethod))) {
                instId.add(flowTransBean.getWorkFlowBean().getBankCode());
                if (flowTransBean.getWorkFlowBean().getFeeRateFactors() == null) {
                    flowTransBean.getWorkFlowBean().setFeeRateFactors(new FeeRateFactors());
                }
                flowTransBean.getWorkFlowBean().getFeeRateFactors()
                        .setInstId(flowTransBean.getWorkFlowBean().getBankCode());
            }
        }

        consultFeeRequest.setInstId(instId);
        consultFeeRequest.setFeeRateFactors(Arrays.asList(flowTransBean.getWorkFlowBean().getFeeRateFactors()));
        flowTransBean.getWorkFlowBean().setRoute(Routes.PG2);
        consultFeeRequest.setRoute(Routes.PG2);
        consultFeeRequest.setMerchantId(flowTransBean.getWorkFlowBean().getPaytmMID());
        LOGGER.debug("Create Request object for consult fee response as ::{}", consultFeeRequest);
        return consultFeeRequest;
    }

    private List<EPayMethod> getPayMethodsForConsultCharges(final WorkFlowTransactionBean flowTransBean) {

        final List<EPayMethod> payMethods = new ArrayList<>();

        if (ERequestType.PAYTM_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE
                        .equals(flowTransBean.getWorkFlowBean().getRequestType())) {

            if (EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {

                payMethods.add(EPayMethod.BALANCE);

            } else if (EPayMode.HYBRID.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {

                if (ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE.equals(flowTransBean.getWorkFlowBean()
                        .getRequestType())) {

                    payMethods.add(EPayMethod.HYBRID_PAYMENT);
                    payMethods.add(EPayMethod.BALANCE);
                    payMethods.add(EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod()));

                } else {

                    payMethods.add(EPayMethod.HYBRID_PAYMENT);

                }

            } else {

                payMethods.add(EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod()));

            }

            return payMethods;

        }
        // Hack for Native

        if (flowTransBean.getMerchantViewConsult() != null) {
            for (final PayMethodViewsBiz payMethod : flowTransBean.getMerchantViewConsult().getPayMethodViews()) {
                try {
                    if (EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()).equals(EPayMethod.BALANCE)
                            && (flowTransBean.getAllowedPayMode() != null && !(EPayMode.ADDANDPAY.equals(flowTransBean
                                    .getAllowedPayMode())))) {
                        Integer balance = Integer.valueOf(payMethod.getPayChannelOptionViews().get(0)
                                .getBalanceChannelInfos().get(0).getAccountBalance());
                        Integer txnAmount = Integer.valueOf(flowTransBean.getWorkFlowBean().getTxnAmount());
                        boolean res = (balance.compareTo(txnAmount)) <= 0 ? true : false;
                        if (res)
                            continue;
                    }
                    payMethods.add(EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()));
                } catch (final Exception e) {
                    continue;
                }
            }
        } else if (flowTransBean.getMerchantLiteViewConsult() != null
                && flowTransBean.getMerchantLiteViewConsult().getPayMethodViews() != null) {
            for (final PayMethodViewsBiz payMethod : flowTransBean.getMerchantLiteViewConsult().getPayMethodViews()) {
                try {
                    if (EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()).equals(EPayMethod.BALANCE)
                            && (flowTransBean.getAllowedPayMode() != null && !(EPayMode.ADDANDPAY.equals(flowTransBean
                                    .getAllowedPayMode())))) {
                        Integer balance = Integer.valueOf(payMethod.getPayChannelOptionViews().get(0)
                                .getBalanceChannelInfos().get(0).getAccountBalance());
                        Integer txnAmount = Integer.valueOf(flowTransBean.getWorkFlowBean().getTxnAmount());
                        boolean res = (balance.compareTo(txnAmount)) <= 0 ? true : false;
                        if (res)
                            continue;
                    }
                    payMethods.add(EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()));
                } catch (final Exception e) {
                    continue;
                }
            }
        }
        if (EPayMode.HYBRID.equals(flowTransBean.getAllowedPayMode())) {
            payMethods.add(EPayMethod.HYBRID_PAYMENT);
        }

        if (!payMethods.contains(EPayMethod.BALANCE)
                && ERequestType.DYNAMIC_QR_2FA.equals(flowTransBean.getWorkFlowBean().getRequestType()))
            payMethods.add(EPayMethod.BALANCE);
        return payMethods;
    }

    public FreshSubscriptionRequest createFreshSubscriptionRequest(final WorkFlowTransactionBean flowTransBean) {

        String maxAmount = flowTransBean.getWorkFlowBean().getSubsMaxAmount();
        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSubsMaxAmount())) {
            maxAmount = flowTransBean.getWorkFlowBean().getTxnAmount();
        }
        String retryCount = flowTransBean.getWorkFlowBean().getSubsRetryCount();
        if (!"1".equals(flowTransBean.getWorkFlowBean().getSubsEnableRetry())) {
            retryCount = "1";
        }
        final FreshSubscriptionRequest request = new FreshSubscriptionRequest(flowTransBean.getWorkFlowBean()
                .getPaytmMID(), SubscriptionRequestType.getRequestTypebyName(
                flowTransBean.getWorkFlowBean().getRequestType().getType()).name(), flowTransBean.getWorkFlowBean()
                .getOrderID(), flowTransBean.getWorkFlowBean().getCustID(), flowTransBean.getWorkFlowBean()
                .getTxnAmount(), flowTransBean.getWorkFlowBean().getChannelID(), AmountType.getEnumByName(
                flowTransBean.getWorkFlowBean().getSubsAmountType()).name(), flowTransBean.getWorkFlowBean()
                .getSubsFrequency(), FrequencyUnit.getFrequencyUnitbyName(
                flowTransBean.getWorkFlowBean().getSubsFrequencyUnit()).getName(), flowTransBean.getWorkFlowBean()
                .getSubsEnableRetry(), flowTransBean.getWorkFlowBean().getSubsExpiryDate(), flowTransBean
                .getWorkFlowBean().getSubsStartDate(), flowTransBean.getWorkFlowBean().getSubsGraceDays(),
                flowTransBean.getWorkFlowBean().getSubsPayMode(),
                flowTransBean.getWorkFlowBean().getSubsPPIOnly() == null ? "" : flowTransBean.getWorkFlowBean()
                        .getSubsPPIOnly(), flowTransBean.getWorkFlowBean().getWebsite(), flowTransBean
                        .getWorkFlowBean().getIndustryTypeID(), maxAmount, retryCount);
        request.setAccountType(flowTransBean.getWorkFlowBean().getAccountType());

        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSubsStartDate())) {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (flowTransBean.getWorkFlowBean().getSubsFrequencyUnit().equals(FrequencyUnit.MONTH.getName())) {
                today.add(Calendar.MONTH, Integer.parseInt(flowTransBean.getWorkFlowBean().getSubsFrequency()));
                request.setSubscriptionStartDate(sdf.format(today.getTime()));
            } else if (flowTransBean.getWorkFlowBean().getSubsFrequencyUnit().equals(FrequencyUnit.YEAR.getName())) {
                today.add(Calendar.YEAR, Integer.parseInt(flowTransBean.getWorkFlowBean().getSubsFrequency()));
                request.setSubscriptionStartDate(sdf.format(today.getTime()));
            } else if (flowTransBean.getWorkFlowBean().getSubsFrequencyUnit().equals(FrequencyUnit.DAY.getName())) {
                // Need to add only frequency here
                today.add(Calendar.DATE, Integer.parseInt(flowTransBean.getWorkFlowBean().getSubsFrequency()));
                request.setSubscriptionStartDate(sdf.format(today.getTime()));
            }

        }
        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSubsGraceDays())) {
            request.setGraceDays("0");
        }
        request.setStartDateFlow(String.valueOf(flowTransBean.getWorkFlowBean().isStartDateFlow()));
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubsServiceID())) {
            request.setServiceId(flowTransBean.getWorkFlowBean().getSubsServiceID());
        }
        LOGGER.debug("Create Request Object for Fresh Subscription as ::{}", request);
        return request;
    }

    public RenewSubscriptionRequest createRenewalRequestBean(final WorkFlowTransactionBean flowTransBean) {

        String mid = flowTransBean.getWorkFlowBean().getPaytmMID();
        String requestType = flowTransBean.getWorkFlowBean().getRequestType().getType();
        String orderId = flowTransBean.getWorkFlowBean().getOrderID();
        String customerId = flowTransBean.getWorkFlowBean().getCustID();
        String txnAmount = flowTransBean.getWorkFlowBean().getTxnAmount();
        String subscriptionId = flowTransBean.getWorkFlowBean().getSubscriptionID();
        String acquirementId = null;
        String additionalInfo = flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo();

        RenewSubscriptionRequest renewSubscriptionRequest = new RenewSubscriptionRequest(mid, requestType, orderId,
                customerId, txnAmount, subscriptionId, acquirementId);

        if (flowTransBean.getWorkFlowBean().getExtendInfo() != null) {
            renewSubscriptionRequest.setMerchantUniqueReference(flowTransBean.getWorkFlowBean().getExtendInfo()
                    .getMerchantUniqueReference());
        }

        renewSubscriptionRequest.setAdditionalInfo(additionalInfo);

        if (null != flowTransBean.getDebitDate()) {
            renewSubscriptionRequest.setDebitDate(flowTransBean.getDebitDate());
        }

        LOGGER.debug("Create Request Object for Renew Subscription as ::{}", renewSubscriptionRequest);
        return renewSubscriptionRequest;
    }

    public ModifySubscriptionRequest createModifyRequest(final WorkFlowTransactionBean flowTransBean) {

        String paytmMID = flowTransBean.getWorkFlowBean().getPaytmMID();
        String requestType = flowTransBean.getWorkFlowBean().getRequestType().getType();
        String orderId = flowTransBean.getWorkFlowBean().getOrderID();
        String customerId = flowTransBean.getWorkFlowBean().getCustID();
        String txnAmount = flowTransBean.getWorkFlowBean().getTxnAmount();
        String subscriptionId = flowTransBean.getSubscriptionServiceResponse().getSubscriptionId();
        String savedCardId = flowTransBean.getWorkFlowBean().getSavedCardID();
        String payerUserId = flowTransBean.getUserDetails().getInternalUserId();
        String payerAccountNumber = flowTransBean.getUserDetails().getPayerAccountNumber();
        String acquirementId = flowTransBean.getTransID();
        String userEmail = flowTransBean.getUserDetails().getEmail();
        String userMobile = flowTransBean.getUserDetails().getMobileNo();

        final ModifySubscriptionRequest modifyRequestBean = new ModifySubscriptionRequest(paytmMID, requestType,
                orderId, customerId, txnAmount, subscriptionId, savedCardId, payerUserId, payerAccountNumber,
                acquirementId, userEmail, userMobile);

        LOGGER.debug("Create Request Object for Activate/Deactivate Subscription as ::{}", modifyRequestBean);
        return modifyRequestBean;
    }

    public QueryByMerchantTransIDRequestBizBean createQueryByMerchantTransIdRequest(
            final WorkFlowTransactionBean flowTransBean, boolean isNeedFullInfoRequired) {

        return new QueryByMerchantTransIDRequestBizBean(flowTransBean.getWorkFlowBean().getAlipayMID(), flowTransBean
                .getWorkFlowBean().getOrderID(), isNeedFullInfoRequired, flowTransBean.getWorkFlowBean().getPaytmMID(),
                flowTransBean.getWorkFlowBean().getRoute());
    }

    public QueryByMerchantRequestIdBizBean createQueryByMerchantRequestIdRequest(
            final WorkFlowTransactionBean flowTransBean) {
        return new QueryByMerchantRequestIdBizBean(flowTransBean.getWorkFlowBean().getOrderID(), flowTransBean
                .getWorkFlowBean().getAlipayMID());
    }

    public ChannelAccountQueryRequestBizBean createChannelAccountQueryRequest(
            WorkFlowTransactionBean workFlowTransactionBean) {
        String merchantId = workFlowTransactionBean.getWorkFlowBean().getAlipayMID();
        String userId = workFlowTransactionBean.getUserDetails().getInternalUserId();
        EnvInfoRequestBean envInfoRequestBean = workFlowTransactionBean.getWorkFlowBean().getEnvInfoReqBean();
        List<PayMethodInfo> payMethodInfos = new ArrayList<>();
        ERequestType requestType = workFlowTransactionBean.getWorkFlowBean().getRequestType();
        // Normal Case when merchant View Consult isn't called before
        if (workFlowTransactionBean.getMerchantViewConsult() == null) {
            payMethodInfos.add(new PayMethodInfo(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId()));
        } else {
            List<PayMethodViewsBiz> payMethodViews = workFlowTransactionBean.getMerchantViewConsult()
                    .getPayMethodViews();
            for (PayMethodViewsBiz payMethodView : payMethodViews) {
                if ((EPayMethod.BALANCE.getMethod().equals(payMethodView.getPayMethod()))
                        || (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodView.getPayMethod()))
                        || (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodView.getPayMethod()) && workFlowTransactionBean
                                .getUserDetails().isPaytmCCEnabled())
                        && payMethodView.getPayChannelOptionViews().get(0).isEnableStatus()) {
                    PayMethodInfo payMethodInfo = new PayMethodInfo(payMethodView.getPayMethod(),
                            AlipayRequestUtils.getExtendeInfoMap(workFlowTransactionBean.getWorkFlowBean()
                                    .getExtendInfo()));

                    // putting sub wallet info in case of channel account query
                    // also.
                    putSubWalletDetails(workFlowTransactionBean, payMethodInfo.getExtendInfo());

                    payMethodInfos.add(payMethodInfo);

                }
            }
        }
        return new ChannelAccountQueryRequestBizBean(merchantId, userId, payMethodInfos, envInfoRequestBean,
                requestType);
    }

    private String differenceAmount(String biggerAmount, Long smallAmount) {
        Long am1 = Long.parseLong(biggerAmount);
        Long diff = am1 - smallAmount;
        return diff.toString();
    }

    private ExtendedInfoRequestBean createExtendedInfo(WorkFlowTransactionBean flowTransBean) {
        ExtendedInfoRequestBean extendInfo = flowTransBean.getWorkFlowBean().getExtendInfo();
        String mid = flowTransBean.getWorkFlowBean().getPaytmMID();
        PaymentRequestBean paymentRequestBean = flowTransBean.getWorkFlowBean().getPaymentRequestBean();
        if (paymentRequestBean != null) {
            String qrCodeId = paymentRequestBean.getQrCodeId();
            boolean isOrderAlreadyCreated = Boolean.parseBoolean(AdditionalInfoUtil.getValueFromAdditionalInfo(
                    paymentRequestBean, TheiaConstant.RequestParams.ORDER_ALREADY_CREATED));
            if (!isOrderAlreadyCreated) {
                if (StringUtils.isNotEmpty(qrCodeId) && !flowTransBean.getWorkFlowBean().isPushDataToDynamicQR()
                        && !ff4jUtils.isFeatureEnabledOnMid(mid, QR_DEEPLINK_BLACKLISTED_MIDS, false)) {
                    LOGGER.info("......Adding DEEPLINK and QRCODEID to Static QR flow...........=" + qrCodeId);
                    extendInfo.setQrCodeId(qrCodeId);
                    String qrDeeplink = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.STATICQR_DEEPLINK,
                            TheiaConstant.ExtraConstants.QR_DEEPLINK);
                    extendInfo.setQrDeeplink(qrDeeplink + qrCodeId);
                } else if (ff4jUtils.isFeatureEnabledOnMid(mid, THEIA_CREATE_NON_QR_DEEPLINK, false)
                        && flowTransBean.getWorkFlowBean().isCreateNonQRDeepLink()
                        && ObjectUtils.notEqual(flowTransBean.getWorkFlowBean().getExtendInfo(), null)
                        && !flowTransBean.getWorkFlowBean().getExtendInfo().isMerchantOnPaytm()
                        && StringUtils.equalsIgnoreCase(MappingUtil.getMerchantSolutionType(mid), BizConstant.OFFLINE)) {
                    String additionalInfo = flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo();
                    String offlineAppMode = AlipayRequestUtils.getAdditionalInfoValueForKey(additionalInfo,
                            TheiaConstant.RequestParams.OFFLINE_APP_MODE);
                    try {
                        String offlineAppModes = getGlobalConfigValue(TheiaConstant.ExtraConstants.OFFLINE_APP_MODES);
                        List<String> offlineAppModeList = null;
                        if (StringUtils.isNotBlank(offlineAppModes)) {
                            offlineAppModeList = new LinkedList<>(Arrays.asList(offlineAppModes.split(",")));
                        }
                        if (CollectionUtils.isNotEmpty(offlineAppModeList)
                                && offlineAppModeList.contains(offlineAppMode)) {
                            LOGGER.info("Adding DEEPLINK to Static QR flow for Mid: {} and Mode: {}", mid,
                                    offlineAppMode);
                            String nonQrDeeplink = getGlobalConfigValue(TheiaConstant.ExtraConstants.STATIC_NON_QR_DEEPLINK);
                            if (StringUtils.isBlank(nonQrDeeplink)) {
                                nonQrDeeplink = TheiaConstant.ExtraConstants.NON_QR_DEEPLINK;
                            }
                            nonQrDeeplink = nonQrDeeplink + mid;
                            String merchantDisplayName = flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                                    .getMerchantDisplayName();
                            if (StringUtils.isNotBlank(merchantDisplayName)) {
                                nonQrDeeplink = nonQrDeeplink + TheiaConstant.ExtraConstants.MERCHANT_DISPLAY_NAME
                                        + merchantDisplayName;
                            }
                            extendInfo.setQrDeeplink(nonQrDeeplink + TheiaConstant.ExtraConstants.SOURCE
                                    + TheiaConstant.ExtraConstants.UTH);
                        }
                    } catch (MappingServiceClientException e) {
                        LOGGER.error("Error while fetching data from mapping service: {}", e.getErrorMessage());
                    }
                }
            }
            if (null != paymentRequestBean.getLinkDetailsData()
                    && null != paymentRequestBean.getLinkDetailsData().getIsHDFCDigiPOSMerchant()) {
                extendInfo.setHDFCDigiPOSMerchant(paymentRequestBean.getLinkDetailsData().getIsHDFCDigiPOSMerchant());
            }
        }

        if (flowTransBean.getSubscriptionServiceResponse() != null
                && (SubsPaymentMode.NORMAL == flowTransBean.getSubscriptionServiceResponse().getPaymentMode()
                        || SubsPaymentMode.CC == flowTransBean.getSubscriptionServiceResponse().getPaymentMode() || SubsPaymentMode.DC == flowTransBean
                        .getSubscriptionServiceResponse().getPaymentMode())) {
            extendInfo.setSavedCardId(flowTransBean.getSubscriptionServiceResponse().getSavedCardID());
            if (Boolean.TRUE.toString().equals(
                    ConfigurationUtil.getTheiaProperty(ADD_MONEY_PHASE2_ENABLE_FLAG, "false"))) {
                if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled() && null != flowTransBean.getSavedCard()
                        && StringUtils.equals(mid, ConfigurationUtil.getTheiaProperty(MP_ADD_MONEY_MID))) {
                    workFlowHelper.getAndSetCardHashAndCardIndexNo(flowTransBean.getWorkFlowBean(),
                            flowTransBean.getUserDetails(), flowTransBean.getSavedCard().getCardNumber());
                    extendInfo.setCardHash(flowTransBean.getWorkFlowBean().getCardHash());
                    extendInfo.setCardIndexNo(flowTransBean.getWorkFlowBean().getCardIndexNo());
                    extendInfo.setCardTokenRequired(flowTransBean.getWorkFlowBean().isCardTokenRequired());
                }
            }

        }
        extendInfo.setTotalTxnAmount(flowTransBean.getWorkFlowBean().getTxnAmount());
        // extendInfo.setTxnType(txnType);//ONLY_WALLET , ADDNPAY , ONLY_PG

        ERequestType requestType = flowTransBean.getWorkFlowBean().getRequestType();

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubscriptionID())) {
            extendInfo.setSubscriptionId(flowTransBean.getWorkFlowBean().getSubscriptionID());
        } else if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getSubscriptionId())) {
            extendInfo.setSubscriptionId(flowTransBean.getWorkFlowBean().getExtendInfo().getSubscriptionId());
        }
        extendInfo.setIssuingBankName(flowTransBean.getWorkFlowBean().getBankName());
        extendInfo.setAutoRetry(flowTransBean.getWorkFlowBean().isSubsRetry());
        extendInfo.setAutoRenewal(flowTransBean.getWorkFlowBean().isSubsRenew());
        extendInfo.setCommunicationManager(flowTransBean.getWorkFlowBean().isCommunicationManager());

        /*
         * setting aggMid for khatabook aggregator
         */

        String aggMid = flowTransBean.getWorkFlowBean().getAggMid();
        if (StringUtils.isNotBlank(aggMid)
                && (AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(aggMid) || AggregatorMidKeyUtil
                        .isMidEnabledForMLVAggregatorMid(aggMid))) {
            extendInfo.setAggregatorMid(aggMid);
        }

        /*
         * Setting Subvention Info in extendInfo
         */
        if (flowTransBean.getWorkFlowBean().getEmiSubventionOfferCheckoutReqData() != null) {
            ValidateResponse validateResponse = flowTransBean.getWorkFlowBean().getEmiSubventionOfferCheckoutReqData();
            String planId = validateResponse.getPlanId();
            Integer tenure = validateResponse.getInterval();
            Double gratificationDiscount = 0.0;
            Double gratificationCashback = 0.0;
            String gratificationType = null;

            for (Gratification gratification : validateResponse.getGratifications()) {
                if (gratification.getType().equals(GratificationType.DISCOUNT)) {
                    gratificationDiscount = gratification.getValue();
                    gratificationType = GratificationType.DISCOUNT.name();
                }
                if (gratification.getType().equals(GratificationType.CASHBACK)) {
                    gratificationCashback = gratification.getValue();
                    gratificationType = GratificationType.CASHBACK.name();
                }
            }
            EmiSubventionInfo emiSubventionInfo = new EmiSubventionInfo(planId, tenure, gratificationDiscount,
                    gratificationCashback);
            emiSubventionInfo.setGratificationType(gratificationType);
            emiSubventionInfo.setPgPlanId(validateResponse.getPgPlanId());
            emiSubventionInfo.setEmiInterestRate(validateResponse.getRate());
            emiSubventionInfo.setEmi(validateResponse.getEmi());

            ValidateRequest validateRequest = flowTransBean.getWorkFlowBean().getEmiSubventionValidateRequestData();
            if (SubventionUtils.isAmountBasedSubventionRequest(validateRequest)) {
                emiSubventionInfo.setEligibleAmt(validateRequest.getSubventionAmount());
                emiSubventionInfo.setSubventionAmount(String.valueOf(validateRequest.getPrice()));
                ItemBreakUp sub_item = null;
                if (CollectionUtils.isNotEmpty(validateResponse.getItemBreakUp())) {
                    for (ItemBreakUp it : validateResponse.getItemBreakUp()) {
                        if (it.getId().contains("SUB")) {
                            sub_item = it;
                            break;
                        }
                    }
                    emiSubventionInfo.setOfferId(Optional.ofNullable(sub_item.getOfferId()).orElse(""));
                    if (sub_item != null && sub_item.getAmountBearer() != null) {
                        com.paytm.pgplus.facade.acquiring.models.AmountBearer amtBearer = new com.paytm.pgplus.facade.acquiring.models.AmountBearer();
                        amtBearer.setPlatform(sub_item.getAmountBearer().getPlatform());
                        amtBearer.setMerchant(sub_item.getAmountBearer().getMerchant());
                        amtBearer.setBrand(sub_item.getAmountBearer().getBrand());
                        emiSubventionInfo.setAmtBearer(amtBearer);
                    }
                }
            } else {
                List<EmiSubventionItem> emiSubventionItems = new ArrayList<EmiSubventionItem>();

                if (validateRequest != null) {
                    Double totalSubventionAmount = 0.00;

                    List<Item> items = validateRequest.getItems();
                    for (Item item : items) {
                        if (BooleanUtils.isFalse(item.getIsStandardEmi())) {
                            totalSubventionAmount += item.getPrice();

                            EmiSubventionItem emiSubventionItem = new EmiSubventionItem();
                            emiSubventionItem.setBrandId(item.getBrandId());
                            emiSubventionItem.setModel(item.getModel());
                            emiSubventionItem.setSerial(item.getEan());
                            emiSubventionItem.setCategoryList(item.getCategoryList());

                            com.paytm.pgplus.facade.acquiring.models.AmountBearer amtBearer = new com.paytm.pgplus.facade.acquiring.models.AmountBearer();
                            for (ItemBreakUp itr : validateResponse.getItemBreakUp()) {
                                if (itr.getId().equals(item.getId())) {
                                    amtBearer.setBrand(itr.getAmountBearer().getBrand());
                                    amtBearer.setMerchant(itr.getAmountBearer().getMerchant());
                                    amtBearer.setPlatform(itr.getAmountBearer().getPlatform());
                                    emiSubventionItem.setOfferId(itr.getOfferId()); // campaignId/
                                                                                    // campaignInfo
                                    break;
                                }
                            }
                            emiSubventionItem.setAmtBearer(amtBearer);
                            emiSubventionItem = trimAndMaskEmiSubventionItemDetails(emiSubventionItem);
                            emiSubventionItems.add(emiSubventionItem);

                        }
                    }
                    emiSubventionInfo.setSubventionAmount(String.valueOf(totalSubventionAmount));
                    if (ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                            .getMid(), Ff4jFeature.EMI_SUBVENTION_ITEM_DETAILS_WITH_CHAR_RESTRICTIONS, true)) {
                        emiSubventionInfo.setItems(emiSubventionItems);
                    }
                }
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubventionOrderList())) {
                extendInfo.setEmiSubventionOrderList(flowTransBean.getWorkFlowBean().getSubventionOrderList());
            } else if (workFlowHelper.subventionAlreadyOrderStamped(flowTransBean.getWorkFlowBean())) {
                String key = workFlowHelper.emiSubventionCheckoutWithOrderRedisKey(flowTransBean.getWorkFlowBean()
                        .getPaytmMID(), flowTransBean.getWorkFlowBean().getOrderID());
                OrderStampRequest subventionOrderStampRequest = (OrderStampRequest) theiaTransactionalRedisUtil
                        .get(key);
                if (subventionOrderStampRequest != null
                        && CollectionUtils.isNotEmpty(subventionOrderStampRequest.getItemBreakUp())) {
                    StringBuilder sb = new StringBuilder("");
                    for (ItemBreakUp itemBreakUp : subventionOrderStampRequest.getItemBreakUp()) {
                        sb.append(itemBreakUp.getOrderItemId()).append("|");
                    }
                    extendInfo.setEmiSubventionOrderList(sb.toString());
                }
            }

            try {
                extendInfo.setEmiSubventionInfo(JsonMapper.mapObjectToJson(emiSubventionInfo));
            } catch (Exception e) {
                LOGGER.info("Could not cast emiSubventionInfo to String");
            }

        }

        /*
         * extendInfo parameters for eComToken
         */

        if (flowTransBean.getWorkFlowBean().isEcomTokenTxn()
                && ERequestType.NATIVE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                && (PaymentTypeIdEnum.CC.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowTransBean.getWorkFlowBean().getPaymentTypeId()))) {
            EcomTokenInfo ecomTokenInfo = null;
            try {
                ecomTokenInfo = JsonMapper.mapJsonToObject(flowTransBean.getWorkFlowBean().getPaymentDetails(),
                        EcomTokenInfo.class);
            } catch (Exception e) {
                LOGGER.error("Invalid EcomTokenInfo {}", flowTransBean.getWorkFlowBean().getPaymentDetails());
            }
            extendInfo.setFirstSixDigits(ecomTokenInfo.getFirstSixDigits());
            extendInfo.setLastFourDigits(ecomTokenInfo.getLastFourDigits());
            extendInfo.setPayRequestIssuingBank(ecomTokenInfo.getIssuingBank());
        }

        // In case of EMI transaction need to pass LastFourDigit in pay request
        /*
         * if (flowTransBean.getWorkFlowBean().isCoftTokenTxn() &&
         * Objects.nonNull(flowTransBean.getWorkFlowBean().getCardTokenInfo()))
         * { extendInfo.setLastFourDigits(flowTransBean.getWorkFlowBean().
         * getCardTokenInfo().getLastFourDigit()); }
         */

        if (ERequestType.isSubscriptionRequest(requestType.getType())
                || ERequestType.SUBSCRIPTION_PARTIAL_RENEWAL.equals(requestType)) {
            extendInfo.setAutoRenewal(flowTransBean.getSubscriptionServiceResponse().isAutoRenewalStatus());
            extendInfo.setAutoRetry(flowTransBean.getSubscriptionServiceResponse().isAutoRetryStatus());
            extendInfo.setCommunicationManager(flowTransBean.getSubscriptionServiceResponse()
                    .isCommunicationManagerStatus());
            extendInfo.setTotalTxnAmount(StringUtils.substringBefore(flowTransBean.getSubscriptionServiceResponse()
                    .getTxnAmount(), "."));
            extendInfo.setSubscriptionId(flowTransBean.getWorkFlowBean().getSubscriptionID());

            String payerUserID = flowTransBean.getSubscriptionServiceResponse().getPayerUserID();
            if (StringUtils.isNotBlank(payerUserID)) {
                extendInfo.setPayerUserID(payerUserID);
            } else {
                payerUserID = (flowTransBean.getUserDetails() != null) ? flowTransBean.getUserDetails()
                        .getInternalUserId() : payerUserID;
                extendInfo.setPayerUserID(payerUserID);
            }

            String payerAccountNumber = flowTransBean.getSubscriptionServiceResponse().getPayerAccountNumber();
            if (StringUtils.isNotBlank(payerAccountNumber)) {
                extendInfo.setPayerAccountNumber(payerAccountNumber);
            } else {
                payerAccountNumber = (flowTransBean.getUserDetails() != null) ? flowTransBean.getUserDetails()
                        .getPayerAccountNumber() : payerAccountNumber;
                extendInfo.setPayerAccountNumber(payerAccountNumber);
            }

            extendInfo.setCustID(flowTransBean.getWorkFlowBean().getCustID());
            extendInfo.setOrderID(flowTransBean.getSubscriptionServiceResponse().getOrderId());
            extendInfo.setSubscriptionType(requestType.getType());
            extendInfo.setSubsCappedAmount(StringUtils.substringBefore(flowTransBean.getSubscriptionServiceResponse()
                    .getTxnAmount(), "."));
            extendInfo.setSubsExpiryDate(flowTransBean.getSubscriptionServiceResponse().getSubscriptionExpiryDate());
            extendInfo.setUserEmail(flowTransBean.getSubscriptionServiceResponse().getUserEmail());
            extendInfo.setUserMobile(flowTransBean.getSubscriptionServiceResponse().getUserMobile());
            extendInfo.setMutualFundFeedInfo(flowTransBean.getWorkFlowBean().getAdditionalInfoMF());
            extendInfo.setAddMoneyDestination(flowTransBean.getAddMoneyDestination());
            if (flowTransBean.getSavedCard() != null && null != flowTransBean.getSavedCard().getFirstSixDigit()) {
                try {
                    String cardNumber = flowTransBean.getSavedCard().getCardNumber();
                    BinDetail binDetail = null;
                    if (flowTransBean.getWorkFlowBean().isTxnFromCardIndexNo()) {
                        binDetail = flowTransBean.getWorkFlowBean().getBinDetail();
                    } else {
                        binDetail = cardUtils.fetchBinDetails(cardNumber);
                    }
                    extendInfo.setIssuingBankId(binDetail.getBankCode());
                    extendInfo.setIssuingBankName(binDetail.getBank());
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while fetching the BIN ", e);
                }

            }
            if (flowTransBean.getWorkFlowBean().getSubsPayMode() != null) {
                switch (flowTransBean.getWorkFlowBean().getSubsPayMode()) {
                case CC:
                    extendInfo.setSubsPayMode("CC");
                    extendInfo.setSubsPPIOnly("N");
                    break;
                case DC:
                    extendInfo.setSubsPayMode("DC");
                    extendInfo.setSubsPPIOnly("N");
                    break;
                case NORMAL:
                    extendInfo.setSubsPayMode("NORMAL");
                    extendInfo.setSubsPPIOnly("N");
                    break;
                case PPI:
                    extendInfo.setSubsPayMode("PPI");
                    extendInfo.setSubsPPIOnly("Y");
                    break;
                case PPBL:
                    extendInfo.setSubsPayMode("PPBL");
                    extendInfo.setSubsPPIOnly("N");
                    break;
                case UPI:
                    extendInfo.setSubsPayMode("UPI");
                    extendInfo.setSubsPPIOnly("N");
                    break;
                default:
                    LOGGER.error("Invalid PayMode from Susbcription Client");
                    break;
                }
            }
        } else if ((requestType.equals(ERequestType.SEAMLESS) || requestType.equals(ERequestType.NATIVE) || requestType
                .equals(ERequestType.SEAMLESS_NATIVE))
                && EPayMethod.UPI.getOldName().equals(flowTransBean.getWorkFlowBean().getPayOption())) {

            extendInfo.setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());

        } else if ((requestType.equals(ERequestType.PAYTM_EXPRESS) || requestType
                .equals(ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE))
                && EPayMethod.UPI.getOldName().equals(flowTransBean.getWorkFlowBean().getPayOption())) {

            extendInfo.setVirtualPaymentAddr(flowTransBean.getWorkFlowBean().getVirtualPaymentAddress());
        }
        extendInfo.setEnhancedNative(flowTransBean.getWorkFlowBean().isEnhancedCashierPageRequest());

        if (flowTransBean.getWorkFlowBean().isNativeJsonRequest()) {
            extendInfo.setPaymentRequestFlow(NATIVE_JSON_REQUEST);
        }
        extendInfo.setWorkFlow(flowTransBean.getWorkFlowBean().getWorkFlow());
        extendInfo.setGuestToken(flowTransBean.getWorkFlowBean().getGuestToken());
        extendInfo.setCardHash(flowTransBean.getWorkFlowBean().getCardHash());
        extendInfo.setPushDataToDynamicQR(flowTransBean.getWorkFlowBean().isPushDataToDynamicQR());
        if (flowTransBean.getWorkFlowBean().isUpiConvertedToAddNPay()) {
            String upiToAddnPayPromoCode = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(BizConstant.UPI_TO_ADDNPAY_PROMO_CODE);
            if (null != upiToAddnPayPromoCode) {
                extendInfo.setUpiToAddnPayPromoCode(com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                        BizConstant.UPI_TO_ADDNPAY_PROMO_CODE, null));
            } else {
                LOGGER.info("Could not load the property upi.to.addnpay.promo.code hence upiToAddnPayPromoCode is not set");
            }
        }

        extendInfo.setCheckoutJsAppInvokePayment(flowTransBean.getWorkFlowBean().isCheckoutJsAppInvokePayment());

        // setting totalTransactionAmount for subscribe to support 0 rs payment
        if (NATIVE_SUBSCRIPTION_PAY.equals(requestType)
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubsOriginalAmount())) {
            extendInfo.setTotalTxnAmount(flowTransBean.getWorkFlowBean().getSubsOriginalAmount());
        }

        // setting orderAdditionalInfo
        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && flowTransBean.getWorkFlowBean().getPaymentRequestBean().getExtendInfo() != null
                && MapUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getExtendInfo()
                        .getOrderAdditionalInfo())) {
            extendInfo.setOrderAdditionalInfo(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getExtendInfo()
                    .getOrderAdditionalInfo());
        }

        if (ff4JUtil.isFeatureEnabled(Ff4jFeature.PASS_USERINFO_COMMENT_TO_ACQUIRING, mid)) {
            if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null) {
                extendInfo.setComment(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getComments());
            }
        }
        extendInfo.setAggType(flowTransBean.getWorkFlowBean().getAggType());
        populateOfflineAdditionalParam(flowTransBean.getWorkFlowBean(), extendInfo);
        extendInfo.setPayableAmount(flowTransBean.getWorkFlowBean().getPayableAmount());
        extendInfo.setPaymentPromoCheckoutDataPromoCode(flowTransBean.getPaymentPromoCheckoutDataPromoCode());

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentMid())
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPaymentOrderId())) {
            extendInfo.setActualMid(flowTransBean.getWorkFlowBean().getPaytmMID());
            extendInfo.setActualOrderId(flowTransBean.getWorkFlowBean().getOrderID());
            extendInfo.setDummyOrderId(flowTransBean.getWorkFlowBean().getPaymentOrderId());
            extendInfo.setDummyMerchantId(flowTransBean.getWorkFlowBean().getPaymentMid());
            extendInfo.setPaytmMerchantId(flowTransBean.getWorkFlowBean().getPaymentMid());
            try {
                MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(flowTransBean.getWorkFlowBean()
                        .getPaymentMid());
                EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
                extendInfo.setDummyAlipayMid(merchantInfo.getAlipayId());
                extendInfo.setAlipayMerchantId(merchantInfo.getAlipayId());
                flowTransBean.getWorkFlowBean().setDummyAlipayMid(merchantInfo.getAlipayId());
            } catch (Exception ex) {
                LOGGER.error("Exception in fetching merchant mapping data", ex);
            }
        }
        if (flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() != null
                && EPayMode.ADDANDPAY.name()
                        .equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid().name())) {
            if (StringUtils.isBlank(extendInfo.getCardHash()))
                extendInfo.setCardHash(flowTransBean.getWorkFlowBean().getCardHash());

            extendInfo.setPaymentMode(flowTransBean.getWorkFlowBean().getPaymentTypeId());

            String payMethod = flowTransBean.getWorkFlowBean().getPayMethod();
            String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails()
                    .getUserId() : null;
            if ((EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                    || EPayMethod.EMI.getMethod().equals(payMethod) || EPayMethod.EMI_DC.getMethod().equals(payMethod))
                    && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                            .getWorkFlowBean().getPaytmMID(), null, userId, false)) {
                extendInfo.setCardIndexNo(flowTransBean.getWorkFlowBean().getGcin());
            } else {
                if (StringUtils.isBlank(extendInfo.getCardIndexNo())) {
                    extendInfo.setCardIndexNo(flowTransBean.getWorkFlowBean().getCardIndexNo());
                }
            }

            extendInfo.setBankId(flowTransBean.getWorkFlowBean().getBankName());
            extendInfo.setSource(AddMoneySourceEnum.THIRD_PARTY.getValue());

            if (flowTransBean.getWorkFlowBean().getBinDetail() != null) {
                extendInfo.setPrepaidCard(flowTransBean.getWorkFlowBean().getBinDetail().isPrepaidCard());
                extendInfo.setCorporateCard(flowTransBean.getWorkFlowBean().getBinDetail().isCorporateCard());
                extendInfo.setBinNumber(String
                        .valueOf(flowTransBean.getWorkFlowBean().getBinDetail().isCorporateCard()));
            }
        }
        extendInfo.setFromAoaMerchant(flowTransBean.getWorkFlowBean().isFromAoaMerchant());

        populateEdcEmiPaymentInfo(extendInfo, flowTransBean);

        if (flowTransBean.getUserDetails() != null && StringUtils.isBlank(extendInfo.getUserMobile())) {
            extendInfo.setUserMobile(flowTransBean.getUserDetails().getMobileNo());
        }

        return extendInfo;
    }

    @Nullable
    private String getGlobalConfigValue(String staticNonQrDeeplink) throws MappingServiceClientException {
        PropertyInfo propertyInfo = globalConfigHelper.getGlobalConfig(staticNonQrDeeplink);
        EXT_LOGGER.customInfo("Mapping response - PropertyInfo :: {} for staticNonQrDeeplink :: {}", propertyInfo,
                staticNonQrDeeplink);
        String propertyValue = null;
        if (ObjectUtils.notEqual(propertyInfo, null)) {
            propertyValue = propertyInfo.getValue();
        }
        return propertyValue;
    }

    private EmiSubventionItem trimAndMaskEmiSubventionItemDetails(EmiSubventionItem emiSubventionItem) {
        if (CollectionUtils.isNotEmpty(emiSubventionItem.getCategoryList())) {
            for (int i = 0; i < emiSubventionItem.getCategoryList().size(); i++) {
                emiSubventionItem.getCategoryList().set(
                        i,
                        getTrimmedString(emiSubventionItem.getCategoryList().get(i),
                                TheiaConstant.ChannelInfoKeys.SIZE_LIMIT_ON_BRANDID_AND_CATEGORYLIST));
            }
        }
        emiSubventionItem.setBrandId(getTrimmedString(emiSubventionItem.getBrandId(),
                TheiaConstant.ChannelInfoKeys.SIZE_LIMIT_ON_BRANDID_AND_CATEGORYLIST));
        emiSubventionItem.setSerial(getMaskedString(emiSubventionItem.getSerial(),
                TheiaConstant.ChannelInfoKeys.SIZE_LIMIT_ON_MODEL_AND_SERIAL));
        emiSubventionItem.setModel(getMaskedString(emiSubventionItem.getModel(),
                TheiaConstant.ChannelInfoKeys.SIZE_LIMIT_ON_MODEL_AND_SERIAL));

        return emiSubventionItem;
    }

    private String getTrimmedString(String inputString, int LIMIT) {
        if (StringUtils.isBlank(inputString) || inputString.length() <= LIMIT) {
            return inputString;
        }
        return inputString.substring(0, LIMIT);

    }

    private String getMaskedString(String inputString, int LIMIT) {
        if (StringUtils.isBlank(inputString) || inputString.length() <= LIMIT) {
            return inputString;
        }
        int prefixChars = 4;
        int suffixchars = 4;
        int maskChars = 2;
        String mask = "";
        while ((maskChars--) > 0) {
            mask += "*";
        }
        return inputString.substring(0, prefixChars) + mask + inputString.substring(inputString.length() - suffixchars);
    }

    private void populateOfflineAdditionalParam(final WorkFlowRequestBean requestData,
            final ExtendedInfoRequestBean extendInfo) {
        if (null == requestData || requestData.isOfflineTxnFlow() == false) {
            return;
        }
        String logoUrl = requestData.getExtendInfo().getLogoURL();
        extendInfo.setLogoURL(logoUrl);
    }

    private Money fetchWalletBalance(WorkFlowTransactionBean flowTransBean) throws BaseException,
            MappingServiceClientException {

        UserInfo userData = userMappingService.getUserData(flowTransBean.getUserDetails().getUserId(), UserOwner.PAYTM);

        GenericCoreResponseBean<Money> walletBalanceResponse;

        if (ERequestType.PAYTM_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE
                        .equals(flowTransBean.getWorkFlowBean().getRequestType())) {

            walletBalanceResponse = walletService.fetchWalletBalance(userData.getPaytmId(), flowTransBean
                    .getWorkFlowBean().getPaytmToken(), flowTransBean.getWorkFlowBean().getOrderID(), false);

        } else if (ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())
                || DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || DYNAMIC_QR_2FA.equals(flowTransBean.getWorkFlowBean().getRequestType())) {

            walletBalanceResponse = walletService.fetchWalletBalance(userData.getPaytmId(), flowTransBean
                    .getWorkFlowBean().getToken(), flowTransBean.getWorkFlowBean().getOrderID(), false);
        } else {

            walletBalanceResponse = walletService.fetchWalletBalance(userData.getPaytmId(), flowTransBean
                    .getWorkFlowBean().getToken(), flowTransBean.getWorkFlowBean().getOrderID());

        }

        if (!walletBalanceResponse.isSuccessfullyProcessed()) {
            LOGGER.error(BALANCE_FETCH_ERR_MSG);
            throw new BaseException(BALANCE_FETCH_ERR_MSG);
        }

        return walletBalanceResponse.getResponse();
    }

    private List<BizPayOptionBill> generatePayOptionBillsForPaytmExpress(WorkFlowTransactionBean flowTransBean,
            String txnAmountStr, String chargeAmount, Map<String, String> channelInfo) throws BaseException,
            MappingServiceClientException, PaytmValidationException {

        String payOption;
        String walletAmount;

        String differenceAmount;
        String payOption2;
        EPayMethod payMethod;
        EPayMethod payMethod2;

        BizPayOptionBill bizPayOption1 = null;
        BizPayOptionBill bizPayOption2 = null;

        String walletBalance = fetchWalletBalance(flowTransBean).getAmount();

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getWalletAmount())) {
            walletAmount = flowTransBean.getWorkFlowBean().getWalletAmount();
        } else {
            walletAmount = walletBalance;
        }

        if (ERequestType.NATIVE_SUBSCRIPTION.equals(flowTransBean.getWorkFlowBean().getRequestType())
                && flowTransBean.getWorkFlowBean().getSubsPayMode() == SubsPaymentMode.NORMAL) {

            Long subsMinAmount = Long.parseLong(ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1")) * 100L;
            Long walletAmountLong = 0L;
            Long totalAmountLong = 1L;

            if (StringUtils.isNotBlank(walletAmount)) {
                try {
                    walletAmountLong = Long.parseLong(walletAmount);
                } catch (final NumberFormatException ex) {
                    LOGGER.error("Wallet Amount parsing error", ex);
                }
            }

            if (StringUtils.isNotBlank(txnAmountStr)) {
                try {
                    totalAmountLong = Long.parseLong(txnAmountStr);
                } catch (final NumberFormatException ex) {
                    LOGGER.error("Total Amount parsing error ", ex);
                }
            }
            if (walletAmountLong >= totalAmountLong && totalAmountLong > 0L) {
                walletAmount = String.valueOf(totalAmountLong - subsMinAmount);
            }
        }

        if (flowTransBean.getWorkFlowBean().isUpiConvertedToAddNPay()) {
            differenceAmount = txnAmountStr;
        } else {
            differenceAmount = differenceAmount(txnAmountStr, Long.valueOf(walletAmount));
        }

        if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().getAddOneRupee())) {
            try {
                Long addMoneyAmount = Long.parseLong(differenceAmount);
                if (addMoneyAmount < 100L) {
                    addMoneyAmount = 100L - addMoneyAmount;
                    Long walletDeductionAmount = Long.parseLong(walletAmount);
                    Long totalTransactionAmount = Long.parseLong(txnAmountStr);
                    walletDeductionAmount = walletDeductionAmount - addMoneyAmount;
                    addMoneyAmount = 100L;
                    EXT_LOGGER.info("Before Conversion totalTxnAmount is:{} walletAmount is :{} "
                            + "addMoneyAmount is :{}", totalTransactionAmount, walletDeductionAmount, addMoneyAmount);
                    if (totalTransactionAmount == (walletDeductionAmount + addMoneyAmount)) {
                        walletAmount = String.valueOf(walletDeductionAmount);
                        differenceAmount = String.valueOf(addMoneyAmount);
                        LOGGER.info("totalTxnAmount is :{} walletAmount is :{} addMoneyAmount is :{} ", txnAmountStr,
                                walletAmount, differenceAmount);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while conversion of Amount", e);
            }
        }

        EPayMode payMode = flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid();

        Boolean isNativeHybridOrAddAndPay = (flowTransBean.getWorkFlowBean().getRequestType()
                .equals(ERequestType.NATIVE) || flowTransBean.getWorkFlowBean().getRequestType()
                .equals(ERequestType.NATIVE_SUBSCRIPTION))
                && (payMode.equals(EPayMode.HYBRID) || payMode.equals(EPayMode.ADDANDPAY));

        if ((Long.valueOf(differenceAmount) <= 0)
                || ((Long.valueOf(walletAmount) > Long.valueOf(walletBalance)) && !isNativeHybridOrAddAndPay)) {
            throw new BaseException("Invalid walletAmount for Transaction");
        }

        payMethod = EPayMethod.BALANCE;
        payOption = EPayMethod.BALANCE.getMethod();
        bizPayOption1 = new BizPayOptionBill(payOption, payMethod, walletAmount, "0");
        bizPayOption1.setPayerAccountNo(flowTransBean.getUserDetails().getPayerAccountNumber());

        setPayOptionForUpiPushExpressOrIntent(flowTransBean);

        payMethod2 = EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod());
        payOption2 = flowTransBean.getWorkFlowBean().getPayOption();

        bizPayOption2 = new BizPayOptionBill(payOption2, payMethod2, differenceAmount, chargeAmount);

        if ((flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value) || flowTransBean
                .getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value))) {

            if (ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())) {
                bizPayOption2.setCardCacheToken(flowTransBean.getCacheCardToken());
                if (PreferredOtpPage.BANK.getValue().equalsIgnoreCase(
                        flowTransBean.getWorkFlowBean().getPreferredOtpPage())) {
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
                } else if (StringUtils.equalsIgnoreCase("true", flowTransBean.getWorkFlowBean().getiDebitEnabled())) {
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                            DirectPaymentVerificationMethod.ATM.getValue());
                } else if (isDirectChannel(flowTransBean)) {
                    // check if channel is direct
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                            DirectPaymentVerificationMethod.OTP.getValue());
                }
                channelInfo.put(TheiaConstant.ChannelInfoKeys.PREFERRED_OTP_PAGE, flowTransBean.getWorkFlowBean()
                        .getPreferredOtpPage());
            } else if (DYNAMIC_QR.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || DYNAMIC_QR_2FA.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
                bizPayOption2.setCardCacheToken(flowTransBean.getCacheCardToken());
            } else {
                bizPayOption2.setCardCacheToken(flowTransBean.getWorkFlowBean().getPaymentDetails());
            }

        } else if (PaymentTypeIdEnum.NB.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                && ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())) {
            if (EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode())) {
                bizAccessTokenPaymentRequestBuilder.buildPPBPaymentRequestParameters(flowTransBean, channelInfo);
            } else {
                try {
                    addPassThroughExtendedInfo(flowTransBean, channelInfo);
                } catch (FacadeCheckedException fce) {
                    throw new PaytmValidationException(
                            PaytmValidationExceptionType.INVALID_PAYMODE_NB.getValidationFailedMsg(), fce);
                }
            }
        } else if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowTransBean.getWorkFlowBean()
                .getPaymentTypeId())
                && ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())) {
            bizAccessTokenPaymentRequestBuilder.buildDigitalCreditRequestParameters(flowTransBean, channelInfo);
        } else if (EPayMethod.MP_COD.getMethod().equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                && ERequestType.isOfflineOrNativeOrUniRequest(flowTransBean.getWorkFlowBean().getRequestType())) {

            channelInfo.put(BizConstant.BROWSER_USER_AGENT, flowTransBean.getWorkFlowBean().getEnvInfoReqBean()
                    .getBrowserUserAgent());
            channelInfo.putAll(buildDefaultChannelInfo());
        }

        if (EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {
            bizPayOption2.setTopupAndPay(true);
        }

        List<BizPayOptionBill> payOptionBills = new ArrayList<>();

        payOptionBills.add(bizPayOption1);
        payOptionBills.add(bizPayOption2);

        return payOptionBills;
    }

    public String fetchChargeAmountIfApplicable(WorkFlowTransactionBean flowTransBean) {

        String chargeAmount = null;

        if (flowTransBean.getConsultFeeResponse() != null) {

            ConsultDetails consultDetails;

            if (EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {

                consultDetails = flowTransBean.getConsultFeeResponse().getConsultDetails().get(EPayMethod.BALANCE);

            } else if (EPayMode.HYBRID.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())) {

                consultDetails = flowTransBean.getConsultFeeResponse().getConsultDetails()
                        .get(EPayMethod.HYBRID_PAYMENT);

            } else {

                consultDetails = flowTransBean.getConsultFeeResponse().getConsultDetails()
                        .get(EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayMethod()));
                if (consultDetails == null) {
                    consultDetails = flowTransBean.getConsultFeeResponse().getConsultDetails()
                            .get(EPayMethod.getPayMethodByMethod(flowTransBean.getWorkFlowBean().getPayOption()));
                }

            }

            if ((consultDetails != null) && (consultDetails.getTotalTransactionAmount() != null)
                    && (consultDetails.getBaseTransactionAmount() != null)) {
                chargeAmount = String.valueOf(consultDetails.getTotalTransactionAmount().subtract(
                        consultDetails.getBaseTransactionAmount()));
                LOGGER.info("Charge amount calculated for Paytm_Express is:: {}", chargeAmount);

                chargeAmount = AmountUtils.getTransactionAmountInPaise(chargeAmount);
            }

        }

        return chargeAmount;

    }

    private BigDecimal fetchWalletBalance(final ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        if (consultPayViewResponseBean == null) {
            return new BigDecimal(-1d);
        }
        try {
            for (final PayMethodViewsBiz paymethod : consultPayViewResponseBean.getPayMethodViews()) {
                if (WALLET_TYPE.equals(paymethod.getPayMethod())) {
                    if (!paymethod.getPayChannelOptionViews().isEmpty()
                            && (paymethod.getPayChannelOptionViews().get(0) != null)
                            && !paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().isEmpty()
                            && (paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().get(0) != null)) {
                        String balance = paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().get(0)
                                .getAccountBalance();
                        if (StringUtils.isNumeric(balance)) {
                            return new BigDecimal(balance);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred , ", e);
        }
        return new BigDecimal(-1d);
    }

    private MerchantVpaTxnInfo getVpaInfoFromWebFormContext(String webFormContext) {
        MerchantVpaTxnInfo merchantVpaTxnInfo = null;
        if (StringUtils.isNotBlank(webFormContext)) {
            try {
                merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext, MerchantVpaTxnInfo.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Unable to map vpa info from webFormContext string");
            }
        }
        return merchantVpaTxnInfo;
    }

    private String preparePassThroughExtendInfoForPPBL(Map<String, String> passThroughExtendInfoMap)
            throws PaytmValidationException {
        String passThroughJson;
        try {
            passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
        } catch (FacadeCheckedException | FacadeUncheckedException e) {
            throw new PaytmValidationException(e);
        }
        String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
        return encodedPassThrough;

    }

    private Map<String, String> buildDefaultChannelInfo() {

        Map<String, String> channelInfo = new HashMap<>();

        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARD_HOLDER_NAME,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.CARD_HOLDER_NAME);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.MOBILE_NO,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.MOBILE_NO);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2);

        return channelInfo;
    }

    private void setSubwalletAmountDetails(CreateOrderAndPayRequestBean createOrderAndPayRequestBean,
            WorkFlowTransactionBean flowTransactionBean) {

        if (Objects.nonNull(flowTransactionBean) && Objects.nonNull(flowTransactionBean.getWorkFlowBean())
                && Objects.nonNull(flowTransactionBean.getWorkFlowBean().getSubWalletOrderAmountDetails())) {

            try {
                createOrderAndPayRequestBean.getExtendInfo().setSubwalletWithdrawMaxAmountDetails(
                        JsonMapper.mapObjectToJson(flowTransactionBean.getWorkFlowBean()
                                .getSubWalletOrderAmountDetails()));
            } catch (Exception exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }
    }

    private void setSubWalletAmountToExtendedInfo(ExtendedInfoRequestBean extendedInfo,
            WorkFlowTransactionBean flowTransactionBean) {
        if (Objects.nonNull(flowTransactionBean) && Objects.nonNull(flowTransactionBean.getWorkFlowBean())
                && Objects.nonNull(flowTransactionBean.getWorkFlowBean().getSubWalletOrderAmountDetails())) {

            try {
                extendedInfo.setSubwalletWithdrawMaxAmountDetails(JsonMapper.mapObjectToJson(flowTransactionBean
                        .getWorkFlowBean().getSubWalletOrderAmountDetails()));
            } catch (Exception exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }
    }

    private void setSubWalletAmountToExtendedInfo(WorkFlowTransactionBean flowTransactionBean) {
        if (Objects.nonNull(flowTransactionBean) && Objects.nonNull(flowTransactionBean.getWorkFlowBean())
                && Objects.nonNull(flowTransactionBean.getWorkFlowBean().getSubWalletOrderAmountDetails())) {

            try {
                flowTransactionBean.getExtendInfo().put(
                        SUBWALLET_AMOUNT_DETAILS,
                        JsonMapper.mapObjectToJson(flowTransactionBean.getWorkFlowBean()
                                .getSubWalletOrderAmountDetails()));
            } catch (Exception exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }
    }

    private void setWorkflowToExtendedInfo(WorkFlowTransactionBean flowTransactionBean) {
        if (flowTransactionBean != null) {
            String txnToken = dynamicQRCoreService.getTxnToken(flowTransactionBean);
            String flow = (String) theiaSessionRedisUtil.hget(txnToken, ExtendedInfoKeys.WORKFLOW);

            if (CHECKOUT.equals(flow)) {
                flowTransactionBean.getExtendInfo().put(CHECKOUT_JS_WORKFLOW, Boolean.TRUE.toString());

                HttpServletRequest httpServletRequest = AlipayRequestUtils.httpServletRequest();
                if (httpServletRequest != null) {
                    httpServletRequest.setAttribute(ExtendedInfoKeys.WORKFLOW, CHECKOUT);
                }
            }
        }
    }

    private void setcollectAppInvokeToExtendedInfo(WorkFlowTransactionBean flowTransactionBean) {
        if (StringUtils.isNotBlank(flowTransactionBean.getWorkFlowBean().getTxnToken())) {
            String isCollectAppInvoke = (String) theiaSessionRedisUtil.hget(flowTransactionBean.getWorkFlowBean()
                    .getTxnToken(), COLLECT_API_INVOKE);
            if (StringUtils.isNotBlank(isCollectAppInvoke)) {
                flowTransactionBean.getExtendInfo().put(COLLECT_API_INVOKE, isCollectAppInvoke);
            }
        }
    }

    private Map<String, String> createRiskExtendedInfo(WorkFlowTransactionBean flowTransBean) {

        Map<String, String> riskExtendedInfo = AlipayRequestUtils
                .selectRiskExtendedInfo(flowTransBean.getUserDetails());

        if (!StringUtils.isBlank(flowTransBean.getWorkFlowBean().getChannelID())) {
            riskExtendedInfo
                    .put(TheiaConstant.RequestParams.CHANNEL_ID, flowTransBean.getWorkFlowBean().getChannelID());
        }

        if (flowTransBean.getWorkFlowBean().getRiskExtendedInfo() != null) {
            riskExtendedInfo.putAll(flowTransBean.getWorkFlowBean().getRiskExtendedInfo());
        }

        updateRiskExtendInfoForAddMoney(flowTransBean, riskExtendedInfo);

        // Set merchant-userId in risk-extend-info
        setMerchantUserIdInRiskExtendInfo(flowTransBean, riskExtendedInfo);

        return riskExtendedInfo;
    }

    public void updateRiskExtendInfoForAddMoney(Map<String, String> riskExtendedInfo, List<String> userTypes,
            String orderId, String txnAmnt, String userId, String trustFactor) {

        if (userTypes == null || !userTypes.contains(SD_MERCHANT)) {
            return;
        }

        if (StringUtils.isBlank(trustFactor)) {
            trustFactor = getTrustFactorFromWalletService(userId, orderId, txnAmnt);
        }
        riskExtendedInfo.put(TRUST_FACTOR, trustFactor);

        MerchantExtendedInfoResponse merchantExtendedInfoResponse = userMappingService
                .getUserMerchantInfoResponse(userId);

        if (merchantExtendedInfoResponse != null) {
            if (merchantExtendedInfoResponse.getExtendedInfo().getIsMerchant() == 1) {
                riskExtendedInfo.put(USER_MID, merchantExtendedInfoResponse.getExtendedInfo().getAlipayMid());
            }
            riskExtendedInfo.put(IS_MERCHANT_BLOCKED,
                    String.valueOf(merchantExtendedInfoResponse.getExtendedInfo().isBlocked()));
            riskExtendedInfo.put(MERCHANT_LIMIT, merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit()
                    .toString());
        }
    }

    private void updateSettleTypeinPayOptionBills(WorkFlowTransactionBean flowTransBean,
            List<BizPayOptionBill> payOptionBills) {

        if (payOptionBills == null || payOptionBills.isEmpty() || flowTransBean == null)
            return;

        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSettleType())
                && PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            payOptionBills.forEach((payOptionBill) -> {
                if (StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(), PAYOPTION_UPI_PUSH)
                        || StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(),
                                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS)) {
                    payOptionBill.setSettleType(flowTransBean.getWorkFlowBean().getSettleType());
                }
            });
        }
    }

    private void updateCCOnUPIReqInPayOptionBills(WorkFlowTransactionBean flowTransBean,
            List<BizPayOptionBill> payOptionBills) {
        if (payOptionBills == null || payOptionBills.isEmpty() || flowTransBean == null)
            return;

        if (null != flowTransBean.getWorkFlowBean() && flowTransBean.getWorkFlowBean().isCcOnUPIEnabledForPtcTxn()) {
            payOptionBills.forEach((payOptionBill) -> {
                if (StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(), PAYOPTION_UPI_PUSH)
                        || StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(),
                                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS)) {
                    payOptionBill.setUpiCC(true);
                }
            });
        }

    }

    private void updateDeepLinkReqdinPayOptionBills(WorkFlowTransactionBean flowTransBean,
            List<BizPayOptionBill> payOptionBills) {

        if (payOptionBills == null || payOptionBills.isEmpty() || flowTransBean == null)
            return;

        if (flowTransBean.getWorkFlowBean().isNativeDeepLinkReqd()
                || "true".equals(flowTransBean.getWorkFlowBean().getIsDeepLinkReq())) {
            payOptionBills.forEach((payOptionBill) -> {
                if (StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(), PAYOPTION_UPI_PUSH)
                        || StringUtils.equalsIgnoreCase(payOptionBill.getPayOption(),
                                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS)) {
                    payOptionBill.setDeepLinkFlow(true);
                }
            });
        }
    }

    private void setInactiveTimeoutforInstantSettlement(final WorkFlowTransactionBean flowTransBean,
            BizCreateOrderRequest request) {

        if (BooleanUtils.isTrue(flowTransBean.getWorkFlowBean().isInstantSettlementEnabled())) {
            boolean inactiveTimeoutPresent = false;
            if (request.getTimeoutConfigRuleList() != null) {
                for (TimeoutConfigRule timeoutConfigRule : request.getTimeoutConfigRuleList()) {
                    if (TimeoutTypeEnum.INACTIVE_TIMEOUT.getValue().equals(
                            timeoutConfigRule.getTimeoutTypeEnum().getValue())) {
                        timeoutConfigRule.setDisabled("true");
                        inactiveTimeoutPresent = true;
                    }
                }
            }
            if (BooleanUtils.isFalse(inactiveTimeoutPresent)) {
                TimeoutConfigRule timeoutConfigRule = new TimeoutConfigRule(TimeoutTypeEnum.INACTIVE_TIMEOUT, "true",
                        String.valueOf(Long.valueOf("1")));

                if (request.getTimeoutConfigRuleList() != null) {
                    request.getTimeoutConfigRuleList().add(timeoutConfigRule);
                } else {
                    List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<>();
                    timeoutConfigRuleList.add(timeoutConfigRule);
                    request.setTimeoutConfigRuleList(timeoutConfigRuleList);
                }
            }
        }
    }

    private void updateRiskExtendInfoForAddMoney(WorkFlowTransactionBean flowTransBean,
            Map<String, String> riskExtendInfo) {
        UserDetailsBiz userDetailsBiz = flowTransBean.getUserDetails();
        if (userDetailsBiz == null) {
            return;
        }
        String requestType = flowTransBean.getWorkFlowBean().getRequestType().getType();
        if (isAddMoneyToWallet(flowTransBean.getWorkFlowBean().getPaytmMID(), requestType)
                || (StringUtils.equals(NATIVE.getType(), requestType) && flowTransBean.getWorkFlowBean()
                        .isNativeAddMoney())) {

            updateRiskExtendInfoForAddMoney(riskExtendInfo, userDetailsBiz.getUserTypes(), flowTransBean
                    .getWorkFlowBean().getOrderID(), flowTransBean.getWorkFlowBean().getTxnAmount(),
                    userDetailsBiz.getUserId(), flowTransBean.getTrustFactor());
        }
        if (StringUtils.isNotBlank(flowTransBean.getAddMoneyDestination())
                && (StringUtils.equals(ADDMONEY_EXPRESS.getType(), requestType) || ((StringUtils.equals(
                        NATIVE.getType(), requestType) && flowTransBean.getWorkFlowBean().isNativeAddMoney())))) {
            riskExtendInfo.put(ADD_MONEY_DESTINATION, flowTransBean.getAddMoneyDestination());
        }
    }

    public boolean isAddMoneyToWallet(String mid, String requestType) {
        /*
         * addMoney is done for SCW... merchant and
         * ADD_MONEY/ADD_MONEY_EXPRESS/DEFAULT/TOPUP_EXPRESS flows,
         * isAddMoneyToWallet check is done so that this walletlimit api and
         * mapping-service api called for only above flows
         */
        return StringUtils.equals(ADDMONEY_EXPRESS.getType(), requestType)
                || StringUtils.equals(ADD_MONEY.getType(), requestType)
                || StringUtils.equals(TOPUP_EXPRESS.getType(), requestType)
                || StringUtils.equals(mid, ConfigurationUtil.getTheiaProperty(MP_ADD_MONEY_MID));
    }

    private String getTrustFactorFromWalletService(final String userId, final String orderId, final String txnAmnt) {
        GenericCoreResponseBean<BizWalletConsultResponse> walletResponse = workFlowHelper.consultAddMoneyV2(userId,
                orderId, txnAmnt, null);
        if (walletResponse.isSuccessfullyProcessed() && walletResponse.getResponse() != null
                && StringUtils.isNotBlank(walletResponse.getResponse().getTrustFactor())) {
            return walletResponse.getResponse().getTrustFactor();
        }
        LOGGER.error("trustFactor not got from walletService, returning null");
        return null;
    }

    private boolean isUpiCollectPayment(WorkFlowTransactionBean flowTransBean) {
        return (flowTransBean.getWorkFlowBean() != null
                && PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                && !flowTransBean.getWorkFlowBean().isUpiPushFlow() && !flowTransBean.getWorkFlowBean()
                .isNativeDeepLinkReqd());
    }

    public void setChannelInfoForNativeJsonRequest(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> map) {
        if (workFlowTransactionBean.getWorkFlowBean().isNativeJsonRequest()
                || workFlowTransactionBean.getWorkFlowBean().isEnhancedCashierPaymentRequest()) {
            map.put(NATIVE_JSON_REQUEST, Boolean.TRUE.toString());
        }
        if (workFlowTransactionBean.getWorkFlowBean().isEnhancedCashierPaymentRequest()) {
            map.put(BizConstant.ExtendedInfoKeys.IS_ENHANCED_NATIVE, Boolean.TRUE.toString());
        }
    }

    private void addPassThroughExtendedInfo(final WorkFlowTransactionBean flowTransBean, Map<String, String> channelInfo)
            throws FacadeCheckedException {
        Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();
        setChannelInfoForNativeJsonRequest(flowTransBean, passThroughExtendInfoMap);
        setPaytmSsoTokenForNBPPBLRequest(flowTransBean, passThroughExtendInfoMap);
        String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
        String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
        channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
        return;
    }

    private void setPaytmSsoTokenForNBPPBLRequest(WorkFlowTransactionBean flowTransBean,
            final Map<String, String> passThroughExtendInfoMap) {
        WorkFlowRequestBean workFlowBean = flowTransBean.getWorkFlowBean();
        if (workFlowBean.getUserDetailsBiz() != null && "NET_BANKING_PYTM".equals(workFlowBean.getPayOption())) {
            passThroughExtendInfoMap.put("paytmSsoToken", workFlowBean.getUserDetailsBiz().getUserToken());
        }
    }

    private void setPayOptionForUpiPushExpressOrIntent(final WorkFlowTransactionBean flowTransBean) {
        if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            if (flowTransBean.getWorkFlowBean().isUpiPushExpressSupported()) {
                flowTransBean.getWorkFlowBean().setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
            } else {
                setPayOptionForUpiIntent(flowTransBean);
            }

        }
    }

    private void setPayOptionForUpiIntent(final WorkFlowTransactionBean flowTransBean) {
        if ((PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId()) && flowTransBean
                .getWorkFlowBean().isNativeDeepLinkReqd()) || flowTransBean.getWorkFlowBean().isGenerateEsnRequest()) {
            LOGGER.info("Setting UPI_PUSH pay option for UPI_INTENT");
            flowTransBean.getWorkFlowBean().setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH);
        }
    }

    private void setMerchantUserIdInRiskExtendInfo(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> riskExtendInfo) {
        UserInfo userInfo = null;
        if (workFlowTransactionBean.getWorkFlowBean() != null) {
            LOGGER.info("Set merchant-user-id in risk-extendinfo for merchant: {}", workFlowTransactionBean
                    .getWorkFlowBean().getPaytmMID());
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantMappingService
                    .getMerchantInfoResponse(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
            if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                    && merchantExtendedInfoResponse.getExtendedInfo().getUserId() != null) {
                String userId = merchantExtendedInfoResponse.getExtendedInfo().getUserId();
                try {
                    LOGGER.info("Getting user info for UserId {}", userId);
                    userInfo = userMappingService.getUserData(userId, UserOwner.PAYTM);
                } catch (Exception e) {
                    LOGGER.info("Error Occured while fetching user-info from mapping-service for userId- {}, mid- {}",
                            userId, workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
                }

            }
        }

        // As mentioned on Jira-12257
        if (userInfo != null && StringUtils.isNotBlank(userInfo.getAlipayId())) {
            riskExtendInfo.put("userMerchant", userInfo.getAlipayId());
        } else {
            LOGGER.info("Setting userMerchant as blank as userInfo or platform Id is Null");
            riskExtendInfo.put("userMerchant", "");
        }
    }

    public ListSubscriptionRequest createFetchActiveSubscriptionsRequestBean(WorkFlowTransactionBean workFlowTransBean) {
        ListSubscriptionRequestBody requestBody = new ListSubscriptionRequestBody();
        requestBody.setMid(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        requestBody.setCustId(workFlowTransBean.getWorkFlowBean().getCustID());
        return new ListSubscriptionRequest(new ValidationHead(), requestBody);
    }

    private void setParamInChannelInfo(WorkFlowTransactionBean flowTransBean, Map<String, String> channelInfo)
            throws PaytmValidationException {
        channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
        channelInfo.put(BizConstant.BROWSER_USER_AGENT, flowTransBean.getWorkFlowBean().getEnvInfoReqBean()
                .getBrowserUserAgent());
        if (PreferredOtpPage.BANK.getValue().equalsIgnoreCase(flowTransBean.getWorkFlowBean().getPreferredOtpPage())) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "false");
        } else if (StringUtils.equalsIgnoreCase("true", flowTransBean.getWorkFlowBean().getiDebitEnabled())) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
            channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                    DirectPaymentVerificationMethod.ATM.getValue());
        } else if (isDirectChannel(flowTransBean) && !flowTransBean.getWorkFlowBean().isDirectBankCardFlow()) {
            // check if channel is direct
            channelInfo.put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
            channelInfo.put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                    DirectPaymentVerificationMethod.OTP.getValue());
        }
        channelInfo.put(TheiaConstant.ChannelInfoKeys.PREFERRED_OTP_PAGE, flowTransBean.getWorkFlowBean()
                .getPreferredOtpPage());

        if (!"Y".equals(channelInfo.get(BizConstant.IS_EMI))) {
            channelInfo.put(BizConstant.IS_EMI, "N");
        }
        try {
            if (PaymentTypeIdEnum.NB.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                    && !(EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode()))) {
                addPassThroughExtendedInfo(flowTransBean, channelInfo);
            }

            if (PaymentTypeIdEnum.NB.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                    && EPayMethod.PPBL.getOldName().equals(flowTransBean.getWorkFlowBean().getBankCode())) {
                bizAccessTokenPaymentRequestBuilder.buildPPBPaymentRequestParameters(flowTransBean, channelInfo);
            } else if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowTransBean.getWorkFlowBean()
                    .getPaymentTypeId())) {
                bizAccessTokenPaymentRequestBuilder.buildDigitalCreditRequestParameters(flowTransBean, channelInfo);
            } else if (PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
                bizAccessTokenPaymentRequestBuilder.buildUPIRequestParameters(flowTransBean, channelInfo);
                setAddNPayInPassThroughInfo(channelInfo, flowTransBean.getWorkFlowBean());

            }
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            throw new PaytmValidationException(
                    PaytmValidationExceptionType.INVALID_PAYMODE_NB.getValidationFailedMsg(), e);
        } catch (PaytmValidationException e) {
            LOGGER.error("Exception occurred while building request for Native retry {}", e.getType());
            throw e;
        }
        if (flowTransBean.getWorkFlowBean().isFromAoaMerchant()
                && ERequestType.isSubscriptionCreationRequest(flowTransBean.getWorkFlowBean().getRequestType()
                        .getType())) {
            channelInfo.put(IS_MANDATE, "true");
        }
        flowTransBean.setChannelInfo(channelInfo);
    }

    public ChannelAccountQueryRequestBizBean createChannelAccountQueryRequestForMgv(
            WorkFlowTransactionBean workFlowTransactionBean) {
        String merchantId = workFlowTransactionBean.getWorkFlowBean().getAlipayMID();
        String userId = workFlowTransactionBean.getUserDetails().getInternalUserId();
        EnvInfoRequestBean envInfoRequestBean = workFlowTransactionBean.getWorkFlowBean().getEnvInfoReqBean();
        List<PayMethodInfo> payMethodInfos = new ArrayList<>();
        ERequestType requestType = workFlowTransactionBean.getWorkFlowBean().getRequestType();
        // Normal Case when merchant View Consult isn't called before
        if (workFlowTransactionBean.getMerchantViewConsult() == null) {
            payMethodInfos.add(new PayMethodInfo(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId()));
        } else {
            List<PayMethodViewsBiz> payMethodViews = workFlowTransactionBean.getMerchantViewConsult()
                    .getPayMethodViews();
            for (PayMethodViewsBiz payMethodView : payMethodViews) {
                if ((EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodView.getPayMethod()))
                        && payMethodView.getPayChannelOptionViews().get(0).isEnableStatus()) {
                    PayMethodInfo payMethodInfo = new PayMethodInfo(payMethodView.getPayMethod(),
                            AlipayRequestUtils.getExtendeInfoMap(workFlowTransactionBean.getWorkFlowBean()
                                    .getExtendInfo()));

                    // putting sub wallet info in case of channel account query
                    // also.
                    putSubWalletDetails(workFlowTransactionBean, payMethodInfo.getExtendInfo());

                    payMethodInfos.add(payMethodInfo);

                }
            }
        }
        return new ChannelAccountQueryRequestBizBean(merchantId, userId, payMethodInfos, envInfoRequestBean,
                requestType);
    }

    public UserSummaryBulkPaymentOptionsRequest createUserSummaryBulkPaymentOptionsRequest(
            WorkFlowRequestBean workFlowRequestBean, WorkFlowTransactionBean workFlowTransactionBean,
            WorkFlowResponseBean workFlowResponseBean) {
        UserSummaryBulkPaymentOptionsRequest userSummaryBulkPaymentOptionsRequest = new UserSummaryBulkPaymentOptionsRequest();
        userSummaryBulkPaymentOptionsRequest.setSiteId(EMI_SUBVENTION_SITE_ID);
        userSummaryBulkPaymentOptionsRequest.setItems(workFlowRequestBean.getItems());
        List<PaymentOption> paymentOptionsArray = new ArrayList<>();
        List<CardBeanBiz> merchantSavedCardList = new ArrayList<>();
        String merchantCoftConfig = workFlowRequestBean.getMerchantCoftConfig();
        boolean isEnableGcinOnCoft = workFlowRequestBean.isEnableGcinOnCoftPromo();

        if (workFlowResponseBean.getUserDetails() != null) {
            merchantSavedCardList = workFlowResponseBean.getUserDetails().getMerchantViewSavedCardsList();
        } else {
            MidCustIdCardBizDetails midCustIdCardBizDetails = workFlowTransactionBean.getMidCustIdCardBizDetails();
            if (midCustIdCardBizDetails != null
                    && !CollectionUtils.isEmpty(midCustIdCardBizDetails.getMerchantCustomerCardList())) {
                merchantSavedCardList = midCustIdCardBizDetails.getMerchantCustomerCardList();
            }
        }

        for (int i = 0; i < merchantSavedCardList.size(); i++) {
            if (merchantSavedCardList.get(i).getCardIndexNo() != null) {
                PaymentOption paymentOption = new PaymentOption();
                String saveCardId = getSaveCardId(workFlowRequestBean, merchantSavedCardList.get(i));
                paymentOption.setCardIndexNo(saveCardId);
                if (StringUtils.isEmpty(paymentOption.getCardIndexNo())) {
                    LOGGER.error("Unable to fetch saved card id from Platform");
                    continue;
                }
                paymentOption.setBin6(merchantSavedCardList.get(i).getFirstSixDigit().intValue());
                // paymentOption.setBin8(merchantSavedCardList.get(0).getCardNumber());
                paymentOption.setPayMethod(merchantSavedCardList.get(i).getCardType());
                paymentOption.setIssuingBank(merchantSavedCardList.get(i).getInstId());
                paymentOption.setIssuingNetworkCode(merchantSavedCardList.get(i).getCardScheme());
                paymentOption.setTransactionAmount(Double.valueOf(workFlowRequestBean
                        .getEmiSubventedTransactionAmount()));
                paymentOption.setVpa("");
                paymentOptionsArray.add(paymentOption);

            }
        }
        List<PaymentDetailsBulk> paymentDetailsBulksArray = new ArrayList<>();
        for (PaymentOption paymentOption : paymentOptionsArray) {
            PaymentDetailsBulk paymentDetailsBulk = new PaymentDetailsBulk();
            PaymentDetail paymentDetails = new PaymentDetail();
            List<PaymentOption> paymentOptions = new ArrayList<>();
            paymentOptions.add(paymentOption);
            paymentDetails.setPaymentOptions(paymentOptions);
            paymentDetails.setTotalTransactionAmount(paymentOption.getTransactionAmount());
            paymentDetailsBulk.setPaymentDetails(paymentDetails);
            paymentDetailsBulksArray.add(paymentDetailsBulk);
        }

        userSummaryBulkPaymentOptionsRequest.setPaymentDetailsBulk(paymentDetailsBulksArray);
        userSummaryBulkPaymentOptionsRequest.setTotalTransactionAmount(Double.valueOf(workFlowRequestBean
                .getEmiSubventedTransactionAmount()));

        return userSummaryBulkPaymentOptionsRequest;

    }

    public GenericCoreResponseBean<Boolean> createUserSummaryBulkPaymentOptionsResponse(
            WorkFlowRequestBean workFlowRequestBean, WorkFlowTransactionBean workFlowTransactionBean,
            WorkFlowResponseBean workFlowResponseBean,
            GenericEmiSubventionResponse<UserSummaryBulkPaymentOptionsResponse> genericUserSummarybulkResponse) {

        if (genericUserSummarybulkResponse != null && genericUserSummarybulkResponse.getStatus() == 0) {
            LOGGER.info("Error in fetching emi subvention bulk for saved card  error code : {}, error message : {}",
                    genericUserSummarybulkResponse.getError().getCode(), genericUserSummarybulkResponse.getError()
                            .getMessage());
            return new GenericCoreResponseBean<>(false);
        }
        UserSummaryBulkPaymentOptionsResponse userSummaryBulkPaymentOptionsResponse = genericUserSummarybulkResponse
                .getData();
        Map<String, CardIndexDetail> paymentInstruments = userSummaryBulkPaymentOptionsResponse.getPaymentInstruments();
        List<CardBeanBiz> savedCardList;

        if (workFlowTransactionBean.getUserDetails() != null) {
            savedCardList = workFlowTransactionBean.getUserDetails().getMerchantViewSavedCardsList();
        } else {
            savedCardList = workFlowTransactionBean.getMidCustIdCardBizDetails().getMerchantCustomerCardList();
        }
        for (CardBeanBiz savedCard : savedCardList) {
            String key = getSaveCardId(workFlowRequestBean, savedCard);
            savedCard.setSavedCardEmiSubventionDetails(paymentInstruments.get(key));
        }
        return new GenericCoreResponseBean<>(true);
    }

    private String getSaveCardId(WorkFlowRequestBean workFlowRequestBean, CardBeanBiz merchantSavedCard) {

        String merchantCoftConfig = workFlowRequestBean.getMerchantCoftConfig();
        if (merchantSavedCard != null & merchantSavedCard.getCardIndexNo() != null) {
            if (!workFlowRequestBean.isEnableGcinOnCoftPromo()) {
                return merchantSavedCard.getCardIndexNo();
            } else {
                // assuming we will get tin in CardIndexNo field only
                if (merchantSavedCard.isCardCoft()) {
                    if (merchantCoftConfig.equals("PAR")) {
                        return merchantSavedCard.getPar();
                    } else if (merchantCoftConfig.equals("GCIN")) {
                        return merchantSavedCard.getGcin();
                    }
                } else if (merchantSavedCard.getCardIndexNo().length() > 15) {
                    if (merchantCoftConfig.equals("GCIN")) {
                        return merchantSavedCard.getGcin();
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public BanksRequest createEmiSubventionBanksRequest(List<Item> items) {
        BanksRequest banksRequest = new BanksRequest();
        banksRequest.setItems(items);
        banksRequest.setSiteId(EMI_SUBVENTION_SITE_ID);
        return banksRequest;

    }

    // Method to set VPA in BizPayOptionBill in case of UPI Push or Collect.
    private void setVPAforUPIPushAndCollect(BizPayOptionBill bizPayOptionBill1, BizPayOptionBill bizPayOptionBill2,
            String vpa, UserDetailsBiz userDetails) {

        if (isUPIPushOrCollect(bizPayOptionBill1)) {
            LOGGER.info("VPA set to bizPayOptionBill1");
            bizPayOptionBill1.setVirtualPaymentAddr(vpa);
        } else if (isUPIPushOrCollect(bizPayOptionBill2)) {
            LOGGER.info("VPA set to bizPayOptionBill2");
            bizPayOptionBill2.setVirtualPaymentAddr(vpa);
        }
    }

    // Method to check if Channel is UPI or UPI_PUSH_EXPRESS i.e. to check if
    // it is case of UPI Push or Collect.
    private boolean isUPIPushOrCollect(BizPayOptionBill payOptionBill) {

        if (null == payOptionBill) {
            return false;
        }
        String payOption = payOptionBill.getPayOption();
        boolean isUPIPushOrCollect = (StringUtils.equals(payOption, "UPI") || StringUtils.equals(payOption,
                "UPI_PUSH_EXPRESS"));
        LOGGER.info("isUPIPushOrCollect: {}", isUPIPushOrCollect);
        return isUPIPushOrCollect;
    }

    public void saveCardAtPlatformIfApplicable(WorkFlowTransactionBean transBean,
            List<BizPayOptionBill> payOptionBillList) {
        String userId = transBean.getUserDetails() != null ? transBean.getUserDetails().getUserId() : null;
        final boolean storeCard = ((transBean.getWorkFlowBean().getStoreCard() != null) && "1".equals(transBean
                .getWorkFlowBean().getStoreCard().trim())) ? true : false;
        // extracting debit/credit payOptions to enable flag to
        // saveCardAtPlatform
        for (BizPayOptionBill payOptionBill : payOptionBillList) {
            String payMethod = payOptionBill.getPayMethod().getMethod();
            if (PayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                    || PayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                    || PayMethod.EMI.getMethod().equals(payMethod) || PayMethod.EMI_DC.getMethod().equals(payMethod)) {
                String custId = transBean.getWorkFlowBean().getCustID();
                if (storeCard && !transBean.getWorkFlowBean().getIsSavedCard()) {

                    /**
                     * cards on mid custid should be saved only for ofus
                     * merchant
                     */
                    if (!StringUtils.equals(custId, userId)
                            && ff4JUtil
                                    .saveCardAtPlatformOnMidCustId(transBean.getWorkFlowBean().getPaytmMID(), custId)) {
                        payOptionBill.setSaveAssetForMerchant(transBean.getWorkFlowBean().isStoreCardPrefEnabled());
                        payOptionBill.setSaveCardAfterPay(payOptionBill.isSaveAssetForMerchant());
                    }
                    if (StringUtils.isNotBlank(userId) && ff4JUtil.saveCardAtPlatformOnUserId(userId)) {
                        payOptionBill.setsaveAssetForUser(true);
                        payOptionBill.setSaveCardAfterPay(true);
                    }
                } else if (transBean.getWorkFlowBean().getIsSavedCard()) {
                    /**
                     * If txn is by savedCard, then we need to update last
                     * successful time at platform by setting
                     * saveAssetForMerchant and saveAssetForUser flags.
                     */

                    payOptionBill.setSaveAssetForMerchant(!StringUtils.equals(custId, userId)
                            && transBean.getWorkFlowBean().isStoreCardPrefEnabled());
                    payOptionBill.setsaveAssetForUser(StringUtils.isNotBlank(userId));
                }
                break;
            }
        }
    }

    private void setRedisKeyForQueryParams(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean != null && PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                && StringUtils.isNotEmpty(workFlowRequestBean.getQueryParams())) {
            EXT_LOGGER.customInfo("setting redis key for UPI query params");
            String mid = workFlowRequestBean.getPaytmMID();
            String orderId = workFlowRequestBean.getOrderID();
            String rediskey = mid + "_" + orderId + "_" + QUERY_PARAMS_KEY_SUFFIX;
            String queryParams = workFlowRequestBean.getQueryParams();

            if (workFlowRequestBean.isAoaSubsOnPgMid()) {
                Map<String, String> queryParamsAsMap = getQueryParams(queryParams);
                queryParamsAsMap.put("mid", mid);
                if (MapUtils.isNotEmpty(queryParamsAsMap)) {
                    queryParams = updateQueryParams(queryParamsAsMap);
                }
                LOGGER.info("queryParams {}", queryParams);
            }

            redisUtil.pushQueryParams(rediskey, queryParams);
        }
    }

    private String updateQueryParams(Map<String, String> queryParamsAsMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParamsAsMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb = sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private Map<String, String> getQueryParams(String queryParamString) {
        Map<String, String> queryParams = null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(queryParamString)) {
            queryParams = new HashMap<>();
            String[] params = queryParamString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                queryParams.put(keyValue[0].trim(), keyValue.length > 1 ? keyValue[1].trim()
                        : TheiaConstant.ExtraConstants.EMPTY_STRING);
            }
        }
        return queryParams;
    }

    public OrderModifyRequest getOrderModifyRequestForUpdatedAmount(WorkFlowRequestBean workFlowRequestBean) {

        OrderModifyRequest orderModifyRequest = null;
        OrderModifyRequestBody orderModifyRequestBody = null;

        Map<String, String> extendInfo = new HashMap<>();
        if (StringUtils.equals(OrderType.BRAND_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            extendInfo.put(EMI_CATEGORY, BRAND_EMI);
        } else if (StringUtils.equals(OrderType.BANK_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            extendInfo.put(EMI_CATEGORY, BANK_EMI);
        }
        extendInfo.put("billAmount", workFlowRequestBean.getExtendInfo().getBillAmount());
        try {
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder = new OrderModifyRequestBody.OrderModifyRequestBodyBuilder(
                    workFlowRequestBean.getAlipayMID(), workFlowRequestBean.getTransID(),
                    workFlowRequestBean.getOrderID());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.operator("MERCHANT");
            Money txnAmount = new Money(EnumCurrency.INR, workFlowRequestBean.getTxnAmount());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.orderAmount(txnAmount);
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder
                    .requestId(RequestIdGenerator.generateRequestId())
                    .orderPricingInfo(workFlowRequestBean.getOrderPricingInfo()).extendInfo(extendInfo)
                    .detailExtendInfo(workFlowRequestBean.getDetailExtendInfo());
            orderModifyRequestBodyBuilder = setRoutePg2OrderModify(workFlowRequestBean, orderModifyRequestBodyBuilder);
            orderModifyRequestBody = orderModifyRequestBodyBuilder.build();
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.ACQUIRING_ORDER_MODIFY);
            orderModifyRequest = new OrderModifyRequest(head, orderModifyRequestBody);
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred while creating facade request for order modify, Reason ::", exception);
        }
        return orderModifyRequest;

    }

    public OrderModifyRequest getOrderModifyRequestForOnusRentPayment(WorkFlowRequestBean workFlowRequestBean,
            Map<String, String> riskInfo) {

        OrderModifyRequest orderModifyRequest = null;
        OrderModifyRequestBody orderModifyRequestBody = null;

        try {
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder = new OrderModifyRequestBody.OrderModifyRequestBodyBuilder(
                    workFlowRequestBean.getAlipayMID(), workFlowRequestBean.getTransID(),
                    workFlowRequestBean.getOrderID());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.operator("MERCHANT");
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.extendInfo(riskInfo);
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.requestId(RequestIdGenerator
                    .generateRequestId());
            orderModifyRequestBodyBuilder = setRoutePg2OrderModify(workFlowRequestBean, orderModifyRequestBodyBuilder);
            orderModifyRequestBody = orderModifyRequestBodyBuilder.build();
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.ACQUIRING_ORDER_MODIFY);
            orderModifyRequest = new OrderModifyRequest(head, orderModifyRequestBody);
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred while creating facade request for order modify, Reason ::", exception);
        }
        return orderModifyRequest;

    }

    public OrderModifyRequest getOrderModifyRequestForExpiry(String alipayMid, String transId, String orderId,
            Long orderTimeout, Routes routes, String mid) {

        OrderModifyRequest orderModifyRequest = null;
        OrderModifyRequestBody orderModifyRequestBody = null;

        try {
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder = new OrderModifyRequestBody.OrderModifyRequestBodyBuilder(
                    alipayMid, transId, orderId);
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.operator("MERCHANT");

            List<TimeoutConfigRule> timeoutConfigRuleList = new ArrayList<>();
            timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.INACTIVE_TIMEOUT, "false", String
                    .valueOf(orderTimeout)));
            timeoutConfigRuleList.add(new TimeoutConfigRule(TimeoutTypeEnum.EXPIRY_TIMEOUT, "false", String
                    .valueOf(orderTimeout)));

            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.timeoutConfigRuleList(timeoutConfigRuleList);
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.requestId(RequestIdGenerator
                    .generateRequestId());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.setPaytmMerchantId(mid);
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.setRoute(routes);
            orderModifyRequestBody = orderModifyRequestBodyBuilder.build();
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.ACQUIRING_ORDER_MODIFY);
            orderModifyRequest = new OrderModifyRequest(head, orderModifyRequestBody);
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred while creating facade request for order modify, Reason ::", exception);
        }
        return orderModifyRequest;

    }

    public OrderModifyRequest getOrderModifyRequestForUpdateTransaction(WorkFlowRequestBean workFlowRequestBean,
            List<Goods> goods, List<ShippingInfo> shippingInfos) {

        OrderModifyRequest orderModifyRequest = null;
        OrderModifyRequestBody orderModifyRequestBody = null;

        try {
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder = new OrderModifyRequestBody.OrderModifyRequestBodyBuilder(
                    workFlowRequestBean.getAlipayMID(), workFlowRequestBean.getTransID(),
                    workFlowRequestBean.getOrderID());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.operator("MERCHANT");
            Money txnAmount = new Money(EnumCurrency.INR, workFlowRequestBean.getTxnAmount());
            if (null != txnAmount) {
                orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.orderAmount(txnAmount);
            }
            if (null != goods) {
                orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.goods(goods);
            }
            if (null != shippingInfos) {
                orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.shippingInfo(shippingInfos);
            }

            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.requestId(RequestIdGenerator
                    .generateRequestId());
            orderModifyRequestBodyBuilder = setRoutePg2OrderModify(workFlowRequestBean, orderModifyRequestBodyBuilder);
            orderModifyRequestBody = orderModifyRequestBodyBuilder.build();
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.ACQUIRING_ORDER_MODIFY);
            orderModifyRequest = new OrderModifyRequest(head, orderModifyRequestBody);
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred while creating facade request for order modify, Reason ::", exception);
        }
        return orderModifyRequest;

    }

    public OrderModifyRequest getOrderModifyRequestToUpdateDetailExtendInfo(WorkFlowRequestBean workFlowRequestBean) {

        OrderModifyRequest orderModifyRequest = null;
        OrderModifyRequestBody orderModifyRequestBody;
        Map<String, String> extendInfo = new HashMap<>();
        if (StringUtils.equals(OrderType.BRAND_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            extendInfo.put(EMI_CATEGORY, BRAND_EMI);
        } else if (StringUtils.equals(OrderType.BANK_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
            extendInfo.put(EMI_CATEGORY, BANK_EMI);
        }
        extendInfo.put("billAmount", workFlowRequestBean.getExtendInfo().getBillAmount());
        try {
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder = new OrderModifyRequestBody.OrderModifyRequestBodyBuilder(
                    workFlowRequestBean.getAlipayMID(), workFlowRequestBean.getTransID(),
                    workFlowRequestBean.getOrderID());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.operator("MERCHANT");
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder
                    .requestId(RequestIdGenerator.generateRequestId())
                    .orderPricingInfo(workFlowRequestBean.getOrderPricingInfo()).extendInfo(extendInfo)
                    .detailExtendInfo(workFlowRequestBean.getDetailExtendInfo());
            orderModifyRequestBodyBuilder = setRoutePg2OrderModify(workFlowRequestBean, orderModifyRequestBodyBuilder);
            orderModifyRequestBody = orderModifyRequestBodyBuilder.build();
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.ACQUIRING_ORDER_MODIFY);
            orderModifyRequest = new OrderModifyRequest(head, orderModifyRequestBody);
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred while creating facade request for order modify, Reason ::", exception);
        }
        return orderModifyRequest;
    }

    private void enrichCobrandedCardData(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        try {
            if (flowTransBean.getWorkFlowBean() != null
                    && flowTransBean.getWorkFlowBean().getCobarandPreferenceEnabled() != null) {
                String cardNumber = "";
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSavedCardID())) {
                    PayCardOptionViewBiz payCardOptionViewBiz = workFlowHelper.getLitePayViewConsultResponse(
                            flowTransBean.getWorkFlowBean().getPaytmMID(), flowTransBean.getWorkFlowBean()
                                    .getSavedCardID());
                    if (null != payCardOptionViewBiz)
                        cardNumber = payCardOptionViewBiz.getMaskedCardNo();
                } else if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardNo())) {
                    cardNumber = flowTransBean.getWorkFlowBean().getCardNo();
                }
                extInfo.put("coBrandedMaskedCardNo", workFlowHelper.getMaskedNumber(cardNumber));
                extInfo.put("coBrandedPrefix", ConfigurationUtil.getProperty(BizConstant.COBRANDED_CARD_PREFIX));
            }
        } catch (Exception e) {
            LOGGER.error("Error in crating masked number {}", e);
        }

    }

    private void updateExtendInfoForPosOrder(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo())
                && flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo().contains(UPI_POS_ORDER)) {
            extInfo.put(BizConstant.REQUEST_TYPE, UPI_POS_ORDER);
        }
    }

    /**
     * Setting userMobile and userEmail in UPI_POS_ORDER and UPI_QR_CODE in
     * order to send notification to merchant
     */

    private void updateMobNoAndEmailInExtendInfo(WorkFlowTransactionBean flowTransBean, Map<String, String> extInfo) {
        if (flowTransBean.getWorkFlowBean() != null
                && flowTransBean.getWorkFlowBean().getExtendInfo() != null
                && StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo())
                && (flowTransBean.getWorkFlowBean().getExtendInfo().getAdditionalInfo().contains(UPI_POS_ORDER) || flowTransBean
                        .getWorkFlowBean().getExtendInfo().getAdditionalInfo().contains(UPI_QR_CODE))) {
            UserDetailsBiz userDetailsBiz = flowTransBean.getUserDetails();
            if (userDetailsBiz != null) {
                extInfo.put(ExtendedInfoKeys.USER_MOBILE, userDetailsBiz.getMobileNo());
                extInfo.put(ExtendedInfoKeys.USER_EMAIL, userDetailsBiz.getEmail());
            }
        }
    }

    public ConsultWalletLimitsRequest createConsultWalletLimitRequest(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, UserDetailsBiz userDetails, boolean isGvPurchaseFlow, boolean isTransitWallet,
            String addMoneySource) {
        String orderId = orderDetail.getOrderId();
        // jira : PGP-19696
        String initialAmount = AmountUtils.getTransactionAmountInPaise(Optional.ofNullable(orderDetail)
                .map(InitiateTransactionRequestBody::getRiskFeeDetails).map(RiskFeeDetails::getInitialAmount)
                .map(com.paytm.pgplus.models.Money::getValue).orElse(null));
        String txnAmount = (StringUtils.isNotBlank(initialAmount)) ? initialAmount : AmountUtils
                .getTransactionAmountInPaise(orderDetail.getTxnAmount().getValue());
        String userId = (userDetails != null) ? userDetails.getUserId() : null;
        String payMode = request.getParameter("paymentMode");
        String targetPhoneNo = orderDetail.getTargetPhoneNo();
        String cardIndexNo = null;
        String cardHash = null;
        if (EPayMethod.CREDIT_CARD.getMethod().equals(payMode) || EPayMethod.DEBIT_CARD.getMethod().equals(payMode)) {
            try {
                cardHash = request.getParameter("cardHash"); // paytm app add
                // money
                if (StringUtils.isBlank(cardHash)) {
                    Map<String, String> cardAttributesMap = getCardAttributes(orderDetail, request, userDetails);
                    if (cardAttributesMap != null) {
                        cardIndexNo = cardAttributesMap.get(CARD_INDEX_NUMBER);
                        cardHash = cardAttributesMap.get(CARD_HASH);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Unable to get cardHash or cardIndexNo for walletConsult");
            }
        } else if (EPayMethod.NET_BANKING.getMethod().equals(payMode)) {
            cardIndexNo = request.getParameter("channelCode");
        }
        final ConsultWalletLimitsRequest walletConsultRequest = new ConsultWalletLimitsRequest(
                Double.valueOf(txnAmount), userId, orderId, payMode, cardIndexNo, cardHash);
        walletConsultRequest.setGvFlag(isGvPurchaseFlow);
        walletConsultRequest.setTransitWallet(isTransitWallet);
        walletConsultRequest.setTargetPhoneNo(targetPhoneNo);
        if (StringUtils.isNotBlank(orderDetail.getMid())
                && ff4JUtil.isFeatureEnabled(ENABLE_ADD_MONEY_SOURCE_IN_CONSULT, orderDetail.getMid())) {
            walletConsultRequest.setSource(addMoneySource);
        }
        LOGGER.debug("Created request for consult wallet limit as ::{}", walletConsultRequest);
        return walletConsultRequest;

    }

    private Map<String, String> getCardAttributes(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, UserDetailsBiz userDetails) throws SecurityException {
        String paymentDetails = getCardInfoFromRequest(request, orderDetail.getMid());
        if (StringUtils.isBlank(paymentDetails)) {
            return null;
        }

        String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
        int paymentDetailsArrayLength = actualPaymentDetailsArray.length;
        String cardNumber = null;
        Map<String, String> cardAttributesMap = new HashMap<>();
        String userId = Objects.nonNull(userDetails) ? userDetails.getUserId() : null;
        boolean isEnableGcinOnCoftWallet = ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET,
                orderDetail.getMid(), null, userId, false);
        if (paymentDetailsArrayLength == 4) {
            if (StringUtils.isNotBlank(actualPaymentDetailsArray[0])) {
                String cardIndexNoIfPresent = workFlowHelper.checkForCardIndexNo(paymentDetails);
                if (StringUtils.isNotBlank(cardIndexNoIfPresent)) {
                    cardIndexNoIfPresent = modifyCardIndexNumberForWallet(cardIndexNoIfPresent, orderDetail.getMid(),
                            isEnableGcinOnCoftWallet);
                    cardAttributesMap.put(CARD_INDEX_NUMBER, cardIndexNoIfPresent);
                    return cardAttributesMap;
                } else {
                    if (userDetails == null) {
                        return null;
                    }
                    String savedCardId = actualPaymentDetailsArray[0];
                    String custId = Optional.ofNullable(orderDetail).map(InitiateTransactionRequestBody::getUserInfo)
                            .map(com.paytm.pgplus.models.UserInfo::getCustId).orElse(null);
                    SavedCardResponse<SavedCardVO> savedCardsBean = savedCardsService.getSavedCardByCardId(
                            Long.parseLong(savedCardId), userDetails.getUserId(), custId, orderDetail.getMid());
                    if (savedCardsBean == null || !savedCardsBean.getStatus()
                            || savedCardsBean.getResponseData() == null) {
                        LOGGER.error("Unable to fetch savedCard");
                        return null;
                    }
                    cardNumber = savedCardsBean.getResponseData().getCardNumber();
                }

            } else if (StringUtils.isNotBlank(actualPaymentDetailsArray[1])) {
                cardNumber = actualPaymentDetailsArray[1];
            }
        }

        try {
            String cardHash = SignatureUtilWrapper.signApiRequest(cardNumber);
            String cardIndexNumber = getCardIndexNumberForWallet(cardNumber, isEnableGcinOnCoftWallet);
            cardAttributesMap.put(CARD_INDEX_NUMBER, cardIndexNumber);
            cardAttributesMap.put(CARD_HASH, cardHash);
            return cardAttributesMap;
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error in generating cardHash {}", e);
            return null;
        }

    }

    private String modifyCardIndexNumberForWallet(String cardIndexNumber, String mid, boolean isEnableGcinOnCoftWallet) {
        if (isEnableGcinOnCoftWallet) {
            if (cardIndexNumber.length() > 15 && cardIndexNumber.length() < 45) {
                return getGcinFromSavedCardId(mid, "TIN", cardIndexNumber);
            } else {
                QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                        .queryNonSensitiveAssetInfo(null, cardIndexNumber);
                if (Objects.nonNull(queryNonSensitiveAssetInfoResponse)) {
                    return queryNonSensitiveAssetInfoResponse.getCardInfo().getGlobalCardIndex();
                }
                return null;
            }
        } else {
            return cardIndexNumber;
        }
    }

    public String getGcinFromSavedCardId(String mid, String requestType, String requestValue) {
        FetchPanUniqueReferenceRequestBody fetchPanUniqueReferenceRequestBody = new FetchPanUniqueReferenceRequestBody(
                mid, requestType, requestValue);
        FetchPanUniqueReferenceRequest fetchPanUniqueReferenceRequest = new FetchPanUniqueReferenceRequest(
                new FetchPanUniqueReferenceRequestHead(), fetchPanUniqueReferenceRequestBody);
        try {
            FetchPanUniqueReferenceResponse fetchPanUniqueReferenceResponse = coftService.fetchPanUniqueReference(
                    fetchPanUniqueReferenceRequest, true);
            if (null != fetchPanUniqueReferenceResponse && null != fetchPanUniqueReferenceResponse.getBody()) {
                return fetchPanUniqueReferenceResponse.getBody().getGlobalPanIndex();
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching GCIN from {} : {} ", requestType, requestValue, e);
        }
        return null;
    }

    private String getCardIndexNumberForWallet(String cardNumber, boolean isEnableGcinOnCoftWallet) {
        if (isEnableGcinOnCoftWallet) {
            return workFlowHelper.getGcinFromCardNumber(cardNumber);
        } else {
            return workFlowHelper.getCardIndexNoFromCardNumber(cardNumber);
        }
    }

    private String getCardInfoFromRequest(HttpServletRequest request, String mid) throws SecurityException {
        if (StringUtils.isNotBlank(request.getParameter("encCardInfo"))) {
            String encryptedPaymentDetails = request.getParameter("encCardInfo");
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantMappingService
                    .getMerchantInfoResponse(mid);
            String merchantKey = Optional.ofNullable(merchantExtendedInfoResponse)
                    .map(MerchantExtendedInfoResponse::getExtendedInfo)
                    .map(MerchantExtendedInfoResponse.MerchantExtendedInfo::getEntityKey).orElse(null);
            String decryptMerchantKey = CryptoUtils.decrypt(merchantKey);
            return CryptoUtils.decrypt(encryptedPaymentDetails, decryptMerchantKey);
        } else {
            return request.getParameter("cardInfo");
        }
    }

    private void setAddNPayInPassThroughInfo(Map<String, String> channelInfo, WorkFlowRequestBean workFlowRequestBean) {

        if (!EPayMode.ADDANDPAY.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
            return;
        }
        String encodedPassThrough = channelInfo.get(PASS_THROUGH_EXTEND_INFO_KEY);
        HashMap<String, String> passThroughChannelExtendedInfoMap = new HashMap<>();

        if (StringUtils.isNotBlank(encodedPassThrough)) {
            String passThroughJson = new String(Base64.getDecoder().decode(encodedPassThrough));
            if (StringUtils.isNotBlank(passThroughJson)) {
                try {
                    passThroughChannelExtendedInfoMap = (HashMap<String, String>) JsonMapper.mapJsonToObject(
                            passThroughJson, Map.class);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                }
            }
        }

        passThroughChannelExtendedInfoMap.put(IS_ADD_AND_PAY, "true");
        setEncodedPassThroughExtendInfo(channelInfo, passThroughChannelExtendedInfoMap);
    }

    public void setZeroSubsInfoInPassThroughInfo(Map<String, String> channelInfo,
            WorkFlowRequestBean workFlowRequestBean) {

        String upiMandateValue = channelInfo.get(UPI_MANDATE);
        if ("true".equals(upiMandateValue)
                && org.apache.commons.lang.StringUtils.isNotBlank(workFlowRequestBean.getPaymentMid())
                && org.apache.commons.lang.StringUtils.isNotBlank(workFlowRequestBean.getPaymentOrderId())) {

            String encodedPassThrough = channelInfo.get(PASS_THROUGH_EXTEND_INFO_KEY);
            HashMap<String, String> passThroughChannelExtendedInfoMap = new HashMap<>();

            if (StringUtils.isNotBlank(encodedPassThrough)) {
                String passThroughJson = new String(Base64.getDecoder().decode(encodedPassThrough));
                if (StringUtils.isNotBlank(passThroughJson)) {
                    try {
                        passThroughChannelExtendedInfoMap = (HashMap<String, String>) JsonMapper.mapJsonToObject(
                                passThroughJson, Map.class);
                    } catch (Exception e) {
                        LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                    }
                }
            }

            passThroughChannelExtendedInfoMap.put(ZERO_RUPEE_TXN, "true");
            passThroughChannelExtendedInfoMap.put(ACTUAL_MID, workFlowRequestBean.getPaytmMID());
            passThroughChannelExtendedInfoMap.put(ACTUAL_ORDERID, workFlowRequestBean.getOrderID());

            setEncodedPassThroughExtendInfo(channelInfo, passThroughChannelExtendedInfoMap);
        }

        return;
    }

    public TokenizedCardsRequestBizBean createTokenizedCardsRequestBean(final WorkFlowTransactionBean flowTransBean)
            throws BaseException {
        String payerUserID = flowTransBean.getUserDetails() != null ? flowTransBean.getUserDetails()
                .getInternalUserId() : null;

        // if (!isCachingConfiguredForOfflineFlow(flowTransBean)) {
        // payerUserID = flowTransBean.getUserDetails() != null ?
        // flowTransBean.getUserDetails().getInternalUserId()
        // : null;
        // }
        // todo abhishek, saqib ask product
        // for flipkart flow since we are caching so we don't want to send user
        // id in tokenized cards request
        // if
        // (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getAccessToken())
        // &&
        // ff4jUtils.featureEnabledOnMultipleKeys(flowTransBean.getWorkFlowBean().getPaytmMID(),
        // payerUserID,
        // Ff4jFeature.BLACKLIST_LPV_FPOV2_WITH_ACCESS_TOKEN, false)) {
        // payerUserID = null;
        // }
        //
        // String alipayMid = "";
        // if (isAddAndPay) {
        // if (ff4JUtil.isFeatureEnabled(
        // TheiaConstant.ExtraConstants.THEIA_SEND_MERCHANT_ID_IN_ADD_AND_PAY_LITEPAYVIEWTASK,
        // flowTransBean
        // .getWorkFlowBean().getPaytmMID())) {
        // alipayMid = flowTransBean.getWorkFlowBean().getAlipayMID();
        // }
        // } else {
        // alipayMid = flowTransBean.getWorkFlowBean().getAlipayMID();
        // }
        // LOGGER.info("creating tokenized cards request");

        UserType userTargetType = null;
        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getCustID())) {
            if (StringUtils.isBlank(payerUserID)) {
                LOGGER.error("Neither user ID or cust ID found in workflow bean. "
                        + "Terminating tokenized consult task for MID {}", flowTransBean.getWorkFlowBean()
                        .getPaytmMID());
                throw new BaseException("User Id or cust Id mandatory for task to execute.");
            } else {
                userTargetType = UserType.PAYTM_USER_CARD;
            }
        } else if (StringUtils.isBlank(payerUserID)) {
            userTargetType = UserType.MERCHANT_USER_CARD;
        } else {
            userTargetType = UserType.PAYTM_AND_MERCHANT_USER_CARD;
        }
        final TokenizedCardsRequestBizBean consultRequest = new TokenizedCardsRequestBizBean(userTargetType,
                flowTransBean.getWorkFlowBean().getAlipayMID(), flowTransBean.getWorkFlowBean().getCustID(),
                payerUserID, true, true, flowTransBean.getWorkFlowBean().isAddNPayGlobalVaultCards(), flowTransBean
                        .getWorkFlowBean().isGlobalVaultCoftPreference(),
                flowTransBean.getWorkFlowBean().getPaytmMID(), flowTransBean.getWorkFlowBean().isNativeAddMoney(),
                flowTransBean.getWorkFlowBean().isStaticQrCode());

        LOGGER.info("Created request for tokenized cards Consult as ::{}", consultRequest);
        return consultRequest;
    }

    public Routes getRoute(WorkFlowTransactionBean flowTransBean, String api) {
        WorkFlowRequestBean requestBean = flowTransBean.getWorkFlowBean();
        requestBean.setRoute(Routes.PG2);
        return Routes.PG2;
    }

    public Routes getRoute(WorkFlowRequestBean requestBean, String api) {
        requestBean.setRoute(Routes.PG2);
        return Routes.PG2;
    }

    private void populateLoyaltyPointData(Map<String, String> passThroughChannelExtendedInfo,
            WorkFlowTransactionBean flowTransBean) {
        try {
            passThroughChannelExtendedInfo.put(BizConstant.LP_ROOT_USER_ID, flowTransBean.getWorkFlowBean()
                    .getLoyaltyPointRootUserId());
            String exchangeRate = flowTransBean.getWorkFlowBean().getPaymentRequestBean().getExchangeRate();
            if (StringUtils.isNotBlank(exchangeRate)) {
                String pointNumber = String.valueOf(Double.parseDouble(exchangeRate)
                        * (Double.parseDouble(flowTransBean.getWorkFlowBean().getTxnAmount()) / 100));
                passThroughChannelExtendedInfo.put(BizConstant.LP_EXCHANGE_RATE, exchangeRate);
                passThroughChannelExtendedInfo.put(BizConstant.LP_POINT, pointNumber);
                LOGGER.info("setting Lp Data in DirectPass exchangeRate = {} ,  pointNumber ={}", exchangeRate,
                        pointNumber);
            }
        } catch (Exception e) {
            LOGGER.error("Invalid exchangeRate for Loyalty Point : {} ", e);
        }
    }

    // setting EDC LINK TXN Data in ExtendeInfo
    private void populateEdcEmiPaymentInfo(ExtendedInfoRequestBean extendInfo, WorkFlowTransactionBean flowTransBean) {
        try {
            if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                    && flowTransBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null) {
                final EdcEmiDetails edcEmiFields = flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                        .getLinkDetailsData().getEdcEmiFields();
                if (edcEmiFields != null) {
                    List<EdcEmiBankOfferDetails> bankOffer = edcEmiFields.getEmiChannelDetail().getBankOfferDetails();
                    if (CollectionUtils.isNotEmpty(bankOffer))
                        extendInfo.setBankOffer(JsonMapper.mapObjectToJson(bankOffer));

                    extendInfo.setEmiSubventionOrderList(edcEmiFields.getProductId());
                    extendInfo.setPaytmTid(flowTransBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                            .getMerchantReferenceId());
                    extendInfo.setBrandName(edcEmiFields.getBrandName());
                    extendInfo.setBrandId(edcEmiFields.getBrandId());
                    extendInfo.setCategoryName(edcEmiFields.getCategoryName());
                    extendInfo.setCategoryId(edcEmiFields.getCategoryId());
                    extendInfo.setProductId(edcEmiFields.getProductId());
                    extendInfo.setProductName(edcEmiFields.getProductName());
                    extendInfo.setImeiKey(IMEI_KEY);
                    if (StringUtils.isNotBlank(edcEmiFields.getValidationKey())) {
                        extendInfo.setImei(edcEmiFields.getValidationValue());
                        extendInfo.setSerialNo(edcEmiFields.getValidationValue());
                    }
                    extendInfo.setEmiPlanId(edcEmiFields.getEmiChannelDetail().getPgPlanId());
                    extendInfo.setEmiTenure(edcEmiFields.getEmiChannelDetail().getEmiMonths());
                    extendInfo.setUserMobile(flowTransBean.getWorkFlowBean().getPaymentRequestBean()
                            .getLinkDetailsData().getCustomerMobile());
                    extendInfo.setEmiInterestRate(edcEmiFields.getEmiChannelDetail().getInterestRate());
                    extendInfo.setEmiTotalAmount(edcEmiFields.getEmiChannelDetail().getTotalAmount().getValue());
                    extendInfo.setBrandEmiAmount(edcEmiFields.getEmiChannelDetail().getEmiAmount().getValue());

                    extendInfo.setEdcLinkTxn(true);
                    extendInfo.setBrandInvoiceNumber(edcEmiFields.getBrandInvoiceNumber());
                    extendInfo.setEmiType(StringUtils.isNotBlank(edcEmiFields.getProductId()) ? EDC_BRAND_EMI_TYPE
                            : EDC_BANK_EMI_TYPE);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not set EdcEmiLinkPaymentInfo in ExtendedInfo{} ", e);
        }
    }

    private void setBinDetailInExtendedInfo(WorkFlowTransactionBean flowTransBean, Map<String, String> extendInfo) {

        if (extendInfo == null)
            return;
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getCardScheme())) {
            extendInfo.put(TheiaConstant.RequestParams.CARD_NAME, flowTransBean.getWorkFlowBean().getCardScheme());
        }
        if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getInstId())) {
            extendInfo.put(TheiaConstant.RequestParams.BANK_CODE_CBR, flowTransBean.getWorkFlowBean().getInstId());
        }
        if (flowTransBean.getWorkFlowBean().getUpiLiteRequestData() != null) {
            extendInfo.put(IS_UPILITE_TRANSACTION, "true");
        }
        extendInfo.put(TheiaConstant.RequestParams.IS_ACTIVE,
                String.valueOf(flowTransBean.getWorkFlowBean().isCardActive()));
        extendInfo.put(TheiaConstant.RequestParams.IS_INDIAN,
                String.valueOf(!Boolean.valueOf(flowTransBean.getWorkFlowBean().isInternationalCard())));
        extendInfo.put(TheiaConstant.RequestParams.CORPORATE_CARD,
                String.valueOf(flowTransBean.getWorkFlowBean().isCorporateCard()));
        extendInfo.put(TheiaConstant.RequestParams.PREPAID_CARD,
                String.valueOf(flowTransBean.getWorkFlowBean().isPrepaidCard()));
        extendInfo.put(TheiaConstant.RequestParams.CARD_TYPE,
                String.valueOf(flowTransBean.getWorkFlowBean().getCardType()));
    }

    private Map<String, String> buildDefaultChannelInfoForAoa() {
        Map<String, String> channelInfo = new HashMap<>();

        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARD_HOLDER_NAME,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.CARD_HOLDER_NAME);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2);
        return channelInfo;
    }

    private OrderModifyRequestBody.OrderModifyRequestBodyBuilder setRoutePg2OrderModify(
            WorkFlowRequestBean workFlowRequestBean,
            OrderModifyRequestBody.OrderModifyRequestBodyBuilder orderModifyRequestBodyBuilder) {
        if (getRoute(workFlowRequestBean, "orderModify") == Routes.PG2) {
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.setPaytmMerchantId(workFlowRequestBean
                    .getPaytmMID());
            orderModifyRequestBodyBuilder = orderModifyRequestBodyBuilder.setRoute(Routes.PG2);
        }
        return orderModifyRequestBodyBuilder;
    }

    private void updateGcinForCoftWalletTxn(WorkFlowTransactionBean flowTransBean) {

        if (ADDMONEY_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || TOPUP_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || flowTransBean.getWorkFlowBean().isNativeAddMoney()) {

            if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled()) {
                String payMethod = flowTransBean.getWorkFlowBean().getPayMethod();
                String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails()
                        .getUserId() : null;
                if ((EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                        || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                        || EPayMethod.EMI.getMethod().equals(payMethod) || EPayMethod.EMI_DC.getMethod().equals(
                        payMethod))
                        && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                                .getWorkFlowBean().getPaytmMID(), null, userId, false)) {
                    flowTransBean.getExtendInfo().put(ExtendedInfoKeys.CARD_INDEX_NUMBER,
                            flowTransBean.getWorkFlowBean().getGcin());
                }
            }
        }
    }

    private void updateGcinForCoftInPay(WorkFlowTransactionBean flowTransBean, List<BizPayOptionBill> payOptionBills) {
        String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails().getUserId()
                : null;
        if ((ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                .getWorkFlowBean().getPaytmMID(), null, userId, false) || ff4jUtils.isFeatureEnabledOnMid(flowTransBean
                .getWorkFlowBean().getPaytmMID(), ENABLE_GCIN_ON_COFT_PROMO, false))
                && StringUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getGcin())) {
            String gcin = flowTransBean.getWorkFlowBean().getGcin();
            flowTransBean.getExtendInfo().put(ExtendedInfoKeys.CARD_INDEX_NUMBER, gcin);
            payOptionBills.forEach(payOptionBill -> payOptionBill.getExtendInfo().put(
                    ExtendedInfoKeys.CARD_INDEX_NUMBER, gcin));
        }
    }

    private void updateGcinForCoftInCOP(WorkFlowTransactionBean flowTransBean, List<BizPayOptionBill> payOptionBills) {
        String userId = Objects.nonNull(flowTransBean.getUserDetails()) ? flowTransBean.getUserDetails().getUserId()
                : null;
        if ((ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET, flowTransBean
                .getWorkFlowBean().getPaytmMID(), null, userId, false) || ff4jUtils.isFeatureEnabledOnMid(flowTransBean
                .getWorkFlowBean().getPaytmMID(), ENABLE_GCIN_ON_COFT_PROMO, false))
                && StringUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getGcin())) {
            String gcin = flowTransBean.getWorkFlowBean().getGcin();
            flowTransBean.getWorkFlowBean().getExtendInfo().setCardIndexNo(gcin);
            payOptionBills.forEach(payOptionBill -> payOptionBill.getExtendInfo().put(
                    ExtendedInfoKeys.CARD_INDEX_NUMBER, gcin));
        }
    }

    private boolean isAddNPayTransaction(WorkFlowTransactionBean flowTransBean) {
        boolean isAddnPay = false;
        if (flowTransBean.getExtendInfo() != null) {
            // check for AddnPay
            if (TXN_TYPE_ADDNPAY.equalsIgnoreCase(flowTransBean.getExtendInfo().get("txnType"))) {
                isAddnPay = true;
            }
        }
        return isAddnPay;
    }

    private void getCustIdUserIdOnCoftConsentCondition(WorkFlowTransactionBean flowTransBean,
            Map<String, String> passThroughChannelExtendedInfo) throws Exception {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
        if (ff4JUtil.isFeatureEnabled(Ff4jFeature.GLOBAL_VAULT_COFT, flowTransBean.getWorkFlowBean().getPaytmMID())
                && !flowTransBean.getWorkFlowBean().isGlobalVaultCoftPreference()
                && flowTransBean.getWorkFlowBean().getCoftConsent() != null
                && flowTransBean.getWorkFlowBean().getCoftConsent().getUserConsent() == 1) {
            merchantExtendedInfoResponse = MappingServiceClient.getMerchantExtendedInfo(flowTransBean.getWorkFlowBean()
                    .getPaytmMID());

            boolean isOnPaytm = false;

            boolean isPaytmAddMoneyMid = (StringUtils.equals(flowTransBean.getWorkFlowBean().getPaytmMID(),
                    ConfigurationUtil.getTheiaProperty(BizConstant.MP_ADD_MONEY_MID)));

            if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null) {
                isOnPaytm = merchantExtendedInfoResponse.getExtendedInfo().isOnPaytm();
            }

            if (isOnPaytm
                    || ADDMONEY_EXPRESS.equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || EPayMode.ADDANDPAY.equals(flowTransBean.getWorkFlowBean().getPaytmExpressAddOrHybrid())
                    || (isPaytmAddMoneyMid || flowTransBean.getWorkFlowBean().isNativeAddMoney())
                    || (flowTransBean.getWorkFlowBean().isScanAndPayFlow() && ff4jUtils.isFeatureEnabledOnMid(
                            flowTransBean.getWorkFlowBean().getPaytmMID(), ENABLE_GLOBAL_VAULT_ON_STATIC_QR, false))) {
                passThroughChannelExtendedInfo.put(USER_ID, flowTransBean.getUserDetails().getUserId());
            } else {
                passThroughChannelExtendedInfo.put(CUST_ID, flowTransBean.getWorkFlowBean().getCustID());
            }
        } else {
            if (flowTransBean.getWorkFlowBean().getCustID() != null) {
                passThroughChannelExtendedInfo.put(CUST_ID, flowTransBean.getWorkFlowBean().getCustID());
            }
            if (flowTransBean.getUserDetails() != null && flowTransBean.getUserDetails().getUserId() != null) {
                passThroughChannelExtendedInfo.put(USER_ID, flowTransBean.getUserDetails().getUserId());
            }
        }

    }

    private boolean isTopUpTransaction(WorkFlowRequestBean workFlowRequestBean) {
        boolean isTopUpnpay = false;
        // check for topup
        ETransType transType = workFlowRequestBean.getTransType();
        if (transType != null && transType.equals(ETransType.TOP_UP)) {
            isTopUpnpay = true;
        }
        return isTopUpnpay;
    }

    private void updateRiskExtendedInfoForPG2(WorkFlowTransactionBean flowTransBean,
            Map<String, String> riskExtendInfo, String route) {
        try {
            EXT_LOGGER.info("Setting riskParameters for PG2");
            if (StringUtils.isNotBlank(route) && Routes.PG2.getName().equals(route)) {
                UserDetailsBiz userDetails = flowTransBean.getUserDetails();
                if (userDetails != null && !StringUtils.isBlank(userDetails.getMobileNo())) {
                    riskExtendInfo.put(USER_LOGIN_MOBILE, userDetails.getMobileNo());
                }

                if (!StringUtils.isBlank(flowTransBean.getWorkFlowBean().getCustID())) {
                    riskExtendInfo.put(BUYER_ID, flowTransBean.getWorkFlowBean().getCustID());
                }

                String orderDetailString = flowTransBean.getWorkFlowBean().getPaymentRequestBean().getOrderDetails();
                if (StringUtils.isNotBlank(orderDetailString) && JsonMapper.isValidJsonString(orderDetailString)) {
                    InitiateTransactionRequestBody orderDetail = null;
                    try {
                        orderDetail = JsonMapper.readValue(orderDetailString,
                                new TypeReference<InitiateTransactionRequestBody>() {
                                });
                    } catch (IOException e) {
                        LOGGER.error("Error while updating riskExtendedInfo for PG2 orderDetailString is {}",
                                orderDetailString);
                    }
                    if (orderDetail != null && CollectionUtils.isNotEmpty(orderDetail.getGoods())
                            && StringUtils.isNotBlank(orderDetail.getGoods().get(0).getCategory())) {
                        riskExtendInfo.put(ITEM_CATEGORY, orderDetail.getGoods().get(0).getCategory());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while updating riskExtendedInfo for PG2", e);
        }
    }

    public SaveAssetRequest createSaveAssetRequest(WorkFlowTransactionBean workFlowTransactionBean,
            CacheCardRequest cardRequest) {
        SaveAssetRequest request = new SaveAssetRequest();
        CacheCardRequestBody cardRequestBody = cardRequest.getBody();
        if (cardRequestBody != null) {
            request.setAssetNo(cardRequestBody.getCardNo());
            String InstNetworkCode = cardRequestBody.getInstNetworkCode();
            if (StringUtils.isNotBlank(InstNetworkCode)) {
                if (InstNetworkCode.equalsIgnoreCase(InstNetworkType.COFT.getNetworkType())) {
                    InstNetworkCode = "TOKEN";
                    request.setAssetType(InstNetworkCode);
                } else if (InstNetworkCode.equalsIgnoreCase(InstNetworkType.ISOCARD.getNetworkType())) {
                    InstNetworkCode = "ISO_CARD";
                    request.setAssetType(InstNetworkCode);
                }
            }
            request.setSource("Theia");
            if (cardRequestBody.getHolderName() != null) {
                request.setHolderFirstName(cardRequestBody.getHolderName().getFirstName());
                request.setHolderSecondName(cardRequestBody.getHolderName().getLastName());
            }
            request.setHolderMobileNo(cardRequestBody.getHolderMobileNo());
            request.setCvv2(cardRequestBody.getCvv2());
            request.setLast4ref(cardRequestBody.getLast4ref());
            request.setGlobalPanIndex(cardRequestBody.getGlobalPanIndex());
            request.setPar(cardRequestBody.getPar());
            request.setUniqueCardReference(cardRequestBody.getUniqueCardReference());
            request.setCacheExpiryMinutes(cardRequestBody.getCacheExpiryMinutes());
            request.setAssetExpireMonth(String.valueOf(cardRequestBody.getExpiryMonth()));
            request.setAssetExpireYear(String.valueOf(cardRequestBody.getExpiryYear()));
            if (Objects.nonNull(workFlowTransactionBean) && Objects.nonNull(workFlowTransactionBean.getWorkFlowBean())
                    && StringUtils.isNotEmpty(workFlowTransactionBean.getWorkFlowBean().getSavedCardID())) {
                request.setAssetIndexNumber(workFlowTransactionBean.getWorkFlowBean().getSavedCardID());
            }
        }
        return request;
    }

    public SaveAssetRequest createSaveAssetRequestForBankTransfer(WorkFlowTransactionBean workFlowTransactionBean,
            CacheCardRequest cardRequest) {
        SaveAssetRequest request = new SaveAssetRequest();
        CacheCardRequestBody cardRequestBody = cardRequest.getBody();
        if (cardRequestBody != null) {
            request.setAssetNo(cardRequestBody.getCardNo());
            request.setIfsc(cardRequestBody.getInstNetworkCode());
            request.setSource("Theia");
            if (cardRequestBody.getHolderName() != null) {
                request.setHolderFirstName(cardRequestBody.getHolderName().getFirstName());
                request.setHolderSecondName(cardRequestBody.getHolderName().getLastName());
            }
            if (Objects.nonNull(workFlowTransactionBean) && null != workFlowTransactionBean.getWorkFlowBean()
                    && null != workFlowTransactionBean.getWorkFlowBean().getBankName()) {
                request.setBankId(workFlowTransactionBean.getWorkFlowBean().getBankName());
            } else {
                request.setBankId(cardRequestBody.getInstNetworkCode());
            }
            request.setBranchId(cardRequestBody.getInstBranchId());
            request.setHolderMobileNo(cardRequestBody.getHolderMobileNo());
            request.setCacheExpiryMinutes(cardRequestBody.getCacheExpiryMinutes());
            request.setAssetType("BANK_ACCOUNT");
        }
        return request;
    }

    private void setPPBLAccountTypeInPassThrough(List<BizPayOptionBill> payOptionBills,
            WorkFlowRequestBean flowRequestBean) {
        HashMap<String, String> passThroughChannelExtendedInfoMap = new HashMap<>();
        for (BizPayOptionBill payOptionBill : payOptionBills) {
            if (payOptionBill.getChannelInfo() != null) {
                String encodedPassThrough = payOptionBill.getChannelInfo().get(PASS_THROUGH_EXTEND_INFO_KEY);
                if (StringUtils.isNotBlank(encodedPassThrough)) {
                    String passThroughJson = new String(Base64.getDecoder().decode(encodedPassThrough));
                    if (StringUtils.isNotBlank(passThroughJson)) {
                        try {
                            passThroughChannelExtendedInfoMap = (HashMap<String, String>) JsonMapper.mapJsonToObject(
                                    passThroughJson, Map.class);
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                        }
                    }
                    if (StringUtils.isNotBlank(flowRequestBean.getPpblAccountType())) {
                        passThroughChannelExtendedInfoMap.put(accountType, flowRequestBean.getPpblAccountType());
                    }
                    setEncodedPassThrough(payOptionBill.getChannelInfo(), passThroughChannelExtendedInfoMap);
                }
            }
        }
    }

    private void setNickname(final WorkFlowTransactionBean flowTransBean) {
        if (ObjectUtils.notEqual(flowTransBean.getUserDetails(), null)
                && StringUtils.isNotBlank(flowTransBean.getUserDetails().getUserName())) {
            String userName = null;
            userName = flowTransBean.getUserDetails().getUserName();
            if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                userName = userName.substring(0, NICKNAME_MAX_LENGTH);
            }
            flowTransBean.getExtendInfo().put(NICKNAME, userName);
        } else {
            String userName = getUserNameFromOrderDetails(flowTransBean);
            if (StringUtils.isNotBlank(userName)) {
                flowTransBean.getExtendInfo().put(NICKNAME, userName);
            }
        }
    }

    private String getUserNameFromOrderDetails(WorkFlowTransactionBean flowTransactionBean) {
        String userName = null;
        if (flowTransactionBean != null && flowTransactionBean.getWorkFlowBean() != null
                && flowTransactionBean.getWorkFlowBean().getOrderDetails() != null) {
            try {
                InitiateTransactionRequestBody orderDetails = JsonMapper.mapJsonToObject(flowTransactionBean
                        .getWorkFlowBean().getOrderDetails(), InitiateTransactionRequestBody.class);
                if (orderDetails != null && orderDetails.getUserInfo() != null) {
                    com.paytm.pgplus.models.UserInfo userInfo = orderDetails.getUserInfo();
                    if (StringUtils.isNotBlank(userInfo.getFirstName())
                            && !userInfo.getFirstName().trim().equalsIgnoreCase("NA")) {
                        userName = userInfo.getFirstName();
                    } else if (StringUtils.isNotBlank(userInfo.getLastName())
                            && !userInfo.getLastName().trim().equalsIgnoreCase("NA")) {
                        userName = userInfo.getLastName();
                    }
                    if (StringUtils.isNotBlank(userName) && userName.length() > NICKNAME_MAX_LENGTH) {
                        userName = userName.substring(0, NICKNAME_MAX_LENGTH);
                    }
                    EXT_LOGGER.info("User Name fetch from Merchant Provided User Info is : {}", userName);
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Parsing Failed due to :", e);
            }
        }
        return userName;
    }

    private void addPassThroughExtendedInfoForAOADqr(final WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) throws PaytmValidationException {
        try {
            Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();
            setChannelInfoForNativeJsonRequest(flowTransBean, passThroughExtendInfoMap);
            String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
            String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
            channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
            return;
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            LOGGER.error("Execption occured while adding passThroughExtendInfo for AOA Dqr : ", e);
            throw new PaytmValidationException(e);
        }
    }

    private void settingSubsDetailsInChannelInfo(WorkFlowTransactionBean flowTransactionBean,
            Map<String, String> channelInfo) {
        if (isSubscriptionCreationRequest(flowTransactionBean.getWorkFlowBean().getRequestType().getType())) {
            channelInfo.put(SUBSCRIPTION_REQUEST_TYPE, CREATE);
        }

        if (isSubscriptionRenewalRequest(flowTransactionBean.getWorkFlowBean().getRequestType().getType())) {
            channelInfo.put(SUBSCRIPTION_REQUEST_TYPE, RENEW);
        }
    }

    private void setBankFormOptimizedFlow(final WorkFlowTransactionBean flowTransBean, Map<String, String> channelInfo) {
        // For now this is only for OrderPayPSP UPI INTENT flow. Later this will
        // hold payOption value.
        if (flowTransBean.getWorkFlowBean() != null
                && flowTransBean.getWorkFlowBean().getBankFormOptimizationParams() != null
                && flowTransBean.getWorkFlowBean().getBankFormOptimizationParams().isBankFormOptimizedFlow()) {
            channelInfo.put("formOptimizedFlow", "UPI_INTENT");
        }
    }

    private void updatePayOptionBillsForBankTransferPG2(WorkFlowRequestBean workFlowRequestBean,
            List<BizPayOptionBill> payOptionBills) {
        if (null != payOptionBills) {
            for (BizPayOptionBill payOptionBill : payOptionBills) {
                if (null != payOptionBill && null == payOptionBill.getVanInfo()) {
                    payOptionBill.setVanInfo(workFlowRequestBean.getVanInfo());
                }
                if (null != payOptionBill && null != payOptionBill.getChannelInfo()
                        && null != workFlowRequestBean.getChannelInfo()) {
                    payOptionBill.getChannelInfo().put("errorCode",
                            workFlowRequestBean.getChannelInfo().get("errorCode"));
                    payOptionBill.getChannelInfo().put("failure", workFlowRequestBean.getChannelInfo().get("failure"));
                    payOptionBill.getChannelInfo().put("errorDescription",
                            workFlowRequestBean.getChannelInfo().get("errorDescription"));
                }
            }
        }
    }

    public Wallet2FAWorkflowIdRequest createWorkflowIdWallet2FARequest(WorkFlowTransactionBean workFlowTransBean,
            String txnToken) {
        Wallet2FAWorkflowIdRequest wallet2FAWorkflowIdRequest = new Wallet2FAWorkflowIdRequest();
        wallet2FAWorkflowIdRequest.setWorkflowName(PASSCODE_SET_W2FA_WEB);
        Wallet2FAInputParams inputParamsWallet2FAWeb = new Wallet2FAInputParams();
        inputParamsWallet2FAWeb.setToken(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getSsoToken());
        inputParamsWallet2FAWeb.setState(txnToken);
        String redirectUri = com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL)
                + TheiaConstant.ExtraConstants.WALLET_2FA_SET_PASSCODE_REDIRECT;
        inputParamsWallet2FAWeb.setRedirectUri(redirectUri);
        wallet2FAWorkflowIdRequest.setInputParams(inputParamsWallet2FAWeb);
        return wallet2FAWorkflowIdRequest;
    }

}
