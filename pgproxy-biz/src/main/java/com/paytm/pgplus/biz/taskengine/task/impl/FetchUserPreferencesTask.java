/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.biz.taskengine.task.impl;

import static com.paytm.pgplus.biz.utils.BizConstant.FETCH_USER_PREFERENCES_TIME;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_FETCH_USER_PREFERENCE_TASK;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceValue.ACTIVE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.UPS_JWT_CLIENT_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.UPS_JWT_SECRET_KEY;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceName.USER_POSTPAID_ACCOUNT_STATUS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceName.USER_PAYMENT_PREFERENCE_ORDER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceValue.DIGITAL_CREDIT;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.ResponseConstants.SUCCESS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.user.models.UserPreference;
import com.paytm.pgplus.facade.user.models.response.GetUserPreferenceResponse;
import com.paytm.pgplus.facade.user.services.IUserPreferenceService;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("fetchUserPreferencesTask")
public class FetchUserPreferencesTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUserPreferencesTask.class);

    @Autowired
    private IUserPreferenceService userPreferenceService;

    @Autowired
    private Environment env;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        List<String> preferencesList = new ArrayList<>();
        UserDetailsBiz userDetails = workFlowTransactionBean.getUserDetails();

        if (ObjectUtils.notEqual(workFlowResponseBean.getMerchnatLiteViewResponse(), null)) {
            List<PayMethodViewsBiz> payMethodViews = workFlowResponseBean.getMerchnatLiteViewResponse()
                    .getPayMethodViews();
            if (CollectionUtils.isNotEmpty(payMethodViews)) {
                for (PayMethodViewsBiz payMethodViewsBiz : payMethodViews) {
                    if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodViewsBiz.getPayMethod())
                            && userDetails.isPaytmCCEnabled()) {
                        preferencesList.add(USER_PAYMENT_PREFERENCE_ORDER);
                    } else if (EPayMethod.BALANCE.getMethod().equals(payMethodViewsBiz.getPayMethod())
                            && workFlowRequestBean.isSubscription()) {
                        preferencesList.add(USER_POSTPAID_ACCOUNT_STATUS);
                        preferencesList.add(USER_PAYMENT_PREFERENCE_ORDER);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(preferencesList)) {
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }

        preferencesList = updateListwithUniqueValues(preferencesList);

        GetUserPreferenceResponse userPreferenceResponse;
        try {
            userPreferenceResponse = userPreferenceService.getUserPreference(workFlowTransactionBean.getUserDetails()
                    .getUserId(), preferencesList, ConfigurationUtil.getTheiaProperty(UPS_JWT_CLIENT_ID), env
                    .getProperty(UPS_JWT_SECRET_KEY));
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while fetching User preferences from UPS {}", ex);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }

        if (isValidGetUserPrefResponse(userPreferenceResponse)) {
            List<UserPreference> userPreferences = userPreferenceResponse.getResponse().getPreferences();
            Map<String, UserPreference> userPreferencesMap = userPreferences.stream().collect(
                    Collectors.toMap(preference -> preference.getKey(), preference -> preference));

            if (workFlowRequestBean.isSubscription() && isUserPostpaidAccActive(userPreferencesMap)
                    && !isPaymentPreferenceOrderAlreadySet(userPreferencesMap)) {
                userDetails.setCapturePostpaidConsentForWalletTopUp(true);
                workFlowTransactionBean.setUserDetails(userDetails);
                return new GenericCoreResponseBean<>(Boolean.TRUE);
            } else if (workFlowRequestBean.isSubscription()) {
                return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
            }

            if (!isPaymentPreferenceOrderAlreadySet(userPreferencesMap)) {
                userDetails.setConsentForAutoDebitPref(true);
                userDetails.setShowConsentSheetAutoDebit(StringUtils.equals(
                        ConfigurationUtil.getTheiaProperty("show.additionalPopUp.forAutoDebitPreference"), "true"));
            }

            workFlowTransactionBean.setUserDetails(userDetails);
            return new GenericCoreResponseBean<>(Boolean.TRUE);
        }
        return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
    }

    private boolean isUserPostpaidAccActive(Map<String, UserPreference> userPreferencesMap) {
        String accStatus = userPreferencesMap.get(USER_POSTPAID_ACCOUNT_STATUS).getValue();
        if (StringUtils.isNotBlank(accStatus)) {
            return StringUtils.equals(accStatus, ACTIVE);
        }
        return false;
    }

    private List<String> updateListwithUniqueValues(List<String> preferencesList) {
        return preferencesList.stream().distinct().collect(Collectors.toList());
    }

    private boolean isPaymentPreferenceOrderAlreadySet(Map<String, UserPreference> userPreferencesMap) {
        String paymentPreferenceVal = null;
        if (userPreferencesMap.containsKey(USER_PAYMENT_PREFERENCE_ORDER)) {
            paymentPreferenceVal = userPreferencesMap.get(USER_PAYMENT_PREFERENCE_ORDER).getValue();
        }
        if (StringUtils.isNotBlank(paymentPreferenceVal)) {
            List<String> paymentPreferenceList = Arrays.asList(StringUtils.split(paymentPreferenceVal, ","));
            return paymentPreferenceList.contains(DIGITAL_CREDIT);
        }
        return false;
    }

    private boolean isValidGetUserPrefResponse(GetUserPreferenceResponse userPreferenceResponse) {
        return userPreferenceResponse != null && userPreferenceResponse.getStatusInfo() != null
                && userPreferenceResponse.getStatusInfo().getStatus() != null
                && userPreferenceResponse.getStatusInfo().getStatus().equals(SUCCESS)
                && userPreferenceResponse.getResponse() != null
                && CollectionUtils.isNotEmpty(userPreferenceResponse.getResponse().getPreferences());
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_USER_PREFERENCES_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {

        if (transBean.getUserDetails() != null && transBean.getUserDetails().getUserId() != null) {
            if (input.isSubscription()) {
                return ff4jUtils.isFeatureEnabledOnMid(input.getPaytmMID(),
                        ENABLE_FETCH_USER_PREFERENCE_TASK_IN_SUBSCRIPTION_PAYFLOW, false);
            } else {
                return ff4jUtils.isFeatureEnabledOnMid(input.getPaytmMID(),
                        ENABLE_FETCH_USER_PREFERENCE_TASK_IN_ONETIME_PAYFLOW, false);
            }
        }
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(FETCH_USER_PREFERENCES_TIME, "2000"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
    }
}