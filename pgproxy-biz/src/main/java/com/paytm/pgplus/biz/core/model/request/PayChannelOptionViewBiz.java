/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.paytm.pgplus.models.Money;

public class PayChannelOptionViewBiz implements Serializable {

    private static final long serialVersionUID = 8807185057088702567L;

    private String payOption;
    private boolean enableStatus;
    private String disableReason;
    private String instId;
    private String instName;
    private List<BalanceChannelInfoBiz> balanceChannelInfos;
    private List<EMIChannelInfoBiz> emiChannelInfos;
    private List<EMIChannelInfoBiz> emiHybridChannelInfos;
    private List<String> supportCountries;
    private List<String> directServiceInsts;
    private List<String> supportAtmPins;
    private Map<String, String> extendInfo;
    private List<ExternalAccountInfoBiz> externalAccountInfos;
    private String templateId;
    private Boolean oneClickChannel;
    private boolean prepaidCardChannel;
    private List<String> supportPayOptionSubTypes;
    private List<String> dccServiceInstIds;
    private String blockPeriodInSeconds;
    private Money maxBlockAmount;
    private Integer excessCapturePercentage;
    private String payConfirmFlowType;
    private String instDispCode;
    private boolean hasLowSuccessRate;

    public boolean isHasLowSuccessRate() {
        return hasLowSuccessRate;
    }

    public void setHasLowSuccessRate(boolean hasLowSuccessRate) {
        this.hasLowSuccessRate = hasLowSuccessRate;
    }

    public Boolean getOneClickChannel() {
        return oneClickChannel;
    }

    public void setOneClickChannel(Boolean oneClickChannel) {
        this.oneClickChannel = oneClickChannel;
    }

    public List<ExternalAccountInfoBiz> getExternalAccountInfos() {
        return externalAccountInfos;
    }

    public void setExternalAccountInfos(List<ExternalAccountInfoBiz> externalAccountInfos) {
        this.externalAccountInfos = externalAccountInfos;
    }

    public List<String> getDirectServiceInsts() {
        return directServiceInsts;
    }

    public void setDirectServiceInsts(List<String> directServiceInsts) {
        this.directServiceInsts = directServiceInsts;
    }

    public List<EMIChannelInfoBiz> getEmiHybridChannelInfos() {
        return emiHybridChannelInfos;
    }

    public void setEmiHybridChannelInfos(List<EMIChannelInfoBiz> emiHybridChannelInfos) {
        this.emiHybridChannelInfos = emiHybridChannelInfos;
    }

    public String getPayOption() {
        return payOption;
    }

    public void setPayOption(final String payOption) {
        this.payOption = payOption;
    }

    public boolean isEnableStatus() {
        return enableStatus;
    }

    public void setEnableStatus(final boolean enableStatus) {
        this.enableStatus = enableStatus;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(final String disableReason) {
        this.disableReason = disableReason;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(final String instId) {
        this.instId = instId;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(final String instName) {
        this.instName = instName;
    }

    public List<BalanceChannelInfoBiz> getBalanceChannelInfos() {
        return balanceChannelInfos != null ? balanceChannelInfos : Collections.emptyList();
    }

    public void setBalanceChannelInfos(final List<BalanceChannelInfoBiz> balanceChannelInfos) {
        this.balanceChannelInfos = balanceChannelInfos;
    }

    public List<EMIChannelInfoBiz> getEmiChannelInfos() {
        return emiChannelInfos != null ? emiChannelInfos : Collections.emptyList();
    }

    public void setEmiChannelInfos(final List<EMIChannelInfoBiz> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }

    public List<String> getSupportCountries() {
        return supportCountries;
    }

    public void setSupportCountries(List<String> supportCountries) {
        this.supportCountries = supportCountries;
    }

    public List<String> getSupportAtmPins() {
        return supportAtmPins;
    }

    public void setSupportAtmPins(List<String> supportAtmPins) {
        this.supportAtmPins = supportAtmPins;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<String> getDccServiceInstIds() {
        return dccServiceInstIds;
    }

    public void setDccServiceInstIds(List<String> dccServiceInstIds) {
        this.dccServiceInstIds = dccServiceInstIds;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PayChannelOptionViewBiz [payOption=").append(payOption).append(", enableStatus=")
                .append(enableStatus).append(", disableReason=").append(disableReason).append(", instId=")
                .append(instId).append(", instName=").append(instName).append(", balanceChannelInfos=")
                .append(balanceChannelInfos).append(", emiChannelInfos=").append(emiChannelInfos)
                .append(", emiHybridChannelInfos=").append(emiHybridChannelInfos).append(", supportCountries=")
                .append(supportCountries).append(", extendInfo=").append(extendInfo).append(", directServiceInsts=")
                .append(directServiceInsts).append(", externalAccountInfos=").append(externalAccountInfos)
                .append(", supportAtmPins=").append(supportAtmPins).append(", templateId=").append(templateId)
                .append(", supportPrepaidCard").append(prepaidCardChannel).append(", supportPayOptionSubTypes=")
                .append(supportPayOptionSubTypes).append(", dccServiceInstIds=").append(dccServiceInstIds)
                .append(",blockPeriodInSeconds").append(blockPeriodInSeconds).append(",maxBlockAmount")
                .append(maxBlockAmount).append(",excessCapturePercentage").append(excessCapturePercentage)
                .append(",payConfirmFlowType").append(payConfirmFlowType).append(",instDispCode").append(instDispCode)
                .append("]");
        return builder.toString();
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * @param extendInfo
     *            the extendInfo to set
     */
    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isPrepaidCardChannel() {
        return prepaidCardChannel;
    }

    public void setPrepaidCardChannel(boolean prepaidCardChannel) {
        this.prepaidCardChannel = prepaidCardChannel;
    }

    public List<String> getSupportPayOptionSubTypes() {
        return supportPayOptionSubTypes;
    }

    public void setSupportPayOptionSubTypes(List<String> supportPayOptionSubTypes) {
        this.supportPayOptionSubTypes = supportPayOptionSubTypes;
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

    public Money getMaxBlockAmount() {
        return maxBlockAmount;
    }

    public void setMaxBlockAmount(Money maxBlockAmount) {
        this.maxBlockAmount = maxBlockAmount;
    }

    public Integer getExcessCapturePercentage() {
        return excessCapturePercentage;
    }

    public void setExcessCapturePercentage(Integer excessCapturePercentage) {
        this.excessCapturePercentage = excessCapturePercentage;
    }

    public String getInstDispCode() {
        return instDispCode;
    }

    public void setInstDispCode(String instDispCode) {
        this.instDispCode = instDispCode;
    }
}