/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.theia.s2s.enums.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.DirectChannelBank;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.SavedCardType;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;

/**
 * @author amit.dubey
 *
 */
@Component("cardInfoSessionUtil")
public class CardInfoSessionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    public void setCardInfoIntoSession(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        if (responseData.getUserDetails() != null) {

            final CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(requestData.getRequest(), true);

            LOGGER.debug("Found data in session, cardInfo :{}", cardInfo);
            final UserDetailsBiz userDetails = responseData.getUserDetails();
            LOGGER.debug("UserDetailsBiz :{}", userDetails);

            if (!responseData.getUserDetails().getMerchantViewSavedCardsList().isEmpty()) {
                // Preparing merchant view saved card list
                List<SavedCardInfo> merchantViewSavedCardsList = new ArrayList<>();
                Map<String, SavedCardInfo> savedCardMap = new HashMap<>();

                SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

                List<PayMethodViewsBiz> payMethodViews = responseData.getMerchnatViewResponse() != null ? responseData
                        .getMerchnatViewResponse().getPayMethodViews() : responseData.getMerchnatLiteViewResponse()
                        .getPayMethodViews() != null ? responseData.getMerchnatLiteViewResponse().getPayMethodViews()
                        : Collections.emptyList();
                for (CardBeanBiz cardBeanBiz : userDetails.getMerchantViewSavedCardsList()) {

                    final SavedCardInfo savedCardInfo = generateSavedCardInfo(cardBeanBiz, payMethodViews,
                            successRateCacheModel);

                    if (null == savedCardInfo) {
                        continue;
                    }

                    savedCardMap.put(savedCardInfo.getCardId().toString(), savedCardInfo);
                    merchantViewSavedCardsList.add(savedCardInfo);
                }

                cardInfo.setSavedCardMap(savedCardMap);
                cardInfo.setMerchantViewSavedCardsList(merchantViewSavedCardsList);

                // Setting SavedCard enabled for showing card on payment page
                cardInfo.setSaveCardEnabled(true);
            }

            if (!responseData.getUserDetails().getAddAndPayViewSavedCardsList().isEmpty()) {
                // Preparing add&PayView saved card list
                final List<SavedCardInfo> addAndPaySavedCardsList = new ArrayList<>();
                Map<String, SavedCardInfo> savedCardMap = new HashMap<>();

                SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();
                List<PayMethodViewsBiz> payMethodViews = responseData.getAddAndPayViewResponse() != null ? responseData
                        .getAddAndPayViewResponse().getPayMethodViews() : responseData.getAddAndPayLiteViewResponse()
                        .getPayMethodViews() != null ? responseData.getAddAndPayLiteViewResponse().getPayMethodViews()
                        : Collections.emptyList();
                for (final CardBeanBiz cardBeanBiz : userDetails.getAddAndPayViewSavedCardsList()) {

                    final SavedCardInfo savedCardInfo = generateSavedCardInfo(cardBeanBiz, payMethodViews,
                            successRateCacheModel);

                    if (null == savedCardInfo) {
                        continue;
                    }

                    savedCardMap.put(savedCardInfo.getCardId().toString(), savedCardInfo);
                    addAndPaySavedCardsList.add(savedCardInfo);
                }
                cardInfo.setAddAnPaySavedCardMap(savedCardMap);
                cardInfo.setAddAndPayViewCardsList(addAndPaySavedCardsList);

                // Setting SavedCard enabled for showing card on payment page
                cardInfo.setAddAndPayViewSaveCardEnabled(true);
            }

            filterSavedCardsForPromo(requestData, cardInfo);

            LOGGER.debug("CardInfo List Created IS:::{}", cardInfo);

        } else if (responseData.getmIdCustIdCardBizDetails() != null) {

            final CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(requestData.getRequest(), true);

            LOGGER.debug("Found data in session, cardInfo :{}", cardInfo);
            final MidCustIdCardBizDetails mIdCustIdCardDetails = responseData.getmIdCustIdCardBizDetails();
            LOGGER.debug("MidCustIdCardBizDetails :{}", mIdCustIdCardDetails);

            if (mIdCustIdCardDetails.getMerchantCustomerCardList() != null
                    && !mIdCustIdCardDetails.getMerchantCustomerCardList().isEmpty()) {

                List<SavedCardInfo> merchantViewSavedCardsList = new ArrayList<>();
                Map<String, SavedCardInfo> savedCardMap = new HashMap<>();
                SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

                List<PayMethodViewsBiz> payMethodViews = responseData.getMerchnatViewResponse() != null ? responseData
                        .getMerchnatViewResponse().getPayMethodViews() : responseData.getMerchnatLiteViewResponse()
                        .getPayMethodViews() != null ? responseData.getMerchnatLiteViewResponse().getPayMethodViews()
                        : Collections.emptyList();
                for (CardBeanBiz cardBeanBiz : mIdCustIdCardDetails.getMerchantCustomerCardList()) {
                    SavedCardInfo savedCardInfo = generateSavedCardInfo(cardBeanBiz, payMethodViews,
                            successRateCacheModel);
                    savedCardMap.put(savedCardInfo.getCardId().toString(), savedCardInfo);
                    merchantViewSavedCardsList.add(savedCardInfo);
                }

                cardInfo.setSavedCardMap(savedCardMap);
                cardInfo.setMerchantViewSavedCardsList(merchantViewSavedCardsList);

                // Setting SavedCard enabled for showing card on payment page
                cardInfo.setSaveCardEnabled(true);
            }
            LOGGER.debug("CardInfo List Created IS:::{}", cardInfo);
        }
    }

    private SavedCardInfo generateSavedCardInfo(CardBeanBiz cardBeanBiz, List<PayMethodViewsBiz> payMethodViews,
            SuccessRateCacheModel successRateCacheModel) {
        final SavedCardInfo savedCardInfo = new SavedCardInfo();

        String savedCardType = cardBeanBiz.getCardType();
        savedCardInfo.setCardType(savedCardType);
        savedCardInfo.setPaymentMode(savedCardType);
        savedCardInfo.setCardId(cardBeanBiz.getCardId());
        savedCardInfo.setTxnMode(EPayMethod.getPayMethodByMethod(savedCardType).getOldName());

        if (SavedCardType.UPI.getCardType().equals(savedCardType)) {
            savedCardInfo.setCardNumber(cardBeanBiz.getCardNumber());
        } else {
            setInstID(cardBeanBiz, savedCardInfo);

            if (SavedCardType.IMPS.getCardType().equals(savedCardType)) {
                savedCardInfo.setCardNumber(maskImpsPhoneNumber(cardBeanBiz));
                savedCardInfo.setHolderMobileNo(maskImpsPhoneNumber(cardBeanBiz));
                savedCardInfo.setExpiryDate(maskImpsMmid(cardBeanBiz));
            } else {
                savedCardInfo.setCardNumber(maskCardNumber(cardBeanBiz));
                savedCardInfo.setCardScheme(cardBeanBiz.getCardScheme());
            }
            savedCardInfo.setFirstSixDigit(cardBeanBiz.getFirstSixDigit());
            savedCardInfo.setLastFourDigit(getLastFourDigits(cardBeanBiz.getLastFourDigit()));

            if (SavedCardType.DC.getCardType().equals(savedCardType)) {
                setIfIDebitCard(payMethodViews, savedCardInfo);
                setIfLowSuccessRate(cardBeanBiz, savedCardInfo, successRateCacheModel);
            }
        }
        savedCardInfo.setMid(cardBeanBiz.getmId());
        savedCardInfo.setUserId(cardBeanBiz.getUserId());
        savedCardInfo.setCustId(cardBeanBiz.getCustId());
        return savedCardInfo;
    }

    private void setIfLowSuccessRate(final CardBeanBiz cardBeanBiz, final SavedCardInfo savedCardInfo,
            SuccessRateCacheModel successRateCacheModel) {

        if (successRateUtils.checkIfLowSuccessRate(cardBeanBiz.getInstId(),
                PayMethod.getPayMethodByMethod(cardBeanBiz.getCardType()), successRateCacheModel)) {
            savedCardInfo.setIssuerLowSuccessRate(true);
        }

        if (successRateUtils.checkIfLowSuccessRate(cardBeanBiz.getCardScheme(),
                PayMethod.getPayMethodByMethod(cardBeanBiz.getCardType()), successRateCacheModel)) {
            savedCardInfo.setCardSchemeLowSuccessRate(true);
        }
    }

    private void setIfIDebitCard(List<PayMethodViewsBiz> payMethodViews, final SavedCardInfo savedCardInfo) {

        LOGGER.debug("Checking if idebit enabled on card : {}", savedCardInfo.getCardId());
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViews) {

            if (!EPayMethod.DEBIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                continue;
            }

            for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {

                if (!payChannelOptionViewBiz.isEnableStatus()
                        || payChannelOptionViewBiz.getDirectServiceInsts() == null
                        || payChannelOptionViewBiz.getDirectServiceInsts().isEmpty()) {
                    continue;
                }
                boolean isDirectInstService = false;
                boolean isSupportPin = false;
                for (DirectChannelBank directChannel : DirectChannelBank.values()) {

                    if (directChannel.getBankCode().equals(savedCardInfo.getInstId())
                            && payChannelOptionViewBiz.getDirectServiceInsts().contains(directChannel.toString())) {
                        isDirectInstService = true;
                        if (payChannelOptionViewBiz.getSupportAtmPins() != null
                                && payChannelOptionViewBiz.getSupportAtmPins().contains(directChannel.toString())) {
                            isSupportPin = true;
                            break;
                        }
                    }

                }
                LOGGER.debug("isDirectInstService is {} and supportAtmPins is {} for cardId : {}", isDirectInstService,
                        isSupportPin, savedCardInfo.getCardId());
                if (isDirectInstService && isSupportPin) {
                    savedCardInfo.setiDebitCard(true);
                    return;
                }

            }
        }
    }

    private String getLastFourDigits(Long lastFourDigit) {
        if (lastFourDigit > 999) {
            return lastFourDigit.toString();
        }
        String lastFourDigitsPadded = new StringBuilder().append("0000").append(lastFourDigit).toString();
        return lastFourDigitsPadded.substring(lastFourDigitsPadded.length() - 4);
    }

    private void setInstID(CardBeanBiz cardBeanBiz, SavedCardInfo savedCardInfo) {
        BinDetail binDetail = null;
        String binNumber = String.valueOf(cardBeanBiz.getFirstSixDigit());

        try {
            binDetail = cardUtils.fetchBinDetails(binNumber);

            if (binDetail == null)
                return;
            String bankName = StringUtils.isBlank(binDetail.getDisplayBankName()) ? binDetail.getBank() : binDetail
                    .getDisplayBankName();
            savedCardInfo.setBankName(bankName);
            savedCardInfo.setInstId(binDetail.getBankCode());

        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", binNumber, exception);
        }
    }

    private String maskImpsPhoneNumber(CardBeanBiz cardBeanBiz) {
        /*
         * First 6 digit of phone number is visible then 4 four should be
         * masked. Here firt_six_digit fetched from DB are already saved first 6
         * digits of phoneNumber.
         */

        StringBuilder maskPhoneNumber = new StringBuilder();
        maskPhoneNumber.append(cardBeanBiz.getFirstSixDigit()).append("XXXX");
        return maskPhoneNumber.toString();
    }

    private String maskImpsMmid(CardBeanBiz cardBeanBiz) {
        /*
         * First 4 digit of MMID is visible then last 3 should be masked. Here
         * last 4 digits fetched from DB are already saved first 4 digits of
         * MMID.
         */
        StringBuilder maskMmid = new StringBuilder();
        maskMmid.append(cardBeanBiz.getLastFourDigit()).append("XXX");
        return maskMmid.toString();
    }

    private String maskCardNumber(CardBeanBiz cardBeanBiz) {
        if (cardBeanBiz != null) {
            StringBuilder maskCardNumber = new StringBuilder();
            String firstFourDigit = String.valueOf(cardBeanBiz.getFirstSixDigit()).substring(0, 4);
            maskCardNumber.append(firstFourDigit).append(" XXXX XXXX ")
                    .append(getLastFourDigits(cardBeanBiz.getLastFourDigit()));
            return maskCardNumber.toString();
        }
        return null;
    }

    // TODO Remove the suppress warnings
    @SuppressWarnings("null")
    private void filterSavedCardsForPromo(final PaymentRequestBean requestData, final CardInfo cardInfo) {

        PromoCodeResponse promoResponse = requestData.getPromoCodeResponse();

        if (promoResponse == null) {

            final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());
            if (txnInfo != null) {
                promoResponse = txnInfo.getPromoCodeResponse();
            }
        }
        if (promoResponse == null || !ResponseCode.SUCCESS_RESPONSE_CODE.equals(promoResponse.getPromoResponseCode())) {
            return;
        }

        boolean isSavedCardFilteringRequired = false;

        if (promoResponse != null
                && promoResponse.getPromoCodeDetail() != null
                && TheiaConstant.ExtraConstants.PROMOCODE_TYPE_DISCOUNT.equals(promoResponse.getPromoCodeDetail()
                        .getPromocodeTypeName())) {
            for (String payMode : promoResponse.getPromoCodeDetail().getPaymentModes()) {
                if ("CC".equals(payMode) || "DC".equals(payMode)) {
                    isSavedCardFilteringRequired = true;
                    break;

                }
            }
        }

        if (isSavedCardFilteringRequired) {

            if (cardInfo.isSaveCardEnabled()) {
                List<SavedCardInfo> filteredMerchantSavedcardList = new ArrayList<>();
                for (SavedCardInfo savedCard : cardInfo.getMerchantViewSavedCardsList()) {
                    for (String promoCardConfig : promoResponse.getPromoCodeDetail().getPromoCardType()) {
                        String cardScheme = StringUtils.substringBefore(promoCardConfig, "-");
                        String payMode = StringUtils.substringAfter(promoCardConfig, "-");
                        if (payMode.equals("CC")) {
                            payMode = "CREDIT_CARD";
                        } else if (payMode.equals("DC")) {
                            payMode = "DEBIT_CARD";
                        }
                        if (savedCard.getCardScheme().equals(cardScheme) && savedCard.getPaymentMode().equals(payMode)) {
                            filteredMerchantSavedcardList.add(savedCard);
                        }
                    }
                }
                if (filteredMerchantSavedcardList.isEmpty()) {
                    cardInfo.setSaveCardEnabled(false);
                } else {
                    cardInfo.setMerchantViewSavedCardsList(filteredMerchantSavedcardList);
                }
            }

            if (cardInfo.isAddAndPayViewSaveCardEnabled()) {
                List<SavedCardInfo> filteredAddAndPaySavedcardList = new ArrayList<>();
                for (SavedCardInfo savedCard : cardInfo.getMerchantViewSavedCardsList()) {
                    for (String promoCardConfig : promoResponse.getPromoCodeDetail().getPromoCardType()) {
                        String cardScheme = StringUtils.substringBefore(promoCardConfig, "-");
                        String payMode = StringUtils.substringAfter(promoCardConfig, "-");
                        if (payMode.equals("CREDIT_CARD")) {
                            payMode = "CC";
                        } else if (payMode.equals("DEBIT_CARD")) {
                            payMode = "DC";
                        }
                        if (savedCard.getCardScheme().equals(cardScheme) && savedCard.getPaymentMode().equals(payMode)) {
                            filteredAddAndPaySavedcardList.add(savedCard);
                        }
                    }
                }
                if (filteredAddAndPaySavedcardList.isEmpty()) {
                    cardInfo.setAddAndPayViewSaveCardEnabled(false);
                } else {
                    cardInfo.setAddAndPayViewCardsList(filteredAddAndPaySavedcardList);
                }
            }
        }
    }

    public void reOrderSavedCardsForUser(final PaymentRequestBean requestData, final SavedCardRequest savedCardRequest) {

        final CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(requestData.getRequest(), true);

        if (!cardInfo.getMerchantViewSavedCardsList().isEmpty()) {
            cardInfo.setMerchantViewSavedCardsList(filterSavedCardBySelectedSavedCard(savedCardRequest,
                    cardInfo.getMerchantViewSavedCardsList()));
        }
        if (!cardInfo.getAddAndPayViewCardsList().isEmpty()) {
            cardInfo.setAddAndPayViewCardsList(filterSavedCardBySelectedSavedCard(savedCardRequest,
                    cardInfo.getAddAndPayViewCardsList()));
        }

    }

    List<SavedCardInfo> filterSavedCardBySelectedSavedCard(SavedCardRequest savedCard,
            List<SavedCardInfo> savedCardInfos) {
        List<SavedCardInfo> savedCardInfoList = new ArrayList<>();
        for (SavedCardInfo savedCardInfo : savedCardInfos) {
            if (savedCard.getSavedCardId().equals(Long.toString(savedCardInfo.getCardId()))) {
                savedCardInfoList.add(savedCardInfo);
            }
        }
        for (SavedCardInfo savedCardInfo : savedCardInfos) {
            if (!savedCard.getSavedCardId().equals(Long.toString(savedCardInfo.getCardId()))) {
                savedCardInfoList.add(savedCardInfo);
            }
        }
        return savedCardInfoList;
    }
}
