/**
 *
 */
package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.EFundType;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.DccPageData;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.model.VanInfo;
import com.paytm.pgplus.common.model.*;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.enums.SplitTypeEnum;
import com.paytm.pgplus.facade.acquiring.models.SplitCommandInfo;
import com.paytm.pgplus.facade.acquiring.models.TimeoutConfigRule;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.emisubvention.models.ItemWithOrder;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.CheckOutResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.paymentpromotion.models.response.CheckoutPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.CheckoutPromoServiceResponseV2;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.UpiLiteRequestData;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import lombok.Getter;
import lombok.Setter;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import com.paytm.pgplus.facade.wallet.models.UserWalletFreezeDetails;

/**
 * @author namanjain
 *
 */
@RedisEncrypt
public class WorkFlowRequestBean implements Serializable {

    private static final long serialVersionUID = 1436775104831485282L;

    private String custID;

    @NotBlank(message = "Paytm Mid is blank")
    @Length(max = 50, message = "Invalid length for Paytm Mid")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Validation regex not matching for Paytm Mid")
    private String paytmMID;

    @NotBlank(message = "Platform+ Mid is blank")
    @Length(max = 32, message = "Invalid length for Platform+ Mid")
    private String alipayMID;

    @NotBlank(message = "Order Id is blank")
    @Length(max = 50, message = "Invalid length for orderID")
    @Pattern(regexp = "^[a-zA-Z0-9-|_@.-]*$", message = "Validation regex not matching for orderID")
    private String orderID;

    @Length(max = 50, message = "Invalid length for channelID")
    @Pattern(regexp = "^[A-Za-z]*$", message = "Validation regex not matching for channelID")
    private String channelID;

    @Length(max = 50, message = "Invalid length for industryTypeID")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Validation regex not matching for industryTypeID")
    private String industryTypeID;

    @Length(max = 50, message = "Invalid length for website")
    @Pattern(regexp = "^[A-Za-z0-9_]*$", message = "Validation regex not matching for website")
    private String website;

    // private EInvoiceType invoivePaymentType;

    private String callBackURL;

    @NotNull
    private ETransType transType;

    @NotNull
    private ERequestType requestType;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String token;

    private String clientIP;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String oAuthCode;

    private ETerminalType terminalType;

    @Length(max = 50, message = "Invalid length for txnAmount")
    @Pattern(regexp = "^[0-9]*$", message = "Validation regex not matching for txnAmount")
    private String txnAmount;
    private String tipAmount;

    private String currency = "INR";
    private String notificationUrl;
    private EFundType fundType = EFundType.TOPUP_MULTIPAY_MODE;

    @Length(max = 15, message = "Invalid length for mobileNo")
    @Mask(prefixNoMaskLen = 3, suffixNoMaskLen = 3)
    private String mobileNo;

    // PGP-5086
    // @Email(message = "Validation regex not matching for emailID")
    @Length(max = 100, message = "Invalid length for emailID")
    @Mask(prefixNoMaskLen = 4)
    private String emailID;

    private String payModeOnly;

    private EnvInfoRequestBean envInfoReqBean;

    @Length(min = 0, max = 64, message = "TransId not falling in validation Range")
    private String transID;

    private String orderTimeOutInMilliSecond;

    // For Subscription
    @Length(min = 0, max = 50, message = "subscriptionID not falling in validation Range")
    private String subscriptionID;

    @Length(min = 0, max = 50, message = "subsServiceID not falling in validation Range")
    private String subsServiceID;

    @Length(min = 0, max = 50, message = "subsAmountType not falling in validation Range")
    private String subsAmountType;

    @Length(min = 0, max = 50, message = "subsFrequency not falling in validation Range")
    private String subsFrequency;

    @Length(min = 0, max = 50, message = "subsFrequencyUnit not falling in validation Range")
    private String subsFrequencyUnit;

    @Length(min = 0, max = 50, message = "subsExpiryDate not falling in validation Range")
    private String subsExpiryDate;

    private String subsMaxAmount;

    private String savedCardID;

    /*
     * Commenting as we need to bypass validations on this field for ondemand
     */
    /*
     * @Min(value = 0, message =
     * "subsEnableRetry not falling in validation Range")
     * 
     * @Max(value = 1, message =
     * "subsEnableRetry not falling in validation Range")
     */
    private String subsEnableRetry;
    private String subsGraceDays;
    private String subsStartDate;
    private SubsPaymentMode subsPayMode;
    private String subsPPIOnly;
    private String subsRetryCount;
    // Specific to Seamless
    @Mask
    private String paymentDetails;
    private String authMode;
    private String paymentTypeId;
    private String promoCampId;
    private String orderDetails;
    @Mask
    private String dob;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String pincode;
    private String loginTheme;
    private String checksumhash;
    private String theme;
    @Mask
    private String merchantKey;
    private boolean isSavedCard;
    private String storeCard;
    private String bankCode;
    private String bankName;
    // Related to seamless cache card info
    @RedisEncryptAttribute
    @Mask
    private String cardNo;
    private String cardIndexNo;
    private String instNetworkType;
    private String instNetworkCode;
    private String cardType;
    private String holderName;
    @Mask(prefixNoMaskLen = 3, suffixNoMaskLen = 3)
    private String holderMobileNo;
    private String instId;
    private String instBranchId;

    @RedisEncryptAttribute
    @Mask
    private String cvv2;
    @Mask
    private String otp;
    private String cardScheme;

    @Mask
    private Short expiryYear;

    @Mask
    private Short expiryMonth;
    // related to seamless UPI
    @Mask
    private String virtualPaymentAddress;
    // related to seamless createOrderAndPay
    private String mcc;
    private ExtendedInfoRequestBean extendInfo;
    // For payOptionBill
    private String payOption;
    private String payMethod;
    private String chargeAmount;
    private String contractId;
    private List<String> allowedPaymentModes;
    private List<String> disabledPaymentModes;
    private String oauthClientId;
    @Mask
    private String oauthSecretKey;
    private String mmid;
    private SubsTypes subsTypes;
    @Mask
    private String adminToken;
    private String goodsInfo;
    private String shippingInfo;
    private String closeReason;
    private Map<String, String> channelInfo;
    private Map<String, String> extendedInfo;
    private boolean slabBasedMDR;

    private boolean isStaticQrCode;

    // Added for litepayview consult request
    private String customizeCode;

    private boolean cardTokenRequired;

    // parameter to check Offline FastForward Request
    private boolean isOfflineFastForwardRequest;
    private boolean isOfflineFetchPayApi;
    private String iDebitEnabled;
    private String qrMerchantCallbackUrl;
    /*
     * PAYTM_EXPRESS Params
     */
    private EPayMode paytmExpressAddOrHybrid;
    private String walletAmount;
    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String paytmToken;
    // Added for merchant pref of store card details
    private boolean isStoreCardPrefEnabled;
    // upi push specific params
    private boolean upiPushFlow;
    private String creditBlock;
    private String accountNumber;
    private String bank;
    private UpiLiteRequestData upiLiteRequestData;

    @Mask
    private String mpin;
    private String deviceId;
    private String seqNo;
    private String txnToken;
    private String appId;
    private boolean startDateFlow = true;

    /*
     * Reseller flow specific
     */
    private String accountRefId;
    private boolean prnEnabled;
    private boolean isEncryptedCardDetail;
    private boolean upiPushExpressSupported;
    private String nativeClientId;
    @Mask
    private String nativeSecretKey;
    private boolean zeroCostEmi;
    // Native retry specific
    private int nativeRetryCount = 0;
    private int maxAllowedOnMerchant;
    private NativePaymentFailureType paymentFailureType;
    private boolean fromAoaMerchant;
    private int nativeTotalPaymentCount;

    private String walletType;
    private String accountType;

    private boolean isPostConvenience;
    private String qrTxnAmount;
    private boolean isQRIdFlowOnly;

    private Map<UserSubWalletType, BigDecimal> subWalletOrderAmountDetails;

    private String directPayModeType;
    // The last retry state or not
    private boolean terminalStateForInternalRetry = true;
    private long currentInternalPaymentRetryCount;
    private long maxPaymentRetryCount;
    private boolean isCartValidationRequired;
    private boolean isMockRequest;
    private ConsultFeeResponse consultFeeResponse;
    // MF Related
    private String allowUnverifiedAccount;
    private String aggMid;
    private String validateAccountNumber;
    private String additionalInfoMF;

    private boolean isEnhancedCashierPageRequest;
    private String mode;
    private String userLbsLatitude;
    private String userLbsLongitude;
    @Mask(prefixNoMaskLen = 3, suffixNoMaskLen = 3)
    private String targetPhoneNo;
    private boolean gvFlag;

    private String kycCode;
    private String kycVersion;
    private boolean postpaidOnboardingSupported;

    private String ifsc;
    private String paymentTimeoutInMinsForUpi;
    private String isDeepLinkReq;
    private boolean isSubscription;
    private boolean isCreateOrderRequired;
    private String umrn;

    // Direct Bank Related
    private boolean directBankCardFlow;

    private Map<String, String> riskExtendedInfo;

    /*
     * This is not expected to be in request, this is done to persist data
     */
    @RedisEncryptAttribute
    private UserDetailsBiz userDetailsBiz;
    private String advanceDepositId;
    private boolean isQREnabled;

    /*
     * This is not expected to be in request, this is done to persist data
     */
    // Hack to support KYC flow in ADD Money Express
    private PaymentRequestBean paymentRequestBean;
    private boolean isCreateTopupRequired;
    private boolean isNativeAddMoney;

    // Hack to support KYC flow in ADD Money Express

    // for Native+
    private boolean nativeOtpSupported;
    private boolean isNativeJsonRequest;
    private boolean nativeDeepLinkReqd;
    private boolean deepLinkFromInsta;
    private String refUrl;
    private String txnNote;
    /**
     * For corporate Advance Deposit
     */
    private String templateId;
    private String bId;
    private String corporateCustId;

    // flag to be used for offline flow support in native, true if offline
    // request otherwise false
    private boolean offlineFlow;

    private boolean isMotoPaymentBySubscription;

    private PaymentOffer paymentOfferCheckoutReqData; // object type has been
                                                      // updated and is response
                                                      // returned from
                                                      // v1/applyPromo
    private boolean isBin8OfferAvailableOnMerchant;
    @Mask
    private String cardHash;

    /**
     * This doesn't comes in request. It is taken from mapping-service for UPI
     * Dynamic QR at Enhanced Cashier Page
     */
    private String merchantVPA;
    private boolean enhancedDynamicUPIQRCodeAllowed;
    private boolean enhancedCashierPaymentRequest;

    private String forceDirectChannel;
    // for wallet consult in risk api call
    private String amountForWalletConsultInRisk;

    private String amountForPaymentFlow;

    private String securityId;

    private boolean dynamicQrRequired;

