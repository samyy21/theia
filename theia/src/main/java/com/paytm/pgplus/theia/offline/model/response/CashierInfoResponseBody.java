/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.theia.models.MerchantDetails;
import com.paytm.pgplus.theia.offline.enums.PaymentFlow;
import com.paytm.pgplus.theia.offline.model.payview.PayMethodViews;
import com.paytm.pgplus.theia.offline.model.payview.PostConvenienceFee;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by rahulverma on 23/8/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashierInfoResponseBody extends ResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;
    private String orderId;

    @Valid
    private PostConvenienceFee postConvenienceFee;

    @ApiModelProperty(required = true)
    @NotEmpty
    private List<PaymentFlow> enabledFlows;

    @NotNull
    @Valid
    private PayMethodViews payMethodViews;

    private MerchantDetails merchantDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String prepaidCardMaxAmount;

    public CashierInfoResponseBody() {
    }

    public CashierInfoResponseBody(String orderId, PostConvenienceFee postConvenienceFee,
            List<PaymentFlow> enabledFlows, PayMethodViews payMethodViews) {
        this.orderId = orderId;
        this.postConvenienceFee = postConvenienceFee;
        this.enabledFlows = enabledFlows;
        this.payMethodViews = payMethodViews;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PostConvenienceFee getPostConvenienceFee() {
        return postConvenienceFee;
    }

    public void setPostConvenienceFee(PostConvenienceFee postConvenienceFee) {
        this.postConvenienceFee = postConvenienceFee;
    }

    public List<PaymentFlow> getEnabledFlows() {
        return enabledFlows;
    }

    public void setEnabledFlows(List<PaymentFlow> enabledFlows) {
        this.enabledFlows = enabledFlows;
    }

    public PayMethodViews getPayMethodViews() {
        return payMethodViews;
    }

    public void setPayMethodViews(PayMethodViews payMethodViews) {
        this.payMethodViews = payMethodViews;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
    }

    public String getPrepaidCardMaxAmount() {
        return prepaidCardMaxAmount;
    }

    public void setPrepaidCardMaxAmount(String prepaidCardMaxAmount) {
        this.prepaidCardMaxAmount = prepaidCardMaxAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CashierInfoResponseBody{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", postConvenienceFee=").append(postConvenienceFee);
        sb.append(", enabledFlows=").append(enabledFlows);
        sb.append(", payMethodViews=").append(payMethodViews);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
