/**
 *
 */
package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.statistics.StatisticConstants;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.*;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.models.AutoDebitRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.FetchQRPaymentDetailsRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.DynamicWrapperUtil;
import com.paytm.pgplus.theia.utils.InterceptorUtils;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.LINK_BUISNESS_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.PGP_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;

/**
 * @author kesari
 * @createdOn 28-Mar-2016
 */
@Component
public class TheiaInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaInterceptor.class);

    private static final Logger LOGGER_USER_AGENT = LoggerFactory.getLogger("USER_AGENT_LOGGER");

    @Autowired
    @Qualifier("aoaUtils")
    AOAUtils aoaUtils;

    @Autowired
    private InterceptorUtils interceptorUtils;

    @Autowired
    @Qualifier(value = "wrapperImpl")
    private IWrapperService wrapperService;

    @Autowired
    DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    private static final List<String> forbiddenCharacters = new ArrayList<>(Arrays.asList("<", ">"));

    private static final List<String> forbiddenCharactersForOidAndMid = new ArrayList<>(Arrays.asList("<", ">", "\\",
            "%", "/", "script"));

    private static final List<String> whitelistedParams = new ArrayList<>(Arrays.asList(RequestParams.CHECKSUMHASH,
            RequestParams.PAYMENT_DETAILS, "STATUS_API", RequestParams.CALLBACK_URL, RequestParams.PEON_URL,
            "JsonData", "orderid"));
    private static final String HEALTH_CHECK_URL = "/theia/healthcheck";
    private static final String NATIVE_CORS_URL = "/theia/api/v";
    private static final String OPTIONS = "OPTIONS";
    private static final String TRANS_STATUS_CORS_URL = "/theia/v1/transactionStatus";
    private static final String UPI_TRANS_STATUS_CORS_URL = "/theia/upi/transactionStatus";

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#
     * afterCompletion(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.Object,
     * java.lang.Exception)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
        /*
         * Statistics loggers
         */
        try {
            logMerchantResponse(request, response);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Writing Response Stats. Data ::", e);
        }

        if (request.getSession(false) != null) {
            for (SessionDataAttributes sessionDataAttributes : SessionDataAttributes.values()) {
                LOGGER.debug("{} : {}", sessionDataAttributes,
                        request.getSession().getAttribute(sessionDataAttributes.name()));
            }
        }

        MDC.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#
     * afterConcurrentHandlingStarted(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#
     * postHandle (javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.Object,
     * org.springframework.web.servlet.ModelAndView)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);

        if ((request.getRequestURI().startsWith(NATIVE_CORS_URL)
                || request.getRequestURI().equals(TRANS_STATUS_CORS_URL) || request.getRequestURI().equals(
                UPI_TRANS_STATUS_CORS_URL))
                && OPTIONS.equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.SC_OK);
        }
        if ((request.getRequestURI().startsWith("/theia/linkPayment/generateSendOTP")
                || request.getRequestURI().startsWith("/theia/linkPayment/validateSendOTP") || request.getRequestURI()
                .startsWith("/theia/processTransaction"))
                && OPTIONS.equalsIgnoreCase(request.getMethod())
                && (request.getHeader("Origin").equalsIgnoreCase(
                        ConfigurationUtil.getProperty(LINK_BUISNESS_URL, "https://paytm.business")) || request
                        .getHeader("origin").equalsIgnoreCase(
                                ConfigurationUtil.getProperty(LINK_BUISNESS_URL, "https://paytm.business")))) {
            LOGGER.info("inside origin {}", ConfigurationUtil.getProperty(LINK_BUISNESS_URL, "https://paytm.business"));
            // response.setHeader("Access-Control-Allow-Origin",
            // ConfigurationUtil.getProperty(LINK_BUISNESS_URL,
            // "https://paytm.business"));
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");
            response.setHeader("Connection", "Keep-Alive");
            response.setStatus(HttpStatus.SC_OK);
        }

        if (modelAndView != null) {
            modelAndView.addObject(ModelViewAttributes.pagepath.name(), modelAndView.getViewName());
            modelAndView.addObject(ModelViewAttributes.useMinifiedAssets.name(),
                    ConfigurationUtil.getProperty("context.useMinifiedAssets", "N").equals("Y"));
            modelAndView.addObject(ModelViewAttributes.staticResourceVersion.name(),
                    ConfigurationUtil.getProperty("context.useMinifiedAssets"));
            modelAndView.addObject(ModelViewAttributes.jvmRoute.name(),
                    request.getParameter(ModelViewAttributes.jvmRoute.name()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#
     * preHandle (javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }

        setMDC(request);
        setOrderIdInReqAttribute(request);
        setOrderIdInMDCIfNotExist(request);
        setMidInReqAttribute(request);
        setUniquePGPId(request);
        setMDCFromQueryParam(request);

        if (!request.getRequestURI().equals(HEALTH_CHECK_URL)) {
            LOGGER_USER_AGENT
                    .info("Request url :{}, User-Agent:{}, X-Forwarded-For :{}, Referer :{}, X-PGP-Unique-ID : {}, SECURE-TLS : {}",
                            request.getRequestURL(), request.getHeader(RequestHeaders.USER_AGENT),
                            request.getHeader(RequestHeaders.X_FORWARDED_FOR),
                            request.getHeader(RequestHeaders.REFERER),
                            request.getHeader(RequestHeaders.X_PGP_UNIQUE_ID),
                            request.getHeader(RequestHeaders.TRACK_TLS));
        }

        /** All the security check only handled by XSSFilter */
        // checkForScriptedParams(request);

        logMerchantRequest(request);
        handleTLS(request);

        // Merchant Migrated. Configuration.
        return super.preHandle(request, response, handler);
    }

    private void setUniquePGPId(HttpServletRequest request) {
        String pgpId = request.getHeader(RequestHeaders.X_PGP_UNIQUE_ID);
        if (StringUtils.isNotBlank(pgpId)) {
            MDC.put(PGP_ID, pgpId);
        }
    }

    private void setOrderIdInMDCIfNotExist(HttpServletRequest request) {
        if (StringUtils.isBlank(MDC.get(RequestParams.ORDER_ID))) {
            LOGGER.debug("OrderID in MDC not found.");
            Object orderId = request.getAttribute(RequestParams.ORDER_ID);
            if (orderId != null) {
                MDC.put(RequestParams.ORDER_ID, (String) orderId);
            }
        }
    }

    private void handleTLS(HttpServletRequest request) {
        if (request.getHeader(RequestHeaders.TRACK_TLS) != null) {
            LOGGER.info("Insecure Request : UA {}", request.getHeader(RequestHeaders.USER_AGENT));
            request.setAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED, getTLSDisabledPaymode());
        }
    }

    private String getTLSDisabledPaymode() {
        StringBuilder sb = new StringBuilder();
        sb.append(EPayMethod.SAVED_CARD.getOldName()).append(",").append(EPayMethod.CREDIT_CARD.getOldName())
                .append(",").append(EPayMethod.DEBIT_CARD.getOldName()).append(",").append(EPayMethod.EMI.getOldName());
        return sb.toString();
    }

    /**
     * @param request
     */

    public void setOrderIdInReqAttribute(HttpServletRequest request) {
        request.setAttribute(TheiaConstant.RequestParams.ORDER_ID, getOrderId(request));
    }

    private void setMidInReqAttribute(HttpServletRequest request) {
        String mid = getMid(request);
        if (StringUtils.isNotBlank(mid))
            request.setAttribute(MID, mid);
    }

    private String getOrderIdByWrapRequest(HttpServletRequest request) {
        LOGGER.debug("inside getOrderIdByWrapRequest");
        String mid = request.getParameter(MID);

        if (request != null && ExtraConstants.CUSTOM_PROCESS_TRANSACTION_URL.equals(request.getRequestURI())
                && dynamicWrapperUtil.isDynamicWrapperEnabled()
                && dynamicWrapperUtil.isDynamicWrapperConfigPresent(mid, API.PROCESS_TRANSACTION, PayloadType.REQUEST)) {
            LOGGER.info("Getting order Id by wrapping request");
            try {
                PaymentRequestBean paymentRequestBean = wrapperService.wrapRequest(request, mid,
                        API.PROCESS_TRANSACTION);
                return paymentRequestBean.getOrderId();
            } catch (Exception e) {
                LOGGER.error("Not able to wrap request for session {}", ExceptionUtils.getStackTrace(e));
                return null;
            }
        }
        return null;
    }

    private String getOrderId(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getParameter("ORDER_ID"))) {
            return request.getParameter("ORDER_ID");
        } else if (StringUtils.isNotBlank(request.getParameter("orderId"))) {
            return request.getParameter("orderId");
        } else if (StringUtils.isNotBlank(request.getParameter("order_id"))) {
            return request.getParameter("order_id");
        } else if (StringUtils.isNotBlank(request.getParameter("orderid"))) {
            return request.getParameter("orderid");
        } else if (StringUtils.isNotBlank(request.getParameter("ORDERID"))) {
            return request.getParameter("ORDERID");
        } else if (StringUtils.isNotBlank(request.getParameter("id"))) {
            return request.getParameter("id");
        } else
            return getOrderIdByWrapRequest(request);
    }

    private String getMid(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getParameter("MID"))) {
            return request.getParameter("MID");
        } else if (StringUtils.isNotBlank(request.getParameter("mid"))) {
            return request.getParameter("mid");
        }
        return MDC.get(MID);
    }

    private void setMDC(HttpServletRequest request) {
        setMDCDefault(request);
        setMDCForAutoDebit(request);
        setTxnStatusControllerMDC(request);
        setMDCForFetchPaymentInstrument(request);
        setMDCForFetchQRPaymentDetails(request);
    }

    private void setMDCFromQueryParam(HttpServletRequest request) {
        String mid = "";
        String orderId = "";
        String query = request.getQueryString();
        String lowerCaseQuery;

        if (StringUtils.isNotBlank(query)) {
            lowerCaseQuery = query.toLowerCase();
            if (StringUtils.isBlank(MDC.get(MID)) && lowerCaseQuery.contains(RequestParams.MID_LOWER_CASE)) {
                mid = parseParam(query, lowerCaseQuery, RequestParams.MID_LOWER_CASE);
                if (StringUtils.isNotBlank(mid)) {
                    MDC.put(MID, mid);
                }
            }

            if (StringUtils.isBlank(MDC.get(TheiaConstant.RequestParams.ORDER_ID))) {
                if (lowerCaseQuery.contains(RequestParams.ORDERID_LOWER_CASE)) {
                    orderId = parseParam(query, lowerCaseQuery, RequestParams.ORDERID_LOWER_CASE);
                } else if (lowerCaseQuery.contains(RequestParams.ORDER_ID_LOWER_CASE)) {
                    orderId = parseParam(query, lowerCaseQuery, RequestParams.ORDER_ID_LOWER_CASE);
                }
                if (StringUtils.isNotBlank(orderId)) {
                    MDC.put(RequestParams.ORDER_ID, orderId);
                }
            }
            LOGGER.info("data parsed from query param mid : {} and orderId :: {} ", mid, orderId);
        }
    }

    private String parseParam(String query, String lowerCaseQuery, String param) {
        int startIndex = lowerCaseQuery.indexOf(param);
        int endIndex = lowerCaseQuery.indexOf("&", startIndex);
        if (endIndex > 0) {
            return query.substring(startIndex + param.length() + 1, endIndex);
        } else {
            return query.substring(startIndex + param.length() + 1);
        }
    }

    private void setMDCDefault(HttpServletRequest request) {
        String mid = request.getParameter(MID);
        /** Hack to Support all merchant */
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID1);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID2);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(Native.MID);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(MERCHANT_ID);
        }

        String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
        if (StringUtils.isBlank(orderId)) {
            orderId = request.getParameter(Native.ORDER_ID);
        }
        // getting mid as merchantCode for odisha task
        if (StringUtils.isBlank(mid)) {
            mid = (String) request.getParameter("merchantCode");
        }

        setMDCDefault(mid, orderId);
    }

    private void setMDCDefault(final String mid, final String orderId) {
        if (!StringUtils.isBlank(mid)) {
            MDC.put(MID, mid);
        }

        if (!StringUtils.isBlank(orderId)) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, orderId);
        }
    }

    private void setMDCDefault(final String mid, final String orderId, final String transId) {
        setMDCDefault(mid, orderId);

        if (!StringUtils.isBlank(transId)) {
            MDC.put(TheiaConstant.RequestParams.TXN_ID, transId);
        }
    }

    private void checkForScriptedParams(HttpServletRequest request) throws Exception {
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (!whitelistedParams.contains(entry.getKey())) {
                String value = entry.getValue()[0];
                if (!(entry.getKey().equals("ORDER_ID") || entry.getKey().equals("MID"))) {
                    for (String forbiddenCharacter : forbiddenCharacters) {
                        if (value.contains(forbiddenCharacter)) {
                            String errorMessage = new StringBuilder().append("Parameter named ").append(entry.getKey())
                                    .append(" is a suspect for XSS. Value received : ").append(value)
                                    .append(", Suspect Char received : ").append(forbiddenCharacter).toString();
                            throw new Exception(errorMessage);
                        }
                    }
                } else {
                    for (String forbiddenCharacter : forbiddenCharactersForOidAndMid) {
                        if (value.contains(forbiddenCharacter)) {
                            String errorMessage = new StringBuilder().append("Parameter named ").append(entry.getKey())
                                    .append(" is a suspect for XSS. Value received : ").append(value)
                                    .append(", Suspect Char received : ").append(forbiddenCharacter).toString();
                            request.setAttribute(TheiaConstant.RequestParams.ORDER_ID, null);
                            throw new Exception(errorMessage);
                        }
                    }
                }
            }
        }
    }

    public void setTxnStatusControllerMDC(HttpServletRequest request) {
        if (ExtraConstants.TRANSACTION_STATUS_URL.equals(request.getRequestURI())
                || ExtraConstants.UPI_TRANSACTION_STATUS_URL.equals(request.getRequestURI())
                || ExtraConstants.DIRECT_CARD_PAYMENT.equals(request.getRequestURI())) {
            String transId = request.getParameter(ExtraConstants.TRANS_ID);

            if (StringUtils.isNotBlank(transId)) {
                TransactionInfo txnInfo = transactionCacheUtils.getTransInfoFromCache(transId);
                if (txnInfo != null) {
                    setMDCDefault(txnInfo.getMid(), txnInfo.getOrderId(), transId);
                } else {
                    MDC.put(TheiaConstant.RequestParams.TXN_ID, transId);
                }
            }
        }

    }

    private void logMerchantRequest(HttpServletRequest request) throws FacadeCheckedException,
            UnsupportedEncodingException {

        String[] midRequestType = interceptorUtils.fetchMidRequestType(request);

        String mid = midRequestType[0];
        String requestType = midRequestType[1];
        String api = null;

        if (request == null) {
            return;
        }

        if (StringUtils.isNotBlank(request.getRequestURI())) {
            api = StringUtils.substringAfterLast(request.getRequestURI(), TheiaConstant.ExtraConstants.FORWARD_SLASH);
        }

        if (StringUtils.isNotBlank(mid)) {
            String destination = StatisticConstants.PGPLUS;
            StatisticsLogger.logMerchantRequest(mid, api, destination, requestType);
            request.setAttribute(TheiaConstant.ExtraConstants.START_TIME, System.currentTimeMillis());
        }
    }

    private void logMerchantResponse(HttpServletRequest request, HttpServletResponse response)
            throws FacadeCheckedException, UnsupportedEncodingException {
        if ((!request.getRequestURI().matches(TheiaConstant.ExtraConstants.NATIVE_BASE_API_REGEX) && request
                .getSession(false) == null) || request.getAttribute(TheiaConstant.ExtraConstants.START_TIME) == null) {
            return;
        }

        String[] midRequestType = interceptorUtils.fetchMidRequestType(request);

        String mid = midRequestType[0];
        String requestType = midRequestType[1];
        String destination = StatisticConstants.PGPLUS;

        long startTime = Long.parseLong(request.getAttribute(TheiaConstant.ExtraConstants.START_TIME).toString());
        String timeElapsed = String.valueOf(System.currentTimeMillis() - startTime);

        String api = null;
        if (StringUtils.isNotBlank(request.getRequestURI())) {
            api = StringUtils.substringAfterLast(request.getRequestURI(), TheiaConstant.ExtraConstants.FORWARD_SLASH);
        }

        String status = StatisticConstants.FAIL;
        if (response.getStatus() == 200) {
            status = StatisticConstants.SUCCESS;
        }

        StatisticsLogger.logMerchantResponse(mid, api, destination, requestType, timeElapsed, status);
    }

    /** Setting MDC Context For Auto Debit, as it has different request format */
    private void setMDCForAutoDebit(HttpServletRequest request) {
        if (!TheiaConstant.RequestParams.AUTO_DEBIT_URL.equals(request.getRequestURI())) {
            return;
        }
        if (request.getParameter("JsonData") == null) {
            LOGGER.warn("JsonData is null in AutoDebit request {}", request);
            return;
        }
        try {
            String decodedString = URLDecoder.decode(request.getParameter("JsonData"), "UTF-8");
            AutoDebitRequest requestData = JsonMapper.mapJsonToObject(decodedString, AutoDebitRequest.class);
            setMDCDefault(requestData.getMid(), requestData.getOrderId());
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error occurred while setting MDC Context for AutoDebitRequest ", e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error occurred while setting MDC Context for AutoDebitRequest ", e);
        }
    }

    private void setMDCForFetchPaymentInstrument(HttpServletRequest request) {
        if (!ExtraConstants.FETCH_PAYMENT_INSTRUMENT_URL.equals(request.getRequestURI())) {
            return;
        }

        try {
            String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            CashierInfoRequest requestData = JsonMapper.mapJsonToObject(content, CashierInfoRequest.class);
            String mid = requestData.getHead().getMid();
            String requestId = requestData.getHead().getRequestId();
            if (StringUtils.isNotBlank(mid)) {
                MDC.put(MID, mid);
            }
            if (StringUtils.isNotBlank(requestId)) {
                MDC.put(TheiaConstant.RequestParams.REQUEST_ID, requestId);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error occurred while setting MDC Context for FetchPaymentInstrument ", e);
        }
    }

    /**
     * Setting MDC Context For FetchQRPaymentDetails, as it has different
     * request format
     */

    private void setMDCForFetchQRPaymentDetails(HttpServletRequest request) {
        if (!ExtraConstants.FETCH_QR_PAYMENT_DETAILS.equals(request.getRequestURI())) {
            return;
        }

        try {
            String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            FetchQRPaymentDetailsRequest requestData = JsonMapper.mapJsonToObject(content,
                    FetchQRPaymentDetailsRequest.class);
            String requestId = requestData.getHead().getRequestId();
            if (StringUtils.isNotBlank(requestId)) {
                MDC.put(TheiaConstant.RequestParams.REQUEST_ID, requestId);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error occurred while setting MDC Context for FetchQRPaymentDetails ", e);
        }
    }

}