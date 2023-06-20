package com.paytm.pgplus.biz.workflow.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateEsnResponseBody {
    private static final long serialVersionUID = -8764031178988194828L;

    String oldExternalSerialNo;
    String newExternalSerialNo;
    String mandateExternalSerialNo;
    String resultCode;
    String resultCodeId;
    String resultMsg;
}
