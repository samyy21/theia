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
import com.paytm.pgplus.facade.notification.request.SendMessageRequest;
import com.paytm.pgplus.facade.notification.response.SendMessageResponse;
import com.paytm.pgplus.facade.notification.service.IMessageService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("sendSMSAppInvokeTask")
public class SendSMSAppInvokeTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendSMSAppInvokeTask.class);

    @Autowired
    MessageHelper messageHelper;

    @Autowired
    IMessageService messageService;

    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {
        SendMessageRequest sendMessageRequest = messageHelper.generateSendSMSPayloadForAppInvoke();
        sendMessageRequest.getBody().setContentPlaceholders(
                ImmutableMap.of(TheiaConstant.RequestParams.SHORT_LINK,
                        response.getShortUrlAPIResponse().getShortUrl(), TheiaConstant.RequestParams.TXN_AMOUNT,
                        input.getTxnAmount(), TheiaConstant.RequestParams.ORDER_ID, input.getOrderID(),
                        TheiaConstant.RequestParams.MERCHANT_NAME_NOTIFICATION,
                        input.getExtendInfo().getMerchantName(), TheiaConstant.RequestParams.TTL,
                        ConfigurationUtil.getProperty(BizConstant.THEIA_SHORTEN_URL_TTL, "15")));
        sendMessageRequest.getBody().setPhoneNo(
                input.getUserDetailsBiz() != null ? input.getUserDetailsBiz().getMobileNo() : "");
        SendMessageResponse sendMessageResponse;
        try {
            sendMessageResponse = messageService.sendMessage(sendMessageRequest);
            if (sendMessageResponse == null) {
                return new GenericCoreResponseBean<SendMessageResponse>("Send Message Response received is empty");
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error while sending SMS : {} ", e);
            return new GenericCoreResponseBean<SendMessageResponse>(
                    "Error while communicating with Send Message Service ");
        }
        response.setSendMessageResponse(sendMessageResponse);
        return new GenericCoreResponseBean<>(sendMessageResponse);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.SEND_SMS_APP_INVOKE;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.SMS_APP_INVOKE_TIME, "200"));
    }
}
