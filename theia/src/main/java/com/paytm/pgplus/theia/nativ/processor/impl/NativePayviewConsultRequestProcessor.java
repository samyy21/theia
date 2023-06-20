package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.enums.BankTransferCheckoutFlow;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.link.EdcEmiBankOfferDetails;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.model.link.EdcEmiOfferDetails;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.common.util.PayMethodUtility;
import com.paytm.pgplus.enums.AppInvokeType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.deals.models.response.DealsResponse;
import com.paytm.pgplus.facade.deals.services.DealsService;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.enums.QrType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.SearchPaymentOffersServiceResponseV2;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.RecurringMandatePsp;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.MappingServiceClient;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.enums.MerchantUserRequestType;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantQueryService;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.accesstoken.model.AccessTokenBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.controllers.helper.ProcessTransactionControllerHelper;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.exceptions.PWPPromoServiceException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.helper.MobileMaskHelper;
import com.paytm.pgplus.theia.helper.PGPreferenceHelper;
import com.paytm.pgplus.theia.models.FPODisablePaymentMode;
import com.paytm.pgplus.theia.models.MerchantDetails;
import com.paytm.pgplus.theia.models.UserAgentInfo;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.*;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.model.payview.response.UPI;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.subscription.SubscriptionDetail;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IPayviewConsultService;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.service.NativePaymentBulkOffersService;
import com.paytm.pgplus.theia.nativ.service.NativePaymentBulkOffersServiceV2;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.EMISubventionStrategy;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.request.SubventionDetails;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyItemOffers;
import com.paytm.pgplus.theia.paymentoffer.model.response.PaymentOffersData;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.supercashoffer.helper.SuperCashServiceHelper;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.FPODisableLinkPaymodesUtils;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.SUBWALLET_AMOUNT_DETAILS;
import static com.paytm.pgplus.biz.utils.BizConstant.UPI_POS_ORDER;
import static com.paytm.pgplus.biz.utils.BizConstant.UPI_QR_CODE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.DYNAMIC_QR_REQUIRED;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ADDITIONALINFO;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.SHOW_UPI_QR;
import static com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.INVALID_PROMOCODE;
import static com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils.getTokenExpiryTime;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PrepaidCard.PREPAID_CARD_MAX_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.LINK_BASED_KEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;

/**
 * This is specific to Native PayviewConsult request "CashierInfoRequest" and
 * response "CashierInfoResponse".
 */
