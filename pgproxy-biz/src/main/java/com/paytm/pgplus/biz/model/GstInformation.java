package com.paytm.pgplus.biz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GstInformation implements Serializable {

    private static final long serialVersionUID = 2871613855315351524L;

    private String gstBrkUp;
    private String invoiceNo;
    private String invoiceDate;
    private String gstIn;
    private String enTips;
    private String payerConsent;
    private String tipAmount;
}
