package com.paytm.pgplus.theia.nativ.model.consent;

import com.paytm.pgplus.response.ResultInfo;

import java.io.Serializable;

/**
 * Created by: satyamsinghrajput at 24/10/19
 */
public class NativeConsentResponseBody implements Serializable {

    private static final long serialVersionUID = 6662732128090300983L;

    private String seqNo;
    private ResultInfo resultInfo;

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        return "NativeConsentResponseBody{" + "seqNo='" + seqNo + '\'' + ", resultInfo=" + resultInfo + '}';
    }
}
