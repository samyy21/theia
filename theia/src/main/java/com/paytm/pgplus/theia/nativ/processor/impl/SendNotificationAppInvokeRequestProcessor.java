package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.facade.notification.response.SendMessageResponse;
import com.paytm.pgplus.facade.notification.response.SendPushNotificationResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.sendNotification.SendNotificationRequest;
import com.paytm.pgplus.theia.nativ.model.sendNotification.SendNotificationResponse;
import com.paytm.pgplus.theia.nativ.model.sendNotification.SendNotificationResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.oneclick.Constants.Constants;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.APMERSAND;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.QUESTION_MARK;

@Service("sendNotificationAppInvokeRequestProcessor")
@NativeControllerAdvice
public class SendNotificationAppInvokeRequestProcessor
        extends
        AbstractRequestProcessor<SendNotificationRequest, SendNotificationResponse, WorkFlowRequestBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotificationAppInvokeRequestProcessor.class);

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    ProcessTransactionUtil processTransactionUtil;

    @Override
    protected WorkFlowRequestBean preProcess(SendNotificationRequest request) throws Exception {
        return validateRequestAndCreateWorkflowRequestBean(request);
    }

    @Override
    protected WorkFlowResponseBean onProcess(SendNotificationRequest request, WorkFlowRequestBean workFlowRequestBean)
            throws Exception {
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = taskExecutor.execute(workFlowRequestBean);
        return workFlowResponseBean != null ? workFlowResponseBean.getResponse() : null;
    }

    @Override
    protected SendNotificationResponse postProcess(SendNotificationRequest request,
            WorkFlowRequestBean workFlowRequestBean, WorkFlowResponseBean response) throws Exception {
        SendNotificationResponse sendNotificationResponse = new SendNotificationResponse();
        ResponseHeader responseHeader = new ResponseHeader("v1");
        sendNotificationResponse.setBody(new SendNotificationResponseBody());
        sendNotificationResponse.setHead(responseHeader);
        if (response != null && response.getShortUrlAPIResponse() != null
                && StringUtils.isNotBlank(response.getShortUrlAPIResponse().getShortUrl())
                && checkIfSMSSent(response.getSendMessageResponse())
                && checkIfPushNotifcationSent(response.getSendPushNotificationResponse())) {
            sendNotificationResponse.getBody().setMessageSent("true");
            sendNotificationResponse.getBody().setNotificationSent("false");
            sendNotificationResponse.getBody().setPageTimeout(
                    ConfigurationUtil.getProperty(BizConstant.THEIA_SEND_NOTIFICATION_TIMEOUT, "480000"));
            sendNotificationResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        } else {
            sendNotificationResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForFailure());
        }
        return sendNotificationResponse;
    }

    private WorkFlowRequestBean validateRequestAndCreateWorkflowRequestBean(SendNotificationRequest request)
            throws UnsupportedEncodingException {

        if (null == request || null == request.getHead() || null == request.getBody()) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.INVALID_REQUEST));
        }
        if (StringUtils.isBlank(request.getHead().getToken())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_TOKEN));
        }

        if (request.getHead().getTokenType() == null) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_TOKEN_TYPE));
        }

        if (!StringUtils.equalsIgnoreCase(request.getHead().getTokenType(), TokenType.TXN_TOKEN.getType())) {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }

        HttpServletRequest servletRequest = OfflinePaymentUtils.gethttpServletRequest();

        if (!StringUtils.equalsIgnoreCase(request.getBody().getMid(),
                servletRequest.getParameter(TheiaConstant.RequestParams.Native.MID))) {
            throw MidDoesnotMatchException.getException();
        }

        if (!StringUtils.equalsIgnoreCase(request.getBody().getOrderId(),
                servletRequest.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID))) {
            throw OrderIdDoesnotMatchException.getException();
        }

        processTransactionUtil.pushNativePaymentEvent(request.getBody().getMid(), request.getBody().getOrderId(),
                "SEND_NOTIFICATION_APP_INVOKE");

        int maxPermitsAllowed = Integer.parseInt(ConfigurationUtil.getProperty(
                BizConstant.THEIA_RATE_LIMIT_SEND_NOTIFICATION, "3"));

        String isResend = OfflinePaymentUtils.gethttpServletRequest().getParameter("isResend");

        if ((isResend == null || StringUtils.equalsIgnoreCase(isResend, "false"))
                && nativeSessionUtil.getPermitsUsedForAPICall(request.getHead().getToken(), "appInvokePermitsUsed") > maxPermitsAllowed) {
            throw SessionExpiredException.getException(ResultCode.RATE_LIMIT_BREACHED_EXCEPTION);
        }

        if ((isResend != null && StringUtils.equalsIgnoreCase(isResend, "true"))
                && nativeSessionUtil.getPermitsUsedForAPICall(request.getHead().getToken(),
                        "appInvokePermitsUsedResend") > maxPermitsAllowed) {
            throw SessionExpiredException.getException(ResultCode.RATE_LIMIT_BREACHED_EXCEPTION);

        }

        InitiateTransactionRequestBody initiateTransactionRequestBody = nativeSessionUtil.getOrderDetail(request
                .getHead().getToken());
        if (initiateTransactionRequestBody == null) {
            throw new SessionExpiredException(OfflinePaymentUtils.resultInfo(ResultCode.SESSION_EXPIRED_EXCEPTION));
        }

        if (!StringUtils.equalsIgnoreCase(initiateTransactionRequestBody.getMid(), request.getBody().getMid())
                || !StringUtils.equalsIgnoreCase(initiateTransactionRequestBody.getOrderId(), request.getBody()
                        .getOrderId())) {
            throw new SessionExpiredException(OfflinePaymentUtils.resultInfo(ResultCode.SESSION_EXPIRED_EXCEPTION));
        }

        nativeSessionUtil.setField(request.getHead().getToken(), BizConstant.COLLECT_API_INVOKE, "true");
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        flowRequestBean.setIsAddMoneyAppInvoke(true);
        flowRequestBean.setPaytmMID(request.getBody().getMid());
        flowRequestBean.setRequestType(ERequestType.ADD_AND_PAY);
        flowRequestBean.setTxnAmount(initiateTransactionRequestBody.getTxnAmount().getValue());
        flowRequestBean.setOrderID(request.getBody().getOrderId());
        flowRequestBean.setExtendInfo(new ExtendedInfoRequestBean());
        flowRequestBean.getExtendInfo().setMerchantName(
                merchantExtendInfoUtils.getMerchantName(request.getBody().getMid()));
        if (nativeSessionUtil.getUserDetails(request.getHead().getToken()) != null) {
            flowRequestBean.setUserDetailsBiz(nativeSessionUtil.getUserDetails(request.getHead().getToken()));
            nativeSessionUtil.setField(request.getHead().getToken(), "sendNotificationAppInvokeUserId", flowRequestBean
                    .getUserDetailsBiz().getUserId());
        } else {
            throw new SessionExpiredException(OfflinePaymentUtils.resultInfo(ResultCode.SESSION_EXPIRED_EXCEPTION));
        }

        flowRequestBean.setAppInvokeURL(URLEncoder.encode(getAppInvokeURL(request, initiateTransactionRequestBody),
                StandardCharsets.UTF_8.toString()));
        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(OfflinePaymentUtils.gethttpServletRequest());
        flowRequestBean.setEnvInfoReqBean(envInfo);
        LOGGER.info("flowRequestBean created successfully");
        return flowRequestBean;
    }

    private String getAppInvokeURL(SendNotificationRequest request,
            InitiateTransactionRequestBody initiateTransactionRequestBody) {
        StringBuilder sb = new StringBuilder(
                com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL));
        String url = processTransactionUtil.isV3AppInvokeRequired(request.getBody().getMid()) ? TheiaConstant.ExtraConstants.NATIVE_APP_INVOKE_URL_V3
                : TheiaConstant.ExtraConstants.NATIVE_APP_INVOKE_URL_V2;
        sb.append(url).append(QUESTION_MARK).append("mid=").append(request.getBody().getMid()).append(APMERSAND)
                .append("orderId=").append(request.getBody().getOrderId()).append(APMERSAND).append("txnToken=")
                .append(request.getHead().getToken()).append(APMERSAND).append("amount=")
                .append(initiateTransactionRequestBody.getTxnAmount().getValue()).append(APMERSAND)
                .append("isAppLink=true").toString();
        return sb.toString();
    }

    private boolean checkIfSMSSent(SendMessageResponse response) {
        boolean flag = false;

        if (response != null && response.getBody() != null && response.getHead() != null
                && response.getBody().getResultInfo() != null
                && response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S")) {
            flag = true;
        }

        return flag;
    }

    private boolean checkIfPushNotifcationSent(SendPushNotificationResponse response) {

        boolean flag = false;
        if (("false").equalsIgnoreCase(ConfigurationUtil.getProperty(
                BizConstant.THEIA_SEND_PUSH_NOTIFICATION_APP_INVOKE_ENABLE, "false"))) {
            flag = true;
            return flag;
        }
        if (response != null && response.getBody() != null && response.getHead() != null
                && response.getBody().getResultInfo() != null
                && response.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S")) {
            flag = true;
        }

        return flag;
    }

}