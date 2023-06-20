/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.models.response.ProcessTransactionErrorJsonResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessTransactionHelper {

    public static String processForError(final PaymentRequestBean paymentRequestData, final Model model) {
        throw new TheiaControllerException("Checksum is Invalid for the request.");
    }

    public static String processForS2SError(final PaymentRequestBean paymentRequestData, final Model model) {
        ProcessTransactionErrorJsonResponse processTransactionErrorJsonResponse = new ProcessTransactionErrorJsonResponse(
                paymentRequestData.getMid(), paymentRequestData.getOrderId());
        try {
            return JsonMapper.mapObjectToJson(processTransactionErrorJsonResponse);
        } catch (FacadeCheckedException e) {
            throw new TheiaControllerException(
                    "Checksum is Invalid for the request. Error while generating error response ", e);
        }
    }

    public static String processForS2SError(final PaymentRequestBean paymentRequestData, final Model model,
            final ResponseConstants responseConstant) {
        ProcessTransactionErrorJsonResponse processTransactionErrorJsonResponse = new ProcessTransactionErrorJsonResponse(
                paymentRequestData.getMid(), paymentRequestData.getOrderId(), responseConstant);
        try {
            return JsonMapper.mapObjectToJson(processTransactionErrorJsonResponse);
        } catch (FacadeCheckedException e) {
            throw new TheiaControllerException(
                    "Checksum is Invalid for the request. Error while generating error response ", e);
        }
    }

    public static void clearErrorParams(HttpServletRequest request) {
        request.removeAttribute("errorMsg");
        request.removeAttribute("validationErrors");
    }

    public static String replaceApostrophe(String str) {
        if (str == null)
            return null;

        str = str.replaceAll("'", "");
        return str;
    }

    public static boolean validateStringAsPerRegex(String stringToValidate, Pattern REGEX) {
        if (StringUtils.isNotBlank(stringToValidate)) {
            Matcher matcher = REGEX.matcher(stringToValidate.trim());
            return matcher.find();
        }
        return false;
    }

    public static boolean checkValidLength(String strToBeChecked, Integer minLength, Integer maxLength) {
        if (StringUtils.isNotBlank(strToBeChecked)) {
            if (strToBeChecked.length() >= minLength && strToBeChecked.length() <= maxLength)
                return true;
        }
        return false;
    }

}
