package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.customaspects.annotations.Mask;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class FetchUserPaymentOffersRequestBody extends FetchAllPaymentOffersRequestBody implements Serializable {

    private static final long serialVersionUID = -6030227324049672999L;

    @Length(max = 15, message = "Invalid length for mobileNo")
    @Mask(prefixNoMaskLen = 3, suffixNoMaskLen = 3)
    @NotNull
    private String mobileNo;

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
