package com.paytm.pgplus.theia.emiSubvention.model.response.tenures;

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
public class EmiTenuresResponse implements Serializable {

    private static final long serialVersionUID = -3840892347306197096L;

    private ResponseHeader head;

    @NotNull
    @Valid
    private EmiTenuresResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EmiTenuresResponseBody getBody() {
        return body;
    }

    public void setBody(EmiTenuresResponseBody body) {
        this.body = body;
    }
}
