package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.cache.model.EmiOnDcResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IEmiOnDcDetails;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

/**
 * Created by charu on 02/10/18.
 */

@Service("emiUtil")
public class EmiUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmiUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EmiUtil.class);

    @Autowired
    @Qualifier("emiOnDcDetails")
    IEmiOnDcDetails emiDetails;

    @Autowired
    Environment env;

    private static final Map<String, Set<String>> dcEmiEligibility = new HashMap<>();

    static {
        createMapForDcEmiEligibility();
    }

    private static void createMapForDcEmiEligibility() {
        try {
            dcEmiEligibility.put(
                    DC_EMI_ELIGIBILITY_DEFAULT_TRUE,
                    new HashSet<>(Arrays.asList(ConfigurationUtil.getProperty(DC_EMI_ELIGIBILITY_DEFAULT_TRUE, "")
                            .split(","))));
            dcEmiEligibility.put(
                    DC_EMI_ELIGIBILITY_CHECK_DB,
                    new HashSet<>(Arrays.asList(ConfigurationUtil.getProperty(DC_EMI_ELIGIBILITY_CHECK_DB, "").split(
                            ","))));
            dcEmiEligibility.put(
                    DC_EMI_ELIGIBILITY_CHECK_MOBILE_NUMBER,
                    new HashSet<>(Arrays.asList(ConfigurationUtil.getProperty(DC_EMI_ELIGIBILITY_CHECK_MOBILE_NUMBER,
                            "").split(","))));
            dcEmiEligibility.put(
                    DC_EMI_ELIGIBILITY_DEFAULT_FALSE,
                    new HashSet<>(Arrays.asList(ConfigurationUtil.getProperty(DC_EMI_ELIGIBILITY_DEFAULT_FALSE, "")
                            .split(","))));

        } catch (Exception e) {
            LOGGER.error("Error creating map for DC EMI Eligibility");
        }
    }

    /**
     * This method will be used for checking eligibilty of user for emi on DC
     *
     * @param userInfo
     *            : this field will be user related info it could be either be
     *            mobile number or card number
     * @param bank
     * @return
     */
    public boolean isUserEligibleforEmiOnDc(String userInfo, String bank) {
        try {
            if (dcEmiEligibility.containsKey(DC_EMI_ELIGIBILITY_DEFAULT_TRUE)
                    && dcEmiEligibility.get(DC_EMI_ELIGIBILITY_DEFAULT_TRUE).contains(bank)) {
                return true;
            } else if (dcEmiEligibility.containsKey(DC_EMI_ELIGIBILITY_CHECK_DB)
                    && dcEmiEligibility.get(DC_EMI_ELIGIBILITY_CHECK_DB).contains(bank)) {
                if ("HDFC".equalsIgnoreCase(bank)) {
                    userInfo = createHashForHdfc(userInfo);
                }
                EmiOnDcResponse emiOnDcResponse = emiDetails.getEmiOnDcEligibilityDetails(userInfo, bank);
                EXT_LOGGER.customInfo("Mapping response - EmiOnDcResponse :: {} UserInfo :: {} Bank : {} ",
                        emiOnDcResponse, userInfo, bank);
                if (emiOnDcResponse != null) {
                    return emiOnDcResponse.isEmiOnDcEnable();
                }
            } else if (dcEmiEligibility.containsKey(DC_EMI_ELIGIBILITY_CHECK_MOBILE_NUMBER)
                    && dcEmiEligibility.get(DC_EMI_ELIGIBILITY_CHECK_MOBILE_NUMBER).contains(bank) && userInfo != null) {
                return true;
            } else if (dcEmiEligibility.containsKey(DC_EMI_ELIGIBILITY_DEFAULT_FALSE)
                    && dcEmiEligibility.get(DC_EMI_ELIGIBILITY_DEFAULT_FALSE).contains(bank)) {
                return false;
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Error validating user for EMI on DC ", e);
        }
        return false;
    }

    private String createHashForHdfc(String phoneNumber) {
        String hash = StringUtils.EMPTY;
        try {
            String secret = env.getProperty("EMI_DC_HDFC_HASH_KEY");
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            hash = Hex.encodeHexString(hmacSHA256.doFinal(phoneNumber.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            LOGGER.error("Error in create hash of phone num: {}", e.getMessage());
        }
        return hash;
    }
}
