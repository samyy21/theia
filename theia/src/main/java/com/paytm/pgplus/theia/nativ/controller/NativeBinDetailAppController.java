package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIRequest;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIResponse;
import com.paytm.pgplus.theia.nativ.model.cardindexnumber.NativeFetchCardIndexNumberAPIRequest;
import com.paytm.pgplus.theia.nativ.model.cardindexnumber.NativeFetchCardIndexNumberAPIResponse;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailRequest;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_BLOCKED_MESSAGE;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeBinDetailAppController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeBinDetailAppController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/fetchBinDetail", responseClass = NativeBinDetailResponse.class)
    @RequestMapping(value = "/fetchBinDetail", method = { RequestMethod.POST })
    public NativeBinDetailResponse fetchBinDetail(
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody NativeBinDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchBinDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<NativeBinDetailRequest, NativeBinDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_BIN_DETAIL_REQUEST);
            // OfflinePaymentUtils.gethttpServletResponse().setHeader("Access-Control-Allow-Origin",
            // "*");
            updateRequestWithReferenceId(request, referenceId);
            NativeBinDetailResponse response = requestProcessor.process(request);
            validateBinNotBlocked(response);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("fetchBinDetail", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "fetchBinDetail" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /fetchBinDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchBinDetail is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }

    }

    private void validateBinNotBlocked(NativeBinDetailResponse binDetails) {
        if (null != binDetails && null != binDetails.getBody() && null != binDetails.getBody().getBinDetail()) {
            if (!Boolean.parseBoolean(binDetails.getBody().getBinDetail().getIsActive())) {
                LOGGER.error("Error occurred since card bin is blocked {} ", binDetails.getBody().getBinDetail()
                        .getBin());
                throw BinDetailException.getException(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN, CARD_BLOCKED_MESSAGE);
            }
        }
    }

    private void updateRequestWithReferenceId(NativeBinDetailRequest request, String referenceId) {
        if (StringUtils.isBlank(referenceId) || null == request || null == request.getBody()) {
            return;
        }
        request.getBody().setReferenceId(referenceId);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchPromoCodeDetail", method = { RequestMethod.POST })
    public NativePromoCodeDetailResponse fetchPromoCodeDetail(
            @ApiParam(required = true) @RequestBody NativePromoCodeDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Native request received for API: /fetchPromoCodeDetail is: {}", request);
        nativePaymentUtil.logNativeRequests(request.getHead().toString());
        EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
        IRequestProcessor<NativePromoCodeDetailRequest, NativePromoCodeDetailResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestType.FETCH_PROMO_CODE_DETAIL);
        NativePromoCodeDetailResponse response = requestProcessor.process(request);
        LOGGER.info("Native response returned for API: /fetchPromoCodeDetail is: {}", response);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
            responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
            statsDUtils.pushResponse("fetchPromoCodeDetail", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "fetchPromoCodeDetail" + "to grafana", exception);
        }
        nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                .getBody()));
        return response;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchCardIndexNo", method = { RequestMethod.POST })
    public NativeFetchCardIndexNumberAPIResponse fetchCardIndexNo(final HttpServletRequest request,
            final HttpServletResponse httpServletResponse,
            @RequestParam(value = "referenceId", required = false) String referenceId) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchCardIndexNo");
            String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
            if (StringUtils.isBlank(requestData)) {
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
            NativeFetchCardIndexNumberAPIRequest fetchCardIndexNumberAPIRequest = JsonMapper.mapJsonToObject(
                    requestData, NativeFetchCardIndexNumberAPIRequest.class);
            fetchCardIndexNumberAPIRequest.getBody().setMid(request.getParameter("mid"));
            updateFCINRequestWithReferenceId(fetchCardIndexNumberAPIRequest, referenceId);
            LOGGER.info("Native FetchCardIndexNumber API Request is: {}", fetchCardIndexNumberAPIRequest);
            nativePaymentUtil.logNativeRequests(fetchCardIndexNumberAPIRequest.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(fetchCardIndexNumberAPIRequest.getHead());
            IRequestProcessor<NativeFetchCardIndexNumberAPIRequest, NativeFetchCardIndexNumberAPIResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.FETCH_CARD_INDEX_NUMBER_REQUEST);
            NativeFetchCardIndexNumberAPIResponse response = requestProcessor.process(fetchCardIndexNumberAPIRequest);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for /fetchCardIndexNo is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void updateFCINRequestWithReferenceId(NativeFetchCardIndexNumberAPIRequest request, String referenceId) {
        if (StringUtils.isBlank(referenceId) || null == request || null == request.getBody()) {
            return;
        }
        request.getBody().setReferenceId(referenceId);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchCardDetails", method = { RequestMethod.POST })
    public NativeBinCardHashAPIResponse fetchCardDetailsAndHash(
            @ApiParam(required = true) @RequestBody NativeBinCardHashAPIRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchCardDetails is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());

            IRequestProcessor<NativeBinCardHashAPIRequest, NativeBinCardHashAPIResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.FETCH_BIN_CARD_HASH_REQUEST);

            NativeBinCardHashAPIResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("fetchCardDetails", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "fetchCardDetails" + "to grafana", exception);
            }
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } catch (MidDoesnotMatchException mdnme) {
            LOGGER.error("MidDoesnotMatchException in  /fetchCardDetails {}", mdnme);
            throw mdnme;
        } catch (NativeFlowException | BinDetailException e) {
            LOGGER.error("NativeFlowException/BinDetailException in /fetchCardDetails {}",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception in /fetchCardDetails {}", e);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
        } finally {
            LOGGER.info("Total time taken for /fetchCardDetails is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/guest/fetchBinDetail", method = { RequestMethod.POST })
    public NativeBinDetailResponse fetchBinDetailGuest(
            @ApiParam(required = true) @RequestBody NativeBinDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /guest/fetchBinDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<NativeBinDetailRequest, NativeBinDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_BIN_DETAIL_REQUEST);
            // OfflinePaymentUtils.gethttpServletResponse().setHeader("Access-Control-Allow-Origin",
            // "*");
            NativeBinDetailResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /guest/fetchBinDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchBinDetail Guest is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }

    }
}
