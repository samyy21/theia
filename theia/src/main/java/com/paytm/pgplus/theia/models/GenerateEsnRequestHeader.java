package com.paytm.pgplus.theia.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
public class GenerateEsnRequestHeader {
    private String clientId;
    @NotBlank(message = "RequestTimeStamp passed in Request is null ")
    private String requestTimestamp;
    @NotBlank(message = "version passed in the request is null")
    private String version;
    @NotBlank(message = "Request id passed in the request is null")
    @Length(max = 60, message = "Invalid length for requestMsgId")
    @Pattern(regexp = "^[a-zA-Z0-9-|_@.-]*$", message = "Validation regex not matching for requestMsgId")
    private String requestMsgId;
    @NotBlank(message = "signature passed in the request is null")
    private String signature;
}