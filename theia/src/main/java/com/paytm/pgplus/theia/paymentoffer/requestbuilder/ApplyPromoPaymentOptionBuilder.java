package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;

import java.util.List;

public interface ApplyPromoPaymentOptionBuilder {

    PaymentOption build(PromoPaymentOption promoPaymentOption, String mid);

    List<PromoPaymentOption> buildPromoPaymentOptions(WorkFlowRequestBean requestBean, String txnAmount,
            String paymentMethod, String issuingBank);

    PaymentOption buildForCoftPromoTxns(PromoPaymentOption promoPaymentOption, String mid, String txnToken);

}
