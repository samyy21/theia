package com.paytm.pgplus.cashier.validator.service.impl;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.PaymentsBankRequest;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.validator.service.IPaymentsBankRequestValidation;
import com.paytm.pgplus.checksum.crypto.impl.RSAEncryption;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.request.ValidatePassCodeRequest;
import com.paytm.pgplus.facade.user.models.response.PassCodeVerificationResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author kartik
 * @date 21-Sep-2017
 */
@Service("paymentsBankRequestValidationImpl")
public class PaymentsBankRequestValidationImpl implements IPaymentsBankRequestValidation {

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authenticationImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsBankRequestValidationImpl.class);

    private static final RSAEncryption RSA_ENCRYPTION = RSAEncryption.getInstance();

    private static final String PAYMENTS_BANK_CODE = "PPBL";
    private static final String BANK_SCOPE_TOKEN_NAME = "ppblToken";
    private static final String PASS_THROUGH_EXTEND_INFO_KEY = "passThroughExtendInfo";
    private static final String ACCOUNT_REF_ID = "accountRefId";

    @Override
    public void validatePaymentRequestViaSavingsAccount(CashierRequest cashierRequest) throws PaytmValidationException {

        ValidationRequest validationRequest = cashierRequest.getValidationRequest();
        if (PAYMENTS_BANK_CODE.equals(validationRequest.getSelectedBank())) {

            PaymentsBankRequest paymentsBankRequest = cashierRequest.getPaymentsBankRequest();
            if (paymentsBankRequest != null) {
                try {
                    if (StringUtils.isBlank(paymentsBankRequest.getPassCode())) {
                        LOGGER.error("Blank passcode entered by user for Paytm payments bank");
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                    }
                    String encPassCode = RSA_ENCRYPTION.encrypt(paymentsBankRequest.getPassCode());
                    ValidatePassCodeRequest validatePassCodeRequest = new ValidatePassCodeRequest(
                            paymentsBankRequest.getUserMobile(), encPassCode, paymentsBankRequest.getClientId(),
                            paymentsBankRequest.getClientSecret());
                    validatePassCodeRequest.setScope("bank_txn");
                    validatePassCodeRequest.setPaytmToken(paymentsBankRequest.getPaytmToken());
                    PassCodeVerificationResponse authVerificationResponse = authenticationImpl
                            .validatePassCode(validatePassCodeRequest);
                    if ((authVerificationResponse != null)) {
                        if (authVerificationResponse.isSuccessfullyProcessed()
                                && StringUtils.isNotBlank(authVerificationResponse.getAuthorizationToken()
                                        .getAccessToken())) {

                            Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();
                            passThroughExtendInfoMap.put(BANK_SCOPE_TOKEN_NAME, authVerificationResponse
                                    .getAuthorizationToken().getAccessToken());
                            passThroughExtendInfoMap.put(ACCOUNT_REF_ID, cashierRequest.getPaymentsBankRequest()
                                    .getAccountRefId());
                            String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
                            String encodedPassThrough = new String(Base64.getEncoder().encode(
                                    passThroughJson.getBytes()));
                            cashierRequest.getPaymentRequest().getPayBillOptions().getChannelInfo()
                                    .put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
                            return;
                        }
                        LOGGER.warn("Pass Code validation failed : {}", authVerificationResponse.getResponseMessage());
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE,
                                authVerificationResponse.getResponseMessage());
                    }
                } catch (FacadeUncheckedException | FacadeCheckedException e) {
                    throw new PaytmValidationException(e);
                }
            }
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_PAYTM_PAYMENTS_BANK);
        }

    }

}
