package com.paytm.pgplus.biz.model.aoaorderlookup;

import com.paytm.pgplus.facade.common.model.Money;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AOAOrderInfo implements Serializable {
    private OrderStatus orderStatus;
    private String merchantTransId;
    private Date createdTime;
    private Money orderAmount;

    @Override
    public String toString() {
        return "AOAOrderInfo{" + "orderStatus=" + orderStatus + ", merchantTransId='" + merchantTransId + '\''
                + ", createdTime=" + createdTime + ", orderAmount=" + orderAmount + '}';
    }
}
