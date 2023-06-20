package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageCacheData;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.INativeDirectBankPageService;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRANSACTION_RESPONSE_OBJECT;

@Service("nativeDirectBankPageCancelProcessor")
public class NativeDirectBankPageCancelProcessor
        extends
        AbstractRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse, NativeDirectBankPageServiceRequest, NativeJsonResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageCancelProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    @Qualifier("nativeDirectBankPageService")
    INativeDirectBankPageService nativeDirectBankPageService;

    @Autowired
    @Qualifier("merchantResponseService")
    MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Override
    protected NativeDirectBankPageServiceRequest preProcess(NativeDirectBankPageRequest request) {

        nativeDirectBankPageHelper.validateRequest(request);

        String txnToken = request.getHead().getTxnToken();

        InitiateTransactionRequestBody orderDetail = null;
        NativeDirectBankPageServiceRequest serviceRequest = null;

        try {

            orderDetail = nativeDirectBankPageHelper.getOrderDetail(request);
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());

            NativeDirectBankPageCacheData cachedBankFormData = nativeSessionUtil.getDirectBankPageRenderData(txnToken);

            serviceRequest = new NativeDirectBankPageServiceRequest();
            serviceRequest.setOrderDetail(orderDetail);
            serviceRequest.setCachedBankFormData(cachedBankFormData);

        } catch (SessionExpiredException see) {
            throwException(ResultCode.SESSION_EXPIRED_EXCEPTION, orderDetail);
        } catch (MidDoesnotMatchException mme) {
            throwException(ResultCode.MID_DOES_NOT_MATCH, orderDetail);
        } catch (OrderIdDoesnotMatchException orderIdException) {
            throwException(ResultCode.ORDER_ID_DOES_NOT_MATCH, orderDetail);
        }

        return serviceRequest;
    }

    @Override
    protected NativeJsonResponse onProcess(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception {

        try {

            NativeDirectBankPageServiceResponse serviceResponse = null;
            try {
                serviceResponse = nativeDirectBankPageService.callInstaProxyCancel(serviceRequest);
                serviceRequest.setInstaProxyResponse(serviceResponse.getDirectAPIResponse());
            } catch (Exception e) {
                LOGGER.error("Exception in communicating with Instaporoxy in cancel request {}",
                        ExceptionUtils.getStackTrace(e));
                serviceResponse = new NativeDirectBankPageServiceResponse(
                        nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                                NativeDirectBankPageRequestType.cancel));
            }

            nativeDirectBankPageHelper.setParamsForCashierResponseInHttpRequest(request, serviceRequest,
                    serviceResponse);

            Map<String, String> data = transactionStatusServiceImpl.getCashierResponse(request.getHttpServletRequest());
            TransactionResponse transactionResponse = JsonMapper.mapJsonToObject(data.get(TRANSACTION_RESPONSE_OBJECT),
                    TransactionResponse.class);
            serviceRequest.setTransactionResponse(transactionResponse);

        } catch (Exception e) {
            LOGGER.error("Exception in direct bank page cancel {}", ExceptionUtils.getStackTrace(e));
            throwException(ResultCode.FAILED, serviceRequest.getOrderDetail());
        }
        return new NativeJsonResponse();
    }

    @Override
    protected NativeJsonResponse postProcess(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest, NativeJsonResponse response) throws Exception {

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);

        Map<String, String> txnInfo = new TreeMap<>();
        merchantResponseService.makeReponseToMerchantEnhancedNative(serviceRequest.getTransactionResponse(), txnInfo);

        if (!StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(), serviceRequest.getTransactionResponse()
                .getTransactionStatus())) {
            // resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
            resultInfo.setResultMsg(serviceRequest.getTransactionResponse().getResponseMsg());
        }

        String callBackUrl = serviceRequest.getTransactionResponse().getCallbackUrl();

        if (isRetryAllowedInCallBackUrl(callBackUrl)) {
            resultInfo.setRetry(true);
        } else {
            resultInfo.setRetry(false);
        }
        resultInfo.setResultMsg(serviceRequest.getTransactionResponse().getResponseMsg());

        body.setResultInfo(resultInfo);
        body.setTxnInfo(txnInfo);
        body.setCallBackUrl(callBackUrl);
        body.setDeclineReason(serviceRequest.getTransactionResponse().getDeclineReason());

        response.setHead(new ResponseHeader());
        response.setBody(body);

        return response;
    }

    private void throwException(ResultCode resultCode, InitiateTransactionRequestBody orderDetail) {
        throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false).isNativeJsonRequest(true)
                .setOrderDetail(orderDetail).build();
    }

    private boolean isRetryAllowedInCallBackUrl(String callBackUrl) {
        return StringUtils.contains(callBackUrl, "retryAllowed=true");
    }
}
