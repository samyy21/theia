package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.model.DccPageData;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
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
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PAYMENT_MODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V1;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;

@Service("dccPageDataHelper")
public class DccPageDataHelper {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DccPageDataHelper.class);

    public void setDCCPageDataToCache(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean,
            DccPaymentDetail dccPaymentDetail) {
        DccPageData dccPageData = new DccPageData();
        dccPageData.setPageType(DCC);
        FormDetail cancelFormData = getCancelFormData(requestData);
        dccPageData.getForms().setCancelForm(cancelFormData);
        Map<String, Object> payformData = getPayFormData(requestData, flowRequestBean.getPayMethod());
        dccPageData.getForms().setPayForm(payformData);
        Map<String, Object> displayMerchantInfo = getDisplayMerchantInfo(requestData, dccPaymentDetail);
        dccPageData.setDisplayField(displayMerchantInfo);
        dccPageData.setDccPaymentDetails(dccPaymentDetail);
        flowRequestBean.setDccPageData(dccPageData);
        nativeSessionUtil.setDccPageData(requestData.getTxnToken(), dccPageData);
    }

    private Map<String, Object> getDisplayMerchantInfo(PaymentRequestBean requestData, DccPaymentDetail dccPaymentDetail) {

        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(requestData.getMid());
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
            merchantDetails.put(MID, requestData.getMid());
            merchantDetails.put(ORDER_ID, requestData.getOrderId());
            merchantDetails
                    .put(AMOUNT,
                            AmountUtils.formatNumberToTwoDecimalPlaces(String.valueOf(Double.parseDouble(requestData
                                    .getTxnAmount())
                                    + Double.parseDouble(StringUtils.isNotEmpty(dccPaymentDetail
                                            .getConvenienceFeeInInr()) ? dccPaymentDetail.getConvenienceFeeInInr()
                                            : "0"))));// change after e2e
            boolean onUsMerchant = merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid());
            merchantDetails.put(ONUS, onUsMerchant);

            return merchantDetails;

        }

    }

    private Map<String, Object> getPayFormData(PaymentRequestBean requestData, String payMethod) {
        Map<String, Object> payFormData = new TreeMap<>();
        payFormData.put(ACTION_URL, getPayActionUrl(requestData.getMid(), requestData.getOrderId()));
        payFormData.put(METHOD, POST);
        payFormData.put(TYPE, PAY);
        Map<String, String> headers = new HashMap<>();
        headers.put(VERSION, Version_V1);
        headers.put(TXN_TOKEN, requestData.getTxnToken());
        headers.put(WORKFLOW, requestData.getWorkflow());
        payFormData.put(HEADERS, headers);
        Map<String, Object> body = new HashMap<>();
        body.put(PAYMENT_MODE, payMethod);
        body.put(MID, requestData.getMid());
        body.put(ORDER_ID, requestData.getOrderId());
        payFormData.put(BODY, body);
        return payFormData;

    }

    private FormDetail getCancelFormData(PaymentRequestBean requestData) {
        FormDetail cancelFormData = new FormDetail();
        cancelFormData.setMethod(POST);
        cancelFormData.setType(CANCEL);
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, CONTENT_VALUE);
        cancelFormData.setHeaders(headers);
        Map<String, String> content = new TreeMap<>();
        String workflow = requestData.getWorkflow() == null ? StringUtils.EMPTY : requestData.getWorkflow();
        switch (workflow) {
        case ENHANCED_CASHIER_FLOW:
            cancelFormData.setActionUrl(getCancelEnhanceActionURL());
            content.put(MID, requestData.getMid());
            content.put(ORDER_ID, requestData.getOrderId());
            content.put(TXN_TOKEN, requestData.getTxnToken());
            content.put(PAYMENT_CALL_DCC, Boolean.toString(BOOLEAN_TRUE));
            cancelFormData.setContent(content);
            break;
        case CHECKOUT:
            cancelFormData.setType(CHECKOUT_CANCEL);
            break;
        default:
            TransactionResponse txnResp = getTransactionResponseForCancelDcc(requestData.getTxnToken());
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
