package com.paytm.pgplus.theia.nativ.model.risk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoVerifyRequestBody implements Serializable {

    private static final long serialVersionUID = 7438448019784096320L;

    @NotBlank(message = "method cannot be blank")
    @Length(max = 32, message = "method cannot be more than 32 characters")
    @JsonProperty("method")
    private String method;

    @NotBlank(message = "validateData cannot be blank")
    @Length(max = 2056, message = "method cannot be more than 2056 characters")
    @JsonProperty("validateData")
    @Mask
    private String validateData;

    public String getMethod() {
        return method;
    }

    public String getValidateData() {
        return validateData;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
