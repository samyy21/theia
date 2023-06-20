/**
 * This flow , validate authCode , fetch userdetails , consultpayView , if add
 * and paya allowed, fetch view for add and pay
 * 
 * For reference see flow number 15
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginRequestBizBean;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("buyerPaysChargePostLoginAtCashierFlow")
public class BuyerPaysChargePostLoginAtCashierFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(BuyerPaysChargePostLoginAtCashierFlow.class);

    @Autowired
    private WorkFlowRequestCreationHelper workFlowCommonHelper;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginService;

    @Autowired
    @Qualifier("normalPostLoginValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setTransID(flowRequestBean.getTransID());
        workFlowTransBean.setPostConvenienceFeeModel(true);

        // Verify Login
        final VerifyLoginRequestBizBean verifyLoginRequestBean = workFlowCommonHelper
                .createVerifyLoginRequestBean(workFlowTransBean);
        final GenericCoreResponseBean<VerifyLoginResponseBizBean> verifyLoginResponse = loginService
                .verfifyLogin(verifyLoginRequestBean);
        LOGGER.debug("Verify Login Response :: {}", verifyLoginResponse);
        if (!verifyLoginResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Verification of user failed due to : {}", verifyLoginResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(verifyLoginResponse.getFailureMessage(),
                    verifyLoginResponse.getResponseConstant());
        }
        // TODO Set Payer UserID After Mapping
        workFlowTransBean.setUserDetails(verifyLoginResponse.getResponse().getUserDetails());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(merchantConsultView.getFailureMessage(),
                    merchantConsultView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultView.getResponse());
        workFlowTransBean.setTransCreatedTime(merchantConsultView.getResponse().getTransCreatedTime());

        // Consult Fees for merchant
        final GenericCoreResponseBean<ConsultFeeResponse> consultFee = workFlowHelper
                .consultBulkFeeResponse(workFlowTransBean);
        if (!consultFee.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultFee.getFailureMessage(), consultFee.getResponseConstant());
        }
        workFlowTransBean.setConsultFeeResponse(consultFee.getResponse());

        // ConsultAdd Check Allowed PayModes
        final EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);
        workFlowTransBean.setAllowedPayMode(allowedPayMode);

        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            final GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponseBean = workFlowHelper
                    .consultPayView(workFlowTransBean, true);
            LOGGER.debug("AddAndPay Consult PayView is : {}", addAndPayConsultViewResponseBean);
            workFlowTransBean.setAddAndPayViewConsult(addAndPayConsultViewResponseBean.getResponse());
            workFlowTransBean.setTransCreatedTime(addAndPayConsultViewResponseBean.getResponse().getTransCreatedTime());
        }

        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        final WorkFlowResponseBean flowResponseBean = new WorkFlowResponseBean();
        flowResponseBean.setTransCreatedTime(workFlowTransBean.getTransCreatedTime());
        flowResponseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        flowResponseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        flowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        flowResponseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        flowResponseBean.setTransID(workFlowTransBean.getTransID());
        flowResponseBean.setConsultFeeResponse(workFlowTransBean.getConsultFeeResponse());
        flowResponseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "BuyerPaysChargePostLoginAtCashierFlow",
                flowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(flowResponseBean);
    }
}
