package com.paytm.pgplus.theia.nativ.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResp;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponseBody;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.FetchMerchantInfoException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("merchantInfoService")
public class MerchantInfoService implements IMerchantInfoService<MerchantInfoServiceRequest, MerchantInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantInfoService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantInfoService.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private MerchantDataServiceImpl merchantDataService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public MerchantInfoResponse fetchMerchantInfoResponse(MerchantInfoServiceRequest request)
            throws HttpCommunicationException, IllegalPayloadException, IOException {
        InitiateTransactionRequestBody orderDetails = request.getNativeInitiateRequest().getInitiateTxnReq().getBody();
        MerchantInfoResponseBody body = new MerchantInfoResponseBody();
        boolean promoCodeApplied = false;

        MerchantBussinessLogoInfo merchantBusinessLogoInfo = getMerchantInfo(request);
        Money txnAmount = orderDetails.getTxnAmount();
        if (StringUtils.isNotEmpty(orderDetails.getPromoCode()))
            promoCodeApplied = true;

        if (null != merchantBusinessLogoInfo
                && ResultCode.SUCCESS.getResultStatus()
                        .equals(merchantBusinessLogoInfo.getResponse().getResultStatus())) {
            convertToMerchantUserInfoResponse(merchantBusinessLogoInfo, txnAmount, promoCodeApplied, body);
        } else {
            throw FetchMerchantInfoException.getException();
        }

        return new MerchantInfoResponse(new ResponseHeader(), body);
    }

    public MerchantBussinessLogoInfo getMerchantInfo(MerchantInfoServiceRequest request) {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = null;

        String pickLogoFromNewLocation = ConfigurationUtil.getProperty("useNewImagePathForMerchant", "false");
        try {
            if ("true".equalsIgnoreCase(pickLogoFromNewLocation)) {
                merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMidV2(request.getMid(),
                        true);
            } else {
                merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMid(request.getMid());
            }
            LOGGER.info("Merchant Business Info from MS: {} ", merchantBussinessLogoInfo);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Error in MerchantUserInfoService getMerchantInfo" + e);
        }
        return merchantBussinessLogoInfo;
    }

    private void convertToMerchantUserInfoResponse(MerchantBussinessLogoInfo merchantBusinessLogoInfo, Money txnAmount,
            boolean promoCodeApplied, MerchantInfoResponseBody body) {
        MerchantInfoResp merchantInfoResp = getMerchantInfoResp(merchantBusinessLogoInfo);

        body.setMerchantInfoResp(merchantInfoResp);
        body.setTxnAmount(txnAmount);
        body.setPromoCodeApplied(String.valueOf(promoCodeApplied));
    }

    private MerchantInfoResp getMerchantInfoResp(MerchantBussinessLogoInfo merchantBusinessLogoInfo) {

        MerchantInfoResp merchantInfoResp = new MerchantInfoResp();

        merchantInfoResp.setMerBusName(merchantBusinessLogoInfo.getMerchantBusinessName());
        merchantInfoResp.setMerDispname(merchantBusinessLogoInfo.getMerchantDisplayName());
        merchantInfoResp.setMerLogoUrl(merchantBusinessLogoInfo.getMerchantImageName());

        return merchantInfoResp;
    }

    public void mapSsoTxnTokens(MerchantInfoServiceRequest serviceRequest, boolean callbackInResponse,
            MerchantInfoResponse merchantInfoResponse) {
        String paytmSsoToken = serviceRequest.getSsoToken();
        LOGGER.debug("SSOToken received from APP" + paytmSsoToken);
        InitiateTransactionRequestBody orderDetail = serviceRequest.getNativeInitiateRequest().getInitiateTxnReq()
                .getBody();
        if (merchantInfoResponse.getBody().isAppInvokeAllowed()) {
            orderDetail.setAppInvoke(true);
        }
        if (paytmSsoToken != null && !paytmSsoToken.isEmpty()) {
            LOGGER.debug("SSOToken received from initiateTxn API" + orderDetail.getPaytmSsoToken());
            orderDetail.setPaytmSsoToken(paytmSsoToken);

            if (callbackInResponse) {
                serviceRequest.setCallbackUrl(orderDetail.getCallbackUrl());
                if (orderDetail.isNeedAppIntentEndpoint()) {
                    // Deleting key, which was inserted at the time of
                    // initiateTransaction call.
                    String appInvokeCallbackURLKey = orderDetail.getOrderId().concat("_").concat(orderDetail.getMid());
                    nativeSessionUtil.deleteField(appInvokeCallbackURLKey,
                            TheiaConstant.ExtraConstants.APP_INVOKE_CALLBACK_URL);
                }
            } else {
                // Changing callback in orderDetails only for v1 api
                updateCallbackUrl(orderDetail);
                String appInvokeCallbackURLKey = orderDetail.getOrderId().concat("_").concat(orderDetail.getMid());
                nativeSessionUtil.setField(appInvokeCallbackURLKey,
                        TheiaConstant.ExtraConstants.APP_INVOKE_CALLBACK_URL, orderDetail.getCallbackUrl(), 900);
            }
            boolean isSaved = nativeSessionUtil.setOrderDetail(serviceRequest.getTxnToken(), orderDetail);
            LOGGER.info("SsoToken map with TxnToken for request fetchMerchantUserInfo", isSaved);
        }
    }

    private void updateCallbackUrl(InitiateTransactionRequestBody orderDetail) {
        // setting paytm standard callBack for AppInvoke
        String defaultCallbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
        if (StringUtils.isBlank(defaultCallbackUrl)) {
            LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
            throw FetchMerchantInfoException.getException();
        }
        defaultCallbackUrl = defaultCallbackUrl + orderDetail.getOrderId();
        LOGGER.info("Setting Callback url for App Invoke {} ", defaultCallbackUrl);
        orderDetail.setCallbackUrl(defaultCallbackUrl);
    }

    public Object getRedisObject(String redisKey) {
        return theiaTransactionalRedisUtil.get(redisKey);
    }
}