package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.Money;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * Created by rahulverma on 23/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditBalanceInfo extends BalanceInfo {

    private static final long serialVersionUID = -8602562123768081864L;
    private Map<String, String> extendInfo;

    public DigitalCreditBalanceInfo(String payerAccountNo, Money accountBalance, String extendInfo,
            boolean isAccountBalanceInPaise) {
        super(payerAccountNo, accountBalance, isAccountBalanceInPaise);
        if (StringUtils.isEmpty(extendInfo))
            return;
        try {
            this.extendInfo = (Map<String, String>) JsonMapper.mapJsonToObject(extendInfo, Map.class);
        } catch (FacadeCheckedException e) {

        }
    }

    public DigitalCreditBalanceInfo(String payerAccountNo, Money accountBalance, Map<String, String> extendInfo,
            boolean isAccountBalanceInPaise) {
        super(payerAccountNo, accountBalance, isAccountBalanceInPaise);
        this.extendInfo = extendInfo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
