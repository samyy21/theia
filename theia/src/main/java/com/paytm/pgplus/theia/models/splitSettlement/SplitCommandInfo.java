package com.paytm.pgplus.theia.models.splitSettlement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.enums.SplitMethod;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.common.interfaces.Builder;
import com.paytm.pgplus.facade.common.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SplitCommandInfo implements Serializable {

    private static final long serialVersionUID = -4442335998420943573L;
    private SplitMethod splitMethod;
    private String targetMerchantId;
    private String pplusMid;
    private Money amount;
    private String percentage;
    private List<Goods> goods;
    private List<ShippingInfo> shippingInfo;
    private String extendInfo;

    public SplitCommandInfo(final SplitCommandInfoBuilder builder) {
        this.splitMethod = builder.splitMethod;
        this.targetMerchantId = builder.targetMerchantId;
        this.pplusMid = builder.pplusMid;
        this.amount = builder.amount;
        this.percentage = builder.percentage;
        this.goods = builder.goods;
        this.shippingInfo = builder.shippingInfo;
        this.extendInfo = builder.extendInfo;
    }

    public static class SplitCommandInfoBuilder implements Builder<SplitCommandInfo> {

        private SplitMethod splitMethod;
        private String targetMerchantId;
        private String pplusMid;
        private Money amount;
        private String percentage;
        private List<Goods> goods;
        private List<ShippingInfo> shippingInfo;
        private String extendInfo;

        public SplitCommandInfoBuilder(SplitMethod splitMethod, String targetMerchantId, String pplusMid, Money amount,
                String percentage) {

            this.splitMethod = splitMethod;
            this.targetMerchantId = targetMerchantId;
            this.pplusMid = pplusMid;
            this.amount = amount;
            this.percentage = percentage;
        }

        public SplitCommandInfoBuilder setSplitMethod(SplitMethod splitMethod) {
            this.splitMethod = splitMethod;
            return this;
        }

        public SplitCommandInfoBuilder setTargetMerchantId(String targetMerchantId) {
            this.targetMerchantId = targetMerchantId;
            return this;
        }

        public SplitCommandInfoBuilder setPplusMid(String pplusMid) {
            this.pplusMid = pplusMid;
            return this;
        }

        public SplitCommandInfoBuilder setAmount(Money amount) {
            this.amount = amount;
            return this;
        }

        public SplitCommandInfoBuilder setPercentage(String percentage) {
            this.percentage = percentage;
            return this;
        }

        public SplitCommandInfoBuilder setGoods(List<Goods> goods) {
            this.goods = goods;
            return this;
        }

        public SplitCommandInfoBuilder setShippingInfo(List<ShippingInfo> shippingInfo) {
            this.shippingInfo = shippingInfo;
            return this;
        }

        public SplitCommandInfoBuilder setExtendInfo(String extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        @Override
        public SplitCommandInfo build() {
            return new SplitCommandInfo(this);
        }

    }
}
