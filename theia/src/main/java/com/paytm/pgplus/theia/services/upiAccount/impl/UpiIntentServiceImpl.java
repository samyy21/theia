package com.paytm.pgplus.theia.services.upiAccount.impl;

import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsAndAccountCheckRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.UpiAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsAndAccountCheckResponseBody;
import com.paytm.pgplus.theia.models.upiAccount.response.UpiAccountResponse;
import com.paytm.pgplus.theia.nativ.model.common.UpiPspOptions;
import com.paytm.pgplus.theia.services.upiAccount.UpiIntentService;
import com.paytm.pgplus.theia.services.upiAccount.helper.UpiIntentServiceHelper;
import com.paytm.pgplus.theiacommon.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpiIntentServiceImpl implements UpiIntentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpiIntentServiceImpl.class);

    @Autowired
    UpiIntentServiceHelper upiIntentServiceHelper;

    @Override
    public UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> getUpiIntent(
            UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request) {
        boolean accountExists = upiIntentServiceHelper.checkIfUPIAccountExistForUser(request);
        try {
            List<UpiPspOptions> upiPspOptions = upiIntentServiceHelper.fetchUpiOptions();
            return upiIntentServiceHelper.prepareSuccessResponse(accountExists, upiPspOptions);
        } catch (BaseException ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return upiIntentServiceHelper.prepareFailureResponse(accountExists);
        }
    }
}
