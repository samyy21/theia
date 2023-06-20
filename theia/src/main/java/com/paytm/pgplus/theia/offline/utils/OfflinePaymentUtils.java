package com.paytm.pgplus.theia.offline.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.requestidclient.IdManager;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.offline.constants.FrontEndMsgConstant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdGenerationException;
import com.paytm.pgplus.theia.offline.model.base.BaseBody;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;

/**
 * Created by rahulverma on 30/8/17.
 */
public class OfflinePaymentUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflinePaymentUtils.class);
    private static final String REQUEST_HEADER_KEY = "REQUEST_HEADER";

    public static ResultInfo resultInfo(ResultCode resultCode) {
        if (resultCode == null)
            resultCode = ResultCode.UNKNOWN_ERROR;
        return new ResultInfo(resultCode.getResultStatus(), resultCode.getResultCodeId(), resultCode.getCode(),
                resultCode.getResultMsg());
    }

    public static ResultInfo resultInfoForSuccess() {
        return resultInfo(ResultCode.SUCCESS);
    }

    public static HttpServletRequest gethttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse gethttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public static String successRateMsg(boolean successRateFlag) {
        return successRateFlag ? FrontEndMsgConstant.SUCCESS_RATE_LOW_MSG : "";
    }

    public static void setRequestHeader(RequestHeader requestHeader) {
        gethttpServletRequest().setAttribute(REQUEST_HEADER_KEY, requestHeader);
    }

    public static RequestHeader getRequestHeader() {
        return (RequestHeader) gethttpServletRequest().getAttribute(REQUEST_HEADER_KEY);
    }

    public static ResponseHeader createResponseHeader() {
        return new ResponseHeader(getRequestHeader());
    }

    public static String generateOrderId(String mid) {
        LOGGER.debug("Generate orderId for mid =  {}", mid);
        try {
            String orderId = IdManager.getInstance().getOrderId(mid);
            LOGGER.debug("Generated orderId = {}", orderId);
            return orderId;
        } catch (IllegalAccessException e) {
            LOGGER.error("Exception in order id generation ", e);
            throw OrderIdGenerationException.getException(ResultCode.ORDER_ID_GENERATION_EXCEPTION);
        }
    }

    public static String getExtendInfoVal(BaseBody baseBody, String key) {
        if (baseBody == null || baseBody.getExtendInfo() == null || baseBody.getExtendInfo().isEmpty())
            return null;
        Object val = baseBody.getExtendInfo().get(key);
        if (val == null)
            return null;
        return val.toString();
    }

    public static void updateOrderIdInMDC(String orderId) {
        if (!StringUtils.isBlank(orderId)) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, orderId);
        }
    }

    public static void updateMidInMDC(String mid) {
        if (!StringUtils.isBlank(mid)) {
            MDC.put(TheiaConstant.RequestParams.MID, mid);
        }
    }

    public static void setMDC(final CashierInfoRequest cashierInfoRequest) {
        if (cashierInfoRequest == null) {
            return;
        }

        setMDC(cashierInfoRequest.getHead());
    }

    public static void setMDC(final RequestHeader header) {
        if (header == null) {
            return;
        }
        setMDC(header.getMid(), null, header.getRequestId());
    }

    public static void setMDC(String mid, String order_id, String request_id) {
        if (!StringUtils.isBlank(mid)) {
            MDC.put(TheiaConstant.RequestParams.MID, mid);
        }

        if (!StringUtils.isBlank(order_id)) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, order_id);
        }

        if (!StringUtils.isBlank(request_id)) {
            MDC.put(TheiaConstant.RequestParams.REQUEST_ID, request_id);
        }
    }

    public static void setMDC(String mid, String order_id, String request_id, String txnToken) {
        if (!StringUtils.isBlank(mid)) {
            MDC.put(TheiaConstant.RequestParams.MID, mid);
        }

        if (!StringUtils.isBlank(order_id)) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, order_id);
        }

        if (!StringUtils.isBlank(request_id)) {
            MDC.put(TheiaConstant.RequestParams.REQUEST_ID, request_id);
        }

        if (!StringUtils.isBlank(txnToken)) {
            MDC.put(TheiaConstant.RequestParams.TXN_TOKEN, txnToken);
        }
    }

    public static void setMDC(String requestId, String txnToken) {
        if (!StringUtils.isBlank(txnToken)) {
            MDC.put(TheiaConstant.RequestParams.TXN_TOKEN, txnToken);
        }

        if (!StringUtils.isBlank(requestId)) {
            MDC.put(TheiaConstant.RequestParams.REQUEST_ID, requestId);
        }
    }

    public static Map<String, Object> getExtendInfoFromWorkflowResponseBean(WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean == null)
            return Collections.emptyMap();
        return toStringObjectMap(workFlowResponseBean.getExtendedInfo());
    }

    public static Map<String, Object> toStringObjectMap(Map<String, String> extendInfo) {
        Map<String, Object> map = new HashMap<>();
        if (extendInfo != null && extendInfo.entrySet() != null) {

            for (Map.Entry<String, String> entry : extendInfo.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

}
