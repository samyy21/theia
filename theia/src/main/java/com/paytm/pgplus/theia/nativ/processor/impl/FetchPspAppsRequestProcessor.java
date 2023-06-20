package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.cache.model.PspSchemaResponseBody;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IPspSchemaService;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsRequest;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsResponse;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("fetchPspAppsRequestProcessor")
public class FetchPspAppsRequestProcessor extends
        AbstractRequestProcessor<FetchPspAppsRequest, FetchPspAppsResponse, FetchPspAppsRequest, FetchPspAppsResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchPspAppsRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FetchPspAppsRequestProcessor.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    IPspSchemaService pspSchemaService;

    @Override
    protected FetchPspAppsRequest preProcess(FetchPspAppsRequest request) {
        if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType())) {
            validateWithTxnToken(request.getHead().getTxnToken());
            return request;
        }
        LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
        throw new RequestValidationException(new ResultInfo(ResultCode.SYSTEM_ERROR.getResultStatus(),
                ResultCode.SYSTEM_ERROR.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
    }

    @Override
    protected FetchPspAppsResponse onProcess(FetchPspAppsRequest request, FetchPspAppsRequest serviceRequest)
            throws Exception {
        FetchPspAppsResponseBody fetchPspAppsResponseBody = getPspAppsList();
        if (fetchPspAppsResponseBody == null)
            return null;
        FetchPspAppsResponse fetchPspAppsResponse = new FetchPspAppsResponse();
        fetchPspAppsResponse.setBody(fetchPspAppsResponseBody);
        return fetchPspAppsResponse;
    }

    @Override
    protected FetchPspAppsResponse postProcess(FetchPspAppsRequest request, FetchPspAppsRequest serviceRequest,
            FetchPspAppsResponse response) {
        if (response == null) {
            return generateErrorResponse();
        }
        return generateSuccessResponse(response);
    }

    private void validateWithTxnToken(String txnToken) {
        NativeInitiateRequest request = nativeSessionUtil.validate(txnToken);
    }

    private FetchPspAppsResponseBody getPspAppsList() throws MappingServiceClientException {
        PspSchemaResponseBody pspSchemeResponseBody = pspSchemaService.getPspSchemas();
        EXT_LOGGER.customInfo("Mapping response - PspSchemaResponseBody :: {}", pspSchemeResponseBody);
        if (pspSchemeResponseBody == null || pspSchemeResponseBody.getPspSchemas() == null) {
            LOGGER.error("Exception occurred while fetching psp app data from mapping");
            return null;
        }
        FetchPspAppsResponseBody fetchPspAppsResponseBody = new FetchPspAppsResponseBody();
        fetchPspAppsResponseBody.setPspSchemas(pspSchemeResponseBody.getPspSchemas());
        return fetchPspAppsResponseBody;
    }

    private FetchPspAppsResponse generateErrorResponse() {
        FetchPspAppsResponse fetchPspAppsResponse = new FetchPspAppsResponse(new ResponseHeader(),
                new FetchPspAppsResponseBody());
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.SYSTEM_ERROR.getCode());
        resultInfo.setResultMsg(ResultCode.SYSTEM_ERROR.getResultMsg());
        resultInfo.setResultStatus(ResultCode.SYSTEM_ERROR.getResultStatus());
        resultInfo.setResultCodeId(ResultCode.SYSTEM_ERROR.getResultCodeId());
        fetchPspAppsResponse.getBody().setResultInfo(resultInfo);
        return fetchPspAppsResponse;
    }

    private FetchPspAppsResponse generateSuccessResponse(FetchPspAppsResponse response) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.SUCCESS.getCode());
        resultInfo.setResultMsg(ResultCode.SUCCESS.getResultMsg());
        resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
        resultInfo.setResultCodeId(ResultCode.SUCCESS.getResultCodeId());
        response.getBody().setResultInfo(resultInfo);
        return response;
    }
}
