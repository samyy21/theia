package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MobileMaskHelper {

    private static byte unmaskedPrefixLength;
    private static byte unmaskedSuffixLength;
    private static String MASK_CHARACTER;

    private static final byte DEFAULT_UNMASKED_PREFIX_LENGTH = 2;
    private static final byte DEFAULT_UNMASKED_SUFFIX_LENGTH = 2;
    private static final String DEFAULT_MASK_CHARACTER = "X";

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MobileMaskHelper.class);

    static {
        try {
            MobileMaskHelper.unmaskedPrefixLength = Byte.parseByte(ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.MOBILE_NUMBER_UNMASK_PREFIX_LENGTH,
                    String.valueOf(DEFAULT_UNMASKED_PREFIX_LENGTH)));
            MobileMaskHelper.unmaskedSuffixLength = Byte.parseByte(ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.MOBILE_NUMBER_UNMASK_SUFFIX_LENGTH,
                    String.valueOf(DEFAULT_UNMASKED_SUFFIX_LENGTH)));
            MobileMaskHelper.MASK_CHARACTER = ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.MOBILE_NUMBER_MASK_CHARACTER, DEFAULT_MASK_CHARACTER);
        } catch (Exception ex) {
            EXT_LOGGER.error("Exception occured while setting mobile mask configurations {}", ex.toString());
        }

        if (unmaskedPrefixLength < 0 || unmaskedSuffixLength < 0 || unmaskedPrefixLength + unmaskedSuffixLength == 0
                || unmaskedPrefixLength + unmaskedSuffixLength > 9) {
            MobileMaskHelper.unmaskedPrefixLength = DEFAULT_UNMASKED_PREFIX_LENGTH;
            MobileMaskHelper.unmaskedSuffixLength = DEFAULT_UNMASKED_SUFFIX_LENGTH;
        }

        if (StringUtils.isBlank(MobileMaskHelper.MASK_CHARACTER) || MobileMaskHelper.MASK_CHARACTER.length() > 1) {
            MobileMaskHelper.MASK_CHARACTER = DEFAULT_MASK_CHARACTER;
        }

        EXT_LOGGER
                .info("Setting unmaskedPrefixLength configuration value as {}", MobileMaskHelper.unmaskedPrefixLength);
        EXT_LOGGER
                .info("Setting unmaskedSuffixLength configuration value as {}", MobileMaskHelper.unmaskedSuffixLength);
        EXT_LOGGER.info("Setting MASK_CHARACTER configuration value as {}", MobileMaskHelper.MASK_CHARACTER);
    }

    public String getMaskedNumber(String mobile) {
        if (StringUtils.isNotBlank(mobile) && mobile.length() >= 10) {
            StringBuffer sbMobile = new StringBuffer(mobile);
            sbMobile.replace(mobile.length() - 10 + MobileMaskHelper.unmaskedPrefixLength, mobile.length()
                    - MobileMaskHelper.unmaskedSuffixLength, StringUtils.repeat(MASK_CHARACTER.charAt(0),
                    10 - (MobileMaskHelper.unmaskedPrefixLength + MobileMaskHelper.unmaskedSuffixLength)));
            EXT_LOGGER.customInfo("Masked mobile number : {}", sbMobile);
            return sbMobile.toString();
        } else {
            EXT_LOGGER.customError("Unable to mask mobile number, returning unmasked number.");
            return mobile;
        }
    }

    public String getMaskCharacter() {
        return MobileMaskHelper.MASK_CHARACTER;
    }

    public boolean isValidMaskedMobileNumber(String mobile) {
        return (StringUtils.isNotBlank(mobile) && mobile.length() >= 10 && mobile.contains(this.getMaskCharacter())) ? true
                : false;
    }
}