@Service
public class NativePayviewConsultRequestProcessor
        extends
        AbstractRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse, CashierInfoContainerRequest, WorkFlowResponseBean> {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativePayviewConsultRequestProcessor.class);
    @Autowired
    @Qualifier("userLoggedInLitePayviewConsultService")
    private IPayviewConsultService userLoggedInPayviewService;

    @Autowired
    @Qualifier("userNotLoggedInLitePayviewConsultService")
    private IPayviewConsultService userNotLoggedInPayviewService;

    @Autowired
    @Qualifier("nativePayviewConsultServiceHelper")
    private IPayviewConsultServiceHelper<NativeCashierInfoRequest, NativeCashierInfoResponse> payviewConsultServiceHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("paymentOffersServiceHelper")
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    @Qualifier("nativePaymentBulkOffersService")
    private NativePaymentBulkOffersService nativePaymentBulkOffersService;

    @Autowired
    @Qualifier("nativePaymentBulkOffersServiceV2")
    private NativePaymentBulkOffersServiceV2 nativePaymentBulkOffersServiceV2;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private AcquiringUtil acquiringUtil;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    private PGPreferenceHelper pgPreferenceHelper;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    @Qualifier("processTransactionControllerHelper")
    ProcessTransactionControllerHelper processTransactionControllerHelper;

    @Autowired
    @Qualifier("merchantQueryServiceImpl")
    private IMerchantQueryService merchantQueryService;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    LinkPaymentUtil linkPaymentUtil;

    @Autowired
    @Qualifier("superCashServiceHelper")
    private SuperCashServiceHelper superCashServiceHelper;

    @Autowired
    private MobileMaskHelper mobileMaskHelper;

    @Autowired
    @Qualifier("dealsService")
    private DealsService dealsService;

    @Autowired
    @Qualifier("subscriptionNativeValidationService")
    private ISusbcriptionNativeValidationService susbcriptionNativeValidationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayviewConsultRequestProcessor.class);

    @Override
    protected CashierInfoContainerRequest preProcess(NativeCashierInfoContainerRequest request) {

        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        updateChannelId(request);
        if (nativeCashierInfoRequest != null && nativeCashierInfoRequest.getBody() != null) {
            if (nativeCashierInfoRequest.getBody().isExternalFetchPaymentOptions()) {
                nativeCashierInfoRequest.getBody().setPaymodeSequenceEnum(PaymodeSequenceEnum.NATIVE);
            }
            disablePaymodesForStaticQR(request, nativeCashierInfoRequest);
            updatePaymentMethods(nativeCashierInfoRequest.getBody().getEnablePaymentMode());
            disablePaymodesForLinkPayments(nativeCashierInfoRequest.getBody(), request.getPaymentRequestBean());
            updatePaymentMethods(nativeCashierInfoRequest.getBody().getDisablePaymentMode());

            if (TokenType.CHECKSUM.equals(nativeCashierInfoRequest.getHead().getTokenType())
                    || TokenType.ACCESS.equals(nativeCashierInfoRequest.getHead().getTokenType())) {
                validateRequestAndSetTokenTypeSSO(request, nativeCashierInfoRequest.getHead().getTokenType());
            }

            // For SSO Token Flow
            if (nativeCashierInfoRequest.getBody().getApplyItemOffers() != null
                    && nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext() != null
                    && nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext()
                            .get("affordabilityInfo") != null) {
                try {
                    nativeCashierInfoRequest.getBody().setAffordabilityInfo(
                            JsonMapper.mapJsonToObject(nativeCashierInfoRequest.getBody().getApplyItemOffers()
                                    .getPromoContext().get("affordabilityInfo"), Map.class));
                } catch (FacadeCheckedException e) {
                    EXT_LOGGER.customWarn("Failed to parse affordabilityInfo in fpo preProcess");
                }
            }

            if (!nativeCashierInfoRequest.getBody().isUpiLiteEligible()
                    && merchantPreferenceService.isUpiLiteEnabled(nativeCashierInfoRequest.getBody().getMid(), false))
                nativeCashierInfoRequest.getBody().setUpiLiteEligible(true);
        }

        // todo PAYMENT CONTEXT TOKEN PRE PROCESSING
        if (request.isSuperGwApiHit()) {
            return preProcessWithPaymentContext(request);
        }

        if (TokenType.SSO.equals(nativeCashierInfoRequest.getHead().getTokenType())) {
            return preProcessWithSSOToken(request);
        }
        if (TokenType.GUEST.equals(nativeCashierInfoRequest.getHead().getTokenType())) {
            return preProcessForGuest(request);
        }

        processTransactionUtil.pushFetchPaymentOptionsEvent(EventNameEnum.ONLINE_FETCHPAYMENTOPTIONS);

        String txnToken = nativeCashierInfoRequest.getHead().getTxnToken();

        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        filterPayModesForEdcEmiLinkPayment(orderDetail, nativeCashierInfoRequest);
        disableUPICollectIfMerchantNotVerified(orderDetail, request);
        updatePayModesIfRequired(nativeCashierInfoRequest, orderDetail, txnToken);
        disableUPICollectForNonLoggedIn(orderDetail);
        if (Objects.nonNull(orderDetail.getAffordabilityInfo()))
            request.getNativeCashierInfoRequest().getBody().setAffordabilityInfo(orderDetail.getAffordabilityInfo());
        if (!orderDetail.isAoaSubsOnPgMid()) {
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        }

        CashierInfoRequest serviceRequestData = generateServiceRequestData(request, nativeCashierInfoRequest,
                orderDetail);
        if (null != request.getNativeCashierInfoRequest() && null != request.getNativeCashierInfoRequest().getBody()) {
            serviceRequestData.getBody().setStaticQrCode(
                    request.getNativeCashierInfoRequest().getBody().isStaticQrCode());
        }
        enrich(serviceRequestData, orderDetail);
        enrich(nativeCashierInfoRequest, orderDetail);
        enrichSubscriptionSpecific(serviceRequestData, orderDetail);
        disableBankTransferIfRequired(serviceRequestData, orderDetail);
        disableCodIfRequired(serviceRequestData, nativeCashierInfoRequest);
        enrichPreAuth(serviceRequestData, orderDetail);
        if (nativeInitiateRequest.getNativePersistData() != null) {
            serviceRequestData.getBody().setNativePersistData(nativeInitiateRequest.getNativePersistData());
        }
        request.setNativeCashierInfoRequest(nativeCashierInfoRequest);
        CashierInfoContainerRequest serviceRequest = new CashierInfoContainerRequest(serviceRequestData,
                request.getPaymentRequestBean());
        return serviceRequest;
    }

    private void disablePaymodesForStaticQR(NativeCashierInfoContainerRequest request,
            NativeCashierInfoRequest nativeCashierInfoRequest) {
        String mid = nativeCashierInfoRequest.getBody().getMid();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = nativeCashierInfoRequest.getBody();
        if (isFetchQRDetailOrOfflineFetchPaymentOptionRequest(request, mid, nativeCashierInfoRequestBody)) {
            String merchantSolutionType = getMerchantSolutionType(mid);
            boolean merchantOnPaytm = merchantExtendInfoUtils.isMerchantOnPaytm(mid);
            if (StringUtils.equalsIgnoreCase(merchantSolutionType, BizConstant.OFFLINE) && !merchantOnPaytm) {
                List<PaymentMode> disabledPaymentModes = nativeCashierInfoRequestBody.getDisablePaymentMode() != null ? nativeCashierInfoRequestBody
                        .getDisablePaymentMode() : new ArrayList<>();
                disabledPaymentModes.add(new PaymentMode(EPayMethod.CREDIT_CARD.getMethod()));
                disabledPaymentModes.add(new PaymentMode(EPayMethod.DEBIT_CARD.getMethod()));
                nativeCashierInfoRequestBody.setDisablePaymentMode(disabledPaymentModes);
            }
        }
    }

    private boolean isFetchQRDetailOrOfflineFetchPaymentOptionRequest(NativeCashierInfoContainerRequest request,
            String mid, NativeCashierInfoRequestBody nativeCashierInfoRequestBody) {
        return (request.isFetchQRDetailsRequest() && nativeCashierInfoRequestBody != null
                && nativeCashierInfoRequestBody.isStaticQrCode() && ff4jUtils.isFeatureEnabledOnMid(mid,
                TheiaConstant.FF4J.DISABLE_CARDS_FOR_STATIC_QR_FQR, false))
                || (!request.isFetchQRDetailsRequest() && nativeCashierInfoRequestBody != null
                        && nativeCashierInfoRequestBody.isOfflineFlow() && ff4jUtils.isFeatureEnabledOnMid(mid,
                        TheiaConstant.FF4J.DISABLE_CARDS_FOR_STATIC_QR_FPO, false));
    }

    private void disableUPICollectIfMerchantNotVerified(InitiateTransactionRequestBody orderDetail,
            NativeCashierInfoContainerRequest request) {
        try {
            if (request == null || request.getNativeCashierInfoRequest() == null
                    || request.getNativeCashierInfoRequest().getBody() == null) {
                return;
            }
            NativeCashierInfoRequestBody nativeCashierInfoRequestBody = request.getNativeCashierInfoRequest().getBody();
            String mid = null;
            Double orderAmount = null;
            if (orderDetail != null && orderDetail.getMid() != null && orderDetail.getTxnAmount() != null
                    && StringUtils.isNotBlank(orderDetail.getTxnAmount().getValue())) {
                mid = orderDetail.getMid();
                orderAmount = Double.parseDouble(orderDetail.getTxnAmount().getValue());
            } else if (StringUtils.isNotBlank(nativeCashierInfoRequestBody.getMid())
                    && StringUtils.isNotBlank(nativeCashierInfoRequestBody.getOrderAmount())) {
                mid = nativeCashierInfoRequestBody.getMid();
                orderAmount = Double.parseDouble(nativeCashierInfoRequestBody.getOrderAmount());
            }

            if (StringUtils.isBlank(mid) || orderAmount == null) {
                return;
            }

            boolean disableUPICollect = !merchantPreferenceService.isUpiCollectWhitelisted(mid, true);
            Double upiCollectLimitUnverified = Double.parseDouble(com.paytm.pgplus.theia.utils.ConfigurationUtil
                    .getProperty(UPI_COLLECT_LIMIT_UNVERIFIED, "2000"));
            if (orderAmount > upiCollectLimitUnverified && disableUPICollect) {
                PaymentMode upiCollectPaymentMode = new PaymentMode(UPI);
                List<String> upiChannels = new ArrayList<>();
                upiChannels.add(UPI);
                upiCollectPaymentMode.setChannels(upiChannels);
                if (Objects.isNull(nativeCashierInfoRequestBody.getDisablePaymentMode())) {
                    request.getNativeCashierInfoRequest().getBody().setDisablePaymentMode(new ArrayList<>());
                }
                request.getNativeCashierInfoRequest().getBody().getDisablePaymentMode().add(upiCollectPaymentMode);
                LOGGER.info("Disabling upi collect as txn Amt > {} and Merchant is not UPI verified :{}",
                        upiCollectLimitUnverified, mid);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while trying to check upi disabled at merchant", e);
        }
    }

    private CashierInfoRequest generateServiceRequestData(NativeCashierInfoContainerRequest request,
            NativeCashierInfoRequest nativeCashierInfoRequest, InitiateTransactionRequestBody orderDetail) {
        CashierInfoRequest serviceRequestData = null;
        if (ObjectUtils.notEqual(request.getNativeCashierInfoRequest().getBody(), null)
                && (PaymodeSequenceEnum.ENHANCE.equals(request.getNativeCashierInfoRequest().getBody()
                        .getPaymodeSequenceEnum()) && (ff4jUtils.isFeatureEnabledOnMid(request
                        .getNativeCashierInfoRequest().getBody().getMid(),
                        TheiaConstant.FF4J.THEIA_ENHANCE_LOGIN_VIA_COOKIE_ENABLED, false)))) {
            serviceRequestData = generateServiceRequestData(nativeCashierInfoRequest, true);

            try {
                if (ObjectUtils.notEqual(serviceRequestData.getBody().getNativePersistData(), null)
                        && StringUtils.isNotBlank(serviceRequestData.getBody().getNativePersistData().getUserDetails()
                                .getUserToken())) {
                    nativeSessionUtil.updateAuthenticatedFlagInCache(nativeCashierInfoRequest.getHead().getTxnToken(),
                            true);
                    nativeSessionUtil.setUserDetails(nativeCashierInfoRequest.getHead().getTxnToken(),
                            serviceRequestData.getBody().getNativePersistData().getUserDetails());
                }
            } catch (Exception ex) {
                LOGGER.error("Exception while updating details in Cache: {}", ex);
            }
        } else {
            serviceRequestData = generateServiceRequestData(nativeCashierInfoRequest,
                    (orderDetail instanceof SubscriptionTransactionRequestBody));
        }
        return serviceRequestData;
    }

    private void disableUPICollectForNonLoggedIn(InitiateTransactionRequestBody orderDetail) {

        boolean disableUPICollect = merchantPreferenceService.isUpiCollectDisabledForNonLoggedIn(orderDetail.getMid());

        if (disableUPICollect && StringUtils.isBlank(orderDetail.getPaytmSsoToken())) {
            PaymentMode upiCollectPaymentMode = new PaymentMode(UPI);
            List<String> upiChannels = new ArrayList<>();
            upiChannels.add(UPI);
            upiCollectPaymentMode.setChannels(upiChannels);
            if (Objects.isNull(orderDetail.getDisablePaymentMode()))
                orderDetail.setDisablePaymentMode(new ArrayList<>());
            orderDetail.getDisablePaymentMode().add(upiCollectPaymentMode);
            LOGGER.info("Disabling upi collect for non logged in users");
        }
    }

    private void disableBankTransferIfRequired(CashierInfoRequest serviceRequest,
            InitiateTransactionRequestBody orderDetail) {
        String bankTransferCheckoutFlow = merchantPreferenceService.getBankTransferCheckoutFlow(orderDetail.getMid());
        if (serviceRequest.getBody().getDisabledInstrumentTypes() == null) {
            serviceRequest.getBody().setDisabledInstrumentTypes(new ArrayList<InstrumentType>());
        }
        if (StringUtils.isBlank(bankTransferCheckoutFlow)
                || BankTransferCheckoutFlow.DISABLED.getValue().equals(bankTransferCheckoutFlow)) {
            LOGGER.info("Bank Transfer Preference is Blank or Disabled");
            serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.BANK_TRANSFER);
        } else if (BankTransferCheckoutFlow.MERCHANT_CONTROLLED.getValue().equals(bankTransferCheckoutFlow)) {
            if (orderDetail.getVanInfo() == null || StringUtils.isBlank(orderDetail.getVanInfo().getIdentificationNo())
                    || StringUtils.isBlank(orderDetail.getVanInfo().getMerchantPrefix())) {
                LOGGER.info("MERCHANT_CONTROLLED VAN Checkout Flow, IdentificationNo or MerchantPrefix is Missing");
                serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.BANK_TRANSFER);
            }
        } else if (BankTransferCheckoutFlow.PAYTM_CONTROLLED.getValue().equals(bankTransferCheckoutFlow)) {
            if (orderDetail.getVanInfo() == null || StringUtils.isBlank(orderDetail.getVanInfo().getMerchantPrefix())) {
                LOGGER.info("PAYTM_CONTROLLED VAN Checkout Flow, MerchantPrefix is Missing");
                serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.BANK_TRANSFER);
            }
        }
    }

    private void enrichPreAuth(CashierInfoRequest serviceRequestData, InitiateTransactionRequestBody orderDetail) {
        if (TxnType.AUTH.equals(orderDetail.getTxnType()) || TxnType.ESCROW.equals(orderDetail.getTxnType())) {
            serviceRequestData.getBody().setPreAuth(true);
            if (TxnType.AUTH.equals(orderDetail.getTxnType())) {
                serviceRequestData.getBody().setProductCode(ProductCodes.StandardAcquiringPreAuthCapture.getId());
            } else if (TxnType.ESCROW.equals(orderDetail.getTxnType())) {
                serviceRequestData.getBody().setProductCode(
                        ProductCodes.StandardAcquiringEscrowDelayedSettlement.getId());
            }
            if (!serviceRequestData.getBody().isUpiPayConfirmSupport() && orderDetail.isAppInvoke()) {
                LOGGER.info("UPI Pay Confirm Not Supported, Request From old APP, Disabling UPI");
                if (serviceRequestData.getBody().getDisabledInstrumentTypes() == null) {
                    serviceRequestData.getBody().setDisabledInstrumentTypes(new ArrayList<InstrumentType>());
                }
                serviceRequestData.getBody().getDisabledInstrumentTypes().add(InstrumentType.UPI);
            }
        }
    }

    private void validateRequestAndSetTokenTypeSSO(NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest,
            TokenType tokenType) {
        NativeCashierInfoRequest request = nativeCashierInfoContainerRequest.getNativeCashierInfoRequest();
        if (TokenType.CHECKSUM == tokenType) {
            // Checksum validated at Interceptor
            AccessTokenBody accessTokenBody = accessTokenUtils.createAccessToken(request);
            request.getBody().setAccessToken(accessTokenBody.getToken());
            changeTokenTypeToSso(request, request.getBody().getPaytmSsoToken());
        } else if (TokenType.ACCESS == tokenType) {
            CreateAccessTokenServiceRequest accessTokenData = nativeValidationService.validateAccessToken(request);
            request.getBody().setAccessToken(request.getHead().getToken());
            request.getBody().setUserInfo(accessTokenData.getUserInfo());
            if (ProductType.UniversalPreAuth.equals(accessTokenData.getProductType())) {
                request.getBody().setProductType(accessTokenData.getProductType());
            }
            String custId = accessTokenData.getCustId();
            if (accessTokenData.getUserInfo() != null
                    && StringUtils.isNotBlank(accessTokenData.getUserInfo().getCustId())) {
                custId = accessTokenData.getUserInfo().getCustId();
            }
            request.getBody().setCustId(custId); // setting custId to show
                                                 // mid+custId cards for non
                                                 // loggedIn flow
            nativeCashierInfoContainerRequest.setNativePersistData(accessTokenData.getNativePersistData());
            if (Objects.isNull(request.getBody().getCardPreAuthType())) {
                request.getBody().setCardPreAuthType(accessTokenData.getPreAuthType());
            }
            if (Objects.isNull(request.getBody().getPreAuthBlockSeconds())) {
                request.getBody().setPreAuthBlockSeconds(accessTokenData.getPreAuthBlockSeconds());
            }
            changeTokenTypeToSsoOrGuest(request, accessTokenData.getPaytmSsoToken());
        }
    }

    private void changeTokenTypeToSso(NativeCashierInfoRequest request, String paytmSsoToken) {
        LOGGER.info("Changing tokenType from {} to {} ", request.getHead().getTokenType().getType(),
                TokenType.SSO.getType());
        request.getHead().setTokenType(TokenType.SSO);
        request.getHead().setToken(paytmSsoToken);
    }

    private void changeTokenTypeToSsoOrGuest(NativeCashierInfoRequest request, String paytmSsoToken) {
        LOGGER.info("Changing tokenType from {} to {} ", request.getHead().getTokenType().getType(),
                TokenType.SSO.getType());

        // for validate otp sso token is linked with access token in validate
        // otp call
        if (StringUtils.isBlank(paytmSsoToken)
                && StringUtils.isNotBlank(nativeSessionUtil.getSsoToken(request.getHead().getToken()))
                && StringUtils.equals(request.getHead().getWorkFlow(), CHECKOUT)) {
            paytmSsoToken = nativeSessionUtil.getSsoToken(request.getHead().getToken());
        }

        if (StringUtils.isBlank(paytmSsoToken)) {
            request.getHead().setTokenType(TokenType.GUEST);
            request.getHead().setTxnToken(nativeSessionUtil.createTokenForGuest(request.getBody().getMid()));
        } else {
            request.getHead().setTokenType(TokenType.SSO);
            request.getHead().setToken(paytmSsoToken);
        }

        if (StringUtils.equals(request.getHead().getWorkFlow(), CHECKOUT)) {
            request.getBody().setBlinkCheckoutAccessSupport(true);
        }

    }

    private static void updatePaymentMethods(List<PaymentMode> payModes) {
        if (CollectionUtils.isNotEmpty(payModes)) {
            PaymentMode upiPaymentMode = null;
            for (PaymentMode paymentMode : payModes) {
                EPayMethod payMethod = PayMethodUtility.getPayMethodByMethod(paymentMode.getMode());
                if (payMethod != null
                        && !(EPayMethod.PPBL.equals(payMethod) || EPayMethod.RENEW_PPBL.equals(payMethod))) {
                    paymentMode.setMode(payMethod.getMethod());
                }
                if (EPayMethod.UPI.equals(payMethod))
                    upiPaymentMode = paymentMode;
            }
            // To bind upi lite with upi
            if (upiPaymentMode != null) {
                PaymentMode upiLitePaymentMode = SerializationUtils.clone(upiPaymentMode);
                upiLitePaymentMode.setMode(EPayMethod.UPI_LITE.getMethod());
                payModes.add(upiLitePaymentMode);
            }
        }
    }

    private CashierInfoContainerRequest preProcessForGuest(NativeCashierInfoContainerRequest request) {
        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        validateRequest(nativeCashierInfoRequest);
        String mid = nativeCashierInfoRequest.getBody().getMid();

        CashierInfoRequest serviceRequestData = generateServiceRequestData(nativeCashierInfoRequest);

        if (StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getGenerateOrderId())
                && Boolean.valueOf(nativeCashierInfoRequest.getBody().getGenerateOrderId())) {
            processTransactionUtil.pushFetchPaymentOptionsEvent(EventNameEnum.OFFLINE_FETCHPAYMENTOPTIONS);
            // generating order id and setting the same in MDC
            OfflinePaymentUtils.updateOrderIdInMDC(getOrderId(nativeCashierInfoRequest.getBody().getMid()));
        }

        serviceRequestData.getBody().setOrderId(MDC.get(TheiaConstant.RequestParams.ORDER_ID));

        mergeTLSDisabledPayModes(nativeCashierInfoRequest);

        String token = nativeCashierInfoRequest.getHead().getToken();
        if (StringUtils.isBlank(token)) {
            token = nativeSessionUtil.createTokenForGuest(mid);
        }
        nativeCashierInfoRequest.getHead().setTxnToken(token);

        serviceRequestData.getHead().setMid(nativeCashierInfoRequest.getBody().getMid());
        serviceRequestData.getBody().setCustId(nativeCashierInfoRequest.getBody().getCustId());
        serviceRequestData.getBody().setUserInfo(nativeCashierInfoRequest.getBody().getUserInfo());
        serviceRequestData.getBody().setCardPreAuthType(nativeCashierInfoRequest.getBody().getCardPreAuthType());

        serviceRequestData.getBody()
                .setPreAuthBlockSeconds(nativeCashierInfoRequest.getBody().getPreAuthBlockSeconds());
        if (ProductType.UniversalPreAuth.equals(nativeCashierInfoRequest.getBody().getProductType())) {
            serviceRequestData.getBody().setProductCode(ProductCodes.StandardAcquiringEscrowDelayedSettlement.getId());
        }

        if (Objects.nonNull(serviceRequestData.getBody().getCardPreAuthType())) {
            serviceRequestData.getBody().setPreAuth(true);
            serviceRequestData.getBody().setProductCode(ProductCodes.StandardAcquiringPreAuthCapture.getId());
        }
        request.setNativeCashierInfoRequest(nativeCashierInfoRequest);
        return new CashierInfoContainerRequest(serviceRequestData, request.getPaymentRequestBean());
    }

    private CashierInfoContainerRequest preProcessWithSSOToken(NativeCashierInfoContainerRequest request) {
        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        validateRequest(nativeCashierInfoRequest);
        String ssoToken = nativeCashierInfoRequest.getHead().getToken();
        String mid = nativeCashierInfoRequest.getBody().getMid();
        UserDetailsBiz userDetails = null;

        if (request.getNativePersistData() != null && request.getNativePersistData().getUserDetails() != null) {
            userDetails = request.getNativePersistData().getUserDetails();
            LOGGER.info("Fetching user details from cache for single api hits");
        } else {
            userDetails = nativeValidationService.validateSSOToken(ssoToken, mid);
        }

        CashierInfoRequest serviceRequestData = generateServiceRequestData(nativeCashierInfoRequest);

        // to handle access token case
        serviceRequestData.getBody().setNativePersistData(new NativePersistData(userDetails));

        if (StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getGenerateOrderId())
                && Boolean.valueOf(nativeCashierInfoRequest.getBody().getGenerateOrderId())) {

            processTransactionUtil.pushFetchPaymentOptionsEvent(EventNameEnum.OFFLINE_FETCHPAYMENTOPTIONS);

            // generating order id and setting the same in MDC
            OfflinePaymentUtils.updateOrderIdInMDC(getOrderId(nativeCashierInfoRequest.getBody().getMid()));

        }
        serviceRequestData.getBody().setOrderId(MDC.get(TheiaConstant.RequestParams.ORDER_ID));

        if (StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getSubwalletAmount())) {
            serviceRequestData.getBody().setSubwalletAmount(nativeCashierInfoRequest.getBody().getSubwalletAmount());
        } else if (StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getGenerateOrderId())
                && nativeCashierInfoRequest.getBody().getGenerateOrderId().equals(FALSE)
                && nativeCashierInfoRequest.getBody().getExtendInfo() != null
                && StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getExtendInfo().get(ADDITIONALINFO))) {

            String additionalInfoString = nativeCashierInfoRequest.getBody().getExtendInfo().get(ADDITIONALINFO);
            Map<String, String> additionalInfoMap = AdditionalInfoUtil
                    .generateMapFromAdditionalInfoString(additionalInfoString);

            String requestType = additionalInfoMap.get(TheiaConstant.LinkBasedParams.REQUEST_TYPE);
            if (StringUtils.isNotBlank(requestType)
                    && (requestType.equals(UPI_QR_CODE) || requestType.equals(UPI_POS_ORDER))) {
                QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = null;
                try {
                    queryByMerchantTransIdResponse = acquiringUtil.queryByMerchantTransId(mid, serviceRequestData
                            .getBody().getOrderId());
                    if (queryByMerchantTransIdResponse != null
                            && queryByMerchantTransIdResponse.getBody() != null
                            && queryByMerchantTransIdResponse.getBody().getResultInfo() != null
                            && "S".equalsIgnoreCase(queryByMerchantTransIdResponse.getBody().getResultInfo()
                                    .getResultStatus())) {
                        Map<String, String> extendInfo = queryByMerchantTransIdResponse.getBody().getExtendInfo();

                        if (extendInfo != null && StringUtils.isNotBlank(extendInfo.get(SUBWALLET_AMOUNT_DETAILS))) {
                            serviceRequestData.getBody().setSubwalletAmount(extendInfo.get(SUBWALLET_AMOUNT_DETAILS));
                        }
                        LOGGER.info("Caching POS ORDER Extend Info");
                        nativeSessionUtil.setPOSOrderExtendInfo(mid, serviceRequestData.getBody().getOrderId(),
                                extendInfo);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred during queryByMerchantTransId ", e);
                }
            }
        }

        mergeTLSDisabledPayModes(nativeCashierInfoRequest);
        if (nativeCashierInfoRequest.getBody().isEmiSubventionRequired()) {
            if (nativeCashierInfoRequest.getBody().getItems() != null) {
                serviceRequestData.getBody().setItems(nativeCashierInfoRequest.getBody().getItems());
            } else {
                LOGGER.info("EMI SUBVENTION items list is empty");
            }
        }

        // set emiSubvention data
        if (nativeCashierInfoRequest.getBody().isEmiSubventionRequired()) {
            if (nativeCashierInfoRequest.getBody().getItems() != null) {
                subventionEmiServiceHelper.setDefaultValuesForItemBasedParams(nativeCashierInfoRequest.getBody()
                        .getItems());
                serviceRequestData.getBody().setItems(nativeCashierInfoRequest.getBody().getItems());
                if (null == serviceRequestData.getBody().getSubventionDetails()) {
                    SubventionDetails subventionDetails = new SubventionDetails();
                    subventionDetails.setStrategy(EMISubventionStrategy.ITEM_BASED);
                    serviceRequestData.getBody().setSubventionDetails(subventionDetails);
                }
            }
            serviceRequestData.getBody().setEmiSubventionCustomerId(
                    nativeCashierInfoRequest.getBody().getEmiSubventionCustomerId());
            serviceRequestData.getBody().setEmiSubventionRequired(
                    nativeCashierInfoRequest.getBody().isEmiSubventionRequired());
            serviceRequestData.getBody().setEmiSubventedTransactionAmount(
                    nativeCashierInfoRequest.getBody().getEmiSubventedTransactionAmount());
        }

        // set txn token: for this flow txn token will be midssoTokenHash
        nativeCashierInfoRequest.getHead().setTxnToken(nativeSessionUtil.createTokenForMidSSOFlow(ssoToken, mid));

        // setting txntoken as accessToken to save cache on access
        if (nativeCashierInfoRequest.getBody().isBlinkCheckoutAccessSupport()) {
            nativeCashierInfoRequest.getHead().setTxnToken(nativeCashierInfoRequest.getBody().getAccessToken());

            // setting userDetails in accessToken to save extra call in cache
            // card api
            CreateAccessTokenServiceRequest accessTokenData = accessTokenUtils
                    .getAccessTokenDetail(nativeCashierInfoRequest.getBody().getAccessToken());
            if (accessTokenData != null) {
                accessTokenData.setNativePersistData(new NativePersistData(userDetails));
                int txnTokenExpiryInSeconds = getTokenExpiryTime();
                nativeSessionUtil.setAccessTokenDetail(nativeCashierInfoRequest.getBody().getAccessToken(),
                        accessTokenData);
            }
        }

        serviceRequestData.getHead().setMid(nativeCashierInfoRequest.getBody().getMid());
        request.setNativeCashierInfoRequest(nativeCashierInfoRequest);
        serviceRequestData.getBody()
                .setPreAuthBlockSeconds(nativeCashierInfoRequest.getBody().getPreAuthBlockSeconds());

        serviceRequestData.getBody().setCardPreAuthType(nativeCashierInfoRequest.getBody().getCardPreAuthType());

        if (ProductType.UniversalPreAuth.equals(nativeCashierInfoRequest.getBody().getProductType())) {
            serviceRequestData.getBody().setProductCode(ProductCodes.StandardAcquiringEscrowDelayedSettlement.getId());
        }

        if (Objects.nonNull(serviceRequestData.getBody().getCardPreAuthType())) {
            serviceRequestData.getBody().setPreAuth(true);
            serviceRequestData.getBody().setProductCode(ProductCodes.StandardAcquiringPreAuthCapture.getId());
        }
        return new CashierInfoContainerRequest(serviceRequestData, request.getPaymentRequestBean());
    }

    /*
     * preProcessing for payment context token
     */
    private CashierInfoContainerRequest preProcessWithPaymentContext(NativeCashierInfoContainerRequest request) {
        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        validatePaymentContextRequest(nativeCashierInfoRequest);
        String mid = nativeCashierInfoRequest.getBody().getMid();
        String referenceId = nativeCashierInfoRequest.getBody().getReferenceId();
        String requestType = nativeCashierInfoRequest.getBody().getRequestType();
        UserDetailsBiz userDetails;

        CashierInfoRequest serviceRequestData = generateServiceRequestData(nativeCashierInfoRequest);

        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestType)) {
            enrichSubscriptionStartDate(nativeCashierInfoRequest.getBody().getSubscriptionTransactionRequestBody());
            enrichSubscriptionSpecific(serviceRequestData, nativeCashierInfoRequest.getBody()
                    .getSubscriptionTransactionRequestBody());
            serviceRequestData.getBody().setSuperGwFpoApiHit(request.isSuperGwApiHit());
            disableCcDcPaymodesIfEligible(nativeCashierInfoRequest);
        }

        if (request.getNativePersistData() != null && request.getNativePersistData().getUserDetails() != null) {
            userDetails = request.getNativePersistData().getUserDetails();
            serviceRequestData.getBody().setNativePersistData(request.getNativePersistData());
            serviceRequestData.getBody().setCustId(userDetails.getUserId());
        }

        serviceRequestData.getBody().setOrderId(MDC.get(TheiaConstant.RequestParams.ORDER_ID));

        mergeTLSDisabledPayModes(nativeCashierInfoRequest);
        if (nativeCashierInfoRequest.getBody().isEmiSubventionRequired()) {
            if (nativeCashierInfoRequest.getBody().getItems() != null) {
                serviceRequestData.getBody().setItems(nativeCashierInfoRequest.getBody().getItems());
            } else {
                LOGGER.info("EMI SUBVENTION items list is empty");
            }
        }

        // set emiSubvention data
        if (nativeCashierInfoRequest.getBody().isEmiSubventionRequired()) {

            serviceRequestData.getBody().setItems(nativeCashierInfoRequest.getBody().getItems());
            serviceRequestData.getBody().setEmiSubventionCustomerId(
                    nativeCashierInfoRequest.getBody().getEmiSubventionCustomerId());
            serviceRequestData.getBody().setEmiSubventionRequired(
                    nativeCashierInfoRequest.getBody().isEmiSubventionRequired());
            serviceRequestData.getBody().setEmiSubventedTransactionAmount(
                    nativeCashierInfoRequest.getBody().getEmiSubventedTransactionAmount());
        }

        // set txn token: for this flow txn token will be midssoTokenHash
        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestType)) {
            nativeCashierInfoRequest.getHead().setTxnToken(
                    nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId,
                            ERequestType.NATIVE_SUBSCRIPTION.getType()));
        } else {
            nativeCashierInfoRequest.getHead().setTxnToken(
                    nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId, ERequestType.NATIVE.getType()));
        }
        if (StringUtils.isNotBlank(nativeCashierInfoRequest.getBody().getCustId())) {
            serviceRequestData.getBody().setCustId(nativeCashierInfoRequest.getBody().getCustId());
        }
        serviceRequestData.getBody().setReferenceId(nativeCashierInfoRequest.getBody().getReferenceId());
        serviceRequestData.getHead().setMid(nativeCashierInfoRequest.getBody().getMid());
        request.setNativeCashierInfoRequest(nativeCashierInfoRequest);
        return new CashierInfoContainerRequest(serviceRequestData, request.getPaymentRequestBean());
    }

    private void enrichSubscriptionStartDate(SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        if (StringUtils.isBlank(subscriptionTransactionRequestBody.getSubscriptionStartDate())) {
            String subscriptionStartDate = nativeSubscriptionHelper.calculateSubsStartDate(
                    subscriptionTransactionRequestBody.getSubscriptionStartDate(),
                    subscriptionTransactionRequestBody.getSubscriptionFrequency(),
                    subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit());
            subscriptionTransactionRequestBody.setSubscriptionStartDate(subscriptionStartDate);
        }
    }

    private void disableCcDcPaymodesIfEligible(NativeCashierInfoRequest nativeCashierInfoRequest) {
        SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = nativeCashierInfoRequest.getBody()
                .getSubscriptionTransactionRequestBody();
        PaymentMode debitCardMode = new PaymentMode(EPayMethod.DEBIT_CARD.getMethod());
        PaymentMode creditCardMode = new PaymentMode(EPayMethod.CREDIT_CARD.getMethod());
        List<PaymentMode> disablePayModesList;
        if (!ff4jUtils.isFeatureEnabledOnMid(nativeCashierInfoRequest.getBody().getMid(),
                THEIA_ENABLE_CC_DC_PAYMODES_SUBSCRIPTION, false)
                || disableCcDcForSubsCriteria(subscriptionTransactionRequestBody)) {
            if (subscriptionTransactionRequestBody.getDisablePaymentMode() != null
                    && !subscriptionTransactionRequestBody.getDisablePaymentMode().isEmpty()) {
                disablePayModesList = subscriptionTransactionRequestBody.getDisablePaymentMode();
            } else {
                disablePayModesList = new ArrayList<>();
            }
            disablePayModesList.add(debitCardMode);
            disablePayModesList.add(creditCardMode);
            subscriptionTransactionRequestBody.setDisablePaymentMode(disablePayModesList);
            nativeCashierInfoRequest.getBody().setDisablePaymentMode(disablePayModesList);
        }
    }

    private boolean disableCcDcForSubsCriteria(SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        return susbcriptionNativeValidationService.isDailySubscription(subscriptionTransactionRequestBody)
                || nativeSubscriptionHelper.invalidGraceDaysForCard(subscriptionTransactionRequestBody
                        .getSubscriptionGraceDays());
    }

    private void validateRequest(NativeCashierInfoRequest request) {

        if (TokenType.GUEST == request.getHead().getTokenType()) {

            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();

            if (StringUtils.equals(servletRequest.getRequestURI(), NATIV_FETCH_PAYMENTOPTIONS_URL)
                    && !request.getBody().isBlinkCheckoutAccessSupport()) {
                LOGGER.info("URI is {}, tokenType=GUEST is not supported in this", NATIV_FETCH_PAYMENTOPTIONS_URL);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(false).build();
            }
        } else {

            if (StringUtils.isBlank(request.getBody().getMid()) || StringUtils.isBlank(request.getHead().getToken())) {
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
        nativeValidationService.validateMid(request.getBody().getMid());
    }

    private void validatePaymentContextRequest(NativeCashierInfoRequest request) {

        nativeValidationService.validateMid(request.getBody().getMid());
    }

    private void mergeTLSDisabledPayModes(NativeCashierInfoRequest request) {
        if (null == request.getBody()) {
            request.setBody(new NativeCashierInfoRequestBody());
        }
        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        String disablePayModesTLS = ((String) httpServletRequest
                .getAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED));
        List<PaymentMode> disablePayModesList = new ArrayList<PaymentMode>();

        if (StringUtils.isNotEmpty(disablePayModesTLS)) {
            String[] disablePayModes = disablePayModesTLS.split("\\s*,\\s*");
            if (disablePayModes.length > 0) {
                LOGGER.info("Adding disable paymode request for Native TLS Header");
                for (String paymode : disablePayModes) {
                    PaymentMode mode = new PaymentMode(EPayMethod.getPayMethodByOldName(paymode).getMethod());
                    disablePayModesList.add(mode);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(request.getBody().getDisablePaymentMode())) {
            request.getBody().getDisablePaymentMode().addAll(disablePayModesList);
        } else {
            request.getBody().setDisablePaymentMode(disablePayModesList);
        }
    }

    private void updatePayModesIfRequired(NativeCashierInfoRequest request, InitiateTransactionRequestBody orderDetail,
            String txnToken) {
        if (null == request.getBody()) {
            return;
        }

        if (CollectionUtils.isNotEmpty(request.getBody().getEnablePaymentMode()))
            orderDetail.setEnablePaymentMode(request.getBody().getEnablePaymentMode());
        if (CollectionUtils.isNotEmpty(request.getBody().getDisablePaymentMode()))
            orderDetail.setDisablePaymentMode(request.getBody().getDisablePaymentMode());
        if (CollectionUtils.isNotEmpty(request.getBody().getEnablePaymentMode())
                || CollectionUtils.isNotEmpty(request.getBody().getDisablePaymentMode()))
            nativeSessionUtil.setOrderDetail(txnToken, orderDetail);
    }

    private void enrich(NativeCashierInfoRequest request, InitiateTransactionRequestBody orderDetail) {
        if (null == request.getBody()) {
            request.setBody(new NativeCashierInfoRequestBody());
        }
        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        String disablePayModesTLS = ((String) httpServletRequest
                .getAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED));
        List<PaymentMode> disablePayModesList = new ArrayList<PaymentMode>();

        if (StringUtils.isNotEmpty(disablePayModesTLS)) {
            String[] disablePayModes = disablePayModesTLS.split("\\s*,\\s*");
            if (disablePayModes.length > 0) {
                LOGGER.info("Adding disable paymode request for Native TLS Header");
                for (String paymode : disablePayModes) {
                    PaymentMode mode = new PaymentMode(EPayMethod.getPayMethodByOldName(paymode).getMethod());
                    disablePayModesList.add(mode);
                }
            }
        }
        if (disablePayModesList.size() > 0 && orderDetail.getDisablePaymentMode() != null
                && orderDetail.getDisablePaymentMode().size() > 0) {
            disablePayModesList.addAll(orderDetail.getDisablePaymentMode());
            request.getBody().setDisablePaymentMode(disablePayModesList);
        } else if (disablePayModesList.size() > 0) {
            request.getBody().setDisablePaymentMode(disablePayModesList);
        } else {
            request.getBody().setDisablePaymentMode(orderDetail.getDisablePaymentMode());
        }
        request.getBody().setEnablePaymentMode(orderDetail.getEnablePaymentMode());
        // setting custId for txnTokenFlow
        request.getBody().setCustId(orderDetail.getUserInfo().getCustId());
        if (StringUtils.isEmpty(request.getBody().getMid())) {
            request.getBody().setMid(orderDetail.getMid());
        }
        // Setting Paymode Sequencing coming in initiate request.
        request.getBody().setPaymodeSequence(orderDetail.getPaymodeSequence());
    }

    @Override
    protected WorkFlowResponseBean onProcess(NativeCashierInfoContainerRequest request,
            CashierInfoContainerRequest serviceRequest) {
        CashierInfoRequest serviceRequestData = serviceRequest.getCashierInfoRequest();
        IPayviewConsultService payviewConsultService = getPayviewConsultService(serviceRequestData);
        WorkFlowResponseBean response;

        try {
            response = payviewConsultService.process(serviceRequest);
        } catch (PaymentRequestValidationException paymentRequestValidationException) {
            String responseConstant = paymentRequestValidationException.getResponseConstants().name();
            ResultCode resultCode = ResultCode.valueOf(responseConstant);
            throw PaymentRequestProcessingException.getException(resultCode);
        }
        return response;
    }

    private List<PaymentOffersData> allPaymentOffersOnMerchant(String mid, String simplifiedPromoCode,
            String paytmUserId, ApplyItemOffers applyItemOffers) {
        try {

            if (!ff4JUtil.isMigrateBankOffersPromo(mid)) {
                SearchPaymentOffersServiceResponse searchPaymentOffersServiceResponse = paymentOffersServiceHelper
                        .searchPaymentOffers(mid, paytmUserId);
                return paymentOffersServiceHelper.preparePaymentOffers(searchPaymentOffersServiceResponse,
                        simplifiedPromoCode);
            } else {
                SearchPaymentOffersServiceResponseV2 searchPaymentOffersServiceResponse = paymentOffersServiceHelperV2
                        .searchPaymentOffers(mid, paytmUserId,
                                applyItemOffers == null ? null : applyItemOffers.getPromoContext());
                return paymentOffersServiceHelperV2.preparePaymentOffers(searchPaymentOffersServiceResponse,
                        simplifiedPromoCode);
            }
        } catch (BaseException e) {
            LOGGER.error("Exception in searchPaymentOffers for mid {}", mid);
        }
        return null;
    }

    private SearchPaymentOffersServiceResponseV2 allPaymentOffersOnMerchantV2(String mid,
            FetchAllItemOffer fetchAllItemOffers, SimplifiedPaymentOffers simplifiedPaymentOffers, String paytmUserId,
            ApplyItemOffers applyItemOffers) {
        SearchPaymentOffersServiceResponseV2 searchPaymentOffersServiceResponse = paymentOffersServiceHelperV2
                .searchPaymentOffers(mid, fetchAllItemOffers, paytmUserId, applyItemOffers);
        String simplifiedPromoCode = (simplifiedPaymentOffers != null) ? simplifiedPaymentOffers.getPromoCode() : null;
        return paymentOffersServiceHelperV2.preparePaymentOffersV2(searchPaymentOffersServiceResponse,
                simplifiedPromoCode);
    }

    private void populateAllPaymentOffersOnMerchant(WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse response,
            CashierInfoRequest cashierInfoRequest) {
        String paytmUserId = null;
        if (workFlowResponse.getUserDetails() != null) {
            paytmUserId = workFlowResponse.getUserDetails().getUserId();
        }
        if (Boolean.parseBoolean(nativeCashierInfoRequest.getBody().getFetchAllPaymentOffers())) {
            if (cashierInfoRequest.getBody() != null
                    && cashierInfoRequest.getBody().getSimplifiedPaymentOffers() != null) {
                response.getBody().setPaymentOffers(
                        allPaymentOffersOnMerchant(nativeCashierInfoRequest.getBody().getMid(), cashierInfoRequest
                                .getBody().getSimplifiedPaymentOffers().getPromoCode(), paytmUserId,
                                nativeCashierInfoRequest.getBody().getApplyItemOffers()));
            } else {
                response.getBody().setPaymentOffers(
                        allPaymentOffersOnMerchant(nativeCashierInfoRequest.getBody().getMid(), null, paytmUserId,
                                nativeCashierInfoRequest.getBody().getApplyItemOffers()));
            }
        } else if (nativeCashierInfoRequest.getBody().getFetchAllItemOffers() != null) {
            response.getBody().setPaymentOffersV2(
                    allPaymentOffersOnMerchantV2(nativeCashierInfoRequest.getBody().getMid(), nativeCashierInfoRequest
                            .getBody().getFetchAllItemOffers(), cashierInfoRequest.getBody()
                            .getSimplifiedPaymentOffers(), paytmUserId, nativeCashierInfoRequest.getBody()
                            .getApplyItemOffers()));
        }
    }

    private void populatePromoData(WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse response,
            CashierInfoRequest cashierInfoRequest) {

        populateAllPaymentOffersOnMerchant(workFlowResponse, nativeCashierInfoRequest, response, cashierInfoRequest);
        populatePaymentOfferOnSavedInstruments(nativeCashierInfoRequest, response, workFlowResponse, cashierInfoRequest);
    }

    private void populatePaymentOfferOnSavedInstruments(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse response, WorkFlowResponseBean workFlowResponse,
            CashierInfoRequest cashierInfoRequest) {

        String paytmUserId = null;
        if (workFlowResponse.getUserDetails() != null) {
            paytmUserId = workFlowResponse.getUserDetails().getUserId();
        }
        if (cashierInfoRequest.getBody() != null && cashierInfoRequest.getBody().getSimplifiedPaymentOffers() != null
                && StringUtils.isBlank(cashierInfoRequest.getBody().getSimplifiedPaymentOffers().getPromoCode())) {
            if (StringUtils.isNotBlank(cashierInfoRequest.getBody().getSimplifiedPaymentOffers().getPromoAmount())
                    && EPayMode.HYBRID != response.getBody().getPaymentFlow()
                    && EPayMode.ADDANDPAY != response.getBody().getPaymentFlow()) {
                nativeCashierInfoRequest.getBody().setOrderAmount(
                        cashierInfoRequest.getBody().getSimplifiedPaymentOffers().getPromoAmount());
            } else {
                nativeCashierInfoRequest.getBody().setOrderAmount(
                        cashierInfoRequest.getBody().getOrderAmount().getValue());
            }
        }

        if ((Boolean.parseBoolean(nativeCashierInfoRequest.getBody().getApplyPaymentOffer()) || (nativeCashierInfoRequest
                .getBody().getApplyItemOffers() != null))
                && NumberUtils.isNumber(nativeCashierInfoRequest.getBody().getOrderAmount())
                && BooleanUtils.isNotTrue(response.getBody().getPwpEnabled())
                && !merchantPreferenceService.isBlockBulkApplyPromo(nativeCashierInfoRequest.getBody().getMid())) {
            List<PaymentOffersData> paymentOffersData = null;
            if (!Boolean.parseBoolean(nativeCashierInfoRequest.getBody().getFetchAllPaymentOffers())) {
                paymentOffersData = allPaymentOffersOnMerchant(nativeCashierInfoRequest.getBody().getMid(), null,
                        paytmUserId, nativeCashierInfoRequest.getBody().getApplyItemOffers());
            } else {
                paymentOffersData = response.getBody().getPaymentOffers();
            }
            if (CollectionUtils.isNotEmpty(paymentOffersData)) {
                bulkApplyPaymentOffers(nativeCashierInfoRequest, response, workFlowResponse);
            }
        }
    }

    private void bulkApplyPaymentOffers(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse response, WorkFlowResponseBean workFlowResponse) {

        if (!ff4JUtil.isMigrateBankOffersPromo(nativeCashierInfoRequest.getBody().getMid())) {

            nativePaymentBulkOffersService.processBulkPaymentOffers(nativeCashierInfoRequest, response,
                    workFlowResponse);
        } else {
            nativePaymentBulkOffersServiceV2.processBulkPaymentOffers(nativeCashierInfoRequest, response,
                    workFlowResponse);
        }
    }

    @Override
    protected NativeCashierInfoResponse postProcess(NativeCashierInfoContainerRequest request,
            CashierInfoContainerRequest serviceRequest, WorkFlowResponseBean workFlowResponse) throws Exception {
        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        makePaymentRequestBeanBackwardCompatible(request.getPaymentRequestBean());
        CashierInfoRequest cashierInfoRequest = serviceRequest.getCashierInfoRequest();
        disablePromoForPWPMerchant(workFlowResponse, cashierInfoRequest);
        chnageAOAWalletToBalanceForApp(request, workFlowResponse);
        NativeCashierInfoResponse response = payviewConsultServiceHelper.transformResponse(workFlowResponse,
                cashierInfoRequest, nativeCashierInfoRequest);
        response.getBody()
                .setChannelCoftPayment(workFlowResponse.getMerchnatLiteViewResponse().getChannelCoftPayment());

        payviewConsultServiceHelper.setMerchantLimit(workFlowResponse, response);

        // TODO remove logger post QA
        EXT_LOGGER.info("NativeCashierInfoContainerRequest {}", request.toString());
        EXT_LOGGER.info("nativeCashierInfoRequest.getHead().getWorkFlow() {}", nativeCashierInfoRequest.getHead()
                .getWorkFlow());

        String token = null;
        if (null != response) {
            response.getBody().setAccessToken(workFlowResponse.getAccessToken());
            response.getBody().setChannelDetails(workFlowResponse.getChannelDetails());
            response.getBody().setAddMoneyDestination(workFlowResponse.getAddMoneyDestination());
            response.getBody().setEmiSubventionBanks(workFlowResponse.getEmiSubventionPlans());
            response.getBody().setPcfEnabled(workFlowResponse.isPcfEnabled());
            response.getBody().setAddMoneyPcfEnabled(workFlowResponse.isAddMoneyPcfEnabled());
            if (StringUtils.isEmpty(response.getBody().getProductCode())) {
                response.getBody().setProductCode(workFlowResponse.getProductCode());
            }
            response.getBody().setNativeAddMoney(workFlowResponse.getWorkFlowRequestBean().isNativeAddMoney());
            response.getBody().setOneClickMaxAmount(ConfigurationUtil.getProperty(ONE_CLICK_MAX_AMOUNT, "2000"));
            response.getBody().setLocationPermission(
                    merchantPreferenceService.isLocationPermission(cashierInfoRequest.getHead().getMid()));
            if (StringUtils.equals(CHECKOUT, nativeCashierInfoRequest.getHead().getWorkFlow())) {
                /* changes for PGP-26686 */
                response.getBody().setLocalStorageAllowedForLastPayMode(
                        (ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.THEIA_LOCAL_STORAGE_ALLOWED_ON_MERCHANT,
                                cashierInfoRequest.getHead().getMid())));
                /* changes for PGP-28785 */
                response.getBody().setHtmlToBeRenderedForBlinkCheckout(
                        (ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.THEIA_CHECKOUT_FORWARD_HTML_RENDER_ALLOWED,
                                cashierInfoRequest.getHead().getMid())));
                /*
                 * set workflow in cache for use in app-invoke-flow from
                 * js-checkout
                 */
                nativeSessionUtil.setWorkflow(nativeCashierInfoRequest.getHead().getTxnToken(), CHECKOUT);
                addUserInfoInResponse(cashierInfoRequest, response.getBody());
            }
            setWalletAccountStatus(response.getBody(), workFlowResponse, request.getNativeCashierInfoRequest());

            if (workFlowResponse.isPrepaidEnabledOnAnyInstrument()) {
                response.getBody().setPrepaidCardMaxAmount(
                        ConfigurationUtil.getProperty(PREPAID_CARD_MAX_AMOUNT, "100000"));
            }
            response.getBody().setActiveMerchant(
                    !merchantExtendInfoUtils.isMerchantActiveOrBlocked(cashierInfoRequest.getHead().getMid()));
            response.getBody().setConsultFeeResponse(workFlowResponse.getConsultFeeResponse());
            merchantDataUtil.mapMerchantDataForMPINGenerationInNative(cashierInfoRequest, response, workFlowResponse);
            setMerchantDetailsInResponse(request, response, cashierInfoRequest);
            setUserDetailsInResponse(nativeCashierInfoRequest, workFlowResponse.getUserDetails(), response,
                    cashierInfoRequest);
            setLinkDetailsInFPO(nativeCashierInfoRequest, cashierInfoRequest, response, request.getPaymentRequestBean());
            populateEdcEmiLinkPaymentData(nativeCashierInfoRequest, cashierInfoRequest, response);
            disableAppInvokeForCTA(nativeCashierInfoRequest, cashierInfoRequest, response,
                    request.getPaymentRequestBean());
            if (TokenType.SSO == nativeCashierInfoRequest.getHead().getTokenType() || request.isSuperGwApiHit()) {

                nativeSessionUtil.setCashierInfoResponseNoInitiateFlow(
                        nativeCashierInfoRequest.getHead().getTxnToken(), response);

                if (cashierInfoRequest.getBody().getOrderId() != null) {
                    token = request.getNativeCashierInfoRequest().getBody().getMid()
                            + cashierInfoRequest.getBody().getOrderId();
                    nativeSessionUtil.setCashierInfoResponseNoInitiateFlow(token, response);
                }
                // TODO Hack for AOA
                if (aoaUtils.isAOAMerchant(request.getNativeCashierInfoRequest().getBody().getMid())) {
                    String pgMidForAoaMid = aoaUtils.getPgMidForAoaMid(request.getNativeCashierInfoRequest().getBody()
                            .getMid());
                    String txnToken = pgMidForAoaMid + cashierInfoRequest.getBody().getOrderId();
                    nativeSessionUtil.setCashierInfoResponseNoInitiateFlow(txnToken, response);
                }
            }

            if (TokenType.GUEST == nativeCashierInfoRequest.getHead().getTokenType()) {
                response.getBody().setOrderId(cashierInfoRequest.getBody().getOrderId());
                nativeSessionUtil.setCashierInfoResponseForGuest(nativeCashierInfoRequest, response);
                nativeSessionUtil.setField(nativeCashierInfoRequest.getHead().getTxnToken(),
                        TheiaConstant.RequestParams.Native.MID, cashierInfoRequest.getHead().getMid());
                setGuestTokenInResponse(nativeCashierInfoRequest, response);
                setSsoTokenInCaseForDeferred(cashierInfoRequest, nativeCashierInfoRequest, nativeCashierInfoRequest
                        .getHead().getTxnToken());
                if (cashierInfoRequest.getBody() != null && cashierInfoRequest.getBody().getNativePersistData() != null
                        && cashierInfoRequest.getBody().getNativePersistData().getUserDetails() != null) {
                    LOGGER.info("Setting SSO token in cache for Guest Flow");
                    nativeSessionUtil.setSsoToken(nativeCashierInfoRequest.getHead().getTxnToken(), cashierInfoRequest
                            .getBody().getNativePersistData().getUserDetails().getUserToken());
                }

            } else {
                nativeSessionUtil.setCashierInfoResponse(nativeCashierInfoRequest.getHead().getTxnToken(), response);
                // Setting Subscription Details required in fetchPaymentOptions
                // API.
                setSubscriptionDetails(nativeCashierInfoRequest, response, request, workFlowResponse);

                // setting sso Token for logged in AccessToken Flow
                if (nativeCashierInfoRequest.getBody().isBlinkCheckoutAccessSupport()) {
                    setSsoTokenInCaseForDeferred(cashierInfoRequest, nativeCashierInfoRequest, nativeCashierInfoRequest
                            .getBody().getAccessToken());
                }
            }

            if (workFlowResponse.getUserDetails() != null) {
                EXT_LOGGER.customInfo("Setting User Details in Cache");
                nativeSessionUtil.setUserDetails(nativeCashierInfoRequest.getHead().getTxnToken(),
                        workFlowResponse.getUserDetails());
            }

            /*
             * set payViewCache in redis(it can be used to save objects from
             * payview consult that is needed in payment call)
             */
            Map<String, Object> payViewConsultCacheInfo = payviewConsultServiceHelper
                    .getPayViewConsultCacheInfo(workFlowResponse);
            if (!payViewConsultCacheInfo.isEmpty()) {
                nativeSessionUtil.setLitepayviewCache(nativeCashierInfoRequest.getHead().getTxnToken(),
                        payViewConsultCacheInfo);

            }
            // saving requests extend to be used at time of fetching Postpaid
            // balance
            nativeSessionUtil.setExtendInfo(nativeCashierInfoRequest.getHead().getTxnToken(),
                    workFlowResponse.getExtendedInfo());
            EntityPaymentOptionsTO entityPaymentOption = payviewConsultServiceHelper.getEntityPaymentOption(
                    workFlowResponse, nativeCashierInfoRequest.getHead().getTxnToken());
            entityPaymentOption.setCodEnabled(checkCODPayMethodEnabled(response));

            nativeSessionUtil.setEntityPaymentOptions(nativeCashierInfoRequest.getHead().getTxnToken(),
                    entityPaymentOption);

            // setting pwp enabled flag
            if (StringUtils.equals(Boolean.TRUE.toString(), workFlowResponse.getPwpEnabled())) {
                response.getBody().setPwpEnabled(Boolean.TRUE);
            }

            // Setting promo data after all cache has been populated
            // TODO:filter paymodes if we get promocode in request, pending from
            // TODO: promo service team [additional search api params needed]

            if (cashierInfoRequest.getBody() != null
                    && cashierInfoRequest.getBody().getSimplifiedPaymentOffers() != null) {
                response.getBody()
                        .setSimplifiedPaymentOffers(cashierInfoRequest.getBody().getSimplifiedPaymentOffers());

                if (cashierInfoRequest.getBody().getSimplifiedPaymentOffers().getCartDetails() != null) {
                    ApplyItemOffers itemOffers = new ApplyItemOffers();
                    itemOffers.setCartDetails(cashierInfoRequest.getBody().getSimplifiedPaymentOffers()
                            .getCartDetails());
                    nativeCashierInfoRequest.getBody().setApplyItemOffers(itemOffers);
                }
            }
            populatePromoData(workFlowResponse, nativeCashierInfoRequest, response, cashierInfoRequest);

            populateSuperCashData(workFlowResponse, nativeCashierInfoRequest, response);

            // populateDealsData(workFlowResponse,nativeCashierInfoRequest,response);

            payviewConsultServiceHelper.trimConsultFeeResponse(response);

            payviewConsultServiceHelper.trimCcDcPayChannels(response);
            payviewConsultServiceHelper.trimEmiChannelInfo(response);
            payviewConsultServiceHelper.trimAdditionalInfoForSavedAssets(response);

            // Check for Zest
            if (isZestEligible(cashierInfoRequest) && !workFlowResponse.isZestOnPG2()
                    && !workFlowResponse.isZestOnAddMoneyPG2()) {
                payviewConsultServiceHelper.populateZestData(response);
            }
            payviewConsultServiceHelper.filterZestFromNBInNative(response);

            // This Net Banking Trimming should always be after populating Zest
            // Data
            if (nativeCashierInfoRequest.getBody() == null
                    || !Boolean.valueOf(nativeCashierInfoRequest.getBody().getFetchAllNB()))
                payviewConsultServiceHelper.trimByTopNBChannels(response);

            /*
             * Commented out to prevent changes in UPI channels - Now there will
             * be 1 to 1 mapping of UPI channels in LPV and FPO
             * payviewConsultServiceHelper
             * .filterPayChannelinUPINative(response);
             */
            payviewConsultServiceHelper.setMerchantOfferMessage(nativeCashierInfoRequest, cashierInfoRequest, response);
            // filter addAndPay option if subs_pay mode is CC,DC, PPBL
            if (SubsPaymentMode.nonAddNPaySubsPayModes.contains(cashierInfoRequest.getBody()
                    .getSubscriptionPaymentMode())) {
                response.getBody().setAddMoneyMerchantDetails(null);
                response.getBody().setAddMoneyPayOption(null);
            }

            /*
             * Sets flag to override check for nativeSavedVpas
             */

            /*
             * This sets flag, which app uses to call v1/ptc in json or formPost
             */
            setNativeJsonRequestSupported(request.getNativeCashierInfoRequest(), response, cashierInfoRequest);
            setLoginInfo(cashierInfoRequest, response);

            setOtpAuthorisedFlag(nativeCashierInfoRequest, response);
            setOrderAmountInFpo(response.getBody(), cashierInfoRequest);

            /*
             * Setting data related to mid sso flow where generateOrderId=true
             * Setting entire session data on mid+orderId as well so that it can
             * be used in PTC
             */
            if (TokenType.SSO == nativeCashierInfoRequest.getHead().getTokenType()) {
                if (cashierInfoRequest.getBody().getOrderId() != null) {
                    response.getBody().setOrderId(cashierInfoRequest.getBody().getOrderId());
                    nativeSessionUtil.setExtendInfo(token, workFlowResponse.getExtendedInfo());
                    nativeSessionUtil.setEntityPaymentOptions(token, entityPaymentOption);
                    nativeSessionUtil.setUserDetails(token, workFlowResponse.getUserDetails());
                    nativeSessionUtil.setSsoToken(token, nativeCashierInfoRequest.getHead().getToken());
                }
                // TODO Hack for AOA
                if (aoaUtils.isAOAMerchant(request.getNativeCashierInfoRequest().getBody().getMid())) {
                    String pgMidForAoaMid = aoaUtils.getPgMidForAoaMid(request.getNativeCashierInfoRequest().getBody()
                            .getMid());
                    String txnToken = pgMidForAoaMid + cashierInfoRequest.getBody().getOrderId();
                    response.getBody().setOrderId(cashierInfoRequest.getBody().getOrderId());
                    nativeSessionUtil.setExtendInfo(txnToken, workFlowResponse.getExtendedInfo());
                    nativeSessionUtil.setEntityPaymentOptions(txnToken, entityPaymentOption);
                    nativeSessionUtil.setUserDetails(txnToken, workFlowResponse.getUserDetails());
                    nativeSessionUtil.setSsoToken(txnToken, nativeCashierInfoRequest.getHead().getToken());
                }
            }
            if (!workFlowResponse.getWorkFlowRequestBean().isEnhancedCashierPageRequest()
                    || StringUtils.equals(CHECKOUT, nativeCashierInfoRequest.getHead().getWorkFlow())) {
                String merchantPayModeOrdering = nativeCashierInfoRequest.getBody().getPaymodeSequence();
                if (StringUtils.isBlank(merchantPayModeOrdering)) {
                    merchantPayModeOrdering = merchantPreferenceService.getMerchantPaymodeSequence(cashierInfoRequest
                            .getHead().getMid(), nativeCashierInfoRequest.getBody().getPaymodeSequenceEnum());
                }
                // ordering final response for all flow
                PayModeOrderUtil.payModeOrdering(response, merchantPayModeOrdering, nativeCashierInfoRequest.getBody()
                        .isOfflineFlow());

            }
            setDescMandatoryParams(response, cashierInfoRequest.getHead().getMid());
            response.getBody().setDescriptionTextFormat(
                    merchantPreferenceService.getDescTextFormat(cashierInfoRequest.getHead().getMid()));

            // seting simplifiedPaymentOffers

            HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
            if (httpServletRequest != null
                    && CHECKOUT.equals(httpServletRequest.getAttribute(TheiaConstant.ExtraConstants.WORKFLOW))
                    && !TokenType.GUEST.equals(nativeCashierInfoRequest.getHead().getTokenType())) {
                token = nativeCashierInfoRequest.getHead().getTxnToken();

                if (httpServletRequest.getAttribute(DYNAMIC_QR_REQUIRED) != null
                        && (boolean) httpServletRequest.getAttribute(DYNAMIC_QR_REQUIRED)) {
                    LOGGER.info("Setting QR details in FPO response");

                    response.getBody().setQrDetail(getQrDetail(cashierInfoRequest.getHead().getMid(), response));
                    response.getBody().setQrCodeDetailsResponse(null);

                    if (Objects.nonNull(response.getBody().getQrDetail())) {
                        nativeSessionUtil.setQrDetail(token, response.getBody().getQrDetail());
                        nativeSessionUtil.setWorkflow(token, CHECKOUT);
                    }
                } else {
                    QrDetail QrDetail = nativeSessionUtil.getQrDetail(token);
                    response.getBody().setQrDetail(QrDetail);
                }
            }

            // setting merchant retry count for JS CHECKOUT
            if (CHECKOUT.equals(nativeCashierInfoRequest.getHead().getWorkFlow())) {
                response.getBody().setAllowedRetryCountsForMerchant(
                        nativeRetryUtil.getAllowedRetryCountsOnMerchant(nativeCashierInfoRequest.getBody().getMid()));
            }
            setPostpaidEnabledOnMerchantAndDisabledOnUser(nativeCashierInfoRequest.getBody().getMid(),
                    workFlowResponse, response);
            response.getBody().setUltimateBeneficiaryDetails(
                    cashierInfoRequest.getBody().getUltimateBeneficiaryDetails());
            setPostpaidThresholdAmountAnd2FA(workFlowResponse, nativeCashierInfoRequest, response);

            response.getBody().setBffLayerEnabled(
                    ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.BFF_LAYER_ENABLED, cashierInfoRequest.getHead()
                            .getMid()));
        }

        if (StringUtils.equals(CHECKOUT, nativeCashierInfoRequest.getHead().getWorkFlow())
                || workFlowResponse.getWorkFlowRequestBean().isEnhancedCashierPageRequest()) {
            List<PayMethod> paymodes = response.getBody().getMerchantPayOption() != null ? response.getBody()
                    .getMerchantPayOption().getPayMethods() : null;
            // set upiAppsPayModeEnabled true in response if merchant preference
            // UPI_APPS_PAY_MODE_DISABLED is false
            if (paymodes != null
                    && isUpiIntentSupported(paymodes)
                    && !merchantPreferenceService
                            .isUpiAppsPayModeDisabled(cashierInfoRequest.getHead().getMid(), false)) {
                LOGGER.info("Setting isUpiAppsPayModeDisabled in FPO response for Blink Checkout and enhanced flow");

                response.getBody().setUpiAppsPayModeEnabled(true);
            } else {
                response.getBody().setUpiAppsPayModeEnabled(false);
            }
            List<PayMethod> addMoneyPaymodes = response.getBody().getAddMoneyPayOption() != null ? response.getBody()
                    .getAddMoneyPayOption().getPayMethods() : null;
            if (addMoneyPaymodes != null
                    && isUpiIntentSupported(addMoneyPaymodes)
                    && workFlowResponse.getWorkFlowRequestBean().isEnhancedCashierPageRequest()
                    && ff4JUtil
                            .isFeatureEnabled(THEIA_UPI_APPS_PAYMODE_DISABLED, cashierInfoRequest.getHead().getMid())
                    && !merchantPreferenceService
                            .isUpiAppsPayModeDisabled(cashierInfoRequest.getHead().getMid(), false)) {
                response.getBody().setUpiAppsPayModeEnabled(true);
            }
        }
        // EMI Subvention All in one SDK Flow
        if (null != cashierInfoRequest.getBody().getSubventionDetails()) {
            PayMethod payMethod = response.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(s -> EPayMethod.EMI.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
            if (payMethod != null) {
                response.getBody().setSubventionDetails(cashierInfoRequest.getBody().getSubventionDetails());
            }
        }
        request.setNativeCashierInfoRequest(nativeCashierInfoRequest);
        serviceRequest.setCashierInfoRequest(cashierInfoRequest);

        setDisableLoginStrip(response, cashierInfoRequest.getHead().getMid(), cashierInfoRequest.getBody()
                .getChannelId().getValue());
        setPostpaidOnlyPrefs(cashierInfoRequest, response);
        setIsMerchantUPIVerified(response, cashierInfoRequest.getHead().getMid());
        response.getBody().setWalletLimits(workFlowResponse.getWalletLimits());

        populateSubscriptionDeepLink(nativeCashierInfoRequest, cashierInfoRequest, response);
        if (null == response.getBody().getUltimateBeneficiaryDetails()) {
            response.getBody().setUltimateBeneficiaryDetails(
                    nativeCashierInfoRequest.getBody().getUltimateBeneficiaryDetails());
        }
        LOGGER.info("Native response returned for fetchPaymentOptions is: {}", response);
        return response;
    }

    private void chnageAOAWalletToBalanceForApp(NativeCashierInfoContainerRequest request,
            WorkFlowResponseBean workFlowResponse) {
        if (TokenType.SSO.equals(request.getNativeCashierInfoRequest().getHead().getTokenType())
                && aoaUtils.isAOAMerchant(request.getNativeCashierInfoRequest().getBody().getMid())) {
            List<PayMethodViewsBiz> payMethodViews = workFlowResponse.getMerchnatLiteViewResponse().getPayMethodViews();
            for (PayMethodViewsBiz payMethodViewsBiz : payMethodViews) {
                if (EPayMethod.WALLET.getMethod().equals(payMethodViewsBiz.getPayMethod())) {

                    payMethodViewsBiz.setPayMethod(EPayMethod.BALANCE.getMethod());
                }
            }
        }
    }

    public void setIsMerchantUPIVerified(NativeCashierInfoResponse response, String mid) {
        if (response != null && response.getBody() != null) {
            if (mid != null && !merchantPreferenceService.isUpiCollectWhitelisted(mid, true)) {
                response.getBody().setMerchantUPIVerified(false);
            } else {
                response.getBody().setMerchantUPIVerified(true);
            }
        }
    }

    private void setPostpaidEnabledOnMerchantAndDisabledOnUser(String mid, WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoResponse response) {
        if (workFlowResponse != null && workFlowResponse.getUserDetails() != null
                && !workFlowResponse.getUserDetails().isPaytmCCEnabled()) {
            response.getBody().setIsPostpaidEnabledOnMerchantAndDisabledOnUser(
                    isPayMethodConfiguredOnMerchant(mid, workFlowResponse.getMerchnatLiteViewResponse(),
                            EPayMethod.PAYTM_DIGITAL_CREDIT));
        }
    }

    public boolean isPayMethodConfiguredOnMerchant(String mid,
            LitePayviewConsultResponseBizBean litePayviewConsultResponse, EPayMethod ePayMethod) {
        if (litePayviewConsultResponse == null || litePayviewConsultResponse.getPayMethodViews() == null
                || !merchantPreferenceService.isPostpaidEnabledOnMerchant(mid, false)) {
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

    private void setDisableLoginStrip(NativeCashierInfoResponse response, String mid, String channelId) {
        if (merchantPreferenceService.isDisabledLoginStrip(mid, false)) {
            LOGGER.info("Login Strip disabled as pref DISABLED_LOGIN_STRIP enabled on MID.");
            response.getBody().getLoginInfo().setDisableLoginStrip(true);
        } else if (StringUtils.equals(channelId, EChannelId.WEB.getValue())
                && ((response.getBody().getQrCodeDetailsResponse() != null && response.getBody()
                        .getQrCodeDetailsResponse().getEncryptedData() != null) || (response.getBody().getQrDetail() != null && response
                        .getBody().getQrDetail().getDataUrl() != null))
                && !merchantPreferenceService.allowLoginOnDesktop(mid, false)) {
            LOGGER.info("Login Strip disabled due to QR code in WEB case");
            response.getBody().getLoginInfo().setDisableLoginStrip(true);
        }
    }

    private boolean isUpiIntentSupported(List<PayMethod> payModes) {
        AtomicBoolean upiIntentSupported = new AtomicBoolean(false);
        payModes.stream().filter(payMode -> payMode.getPayMethod().equals(UPI)).findFirst()
                .ifPresent(paymentMode -> paymentMode.getPayChannelOptions().stream().forEach(paychannelOptions -> {
                    if (Boolean.FALSE.toString().equals(paychannelOptions.getIsDisabled().getStatus())) {
                        UPI payChannelBase = (UPI) paychannelOptions;
                        if (UPI_PUSH.equals(payChannelBase.getInstId())) {
                            upiIntentSupported.set(true);
                        }
                    }

                }));
        return upiIntentSupported.get();
    }

    private void setDescMandatoryParams(NativeCashierInfoResponse response, String mid) {
        boolean isAddDescSupported = merchantPreferenceService.isAddDescriptionMandatorySupported(mid);
        if (isAddDescSupported) {
            response.getBody().setAddDescriptionMandatory(isAddDescSupported);
        }
    }

    private void setSubscriptionDetails(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse response, NativeCashierInfoContainerRequest request,
            WorkFlowResponseBean workFlowResponseBean) throws Exception {
        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(
                request.getNativeCashierInfoRequest().getBody().getRequestType())
                && request.isSuperGwApiHit()) {
            buildMandateSupportedApps(response, workFlowResponseBean);
        } else if (StringUtils.isNotEmpty(nativeCashierInfoRequest.getHead().getTxnToken())) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil
                    .getNativeInitiateRequest(nativeCashierInfoRequest.getHead().getTxnToken());
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                if (orderDetail instanceof SubscriptionTransactionRequestBody) {
                    nativeCashierInfoRequest.getBody().setPaymodeSequenceEnum(PaymodeSequenceEnum.SUBSCRIPTION);
                    SubscriptionTransactionRequestBody subsOrderDetail = (SubscriptionTransactionRequestBody) orderDetail;
                    String paymodeEligibleForAOASubscriptionFlow = ff4jUtils.getPropertyAsStringWithDefault(
                            AOA_SUBSCRIPTION_PAYMODES, StringUtils.EMPTY);
                    Set<String> paymodeEligibleForAOASubscription = new HashSet<>(
                            Arrays.asList(paymodeEligibleForAOASubscriptionFlow.split(",")));

                    if (aoaUtils.isAOAMerchant(nativeCashierInfoRequest.getBody().getMid())
                            && paymodeEligibleForAOASubscription.contains(subsOrderDetail.getSubscriptionPaymentMode())) {
                        // Skip setting subscriptionDetails in
                        // fetchPaymentOptions in case of AOA Subs Configured
                        // Paymodes
                        return;
                    }

                    // Setting appInvokeDevice in response which was captured in
                    // initiate api.
                    response.getBody().setAppInvokeDevice(
                            AppInvokeType.getAppInvokeType(subsOrderDetail.getAppInvokeDevice()));

                    EnhancedCashierPageWalletInfo walletInfo = processTransactionUtil
                            .getEnhancedCashierPageWalletInfo(request.getPaymentRequestBean(), response,
                                    nativeCashierInfoRequest.getHead().getTxnToken());
                    boolean isInsufficientBalance = processTransactionUtil.checkUserWalletSufficient(walletInfo,
                            subsOrderDetail.getTxnAmount().getValue(), response, true);
                    response.getBody().setSubscriptionDetail(
                            getSubscriptionDetails(walletInfo, isInsufficientBalance,
                                    request.getNativeCashierInfoRequest(), subsOrderDetail));
                    buildMandateSupportedApps(response, workFlowResponseBean);
                }
            }
        }
    }

    private void setLoginInfo(CashierInfoRequest request, NativeCashierInfoResponse response) {

        LoginInfo loginData = new LoginInfo();
        boolean isMobileNumberNonEditable = merchantPreferenceService.isMobileNumberNonEditable(request.getHead()
                .getMid(), false);
        boolean isUserLoggedIn = response.getBody().getUserDetails() != null ? true : false;

        boolean isPgAutoLoginEnabled = pgPreferenceHelper.checkPgAutologinEnabledFlag(request.getHead().getMid());
        loginData.setPgAutoLoginEnabled(isPgAutoLoginEnabled);
        loginData.setUserLoggedIn(isUserLoggedIn);
        if (response.getBody().getUserDetails() != null
                && StringUtils.isBlank(response.getBody().getUserDetails().getMobileNo()) && isMobileNumberNonEditable) {
            loginData.setMobileNumberNonEditable(false);
        } else {
            loginData.setMobileNumberNonEditable(isMobileNumberNonEditable);
        }

        response.getBody().setLoginInfo(loginData);

    }

    public SubscriptionDetail getSubscriptionDetails(EnhancedCashierPageWalletInfo walletInfo,
            boolean isInsufficientBalance, NativeCashierInfoRequest nativeCashierInfoRequest,
            SubscriptionTransactionRequestBody requestData) {

        if (ERequestType.isSubscriptionRequest(requestData.getRequestType())) {
            StringBuilder key = new StringBuilder(requestData.getRequestType()).append(nativeCashierInfoRequest
                    .getHead().getTxnToken());
            SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                    .toString());
            LOGGER.info("SubscriptionResponse from redis: {}", subscriptionResponse);
            SubscriptionDetail subscriptionDetail = NativeSubscriptionUtils.getSubscriptionDetail(requestData,
                    subscriptionResponse);

            // set display-txn-amount
            String txnAmount = requestData.getTxnAmount().getValue();
            if (NativeSubscriptionUtils.isZeroRupeesSubscription(txnAmount)) {
                txnAmount = "1";
            }

            // Set display-button-amount
            if ((SubsPaymentMode.NORMAL.name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode())
                    || SubsPaymentMode.PPI.name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode()) || SubsPaymentMode.UNKNOWN
                    .name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode())) && walletInfo != null) {
                subscriptionDetail
                        .setAmount(NativeSubscriptionUtils.calculateDisplayButtonAmount(txnAmount,
                                walletInfo.getWalletBalance(), requestData.getSubscriptionPaymentMode(),
                                isInsufficientBalance));
            }

            // Set subscription based validations
            if (SubsPaymentMode.NORMAL.name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode())) {

                // Set messages to be displayed on ui
                subscriptionDetail.setRenewMessage(NativeSubscriptionUtils.getNonWalletRenewMessage());
                if (!isInsufficientBalance) {
                    subscriptionDetail.setInfoMessageList(NativeSubscriptionUtils.getInfoMessage(txnAmount));
                }

            } else if (SubsPaymentMode.PPI.name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode())
                    && "Y".equalsIgnoreCase(requestData.getSubsPPIOnly())) {

                if (isInsufficientBalance) {
                    subscriptionDetail.setRenewMessage(NativeSubscriptionUtils.getWalletRenewMessage());
                }
                subscriptionDetail.setSaveCardMandatoryAddNPay(false);
            } else if (SubsPaymentMode.UNKNOWN.name().equalsIgnoreCase(requestData.getSubscriptionPaymentMode())) {

                if (isInsufficientBalance) {
                    subscriptionDetail.setRenewMessage(NativeSubscriptionUtils.getWalletRenewMessage());
                }
                subscriptionDetail.setSaveCardMandatoryAddNPay(false);
                subscriptionDetail.setNonSpecificPayMode(true);
            }
            return subscriptionDetail;
        }
        return null;
    }

    private void setGuestTokenInResponse(NativeCashierInfoRequest request, NativeCashierInfoResponse response) {
        response.getBody().setGuestToken(request.getHead().getTxnToken());
    }

    private String getOrderId(String mid) {
        String aggregatorMid = merchantDataUtil.getAggregatorMid(mid);

        return OfflinePaymentUtils.generateOrderId(aggregatorMid);
    }

    private void setNativeJsonRequestSupported(NativeCashierInfoRequest request, NativeCashierInfoResponse response,
            CashierInfoRequest cashierInfoRequest) {

        String appVersion = request.getBody().getAppVersion();
        String sdkVersion = request.getBody().getSdkVersion();
        String mid = cashierInfoRequest.getHead().getMid();

        boolean isNativeJsonRequestSupportedOnMerchant = merchantPreferenceService.isNativeJsonRequestSupported(mid);

        if (!isNativeJsonRequestSupportedOnMerchant) {
            response.getBody().setNativeJsonRequestSupported(false);
            return;
        }

        boolean isAppVersionAllowed = isAppVersionAllowedForNativeJsonRequest(appVersion, sdkVersion, mid);

        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("api", "/theia/api/v1/fetchPaymentOptions");

        if (TokenType.SSO == request.getHead().getTokenType()) {
            if (cashierInfoRequest.getBody().getOrderId() != null) {
                response.getBody().setOrderId(cashierInfoRequest.getBody().getOrderId());
            }
        }
        map.put("nativeJsonRequestSupported", String.valueOf(isAppVersionAllowed));
        map.put("appVersion", appVersion);
        map.put("sdkVersion", sdkVersion);

        nativePaymentUtil.logNativeResponse(map, EventNameEnum.NATIVE_JSON_REQUEST_SUPPORTED);

        response.getBody().setNativeJsonRequestSupported(isAppVersionAllowed);
    }

    private boolean isAppVersionAllowedForNativeJsonRequest(String appVersion, String sdkVersion, String mid) {

        if (StringUtils.isBlank(appVersion) && StringUtils.isBlank(sdkVersion)) {
            return true;
        }

        String disallowedAppVersions = ConfigurationUtil.getProperty(NATIVE_JSON_REQUEST_APP_VERSION);
        String disallowedSdkVersions = ConfigurationUtil.getProperty(NATIVE_JSON_REQUEST_SDK_VERSION);
        /*
         * appVersion/sdkVersion comes as iOS_1.2.3 ; Android_5.6.7
         */

        if (StringUtils.equals("ALL", disallowedAppVersions) || StringUtils.equals("ALL", disallowedSdkVersions)) {
            LOGGER.info("blocking nativeJsonRequestSupported flag because ALL in property");
            return false;
        }

        if (StringUtils.equals("NONE", disallowedAppVersions) && StringUtils.equals("NONE", disallowedSdkVersions)) {
            LOGGER.info("allowing nativeJsonRequestSupported flag because NONE in properties");
            return true;
        }

        String midAppVersionKey = mid + "@" + appVersion;
        String midSdkVersionKey = mid + "@" + sdkVersion;

        int appVersionIndex = StringUtils.indexOf(disallowedAppVersions, midAppVersionKey);
        if (appVersionIndex != -1) {
            LOGGER.info("blocked nativeJsonRequestSupported | mid {}, appVersion {}", mid, appVersion);
            return false;
        }
        appVersionIndex = StringUtils.indexOf(disallowedAppVersions, ALL + "@" + appVersion);
        if (appVersionIndex != -1) {
            LOGGER.info("blocked nativeJsonRequestSupported | appVersion {} for ALL mid", appVersion);
            return false;
        }

        int sdkVersionIndex = StringUtils.indexOf(disallowedSdkVersions, midSdkVersionKey);
        if (sdkVersionIndex != -1) {
            LOGGER.info("blocked nativeJsonRequestSupported | mid {}, sdkVersion {}", mid, sdkVersion);
            return false;
        }
        sdkVersionIndex = StringUtils.indexOf(disallowedSdkVersions, ALL + "@" + sdkVersion);
        if (sdkVersionIndex != -1) {
            LOGGER.info("blocked nativeJsonRequestSupported | sdkVersion {} for ALL mid", sdkVersion);
            return false;
        }

        return true;
    }

    private IPayviewConsultService getPayviewConsultService(CashierInfoRequest serviceRequest) {
        if ((serviceRequest.getHead().getTokenType() == TokenType.SSO)
                || (serviceRequest.getBody().getNativePersistData() != null)) {
            return userLoggedInPayviewService;
        } else {
            return userNotLoggedInPayviewService;
        }
    }

    private void enrich(CashierInfoRequest request, InitiateTransactionRequestBody orderDetail) {

        if (orderDetail.getPaytmSsoToken() != null && !orderDetail.getPaytmSsoToken().isEmpty()) {
            request.getHead().setToken(orderDetail.getPaytmSsoToken());
            request.getHead().setTokenType(TokenType.SSO);
        }

        request.getHead().setMid(orderDetail.getMid());
        request.getBody().setOrderAmount(orderDetail.getTxnAmount());
        request.getBody().setOrderId(orderDetail.getOrderId());
        request.getBody().setWebsite(orderDetail.getWebsiteName());
        request.getBody().setUserInfo(orderDetail.getUserInfo());

        if (ObjectUtils.notEqual(orderDetail.getUltimateBeneficiaryDetails(), null)) {
            request.getBody().setUltimateBeneficiaryDetails(orderDetail.getUltimateBeneficiaryDetails());
        }

        if (orderDetail.getEmiOption() != null && !orderDetail.getEmiOption().isEmpty()) {
            request.getBody().setEmiOption(orderDetail.getEmiOption());
        }

        if (orderDetail.getUserInfo() != null && orderDetail.getUserInfo().getCustId() != null
                && !orderDetail.getUserInfo().getCustId().isEmpty()) {
            request.getBody().setCustId(orderDetail.getUserInfo().getCustId());
            if (orderDetail.getExtendInfo() != null) {
                request.getBody().setSubwalletAmount(orderDetail.getExtendInfo().getSubwalletAmount());
            }
        }

        if (StringUtils.isNotBlank(orderDetail.getAccountNumber())) {
            request.getBody().setAccountNumber(orderDetail.getAccountNumber());
        }
        if (StringUtils.isNotBlank(orderDetail.getValidateAccountNumber())) {
            request.getBody().setValidateAccountNumber(orderDetail.getValidateAccountNumber());
        }
        if (StringUtils.isNotBlank(orderDetail.getAllowUnverifiedAccount())) {
            request.getBody().setAllowUnverifiedAccount(orderDetail.getAllowUnverifiedAccount());
        }

        if (orderDetail.getRiskFeeDetails() != null && orderDetail.getRiskFeeDetails().getInitialAmount() != null) {
            request.getBody().setAmountForWalletConsultInRisk(
                    orderDetail.getRiskFeeDetails().getInitialAmount().getValue());
        }

        if (!StringUtils.isEmpty(orderDetail.getPromoCode())) {
            request.getBody().setPromoCode(orderDetail.getPromoCode());
        }
        if (orderDetail.getLinkDetailsData() != null
                && StringUtils.isNotBlank(orderDetail.getLinkDetailsData().getSubRequestType())) {
            request.getBody().setRequestType(orderDetail.getLinkDetailsData().getSubRequestType());
        } else {
            request.getBody().setRequestType(orderDetail.getRequestType());
        }
        request.getBody().setGoodsInfo(orderDetail.getGoods());
        request.getBody().setTargetPhoneNo(orderDetail.getTargetPhoneNo());
        request.getBody().setNativeAddMoney(orderDetail.isNativeAddMoney());
        request.getBody().setBankAccountNumbers(orderDetail.getBankAccountNumbers());
        request.getBody().setAppInvoke(orderDetail.isAppInvoke());
        // simplified PaymentOfferCheck
        if (orderDetail.getSimplifiedPaymentOffers() != null) {
            request.getBody().setSimplifiedPaymentOffers(orderDetail.getSimplifiedPaymentOffers());
        }

        // EMI Subvention All in one SDK Flow
        if (orderDetail.getSimplifiedSubvention() != null
                && BooleanUtils.isTrue(orderDetail.getSimplifiedSubvention().getSelectPlanOnCashierPage())) {
            SubventionDetails subventionDetails = new SubventionDetails();
            subventionDetails.setCustomerId(orderDetail.getSimplifiedSubvention().getCustomerId());
            subventionDetails.setItems(orderDetail.getSimplifiedSubvention().getItems());
            subventionDetails.setSubventionAmount(orderDetail.getSimplifiedSubvention().getSubventionAmount());
            boolean isAmountBasedSubvention = org.springframework.util.CollectionUtils.isEmpty(subventionDetails
                    .getItems());
            if (isAmountBasedSubvention) {
                subventionDetails.setStrategy(EMISubventionStrategy.AMOUNT_BASED);
                request.getBody().setItems(
                        subventionEmiServiceHelper.prepareItemListForAmountBasedSubvention(orderDetail));
            } else {
                subventionDetails.setStrategy(EMISubventionStrategy.ITEM_BASED);
                request.getBody().setItems(
                        subventionDetails.getItems().stream().map(SubventionEmiServiceHelper::transform)
                                .collect(Collectors.toList()));
            }
            request.getBody().setSubventionDetails(subventionDetails);

            // To Get EMI Subvention details corresponding to each saved card
            request.getBody().setEmiSubventionCustomerId(subventionDetails.getCustomerId());
            request.getBody().setEmiSubventionRequired(true);
            request.getBody().setEmiSubventedTransactionAmount(orderDetail.getTxnAmount().getValue());
        }

    }

    private void enrichSubscriptionSpecific(CashierInfoRequest serviceRequest,
            InitiateTransactionRequestBody orderDetail) {
        if (orderDetail instanceof SubscriptionTransactionRequestBody) {
            SubscriptionTransactionRequestBody subsOrderDetail = (SubscriptionTransactionRequestBody) orderDetail;
            serviceRequest.getBody().setSubscriptionPaymentMode(subsOrderDetail.getSubscriptionPaymentMode());
            serviceRequest.getBody().setSubscriptionTransactionRequestBody(subsOrderDetail);
            serviceRequest.getBody().setMandateType(MandateMode.getByName(subsOrderDetail.getMandateType()));
            if (nativeSubscriptionHelper.subsPPIAmountLimitBreached(
                    com.paytm.pgplus.cashier.enums.PaymentMode.PPI.getMode(),
                    subsOrderDetail.getSubscriptionMaxAmount(), subsOrderDetail.getMid())) {
                if (serviceRequest.getBody().getDisabledInstrumentTypes() == null) {
                    serviceRequest.getBody().setDisabledInstrumentTypes(new ArrayList<InstrumentType>());
                }

                serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.WALLET);
            }
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = (SubscriptionTransactionRequestBody) orderDetail;

            if (eligibleConditionForFilterUpiSubsPaymentModeForRecurring(subscriptionTransactionRequestBody,
                    serviceRequest)) {
                if (serviceRequest.getBody().getDisabledInstrumentTypes() == null) {
                    serviceRequest.getBody().setDisabledInstrumentTypes(new ArrayList<InstrumentType>());
                }
                serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.UPI);
            }
            // Disabling BANK_MANDATE paymode for subs frequency unit -
            // FORTNIGHT
            if (nativeSubscriptionHelper.invalidSubsFrequencyUnitForBankMandate(
                    subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit(),
                    subscriptionTransactionRequestBody.getSubscriptionPaymentMode())
                    && !subscriptionTransactionRequestBody.isFlexiSubscription()) {
                if (serviceRequest.getBody().getDisabledInstrumentTypes() == null) {
                    serviceRequest.getBody().setDisabledInstrumentTypes(new ArrayList<InstrumentType>());
                }
                serviceRequest.getBody().getDisabledInstrumentTypes().add(InstrumentType.BANK_MANDATE);
            }
        }
    }

    private CashierInfoRequest generateServiceRequestData(NativeCashierInfoRequest inRequest, boolean checkLoginCookie) {
        CashierInfoRequest outRequest = new CashierInfoRequest();
        TokenRequestHeader inHeader = inRequest.getHead();
        RequestHeader outHeader = new RequestHeader();
        outHeader.setRequestTimestamp(inHeader.getRequestTimestamp());
        outHeader.setToken(inHeader.getTxnToken());
        outHeader.setTokenType(inHeader.getTokenType());

        UserDetailsBiz userDetails = null;

        NativeCashierInfoRequestBody inRequestBody = inRequest.getBody();
        if (TokenType.SSO == inHeader.getTokenType()) {
            outHeader.setToken(inHeader.getToken());
            outHeader.setTokenType(TokenType.SSO);
            // in access token, sso token is already validated in create access
            // token request
            if (null != inRequestBody && StringUtils.isBlank(inRequest.getBody().getAccessToken())) {
                userDetails = nativeValidationService.validateSSOToken(inHeader.getToken(), inRequestBody.getMid());
            }
        }

        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        httpServletRequest.setAttribute(WORKFLOW, inHeader.getWorkFlow());

        InitiateTransactionRequestBody orderDetail = null;

        if (StringUtils.equals(inHeader.getWorkFlow(), CHECKOUT)
                && httpServletRequest.getAttribute(DYNAMIC_QR_REQUIRED) == null
                && !TokenType.GUEST.equals(inHeader.getTokenType())
                && !inRequest.getBody().isBlinkCheckoutAccessSupport()
                && !TokenType.SSO.equals(inHeader.getTokenType())) {
            httpServletRequest.setAttribute(DYNAMIC_QR_REQUIRED, true);

            try {
                orderDetail = nativeSessionUtil.getOrderDetail(inHeader.getTxnToken());
            } catch (SessionExpiredException see) {
                LOGGER.debug("SessionExpiredException for workFlow=checkout with txnToken");
            }

            if (orderDetail != null && orderDetail.getDisablePaymentMode() != null
                    && !orderDetail.getDisablePaymentMode().isEmpty()) {
                List<PaymentMode> payModes = orderDetail.getDisablePaymentMode();
                payModes.stream()
                        .filter(payMode -> payMode.getMode().equals(UPI))
                        .findFirst()
                        .ifPresent(
                                paymentMode -> {

                                    if ((paymentMode.getChannels() != null && !paymentMode.getChannels().isEmpty() && paymentMode
                                            .getChannels().contains(UPI_PUSH))
                                            || (paymentMode.getChannels() == null || paymentMode.getChannels()
                                                    .isEmpty())) {
                                        httpServletRequest.setAttribute(SHOW_UPI_QR, false);
                                    }
                                });
            }
        }

        if (StringUtils.equals(inHeader.getWorkFlow(), CHECKOUT) || checkLoginCookie) {
            /*
             * This checks if user's login cookie is present or not, if yes,
             * gets userDetails
             */
            if (userDetails == null) {
                userDetails = getUserDetails(inRequest);
            }

            if (userDetails != null) {
                nativePaymentUtil.setSsoTokenInHttpRequest(userDetails.getUserToken());
                if ((TokenType.TXN_TOKEN == inHeader.getTokenType() || StringUtils.isNotEmpty(inHeader.getTxnToken()))
                        && !TokenType.SSO.equals(inHeader.getTokenType())) {

                    if (orderDetail == null) {
                        orderDetail = nativeSessionUtil.getOrderDetail(inHeader.getTxnToken());
                    }

                    orderDetail.setPaytmSsoToken(userDetails.getUserToken());
                    nativeSessionUtil.setOrderDetail(inHeader.getTxnToken(), orderDetail);
                }
            }
        }

        outHeader.setVersion(inHeader.getVersion());
        outRequest.setHead(outHeader);
        CashierInfoRequestBody cashierInfoRequestBody = new CashierInfoRequestBody();
        cashierInfoRequestBody.setInstrumentTypes(Arrays.asList(new InstrumentType[] { InstrumentType.ALL }));
        cashierInfoRequestBody.setSavedInstrumentsTypes(Arrays.asList(new InstrumentType[] { InstrumentType.ALL }));
        cashierInfoRequestBody.setChannelId(inRequest.getHead().getChannelId());
        outRequest.setBody(cashierInfoRequestBody);
        if (userDetails != null) {
            NativePersistData nativePersistData = new NativePersistData(userDetails);
            outRequest.getBody().setNativePersistData(nativePersistData);
            outRequest.getBody().setCustId(userDetails.getUserId());
        }
        if (null != inRequestBody) {
            cashierInfoRequestBody.setPostpaidOnboardingSupported(inRequestBody.isPostpaidOnboardingSupported());
            cashierInfoRequestBody.setEmiOption(inRequestBody.getEmiOption());
            cashierInfoRequestBody.setEightDigitBinRequired(inRequestBody.isEightDigitBinRequired());
            cashierInfoRequestBody.setCardHashRequired(inRequestBody.isCardHashRequired());
            cashierInfoRequestBody.setAmountForWalletConsultInRisk(inRequestBody.getOrderAmount());
            cashierInfoRequestBody.setAmountForPaymentFlow(inRequestBody.getOrderAmount());
            cashierInfoRequestBody.setAppVersion(inRequestBody.getAppVersion());
            cashierInfoRequestBody.setInternalFetchPaymentOptions(inRequestBody.isInternalFetchPaymentOptions());
            cashierInfoRequestBody.setExternalFetchPaymentOptions(inRequestBody.isExternalFetchPaymentOptions());
            cashierInfoRequestBody.setOriginChannel(inRequestBody.getOriginChannel());
            cashierInfoRequestBody.setDeviceId(inRequestBody.getDeviceId());
            cashierInfoRequestBody.setProductCode(inRequestBody.getProductCode());
            cashierInfoRequestBody.setAccessToken(inRequestBody.getAccessToken());
            cashierInfoRequestBody.setAddMoneyFeeAppliedOnWallet(inRequestBody.isAddMoneyFeeAppliedOnWallet());
            cashierInfoRequestBody.setInitialAddMoneyAmount(inRequestBody.getInitialAddMoneyAmount());
            cashierInfoRequestBody.setReturnDisabledChannels(inRequestBody.isReturnDisabledChannels());
            cashierInfoRequestBody.setGoodsInfo(inRequestBody.getGoods());
            cashierInfoRequestBody.setReturnToken(inRequestBody.isReturnToken());

            if (inRequestBody.isMlvSupported()) {
                cashierInfoRequestBody.setMlvSupported(inRequestBody.isMlvSupported());
                cashierInfoRequestBody.setAppVersion(inRequestBody.getAppVersion());
                cashierInfoRequestBody.setqRCodeInfo(inRequestBody.getqRCodeInfo());
            }
            cashierInfoRequestBody.setUpiRecurringSupport(inRequestBody.isUpiRecurringSupport());
            cashierInfoRequestBody.setUpiPayConfirmSupport(inRequestBody.isUpiPayConfirmSupport());
            cashierInfoRequestBody.setCardPreAuthType(inRequest.getBody().getCardPreAuthType());
            cashierInfoRequestBody.setPreAuthBlockSeconds(inRequest.getBody().getPreAuthBlockSeconds());
            cashierInfoRequestBody.setFetchAddMoneyOptions(inRequestBody.isFetchAddMoneyOptions());
            cashierInfoRequestBody.setAddNPayOnPostpaidSupported(inRequestBody.isAddNPayOnPostpaidSupported());
            cashierInfoRequestBody.setFetchPaytmInstrumentsBalance(inRequestBody.isFetchPaytmInstrumentsBalance());
            cashierInfoRequestBody.setTwoFADetails(inRequestBody.getTwoFADetails());
            cashierInfoRequestBody.setDealsFlow(paymentOffersServiceHelperV2.isDealsFlow(inRequestBody
                    .getAffordabilityInfo()));
            cashierInfoRequestBody.setUpiLiteEligible(inRequest.getBody().isUpiLiteEligible());
            cashierInfoRequestBody.setRequestType(inRequestBody.getRequestType());
            cashierInfoRequestBody.setSubscriptionTransactionRequestBody(inRequestBody
                    .getSubscriptionTransactionRequestBody());
            if (TokenType.SSO.equals(inHeader.getTokenType()) && StringUtils.isNotBlank(inRequestBody.getOrderAmount())) {
                cashierInfoRequestBody.setOrderAmount(new Money(inRequestBody.getOrderAmount()));
            }
        }
        return outRequest;
    }

    private CashierInfoRequest generateServiceRequestData(NativeCashierInfoRequest inRequest) {
        return generateServiceRequestData(inRequest, false);
    }

    private void disableCodIfRequired(CashierInfoRequest cashierInfoRequest,
            NativeCashierInfoRequest nativeCashierInfoRequest) {
        if (!isCODApplicableOnOrder(cashierInfoRequest)) {
            if (nativeCashierInfoRequest.getBody().getDisablePaymentMode() == null) {
                nativeCashierInfoRequest.getBody().setDisablePaymentMode(new ArrayList<>());
            }

            nativeCashierInfoRequest.getBody().getDisablePaymentMode()
                    .add(new PaymentMode(EPayMethod.MP_COD.getOldName()));
        }
    }

    private boolean isCODApplicableOnOrder(CashierInfoRequest cashierInfoRequest) {

        /*
         * COD is only applicable when user is logged in and TXN amount is
         * greater than equal to configured minimum amount
         */
        boolean codEnabled = false;

        if (cashierInfoRequest.getHead().getTokenType() == TokenType.SSO) {
            codEnabled = true;

            String txnAmount = cashierInfoRequest.getBody().getOrderAmount().getValue();
            Double transAmount = Double.parseDouble(txnAmount);
            PaytmProperty paytmProperty = configurationDataService
                    .getPaytmProperty(TheiaConstant.ExtraConstants.COD_MIN_AMOUNT);
            if ((paytmProperty != null) && NumberUtils.isNumber(paytmProperty.getValue())) {
                String value = paytmProperty.getValue();
                double minCodAmount = Double.parseDouble(value);
                if (minCodAmount > transAmount) {
                    codEnabled = false;
                }
            }
        }
        return codEnabled;
    }

    private boolean checkCODPayMethodEnabled(NativeCashierInfoResponse response) {
        if (response.getBody() != null && response.getBody().getMerchantPayOption() != null
                && CollectionUtils.isNotEmpty(response.getBody().getMerchantPayOption().getPayMethods())) {
            return response
                    .getBody()
                    .getMerchantPayOption()
                    .getPayMethods()
                    .stream()
                    .anyMatch(
                            payMethod -> StringUtils.equalsIgnoreCase(EPayMethod.MP_COD.getOldName(),
                                    payMethod.getPayMethod()));
        }
        return false;
    }

    // check for Zest in case of EMI Offline and normal condition
    private boolean isZestEligible(CashierInfoRequest cashierInfoRequest) {
        if ((cashierInfoRequest != null && cashierInfoRequest.getBody() != null)
                && ((cashierInfoRequest.getBody().getOrderAmount() == null) || (cashierInfoRequest.getBody()
                        .getOrderAmount() != null
                        && StringUtils.isNotBlank(cashierInfoRequest.getBody().getOrderAmount().getValue())
                        && Double.valueOf(cashierInfoRequest.getBody().getOrderAmount().getValue()) >= Double
                                .valueOf(ConfigurationUtil.getProperty(ZEST_MONEY_MINAMOUNT_TEXT, "500")) && Double
                        .valueOf(cashierInfoRequest.getBody().getOrderAmount().getValue()) <= Double
                        .valueOf(ConfigurationUtil.getProperty(ZEST_MONEY_MAXAMOUNT_TEXT, "150000"))))) {
            return true;

        }
        return false;
    }

    private void makePaymentRequestBeanBackwardCompatible(PaymentRequestBean paymentRequestBean) {
        if (paymentRequestBean != null) {

            if (paymentRequestBean.isCreateOrderRequired()) {
                paymentRequestBean.setCreateOrderRequired(false);
            }
            if (paymentRequestBean.isCreateTopupRequired()) {
                paymentRequestBean.setCreateTopupRequired(false);
            }
            if (paymentRequestBean.isEnhancedCashierPageRequest()) {
                if (ERequestType.NATIVE_SUBSCRIPTION_PAY.getType()
                        .equalsIgnoreCase(paymentRequestBean.getRequestType())) {
                    paymentRequestBean.setRequestType(ERequestType.NATIVE_SUBSCRIPTION.getType());
                } else {
                    paymentRequestBean.setRequestType(ERequestType.DEFAULT.getType());
                }
            }

            InitiateTransactionRequestBody initiateTransactionRequestBody = nativeSessionUtil
                    .getOrderDetail(paymentRequestBean.getTxnToken());
            if (initiateTransactionRequestBody != null
                    && (StringUtils.isNotBlank(paymentRequestBean.getLinkId()) || StringUtils
                            .isNotBlank(paymentRequestBean.getInvoiceId()))) {
                paymentRequestBean.setRequestType(initiateTransactionRequestBody.getRequestType());
            }
        }
    }

    private void setMerchantDetailsInResponse(NativeCashierInfoContainerRequest request,
            NativeCashierInfoResponse response, CashierInfoRequest infoRequest) {

        NativeCashierInfoRequest cashierInfoRequest = request.getNativeCashierInfoRequest();
        String mid = null;
        if (null != cashierInfoRequest.getBody().getMid()) {
            mid = cashierInfoRequest.getBody().getMid();
        } else {
            mid = infoRequest.getHead().getMid();
        }
        if (StringUtils.isBlank(mid)) {
            return;
        }

        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(mid);
        MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService.getMerchantInfo(merchantDetailsRequest);
        if (merchantDetailsResponse == null) {
            LOGGER.error("Not able to fetch merchant logo");
            return;
        }
        MerchantDetails merchantDetails = response.getBody().getMerchantDetails();
        if (merchantDetails == null) {
            merchantDetails = new MerchantDetails();
        }
        if (StringUtils.isBlank(merchantDetails.getMerchantName())) {
            if (StringUtils.isNotBlank(merchantDetailsResponse.getMerchantBusinessName())) {
                merchantDetails.setMerchantName(merchantDetailsResponse.getMerchantBusinessName());
            } else {
                merchantDetails.setMerchantName(merchantDetailsResponse.getMerchantDisplayName());
            }
        }
        MerchantProfile merchantProfileInfo = null;
        try {
            merchantProfileInfo = merchantDataService.getMerchantProfileInfo(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantProfileInfo :: {}", merchantProfileInfo);
            merchantDetails.setMerchantBankName(merchantProfileInfo.getMerchantBankName());
            String ppiLimitForVerifiedMerchant = ConfigurationUtil.getProperty("ppi.limit.for.verified.merchant", "0");
            if (merchantProfileInfo.getPpiLimit() != null
                    && ppiLimitForVerifiedMerchant.contains(merchantProfileInfo.getPpiLimit().toString())) {
                merchantDetails.setVerifiedMerchant(true);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occurred on calling merchant profile api ", e);
        }

        merchantDetails.setMerchantLogo(merchantDetailsResponse.getMerchantImageName());
        if (merchantPreferenceService.isAppInvokeAllowed(mid, false)) {
            merchantDetails.setIsAppInvokeAllowed(true);
        }

        merchantDetails.setMerchantDisplayName(merchantDetailsResponse.getMerchantDisplayName());
        merchantDetails.setAutoAppInvokeAllowed(checkIfAutoAppInvokeAllowed(request, infoRequest));

        // Setting identifier for AOA merchant in response
        if (aoaUtils.isAOAMerchant(mid)) {
            merchantDetails.setAoaMerchant(true);
        }

        response.getBody().setMerchantDetails(merchantDetails);
    }

    private void setUserDetailsInResponse(NativeCashierInfoRequest nativeCashierInfoRequest,
            UserDetailsBiz userDetailsBiz, NativeCashierInfoResponse response, CashierInfoRequest cashierInfoRequest) {

        if (userDetailsBiz == null) {
            return;
        }

        UserDetails userDetails = new UserDetails();
        userDetails.setMobileNo(userDetailsBiz.getMobileNo());
        userDetails.setEmail(userDetailsBiz.getEmail());
        userDetails.setUserName(userDetailsBiz.getUserName());
        userDetails.setKYC(userDetailsBiz.isKYC());
        userDetails.setPaytmCCEnabled(userDetailsBiz.isPaytmCCEnabled());
        userDetails.setConsentForAutoDebitPref(userDetailsBiz.getConsentForAutoDebitPref());
        userDetails.setShowConsentSheetAutoDebit(userDetailsBiz.getShowConsentSheetAutoDebit());
        userDetails.setCapturePostpaidConsentForWalletTopUp(userDetailsBiz.getCapturePostpaidConsentForWalletTopUp());

        boolean isOneClickOnboardingValid = validateOneClickOnboarding(nativeCashierInfoRequest, cashierInfoRequest,
                userDetailsBiz);
        if (isOneClickOnboardingValid) {
            userDetails.setUserEligibileForPostPaidOnboarding(userDetailsBiz.getUserEligibileForPostPaidOnboarding());
            userDetails.setPostpaidCreditLimit(userDetailsBiz.getPostpaidCreditLimit());
            userDetails.setPostpaidOnboardingStageMsg(userDetailsBiz.getPostpaidOnboardingStageMsg());
        }

        response.getBody().setUserDetails(userDetails);
    }

    private boolean checkIfAutoAppInvokeAllowed(NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest,
            CashierInfoRequest cashierInfoRequest) {

        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = nativeCashierInfoContainerRequest
                .getNativeCashierInfoRequest().getBody();
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoRequest.getBody();
        PaymentRequestBean requestData = nativeCashierInfoContainerRequest.getPaymentRequestBean();
        if (requestData == null) {
            requestData = new PaymentRequestBean();
            requestData.setRequestType(cashierInfoRequestBody.getRequestType());
            requestData.setCustId(cashierInfoRequestBody.getCustId());
            requestData.setMid(nativeCashierInfoRequestBody.getMid());
            if (cashierInfoRequestBody.getChannelId() != null)
                requestData.setChannelId(cashierInfoRequestBody.getChannelId().getValue());
        }

        UserAgentInfo userAgentInfo = null;
        try {
            userAgentInfo = BrowserUtil.getUserAgentInfo();
        } catch (Exception e) {
            LOGGER.error("Exception occured while getting userAgentInfo", e);
        }
        boolean appInvokeV2ff4j = ff4JUtil.isFeatureEnabled(THEIA_AUTO_APP_INVOKE_PHASE2, requestData.getMid());
        boolean isSdkProcessTxn = BooleanUtils.isTrue(requestData.isSdkProcessTxnFlow());

        return processTransactionControllerHelper.checkIfAutoAppInvokeAllowed(requestData, userAgentInfo, true,
                appInvokeV2ff4j, isSdkProcessTxn);
    }

    private UserDetailsBiz getUserDetails(NativeCashierInfoRequest nativeCashierInfoRequest) {
        String token = nativeCashierInfoRequest.getHead().getTxnToken();
        if (StringUtils.isEmpty(token)) {
            token = nativeCashierInfoRequest.getHead().getToken();
        }
        if (StringUtils.isNotBlank(token)) {
            String ssoToken = nativeSessionUtil.getSsoToken(token);
            if (StringUtils.isNotBlank(ssoToken)) {
                LOGGER.info("ssoToken already attached to token");
                UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(ssoToken,
                        nativeCashierInfoRequest.getBody().getMid());
                userDetailsBiz.setUserToken(ssoToken);
                return userDetailsBiz;
            }
        }
        if (nativeCashierInfoRequest.getBody() != null && nativeCashierInfoRequest.getBody().getMid() != null) {
            String mid = nativeCashierInfoRequest.getBody().getMid();
            return nativeValidationService.validateLoginViaCookie(mid);
        }
        return null;
    }

    private void updateChannelId(NativeCashierInfoContainerRequest request) {
        /*
         * This is being done for cases when merchant is sending channelId=APP,
         * in the contract only two channelId's are allowed {WEB, WAP} We change
         * APP to WAP
         */
        if (request.getNativeCashierInfoRequest() == null || request.getNativeCashierInfoRequest().getHead() == null) {
            return;
        }
        EChannelId channelId = request.getNativeCashierInfoRequest().getHead().getChannelId();
        if (EChannelId.APP == channelId) {
            request.getNativeCashierInfoRequest().getHead().setChannelId(EChannelId.WAP);
            LOGGER.info("Setting channelId WAP from APP");
        }
    }

    private void disablePromoForPWPMerchant(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest) {

        if (StringUtils.equals(Boolean.TRUE.toString(), workFlowResponseBean.getPwpEnabled())
                && cashierInfoRequest.getBody() != null
                && StringUtils.isNotBlank(cashierInfoRequest.getBody().getPromoCode())) {
            throw new PWPPromoServiceException(INVALID_PROMOCODE);
        }
    }

    private void setOrderAmountInFpo(NativeCashierInfoResponseBody response, CashierInfoRequest cashierInfoRequest) {
        if (cashierInfoRequest.getBody().getOrderAmount() != null) {
            response.setOrderAmount(cashierInfoRequest.getBody().getOrderAmount().getValue());
        }
    }

    private void buildMandateSupportedApps(NativeCashierInfoResponse response, WorkFlowResponseBean workFlowResponseBean) {
        List<MandateSupportedApps> mandateSupportedAppsList = new ArrayList<>();

        Map<String, Integer> mandatePackagePriority = getMandatePackagePriority(workFlowResponseBean);
        if (!CollectionUtils.isEmpty(workFlowResponseBean.getPspList())) {
            List<RecurringMandatePsp> recurringMandatePsps = workFlowResponseBean.getPspList();
            for (RecurringMandatePsp recurringMandatePspItem : recurringMandatePsps) {
                MandateSupportedApps mandateSupportedApps = new MandateSupportedApps(recurringMandatePspItem.getName(),
                        recurringMandatePspItem.getHandle(), recurringMandatePspItem.getPackageName(),
                        mandatePackagePriority.get(recurringMandatePspItem.getPackageName()));
                mandateSupportedAppsList.add(mandateSupportedApps);
            }
        }
        response.getBody().setMandateSupportedApps(mandateSupportedAppsList);
    }

    private Map<String, Integer> getMandatePackagePriority(WorkFlowResponseBean flowResponseBean) {

        List<String> storedMandatePackagelist = Collections.emptyList();
        Map<String, Integer> mandatePackageListPriority = new HashMap<>();
        Map<String, Integer> upiPackageMap = new HashMap<>();
        int i = 0;
        Set<String> upiPackageSet = new HashSet<String>();

        if (!CollectionUtils.isEmpty(flowResponseBean.getPspList())) {
            for (RecurringMandatePsp recurringMandatePsp : flowResponseBean.getPspList()) {
                if (!upiPackageMap.containsKey(recurringMandatePsp.getPackageName())) {
                    i++;
                    upiPackageMap.put(recurringMandatePsp.getPackageName(), i);
                    upiPackageSet.add(recurringMandatePsp.getPackageName());

                }
            }
        }

        String property = com.paytm.pgplus.common.config.ConfigurationUtil
                .getProperty(TheiaConstant.RequestParams.UPI_MANDATE_APP_LIST);
        i = 1;
        if (StringUtils.isNotBlank(property)) {

            storedMandatePackagelist = Arrays.asList(property.split(","));
            for (String storedMandatePackageItem : storedMandatePackagelist) {
                if (upiPackageMap.containsKey(storedMandatePackageItem)) {
                    mandatePackageListPriority.put(storedMandatePackageItem, i);
                    i++;
                    upiPackageSet.remove(storedMandatePackageItem);
                }
            }
        }

        if (!upiPackageSet.isEmpty()) {
            for (String upiPspItem : upiPackageSet) {
                mandatePackageListPriority.put(upiPspItem, i);
                i++;
            }
        }
        return mandatePackageListPriority;
    }

    private boolean eligibleConditionForFilterUpiSubsPaymentModeForRecurring(
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody, CashierInfoRequest serviceRequest) {
        String subsMaxAmount = subscriptionTransactionRequestBody.getSubscriptionMaxAmount();
        Money txnAmount = subscriptionTransactionRequestBody.getTxnAmount();
        if ((StringUtils.isNotBlank(subsMaxAmount) && nativeSubscriptionHelper
                .subsUPIAmountLimitBreached(subsMaxAmount))
                || (ObjectUtils.notEqual(txnAmount, null) && nativeSubscriptionHelper
                        .subsUPIAmountLimitBreached(subscriptionTransactionRequestBody.getTxnAmount().getValue()))
                || ((!FrequencyUnit.ONDEMAND.getName().equals(
                        subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit()) && !subscriptionTransactionRequestBody
                        .isFlexiSubscription()) && (nativeSubscriptionHelper
                        .invalidUpifrequencyCycle(subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit())
                        || nativeSubscriptionHelper.subsUpiMonthlyFrequencyBreach(
                                subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit(),
                                subscriptionTransactionRequestBody.getSubscriptionFrequency())
                        || nativeSubscriptionHelper.invalidUpiGraceDays(
                                subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit(),
                                subscriptionTransactionRequestBody.getSubscriptionGraceDays(),
                                subscriptionTransactionRequestBody.getSubscriptionFrequency())
                        || nativeSubscriptionHelper.invalidSubsRetryCount(
                                subscriptionTransactionRequestBody.getSubscriptionEnableRetry(),
                                subscriptionTransactionRequestBody.getSubscriptionRetryCount())
                        || nativeSubscriptionHelper.invalidUpiSubsStartDate(
                                subscriptionTransactionRequestBody.getSubscriptionStartDate(),
                                subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit()) || nativeSubscriptionHelper
                            .invalidUpiSubsFrequency(subscriptionTransactionRequestBody.getSubscriptionFrequency())))
                || isbackwardCompatibilityNotHandled(subscriptionTransactionRequestBody, serviceRequest)) {

            return true;
        }
        return false;

    }

    private QrDetail getQrDetail(String mID, NativeCashierInfoResponse nativeCashierInfoResponse) {
        QRCodeDetailsResponse qrCodeDetailsResponse = nativeCashierInfoResponse.getBody().getQrCodeDetailsResponse();
        QrDetail qrDetail = null;

        if (qrCodeDetailsResponse != null) {
            String pageTimeout = com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_QR_PAGE_TIMEOUT, "4800000");
            boolean isPRN = merchantPreferenceService.isPRNEnabled(mID);
            boolean isUPIQR = QrType.UPI_QR == qrCodeDetailsResponse.getQrType();
            String displayMessage;
            if (isUPIQR)
                displayMessage = com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_UPI_QR_DISPLAY_MESSAGE);
            else
                displayMessage = com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_PAYTM_QR_DISPLAY_MESSAGE);

            qrDetail = new QrDetail(qrCodeDetailsResponse.getPath(), Long.valueOf(pageTimeout), displayMessage, true,
                    isPRN, isUPIQR);
        }

        return qrDetail;
    }

    private boolean isbackwardCompatibilityNotHandled(
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody, CashierInfoRequest serviceRequest) {
        if (merchantExtendInfoUtils.isMerchantOnPaytm(subscriptionTransactionRequestBody.getMid())
                || subscriptionTransactionRequestBody.isAppInvoke()) {
            LOGGER.info("Either merchant is on-us, or it's an app Invoke flow and flag is {}", serviceRequest.getBody()
                    .isUpiRecurringSupport());
            return !serviceRequest.getBody().isUpiRecurringSupport();
        } else
            return false;
    }

    private void setSsoTokenInCaseForDeferred(CashierInfoRequest cashierInfoRequest,
            NativeCashierInfoRequest nativeCashierInfoRequest, String token) {

        if (cashierInfoRequest.getBody() != null && cashierInfoRequest.getBody().getNativePersistData() != null
                && cashierInfoRequest.getBody().getNativePersistData().getUserDetails() != null) {
            LOGGER.info("Setting SSO token in cache for Guest/Access Token Flow");
            nativeSessionUtil.setSsoToken(token, cashierInfoRequest.getBody().getNativePersistData().getUserDetails()
                    .getUserToken());
        }
    }

    private void setOtpAuthorisedFlag(NativeCashierInfoRequest request, NativeCashierInfoResponse response) {
        String txnToken = request.getHead().getTxnToken();
        boolean isAuthorised = nativeSessionUtil.getSubsOtpAuthorisedFlag(txnToken);
        response.getBody().setOtpAuthorised(isAuthorised);
    }

    private void disableUPICollectAndIntentForLinkPayments(NativeCashierInfoRequestBody nativeCashierInfoRequestBody,
            PaymentRequestBean paymentRequestBean) {
        String fpoDisablePaymodes = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.FPO_DISBALE_PAYMODES);
        LOGGER.debug(
                "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments | fpoDisablePaymodes = {}",
                fpoDisablePaymodes);
        List<String> fpoDisablePaymodesList = StringUtils.isNotBlank(fpoDisablePaymodes) ? Arrays
                .asList(fpoDisablePaymodes.split(",")) : null;
        if (paymentRequestBean != null
                && CollectionUtils.isNotEmpty(fpoDisablePaymodesList)
                && (paymentRequestBean.getLinkDetailsData() != null
                        || StringUtils.isNotBlank(paymentRequestBean.getLinkId()) || StringUtils
                            .isNotBlank(paymentRequestBean.getInvoiceId()))) {
            String fpoDisablePaymodesOnMerchant = ConfigurationUtil
                    .getProperty(TheiaConstant.ExtraConstants.FPO_DISBALE_PAYMODES_ON_MERCHANT_TYPE);
            List<String> fpoDisablePaymodesOnMerchantList = StringUtils.isNoneBlank(fpoDisablePaymodesOnMerchant) ? Arrays
                    .asList(fpoDisablePaymodesOnMerchant.split(",")) : null;
            LOGGER.debug(
                    "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments | fpoDisablePaymodesOnMerchant = {}",
                    fpoDisablePaymodesOnMerchant);
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
            String merchantLimitType = null;
            try {
                merchantExtendedInfoResponse = MappingServiceClient
                        .getMerchantExtendedInfo(paymentRequestBean.getMid());
                if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                        && merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit() != null) {
                    merchantLimitType = String.valueOf(merchantExtendedInfoResponse.getExtendedInfo()
                            .getMerchantLimit());
                }
            } catch (Exception e) {
                LOGGER.error(
                        "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments error in getting merchant extendInfo :{}",
                        e.getMessage());
            }
            if (CollectionUtils.isNotEmpty(fpoDisablePaymodesOnMerchantList)
                    && !fpoDisablePaymodesOnMerchantList.contains(merchantLimitType)) {
                return;
            }
            List<PaymentMode> disablePaymentModeList = nativeCashierInfoRequestBody.getDisablePaymentMode();
            if (CollectionUtils.isEmpty(disablePaymentModeList)) {
                disablePaymentModeList = new ArrayList<PaymentMode>();
            }
            for (String fpoDisablePayMode : fpoDisablePaymodesList) {
                PaymentMode paymentMode = new PaymentMode();
                paymentMode.setMode(EPayMethod.getPayMethodByMethod(fpoDisablePayMode.toUpperCase()).getMethod());
                String paymodeChannels = ConfigurationUtil.getProperty(FPO_DISBALE_PAYMODE_CHANNEL_PREFIX
                        + fpoDisablePayMode + FPO_DISBALE_PAYMODE_CHANNEL_SUFFIX);
                List<String> paymodeChannelsList = StringUtils.isNoneBlank(paymodeChannels) ? Arrays
                        .asList(paymodeChannels.toUpperCase().split(",")) : null;
                if (CollectionUtils.isNotEmpty(paymodeChannelsList)) {
                    LOGGER.info(
                            "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments | paymodeChannels = {}",
                            paymodeChannelsList);
                    paymentMode.setChannels(paymodeChannelsList);
                }
                disablePaymentModeList.add(paymentMode);
            }
            nativeCashierInfoRequestBody.setDisablePaymentMode(disablePaymentModeList);
        }
    }

    private void disablePaymodesForLinkPayments(NativeCashierInfoRequestBody nativeCashierInfoRequestBody,
            PaymentRequestBean paymentRequestBean) {
        try {
            List<FPODisablePaymentMode> fpoDisablePaymentModeList = FPODisableLinkPaymodesUtils.fpoDisablePaymentModeList;
            if (paymentRequestBean != null
                    && !merchantPreferenceService.isBlockLinkFPODisablePaymode(paymentRequestBean.getMid(), false)
                    && CollectionUtils.isNotEmpty(fpoDisablePaymentModeList)
                    && (paymentRequestBean.getLinkDetailsData() != null
                            || StringUtils.isNotBlank(paymentRequestBean.getLinkId()) || StringUtils
                                .isNotBlank(paymentRequestBean.getInvoiceId()))) {
                MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
                String merchantLimitType = null;

                merchantExtendedInfoResponse = MappingServiceClient
                        .getMerchantExtendedInfo(paymentRequestBean.getMid());
                if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                        && merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit() != null) {
                    merchantLimitType = String.valueOf(merchantExtendedInfoResponse.getExtendedInfo()
                            .getMerchantLimit());
                }
                String merchantType = getMerchantSolutionType(paymentRequestBean.getMid());
                String finalMerchantLimitType = merchantLimitType;
                String finalMerchantType = merchantType;

                List<FPODisablePaymentMode> filteredLinkFPODisablePaymentModeList = fpoDisablePaymentModeList
                        .stream()
                        .filter(fpoDisablePaymentMode -> StringUtils.isNotBlank(fpoDisablePaymentMode.getPaymode())
                                && CollectionUtils.isNotEmpty(fpoDisablePaymentMode.getMerchantLimitType())
                                && fpoDisablePaymentMode.getMerchantLimitType().contains(finalMerchantLimitType)
                                && StringUtils.isNotBlank(fpoDisablePaymentMode.getMerchantType())
                                && fpoDisablePaymentMode.getMerchantType().equalsIgnoreCase(finalMerchantType))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(filteredLinkFPODisablePaymentModeList)) {
                    List<PaymentMode> disablePaymentModeList = nativeCashierInfoRequestBody.getDisablePaymentMode();
                    if (CollectionUtils.isEmpty(disablePaymentModeList)) {
                        disablePaymentModeList = new ArrayList<PaymentMode>();
                    }
                    for (FPODisablePaymentMode fpoDisablePayMode : filteredLinkFPODisablePaymentModeList) {
                        PaymentMode paymentMode = null;
                        if (disablePaymentModeList.size() > 0) {
                            List<PaymentMode> filteredPaymentModeList = disablePaymentModeList
                                    .stream()
                                    .filter(myPaymentMode -> myPaymentMode.getMode().equalsIgnoreCase(
                                            fpoDisablePayMode.getPaymode())).collect(Collectors.toList());
                            if (filteredPaymentModeList != null && filteredPaymentModeList.size() > 0) {
                                paymentMode = filteredPaymentModeList.get(0);
                                disablePaymentModeList.remove(paymentMode);
                            }
                        }
                        if (paymentMode == null) {
                            paymentMode = new PaymentMode();
                        }
                        paymentMode
                                .setMode(EPayMethod.getPayMethodByMethod(fpoDisablePayMode.getPaymode()).getMethod());
                        if (CollectionUtils.isNotEmpty(fpoDisablePayMode.getPaymodeChannels())) {
                            if (CollectionUtils.isEmpty(paymentMode.getChannels())) {
                                paymentMode.setChannels(new ArrayList<>(fpoDisablePayMode.getPaymodeChannels()));
                            } else {
                                paymentMode.getChannels().addAll(fpoDisablePayMode.getPaymodeChannels());
                            }
                        }
                        disablePaymentModeList.add(paymentMode);
                    }
                    LOGGER.debug(
                            "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments | disablePaymentModeList = {}",
                            disablePaymentModeList);
                    nativeCashierInfoRequestBody.setDisablePaymentMode(disablePaymentModeList);
                }
            }
        } catch (Exception e) {
            LOGGER.error(
                    "NativePayviewConsultRequestProcessor.disableUPICollectAndIntentForLinkPayments error in getting merchant extendInfo :{}",
                    e);
        }
    }

    private String getMerchantSolutionType(String mid) {
        String merchantAttributeResponse = null;
        Map<String, Object> merchantAttributeResponseMap;
        String merchantType = null;

        try {
            merchantAttributeResponse = merchantQueryService.getMerchantAttribute(mid, "merchantPreference",
                    MerchantUserRequestType.PAYTM.getValue());
            EXT_LOGGER.customInfo("Mapping response - MerchantAttributeResponse :: {}", merchantAttributeResponse);
            if (merchantAttributeResponse != null) {
                merchantAttributeResponseMap = JsonMapper.mapJsonToObject(merchantAttributeResponse, Map.class);
                if (merchantAttributeResponseMap != null) {
                    if (merchantAttributeResponseMap.get("merchantSolutionType") != null) {
                        merchantType = (String) merchantAttributeResponseMap.get("merchantSolutionType");
                    }
                    if (merchantType == null) {
                        merchantType = OFFLINE_MERCHANT;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("error while fetching merchant profile info for mid :{} ,error {}", mid, e);
        }
        return merchantType;
    }

    private void setLinkDetailsInFPO(NativeCashierInfoRequest nativeCashierInfoRequest,
            CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponse response,
            PaymentRequestBean paymentRequestBean) {
        if (cashierInfoRequest != null
                && cashierInfoRequest.getBody() != null
                && (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(
                        cashierInfoRequest.getBody().getRequestType()) || ERequestType.LINK_BASED_PAYMENT.getType()
                        .equalsIgnoreCase(cashierInfoRequest.getBody().getRequestType()))) {
            try {
                if (nativeCashierInfoRequest != null && nativeCashierInfoRequest.getHead() != null
                        && nativeCashierInfoRequest.getHead().getTxnToken() != null) {
                    String txnToken = nativeCashierInfoRequest.getHead().getTxnToken();
                    NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
                    LinkDetailResponseBody linkDetailCachedResponse = null;
                    if (paymentRequestBean != null) {
                        linkDetailCachedResponse = linkPaymentUtil.getLinkDetailCachedResponse(paymentRequestBean);
                    }
                    if (nativeInitiateRequest != null
                            && nativeInitiateRequest.getInitiateTxnReq() != null
                            && nativeInitiateRequest.getInitiateTxnReq().getBody() != null
                            && (nativeInitiateRequest.getInitiateTxnReq().getBody().getLinkDetailsData() != null || linkDetailCachedResponse != null)) {
                        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq()
                                .getBody();
                        LinkDetailResponseBody linkDetailResponseBody = orderDetail.getLinkDetailsData() != null ? orderDetail
                                .getLinkDetailsData() : linkDetailCachedResponse;
                        LinkAppPushData linkPushData;
                        boolean isSkipLoginFlow = false;

                        if (linkDetailResponseBody.getPaymentFormDetails() != null
                                && Boolean.TRUE.equals(linkDetailResponseBody.getPaymentFormDetails()
                                        .getSkipLoginEnabled())) {
                            isSkipLoginFlow = true;
                        }
                        String linkType = null;
                        if (cashierInfoRequest.getBody() != null
                                && ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(
                                        cashierInfoRequest.getBody().getRequestType())) {
                            linkType = TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE;
                        } else if (cashierInfoRequest.getBody() != null
                                && ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(
                                        cashierInfoRequest.getBody().getRequestType())) {
                            linkType = TheiaConstant.LinkRiskParams.LINK_TYPE_NON_INVOICE;
                        }

                        if (isSkipLoginFlow) {
                            String linkId = linkDetailResponseBody.getLinkId();
                            String redirectUrl = ConfigurationUtil.getProperty("link.service.base.url")
                                    + "/ptcRedirect/" + linkId;
                            linkPushData = new LinkAppPushData(
                                    ProcessTransactionHelper.replaceApostrophe(linkDetailResponseBody
                                            .getLinkDescription()), redirectUrl, linkType);
                        } else {
                            linkPushData = new LinkAppPushData(
                                    ProcessTransactionHelper.replaceApostrophe(linkDetailResponseBody
                                            .getLinkDescription()), linkDetailResponseBody.getLongUrl()
                                            + TheiaConstant.LinkBasedParams.LINK_INVOICE_CHANGE_NUMBER, linkType);
                            if (StringUtils.isNotBlank(orderDetail.getAccountNumber())) {
                                linkPushData.setAccountNumber(MaskingUtil.getMaskedBankAccountNumber(
                                        orderDetail.getAccountNumber(), 0, 4));
                            }
                        }
                        linkPushData.setDisplayWarningMessage(linkDetailResponseBody.isDisplayWarningMessage());
                        if (nativeInitiateRequest.getInitiateTxnReq().getBody().getMid() != null) {
                            Boolean disableLocationPopUp = setLocationPopUpFlag(nativeInitiateRequest
                                    .getInitiateTxnReq().getBody().getMid());
                            linkPushData.setLocationPopUpDisabled(disableLocationPopUp);
                        }

                        response.getBody().setLink(linkPushData);

                        if (StringUtils.isNotEmpty(linkDetailResponseBody.getLinkId())) {
                            nativeSessionUtil.setLinkId(nativeCashierInfoRequest.getHead().getTxnToken(),
                                    linkDetailResponseBody.getLinkId());
                        }

                        if (StringUtils.isNotEmpty(linkDetailResponseBody.getInvoiceId())) {
                            nativeSessionUtil.setInvoiceId(nativeCashierInfoRequest.getHead().getTxnToken(),
                                    linkDetailResponseBody.getInvoiceId());
                        }

                        linkPushData.setLinkId(linkDetailResponseBody.getLinkId() != null ? linkDetailResponseBody
                                .getLinkId() : paymentRequestBean != null ? paymentRequestBean.getLinkId() : null);
                        linkPushData.setLinkName(linkDetailResponseBody.getLinkName() != null ? linkDetailResponseBody
                                .getLinkName() : paymentRequestBean != null ? paymentRequestBean.getLinkName() : null);

                        response.getBody().getMerchantDetails().setAutoAppInvokeAllowed(false);
                        if (nativeInitiateRequest.getInitiateTxnReq().getBody().getMid() != null
                                && response.getBody() != null && response.getBody().getMerchantDetails() != null) {
                            LOGGER.info("Setting merchant info in redis for Link based payment for MID = {}",
                                    nativeInitiateRequest.getInitiateTxnReq().getBody().getMid());
                            LinkBasedMerchantInfo linkBasedMerchantInfo = new LinkBasedMerchantInfo(
                                    nativeInitiateRequest.getInitiateTxnReq().getBody().getMid(), response.getBody()
                                            .getMerchantDetails().getMerchantName(), response.getBody()
                                            .getMerchantDetails().getMerchantLogo());
                            theiaTransactionalRedisUtil.set(
                                    LINK_BASED_KEY
                                            + nativeSessionUtil.getTxnId(nativeCashierInfoRequest.getHead()
                                                    .getTxnToken()), linkBasedMerchantInfo);
                        }

                        String mid = nativeInitiateRequest.getInitiateTxnReq().getBody().getMid();
                        String orderid = nativeInitiateRequest.getInitiateTxnReq().getBody().getOrderId();
                        linkPaymentUtil.setCacheKeyNameForLinkDetail(linkDetailResponseBody, mid, orderid);
                        MerchantDetails merchantDetails = response.getBody().getMerchantDetails();
                        if (merchantDetails != null && merchantDetails.getIsAppInvokeAllowed()
                                && linkDetailResponseBody.isDisableAppInvokeCTA()) {
                            merchantDetails.setIsAppInvokeAllowed(false);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Exception in NativePayviewConsultRequestProcessor.setLinkDetailsInFPO()", e);
            }
        }
    }

    private Boolean setLocationPopUpFlag(String mid) {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
        String merchantLimitType = null;
        Boolean disableLocationPopUp = false;

        String disableLocationPopUpForMerchantType = ConfigurationUtil
                .getProperty(TheiaConstant.ExtraConstants.FPO_DISABLE_LOCATION_LINKS_MERCHANT_LIMIT);
        List<String> disableLocationPopUpForMerchantTypeList = StringUtils
                .isNotBlank(disableLocationPopUpForMerchantType) ? Arrays.asList(disableLocationPopUpForMerchantType
                .split(",")) : null;
        if (CollectionUtils.isNotEmpty(disableLocationPopUpForMerchantTypeList)) {
            try {
                merchantExtendedInfoResponse = MappingServiceClient.getMerchantExtendedInfo(mid);
                if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                        && merchantExtendedInfoResponse.getExtendedInfo().getMerchantLimit() != null) {
                    merchantLimitType = String.valueOf(merchantExtendedInfoResponse.getExtendedInfo()
                            .getMerchantLimit());
                }
                if (merchantLimitType != null && disableLocationPopUpForMerchantTypeList.contains(merchantLimitType)) {
                    disableLocationPopUp = true;
                }
            } catch (Exception e) {
                LOGGER.error("Exception in NativePayViewConsultRequestProcessor.showLocationPopUp():: {}", e);
            }
        }
        return disableLocationPopUp;
    }

    private void setPostpaidOnlyPrefs(CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponse response) {
        LOGGER.info("Setting postpaidOnlyUserMessage and postpaidOnlyMerchant flag");
        try {
            if (merchantPreferenceService.isPostpaidOnlyMerchant(cashierInfoRequest.getHead().getMid(), false)) {
                if (response.getBody().getLoginInfo() != null && response.getBody().getLoginInfo().isUserLoggedIn()) {
                    response.getBody().setPostpaidOnlyMerchant(BOOLEAN_TRUE);
                    String userMsg = ConfigurationUtil
                            .getProperty(TheiaConstant.ExtraConstants.POSTPAID_ONLY_MSG_AFTER_LOGIN);

                    List<PayMethod> payMethodsList = response.getBody().getMerchantPayOption().getPayMethods();
                    if (CollectionUtils.isEmpty(payMethodsList)) {
                        response.getBody().setPostpaidOnlyUserMessage(userMsg);
                    } else {
                        boolean postpaidEnabledForUser = payMethodsList.stream().anyMatch(
                                x -> (PAYTM_DIGITAL_CREDIT.equalsIgnoreCase(x.getPayMethod())
                                        && x.getIsDisabled() != null && FALSE.equalsIgnoreCase(x.getIsDisabled()
                                        .getStatus())));
                        if (!postpaidEnabledForUser) {
                            response.getBody().setPostpaidOnlyUserMessage(userMsg);
                            payMethodsList.removeIf(payMethod -> "BALANCE".equalsIgnoreCase(payMethod.getPayMethod()));
                            response.getBody().getMerchantPayOption().setPayMethods(payMethodsList);
                        } else {
                            response.getBody().setPostpaidOnlyMerchant(BOOLEAN_FALSE);
                        }
                    }
                } else if (response.getBody().getLoginInfo() != null
                        && !response.getBody().getLoginInfo().isUserLoggedIn()) {
                    String userMsg = ConfigurationUtil
                            .getProperty(TheiaConstant.ExtraConstants.POSTPAID_ONLY_MSG_BEFORE_LOGIN);
                    response.getBody().setPostpaidOnlyMerchant(BOOLEAN_TRUE);
                    response.getBody().setPostpaidOnlyUserMessage(userMsg);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Error setting postpaidOnlyUserMessage & postpaidOnlyMerchant flag in FPO: {}", e);
        }
    }

    private void filterPayModesForEdcEmiLinkPayment(InitiateTransactionRequestBody orderDetail,
            NativeCashierInfoRequest nativeCashierInfoRequest) {
        if (orderDetail != null && orderDetail.getLinkDetailsData() != null
                && orderDetail.getLinkDetailsData().getEdcEmiFields() != null) {
            final EdcEmiDetails edcEmiFields = orderDetail.getLinkDetailsData().getEdcEmiFields();

            boolean isEmiPayment = ZERO.equals(edcEmiFields.getEmiChannelDetail().getEmiMonths()) ? false : true;
            List<PaymentMode> enablePaymentModes = new ArrayList<>();
            PaymentMode paymentMode = new PaymentMode();
            List<String> banks = new ArrayList<>();
            if (isEmiPayment) {
                paymentMode.setMode(EPayMethod.EMI.toString());
                paymentMode.setEmiType(edcEmiFields.getCardType());
            } else {
                paymentMode.setMode(edcEmiFields.getCardType());
            }
            banks.add(edcEmiFields.getBankCode());
            paymentMode.setBanks(banks);
            enablePaymentModes.add(paymentMode);
            nativeCashierInfoRequest.getBody().setEnablePaymentMode(enablePaymentModes);
        }
    }

    private void populateEdcEmiLinkPaymentData(NativeCashierInfoRequest nativeCashierInfoRequest,
            CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponse response) {

        try {
            if (cashierInfoRequest != null
                    && cashierInfoRequest.getBody() != null
                    && (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(
                            cashierInfoRequest.getBody().getRequestType()) || ERequestType.LINK_BASED_PAYMENT.getType()
                            .equalsIgnoreCase(cashierInfoRequest.getBody().getRequestType()))) {

                if (nativeCashierInfoRequest != null && nativeCashierInfoRequest.getHead() != null
                        && nativeCashierInfoRequest.getHead().getTxnToken() != null) {
                    String txnToken = nativeCashierInfoRequest.getHead().getTxnToken();
                    NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
                    if (nativeInitiateRequest != null
                            && nativeInitiateRequest.getInitiateTxnReq() != null
                            && nativeInitiateRequest.getInitiateTxnReq().getBody() != null
                            && nativeInitiateRequest.getInitiateTxnReq().getBody().getLinkDetailsData() != null
                            && nativeInitiateRequest.getInitiateTxnReq().getBody().getLinkDetailsData()
                                    .getEdcEmiFields() != null) {

                        LinkAppPushData linkAppPushData = null;
                        EdcEmiLinkPaymentDetails edcEmiLinkPaymentDetails = new EdcEmiLinkPaymentDetails();
                        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq()
                                .getBody();
                        final EdcEmiDetails edcEmiFields = orderDetail.getLinkDetailsData().getEdcEmiFields();
                        if (response.getBody().getLink() == null)
                            linkAppPushData = new LinkAppPushData();
                        else
                            linkAppPushData = response.getBody().getLink();

                        edcEmiLinkPaymentDetails.setBankCode(edcEmiFields.getBankCode());
                        edcEmiLinkPaymentDetails.setBankName(edcEmiFields.getBankName());
                        edcEmiLinkPaymentDetails.setCardType(edcEmiFields.getCardType());
                        if (edcEmiFields.getEmiChannelDetail() != null) {

                            edcEmiLinkPaymentDetails.setEmiAmount(edcEmiFields.getEmiChannelDetail().getEmiAmount()
                                    .getValue());
                            edcEmiLinkPaymentDetails.setEmiInterestRate(edcEmiFields.getEmiChannelDetail()
                                    .getInterestRate());
                            edcEmiLinkPaymentDetails.setEmiMonths(edcEmiFields.getEmiChannelDetail().getEmiMonths());
                            edcEmiLinkPaymentDetails.setEmiTotalAmount(edcEmiFields.getEmiChannelDetail()
                                    .getTotalAmount().getValue());
                            edcEmiLinkPaymentDetails.setPgPlanId(edcEmiFields.getEmiChannelDetail().getPgPlanId());
                            if (edcEmiFields.getEmiChannelDetail().getOfferDetails() != null) {
                                Optional<EdcEmiOfferDetails> offer = edcEmiFields.getEmiChannelDetail()
                                        .getOfferDetails().stream()
                                        .filter(p -> p.getType().equalsIgnoreCase(PROMOCODE_TYPE_CASHBACK)).findFirst();
                                if (offer.isPresent())
                                    edcEmiLinkPaymentDetails.setEmiCashback(offer.get().getAmount().getValue());
                            }
                            if (edcEmiFields.getEmiChannelDetail().getBankOfferDetails() != null) {
                                Optional<EdcEmiBankOfferDetails> bankOfferDetails = edcEmiFields.getEmiChannelDetail()
                                        .getBankOfferDetails().stream()
                                        .filter(p -> p.getType().equalsIgnoreCase(PROMOCODE_TYPE_CASHBACK)).findFirst();
                                if (bankOfferDetails.isPresent())
                                    edcEmiLinkPaymentDetails.setAdditionalCashback(bankOfferDetails.get().getAmount()
                                            .getValue());
                            }
                        }

                        linkAppPushData.setEdcEmiLinkPaymentDetails(edcEmiLinkPaymentDetails);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NativePayviewConsultRequestProcessor.populateEdcEmiLinkPaymentData() {}", e);
        }
    }

    private void disableAppInvokeForCTA(NativeCashierInfoRequest nativeCashierInfoRequest,
            CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponse response,
            PaymentRequestBean paymentRequestBean) {
        if (cashierInfoRequest != null && cashierInfoRequest.getBody() != null
                && (ERequestType.NATIVE_MF.getType().equalsIgnoreCase(cashierInfoRequest.getBody().getRequestType()))) {
            try {
                if (nativeCashierInfoRequest != null && nativeCashierInfoRequest.getHead() != null
                        && nativeCashierInfoRequest.getHead().getTxnToken() != null) {
                    String txnToken = nativeCashierInfoRequest.getHead().getTxnToken();
                    NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
                    LinkDetailResponseBody linkDetailCachedResponse = null;
                    if (paymentRequestBean != null) {
                        linkDetailCachedResponse = linkPaymentUtil.getLinkDetailCachedResponse(paymentRequestBean);
                    }
                    if (nativeInitiateRequest != null
                            && nativeInitiateRequest.getInitiateTxnReq() != null
                            && nativeInitiateRequest.getInitiateTxnReq().getBody() != null
                            && (nativeInitiateRequest.getInitiateTxnReq().getBody().getLinkDetailsData() != null || linkDetailCachedResponse != null)) {
                        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq()
                                .getBody();
                        LinkDetailResponseBody linkDetailResponseBody = orderDetail.getLinkDetailsData() != null ? orderDetail
                                .getLinkDetailsData() : linkDetailCachedResponse;
                        MerchantDetails merchantDetails = response.getBody().getMerchantDetails();
                        if (merchantDetails != null && merchantDetails.getIsAppInvokeAllowed()
                                && linkDetailResponseBody.isDisableAppInvokeCTA()) {
                            merchantDetails.setIsAppInvokeAllowed(false);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Exception in NativePayviewConsultRequestProcessor.disableAppInvokeForCTA()", e);
            }
        }
    }

    private void setWalletAccountStatus(NativeCashierInfoResponseBody nativeCashierInfoResponseBody,
            WorkFlowResponseBean workFlowResponseBean, NativeCashierInfoRequest nativeCashierInfoRequest) {
        if (nativeCashierInfoResponseBody != null && nativeCashierInfoResponseBody.getMerchantPayOption() != null
                && CollectionUtils.isNotEmpty(nativeCashierInfoResponseBody.getMerchantPayOption().getPayMethods())) {
            ActiveInfo activeInfo = new ActiveInfo();
            List<PayMethod> payMethodList = nativeCashierInfoResponseBody.getMerchantPayOption().getPayMethods();
            for (PayMethod payMethod : payMethodList) {
                if (EPayMethod.BALANCE.getMethod().equalsIgnoreCase(payMethod.getPayMethod())) {
                    if (workFlowResponseBean.isWalletInactive()) {
                        activeInfo.setStatus(false);
                        activeInfo.setMsg(ConfigurationUtil.getProperty("wallet.account.inactive.message"));
                        if (workFlowResponseBean.isWalletNewUser()) {
                            activeInfo.setNewWalletUser(true);
                            activeInfo.setMsg(ConfigurationUtil.getProperty("wallet.new.user.blocked.message"));
                            if (nativeCashierInfoRequest.getBody().isOfflineFlow()
                                    || workFlowResponseBean.isOfflineFlow()) {
                                payMethod.getIsDisabled().setStatus(Boolean.TRUE.toString());
                            }
                        }

                        payMethod.setIsActive(activeInfo);
                        LOGGER.info("Wallet Account Inactive : {}", activeInfo);
                    }
                    String txnAmount = AmountUtils
                            .getTransactionAmountInRupee(StringUtils.isNotBlank(workFlowResponseBean
                                    .getWorkFlowRequestBean().getTxnAmount()) ? workFlowResponseBean
                                    .getWorkFlowRequestBean().getTxnAmount() : workFlowResponseBean
                                    .getWorkFlowRequestBean().getAmountForWalletConsultInRisk());
                    String walletAmount = null;
                    if (payMethod != null && payMethod.getPayChannelOptions() != null
                            && !payMethod.getPayChannelOptions().isEmpty()) {
                        AccountInfo balanceInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0))
                                .getBalanceInfo();
                        if (balanceInfo != null && balanceInfo.getAccountBalance() != null
                                && !StringUtils.isEmpty(balanceInfo.getAccountBalance().getValue())) {
                            walletAmount = balanceInfo.getAccountBalance().getValue();
                        }
                    }

                    if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails() != null
                            && StringUtils.equals(nativeCashierInfoResponseBody.getPaymentFlow().getValue(),
                                    EPayMode.ADDANDPAY.getValue())) {
                        if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 1) {

                            if (StringUtils.isBlank(txnAmount)
                                    || (StringUtils.isNotBlank(txnAmount) && StringUtils.isNotBlank(walletAmount) && Double
                                            .parseDouble(txnAmount) > Double.parseDouble(walletAmount))) {
                                activeInfo.setStatus(Boolean.FALSE);
                                activeInfo.setMsg(ConfigurationUtil
                                        .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                                activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                        .getUserWalletFreezeDetails());
                                payMethod.setIsActive(activeInfo);
                            }
                        } else if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 2) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        } else if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 3) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        }
                    }

                    if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails() != null
                            && StringUtils.equals(nativeCashierInfoResponseBody.getPaymentFlow().getValue(),
                                    EPayMode.HYBRID.getValue())) {
                        if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 2) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        } else if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 3) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        }
                    }

                    if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails() != null
                            && StringUtils.equals(nativeCashierInfoResponseBody.getPaymentFlow().getValue(),
                                    EPayMode.NONE.getValue())) {
                        if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 1) {
                            if (StringUtils.isBlank(txnAmount)
                                    || (StringUtils.isNotBlank(txnAmount) && StringUtils.isNotBlank(walletAmount) && Double
                                            .parseDouble(txnAmount) > Double.parseDouble(walletAmount))) {
                                activeInfo.setStatus(Boolean.FALSE);
                                activeInfo.setMsg(ConfigurationUtil
                                        .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                                activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                        .getUserWalletFreezeDetails());
                                payMethod.setIsActive(activeInfo);
                            }
                        } else if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 2) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        } else if (workFlowResponseBean.getWorkFlowRequestBean().getUserWalletFreezeDetails()
                                .getFreezeStatus() == 3) {
                            activeInfo.setStatus(Boolean.FALSE);
                            activeInfo.setMsg(ConfigurationUtil
                                    .getProperty(WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE));
                            activeInfo.setUserWalletFreezeDetails(workFlowResponseBean.getWorkFlowRequestBean()
                                    .getUserWalletFreezeDetails());
                            payMethod.setIsActive(activeInfo);
                        }
                    }
                    if (workFlowResponseBean.getWalletLimits() != null
                            && EPayMode.NONE.getValue().equals(
                                    nativeCashierInfoResponseBody.getPaymentFlow().getValue())
                            && workFlowResponseBean.getWalletLimits().getIsLimitApplicable()) {
                        activeInfo.setStatus(Boolean.FALSE);
                        activeInfo.setMsg(ConfigurationUtil.getProperty(WALLET_LIMIT_REJECT_MESSAGE,
                                DEFAULT_WALLET_LIMIT_REJECT_MESSAGE));
                        activeInfo.setWalletLimitBreached(Boolean.TRUE);
                        payMethod.setIsActive(activeInfo);
                    }
                }
            }
        }
    }

    private void setPostpaidThresholdAmountAnd2FA(WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse response) {
        if (isPayMethodConfiguredOnMerchant(nativeCashierInfoRequest.getBody().getMid(),
                workFlowResponse.getMerchnatLiteViewResponse(), EPayMethod.PAYTM_DIGITAL_CREDIT)
                && workFlowResponse.getUserDetails() != null && workFlowResponse.getUserDetails().isPaytmCCEnabled()) {
            if (ff4JUtil.isFeatureEnabled(POSTPAID_2FA_ENABLED, nativeCashierInfoRequest.getBody().getMid())
                    || merchantPreferenceService.isThresholdAmount2FAPreferenceEnabled(nativeCashierInfoRequest
                            .getBody().getMid())) {
                response.getBody().setPostpaid2FAThresholdValue(
                        ConfigurationUtil.getProperty(PP_TWO_FACTOR_AUTH_THRESHOLD_VALUE, "1500"));
                response.getBody().setPostpaid2FAEnabled(true);
            }
        }

    }

    private void populateSubscriptionDeepLink(NativeCashierInfoRequest nativeCashierInfoRequest,
            CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponse response) {
        LOGGER.info("Populating a Subscription deep link");
        try {
            QrDetail qrDetail = null;
            String deepLink = null;
            if (nativeCashierInfoRequest.getBody().isDeepLinkRequired()
                    && ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.THEIA_SUBSCRIPTION_QR_ENABLED, false)
                    && merchantPreferenceService.isSubscriptionQrEnabled(nativeCashierInfoRequest.getBody().getMid())) {

                boolean fetchDeepLink = false;
                if (response != null && response.getBody() != null && null != response.getBody().getMerchantPayOption()) {
                    List<PayMethod> payMethodList = response.getBody().getMerchantPayOption().getPayMethods();
                    for (PayMethod payMethod : payMethodList) {
                        if (UPI.equals(payMethod.getPayMethod())) {
                            fetchDeepLink = true;
                            break;
                        }
                    }
                }

                if (fetchDeepLink) {
                    deepLink = nativeSubscriptionHelper.fetchDeepLink(cashierInfoRequest.getBody().getOrderId(),
                            nativeCashierInfoRequest.getBody().getMid());
                    qrDetail = nativeSubscriptionHelper.fetchQrDetails(cashierInfoRequest.getBody().getOrderId(),
                            nativeCashierInfoRequest.getBody().getMid(), deepLink);
                    response.getBody().setQrDetail(qrDetail);
                    response.getBody().setDeepLink(deepLink);
                } else {
                    LOGGER.error("Payment mode UPI is not supported on this mid");
                }

            }
        } catch (Exception e) {
            LOGGER.error("Exception in populateSubscriptionDeepLink:", e);
        }

    }

    private void populateSuperCashData(WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse response) {
        try {

            if (merchantPreferenceService.isBlackListSupercashInlineOffers(nativeCashierInfoRequest.getBody().getMid())) {
                LOGGER.info("Supercash Inline offers are blacklisted for this mid: {}", nativeCashierInfoRequest
                        .getBody().getMid());
                return;
            }

            if (ff4JUtil.isSuperCashEnabledMid(nativeCashierInfoRequest.getBody().getMid())
                    || ff4JUtil.isSuperCashEnabledForOffline(nativeCashierInfoRequest.getBody().getMid())) {

                if (workFlowResponse.getUserDetails() == null
                        || StringUtils.isEmpty(workFlowResponse.getUserDetails().getUserId())) {
                    LOGGER.info("paytmUserId is mandatory, returning from populateSupercashData");
                    return;
                }

                String merchantType = getMerchantSolutionType(nativeCashierInfoRequest.getBody().getMid());
                boolean isMerchantOnPaytm = merchantExtendInfoUtils.isMerchantOnPaytm(nativeCashierInfoRequest
                        .getBody().getMid());

                EXT_LOGGER.customInfo("supercash merchantType :: {}", merchantType);
                EXT_LOGGER.customInfo("supercash isMerchantOnPaytm :: {}", isMerchantOnPaytm);

                String orderAmt = extractOrderAmt(nativeCashierInfoRequest);

                if ((merchantType.equalsIgnoreCase(OFFLINE_MERCHANT) || merchantType
                        .equalsIgnoreCase(PPBL_OFFLINE_MERCHANT)) && !isMerchantOnPaytm) {
                    fetchSuperCashOffersForFPO(nativeCashierInfoRequest, response, workFlowResponse, true, orderAmt);
                } else if (NumberUtils.isNumber(orderAmt) && NumberUtils.createDouble(orderAmt) > 0) {
                    fetchSuperCashOffersForFPO(nativeCashierInfoRequest, response, workFlowResponse, false, orderAmt);
                } else {
                    LOGGER.info("Invalid txn amount :{}, returning from populateSupercashData",
                            nativeCashierInfoRequest.getBody().getOrderAmount());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in populateSupercashData: {}", e);
        }
    }

    private void fetchSuperCashOffersForFPO(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse response, WorkFlowResponseBean workFlowResponseBean,
            boolean merchantSolutionTypeOffline, String orderAmount) {

        if (ff4JUtil.isSuperCashEnabledMid(nativeCashierInfoRequest.getBody().getMid()) && !merchantSolutionTypeOffline) {
            superCashServiceHelper.searchSuperCashOffers(nativeCashierInfoRequest, response, workFlowResponseBean,
                    false, orderAmount);
        } else if (ff4JUtil.isSuperCashEnabledForOffline(nativeCashierInfoRequest.getBody().getMid())
                && merchantSolutionTypeOffline) {
            superCashServiceHelper.searchSuperCashOffers(nativeCashierInfoRequest, response, workFlowResponseBean,
                    true, orderAmount);
        } else {
            LOGGER.error("Supercash feature is off for flow {}", merchantSolutionTypeOffline ? "offline" : "online");
        }
    }

    private String extractOrderAmt(NativeCashierInfoRequest nativeCashierInfoRequest) {
        String amt = null;
        if (nativeCashierInfoRequest != null && nativeCashierInfoRequest.getBody() != null
                && nativeCashierInfoRequest.getHead() != null) {
            amt = nativeCashierInfoRequest.getBody().getOrderAmount();

            if (StringUtils.isEmpty(amt) && nativeCashierInfoRequest.getBody().getqRCodeInfo() != null) {
                if (nativeCashierInfoRequest.getBody().getqRCodeInfo().getTxnAmount() != null
                        && NumberUtils.isNumber(nativeCashierInfoRequest.getBody().getqRCodeInfo().getTxnAmount())) {
                    amt = nativeCashierInfoRequest.getBody().getqRCodeInfo().getTxnAmount();
                }
            }
            if (StringUtils.isEmpty(amt)) {
                String txnToken = null;
                InitiateTransactionRequestBody orderDetails = null;
                if (TokenType.TXN_TOKEN.equals(nativeCashierInfoRequest.getHead().getTokenType())) {
                    txnToken = nativeCashierInfoRequest.getHead().getToken();
                    orderDetails = nativeSessionUtil.getOrderDetail(txnToken);
                } else if (!TokenType.SSO.equals(nativeCashierInfoRequest.getHead().getTokenType())
                        && StringUtils.isEmpty(txnToken)) {
                    orderDetails = StringUtils.isNotBlank(nativeCashierInfoRequest.getHead().getTxnToken()) ? nativeSessionUtil
                            .getOrderDetail(nativeCashierInfoRequest.getHead().getTxnToken()) : null;
                }
                if (orderDetails != null && orderDetails.getTxnAmount() != null) {
                    amt = orderDetails.getTxnAmount().getValue();
                }
            }
        }
        return amt;
    }

    private void addUserInfoInResponse(CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponseBody response) {
        if (response.getUserDetails() == null
                && ff4JUtil
                        .isFeatureEnabled(RETURN_USER_INFO_IN_V2_FPO_RESPONSE, cashierInfoRequest.getHead().getMid())) {
            UserInfo userInfo = cashierInfoRequest.getBody().getUserInfo();
            if (userInfo != null
                    && StringUtils.isNotBlank(userInfo.getMobile())
                    && ff4JUtil.isFeatureEnabled(MASK_MOBILE_ON_CASHIER_PAGE_ENABLED, cashierInfoRequest.getHead()
                            .getMid())) {
                String mobileNo = userInfo.getMobile();
                userInfo.setMobile(mobileMaskHelper.getMaskedNumber(mobileNo));
            }
            response.setUserInfo(userInfo);
        }
    }

    private boolean validateOneClickOnboarding(NativeCashierInfoRequest nativeCashierInfoRequest,
            CashierInfoRequest cashierInfoRequest, UserDetailsBiz userDetailsBiz) {

        if (merchantPreferenceService.isPostpaidEnabledOnMerchant(nativeCashierInfoRequest.getBody().getMid(), false)
                && merchantPreferenceService.isPostpaidOnbordingEnable(nativeCashierInfoRequest.getBody().getMid(),
                        false)) {
            if (StringUtils.isBlank(userDetailsBiz.getPostpaidCreditLimit())) {
                return false;
            }

            try {
                String txnAmount = (cashierInfoRequest.getBody().getOrderAmount() != null) ? cashierInfoRequest
                        .getBody().getOrderAmount().getValue() : nativeCashierInfoRequest.getBody().getOrderAmount();
                if (StringUtils.isBlank(txnAmount)
                        || Double.parseDouble(txnAmount) < Double.parseDouble(userDetailsBiz.getPostpaidCreditLimit())) {
                    EXT_LOGGER.customInfo("validate OneClickOnboarding successful");
                    return true;
                }
            } catch (Exception ex) {
                LOGGER.error("Exception while parsing txnAmount or CreditLimit", ex);
            }
        }
        EXT_LOGGER.customInfo("validate OneClickOnboarding returning failure");
        return false;
    }

    /*
     * function to populate deals object interactes with deal api (if preference
     * enabled on merch.) and appends the response in fetchqrpaymentdetails reqd
     * : mid , qrCodeId , customerId
     */
    private void populateDealsData(WorkFlowResponseBean workFlowResponse,
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse response) {
        try {
            String mId = nativeCashierInfoRequest.getBody().getMid();
            boolean dealEnabled = merchantPreferenceService.isDealsEnabled(mId, false);
            QRCodeInfoResponseData qrCodeInfo = nativeCashierInfoRequest.getBody().getqRCodeInfo();
            boolean fetchDealsFf4j = ff4jUtils.isFeatureEnabled(FF4J_FETCH_DEALS_ENABLED, false);

            // only proceed if pref. enabled and qrcodeinfo available
            if (dealEnabled && fetchDealsFf4j && qrCodeInfo != null) {
                String qrCodeId = qrCodeInfo.getQrCodeId();
                String custId = workFlowResponse.getUserDetails().getUserId();

                if (StringUtils.isBlank(qrCodeId) || StringUtils.isBlank(mId) || StringUtils.isBlank(custId)) {
                    LOGGER.info(
                            "qrcodeId , mid , custid  is mandatory, returning from populateDealsData . Input params - mid :: {} , qrCodeId :: {} , custid :: {} ",
                            mId, qrCodeId, custId);
                    return;
                }
                boolean cbEnabled = ff4jUtils.isFeatureEnabled("theia.circuitBreaker", false);
                LOGGER.info("fetching deals for mid :: {} , qrCodeId :: {} , custid :: {} ", mId, qrCodeId, custId);
                DealsResponse dealsResponse = dealsService.getDeals(mId, qrCodeId, custId, cbEnabled);
                // 0 - error , 1 success
                if (dealsResponse == null || dealsResponse.getStatus() == 0) {
                    LOGGER.error("error occured while fetching deals. deals resp msg :: {} ", dealsResponse);
                    return;
                }
                response.getBody().setDealsResult(dealsResponse.getDealsResult());
                LOGGER.info("deals fetched successfully for mid :: {} , dealsResponse :: {} ", mId, dealsResponse);

            }
        } catch (Exception e) {
            LOGGER.error("Exception in populateDealsData: ", e);
        }
    }
}