    // For MGV
    private List<String> mgvTemplateIds;

    private String merchantRequestedChannelId;

    private boolean emiDetailsApi;

    private boolean gvConsentFlow;

    @Length(max = 50, message = "Invalid length for promoAmount")
    @Pattern(regexp = "^[0-9]*$", message = "Validation regex not matching for promoAmount")
    private String promoAmount;

    private String merchantLogo;

    // for auto-renew and auto-retry subscription
    private boolean subsRenew;
    private boolean subsRetry;
    private boolean subsCommunicationManager;

    private String oneClickInfo;

    private boolean isInternalFetchPaymentOptions;

    private boolean txnFromCardIndexNo;

    private String workFlow;

    private String guestToken;

    private List<Item> items;
    @JsonProperty("isEmiSubventionRequired")
    private boolean isEmiSubventionRequired;
    private String emiSubventedTransactionAmount;
    private String emiSubventionCustomerId;

    private EmiSubventionInfo emiSubventionInfo;

    private ValidateResponse emiSubventionOfferCheckoutReqData;

    private ValidateRequest emiSubventionValidateRequestData;

    private String subventionOrderList;

    private String upiAccRefId;

    private String apiVersion;

    private String originChannel;

    private String productCode;

    /**
     * Added to check whether it is initiate txn request or not
     */

    private boolean createOrderForInitiateTxnRequest;

    private String aggType;

    private OrderPricingInfo orderPricingInfo;

    private boolean offlineTxnFlow;

    private String additionalInfo;

    // for FetchChannelDetailsTask
    private boolean mlvSupported;
    private String appVersion;
    private QRCodeInfoResponseData qRCodeInfo;
    private String queryParams;

    private Date debitDate;
    private String accessToken;
    private String pwpCategory;

    private boolean prepaidCard;
    private boolean isExternalFetchPaymentOptions;

    private SimplifiedPaymentOffers simplifiedPaymentOffers;

    private String payableAmount;

    // flag to be used only for offline flow, unlike offlineFlow flag which
    // supports link payment also
    private boolean scanAndPayFlow;
    private boolean isOrderPSPRequest;
    private List<SplitCommandInfo> splitCommandInfoList;

    private String loyaltyPointRootUserId;

    private boolean dynamicFeeMerchant;

    private boolean onusRentPaymentMerchant;

    private String subsPaymentType;
    private boolean isInternalRequestForSubsPayment;

    private FeeRateFactors feeRateFactors;

    private boolean addMoneyFeeAppliedOnWallet;

    private String initialAddMoneyAmount;

    private String riskVerifyTxnToken; // same value as txn token and is
    // required in showVerificationPage

    private String orderExpiryTimeInMerchantContract;

    private String umn;
    private String vpa;
    private String refServiceInstId;

    // Used to fail txns if UPI pcf is > 0
    private boolean isStaticQrUpiPayment;

    private boolean isEnhanceQrCodeDisabled;

    /***
     * Added Flag to send 'addMoneyDestination=TRANSIT_BLOCKED_WALLET" in wallet
     * consult request.
     */
    private boolean transitWallet;

    private boolean isEcomTokenTxn;

    private String osType;

    private String pspApp;

    private boolean returnDisabledChannelInFpo;

    private String subsOriginalAmount;

    // for upi pay confirm
    private String preAuthExpiryDate;
    private boolean preAuth;

    private boolean addAndPayWithUPICollectSupported;

    // to be populated in case paymethod is null
    private String savedCardPayMethod;

    private boolean costBasedPreferenceEnabled;

    private boolean willUserChange = true;

    // True in case of Add Money App invoke
    private boolean notificationAppInvoke;

    private volatile String appInvokeURL;

    @Mask
    private String paymentPromoCheckoutDataPromoCode;

    private String addMoneySource;

    /**
     * additionalOrderExtendInfo will add key value pairs in Order Extend Info.
     */
    private Map<String, String> additionalOrderExtendInfo;

    private String upiOrderTimeOutInSeconds;

    private ERequestType subRequestType;

    private boolean processSimplifiedEmi;

    private boolean instaDirectForm;

    private String referenceId;

    private boolean flexiSubscription;

    private String paymentMid;
    private String paymentOrderId;
    private String dummyAlipayMid;

    private boolean dccSupported;
    private boolean paymentCallFromDccPage;
    private String dccServiceInstId;
    private boolean dccSelectedByUser;
    private DccPageData dccPageData;
    private boolean useInvestmentAsFundingSource;
    private boolean upiDynamicQrPaymentExceptEnhanceQR;
    private String isOfflineTxn;
    private PaymentOfferV2 paymentOffersAppliedV2;
    private String requestFlow;
    private boolean pushDataToDynamicQR;
    private boolean isPostPaidOnAddnPay;
    private EPreAuthType cardPreAuthType;
    private Long preAuthBlockSeconds;
    private boolean upiConvertedToAddNPay;
    private String fromAOARequest;
    private boolean isOverrideAddNPayBehaviour;
    private Boolean edcLinkTxn;
    private Map<String, String> emiDetailInfo;

    private String orderAmount;

    private boolean isCoftTokenTxn;

    private CardTokenInfo cardTokenInfo;
    private boolean isBinEligibleForCoft;
    private boolean isMerchantEligibleForMultipleMBIDFlow;

    private String lastFourDigits;

    public Boolean isCobarandPreferenceEnabled;

    private String upiPspReqMsgId;
    private String settleType;
    private boolean instantSettlementEnabled;

    private UserWalletFreezeDetails userWalletFreezeDetails;

    private String customDisplayName;

    private String type;

    private TwoFAConfig twoFAConfig;

    private List<String> brandCodes;

    private Boolean addOneRupee;

    private boolean ccOnUPIEnabled;

    private boolean ccOnUPIEnabledForPtcTxn;

    private boolean upiDirectSettlementEnabled;

    public boolean isUpiDirectSettlementEnabled() {
        return upiDirectSettlementEnabled;
    }

    public void setUpiDirectSettlementEnabled(boolean upiDirectSettlementEnabled) {
        this.upiDirectSettlementEnabled = upiDirectSettlementEnabled;
    }

    private boolean upiLite;

    private boolean superGwFpoApiHit;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String ppblAccountType;

    public Boolean getCobarandPreferenceEnabled() {
        return isCobarandPreferenceEnabled;
    }

    public void setCobarandPreferenceEnabled(Boolean cobarandPreferenceEnabled) {
        isCobarandPreferenceEnabled = cobarandPreferenceEnabled;
    }

    private boolean isPostpaidEnabledOnMerchant;

    public boolean isPostpaidEnabledOnMerchant() {
        return isPostpaidEnabledOnMerchant;
    }

    public void setPostpaidEnabledOnMerchant(boolean postpaidEnabledOnMerchant) {
        isPostpaidEnabledOnMerchant = postpaidEnabledOnMerchant;
    }

    private boolean isPG2PreferenceEnabledOnMid;

    private Routes route;

    private Map<String, String> detailExtendInfo;

    private String PG2EnabledPaymodesPayoptions;

    private boolean fullPg2TrafficEnabled;

    private String merchantCoftConfig;

    private boolean isEnableGcinOnCoftPromo;

    private boolean is3pAddMoneyEnabled;

    public boolean is3pAddMoneyEnabled() {
        return is3pAddMoneyEnabled;
    }

    public void setIs3pAddMoneyEnabled(boolean is3pAddMoneyEnabled) {
        this.is3pAddMoneyEnabled = is3pAddMoneyEnabled;
    }

    public Map<String, String> getDetailExtendInfo() {
        return detailExtendInfo;
    }

    public void setDetailExtendInfo(Map<String, String> detailExtendInfo) {
        this.detailExtendInfo = detailExtendInfo;
    }

    public boolean isFullPg2TrafficEnabled() {
        return fullPg2TrafficEnabled;
    }

