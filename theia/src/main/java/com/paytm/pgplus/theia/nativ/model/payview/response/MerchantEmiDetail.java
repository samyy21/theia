package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.enums.EmiType;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantEmiDetail implements Serializable {

    private static final long serialVersionUID = 2413229384943829540L;

    private String channelCode;
    private String channelName;
    private String emiType;
    private boolean isMultiItemEmiSupported;
    private String iconUrl;
    private List<MerchantEMIChannelInfo> emiChannelInfos;

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public boolean isMultiItemEmiSupported() {
        return isMultiItemEmiSupported;
    }

    public void setMultiItemEmiSupported(boolean multiItemEmiSupported) {
        isMultiItemEmiSupported = multiItemEmiSupported;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<MerchantEMIChannelInfo> getEmiChannelInfos() {
        return emiChannelInfos;
    }

    public void setEmiChannelInfos(List<MerchantEMIChannelInfo> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
