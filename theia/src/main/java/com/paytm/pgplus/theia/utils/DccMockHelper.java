package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.model.DccPageData;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.common.model.DccPaymentDetailResponse;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.DccPaymentDetailRequest;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theiacommon.utils.AmountUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.ONUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.PAYMENT_CALL_DCC;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay.MERCHANT_NAME;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V1;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.PAYMENT_MODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;

@Service("dccMockHelper")
public class DccMockHelper {
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    @Qualifier("dccPageDataHelper")
    private DccPageDataHelper dccPageDataHelper;
    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;
    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(DccPageDataHelper.class);

    /*
     * this is a mock class for dev testing purpose, do not use for buisiness
     * logic
     */
    public DccPaymentDetail setDccPaymentDetails() {
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();
        dccPaymentDetail.setDccOffered("true");
        dccPaymentDetail.setDccId("79205282");
        dccPaymentDetail.setAmountPerUnitForeignAmount("70.00");
        dccPaymentDetail.setIsoForeignCurrencyCode("567");
        dccPaymentDetail.setForeignCurrencyName("dollar");
        dccPaymentDetail.setForeignCurrencySymbol("$");
        dccPaymentDetail.setForeignPayableAmount("104.00");
        dccPaymentDetail.setForeignPaymentAmount("100.00");
        dccPaymentDetail.setForeignMarkupAmount("4.00");
        dccPaymentDetail.setForeignMarkupRatePercentage("4.000");
        dccPaymentDetail.setExpirationTimestamp("ss");
        dccPaymentDetail.setExchangeRateSourceName("dsds");
        dccPaymentDetail.setForeignCurrencyCode("USD");
        return dccPaymentDetail;
    }

    public DccPaymentDetailResponse setDccPaymentDetailResponse() {
        DccPaymentDetailResponse dccPaymentDetailResponse = new DccPaymentDetailResponse();
        dccPaymentDetailResponse.setApiStatus("Success");
        dccPaymentDetailResponse.setDccOffered(true);
        dccPaymentDetailResponse.setResponseTimeStamp(Long.toString(System.currentTimeMillis()));
        dccPaymentDetailResponse.setOrderId("myOrder1");
        dccPaymentDetailResponse.setDccId("12345677");
        dccPaymentDetailResponse.setAmountPerUnitForeignAmount("70.00");
        dccPaymentDetailResponse.setIsoForeignCurrencyCode("567");
        dccPaymentDetailResponse.setForeignCurrencyCode("USD");
        dccPaymentDetailResponse.setForeignCurrencySymbol("$");
        dccPaymentDetailResponse.setForeignPayableAmount("104.00");
        dccPaymentDetailResponse.setForeignPaymentAmount("100.00");
        dccPaymentDetailResponse.setForeignMarkupAmount("4.00");
        dccPaymentDetailResponse.setForeignMarkupRatePercentage("4.000");
        dccPaymentDetailResponse.setExpirationTimestamp("2018-06-29T19:06:00.000+02:00");
        dccPaymentDetailResponse.setExchangeRateSourceName("REUTERS WHOLESALE INTERBANK");
        dccPaymentDetailResponse.setResponseMessage("Success");
        dccPaymentDetailResponse.setForeignCurrencyName("dollar");
        return dccPaymentDetailResponse;
    }

    public void buildDccData1(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {

        DccPaymentDetailRequest dccPaymentDetailRequest = createDccPaymentRequest(requestData, flowRequestBean);

        // call to insta

        IRequestProcessor<DccPaymentDetailRequest, DccPaymentDetail> dccFetchRatesRequestProcessor = requestProcessorFactory

        .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_DCC_RATES);

        DccPaymentDetail dccPaymentDetail = null;

        try {

            dccPaymentDetail = dccFetchRatesRequestProcessor.process(dccPaymentDetailRequest);
            dccPaymentDetail = setDccPaymentDetails();

            if (dccPaymentDetail != null) {

                dccPageDataHelper.setDCCPageDataToCache(requestData, flowRequestBean, dccPaymentDetail);

            }

        } catch (Exception e) {

            LOGGER.info("Error in fetching rates from InstaProxy {}", e);

        }

    }

