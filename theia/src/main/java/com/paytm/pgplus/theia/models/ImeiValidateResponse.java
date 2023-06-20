package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.common.model.ResultInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class ImeiValidateResponse implements Serializable {
    private ResultInfo resultInfo;
    private String mid;
    private String orderId;
}
