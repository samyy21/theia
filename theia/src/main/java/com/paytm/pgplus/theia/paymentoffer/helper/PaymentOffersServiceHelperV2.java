package com.paytm.pgplus.theia.paymentoffer.helper;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.util.CryptoUtils;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOptionBulk;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.Item;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.PaymentDetails;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.*;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.*;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoSevice;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaResponse;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequest;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequestBody;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.enums.PaymentPromoServiceError;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.model.request.*;
import com.paytm.pgplus.theia.paymentoffer.model.response.*;
import com.paytm.pgplus.theia.paymentoffer.requestbuilder.ApplyPromoPaymentOptionBuilder;
import com.paytm.pgplus.theia.paymentoffer.requestbuilder.ApplyPromoPaymentOptionBuilderFactory;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.common.enums.CardTypeEnum.DINERS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;

@Component("paymentOffersServiceHelperV2")
public class PaymentOffersServiceHelperV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersServiceHelperV2.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String CONST_ITEM = "item001";

    @Autowired
    PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

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

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    @Qualifier("coftTokenDataService")
    private CoftTokenDataService coftTokenDataService;

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffers(FetchAllPaymentOffersRequest request,
            String paytmUserId) {
        return searchPaymentOffers(request.getBody().getMid(), paytmUserId);
    }

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffers(String mid, String paytmUserId)
            throws BaseException {
        SearchPaymentOffersServiceRequestV2 searchPaymentOffersRequest = prepareSearchPaymentOffersServiceRequest();
        return searchPaymentOffers(searchPaymentOffersRequest, mid, paytmUserId);
    }

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffers(String mid, String paytmUserId,
            Map<String, String> promoContext) throws BaseException {
        SearchPaymentOffersServiceRequestV2 searchPaymentOffersRequest = prepareSearchPaymentOffersServiceRequest();
        searchPaymentOffersRequest.setAffordabilityInfo(getAffordabilityInfo(promoContext));
        return searchPaymentOffers(searchPaymentOffersRequest, mid, paytmUserId);
    }

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffers(String mid, FetchAllItemOffer fetchAllItemOffer,
            String paytmUserId, ApplyItemOffers applyItemOffers) throws BaseException {
        SearchPaymentOffersServiceRequestV2 searchPaymentOffersRequest = prepareSearchPaymentOffersServiceRequest(
                fetchAllItemOffer, applyItemOffers);
        return searchPaymentOffersV2(searchPaymentOffersRequest, mid, paytmUserId);
    }

    public SearchPaymentOffersServiceRequestV2 prepareSearchPaymentOffersServiceRequest(
            FetchAllItemOffer fetchAllItemOffer, ApplyItemOffers applyItemOffers) {
        SearchPaymentOffersServiceRequestV2 searchPaymentOffersServiceRequest = new SearchPaymentOffersServiceRequestV2();
        List<SearchPaymentOffersServiceRequestData> requestDataList = new ArrayList<>();
        if (fetchAllItemOffer != null && CollectionUtils.isNotEmpty(fetchAllItemOffer.getItems())) {
            for (FetchAllItemOffer.Item itemOffer : fetchAllItemOffer.getItems()) {
                requestDataList.add(getSearchPaymentOffersServiceRequestData(itemOffer));
            }
        }
        searchPaymentOffersServiceRequest.setItems(requestDataList);
        Map<String, String> affordabilityInfo = getAffordabilityInfo(applyItemOffers);
        searchPaymentOffersServiceRequest.setAffordabilityInfo(affordabilityInfo);
        return searchPaymentOffersServiceRequest;
    }

    @Nullable
    private Map<String, String> getAffordabilityInfo(ApplyItemOffers applyItemOffers) {
        if (Objects.nonNull(applyItemOffers)) {
            return getAffordabilityInfo(applyItemOffers.getPromoContext());
        }
        return new HashMap<>();
    }

    private Map<String, String> getAffordabilityInfo(Map<String, String> promoContext) {
        if (Objects.nonNull(promoContext) && Objects.nonNull(promoContext.get("affordabilityInfo"))) {
            try {
                return JsonMapper.mapJsonToObject(promoContext.get("affordabilityInfo"), HashMap.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Failed to parse affordability info", e);
            }
        }
        return new HashMap<>();
    }

    public boolean isDealsFlow(NativeCashierInfoRequestBody nativeCashierInfoRequestBody) {
        Map<String, String> affordabilityInfo = getAffordabilityInfo(nativeCashierInfoRequestBody.getApplyItemOffers());
        return isDealsFlow(affordabilityInfo);
    }

    public boolean isDealsFlow(Map<String, String> affordabilityInfo) {
        return MapUtils.isNotEmpty(affordabilityInfo)
                && com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.DEAL_FLOW
                        .equals(affordabilityInfo
                                .get(com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.PAYMENT_FLOW));
    }

    public SearchPaymentOffersServiceRequestData getSearchPaymentOffersServiceRequestData(
            FetchAllItemOffer.Item itemOffer) {
        SearchPaymentOffersServiceRequestData requestData = new SearchPaymentOffersServiceRequestData();
        requestData.setBrandId(itemOffer.getBrandId());
        requestData.setCategoryList(itemOffer.getCategoryList());
        requestData.setDiscoverability(itemOffer.getDiscoverability());
        requestData.setId(itemOffer.getId());
        requestData.setMerchantId(itemOffer.getMerchantId());
        requestData.setModel(itemOffer.getModel());
        requestData.setPrice(itemOffer.getPrice());
        requestData.setProductId(itemOffer.getProductId());
        return requestData;
    }

    public SearchPaymentOffersServiceRequestV2 prepareSearchPaymentOffersServiceRequest() {
        SearchPaymentOffersServiceRequestV2 searchPaymentOffersServiceRequest = new SearchPaymentOffersServiceRequestV2();
        SearchPaymentOffersServiceRequestData requestData = new SearchPaymentOffersServiceRequestData();
        requestData.setId(CONST_ITEM);
        List<SearchPaymentOffersServiceRequestData> requestDataList = new ArrayList<>();
        requestDataList.add(requestData);
        searchPaymentOffersServiceRequest.setItems(requestDataList);
        return searchPaymentOffersServiceRequest;
    }

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffers(SearchPaymentOffersServiceRequestV2 request,
            String mid, String paytmUserId) throws BaseException {

        SearchPaymentOffersServiceResponseV2 cachedResponse = null;
        boolean enablePromoUserSegmentation = ff4JUtil.isFeatureEnabled(THEIA_ENABLE_PROMO_USER_SEGMENTATION, mid);
        boolean dealsFlow = isDealsFlow(request.getAffordabilityInfo());
        if (!enablePromoUserSegmentation) {
            cachedResponse = getCachedResponse(mid, SearchPaymentOffersServiceResponseV2.class, dealsFlow);
        }

        if (cachedResponse != null) {
            LOGGER.info("Serving SearchPaymentOffersServiceResponseV2 from cache");
            return cachedResponse;
        }
        try {
            SearchPaymentOffersServiceResponseV2 response = paymentPromoSevice.searchPaymentOffersV2(request,
                    prepareQueryParamsV2(mid, paytmUserId, enablePromoUserSegmentation));
            if (isValidSearchPaymentOffersServiceResponseV2(response)) {
                if (!enablePromoUserSegmentation) {
                    cacheResponse(mid, response, dealsFlow);
                }

                return response;
            } else {
                LOGGER.error("Error in getting SearchPaymentOffersServiceResponse");
                throw BaseException.getException();
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BaseException();
        }
    }

    public SearchPaymentOffersServiceResponseV2 searchPaymentOffersV2(SearchPaymentOffersServiceRequestV2 request,
            String mid, String paytmUserId) {
        /*
         * SearchPaymentOffersServiceResponseV2 cachedResponse = null;
         * List<String> itemIds = request.getItems().stream().map(i ->
         * i.getId()).collect(Collectors.toList()); cachedResponse =
         * getCachedResponse(mid, itemIds,
         * SearchPaymentOffersServiceResponseV2.class);
         * 
         * if (cachedResponse != null) { LOGGER.info(
         * "Serving item level SearchPaymentOffersServiceResponseV2 from cache"
         * ); return cachedResponse; }
         */

        try {
            SearchPaymentOffersServiceResponseV2 response = paymentPromoSevice.searchPaymentOffersV2(request,
                    prepareQueryParamsV2(mid, paytmUserId, false));
            if (isValidSearchPaymentOffersServiceResponseV2(response)) {
                return response;
            } else {
                LOGGER.error("Error in getting SearchPaymentOffersServiceResponse");
                throw BaseException.getException();
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BaseException();
        }
    }

    public boolean isAnyPaymentOfferAvailableOnMerchant(String mid, String paytmUserId, Map<String, String> promoContext) {
        try {
            SearchPaymentOffersServiceResponseV2 promoResp = searchPaymentOffers(mid, paytmUserId, promoContext);
            if (promoResp != null && CollectionUtils.isNotEmpty(promoResp.getItems())
                    && promoResp.getItems().get(0) != null
                    && CollectionUtils.isNotEmpty(promoResp.getItems().get(0).getData())) {
                return true;
            }
        } catch (BaseException e) {
            LOGGER.error("Exception in searchPaymentOffers for mid {}", mid);
        }
        return false;
    }

    public boolean isBin8OfferAvailableOnMerchant(String mid, String paytmUserId, Map<String, String> promoContext) {
        try {
            if (StringUtils.isBlank(paytmUserId)
                    && ff4JUtil.isFeatureEnabled(THEIA_ENABLE_PROMO_USER_SEGMENTATION, mid)) {
                return true;
            }
            SearchPaymentOffersServiceResponseV2 searchPaymentOffersServiceResponse = searchPaymentOffers(mid,
                    paytmUserId, promoContext);
            if (searchPaymentOffersServiceResponse != null
                    && CollectionUtils.isNotEmpty(searchPaymentOffersServiceResponse.getItems())
                    && searchPaymentOffersServiceResponse.getItems().get(0) != null
                    && CollectionUtils.isNotEmpty(searchPaymentOffersServiceResponse.getItems().get(0).getData())) {
                for (SearchPaymentOffersResponseData offersResponseData : searchPaymentOffersServiceResponse.getItems()
                        .get(0).getData()) {
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

    private void cacheResponse(String mid, SearchPaymentOffersServiceResponseV2 response, boolean dealsFlow) {
        String ttl = ConfigurationUtil.getProperty("payment.promo.search.cache.minutes", "15");
        LOGGER.info("Caching SearchPaymentOffersServiceResponseV2 {}, ttl {}", response, ttl);
        String cacheKey;
        if (dealsFlow)
            cacheKey = PaymentOfferUtils.getSearchPaymentOffersCacheKeyForDeals(mid, response.getClass());
        else
            cacheKey = PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid, response.getClass());
        theiaSessionRedisUtil.set(cacheKey, response, Integer.parseInt(ttl) * 60);

        if (!ff4jUtils.isFeatureEnabledOnMid(CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE,
                THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
            LOGGER.info("operation on static redis, {}", CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE);
            theiaTransactionalRedisUtil.set(cacheKey, response, Integer.parseInt(ttl) * 60);
        }
    }

    private SearchPaymentOffersServiceResponseV2 getCachedResponse(String mid, Class cachedClass, boolean dealsFlow) {

        SearchPaymentOffersServiceResponseV2 cachedResponse = null;
        String cacheKey;
        if (dealsFlow)
            cacheKey = PaymentOfferUtils.getSearchPaymentOffersCacheKeyForDeals(mid, cachedClass);
        else
            cacheKey = PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid, cachedClass);
        cachedResponse = (SearchPaymentOffersServiceResponseV2) theiaSessionRedisUtil.get(cacheKey);

        if (cachedResponse == null) {
            if (!ff4jUtils.isFeatureEnabledOnMid(CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE,
                    THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
                LOGGER.info("operation on static redis, {}", CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE);
                cachedResponse = (SearchPaymentOffersServiceResponseV2) theiaTransactionalRedisUtil.get(cacheKey);
            }
        }

        return cachedResponse;
    }

    /*
     * private SearchPaymentOffersServiceResponseV2 getCachedResponse(String
     * mid, List<String> itemIds, Class cachedClass) {
     * 
     * SearchPaymentOffersServiceResponseV2 cachedResponse = null;
     * 
     * String cacheKey = PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid,
     * itemIds, cachedClass); cachedResponse =
     * (SearchPaymentOffersServiceResponseV2)
     * theiaSessionRedisUtil.get(cacheKey);
     * 
     * if (cachedResponse == null) { // need to remove after testing
     * LOGGER.info("cache is null for key :{}", cacheKey); if
     * (!ff4jUtils.isFeatureEnabledOnMid(CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE,
     * THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
     * LOGGER.info("operation on static redis, {}",
     * CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE); cachedResponse =
     * (SearchPaymentOffersServiceResponseV2) theiaTransactionalRedisUtil.get(
     * PaymentOfferUtils.getSearchPaymentOffersCacheKey(mid, itemIds,
     * cachedClass)); } }
     * 
     * return cachedResponse; }
     */

    public ApplyPromoServiceResponseV2 applyPromoV2(ApplyPromoRequest request, String referenceId) {

        if (isAnyPaymentOfferAvailableOnMerchant(request.getBody().getMid(), request.getBody().getPaytmUserId(),
                request.getBody().getPromoContext())) {
            populateEncUserIdInApplyPromoRequest(request.getBody().getEncUserId(), request, referenceId);
            if (StringUtils.isNotBlank(request.getBody().getEncUserId())) {
                try {
                    request.getBody().setPaytmUserId(CryptoUtils.decryptAES(request.getBody().getEncUserId()));
                } catch (Exception e) {
                    LOGGER.error("Error while decrypting user id corresponding to vpa :{}", e.getMessage());
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);

                }
            }
            ApplyPromoServiceRequestV2 applyPromoServiceRequestV2 = prepareApplyPromoServiceRequestV2(request);
            try {
                ApplyPromoServiceResponseV2 applyPromoServiceResponseV2 = paymentPromoSevice.applyPromoV2(
                        applyPromoServiceRequestV2,
                        prepareQueryParams(request.getBody().getMid(), request.getBody().getCustId(), null, request
                                .getBody().getPaytmUserId()));
                return applyPromoServiceResponseV2;
                // return
                // prepareResponse(applyPromoServiceResponseV2,request,version);
            } catch (FacadeCheckedException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                throw BaseException.getException();
            }
        } else {
            LOGGER.info("No promo exists for merchant = {}", request.getBody().getMid());
            throw BaseException.getException(ResultCode.PAYMENT_PROMO_NOT_EXISTS_ON_MERCHANT);
        }
    }

    public <T extends PromoServiceResponseBase> boolean isPromoServiceSuccessResponse(T response) {
        return response != null && response.getStatus() == 1;
    }

    private ApplyPromoServiceRequestV2 prepareApplyPromoServiceRequestV2(ApplyPromoRequest request) {
        ApplyPromoServiceRequestV2 applyPromoServiceRequestV2 = new ApplyPromoServiceRequestV2();
        applyPromoServiceRequestV2.setClientDetails(getClientDetails(request.getHead().getChannelId().getValue()));
        applyPromoServiceRequestV2.setUser(getUserDetails(request.getBody().getCustId(), request.getBody()
                .getPaytmUserId()));
        setPromoContext(applyPromoServiceRequestV2, request);
        if (request.getBody().getPromocode() != null) {
            applyPromoServiceRequestV2.setPromocode(request.getBody().getPromocode());
        } else {
            if (request.getBody().getPromoInfo() != null) {
                String promoInfo = request.getBody().getPromoInfo().stream()
                        .filter(promo -> promo != null && StringUtils.isNotBlank(promo.getPromocode()))
                        .map(promo -> String.valueOf(promo.getPromocode())).collect(Collectors.joining(","));
                applyPromoServiceRequestV2.setPromocode(promoInfo);
            }
        }
        applyPromoServiceRequestV2.setPaymentDetails(getPaymentDetails(request));
        return applyPromoServiceRequestV2;
    }

    private void setPromoContext(ApplyPromoServiceRequestV2 applyPromoServiceRequestV2, ApplyPromoRequest request) {
        if (request.getBody().getPromoContext() != null) {
            applyPromoServiceRequestV2.setPromoContext(request.getBody().getPromoContext());

        } else {
            applyPromoServiceRequestV2.setCart(getCartDetails(request));
        }
    }

    private User getUserDetails(String custId, String paytmUserId) {
        User user = new User();
        user.setId(custId);
        user.setPaytmUserId(paytmUserId);
        return user;
    }

    private Cart getCartDetails(ApplyPromoRequest request) {
        Cart cart = new Cart();
        if (request.getBody().getCartDetails() != null
                && CollectionUtils.isNotEmpty(request.getBody().getCartDetails().getItems())) {
            HashMap<String, Item> cartMap = new HashMap<>();
            for (PromoItemDetail item : request.getBody().getCartDetails().getItems()) {
                cartMap.put(item.getId(), getItem(item));
            }
            cart.setItems(cartMap);
        } else {
            cart.setItems(getItemDetails(request.getBody().getTotalTransactionAmount()));
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

    private Cart getCartDetails(Long totalAmount) {
        Cart cart = new Cart();
        cart.setItems(getItemDetails(totalAmount));
        return cart;
    }

    private Map<String, Item> getItemDetails(String totalTransactionAmount) {
        Map<String, Item> items = new HashMap<>();
        Item item = new Item();
        item.setPrice((int) PaymentOfferUtils.getAmountInPaise(totalTransactionAmount));
        items.put(CONST_ITEM, item);
        return items;
    }

    private Map<String, Item> getItemDetails(Long totalTransactionAmount) {
        Map<String, Item> items = new HashMap<>();
        Item item = new Item();
        item.setPrice(Math.toIntExact(totalTransactionAmount));
        items.put(CONST_ITEM, item);
        return items;
    }

    private ClientDetails getClientDetails(String channel) {
        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setChannel(channel);
        return clientDetails;
    }

    private PaymentDetails getPaymentDetails(ApplyPromoRequest request) {
        PaymentDetails paymentDetails = new PaymentDetails();
        ApplyPromoRequestBody requestBody = request.getBody();
        boolean isEnableGcinOnCoftPromo = ff4jUtils.isFeatureEnabledOnMid(requestBody.getMid(),
                ENABLE_GCIN_ON_COFT_PROMO, false);
        if (!isEnableGcinOnCoftPromo) {
            paymentDetails.setPaymentOptions(getPaymentOptions(requestBody));
        } else {
            String txnToken = getUpdatedTxnToken(request, requestBody);
            paymentDetails.setPaymentOptions(getPaymentOptionsForCoftPromoTxns(requestBody, txnToken));
        }
        paymentDetails.setTotalTransactionAmount(PaymentOfferUtils.getAmountInPaise(requestBody
                .getTotalTransactionAmount()));
        return paymentDetails;
    }

    private String getUpdatedTxnToken(ApplyPromoRequest request, ApplyPromoRequestBody requestBody) {
        String txnToken = request.getHead().getToken();
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            String mid = requestBody.getMid();
            txnToken = nativeSessionUtil.createTokenForMidSSOFlow(txnToken, mid);
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType()) && null == txnToken) {
            txnToken = request.getHead().getTxnToken();
        }
        return txnToken;
    }

    private List<PaymentOption> getPaymentOptions(ApplyPromoRequestBody requestBody) {
        List<PaymentOption> paymentOptions = new ArrayList<>(requestBody.getPaymentOptions().size());
        String mid = requestBody.getMid();
        String paytmUserId = requestBody.getPaytmUserId();
        Integer payOptionSize = requestBody.getPaymentOptions().size();
        for (PromoPaymentOption promoPaymentOption : requestBody.getPaymentOptions()) {
            // Hybrid is not supported currently, will send only nonwallet part
            // to promoservice
            if (payOptionSize > 1 && PayMethod.BALANCE.equals(promoPaymentOption.getPayMethod())) {
                continue;
            }
            if (isCCDCPaymethod(promoPaymentOption.getPayMethod()) && ff4JUtil.isFeatureEnabledForPromo(mid)) {
                if (StringUtils.isNotEmpty(promoPaymentOption.getSavedCardId())) {
                    processForSavedCardId(promoPaymentOption, requestBody.getMid(), paytmUserId);
                } else if (StringUtils.isNotEmpty(promoPaymentOption.getCardNo())) {
                    processForCardNumber(promoPaymentOption, mid, paytmUserId);
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

    private List<PaymentOption> getPaymentOptionsForCoftPromoTxns(ApplyPromoRequestBody requestBody, String txnToken) {

        List<PaymentOption> paymentOptions = new ArrayList<>(requestBody.getPaymentOptions().size());
        String mid = requestBody.getMid();
        Integer payOptionSize = requestBody.getPaymentOptions().size();
        for (PromoPaymentOption promoPaymentOption : requestBody.getPaymentOptions()) {
            // Hybrid is not supported currently, will send only nonwallet part
            // to promoservice
            if (payOptionSize > 1 && PayMethod.BALANCE.equals(promoPaymentOption.getPayMethod())) {
                continue;
            }
            /*
             * if (isCCDCPaymethod(promoPaymentOption.getPayMethod()) &&
             * ff4JUtil.isFeatureEnabledForPromo(mid)) { String paytmUserId =
             * requestBody.getPaytmUserId(); String merchantCoftConfig =
             * coftTokenDataService.getMerchantConfig(mid); boolean
             * bin8Available = isBin8Available(mid, paytmUserId);
             * 
             * if (StringUtils.isNotEmpty(promoPaymentOption.getSavedCardId()))
             * { processForCoftSavedCardId(promoPaymentOption, mid,
             * merchantCoftConfig, txnToken, bin8Available); } else if
             * (StringUtils.isNotEmpty(promoPaymentOption.getCardNo())) {
             * processForCoftCardNumber(promoPaymentOption, mid, bin8Available,
             * merchantCoftConfig); } else if (null !=
             * promoPaymentOption.getCardTokenInfo()) {
             * processForCoftTokenCards(promoPaymentOption, mid,
             * merchantCoftConfig, bin8Available); } else { throw
             * RequestValidationException.getException(); } }
             */

            ApplyPromoPaymentOptionBuilder paymentOptionBuilder = ApplyPromoPaymentOptionBuilderFactory
                    .getApplyPromoPaymentOptionBuilder(promoPaymentOption.getPayMethod(),
                            requestBody.isPromoForPCFMerchant());
            promoPaymentOption.setPromoContext(requestBody.getPromoContext());
            paymentOptions.add(paymentOptionBuilder.buildForCoftPromoTxns(promoPaymentOption, mid, txnToken));

        }
        return paymentOptions;
    }

    public boolean isBin8Available(String mid, String paytmUserId, Map<String, String> promoContext) {
        boolean migrateBankOffersPromo = ff4JUtil.isMigrateBankOffersPromo(mid);
        if (migrateBankOffersPromo) {
            return isBin8OfferAvailableOnMerchant(mid, paytmUserId, promoContext);
        } else {
            return paymentOffersServiceHelper.isBin8OfferAvailableOnMerchant(mid, paytmUserId);
        }
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

        if (isBin8OfferAvailableOnMerchant(mid, paytmUserId, promoPaymentOption.getPromoContext())) {
            setBin8Hash(promoPaymentOption, cardNo, cardIndexNumber);
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
                promoPaymentOption.setCardNo(response.getCardInfo().getCardBin());
            }
        } else {

            /**
             * Fetching CIN (CardHash) by calling cache card token API 8 bin
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
            if (isBin8OfferAvailableOnMerchant(mid, paytmUserId, promoPaymentOption.getPromoContext())) {
                setBin8Hash(promoPaymentOption, cardNumber, CIN);
            }

        }
    }

    public void processForCoftSavedCardId(PromoPaymentOption promoPaymentOption, String mid, String merchantCoftConfig,
            String txnToken, boolean eightBinHashSupported) {
        {
            String savedCardId = promoPaymentOption.getSavedCardId();

            // case where TIN has been send in the saved card field
            if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                getSavedCardsFromFPOForTin(merchantCoftConfig, promoPaymentOption, savedCardId, txnToken,
                        eightBinHashSupported);
                if (StringUtils.isEmpty(promoPaymentOption.getUniquePromoIdentifier())) {
                    String savedId = coftTokenDataService.getTokenData(mid, savedCardId, "TIN", merchantCoftConfig);
                    if (StringUtils.isNotEmpty(savedId)) {
                        promoPaymentOption.setUniquePromoIdentifier(savedId);
                        setBinDetailsForTIN(mid, savedCardId, promoPaymentOption, eightBinHashSupported);
                    } else {
                        LOGGER.error("Unable to fetch Saved Card ID for TIN");
                        throw BaseException.getException();
                    }
                }
            } else if (savedCardId.length() > 15) {
                getSavedCardsFromFPOForCIN(merchantCoftConfig, promoPaymentOption, savedCardId, txnToken,
                        eightBinHashSupported);
                if (StringUtils.isEmpty(promoPaymentOption.getUniquePromoIdentifier())) {
                    QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                            .queryNonSensitiveAssetInfo(null, savedCardId);
                    String savedId = coftTokenDataService.getSavedCardIdFromCardIndexNumber(mid, merchantCoftConfig,
                            savedCardId, queryNonSensitiveAssetInfoResponse);
                    if (StringUtils.isNotEmpty(savedId)) {
                        promoPaymentOption.setUniquePromoIdentifier(savedId);
                        setBinDetailsForCIN(queryNonSensitiveAssetInfoResponse, promoPaymentOption,
                                eightBinHashSupported);
                    } else {
                        LOGGER.error("Unable to fetch Saved Card ID for CIN");
                        throw BaseException.getException();
                    }
                }
            }

        }
    }

    public void processForCoftCardNumber(PromoPaymentOption promoPaymentOption, String mid, boolean bin8Available,
            String merchantCoftConfig) {
        String cardNo = promoPaymentOption.getCardNo();
        String savedId = coftTokenDataService.getSavedCardIdFromCardNumber(mid, cardNo, merchantCoftConfig);
        if (StringUtils.isNotEmpty(savedId)) {
            promoPaymentOption.setUniquePromoIdentifier(savedId);
        } else {
            LOGGER.error("Unable to fetch Saved Card ID for Card Number");
            throw BaseException.getException();
        }
        /**
         * Calling Platform getBinHash API for 8 bin alias and saving it in
         * cache which is required at checkout promo.
         */
        if (bin8Available) {
            setBin8Hash(promoPaymentOption, cardNo, savedId);
        }
    }

    // response.getData() will be empty if no offers exists and that will be
    // valid response
    private boolean isValidSearchPaymentOffersServiceResponse(SearchPaymentOffersServiceResponseV2 response) {
        // return !(response == null || (response.getData() == null &&
        // CollectionUtils.isEmpty(response.getErrors())));
        return !(response == null || (response.getItems() == null));
    }

    public FetchAllPaymentOffersResponse prepareResponse(SearchPaymentOffersServiceResponseV2 promoResp,
            FetchAllPaymentOffersRequest request, String simplifiedPromoCode) {
        FetchAllPaymentOffersResponse apiResponse = new FetchAllPaymentOffersResponse();
        apiResponse.setHead(PaymentOfferUtils.createResponseHeader());
        apiResponse.setBody(new FetchPaymentOffersResponseBody());
        apiResponse.getHead().setRequestId(request.getHead().getRequestId());
        apiResponse.getBody().setPaymentOffers(preparePaymentOffers(promoResp, simplifiedPromoCode));
        return apiResponse;
    }

    private boolean isValidSearchPaymentOffersServiceResponseV2(SearchPaymentOffersServiceResponseV2 promoResp) {
        if (promoResp != null && CollectionUtils.isNotEmpty(promoResp.getItems())
                && promoResp.getItems().get(0) != null && promoResp.getItems().get(0).getData() != null) {
            return true;
        }
        return false;
    }

    public List<PaymentOffersData> preparePaymentOffers(SearchPaymentOffersServiceResponseV2 promoResp,
            String simplifiedPromoCode) {
        if (promoResp == null || CollectionUtils.isEmpty(promoResp.getItems()) || promoResp.getItems().get(0) == null
                || CollectionUtils.isEmpty(promoResp.getItems().get(0).getData())) {
            return Collections.emptyList();
        }

        List<SearchPaymentOffersResponseDataV2> searchPaymentOffersResponseData = promoResp.getItems().get(0).getData();

        if (StringUtils.isNotBlank(simplifiedPromoCode)) {
            searchPaymentOffersResponseData = searchPaymentOffersResponseData.parallelStream().filter(Objects::nonNull)
                    .filter(p -> (p.getPromocode().equalsIgnoreCase(simplifiedPromoCode))).collect(Collectors.toList());
        }

        List<PaymentOffersData> paymentOffersData = new ArrayList<>(searchPaymentOffersResponseData.size());

        for (SearchPaymentOffersResponseDataV2 offersResponseData : searchPaymentOffersResponseData) {
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

    public Map<String, String> prepareQueryParamsV2(String mid, String paytmUserId, boolean enablePromoUserSegmentation) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("X-CLIENT-ID", mid);
        queryParams.put("X-CLIENT", "PG");
        if (paytmUserId != null && enablePromoUserSegmentation) {
            queryParams.put("paytm-user-id", paytmUserId);
        }
        return queryParams;
    }

    public ApplyPromoResponse prepareResponse(PromoServiceResponseBase baseResponse, ApplyPromoRequest request,
            String version) {
        ApplyPromoServiceResponseV2 serviceResponse = (ApplyPromoServiceResponseV2) baseResponse;
        ApplyPromoResponse apiResponse = new ApplyPromoResponse();
        apiResponse.setHead(PaymentOfferUtils.createResponseHeader());
        apiResponse.setBody(new ApplyPromoResponseBody());
        apiResponse.getHead().setRequestId(request.getHead().getRequestId());
        if (isPromoServiceSuccessResponse(serviceResponse)) {
            if (serviceResponse.getData() != null) {
                if (com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2.equalsIgnoreCase(version)) {
                    apiResponse.getHead().setVersion(
                            com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2);
                    if (serviceResponse.getData().getStatus() == 1) {
                        // LOGGER.info("promo apply/validation successful response received for version v2");
                        apiResponse.getBody().setPaymentOffer(getPaymentOffer(serviceResponse, request.getBody()));
                        if (request.getBody().getCartDetails() != null) {
                            apiResponse.getBody().getPaymentOffer()
                                    .setCartOfferDetail(getCartOfferDetail(serviceResponse, request.getBody()));
                            apiResponse.getBody().getPaymentOffer().setCartDetails(request.getBody().getCartDetails());
                        }
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

    private PromoCartOfferDetail getCartOfferDetail(ApplyPromoServiceResponseV2 serviceResponse,
            ApplyPromoRequestBody body) {
        PromoCartOfferDetail cartOfferDetail = new PromoCartOfferDetail();
        List<PromoItemOffer> itemOffers = new ArrayList<>();
        for (Map.Entry<String, PromoResponseData> entry : serviceResponse.getData().getPromoResponse().entrySet()) {
            PromoItemOffer item = new PromoItemOffer();
            item.setPromocode(entry.getKey());
            item.setItems(getItemOfferDetail(entry.getValue().getItems()));
            itemOffers.add(item);

        }
        cartOfferDetail.setItemOffers(itemOffers);
        return cartOfferDetail;
    }

    private List<PromoItemOfferDetail> getItemOfferDetail(Map<String, Items> items) {
        List<PromoItemOfferDetail> offerDetails = new ArrayList<>();
        for (Map.Entry<String, Items> itemsEntry : items.entrySet()) {
            PromoItemOfferDetail offerDetail = new PromoItemOfferDetail();
            offerDetail.setId(itemsEntry.getKey());
            offerDetail.setMetaData(getMetaData(itemsEntry.getValue()));
            offerDetails.add(offerDetail);
        }
        return offerDetails;

    }

    private List<PromoItemUsageData> getMetaData(Items value) {
        List<PromoItemUsageData> metaData = new ArrayList<>();
        for (PromoUsageData usageData : value.getUsage_data()) {
            if (usageData != null) {
                PromoItemUsageData itemUsageData = new PromoItemUsageData();
                itemUsageData.setAmount(usageData.getAmount());
                itemUsageData.setCampaign(usageData.getCampaign());
                itemUsageData.setPromocode(usageData.getPromocode());
                itemUsageData.setFlags(usageData.getFlags());
                itemUsageData.setCustomText(usageData.getCustom_text());
                itemUsageData.setFraud1(usageData.getFraud1());
                itemUsageData.setPromoGratificationData(usageData.getPromoGratificationData());
                itemUsageData.setFulfillmentStatus(usageData.getFulfillmentStatus());
                itemUsageData.setSiteId(usageData.getSiteId());
                itemUsageData.setUserId(usageData.getUserId());
                itemUsageData.setPromocodeId(usageData.getPromocodeId());
                itemUsageData.setStatus(usageData.getStatus());
                itemUsageData.setRedemptionType(usageData.getRedemptionType());
                metaData.add(itemUsageData);
            }
        }
        return metaData;
    }

    public ApplyItemLevelPromoResponse prepareResponse(PromoServiceResponseBase baseResponse, ApplyPromoRequest request) {
        ApplyPromoServiceResponseV2 serviceResponse = (ApplyPromoServiceResponseV2) baseResponse;
        ApplyItemLevelPromoResponse apiResponse = new ApplyItemLevelPromoResponse();
        apiResponse.setHead(PaymentOfferUtils.createResponseHeader());
        apiResponse.setBody(new ApplyItemLevelPromoResponseBody());
        apiResponse.getHead().setRequestId(request.getHead().getRequestId());
        if (isPromoServiceSuccessResponse(serviceResponse)) {
            if (serviceResponse.getData() != null) {
                apiResponse.getHead().setVersion(
                        com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V1);
                apiResponse.getBody().setPaymentOffer(getItemLevelPaymentOffer(serviceResponse));
            } else {
                LOGGER.error("promo apply/validation invalid response, status is 1 but data is null");
                throw BaseException.getException(serviceResponse.getError() != null ? serviceResponse.getError()
                        .getMessage() : "Error in getting promo details");
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

    private ItemLevelPaymentOffer getItemLevelPaymentOffer(ApplyPromoServiceResponseV2 serviceResponse) {
        ItemLevelPaymentOffer paymentOffer = new ItemLevelPaymentOffer();
        ApplyPromoResponseDataV2 responseData = serviceResponse.getData();
        paymentOffer.setPayText(responseData.getPayText());
        paymentOffer.setPromoCode(responseData.getPromocode());
        paymentOffer.setEffectivePromoDeduction(PaymentOfferUtils.getAmountInRupees(responseData
                .getEffectivePromoDeduction()));
        paymentOffer
                .setEffectivePromoSaving(PaymentOfferUtils.getAmountInRupees(responseData.getEffectivePromoSaving()));
        paymentOffer.setPrePromoText(responseData.getPrePromoText());
        paymentOffer.setPromotext(responseData.getPromotext());
        paymentOffer.setPromoVisibility(Boolean.valueOf(responseData.isPromoVisibility()).toString());
        paymentOffer.setStatus(responseData.getStatus());
        paymentOffer.setTncUrl(responseData.getTncUrl());
        paymentOffer.setVerificationCode(responseData.getVerificationCode());
        paymentOffer.setResponseCode(responseData.getResponseCode());
        if (responseData.getPromoContext() != null) {
            paymentOffer.setPromoContext(responseData.getPromoContext());
        }
        if (CollectionUtils.isNotEmpty(responseData.getSavings())) {
            List<ItemLevelPaymentOffer.PromoSaving> savings = new ArrayList<>();
            for (PromoSaving saving : responseData.getSavings()) {
                savings.add(new ItemLevelPaymentOffer.PromoSaving(PaymentOfferUtils.getAmountInRupees(saving
                        .getSavings()), saving.getRedemptionType()));
            }
            paymentOffer.setSavings(savings);
        }
        return paymentOffer;
    }

    public PaymentOffer getPaymentOffer(ApplyPromoServiceResponseV2 serviceResponse, ApplyPromoRequestBody requestBody) {
        PaymentOffer paymentOffer = new PaymentOffer();
        paymentOffer.setOfferBreakup(getPromoOfferDetails(serviceResponse.getData(), requestBody.getPaymentOptions(),
                requestBody.getChangeRedemptionTypetoCashBack()));
        // Hybrid is not supported currently on promoservice, total will be same
        paymentOffer.setTotalCashbackAmount(paymentOffer.getOfferBreakup().get(0).getCashbackAmount());
        paymentOffer.setTotalInstantDiscount(paymentOffer.getOfferBreakup().get(0).getInstantDiscount());
        paymentOffer.setTotalTransactionAmount(requestBody.getTotalTransactionAmount());
        paymentOffer.setTotalPaytmCashbackAmount(paymentOffer.getOfferBreakup().get(0).getPaytmCashbackAmount());
        paymentOffer.setEncUserId(requestBody.getEncUserId());
        if (ff4JUtil.isFeatureEnabled(APPLY_PROMO_SEND_RESPONSE_TNC_URL, requestBody.getMid())) {
            paymentOffer.setTncUrl(serviceResponse.getData().getTncUrl());
        }
        return paymentOffer;
    }

    private List<String> transformPromoResponseV2(ApplyPromoResponseDataV2 applyPromoResponseDataV2) {
        List<String> applicabletenures = new ArrayList<>();
        Map<String, PromoResponseData> promoResponse = applyPromoResponseDataV2.getPromoResponse();
        if (promoResponse != null) {
            for (String key : promoResponse.keySet()) {
                PromoResponseData promoResponseData = promoResponse.get(key);
                List<Integer> applicableTenuresInInt = new ArrayList<>();
                if (null != promoResponseData && null != promoResponseData.getOfferMeta()) {
                    applicableTenuresInInt = promoResponseData.getOfferMeta().getApplicableTenures();
                }
                if (null != applicableTenuresInInt) {
                    for (Integer tenures : applicableTenuresInInt) {
                        applicabletenures.add(String.valueOf(tenures));
                    }
                }
            }
        }
        return applicabletenures;
    }

    // Hybrid is not supported currently, promo will only provide nonwallet
    // payment offer details
    public List<PaymentOfferDetails> getPromoOfferDetails(ApplyPromoResponseDataV2 responseData,
            List<PromoPaymentOption> paymentOptions, Boolean changeRedemptionTypetoCashBack) {
        List<PaymentOfferDetails> toRet = new ArrayList<>();
        PaymentOfferDetails paymentOfferDetails = new PaymentOfferDetails();
        paymentOfferDetails.setPromotext(responseData.getPromotext());
        paymentOfferDetails.setPromoVisibility(String.valueOf(responseData.isPromoVisibility()));
        if (responseData.getPromoResponse() != null) {
            for (Map.Entry<String, PromoResponseData> entry : responseData.getPromoResponse().entrySet()) {
                paymentOfferDetails.setPromocodeApplied(entry.getKey());
                if (entry.getValue() != null && MapUtils.isNotEmpty(entry.getValue().getItems())) {
                    if (!responseData.getSavings().isEmpty()) {
                        for (PromoSaving promoSaving : responseData.getSavings()) {
                            if (RedemptionType.CASHBACK.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                                paymentOfferDetails.setCashbackAmount(PaymentOfferUtils.getAmountInRupees(promoSaving
                                        .getSavings()));
                            }
                            if (RedemptionType.DISCOUNT.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                                paymentOfferDetails.setInstantDiscount(PaymentOfferUtils.getAmountInRupees(promoSaving
                                        .getSavings()));
                            }
                            if (RedemptionType.PAYTM_CASHBACK.getType().equalsIgnoreCase(
                                    promoSaving.getRedemptionType())) {
                                paymentOfferDetails.setPaytmCashbackAmount(PaymentOfferUtils
                                        .getAmountInRupees(promoSaving.getSavings()));
                            }
                        }
                    } else if (entry.getValue().getItems().get(CONST_ITEM) != null) {
                        Items items = entry.getValue().getItems().get(CONST_ITEM);
                        if (CollectionUtils.isNotEmpty(items.getUsage_data())) {
                            PromoUsageData promoUsageData = items.getUsage_data().get(0);

                            if (BooleanUtils.isTrue(changeRedemptionTypetoCashBack)) {
                                LOGGER.info("Changing Redemption Type");
                                paymentOfferDetails.setCashbackAmount(getSavingAmount(promoUsageData,
                                        RedemptionType.PAYTM_CASHBACK));
                                paymentOfferDetails.setInstantDiscount(getSavingAmount(promoUsageData,
                                        RedemptionType.DISCOUNT));
                                paymentOfferDetails.setPaytmCashbackAmount(getSavingAmount(promoUsageData,
                                        RedemptionType.CASHBACK));
                            } else {
                                paymentOfferDetails.setCashbackAmount(getSavingAmount(promoUsageData,
                                        RedemptionType.CASHBACK));
                                paymentOfferDetails.setInstantDiscount(getSavingAmount(promoUsageData,
                                        RedemptionType.DISCOUNT));
                                paymentOfferDetails.setPaytmCashbackAmount(getSavingAmount(promoUsageData,
                                        RedemptionType.PAYTM_CASHBACK));
                            }
                        }
                    }

                }
            }
        }
        if (responseData.getResponseCode() != null) {
            paymentOfferDetails.setResponseCode(responseData.getResponseCode());
        }
        paymentOfferDetails.setTransactionAmount(getPayModeSpecificTransactionAmount(paymentOptions));
        paymentOfferDetails.setPayMethod(getNonWalletPayMethod(paymentOptions));
        if (CollectionUtils.isNotEmpty(paymentOptions)) {
            for (PromoPaymentOption promoPaymentOption : paymentOptions) {
                if (promoPaymentOption != null && StringUtils.isNotEmpty(promoPaymentOption.getTenure())) {
                    paymentOfferDetails.setApplicableTenures(transformPromoResponseV2(responseData));
                }
            }
        }
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

    private String getSavingAmount(PromoUsageData promoUsageData, RedemptionType redemptionType) {
        if (promoUsageData == null)
            return null;
        Optional<RedemptionType> redemptionTypeOptional = RedemptionType.fromString(promoUsageData.getRedemptionType());
        if (redemptionTypeOptional.isPresent() && redemptionTypeOptional.get() == redemptionType) {
            return PaymentOfferUtils.getAmountInRupees(promoUsageData.getAmount());
        }
        return null;
    }

    public boolean isPromoApplied(ApplyPromoResponseData data) {
        return data != null && data.getStatus() == 1;
    }

    public BulkApplyPromoServiceRequestV2 prepareApplyPromoServiceRequest(
            NativeCashierInfoRequest nativeCashierInfoRequest, NativeCashierInfoResponse cashierInfoResponse,
            WorkFlowResponseBean workFlowResponseBean, boolean offerOnTotalAmount, String custId, String paytmUserId) {
        List<CardBeanBiz> savedCards = null;
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        if (userDetailsBiz != null) {
            savedCards = userDetailsBiz.getMerchantViewSavedCardsList();
        }

        BulkApplyPromoServiceRequestV2 applyPromoServiceRequest = new BulkApplyPromoServiceRequestV2();
        Long totalAmount;
        String amountInRupees;
        String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
        boolean isEnableGcinOnCoftPromo = ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GCIN_ON_COFT_PROMO, false);

        if (offerOnTotalAmount) {
            totalAmount = getNonHybridTxnAmountInPaise(nativeCashierInfoRequest, cashierInfoResponse);
            amountInRupees = getNonHybridTxnAmountInRupees(nativeCashierInfoRequest, cashierInfoResponse);
        } else {
            totalAmount = nonWalletTxnAmountInPaiseIfHybrid(nativeCashierInfoRequest, cashierInfoResponse);
            amountInRupees = nonWalletTxnAmountIfHybrid(nativeCashierInfoRequest, cashierInfoResponse);
        }
        if (nativeCashierInfoRequest.getBody() != null
                && nativeCashierInfoRequest.getBody().getApplyItemOffers() != null) {
            if (nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext() != null) {
                applyPromoServiceRequest.setPromoContext(nativeCashierInfoRequest.getBody().getApplyItemOffers()
                        .getPromoContext());
            } else if (nativeCashierInfoRequest.getBody().getApplyItemOffers().getCartDetails() != null) {
                Cart cart = new Cart();
                HashMap<String, Item> cartMap = new HashMap<>();
                for (PromoItemDetail item : nativeCashierInfoRequest.getBody().getApplyItemOffers().getCartDetails()
                        .getItems()) {
                    cartMap.put(item.getId(), getItem(item));
                }
                cart.setItems(cartMap);
                applyPromoServiceRequest.setCart(cart);
            }
        } else {
            applyPromoServiceRequest.setCart(getCartDetails(totalAmount));
        }
        applyPromoServiceRequest.setUser(getUserDetails(custId, paytmUserId));
        applyPromoServiceRequest.setClientDetails(getClientDetails(nativeCashierInfoRequest.getHead().getChannelId()
                .getValue()));
        PaymentDetailsBulk paymentDetailsBulk = new PaymentDetailsBulk();
        paymentDetailsBulk.setTotalTransactionAmount(totalAmount);
        paymentDetailsBulk.setPaymentOptionsBulk(new ArrayList<>());
        List<PaymentOptionBulk> paymentOptionBulkList = paymentDetailsBulk.getPaymentOptionsBulk();
        List<PayChannelBase> merchantSavedInstruments = cashierInfoResponse.getBody().getMerchantPayOption()
                .getSavedInstruments();
        for (PayChannelBase savedInstrument : merchantSavedInstruments) {

            if (!isEnableGcinOnCoftPromo && savedInstrument instanceof SavedCard
                    && ((SavedCard) savedInstrument).isCardCoft()) {
                continue;
            }
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
                    PaymentOption paymentOption = null;
                    if (!isEnableGcinOnCoftPromo) {
                        paymentOption = paymentOptionBuilder.build(promoPaymentOption, nativeCashierInfoRequest
                                .getBody().getMid());
                    } else {
                        paymentOption = paymentOptionBuilder.buildForCoftPromoTxns(promoPaymentOption,
                                nativeCashierInfoRequest.getBody().getMid(), null);
                    }
                    paymentOptionBulk.getPaymentOptions().add(paymentOption);
                }
            } else {
                LOGGER.error("Paymethod = {} is not valid in savedInstrument", savedInstrument.getPayMethod());
            }
            paymentOptionBulkList.add(paymentOptionBulk);
        }
        applyPromoServiceRequest.setPaymentDetailsBulk(paymentDetailsBulk);
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

    public BulkApplyPromoServiceResponseV2 bulkApplyPromoV2(
            BulkApplyPromoServiceRequestV2 bulkApplyPromoServiceRequest, String mid, String custId, String orderId,
            String paytmUserId) {
        try {
            return paymentPromoSevice.bulkApplyPromoV2(bulkApplyPromoServiceRequest,
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
        String merchantCoftConfig = coftTokenDataService.getMerchantConfig(mid);
        boolean isEnableGcinOnCoftPromo = ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GCIN_ON_COFT_PROMO, false);
        String savedId = null;

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

                        if (!isEnableGcinOnCoftPromo) {
                            if (cardId.length() > 15) {
                                paymentOption.setSavedCardId(cardId);
                                paymentOption.setEightDigitBinHash(card.getEightDigitBinHash());

                                /**
                                 * In case of CIN, cardNumber is masked, So to
                                 * find bin6 while creating applybulk request,
                                 * PG will set First6Dgit in cardNumber.
                                 */

                                paymentOption.setCardNo(String.valueOf(card.getFirstSixDigit()));
                            } else if (ff4JUtil.isFeatureEnabledForPromo(mid)) {
                                PayCardOptionViewBiz payCardOptionViewBiz = getSavedCardInfoFromLitePayViewResponse(
                                        MerchantLitePayviewConsultResponseBizBean,
                                        AddNPayLitePayviewConsultResponseBizBean, card.getCardNumber(),
                                        card.getExpiryDate());
                                if (null != payCardOptionViewBiz) {
                                    paymentOption.setSavedCardId(payCardOptionViewBiz.getCardIndexNo());
                                    paymentOption.setEightDigitBinHash(payCardOptionViewBiz.getExtendInfo().get(
                                            (TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH)));
                                }
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

                        } else {
                            paymentOption.setEightDigitBinHash(card.getEightDigitBinHash());
                            if (savedCard.isCardCoft()) {
                                if (merchantCoftConfig.equals("PAR")) {
                                    paymentOption.setUniquePromoIdentifier(card.getPar());
                                } else if (merchantCoftConfig.equals("GCIN")) {
                                    paymentOption.setUniquePromoIdentifier(card.getGcin());
                                }
                                if (StringUtils.isEmpty(paymentOption.getUniquePromoIdentifier())) {
                                    LOGGER.error("Unable to fetch saved card id from Platform");
                                    throw BaseException.getException();
                                }
                                setPaymentOptionForBulkApply(paymentOption, card, cardId);
                            } else if (cardId.length() > 15) {
                                if (merchantCoftConfig.equals("GCIN")) {
                                    paymentOption.setUniquePromoIdentifier(card.getGcin());
                                }
                                if (StringUtils.isEmpty(paymentOption.getUniquePromoIdentifier())) {
                                    LOGGER.error("Unable to fetch saved card id from Platform");
                                    throw BaseException.getException();
                                }
                                setPaymentOptionForBulkApply(paymentOption, card, cardId);

                                /**
                                 * In case of CIN, cardNumber is masked, So to
                                 * find bin6 while creating applybulk request,
                                 * PG will set First6Dgit in cardNumber.
                                 */

                            } else if (ff4JUtil.isFeatureEnabledForPromo(mid)) {
                                PayCardOptionViewBiz payCardOptionViewBiz = getSavedCardInfoFromLitePayViewResponse(
                                        MerchantLitePayviewConsultResponseBizBean,
                                        AddNPayLitePayviewConsultResponseBizBean, card.getCardNumber(),
                                        card.getExpiryDate());
                                if (null != payCardOptionViewBiz) {
                                    if (merchantCoftConfig.equals("PAR")) {
                                        paymentOption.setUniquePromoIdentifier(payCardOptionViewBiz.getPar());
                                    } else if (merchantCoftConfig.equals("GCIN")) {
                                        paymentOption.setUniquePromoIdentifier(payCardOptionViewBiz.getGcin());
                                    }
                                    if (StringUtils.isEmpty(paymentOption.getUniquePromoIdentifier())) {
                                        LOGGER.error("Unable to fetch saved card id from Platform");
                                        throw BaseException.getException();
                                    }
                                    paymentOption.setEightDigitBinHash(payCardOptionViewBiz.getExtendInfo().get(
                                            (TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH)));
                                    setPaymentOptionForBulkApply(paymentOption, card, cardId);
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

    private void setBin8Hash(PromoPaymentOption promoPaymentOption, String cardNumber, String savedCardId) {
        CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNumber.substring(0, 8));
        if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
            String eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
            nativeSessionUtil.cacheEightDigitBinHash(PaymentOfferUtils.getApplyPromoForCachedKey(savedCardId),
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

    public SearchPaymentOffersServiceResponseV2 preparePaymentOffersV2(SearchPaymentOffersServiceResponseV2 promoResp,
            String simplifiedPromoCode) {
        if (promoResp == null || CollectionUtils.isEmpty(promoResp.getItems()) || promoResp.getItems().get(0) == null
                || CollectionUtils.isEmpty(promoResp.getItems().get(0).getData())) {
            return null;
        }

        if (StringUtils.isNotBlank(simplifiedPromoCode)) {
            List<SearchPaymentOffersResponseDataV2> searchPaymentOffersResponseData = null;
            for (int i = 0; i < promoResp.getItems().size(); i++) {
                searchPaymentOffersResponseData = promoResp.getItems().get(i).getData();
                promoResp
                        .getItems()
                        .get(i)
                        .setData(
                                searchPaymentOffersResponseData.parallelStream().filter(Objects::nonNull)
                                        .filter(p -> (p.getPromocode().equalsIgnoreCase(simplifiedPromoCode)))
                                        .collect(Collectors.toList()));
            }
        }

        return promoResp;
    }

    public FetchUserIdResponse getVpaValidateResponse(FetchUserIdRequest request, String referenceId) throws Exception {

        VpaValidateRequest vpaValidateRequest = new VpaValidateRequest();

        vpaValidateRequest.setHead(request.getHead());

        vpaValidateRequest.setBody(getVPAValidateRequestBody(request, referenceId));

        IRequestProcessor<VpaValidateRequest, ValidateVpaResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.VALIDATE_VPA_REQUEST);

        ValidateVpaResponse validateVpaResponse = requestProcessor.process(vpaValidateRequest);

        return prepareFetchUserIdResponse(validateVpaResponse);

    }

    private VpaValidateRequestBody getVPAValidateRequestBody(FetchUserIdRequest request, String referenceId) {

        VpaValidateRequestBody vpaValidateRequestBody = new VpaValidateRequestBody();
        vpaValidateRequestBody.setMid(request.getBody().getMid());
        vpaValidateRequestBody.setVpa(request.getBody().getVpa());
        vpaValidateRequestBody.setReferenceId(referenceId);

        return vpaValidateRequestBody;

    }

    private FetchUserIdResponse prepareFetchUserIdResponse(ValidateVpaResponse validateVpaResponse)
            throws SecurityException {

        FetchUserIdResponse response = new FetchUserIdResponse();

        FetchUserIdResponseBody body = new FetchUserIdResponseBody();

        try {
            body.setEncUserId(CryptoUtils.encryptAES(validateVpaResponse.getBody().getCustId()));
        } catch (Exception e) {
            throw RequestValidationException.getException("Tampered EncCustId, Couldn't decrypted");
        }
        body.setResponseMsg("SUCCESS");

        response.setHead(PaymentOfferUtils.createResponseHeader());

        response.setBody(body);

        return response;

    }

    private void populateEncUserIdInApplyPromoRequest(String encUserId, ApplyPromoRequest request, String referenceId) {
        if (StringUtils.isNotBlank(encUserId)) {
            return;
        }

        if (CollectionUtils.isNotEmpty(request.getBody().getPaymentOptions())) {
            PromoPaymentOption promoPaymentOption = request.getBody().getPaymentOptions().get(0);
            if (StringUtils.isNotBlank(promoPaymentOption.getVpa())) {
                String vpa = promoPaymentOption.getVpa();
                String vpaArray[] = vpa.split("@");
                if (!(vpaArray.length == 2 && TheiaConstant.ExtraConstants.TYPE.equals(vpaArray[1]))) {
                    return;
                }
                FetchUserIdRequest fetchUserIdRequest = new FetchUserIdRequest();
                TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
                tokenRequestHeader.setToken(request.getHead().getToken());
                tokenRequestHeader.setTokenType(request.getHead().getTokenType());
                tokenRequestHeader.setTxnToken(request.getHead().getTxnToken());
                fetchUserIdRequest.setHead(tokenRequestHeader);
                FetchUserIdRequestBody fetchUserIdRequestBody = new FetchUserIdRequestBody();
                fetchUserIdRequestBody.setMid(request.getBody().getMid());
                fetchUserIdRequestBody.setVpa(promoPaymentOption.getVpa());
                fetchUserIdRequest.setBody(fetchUserIdRequestBody);
                try {
                    FetchUserIdResponse fetchUserIdResponse = getVpaValidateResponse(fetchUserIdRequest, referenceId);
                    request.getBody().setEncUserId(fetchUserIdResponse.getBody().getEncUserId());
                } catch (Exception e) {
                    LOGGER.error("Error while getting enc user id corresponding to vpa :{}", e.getMessage());
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);

                }
            }
        }
    }

    public void setBinDetailsForTIN(String mid, String savedCardId, PromoPaymentOption promoPaymentOption,
            boolean eightBinHashSupported) {
        String tokenBin = coftTokenDataService.fetchTokenDetail(mid, savedCardId);
        if (StringUtils.isNotEmpty(tokenBin)) {
            BinDetail binDetail = coftTokenDataService.getCardBinDetails(tokenBin);
            setBinDetailsForToken(binDetail, promoPaymentOption, eightBinHashSupported);
        }
    }

    public void setBinDetailsForCIN(QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse,
            PromoPaymentOption promoPaymentOption, boolean eightBinHashSupported) {
        if (null != queryNonSensitiveAssetInfoResponse) {
            if (eightBinHashSupported) {
                String eightDigitBinHash = queryNonSensitiveAssetInfoResponse.getCardInfo().getExtendInfo()
                        .get(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
                nativeSessionUtil.cacheEightDigitBinHash(
                        PaymentOfferUtils.getApplyPromoForCachedKey(promoPaymentOption.getUniquePromoIdentifier()),
                        TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH, eightDigitBinHash);
                promoPaymentOption.setEightDigitBinHash(eightDigitBinHash);
            }
            promoPaymentOption.setCardNo(queryNonSensitiveAssetInfoResponse.getCardInfo().getCardBin());
        }
    }

    public void getSavedCardsFromFPOForTin(String merchantCoftConfig, PromoPaymentOption promoPaymentOption,
            String savedCardId, String txnToken, boolean eightBinHashSupported) {
        if (null != txnToken) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
            SavedCard savedCard = coftTokenDataService.fetchTokenDataFromFPO(savedCardId, cashierInfoResponse);
            if (null != savedCard) {
                if (merchantCoftConfig.equals("PAR")) {
                    promoPaymentOption.setUniquePromoIdentifier(savedCard.getPar());
                } else if (merchantCoftConfig.equals("GCIN")) {
                    promoPaymentOption.setUniquePromoIdentifier(savedCard.getGcin());
                }

                if (StringUtils.isNotEmpty(promoPaymentOption.getUniquePromoIdentifier())) {
                    promoPaymentOption.setCardNo(savedCard.getCardDetails().getFirstSixDigit());
                    if (eightBinHashSupported) {
                        String accountRangeCardBin = savedCard.getAccountRangeCardBin();
                        if (StringUtils.isNotEmpty(accountRangeCardBin)) {
                            setBin8Hash(promoPaymentOption, accountRangeCardBin,
                                    promoPaymentOption.getUniquePromoIdentifier());
                        }
                    }
                }
            }
        }
    }

    public void getSavedCardsFromFPOForCIN(String merchantCoftConfig, PromoPaymentOption promoPaymentOption,
            String savedCardId, String txnToken, boolean eightBinHashSupported) {
        if (null != txnToken) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
            SavedCard savedCard = coftTokenDataService.fetchTokenDataFromFPO(savedCardId, cashierInfoResponse);
            if (null != savedCard) {
                if (merchantCoftConfig.equals("PAR")) {
                    promoPaymentOption.setUniquePromoIdentifier(savedCard.getPar());
                } else if (merchantCoftConfig.equals("GCIN")) {
                    promoPaymentOption.setUniquePromoIdentifier(savedCard.getGcin());
                }

                if (StringUtils.isNotEmpty(promoPaymentOption.getUniquePromoIdentifier())) {
                    promoPaymentOption.setCardNo(savedCard.getCardDetails().getFirstSixDigit());
                    if (eightBinHashSupported) {
                        String eightDigitBinHash = savedCard.getAccountRangeCardBin();
                        if (StringUtils.isNotEmpty(eightDigitBinHash)) {
                            nativeSessionUtil.cacheEightDigitBinHash(PaymentOfferUtils
                                    .getApplyPromoForCachedKey(promoPaymentOption.getUniquePromoIdentifier()),
                                    TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH, eightDigitBinHash);
                            promoPaymentOption.setEightDigitBinHash(eightDigitBinHash);
                        }
                    }
                }
            }
        }
    }

    public void processForCoftTokenCards(PromoPaymentOption promoPaymentOption, String mid, String merchantCoftConfig,
            boolean bin8Available) {
        CardTokenInfo cardTokenInfo = promoPaymentOption.getCardTokenInfo();
        if (StringUtils.isBlank(cardTokenInfo.getCardToken())) {
            throw RequestValidationException.getException();
        }
        BinDetail binDetail = coftTokenDataService.getCardBinDetails(cardTokenInfo.getCardToken());
        if (binDetail != null && DINERS.getName().equalsIgnoreCase(binDetail.getCardName())) {
            LOGGER.error("For DINERS scheme third-party card token transactions, uniquePromoIdentifier is not available");
            throw BaseException.getException();
        } else {
            if (StringUtils.isBlank(cardTokenInfo.getPanUniqueReference())) {
                throw RequestValidationException.getException();
            }
            if (merchantCoftConfig.equals("PAR")) {
                promoPaymentOption.setUniquePromoIdentifier(cardTokenInfo.getPanUniqueReference());
            } else {
                String uniqueIdentifier = coftTokenDataService.getTokenData(mid, cardTokenInfo.getPanUniqueReference(),
                        "PAR", merchantCoftConfig);
                if (StringUtils.isNotEmpty(uniqueIdentifier)) {
                    promoPaymentOption.setUniquePromoIdentifier(uniqueIdentifier);
                } else {
                    LOGGER.error("Unable to fetch uniquePromoIdentifier for Token");
                    throw BaseException.getException();
                }
            }
            setBinDetailsForToken(binDetail, promoPaymentOption, bin8Available);
        }
    }

    private void setBinDetailsForToken(BinDetail binDetail, PromoPaymentOption promoPaymentOption, boolean bin8Available) {
        if (binDetail != null && binDetail.getBinAttributes() != null) {
            String accountRangeCardBin = binDetail.getBinAttributes().get(
                    BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
            if (StringUtils.isNotEmpty(accountRangeCardBin)) {
                promoPaymentOption.setCardNo(accountRangeCardBin);
                if (bin8Available) {
                    setBin8Hash(promoPaymentOption, accountRangeCardBin, promoPaymentOption.getUniquePromoIdentifier());
                }
            }
        }
    }

    public void setPaymentOptionForBulkApply(PromoPaymentOption paymentOption, CardBeanBiz card, String cardId) {
        paymentOption.setCardNo(String.valueOf(card.getFirstSixDigit()));
        if (StringUtils.isEmpty(paymentOption.getEightDigitBinHash())) {
            String eightDigitBinHash = null;
            if (cardId.length() > 15 && cardId.length() < 45) {
                String accountRangeCardBin = card.getAccountRangeCardBin();
                CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(accountRangeCardBin.substring(
                        0, 8));
                if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                    eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
                }
            } else if (cardId.length() > 15) {
                QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                        .queryNonSensitiveAssetInfo(null, cardId);
                if (null != queryNonSensitiveAssetInfoResponse) {
                    eightDigitBinHash = queryNonSensitiveAssetInfoResponse.getCardInfo().getExtendInfo()
                            .get(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
                }
            }
            paymentOption.setEightDigitBinHash(eightDigitBinHash);
        }
    }

}