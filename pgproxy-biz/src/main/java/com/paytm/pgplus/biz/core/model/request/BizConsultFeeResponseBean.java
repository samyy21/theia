package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Map;

import com.paytm.pgplus.common.enums.EPayMethod;

/**
 * @author namanjain
 *
 */
public class BizConsultFeeResponseBean implements Serializable {

    private static final long serialVersionUID = 30L;

    private Map<EPayMethod, BizConsultFeeDetails> consultFeeResponse;

    public Map<EPayMethod, BizConsultFeeDetails> getConsultFeeResponse() {
        return consultFeeResponse;
    }

    public void setConsultFeeResponse(Map<EPayMethod, BizConsultFeeDetails> consultFeeResponse) {
        this.consultFeeResponse = consultFeeResponse;
    }

}
