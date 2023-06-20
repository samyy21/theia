package com.paytm.pgplus.theia.offline.model.payview;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiChannel extends Bank {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3911751255480031301L;
    private List<EMIChannelInfo> emiChannelInfos;
    private List<EMIChannelInfo> emiHybridChannelInfos;

    public EmiChannel() {
    }

    public EmiChannel(List<EMIChannelInfo> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }

    public List<EMIChannelInfo> getEmiChannelInfos() {
        return emiChannelInfos;
    }

    public void setEmiChannelInfos(List<EMIChannelInfo> emiChannelInfo) {
        this.emiChannelInfos = emiChannelInfo;
    }

    public void setEmiHybridChannelInfos(List<EMIChannelInfo> emiHybridChannelInfos) {
        this.emiHybridChannelInfos = emiHybridChannelInfos;
    }

    public List<EMIChannelInfo> getEmiHybridChannelInfos() {
        return emiHybridChannelInfos;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EmiChannel [emiChannelInfos=");
        builder.append(emiChannelInfos);
        builder.append(", emiHybridChannelInfos=");
        builder.append(emiHybridChannelInfos);
        builder.append("]");
        return builder.toString();
    }
}
