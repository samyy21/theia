package com.paytm.pgplus.theia.nativ.model.payview.response;

import java.util.Map;

import com.paytm.pgplus.models.Money;
import org.apache.commons.lang3.StringUtils;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;

public class AdvanceDepositAccountInfo extends AccountInfo {
    private static final long serialVersionUID = -1268464393614280914L;
    private Map<String, String> extendInfo;

    public AdvanceDepositAccountInfo(String payerAccountNo, Money accountBalance, String extendInfo) {
        super(payerAccountNo, accountBalance);
        if (StringUtils.isEmpty(extendInfo))
            return;
        try {
            this.extendInfo = (Map<String, String>) JsonMapper.mapJsonToObject(extendInfo, Map.class);
        } catch (FacadeCheckedException e) {

        }
    }

    public AdvanceDepositAccountInfo(String payerAccountNo, Money accountBalance, Map<String, String> extendInfo) {
        super(payerAccountNo, accountBalance);
        if (extendInfo == null) {
            return;
        }
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
        final StringBuilder sb = new StringBuilder("AdvanceDepositAccountInfo{");
        sb.append("extendInfo=").append(extendInfo);
        sb.append('}');
        return sb.toString();
    }
}
