package com.paytm.pgplus.theia.paymentoffer.helper;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.ApplyPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.BulkApplyPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOptionBulk;
import com.paytm.pgplus.facade.paymentpromotion.models.response.*;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoSevice;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.models.PaymentOffer;
import com.paytm.pgplus.models.PaymentOfferDetails;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.enums.PaymentPromoServiceError;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequestBody;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchAllPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import com.paytm.pgplus.theia.paymentoffer.model.response.*;
import com.paytm.pgplus.theia.paymentoffer.requestbuilder.ApplyPromoPaymentOptionBuilder;
import com.paytm.pgplus.theia.paymentoffer.requestbuilder.ApplyPromoPaymentOptionBuilderFactory;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;

@Component("paymentOffersServiceHelper")
public class PaymentOffersServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersServiceHelper.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Autowired
    @Qualifier("paymentPromoService")
    private IPaymentPromoSevice paymentPromoSevice;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardsService;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    public SearchPaymentOffersServiceResponse searchPaymentOffers(FetchAllPaymentOffersRequest request,
            String paytmUserId) {
        return searchPaymentOffers(request.getBody().getMid(), paytmUserId);
    }

    public SearchPaymentOffersServiceResponse searchPaymentOffers(String mid, String paytmUserId) throws BaseException {

        SearchPaymentOffersServiceResponse cachedResponse = null;
        boolean enablePromoUserSegmentation = ff4JUtil.isFeatureEnabled(THEIA_ENABLE_PROMO_USER_SEGMENTATION, mid);
        if (!enablePromoUserSegmentation) {
            cachedResponse = getCachedResponse(mid, SearchPaymentOffersServiceResponse.class);
        }
        if (cachedResponse != null) {
            LOGGER.info("Serving SearchPaymentOffersServiceResponse from cache");
            return cachedResponse;
        }
        try {
            SearchPaymentOffersServiceResponse response = paymentPromoSevice.searchPaymentOffers(prepareQueryParams(
                    mid, paytmUserId, enablePromoUserSegmentation));
            if (isValidSearchPaymentOffersServiceResponse(response) && response.getData() != null) {
                if (!enablePromoUserSegmentation) {
                    cacheResponse(mid, response);
                }
                return response;
            } else {
                String msg = "";
                if (CollectionUtils.isNotEmpty(response.getErrors())) {
                    msg = response.getErrors().get(0).getMessage();
                    LOGGER.error("Error in getting SearchPaymentOffersServiceResponse msg = {}, code = {}", msg,
                            response.getErrors().get(0));
                } else {
                    LOGGER.error("Error in getting SearchPaymentOffersServiceResponse");
                }
                throw BaseException.getException();
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BaseException();
        }
    }

    public boolean isAnyPaymentOfferAvailableOnMerchant(String mid, String paytmUserId) {
        try {
            SearchPaymentOffersServiceResponse searchPaymentOffersServiceResponse = searchPaymentOffers(mid,
                    paytmUserId);
            if (searchPaymentOffersServiceResponse != null
                    && CollectionUtils.isNotEmpty(searchPaymentOffersServiceResponse.getData())) {
                return true;
            }
        } catch (BaseException e) {
            LOGGER.error("Exception in searchPaymentOffers for mid {}", mid);
        }
        return false;
    }

    public boolean isBin8OfferAvailableOnMerchant(String mid, String paytmUserId) {
        try {
            if (StringUtils.isBlank(paytmUserId)
                    && ff4JUtil.isFeatureEnabled(THEIA_ENABLE_PROMO_USER_SEGMENTATION, mid)) {
                return true;
            }
            SearchPaymentOffersServiceResponse searchPaymentOffersServiceResponse = searchPaymentOffers(mid,
                    paytmUserId);
            if (searchPaymentOffersServiceResponse != null
                    && CollectionUtils.isNotEmpty(searchPaymentOffersServiceResponse.getData())) {
                for (SearchPaymentOffersResponseData offersResponseData : searchPaymentOffersServiceResponse.getData()) {
                    if (Boolean.parseBoolean(offersResponseData.getIs8DigitBin())) {
                        return true;
                    }
                }
            }
        } catch (BaseException e) {
            LOGGER.error("Exception in searchPaymentOffers for mid {}", mid);
        }
        return false;
    }

    private void cacheResponse(String mid, SearchPaymentOffersServiceResponse response) {
        String ttl = ConfigurationUtil.getProperty("payment.promo.search.cache.minutes", "15");
        LOGGER.info("Caching SearchPaymentOffersServiceResponse {}, ttl {}", response, ttl);

        theiaSessionRedisUtil.set(PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid, response.getClass()), response,
                Integer.parseInt(ttl) * 60);

        if (!ff4jUtils.isFeatureEnabledOnMid(CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE,
                THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
            LOGGER.info("operation on static redis, {}", CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE);
            theiaTransactionalRedisUtil.set(PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid, response.getClass()),
                    response, Integer.parseInt(ttl) * 60);
        }
    }

    private SearchPaymentOffersServiceResponse getCachedResponse(String mid, Class cachedClass) {

        SearchPaymentOffersServiceResponse cachedResponse = null;
        cachedResponse = (SearchPaymentOffersServiceResponse) theiaSessionRedisUtil.get(PaymentOfferUtils
                .getSearchPaymentOffersCacheKey(mid, cachedClass));

        if (cachedResponse == null) {
            if (!ff4jUtils.isFeatureEnabledOnMid(CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE,
                    THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
                LOGGER.info("operation on static redis, {}", CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE);
                cachedResponse = (SearchPaymentOffersServiceResponse) theiaTransactionalRedisUtil.get(PaymentOfferUtils
                        .getSearchPaymentOffersCacheKey(mid, cachedClass));
            }
        }

        return cachedResponse;
    }

    public ApplyPromoServiceResponse applyPromo(ApplyPromoRequest request) {
        if (isAnyPaymentOfferAvailableOnMerchant(request.getBody().getMid(), request.getBody().getPaytmUserId())) {
            ApplyPromoServiceRequest applyPromoServiceRequest = prepareApplyPromoServiceRequest(request);
            try {
                return paymentPromoSevice.applyPromo(
                        applyPromoServiceRequest,
                        prepareQueryParams(request.getBody().getMid(), request.getBody().getCustId(), null, request
                                .getBody().getPaytmUserId()));
            } catch (FacadeCheckedException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                throw BaseException.getException();
            }
        } else {
            LOGGER.info("No promo exists for merchant = {}", request.getBody().getMid());
            throw BaseException.getException(ResultCode.PAYMENT_PROMO_NOT_EXISTS_ON_MERCHANT);
        }
    }

    public <T extends PromoServiceResponseBase> boolean isPromoServiceSeccessResponse(T response) {
        return response != null && response.getStatus() == 1;
    }

    private ApplyPromoServiceRequest prepareApplyPromoServiceRequest(ApplyPromoRequest request) {
        ApplyPromoServiceRequest applyPromoServiceRequest = new ApplyPromoServiceRequest();
        applyPromoServiceRequest.setChannel(request.getHead().getChannelId().getValue());
        if (request.getBody().getPromocode() != null) {
            applyPromoServiceRequest.setPromocode(request.getBody().getPromocode());
        } else {
            if (request.getBody().getPromoInfo() != null) {
                String promoInfo = request.getBody().getPromoInfo().stream()
                        .filter(promo -> promo != null && StringUtils.isNotBlank(promo.getPromocode()))
                        .map(promo -> String.valueOf(promo.getPromocode())).collect(Collectors.joining(","));
                applyPromoServiceRequest.setPromocode(promoInfo);
            }
        }
        // converting Amount to paise
        applyPromoServiceRequest.setTotalTransactionAmount(PaymentOfferUtils.getAmountInPaise(request.getBody()
                .getTotalTransactionAmount()));
        applyPromoServiceRequest.setPaymentOptions(getPaymentOptions(request.getBody()));
        return applyPromoServiceRequest;
    }

    private List<PaymentOption> getPaymentOptions(ApplyPromoRequestBody requestBody) {
        List<PaymentOption> paymentOptions = new ArrayList<>(requestBody.getPaymentOptions().size());
        String mid = requestBody.getMid();
        Integer payOptionSize = requestBody.getPaymentOptions().size();
        for (PromoPaymentOption promoPaymentOption : requestBody.getPaymentOptions()) {
            // Hybrid is not supported currently, will send only nonwallet part
            // to promoservice
            if (payOptionSize > 1 && PayMethod.BALANCE.equals(promoPaymentOption.getPayMethod())) {
                continue;
            }
            if (isCCDCPaymethod(promoPaymentOption.getPayMethod()) && ff4JUtil.isFeatureEnabledForPromo(mid)) {
                if (StringUtils.isNotEmpty(promoPaymentOption.getSavedCardId())) {
                    processForSavedCardId(promoPaymentOption, requestBody.getMid(), requestBody.getPaytmUserId());
                } else if (StringUtils.isNotEmpty(promoPaymentOption.getCardNo())) {
                    processForCardNumber(promoPaymentOption, mid, requestBody.getPaytmUserId());
                } else {
                    throw RequestValidationException.getException();
                }
            }
            ApplyPromoPaymentOptionBuilder paymentOptionBuilder = ApplyPromoPaymentOptionBuilderFactory
                    .getApplyPromoPaymentOptionBuilder(promoPaymentOption.getPayMethod(),
                            requestBody.isPromoForPCFMerchant());
            paymentOptions.add(paymentOptionBuilder.build(promoPaymentOption, requestBody.getMid()));
            // }
        }
        return paymentOptions;
    }

    /**
     * @param promoPaymentOption
     * @param mid
     * @throws BaseException
     */
    private void processForCardNumber(PromoPaymentOption promoPaymentOption, String mid, String paytmUserId) {
        String cardNo = promoPaymentOption.getCardNo();

        /**
         * Calling CacheCard API to fetch CIN on the basis of plain cardNo.
         */

        String cardIndexNumber = workFlowHelper.getCardIndexNoFromCardNumber(cardNo);
        if (StringUtils.isNotEmpty(cardIndexNumber)) {
            promoPaymentOption.setSavedCardId(cardIndexNumber);
        } else {
            LOGGER.error("Unable to fetch cardIndexNumber");
            throw BaseException.getException();
        }

        /**
         * Calling Platform getBinHash API for 8 bin alias and saving it in
         * cache which is required at checkout promo.
         */

        if (isBin8OfferAvailableOnMerchant(mid, paytmUserId)) {
            setbin8Hash(promoPaymentOption, cardNo, cardIndexNumber);
        }

    }

    /**
     * @param promoPaymentOption
     * @param mid
     */
    private void processForSavedCardId(PromoPaymentOption promoPaymentOption, String mid, String paytmUserId) {
        String eightDigitBinHash = null;
        String savedCardId = promoPaymentOption.getSavedCardId();

        if (savedCardId.length() > 15) {

            /**
             * Fetching 8 bin alias by hitting card center Non sensitive API
             */

            QueryNonSensitiveAssetInfoResponse response = cardCenterHelper
                    .queryNonSensitiveAssetInfo(null, savedCardId);
            if (null != response) {
                eightDigitBinHash = response.getCardInfo().getExtendInfo()
                        .get(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
                nativeSessionUtil.cacheEightDigitBinHash(PaymentOfferUtils.getApplyPromoForCachedKey(savedCardId),
                        TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH, eightDigitBinHash);
                promoPaymentOption.setEightDigitBinHash(eightDigitBinHash);
                promoPaymentOption.setCardNo(response.getCardInfo().getMaskedCardNo().substring(0, 6));
            }
        } else {

            /**
             * Fetching CIN (CardHash) by calling cache card token API + 8 bin
             * alias by calling Platform getBinHash API.
             */

            String cardNumber = setCardNumber(promoPaymentOption);
            String CIN = workFlowHelper.getCardIndexNoFromCardNumber(cardNumber);
            if (StringUtils.isNotEmpty(CIN)) {
                promoPaymentOption.setSavedCardId(CIN);
            } else {
                LOGGER.error("Unable to fetch cardIndexNumber");
                throw BaseException.getException();
            }
            if (isBin8OfferAvailableOnMerchant(mid, paytmUserId)) {
                setbin8Hash(promoPaymentOption, cardNumber, CIN);
            }

        }

    }

    // response.getData() will be empty if no offers exists and that will be
    // valid response
    private boolean isValidSearchPaymentOffersServiceResponse(SearchPaymentOffersServiceResponse response) {
        return !(response == null || (response.getData() == null && CollectionUtils.isEmpty(response.getErrors())));
    }

    public FetchAllPaymentOffersResponse prepareResponse(SearchPaymentOffersServiceResponse promoResp,
            FetchAllPaymentOffersRequest request, String simplifiedPromoCode) {
        FetchAllPaymentOffersResponse apiResponse = new FetchAllPaymentOffersResponse();
        apiResponse.setHead(PaymentOfferUtils.createResponseHeader());
        apiResponse.setBody(new FetchPaymentOffersResponseBody());
        apiResponse.getHead().setRequestId(request.getHead().getRequestId());
        apiResponse.getBody().setPaymentOffers(preparePaymentOffers(promoResp, simplifiedPromoCode));
        return apiResponse;
    }

    public List<PaymentOffersData> preparePaymentOffers(SearchPaymentOffersServiceResponse promoResp,
            String simplifiedPromoCode) {
        if (promoResp == null || CollectionUtils.isEmpty(promoResp.getData())) {
            return Collections.emptyList();
        }

        List<SearchPaymentOffersResponseData> searchPaymentOffersResponseData;

        if (StringUtils.isNotBlank(simplifiedPromoCode)) {
            searchPaymentOffersResponseData = promoResp.getData().parallelStream().filter(Objects::nonNull)
                    .filter(p -> (p.getPromocode().equalsIgnoreCase(simplifiedPromoCode))).collect(Collectors.toList());
        } else {
            searchPaymentOffersResponseData = promoResp.getData();
        }
        List<PaymentOffersData> paymentOffersData = new ArrayList<>(searchPaymentOffersResponseData.size());

        for (SearchPaymentOffersResponseData offersResponseData : searchPaymentOffersResponseData) {
            PaymentOffersData data = new PaymentOffersData();
            data.setPromocode(offersResponseData.getPromocode());
            data.setOffer(offersResponseData.getOffer());
            data.setIsPromoVisible(offersResponseData.getIsPromoVisible());
            data.setValidFrom(PaymentOfferUtils.dateStringToMillis(offersResponseData.getValidFrom(), DATE_FORMAT));
            data.setValidUpto(PaymentOfferUtils.dateStringToMillis(offersResponseData.getValidUpto(), DATE_FORMAT));
            data.setTermsTitle(offersResponseData.getTermsTitle());
            data.setTermsUrl(offersResponseData.getTerms());
            paymentOffersData.add(data);
        }
        return paymentOffersData;
    }

    private Map<String, String> prepareQueryParams(String mid, String custId, String orderId) {
        return prepareQueryParams(mid, custId, orderId, null);
    }

    private Map<String, String> prepareQueryParams(String mid, String paytmUserId, boolean enablePromoUserSegmentation) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("merchant-id", mid);
        if (paytmUserId != null && enablePromoUserSegmentation) {
            queryParams.put("paytm-user-id", paytmUserId);
        }
        return queryParams;
    }

    public ApplyPromoResponse prepareResponse(PromoServiceResponseBase baseResponse, ApplyPromoRequest request,
            String version) {
        ApplyPromoServiceResponse serviceResponse = (ApplyPromoServiceResponse) baseResponse;
        ApplyPromoResponse apiResponse = new ApplyPromoResponse();
        apiResponse.setHead(PaymentOfferUtils.createResponseHeader());
        apiResponse.setBody(new ApplyPromoResponseBody());
        apiResponse.getHead().setRequestId(request.getHead().getRequestId());
        if (isPromoServiceSeccessResponse(serviceResponse)) {
            if (serviceResponse.getData() != null) {
                if (com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2.equalsIgnoreCase(version)) {
                    apiResponse.getHead().setVersion(
                            com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2);
                    if (serviceResponse.getData().getStatus() == 1) {
                        // LOGGER.info("promo apply/validation successful response received for version v2");
                        apiResponse.getBody().setPaymentOffer(getPaymentOffer(serviceResponse, request.getBody()));
                    } else {
                        LOGGER.error("promo apply/validation failed :{}", serviceResponse.getData().getPromotext());
                        throw BaseException.getException(serviceResponse.getData().getPromotext());
                    }
                } else {
                    LOGGER.info("promo apply/validation successful response received for version v1");
                    apiResponse.getBody().setPaymentOffer(getPaymentOffer(serviceResponse, request.getBody()));
                }
            } else {
                // TODO ask if null check needs to be added for error object
                LOGGER.error("promo apply/validation invalid response, status is 1 but data is null");
                throw BaseException.getException(serviceResponse.getError().getMessage());
            }
        } else {
            LOGGER.error("promo service failure response");
            Optional<PaymentPromoServiceError> paymentPromoServiceError = PaymentPromoServiceError
                    .fromString(serviceResponse.getError().getCode());
            if (paymentPromoServiceError.isPresent()) {
                throw BaseException.getException(paymentPromoServiceError.get().getResultCode());
            } else {
                throw BaseException.getException(serviceResponse.getError().getMessage());
            }
        }
        return apiResponse;
    }

    private PaymentOffer getPaymentOffer(ApplyPromoServiceResponse serviceResponse, ApplyPromoRequestBody requestBody) {
        PaymentOffer paymentOffer = new PaymentOffer();
        paymentOffer.setOfferBreakup(getPromoOfferDetails(serviceResponse.getData(), requestBody.getPaymentOptions()));
        // Hybrid is not supported currently on promoservice, total will be same
        paymentOffer.setTotalCashbackAmount(paymentOffer.getOfferBreakup().get(0).getCashbackAmount());
        paymentOffer.setTotalInstantDiscount(paymentOffer.getOfferBreakup().get(0).getInstantDiscount());
        paymentOffer.setTotalTransactionAmount(requestBody.getTotalTransactionAmount());
        paymentOffer.setTotalPaytmCashbackAmount(paymentOffer.getOfferBreakup().get(0).getPaytmCashbackAmount());
        if (ff4JUtil.isFeatureEnabled(APPLY_PROMO_SEND_RESPONSE_TNC_URL, requestBody.getMid())) {
            paymentOffer.setTncUrl(serviceResponse.getData().getTncUrl());
        }
        return paymentOffer;
    }

    // Hybrid is not supported currently, promo will only provide nonwallet
    // payment offer details
    private List<PaymentOfferDetails> getPromoOfferDetails(ApplyPromoResponseData responseData,
            List<PromoPaymentOption> paymentOptions) {
        List<PaymentOfferDetails> toRet = new ArrayList<>();
        PaymentOfferDetails paymentOfferDetails = new PaymentOfferDetails();
        paymentOfferDetails.setPromotext(responseData.getPromotext());
        paymentOfferDetails.setPromocodeApplied(responseData.getPromocode());
        paymentOfferDetails.setCashbackAmount(getSavingAmount(responseData.getSavings(), RedemptionType.CASHBACK));
        paymentOfferDetails.setInstantDiscount(getSavingAmount(responseData.getSavings(), RedemptionType.DISCOUNT));
        paymentOfferDetails.setPayMethod(getNonWalletPayMethod(paymentOptions));
        paymentOfferDetails.setPromoVisibility(Boolean.valueOf(responseData.getPromoVisibility()).toString());
        paymentOfferDetails.setResponseCode(responseData.getResponseCode());
        paymentOfferDetails.setTransactionAmount(getPayModeSpecificTransactionAmount(paymentOptions));
        paymentOfferDetails.setPaytmCashbackAmount(getSavingAmount(responseData.getSavings(),
                RedemptionType.PAYTM_CASHBACK));
        toRet.add(paymentOfferDetails);
        return toRet;
    }

    private String getPayModeSpecificTransactionAmount(List<PromoPaymentOption> paymentOptions) {
        if (CollectionUtils.isEmpty(paymentOptions))
            return null;
        Integer paymentOptionsSize = paymentOptions.size();
        if (paymentOptionsSize == 1) {
            return paymentOptions.get(0).getTransactionAmount();
        }
        for (PromoPaymentOption promoPaymentOption : paymentOptions) {
            if (promoPaymentOption.getPayMethod() != PayMethod.BALANCE) {
                return promoPaymentOption.getTransactionAmount();
            }
        }
        return null;
    }

    private PayMethod getNonWalletPayMethod(List<PromoPaymentOption> paymentOptions) {
        if (paymentOptions == null)
            return null;
        Integer paymentOptionsSize = paymentOptions.size();
        if (paymentOptionsSize == 1) {
            return paymentOptions.get(0).getPayMethod();
        }
        for (PromoPaymentOption promoPaymentOption : paymentOptions) {
            if (promoPaymentOption.getPayMethod() != PayMethod.BALANCE) {
                return promoPaymentOption.getPayMethod();
            }
        }
        return null;
    }

    private String getSavingAmount(List<PromoSaving> promoSavings, RedemptionType redemptionType) {
        if (promoSavings == null)
            return null;
        for (PromoSaving promoSaving : promoSavings) {
            Optional<RedemptionType> redemptionTypeOptional = RedemptionType
                    .fromString(promoSaving.getRedemptionType());
            if (redemptionTypeOptional.isPresent() && redemptionTypeOptional.get() == redemptionType) {
                return PaymentOfferUtils.getAmountInRupees(promoSaving.getSavings());
            }
        }
        return null;
    }

    public boolean isPromoApplied(ApplyPromoResponseData data) {
        return data != null && data.getStatus() == 1;
    }

    public BulkApplyPromoServiceRequest prepareApplyPromoServiceRequest(
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse cashierInfoResponse,
            WorkFlowResponseBean workFlowResponseBean, boolean offerOnTotalAmount) {

        List<CardBeanBiz> savedCards = null;
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        if (userDetailsBiz != null) {
            savedCards = userDetailsBiz.getMerchantViewSavedCardsList();
        }

        BulkApplyPromoServiceRequest applyPromoServiceRequest = new BulkApplyPromoServiceRequest();
        applyPromoServiceRequest.setChannel(nativeCashierInfoRequest.getHead().getChannelId().getValue());

        Long totalAmount;
        String amountInRupees;
        if (offerOnTotalAmount) {
            totalAmount = getNonHybridTxnAmountInPaise(nativeCashierInfoRequest, cashierInfoResponse);
            amountInRupees = getNonHybridTxnAmountInRupees(nativeCashierInfoRequest, cashierInfoResponse);
        } else {
            totalAmount = nonWalletTxnAmountInPaiseIfHybrid(nativeCashierInfoRequest, cashierInfoResponse);
            amountInRupees = nonWalletTxnAmountIfHybrid(nativeCashierInfoRequest, cashierInfoResponse);
        }

        applyPromoServiceRequest.setTotalTransactionAmount(totalAmount);
        applyPromoServiceRequest.setPaymentOptionsBulk(new ArrayList<>());
        List<PaymentOptionBulk> paymentOptionBulkList = applyPromoServiceRequest.getPaymentOptionsBulk();
        List<PayChannelBase> merchantSavedIntruments = cashierInfoResponse.getBody().getMerchantPayOption()
                .getSavedInstruments();
        for (PayChannelBase savedInstrument : merchantSavedIntruments) {
            PaymentOptionBulk paymentOptionBulk = new PaymentOptionBulk(new ArrayList<>());
            PayMethod payMethod = PayMethod.getPayMethodByMethod(savedInstrument.getPayMethod());
            if (payMethod != null) {
                PromoPaymentOption promoPaymentOption = preparePromoPaymentOption(
                        // this will be in rupees as it will be further
                        // converted in paise in paymentOptionBuilder
                        amountInRupees, savedInstrument, payMethod, savedCards, nativeCashierInfoRequest.getBody()
                                .getMid(), workFlowResponseBean.getMerchnatLiteViewResponse(),
                        workFlowResponseBean.getAddAndPayLiteViewResponse());
                if (promoPaymentOption != null) {
                    ApplyPromoPaymentOptionBuilder paymentOptionBuilder = ApplyPromoPaymentOptionBuilderFactory
                            .getApplyPromoPaymentOptionBuilder(payMethod);
                    PaymentOption paymentOption = paymentOptionBuilder.build(promoPaymentOption,
                            nativeCashierInfoRequest.getBody().getMid());
                    paymentOptionBulk.getPaymentOptions().add(paymentOption);
                }
            } else {
                LOGGER.error("Paymethod = {} is not valid in savedInstrument", savedInstrument.getPayMethod());
            }
            paymentOptionBulkList.add(paymentOptionBulk);
        }

        return applyPromoServiceRequest;
    }

    public String getWalletBalance(NativeCashierInfoResponse nativeCashierInfoResponse) {
        for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : nativeCashierInfoResponse
                .getBody().getMerchantPayOption().getPayMethods()) {
            if (PayMethod.BALANCE.getMethod().equals(payMethod.getPayMethod())) {
                for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                    if (payChannelBase instanceof PPI) {
                        return ((PPI) payChannelBase).getBalanceInfo().getAccountBalance().getValue();
                    }
                }
            }
        }
        return null;
    }

    public BulkApplyPromoServiceResponse bulkApplyPromo(BulkApplyPromoServiceRequest bulkApplyPromoServiceRequest,
            String mid, String custId, String orderId, String paytmUserId) {
        try {
            return paymentPromoSevice.bulkApplyPromo(bulkApplyPromoServiceRequest,
                    prepareQueryParams(mid, custId, orderId, paytmUserId));
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private PromoPaymentOption preparePromoPaymentOption(final String nonWalletTxnAmount,
            final PayChannelBase savedInstrument, final PayMethod payMethod, List<CardBeanBiz> savedCards, String mid,
            LitePayviewConsultResponseBizBean MerchantLitePayviewConsultResponseBizBean,
            LitePayviewConsultResponseBizBean AddNPayLitePayviewConsultResponseBizBean) {
        PromoPaymentOption paymentOption = new PromoPaymentOption();
        paymentOption.setTransactionAmount(nonWalletTxnAmount);
        paymentOption.setPayMethod(payMethod);

        /**
         * PG will start sending CIN and bin 8 alias to promo in ApplyBulk Promo
         * Api (AND Flow) once <theia.sendCINAnd8BinHashToPromo> FF4J Flag get
         * enable. We will fetch CIN and bin8Hash to promo by filtering data out
         * of LitePayView Response.
         */

        if (savedInstrument instanceof SavedCard) {
            SavedCard savedCard = (SavedCard) savedInstrument;
            String cardId = savedCard.getCardDetails().getCardId();
            if (savedCards != null) {
                for (CardBeanBiz card : savedCards) {

                    /**
                     * 1st condition :
                     * StringUtils.isNotEmpty(card.getCardIndexNo()) &&
                     * card.getCardIndexNo().equals(cardId) - true in case of
                     * CIN returned in cachier response.
                     *
                     * 2nd condition : null != card.getCardId() &&
                     * card.getCardId().equals( Long.parseLong(cardId)) - true
                     * in case of savedcardId is returned in cashier response
                     */

                    if ((StringUtils.isNotEmpty(card.getCardIndexNo()) && card.getCardIndexNo().equals(cardId))
                            || (null != card.getCardId() && card.getCardId().equals(Long.parseLong(cardId)))) {
                        paymentOption.setBankCode(savedCard.getIssuingBank());
                        paymentOption.setCardNo(card.getCardNumber());
                        if (cardId.length() > 15) {
                            paymentOption.setSavedCardId(cardId);
                            paymentOption.setEightDigitBinHash(card.getEightDigitBinHash());

                            /**
                             * In case of CIN, cardNumber is masked, So to find
                             * bin6 while creating applybulk request, PG will
                             * set First6Dgit in cardNumber.
                             */

                            paymentOption.setCardNo(card.getCardNumber().substring(0, 6));
                        } else if (ff4JUtil.isFeatureEnabledForPromo(mid)) {
                            PayCardOptionViewBiz payCardOptionViewBiz = getSavedCardInfoFromLitePayViewResponse(
                                    MerchantLitePayviewConsultResponseBizBean,
                                    AddNPayLitePayviewConsultResponseBizBean, card.getCardNumber(),
                                    card.getExpiryDate());
                            if (null != payCardOptionViewBiz) {
                                paymentOption.setSavedCardId(payCardOptionViewBiz.getCardIndexNo());
                                paymentOption.setEightDigitBinHash(payCardOptionViewBiz.getExtendInfo().get(
                                        (TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH)));
                            } else {

                                /**
                                 * ***Temporary code - This will occur when any
                                 * card from saved card PG DB is not present in
                                 * Platform response and
                                 * <theia.sendCINAnd8BinHashToPromo> FF4J Flag
                                 * is true, then we have to send CIN and
                                 * eightDigitBinHash to promo.
                                 */
                                String cardIndexNumber = workFlowHelper.getCardIndexNoFromCardNumber(card
                                        .getCardNumber());
                                if (null != cardIndexNumber) {
                                    paymentOption.setSavedCardId(cardIndexNumber);
                                } else {
                                    LOGGER.error("Unable to fetch cardIndexNumber from Platform");
                                    throw BaseException.getException();
                                }
                                CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(card
                                        .getCardNumber().substring(0, 8));
                                if (null != cardBinHashResponse
                                        && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                                    paymentOption.setEightDigitBinHash(cardBinHashResponse.getCardBinDigestDetailInfo()
                                            .getEightDigitBinHash());
                                } else {
                                    LOGGER.error("Unable to fetch eightDigitBinHash from Platform");
                                }
                            }
                        }
                    }
                }
            }
        } else if (savedInstrument instanceof SavedVPA) {
            SavedVPA savedVPA = (SavedVPA) savedInstrument;
            paymentOption.setCardNo(savedVPA.getVpaDetails().getVpa());
        } else {
            // LOGGER.error("savedInstrument is not savedCard or savedVPA");
            LOGGER.error("savedInstrument is not savedCard or savedVPA savedInstrument Object = {}", savedInstrument);
            return null;
        }
        return paymentOption;
    }

    private String nonWalletTxnAmountIfHybrid(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        long nonWalletAmount = nonWalletTxnAmountInPaiseIfHybrid(nativeCashierInfoRequest, nativeCashierInfoResponse);
        return AmountUtils.getPaddedTransactionAmountInRupee(String.valueOf(nonWalletAmount));
    }

    private long nonWalletTxnAmountInPaiseIfHybrid(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse) {

        long orderAmount = Long.parseLong(AmountUtils.getTransactionAmountInPaise(nativeCashierInfoRequest.getBody()
                .getOrderAmount()));
        long nonWalletAmount = orderAmount;
        if (EPayMode.HYBRID == nativeCashierInfoResponse.getBody().getPaymentFlow()) {
            long walletBalance = Long.parseLong(AmountUtils
                    .getTransactionAmountInPaise(getWalletBalance(nativeCashierInfoResponse)));
            if (walletBalance < orderAmount) {
                LOGGER.info("Valid hybrid case applying payment offer on orderAmount - walletBalance");
                nonWalletAmount = orderAmount - walletBalance;
            }
        }
        return nonWalletAmount;
    }

    private long getNonHybridTxnAmountInPaise(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        return Long.parseLong(AmountUtils.getTransactionAmountInPaise(nativeCashierInfoRequest.getBody()
                .getOrderAmount()));
    }

    private String getNonHybridTxnAmountInRupees(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        long nonHybridTxnAmountInPaise = getNonHybridTxnAmountInPaise(nativeCashierInfoRequest,
                nativeCashierInfoResponse);
        return AmountUtils.getPaddedTransactionAmountInRupee(String.valueOf(nonHybridTxnAmountInPaise));
    }

    private String setCardNumber(PromoPaymentOption promoPaymentOption) {
        SavedCardResponse<SavedCardVO> savedCardResponse = savedCardsService.getSavedCardByCardId(Long
                .parseLong(promoPaymentOption.getSavedCardId()));
        if (savedCardResponse.getStatus()) {
            promoPaymentOption.setCardNo(savedCardResponse.getResponseData().getCardNumber());
        } else {
            LOGGER.error("Error in fetching savedcard by savedcardId = {}, errorMsg = {}",
                    promoPaymentOption.getSavedCardId(), savedCardResponse.getMessage());
            throw BaseException.getException();
        }
        return promoPaymentOption.getCardNo();
    }

    /**
     * @param MerchantLitePayviewConsultResponseBizBean
     * @param AddNPayLitePayviewConsultResponseBizBean
     * @param cardNumber
     * @param expiryDate
     * @return saved card from litePayView Response
     */
    public PayCardOptionViewBiz getSavedCardInfoFromLitePayViewResponse(
            LitePayviewConsultResponseBizBean MerchantLitePayviewConsultResponseBizBean,
            LitePayviewConsultResponseBizBean AddNPayLitePayviewConsultResponseBizBean, String cardNumber,
            String expiryDate) {

        String first6Digit = cardNumber.substring(0, 6);
        String last4Digit = cardNumber.substring(cardNumber.length() - 4, cardNumber.length());
        String expiryMonth = expiryDate.substring(0, 2);
        String expiryYear = expiryDate.substring(expiryDate.length() - 4, expiryDate.length());
        PayCardOptionViewBiz payCardOptionViewBiz = null;
        if (null != MerchantLitePayviewConsultResponseBizBean) {
            for (PayMethodViewsBiz payMethodViewsBiz : MerchantLitePayviewConsultResponseBizBean.getPayMethodViews()) {
                payCardOptionViewBiz = payMethodViewsBiz
                        .getPayCardOptionViews()
                        .stream()
                        .filter(payCardOptionBiz -> first6Digit.equals(payCardOptionBiz.getMaskedCardNo().substring(0,
                                6))
                                && last4Digit.equals(payCardOptionBiz.getMaskedCardNo().substring(
                                        payCardOptionBiz.getMaskedCardNo().length() - 4,
                                        payCardOptionBiz.getMaskedCardNo().length()))
                                && expiryMonth.equals(payCardOptionBiz.getExpiryMonth())
                                && expiryYear.equals(payCardOptionBiz.getExpiryYear())).findAny().orElse(null);
                if (null != payCardOptionViewBiz) {
                    return payCardOptionViewBiz;
                }
            }
        }

        if (null != AddNPayLitePayviewConsultResponseBizBean) {
            for (PayMethodViewsBiz payMethodViewsBiz : AddNPayLitePayviewConsultResponseBizBean.getPayMethodViews()) {
                payCardOptionViewBiz = payMethodViewsBiz
                        .getPayCardOptionViews()
                        .stream()
                        .filter(payCardOptionBiz -> first6Digit.equals(payCardOptionBiz.getMaskedCardNo().substring(0,
                                6))
                                && last4Digit.equals(payCardOptionBiz.getMaskedCardNo().substring(
                                        payCardOptionBiz.getMaskedCardNo().length() - 4,
                                        payCardOptionBiz.getMaskedCardNo().length()))
                                && expiryMonth.equals(payCardOptionBiz.getExpiryMonth())
                                && expiryYear.equals(payCardOptionBiz.getExpiryYear())).findAny().orElse(null);
                if (null != payCardOptionViewBiz) {
                    return payCardOptionViewBiz;
                }
            }

        }
        return payCardOptionViewBiz;
    }

    private void setbin8Hash(PromoPaymentOption promoPaymentOption, String cardNumber, String CIN) {
        CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNumber.substring(0, 8));
        if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
            String eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
            nativeSessionUtil.cacheEightDigitBinHash(PaymentOfferUtils.getApplyPromoForCachedKey(CIN),
                    TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH, eightDigitBinHash);
            promoPaymentOption.setEightDigitBinHash(eightDigitBinHash);
        }
    }

    private boolean isCCDCPaymethod(PayMethod payMethod) {

        switch (payMethod) {
        case CREDIT_CARD:
        case DEBIT_CARD:
        case EMI:
        case EMI_DC:
            return true;
        default:
            return false;
        }
    }

    private Map<String, String> prepareQueryParams(String mid, String custId, String orderId, String paytmUserId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("merchant-id", mid);
        if (StringUtils.isNotBlank(custId)) {
            queryParams.put("customer-id", custId);
        }
        if (StringUtils.isNotBlank(orderId)) {
            queryParams.put("order-id", orderId);
        }
        if (StringUtils.isNotBlank(paytmUserId)
                && ff4JUtil.isFeatureEnabled(TheiaConstant.ExtraConstants.THEIA_ENABLE_PROMO_WALLET_CASHBACK, mid)) {
            queryParams.put("paytm-user-id", paytmUserId);
        }
        return queryParams;
    }

    public void validatePromoCode(String promoCodeFromSimplified, String requestPromoCode) {
        // if not null then validate else complete validation
        if (StringUtils.isNotBlank(promoCodeFromSimplified) && !promoCodeFromSimplified.equals(requestPromoCode)) {
            throw RequestValidationException
                    .getException("PromoCode From SimlifiedPaymentOffers and ApplyPromoRequest does not match");
        }
    }

    /*
     * 1.Validate PromoRequest mid and orderId with query param 2.Validate
     * PromoRequest mid and orderId with mid and orderId from InitiateRequest
     */
    private void validateMidAndOrderIdInQueryParam(String mid, String orderId, String requestMid, String requestOrderId) {

        if (StringUtils.isBlank(requestOrderId)) {
            throw BaseException.getException("OrderId can't be blank");
        }

        if (!requestMid.equals(OfflinePaymentUtils.gethttpServletRequest().getParameter(
                com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID))
                || !requestOrderId.equals(OfflinePaymentUtils.gethttpServletRequest().getParameter(
                        com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID))
                || !requestMid.equals(mid) || !requestOrderId.equals(orderId)) {
            throw BaseException.getException("Mid or OrderId doesn't match");
        }
    }

    /*
     * 1.Validate txnToken 2.Match orderId and mid from InitiateRequest and
     * PromoRequests 3.Return promocode from simpliedPaymentOffers if available
     */
    public String validateAndGetSimplifiedPromoCode(InitiateTransactionRequestBody orderDetails, String requestMid,
            String requestOrderId) {

        String initiatePromoCode = null;

        if (orderDetails != null && orderDetails.getSimplifiedPaymentOffers() != null) {
            initiatePromoCode = orderDetails.getSimplifiedPaymentOffers().getPromoCode();
            // validate mid and orderId
            validateMidAndOrderIdInQueryParam(orderDetails.getMid(), orderDetails.getOrderId(), requestMid,
                    requestOrderId);
        } else {
            // if simplifiedPaymentOffers is null then throw exception
            throw RequestValidationException.getException("SimlifiedPaymentOffers can't be null for tokenType "
                    + TokenType.TXN_TOKEN.getType());
        }
        return initiatePromoCode;
    }

    public void setCustIdForTxnTokenFlow(ApplyPromoRequest request, InitiateTransactionRequestBody orderDetails) {

        if (orderDetails != null) {
            if (StringUtils.isBlank(request.getBody().getCustId())) {
                request.getBody().setCustId(orderDetails.getUserInfo().getCustId());
            }
        }
    }

    public boolean isAnyPaymentOfferAvailableOnMerchant(NativeCashierInfoResponse response, String mid,
            String paytmUserId) {
        if (response != null && response.getBody() != null
                && CollectionUtils.isNotEmpty(response.getBody().getPaymentOffers())) {
            return true;
        }
        return isAnyPaymentOfferAvailableOnMerchant(mid, paytmUserId);
    }

}
