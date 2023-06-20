package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.risk.RiskConvenienceFee;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Util to provide risk fee for paymodes based on properties

public class PayModeRiskConsultFeeUtil {

    public static List<RiskConvenienceFee> fetchRiskConvenienceFeeDetails() {
        String payModesSupportingRiskFee = ConfigurationUtil.getTheiaProperty(
                TheiaConstant.RiskConsultConstants.PAYMODES_FOR_RISK_CONSULT, "");
        List<RiskConvenienceFee> riskConvenienceFeeList = new ArrayList<>();
        if (StringUtils.isNotBlank(payModesSupportingRiskFee)) {
            List<String> supportedPayModes = Arrays.asList(payModesSupportingRiskFee.split(","));
            for (String payMode : supportedPayModes) {
                com.paytm.pgplus.common.enums.PayMethod payMethod = com.paytm.pgplus.common.enums.PayMethod
                        .getPayMethodByMethod(payMode);
                RiskConvenienceFee riskConvenienceFee = new RiskConvenienceFee();
                if (payMethod != null) {
                    String riskFeeKey = TheiaConstant.RiskConsultConstants.RISK_CONSULT_FEE_PERCENT.replace(
                            TheiaConstant.RiskConsultConstants.PAYMODE_PLACEHOLDER, payMethod.getMethod());
                    String riskFee = ConfigurationUtil.getTheiaProperty(riskFeeKey, "0");
                    String riskMessageKey = TheiaConstant.RiskConsultConstants.RISK_CONSULT_MESSAGE.replace(
                            TheiaConstant.RiskConsultConstants.PAYMODE_PLACEHOLDER, payMethod.getMethod());
                    String riskMessage = ConfigurationUtil.getTheiaProperty(riskMessageKey, "");
                    riskConvenienceFee.setPayMethod(payMethod);
                    riskConvenienceFee.setFeePercent(riskFee);
                    riskConvenienceFee.setReason(riskMessage);
                }
                riskConvenienceFeeList.add(riskConvenienceFee);
            }
        }
        return riskConvenienceFeeList;
    }
}
