package com.paytm.pgplus.theia.dynamicWrapper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WrapperTransitionPage implements Serializable {

    private static final long serialVersionUID = 4862129091196313257L;

    private Map<String, String> displayInfo;
    private Map<String, String> txnInfo;
    private String callbackUrl;
    private String txnStatus;
    private int timeCounter;
    private boolean downloadReceiptEnable;
    private boolean printReceiptEnable;
    private boolean timeCounterEnable;
    private LogoModel merchantLogo;
    private LogoModel secondaryLogo;
    private String mid;
    private String paymentType;
    private String merchantName;
    private String orderId;
    private String txnToken;
    private String txnAmount;
    private String txnTime;
}
