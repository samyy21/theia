package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;

import java.io.Serializable;
import java.util.List;

public class BizPaymentInfo implements Serializable {

    private static final long serialVersionUID = 4250224204975417207L;

    private String contractId;

    private List<BizPayOptionBill> payOptionBills;

    private List<BizAoaPayOptionBill> aoaPayOptionBills;

    private BizChannelPreference channelPreference;

    private String pwpCategory;

    private boolean addAndPayMigration;

    private String verificationType;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public BizChannelPreference getChannelPreference() {
        return channelPreference;
    }

    /**
	 *
	 */
    public BizPaymentInfo() {
    }

    /**
     * @return the contractId
     */
    public String getContractId() {
        return contractId;
    }

    public String getPwpCategory() {
        return pwpCategory;
    }

    /**
     * @return the payOptionBills
     */
    public List<BizPayOptionBill> getPayOptionBills() {
        return payOptionBills;
    }

    public void setPayOptionBills(List<BizPayOptionBill> payOptionBills) {
        this.payOptionBills = payOptionBills;
    }

    public boolean isAddAndPayMigration() {
        return addAndPayMigration;
    }

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    /**
     * @param contractId
     * @param payOptionBills
     */
    public BizPaymentInfo(final String contractId, final List<BizPayOptionBill> payOptionBills) {
        this.contractId = contractId;
        this.payOptionBills = payOptionBills;
    }

    public BizPaymentInfo(String contractId, List<BizPayOptionBill> payOptionBills,
            BizChannelPreference channelPreference) {
        this.contractId = contractId;
        this.payOptionBills = payOptionBills;
        this.channelPreference = channelPreference;
    }

    public BizPaymentInfo(String contractId, BizChannelPreference channelPreference,
            List<BizAoaPayOptionBill> aoaPayOptionBills) {
        this.contractId = contractId;
        this.aoaPayOptionBills = aoaPayOptionBills;
        this.channelPreference = channelPreference;
    }

    public BizPaymentInfo(final List<BizAoaPayOptionBill> aoaPayOptionBills, final String contractId) {
        this.contractId = contractId;
        this.aoaPayOptionBills = aoaPayOptionBills;
    }

    public BizPaymentInfo(String contractId, List<BizPayOptionBill> payOptionBills, String pwpCategory,
            boolean addAndPayMigration) {
        this.contractId = contractId;
        this.payOptionBills = payOptionBills;
        this.pwpCategory = pwpCategory;
        this.addAndPayMigration = addAndPayMigration;
    }

    public BizPaymentInfo(String contractId, List<BizPayOptionBill> payOptionBills, String pwpCategory,
            boolean addAndPayMigration, String verificationType) {
        this.contractId = contractId;
        this.payOptionBills = payOptionBills;
        this.pwpCategory = pwpCategory;
        this.addAndPayMigration = addAndPayMigration;
        this.verificationType = verificationType;
    }

    /**
     * @param contractId
     */
    public BizPaymentInfo(final String contractId) {
        this.contractId = contractId;
    }

    /**
     * @param payOptionBills
     */
    public BizPaymentInfo(final List<BizPayOptionBill> payOptionBills) {
        this.payOptionBills = payOptionBills;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizPaymentInfo{");
        sb.append("contractId='").append(contractId).append('\'');
        sb.append(", payOptionBills=").append(payOptionBills);
        sb.append(", channelPreference=").append(channelPreference);
        sb.append(", addAndPayMigration=").append(addAndPayMigration);
        sb.append(", verificationType=").append(verificationType);
        sb.append('}');
        return sb.toString();
    }
}
