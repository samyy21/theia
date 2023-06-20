package com.paytm.pgplus.theia.paymentoffer.controller;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchAllPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserIdRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.*;
import com.paytm.pgplus.theia.paymentoffer.service.IPaymentOffersService;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.utils.EventUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.APPLY_PROMO_URL;

@NativeControllerAdvice
@RestController
@RequestMapping("api/v1")
public class PaymentOffersController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersController.class);

    @Autowired
    private IPaymentOffersService paymentOffersService;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @LocaleAPI(apiName = APPLY_PROMO_URL, responseClass = ApplyPromoResponse.class)
    @ApiOperation(value = "applyPromo", notes = "to apply/validate payment instruments based offer")
    @RequestMapping(value = "/applyPromo", method = { RequestMethod.POST })
    public ApplyPromoResponse applyPromo(@ApiParam(required = true) @RequestBody ApplyPromoRequest applyPromoRequest,
            @RequestParam(value = "referenceId", required = false) String referenceId) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("applyPromo request = {}", applyPromoRequest);

        try {
            PaymentOfferUtils.setRequestHeader(applyPromoRequest.getHead());
            ApplyPromoResponse response = paymentOffersService.applyPromo(applyPromoRequest, "V1", referenceId);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("v1/applyPromo", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "v1/applyPromo" + "to grafana", exception);
            }
            LOGGER.info("returning response for applyPromo = {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(TheiaConstant.ExtraConstants.APPLY_PROMO_URL,
                        EventNameEnum.RESPONSE_CODE_SENT, response.getBody().getResultInfo().getResultCode(), response
                                .getBody().getResultInfo().getResultMsg());
            }
            return response;
        } finally {
            LOGGER.info("Total time taken by applyPromo API : {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @ApiOperation(value = "fetchAllPaymentOffers", notes = "to fetch payment instruments based offer")
    @RequestMapping(value = "/fetchAllPaymentOffers", method = { RequestMethod.POST })
    public FetchAllPaymentOffersResponse fetchAllPaymentOffers(
            @ApiParam(required = true) @RequestBody FetchAllPaymentOffersRequest request,
            @RequestParam(value = "referenceId", required = false) String referenceId) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("fetchAllPaymentOffers request = {}", request);

        try {
            PaymentOfferUtils.setRequestHeader(request.getHead());
            FetchAllPaymentOffersResponse response = paymentOffersService.fetchAllPaymentOffers(request, referenceId);
            LOGGER.info("returning response for fetchAllPaymentOffers = {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(TheiaConstant.ExtraConstants.FETCH_PAYMENT_OFFERS_URL,
                        EventNameEnum.RESPONSE_CODE_SENT, response.getBody().getResultInfo().getResultCode(), response
                                .getBody().getResultInfo().getResultMsg());
            }
            return response;
        } finally {
            LOGGER.info("Total time taken by fetchAllPaymentOffers API : {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @ApiOperation(value = "applyPromo", notes = "to apply/validate payment instruments based offer")
    @RequestMapping(value = "item/level/applyPromo", method = { RequestMethod.POST })
    public ApplyItemLevelPromoResponse itemlevelapplyPromo(
            @ApiParam(required = true) @RequestBody ApplyPromoRequest applyItemLevelPromo,
            @RequestParam(value = "referenceId", required = false) String referenceId) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("applyPromo request = {}", applyItemLevelPromo);

        try {
            PaymentOfferUtils.setRequestHeader(applyItemLevelPromo.getHead());
            ApplyItemLevelPromoResponse response = paymentOffersService.applyItemLevelPromo(applyItemLevelPromo,
                    referenceId);
            LOGGER.info("returning response for item level applyPromo = {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(TheiaConstant.ExtraConstants.ITEM_LEVEL_PROMO_URL,
                        EventNameEnum.RESPONSE_CODE_SENT, response.getBody().getResultInfo().getResultCode(), response
                                .getBody().getResultInfo().getResultMsg());
            }
            return response;
        } finally {
            LOGGER.info("Total time taken by applyPromo API V2: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchUserPaymentOffers", method = { RequestMethod.POST })
    public FetchUserPaymentOffersResponse fetchUserPaymentOffers(
            @ApiParam(required = true) @RequestBody FetchUserPaymentOffersRequest request,
            @RequestParam(value = "referenceId", required = false) String referenceId) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchUserPaymentOffers is: {}", request);
            IRequestProcessor<FetchUserPaymentOffersRequest, FetchUserPaymentOffersResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_USER_PAYMENT_OFFERS);
            FetchUserPaymentOffersResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /fetchUserPaymentOffers is: {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(TheiaConstant.ExtraConstants.FETCH_USER_PAYMENT_OFFERS_URL,
                        EventNameEnum.RESPONSE_CODE_SENT, response.getBody().getResultInfo().getResultCode(), response
                                .getBody().getResultInfo().getResultMsg());
            }
            return response;
        } finally {
            LOGGER.info("Total time taken for fetchUserPaymentOffers is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @ApiOperation(value = "fetchUserId", notes = "to fetch paytm User ID against a vpa")
    @RequestMapping(value = "/fetchUserId", method = RequestMethod.POST)
    public FetchUserIdResponse fetchUserId(@RequestBody FetchUserIdRequest fetchUserIdRequest,
            @RequestParam(value = "referenceId", required = true) String referenceId) {

        long startTime = System.currentTimeMillis();
        LOGGER.info("fetchUserIdRequest request received= {}", fetchUserIdRequest);
        FetchUserIdResponse fetchUserIdResponse = null;
        try {
            // boolean validVpa = Pattern.matches("\\w*@paytm\\b",
            // fetchUserIdRequest.getBody().getVpa());
            // if (!validVpa) {
            // FetchUserIdResponseBody fetchUserIdResponseBody = new
            // FetchUserIdResponseBody();
            // fetchUserIdResponseBody
            // .setResponseMsg("Invalid Vpa provided in request, other than @paytm VPA is not supported");
            // fetchUserIdResponse.setBody(fetchUserIdResponseBody);
            // return fetchUserIdResponse;
            // }
            fetchUserIdResponse = paymentOffersService.fetchUserId(fetchUserIdRequest, referenceId);
            LOGGER.info("returning response for fetchUserId api= {}", fetchUserIdResponse);
        } catch (Exception e) {

            LOGGER.error("Exception occurred while fetching UserId from paymentOffersService {}", e);

        } finally {
            LOGGER.info("Total time taken by fetchUserId API : {}ms", System.currentTimeMillis() - startTime);
        }
        return fetchUserIdResponse;

    }

}
