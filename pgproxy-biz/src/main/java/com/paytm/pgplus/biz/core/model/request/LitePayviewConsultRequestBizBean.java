package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EmiPlanParam;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.model.TransAmount;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by rahulverma on 8/9/17.
 */

public class LitePayviewConsultRequestBizBean implements Serializable {

    private static final long serialVersionUID = 8079546320696466867L;

    private String payerUserId;
    private EnvInfoRequestBean envInfoRequestBean;
    private ERequestType productCode;
    private String customizeCode;
    private String platformMid;
    private boolean isPostConvenienceFee;
    private List<String> exclusionPayMethods;
    private Map<String, String> extendInfo;
    private boolean fromAoaMerchant;
    private boolean defaultLitePayView;
    private String requestType;
    private String externalUserId;
    private String fetchSavedAsset;

    /**
     * Cards will be fetched for userLoggedIn
     */
    private String fetchSavedAssetForUser;

    /**
     * Cards will be fetched if merchant storecardpref is enabled
     */
    private String fetchSavedAssetForMerchant;
    private String pwpCategory;
    private boolean dynamicFeeMerchant;
    private String addAndPayMigration;
    private boolean defaultDynamicFeeMerchantPayment;
    private boolean includeDisabledAssets;
    private String payConfirmFlowType;
    private String blockPeriodInSeconds;
    private String verificationType;
    private String paytmMerchantId;
    private String paytmUserId;
    private boolean isAddAndPayRequest;
    private TransAmount transAmount;
    private EmiPlanParam emiPlanParam;

    private boolean isDealsFlow;
    private String lpvProductCode;

    public boolean isDealsFlow() {
        return isDealsFlow;
    }

    public void setDealsFlow(boolean dealsFlow) {
        isDealsFlow = dealsFlow;
    }

    public LitePayviewConsultRequestBizBean() {
    }

