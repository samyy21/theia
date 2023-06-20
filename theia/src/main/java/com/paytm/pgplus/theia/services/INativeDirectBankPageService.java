package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceResponse;

import java.util.Map;

public interface INativeDirectBankPageService {

    NativeDirectBankPageServiceResponse callInstaProxyResendOtp(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception;

    NativeDirectBankPageServiceResponse callInstaProxySubmit(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception;

    NativeDirectBankPageServiceResponse callInstaProxyCancel(NativeDirectBankPageServiceRequest serviceRequest)
            throws Exception;
}
