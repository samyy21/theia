package com.paytm.pgplus.biz.core.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.common.model.link.UserFormInput;
import com.paytm.pgplus.facade.merchant.models.AddressInfo;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.facade.constants.FacadeConstants.ExtendInfo.PLATFORM_FALLBACK_DISABLED;

/**
 * @author namanjain
 */
public class ExtendedInfoRequestBean implements Serializable {

    private static final long serialVersionUID = -3619019898097233808L;

    private String callBackURL;
    private String peonURL;
    private String responseCode;
    private String theme;
    private String clientIP;
    private String website;
    private String promoCode;
    private String email;
    private String phoneNo;
    private String promoResponseCode;
    private String merchantUniqueReference;

    private String merchantTransId;
    private String paytmMerchantId;
    private String alipayMerchantId;
    private String ssoToken;
    private String mccCode;
    private String requestType;
    private String virtualPaymentAddr;

    /*
     * Params added while Running SubscriptionRenwal Flow
     */

    private String savedCardId;
    private String txnType;
    private String productCode;
    private String totalTxnAmount;

    private String subscriptionId;
    private String currentSubscriptionId;
    private String subsExpiryDate;
    private String payerUserID;
    private String payerAccountNumber;
    private String custID;
    private String orderID;

    @JsonProperty(value = "SUBS_PPI_ONLY")
    private String subsPPIOnly;

    @JsonProperty(value = "SUBS_PAY_MODE")
    private String subsPayMode;

    private String subsCappedAmount;
    private String subscriptionType;
    private String amountToBeRefunded;
    private String userEmail;
    private String userMobile;
    private String merchantName;
    private String subsFreq;
    private String subsFreqUnit;

    // Paytm Express Add Money Specific
    private String issuingBankName;
    private String issuingBankId;
    private String retryCount;
    private String emiPlanId;
    private boolean topupAndPay;

    @JsonProperty(value = "PAYTM_USER_ID")
    private String paytmUserId;

    // CC Bill Payment specific
    @JsonProperty(value = "CC_BILL_NO")
    private String creditCardBillNo;
    private String successCallBackURL;
    private String failureCallBackURL;
    private String pendingCallBackURL;

    private transient Map<String, Object> extraParamsMap;
    private String udf1;
    private String udf2;
    private String udf3;
    private String additionalInfo;

    // Link Based Specific
    private String linkDescription;
    private String linkName;

    /*
     * Parameters to check if merchant limit was updated at terminal state of
     * txn
     */
    private boolean merchantLimitEnabled;
    private boolean merchantLimitUpdated;

    private AddressInfo merchantAddress;
    private String merchantPhone;

    private boolean linkBasedInvoicePayment;

    private String qrDisplayName;

    private String qrCodeId;
    private String qrDeeplink;

    private String merchantCategory;
    private String merchantSubCategory;

    private String comments;
    private String maskedCustomerMobileNumber;
    private String posId;
    private String uniqueReferenceLabel;
    private String uniqueReferenceValue;
    private String pccCode;
    private String prn;
    /* Parameter to support subscription ofus merchants */
    private String subsServiceId;

    /* Parameter to support Loyalty Point Paymode */
    private String exchangeRate;
    private String pointNumber;
    private String promoApplyResultStatus;

    /* Parameter for native CARD_INDEX_NO */
    private String cardIndexNo;

    private String subWalletOrderAmountDetails;
    private String subwalletWithdrawMaxAmountDetails;
    private String mutualFundFeedInfo;

    private String clientId;

    private boolean isEnhancedNative;

    @JsonProperty(value = "CUST_ID")
    private String customerId;
    private String virtualAccountNo;
    private String templateId;
    private String templateName;

    private String targetPhoneNo;
    private String addMoneyDestination;

    private boolean merchantOnPaytm;
    // For Postpaid new JWT Token for Insta
    private String flowType;

    private String isMerchantLimitEnabledForPay;
    private String isMerchantLimitUpdatedForPay;

    private boolean offlineFlow;
    private String paymentPromoCheckoutData;

    private String paymentRequestFlow;

    private boolean linkBasedNonInvoicePayment;

    private String merchantKybId;

    private String merchantContactNo;

    @JsonProperty("SPLIT_PAY_INFO")
    private String splitPayInfo;

    private String splitSettlementInfo;

    private String payerDeviceId;

    private boolean autoRenewal;

    private boolean autoRetry;

    private boolean communicationManager;

    private boolean subsRenewOrderAlreadyCreated;

    private int graceDays;

    private String aggregatorMid;

    private String emiSubventionInfo;

    private String emiSubventionOrderList;

    private List<UserFormInput> paymentForm;

    private String linkNotes;

    private String customerName;

    private String workFlow;

    private String guestToken;

    private Map<String, String> orderAdditionalInfo;

    private String paymentForms;

    /* Parameters for MLV */
    private String aggType;
    private String logoURL;

    private String cardHash;

    private boolean cardTokenRequired;

    private boolean preDebitRenewal;

    private String payableAmount;

    private String search1;
    private String resellerName;

    private String payerName;
    private String payerPSP;
    private String merchantLinkRefId;

    private String paymentPromoCheckoutDataPromoCode;

    private String search2;
    private String vanIfsc;

    /*
     * eComToken extendInfo parameters
     */
    private String firstSixDigits;
    private String lastFourDigits;
    private String payRequestIssuingBank;
    private String networkTokenRequestorId;
    private String sdkType;
    private String subsLinkInfo;

