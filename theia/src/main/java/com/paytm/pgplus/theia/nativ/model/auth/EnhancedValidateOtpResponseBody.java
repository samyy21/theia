package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedValidateOtpResponseBody extends ValidateOtpResponseBody {

    private static final long serialVersionUID = 7222034146232727829L;

    private EnhancedCashierPage enhancedCashierPage;

    public EnhancedValidateOtpResponseBody() {
    }

    public EnhancedCashierPage getEnhancedCashierPage() {
        return enhancedCashierPage;
    }

    public void setEnhancedCashierPage(EnhancedCashierPage enhancedCashierPage) {
        this.enhancedCashierPage = enhancedCashierPage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(
                "com.paytm.pgplus.theia.nativ.model.auth.EnhancedValidateOtpResponseBody{");
        sb.append("enhancedCashierPage=").append(enhancedCashierPage);
        sb.append('}');
        return sb.toString();
    }
}
