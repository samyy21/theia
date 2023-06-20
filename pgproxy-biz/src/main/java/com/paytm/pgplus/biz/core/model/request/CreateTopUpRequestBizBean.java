package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

import com.paytm.pgplus.biz.enums.EFundType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;

/**
 * @author namanjain
 *
 */
public class CreateTopUpRequestBizBean implements Serializable {

    private static final long serialVersionUID = -6281654029466892249L;

    private String txnAmount;

    private String currency;

    private EFundType fundType;

    private String payerUserId;

    private String requestId; // Merchant OredrID

    private String notificationUrl; // From Prop file

    private String merchantId; // From GenericMapping

    private EnvInfoRequestBean envInfoBean;

    private ExtendedInfoRequestBean extInfoReqBean;

    /**
     * @return
     */
    public ExtendedInfoRequestBean getExtInfoReqBean() {
        return extInfoReqBean;
    }

    /**
     * @param extInfoReqBean
     */
    public void setExtInfoReqBean(ExtendedInfoRequestBean extInfoReqBean) {
        this.extInfoReqBean = extInfoReqBean;
    }

    public String getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(String payerUserId) {
        this.payerUserId = payerUserId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public EFundType getFundType() {
        return fundType;
    }

    public void setFundType(EFundType fundType) {
        this.fundType = fundType;
    }

    public EnvInfoRequestBean getEnvInfoBean() {
        return envInfoBean;
    }

    public void setEnvInfoBean(EnvInfoRequestBean envInfoBean) {
        this.envInfoBean = envInfoBean;
    }

    public CreateTopUpRequestBizBean(String txnAmount, String currency, EFundType fundType, String payerUserId,
            ExtendedInfoRequestBean extInfoReqBean, String requestId, String notificationUrl, String merchantId,
            EnvInfoRequestBean envInfoBean) {
        this.txnAmount = txnAmount;
        this.currency = currency;
        this.fundType = fundType;
        this.payerUserId = payerUserId;
        this.extInfoReqBean = extInfoReqBean;
        this.requestId = requestId;
        this.notificationUrl = notificationUrl;
        this.merchantId = merchantId;
        this.envInfoBean = envInfoBean;
    }
}
