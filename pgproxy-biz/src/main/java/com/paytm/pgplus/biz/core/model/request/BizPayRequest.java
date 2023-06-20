/**
 * 
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.PaymentBizInfo;
import com.paytm.pgplus.facade.enums.PaymentScenario;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author namanjain
 *
 */
public class BizPayRequest implements Serializable {

    private static final long serialVersionUID = 3639010592326277671L;

    private String transID;

    private ETransType transType;

    private String payerUserID;

    private List<BizPayOptionBill> payOptionBills;

    private EnvInfo envInfo;

    private String securityId;

    private String requestID;

    private ERequestType productCode;

    private Map<String, String> extInfo;

    private Map<String, String> riskExtendInfo;

    private PaymentScenario paymentScenario;

    private String pwpCategory;

    private boolean addAndPayMigration;

    private boolean costBasedPreferenceEnabled;

    private PaymentBizInfo paymentBizInfo;

    private String paytmMerchantId;

    private String requestFlow;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    private String verificationType;

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public Map<String, String> getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String transID) {
        this.transID = transID;
    }

    public ETransType getTransType() {
        return transType;
    }

    public void setTransType(ETransType transType) {
        this.transType = transType;
    }

    public String getPayerUserID() {
        return payerUserID;
    }

    public void setPayerUserID(String payerUserID) {
        this.payerUserID = payerUserID;
    }

    public List<BizPayOptionBill> getPayOptionBills() {
        return payOptionBills;
    }

    public void setPayOptionBills(List<BizPayOptionBill> payOptionBills) {
        this.payOptionBills = payOptionBills;
    }

    public EnvInfo getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(EnvInfo envInfo) {
        this.envInfo = envInfo;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public ERequestType getProductCode() {
        return productCode;
    }

    public void setProductCode(ERequestType productCode) {
        this.productCode = productCode;
    }

    public Map<String, String> getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setRiskExtendInfo(Map<String, String> riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
    }

    public PaymentScenario getPaymentScenario() {
        return paymentScenario;
    }

    public void setPaymentScenario(PaymentScenario paymentScenario) {
        this.paymentScenario = paymentScenario;
    }

    public String getPwpCategory() {
        return pwpCategory;
    }

    public void setPwpCategory(String pwpCategory) {
        this.pwpCategory = pwpCategory;
    }

    public boolean isAddAndPayMigration() {
        return addAndPayMigration;
    }

    public void setAddAndPayMigration(boolean addAndPayMigration) {
        this.addAndPayMigration = addAndPayMigration;
    }

    public boolean isCostBasedPreferenceEnabled() {
        return costBasedPreferenceEnabled;
    }

    public void setCostBasedPreferenceEnabled(boolean costBasedPreferenceEnabled) {
        this.costBasedPreferenceEnabled = costBasedPreferenceEnabled;
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

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public BizPayRequest(String transID, ETransType transType, String payerUserID,
            List<BizPayOptionBill> payOptionBills, String securityId, String requestID, ERequestType productCode,
            Map<String, String> extInfo, PaymentScenario paymentScenario, String pwpCategory,
            boolean addAndPayMigration, boolean costBasedPreferenceEnabled, String paytmMerchantId) {
        this.transID = transID;
        this.transType = transType;
        this.payerUserID = payerUserID;
        this.payOptionBills = payOptionBills;
        this.securityId = securityId;
        this.requestID = requestID;
        this.productCode = productCode;
        this.extInfo = extInfo;
        this.paymentScenario = paymentScenario;
        this.pwpCategory = pwpCategory;
        this.addAndPayMigration = addAndPayMigration;
        this.costBasedPreferenceEnabled = costBasedPreferenceEnabled;
        this.paytmMerchantId = paytmMerchantId;
    }

}
