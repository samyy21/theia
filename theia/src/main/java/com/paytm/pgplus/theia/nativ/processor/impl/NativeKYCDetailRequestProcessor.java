package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.pgproxycommon.exception.KycValidationException;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequest;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailResponse;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.services.IUserKycService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_ERROR_MESSAGE;

@Service
public class NativeKYCDetailRequestProcessor
        extends
        AbstractRequestProcessor<NativeKYCDetailRequest, NativeKYCDetailResponse, NativeKYCDetailRequest, NativeKYCDetailResponse> {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("userKycServiceImpl")
    private IUserKycService userKycServiceImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeKYCDetailRequestProcessor.class);

    @Override
    protected NativeKYCDetailRequest preProcess(NativeKYCDetailRequest request) {
        return request;
    }

    @Override
    protected NativeKYCDetailResponse onProcess(NativeKYCDetailRequest request, NativeKYCDetailRequest serviceRequest)
            throws Exception {
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());

        NativeCashierInfoResponse nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request
                .getHead().getTxnToken());
        if (nativeCashierInfoResponse == null) {
            nativePaymentUtil.fetchPaymentOptions(request.getHead(), orderDetail, null);
            nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
        }

        // Validate And Submit KYC
        NativeKYCDetailResponse nativeKYCDetailResponse = submitKYCRequest(request);

        if (nativeKYCDetailResponse.getBody() != null && nativeKYCDetailResponse.getBody().isKycSuccessful()) {
            // Update isOnTheFlyKYCRequired in NativeCashierInfoResponse in
            // redis
            LOGGER.info("KYC in Native is success.");
            nativeCashierInfoResponse.getBody().setOnTheFlyKYCRequired(false);
            nativeSessionUtil.setCashierInfoResponse(request.getHead().getTxnToken(), nativeCashierInfoResponse);
        } else {
            LOGGER.info("KYC in Native failed.");
        }

        return nativeKYCDetailResponse;
    }

    private NativeKYCDetailResponse submitKYCRequest(NativeKYCDetailRequest request) {
        NativeKYCDetailResponseBody body = new NativeKYCDetailResponseBody();
        UserDetailsBiz userDetailsBiz = null;
        Integer currentKYCRetryCount = 0;
        String userKycKey;
        try {
            userDetailsBiz = nativeSessionUtil.getUserDetails(request.getHead().getTxnToken());
            userKycKey = "KYC_" + userDetailsBiz.getUserId();
            boolean hasCustomerDoneKYCRecently = nativeSessionUtil.getKey(userKycKey) != null ? true : false;
            if (hasCustomerDoneKYCRecently) {
                LOGGER.info("User did KYC recently. Hence skipping KYC validation. ");
            } else {
                currentKYCRetryCount = checkForKYCRetry(request.getHead().getTxnToken());
                body.setKycSuccessful(false);
                body.setKycRetryCount(currentKYCRetryCount);
                body.setResultInfo(NativePaymentUtil.resultInfoForFailure());
                if (currentKYCRetryCount < 0) {
                    String kycErrMsg = "Retry limit reached for KYC.";
                    LOGGER.info(kycErrMsg);
                    body.setKycErrorMsg(kycErrMsg);
                    body.getResultInfo().setResultMsg(kycErrMsg);
                    return new NativeKYCDetailResponse(new ResponseHeader(), body);
                }
                LOGGER.info("Setting current kyc retry count as  : {}", currentKYCRetryCount);
                nativeSessionUtil.setKYCRetryCount(request.getHead().getTxnToken(), currentKYCRetryCount);

                // Proceed for KYC
                userKycServiceImpl.doKYC(request, userDetailsBiz.getUserId());

                // When KYC success then Add redis key for recently updated KYC
                // key
                long timeOut = (long) 30 * 60;
                nativeSessionUtil.setKey(userKycKey, true, timeOut);
            }
        } catch (KycValidationException e) {
            // LOGGER.info("User KYC failed KycValidationException: " + e);
            LOGGER.info("User KYC failed KycValidationException: " + ExceptionLogUtils.limitLengthOfStackTrace(e));
            String kycErrMsg = ConfigurationUtil.getProperty(KYC_ERROR_MESSAGE,
                    "We could not validate your ID. Please try again with a different document ID");
            if (e != null && StringUtils.isNotBlank(e.getReason())) {
                kycErrMsg = e.getReason();
            }
            body.setKycErrorMsg(kycErrMsg);
            if (body.getResultInfo() != null) {
                body.getResultInfo().setResultMsg(kycErrMsg);
            }
            return new NativeKYCDetailResponse(new ResponseHeader(), body);
        } catch (Exception e) {
            LOGGER.info("User KYC failed " + e);
            String kycErrMsg = ConfigurationUtil.getProperty(KYC_ERROR_MESSAGE,
                    "We could not validate your ID. Please try again with a different document ID");
            if (body.getResultInfo() != null) {
                body.getResultInfo().setResultMsg(kycErrMsg);
            }
            body.setKycErrorMsg(kycErrMsg);
            return new NativeKYCDetailResponse(new ResponseHeader(), body);
        }

        // KYC successful
        body.setKycSuccessful(true);
        body.setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        body.setKycRetryCount(currentKYCRetryCount);

        return new NativeKYCDetailResponse(new ResponseHeader(), body);
    }

    @Override
    protected NativeKYCDetailResponse postProcess(NativeKYCDetailRequest request,
            NativeKYCDetailRequest serviceRequest, NativeKYCDetailResponse serviceResponse) {
        return serviceResponse;
    }

    private Integer checkForKYCRetry(String txnToken) {
        Integer currentKYCRetryCount = nativeSessionUtil.getKYCRetryCount(txnToken);
        // first kyc validation attempt
        if (currentKYCRetryCount == null) {
            currentKYCRetryCount = 2;
        } else {
            currentKYCRetryCount = currentKYCRetryCount - 1;
        }
        return currentKYCRetryCount;
    }
}