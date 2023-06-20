package com.paytm.pgplus.biz.workflow.valservice.service.impl;

import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnRequest;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnResponse;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnResponseBody;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.valservice.helper.ValidationServiceHelper;
import com.paytm.pgplus.biz.workflow.valservice.service.IValidationService;
import com.paytm.pgplus.facade.enums.EdcLinkValidationServiceUrl;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.paytm.pgplus.dynamicwrapper.utils.JSONUtils.toJsonString;

@Service("validationServiceImpl")
public class ValidationServiceImpl implements IValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceImpl.class);

    @Autowired
    private ValidationServiceHelper validationServiceHelper;

    @Override
    public GenericCoreResponseBean<ValidationServicePreTxnResponse> executePreTxnValidationModel(
            WorkFlowTransactionBean workflowTxnBean) throws EdcLinkBankAndBrandEmiCheckoutException {
        long startTime = System.currentTimeMillis();
        ValidationServicePreTxnResponse validationServicePreTxnResponse = null;
        try {
            ValidationServicePreTxnRequest validationServicePreTxnRequest = validationServiceHelper
                    .getValidationServicePreTxnRequest(workflowTxnBean);
            LOGGER.info("validation Mode pre txn Request received {} ", validationServicePreTxnRequest);
            final Map<String, String> queryMap = validationServiceHelper.prepareQueryParams(workflowTxnBean);
            final MultivaluedMap<String, Object> headerMap = validationServiceHelper.prepareHeaderMap(workflowTxnBean);
            validationServicePreTxnResponse = executePostV2(validationServicePreTxnRequest,
                    EdcLinkValidationServiceUrl.VALIDATION_MODEL_PRE_TXN.getUrl(),
                    ValidationServicePreTxnResponse.class, queryMap, headerMap, ExternalEntity.VALIDATION_MODEL);
            boolean isValidated = validateValidationModelResponse(validationServicePreTxnResponse);
            if (!isValidated && validationServicePreTxnResponse.getBody() != null
                    && validationServicePreTxnResponse.getBody().getResultInfo() != null) {
                throw new EdcLinkBankAndBrandEmiCheckoutException(validationServicePreTxnResponse.getBody()
                        .getResultInfo().getResultMsg(), ResponseConstants.VALIDATION_API_FAILURE);
            } else {
                return new GenericCoreResponseBean<>(validationServicePreTxnResponse);
            }
        } catch (Exception e) {
            throw new EdcLinkBankAndBrandEmiCheckoutException(
                    BizConstant.EdcLinkEmiTxn.EXCEPTION_MESSAGE_VALIDATION_API,
                    ResponseConstants.VALIDATION_API_FAILURE);
        } finally {
            LOGGER.info("Response received from /executePreTxnValidationModel API : {} ",
                    validationServicePreTxnResponse);
            LOGGER.info("Total time taken by /executePreTxnValidationModel API : {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    public boolean validateValidationModelResponse(ValidationServicePreTxnResponse validationServicePreTxnResponse) {
        if (validationServicePreTxnResponse == null || validationServicePreTxnResponse.getBody() == null)
            return false;
        else {
            ValidationServicePreTxnResponseBody responseBody = validationServicePreTxnResponse.getBody();
            if (responseBody.getResultInfo() != null
                    && "S".equalsIgnoreCase(responseBody.getResultInfo().getResultStatus())) {
                return true;
            }
        }
        return false;
    }

    private <Req, Resp> Resp executePostV2(Req request, String url, Class<Resp> respClass,
            Map<String, String> queryParams, MultivaluedMap<String, Object> headerMap, ExternalEntity externalEntity)
            throws FacadeCheckedException {
        long startTime = System.currentTimeMillis();
        final HttpRequestPayload<String> payload = generatePayloadV2(request, url, headerMap, queryParams);
        try {
            LogUtil.logPayload(externalEntity, url, Type.REQUEST, payload.toString());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);

            final String responseEntity = response.readEntity(String.class);
            final Resp responseObject = JsonMapper.mapJsonToObject(responseEntity, respClass);
            String responseString = toJsonString(responseObject);
            LogUtil.logResponsePayload(externalEntity, url, Type.RESPONSE, responseString, startTime);
            return responseObject;
        } catch (final Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private <T> HttpRequestPayload<String> generatePayloadV2(final T request, String url,
            MultivaluedMap<String, Object> headerMap, Map<String, String> queryParams) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        payload.setTarget(url);
        payload.setHeaders(headerMap);
        payload.setHttpMethod(HttpMethod.POST);
        payload.setQueryParameters(queryParams);
        String requestBody = generateBody(request);
        payload.setEntity(requestBody);
        return payload;
    }

    private <T> String generateBody(final T request) throws FacadeCheckedException {
        if (request == null)
            return null;
        return JsonMapper.mapObjectToJson(request);
    }
}
