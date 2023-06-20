package com.paytm.pgplus.theia.services.upiAccount.impl;

import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import com.paytm.pgplus.theia.services.upiAccount.FetchUpiOptionsService;
import com.paytm.pgplus.theia.services.upiAccount.helper.FetchUpiOptionsServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class FetchUpiOptionsServiceImpl implements FetchUpiOptionsService {
    @Autowired
    private FetchUpiOptionsServiceHelper fetchUpiOptionsServiceHelper;

    @Override
    public FetchUpiOptionsResponse fetchUpiOptions(FetchUpiOptionsRequest fetchUpiOptionsRequest) {
        fetchUpiOptionsServiceHelper.validateRequest(fetchUpiOptionsRequest);
        FetchUpiOptionsResponse fetchUPIOptionsResponse = fetchUpiOptionsServiceHelper
                .fetchUpiOptions(fetchUpiOptionsRequest);
        return fetchUPIOptionsResponse;
    }

}
