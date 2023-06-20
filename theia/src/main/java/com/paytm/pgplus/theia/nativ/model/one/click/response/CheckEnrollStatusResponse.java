package com.paytm.pgplus.theia.nativ.model.one.click.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.CheckEnrollStatusResponseBody;
import com.paytm.pgplus.common.model.CheckEnrollStatusResponseDataAccount;
import com.paytm.pgplus.common.model.InstaProxyEnrollStatusResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckEnrollStatusResponse implements Serializable {

    private static final long serialVersionUID = 5123978224609202600L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private CheckEnrollStatusResponseBody body;

    public CheckEnrollStatusResponse(ResultCode resultCode) {
        this.body = new CheckEnrollStatusResponseBody();
        this.head = new ResponseHeader();

        if (resultCode == null) {
            resultCode = ResultCode.FAILED;
        }
        this.body.setResultInfo(NativePaymentUtil.resultInfo(resultCode));
    }

    public CheckEnrollStatusResponse(com.paytm.pgplus.common.model.ResultInfo resultInfo) {
        this.body = new CheckEnrollStatusResponseBody();
        this.head = new ResponseHeader();
        if (resultInfo != null) {
            ResultInfo resultInfo1 = new ResultInfo();
            this.body.setResultInfo(new ResultInfo(resultInfo.getResultStatus(), resultInfo.getResultCode(), resultInfo
                    .getResultMsg()));
        }
    }

    public CheckEnrollStatusResponse(ResponseHeader head, CheckEnrollStatusResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public CheckEnrollStatusResponse(InstaProxyEnrollStatusResponse instaProxyAPIResponse) {

        this.head = new ResponseHeader();
        String resultStatus = "F";
        String resultCode = "9999";
        String resultMsg = "Something went wrong";
        List<CheckEnrollStatusResponseDataAccount> accountStatusDataList = null;
        if (null != instaProxyAPIResponse) {
            String status = instaProxyAPIResponse.getStatus();
            if (StringUtils.isNotBlank(status) && status.equalsIgnoreCase("SUCCESS")
                    && instaProxyAPIResponse.getData().getAccountStatusDataList() != null) {
                resultStatus = "S";
                resultCode = "0000";
                resultMsg = "Success";
                accountStatusDataList = instaProxyAPIResponse.getData().getAccountStatusDataList();
            } else {
                new CheckEnrollStatusResponse(ResultCode.UNKNOWN_ERROR);
            }
        }

        ResultInfo resultInfo = new ResultInfo(resultStatus, resultCode, resultMsg);
        this.body = new CheckEnrollStatusResponseBody(resultInfo, accountStatusDataList);
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public CheckEnrollStatusResponseBody getBody() {
        return body;
    }

    public void setBody(CheckEnrollStatusResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckEnrollStatusResponse {").append("head=")
                .append(head.toString()).append("body=").append(body.toString()).append('}');
        return sb.toString();
    }
}
