package com.paytm.pgplus.theia.nativ.model.directpage;

import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.common.bankForm.model.FormDetail;

public class NativeDirectBankPageServiceResponse {

    private FormDetail formDetail;
    private DirectAPIResponse directAPIResponse;

    public NativeDirectBankPageServiceResponse(FormDetail formDetail, DirectAPIResponse directAPIResponse) {
        this.formDetail = formDetail;
        this.directAPIResponse = directAPIResponse;
    }

    public NativeDirectBankPageServiceResponse(FormDetail formDetail) {
        this.formDetail = formDetail;
    }

    public FormDetail getFormDetail() {
        return formDetail;
    }

    public void setFormDetail(FormDetail formDetail) {
        this.formDetail = formDetail;
    }

    public DirectAPIResponse getDirectAPIResponse() {
        return directAPIResponse;
    }

    public void setDirectAPIResponse(DirectAPIResponse directAPIResponse) {
        this.directAPIResponse = directAPIResponse;
    }
}
