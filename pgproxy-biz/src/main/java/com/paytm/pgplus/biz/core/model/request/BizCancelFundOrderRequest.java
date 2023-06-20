package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

/**
 * @author vivek
 *
 */
public class BizCancelFundOrderRequest implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2380520040463092293L;
    private String fundOrderId;
    private EnvInfoRequestBean envInfo;
    private String closeReason;
    private String paytmMerchantId;
    private Routes route;

    public BizCancelFundOrderRequest(String fundOrderId, EnvInfoRequestBean envInfo) {
        this.fundOrderId = fundOrderId;
        this.envInfo = envInfo;
    }

    public BizCancelFundOrderRequest(String fundOrderId, EnvInfoRequestBean envInfo, String closeReason) {
        this.fundOrderId = fundOrderId;
        this.envInfo = envInfo;
        this.closeReason = closeReason;
    }

    public String getFundOrderId() {
        return fundOrderId;
    }

    public EnvInfoRequestBean getEnvInfo() {
        return envInfo;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }
}
