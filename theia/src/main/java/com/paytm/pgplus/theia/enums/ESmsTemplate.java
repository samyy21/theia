package com.paytm.pgplus.theia.enums;

import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams;

public enum ESmsTemplate {

    LOGIN_OTP_DEFAULT_MESSAGE(StringUtils.EMPTY, ExtraConstants.LOGIN_OTP_DEFAULT_MESSAGE_TEMPLATE_ID,
            ExtraConstants.LOGIN_OTP_DEFAULT_MESSAGE), LINK_DEFAULT_MESSAGE(StringUtils.EMPTY,
            RequestParams.DEFAULT_MESSAGE_TEMPLATE_ID, RequestParams.DEFAULT_MESSAGE), SUBSCRIPTION_CREATE_OTP_MESSAGE(
            ExtraConstants.SUBSCRIPTION_CREATE_OTP_MESSAGE, ExtraConstants.SUBSCRIPTION_CREATE_OTP_MESSAGE_TEMPLATE_ID), LOGIN_OTP_MESSAGE_WITHOUT_HASH(
            ExtraConstants.LOGIN_OTP_MESSAGE_WITHOUT_HASH, ExtraConstants.LOGIN_OTP_MESSAGE_WITHOUT_HASH_TEMPLATE_ID), LOGIN_OTP_MESSAGE_WITH_HASH(
            ExtraConstants.LOGIN_OTP_MESSAGE_WITH_HASH, ExtraConstants.LOGIN_OTP_MESSAGE_WITH_HASH_TEMPLATE_ID), LINK_LOGIN_OTP(
            RequestParams.LOGIN_OTP, RequestParams.LOGIN_OTP_TEMPLATE_ID);

    private static final String ENTITY_ID_PROPERTY_KEY = "oauth.sms.entity.id";
    private static final String SMS_SENDER_ID_PROPERTY_KEY = "oauth.sms.sender.id";
    private static final String ENTITY_ID_DEFAULT_VALUE = "1501601290000011395";
    private static final String SMS_SENDER_ID_DEFAULT_VALUE = "iPaytm";
    private String messageProperty;
    private String templateIdProperty;
    private String defaultMsgString;

    ESmsTemplate(String messageProperty, String templateIdProperty) {
        this.messageProperty = messageProperty;
        this.templateIdProperty = templateIdProperty;
    }

    ESmsTemplate(String messageProperty, String templateIdProperty, String defaultMsgString) {
        this.messageProperty = messageProperty;
        this.templateIdProperty = templateIdProperty;
        this.defaultMsgString = defaultMsgString;
    }

    public String getMessage() {
        if (this == LOGIN_OTP_DEFAULT_MESSAGE || this == LINK_DEFAULT_MESSAGE) {
            return this.defaultMsgString;
        }
        return ConfigurationUtil.getProperty(messageProperty);
    }

    public String getTemplateId() {
        return ConfigurationUtil.getProperty(templateIdProperty);
    }

    public static String getEntityId() {
        return ConfigurationUtil.getProperty(ENTITY_ID_PROPERTY_KEY, ENTITY_ID_DEFAULT_VALUE);
    }

    public static String getSmsSenderId() {
        return ConfigurationUtil.getProperty(SMS_SENDER_ID_PROPERTY_KEY, SMS_SENDER_ID_DEFAULT_VALUE);
    }

    public static ESmsTemplate withDefault(ESmsTemplate smsTemplate, ESmsTemplate defaultTemplate) {
        if (StringUtils.isBlank(smsTemplate.getMessage())) {
            return defaultTemplate;
        }
        return smsTemplate;
    }

}
