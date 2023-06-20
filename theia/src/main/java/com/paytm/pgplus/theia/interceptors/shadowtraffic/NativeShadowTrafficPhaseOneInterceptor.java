package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ErrorResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Shadow traffic phase 1 interceptor for Native flow
 */
public class NativeShadowTrafficPhaseOneInterceptor extends ShadowTrafficPhaseOneInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeShadowTrafficPhaseOneInterceptor.class);

    /**
     * @see ShadowTrafficPhaseOneInterceptor#preHandle(HttpServletRequest,
     *      HttpServletResponse, Object)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }

        String mid = request.getParameter("mid");

        // If the MID is present in request with "mid" as key, try processing
        // our interceptor. Else, passthrough to lower interceptors.
        if (StringUtils.isNotBlank(mid)) {
            if (!isRequestAllowed(request, mid)) {
                String message = "There is a configuration mismatch for shadow request";
                writeErrorResponse(response, message);
                return false;
            }
        }
        return true;
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponse errorResponse = getErrorResponse(message);
        String errorResponseString = null;
        try {
            errorResponseString = JsonMapper.mapObjectToJson(errorResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while mapping object to json", e);
            throw new BaseException();
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(errorResponseString);
    }

    private ErrorResponse getErrorResponse(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.CONFIGURATION_MISMATCH_SHADOW_REQUEST);
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
}
