package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequest;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponse;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequest;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponse;

public interface IRiskVerifierService {

    DoViewResponse doView(DoViewRequest request);

    DoVerifyResponse doVerify(DoVerifyRequest request);
}
