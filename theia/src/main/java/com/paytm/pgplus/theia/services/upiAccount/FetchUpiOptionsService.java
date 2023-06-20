package com.paytm.pgplus.theia.services.upiAccount;

import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public interface FetchUpiOptionsService {
    FetchUpiOptionsResponse fetchUpiOptions(FetchUpiOptionsRequest fetchUpiOptionsRequest);
}
