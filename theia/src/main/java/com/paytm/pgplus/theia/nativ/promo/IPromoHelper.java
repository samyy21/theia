package com.paytm.pgplus.theia.nativ.promo;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailRequest;

public interface IPromoHelper {

    public PromoCodeResponse applyPromoCode(WorkFlowRequestBean workFlowRequestBean, String transId, String txnAmount);

    public PromoCodeResponse validatePromoCode(String promoCode, String mid);

    public PromoCodeResponse validatePromoCodePaymentMode(String paytmMid, String txntype, String promoCode,
            String cardNo, String bankCode);

    public PromoCodeResponse updatePromoCode(WorkFlowRequestBean workFlowRequestBean, String transId, String txnAmount);

    public PromoCodeResponse validatePromoCodePaymentMode(NativePromoCodeDetailRequest serviceReq);
}
