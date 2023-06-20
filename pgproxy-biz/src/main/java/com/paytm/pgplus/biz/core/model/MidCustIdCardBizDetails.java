package com.paytm.pgplus.biz.core.model;

import java.io.Serializable;
import java.util.List;

public class MidCustIdCardBizDetails implements Serializable {

    private static final long serialVersionUID = -339094552889039472L;
    private String custId;
    private String mId;
    private List<CardBeanBiz> merchantCustomerCardList;

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public List<CardBeanBiz> getMerchantCustomerCardList() {
        return merchantCustomerCardList;
    }

    public void setMerchantCustomerCardList(List<CardBeanBiz> merchantCustomerCardList) {
        this.merchantCustomerCardList = merchantCustomerCardList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MidCustIdCardBizDetails [custId=").append(custId).append(", mId=").append(mId)
                .append(", merchantCustomerCardList=").append(merchantCustomerCardList).append("]");
        return builder.toString();
    }

}
