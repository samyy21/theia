package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponse;

import java.io.IOException;

public interface IMerchantInfoService<Req, Res> {

    void mapSsoTxnTokens(Req req, boolean callbackInResponse, MerchantInfoResponse merchantInfoResponse);

    Res fetchMerchantInfoResponse(Req req) throws HttpCommunicationException, IllegalPayloadException, IOException;

}
