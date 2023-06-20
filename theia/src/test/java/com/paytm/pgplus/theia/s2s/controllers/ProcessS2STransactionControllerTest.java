package com.paytm.pgplus.theia.s2s.controllers;

import com.paytm.pgplus.biz.workflow.model.PaymentS2SResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequest;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestBody;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestHeader;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SResponseUtil;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.rabbitmq.client.AMQP;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessS2STransactionControllerTest {

    @InjectMocks
    private ProcessS2STransactionController processS2STransactionController;

    @Mock
    private IJsonResponsePaymentService seamlessS2SPaymentService;

    @Mock
    private PaymentS2SResponseUtil responseUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processPaymentRequest() {

        HttpServletRequest request = mock(HttpServletRequest.class);
        PaymentS2SRequest paymentS2SRequest = new PaymentS2SRequest();
        paymentS2SRequest.setHeader(new PaymentS2SRequestHeader());
        paymentS2SRequest.setBody(new PaymentS2SRequestBody());
        paymentS2SRequest.getBody().setPaymentTypeId(EPayMethod.UPI_INTENT.getMethod());
        when(responseUtil.generateResponse(any())).thenReturn(new PaymentS2SResponse());
        assertNotNull(processS2STransactionController.processPaymentRequest(request, paymentS2SRequest));
        paymentS2SRequest.getBody().setRequestType(TheiaConstant.RequestTypes.SEAMLESS_3D_FORM);
        when(seamlessS2SPaymentService.processPaymentRequest(any())).thenReturn(mock(WorkFlowResponseBean.class));
        assertNull(processS2STransactionController.processPaymentRequest(request, paymentS2SRequest));

    }

    @Test
    public void processValidationError() {

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
                .thenReturn(Collections.singletonList(new FieldError("object", "header", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "body", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "channelId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "clientId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "signature", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "version", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "requestType", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "txnAmount", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "orderId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "mid", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "custId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "industryTypeId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "website", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "paymentTypeId", "message")))
                .thenReturn(Collections.singletonList(new FieldError("object", "", "message")));

        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));
        processS2STransactionController.processValidationError(new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult));

    }

    @Test
    public void processPaymentRequestValidationError() {
        processS2STransactionController.processPaymentRequestValidationError(new PaymentRequestValidationException());
    }

    @Test
    public void processInternalServerError() {
        processS2STransactionController.processInternalServerError(new PaymentRequestValidationException());
    }
}