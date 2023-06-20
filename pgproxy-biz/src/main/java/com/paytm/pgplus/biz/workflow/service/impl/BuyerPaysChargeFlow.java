/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import java.util.List;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("buyerPaysChargeFlow")
public class BuyerPaysChargeFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(BuyerPaysChargeFlow.class);

    @Autowired
    @Qualifier("postConvinienceFeeValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCardsService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        GenericCoreResponseBean<List<CardBeanBiz>> savedCards = null;
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setPostConvenienceFeeModel(true);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());

        // Create Order
        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

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
            return new GenericCoreResponseBean<>(consultFee.getFailureMessage());
        }
        workFlowTransBean.setConsultFeeResponse(consultFee.getResponse());

        // Fetch saved cards on custid and mid
        final GenericCoreResponseBean<MidCustIdCardBizDetails> mIdCustIdCardDetails = workFlowHelper
                .fetchSavedCards(workFlowTransBean);
        if (mIdCustIdCardDetails.isSuccessfullyProcessed()) {
            workFlowTransBean.setMidCustIdCardBizDetails(mIdCustIdCardDetails.getResponse());
        }

        workFlowHelper.filterSavedCardsUserNotLogged(workFlowTransBean);

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransCreatedTime(workFlowTransBean.getTransCreatedTime());
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setConsultFeeResponse(workFlowTransBean.getConsultFeeResponse());
        responseBean.setmIdCustIdCardBizDetails(workFlowTransBean.getMidCustIdCardBizDetails());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "BuyerPaysChargeFlow", responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}
