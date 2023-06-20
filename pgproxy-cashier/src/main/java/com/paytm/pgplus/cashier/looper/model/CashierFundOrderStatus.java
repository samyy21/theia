/**
 * 
 */
package com.paytm.pgplus.cashier.looper.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author amit.dubey
 *
 */
public class CashierFundOrderStatus implements Serializable {

    /**
     * serial version id
     */
    private static final long serialVersionUID = -2823170613121717069L;

    private String fundOrderId;
    private String actorUserId;
    private String invokerId;
    private String requestId;

    // FundType
    private String fundType;

    // ProductCodes
    private String productCode;
    private String productCodeId;

    // TerminalType
    private String terminalType;

    // UserIdentifier - payer
    private String payerUserId;
    private String payerLoginId;
    private String payerLoginIdType;
    private String payerAccountNo;

    // UserIdentifier - payee
    private String payeeUserId;
    private String payeeLoginId;
    private String payeeLoginIdType;
    private String payeeAccountNo;

    // Money
    private CashierMoney fundAmount;
    private CashierMoney chargeAmount;
    private CashierMoney taxAmount;
    private CashierMoney paidTotalAmount;
    private CashierMoney actualFundAmount;

    // FundOrderStatus
    private String fundOrderStatus;

    private Map<String, String> extendInfo;

    // dates
    private Date acceptedTime;
    private Date paidTime;
    private Date acceptExpiryTime;
    private Date payExpiryTime;
    private Date successTime;
    private Date createdTime;
    private Date modifiedTime;

    private CashierFundOrderStatus(CashierFundOrderStatusBuilder builder) {
        this.fundOrderId = builder.fundOrderId;
        this.actorUserId = builder.actorUserId;
        this.invokerId = builder.invokerId;
        this.requestId = builder.requestId;

        this.fundType = builder.fundType;

        this.productCode = builder.productCode;
        this.productCodeId = builder.productCodeId;

        this.terminalType = builder.terminalType;

        this.payerUserId = builder.payerUserId;
        this.payerLoginId = builder.payerLoginId;
        this.payerLoginIdType = builder.payerLoginIdType;
        this.payerAccountNo = builder.payerAccountNo;

        this.payeeUserId = builder.payeeUserId;
        this.payeeLoginId = builder.payeeLoginId;
        this.payeeLoginIdType = builder.payeeLoginIdType;
        this.payeeAccountNo = builder.payeeAccountNo;

        this.fundAmount = builder.fundAmount;

        this.chargeAmount = builder.chargeAmount;

        this.taxAmount = builder.taxAmount;

        this.paidTotalAmount = builder.paidTotalAmount;

        this.actualFundAmount = builder.actualFundAmount;

        this.fundOrderStatus = builder.fundOrderStatus;

        this.extendInfo = builder.extendInfo;

        this.acceptedTime = builder.acceptedTime;
        this.paidTime = builder.paidTime;
        this.acceptExpiryTime = builder.acceptExpiryTime;
        this.payExpiryTime = builder.payExpiryTime;
        this.successTime = builder.successTime;
        this.createdTime = builder.createdTime;
        this.modifiedTime = builder.modifiedTime;
    }

    /**
     * @return the fundOrderId
     */
    public String getFundOrderId() {
        return fundOrderId;
    }

    /**
     * @return the actorUserId
     */
    public String getActorUserId() {
        return actorUserId;
    }

