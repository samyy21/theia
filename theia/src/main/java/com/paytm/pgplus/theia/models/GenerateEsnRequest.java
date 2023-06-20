package com.paytm.pgplus.theia.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class GenerateEsnRequest {
    @NotNull(message = "Header passed in the request is null")
    @Valid
    private GenerateEsnRequestHeader header;
    @NotNull(message = "Body passed in the request is null")
    @Valid
    private GenerateEsnRequestBody body;
}
