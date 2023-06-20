package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailRequest;

public interface INativePaymentService {

    InitiateTokenBody initiateTransaction(NativeInitiateRequest request);

    InitiateTokenBody initiateTransaction(InitiateTransactionRequest request);

    String generateResponseChecksum(InitiateTransactionRequest request, InitiateTransactionResponse response)
            throws Exception;

    Boolean updateTransactionDetail(UpdateTransactionDetailRequest request);

}
