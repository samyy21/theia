package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DisablePaymentModeForAddCardFlowInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        String disabledPaymentModesParam = request.getParameter(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED) == null ? ""
                : request.getParameter(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED);

        String disabledPaymentModesAttribute = request.getAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED) == null ? ""
                : (String) request.getAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED);
        String disabledPaymentModes = mergePaymentModes(disabledPaymentModesParam, disabledPaymentModesAttribute);
        if (TheiaConstant.ExtraConstants.ADD_CARD_THEME.equals(request.getParameter(TheiaConstant.RequestParams.THEME))) {
            List<String> originalPaymentMode = Arrays.asList(disabledPaymentModes.split(","));
            String modifiedPaymentModes = Stream
                    .of(EPayMethod.values())
                    .filter(s -> s.getOldName() != null
                            && !(EPayMethod.CREDIT_CARD.getOldName().equals(s.getOldName())
                                    || EPayMethod.DEBIT_CARD.getOldName().equals(s.getOldName()) || originalPaymentMode
                                        .contains(s.getOldName()))).map(s -> s.getOldName())
                    .collect(Collectors.toSet()).toString().replace("[", "").replace("]", "").replace(" ", "");
            if (StringUtils.isNotBlank(disabledPaymentModes)) {
                request.setAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED, disabledPaymentModes
                        .concat(",").concat(modifiedPaymentModes));
            } else {
                request.setAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED, modifiedPaymentModes);
            }
        } else {
            request.setAttribute(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED, disabledPaymentModes);
        }
        return super.preHandle(request, response, handler);
    }

    private String mergePaymentModes(String fromParam, String fromAttribute) {
        if (StringUtils.isNotBlank(fromParam)) {
            String[] paymentModesArr = fromAttribute.split(",");
            for (String s : paymentModesArr) {
                if (fromParam.toLowerCase().contains(s.toLowerCase())) {
                    continue;
                } else {
                    fromParam = fromParam.concat(",").concat(s);
                }
            }
            return fromParam;
        } else {
            return fromAttribute;
        }
    }

}