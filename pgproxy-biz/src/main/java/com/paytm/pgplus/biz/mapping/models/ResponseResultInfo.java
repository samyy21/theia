package com.paytm.pgplus.biz.mapping.models;

import java.io.Serializable;

import com.paytm.pgplus.common.model.ResultInfo;

public class ResponseResultInfo implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8935431055896520091L;

    private ResultInfo resultInfo;

    /**
     * @return the resultInfo
     */
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    /**
     * @param resultInfo
     *            the resultInfo to set
     */
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

}
