package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.FetchMerchantInfoRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IMerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_THEIA_APP_INVOKE_AS_COLLECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2;

@Service
public class FetchMerchantInfoRequestProcessor
        extends
        AbstractRequestProcessor<FetchMerchantInfoRequest, MerchantInfoResponse, MerchantInfoServiceRequest, MerchantInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMerchantInfoRequestProcessor.class);

    @Autowired
    @Qualifier("merchantInfoService")
    private IMerchantInfoService<MerchantInfoServiceRequest, MerchantInfoResponse> merchantInfoService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    ProcessTransactionUtil processTransactionUtil;

    @Override
    protected MerchantInfoServiceRequest preProcess(FetchMerchantInfoRequest request) {
        MerchantInfoServiceRequest wrapper = null;
        NativeInitiateRequest nativeInitiateRequest = validate(request);
        wrapper = getMerchantInfoServiceRequest(request, nativeInitiateRequest);

        return wrapper;
    }

    @Override
    protected MerchantInfoResponse onProcess(FetchMerchantInfoRequest request, MerchantInfoServiceRequest serviceRequest)
            throws IOException, IllegalPayloadException, HttpCommunicationException {
        boolean callbackInResponse = StringUtils.equals(Version_V2, request.getHead().getVersion());

        MerchantInfoResponse merchantInfoResponse = merchantInfoService.fetchMerchantInfoResponse(serviceRequest);
        updateAppInvokePreference(serviceRequest, merchantInfoResponse);

        merchantInfoService.mapSsoTxnTokens(serviceRequest, callbackInResponse, merchantInfoResponse);
        if (callbackInResponse) {
            addCallbackInResponse(merchantInfoResponse, serviceRequest);
        }
        return merchantInfoResponse;
    }

    private void addCallbackInResponse(MerchantInfoResponse merchantInfoResponse,
            MerchantInfoServiceRequest serviceRequest) {
        LOGGER.info("Adding callback in fetchMerchantInfo response");
        merchantInfoResponse.getBody().setCallbackUrl(serviceRequest.getCallbackUrl());
    }

    @Override
    protected MerchantInfoResponse postProcess(FetchMerchantInfoRequest request,
            MerchantInfoServiceRequest serviceRequest, MerchantInfoResponse serviceResponse) {
        return serviceResponse;
    }

    private void updateAppInvokePreference(MerchantInfoServiceRequest serviceRequest,
            MerchantInfoResponse merchantInfoResponse) {
        /*
         * By default making this preference as true unless made false.
         */
        merchantInfoResponse.getBody().setAppInvokeAllowed(
                merchantPreferenceService.isAppInvokeAllowed(serviceRequest.getMid(), true));
        if (!serviceRequest.isAppSSOMatchWithOrderDetailSSO()) {
            merchantInfoResponse.getBody().setAppInvokeAllowed(false);
        }
    }

    private NativeInitiateRequest validate(FetchMerchantInfoRequest request) {
        validateRequestParam(request);
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request.getHead().getTxnToken());
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        validateMidOrderId(orderDetail, request);
        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType())
                || ERequestType.SUBSCRIBE.getType().equals(orderDetail.getRequestType())
                || ERequestType.NATIVE_MF_SIP.getType().equals(orderDetail.getRequestType())) {
            nativeValidationService.validateTxnAmountForSubscription(orderDetail.getTxnAmount().getValue());
        } else {
            nativeValidationService.validateTxnAmount(orderDetail.getTxnAmount().getValue());
        }
        return nativeInitiateRequest;
    }

    private void validateMidOrderId(InitiateTransactionRequestBody orderDetail, FetchMerchantInfoRequest request) {
        String requestMid = request.getBody().getMid();
        String requestOrderId = request.getBody().getOrderId();
        if (!requestMid.equals(orderDetail.getMid())) {
            throw MidDoesnotMatchException.getException();
        } else if (!requestOrderId.equals(orderDetail.getOrderId())) {
            throw OrderIdDoesnotMatchException.getException();
        }
    }

    private void validateRequestParam(FetchMerchantInfoRequest request) {
        if (request.getHead() == null || request.getBody() == null || StringUtils.isBlank(request.getBody().getMid())
                || StringUtils.isBlank(request.getBody().getOrderId())
                || StringUtils.isBlank(request.getHead().getSsoToken())
                || StringUtils.isBlank(request.getHead().getTxnToken())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private MerchantInfoServiceRequest getMerchantInfoServiceRequest(FetchMerchantInfoRequest request,
            NativeInitiateRequest nativeInitiateRequest) {
        boolean ssoTokenMatched = true;
        NativePersistData nativePersistData = nativeInitiateRequest.getNativePersistData();
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        UserDetailsBiz appsUserDetailsBiz = nativeValidationService.validateSSOToken(request.getHead().getSsoToken(),
                request.getBody().getMid());

        String initiateUserId = (String) nativeSessionUtil.getField(request.getHead().getTxnToken(),
                "sendNotificationAppInvokeUserId");

        if (initiateUserId != null
                && appsUserDetailsBiz != null
                && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(FEATURE_THEIA_APP_INVOKE_AS_COLLECT,
                        orderDetail.getMid(), orderDetail.getUserInfo().getCustId(), initiateUserId)
                && !(StringUtils.equalsIgnoreCase(initiateUserId, appsUserDetailsBiz.getUserId()))) {
            LOGGER.error("Payment sso token and app sso token are not matching");
            ssoTokenMatched = false;
        }
        processTransactionUtil.pushNativePaymentEvent(orderDetail.getMid(), orderDetail.getOrderId(),
                "APP_INVOKE_".concat(String.valueOf(ssoTokenMatched)));
        MerchantInfoServiceRequest wrapper = new MerchantInfoServiceRequest(request.getBody().getMid(), request
                .getBody().getOrderId(), request.getHead().getTxnToken(), request.getHead().getSsoToken(),
                nativeInitiateRequest, ssoTokenMatched);
        return wrapper;
    }
}
