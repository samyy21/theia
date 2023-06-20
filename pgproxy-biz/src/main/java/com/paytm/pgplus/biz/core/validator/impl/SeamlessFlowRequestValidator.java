/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.validator.impl;

import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.core.validator.service.ICardValidator;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.LuhnAlgoImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * @author manojpal, vivek
 *
 */

@Service("seamlessvalidator")
public class SeamlessFlowRequestValidator implements IValidator, ICardValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessFlowRequestValidator.class);

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
            errorMessage = "NullBeanSentForValidation";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputObjectParam(workFlowBean.getRequestType())) {
            LOGGER.error("validation failed as invalid request Type is Passed");
            errorMessage = "InvalidRequestType";
            responseConstant = ResponseConstants.INVALID_REQUEST_TYPE;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateInputNumberParam(workFlowBean.getTxnAmount())) {
            LOGGER.error("validation failed as invalid txnAmount is Passed");
            errorMessage = "InvalidTxnAmount";
            responseConstant = ResponseConstants.INVALID_TXN_AMOUNT;
        }

        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateChannelID(workFlowBean.getChannelID())) {
            LOGGER.error("validation failed as invalid ChannelID is Passed");
            errorMessage = "InvalidChannelID";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (StringUtils.isBlank(errorMessage)
                && !BizParamValidator.validateIndustryTypeID(workFlowBean.getIndustryTypeID())) {
            LOGGER.error("validation failed as invalid IndustryTypeID is Passed");
            errorMessage = "InvalidIndustryTypeID";
            responseConstant = ResponseConstants.INVALID_INDUSTRY_TYPE_ID;
        }

        // TODO:Need to check it for offline case ?
        /*
         * if (StringUtils.isBlank(errorMessage) &&
         * !BizParamValidator.validateWebsite(workFlowBean.getWebsite()) &&
         * !workFlowBean.isOfflineFlow()) {
         * LOGGER.error("validation failed as invalid website is Passed");
         * errorMessage = "InvalidWebsite"; responseConstant =
         * ResponseConstants.INVALID_PARAM; }
         */

        // Validating store_card; supported values are 1(need to save) & 0(no
        // need)
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateStoreCard(workFlowBean)) {
            LOGGER.error("validation failed as invalid storeCard value is Passed");
            errorMessage = "InvalidStoreCard";
            responseConstant = ResponseConstants.INVALID_SAVED_CARD_ID;
        }

        if (StringUtils.isBlank(errorMessage)
                && (!PaymentTypeIdEnum.WALLET.value.equals(workFlowBean.getPaymentTypeId()))
                && (!BizParamValidator.validateInputStringParam(workFlowBean.getPaymentDetails()))
                && !PaymentTypeIdEnum.COD.value.equals(workFlowBean.getPaymentTypeId())
                && !EPayMethod.MP_COD.getMethod().equals(workFlowBean.getPaymentTypeId())
                && !PaymentTypeIdEnum.PPI.value.equals(workFlowBean.getPaymentTypeId())
                && !PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value.equals(workFlowBean.getPaymentTypeId())
                && !PaymentTypeIdEnum.GIFT_VOUCHER.value.equals(workFlowBean.getPaymentTypeId())) {
            LOGGER.error("validation failed as invalid Payment Details are Passed");
            errorMessage = "InvalidPaymentDetails";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // AuthMode in both request types are 3D & USRPWD
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateAuthMode(workFlowBean.getAuthMode())) {
            LOGGER.error("validation failed as invalid AuthMode is Passed");
            errorMessage = "InvalidAuthMode";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // PaymentTypeId supported are CC,DC,IMPS,NB,UPI
        String paymentTypeId = workFlowBean.getPaymentTypeId();
        if (StringUtils.isBlank(errorMessage)
                && !(PaymentTypeIdEnum.CC.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.DC.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.NB.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.IMPS.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.UPI.getValue().equals(paymentTypeId)
                        || PaymentTypeIdEnum.EMI.getValue().equals(paymentTypeId) || PaymentTypeIdEnum.BANK_MANDATE
                        .getValue().equals(paymentTypeId))
                && !ERequestType.isOfflineOrNativeOrUniRequest(workFlowBean.getRequestType())) {
            LOGGER.error("validation failed as invalid PaymentTypeId is Passed. Only CC,DC,NB,UPI & IMPS supported");
            errorMessage = "InvalidPaymentTypeId";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        // Validating bank_code; required in case of NetBanking
        if (StringUtils.isBlank(errorMessage) && !BizParamValidator.validateBankCodeForNB(workFlowBean)) {
            LOGGER.error("validation failed as invalid bankCode value is Passed");
            errorMessage = "InvalidBankCode";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }
        // Check for offline, ppi and addAndHybrid flow as token is mandatory
        // for the flow
        if (StringUtils.isBlank(errorMessage)
                && (ERequestType.OFFLINE.equals(workFlowBean.getRequestType())
                        || (!ERequestType.UNI_PAY.equals(workFlowBean.getRequestType()) && PaymentTypeIdEnum.PPI.value
                                .equals(workFlowBean.getPaymentTypeId()))
                        || workFlowBean.getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY || workFlowBean
                        .getPaytmExpressAddOrHybrid() == EPayMode.HYBRID)
                && !BizParamValidator.validateInputStringParam(workFlowBean.getToken())) {
            LOGGER.error("validation failed as invalid token value for offline/PPI/addAndHybrid flow is Passed");
            errorMessage = "InvalidToken";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }
        if (StringUtils.isBlank(errorMessage)
                && (ERequestType.isOfflineOrNativeOrUniRequest(workFlowBean.getRequestType()))
                && !BizParamValidator.validateDigitalCreditRequestAddnPay(workFlowBean)) {
            LOGGER.error("validation failed for digital credit request");
            errorMessage = "InvalidDigitalCreditRequest";
            responseConstant = ResponseConstants.INVALID_PARAM;
        }

        if (StringUtils.isBlank(errorMessage) && workFlowBean.isUpiPushFlow()
                && !BizParamValidator.isValidPaytmVPA(workFlowBean)) {
            LOGGER.error("validation failed as invalid VPA is Passed");
            errorMessage = "InvalidPaytmVPA";
            responseConstant = ResponseConstants.INVALID_PARAM;
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
        Calendar now = Calendar.getInstance();
        if (null != cacheCardRequest.getExpiryMonth() && null != cacheCardRequest.getExpiryYear()) {
            int year = Integer.parseInt(cacheCardRequest.getExpiryYear().toString());
            if (year < now.get(Calendar.YEAR)) {
                responseConstant = ResponseConstants.INVALID_YEAR;
                LOGGER.info("INVALID_YEAR for year {} and month {}", cacheCardRequest.getExpiryYear().toString(),
                        cacheCardRequest.getExpiryMonth().toString());
                return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
            }

            try {
                cardUtils.validateExpiryDate(cacheCardRequest.getExpiryMonth().toString(), cacheCardRequest
                        .getExpiryYear().toString(), cacheCardRequest.getCardScheme(), cacheCardRequest.getCardNo());
            } catch (PaytmValidationException e) {
                responseConstant = ResponseConstants.INVALID_MONTH;
                LOGGER.info("INVALID_MONTH for year {} and month {}", cacheCardRequest.getExpiryYear().toString(),
                        cacheCardRequest.getExpiryMonth().toString());
                return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
            }
        }

        if (null == cacheCardRequest.getExpiryMonth()) {
            responseConstant = ResponseConstants.INVALID_MONTH;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }
        if (null == cacheCardRequest.getExpiryYear()) {
            responseConstant = ResponseConstants.INVALID_YEAR;
            return new GenericCoreResponseBean<>(responseConstant.getMessage(), responseConstant);
        }
        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }
}