    public LitePayviewConsultRequestBizBean(String payerUserId, EnvInfoRequestBean envInfoRequestBean,
            ERequestType productCode, String customizeCode, String platformMid, boolean isPostConvenienceFee,
            List<String> exclusionPayMethods, Map<String, String> extendInfo, String pwpCategory,
            boolean dynamicFeeMerchant) {
        this.payerUserId = payerUserId;
        this.envInfoRequestBean = envInfoRequestBean;
        this.productCode = productCode;
        this.customizeCode = customizeCode;
        this.platformMid = platformMid;
        this.exclusionPayMethods = exclusionPayMethods;
        this.extendInfo = extendInfo;
        this.isPostConvenienceFee = isPostConvenienceFee;
        this.pwpCategory = pwpCategory;
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public LitePayviewConsultRequestBizBean(String payerUserId, EnvInfoRequestBean envInfoRequestBean,
            ERequestType productCode, String customizeCode, String platformMid, boolean isPostConvenienceFee,
            List<String> exclusionPayMethods, Map<String, String> extendInfo, String pwpCategory,
            boolean dynamicFeeMerchant, String paytmMerchantId, String paytmUserId, boolean isAddAndPayRequest) {
        this.payerUserId = payerUserId;
        this.envInfoRequestBean = envInfoRequestBean;
        this.productCode = productCode;
        this.customizeCode = customizeCode;
        this.platformMid = platformMid;
        this.exclusionPayMethods = exclusionPayMethods;
        this.extendInfo = extendInfo;
        this.isPostConvenienceFee = isPostConvenienceFee;
        this.pwpCategory = pwpCategory;
        this.dynamicFeeMerchant = dynamicFeeMerchant;
        this.paytmMerchantId = paytmMerchantId;
        this.paytmUserId = paytmUserId;
        this.isAddAndPayRequest = isAddAndPayRequest;
    }

    public String getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(String payerUserId) {
        this.payerUserId = payerUserId;
    }

    public EnvInfoRequestBean getEnvInfoRequestBean() {
        return envInfoRequestBean;
    }

    public void setEnvInfoRequestBean(EnvInfoRequestBean envInfo) {
        this.envInfoRequestBean = envInfo;
    }

    public ERequestType getProductCode() {
        return productCode;
    }

    public void setProductCode(ERequestType productCode) {
        this.productCode = productCode;
    }

    public String getCustomizeCode() {
        return customizeCode;
    }

    public void setCustomizeCode(String customizeCode) {
        this.customizeCode = customizeCode;
    }

    public String getPlatformMid() {
        return platformMid;
    }

    public boolean isPostConvenienceFee() {
        return isPostConvenienceFee;
    }

    public void setPostConvenienceFee(boolean postConvenienceFee) {
        isPostConvenienceFee = postConvenienceFee;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public void setPlatformMid(String platformMid) {
        this.platformMid = platformMid;
    }

    public List<String> getExclusionPayMethods() {
        return exclusionPayMethods;
    }

    public void setExclusionPayMethods(List<String> exclusionPayMethods) {
        this.exclusionPayMethods = exclusionPayMethods;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public boolean isDefaultLitePayView() {
        return defaultLitePayView;
    }

    public void setDefaultLitePayView(boolean defaultLitePayView) {
        this.defaultLitePayView = defaultLitePayView;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getFetchSavedAsset() {
        return fetchSavedAsset;
    }

    public void setFetchSavedAsset(String fetchSavedAsset) {
        this.fetchSavedAsset = fetchSavedAsset;
    }

    public String getFetchSavedAssetForUser() {
        return fetchSavedAssetForUser;
    }

    public void setFetchSavedAssetForUser(String fetchSavedAssetForUser) {
        this.fetchSavedAssetForUser = fetchSavedAssetForUser;
    }

    public String getFetchSavedAssetForMerchant() {
        return fetchSavedAssetForMerchant;
    }

    public void setFetchSavedAssetForMerchant(String fetchSavedAssetForMerchant) {
        this.fetchSavedAssetForMerchant = fetchSavedAssetForMerchant;
    }

    public String getPwpCategory() {
        return pwpCategory;
    }

    public void setPwpCategory(String pwpCategory) {
        this.pwpCategory = pwpCategory;
    }

    public boolean isDefaultDynamicFeeMerchantPayment() {
        return defaultDynamicFeeMerchantPayment;
    }

    public void setDefaultDynamicFeeMerchantPayment(boolean defaultDynamicFeeMerchantPayment) {
        this.defaultDynamicFeeMerchantPayment = defaultDynamicFeeMerchantPayment;
    }

    public String getAddAndPayMigration() {
        return addAndPayMigration;
    }

    public void setAddAndPayMigration(String addAndPayMigration) {
        this.addAndPayMigration = addAndPayMigration;
    }

    public boolean isIncludeDisabledAssets() {
        return includeDisabledAssets;
    }

    public void setIncludeDisabledAssets(boolean includeDisabledAssets) {
        this.includeDisabledAssets = includeDisabledAssets;
    }

    public String getPayConfirmFlowType() {
        return payConfirmFlowType;
    }

    public void setPayConfirmFlowType(String payConfirmFlowType) {
        this.payConfirmFlowType = payConfirmFlowType;
    }

    public String getBlockPeriodInSeconds() {
        return blockPeriodInSeconds;
    }

    public void setBlockPeriodInSeconds(String blockPeriodInSeconds) {
        this.blockPeriodInSeconds = blockPeriodInSeconds;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public String getPaytmUserId() {
        return paytmUserId;
    }

    public boolean isAddAndPayRequest() {
        return isAddAndPayRequest;
    }

    public TransAmount getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(TransAmount transAmount) {
        this.transAmount = transAmount;
    }

    public EmiPlanParam getEmiPlanParam() {
        return emiPlanParam;
    }

    public void setEmiPlanParam(EmiPlanParam emiPlanParam) {
        this.emiPlanParam = emiPlanParam;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public void setPaytmUserId(String paytmUserId) {
        this.paytmUserId = paytmUserId;
    }

    public String getLpvProductCode() {
        return lpvProductCode;
    }

    public void setLpvProductCode(String lpvProductCode) {
        this.lpvProductCode = lpvProductCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LitePayviewConsultRequestBizBean that = (LitePayviewConsultRequestBizBean) o;
        return Objects.equals(payerUserId, that.payerUserId)
                && Objects.equals(envInfoRequestBean, that.envInfoRequestBean) && productCode == that.productCode
                && Objects.equals(customizeCode, that.customizeCode) && Objects.equals(platformMid, that.platformMid)
                && Objects.equals(exclusionPayMethods, that.exclusionPayMethods)
                && Objects.equals(extendInfo, that.extendInfo) && Objects.equals(pwpCategory, that.pwpCategory)
                && Objects.equals(verificationType, that.verificationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payerUserId, envInfoRequestBean, productCode, customizeCode, platformMid,
                exclusionPayMethods, extendInfo, pwpCategory, verificationType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LitePayviewConsultRequestBizBean{");
        sb.append("payerUserId='").append(payerUserId).append('\'');
        sb.append(", envInfoRequestBean=").append(envInfoRequestBean);
        sb.append(", productCode=").append(productCode);
        sb.append(", customizeCode='").append(customizeCode).append('\'');
        sb.append(", platformMid='").append(platformMid).append('\'');
        sb.append(", isPostConvenienceFee=").append(isPostConvenienceFee);
        sb.append(", exclusionPayMethods=").append(exclusionPayMethods);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append(", fromAoaMerchant=").append(fromAoaMerchant);
        sb.append(", defaultLitePayView=").append(defaultLitePayView);
        sb.append(", requestType='").append(requestType);
        sb.append(", externalUserId=").append(externalUserId);
        sb.append(", fetchSavedAsset='").append(fetchSavedAsset);
        sb.append(", fetchSavedAssetForUser='").append(fetchSavedAssetForUser);
        sb.append(", pwpCategory='").append(pwpCategory);
        sb.append(", dynamicFeeMerchant=").append(dynamicFeeMerchant);
        sb.append(", addAndPayMigration=").append(addAndPayMigration);
        sb.append(", fetchSavedAssetForMerchant='").append(fetchSavedAssetForMerchant).append('\'');
        sb.append(", includeDisabledAssets='").append(includeDisabledAssets).append('\'');
        sb.append(",payConfirmFlowType=").append(payConfirmFlowType);
        sb.append(",blockPeriodInSeconds=").append(blockPeriodInSeconds);
        sb.append(",verificationType=").append(verificationType);
        sb.append(",transAmount=").append(transAmount);
        sb.append(",emiPlanParam=").append(emiPlanParam);
        sb.append(",lpvProductCode=").append(lpvProductCode);
        sb.append('}');
        return sb.toString();
    }
}
