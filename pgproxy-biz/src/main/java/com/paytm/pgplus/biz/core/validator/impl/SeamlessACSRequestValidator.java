/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.validator.impl;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.ICardValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.LuhnAlgoImpl;

/**
 * @author manojpal, vivek
 *
 */
@Service("seamlessACSRequestValidator")
public class SeamlessACSRequestValidator implements IValidator, ICardValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessACSRequestValidator.class);

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    /**
     * @param workFlowBean
     * @return
     */
    @Override
    public GenericCoreResponseBean<Boolean> validate(final WorkFlowRequestBean workFlowBean) {
        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (!BizParamValidator.validateInputObjectParam(workFlowBean)) {
            LOGGER.error("Validation failed as request bean is null");
            responseConstant = ResponseConstants.INVALID_PARAM;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.error("validation failed as invalid request Type is Passed");
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.error("validation failed as invalid txnAmount is Passed");
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.error("validation failed as invalid ChannelID is Passed");
            responseConstant = ResponseConstants.INVALID_CHANNEL;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateIndustryTypeID(workFlowBean.getIndustryTypeID())) {
            LOGGER.error("validation failed as invalid IndustryTypeID is Passed");
            responseConstant = ResponseConstants.INVALID_INDUSTRY_TYPE_ID;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateCallbackUrls(workFlowBean.getExtendInfo())) {
            LOGGER.error("validation failed as callback URLs are not configured properly");
            responseConstant = ResponseConstants.INVALID_PARAM;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage)
                && (!BizParamValidator.validateInputStringParam(workFlowBean.getPaymentDetails()))) {
            LOGGER.error("validation failed as invalid Payment Details are Passed");
            responseConstant = ResponseConstants.INVALID_PAYMENT_DETAILS;
            errorMessage = responseConstant.getMessage();
        }

        // PaymentTypeId supported are CC,DC,IMPS,NB,UPI
        String paymentTypeId = workFlowBean.getPaymentTypeId();
        if (StringUtils.isBlank(errorMessage)
                && !(PaymentTypeIdEnum.CC.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.DC.getValue().equals(paymentTypeId) || PaymentTypeIdEnum.NB.getValue()
                        .equals(paymentTypeId))) {
            LOGGER.error("validation failed as invalid PaymentTypeId is Passed. Only CC,DC,NB supported");
            responseConstant = ResponseConstants.INVALID_PAYMENTMODE;
            errorMessage = responseConstant.getMessage();
        }

        if (StringUtils.isBlank(errorMessage)
                && (!BizParamValidator.validateInputStringParam(workFlowBean.getCustID()))) {
            LOGGER.error("validation failed as invalid Cust ID is Passed");
            responseConstant = ResponseConstants.INVALID_CUST_ID;
            errorMessage = responseConstant.getMessage();
        }

        return errorMessage == null ? new GenericCoreResponseBean<>(Boolean.TRUE) : new GenericCoreResponseBean<>(
                errorMessage, responseConstant);
    }

    @Override
    public GenericCoreResponseBean<Boolean> validateCardNumber(final CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException {
        if (StringUtils.isBlank(cacheCardRequest.getCardNo())
                || !LuhnAlgoImpl.validateCardNumber(cacheCardRequest.getCardNo())) {
            ResponseConstants responseConstant = ResponseConstants.INVALID_CARD_NO;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }
        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

    @Override
    public GenericCoreResponseBean<Boolean> validateCvv(final CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException {
        try {
            cardUtils.validateCVV(cacheCardRequest.getCvv2(), cacheCardRequest.getCardScheme(),
                    cacheCardRequest.getCardNo());
        } catch (PaytmValidationException e) {
            return new GenericCoreResponseBean<>(ResponseConstants.INVALID_CVV.getMessage(),
                    ResponseConstants.INVALID_CVV);
        }
        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

    @Override
    public GenericCoreResponseBean<Boolean> validateExpiry(final CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException {
        ResponseConstants responseConstant = null;
        if (null == cacheCardRequest.getExpiryMonth()) {
            responseConstant = ResponseConstants.INVALID_MONTH;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }
        if (null == cacheCardRequest.getExpiryYear()) {
            responseConstant = ResponseConstants.INVALID_YEAR;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }
        Calendar now = Calendar.getInstance();
        int year = Integer.parseInt(cacheCardRequest.getExpiryYear().toString());
        if (year < now.get(Calendar.YEAR)) {
            responseConstant = ResponseConstants.INVALID_YEAR;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }

        try {
            cardUtils.validateExpiryDate(cacheCardRequest.getExpiryMonth().toString(), cacheCardRequest.getExpiryYear()
                    .toString(), cacheCardRequest.getCardScheme(), cacheCardRequest.getCardNo());
        } catch (PaytmValidationException e) {
            responseConstant = ResponseConstants.INVALID_MONTH;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }

        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }
}
