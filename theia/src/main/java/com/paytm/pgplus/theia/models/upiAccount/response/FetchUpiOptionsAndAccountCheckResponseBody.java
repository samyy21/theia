package com.paytm.pgplus.theia.models.upiAccount.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.UpiPspOptions;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class FetchUpiOptionsAndAccountCheckResponseBody extends BaseResponseBody {

    @NonNull
    private boolean accountExists;

    @NonNull
    private List<UpiPspOptions> upiPspOptions;

    public boolean isAccountExists() {
        return accountExists;
    }

    public void setAccountExists(boolean accountExists) {
        this.accountExists = accountExists;
    }

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
