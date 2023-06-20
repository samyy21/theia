package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponse;

public interface ICardValidationService {
    public ValidateCardResponse validateCardandFetchDetails(ValidateCardRequest validateCardRequest)
            throws FacadeCheckedException;
}
