package com.paytm.pgplus.theia.s2s.utils;

import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.s2s.enums.ResponseCode;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PaymentS2SResponseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentS2SResponseUtil.class);

    public PaymentS2SResponse generateResponseForChecksumFailure(PaymentRequestBean paymentRequestBean) {

        PaymentS2SRequestHeader requestHeader = PaymentS2SUtil.getRequestHeader();
        PaymentS2SResponseHeader responseHeader = new PaymentS2SResponseHeader(requestHeader.getClientId(),
                requestHeader.getVersion(), String.valueOf(System.currentTimeMillis()), StringUtils.EMPTY);

        BizResultInfo bizResultInfo = getResultInfoFromResponseCode(ResponseCode.INVALID_CHECKSUM);
        PaymentS2SResponseBody responseBody = new PaymentS2SResponseBody(bizResultInfo);
        responseBody.setOrderId(paymentRequestBean.getOrderId());
        responseBody.setPaymentMode(paymentRequestBean.getPaymentTypeId());

        PaymentS2SResponse response = new PaymentS2SResponse(responseHeader, responseBody);
        return response;
    }

    public PaymentS2SResponse processMerchantFailResponse(PaymentRequestBean requestData,
            ResponseConstants responseConstants) {

        PaymentS2SResponseHeader responseHeader = new PaymentS2SResponseHeader("v1", String.valueOf(System
                .currentTimeMillis()));

        BizResultInfo bizResultInfo = mapResponseConstantToResultInfo(responseConstants);
        PaymentS2SResponseBody responseBody = new PaymentS2SResponseBody(bizResultInfo);
        responseBody.setOrderId(requestData.getOrderId());
        responseBody.setPaymentMode(requestData.getPaymentTypeId());

        PaymentS2SResponse response = new PaymentS2SResponse(responseHeader, responseBody);
        return response;
    }

    public PaymentS2SResponse generateMerchantResponseWithBankForm(PaymentRequestBean paymentRequestBean,
            BankRedirectionDetail bankRequest, BankForm bankForm) {

        PaymentS2SRequestHeader requestHeader = PaymentS2SUtil.getRequestHeader();
        PaymentS2SResponseHeader responseHeader = new PaymentS2SResponseHeader(requestHeader.getClientId(),
                requestHeader.getVersion(), String.valueOf(System.currentTimeMillis()), StringUtils.EMPTY);

        BizResultInfo bizResultInfo = getResultInfoFromResponseCode(ResponseCode.SUCCESS_RESPONSE_CODE);

        PaymentS2SResponseBody responseBody = new PaymentS2SResponseBody(bizResultInfo);
        responseBody.setOrderId(paymentRequestBean.getOrderId());
        responseBody.setPaymentMode(paymentRequestBean.getPaymentTypeId());
        responseBody.setBankRequest(bankRequest);
        responseBody.setBankForm(bankForm);

        PaymentS2SResponse response = new PaymentS2SResponse(responseHeader, responseBody);
        return response;
    }

    private BizResultInfo mapResponseConstantToResultInfo(ResponseConstants responseConstant) {

        ResponseCode responseCode = ResponseCode.getResponseCodeByResponseConstant(responseConstant);

        /*
         * Error Code Mapping not found setting INTERNAL_PROCESSING_ERROR
         */
        if (Objects.isNull(responseCode)) {
            LOGGER.error("Failure code mapping not found for response constant :: {}", responseConstant);
            responseCode = ResponseCode.INTERNAL_PROCESSING_ERROR;
        }

        return getResultInfoFromResponseCode(responseCode);
    }

    public BizResultInfo getResultInfoFromResponseCode(ResponseCode responseCode) {
        BizResultInfo bizResultInfo = new BizResultInfo();
        bizResultInfo.setResultCode(responseCode.getResultCode());
        bizResultInfo.setResultCodeId(responseCode.getResultCodeId());
        bizResultInfo.setResultStatus(responseCode.getResultStatus());
        bizResultInfo.setResultMsg(responseCode.getResultMsg());
        return bizResultInfo;
    }

    public PaymentS2SResponseHeader getResponseHeaderForV1() {
        return new PaymentS2SResponseHeader("v1", String.valueOf(System.currentTimeMillis()));
    }

    public PaymentS2SResponse generateResponse(ResponseCode responseCode) {
        BizResultInfo bizResultInfo = getResultInfoFromResponseCode(responseCode);
        PaymentS2SResponseHeader responseHeader = getResponseHeaderForV1();
        PaymentS2SResponseBody responseBody = new PaymentS2SResponseBody(bizResultInfo);
        PaymentS2SResponse response = new PaymentS2SResponse(responseHeader, responseBody);
        LOGGER.info("Final Response generated : {}", response);
        return response;
    }

}
