package com.paytm.pgplus.theia.s2s.utils;

import com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.s2s.enums.ResponseCode;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestHeader;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.*;

public class PaymentS2SResponseUtilTest {

    PaymentS2SResponseUtil paymentS2SResponseUtil = new PaymentS2SResponseUtil();

    @Test
    public void generateResponseForChecksumFailure() {
        PaymentS2SUtil.setRequestHeader(new PaymentS2SRequestHeader());
        assertNotNull(paymentS2SResponseUtil.generateResponseForChecksumFailure(new PaymentRequestBean()));

    }

    @Test
    public void processMerchantFailResponse() {

        assertNotNull(paymentS2SResponseUtil.processMerchantFailResponse(new PaymentRequestBean(),
                ResponseConstants.MERCHANT_FAILURE_RESPONSE));
    }

    @Test
    public void generateMerchantResponseWithBankForm() {

        PaymentS2SUtil.setRequestHeader(new PaymentS2SRequestHeader());
        assertNotNull(paymentS2SResponseUtil.generateMerchantResponseWithBankForm(new PaymentRequestBean(),
                new BankRedirectionDetail(), new BankForm()));

    }

    @Test
    public void getResponseHeaderForV1() {

        assertNotNull(paymentS2SResponseUtil.getResponseHeaderForV1());
    }

    @Test
    public void generateResponse() {
        assertNotNull(paymentS2SResponseUtil.generateResponse(ResponseCode.SUCCESS_RESPONSE_CODE));
    }
}