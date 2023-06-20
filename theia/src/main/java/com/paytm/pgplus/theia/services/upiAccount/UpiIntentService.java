package com.paytm.pgplus.theia.services.upiAccount;

import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsAndAccountCheckRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.UpiAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsAndAccountCheckResponseBody;
import com.paytm.pgplus.theia.models.upiAccount.response.UpiAccountResponse;

public interface UpiIntentService {

    UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> getUpiIntent(
            UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request);
}