    private DccPaymentDetailRequest createDccPaymentRequest(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) {

        DccPaymentDetailRequest dccPaymentDetailRequest = new DccPaymentDetailRequest();

        dccPaymentDetailRequest.setVersion("v1");

        dccPaymentDetailRequest.setRequestTimeStamp(Long.toString(System.currentTimeMillis()));

        dccPaymentDetailRequest.setAmount(AmountUtils.formatNumberToTwoDecimalPlaces(requestData.getTxnAmount()));

        dccPaymentDetailRequest.setClient("Theia");

        dccPaymentDetailRequest.setBankCode("ICICIPAY");

        dccPaymentDetailRequest.setMid(requestData.getMid());

        dccPaymentDetailRequest.setOrderId(requestData.getOrderId());

        dccPaymentDetailRequest.setBin(flowRequestBean.getCardNo().substring(0, 6));

        dccPaymentDetailRequest.setExtendedInfo("NA");

        dccPaymentDetailRequest.setPayMode(requestData.getPaymentTypeId());

        return dccPaymentDetailRequest;

    }

    public void setDccPageData(String mid, String orderId, String txnToken) {
        DccPageData dccPageData = new DccPageData();
        dccPageData.setPageType(DCC);
        FormDetail cancelFormData = getCancelFormData(mid, orderId, txnToken);
        dccPageData.getForms().setCancelForm(cancelFormData);
        Map<String, Object> payformData = getPayFormData(mid, orderId, txnToken);
        dccPageData.getForms().setPayForm(payformData);
        Map<String, Object> displayMerchantInfo = getDisplayMerchantInfo(mid, orderId);
        dccPageData.setDisplayField(displayMerchantInfo);
        dccPageData.setDccPaymentDetails(setDccPaymentDetails());
        nativeSessionUtil.setDccPageData(txnToken, dccPageData);

    }

    private Map<String, Object> getDisplayMerchantInfo(String mid, String orderId) {

        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(mid);
        MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService.getMerchantInfo(merchantDetailsRequest);
        if (merchantDetailsResponse == null) {
            LOGGER.error("Not able to fetch merchant logo");
            return null;
        } else {
            Map<String, Object> merchantDetails = new TreeMap<>();
            if (StringUtils.isNotBlank(merchantDetailsResponse.getMerchantBusinessName())) {
                merchantDetails.put(MERCHANT_NAME, merchantDetailsResponse.getMerchantBusinessName());
            } else {
                merchantDetails.put(MERCHANT_NAME, merchantDetailsResponse.getMerchantDisplayName());
            }
            if (StringUtils.isNotBlank(merchantDetailsResponse.getMerchantImageName())) {
                merchantDetails.put(MERCHANT_LOGO_URL, merchantDetailsResponse.getMerchantImageName());
                merchantDetails.put(MERCHANT_LOGO_FLAG, Boolean.TRUE);

            } else {
                merchantDetails.put(MERCHANT_LOGO_FLAG, Boolean.FALSE);
            }
            merchantDetails.put(MID, mid);
            merchantDetails.put(ORDER_ID, orderId);
            merchantDetails.put(AMOUNT, AmountUtils.formatNumberToTwoDecimalPlaces("1000"));// change//
            // after//
            // e2e
            boolean onUsMerchant = merchantExtendInfoUtils.isMerchantOnPaytm(mid);
            merchantDetails.put(ONUS, onUsMerchant);

            return merchantDetails;

        }

    }

