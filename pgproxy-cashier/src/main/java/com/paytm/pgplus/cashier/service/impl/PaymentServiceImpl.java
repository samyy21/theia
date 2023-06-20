/**
 *
 */
package com.paytm.pgplus.cashier.service.impl;

import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;

import com.paytm.pgplus.facade.utils.GenericCallBack;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.SaveCardValidationException;
import com.paytm.pgplus.cashier.exception.ValidationException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.cashier.pay.model.BusinessLogicValidation;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.service.IPaymentService;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.cashier.workflow.AbstractCashierWorkflow;
import com.paytm.pgplus.cashier.workflow.util.CashierWorkflowFactory;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.ValidationMessage;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;

/**
 * @author amit.dubey
 *
 */
@Component("paymentServiceImpl")
public class PaymentServiceImpl implements IPaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String ERROR_MESSAGE = "Process failed : Internal system error";
    private static final String BUSINESS_VALIDATION_FAILED = "Business Validation Failed";

    @Autowired
    CashierWorkflowFactory workflowFactory;

    @Autowired
    CashierUtilService cashierUtilService;

    @Override
    public GenericCoreResponseBean<InitiatePaymentResponse> initiate(CashierRequest cashierRequest)
            throws PaytmValidationException, SaveCardValidationException {

        GenericCoreResponseBean<InitiatePaymentResponse> genericCoreResponseBean;
        try {

            validateRequestForInitiate(cashierRequest);

            BusinessLogicValidation blValidation = fillValidationRequest(cashierRequest.getValidationRequest(),
                    cashierRequest);

            AbstractCashierWorkflow workflow = workflowFactory.getCashierWorkflow(cashierRequest.getCashierWorkflow()
                    .getValue());

            if (null == workflow) {
                throw new CashierCheckedException(ERROR_MESSAGE);
            }

            ValidationHelper validationHelper = new ValidationHelper(blValidation);
            validationHelper = workflow.validate(validationHelper);

            if (!validationHelper.getErrors().isEmpty()) {
                throw new ValidationException(validationHelper.getErrors());
            }

            // Start the payment process
            InitiatePaymentResponse response = workflow.initiatePayment(cashierRequest);

            // set the payment response and return
            genericCoreResponseBean = new GenericCoreResponseBean<>(response);

        } catch (CashierCheckedException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
        } catch (ValidationException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getErrors());
            PaytmValidationExceptionType type = getTypeFromExceptions(e.getErrors());
            throw new PaytmValidationException(genericCoreResponseBean.getFailureMessage(), type);
        }

        return genericCoreResponseBean;
    }

    @Override
    public GenericCoreResponseBean<DoPaymentResponse> submit(CashierRequest cashierRequest)
            throws PaytmValidationException {
        GenericCoreResponseBean<DoPaymentResponse> genericCoreResponseBean;

        try {
            validateRequestForSubmit(cashierRequest);

            AbstractCashierWorkflow workflow = workflowFactory.getCashierWorkflow(cashierRequest.getCashierWorkflow()
                    .getValue());

            if (null == workflow) {
                throw new CashierCheckedException(ERROR_MESSAGE);
            }
            LOGGER.debug("initiating fetch payment status");

            // Start the payment process
            DoPaymentResponse response = workflow.doPayment(cashierRequest);

            // save payment process response
            genericCoreResponseBean = new GenericCoreResponseBean<>(response);

        } catch (CashierCheckedException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
        }

        return genericCoreResponseBean;
    }

    public void submitAsync(CashierRequest cashierRequest,
            GenericCallBack<GenericCoreResponseBean<DoPaymentResponse>> callBack) throws PaytmValidationException {
        GenericCoreResponseBean<DoPaymentResponse> genericCoreResponseBean;
        try {
            validateRequestForSubmit(cashierRequest);

            AbstractCashierWorkflow workflow = workflowFactory.getCashierWorkflow(cashierRequest.getCashierWorkflow()
                    .getValue());

            if (null == workflow) {
                throw new CashierCheckedException(ERROR_MESSAGE);
            }
            LOGGER.debug("initiating fetch payment status");

            // Start the payment process
            workflow.doPaymentAsync(cashierRequest, response -> {
                GenericCoreResponseBean<DoPaymentResponse> genericResponseBean = null;
                // save payment process response
                    if (response instanceof DoPaymentResponse) {
                        genericResponseBean = new GenericCoreResponseBean<>((DoPaymentResponse) response);
                    } else if (response instanceof Exception) {
                        Exception e = (Exception) response;
                        genericResponseBean = new GenericCoreResponseBean<>(e.getMessage());
                    }
                    callBack.processResponse(genericResponseBean);
                });

        } catch (CashierCheckedException e) {
            LOGGER.error("Exception occured: ", e);
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
            callBack.processResponse(genericCoreResponseBean);
        }
    }

    @Override
    public GenericCoreResponseBean<DoPaymentResponse> initiateAndSubmit(CashierRequest cashierRequest)
            throws PaytmValidationException {
        GenericCoreResponseBean<DoPaymentResponse> genericCoreResponseBean;

        try {
            validateRequestForInitiateAndSubmit(cashierRequest);

            BusinessLogicValidation blValidation = fillValidationRequest(cashierRequest.getValidationRequest(),
                    cashierRequest);

            AbstractCashierWorkflow workflow = workflowFactory.getCashierWorkflow(cashierRequest.getCashierWorkflow()
                    .getValue());

            if (null == workflow) {
                throw new CashierCheckedException(ERROR_MESSAGE);
            }

            ValidationHelper validationHelper = new ValidationHelper(blValidation);
            validationHelper = workflow.validate(validationHelper);

            if (!validationHelper.getErrors().isEmpty()) {
                throw new ValidationException(validationHelper.getErrors(), BUSINESS_VALIDATION_FAILED);
            }

            DoPaymentResponse response = workflow.doPayment(cashierRequest);

            genericCoreResponseBean = new GenericCoreResponseBean<>(response);

        } catch (CashierCheckedException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
        } catch (ValidationException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getErrors());
            PaytmValidationExceptionType type = getTypeFromExceptions(e.getErrors());
            throw new PaytmValidationException(genericCoreResponseBean.getFailureMessage(), type, e);
        }

        return genericCoreResponseBean;
    }

    @Override
    public GenericCoreResponseBean<InitiatePaymentResponse> seamlessBankCardPayment(
            final SeamlessBankCardPayRequest seamlessBankCardPayRequest) throws PaytmValidationException {
        GenericCoreResponseBean<InitiatePaymentResponse> genericCoreResponseBean;

        try {
            AbstractCashierWorkflow workflow = workflowFactory
                    .getCashierWorkflow(CashierWorkflow.DIRECT_BANK_CARD_PAYMENT.getValue());
            InitiatePaymentResponse response = workflow.seamlessBankCardPayment(seamlessBankCardPayRequest);
            genericCoreResponseBean = new GenericCoreResponseBean<>(response);
        } catch (CashierCheckedException e) {
            LOGGER.error("SYSTEM_ERROR : Cashier won't be able to process the request ", e);
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
        }

        return genericCoreResponseBean;
    }

    @Override
    public GenericCoreResponseBean<InitiatePaymentResponse> chargeFeeOnRiskAnalysis(CashierRequest cashierRequest,
            String externalUserId) throws PaytmValidationException {
        GenericCoreResponseBean<InitiatePaymentResponse> genericCoreResponseBean;
        try {
            validateRequestForInitiate(cashierRequest);
            BusinessLogicValidation blValidation = fillValidationRequest(cashierRequest.getValidationRequest(),
                    cashierRequest);

            AbstractCashierWorkflow workflow = workflowFactory.getCashierWorkflow(CashierWorkflow.RISK_POLICY_CONSULT
                    .getValue());

            if (null == workflow) {
                throw new CashierCheckedException(ERROR_MESSAGE);
            }

            ValidationHelper validationHelper = new ValidationHelper(blValidation);
            validationHelper = workflow.validate(validationHelper);

            if (!validationHelper.getErrors().isEmpty()) {
                throw new ValidationException(validationHelper.getErrors());
            }

            InitiatePaymentResponse response = workflow.riskConsult(cashierRequest, externalUserId);

            // set the payment response and return
            genericCoreResponseBean = new GenericCoreResponseBean<>(response);
        } catch (CashierCheckedException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getMessage());
        } catch (ValidationException e) {
            genericCoreResponseBean = new GenericCoreResponseBean<>(e.getErrors());
            PaytmValidationExceptionType type = getTypeFromExceptions(e.getErrors());
            throw new PaytmValidationException(genericCoreResponseBean.getFailureMessage(), type);
        }

        return genericCoreResponseBean;
    }

    /**
     * @param cashierRequest
     * @throws CashierCheckedException
     */
    private void validateRequestForInitiateAndSubmit(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing :  cashier request");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            throw new CashierCheckedException("Manadatory parameter is missing :  payment request");
        }

        if (null == cashierRequest.getCashierMerchant()) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier merchant");
        }

        switch (cashierRequest.getCashierWorkflow()) {
        case COD:
        case WALLET:
        case IMPS:
        case ADD_MONEY_IMPS:
        case DIGITAL_CREDIT_PAYMENT:
            break;
        case ADD_MONEY_ATM:
        case ADD_MONEY_ISOCARD:
        case ADD_MONEY_NB:
        case ADD_MONEY_UPI:
        case ATM:
        case DIRECT_BANK_CARD_PAYMENT:
        case ISOCARD:
        case NB:
        case SUBSCRIPTION:
        case UPI:
        case RISK_POLICY_CONSULT:
        default:
            throw new CashierCheckedException("Process failed : " + cashierRequest.getCashierWorkflow()
                    + " workflow not supported");
        }

    }

    /**
     * @param cashierRequest
     * @throws CashierCheckedException
     */
    private void validateRequestForInitiate(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request");
        }

        if (null == cashierRequest.getCashierMerchant()) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier merchant");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            throw new CashierCheckedException("Manadatory parameter is missing : payment request");
        }

        switch (cashierRequest.getCashierWorkflow()) {
        case ADD_MONEY_ISOCARD:
        case ADD_MONEY_NB:
        case ADD_MONEY_ATM:
        case ADD_MONEY_UPI:
        case ISOCARD:
        case ATM:
        case NB:
        case UPI:
            break;
        case ADD_MONEY_IMPS:
        case COD:
        case DIRECT_BANK_CARD_PAYMENT:
        case IMPS:
        case SUBSCRIPTION:
        case WALLET:
        case DIGITAL_CREDIT_PAYMENT:
        case RISK_POLICY_CONSULT:
        default:
            throw new CashierCheckedException("Process failed : " + cashierRequest.getCashierWorkflow()
                    + " workflow not supported for Initiate Method");
        }

    }

    /**
     * @param cashierRequest
     * @throws CashierCheckedException
     */
    private void validateRequestForSubmit(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request ");
        }

        if (null == cashierRequest.getLooperRequest()) {
            throw new CashierCheckedException("Manadatory parameter is missing : looper request");
        }

        switch (cashierRequest.getCashierWorkflow()) {
        case ADD_MONEY_ISOCARD:
        case ADD_MONEY_NB:
        case ADD_MONEY_ATM:
        case ADD_MONEY_UPI:
        case ISOCARD:
        case ATM:
        case NB:
        case UPI:
        case GENERIC_PAYMENT_WORKFLOW:
            break;
        case ADD_MONEY_IMPS:
        case COD:
        case DIRECT_BANK_CARD_PAYMENT:
        case IMPS:
        case SUBSCRIPTION:
        case WALLET:
        case DIGITAL_CREDIT_PAYMENT:
        case RISK_POLICY_CONSULT:
        default:
            throw new CashierCheckedException("Process failed : " + cashierRequest.getCashierWorkflow()
                    + " workflow not supported for Submit Method");
        }
    }

    private BusinessLogicValidation fillValidationRequest(ValidationRequest validationRequest,
            CashierRequest cashierRequest) throws SaveCardValidationException, PaytmValidationException {
        BusinessLogicValidation blValidation = new BusinessLogicValidation();
        BinCardRequest binCardRequest;

        if (CashierWorkflow.ISOCARD.equals(cashierRequest.getCashierWorkflow())
                || CashierWorkflow.ADD_MONEY_ISOCARD.equals(cashierRequest.getCashierWorkflow())) {
            if (null != cashierRequest.getCardRequest().getBankCardRequest()) {
                blValidation.setCardNumber(cashierRequest.getCardRequest().getBankCardRequest().getCardNo());
            } else {
                String savedCardId = cashierRequest.getCardRequest().getSavedCardRequest().getSavedCardId();
                SavedCardVO savedCard = null;
                try {
                    boolean storeCardPref = Boolean.parseBoolean(cashierRequest.getPaymentRequest().getExtendInfo()
                            .get(CashierConstant.STORE_CARD_PREFERENCE));
                    String mId = cashierRequest.getCashierMerchant().getMerchantId();
                    String custId = cashierRequest.getPaymentRequest().getExtendInfo().get(CashierConstant.CUST_ID);
                    String userID = cashierRequest.getPaymentRequest().getExtendInfo()
                            .get(CashierConstant.PAYTM_USER_ID);
                    if (storeCardPref && StringUtils.isNotEmpty(custId) && StringUtils.isNotEmpty(mId)) {
                        savedCard = cashierUtilService.getSavedCardDetailsByCustIdMid(Long.parseLong(savedCardId),
                                userID, custId, mId);
                    } else {
                        savedCard = cashierUtilService.getSavedCardDetails(Long.parseLong(savedCardId), userID);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("Exception while retreiving Saved Card : ", e);
                }
                blValidation.setCardNumber(savedCard != null ? savedCard.getCardNumber() : null);
            }

            if (null != cashierRequest.getBinCardRequest()) {
                binCardRequest = cashierRequest.getBinCardRequest();
            } else {
                String binNumber = blValidation.getCardNumber().substring(0, 6);
                binCardRequest = cashierUtilService.getBinCardRequest(binNumber);
                cashierRequest.setBinCardRequest(binCardRequest);
            }
            blValidation.setBinCard(binCardRequest);
        }

        blValidation.setMid(cashierRequest.getCashierMerchant().getMerchantId());
        long totalAmount;
        if (cashierRequest.getPaymentRequest().getPayBillOptions().getChargeFeeAmount() != null) {
            totalAmount = cashierRequest.getPaymentRequest().getPayBillOptions().getServiceAmount()
                    + cashierRequest.getPaymentRequest().getPayBillOptions().getChargeFeeAmount();
        } else {
            totalAmount = cashierRequest.getPaymentRequest().getPayBillOptions().getServiceAmount();
        }
        blValidation.setAmount(Long.toString(totalAmount));
        blValidation.setValidationRequest(validationRequest);
        blValidation.setTransType(cashierRequest.getPaymentRequest().getTransType());
        blValidation.setPaymentType(cashierRequest.getPaymentRequest().getPaymentType());

        return blValidation;
    }

    private PaytmValidationExceptionType getTypeFromExceptions(MultivaluedMap<ValidationMessage, String> errorMap) {
        Iterator<ValidationMessage> it = errorMap.keySet().iterator();

        while (it.hasNext()) {
            ValidationMessage validationMessage = it.next();

            switch (validationMessage) {
            case INVALID_CARD_NUMBER:
                return PaytmValidationExceptionType.INVALID_CARD;
            case INVALID_BIN:
                return PaytmValidationExceptionType.INVALID_BIN_DETAILS;
            case DISABLED_PAYMENT_MODE:
                return PaytmValidationExceptionType.INVALID_PAYMODE;
            case SBI_ADD_MONEY:
            case INVALID_TXN_AMOUNT:
            case ERROR_MSG:
            case INVALID_BANK:
            default:
                return null;
            }

        }
        return null;
    }

}