package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnRequest;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnRequestBody;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnResponse;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.ImeiValidateRequestBody;
import com.paytm.pgplus.theia.nativ.exception.ImeiValidationException;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.RouterUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.paytm.pgplus.dynamicwrapper.utils.JSONUtils.toJsonString;
import static com.paytm.pgplus.facade.enums.EdcLinkValidationServiceUrl.VALIDATION_MODEL_POST_TXN;
import static com.paytm.pgplus.facade.enums.EdcLinkValidationServiceUrl.VALIDATION_MODEL_PRE_TXN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EdcEmiAdditionalFields.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_BANK_OFFER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_EMI;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.IMEI_KEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay.TOTAL_TXN_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRUE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.KYB_ID;

@Service
public class NativeImeiValidationProcessor
        extends
        AbstractRequestProcessor<ImeiValidateRequestBody, ValidationServicePreTxnResponse, ImeiValidateRequestBody, ValidationServicePreTxnResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeImeiValidationProcessor.class);

    @Autowired
    private MerchantExtendInfoUtils merchantExtendedInfo;
    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private IAcquiringOrder acquiringOrder;
    @Autowired
    private RouterUtil routerUtil;
    @Autowired
    private Environment environment;

    @Override
    protected ImeiValidateRequestBody preProcess(ImeiValidateRequestBody request) throws ImeiValidationException {
        validateMandatoryParams(request);
        return request;
    }

    @Override
    protected ValidationServicePreTxnResponse onProcess(ImeiValidateRequestBody request,
            ImeiValidateRequestBody serviceRequest) throws ImeiValidationException {
        ValidationServicePreTxnResponse validationServicePreTxnResponse = null;

        ValidationServicePreTxnRequest validationServicePreTxnRequest = getImeiValidationRequest(request);
        LOGGER.info("Imei validation Mode Request received {} ", validationServicePreTxnRequest);
        final Map<String, String> queryMap = prepareQueryParams(request);
        final MultivaluedMap<String, Object> headerMap = prepareHeaderMap(request);

        try {
            if (StringUtils.equalsIgnoreCase("UNBLOCK", request.getAction())) {
                validationServicePreTxnRequest.getBody().setPaymentStatus("FAILURE");
                validationServicePreTxnResponse = executePostV2(validationServicePreTxnRequest,
                        VALIDATION_MODEL_POST_TXN.getUrl(), ValidationServicePreTxnResponse.class, queryMap, headerMap,
                        ExternalEntity.VALIDATION_MODEL);
            } else if (StringUtils.isBlank(request.getAction())
                    || StringUtils.equalsIgnoreCase("BLOCK", request.getAction())) {
                validationServicePreTxnResponse = executePostV2(validationServicePreTxnRequest,
                        VALIDATION_MODEL_PRE_TXN.getUrl(), ValidationServicePreTxnResponse.class, queryMap, headerMap,
                        ExternalEntity.VALIDATION_MODEL);
            } else {
                throw new ImeiValidationException(ResultCode.INVALID_ACTION);
            }
        } catch (FacadeCheckedException fce) {
            throw new ImeiValidationException(ResultCode.VALIDATION_API_FAILURE);
        }

        return validationServicePreTxnResponse;
    }

    @Override
    protected ValidationServicePreTxnResponse postProcess(ImeiValidateRequestBody request,
            ImeiValidateRequestBody serviceRequest, ValidationServicePreTxnResponse serviceResponse) {
        boolean isValidated = validateValidationModelResponse(serviceResponse);
        if (!isValidated) {
            throw new ImeiValidationException(ResultCode.VALIDATION_API_FAILURE);
        }
        return serviceResponse;
    }

    private void validateMandatoryParams(ImeiValidateRequestBody request) {
        if (StringUtils.isBlank(request.getMid())) {
            throw new ImeiValidationException(ResultCode.MISSING_MANDATORY_ELEMENT_MID);
        } else if (StringUtils.isBlank(request.getOrderId())) {
            throw new ImeiValidationException(ResultCode.MISSING_MANDATORY_ELEMENT_ORDERID);
        } else if (StringUtils.isBlank(request.getSkuCode())) {
            throw new ImeiValidationException(ResultCode.MISSING_MANDATORY_ELEMENT_SKUCODE);
        } else if (StringUtils.isBlank(request.getImei())) {
            throw new ImeiValidationException(ResultCode.MISSING_MANDATORY_ELEMENT_IMEI);
        } else if (StringUtils.isBlank(request.getBrandId())) {
            throw new ImeiValidationException(ResultCode.MISSING_MANDATORY_ELEMENT_BRANDID);
        }
    }

    public boolean validateValidationModelResponse(ValidationServicePreTxnResponse validationServicePreTxnResponse) {
        if (validationServicePreTxnResponse == null || validationServicePreTxnResponse.getBody() == null)
            return false;

        return true;
    }

    private <Req, Resp> Resp executePostV2(Req request, String url, Class<Resp> respClass,
            Map<String, String> queryParams, MultivaluedMap<String, Object> headerMap, ExternalEntity externalEntity)
            throws FacadeCheckedException {
        long startTime = System.currentTimeMillis();
        final HttpRequestPayload<String> payload = generatePayloadV2(request, url, headerMap, queryParams);
        try {
            LogUtil.logPayload(externalEntity, url, Type.REQUEST, payload.toString());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);
            final String responseEntity = response.readEntity(String.class);
            final Resp responseObject = JsonMapper.mapJsonToObject(responseEntity, respClass);
            String responseString = toJsonString(responseObject);
            LogUtil.logResponsePayload(externalEntity, url, Type.RESPONSE, responseString, startTime);
            return responseObject;
        } catch (final Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private <T> HttpRequestPayload<String> generatePayloadV2(final T request, String url,
            MultivaluedMap<String, Object> headerMap, Map<String, String> queryParams) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        payload.setTarget(url);
        payload.setHeaders(headerMap);
        payload.setHttpMethod(HttpMethod.POST);
        payload.setQueryParameters(queryParams);
        String requestBody = generateBody(request);
        payload.setEntity(requestBody);
        return payload;
    }

    private <T> String generateBody(final T request) throws FacadeCheckedException {
        if (request == null)
            return null;
        return JsonMapper.mapObjectToJson(request);
    }

    public ValidationServicePreTxnRequest getImeiValidationRequest(ImeiValidateRequestBody imeiValidateRequestBody)
            throws ImeiValidationException {
        ValidationServicePreTxnRequest request = new ValidationServicePreTxnRequest();
        SecureRequestHeader secureRequestHeader = new SecureRequestHeader();
        String clientId = ConfigurationUtil.getProperty(FacadeConstants.VALIDATION_SERVICE_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.VALIDATION_SERVICE_CLIENT_SECRET);
        secureRequestHeader.setClientId(clientId);
        secureRequestHeader.setSignature(clientSecret);
        request.setHead(secureRequestHeader);
        ValidationServicePreTxnRequestBody requestBody = new ValidationServicePreTxnRequestBody();
        requestBody.setMid(imeiValidateRequestBody.getMid());
        requestBody.setOrderId(imeiValidateRequestBody.getOrderId());
        requestBody.setValidationMode(ConfigurationUtil.getProperty(imeiValidateRequestBody.getBrandId(), "2"));
        requestBody.setValidationInfo(populateValidationModelInfo(imeiValidateRequestBody));
        request.setBody(requestBody);
        return request;
    }

    private ValidationModelInfo populateValidationModelInfo(ImeiValidateRequestBody imeiValidateRequestBody)
            throws ImeiValidationException {
        ValidationModelInfo validationModelInfo = new ValidationModelInfo();
        validationModelInfo.setClientInfo(populateClientInfo(imeiValidateRequestBody));
        validationModelInfo.setProductInfo(populateProductInfo(imeiValidateRequestBody));
        validationModelInfo.setSerialInfo(populateSerialInfo(imeiValidateRequestBody));
        QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = getQueryByMerchantTransIdResponse(imeiValidateRequestBody);
        checkForSuccessAndRefundTxn(imeiValidateRequestBody, queryByMerchantTransIdResponse);
        Map<String, String> payOptionBillExtendInfo = getLatestPayOptionBillExtendInfo(queryByMerchantTransIdResponse);
        LOGGER.info("payOptionBillExtendInfo : {}", payOptionBillExtendInfo);
        validationModelInfo.setTransactionInfo(populateTransactionInfo(queryByMerchantTransIdResponse,
                payOptionBillExtendInfo));
        validationModelInfo.setAdditionalDetails(populateAdditionalDetails(imeiValidateRequestBody,
                payOptionBillExtendInfo));
        return validationModelInfo;
    }

    private ClientInfo populateClientInfo(ImeiValidateRequestBody imeiValidateRequestBody)
            throws ImeiValidationException {
        ClientInfo clientInfo = new ClientInfo();
        String kybId = merchantExtendedInfo.getKeyFromExtendInfo(imeiValidateRequestBody.getMid(), KYB_ID);
        if (StringUtils.isNotBlank(kybId)) {
            clientInfo.setKybId(kybId);
        } else {
            LOGGER.error("Got KYBID as null in merchantExtendedInfo call");
            throw new ImeiValidationException(ResultCode.INVALID_MID);
        }
        clientInfo.setStoreId(imeiValidateRequestBody.getMid());
        clientInfo.setSourceContext(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.SOURCE_CONTEXT);
        return clientInfo;
    }

    private SerialInfo populateSerialInfo(ImeiValidateRequestBody imeiValidateRequestBody) {
        SerialInfo serialInfo = new SerialInfo();
        serialInfo.setId(imeiValidateRequestBody.getImei());
        serialInfo.setType(IMEI_KEY);
        return serialInfo;
    }

    private ProductInfo populateProductInfo(ImeiValidateRequestBody imeiValidateRequestBody) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setBrandId(imeiValidateRequestBody.getBrandId());
        if (StringUtils.isNotBlank(imeiValidateRequestBody.getCategoryId())) {
            productInfo.setCategoryId(imeiValidateRequestBody.getCategoryId());
        } else {
            productInfo.setCategoryId(RandomStringUtils.random(12, true, true));
        }
        productInfo.setProductId(RandomStringUtils.random(12, true, true));
        productInfo.setSkuCode(imeiValidateRequestBody.getSkuCode());
        return productInfo;
    }

    private EdcLinkTransactionInfo populateTransactionInfo(
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse, Map<String, String> payOptionBillExtendInfo)
            throws ImeiValidationException {
        EdcLinkTransactionInfo transactionInfo = new EdcLinkTransactionInfo();
        transactionInfo.setTxnIdentifier(queryByMerchantTransIdResponse.getBody().getAcquirementId());
        transactionInfo.setUniqueIdentifier(queryByMerchantTransIdResponse.getBody().getAcquirementId());
        transactionInfo.setTxnAmount(payOptionBillExtendInfo.get(TOTAL_TXN_AMOUNT));
        EdcLinkPaymentMethod.ExtraInfo extraInfo = new EdcLinkPaymentMethod.ExtraInfo(
                payOptionBillExtendInfo.get("bankCode"), getEmiMonthsFromEmiInfo(payOptionBillExtendInfo));
        String paymentType = null;
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.EMI, queryByMerchantTransIdResponse.getBody()
                .getPaymentViews().get(0).getPayOptionInfos().get(0).getPayMethod().getMethod())) {
            paymentType = PAYMENT_TYPE_EMI;
        } else {
            paymentType = PAYMENT_TYPE_BANK_OFFER;
        }
        List<EdcLinkPaymentMethod> paymentMethods = Arrays.asList(new EdcLinkPaymentMethod(paymentType, extraInfo));
        transactionInfo.setPaymentMethod(paymentMethods);
        return transactionInfo;
    }

    private Map<String, String> populateAdditionalDetails(ImeiValidateRequestBody imeiValidateRequestBody,
            Map<String, String> payOptionBillExtendInfo) {

        Map<String, String> additionalDetails = new HashMap<>();
        additionalDetails.put(ORDERID, imeiValidateRequestBody.getOrderId());
        additionalDetails.put(MID, imeiValidateRequestBody.getMid());
        additionalDetails.put(IS_APPLE_EXCHANGE_SUPPORTED, TRUE);
        additionalDetails.put(INVOICE_NO, payOptionBillExtendInfo.get(INVOICE_NO));
        additionalDetails.put(TXN_AMOUNT, payOptionBillExtendInfo.get(TOTAL_TXN_AMOUNT));
        additionalDetails.put(CARD_TYPE, payOptionBillExtendInfo.get(TheiaConstant.RequestParams.CARD_TYPE));
        additionalDetails.put(TENURE, getEmiMonthsFromEmiInfo(payOptionBillExtendInfo));
        if (!StringUtils.equals(additionalDetails.get(TENURE), "0")) {
            additionalDetails.put(LOAN_AMOUNT, calculateLoanAmount(payOptionBillExtendInfo));
        } else {
            additionalDetails.put(LOAN_AMOUNT, "0");
        }

        additionalDetails.put(TID, RandomStringUtils.random(12, true, true));
        additionalDetails.put(ISSUER_BANK, payOptionBillExtendInfo.get(TheiaConstant.RetryConstants.ISSUING_BANK_NAME));
        return additionalDetails;
    }

    private QueryByMerchantTransIdResponse getQueryByMerchantTransIdResponse(
            ImeiValidateRequestBody imeiValidateRequestBody) throws ImeiValidationException {
        QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = null;
        try {
            GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                    .fetchMerchanData(imeiValidateRequestBody.getMid());
            String alipayId = null;
            if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
                alipayId = merchantMappingResponse.getResponse().getAlipayId();
            } else {
                LOGGER.error("Can't get alipayId against mid for imei block/unblock");
                throw new ImeiValidationException(ResultCode.INVALID_MID);
            }
            QueryByMerchantTransIdRequestBody requestBody = new QueryByMerchantTransIdRequestBody(alipayId,
                    imeiValidateRequestBody.getOrderId(), true);
            requestBody.setRoute(routerUtil.getRoute(imeiValidateRequestBody.getMid(),
                    imeiValidateRequestBody.getOrderId(), "queryByMerchantTransId"));
            QueryByMerchantTransIdRequest request = new QueryByMerchantTransIdRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), requestBody);
            request.getHead().setMerchantId(imeiValidateRequestBody.getMid());
            queryByMerchantTransIdResponse = acquiringOrder.queryByMerchantTransId(request);
        } catch (FacadeCheckedException ex) {
            LOGGER.error("Exception while fetching QueryByMerchantTransIdResponse for imei Block/Unblock {}", ex);
            throw new ImeiValidationException(ResultCode.INVALID_ORDERID);
        }
        return queryByMerchantTransIdResponse;
    }

    public Map<String, String> prepareQueryParams(ImeiValidateRequestBody imeiValidateRequestBody) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(TheiaConstant.RequestParams.MID, imeiValidateRequestBody.getMid());
        return queryParams;
    }

    public MultivaluedMap<String, Object> prepareHeaderMap(ImeiValidateRequestBody imeiValidateRequestBody) {
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add("content-type", MediaType.APPLICATION_JSON);
        headerMap.add("X-REQUEST-ID", UUID.randomUUID().toString());
        headerMap.add("X-CLIENT", "PG");
        headerMap.add("X-CLIENT-ID", imeiValidateRequestBody.getMid());
        return headerMap;
    }

    private String getEmiMonthsFromEmiInfo(Map<String, String> payOptionBillExtendedInfo) {
        Map<Object, Object> emiSubventionInfo = getEmiSubventionInfo(payOptionBillExtendedInfo);
        LOGGER.info("EmiSubvention Info : {}", emiSubventionInfo);
        if (emiSubventionInfo != null && emiSubventionInfo.get("tenure") != null) {
            return String.valueOf(emiSubventionInfo.get("tenure"));
        } else if (payOptionBillExtendedInfo != null
                && StringUtils.isNotBlank(payOptionBillExtendedInfo.get("emiInfo"))) {
            try {
                Map<String, String> emiInfoMap = JsonMapper.mapJsonToObject(payOptionBillExtendedInfo.get("emiInfo"),
                        Map.class);
                if (emiInfoMap != null && StringUtils.isNotBlank(emiInfoMap.get("emiMonths"))) {
                    return emiInfoMap.get("emiMonths");
                }
            } catch (FacadeCheckedException ex) {
                LOGGER.error("Exception occurred while parsing EmiInfo : {}", ex);
            }
        }
        return "0";
    }

    private Map<String, String> getLatestPayOptionBillExtendInfo(
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {

        if (queryByMerchantTransIdResponse != null && queryByMerchantTransIdResponse.getBody() != null
                && queryByMerchantTransIdResponse.getBody().getPaymentViews() != null) {
            int paymentViewsLength = queryByMerchantTransIdResponse.getBody().getPaymentViews().size();
            if (queryByMerchantTransIdResponse.getBody().getPaymentViews().get(paymentViewsLength - 1) != null) {
                PaymentView paymentView = queryByMerchantTransIdResponse.getBody().getPaymentViews()
                        .get(paymentViewsLength - 1);
                if (paymentView != null && paymentView.getPayOptionInfos() != null
                        && paymentView.getPayOptionInfos().get(0) != null
                        && paymentView.getPayOptionInfos().get(0).getPayOptionBillExtendInfo() != null) {
                    return paymentView.getPayOptionInfos().get(0).getPayOptionBillExtendInfo();
                }
            }
        }
        throw new ImeiValidationException(ResultCode.INVALID_ORDERID);
    }

    private Map<Object, Object> getEmiSubventionInfo(Map<String, String> payOptionBillExtendInfo) {
        if (payOptionBillExtendInfo != null && StringUtils.isNotBlank(payOptionBillExtendInfo.get("emiSubventionInfo"))) {
            try {
                Map<Object, Object> emiSubventionInfo = JsonMapper.mapJsonToObject(
                        payOptionBillExtendInfo.get("emiSubventionInfo"), Map.class);
                return emiSubventionInfo;
            } catch (FacadeCheckedException ex) {
                LOGGER.error("Exception occurred while parsing emiSubventionInfo : {}", ex);
            }
        }
        return null;
    }

    private String getEmiSubventionAmount(Map<Object, Object> emiSubventionInfo) {
        try {
            if (StringUtils.isNotBlank((String) emiSubventionInfo.get("subventionAmount"))
                    && emiSubventionInfo.get("gratificationDiscount") != null) {
                LOGGER.info("Setting Loan Amount, SubventionAmount : {} , GratificationDiscount : {}",
                        emiSubventionInfo.get("subventionAmount"), emiSubventionInfo.get("gratificationDiscount"));
                return String.valueOf(Double.valueOf((String) emiSubventionInfo.get("subventionAmount"))
                        - (Double) emiSubventionInfo.get("gratificationDiscount"));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception Occurred while calculating loan Amount : {}", ex);
            throw ex;
        }
        return null;
    }

    private String calculateLoanAmount(Map<String, String> payOptionBillExtendInfo) {
        Map<Object, Object> emiSubventionInfo = getEmiSubventionInfo(payOptionBillExtendInfo);
        LOGGER.info("EmiSubventionInfo : {}", emiSubventionInfo);
        if ((StringUtils.equalsIgnoreCase("HDFC", payOptionBillExtendInfo.get("bankCode")) || StringUtils
                .equalsIgnoreCase("SBI", payOptionBillExtendInfo.get("bankCode"))) && emiSubventionInfo != null) {
            String loanAmount = getEmiSubventionAmount(emiSubventionInfo);
            if (StringUtils.isNotBlank(loanAmount))
                return loanAmount;
        }
        return String.valueOf(Double.parseDouble(payOptionBillExtendInfo.get(TOTAL_TXN_AMOUNT)) / 100);
    }

    private void checkForSuccessAndRefundTxn(ImeiValidateRequestBody imeiValidateRequestBody,
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {
        if (StringUtils.equalsIgnoreCase(imeiValidateRequestBody.getAction(), "BLOCK")) {
            if (!StringUtils.equalsIgnoreCase(queryByMerchantTransIdResponse.getBody().getResultInfo().getResultCode(),
                    "SUCCESS")) {
                throw new ImeiValidationException(ResultCode.VALIDATION_FAIL_FAILED_TXN);
            }
            if (queryByMerchantTransIdResponse.getBody().getAmountDetail().getRefundAmount() != null) {
                throw new ImeiValidationException(ResultCode.VALIDATION_FAIL_REFUND_TXN);
            }
        }
    }
}
