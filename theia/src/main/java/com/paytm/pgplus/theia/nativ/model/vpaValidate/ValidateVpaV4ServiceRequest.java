package com.paytm.pgplus.theia.nativ.model.vpaValidate;

import com.paytm.pgplus.facade.common.model.BaseRequestBody;
import com.paytm.pgplus.theia.nativ.supergw.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateVpaV4ServiceRequest extends BaseRequestBody {

    private String mid;
    private String vpaAddress;
    private String orderId;
    private PaymentType paymentType;
    private String queryParams;

}
