/**
 * Alipay.com Inc. * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletLimits implements Serializable {

    private static final long serialVersionUID = -7869921683149196434L;

    private Boolean isLimitApplicable;
    private String limitMessage;
    private String message;
}