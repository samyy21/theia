package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.utils.BinRestrictedCard;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.CheckoutPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersServiceResponse;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CardCheckoutPaymentPromoReqBuilder extends BaseCheckoutPaymentPromoReqBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardCheckoutPaymentPromoReqBuilder.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(CardCheckoutPaymentPromoReqBuilder.class);

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    @Override
    public CheckoutPromoServiceRequest build(WorkFlowRequestBean workFlowRequestBean) {
        CheckoutPromoServiceRequest checkoutPromoServiceRequest = super.build(workFlowRequestBean);
        PaymentOption paymentOption = getCardPayOption(checkoutPromoServiceRequest.getPaymentOptions(),
                workFlowRequestBean.getPaytmExpressAddOrHybrid());
        /*
         * in case of addnpay promo paymode should be balance and no other card
         * details should be set
         */
        if (PayMethod.BALANCE.equals(paymentOption.getPayMethod())) {
            return checkoutPromoServiceRequest;
        }
        paymentOption.setBin6(parseFistNDigit(workFlowRequestBean.getCardNo(), 6));
        String mid = workFlowRequestBean.getPaytmMID();
        String savedCardID = workFlowRequestBean.getSavedCardID();
        boolean isCardIndexNumberExist = null != savedCardID ? savedCardID.length() > 15 ? true : false : false;
        boolean sentBin8Hash = false;
        try {
            if (isCardIndexNumberExist) {
                paymentOption.setCardIndexNo(savedCardID);
            } else if (ff4JUtil.isFeatureEnabledForPromo(mid)) {
                paymentOption.setCardIndexNo(workFlowRequestBean.getCardIndexNo());
                sentBin8Hash = true;
            } else {
                paymentOption.setCardIndexNo(SignatureUtilWrapper.signApiRequest(workFlowRequestBean.getCardNo()));
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BizPaymentOfferCheckoutException();
        }
        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!workFlowRequestBean.getPayMethod().equals(binDetail.getCardType())
                && !((EPayMethod.EMI.getMethod().equals(workFlowRequestBean.getPayMethod()) && EPayMethod.CREDIT_CARD
                        .getMethod().equals(binDetail.getCardType())) || (EPayMethod.EMI_DC.getMethod().equals(
                        workFlowRequestBean.getPayMethod()) && EPayMethod.DEBIT_CARD.getMethod().equals(
                        binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw new BizPaymentOfferCheckoutException();
        }
        String bankCode = binDetail.getBankCode();
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        if (workFlowRequestBean.isBin8OfferAvailableOnMerchant()
                && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                        .contains(networkCode))) {
            LOGGER.info("Setting bin8 for mid = {}, bank = {}, network = {}", workFlowRequestBean.getPaytmMID(),
                    bankCode, networkCode);
            if (isCardIndexNumberExist || sentBin8Hash) {
                String eightDigitBinHash = getAndSetBin8Alias(paymentOption.getCardIndexNo(),
                        workFlowRequestBean.getPaytmMID());
                if (StringUtils.isNotEmpty(eightDigitBinHash)) {
                    paymentOption.setBin8Hash(eightDigitBinHash);
                } else {
                    throw new BizPaymentOfferCheckoutException();
                }
            } else {
                paymentOption.setBin8(String.valueOf((parseFistNDigit(workFlowRequestBean.getCardNo(), 8))));
            }
        }
        return checkoutPromoServiceRequest;
    }

    @Override
    public CheckoutPromoServiceRequestV2 buildV2(WorkFlowRequestBean workFlowRequestBean) {
        CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = super.buildV2(workFlowRequestBean);
        PaymentOption paymentOption = getCardPayOption(checkoutPromoServiceRequest.getPaymentDetails()
                .getPaymentOptions(), workFlowRequestBean.getPaytmExpressAddOrHybrid());
        /*
         * in case of addnpay promo paymode should be balance and no other card
         * details should be set
         */
        if (PayMethod.BALANCE.equals(paymentOption.getPayMethod())) {
            return checkoutPromoServiceRequest;
        }
        paymentOption.setBin6(parseFistNDigit(workFlowRequestBean.getCardNo(), 6));
        String mid = workFlowRequestBean.getPaytmMID();
        String savedCardID = workFlowRequestBean.getSavedCardID();
        boolean isCardIndexNumberExist = null != savedCardID ? savedCardID.length() > 15 ? true : false : false;
        boolean sentBin8Hash = false;
        try {
            if (isCardIndexNumberExist) {
                paymentOption.setCardIndexNo(savedCardID);
            } else if (ff4JUtil.isFeatureEnabledForPromo(mid)) {
                paymentOption.setCardIndexNo(workFlowRequestBean.getCardIndexNo());
                sentBin8Hash = true;
            } else {
                paymentOption.setCardIndexNo(SignatureUtilWrapper.signApiRequest(workFlowRequestBean.getCardNo()));
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BizPaymentOfferCheckoutException();
        }
        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!workFlowRequestBean.getPayMethod().equals(binDetail.getCardType())
                && !((EPayMethod.EMI.getMethod().equals(workFlowRequestBean.getPayMethod()) && EPayMethod.CREDIT_CARD
                        .getMethod().equals(binDetail.getCardType())) || (EPayMethod.EMI_DC.getMethod().equals(
                        workFlowRequestBean.getPayMethod()) && EPayMethod.DEBIT_CARD.getMethod().equals(
                        binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw new BizPaymentOfferCheckoutException();
        }
        String bankCode = binDetail.getBankCode();
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        if (workFlowRequestBean.isBin8OfferAvailableOnMerchant()
                && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                        .contains(networkCode))) {
            LOGGER.info("Setting bin8 for mid = {}, bank = {}, network = {}", workFlowRequestBean.getPaytmMID(),
                    bankCode, networkCode);
            if (isCardIndexNumberExist || sentBin8Hash) {
                String eightDigitBinHash = getAndSetBin8Alias(paymentOption.getCardIndexNo(),
                        workFlowRequestBean.getPaytmMID());
                if (StringUtils.isNotEmpty(eightDigitBinHash)) {
                    paymentOption.setBin8Hash(eightDigitBinHash);
                } else {
                    throw new BizPaymentOfferCheckoutException();
                }
            } else {
                paymentOption.setBin8(String.valueOf((parseFistNDigit(workFlowRequestBean.getCardNo(), 8))));
            }
        }
        return checkoutPromoServiceRequest;
    }

    @Override
    public CheckoutPromoServiceRequestV2 buildCoftV2(WorkFlowRequestBean workFlowRequestBean) {
        CheckoutPromoServiceRequestV2 checkoutPromoServiceRequest = super.buildCoftV2(workFlowRequestBean);
        PaymentOption paymentOption = getCardPayOption(checkoutPromoServiceRequest.getPaymentDetails()
                .getPaymentOptions(), workFlowRequestBean.getPaytmExpressAddOrHybrid());
        /*
         * in case of addnpay promo paymode should be balance and no other card
         * details should be set
         */
        if (PayMethod.BALANCE.equals(paymentOption.getPayMethod())) {
            return checkoutPromoServiceRequest;
        }

        try {
            setCardIndexNo(workFlowRequestBean, paymentOption);
            setBin6(workFlowRequestBean, paymentOption);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BizPaymentOfferCheckoutException();
        }

        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!workFlowRequestBean.getPayMethod().equals(binDetail.getCardType())
                && !((EPayMethod.EMI.getMethod().equals(workFlowRequestBean.getPayMethod()) && EPayMethod.CREDIT_CARD
                        .getMethod().equals(binDetail.getCardType())) || (EPayMethod.EMI_DC.getMethod().equals(
                        workFlowRequestBean.getPayMethod()) && EPayMethod.DEBIT_CARD.getMethod().equals(
                        binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw new BizPaymentOfferCheckoutException();
        }
        String bankCode = binDetail.getBankCode();
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        if (workFlowRequestBean.isBin8OfferAvailableOnMerchant()
                && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                        .contains(networkCode))) {
            LOGGER.info("Setting bin8 for mid = {}, bank = {}, network = {}", workFlowRequestBean.getPaytmMID(),
                    bankCode, networkCode);
            String eightDigitBinHash = getBin8AliasForCoft(workFlowRequestBean, paymentOption);
            if (StringUtils.isNotEmpty(eightDigitBinHash)) {
                paymentOption.setBin8(eightDigitBinHash);
            } else {
                throw new BizPaymentOfferCheckoutException();
            }
        }
        return checkoutPromoServiceRequest;
    }

    private void setBin6(WorkFlowRequestBean workFlowRequestBean, PaymentOption paymentOption) {
        String bin6 = null;
        if (StringUtils.isNotEmpty(workFlowRequestBean.getSavedCardID())) {
            String savedCardId = workFlowRequestBean.getSavedCardID();
            if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                bin6 = workFlowRequestBean.getAccountRangeCardBin();
            } else if (savedCardId.length() > 15) {
                bin6 = workFlowRequestBean.getCardNo();
            }
        } else if (Objects.nonNull(workFlowRequestBean.getCardTokenInfo())) {
            bin6 = workFlowRequestBean.getAccountRangeCardBin();
        } else if (StringUtils.isNotEmpty(workFlowRequestBean.getCardNo())) {
            bin6 = workFlowRequestBean.getCardNo();
        }
        paymentOption.setBin6(parseFistNDigit(bin6, 6));
    }

    private void setCardIndexNo(WorkFlowRequestBean workFlowRequestBean, PaymentOption paymentOption) {
        String merchantCoftConfig = workFlowRequestBean.getMerchantCoftConfig();
        String uniquePromoIdentifier = null;
        if (merchantCoftConfig.equals("GCIN")) {
            uniquePromoIdentifier = workFlowRequestBean.getGcin();
        } else if (merchantCoftConfig.equals("PAR")) {
            uniquePromoIdentifier = workFlowRequestBean.getPar();
        }
        paymentOption.setCardIndexNo(uniquePromoIdentifier);
    }

    private String getSearchPaymentOffersCacheKey(String mid, Class cachedClass) {
        return cachedClass.getCanonicalName() + "_" + mid;
    }

    private SearchPaymentOffersServiceResponse getCachedSearchPaymentOffersServiceResponse(String mid) {
        return (SearchPaymentOffersServiceResponse) theiaTransactionalRedisUtil.get(getSearchPaymentOffersCacheKey(mid,
                SearchPaymentOffersServiceResponse.class));
    }

    // List will have wallet in addition to card paymethod in case of hybrid
    private BinDetail binDetail(int binNumber) {
        try {
            if (iPgpFf4jClient.checkWithDefault(BizConstant.Ff4jFeature.SIX_DIGIT_BIN_LOGGING, false) && binNumber > 0
                    && String.valueOf(binNumber).length() == 6) {
                LOGGER.info("Bin Number length is 6.");
            }

            BinDetail binDetail = binFetchService.getCardBinDetail((long) binNumber);
            EXT_LOGGER.customInfo("Mapping response - BinDetail :: {}", binDetail);
            if (binDetail != null) {
                return binDetail;
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        throw new BizPaymentOfferCheckoutException();
    }

    private int parseFistNDigit(String number, int n) {
        if (NumberUtils.isNumber(number)) {
            if (n > number.length()) {
                n = number.length();
            }
            return NumberUtils.toInt(number.substring(0, n));
        }
        return 0;
    }

    private PaymentOption getCardPayOption(List<PaymentOption> paymentOptions, EPayMode payMode) {
        for (PaymentOption paymentOption : paymentOptions) {
            // supporting addnoay and promo on wallet
            if (EPayMode.ADDANDPAY == payMode && PayMethod.BALANCE.equals(paymentOption.getPayMethod())) {
                return paymentOption;
            }
            if (PayMethod.BALANCE != paymentOption.getPayMethod()) {
                return paymentOption;
            }
        }
        throw new BizPaymentOfferCheckoutException();
    }

    @Override
    public void validateWorkflowReqForCheckoutPaymentPromo(WorkFlowRequestBean promoPaymentOption) {
        super.validateWorkflowReqForCheckoutPaymentPromo(promoPaymentOption);
        if (StringUtils.isNotBlank(promoPaymentOption.getCardNo())) {
            return;
        }
        throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
    }

    private String getAndSetBin8Alias(String CIN, String paytmMid) {

        String eightDigitBinHash = getCachedEightDigitBinHash(getApplyPromoForCachedKey(CIN),
                TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
        if (StringUtils.isEmpty(eightDigitBinHash)) {
            QueryNonSensitiveAssetInfoResponse response = cardCenterHelper.queryNonSensitiveAssetInfo(null, CIN);
            if (null != response) {
                eightDigitBinHash = response.getCardInfo().getExtendInfo()
                        .get(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
            }
        }
        return eightDigitBinHash;
    }

    private String getBin8AliasForCoft(WorkFlowRequestBean workFlowRequestBean, PaymentOption paymentOption) {
        String eightDigitBinHash = getCachedEightDigitBinHash(
                getApplyPromoForCachedKey(paymentOption.getCardIndexNo()),
                TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
        if (StringUtils.isEmpty(eightDigitBinHash)) {
            if (StringUtils.isNotEmpty(workFlowRequestBean.getSavedCardID())) {
                String savedCardId = workFlowRequestBean.getSavedCardID();
                if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                    eightDigitBinHash = getEightDigitBinHash(workFlowRequestBean.getAccountRangeCardBin());
                } else if (savedCardId.length() > 15) {
                    QueryNonSensitiveAssetInfoResponse response = cardCenterHelper.queryNonSensitiveAssetInfo(null,
                            savedCardId);
                    if (null != response) {
                        eightDigitBinHash = response.getCardInfo().getExtendInfo()
                                .get(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH);
                    }
                }
            } else if (Objects.nonNull(workFlowRequestBean.getCardTokenInfo())) {
                eightDigitBinHash = getEightDigitBinHash(workFlowRequestBean.getAccountRangeCardBin());
            } else if (StringUtils.isNotEmpty(workFlowRequestBean.getCardNo())) {
                eightDigitBinHash = getEightDigitBinHash(workFlowRequestBean.getCardNo());
            }
        }
        return eightDigitBinHash;
    }

    private String getEightDigitBinHash(String cardNo) {
        if (StringUtils.isNotEmpty(cardNo)) {
            CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNo.substring(0, 8));
            if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                return cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
            }
        }
        return null;
    }

    private String getCachedEightDigitBinHash(String key, String field) {
        return (String) theiaSessionRedisUtil.hget(key, field);
    }

    public static String getApplyPromoForCachedKey(String cardIndexNumber) {
        return cardIndexNumber;
    }
}
