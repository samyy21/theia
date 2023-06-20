package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequest;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.BANK_TRANSFER;

@Service("bankTransferHelper")
public class BankTransferHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransferHelper.class);

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier(value = "bankTransferService")
    private IJsonResponsePaymentService bankTransferService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    public NativeJsonResponse processNativeJsonRequest(HttpServletRequest httpServletRequest) {

        LOGGER.info("Processing BankTransfer Request");

        PaymentRequestBean paymentRequestData = getPaymentRequestBean(httpServletRequest);
        WorkFlowResponseBean workFlowResponseBean = bankTransferService.processPaymentRequest(paymentRequestData);

        NativeJsonResponse nativeJsonResponse = theiaResponseGenerator.getNativeJsonResponse(workFlowResponseBean,
                paymentRequestData, getWorkFlowRequestBean(paymentRequestData));

        handleResponse(nativeJsonResponse);

        LOGGER.info("Final Response sent in NativeJsonResponse : {}", nativeJsonResponse);
        return nativeJsonResponse;
    }

    private void handleResponse(NativeJsonResponse response) {

        if (null == response.getBody().getTxnInfo() || null == response.getBody().getResultInfo()) {
            return;
        }
        NativeJsonResponseBody body = response.getBody();
        LOGGER.info("Updating ResultInfo after successful API call");
        body.getResultInfo().setResultStatus(ResultCode.SUCCESS.getResultStatus());
        body.getResultInfo().setResultCode(ResultCode.SUCCESS.getResultCodeId());
    }

    public WorkFlowRequestBean getWorkFlowRequestBean(PaymentRequestBean requestData) {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
            InternalTransactionRequest request = (InternalTransactionRequest) requestData.getBankTransferDTO().get(
                    BANK_TRANSFER);
            workFlowRequestBean.getChannelInfo().put("tpvDescription", request.getBody().getTpvDescription());
            workFlowRequestBean.getChannelInfo().put("tpvErrorCode", request.getBody().getTpvErrorCode());
            workFlowRequestBean.getChannelInfo().put("isTPVFailure", String.valueOf(request.getBody().isTPVFailure()));
            workFlowRequestBean.getChannelInfo().put("errorCode", request.getBody().getErrorCode());
            workFlowRequestBean.getChannelInfo().put("failure", String.valueOf(request.getBody().isFailure()));
            workFlowRequestBean.getChannelInfo().put("errorDescription", request.getBody().getErrorDescription());
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : {}", e);
            throwExceptionForNativeJsonRequest(e.getResponseConstant(), requestData);
        }
        return workFlowRequestBean;
    }

    private void throwExceptionForNativeJsonRequest(ResponseConstants rc, PaymentRequestBean requestBean) {
        throw new NativeFlowException.ExceptionBuilder(rc).isHTMLResponse(false).isNativeJsonRequest(true)
                .isRetryAllowed(requestBean.isNativeRetryEnabled()).build();
    }

    private PaymentRequestBean getPaymentRequestBean(HttpServletRequest httpServletRequest) {

        String requestData = null;
        InternalTransactionRequest request = null;

        try {
            requestData = IOUtils.toString(httpServletRequest.getInputStream(), Charsets.UTF_8.name());
            request = JsonMapper.mapJsonToObject(requestData, InternalTransactionRequest.class);
        } catch (IOException | FacadeCheckedException e) {
            LOGGER.error("Something went wrong {}", e);
        }

        LOGGER.info("InternalTransactionRequest is: {}", request);

        final SecureRequestHeader head = request.getHead();
        final InternalTransactionRequestBody body = request.getBody();

        PaymentRequestBean requestBean = new PaymentRequestBean(httpServletRequest);
        requestBean.setSessionRequired(false);
        requestBean.setRequest(httpServletRequest);

        setHeadDetails(head, requestBean);

        requestBean.setMid(body.getMid());
        requestBean.setOrderId(body.getOrderId());
        requestBean.setTxnAmount(body.getTxnAmount().getValue());

        requestBean.setPaymentMode(body.getPaymentMode());
        requestBean.setWebsite(body.getWebsite());

        requestBean.setPaymentTypeId(body.getPaymentMode());

        requestBean.setFeesAmount(body.getFeesAmount());
        requestBean.setVanInfo(body.getVanInfo());

        Map<String, Object> requestDTO = null;
        if (null == requestBean.getBankTransferDTO()) {
            requestDTO = new HashMap<>();
            requestBean.setBankTransferDTO(requestDTO);
        }
        requestDTO.put(BANK_TRANSFER, request);
        return requestBean;
    }

    private void setHeadDetails(SecureRequestHeader head, PaymentRequestBean requestBean) {
        requestBean.setTokenType(head.getTokenType());
        requestBean.setJwtToken(head.getToken());
        requestBean.setChannelId(head.getChannelId().getValue());
        requestBean.setClientId(head.getClientId());
        requestBean.setVersion(head.getVersion());
    }
}
