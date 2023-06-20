package com.paytm.pgplus.theia.nativ.model.one.click.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.CheckEnrollStatusResponseBody;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeEnrollOneClickResponse implements Serializable {

    private static final long serialVersionUID = 5123978224609202600L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private BaseResponseBody body;

    public DeEnrollOneClickResponse(ResultCode resultCode) {
        this.body = new BaseResponseBody();
        this.head = new ResponseHeader();

        if (resultCode == null) {
            resultCode = ResultCode.FAILED;
        }
        this.body.setResultInfo(NativePaymentUtil.resultInfo(resultCode));
    }

    public DeEnrollOneClickResponse(com.paytm.pgplus.common.model.ResultInfo resultInfo) {
        this.body = new CheckEnrollStatusResponseBody();
        this.head = new ResponseHeader();
        if (resultInfo != null) {
            ResultInfo resultInfo1 = new ResultInfo();
            this.body.setResultInfo(new ResultInfo(resultInfo.getResultStatus(), resultInfo.getResultCode(), resultInfo
                    .getResultMsg()));
        }
    }

    public DeEnrollOneClickResponse(InstaProxyDeEnrollOneClickResponse instaProxyAPIResponse) {

        this.head = new ResponseHeader();

        String resultStatus = "F";
        String resultCode = "0001";
        String resultMsg = "Failed";

        if (null != instaProxyAPIResponse) {
            String status = instaProxyAPIResponse.getStatus();
            if (StringUtils.isNotBlank(status) && status.equalsIgnoreCase("SUCCESS")) {
                resultStatus = "S";
                resultCode = "0000";
                resultMsg = "Success";
            }
            resultMsg = StringUtils.isNotBlank(instaProxyAPIResponse.getMessage()) ? instaProxyAPIResponse.getMessage()
                    : resultMsg;
        }

        ResultInfo resultInfo = new ResultInfo(resultStatus, resultCode, resultMsg);
        this.body = new BaseResponseBody(resultInfo);
    }

    public DeEnrollOneClickResponse(ResponseHeader head, BaseResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public BaseResponseBody getBody() {
        return body;
    }

    public void setBody(BaseResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeEnrollOneClickResponseBody {").append("head=")
                .append(head.toString()).append("body=").append(body.toString()).append('}');
        return sb.toString();
    }
}
