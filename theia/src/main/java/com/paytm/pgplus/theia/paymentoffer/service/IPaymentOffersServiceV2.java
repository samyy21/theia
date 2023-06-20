package com.paytm.pgplus.theia.paymentoffer.service;

import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchAllPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchAllPaymentOffersResponse;

public interface IPaymentOffersServiceV2 {
    ApplyPromoResponse applyPromoV2(ApplyPromoRequest request, String version, String referenceId);

    FetchAllPaymentOffersResponse fetchAllPaymentOffersV2(FetchAllPaymentOffersRequest request);
}
