package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.payment.utils.LitePayViewEmiUtil;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.fund.constants.FundConstants;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("defaultLoggedInFlowLiteView")
public class DefaultFlowUserLoggedInLiteView implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultFlowUserLoggedIn.class);

    @Autowired
    @Qualifier("defaultUserLoggedInValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("litePayViewEmiUtil")
    private LitePayViewEmiUtil emiUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        LOGGER.info("Initiating {} workflow.", "DefaultFlowUserLoggedInLiteView");

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setDefaultLiteViewFlow(true);

        // fetch UserDetails
        boolean saveCardRequired = true;
        if (ERequestType.RESELLER.name().equals(flowRequestBean.getRequestType().name())) {
            saveCardRequired = false;
        }
        if (CollectionUtils.isNotEmpty(flowRequestBean.getDisabledPaymentModes())
                && flowRequestBean.getDisabledPaymentModes().contains(EPayMethod.SAVED_CARD.toString())) {
            saveCardRequired = false;
        }
        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), saveCardRequired);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        // Set user details
        workFlowTransBean.setUserDetails(userDetails.getResponse());

        // Create Order

        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

        // litePayViewConsult
        final GenericCoreResponseBean<LitePayviewConsultResponseBizBean> merchantLiteConsultPayView = workFlowHelper
                .litePayviewConsult(workFlowTransBean);

        if (!merchantLiteConsultPayView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<WorkFlowResponseBean>(merchantLiteConsultPayView.getFailureMessage(),
                    merchantLiteConsultPayView.getResponseConstant());
        }

        GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultPayView = getMerchantConsultPayViewBean(merchantLiteConsultPayView);

        workFlowTransBean.setMerchantViewConsult(merchantConsultPayView.getResponse());

        emiUtil.setEmiInfoForLiteConsultPayview(workFlowTransBean, flowRequestBean.getTxnAmount());

        // Fetch PayMode(AddAndPay or Hybrid)
        final EPayMode allowedPayMode = workFlowHelper.allowedPayMode(workFlowTransBean);

        if (allowedPayMode.equals(EPayMode.LIMIT_REJECT)) {
            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }
        workFlowTransBean.setAllowedPayMode(allowedPayMode);
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> addAndPayLiteConsultViewResponse;

        if (allowedPayMode.equals(EPayMode.ADDANDPAY)) {
            // Consult View for AddAndPay
            addAndPayLiteConsultViewResponse = workFlowHelper.litePayviewConsult(workFlowTransBean, true);
            if (!addAndPayLiteConsultViewResponse.isSuccessfullyProcessed()) {
                LOGGER.error("Consult For AddAndPay View failed due to ::{}",
                        addAndPayLiteConsultViewResponse.getFailureMessage());
            }
            GenericCoreResponseBean<ConsultPayViewResponseBizBean> addAndPayConsultViewResponse = getMerchantConsultPayViewBean(addAndPayLiteConsultViewResponse);
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

        if (workFlowHelper.checkUPIPUSHEnabled(workFlowTransBean)) {
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

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setSarvatraUserProfile(workFlowTransBean.getSarvatraUserProfile());
        responseBean.setSarvatraVpa(workFlowTransBean.getSarvatraVpa());
        if (responseBean.getSarvatraUserProfile() != null) {
            responseBean.setMerchantUpiPushEnabled(workFlowTransBean.isMerchantUpiPushEnabled());
            responseBean.setAddUpiPushEnabled(workFlowTransBean.isAddUpiPushEnabled());
        }
        responseBean.setAddMoneyDestination(workFlowTransBean.getAddMoneyDestination());
        responseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());
        responseBean.setOnTheFlyKYCRequired(workFlowTransBean.isOnTheFlyKYCRequired());
        // responseBean.setPaytmVpaInfo(workFlowTransBean.getPaytmVpaInfo());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "DefaultFlowUserLoggedInLiteView",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }

    private GenericCoreResponseBean<ConsultPayViewResponseBizBean> getMerchantConsultPayViewBean(
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult) {
        ConsultPayViewResponseBizBean merchantConsult = new ConsultPayViewResponseBizBean();
        merchantConsult.setPayMethodViews(litePayviewConsult.getResponse().getPayMethodViews());
        merchantConsult.setPaymentsBankSupported(litePayviewConsult.getResponse().isPaymentsBankSupported());
        merchantConsult.setExtendInfo(litePayviewConsult.getResponse().getExtendInfo());
        merchantConsult.setLoginMandatory(litePayviewConsult.getResponse().isLoginMandatory());
        merchantConsult.setWalletOnly(litePayviewConsult.getResponse().isWalletOnly());
        merchantConsult.setWalletFailed(litePayviewConsult.getResponse().isWalletFailed());
        return new GenericCoreResponseBean(merchantConsult);

    }
}
