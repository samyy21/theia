package com.paytm.pgplus.theia.emiSubvention.model.response.banks;

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
public class EmiBanksResponse implements Serializable {

    private static final long serialVersionUID = 1011360760404447000L;

    private ResponseHeader head;

    @NotNull
    @Valid
    private EmiBanksResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EmiBanksResponseBody getBody() {
        return body;
    }

    public void setBody(EmiBanksResponseBody body) {
        this.body = body;
    }
}
