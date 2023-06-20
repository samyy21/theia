package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NetbankingPaymentOptionBuilder extends BasePaymentOptionBuilder {

    @Override
    public void validatePromoPaymentOption(PromoPaymentOption promoPaymentOption) {
        super.validatePromoPaymentOption(promoPaymentOption);
        if (StringUtils.isNotBlank(promoPaymentOption.getBankCode())) {
            return;
        }
        throw RequestValidationException.getException();
    }
}
