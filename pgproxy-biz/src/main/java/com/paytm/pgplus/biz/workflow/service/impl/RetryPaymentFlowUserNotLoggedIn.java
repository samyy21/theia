package com.paytm.pgplus.biz.workflow.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

@Service("retryPaymentFlowUserNotLoggedIn")
public class RetryPaymentFlowUserNotLoggedIn implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetryPaymentFlowUserNotLoggedIn.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // Setting TransId
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultPayView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(merchantConsultPayView.getFailureMessage(),
                    merchantConsultPayView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultPayView.getResponse());

        // Setting PayMode
        workFlowTransBean.setAllowedPayMode(EPayMode.NONE);

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

        final GenericCoreResponseBean<MidCustIdCardBizDetails> midCustIdCardDetails = workFlowHelper
                .fetchSavedCards(workFlowTransBean);

        if (midCustIdCardDetails.isSuccessfullyProcessed()) {
            workFlowTransBean.setMidCustIdCardBizDetails(midCustIdCardDetails.getResponse());
        }

        workFlowHelper.filterSavedCardsUserNotLogged(workFlowTransBean);

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setExtendedInfo(queryByMerchantIDResponse != null ? createExtendedInfo(queryByMerchantIDResponse
                .getResponse()) : null);
        responseBean.setmIdCustIdCardBizDetails(workFlowTransBean.getMidCustIdCardBizDetails());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "RetryPaymentFlowUserNotLoggedIn",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }

    private Map<String, String> createExtendedInfo(QueryByMerchantTransIDResponseBizBean queryByMerchantIDResponse) {
        Map<String, String> extendedInfoMap = new HashMap<>();
        extendedInfoMap.putAll(queryByMerchantIDResponse.getExtendInfo());
        return extendedInfoMap;
    }
}
