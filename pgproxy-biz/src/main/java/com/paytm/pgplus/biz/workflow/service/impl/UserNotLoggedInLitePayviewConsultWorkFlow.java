package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;

import java.util.List;

@Service("userNotLoggedInLitePayviewConsultWorkFlow")
public class UserNotLoggedInLitePayviewConsultWorkFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserNotLoggedInLitePayviewConsultWorkFlow.class);

    @Autowired
    @Qualifier("userNotLoggedInLitePayviewConsultValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginService;

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCards;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Initiating {} workflow.", "UserNotLoggedInLitePayviewConsultWorkFlow");

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()
                && requestBeanValidationResult.getResponseConstant() != ResponseConstants.INVALID_ORDER_ID) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setPostConvenienceFeeModel(flowRequestBean.isPostConvenience());
        // fetch saved Card for Mid and cust id

        if (flowRequestBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(flowRequestBean.getCustID())) {
            GenericCoreResponseBean<List<CardBeanBiz>> savedCardsList = savedCards.fetchSavedCardsByMidCustIdUserId(
                    flowRequestBean.getPaytmMID(), flowRequestBean.getCustID(), null);
            if (savedCardsList.isSuccessfullyProcessed()
                    && BizParamValidator.validateInputListParam(savedCardsList.getResponse())) {
                MidCustIdCardBizDetails midCustIdCardBizDetails = new MidCustIdCardBizDetails();
                midCustIdCardBizDetails.setmId(flowRequestBean.getPaytmMID());
                midCustIdCardBizDetails.setCustId(flowRequestBean.getCustID());
                midCustIdCardBizDetails.setMerchantCustomerCardList(savedCardsList.getResponse());
                workFlowTransBean.setMidCustIdCardBizDetails(midCustIdCardBizDetails);
            }
        }

        // Consult PayView
        final GenericCoreResponseBean<LitePayviewConsultResponseBizBean> merchantConsultPayView = workFlowHelper
                .litePayviewConsult(workFlowTransBean);

        if (!merchantConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(merchantConsultPayView.getFailureMessage(),
                    merchantConsultPayView.getResponseConstant());
        }

        workFlowTransBean.setMerchantLiteViewConsult(merchantConsultPayView.getResponse());

        // filter saved cards according to pay methods of merchant
        if (workFlowTransBean.getMidCustIdCardBizDetails() != null
                && CollectionUtils.isNotEmpty(workFlowTransBean.getMidCustIdCardBizDetails()
                        .getMerchantCustomerCardList())) {
            workFlowHelper.filterSavedCardsUserNotLogged(workFlowTransBean.getMidCustIdCardBizDetails(),
                    workFlowTransBean.getMerchantLiteViewConsult().getPayMethodViews(), workFlowTransBean);
        }

        // Specific for subscription
        if (flowRequestBean.isSubscription()) {
            // Filter payMethods for subscription Specific
            workFlowHelper.filterOperationsForUserNotLoggedIn(workFlowTransBean, true);
            // Filtering saved cards specifically for those where subscription
            // is
            // allowed
            workFlowHelper.filterSavedCardsForSubscriptionNotLoggedIn(workFlowTransBean);
        }

        // Fetch active subscriptions to use as payment mode
        if (ERequestType.NATIVE_MF_PAY.equals(workFlowTransBean.getWorkFlowBean().getRequestType())) {
            workFlowHelper.processToFetchActiveSubscriptions(workFlowTransBean);
        }

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setMerchnatLiteViewResponse(workFlowTransBean.getMerchantLiteViewConsult());
        responseBean.setmIdCustIdCardBizDetails(workFlowTransBean.getMidCustIdCardBizDetails());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setActiveSubscriptions(workFlowTransBean.getActiveSubscriptions());
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "UserNotLoggedInLitePayviewConsultWorkFlow",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}
