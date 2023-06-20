package com.paytm.pgplus.cashier.validator.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DigitalCreditRequest;
import com.paytm.pgplus.cashier.validator.service.IDigitalCreditPassCodeValidator;
import com.paytm.pgplus.checksum.crypto.impl.RSAEncryption;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.request.ValidatePassCodeRequest;
import com.paytm.pgplus.facade.user.models.response.PassCodeVerificationResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author kartik
 * @date 11-05-2017
 */
@Service("digitalCreditPassCodeValidator")
public class DigitalCreditPassCodeValidatorImpl implements IDigitalCreditPassCodeValidator {

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authenticationImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(DigitalCreditPassCodeValidatorImpl.class);

    private static final RSAEncryption RSA_ENCRYPTION = RSAEncryption.getInstance();

    @Override
    public void validatePassCodeForPaytmCC(CashierRequest cashierRequest) throws PaytmValidationException {
        DigitalCreditRequest digitalCreditRequest = cashierRequest.getDigitalCreditRequest();
        if (digitalCreditRequest != null) {
            try {
                if (!digitalCreditRequest.isPasscodeRequired()) {
                    LOGGER.info("Passcode not required for acquirement id : {}", cashierRequest.getAcquirementId());
                    cashierRequest
                            .getPaymentRequest()
                            .getPayBillOptions()
                            .getChannelInfo()
                            .put(CashierConstant.AUTHORIZATION_TOKEN, "SSO_TOKEN|" + digitalCreditRequest.getSsoToken());
                    return;
                }
                if (StringUtils.isBlank(digitalCreditRequest.getPassCode())) {
                    LOGGER.error("Blank passcode entered by user for PaytmCC");
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                }
                String encPassCode = RSA_ENCRYPTION.encrypt(digitalCreditRequest.getPassCode());
                ValidatePassCodeRequest validatePassCodeRequest = new ValidatePassCodeRequest(
                        digitalCreditRequest.getUserMobile(), encPassCode, digitalCreditRequest.getClientId(),
                        digitalCreditRequest.getClientSecret());
                validatePassCodeRequest.setScope("dc_txn");
                validatePassCodeRequest.setPaytmToken(digitalCreditRequest.getPaytmToken());
                PassCodeVerificationResponse authVerificationResponse = authenticationImpl
                        .validatePassCode(validatePassCodeRequest);
                if ((authVerificationResponse != null)) {
                    if (authVerificationResponse.isSuccessfullyProcessed()
                            && StringUtils
                                    .isNotBlank(authVerificationResponse.getAuthorizationToken().getAccessToken())) {
                        cashierRequest
                                .getPaymentRequest()
                                .getPayBillOptions()
                                .getChannelInfo()
                                .put(CashierConstant.AUTHORIZATION_TOKEN,
                                        authVerificationResponse.getAuthorizationToken().getAccessToken());
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
        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_PAYTMCC);
    }

}
