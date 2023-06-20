/**
 * 
 */
package com.paytm.pgplus.theia.models.response;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;

import java.io.Serializable;
import java.util.Map;

/**
 * @author naman
 *
 */
public class PageDetailsResponse implements Serializable {

    private static final long serialVersionUID = 596322382454427415L;

    private boolean isSuccessfullyProcessed;

    private String htmlPage;

    private String jspName;

    private String s2sResponse;

    private boolean kycPageRequired;

    private Map<String, String> data;

    private String redirectionUrl;

    private String addMoneyToGvConsentKey;

    private boolean riskVerificationRequired;

    private String transId;

    private Exception exception;

    public PageDetailsResponse() {

    }

    public PageDetailsResponse(boolean isSuccessfullyProcessed) {
        this.isSuccessfullyProcessed = isSuccessfullyProcessed;
    }

    public boolean isSuccessfullyProcessed() {
        return isSuccessfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean isSuccessfullyProcessed) {
        this.isSuccessfullyProcessed = isSuccessfullyProcessed;
    }

    public String getHtmlPage() {
        return htmlPage;
    }

    public void setHtmlPage(String htmlPage) {
        this.htmlPage = htmlPage;
    }

    public String getJspName() {
        return jspName;
    }

    public void setJspName(String jspName) {
        this.jspName = jspName;
    }

    /**
     * @return the s2sResponse
     */
    public String getS2sResponse() {
        return s2sResponse;
    }

    /**
     * @param s2sResponse
     *            the s2sResponse to set
     */
    public void setS2sResponse(String s2sResponse) {
        this.s2sResponse = s2sResponse;
    }

    public boolean isKycPageRequired() {
        return kycPageRequired;
    }

    public void setKycPageRequired(boolean kycPageRequired) {
        this.kycPageRequired = kycPageRequired;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }

    public boolean isRiskVerificationRequired() {
        return riskVerificationRequired;
    }

    public void setRiskVerificationRequired(boolean riskVerificationRequired) {
        this.riskVerificationRequired = riskVerificationRequired;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getAddMoneyToGvConsentKey() {
        return addMoneyToGvConsentKey;
    }

    public void setAddMoneyToGvConsentKey(String addMoneyToGvConsentKey) {
        this.addMoneyToGvConsentKey = addMoneyToGvConsentKey;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "PageDetailsResponse{" + "isSuccessfullyProcessed=" + isSuccessfullyProcessed + ", htmlPage='"
                + htmlPage + '\'' + ", jspName='" + jspName + '\'' + ", s2sResponse='" + s2sResponse + '\''
                + ", kycPageRequired=" + kycPageRequired + ", data=" + data + ", redirectionUrl='" + redirectionUrl
                + '\'' + ", addMoneyToGvConsentKey='" + addMoneyToGvConsentKey + '\'' + ", riskVerificationRequired="
                + riskVerificationRequired + ", transId='" + transId + '\'' + '}';
    }

}
