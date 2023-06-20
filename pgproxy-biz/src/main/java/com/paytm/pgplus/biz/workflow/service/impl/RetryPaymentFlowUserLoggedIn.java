package com.paytm.pgplus.biz.workflow.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("retryPaymentFlowUserLoggedIn")
public class RetryPaymentFlowUserLoggedIn implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetryPaymentFlowUserLoggedIn.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // Setting TransId
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        // fetch UserDetails
        boolean saveCardRequired = true;
        if (ERequestType.RESELLER.name().equals(flowRequestBean.getRequestType().name())) {
            saveCardRequired = false;
        }

        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), saveCardRequired);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage());
        }
        // Set user details
        workFlowTransBean.setUserDetails(userDetails.getResponse());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultPayView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(merchantConsultPayView.getFailureMessage());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultPayView.getResponse());

        // Fetch PayMode(AddAndPay or Hybrid)
        final EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);

        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponse = null;
        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // Consult View for AddAndPay
            addAndPayConsultViewResponse = workFlowHelper.consultPayView(workFlowTransBean, true);
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponse.getResponse());
        }

        if (workFlowHelper.paymentsBankAllowed(workFlowTransBean)) {
            // Invoke BMW API
            GenericCoreResponseBean<AccountBalanceResponse> bankResponse = workFlowHelper
                    .fetchAccountBalance(workFlowTransBean);
            if (bankResponse.isSuccessfullyProcessed() && bankResponse.getResponse() != null) {
                workFlowTransBean.setAccountBalanceResponse(bankResponse.getResponse());
            }
        }
        boolean isSavedVpaCheckOverride = false;
        if (flowRequestBean.getPaymentRequestBean() != null) {
            isSavedVpaCheckOverride = workFlowHelper.isSavedVpaCheckOverride(flowRequestBean.getPaymentRequestBean()
                    .getAppVersion());
        }
        if (workFlowHelper.checkExpressEnabledInNative(workFlowTransBean)
                || flowRequestBean.isEnhancedCashierPageRequest() || isSavedVpaCheckOverride) {
            GenericCoreResponseBean<UserProfileSarvatra> sarvatraResponse = workFlowHelper
                    .fetchUserProfileFromSarvatra(workFlowTransBean);
            if (sarvatraResponse.isSuccessfullyProcessed() && null != sarvatraResponse.getResponse()) {
                workFlowTransBean.setSarvatraUserProfile(sarvatraResponse.getResponse());
                workFlowTransBean.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(sarvatraResponse.getResponse()
                        .getResponse()));
            }
        }

        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        // For invoice flow
        GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantIDResponse = null;
        if (flowRequestBean.getRequestType().getType().equals("EMAIL_INVOICE")
                || flowRequestBean.getRequestType().getType().equals("SMS_INVOICE")) {
            queryByMerchantIDResponse = workFlowHelper.queryByMerchantTransID(workFlowTransBean, true);
            if (!queryByMerchantIDResponse.isSuccessfullyProcessed()
                    || StringUtils.isBlank(queryByMerchantIDResponse.getResponse().getAcquirementId())) {

                LOGGER.error("Error occured while quering by Merchant TransID or TransID fetched is blank");
                return new GenericCoreResponseBean<>(queryByMerchantIDResponse.getFailureMessage());

            } else if (queryByMerchantIDResponse.getResponse().getStatusDetail() == null
                    || queryByMerchantIDResponse.getResponse().getStatusDetail().getAcquirementStatus() == null
                    || !queryByMerchantIDResponse.getResponse().getStatusDetail().getAcquirementStatus()
                            .equals(AcquirementStatusType.INIT)) {
                return new GenericCoreResponseBean<>("Invalid Acquirement Status on Query Merchant TransID",
                        queryByMerchantIDResponse.getResponseConstant());
            }
        }

        // End
        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setExtendedInfo(queryByMerchantIDResponse != null ? createExtendedInfo(queryByMerchantIDResponse
                .getResponse()) : null);
        responseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());
        responseBean.setSarvatraUserProfile(workFlowTransBean.getSarvatraUserProfile());
        responseBean.setSarvatraVpa(workFlowTransBean.getSarvatraVpa());
        if (responseBean.getSarvatraUserProfile() != null) {
            responseBean.setMerchantUpiPushEnabled(workFlowTransBean.isMerchantUpiPushEnabled());
            responseBean.setMerchantUpiPushExpressEnabled(workFlowTransBean.isMerchantUpiPushExpressEnabled());
            responseBean.setAddUpiPushEnabled(workFlowTransBean.isAddUpiPushEnabled());
            responseBean.setAddUpiPushExpressEnabled(workFlowTransBean.isAddUpiPushExpressEnabled());
        }
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "RetryPaymentFlowUserLoggedIn",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }

    private Map<String, String> createExtendedInfo(QueryByMerchantTransIDResponseBizBean queryByMerchantIDResponse) {
        Map<String, String> extendedInfoMap = new HashMap<>();
        extendedInfoMap.putAll(queryByMerchantIDResponse.getExtendInfo());
        return extendedInfoMap;
    }
}
