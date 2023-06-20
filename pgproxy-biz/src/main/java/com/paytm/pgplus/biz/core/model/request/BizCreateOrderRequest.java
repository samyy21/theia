/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.biz.enums.ECurrency;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.acquiring.models.SplitCommandInfo;
import com.paytm.pgplus.facade.acquiring.models.TimeoutConfigRule;
import com.paytm.pgplus.facade.edcpg2.acquiring.model.SplitType;
import com.paytm.pgplus.models.OrderPricingInfo;
import com.paytm.pgplus.enums.SplitTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author namanjain
 *
 */
public class BizCreateOrderRequest implements Serializable {

    private static final long serialVersionUID = 9L;

    private String internalUserId;
    private String externalUserId;
    private String internalMerchantId;
    private BigDecimal orderAmount;
    private String orderDescription;
    private String orderId;
    private Date creationDate;
    private Long orderTimeoutInMilliseconds;
    private String industryCode;
    private String paytmNotificationUrl;
    private String tokenId;
    private String clientIp;
    private String osType;
    private EChannelId channel;
    private final ECurrency currency = ECurrency.INR;
    private ETransType transactionType;
    private ExtendedInfoRequestBean extendInfo;
    private ERequestType requestType;
    private boolean isPostConvenienceFee;
    private String goodsInfo;
    private String shippingInfo;
    private String riskToken;
    private EnvInfoRequestBean envInfoRequestBean;
    private boolean slabBasedMDR;
    private String paymentPendingTimeout;
    private boolean fromAoaMerchant;
    private List<TimeoutConfigRule> timeoutConfigRuleList;
    private OrderPricingInfo orderPricingInfo;
    private boolean dynamicFeeMerchant;
    private List<SplitCommandInfo> splitCommandInfoList;
    private Map<String, String> emiDetailInfo;
    private SplitTypeEnum splitType;

    /**
     * Added to check whether it is initiate txn request or not
     */

    private boolean createOrderForInitiateTxnRequest;

    private Map<String, String> additionalOrderExtendInfo;

    private boolean defaultDynamicFeeMerchantPayment;

    private String firstName;

    private boolean isDealsTransaction;

    public boolean isDealsTransaction() {
        return isDealsTransaction;
    }

