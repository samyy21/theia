package com.paytm.pgplus.theia.services.impl;

import Api.PaymentsApi;
import Invokers.ApiClient;
import Invokers.ApiException;
import Model.*;
import com.cybersource.authsdk.core.ConfigException;
import com.cybersource.authsdk.core.MerchantConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.CardValidationStatus;
import com.paytm.pgplus.theia.enums.CybersourceResponseEnum;
import com.paytm.pgplus.theia.models.VisaCyberSourceRequest;
import com.paytm.pgplus.theia.models.VisaCyberSourceResponse;
import com.paytm.pgplus.theia.nativ.utils.CyberSourceUtil;
import com.paytm.pgplus.theia.services.IVisaCyberSourceService;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.CyberSource.*;

@Component("visaCyberSourceServiceImpl")
public class VisaCyberSourceServiceImpl implements IVisaCyberSourceService {

    private static final String defaultDirectory = "/etc/payment_engine/key";
    private static final String defaultEnv = "CyberSource.IN.Environment.PRODUCTION";
    private static final String defaultTime = "15";

    private static final Logger LOGGER = LoggerFactory.getLogger(VisaCyberSourceServiceImpl.class);

    public VisaCyberSourceResponse getCardDetailFromVisaCyberSource(VisaCyberSourceRequest visaCyberSourceRequest) {
        VisaCyberSourceResponse visaCyberSourceResponse = new VisaCyberSourceResponse();
        try {
            CreatePaymentRequest requestForCyberSource = buildRequestForCyberSource(visaCyberSourceRequest);
            if (requestForCyberSource != null) {
                String cyberSourceKeysDirectory = ConfigurationUtil.getProperty(CYBER_SOURCE_KEYS_DIRECTORY,
                        defaultDirectory);
                String cyberSourceTimeOut = ConfigurationUtil.getProperty(CYBER_SOURCE_TIME, defaultTime);
                String cyberSourceMbid = ConfigurationUtil.getProperty(CYBER_SOURCE_Mbid);
                String cyberSourceEnv = ConfigurationUtil.getProperty(CYBER_SOURCE_ENVIRONMENT, defaultEnv);

                MerchantConfig merchantConfig = new MerchantConfig(CyberSourceUtil.getStatusQryProps(cyberSourceMbid,
                        cyberSourceKeysDirectory, cyberSourceTimeOut, cyberSourceEnv));
                ApiClient apiClient = new ApiClient();
                apiClient.merchantConfig = merchantConfig;
                PaymentsApi paymentApi = new PaymentsApi(apiClient);

                LOGGER.info("Zero Dollar Authentication Cybersource API status query request {}",
                        visaCyberSourceRequest);
                PtsV2PaymentsPost201Response ptsV2PaymentsPost201Response = paymentApi
                        .createPayment(requestForCyberSource);
                LOGGER.info("Zero Dollar Authentication Cybersource status query response {}",
                        ptsV2PaymentsPost201Response);

                if (ptsV2PaymentsPost201Response != null
                        && VISA_SUCCESS_STATUS_CODE.equalsIgnoreCase(apiClient.responseCode)) {
                    visaCyberSourceResponse.setStatus(ptsV2PaymentsPost201Response.getStatus());
                    if (ptsV2PaymentsPost201Response.getErrorInformation() != null
                            && StringUtils.isNotEmpty(ptsV2PaymentsPost201Response.getErrorInformation().getReason())) {
                        visaCyberSourceResponse.setResponseEnum(CybersourceResponseEnum.getByCodeAndStatus(
                                VISA_SUCCESS_STATUS_CODE, ptsV2PaymentsPost201Response.getErrorInformation()
                                        .getReason()));
                    } else if (ptsV2PaymentsPost201Response.getStatus() != null) {
                        visaCyberSourceResponse.setResponseEnum(CybersourceResponseEnum.getByCodeAndStatus(
                                VISA_SUCCESS_STATUS_CODE, ptsV2PaymentsPost201Response.getStatus()));
                    }
                } else {
                    visaCyberSourceResponse.setStatus(CardValidationStatus.UNKNOWN.getCode());
                }
                LOGGER.info("api status code from Zero Dollar Authentication Cybersource {}", apiClient.responseCode);
            }
        } catch (ApiException e) {
            if (e.getCode() == VISA_FAILURE_STATUS_CODE) {
                setStatusForInvalidRequest(visaCyberSourceResponse, e.getResponseBody());
            } else {
                visaCyberSourceResponse.setStatus(CardValidationStatus.SERVER_ERROR.getCode());
            }
            pushCyberSourceResponseEvent(e.getCode());
            LOGGER.error(
                    "Exception Occurred while fetching Cyber Source statusQry: rootcause is {} with response code is {} with Exception {}",
                    e.getResponseBody(), e.getCode(), e);
        } catch (ConfigException e) {
            LOGGER.error(
                    "Exception Occurred while building the merchant Config for Cyber Source some property is missing : {}",
                    e);
            visaCyberSourceResponse.setStatus(CardValidationStatus.SERVER_ERROR.getCode());
        } catch (Exception exp) {
            LOGGER.error("Exception Occurred while fetching the for Cyber Source: {}", exp);
            visaCyberSourceResponse.setStatus(CardValidationStatus.UNKNOWN.getCode());
        }
        return visaCyberSourceResponse;
    }

