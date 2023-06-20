package com.paytm.pgplus.biz.workflow.service.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaSubscriptionBinValidationRequest;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionBinValidationResponse;
//import com.paytm.pgplus.aoaSubscriptionClient.service.IAoaSubscriptionService;
import com.paytm.pgplus.biz.constants.BizConstant.ApiName;
import com.paytm.pgplus.biz.core.cachecard.service.ICacheCardInfoService;
import com.paytm.pgplus.biz.core.cachecard.utils.CacheCardInfoHelper;
import com.paytm.pgplus.biz.core.model.*;
import com.paytm.pgplus.biz.core.model.oauth.*;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.model.wallet.PGPlusWalletDecisionMakerRequestBizBean;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.notification.service.IPayRequestNotifierService;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.core.payment.utils.AOAUtils;
import com.paytm.pgplus.biz.core.payment.utils.FeeHelper;
import com.paytm.pgplus.biz.core.payment.utils.PGPlusWalletDecisionMaker;
import com.paytm.pgplus.biz.core.promo.PromoCheckoutFeatureFF4jData;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskConvenienceFee;
import com.paytm.pgplus.biz.core.risk.RiskUtil;
import com.paytm.pgplus.biz.core.risk.RiskVerifierCacheProcessingPayload;
import com.paytm.pgplus.biz.core.user.service.*;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.enums.*;
import com.paytm.pgplus.biz.exception.*;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder.CheckoutPaymentPromoReqBuilderFactory;
import com.paytm.pgplus.biz.workflow.emiSubventioncheckout.requestbuilder.EmiSubventionCheckoutReqBuilderFactory;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.biz.workflow.service.ICreateOrderAndPay;
import com.paytm.pgplus.biz.workflow.service.IPay;
import com.paytm.pgplus.biz.workflow.service.factory.PayAndCopFactory;
import com.paytm.pgplus.biz.workflow.service.factory.PayAndCopImplEnum;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.biz.workflow.walletconsult.WalletConsultService;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cache.util.CryptoUtils;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.*;
import com.paytm.pgplus.common.model.link.PaymentFormDetails;
import com.paytm.pgplus.common.responsecode.models.CommonResponseCode;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequest;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.*;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.OrderInfo;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.PaymentDetails;
import com.paytm.pgplus.facade.affordabilityPlatform.models.request.UserInfo;
import com.paytm.pgplus.facade.affordabilityPlatform.models.response.APOrderCheckoutResponse;
import com.paytm.pgplus.facade.affordabilityPlatform.service.IAffordabilityService;
import com.paytm.pgplus.facade.bankOauth.models.*;
import com.paytm.pgplus.facade.bankOauth.services.IBankOauthService;
import com.paytm.pgplus.facade.cart.interfaces.IChannelPaymentDetailsService;
import com.paytm.pgplus.facade.cart.model.ChannelPaymentDetailsHeaderRequest;
import com.paytm.pgplus.facade.cart.model.ChannelPaymentDetailsRequest;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.common.model.SecurityPolicyResult;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.emisubvention.constants.EmiSubventionConstants;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.models.AmountBearer;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.request.CheckoutRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.CheckoutWithOrderRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.OrderStampRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.CheckOutResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.OrderStampResponse;
import com.paytm.pgplus.facade.emisubvention.service.IEmiSubventionService;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.HealthCategory;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.kyb.IKybDataService;
import com.paytm.pgplus.facade.kyb.model.request.KybDataServiceRequest;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitAccumulationQueryResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitConsumptionResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitQueryResponse;
import com.paytm.pgplus.facade.merchantlimit.services.IMerchantLimitService;
import com.paytm.pgplus.facade.notification.response.SendMessageResponse;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.payment.models.response.UPIPushInitiateResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentPromoServiceNotifyMsg;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentPromoServiceNotifyMsgV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.Cart;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.CheckoutPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.Item;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.Product;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.CheckoutPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.*;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.PromoUsageData;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoServiceNotify;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoServiceNotifyV2;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoSevice;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.ppb.models.*;
import com.paytm.pgplus.facade.ppb.services.IPaymentsBankAccountQuery;
import com.paytm.pgplus.facade.risk.enums.BizSceneEnum;
import com.paytm.pgplus.facade.risk.enums.RiskResultEnum;
import com.paytm.pgplus.facade.risk.models.request.RiskRequest;
import com.paytm.pgplus.facade.risk.models.request.RiskRequestBody;
import com.paytm.pgplus.facade.risk.models.response.RiskResponse;
import com.paytm.pgplus.facade.risk.services.IRisk;
import com.paytm.pgplus.facade.user.models.*;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.CardBinHashRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.request.SaveAssetRequest;
import com.paytm.pgplus.facade.user.models.response.*;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.facade.user.services.IAssetCenterService;
import com.paytm.pgplus.facade.user.services.ICardInfoQueryService;
import com.paytm.pgplus.facade.utils.*;
import com.paytm.pgplus.facade.wallet.models.*;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.*;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.subscription.request.ListSubscriptionRequest;
import com.paytm.pgplus.payloadvault.subscription.response.ListSubscriptionResponse;
import com.paytm.pgplus.payloadvault.subscription.response.SubscriptionDetail;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo.UrlTypeId;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.subscriptionClient.enums.SubscriptionRequestType;
import com.paytm.pgplus.subscriptionClient.model.request.FreshSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.request.ModifySubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.request.RenewSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.subscriptionClient.utils.PropertiesUtil;
import com.paytm.pgplus.subscriptionClient.utils.SubscriptionUtil;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.PostpaidServiceHelper;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.enums.AddMoneySourceEnum.*;
import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.common.constant.CommonConstant.ADD_MONEY_PHASE2_ENABLE_FLAG;
import static com.paytm.pgplus.common.enums.ERequestType.ADDMONEY_EXPRESS;
import static com.paytm.pgplus.common.enums.ERequestType.TOPUP_EXPRESS;
import static com.paytm.pgplus.facade.constants.FacadeConstants.BANK_OAUTH_CLIENT_ID;
import static com.paytm.pgplus.facade.constants.FacadeConstants.CLIENT_PARAM;
import static com.paytm.pgplus.facade.user.constants.AssetServiceConstant.AssetServiceKeys.CLIENT_SECRET_KEY;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.NATIVE_TXN_INITIATE_REQUEST;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.NATIVE_REQUEST_SAVED_VPAS_APP_VERSION_ANDROID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.NATIVE_REQUEST_SAVED_VPAS_APP_VERSION_IOS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.SHOW_UPI_QR;
import com.paytm.pgplus.biz.core.merchant.service.IMerchantCenterService;

/**
 * @author namanjain, AmitD
 */
@Service("workFlowHelper")
public class WorkFlowHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowHelper.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(WorkFlowHelper.class);

    private static final String FAILURE = "FAILURE";
    private static final String UPI_USER_NOT_FOUND_RESPONSE_CODE = "514";
    private static final int subsUpiRecurringkeyExpiryTimeInSeconds = 900;
    private static final String SUBSCRIPTION = "SUBSCRIPTION";
    private static final HashSet<ERequestType> BFSI_MERCHANTS_REQUEST_TYPES_SET = new HashSet<ERequestType>(
            Arrays.asList(ERequestType.NATIVE_MF, ERequestType.NATIVE_MF_PAY, ERequestType.NATIVE_ST,
                    ERequestType.NATIVE_ST_PAY, ERequestType.NATIVE_MF_SIP, ERequestType.NATIVE_MF_SIP_PAY));

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderService;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginService;

    @Autowired
    private EmiSubventionCheckoutReqBuilderFactory emiSubventionCheckoutReqBuilderFactory;

    @Autowired
    @Qualifier("pgPlusWalletDecisionMaker")
    private PGPlusWalletDecisionMaker pgPlusWalletDecisionMakerService;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardsService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtility;

    @Autowired
    @Qualifier("cachecardinfoservice")
    private ICacheCardInfoService cacheCardInfoService;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    @Qualifier("subscriptionUtil")
    private SubscriptionUtil subscriptionUtil;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    @Qualifier("paymentsBankServiceImpl")
    private IPaymentsBankService paymentsBankService;

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCardService;

    @Autowired
    private PostpaidServiceHelper postPaidServiceHelper;

    @Autowired
    private MerchantVelocityUtil merchantVelocityUtil;

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    @Qualifier("paymentPromoService")
    private IPaymentPromoSevice paymentPromoService;

    @Autowired
    @Qualifier("emiSubventionService")
    private IEmiSubventionService emiSubventionService;

    @Autowired
    @Qualifier("paymentPromoServiceNotify")
    private IPaymentPromoServiceNotify paymentPromoServiceNotify;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("kybDataService")
    private IKybDataService kybDataService;

    @Autowired
    private RiskUtil riskUtil;

    @Autowired
    @Qualifier("walletQRCodeServiceImpl")
    private IWalletQRCodeService walletQRCodeService;

    @Autowired
    @Qualifier("lookupDataServiceImpl")
    private ILookupDataInfoService lookupDataInfoService;

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    IBankInfoDataService bankInfoDataService;

    @Autowired
    IRisk riskImpl;

    @Autowired
    @Qualifier("userMappingServiceImpl")
    IUserMappingService userMappingService;

    @Autowired
    private IEMIDetails emiDetailsClient;

    @Autowired
    @Qualifier(value = "merchantLimitService")
    private IMerchantLimitService merchantLimitService;

    @Autowired
    private NpciHealthUtil npciHealthUtil;

    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetFacade;

    @Autowired
    private IChannelPaymentDetailsService channelPaymentDetailsService;

    @Autowired
    @Qualifier("BizProdHelper")
    private BizProdHelper bizProdHelper;

    @Autowired
    private MerchantBizProdHelper merchantBizProdHelper;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    ICardInfoQueryService cardInfoQueryService;

    @Autowired
    private IPgpFf4jClient pgpFf4jClient;

    @Autowired
    private WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private SimplifiedSubventionHelper simplifiedSubventionHelper;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("payRequestNotifierService")
    private IPayRequestNotifierService payRequestNotifierService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("paymentPromoServiceNotifyV2")
    private IPaymentPromoServiceNotifyV2 paymentPromoServiceNotifyV2;

    @Autowired
    @Qualifier("walletConsultServiceImpl")
    private WalletConsultService walletConsultService;

    @Autowired
    private IAuthService authService;

    @Autowired
    private AtomicSequenceGenerator atomicSequenceGenerator;

    @Autowired
    @Qualifier("paymentsBankAccountQueryImpl")
    private IPaymentsBankAccountQuery paymentsBankAccountQuery;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("payAndCopFactory")
    private PayAndCopFactory payAndCopFactory;

    @Autowired
    @Qualifier("aoaUtil")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("pg2Utilities")
    private PG2Utilities pg2Utilities;

    @Autowired
    private PG2Util pg2Util;

    // @Autowired
    // private IAoaSubscriptionService aoaSubscriptionService;

    @Autowired
    @Qualifier("assetService")
    private IAssetCenterService assetCenterService;

    @Autowired
    private Environment environment;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    private IAffordabilityService affordabilityService;

    @Autowired
    @Qualifier("cardBinHelper")
    private CardBinHelper cardBinHelper;

    @Autowired
    private IBankOauthService bankOauthService;

    @Autowired
    private Ff4jUtils ff4jUtil;

    @Autowired
    private IMerchantCenterService merchantCenterService;

    private static final String TLS_WARNING_DEFAULT_MSG = "Card payment is disabled as your device does not support required security protocol. Please update your browser or device if possible.";
    private static final String PASSCODE_VALIDATION_ERRMSG_CHECK1 = "attempts";
    private static final String PASSCODE_VALIDATION_ERRMSG_CHECK2 = "disabled";
    private static final String REDEMPTION_TYPE_DISCOUNT = "discount";
    private static final String REDEMPTION_TYPE_CASHBACK = "cashback";
    private static final String REDEMPTION_TYPE_PAYTM_CASHBACK = "paytm_cashback";
    private static final String DISABLE_PROMO_CHECKOUT_RETRY = "theia.disablePromoCheckoutRetry";
    private static final String PROMO_CHECKOUT_RETRY_COUNT = "promoCheckoutRetryCount";
    private static final String SAVE_PROMO_CHECKOUT_DATA = "theia.savePromoCheckoutData";
    private static final String PROMO_CHECKOUT_RESPONSE_DETAILS = "promoCheckoutResponseDetails";
    private static final String MODIFY_LOOPER_TIMEOUT_TO_FETCH_BANKFORM = "theia.modifyLooperTimeoutToFetchBankForm";
    private static ObjectMapper mapper = new ObjectMapper();
    private static String ISOCARD = "1001";
    private static String GLOBAL_CARD_INDEX_SEP = "&";
    private static String JWT_KEY_GCIN = "gcin.secret.key";

    /*
     * This Service Validates request Bean, Both Generic and Request Specific
     * validation are done here
     */
    /*
     * This Service Validates request Bean, Both Generic and Request Specific
     * validation are done here
     */

    public GenericCoreResponseBean<Boolean> specificBeanValidation(WorkFlowRequestBean flowRequestBean,
            IValidator validatorService) {
        long threadStartTime = System.currentTimeMillis();
        final GenericCoreResponseBean<Boolean> result = validatorService.validate(flowRequestBean);
        long timeConsumedByThread = System.currentTimeMillis() - threadStartTime;
        EventUtils.pushTheiaEvents(EventNameEnum.SPECIFIC_BEAN_VALIDATION, new ImmutablePair<>(
                "SPECIFIC_BEAN_VALIDATION", String.valueOf(timeConsumedByThread)));

        if (!result.isSuccessfullyProcessed() || !result.getResponse()) {
            return new GenericCoreResponseBean<>(result.getFailureMessage(), result.getResponseConstant());
        }

        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

    public GenericCoreResponseBean<Boolean> beanValidation(WorkFlowRequestBean flowRequestBean,
            IValidator validatorService) {

        final GenericFlowRequestBeanValidator<WorkFlowRequestBean> bean = new GenericFlowRequestBeanValidator<WorkFlowRequestBean>(
                flowRequestBean);

        ValidationResultBean validationResultBean = bean.validate();

        if (!validationResultBean.isSuccessfullyProcessed()) {

            String failureDescription = StringUtils.isNotBlank(bean.getErrorMessage()) ? bean.getErrorMessage()
                    : "Validation Failed";

            return new GenericCoreResponseBean<>(failureDescription, validationResultBean.getResponseConstant());
        }
        return specificBeanValidation(flowRequestBean, validatorService);
    }

    /**
     * Consult AddMoney returns True or False
     */
    public GenericCoreResponseBean<Boolean> consultAddMoney(WorkFlowTransactionBean workFlowTransBean) {
        final ConsultWalletLimitsRequest walletConsultRequest = workRequestCreator
                .createConsultWalletLimitRequest(workFlowTransBean);
        final boolean isAddMoneyAllowedByWallet = bizPaymentService.walletLimitsConsult(walletConsultRequest);
        LOGGER.info("Is Add Money Allowed By Wallet Response : {}", isAddMoneyAllowedByWallet);

        return new GenericCoreResponseBean<>(Boolean.valueOf(isAddMoneyAllowedByWallet));
    }

    /* specifies addMoney destination */
    public GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoneyV2(WorkFlowTransactionBean workFlowTransBean) {
        final ConsultWalletLimitsRequest walletConsultRequest = workRequestCreator
                .createConsultWalletLimitRequest(workFlowTransBean);
        final BizWalletConsultResponse addMoneyConsultResponse = bizPaymentService
                .walletLimitsConsultV2(walletConsultRequest);
        if (workFlowTransBean.isAddMoneyPcfEnabled() && addMoneyConsultResponse != null
                && CollectionUtils.isNotEmpty(addMoneyConsultResponse.getFeeDetails())) {
            String payMethod = workFlowTransBean.getWorkFlowBean().getPayMethod();
            List<Map<String, Object>> feeDetails = addMoneyConsultResponse.getFeeDetails();
            getWalletConsultFeeRateCode(feeDetails, payMethod, workFlowTransBean);
        }
        LOGGER.info("Add money consult response for v2/walletLimits : {}", addMoneyConsultResponse);

        return new GenericCoreResponseBean<>(addMoneyConsultResponse);
    }

    public GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoneyV2(String userId, String orderId,
            String txnAmnt, String addMoneySource) {
        final ConsultWalletLimitsRequest walletConsultRequest = workRequestCreator.createConsultWalletLimitRequest(
                userId, orderId, txnAmnt, addMoneySource);
        final BizWalletConsultResponse addMoneyConsultResponse = bizPaymentService
                .walletLimitsConsultV2(walletConsultRequest);
        LOGGER.info("Add money consult response for v2/walletLimits : {}", addMoneyConsultResponse);

        return new GenericCoreResponseBean<>(addMoneyConsultResponse);
    }

    private void getWalletConsultFeeRateCode(List<Map<String, Object>> feeDetails, String addNPayMethod,
            WorkFlowTransactionBean flowTransactionBean) {
        for (Map<String, Object> feeItem : feeDetails) {
            if (MapUtils.isNotEmpty(feeItem)) {
                String payMethod = (String) feeItem.get(PAYMENT_METHOD);
                if (StringUtils.isNotBlank(payMethod) && payMethod.equalsIgnoreCase(addNPayMethod)
                        && StringUtils.isNotBlank((String) feeItem.get(FEE_RATE_CODE))) {
                    String feeRateCode = (String) feeItem.get(FEE_RATE_CODE);
                    LOGGER.info("payMethod {}, feeRateCode {}", payMethod, feeRateCode);
                    flowTransactionBean.setFeeRateCode(feeRateCode);
                    flowTransactionBean.getWorkFlowBean().setFeeRateCode(feeRateCode);
                }
            }
        }
    }

    private void setMerchantVeloctyLimitInExtendInfo(WorkFlowRequestBean workFlowBean) {
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

    // Pay API
    public GenericCoreResponseBean<BizPayResponse> pay(WorkFlowTransactionBean workFlowTransBean) {
        if (Boolean.TRUE.equals(workFlowTransBean.getWorkFlowBean().getEdcLinkTxn())) {
            IPay payService = payAndCopFactory.getPayService(PayAndCopImplEnum.EDL_LINK_PAYMENT_PAY.getValue());
            return payService.processPayRequest(workFlowTransBean);
        } else {
            return getGenericPayResponse(workFlowTransBean);
        }
    }

    public boolean isSimplifiedFlow(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.isProcessSimplifiedEmi()
                || Objects.nonNull(workFlowRequestBean.getSimplifiedPaymentOffers());
    }

    private GenericCoreResponseBean convertRiskVerificationToRiskReject() {
        return convertRiskVerificationToRiskReject(null);
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

    // Create Top UP
    public GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUp(WorkFlowTransactionBean workFlowTransBean,
            boolean isRouteDecided) {
        final CreateTopUpRequestBizBean createTopUpReqBizBean = workRequestCreator
                .createTopUpRequestBizBean(workFlowTransBean);

        Routes route = workFlowTransBean.getWorkFlowBean().getRoute();
        if (!isRouteDecided || null == route) {
            route = pg2Util.getRouteForTopUpRequest(workFlowTransBean.getWorkFlowBean().getPaytmMID());
            workFlowTransBean.getWorkFlowBean().setRoute(route);
        }

        Map<String, String> distributionMap = new HashMap<>();
        distributionMap.put("TransType", TransType.TOP_UP.name());
        distributionMap.put("env", route.getName());
        EventUtils.pushTheiaEvents(EventNameEnum.TRAFFIC_DISTRIBUTION, distributionMap);

        if (Routes.PG2.equals(route)) {
            createTopUpReqBizBean.setMerchantId(workFlowTransBean.getWorkFlowBean().getPaytmMID());
            createTopUpReqBizBean.setPayerUserId(workFlowTransBean.getUserDetails().getUserId());
            createTopUpReqBizBean.getExtInfoReqBean().setPplusUserId(
                    workFlowTransBean.getUserDetails().getInternalUserId());
            createTopUpReqBizBean.getExtInfoReqBean().setRoute(Routes.PG2);
        }

        final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUPResponse = orderService
                .createTopUp(createTopUpReqBizBean);
        LOGGER.debug("CreateTopUp response is : {}", createTopUPResponse);

        if (!createTopUPResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Create TopUp Failed Due to Reason : {}", createTopUPResponse.getFailureMessage());
            return new GenericCoreResponseBean<CreateTopUpResponseBizBean>(createTopUPResponse.getFailureMessage(),
                    createTopUPResponse.getResponseConstant());
        }

        LOGGER.debug("Create topup is successfully processed");
        return createTopUPResponse;
    }

    /**
     * Consult PayView , for Merchant
     */
    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView(
            WorkFlowTransactionBean workFlowTransBean) {
        return consultPayView(workFlowTransBean, false);
    }

    /**
     * Consult PayView , if boolean true , then fetch for AddAndPay which
     * changes request creation way
     **/
    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView(
            WorkFlowTransactionBean workFlowTransBean, boolean addAndPay) {
        final ConsultPayViewRequestBizBean consultViewRequestBean = workRequestCreator.createConsultRequestBean(
                workFlowTransBean, addAndPay);
        GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultViewResponseBean;
        consultViewResponseBean = bizPaymentService.consultPayView(consultViewRequestBean);

        if (!consultViewResponseBean.isSuccessfullyProcessed()) {
            LOGGER.error("Consult PayView Failed due to reason : {}", consultViewResponseBean.getFailureMessage());
            return new GenericCoreResponseBean<>(consultViewResponseBean.getFailureMessage(),
                    consultViewResponseBean.getResponseConstant());
        }
        setTLSWarning(consultViewResponseBean.getResponse(), workFlowTransBean.getWorkFlowBean().getRequestType(),
                addAndPay);
        if (!addAndPay || isTLSHeaderPresent()) {
            consultViewResponseBean = filterPaymentModes(workFlowTransBean, consultViewResponseBean.getResponse());
        }

        LOGGER.info("Consult Payview is successfully processed");
        return consultViewResponseBean;
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult(
            WorkFlowTransactionBean workFlowTransBean) {
        // addAndPay flag sent true in case of addMoney request
        boolean addAndPay = false;
        if (workFlowTransBean.getWorkFlowBean() != null && workFlowTransBean.getWorkFlowBean().isNativeAddMoney()) {
            addAndPay = true;
        }
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> response = litePayviewConsult(workFlowTransBean,
                addAndPay);
        enrichUserBalancesInLitePayviewConsultResponse(workFlowTransBean, response);
        return response;
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultForAddAndPay(
            WorkFlowTransactionBean workFlowTransBean) {
        return litePayviewConsult(workFlowTransBean, true);
    }

    private void enrichUserBalancesInLitePayviewConsultResponse(WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> response) {
        WorkFlowRequestBean workFlowBean = workFlowTransBean.getWorkFlowBean();
        if (response.isSuccessfullyProcessed() && StringUtils.isNotBlank(workFlowBean.getToken())
                && StringUtils.isNotBlank(workFlowBean.getPaytmMID())
                && StringUtils.isNotBlank(workFlowBean.getTxnAmount())
                && isCachingConfiguredForOfflineFlow(workFlowTransBean)) {
            LOGGER.info("Setting digital credit balance");
            fetchAndSetDigitalCreditBalance(workFlowBean, workFlowTransBean, null, false);
        }

    }

    private void fetchAndSetDigitalCreditBalance(WorkFlowRequestBean workFlowBean,
            WorkFlowTransactionBean workFlowTransBean, WorkFlowResponseBean workFlowResponseBean,
            boolean setResponseForAddAndPayLiteViewConsult) {

        boolean isPayModAllowForMerchantLiteViewConsult = isPayMethodConfiguredOnMerchant(
                workFlowTransBean.getMerchantLiteViewConsult(), EPayMethod.PAYTM_DIGITAL_CREDIT);
        boolean isPayModAllowForAddAndPayLiteViewConsult = isPayMethodConfiguredOnMerchant(
                workFlowTransBean.getAddAndPayLiteViewConsult(), EPayMethod.PAYTM_DIGITAL_CREDIT);

        if ((isPayModAllowForMerchantLiteViewConsult || isPayModAllowForAddAndPayLiteViewConsult)
                && workFlowTransBean.getUserDetails() != null && workFlowTransBean.getUserDetails().isPaytmCCEnabled()) {

            PaytmDigitalCreditRequest request = getPaytmDigitalCreditRequest(workFlowBean.getPaytmMID(),
                    workFlowBean.getTxnAmount());
            // TODO : Reverting changes for PGP-37201 - Until LMS contract
            // changes are live in prod.
            // if (workFlowBean.isAddnpayPreferenceOnMerchant()) {
            // request.setServiceType("ADD_PAY");
            // }
            try {
                PaytmDigitalCreditResponse response = postPaidServiceHelper.checkBalance(request, workFlowTransBean
                        .getUserDetails().getUserId());

                if (workFlowResponseBean != null)
                    workFlowResponseBean.setPaytmCCResponse(response);

                if (isPayModAllowForMerchantLiteViewConsult)
                    setDigitalCreditCheckBalanceResponse(workFlowTransBean.getMerchantLiteViewConsult(), response);

                if (isPayModAllowForAddAndPayLiteViewConsult && setResponseForAddAndPayLiteViewConsult)
                    setDigitalCreditCheckBalanceResponse(workFlowTransBean.getAddAndPayLiteViewConsult(), response);

            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception while fetching digital credit balance {}", ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public PaytmDigitalCreditResponse getPaytmDigitalCreditBalanceResponse(PaytmDigitalCreditRequest request,
            String customerId) throws FacadeCheckedException {
        return postPaidServiceHelper.checkBalance(request, customerId);
    }

    public boolean isPayMethodConfiguredOnMerchant(LitePayviewConsultResponseBizBean litePayviewConsultResponse,
            EPayMethod ePayMethod) {
        if (litePayviewConsultResponse == null || litePayviewConsultResponse.getPayMethodViews() == null) {
            return false;
        }
        for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponse.getPayMethodViews()) {
            if (ePayMethod.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                List<PayChannelOptionViewBiz> payChannelOptionViewBizs = payMethodViewsBiz.getPayChannelOptionViews();
                if (payChannelOptionViewBizs != null && payChannelOptionViewBizs.size() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setDigitalCreditCheckBalanceResponse(LitePayviewConsultResponseBizBean litePayviewConsultResponse,
            PaytmDigitalCreditResponse digitalCreditResponse) {
        if (litePayviewConsultResponse == null || digitalCreditResponse == null
                || digitalCreditResponse.getStatusCode() != 0
                || CollectionUtils.isEmpty(digitalCreditResponse.getResponse())) {
            return;
        }
        List<PayMethodViewsBiz> payMethodViewsBizs = litePayviewConsultResponse.getPayMethodViews();
        if (payMethodViewsBizs == null)
            return;
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                List<PayChannelOptionViewBiz> payChannelOptionViewBizs = payMethodViewsBiz.getPayChannelOptionViews();
                if (payChannelOptionViewBizs != null && payChannelOptionViewBizs.size() == 1) {
                    List<ExternalAccountInfoBiz> externalAccountInfoBizs = new ArrayList<>();
                    ExternalAccountInfoBiz externalAccountInfoBiz = new ExternalAccountInfoBiz();
                    externalAccountInfoBizs.add(externalAccountInfoBiz);
                    CheckBalanceResponse checkBalanceResponse = digitalCreditResponse.getResponse().get(0);
                    String extendInfo = getExtendInfoForDigitalCreditBalanceResponse(checkBalanceResponse);
                    if (StringUtils.isNotBlank(extendInfo)) {
                        // converting balance in paise to keep it consistent
                        // with alipay response
                        long balanceInPaise = (long) (checkBalanceResponse.getAmount() * 100);
                        externalAccountInfoBiz.setAccountBalance(String.valueOf(balanceInPaise));
                        externalAccountInfoBiz.setExternalAccountNo(checkBalanceResponse.getAccountID());
                        externalAccountInfoBiz.setExtendInfo(extendInfo);

                        payChannelOptionViewBizs.get(0).setExternalAccountInfos(externalAccountInfoBizs);
                    }
                }
            }
        }
    }

    public String getExtendInfoForDigitalCreditBalanceResponse(CheckBalanceResponse checkBalanceResponse) {
        Map<String, String> extendInfo = new HashMap<>();
        extendInfo.put(LENDER_ID, checkBalanceResponse.getLender());
        extendInfo.put(LENDER_DESCRIPTION, checkBalanceResponse.getLenderDescription());
        extendInfo.put(DISPLAY_REQUIRED, String.valueOf(checkBalanceResponse.isDisplay()));
        extendInfo.put(PASSCODE_REQUIRED, String.valueOf(checkBalanceResponse.isPasscodeRequired()));
        extendInfo.put(OTP_REQUIRED, String.valueOf(checkBalanceResponse.isOtpRequired()));
        extendInfo.put(ACCOUNT_STATUS, String.valueOf(checkBalanceResponse.getAccountStatus()));
        extendInfo.put(INFO_BUTTON_MESSAGE, String.valueOf(checkBalanceResponse.getInfoButtonMessage()));
        extendInfo.put(DISPLAY_MESSAGE, String.valueOf(checkBalanceResponse.getDisplayMessage()));
        extendInfo.put(KYC_CODE, String.valueOf(checkBalanceResponse.getKycCode()));
        extendInfo.put(KYC_SET_NAME, String.valueOf(checkBalanceResponse.getKycSetName()));
        extendInfo.put(KYC_VERSION, String.valueOf(checkBalanceResponse.getKycVersion()));

        try {
            return JsonMapper.mapObjectToJson(extendInfo);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception in converting digital credit extendInfo to json {} ",
                    ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private PaytmDigitalCreditRequest getPaytmDigitalCreditRequest(String mid, String amount) {
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
        paytmDigitalCreditRequest.setPgmid(mid);
        if (StringUtils.isNotBlank(amount))
            paytmDigitalCreditRequest.setAmount(Double.parseDouble(amount));
        paytmDigitalCreditRequest.setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        return paytmDigitalCreditRequest;
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult(
            WorkFlowTransactionBean workFlowTransBean, boolean addAndPay) {
        final long startTime = System.currentTimeMillis();
        try {
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBean = getLitePayviewConsultResponseFromCache(
                    workFlowTransBean, addAndPay);
            if (null != litePayviewConsultResponseBean) {
                LOGGER.debug("litePayviewConsult is serving from cache for mid : {}", workFlowTransBean
                        .getWorkFlowBean().getPaytmMID());
                setTLSWarning(litePayviewConsultResponseBean.getResponse(), workFlowTransBean.getWorkFlowBean()
                        .getRequestType(), addAndPay);
                if (!addAndPay || isTLSHeaderPresent()) {
                    litePayviewConsultResponseBean = filterPaymentModes(workFlowTransBean,
                            litePayviewConsultResponseBean.getResponse());
                }
                return litePayviewConsultResponseBean;
            }

            final LitePayviewConsultRequestBizBean litePayviewConsultRequestBean = workRequestCreator
                    .createLitePayviewConsultRequestBean(workFlowTransBean, addAndPay);

            litePayviewConsultResponseBean = bizPaymentService.litePayviewConsult(litePayviewConsultRequestBean);

            if (!litePayviewConsultResponseBean.isSuccessfullyProcessed()) {
                LOGGER.error("litePayviewConsult Failed due to reason : {} for mid {}", litePayviewConsultResponseBean
                        .getFailureMessage(), workFlowTransBean.getWorkFlowBean().getPaytmMID());
                return new GenericCoreResponseBean<>(litePayviewConsultResponseBean.getFailureMessage(),
                        litePayviewConsultResponseBean.getResponseConstant());
            }
            setTLSWarning(litePayviewConsultResponseBean.getResponse(), workFlowTransBean.getWorkFlowBean()
                    .getRequestType(), addAndPay);
            setLitePayviewConsultResponseInCache(workFlowTransBean, addAndPay, litePayviewConsultResponseBean);
            if (!addAndPay || isTLSHeaderPresent()) {
                litePayviewConsultResponseBean = filterPaymentModes(workFlowTransBean,
                        litePayviewConsultResponseBean.getResponse());
            }
            return litePayviewConsultResponseBean;
        } finally {
            LOGGER.debug("Total time taken for litePayviewConsult api  is {} ms", System.currentTimeMillis()
                    - startTime);
        }

    }

    private boolean isTLSHeaderPresent() {
        return httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null;
    }

    private HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private void setLitePayviewConsultResponseInCache(WorkFlowTransactionBean workFlowTransBean, boolean addAndPay,
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBean) {
        if (addAndPay) {
            setAddAndPayLitePayviewConsultResponseInCache(litePayviewConsultResponseBean);
        } else {
            if (isCachingConfiguredForOfflineFlow(workFlowTransBean)) {
                enableUserBalanceFlagForOffline(workFlowTransBean, litePayviewConsultResponseBean);
            }
            // Hack for UPI Express
            setLitePayviewConsultResponseInCache(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                    litePayviewConsultResponseBean);
        }
    }

    private GenericCoreResponseBean<LitePayviewConsultResponseBizBean> getLitePayviewConsultResponseFromCache(
            WorkFlowTransactionBean workFlowTransBean, boolean addAndPay) {
        if (addAndPay) {
            return getAddAndPayLitePayviewConsultResponse();
        } else if (isCachingConfiguredForOfflineFlow(workFlowTransBean)) {
            return getLitePayViewConsultResponse(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        }
        return null;
    }

    private void enableUserBalanceFlagForOffline(WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBean) {
        Set<String> payMethodSet = new HashSet<>();
        payMethodSet.add(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        payMethodSet.add(EPayMethod.BALANCE.getMethod());

        if (null != litePayviewConsultResponseBean.getResponse()
                && !CollectionUtils.isEmpty(litePayviewConsultResponseBean.getResponse().getPayMethodViews())) {

            for (PayMethodViewsBiz payViewBiz : litePayviewConsultResponseBean.getResponse().getPayMethodViews()) {

                if (StringUtils.isNotEmpty(payViewBiz.getPayMethod())
                        && payMethodSet.contains(payViewBiz.getPayMethod())
                        && !CollectionUtils.isEmpty(payViewBiz.getPayChannelOptionViews())) {
                    for (PayChannelOptionViewBiz payOption : payViewBiz.getPayChannelOptionViews()) {
                        payOption.setEnableStatus(true);
                        payOption.setDisableReason(null);
                    }
                }
            }
        }
    }

    private boolean isCachingConfiguredForOfflineFlow(WorkFlowTransactionBean workFlowTransBean) {
        String isCachingConfigured = ConfigurationUtil.getProperty(BizConstant.IS_LITEPAYVIEW_CONSULT_CACHE_ENABLED,
                "false");
        return workFlowTransBean.getWorkFlowBean().isOfflineFetchPayApi()
                && BooleanUtils.toBoolean(isCachingConfigured);
    }

    /**
     * @param workFlowTransBean
     * @param token
     * @param fetchSavedCards
     * @return
     */
    public GenericCoreResponseBean<UserDetailsBiz> fetchUserDetails(WorkFlowTransactionBean workFlowTransBean,
            String token, boolean fetchSavedCards) {
        long startTime = System.currentTimeMillis();
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        try {
            userDetails = loginService.fetchUserDetails(token, fetchSavedCards, workFlowTransBean.getWorkFlowBean());
            LOGGER.debug("UserDetails found for the token ::{}", token);

            if (!userDetails.isSuccessfullyProcessed()) {
                StatisticsLogger.logForXflush("PGPLUS", "OAUTH", null, "response", "user details fetching failed",
                        userDetails.getFailureMessage());
                LOGGER.error("user details fetching failed due to ::{}", userDetails.getFailureMessage());
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            userDetails.getResponse().setUserToken(workFlowTransBean.getWorkFlowBean().getToken());
            LOGGER.info("PAYTM_DIGITAL_CREDIT STATUS : {} , PAYMENTS BANK ACCOUNT REGISTERED : {}", userDetails
                    .getResponse().isPaytmCCEnabled(), userDetails.getResponse().isSavingsAccountRegistered());

            return userDetails;
        } finally {
            LOGGER.info("Total time taken for fetching user details : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    public void fetchSavedCardsForUserDetail(WorkFlowRequestBean workFlowRequestBean, UserDetailsBiz userDetails,
            boolean fetchSavedCards) {
        try {
            loginService.fetchSavedCards(workFlowRequestBean, userDetails, fetchSavedCards);
            userDetails.setUserToken(workFlowRequestBean.getToken());
            LOGGER.info("PAYTM_DIGITAL_CREDIT STATUS : {} , PAYMENTS BANK ACCOUNT REGISTERED : {}",
                    userDetails.isPaytmCCEnabled(), userDetails.isSavingsAccountRegistered());
        } catch (Exception e) {
            LOGGER.error("Error while fetching SavedCards {}", e);
        }
    }

    public GenericCoreResponseBean<UserDetailsBiz> fetchUserDetailsNoSavedCards(
            WorkFlowTransactionBean workFlowTransBean, String token) {
        long startTime = System.currentTimeMillis();
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        try {
            userDetails = loginService.fetchUserDetailsNoSavedCards(workFlowTransBean.getWorkFlowBean(), token);
            LOGGER.debug("UserDetails found for the token ::{}", token);

            if (!userDetails.isSuccessfullyProcessed()) {
                LOGGER.error("user details fetching failed due to ::{} ", userDetails.getFailureMessage());
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            userDetails.getResponse().setUserToken(workFlowTransBean.getWorkFlowBean().getToken());
            LOGGER.info("PAYTM_DIGITAL_CREDIT STATUS : {} , PAYMENTS BANK ACCOUNT REGISTERED : {}", userDetails
                    .getResponse().isPaytmCCEnabled(), userDetails.getResponse().isSavingsAccountRegistered());

            return userDetails;
        } finally {
            LOGGER.info("Total time taken for fetching user details : {} ms", System.currentTimeMillis() - startTime);
        }

    }

    // TODO: This function works for current account also. V4 version of ppbl
    // api doesnot support it.
    public GenericCoreResponseBean<AccountBalanceResponse> fetchAccountBalance(WorkFlowTransactionBean workFlowTransBean) {
        long startTime = System.currentTimeMillis();
        GenericCoreResponseBean<AccountBalanceResponse> fetchBalanceResponse = null;
        try {
            fetchBalanceResponse = paymentsBankService.fetchSavingsAccountBalance(workFlowTransBean);
            if (!fetchBalanceResponse.isSuccessfullyProcessed()) {
                LOGGER.error("Failed to fetch savings account details from Bank Middleware :: {} , user Id :: {} ",
                        fetchBalanceResponse.getFailureDescription(),
                        workFlowTransBean.getUserDetails() != null ? workFlowTransBean.getUserDetails().getUserId()
                                : null);
                return new GenericCoreResponseBean<>(fetchBalanceResponse.getFailureDescription());
            }
            return fetchBalanceResponse;
        } finally {
            LOGGER.info("Total time taken for fetching user savings account details from Bank Middleware : {} ms",
                    System.currentTimeMillis() - startTime);
        }
    }

    public boolean paymentsBankAllowed(WorkFlowTransactionBean workFlowTransBean) {
        boolean decision = false;
        switch (workFlowTransBean.getAllowedPayMode()) {
        case ADDANDPAY:
            if (workFlowTransBean.getAddAndPayViewConsult() != null
                    && workFlowTransBean.getAddAndPayViewConsult().isPaymentsBankSupported()
                    && workFlowTransBean.getUserDetails() != null
                    && workFlowTransBean.getUserDetails().isSavingsAccountRegistered()) {
                decision = true;
            }
            break;
        case HYBRID:
        case NONE:
            if (workFlowTransBean.getMerchantViewConsult() != null
                    && workFlowTransBean.getMerchantViewConsult().isPaymentsBankSupported()
                    && workFlowTransBean.getUserDetails() != null
                    && workFlowTransBean.getUserDetails().isSavingsAccountRegistered()) {
                decision = true;
            }
            break;
        default:
            break;
        }
        LOGGER.info("For allowed paymode : {} , returning flag for fetch payments bank balance : {}",
                workFlowTransBean.getAllowedPayMode(), decision);
        return decision;
    }

    /**
     * Check for UPI_PUSH / UPI_PUSH_EXPRESS
     */
    public boolean checkUPIPUSHEnabled(WorkFlowTransactionBean workflowTransBean) {
        // check first if UPI_PUSH/UPI_PUSH_EXPRESS enabled on the
        // merchant(Includes cases for both NONE,HYBRID)
        if (null != workflowTransBean.getMerchantViewConsult()
                && !CollectionUtils.isEmpty(workflowTransBean.getMerchantViewConsult().getPayMethodViews())) {
            workflowTransBean.setMerchantUpiPushEnabled(containsPayChannelInPaymethod(workflowTransBean
                    .getMerchantViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH));
            workflowTransBean.setMerchantUpiPushExpressEnabled(containsPayChannelInPaymethod(workflowTransBean
                    .getMerchantViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS));
        }
        // Also check if this transaction supports AddNPay and set
        // UpiPushEnabled flag accordingly
        if (EPayMode.ADDANDPAY.equals(workflowTransBean.getAllowedPayMode())) {
            if (null != workflowTransBean.getAddAndPayViewConsult()
                    && !CollectionUtils.isEmpty(workflowTransBean.getAddAndPayViewConsult().getPayMethodViews())) {
                workflowTransBean.setAddUpiPushEnabled(containsPayChannelInPaymethod(workflowTransBean
                        .getAddAndPayViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH));
                workflowTransBean.setAddUpiPushExpressEnabled(containsPayChannelInPaymethod(workflowTransBean
                        .getAddAndPayViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS));
            }
        }
        return (workflowTransBean.isMerchantUpiPushEnabled() || workflowTransBean.isMerchantUpiPushExpressEnabled()
                || workflowTransBean.isAddUpiPushEnabled() || workflowTransBean.isAddUpiPushExpressEnabled());
    }

    public boolean checkExpressEnabledInNative(WorkFlowTransactionBean workflowTransBean) {
        // check first if UPI_PUSH/UPI_PUSH_EXPRESS enabled on the
        // merchant(Includes cases for both NONE,HYBRID)
        if (null != workflowTransBean.getMerchantViewConsult()
                && !CollectionUtils.isEmpty(workflowTransBean.getMerchantViewConsult().getPayMethodViews())) {
            workflowTransBean.setMerchantUpiPushExpressEnabled(containsPayChannelInPaymethod(workflowTransBean
                    .getMerchantViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS));
        }
        // Also check if this transaction supports AddNPay and set
        // UpiPushEnabled flag accordingly
        if (EPayMode.ADDANDPAY.equals(workflowTransBean.getAllowedPayMode())) {
            if (null != workflowTransBean.getAddAndPayViewConsult()
                    && !CollectionUtils.isEmpty(workflowTransBean.getAddAndPayViewConsult().getPayMethodViews())) {
                workflowTransBean.setAddUpiPushExpressEnabled(containsPayChannelInPaymethod(workflowTransBean
                        .getAddAndPayViewConsult().getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS));
            }
        }
        return (workflowTransBean.isMerchantUpiPushExpressEnabled() || workflowTransBean.isAddUpiPushExpressEnabled());
    }

    private boolean containsPayChannelInPaymethod(List<PayMethodViewsBiz> payMethod, String payOption) {

        for (PayMethodViewsBiz payMode : payMethod) {
            if (EPayMethod.UPI.getMethod().equals(payMode.getPayMethod())) {
                for (PayChannelOptionViewBiz payChannel : payMode.getPayChannelOptionViews()) {
                    if (payOption.equals(payChannel.getPayOption()) && payChannel.isEnableStatus())
                        return true;
                }
            }
        }
        return false;

    }

    public List<String> getSarvatraVPAList(PaytmVpaDetails vpaDetails) {
        List<String> vpalist = new ArrayList<>();
        if (null != vpaDetails && !CollectionUtils.isEmpty(vpaDetails.getVpaDetails())) {
            for (SarvatraVpaDetails vpaIterator : vpaDetails.getVpaDetails()) {
                if (!vpalist.contains(vpaIterator.getName())) {
                    vpalist.add(vpaIterator.getName());
                }
            }
        }
        return vpalist;
    }

    /**
     * @param workFlowTransBean
     * @return
     */
    public boolean validateSavedCardForSubscription(final WorkFlowTransactionBean workFlowTransBean,
            SubscriptionRequestType subscriptionRequestType) {

        /**
         * Checking whether txn is by cardIndexNumber and it belongs to the user
         * or not
         */
        String savedCardId = workFlowTransBean.getWorkFlowBean().getSavedCardID();
        boolean isCardIndexNumberBelongToUser = StringUtils.isNotEmpty(savedCardId)
                && !savedCardId.chars().allMatch(Character::isDigit) && isCardIndexNumberBoundToUser(workFlowTransBean) ? true
                : false;
        return isSavedCardBoundToUser(workFlowTransBean, isCardIndexNumberBelongToUser)
                && isSavedCardBoundToFlow(workFlowTransBean, subscriptionRequestType);
    }

    /**
     * This method checks whether the saved card that is passed in the request
     * is applicable for the specific flow.
     *
     * @param workFlowTransBean
     * @return
     */
    private boolean isSavedCardBoundToFlow(WorkFlowTransactionBean workFlowTransBean,
            SubscriptionRequestType subscriptionRequestType) {
        BinDetail binDetail = getBinToBeUsed(workFlowTransBean);
        SubsPaymentMode subsTypes = workFlowTransBean.getWorkFlowBean().getSubsPayMode();
        return subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail, subsTypes, subscriptionRequestType);
    }

    private BinDetail getBinToBeUsed(WorkFlowTransactionBean workFlowTransBean) {
        BinDetail binDetail = null;
        String savedCardId = workFlowTransBean.getWorkFlowBean().getSavedCardID();
        if (StringUtils.isEmpty(savedCardId)) {
            return binDetail;
        }
        if (!savedCardId.chars().allMatch(Character::isDigit)) {
            binDetail = new BinDetail();
            binDetail.setCardType(workFlowTransBean.getWorkFlowBean().getCardType());
            binDetail.setCardName(workFlowTransBean.getWorkFlowBean().getCardScheme());
            binDetail.setBankCode(workFlowTransBean.getWorkFlowBean().getBankCode());
        } else {
            for (CardBeanBiz cardBeanBiz : workFlowTransBean.getUserDetails().getMerchantViewSavedCardsList()) {
                if (cardBeanBiz.getCardId().toString().equals(savedCardId)) {
                    binDetail = mappingUtility.getBinDetail(cardBeanBiz.getFirstSixDigit());
                }
            }
        }
        return binDetail;
    }

    /**
     * This method checks whether the saved card that is passed in the request
     * belongs to the user.
     *
     * @param workFlowTransBean
     * @return
     */
    private boolean isSavedCardBoundToUser(final WorkFlowTransactionBean workFlowTransBean,
            boolean isCardIndexNumberBoundToUser) {
        switch (workFlowTransBean.getWorkFlowBean().getSubsTypes()) {
        case CC_ONLY:
        case DC_ONLY:
        case NORMAL:
            if (isCardIndexNumberBoundToUser) {
                return true;
            }
            for (CardBeanBiz cardBeanBiz : workFlowTransBean.getUserDetails().getMerchantViewSavedCardsList()) {
                if (cardBeanBiz.getCardId().toString().equals(workFlowTransBean.getWorkFlowBean().getSavedCardID())) {
                    workFlowTransBean.getWorkFlowBean().setCardIndexNo(cardBeanBiz.getCardIndexNo());
                    workFlowTransBean.getWorkFlowBean().setCardNo(cardBeanBiz.getCardNumber());
                    return true;
                }
            }
            break;
        case PPI_ONLY:
            return true;
        case PPBL_ONLY:
            return true;
        default:
            break;
        }
        return false;
    }

    /**
     * @param workFlowTransBean
     * @return
     */
    public GenericCoreResponseBean<BizCreateOrderResponse> createOrder(WorkFlowTransactionBean workFlowTransBean) {
        return createOrder(workFlowTransBean, false);
    }

    /**
     * @param workFlowTransBean
     * @param isTimeOutZero
     * @return
     */
    public GenericCoreResponseBean<BizCreateOrderResponse> createOrder(WorkFlowTransactionBean workFlowTransBean,
            boolean isTimeOutZero) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransBean.getWorkFlowBean();
        BizCreateOrderRequest createOrderReqBizBean = workRequestCreator.createOrderRequestBean(workFlowTransBean,
                isTimeOutZero);
        GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = orderService
                .createOrder(createOrderReqBizBean);

        if (!createOrderResponse.isSuccessfullyProcessed()) {
            if (createOrderResponse.getResponseConstant().equals(ResponseConstants.SUCCESS_IDEMPOTENT_ERROR)) {
                LOGGER.error("Success Idempotent Error in Create order reason : {}",
                        createOrderResponse.getFailureMessage());
                return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                        createOrderResponse.getAcquirementId(), createOrderResponse.getResponseConstant());
            } else {
                String requestType = workFlowRequestBean.getRequestType() == null ? null : workFlowRequestBean
                        .getRequestType().toString();
                StatisticsLogger.logForXflush(MDC.get("MID"), "Alipay", requestType, "response",
                        "Create Order Failed due to reason", createOrderResponse.getFailureMessage());
                LOGGER.error("Create Order Failed due to reason : {}", createOrderResponse.getFailureMessage());
                EventUtils.pushTheiaEvents(EventNameEnum.CREATE_ORDER_FAILED, new ImmutablePair<>(
                        "Create Order failed due to reason ", createOrderResponse.getFailureMessage()));
            }
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }

        LOGGER.info("Create order is successfully processed");
        return createOrderResponse;
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
            LOGGER.error("Exception in validating and updating merchant velociy");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private boolean bypassVelocity(String mid) {
        return AllowedMidCustidPropertyUtil.isMidCustIdEligible(mid, null,
                BizConstant.MerchantVelocity.BYPASS_VELOCITY_MIDS, BizConstant.MerchantVelocity.NONE, false);
    }

    private void rollbackMerchantVelocityLimitUpdate(final String mid, final String txnAmt,
            final ExtendedInfoRequestBean extendedInfoRequestBean, boolean isTxnAmtInPaise) {
        try {
            // LOGGER.info("rolling back merchant velocity limit for mid={} ,txnAmount={} , isTxnAmtInPaise = {}",
            // mid,
            // txnAmt, isTxnAmtInPaise);
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
            LOGGER.error("Exception in rolling back merchant velocity limit update");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * @param workFlowTransBean
     * @return
     */
    public EPayMode allowedPayMode(WorkFlowTransactionBean workFlowTransBean) {

        if (SubsTypes.NORMAL.equals(workFlowTransBean.getWorkFlowBean().getSubsTypes())) {
            return EPayMode.ADDANDPAY;
        }

        PGPlusWalletDecisionMakerRequestBizBean pgPlusWalletRequestBean = workRequestCreator
                .createPGPlusWalletRequestBean(workFlowTransBean);
        EPayMode allowedPayMode = pgPlusWalletDecisionMakerService.allowedPayMode(pgPlusWalletRequestBean,
                workFlowTransBean).getResponse();

        if (EPayMode.ADDANDPAY_KYC.equals(allowedPayMode)) {
            workFlowTransBean.setOnTheFlyKYCRequired(true);
            allowedPayMode = EPayMode.ADDANDPAY;
            LOGGER.info("On the fly KYC required for userId : {}", workFlowTransBean.getUserDetails().getUserId());
        }

        if (EPayMode.NONE_KYC.equals(allowedPayMode)) {
            LOGGER.info("On the fly KYC required for userId : {}", workFlowTransBean.getUserDetails().getUserId());
            workFlowTransBean.setOnTheFlyKYCRequired(true);
            allowedPayMode = EPayMode.NONE;
        }

        if (isWalletDisabled(workFlowTransBean)
                || ERequestType.RESELLER.name().equals(workFlowTransBean.getWorkFlowBean().getRequestType().name())
                || workFlowTransBean.getWorkFlowBean().isOfflineFetchPayApi()) {
            return EPayMode.NONE;
        }

        return allowedPayMode;
    }

    private boolean isWalletDisabled(WorkFlowTransactionBean workFlowTransBean) {

        List<String> allowedPayModes = workFlowTransBean.getWorkFlowBean().getAllowedPaymentModes();
        List<String> disabledPayModes = workFlowTransBean.getWorkFlowBean().getDisabledPaymentModes();

        if ((allowedPayModes != null && !allowedPayModes.isEmpty() && !allowedPayModes.contains(EPayMethod.BALANCE
                .toString()))
                || (allowedPayModes == null && disabledPayModes != null && disabledPayModes.contains(EPayMethod.BALANCE
                        .toString()))) {
            return true;
        }

        return false;
    }

    public EPayMode allowedPayMode(Map<String, String> extendInfo) {
        if (extendInfo == null || extendInfo.isEmpty())
            return EPayMode.NONE;
        EPayMode allowedPayMode = (pgPlusWalletDecisionMakerService.isAddAndPayAllowedByMerchant(extendInfo)) ? EPayMode.ADDANDPAY
                : pgPlusWalletDecisionMakerService.isHybridPayEnabled(extendInfo) ? EPayMode.HYBRID : EPayMode.NONE;
        LOGGER.debug("Allowed PayMode Is : {}", allowedPayMode);
        return allowedPayMode;
    }

    /*
     * VerifYLogin , using authCode
     */
    public GenericCoreResponseBean<VerifyLoginResponseBizBean> verifyLogin(WorkFlowTransactionBean workFlowTransBean) {
        VerifyLoginRequestBizBean verifyLoginRequestBean = workRequestCreator
                .createVerifyLoginRequestBean(workFlowTransBean);
        GenericCoreResponseBean<VerifyLoginResponseBizBean> verifyLoginResponse = loginService
                .verfifyLogin(verifyLoginRequestBean);
        LOGGER.debug("Verify Login Response : {}", verifyLoginResponse);
        if (!verifyLoginResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Verification of user failed due to : {}", verifyLoginResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(verifyLoginResponse.getFailureMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        }

        return verifyLoginResponse;
    }

    @Deprecated
    public GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse(WorkFlowTransactionBean workFlowTransBean) {
        ConsultFeeRequest consultFeeRequestBean = workRequestCreator.createConsultFeeRequest(workFlowTransBean);
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = bizPaymentService
                .consultConvenienceFees(consultFeeRequestBean);
        LOGGER.debug("Consult Fee Response Is : {}", consultFeeResponse);

        if (!consultFeeResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Consult Fee Response failed due to : {}", consultFeeResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(consultFeeResponse.getFailureMessage(),
                    consultFeeResponse.getResponseConstant());
        }
        return new GenericCoreResponseBean<>(consultFeeResponse.getResponse());
    }

    public GenericCoreResponseBean<ConsultFeeResponse> consultBulkFeeResponse(WorkFlowTransactionBean workFlowTransBean) {
        ConsultFeeRequest consultFeeRequestBean = workRequestCreator.createConsultFeeRequest(workFlowTransBean);
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = bizPaymentService
                .consultBulkConvenienceFees(consultFeeRequestBean);
        LOGGER.debug("Consult Fee Response Is : {}", consultFeeResponse);

        if (!consultFeeResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Consult Fee Failed due to : {}", consultFeeResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(consultFeeResponse.getFailureMessage(),
                    consultFeeResponse.getResponseConstant());
        }
        return new GenericCoreResponseBean<>(consultFeeResponse.getResponse());
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SUBSCRIPTION_CREATE_FRESH_CONTRACT)
    public SubscriptionResponse processFreshSubscriptionContrat(WorkFlowTransactionBean workFlowTransBean) {
        FreshSubscriptionRequest freshSubscriptionRequest = workRequestCreator
                .createFreshSubscriptionRequest(workFlowTransBean);
        SubscriptionResponse freshSubscriptionResponse = subscriptionService
                .processFreshSubscription(freshSubscriptionRequest);
        LOGGER.debug("Response from subscription service : {}", freshSubscriptionResponse);
        return freshSubscriptionResponse;
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SUBSCRIPTION_CREATE_RENEWAL_CONTRACT)
    public SubscriptionResponse processRenewSubscription(WorkFlowTransactionBean workFlowTransBean) {
        RenewSubscriptionRequest renewSubscriptionRequest = workRequestCreator
                .createRenewalRequestBean(workFlowTransBean);
        SubscriptionResponse subscriptionServiceResponse = subscriptionService
                .renewSubscription(renewSubscriptionRequest);
        return subscriptionServiceResponse;
    }

    /**
     * Function to fetch active subscriptions with ONDEMAND frequency
     *
     * @param workFlowTransBean
     */
    public void processToFetchActiveSubscriptions(WorkFlowTransactionBean workFlowTransBean) {
        ListSubscriptionRequest request = workRequestCreator
                .createFetchActiveSubscriptionsRequestBean(workFlowTransBean);
        ListSubscriptionResponse listSubscriptionResponse = subscriptionService.fetchActiveSubscriptions(request);
        setActiveSubscriptionDetails(workFlowTransBean, listSubscriptionResponse);
    }

    private void setActiveSubscriptionDetails(WorkFlowTransactionBean workFlowTransBean,
            ListSubscriptionResponse listSubscriptionResponse) {
        if (listSubscriptionResponse != null && listSubscriptionResponse.getBody() != null) {
            List<SubscriptionDetail> subscriptionDetails = listSubscriptionResponse.getBody().getSubscriptionDetails();

            // Filter subscriptions as per Allowed Paymethods
            List<PayMethodViewsBiz> payMethodViews = workFlowTransBean.getMerchantLiteViewConsult().getPayMethodViews();
            List<String> payMethods = new ArrayList<>();
            payMethodViews.forEach(method -> payMethods.add(method.getPayMethod()));

            List<SubscriptionDetail> filteredSubscriptions = null;
            if (subscriptionDetails != null) {
                filteredSubscriptions = subscriptionDetails.stream()
                        .filter(subs -> payMethods.contains(subs.getSubscriptionType().getePayMethodName()))
                        .collect(Collectors.toList());
            }
            //

            List<ActiveSubscriptionBeanBiz> list = new ArrayList<>();
            if (filteredSubscriptions != null) {
                filteredSubscriptions.stream().forEach(subscriptionDetail -> {
                    ActiveSubscriptionBeanBiz bean = new ActiveSubscriptionBeanBiz();
                    bean.setSubscriptionId(subscriptionDetail.getSubscriptionId());
                    bean.setSavedCardId(subscriptionDetail.getSavedCardId());
                    bean.setAccountNumber(subscriptionDetail.getIssuerBankAccNo());
                    bean.setAccountHolderName(subscriptionDetail.getAccountHolderName());
                    bean.setBankName(subscriptionDetail.getIssuerBankName());
                    bean.setBankIFSC(subscriptionDetail.getIssuerBankIfsc());
                    bean.setPaymentMode(subscriptionDetail.getSubscriptionType().getePayMethodName());
                    list.add(bean);
                });
            }
            workFlowTransBean.setActiveSubscriptions(list);
            return;
        }
        LOGGER.warn("Unable to set Active subscriptions due to unavailable data");
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SUBSCRIPTION_CREATE_RENEWAL_CONTRACT)
    public SubscriptionResponse processPartialRenewSubscription(WorkFlowTransactionBean workFlowTransBean) {
        RenewSubscriptionRequest renewSubscriptionRequest = workRequestCreator
                .createRenewalRequestBean(workFlowTransBean);
        SubscriptionResponse subscriptionServiceResponse = subscriptionService
                .partialRenewSubscription(renewSubscriptionRequest);
        return subscriptionServiceResponse;
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SUBSCRIPTION_ACTIVATE_CONTRACT)
    public SubscriptionResponse processActivateSubscription(WorkFlowTransactionBean workFlowTransBean) {
        ModifySubscriptionRequest modifySubscriptionRequestBean = workRequestCreator
                .createModifyRequest(workFlowTransBean);
        modifySubscriptionRequestBean.setCardIndexNumber(workFlowTransBean.getWorkFlowBean().getCardIndexNo());
        SubscriptionResponse modifySubscriptionResponse = subscriptionService
                .activateSubscription(modifySubscriptionRequestBean);
        LOGGER.debug("Activate Subscription Response : {}", modifySubscriptionResponse);
        return modifySubscriptionResponse;
    }

    public GenericCoreResponseBean<CardBeanBiz> fetchSavedCardByID(String cardID, String userID) {
        SavedCardResponse<SavedCardVO> savedCard = savedCardsService.getSavedCardByCardId(Long.parseLong(cardID),
                userID);
        LOGGER.debug("Response received from SavedCard Service is : {}", savedCard);
        if (!savedCard.getStatus()) {
            LOGGER.error("Error occured while fetching data for saved card id : {}", cardID);
            return new GenericCoreResponseBean<>(savedCard.getMessage());
        }
        CardBeanBiz cardBeanBiz = mappingUtility.mapSavedCard(savedCard.getResponseData());
        LOGGER.debug("Biz Saved Card Model is : {}", cardBeanBiz);
        return new GenericCoreResponseBean<>(cardBeanBiz);
    }

    public GenericCoreResponseBean<CardBeanBiz> fetchSavedCardByID(String cardId, String userId, String custId,
            String mId) {
        SavedCardResponse<SavedCardVO> savedCard = savedCardsService.getSavedCardByCardId(Long.parseLong(cardId),
                userId, custId, mId);
        LOGGER.debug("Response received from SavedCard Service is : {}", savedCard);
        if (!savedCard.getStatus()) {
            LOGGER.error("Error occured while fetching data for saved card id : {}", cardId);
            return new GenericCoreResponseBean<>(savedCard.getMessage());
        }
        CardBeanBiz cardBeanBiz = mappingUtility.mapSavedCard(savedCard.getResponseData());
        LOGGER.debug("Biz Saved Card Model is : {}", cardBeanBiz);
        return new GenericCoreResponseBean<>(cardBeanBiz);
    }

    /*
     * Check payment Status using looper service
     */
    public GenericCoreResponseBean<QueryPaymentStatus> fetchPaymentStatus(WorkFlowTransactionBean flowTransBean) {
        final QueryPayResultRequestBean queryPayResultRequestBean = workRequestCreator
                .createQueryPayResultRequestBean(flowTransBean);
        final GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = bizPaymentService
                .queryPayResultForPaymentStatus(queryPayResultRequestBean);

        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            LOGGER.error("queryPayResult failed due to : {}", queryPayResultResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }
        return queryPayResultResponse;
    }

    public GenericCoreResponseBean<QueryPaymentStatus> getPaymentStatusInOneCall(WorkFlowTransactionBean flowTransBean) {
        final QueryPayResultRequestBean queryPayResultRequestBean = workRequestCreator
                .createQueryPayResultRequestBean(flowTransBean);
        final GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = bizPaymentService
                .queryPayResultForPaymentStatusInOneCall(queryPayResultRequestBean);

        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            LOGGER.error("queryPayResult failed due to : {}", queryPayResultResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }
        return queryPayResultResponse;
    }

    /*
     * Check payment Status using looper service
     */
    public GenericCoreResponseBean<QueryPaymentStatus> fetchBankForm(WorkFlowTransactionBean flowTransBean) {
        final QueryPayResultRequestBean queryPayResultRequestBean = workRequestCreator
                .createQueryPayResultRequestBean(flowTransBean);
        String modifiedLooperTimeout = null;
        String supportedPaymodesToDisableBankFormRetry = com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                SUPPORTED_PAYMODE_TO_DISABLE_BANKFORM_RETRY, "");
        boolean isPaymodeAvailable = false;
        List<String> supportedPaymodes = new ArrayList<>();
        supportedPaymodes = Arrays.asList(supportedPaymodesToDisableBankFormRetry.split(Pattern.quote(",")));
        if (flowTransBean.getWorkFlowBean() != null && flowTransBean.getWorkFlowBean().getPayMethod() != null) {
            isPaymodeAvailable = supportedPaymodes.stream().anyMatch(
                    element -> flowTransBean.getWorkFlowBean().getPayMethod().equalsIgnoreCase(element));
        }
        if (isPaymodeAvailable
                && ff4JUtil.isFeatureEnabled(MODIFY_LOOPER_TIMEOUT_TO_FETCH_BANKFORM, flowTransBean.getWorkFlowBean()
                        .getPaytmMID())) {
            modifiedLooperTimeout = ConfigurationUtil.getTheiaProperty(
                    BizConstant.MODIFIED_LOOPER_TIMEOUT_TO_FECTH_BANKFORM, "11000");
        }
        modifiedLooperTimeout = StringUtils.isNotBlank(flowTransBean.getCustomLooperTimeout()) ? flowTransBean
                .getCustomLooperTimeout() : modifiedLooperTimeout;
        final GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = bizPaymentService
                .queryPayResultForBankForm(queryPayResultRequestBean, modifiedLooperTimeout, flowTransBean
                        .getWorkFlowBean().getBankFormOptimizationParams());
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }
        checkBankFormFetchFail(queryPayResultResponse);
        return queryPayResultResponse;
    }

    private void checkBankFormFetchFail(GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        if (!queryPayResultResponse.isSuccessfullyProcessed() || queryPayResultResponse.getResponse() == null
                || StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
            if (!(queryPayResultResponse.getResponse() != null && checkIfInstaCodeInNotAllowedCodes(queryPayResultResponse
                    .getResponse()))) {
                queryPayResultResponse.setRetryStatus(RetryStatus.BANK_FORM_FETCH_FAILED);
            }
            logBankFormFailureResponse(queryPayResultResponse);
        }
    }

    private void logBankFormFailureResponse(GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse) {
        Map keyMap = new HashMap();
        if (queryPayResultResponse != null && queryPayResultResponse.getRetryStatus() != null
                && queryPayResultResponse.getRetryStatus() == RetryStatus.BANK_FORM_FETCH_FAILED) {
            keyMap.put("status", "BANK_FORM_FETCH_FAIL");
            keyMap.put("InBlackListCodes", false);
        } else {
            keyMap.put("status", "BANK_FORM_FETCH_FAIL");
            keyMap.put("InBlackListCodes", true);
        }
        keyMap.put("InstaErrorCode", queryPayResultResponse.getResponse() != null ? queryPayResultResponse
                .getResponse().getActualInstErrorCode() : null);
        EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
    }

    private boolean checkIfInstaCodeInNotAllowedCodes(QueryPaymentStatus status) {
        String instErrorCode = null;
        if (status != null) {
            if (StringUtils.isNotEmpty(status.getActualInstErrorCode())) {
                instErrorCode = status.getActualInstErrorCode();
            } else {
                instErrorCode = status.getInstErrorCode();
            }
        }
        String notAllowedEntries = ConfigurationUtil.getTheiaProperty(BizConstant.INTERNAL_RETRY_BLACKLIST_CODES);
        if (StringUtils.isNotEmpty(notAllowedEntries) && StringUtils.isNotEmpty(instErrorCode)) {
            String[] codes = notAllowedEntries.split(",");
            if (codes != null && codes.length > 0)
                for (String code : codes) {
                    if (instErrorCode.equalsIgnoreCase(StringUtils.trim(code))) {
                        return true;
                    }
                }
        }
        return false;
    }

    public GenericCoreResponseBean<UPIPushInitiateResponse> initiateUpiPushTransaction(
            WorkFlowTransactionBean flowTransBean) {
        final UPIPushInitiateRequestBean initiateRequestBean = workRequestCreator
                .createUpiPushInitiateRequestBean(flowTransBean);
        final GenericCoreResponseBean<UPIPushInitiateResponse> initiateUpiPushTransactionResponse = bizPaymentService
                .initiateUpiPushTransaction(initiateRequestBean);
        if (!initiateUpiPushTransactionResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(initiateUpiPushTransactionResponse.getFailureMessage(),
                    initiateUpiPushTransactionResponse.getResponseConstant());
        }
        return initiateUpiPushTransactionResponse;
    }

    /*
     * Check transaction status using looper Service
     */
    public GenericCoreResponseBean<QueryTransactionStatus> fetchTransactionStatus(WorkFlowTransactionBean flowTransBean) {
        final QueryByAcquirementIdRequestBean queryByAcquirementIdRequestBean = workRequestCreator
                .createqueryByAcquirementIdRequestBean(flowTransBean);
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = orderService
                .createQueryByAcquirementId(queryByAcquirementIdRequestBean);

        if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
            LOGGER.error("QueryTransactionStatus failed due to : {}", queryByAcquirementIdResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                    queryByAcquirementIdResponse.getResponseConstant());
        }

        return queryByAcquirementIdResponse;
    }

    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay(
            WorkFlowTransactionBean flowTransBean) {
        return createOrderAndPay(flowTransBean, false);
    }

    /*
     * In case of Subscrption we will , pass terminal type as SYSTEM
     */
    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay(
            WorkFlowTransactionBean flowTransBean, boolean isRenewSubscriptionRequest) {
        if (Boolean.TRUE.equals(flowTransBean.getWorkFlowBean().getEdcLinkTxn())) {
            ICreateOrderAndPay copService = payAndCopFactory.getCopService(PayAndCopImplEnum.EDL_LINK_PAYMENT_COP
                    .getValue());
            return copService.processCopRequest(flowTransBean, isRenewSubscriptionRequest);
        } else {
            return getGenericCopResponse(flowTransBean, isRenewSubscriptionRequest);
        }
    }

    private boolean isInternalPaymentRetry(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.getCurrentInternalPaymentRetryCount() > 0;
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
                    EXT_LOGGER.customInfo("setting message in rollback packet");
                    msg.setPromocode(promoCheckoutData.getPromocode());
                }
            }
            paymentPromoServiceNotify.pushFailureMsg(msg);
        } else if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null && !rollBackPaymentOffer) {
            LOGGER.info("not doing rollback for payment failure");
        }
    }

    private void rollbackPaymentOfferCheckoutV2(WorkFlowRequestBean workFlowRequestBean, boolean rollBackPaymentOffer) {
        if (!workFlowRequestBean.isDealsFlow()
                && (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null || workFlowRequestBean
                        .getPaymentOffersAppliedV2() != null) && rollBackPaymentOffer) {
            LOGGER.info("Rolling back Payment offer checkout v2");
            PaymentPromoServiceNotifyMsgV2 paymentPromoServiceNotifyMsgV2 = preparePromoServiceNotifyMsgV2(workFlowRequestBean);
            paymentPromoServiceNotifyV2.pushFailureMsg(paymentPromoServiceNotifyMsgV2);
        } else {
            LOGGER.info("not doing rollback for payment failure v2");
        }
    }

    private void rollbackPaymentOfferForDeals(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                || workFlowRequestBean.getPaymentOffersAppliedV2() != null) {
            LOGGER.info("Rolling back payment offer for deals");
            PaymentPromoServiceNotifyMsgV2 paymentPromoServiceNotifyMsgV2 = preparePromoServiceNotifyMsgV2(workFlowRequestBean);
            paymentPromoServiceNotifyV2.pushFailureMsg(paymentPromoServiceNotifyMsgV2);
        }
    }

    private PaymentPromoServiceNotifyMsgV2 preparePromoServiceNotifyMsgV2(WorkFlowRequestBean workFlowRequestBean) {
        PaymentPromoServiceNotifyMsgV2 msg = new PaymentPromoServiceNotifyMsgV2(workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), custIdFromWorkflowReqBean(workFlowRequestBean));
        msg.setClient("PG");
        return msg;
    }

    private PromoCheckoutFeatureFF4jData checkoutPaymentOfferUpdateOrderAmountAndExtendInfo(
            WorkFlowTransactionBean workFlowTransactionBean, int retryCount, boolean modifyCreatedOrder) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        boolean ff4jSaveCheckoutData = ff4JUtil.isFeatureEnabled(SAVE_PROMO_CHECKOUT_DATA,
                workFlowRequestBean.getPaytmMID());
        PromoCheckoutFeatureFF4jData featureFF4jData = new PromoCheckoutFeatureFF4jData();
        featureFF4jData.setFeaturePromoSaveCheckoutDataFlagEnabled(ff4jSaveCheckoutData);
        CheckoutPromoServiceResponse savedCheckoutResponse = null;
        if (ff4jSaveCheckoutData) {
            retryCount = 0;
            com.paytm.pgplus.facade.enums.PayMethod pMethod = null;
            try {
                pMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(workFlowRequestBean
                        .getPayMethod());
            } catch (FacadeInvalidParameterException e) {
            }
            if ((workFlowRequestBean.getPaytmExpressAddOrHybrid() != null && workFlowRequestBean
                    .getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY)
                    || com.paytm.pgplus.facade.enums.PayMethod.BALANCE.equals(pMethod)) {
                savedCheckoutResponse = (CheckoutPromoServiceResponse) theiaSessionRedisUtil.hget(
                        workFlowRequestBean.getTxnToken(), PROMO_CHECKOUT_RESPONSE_DETAILS);
            } else {
                LOGGER.info("flow is not addnpay or paymethod is not valid");
            }
        }
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null || savedCheckoutResponse != null
                || workFlowRequestBean.getPaymentOffersAppliedV2() != null) {
            boolean txnFailureOnPromoCheckoutFailure = false;
            if ((workFlowRequestBean.getSimplifiedPaymentOffers() == null)
                    || (workFlowRequestBean.getSimplifiedPaymentOffers() != null && workFlowRequestBean
                            .getSimplifiedPaymentOffers().isValidatePromo())) {
                txnFailureOnPromoCheckoutFailure = true;
            }
            boolean ff4jCheckoutRetry = ff4JUtil.isFeatureEnabled(DISABLE_PROMO_CHECKOUT_RETRY,
                    workFlowRequestBean.getPaytmMID());
            featureFF4jData.setFeaturePromoCheckoutRetryFlagEnabled(ff4jCheckoutRetry);
            if (ff4jCheckoutRetry
                    && theiaSessionRedisUtil.hget(workFlowRequestBean.getTxnToken(), PROMO_CHECKOUT_RETRY_COUNT) != null) {
                LOGGER.error(FailureLogs.PROMO_CHECKOUT_LIMIT_BREACHED);
                failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.PROMO_CHECKOUT_LIMIT_BREACHED, null, true);
                throw new BizPaymentOfferCheckoutException("Retry count breached");
            }
            LOGGER.info("Request with payment offer");
            com.paytm.pgplus.facade.enums.PayMethod payMethod = null;
            Optional<CheckoutPromoServiceResponse> responseOptional = null;
            if (savedCheckoutResponse != null) {
                responseOptional = Optional.ofNullable(savedCheckoutResponse);
            } else {
                try {
                    payMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(workFlowRequestBean
                            .getPayMethod());
                } catch (FacadeInvalidParameterException e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                    failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
                    throw new BizPaymentOfferCheckoutException();
                }

                CheckoutPromoServiceRequest checkoutPromoServiceRequest = CheckoutPaymentPromoReqBuilderFactory
                        .getApplyPromoPaymentOptionBuilder(payMethod).build(workFlowRequestBean);
                Map<String, String> queryParam = new HashMap<>();
                queryParam.put("customer-id", custIdFromWorkflowReqBean(workFlowRequestBean));
                queryParam.put("merchant-id", workFlowRequestBean.getPaytmMID());
                queryParam.put("order-id", workFlowRequestBean.getOrderID());
                if (workFlowRequestBean.getUserDetailsBiz() != null
                        && workFlowRequestBean.getSimplifiedPaymentOffers() == null
                        && ff4JUtil.isFeatureEnabled(TheiaConstant.ExtraConstants.THEIA_ENABLE_PROMO_WALLET_CASHBACK,
                                workFlowRequestBean.getPaytmMID())) {
                    queryParam.put("paytm-user-id", workFlowRequestBean.getUserDetailsBiz().getUserId());
                }

                if (workFlowRequestBean.getPaymentRequestBean() != null
                        && StringUtils.isNotBlank(workFlowRequestBean.getPaymentRequestBean().getMerchantDisplayName())) {
                    checkoutPromoServiceRequest.setMerchantDisplayName(workFlowRequestBean.getPaymentRequestBean()
                            .getMerchantDisplayName());
                }

                responseOptional = checkoutPaymentOffer(checkoutPromoServiceRequest, queryParam, retryCount);
                if (!isPromoCheckoutSuccessResponse(responseOptional)) {
                    LOGGER.error(FailureLogs.PAYMENT_OFFER_CHECKOUT_FAILURE);
                    // Rollback is important here for cases like requestTimeout
                    // where promocheckout migth be got successful
                    rollbackPaymentOfferCheckout(workFlowRequestBean, true);
                    if (txnFailureOnPromoCheckoutFailure) {
                        failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.PAYMENT_OFFER_CHECKOUT_FAILURE, null,
                                true);
                        throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsg(responseOptional));
                    } else {
                        return featureFF4jData;
                    }

                } else {
                    workFlowRequestBean.setCheckoutPromoServiceResponse(responseOptional.get());
                }
                try {
                    if (ff4jSaveCheckoutData
                            && ((workFlowRequestBean.getPaytmExpressAddOrHybrid() != null && workFlowRequestBean
                                    .getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY) || com.paytm.pgplus.facade.enums.PayMethod.BALANCE
                                    .equals(payMethod))
                            && (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                                    && CollectionUtils.isNotEmpty(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                                            .getOfferBreakup()) && com.paytm.pgplus.enums.PayMethod.BALANCE
                                        .equals(workFlowRequestBean.getPaymentOfferCheckoutReqData().getOfferBreakup()
                                                .get(0).getPayMethod()))) {
                        theiaSessionRedisUtil.hsetIfExist(workFlowRequestBean.getTxnToken(),
                                PROMO_CHECKOUT_RESPONSE_DETAILS, responseOptional.get());
                    }
                } catch (Exception e) {
                    LOGGER.error("problem while saving checkout response data :{}", e.getMessage());
                }
            }
            try {
                workFlowRequestBean.getExtendInfo().setPaymentPromoCheckoutData(
                        JsonMapper.mapObjectToJson(responseOptional.get().getData()));
            } catch (FacadeCheckedException e) {
                LOGGER.error("ApplyPromoResponseData map to json Exception : " + ExceptionUtils.getStackTrace(e));
                // Rollback is important here for cases where we
                // are not able to set PaymentPromoCheckoutData in extendInfo
                rollbackPaymentOfferCheckout(workFlowRequestBean, true);
                if (txnFailureOnPromoCheckoutFailure) {
                    failureLogUtil
                            .setFailureMsgForDwhPush(null, getPromoCheckoutErrorMsg(responseOptional), null, true);
                    throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsg(responseOptional));
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
                promoSavings = responseOptional.get().getData().getSavings();
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

    public void modifyOrder(WorkFlowTransactionBean workFlowTransactionBean) {
        if (workFlowTransactionBean.isModifyOrderRequired()) {
            WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
            OrderModifyRequest orderModifyRequest = workRequestCreator
                    .getOrderModifyRequestForUpdatedAmount(workFlowRequestBean);
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

    private PromoCheckoutFeatureFF4jData checkoutPaymentOfferUpdateOrderAmountAndExtendInfoV2(
            WorkFlowTransactionBean workFlowTransactionBean, int retryCount, boolean modifyCreatedOrder) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        PromoCheckoutFeatureFF4jData featureFF4jData = new PromoCheckoutFeatureFF4jData();
        if (workFlowRequestBean.isPaymentResumed()) {
            retryCount = 1;
        }
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                || workFlowRequestBean.getPaymentOffersAppliedV2() != null || workFlowRequestBean.isDealsFlow()) {
            boolean txnFailureOnPromoCheckoutFailure = false;
            if ((workFlowRequestBean.getSimplifiedPaymentOffers() == null)
                    || (workFlowRequestBean.getSimplifiedPaymentOffers() != null && workFlowRequestBean
                            .getSimplifiedPaymentOffers().isValidatePromo())) {
                txnFailureOnPromoCheckoutFailure = true;
            }
            boolean ff4jCheckoutRetry = ff4JUtil.isFeatureEnabled(DISABLE_PROMO_CHECKOUT_RETRY,
                    workFlowRequestBean.getPaytmMID());
            featureFF4jData.setFeaturePromoCheckoutRetryFlagEnabled(ff4jCheckoutRetry);
            if (ff4jCheckoutRetry
                    && theiaSessionRedisUtil.hget(workFlowRequestBean.getTxnToken(), PROMO_CHECKOUT_RETRY_COUNT) != null) {
                LOGGER.error(FailureLogs.PROMO_CHECKOUT_LIMIT_BREACHED);
                failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.PROMO_CHECKOUT_LIMIT_BREACHED, null, true);
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
                failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
                throw new BizPaymentOfferCheckoutException();
            }

            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = null;
            Map<String, String> queryParam = null;
            if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                    || workFlowRequestBean.getPaymentOffersAppliedV2() != null) {
                boolean isEnableGcinOnCoftPromo = ff4JUtils.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(),
                        ENABLE_GCIN_ON_COFT_PROMO, false);
                if (isEnableGcinOnCoftPromo) {
                    checkoutPromoServiceRequest = CheckoutPaymentPromoReqBuilderFactory
                            .getApplyPromoPaymentOptionBuilder(payMethod).buildCoftV2(workFlowRequestBean);
                } else {
                    checkoutPromoServiceRequest = CheckoutPaymentPromoReqBuilderFactory
                            .getApplyPromoPaymentOptionBuilder(payMethod).buildV2(workFlowRequestBean);
                }

                queryParam = getPromoCheckoutQueryParams(workFlowRequestBean);
                if (workFlowRequestBean.getPaymentRequestBean() != null
                        && StringUtils.isNotBlank(workFlowRequestBean.getPaymentRequestBean().getMerchantDisplayName())) {
                    checkoutPromoServiceRequest.setMerchantDisplayName(workFlowRequestBean.getPaymentRequestBean()
                            .getMerchantDisplayName());
                }

            }
            if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                    && workFlowRequestBean.getPaymentOfferCheckoutReqData().getCartDetails() != null) {
                checkoutPromoServiceRequest.setCart(setCartDetails(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                        .getCartDetails()));
            }
            if (workFlowRequestBean.isDealsFlow())
                response = checkoutPaymentOfferAtAffordability(workFlowRequestBean, checkoutPromoServiceRequest,
                        payMethod);
            else
                response = checkoutPaymentOfferV2(checkoutPromoServiceRequest, queryParam, retryCount,
                        workFlowRequestBean);
            if (!(workFlowRequestBean.isDealsFlow() && checkoutPromoServiceRequest == null)) {
                if (!isPromoCheckoutSuccessResponseV2(response)) {
                    LOGGER.error("Payment Offer checkout failure");
                    Map keyMap = new HashMap(1);
                    keyMap.put("simplifiedOfferFlow", (workFlowRequestBean.getSimplifiedPaymentOffers() != null) ? true
                            : false);
                    EventUtils.pushTheiaEvents(EventNameEnum.CHECKOUT_PROMO_FAILED, keyMap);
                    if (txnFailureOnPromoCheckoutFailure) {
                        failureLogUtil.setFailureMsgForDwhPush(null, getPromoCheckoutErrorMsgV2(response), null, true);
                        throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsgV2(response));
                    } else {
                        return featureFF4jData;
                    }
                }
                workFlowRequestBean.setPromoCheckoutInfo(response);
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
                    // are not able to set PaymentPromoCheckoutData in
                    // extendInfo
                    if (!workFlowRequestBean.isDealsFlow())
                        rollbackPaymentOfferCheckout(workFlowRequestBean, true);
                    if (txnFailureOnPromoCheckoutFailure) {
                        failureLogUtil.setFailureMsgForDwhPush(null, getPromoCheckoutErrorMsgV2(response), null, true);
                        throw new BizPaymentOfferCheckoutException(getPromoCheckoutErrorMsgV2(response));
                    } else {
                        return featureFF4jData;
                    }
                }

                boolean isPromoDataInMerchantStatusServiceAllowed = ff4JUtils.isFeatureEnabledOnMid(
                        workFlowRequestBean.getPaytmMID(), ALLOW_PROMO_DATA_IN_MERCHANT_STATUS_SERVICE, false);

                if (workFlowRequestBean.getSimplifiedPaymentOffers() != null
                        || isPromoDataInMerchantStatusServiceAllowed) {
                    workFlowRequestBean.setPayableAmount(workFlowRequestBean.getTxnAmount());
                }

                List<PromoSaving> promoSavings = null;
                if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null) {
                    promoSavings = getPromoSavings(workFlowRequestBean.getPaymentOfferCheckoutReqData()
                            .getOfferBreakup().get(0));
                } else {
                    promoSavings = responseV1.getData().getSavings();
                }
                workFlowRequestBean.setTxnAmount(calculateTxnAmountAfterOffer(promoSavings,
                        workFlowRequestBean.getTxnAmount(), workFlowRequestBean.getPromoAmount()));
            }
            if (modifyCreatedOrder
                    && ((workFlowRequestBean.getSimplifiedPaymentOffers() != null && !workFlowRequestBean
                            .getTxnAmount().equals(workFlowRequestBean.getPayableAmount())) || workFlowRequestBean
                            .isDealsFlow())
                    && (theiaSessionRedisUtil.hget(workFlowRequestBean.getTxnToken(), "orderModified") == null)) {
                workFlowTransactionBean.setModifyOrderRequired(true);
                workFlowTransactionBean.setFailTxnIfModifyOrderFails(txnFailureOnPromoCheckoutFailure);
            }
            featureFF4jData.setSuccess(true);
            LOGGER.info("successfully checkoutPaymentOfferUpdateOrderAmountAndExtendInfo");
            return featureFF4jData;
        }
        return featureFF4jData;
    }

    private Cart setCartDetails(PromoCartDetails cartDetails) {
        Cart cart = new Cart();
        if (CollectionUtils.isNotEmpty(cartDetails.getItems())) {
            HashMap<String, Item> cartMap = new HashMap<>();
            for (PromoItemDetail item : cartDetails.getItems()) {
                cartMap.put(item.getId(), getItem(item));
            }
            cart.setItems(cartMap);
        }
        return cart;
    }

    private Item getItem(PromoItemDetail promoItem) {
        Item item = new Item();
        if (promoItem.getProductDetail() != null) {
            item.setProduct(getProduct(promoItem.getProductDetail()));
        }
        item.setPrice(promoItem.getAmount());
        return item;
    }

    private Product getProduct(PromoProductDetail productDetail) {
        Product product = new Product();
        product.setBrand_id(productDetail.getBrandId());
        product.setId(productDetail.getId());
        product.setMerchant_id(productDetail.getMerchantId());
        product.setCategory_ids(productDetail.getCategoryIds());
        product.setVertical_id(productDetail.getVerticalId());
        return product;
    }

    private CheckoutPromoServiceResponseV2 checkoutPaymentOfferAtAffordability(WorkFlowRequestBean workFlowRequestBean,
            CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest, com.paytm.pgplus.facade.enums.PayMethod payMethod) {
        CheckoutPromoServiceResponseV2 response = null;
        APOrderCheckoutRequest orderCheckoutRequest = new APOrderCheckoutRequest();
        if (workFlowRequestBean.isDealsFlow()) {
            orderCheckoutRequest
                    .setProductCode(com.paytm.pgplus.facade.enums.ProductCodes.StandardDirectPayDealAcquiringProd
                            .getId());
        } else {
            orderCheckoutRequest
                    .setProductCode(StringUtils.isNotEmpty(workFlowRequestBean.getProductCode()) ? workFlowRequestBean
                            .getProductCode() : workFlowRequestBean.getExtendInfo().getProductCode());
        }

        if (Objects.nonNull(workFlowRequestBean.getUserDetailsBiz())) {
            UserDetailsBiz userDetailsBiz = workFlowRequestBean.getUserDetailsBiz();
            UserInfo userInfo = new UserInfo();
            userInfo.setPaytmUserId(userDetailsBiz.getUserId());
            userInfo.setPplusUserId(userDetailsBiz.getInternalUserId());
            // userInfo.setUserType(userDetailsBiz.getUserTypes());
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

        if (Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest())
                && Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody())) {
            orderCheckoutRequest.setOrderOfferInfo(workFlowRequestBean.getInitiateTransactionRequest().getBody()
                    .getAffordabilityDetails());

            orderCheckoutRequest.setAffordabilityInfo(workFlowRequestBean.getInitiateTransactionRequest().getBody()
                    .getAffordabilityInfo());

            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderId(workFlowRequestBean.getOrderID());
            orderInfo.setOrderAmount(new Money(workFlowRequestBean.getTxnAmount()));
            if (workFlowRequestBean.isDealsFlow() && StringUtils.isNotBlank(workFlowRequestBean.getPromoAmount())) {
                orderInfo.setCheckoutOrderAmount(new Money(workFlowRequestBean.getPromoAmount()));
            }
            orderInfo.setPgAcquirementId(workFlowRequestBean.getTransID());
            orderCheckoutRequest.setOrderInfo(orderInfo);
            if (workFlowRequestBean.isICBFlow()) {
                if (Objects.nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody().getPayableAmount())) {
                    setOrderCheckoutAmountInOrderInfo(orderInfo, workFlowRequestBean.getInitiateTransactionRequest()
                            .getBody().getPayableAmount());
                } else if (Objects
                        .nonNull(workFlowRequestBean.getInitiateTransactionRequest().getBody().getTxnAmount())) {
                    setOrderCheckoutAmountInOrderInfo(orderInfo, workFlowRequestBean.getInitiateTransactionRequest()
                            .getBody().getTxnAmount());
                } else {
                    LOGGER.info("Can't find TxnAmount in InitiateTransactionRequest");// need
                                                                                      // to
                                                                                      // remove
                }
                if (Objects.nonNull(workFlowRequestBean.getOrderType())) {
                    if (StringUtils.equals(OrderType.BRAND_EMI_ORDER.getValue(), workFlowRequestBean.getOrderType())) {
                        populateProductInfo(workFlowRequestBean, orderInfo);
                    }
                    orderInfo.setOrderType(workFlowRequestBean.getOrderType());
                }
            }
        }

        if (workFlowRequestBean.isDealsFlow()) {
            Map<String, Object> paymentOfferInfo = new HashMap<>();
            paymentOfferInfo.put("promoCheckoutInfo", checkoutPromoServiceRequest);
            orderCheckoutRequest.setPaymentOfferInfo(paymentOfferInfo);
        }

        PaymentDetails paymentDetails = new PaymentDetails();
        String bankCode = StringUtils.isNotEmpty(workFlowRequestBean.getBankCode()) ? workFlowRequestBean.getBankCode()
                : workFlowRequestBean.getInstId();
        paymentDetails.setIssuingBankCode(bankCode);
        paymentDetails.setIssuingBankName(workFlowRequestBean.getBankName());

        PayOption payOption = new PayOption();
        payOption.setPayMode(payMethod.getMethod());
        payOption.setPayAmount(new Money(workFlowRequestBean.getTxnAmount()));
        payOption.setPaymentDetails(paymentDetails);
        APPaymentInfo paymentInfo = new APPaymentInfo();
        List<PayOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(payOption);
        paymentInfo.setPayOptions(paymentOptions);
        orderCheckoutRequest.setPaymentInfo(paymentInfo);
        if (workFlowRequestBean.isICBFlow()) {
            orderCheckoutRequest.setPromoCheckoutInfo(workFlowRequestBean.getPromoCheckoutInfo());
            orderCheckoutRequest.setSubventionCheckoutInfo(workFlowRequestBean.getSubventionCheckoutInfo());
        }
        LOGGER.info("WorkFlowRequestBean {}", workFlowRequestBean);
        APOrderCheckoutResponse checkoutResponse = null;
        try {
            checkoutResponse = affordabilityService.orderCheckOut(orderCheckoutRequest);
        } catch (FacadeCheckedException e) {
            LOGGER.error("FacadeCheckedException occurred while connecting to affordability ", e);
            EXT_LOGGER.customError("Exception in order checkout at affordability platform", e);
            if (checkoutPromoServiceRequest != null) {
                rollbackPaymentOfferForDeals(workFlowRequestBean);
            }
        } catch (Throwable e) {
            LOGGER.error("Exception occurred while connecting to affordability ", e);
        }
        if (Objects.nonNull(checkoutResponse) && Objects.nonNull(checkoutResponse.getResultInfo())
                && "S".equals(checkoutResponse.getResultInfo().getStatus())
                && Objects.nonNull(checkoutResponse.getCheckoutInfo())
                && Objects.nonNull(checkoutResponse.getCheckoutInfo().getCheckoutStatus())
                && "S".equals(checkoutResponse.getCheckoutInfo().getCheckoutStatus().getStatus())) {
            response = checkoutResponse.getCheckoutInfo().getPaymentOfferCheckoutResponse();

            if (CollectionUtils.isNotEmpty(checkoutResponse.getCheckoutInfo().getPricingAmountInfoList())) {
                OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
                orderPricingInfo
                        .setPricingAmountInfoList(checkoutResponse.getCheckoutInfo().getPricingAmountInfoList());
                workFlowRequestBean.setOrderPricingInfo(orderPricingInfo);
            }

            if (workFlowRequestBean.getAdditionalOrderExtendInfo() == null)
                workFlowRequestBean.setAdditionalOrderExtendInfo(new HashMap<>());
            Money billAmount = checkoutResponse.getOrderInfo().getBillAmount();
            if (Objects.nonNull(billAmount))
                workFlowRequestBean.getExtendInfo().setBillAmount(billAmount.getAmount());
            workFlowRequestBean.setDetailExtendInfo(checkoutResponse.getCheckoutInfo().getCheckoutExtendInfo());
        } else {
            LOGGER.error("Affordability checkout failed");
            String errorMessage = ConfigurationUtil.getProperty("affordability.offer.checkout.failure.msg",
                    "There seems to be a problem in processing. Please try again");
            throw new BizPaymentOfferCheckoutException(errorMessage);
        }
        return response;
    }

    public void setOrderCheckoutAmountInOrderInfo(OrderInfo orderInfo, com.paytm.pgplus.models.Money txnAmount) {
        Money amount = new Money();
        amount.setValue(AmountUtils.getTransactionAmountInPaise(txnAmount.getValue()));
        try {
            amount.setCurrency(com.paytm.pgplus.facade.enums.EnumCurrency.getEnumByCurrency(txnAmount.getCurrency()
                    .getCurrency()));
        } catch (FacadeInvalidParameterException e) {
            EXT_LOGGER.customWarn("Exception in serializing currency", e);
            amount.setCurrency(com.paytm.pgplus.facade.enums.EnumCurrency.INR);
        }
        orderInfo.setCheckoutOrderAmount(amount);

    }

    private void populateProductInfo(WorkFlowRequestBean workFlowRequestBean, OrderInfo orderInfo) {
        List<ProductInfo> productInfoList = new ArrayList<>();
        if (workFlowRequestBean.getPaymentOfferCheckoutReqData() != null
                && workFlowRequestBean.getPaymentOfferCheckoutReqData().getCartDetails() != null) {
            PromoCartDetails cartDetails = workFlowRequestBean.getPaymentOfferCheckoutReqData().getCartDetails();
            List<PromoItemDetail> items = cartDetails.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                for (PromoItemDetail item : items) {
                    ProductInfo productInfo = new ProductInfo();
                    productInfo.setPrice(String.valueOf(item.getAmount()));
                    PromoProductDetail productDetail = item.getProductDetail();
                    productInfo.setProductId(productDetail.getId());
                    productInfo.setBrandId(productDetail.getBrandId());
                    productInfo.setModel(productDetail.getModel());
                    productInfoList.add(productInfo);
                }
            }
        } else if (MapUtils.isNotEmpty(workFlowRequestBean.getPromoContextICB())) {
            String cart = workFlowRequestBean.getPromoContextICB().get(CART);
            try {
                if (cart != null) {
                    Cart data = JsonMapper.mapJsonToObject(cart, Cart.class);
                    for (Map.Entry<String, Item> entry : data.getItems().entrySet()) {
                        ProductInfo productInfo = new ProductInfo();
                        if (entry.getValue() != null) {
                            productInfo.setPrice(String.valueOf(entry.getValue().getPrice()));
                            Product product = entry.getValue().getProduct();
                            if (product != null) {
                                productInfo.setProductId(product.getId());
                                productInfo.setBrandId(product.getBrand_id());
                            }
                            productInfoList.add(productInfo);
                        }
                    }
                }
            } catch (FacadeCheckedException ex) {
                LOGGER.info("Error in parsing cart data :{} ", workFlowRequestBean.getPromoContextICB(), ex);
            }
        } else if (CollectionUtils.isNotEmpty(workFlowRequestBean.getItemsICB())) {
            List<ItemWithOrder> items = workFlowRequestBean.getItemsICB();
            for (ItemWithOrder item : items) {
                ProductInfo productInfo = new ProductInfo();
                productInfo.setPrice(String.valueOf(item.getPrice()));
                productInfo.setProductId(item.getProductId());
                productInfo.setBrandId(item.getBrandId());
                productInfo.setModel(item.getModel());
                productInfoList.add(productInfo);
            }
        }
        orderInfo.setProductInfo(productInfoList);
    }

    public void applyEmiSubventionOffer(WorkFlowRequestBean workFlowRequestBean) {

        if (workFlowRequestBean.getEmiSubventionOfferCheckoutReqData() != null
                && workFlowRequestBean.getEmiSubventionValidateRequestData() != null) {

            if (StringUtils.isNotBlank(workFlowRequestBean.getCardNo())) {
                String cardBin = StringUtils.substring(workFlowRequestBean.getCardNo(), 0, 6);
                if ((workFlowRequestBean.isCoftTokenTxn()
                        && StringUtils.isNotBlank(workFlowRequestBean.getAccountRangeCardBin()) && ff4JUtils
                            .isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), UPDATE_CARD_NO_EMI_SUBVENTION,
                                    false))) {
                    cardBin = StringUtils.substring(workFlowRequestBean.getAccountRangeCardBin(), 0, 6);
                }
                PaymentDetail paymentDetail = workFlowRequestBean.getEmiSubventionValidateRequestData()
                        .getPaymentDetails();
                if (paymentDetail != null && !paymentDetail.getPaymentOptions().isEmpty()) {
                    int validateSubventionBin = paymentDetail.getPaymentOptions().get(0).getBin6();
                    if (validateSubventionBin != 0
                            && !StringUtils.equals(String.valueOf(validateSubventionBin), cardBin)) {
                        LOGGER.error(FailureLogs.PAYMENT_CARD_BIN_DOES_NOT_MATCH);
                        failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.PAYMENT_CARD_BIN_DOES_NOT_MATCH, null,
                                true);
                        throw new BizSubventionOfferCheckoutException("Invalid card details");
                    }
                }
            }

            CheckoutRequest emiSubventionCheckoutRequest = null;
            GenericEmiSubventionResponse<CheckOutResponse> emiSubventionCheckoutGenericResponse = null;
            CheckOutResponse emiSubventionCheckoutResponse = null;

            if (!subventionAlreadyOrderStamped(workFlowRequestBean)) {

                if (!subventionAlreadyCheckout(workFlowRequestBean)) {
                    emiSubventionCheckoutRequest = emiSubventionCheckoutReqBuilderFactory
                            .getSubventionCheckoutReq(workFlowRequestBean);
                    Map<String, String> queryParam = new HashMap<>();
                    Map<String, String> headersParam = new HashMap<>();
                    queryParam.put(SUBVENTION_CUSTOMER_ID, getSubventioncustId(workFlowRequestBean));
                    queryParam.put(PG_MERCHANT_ID, workFlowRequestBean.getPaytmMID());
                    headersParam.put(SUBVENTION_USER_ID, getSubventioncustId(workFlowRequestBean));
                    if (workFlowRequestBean.getEmiSubventionValidateRequestData().isItemBasedRequest()) {
                        headersParam.put(EmiSubventionConstants.CLIENT, EmiSubventionConstants.ITEM_LEVEL_CLIENT_VALUE);
                    }

                    emiSubventionCheckoutGenericResponse = checkoutEmiSubvention(emiSubventionCheckoutRequest,
                            queryParam, headersParam);

                    if (emiSubventionCheckoutGenericResponse == null
                            || !isEmiSubventionResponseSuccessful(emiSubventionCheckoutGenericResponse)) {
                        LOGGER.error(FailureLogs.SUBVENTION_OFFER_CHECKOUT_FAILURE);
                        failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.SUBVENTION_OFFER_CHECKOUT_FAILURE,
                                null, true);
                        throw new BizSubventionOfferCheckoutException(
                                getSubventionResponseErrorMsg(emiSubventionCheckoutGenericResponse));
                    }
                    emiSubventionCheckoutResponse = emiSubventionCheckoutGenericResponse.getData();

                }

                GenericEmiSubventionResponse<OrderStampResponse> emiSubventionOrderStampGenericResponse = emiSubventionOrderStamping(
                        workFlowRequestBean, emiSubventionCheckoutRequest, emiSubventionCheckoutResponse);

                if (!isEmiSubventionResponseSuccessful(emiSubventionOrderStampGenericResponse)) {
                    LOGGER.error(FailureLogs.SUBVENTION_OFFER_ORDER_STAMPING_FAILURE);
                    failureLogUtil.setFailureMsgForDwhPush(null, FailureLogs.SUBVENTION_OFFER_ORDER_STAMPING_FAILURE,
                            null, true);
                    throw new BizSubventionOfferCheckoutException(
                            getSubventionResponseErrorMsg(emiSubventionOrderStampGenericResponse));
                }

                settingKeyForOrderStampInRedis(workFlowRequestBean);

                LOGGER.info("Successfull Checkout and OrderStamp for EMI Subvention");

            }
        }

    }

    // Checkout with order in single api call to EMI
    public void applyEmiSubventionOfferV2(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.getEmiSubventionOfferCheckoutReqData() != null
                && workFlowRequestBean.getEmiSubventionValidateRequestData() != null) {

            if (StringUtils.isNotBlank(workFlowRequestBean.getCardNo())) {
                String cardBin = StringUtils.substring(workFlowRequestBean.getCardNo(), 0, 6);
                if ((workFlowRequestBean.isCoftTokenTxn()
                        && StringUtils.isNotBlank(workFlowRequestBean.getAccountRangeCardBin()) && ff4JUtils
                            .isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), UPDATE_CARD_NO_EMI_SUBVENTION,
                                    false))) {
                    cardBin = StringUtils.substring(workFlowRequestBean.getAccountRangeCardBin(), 0, 6);
                }
                PaymentDetail paymentDetail = workFlowRequestBean.getEmiSubventionValidateRequestData()
                        .getPaymentDetails();
                if (paymentDetail != null && !paymentDetail.getPaymentOptions().isEmpty()) {
                    int validateSubventionBin = paymentDetail.getPaymentOptions().get(0).getBin6();
                    if (validateSubventionBin != 0
                            && !StringUtils.equals(String.valueOf(validateSubventionBin), cardBin)) {
                        LOGGER.error("Payment card bin does not match with bin used in validate subvention request");
                        throw new BizSubventionOfferCheckoutException("Invalid card details");
                    }
                }
            }

            CheckoutWithOrderRequest emiSubventionCheckoutRequest = null;
            GenericEmiSubventionResponse<CheckOutResponse> emiSubventionCheckoutGenericResponse = null;
            if (!subventionAlreadyOrderStamped(workFlowRequestBean)) {

                if (!subventionAlreadyCheckoutWithOrder(workFlowRequestBean)) {
                    emiSubventionCheckoutRequest = emiSubventionCheckoutReqBuilderFactory
                            .getSubventionCheckoutWithOrderReq(workFlowRequestBean);
                    Map<String, String> queryParam = new HashMap<>();
                    Map<String, String> headersParam = new HashMap<>();
                    queryParam.put(SUBVENTION_CUSTOMER_ID, getSubventioncustId(workFlowRequestBean));
                    queryParam.put(PG_MERCHANT_ID, workFlowRequestBean.getPaytmMID());
                    queryParam.put(SUBVENTION_ORDER_ID, workFlowRequestBean.getOrderID());
                    headersParam.put(SUBVENTION_USER_ID, getSubventioncustId(workFlowRequestBean));
                    if (workFlowRequestBean.getEmiSubventionValidateRequestData().isItemBasedRequest()) {
                        headersParam.put(EmiSubventionConstants.CLIENT, EmiSubventionConstants.ITEM_LEVEL_CLIENT_VALUE);
                    }
                    emiSubventionCheckoutGenericResponse = checkoutWithOrderEmiSubvention(emiSubventionCheckoutRequest,
                            queryParam, headersParam);

                    if (emiSubventionCheckoutGenericResponse == null
                            || !isEmiSubventionResponseSuccessful(emiSubventionCheckoutGenericResponse)) {
                        LOGGER.error("Subvention Offer checkout failure");
                        throw new BizSubventionOfferCheckoutException(
                                getSubventionResponseErrorMsg(emiSubventionCheckoutGenericResponse));
                    }
                    if (isItemBasedSubvention(emiSubventionCheckoutRequest.getItems())) {
                        workFlowRequestBean.setItemsICB(emiSubventionCheckoutRequest.getItems());
                    }
                    workFlowRequestBean.setSubventionCheckoutInfo(emiSubventionCheckoutGenericResponse);
                    OrderStampRequest subventionOrderStampRequest = null;
                    if (emiSubventionCheckoutRequest != null) {
                        subventionOrderStampRequest = emiSubventionCheckoutReqBuilderFactory
                                .getSubventionCheckoutOrderStampRequest(workFlowRequestBean,
                                        emiSubventionCheckoutRequest, emiSubventionCheckoutGenericResponse.getData());
                        setCheckoutWithOrderFlagInRedis(workFlowRequestBean.getPaytmMID(),
                                workFlowRequestBean.getOrderID(), subventionOrderStampRequest);
                    }
                }
                settingKeyForOrderStampInRedis(workFlowRequestBean);
                LOGGER.info("Successful Checkout with Order for EMI Subvention");
            }
        }
    }

    private void settingKeyForOrderStampInRedis(WorkFlowRequestBean workFlowRequestBean) {
        String key = SUBVENTION_ORDERSTAMP_KEY + "_" + workFlowRequestBean.getPaytmMID() + "_"
                + workFlowRequestBean.getOrderID();
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.SUBVENTION_KEY_EXPIRY, "900"));
        theiaTransactionalRedisUtil.set(key, true, expiry);

    }

    private boolean isItemBasedSubvention(List<ItemWithOrder> items) {
        for (ItemWithOrder item : items) {
            if (StringUtils.isNotEmpty(item.getBrandId())) {
                return true;
            }
        }
        return false;
    }

    private String getValidateCardIndexNo(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.getEmiSubventionValidateRequestData().getPaymentDetails().getPaymentOptions().get(0)
                .getCardIndexNo();
    }

    private boolean subventionAlreadyCheckout(WorkFlowRequestBean workFlowRequestBean) {

        String key = emiSubventionCheckoutRedisKey(workFlowRequestBean.getPaytmMID(), workFlowRequestBean.getOrderID());
        if (theiaTransactionalRedisUtil.get(key) != null) {
            return true;
        }

        return false;
    }

    private boolean subventionAlreadyCheckoutWithOrder(WorkFlowRequestBean workFlowRequestBean) {

        String key = emiSubventionCheckoutWithOrderRedisKey(workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID());
        if (theiaTransactionalRedisUtil.get(key) != null) {
            return true;
        }

        return false;
    }

    public boolean subventionAlreadyOrderStamped(WorkFlowRequestBean workFlowRequestBean) {

        String key = SUBVENTION_ORDERSTAMP_KEY + "_" + workFlowRequestBean.getPaytmMID() + "_"
                + workFlowRequestBean.getOrderID();

        if (theiaTransactionalRedisUtil.get(key) != null) {
            return true;
        }

        return false;
    }

    private void setCheckoutFlagInRedis(String mid, String orderId, OrderStampRequest subventionOrderStampRequest) {

        long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.SUBVENTION_KEY_EXPIRY, "900"));
        String key = emiSubventionCheckoutRedisKey(mid, orderId);
        theiaTransactionalRedisUtil.set(key, subventionOrderStampRequest, expiry);
    }

    private void setCheckoutWithOrderFlagInRedis(String mid, String orderId, OrderStampRequest checkoutWithOrderRequest) {
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.SUBVENTION_KEY_EXPIRY, "900"));
        String key = emiSubventionCheckoutWithOrderRedisKey(mid, orderId);
        theiaTransactionalRedisUtil.set(key, checkoutWithOrderRequest, expiry);
    }

    private GenericEmiSubventionResponse<CheckOutResponse> checkoutEmiSubvention(
            CheckoutRequest emiSubventionCheckoutRequest, Map<String, String> queryParam,
            Map<String, String> headersParam) {
        int retryCount = 0;
        int subventionCheckoutRetryCount = (Integer.parseInt(ConfigurationUtil.getProperty(
                SUBVENTION_CHECKOUT_RETRY_COUNT, "2")));
        while (retryCount < subventionCheckoutRetryCount) {
            try {
                return emiSubventionService.checkout(emiSubventionCheckoutRequest, headersParam, queryParam);
            } catch (FacadeCheckedException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            retryCount++;
        }
        return null;
    }

    private GenericEmiSubventionResponse<CheckOutResponse> checkoutWithOrderEmiSubvention(
            CheckoutWithOrderRequest emiSubventionCheckoutRequest, Map<String, String> queryParam,
            Map<String, String> headersParam) {
        int retryCount = 0;
        int subventionCheckoutRetryCount = (Integer.parseInt(ConfigurationUtil.getProperty(
                SUBVENTION_CHECKOUT_RETRY_COUNT, "2")));
        while (retryCount < subventionCheckoutRetryCount) {
            try {
                return emiSubventionService.checkoutWithOrder(emiSubventionCheckoutRequest, headersParam, queryParam);
            } catch (FacadeCheckedException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            retryCount++;
        }
        return null;
    }

    private GenericEmiSubventionResponse<OrderStampResponse> emiSubventionOrderStamping(
            WorkFlowRequestBean workFlowRequestBean, CheckoutRequest emiSubventionCheckoutRequest,
            CheckOutResponse checkOutResponse) {

        OrderStampRequest subventionOrderStampRequest = null;
        if (emiSubventionCheckoutRequest != null) {
            subventionOrderStampRequest = emiSubventionCheckoutReqBuilderFactory.getSubventionOrderStampRequest(
                    workFlowRequestBean, emiSubventionCheckoutRequest, checkOutResponse);
            setCheckoutFlagInRedis(workFlowRequestBean.getPaytmMID(), workFlowRequestBean.getOrderID(),
                    subventionOrderStampRequest);
        } else {
            String key = emiSubventionCheckoutRedisKey(workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getOrderID());
            subventionOrderStampRequest = (OrderStampRequest) theiaTransactionalRedisUtil.get(key);
        }

        Map<String, String> queryParam = new HashMap<>();
        Map<String, String> headersParam = new HashMap<>();
        queryParam.put(SUBVENTION_CUSTOMER_ID, getSubventioncustId(workFlowRequestBean));
        queryParam.put(PG_MERCHANT_ID, workFlowRequestBean.getPaytmMID());
        queryParam.put(SUBVENTION_ORDER_ID, workFlowRequestBean.getOrderID());
        headersParam.put(SUBVENTION_USER_ID, getSubventioncustId(workFlowRequestBean));
        if (workFlowRequestBean.getEmiSubventionValidateRequestData().isItemBasedRequest()) {
            headersParam.put(EmiSubventionConstants.CLIENT, EmiSubventionConstants.ITEM_LEVEL_CLIENT_VALUE);
        }
        GenericEmiSubventionResponse<OrderStampResponse> emiSubventionOrderStampGenericResponse = stampOrderForSubvention(
                subventionOrderStampRequest, queryParam, headersParam);

        return emiSubventionOrderStampGenericResponse;

    }

    public String emiSubventionCheckoutRedisKey(String mid, String orderId) {
        return SUBVENTION_CHECKOUT_RESPONSE + "_" + mid + "_" + orderId;
    }

    public String emiSubventionCheckoutWithOrderRedisKey(String mid, String orderId) {
        return SUBVENTION_CHECKOUT_WITH_ORDER_RESPONSE + "_" + mid + "_" + orderId;
    }

    private GenericEmiSubventionResponse<OrderStampResponse> stampOrderForSubvention(
            OrderStampRequest subventionOrderStampRequest, Map<String, String> queryParam,
            Map<String, String> headersParam) {
        int retryCount = 0;
        int subventionOrderStampRetryCount = (Integer.parseInt(ConfigurationUtil.getProperty(
                SUBVENTION_ORDERSTAMP_RETRY_COUNT, "2")));

        while (retryCount < subventionOrderStampRetryCount) {
            try {
                return emiSubventionService.stampOrder(subventionOrderStampRequest, headersParam, queryParam);

            } catch (FacadeCheckedException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            retryCount++;
        }

        return null;
    }

    private String getSubventionResponseErrorMsg(GenericEmiSubventionResponse<?> emiSubventionCheckoutGenericResponse) {

        if (emiSubventionCheckoutGenericResponse != null && emiSubventionCheckoutGenericResponse.getError() != null
                && emiSubventionCheckoutGenericResponse.getError().getMessage() != null) {
            return emiSubventionCheckoutGenericResponse.getError().getMessage();
        }
        return ConfigurationUtil.getProperty(SUBVENTION_OFFER_FAILURE_MESSAGE,
                "There seems to be a problem in processing. Please try again");
    }

    private boolean isEmiSubventionResponseSuccessful(
            GenericEmiSubventionResponse<?> emiSubventionOrderStampGenericResponse) {

        if (emiSubventionOrderStampGenericResponse.getStatus() == 1
                && emiSubventionOrderStampGenericResponse.getData() != null
                && emiSubventionOrderStampGenericResponse.getError() == null) {
            return true;
        }
        return false;
    }

    private String getPromoCheckoutErrorMsg(Optional<CheckoutPromoServiceResponse> responseOptional) {
        if (responseOptional.isPresent() && responseOptional.get().getData() != null
                && StringUtils.isNotBlank(responseOptional.get().getData().getPromotext())
                && responseOptional.get().getData().getPromoVisibility()) {
            return responseOptional.get().getData().getPromotext();
        }
        return ConfigurationUtil.getProperty("payment.offer.checkout.failure.msg",
                "There seems to be a problem in processing. Please try again");
    }

    private String getPromoCheckoutErrorMsgV2(CheckoutPromoServiceResponseV2 response) {
        if (response != null && response.getData() != null && StringUtils.isNotBlank(response.getData().getPromotext())) {
            return response.getData().getPromotext();
        }
        return ConfigurationUtil.getProperty("payment.offer.checkout.failure.msg",
                "There seems to be a problem in processing. Please try again");
    }

    private String calculateTxnAmountAfterOffer(List<PromoSaving> promoSavings, String txnAmount, String promoAmount) {
        for (PromoSaving promoSaving : promoSavings) {
            if (REDEMPTION_TYPE_DISCOUNT.equalsIgnoreCase(promoSaving.getRedemptionType())) {

                // LOGGER.info("processing promoType {}",
                // REDEMPTION_TYPE_DISCOUNT);
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
            } else if (REDEMPTION_TYPE_CASHBACK.equalsIgnoreCase(promoSaving.getRedemptionType())
                    || REDEMPTION_TYPE_PAYTM_CASHBACK.equalsIgnoreCase(promoSaving.getRedemptionType())) {
                LOGGER.info("processing promoType {}", REDEMPTION_TYPE_CASHBACK);
                if (StringUtils.isNotBlank(promoAmount)) {
                    LOGGER.info("using discountedAmount got from merchant");
                    return String.valueOf(Long.parseLong(txnAmount));
                }
            }
        }
        return txnAmount;
    }

    private String custIdFromWorkflowReqBean(WorkFlowRequestBean workFlowRequestBean) {
        return StringUtils.isNotBlank(workFlowRequestBean.getCustID()) ? workFlowRequestBean.getCustID()
                : (workFlowRequestBean.getUserDetailsBiz() != null) ? workFlowRequestBean.getUserDetailsBiz()
                        .getUserId() : null;
    }

    private String getSubventioncustId(WorkFlowRequestBean workFlowRequestBean) {
        return workFlowRequestBean.getEmiSubventionCustomerId();
    }

    private boolean isPromoCheckoutSuccessResponse(Optional<CheckoutPromoServiceResponse> responseOptional) {
        if (responseOptional.isPresent()) {
            CheckoutPromoServiceResponse response = responseOptional.get();
            if (isPromoServiceSeccessResponse(response) && response.getData() != null
                    && isPromoCheckoutSuccess(response.getData())
                    && StringUtils.isNotBlank(response.getData().getPromocode())) {
                return true;
            }
        }
        return false;
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

    private Optional<CheckoutPromoServiceResponse> checkoutPaymentOffer(
            CheckoutPromoServiceRequest checkoutPromoServiceRequest, Map<String, String> queryParam, int retryCount) {
        int retryAllowed = Integer.parseInt(ConfigurationUtil.getProperty("payment.offer.checkout.retries.count", "2"))
                + retryCount;
        while (retryCount < retryAllowed + 1) {
            try {
                queryParam.put("requestRetry", String.valueOf(retryCount++));
                CheckoutPromoServiceResponse response = paymentPromoService.checkoutPromo(checkoutPromoServiceRequest,
                        queryParam);
                if (response != null && response.getData() != null) {
                    response.getData().setTotalTransactionAmount(
                            String.valueOf(checkoutPromoServiceRequest.getTotalTransactionAmount()));
                }
                return Optional.ofNullable(response);
            } catch (FacadeCheckedException e) {
                LOGGER.error("checkoutPaymentOffer failure for retry {}", retryCount);
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return Optional.empty();
    }

    private CheckoutPromoServiceResponseV2 checkoutPaymentOfferV2(
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
                LOGGER.error("checkoutPaymentOffer failure for retry {}", retryCount);
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        rollbackPaymentOfferCheckoutV2(workFlowRequestBean, true);
        return null;
    }

    // Check for DirectChannel and Set DirectBankCardFlow as true
    private void setDirectChannelRequest(CreateOrderAndPayRequestBean createOrderAndPayRequestBean,
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

    private void setDirectChannelRequest(BizPayRequest payRequest, WorkFlowTransactionBean flowTransBean) {
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

    // Disabling Flag for Direct Bank After Redirect
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

    public String generateCardGCIN(String cardNumber) {
        String GCIN = DigestUtils.sha256Hex(cardNumber + environment.getProperty(JWT_KEY_GCIN));
        GCIN = ISOCARD + GLOBAL_CARD_INDEX_SEP + GCIN;
        LOGGER.info("GCIN Generated: {}", GCIN);
        return GCIN;
    }

    private boolean isNotExemptedPayMode(WorkFlowRequestBean workFlowRequestBean) {
        try {
            EPayMode ePayMode = workFlowRequestBean.getPaytmExpressAddOrHybrid();
            if (ePayMode != null) {
                LOGGER.info("isExemptedPayMode  ePayMode = {}", ePayMode);
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

    private boolean isAcquirementIdBlank(
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse) {
        return !(createOrderAndPayResponse.getResponse() != null && StringUtils.isNotBlank(createOrderAndPayResponse
                .getResponse().getAcquirementId()));
    }

    private boolean orderNotCreated(WorkFlowTransactionBean flowTransBean) {
        GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> responseBean = queryByMerchantRequestId(flowTransBean);
        return (!responseBean.isSuccessfullyProcessed() && ResponseConstants.TARGET_NOT_FOUND == responseBean
                .getResponseConstant());
    }

    /*
     * In case of seamLess we need info to save cards from requestbean.. In
     * Normal case we fetch card details from TransBean savedcardBean
     */
    public GenericCoreResponseBean<CacheCardResponseBean> cacheCard(final WorkFlowTransactionBean flowTransBean,
            CacheCardType cacheCardType) {
        Routes routes = getRouteForCacheCard(flowTransBean);
        CacheCardRequestBean createCacheCardRequestBean = workRequestCreator.createCacheCardRequestBean(flowTransBean,
                cacheCardType);
        GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponse = cacheCardInfoService.cacheCardInfo(
                createCacheCardRequestBean, flowTransBean);
        if (cacheCardResponse.isSuccessfullyProcessed()) {
            GenericCoreResponseBean<CacheCardResponseBean> verifyCardHashResponse = verifyCardHash(flowTransBean,
                    createCacheCardRequestBean, cacheCardResponse.getResponse().getCardIndexNo());
            if (verifyCardHashResponse != null) {
                return verifyCardHashResponse;
            }
        }
        return cacheCardResponse;
    }

    public GenericCoreResponseBean<CacheCardResponseBean> cacheCard(final WorkFlowTransactionBean flowTransBean,
            final boolean isSeamLess) {
        CacheCardRequestBean createCacheCardRequestBean = null;
        Routes routes = getRouteForCacheCard(flowTransBean);
        if (isSeamLess) {
            if (StringUtils.isNotEmpty(flowTransBean.getWorkFlowBean().getSavedCardID())
                    && flowTransBean.getWorkFlowBean().getSavedCardID().length() > 15
                    && !flowTransBean.getWorkFlowBean().isCoftTokenTxn()) {
                createCacheCardRequestBean = workRequestCreator.getCacheCardRequestBeanForSeamless(flowTransBean);
            } else {
                createCacheCardRequestBean = workRequestCreator.createCacheCardRequestBean(flowTransBean, true);
            }
        } else {
            createCacheCardRequestBean = workRequestCreator.createCacheCardRequestBean(flowTransBean);
        }

        return cacheCardInfoService.cacheCardInfo(createCacheCardRequestBean, flowTransBean);
    }

    public GenericCoreResponseBean<CardBeanBiz> fetchSavedCardByID(WorkFlowTransactionBean flowTransBean) {

        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSavedCardID())
                && (flowTransBean.getSubscriptionServiceResponse() != null)
                && StringUtils.isBlank(flowTransBean.getSubscriptionServiceResponse().getSavedCardID())) {
            return new GenericCoreResponseBean<>("InvalidSavedCardID");
        }

        if (StringUtils.isBlank(flowTransBean.getUserDetails().getUserId())) {
            return new GenericCoreResponseBean<>("InvalidUserID");
        }

        SavedCardResponse<SavedCardVO> savedCard = null;
        String savedCardID = StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSavedCardID()) ? flowTransBean
                .getSubscriptionServiceResponse().getSavedCardID() : flowTransBean.getWorkFlowBean().getSavedCardID();
        String userID = flowTransBean.getUserDetails().getUserId();
        String mId = flowTransBean.getWorkFlowBean().getPaytmMID();
        String custId = flowTransBean.getWorkFlowBean().getCustID();
        if (StringUtils.isNotEmpty(mId) && StringUtils.isNotEmpty(custId)) {

            if (flowTransBean.getWorkFlowBean().isStoreCardPrefEnabled()
                    || ERequestType.isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType()
                            .getType())) {
                savedCard = savedCardsService.getSavedCardByCardId(Long.parseLong(savedCardID), userID, custId, mId);
            }
        } else {
            savedCard = savedCardsService.getSavedCardByCardId(Long.parseLong(savedCardID), userID);
        }

        if (!savedCard.getStatus()) {
            LOGGER.error("Error occured while fetching data for saved card id : {}, due to reason : {}", savedCardID,
                    savedCard.getMessage());
            return new GenericCoreResponseBean<>(savedCard.getMessage());
        }

        String decryptedCardNumber = null;
        String decryptedExpiryDate = null;
        try {
            decryptedCardNumber = savedCard.getResponseData().getCardNumber();
            decryptedExpiryDate = savedCard.getResponseData().getExpiryDate();
        } catch (Exception e) {
            return new GenericCoreResponseBean<>("Exception occurred while decrypting card details using master key");
        }
        CardBeanBiz cardBean = mappingUtility.mapDecryptedSavedCard(savedCard.getResponseData(), decryptedCardNumber,
                decryptedExpiryDate);
        if (!BizParamValidator.validateInputObjectParam(cardBean)) {
            return new GenericCoreResponseBean<CardBeanBiz>("MappedCard Bean is null");
        }

        return new GenericCoreResponseBean<CardBeanBiz>(cardBean);
    }

    /**
     * Method to fetch savecards on the basis of custID and MID in subscription
     * not login flow
     *
     * @param flowTransBean
     */
    public GenericCoreResponseBean<CardBeanBiz> fetchSavedCardByCustIdMid(WorkFlowTransactionBean flowTransBean) {
        if (StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSavedCardID())
                && (flowTransBean.getSubscriptionServiceResponse() != null)
                && StringUtils.isBlank(flowTransBean.getSubscriptionServiceResponse().getSavedCardID())) {
            return new GenericCoreResponseBean<>("InvalidSavedCardID");
        }
        SavedCardResponse<SavedCardVO> savedCard = null;
        String savedCardID = StringUtils.isBlank(flowTransBean.getWorkFlowBean().getSavedCardID()) ? flowTransBean
                .getSubscriptionServiceResponse().getSavedCardID() : flowTransBean.getWorkFlowBean().getSavedCardID();
        String mId = flowTransBean.getWorkFlowBean().getPaytmMID();
        String custId = flowTransBean.getWorkFlowBean().getCustID();
        if (StringUtils.isNotEmpty(mId) && StringUtils.isNotEmpty(custId)) {
            if (flowTransBean.getWorkFlowBean().isStoreCardPrefEnabled()
                    || ERequestType.isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType()
                            .getType())) {
                savedCard = savedCardsService.getSavedCardByCardId(Long.parseLong(savedCardID), custId, mId);
            }
        }

        if (savedCard != null && !savedCard.getStatus()) {
            LOGGER.error("Error occured while fetching data for saved card id : {}, due to reason : {}", savedCardID,
                    savedCard.getMessage());
            return new GenericCoreResponseBean<>(savedCard.getMessage());
        }

        String decryptedCardNumber = null;
        String decryptedExpiryDate = null;
        try {
            decryptedCardNumber = savedCard.getResponseData().getCardNumber();
            decryptedExpiryDate = savedCard.getResponseData().getExpiryDate();
        } catch (Exception e) {
            return new GenericCoreResponseBean<>("Exception occurred while decrypting card details using master key");
        }
        CardBeanBiz cardBean = mappingUtility.mapDecryptedSavedCard(savedCard.getResponseData(), decryptedCardNumber,
                decryptedExpiryDate);
        if (!BizParamValidator.validateInputObjectParam(cardBean)) {
            return new GenericCoreResponseBean<CardBeanBiz>("MappedCard Bean is null");
        }

        return new GenericCoreResponseBean<CardBeanBiz>(cardBean);
    }

    /*
     * Filter Paymodes , based on request param allowed and disabled payModes
     */
    private GenericCoreResponseBean<ConsultPayViewResponseBizBean> filterPaymentModes(
            WorkFlowTransactionBean workFlowTransBean, ConsultPayViewResponseBizBean consultPayViewResponseBean) {

        if (BizParamValidator.validateInputObjectParam(consultPayViewResponseBean)
                && BizParamValidator.validateInputObjectParam(workFlowTransBean)
                && BizParamValidator.validateInputObjectParam(workFlowTransBean.getWorkFlowBean())) {

            List<PayMethodViewsBiz> paymentMethodViewsList = consultPayViewResponseBean.getPayMethodViews();
            List<String> allowedPaymentModes = workFlowTransBean.getWorkFlowBean().getAllowedPaymentModes();
            List<String> disabledPaymentModes = workFlowTransBean.getWorkFlowBean().getDisabledPaymentModes();
            consultPayViewResponseBean.setPayMethodViews(filterPaymentModes(paymentMethodViewsList,
                    allowedPaymentModes, disabledPaymentModes, false));
            filterPaytmRelatedPayModes(consultPayViewResponseBean, workFlowTransBean, allowedPaymentModes,
                    disabledPaymentModes);

        }
        return new GenericCoreResponseBean<ConsultPayViewResponseBizBean>(consultPayViewResponseBean);

    }

    /*
     * Filter Wallet, PPBL, PAYTM_DIGITAL_CREDIT
     */
    private void filterPaytmRelatedPayModes(final ConsultPayViewResponseBizBean consultPayViewResponseBean,
            final WorkFlowTransactionBean workFlowTransBean, final List<String> allowedPaymentModes,
            final List<String> disabledPaymentModes) {

        boolean strict = false;

        if (!allowedPaymentModes.isEmpty())
            strict = true;

        if ((strict && !allowedPaymentModes.contains(EPayMethod.PPBL.toString()))
                || (!strict && disabledPaymentModes.contains(EPayMethod.PPBL.toString()))) {
            consultPayViewResponseBean.setPaymentsBankSupported(false);
        }
    }

    private void filterPaytmRelatedPayModes(final LitePayviewConsultResponseBizBean consultPayViewResponseBean,
            final WorkFlowTransactionBean workFlowTransBean, final List<String> allowedPaymentModes,
            final List<String> disabledPaymentModes) {

        boolean strict = false;

        if (!allowedPaymentModes.isEmpty())
            strict = true;

        if ((strict && !allowedPaymentModes.contains(EPayMethod.PPBL.toString()))
                || (!strict && disabledPaymentModes.contains(EPayMethod.PPBL.toString()))) {
            consultPayViewResponseBean.setPaymentsBankSupported(false);
        }
    }

    private List<PayMethodViewsBiz> filterPaymentModes(List<PayMethodViewsBiz> paymentMethodViewsList,
            List<String> allowedPaymentModes, List<String> disabledPaymentModes, boolean isEnhancedCashierRequest) {
        if ((BizParamValidator.validateInputListParam(allowedPaymentModes) || BizParamValidator
                .validateInputListParam(disabledPaymentModes))
                && BizParamValidator.validateInputObjectParam(paymentMethodViewsList)) {
            boolean strict = false;
            List<String> paymentModesAllowed = (allowedPaymentModes == null) ? new ArrayList<>() : new ArrayList<>(
                    allowedPaymentModes);
            List<String> paymentModesDisabled = (disabledPaymentModes == null) ? new ArrayList<>() : new ArrayList<>(
                    disabledPaymentModes);
            List<PayMethodViewsBiz> filteredPayMethodViewsList = new ArrayList<>();

            if (!paymentModesAllowed.isEmpty()) {
                strict = true;
            }

            for (PayMethodViewsBiz payMethodView : paymentMethodViewsList) {
                // Below check is to allow only PPBL in NB, in case where PPBL
                // only comes in allowedPaymentMode
                if (payMethodView.getPayMethod().equals(EPayMethod.NET_BANKING.getMethod())
                        && paymentModesAllowed.contains(EPayMethod.PPBL.getOldName())
                        && !paymentModesAllowed.contains(EPayMethod.NET_BANKING.getMethod())) {
                    paymentModesAllowed.add(EPayMethod.NET_BANKING.getMethod());
                    setPPBLOnlyInNB(payMethodView);
                }

                // To set PPBL if NB is disabled
                if (payMethodView.getPayMethod().equals(EPayMethod.NET_BANKING.getMethod())
                        && paymentModesDisabled.contains(EPayMethod.NET_BANKING.getMethod())
                        && !paymentModesDisabled.contains(EPayMethod.PPBL.getOldName())) {
                    paymentModesDisabled.remove(EPayMethod.NET_BANKING.getMethod());
                    setPPBLOnlyInNB(payMethodView);
                }

                if (strict && isEnhancedCashierRequest && paymentModesAllowed.contains(EPayMethod.MP_COD.getOldName())
                        && StringUtils.equalsIgnoreCase(payMethodView.getPayMethod(), EPayMethod.MP_COD.getMethod())) {
                    filteredPayMethodViewsList.add(payMethodView);
                }

                if (strict && paymentModesAllowed.contains(payMethodView.getPayMethod())) {
                    if (!paymentModesDisabled.contains(payMethodView.getPayMethod())) {
                        filteredPayMethodViewsList.add(payMethodView);
                    }
                } else if (!strict && !paymentModesDisabled.contains(payMethodView.getPayMethod())) {
                    filteredPayMethodViewsList.add(payMethodView);
                } else {
                    LOGGER.debug("Removing pay method :: {}", payMethodView);
                }
            }

            return filteredPayMethodViewsList;
        }
        return paymentMethodViewsList;
    }

    private void setPPBLOnlyInNB(PayMethodViewsBiz payMethodView) {
        List<PayChannelOptionViewBiz> list = payMethodView.getPayChannelOptionViews().stream()
                .filter(payOptionView -> payOptionView.getPayOption().equals("NET_BANKING_PPBL"))
                .collect(Collectors.toList());
        payMethodView.setPayChannelOptionViews(list);
    }

    private void setTLSWarning(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean,
            ERequestType requestType, boolean addAndPay) {
        if (litePayviewConsultResponseBizBean != null && litePayviewConsultResponseBizBean.getPayMethodViews() != null) {
            List<PayMethodViewsBiz> paymentMethodViewsList = litePayviewConsultResponseBizBean.getPayMethodViews();
            setTLSWarning(addAndPay, paymentMethodViewsList, requestType);
        }
    }

    private void setTLSWarning(ConsultPayViewResponseBizBean consultResponseBizBean, ERequestType requestType,
            boolean addAndPay) {
        if (consultResponseBizBean != null && consultResponseBizBean.getPayMethodViews() != null) {
            List<PayMethodViewsBiz> paymentMethodViewsList = consultResponseBizBean.getPayMethodViews();
            setTLSWarning(addAndPay, paymentMethodViewsList, requestType);
        }
    }

    private void setTLSWarning(boolean addAndPay, List<PayMethodViewsBiz> paymentMethodViewsList,
            ERequestType requestType) {
        if (paymentMethodViewsList == null)
            return;
        for (PayMethodViewsBiz payMethodViewsBiz : paymentMethodViewsList) {
            if ((PayMethod.CREDIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod()) || PayMethod.DEBIT_CARD
                    .getMethod().equals(payMethodViewsBiz.getPayMethod()))
                    && isAnyChannelEnable(payMethodViewsBiz.getPayChannelOptionViews()) && isTLSHeaderPresent()) {
                LOGGER.info("TLS header present disabling {}", payMethodViewsBiz.getPayMethod());
                if (addAndPay && ERequestType.ADD_MONEY != requestType) {
                    httpServletRequest().setAttribute(TheiaConstant.ResponseConstants.ADDNPAY_TLS_WARNING_MESSAGE,
                            getConfiguredTLSWarningMessage());
                } else {
                    httpServletRequest().setAttribute(TheiaConstant.ResponseConstants.TLS_WARNING_MESSAGE,
                            getConfiguredTLSWarningMessage());
                }
            }
        }
    }

    private String getConfiguredTLSWarningMessage() {
        return ConfigurationUtil.getProperty(BizConstant.TLS_WARNING_MESSAGE, TLS_WARNING_DEFAULT_MSG);
    }

    private boolean isAnyChannelEnable(List<PayChannelOptionViewBiz> channelOptionViewBizs) {
        for (PayChannelOptionViewBiz channelOptionViewBiz : channelOptionViewBizs) {
            if (channelOptionViewBiz.isEnableStatus()) {
                return true;
            }
        }
        return false;
    }

    private GenericCoreResponseBean<LitePayviewConsultResponseBizBean> filterPaymentModes(
            WorkFlowTransactionBean workFlowTransBean,
            LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {

        if (BizParamValidator.validateInputObjectParam(litePayviewConsultResponseBizBean)
                && BizParamValidator.validateInputObjectParam(workFlowTransBean)
                && BizParamValidator.validateInputObjectParam(workFlowTransBean.getWorkFlowBean())) {

            List<PayMethodViewsBiz> paymentMethodViewsList = litePayviewConsultResponseBizBean.getPayMethodViews();
            List<String> allowedPaymentModes = workFlowTransBean.getWorkFlowBean().getAllowedPaymentModes();
            List<String> disabledPaymentModes = workFlowTransBean.getWorkFlowBean().getDisabledPaymentModes();
            boolean isEnhancedCashierRequest = workFlowTransBean.getWorkFlowBean().isEnhancedCashierPageRequest();
            litePayviewConsultResponseBizBean.setPayMethodViews(filterPaymentModes(paymentMethodViewsList,
                    allowedPaymentModes, disabledPaymentModes, isEnhancedCashierRequest));
            if (workFlowTransBean.isDefaultLiteViewFlow()) {
                filterPaytmRelatedPayModes(litePayviewConsultResponseBizBean, workFlowTransBean, allowedPaymentModes,
                        disabledPaymentModes);
            }

        }
        return new GenericCoreResponseBean<LitePayviewConsultResponseBizBean>(litePayviewConsultResponseBizBean);
    }

    /*
     * Filter saved cards , on the basis of filtered ConsultPayView .
     */
    public void filterSavedCards(WorkFlowTransactionBean flowTransBean) {
        UserDetailsBiz userDetails = flowTransBean.getUserDetails();
        List<CardBeanBiz> merchantSavedCardList = null;

        if (userDetails != null && !CollectionUtils.isEmpty(userDetails.getMerchantViewSavedCardsList())) {
            /*
             * Merchant PayModes
             */
            if (BizParamValidator.validateInputListParam(flowTransBean.getMerchantViewConsult().getPayMethodViews())) {
                List<PayMethodViewsBiz> merchantConsultViewList = flowTransBean.getMerchantViewConsult()
                        .getPayMethodViews();
                if (!CollectionUtils.isEmpty(merchantConsultViewList)) {
                    if (ERequestType.NATIVE_SUBSCRIPTION_PAY.getType().equalsIgnoreCase(
                            flowTransBean.getWorkFlowBean().getRequestType().getType())) {
                        merchantSavedCardList = getMerchantSavedCardListForSubscription(userDetails,
                                merchantConsultViewList, flowTransBean.getWorkFlowBean().getSubsPayMode(),
                                flowTransBean);
                    } else {
                        merchantSavedCardList = getMerchantSavedCardList(userDetails, merchantConsultViewList,
                                flowTransBean);
                    }
                }
            }

            /*
             * AddAndPay PayModes
             */
            if ((flowTransBean.getAddAndPayViewConsult() != null)
                    && BizParamValidator.validateInputListParam(flowTransBean.getAddAndPayViewConsult()
                            .getPayMethodViews())) {
                List<PayMethodViewsBiz> addAndPayConsultViewList = flowTransBean.getAddAndPayViewConsult()
                        .getPayMethodViews();
                if (!CollectionUtils.isEmpty(addAndPayConsultViewList)) {
                    List<CardBeanBiz> addAndPaySavedCardList = getAddAndPaySavedCardList(userDetails,
                            addAndPayConsultViewList, flowTransBean);
                    userDetails.setAddAndPayViewSavedCardsList(addAndPaySavedCardList);
                }
            }
            userDetails.setMerchantViewSavedCardsList(merchantSavedCardList);
        }
        filterSavedVPA(flowTransBean);
    }

    /*
     * remove sarvatra vpa from saved instruments
     */
    private void filterSavedVPA(WorkFlowTransactionBean flowTransBean) {
        if (CollectionUtils.isEmpty(flowTransBean.getSarvatraVpa())) {
            return;
        }

        if (!CollectionUtils.isEmpty(flowTransBean.getUserDetails().getMerchantViewSavedCardsList())) {
            Iterator<CardBeanBiz> iterator = flowTransBean.getUserDetails().getMerchantViewSavedCardsList().iterator();
            while (iterator.hasNext()) {
                CardBeanBiz cardBeanBiz = iterator.next();
                if (flowTransBean.getSarvatraVpa().contains(cardBeanBiz.getCardNumber())) {
                    iterator.remove();
                }
            }
        }

        if (!CollectionUtils.isEmpty(flowTransBean.getUserDetails().getAddAndPayViewSavedCardsList())) {
            Iterator<CardBeanBiz> iterator = flowTransBean.getUserDetails().getAddAndPayViewSavedCardsList().iterator();
            while (iterator.hasNext()) {
                CardBeanBiz cardBeanBiz = iterator.next();
                if (flowTransBean.getSarvatraVpa().contains(cardBeanBiz.getCardNumber())) {
                    iterator.remove();
                }
            }
        }

    }

    /*
     * Filter saved cards , on the basis of filtered LitePayViewConsult .
     */

    public void filterSavedCardsForLitePayviewConsult(WorkFlowTransactionBean flowTransBean) {
        UserDetailsBiz userDetails = flowTransBean.getUserDetails();
        List<CardBeanBiz> merchantSavedCardList = null;

        if (!CollectionUtils.isEmpty(userDetails.getMerchantViewSavedCardsList())) {
            /*
             * Merchant PayModes
             */
            if (BizParamValidator
                    .validateInputListParam(flowTransBean.getMerchantLiteViewConsult().getPayMethodViews())) {
                List<PayMethodViewsBiz> merchantConsultViewList = flowTransBean.getMerchantLiteViewConsult()
                        .getPayMethodViews();
                if (!CollectionUtils.isEmpty(merchantConsultViewList)) {
                    merchantSavedCardList = getMerchantSavedCardList(userDetails, merchantConsultViewList,
                            flowTransBean);
                }
            }
            /*
             * AddAndPay PayModes
             */
            if ((flowTransBean.getAddAndPayLiteViewConsult() != null)
                    && BizParamValidator.validateInputListParam(flowTransBean.getAddAndPayLiteViewConsult()
                            .getPayMethodViews())) {
                List<PayMethodViewsBiz> addAndPayConsultViewList = flowTransBean.getAddAndPayLiteViewConsult()
                        .getPayMethodViews();
                if (!CollectionUtils.isEmpty(addAndPayConsultViewList)) {
                    List<CardBeanBiz> addAndPaySavedCardList = getAddAndPaySavedCardList(userDetails,
                            addAndPayConsultViewList, flowTransBean);
                    userDetails.setAddAndPayViewSavedCardsList(addAndPaySavedCardList);
                }
            }
        }
        userDetails.setMerchantViewSavedCardsList(merchantSavedCardList);
    }

    private List<CardBeanBiz> getAddAndPaySavedCardList(UserDetailsBiz userDetails,
            List<PayMethodViewsBiz> addAndPayConsultViewList, WorkFlowTransactionBean flowTransBean) {
        return filterSavedCards(userDetails, addAndPayConsultViewList, flowTransBean);
    }

    private List<CardBeanBiz> getMerchantSavedCardList(UserDetailsBiz userDetails,
            List<PayMethodViewsBiz> merchantConsultViewList, WorkFlowTransactionBean flowTransBean) {
        return filterSavedCards(userDetails, merchantConsultViewList, flowTransBean);
    }

    private List<CardBeanBiz> getMerchantSavedCardListForSubscription(UserDetailsBiz userDetails,
            List<PayMethodViewsBiz> merchantConsultViewList, SubsPaymentMode subsPaymentMode,
            WorkFlowTransactionBean flowTransBean) {
        if (SubsPaymentMode.UNKNOWN == subsPaymentMode) {
            UserDetailsBiz userDetailsBiz1 = SerializationUtils.clone(userDetails);
            userDetailsBiz1.setMerchantViewSavedCardsList(filterSavedCardsForSubscription(flowTransBean
                    .getWorkFlowBean().isFromAoaMerchant(), userDetailsBiz1, SubsPaymentMode.NORMAL));
            return filterSavedCards(userDetailsBiz1, merchantConsultViewList, flowTransBean);
        }
        return filterSavedCards(userDetails, merchantConsultViewList, flowTransBean);
    }

    private List<CardBeanBiz> filterSavedCards(UserDetailsBiz userDetails, List<PayMethodViewsBiz> consultViewList,
            WorkFlowTransactionBean flowTransBean) {
        List<CardBeanBiz> savedCardList = new ArrayList<>();
        List<CardBeanBiz> savedCardFromDB = userDetails.getMerchantViewSavedCardsList();

        Set<String> enabledPayMethods = new HashSet<>();
        Set<String> supportedCountry;
        Map<String, Set<String>> enableCCDCPayMethods = new HashMap<>();
        Map<String, Set<String>> channelsNotAvailableVsSupportedCountries = new HashMap<>();
        boolean returnDisabledCards = flowTransBean.getWorkFlowBean().isReturnDisabledChannelInFpo();
        for (PayMethodViewsBiz payMethod : consultViewList) {
            if (BizParamValidator.validateInputListParam(payMethod.getPayChannelOptionViews())) {
                for (PayChannelOptionViewBiz payChannelOption : payMethod.getPayChannelOptionViews()) {
                    if (payChannelOption.isEnableStatus()) {
                        enabledPayMethods.add(payMethod.getPayMethod());
                        if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                                || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                            supportedCountry = new HashSet<>();
                            supportedCountry.addAll(payChannelOption.getSupportCountries());
                            enableCCDCPayMethods.put(payChannelOption.getPayOption(), supportedCountry);
                        }
                    } else {
                        if (returnDisabledCards
                                && TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE.equals(payChannelOption
                                        .getDisableReason())) {
                            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                                supportedCountry = new HashSet<>();
                                supportedCountry.addAll(payChannelOption.getSupportCountries());
                                channelsNotAvailableVsSupportedCountries.put(payChannelOption.getPayOption(),
                                        supportedCountry);
                            }
                        }
                    }
                }
            }
        }

        BinDetail binDetail;
        for (CardBeanBiz cardData : savedCardFromDB) {
            try {
                binDetail = cardUtils.fetchBinDetails(cardData.getCardNumber());
                String cardKey = cardData.getCardType() + "_" + cardData.getCardScheme();
                if (!binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey) != null
                        && enableCCDCPayMethods.get(cardKey).contains("INTL")) {
                    savedCardList.add(cardData);
                } else if (binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey) != null) {
                    savedCardList.add(cardData);
                }
                if (returnDisabledCards && channelsNotAvailableVsSupportedCountries.get(cardKey) != null) {
                    if (!binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey).contains("INTL")) {
                        cardData.setDisabled(true);
                        cardData.setDisabledReason(TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE);
                        savedCardList.add(cardData);
                    } else if (binDetail.getIsIndian()) {
                        cardData.setDisabled(true);
                        cardData.setDisabledReason(TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE);
                        savedCardList.add(cardData);
                    }
                }
            } catch (PaytmValidationException e) {
                LOGGER.info("Not able to fetch bin for bin {} for International Card Payment",
                        cardData.getFirstSixDigit());
            }
        }

        return savedCardList;
    }

    public GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantTransID(
            WorkFlowTransactionBean flowTransBean, boolean isNeedFullInfoRequired) {

        QueryByMerchantTransIDRequestBizBean queryByMerchantTransIDRequest = workRequestCreator
                .createQueryByMerchantTransIdRequest(flowTransBean, isNeedFullInfoRequired);
        GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantTransIDResponseBizBean = orderService
                .createQueryByMerchantTransId(queryByMerchantTransIDRequest, flowTransBean.getWorkFlowBean()
                        .isFromAoaMerchant());
        if (!queryByMerchantTransIDResponseBizBean.isSuccessfullyProcessed()) {
            LOGGER.error("Query Pay Result failed due to : {}",
                    queryByMerchantTransIDResponseBizBean.getFailureMessage());
            return new GenericCoreResponseBean<>(queryByMerchantTransIDResponseBizBean.getFailureMessage());
        }

        return queryByMerchantTransIDResponseBizBean;
    }

    public GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> queryByMerchantRequestId(
            WorkFlowTransactionBean flowTransBean) {
        QueryByMerchantRequestIdBizBean queryByMerchantRequestIdBean = workRequestCreator
                .createQueryByMerchantRequestIdRequest(flowTransBean);
        queryByMerchantRequestIdBean.setMerchantId(flowTransBean.getWorkFlowBean().getPaytmMID());
        queryByMerchantRequestIdBean.setRoute(Routes.PG2);
        return orderService.createQueryByMerchantRequestId(queryByMerchantRequestIdBean);
    }

    public GenericCoreResponseBean<BizCancelOrderResponse> closeOrder(WorkFlowTransactionBean workFlowTransactionBean) {
        if (workFlowTransactionBean.getWorkFlowBean().isNativeAddMoney()) {
            return closeFundOrder(workFlowTransactionBean);
        }
        String alipayMid = workFlowTransactionBean.getWorkFlowBean().getAlipayMID();

        if (StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getDummyAlipayMid())) {
            alipayMid = workFlowTransactionBean.getWorkFlowBean().getDummyAlipayMid();
        }
        BizCancelOrderRequest bizCancelOrderRequest = new BizCancelOrderRequest(alipayMid,
                workFlowTransactionBean.getTransID(), workFlowTransactionBean.getWorkFlowBean().getCloseReason(),
                workFlowTransactionBean.getWorkFlowBean().isFromAoaMerchant(), workFlowTransactionBean
                        .getWorkFlowBean().getPaytmMID(), workFlowTransactionBean.getWorkFlowBean().getRoute());

        if (workFlowTransactionBean.getWorkFlowBean().isFromAoaMerchant()) {
            bizCancelOrderRequest.setFromAoaMerchant(true);
        }

        return orderService.closeOrder(bizCancelOrderRequest);
    }

    public GenericCoreResponseBean<BizCancelOrderResponse> closeFundOrder(
            WorkFlowTransactionBean workFlowTransactionBean) {
        EnvInfoRequestBean envInfo = workFlowTransactionBean.getEnvInfoReqBean();
        if (envInfo == null) {
            envInfo = workFlowTransactionBean.getWorkFlowBean().getEnvInfoReqBean();
        }
        BizCancelFundOrderRequest bizCancelFundOrderRequest = new BizCancelFundOrderRequest(
                workFlowTransactionBean.getTransID(), envInfo);
        bizCancelFundOrderRequest.setPaytmMerchantId(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        bizCancelFundOrderRequest.setRoute(Routes.PG2);
        return orderService.closeFundOrder(bizCancelFundOrderRequest);
    }

    public String subscritpionMode(WorkFlowTransactionBean workFlowTransactionBean) {
        // Check if , Subscription Mode is AddAndPay, PPI
        if (workFlowTransactionBean.getWorkFlowBean().getSubsPayMode().equals(SubsPaymentMode.CC)) {
            workFlowTransactionBean.setAllowedPayMode(EPayMode.NONE);
            return "CC";
        }

        if (workFlowTransactionBean.getWorkFlowBean().getSubsPayMode().equals(SubsPaymentMode.NORMAL)) {
            workFlowTransactionBean.setAllowedPayMode(EPayMode.ADDANDPAY);
            return "ADDPPI";
        }

        workFlowTransactionBean.setAllowedPayMode(EPayMode.ADDANDPAY);

        if (workFlowTransactionBean.getUserDetails() != null) {

            List<PayMethodViewsBiz> merchantPayMethodViewsList = workFlowTransactionBean.getMerchantViewConsult()
                    .getPayMethodViews();

            String accountBalance = StringUtils.EMPTY;

            for (PayMethodViewsBiz payMethodView : merchantPayMethodViewsList) {
                if (payMethodView.getPayMethod().equals(PayMethod.BALANCE.getMethod())) {
                    if (payMethodView.getPayChannelOptionViews().get(0).isEnableStatus()) {
                        accountBalance = payMethodView.getPayChannelOptionViews().get(0).getBalanceChannelInfos()
                                .get(0).getAccountBalance();
                        accountBalance = AmountUtils.getTransactionAmountInRupee(accountBalance);
                        if (Double.parseDouble(accountBalance) > Double.parseDouble(workFlowTransactionBean
                                .getWorkFlowBean().getTxnAmount())) {
                            return "PPI";
                        }
                        return "ADDPPI";
                    }
                }
            }

        } else {
            return "PPI";
        }

        LOGGER.error("Balance fetch Failed ");
        return "FAIL";

    }

    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> subscriptionFilteredConsultView(
            WorkFlowTransactionBean workFlowTransactionBean) {

        List<PayMethodViewsBiz> merchantPayMethodViewsList = workFlowTransactionBean.getMerchantViewConsult()
                .getPayMethodViews();
        List<PayMethodViewsBiz> filteredPayMethodViews = new ArrayList<>();
        ConsultPayViewResponseBizBean filteredConsultView = workFlowTransactionBean.getMerchantViewConsult();
        UserDetailsBiz userDetailsBiz = workFlowTransactionBean.getUserDetails();

        if (workFlowTransactionBean.getSubscriptionPayMode().equals(PayMethod.CREDIT_CARD.getOldName())) {
            for (PayMethodViewsBiz payMethodView : merchantPayMethodViewsList) {
                if (payMethodView.getPayMethod().equals(PayMethod.CREDIT_CARD.getMethod())) {
                    filteredPayMethodViews.add(payMethodView);
                    if (null != userDetailsBiz.getMerchantViewSavedCardsList()) {
                        filterCardOfRequiredType(workFlowTransactionBean, PayMethod.CREDIT_CARD.getMethod());
                    }
                }
            }
        }

        if ((workFlowTransactionBean.getSubscriptionPayMode().equals("ADDPPI") || workFlowTransactionBean
                .getSubscriptionPayMode().equals("PPI"))
                && workFlowTransactionBean.getWorkFlowBean().getSubsPPIOnly().equals("N")) {

            for (PayMethodViewsBiz payMethodView : merchantPayMethodViewsList) {
                if (payMethodView.getPayMethod().equals(PayMethod.CREDIT_CARD.getMethod())) {
                    filteredPayMethodViews.add(payMethodView);
                    filterCardOfRequiredType(workFlowTransactionBean, PayMethod.CREDIT_CARD.getMethod());
                }
            }

        }

        if (workFlowTransactionBean.getSubscriptionPayMode().equals("PPI")
                || workFlowTransactionBean.getSubscriptionPayMode().equals("ADDPPI")) {
            for (PayMethodViewsBiz payMethodView : merchantPayMethodViewsList) {
                if (payMethodView.getPayMethod().equals(PayMethod.BALANCE.getMethod())) {
                    filteredPayMethodViews.add(payMethodView);
                }
            }
        }

        if (filteredPayMethodViews.isEmpty()) {
            LOGGER.error("Filtered Pay methods calculation failed as payMethod List is Empty");
            return new GenericCoreResponseBean<>("Subscription filtered PayModes Calculation Failed");
        }
        filteredConsultView.setPayMethodViews(filteredPayMethodViews);
        workFlowTransactionBean.setMerchantViewConsult(filteredConsultView);
        return new GenericCoreResponseBean<>(filteredConsultView);

    }

    public void filterCardOfRequiredType(WorkFlowTransactionBean workFlowTransactionBean, String cardType) {

        List<CardBeanBiz> cardsList = workFlowTransactionBean.getUserDetails().getMerchantViewSavedCardsList();
        List<CardBeanBiz> filteredCardsList = new ArrayList<>();

        if (!cardsList.isEmpty()) {
            for (CardBeanBiz cardBean : cardsList) {
                if (cardBean.getCardType().equals(cardType)) {
                    filteredCardsList.add(cardBean);
                }
            }

            workFlowTransactionBean.getUserDetails().setMerchantViewSavedCardsList(filteredCardsList);
        }
    }

    /**
     * Subscription specific PayMethods filtering
     *
     * @param workFlowTransBean
     * @param isNativeFlow
     */
    public void filterOperationsForUserNotLoggedIn(WorkFlowTransactionBean workFlowTransBean, boolean isNativeFlow) {
        List<PayMethodViewsBiz> payMethods = isNativeFlow ? workFlowTransBean.getMerchantLiteViewConsult()
                .getPayMethodViews() : workFlowTransBean.getMerchantViewConsult().getPayMethodViews();
        List<PayMethodViewsBiz> filteredModes = new ArrayList<>();

        switch (workFlowTransBean.getWorkFlowBean().getSubsTypes()) {
        case CC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.CREDIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            break;
        case DC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.DEBIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            break;
        case BANK_MANDATE_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BANK_MANDATE.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            break;
        case UNKNOWN:
            Set<String> subsPayMethod = SubsPaymentMode.getEPayMethodNamesNotLoggedIn();
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (subsPayMethod.contains(payMethodViewsBiz.getPayMethod())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            break;
        case UPI_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.UPI.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            break;

        default:
            break;
        }

        if (isNativeFlow) {
            workFlowTransBean.getMerchantLiteViewConsult().setPayMethodViews(filteredModes);
            workFlowTransBean.getMerchantLiteViewConsult().setWalletFailed(true);
        } else {
            workFlowTransBean.getMerchantViewConsult().setPayMethodViews(filteredModes);
            workFlowTransBean.getMerchantViewConsult().setWalletFailed(true);
        }
    }

    public void filterOperationsForUserNotLoggedIn_V2(WorkFlowTransactionBean workFlowTransBean,
            boolean isBalanceChangeRequired) {
        ConsultPayViewResponseBizBean merchantConsult = isBalanceChangeRequired ? workFlowTransBean
                .getMerchantViewConsult() : workFlowTransBean.getAddAndPayViewConsult();

        List<PayMethodViewsBiz> filteredModes = new ArrayList<>();
        List<PayMethodViewsBiz> payMethods = merchantConsult.getPayMethodViews();

        if (SubsTypes.CC_ONLY.equals(workFlowTransBean.getWorkFlowBean().getSubsTypes())) {
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.CREDIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPayMethodViews(filteredModes);
        } else {
            merchantConsult.setLoginMandatory(true);
            merchantConsult.setPayMethodViews(filteredModes);
        }

    }

    public void filterOperations(WorkFlowTransactionBean workFlowTransBean, boolean areMerchantModesBeingFiltered) {
        ConsultPayViewResponseBizBean merchantConsult = areMerchantModesBeingFiltered ? workFlowTransBean
                .getMerchantViewConsult() : workFlowTransBean.getAddAndPayViewConsult();
        List<PayMethodViewsBiz> payMethods = merchantConsult.getPayMethodViews();
        List<PayMethodViewsBiz> filteredModes = new ArrayList<>();
        switch (workFlowTransBean.getWorkFlowBean().getSubsTypes()) {
        case CC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.CREDIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }

            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case NORMAL:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BALANCE.name()) && areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                } else if (payMethodViewsBiz.getPayMethod().equals(PayMethod.CREDIT_CARD.name())
                        && !areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case PPI_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BALANCE.name())) {
                    filteredModes.add(payMethodViewsBiz);
                } else if (!areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case DC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.DEBIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case PPBL_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.PPBL.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(true);
            merchantConsult.setWalletFailed(true);
            break;

        default:
            break;
        }
        merchantConsult.setPayMethodViews(filteredModes);
        if (areMerchantModesBeingFiltered) {
            workFlowTransBean.setMerchantViewConsult(merchantConsult);
        } else {
            workFlowTransBean.setAddAndPayViewConsult(merchantConsult);
        }

    }

    /**
     * Filter subscription related payment modes according to its type
     *
     * @param workFlowTransBean
     * @param areMerchantModesBeingFiltered
     */
    public void filterOperationsLitePayView(WorkFlowTransactionBean workFlowTransBean,
            boolean areMerchantModesBeingFiltered) {
        LitePayviewConsultResponseBizBean merchantConsult = areMerchantModesBeingFiltered ? workFlowTransBean
                .getMerchantLiteViewConsult() : workFlowTransBean.getAddAndPayLiteViewConsult();
        List<PayMethodViewsBiz> payMethods = merchantConsult.getPayMethodViews();
        List<PayMethodViewsBiz> filteredModes = new ArrayList<>();
        switch (workFlowTransBean.getWorkFlowBean().getSubsTypes()) {
        case CC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.CREDIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }

            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case NORMAL:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BALANCE.name()) && areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                } else if ((PropertiesUtil.getInstance().isPayModeSupportedForRenew(payMethodViewsBiz.getPayMethod()))
                        && !areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case PPI_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BALANCE.name())) {
                    filteredModes.add(payMethodViewsBiz);
                } else if (!areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case DC_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.DEBIT_CARD.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case PPBL_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.PPBL.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(true);
            merchantConsult.setWalletFailed(true);
            break;
        case BANK_MANDATE_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.BANK_MANDATE.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case UPI_ONLY:
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (payMethodViewsBiz.getPayMethod().equals(PayMethod.UPI.name())) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setWalletFailed(true);
            merchantConsult.setPaymentsBankSupported(false);
            break;
        case UNKNOWN:
            Set<String> subsPayMethod = SubsPaymentMode.getEPayMethodNames();
            for (PayMethodViewsBiz payMethodViewsBiz : payMethods) {
                if (areMerchantModesBeingFiltered && subsPayMethod.contains(payMethodViewsBiz.getPayMethod())) {
                    filteredModes.add(payMethodViewsBiz);
                } else if (!areMerchantModesBeingFiltered) {
                    filteredModes.add(payMethodViewsBiz);
                }
            }
            merchantConsult.setPaymentsBankSupported(true);
            break;
        default:
            break;
        }
        merchantConsult.setPayMethodViews(filteredModes);
        if (areMerchantModesBeingFiltered) {
            workFlowTransBean.setMerchantLiteViewConsult(merchantConsult);

            /**
             * Setting filtered Paymethod in merchantViewConsult, So that in
             * FilterSavedCardTask, we received filtered Paymethods for
             * Subscription.
             */

            workFlowTransBean.getMerchantViewConsult().setPayMethodViews(filteredModes);
        } else {
            workFlowTransBean.setAddAndPayLiteViewConsult(merchantConsult);
            /**
             * Setting filtered Paymethod in AddnPayViewConsult, So that in
             * FilterSavedCardTask, we received filtered Paymethods for
             * Subscription.
             */
            workFlowTransBean.getAddAndPayViewConsult().setPayMethodViews(filteredModes);
        }

    }

    public boolean isValidSubsPaymode(WorkFlowTransactionBean workFlowTransactionBean,
            PayMethodViewsBiz payMethodViewsBiz) {
        if (SubsPaymentMode.BANK_MANDATE.getePayMethodName().equals(payMethodViewsBiz.getPayMethod())) {
            if (workFlowTransactionBean.getWorkFlowBean().isSubsRetry()
                    || workFlowTransactionBean.getWorkFlowBean().isSubsRenew()) {
                return false;
            }
        }
        if (SubsPaymentMode.PPI.getePayMethodName().equals(payMethodViewsBiz.getPayMethod())) {
            if (workFlowTransactionBean.getWorkFlowBean().isSubsRenew()
                    || workFlowTransactionBean.getWorkFlowBean().isSubsRetry()) {
                return false;
            }
        }
        return true;
    }

    public void setAdditionalParamsForSubsRenewal(final WorkFlowTransactionBean workFlowTransBean) {
        MappingMerchantUrlInfo merchantUrlInfo = null;
        try {
            MerchantUrlInfo urlInfo = merchantDataService.getMerchantUrlInfo(workFlowTransBean.getWorkFlowBean()
                    .getPaytmMID(), UrlTypeId.RESPONSE.name(), workFlowTransBean.getSubscriptionServiceResponse()
                    .getWebsite());
            EXT_LOGGER.customInfo("Mapping response - MerchantUrlInfo :: {}", urlInfo);

            if (urlInfo != null) {
                merchantUrlInfo = JsonMapper.convertValue(urlInfo, MappingMerchantUrlInfo.class);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching merchant url info for merchantId : {}", workFlowTransBean
                    .getWorkFlowBean().getPaytmMID(), e);
        }

        if (merchantUrlInfo != null) {
            workFlowTransBean.getWorkFlowBean().getExtendInfo().setCallBackURL(merchantUrlInfo.getPostBackurl());
            workFlowTransBean.getWorkFlowBean().getExtendInfo().setPeonURL(merchantUrlInfo.getNotificationStatusUrl());
        }
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setSubsFreq(workFlowTransBean.getSubscriptionServiceResponse().getSubsFreq());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setSubsFreqUnit(workFlowTransBean.getSubscriptionServiceResponse().getSubsFreqUnit());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setMccCode(workFlowTransBean.getSubscriptionServiceResponse().getIndustryType());
        workFlowTransBean.getWorkFlowBean().setIndustryTypeID(
                workFlowTransBean.getSubscriptionServiceResponse().getIndustryType());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setCurrentSubscriptionId(workFlowTransBean.getSubscriptionServiceResponse().getSubscriptionId());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setUserEmail(workFlowTransBean.getSubscriptionServiceResponse().getUserEmail());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setUserMobile(workFlowTransBean.getSubscriptionServiceResponse().getUserMobile());
        workFlowTransBean.getWorkFlowBean().setCustID(workFlowTransBean.getSubscriptionServiceResponse().getCustId());
        if (StringUtils.isNotBlank(workFlowTransBean.getSubscriptionServiceResponse().getServiceId())) {
            workFlowTransBean.getWorkFlowBean().getExtendInfo()
                    .setSubsServiceId(workFlowTransBean.getSubscriptionServiceResponse().getServiceId());
        }
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setAutoRenewal(workFlowTransBean.getSubscriptionServiceResponse().isAutoRenewalStatus());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setAutoRetry(workFlowTransBean.getSubscriptionServiceResponse().isAutoRetryStatus());
        workFlowTransBean
                .getWorkFlowBean()
                .getExtendInfo()
                .setCommunicationManager(
                        workFlowTransBean.getSubscriptionServiceResponse().isCommunicationManagerStatus());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setGraceDays(workFlowTransBean.getSubscriptionServiceResponse().getGraceDays());
    }

    public boolean validateSavedCardForSubscription(boolean isFromAoaMerchant, CardBeanBiz cardBeanBiz,
            SubsPaymentMode paymentMode, SubscriptionRequestType subscriptionRequestType) {
        boolean isValidRequest = false;
        BinDetail binDetail = mappingUtility.getBinDetail(cardBeanBiz.getFirstSixDigit());
        String bin = String.valueOf(binDetail.getBin());
        if (isFromAoaMerchant && StringUtils.isNotBlank(bin)) {
            LOGGER.error("AOA subscription client call is being used");
            // AoaSubscriptionBinValidationResponse subsBinValidateResponse =
            // aoaSubscriptionService
            // .isBinSupported(new AoaSubscriptionBinValidationRequest(bin));
            // if (null != subsBinValidateResponse &&
            // subsBinValidateResponse.isBinBoundToSubscription()) {
            // isValidRequest = true;
            // } else {
            // LOGGER.error("Error occurred while validating bin from aoa subs");
            // }
        } else {
            isValidRequest = subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail, paymentMode,
                    subscriptionRequestType);
        }
        return isValidRequest;
    }

    public boolean validateSavedCardForMoto(CardBeanBiz cardBeanBiz, WorkFlowTransactionBean workFlowTransBean) {
        BinDetail binDetail = mappingUtility.getBinDetail(cardBeanBiz.getFirstSixDigit());
        if (null != binDetail) {
            switch (binDetail.getCardType()) {
            case "CREDIT_CARD":
                workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.CC.value);
                return PropertiesUtil.getInstance().isBinSupportedForCC(binDetail, SubscriptionRequestType.RENEW);
            case "DEBIT_CARD":
                workFlowTransBean.getWorkFlowBean().setPaymentTypeId(PaymentTypeIdEnum.DC.value);
                return PropertiesUtil.getInstance().isBinSupportedForDC(binDetail, SubscriptionRequestType.RENEW);
            default:
                break;
            }
        }
        return false;
    }

    public String getResponseForResponseConstant(ResponseConstants responseConstant) {
        String respMsg = responseConstant.getMessage();
        CommonResponseCode commonResponseCode = new CommonResponseCode();
        commonResponseCode.setSystemRespCode(responseConstant.getSystemResponseCode());
        ResponseCodeDetails responseCodeDetails = merchantResponseUtil.getMerchantRespCodeDetails(commonResponseCode);
        if (responseCodeDetails != null) {
            respMsg = StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                    .getDisplayMessage()
                    : StringUtils.isNotBlank(responseCodeDetails.getRemark()) ? responseCodeDetails.getRemark()
                            : responseConstant.getMessage();
        }
        return respMsg;
    }

    public void filterSavedCardsForSubscription(WorkFlowTransactionBean workFlowTransBean) {
        workFlowTransBean.getUserDetails().setMerchantViewSavedCardsList(
                filterSavedCardsForSubscription(workFlowTransBean.getWorkFlowBean().isFromAoaMerchant(),
                        workFlowTransBean.getUserDetails(), workFlowTransBean.getWorkFlowBean().getSubsPayMode()));
    }

    public List<CardBeanBiz> filterSavedCardsForSubscription(boolean isFromAoaMerchant, UserDetailsBiz userDetailsBiz,
            SubsPaymentMode subsPaymentMode) {
        List<CardBeanBiz> filteredCards = new ArrayList<>();
        if (userDetailsBiz.getMerchantViewSavedCardsList() != null) {
            for (CardBeanBiz cardBeanBiz : userDetailsBiz.getMerchantViewSavedCardsList()) {
                if (!"UPI".equals(cardBeanBiz.getCardType())
                        && validateSavedCardForSubscription(isFromAoaMerchant, cardBeanBiz, subsPaymentMode,
                                SubscriptionRequestType.CREATE)) {
                    filteredCards.add(cardBeanBiz);
                }
            }
        }
        return filteredCards;
    }

    /**
     * Method to filter savecards which are valid for subscription only
     *
     * @param workFlowTransBean
     */
    public void filterSavedCardsForSubscriptionNotLoggedIn(WorkFlowTransactionBean workFlowTransBean) {
        MidCustIdCardBizDetails midCustIdCardBizDetails = workFlowTransBean.getMidCustIdCardBizDetails();
        if (midCustIdCardBizDetails != null
                && !CollectionUtils.isEmpty(midCustIdCardBizDetails.getMerchantCustomerCardList())) {
            List<CardBeanBiz> filteredCards = new ArrayList<>();
            for (CardBeanBiz cardBeanBiz : midCustIdCardBizDetails.getMerchantCustomerCardList()) {
                if (!"UPI".equals(cardBeanBiz.getCardType())
                        && validateSavedCardForSubscription(workFlowTransBean.getWorkFlowBean().isFromAoaMerchant(),
                                cardBeanBiz, workFlowTransBean.getWorkFlowBean().getSubsPayMode(),
                                SubscriptionRequestType.CREATE)) {
                    filteredCards.add(cardBeanBiz);
                }
            }
            workFlowTransBean.getMidCustIdCardBizDetails().setMerchantCustomerCardList(filteredCards);
        }
    }

    /**
     * @param flowTransBean
     *            VPA
     */
    public GenericCoreResponseBean<UserProfileSarvatra> fetchUserProfileFromSarvatra(
            WorkFlowTransactionBean flowTransBean) {
        // fetch upi vpa from cache first
        String mid = flowTransBean.getWorkFlowBean().getPaytmMID();
        WorkFlowRequestBean input = flowTransBean.getWorkFlowBean();
        String userId = null != flowTransBean.getUserDetails() ? flowTransBean.getUserDetails().getUserId()
                : null != input.getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        if (ff4JUtils.featureEnabledOnMultipleKeys(mid, userId, Ff4jFeature.STORE_UPI_PROFILE_RESPONSE_IN_CACHE, false)) {
            UserProfileSarvatra userProfileSarvatra = (UserProfileSarvatra) theiaSessionRedisUtil
                    .get(BizConstant.USER_PROFILE_V2_CACHE_CONSTANT + userId);
            if (userProfileSarvatra != null) {
                return new GenericCoreResponseBean<UserProfileSarvatra>(userProfileSarvatra);
            }
        }
        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest();
        if (flowTransBean.getUserDetails() != null) {
            fetchUserPaytmVpaRequest.setUserId(flowTransBean.getUserDetails().getUserId());
            fetchUserPaytmVpaRequest.setUserToken(flowTransBean.getUserDetails().getUserToken());
            if (flowTransBean.getWorkFlowBean() != null) {
                fetchUserPaytmVpaRequest.setQueryParams(flowTransBean.getWorkFlowBean().getQueryParams());
            }
            fetchUserPaytmVpaRequest.setFetchLRNDetails(String.valueOf(flowTransBean.getWorkFlowBean().isUpiLite()));
        }
        GenericCoreResponseBean<UserProfileSarvatra> userProfileVpa = sarvatraVpaDetails
                .fetchUserProfileVpa(fetchUserPaytmVpaRequest);
        if (userProfileVpa != null
                && userProfileVpa.isSuccessfullyProcessed()
                && userProfileVpa.getResponse() != null
                && ff4JUtils.featureEnabledOnMultipleKeys(mid, userId, Ff4jFeature.STORE_UPI_PROFILE_RESPONSE_IN_CACHE,
                        false)) {
            theiaSessionRedisUtil.set(BizConstant.USER_PROFILE_V2_CACHE_CONSTANT + userId,
                    userProfileVpa.getResponse(), 300);
        }
        return userProfileVpa;
    }

    public PaymentStatus triggerCloseOrderPulses(WorkFlowTransactionBean workFlowTransBean) {
        int startTime = Integer.parseInt(BizConstant.PLATFORM_PLUS_CLOSE_ORDER_TIME);
        int endTime = Integer.parseInt(BizConstant.POST_RESPONSE_ORDER_TIMEOUT);
        // making calls for every 2 seconds

        EXT_LOGGER.customInfo("triggering closeOrder calls");
        for (int orderCloseTime = startTime; orderCloseTime <= endTime; orderCloseTime = orderCloseTime + 2) {
            final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrderResponse = closeOrder(workFlowTransBean);

            if (cancelOrderResponse != null && cancelOrderResponse.getResponse() != null) {

                String resultCode = cancelOrderResponse.getResponse().getBizResultInfo() != null ? cancelOrderResponse
                        .getResponse().getBizResultInfo().getResultCode() : "";

                if (cancelOrderResponse.getResponse().isSuccessfullyProcessed()) {
                    LOGGER.info("Order is successfully closed");
                    return PaymentStatus.FAIL;
                } else if (ResultCode.ORDER_IS_PAID.getCode().equals(resultCode)) {
                    LOGGER.info("Order is already paid");
                    return PaymentStatus.SUCCESS;
                } else if (ResultCode.ORDER_IS_CLOSED.getCode().equals(resultCode)) {
                    LOGGER.info("Order is already closed");
                    return PaymentStatus.FAIL;
                } else {
                    LOGGER.info("Other result code found : {}", resultCode);
                    return PaymentStatus.PROCESSING;
                }
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                LOGGER.error("Exception occurred , ", e);
            }
        }
        LOGGER.error("Unable to close order within the time frame");
        return PaymentStatus.PROCESSING;
    }

    public GenericCoreResponseBean<MidCustIdCardBizDetails> fetchSavedCards(WorkFlowTransactionBean workFlowTransBean) {
        long startTime = System.currentTimeMillis();
        MidCustIdCardBizDetails mIdCustIdCardDetails = new MidCustIdCardBizDetails();

        GenericCoreResponseBean<List<CardBeanBiz>> savedCards = null;
        WorkFlowRequestBean flowRequestBean = workFlowTransBean.getWorkFlowBean();

        try {
            if (!flowRequestBean.isStoreCardPrefEnabled()) {
                return new GenericCoreResponseBean<>("Saved card preference is not enabled on merchant");
            }

            if ((StringUtils.isNotBlank(flowRequestBean.getCustID()) && StringUtils.isNotBlank(flowRequestBean
                    .getPaytmMID())) || workFlowTransBean.getUserDetails() != null) {

                if (workFlowTransBean.getUserDetails() != null) {
                    savedCards = savedCardService.fetchSavedCardsByMidCustIdUserId(flowRequestBean.getPaytmMID(),
                            flowRequestBean.getCustID(), workFlowTransBean.getUserDetails().getUserId());
                } else {
                    savedCards = savedCardService.fetchSavedCardsByMidCustIdUserId(flowRequestBean.getPaytmMID(),
                            flowRequestBean.getCustID(), null);
                }
            }

            if (null != savedCards && savedCards.isSuccessfullyProcessed()
                    && BizParamValidator.validateInputListParam(savedCards.getResponse())) {
                mIdCustIdCardDetails.setMerchantCustomerCardList(savedCards.getResponse());
                mIdCustIdCardDetails.setCustId(flowRequestBean.getCustID());
                mIdCustIdCardDetails.setmId(flowRequestBean.getPaytmMID());
            }
            return new GenericCoreResponseBean<>(mIdCustIdCardDetails);
        } finally {
            LOGGER.info("Total time taken for fetching saved card details : {} ms", System.currentTimeMillis()
                    - startTime);
        }

    }

    public void filterSavedCardsUserNotLogged(WorkFlowTransactionBean flowTransBean) {
        MidCustIdCardBizDetails midCustIdCardBizDetails = flowTransBean.getMidCustIdCardBizDetails();
        List<CardBeanBiz> merchantSavedCardList = null;

        if (midCustIdCardBizDetails != null
                && !CollectionUtils.isEmpty(midCustIdCardBizDetails.getMerchantCustomerCardList())) {
            if (BizParamValidator.validateInputListParam(flowTransBean.getMerchantViewConsult().getPayMethodViews())) {
                List<PayMethodViewsBiz> merchantConsultViewList = flowTransBean.getMerchantViewConsult()
                        .getPayMethodViews();
                if (!CollectionUtils.isEmpty(merchantConsultViewList)) {
                    merchantSavedCardList = getMerchantCustomerSavedCardList(midCustIdCardBizDetails,
                            merchantConsultViewList, flowTransBean);
                }
            }
            midCustIdCardBizDetails.setMerchantCustomerCardList(merchantSavedCardList);
        }
    }

    private List<CardBeanBiz> getMerchantCustomerSavedCardList(MidCustIdCardBizDetails midCustIdCardDetails,
            List<PayMethodViewsBiz> merchantConsultViewList, WorkFlowTransactionBean flowTransBean) {
        return filterSavedCardsUserNotLogged(midCustIdCardDetails, merchantConsultViewList, flowTransBean);
    }

    public List<CardBeanBiz> filterSavedCardsUserNotLogged(MidCustIdCardBizDetails midCustIdCardDetails,
            List<PayMethodViewsBiz> consultViewList, WorkFlowTransactionBean flowTransBean) {
        List<CardBeanBiz> savedCardList = new ArrayList<>();
        List<CardBeanBiz> savedCardFromDB = midCustIdCardDetails.getMerchantCustomerCardList();

        Set<String> enabledPayMethods = new HashSet<>();
        Map<String, Set<String>> enableCCDCPayMethods = new HashMap<>();
        Map<String, Set<String>> channelsNotAvailableVsSupportedCountries = new HashMap<>();
        boolean returnDisabledCards = flowTransBean.getWorkFlowBean().isReturnDisabledChannelInFpo();
        for (PayMethodViewsBiz payMethod : consultViewList) {
            if (BizParamValidator.validateInputListParam(payMethod.getPayChannelOptionViews())) {
                for (PayChannelOptionViewBiz payChannelOption : payMethod.getPayChannelOptionViews()) {
                    if (payChannelOption.isEnableStatus()) {
                        enabledPayMethods.add(payMethod.getPayMethod());
                        if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                                || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                            Set<String> supportedCountries = new HashSet<>();
                            supportedCountries.addAll(payChannelOption.getSupportCountries());
                            enableCCDCPayMethods.put(payChannelOption.getPayOption(), supportedCountries);
                        }
                    } else {
                        if (returnDisabledCards
                                && TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE.equals(payChannelOption
                                        .getDisableReason())) {
                            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                                Set<String> supportedCountries = new HashSet<>();
                                supportedCountries.addAll(payChannelOption.getSupportCountries());
                                channelsNotAvailableVsSupportedCountries.put(payChannelOption.getPayOption(),
                                        supportedCountries);
                            }
                        }
                    }
                }
            }
        }

        BinDetail binDetail;
        for (CardBeanBiz cardData : savedCardFromDB) {
            try {
                binDetail = cardUtils.fetchBinDetails(cardData.getCardNumber());
                String cardKey = cardData.getCardType() + "_" + cardData.getCardScheme();
                if (!binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey) != null
                        && enableCCDCPayMethods.get(cardKey).contains("INTL")) {
                    savedCardList.add(cardData);
                } else if (binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey) != null) {
                    savedCardList.add(cardData);
                }
                if (returnDisabledCards && channelsNotAvailableVsSupportedCountries.get(cardKey) != null) {
                    if (!binDetail.getIsIndian() && enableCCDCPayMethods.get(cardKey).contains("INTL")) {
                        cardData.setDisabled(true);
                        cardData.setDisabledReason(TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE);
                        savedCardList.add(cardData);
                    } else if (binDetail.getIsIndian()) {
                        cardData.setDisabled(true);
                        cardData.setDisabledReason(TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE);
                        savedCardList.add(cardData);
                    }
                }
            } catch (PaytmValidationException e) {
                LOGGER.info("Not able to fetch bin for bin {} for International Card Payment",
                        cardData.getFirstSixDigit());
            }
        }

        return savedCardList;
    }

    @SuppressWarnings("unchecked")
    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> getAddAndPayLitePayviewConsultResponse() {
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> response = (GenericCoreResponseBean<LitePayviewConsultResponseBizBean>) theiaSessionRedisUtil
                .get(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE);
        if (response == null && !ff4JUtils.isFeatureEnabled(Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV, false)) {
            response = (GenericCoreResponseBean<LitePayviewConsultResponseBizBean>) theiaTransactionalRedisUtil
                    .get(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE);
        }
        return response;
    }

    public void setAddAndPayLitePayviewConsultResponseInCache(
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> addAndPayLitePayviewConsultResponse) {
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_EXPIREY,
                "43200"));
        theiaSessionRedisUtil.set(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE,
                addAndPayLitePayviewConsultResponse, expiry);
        if (!ff4JUtils.isFeatureEnabled(Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV, false)) {
            theiaTransactionalRedisUtil.set(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE,
                    addAndPayLitePayviewConsultResponse, expiry);
        }
    }

    @SuppressWarnings("unchecked")
    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> getLitePayViewConsultResponse(String mid) {
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> response = (GenericCoreResponseBean<LitePayviewConsultResponseBizBean>) theiaSessionRedisUtil
                .get(getLitePayviewConsultResponseInCacheKey(mid));
        if (response == null
                && !ff4JUtils.isFeatureEnabledOnMid(mid, Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV_MID_BASED, false)) {
            response = (GenericCoreResponseBean<LitePayviewConsultResponseBizBean>) theiaTransactionalRedisUtil
                    .get(getLitePayviewConsultResponseInCacheKey(mid));
        }
        return response;
    }

    public void setLitePayviewConsultResponseInCache(String mid,
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponse) {
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(
                BizConstant.LITEPAYVIEW_CONSULT_CACHE_EXPIREY_SECONDS, "1800"));
        theiaSessionRedisUtil.set(getLitePayviewConsultResponseInCacheKey(mid), litePayviewConsultResponse, expiry);
        if (!ff4JUtils.isFeatureEnabledOnMid(mid, Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV_MID_BASED, false)) {
            theiaTransactionalRedisUtil.set(getLitePayviewConsultResponseInCacheKey(mid), litePayviewConsultResponse,
                    expiry);
        }
    }

    public String getLitePayviewConsultResponseInCacheKey(String mid) {
        return BizConstant.LITEPAYVIEW_CONSULT_RESPONSE + mid;
    }

    public void setIfKYCRequired(final BizWalletConsultResponse bizWalletConsultResponse,
            WorkFlowTransactionBean workFlowTransactionBean) {

        if ((StringUtils.equals(BizConstant.BASIC_KYC, bizWalletConsultResponse.getWalletRbiType()) || StringUtils
                .equals(BizConstant.PRIMITIVE_KYC, bizWalletConsultResponse.getWalletRbiType()))
                && StringUtils.equals(bizWalletConsultResponse.getAddMoneyDestination(),
                        BizConstant.AddMoneyDestination.MAIN)) {
            workFlowTransactionBean.setOnTheFlyKYCRequired(true);
        }
    }

    public boolean checkUPIPushExpressEnabled(List<PayMethodViewsBiz> payMethods) {
        if (BizParamValidator.validateInputListParam(payMethods)) {
            for (PayMethodViewsBiz payMethod : payMethods) {
                if (!EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod())) {
                    continue;
                }
                List<PayChannelOptionViewBiz> payChannels = payMethod.getPayChannelOptionViews();
                for (PayChannelOptionViewBiz payChannel : payChannels) {
                    if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayOption())
                            && payChannel.isEnableStatus()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewPayMethodConsult(
            WorkFlowTransactionBean workFlowTransBean, boolean addAndPay) {
        final long startTime = System.currentTimeMillis();
        try {
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBean = null;

            litePayviewConsultResponseBean = fetchLPVResponseFromCache(workFlowTransBean, addAndPay);

            if (litePayviewConsultResponseBean != null) {
                LOGGER.info("Lite PayView fetched from cache");
                return litePayviewConsultResponseBean;
            }

            final LitePayviewConsultRequestBizBean litePayviewConsultRequestBean = workRequestCreator
                    .createLitePayviewConsultRequestBean(workFlowTransBean, addAndPay);

            litePayviewConsultResponseBean = bizPaymentService
                    .litePayviewPayMethodConsult(litePayviewConsultRequestBean);

            if (!litePayviewConsultResponseBean.isSuccessfullyProcessed()) {
                LOGGER.error("litePayviewConsult Failed due to reason : {} for mid {}", litePayviewConsultResponseBean
                        .getFailureMessage(), workFlowTransBean.getWorkFlowBean().getPaytmMID());
                return new GenericCoreResponseBean<>(litePayviewConsultResponseBean.getFailureMessage(),
                        litePayviewConsultResponseBean.getResponseConstant());
            }
            // Hack For Parllelization Flow to save litepayview
            if (addAndPay) {
                setAddAndPayLitePayviewConsultResponseInCache(litePayviewConsultResponseBean);
            } else {
                setLitePayviewConsultResponseInCache(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                        litePayviewConsultResponseBean);
            }
            if (!addAndPay) {
                litePayviewConsultResponseBean = filterPaymentModes(workFlowTransBean,
                        litePayviewConsultResponseBean.getResponse());
            }

            if (StringUtils.isNotBlank(litePayviewConsultRequestBean.getLpvProductCode())) {
                workFlowTransBean.setProductCode(litePayviewConsultRequestBean.getLpvProductCode());
            }

            LOGGER.info("litePayviewConsult is successfully processed for mid : {}", workFlowTransBean
                    .getWorkFlowBean().getPaytmMID());
            return litePayviewConsultResponseBean;
        } finally {
            LOGGER.info("Total time taken for litePayviewConsult api  is {} ms", System.currentTimeMillis() - startTime);
        }

    }

    private GenericCoreResponseBean<LitePayviewConsultResponseBizBean> fetchLPVResponseFromCache(
            WorkFlowTransactionBean workFlowTransBean, boolean addAndPay) {
        /**
         * not to call LPV in flipkart flow for api fpov2 with access token
         */
        try {
            WorkFlowRequestBean requestBean = workFlowTransBean.getWorkFlowBean();
            String userId = null != workFlowTransBean.getUserDetails() ? workFlowTransBean.getUserDetails().getUserId()
                    : null != requestBean.getUserDetailsBiz() ? requestBean.getUserDetailsBiz().getUserId() : null;
            if (StringUtils.isNotBlank(requestBean.getAccessToken())
                    && ff4JUtils.featureEnabledOnMultipleKeys(requestBean.getPaytmMID(), userId,
                            Ff4jFeature.BLACKLIST_LPV_FPOV2_WITH_ACCESS_TOKEN, false)) {
                if (addAndPay) {
                    return getAddAndPayLitePayviewConsultResponse();
                } else {
                    GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBean = getLitePayViewConsultResponse(requestBean
                            .getPaytmMID());
                    return filterPaymentModes(workFlowTransBean, litePayviewConsultResponseBean.getResponse());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to fetch LPV from cache");
        }
        return null;
    }

    public GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQuery(
            WorkFlowTransactionBean workFlowTransactionBean) {
        ChannelAccountQueryRequestBizBean channelAccountQueryRequestBizBean = workRequestCreator
                .createChannelAccountQueryRequest(workFlowTransactionBean);
        if (StringUtils.isBlank(channelAccountQueryRequestBizBean.getUserId())
                || CollectionUtils.isEmpty(channelAccountQueryRequestBizBean.getPayMethodInfos())) {
            return new GenericCoreResponseBean<>("Channel query Not required");
        }
        GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = bizPaymentService
                .channelAccountQuery(channelAccountQueryRequestBizBean);
        if (channelAccountQueryResponse.isSuccessfullyProcessed()
                && EPayMethod.LOYALTY_POINT.getMethod().equals(
                        workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId())) {
            LOGGER.info("Successful ChannelAccountQueryResponse on user {} , mid {}", workFlowTransactionBean
                    .getUserDetails().getInternalUserId(), workFlowTransactionBean.getWorkFlowBean().getAlipayMID());
            return channelAccountQueryResponse;
        }

        if (!channelAccountQueryResponse.isSuccessfullyProcessed()
                && PayMethod.LOYALTY_POINT.getMethod().equals(
                        channelAccountQueryRequestBizBean.getPayMethodInfos().get(0).getPayMethod())) {
            return new GenericCoreResponseBean<>(channelAccountQueryResponse.getFailureDescription());
        }
        if (channelAccountQueryResponse.isSuccessfullyProcessed()) {
            // map the info to consult Response
            mapChannelQueryToConsultResponse(workFlowTransactionBean, channelAccountQueryResponse.getResponse());
            return channelAccountQueryResponse;
        }

        // case where channel query fails
        disablePayMethodsInMerchantConsult(workFlowTransactionBean, channelAccountQueryRequestBizBean,
                channelAccountQueryResponse);
        return channelAccountQueryResponse;
    }

    public GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryForMgv(
            WorkFlowTransactionBean workFlowTransactionBean) {
        ChannelAccountQueryRequestBizBean channelAccountQueryRequestBizBean = workRequestCreator
                .createChannelAccountQueryRequestForMgv(workFlowTransactionBean);
        if (StringUtils.isBlank(channelAccountQueryRequestBizBean.getUserId())
                || CollectionUtils.isEmpty(channelAccountQueryRequestBizBean.getPayMethodInfos())) {
            return new GenericCoreResponseBean<>("Channel query Not required");
        }
        GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = bizPaymentService
                .channelAccountQuery(channelAccountQueryRequestBizBean);

        if (channelAccountQueryResponse.isSuccessfullyProcessed()) {
            // map the info to consult Response
            mapChannelQueryToConsultResponseForMgv(workFlowTransactionBean, channelAccountQueryResponse.getResponse());
            return channelAccountQueryResponse;
        }

        // case where channel query fails
        disablePayMethodsInMerchantConsult(workFlowTransactionBean, channelAccountQueryRequestBizBean,
                channelAccountQueryResponse);
        return channelAccountQueryResponse;
    }

    private void disablePayMethodsInMerchantConsult(WorkFlowTransactionBean workFlowTransactionBean,
            ChannelAccountQueryRequestBizBean channelAccountQueryRequestBizBean,
            GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse) {
        List<PayMethodViewsBiz> payMethodViewsBizList = workFlowTransactionBean.getMerchantViewConsult()
                .getPayMethodViews();
        for (PayMethodInfo payMethodInfo : channelAccountQueryRequestBizBean.getPayMethodInfos()) {
            for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {
                if (payMethodViewsBiz.getPayMethod().equals(payMethodInfo.getPayMethod())) {
                    payMethodViewsBiz.getPayChannelOptionViews().get(0).setEnableStatus(false);
                    payMethodViewsBiz.getPayChannelOptionViews().get(0)
                            .setDisableReason(channelAccountQueryResponse.getResponseConstant().getAlipayResultMsg());
                    break;
                }
            }

        }
    }

    private void mapChannelQueryToConsultResponse(WorkFlowTransactionBean workFlowTransactionBean,
            ChannelAccountQueryResponseBizBean response) {
        List<PayMethodViewsBiz> payMethodViewsBizList = workFlowTransactionBean.getMerchantViewConsult()
                .getPayMethodViews();
        List<ChannelAccountView> channelAccountViewsList = response.getChannelAccountViews();
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {
            if (EPayMethod.BALANCE.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                if (!checkForBalance(payMethodViewsBiz, channelAccountViewsList)) {
                    workFlowTransactionBean.getMerchantViewConsult().setWalletFailed(true);
                }
            }
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                checkForPaytmDigitalCredit(payMethodViewsBiz, channelAccountViewsList);

            }
            if (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                /*
                 * For Merchant Gift Voucher , user can buy multiple instances
                 * of a single template. We calculate total mgv balances for
                 * every templateId. Each templateId and its balance will be a
                 * payChannel for GIFT_VOUCHER paymode
                 */
                checkForMgvBalance(payMethodViewsBiz, channelAccountViewsList);

            }
        }
        // setting balance info in litePayView consult as well.
        workFlowTransactionBean.getMerchantLiteViewConsult().setPayMethodViews(payMethodViewsBizList);
    }

    private void mapChannelQueryToConsultResponseForMgv(WorkFlowTransactionBean workFlowTransactionBean,
            ChannelAccountQueryResponseBizBean response) {
        List<PayMethodViewsBiz> payMethodViewsBizList = workFlowTransactionBean.getMerchantViewConsult()
                .getPayMethodViews();
        List<ChannelAccountView> channelAccountViewsList = response.getChannelAccountViews();
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {

            if (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                /*
                 * For Merchant Gift Voucher , user can buy multiple instances
                 * of a single template. We calculate total mgv balances for
                 * every templateId. Each templateId and its balance will be a
                 * payChannel for GIFT_VOUCHER paymode
                 */
                checkForMgvBalance(payMethodViewsBiz, channelAccountViewsList);

            }
        }
        // setting balance info in litePayView consult as well.
        workFlowTransactionBean.getMerchantLiteViewConsult().setPayMethodViews(payMethodViewsBizList);
    }

    private void checkForPaytmDigitalCredit(PayMethodViewsBiz payMethodViewsBiz,
            List<ChannelAccountView> channelAccountViewsList) {
        PayChannelOptionViewBiz payChannelOptionViewBiz = payMethodViewsBiz.getPayChannelOptionViews().get(0);
        for (ChannelAccountView channelAccountView : channelAccountViewsList) {
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(channelAccountView.getPayMethod())) {
                if (!channelAccountView.isEnableStatus()) {
                    payChannelOptionViewBiz.setEnableStatus(false);
                    payChannelOptionViewBiz.setDisableReason(channelAccountView.getDisableReason());
                } else {
                    ChannelAccount channelAccount = channelAccountView.getChannelAccounts().get(0);
                    ExternalAccountInfoBiz externalAccountInfoBiz = new ExternalAccountInfoBiz();
                    externalAccountInfoBiz.setAccountBalance(channelAccount.getAvailableBalance());
                    externalAccountInfoBiz.setExternalAccountNo(channelAccount.getAccountNo());
                    try {
                        externalAccountInfoBiz
                                .setExtendInfo(JsonMapper.mapObjectToJson(channelAccount.getExtendInfo()));
                    } catch (FacadeCheckedException e) {
                        LOGGER.error("Exception in parsing extend info");
                    }
                    List<ExternalAccountInfoBiz> externalAccountInfoBizList = new ArrayList<>();
                    externalAccountInfoBizList.add(externalAccountInfoBiz);
                    payChannelOptionViewBiz.setExternalAccountInfos(externalAccountInfoBizList);
                }
                break;
            }
        }
    }

    /**
     * returns true if wallet is successfully processed
     *
     * @param payMethodViewsBiz
     * @param channelAccountViewsList
     * @return
     */

    private boolean checkForBalance(PayMethodViewsBiz payMethodViewsBiz,
            List<ChannelAccountView> channelAccountViewsList) {
        PayChannelOptionViewBiz payChannelOptionViewBiz = payMethodViewsBiz.getPayChannelOptionViews().get(0);

        for (ChannelAccountView channelAccountView : channelAccountViewsList) {
            if (EPayMethod.BALANCE.getMethod().equals(channelAccountView.getPayMethod())) {
                if (!channelAccountView.isEnableStatus()) {
                    payChannelOptionViewBiz.setDisableReason(channelAccountView.getDisableReason());
                    payChannelOptionViewBiz.setEnableStatus(false);
                    return false;
                } else {
                    ChannelAccount channelAccount = channelAccountView.getChannelAccounts().get(0);
                    BalanceChannelInfoBiz balanceChannelInfoBiz = new BalanceChannelInfoBiz();
                    balanceChannelInfoBiz.setPayerAccountNo(channelAccount.getAccountNo());
                    balanceChannelInfoBiz.setAccountBalance(channelAccount.getAvailableBalance());
                    List<BalanceChannelInfoBiz> balanceChannelInfoList = new ArrayList<>();
                    balanceChannelInfoList.add(balanceChannelInfoBiz);
                    payChannelOptionViewBiz.setBalanceChannelInfos(balanceChannelInfoList);
                    payChannelOptionViewBiz.setExtendInfo(channelAccount.getExtendInfo());
                    return true;
                }
            }
        }
        return false;
    }

    public void setAdditionParamsForNativeSubs(WorkFlowTransactionBean workFlowTransBean) {
        ExtendedInfoRequestBean extendedInfo = workFlowTransBean.getWorkFlowBean().getExtendInfo();
        SubscriptionResponse subscriptionResponse = workFlowTransBean.getSubscriptionServiceResponse();
        UserDetailsBiz userDetailsBiz = workFlowTransBean.getUserDetails();
        extendedInfo.setSubscriptionId(subscriptionResponse.getSubscriptionId());
        extendedInfo.setCurrentSubscriptionId(subscriptionResponse.getSubscriptionId());
        if (null != userDetailsBiz) {
            extendedInfo.setPayerUserID(userDetailsBiz.getUserId());
            extendedInfo.setPayerAccountNumber(userDetailsBiz.getPayerAccountNumber());
        }
    }

    public void createCardBeanSeamless(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowRequestBean workFlowRequestBean) {

        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        if (StringUtils.isNotEmpty(workFlowRequestBean.getSavedCardID())
                && StringUtils.isBlank(workFlowRequestBean.getCardIndexNo()) && !workFlowRequestBean.isCoftTokenTxn()) {
            cardBeanBiz.setCardId(Long.valueOf(workFlowRequestBean.getSavedCardID()));
        }
        cardBeanBiz.setCardNumber(workFlowRequestBean.getCardNo());
        cardBeanBiz.setCardType(workFlowRequestBean.getCardType());
        cardBeanBiz.setInstId(workFlowRequestBean.getInstId());
        cardBeanBiz.setCardScheme(workFlowRequestBean.getCardScheme());
        cardBeanBiz.setExpiryMonth(workFlowRequestBean.getExpiryMonth());
        cardBeanBiz.setExpiryYear(workFlowRequestBean.getExpiryYear());
        cardBeanBiz.setmId(workFlowRequestBean.getPaytmMID());
        cardBeanBiz.setCustId(workFlowRequestBean.getCustID());
        cardBeanBiz.setVpaAccNumber(workFlowRequestBean.getVirtualPaymentAddress());
        cardBeanBiz.setCardIndexNo(workFlowRequestBean.getCardIndexNo());
        cardBeanBiz.setCorporateCard(workFlowRequestBean.isCorporateCard());
        cardBeanBiz.setPrepaidCard(workFlowRequestBean.isPrepaidCard());
        cardBeanBiz.setIndian(!workFlowRequestBean.isInternationalCard());
        cardBeanBiz.setActive(workFlowRequestBean.isCardActive());
        if (workFlowRequestBean.isCoftTokenTxn()) {
            cardBeanBiz.setFirstSixDigit(0L);
        } else if (StringUtils.isNotBlank(cardBeanBiz.getCardNumber()) && cardBeanBiz.getCardNumber().length() > 6) {
            cardBeanBiz.setFirstSixDigit(Long.parseLong(cardBeanBiz.getCardNumber().substring(0, 6)));
        }
        workFlowTransBean.setSavedCard(cardBeanBiz);
    }

    public GenericCoreResponseBean<Boolean> fetchPostpaidBalanceResponse(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowResponseBean workFlowResponseBean) {
        WorkFlowRequestBean workFlowBean = workFlowTransBean.getWorkFlowBean();
        try {
            EXT_LOGGER.customInfo("Setting digital credit balance");

            fetchAndSetDigitalCreditBalance(workFlowBean, workFlowTransBean, workFlowResponseBean, true);

            LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = workFlowTransBean
                    .getMerchantLiteViewConsult();
            List<PayMethodViewsBiz> payMethodViewsBizs = litePayviewConsultResponseBizBean.getPayMethodViews();
            List<PayChannelOptionViewBiz> payChannelOptionViewBizs = null;
            for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {

                if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                    payChannelOptionViewBizs = payMethodViewsBiz.getPayChannelOptionViews();
                }
            }
            if (CollectionUtils.isNotEmpty(payChannelOptionViewBizs)) {
                List<ExternalAccountInfoBiz> externalAccountInfoBizs = payChannelOptionViewBizs.get(0)
                        .getExternalAccountInfos();
                if (!externalAccountInfoBizs.isEmpty())
                    return new GenericCoreResponseBean<>(Boolean.TRUE);
                else
                    LOGGER.error("Unable to fetch postpaid balance");
                return new GenericCoreResponseBean<>(Boolean.FALSE);
            }
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        } catch (Exception e) {
            LOGGER.warn("something unexcpected occured while check postpaid balance", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }

    }

    public GenericCoreResponseBean<Boolean> fetchPPBLBalanceResponse(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowResponseBean workFlowResponseBean) {
        WorkFlowRequestBean workFlowBean = workFlowTransBean.getWorkFlowBean();
        try {

            LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = workFlowTransBean
                    .getMerchantLiteViewConsult();
            List<PayMethodViewsBiz> payMethodViewsBizs = litePayviewConsultResponseBizBean.getPayMethodViews();
            List<PayChannelOptionViewBiz> payChannelOptionViewBizs = null;
            for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {

                if (EPayMethod.NET_BANKING.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                    for (PayChannelOptionViewBiz payChannel : payMethodViewsBiz.getPayChannelOptionViews()) {
                        if (payChannel.getPayOption().equals("NET_BANKING_PPBL")) {
                            UserDetailsBiz userDetails = workFlowTransBean.getUserDetails();
                            FetchAccountBalanceResponse accountBalanceResponse = null;
                            FetchAccountBalanceRequest fetchAccountBalanceRequest = new FetchAccountBalanceRequest();
                            fetchAccountBalanceRequest.setAccountType(TheiaConstant.RequestParams.SAVING_ACCOUNT);
                            fetchAccountBalanceRequest.setToken(userDetails.getUserToken());
                            if (ff4JUtils.isFeatureEnabledOnMid(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                                    THEIA_FALLBACK_PBBL_V4_ENABLED, false)) {
                                accountBalanceResponse = paymentsBankAccountQuery
                                        .queryAccountDetailsFromBankMiddlewareV4(fetchAccountBalanceRequest);
                            } else {
                                FetchPPBLUserBalanceRequest fetchPPBLUserBalanceRequest = new FetchPPBLUserBalanceRequest();
                                fetchPPBLUserBalanceRequest.setUserToken(userDetails.getUserToken());
                                fetchPPBLUserBalanceRequest.setIsFdBalanceRequired(true);
                                FetchPPBLUserBalanceResponse fetchPPBLUserBalanceResponse = paymentsBankAccountQuery
                                        .queryAccountDetailsFromBankV5(fetchPPBLUserBalanceRequest);
                                accountBalanceResponse = fetchUserAccountDetails(fetchPPBLUserBalanceResponse,
                                        workFlowBean.getPaytmMID());
                            }
                            workFlowResponseBean.setPpblAccountResponse(accountBalanceResponse);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("something unexcpected occured while checking ppbl balance", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

    public String updateAdditionalInfo(String additionalInfo, String field, String updatedValue) {

        LOGGER.info("received additionalInfo {}", additionalInfo);
        int indexOfField = additionalInfo.indexOf(field);
        if (indexOfField != -1) {
            int indexOfPipe = additionalInfo.indexOf("|", indexOfField);
            indexOfPipe = indexOfPipe == -1 ? additionalInfo.length() : indexOfPipe;
            additionalInfo = additionalInfo.substring(0, indexOfField) + field + updatedValue
                    + additionalInfo.substring(indexOfPipe);
            LOGGER.info("updated additionalInfo {} ", additionalInfo);
        } else {
            LOGGER.info("Field {} not found in additional Info", field);
        }
        return additionalInfo;
    }

    public GenericCoreResponseBean<Boolean> fetchWalletBalanceResponse(WorkFlowTransactionBean workFlowTransBean) {
        WorkFlowRequestBean workFlowBean = workFlowTransBean.getWorkFlowBean();
        List<PayMethodViewsBiz> payMethodViewsBizs = workFlowTransBean.getMerchantLiteViewConsult().getPayMethodViews();
        List<PayChannelOptionViewBiz> payChannelOptionViewBizs = null;
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {
            if (EPayMethod.BALANCE.getMethod().equals(payMethodViewsBiz.getPayMethod())
                    || EPayMethod.WALLET.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                payChannelOptionViewBizs = payMethodViewsBiz.getPayChannelOptionViews();
                break;
            }
        }
        // LOGGER.info("Setting wallet balance");
        EXT_LOGGER.customInfo("Setting wallet balance");

        if (CollectionUtils.isNotEmpty(payChannelOptionViewBizs) && workFlowTransBean.getUserDetails() != null) {
            fetchAndSetWalletBalance(BizConstant.STR_YES, BizConstant.STR_NO, true, true, workFlowTransBean,
                    payChannelOptionViewBizs, workFlowTransBean);
        }

        List<BalanceChannelInfoBiz> balanceChannelInfoBizs = null;
        if (CollectionUtils.isNotEmpty(payChannelOptionViewBizs)) {
            balanceChannelInfoBizs = payChannelOptionViewBizs.get(0).getBalanceChannelInfos();
        }
        if (CollectionUtils.isNotEmpty(balanceChannelInfoBizs))
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        else {
            LOGGER.error("Unable to fetch wallet balance");
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }

    }

    private void fetchAndSetWalletBalance(String isDetailInfo, String isClubSubwalletsRequired,
            boolean userWalletFreezeDetailsRequired, boolean userBlockDetailsRequired,
            WorkFlowTransactionBean workFlowTransactionBean, List<PayChannelOptionViewBiz> payChannelOptionViewBizs,
            WorkFlowTransactionBean workFlowTransBean) {
        WalletBalanceRequest request = getWalletBalanceRequest(isDetailInfo, isClubSubwalletsRequired,
                userWalletFreezeDetailsRequired, userBlockDetailsRequired, workFlowTransactionBean);
        WalletBalanceResponse response = bizPaymentService.fetchWalletBalance(request, workFlowTransactionBean
                .getUserDetails().getUserId());
        setFetchWalletBalanceResponse(payChannelOptionViewBizs, response, workFlowTransactionBean.getUserDetails(),
                workFlowTransBean);
        setWalletAccountStatus(response.getResponse(), workFlowTransactionBean);
    }

    private void setFetchWalletBalanceResponse(List<PayChannelOptionViewBiz> payChannelOptionViewBizs,
            WalletBalanceResponse walletBalanceResponse, UserDetailsBiz userDetailsBiz,
            WorkFlowTransactionBean workFlowTransBean) {
        PayChannelOptionViewBiz payChannelOptionViewBiz = payChannelOptionViewBizs.get(0);
        if (walletBalanceResponse.getResponse() == null
                || BizConstant.FAILURE.equalsIgnoreCase(walletBalanceResponse.getStatus())) {
            payChannelOptionViewBiz.setEnableStatus(false);
            payChannelOptionViewBiz.setDisableReason(walletBalanceResponse.getStatusMessage());
        } else {
            List<BalanceChannelInfoBiz> balanceChannelInfoBizs = new ArrayList<>();
            BalanceChannelInfoBiz balanceChannelInfoBiz = new BalanceChannelInfoBiz();
            balanceChannelInfoBizs.add(balanceChannelInfoBiz);
            WalletBalanceResponseData walletBalanceResponseData = walletBalanceResponse.getResponse();
            Map<String, String> extendInfo = getExtendInfoForWalletBalanceResponse(walletBalanceResponseData);
            if (!extendInfo.isEmpty()) {
                // converting balance in paise to keep it consistent
                // with alipay response
                DecimalFormat decimalFormat = new DecimalFormat("0.#");
                String balanceInPaise = decimalFormat.format(walletBalanceResponseData.getTotalBalance() * 100);
                balanceChannelInfoBiz.setAccountBalance(balanceInPaise);
                balanceChannelInfoBiz.setPayerAccountNo(userDetailsBiz.getPayerAccountNumber());
                populateTwoFADetails(balanceChannelInfoBiz, walletBalanceResponseData, workFlowTransBean);
                payChannelOptionViewBiz.setExtendInfo(extendInfo);
                payChannelOptionViewBiz.setBalanceChannelInfos(balanceChannelInfoBizs);
            }
        }
    }

    private Map<String, String> getExtendInfoForWalletBalanceResponse(
            WalletBalanceResponseData walletBalanceResponseData) {
        Map<String, String> extendInfo = new HashMap<>();
        extendInfo.put(BizConstant.OTHER_SUB_WALLET_BALANCE,
                String.valueOf(walletBalanceResponseData.getOtherSubWalletBalance()));
        extendInfo.put(BizConstant.WALLET_GRADE, walletBalanceResponseData.getWalletGrade());
        extendInfo.put(BizConstant.OWNER_GUID, walletBalanceResponseData.getOwnerGuid());
        extendInfo.put(BizConstant.TOTAL_BALANCE, String.valueOf(walletBalanceResponseData.getTotalBalance()));
        extendInfo.put(BizConstant.PAYTM_WALLET_BALANCE,
                String.valueOf(walletBalanceResponseData.getPaytmWalletBalance()));
        extendInfo.put(BizConstant.SSO_ID, walletBalanceResponseData.getSsoId());
        try {
            String subWalletDetailsList = JsonMapper.mapObjectToJson(walletBalanceResponseData
                    .getSubWalletDetailsList());
            extendInfo.put("subWalletDetailsList", subWalletDetailsList);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception in converting subwalletlist to json {} ", ExceptionUtils.getStackTrace(e));
        }
        return extendInfo;
    }

    private WalletBalanceRequest getWalletBalanceRequest(String isDetailInfo, String isClubSubwalletsRequired,
            boolean userWalletFreezeDetailsRequired, boolean userBlockDetailsRequired,
            WorkFlowTransactionBean workFlowTransactionBean) {
        WalletBalanceRequest walletBalanceRequest = new WalletBalanceRequest();
        WalletBalanceRequestData request = new WalletBalanceRequestData();
        request.setIsDetailInfo(isDetailInfo);
        request.setIsClubSubwalletsRequired(isClubSubwalletsRequired);
        request.setMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        request.setUserWalletFreezeDetailsRequired(userWalletFreezeDetailsRequired);
        request.setUserBlockDetailsRequired(userBlockDetailsRequired);
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
        Map<String, String> queryParams = getQueryParams(workFlowTransactionBean.getWorkFlowBean().getQueryParams());
        if (MapUtils.isNotEmpty(queryParams)) {
            request.setPlatform(queryParams.get(CLIENT_PARAM));
            request.setAppVersion(queryParams.get(FacadeConstants.CoftAPIConstant.VERSION));
        }
        if (isWebTxn(workFlowTransactionBean.getWorkFlowBean())) {
            request.setPlatform("web");
        }
        TwoFADetails twoFADetails = workFlowTransactionBean.getWorkFlowBean().getTwoFADetails();
        if (twoFADetails != null) {
            request.setIs2faDetailsRequired(BooleanUtils.isTrue(twoFADetails.isTwoFARequired()) ? STR_YES : STR_NO);
            request.setTxnType(twoFADetails.getTxnType());
        }
        walletBalanceRequest.setRequest(request);
        return walletBalanceRequest;
    }

    private String getErrorMsgToReturnPaytmValidationExp(PaytmValidationException pve) {
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

    public void updatePostpaidStatusAndCCEnabledFlag(String token, boolean isPostpaidOnboardingSupported,
            String clientId, String clientSecret, UserDetailsBiz userDetails) {
        try {
            loginService.updatePostpaidStatusAndCCEnabledFlag(token, isPostpaidOnboardingSupported, clientId,
                    clientSecret, userDetails);
            LOGGER.info("Successfully updated user postpaid status");
        } catch (final Exception e) {
            LOGGER.error("Unable to get data from FetchUserTypeAttribute API : ", e);
        }
    }

    public Map<String, Map<String, String>> fetchUserTypeAttributesDetails(String token, String userType,
            String clientId, String clientSecret) throws FacadeCheckedException {
        return loginService.fetchUserTypeAttributesDetails(token, userType, clientId, clientSecret);

    }

    public EMIDetailList fetchEmiDetailsList(EmiDetailRequest emiDetailRequest) throws Exception {
        EMIDetailList emiDetailList;
        if (ff4JUtils.isFeatureEnabledOnMid(emiDetailRequest.getMid(), FETCH_EMI_DETAILS_FROM_MERCHANT_CENTER, false)) {
            EXT_LOGGER.customInfo("calling merchant center-service for fetching EMI details");
            emiDetailList = merchantCenterService.getEMIDetailsByMid(emiDetailRequest.getMid());
        } else {
            EXT_LOGGER.customInfo("calling mapping-service for fetching EMI details");
            emiDetailList = emiDetailsClient.getEMIByMid(emiDetailRequest);
        }
        LOGGER.info("EMIDetailList :: {}", emiDetailList);
        return emiDetailList;

    }

    public GenericCoreResponseBean<ConsultFeeResponse> consultBulkFeeResponseForPay(
            WorkFlowTransactionBean workFlowTransBean, EPayMethod payMethod) {
        List<String> instId = new ArrayList<String>();
        ConsultFeeRequest consultFeeRequest = new ConsultFeeRequest();
        WorkFlowRequestBean flowRequestBean = workFlowTransBean.getWorkFlowBean();
        if (payMethod == null) {
            payMethod = EPayMethod.getPayMethodByMethod(flowRequestBean.getPayMethod());
        }
        LOGGER.info("Calling PCF Api for payment payMethod");
        if ((!workFlowTransBean.isAddMoneyPcfEnabled())
                && EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())) {
            payMethod = EPayMethod.BALANCE;
        } else {
            if (EPayMethod.DEBIT_CARD.equals(payMethod) || EPayMethod.CREDIT_CARD.equals(payMethod)) {
                instId.add(flowRequestBean.getCardScheme());
                if (flowRequestBean.getFeeRateFactors() == null) {
                    flowRequestBean.setFeeRateFactors(new FeeRateFactors());
                }
                flowRequestBean.getFeeRateFactors().setInstId(flowRequestBean.getCardScheme());

            } else if (EPayMethod.EMI.equals(payMethod) || (EPayMethod.NET_BANKING.equals(payMethod))) {
                instId.add(flowRequestBean.getBankCode());
                if (flowRequestBean.getFeeRateFactors() == null) {
                    flowRequestBean.setFeeRateFactors(new FeeRateFactors());
                }
                flowRequestBean.getFeeRateFactors().setInstId(flowRequestBean.getBankCode());
            }
        }
        List<EPayMethod> payMethods = new ArrayList<EPayMethod>();
        payMethods.add(payMethod);
        final BigDecimal txnAmount;
        if (flowRequestBean.isQRIdFlowOnly() && StringUtils.isNotBlank(flowRequestBean.getQrTxnAmount())) {
            txnAmount = new BigDecimal(flowRequestBean.getQrTxnAmount());
        } else {
            txnAmount = new BigDecimal(flowRequestBean.getTxnAmount());
        }
        consultFeeRequest.setMerchantId(flowRequestBean.getAlipayMID());
        consultFeeRequest.setTransactionAmount(txnAmount);
        consultFeeRequest.setTransactionType(flowRequestBean.getRequestType());
        consultFeeRequest.setPayMethods(payMethods);
        consultFeeRequest.setInstId(instId);
        consultFeeRequest.setSlabBasedMDR(flowRequestBean.isSlabBasedMDR());
        consultFeeRequest.setDynamicFeeMerchant(flowRequestBean.isDynamicFeeMerchant());
        consultFeeRequest.setFeeRateFactors(Arrays.asList(flowRequestBean.getFeeRateFactors()));

        if (flowRequestBean.isDynamicFeeMerchant()) {
            consultFeeRequest.setProductCode(getProductCodeForDynamicChargePayment(flowRequestBean.getProductCode()));
        }
        workFlowTransBean.getWorkFlowBean().setRoute(Routes.PG2);
        consultFeeRequest.setRoute(Routes.PG2);
        consultFeeRequest.setMerchantId(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        prepareConsultFeeRequest(workFlowTransBean, consultFeeRequest, payMethod);
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = bizPaymentService
                .consultBulkConvenienceFees(consultFeeRequest);
        LOGGER.debug("Consult Fee Response Is : {}", consultFeeResponse);

        if (!consultFeeResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Consult Fee Failed due to : {}", consultFeeResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(consultFeeResponse.getFailureMessage(),
                    consultFeeResponse.getResponseConstant());
        }
        return new GenericCoreResponseBean<>(consultFeeResponse.getResponse());
    }

    private void prepareConsultFeeRequest(WorkFlowTransactionBean workFlowTransBean,
            ConsultFeeRequest consultFeeRequest, EPayMethod payMethod) {
        if (workFlowTransBean.isAddMoneyPcfEnabled() && workFlowTransBean.getWorkFlowBean().isNativeAddMoney()) {
            consultFeeRequest.setTxnType(REQUEST_ADD_MONEY);
            consultFeeRequest.setFeeRateCode(workFlowTransBean.getFeeRateCode());
            consultFeeRequest.setAddMoneyPcfEnabled(true);
        } else if (workFlowTransBean.isAddMoneyPcfEnabled()) {
            WorkFlowRequestBean flowRequestBean = workFlowTransBean.getWorkFlowBean();
            List<EPayMethod> payMethods = new ArrayList<>();
            String walletAmount = flowRequestBean.getWalletAmount();
            Double walletAmountToDeduct = StringUtils.isNotBlank(walletAmount) ? Double.valueOf(walletAmount) : 0;
            Double addMoneyAmount = Double.valueOf(flowRequestBean.getTxnAmount()) - walletAmountToDeduct;
            if (BooleanUtils.isTrue(flowRequestBean.getAddOneRupee()) && addMoneyAmount < 100D) {
                addMoneyAmount = 100D;
            }
            payMethods.add(EPayMethod.BALANCE);
            payMethods.add(payMethod);
            consultFeeRequest.setPayMethods(payMethods);
            consultFeeRequest.setAddMoneyPcfEnabled(true);
            consultFeeRequest.setAddMoneyAmount(new BigDecimal(addMoneyAmount));
            consultFeeRequest.setFeeRateCode(workFlowTransBean.getFeeRateCode());
            consultFeeRequest.setAddNPayProductCode(workFlowTransBean.getWorkFlowBean().getExtendInfo()
                    .getProductCode());
        }
    }

    public com.paytm.pgplus.facade.enums.ProductCodes getProductCodeForDynamicChargePayment(String productCode) {
        List<String> productCodeList = FeeHelper.getProductCodesForDynamicChargeSupport();
        if (CollectionUtils.isNotEmpty(productCodeList) && productCodeList.contains(productCode)) {
            try {
                return com.paytm.pgplus.facade.enums.ProductCodes.getProductById(productCode);
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Could not set product code because : ", e);
            }
        }
        return null;

    }

    public Map<String, String> getExtendInfoFromExtendBean(WorkFlowRequestBean flowRequestBean) {

        Map<String, String> extendInfo = new HashMap<String, String>();
        ExtendedInfoRequestBean extendInfoBean = flowRequestBean.getExtendInfo();

        extendInfo.put(BizConstant.ExtendedInfoKeys.TOTAL_TXN_AMOUNT, extendInfoBean.getTotalTxnAmount());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_NAME, flowRequestBean.getBankName());
        // extendInfo.put(BizConstant.ExtendedInfoKeys.RETRY_COUNT, "0");
        extendInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_NAME, extendInfoBean.getMerchantName());
        extendInfo.put(BizConstant.ExtendedInfoKeys.MCC_CODE, extendInfoBean.getMccCode());
        extendInfo.put(BizConstant.ExtendedInfoKeys.PRODUCT_CODE, extendInfoBean.getProductCode());
        extendInfo.put(BizConstant.ExtendedInfoKeys.EMI_PLAN_ID, extendInfoBean.getEmiPlanId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.TOPUP_AND_PAY, "false");
        extendInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_TRANS_ID, extendInfoBean.getMerchantTransId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.PAYTM_MERCHANT_ID, extendInfoBean.getPaytmMerchantId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ISSUING_BANK_ID, flowRequestBean.getInstId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.ALIPAY_MERCHANT_ID, extendInfoBean.getAlipayMerchantId());
        extendInfo.put(BizConstant.ExtendedInfoKeys.TXNTYPE, "ONLY_PG");
        extendInfo.put(BizConstant.ExtendedInfoKeys.IS_ENHANCED_NATIVE,
                Boolean.valueOf(extendInfoBean.isEnhancedNative()).toString());
        if (PaymentTypeIdEnum.UPI.value.equals(flowRequestBean.getPaymentTypeId())) {
            extendInfo.put(BizConstant.VIRTUAL_PAYMENT_ADDRESS, flowRequestBean.getVirtualPaymentAddress());
        }

        return extendInfo;

    }

    public boolean checkSubscriptionPaymentModeEligibility(WorkFlowRequestBean workFlowRequestBean) {

        boolean isValidRequest = false;

        switch (workFlowRequestBean.getSubsPayMode().name()) { // this value not
                                                               // be UNKNOWN
        case "DC":
        case "CC":
            try {
                BinDetail binDetail;
                if (workFlowRequestBean.isTxnFromCardIndexNo()) {
                    binDetail = workFlowRequestBean.getBinDetail();

                } else if (workFlowRequestBean.isCoftTokenTxn()) {
                    String cardNo = workFlowRequestBean.getAccountRangeCardBin();
                    binDetail = cardUtils.fetchBinDetails(cardNo);

                } else {

                    String cardNo = workFlowRequestBean.getCardNo();
                    binDetail = cardUtils.fetchBinDetails(cardNo);
                }
                if (workFlowRequestBean.isFromAoaMerchant()) {
                    LOGGER.error("AOA subscription client call is being used");
                    // AoaSubscriptionBinValidationResponse
                    // subsBinValidateResponse = aoaSubscriptionService
                    // .isBinSupported(new
                    // AoaSubscriptionBinValidationRequest(String.valueOf(binDetail.getBin())));
                    // if (null != subsBinValidateResponse) {
                    // LOGGER.debug("Response received for aoa subs bin validation {}",
                    // subsBinValidateResponse);
                    // if
                    // ("SUCCESS".equalsIgnoreCase(subsBinValidateResponse.getResultInfo().getStatus()))
                    // {
                    // isValidRequest =
                    // subsBinValidateResponse.isBinBoundToSubscription();
                    // }
                    // } else {
                    // LOGGER.error("Error occured while validating bin from aoa subs");
                    // }
                } else {
                    isValidRequest = subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail,
                            workFlowRequestBean.getSubsPayMode(), SubscriptionRequestType.CREATE);
                }
            } catch (Exception e) {
                LOGGER.error("Error occurs in validateRequestForNativeSubscription method due to {}:", e.getMessage());
            }
            break;
        case "PPI":
            isValidRequest = true;
            break;
        case "PPBL":
            isValidRequest = true;
            break;
        case "NORMAL":
            isValidRequest = true;
            break;
        case "UPI":
            isValidRequest = true;
            break;
        }

        return isValidRequest;
    }

    public void setAdditionParamsForMandate(WorkFlowTransactionBean workFlowTransBean,
            SubscriptionResponse subscriptionServiceResponse) {
        if (subscriptionServiceResponse.getMandateInfo() != null) {
            workFlowTransBean.getWorkFlowBean().setAccountNumber(
                    subscriptionServiceResponse.getMandateInfo().getIssuerBankAccNo());
            workFlowTransBean.getWorkFlowBean().setBankName(
                    subscriptionServiceResponse.getMandateInfo().getIssuerBankName());
            workFlowTransBean.getWorkFlowBean().setIfsc(
                    subscriptionServiceResponse.getMandateInfo().getIssuerBankIfsc());
            workFlowTransBean.getWorkFlowBean().setHolderName(
                    subscriptionServiceResponse.getMandateInfo().getAccountHolderName());
            workFlowTransBean.getWorkFlowBean().setUmrn(subscriptionServiceResponse.getMandateInfo().getUmrnNo());
            workFlowTransBean.getWorkFlowBean().setBankCode(
                    subscriptionServiceResponse.getMandateInfo().getIssuerBankCode());
        }
    }

    public void setAdditionParamsForUPIRecurringMandate(WorkFlowTransactionBean workFlowTransBean,
            SubscriptionResponse subscriptionServiceResponse) {
        if (subscriptionServiceResponse.getSubscriptionUpiInfo() != null) {
            workFlowTransBean.getWorkFlowBean().setVpa(subscriptionServiceResponse.getSubscriptionUpiInfo().getVpa());
            workFlowTransBean.getWorkFlowBean().setUmn(subscriptionServiceResponse.getSubscriptionUpiInfo().getUmn());
            workFlowTransBean.getWorkFlowBean().setPayOption(
                    subscriptionServiceResponse.getSubscriptionUpiInfo().getPayOption());
            workFlowTransBean.getWorkFlowBean().setRefServiceInstId(
                    subscriptionServiceResponse.getSubscriptionUpiInfo().getRefServiceInstId());
            cacheUPIRecurringInfo(workFlowTransBean.getWorkFlowBean(), subscriptionServiceResponse);
        }
    }

    public EPayMode allowedPayModeForNoTxnAmt(WorkFlowTransactionBean workFlowTransactionBean) {

        return pgPlusWalletDecisionMakerService.allowedPayMode(workFlowTransactionBean);
    }

    public GenericCoreResponseBean<Boolean> validateUserForAdvanceDeposit(String userId, String bid, String custID) {
        long startTime = System.currentTimeMillis();
        Boolean validation = false;
        KybDataServiceRequest kybDataServiceRequest = new KybDataServiceRequest(userId, bid, custID);
        try {
            validation = kybDataService.validateUser(kybDataServiceRequest);
            return new GenericCoreResponseBean<>(validation);
        } catch (Exception e) {
            LOGGER.error("Unable to validate user via KYB data service due to ", e);
            return new GenericCoreResponseBean<Boolean>(validation);
        } finally {
            LOGGER.info("Total time taken for validating user details : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private GenericCoreResponseBean<CacheCardResponseBean> verifyCardHash(WorkFlowTransactionBean flowTransactionBean,
            CacheCardRequestBean cacheCardRequestBean, String CIN) {
        String mid = flowTransactionBean.getWorkFlowBean().getPaytmMID();
        if (ff4JUtils.isFeatureEnabledOnMid(mid, DISABLE_VERIFY_CARD_HASH, false)) {
            return null;
        } else if (ff4JUtils.isFeatureEnabledOnMid(mid, VERIFY_CARD_HASH_ON_TIN, false)) {
            return verifyCardHashForTIN(flowTransactionBean);
        } else {
            return verifyCardHashForCIN(flowTransactionBean, cacheCardRequestBean, CIN);
        }
    }

    private GenericCoreResponseBean<CacheCardResponseBean> verifyCardHashForTIN(
            WorkFlowTransactionBean flowTransactionBean) {
        String alreadyGeneratedCardHash = checkCardHashInInitTxnRequest(flowTransactionBean.getWorkFlowBean()
                .getTxnToken());
        String merchantCoftConfig = flowTransactionBean.getWorkFlowBean().getMerchantCoftConfig();

        String cardHash = null;
        if (merchantCoftConfig.equals("GCIN")) {
            cardHash = flowTransactionBean.getWorkFlowBean().getGcin();
        } else if (merchantCoftConfig.equals("PAR")) {
            cardHash = flowTransactionBean.getWorkFlowBean().getPar();
        }

        if (!StringUtils.equals(alreadyGeneratedCardHash, cardHash)) {
            LOGGER.error("CardHash does not match, failing txn!, EXPECTED_CARDHASH {}, ACTUAL_CARDHASH {}", cardHash,
                    alreadyGeneratedCardHash);
            return new GenericCoreResponseBean<>(ResponseConstants.INVALID_CARD_NO.getMessage(),
                    ResponseConstants.INVALID_CARD_NO);
        }
        return null;
    }

    private GenericCoreResponseBean<CacheCardResponseBean> verifyCardHashForCIN(
            WorkFlowTransactionBean flowTransactionBean, CacheCardRequestBean cacheCardRequestBean, String CIN) {
        try {
            String alreadyGeneratedCardHash = null;
            boolean isSendCINAnd8BinHashToPromoEnabled = ff4JUtil.isFeatureEnabledForPromo(flowTransactionBean
                    .getWorkFlowBean().getPaytmMID());
            String cardHash = null;
            if (isSendCINAnd8BinHashToPromoEnabled) {
                // card number or CIN or save card id can come in /v1/ptc
                alreadyGeneratedCardHash = checkCardHashInInitTxnRequest(flowTransactionBean.getWorkFlowBean()
                        .getTxnToken());
                if (StringUtils.isNotEmpty(alreadyGeneratedCardHash) && alreadyGeneratedCardHash.length() > 15
                        && alreadyGeneratedCardHash.length() < 45) {
                    LOGGER.info("Verifying cardHash for TIN flow");
                    String savedCardID = flowTransactionBean.getWorkFlowBean().getSavedCardID();
                    if (StringUtils.equals(alreadyGeneratedCardHash, savedCardID)) {
                        return null;
                    }
                    LOGGER.error(
                            "TIN :: CardHash does not match, failing txn!, EXPECTED_CARDHASH {}, ACTUAL_CARDHASH {}",
                            savedCardID, alreadyGeneratedCardHash);
                    return new GenericCoreResponseBean<>(ResponseConstants.INVALID_CARD_NO.getMessage(),
                            ResponseConstants.INVALID_CARD_NO);
                }
                cardHash = CIN;
            } else {
                // card number / save card id can come in /v1/ptc
                alreadyGeneratedCardHash = flowTransactionBean.getWorkFlowBean().getCardHash();
                if (StringUtils.isBlank(cacheCardRequestBean.getCardNo())) {
                    return null;
                }
                cardHash = SignatureUtilWrapper.signApiRequest(cacheCardRequestBean.getCardNo());
            }
            if (StringUtils.isNotBlank(alreadyGeneratedCardHash)) {
                String savedCardID = flowTransactionBean.getWorkFlowBean().getSavedCardID();
                boolean isCardIndexNumberExist = null != savedCardID ? savedCardID.length() > 15 ? true : false : false;
                String cardNumber = cacheCardRequestBean.getCardNo();
                /**
                 * card number or save card Id checking with both as in save
                 * card mig rollout some machines can give card number hash and
                 * some can give CIN in fpo . after migration it will fall in
                 * else part
                 */
                if (!isCardIndexNumberExist
                        && (StringUtils.equals(SignatureUtilWrapper.signApiRequest(cardNumber),
                                alreadyGeneratedCardHash) || StringUtils.equals(CIN, alreadyGeneratedCardHash))) {
                    return null;
                } else if (StringUtils.equals(CIN, alreadyGeneratedCardHash)) {
                    // txn is via CIN
                    return null;
                }
                LOGGER.error("CardHash does not match, failing txn!, EXPECTED_CARDHASH {}, ACTUAL_CARDHASH {}",
                        cardHash, alreadyGeneratedCardHash);
                return new GenericCoreResponseBean<>(ResponseConstants.INVALID_CARD_NO.getMessage(),
                        ResponseConstants.INVALID_CARD_NO);
            }
            return null;
        } catch (IOException | GeneralSecurityException ex) {
            LOGGER.error("Error in generating cardHash {}", ex);
            return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        }
    }

    public GenericCoreResponseBean<QRCodeDetailsResponse> createDynamicQR(WorkFlowTransactionBean input) {
        String merchantVPA = getMerchantVPA(input.getWorkFlowBean());
        LOGGER.info("Merchant VPA found is {}", merchantVPA);
        boolean isUPISupported = isUPIPayModeSupported(input);
        boolean isUPIDisabled = isUPIDisabledForQR(input);

        input.getWorkFlowBean().setEnhancedDynamicUPIQRCodeAllowed(
                isUPISupported && !isUPIDisabled
                        && merchantVPA.contains(TheiaConstant.ExtraConstants.PAYTM_UPI_HANDLER));
        /**
         * UPI not supported in Merchant Pay Options OR UPI not allowed as
         * PayMode in Initiate Transaction
         */
        if (BooleanUtils.isFalse(isUPISupported)
                || BooleanUtils.isFalse(isUPIPayModeAndPayChannelSupportedInOrderDetail(input))) {
            return new GenericCoreResponseBean<>("UPI PayMode not allowed, UPI QR is not supported");
        }

        /**
         * User Logged In But UPI QR is not Supported
         */
        if (!ERequestType.DYNAMIC_QR_2FA.equals(input.getWorkFlowBean().getRequestType())
                && input.getWorkFlowBean().getToken() != null
                && !input.getWorkFlowBean().isEnhancedDynamicUPIQRCodeAllowed()) {
            LOGGER.info("User is loggedIn, But UPI QR is not supported");
            return new GenericCoreResponseBean<>("User LoggedIn and UPI QR is Not Supported");
        }

        GenericCoreResponseBean<QRCodeDetailsResponse> qrCodeDetailsResponse = walletQRCodeService
                .fetchQRCodeDetails(input);
        LOGGER.info("QRCodeDetailsResponse : {}", qrCodeDetailsResponse);
        return qrCodeDetailsResponse;
    }

    public boolean isUPIPayModeAndPayChannelSupportedInOrderDetail(WorkFlowTransactionBean input) {
        String payMethod = EPayMethod.UPI.getMethod();
        String orderDetailStr = null;
        InitiateTransactionRequestBody orderDetail = null;
        if (StringUtils.isNotBlank(input.getWorkFlowBean().getOrderDetails())) {
            orderDetailStr = input.getWorkFlowBean().getOrderDetails();
            try {
                orderDetail = JsonMapper.mapJsonToObject(orderDetailStr, InitiateTransactionRequestBody.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception occurred while parsing OrderDetail String", e);
            }
        }
        boolean pushEnabledOnMerchant = false;
        boolean pushExpressEnabledOnMerchant = false;
        if (null != input.getMerchantViewConsult()
                && !CollectionUtils.isEmpty(input.getMerchantViewConsult().getPayMethodViews())) {
            pushEnabledOnMerchant = containsPayChannelInPaymethod(input.getMerchantViewConsult().getPayMethodViews(),
                    TheiaConstant.BasicPayOption.UPI_PUSH);
            pushExpressEnabledOnMerchant = containsPayChannelInPaymethod(input.getMerchantViewConsult()
                    .getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
        }

        if (orderDetail != null) {
            return (pushEnabledOnMerchant && isChannelEnabledOnTxn(payMethod, orderDetail, PAYCHANNEL_UPI_PUSH))
                    && (pushExpressEnabledOnMerchant && isChannelEnabledOnTxn(payMethod, orderDetail,
                            PAYCHANNEL_UPI_PUSH_EXPRESS));
        }

        return pushEnabledOnMerchant && pushExpressEnabledOnMerchant;
    }

    public boolean isChannelEnabledOnTxn(String payMethod, InitiateTransactionRequestBody orderDetail, String payChannel) {
        return (CollectionUtils.isEmpty(orderDetail.getEnablePaymentMode()) || (isPaymentModePresentInList(
                orderDetail.getEnablePaymentMode(), payMethod) && (!isPayChannelListNotEmpty(
                orderDetail.getEnablePaymentMode(), payMethod) || isPayChannelPresentInList(
                orderDetail.getEnablePaymentMode(), payChannel))))
                && (CollectionUtils.isEmpty(orderDetail.getDisablePaymentMode()) || (!isPaymentModePresentInList(
                        orderDetail.getDisablePaymentMode(), payMethod) || (isPayChannelListNotEmpty(
                        orderDetail.getDisablePaymentMode(), payMethod) && !isPayChannelPresentInList(
                        orderDetail.getDisablePaymentMode(), payChannel))));
    }

    public boolean isPayChannelListNotEmpty(List<PaymentMode> paymentModesList, String payMethod) {
        boolean isPayChannelPresent = false;
        for (PaymentMode payMode : paymentModesList) {
            if (payMethod != null && payMethod.equals(payMode.getMode())) {
                if (CollectionUtils.isNotEmpty(payMode.getChannels())) {
                    isPayChannelPresent = true;
                    break;
                }
            }
        }
        return isPayChannelPresent;
    }

    public boolean isPaymentModePresentInList(List<PaymentMode> paymentModes, String payMethod) {
        return paymentModes != null
                && paymentModes.stream().anyMatch(payMode -> payMethod != null && payMethod.equals(payMode.getMode()));
    }

    public boolean isPayChannelPresentInList(List<PaymentMode> paymentModes, String paymentChannel) {
        if (paymentModes != null) {
            for (PaymentMode payMode : paymentModes) {
                if (EPayMethod.UPI.getMethod().equals(payMode.getMode())) {
                    return payMode.getChannels() != null
                            && payMode
                                    .getChannels()
                                    .stream()
                                    .anyMatch(payChannel -> paymentChannel != null && paymentChannel.equals(payChannel));
                }
            }
        }
        return false;
    }

    private boolean isUPIDisabledForQR(WorkFlowTransactionBean input) {
        List<String> allowedPaymodes = Optional.ofNullable(input).map(WorkFlowTransactionBean::getWorkFlowBean)
                .map(WorkFlowRequestBean::getAllowedPaymentModes).orElse(null);

        boolean upiDisabled = CollectionUtils.isNotEmpty(allowedPaymodes)
                && allowedPaymodes.stream().noneMatch(payMode -> EPayMethod.UPI.getMethod().equals(payMode));

        // not used private httpServletRequest() method here because it gives
        // NPE
        if (input != null && input.getWorkFlowBean() != null && input.getWorkFlowBean().getPaymentRequestBean() != null
                && input.getWorkFlowBean().getPaymentRequestBean().getRequest() != null
                && input.getWorkFlowBean().getPaymentRequestBean().getRequest().getAttribute(SHOW_UPI_QR) != null) {
            upiDisabled = !(boolean) input.getWorkFlowBean().getPaymentRequestBean().getRequest()
                    .getAttribute(SHOW_UPI_QR);
        }

        return upiDisabled;
    }

    private boolean isUPIPayModeSupported(WorkFlowTransactionBean workFlowTransactionBean) {
        if (null != workFlowTransactionBean.getMerchantViewConsult()
                && !CollectionUtils.isEmpty(workFlowTransactionBean.getMerchantViewConsult().getPayMethodViews())) {
            boolean pushEnabled = containsPayChannelInPaymethod(workFlowTransactionBean.getMerchantViewConsult()
                    .getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH);
            boolean pushExpressEnabled = containsPayChannelInPaymethod(workFlowTransactionBean.getMerchantViewConsult()
                    .getPayMethodViews(), TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
            LOGGER.info("PushEnabled {}, PushExpressEnabled {}", pushEnabled, pushExpressEnabled);
            return pushEnabled || pushExpressEnabled;
        }
        return false;
    }

    private String getMerchantVPA(WorkFlowRequestBean input) {
        Long paymentId, authModeId;
        try {
            LookupDataDetails lookupInfoData = lookupDataInfoService.getLookupInfoData("UPI", "PAYMENT_MODE");
            EXT_LOGGER.customInfo("Mapping response - LookupDataDetails for Payment mode :: {}", lookupInfoData);
            paymentId = lookupInfoData.getId();

            lookupInfoData = lookupDataInfoService.getLookupInfoData("USRPWD", "AUTH_MODE");
            EXT_LOGGER.customInfo("Mapping response - LookupDataDetails for Auth mode :: {}", lookupInfoData);
            authModeId = lookupInfoData.getId();

            BankMasterDetails bankMasterDetails = bankInfoDataService.getBankInfoDataByBankCode("PPBL");
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetails for PPBL :: {}", bankMasterDetails);

            MBIdConfiguration mbidConfig = merchantDataService.getMBID(input.getPaytmMID(),
                    bankMasterDetails.getBankId(), paymentId, authModeId);
            EXT_LOGGER.customInfo("Mapping response - MBIdConfiguration :: {}", mbidConfig);

            String params = mbidConfig.getParameter();
            LOGGER.info("Params For Merchant VPA {} and status {} ", params, mbidConfig.getStatus());

            if (StringUtils.isNotBlank(params) && mbidConfig.getStatus()) {
                Map<String, String> paramMap = getParamMap(params);
                input.setMerchantVPA(paramMap.get("MERCHANT_VPA"));
                input.setMcc(paramMap.get("MCC"));
                return input.getMerchantVPA();
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Error Occured while getting data from mapping-service for Merchant VPA ");
        } catch (Exception e) {
            LOGGER.error("Unknown Exception occured while getting Merchant VPA ", e);
        }
        LOGGER.info("Could not find merchant VPA from database");
        return StringUtils.EMPTY;
    }

    private Map<String, String> getParamMap(String params) {
        Map<String, String> map = new HashMap<String, String>();
        String[] keyValueArray = params.split(";");
        for (int i = 0; i < keyValueArray.length; i++) {

            int index = keyValueArray[i].indexOf("=");

            if (index > 0) {
                map.put(keyValueArray[i].substring(0, index),
                        keyValueArray[i].substring(index + 1, keyValueArray[i].length()));
            }
        }
        return map;
    }

    public boolean isAllowedOnMidAndCustId(WorkFlowRequestBean inputBean) {
        return AllowedMidCustidPropertyUtil.isMidCustIdEligible(inputBean.getPaytmMID(), inputBean.getCustID(),
                TheiaConstant.ExtraConstants.ENHANCED_DYNAMICQR_ALLOWED_CUSTID_LIST_KEY,
                TheiaConstant.ExtraConstants.ALL, Boolean.FALSE);
    }

    private boolean checkForceDirectChannel(String forceDirectChannel) {
        if ((StringUtils.isNotBlank(forceDirectChannel)) && StringUtils.equals("forceDirect", forceDirectChannel)) {
            return true;
        }

        return false;
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

    public GenericCoreResponseBean<Boolean> checkForRiskConsultForAddMoney(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean responseBean) {

        String addMoneyAmount = StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean()
                .getAmountForWalletConsultInRisk()) ? workFlowTransactionBean.getWorkFlowBean()
                .getAmountForWalletConsultInRisk() : (StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean()
                .getTxnAmount()) ? workFlowTransactionBean.getWorkFlowBean().getTxnAmount()
                : DUMMY_TXN_AMOUNT_FOR_WALLET_CONSULT);

        String custId = (workFlowTransactionBean.getUserDetails() != null) ? workFlowTransactionBean.getUserDetails()
                .getUserId() : null;

        if (isMidAllowedChargeFeeOnAddMoneyByWallet(workFlowTransactionBean.getWorkFlowBean().getPaytmMID())) {
            LOGGER.info("<new addMoney fee integration>");
            return getRiskResponseForAddMoneyFromWallet(flowRequestBean, workFlowTransactionBean, addMoneyAmount,
                    custId, responseBean);
        } else if (!flowRequestBean.isGvFlag()) {
            LOGGER.info("<old addMoney fee integration>");
            return getRiskResponseForAddMoney(workFlowTransactionBean, responseBean, addMoneyAmount);
        }
        return new GenericCoreResponseBean<>(Boolean.FALSE);
    }

    private boolean isEnhancedCashierPageRequest(WorkFlowRequestBean flowRequestBean) {
        return ((flowRequestBean != null) && flowRequestBean.isEnhancedCashierPageRequest());
    }

    private GenericCoreResponseBean<Boolean> getRiskResponseForAddMoneyFromWallet(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, String addMoneyAmount, String custId,
            WorkFlowResponseBean responseBean) {

        if (!isEnhancedCashierPageRequest(flowRequestBean)
                && !(flowRequestBean != null && flowRequestBean.isNativeAddMoney())
                && workFlowTransactionBean.getWorkFlowBean().isAddMoneyFeeAppliedOnWallet()) {
            LOGGER.info("Not hitting wallet limits for getting fee details, isAddMoneyFeeAppliedOnWallet=true");
            return new GenericCoreResponseBean<>(Boolean.FALSE);

        } else if ((flowRequestBean != null)
                && (!flowRequestBean.isGvFlag() || isEnhancedCashierPageRequest(flowRequestBean) || flowRequestBean
                        .isNativeAddMoney())) {

            LOGGER.info("hitting wallet limits for getting fee details");
            /*
             * Finds addMoneyRequest source and sends the same to wallet
             */
            String addMoneySource = StringUtils.EMPTY;
            if (StringUtils.isNotBlank(flowRequestBean.getPaytmMID())
                    && ff4JUtil.isFeatureEnabled(ENABLE_ADD_MONEY_SOURCE_IN_CONSULT, flowRequestBean.getPaytmMID())) {
                if (flowRequestBean.isNativeAddMoney()) {
                    addMoneySource = THIRD_PARTY.getValue();
                } else if (isEnhancedCashierPageRequest(flowRequestBean)) {
                    addMoneySource = PAYTM_WEBSITE.getValue();
                } else {
                    addMoneySource = PAYTM_APP.getValue();
                }
            }
            GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse = consultWalletService(custId,
                    addMoneyAmount, workFlowTransactionBean.getWorkFlowBean().getOrderID(), addMoneySource);

            if (!walletConsultResponse.isSuccessfullyProcessed() || walletConsultResponse.getResponse() == null) {
                LOGGER.error("walletConsultResponse is not processed correctly or is null");
                return new GenericCoreResponseBean<>(Boolean.FALSE);
            }

            if ("GIFT_VOUCHER".equalsIgnoreCase(walletConsultResponse.getResponse().getAddMoneyDestination())) {
                LOGGER.info("Add Money destination is Gift Voucher, returning null as risk consult is not required ");
                return new GenericCoreResponseBean<>(Boolean.FALSE);
            }

            List<Map<String, Object>> feeDetails = walletConsultResponse.getResponse().getFeeDetails();
            if (CollectionUtils.isEmpty(feeDetails)) {
                LOGGER.info("feeDetails is null/empty, returning");
                return new GenericCoreResponseBean<>(Boolean.FALSE);
            }

            List<RiskConvenienceFee> riskConvenienceFeeList = new LinkedList<>();

            for (Map<String, Object> feeItem : feeDetails) {
                if (feeItem != null && !feeItem.isEmpty()) {

                    RiskConvenienceFee riskConvenienceFee = new RiskConvenienceFee();
                    riskConvenienceFee.setPayMethod(PayMethod.getPayMethodByMethod((String) feeItem.get("payMethod")));
                    riskConvenienceFee.setFeePercent((String) feeItem.get("feePercent"));
                    riskConvenienceFee.setReason((String) feeItem.get("msg"));
                    riskConvenienceFee.setFeeDetails(feeItem);

                    riskConvenienceFeeList.add(riskConvenienceFee);
                }
            }
            if (CollectionUtils.isNotEmpty(riskConvenienceFeeList)) {
                responseBean.setRiskConvenienceFee(riskConvenienceFeeList);
                LOGGER.info("Risk Convenience Fee set in response {} ", riskConvenienceFeeList);
            }
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        } else {
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
    }

    private GenericCoreResponseBean<Boolean> getRiskResponseForAddMoney(
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean responseBean, String addMoneyAmount) {
        RiskResponse riskResponse = null;
        try {
            riskResponse = riskPolicyConsultResponse(workFlowTransactionBean.getWorkFlowBean().getEnvInfoReqBean(),
                    workFlowTransactionBean.getUserDetails(), addMoneyAmount, workFlowTransactionBean.getWorkFlowBean()
                            .getOrderID());
            if (riskResponse == null) {
                return new GenericCoreResponseBean<>(Boolean.FALSE);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to communicate with RISK_POLICY_CONSULT API", e);
            return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception occured while processing json request for RISK_POLICY_CONSULT ", e);
            return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        }
        if ("S".equalsIgnoreCase(riskResponse.getBody().getResultInfo().getResultStatus())) {
            boolean riskApplied = validateRiskResponse(riskResponse);
            if (riskApplied) {
                List<RiskConvenienceFee> riskConvenienceFeeList = PayModeRiskConsultFeeUtil
                        .fetchRiskConvenienceFeeDetails();
                if (CollectionUtils.isNotEmpty(riskConvenienceFeeList)) {
                    responseBean.setRiskConvenienceFee(riskConvenienceFeeList);
                    LOGGER.info("Risk Convenience Fee set in response {} ", riskConvenienceFeeList);
                }

            }
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        }
        return new GenericCoreResponseBean<>(Boolean.FALSE);

    }

    public RiskResponse riskPolicyConsultResponse(EnvInfoRequestBean envInfoRequestBean, UserDetailsBiz userDetails,
            String txnAmount, String orderId) throws FacadeCheckedException, JsonProcessingException {
        RiskResponse riskResponse = null;
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.RISK_POLICY_CONSULT);
        if (userDetails != null) {
            String trustFactor = null;
            GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse = consultWalletService(
                    userDetails.getUserId(), txnAmount, orderId, null);
            if (walletConsultResponse.isSuccessfullyProcessed() && walletConsultResponse.getResponse() != null) {
                if ("GIFT_VOUCHER".equalsIgnoreCase(walletConsultResponse.getResponse().getAddMoneyDestination())
                        || "TRANSIT_BLOCKED_WALLET".equalsIgnoreCase(walletConsultResponse.getResponse()
                                .getAddMoneyDestination())) {
                    LOGGER.info("Add Money destination is Gift Voucher/Transit Block Wallet, returning null as risk consult is not required ");
                    return null;
                } else {
                    trustFactor = walletConsultResponse.getResponse().getTrustFactor();
                }
            }
            RiskRequestBody riskRequestBody = buildRiskRequestBodyForUserRiskConsult(envInfoRequestBean, userDetails,
                    trustFactor, txnAmount);
            RiskRequest riskRequest = new RiskRequest(head, riskRequestBody);
            riskResponse = riskImpl.riskPolicyConsult(riskRequest);
            LOGGER.info("risk consult response {}", riskResponse);
        }

        return riskResponse;

    }

    private RiskRequestBody buildRiskRequestBodyForUserRiskConsult(EnvInfoRequestBean envInfoRequestBean,
            UserDetailsBiz userDetails, String trustFactor, String txnAmount) throws FacadeInvalidParameterException,
            JsonProcessingException {

        Date date = new Date();

        EnvInfo envInfo = buildEnvInfoForRiskRequest(envInfoRequestBean);

        Map<String, String> extendInfoMap = new HashMap<>();

        extendInfoMap.put(EVENT_AMOUNT, txnAmount);

        extendInfoMap.put(TRUST_FACTOR, trustFactor);

        MerchantExtendedInfoResponse merchantExtendedInfoResponse = userMappingService
                .getUserMerchantInfoResponse(userDetails.getUserId());

        if (merchantExtendedInfoResponse != null) {
            if (merchantExtendedInfoResponse.getExtendedInfo().getIsMerchant() == 1) {
                extendInfoMap.put(USER_MID, merchantExtendedInfoResponse.getExtendedInfo().getAlipayMid());
            }
            extendInfoMap.put(IS_MERCHANT_BLOCKED,
                    String.valueOf(merchantExtendedInfoResponse.getExtendedInfo().isBlocked()));
            extendInfoMap.put(MERCHANT_LIMIT, merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit()
                    .toString());
        }

        String extendInfo = "";
        extendInfo = mapper.writeValueAsString(extendInfoMap);

        return new RiskRequestBody(date, userDetails.getInternalUserId(), BizSceneEnum.NAMELIST_QUERY, envInfo,
                extendInfo);
    }

    private EnvInfo buildEnvInfoForRiskRequest(EnvInfoRequestBean envInfoRequestBean)
            throws FacadeInvalidParameterException {
        EnvInfo envInfo;
        envInfo = new EnvInfo.EnvInfoBuilder(envInfoRequestBean.getClientIp(), envInfoRequestBean.getTerminalType()
                .getTerminal()).clientKey(envInfoRequestBean.getClientKey()).tokenId(envInfoRequestBean.getTokenId())
                .sessionId(envInfoRequestBean.getSessionId()).sdkVersion(envInfoRequestBean.getSdkVersion())
                .appVersion(envInfoRequestBean.getAppVersion())
                .websiteLanguage(envInfoRequestBean.getWebsiteLanguage()).osType(envInfoRequestBean.getOsType())
                .orderOsType(envInfoRequestBean.getOrderOsType())
                .orderTerminalType(envInfoRequestBean.getOrderTerminalType())
                .merchantAppVersion(envInfoRequestBean.getAppVersion()).extendInfo(envInfoRequestBean.getExtendInfo())
                .build();
        return envInfo;
    }

    public GenericCoreResponseBean<BizWalletConsultResponse> consultWalletService(final String userId,
            final String txnAmount, final String orderId, final String addMoneySource) {
        GenericCoreResponseBean<BizWalletConsultResponse> walletResponse = consultAddMoneyV2(userId, orderId,
                txnAmount, addMoneySource);
        return walletResponse;
    }

    private boolean validateRiskResponse(RiskResponse riskResponse) {
        if (riskResponse.getBody() != null && RiskResultEnum.REJECT == riskResponse.getBody().getRiskResult()) {
            LOGGER.info("Risk applied on the provided field");
            return true;
        }
        return false;
    }

    public String calculateChargeAmountInPaise(ConsultDetails consultDetails) {
        String chargeAmount = null;
        if ((consultDetails != null) && (consultDetails.getTotalTransactionAmount() != null)
                && (consultDetails.getBaseTransactionAmount() != null)) {
            chargeAmount = String.valueOf(consultDetails.getTotalTransactionAmount().subtract(
                    consultDetails.getBaseTransactionAmount()));

            chargeAmount = AmountUtils.getTransactionAmountInPaise(chargeAmount);
            LOGGER.info("Charge amount calculated for ScanNPay is::{}", chargeAmount);
        }
        return chargeAmount;
    }

    public GenericCoreResponseBean<Boolean> fetchAndSetMgvBalance(WorkFlowTransactionBean workFlowTransBean) {

        if (isPayMethodConfiguredOnMerchant(workFlowTransBean.getMerchantLiteViewConsult(), EPayMethod.GIFT_VOUCHER)) {
            LOGGER.info("MGV configured on merchant ,fetching mgv balance");
            GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = null;
            channelAccountQueryResponse = channelAccountQueryForMgv(workFlowTransBean);
            if (channelAccountQueryResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(Boolean.TRUE);
            }
        }
        return new GenericCoreResponseBean<>(Boolean.FALSE);
    }

    public void checkForMgvBalance(PayMethodViewsBiz payMethodViewsBiz, List<ChannelAccountView> channelAccountViewsList) {
        {
            PayChannelOptionViewBiz firstPayChannelOptionViewBiz = payMethodViewsBiz.getPayChannelOptionViews().get(0);
            List<PayChannelOptionViewBiz> payChannelOptionViewBizList = payMethodViewsBiz.getPayChannelOptionViews();
            payChannelOptionViewBizList.clear();
            Map<String, PayChannelOptionViewBiz> templateIdBalanceMap = new HashMap<>();

            ChannelAccountView mgvChannelAccountView = null;

            for (ChannelAccountView channelAccountView : channelAccountViewsList) {
                if (EPayMethod.GIFT_VOUCHER.getMethod().equals(channelAccountView.getPayMethod())) {
                    mgvChannelAccountView = channelAccountView;
                    break;
                }
            }
            if (mgvChannelAccountView == null)
                return;
            else if (!mgvChannelAccountView.isEnableStatus()) {
                firstPayChannelOptionViewBiz.setDisableReason(mgvChannelAccountView.getDisableReason());
                firstPayChannelOptionViewBiz.setEnableStatus(false);
                return;
            }

            if (mgvChannelAccountView.getNoInternalAssetAvailable() != null) {
                payMethodViewsBiz.setNewUser(mgvChannelAccountView.getNoInternalAssetAvailable());
            }

            for (ChannelAccount channelAccount : mgvChannelAccountView.getChannelAccounts()) {
                if (templateIdBalanceMap.get(channelAccount.getTemplateId()) == null) {
                    /*
                     * Creating PaychannelOptionView and setting in map for
                     * templateId
                     */
                    PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();
                    payChannelOptionViewBiz.setPayOption(firstPayChannelOptionViewBiz.getPayOption());
                    payChannelOptionViewBiz.setEnableStatus(firstPayChannelOptionViewBiz.isEnableStatus());
                    payChannelOptionViewBiz.setExtendInfo(firstPayChannelOptionViewBiz.getExtendInfo());
                    payChannelOptionViewBiz.setTemplateId(channelAccount.getTemplateId());

                    List<BalanceChannelInfoBiz> list = new ArrayList<>();
                    BalanceChannelInfoBiz balanceChannelInfoBiz = new BalanceChannelInfoBiz();
                    balanceChannelInfoBiz.setAccountBalance(channelAccount.getAvailableBalance());
                    balanceChannelInfoBiz.setPayerAccountNo(channelAccount.getAccountNo());
                    list.add(balanceChannelInfoBiz);
                    payChannelOptionViewBiz.setBalanceChannelInfos(list);
                    payChannelOptionViewBizList.add(payChannelOptionViewBiz);
                    templateIdBalanceMap.put(channelAccount.getTemplateId(), payChannelOptionViewBiz);
                } else {
                    /*
                     * Fetching from map and Updating balance to sum balances
                     * for multiple vouchers into a single templateId
                     */
                    PayChannelOptionViewBiz payChannelOptionViewBiz = templateIdBalanceMap.get(channelAccount
                            .getTemplateId());
                    Double balance1 = Double.parseDouble(channelAccount.getAvailableBalance());
                    Double balance2 = Double.parseDouble(payChannelOptionViewBiz.getBalanceChannelInfos().get(0)
                            .getAccountBalance());
                    String totalBalance = String.valueOf((balance1 + balance2));
                    payChannelOptionViewBiz.getBalanceChannelInfos().get(0).setAccountBalance(totalBalance);
                    templateIdBalanceMap.put(channelAccount.getTemplateId(), payChannelOptionViewBiz);
                }
            }

        }
    }

    public void setLinkPaymentsUserDetails(WorkFlowTransactionBean workFlowTransBean) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .getMerchantUniqueReference())
                && (workFlowTransBean.getWorkFlowBean().getExtendInfo().getMerchantUniqueReference().startsWith("LI_") || workFlowTransBean
                        .getWorkFlowBean().getExtendInfo().getMerchantUniqueReference().startsWith("INV_"))) {
            UserDetailsBiz userDetailsBiz = workFlowTransBean.getUserDetails();
            if (userDetailsBiz != null) {
                workFlowTransBean.getWorkFlowBean().getExtendInfo().setUserEmail(userDetailsBiz.getEmail());
                workFlowTransBean.getWorkFlowBean().getExtendInfo().setUserMobile(userDetailsBiz.getMobileNo());
                workFlowTransBean.getWorkFlowBean().getExtendInfo().setPhoneNo(userDetailsBiz.getMobileNo());
                workFlowTransBean.getWorkFlowBean().getExtendInfo().setEmail(userDetailsBiz.getEmail());
            }
            LOGGER.info("Link based params added here: {}", workFlowTransBean);

            workFlowTransBean.getWorkFlowBean().getExtendInfo()
                    .setLinkNotes(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getLinkNotes());

            /* For Payment Forms , populate extended Info with form details */
            if (StringUtils.isNotEmpty(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getPaymentFormId())) {
                LOGGER.info("Setting payment form in ExtendedInfo for link based payments");
                PaymentFormDetails paymentFormDetails = null;
                if (workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null) {
                    paymentFormDetails = workFlowTransBean.getWorkFlowBean().getPaymentRequestBean()
                            .getLinkDetailsData().getPaymentFormDetails();
                } else {
                    paymentFormDetails = (PaymentFormDetails) theiaSessionRedisUtil.get(workFlowTransBean
                            .getWorkFlowBean().getPaymentRequestBean().getPaymentFormId());
                }
                if (paymentFormDetails != null) {
                    try {
                        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                                .setPaymentForms(JsonMapper.mapObjectToJson(paymentFormDetails.getPaymentForm()));
                    } catch (FacadeCheckedException e) {
                        LOGGER.error("IOException occurred for setting paymentFormDetails in extendInfo", e);
                    }

                    /*
                     * For Skip Login Flow , populate user details from User
                     * filled form details
                     */
                    if (paymentFormDetails.getSkipLoginEnabled() != null && paymentFormDetails.getSkipLoginEnabled()
                            && userDetailsBiz == null) {
                        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                                .setUserEmail(paymentFormDetails.getEmailId());
                        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                                .setUserMobile(paymentFormDetails.getMobileNo());
                        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                                .setPhoneNo(paymentFormDetails.getMobileNo());
                        workFlowTransBean.getWorkFlowBean().getExtendInfo().setEmail(paymentFormDetails.getEmailId());
                        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                                .setCustomerName(paymentFormDetails.getCustomerName());
                    }
                }
            }
        }
    }

    public boolean isSavedVpaCheckOverride(String requestAppVersion) {
        if (StringUtils.isEmpty(requestAppVersion)) {
            return false;
        }

        if (requestAppVersion.matches("Android_[0-9]*\\.[0-9]*\\.[0-9]*")
                || requestAppVersion.matches("iOS_[0-9]*\\.[0-9]*\\.[0-9]*")) {

            String androidAppVersionForSavedVpas = ConfigurationUtil.getProperty(
                    NATIVE_REQUEST_SAVED_VPAS_APP_VERSION_ANDROID, "10.0.0");
            String iosAppVersionForSavedVpas = ConfigurationUtil.getProperty(NATIVE_REQUEST_SAVED_VPAS_APP_VERSION_IOS,
                    "10.0.0");
            /*
             * appVersion comes as iOS_1.2.3 ; Android_5.6.7
             */
            String configAppVersion;
            if (requestAppVersion.contains("iOS")) {
                configAppVersion = iosAppVersionForSavedVpas;
            } else {
                configAppVersion = androidAppVersionForSavedVpas;
            }

            String requestVersion = requestAppVersion.split("_")[1];
            if (configAppVersion.compareToIgnoreCase(requestVersion) == 0) { // If
                                                                             // both
                                                                             // versions
                                                                             // are
                                                                             // same
                LOGGER.info("Returning savedVpaCheckOverride flag as true");
                return true;
            } else {
                Scanner reqScanner = new Scanner(requestVersion);
                Scanner configScanner = new Scanner(configAppVersion);
                reqScanner.useDelimiter("\\.");
                configScanner.useDelimiter("\\.");

                while (reqScanner.hasNextInt() && configScanner.hasNextInt()) {
                    int v1 = reqScanner.nextInt();
                    int v2 = configScanner.nextInt();
                    if (v1 < v2) {
                        return false;
                    } else if (v1 > v2) {
                        LOGGER.info("Returning savedVpaCheckOverride flag as true");
                        return true;
                    }
                }

            }
        } else {
            LOGGER.error("AppVersion entered is incorrect : {} , setting savedVpaCheckOverride flag = false",
                    requestAppVersion);
            return false;
        }
        return false; // default value false
    }

    /**
     * fetch banks based on mode from redis if not present there fetch them from
     * mapping service.
     *
     * @param mandateMode
     * @return
     * @throws MappingServiceClientException
     */
    public List<BankMasterDetails> getMandateBanks(MandateMode mandateMode) throws MappingServiceClientException {
        List<BankMasterDetails> bankMasterDetailsList = null;
        if (null != mandateMode) {
            if (MandateMode.E_MANDATE == mandateMode) {
                bankMasterDetailsList = (List<BankMasterDetails>) theiaTransactionalRedisUtil
                        .get(TheiaConstant.E_MANDATE_BANKS);
            } else if (MandateMode.PAPER_MANDATE == mandateMode) {
                bankMasterDetailsList = (List<BankMasterDetails>) theiaTransactionalRedisUtil
                        .get(TheiaConstant.PHYSICAL_MANDATE_BANKS);
            }

            if (CollectionUtils.isEmpty(bankMasterDetailsList)) {
                bankMasterDetailsList = fetchMandateBanks(mandateMode);
            }
        } else {
            List<BankMasterDetails> eMandateBanks = (List<BankMasterDetails>) theiaTransactionalRedisUtil
                    .get(TheiaConstant.E_MANDATE_BANKS);
            List<BankMasterDetails> physicalMandateBanks = (List<BankMasterDetails>) theiaTransactionalRedisUtil
                    .get(TheiaConstant.PHYSICAL_MANDATE_BANKS);

            if (CollectionUtils.isNotEmpty(eMandateBanks) || CollectionUtils.isNotEmpty(physicalMandateBanks)) {
                bankMasterDetailsList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(eMandateBanks)) {
                    bankMasterDetailsList.addAll(eMandateBanks);
                }
                if (CollectionUtils.isNotEmpty(physicalMandateBanks)) {
                    bankMasterDetailsList.addAll(physicalMandateBanks);
                }
                return bankMasterDetailsList;
            } else {
                bankMasterDetailsList = fetchMandateBanks(mandateMode);
            }
        }
        return bankMasterDetailsList;
    }

    /**
     * fetch bank list from mapping service and then save them in redis (ttl for
     * redis is 1 hour)
     *
     * @param mandateMode
     * @return
     * @throws MappingServiceClientException
     */
    private List<BankMasterDetails> fetchMandateBanks(MandateMode mandateMode) throws MappingServiceClientException {
        BankMasterDetailsResponse bankMasterDetailsResponse = bankInfoDataService
                .getBankMasterListByPayMode(TheiaConstant.MAPPING_PAY_MODE_FOR_MANDATE_BANKS);
        EXT_LOGGER.customInfo("Mapping response - BankMasterDetailsResponse for NB :: {}", bankMasterDetailsResponse);

        List<BankMasterDetails> bankMasterDetailsList = null;

        if (null != bankMasterDetailsResponse
                && CollectionUtils.isNotEmpty(bankMasterDetailsResponse.getBankMasterDetailsList())) {
            bankMasterDetailsList = bankMasterDetailsResponse.getBankMasterDetailsList();

            List<BankMasterDetails> eMandateBanks = new ArrayList<>();
            List<BankMasterDetails> physicalMandateBanks = new ArrayList<>();

            for (BankMasterDetails bankMasterDetails : bankMasterDetailsList) {
                MandateMode bankMode = MandateMode.getByMappingName(bankMasterDetails.getBankMandate());

                if (null != bankMode && bankMode == MandateMode.PAPER_MANDATE) {
                    physicalMandateBanks.add(bankMasterDetails);
                }

                if (null != bankMode && bankMode == MandateMode.E_MANDATE) {
                    eMandateBanks.add(bankMasterDetails);
                }
            }

            // using default expire time which is of one hour.
            if (CollectionUtils.isNotEmpty(eMandateBanks)) {
                theiaTransactionalRedisUtil.set(TheiaConstant.E_MANDATE_BANKS, eMandateBanks);
            }

            if (CollectionUtils.isNotEmpty(physicalMandateBanks)) {
                theiaTransactionalRedisUtil.set(TheiaConstant.PHYSICAL_MANDATE_BANKS, physicalMandateBanks);
            }

            if (MandateMode.PAPER_MANDATE == mandateMode)
                return physicalMandateBanks;

            if (MandateMode.E_MANDATE == mandateMode)
                return eMandateBanks;

            List<BankMasterDetails> bankMasterList = new ArrayList<>();
            bankMasterList.addAll(eMandateBanks);
            bankMasterList.addAll(physicalMandateBanks);
            return bankMasterList;
        } else {
            LOGGER.info("Unable to get bank list from mapping service");
        }
        return null;
    }

    public GenericCoreResponseBean<LimitQueryResponse> getMerchantTotalLimit(String alipayMid)
            throws FacadeCheckedException {

        LimitQueryResponse limitQueryResponse = merchantLimitService.getTotalLimits(alipayMid);
        EXT_LOGGER.customInfo("Mapping response - LimitQueryResponse :: {}", limitQueryResponse);
        if (!BizConstant.SUCCESS.equals(limitQueryResponse.getBody().getResultInfo().getResultCode())) {
            final String errorMessage = limitQueryResponse.getBody().getResultInfo().getResultMsg();
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    limitQueryResponse.getBody().getResultInfo().getResultCodeId());
            LOGGER.info("Invalid Result returned for Merchant Total Limit : {}", errorMessage);
            return new GenericCoreResponseBean<>(errorMessage, responseConstants);
        }

        return new GenericCoreResponseBean<>(limitQueryResponse);
    }

    public GenericCoreResponseBean<LimitAccumulationQueryResponse> getMerchantAccumulatedLimit(String alipayMid)
            throws FacadeCheckedException {

        LimitAccumulationQueryResponse limitAccumulationQueryResponse = merchantLimitService
                .getAccumulatedLimits(alipayMid);
        EXT_LOGGER.customInfo("Mapping response - LimitAccumulationQueryResponse :: {} for alipayMid :: {}",
                limitAccumulationQueryResponse, alipayMid);
        if (!BizConstant.SUCCESS.equals(limitAccumulationQueryResponse.getBody().getResultInfo().getResultCode())) {
            final String errorMessage = limitAccumulationQueryResponse.getBody().getResultInfo().getResultMsg();
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    limitAccumulationQueryResponse.getBody().getResultInfo().getResultCodeId());
            LOGGER.info("Invalid Result returned for Accumulated Limit Query Order : {}", errorMessage);
            return new GenericCoreResponseBean<>(errorMessage, responseConstants);
        }

        return new GenericCoreResponseBean<>(limitAccumulationQueryResponse);
    }

    public GenericCoreResponseBean<LimitConsumptionResponse> getConsumedLimitDetail(String paytmMid)
            throws FacadeCheckedException {

        LimitConsumptionResponse limitConsumptionResponse = merchantLimitService.getLimitConsumption(paytmMid);
        EXT_LOGGER.customInfo("Mapping response - LimitConsumptionResponse :: {} for Paytm MID :: {}",
                limitConsumptionResponse, paytmMid);
        if (CollectionUtils.isEmpty(limitConsumptionResponse.getLimitDetails())) {
            final String errorMessage = limitConsumptionResponse.getResponseMessage();
            LOGGER.info("Invalid Result returned for Accumulated Limit Query Order : {}", errorMessage);
            return new GenericCoreResponseBean<>(errorMessage);
        }

        return new GenericCoreResponseBean<>(limitConsumptionResponse);
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
                            buildPayOptionBillsForDcc(payOptionBill, transactionBean);
                            // Setting riskExtendedInfo for International
                            // Payments
                            riskExtendedInfo.put(BizConstant.INTERNATIONAL_PAYMENT_KEY,
                                    BizConstant.INTERNATIONAL_PAYMENT_VALUE);
                            LOGGER.info("international card request received with bin{}, orderId{}, mid{}", binDetail
                                    .getBin(), transactionBean.getWorkFlowBean().getOrderID(), transactionBean
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

    public GenericCoreResponseBean<Boolean> fetchAndSetUpiProfile(WorkFlowRequestBean input,
            WorkFlowTransactionBean workFlowTransBean) {

        EXT_LOGGER.customInfo("UPI configured on merchant ,fetching UPI profile v4");
        GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse = null;

        String userId = null != workFlowTransBean.getUserDetails() ? workFlowTransBean.getUserDetails().getUserId()
                : null != input.getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        if (ff4JUtils.featureEnabledOnMultipleKeys(input.getPaytmMID(), userId,
                Ff4jFeature.STORE_UPI_PROFILE_RESPONSE_IN_CACHE, false)) {
            UserProfileSarvatraV4 userProfileSarvatraV4 = (UserProfileSarvatraV4) theiaSessionRedisUtil
                    .get(BizConstant.USER_PROFILE_V4_CACHE_CONSTANT + userId);

            if (userProfileSarvatraV4 != null) {
                workFlowTransBean.setSarvatraUserProfileV4(userProfileSarvatraV4);
                setUpiOnboardingFlag(workFlowTransBean, userProfileSarvatraV4);
                setUpiOnBoardingForNewBankAcct(workFlowTransBean);
                filterUpiLinkedBankAccounts(workFlowTransBean);
                filterSubscriptionSupportedBankAccounts(workFlowTransBean);
                return new GenericCoreResponseBean<>(Boolean.TRUE);
            }
        }

        fetchUpiProfileResponse = fetchUserProfileV4(input, workFlowTransBean);

        if (fetchUpiProfileResponse != null && fetchUpiProfileResponse.isSuccessfullyProcessed()) {

            if (fetchUpiProfileResponse.getResponse() != null) {
                setUpiOnboardingFlag(workFlowTransBean, fetchUpiProfileResponse.getResponse());
            }

            if (workFlowTransBean.getSarvatraUserProfileV4() == null) {
                workFlowTransBean.setSarvatraUserProfileV4(fetchUpiProfileResponse.getResponse());
                if (fetchUpiProfileResponse.getResponse() != null
                        && fetchUpiProfileResponse.getResponse().getRespDetails() != null) {
                    // caching npci health
                    // todo - please remove as we are now caching full response
                    if (fetchUpiProfileResponse.getResponse().getRespDetails().getMetaDetails() != null) {
                        npciHealthUtil.setNpciHealthInCache(fetchUpiProfileResponse.getResponse().getRespDetails()
                                .getMetaDetails());
                    }
                    // caching upi profile v4
                    if (fetchUpiProfileResponse.getResponse().getRespDetails().getProfileDetail() != null
                            && ff4JUtils.featureEnabledOnMultipleKeys(input.getPaytmMID(), userId,
                                    Ff4jFeature.STORE_UPI_PROFILE_RESPONSE_IN_CACHE, false)) {
                        theiaSessionRedisUtil.set(BizConstant.USER_PROFILE_V4_CACHE_CONSTANT + userId,
                                fetchUpiProfileResponse.getResponse(), 300);
                    }
                    filterSubscriptionSupportedBankAccounts(workFlowTransBean);
                }

            }

            setUpiOnBoardingForNewBankAcct(workFlowTransBean);
            filterUpiLinkedBankAccounts(workFlowTransBean);
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        }

        return new GenericCoreResponseBean<>(Boolean.FALSE);
    }

    private void filterUpiLinkedBankAccounts(WorkFlowTransactionBean workFlowTransactionBean) {
        // if there is no accountNumber in initiate request then return all bank
        // accounts.
        // Else return the bank with masked account number matching the provided
        // account number.
        if (ERequestType.isMutualFundRequest(workFlowTransactionBean.getWorkFlowBean().getRequestType().getType())) {
            String accountNumber = workFlowTransactionBean.getWorkFlowBean().getPaymentRequestBean().getAccountNumber();
            if (StringUtils.isBlank(accountNumber)) {
                return;
            }
            if (workFlowTransactionBean.getSarvatraUserProfileV4() != null
                    && workFlowTransactionBean.getSarvatraUserProfileV4().getRespDetails() != null
                    && workFlowTransactionBean.getSarvatraUserProfileV4().getRespDetails().getProfileDetail() != null
                    && workFlowTransactionBean.getSarvatraUserProfileV4().getRespDetails().getProfileDetail()
                            .getBankAccounts() != null) {
                List<UpiBankAccountV4> upiFilteredBankAccounts = workFlowTransactionBean
                        .getSarvatraUserProfileV4()
                        .getRespDetails()
                        .getProfileDetail()
                        .getBankAccounts()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(bankAccount -> StringUtils.isNotBlank(bankAccount.getMaskedAccountNumber())
                                && matchingBankAccountNumber(bankAccount, accountNumber)).collect(Collectors.toList());
                workFlowTransactionBean.getSarvatraUserProfileV4().getRespDetails().getProfileDetail()
                        .setBankAccounts(upiFilteredBankAccounts);
            }
            return;
        }
    }

    private boolean matchingBankAccountNumber(UpiBankAccountV4 bankAccount, String accountNumber) {
        if (bankAccount.getMaskedAccountNumber().length() > 4 && accountNumber.length() > 4) {
            return StringUtils.equals(
                    bankAccount.getMaskedAccountNumber().substring(bankAccount.getMaskedAccountNumber().length() - 4),
                    accountNumber.substring(accountNumber.length() - 4));
        }
        return false;
    }

    private void setUpiOnboardingFlag(WorkFlowTransactionBean workFlowTransactionBean,
            UserProfileSarvatraV4 userProfileSarvatraV4) {
        if (checkIfUserNotOnboardedOnUPI(userProfileSarvatraV4) || checkIfBankAccountsNotLinked(userProfileSarvatraV4)) {
            createAndSetUpiProfileWithOnlyNpciHealth(workFlowTransactionBean);
            HealthCategory npciHealth = Optional.ofNullable(workFlowTransactionBean)
                    .map(WorkFlowTransactionBean::getSarvatraUserProfileV4).map(UserProfileSarvatraV4::getRespDetails)
                    .map(PaytmVpaDetailsV4::getMetaDetails).map(NpciHealthData::getNpciHealthCategory).orElse(null);
            if (HealthCategory.RED == npciHealth) {
                // stopping onboarding if npci is down
                workFlowTransactionBean.getSarvatraUserProfileV4().setUpiOnboarding(false);
            } else if (HealthCategory.GREEN == npciHealth || HealthCategory.YELLOW == npciHealth) {
                workFlowTransactionBean.getSarvatraUserProfileV4().setUpiOnboarding(true);
            }
        }
    }

    private void setUpiOnBoardingForNewBankAcct(WorkFlowTransactionBean workFlowTransactionBean) {
        if (workFlowTransactionBean.getWorkFlowBean() != null
                && ff4JUtils.isFeatureEnabledOnMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID(),
                        FEATURE_ENABLE_UPI_ONBOARDING_FOR_NEW_BANKACCT, false)) {
            HealthCategory npciHealth = Optional.ofNullable(workFlowTransactionBean)
                    .map(WorkFlowTransactionBean::getSarvatraUserProfileV4).map(UserProfileSarvatraV4::getRespDetails)
                    .map(PaytmVpaDetailsV4::getMetaDetails).map(NpciHealthData::getNpciHealthCategory).orElse(null);
            if (HealthCategory.RED == npciHealth) {
                // stopping onboarding if npci is down
                workFlowTransactionBean.getSarvatraUserProfileV4().setOnboardNewUPIBankAcct(false);
            } else if (HealthCategory.GREEN == npciHealth || HealthCategory.YELLOW == npciHealth) {
                workFlowTransactionBean.getSarvatraUserProfileV4().setOnboardNewUPIBankAcct(true);
            }
        }
    }

    private boolean checkIfUserNotOnboardedOnUPI(UserProfileSarvatraV4 fetchUserPaytmVpaResponse) {
        if (fetchUserPaytmVpaResponse != null && FAILURE.equalsIgnoreCase(fetchUserPaytmVpaResponse.getStatus())
                && UPI_USER_NOT_FOUND_RESPONSE_CODE.equals(fetchUserPaytmVpaResponse.getRespCode())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkIfBankAccountsNotLinked(UserProfileSarvatraV4 fetchUserPaytmVpaResponse) {
        if (fetchUserPaytmVpaResponse != null
                && fetchUserPaytmVpaResponse.getRespDetails() != null
                && fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail() != null
                && (CollectionUtils.isEmpty(fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail()
                        .getBankAccounts()))) {
            return true;
        } else {
            return false;
        }
    }

    private GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserProfileV4(WorkFlowRequestBean input,
            WorkFlowTransactionBean flowTransBean) {

        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest();

        if (flowTransBean.getUserDetails() != null) {
            fetchUserPaytmVpaRequest.setUserId(flowTransBean.getUserDetails().getUserId());
            fetchUserPaytmVpaRequest.setUserToken(flowTransBean.getUserDetails().getUserToken());
            fetchUserPaytmVpaRequest.setOriginChannel(input.getOriginChannel());
            fetchUserPaytmVpaRequest.setRequestId(RequestIdGenerator.generateRequestId());
            fetchUserPaytmVpaRequest.setDeviceId(input.getDeviceId());
            if (StringUtils.isBlank(input.getDeviceId())) {
                Map<String, String> queryParams = getQueryParams(input.getQueryParams());
                if (queryParams != null) {
                    fetchUserPaytmVpaRequest.setDeviceId(queryParams.get(FacadeConstants.ConsentAPIConstant.DEVICE_ID));
                }
            }
            fetchUserPaytmVpaRequest.setQueryParams(input.getQueryParams());
            fetchUserPaytmVpaRequest.setFetchCreditCardAccounts(String.valueOf(input.isCcOnUPIEnabled()));
            fetchUserPaytmVpaRequest.setFetchLRNDetails(String.valueOf(input.isUpiLite()));
        }

        // TODO Removing check as not getting used
        /*
         * if (StringUtils.isBlank(fetchUserPaytmVpaRequest.getUserToken())) {
         * LOGGER.error("Token is blank"); return new
         * GenericCoreResponseBean<UserProfileSarvatraV4
         * >("InvalidTokenProvided", ResponseConstants.SYSTEM_ERROR); }
         */
        if (ERequestType.SUBSCRIBE.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(flowTransBean.getWorkFlowBean().getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
            fetchUserPaytmVpaRequest.setFilterType("SUBSCRIPTION");
        }

        if (flowTransBean.getWorkFlowBean().isPreAuth()) {
            fetchUserPaytmVpaRequest.setFilterType("OTM");
        }

        return sarvatraVpaDetails.fetchUserProfileVpaV4(fetchUserPaytmVpaRequest);
    }

    private Map<String, String> getQueryParams(String queryParamString) {
        Map<String, String> queryParams = null;
        if (StringUtils.isNotBlank(queryParamString)) {
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

    public GenericCoreResponseBean<?> createAndSetUpiProfileWithOnlyNpciHealth(WorkFlowTransactionBean transBean) {
        NpciHealthData npciHealthData;
        try {
            npciHealthData = npciHealthUtil.getNpciHealth();

        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching Npci Health : {} ", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
        if (npciHealthData != null) {
            transBean.setSarvatraUserProfileV4(new UserProfileSarvatraV4(npciHealthData));
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        }

        return new GenericCoreResponseBean<>(Boolean.FALSE);
    }

    public boolean isMidAllowedChargeFeeOnAddMoneyByWallet(String mid) {
        LOGGER.info("checking if allowedFeeOnAddMoneyOnWallet for mid:{}", mid);
        return ff4JUtil.isFeatureEnabled(ADD_MONEY_FEE_WALLET_CONSULT, mid);
    }

    public String getMerchantNameFromExtendInfo(String mid) {
        try {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantDataService
                    .getMerchantExtendedData(mid);
            EXT_LOGGER
                    .customInfo("Mapping response - MerchantExtendedInfoResponse :: {}", merchantExtendedInfoResponse);

            return Optional.ofNullable(merchantExtendedInfoResponse).map(MerchantExtendedInfoResponse::getExtendedInfo)
                    .map(MerchantExtendedInfoResponse.MerchantExtendedInfo::getMerchantName).orElse(StringUtils.EMPTY);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Unable to fetch Merchant Extend Data from Mapping Service");
            return StringUtils.EMPTY;
        }
    }

    public void pushDynamicQrPaymentEvent(String payMethod) {
        Map<String, String> map = new HashMap<>();
        map.put("PayMethod", payMethod);
        EventUtils.pushTheiaEvents(EventNameEnum.DYNAMIC_QR_PAYMENT, map);
    }

    /**
     * card IndexNumber will come in savedCardId ()
     */
    public boolean checkForCardIndexNo(WorkFlowRequestBean flowRequestBean) {
        if (Objects.nonNull(flowRequestBean) && StringUtils.isNotEmpty(flowRequestBean.getSavedCardID())) {
            if (flowRequestBean.getSavedCardID().length() > 15) {
                LOGGER.info("Txn is via Card Index No");
                flowRequestBean.setTxnFromCardIndexNo(true);
                return true;
            }
        }
        return false;
    }

    public String checkForCardIndexNo(String requestPaymentDetails) {
        try {
            if (StringUtils.isNotBlank(requestPaymentDetails)) {
                String[] paymentDetails = requestPaymentDetails.split(Pattern.quote("|"), -1);
                if (paymentDetails[0].length() > 15) {
                    LOGGER.info("Txn is via Card Index No");
                    return paymentDetails[0];
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in parsing paymentDetails for CC/DC");
        }
        return null;

    }

    public void getAndSetCardHashAndCardIndexNo(WorkFlowRequestBean flowRequestBean, UserDetailsBiz userDetails,
            String cardNumber) {
        if (flowRequestBean == null) {
            return;
        }

        SavedCardResponse<SavedCardVO> savedCardsBean = null;
        String custId = flowRequestBean.getCustID();
        String mId = flowRequestBean.getPaytmMID();
        String savedCardId = flowRequestBean.getSavedCardID();
        // in case savedMigration is over , savedCardId will be cardIndexNo
        boolean isTxnByCardIndexNo = StringUtils.isEmpty(savedCardId);
        if (isTxnByCardIndexNo) {
            return;
        }
        if (StringUtils.isEmpty(cardNumber)) {
            if (userDetails == null) {
                return;
            }
            savedCardsBean = savedCardsService.getSavedCardByCardId(Long.parseLong(savedCardId),
                    userDetails.getUserId(), custId, mId);
            if (savedCardsBean == null || !savedCardsBean.getStatus() || savedCardsBean.getResponseData() == null) {
                LOGGER.error("Unable to fetch savedCard");
                return;
            }
            cardNumber = savedCardsBean.getResponseData().getCardNumber();
        }
        try {
            String cardHash = SignatureUtilWrapper.signApiRequest(cardNumber);
            flowRequestBean.setCardHash(cardHash);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error in generating cardHash {}", e);
            return;
        }
    }

    public String getCardIndexNoFromCardNumber(String cardNumber) {
        // ff4j support as part of deprecation of cache card API
        LOGGER.info("Calling Cache Card Service for CIN");
        if (ff4JUtils.isFeatureEnabled(DEPRECATE_CACHE_CARD_API, false)) {
            return null;
        }
        final CacheCardRequest cacheCardReq = createCacheCardRequest(cardNumber);
        if (null != cacheCardReq) {
            CacheCardResponse cacheCardFacadeResponse = callAPlusForCardIdxNo(cacheCardReq);
            return Optional.ofNullable(cacheCardFacadeResponse).map(CacheCardResponse::getBody)
                    .map(CacheCardResponseBody::getCardIndexNo).orElse(null);
        }
        return null;
    }

    public String getGcinFromCardNumber(String cardNumber) {
        if (ff4JUtils.isFeatureEnabled(THEIA_ENABLE_GCIN_GENERATION, false)) {
            return generateCardGCIN(cardNumber);
        }
        CacheCardRequest cacheCardRequest = createCacheCardRequest(cardNumber);
        if (null != cacheCardRequest) {
            CacheCardResponse cacheCardFacadeResponse = callAPlusForCardIdxNo(cacheCardRequest);
            return Optional.ofNullable(cacheCardFacadeResponse).map(CacheCardResponse::getBody)
                    .map(CacheCardResponseBody::getGlobalPanIndex).orElse(null);
        }
        return null;
    }

    public String getMaskedNumber(String input) {
        String maskedString = "";
        if (StringUtils.isNotBlank(input) && input.length() > 4) {
            int len = input.length();
            String firstPart = input.substring(0, len - 4);
            String lastPart = input.substring(len - 4, len);
            firstPart = firstPart.replaceAll("[0-9]", "*");
            String prefix = ConfigurationUtil.getProperty(BizConstant.COBRANDED_CARD_PREFIX);
            maskedString = firstPart + prefix + lastPart;

        }
        return maskedString;
    }

    private CacheCardRequest createCacheCardRequest(final String cardNumber) {
        final CacheCardRequestBean cacheCardReqBean = new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNumber,
                null, null, null, null, null, null, null).build();
        GenericCoreResponseBean<CacheCardRequest> cacheCardReq = CacheCardInfoHelper
                .createCacheCardRequestForCardPayment(cacheCardReqBean, InstNetworkType.ISOCARD);
        if (!cacheCardReq.isSuccessfullyProcessed()) {
            LOGGER.error("Error in fetching cardIndexNo from Platform {} ", cacheCardReq.getFailureDescription());
            return null;
        }
        return cacheCardReq.getResponse();
    }

    private CacheCardResponse callAPlusForCardIdxNo(CacheCardRequest cacheCardReq) {
        try {
            coftUtil.updateCacheCardRequest(cacheCardReq);
            return assetFacade.cacheCard(cacheCardReq);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured in cache card info in /fetchCardIndexNo: ", e);
        }
        return null;
    }

    public boolean isAddMoneyOnCCfeePhase2Enabled() {
        return StringUtils.equalsIgnoreCase(TRUE,
                ConfigurationUtil.getTheiaProperty(ADD_MONEY_PHASE2_ENABLE_FLAG, "false"));
    }

    public GenericCoreResponseBean<Object> fetchChannelDetails(WorkFlowRequestBean workFlowRequestBean) {

        final long startTime = System.currentTimeMillis();

        try {

            ChannelPaymentDetailsRequest channelRequest = createChannelPaymentDetailsRequest(workFlowRequestBean
                    .getqRCodeInfo().getKybId(), workFlowRequestBean.getqRCodeInfo().getShopId(),
                    workFlowRequestBean.getAppVersion());
            channelRequest.setPgMid(workFlowRequestBean.getPaytmMID());

            ChannelPaymentDetailsHeaderRequest headerRequest = createChannelPaymentDetailsHeaderRequest(workFlowRequestBean
                    .getToken());

            String queryParams = workFlowRequestBean.getQueryParams();
            // LOGGER.info("FetchChannelDetails initiated for trace id: {}",
            // headerRequest.getTraceId());

            Object channelResponse = channelPaymentDetailsService.getChannelPaymentDetails(channelRequest,
                    headerRequest, queryParams);

            LOGGER.info("FetchChannelDetails successful for trace id: {}", headerRequest.getTraceId());

            if (null == channelResponse) {
                return null;
            }

            return new GenericCoreResponseBean<>(channelResponse);

        } catch (Exception e) {
            LOGGER.error("Error occured in FetchChannelDetails task: {}", e);
            return null;
        } finally {
            LOGGER.info("Total time taken for FetchChannelDetails task  is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    private ChannelPaymentDetailsHeaderRequest createChannelPaymentDetailsHeaderRequest(String token) {
        ChannelPaymentDetailsHeaderRequest request = new ChannelPaymentDetailsHeaderRequest();
        request.setContentType(MediaType.APPLICATION_JSON);
        request.setSessionToken(token);
        request.setTraceId(UUID.randomUUID().toString());
        try {
            request.setUserAgent(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
        } catch (Exception e) {
            LOGGER.warn("error while getting user agent for mlv request :{}", e.getMessage());
        }
        return request;
    }

    private ChannelPaymentDetailsRequest createChannelPaymentDetailsRequest(String kybId, String shopId,
            String appVersion) {
        ChannelPaymentDetailsRequest detailsRequest = new ChannelPaymentDetailsRequest();
        detailsRequest.setKybId(kybId);
        detailsRequest.setShopId(shopId);
        detailsRequest.setVersion_id(appVersion);
        return detailsRequest;
    }

    public PayCardOptionViewBiz getLitePayViewConsultResponse(String mid, String cardIndexNumber) {

        PayCardOptionViewBiz payCardOptionViewBiz = null;
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsultResponseBizBean = getLitePayViewConsultResponse(mid);
        if (null != litePayviewConsultResponseBizBean) {
            for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getResponse()
                    .getPayMethodViews()) {
                payCardOptionViewBiz = payMethodViewsBiz.getPayCardOptionViews().stream()
                        .filter(payCardOptionBiz -> cardIndexNumber.equals(payCardOptionBiz.getCardIndexNo()))
                        .findAny().orElse(null);
                if (null != payCardOptionViewBiz) {
                    return payCardOptionViewBiz;
                }
            }
        }

        litePayviewConsultResponseBizBean = getAddAndPayLitePayviewConsultResponse();
        if (null != litePayviewConsultResponseBizBean) {
            for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getResponse()
                    .getPayMethodViews()) {
                payCardOptionViewBiz = payMethodViewsBiz.getPayCardOptionViews().stream()
                        .filter(payCardOptionBiz -> cardIndexNumber.equals(payCardOptionBiz.getCardIndexNo()))
                        .findAny().orElse(null);
                if (null != payCardOptionViewBiz) {
                    return payCardOptionViewBiz;
                }
            }
        }

        return payCardOptionViewBiz;
    }

    public void filterPlatformSavedAssetsForSubs(LitePayviewConsultResponseBizBean liteViewResponse,
            SubsPaymentMode subsPaymentMode, LitePayviewConsultType consultType, boolean isAoaMerchant) {
        if (liteViewResponse == null || CollectionUtils.isEmpty(liteViewResponse.getPayMethodViews())) {
            return;
        }
        List<PayMethodViewsBiz> payMethodViews = liteViewResponse.getPayMethodViews();
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViews) {
            if (CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayCardOptionViews())) {
                // filter saved assets for subs
                Iterator<PayCardOptionViewBiz> payCardOptionViewBizIterator = payMethodViewsBiz.getPayCardOptionViews()
                        .iterator();
                while (payCardOptionViewBizIterator.hasNext()) {
                    PayCardOptionViewBiz payCardOptionViewBiz = payCardOptionViewBizIterator.next();
                    BinDetail binDetail = new BinDetail();
                    binDetail.setCardType(payCardOptionViewBiz.getPayMethod());
                    binDetail.setCardName(payCardOptionViewBiz.getCardScheme());
                    binDetail.setBankCode(payCardOptionViewBiz.getInstId());
                    binDetail.setBin(Long.parseLong(payCardOptionViewBiz.getCardBin()));
                    if (LitePayviewConsultType.MerchantLitePayViewConsult == consultType
                            && SubsPaymentMode.UNKNOWN == subsPaymentMode) {
                        subsPaymentMode = SubsPaymentMode.NORMAL;
                    }
                    if (isAoaMerchant) {
                        String bin = String.valueOf(binDetail.getBin());
                        if (!isBinValid(bin)) {
                            payCardOptionViewBizIterator.remove();
                        }
                    } else if (!subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail, subsPaymentMode,
                            SubscriptionRequestType.CREATE)) {
                        payCardOptionViewBizIterator.remove();
                    }
                }
            }
        }
    }

    public boolean isBinValid(String bin) {
        if (StringUtils.isNotBlank(bin)) {
            LOGGER.error("AOA subscription client call is being used");
            // AoaSubscriptionBinValidationRequest
            // aoaSubscriptionBinValidationRequest = new
            // AoaSubscriptionBinValidationRequest();
            // aoaSubscriptionBinValidationRequest.setBin(bin);
            // AoaSubscriptionBinValidationResponse subsBinValidateResponse =
            // aoaSubscriptionService
            // .isBinSupported(aoaSubscriptionBinValidationRequest);
            // return null != subsBinValidateResponse &&
            // subsBinValidateResponse.isBinBoundToSubscription();
        }
        return false;
    }

    /**
     * This method checks whether given cardIndexNumber belongs to user or not
     */
    private boolean isCardIndexNumberBoundToUser(final WorkFlowTransactionBean workFlowTransBean) {
        WorkFlowRequestBean flowRequestBean = workFlowTransBean.getWorkFlowBean();
        UserDetailsBiz userDetails = workFlowTransBean.getUserDetails();
        SavedAssetInfo savedAssetInfo = bizProdHelper.getSavedCardByUserIdAndCardId(flowRequestBean.getSavedCardID(),
                userDetails.getUserId());

        if (null == savedAssetInfo) {

            savedAssetInfo = merchantBizProdHelper.getSavedCardByMidCustIdAndCardId(flowRequestBean.getSavedCardID(),
                    flowRequestBean.getPaytmMID(), flowRequestBean.getCustID());

        }
        if (null == savedAssetInfo) {
            LOGGER.error("Unable to fetch saved card data from bizProd/merchantProd ");
            return false;
        }
        workFlowTransBean.getWorkFlowBean().setCardNo(savedAssetInfo.getMaskedCardNo());
        workFlowTransBean.getWorkFlowBean().setCardIndexNo(savedAssetInfo.getCardIndexNo());
        workFlowTransBean.getWorkFlowBean().setCardType(
                EPayMethod.getPayMethodByOldName(savedAssetInfo.getAssetType()).getMethod());
        workFlowTransBean.getWorkFlowBean().setBankCode(savedAssetInfo.getInstId());
        workFlowTransBean.getWorkFlowBean().setCardScheme(savedAssetInfo.getCardScheme());
        return true;
    }

    /**
     * @param cardBin
     * @return bin8Hash
     */
    public CardBinHashResponse getCardBinHash(String cardBin) {
        CardBinHashResponse cardBinHashResponse = null;
        try {
            CardBinHashRequest cardBinHashRequest = new CardBinHashRequest(cardBin);
            if (ff4JUtils.isFeatureEnabled(THEIA_ENABLE_CARD_HASH_GENERATION, false)) {
                cardBinHashResponse = cardBinHelper.generateCardBinHashResponse(cardBinHashRequest);
            } else {
                cardBinHashResponse = cardInfoQueryService.cardBinHash(cardBinHashRequest);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception fetching card bin hash", ex);
        }
        return cardBinHashResponse;
    }

    private GenericCoreResponseBean convertRiskVerificationToRiskReject(String message) {

        if (StringUtils.isEmpty(message)) {
            message = RiskConstants.RISK_REJECT_MESSAGE;
        }
        return new GenericCoreResponseBean<>(ResponseConstants.RISK_REJECT.getMessage(), ResponseConstants.RISK_REJECT,
                message);
    }

    public void modifyOrderForOnusRentPayment(WorkFlowRequestBean flowRequestBean, Map<String, String> riskInfo) {

        OrderModifyRequest orderModifyRequest = workRequestCreator.getOrderModifyRequestForOnusRentPayment(
                flowRequestBean, riskInfo);
        try {
            orderService.modifyOrder(orderModifyRequest);
        } catch (FacadeCheckedException exception) {
            LOGGER.error("Exception Occurred while modifying order :", exception);
        }

    }

    public void cacheUPIRecurringInfo(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            UpiRecurringInfo upiRecurringInfo = buildUpiRecurringInfo(flowRequestBean);
            String subsUpiRecurringkey = new StringBuilder("UPI_SUBSCRIPTION_").append(flowRequestBean.getPaytmMID())
                    .append("_").append(flowRequestBean.getOrderID()).toString();
            theiaTransactionalRedisUtil.setnx(subsUpiRecurringkey, upiRecurringInfo,
                    subsUpiRecurringkeyExpiryTimeInSeconds);
        }
    }

    public void setAccountInfoInChannelInfo(Map<String, String> channelInfo, WorkFlowRequestBean workFlowRequestBean) {
        LOGGER.info("Adding Account verification Related Info in ChannelInfo");
        channelInfo
                .put(TheiaConstant.ChannelInfoKeys.EXPECTED_USER_FUND_SOURCE, workFlowRequestBean.getAccountNumber());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.NEED_FUND_SOURCE_VERIFICATION,
                org.apache.commons.lang.StringUtils.isBlank(workFlowRequestBean.getValidateAccountNumber()) ? "false"
                        : workFlowRequestBean.getValidateAccountNumber());
        channelInfo.put(TheiaConstant.ChannelInfoKeys.ALLOW_UNVERIFIED_FUND_SOURCE,
                org.apache.commons.lang.StringUtils.isBlank(workFlowRequestBean.getAllowUnverifiedAccount()) ? "false"
                        : workFlowRequestBean.getAllowUnverifiedAccount());
    }

    private void cacheUPIRecurringInfo(WorkFlowRequestBean flowRequestBean,
            SubscriptionResponse subscriptionServiceResponse) {
        LOGGER.info("Caching UPI Recurring Info for Insta for Renewal");
        UpiRecurringInfo upiRecurringInfo = new UpiRecurringInfo();
        upiRecurringInfo.setMandateExecutionNo(subscriptionServiceResponse.getSubscriptionUpiInfo().getRenewalCount());
        upiRecurringInfo.setRetryAttempt(subscriptionServiceResponse.getSubscriptionUpiInfo().getRetryAttempt());
        String subsUpiRecurringkey = new StringBuilder("UPI_SUBSCRIPTION_").append(flowRequestBean.getPaytmMID())
                .append("_").append(flowRequestBean.getOrderID()).toString();
        theiaTransactionalRedisUtil
                .setnx(subsUpiRecurringkey, upiRecurringInfo, subsUpiRecurringkeyExpiryTimeInSeconds);
    }

    private UpiRecurringInfo buildUpiRecurringInfo(WorkFlowRequestBean flowRequestBean) {
        LOGGER.info("Caching UPI Recurring Info for Insta for Creation");
        UpiRecurringInfo upiRecurringInfo = new UpiRecurringInfo();
        upiRecurringInfo.setCustId(flowRequestBean.getCustID());
        FrequencyUnit frequencyUnit = FrequencyUnit.getFrequencyUnitbyName(flowRequestBean.getSubsFrequencyUnit());
        /* changing frequency to ONDEMAND in case of flexi subscription */
        if (flowRequestBean.isFlexiSubscription()) {
            frequencyUnit = FrequencyUnit.ONDEMAND;
        }
        String validityStartDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        upiRecurringInfo.setValidityStartDate(validityStartDate);
        if (frequencyUnit != null) {
            upiRecurringInfo.setFrequencyPattern(frequencyUnit.getUpiFreq());
            upiRecurringInfo.setFrequencyRule(getFrequencyRule(frequencyUnit.getName(), validityStartDate));
        }
        upiRecurringInfo.setGracePeriod(flowRequestBean.getSubsGraceDays());
        upiRecurringInfo.setSubscriptionMaxAmount(flowRequestBean.getSubsMaxAmount());
        upiRecurringInfo.setValidityEndDate(flowRequestBean.getSubsExpiryDate());
        return upiRecurringInfo;
    }

    private String getFrequencyRule(String subsFrequencyUnit, String subsStartDate) {

        switch (subsFrequencyUnit) {
        case "DAY":
            return "1";
        case "WEEK":
            FrequencyRule frequencyRule = FrequencyRule.getFrequencyMappingByValue(getDayOfWeek(subsStartDate));
            return frequencyRule.getDay();
        case "FORTNIGHT":
            // Get day from date
            String subsCreateDate = getDay(subsStartDate);
            return getFrequencyValue(subsCreateDate);
        default:
            return getDay(subsStartDate);

        }
    }

    private String getDayOfWeek(String date) {
        return LocalDate.parse(date).getDayOfWeek().name();
    }

    private String getDay(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String day = "";
        try {
            Date subsDate = sdf.parse(date);
            day = String.valueOf(subsDate.getDate());
        } catch (ParseException e) {
            LOGGER.error("exception while parsing subscription start date", e);
        }
        return day;
    }

    public boolean isAddMoneyToWallet(String mid, WorkFlowRequestBean workFlowRequestBean) {
        /*
         * addMoney is done for SCW... merchant and
         * ADD_MONEY/ADD_MONEY_EXPRESS/DEFAULT/TOPUP_EXPRESS flows,
         * isAddMoneyToWallet check is done so that this walletlimit api and
         * mapping-service api called for only above flows
         */
        if (workFlowRequestBean != null && workFlowRequestBean.getRequestType() != null) {
            String requestType = workFlowRequestBean.getRequestType().getType();
            return org.apache.commons.lang.StringUtils.equals(ADDMONEY_EXPRESS.getType(), requestType)
                    || org.apache.commons.lang.StringUtils.equals(ERequestType.ADD_MONEY.getType(), requestType)
                    || org.apache.commons.lang.StringUtils.equals(TOPUP_EXPRESS.getType(), requestType)
                    || org.apache.commons.lang.StringUtils.equals(mid,
                            ConfigurationUtil.getTheiaProperty(MP_ADD_MONEY_MID))
                    || workFlowRequestBean.isNativeAddMoney();
        }
        return false;
    }

    public GenericCoreResponseBean<BizCancelOrderResponse> closeFundOrderWithReason(
            WorkFlowTransactionBean workFlowTransactionBean, String closeReason) {
        EnvInfoRequestBean envInfo = workFlowTransactionBean.getEnvInfoReqBean();
        if (envInfo == null) {
            envInfo = workFlowTransactionBean.getWorkFlowBean().getEnvInfoReqBean();
        }
        BizCancelFundOrderRequest bizCancelFundOrderRequest = new BizCancelFundOrderRequest(
                workFlowTransactionBean.getTransID(), envInfo, closeReason);
        bizCancelFundOrderRequest.setPaytmMerchantId(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        bizCancelFundOrderRequest.setRoute(Routes.PG2);
        return orderService.closeFundOrder(bizCancelFundOrderRequest);
    }

    public GenericCoreResponseBean<BizWalletConsultResponse> consultWalletService(
            InitiateTransactionRequestBody orderDetail, HttpServletRequest request, UserDetailsBiz userDetails,
            boolean isGvPurchaseFlow, boolean isTransitWallet, String addMoneySource) {
        final ConsultWalletLimitsRequest walletConsultRequest = workRequestCreator.createConsultWalletLimitRequest(
                orderDetail, request, userDetails, isGvPurchaseFlow, isTransitWallet, addMoneySource);
        final BizWalletConsultResponse addMoneyConsultResponse = bizPaymentService
                .walletLimitsConsultV2(walletConsultRequest);
        LOGGER.info("Add money consult response for v2/walletLimits : {}", addMoneyConsultResponse);

        return new GenericCoreResponseBean<>(addMoneyConsultResponse);
    }

    public void setOrderTimeOutForSubscription(WorkFlowRequestBean workFlowRequestBean) {
        if (PaymentTypeIdEnum.CC.value.equals(workFlowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            String orderTimeOutSubscriptonInCache = (String) theiaSessionRedisUtil.hget(
                    workFlowRequestBean.getTxnToken(), "orderTimeOut");
            if (StringUtils.isNotBlank(orderTimeOutSubscriptonInCache)) {
                workFlowRequestBean.setOrderTimeOutInMilliSecond(orderTimeOutSubscriptonInCache);
            } else {
                workFlowRequestBean.setOrderTimeOutInMilliSecond(workFlowRequestBean
                        .getOrderExpiryTimeInMerchantContract());
            }
        }
    }

    private List<PromoSaving> getPromoSavings(PaymentOfferDetails paymentOfferDetails) {
        List<PromoSaving> promoSavings = new ArrayList<>();
        if (StringUtils.isNotBlank(paymentOfferDetails.getCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(REDEMPTION_TYPE_CASHBACK);
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
            promoSaving.setRedemptionType(REDEMPTION_TYPE_PAYTM_CASHBACK);
            promoSaving.setSavings(Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOfferDetails
                    .getPaytmCashbackAmount())));
            promoSavings.add(promoSaving);
        }
        return promoSavings;
    }

    private String checkCardHashInInitTxnRequest(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return null;
            }
            return (String) theiaSessionRedisUtil.hget(token, ExtendedInfoKeys.CARD_HASH);
        } catch (Exception ex) {
            LOGGER.error("Exception in fetching card Hash from initiate Request : {}", ex);
            return null;
        }
    }

    public void modifyCreatedOrderInInitiateTxn(WorkFlowRequestBean flowRequestBean, List<Goods> goodsList,
            List<ShippingInfo> shippingInfos) throws FacadeCheckedException {

        OrderModifyRequest orderModifyRequest = workRequestCreator.getOrderModifyRequestForUpdateTransaction(
                flowRequestBean, goodsList, shippingInfos);
        try {
            orderService.modifyOrder(orderModifyRequest);
        } catch (FacadeCheckedException ex) {
            LOGGER.error("Exception Occurred while modifying order :", ex);
            throw ex;
        }

    }

    public void setTransIdForPreAuth(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.isPreAuth()) {
            workFlowRequestCreationHelper.getRoute(workFlowRequestBean, "queryByMerchantTransId");
            LOGGER.info("Setting Acquiremnt Id for PrAuth Request");
            workFlowRequestBean.setTransID(getTransId(workFlowRequestBean).getResponse().getAcquirementId());
        }
    }

    private GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> getTransId(
            WorkFlowRequestBean flowRequestBean) {
        WorkFlowTransactionBean flowTransBean = new WorkFlowTransactionBean();
        flowTransBean.setWorkFlowBean(flowRequestBean);
        GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantIDResponse = queryByMerchantTransID(
                flowTransBean, true);
        if (!queryByMerchantIDResponse.isSuccessfullyProcessed()
                || org.apache.commons.lang.StringUtils.isBlank(queryByMerchantIDResponse.getResponse()
                        .getAcquirementId())) {
            LOGGER.error("Query by Merchant TransID status is Invalid : {}", queryByMerchantIDResponse);
            return new GenericCoreResponseBean<>("System Error", ResponseConstants.SYSTEM_ERROR);
        } else {
            if (AcquirementStatusType.CLOSED.equals(queryByMerchantIDResponse.getResponse().getStatusDetail()
                    .getAcquirementStatus())) {
                LOGGER.error("Order is closed so terminating transaction  ");
                return new GenericCoreResponseBean<>("Order is closed", ResponseConstants.ORDER_IS_CLOSED);
            }
        }
        return queryByMerchantIDResponse;
    }

    public GenericCoreResponseBean<SendMessageResponse> sendNotificationAppInvoke(
            WorkFlowRequestBean workFlowRequestBean) {

        return null;
    }

    public String getFrequencyValue(String subsStartDate) {
        int subsCreateDate = Integer.parseInt(subsStartDate);
        if (subsCreateDate >= 1 && subsCreateDate <= 15) {
            return Integer.toString(subsCreateDate);
        }
        return (Integer.toString((subsCreateDate % 15)));
    }

    public void buildPayOptionBillsForDcc(BizPayOptionBill bizPayOptionBill, WorkFlowTransactionBean flowTransactionBean) {
        WorkFlowRequestBean flowRequestBean = flowTransactionBean.getWorkFlowBean();
        if (flowRequestBean.getDccSupported() && flowRequestBean.isPaymentCallFromDccPage()
                && flowRequestBean.getDccPageData() != null) {
            DccPaymentInfo dccPaymentInfo = new DccPaymentInfo(flowRequestBean.getDccServiceInstId(),
                    String.valueOf(flowRequestBean.isDccSelectedByUser()), flowRequestBean.getDccPageData()
                            .getDccPaymentDetails());
            bizPayOptionBill.setDccPaymentInfo(dccPaymentInfo);

        }
    }

    private void pushRequestToDwhKafkaTopic(Object payRequest, String mid) {
        boolean ff4jNotifyPaymentRequestData = ff4JUtil.isFeatureEnabled(
                BizConstant.Ff4jFeature.NOTIFY_PAYMENT_REQUEST_DATA, mid);
        if (ff4jNotifyPaymentRequestData) {
            long startTime = System.currentTimeMillis();
            pushPayloadToDwhKafkaTopic(payRequest, mid);
            long endTime = System.currentTimeMillis();
            EXT_LOGGER.customInfo("Total time to push Pay Request on DWH Kafka Topic {}", (endTime - startTime));
        }
    }

    private void pushPayloadToDwhKafkaTopic(Object payRequest, String mid) {
        try {
            payRequestNotifierService.pushPayloadToKafkaTopic(payRequest);
        } catch (Exception e) {
            boolean logStackTrace = ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.NOTIFY_PAYMENT_DWH_STACKTRACE,
                    mid);
            if (logStackTrace)
                LOGGER.error("Error While sending Request of DWH Kafka Topic", e);
            else {
                EventNameEnum eventName = null;
                if (payRequest instanceof CreateOrderAndPayRequestBean) {
                    if (((CreateOrderAndPayRequestBean) payRequest).isFromAoaMerchant()) {
                        eventName = EventNameEnum.AOA_COP_KAFKA_ERROR;
                    } else {
                        eventName = EventNameEnum.COP_KAFKA_ERROR;
                    }
                } else if (payRequest instanceof BizPayRequest) {
                    eventName = EventNameEnum.PAY_KAFKA_ERROR;
                } else if (payRequest instanceof BizAoaPayRequest) {
                    eventName = EventNameEnum.AOA_PAY_KAFKA_ERROR;
                } else {
                    LOGGER.error("Error While sending unknown Request of DWH Kafka Topic ", e.getMessage());
                    return;
                }
                EventUtils.pushTheiaEvents(eventName, new ImmutablePair<>(
                        "Error While sending Request of DWH Kafka Topic", e.getMessage()));
            }
        }
    }

    private void pushCopRequestToDwhKafkaTopic(CreateOrderAndPayRequestBean createOrderAndPayRequestBean, String mid) {
        try {
            payRequestNotifierService.pushPayloadToKafkaTopic(createOrderAndPayRequestBean);
        } catch (Exception e) {
            boolean logStackTrace = ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.NOTIFY_PAYMENT_DWH_STACKTRACE,
                    mid);
            if (logStackTrace)
                LOGGER.error("Error While sending Cop Request of DWH Kafka Topic", e);
            else {
                EventUtils.pushTheiaEvents(EventNameEnum.COP_KAFKA_ERROR, new ImmutablePair<>(
                        "Error While sending Cop Request of DWH Kafka Topic", e.getMessage()));
            }
        }

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

    private CheckoutPromoServiceResponse getPromoCheckoutResponseV1FromV2(CheckoutPromoServiceResponseV2 response) {
        CheckoutPromoServiceResponse responseV1 = new CheckoutPromoServiceResponse();
        ApplyPromoResponseData data = new ApplyPromoResponseData();
        data.setStatus(1);
        data.setPromotext(response.getData().getPromotext());
        data.setPromoVisibility(response.getData().isPromoVisibility());
        data.setResponseCode(response.getData().getResponseCode());
        List<PromoSaving> savings = new ArrayList<PromoSaving>();
        String promoCode = null;
        if (response.getData().getSavings() != null) {
            savings.addAll(response.getData().getSavings());
            promoCode = response.getData().getPromocode();
        } else if (response.getData().getPromoResponse() != null) {
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
        }
        data.setSavings(savings);
        data.setPromocode(promoCode);
        responseV1.setData(data);
        return responseV1;
    }

    private boolean isSkuBasedOffer(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.getInitiateTransactionRequest() != null
                && workFlowRequestBean.getInitiateTransactionRequest().getBody() != null) {
            InitiateTransactionRequestBody initiateRequest = workFlowRequestBean.getInitiateTransactionRequest()
                    .getBody();
            if ((initiateRequest.getSimplifiedSubvention() != null && CollectionUtils.isNotEmpty(initiateRequest
                    .getSimplifiedSubvention().getItems()))
                    || (initiateRequest.getSimplifiedPaymentOffers() != null
                            && initiateRequest.getSimplifiedPaymentOffers().getCartDetails() != null && CollectionUtils
                                .isNotEmpty(initiateRequest.getSimplifiedPaymentOffers().getCartDetails().getItems()))
                    || (initiateRequest.getPaymentOffersAppliedV2() != null
                            && MapUtils.isNotEmpty(initiateRequest.getPaymentOffersAppliedV2().getPromoContext()) && initiateRequest
                            .getPaymentOffersAppliedV2().getPromoContext().get(CART) != null)
                    || (initiateRequest.getPaymentOffersApplied() != null
                            && initiateRequest.getPaymentOffersApplied().getCartDetails() != null && CollectionUtils
                                .isNotEmpty(initiateRequest.getPaymentOffersApplied().getCartDetails().getItems()))
                    || CollectionUtils.isNotEmpty(workFlowRequestBean.getItemsICB())) {
                return true;
            }
        }
        return false;
    }

    public boolean isICBFlow(WorkFlowRequestBean workFlowRequestBean) {
        boolean isICB = false;
        if (!(ff4jUtil.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), ENABLE_THEIA_EMI_ICB, false)
                && (workFlowRequestBean.isFullPg2TrafficEnabled()) && (workFlowRequestBean.getPaymentTypeId()
                .equals(PaymentTypeIdEnum.EMI.value)))) {
            return false;
        }
        CheckoutPromoServiceResponseV2 response = workFlowRequestBean.getPromoCheckoutInfo();
        GenericEmiSubventionResponse<CheckOutResponse> emiSubventionCheckoutGenericResponse = workFlowRequestBean
                .getSubventionCheckoutInfo();
        boolean isSkuBasedOffer = isSkuBasedOffer(workFlowRequestBean);
        String orderType;
        boolean redemptionTypeDiscount = false;
        if (response != null && response.getData() != null) {
            List<PromoSaving> savings = response.getData().getSavings();
            if (CollectionUtils.isNotEmpty(savings)) {
                for (PromoSaving saving : savings) {
                    if (REDEMPTION_TYPE_DISCOUNT.equals(saving.getRedemptionType())) {
                        redemptionTypeDiscount = true;
                        break;
                    }
                }
            }
            if (redemptionTypeDiscount) {
                if (response.getData().getPromoResponse() != null) {
                    for (Map.Entry<String, PromoResponseData> entry : response.getData().getPromoResponse().entrySet()) {
                        if (entry.getValue() != null && entry.getValue().getBankOfferContriInfo() != null) {
                            BankOfferContriInfo offer = entry.getValue().getBankOfferContriInfo();
                            if (offer.getBank() > 0 || offer.getBrand() > 0 || offer.getPlatform() > 0) {
                                workFlowRequestBean.setICBFlow(true);
                                isICB = true;
                                if (isSkuBasedOffer && offer.getBrand() > 0) {
                                    orderType = OrderType.BRAND_EMI_ORDER.getValue();
                                    workFlowRequestBean.setOrderType(orderType);
                                    return isICB;
                                }
                            }
                        }
                    }
                } else if (response.getData().getPromoContext() != null) {
                    String promoResponse = response.getData().getPromoContext().get("promoResponse");
                    if (promoResponse != null) {
                        try {
                            Map<String, String> promoResponseMap = JsonMapper.mapJsonToObject(promoResponse, Map.class);
                            for (String promoResponseMapData : promoResponseMap.values()) {
                                PromoResponseData data = JsonMapper.mapJsonToObject(promoResponseMapData,
                                        PromoResponseData.class);
                                BankOfferContriInfo offer = data.getBankOfferContriInfo();
                                if (offer.getBank() > 0 || offer.getBrand() > 0 || offer.getPlatform() > 0) {
                                    workFlowRequestBean.setICBFlow(true);
                                    isICB = true;
                                    if (isSkuBasedOffer && offer.getBrand() > 0) {
                                        orderType = OrderType.BRAND_EMI_ORDER.getValue();
                                        workFlowRequestBean.setOrderType(orderType);
                                        return isICB;
                                    }
                                }
                            }
                        } catch (FacadeCheckedException ex) {
                            LOGGER.error("Error while mapping PromoResponseData to check BankOfferContriInfo :{} ",
                                    promoResponse, ex);

                        }
                    }
                }
            }
        }

        redemptionTypeDiscount = false;
        if (emiSubventionCheckoutGenericResponse != null && emiSubventionCheckoutGenericResponse.getData() != null) {
            List<ItemBreakUp> items = emiSubventionCheckoutGenericResponse.getData().getItemBreakUp();
            if (CollectionUtils.isNotEmpty(items)) {
                for (ItemBreakUp item : items) {
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
                            workFlowRequestBean.setICBFlow(true);
                            isICB = true;
                            if (isSkuBasedOffer && amountBearer.getBrand() > 0) {
                                orderType = OrderType.BRAND_EMI_ORDER.getValue();
                                workFlowRequestBean.setOrderType(orderType);
                                return isICB;
                            }
                        }
                    }
                }
            }
        }
        if (isICB) {
            orderType = OrderType.BANK_EMI_ORDER.getValue();
            workFlowRequestBean.setOrderType(orderType);
        }
        return isICB;
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

    private boolean isFeeApplicableOnAddAndPayTxn(WorkFlowTransactionBean flowTransactionBean) {
        boolean ret = false;
        if ((!flowTransactionBean.getWorkFlowBean().isUpiConvertedToAddNPay())
                && (EPayMode.ADDANDPAY.equals(flowTransactionBean.getWorkFlowBean().getPaytmExpressAddOrHybrid()))
                && (ff4JUtils.isFeatureEnabledOnMid(flowTransactionBean.getWorkFlowBean().getPaytmMID(),
                        Ff4jFeature.ADD_MONEY_FEE_ON_ADDNPAY_TXN, false) && !(EPayMethod.CREDIT_CARD.getMethod()
                        .equals(flowTransactionBean.getWorkFlowBean().getPayMethod()) && flowTransactionBean
                        .getWorkFlowBean().isDisableLimitCCAddNPay()))) {
            ret = true;
        }
        return ret;
    }

    public BinDetail getBinDetail(Long binNumber) {
        return mappingUtility.getBinDetail(binNumber);
    }

    private void setMobileNumberInPaymentPromoCheckoutData(WorkFlowRequestBean workFlowRequestBean,
            CheckoutPromoServiceResponse responseV1) {
        List<PromoSaving> promoSavings = responseV1.getData().getSavings();
        if (REDEMPTION_TYPE_PAYTM_CASHBACK.equalsIgnoreCase(promoSavings.get(0).getRedemptionType())) {
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

    public void setChallanIdNum(PaymentRequestBean paymentRequestData) {
        if (paymentRequestData.getExtraParamsMap() == null) {
            paymentRequestData.setExtraParamsMap(new HashMap<>());
        }
        paymentRequestData.getExtraParamsMap().put("challanIdNum", generateCIN(TheiaConstant.RequestParams.BSR_CODE));
    }

    public String generateCIN(Object bsrCode) {
        /*
         * BSR Code(7)+ Running Sequence No.(6) (Sequence No. would reset on
         * daily basis)+ Date of payment(DDMMYYYY)(8)
         */
        StringBuilder sb = new StringBuilder();
        sb.append(bsrCode);
        sb.append(String.format("%06d", atomicSequenceGenerator.getNext()));
        sb.append(getCINDate());
        return sb.toString();
    }

    private String getCINDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        return LocalDate.now().format(dateFormatter);
    }

    private GenericCoreResponseBean<BizPayResponse> getGenericPayResponse(WorkFlowTransactionBean workFlowTransBean) {
        LOGGER.debug("WorkFlowTransactionBean : {}", workFlowTransBean);

        // Calling velocity service
        BizPayRequest payRequest = null;
        BizAoaPayRequest aoaPayRequest = null;
        setMerchantVeloctyLimitInExtendInfo(workFlowTransBean.getWorkFlowBean());
        if (isFeeApplicableOnAddAndPayTxn(workFlowTransBean)) {
            GenericCoreResponseBean<?> walletConsultResponse = getForFeeOnAddNPayTransaction(workFlowTransBean);
            if (!walletConsultResponse.isSuccessfullyProcessed()
                    && ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.equals(walletConsultResponse
                            .getResponseConstant())) {
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.getCode(),
                        ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.getMessage(), null, true);
                return convertRiskVerificationToRiskReject(walletConsultResponse.getRiskRejectUserMessage());
            } else if (!walletConsultResponse.isSuccessfullyProcessed()) {
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.getCode(),
                        FailureLogs.ADDMONEY_NOT_ALLOWED_OR_FAILED, null, true);
                return new GenericCoreResponseBean<>(FailureLogs.ADDMONEY_NOT_ALLOWED_OR_FAILED,
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }
        // retryCount should be 1 as for native this method will be called only
        // for retry case
        // sending retryCount more than 0 to promo, will ensure depuplication of
        // promo checkout
        PromoCheckoutFeatureFF4jData checkoutFeatureFF4jData = null;
        boolean migrateBankOffersPromo = ff4JUtil.isMigrateBankOffersPromo(workFlowTransBean.getWorkFlowBean()
                .getPaytmMID());
        if (!migrateBankOffersPromo) {
            checkoutFeatureFF4jData = checkoutPaymentOfferUpdateOrderAmountAndExtendInfo(workFlowTransBean, 1, true);
        } else {
            checkoutFeatureFF4jData = checkoutPaymentOfferUpdateOrderAmountAndExtendInfoV2(workFlowTransBean, 0, true);
        }
        simplifiedSubventionHelper.validateSimplifiedEmi(workFlowTransBean);
        if (isSimplifiedFlow(workFlowTransBean.getWorkFlowBean())
                && (workFlowTransBean.isAddMoneyPcfEnabled() || workFlowTransBean.getWorkFlowBean().isPostConvenience())) {
            workFlowTransBean
                    .setConsultFeeResponse(consultBulkFeeResponseForPay(workFlowTransBean, null).getResponse());
            workFlowTransBean.getWorkFlowBean().setChargeAmount(
                    workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(workFlowTransBean));
        }
        try {
            if (workFlowTransBean.getWorkFlowBean().isFromAoaMerchant()) {
                modifyOrder(workFlowTransBean);
                aoaPayRequest = workRequestCreator.createBizAoaPayRequest(workFlowTransBean);
            } else {
                applyEmiSubventionOfferV2(workFlowTransBean.getWorkFlowBean());
                if (isICBFlow(workFlowTransBean.getWorkFlowBean())) {
                    com.paytm.pgplus.facade.enums.PayMethod payMethod = null;
                    try {
                        payMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(workFlowTransBean
                                .getWorkFlowBean().getPayMethod());
                    } catch (FacadeInvalidParameterException e) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e));
                        failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
                        throw new BizPaymentOfferCheckoutException();
                    }
                    checkoutPaymentOfferAtAffordability(workFlowTransBean.getWorkFlowBean(), null, payMethod);
                    workFlowTransBean.setModifyOrderRequired(true);
                }
                modifyOrder(workFlowTransBean);
                payRequest = workRequestCreator.createBizPayRequest(workFlowTransBean);
            }
        } catch (PaytmValidationException pve) {
            if (isNotExemptedPayMode(workFlowTransBean.getWorkFlowBean())) {
                rollbackMerchantVelocityLimitUpdate(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                        workFlowTransBean.getWorkFlowBean().getTxnAmount(), workFlowTransBean.getWorkFlowBean()
                                .getExtendInfo(), true);
            }
            if (ff4JUtil.isFeatureEnabled(SAVE_PROMO_CHECKOUT_DATA, workFlowTransBean.getWorkFlowBean().getPaytmMID())) {
                rollbackPaymentOfferCheckoutV2(workFlowTransBean.getWorkFlowBean(),
                        !checkoutFeatureFF4jData.isFeaturePromoSaveCheckoutDataFlagEnabled());
            }
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_PAYMENT_DETAILS.getCode(),
                    ResponseConstants.INVALID_PAYMENT_DETAILS.getMessage(), null, true);
            return new GenericCoreResponseBean<>(getErrorMsgToReturnPaytmValidationExp(pve),
                    ResponseConstants.INVALID_PAYMENT_DETAILS);
        } catch (BaseException | MappingServiceClientException e) {
            LOGGER.error("Error occured while creating Pay Request: {}", e);
            failureLogUtil
                    .setFailureMsgForDwhPush(ResponseConstants.SYSTEM_ERROR.getCode(), e.getMessage(), null, true);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
        if (payRequest != null && PaymentAdapterUtil.eligibleForPG2(payRequest.getExtInfo())) {
            Integer pg2PaymentCountValue = null;
            try {
                pg2PaymentCountValue = (Integer) theiaSessionRedisUtil.hget(workFlowTransBean.getWorkFlowBean()
                        .getTxnToken(), "pg2PaymentCount");
            } catch (Exception e) {
                LOGGER.info("Exception while getting pg2PaymentCount from redis: {}", e.getMessage());
            }
            if (pg2PaymentCountValue != null) {
                return new GenericCoreResponseBean<>("Cannot retry on pg2 routed payment",
                        ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
            } else if (pg2Utilities.disablePg2Retry(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                    workFlowTransBean.getWorkFlowBean().isFullPg2TrafficEnabled())) {
                pg2Utilities.setPg2PaymentCount(workFlowTransBean.getWorkFlowBean().getTxnToken());
            }
        }
        GenericCoreResponseBean<BizPayResponse> payResponse = null;
        if (workFlowTransBean.getWorkFlowBean().isFromAoaMerchant()) {
            pushRequestToDwhKafkaTopic(aoaPayRequest, workFlowTransBean.getWorkFlowBean().getPaytmMID());
            payResponse = bizPaymentService.aoaPay(aoaPayRequest);
        } else {
            // setting international payment card flag
            if (EPayMethod.DEBIT_CARD.getMethod().equals(workFlowTransBean.getWorkFlowBean().getPayMethod())
                    || EPayMethod.CREDIT_CARD.getMethod().equals(workFlowTransBean.getWorkFlowBean().getPayMethod())
                    || EPayMethod.EMI.getMethod().equals(workFlowTransBean.getWorkFlowBean().getPayMethod())) {
                setInternationalCardPaymentFlag(payRequest.getPayOptionBills(), workFlowTransBean,
                        payRequest.getRiskExtendInfo());

            }

            /*
             * check for ForceDirect OTP in case of Direct Bank
             */
            if (checkForceDirectChannel(workFlowTransBean.getWorkFlowBean().getForceDirectChannel())) {
                setForceDirectChannel(payRequest, workFlowTransBean);
            } else {
                // if DirectBank redirect request
                if (workFlowTransBean.getWorkFlowBean().isDirectBankCardFlow()) {
                    disableDirectChannelFlag(payRequest);
                }
                // Setting Flag if DirectBank Request
                setDirectChannelRequest(payRequest, workFlowTransBean);
            }

            pushRequestToDwhKafkaTopic(payRequest, workFlowTransBean.getWorkFlowBean().getPaytmMID());
            payResponse = bizPaymentService.pay(payRequest);
        }
        LOGGER.debug("Pay response is : {}", payResponse);
        if (!payResponse.isSuccessfullyProcessed()) {
            if (!migrateBankOffersPromo) {
                rollbackPaymentOfferCheckout(workFlowTransBean.getWorkFlowBean(),
                        !checkoutFeatureFF4jData.isFeaturePromoSaveCheckoutDataFlagEnabled());
            } else {
                rollbackPaymentOfferCheckoutV2(workFlowTransBean.getWorkFlowBean(),
                        !checkoutFeatureFF4jData.isFeaturePromoSaveCheckoutDataFlagEnabled());
            }
            if (isNotExemptedPayMode(workFlowTransBean.getWorkFlowBean())) {
                rollbackMerchantVelocityLimitUpdate(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                        workFlowTransBean.getWorkFlowBean().getTxnAmount(), workFlowTransBean.getWorkFlowBean()
                                .getExtendInfo(), true);
            }
            LOGGER.error("Pay Failed Due to Reason :{}", payResponse.getFailureMessage());
            if (StringUtils.isNotBlank(payResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                        payResponse.getResponseConstant(), payResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                return responseBean;
            } else if (payResponse.getResponseConstant() != null
                    && ResponseConstants.RISK_VERIFICATION.equals(payResponse.getResponseConstant())) {
                if (ERequestType.NATIVE != workFlowTransBean.getWorkFlowBean().getRequestType()) {
                    return convertRiskVerificationToRiskReject();
                }
                RiskVerifierCacheProcessingPayload riskDoViewResponse = setRiskDoViewResponseInCache(workFlowTransBean,
                        workFlowTransBean.getWorkFlowBean().getTransID(), payResponse.getResponse()
                                .getSecurityPolicyResult());
                if (!riskDoViewResponse.isSuccessful()) {
                    return convertRiskVerificationToRiskReject(riskDoViewResponse.getMessage());
                }

            }
            return new GenericCoreResponseBean<>(payResponse.getFailureMessage(), payResponse.getResponseConstant());
        }
        if (checkoutFeatureFF4jData.isSuccess() && checkoutFeatureFF4jData.isFeaturePromoCheckoutRetryFlagEnabled()) {
            LOGGER.info("setting promo checkout retry count 1");
            theiaSessionRedisUtil.hsetIfExist(workFlowTransBean.getWorkFlowBean().getTxnToken(),
                    PROMO_CHECKOUT_RETRY_COUNT, 1);
        }
        LOGGER.info("Cashier pay is successfully processed");
        return payResponse;
    }

    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> getGenericCopResponse(
            WorkFlowTransactionBean flowTransBean, boolean isRenewSubscriptionRequest) {
        WorkFlowRequestBean workFlowRequestBean = flowTransBean.getWorkFlowBean();
        if (isFeeApplicableOnAddAndPayTxn(flowTransBean)) {
            GenericCoreResponseBean<?> walletConsultResponse = getForFeeOnAddNPayTransaction(flowTransBean);
            if (!walletConsultResponse.isSuccessfullyProcessed()
                    && ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.equals(walletConsultResponse
                            .getResponseConstant())) {
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.getCode(),
                        ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH.getMessage(), null, true);
                return convertRiskVerificationToRiskReject(walletConsultResponse.getRiskRejectUserMessage());
            } else if (!walletConsultResponse.isSuccessfullyProcessed()) {
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED.getCode(),
                        FailureLogs.ADDMONEY_NOT_ALLOWED_OR_FAILED, null, true);
                return new GenericCoreResponseBean<>(FailureLogs.ADDMONEY_NOT_ALLOWED_OR_FAILED,
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }
        // boolean usePromoV1 =
        // Boolean.parseBoolean(ConfigurationUtil.getProperty(USE_PROMO_V1,
        // "true"));
        PromoCheckoutFeatureFF4jData promoCheckoutFeatureFF4jData = new PromoCheckoutFeatureFF4jData();
        if (!isInternalPaymentRetry(workFlowRequestBean)) {
            if (!ff4JUtil.isMigrateBankOffersPromo(workFlowRequestBean.getPaytmMID())) {
                promoCheckoutFeatureFF4jData = checkoutPaymentOfferUpdateOrderAmountAndExtendInfo(flowTransBean, 0,
                        false);
            } else {
                promoCheckoutFeatureFF4jData = checkoutPaymentOfferUpdateOrderAmountAndExtendInfoV2(flowTransBean, 0,
                        false);
            }
        }

        /*
         * 
         * Applying emi subvention offer
         */
        simplifiedSubventionHelper.validateSimplifiedEmi(flowTransBean);
        applyEmiSubventionOfferV2(workFlowRequestBean);
        if (isICBFlow(workFlowRequestBean)) {
            com.paytm.pgplus.facade.enums.PayMethod payMethod = null;
            try {
                payMethod = com.paytm.pgplus.facade.enums.PayMethod.getPayMethodByMethod(workFlowRequestBean
                        .getPayMethod());
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
                throw new BizPaymentOfferCheckoutException();
            }
            checkoutPaymentOfferAtAffordability(workFlowRequestBean, null, payMethod);
        }
        if (isSimplifiedFlow(flowTransBean.getWorkFlowBean())
                && (flowTransBean.isAddMoneyPcfEnabled() || flowTransBean.getWorkFlowBean().isPostConvenience())) {
            flowTransBean.setConsultFeeResponse(consultBulkFeeResponseForPay(flowTransBean, null).getResponse());
            flowTransBean.getWorkFlowBean().setChargeAmount(
                    workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(flowTransBean));
        }
        CreateOrderAndPayRequestBean createOrderAndPayRequestBean = null;
        try {
            createOrderAndPayRequestBean = workRequestCreator.createOrderAndPayRequestBean(flowTransBean,
                    isRenewSubscriptionRequest);
        } catch (PaytmValidationException e) {
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
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_PAYMENT_DETAILS.getCode(), e.getMessage(),
                    null, true);
            return new GenericCoreResponseBean<>(errorMessage.toString(), ResponseConstants.INVALID_PAYMENT_DETAILS);
        } catch (AccountMismatchException | AccountNotExistsException e) {
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception occured while creating createOrderAndPay request ",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            failureLogUtil
                    .setFailureMsgForDwhPush(ResponseConstants.SYSTEM_ERROR.getCode(), e.getMessage(), null, true);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
        // setting international payment flag
        if (EPayMethod.DEBIT_CARD.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod())
                || EPayMethod.CREDIT_CARD.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod())
                || EPayMethod.EMI.getMethod().equals(flowTransBean.getWorkFlowBean().getPayMethod())) {
            setInternationalCardPaymentFlag(createOrderAndPayRequestBean.getPaymentInfo().getPayOptionBills(),
                    flowTransBean, createOrderAndPayRequestBean.getRiskExtendInfo());
        }

        setDirectChannelRequest(createOrderAndPayRequestBean, flowTransBean);
        setMerchantVeloctyLimitInExtendInfo(workFlowRequestBean);

        pushRequestToDwhKafkaTopic(createOrderAndPayRequestBean, workFlowRequestBean.getPaytmMID());
        GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = orderService
                .createOrderAndPay(createOrderAndPayRequestBean);

        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
            if (!ff4JUtil.isMigrateBankOffersPromo(workFlowRequestBean.getPaytmMID())) {
                rollbackPaymentOfferCheckout(workFlowRequestBean,
                        !promoCheckoutFeatureFF4jData.isFeaturePromoSaveCheckoutDataFlagEnabled());
            } else {
                rollbackPaymentOfferCheckoutV2(workFlowRequestBean,
                        !promoCheckoutFeatureFF4jData.isFeaturePromoSaveCheckoutDataFlagEnabled());

            }
            if (isNotExemptedPayMode(workFlowRequestBean)) {
                rollbackMerchantVelocityLimitUpdate(workFlowRequestBean.getPaytmMID(),
                        workFlowRequestBean.getTxnAmount(), workFlowRequestBean.getExtendInfo(), true);
            }
            LOGGER.error("CreateOrderAndPay is failed due to reason : {}",
                    createOrderAndPayResponse.getFailureMessage());
            EventUtils.pushTheiaEvents(EventNameEnum.CREATE_ORDER_AND_PAY_FAILED, new ImmutablePair<>(
                    "CreateOrderAndPay is failed due to reason ", createOrderAndPayResponse.getFailureMessage()));
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

            if (createOrderAndPayResponse.getResponseConstant() != null
                    && ResponseConstants.NEED_RISK_CHALLENGE.equals(createOrderAndPayResponse.getResponseConstant())) {
                if (ERequestType.NATIVE != flowTransBean.getWorkFlowBean().getRequestType()) {
                    return convertRiskVerificationToRiskReject();
                }
                RiskVerifierCacheProcessingPayload riskDoViewResponse = setRiskDoViewResponseInCache(flowTransBean,
                        createOrderAndPayResponse.getResponse().getAcquirementId(), createOrderAndPayResponse
                                .getResponse().getSecurityPolicyResult());
                if (riskDoViewResponse.isSuccessful()) {
                    responseBean.setAcquirementId(createOrderAndPayResponse.getResponse().getAcquirementId());
                } else {
                    return convertRiskVerificationToRiskReject(riskDoViewResponse.getMessage());
                }

            }
            if (StringUtils.isNotBlank(createOrderAndPayResponse.getAcquirementId())) {
                responseBean.setAcquirementId(createOrderAndPayResponse.getAcquirementId());
            }
            return responseBean;
        }
        if (promoCheckoutFeatureFF4jData != null && promoCheckoutFeatureFF4jData.isSuccess()
                && promoCheckoutFeatureFF4jData.isFeaturePromoCheckoutRetryFlagEnabled()) {
            LOGGER.info("setting promo checkout retry count 1");
            theiaSessionRedisUtil.hsetIfExist(workFlowRequestBean.getTxnToken(), PROMO_CHECKOUT_RETRY_COUNT, 1);
        }
        return createOrderAndPayResponse;
    }

    public String getCardTypeByOldName(String cardType) {
        if (EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(cardType)) {
            return EPayMethod.CREDIT_CARD.getOldName();
        }
        if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(cardType)) {
            return EPayMethod.DEBIT_CARD.getOldName();
        }
        return cardType;
    }

    public VerificationType getVerificationType(WorkFlowRequestBean workFlowRequestBean) {

        VerificationType verificationType = null;
        if ((ff4JUtils
                .isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), ENABLE_TPV_FOR_ALL_REQUEST_TYPES, false))
                || BFSI_MERCHANTS_REQUEST_TYPES_SET.contains(workFlowRequestBean.getRequestType())
                || BFSI_MERCHANTS_REQUEST_TYPES_SET.contains(workFlowRequestBean.getSubRequestType())) {
            String accountNumber = workFlowRequestBean.getAccountNumber();
            String validateAccountNumber = workFlowRequestBean.getValidateAccountNumber();
            String allowUnverifiedAccount = workFlowRequestBean.getAllowUnverifiedAccount();

            verificationType = (StringUtils.isNotBlank(accountNumber) && StringUtils.isNotBlank(validateAccountNumber)
                    && StringUtils.isNotBlank(allowUnverifiedAccount)
                    && StringUtils.equalsIgnoreCase(validateAccountNumber, "true") && StringUtils.equalsIgnoreCase(
                    allowUnverifiedAccount, "false")) ? VerificationType.TPV : VerificationType.NON_TPV;

            EXT_LOGGER.customInfo("Adding verificationType as : {}", verificationType);
        } else {
            verificationType = VerificationType.NON_TPV;
        }
        return verificationType;
    }

    public EPayMode checkPayModeAndReturn(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {

        return pgPlusWalletDecisionMakerService.checkPayModeAndReturn(litePayviewConsultResponseBizBean);
    }

    private void setWalletAccountStatus(WalletBalanceResponseData walletBalanceResponseData,
            WorkFlowTransactionBean workFlowTransactionBean) {
        if (walletBalanceResponseData != null && walletBalanceResponseData.getUserWalletFreezeDetails() != null
                && !walletBalanceResponseData.getUserWalletFreezeDetails().isActive()) {
            workFlowTransactionBean.setWalletInactive(true);
        }
        if (walletBalanceResponseData != null && walletBalanceResponseData.isEmbargoApplied()) {
            workFlowTransactionBean.setWalletInactive(true);
            workFlowTransactionBean.setWalletNewUser(true);
        }
        if (walletBalanceResponseData != null && walletBalanceResponseData.getUserWalletFreezeDetails() != null
                && walletBalanceResponseData.getUserWalletFreezeDetails().getFreezeStatus() != 0) {
            workFlowTransactionBean.getWorkFlowBean().setUserWalletFreezeDetails(
                    walletBalanceResponseData.getUserWalletFreezeDetails());
        }

    }

    private void populateTwoFADetails(BalanceChannelInfoBiz balanceChannelInfoBiz,
            WalletBalanceResponseData walletBalanceResponseData, WorkFlowTransactionBean workFlowTransBean) {
        if (walletBalanceResponseData != null && walletBalanceResponseData.getTwoFaDetails() != null) {
            TwoFARespData responseTwoFAConfig = new TwoFARespData();
            responseTwoFAConfig.setTwoFAEnabled(walletBalanceResponseData.getTwoFaDetails().isTwoFaEnabled());
            responseTwoFAConfig.setPassCodeExist(walletBalanceResponseData.getTwoFaDetails().isPassCodeExist());
            responseTwoFAConfig.setAmount(String.valueOf(walletBalanceResponseData.getTwoFaDetails().getAmount()));
            balanceChannelInfoBiz.setTwoFAConfig(responseTwoFAConfig);
            workFlowTransBean.setTwoFAConfig(responseTwoFAConfig);
        }
    }

    public void enrichRequestBeanExtendInfo(WorkFlowTransactionBean workFlowTransBean) {

        if (null != workFlowTransBean.getWorkFlowBean().getExtendInfo() && null != workFlowTransBean.getUserDetails()
                && null != workFlowTransBean.getUserDetails().getUserId()) {
            workFlowTransBean.getWorkFlowBean().getExtendInfo()
                    .setPaytmUserId(workFlowTransBean.getUserDetails().getUserId());
        }

    }

    public FetchAccountBalanceResponse fetchUserAccountDetails(
            FetchPPBLUserBalanceResponse fetchPPBLUserBalanceResponse, String mid) {
        FetchAccountBalanceResponse fetchAccountBalanceResponse = null;
        if (fetchPPBLUserBalanceResponse.getStatus().equals(PPBL_FAILURE_STATUS)) {
            fetchAccountBalanceResponse = new FetchAccountBalanceResponse(PPBL_FAILURE_STATUS);
        } else {
            List<UserAccountDetails> accountDetails = fetchPPBLUserBalanceResponse.getAccountDetails();
            if (CollectionUtils.isNotEmpty(accountDetails)) {
                if (accountDetails.size() > 1) {
                    // This is flipping strategy for giving priority to ica
                    // account over isa account
                    if (ff4JUtils.isFeatureEnabledOnMid(mid, THEIA_PPBL_ACCOUNT_TYPE_STRATEGY, false)) {
                        String accountType = ff4JUtils.getPropertyAsStringWithDefault(THEIA_PPBL_ACCOUNT_TYPE_VALUE,
                                SAVING_ACCOUNT);
                        UserAccountDetails userAccountDetails = accountDetails.stream()
                                .filter(x -> accountType.equalsIgnoreCase(x.getAccountType())).findAny().orElse(null);
                        if (userAccountDetails != null) {
                            fetchAccountBalanceResponse = mapToFetchBalanceResponse(userAccountDetails);
                        }
                        LOGGER.info("Fetching account details based on account type strategy");
                    }
                    // This is default strategy to give priority to higher
                    // balance account
                    else {
                        Double previousBalance = -Double.MAX_VALUE;
                        Double currentBalance;
                        UserAccountDetails finalUserAccountDetails = null;
                        for (UserAccountDetails userAccountDetails : accountDetails) {
                            currentBalance = userAccountDetails.getEffectiveBalance();
                            if (currentBalance.compareTo(previousBalance) == 1) {
                                finalUserAccountDetails = userAccountDetails;
                            }
                            previousBalance = currentBalance;
                        }
                        if (finalUserAccountDetails != null) {
                            fetchAccountBalanceResponse = mapToFetchBalanceResponse(finalUserAccountDetails);
                        }
                        LOGGER.info("Fetching user account details");
                    }
                }
                // If user has only one account then it will be considered as
                // default account
                else if (accountDetails.size() == 1) {
                    UserAccountDetails userAccountDetails = accountDetails.get(0);
                    fetchAccountBalanceResponse = mapToFetchBalanceResponse(userAccountDetails);
                    LOGGER.info("Fetching Default account details");
                }
            }
        }
        return fetchAccountBalanceResponse;
    }

    public FetchAccountBalanceResponse mapToFetchBalanceResponse(UserAccountDetails userAccountDetails) {
        FetchAccountBalanceResponse fetchAccountBalanceResponse = new FetchAccountBalanceResponse();
        fetchAccountBalanceResponse.setAccountType(userAccountDetails.getAccountType());
        fetchAccountBalanceResponse.setAccountState(userAccountDetails.getAccountStatus());
        fetchAccountBalanceResponse.setAccountNumber(userAccountDetails.getAccountNumber());
        fetchAccountBalanceResponse.setRedemptionAllowed(userAccountDetails.getIsFDRedemptionAllowed());
        fetchAccountBalanceResponse.setRedeemableInvestmentBalance(userAccountDetails.getRedeemableInvestmentBalance());
        fetchAccountBalanceResponse.setSlfdBalance(userAccountDetails.getSlfdBalance());
        fetchAccountBalanceResponse.setEffectiveBalance(userAccountDetails.getEffectiveBalance());
        return fetchAccountBalanceResponse;
    }

    public WorkFlowResponseBean returnResponseForOrderNotFound(final WorkFlowRequestBean flowRequestBean) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        UPIPSPResponseBody upiPspResponse = new UPIPSPResponseBody();
        upiPspResponse.setOrderId(flowRequestBean.getOrderID());
        upiPspResponse.setResultCode(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultCode());
        upiPspResponse.setResultCodeId(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultCodeId());
        upiPspResponse.setResultMsg(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultMsg());
        upiPspResponse.setTxnAmount(AmountUtils.getTransactionAmountInRupee(flowRequestBean.getTxnAmount()));
        upiPspResponse.setRequestMsgId(flowRequestBean.getUpiPspReqMsgId());
        upiPspResponse.setMid(flowRequestBean.getPaytmMID());
        workFlowResponseBean.setUpiPSPResponse(upiPspResponse);
        return workFlowResponseBean;
    }

    public String getSubscriptionKey(String subsId) {
        return "QR_SUBSCRIPTION" + "_" + subsId;
    }

    private GenericCoreResponseBean<CacheCardResponseBean> mapToCacheCardResponse(SaveAssetResponse saveAssetResponse,
            WorkFlowTransactionBean flowTransactionBean) {
        CacheCardResponseBean cacheCardResponseBean = null;
        if (saveAssetResponse != null && saveAssetResponse.getResponseBody() != null) {
            String tokenId = saveAssetResponse.getResponseBody().getCacheAssetId();
            cacheCardResponseBean = new CacheCardResponseBean(tokenId, null, saveAssetResponse.getResponseBody()
                    .getGlobalPanIndex());
            if (Objects.nonNull(flowTransactionBean) && Objects.nonNull(flowTransactionBean.getWorkFlowBean())
                    && StringUtils.isBlank(flowTransactionBean.getWorkFlowBean().getGcin())
                    && StringUtils.isNotBlank(saveAssetResponse.getResponseBody().getGlobalPanIndex())) {
                if (flowTransactionBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                        || flowTransactionBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                        || flowTransactionBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)
                        || ff4JUtils.isFeatureEnabledOnMid(flowTransactionBean.getWorkFlowBean().getPaytmMID(),
                                ENABLE_ROUTE_CACHE_CARD_FOR_ALL_PAYMODES, false)) {
                    flowTransactionBean.getWorkFlowBean().setGcin(
                            saveAssetResponse.getResponseBody().getGlobalPanIndex());
                }
            }
        }
        return new GenericCoreResponseBean<>(cacheCardResponseBean);
    }

    private GenericCoreResponseBean<CacheCardResponseBean> mapToCacheCardResponseForBankTransfer(
            SaveAssetResponse saveAssetResponse, WorkFlowTransactionBean flowTransactionBean) {
        CacheCardResponseBean cacheCardResponseBean = null;
        if (saveAssetResponse != null && saveAssetResponse.getResponseBody() != null) {
            String tokenId = saveAssetResponse.getResponseBody().getCacheAssetId();
            cacheCardResponseBean = new CacheCardResponseBean(tokenId, null, saveAssetResponse.getResponseBody()
                    .getGlobalPanIndex());
        }
        return new GenericCoreResponseBean<>(cacheCardResponseBean);
    }

    public GenericCoreResponseBean<CacheCardResponseBean> fetchAssetIdforPG2Request(
            WorkFlowTransactionBean flowTransBean, CacheCardRequest cacheCardRequest) {
        try {
            SaveAssetRequest saveAssetRequest = workFlowRequestCreationHelper.createSaveAssetRequest(flowTransBean,
                    cacheCardRequest);
            SaveAssetResponse saveAssetResponse = assetCenterService.saveAsset(saveAssetRequest,
                    environment.getProperty(CLIENT_SECRET_KEY));
            EXT_LOGGER.customInfo("Save Asset Response Received from Asset Center: {}", saveAssetResponse);
            if (saveAssetResponse != null
                    && !saveAssetResponse.getResponseStatus().getStatus().equalsIgnoreCase("SUCCESS")) {
                return new GenericCoreResponseBean<>(saveAssetResponse.getResponseStatus().getStatusMessage());
            }
            return mapToCacheCardResponse(saveAssetResponse, flowTransBean);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred while Executing Save Asset Api ", e);
        }
        return new GenericCoreResponseBean<CacheCardResponseBean>("Could not fetch Save Asset Response");
    }

    public GenericCoreResponseBean<CacheCardResponseBean> fetchAssetIdforPG2BankTransferRequest(
            WorkFlowTransactionBean flowTransBean, CacheCardRequest cacheCardRequest) {
        try {
            SaveAssetRequest saveAssetRequest = workFlowRequestCreationHelper.createSaveAssetRequestForBankTransfer(
                    flowTransBean, cacheCardRequest);
            SaveAssetResponse saveAssetResponse = assetCenterService.saveAsset(saveAssetRequest,
                    environment.getProperty(CLIENT_SECRET_KEY));
            EXT_LOGGER.customInfo("Save Asset Response Received from Asset Center For Bank Transfer : {}",
                    saveAssetResponse);
            if (saveAssetResponse != null
                    && !saveAssetResponse.getResponseStatus().getStatus().equalsIgnoreCase("SUCCESS")) {
                return new GenericCoreResponseBean<>(saveAssetResponse.getResponseStatus().getStatusMessage());
            }
            return mapToCacheCardResponseForBankTransfer(saveAssetResponse, flowTransBean);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred while Executing Save Asset Api For Bank Transfer ", e);
        }
        return new GenericCoreResponseBean<CacheCardResponseBean>(
                "Could not fetch Save Asset Response For Bank Transfer");
    }

    private Routes getRouteForCacheCard(final WorkFlowTransactionBean flowTransBean) {
        flowTransBean.getWorkFlowBean().setRoute(Routes.PG2);
        return Routes.PG2;
    }

    private void filterSubscriptionSupportedBankAccounts(WorkFlowTransactionBean workFlowTransBean) {
        if (ERequestType.isSubscriptionOrMFRequest(workFlowTransBean.getWorkFlowBean().getRequestType())) {
            UserProfileSarvatraV4 userProfileSarvatraV4 = workFlowTransBean.getSarvatraUserProfileV4();
            if (userProfileSarvatraV4.getRespDetails() != null
                    && userProfileSarvatraV4.getRespDetails().getProfileDetail() != null
                    && userProfileSarvatraV4.getRespDetails().getProfileDetail().getBankAccounts() != null) {
                // fetch bankAccounts having subscription present in featureList
                List<UpiBankAccountV4> upiFilteredBankAccountsHavingSubscription = workFlowTransBean
                        .getSarvatraUserProfileV4()
                        .getRespDetails()
                        .getProfileDetail()
                        .getBankAccounts()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(bankAccount -> Objects.nonNull(bankAccount.getBankAccountFeatureList())
                                && bankAccount.getBankAccountFeatureList().contains(SUBSCRIPTION))
                        .collect(Collectors.toList());
                userProfileSarvatraV4.getRespDetails().getProfileDetail()
                        .setBankAccounts(upiFilteredBankAccountsHavingSubscription);
            }
        }
    }

    public GenericCoreResponseBean<Boolean> fetchWorkflowIdWallet2FAWeb(WorkFlowTransactionBean workFlowTransBean) {
        if (fetchWorkflowIdWallet2FA(workFlowTransBean)) {
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        } else {
            LOGGER.error("Unable to fetch workflowId for wallet 2FA");
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
    }

    private boolean fetchWorkflowIdWallet2FA(WorkFlowTransactionBean workFlowTransBean) {
        if (workFlowTransBean != null && workFlowTransBean.getWorkFlowBean() != null
                && workFlowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getSsoToken())) {
            String txnToken = getTxnTokenFromCache(workFlowTransBean);
            Wallet2FAWorkflowIdRequest wallet2FAWorkflowIdRequest = workRequestCreator
                    .createWorkflowIdWallet2FARequest(workFlowTransBean, txnToken);
            Wallet2FAWorkflowIdResponse wallet2FAWorkflowIdResponse = bankOauthService
                    .fetchWorkflowIdForWallet2FAWeb(wallet2FAWorkflowIdRequest);
            return setWorkflowIdWallet2FAResponse(workFlowTransBean, wallet2FAWorkflowIdResponse);
        }
        return false;
    }

    private String getTxnTokenFromCache(WorkFlowTransactionBean workFlowTransBean) {
        if (workFlowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getOrderId())
                && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getMid())) {
            String orderId = workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getOrderId();
            String mid = workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getMid();
            StringBuilder midOrderIdKey = new StringBuilder(NATIVE_TXN_INITIATE_REQUEST);
            midOrderIdKey.append(mid).append("_").append(orderId);
            String txnToken = (String) theiaSessionRedisUtil.get(midOrderIdKey.toString());
            return txnToken;
        }
        return null;
    }

    private boolean setWorkflowIdWallet2FAResponse(WorkFlowTransactionBean workFlowTransBean,
            Wallet2FAWorkflowIdResponse wallet2FAWorkflowIdResponse) {
        if (wallet2FAWorkflowIdResponse != null && SUCCESS.equalsIgnoreCase(wallet2FAWorkflowIdResponse.getStatus())) {
            String clientId = VaultPropertyUtil.getProperty(BANK_OAUTH_CLIENT_ID);
            String redirectUri = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(RiskConstants.THEIA_BASE_URL)
                    + TheiaConstant.ExtraConstants.WALLET_2FA_SET_PASSCODE_REDIRECT;
            workFlowTransBean.getTwoFAConfig().setWorkflowId(wallet2FAWorkflowIdResponse.getWorkflowId());
            workFlowTransBean.getTwoFAConfig().setNextStep(wallet2FAWorkflowIdResponse.getNextStep());
            workFlowTransBean.getTwoFAConfig().setClientId(clientId);
            workFlowTransBean.getTwoFAConfig().setRedirectUri(redirectUri);
            return true;
        }
        return false;
    }

    public boolean isWebTxn(WorkFlowRequestBean flowRequestBean) {
        Map<String, String> queryParams = getQueryParams(flowRequestBean.getQueryParams());
        if (MapUtils.isNotEmpty(queryParams)
                && (StringUtils.equalsIgnoreCase(IOSAPP, queryParams.get(CLIENT_PARAM)) || StringUtils
                        .equalsIgnoreCase(ANDROIDAPP, queryParams.get(CLIENT_PARAM)))) {
            return false;
        }
        return true;
    }
}
