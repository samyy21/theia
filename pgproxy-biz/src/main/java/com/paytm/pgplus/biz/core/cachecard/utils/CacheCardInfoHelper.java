/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.cachecard.utils;

import com.paytm.pgplus.facade.user.models.UserName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.user.enums.CardType;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequestBody.CacheCardRequestBodyBuilder;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public class CacheCardInfoHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheCardInfoHelper.class);

    public static GenericCoreResponseBean<CacheCardRequest> createCacheCardRequestForCardPayment(
            final CacheCardRequestBean cacheCardRequestBean, InstNetworkType instNetworkTyp) {

        try {
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);
            final CacheCardRequestBodyBuilder cacheCardRequestBodyBuilder = new CacheCardRequestBody.CacheCardRequestBodyBuilder();
            String cardNumber = cacheCardRequestBean.getCardNo();
            if (instNetworkTyp == null) {
                instNetworkTyp = InstNetworkType.ISOCARD;
            }
            InstNetworkType instNetworkType = instNetworkTyp;
            String instNetworkCode = instNetworkType.toString();
            String cvv2 = cacheCardRequestBean.getCvv2();
            Short expiryYear = cacheCardRequestBean.getExpiryYear();
            Short expiryMonth = cacheCardRequestBean.getExpiryMonth();
            CardType cardType = CardType.getCardTypebyValue(cacheCardRequestBean.getCardType());
            String cardScheme = cacheCardRequestBean.getCardScheme();
            String instId = cacheCardRequestBean.getInstId();
            String cardIndexNo = cacheCardRequestBean.getCardIndexNo();
            boolean storeInCacheOnly = cacheCardRequestBean.isStoreInCacheOnly();
            final CacheCardRequestBody body = cacheCardRequestBodyBuilder.cardNo(cardNumber)
                    .instNetworkType(instNetworkType).instNetworkCode(instNetworkCode).cvv2(cvv2)
                    .expiryYear(expiryYear).expiryMonth(expiryMonth).cardType(cardType).cardScheme(cardScheme)
                    .instId(instId).cardIndexNo(cardIndexNo).last4ref(cacheCardRequestBean.getLast4ref())
                    .globalPanIndex(cacheCardRequestBean.getGlobalPanIndex()).par(cacheCardRequestBean.getPar())
                    .uniqueCardReference(cacheCardRequestBean.getUniqueCardReference()).build();
            body.setStoreInCacheOnly(storeInCacheOnly);
            final CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            LOGGER.debug("Cache Card Request Object::{}", cacheCardRequest);
            return new GenericCoreResponseBean<CacheCardRequest>(cacheCardRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in creataeCacheCardRequest: ", e);
            return new GenericCoreResponseBean<CacheCardRequest>(e.getMessage());
        }

    }

    public static GenericCoreResponseBean<CacheCardRequest> createCacheCardForIMPS(
            final CacheCardRequestBean cacheCardRequestBean) {

        try {
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);
            final CacheCardRequestBodyBuilder cacheCardRequestBodyBuilder = new CacheCardRequestBody.CacheCardRequestBodyBuilder();
            String cardNumber = cacheCardRequestBean.getCardNo();
            InstNetworkType instNetworkType = InstNetworkType.IMPS;
            String instNetworkTypeString = InstNetworkType.IMPS.toString();
            String otp = cacheCardRequestBean.getOtp();
            String holderMobileNumber = cacheCardRequestBean.getHolderMobileNo();
            final CacheCardRequestBody body = cacheCardRequestBodyBuilder.cardNo(cardNumber)
                    .instNetworkType(instNetworkType).instNetworkCode(instNetworkTypeString).otp(otp)
                    .holderMobileNo(holderMobileNumber).build();

            final CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            LOGGER.debug("Cache Card Request Object::{}", cacheCardRequest);
            return new GenericCoreResponseBean<CacheCardRequest>(cacheCardRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in creataeCacheCardRequest: ", e);
            return new GenericCoreResponseBean<CacheCardRequest>(e.getMessage());

        }

    }

    public static GenericCoreResponseBean<CacheCardRequest> createCacheCardForNB(
            final CacheCardRequestBean cacheCardRequestBean) {

        try {
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);
            final CacheCardRequestBodyBuilder cacheCardRequestBodyBuilder = new CacheCardRequestBody.CacheCardRequestBodyBuilder();
            String cardNumber = cacheCardRequestBean.getCardNo();
            String instId = cacheCardRequestBean.getInstId();
            InstNetworkType instNetworkType = InstNetworkType.SMA;
            String instNetworkTypeString = InstNetworkType.SMA.toString();
            final CacheCardRequestBody body = cacheCardRequestBodyBuilder.cardNo(cardNumber)
                    .instNetworkType(instNetworkType).instNetworkCode(instNetworkTypeString).instId(instId).build();

            final CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            LOGGER.info("Cache Card Request Object for NB::{}", cacheCardRequest);
            return new GenericCoreResponseBean<CacheCardRequest>(cacheCardRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in creataeCacheCardRequest: ", e);
            return new GenericCoreResponseBean<CacheCardRequest>(e.getMessage());

        }

    }

    public static CacheCardResponseBean mapCacheCardResponse(final CacheCardResponse cacheCardFacadeResponse) {
        String tokenID = cacheCardFacadeResponse.getBody().getTokenId();
        String maskedCardNumber = cacheCardFacadeResponse.getBody().getMaskedCardNo();
        String cardIndexNo = cacheCardFacadeResponse.getBody().getCardIndexNo();
        return new CacheCardResponseBean(tokenID, maskedCardNumber, cardIndexNo);
    }

    public static GenericCoreResponseBean<CacheCardRequest> createCacheCardForMandate(
            CacheCardRequestBean cacheCardRequestBean) {
        try {
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);
            final CacheCardRequestBodyBuilder cacheCardRequestBodyBuilder = new CacheCardRequestBody.CacheCardRequestBodyBuilder();
            String cardNumber = cacheCardRequestBean.getCardNo(); // Bank
                                                                  // Account
                                                                  // Number
            String instId = cacheCardRequestBean.getInstId(); // Issuing bank
                                                              // name
            InstNetworkType instNetworkType = InstNetworkType.IFSC;
            String instNetworkCode = cacheCardRequestBean.getInstNetworkCode();
            UserName userName = new UserName(cacheCardRequestBean.getHolderName(), "");
            String instBranchId = null; // No usage of this field as of now
            String unMaskedAccountNo = cacheCardRequestBean.getUnMaskedAccountNo();

            final CacheCardRequestBody body = cacheCardRequestBodyBuilder.cardNo(cardNumber)
                    .instNetworkType(instNetworkType).instNetworkCode(instNetworkCode).instId(instId)
                    .holderName(userName).unMaskedAccountNo(unMaskedAccountNo).build();

            final CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            LOGGER.debug("Cache Card Request Object::{}", cacheCardRequest);
            return new GenericCoreResponseBean<>(cacheCardRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in creataeCacheCardRequest: ", e);
            return new GenericCoreResponseBean<>(e.getMessage());

        }
    }

    public static GenericCoreResponseBean<CacheCardRequest> createCacheCardRequestForBankAccountNumberPayment(
            CacheCardRequestBean cacheCardRequestBean) {
        try {
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CACHE_CARD);
            final CacheCardRequestBodyBuilder cacheCardRequestBodyBuilder = new CacheCardRequestBody.CacheCardRequestBodyBuilder();
            String cardNumber = cacheCardRequestBean.getCardNo(); // Bank
                                                                  // Account
                                                                  // Number
            String instId = cacheCardRequestBean.getInstId();
            InstNetworkType instNetworkType = InstNetworkType.IFSC;
            String instNetworkCode = cacheCardRequestBean.getInstNetworkCode();
            String instBranchId = null;

            final CacheCardRequestBody body = cacheCardRequestBodyBuilder.cardNo(cardNumber)
                    .instNetworkType(instNetworkType).instNetworkCode(instNetworkCode).instId(instId).build();

            final CacheCardRequest cacheCardRequest = new CacheCardRequest(head, body);
            LOGGER.info("Cache Card Request Object::{}", cacheCardRequest);
            return new GenericCoreResponseBean<>(cacheCardRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in creataeCacheCardRequest: ", e);
            return new GenericCoreResponseBean<>(e.getMessage());

        }
    }

}
