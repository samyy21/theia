package com.paytm.pgplus.theia.offline.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.dynamicwrapper.core.config.impl.CacheService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.annotations.OfflineControllerAdvice;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.services.ICashierService;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.offline.utils.PayModeOrderUtil;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import io.swagger.annotations.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Created by rahulverma on 23/8/17.
 */

@Api(value = "/OfflineController", description = "Offline payment APIs", authorizations = { @Authorization(value = "sso token", scopes = {}) })
@OfflineControllerAdvice
@RestController
public class OfflineController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineController.class);
    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("EVENT_LOGGER");

    @Autowired
    @Qualifier("cashierService")
    private ICashierService cashierService;

    @Autowired
    @Qualifier("cacheService")
    private CacheService cacheService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @ApiOperation(value = "fetchPaymentInstruments", notes = "to fetch payment instruments for offline payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchPaymentInstruments", method = { RequestMethod.POST })
    public CashierInfoResponse fetchPaymentInstruments(
            @ApiParam(required = true) @RequestBody CashierInfoRequest cashierInfoRequest) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("fetchPaymentInstruments request = {}", cashierInfoRequest);
        nativePaymentUtil.logNativeRequests(cashierInfoRequest.toString());
        try {
            if (cashierInfoRequest == null || cashierInfoRequest.getHead() == null) {
                throw new TheiaControllerException("CashierInfoRequest can't be null");
            }
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Offline : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }

            OfflinePaymentUtils.setMDC(cashierInfoRequest);

            OfflinePaymentUtils.setRequestHeader(cashierInfoRequest.getHead());

            processTransactionUtil.pushFetchPaymentOptionsEvent(EventNameEnum.OFFLINE_FETCHPAYMENTOPTIONS);

            CashierInfoResponse cashierInfoResponse = cashierService.fetchCashierInfo(cashierInfoRequest);
            PayModeOrderUtil.payModeOrdering(cashierInfoResponse, merchantPreferenceService.getMerchantPaymodeSequence(
                    cashierInfoRequest.getHead().getMid(), PaymodeSequenceEnum.OFFLINE));
            LOGGER.info("returning response for fetchPaymentInstruments = {}", cashierInfoResponse);
            EVENT_LOGGER.info("returning response for fetchPaymentInstruments = {}", cashierInfoResponse);
            return cashierInfoResponse;
        } finally {
            LOGGER.info("Total time taken by fetchPaymentInstruments API : {}ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @ApiOperation(value = "fetchPaymentInstrumentsV2", notes = "to fetch payment instruments for offline payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/v2/fetchPaymentInstruments", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentInstrumentsV2(
            @ApiParam(required = true) @RequestBody CashierInfoRequest cashierInfoRequest)
            throws JsonProcessingException, IOException {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("fetchPaymentInstruments v2 request = {}", cashierInfoRequest);
        nativePaymentUtil.logNativeRequests(cashierInfoRequest.toString());
        try {
            if (cashierInfoRequest == null || cashierInfoRequest.getHead() == null) {
                throw new TheiaControllerException("CashierInfoRequest can't be null");
            }
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Offline : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }

            OfflinePaymentUtils.setMDC(cashierInfoRequest);

            OfflinePaymentUtils.setRequestHeader(cashierInfoRequest.getHead());

            processTransactionUtil.pushFetchPaymentOptionsEvent(EventNameEnum.OFFLINE_FETCHPAYMENTOPTIONS);

            CashierInfoResponse cashierInfoResponse = cashierService.fetchCashierInfo(cashierInfoRequest);
            PayModeOrderUtil.payModeOrdering(cashierInfoResponse, merchantPreferenceService.getMerchantPaymodeSequence(
                    cashierInfoRequest.getHead().getMid(), PaymodeSequenceEnum.OFFLINE));
            LOGGER.info("returning response for fetchPaymentInstruments = {}", cashierInfoResponse);
            EVENT_LOGGER.info("returning response for fetchPaymentInstruments = {}", cashierInfoResponse);
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(cashierInfoResponse));
        } finally {
            LOGGER.info("Total time taken by fetchPaymentInstruments API : {}ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @RequestMapping(value = "cache/refresh", method = { RequestMethod.GET })
    public void refreshCache() {
        try {
            cacheService.refreshCache();
        } catch (Exception e) {
            LOGGER.error("Exception in cache refresh {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

}