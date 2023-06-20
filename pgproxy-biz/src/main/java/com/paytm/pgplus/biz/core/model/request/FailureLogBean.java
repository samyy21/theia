package com.paytm.pgplus.biz.core.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailureLogBean implements Serializable {

    private static final long serialVersionUID = 7653300963596387240L;

    private Date createDateAndTime;
    private String mid;
    private String orderId;
    private String uniqueId;
    private String theiaApiName;
    private String theiaErrorCode;
    private String theiaErrorMessage;
    private String paymentMode;
    private String channelCode;
    private String ApiName;
    private String ApiErrorCode;
    private String ApiErrorMessage;

}