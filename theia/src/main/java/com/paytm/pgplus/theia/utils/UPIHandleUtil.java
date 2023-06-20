package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.theia.enums.UPIHandle;
import com.paytm.pgplus.theia.models.UPIHandleInfo;
import com.paytm.pgplus.theia.nativ.model.enhancenative.UPIContent;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by: satyamsinghrajput at 17/7/19
 */
@Component
public class UPIHandleUtil {
    private static HashMap<String, String> upiHandleMap;

    static {
        upiHandleMap = new HashMap<>();
        upiHandleMap.put(UPIHandle.PAYTM.getHandle(), UPIHandle.PAYTM.getAppName());
        upiHandleMap.put(UPIHandle.PHONEPE.getHandle(), UPIHandle.PHONEPE.getAppName());
        upiHandleMap.put(UPIHandle.BHIM.getHandle(), UPIHandle.BHIM.getAppName());
        upiHandleMap.put(UPIHandle.GOOGLE_PAY_AXIS.getHandle(), UPIHandle.GOOGLE_PAY_AXIS.getAppName());
        upiHandleMap.put(UPIHandle.GOOGLE_PAY_HDFC.getHandle(), UPIHandle.GOOGLE_PAY_HDFC.getAppName());
        upiHandleMap.put(UPIHandle.GOOGLE_PAY_ICICI.getHandle(), UPIHandle.GOOGLE_PAY_ICICI.getAppName());
        upiHandleMap.put(UPIHandle.GOOGLE_PAY_SBI.getHandle(), UPIHandle.GOOGLE_PAY_SBI.getAppName());
        upiHandleMap.put(UPIHandle.DEFAULT.getHandle(), UPIHandle.DEFAULT.getAppName());
    }

    public static HashMap<String, String> getUpiHandleMap() {
        return upiHandleMap;
    }

    // Setting App Name for upi polling page

    public static void setUPIHandle(WorkFlowRequestBean workFlowRequestBean, UPIContent content) {
        if (StringUtils.isNotBlank(workFlowRequestBean.getPaymentDetails())
                && workFlowRequestBean.getPaymentDetails().contains("@")) {
            String handle = workFlowRequestBean.getPaymentDetails().split("@")[1];

            if (UPIHandle.PHONEPE.getHandle().equalsIgnoreCase(handle)) {
                content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.PHONEPE.getAppName(), UPIHandle.PHONEPE
                        .getUpiImageName()));

            } else if (UPIHandle.PAYTM.getHandle().equalsIgnoreCase(handle)) {
                content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.PAYTM.getAppName(), UPIHandle.PAYTM
                        .getUpiImageName()));

            } else if (UPIHandle.BHIM.getHandle().equalsIgnoreCase(handle)) {
                content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.BHIM.getAppName(), UPIHandle.BHIM
                        .getUpiImageName()));

            } else if (handle.toLowerCase().contains(UPIHandle.GOOGLE_PAY.getHandle())) {
                content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.GOOGLE_PAY.getAppName(), UPIHandle.GOOGLE_PAY
                        .getUpiImageName()));

            } else {// fallback case
                content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.DEFAULT.getAppName(), UPIHandle.DEFAULT
                        .getUpiImageName()));
            }
        } else {
            content.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.DEFAULT.getAppName(), UPIHandle.DEFAULT
                    .getUpiImageName()));
        }
    }

    public static void setUPIHandle(WorkFlowRequestBean workFlowRequestBean, UPITransactionInfo transactionInfo) {
        if (StringUtils.isNotBlank(workFlowRequestBean.getPaymentDetails())
                && workFlowRequestBean.getPaymentDetails().contains("@")) {
            String handle = workFlowRequestBean.getPaymentDetails().split("@")[1];

            if (UPIHandle.PHONEPE.getHandle().equalsIgnoreCase(handle)) {
                transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.PHONEPE.getAppName(), UPIHandle.PHONEPE
                        .getUpiImageName()));

            } else if (UPIHandle.PAYTM.getHandle().equalsIgnoreCase(handle)) {
                transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.PAYTM.getAppName(), UPIHandle.PAYTM
                        .getUpiImageName()));

            } else if (UPIHandle.BHIM.getHandle().equalsIgnoreCase(handle)) {
                transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.BHIM.getAppName(), UPIHandle.BHIM
                        .getUpiImageName()));

            } else if (handle.toLowerCase().contains(UPIHandle.GOOGLE_PAY.getHandle())) {
                transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.GOOGLE_PAY.getAppName(),
                        UPIHandle.GOOGLE_PAY.getUpiImageName()));

            } else {// fallback case
                transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.DEFAULT.getAppName(), UPIHandle.DEFAULT
                        .getUpiImageName()));
            }
        } else {
            transactionInfo.setUpiHandleInfo(new UPIHandleInfo(UPIHandle.DEFAULT.getAppName(), UPIHandle.DEFAULT
                    .getUpiImageName()));
        }
    }
}
