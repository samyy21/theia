package com.paytm.pgplus.theia.nativ.supergw.util;

import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoV4Request;
import org.springframework.stereotype.Component;

import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@Component
public class PaymentUtil {

    public void setReferenceIdInBody(NativeCashierInfoV4Request request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

}
