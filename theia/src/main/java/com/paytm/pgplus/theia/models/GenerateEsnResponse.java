package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.biz.workflow.model.GenerateEsnResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateEsnResponse {
    private static final long serialVersionUID = 8998407480933977000L;

    private GenerateEsnResponseHeader head;
    private GenerateEsnResponseBody body;
}
