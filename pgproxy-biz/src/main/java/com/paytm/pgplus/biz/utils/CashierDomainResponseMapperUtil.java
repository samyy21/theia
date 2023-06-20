package com.paytm.pgplus.biz.utils;

import com.mchange.v1.lang.BooleanUtils;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.BinConfigAttributesEnum;
import com.paytm.pgplus.facade.dataservice.models.Card;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by charu on 07/02/20.
 */

/**
 * This service will take care of mapping platform's cashier domain to PG PoJos.
 */

@Service
public class CashierDomainResponseMapperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CashierDomainResponseMapperUtil.class);

    @Autowired
    Ff4jUtils ff4jUtils;

    public List<CardBeanBiz> getCardBeanListFromPlatformResponse(
            LitePayviewConsultResponseBizBean payviewConsultResponseBizBean) {
        if (payviewConsultResponseBizBean == null
                || CollectionUtils.isEmpty(payviewConsultResponseBizBean.getPayMethodViews())) {
            return Collections.emptyList();
        }
        List<PayMethodViewsBiz> payMethodViewsBizList = payviewConsultResponseBizBean.getPayMethodViews();
        List<CardBeanBiz> cardBeanBizList = new LinkedList<>();
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {
            for (PayCardOptionViewBiz cardDetails : payMethodViewsBiz.getPayCardOptionViews()) {
                cardBeanBizList.add(getCardBeanFromPayCardView(cardDetails));
            }
        }
        return cardBeanBizList;
    }

    public CardBeanBiz getCardBeanFromPayCardView(PayCardOptionViewBiz cardDetails) {
        String expireDate = cardDetails.getExpiryMonth() + cardDetails.getExpiryYear();
        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        cardBeanBiz.setCardNumber(cardDetails.getMaskedCardNo());
        cardBeanBiz.setCardScheme(cardDetails.getCardScheme());
        cardBeanBiz.setDisplayName(cardDetails.getInstId());
        cardBeanBiz.setCardIndexNo(cardDetails.getCardIndexNo());
        // hardcoding status to 1 as alipay is sending only active cards.
        cardBeanBiz.setStatus(1);
        cardBeanBiz.setDisabled(!cardDetails.isEnableStatus());
        cardBeanBiz.setDisabledReason(cardDetails.getDisableReason());
        cardBeanBiz.setCardType(cardDetails.getPayMethod());
        cardBeanBiz.setInstId(cardDetails.getInstId());
        cardBeanBiz.setExpiryDate(expireDate);
        cardBeanBiz.setRemainingLimit(cardDetails.getRemainingLimit());
        // case when tokenized cards are preferred over lpv card
        if (cardDetails.isCardCoft()) {
            String accountRangeCardBin = (cardDetails.getExtendInfo() != null) ? cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.ACCOUNT_RANGE_CARD_BIN.getValue()) : null;
            cardBeanBiz
                    .setFirstSixDigit(StringUtils.isNotEmpty(accountRangeCardBin) ? accountRangeCardBin.length() > 6 ? Long
                            .valueOf(accountRangeCardBin.substring(0, 6)) : Long.valueOf(accountRangeCardBin)
                            : StringUtils.isNotEmpty(cardDetails.getCardBin()) ? Long.valueOf(cardDetails.getCardBin())
                                    : 0L);
            cardBeanBiz.setLastFourDigit(Long.valueOf(cardDetails.getMaskedCardNo()));
            cardBeanBiz.setAccountRangeCardBin(accountRangeCardBin);
        } else {
            cardBeanBiz.setFirstSixDigit(Long.valueOf(cardDetails.getCardBin()));
            cardBeanBiz.setLastFourDigit(Long.valueOf(cardDetails.getMaskedCardNo().substring(
                    cardDetails.getMaskedCardNo().length() - 4)));
            String eightDigitBinHash = (cardDetails.getExtendInfo() != null) ? cardDetails.getExtendInfo().get(
                    (BinConfigAttributesEnum.EIGHT_DIGIT_BIN_HASH.getValue())) : null;
            cardBeanBiz.setAccountRangeCardBin(eightDigitBinHash);
        }
        if (cardDetails.getExtendInfo() != null) {
            cardBeanBiz.setEightDigitBinHash(cardDetails.getExtendInfo().get(
                    (BinConfigAttributesEnum.EIGHT_DIGIT_BIN_HASH.getValue())));
            cardBeanBiz.setOneClickSupported(Boolean.parseBoolean(cardDetails.getExtendInfo().get(
                    (BinConfigAttributesEnum.ONE_CLICK_SUPPORTED.getValue()))));
            cardBeanBiz.setPrepaidCard(Boolean.parseBoolean(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.PREPAID_CARD.getValue())));
            cardBeanBiz.setCorporateCard(Boolean.parseBoolean(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.CORPORATE_CARD.name())));
            cardBeanBiz.setIndian(Boolean.parseBoolean(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.INDIAN.getValue())));
            cardBeanBiz.setZeroSuccessRate(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.ZERO_SUCCESS_RATE.getValue()));
            cardBeanBiz.setInstName(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.INST_NAME.getValue()));
            cardBeanBiz
                    .setCountryCode(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.COUNTRY_CODE.getValue()));
            cardBeanBiz.setCountry(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.COUNTRY.getValue()));
            cardBeanBiz.setCountryCodeIso(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.COUNTRY_CODE_ISO.getValue()));
            cardBeanBiz.setCurrency(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.CURRENCY.getValue()));
            cardBeanBiz.setCurrencyCode(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.CURRENCY_CODE.getValue()));
            cardBeanBiz.setCurrencyCodeIso(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.CURRENCY_CODE_ISO.getValue()));
            cardBeanBiz.setSymbol(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.SYMBOL.getValue()));
            cardBeanBiz.setCurrencyPrecision(cardDetails.getExtendInfo().get(
                    BinConfigAttributesEnum.CURRENCY_PRECISION.getValue()));
            cardBeanBiz.setCategory(cardDetails.getExtendInfo().get(BinConfigAttributesEnum.CATEGORY.getValue()));
        }

        if (ff4jUtils.isFeatureEnabled("theia.saveCardDisplayName", false)) {
            cardBeanBiz.setDisplayName(cardDetails.getInstName());
            if (cardDetails.getExtendInfo() != null
                    && cardDetails.getExtendInfo().get(BinConfigAttributesEnum.CUSTOM_DISPLAY_NAME.getValue()) != null) {
                cardBeanBiz.setDisplayName(cardDetails.getExtendInfo().get(
                        BinConfigAttributesEnum.CUSTOM_DISPLAY_NAME.getValue()));
            }
        }
        cardBeanBiz.setCardCoft(cardDetails.isCardCoft());
        cardBeanBiz.setEligibleForCoft(cardDetails.isEligibleForCoft());
        cardBeanBiz.setCoftPaymentSupported(cardDetails.isCoftPaymentSupported());
        cardBeanBiz.setPar(cardDetails.getPar());
        cardBeanBiz.setTokenStatus(cardDetails.getTokenStatus());
        cardBeanBiz.setFingerPrint(cardDetails.getFingerPrint());
        cardBeanBiz.setGcin(cardDetails.getGcin());
        return cardBeanBiz;

    }

}
