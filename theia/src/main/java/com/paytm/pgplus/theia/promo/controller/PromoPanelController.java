package com.paytm.pgplus.theia.promo.controller;

import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.promo.service.MerchantInfoService;
import com.paytm.pgplus.theia.promo.service.PaymentInfoService;
import com.paytm.pgplus.theia.promo.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/api/v1")
public class PromoPanelController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromoPanelController.class);

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private MerchantInfoService merchantInfoService;

    /**
     * This controller gets merchantInfo from mapping service
     * 
     * @param request
     *            this contains the request body
     *            {@link FetchMerchantInfoRequest}
     * @return {@link FetchMerchantInfoResponse}
     * @see <a href =
     *      "https://docs.google.com/document/d/1_WWt3zM40lNDeanrsblCl_32GavI--nxZK8QYjDRAjg/edit">PRD</a>
     * @see <a href = "https://wiki.mypaytm.com/display/PGP/API+Doc"> API
     *      Doc</a>
     */

    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    @RequestMapping(value = "/validateAndFetchMerchantInfo", method = { RequestMethod.POST })
    @ApiOperation(value = "/validateAndFetchMerchantInfo", notes = "Get Merchant Info for panel to create promo")
    public FetchMerchantInfoResponse fetchMerchantInfo(
            @ApiParam(required = true) @RequestBody FetchMerchantInfoRequest request) {
        LOGGER.info("Fetch Merchant Info request : {}", request);

        FetchMerchantInfoResponse response = null;

        if (merchantInfoService.isValidRequest(request)) {
            response = merchantInfoService.getMerchantInfoResponse(request);
        } else {
            response = new FetchMerchantInfoResponse(new ResponseHeader(), new MerchantInfoResponseBody());
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setResultCode(ResultCode.INVALID_INPUT_DATA.getCode());
            resultInfo.setResultStatus(ResultCode.INVALID_INPUT_DATA.getResultStatus());
            resultInfo.setResultMsg(ResultCode.INVALID_INPUT_DATA.getResultMsg());
            response.getBody().setResultInfo(resultInfo);
        }

        LOGGER.info("Fetch Merchant Info response : {}", response);
        return response;
    }

    /**
     * This controller gets bankdetails, cardNetworkDetails and payMethoddetails
     * form mapping service
     * 
     * @param request
     *            this contains the request body {@link FetchPaymentInfoRequest}
     * @return {@link FetchPaymentInfoResponse}
     * @see <a href =
     *      "https://docs.google.com/document/d/1_WWt3zM40lNDeanrsblCl_32GavI--nxZK8QYjDRAjg/edit">PRD</a>
     * @see <a href = "https://wiki.mypaytm.com/display/PGP/API+Doc"> API
     *      Doc</a>
     */
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    @RequestMapping(value = "/fetchPaymentPromotionAttributes", method = { RequestMethod.POST })
    @ApiOperation(value = "/fetchPaymentPromotionAttributes", notes = "Get Payment Promotion Attributes for panel to create promo")
    public FetchPaymentInfoResponse fetchPaymentInfo(
            @ApiParam(required = true) @RequestBody FetchPaymentInfoRequest request) {
        LOGGER.info("Fetch Merchant Info request : {}", request);

        FetchPaymentInfoResponse response = null;

        if (paymentInfoService.isValidRequest(request)) {

            response = paymentInfoService.getPaymentPromoAttributeResponse();
        } else {
            response = new FetchPaymentInfoResponse(new ResponseHeader(), new FetchPaymentInfoResponseBody());
            ResultInfo resultInfo = new ResultInfo();

            resultInfo.setResultCode(ResultCode.INVALID_INPUT_DATA.getCode());
            resultInfo.setResultStatus(ResultCode.INVALID_INPUT_DATA.getResultStatus());
            resultInfo.setResultMsg(ResultCode.INVALID_INPUT_DATA.getResultMsg());

            response.getBody().setResultInfo(resultInfo);
        }
        LOGGER.info("Fetch Merchant Info response : {}", response);
        return response;
    }
}
