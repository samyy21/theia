package com.paytm.pgplus.theia.response.handler;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.response.interfaces.SecureResponse;
import com.paytm.pgplus.theia.annotation.SignedResponseBody;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * This Handler is to add signature in the response returned from controller
 * annotated with {@link SignedResponseBody} .
 *
 */
public class AddSignatureReturnValueHandler implements HandlerMethodReturnValueHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddSignatureReturnValueHandler.class);

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.getMethodAnnotation(SignedResponseBody.class) != null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {
        SecureResponse response = (SecureResponse) returnValue;
        if (null != response) {
            if (response.getBody().getResultInfo().getResultStatus().equals("S")) {
                String mid = getMid(webRequest);
                String childMid = mid;
                mid = getAggrMid(webRequest, mid);
                Map<String, Object> context = new HashMap<>();
                context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
                if (iPgpFf4jClient.checkWithdefault(TheiaConstant.ExtraConstants.CREATE_CHECKSUM_WITH_AGGR_MID,
                        context, false)) {
                    mid = childMid;
                }
                String responseBody = JsonMapper.mapObjectToJson(response.getBody());
                responseBody = StringUtils.normalizeSpace(responseBody).concat("|");
                String clientId = response.getHead().getClientId();
                String signature = ValidateChecksum.getInstance()
                        .getRespCheckSumValue(getMerchantKey(mid, clientId), new StringBuilder(responseBody))
                        .get("CHECKSUMHASH");
                response.getHead().setSignature(signature);
            }
            HttpServletResponse httpResponse = webRequest.getNativeResponse(HttpServletResponse.class);
            String responseString = JsonMapper.mapObjectToJson(response);
            responseString = StringUtils.normalizeSpace(responseString);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            mavContainer.setRequestHandled(true);
            httpResponse.getWriter().write(responseString);
        }
    }

    private String getAggrMid(NativeWebRequest webRequest, String mid) {
        if (webRequest.getNativeRequest(HttpServletRequest.class).getRequestURI().contains("initiateTransaction")) {
            try {
                InitiateTransactionRequest request = JsonMapper.mapJsonToObject(
                        IOUtils.toString(webRequest.getNativeRequest(HttpServletRequest.class).getInputStream()),
                        InitiateTransactionRequest.class);

                if (StringUtils.isNotBlank((request.getBody().getAggMid()))) {
                    LOGGER.info("Aggregator Mid Chosen for Response Checksum {}", request.getBody().getAggMid());
                    return request.getBody().getAggMid();
                }
            } catch (com.paytm.pgplus.facade.exception.FacadeCheckedException | IOException ignored) {
                LOGGER.error("Error occured while adding aggMid for Response Checksum");
            }
        }
        return mid;
    }

    private String getMid(NativeWebRequest webRequest) {
        return webRequest.getParameter("mid");
    }

    private String getMerchantKey(String mid, String clientId) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }
}