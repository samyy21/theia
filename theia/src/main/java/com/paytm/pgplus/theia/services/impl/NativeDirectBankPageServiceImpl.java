package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.facade.payment.services.IBankProxyService;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceResponse;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.INativeDirectBankPageService;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static com.paytm.pgplus.cashier.constant.CashierConstant.ACCEPTED_SUCCESS_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service("nativeDirectBankPageService")
public class NativeDirectBankPageServiceImpl implements INativeDirectBankPageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageServiceImpl.class);

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    @Qualifier("bankProxyServiceImpl")
    private IBankProxyService bankProxyServiceImpl;

    @Override
    public NativeDirectBankPageServiceResponse callInstaProxyResendOtp(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception {

        FormDetail formDetail = nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                NativeDirectBankPageRequestType.resend);

        HttpRequestPayload<String> payload = nativeDirectBankPageHelper.createResendOtpRequest(formDetail, request);

        DirectAPIResponse directAPIResponse = bankProxyServiceImpl.initiateResendOtpRequest(payload);
        validateResponse(directAPIResponse);

        return new NativeDirectBankPageServiceResponse(formDetail, directAPIResponse);
    }

    @Override
    public NativeDirectBankPageServiceResponse callInstaProxySubmit(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception {

        FormDetail formDetail = nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                NativeDirectBankPageRequestType.submit);

        HttpRequestPayload<String> payload = nativeDirectBankPageHelper.createSubmitRequest(formDetail, request,
                serviceRequest);
        DirectAPIResponse directAPIResponse = bankProxyServiceImpl.initiateSubmitRequest(payload);
        validateResponse(directAPIResponse);
        validateResponseIfAPIRequestOriginIsPG(request, directAPIResponse);

        return new NativeDirectBankPageServiceResponse(formDetail, directAPIResponse);
    }

    @Override
    public NativeDirectBankPageServiceResponse callInstaProxyCancel(NativeDirectBankPageServiceRequest serviceRequest)
            throws Exception {

        FormDetail formDetail = nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                NativeDirectBankPageRequestType.cancel);

        HttpRequestPayload<String> payload = nativeDirectBankPageHelper.createCancelRequest(formDetail);

        DirectAPIResponse directAPIResponse = bankProxyServiceImpl.initiateCancelRequest(payload);
        validateResponse(directAPIResponse);

        return new NativeDirectBankPageServiceResponse(formDetail, directAPIResponse);
    }

    private void validateResponse(DirectAPIResponse directAPIResponse) {
        if (directAPIResponse == null || directAPIResponse.getResultInfo() == null) {
            LOGGER.error("Response got from InstaProxy for DirectBankPage is null");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
        }
        if (!StringUtils.equals(ACCEPTED_SUCCESS_CODE, directAPIResponse.getResultInfo().getResultStatus())) {
            LOGGER.error("ResultStatus is not ACCEPTED_SUCCESS");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
        }
    }

    private void validateResponseIfAPIRequestOriginIsPG(NativeDirectBankPageRequest request,
            DirectAPIResponse directAPIResponse) {

        if (nativeDirectBankPageHelper.isApiRequestOriginPG(request)) {
            if (CollectionUtils.isEmpty(directAPIResponse.getExtendInfo())) {
                LOGGER.error("ExtendInfo received from instaProxy for directBankPage(pg owned) is empty");
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
            }

            Map<String, String> extendInfo = directAPIResponse.getExtendInfo();

            for (String extendInfoValue : extendInfo.values()) {
                if (StringUtils.isBlank(extendInfoValue)) {
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
                }
            }

            validateExtendedInfoKeys(extendInfo);

        }
    }

    private void validateExtendedInfoKeys(Map<String, String> extendInfo) {

        /*
         * Mandatory parameters to be sent from instaproxy side
         */

        if (extendInfo.get(CASHIER_REQUEST_ID) == null || extendInfo.get(TRANS_ID) == null
                || extendInfo.get(MERCHANT_ID) == null || extendInfo.get(PAYMENT_MODE) == null) {

            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false).build();
        }
    }
}
