package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponseBody;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.payloadvault.refund.enums.ResponseCode;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.CustomInitTxnResponse;
import com.paytm.pgplus.response.InitiateTransactionResponse;

import javax.ws.rs.core.Response;

public interface ICustomInitiateTransactionService {
    InitiateTransactionResponse createCustomInitResponse(ResponseCode responseCode);

    void setTransactionResponseCodeAndMessage(CustomInitTxnResponse customInitTxnResponse, String instErrorCode,
            SystemResponseCode systemResponseCode, String responseStatus, String resellerParentMid, String redirectUrl)
            throws MappingServiceClientException;

    QueryByMerchantTransIdResponse getMerchantTransIdResponse(String OrderId, String alipayMid, String paytmMid)
            throws Exception;

    void setStatusDetails(QueryByMerchantTransIdResponseBody queryByMerchantTransIdResponseBody,
            CustomInitTxnResponse txnStatusResponse, String resellerParentMid, String redirectUrl)
            throws MappingServiceClientException;

    void getCustomInitResponse(String resellerParentMid, String wrapperName, InitiateTransactionRequestBody body,
            CustomInitTxnResponse customInitTxnResponse, Response response) throws Exception;
}
