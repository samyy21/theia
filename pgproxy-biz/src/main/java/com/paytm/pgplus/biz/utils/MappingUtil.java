/*
 * this class maps small beans of facade to biz beans for eg. authToken ,
 * userDetails
 */
package com.paytm.pgplus.biz.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.OAuthTokenBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.exception.FacadeToBIzMappingException;
import com.paytm.pgplus.biz.mapping.models.MappingOuterResponse;
import com.paytm.pgplus.biz.mapping.models.MappingResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.CardTokenInfo;
import com.paytm.pgplus.cache.model.CardTokenRequesterResponse;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.enums.QrType;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.user.models.OAuthToken;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.response.CardInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.wallet.enums.RequestType;
import com.paytm.pgplus.facade.wallet.models.CreateDynamicQRBaseResponse;
import com.paytm.pgplus.facade.wallet.models.CreateDynamicQRRequest;
import com.paytm.pgplus.mappingserviceclient.enums.MerchantUserRequestType;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.ICardTokenService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantQueryService;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput.UserOwner;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.biz.utils.BizConstant.MERCHANT_SOLUTION_TYPE;

/**
 * @author namanjain
 *
 */
@Component("mapUtilsBiz")
public class MappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MappingUtil.class);

    @Autowired
    @Qualifier("userMappingServiceImpl")
    IUserMappingService userMappingService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    private ICardTokenService cardTokenService;

    @Autowired
    @Qualifier("merchantQueryServiceImpl")
    private IMerchantQueryService merchantQueryServiceUtil;

    private static IMerchantQueryService merchantQueryService;

    private static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    @PostConstruct
    private void init() {
        merchantQueryService = this.merchantQueryServiceUtil;
    }

    public UserDetailsBiz mapUserDetails(final UserDetails userDetailsFacade) throws MappingServiceClientException {
        LOGGER.debug("Request UserDetails bean for mapping::{}", userDetailsFacade);
        final UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setEmail(userDetailsFacade.getEmail());
        userDetailsBiz.setMobileNo(userDetailsFacade.getMobileNo());
        userDetailsBiz.setUserId(userDetailsFacade.getUserId());
        userDetailsBiz.setUserName(userDetailsFacade.getUserName());
        userDetailsBiz.setKYC(userDetailsFacade.isKYC());
        userDetailsBiz.setPaytmCCEnabled(userDetailsFacade.isPaytmCCEnabled());
        userDetailsBiz.setPostpaidStatus(userDetailsFacade.getPostpaidStatus());
        userDetailsBiz.setClientId(userDetailsFacade.getClientId());
        userDetailsBiz.setChildUserId(userDetailsFacade.getChildUserId());
        userDetailsBiz.setUserEligibileForPostPaidOnboarding(userDetailsFacade.getUserEligibileForPostPaidOnboarding());
        userDetailsBiz.setPostpaidCreditLimit(userDetailsFacade.getPostpaidCreditLimit());
        userDetailsBiz.setPostpaidOnboardingStageMsg(userDetailsFacade.getPostpaidOnboardingStageMsg());
        List<String> userTypes = userDetailsFacade.getUserTypes();
        userDetailsBiz.setUserTypes(userTypes);
        if (userTypes != null && !userTypes.isEmpty()) {
            for (String type : userTypes) {
                if ("PPB_CUSTOMER".equals(type) || "CA_CUSTOMER".equals(type)) {
                    userDetailsBiz.setSavingsAccountRegistered(true);
                    break;
                }
            }
        }

        // fetching Alipay MID from Cache
        // final MappingUserData mappingUserData =
        // CacheUtils.getValueFromCache(BizConstant.MAPPING_USER_CACHE, new
        // UserDataMappingInput(userDetailsBiz.getUserId(), UserOwner.paytm),
        // MappingUserData.class);
        UserInfo mappingUserData = userMappingService.getUserData(userDetailsBiz.getUserId(), UserOwner.PAYTM);

        if (mappingUserData != null) {
            userDetailsBiz.setInternalUserId(mappingUserData.getAlipayId());
            userDetailsBiz.setPayerAccountNumber(mappingUserData.getAlipayAccountId());
        } else {
            LOGGER.error("No user details found : {}", userDetailsBiz.getUserId());
        }

        LOGGER.debug("Response UserDetails bean for mapping::{}", userDetailsBiz);
        return userDetailsBiz;
    }

    public AccountBalanceResponse mapSavingAccountDetails(final FetchAccountBalanceResponse fetchAccountBalanceResponse) {

        AccountBalanceResponse accountBalanceResponse = new AccountBalanceResponse();
        accountBalanceResponse.setAccountNumber(fetchAccountBalanceResponse.getAccountNumber());
        accountBalanceResponse.setEffectiveBalance(Double.toString(fetchAccountBalanceResponse.getEffectiveBalance()));
        accountBalanceResponse.setSlfdBalance(Double.toString(fetchAccountBalanceResponse.getSlfdBalance()));
        accountBalanceResponse.setTxnId(fetchAccountBalanceResponse.getTxnId());
        return accountBalanceResponse;
    }

    public OAuthTokenBiz mapOAuthToken(final OAuthToken oAuthTokenFacade) throws FacadeToBIzMappingException {
        LOGGER.debug("Request OAuthToken bean for mapping::{}", oAuthTokenFacade);
        final OAuthTokenBiz oauthBiz = new OAuthTokenBiz();
        oauthBiz.setExpiryTime(oAuthTokenFacade.getExpiryTime());
        oauthBiz.setResourceOwnerId(oAuthTokenFacade.getResourceOwnerId());
        oauthBiz.setScope(oAuthTokenFacade.getScope());
        oauthBiz.setToken(oAuthTokenFacade.getToken());
        LOGGER.debug("Response OAuthToken bean for mapping::{}", oauthBiz);
        return oauthBiz;
    }

    public CardBeanBiz mapSavedCard(final SavedCardVO savedCardVO) {
        return new CardBeanBiz(savedCardVO.getCardId(), savedCardVO.getCardNumber(), savedCardVO.getCardType()
                .getName(), savedCardVO.getExpiryDate(), savedCardVO.getFirstSixDigit(),
                savedCardVO.getLastFourDigit(), savedCardVO.getStatus().getValue(), savedCardVO.getUserId(),
                savedCardVO.getmId(), savedCardVO.getCustId());
    }

    public CardBeanBiz mapDecryptedSavedCard(final SavedCardVO savedCardVO, String savedCardNumber, String expiryDate) {
        Long cardID = savedCardVO.getCardId();
        Long firstSixDigit = savedCardVO.getFirstSixDigit();
        Long lastFourDigit = savedCardVO.getLastFourDigit();
        String userID = savedCardVO.getUserId();
        int status = savedCardVO.getStatus().getValue();

        BinDetail binDetail = getBinDetail(firstSixDigit);

        if (binDetail == null) {
            return null;
        }
        CardBeanBiz beanBiz = new CardBeanBiz(cardID, savedCardNumber, binDetail.getCardName(), expiryDate,
                firstSixDigit, lastFourDigit, status, userID, binDetail.getBankCode(), savedCardVO.getCustId(),
                savedCardVO.getmId());
        beanBiz.setCardType(binDetail.getCardType());
        return beanBiz;
    }

    public List<CardBeanBiz> mapListSavedCards(final List<SavedCardVO> savedCardsVO) {

        if (!BizParamValidator.validateInputListParam(savedCardsVO)) {
            return null;
        }

        final List<CardBeanBiz> bizSavedCardBean = new ArrayList<>();
        for (final SavedCardVO savedCardVO : savedCardsVO) {

            if (CardTypeEnum.IMPS == savedCardVO.getCardType() || CardTypeEnum.VPA == savedCardVO.getCardType()) {
                CardBeanBiz cardBean = mapSavedCard(savedCardVO);
                if (CardTypeEnum.VPA == savedCardVO.getCardType()) {
                    cardBean.setCardType("UPI");
                } else {
                    cardBean.setCardType(savedCardVO.getCardType().getName());
                }
                bizSavedCardBean.add(cardBean);
            } else {
                BinDetail binDetails = getBinDetail(savedCardVO.getFirstSixDigit());

                if (!BizParamValidator.validateInputObjectParam(binDetails)) {
                    return null;
                }

                final CardBeanBiz cardBean = mapSavedCard(savedCardVO);

                // Setting BIN specific details
                cardBean.setCardScheme(StringUtils.isNotBlank(binDetails.getCardName()) ? binDetails.getCardName()
                        : savedCardVO.getCardType().getName());
                cardBean.setInstId(binDetails.getBankCode());
                cardBean.setCardType(binDetails.getCardType());
                /*
                 * setting oneClick in card bin
                 */
                cardBean.setOneClickSupported(binDetails.isOneClickSupported());

                cardBean.setPrepaidCard(binDetails.isPrepaidCard());
                cardBean.setCorporateCard(binDetails.isCorporateCard());
                cardBean.setIndian(binDetails.getIsIndian());

                // If displaybank name is empty , use BankCode
                if (StringUtils.isNotBlank(binDetails.getDisplayBankName())) {
                    cardBean.setDisplayName(binDetails.getDisplayBankName());
                } else {
                    cardBean.setDisplayName(binDetails.getBankCode());
                }
                cardBean.setCardIndexNo(savedCardVO.getCardIndexNumber());
                bizSavedCardBean.add(cardBean);
            }
        }

        return bizSavedCardBean;
    }

    public BinDetail getBinDetail(Long binNumber) {

        try {

            return cardUtils.fetchBinDetails(String.valueOf(binNumber));

        } catch (PaytmValidationException exception) {
            LOGGER.error("Exception occured while fetching bin details for bin ::", binNumber, exception);
            return null;
        }
    }

    public CardBeanBiz mapSavedCards(final SavedCardVO savedCardVO, WorkFlowRequestBean flowRequestBean) {

        if (!BizParamValidator.validateInputObjectParam(savedCardVO)) {
            return null;
        }
        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        cardBeanBiz.setCardType(flowRequestBean.getCardType());
        cardBeanBiz.setCardScheme(flowRequestBean.getCardScheme());
        cardBeanBiz.setFirstSixDigit(savedCardVO.getFirstSixDigit());
        cardBeanBiz.setLastFourDigit(savedCardVO.getLastFourDigit());
        cardBeanBiz.setStatus(savedCardVO.getStatus().getValue());
        cardBeanBiz.setUserId(savedCardVO.getUserId());
        cardBeanBiz.setCustId(savedCardVO.getCustId());
        cardBeanBiz.setmId(savedCardVO.getmId());

        return cardBeanBiz;
    }

    public SavedCardVO createSavedCardVO(final WorkFlowRequestBean flowRequestBean, final UserDetailsBiz userDetails) {
        final SavedCardVO savedCardVO = new SavedCardVO();

        // Encrypt CardNumber & Expiry date using MASTER key, not merchant key
        // String encryptedCardNumber = null;
        // String encryptedExpiryDate = null;

        String decryptedCardNumber = null;
        String decryptedExpiryDate = null;
        Long firstSixDigit = null;
        Long lastFourDigit = null;
        String cardScheme = null;
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
            try {
                // encryptedCardNumber =
                // CryptoUtils.encrypt(flowRequestBean.getCardNo());
                decryptedCardNumber = flowRequestBean.getCardNo();
                // Validating expiryMonth
                StringBuilder expiryMonth = new StringBuilder();
                if (String.valueOf(flowRequestBean.getExpiryMonth()).length() == 1) {
                    expiryMonth.append(BizConstant.ZERO).append(String.valueOf(flowRequestBean.getExpiryMonth()));
                } else {
                    expiryMonth.append(String.valueOf(flowRequestBean.getExpiryMonth()));
                }
                // encryptedExpiryDate = CryptoUtils
                // .encrypt(expiryMonth.toString() +
                // String.valueOf(flowRequestBean.getExpiryYear()));
                decryptedExpiryDate = expiryMonth.toString() + String.valueOf(flowRequestBean.getExpiryYear());
                firstSixDigit = Long.valueOf(flowRequestBean.getCardNo().substring(0, 6));
                int cardNumberLength = flowRequestBean.getCardNo().length();
                lastFourDigit = Long.valueOf(flowRequestBean.getCardNo().substring(cardNumberLength - 4,
                        cardNumberLength));
                cardScheme = flowRequestBean.getCardScheme();

            } catch (Exception e) {
                LOGGER.error("Exception occured while encrypting card details using master key", e);
                return null;
            }

        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {
            try {
                String phoneNumber = flowRequestBean.getHolderMobileNo() == null ? flowRequestBean.getMobileNo()
                        : flowRequestBean.getHolderMobileNo();
                // encryptedCardNumber = CryptoUtils.encrypt(phoneNumber);
                // encryptedExpiryDate =
                // CryptoUtils.encrypt(flowRequestBean.getMmid());
                decryptedExpiryDate = flowRequestBean.getMmid();
                decryptedCardNumber = phoneNumber;
                firstSixDigit = Long.valueOf(phoneNumber.substring(0, 6));
                lastFourDigit = Long.valueOf(flowRequestBean.getMmid().substring(0, 4));
                cardScheme = "IMPS";

            } catch (Exception e) {
                LOGGER.error("Exception occured while encrypting card details using master key", e);
                return null;
            }

        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            decryptedCardNumber = flowRequestBean.getVirtualPaymentAddress();
            cardScheme = "VPA";
        }

        savedCardVO.setCardNumber(decryptedCardNumber);
        savedCardVO.setCardType(CardTypeEnum.getEnumByName(cardScheme));
        savedCardVO.setStatus(StatusEnum.ACTIVE);
        if (StringUtils.isNotBlank(userDetails.getUserId())) {
            savedCardVO.setUserId(userDetails.getUserId());
        }
        if (!flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            savedCardVO.setFirstSixDigit(firstSixDigit);
            savedCardVO.setLastFourDigit(lastFourDigit);
            savedCardVO.setExpiryDate(decryptedExpiryDate);
            savedCardVO.setmId(flowRequestBean.getPaytmMID());
            savedCardVO.setCustId(flowRequestBean.getCustID());
        }
        if (flowRequestBean.getRequestType() != null) {
            savedCardVO.setRequestType(flowRequestBean.getRequestType().getType());
        }
        if (ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            savedCardVO.setCacheCardExpiryTimeInMillis(flowRequestBean.getOrderTimeOutInMilliSecond());
        }

        savedCardVO.setCardIndexNumber(flowRequestBean.getCardIndexNo());

        return savedCardVO;
    }

    public static <T> T parseJsonData(String jsonData, Class<T> requiredResponseFormat) throws JsonParseException,
            JsonMappingException, IOException {
        LOGGER.info("JsonResponse {} & required format {}", jsonData, requiredResponseFormat);
        JavaType javaType = objectMapper.getTypeFactory().constructType(MappingOuterResponse.class,
                requiredResponseFormat);
        MappingOuterResponse<T> mappingOuterResponse = objectMapper.readValue(jsonData, javaType);

        LOGGER.info(" ----> {}", mappingOuterResponse.getResponse().getBody().getResponse());

        final MappingResponse<T> mappingResponse = mappingOuterResponse.getResponse();

        if ((mappingResponse != null) && (mappingResponse.getBody() != null)
                && (mappingResponse.getBody().getResponse() != null)) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final T mappingResponseData = objectMapper.convertValue(mappingOuterResponse.getResponse().getBody()
                    .getResponse(), requiredResponseFormat);
            LOGGER.debug("Required response data format :: {}", mappingResponseData);
            return mappingResponseData;
        }
        return null;
    }

    public QRCodeDetailsResponse mapQRCodeDetails(CreateDynamicQRBaseResponse createDynamicQRBaseResponse,
            CreateDynamicQRRequest createDynamicQRRequest) {
        final QRCodeDetailsResponse QRDetails = new QRCodeDetailsResponse();
        QRDetails.setEncryptedData(createDynamicQRBaseResponse.getEncryptedData());
        QRDetails.setPath(createDynamicQRBaseResponse.getPath());
        if (createDynamicQRRequest.getRequest().getRequestType().equals(RequestType.UPI_QR_CODE.getValue())) {
            QRDetails.setQrType(QrType.UPI_QR);
        } else if (createDynamicQRRequest.getRequest().getRequestType().equals(RequestType.QR_ORDER.getValue())) {
            QRDetails.setQrType(QrType.PAYTM_QR);
        }
        return QRDetails;
    }

    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> getMerchantConsultPayViewBean(
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult) {
        ConsultPayViewResponseBizBean merchantConsult = new ConsultPayViewResponseBizBean();
        if (litePayviewConsult != null && litePayviewConsult.getResponse() != null) {
            merchantConsult.setPayMethodViews(litePayviewConsult.getResponse().getPayMethodViews());
            merchantConsult.setPaymentsBankSupported(litePayviewConsult.getResponse().isPaymentsBankSupported());
            merchantConsult.setExtendInfo(litePayviewConsult.getResponse().getExtendInfo());
            merchantConsult.setLoginMandatory(litePayviewConsult.getResponse().isLoginMandatory());
            merchantConsult.setWalletOnly(litePayviewConsult.getResponse().isWalletOnly());
            merchantConsult.setWalletFailed(litePayviewConsult.getResponse().isWalletFailed());
        }
        return new GenericCoreResponseBean<>(merchantConsult);
    }

    public GenericCoreResponseBean<CardBeanBiz> mapNonSensitiveAssetInfoToCardBeanBiz(CardInfoResponse cardInfo) {
        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        if (cardInfo.getMaskedCardNo().length() > 6) {
            cardBeanBiz.setFirstSixDigit(Long.valueOf(cardInfo.getMaskedCardNo().substring(0, 6)));
            cardBeanBiz.setLastFourDigit(Long.valueOf(cardInfo.getMaskedCardNo().substring(
                    cardInfo.getMaskedCardNo().length() - 4)));
        }
        cardBeanBiz.setCardNumber(cardInfo.getMaskedCardNo());
        cardBeanBiz.setCardScheme(cardInfo.getCardScheme());
        if (StringUtils.isNotBlank(cardInfo.getExtendInfo().get(FacadeConstants.ExtendInfo.CARD_EXPIRE_MONTH))
                && StringUtils.isNotBlank(cardInfo.getExtendInfo().get(FacadeConstants.ExtendInfo.CARD_EXPIRE_YEAR))) {
            cardBeanBiz.setExpiryDate(cardInfo.getExtendInfo().get(FacadeConstants.ExtendInfo.CARD_EXPIRE_MONTH)
                    + cardInfo.getExtendInfo().get(FacadeConstants.ExtendInfo.CARD_EXPIRE_YEAR));
        }
        cardBeanBiz.setInstId(cardInfo.getInstId());
        cardBeanBiz.setCardIndexNo(cardInfo.getCardIndexNo());
        return new GenericCoreResponseBean<>(cardBeanBiz);
    }

    public boolean getCoftEligibilityForMerchant(String mid, String cardScheme) {
        Map<String, List<String>> coftEligibilityMerchantStatus = fillCoftEligibilityMIDMap(mid);
        return coftEligibilityMerchantStatus.get(mid) != null
                && coftEligibilityMerchantStatus.get(mid).contains(cardScheme);
    }

    public boolean isLocalVaultEnabled(String mid, String custId) {
        if (mid == null || custId == null) {
            return false;
        }
        Map<String, List<String>> coftEligibilityMerchantStatus = fillCoftEligibilityMIDMap(mid);
        return coftEligibilityMerchantStatus.get(mid) != null && coftEligibilityMerchantStatus.get(mid).size() > 0;
    }

    public Map<String, List<String>> fillCoftEligibilityMIDMap(String mid) {
        Map<String, List<String>> coftEligibilityMerchantStatus = new HashMap<>();
        try {
            // LOGGER.info("Calling Mapping Service to check COFT eligibility");
            EXT_LOGGER.customInfo("Calling Mapping Service to check COFT eligibility");
            CardTokenRequesterResponse response = cardTokenService.getCardTokenInfos(mid, null, null);
            EXT_LOGGER.customInfo("Mapping response - CardTokenRequesterResponse :: {}", response);
            if (response != null && !CollectionUtils.isEmpty(response.getCardTokenInfoList())) {
                for (CardTokenInfo cardTokenInfo : response.getCardTokenInfoList()) {
                    if (coftEligibilityMerchantStatus.get(mid) != null) {
                        coftEligibilityMerchantStatus.get(mid).add(cardTokenInfo.getCardScheme());
                    } else {
                        List<String> cardSchemeList = new ArrayList<>();
                        cardSchemeList.add(cardTokenInfo.getCardScheme());
                        coftEligibilityMerchantStatus.put(mid, cardSchemeList);
                    }
                }
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Exception while calling MS getCardTokenInfos API : ",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        LOGGER.info("coftEligibilityMerchantStatus {}", coftEligibilityMerchantStatus);
        return coftEligibilityMerchantStatus;
    }

    public boolean isPaytmTokenRequestor(String mid, String cardScheme) {
        try {
            CardTokenRequesterResponse cardTokenRequesterResponse = cardTokenService.getCardTokenInfos(mid, cardScheme,
                    null);
            EXT_LOGGER.customInfo("Mapping response - CardTokenRequesterResponse :: {}", cardTokenRequesterResponse);
            if (cardTokenRequesterResponse != null
                    && !CollectionUtils.isEmpty(cardTokenRequesterResponse.getCardTokenInfoList())) {
                for (CardTokenInfo cardTokenInfo : cardTokenRequesterResponse.getCardTokenInfoList()) {
                    if (cardTokenInfo.getModel().equals("PAYTM") || cardTokenInfo.getModel().equals("PAYTM_MERCHANT")) {
                        return true;
                    }
                }
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Exception while calling MS getCardTokenInfos API : ",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
        }

        return false;
    }

    public static String getMerchantSolutionType(String mid) {
        String merchantAttributeResponse;
        Map<String, Object> merchantAttributeResponseMap;
        String merchantType = OFFLINE_MERCHANT;

        try {
            merchantAttributeResponse = merchantQueryService.getMerchantAttribute(mid, MERCHANT_PREFERENCE,
                    MerchantUserRequestType.PAYTM.getValue());
            EXT_LOGGER.customInfo("Mapping response - MerchantAttributeResponse :: {}", merchantAttributeResponse);
            if (merchantAttributeResponse != null) {
                merchantAttributeResponseMap = JsonMapper.mapJsonToObject(merchantAttributeResponse, Map.class);
                if (merchantAttributeResponseMap != null) {
                    if (merchantAttributeResponseMap.get(MERCHANT_SOLUTION_TYPE) != null) {
                        merchantType = (String) merchantAttributeResponseMap.get(MERCHANT_SOLUTION_TYPE);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching Solution Type for mid: {}, error: {}", mid, e.getMessage());
            return null;
        }
        return merchantType;
    }

}