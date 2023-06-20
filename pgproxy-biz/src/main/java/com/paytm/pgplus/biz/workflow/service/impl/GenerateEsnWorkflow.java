package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.enums.GenerateEsnEnum;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("GenerateEsnWorkflow")
public class GenerateEsnWorkflow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(GenerateEsnWorkflow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        GenerateEsnResponseBody response = new GenerateEsnResponseBody();
        if (ERequestType.isSubscriptionRequest(flowRequestBean.getPaymentRequestBean().getRequestType())) {
            StringBuilder key = new StringBuilder((flowRequestBean.getPaymentRequestBean().getRequestType()))
                    .append((flowRequestBean.getPaymentRequestBean().getTxnToken()));
            SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                    .toString());
            if (subscriptionResponse == null) {
                LOGGER.error("Exception occurred while fetching subscription details ");
                setGenerateEsnResponse(GenerateEsnEnum.SYSTEM_ERROR, response,
                        GenerateEsnEnum.SYSTEM_ERROR.getResultMsg());
                workFlowResponseBean.setGenerateEsnResponseBody(response);
                return new GenericCoreResponseBean<>(workFlowResponseBean);
            }
            workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
        }
        LOGGER.info("Pay called from GenerateEsnWorkflow");
        final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
        if (!payResponse.isSuccessfullyProcessed()) {
            setGenerateEsnResponse(GenerateEsnEnum.PLATFORM_ERROR, response,
                    GenerateEsnEnum.PLATFORM_ERROR.getResultMsg());
            workFlowResponseBean.setGenerateEsnResponseBody(response);
            return new GenericCoreResponseBean<>(workFlowResponseBean);
        }
        workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = workFlowHelper
                .fetchBankForm(workFlowTransBean);
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Error while fetching bankform due to : {}", queryPayResultResponse.getFailureMessage());
            GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                    queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant());
            if (queryPayResultResponse.getResponse() != null) {
                responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
            }
            return responseBean;
        }
        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
            workFlowTransBean.setPaymentDone(false);
            setGenerateEsnResponse(GenerateEsnEnum.FAIL, response, GenerateEsnEnum.FAIL.getResultMsg());
        }

        String webFormContext = workFlowTransBean.getQueryPaymentStatus().getWebFormContext();
        String externalSerialNo = "";
        try {
            if (StringUtils.isNotBlank(webFormContext)) {
                // webFormContext holds ESN in BankFormOptimizedFlow
                if (workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams() != null
                        && workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams()
                                .isBankFormOptimizedFlow()) {
                    externalSerialNo = webFormContext;
                } else {
                    MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext,
                            MerchantVpaTxnInfo.class);
                    externalSerialNo = (null != merchantVpaTxnInfo) ? merchantVpaTxnInfo.getExternalSrNo() : null;
                }
            } else {
                LOGGER.error("Got empty webFormContext:");
            }
        } catch (Exception e) {
            LOGGER.error("Error while parsing json of webformcontext : {}", webFormContext);
        }
        if (StringUtils.isNotBlank(queryPayResultResponse.getFailureMessage())) {
            setGenerateEsnResponse(GenerateEsnEnum.FAIL, response, GenerateEsnEnum.FAIL.getResultMsg());
        } else if ((PaymentStatus.REDIRECT.name().equals(
                workFlowTransBean.getQueryPaymentStatus().getPaymentStatusValue()) || (workFlowTransBean
                .getWorkFlowBean().getBankFormOptimizationParams() != null && workFlowTransBean.getWorkFlowBean()
                .getBankFormOptimizationParams().isBankFormOptimizedFlow()))
                && StringUtils.isNotBlank(externalSerialNo)) {
            setGenerateEsnResponse(GenerateEsnEnum.SUCCESS, response, GenerateEsnEnum.SUCCESS.getResultMsg());
        } else {
            setGenerateEsnResponse(GenerateEsnEnum.FAIL, response, GenerateEsnEnum.FAIL.getResultMsg());
        }
        response.setNewExternalSerialNo(externalSerialNo);
        workFlowResponseBean.setGenerateEsnResponseBody(response);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void setGenerateEsnResponse(GenerateEsnEnum resultEnum, GenerateEsnResponseBody body, String msg) {
        body.setResultCode(resultEnum.getResultCode());
        body.setResultCodeId(resultEnum.getResultCodeId());
        body.setResultMsg(msg);
    }
}
