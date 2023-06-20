package com.paytm.pgplus.theia.nativ.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class FilterUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterUtils.class);

    public static String getMidFromQueryParam(Ff4jUtils ff4jUtils, HttpServletRequest request) {
        String mid = null;
        String query = request.getQueryString();
        String lowerCaseQuery;

        if (StringUtils.isNotBlank(query)) {
            lowerCaseQuery = query.toLowerCase();
            if (lowerCaseQuery.contains(RequestParams.MID_LOWER_CASE)) {
                mid = parseParam(query, lowerCaseQuery, RequestParams.MID_LOWER_CASE);
            }
            LOGGER.info("data parsed from query param mid : {} ", mid);
        }
        return mid;
    }

    public static String getOrderIdFromQueryParam(Ff4jUtils ff4jUtils, HttpServletRequest request) {
        String orderId = null;
        String query = request.getQueryString();
        String lowerCaseQuery;

        if (StringUtils.isNotBlank(query)) {
            lowerCaseQuery = query.toLowerCase();
            if (lowerCaseQuery.contains(RequestParams.ORDERID_LOWER_CASE)) {
                orderId = parseParam(query, lowerCaseQuery, RequestParams.ORDERID_LOWER_CASE);
            } else if (lowerCaseQuery.contains(RequestParams.ORDER_ID_LOWER_CASE)) {
                orderId = parseParam(query, lowerCaseQuery, RequestParams.ORDER_ID_LOWER_CASE);
            }
            LOGGER.info("data parsed from query param orderId :: {} ", orderId);
        }
        return orderId;
    }

    private static String parseParam(String query, String lowerCaseQuery, String param) {
        int startIndex = lowerCaseQuery.indexOf(param);
        int endIndex = lowerCaseQuery.indexOf("&", startIndex);
        if (endIndex > 0) {
            return query.substring(startIndex + param.length() + 1, endIndex);
        } else {
            return query.substring(startIndex + param.length() + 1);
        }
    }
}
