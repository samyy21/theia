package com.paytm.pgplus.theia.paymentoffer.controller;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponseBody;
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

@NativeControllerAdvice
@RestController
@RequestMapping("api/v2")
public class PaymentOffersControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersControllerV2.class);

    @Autowired
    private IPaymentOffersService paymentOffersService;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @ApiOperation(value = "applyPromo", notes = "to apply/validate payment instruments based offer")
    @RequestMapping(value = "/applyPromo", method = { RequestMethod.POST })
    public ApplyPromoResponse applyPromo(@ApiParam(required = true) @RequestBody ApplyPromoRequest applyPromoRequest,
            @RequestParam(value = "referenceId", required = false) String referenceId) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("applyPromo request = {}", applyPromoRequest);

        try {
            PaymentOfferUtils.setRequestHeader(applyPromoRequest.getHead());
            // try {
            ApplyPromoResponse response = paymentOffersService.applyPromo(applyPromoRequest, "V2", referenceId);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("applyPromo", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "applyPromo" + "to grafana", exception);
            }
            LOGGER.info("returning response for applyPromo V2 = {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(TheiaConstant.ExtraConstants.APPLY_PROMO_URL,
                        EventNameEnum.RESPONSE_CODE_SENT, response.getBody().getResultInfo().getResultCode(), response
                                .getBody().getResultInfo().getResultMsg());
            }
            return response;
            /*
             * } catch (RequestValidationException rve) { return
             * prepareErrorResponse(rve.getResultInfo()); } catch (BaseException
             * be) { return prepareErrorResponse(be.getResultInfo()); } catch
             * (Exception e) { return prepareErrorResponse(null); }
             */
        } finally {
            LOGGER.info("Total time taken by applyPromo API V2: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    private ApplyPromoResponse prepareErrorResponse(ResultInfo resultInfo) {
        ApplyPromoResponse response = new ApplyPromoResponse();
        response.setHead(new ResponseHeader());
        response.setBody(new ApplyPromoResponseBody());
        if (resultInfo != null) {
            response.getBody().setResultInfo(resultInfo);
        } else {
            response.getBody().setResultInfo(
                    new ResultInfo(ResultCode.SYSTEM_ERROR.getResultStatus(),
                            ResultCode.SYSTEM_ERROR.getResultCodeId(), ResultCode.SYSTEM_ERROR.getResultMsg()));
        }
        return response;
    }

}
