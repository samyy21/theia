package com.paytm.pgplus.biz.core.validator.service;

import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author vivek
 *
 */
public interface ICardValidator {

    GenericCoreResponseBean<Boolean> validateCardNumber(CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException;

    GenericCoreResponseBean<Boolean> validateCvv(final CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException;

    GenericCoreResponseBean<Boolean> validateExpiry(final CacheCardRequestBean cacheCardRequest)
            throws PaytmValidationException;
}