    private Map<String, Object> getPayFormData(String mid, String orderId, String txnToken) {
        Map<String, Object> payFormData = new TreeMap<>();
        payFormData.put(ACTION_URL, getPayActionUrl(mid, orderId));
        payFormData.put(METHOD, POST);
        payFormData.put(TYPE, PAY);
        Map<String, String> headers = new HashMap<>();
        headers.put(VERSION, Version_V1);
        headers.put(TXN_TOKEN, txnToken);
        headers.put(WORKFLOW, null);
        payFormData.put(HEADERS, headers);
        Map<String, Object> body = new HashMap<>();
        body.put(PAYMENT_MODE, "CREDIT_CARD");
        body.put(MID, mid);
        body.put(ORDER_ID, orderId);
        payFormData.put(BODY, body);
        return payFormData;

    }

    private FormDetail getCancelFormData(String mid, String orderId, String txnToken) {
        FormDetail cancelFormData = new FormDetail();
        cancelFormData.setMethod(POST);
        cancelFormData.setType(CANCEL);
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, CONTENT_VALUE);
        cancelFormData.setHeaders(headers);
        Map<String, String> content = new TreeMap<>();
        // change accordingly
        String workflow = ENHANCED_CASHIER_FLOW;
        switch (workflow) {
        case ENHANCED_CASHIER_FLOW:
            cancelFormData.setActionUrl(getCancelEnhanceActionURL());
            content.put(MID, mid);
            content.put(ORDER_ID, orderId);
            content.put(TXN_TOKEN, txnToken);
            content.put(PAYMENT_CALL_DCC, Boolean.toString(BOOLEAN_TRUE));
            cancelFormData.setContent(content);
            break;
        case CHECKOUT:
            cancelFormData.setType(CHECKOUT_CANCEL);
            break;
        default:
            TransactionResponse txnResp = getTransactionResponseForCancelDcc(txnToken);
            if (txnResp != null) {
                cancelFormData.setActionUrl(txnResp.getCallbackUrl());
            }
            Map<String, String> paramMap = new TreeMap<>();
            merchantResponseService.makeReponseToMerchantEnhancedNative(txnResp, paramMap);
            cancelFormData.setContent(paramMap);
            break;

        }
        return cancelFormData;
    }

    private TransactionResponse getTransactionResponseForCancelDcc(String txnToken) {
        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getNativeInitiateRequest(txnToken)
                .getInitiateTxnReq().getBody();
        NativeRetryInfo retryInfo = new NativeRetryInfo(Boolean.TRUE, DCC_PAGE_BACK_BUTTON_MESSAGE);
        ResultCode resultCode = ResultCode.FAILED;
        ResultInfo nativeResultInfo = new ResultInfo(resultCode.getResultStatus(), resultCode.getResultCodeId(),
                resultCode.getCode(), resultCode.getResultMsg());
        nativeResultInfo.setRedirect(Boolean.FALSE);
        TransactionResponse txnResp = merchantResponseService.createNativeRequestForMerchant(orderDetail, retryInfo,
                nativeResultInfo, Boolean.FALSE, DCC_PAGE_BACK_BUTTON_MESSAGE);
        return txnResp;
    }

    private String getCancelEnhanceActionURL() {
        StringBuilder queryBuilder = new StringBuilder();
        String theiaBaseURL = ConfigurationUtil.getProperty(THEIA_BASE_URL);
        queryBuilder.append(theiaBaseURL).append(NATIVE_APP_INVOKE_URL).append("?errorMessage=")
                .append(DCC_PAGE_BACK_BUTTON_MESSAGE);
        return queryBuilder.toString();
    }

    private Object getPayActionUrl(String mid, String orderId) {
        StringBuilder queryBuilder = new StringBuilder();
        String theiaBaseURL = ConfigurationUtil.getProperty(THEIA_BASE_URL);
        queryBuilder.append(theiaBaseURL).append(V1_PTC).append("?mid=").append(mid).append("&orderId=")
                .append(orderId);
        return queryBuilder.toString();
    }

}
