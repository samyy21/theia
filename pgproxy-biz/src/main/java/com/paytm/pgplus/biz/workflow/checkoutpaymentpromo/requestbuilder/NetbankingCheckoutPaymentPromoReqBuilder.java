package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NetbankingCheckoutPaymentPromoReqBuilder extends BaseCheckoutPaymentPromoReqBuilder {

    @Override
    public void validateWorkflowReqForCheckoutPaymentPromo(WorkFlowRequestBean workFlowRequestBean) {
        super.validateWorkflowReqForCheckoutPaymentPromo(workFlowRequestBean);
        if (StringUtils.isNotBlank(workFlowRequestBean.getBankCode())) {
            return;
        }
        throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
    }
}