    public void setFullPg2TrafficEnabled(boolean fullPg2TrafficEnabled) {
        this.fullPg2TrafficEnabled = fullPg2TrafficEnabled;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public boolean isPostPaidOnAddnPay() {
        return isPostPaidOnAddnPay;
    }

    public void setPostPaidOnAddnPay(boolean postPaidOnAddnPay) {
        isPostPaidOnAddnPay = postPaidOnAddnPay;
    }

    public boolean isProcessSimplifiedEmi() {
        return processSimplifiedEmi;
    }

    public void setProcessSimplifiedEmi(boolean processSimplifiedEmi) {
        this.processSimplifiedEmi = processSimplifiedEmi;
    }

    private String walletPostpaidAuthorizationMode;

    // Added for international Card
    private boolean isInternationalCard;

    private boolean corporateCard;
    private boolean isCardActive;

    public boolean isCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(boolean corporateCard) {
        this.corporateCard = corporateCard;
    }

    public boolean isCardActive() {
        return isCardActive;
    }

    public void setCardActive(boolean cardActive) {
        isCardActive = cardActive;
    }

    private CheckoutPromoServiceResponse checkoutPromoServiceResponse;

    public String getWalletPostpaidAuthorizationMode() {
        return walletPostpaidAuthorizationMode;
    }

    public void setWalletPostpaidAuthorizationMode(String walletPostpaidAuthorizationMode) {
        this.walletPostpaidAuthorizationMode = walletPostpaidAuthorizationMode;
    }

    public boolean isReturnDisabledChannelInFpo() {
        return returnDisabledChannelInFpo;
    }

    public void setReturnDisabledChannelInFpo(boolean returnDisabledChannelInFpo) {
        this.returnDisabledChannelInFpo = returnDisabledChannelInFpo;
    }

    private boolean dynamicQREdcRequest;
    private BinDetail binDetail;

    private boolean paymentResumed;

    private boolean checkoutJsAppInvokePayment;

    private Map<String, String> posOrderData;

    private VanInfo vanInfo;

    private List<TpvInfo> tpvInfos;

    private String feesAmount;

    private boolean offusBasedOrderFound;

    private String preferredOtpPage;

    private boolean fetchPaytmInstrumentsBalance;

    private CoftConsent coftConsent;

    private boolean returnToken;

    private String emiSubventionStratergy;

    // preference driven
    private boolean fetchPostpaidBalance;

    private boolean aoaSubsOnPgMid;

    private boolean subsAoaPgMidTxn;

    private String gcin;
    private String par;
    private String accountRangeCardBin;

    private boolean addNPayGlobalVaultCards;
    private boolean globalVaultCoftPreference;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    private boolean disableLimitCCAddNPay;
    private boolean allowInternalRetryOnDirectBankCardFlow;
    private boolean createNonQRDeepLink;

    private TwoFADetails twoFADetails;

    @Getter
    @Setter
    private String txnFlow;

    private boolean isDealsFlow;

    private Map<String, String> promoContext;

    private InitiateTransactionRequest initiateTransactionRequest;

    private BankFormOptimizationParams bankFormOptimizationParams;

    private boolean isICBFlow;

    private String orderType;

    private CheckoutPromoServiceResponseV2 promoCheckoutInfo;

    private GenericEmiSubventionResponse<CheckOutResponse> subventionCheckoutInfo;

    private Map<String, String> promoContextICB;

    private List<ItemWithOrder> itemsICB;

    private String source;

    private boolean addNPayDisabledForZeroBalance;

    private boolean isAddMoneyPcfEnabled;

    private String feeRateCode;

    private String upiIntentAllowedRetries;

    private boolean generateEsnRequest;

    private SplitTypeEnum splitType;
    private String postTxnSplitTimeout;

    public InitiateTransactionRequest getInitiateTransactionRequest() {
        return initiateTransactionRequest;
    }

    public void setInitiateTransactionRequest(InitiateTransactionRequest initiateTransactionRequest) {
        this.initiateTransactionRequest = initiateTransactionRequest;
    }

    public Map<String, String> getPromoContext() {
        return promoContext;
    }

    public void setPromoContext(Map<String, String> promoContext) {
        this.promoContext = promoContext;
    }

    public boolean isDealsFlow() {
        return isDealsFlow;
    }

    public void setDealsFlow(boolean dealsFlow) {
        isDealsFlow = dealsFlow;
    }

    public boolean isAoaSubsOnPgMid() {
        return aoaSubsOnPgMid;
    }

    public void setAoaSubsOnPgMid(boolean aoaSubsOnPgMid) {
        this.aoaSubsOnPgMid = aoaSubsOnPgMid;
    }

    private boolean addnpayPreferenceOnMerchant;

    public boolean isDynamicQREdcRequest() {
        return dynamicQREdcRequest;
    }

    public void setDynamicQREdcRequest(boolean dynamicQREdcRequest) {
        this.dynamicQREdcRequest = dynamicQREdcRequest;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isEnhanceQrCodeDisabled() {
        return isEnhanceQrCodeDisabled;
    }

    public void setEnhanceQrCodeDisabled(boolean enhanceQrCodeDisabled) {
        isEnhanceQrCodeDisabled = enhanceQrCodeDisabled;
    }

    public boolean isStaticQrUpiPayment() {
        return isStaticQrUpiPayment;
    }

    public void setStaticQrUpiPayment(boolean staticQrUpiPayment) {
        isStaticQrUpiPayment = staticQrUpiPayment;
    }

    public boolean isAddMoneyFeeAppliedOnWallet() {
        return addMoneyFeeAppliedOnWallet;
    }

    public void setAddMoneyFeeAppliedOnWallet(boolean addMoneyFeeAppliedOnWallet) {
        this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
    }

    public String getInitialAddMoneyAmount() {
        return initialAddMoneyAmount;
    }

    public void setInitialAddMoneyAmount(String initialAddMoneyAmount) {
        this.initialAddMoneyAmount = initialAddMoneyAmount;
    }

    public FeeRateFactors getFeeRateFactors() {
        return feeRateFactors;
    }

    public void setFeeRateFactors(FeeRateFactors feeRateFactors) {
        this.feeRateFactors = feeRateFactors;
    }

    public boolean isPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public EmiSubventionInfo getEmiSubventionInfo() {
        return emiSubventionInfo;
    }

    public void setEmiSubventionInfo(EmiSubventionInfo emiSubventionInfo) {
        this.emiSubventionInfo = emiSubventionInfo;
    }

    public ValidateRequest getEmiSubventionValidateRequestData() {
        return emiSubventionValidateRequestData;
    }

    public void setEmiSubventionValidateRequestData(ValidateRequest emiSubventionValidateRequestData) {
        this.emiSubventionValidateRequestData = emiSubventionValidateRequestData;
    }

    public ValidateResponse getEmiSubventionOfferCheckoutReqData() {
        return emiSubventionOfferCheckoutReqData;
    }

    public void setEmiSubventionOfferCheckoutReqData(ValidateResponse emiSubventionOfferCheckoutReqData) {
        this.emiSubventionOfferCheckoutReqData = emiSubventionOfferCheckoutReqData;
    }

    public String getPromoAmount() {
        return promoAmount;
    }

    public void setPromoAmount(String promoAmount) {
        this.promoAmount = promoAmount;
    }

    public String getMerchantRequestedChannelId() {
        return merchantRequestedChannelId;
    }

    public void setMerchantRequestedChannelId(String merchantRequestedChannelId) {
        this.merchantRequestedChannelId = merchantRequestedChannelId;
    }

    public PaymentOffer getPaymentOfferCheckoutReqData() {
        return paymentOfferCheckoutReqData;
    }

    public boolean isSubsRenew() {
        return subsRenew;
    }

    public void setSubsRenew(boolean renew) {
        subsRenew = renew;
    }

    public boolean isSubsRetry() {
        return subsRetry;
    }

    public void setSubsRetry(boolean retry) {
        subsRetry = retry;
    }

    public boolean isCommunicationManager() {
        return subsCommunicationManager;
    }

    public void setCommunicationManager(boolean communicationManager) {
        subsCommunicationManager = communicationManager;
    }

    public void setPaymentOfferCheckoutReqData(PaymentOffer paymentOfferCheckoutReqData) {
        this.paymentOfferCheckoutReqData = paymentOfferCheckoutReqData;
    }

    public boolean isBin8OfferAvailableOnMerchant() {
        return isBin8OfferAvailableOnMerchant;
    }

    public void setBin8OfferAvailableOnMerchant(boolean bin8OfferAvailableOnMerchant) {
        isBin8OfferAvailableOnMerchant = bin8OfferAvailableOnMerchant;
    }

    public boolean isNativeAddMoney() {
        return isNativeAddMoney;
    }

    public void setNativeAddMoney(boolean nativeAddMoney) {
        isNativeAddMoney = nativeAddMoney;
    }

    public boolean isCreateTopupRequired() {
        return isCreateTopupRequired;
    }

    public void setCreateTopupRequired(boolean createTopupRequired) {
        isCreateTopupRequired = createTopupRequired;
    }

    public boolean isNativeDeepLinkReqd() {
        return nativeDeepLinkReqd;
    }

    public void setNativeDeepLinkReqd(boolean nativeDeepLinkReqd) {
        this.nativeDeepLinkReqd = nativeDeepLinkReqd;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    public String getTxnNote() {
        return txnNote;
    }

    public void setTxnNote(String txnNote) {
        this.txnNote = txnNote;
    }

    public boolean isDeepLinkFromInsta() {
        return deepLinkFromInsta;
    }

    public void setDeepLinkFromInsta(boolean deepLinkFromInsta) {
        this.deepLinkFromInsta = deepLinkFromInsta;
    }

    public boolean isCreateOrderRequired() {
        return isCreateOrderRequired;
    }

    public void setCreateOrderRequired(boolean createOrderRequired) {
        isCreateOrderRequired = createOrderRequired;
    }

    public boolean isDirectBankCardFlow() {
        return directBankCardFlow;
    }

    public void setDirectBankCardFlow(boolean directBankCardFlow) {
        this.directBankCardFlow = directBankCardFlow;
    }

    public String getPaymentTimeoutInMinsForUpi() {
        return paymentTimeoutInMinsForUpi;
    }

    public void setPaymentTimeoutInMinsForUpi(String paymentTimeountInMinsForUpi) {
        this.paymentTimeoutInMinsForUpi = paymentTimeountInMinsForUpi;
    }

    public Map<String, String> getRiskExtendedInfo() {
        return riskExtendedInfo;
    }

    public void setRiskExtendedInfo(Map<String, String> riskExtendedInfo) {
        this.riskExtendedInfo = riskExtendedInfo;
    }

    public boolean isGvFlag() {
        return gvFlag;
    }

    public void setGvFlag(boolean gvFlag) {
        this.gvFlag = gvFlag;
    }

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    public UserDetailsBiz getUserDetailsBiz() {
        return userDetailsBiz;
    }

    public void setUserDetailsBiz(UserDetailsBiz userDetailsBiz) {
        this.userDetailsBiz = userDetailsBiz;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUserLbsLatitude() {
        return userLbsLatitude;
    }

    public void setUserLbsLatitude(String userLbsLatitude) {
        this.userLbsLatitude = userLbsLatitude;
    }

    public String getUserLbsLongitude() {
        return userLbsLongitude;
    }

    public void setUserLbsLongitude(String userLbsLongitude) {
        this.userLbsLongitude = userLbsLongitude;
    }

    public boolean isEnhancedCashierPageRequest() {
        return isEnhancedCashierPageRequest;
    }

    public void setEnhancedCashierPageRequest(boolean enhancedCashierPageRequest) {
        isEnhancedCashierPageRequest = enhancedCashierPageRequest;
    }

    public boolean isCartValidationRequired() {
        return isCartValidationRequired;
    }

    public void setCartValidationRequired(boolean cartValidationRequired) {
        isCartValidationRequired = cartValidationRequired;
    }

    public boolean isUpiPushFlow() {
        return upiPushFlow;
    }

    public void setUpiPushFlow(boolean upiPushFlow) {
        this.upiPushFlow = upiPushFlow;
    }

    public String getCreditBlock() {
        return creditBlock;
    }

    public void setCreditBlock(String creditBlock) {
        this.creditBlock = creditBlock;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    /*
     * public EInvoiceType getInvoivePaymentType() { return invoivePaymentType;
     * }
     * 
     * public void setInvoicePaymentType(EInvoiceType invoivePaymentType) {
     * this.invoivePaymentType = invoivePaymentType; }
     */

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Map<String, String> getExtendedInfo() {
        return extendedInfo;
    }

    public void setExtendedInfo(Map<String, String> extendedInfo) {
        this.extendedInfo = extendedInfo;
    }

    public String getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(String paytmToken) {
        this.paytmToken = paytmToken;
    }

    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(Map<String, String> channelInfo) {
        this.channelInfo = channelInfo;
    }

    public EPayMode getPaytmExpressAddOrHybrid() {
        return paytmExpressAddOrHybrid;
    }

    public void setPaytmExpressAddOrHybrid(EPayMode paytmExpressAddOrHybrid) {
        this.paytmExpressAddOrHybrid = paytmExpressAddOrHybrid;
    }

    public String getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(String walletAmount) {
        this.walletAmount = walletAmount;
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    public String getSubsMaxAmount() {
        return subsMaxAmount;
    }

    public void setSubsMaxAmount(String subsMaxAmount) {
        this.subsMaxAmount = subsMaxAmount;
    }

    public void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public String getSubsPPIOnly() {
        return subsPPIOnly;
    }

    public void setSubsPPIOnly(String subsPPIOnly) {
        this.subsPPIOnly = subsPPIOnly;
    }

    public SubsPaymentMode getSubsPayMode() {
        return subsPayMode;
    }

    public void setSubsPayMode(SubsPaymentMode subsPayMode) {
        this.subsPayMode = subsPayMode;
    }

    public List<String> getAllowedPaymentModes() {
        return allowedPaymentModes != null ? allowedPaymentModes : Collections.emptyList();
    }

    public void setAllowedPaymentModes(final List<String> paymentModes) {
        this.allowedPaymentModes = paymentModes;
    }

    public List<String> getDisabledPaymentModes() {
        return disabledPaymentModes != null ? disabledPaymentModes : Collections.emptyList();
    }

    public void setDisabledPaymentModes(final List<String> disabledPaymentModes) {
        this.disabledPaymentModes = disabledPaymentModes;
    }

    /**
     * @return the bankCode
     */
    public String getBankCode() {
        return bankCode;
    }

    /**
     * @param bankCode
     *            the bankCode to set
     */
    public void setBankCode(final String bankCode) {
        this.bankCode = bankCode;
    }

    /**
     * @return the address1
     */
    public String getAddress1() {
        return address1;
    }

    /**
     * @param address1
     *            the address1 to set
     */
    public void setAddress1(final String address1) {
        this.address1 = address1;
    }

    /**
     * @return the address2
     */
    public String getAddress2() {
        return address2;
    }

    /**
     * @param address2
     *            the address2 to set
     */
    public void setAddress2(final String address2) {
        this.address2 = address2;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(final String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(final String state) {
        this.state = state;
    }

    /**
     * @return the pincode
     */
    public String getPincode() {
        return pincode;
    }

    /**
     * @param pincode
     *            the pincode to set
     */
    public void setPincode(final String pincode) {
        this.pincode = pincode;
    }

    /**
     * @return the loginTheme
     */
    public String getLoginTheme() {
        return loginTheme;
    }

    /**
     * @param loginTheme
     *            the loginTheme to set
     */
    public void setLoginTheme(final String loginTheme) {
        this.loginTheme = loginTheme;
    }

    /**
     * @return the checksumhash
     */
    public String getChecksumhash() {
        return checksumhash;
    }

    /**
     * @param checksumhash
     *            the checksumhash to set
     */
    public void setChecksumhash(final String checksumhash) {
        this.checksumhash = checksumhash;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme
     *            the theme to set
     */
    public void setTheme(final String theme) {
        this.theme = theme;
    }

    /**
     * @return the promoCampId
     */
    public String getPromoCampId() {
        return promoCampId;
    }

    /**
     * @param promoCampId
     *            the promoCampId to set
     */
    public void setPromoCampId(final String promoCampId) {
        this.promoCampId = promoCampId;
    }

    /**
     * @return the orderDetails
     */
    public String getOrderDetails() {
        return orderDetails;
    }

    /**
     * @param orderDetails
     *            the orderDetails to set
     */
    public void setOrderDetails(final String orderDetails) {
        this.orderDetails = orderDetails;
    }

    /**
     * @return the dob
     */
    public String getDob() {
        return dob;
    }

    /**
     * @param dob
     *            the dob to set
     */
    public void setDob(final String dob) {
        this.dob = dob;
    }

    /**
     * @return the extendInfo
     */
    public ExtendedInfoRequestBean getExtendInfo() {
        return extendInfo;
    }

    /**
     * @param extendInfo
     *            the extendInfo to set
     */
    public void setExtendInfo(final ExtendedInfoRequestBean extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getSubsStartDate() {
        return subsStartDate;
    }

    public void setSubsStartDate(final String subsStartDate) {
        this.subsStartDate = subsStartDate;
    }

    public String getSubsGraceDays() {
        return subsGraceDays;
    }

    public void setSubsGraceDays(final String subsGraceDays) {
        this.subsGraceDays = subsGraceDays;
    }

    /**
     * @return the authMode
     */
    public String getAuthMode() {
        return authMode;
    }

    /**
     * @param authMode
     *            the authMode to set
     */
    public void setAuthMode(final String authMode) {
        this.authMode = authMode;
    }

    /**
     * @return the paymentTypeId
     */
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    /**
     * @param paymentTypeId
     *            the paymentTypeId to set
     */
    public void setPaymentTypeId(final String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public boolean getIsSavedCard() {
        return isSavedCard;
    }

    /**
     * @param isSavedCard
     *            the isSavedCard to set
     */

    public void setIsSavedCard(final boolean isSavedCard) {
        this.isSavedCard = isSavedCard;
    }

    public String getStoreCard() {
        return storeCard;
    }

    public void setStoreCard(final String storeCard) {
        this.storeCard = storeCard;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(final String contractId) {
        this.contractId = contractId;
    }

    public String getPayOption() {
        return payOption;
    }

    public void setPayOption(final String payOption) {
        this.payOption = payOption;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(final String payMethod) {
        this.payMethod = payMethod;
    }

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(final String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(final String mcc) {
        this.mcc = mcc;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(final String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(final String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public String getInstNetworkType() {
        return instNetworkType;
    }

    public void setInstNetworkType(final String instNetworkType) {
        this.instNetworkType = instNetworkType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(final String cardType) {
        this.cardType = cardType;
    }

    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    public void setInstNetworkCode(final String instNetworkCode) {
        this.instNetworkCode = instNetworkCode;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(final String holderName) {
        this.holderName = holderName;
    }

    public String getHolderMobileNo() {
        return holderMobileNo;
    }

    public void setHolderMobileNo(final String holderMobileNo) {
        this.holderMobileNo = holderMobileNo;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(final String instId) {
        this.instId = instId;
    }

    public String getInstBranchId() {
        return instBranchId;
    }

    public void setInstBranchId(final String instBranchId) {
        this.instBranchId = instBranchId;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(final String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(final String otp) {
        this.otp = otp;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(final String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public Short getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(final Short expiryYear) {
        this.expiryYear = expiryYear;
    }

    public Short getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(final Short expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getVirtualPaymentAddress() {
        return virtualPaymentAddress;
    }

    public void setVirtualPaymentAddress(String virtualPaymentAddress) {
        this.virtualPaymentAddress = virtualPaymentAddress;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(final String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(final String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getSavedCardID() {
        return savedCardID;
    }

    public void setSavedCardID(final String savedCardID) {
        this.savedCardID = savedCardID;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(final String transID) {
        this.transID = transID;
    }

    public String getSubsServiceID() {
        return subsServiceID;
    }

    public void setSubsServiceID(final String subsServiceID) {
        this.subsServiceID = subsServiceID;
    }

    public String getSubsAmountType() {
        return subsAmountType;
    }

    public void setSubsAmountType(final String subsAmountType) {
        this.subsAmountType = subsAmountType;
    }

    public String getSubsFrequency() {
        return subsFrequency;
    }

    public void setSubsFrequency(final String subsFrequency) {
        this.subsFrequency = subsFrequency;
    }

    public String getSubsFrequencyUnit() {
        return subsFrequencyUnit;
    }

    public void setSubsFrequencyUnit(final String subsFrequencyUnit) {
        this.subsFrequencyUnit = subsFrequencyUnit;
    }

    public String getSubsExpiryDate() {
        return subsExpiryDate;
    }

    public void setSubsExpiryDate(final String subsExpiryDate) {
        this.subsExpiryDate = subsExpiryDate;
    }

    public String getSubsEnableRetry() {
        return subsEnableRetry;
    }

    public void setSubsEnableRetry(final String subsEnableRetry) {
        this.subsEnableRetry = subsEnableRetry;
    }

    public String getoAuthCode() {
        return oAuthCode;
    }

    public void setoAuthCode(final String oAuthCode) {
        this.oAuthCode = oAuthCode;
    }

    public String getOrderTimeOutInMilliSecond() {
        return orderTimeOutInMilliSecond;
    }

    public void setOrderTimeOutInMilliSecond(final String orderTimeOutInMilliSecond) {
        this.orderTimeOutInMilliSecond = orderTimeOutInMilliSecond;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(final String clientIP) {
        this.clientIP = clientIP;
    }

    public ERequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(final ERequestType requestType) {
        this.requestType = requestType;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(final String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(final String emailID) {
        this.emailID = emailID;
    }

    public String getPayModeOnly() {
        return payModeOnly;
    }

    public void setPayModeOnly(final String payModeOnly) {
        this.payModeOnly = payModeOnly;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(final String custID) {
        this.custID = custID;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(final String callBackURL) {
        this.callBackURL = callBackURL;
    }

    /*
     * public String getReqType() { return reqType; }
     * 
     * public void setReqType(String reqType) { this.reqType = reqType; }
     */

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(final String channelID) {
        this.channelID = channelID;
    }

    public String getIndustryTypeID() {
        return industryTypeID;
    }

    public void setIndustryTypeID(final String industryTypeID) {
        this.industryTypeID = industryTypeID;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(final String orderID) {
        this.orderID = orderID;
    }

    public String getPaytmMID() {
        return paytmMID;
    }

    public void setPaytmMID(final String paytmMID) {
        this.paytmMID = paytmMID;
    }

    public String getAlipayMID() {
        return alipayMID;
    }

    public void setAlipayMID(final String alipayMID) {
        this.alipayMID = alipayMID;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(final String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(final String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(final String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public EFundType getFundType() {
        return fundType;
    }

    public void setFundType(final EFundType fundType) {
        this.fundType = fundType;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token
     *            the token to set
     */
    public void setToken(final String token) {
        this.token = token;
    }

    public ETerminalType getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(final ETerminalType terminalType) {
        this.terminalType = terminalType;
    }

    public ETransType getTransType() {
        return transType;
    }

    public void setTransType(final ETransType transType) {
        this.transType = transType;
    }

    public EnvInfoRequestBean getEnvInfoReqBean() {
        return envInfoReqBean;
    }

    public void setEnvInfoReqBean(final EnvInfoRequestBean envInfoReqBean) {
        this.envInfoReqBean = envInfoReqBean;
    }

    /**
     * @return the oauthClientId
     */
    public String getOauthClientId() {
        return oauthClientId;
    }

    /**
     * @param oauthClientId
     *            the oauthClientId to set
     */
    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    /**
     * @return the oauthSecretKey
     */
    public String getOauthSecretKey() {
        return oauthSecretKey;
    }

    /**
     * @param oauthSecretKey
     *            the oauthSecretKey to set
     */
    public void setOauthSecretKey(String oauthSecretKey) {
        this.oauthSecretKey = oauthSecretKey;
    }

    /**
     * @return the mmid
     */
    public String getMmid() {
        return mmid;
    }

    /**
     * @param mmid
     *            the mmid to set
     */
    public void setMmid(String mmid) {
        this.mmid = mmid;
    }

    /**
     * @return the subsTypes
     */
    public SubsTypes getSubsTypes() {
        return subsTypes;
    }

    /**
     * @param subsTypes
     *            the subsTypes to set
     */
    public void setSubsTypes(SubsTypes subsTypes) {
        this.subsTypes = subsTypes;
    }

    /**
     * @return the adminToken
     */
    public String getAdminToken() {
        return adminToken;
    }

    /**
     * @param adminToken
     *            the adminToken to set
     */
    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    /**
     * @return the subsRetryCount
     */
    public String getSubsRetryCount() {
        return subsRetryCount;
    }

    /**
     * @param subsRetryCount
     *            the subsRetryCount to set
     */
    public void setSubsRetryCount(String subsRetryCount) {
        this.subsRetryCount = subsRetryCount;
    }

    public String getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(String goodsInfo) {
        this.goodsInfo = goodsInfo;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    public String getCustomizeCode() {
        return customizeCode;
    }

    public void setCustomizeCode(String customizeCode) {
        this.customizeCode = customizeCode;
    }

    public boolean isSlabBasedMDR() {
        return slabBasedMDR;
    }

    public void setSlabBasedMDR(boolean slabBasedMDR) {
        this.slabBasedMDR = slabBasedMDR;
    }

    public boolean isStaticQrCode() {
        return isStaticQrCode;
    }

    public void setStaticQrCode(boolean staticQrCode) {
        isStaticQrCode = staticQrCode;
    }

    public boolean isoffusOrderNotFound() {
        return offusBasedOrderFound;
    }

    public void setOffusBasedOrderFound(boolean offusBasedOrderFound) {
        this.offusBasedOrderFound = offusBasedOrderFound;
    }

    public UpiLiteRequestData getUpiLiteRequestData() {
        return upiLiteRequestData;
    }

    public void setUpiLiteRequestData(UpiLiteRequestData upiLiteRequestData) {
        this.upiLiteRequestData = upiLiteRequestData;
    }

    public boolean isCardTokenRequired() {
        return cardTokenRequired;
    }

    public void setCardTokenRequired(boolean cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
    }

    public boolean isOfflineFastForwardRequest() {
        return isOfflineFastForwardRequest;
    }

    public void setOfflineFastForwardRequest(boolean isOfflineFastForwardRequest) {
        this.isOfflineFastForwardRequest = isOfflineFastForwardRequest;
    }

    public String getiDebitEnabled() {
        return iDebitEnabled;
    }

    public void setiDebitEnabled(String iDebitEnabled) {
        this.iDebitEnabled = iDebitEnabled;
    }

    public String getQrMerchantCallbackUrl() {
        return qrMerchantCallbackUrl;
    }

    public void setQrMerchantCallbackUrl(String qrMerchantCallbackUrl) {
        this.qrMerchantCallbackUrl = qrMerchantCallbackUrl;
    }

    public boolean isStoreCardPrefEnabled() {
        return isStoreCardPrefEnabled;
    }

    public void setStoreCardPrefEnabled(boolean isStoreCardPrefEnabled) {
        this.isStoreCardPrefEnabled = isStoreCardPrefEnabled;
    }

    public boolean isOfflineFetchPayApi() {
        return isOfflineFetchPayApi;
    }

    public void setOfflineFetchPayApi(boolean isOfflineFetchPayApi) {
        this.isOfflineFetchPayApi = isOfflineFetchPayApi;
    }

    public boolean isQREnabled() {
        return isQREnabled;
    }

    public void setQREnabled(boolean isQREnabled) {
        this.isQREnabled = isQREnabled;
    }

    public boolean isStartDateFlow() {
        return startDateFlow;
    }

    public void setStartDateFlow(boolean startDateFlow) {
        this.startDateFlow = startDateFlow;
    }

    public boolean isPrnEnabled() {
        return prnEnabled;
    }

    public WorkFlowRequestBean setPrnEnabled(boolean prnEnabled) {
        this.prnEnabled = prnEnabled;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public boolean isUpiPushExpressSupported() {
        return upiPushExpressSupported;
    }

    public void setUpiPushExpressSupported(boolean upiPushExpressSupported) {
        this.upiPushExpressSupported = upiPushExpressSupported;
    }

    public boolean isEncryptedCardDetail() {
        return isEncryptedCardDetail;
    }

    public void setEncryptedCardDetail(boolean isEncryptedCardDetail) {
        this.isEncryptedCardDetail = isEncryptedCardDetail;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public String getAccountRefId() {
        return accountRefId;
    }

    public void setAccountRefId(String accountRefId) {
        this.accountRefId = accountRefId;
    }

    public String getNativeClientId() {
        return nativeClientId;
    }

    public void setNativeClientId(String nativeClientId) {
        this.nativeClientId = nativeClientId;
    }

    public String getNativeSecretKey() {
        return nativeSecretKey;
    }

    public void setNativeSecretKey(String nativeSecretKey) {
        this.nativeSecretKey = nativeSecretKey;
    }

    public boolean isZeroCostEmi() {
        return zeroCostEmi;
    }

    public void setZeroCostEmi(boolean zeroCostEmi) {
        this.zeroCostEmi = zeroCostEmi;
    }

    public void setSubWalletOrderAmountDetails(Map<UserSubWalletType, BigDecimal> subWalletOrderAmountDetails) {
        this.subWalletOrderAmountDetails = subWalletOrderAmountDetails;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getNativeRetryCount() {
        return nativeRetryCount;
    }

    public void setNativeRetryCount(int nativeRetryCount) {
        this.nativeRetryCount = nativeRetryCount;
    }

    public int getMaxAllowedOnMerchant() {
        return maxAllowedOnMerchant;
    }

    public void setMaxAllowedOnMerchant(int maxAllowedOnMerchant) {
        this.maxAllowedOnMerchant = maxAllowedOnMerchant;
    }

    public NativePaymentFailureType getPaymentFailureType() {
        return paymentFailureType;
    }

    public void setPaymentFailureType(NativePaymentFailureType paymentFailureType) {
        this.paymentFailureType = paymentFailureType;
    }

    public int getNativeTotalPaymentCount() {
        return nativeTotalPaymentCount;
    }

    public void setNativeTotalPaymentCount(int nativeTotalPaymentCount) {
        this.nativeTotalPaymentCount = nativeTotalPaymentCount;
    }

    public PaymentRequestBean getPaymentRequestBean() {
        return paymentRequestBean;
    }

    public void setPaymentRequestBean(PaymentRequestBean paymentRequestBean) {
        this.paymentRequestBean = paymentRequestBean;
    }

    public Map<UserSubWalletType, BigDecimal> getSubWalletOrderAmountDetails() {
        return subWalletOrderAmountDetails;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isPostConvenience() {
        return isPostConvenience;
    }

    public void setPostConvenience(boolean postConvenience) {
        isPostConvenience = postConvenience;
    }

    public String getDirectPayModeType() {
        return directPayModeType;
    }

    public void setDirectPayModeType(String directPayModeType) {
        this.directPayModeType = directPayModeType;
    }

    public boolean isTerminalStateForInternalRetry(RetryStatus retryStatus) {
        return terminalStateForInternalRetry || retryStatus == null;
    }

    public void setTerminalStateForInternalRetry(boolean terminalStateForInternalRetry) {
        this.terminalStateForInternalRetry = terminalStateForInternalRetry;
    }

    public long getCurrentInternalPaymentRetryCount() {
        return currentInternalPaymentRetryCount;
    }

    public void setCurrentInternalPaymentRetryCount(long currentInternalPaymentRetryCount) {
        this.currentInternalPaymentRetryCount = currentInternalPaymentRetryCount;
    }

    public long getMaxPaymentRetryCount() {
        return maxPaymentRetryCount;
    }

    public void setMaxPaymentRetryCount(long maxPaymentRetryCount) {
        this.maxPaymentRetryCount = maxPaymentRetryCount;
    }

    public boolean isMockRequest() {
        return isMockRequest;
    }

    public void setMockRequest(boolean mockRequest) {
        isMockRequest = mockRequest;
    }

    public ConsultFeeResponse getConsultFeeResponse() {
        return consultFeeResponse;
    }

    public void setConsultFeeResponse(ConsultFeeResponse consultFeeResponse) {
        this.consultFeeResponse = consultFeeResponse;
    }

    public String getAllowUnverifiedAccount() {
        return allowUnverifiedAccount;
    }

    public void setAllowUnverifiedAccount(String allowUnverifiedAccount) {
        this.allowUnverifiedAccount = allowUnverifiedAccount;
    }

    public String getAggMid() {
        return aggMid;
    }

    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
    }

    public String getValidateAccountNumber() {
        return validateAccountNumber;
    }

    public void setValidateAccountNumber(String validateAccountNumber) {
        this.validateAccountNumber = validateAccountNumber;
    }

    public String getAdditionalInfoMF() {
        return additionalInfoMF;
    }

    public void setAdditionalInfoMF(String additionalInfoMF) {
        this.additionalInfoMF = additionalInfoMF;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getIsDeepLinkReq() {
        return isDeepLinkReq;
    }

    public void setIsDeepLinkReq(String isDeepLinkReq) {
        this.isDeepLinkReq = isDeepLinkReq;
    }

    public boolean isSubscription() {
        return isSubscription;
    }

    public void setSubscription(boolean subscription) {
        isSubscription = subscription;
    }

    public String getKycCode() {
        return kycCode;
    }

    public void setKycCode(String kycCode) {
        this.kycCode = kycCode;
    }

    public String getKycVersion() {
        return kycVersion;
    }

    public void setKycVersion(String kycVersion) {
        this.kycVersion = kycVersion;
    }

    public boolean isPostpaidOnboardingSupported() {
        return postpaidOnboardingSupported;
    }

    public void setPostpaidOnboardingSupported(boolean postpaidOnboardingSupported) {
        this.postpaidOnboardingSupported = postpaidOnboardingSupported;
    }

    public String getAdvanceDepositId() {
        return advanceDepositId;
    }

    public void setAdvanceDepositId(String advanceDepositId) {
        this.advanceDepositId = advanceDepositId;
    }

    public String getUmrn() {
        return umrn;
    }

    public void setUmrn(String umrn) {
        this.umrn = umrn;
    }

    public boolean isNativeOtpSupported() {
        return nativeOtpSupported;
    }

    public void setNativeOtpSupported(boolean nativeOtpSupported) {
        this.nativeOtpSupported = nativeOtpSupported;
    }

    public boolean isNativeJsonRequest() {
        return isNativeJsonRequest;
    }

    public void setNativeJsonRequest(boolean nativeJsonRequest) {
        isNativeJsonRequest = nativeJsonRequest;
    }

    public boolean isMotoPaymentBySubscription() {
        return isMotoPaymentBySubscription;
    }

    public void setMotoPaymentBySubscription(boolean motoPaymentBySubscription) {
        isMotoPaymentBySubscription = motoPaymentBySubscription;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getbId() {
        return bId;
    }

    public void setbId(String bId) {
        this.bId = bId;
    }

    public String getCorporateCustId() {
        return corporateCustId;
    }

    public void setCorporateCustId(String corporateCustId) {
        this.corporateCustId = corporateCustId;
    }

    /*
     * public EInvoiceType getInvoivePaymentType() { return invoivePaymentType;
     * }
     * 
     * public void setInvoicePaymentType(EInvoiceType invoivePaymentType) {
     * this.invoivePaymentType = invoivePaymentType; }
     */

    public boolean isOfflineFlow() {
        return offlineFlow;
    }

    public void setOfflineFlow(boolean offlineFlow) {
        this.offlineFlow = offlineFlow;
    }

    public String getForceDirectChannel() {
        return forceDirectChannel;
    }

    public void setForceDirectChannel(String forceDirectChannel) {
        this.forceDirectChannel = forceDirectChannel;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public String getMerchantVPA() {
        return merchantVPA;
    }

    public void setMerchantVPA(String merchantVPA) {
        this.merchantVPA = merchantVPA;
    }

    public boolean isEnhancedDynamicUPIQRCodeAllowed() {
        return enhancedDynamicUPIQRCodeAllowed;
    }

    public void setEnhancedDynamicUPIQRCodeAllowed(boolean enhancedDynamicUPIQRCodeAllowed) {
        this.enhancedDynamicUPIQRCodeAllowed = enhancedDynamicUPIQRCodeAllowed;
    }

    public boolean isEnhancedCashierPaymentRequest() {
        return enhancedCashierPaymentRequest;
    }

    public void setEnhancedCashierPaymentRequest(boolean enhancedCashierPaymentRequest) {
        this.enhancedCashierPaymentRequest = enhancedCashierPaymentRequest;
    }

    public String getAmountForWalletConsultInRisk() {
        return amountForWalletConsultInRisk;
    }

    public void setAmountForWalletConsultInRisk(String amountForWalletConsultInRisk) {
        this.amountForWalletConsultInRisk = amountForWalletConsultInRisk;
    }

    public String getAmountForPaymentFlow() {
        return amountForPaymentFlow;
    }

    public void setAmountForPaymentFlow(String amountForPaymentFlow) {
        this.amountForPaymentFlow = amountForPaymentFlow;
    }

    public boolean isGvConsentFlow() {
        return gvConsentFlow;
    }

    public void setGvConsentFlow(boolean gvConsentFlow) {
        this.gvConsentFlow = gvConsentFlow;
    }

    public boolean isEmiDetailsApi() {
        return emiDetailsApi;
    }

    public void setEmiDetailsApi(boolean emiDetailsApi) {
        this.emiDetailsApi = emiDetailsApi;
    }

    public boolean isDynamicQrRequired() {
        return dynamicQrRequired;
    }

    public void setDynamicQrRequired(boolean dynamicQrRequired) {
        this.dynamicQrRequired = dynamicQrRequired;
    }

    public List<String> getMgvTemplateIds() {
        return mgvTemplateIds;
    }

    public void setMgvTemplateIds(List<String> mgvTemplateIds) {
        this.mgvTemplateIds = mgvTemplateIds;
    }

    public String getMerchantLogo() {
        return merchantLogo;
    }

    public void setMerchantLogo(String merchantLogo) {
        this.merchantLogo = merchantLogo;
    }

    public String getOneClickInfo() {
        return oneClickInfo;
    }

    public void setOneClickInfo(String oneClickInfo) {
        this.oneClickInfo = oneClickInfo;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getEmiSubventedTransactionAmount() {
        return emiSubventedTransactionAmount;
    }

    public void setEmiSubventedTransactionAmount(String emiSubventedTransactionAmount) {
        this.emiSubventedTransactionAmount = emiSubventedTransactionAmount;
    }

    public String getEmiSubventionCustomerId() {
        return emiSubventionCustomerId;
    }

    public void setEmiSubventionCustomerId(String emiSubventionCustomerId) {
        this.emiSubventionCustomerId = emiSubventionCustomerId;
    }

    public boolean isEmiSubventionRequired() {
        return isEmiSubventionRequired;
    }

    public void setEmiSubventionRequired(boolean emiSubventionRequired) {
        isEmiSubventionRequired = emiSubventionRequired;
    }

    public String getSubventionOrderList() {
        return subventionOrderList;
    }

    public void setSubventionOrderList(String subventionOrderList) {
        this.subventionOrderList = subventionOrderList;
    }

    public boolean isInternalFetchPaymentOptions() {
        return isInternalFetchPaymentOptions;
    }

    public void setInternalFetchPaymentOptions(boolean internalFetchPaymentOptions) {
        isInternalFetchPaymentOptions = internalFetchPaymentOptions;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(String originChannel) {
        this.originChannel = originChannel;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getUpiAccRefId() {
        return upiAccRefId;
    }

    public void setUpiAccRefId(String upiAccRefId) {
        this.upiAccRefId = upiAccRefId;
    }

    public boolean isTxnFromCardIndexNo() {
        return txnFromCardIndexNo;
    }

    public void setTxnFromCardIndexNo(boolean txnFromCardIndexNo) {
        this.txnFromCardIndexNo = txnFromCardIndexNo;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public String getUmn() {
        return umn;
    }

    public void setUmn(String umn) {
        this.umn = umn;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isCreateOrderForInitiateTxnRequest() {
        return createOrderForInitiateTxnRequest;
    }

    public void setCreateOrderForInitiateTxnRequest(boolean createOrderForInitiateTxnRequest) {
        this.createOrderForInitiateTxnRequest = createOrderForInitiateTxnRequest;
    }

    public String getAggType() {
        return aggType;
    }

    public void setAggType(String aggType) {
        this.aggType = aggType;
    }

    public OrderPricingInfo getOrderPricingInfo() {
        return orderPricingInfo;
    }

    public void setOrderPricingInfo(OrderPricingInfo orderPricingInfo) {
        this.orderPricingInfo = orderPricingInfo;
    }

    public String getPwpCategory() {
        return pwpCategory;
    }

    public void setPwpCategory(String pwpCategory) {
        this.pwpCategory = pwpCategory;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).setExcludeFieldNames("alipayMID", "dummyAlipayMid").toString();
    }

    public boolean isOfflineTxnFlow() {
        return offlineTxnFlow;
    }

    public void setOfflineTxnFlow(boolean offlineTxnFlow) {
        this.offlineTxnFlow = offlineTxnFlow;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isMlvSupported() {
        return mlvSupported;
    }

    public void setMlvSupported(boolean mlvSupported) {
        this.mlvSupported = mlvSupported;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public QRCodeInfoResponseData getqRCodeInfo() {
        return qRCodeInfo;
    }

    public void setqRCodeInfo(QRCodeInfoResponseData qRCodeInfo) {
        this.qRCodeInfo = qRCodeInfo;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public Date getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(Date debitDate) {
        this.debitDate = debitDate;
    }

    public SimplifiedPaymentOffers getSimplifiedPaymentOffers() {
        return simplifiedPaymentOffers;
    }

    public void setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers) {
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
    }

    public String getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(String payableAmount) {
        this.payableAmount = payableAmount;
    }

    public boolean isScanAndPayFlow() {
        return scanAndPayFlow;
    }

    public void setScanAndPayFlow(boolean scanAndPayFlow) {
        this.scanAndPayFlow = scanAndPayFlow;
    }

    public boolean isExternalFetchPaymentOptions() {
        return isExternalFetchPaymentOptions;
    }

    public void setExternalFetchPaymentOptions(boolean externalFetchPaymentOptions) {
        isExternalFetchPaymentOptions = externalFetchPaymentOptions;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public String getLoyaltyPointRootUserId() {
        return loyaltyPointRootUserId;
    }

    public void setLoyaltyPointRootUserId(String loyaltyPointRootUserId) {
        this.loyaltyPointRootUserId = loyaltyPointRootUserId;
    }

    public boolean isOrderPSPRequest() {
        return isOrderPSPRequest;
    }

    public void setOrderPSPRequest(boolean orderPSPRequest) {
        isOrderPSPRequest = orderPSPRequest;
    }

    public List<SplitCommandInfo> getSplitCommandInfoList() {
        return splitCommandInfoList;
    }

    public void setSplitCommandInfoList(List<SplitCommandInfo> splitCommandInfoList) {
        this.splitCommandInfoList = splitCommandInfoList;
    }

    public boolean isOnusRentPaymentMerchant() {
        return onusRentPaymentMerchant;
    }

    public void setOnusRentPaymentMerchant(boolean onusRentPaymentMerchant) {
        this.onusRentPaymentMerchant = onusRentPaymentMerchant;
    }

    public String getSubsPaymentType() {
        return subsPaymentType;
    }

    public void setSubsPaymentType(String subsPaymentType) {
        this.subsPaymentType = subsPaymentType;
    }

    public boolean isInternalRequestForSubsPayment() {
        return isInternalRequestForSubsPayment;
    }

    public void setInternalRequestForSubsPayment(boolean internalRequestForSubsPayment) {
        isInternalRequestForSubsPayment = internalRequestForSubsPayment;
    }

    public String getRiskVerifyTxnToken() {
        return riskVerifyTxnToken;
    }

    public void setRiskVerifyTxnToken(String riskVerifyTxnToken) {
        this.riskVerifyTxnToken = riskVerifyTxnToken;
    }

    public String getOrderExpiryTimeInMerchantContract() {
        return orderExpiryTimeInMerchantContract;
    }

    public void setOrderExpiryTimeInMerchantContract(String orderExpiryTimeInMerchantContract) {
        this.orderExpiryTimeInMerchantContract = orderExpiryTimeInMerchantContract;
    }

    public String getRefServiceInstId() {
        return refServiceInstId;
    }

    public void setRefServiceInstId(String refServiceInstId) {
        this.refServiceInstId = refServiceInstId;
    }

    public boolean isTransitWallet() {
        return transitWallet;
    }

    public void setTransitWallet(boolean transitWallet) {
        this.transitWallet = transitWallet;
    }

    public boolean isEcomTokenTxn() {
        return isEcomTokenTxn;
    }

    public void setEcomTokenTxn(boolean ecomTokenTxn) {
        isEcomTokenTxn = ecomTokenTxn;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getPspApp() {
        return pspApp;
    }

    public void setPspApp(String pspApp) {
        this.pspApp = pspApp;
    }

    public String getSubsOriginalAmount() {
        return subsOriginalAmount;
    }

    public void setSubsOriginalAmount(String subsOriginalAmount) {
        this.subsOriginalAmount = subsOriginalAmount;
    }

    public String getPreAuthExpiryDate() {
        return preAuthExpiryDate;
    }

    public void setPreAuthExpiryDate(String preAuthExpiryDate) {
        this.preAuthExpiryDate = preAuthExpiryDate;
    }

    public boolean isPreAuth() {
        return preAuth;
    }

    public void setPreAuth(boolean preAuth) {
        this.preAuth = preAuth;
    }

    public String getSavedCardPayMethod() {
        return savedCardPayMethod;
    }

    public void setSavedCardPayMethod(String savedCardPayMethod) {
        this.savedCardPayMethod = savedCardPayMethod;
    }

    public boolean isAddAndPayWithUPICollectSupported() {
        return addAndPayWithUPICollectSupported;
    }

    public void setAddAndPayWithUPICollectSupported(boolean addAndPayWithUPICollectSupported) {
        this.addAndPayWithUPICollectSupported = addAndPayWithUPICollectSupported;
    }

    public boolean isCostBasedPreferenceEnabled() {
        return costBasedPreferenceEnabled;
    }

    public void setCostBasedPreferenceEnabled(boolean costBasedPreferenceEnabled) {
        this.costBasedPreferenceEnabled = costBasedPreferenceEnabled;
    }

    public boolean isWillUserChange() {
        return willUserChange;
    }

    public void setWillUserChange(boolean willUserChange) {
        this.willUserChange = willUserChange;
    }

    public boolean isNotificationAppInvoke() {
        return notificationAppInvoke;
    }

    public void setIsAddMoneyAppInvoke(boolean isAddMoneyAppInvoke) {
        this.notificationAppInvoke = isAddMoneyAppInvoke;
    }

    public String getAppInvokeURL() {
        return appInvokeURL;
    }

    public void setAppInvokeURL(String appInvokeURL) {
        this.appInvokeURL = appInvokeURL;
    }

    public String getPaymentPromoCheckoutDataPromoCode() {
        return paymentPromoCheckoutDataPromoCode;
    }

    public void setPaymentPromoCheckoutDataPromoCode(String paymentPromoCheckoutDataPromoCode) {
        this.paymentPromoCheckoutDataPromoCode = paymentPromoCheckoutDataPromoCode;
    }

    public String getAddMoneySource() {
        return addMoneySource;
    }

    public void setAddMoneySource(String addMoneySource) {
        this.addMoneySource = addMoneySource;
    }

    public Map<String, String> getAdditionalOrderExtendInfo() {
        return additionalOrderExtendInfo;
    }

    public void setAdditionalOrderExtendInfo(Map<String, String> additionalOrderExtendInfo) {
        this.additionalOrderExtendInfo = additionalOrderExtendInfo;
    }

    public String getUpiOrderTimeOutInSeconds() {
        return upiOrderTimeOutInSeconds;
    }

    public void setUpiOrderTimeOutInSeconds(String upiOrderTimeOutInSeconds) {
        this.upiOrderTimeOutInSeconds = upiOrderTimeOutInSeconds;
    }

    public ERequestType getSubRequestType() {
        return subRequestType;
    }

    public void setSubRequestType(ERequestType subRequestType) {
        this.subRequestType = subRequestType;
    }

    public String getPaymentMid() {
        return paymentMid;
    }

    public void setPaymentMid(String paymentMid) {
        this.paymentMid = paymentMid;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getDummyAlipayMid() {
        return dummyAlipayMid;
    }

    public void setDummyAlipayMid(String dummyAlipayMid) {
        this.dummyAlipayMid = dummyAlipayMid;
    }

    public boolean isInstaDirectForm() {
        return instaDirectForm;
    }

    public void setInstaDirectForm(boolean instaDirectForm) {
        this.instaDirectForm = instaDirectForm;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isFlexiSubscription() {
        return flexiSubscription;
    }

    public void setFlexiSubscription(boolean flexiSubscription) {
        this.flexiSubscription = flexiSubscription;
    }

    public boolean getDccSupported() {
        return dccSupported;
    }

    public void setDccSupported(boolean dccSupported) {
        this.dccSupported = dccSupported;
    }

    public boolean isPaymentCallFromDccPage() {
        return paymentCallFromDccPage;
    }

    public void setPaymentCallFromDccPage(boolean paymentCallFromDccPage) {
        this.paymentCallFromDccPage = paymentCallFromDccPage;
    }

    public String getDccServiceInstId() {
        return dccServiceInstId;
    }

    public void setDccServiceInstId(String dccServiceInstId) {
        this.dccServiceInstId = dccServiceInstId;
    }

    public boolean isDccSupported() {
        return dccSupported;
    }

    public boolean isDccSelectedByUser() {
        return dccSelectedByUser;
    }

    public void setDccSelectedByUser(boolean dccSelectedByUser) {
        this.dccSelectedByUser = dccSelectedByUser;
    }

    public DccPageData getDccPageData() {
        return dccPageData;
    }

    public void setDccPageData(DccPageData dccPageData) {
        this.dccPageData = dccPageData;
    }

    public boolean getUseInvestmentAsFundingSource() {
        return useInvestmentAsFundingSource;
    }

    public void setUseInvestmentAsFundingSource(boolean useInvestmentAsFundingSource) {
        this.useInvestmentAsFundingSource = useInvestmentAsFundingSource;
    }

    public boolean isInternationalCard() {
        return isInternationalCard;
    }

    public void setInternationalCard(boolean internationalCard) {
        isInternationalCard = internationalCard;
    }

    public CheckoutPromoServiceResponse getCheckoutPromoServiceResponse() {
        return checkoutPromoServiceResponse;
    }

    public void setCheckoutPromoServiceResponse(CheckoutPromoServiceResponse checkoutPromoServiceResponse) {
        this.checkoutPromoServiceResponse = checkoutPromoServiceResponse;

    }

    public boolean isPaymentResumed() {
        return paymentResumed;
    }

    public void setPaymentResumed(boolean paymentResumed) {
        this.paymentResumed = paymentResumed;
    }

    public boolean isUpiDynamicQrPaymentExceptEnhanceQR() {
        return upiDynamicQrPaymentExceptEnhanceQR;
    }

    public void setUpiDynamicQrPaymentExceptEnhanceQR(boolean upiDynamicQrPaymentExceptEnhanceQR) {
        this.upiDynamicQrPaymentExceptEnhanceQR = upiDynamicQrPaymentExceptEnhanceQR;
    }

    public boolean isCheckoutJsAppInvokePayment() {
        return checkoutJsAppInvokePayment;
    }

    public void setCheckoutJsAppInvokePayment(boolean checkoutJsAppInvokePayment) {
        this.checkoutJsAppInvokePayment = checkoutJsAppInvokePayment;
    }

    public String getIsOfflineTxn() {
        return isOfflineTxn;
    }

    public void setIsOfflineTxn(String isOfflineTxn) {
        this.isOfflineTxn = isOfflineTxn;
    }

    public Map<String, String> getPosOrderData() {
        return posOrderData;
    }

    public void setPosOrderData(Map<String, String> posOrderData) {
        this.posOrderData = posOrderData;
    }

    public BinDetail getBinDetail() {
        return binDetail;
    }

    public void setBinDetail(BinDetail binDetail) {
        this.binDetail = binDetail;
    }

    public VanInfo getVanInfo() {
        return vanInfo;
    }

    public void setVanInfo(VanInfo vanInfo) {
        this.vanInfo = vanInfo;
    }

    public PaymentOfferV2 getPaymentOffersAppliedV2() {
        return paymentOffersAppliedV2;
    }

    public void setPaymentOffersAppliedV2(PaymentOfferV2 paymentOffersAppliedV2) {
        this.paymentOffersAppliedV2 = paymentOffersAppliedV2;
    }

    public String getRequestFlow() {
        return requestFlow;
    }

    public void setRequestFlow(String requestFlow) {
        this.requestFlow = requestFlow;
    }

    public String getPreferredOtpPage() {
        return preferredOtpPage;
    }

    public void setPreferredOtpPage(String preferredOtpPage) {
        this.preferredOtpPage = preferredOtpPage;
    }

    public boolean isPushDataToDynamicQR() {
        return pushDataToDynamicQR;
    }

    public void setPushDataToDynamicQR(boolean pushDataToDynamicQR) {
        this.pushDataToDynamicQR = pushDataToDynamicQR;
    }

    public Long getPreAuthBlockSeconds() {
        return preAuthBlockSeconds;
    }

    public void setPreAuthBlockSeconds(Long preAuthBlockSeconds) {
        this.preAuthBlockSeconds = preAuthBlockSeconds;
    }

    public EPreAuthType getCardPreAuthType() {
        return cardPreAuthType;
    }

    public void setCardPreAuthType(EPreAuthType cardPreAuthType) {
        this.cardPreAuthType = cardPreAuthType;

    }

    public boolean isUpiConvertedToAddNPay() {
        return upiConvertedToAddNPay;
    }

    public void setUpiConvertedToAddNPay(boolean upiConvertedToAddNPay) {
        this.upiConvertedToAddNPay = upiConvertedToAddNPay;
    }

    public String getFromAOARequest() {
        return fromAOARequest;
    }

    public void setFromAOARequest(String fromAOARequest) {
        this.fromAOARequest = fromAOARequest;
    }

    public boolean isFetchPaytmInstrumentsBalance() {
        return fetchPaytmInstrumentsBalance;
    }

    public void setFetchPaytmInstrumentsBalance(boolean fetchPaytmInstrumentsBalance) {
        this.fetchPaytmInstrumentsBalance = fetchPaytmInstrumentsBalance;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public CoftConsent getCoftConsent() {
        return coftConsent;
    }

    public void setCoftConsent(CoftConsent coftConsent) {
        this.coftConsent = coftConsent;
    }

    public boolean isReturnToken() {
        return returnToken;
    }

    public void setReturnToken(boolean returnToken) {
        this.returnToken = returnToken;
    }

    public boolean isCoftTokenTxn() {
        return isCoftTokenTxn;
    }

    public void setCoftTokenTxn(boolean coftTokenTxn) {
        isCoftTokenTxn = coftTokenTxn;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    public boolean isBinEligibleForCoft() {
        return isBinEligibleForCoft;
    }

    public void setBinEligibleForCoft(boolean binEligibleForCoft) {
        isBinEligibleForCoft = binEligibleForCoft;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public boolean isOverrideAddNPayBehaviourEnabled() {
        return isOverrideAddNPayBehaviour;
    }

    public void setOverrideAddNPayBehaviour(boolean overrideAddNPayBehaviour) {
        this.isOverrideAddNPayBehaviour = overrideAddNPayBehaviour;
    }

    public String getEmiSubventionStratergy() {
        return emiSubventionStratergy;
    }

    public void setEmiSubventionStratergy(String emiSubventionStratergy) {
        this.emiSubventionStratergy = emiSubventionStratergy;
    }

    public Boolean getEdcLinkTxn() {
        return edcLinkTxn;
    }

    public void setEdcLinkTxn(Boolean edcLinkTxn) {
        this.edcLinkTxn = edcLinkTxn;
    }

    public Map<String, String> getEmiDetailInfo() {
        return emiDetailInfo;
    }

    public void setEmiDetailInfo(Map<String, String> emiDetailInfo) {
        this.emiDetailInfo = emiDetailInfo;
    }

    public boolean isPG2PreferenceEnabledOnMid() {
        return isPG2PreferenceEnabledOnMid;
    }

    public void setPG2PreferenceEnabledOnMid(boolean pG2PreferenceEnabledOnMid) {
        isPG2PreferenceEnabledOnMid = pG2PreferenceEnabledOnMid;
    }

    public boolean isFetchPostpaidBalance() {
        return fetchPostpaidBalance;
    }

    public void setFetchPostpaidBalance(boolean fetchPostpaidBalance) {
        this.fetchPostpaidBalance = fetchPostpaidBalance;
    }

    public String getPG2EnabledPaymodesPayoptions() {
        return PG2EnabledPaymodesPayoptions;
    }

    public void setPG2EnabledPaymodesPayoptions(String PG2EnabledPaymodesPayoptions) {
        this.PG2EnabledPaymodesPayoptions = PG2EnabledPaymodesPayoptions;
    }

    public boolean isSubsAoaPgMidTxn() {
        return subsAoaPgMidTxn;
    }

    public void setSubsAoaPgMidTxn(boolean subsAoaPgMidTxn) {
        this.subsAoaPgMidTxn = subsAoaPgMidTxn;
    }

    public List<TpvInfo> getTpvInfos() {
        return tpvInfos;
    }

    public void setTpvInfos(List<TpvInfo> tpvInfos) {
        this.tpvInfos = tpvInfos;
    }

    public String getFeesAmount() {
        return feesAmount;
    }

    public void setFeesAmount(String feesAmount) {
        this.feesAmount = feesAmount;
    }

    public boolean isAddnpayPreferenceOnMerchant() {
        return addnpayPreferenceOnMerchant;
    }

    public void setAddnpayPreferenceOnMerchant(boolean addnpayPreferenceOnMerchant) {
        this.addnpayPreferenceOnMerchant = addnpayPreferenceOnMerchant;
    }

    public boolean isMerchantEligibleForMultipleMBIDFlow() {
        return isMerchantEligibleForMultipleMBIDFlow;
    }

    public void setMerchantEligibleForMultipleMBIDFlow(boolean isMerchantEligibleForMultipleMBIDFlow) {
        this.isMerchantEligibleForMultipleMBIDFlow = isMerchantEligibleForMultipleMBIDFlow;

    }

    public String getGcin() {
        return gcin;
    }

    public void setGcin(String gcin) {
        this.gcin = gcin;
    }

    public String getMerchantCoftConfig() {
        return merchantCoftConfig;
    }

    public void setMerchantCoftConfig(String merchantCoftConfig) {
        this.merchantCoftConfig = merchantCoftConfig;
    }

    public boolean isEnableGcinOnCoftPromo() {
        return isEnableGcinOnCoftPromo;
    }

    public void setEnableGcinOnCoftPromo(boolean enableGcinOnCoftPromo) {
        isEnableGcinOnCoftPromo = enableGcinOnCoftPromo;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getAccountRangeCardBin() {
        return accountRangeCardBin;
    }

    public void setAccountRangeCardBin(String accountRangeCardBin) {
        this.accountRangeCardBin = accountRangeCardBin;
    }

    public boolean isAddNPayGlobalVaultCards() {
        return addNPayGlobalVaultCards;
    }

    public void setAddNPayGlobalVaultCards(boolean addNPayGlobalVaultCards) {
        this.addNPayGlobalVaultCards = addNPayGlobalVaultCards;
    }

    public boolean isGlobalVaultCoftPreference() {
        return globalVaultCoftPreference;
    }

    public void setGlobalVaultCoftPreference(boolean globalVaultCoftPreference) {
        this.globalVaultCoftPreference = globalVaultCoftPreference;
    }

    public UserWalletFreezeDetails getUserWalletFreezeDetails() {
        return userWalletFreezeDetails;
    }

    public void setUserWalletFreezeDetails(UserWalletFreezeDetails userWalletFreezeDetails) {
        this.userWalletFreezeDetails = userWalletFreezeDetails;
    }

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public String getUpiPspReqMsgId() {
        return upiPspReqMsgId;
    }

    public void setUpiPspReqMsgId(String upiPspReqMsgId) {
        this.upiPspReqMsgId = upiPspReqMsgId;
    }

    public String getSettleType() {
        return settleType;
    }

    public void setSettleType(String settleType) {
        this.settleType = settleType;
    }

    public boolean isInstantSettlementEnabled() {
        return instantSettlementEnabled;
    }

    public void setInstantSettlementEnabled(boolean instantSettlementEnabled) {
        this.instantSettlementEnabled = instantSettlementEnabled;
    }

    public boolean isDisableLimitCCAddNPay() {
        return disableLimitCCAddNPay;
    }

    public void setDisableLimitCCAddNPay(boolean disableLimitCCAddNPay) {
        this.disableLimitCCAddNPay = disableLimitCCAddNPay;
    }

    public String getPpblAccountType() {
        return ppblAccountType;
    }

    public void setPpblAccountType(String ppblAccountType) {
        this.ppblAccountType = ppblAccountType;
    }

    public boolean isAllowInternalRetryOnDirectBankCardFlow() {
        return allowInternalRetryOnDirectBankCardFlow;
    }

    public void setAllowInternalRetryOnDirectBankCardFlow(boolean allowInternalRetryOnDirectBankCardFlow) {
        this.allowInternalRetryOnDirectBankCardFlow = allowInternalRetryOnDirectBankCardFlow;
    }

    public boolean isCreateNonQRDeepLink() {
        return createNonQRDeepLink;
    }

    public void setCreateNonQRDeepLink(boolean createNonQRDeepLink) {
        this.createNonQRDeepLink = createNonQRDeepLink;
    }

    public TwoFADetails getTwoFADetails() {
        return twoFADetails;
    }

    public void setTwoFADetails(TwoFADetails twoFADetails) {
        this.twoFADetails = twoFADetails;
    }

    public TwoFAConfig getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFAConfig twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public String getCustomDisplayName() {
        return customDisplayName;
    }

    public void setCustomDisplayName(String customDisplayName) {
        this.customDisplayName = customDisplayName;
    }

    public boolean isCcOnUPIEnabled() {
        return ccOnUPIEnabled;
    }

    public void setCcOnUPIEnabled(boolean ccOnUPIEnabled) {
        this.ccOnUPIEnabled = ccOnUPIEnabled;
    }

    public boolean isCcOnUPIEnabledForPtcTxn() {
        return ccOnUPIEnabledForPtcTxn;
    }

    public void setCcOnUPIEnabledForPtcTxn(boolean ccOnUPIEnabledForPtcTxn) {
        this.ccOnUPIEnabledForPtcTxn = ccOnUPIEnabledForPtcTxn;
    }

    public boolean isUpiLite() {
        return upiLite;
    }

    public void setUpiLite(boolean upiLite) {
        this.upiLite = upiLite;
    }

    public Boolean getAddOneRupee() {
        return addOneRupee;
    }

    public void setAddOneRupee(Boolean addOneRupee) {
        this.addOneRupee = addOneRupee;
    }

    public BankFormOptimizationParams getBankFormOptimizationParams() {
        return bankFormOptimizationParams;
    }

    public void setBankFormOptimizationParams(BankFormOptimizationParams bankFormOptimizationParams) {
        this.bankFormOptimizationParams = bankFormOptimizationParams;
    }

    public boolean isSuperGwFpoApiHit() {
        return superGwFpoApiHit;
    }

    public void setSuperGwFpoApiHit(boolean superGwFpoApiHit) {
        this.superGwFpoApiHit = superGwFpoApiHit;
    }

    public boolean isICBFlow() {
        return isICBFlow;
    }

    public void setICBFlow(boolean ICBFlow) {
        this.isICBFlow = ICBFlow;
    }

    public CheckoutPromoServiceResponseV2 getPromoCheckoutInfo() {
        return promoCheckoutInfo;
    }

    public void setPromoCheckoutInfo(CheckoutPromoServiceResponseV2 promoCheckoutInfo) {
        this.promoCheckoutInfo = promoCheckoutInfo;
    }

    public GenericEmiSubventionResponse<CheckOutResponse> getSubventionCheckoutInfo() {
        return subventionCheckoutInfo;
    }

    public void setSubventionCheckoutInfo(GenericEmiSubventionResponse<CheckOutResponse> subventionCheckoutInfo) {
        this.subventionCheckoutInfo = subventionCheckoutInfo;
    }

    public List<String> getBrandCodes() {
        return brandCodes;
    }

    public void setBrandCodes(List<String> brandCodes) {
        this.brandCodes = brandCodes;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Map<String, String> getPromoContextICB() {
        return promoContextICB;
    }

    public void setPromoContextICB(Map<String, String> promoContextICB) {
        this.promoContextICB = promoContextICB;
    }

    public List<ItemWithOrder> getItemsICB() {
        return itemsICB;
    }

    public void setItemsICB(List<ItemWithOrder> itemsICB) {
        this.itemsICB = itemsICB;
    }

    public boolean isAddNPayDisabledForZeroBalance() {
        return addNPayDisabledForZeroBalance;
    }

    public void setAddNPayDisabledForZeroBalance(boolean addNPayDisabledForZeroBalance) {
        this.addNPayDisabledForZeroBalance = addNPayDisabledForZeroBalance;
    }

    public boolean isQRIdFlowOnly() {
        return isQRIdFlowOnly;
    }

    public void setQRIdFlowOnly(boolean qrIdFlowOnly) {
        isQRIdFlowOnly = qrIdFlowOnly;
    }

    public String getQrTxnAmount() {
        return qrTxnAmount;
    }

    public void setQrTxnAmount(String qrTxnAmount) {
        this.qrTxnAmount = qrTxnAmount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    public String getFeeRateCode() {
        return feeRateCode;
    }

    public void setFeeRateCode(String feeRateCode) {
        this.feeRateCode = feeRateCode;
    }

    public String getUpiIntentAllowedRetries() {
        return upiIntentAllowedRetries;
    }

    public void setUpiIntentAllowedRetries(String upiIntentAllowedRetries) {
        this.upiIntentAllowedRetries = upiIntentAllowedRetries;
    }

    public boolean isGenerateEsnRequest() {
        return generateEsnRequest;
    }

    public void setGenerateEsnRequest(boolean generateEsnRequest) {
        this.generateEsnRequest = generateEsnRequest;
    }

    public String getPostTxnSplitTimeout() {
        return postTxnSplitTimeout;
    }

    public void setPostTxnSplitTimeout(String postTxnSplitTimeout) {
        this.postTxnSplitTimeout = postTxnSplitTimeout;
    }

    public SplitTypeEnum getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitTypeEnum splitType) {
        this.splitType = splitType;
    }

}
