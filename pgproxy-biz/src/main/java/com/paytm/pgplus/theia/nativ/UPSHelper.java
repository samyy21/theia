/**
 * Alipay.com Inc. * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ;

import com.google.common.base.Splitter;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserPreference;
import com.paytm.pgplus.facade.user.models.response.GetUserPreferenceResponse;
import com.paytm.pgplus.facade.user.services.IUserPreferenceService;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GET_USER_POSTPAID_STATUS_FROM_UPS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceName.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceValue.ACTIVE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.ResponseConstants.SUCCESS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UserPayModeStatusConstants.USER_PAYMODE_INACTIVE_STATUS;

@Service("upsHelper")
public class UPSHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPSHelper.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(UPSHelper.class);

    @Autowired
    private IUserPreferenceService userPreferenceService;

    @Autowired
    private Environment env;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public void updateUserPostpaidAccStatusFromUPS(UserDetails userDetails) {
        List<UserPreference> userPreferences = getUserPostpaidDetailsFromUPS(userDetails.getUserId());
        if (userPreferences == null)
            return;

        String oneClickActivateStatus = null;
        Optional<UserPreference> userEligibileForPostpaidOnboarding = userPreferences.stream()
                .filter(preference -> USER_POSTPAID_ONE_CLICK_ACTIVATE.equals(preference.getKey())).findFirst();
        if (userEligibileForPostpaidOnboarding.isPresent()) {
            if (StringUtils.isNotBlank(userEligibileForPostpaidOnboarding.get().getValue())) {
                oneClickActivateStatus = userEligibileForPostpaidOnboarding.get().getValue();
            } else {
                LOGGER.error("User postpaid onboarding preference received as blank from UPS for userId: {}",
                        userDetails.getUserId());
            }
        }

        String postpaidCreditLimit = null;
        Optional<UserPreference> userPostpaidCreditLimit = userPreferences.stream()
                .filter(preference -> USER_POSTPAID_APPROVED_LIMIT.equals(preference.getKey())).findFirst();
        if (userPostpaidCreditLimit.isPresent()) {
            if (StringUtils.isNotBlank(userPostpaidCreditLimit.get().getValue())) {
                postpaidCreditLimit = userPostpaidCreditLimit.get().getValue();
            } else {
                LOGGER.error("User postpaid credit limit received as blank from UPS for userId: {}",
                        userDetails.getUserId());
            }
        }

        Boolean isTxnBeforeExpiry = false;
        Optional<UserPreference> userOneClickActivateExpiry = userPreferences.stream()
                .filter(preference -> USER_ONE_CLICK_ACTIVATE_EXPIRY.equals(preference.getKey())).findFirst();
        if (userOneClickActivateExpiry.isPresent()) {
            isTxnBeforeExpiry = validateOnceClickExpiry(userOneClickActivateExpiry.get().getValue());
        }

        if (StringUtils.isNotBlank(oneClickActivateStatus)
                && !USER_PAYMODE_INACTIVE_STATUS.equalsIgnoreCase(oneClickActivateStatus) && isTxnBeforeExpiry) {
            userDetails.setUserEligibileForPostPaidOnboarding(true);
            userDetails.setPostpaidCreditLimit(postpaidCreditLimit);
            userDetails.setPostpaidOnboardingStageMsg(getPostPaidOnboardingStageMsg(oneClickActivateStatus,
                    postpaidCreditLimit));
        }

        // Setting User Postpaid Status
        Optional<UserPreference> userPostpaidStatus = userPreferences.stream()
                .filter(preference -> USER_POSTPAID_ACCOUNT_STATUS.equals(preference.getKey())).findFirst();
        if (userPostpaidStatus.isPresent()) {
            String postpaidStatus = userPostpaidStatus.get().getValue();
            if (StringUtils.isNotBlank(postpaidStatus)) {
                userDetails.setPaytmCCEnabled(StringUtils.equals(postpaidStatus, ACTIVE));
            } else {
                LOGGER.error("User postpaid status received as blank from UPS for userId: {}", userDetails.getUserId());
            }
        }
    }

    private String getPostPaidOnboardingStageMsg(String oneClickActivateStatus, String postpaidCreditLimit) {
        String onbordingActivtedFlowMsg = ConfigurationUtil.getTheiaProperty(ONBORDING_ACTIVETED_FLOW_VALUE);
        Map<String, String> onbordingActivtedFlowMap = new HashMap<>();
        if (StringUtils.isNotEmpty(onbordingActivtedFlowMsg)) {
            onbordingActivtedFlowMap = splitToMap(onbordingActivtedFlowMsg);
        }
        String message = onbordingActivtedFlowMap.get(StringUtils.lowerCase(oneClickActivateStatus));
        if (StringUtils.isEmpty(message)) {
            message = onbordingActivtedFlowMap.get(DEFAULT);
        }
        message = updateCreditLimitInMessage(oneClickActivateStatus, postpaidCreditLimit, message);
        return message;
    }

    private String updateCreditLimitInMessage(String oneClickActivateStatus, String postpaidCreditLimit, String message) {
        if (StringUtils.isNotEmpty(message)) {
            if (message.contains(CREDIT_LIMIT)) {
                if (StringUtils.isNotBlank(postpaidCreditLimit)) {
                    message = message.replace(CREDIT_LIMIT, postpaidCreditLimit);
                } else if (TWO_CLICK_FLOW.equalsIgnoreCase(StringUtils.lowerCase(oneClickActivateStatus))
                        || DEFAULT.equalsIgnoreCase(StringUtils.lowerCase(oneClickActivateStatus))) {
                    message = message.replace(CREDIT_LIMIT, TWO_CLICK_DEFAULT_CREDIT_LIMIT);
                }
            }
        }
        return message;
    }

    private Map<String, String> splitToMap(String messages) {
        return Splitter.on("|").withKeyValueSeparator(":").split(messages);
    }

    public List<UserPreference> getUserPostpaidDetailsFromUPS(String userId) {
        if (StringUtils.isBlank(userId)) {
            LOGGER.error("userId cannot be blank");
            return null;
        }

        List<String> preferencesList = new ArrayList<>();
        preferencesList.add(USER_POSTPAID_ACCOUNT_STATUS);
        preferencesList.add(USER_POSTPAID_ONE_CLICK_ACTIVATE);
        preferencesList.add(USER_POSTPAID_APPROVED_LIMIT);
        preferencesList.add(USER_ONE_CLICK_ACTIVATE_EXPIRY);
        GetUserPreferenceResponse userPreferenceResponse;
        try {
            userPreferenceResponse = userPreferenceService.getUserPreference(userId, preferencesList,
                    ConfigurationUtil.getTheiaProperty(UPS_JWT_CLIENT_ID), env.getProperty(UPS_JWT_SECRET_KEY));
            if (isValidGetUserPrefResponse(userPreferenceResponse)) {
                return userPreferenceResponse.getResponse().getPreferences();
            } else {
                LOGGER.info("Recieved invalid Postpaid user preference response from UPS: {}", userPreferenceResponse);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while fetching User Postpaid status from UPS {}", ex);
        }
        return null;
    }

    private boolean isValidGetUserPrefResponse(GetUserPreferenceResponse userPreferenceResponse) {
        return userPreferenceResponse != null && userPreferenceResponse.getStatusInfo() != null
                && SUCCESS.equals(userPreferenceResponse.getStatusInfo().getStatus())
                && userPreferenceResponse.getResponse() != null
                && CollectionUtils.isNotEmpty(userPreferenceResponse.getResponse().getPreferences());
    }

    public void updateUserPostpaidAccStatusFromUPS(String mid, UserDetails userDetails) {
        if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GET_USER_POSTPAID_STATUS_FROM_UPS, false)) {
            EXT_LOGGER.customInfo("Fetching user postpaid details from UPS");
            updateUserPostpaidAccStatusFromUPS(userDetails);
        }
    }

    public String getUserPostpaidAccStatusFromUPS(String userId) {
        List<UserPreference> userPreferences = getUserPostpaidDetailsFromUPS(userId);
        if (userPreferences != null) {
            Optional<UserPreference> userPostpaidStatus = userPreferences.stream()
                    .filter(preference -> USER_POSTPAID_ACCOUNT_STATUS.equals(preference.getKey())).findFirst();
            return userPostpaidStatus.map(UserPreference::getValue).orElse(null);
        }
        return null;
    }

    private Boolean validateOnceClickExpiry(String oneClickExpiry) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate oneClickExpiryDate = LocalDate.parse(oneClickExpiry, formatter);
            LocalDate currentDate = LocalDate.now();
            if (currentDate.isBefore(oneClickExpiryDate))
                return true;
        } catch (Exception ex) {
            LOGGER.error("Unable to validate oneClickActivateExpiry : ", ex);
            return false;
        }
        return false;
    }
}
