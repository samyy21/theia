package com.paytm.pgplus.theia.services;

import javax.servlet.http.HttpServletRequest;

import com.paytm.pgplus.pgproxycommon.exception.KycValidationException;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequest;

/**
 * @author kartik
 * @date 06-Mar-2018
 */
public interface IUserKycService {

    void doKYC(HttpServletRequest request) throws KycValidationException;

    void doKYC(HttpServletRequest request, String userId) throws KycValidationException;

    void doKYC(NativeKYCDetailRequest request, String userId) throws KycValidationException;

}
