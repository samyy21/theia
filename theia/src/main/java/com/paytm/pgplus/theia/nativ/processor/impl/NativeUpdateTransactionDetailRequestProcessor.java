package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailRequest;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativePaymentService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("nativeUpdateTransactionDetailRequestProcessor")
public class NativeUpdateTransactionDetailRequestProcessor
        extends
        AbstractRequestProcessor<UpdateTransactionDetailRequest, UpdateTransactionDetailResponse, UpdateTransactionDetailRequest, Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeUpdateTransactionDetailRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("nativePaymentService")
    private INativePaymentService nativePaymentService;

    @Override
    protected UpdateTransactionDetailRequest preProcess(UpdateTransactionDetailRequest request) {
        validate(request);
        return request;
    }

    @Override
    protected Boolean onProcess(UpdateTransactionDetailRequest request, UpdateTransactionDetailRequest serviceReq)
            throws Exception {
        return nativePaymentService.updateTransactionDetail(serviceReq);

    }

    @Override
    protected UpdateTransactionDetailResponse postProcess(UpdateTransactionDetailRequest request,
            UpdateTransactionDetailRequest serviceReq, Boolean serviceRes) throws Exception {
        return createResponse(serviceReq, serviceRes);
    }

    private UpdateTransactionDetailResponse createResponse(UpdateTransactionDetailRequest request, Boolean serviceRes)
            throws Exception {
        BaseResponseBody responseBody = new BaseResponseBody();
        if (BooleanUtils.isNotTrue(serviceRes)) {
            responseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.UPDATE_TXN_DETAIL_FAILED));
        }
        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        UpdateTransactionDetailResponse response = new UpdateTransactionDetailResponse(responseHeader, responseBody);
        return response;
    }

    private void validate(UpdateTransactionDetailRequest request) {

        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());

        nativeValidationService.validateUpdateTxnDetail(request);
    }
}
