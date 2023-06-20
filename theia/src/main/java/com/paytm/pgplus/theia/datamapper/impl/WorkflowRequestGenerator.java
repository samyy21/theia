package com.paytm.pgplus.theia.datamapper.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.TheiaSessionException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("workflowRequestGenerator")
public class WorkflowRequestGenerator {

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRequestGenerator.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    BizRequestResponseMapperImpl bizRequestResponseMapper;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginServiceBiz;

    public WorkFlowRequestBean generateRequestBeanForPostLogin(final PaymentRequestBean paymentRequestData) {

        final MerchantInfo merchantInfo = theiaSessionDataService.getMerchantInfoFromSession(paymentRequestData
                .getRequest());
        final TransactionConfig transactionConfig = theiaSessionDataService.getTxnConfigFromSession(paymentRequestData
                .getRequest());
        final TransactionInfo transInfo = theiaSessionDataService
                .getTxnInfoFromSession(paymentRequestData.getRequest());

        if (merchantInfo == null || transactionConfig == null || transInfo == null) {
            throw new TheiaSessionException("Mandatory session attributes are missing");
        }

        ExtendedInfoRequestBean extendedInfoRequestBean = theiaSessionDataService
                .geExtendedInfoRequestBean(paymentRequestData.getRequest());

        if (extendedInfoRequestBean == null) {
            extendedInfoRequestBean = new ExtendedInfoRequestBean();
        }

        if (StringUtils.isBlank(extendedInfoRequestBean.getPaytmMerchantId())) {
            if (merchantInfo != null) {
                extendedInfoRequestBean.setAlipayMerchantId(merchantInfo.getInternalMid());
                extendedInfoRequestBean.setPaytmMerchantId(merchantInfo.getMid());
            }
            extendedInfoRequestBean.setMerchantTransId(transInfo.getOrderId());
        }

        final WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        paymentRequestData.setRequestType(transInfo.getRequestType());
        paymentRequestData.setTxnAmount(transInfo.getTxnAmount());
        workFlowRequestBean.setRequestType(ERequestType.getByRequestType(paymentRequestData.getRequestType()));
        workFlowRequestBean.setPaytmMID(merchantInfo.getMid());
        workFlowRequestBean.setAlipayMID(merchantInfo.getInternalMid());
        workFlowRequestBean.setTransID(transInfo.getTxnId());
        workFlowRequestBean.setoAuthCode(paymentRequestData.getAuthCode());
        workFlowRequestBean.setOrderID(transInfo.getOrderId());
        workFlowRequestBean.setPayModeOnly(transactionConfig.getPaymentModeOnly());
        workFlowRequestBean.setAllowedPaymentModes(transactionConfig.getAllowedPayModes());
        workFlowRequestBean.setDisabledPaymentModes(transactionConfig.getDisallowedPayModes());
        workFlowRequestBean
                .setChannelID(getChannel(transactionConfig.getCommMode(), paymentRequestData.getoAuthState()));
        workFlowRequestBean.setWebsite(transInfo.getWebsite());
        workFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(transInfo.getTxnAmount()));
        workFlowRequestBean.setIndustryTypeID(merchantInfo.getMerchantCategoryCode());
        workFlowRequestBean.setEnvInfoReqBean(theiaSessionDataService.getEnvInfoRequestBean(paymentRequestData
                .getRequest()));
        workFlowRequestBean.setSubsTypes(transactionConfig.getSubsTypes());
        workFlowRequestBean.setCustID(transInfo.getCustID());
        workFlowRequestBean.setStoreCardPrefEnabled(merchantInfo.isMerchantStoreCardPref());

        if (StringUtils.isNotBlank(transInfo.getSubscriptionPaymentMode())) {
            workFlowRequestBean.setSubsPayMode(SubsPaymentMode.valueOf(transInfo.getSubscriptionPaymentMode()));
        }
        workFlowRequestBean.setSubsPPIOnly(theiaSessionDataService.getTxnInfoFromSession(
                paymentRequestData.getRequest()).getSubscriptionPPIOnly());

        if (TheiaConstant.RequestTypes.ADD_MONEY.equals(transInfo.getRequestType())) {
            workFlowRequestBean.setTransType(ETransType.TOP_UP);
        } else {
            workFlowRequestBean.setTransType(ETransType.ACQUIRING);
        }

        setClientIdAndSecret(merchantInfo.getMid(), workFlowRequestBean, paymentRequestData.getoAuthState());

        workFlowRequestBean.setExtendInfo(extendedInfoRequestBean);
        workFlowRequestBean.setSubWalletOrderAmountDetails(transInfo.getSubwalletAmount());
        return workFlowRequestBean;
    }

    private String getChannel(final String channelID, String oAuthState) {

        if (StringUtils.isNotBlank(oAuthState)) {

            String[] state = oAuthState.split(":");

            if (state.length >= 3) {

                switch (state[2]) {

                case "WEB":
                    return "WEB";
                case "WAP":
                    return "WAP";
                }
            }
        }

        return channelID;
    }

    private void setClientIdAndSecret(final String merchantId, final WorkFlowRequestBean workFlowRequestBean,
            final String authState) {

        String clientId;
        String secretKey;

        final String noCookieMids = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_MIDS);
        final String oauthOtpMids = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_MIDS);

        String channel = workFlowRequestBean.getChannelID();

        if (StringUtils.isNotBlank(authState)) {

            String[] stateSplit = authState.split(":");

            if (stateSplit.length > 3) {

                if (stateSplit[2].equals("undefined")) {
                    channel = stateSplit[3];
                } else {
                    channel = stateSplit[2];
                }
            }
        }

        if (merchantId != null && noCookieMids.contains(merchantId)) {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_SECRET_KEY);
        } else if (merchantId != null && oauthOtpMids.contains(merchantId)) {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_SECRET_KEY);

        } else if (EChannelId.WAP.toString().equals(channel)) {

            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_WAP_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_WAP_SECRET_KEY);
        } else {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        }

        workFlowRequestBean.setOauthClientId(clientId);
        workFlowRequestBean.setOauthSecretKey(secretKey);
    }

    public String[] fetchClientIDAndClientSecret(final String merchantId, final String authState) {

        String clientId;
        String secretKey;

        final String noCookieMids = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_MIDS);
        final String oauthOtpMids = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_MIDS);

        String channel = "WAP";

        if (StringUtils.isNotBlank(authState)) {

            String[] stateSplit = authState.split(":");

            if (stateSplit.length > 3) {

                if (stateSplit[2].equals("undefined")) {
                    channel = stateSplit[3];
                } else {
                    channel = stateSplit[2];
                }
            }
        }

        if (merchantId != null && noCookieMids.contains(merchantId)) {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_NOCOOKIE_CLIENT_SECRET_KEY);
        } else if (merchantId != null && oauthOtpMids.contains(merchantId)) {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_OTP_CLIENT_SECRET_KEY);

        } else if (EChannelId.WAP.toString().equals(channel)) {

            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_WAP_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_WAP_SECRET_KEY);
        } else {
            clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID);
            secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        }

        return new String[] { clientId, secretKey };

    }

}
