package com.paytm.pgplus.theia.nativ.service.impl;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponseBody;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.payloadvault.merchant.status.enums.ApiResponse;
import com.paytm.pgplus.payloadvault.refund.enums.ResponseCode;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.*;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.service.ICustomInitiateTransactionService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.WixUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.USE_NEW_THEIA_BASED_URL;
import static com.paytm.pgplus.facade.enums.ProductCodes.EDCPayConfirmAcquiringProd;
import static com.paytm.pgplus.payloadvault.merchant.status.enums.ApiResponse.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.SHOPIFY_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.WIX_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ResponseConstants.WIX_DEFAULT_REASON_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service
public class CustomInitiateTransactionService implements ICustomInitiateTransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomInitiateTransactionService.class);

    @Autowired
    private IAcquiringOrder acquiringOrderImpl;

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    private WixUtil wixUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public InitiateTransactionResponse createCustomInitResponse(ResponseCode responseCode) {
        ResultCode resultCode = getResultCodeFromResponseCode(responseCode);
        InitiateTransactionResponseBody responseBody = new InitiateTransactionResponseBody();
        responseBody.setResultInfo(NativePaymentUtil.resultInfo(resultCode));
        SecureResponseHeader responseHeader = new SecureResponseHeader();
        InitiateTransactionResponse response = new InitiateTransactionResponse(responseHeader, responseBody);
        return response;
    }

    private ResultCode getResultCodeFromResponseCode(ResponseCode responseCode) {
        ResultCode resultCode = null;
        if (responseCode == ResponseCode.JWT_HASH_MISMATCH)
            resultCode = ResultCode.JWT_HASH_MISMATCH;
        else if (responseCode == ResponseCode.EXPIRED_JWT)
            resultCode = ResultCode.EXPIRED_JWT;
        else if (responseCode == ResponseCode.INVALID_JWT)
            resultCode = ResultCode.INVALID_JWT;
        else if (responseCode == ResponseCode.JWT_MISSING)
            resultCode = ResultCode.JWT_MISSING;
        else if (responseCode == ResponseCode.CURRENCY_NOT_SUPPORTED)
            resultCode = ResultCode.CURRENCY_NOT_SUPPORTED;
        return resultCode;
    }

    /**
     * @param alipayMid
     * @return
     * @throws FacadeInvalidParameterException
     * @throws FacadeCheckedException
     */
    public QueryByMerchantTransIdResponse getMerchantTransIdResponse(String OrderId, String alipayMid, String paytmMid)
            throws Exception {

        QueryByMerchantTransIdRequestBody body = new QueryByMerchantTransIdRequestBody(alipayMid, OrderId, true, false);
        ApiFunctions apiFunction = ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID;
        QueryByMerchantTransIdRequest queryByMerchantTransIdRequest = new QueryByMerchantTransIdRequest(
                RequestHeaderGenerator.getHeader(apiFunction), body);
        if (StringUtils.isNotBlank(paytmMid)) {
            queryByMerchantTransIdRequest.getHead().setMerchantId(paytmMid);
        }
        return acquiringOrderImpl.queryByMerchantTransId(queryByMerchantTransIdRequest);
    }

    /**
     * @param queryByMerchantTransIdResponseBody
     * @param txnStatusResponse
     */
    public void setStatusDetails(QueryByMerchantTransIdResponseBody queryByMerchantTransIdResponseBody,
            CustomInitTxnResponse txnStatusResponse, String resellerParentMid, String redirectUrl)
            throws MappingServiceClientException {
        SystemResponseCode systemResponseCode = null;
        String responseStatus = "";
        if (queryByMerchantTransIdResponseBody.getStatusDetail() != null) {
            AcquirementStatusType status = queryByMerchantTransIdResponseBody.getStatusDetail().getAcquirementStatus();
            ApiResponse apiResponse = null;
            switch (status) {
            case SUCCESS:
                txnStatusResponse.setPluginTransactionId(queryByMerchantTransIdResponseBody.getExtendInfo().get(
                        "extraParamsMap.MERC_UNQ_REF"));
                break;
            case INIT:
            case PAYING:
                apiResponse = SALE_PENDING_WITHOUT_BANKNAME;
                systemResponseCode = SystemResponseCode.SALE_PENDING_WITHOUT_BANKNAME;
                if (EDCPayConfirmAcquiringProd.getId().equals(queryByMerchantTransIdResponseBody.getProductCode())) {
                    apiResponse = SALE_SUCCESS;
                    systemResponseCode = SystemResponseCode.SUCCESS;
                    queryByMerchantTransIdResponseBody.getStatusDetail().setAcquirementStatus(
                            AcquirementStatusType.SUCCESS);
                }
                break;
            case CLOSED:
                Map<String, Object> apiResponseForPaymentError = getApiResponseForPaymentError(queryByMerchantTransIdResponseBody);
                systemResponseCode = (SystemResponseCode) apiResponseForPaymentError
                        .get(TheiaConstant.ResponseConstants.SYSTEM_RESPCODE);
                apiResponse = (ApiResponse) apiResponseForPaymentError
                        .get(TheiaConstant.ResponseConstants.API_RESPONSE);
                break;
            default:
                break;
            }
            if (apiResponse != null) {
                responseStatus = apiResponse.getStatus();
                setTransactionResponseCodeAndMessage(
                        txnStatusResponse,
                        StringUtils.isNotBlank(merchantResponseUtil
                                .getInstErrorCodeUsingQueryByMerchantIdResponse(queryByMerchantTransIdResponseBody)) ? merchantResponseUtil
                                .getInstErrorCodeUsingQueryByMerchantIdResponse(queryByMerchantTransIdResponseBody)
                                : merchantResponseUtil
                                        .getPaymentErrorCodeUsingQueryByMerchantIdResponse(queryByMerchantTransIdResponseBody),
                        systemResponseCode, responseStatus, resellerParentMid, redirectUrl);
            }
        }
    }

    @Override
    public void setTransactionResponseCodeAndMessage(CustomInitTxnResponse customInitTxnResponse, String instErrorCode,
            SystemResponseCode systemResponseCode, String responseStatus, String resellerParentMid, String redirectUrl)
            throws MappingServiceClientException {

        ResponseCodeDetails responseCodeDetails = merchantResponseUtil.fetchResponseCodeDetails(instErrorCode,
                systemResponseCode, responseStatus);
        if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
            if (StringUtils.equals(responseStatus, "PENDING")) {
                customInitTxnResponse.setRedirectUrl(redirectUrl);

            } else {
                customInitTxnResponse.setErrorCode(responseCodeDetails.getResponseCode());
                customInitTxnResponse.setErrorDescription(merchantResponseUtil.getRespMessage(responseCodeDetails));
                String responseCodeAndMsgForWix = null;
                responseCodeAndMsgForWix = merchantResponseUtil.getReasonCodeFromMapping(
                        responseCodeDetails.getResultCode(), resellerParentMid);
                if (responseCodeAndMsgForWix != null && StringUtils.isNotEmpty(responseCodeAndMsgForWix)) {
                    customInitTxnResponse.setReasonCode(Integer.valueOf(responseCodeAndMsgForWix));
                } else {
                    customInitTxnResponse.setReasonCode(WIX_DEFAULT_REASON_CODE);
                }
            }
        }
    }

    private Map<String, Object> getApiResponseForPaymentError(
            QueryByMerchantTransIdResponseBody queryByMerchantTransIdResponseBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentView paymentView = fetchPaymentViews(queryByMerchantTransIdResponseBody);
            Map<String, String> extendedInfo = paymentView.getExtendInfo();
            if (BALANCE_NOT_ENOUGH.getResponseMessage().equals(
                    extendedInfo.get(TheiaConstant.ExtraConstants.PAYMENT_ERROR_CODE))) {
                if (StringUtils.isNotBlank(queryByMerchantTransIdResponseBody.getExtendInfo().get(
                        TheiaConstant.ExtraConstants.SUBSCRIPTION_TYPE))) {
                    response.put(TheiaConstant.ResponseConstants.API_RESPONSE, BALANCE_NOT_ENOUGH_SUBS);
                    response.put(TheiaConstant.ResponseConstants.SYSTEM_RESPCODE,
                            SystemResponseCode.BALANCE_NOT_ENOUGH_SUBS);
                    return response;
                } else {
                    response.put(TheiaConstant.ResponseConstants.API_RESPONSE, BALANCE_NOT_ENOUGH);
                    response.put(TheiaConstant.ResponseConstants.SYSTEM_RESPCODE, SystemResponseCode.BALANCE_NOT_ENOUGH);
                    return response;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception in getApiResponseForPaymentError : {} ", ex.getMessage());
        }
        response.put(TheiaConstant.ResponseConstants.API_RESPONSE, ORDER_CLOSE);
        response.put(TheiaConstant.ResponseConstants.SYSTEM_RESPCODE, SystemResponseCode.ORDER_CLOSE);
        return response;
    }

    private PaymentView fetchPaymentViews(QueryByMerchantTransIdResponseBody queryByMerchantTransIdResponseBody) {
        return queryByMerchantTransIdResponseBody.getPaymentViews().get(TheiaConstant.ExtraConstants.FIRST_ELEMENT);
    }

    public void getCustomInitResponse(String resellerParentMid, String wrapperName,
            InitiateTransactionRequestBody body, CustomInitTxnResponse customInitTxnResponse, Response response)
            throws Exception {
        final String responseString = response.readEntity(String.class);
        InitiateTransactionResponse initTxnResponse = JsonMapper.mapJsonToObject(responseString,
                InitiateTransactionResponse.class);
        ResultInfo resultInfo = initTxnResponse.getBody().getResultInfo();
        String resultStatus = resultInfo.getResultStatus();
        String resultCode = resultInfo.getResultCode();
        StringBuilder redirectUrl = new StringBuilder();
        String baseUrl = null;
        boolean flagUseNewBaseUrl = ff4jUtils.isFeatureEnabledOnMid(resellerParentMid, USE_NEW_THEIA_BASED_URL, false);
        if (flagUseNewBaseUrl) {
            LOGGER.info("New base Url is getting picked");
            baseUrl = ff4jUtils.getPropertyAsStringWithDefault(THEIA_NEW_BASE_URL,
                    ConfigurationUtil.getProperty(THEIA_BASE_URL));
        } else {
            baseUrl = ConfigurationUtil.getProperty(THEIA_BASE_URL);
        }
        redirectUrl.append(baseUrl).append(NATIVE_APP_INVOKE_URL_V2).append(QUESTION_MARK).append("mid=")
                .append(body.getMid()).append(APMERSAND).append("orderId=").append(body.getOrderId()).append(APMERSAND)
                .append("txnToken=").append(initTxnResponse.getBody().getTxnToken());
        if (!flagUseNewBaseUrl) {
            redirectUrl
                    .append(APMERSAND)
                    .append("amount=")
                    .append(body.getTxnAmount() != null ? body.getTxnAmount().getValue() : "0")
                    .append(APMERSAND)
                    .append("sourceUrl=")
                    .append(ff4jUtils.getPropertyAsStringWithDefault(THEIA_NEW_BASE_URL,
                            ConfigurationUtil.getProperty(THEIA_BASE_URL))).append(NATIVE_APP_INVOKE_URL_V2);
        }
        if ((StringUtils.equals(resultCode, ResultCode.SUCCESS_IDEMPOTENT_ERROR.getResultCodeId()) || StringUtils
                .equals(resultStatus, ResultCode.FAILED.getResultStatus()))
                && StringUtils.equals(WIX_WRAPPER, wrapperName)) {
            wixUtil.updateWixResponseData(customInitTxnResponse, resultInfo, body.getOrderId(), body.getMid(),
                    resellerParentMid, "", redirectUrl.toString());
        } else {
            if (StringUtils.equals(SHOPIFY_WRAPPER, wrapperName)) {
                customInitTxnResponse.setRedirectUrlShopify(redirectUrl.toString());
            } else {
                customInitTxnResponse.setRedirectUrl(redirectUrl.toString());
            }

            if (StringUtils.equals(WIX_WRAPPER, wrapperName))
                customInitTxnResponse.setPluginTransactionId(body.getOrderId());
        }
        LOGGER.info("Custom Initiate Transaction Response : {}", customInitTxnResponse);
    }
}
