package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author manojpal
 *
 */
@Service("retryBuyerPaysChargeUserLoggedInFlow")
public class RetryBuyerPaysChargeUserLoggedInFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetryBuyerPaysChargeUserLoggedInFlow.class);

    @Autowired
    @Qualifier("postConvinienceFeeValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setPostConvenienceFeeModel(true);
        // Setting TransId
        workFlowTransBean.setTransID(flowRequestBean.getTransID());

        // fetch UserDetails
        final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), true);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        workFlowTransBean.setUserDetails(userDetails.getResponse());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(merchantConsultView.getFailureMessage(),
                    merchantConsultView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultView.getResponse());
        workFlowTransBean.setTransCreatedTime(merchantConsultView.getResponse().getTransCreatedTime());

        // ConsultAdd Check Allowed PayModes
        final EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);
        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // ConsultPayView for AddAndPay
            final GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponseBean = workFlowHelper
                    .consultPayView(workFlowTransBean, true);
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponseBean.getResponse());
            workFlowTransBean.setTransCreatedTime(addAndPayConsultViewResponseBean.getResponse().getTransCreatedTime());
        }

        final GenericCoreResponseBean<ConsultFeeResponse> consultFee = workFlowHelper
                .consultBulkFeeResponse(workFlowTransBean);
        if (!consultFee.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultFee.getFailureMessage(), consultFee.getResponseConstant());
        }

        workFlowHelper.filterSavedCards(workFlowTransBean);

        workFlowTransBean.setConsultFeeResponse(consultFee.getResponse());

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransCreatedTime(workFlowTransBean.getTransCreatedTime());
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setConsultFeeResponse(workFlowTransBean.getConsultFeeResponse());
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "RetryBuyerPaysChargeUserLoggedInFlow",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}
