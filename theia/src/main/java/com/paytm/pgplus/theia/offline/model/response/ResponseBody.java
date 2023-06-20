/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.model.base.BaseBody;

/**
 * Created by rahulverma on 28/8/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseBody extends BaseBody {

    private static final long serialVersionUID = -9009073158873397985L;
    @NotNull
    private ResultInfo resultInfo;

    public ResponseBody() {

    }

    public ResponseBody(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseBody{");
        sb.append("bizResultInfo=").append(resultInfo);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
