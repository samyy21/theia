package com.paytm.pgplus.theia.models.upiAccount.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUpiOptionsRequestHeader extends TokenRequestHeader {

    private static final long serialVersionUID = 1044777696434020169L;

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
