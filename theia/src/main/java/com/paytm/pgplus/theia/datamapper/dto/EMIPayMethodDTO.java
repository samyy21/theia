package com.paytm.pgplus.theia.datamapper.dto;

import java.io.Serializable;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;

/**
 * 
 * @author ruchikagarg
 *
 */
public class EMIPayMethodDTO extends BasePayMethodDTO implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1605536932764685909L;

    public EMIPayMethodDTO() {
    }

    public EMIPayMethodDTO(PaymentRequestBean paymentRequestBean, WorkFlowRequestBean workFlowRequestBean) {
        super(paymentRequestBean, workFlowRequestBean);
    }

}
