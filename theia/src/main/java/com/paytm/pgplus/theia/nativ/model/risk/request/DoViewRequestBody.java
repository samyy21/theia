package com.paytm.pgplus.theia.nativ.model.risk.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoViewRequestBody implements Serializable {

    private static final long serialVersionUID = 8634893450457277325L;

    @NotBlank(message = "method cannot be blank")
    @Length(max = 32, message = "method cannot be more than 32 characters")
    @JsonProperty("method")
    private String method;

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "DoViewRequestBody{" + "method='" + method + '\'' + '}';
    }
}