    private String search3;
    private String actualMid;
    private String actualOrderId;
    private String dummyOrderId;
    private String dummyMerchantId;
    private String dummyAlipayMid;

    private String internationalCardPayment;

    private boolean checkoutJsAppInvokePayment;
    private boolean pushDataToDynamicQR;
    private String upiToAddnPayPromoCode;
    /**
     * LIC Related
     */
    private String ptInfo1;
    private String typeOfPayment;
    private String paymodeIdentifier;

    private String paymentMode;
    private String bankId;
    private String source;
    private Boolean prepaidCard;
    private Boolean corporateCard;
    private String binNumber;
    private String comment;

    private boolean fromAoaMerchant;

    @JsonProperty(value = "TXN_TOKEN")
    private String txnToken;

    @JsonProperty(value = "ROUTE")
    private Routes route;

    @JsonProperty(value = BizConstant.ExtendedInfoKeys.LPV_ROUTE)
    private Routes lpvRoute;

    private Boolean edcLinkTxn;
    private String validationModelInfo;
    private String validationMode;
    private String brandInvoiceNumber;
    private Boolean validationSkipFlag;
    private String emiType;
    private boolean aoaSubsOnPgMid;
    @JsonProperty(value = PLATFORM_FALLBACK_DISABLED)
    private boolean fullPg2TrafficEnabled;

    // EDC Link parameters
    private String paytmTid;
    private String brandName;
    private String brandId;
    private String categoryName;
    private String categoryId;
    private String productId;
    private String productName;
    private String imeiKey;
    private String imei;
    private String serialNo;
    private String emiInterestRate;
    private String emiTotalAmount;
    private String brandEmiAmount;
    private String emiTenure;
    private String bankOffer;
    private Boolean isSubventionCreated;
    private Boolean isBankOfferApplied;

    private String cobrandedCustomDisplayName;

    private String pplusUserId;

    private String binIrcId;

    private String billAmount;

    @JsonProperty("REQUEST_TYPE")
    private String flowRequestType;

