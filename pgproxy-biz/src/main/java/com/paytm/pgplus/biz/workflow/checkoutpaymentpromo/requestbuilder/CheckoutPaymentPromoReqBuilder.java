package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.CheckoutPromoServiceRequestV2;

public interface CheckoutPaymentPromoReqBuilder {

    CheckoutPromoServiceRequest build(WorkFlowRequestBean workFlowRequestBean);

    CheckoutPromoServiceRequestV2 buildV2(WorkFlowRequestBean workFlowRequestBean);

    CheckoutPromoServiceRequestV2 buildCoftV2(WorkFlowRequestBean workFlowRequestBean);
}
