package com.paytm.pgplus.biz.enums;

import org.apache.commons.lang.StringUtils;

public enum BusinessFlowType {
    STANDARD_CHECKOUT("standardCheckout", false, "enhancedCashierFlow"), CUSTOM_CHECKOUT("customCheckout", true, ""), BLINK_CHECKOUT(
            "blinkCheckout", true, "checkout");

    private String value;
    private boolean isNativeJsonRequest;
    private String workflow;

    BusinessFlowType(String value, boolean isNativeJsonRequest, String workflow) {
        this.value = value;
        this.isNativeJsonRequest = isNativeJsonRequest;
        this.workflow = workflow;
    }

    public static String getBusinessType(String workflow, boolean isNativeJsonRequest) {
        for (BusinessFlowType businessFlowType : BusinessFlowType.values()) {
            if (StringUtils.isNotBlank(workflow)) {
                if (businessFlowType.isNativeJsonRequest == isNativeJsonRequest
                        && businessFlowType.workflow.equals(workflow))
                    return businessFlowType.getValue();
            } else if (isNativeJsonRequest) {
                return CUSTOM_CHECKOUT.getValue();
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNativeJsonRequest() {
        return isNativeJsonRequest;
    }

    public void setNativeJsonRequest(boolean nativeJsonRequest) {
        isNativeJsonRequest = nativeJsonRequest;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }
}
