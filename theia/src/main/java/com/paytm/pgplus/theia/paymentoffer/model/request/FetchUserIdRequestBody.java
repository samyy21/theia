package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUserIdRequestBody implements Serializable {

    private static final long serialVersionUID = -8700580428214683613L;

    @NotBlank
    private String mid;

    @NotBlank
    @Pattern(regexp = "\\w*@paytm\\b")
    private String vpa;
}
