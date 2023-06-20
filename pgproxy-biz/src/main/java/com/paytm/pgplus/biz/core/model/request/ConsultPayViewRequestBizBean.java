/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;

/**
 * @author namanjain
 *
 */
public class ConsultPayViewRequestBizBean implements Serializable {

    private static final long serialVersionUID = 1816512510139535759L;

    private String payerUserId;
    private String transID;
    private ETransType transType;
    private ERequestType productCode;
    private ETerminalType terminalType;
    private EnvInfoRequestBean envInfoReqBean;
    private boolean isPostConvenienceFee;
    private Map<String, String> riskExtendInfo;
    private Map<String, String> extendInfo;
    private List<String> exclusionPayMethods;
    private boolean slabBasedMDR;
    private boolean dynamicFeeMerchant;

    public ERequestType getProductCode() {
        return productCode;
    }

    public void setProductCode(final ERequestType productCode) {
        this.productCode = productCode;
    }

    public ETerminalType getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(final ETerminalType terminalType) {
        this.terminalType = terminalType;
    }

    public String getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(final String payerUserId) {
        this.payerUserId = payerUserId;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(final String transID) {
        this.transID = transID;
    }

    public ETransType getTransType() {
        return transType;
    }

    public void setTransType(final ETransType transType) {
        this.transType = transType;
    }

    public EnvInfoRequestBean getEnvInfoReqBean() {
        return envInfoReqBean;
    }

    public void setEnvInfoReqBean(final EnvInfoRequestBean envInfoReqBean) {
        this.envInfoReqBean = envInfoReqBean;
    }

    public Map<String, String> getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setRiskExtendInfo(Map<String, String> riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
    }

    public List<String> getExclusionPayMethods() {
        return exclusionPayMethods;
    }

    public void setExclusionPayMethods(List<String> exclusionPayMethods) {
        this.exclusionPayMethods = exclusionPayMethods;
    }

    public ConsultPayViewRequestBizBean(final String payerUserId, final String transID, final ETransType transType,
            final ERequestType productCode, final ETerminalType terminalType, final EnvInfoRequestBean envInfoReqBean,
            final Map<String, String> riskExtendInfo, final Map<String, String> extendInfo,
            final List<String> exclusionPayMethods) {
        this.payerUserId = payerUserId;
        this.transID = transID;
        this.transType = transType;
        this.productCode = productCode;
        this.terminalType = terminalType;
        this.envInfoReqBean = envInfoReqBean;
        this.riskExtendInfo = riskExtendInfo;
        this.extendInfo = extendInfo;
        this.exclusionPayMethods = exclusionPayMethods;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((terminalType == null) ? 0 : terminalType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConsultPayViewRequestBizBean other = (ConsultPayViewRequestBizBean) obj;
        if (terminalType != other.terminalType) {
            return false;
        }
        return true;
    }

    /**
     * @return the isPostConvenienceFee
     */
    public boolean isPostConvenienceFee() {
        return isPostConvenienceFee;
    }

    /**
     * @param isPostConvenienceFee
     *            the isPostConvenienceFee to set
     */
    public void setPostConvenienceFee(boolean isPostConvenienceFee) {
        this.isPostConvenienceFee = isPostConvenienceFee;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isSlabBasedMDR() {
        return slabBasedMDR;
    }

    public void setSlabBasedMDR(boolean slabBasedMDR) {
        this.slabBasedMDR = slabBasedMDR;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConsultPayViewRequestBizBean [payerUserId=").append(payerUserId).append(", transID=")
                .append(transID).append(", transType=").append(transType).append(", productCode=").append(productCode)
                .append(", terminalType=").append(terminalType).append(", envInfoReqBean=").append(envInfoReqBean)
                .append(", isPostConvenienceFee=").append(isPostConvenienceFee).append(", riskExtendInfo=")
                .append(riskExtendInfo).append(", extendInfo=").append(extendInfo).append(", exclusionPayMethods=")
                .append(exclusionPayMethods).append(", slabBasedMDR=").append(slabBasedMDR)
                .append(", dynamicFeeMerchant=").append(dynamicFeeMerchant).append("]");
        return builder.toString();
    }

}
