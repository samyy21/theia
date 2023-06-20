package com.paytm.pgplus.theia.paymentoffer.service;

import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchAllPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyItemLevelPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchAllPaymentOffersResponse;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserIdRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchUserIdResponse;

public interface IPaymentOffersService {

    ApplyPromoResponse applyPromo(ApplyPromoRequest request, String version, String referenceId);

    FetchAllPaymentOffersResponse fetchAllPaymentOffers(FetchAllPaymentOffersRequest request, String referenceId);

    PromoServiceResponseBase getApplyPromoResponse(ApplyPromoRequest request, String referenceId);

    ApplyItemLevelPromoResponse applyItemLevelPromo(ApplyPromoRequest request, String referenceId);

    FetchUserIdResponse fetchUserId(FetchUserIdRequest fetchUserIdRequest, String referenceId) throws Exception;

}
