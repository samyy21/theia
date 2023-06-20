package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.FailureLogs.ALLOWED_ISSUERS_ON_MERCHANT_ERROR_MESSAGE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ALLOWED_ISSUERS_AT_MID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.CREDIT_CARD;

@Service("cardTransactionUtil")
public class CardTransactionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardTransactionUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(CardTransactionUtil.class);

    @Autowired
    private Ff4jUtils ff4jUtils;

    /**
     * Get Map of allowed issuers on MID configured on FF4J property.
     *
     * @return - Map
     */
    public Map<String, Set<String>> getAllowedIssuersConfigMapOnMid() {
        Map<String, Set<String>> allowedIssuersMap = new HashMap<>();
        try {
            String allowedIssuersConfig = ff4jUtils.getPropertyAsStringWithDefault(ALLOWED_ISSUERS_AT_MID,
                    StringUtils.EMPTY);
            if (StringUtils.isNotBlank(allowedIssuersConfig)) {
                List<String> allowedIssuers = Arrays.asList(allowedIssuersConfig.split(","));
                if (!CollectionUtils.isEmpty(allowedIssuers)) {
                    for (String allowedIssuerConfig : allowedIssuers) {
                        String[] issuerArray = allowedIssuerConfig.split("\\.", 2);
                        if (issuerArray.length == 2 && StringUtils.isNotBlank(issuerArray[0])
                                && StringUtils.isNotBlank(issuerArray[1])) {
                            if (allowedIssuersMap.get(issuerArray[0]) == null) {
                                allowedIssuersMap.put(issuerArray[0], new HashSet<String>() {
                                    {
                                        add(allowedIssuerConfig);
                                    }
                                });
                            } else {
                                allowedIssuersMap.get(issuerArray[0]).add(allowedIssuerConfig);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while parsing allowed issuer config", e);
        }
        return allowedIssuersMap;
    }

    /**
     * Check if issuerConfig corresponding to this MID is configured for allowed
     * issuers.
     *
     * @param mid
     *            - Paytm Merchant ID.
     * @param issuerConfig
     *            - In the format "MID.CardType.CardScheme.IssuingBank" ex :
     *            "MID1.CREDIT_CARD.VISA.HDFC"
     * @return
     */
    public boolean isIssuerConfigAllowedOnMid(ERequestType requestType, String mid, String issuerConfig,
            boolean isAddNPayOrAddMoneyFlow) {
        if (BooleanUtils.isFalse(ERequestType.isSubscriptionOrMFRequest(requestType))
                && BooleanUtils.isFalse(isAddNPayOrAddMoneyFlow)) {
            EXT_LOGGER
                    .customInfo("Verifying if issuer configuration is allowed on MID for requestType {}", requestType);
            Map<String, Set<String>> allowedIssuersConfigMapOnMid = getAllowedIssuersConfigMapOnMid();
            return allowedIssuersConfigMapOnMid.get(mid) == null
                    || allowedIssuersConfigMapOnMid.get(mid).contains(issuerConfig);
        }
        return true;
    }

    /**
     * To get custom error message based on allowed issuer configuration on the
     * FF4J property for the given MID.
     *
     * @param mid
     *            Paytm Merchant ID.
     * @return - Custom error message.
     */
    public String getCustomErrMsgForIssuerNotAllowed(String mid) {
        try {
            Set<String> customMsg = new TreeSet<>();
            Map<String, Set<String>> issuingBankCardTypeMap = new HashMap<>();
            Set<String> issuerConfigOnMid = getAllowedIssuersConfigMapOnMid().get(mid);
            if (!org.springframework.util.CollectionUtils.isEmpty(issuerConfigOnMid)) {
                for (String allowedIssuerConfig : issuerConfigOnMid) {
                    String[] issuerConfigArr = allowedIssuerConfig.split("\\.");
                    if (issuerConfigArr.length == 4) {
                        if (issuingBankCardTypeMap.get(issuerConfigArr[3]) == null) {
                            issuingBankCardTypeMap.put(issuerConfigArr[3], new TreeSet<String>() {
                                {
                                    add(CREDIT_CARD.equals(issuerConfigArr[1]) ? EPayMethod.CREDIT_CARD
                                            .getDisplayName() : EPayMethod.DEBIT_CARD.getDisplayName());
                                }
                            });
                        } else {
                            issuingBankCardTypeMap.get(issuerConfigArr[3]).add(
                                    CREDIT_CARD.equals(issuerConfigArr[1]) ? EPayMethod.CREDIT_CARD.getDisplayName()
                                            : EPayMethod.DEBIT_CARD.getDisplayName());
                        }
                    }
                }
                issuingBankCardTypeMap.forEach((issuingBank, cardType) -> customMsg.add(issuingBank + " "
                        + StringUtils.join(cardType.toArray(), " & ")));
                return MessageFormat.format(ALLOWED_ISSUERS_ON_MERCHANT_ERROR_MESSAGE,
                        StringUtils.join(customMsg.toArray(), ", "));
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while building error message for the allowed issuers", e);
        }
        return null;
    }
}
