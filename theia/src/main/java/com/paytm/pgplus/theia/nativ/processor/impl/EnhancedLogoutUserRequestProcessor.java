package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.facade.user.models.response.ValidateSignInOtpResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.model.auth.EnhancedLogoutResponseBody;
import com.paytm.pgplus.theia.nativ.model.auth.EnhancedNativeUserLogoutRequest;
import com.paytm.pgplus.theia.nativ.model.auth.EnhancedNativeUserLogoutResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPageDynamicQR;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.MerchantRedirectRequestException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.impl.EnhancedCashierPageServiceImpl;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service("enhancedLogoutUserRequestProcessor")
public class EnhancedLogoutUserRequestProcessor
        extends
        AbstractRequestProcessor<EnhancedNativeUserLogoutRequest, EnhancedNativeUserLogoutResponse, EnhancedNativeUserLogoutResponse, EnhancedNativeUserLogoutResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedLogoutUserRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("enhancedCashierPageService")
    EnhancedCashierPageServiceImpl enhancedCashierPageService;

    @Override
    protected EnhancedNativeUserLogoutResponse preProcess(EnhancedNativeUserLogoutRequest request) {
        // Creating blank response
        EnhancedNativeUserLogoutResponse response = getEnhanceLogoutResponse();
        return response;
    }

    @Override
    protected EnhancedNativeUserLogoutResponse onProcess(EnhancedNativeUserLogoutRequest request,
            EnhancedNativeUserLogoutResponse response) throws Exception {

        InitiateTransactionRequestBody orderDetail = new InitiateTransactionRequestBody();
        try {
            // We have to delete cookie everytime we receive request for logout
            nativeSessionUtil.deleteLoginCookie();
            orderDetail = validate(request);
            populateResponseData(orderDetail, response, request.getHead().getTxnToken());
        } catch (BaseException e) {
            LOGGER.error("Exception occured while logging out {} ", e);
            e.getResultInfo().setRedirect(true);
            throw e;
        }
        response.getBody().getResultInfo().setResultStatus("S");
        return response;
    }

    @Override
    protected EnhancedNativeUserLogoutResponse postProcess(EnhancedNativeUserLogoutRequest request,
            EnhancedNativeUserLogoutResponse enhancedNativeUserLogoutResponse, EnhancedNativeUserLogoutResponse response)
            throws Exception {
        return response;
    }

    private InitiateTransactionRequestBody validate(EnhancedNativeUserLogoutRequest request) {
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        return orderDetail;
    }

    private void populateResponseData(InitiateTransactionRequestBody orderDetail,
            EnhancedNativeUserLogoutResponse response, String txnToken) throws Exception {

        LOGGER.info("Populating app Data from cache for enhanced logout flow");

        String key = enhancedCashierPageServiceHelper.fetchRedisKey(orderDetail.getMid(), orderDetail.getOrderId());

        EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                .getKey(key);

        if (null == enhanceCashierPageCachePayload) {
            LOGGER.error("Unable to get enhanceCashierPageCachePayload data from cache");
            throw new SessionExpiredException();
        }
        EnhancedCashierPageDynamicQR qr = enhanceCashierPageCachePayload.getEnhancedCashierPage().getQr();
        PaymentRequestBean paymentRequestBean = enhanceCashierPageCachePayload.getMerchantRequestData();
        paymentRequestBean.setDynamicQrRequired(qr == null);
        InitiateTransactionResponse initiateTransactionResponse = enhanceCashierPageCachePayload
                .getInitiateTransactionResponse();

        paymentRequestBean.setPaytmToken(null);
        paymentRequestBean.setSsoToken(null);
        orderDetail.setPaytmSsoToken(null);
        initiateTransactionResponse.getBody().setAuthenticated(false);

        nativeSessionUtil.setOrderDetail(txnToken, orderDetail);

        EnhancedCashierPage enhancedCashierPage = enhancedCashierPageService.getEnhancedCashierPage(paymentRequestBean,
                initiateTransactionResponse);
        if (qr != null) {
            enhancedCashierPage.setQr(qr);
        }
        enhancedCashierPageService.setEnhancedCashierPageInCache(paymentRequestBean, initiateTransactionResponse,
                enhancedCashierPage);

        LOGGER.info("App Data successfully populated from cache for enhanced validate otp flow :",
                JsonMapper.mapObjectToJson(enhancedCashierPage));

        response.getBody().setEnhancedCashierPage(enhancedCashierPage);
    }

    private EnhancedNativeUserLogoutResponse getEnhanceLogoutResponse() {
        EnhancedNativeUserLogoutResponse response = new EnhancedNativeUserLogoutResponse();
        EnhancedLogoutResponseBody responseBody = new EnhancedLogoutResponseBody();
        response.setBody(responseBody);
        response.setHead(new ResponseHeader());
        return response;
    }

}
