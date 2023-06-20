package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedLogoutResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -1234151791477561433L;

    public EnhancedLogoutResponseBody() {
        super();
    }

    private EnhancedCashierPage enhancedCashierPage;

    public EnhancedCashierPage getEnhancedCashierPage() {
        return enhancedCashierPage;
    }

    public void setEnhancedCashierPage(EnhancedCashierPage enhancedCashierPage) {
        this.enhancedCashierPage = enhancedCashierPage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(
                "com.paytm.pgplus.theia.nativ.model.auth.EnhancedLogoutResponseBody{");
        sb.append("enhancedCashierPage=").append(enhancedCashierPage);
        sb.append('}');
        return sb.toString();
    }
}
