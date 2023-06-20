/**
 * 
 */
package com.paytm.pgplus.cashier.models;

/**
 * @author amit.dubey
 *
 */
public class CashierResultInfo {
    private String resultMsg;
    private String resultMsgCode;
    private String errParamPath;
    private String errParamMsg;

    public CashierResultInfo() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param resultMsg
     * @param resultMsgCode
     * @param errParamPath
     * @param errParamMsg
     */
    public CashierResultInfo(String resultMsg, String resultMsgCode, String errParamPath, String errParamMsg) {
        this.resultMsg = resultMsg;
        this.resultMsgCode = resultMsgCode;
        this.errParamPath = errParamPath;
        this.errParamMsg = errParamMsg;
    }

    /**
     * @return the resultMsg
     */
    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * @param resultMsg
     *            the resultMsg to set
     */
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    /**
     * @return the resultMsgCode
     */
    public String getResultMsgCode() {
        return resultMsgCode;
    }

    /**
     * @param resultMsgCode
     *            the resultMsgCode to set
     */
    public void setResultMsgCode(String resultMsgCode) {
        this.resultMsgCode = resultMsgCode;
    }

    /**
     * @return the errParamPath
     */
    public String getErrParamPath() {
        return errParamPath;
    }

    /**
     * @param errParamPath
     *            the errParamPath to set
     */
    public void setErrParamPath(String errParamPath) {
        this.errParamPath = errParamPath;
    }

    /**
     * @return the errParamMsg
     */
    public String getErrParamMsg() {
        return errParamMsg;
    }

    /**
     * @param errParamMsg
     *            the errParamMsg to set
     */
    public void setErrParamMsg(String errParamMsg) {
        this.errParamMsg = errParamMsg;
    }

}
