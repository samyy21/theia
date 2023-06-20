/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.promo.service.client.model.PromoCodeApplyRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PromoCodeType;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SAVED_CARD_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SAVED_CARD_ENABLE_ALT;

/**
 * @author amitdubey
 *
 */
@Service
public class TheiaPromoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaPromoUtil.class);

    @Autowired
    @Qualifier("promoServiceHelper")
    IPromoServiceHelper promoServiceHelper;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    public PromoCodeResponse applyPromocode(final TheiaPaymentRequest theiaPaymentRequest,
            final MerchantInfo merchInfo, final TransactionInfo txnInfo, final LoginInfo loginInfo)
            throws PaytmValidationException {
        PromoCodeResponse applyPromoCodeResponse = null;

        if ((null != txnInfo.getPromoCodeResponse())
                && ResponseCodeConstant.PROMO_SUCCESS.equals(txnInfo.getPromoCodeResponse().getPromoResponseCode())) {

            applyPromoCodeResponse = processApplyPromoCodeRequest(txnInfo, merchInfo, theiaPaymentRequest, loginInfo);
            final String promoCodeTypeName = (null == applyPromoCodeResponse)
                    || (null == applyPromoCodeResponse.getPromoCodeDetail()) ? null : applyPromoCodeResponse
                    .getPromoCodeDetail().getPromocodeTypeName();

            String respCode = null == applyPromoCodeResponse ? null : applyPromoCodeResponse.getPromoResponseCode();

            if (null == respCode) {
                throw new PaytmValidationException("Invalid Promo Code Applied",
                        PaytmValidationExceptionType.INVALID_PROMO_DETAILS);
            }

            if (!ResponseCodeConstant.PROMO_APPLIED.equals(respCode)
                    && PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName)) {
                throw new PaytmValidationException("Unable to apply promo code",
                        PaytmValidationExceptionType.INVALID_PROMO_DETAILS);
            }

            if ((applyPromoCodeResponse != null) && (applyPromoCodeResponse.getPromoCodeDetail() != null)) {
                String txnId = txnInfo.getTxnId();
                String[] txnIdParameters = StringUtils.split(txnId, "|");
                if (txnIdParameters.length == 3) {
                    /*
                     * In case txnId is set as mid|orderId|txnId in txnInfo,
                     * extract only txnId
                     */
                    txnId = txnIdParameters[2];
                }

                String key = new StringBuilder(CommonConstants.PROMO_CODE_KEY).append(txnId).toString();

                String timeOut = ConfigurationUtil.getProperty("	");

                if (StringUtils.isBlank(timeOut)) {
                    timeOut = "3600";
                }

                theiaTransactionalRedisUtil.set(key, applyPromoCodeResponse.getPromoCodeDetail().getPromoCode(),
                        Long.valueOf(timeOut));
            }
        }
        return applyPromoCodeResponse;
    }

    private PromoCodeResponse processApplyPromoCodeRequest(final TransactionInfo txnInfo, final MerchantInfo merchInfo,
            final TheiaPaymentRequest theiaPaymentRequest, final LoginInfo loginInfo) throws PaytmValidationException {

        try {
            PromoCodeResponse promoCodeResponse = txnInfo.getPromoCodeResponse();
            String mid = merchInfo.getInternalMid();
            String promoCode = null;
            String txnAmount = txnInfo.getTxnAmount();
            String txnMode = theiaPaymentRequest.getTxnMode();
            String txnId = txnInfo.getTxnId();
            boolean isAddAndPay = TheiaConstant.ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest
                    .getAddMoneyFlag()) ? true : false;

            String cardNumber = theiaPaymentRequest.getCardNo();

            String userID = null;
            if (loginInfo != null && loginInfo.getUser() != null) {
                userID = loginInfo.getUser().getUserID();
            }

            if (!StringUtils.isBlank(theiaPaymentRequest.getSavedCardId())) {
                Long savedCard = Long.parseLong(theiaPaymentRequest.getSavedCardId());

                if (userID == null) {
                    LOGGER.error("No user found for saved card : {}", savedCard);
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
                }
                cardNumber = cashierUtilService.getCardNumer(savedCard, userID);
            }

            if ((null != promoCodeResponse)
                    && ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse.getPromoResponseCode())) {
                PromoCodeData promoCodeData = promoCodeResponse.getPromoCodeDetail();
                if (null != promoCodeData) {
                    promoCode = promoCodeData.getPromoCode();
                }
            }
            if (StringUtils.isNotBlank(promoCode)
                    && ff4JUtils.isFeatureEnabledOnMid(mid, TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT,
                            false)) {
                return null;
            }

            if (StringUtils.isNotBlank(txnMode) && StringUtils.isNotBlank(promoCode) && StringUtils.isNotBlank(txnId)) {

                final PromoCodeApplyRequest promoCodeApplyRequest = new PromoCodeApplyRequest();
                promoCodeApplyRequest.setMerchantId(mid);
                promoCodeApplyRequest.setPromoCode(promoCode);
                promoCodeApplyRequest.setTxnAmount(new BigDecimal(txnAmount));
                promoCodeApplyRequest.setTxnMode(txnMode);
                promoCodeApplyRequest.setTxnId(txnId);
                promoCodeApplyRequest.setCardNumber(cardNumber);
                promoCodeApplyRequest.setBankName(theiaPaymentRequest.getBankCode());
                promoCodeApplyRequest.setUserID(userID);
                promoCodeApplyRequest.setAddAndPay(isAddAndPay);
                if (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())
                        && StringUtils.isNumeric(theiaPaymentRequest.getSavedCardId())) {
                    promoCodeApplyRequest.setSavedCardId(Long.parseLong(theiaPaymentRequest.getSavedCardId()));
                }
                boolean saveChannelInfoAfterPay = SAVED_CARD_ENABLE.equalsIgnoreCase(theiaPaymentRequest
                        .getStoreCardFlag())
                        || SAVED_CARD_ENABLE_ALT.equalsIgnoreCase(theiaPaymentRequest.getStoreCardFlag());
                promoCodeApplyRequest.setCardStoreOption(saveChannelInfoAfterPay);

                return promoServiceHelper.applyPromoCode(promoCodeApplyRequest);
            }
        } catch (final Exception ex) {
            throw new PaytmValidationException("Unable to apply promo Code",
                    PaytmValidationExceptionType.INVALID_PROMO_DETAILS, ex);
        }

        return null;
    }
}