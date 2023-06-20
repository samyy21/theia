package com.paytm.pgplus.theia.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@ToString
public class GenerateEsnRequestBody {
    @NotBlank(message = "RequestType passed in the request is null")
    private String externalSerialNo;
    private boolean isMandateFlow;
}
