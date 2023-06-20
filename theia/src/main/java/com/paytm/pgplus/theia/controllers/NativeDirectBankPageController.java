package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_DIRECT_BANK_REQUEST;

@RestController
@RequestMapping("api/v1")
public class NativeDirectBankPageController implements Serializable {

    private static final long serialVersionUID = 162115942318072195L;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeDirectBankPageController.class);

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = V1_DIRECT_BANK_REQUEST, responseClass = NativeJsonResponse.class, isResponseObjectType = false)
    @RequestMapping(value = "/directBankRequest", method = { RequestMethod.POST })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void processDirectBankPageRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        long startTime = System.currentTimeMillis();
        /*
         * This API handles request for submit, cancel and resend from
         * directBankPage
         */
        NativeJsonResponse response = null;
        NativeDirectBankPageRequest nativeDirectBankPageRequest = null;

        try {

            String requestData = IOUtils.toString(httpRequest.getInputStream(), Charsets.UTF_8.name());
            Map<String, String> content = JsonMapper.mapJsonToObject(requestData, Map.class);

            if (content == null) {
                LOGGER.error("content is null in directBankPage Request");
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isNativeJsonRequest(true).build();
            }

            nativeDirectBankPageRequest = nativeDirectBankPageHelper.createNativeDirectBankPageRequest(content);

            LOGGER.info("nativeDirectBankPageRequest received: {}", nativeDirectBankPageRequest);
            nativeDirectBankPageHelper.validateDirectBankPageRequest(nativeDirectBankPageRequest);

            nativeDirectBankPageRequest.setHttpServletRequest(httpRequest);

            String requestType = nativeDirectBankPageRequest.getBody().getRequestType();
            NativeDirectBankPageRequestType requestTypeEnum = NativeDirectBankPageRequestType.getType(requestType);

            if (submit == requestTypeEnum) {
                response = nativeDirectBankPageHelper.doSubmitRequest(nativeDirectBankPageRequest);
            }
            if (cancel == requestTypeEnum) {
                response = nativeDirectBankPageHelper.doCancelRequest(nativeDirectBankPageRequest);
            }
            if (resend == requestTypeEnum) {
                response = nativeDirectBankPageHelper.doResentOtpRequest(nativeDirectBankPageRequest);
            }
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/directBankRequest", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/directBankRequest" + "to grafana",
                        exception);
            }
            localeFieldAspect.addLocaleFieldsInObject(response, V1_DIRECT_BANK_REQUEST);
            String responseJson = getJsonString(response);

            // LOGGER.info("Json Response sent in nativeDirectBankPage {}",
            // responseJson);
            EXT_LOGGER.customInfo("Json Response sent in nativeDirectBankPage {}", responseJson);

            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(responseJson);
            nativePaymentUtil.logNativeResponse(response != null ? (response.getBody() != null ? response.getBody()
                    .getResultInfo() : null) : null);
        } catch (NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in directBankRequest {}", ExceptionUtils.getStackTrace(nfe));
            throw nfe;
        } catch (Exception e) {
            LOGGER.error("Exception in directBankRequest {}", ExceptionUtils.getStackTrace(e));
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                    .isNativeJsonRequest(true).build();
        }
    }

    private String getJsonString(NativeJsonResponse nativeJsonResponse) {
        String respJsonString = "{}";
        try {
            respJsonString = JsonMapper.mapObjectToJson(nativeJsonResponse);
        } catch (FacadeCheckedException fce) {
            LOGGER.info("failed mapping object to Json");
        }
        return respJsonString;
    }
}
