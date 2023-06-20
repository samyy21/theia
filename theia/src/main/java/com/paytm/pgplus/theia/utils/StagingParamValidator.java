package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.StagingRequestException;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class StagingParamValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StagingParamValidator.class);
    private static final int CHECKSUM_LENGTH = 108;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    public boolean validate(HttpServletRequest request, MerchantPreferenceInfoResponse merchantPreferenceInfoResponse) {
        if (!("POST".equalsIgnoreCase(request.getMethod()) && isCustomPageEnabledForURL(request.getServerName()))) {
            return true;
        }
        List<StagingParamValidatorResponse> paramValidatorResponseList = new ArrayList<>();
        PaymentRequestBean requestBean = new PaymentRequestBean(request);
        boolean isMidValid = validateRequestMid(paramValidatorResponseList, requestBean.getMid());
        boolean isOrderIdValid = validateRequestOrderId(paramValidatorResponseList, requestBean.getOrderId());
        boolean isChannelIdValid = validateRequestChannelId(paramValidatorResponseList, requestBean.getChannelId());
        boolean isCustomerIdValid = validateRequestCustomerId(paramValidatorResponseList, requestBean.getCustId());
        boolean isTxnAmountValid = validateRequestTxnAmount(paramValidatorResponseList, requestBean.getTxnAmount());
        boolean isWebsiteValid = validateRequestWebsite(paramValidatorResponseList, requestBean);
        boolean isCallBackUrlValid = validateRequestCallBackUrl(paramValidatorResponseList, requestBean);
        boolean isIndustryTypeIdValid = validateRequestIndustryTypeId(paramValidatorResponseList,
                requestBean.getIndustryTypeId());
        boolean isChecksumValid = validateRequestChecksum(paramValidatorResponseList, requestBean.getMid(),
                requestBean.getChecksumhash(), isMidValid, merchantPreferenceInfoResponse);

        boolean isValidated = isMidValid && isOrderIdValid && isChannelIdValid && isCustomerIdValid && isTxnAmountValid
                && isWebsiteValid && isCallBackUrlValid && isIndustryTypeIdValid && isChecksumValid;
        LOGGER.info("isValidated {}", isValidated);
        if (!isValidated) {
            addToRequestAttribute(request, paramValidatorResponseList);
            throw new StagingRequestException("Invalid Parameter");
        }
        return isValidated;
    }

    private boolean validateRequestCallBackUrl(List<StagingParamValidatorResponse> paramValidatorResponseList,
            PaymentRequestBean paymentRequestBean) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        String callbackUrl = paymentRequestBean.getCallbackUrl();

        if (StringUtils.isNotBlank(callbackUrl) && !isValidUrl(callbackUrl)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.CALLBACKURL_INVALID;
            isValid = false;
        }
        if (StringUtils.isNotBlank(callbackUrl)) {
            paramValidatorResponseList.add(new StagingParamValidatorResponse(
                    TheiaConstant.StagingValidation.CALLBACK_URL, status, msg));
        }
        return isValid;
    }

    private boolean isValidUrl(String callbackUrl) {
        try {
            new URL(callbackUrl).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateRequestChecksum(List<StagingParamValidatorResponse> paramValidatorResponseList, String mid,
            String checksumhash, boolean isMidValid, MerchantPreferenceInfoResponse merchantPreferenceInfoResponse) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (!isMidValid) {
            return false;
        }
        MerchantPreferenceStore merchantPreferenceStore = null;
        if (ConfigurationUtil.isRedisOPtimizedFlow() || null != merchantPreferenceInfoResponse) {
            merchantPreferenceStore = merchantPreferenceProvider.parseResponse(merchantPreferenceInfoResponse);
        } else {
            merchantPreferenceStore = merchantPreferenceService.getMerchantPreferenceStore(mid);
        }
        boolean isChecksumEnabled = merchantPreferenceProvider.isChecksumEnabled(merchantPreferenceStore);
        if (isChecksumEnabled && StringUtils.isBlank(checksumhash)) {
            status = TheiaConstant.StagingValidation.MISSING;
            msg = TheiaConstant.StagingValidation.MISSING_MSG;
            isValid = false;
        } else if (StringUtils.isNotBlank(checksumhash) && !validateChecksumSyntax(checksumhash)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.CHECKSUM_INVALID;
            isValid = false;
        }
        paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.CHECKSUMHASH,
                status, msg));
        return isValid;
    }

    private static boolean validateChecksumSyntax(String checksumhash) {
        return (checksumhash.length() == CHECKSUM_LENGTH && !checksumhash.contains(" "));
    }

    private boolean validateRequestIndustryTypeId(List<StagingParamValidatorResponse> paramValidatorResponseList,
            String industryTypeId) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(industryTypeId) && !validateAlphaNumericCheck(industryTypeId)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.INVALID_MSG;
            isValid = false;
        }
        if (StringUtils.isNotBlank(industryTypeId)) {
            paramValidatorResponseList.add(new StagingParamValidatorResponse(
                    TheiaConstant.StagingValidation.INDUSTRY_TYPE_ID, status, msg));
        }
        return isValid;
    }

    private boolean validateRequestWebsite(List<StagingParamValidatorResponse> paramValidatorResponseList,
            PaymentRequestBean paymentRequestBean) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        String website = paymentRequestBean.getWebsite();
        String requestType = paymentRequestBean.getRequestType();
        if (!(ERequestType.SEAMLESS_3D_FORM.getType().equals(requestType)
                || ERequestType.SUBSCRIPTION_RENEWAL.getType().equals(requestType) || ERequestType.SUBSCRIPTION_PARTIAL_RENEWAL
                .getType().equals(requestType))) {
            if (StringUtils.isBlank(website)) {
                status = TheiaConstant.StagingValidation.MISSING;
                msg = TheiaConstant.StagingValidation.MISSING_MSG;
                isValid = false;
            } else if (!validateAlphaNumericCheck(website)) {
                status = TheiaConstant.StagingValidation.INVALID;
                msg = TheiaConstant.StagingValidation.INVALID_MSG;
                isValid = false;
            }
        }
        paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.WEBSITE,
                status, msg));
        return isValid;
    }

    private boolean validateRequestTxnAmount(List<StagingParamValidatorResponse> paramValidatorResponseList,
            String txnAmount) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isBlank(txnAmount)) {
            status = TheiaConstant.StagingValidation.MISSING;
            msg = TheiaConstant.StagingValidation.MISSING_MSG;
            isValid = false;
        } else if (!validateTxnAmountSyntax(txnAmount)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.AMOUNT_INVALID;
            isValid = false;
        }
        paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.TXN_AMOUNT,
                status, msg));
        return isValid;
    }

    private boolean validateTxnAmountSyntax(String txnAmount) {
        if (NumberUtils.isParsable(txnAmount)) {
            String[] amountSplit = txnAmount.split("\\.");
            if (amountSplit.length == 1) {
                return validateMantissa(amountSplit);
            } else if (amountSplit.length == 2) {
                return validateMantissa(amountSplit) && validateExponent(amountSplit);
            }
        }
        return false;
    }

    private boolean validateMantissa(String[] amountSplit) {
        int mantissa = Integer.parseInt(amountSplit[0]);
        if (mantissa < 0) {
            return false;
        }
        return true;
    }

    private boolean validateExponent(String[] amountSplit) {
        if (amountSplit[1].length() > 2) {
            return false;
        }
        return true;
    }

    private boolean validateRequestCustomerId(List<StagingParamValidatorResponse> paramValidatorResponseList,
            String custId) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(custId) && !validateCustOrderIdSyntax(custId)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.CUSTID_INVALID;
            isValid = false;
        }
        if (StringUtils.isNotBlank(custId)) {
            paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.CUSTID,
                    status, msg));
        }
        return isValid;
    }

    private boolean validateRequestChannelId(List<StagingParamValidatorResponse> paramValidatorResponseList,
            String channelId) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(channelId) && !EnumUtils.isValidEnum(EChannelId.class, channelId)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.CHANNELID_INVALID;
            isValid = false;
        }
        if (StringUtils.isNotBlank(channelId)) {
            paramValidatorResponseList.add(new StagingParamValidatorResponse(
                    TheiaConstant.StagingValidation.CHANNEL_ID, status, msg));
        }
        return isValid;
    }

    private boolean validateRequestOrderId(List<StagingParamValidatorResponse> paramValidatorResponseList,
            String orderId) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isBlank(orderId)) {
            status = TheiaConstant.StagingValidation.MISSING;
            msg = TheiaConstant.StagingValidation.MISSING_MSG;
            isValid = false;
        } else if (!validateCustOrderIdSyntax(orderId)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.ORDERID_INVALID;
            isValid = false;
        }
        paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.ORDER_ID,
                status, msg));
        return isValid;
    }

    private boolean validateRequestMid(List<StagingParamValidatorResponse> paramValidatorResponseList, String mid) {
        boolean isValid = true;
        String status = TheiaConstant.StagingValidation.CORRECT;
        String msg = StringUtils.EMPTY;
        if (StringUtils.isBlank(mid)) {
            status = TheiaConstant.StagingValidation.MISSING;
            msg = TheiaConstant.StagingValidation.MISSING_MSG;
            isValid = false;
        } else if (!validateAlphaNumericCheck(mid)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.MID_INCORRECT;
            isValid = false;
        } else if (!validateIfMidPresentAtPaytm(mid)) {
            status = TheiaConstant.StagingValidation.INVALID;
            msg = TheiaConstant.StagingValidation.MID_INVALID;
            isValid = false;
        }
        paramValidatorResponseList.add(new StagingParamValidatorResponse(TheiaConstant.StagingValidation.MID, status,
                msg));
        return isValid;
    }

    private boolean validateIfMidPresentAtPaytm(String mid) {
        try {
            String key = merchantExtendInfoUtils.getMerchantKey(mid);
            if (StringUtils.isBlank(key))
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean validateAlphaNumericCheck(String input) {
        return Pattern.matches("^[A-Za-z0-9]*$", input);
    }

    private boolean validateCustOrderIdSyntax(String id) {
        return Pattern.matches("^[a-zA-Z0-9-|_@.-]*$", id);
    }

    public boolean isCustomPageEnabledForURL(String envURL) {
        String validationEnabledURL = ConfigurationUtil.getProperty(TheiaConstant.StagingValidation.ENABLED_URL,
                StringUtils.EMPTY);

        Set<String> validationEnabledURLSet = new HashSet<>(Arrays.asList(StringUtils.split(validationEnabledURL, ";")));
        return validationEnabledURLSet.contains(envURL);
    }

    private void addToRequestAttribute(HttpServletRequest request,
            List<StagingParamValidatorResponse> paramValidatorResponseList) {
        request.setAttribute("paramValid", paramValidatorResponseList);
    }

    public boolean midOrderIDCheck(HttpServletRequest request) {
        if (request.getParameter(TheiaConstant.RequestParams.ENC_PARAMS) != null) {
            return true;
        }
        List<StagingParamValidatorResponse> paramValidatorResponseList = new ArrayList<>();
        String mid = request.getParameter(TheiaConstant.StagingValidation.MID);
        String orderId = request.getParameter(TheiaConstant.StagingValidation.ORDER_ID);
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        }
        if (StringUtils.isBlank(orderId)) {
            orderId = request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);
        }
        boolean isMidValid = validateRequestMid(paramValidatorResponseList, mid);
        boolean isOrderIdValid = validateRequestOrderId(paramValidatorResponseList, orderId);
        if (isMidValid && isOrderIdValid) {
            return true;
        }
        addToRequestAttribute(request, paramValidatorResponseList);
        return false;
    }
}