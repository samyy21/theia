package com.paytm.pgplus.theia.nativ.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ErrorResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_FETCH_PAYMODE_STATUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIV_INITIATE_TRANSACTION_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.SUPERGW_VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.ResponseCodeMessages.REFERENCE_ID_LENGTH_VALIDATION_MSG;

public class QueryParamValidationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParamValidationFilter.class);
    private static String apiNamesToLogParams = ConfigurationUtil.getProperty(
            TheiaConstant.ExtraConstants.LOG_API_REQUEST_PARAMS_IN_FILTER, "");
    private static List<String> listOfApiNamesToLogParams = Arrays.asList(apiNamesToLogParams.split(",")).stream()
            .map(s -> s.trim()).filter(s -> s.length() > 0).collect(Collectors.toList());
    boolean isMidEligibleForLogging = false;
    boolean isMidEligibleForErrorReturn = false;

    private static Ff4jUtils ff4jUtils;

    public void init() {
        try {
            if (ff4jUtils == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while initializing ", ex);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        init();

        String tokenType = "";
        String merchant_id = "";
        String requestID = "";
        String reference_id = "";
        String token = "";
        String qrcode_id = "";
        String txnAmount = "";

        String mid = request.getParameter(Native.MID);
        String orderId = request.getParameter(Native.ORDER_ID);
        String pgpId = request.getHeader(X_PGP_UNIQUE_ID);

        try {
            isMidEligibleForLogging = AllowedMidCustidPropertyUtil.isMidCustIdEligible(mid, CommonConstants.ALL,
                    "eligible.mids.for.logging.at.filter", CommonConstants.NONE, Boolean.FALSE);
            isMidEligibleForErrorReturn = AllowedMidCustidPropertyUtil.isMidCustIdEligible(mid, CommonConstants.ALL,
                    "return.error.from.filter.mid.list", CommonConstants.NONE, Boolean.FALSE);
        } catch (Exception e) {
            LOGGER.info("Exception occured while checking isMidEligibleForLogging, e : {}", e);
        }

        if (!(request instanceof MultiReadHttpServletRequestWrapper)) {
            request = new MultiReadHttpServletRequestWrapper(request);
        }

        mid = StringUtils.isBlank(mid) ? request.getParameter(Native.MID) : mid;
        orderId = StringUtils.isBlank(orderId) ? request.getParameter(Native.ORDER_ID) : orderId;

        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        if (isMidEligibleForLogging) {
            LOGGER.info("inside queryParam filter, request is : {} , content is : {} ", request, content);
        }
        try {
            JSONObject json = new JSONObject(content);
            JSONObject head = json.getJSONObject("head");
            JSONObject body = json.getJSONObject("body");
            merchant_id = (body.has(Native.MID)) ? body.getString(Native.MID) : null;
            reference_id = (body.has(Native.REFERENCE_ID)) ? body.getString(Native.REFERENCE_ID) : null;
            tokenType = (head.has(Native.TOKEN_TYPE)) ? head.getString(Native.TOKEN_TYPE) : null;
            token = (head.has(Native.TOKEN)) ? head.getString(Native.TOKEN) : null;
            qrcode_id = (body.has(Native.QRCODE_ID)) ? body.getString(Native.QRCODE_ID) : null;
            if (StringUtils.isBlank(orderId)) {
                orderId = (body.has(Native.ORDER_ID)) ? body.getString(Native.ORDER_ID) : null;
                request.setAttribute(Native.ORDER_ID, orderId);
            }
            requestID = (head.has(Native.REQUEST_ID)) ? head.getString(Native.REQUEST_ID) : null;
            if (body.has(Native.TXN_AMOUNT_IN_FETCH_PAYMODE_STATUS_API)) {
                Object object = body.get(Native.TXN_AMOUNT_IN_FETCH_PAYMODE_STATUS_API);
                if (object instanceof String) {
                    txnAmount = (String) object;
                }
            }
        } catch (JSONException e) {
            LOGGER.info("Invalid Json Request for mid: {}, orderId: {}, pgpId: {}, error: {}", mid, orderId, pgpId,
                    e.getMessage());
            if (isMidEligibleForErrorReturn) {
                writeErrorResponse(response, "Invalid Json Request");
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                return;
            }
        }
        logParametersAndAttributes(request, mid, merchant_id, orderId, content);

        if (request.getRequestURI().contains("v1/emiSubvention/validateEmi")
                || request.getRequestURI().contains("v1/emiSubvention/banks")
                || request.getRequestURI().contains("v1/emiSubvention/tenures")) {
            if (StringUtils.isBlank(requestID)) {
                String message = "requestId can't be blank";
                writeErrorResponse(response, message);
            } else if (StringUtils.isBlank(merchant_id) || StringUtils.isBlank(mid)) {
                String message = "mid can't be blank";
                writeErrorResponse(response, message);
            } else if (!mid.equals(merchant_id)) {
                String message = "mid passed in query params and request body does not match";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains("/v4/")) {
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if (StringUtils.isBlank(referenceId) || StringUtils.isBlank(mid)) {
                String message = "ReferenceId and Mid are mandatory in query parameter";
                writeErrorResponsev4(response, message);
            } else {
                filterChain.doFilter(request, response);
            }

        } else if (request.getRequestURI().contains("v1/validateBin")) {
            if (StringUtils.isBlank(mid)) {
                String message = "mid can't be blank";
                writeErrorResponse(response, message);
            } else if (!mid.equals(merchant_id)) {
                String message = "mid passed in query params and request body does not match";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }

        } else if (request.getRequestURI().contains("v1/fetchQRPaymentDetails")) {
            /*
             * Validation for fetchQRPayment Options
             */
            String appVersion = request.getParameter(Native.APP_VERSION);
            String client = request.getParameter(Native.CLIENT);
            if (StringUtils.isBlank(appVersion) || StringUtils.isBlank(client)) {
                String message = "AppVersion and client is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains("v1/card/enrollmentStatus")) {
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if ((StringUtils.isBlank(mid) || StringUtils.isBlank(referenceId))
                    && TokenType.ACCESS.name().equals(tokenType)) {
                String message = "mid and referenceId is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else if (StringUtils.isBlank(mid) && TokenType.CHECKSUM.name().equals(tokenType)) {
                String message = "mid is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains("v1/card/deEnroll")) {
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if ((StringUtils.isBlank(mid) || StringUtils.isBlank(referenceId))
                    && TokenType.ACCESS.name().equals(tokenType)) {
                String message = "mid and referenceId is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else if (StringUtils.isBlank(mid) && TokenType.CHECKSUM.name().equals(tokenType)) {
                String message = "mid is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }

        } else if (request.getRequestURI().contains("/fetchPaymentOptions")
                && (TokenType.ACCESS.name().equals(tokenType) || TokenType.CHECKSUM.name().equals(tokenType))) {
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if (StringUtils.isBlank(mid) || StringUtils.isBlank(referenceId)) {
                String message = "mid and referenceId are mandatory in query parameter";
                writeErrorResponse(response, message);
            } else if (!mid.equals(merchant_id)) {
                String message = "mid passed in query params and request body does not match";
                writeErrorResponse(response, message);
            } else if (TokenType.CHECKSUM.name().equals(tokenType)) {
                if (!referenceId.equals(reference_id)) {
                    String message = "referenceId passed in query params and request body does not match";
                    writeErrorResponse(response, message);
                } else if (referenceId.length() < 10 || referenceId.length() > 20) {
                    writeErrorResponse(response, REFERENCE_ID_LENGTH_VALIDATION_MSG);
                } else {
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains("v1/fetchCardIndexNo")
                && StringUtils.equals(request.getParameter(Native.CLIENT), "IN")) {
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if (StringUtils.isBlank(referenceId) && TokenType.JWT.getType().equals(tokenType)) {
                String message = "Request param validation failed";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains(NATIVE_FETCH_PAYMODE_STATUS)) {
            if (StringUtils.isNotBlank(txnAmount) && StringUtils.isBlank(mid)) {
                String message = "Mid is mandatory in query parameter";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains("v1/processTransaction") && StringUtils.isBlank(mid)
                && StringUtils.isNotBlank(qrcode_id)) {
            if (StringUtils.isNotBlank(orderId)) {
                filterChain.doFilter(request, response);
            } else if (TokenType.SSO.getType().equals(tokenType)) {
                request.setAttribute(Native.IS_ORDER_ID_NEED_TO_BE_GENERATED, true);
                filterChain.doFilter(request, response);
            } else {
                String message = "OrderId is mandatory in query parameter";
                writeErrorResponse(response, message);
            }
        } else if (StringUtils.isBlank(mid) && StringUtils.isBlank(orderId)) {
            String message = "Mid and OrderId are mandatory in query parameter";
            writeErrorResponse(response, message);
        } else if (StringUtils.isBlank(mid)) {
            String message = "Mid is mandatory in query parameter";
            writeErrorResponse(response, message);
        } else if ((!TokenType.ACCESS.getType().equals(tokenType) && !TokenType.SSO.getType().equals(tokenType)
                && !TokenType.GUEST.getType().equals(tokenType) && !TokenType.CHECKSUM.getType().equals(tokenType))
                && StringUtils.isBlank(orderId)) {
            String message = "OrderId is mandatory in query parameter";
            writeErrorResponse(response, message);

        } else if (TokenType.SSO.getType().equals(tokenType) && StringUtils.isBlank(orderId)
                && request.getRequestURI().contains("v1/processTransaction")) {

            request.setAttribute(Native.IS_ORDER_ID_NEED_TO_BE_GENERATED, true);
            filterChain.doFilter(request, response);
        } else if (request.getRequestURI().contains("v1/fetchBinDetail")
                && TokenType.ACCESS.getType().equals(tokenType)) {
            /*
             * Validation for v1/fetchBinDetail
             */
            String referenceId = request.getParameter(Native.REFERENCE_ID);
            if (StringUtils.isBlank(referenceId) || !StringUtils.equals(mid, merchant_id)) {
                String message = "ReferenceId and Mid are mandatory in query parameter";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else if (request.getRequestURI().contains(NATIV_INITIATE_TRANSACTION_URL)) {
            if (TokenType.JWT.getType().equals(tokenType) && StringUtils.isBlank(token)) {
                String message = "Token is mandatory for tokenType: JWT";
                writeErrorResponse(response, message);
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponse errorResponse = getErrorResponse(message);
        String errorResponseString = null;
        try {
            errorResponseString = JsonMapper.mapObjectToJson(errorResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while map object to json", e);
            throw new BaseException();
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(errorResponseString);
    }

    private ErrorResponse getErrorResponse(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.MISSING_MANDATORY_ELEMENT);
        resultInfo.setResultMsg(message);
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        return errorResponse;
    }

    private BaseResponseBody baseResponseBody(ResultInfo resultInfo) {
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }

    private void writeErrorResponsev4(HttpServletResponse response, String message) throws IOException {
        ErrorResponse errorResponse = getErrorResponse(message);
        errorResponse.getHead().setVersion(SUPERGW_VERSION);
        String errorResponseString = null;
        try {
            errorResponseString = JsonMapper.mapObjectToJson(errorResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while map object to json", e);
            throw new BaseException();
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(errorResponseString);
    }

    private void logParametersAndAttributes(HttpServletRequest request, String mid, String merchant_id, String orderId,
            String content) {
        try {
            if (CollectionUtils.isNotEmpty(listOfApiNamesToLogParams)
                    && listOfApiNamesToLogParams.contains(request.getRequestURI())
                    && ((StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) || isMidEligibleForLogging)) {

                Map<String, Object> logData = new HashMap<String, Object>();

                logData.put("URI", request.getRequestURI());
                logData.put("queryString", request.getQueryString());
                logData.put("requestObjectClassname", request.getClass().getName());

                Map<String, Object> filterData = new HashMap<String, Object>();
                Map<String, ? extends FilterRegistration> filterRegistrations = request.getServletContext()
                        .getFilterRegistrations();
                if (filterRegistrations != null) {
                    for (Map.Entry<String, ? extends FilterRegistration> e : filterRegistrations.entrySet()) {
                        filterData.put(e.getKey(), e.getValue().getServletNameMappings().toString());
                    }
                }

                Map<String, Object> headerData = new HashMap<String, Object>();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        headerData.put(headerName, request.getHeader(headerName));
                    }
                }

                Map<String, Object> paramData = new HashMap<String, Object>();
                Map<String, String[]> requestParamtersMap = request.getParameterMap();
                if (requestParamtersMap != null) {
                    for (Map.Entry<String, String[]> entry : requestParamtersMap.entrySet()) {
                        paramData.put(entry.getKey(), Arrays.asList(entry.getValue()));
                    }
                }

                Map<String, Object> attributeData = new HashMap<String, Object>();
                Enumeration<String> requestAttributes = request.getAttributeNames();
                if (requestAttributes != null) {
                    while (requestAttributes.hasMoreElements()) {
                        String attributeKey = requestAttributes.nextElement();
                        attributeData.put(attributeKey, request.getAttribute(attributeKey));
                    }
                }

                logData.put("filterMap", filterData);
                logData.put("headerMap", headerData);
                logData.put("parameterMap", paramData);
                logData.put("attributeMap", attributeData);

                LOGGER.info(
                        "logParametersAndAttributes ::  mid : {}, merchant_id : {}, orderId : {}, content : {}, logData : {}",
                        mid, merchant_id, orderId, content, logData);

            }

        } catch (Exception e) {
            LOGGER.error("Exception Occurred : {}", e);
        }
    }
}
