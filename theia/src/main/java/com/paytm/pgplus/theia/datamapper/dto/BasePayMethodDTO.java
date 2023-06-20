package com.paytm.pgplus.theia.datamapper.dto;

import java.io.Serializable;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;

/**
 * 
 * @author ruchikagarg
 *
 */
public class BasePayMethodDTO implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2942605783880620940L;

    protected PaymentRequestBean paymentRequestBean;

    protected WorkFlowRequestBean workFlowRequestBean;

    public BasePayMethodDTO() {

    }

    public BasePayMethodDTO(PaymentRequestBean paymentRequestBean, WorkFlowRequestBean workFlowRequestBean) {
        this.paymentRequestBean = paymentRequestBean;
        this.workFlowRequestBean = workFlowRequestBean;
    }

    public PaymentRequestBean getPaymentRequestBean() {
        return paymentRequestBean;
    }

    public WorkFlowRequestBean getWorkFlowRequestBean() {
        return workFlowRequestBean;
    }

}
