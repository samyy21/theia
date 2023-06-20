package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.biz.utils.BinRestrictedCard;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import com.paytm.pgplus.theia.utils.BinUtils;
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
public class CardPaymentOptionBuilder extends BasePaymentOptionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardPaymentOptionBuilder.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(CardPaymentOptionBuilder.class);

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    @Qualifier("savedCardService")
    ISavedCardService savedCardService;

    @Autowired
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("coftTokenDataService")
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public PaymentOption build(PromoPaymentOption promoPaymentOption, String mid) {
        PaymentOption paymentOption = super.build(promoPaymentOption, mid);
        String savedCardId = promoPaymentOption.getSavedCardId();
        boolean isCardIndexNumberExist = StringUtils.isNotEmpty(savedCardId) ? savedCardId.length() > 15 ? true : false
                : false;
        if (!isCardIndexNumberExist) {
            setCardNumberForSavedCardId(promoPaymentOption);
        }
        paymentOption.setBin6(parseFistNDigit(promoPaymentOption.getCardNo(), 6));
        try {
            if (!isCardIndexNumberExist) {
                paymentOption.setCardIndexNo(SignatureUtilWrapper.signApiRequest(promoPaymentOption.getCardNo()));
            } else {
                paymentOption.setCardIndexNo(savedCardId);
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }
        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!promoPaymentOption.getPayMethod().getMethod().equals(binDetail.getCardType())
                && !((PayMethod.EMI == promoPaymentOption.getPayMethod() && PayMethod.CREDIT_CARD.getMethod().equals(
                        binDetail.getCardType())) || (PayMethod.EMI_DC == promoPaymentOption.getPayMethod() && PayMethod.DEBIT_CARD
                        .getMethod().equals(binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw RequestValidationException.getException();
        }
        String bankCode = binDetail.getBankCode();
        if (StringUtils.isNotBlank(promoPaymentOption.getBankCode())
                && !promoPaymentOption.getBankCode().equals(bankCode)) {
            LOGGER.error("BankCode from card bin is different from bankcode received in the request");
            throw RequestValidationException.getException();
        }
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);
        boolean migrateBankOffersPromo = ff4JUtil.isMigrateBankOffersPromo(mid);
        boolean bin8Available = false;
        if (migrateBankOffersPromo) {
            bin8Available = paymentOffersServiceHelperV2.isBin8OfferAvailableOnMerchant(mid, null,
                    promoPaymentOption.getPromoContext());
        } else {
            bin8Available = paymentOffersServiceHelper.isBin8OfferAvailableOnMerchant(mid, null);
        }
        if (bin8Available
                && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                        .contains(networkCode))) {
            setBin8Response(mid, bankCode, networkCode, isCardIndexNumberExist, paymentOption, promoPaymentOption);
        }
        if (StringUtils.isNotBlank(promoPaymentOption.getTenure())) {
            paymentOption.setTenure(Integer.valueOf(promoPaymentOption.getTenure()));
        }
        return paymentOption;
    }

    @Override
    public PaymentOption buildForCoftPromoTxns(PromoPaymentOption promoPaymentOption, String mid, String txnToken) {
        PaymentOption paymentOption = super.buildForCoftPromoTxns(promoPaymentOption, mid, txnToken);
        String merchantCoftConfig = coftTokenDataService.getMerchantConfig(mid);
        boolean bin8Available = paymentOffersServiceHelperV2.isBin8Available(mid, null,
                promoPaymentOption.getPromoContext());

        try {
            if (StringUtils.isEmpty(promoPaymentOption.getUniquePromoIdentifier())) {
                if (StringUtils.isNotEmpty(promoPaymentOption.getSavedCardId())) {
                    paymentOffersServiceHelperV2.processForCoftSavedCardId(promoPaymentOption, mid, merchantCoftConfig,
                            txnToken, bin8Available);
                } else if (Objects.nonNull(promoPaymentOption.getCardTokenInfo())) {
                    paymentOffersServiceHelperV2.processForCoftTokenCards(promoPaymentOption, mid, merchantCoftConfig,
                            bin8Available);
                } else if (StringUtils.isNotEmpty(promoPaymentOption.getCardNo())) {
                    paymentOffersServiceHelperV2.processForCoftCardNumber(promoPaymentOption, mid, bin8Available,
                            merchantCoftConfig);
                } else {
                    throw RequestValidationException.getException();
                }
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }

        paymentOption.setCardIndexNo(promoPaymentOption.getUniquePromoIdentifier());

        paymentOption.setBin6(parseFistNDigit(promoPaymentOption.getCardNo(), 6));
        BinDetail binDetail = binDetail(paymentOption.getBin6());
        if (!promoPaymentOption.getPayMethod().getMethod().equals(binDetail.getCardType())
                && !((PayMethod.EMI == promoPaymentOption.getPayMethod() && PayMethod.CREDIT_CARD.getMethod().equals(
                        binDetail.getCardType())) || (PayMethod.EMI_DC == promoPaymentOption.getPayMethod() && PayMethod.DEBIT_CARD
                        .getMethod().equals(binDetail.getCardType())))) {
            LOGGER.error("Not a valid card number for payMethod");
            throw RequestValidationException.getException();
        }
        String bankCode = binDetail.getBankCode();
        if (StringUtils.isNotBlank(promoPaymentOption.getBankCode())
                && !promoPaymentOption.getBankCode().equals(bankCode)) {
            LOGGER.error("BankCode from card bin is different from bankcode received in the request");
            throw RequestValidationException.getException();
        }
        String networkCode = binDetail.getCardName();
        paymentOption.setIssuingBank(bankCode);
        paymentOption.setIssuingNetworkCode(networkCode);

        if (bin8Available
                && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                        .contains(networkCode))) {
            setBin8ResponseForCoftPromoTxn(mid, bankCode, networkCode, paymentOption, promoPaymentOption);
        }
        if (StringUtils.isNotBlank(promoPaymentOption.getTenure())) {
            paymentOption.setTenure(Integer.valueOf(promoPaymentOption.getTenure()));
        }
        return paymentOption;
    }

    private void setBin8Response(String mid, String bankCode, String networkCode, boolean isCardIndexNumberExist,
            PaymentOption paymentOption, PromoPaymentOption promoPaymentOption) {
        LOGGER.info("Setting bin8 for mid = {}, bank = {}, network = {}", mid, bankCode, networkCode);
        if (!isCardIndexNumberExist) {
            paymentOption.setBin8(String.valueOf((parseFistNDigit(promoPaymentOption.getCardNo(), 8))));
        } else {
            if (StringUtils.isNotEmpty(promoPaymentOption.getEightDigitBinHash())) {
                paymentOption.setBin8Hash(promoPaymentOption.getEightDigitBinHash());
            } else {
                LOGGER.error("Couldn't find bin8Hash");
                throw BaseException.getException();
            }
        }
    }

    private void setBin8ResponseForCoftPromoTxn(String mid, String bankCode, String networkCode,
            PaymentOption paymentOption, PromoPaymentOption promoPaymentOption) {
        LOGGER.info("Setting bin8 for mid = {}, bank = {}, network = {}", mid, bankCode, networkCode);
        if (StringUtils.isNotEmpty(promoPaymentOption.getEightDigitBinHash())) {
            paymentOption.setBin8(promoPaymentOption.getEightDigitBinHash());
        } else {
            LOGGER.error("Couldn't find bin8Hash");
            throw BaseException.getException();
        }
    }

    private void setCardNumberForSavedCardId(PromoPaymentOption promoPaymentOption) {
        if (StringUtils.isNotBlank(promoPaymentOption.getSavedCardId())
                && StringUtils.isBlank(promoPaymentOption.getCardNo())) {
            LOGGER.info("Promo apply request with savedCardId = {}", promoPaymentOption.getSavedCardId());
            SavedCardResponse<SavedCardVO> savedCardResponse = savedCardService.getSavedCardByCardId(Long
                    .parseLong(promoPaymentOption.getSavedCardId()));
            if (savedCardResponse.getStatus()) {
                promoPaymentOption.setCardNo(savedCardResponse.getResponseData().getCardNumber());
            } else {
                LOGGER.error("Error in fetching savedcard by savedcardId = {}, errorMsg = {}",
                        promoPaymentOption.getSavedCardId(), savedCardResponse.getMessage());
                throw BaseException.getException();
            }
        }
    }

    private BinDetail binDetail(int binNumber) {
        try {
            BinUtils.logSixDigitBinLength(Integer.toString(binNumber));
            BinDetail binDetail = binFetchService.getCardBinDetail((long) binNumber);
            EXT_LOGGER.customInfo("Mapping response - BinDetail :: {}", binDetail);
            if (binDetail != null) {
                return binDetail;
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        throw BaseException.getException();
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

    @Override
    public void validatePromoPaymentOption(PromoPaymentOption promoPaymentOption) {
        super.validatePromoPaymentOption(promoPaymentOption);
        if (StringUtils.isNotBlank(promoPaymentOption.getCardNo())
                || StringUtils.isNotBlank(promoPaymentOption.getSavedCardId())
                || Objects.nonNull(promoPaymentOption.getCardTokenInfo())) {
            return;
        }
        throw RequestValidationException.getException();
    }

    @Override
    public List<PromoPaymentOption> buildPromoPaymentOptions(WorkFlowRequestBean workFlowRequestBean, String txnAmount,
            String paymentMethod, String issuingBank) {
        List<PromoPaymentOption> paymentOptions = super.buildPromoPaymentOptions(workFlowRequestBean, txnAmount,
                paymentMethod, issuingBank);
        PromoPaymentOption paymentOption = getCardPayOption(paymentOptions);
        paymentOption.setCardNo(workFlowRequestBean.getCardNo());
        paymentOption.setSavedCardId(workFlowRequestBean.getSavedCardID());
        paymentOption.setCardTokenInfo(workFlowRequestBean.getCardTokenInfo());
        return paymentOptions;
    }

    private PromoPaymentOption getCardPayOption(List<PromoPaymentOption> paymentOptions) {
        for (PromoPaymentOption paymentOption : paymentOptions) {
            if (PayMethod.BALANCE != paymentOption.getPayMethod()) {
                return paymentOption;
            }
        }
        return null;
    }

}
