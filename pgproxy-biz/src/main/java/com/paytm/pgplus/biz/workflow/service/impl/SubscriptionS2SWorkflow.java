/**
 *
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.SubscriptionRenewalResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.subscriptionClient.enums.SubscriptionRequestType;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_ACTIVATION_SUCCESS_CODE;
import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_SUCCESS_CODE;

/**
 * @author namanjain
 *
 */
@Service("subscriptionS2SWorkflow")
public class SubscriptionS2SWorkflow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionS2SWorkflow.class);

    @Autowired
    @Qualifier("subscriptionS2SCCOnlyNormalValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

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

        // fetch UserDetails
        final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                flowRequestBean.getToken(), true);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        workFlowTransBean.setUserDetails(userDetails.getResponse());

        if (!workFlowHelper.validateSavedCardForSubscription(workFlowTransBean, SubscriptionRequestType.CREATE)) {
            LOGGER.error("Subscription failed due to :: Invalid Saved Card ID");
            return new GenericCoreResponseBean<>("Invalid Saved Card ID");
        }

        /**
         * setting card id as null in PPI and PPBL case.
         */

        if (checkPPIorPPBL(flowRequestBean.getSubsPayMode())) {
            flowRequestBean.setSavedCardID(null);
        } else if (StringUtils.isEmpty(workFlowTransBean.getWorkFlowBean().getCardIndexNo())) {

            /**
             * Calling cache card token API to send CIN in activate call, if in
             * case CIN is not present in PG DB savedcard response.
             */

            try {
                String cardIndexNumber = workFlowHelper.getCardIndexNoFromCardNumber(workFlowTransBean
                        .getWorkFlowBean().getCardNo());
                if (null != cardIndexNumber) {
                    workFlowTransBean.getWorkFlowBean().setCardIndexNo(cardIndexNumber);
                } else {
                    LOGGER.error("Unable to fetch cardIndexNumber");
                    return new GenericCoreResponseBean<>("Unable to fetch cardIndexNumber");
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to fetch cardIndexNumber {}", ex);
                return new GenericCoreResponseBean<>("Unable to fetch cardIndexNumber");
            }
        }

        // Fresh Subscription Request
        final SubscriptionResponse freshSubscriptionResponse = workFlowHelper
                .processFreshSubscriptionContrat(workFlowTransBean);
        if (!freshSubscriptionResponse.getRespCode().equals(SUBSCRIPTION_SUCCESS_CODE)) {
            LOGGER.error("Subscription failed due to ::{}", freshSubscriptionResponse.getRespMsg());
            return new GenericCoreResponseBean<>(freshSubscriptionResponse.getRespMsg());
        }
        workFlowTransBean.setSubscriptionServiceResponse(freshSubscriptionResponse);

        /**
         * Setting savedCardId = null, as subscription service doesn't support
         * alphanumeric string in savedCardId. CIN will be passed in
         * subscription activate call in both savedcardId and CIN cases in
         * cardIndexNumber field.
         */
        String savedCardId = workFlowTransBean.getWorkFlowBean().getSavedCardID();
        if (StringUtils.isNotEmpty(savedCardId) && !savedCardId.chars().allMatch(Character::isDigit)) {
            workFlowTransBean.getWorkFlowBean().setSavedCardID(null);
        }
        // Activate Subscription Contract
        final SubscriptionResponse modifySubscriptionResponse = workFlowHelper
                .processActivateSubscription(workFlowTransBean);
        if (!modifySubscriptionResponse.getRespCode().equals(SUBSCRIPTION_ACTIVATION_SUCCESS_CODE)) {
            LOGGER.error("Subscription Activation failed due to ::{}", modifySubscriptionResponse.getRespMsg());
            return new GenericCoreResponseBean<>(modifySubscriptionResponse.getRespMsg());
        }

        // To provide cardHash and CardIndexNo to wallet for risk for CC/DC
        if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled()) {
            try {
                if (checkIfCCorDC(flowRequestBean.getSubsPayMode())) {
                    workFlowHelper.getAndSetCardHashAndCardIndexNo(flowRequestBean, userDetails.getResponse(), null);
                }
            } catch (Exception e) {
                LOGGER.error("Error in setting cardHash and cardIndexNo");
            }
        }

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        SubscriptionRenewalResponse response = new SubscriptionRenewalResponse();
        response.setMid(flowRequestBean.getPaytmMID());
        response.setOrderId(flowRequestBean.getOrderID());
        response.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(workFlowTransBean.getWorkFlowBean()
                .getTxnAmount()));
        response.setSubsId(workFlowTransBean.getSubscriptionServiceResponse().getSubscriptionId());
        response.setStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
        response.setRespCode("01");
        response.setRespMsg("Subscription registered successfully");
        response.setCardHash(flowRequestBean.getCardHash());
        response.setCardIndexNo(flowRequestBean.getCardIndexNo());
        // response.setTxnId(workFlowTransBean.getTransID());
        if (flowRequestBean.getExtendInfo() != null) {
            response.setMerchantUniqueRefernce(flowRequestBean.getExtendInfo().getMerchantUniqueReference());
        }
        responseBean.setSubscriptionRenewalResponse(response);

        LOGGER.info("Returning Response Bean From SubscriptionS2SCCOnlyNormal, trans Id : {} ", response.getTxnId());
        return new GenericCoreResponseBean<>(responseBean);
    }

    private boolean checkIfCCorDC(SubsPaymentMode subsPayMode) {
        switch (subsPayMode) {
        case DC:
        case CC:
            return true;
        default:
            return false;
        }
    }

    /**
     * @param subsPayMode
     * @return true if subsPaymode is PPI or PPBL, otherwise False.
     */
    private boolean checkPPIorPPBL(SubsPaymentMode subsPayMode) {
        switch (subsPayMode) {
        case PPI:
        case PPBL:
            return true;
        default:
            return false;
        }
    }

}
