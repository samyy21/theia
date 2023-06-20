/**
 * 
 */
package com.paytm.pgplus.cashier.looper.model;

import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.SplitCommandInfo;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author amit.dubey
 *
 */
public class CashierTransactionStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String acquirementId;
    private final String merchantTransId;
    private final String orderTitle;
    private final Map<String, String> extendInfo;

    // InputUserInfo buyer
    private final String buyerUserId;
    private final String buyerExternalUserId;
    private final String buyerExternalUserType = "MERCHANT";
    private final String buyerNickname = "";

    // InputUserInfo seller
    private final String sellerUserId;
    private final String sellerExternalUserId;
    private final String sellerExternalUserType = "MERCHANT";
    private final String sellerNickname = "";

    // AmountDetail amountDetail
    private final CashierMoney orderAmount;
    private final CashierMoney payAmount;
    private final CashierMoney voidAmount;
    private final CashierMoney confirmAmount;
    private final CashierMoney refundAmount;
    private final CashierMoney chargebackAmount;
    private final CashierMoney unconfirmAmount;
    private final CashierMoney chargeAmount;

    // TimeDetail
    private final Date createdTime;
    private final List<Date> paidTime; // This field is wrongly mapped with p+ ,
                                       // so addig new one
    private final List<Date> confirmedTime;
    private final Date expiryTime;
    private final List<Date> paidTimes;

    // StatusDetail
    private final String statusDetailType;
    private final boolean statusDetailFrozen;
    private String requestType;
    private String currentTxnCount;

    // PaymentViews
    private List<PaymentView> paymentViews;
    private boolean prnValidationStatusSuccess;
    private String orderModifyExtendInfo;
    private List<SplitCommandInfo> splitCommandInfoList;

    public CashierTransactionStatus(final CashierTransactionStatusBuilder builder) {
        this.acquirementId = builder.acquirementId;
        this.merchantTransId = builder.merchantTransId;
        this.orderTitle = builder.orderTitle;
        this.extendInfo = builder.extendInfo;

        this.buyerUserId = builder.buyerUserId;
        this.buyerExternalUserId = builder.buyerExternalUserId;

        this.sellerUserId = builder.sellerUserId;
        this.sellerExternalUserId = builder.sellerExternalUserId;

        this.orderAmount = builder.orderAmount;
        this.payAmount = builder.payAmount;
        this.voidAmount = builder.voidAmount;
        this.confirmAmount = builder.confirmAmount;
        this.refundAmount = builder.refundAmount;
        this.chargebackAmount = builder.chargebackAmount;
        this.unconfirmAmount = builder.unconfirmAmount;
        this.chargeAmount = builder.chargeAmount;

        this.createdTime = builder.createdTime;
        this.paidTime = builder.paidTime;
        this.confirmedTime = builder.confirmedTime;
        this.expiryTime = builder.expiryTime;

        this.statusDetailType = builder.statusDetailType;
        this.statusDetailFrozen = builder.statusDetailFrozen;
        this.currentTxnCount = builder.currentTxnCount;
        this.paymentViews = builder.paymentViews;
        this.splitCommandInfoList = builder.splitCommandInfoList;
        this.orderModifyExtendInfo = builder.orderModifyExtendInfo;
        this.paidTimes = builder.paidTimes;

    }

    public CashierTransactionStatus(final QueryTransactionStatus queryTransactionStatus) {
        this.acquirementId = queryTransactionStatus.getAcquirementId();
        this.merchantTransId = queryTransactionStatus.getMerchantTransId();
        this.orderTitle = queryTransactionStatus.getOrderTitle();
        this.extendInfo = queryTransactionStatus.getExtendInfo();

        this.buyerUserId = queryTransactionStatus.getBuyerUserId();
        this.buyerExternalUserId = queryTransactionStatus.getBuyerExternalUserId();

        this.sellerUserId = queryTransactionStatus.getSellerUserId();
        this.sellerExternalUserId = queryTransactionStatus.getSellerExternalUserId();

        if (queryTransactionStatus.getOrderAmount() != null) {
            this.orderAmount = new CashierMoney(queryTransactionStatus.getOrderAmount().getCurrencyType(),
                    queryTransactionStatus.getOrderAmount().getAmount());
        } else {
            this.orderAmount = new CashierMoney("INR", "0");
        }

        if (queryTransactionStatus.getPayAmount() != null) {
            this.payAmount = new CashierMoney(queryTransactionStatus.getPayAmount().getCurrencyType(),
                    queryTransactionStatus.getPayAmount().getAmount());
        } else {
            this.payAmount = new CashierMoney("INR", "0");
        }

        if (queryTransactionStatus.getRefundAmount() != null) {
            this.refundAmount = new CashierMoney(queryTransactionStatus.getRefundAmount().getCurrencyType(),
                    queryTransactionStatus.getRefundAmount().getAmount());
        } else {
            this.refundAmount = new CashierMoney("INR", "0");
        }

        if (queryTransactionStatus.getChargebackAmount() != null) {
            this.chargebackAmount = new CashierMoney(queryTransactionStatus.getChargebackAmount().getCurrencyType(),
                    queryTransactionStatus.getChargebackAmount().getAmount());
        } else {
            this.chargebackAmount = new CashierMoney("INR", "0");
        }

        if (queryTransactionStatus.getChargeAmount() != null) {
            this.chargeAmount = new CashierMoney(queryTransactionStatus.getChargeAmount().getCurrencyType(),
                    queryTransactionStatus.getChargeAmount().getAmount());
        } else {
            this.chargeAmount = new CashierMoney("INR", "0");
        }

        this.voidAmount = new CashierMoney("INR", "0");
        this.confirmAmount = new CashierMoney("INR", "0");
        this.unconfirmAmount = new CashierMoney("INR", "0");

        this.createdTime = queryTransactionStatus.getCreatedTime();
        this.paidTime = queryTransactionStatus.getPaidTime();
        this.confirmedTime = queryTransactionStatus.getConfirmedTime();
        this.expiryTime = queryTransactionStatus.getExpiryTime();

        this.statusDetailType = queryTransactionStatus.getStatusDetailType();
        this.statusDetailFrozen = queryTransactionStatus.isStatusDetailFrozen();
        this.currentTxnCount = queryTransactionStatus.getCurrentTxnCount();
        this.splitCommandInfoList = queryTransactionStatus.getSplitCommandInfoList();
        this.paidTimes = queryTransactionStatus.getPaidTimes();
    }

    /**
     * @return the acquirementId
     */
    public String getAcquirementId() {
        return acquirementId;
    }

    /**
     * @return the merchantTransId
     */
    public String getMerchantTransId() {
        return merchantTransId;
    }

    /**
     * @return the buyerUserId
     */
    public String getBuyerUserId() {
        return buyerUserId;
    }

    /**
     * @return the buyerExternalUserId
     */
    public String getBuyerExternalUserId() {
        return buyerExternalUserId;
    }

    /**
     * @return the sellerUserId
     */
    public String getSellerUserId() {
        return sellerUserId;
    }

    /**
     * @return the sellerExternalUserId
     */
    public String getSellerExternalUserId() {
        return sellerExternalUserId;
    }

    /**
     * @return the orderTitle
     */
    public String getOrderTitle() {
        return orderTitle;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo != null ? extendInfo : Collections.emptyMap();
    }

    /**
     * @return the orderAmount
     */
    public CashierMoney getOrderAmount() {
        return orderAmount;
    }

    /**
     * @return the payAmount
     */
    public CashierMoney getPayAmount() {
        return payAmount;
    }

    /**
     * @return the voidAmount
     */
    public CashierMoney getVoidAmount() {
        return voidAmount;
    }

    /**
     * @return the confirmAmount
     */
    public CashierMoney getConfirmAmount() {
        return confirmAmount;
    }

    /**
     * @return the refundAmount
     */
    public CashierMoney getRefundAmount() {
        return refundAmount;
    }

    /**
     * @return the chargebackAmount
     */
    public CashierMoney getChargebackAmount() {
        return chargebackAmount;
    }

    /**
     * @return the unconfirmAmount
     */
    public CashierMoney getUnconfirmAmount() {
        return unconfirmAmount;
    }

    /**
     * @return the chargeAmount
     */
    public CashierMoney getChargeAmount() {
        return chargeAmount;
    }

    /**
     * @return the createdTime
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * @return the paidTime
     */
    public List<Date> getPaidTime() {
        return paidTime;
    }

    /**
     * @return the confirmedTime
     */
    public List<Date> getConfirmedTime() {
        return confirmedTime;
    }

    /**
     * @return the expiryTime
     */
    public Date getExpiryTime() {
        return expiryTime;
    }

    /**
     * @return the statusDetailType
     */
    public String getStatusDetailType() {
        return statusDetailType;
    }

    /**
     * @return the statusDetailFrozen
     */
    public boolean isStatusDetailFrozen() {
        return statusDetailFrozen;
    }

    public String getBuyerExternalUserType() {
        return buyerExternalUserType;
    }

    public String getBuyerNickname() {
        return buyerNickname;
    }

    public String getSellerExternalUserType() {
        return sellerExternalUserType;
    }

    public String getSellerNickname() {
        return sellerNickname;
    }

    public String getRequestType() {
        return requestType;
    }

    public CashierTransactionStatus setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public String getCurrentTxnCount() {
        return currentTxnCount;
    }

    public void setCurrentTxnCount(String currentTxnCount) {
        this.currentTxnCount = currentTxnCount;
    }

    public List<PaymentView> getPaymentViews() {
        return paymentViews;
    }

    public boolean isPrnValidationStatusSuccess() {
        return prnValidationStatusSuccess;
    }

    public void setPrnValidationStatusSuccess(boolean prnValidationStatusSuccess) {
        this.prnValidationStatusSuccess = prnValidationStatusSuccess;
    }

    public List<SplitCommandInfo> getSplitCommandInfoList() {
        return splitCommandInfoList;
    }

    public void setSplitCommandInfoList(List<SplitCommandInfo> splitCommandInfoList) {
        this.splitCommandInfoList = splitCommandInfoList;
    }

    public String getOrderModifyExtendInfo() {
        return orderModifyExtendInfo;
    }

    public void setOrderModifyExtendInfo(String orderModifyExtendInfo) {
        this.orderModifyExtendInfo = orderModifyExtendInfo;
    }

    public List<Date> getPaidTimes() {
        return paidTimes;
    }

    public static class CashierTransactionStatusBuilder {
        private final String acquirementId;
        private final String merchantTransId;
        private final String orderTitle;
        private final Map<String, String> extendInfo;

        // InputUserInfo buyer
        private String buyerUserId;
        private String buyerExternalUserId;

        // InputUserInfo seller
        private String sellerUserId;
        private String sellerExternalUserId;

        // AmountDetail amountDetail
        private CashierMoney orderAmount;
        private CashierMoney payAmount;
        private CashierMoney voidAmount;
        private CashierMoney confirmAmount;
        private CashierMoney refundAmount;
        private CashierMoney chargebackAmount;
        private CashierMoney unconfirmAmount;
        private CashierMoney chargeAmount;

        // TimeDetail
        private Date createdTime;
        private List<Date> paidTime;
        private List<Date> confirmedTime;
        private Date expiryTime;
        private List<Date> paidTimes;

        // StatusDetail
        private String statusDetailType;
        private boolean statusDetailFrozen;
        private String currentTxnCount;

        // paymentViews
        private List<PaymentView> paymentViews;
        private List<SplitCommandInfo> splitCommandInfoList;

        private String orderModifyExtendInfo;

        /**
         * @param acquirementId
         * @param merchantTransId
         * @param orderTitle
         * @param extendInfo
         */
        public CashierTransactionStatusBuilder(final String acquirementId, final String merchantTransId,
                final String orderTitle, final Map<String, String> extendInfo) {
            this.acquirementId = acquirementId;
            this.merchantTransId = merchantTransId;
            this.orderTitle = orderTitle;
            this.extendInfo = extendInfo;
        }

        public CashierTransactionStatusBuilder setInputUserInfoBuyer(final String buyerUserId,
                final String buyerExternalUserId) {
            this.buyerUserId = buyerUserId;
            this.buyerExternalUserId = buyerExternalUserId;
            return this;
        }

        public CashierTransactionStatusBuilder setInputUserInfoSeller(final String sellerUserId,
                final String sellerExternalUserId) {
            this.sellerUserId = sellerUserId;
            this.sellerExternalUserId = sellerExternalUserId;
            return this;
        }

        public CashierTransactionStatusBuilder setOrderAmount(final CashierMoney orderAmount) {
            this.orderAmount = orderAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setPayAmount(final CashierMoney payAmount) {
            this.payAmount = payAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setVoidAmount(final CashierMoney voidAmount) {
            this.voidAmount = voidAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setConfirmAmount(final CashierMoney confirmAmount) {
            this.confirmAmount = confirmAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setRefundAmount(final CashierMoney refundAmount) {
            this.refundAmount = refundAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setChargebackAmount(final CashierMoney chargebackAmount) {
            this.chargebackAmount = chargebackAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setUnconfirmAmount(final CashierMoney unconfirmAmount) {
            this.unconfirmAmount = unconfirmAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setChargeAmount(final CashierMoney chargeAmount) {
            this.chargeAmount = chargeAmount;
            return this;
        }

        public CashierTransactionStatusBuilder setTimeDetail(final Date createdTime, final List<Date> paidTime,
                final List<Date> confirmedTime, final Date expiryTime) {
            this.createdTime = createdTime;
            this.paidTime = paidTime;
            this.confirmedTime = confirmedTime;
            this.expiryTime = expiryTime;
            return this;
        }

        public CashierTransactionStatusBuilder setStatusDetail(final String statusDetailType,
                final boolean statusDetailFrozen) {
            this.statusDetailType = statusDetailType;
            this.statusDetailFrozen = statusDetailFrozen;
            return this;
        }

        public CashierTransactionStatusBuilder setCurrentTxnCount(final String currentTxnCount) {
            this.currentTxnCount = currentTxnCount;
            return this;
        }

        public CashierTransactionStatusBuilder setPaymentViews(final List<PaymentView> paymentViews) {
            this.paymentViews = paymentViews;
            return this;
        }

        public CashierTransactionStatusBuilder setSplitCommandInfoList(final List<SplitCommandInfo> splitCommandInfoList) {
            this.splitCommandInfoList = splitCommandInfoList;
            return this;
        }

        public CashierTransactionStatusBuilder setOrderModifyExtendInfo(final String orderModifyExtendInfo) {
            this.orderModifyExtendInfo = orderModifyExtendInfo;
            return this;
        }

        public CashierTransactionStatusBuilder setPaidTimesForTimeDetails(final List<Date> paidTimes) {
            this.paidTimes = paidTimes;
            return this;
        }

        public CashierTransactionStatus build() {
            return new CashierTransactionStatus(this);
        }
    }

}