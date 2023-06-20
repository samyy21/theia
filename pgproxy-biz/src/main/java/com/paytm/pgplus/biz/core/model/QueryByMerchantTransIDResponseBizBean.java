/**
 * 
 */
package com.paytm.pgplus.biz.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.facade.acquiring.models.AmountDetail;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.InputUserInfo;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.acquiring.models.StatusDetail;
import com.paytm.pgplus.facade.acquiring.models.TimeDetail;

/**
 * @author namanjain
 *
 */
public class QueryByMerchantTransIDResponseBizBean implements Serializable {

    private static final long serialVersionUID = -6511274425711066165L;

    private String acquirementId;
    private String merchantTransId;
    private InputUserInfo buyer;
    private InputUserInfo seller;
    private String orderTitle;
    private Map<String, String> extendInfo;
    private AmountDetail amountDetail;
    private TimeDetail timeDetail;
    private StatusDetail statusDetail;
    private List<Goods> goods;
    private List<ShippingInfo> shippingInfo;

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public String getMerchantTransId() {
        return merchantTransId;
    }

    public void setMerchantTransId(String merchantTransId) {
        this.merchantTransId = merchantTransId;
    }

    public InputUserInfo getBuyer() {
        return buyer;
    }

    public void setBuyer(InputUserInfo buyer) {
        this.buyer = buyer;
    }

    public InputUserInfo getSeller() {
        return seller;
    }

    public void setSeller(InputUserInfo seller) {
        this.seller = seller;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public AmountDetail getAmountDetail() {
        return amountDetail;
    }

    public void setAmountDetail(AmountDetail amountDetail) {
        this.amountDetail = amountDetail;
    }

    public TimeDetail getTimeDetail() {
        return timeDetail;
    }

    public void setTimeDetail(TimeDetail timeDetail) {
        this.timeDetail = timeDetail;
    }

    public StatusDetail getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(StatusDetail statusDetail) {
        this.statusDetail = statusDetail;
    }

    public List<Goods> getGoods() {
        return goods;
    }

    public void setGoods(List<Goods> goods) {
        this.goods = goods;
    }

    public List<ShippingInfo> getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(List<ShippingInfo> shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public QueryByMerchantTransIDResponseBizBean() {

    }

    public QueryByMerchantTransIDResponseBizBean(String acquirementId, String merchantTransId,
            AmountDetail amountDetail, TimeDetail timeDetail, StatusDetail statusDetail) {
        this.acquirementId = acquirementId;
        this.merchantTransId = merchantTransId;
        this.amountDetail = amountDetail;
        this.timeDetail = timeDetail;
        this.statusDetail = statusDetail;
    }

    @Override
    public String toString() {
        return "QueryByMerchantTransIDResponseBizBean [acquirementId=" + acquirementId + ", merchantTransId="
                + merchantTransId + ", buyer=" + buyer + ", seller=" + seller + ", orderTitle=" + orderTitle
                + ", extendInfo=" + extendInfo + ", amountDetail=" + amountDetail + ", timeDetail=" + timeDetail
                + ", statusDetail=" + statusDetail + ", goods=" + goods + ", shippingInfo=" + shippingInfo + "]";
    }

}
