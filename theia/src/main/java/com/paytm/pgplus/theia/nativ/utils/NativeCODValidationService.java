package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("nativeCODValidationService")
public class NativeCODValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCODValidationService.class);
    @Autowired
    @Qualifier("nativeCodUtils")
    NativeCODUtils nativeCodUtils;
    @Autowired
    @Qualifier("theiaSessionDataServiceAdapterNative")
    private ITheiaSessionDataService theiaSessionDataServiceAdapterNative;

    public boolean isValidPaymentFlowAndMode(PaymentRequestBean requestData) {

        // Minimum COD amount check for Hybrid COD
        if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_0.equals(requestData.isAddMoney())) {
            WalletInfo walletInfo = theiaSessionDataServiceAdapterNative.getWalletInfoFromSession(requestData
                    .getRequest());
            String txnAmount = requestData.getTxnAmount();
            if (NumberUtils.isNumber(txnAmount)) {
                boolean supportCodHybrid = true;
                String minimumCodAmount = nativeCodUtils.getMinimumCodAmount();
                if (minimumCodAmount != null) {
                    double minCodAmount = Double.parseDouble(minimumCodAmount);
                    if (minCodAmount > (Double.parseDouble(txnAmount) - walletInfo.getWalletBalance())) {
                        LOGGER.info("COD Hybrid not supported because of amount checks");
                        supportCodHybrid = false;
                    }
                }
                return supportCodHybrid;
            }

        }
        return true;
    }

}
