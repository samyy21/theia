package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.BinDetailWithDisplayName;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.ADD_MONEY_FLAG_VALUE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PaymentFlag.ADD_MONEY_FLAG;

@Service("binUtils")
public class BinUtils {

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private Ff4jUtils ff4jUtil;
    private static Ff4jUtils ff4jUtils;

    @PostConstruct
    private void init() {
        ff4jUtils = this.ff4jUtil;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BinUtils.class);

    public boolean validateBinDetails(String bin) {
        return StringUtils.isNotBlank(bin) && (bin.length() >= 6) && StringUtils.isNumeric(bin);
    }

    public BinDetail retrieveBinDetails(String bin) {
        BinDetail binDetail;
        try {
            binDetail = cardUtils.fetchBinDetails(bin);
            if (binDetail == null)
                return null;
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", bin, exception);
            return null;
        }
        return binDetail;
    }

    public BinDetailWithDisplayName retrieveBinDetailsWithDisplayName(String bin) {
        BinDetailWithDisplayName binDetailWithDisplayName;
        try {
            binDetailWithDisplayName = cardUtils.fetchBinDetailsWithDisplayName(bin);
            if (binDetailWithDisplayName == null)
                return null;
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", bin, exception);
            return null;
        }
        return binDetailWithDisplayName;
    }

    public boolean checkIfCardEnabled(HttpServletRequest request, BinDetail binDetail) {

        if (binDetail == null) {
            return false;
        }
        String addMoneyFlag = request.getParameter(ADD_MONEY_FLAG);
        boolean isAddAndPaySelected = ADD_MONEY_FLAG_VALUE.equals(addMoneyFlag);
        EntityPaymentOptionsTO entityPaymentoptions = theiaSessionDataService.getEntityPaymentOptions(request);

        if (entityPaymentoptions == null) {
            return false;
        }

        if (PayMethod.DEBIT_CARD.getMethod().equals(binDetail.getCardType())) {
            if (isAddAndPaySelected) {
                return checkMerchantEnabledCardList(entityPaymentoptions.getAddCompleteDcList(),
                        binDetail.getCardName());
            } else {
                return checkMerchantEnabledCardList(entityPaymentoptions.getCompleteDcList(), binDetail.getCardName());
            }
        }

        if (PayMethod.CREDIT_CARD.getMethod().equals(binDetail.getCardType())) {
            if (isAddAndPaySelected) {
                return checkMerchantEnabledCardList(entityPaymentoptions.getAddCompleteCcList(),
                        binDetail.getCardName());
            } else {
                return checkMerchantEnabledCardList(entityPaymentoptions.getCompleteCcList(), binDetail.getCardName());
            }
        }
        return false;
    }

    public boolean checkMerchantEnabledCardList(List<BankInfo> cardList, String cardName) {
        boolean isCardEnabled = false;

        if (cardList == null) {
            return isCardEnabled;
        }

        try {
            for (BankInfo info : cardList) {
                if (cardName.equalsIgnoreCase(info.getBankName())) {
                    isCardEnabled = true;
                    break;
                }
            }
        } catch (NullPointerException npe) {
            LOGGER.error("Unable to validate", npe);
        }

        return isCardEnabled;
    }

    public static void logSixDigitBinLength(String binNumber) {
        if (ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.SIX_DIGIT_BIN_LOGGING, false)
                && StringUtils.isNotBlank(binNumber) && binNumber.length() == 6) {
            LOGGER.info("Bin Number Length is 6.");
        }
    }
}
