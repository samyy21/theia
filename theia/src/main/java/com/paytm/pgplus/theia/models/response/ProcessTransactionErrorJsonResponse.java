package com.paytm.pgplus.theia.models.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

public class ProcessTransactionErrorJsonResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3749057632951329379L;
    @JsonProperty("MID")
    private final String mid;
    @JsonProperty("ORDERID")
    private final String orderId;
    @JsonProperty("RESPCODE")
    private final String respCode;
    @JsonProperty("RESPMSG")
    private final String respMsg;
    @JsonProperty("STATUS")
    private final String status;

    public String getMid() {
        return mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getRespCode() {
        return respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public String getStatus() {
        return status;
    }

    /**
     * @param mid
     * @param orderId
     * @param respCode
     * @param respMsg
     * @param status
     */
    public ProcessTransactionErrorJsonResponse(String mid, String orderId, String respCode, String respMsg,
            String status) {
        super();
        this.mid = mid;
        this.orderId = orderId;
        this.respCode = respCode;
        this.respMsg = respMsg;
        this.status = status;
    }

    /**
     * @param mid
     * @param orderId
     */
    public ProcessTransactionErrorJsonResponse(String mid, String orderId) {
        super();
        this.mid = mid;
        this.orderId = orderId;
        respCode = ResponseConstants.SYSTEM_ERROR.getCode();
        respMsg = ResponseConstants.SYSTEM_ERROR.getMessage();
        status = ExternalTransactionStatus.PENDING.name();
    }

    /**
     * @param mid
     * @param orderId
     */
    public ProcessTransactionErrorJsonResponse(String mid, String orderId, ResponseConstants responseConstant) {
        super();
        this.mid = mid;
        this.orderId = orderId;
        respCode = responseConstant.getCode();
        respMsg = responseConstant.getMessage();
        status = ExternalTransactionStatus.TXN_FAILURE.name();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcessTransactionErrorJsonResponse [mid=").append(mid).append(", orderId=").append(orderId)
                .append(", respCode=").append(respCode).append(", respMsg=").append(respMsg).append(", status=")
                .append(status).append("]");
        return builder.toString();
    }

}
