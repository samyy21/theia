package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.biz.workflow.model.MotoResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @createdOn 12-Dec-2017
 * @author shubham singh
 */
@Service("motoWorkflow")
public class MotoWorkFlow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(MotoWorkFlow.class);

    @Autowired
    @Qualifier("motoValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.specificBeanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(
                    workFlowTransBean, flowRequestBean.getToken(), true);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());

        } else {
            return new GenericCoreResponseBean<>("Invalid request data", ResponseConstants.SYSTEM_ERROR);
        }

        if (!validateSavedCardId(workFlowTransBean.getUserDetails().getMerchantViewSavedCardsList(), flowRequestBean,
                workFlowTransBean)) {
            LOGGER.warn("Saved Card ID : {} is not present in User Details", flowRequestBean.getSavedCardID());
            returnResponseForInvalidSavedCard(flowRequestBean);
        }

        if (!workFlowHelper.validateSavedCardForMoto(workFlowTransBean.getSavedCard(), workFlowTransBean)) {
            LOGGER.warn("Saved Card ID : {} is not allowed", workFlowTransBean.getSavedCard().getCardId());
            return returnResponseForInvalidSavedCard(flowRequestBean);
        }

        // Cache CC Card to Alipay
        final GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponse = workFlowHelper.cacheCard(
                workFlowTransBean, CacheCardType.NORMAL);
        if (!cacheCardResponse.isSuccessfullyProcessed()) {
            return returnResponseForInvalidSavedCard(flowRequestBean);
        }
        workFlowTransBean.setCacheCardToken(cacheCardResponse.getResponse().getTokenId());

        // Moto Channel is 'SYSYTEM' terminal type request
        workFlowTransBean.getWorkFlowBean().getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);

        // Create Order And Pay
        final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay = workFlowHelper
                .createOrderAndPay(workFlowTransBean);
        if (!createOrderAndPay.isSuccessfullyProcessed()) {
            return returnResponseForOrderNotCreated(flowRequestBean, createOrderAndPay);
        }

        workFlowTransBean.setCashierRequestId(createOrderAndPay.getResponse().getCashierRequestId());
        if (createOrderAndPay.getResponse().getSecurityPolicyResult() != null) {
            workFlowTransBean.setRiskResult(createOrderAndPay.getResponse().getSecurityPolicyResult().getRiskResult());
        }
        workFlowTransBean.setTransID(createOrderAndPay.getResponse().getAcquirementId());
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        MotoResponse motoResponse = createMotoResponse(flowRequestBean, workFlowTransBean);
        workFlowResponseBean.setMotoResponse(motoResponse);
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());

        LOGGER.info("Returning Response Bean From Moto request, trans Id : {} ", workFlowTransBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private boolean validateSavedCardId(List<CardBeanBiz> listofCardBeanBiz, final WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {
        for (CardBeanBiz cardBeanBiz : listofCardBeanBiz) {
            if (null != cardBeanBiz.getCardId() && null != flowRequestBean.getSavedCardID()
                    && flowRequestBean.getSavedCardID().equals(String.valueOf(cardBeanBiz.getCardId()))) {
                workFlowTransBean.setSavedCard(cardBeanBiz);
                return true;
            }
        }
        return false;
    }

    private MotoResponse createMotoResponse(final WorkFlowRequestBean flowRequestBean,
            final WorkFlowTransactionBean workFlowTransBean) {
        MotoResponse motoResponse = new MotoResponse();
        motoResponse.setOrderId(flowRequestBean.getOrderID());
        motoResponse.setMid(flowRequestBean.getPaytmMID());
        motoResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        motoResponse.setTxnId(workFlowTransBean.getTransID());
        motoResponse.setStatus(ExternalTransactionStatus.TXN_ACCEPTED.name());
        return motoResponse;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForOrderNotCreated(
            final WorkFlowRequestBean flowRequestBean,
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        MotoResponse motoResponse = new MotoResponse();
        motoResponse.setOrderId(flowRequestBean.getOrderID());
        motoResponse.setMid(flowRequestBean.getPaytmMID());
        motoResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        motoResponse.setTxnId(workFlowResponseBean.getTransID());
        motoResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        ResponseConstants responseCode = createOrderAndPay.getResponseConstant() != null ? createOrderAndPay
                .getResponseConstant() : ResponseConstants.SYSTEM_ERROR;
        motoResponse.setRespCode(responseCode.getCode());
        if (StringUtils.isNotBlank(createOrderAndPay.getRiskRejectUserMessage())) {
            motoResponse.setRespMsg(createOrderAndPay.getRiskRejectUserMessage());
        } else {
            motoResponse.setRespMsg(responseCode.getMessage());
        }
        workFlowResponseBean.setMotoResponse(motoResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForInvalidSavedCard(
            final WorkFlowRequestBean flowRequestBean) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        MotoResponse motoResponse = new MotoResponse();
        motoResponse.setOrderId(flowRequestBean.getOrderID());
        motoResponse.setMid(flowRequestBean.getPaytmMID());
        motoResponse.setTxnAmount(flowRequestBean.getTxnAmount());
        motoResponse.setTxnId(workFlowResponseBean.getTransID());
        motoResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        motoResponse.setRespCode(ResponseConstants.INVALID_SAVED_CARD_ID.getCode());
        motoResponse.setRespMsg(workFlowHelper.getResponseForResponseConstant(ResponseConstants.INVALID_SAVED_CARD_ID));
        workFlowResponseBean.setMotoResponse(motoResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

}
