package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.enums.BinConfigAttributesEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.coft.model.CoftSavedCards;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.payment.models.response.CardInfo;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.CREDIT_CARD;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DEBIT_CARD;

@Service
public class CoftUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoftUtil.class);

    @Autowired
    private Ff4jUtils ff4jUtils;

    public void addTokenDetailsInPlatformCard(CardInfo tokenCard, PayCardOptionViewBiz platformCard) {
        platformCard.setCardIndexNo(tokenCard.getCardId());
        platformCard.setExtendInfo(tokenCard.getExtendInfo());
        if (!CollectionUtils.isEmpty(tokenCard.getHolderName())) {
            platformCard.setFirstName(tokenCard.getHolderName().get(BizConstant.FIRST_NAME));
            platformCard.setLastName(tokenCard.getHolderName().get(BizConstant.LAST_NAME));
        }
        platformCard.setInstName(tokenCard.getInstOfficialName());
        platformCard.setLastSuccessfulUsedTime(tokenCard.getLastSuccessfulUsedTime());
        // platformCard.setPayOption(platformCard.getPayMethod() + "_" +
        // platformCard.getCardScheme());
        platformCard.setCardCoft(true);
        platformCard.setPar(tokenCard.getPar());
        platformCard.setTokenStatus(tokenCard.getTokenStatus());
        platformCard.setMaskedCardNo(tokenCard.getLastFourDigit());
        platformCard.setCoftPaymentSupported(true);
        platformCard.setCardBin(tokenCard.getCardBin());
        platformCard.setFingerPrint(tokenCard.getFingerprint());
        platformCard.setGcin(tokenCard.getGlobalPanIndex());
    }

    public void updateCacheCardRequest(CacheCardRequest cacheCardRequest) {
        if (null != cacheCardRequest) {
            CacheCardRequestBody cacheCardRequestBody = cacheCardRequest.getBody();
            if ((cacheCardRequestBody.getInstNetworkType() == InstNetworkType.COFT)
                    || cacheCardRequestBody.getInstNetworkType() == InstNetworkType.ISOCARD) {
                if (ff4jUtils.isFeatureEnabled(ENABLE_STORE_IN_CACHE_ONLY, false)) {
                    cacheCardRequestBody.setStoreInCacheOnly(true);
                }
                if (ff4jUtils.isFeatureEnabled(DISABLE_CARD_INDEX_NUMBER, false)) {
                    cacheCardRequestBody.setCardIndexNo(null);
                }
            }
        }
    }

    public boolean checkSimilarCard(CardInfo tokenCard, PayCardOptionViewBiz platformCard) {
        if (tokenCard.getGlobalPanIndex().equals(platformCard.getGcin())) {
            return true;
        }
        return false;
    }

    public PayCardOptionViewBiz convertTokenCardToPlatformCard(CardInfo tokenCard) {
        PayCardOptionViewBiz platformCard = new PayCardOptionViewBiz();
        platformCard.setAssetType(tokenCard.getCardType());
        platformCard.setCardIndexNo(tokenCard.getCardId());
        platformCard.setCardScheme(tokenCard.getCardScheme());
        platformCard.setExpiryMonth(tokenCard.getCardExpiryMonth());
        platformCard.setExpiryYear(tokenCard.getCardExpiryYear());
        platformCard.setExtendInfo(tokenCard.getExtendInfo());
        if (!CollectionUtils.isEmpty(tokenCard.getHolderName())) {
            platformCard.setFirstName(tokenCard.getHolderName().get(BizConstant.FIRST_NAME));
            platformCard.setLastName(tokenCard.getHolderName().get(BizConstant.LAST_NAME));
        }
        platformCard.setInstId(tokenCard.getInstId());
        platformCard.setInstName(tokenCard.getInstOfficialName());
        platformCard.setLastSuccessfulUsedTime(tokenCard.getLastSuccessfulUsedTime());
        platformCard.setPayMethod(BizConstant.CC.equals(tokenCard.getCardType()) ? CREDIT_CARD : DEBIT_CARD);
        platformCard.setPayOption(platformCard.getPayMethod() + "_" + platformCard.getCardScheme());
        platformCard.setCardCoft(true);
        platformCard.setPar(tokenCard.getPar());
        platformCard.setTokenStatus(tokenCard.getTokenStatus());
        platformCard.setMaskedCardNo(tokenCard.getLastFourDigit());
        platformCard.setCardTokenized(true);
        platformCard.setCardBin(tokenCard.getCardBin());
        platformCard.setFingerPrint(tokenCard.getFingerprint());
        platformCard.setGcin(tokenCard.getGlobalPanIndex());
        return platformCard;
    }

    public PayCardOptionViewBiz populateDetailsOfTokenOrPlatformCard(CoftSavedCards card) {
        PayCardOptionViewBiz platformCard = new PayCardOptionViewBiz();
        platformCard.setAssetType(card.getCardType());
        platformCard.setCardIndexNo(card.getSavedCardId());
        platformCard.setCardScheme(card.getCardScheme());
        if (!CollectionUtils.isEmpty(card.getAdditionalInfo())) {
            platformCard.setExpiryMonth(card.getAdditionalInfo().get(BizConstant.EXPIRYMONTH_DISPLAY));
            platformCard.setExpiryYear(card.getAdditionalInfo().get(BizConstant.EXPIRYYEAR_DISPLAY));
            platformCard.setFirstName(card.getAdditionalInfo().get(BizConstant.FIRST_NAME));
            platformCard.setLastName(card.getAdditionalInfo().get(BizConstant.LAST_NAME));
            platformCard.setLastSuccessfulUsedTime(card.getAdditionalInfo().get(BizConstant.TOKEN_LAST_USED_TIME));
            if (StringUtils.isNotBlank(card.getAdditionalInfo().get(BizConstant.CARD_NO_LENGTH)))
                platformCard.setCardNoLength(Integer.valueOf(card.getAdditionalInfo().get(BizConstant.CARD_NO_LENGTH)));
            else
                platformCard.setCardNoLength(4);

            platformCard.setMaskedCardNo(card.getAdditionalInfo().get(BizConstant.MASK_CARD_NUMBER));
        }
        platformCard.setCardBin(card.getCardFirstSixDigits());
        platformCard.setInstName(card.getIssuingBankName());
        platformCard.setInstId(card.getIssuerCode());
        platformCard.setExtendInfo(card.getExtendInfo());
        platformCard.setPayMethod(BizConstant.CC.equals(card.getCardType()) ? CREDIT_CARD : DEBIT_CARD);
        platformCard.setPayOption(card.getCardType() + "_" + card.getCardScheme());
        platformCard.setCardCoft(card.isCardCoft());
        platformCard.setPar(card.getPanUniqueReference());
        platformCard.setTokenStatus(card.getTokenStatus());
        platformCard.setGcin(card.getGlobalPanIndex());
        return platformCard;
    }

    public boolean isCardPaymethodDisabled(WorkFlowRequestBean requestBean) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(requestBean.getDisabledPaymentModes())
                && requestBean.getDisabledPaymentModes().contains(EPayMethod.CREDIT_CARD.toString())
                && requestBean.getDisabledPaymentModes().contains(EPayMethod.DEBIT_CARD.toString())) {
            return true;
        }
        return false;
    }

    public boolean isIssuerTokenProcessingEnabled(PayCardOptionViewBiz platformCard) {
        String blockedIssuersString = ff4jUtils.getPropertyAsStringWithDefault(BLOCKED_ISSUERS_ON_COFT,
                StringUtils.EMPTY);
        Set<String> blockedIssuers = new HashSet<>(Arrays.asList(blockedIssuersString.split(",")));
        String issuer = platformCard.getPayMethod() + "." + platformCard.getCardScheme() + "."
                + platformCard.getInstId();
        return !blockedIssuers.contains(issuer);
    }

    public Map<String, String> getIssuerTokenProcessingOnMidMap() {
        String blockedIssuersString = ff4jUtils.getPropertyAsStringWithDefault(BLOCKED_ISSUERS_ON_COFT_AT_MID,
                StringUtils.EMPTY);
        Map<String, String> issuerMap = new HashMap<>();
        if (StringUtils.isNotBlank(blockedIssuersString)) {
            List<String> listOfBlockIssuer = Arrays.asList(blockedIssuersString.split(","));
            if (!CollectionUtils.isEmpty(listOfBlockIssuer)) {
                for (String s : listOfBlockIssuer) {
                    String[] issuerArray = s.split(":");
                    if (issuerArray.length == 2 && issuerArray[0] != null && issuerArray[1] != null) {
                        issuerMap.put(issuerArray[0], issuerArray[1]);
                    }
                }
            }

        }
        return issuerMap;
    }

    public boolean isBinTokenProcessingEnabled(CardInfo tokenCard) {
        if (StringUtils.isBlank(tokenCard.getTokenBin())) {
            return true;
        }
        String blockedBinsString = ff4jUtils.getPropertyAsStringWithDefault(BLOCKED_BINS_ON_COFT, StringUtils.EMPTY);
        Set<String> blockedBins = new HashSet<>(Arrays.asList(blockedBinsString.split(",")));
        return !blockedBins.contains(tokenCard.getTokenBin());
    }

    public boolean checkTokenProcessingEnable(Map<String, String> issuerTokenProcessingEnabledOnMid, String txnAmt,
            String issuer) {
        if (MapUtils.isNotEmpty(issuerTokenProcessingEnabledOnMid)
                && issuerTokenProcessingEnabledOnMid.containsKey(issuer)) {
            if (StringUtils.isEmpty(txnAmt)) {
                return false;
            } else {
                String issuerTxnAmt = issuerTokenProcessingEnabledOnMid.get(issuer);
                if (org.apache.commons.lang.StringUtils.isNotEmpty(issuerTxnAmt)
                        && !"NA".equalsIgnoreCase(issuerTxnAmt)) {
                    return Double.parseDouble(txnAmt) <= Double.parseDouble(issuerTxnAmt);
                }
            }
        }
        return true;
    }

    public boolean isPaytmCobrandedCard(CardInfo card) {

        return card != null
                && MapUtils.isNotEmpty(card.getExtendInfo())
                && StringUtils.contains(card.getExtendInfo()
                        .get(BinConfigAttributesEnum.CUSTOM_DISPLAY_NAME.getValue()),
                        BizConstant.COBRANDED_COFT_CARD_PREFIX);
    }
}