package com.paytm.pgplus.theia.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;

/**
 * Created by Naman on 05/06/17.
 */
@Component
public class InterceptorUtils {

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    public String[] fetchMidRequestType(HttpServletRequest request) throws FacadeCheckedException,
            UnsupportedEncodingException {

        String mid;
        String requestType;

        if (TheiaConstant.ExtraConstants.FETCH_PAYMENT_INSTRUMENT_URL.equals(request.getRequestURI())) {
            String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            CashierInfoRequest requestData = JsonMapper.mapJsonToObject(content, CashierInfoRequest.class);
            mid = requestData.getHead().getMid();
            requestType = TheiaConstant.RequestTypes.FETCH_PAYMENT_INSTRUMENT;
        } else if (TheiaConstant.RequestParams.AUTO_DEBIT_URL.equals(request.getRequestURI())) {
            if (StringUtil.isBlank(request.getParameter(TheiaConstant.RequestParams.AUTO_DEBIT_DATA))) {
                return new String[] { "", "" };
            }
            String decodedString = URLDecoder.decode(request.getParameter(TheiaConstant.RequestParams.AUTO_DEBIT_DATA),
                    "UTF-8");
            mid = JsonMapper.getStringParamFromJson(decodedString, TheiaConstant.RequestParams.AUTO_DEBIT_MID);
            requestType = JsonMapper.getStringParamFromJson(decodedString,
                    TheiaConstant.RequestParams.AUTO_DEBIT_REQUEST_TYPE);
        } else if (request.getRequestURI().matches(TheiaConstant.ExtraConstants.NATIVE_BASE_API_REGEX)) {
            mid = request.getParameter(Native.MID);
            if (aoaUtils.isAOAMerchant(mid)) {
                requestType = TheiaConstant.RequestTypes.UNI_PAY;
            } else {
                requestType = TheiaConstant.RequestTypes.NATIVE;
            }
        } else {
            mid = request.getParameter(TheiaConstant.RequestParams.MID);
            requestType = request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE);
        }

        return new String[] { mid, requestType };
    }

}
