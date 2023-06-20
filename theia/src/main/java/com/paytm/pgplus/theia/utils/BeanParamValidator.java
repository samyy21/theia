package com.paytm.pgplus.theia.utils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author amit.dubey
 *
 */
public class BeanParamValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanParamValidator.class);

    /**
     * This method is used to validate any Input String Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank
     */
    public static boolean validateInputStringParam(String inputParam) {
        LOGGER.debug("BeanParamValidator for StringParam::{}", inputParam);
        if (StringUtils.isBlank(inputParam)) {
            LOGGER.debug("StringParam is null or Blank ::{}", inputParam);
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate any Input Object Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null
     */
    public static boolean validateInputObjectParam(Object inputParam) {
        LOGGER.debug("BeanParamValidator for ObjectParam::{}", inputParam);
        if (inputParam == null) {
            LOGGER.debug("ObjectParam is null ::{}", inputParam);
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate any Input List Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static boolean validateInputListParam(List<?> inputParam) {
        LOGGER.debug("BeanParamValidator for ListParam::{}", inputParam);
        if (inputParam == null || inputParam.isEmpty()) {
            LOGGER.debug("ListParam is null or Empty ::{}", inputParam);
            return false;
        }
        return true;
    }

    /**
     * This method is used to validate any Input Map Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is null or empty
     */
    public static boolean validateInputMapParam(Map<?, ?> inputParam) {
        LOGGER.debug("BeanParamValidator for MapParam::{}", inputParam);
        if (inputParam == null || inputParam.isEmpty()) {
            LOGGER.debug("MapParam is null or Empty ::{}", inputParam);
            return false;
        }
        return true;
    }

    /**
     * 
     * This method is used to validate an Input Number Parameter.
     * 
     * @param inputParam
     * @throws InvalidParameterException
     *             if the input parameter is blank, or not numeric
     */
    public static boolean validateInputNumberParam(String inputParam) {
        LOGGER.debug("BeanParamValidator for NumericStringParam::{}", inputParam);
        if (!validateInputStringParam(inputParam) || !StringUtils.isNumeric(inputParam)) {
            LOGGER.debug("NumericStringParam is null or Empty ::{}", inputParam);
            return false;
        }
        return true;
    }

}
