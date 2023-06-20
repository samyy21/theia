package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponse;

public interface ICardNumberValidationService {

    public CardNumberValidationResponse fetchCardNumberValidationDetail(
            CardNumberValidationRequest cardNumberValidationRequest);

}
