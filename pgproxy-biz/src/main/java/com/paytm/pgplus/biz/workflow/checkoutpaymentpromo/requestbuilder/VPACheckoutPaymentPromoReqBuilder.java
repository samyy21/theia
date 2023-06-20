package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class VPACheckoutPaymentPromoReqBuilder extends BaseCheckoutPaymentPromoReqBuilder {
    @Override
    public void validateWorkflowReqForCheckoutPaymentPromo(WorkFlowRequestBean workFlowRequestBean) {
        super.validateWorkflowReqForCheckoutPaymentPromo(workFlowRequestBean);
        if (StringUtils.isNotBlank(workFlowRequestBean.getVirtualPaymentAddress())) {
            return;
        }
        throw new BizMerchantVelocityBreachedException("CheckoutPaymentPromo req builder validation failed");
    }
}
