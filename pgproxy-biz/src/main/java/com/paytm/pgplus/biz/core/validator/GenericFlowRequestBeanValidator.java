/**
 * 
 */
package com.paytm.pgplus.biz.core.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.validator.ValidatorSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

/**
 * @author namanjain
 *
 */
public class GenericFlowRequestBeanValidator<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericFlowRequestBeanValidator.class);

    T bean;
    private String errorMessage;
    private String propertyPath;

    /**
     * @param bean
     */
    public GenericFlowRequestBeanValidator(T bean) {
        this.bean = bean;
    }

    /**
     * @return the propertyPath
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * @param propertyPath
     *            the propertyPath to set
     */
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    /**
     * @return the bean
     */
    public T getBean() {
        return bean;
    }

    /**
     * @param bean
     *            the bean to set
     */
    public void setBean(T bean) {
        this.bean = bean;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return getPropertyPath() + " " + errorMessage;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return true/false as par validation
     */
    public ValidationResultBean validate() {

        long threadStartTime1 = System.currentTimeMillis();
        Validator validator = ValidatorSingleton.getInstance().getValidator();
        long timeForBuildingValidator = System.currentTimeMillis() - threadStartTime1;

        long threadStartTime2 = System.currentTimeMillis();
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        long timeForBeanValidation = System.currentTimeMillis() - threadStartTime2;
        Map<String, String> metaData = new HashMap<>();
        metaData.put("LEGACY_BEAN_VALIDATION_CREATION", String.valueOf(timeForBuildingValidator));
        metaData.put("LEGACY_BEAN_VALIDATION_VALIDATE", String.valueOf(timeForBeanValidation));
        EventUtils.pushTheiaEvents(EventNameEnum.LEGACY_BEAN_VALIDATION, metaData);

        for (ConstraintViolation<T> violation : violations) {
            setPropertyPath(violation.getPropertyPath().toString());
            setErrorMessage(violation.getMessage());
            ResponseConstants errorCode = fetchValidationErrorCode(violation.getPropertyPath().toString());
            ValidationResultBean validationResultBean = new ValidationResultBean(errorCode);
            LOGGER.error("Validation Failed. Validation Result being returned as : {} ", validationResultBean);
            return validationResultBean;
        }

        return new ValidationResultBean(true);
    }

    private ResponseConstants fetchValidationErrorCode(String propertyPath) {

        switch (propertyPath) {
        case "requestType":
            return ResponseConstants.INVALID_REQUEST_TYPE;

        case "txnAmount":
            return ResponseConstants.INVALID_TXN_AMOUNT;

        case "orderID":
            return ResponseConstants.INVALID_ORDER_ID;

        case "cardNo":
            return ResponseConstants.INVALID_CARD_NO;

        case "savedCardID":
            return ResponseConstants.INVALID_SAVED_CARD_ID;

        case "expiryMonth":
            return ResponseConstants.INVALID_MONTH;

        case "expiryYear":
            return ResponseConstants.INVALID_YEAR;

        case "cvv2":
            return ResponseConstants.INVALID_CVV;

        case "custID":
            return ResponseConstants.INVALID_CUST_ID;

        case "industryTypeID":
            return ResponseConstants.INVALID_INDUSTRY_TYPE_ID;

        case "storeCard":
            return ResponseConstants.INVALID_CARD_STORE_FLAG;

        case "otp":
            return ResponseConstants.INVALID_OTP;

        case "paytmMID":
            return ResponseConstants.INVALID_MID;

        case "transType":
            return ResponseConstants.INVALID_TRANS_TYPE;

        case "subsStartDate":
            return ResponseConstants.INVALID_SUBS_START_DATE;

        case "subsExpiryDate":
            return ResponseConstants.INVALID_SUBS_END_DATE;

        case "mmid":
            return ResponseConstants.INVALID_MMID;

        case "mobileNo":
            return ResponseConstants.INVALID_MOBILE_NUMBER;

        case "tipAmount":
            return ResponseConstants.INVALID_TIP_AMOUNT;

        default:
            return null;
        }

    }
}