/*
 * @Dev - AmitD
 * @Date - 14 Feb 2018
 */

package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.utils.MapperUtils;
import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoDebitCoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitCoreService.class);

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    public void setResponseData(GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean,
            AutoDebitResponse autoDebitResponse, PaymentRequestBean requestBean) {
        // Need to check if BIZ flow break somewhere
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponse() == null) {
            LOGGER.info("Biz Call Unsuccessful due to : {}", bizResponseBean.getFailureDescription());
            // Need to prepare AutoDebitResponse
            autoDebitResponse
                    .setStatus(ResponseConstants.SYSTEM_ERROR.equals(bizResponseBean.getResponseConstant()) ? ExternalTransactionStatus.PENDING
                            .name() : ExternalTransactionStatus.TXN_FAILURE.name());
            autoDebitResponse.setResponseCode(bizResponseBean.getResponseConstant().getCode());
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                autoDebitResponse.setResponseMessage(bizResponseBean.getRiskRejectUserMessage());
            } else {
                autoDebitResponse.setResponseMessage(bizResponseBean.getResponseConstant().getMessage());
            }
            if (StringUtils.equals("Invalid Token", bizResponseBean.getFailureDescription())
                    && requestBean.isOfflineFastForwardRequest()) {
                autoDebitResponse.setResponseCode(ResponseConstants.INVALID_SSO_TOKEN.getCode());
                autoDebitResponse.setResponseMessage(ResponseConstants.INVALID_SSO_TOKEN.getMessage());
            }

        } else if (bizResponseBean.getResponse().getQueryPaymentStatus() != null
                && bizResponseBean.getResponse().getQueryTransactionStatus() != null
                && PaymentStatus.SUCCESS.toString().equals(
                        bizResponseBean.getResponse().getQueryPaymentStatus().getPaymentStatusValue())
                && AcquirementStatusType.SUCCESS.toString().equals(
                        bizResponseBean.getResponse().getQueryTransactionStatus().getStatusDetailType())) {
            // When Payment & Transaction both SUCCESS : Setting transId
            setLoyaltyPoints(bizResponseBean, autoDebitResponse);
            autoDebitResponse.setTxnId(bizResponseBean.getResponse().getTransID());
            autoDebitResponse.setStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
            autoDebitResponse
                    .setResponseCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SUCCESS_RESPONSE_CODE
                            .getCode());
            if (bizResponseBean != null
                    && bizResponseBean.getResponse() != null
                    && bizResponseBean.getResponse().getQueryPaymentStatus() != null
                    && bizResponseBean.getResponse().getQueryPaymentStatus().getPayOptions() != null
                    && bizResponseBean.getResponse().getQueryPaymentStatus().getPayOptions().get(0) != null
                    && bizResponseBean.getResponse().getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo() != null
                    && bizResponseBean.getResponse().getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo()
                            .get("referenceNo") != null) {
                autoDebitResponse.setBankTxnId(bizResponseBean.getResponse().getQueryPaymentStatus().getPayOptions()
                        .get(0).getExtendInfo().get("referenceNo"));
            }
            autoDebitResponse.setResponseMessage("Txn Successful.");

        } else {
            /*
             * Otherwise setting status & codes accordingly, using Payment
             * status & Transaction status
             */
            autoDebitResponse.setTxnId(bizResponseBean.getResponse().getTransID());
            autoDebitResponse.setStatus(MapperUtils.getTransactionStatusForResponse(bizResponseBean.getResponse()
                    .getQueryTransactionStatus(), bizResponseBean.getResponse().getQueryPaymentStatus()));
            setResponseCodeAndMessage(autoDebitResponse, bizResponseBean.getResponse());
        }
    }

    private void setLoyaltyPoints(GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean,
            AutoDebitResponse autoDebitResponse) {
        if (bizResponseBean.getResponse().getQueryPaymentStatus() != null
                && bizResponseBean.getResponse().getQueryPaymentStatus().getExtendInfo() != null) {
            if (bizResponseBean.getResponse().getQueryPaymentStatus().getExtendInfo().get("pointNumber") != null)
                autoDebitResponse.setLoyaltyPoints(bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getExtendInfo().get("pointNumber"));
            else if (bizResponseBean.getResponse().getQueryTransactionStatus().getExtendInfo().get("pointNumber") != null)
                autoDebitResponse.setLoyaltyPoints(bizResponseBean.getResponse().getQueryTransactionStatus()
                        .getExtendInfo().get("pointNumber"));
        }
    }

    private void setResponseCodeAndMessage(AutoDebitResponse autoDebitResponse,
            WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean.getQueryPaymentStatus() != null) {
            String alipayRespCode = null;
            if (StringUtils.isNotBlank(workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode())) {
                alipayRespCode = workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode();
            } else {
                alipayRespCode = workFlowResponseBean.getQueryPaymentStatus().getPaymentErrorCode();
            }
            ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(alipayRespCode,
                    SystemResponseCode.SYSTEM_ERROR, MapperUtils.getTransactionStatusForResponse(
                            workFlowResponseBean.getQueryTransactionStatus(),
                            workFlowResponseBean.getQueryPaymentStatus()));
            if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
                autoDebitResponse.setResponseCode(responseCodeDetails.getResponseCode());
                autoDebitResponse.setResponseMessage(responseCodeUtil.getResponseMsg(responseCodeDetails));
            }
        }
    }

    private void setSystemErrorCode(AutoDebitResponse autoDebitResponse) {
        autoDebitResponse.setResponseMessage(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR
                .getMessage());
        autoDebitResponse
                .setResponseCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getCode());
    }

    public GenericCoreResponseBean<WorkFlowResponseBean> generateAutoDebitResponse(
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean, AutoDebitResponse autoDebitResponse) {
        if (bizResponseBean.getResponse() != null) {
            bizResponseBean.getResponse().setAutoDebitResponse(autoDebitResponse);
        } else {
            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
            workFlowResponseBean.setAutoDebitResponse(autoDebitResponse);
            return new GenericCoreResponseBean<WorkFlowResponseBean>(workFlowResponseBean);
        }
        return bizResponseBean;
    }
}
