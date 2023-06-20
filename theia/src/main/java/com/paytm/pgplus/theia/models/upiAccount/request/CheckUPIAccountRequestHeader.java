package com.paytm.pgplus.theia.models.upiAccount.request;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

@Deprecated
public class CheckUPIAccountRequestHeader extends TokenRequestHeader {

    private final static long serialVersionUID = -92452623723427L;

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
