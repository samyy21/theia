package com.paytm.pgplus.biz.workflow.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;

/**
 * @author vivek
 *
 */
@Service("topupExpressFlow")
public class TopupExpressFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(TopupExpressFlow.class);

    @Autowired
    @Qualifier("addMoneyValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        // Request Bean Validation
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed() || StringUtils.isEmpty(flowRequestBean.getToken())) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());

        Map<String, String> extendInfo = workFlowHelper.getExtendInfoFromExtendBean(flowRequestBean);

        // Fetch User Details
        if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                LOGGER.info("User details could not be fetched because ::{}", userDetails.getFailureMessage());
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            extendInfo.put(BizConstant.ExtendedInfoKeys.PAYTM_USER_ID, userDetails.getResponse().getUserId());
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

        // cache card
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
        if (createCacheCardResponse != null) {
            return createCacheCardResponse;
        }

        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean storeCard = ((flowRequestBean.getStoreCard() != null) && "1".equals(flowRequestBean
                .getStoreCard().trim())) ? true : false;

        /*
         * Check for wallet limits Return a generic response code for all limit
         * failure scenarios
         */
        final GenericCoreResponseBean<BizWalletConsultResponse> consultAddMoney = workFlowHelper
                .consultAddMoneyV2(workFlowTransBean);
        if (!consultAddMoney.isSuccessfullyProcessed()
                || "FAILURE".equals(consultAddMoney.getResponse().getStatusCode())
                || ("SUCCESS".equals(consultAddMoney.getResponse().getStatusCode()) && consultAddMoney.getResponse()
                        .isLimitApplicable())) {

            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }
        workFlowTransBean.setAddMoneyDestination(consultAddMoney.getResponse().getAddMoneyDestination());
        workFlowTransBean.setTrustFactor(consultAddMoney.getResponse().getTrustFactor());

        // Create Topup
        final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUpResponse = workFlowHelper.createTopUp(
                workFlowTransBean, false);
        if (!createTopUpResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Topup API call failed due to ::{}", createTopUpResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(createTopUpResponse.getFailureMessage(),
                    createTopUpResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());
        LOGGER.info("Topup Created with response ::{}", createTopUpResponse);

        workFlowTransBean.setExtendInfo(extendInfo);
        LOGGER.info("Pay called from TopupExpressFlow");
        // Pay
        final GenericCoreResponseBean<BizPayResponse> bizPayResponse = workFlowHelper.pay(workFlowTransBean);
        if (!bizPayResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Pay API call failed due to ::{}", bizPayResponse.getFailureMessage());

            if (StringUtils.isNotBlank(bizPayResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                        bizPayResponse.getFailureMessage(), bizPayResponse.getResponseConstant(),
                        bizPayResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(bizPayResponse.getInternalErrorCode());
                return responseBean;
            } else {
                return new GenericCoreResponseBean<>(bizPayResponse.getFailureMessage(),
                        bizPayResponse.getResponseConstant());
            }
        }
        workFlowTransBean.setCashierRequestId(bizPayResponse.getResponse().getCashierRequestID());
        if (bizPayResponse.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(bizPayResponse.getResponse().getSecurityPolicyResult().getRiskResult());
        }
        LOGGER.info("Pay API called successfully with response ::{}", bizPayResponse);

        // Caching card details in Redis
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)) {
            if (!isSavedCardTxn && storeCard && userDetails != null) {
                /*
                 * Encrypt card data & then caching card details in Redis & in
                 * UserDetailsBiz also
                 */
                userDetails = savedCardsService.cacheCardDetails(flowRequestBean, userDetails.getResponse(),
                        workFlowTransBean.getTransID());
                if (!userDetails.isSuccessfullyProcessed()) {
                    LOGGER.error("Exception occured while saving card details in cache: ",
                            userDetails.getFailureMessage());
                }
            }
        }

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Query Payy Result failed due to ::{}", queryPayResultResponse.getFailureMessage());
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant());
                if (queryPayResultResponse.getResponse() != null) {
                    responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
                }
                return responseBean;
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeFundOrder(workFlowTransBean);
            }
        }

        // Check payment status & Transaction status in case of IMPS
        if (PaymentTypeIdEnum.IMPS.value.equals(flowRequestBean.getPaymentTypeId())) {
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeFundOrder(workFlowTransBean);
            } else {
                queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
                if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(
                            queryByAcquirementIdResponse.getFailureMessage(),
                            queryByAcquirementIdResponse.getResponseConstant());
                }
                workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
                workFlowTransBean.setPaymentDone(true);
            }
        }

        // Check payment Status in case of UPI
        if (PaymentTypeIdEnum.UPI.value.equals(flowRequestBean.getPaymentTypeId())) {

            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()
                    || PaymentStatus.FAIL.toString().equals(
                            queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeFundOrder(workFlowTransBean);
                return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                        ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED);
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);
        }

        // Setting data in workFlowResponse bean for processing
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setExtendedInfo(extendInfo);
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void closeFundOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = workFlowHelper
                .closeFundOrder(workFlowTransBean);
        if (!cancelFundOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelFundOrder.getFailureMessage());
        }
    }

}