    private void pushCyberSourceResponseEvent(int statusCode) {
        String mid = MDC.get(TheiaConstant.RequestParams.MID);
        String requestId = MDC.get(TheiaConstant.RequestParams.REQUEST_ID);
        Map<String, String> metaInfo = new HashMap<>();
        metaInfo.put(API, CARD_VALIDATION_API);
        metaInfo.put(RESPONSE_CODE_CARD_VALIDATION, String.valueOf(statusCode));
        EventUtils.pushTheiaEvents(mid, requestId, EventNameEnum.CARD_VALIDATION, metaInfo);
    }

    private void setStatusForInvalidRequest(VisaCyberSourceResponse visaCyberSourceResponse, String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (StringUtils.isNotBlank(responseBody)) {
                Map<String, String> map = mapper.readValue(responseBody, Map.class);
                if (map != null) {
                    if (map.containsKey(REASON)) {
                        visaCyberSourceResponse.setResponseEnum(CybersourceResponseEnum.getByCodeAndStatus(
                                String.valueOf(VISA_FAILURE_STATUS_CODE), map.get(REASON)));
                    } else if (map.containsKey(STATUS)) {
                        visaCyberSourceResponse.setResponseEnum(CybersourceResponseEnum.getByCodeAndStatus(
                                String.valueOf(VISA_FAILURE_STATUS_CODE), map.get(STATUS)));
                    }
                    if (map.containsKey(STATUS) && map.get(STATUS).equalsIgnoreCase(DECLINED)) {
                        // for Invalid .p12 file
                        visaCyberSourceResponse.setStatus(CardValidationStatus.SERVER_ERROR.getCode());
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Exception Occurred while converted into ptsV2PaymentsPost201Response class {}", ex);
        }
        if (StringUtils.isBlank(visaCyberSourceResponse.getStatus())) {
            // For Invalid Request
            visaCyberSourceResponse.setStatus(CardValidationStatus.FAILED.getCode());
        }
    }

    public CreatePaymentRequest buildRequestForCyberSource(VisaCyberSourceRequest visaCyberSourceRequest) {

        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest();
        if (visaCyberSourceRequest != null) {
            Ptsv2paymentsClientReferenceInformation client = new Ptsv2paymentsClientReferenceInformation();
            client.code(visaCyberSourceRequest.getUniqueId());
            createPaymentRequest.clientReferenceInformation(client);

            Ptsv2paymentsOrderInformationAmountDetails amountDetails = new Ptsv2paymentsOrderInformationAmountDetails();
            amountDetails.totalAmount(visaCyberSourceRequest.getAmount());
            amountDetails.currency(visaCyberSourceRequest.getCurrency());

            Ptsv2paymentsOrderInformation orderInformation = new Ptsv2paymentsOrderInformation();
            orderInformation.amountDetails(amountDetails);
            createPaymentRequest.setOrderInformation(orderInformation);

            Ptsv2paymentsPaymentInformationCard card = new Ptsv2paymentsPaymentInformationCard();
            card.number(visaCyberSourceRequest.getCardNumber());
            if (StringUtils.isNotBlank(visaCyberSourceRequest.getExpiryMonth())
                    && StringUtils.isNotBlank(visaCyberSourceRequest.getExpiryYear())) {
                card.expirationMonth(visaCyberSourceRequest.getExpiryMonth());
                card.expirationYear(visaCyberSourceRequest.getExpiryYear());
            }
            Ptsv2paymentsPaymentInformation paymentInformation = new Ptsv2paymentsPaymentInformation();
            paymentInformation.card(card);
            createPaymentRequest.setPaymentInformation(paymentInformation);
        }
        return createPaymentRequest;
    }

    private VisaCyberSourceResponse parseCyberSourceResponse(PtsV2PaymentsPost201Response response) {
        VisaCyberSourceResponse visaCyberSourceResponse = new VisaCyberSourceResponse();
        if (response != null && response.getStatus() != null) {
            visaCyberSourceResponse.setStatus(response.getStatus());
        } else {
            LOGGER.info("Exception Occuared Invalid response received from cyber source API {}", response);
            visaCyberSourceResponse.setStatus(UNKNOWN);
        }
        return visaCyberSourceResponse;
    }

}
