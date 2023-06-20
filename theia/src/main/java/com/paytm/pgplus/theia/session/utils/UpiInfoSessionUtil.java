package com.paytm.pgplus.theia.session.utils;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DeepLink;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.UPIHandle;
import com.paytm.pgplus.theia.nativ.model.enhancenative.*;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.UPIHandleUtil;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kartik
 * @Date 31-01-2017
 *
 */
@Component("upiInfoSessionUtil")
public class UpiInfoSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpiInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    UPIHandleUtil upiHandleUtil;

    public void generateAndSetRequiredUpiTransactionInfoInSession(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, WorkFlowResponseBean workFlowResponseBean) {

        UPITransactionInfo transactionInfo = new UPITransactionInfo();

        transactionInfo.setAcquirementId(workFlowResponseBean.getTransID());
        transactionInfo.setCashierRequestId(workFlowResponseBean.getCashierRequestId());
        transactionInfo.setPaytmMerchantId(requestData.getMid());
        transactionInfo.setAlipayMerchantId(requestData.getAlipayMerchantId());
        transactionInfo.setVpaID(workFlowRequestBean.getVirtualPaymentAddress());
        transactionInfo.setMerchantTransId(requestData.getOrderId());
        // for upi collect polling page
        upiHandleUtil.setUPIHandle(workFlowRequestBean, transactionInfo);

        String vpa = transactionInfo.getVpaID();
        transactionInfo.setPaytmVpa(UpiInfoSessionUtil.isPaytmVpa(workFlowRequestBean, vpa));
        transactionInfo.setTransactionAmount(UpiInfoSessionUtil.getTransactionAmount(workFlowResponseBean, requestData,
                workFlowRequestBean));
        transactionInfo.setBaseUrl(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.MERCHANT_STATUS_SERVICE_BASE_URL)
                + ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.MERCHANT_STATUS_SERVICE_API));
        transactionInfo.setStatusTimeOut(getStatusTimeout(workFlowRequestBean.getPaymentTimeoutInMinsForUpi()));
        transactionInfo.setStatusInterval(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL));
        transactionInfo.setUpiPollBaseUrl(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.UPI_POLL_BASE_URL));
        String bankForm = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        if (StringUtils.isNotBlank(bankForm)) {
            try {
                MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(bankForm, MerchantVpaTxnInfo.class);
                transactionInfo.setMerchantVpaTxnInfo(merchantVpaTxnInfo);
                if (merchantVpaTxnInfo.getVpa() != null) {
                    merchantVpaTxnInfo.setMaskedMerchantVpa(VPAHelper.setMaskedMerchantVpa(merchantVpaTxnInfo.getVpa(),
                            requestData.getMid()));
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception occured while converting Bank Form to MerchantVpaTxnInfo : Invalid Json : ", e);
            }
        }
        theiaSessionDataService.setUPITransactionInfoInSession(requestData.getRequest(), transactionInfo);

    }

    public static boolean isPaytmVpa(WorkFlowRequestBean workFlowRequestBean, String vpa) {
        if (StringUtils.isNotBlank(vpa)) {
            String[] vpaHandle = vpa.split("@");
            if (vpaHandle != null && vpaHandle.length == 2) {
                if ("paytm".equalsIgnoreCase(vpaHandle[1])
                        && ("WAP".equalsIgnoreCase(workFlowRequestBean.getChannelID()) || "APP"
                                .equalsIgnoreCase(workFlowRequestBean.getChannelID()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void generateAndSetRequiredTransactionInfoInSession(PaymentRequestBean requestBean,
            WorkFlowRequestBean workFlowRequestBean, WorkFlowResponseBean workFlowResponseBean) {

        TransactionInfo transInfo = theiaSessionDataService.getTxnInfoFromSession(requestBean.getRequest(), true);

        transInfo.setMid(requestBean.getMid());
        transInfo.setOrderId(requestBean.getOrderId());
        if (workFlowRequestBean.getChargeAmount() != null) {
            Float txnAmount = Float.valueOf(requestBean.getTxnAmount());
            Float chargeAmount = Float.valueOf(workFlowRequestBean.getChargeAmount());
            transInfo.setTxnAmount(String.valueOf(txnAmount + chargeAmount));
        } else {
            transInfo.setTxnAmount(requestBean.getTxnAmount());
        }
        transInfo.setTxnId(workFlowResponseBean.getTransID());
        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        if (StringUtils.isNotBlank(webFormContext)) {
            try {
                MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext,
                        MerchantVpaTxnInfo.class);
                if (StringUtils.isNotBlank(merchantVpaTxnInfo.getVpa())) {
                    transInfo.setVPA(merchantVpaTxnInfo.getVpa());
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception while fetching VPA from webformcontext:", e);
                transInfo.setVPA("paytm@icici");
            }
        }
    }

    public void generateAndSetRequiredMerchantInfoInSession(PaymentRequestBean requestBean) {
        MerchantInfo merchInfo = theiaSessionDataService.getMerchantInfoFromSession(requestBean.getRequest(), true);
        merchInfo.setMid(requestBean.getMid());
    }

    public void generateUPISessionData(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData, boolean processed,
            String paymentStatusVal) {
        if (!PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                || workFlowRequestBean.isUpiPushExpressSupported()) {
            return;
        }
        // TODO
        if (requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))) {
            return;
        }

        if (processed && !PaymentStatus.FAIL.toString().equals(paymentStatusVal)) {
            theiaSessionDataService.setUPIAccepted(requestData.getRequest(), true);
        } else {
            theiaSessionDataService.setUPIAccepted(requestData.getRequest(), false);
        }

        generateAndSetRequiredUpiTransactionInfoInSession(requestData, workFlowRequestBean, workFlowResponseBean);
        generateAndSetRequiredTransactionInfoInSession(requestData, workFlowRequestBean, workFlowResponseBean);
        generateAndSetRequiredMerchantInfoInSession(requestData);
    }

    public UPIPollResponse generateUPIPollResponse(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData, boolean processed,
            String paymentStatusVal, ResponseConstants responseConstant) {

        ResultInfo resultInfo = new ResultInfo();
        UPIPollResponseBody upiPollResponseBody = new UPIPollResponseBody();
        UPIContent content = new UPIContent();
        upiPollResponseBody.setResultInfo(resultInfo);
        UPIPollResponse upiPollResponse = new UPIPollResponse(upiPollResponseBody);
        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        if (processed && !PaymentStatus.FAIL.toString().equals(paymentStatusVal)
                && StringUtils.isNotBlank(webFormContext)) {
            content.setUpiAccepted(true);
            resultInfo.setResultCode(ResultCode.SUCCESS.getCode());
            resultInfo.setResultMsg(ResultCode.SUCCESS.getResultMsg());
            resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
        } else {
            content.setUpiAccepted(false);
            if (responseConstant != null && StringUtils.isNotBlank(responseConstant.getMessage())) {
                resultInfo.setResultCode(responseConstant.getCode());
                resultInfo.setResultMsg(responseConstant.getMessage());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            } else if (workFlowResponseBean.getQueryPaymentStatus() != null
                    && StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode())) {
                resultInfo.setResultMsg(workFlowResponseBean.getQueryPaymentStatus().getErrorMessage());
                resultInfo.setResultCode(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());

            } else {
                resultInfo.setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
                resultInfo.setResultCode(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            }
        }
        // Setting Appname for upi polling page
        upiHandleUtil.setUPIHandle(workFlowRequestBean, content);

        content.setOrderId(requestData.getOrderId());
        content.setMid(requestData.getMid());
        content.setTxnId(workFlowResponseBean.getTransID());
        content.setCashierRequestId(workFlowResponseBean.getCashierRequestId());
        content.setVpaID(workFlowRequestBean.getVirtualPaymentAddress());
        content.setSelfPush(UpiInfoSessionUtil.isPaytmVpa(workFlowRequestBean,
                workFlowRequestBean.getVirtualPaymentAddress()));
        content.setTimeInterval(ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL));
        content.setTimeOut(getStatusTimeout(workFlowRequestBean.getPaymentTimeoutInMinsForUpi()));
        content.setPaymentMode(PaymentTypeIdEnum.UPI.value);
        content.setTxnAmount(UpiInfoSessionUtil.getTransactionAmount(workFlowResponseBean, requestData,
                workFlowRequestBean));
        content.setMerchantVPA(UpiInfoSessionUtil.getMerchantVPA(workFlowResponseBean));
        content.setMerchantVpaTxnInfo(getMerchantVpaTxnInfo(workFlowResponseBean));

        upiPollResponseBody.setContent(content);
        content.setUpiStatusUrl(getBaseUrl() + "/upi/transactionStatus?MID=" + requestData.getMid() + "&ORDER_ID="
                + requestData.getOrderId());
        upiPollResponseBody.setContent(content);
        upiPollResponseBody.setCallbackUrl(getBaseUrl() + "/transactionStatus?MID=" + requestData.getMid()
                + "&ORDER_ID=" + requestData.getOrderId());

        return upiPollResponse;
    }

    public UPIPushResponse generateUPIPushResponse(BaseResponse response, WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData, boolean processed,
            String paymentStatusVal, ResponseConstants responseConstant) {
        ResultInfo resultInfo = new ResultInfo();
        UPIPushResponseBody upiPushResponseBody = new UPIPushResponseBody();
        UPIPushResponse upiPushResponse = new UPIPushResponse();
        BankFormData formData = (BankFormData) response;
        BankRedirectionDetail bankRedirectionDetail = formData.getBody();
        Map<String, String> content = bankRedirectionDetail.getContent();
        String callbackUrl = bankRedirectionDetail.getCallbackUrl();
        String method = bankRedirectionDetail.getMethod();
        if (paymentStatusVal.equals(PaymentStatus.FAIL.toString())) {
            if (responseConstant != null && StringUtils.isNotBlank(responseConstant.getMessage())) {
                resultInfo.setResultCode(responseConstant.getCode());
                resultInfo.setResultMsg(responseConstant.getMessage());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            } else if (workFlowResponseBean.getQueryPaymentStatus() != null
                    && StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode())) {
                resultInfo.setResultMsg(workFlowResponseBean.getQueryPaymentStatus().getErrorMessage());
                resultInfo.setResultCode(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());

            } else {
                resultInfo.setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
                resultInfo.setResultCode(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            }
            if (!requestData.isNativeRetryEnabled())
                resultInfo.setRedirect(true);
            else {
                resultInfo.setRedirect(false);
            }
        }
        if (workFlowResponseBean.isBankFormFetchFailed()) {
            resultInfo.setRedirect(false);
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            resultInfo.setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
        }
        upiPushResponseBody.setMethod(method);
        upiPushResponseBody.setCallbackUrl(callbackUrl);
        upiPushResponseBody.setResultInfo(resultInfo);
        upiPushResponseBody.setContent(content);
        upiPushResponse.setBody(upiPushResponseBody);
        return upiPushResponse;

    }

    public UPIIntentResponse generateUPIIntentResponse(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData, boolean processed,
            String paymentStatusVal, ResponseConstants responseConstant) {

        UPIIntentResponseBody upiIntentResponseBody = new UPIIntentResponseBody();
        ResultInfo resultInfo = upiIntentResponseBody.getResultInfo();
        UPIIntentResponse upiIntentResponse = new UPIIntentResponse();
        if (null != workFlowResponseBean.getQueryPaymentStatus()) {
            String bankFormJson = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
            if (StringUtils.isNotBlank(bankFormJson)) {
                resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
                BankForm bankForm = new Gson().fromJson(bankFormJson, BankForm.class);
                DeepLink deepLink = bankForm != null ? bankForm.getDeepLink() : null;
                LOGGER.info("Setting deepLinkResponse in UpiIntent for SUCCESS");
                setContentForUPIIntent(upiIntentResponseBody, requestData, workFlowResponseBean, deepLink);

            } else {
                if (responseConstant != null && StringUtils.isNotBlank(responseConstant.getMessage())) {
                    resultInfo.setResultCode(responseConstant.getCode());
                    resultInfo.setResultMsg(responseConstant.getMessage());
                    resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
                } else if (workFlowResponseBean.getQueryPaymentStatus() != null
                        && StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode())) {
                    resultInfo.setResultMsg(workFlowResponseBean.getQueryPaymentStatus().getErrorMessage());
                    resultInfo.setResultCode(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode());
                    resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());

                } else {
                    resultInfo.setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
                    resultInfo.setResultCode(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
                    resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
                }
                LOGGER.info("Setting deepLinkResponse in UpiIntent for FAILURE");
                setContentForUPIIntent(upiIntentResponseBody, requestData, workFlowResponseBean, null);
                if (requestData.isNativeRetryEnabled()) {
                    resultInfo.setRedirect(false);
                } else {
                    resultInfo.setRedirect(true);
                }
            }

        } else {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            setContentForUPIIntent(upiIntentResponseBody, requestData, workFlowResponseBean, null);
        }
        upiIntentResponseBody.setResultInfo(resultInfo);
        upiIntentResponse.setBody(upiIntentResponseBody);
        return upiIntentResponse;
    }

    private String getBaseUrl() {
        return ConfigurationUtil.getProperty("theia.base.path");
    }

    static public String getTransactionAmount(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        String txnAmt = null;
        if (StringUtils.isNotBlank(webFormContext)) {
            try {
                if (requestData.isNativeJsonRequest() || requestData.isEnhancedCashierPaymentRequest()) {
                    BankForm bankForm = getBankFormObject(workFlowResponseBean);
                    if (bankForm != null && bankForm.getRedirectForm() != null
                            && bankForm.getRedirectForm().getContent() != null) {
                        txnAmt = bankForm.getRedirectForm().getContent().get("txnAmount");
                    }
                } else {
                    MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext,
                            MerchantVpaTxnInfo.class);
                    txnAmt = merchantVpaTxnInfo.getTxnAmount();
                }
                if (StringUtils.isNotBlank(txnAmt)) {
                    return txnAmt;
                } else {
                    throw new FacadeCheckedException("txnAmt is blank in bankForm");
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception while fetching transaction Amount from webformcontext: ", e);
                String baseTxnAmount = getTxnAmountFromRequest(requestData);
                // Handle convenience fee for PAYTM_EXPRESS
                if (workFlowRequestBean.getChargeAmount() != null) {
                    Float baseTxnAmountFloat = Float.valueOf(baseTxnAmount);
                    Float chargeAmount = Float.valueOf(AmountUtils.getTransactionAmountInRupee(workFlowRequestBean
                            .getChargeAmount()));
                    return String.valueOf(baseTxnAmountFloat + chargeAmount);
                }
                return baseTxnAmount;
            }
        }
        return null;
    }

    private static String getTxnAmountFromRequest(PaymentRequestBean requestData) {
        String walletAmount = requestData.getWalletAmount();
        if (StringUtils.isNotBlank(walletAmount) && NumberUtils.isNumber(walletAmount)) {
            Float txnAmount = Float.valueOf(requestData.getTxnAmount());
            Float walletAmout = Float.valueOf(walletAmount);
            return String.valueOf(txnAmount - walletAmout);
        } else {
            return requestData.getTxnAmount();
        }
    }

    public EnhancedResponse generateEnhancedResponse(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData, boolean processed,
            String paymentStatusVal, ResponseConstants responseConstant) {

        ResultInfo resultInfo = new ResultInfo();
        EnhancedResponseBody enhancedResponseBody = new EnhancedResponseBody();
        enhancedResponseBody.setResultInfo(resultInfo);
        EnhancedResponse enhancedResponse = new EnhancedResponse(enhancedResponseBody);
        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        if (processed && !PaymentStatus.FAIL.toString().equals(paymentStatusVal)
                && StringUtils.isNotBlank(webFormContext)) {
            enhancedResponseBody.setUpiAccepted(true);
        } else {
            enhancedResponseBody.setUpiAccepted(false);
            if (responseConstant != null && StringUtils.isNotBlank(responseConstant.getMessage())) {
                resultInfo.setResultCode(responseConstant.getCode());
                resultInfo.setResultMsg(responseConstant.getMessage());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            } else if (workFlowResponseBean.getQueryPaymentStatus() != null
                    && StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode())) {
                resultInfo.setResultMsg(workFlowResponseBean.getQueryPaymentStatus().getErrorMessage());
                resultInfo.setResultCode(workFlowResponseBean.getQueryPaymentStatus().getPaytmResponseCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());

            } else {
                resultInfo.setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
                resultInfo.setResultCode(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
                resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            }
        }
        enhancedResponseBody.setOrderId(requestData.getOrderId());
        enhancedResponseBody.setMid(requestData.getMid());
        enhancedResponseBody.setTxnId(workFlowResponseBean.getTransID());
        enhancedResponseBody.setCashierRequestId(workFlowResponseBean.getCashierRequestId());
        enhancedResponseBody.setVpaID(workFlowRequestBean.getVirtualPaymentAddress());
        enhancedResponseBody.setSelfPush(UpiInfoSessionUtil.isPaytmVpa(workFlowRequestBean,
                workFlowRequestBean.getVirtualPaymentAddress()));
        enhancedResponseBody.setTimeInterval(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL));
        enhancedResponseBody.setTimeOut(getStatusTimeout(workFlowRequestBean.getPaymentTimeoutInMinsForUpi()));
        enhancedResponseBody.setPaymentMode(PaymentTypeIdEnum.UPI.value);
        enhancedResponseBody.setTxnAmount(UpiInfoSessionUtil.getTransactionAmount(workFlowResponseBean, requestData,
                workFlowRequestBean));
        enhancedResponseBody.setUpiStatusUrl(getBaseUrl() + "/upi/transactionStatus?MID=" + requestData.getMid()
                + "&ORDER_ID=" + requestData.getOrderId());
        enhancedResponseBody.setCallbackUrl(getBaseUrl() + "/transactionStatus?MID=" + requestData.getMid()
                + "&ORDER_ID=" + requestData.getOrderId());
        return enhancedResponse;
    }

    private static String getMerchantVPA(WorkFlowResponseBean workFlowResponseBean) {
        BankForm bankForm = getBankFormObject(workFlowResponseBean);
        // when insta doesn't provide any merchantVpa we set default vpa
        String defaultMerchantVpa = "paytm@icici";
        if (bankForm != null && bankForm.getRedirectForm() != null && bankForm.getRedirectForm().getContent() != null) {
            String merchantVpa = bankForm.getRedirectForm().getContent().get("MERCHANT_VPA");
            if (StringUtils.isBlank(merchantVpa))
                merchantVpa = defaultMerchantVpa;

            return merchantVpa;
        } else
            return null;
    }

    private static BankForm getBankFormObject(WorkFlowResponseBean workFlowResponseBean) {
        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        if (StringUtils.isNotBlank(webFormContext)) {
            return new Gson().fromJson(webFormContext, BankForm.class);
        } else
            return null;
    }

    private void setContentForUPIIntent(UPIIntentResponseBody upiIntentResponseBody, PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean, DeepLink deepLink) {
        String callBackUrl = getBaseUrl() + "/transactionStatus?MID=" + requestData.getMid() + "&ORDER_ID="
                + requestData.getOrderId();
        String upiStatusUrl = getBaseUrl() + "/upi/transactionStatus?MID=" + requestData.getMid() + "&ORDER_ID="
                + requestData.getOrderId();
        String deepLinkUrl = deepLink != null ? (StringUtils.isNotBlank(deepLink.getUrl()) ? deepLink.getUrl() : "")
                : "";
        String orderId = requestData.getOrderId();
        String merchantId = requestData.getMid();
        String transId = nativeSessionUtil.getTxnId(requestData.getTxnToken());
        String cashierRequestId = StringUtils.isNotBlank(workFlowResponseBean.getCashierRequestId()) ? workFlowResponseBean
                .getCashierRequestId() : "";
        String paymentMode = PaymentTypeIdEnum.UPI.value;
        String statusTimeout = ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_TIMEOUT);
        String statusInterval = ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL);
        Map<String, String> content = new LinkedHashMap<>();
        content.put(TheiaConstant.ResponseConstants.ORDERID, orderId);
        content.put(TheiaConstant.ResponseConstants.MERCHANT_ID, merchantId);
        content.put(TheiaConstant.ResponseConstants.PAY_MODE, paymentMode);
        content.put(TheiaConstant.ResponseConstants.DEEP_LINK, deepLinkUrl);
        content.put(TheiaConstant.ResponseConstants.CASHIER_REQUEST_ID, cashierRequestId);
        content.put(TheiaConstant.ResponseConstants.TRANS_ID, transId);
        content.put(TheiaConstant.ResponseConstants.UPI_STATUS_URL, upiStatusUrl);
        content.put(TheiaConstant.ResponseConstants.UPI_STATUS_TIMEOUT, statusTimeout);
        content.put(TheiaConstant.ResponseConstants.UPI_STATUS_INTERVAL, statusInterval);
        upiIntentResponseBody.setCallbackUrl(callBackUrl);
        upiIntentResponseBody.setContent(content);
    }

    private MerchantVpaTxnInfo getMerchantVpaTxnInfo(WorkFlowResponseBean workFlowResponseBean) {
        BankForm bankForm = getBankFormObject(workFlowResponseBean);

        MerchantVpaTxnInfo merchantVpaTxnInfo = null;

        if (bankForm != null && bankForm.getRedirectForm() != null && bankForm.getRedirectForm().getContent() != null) {

            try {
                String merchantVpaTxnInfoJson = new Gson().toJson(bankForm.getRedirectForm().getContent());
                merchantVpaTxnInfo = JsonMapper.mapJsonToObject(merchantVpaTxnInfoJson, MerchantVpaTxnInfo.class);
            } catch (Exception e) {
                LOGGER.error("Exception in converting stringJson to merchantVpaTxnInfo");
            }
        }
        return merchantVpaTxnInfo;
    }

    public String getPaymentTimeoutinMinsForUpi(String mid) {
        String orderExpiryTime = merchantPreferenceService.getOrderExpiryTimeForMerchant(mid);
        int timeout = TheiaConstant.UpiConfiguration.DEFAULT_UPI_COLLECT_PAYMENT_TIMEOUT;
        try {
            timeout = Math.min(Integer.parseInt(orderExpiryTime), timeout);
        } catch (Exception e) {
            LOGGER.info("Invalid order Expiry time recieved {},so setting UPI Payment Timeout as 30 mins",
                    orderExpiryTime);
        }
        return String.valueOf(timeout);
    }

    public String getStatusTimeout(String paymentTimeoutInMinsForUpi) {
        String defaultStatusTimeout = ConfigurationUtil.getProperty(
                TheiaConstant.UpiConfiguration.STATUS_QUERY_TIMEOUT, "48000");
        long statusTimeout;
        long upiTimeout;
        try {
            statusTimeout = Long.parseLong(defaultStatusTimeout);
        } catch (Exception ex) {
            LOGGER.error("Invalid value of defaultStatusTimeout : {} Returning the same", defaultStatusTimeout);
            return defaultStatusTimeout;
        }
        upiTimeout = statusTimeout;
        try {
            upiTimeout = Math.min(Long.parseLong(paymentTimeoutInMinsForUpi) * 60 * 1000, statusTimeout);
        } catch (Exception e) {
            LOGGER.info("Some error occured while computing status Timeout");
        }
        return String.valueOf(upiTimeout);
    }

}