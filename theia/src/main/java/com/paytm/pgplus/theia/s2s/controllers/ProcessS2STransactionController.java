package com.paytm.pgplus.theia.s2s.controllers;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.PaymentS2SResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.impl.MerchantPreferenceServiceImpl;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.s2s.enums.ResponseCode;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequest;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestBody;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestHeader;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SResponseUtil;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SUtil;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author kartik
 * @date 20-Dec-2017
 */
@RestController
@RequestMapping("/api/v1")
public class ProcessS2STransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessS2STransactionController.class);

    @Autowired
    @Qualifier("seamlessS2SPaymentServiceImpl")
    private IJsonResponsePaymentService seamlessS2SPaymentService;

    @Autowired
    private PaymentS2SResponseUtil responseUtil;

    @Autowired
    private MerchantPreferenceServiceImpl merchantPreferenceService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @RequestMapping(value = "/order/pay", method = { RequestMethod.POST })
    @ResponseBody
    public PaymentS2SResponse processPaymentRequest(HttpServletRequest request,
            @Valid @RequestBody PaymentS2SRequest paymentRequest) {
        long startTime = System.currentTimeMillis();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        PaymentS2SResponse paymentS2SResponse = null;
        try {
            LOGGER.info("S2S payment request data : {}", paymentRequest);
            PaymentS2SUtil.setRequestHeader(paymentRequest.getHeader());

            paymentRequestBean = preparePaymentRequestBean(paymentRequest, paymentRequestBean);
            // signature validation done
            paymentRequestBean.setChecksumhash(paymentRequest.getHeader().getSignature());
            paymentRequestBean.setRequest(request);
            paymentRequestBean.setSessionRequired(false);

            if (!RequestTypes.SEAMLESS_3D_FORM.equals(paymentRequestBean.getRequestType())) {
                return responseUtil.generateResponse(ResponseCode.INVALID_REQUEST_TYPE);
            }
            paymentRequestBean.setRequestType(ERequestType.SEAMLESS.getType());
            WorkFlowResponseBean workFlowResponseBean = seamlessS2SPaymentService
                    .processPaymentRequest(paymentRequestBean);
            LOGGER.debug("Response data : {}", workFlowResponseBean);
            paymentS2SResponse = workFlowResponseBean.getPaymentS2SResponse();
            LOGGER.info("Final Response generated: {}", paymentS2SResponse);
            return paymentS2SResponse;

        } finally {
            LOGGER.info("Total time taken for ProcessS2STransactionController is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public PaymentS2SResponse processValidationError(MethodArgumentNotValidException ex) {
        LOGGER.error("Exception occurred while payment request validation : ", ex);
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        // Get the first validation error and return in response
        FieldError fieldError = fieldErrors.get(0);
        String fieldName = fieldError.getField();
        ResponseCode validationErrorCode = getValidationErrorCode(fieldName);
        statsDUtils.pushException(ex.getClass().getSimpleName());
        return responseUtil.generateResponse(validationErrorCode);
    }

    @ExceptionHandler(PaymentRequestValidationException.class)
    @ResponseBody
    public PaymentS2SResponse processPaymentRequestValidationError(PaymentRequestValidationException ex) {
        LOGGER.error("Exception occurred while payment request validation : ", ex);
        ResponseCode validationErrorCode = ResponseCode.getResponseCodeByResponseConstant(ex.getResponseConstants());
        statsDUtils.pushException(ex.getClass().getSimpleName());
        return responseUtil.generateResponse(validationErrorCode);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public PaymentS2SResponse processInternalServerError(Exception e) {
        LOGGER.error("Exception occurred while processing payment request: ", e);
        statsDUtils.pushException(e.getClass().getSimpleName());
        return responseUtil.generateResponse(ResponseCode.INTERNAL_PROCESSING_ERROR);
    }

    private ResponseCode getValidationErrorCode(String propertyPath) {
        String[] path = propertyPath.split("\\.");
        String propertyName = path[path.length - 1];
        switch (propertyName) {
        case "header":
            return ResponseCode.INVALID_REQUEST_HEADER;
        case "body":
            return ResponseCode.INVALID_REQUEST_BODY;
        case "channelId":
            return ResponseCode.INVALID_CHANNEL;
        case "clientId":
            return ResponseCode.INVALID_CLIENT;
        case "signature":
            return ResponseCode.INVALID_CHECKSUM;
        case "version":
            return ResponseCode.INVALID_API_VERSION;
        case "requestType":
            return ResponseCode.INVALID_REQUEST_TYPE;
        case "txnAmount":
            return ResponseCode.INVALID_TXN_AMOUNT;
        case "orderId":
            return ResponseCode.INVALID_ORDER_ID;
        case "mid":
            return ResponseCode.INVALID_MID;
        case "custId":
            return ResponseCode.INVALID_CUST_ID;
        case "industryTypeId":
            return ResponseCode.INVALID_INDUSTRY_TYPE_ID;
        case "website":
            return ResponseCode.INVALID_WEBSITE;
        case "paymentTypeId":
            return ResponseCode.INVALID_PAYMENTMODE;
        default:
            return ResponseCode.INTERNAL_PROCESSING_ERROR;
        }
    }

    private PaymentRequestBean preparePaymentRequestBean(PaymentS2SRequest requestData,
            PaymentRequestBean paymentRequestBean) {

        PaymentS2SRequestHeader requestHeader = requestData.getHeader();
        PaymentS2SRequestBody requestBody = requestData.getBody();

        paymentRequestBean.setChannelId(requestHeader.getChannelId());
        // For Promo Checkout
        paymentRequestBean.setMerchantRequestedChannelId(requestHeader.getChannelId());
        paymentRequestBean.setChecksumhash(requestHeader.getSignature());

        paymentRequestBean.setRequestType(requestBody.getRequestType());
        paymentRequestBean.setMid(requestBody.getMid());
        paymentRequestBean.setOrderId(requestBody.getOrderId());
        paymentRequestBean.setTxnAmount(requestBody.getTxnAmount());
        paymentRequestBean.setCustId(requestBody.getCustId());
        paymentRequestBean.setPaymentTypeId(requestBody.getPaymentTypeId());
        paymentRequestBean.setIndustryTypeId(requestBody.getIndustryTypeId());
        paymentRequestBean.setWebsite(requestBody.getWebsite());
        paymentRequestBean.setAuthMode("3D");
        paymentRequestBean.setBankCode(getBankCode(requestBody));
        paymentRequestBean.setEmiChannelInfo(requestBody.getEmiChannelInfo());
        paymentRequestBean.setPaymentDetails(requestBody.getPaymentDetails());
        paymentRequestBean.setMobileNo(requestBody.getMobileNo());
        paymentRequestBean.setEmail(requestBody.getEmail());
        paymentRequestBean.setCallbackUrl(requestBody.getCallBackURL());
        paymentRequestBean.setFromAOARequest(requestBody.getFromAOARequest());

        if (StringUtils.isNotBlank(requestBody.getFromAOARequest())
                && requestBody.getFromAOARequest().equalsIgnoreCase("true")) {
            paymentRequestBean.setNativeJsonRequest(true);
        }

        validatePromoOfferRequest(requestBody);
        paymentRequestBean.setPaymentOffer(requestBody.getPaymentOffersApplied());

        if (EPayMethod.UPI_INTENT.getMethod().equals(requestBody.getPaymentTypeId())) {
            paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.UPI.value);
            paymentRequestBean.setRefUrl(requestBody.getRefUrl());
            paymentRequestBean.setTxnNote(requestBody.getTxnNote());
            paymentRequestBean.setOsType(requestBody.getOsType());
            paymentRequestBean.setPspApp(requestBody.getPspApp());
            paymentRequestBean.setPaymentDetails("dummyvpa@upi");
            paymentRequestBean.setVirtualPaymentAddr("dummyvpa@upi");
            paymentRequestBean.setDeepLinkRequired(true);
            paymentRequestBean.setDeepLinkFromInsta(true);
            paymentRequestBean.setAccountNumber(requestBody.getAccountNumber());
        }

        setUDFs(paymentRequestBean, requestBody);

        if (ff4jUtils.isCOFTEnabledOnAOA(requestBody.getMid())) {
            // to support coft transactions in pg leg from insta (token/tavv).
            if (requestBody.getCardTokenInfo() != null) {
                paymentRequestBean.setCoftTokenTxn(true);
                paymentRequestBean.setCardTokenInfo(requestBody.getCardTokenInfo());
                // this paymentDetails will be mapped to cvv2 in
                // workFlowRequestBean further in process.
                String paymentDetails = requestBody.getCardInfo().split(Pattern.quote("|"))[2];
                paymentRequestBean.setPaymentDetails(paymentDetails);
            }
        }
        return paymentRequestBean;
    }

    private String getBankCode(PaymentS2SRequestBody requestBody) {
        if ("KOTAK".equalsIgnoreCase(requestBody.getBankCode())) {
            return "NKMB";
        } else if ("ANDHRA".equalsIgnoreCase(requestBody.getBankCode())) {
            return "ANDB";
        } else
            return requestBody.getBankCode();
    }

    private void setUDFs(PaymentRequestBean paymentRequestBean, PaymentS2SRequestBody requestBody) {
        paymentRequestBean.setUdf1(requestBody.getUdf1());
        paymentRequestBean.setUdf2(requestBody.getUdf2());
        paymentRequestBean.setUdf2(requestBody.getUdf3());
        setMerchantUniqueReference(paymentRequestBean, requestBody);
        paymentRequestBean.setAdditionalInfo(requestBody.getAdditionalInfo());
    }

    private void setMerchantUniqueReference(PaymentRequestBean paymentRequestBean, PaymentS2SRequestBody requestBody) {
        try {
            String additionalInfo = requestBody.getAdditionalInfo();
            if (StringUtils.isNotEmpty(additionalInfo)) {
                Map<String, String> additionalInfoMap = JsonMapper.mapJsonToObject(additionalInfo.replace("\\", ""),
                        Map.class);
                if (additionalInfoMap != null && additionalInfoMap.containsKey("merchantUniqueReference")) {
                    paymentRequestBean.setMerchUniqueReference(additionalInfoMap.get("merchantUniqueReference"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting merchant unique reference from additional info");
        }
    }

    private void validatePromoOfferRequest(PaymentS2SRequestBody requestBody) {
        Long transAmount = null;
        Long promoAmount = null;
        if (requestBody.getPaymentOffersApplied() != null) {
            try {
                transAmount = requestBody.getTxnAmount() != null ? Long.valueOf(AmountUtils
                        .getTransactionAmountInPaise(requestBody.getTxnAmount())) : null;
                promoAmount = Long.valueOf(AmountUtils.getTransactionAmountInPaise(requestBody
                        .getPaymentOffersApplied().getTotalTransactionAmount()));
            } catch (Exception e) {
                LOGGER.error("invalid values for trans/promo amount :{},{}",
                        (requestBody.getTxnAmount() != null) ? requestBody.getTxnAmount() : null, requestBody
                                .getPaymentOffersApplied().getTotalTransactionAmount());
            }
            if (transAmount != null && promoAmount != null && promoAmount > transAmount) {
                throw RequestValidationException.getException(ResultCode.INVALID_PROMO_AMOUNT);
            }
        }
    }

}
