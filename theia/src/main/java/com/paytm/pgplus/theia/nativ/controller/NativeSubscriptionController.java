package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.TransactionStatus;
import com.paytm.pgplus.common.model.TxnStateLog;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.RenewSubscriptionRequest;
import com.paytm.pgplus.request.RenewSubscriptionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.response.RenewSubscriptionResponse;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.response.SubscriptionTransactionResponse;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.annotation.SignedResponseBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_SUBSCRIPTION_ON_AOAMID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.AOA_SUBSCRIPTION_PAYMODES;

@NativeControllerAdvice
@Controller
@RequestMapping("api/v1")
public class NativeSubscriptionController {

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier(value = "subscriptionRenewalService")
    private IJsonResponsePaymentService subscriptionRenewalService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSubscriptionController.class);

    @ApiOperation(value = "create subscription", notes = "To create subscription and start its transaction in native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/subscription/create", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public SubscriptionTransactionResponse createSubscriptionTransaction(
            @ApiParam(required = true) @RequestBody SubscriptionTransactionRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Subscription Native request received for API: /create/subscription is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());

            NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
            nativeInitiateRequest.setInitiateTxnReq(request);

            IRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse> requestProcessor = null;
            String paymodeEligibleForAOASubscriptionFlow = ff4jUtils.getPropertyAsStringWithDefault(
                    AOA_SUBSCRIPTION_PAYMODES, "BANK_MANDATE");

            Set<String> paymodeEligibleForAOASubscription = new HashSet<>(
                    Arrays.asList(paymodeEligibleForAOASubscriptionFlow.split(",")));
            if (aoaUtils.isAOAMerchant(request.getBody().getMid())
                    && paymodeEligibleForAOASubscription.contains(request.getBody().getSubscriptionPaymentMode())) {
                if (nativeSubscriptionHelper.getAOASubscriptionPaymodesConfigured().contains(
                        request.getBody().getSubscriptionPaymentMode())
                        || ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(), ENABLE_SUBSCRIPTION_ON_AOAMID,
                                false)) {
                    requestProcessor = requestProcessorFactory
                            .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_AOA_SUBSCRIPTION);
                } else {
                    requestProcessor = requestProcessorFactory
                            .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION);
                }

            } else {
                requestProcessor = requestProcessorFactory
                        .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION);
            }

            SubscriptionTransactionResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("create subscription", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "create subscription" + "to grafana", exception);
            }

            LOGGER.info("Subscription response returned for API: /initiateTransaction is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for CreateSubscriptionTransaction is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    @ApiOperation(value = "renewSubscription", notes = "To renew subscription in native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/subscription/renew", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public RenewSubscriptionResponse renewSubscription(
            @ApiParam(required = true) @RequestBody RenewSubscriptionRequest request) throws Exception {

        long startTime = System.currentTimeMillis();
        ThreadLocalUtil.set(new TxnStateLog(StringUtils.EMPTY, request.getBody().getMid(), request.getBody()
                .getOrderId(), request.getBody().getTxnAmount().getValue()));

        LOGGER.info("Subscription Native request received for API: /subscription/renew is: {}", request);
        nativePaymentUtil.logNativeRequests(request.getHead().toString());
        nativeValidationService.validateMidOrderId(request.getBody().getMid(), request.getBody().getOrderId());
        ResultInfo resultInfo;
        PaymentRequestBean paymentRequestData = setPaymentRequestBean(request, getHttpServletRequest(request));
        LOGGER.info("PaymentRequestBean received : {}", paymentRequestData);
        RenewSubscriptionResponse renewSubscriptionResponse = new RenewSubscriptionResponse();

        EventUtils.pushTheiaEvents(EventNameEnum.ORDER_INITIATED, new ImmutablePair<>("REQUEST_TYPE",
                paymentRequestData.getRequestType()));

        WorkFlowResponseBean workFlowResponseBean = subscriptionRenewalService
                .processPaymentRequest(paymentRequestData);

        if (workFlowResponseBean != null) {
            if (workFlowResponseBean.getSubscriptionRenewalResponse().getStatus()
                    .equals(TransactionStatus.TXN_FAILURE.getName())) {
                renewSubscriptionResponse.getBody().setTxnId(
                        workFlowResponseBean.getSubscriptionRenewalResponse().getTxnId());
                resultInfo = new ResultInfo("F", workFlowResponseBean.getSubscriptionRenewalResponse().getRespCode(),
                        workFlowResponseBean.getSubscriptionRenewalResponse().getRespMsg());
            } else {
                renewSubscriptionResponse.getBody().setTxnId(
                        workFlowResponseBean.getSubscriptionRenewalResponse().getTxnId());
                resultInfo = new ResultInfo("S", workFlowResponseBean.getSubscriptionRenewalResponse().getRespCode(),
                        workFlowResponseBean.getSubscriptionRenewalResponse().getRespMsg());
            }

        } else {
            resultInfo = generateFailureResultInfo();
        }
        renewSubscriptionResponse.getBody().setResultInfo(resultInfo);
        LOGGER.info("response for renew subscription : {}", renewSubscriptionResponse);
        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        renewSubscriptionResponse.setHead(responseHeader);
        nativePaymentUtil.logNativeResponse(renewSubscriptionResponse == null ? null : nativePaymentUtil
                .getResultInfo(renewSubscriptionResponse.getBody()));
        return renewSubscriptionResponse;
    }

    @ApiOperation(value = "renewSubscription", notes = "To renew subscription in native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/subscription/renew/internal", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public RenewSubscriptionResponse renewSubscriptionInternal(
            @ApiParam(required = true) @RequestBody RenewSubscriptionRequest request) throws Exception {

        return renewSubscription(request);
    }

    private PaymentRequestBean setPaymentRequestBean(
            @ApiParam(required = true) @RequestBody RenewSubscriptionRequest request,
            HttpServletRequest httpServletRequest) {
        PaymentRequestBean paymentRequestData = new PaymentRequestBean();
        paymentRequestData.setRequest(httpServletRequest);
        RenewSubscriptionRequestBody requestBody = request.getBody();
        paymentRequestData.setMid(requestBody.getMid());
        paymentRequestData.setOrderId(requestBody.getOrderId());
        paymentRequestData.setTxnAmount(requestBody.getTxnAmount().getValue());
        paymentRequestData.setSubscriptionID(requestBody.getSubscriptionId());
        paymentRequestData.setAdditionalInfo(requestBody.getAdditionalInfo());
        try {
            paymentRequestData.setAdditionalInfoMF(JsonMapper.mapObjectToJson(requestBody.getFeedFileInfo()));
        } catch (FacadeCheckedException e) {
            LOGGER.info("EXception while parsing feedInfo {}", e);
        }
        paymentRequestData.setRequestType(ERequestType.SUBSCRIPTION_RENEWAL.getType());
        if (StringUtils.isNotBlank(request.getBody().getRequestType())) {
            if (ERequestType.SUBS_RENEWAL_MF_SIP.getType().equalsIgnoreCase(request.getBody().getRequestType())) {
                paymentRequestData.setRequestType(ERequestType.SUBS_RENEWAL_MF_SIP.getType());
            }
        }
        paymentRequestData.setDebitDate(request.getBody().getDebitDate());
        paymentRequestData.setSplitSettlementInfoData(request.getBody().getSplitSettlementInfoData());
        return paymentRequestData;
    }

    private ResultInfo generateFailureResultInfo() {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResponseConstants.SYSTEM_ERROR.getCode());
        resultInfo.setResultMsg("Internal System error");
        resultInfo.setResultStatus("F");
        return resultInfo;
    }

    private boolean checkAPIBasedTransactionFailure(PaymentRequestBean paymentRequestData) {
        boolean isAPIDisabled = merchantPreferenceService.isAPIDisabled(paymentRequestData.getMid());
        if (!isAPIDisabled) {
            return false;
        }
        String requestType = paymentRequestData.getRequestType();
        boolean typeCheck = ERequestType.DEFAULT.getType().equalsIgnoreCase(requestType)
                || ERequestType.NATIVE.getType().equalsIgnoreCase(requestType);
        return typeCheck;
    }

    private HttpServletRequest getHttpServletRequest(RenewSubscriptionRequest request) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        httpServletRequest.setAttribute("MID", request.getBody().getMid());
        httpServletRequest.setAttribute("ORDER_ID", request.getBody().getOrderId());
        return httpServletRequest;
    }

}
