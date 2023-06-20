package com.paytm.pgplus.theia.nativ.controller;

import com.google.common.io.CharStreams;
import com.paytm.pgplus.cache.model.MiscAttributeInfo;
import com.paytm.pgplus.cache.model.ResellerMasterInfo;
import com.paytm.pgplus.dynamicwrapper.exceptions.WrapperServiceException;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.facade.utils.JWTValidationUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMiscAttributeService;
import com.paytm.pgplus.mappingserviceclient.service.IResellerService;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.refund.enums.ResponseCode;
import com.paytm.pgplus.pgproxycommon.models.MultiReadHttpServletRequest;
import com.paytm.pgplus.pgproxycommon.utils.WixUtils;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.response.CustomInitTxnResponse;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.annotation.SignedResponseBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.CustomInitTxnException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailRequest;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.service.ICustomInitiateTransactionService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.WixUtil;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.utils.DynamicWrapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.SHOPIFY_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.WIX_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestHeaders.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.APMERSAND;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.QUESTION_MARK;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.RESELLER_PARENT_MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static com.paytm.pgplus.theia.utils.JWTValidationUtil.TXN_AMOUNT;

@NativeControllerAdvice
@Controller
@RequestMapping("api/v1")
public class NativeInitiateTransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeInitiateTransactionController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeInitiateTransactionController.class);

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    @Qualifier(value = "wrapperImpl")
    private IWrapperService wrapperService;

    @Autowired
    private IMiscAttributeService miscAttributeService;

    @Autowired
    private IResellerService resellerService;

    @Autowired
    private Environment environment;

    @Autowired
    private ICustomInitiateTransactionService customInitTxnService;

    @Autowired
    private WixUtil wixUtil;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @ApiOperation(value = "initiateTransaction", notes = "To start transaction for native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/initiateTransaction", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public InitiateTransactionResponse createTransaction(
            @ApiParam(required = true) @RequestBody InitiateTransactionRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /initiateTransaction is: {}", request);
            try {
                logNativeInitiateEvent(request);
            } catch (Exception e) {
                LOGGER.error("Problem while adding initiate request event", e.getMessage());
            }
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());

            NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
            nativeInitiateRequest.setInitiateTxnReq(request);

            IRequestProcessor<NativeInitiateRequest, InitiateTransactionResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.INITIATE_TRANSACTION_REQUEST);
            try {
                InitiateTransactionResponse response = requestProcessor.process(nativeInitiateRequest);
                try {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                    responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                    statsDUtils.pushResponse("initiateTransaction", responseMap);
                } catch (Exception exception) {
                    LOGGER.error("Error in pushing response message " + "initiateTransaction" + "to grafana", exception);
                }
                LOGGER.info("Native response returned for API: /initiateTransaction is: {}", response);
                nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                        .getBody()));
                return response;

            } catch (Exception e) {
                LOGGER.error("Exception occurred in Initiate Transaction - {}", e.getMessage());
                if (e instanceof BaseException && ((BaseException) e).getResultInfo() != null
                        && ((BaseException) e).getResultInfo().getResultCode() != null) {
                    String resultCode = ((BaseException) e).getResultInfo().getResultCode();
                    wixUtil.putErrorMsgInRedisForWix(request.getBody(), resultCode);
                } else if (e instanceof PaymentRequestValidationException
                        && ((PaymentRequestValidationException) e).getResultInfo() != null
                        && ((PaymentRequestValidationException) e).getResultInfo().getResultCode() != null) {
                    String resultCode = ((PaymentRequestValidationException) e).getResultInfo().getResultCode();
                    wixUtil.putErrorMsgInRedisForWix(request.getBody(), resultCode);
                }
                throw e;
            }
        } finally {
            EXT_LOGGER.customInfo("Total time taken for NativeInitiateTransaction is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @RequestMapping(value = "/customInitTxn", method = { RequestMethod.POST })
    public @ResponseBody CustomInitTxnResponse customCreateTransaction(final HttpServletRequest request)
            throws CustomInitTxnException {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Custom Initiate Transaction Flow Started.");
            String resellerParentMid = null;
            String childMid = null;
            String wrapperName = "";
            String wixMerchantId = "";

            ResponseCode responseCode = null;
            InitiateTransactionResponse errorResponse = null;
            String errorType = "";
            MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);
            if (StringUtils.isNotEmpty(request.getHeader(SHOPIFY_SHOP_DOMAIN))) {
                wrapperName = SHOPIFY_WRAPPER;
                MDC.put(SHOPIFY_REQUEST_ID, request.getHeader(SHOPIFY_REQUEST_ID));
                MiscAttributeInfo miscAttributeInfo = miscAttributeService.getMiscAttribute("SHOP_ID", "value",
                        request.getHeader(SHOPIFY_SHOP_DOMAIN));
                EXT_LOGGER.customInfo("Mapping response - MiscAttributeInfo :: {}", miscAttributeInfo);
                if (miscAttributeInfo != null && StringUtils.isNotEmpty(miscAttributeInfo.getMid())) {
                    childMid = miscAttributeInfo.getMid();
                    ResellerMasterInfo resellerMasterInfo = resellerService.getResellerMid(childMid);
                    EXT_LOGGER.customInfo("Mapping response - ResellerMasterInfo :: {}", resellerMasterInfo);
                    if (resellerMasterInfo != null) {
                        resellerParentMid = resellerMasterInfo.getResellerMid();
                    }
                }
            } else if (StringUtils.isNotEmpty(request.getHeader(WIX_JWT))) {
                StringBuilder responseStrBuilder = new StringBuilder(CharStreams.toString(multiReadHttpServletRequest
                        .getReader()));
                wixMerchantId = WixUtils.getWixMerchantId(responseStrBuilder);
                if (StringUtils.isNotEmpty(wixMerchantId)) {
                    childMid = wixMerchantId;
                    wrapperName = WIX_WRAPPER;
                    ResellerMasterInfo resellerMasterInfo = resellerService.getResellerMid(childMid);
                    EXT_LOGGER.customInfo("Mapping response - ResellerMasterInfo :: {}", resellerMasterInfo);
                    if (resellerMasterInfo != null) {
                        resellerParentMid = resellerMasterInfo.getResellerMid();
                    }
                    responseCode = WixUtils.validateJWTRequestDigest(String.valueOf(responseStrBuilder),
                            request.getHeader(WIX_JWT), wrapperName);
                    if (responseCode != null) {
                        errorResponse = customInitTxnService.createCustomInitResponse(responseCode);
                        errorType = "JWT";
                    }
                }
            } else {
                StringBuilder responseStrBuilder = new StringBuilder(CharStreams.toString(multiReadHttpServletRequest
                        .getReader()));
                if (WixUtils.checkWixMerchant(responseStrBuilder)) {
                    wixMerchantId = WixUtils.getWixMerchantId(responseStrBuilder);
                    if (StringUtils.isNotEmpty(wixMerchantId)) {
                        childMid = wixMerchantId;
                        wrapperName = WIX_WRAPPER;
                        ResellerMasterInfo resellerMasterInfo = resellerService.getResellerMid(childMid);
                        EXT_LOGGER.customInfo("Mapping response - ResellerMasterInfo :: {}", resellerMasterInfo);
                        if (resellerMasterInfo != null) {
                            resellerParentMid = resellerMasterInfo.getResellerMid();
                        }
                        responseCode = WixUtils.validateJWTRequestDigest(String.valueOf(responseStrBuilder),
                                request.getHeader(WIX_JWT), wrapperName);
                        if (responseCode != null) {
                            errorResponse = customInitTxnService.createCustomInitResponse(responseCode);
                            errorType = "JWT";
                        }
                    }
                }

            }
            LOGGER.info("MerchantConfPresent = {}", dynamicWrapperUtil.isDynamicWrapperConfigPresent(resellerParentMid,
                    API.INITIATE_TRANSACTION, PayloadType.REQUEST));
            if (dynamicWrapperUtil.isDynamicWrapperConfigPresent(resellerParentMid, API.INITIATE_TRANSACTION,
                    PayloadType.REQUEST)) {
                ModifiableHttpServletRequest servletRequestWrapper = new ModifiableHttpServletRequest(
                        multiReadHttpServletRequest, new HashMap<>());
                InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
                InitiateTransactionRequestBody body = wrapperService.wrapRequest(servletRequestWrapper,
                        resellerParentMid, API.INITIATE_TRANSACTION);
                body.setMid(childMid);
                if (StringUtils.isNotEmpty(request.getHeader(SHOPIFY_SHOP_DOMAIN))
                        || StringUtils.isNotEmpty(wixMerchantId)) {
                    body.getExtraParamsMap().put(RESELLER_PARENT_MID, resellerParentMid);
                }
                if (errorResponse == null && body.getExtraParamsMap() != null
                        && body.getExtraParamsMap().get("isWixcurrencyINR") != null
                        && (boolean) body.getExtraParamsMap().get("isWixcurrencyINR") == false) {
                    responseCode = WixUtils.updateWixCurrencyError();
                    if (responseCode != null) {
                        errorResponse = customInitTxnService.createCustomInitResponse(responseCode);
                        errorType = "CURRENCY";
                    }
                }
                MDC.put(ORDER_ID, body.getOrderId());
                MDC.put(MID, body.getMid());
                initiateTransactionRequest.setBody(body);
                CustomInitTxnResponse customInitTxnResponse = new CustomInitTxnResponse();
                if (errorResponse != null) {
                    LOGGER.info("Error Response : {}", errorResponse);
                    ResultInfo resultInfo = errorResponse.getBody().getResultInfo();
                    wixUtil.updateWixResponseData(customInitTxnResponse, resultInfo, body.getOrderId(), body.getMid(),
                            resellerParentMid, errorType, "");
                    LOGGER.info("Custom Initiate Transaction Response : {}", customInitTxnResponse);
                    return customInitTxnResponse;
                } else {
                    final Response response = callInitiateTransaction(initiateTransactionRequest, wrapperName);
                    if (response != null && response.getStatus() == 200) {
                        customInitTxnService.getCustomInitResponse(resellerParentMid, wrapperName, body,
                                customInitTxnResponse, response);
                        return customInitTxnResponse;
                    } else {
                        throw new Exception("Http Response Code Received for Initiate Transaction : "
                                + response.getStatus());
                    }
                }
            }
            return null;
        } catch (WrapperServiceException e) {
            throw new CustomInitTxnException(e);
        } catch (Exception e) {
            throw new CustomInitTxnException(e);
        } finally {
            LOGGER.info("Total time taken for CustomInitiateTransaction is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    private Response callInitiateTransaction(InitiateTransactionRequest initiateTransactionRequest, String wrapperName)
            throws IllegalPayloadException, HttpCommunicationException {
        SecureRequestHeader header = new SecureRequestHeader();
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(MID, initiateTransactionRequest.getBody().getMid());
        jwtClaims.put(ORDER_ID, initiateTransactionRequest.getBody().getOrderId());
        jwtClaims.put(TXN_AMOUNT, initiateTransactionRequest.getBody().getTxnAmount().getValue());

        header.setTokenType("JWT");
        header.setClientId(wrapperName);

        header.setToken(JWTValidationUtil.createJWTToken(jwtClaims, "ts",
                environment.getProperty(JWT_INITIATE_SECRET + wrapperName)));
        initiateTransactionRequest.setHead(header);

        final HttpRequestPayload<Object> requestPayload = new HttpRequestPayload<>();
        requestPayload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        requestPayload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        requestPayload.setEntity(initiateTransactionRequest);
        StringBuilder targetUrl = new StringBuilder(
                com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.THEIA_INTERNAL_BASE_URL));
        targetUrl.append(TheiaConstant.ExtraConstants.NATIV_INITIATE_TRANSACTION_URL).append(QUESTION_MARK)
                .append("mid=").append(initiateTransactionRequest.getBody().getMid()).append(APMERSAND)
                .append("orderId=").append(initiateTransactionRequest.getBody().getOrderId());
        requestPayload.setTarget(targetUrl.toString());
        return JerseyHttpClient.sendHttpPostRequest(requestPayload);
    }

    private void logNativeInitiateEvent(InitiateTransactionRequest request) {
        Map<String, String> metadata = new HashMap<String, String>();
        if (request != null && request.getBody() != null) {
            boolean paymentOffersExists = (request.getBody().getPaymentOffersApplied() != null) ? true : false;
            metadata.put(TheiaConstant.EventLogConstants.PAYMENT_OFFERS_APPLIED_EXISTS,
                    String.valueOf(paymentOffersExists));
        }
        nativePaymentUtil.logNativeRequests(request.getHead().toString(), metadata);
    }

    @ApiOperation(value = "updateTransactionDetail", notes = "To update transaction details for native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/updateTransactionDetail", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public UpdateTransactionDetailResponse updateTransactionDetail(
            @ApiParam(required = true) @RequestBody UpdateTransactionDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /updateTransactionDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<UpdateTransactionDetailRequest, UpdateTransactionDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.UPDATE_TRANSACTION_DETAIL_REQUEST);
            UpdateTransactionDetailResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /updateTransactionDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for updateTransactionDetail is {} ms", System.currentTimeMillis() - startTime);
        }
    }

}