    public String getFlowRequestType() {
        return flowRequestType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String emiCategory;

    // flag to identify HDFC digipos links
    private Boolean isHDFCDigiPOSMerchant;

    @JsonProperty("3PconvFeeAmount")
    private String threePconvFeeAmount;

    @JsonProperty("is3PconvFee")
    private String threePconvFee;

    public void setFlowRequestType(String flowRequestType) {
        this.flowRequestType = flowRequestType;
    }

    public void setBillAmount(String billAmount) {
        this.billAmount = billAmount;
    }

    public String getBillAmount() {
        return billAmount;
    }

    public String getBinIrcId() {
        return binIrcId;
    }

    public void setBinIrcId(String binIrcId) {
        this.binIrcId = binIrcId;
    }

    private String acquiringType;

    public void setAcquiringType(String acquiringType) {
        this.acquiringType = acquiringType;
    }

    public String getPaytmTid() {
        return paytmTid;
    }

    public void setPaytmTid(String paytmTid) {
        this.paytmTid = paytmTid;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImeiKey() {
        return imeiKey;
    }

    public void setImeiKey(String imeiKey) {
        this.imeiKey = imeiKey;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getEmiInterestRate() {
        return emiInterestRate;
    }

    public void setEmiInterestRate(String emiInterestRate) {
        this.emiInterestRate = emiInterestRate;
    }

    public String getEmiTotalAmount() {
        return emiTotalAmount;
    }

    public void setEmiTotalAmount(String emiTotalAmount) {
        this.emiTotalAmount = emiTotalAmount;
    }

    public String getBrandEmiAmount() {
        return brandEmiAmount;
    }

    public void setBrandEmiAmount(String brandEmiAmount) {
        this.brandEmiAmount = brandEmiAmount;
    }

    public String getEmiTenure() {
        return emiTenure;
    }

    public void setEmiTenure(String emiTenure) {
        this.emiTenure = emiTenure;
    }

    public String getBankOffer() {
        return bankOffer;
    }

    public void setBankOffer(String bankOffer) {
        this.bankOffer = bankOffer;
    }

    public void setFullPg2TrafficEnabled(boolean fullPg2TrafficEnabled) {
        this.fullPg2TrafficEnabled = fullPg2TrafficEnabled;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public Map<String, String> getOrderAdditionalInfo() {
        return orderAdditionalInfo;
    }

    public void setOrderAdditionalInfo(Map<String, String> orderAdditionalInfo) {
        this.orderAdditionalInfo = orderAdditionalInfo;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getAggregatorMid() {
        return aggregatorMid;
    }

    public void setAggregatorMid(String aggregatorMid) {
        this.aggregatorMid = aggregatorMid;
    }

    public String getPaymentRequestFlow() {
        return paymentRequestFlow;
    }

    public void setPaymentRequestFlow(String paymentRequestFlow) {
        this.paymentRequestFlow = paymentRequestFlow;
    }

    // JsonString
    public String getPaymentPromoCheckoutData() {
        return paymentPromoCheckoutData;
    }

    public void setPaymentPromoCheckoutData(String paymentPromoCheckoutData) {
        this.paymentPromoCheckoutData = paymentPromoCheckoutData;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    public boolean isEnhancedNative() {
        return isEnhancedNative;
    }

    public void setEnhancedNative(boolean enhancedNative) {
        isEnhancedNative = enhancedNative;
    }

    public String getSubwalletWithdrawMaxAmountDetails() {
        return subwalletWithdrawMaxAmountDetails;
    }

    public void setSubwalletWithdrawMaxAmountDetails(String subwalletWithdrawMaxAmountDetails) {
        this.subwalletWithdrawMaxAmountDetails = subwalletWithdrawMaxAmountDetails;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMaskedCustomerMobileNumber() {
        return maskedCustomerMobileNumber;
    }

    public void setMaskedCustomerMobileNumber(String maskedCustomerMobileNumber) {
        this.maskedCustomerMobileNumber = maskedCustomerMobileNumber;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getUniqueReferenceLabel() {
        return uniqueReferenceLabel;
    }

    public void setUniqueReferenceLabel(String uniqueReferenceLabel) {
        this.uniqueReferenceLabel = uniqueReferenceLabel;
    }

    public String getUniqueReferenceValue() {
        return uniqueReferenceValue;
    }

    public void setUniqueReferenceValue(String uniqueReferenceValue) {
        this.uniqueReferenceValue = uniqueReferenceValue;
    }

    public String getPccCode() {
        return pccCode;
    }

    public void setPccCode(String pccCode) {
        this.pccCode = pccCode;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String getPaytmUserId() {
        return paytmUserId;
    }

    public void setPaytmUserId(String paytmUserId) {
        this.paytmUserId = paytmUserId;
    }

    public String getIssuingBankName() {
        return issuingBankName;
    }

    public void setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
    }

    public String getIssuingBankId() {
        return issuingBankId;
    }

    public void setIssuingBankId(String issuingBankId) {
        this.issuingBankId = issuingBankId;
    }

    public String getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(String retryCount) {
        this.retryCount = retryCount;
    }

    public String getEmiPlanId() {
        return emiPlanId;
    }

    public void setEmiPlanId(String emiPlanId) {
        this.emiPlanId = emiPlanId;
    }

    public boolean isTopupAndPay() {
        return topupAndPay;
    }

    public void setTopupAndPay(boolean topupAndPay) {
        this.topupAndPay = topupAndPay;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getTotalTxnAmount() {
        return totalTxnAmount;
    }

    public void setTotalTxnAmount(String totalTxnAmount) {
        this.totalTxnAmount = totalTxnAmount;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubsExpiryDate() {
        return subsExpiryDate;
    }

    public void setSubsExpiryDate(String subsExpiryDate) {
        this.subsExpiryDate = subsExpiryDate;
    }

    public String getPayerUserID() {
        return payerUserID;
    }

    public void setPayerUserID(String payerUserID) {
        this.payerUserID = payerUserID;
    }

    public String getPayerAccountNumber() {
        return payerAccountNumber;
    }

    public void setPayerAccountNumber(String payerAccountNumber) {
        this.payerAccountNumber = payerAccountNumber;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
        this.customerId = custID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSubsPPIOnly() {
        return subsPPIOnly;
    }

    public void setSubsPPIOnly(String subsPPIOnly) {
        this.subsPPIOnly = subsPPIOnly;
    }

    public String getSubsPayMode() {
        return subsPayMode;
    }

    public void setSubsPayMode(String subsPayMode) {
        this.subsPayMode = subsPayMode;
    }

    public String getSubsCappedAmount() {
        return subsCappedAmount;
    }

    public void setSubsCappedAmount(String subsCappedAmount) {
        this.subsCappedAmount = subsCappedAmount;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getVirtualPaymentAddr() {
        return virtualPaymentAddr;
    }

    public void setVirtualPaymentAddr(String virtualPaymentAddr) {
        this.virtualPaymentAddr = virtualPaymentAddr;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPromoResponseCode() {
        return promoResponseCode;
    }

    public void setPromoResponseCode(String promoResponseCode) {
        this.promoResponseCode = promoResponseCode;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getPeonURL() {
        return peonURL;
    }

    public void setPeonURL(String peonURL) {
        this.peonURL = peonURL;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    /**
     * @return the merchantUniqueReference
     */
    public String getMerchantUniqueReference() {
        return merchantUniqueReference;
    }

    /**
     * @param merchantUniqueReference
     *            the merchantUniqueReference to set
     */
    public void setMerchantUniqueReference(String merchantUniqueReference) {
        this.merchantUniqueReference = merchantUniqueReference;
    }

    public String getMerchantTransId() {
        return merchantTransId;
    }

    public void setMerchantTransId(String merchantTransId) {
        this.merchantTransId = merchantTransId;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public String getAlipayMerchantId() {
        return alipayMerchantId;
    }

    public void setAlipayMerchantId(String alipayMerchantId) {
        this.alipayMerchantId = alipayMerchantId;
    }

    @JsonIgnore
    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getMccCode() {
        return mccCode;
    }

    public void setMccCode(String mccCode) {
        this.mccCode = mccCode;
    }

    /**
     * @return the currentSubscriptionId
     */
    public String getCurrentSubscriptionId() {
        return currentSubscriptionId;
    }

    /**
     * @param currentSubscriptionId
     *            the currentSubscriptionId to set
     */
    public void setCurrentSubscriptionId(String currentSubscriptionId) {
        this.currentSubscriptionId = currentSubscriptionId;
    }

    /**
     * @return the amountToBeRefunded
     */
    public String getAmountToBeRefunded() {
        return amountToBeRefunded;
    }

    /**
     * @param amountToBeRefunded
     *            the amountToBeRefunded to set
     */
    public void setAmountToBeRefunded(String amountToBeRefunded) {
        this.amountToBeRefunded = amountToBeRefunded;
    }

    /**
     * @return the userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * @param userEmail
     *            the userEmail to set
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * @return the userMobile
     */
    public String getUserMobile() {
        return userMobile;
    }

    /**
     * @param userMobile
     *            the userMobile to set
     */
    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    /**
     * @return the merchantName
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * @param merchantName
     *            the merchantName to set
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    /**
     * @return the subsFreq
     */
    public String getSubsFreq() {
        return subsFreq;
    }

    /**
     * @param subsFreq
     *            the subsFreq to set
     */
    public void setSubsFreq(String subsFreq) {
        this.subsFreq = subsFreq;
    }

    /**
     * @return the subsFreqUnit
     */
    public String getSubsFreqUnit() {
        return subsFreqUnit;
    }

    /**
     * @param subsFreqUnit
     *            the subsFreqUnit to set
     */
    public void setSubsFreqUnit(String subsFreqUnit) {
        this.subsFreqUnit = subsFreqUnit;
    }

    public String getCreditCardBillNo() {
        return creditCardBillNo;
    }

    public void setCreditCardBillNo(String creditCardBillNo) {
        this.creditCardBillNo = creditCardBillNo;
    }

    public String getSuccessCallBackURL() {
        return successCallBackURL;
    }

    public void setSuccessCallBackURL(String successCallBackURL) {
        this.successCallBackURL = successCallBackURL;
    }

    public String getFailureCallBackURL() {
        return failureCallBackURL;
    }

    public void setFailureCallBackURL(String failureCallBackURL) {
        this.failureCallBackURL = failureCallBackURL;
    }

    public String getPendingCallBackURL() {
        return pendingCallBackURL;
    }

    public void setPendingCallBackURL(String pendingCallBackURL) {
        this.pendingCallBackURL = pendingCallBackURL;
    }

    public Map<String, Object> getExtraParamsMap() {

        return extraParamsMap == null ? Collections.emptyMap() : extraParamsMap;
    }

    public void setExtraParamsMap(Map<String, Object> extraParamsMap) {
        this.extraParamsMap = extraParamsMap;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getLinkDescription() {
        return linkDescription;
    }

    public void setLinkDescription(String linkDescription) {
        this.linkDescription = linkDescription;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public boolean isMerchantLimitEnabled() {
        return merchantLimitEnabled;
    }

    public void setMerchantLimitEnabled(boolean merchantLimitEnabled) {
        this.merchantLimitEnabled = merchantLimitEnabled;
    }

    public boolean isMerchantLimitUpdated() {
        return merchantLimitUpdated;
    }

    public void setMerchantLimitUpdated(boolean merchantLimitUpdated) {
        this.merchantLimitUpdated = merchantLimitUpdated;
    }

    public AddressInfo getMerchantAddress() {
        return merchantAddress;
    }

    public void setMerchantAddress(AddressInfo merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public String getMerchantPhone() {
        return merchantPhone;
    }

    public void setMerchantPhone(String merchantPhone) {
        this.merchantPhone = merchantPhone;
    }

    public String getQrDisplayName() {
        return qrDisplayName;
    }

    public void setQrDisplayName(String qrDisplayName) {
        this.qrDisplayName = qrDisplayName;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public void setQrDeeplink(String qrDeeplink) {
        this.qrDeeplink = qrDeeplink;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(String merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getMerchantSubCategory() {
        return merchantSubCategory;
    }

    public void setMerchantSubCategory(String merchantSubCategory) {
        this.merchantSubCategory = merchantSubCategory;
    }

    public boolean isLinkBasedInvoicePayment() {
        return linkBasedInvoicePayment;
    }

    public void setLinkBasedInvoicePayment(boolean linkBasedInvoicePayment) {
        this.linkBasedInvoicePayment = linkBasedInvoicePayment;
    }

    public String getSubsServiceId() {
        return subsServiceId;
    }

    public void setSubsServiceId(String subsServiceId) {
        this.subsServiceId = subsServiceId;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getPointNumber() {
        return pointNumber;
    }

    public void setPointNumber(String pointNumber) {
        this.pointNumber = pointNumber;
    }

    public String getPromoApplyResultStatus() {
        return promoApplyResultStatus;
    }

    public void setPromoApplyResultStatus(String promoApplyResultStatus) {
        this.promoApplyResultStatus = promoApplyResultStatus;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMutualFundFeedInfo() {
        return mutualFundFeedInfo;
    }

    public void setMutualFundFeedInfo(String mutualFundFeedInfo) {
        this.mutualFundFeedInfo = mutualFundFeedInfo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public boolean isMerchantOnPaytm() {
        return merchantOnPaytm;
    }

    public void setMerchantOnPaytm(boolean merchantOnPaytm) {
        this.merchantOnPaytm = merchantOnPaytm;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getIsMerchantLimitEnabledForPay() {
        return isMerchantLimitEnabledForPay;
    }

    public void setIsMerchantLimitEnabledForPay(String isMerchantLimitEnabledForPay) {
        this.isMerchantLimitEnabledForPay = isMerchantLimitEnabledForPay;
    }

    public String getIsMerchantLimitUpdatedForPay() {
        return isMerchantLimitUpdatedForPay;
    }

    public void setIsMerchantLimitUpdatedForPay(String isMerchantLimitUpdatedForPay) {
        this.isMerchantLimitUpdatedForPay = isMerchantLimitUpdatedForPay;

    }

    public String getVirtualAccountNo() {
        return virtualAccountNo;
    }

    public void setVirtualAccountNo(String virtualAccountNo) {
        this.virtualAccountNo = virtualAccountNo;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isOfflineFlow() {
        return offlineFlow;
    }

    public void setOfflineFlow(boolean offlineFlow) {
        this.offlineFlow = offlineFlow;
    }

    public boolean isLinkBasedNonInvoicePayment() {
        return linkBasedNonInvoicePayment;
    }

    public void setLinkBasedNonInvoicePayment(boolean linkBasedNonInvoicePayment) {
        this.linkBasedNonInvoicePayment = linkBasedNonInvoicePayment;
    }

    public String getMerchantKybId() {
        return merchantKybId;
    }

    public void setMerchantKybId(String merchantKybId) {
        this.merchantKybId = merchantKybId;
    }

    public boolean isAutoRenewal() {
        return autoRenewal;
    }

    public void setAutoRenewal(boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }

    public boolean isAutoRetry() {
        return autoRetry;
    }

    public void setAutoRetry(boolean autoRetry) {
        this.autoRetry = autoRetry;
    }

    public boolean isCommunicationManager() {
        return communicationManager;
    }

    public void setCommunicationManager(boolean communicationManager) {
        this.communicationManager = communicationManager;
    }

    public boolean isSubsRenewOrderAlreadyCreated() {
        return subsRenewOrderAlreadyCreated;
    }

    public void setSubsRenewOrderAlreadyCreated(boolean subsRenewOrderAlreadyCreated) {
        this.subsRenewOrderAlreadyCreated = subsRenewOrderAlreadyCreated;
    }

    public int getGraceDays() {
        return graceDays;
    }

    public void setGraceDays(int graceDays) {
        this.graceDays = graceDays;
    }

    public String getMerchantContactNo() {
        return merchantContactNo;
    }

    public void setMerchantContactNo(String merchantContactNo) {
        this.merchantContactNo = merchantContactNo;
    }

    public String getSplitPayInfo() {
        return splitPayInfo;
    }

    public void setSplitPayInfo(String splitPayInfo) {
        this.splitPayInfo = splitPayInfo;
    }

    public String getSplitSettlementInfo() {
        return splitSettlementInfo;
    }

    public void setSplitSettlementInfo(String splitSettlementInfo) {
        this.splitSettlementInfo = splitSettlementInfo;
    }

    public String getPayerDeviceId() {
        return payerDeviceId;
    }

    public void setPayerDeviceId(String payerDeviceId) {
        this.payerDeviceId = payerDeviceId;
    }

    public String getEmiSubventionInfo() {
        return emiSubventionInfo;
    }

    public void setEmiSubventionInfo(String emiSubventionInfo) {
        this.emiSubventionInfo = emiSubventionInfo;
    }

    public String getEmiSubventionOrderList() {
        return emiSubventionOrderList;
    }

    public void setEmiSubventionOrderList(String emiSubventionOrderList) {
        this.emiSubventionOrderList = emiSubventionOrderList;
    }

    public String getLinkNotes() {
        return linkNotes;
    }

    public void setLinkNotes(String linkNotes) {
        this.linkNotes = linkNotes;
    }

    public List<UserFormInput> getPaymentForm() {
        return paymentForm;
    }

    public void setPaymentForm(List<UserFormInput> paymentForm) {
        this.paymentForm = paymentForm;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public String getPaymentForms() {
        return paymentForms;
    }

    public void setPaymentForms(String paymentForms) {
        this.paymentForms = paymentForms;
    }

    public String getAggType() {
        return aggType;
    }

    public void setAggType(String aggType) {
        this.aggType = aggType;
    }

    public boolean isCardTokenRequired() {
        return cardTokenRequired;
    }

    public void setCardTokenRequired(boolean cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
    }

    public boolean isPreDebitRenewal() {
        return preDebitRenewal;
    }

    public void setPreDebitRenewal(boolean preDebitRenewal) {
        this.preDebitRenewal = preDebitRenewal;
    }

    public String getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(String payableAmount) {
        this.payableAmount = payableAmount;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerPSP() {
        return payerPSP;
    }

    public void setPayerPSP(String payerPSP) {
        this.payerPSP = payerPSP;
    }

    public String getSearch1() {
        return search1;
    }

    public void setSearch1(String search1) {
        this.search1 = search1;
    }

    public String getResellerName() {
        return resellerName;
    }

    public void setResellerName(String resellerName) {
        this.resellerName = resellerName;
    }

    public String getFirstSixDigits() {
        return firstSixDigits;
    }

    public void setFirstSixDigits(String firstSixDigits) {
        this.firstSixDigits = firstSixDigits;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getPayRequestIssuingBank() {
        return payRequestIssuingBank;
    }

    public void setPayRequestIssuingBank(String payRequestIssuingBank) {
        this.payRequestIssuingBank = payRequestIssuingBank;
    }

    public String getNetworkTokenRequestorId() {
        return networkTokenRequestorId;
    }

    public void setNetworkTokenRequestorId(String networkTokenRequestorId) {
        this.networkTokenRequestorId = networkTokenRequestorId;
    }

    public String getMerchantLinkRefId() {
        return merchantLinkRefId;
    }

    public void setMerchantLinkRefId(String merchantLinkRefId) {
        this.merchantLinkRefId = merchantLinkRefId;
    }

    public String getSdkType() {
        return sdkType;
    }

    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }

    public String getPaymentPromoCheckoutDataPromoCode() {
        return paymentPromoCheckoutDataPromoCode;
    }

    public String getSubsLinkInfo() {
        return subsLinkInfo;
    }

    public void setSubsLinkInfo(String subsLinkInfo) {
        this.subsLinkInfo = subsLinkInfo;
    }

    public void setPaymentPromoCheckoutDataPromoCode(String paymentPromoCheckoutDataPromoCode) {
        this.paymentPromoCheckoutDataPromoCode = paymentPromoCheckoutDataPromoCode;
    }

    public String getSearch2() {
        return search2;
    }

    public void setSearch2(String search2) {
        this.search2 = search2;
    }

    public String getVanIfsc() {
        return vanIfsc;
    }

    public void setVanIfsc(String vanIfsc) {
        this.vanIfsc = vanIfsc;
    }

    public String getActualMid() {
        return actualMid;
    }

    public void setActualMid(String actualMid) {
        this.actualMid = actualMid;
    }

    public String getActualOrderId() {
        return actualOrderId;
    }

    public void setActualOrderId(String actualOrderId) {
        this.actualOrderId = actualOrderId;
    }

    public String getDummyOrderId() {
        return dummyOrderId;
    }

    public void setDummyOrderId(String dummyOrderId) {
        this.dummyOrderId = dummyOrderId;
    }

    public String getDummyMerchantId() {
        return dummyMerchantId;
    }

    public void setDummyMerchantId(String dummyMerchantId) {
        this.dummyMerchantId = dummyMerchantId;
    }

    public String getDummyAlipayMid() {
        return dummyAlipayMid;
    }

    public void setDummyAlipayMid(String dummyAlipayMid) {
        this.dummyAlipayMid = dummyAlipayMid;
    }

    public String getSearch3() {
        return search3;
    }

    public void setSearch3(String search3) {
        this.search3 = search3;
    }

    public String getInternationalCardPayment() {
        return internationalCardPayment;
    }

    public void setInternationalCardPayment(String internationalCardPayment) {
        this.internationalCardPayment = internationalCardPayment;
    }

    public boolean isCheckoutJsAppInvokePayment() {
        return checkoutJsAppInvokePayment;
    }

    public void setCheckoutJsAppInvokePayment(boolean checkoutJsAppInvokePayment) {
        this.checkoutJsAppInvokePayment = checkoutJsAppInvokePayment;
    }

    public boolean isPushDataToDynamicQR() {
        return pushDataToDynamicQR;
    }

    public void setPushDataToDynamicQR(boolean pushDataToDynamicQR) {
        this.pushDataToDynamicQR = pushDataToDynamicQR;
    }

    public String getPtInfo1() {
        return ptInfo1;
    }

    public void setPtInfo1(String ptInfo1) {
        this.ptInfo1 = ptInfo1;
    }

    public String getTypeOfPayment() {
        return typeOfPayment;
    }

    public void setTypeOfPayment(String typeOfPayment) {
        this.typeOfPayment = typeOfPayment;
    }

    public String getPaymodeIdentifier() {
        return paymodeIdentifier;
    }

    public void setPaymodeIdentifier(String paymodeIdentifier) {
        this.paymodeIdentifier = paymodeIdentifier;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean isPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(Boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public Boolean isCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(Boolean corporateCard) {
        this.corporateCard = corporateCard;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public String getUpiToAddnPayPromoCode() {
        return upiToAddnPayPromoCode;
    }

    public void setUpiToAddnPayPromoCode(String upiToAddnPayPromoCode) {
        this.upiToAddnPayPromoCode = upiToAddnPayPromoCode;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public Routes getLpvRoute() {
        return lpvRoute;
    }

    public void setLpvRoute(Routes lpvRoute) {
        this.lpvRoute = lpvRoute;
    }

    public Boolean getEdcLinkTxn() {
        return edcLinkTxn;
    }

    public void setEdcLinkTxn(Boolean edcLinkTxn) {
        this.edcLinkTxn = edcLinkTxn;
    }

    public String getValidationModelInfo() {
        return validationModelInfo;
    }

    public void setValidationModelInfo(String validationModeInfo) {
        this.validationModelInfo = validationModeInfo;
    }

    public String getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    public String getBrandInvoiceNumber() {
        return brandInvoiceNumber;
    }

    public void setBrandInvoiceNumber(String brandInvoiceNumber) {
        this.brandInvoiceNumber = brandInvoiceNumber;
    }

    public Boolean getValidationSkipFlag() {
        return validationSkipFlag;
    }

    public void setValidationSkipFlag(Boolean validationSkipFlag) {
        this.validationSkipFlag = validationSkipFlag;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public boolean isAoaSubsOnPgMid() {
        return aoaSubsOnPgMid;
    }

    public void setAoaSubsOnPgMid(boolean aoaSubsOnPgMid) {
        this.aoaSubsOnPgMid = aoaSubsOnPgMid;
    }

    public String getCobrandedCustomDisplayName() {
        return cobrandedCustomDisplayName;
    }

    public void setCobrandedCustomDisplayName(String cobrandedCustomDisplayName) {
        this.cobrandedCustomDisplayName = cobrandedCustomDisplayName;
    }

    public Boolean getIsSubventionCreated() {
        return isSubventionCreated;
    }

    public void setIsSubventionCreated(Boolean isSubventionCreated) {
        this.isSubventionCreated = isSubventionCreated;
    }

    public Boolean getIsBankOfferApplied() {
        return isBankOfferApplied;
    }

    public void setIsBankOfferApplied(Boolean isBankOfferApplied) {
        this.isBankOfferApplied = isBankOfferApplied;
    }

    public String getPplusUserId() {
        return pplusUserId;
    }

    public void setPplusUserId(String pplusUserId) {
        this.pplusUserId = pplusUserId;
    }

    public String getEmiCategory() {
        return emiCategory;
    }

    public void setEmiCategory(String emiCategory) {
        this.emiCategory = emiCategory;
    }

    public Boolean getHDFCDigiPOSMerchant() {
        return isHDFCDigiPOSMerchant;
    }

    public void setHDFCDigiPOSMerchant(Boolean HDFCDigiPOSMerchant) {
        isHDFCDigiPOSMerchant = HDFCDigiPOSMerchant;
    }

    public String getThreePconvFeeAmount() {
        return threePconvFeeAmount;
    }

    public void setThreePconvFeeAmount(String threePconvFeeAmount) {
        this.threePconvFeeAmount = threePconvFeeAmount;
    }

    public String getThreePconvFee() {
        return threePconvFee;
    }

    public void setThreePconvFee(String threePconvFee) {
        this.threePconvFee = threePconvFee;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ExtendedInfoRequestBean [callBackURL=").append(callBackURL).append(", peonURL=")
                .append(peonURL).append(", responseCode=").append(responseCode).append(", theme=").append(theme)
                .append(", clientIP=").append(clientIP).append(", website=").append(website).append(", promoCode=")
                .append(promoCode).append(", email=").append(email).append(", phoneNo=").append(phoneNo)
                .append(", promoResponseCode=").append(promoResponseCode).append(", merchantUniqueReference=")
                .append(merchantUniqueReference).append(", merchantTransId=").append(merchantTransId)
                .append(", paytmMerchantId=").append(paytmMerchantId).append(", mccCode=").append(mccCode)
                .append(", requestType=").append(requestType).append(", virtualPaymentAddr=")
                .append(virtualPaymentAddr).append(", savedCardId=").append(savedCardId).append(", txnType=")
                .append(txnType).append(", productCode=").append(productCode).append(", totalTxnAmount=")
                .append(totalTxnAmount).append(", subscriptionId=").append(subscriptionId)
                .append(", currentSubscriptionId=").append(currentSubscriptionId).append(", subsExpiryDate=")
                .append(subsExpiryDate).append(", payerUserID=").append(payerUserID).append(", payerAccountNumber=")
                .append(payerAccountNumber).append(", custID=").append(custID).append(", orderID=").append(orderID)
                .append(", subsPPIOnly=").append(subsPPIOnly).append(", subsPayMode=").append(subsPayMode)
                .append(", subsCappedAmount=").append(subsCappedAmount).append(", subscriptionType=")
                .append(subscriptionType).append(", amountToBeRefunded=").append(amountToBeRefunded)
                .append(", merchantName=").append(merchantName).append(", subsFreq=").append(subsFreq)
                .append(", subsFreqUnit=").append(subsFreqUnit).append(", issuingBankName=").append(issuingBankName)
                .append(", issuingBankId=").append(issuingBankId).append(", retryCount=").append(retryCount)
                .append(", emiPlanId=").append(emiPlanId).append(", topupAndPay=").append(topupAndPay)
                .append(", paytmUserId=").append(paytmUserId).append(", creditCardBillNo=").append(creditCardBillNo)
                .append(", successCallBackURL=").append(successCallBackURL).append(", failureCallBackURL=")
                .append(failureCallBackURL).append(", pendingCallBackURL=").append(pendingCallBackURL)
                .append(", udf1=").append(udf1).append(", udf2=").append(udf2).append(", udf3=").append(udf3)
                .append(", additionalInfo=").append(additionalInfo).append(", linkDescription=")
                .append(linkDescription).append(", linkName=").append(linkName).append(", merchantLimitEnabled=")
                .append(merchantLimitEnabled).append(", merchantLimitUpdated=").append(merchantLimitUpdated)
                .append(", merchantAddress=").append(merchantAddress).append(", merchantPhone=").append(merchantPhone)
                .append(", qrDisplayName=").append(qrDisplayName).append(", merchantCategory=")
                .append(merchantCategory).append(", merchantSubCategory=").append(merchantSubCategory)
                .append(", subsServiceId=").append(subsServiceId).append("]").append(", qrDisplayName=")
                .append(qrDisplayName).append(", merchantCategory=").append(merchantCategory).append(", exchangeRate=")
                .append(exchangeRate).append(", pointNumber=").append(pointNumber).append(", merchantSubCategory=")
                .append(merchantSubCategory).append(", promoApplyResultStatus=").append(promoApplyResultStatus)
                .append(", clientId=").append(clientId).append(", cardIndexNo=").append(cardIndexNo)
                .append(", mutualFundFeedInfo=").append(mutualFundFeedInfo).append(", CUST_ID=").append(customerId)
                .append(", merchantOnPaytm=").append(merchantOnPaytm).append(", flowType=").append(flowType)
                .append(", virtualAccountNo=").append(virtualAccountNo).append(", templateId").append(templateId)
                .append(", templateName").append(templateName).append(", isMerchantLimitEnabledForPay=")
                .append(isMerchantLimitEnabledForPay).append(", isMErchantLimitUpdatedForPay=")
                .append(isMerchantLimitEnabledForPay).append(", paymentPromoCheckoutData=")
                .append(paymentPromoCheckoutData).append(", merchantKybId=").append(merchantKybId)
                .append(", merchantContactNo=").append(merchantContactNo).append(", workFlow=").append(workFlow)
                .append(", guestToken=").append(guestToken).append(", autoRenewal=").append(autoRenewal)
                .append(", autoRetry=").append(autoRetry).append(", communicationManager=")
                .append(communicationManager).append(", subsRenewOrderAlreadyCreated=")
                .append(subsRenewOrderAlreadyCreated).append("]").append(paymentPromoCheckoutData)
                .append(", merchantKybId=").append(", merchantContactNo=").append(", orderAdditionalInfo=")
                .append(orderAdditionalInfo).append(merchantContactNo).append(", cardTokenRequired=")
                .append(cardTokenRequired).append("preDebitRenewal=").append(preDebitRenewal).append("payerName=")
                .append(payerName).append("payerPSP=").append(payerPSP).append(", search1=").append(search1)
                .append(cardTokenRequired).append("preDebitRenewal=").append(preDebitRenewal).append(", search1=")
                .append(search1).append(", firstSixDigits=").append(firstSixDigits).append(", lastFourDigits=")
                .append(lastFourDigits).append(", payRequestIssuingBank=").append(payRequestIssuingBank)
                .append(", networkTokenRequestorId=").append(networkTokenRequestorId).append(search1)
                .append(", merchantLinkRefId=").append(merchantLinkRefId).append(", search2=").append(search2)
                .append(", vanIfsc=").append(vanIfsc).append("]").append(", networkTokenRequestorId=")
                .append(networkTokenRequestorId).append(search1).append(", merchantLinkRefId=")
                .append(merchantLinkRefId).append(", search2=").append(search2).append(", search3=").append(search3)
                .append(", vanIfsc=").append(vanIfsc).append(", actualMid=").append(actualMid)
                .append(", actualOrderId=").append(actualOrderId).append(", dummyOrderId=").append(dummyOrderId)
                .append(", dummyMerchantId=").append(dummyMerchantId).append(", actualMid=").append(actualMid)
                .append(", actualOrderId=").append(actualOrderId).append("]").append(", networkTokenRequestorId=")
                .append(networkTokenRequestorId).append(search1).append(", merchantLinkRefId=")
                .append(merchantLinkRefId).append(", search2=").append(search2).append(", search3=").append(search3)
                .append(", vanIfsc=").append(vanIfsc).append(", actualMid=").append(actualMid)
                .append(", actualOrderId=").append(actualOrderId).append(", dummyOrderId=").append(dummyOrderId)
                .append(", dummyMerchantId=").append(dummyMerchantId).append(", actualMid=").append(actualMid)
                .append(", actualOrderId=").append(actualOrderId).append(", internationalCardPayment=")
                .append(internationalCardPayment).append(", checkoutJsAppInvokePayment=")
                .append(checkoutJsAppInvokePayment).append(", ptInfo1=").append(ptInfo1).append(", typeOfPayment=")
                .append(typeOfPayment).append(", paymodeIdentifier=").append(paymodeIdentifier).append(", bankId=")
                .append(bankId).append(", source=").append(source).append(", prepaidCard=").append(prepaidCard)
                .append(", corporateCard=").append(corporateCard).append(", binNumber=").append(binNumber)
                .append(", comments=").append(comments).append(", comment=").append(comment)
                .append(", fromAoaMerchant=").append(fromAoaMerchant).append(", qrCodeId=").append(qrCodeId)
                .append(", qrDeeplink=").append(qrDeeplink).append(", txnToken=").append(txnToken)
                .append(", isEdcLinkTxn").append(edcLinkTxn).append(brandInvoiceNumber).append(" , brandInvoiceNumber")
                .append(", validationModelInfo").append(validationModelInfo).append(", aoaSubsOnPgMid=")
                .append(aoaSubsOnPgMid).append(", paytmTid=").append(paytmTid).append(", brandName=").append(brandName)
                .append(", brandId=").append(brandId).append(", categoryName=").append(categoryName)
                .append(", categoryId=").append(categoryId).append(", productId=").append(productId)
                .append(", productName=").append(productName).append(", imeiKey=").append(imeiKey).append(", imei=")
                .append(imei).append(", serialNo=").append(serialNo).append(", emiInterestRate=")
                .append(emiInterestRate).append(", emiTotalAmount=").append(emiTotalAmount).append(", brandEmiAmount=")
                .append(brandEmiAmount).append(", emiTenure=").append(emiTenure).append(", bankOffer=")
                .append(bankOffer).append(", pplusUserId=").append(pplusUserId).append(", emiCategory=")
                .append(emiCategory).append(", isHDFCDigiPOSMerchant=").append(isHDFCDigiPOSMerchant)
                .append(", is3PconvFee=").append(threePconvFee).append(", 3PconvFeeAmount=")
                .append(threePconvFeeAmount).append("]");

        return builder.toString();

    }

}
