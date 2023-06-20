/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.model.PromoCodeValidateCardRequest;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.enums.PromoServiceConsants;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * @createdOn 21-Mar-2016
 * @author kesari,surendra
 */
@Controller
public class PromoValidatorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromoValidatorController.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("promoServiceHelper")
    private IPromoServiceHelper promoServiceHelper;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @RequestMapping(value = "/checkPromoValidity")
    public void checkPromoValidity(final HttpServletRequest request, final HttpServletResponse response,
            final Model model, final Locale locale) {
        final long startTime = System.currentTimeMillis();

        try {
            final PromoCodeValidateCardRequest promoCodeValidateCardRequest = getPromoCodeValidateCardRequest(request);
            LOGGER.info("Request received for check promo validity : {}", promoCodeValidateCardRequest);

            final String responseString = processRequest(promoCodeValidateCardRequest);
            LOGGER.debug("responseString : {}", responseString);
            response.getWriter().print(responseString);

        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ", e);
        } finally {
            LOGGER.info("Total time taken for Controller {} is {} ms", "PromoValidatorController",
                    System.currentTimeMillis() - startTime);

        }
    }

    private PromoCodeValidateCardRequest getPromoCodeValidateCardRequest(final HttpServletRequest request) {
        final PromoCodeValidateCardRequest promoCodeRequest = new PromoCodeValidateCardRequest();
        try {
            promoCodeRequest.setMerchantId(request.getParameter(RequestParams.MID));
            promoCodeRequest.setPromoCode(request.getParameter(RequestParams.PROMO_CAMP_ID));
            promoCodeRequest.setCardNumber(request.getParameter(RequestParams.CARD_NO));
            promoCodeRequest.setTxnMode(getTxnMode(request.getParameter(RequestParams.CARD_NO)));
        } catch (final Exception ex) {
            LOGGER.error("Some Exception Occurred in getPromoCodeRequestData() method!!! {}", ex);
        }
        return promoCodeRequest;
    }

    private String getTxnMode(final String cardNumber) throws PaytmValidationException {
        final String binNumber = generateBinNumber(cardNumber);

        if (StringUtils.isBlank(binNumber)) {
            return null;
        }

        BinDetail binDetail = cardUtils.fetchBinDetails(binNumber);
        ;

        if (!StringUtils.isBlank(binDetail.getCardType())) {
            final String cardType = binDetail.getCardType();

            if (cardType.contains(ExtraConstants.DEBIT_CARD)) {
                return "DC";
            }
            if (cardType.contains(ExtraConstants.CREDIT_CARD)) {
                return "CC";
            }
        } else {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_BIN_CARD_TYPE);
        }

        return null;
    }

    /**
     * @param cardNumber
     * @return
     */
    private String generateBinNumber(final String cardNumber) {
        return StringUtils.isNotBlank(cardNumber) ? cardNumber.substring(0, 6) : StringUtils.EMPTY;
    }

    private String processRequest(final PromoCodeValidateCardRequest promoCodeValidateCardRequest) {
        final PromoCodeResponse promoCodeResponse;

        final JsonNodeFactory nodeFactory = new ObjectMapper().getNodeFactory();
        final ObjectNode node = nodeFactory.objectNode();
        node.put("error", false);
        try {
            String errorMsg = PromoServiceConsants.PROMO_ERROR_MSG_DEFAULT.getValue();
            promoCodeResponse = promoServiceHelper.validateCardPromoCode(promoCodeValidateCardRequest);
            final String respCode = promoCodeResponse == null ? null : promoCodeResponse.getPromoResponseCode();

            if (ResponseCodeConstant.PROMO_OLD_CARD_TRANSACTED.equals(respCode)) {
                node.put("error", true);
                final PaytmProperty paytmProperty = configurationDataService
                        .getPaytmProperty(PromoServiceConsants.PROMO_MSG_OLD_CARD_TRANSACTED.getValue());
                if (null != paytmProperty) {
                    errorMsg = paytmProperty.getValue();
                }
                node.put("errorMsg", errorMsg);
                return node.toString();

            } else if (ResponseCodeConstant.PROMO_MAX_COUNT_EXCEEDED.equals(respCode)) {
                node.put("error", true);
                final PaytmProperty paytmProperty = configurationDataService
                        .getPaytmProperty(PromoServiceConsants.PROMO_MSG_MAX_COUNT_EXCEEDED.getValue());
                if (null != paytmProperty) {
                    errorMsg = paytmProperty.getValue();
                }
                node.put("errorMsg", errorMsg);
                return node.toString();

            } else if (ResponseCodeConstant.PROMO_MAX_AMOUNT_EXCEEDED.equals(respCode)) {
                node.put("error", true);
                final PaytmProperty paytmProperty = configurationDataService
                        .getPaytmProperty(PromoServiceConsants.PROMO_MSG_MAX_AMOUNT_EXCEEDED.getValue());
                if (null != paytmProperty) {
                    errorMsg = paytmProperty.getValue();
                }
                node.put("errorMsg", errorMsg);
                return node.toString();

            }
            if (!ResponseCodeConstant.PROMO_SUCCESS.equals(respCode)) {
                node.put("error", true);
                node.put("errorMsg", errorMsg);

                EventUtils.pushTheiaEvents(EventNameEnum.PROMO_VALIDATION_FAILED, new ImmutablePair<>("ERROR_MESSAGE",
                        errorMsg), new ImmutablePair<>("PROMO_CODE", promoCodeValidateCardRequest.getPromoCode()),
                        new ImmutablePair<>("TXN_MODE", promoCodeValidateCardRequest.getTxnMode()));

                return node.toString();
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred", e);
        }
        return node.toString();

    }

}
