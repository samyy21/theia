package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by charu on 03/06/18.
 */

public class ChannelAccountQueryRequestBizBean implements Serializable {

    private static final long serialVersionUID = -4324569386964985882L;

    private String merchantId;
    private String userId;
    private List<PayMethodInfo> payMethodInfos;
    private EnvInfoRequestBean envInfoRequestBean;
    private ERequestType requestType;

    public ChannelAccountQueryRequestBizBean(String merchantId, String userId, List<PayMethodInfo> payMethodInfos,
            EnvInfoRequestBean envInfoRequestBean, ERequestType requestType) {
        this.merchantId = merchantId;
        this.userId = userId;
        this.payMethodInfos = payMethodInfos;
        this.envInfoRequestBean = envInfoRequestBean;
        this.requestType = requestType;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PayMethodInfo> getPayMethodInfos() {
        return payMethodInfos;
    }

    public void setPayMethodInfos(List<PayMethodInfo> payMethodInfos) {
        this.payMethodInfos = payMethodInfos;
    }

    public EnvInfoRequestBean getEnvInfoRequestBean() {
        return envInfoRequestBean;
    }

    public void setEnvInfoRequestBean(EnvInfoRequestBean envInfoRequestBean) {
        this.envInfoRequestBean = envInfoRequestBean;
    }

    public ERequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ERequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelAccountQueryRequestBizBean{");
        sb.append("merchantId='").append(merchantId).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", payMethodInfos=").append(payMethodInfos);
        sb.append(", envInfoRequestBean=").append(envInfoRequestBean);
        sb.append(", requestType=").append(requestType);
        sb.append('}');
        return sb.toString();
    }
}
