package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import mockit.MockUp;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasePaymentOptionBuilderTest {

    private BasePaymentOptionBuilder basePaymentOptionBuilder = new BasePaymentOptionBuilder();

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePaymentOptionBuilder.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        new MockUp<PayMethod>() {
            @mockit.Mock
            private PayMethod getPayMethodByMethod() {
                return null;
            }
        };
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBuild() {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setPayMethod(PayMethod.BALANCE);
        promoPaymentOption.setTransactionAmount("1000");
        basePaymentOptionBuilder.build(promoPaymentOption, "mid");
    }

    @Test
    public void testValidatePromoPaymentOption() {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setPayMethod(PayMethod.BALANCE);
        promoPaymentOption.setTransactionAmount("1000");
        basePaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption);

        exceptionRule.expect(RequestValidationException.class);
        PromoPaymentOption promoPaymentOption1 = new PromoPaymentOption();
        basePaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption1);
    }

    @Test
    public void testBuildPromoPaymentOptions() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setPayMethod("PPBL");
        workFlowRequestBean.setBankCode("BankCode");
        workFlowRequestBean.setInstId("instId");
        basePaymentOptionBuilder.buildPromoPaymentOptions(workFlowRequestBean, "txnAmount", "paymentMethod", "PPBL");

        WorkFlowRequestBean bean1 = new WorkFlowRequestBean();
        basePaymentOptionBuilder.buildPromoPaymentOptions(bean1, "txnAmount", "paymentMethod", "PPBL");
    }
}