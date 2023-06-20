package com.paytm.pgplus.theia.offline.services;

import com.paytm.pgplus.theia.offline.model.request.PassCodeValidationRequest;
import com.paytm.pgplus.theia.offline.model.response.PassCodeValidationResponse;

/**
 * Created by rahulverma on 13/10/17.
 */
public interface IPassCodeValidationService {

    PassCodeValidationResponse validatePassCode(PassCodeValidationRequest passCodeValidationRequest);

}
