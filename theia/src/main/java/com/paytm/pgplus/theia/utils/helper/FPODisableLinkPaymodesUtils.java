package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.FPODisablePaymentMode;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FPODisableLinkPaymodesUtils implements Serializable {

    private static final long serialVersionUID = 1281487678469042663L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FPODisableLinkPaymodesUtils.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FPODisableLinkPaymodesUtils.class);

    public static List<FPODisablePaymentMode> fpoDisablePaymentModeList = getFPODisabledPaymodes();

    private static List<FPODisablePaymentMode> getFPODisabledPaymodes() {
        List<FPODisablePaymentMode> fpoDisablePaymentModeList = null;
        try {
            String fpoDisablePaymodes = ConfigurationUtil
                    .getProperty(TheiaConstant.ExtraConstants.FPO_DISBALE_PAYMODES_GENERIC);
            EXT_LOGGER.customInfo(".getLinkDisabledPaymodes | fpoDisablePaymodes = {}", fpoDisablePaymodes);
            List<String> fpoDisablePaymodesList = StringUtils.isNotBlank(fpoDisablePaymodes) ? Arrays
                    .asList(fpoDisablePaymodes.split(";")) : null;
            if (fpoDisablePaymodesList != null) {
                FPODisablePaymentMode fpoDisablePaymentMode = null;
                for (String fpoDisablePayMode : fpoDisablePaymodesList) {
                    // LOGGER.info(
                    // ".getLinkDisabledPaymodes | fpoDisablePayMode = {}",
                    // fpoDisablePayMode);
                    List<String> fpoDisablePaymodeVars = StringUtils.isNotBlank(fpoDisablePayMode) ? Arrays
                            .asList(fpoDisablePayMode.split(",")) : null;
                    if (fpoDisablePaymodeVars != null && fpoDisablePaymodeVars.size() > 0) {
                        if (fpoDisablePaymentModeList == null) {
                            fpoDisablePaymentModeList = new ArrayList<>();
                        }
                        fpoDisablePaymentMode = new FPODisablePaymentMode();
                        for (int i = 0; i < fpoDisablePaymodeVars.size(); i++) {
                            String[] intanceVar = StringUtils.isNotBlank(fpoDisablePaymodeVars.get(i)) ? fpoDisablePaymodeVars
                                    .get(i).split(":") : null;
                            if (intanceVar != null && intanceVar.length == 2) {
                                if (TheiaConstant.ExtraConstants.PAYMODE.equals(intanceVar[0])
                                        && StringUtils.isNotBlank(intanceVar[1])) {
                                    fpoDisablePaymentMode.setPaymode(intanceVar[1].toUpperCase());
                                } else if (TheiaConstant.ExtraConstants.MERCHANT_TYPE.equals(intanceVar[0])
                                        && StringUtils.isNotBlank(intanceVar[1])) {
                                    fpoDisablePaymentMode.setMerchantType(intanceVar[1]);
                                } else if (TheiaConstant.ExtraConstants.PAYMODE_CHANNELS.equals(intanceVar[0])
                                        && StringUtils.isNotBlank(intanceVar[1])) {
                                    fpoDisablePaymentMode.setPaymodeChannels(Arrays.asList(intanceVar[1].toUpperCase()
                                            .split("_")));
                                } else if (TheiaConstant.ExtraConstants.MERCHANT_LIMIT_TYPE.equals(intanceVar[0])
                                        && StringUtils.isNotBlank(intanceVar[1])) {
                                    fpoDisablePaymentMode.setMerchantLimitType(Arrays.asList(intanceVar[1].split("_")));
                                }
                            }
                        }
                        // LOGGER.info(
                        // " FPODisablePaymentMode = {}",
                        // fpoDisablePaymentMode);
                        // fpoDisablePaymentModeList.add(fpoDisablePaymentMode);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in get linkdisablepaymodes :{}", e);
        }
        LOGGER.info(" FPODisablePaymentModeList = {}", fpoDisablePaymentModeList);
        return fpoDisablePaymentModeList;
    }
}
