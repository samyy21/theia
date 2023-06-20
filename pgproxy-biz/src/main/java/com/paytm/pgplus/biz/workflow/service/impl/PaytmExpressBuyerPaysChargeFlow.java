package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.utils.SeamlessCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_PROMO;

@Service("paytmExpressBuyerPaysChargeFlow")
public class PaytmExpressBuyerPaysChargeFlow implements IWorkFlow {

    @Autowired()
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public static final Logger LOGGER = LoggerFactory.getLogger(PaytmExpressBuyerPaysChargeFlow.class);

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        if (PaymentTypeIdEnum.CC.value.equals(workFlowTransBean.getWorkFlowBean().getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(workFlowTransBean.getWorkFlowBean().getPaymentTypeId())
                || PaymentTypeIdEnum.EMI.value.equals(workFlowTransBean.getWorkFlowBean().getPaymentTypeId())) {
            workFlowTransBean.setCacheCardToken(flowRequestBean.getPaymentDetails());
        }

        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        final boolean isSavedCardTxn = flowRequestBean.getIsSavedCard();
        final boolean storeCard = ((flowRequestBean.getStoreCard() != null) && "1".equals(flowRequestBean
                .getStoreCard().trim())) ? true : false;

        if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
        }

        if (ff4jUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(), ENABLE_GCIN_ON_COFT_PROMO, false)
                && flowRequestBean.isCoftTokenTxn()) {
            workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

            // cache card
            final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                    .cacheBankCardInfo(flowRequestBean, workFlowTransBean);
            if (createCacheCardResponse != null) {
                return createCacheCardResponse;
            }

            flowRequestBean.setPaymentDetails(workFlowTransBean.getCacheCardToken());
        }

        /*
         * If add Money case consult wallet
         */
        if (EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())) {
            final GenericCoreResponseBean<Boolean> consultAddMoney = workFlowHelper.consultAddMoney(workFlowTransBean);
            if (!consultAddMoney.isSuccessfullyProcessed() || consultAddMoney.getResponse().equals(false)) {
                return new GenericCoreResponseBean<>("AddMoneyNotAllowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            }
        }
        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        final GenericCoreResponseBean<ConsultFeeResponse> consultFee = workFlowHelper
                .consultBulkFeeResponse(workFlowTransBean);
        if (!consultFee.isSuccessfullyProcessed()) {
            LOGGER.error("Cosnult Fee Failed due to :::{}", consultFee.getFailureMessage());
            return new GenericCoreResponseBean<>(consultFee.getFailureMessage(), consultFee.getResponseConstant());
        }
        workFlowTransBean.setConsultFeeResponse(consultFee.getResponse());

        /*
         * Create Order And Pay
         */
        final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                .createOrderAndPay(workFlowTransBean);
        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
            if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                        createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant(),
                        createOrderAndPayResponse.getRiskRejectUserMessage());
                responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
                return responseBean;
            } else {
                return new GenericCoreResponseBean<>(createOrderAndPayResponse.getFailureMessage(),
                        createOrderAndPayResponse.getResponseConstant());
            }
        }
        workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
        if (createOrderAndPayResponse.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(createOrderAndPayResponse.getResponse().getSecurityPolicyResult()
                    .getRiskResult());
        }
        workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        /*
         * Caching card details in Redis
         */
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
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

        /*
         * Fetch Bank Form using Query_PayResult API in case of CC/DC &
         * NetBanking
         */
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
                closeOrder(workFlowTransBean);
            }
        }
        // Check payment status & Transaction status in case of IMPS
        if (PaymentTypeIdEnum.IMPS.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.PPI.value.equals(flowRequestBean.getPaymentTypeId())) {
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            // Need to close order in case paymentStatus is FAIL
            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
                closeOrder(workFlowTransBean);
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
                closeOrder(workFlowTransBean);
                return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                        ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED);
            }

            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
            workFlowTransBean.setPaymentDone(false);
        }

        // Setting data in workFlowResponse bean for processing
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "PaytmExpressBuyerPaysChargeFlow",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }
}
