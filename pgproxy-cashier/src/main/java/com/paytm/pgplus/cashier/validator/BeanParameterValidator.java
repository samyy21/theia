package com.paytm.pgplus.cashier.validator;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import org.slf4j.MDC;

/**
 * @author amit.dubey
 *
 */
public class BeanParameterValidator {

    /**
     *
     * This method is used to validate any Input String Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank
     */
    public static void validateInputStringParam(String inputParam, String paramName)
            throws CashierInvalidParameterException {
        if (StringUtils.isBlank(inputParam)) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "NONE", "request", "Input param blank", paramName);
            throw new CashierInvalidParameterException("Input Param cannot be blank : " + paramName);
        }
    }

    /**
     *
     * This method is used to validate any Input Object Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null
     */
    public static void validateInputObjectParam(Object inputParam, String paramName)
            throws CashierInvalidParameterException {
        if (inputParam == null) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "NONE", "request", "Input param blank", paramName);
            throw new CashierInvalidParameterException("Input Param cannot be blank : " + paramName);
        }
    }

    /**
     *
     * This method is used to validate any Input List Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static void validateInputListParam(List<?> inputParam, String paramName)
            throws CashierInvalidParameterException {
        if ((inputParam == null) || inputParam.isEmpty()) {
            throw new CashierInvalidParameterException("Input Param cannot be empty : " + paramName);
        }
    }

    /**
     *
     * This method is used to validate any Input Set Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static void validateInputSetParam(Set<?> inputParam, String paramName)
            throws CashierInvalidParameterException {
        if ((inputParam == null) || inputParam.isEmpty()) {
            throw new CashierInvalidParameterException("Input Param cannot be empty : " + paramName);
        }
    }

    /**
     * This method is used to validate any Input Map Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static void validateInputMapParam(Map<?, ?> inputParam, String paramName)
            throws CashierInvalidParameterException {
        if ((inputParam == null) || inputParam.isEmpty()) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "NONE", "request", "Input param blank", paramName);
            throw new CashierInvalidParameterException("Input Param cannot be empty : " + paramName);
        }
    }

    /**
     *
     * This method is used to validate an Input Number Parameter.
     *
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank, or not numeric
     */
    public static void validateInputNumberParam(String inputParam, String paramName)
            throws CashierInvalidParameterException {
        validateInputStringParam(inputParam, paramName);
        if (!StringUtils.isNumeric(inputParam)) {
            throw new CashierInvalidParameterException("Input Param should be numeric : " + paramName);
        }
    }

}
