package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BasePaymentOptionBuilder implements ApplyPromoPaymentOptionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasePaymentOptionBuilder.class);

    @Override
    public PaymentOption build(PromoPaymentOption promoPaymentOption, String mid) {
        validatePromoPaymentOption(promoPaymentOption);
        PaymentOption paymentOption = new PaymentOption();
        PayMethod payMethod = null;
        try {
            payMethod = PayMethod.getPayMethodByMethod(promoPaymentOption.getPayMethod().getMethod());
        } catch (FacadeInvalidParameterException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }
        paymentOption.setPayMethod(payMethod);
        paymentOption
                .setTransactionAmount(PaymentOfferUtils.getAmountInPaise(promoPaymentOption.getTransactionAmount()));
        paymentOption.setIssuingBank(promoPaymentOption.getBankCode());
        paymentOption.setVpa(promoPaymentOption.getVpa());
        if (StringUtils.isNotBlank(promoPaymentOption.getTenure())) {
            paymentOption.setTenure(Integer.valueOf(promoPaymentOption.getTenure()));
        }
        return paymentOption;
    }

    @Override
    public PaymentOption buildForCoftPromoTxns(PromoPaymentOption promoPaymentOption, String mid, String txnToken) {
        validatePromoPaymentOption(promoPaymentOption);
        PaymentOption paymentOption = new PaymentOption();
        PayMethod payMethod = null;
        try {
            payMethod = PayMethod.getPayMethodByMethod(promoPaymentOption.getPayMethod().getMethod());
        } catch (FacadeInvalidParameterException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw BaseException.getException();
        }
        paymentOption.setPayMethod(payMethod);
        paymentOption
                .setTransactionAmount(PaymentOfferUtils.getAmountInPaise(promoPaymentOption.getTransactionAmount()));
        paymentOption.setIssuingBank(promoPaymentOption.getBankCode());
        paymentOption.setVpa(promoPaymentOption.getVpa());
        if (StringUtils.isNotBlank(promoPaymentOption.getTenure())) {
            paymentOption.setTenure(Integer.valueOf(promoPaymentOption.getTenure()));
        }
        return paymentOption;
    }

    @Override
    public List<PromoPaymentOption> buildPromoPaymentOptions(WorkFlowRequestBean workFlowRequestBean, String txnAmount,
            String paymentMethod, String issuingBankName) {
        List<PromoPaymentOption> paymentOptions = new ArrayList<>();
        com.paytm.pgplus.enums.PayMethod payMethod = null;
        try {
            if (StringUtils.isBlank(workFlowRequestBean.getPayMethod())) {
                payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(paymentMethod);
            } else {
                payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(workFlowRequestBean.getPayMethod());
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
        }
        PromoPaymentOption paymentOption = new PromoPaymentOption();
        String issuingBank = StringUtils.isNotBlank(workFlowRequestBean.getBankCode()) ? workFlowRequestBean
                .getBankCode() : workFlowRequestBean.getInstId();

        // handled for old savedCardId
        if (StringUtils.isBlank(issuingBank)) {
            issuingBank = issuingBankName;
        }

        if (PayMethod.PPBL.getMethod().equals(issuingBank)) {
            paymentOption.setPayMethod(com.paytm.pgplus.enums.PayMethod.PPBL);
        } else {
            paymentOption.setPayMethod(payMethod);
            paymentOption.setBankCode(issuingBank);
        }
        paymentOption.setVpa(workFlowRequestBean.getVirtualPaymentAddress());
        // TODO Ask if we need to put check of hybrid
        paymentOption.setTransactionAmount(txnAmount);
        if (PayMethod.EMI.getMethod().equals(workFlowRequestBean.getPayMethod())
                || PayMethod.EMI_DC.getMethod().equals(workFlowRequestBean.getPayMethod())) {
            paymentOption.setTenure(getTenure(workFlowRequestBean));
        }
        paymentOptions.add(paymentOption);
        return paymentOptions;
    }

    private String getTenure(WorkFlowRequestBean workFlowRequestBean) {
        String emiPlanId = null;
        PaymentRequestBean paymentRequestBean = workFlowRequestBean.getPaymentRequestBean();
        if (null != paymentRequestBean && StringUtils.isNotBlank(paymentRequestBean.getEmiPlanID())) {
            emiPlanId = paymentRequestBean.getEmiPlanID();
            String emiMonth = StringUtils.substringAfter(emiPlanId, "|");
            return emiMonth;
        }
        return null;
    }

    public void validatePromoPaymentOption(PromoPaymentOption promoPaymentOption) {
        if (promoPaymentOption != null && promoPaymentOption.getPayMethod() != null
                && StringUtils.isNotBlank(promoPaymentOption.getTransactionAmount())) {
            return;
        }
        throw RequestValidationException.getException();
    }
}