    public void setDealsTransaction(boolean dealsTransaction) {
        isDealsTransaction = dealsTransaction;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the internalUserId
     */
    public String getInternalUserId() {
        return internalUserId;
    }

    /**
     * @param internalUserId
     *            the internalUserId to set
     */
    public void setInternalUserId(final String internalUserId) {
        this.internalUserId = internalUserId;
    }

    /**
     * @return the externalUserId
     */
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * @param externalUserId
     *            the externalUserId to set
     */
    public void setExternalUserId(final String externalUserId) {
        this.externalUserId = externalUserId;
    }

    /**
     * @return the internalMerchantId
     */
    public String getInternalMerchantId() {
        return internalMerchantId;
    }

    /**
     * @param internalMerchantId
     *            the internalMerchantId to set
     */
    public void setInternalMerchantId(final String internalMerchantId) {
        this.internalMerchantId = internalMerchantId;
    }

    /**
     * @return the orderAmount
     */
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    /**
     * @param orderAmount
     *            the orderAmount to set
     */
    public void setOrderAmount(final BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    /**
     * @return the orderDescription
     */
    public String getOrderDescription() {
        return orderDescription;
    }

    /**
     * @param orderDescription
     *            the orderDescription to set
     */
    public void setOrderDescription(final String orderDescription) {
        this.orderDescription = orderDescription;
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId
     *            the orderId to set
     */
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the orderTimeoutInMilliseconds
     */
    public Long getOrderTimeoutInMilliseconds() {
        return orderTimeoutInMilliseconds;
    }

    /**
     * @param orderTimeoutInMilliseconds
     *            the orderTimeoutInMilliseconds to set
     */
    public void setOrderTimeoutInMilliseconds(final Long orderTimeoutInMilliseconds) {
        this.orderTimeoutInMilliseconds = orderTimeoutInMilliseconds;
    }

    /**
     * @return the industryCode
     */
    public String getIndustryCode() {
        return industryCode;
    }

    /**
     * @param industryCode
     *            the industryCode to set
     */
    public void setIndustryCode(final String industryCode) {
        this.industryCode = industryCode;
    }

    /**
     * @return the paytmNotificationUrl
     */
    public String getPaytmNotificationUrl() {
        return paytmNotificationUrl;
    }

    /**
     * @param paytmNotificationUrl
     *            the paytmNotificationUrl to set
     */
    public void setPaytmNotificationUrl(final String paytmNotificationUrl) {
        this.paytmNotificationUrl = paytmNotificationUrl;
    }

    /**
     * @return the tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * @param tokenId
     *            the tokenId to set
     */
    public void setTokenId(final String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * @return the clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @param clientIp
     *            the clientIp to set
     */
    public void setClientIp(final String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * @return the osType
     */
    public String getOsType() {
        return osType;
    }

    /**
     * @param osType
     *            the osType to set
     */
    public void setOsType(final String osType) {
        this.osType = osType;
    }

    /**
     * @return the channel
     */
    public EChannelId getChannel() {
        return channel;
    }

    /**
     * @param channel
     *            the channel to set
     */
    public void setChannel(final EChannelId channel) {
        this.channel = channel;
    }

    /**
     * @return the currency
     */
    public ECurrency getCurrency() {
        return currency;
    }

    public List<TimeoutConfigRule> getTimeoutConfigRuleList() {
        return timeoutConfigRuleList;
    }

    public void setTimeoutConfigRuleList(List<TimeoutConfigRule> timeoutConfigRuleList) {
        this.timeoutConfigRuleList = timeoutConfigRuleList;
    }

    public OrderPricingInfo getOrderPricingInfo() {
        return orderPricingInfo;
    }

    public void setOrderPricingInfo(OrderPricingInfo orderPricingInfo) {
        this.orderPricingInfo = orderPricingInfo;
    }

    public BizCreateOrderRequest(final String externalUserId, final String internalMerchantId,
            final BigDecimal orderAmount, final String orderId, final Long orderTimeoutInMilliseconds,
            final String industryCode, final String clientIp, final EChannelId channel, final ETransType transType,
            final ExtendedInfoRequestBean extendInfo) {
        this.externalUserId = externalUserId;
        this.internalMerchantId = internalMerchantId;
        this.orderAmount = orderAmount;
        this.orderId = orderId;
        this.orderTimeoutInMilliseconds = orderTimeoutInMilliseconds;
        this.industryCode = industryCode;
        this.clientIp = clientIp;
        this.channel = channel;
        this.orderDescription = orderId;
        this.transactionType = transType;
        this.extendInfo = extendInfo;
    }

    public BizCreateOrderRequest() {
    }

    public ETransType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final ETransType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * @return the extendInfo
     */
    public ExtendedInfoRequestBean getExtendInfo() {
        return extendInfo;
    }

    /**
     * @param extendInfo
     *            the extendInfo to set
     */
    public void setExtendInfo(final ExtendedInfoRequestBean extendInfo) {
        this.extendInfo = extendInfo;
    }

    /**
     * @return the requestType
     */
    public ERequestType getRequestType() {
        return requestType;
    }

    /**
     * @param requestType
     *            the requestType to set
     */
    public void setRequestType(final ERequestType requestType) {
        this.requestType = requestType;
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

    public String getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(String goodsInfo) {
        this.goodsInfo = goodsInfo;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public String getRiskToken() {
        return riskToken;
    }

    public void setRiskToken(String riskToken) {
        this.riskToken = riskToken;
    }

    /**
     * @return the envInfoRequestBean
     */
    public EnvInfoRequestBean getEnvInfoRequestBean() {
        return envInfoRequestBean;
    }

    /**
     * @param envInfoRequestBean
     *            the envInfoRequestBean to set
     */
    public void setEnvInfoRequestBean(EnvInfoRequestBean envInfoRequestBean) {
        this.envInfoRequestBean = envInfoRequestBean;
    }

    public boolean isSlabBasedMDR() {
        return slabBasedMDR;
    }

    public void setSlabBasedMDR(boolean slabBasedMDR) {
        this.slabBasedMDR = slabBasedMDR;
    }

    public String getPaymentPendingTimeout() {
        return paymentPendingTimeout;
    }

    public void setPaymentPendingTimeout(String paymentPendingTimeout) {
        this.paymentPendingTimeout = paymentPendingTimeout;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public boolean isCreateOrderForInitiateTxnRequest() {
        return createOrderForInitiateTxnRequest;
    }

    public void setCreateOrderForInitiateTxnRequest(boolean createOrderForInitiateTxnRequest) {
        this.createOrderForInitiateTxnRequest = createOrderForInitiateTxnRequest;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public List<SplitCommandInfo> getSplitCommandInfoList() {
        return splitCommandInfoList;
    }

    public void setSplitCommandInfoList(List<SplitCommandInfo> splitCommandInfoList) {
        this.splitCommandInfoList = splitCommandInfoList;
    }

    public Map<String, String> getAdditionalOrderExtendInfo() {
        return additionalOrderExtendInfo;
    }

    public void setAdditionalOrderExtendInfo(Map<String, String> additionalOrderExtendInfo) {
        this.additionalOrderExtendInfo = additionalOrderExtendInfo;
    }

    public boolean isDefaultDynamicFeeMerchantPayment() {
        return defaultDynamicFeeMerchantPayment;
    }

    public void setDefaultDynamicFeeMerchantPayment(boolean defaultDynamicFeeMerchantPayment) {
        this.defaultDynamicFeeMerchantPayment = defaultDynamicFeeMerchantPayment;
    }

    public Map<String, String> getEmiDetailInfo() {
        return emiDetailInfo;
    }

    public void setEmiDetailInfo(Map<String, String> emiDetailInfo) {
        this.emiDetailInfo = emiDetailInfo;
    }

    public SplitTypeEnum getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitTypeEnum splitType) {
        this.splitType = splitType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BizCreateOrderRequest [internalUserId=").append(internalUserId).append(", externalUserId=")
                .append(externalUserId).append(", internalMerchantId=").append(internalMerchantId)
                .append(", orderAmount=").append(orderAmount).append(", orderDescription=").append(orderDescription)
                .append(", orderId=").append(orderId).append(", creationDate=").append(creationDate)
                .append(", orderTimeoutInMilliseconds=").append(orderTimeoutInMilliseconds).append(", industryCode=")
                .append(industryCode).append(", paytmNotificationUrl=").append(paytmNotificationUrl)
                .append(", tokenId=").append(tokenId).append(", clientIp=").append(clientIp).append(", osType=")
                .append(osType).append(", channel=").append(channel).append(", currency=").append(currency)
                .append(", transactionType=").append(transactionType).append(", extendInfo=").append(extendInfo)
                .append(", requestType=").append(requestType).append(", isPostConvenienceFee=")
                .append(isPostConvenienceFee).append(", goodsInfo=").append(goodsInfo).append(", shippingInfo=")
                .append(shippingInfo).append(", riskToken=").append(riskToken).append(", envInfoRequestBean=")
                .append(envInfoRequestBean).append(", slabBasedMDR=").append(slabBasedMDR)
                .append(", paymentPendingTimeout=").append(paymentPendingTimeout).append(",fromAoaMerchant=")
                .append(fromAoaMerchant).append("timeoutConfigRuleList").append(timeoutConfigRuleList)
                .append("dynamicFeeMerchant").append(dynamicFeeMerchant).append("splitType").append(splitType)
                .append("]");
        return builder.toString();
    }

}