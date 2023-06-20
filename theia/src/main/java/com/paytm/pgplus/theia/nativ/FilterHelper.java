package com.paytm.pgplus.theia.nativ;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;

public class FilterHelper {
    private static String getQueryParamsFromQueryString = ConfigurationUtil.getProperty(
            TheiaConstant.ExtraConstants.GET_QUERY_PARAM_FROM_QUERY_STRING_ENABLE, "true");

    public static Boolean isGettingQueryParamFromQueryStringEnabled() {
        return Boolean.valueOf(getQueryParamsFromQueryString);
    }
}
