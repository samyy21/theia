package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.BaseResponseBody;
import com.paytm.pgplus.models.ItemLevelPaymentOffer;
import com.paytm.pgplus.models.PaymentOffer;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ApplyItemLevelPromoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 5348429511016532023L;
    private ItemLevelPaymentOffer paymentOffer;
}
