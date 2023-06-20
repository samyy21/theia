package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.core.cachecard.utils.CacheCardInfoHelper;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.facade.coft.model.*;
import com.paytm.pgplus.facade.coft.service.ICoftService;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CoftUtils;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.utils.BinUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_ENABLE_GCIN_GENERATION;

/**
 * @author utkarshsrivastava on 26/05/22
 */

@Service("coftTokenDataService")
public class CoftTokenDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoftTokenDataService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(CoftTokenDataService.class);

    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetFacade;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier("CoftService")
    private ICoftService coftService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private CoftUtils coftUtils;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public String getSavedCardIdFromCardNumber(String mid, String cardNo, String merchantCoftConfig) {
        if (merchantCoftConfig.equals("GCIN")) {
            return getGcinFromCardNumber(cardNo);
        } else if (merchantCoftConfig.equals("PAR")) {
            return getParFromCardNumber(mid, cardNo, merchantCoftConfig);
        }
        return null;
    }

    public String getGcinFromCardNumber(String cardNumber) {
        if (ff4jUtils.isFeatureEnabled(THEIA_ENABLE_GCIN_GENERATION, false)) {
            if (StringUtils.isNotBlank(cardNumber)) {
                LOGGER.info("GCIN generation at theia");
                return workFlowHelper.generateCardGCIN(cardNumber);
            }
        } else {
            final CacheCardRequest cacheCardRequest = createCacheCardRequest(cardNumber);
            if (null != cacheCardRequest) {
                CacheCardResponse cacheCardResponse = cacheCard(cacheCardRequest);
                if (cacheCardResponse != null && cacheCardResponse.getBody() != null) {
                    return cacheCardResponse.getBody().getGlobalPanIndex();
                }
            }
        }
        return null;
    }

    public String getParFromCardNumber(String mid, String cardNo, String merchantCoftConfig) {
        String encryptedCardNo = encryptByPublicKey(cardNo);
        if (encryptedCardNo != null) {
            return getTokenData(mid, encryptedCardNo, "PAN", merchantCoftConfig);
        }
        return null;
    }

    public String encryptByPublicKey(String plainData) {
        try {
            String secretKey = environment.getProperty("coft.public.key");
            PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(secretKey)));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainData.getBytes()));
        } catch (Exception e) {
            LOGGER.error("Exception occurred while encrypting card no");
        }
        return null;
    }

    public String getSavedCardIdFromCardIndexNumber(String mid, String merchantCoftConfig, String savedCardId,
            QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse) {
        if (merchantCoftConfig.equals("GCIN")) {
            if (Objects.nonNull(queryNonSensitiveAssetInfoResponse)) {
                return queryNonSensitiveAssetInfoResponse.getCardInfo().getGlobalCardIndex();
            }
        } else if (merchantCoftConfig.equals("PAR")) {
            return getTokenData(mid, savedCardId, "CIN", merchantCoftConfig);
        }
        return null;
    }

    private CacheCardRequest createCacheCardRequest(String cardNumber) {
        final CacheCardRequestBean cacheCardReqBean = new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNumber,
                null, null, null, null, null, null, null).build();
        GenericCoreResponseBean<CacheCardRequest> cacheCardReq = CacheCardInfoHelper
                .createCacheCardRequestForCardPayment(cacheCardReqBean, InstNetworkType.ISOCARD);
        if (!cacheCardReq.isSuccessfullyProcessed()) {
            LOGGER.error("Error in creating cache card request: {} ", cacheCardReq.getFailureDescription());
            return null;
        }
        return cacheCardReq.getResponse();
    }

    private CacheCardResponse cacheCard(CacheCardRequest cacheCardReq) {
        try {
            coftUtil.updateCacheCardRequest(cacheCardReq);
            return assetFacade.cacheCard(cacheCardReq);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching token data from cardnumber: ", e);
        }
        return null;
    }

    public String getTokenData(String mid, String uniqueId, String requestType, String merchantCoftConfig) {
        final FetchPanUniqueReferenceRequestBody fetchPanUniqueReferenceRequestBody = new FetchPanUniqueReferenceRequestBody(
                mid, requestType, uniqueId);
        final FetchPanUniqueReferenceRequest fetchPanUniqueReferenceRequest = createFetchPanUniqueReferenceRequest(fetchPanUniqueReferenceRequestBody);
        try {
            FetchPanUniqueReferenceResponse fetchPanUniqueReferenceResponse = coftService.fetchPanUniqueReference(
                    fetchPanUniqueReferenceRequest, false);
            if (null != fetchPanUniqueReferenceResponse && null != fetchPanUniqueReferenceResponse.getBody()) {
                String savedId;
                if (merchantCoftConfig.equals("PAR")) {
                    savedId = fetchPanUniqueReferenceResponse.getBody().getPanUniqueReference();
                } else if (merchantCoftConfig.equals("GCIN")) {
                    savedId = fetchPanUniqueReferenceResponse.getBody().getGlobalPanIndex();
                } else {
                    savedId = fetchPanUniqueReferenceResponse.getBody().getCardIndexNumber();
                }
                return savedId;
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching uniqueId data : {} ", uniqueId, e);
        }
        return null;
    }

    public FetchPanUniqueReferenceResponse getTokenData(String mid, String requestType, String requestValue,
            boolean disableNetworkRefresh) {
        final FetchPanUniqueReferenceRequestBody fetchPanUniqueReferenceRequestBody = new FetchPanUniqueReferenceRequestBody(
                mid, requestType, requestValue);
        final FetchPanUniqueReferenceRequest fetchPanUniqueReferenceRequest = createFetchPanUniqueReferenceRequest(fetchPanUniqueReferenceRequestBody);
        try {
            return coftService.fetchPanUniqueReference(fetchPanUniqueReferenceRequest, disableNetworkRefresh);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching token data for requestType: {}, requestValue: {}",
                    requestType, requestValue, e);
        }
        return null;
    }

    private FetchPanUniqueReferenceRequest createFetchPanUniqueReferenceRequest(
            FetchPanUniqueReferenceRequestBody fetchPanUniqueReferenceRequestBody) {
        return new FetchPanUniqueReferenceRequest(new FetchPanUniqueReferenceRequestHead(),
                fetchPanUniqueReferenceRequestBody);
    }

    public String getMerchantConfig(String mid) {
        return merchantPreferenceService.isCoftPromoParConfigEnabled(mid) ? "PAR" : "GCIN";
    }

    public BinDetail fetchBinDetailsOnToken(String token) {
        return processTransactionUtil.getBinDetailForCoft(token);
    }

    public String fetchTokenDetail(String mid, String tokenIndexNumber) {
        FetchTokenDetailRequestBody fetchTokenDetailRequestBody = new FetchTokenDetailRequestBody();
        fetchTokenDetailRequestBody.setMid(mid);
        fetchTokenDetailRequestBody.setTokenIndexNumber(tokenIndexNumber);

        FetchTokenDetailRequest fetchTokenDetailRequest = new FetchTokenDetailRequest();
        fetchTokenDetailRequest.setHead(new GenerateTokenDataRequestHead());
        fetchTokenDetailRequest.setBody(fetchTokenDetailRequestBody);

        FetchTokenDetailResponse fetchTokenDetailResponse = null;
        try {
            fetchTokenDetailResponse = coftService.fetchTokenDetail(fetchTokenDetailRequest);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while fetching tokenDetails from coftService {}", e);
        }

        if (fetchTokenDetailResponse != null && fetchTokenDetailResponse.getBody() != null
                && fetchTokenDetailResponse.getBody().getTokenInfo() != null) {
            return fetchTokenDetailResponse.getBody().getTokenInfo().getTokenBin();
        }
        return StringUtils.EMPTY;
    }

    public GenerateTokenDataResponseBody getTokenTavvFromTin(String Token, String userId, String mid) {

        UserInfo userInfo = new UserInfo();
        userInfo.setCustId(userId);
        GenerateTokenDataRequestBody generateTokenDataRequestBody = new GenerateTokenDataRequestBody("ECOM", Token,
                userInfo);
        final GenerateTokenDataRequest generateTokenDataRequest = createGenerateTokenDataRequest(generateTokenDataRequestBody);
        if (null != generateTokenDataRequest) {
            GenerateTokenDataResponse generateTokenDataResponse = null;
            try {
                generateTokenDataResponse = coftService.generateTokenData(generateTokenDataRequest, mid);
                try {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("RESPONSE_STATUS", generateTokenDataResponse.getBody().getResultInfo()
                            .getResultStatus());
                    responseMap.put("RESPONSE_MESSAGE", generateTokenDataResponse.getBody().getResultInfo()
                            .getResultMsg());
                    statsDUtils.pushResponse("GENERATE_TOKEN_DATA", responseMap);
                } catch (Exception exception) {
                    LOGGER.error("Error in pushing response message " + "GENERATE_TOKEN_DATA" + "to grafana", exception);
                }

            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception occurred while fetching uniqueId data from TIN  {}:  ", Token, e);
            }
            if (null != generateTokenDataResponse && null != generateTokenDataResponse.getBody()) {
                return generateTokenDataResponse.getBody();
            }

        }
        return null;
    }

    public GenerateTokenDataRequest createGenerateTokenDataRequest(
            GenerateTokenDataRequestBody generateTokenDataRequestBody) {
        GenerateTokenDataRequest generateTokenDataRequest = new GenerateTokenDataRequest(
                new GenerateTokenDataRequestHead(), generateTokenDataRequestBody);
        return generateTokenDataRequest;
    }

    public NativeCashierInfoResponse populateCashierInfoResponseFromCache(String txnToken) {
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
        return cashierInfoResponse;

    }

    public SavedCard fetchTokenDataFromFPO(String uniqueId, NativeCashierInfoResponse cashierInfoResponse) {
        if (Objects.nonNull(cashierInfoResponse)) {
            List<PayChannelBase> merchantPayOption = cashierInfoResponse.getBody().getMerchantPayOption()
                    .getSavedInstruments();
            List<PayChannelBase> addMoneyPayOption = cashierInfoResponse.getBody().getAddMoneyPayOption()
                    .getSavedInstruments();
            if (uniqueId.length() > 15) {
                SavedCard saveCard = fetchSavedCardByTINorGCIN(merchantPayOption, uniqueId);
                if (null == saveCard) {
                    saveCard = fetchSavedCardByTINorGCIN(addMoneyPayOption, uniqueId);

                }
                return saveCard;
            }
        }
        return null;
    }

    private SavedCard fetchSavedCardByTINorGCIN(List<PayChannelBase> savedCardList, String uniqueId) {
        if (CollectionUtils.isNotEmpty(savedCardList)) {
            for (PayChannelBase payChannelBase : savedCardList) {
                SavedCard savedCard = (SavedCard) payChannelBase;
                if (uniqueId.equals(savedCard.getCardDetails().getCardId())) {
                    return savedCard;
                }
            }
        }
        return null;
    }

    public BinDetail getCardBinDetails(String binNumber) {
        if (StringUtils.isNotEmpty(binNumber) && binNumber.length() >= 6) {
            binNumber = binNumber.length() >= 9 ? binNumber.substring(0, 9) : binNumber.substring(0, 6);
        }

        BinDetail binDetail = null;
        try {
            BinUtils.logSixDigitBinLength(binNumber);
            binDetail = binFetchService.getCardBinDetail(Long.parseLong(binNumber));
            EXT_LOGGER.customInfo("Mapping response - BinDetail :: {}", binDetail);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Exception occurred while fetching Bin {}, due to", binNumber, e);
        }
        return binDetail;
    }

}