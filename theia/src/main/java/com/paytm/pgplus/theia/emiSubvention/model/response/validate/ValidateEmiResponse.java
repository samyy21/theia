package com.paytm.pgplus.theia.emiSubvention.model.response.validate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateEmiResponse implements Serializable {

    private static final long serialVersionUID = 5814391388824674309L;

    private ResponseHeader head;

    @NotNull
    @Valid
    private ValidateEmiResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public ValidateEmiResponseBody getBody() {
        return body;
    }

    public void setBody(ValidateEmiResponseBody body) {
        this.body = body;
    }
}
