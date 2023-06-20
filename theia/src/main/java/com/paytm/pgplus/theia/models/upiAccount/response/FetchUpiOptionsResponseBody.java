package com.paytm.pgplus.theia.models.upiAccount.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.UpiPspOptions;

import java.util.List;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUpiOptionsResponseBody extends BaseResponseBody {

    private List<UpiPspOptions> upiPspOptions;

    public List<UpiPspOptions> getUpiPspOptions() {
        return upiPspOptions;
    }

    public void setUpiPspOptions(List<UpiPspOptions> upiPspOptions) {
        this.upiPspOptions = upiPspOptions;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
