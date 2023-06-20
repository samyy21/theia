package com.paytm.pgplus.biz.taskengine.task.impl;

import com.google.common.collect.ImmutableMap;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.notification.helper.MessageHelper;
import com.paytm.pgplus.facade.notification.request.SendPushNotificationRequest;
import com.paytm.pgplus.facade.notification.response.SendPushNotificationResponse;
import com.paytm.pgplus.facade.notification.service.IMessageService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("sendPushNotificationAppInvokeTask")
public class SendPushNotificationAppInvokeTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendPushNotificationAppInvokeTask.class);

    @Autowired
    MessageHelper messageHelper;

    @Autowired
    IMessageService messageService;

    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {
        SendPushNotificationResponse sendPushNotificationResponse = null;
        SendPushNotificationRequest sendPushNotificationRequest = messageHelper
                .generateSendPushNotificationPayloadForAppInvoke(Arrays.asList(input.getUserDetailsBiz() != null ? input
                        .getUserDetailsBiz().getUserId() : ""));
        sendPushNotificationRequest.getBody().setDeepLink(input.getAppInvokeURL());
        sendPushNotificationRequest.getBody()
                .setContentPlaceholders(
                        ImmutableMap.of(TheiaConstant.RequestParams.SHORT_LINK, response.getShortUrlAPIResponse()
                                .getShortUrl(), TheiaConstant.RequestParams.TXN_AMOUNT, input.getTxnAmount(),
                                TheiaConstant.RequestParams.ORDER_ID, input.getOrderID(),
                                TheiaConstant.RequestParams.MERCHANT_NAME_NOTIFICATION, input.getExtendInfo()
                                        .getMerchantName()));
        try {
            sendPushNotificationResponse = messageService.sendPushNotification(sendPushNotificationRequest);
            if (sendPushNotificationResponse == null) {
                return new GenericCoreResponseBean<SendPushNotificationResponse>(
                        "Send Push Notification response received is empty");
            }
        } catch (FacadeCheckedException e) {
            LOGGER.info("Error while sending Push Notification : {} ", e);
            return new GenericCoreResponseBean<SendPushNotificationResponse>(
                    "Error while communicating with Send Push Notification service");
        }
        return new GenericCoreResponseBean<>(sendPushNotificationResponse);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.SEND_NOTIFICATION_APP_INVOKE;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return Boolean.valueOf(ConfigurationUtil.getProperty(
                BizConstant.THEIA_SEND_PUSH_NOTIFICATION_APP_INVOKE_ENABLE, "false"));
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.SHORTEN_URL_TIME, "200"));
    }
}
