package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiTypeChange implements Serializable {

    private static final long serialVersionUID = 1524313135669420546L;

    private String code;
    private boolean isUserMsgDisplay;
    private String message;
}
