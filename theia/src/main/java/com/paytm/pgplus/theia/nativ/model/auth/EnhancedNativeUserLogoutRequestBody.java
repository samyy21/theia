package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedNativeUserLogoutRequestBody implements Serializable {

    private static final long serialVersionUID = -440964455243505338L;

}