    /**
     * @return the invokerId
     */
    public String getInvokerId() {
        return invokerId;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the fundType
     */
    public String getFundType() {
        return fundType;
    }

    /**
     * @return the productCode
     */
    public String getProductCode() {
        return productCode;
    }

    /**
     * @return the productCodeId
     */
    public String getProductCodeId() {
        return productCodeId;
    }

    /**
     * @return the terminalType
     */
    public String getTerminalType() {
        return terminalType;
    }

    /**
     * @return the payerUserId
     */
    public String getPayerUserId() {
        return payerUserId;
    }

    /**
     * @return the payerLoginId
     */
    public String getPayerLoginId() {
        return payerLoginId;
    }

    /**
     * @return the payerLoginIdType
     */
    public String getPayerLoginIdType() {
        return payerLoginIdType;
    }

    /**
     * @return the payerAccountNo
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * @return the payeeUserId
     */
    public String getPayeeUserId() {
        return payeeUserId;
    }

    /**
     * @return the payeeLoginId
     */
    public String getPayeeLoginId() {
        return payeeLoginId;
    }

    /**
     * @return the payeeLoginIdType
     */
    public String getPayeeLoginIdType() {
        return payeeLoginIdType;
    }

    /**
     * @return the payeeAccountNo
     */
    public String getPayeeAccountNo() {
        return payeeAccountNo;
    }

    /**
     * @return the fundAmount
     */
    public CashierMoney getFundAmount() {
        return fundAmount;
    }

    /**
     * @return the chargeAmount
     */
    public CashierMoney getChargeAmount() {
        return chargeAmount;
    }

    /**
     * @return the taxAmount
     */
    public CashierMoney getTaxAmount() {
        return taxAmount;
    }

    /**
     * @return the paidTotalAmount
     */
    public CashierMoney getPaidTotalAmount() {
        return paidTotalAmount;
    }

    /**
     * @return the actualFundAmount
     */
    public CashierMoney getActualFundAmount() {
        return actualFundAmount;
    }

    /**
     * @return the fundOrderStatus
     */
    public String getFundOrderStatus() {
        return fundOrderStatus;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * @return the acceptedTime
     */
    public Date getAcceptedTime() {
        return acceptedTime;
    }

    /**
     * @return the paidTime
     */
    public Date getPaidTime() {
        return paidTime;
    }

    /**
     * @return the acceptExpiryTime
     */
    public Date getAcceptExpiryTime() {
        return acceptExpiryTime;
    }

    /**
     * @return the payExpiryTime
     */
    public Date getPayExpiryTime() {
        return payExpiryTime;
    }

    /**
     * @return the successTime
     */
    public Date getSuccessTime() {
        return successTime;
    }

    /**
     * @return the createdTime
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * @return the modifiedTime
     */
    public Date getModifiedTime() {
        return modifiedTime;
    }

    public static class CashierFundOrderStatusBuilder {

        private String fundOrderId;
        private String actorUserId;
        private String invokerId;
        private String requestId;

        // FundType
        private String fundType;

        // ProductCodes
        private String productCode;
        private String productCodeId;

        // TerminalType
        private String terminalType;

        // UserIdentifier - payer
        private String payerUserId;
        private String payerLoginId;
        private String payerLoginIdType;
        private String payerAccountNo;

        // UserIdentifier - payee
        private String payeeUserId;
        private String payeeLoginId;
        private String payeeLoginIdType;
        private String payeeAccountNo;

        // Money
        private CashierMoney fundAmount;
        private CashierMoney chargeAmount;
        private CashierMoney taxAmount;
        private CashierMoney paidTotalAmount;
        private CashierMoney actualFundAmount;

        // FundOrderStatus
        private String fundOrderStatus;

        private Map<String, String> extendInfo;

        // dates
        private Date acceptedTime;
        private Date paidTime;
        private Date acceptExpiryTime;
        private Date payExpiryTime;
        private Date successTime;
        private Date createdTime;
        private Date modifiedTime;

        /**
         * @param fundOrderId
         * @param actorUserId
         * @param invokerId
         * @param requestId
         */
        public CashierFundOrderStatusBuilder(String fundOrderId, String actorUserId, String invokerId, String requestId) {
            this.fundOrderId = fundOrderId;
            this.actorUserId = actorUserId;
            this.invokerId = invokerId;
            this.requestId = requestId;
        }

        public CashierFundOrderStatusBuilder setFundType(String fundType) {
            this.fundType = fundType;
            return this;
        }

        public CashierFundOrderStatusBuilder setProductCode(String productCode, String productCodeId) {
            this.productCode = productCode;
            this.productCodeId = productCodeId;
            return this;
        }

        public CashierFundOrderStatusBuilder setTerminalType(String terminalType) {
            this.terminalType = terminalType;
            return this;
        }

        public CashierFundOrderStatusBuilder setPayerUser(String payerUserId, String payerLoginId,
                String payerLoginIdType, String payerAccountNo) {
            this.payerUserId = payerUserId;
            this.payerLoginId = payerLoginId;
            this.payerLoginIdType = payerLoginIdType;
            this.payerAccountNo = payerAccountNo;
            return this;
        }

        public CashierFundOrderStatusBuilder setPayeeUser(String payeeUserId, String payeeLoginId,
                String payeeLoginIdType, String payeeAccountNo) {
            this.payeeUserId = payeeUserId;
            this.payeeLoginId = payeeLoginId;
            this.payeeLoginIdType = payeeLoginIdType;
            this.payeeAccountNo = payeeAccountNo;
            return this;
        }

        public CashierFundOrderStatusBuilder setFundAmount(String currencyType, String amount) {
            this.fundAmount = new CashierMoney(currencyType, amount);
            return this;
        }

        public CashierFundOrderStatusBuilder setChargeAmount(String currencyType, String amount) {
            this.chargeAmount = new CashierMoney(currencyType, amount);
            return this;
        }

        public CashierFundOrderStatusBuilder setTaxAmount(String currencyType, String amount) {
            this.taxAmount = new CashierMoney(currencyType, amount);
            return this;
        }

        public CashierFundOrderStatusBuilder setPaidtotalAmount(String currencyType, String amount) {
            this.paidTotalAmount = new CashierMoney(currencyType, amount);
            return this;
        }

        public CashierFundOrderStatusBuilder setActualFundAmount(String currencyType, String amount) {
            this.actualFundAmount = new CashierMoney(currencyType, amount);
            return this;
        }

        public CashierFundOrderStatusBuilder setFundOrderStatus(String fundOrderStatus) {
            this.fundOrderStatus = fundOrderStatus;
            return this;
        }

        public CashierFundOrderStatusBuilder setExtendedInfo(Map<String, String> extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public CashierFundOrderStatusBuilder setDates(Date acceptedTime, Date paidTime, Date acceptExpiryTime,
                Date payExpiryTime, Date successTime, Date createdTime, Date modifiedTime) {
            this.acceptedTime = acceptedTime;
            this.paidTime = paidTime;
            this.acceptExpiryTime = acceptExpiryTime;
            this.payExpiryTime = payExpiryTime;
            this.successTime = successTime;
            this.createdTime = createdTime;
            this.modifiedTime = modifiedTime;
            return this;
        }

        public CashierFundOrderStatus build() {
            return new CashierFundOrderStatus(this);
        }

    }

}
