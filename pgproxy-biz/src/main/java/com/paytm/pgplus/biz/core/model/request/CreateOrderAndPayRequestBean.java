package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Map;

import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.common.model.PaymentBizInfo;

public class CreateOrderAndPayRequestBean implements Serializable {

    private static final long serialVersionUID = 7172153894379797388L;

    private BizCreateOrderRequest order;
    private String merchantId;
    private String mcc;
    private String productCode;
    private EnvInfoRequestBean envInfo;
    private ExtendedInfoRequestBean extendInfo;
    private String requestId;
    private BizPaymentInfo paymentInfo;
    private String securityId;
    private Map<String, String> riskExtendInfo;
    private boolean fromAoaMerchant;
    private boolean costBasedPreferenceEnabled;
    private boolean willUserChange = true;
    private PaymentBizInfo paymentBizInfo;
    private String paytmMerchantId;
    private String requestFlow;
    private Map<String, String> detailExtendInfo;

    public CreateOrderAndPayRequestBean(BizCreateOrderRequest order, String merchantId, String mcc, String productCode,
            String requestId, BizPaymentInfo paymentInfo, ExtendedInfoRequestBean extendInfo,
            Map<String, String> riskExtendInfo, boolean costBasedPreferenceEnabled, String paytmMerchantId) {
        this.order = order;
        this.merchantId = merchantId;
        this.mcc = mcc;
        this.productCode = productCode;
        this.requestId = requestId;
        this.paymentInfo = paymentInfo;
        this.extendInfo = extendInfo;
        this.riskExtendInfo = riskExtendInfo;
        this.costBasedPreferenceEnabled = costBasedPreferenceEnabled;
        this.paytmMerchantId = paytmMerchantId;
    }

    public BizCreateOrderRequest getOrder() {
        return order;
    }

    public void setOrder(BizCreateOrderRequest order) {
        this.order = order;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public EnvInfoRequestBean getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(EnvInfoRequestBean envInfo) {
        this.envInfo = envInfo;
    }

    public ExtendedInfoRequestBean getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(ExtendedInfoRequestBean extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public BizPaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(BizPaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public Map<String, String> getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setRiskExtendInfo(Map<String, String> riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public boolean isCostBasedPreferenceEnabled() {
        return costBasedPreferenceEnabled;
    }

    public void setCostBasedPreferenceEnabled(boolean costBasedPreferenceEnabled) {
        this.costBasedPreferenceEnabled = costBasedPreferenceEnabled;
    }

    public boolean isWillUserChange() {
        return willUserChange;
    }

    public void setWillUserChange(boolean willUserChange) {
        this.willUserChange = willUserChange;
    }

    public PaymentBizInfo getPaymentBizInfo() {
        return paymentBizInfo;
    }

    public void setPaymentBizInfo(PaymentBizInfo paymentBizInfo) {
        this.paymentBizInfo = paymentBizInfo;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public String getRequestFlow() {
        return requestFlow;
    }

    public void setRequestFlow(String requestFlow) {
        this.requestFlow = requestFlow;
    }

    public Map<String, String> getDetailExtendInfo() {
        return detailExtendInfo;
    }

    public void setDetailExtendInfo(Map<String, String> detailExtendInfo) {
        this.detailExtendInfo = detailExtendInfo;
    }
}
