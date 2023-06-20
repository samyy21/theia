/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetails;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.biz.utils.BizConstant.SEND_DEEPLINK_INFO_INSTA;

@Service("nativeCoreService")
public class NativeCoreService {

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    public static final Logger LOGGER = LoggerFactory.getLogger(NativeCoreService.class);

    private static final String PASS_THROUGH_EXTEND_INFO_KEY = "passThroughExtendInfo";
    private static final String THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS = "theia.staticRedis.migration.sessionRedis";
    public static final String USER_PROFILE_SARVATRA_FROM_TXNTOKEN = "USER_PROFILE_SARVATRA_FROM_TXNTOKEN";

    public void ceatePassThroughExtendedInfoNativeUPI(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) throws PaytmValidationException {
        try {

            UserProfileSarvatra userProfileSarvatra = null;
            userProfileSarvatra = (UserProfileSarvatra) theiaSessionRedisUtil.hget(flowTransBean.getWorkFlowBean()
                    .getTxnToken(), "userProfileSarvatra");

            if (userProfileSarvatra == null) {
                if (!ff4jUtils.isFeatureEnabledOnMid(USER_PROFILE_SARVATRA_FROM_TXNTOKEN,
                        THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
                    userProfileSarvatra = (UserProfileSarvatra) theiaTransactionalRedisUtil.hget(flowTransBean
                            .getWorkFlowBean().getTxnToken(), "userProfileSarvatra");
                }
            }

            Map<String, String> passThroughExtendInfoMap = new HashMap<>();
            passThroughExtendInfoMap.put(FacadeConstants.DEVICE_ID, flowTransBean.getWorkFlowBean().getDeviceId());

            if (flowTransBean.getUserDetails() != null) {
                passThroughExtendInfoMap.put(FacadeConstants.MOBILE_NO, flowTransBean.getUserDetails().getMobileNo());
                passThroughExtendInfoMap.put(FacadeConstants.PAYTM_USER_ID, flowTransBean.getUserDetails().getUserId());
                // setting paytmUserId in custId to be sent to insta
                passThroughExtendInfoMap.put(FacadeConstants.CUSTID, flowTransBean.getUserDetails().getUserId());
            }

            passThroughExtendInfoMap.put(FacadeConstants.SEQUENCE_NUMBER, flowTransBean.getWorkFlowBean().getSeqNo());
            // For FPV2 UPI PUSH FLOW
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getUpiAccRefId())) {
                LOGGER.info("Setting AccRefId and origin-channel for UPI Flow");
                passThroughExtendInfoMap.put(FacadeConstants.UPI_ACC_REF_ID, flowTransBean.getWorkFlowBean()
                        .getUpiAccRefId());
                passThroughExtendInfoMap.put(FacadeConstants.ORIGIN_CHANNEL, flowTransBean.getWorkFlowBean()
                        .getOriginChannel());

            } else if (flowTransBean.getWorkFlowBean().getAccountNumber() != null
                    && flowTransBean.getWorkFlowBean().getIfsc() != null) {
                LOGGER.info("Setting Ifsc and account for User consent flow");
                passThroughExtendInfoMap.put(FacadeConstants.IFSC, flowTransBean.getWorkFlowBean().getIfsc());
                passThroughExtendInfoMap.put(FacadeConstants.ACCOUNT_NUMBER, flowTransBean.getWorkFlowBean()
                        .getAccountNumber());
            } else if (userProfileSarvatra != null) {
                PaytmVpaDetails paytmVpaDetails = userProfileSarvatra.getResponse();
                for (SarvatraVpaDetails sarvatraVpaDetails : paytmVpaDetails.getVpaDetails()) {
                    if (sarvatraVpaDetails.getName().equalsIgnoreCase(
                            flowTransBean.getWorkFlowBean().getVirtualPaymentAddress())) {
                        passThroughExtendInfoMap.put(FacadeConstants.IFSC, sarvatraVpaDetails.getDefaultDebit()
                                .getIfsc());
                        passThroughExtendInfoMap.put(FacadeConstants.ACCOUNT_NUMBER, sarvatraVpaDetails
                                .getDefaultDebit().getAccount());

                        break;
                    }
                }
            } else {
                LOGGER.info("Neither the account number is passed nor found in sarvatra profile");
            }
            passThroughExtendInfoMap.put(FacadeConstants.MPIN, flowTransBean.getWorkFlowBean().getMpin());
            passThroughExtendInfoMap.put(FacadeConstants.PAYER_VPA, flowTransBean.getWorkFlowBean()
                    .getVirtualPaymentAddress());
            passThroughExtendInfoMap.put(FacadeConstants.ORDER_ID, flowTransBean.getWorkFlowBean().getOrderID());
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getAppId())) {
                passThroughExtendInfoMap.put(FacadeConstants.APP_ID, flowTransBean.getWorkFlowBean().getAppId());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubscriptionID())) {
                passThroughExtendInfoMap.put(FacadeConstants.UPI_RECURRING_KEY, "UPI_SUBSCRIPTION_"
                        + flowTransBean.getWorkFlowBean().getPaytmMID() + "_"
                        + flowTransBean.getWorkFlowBean().getOrderID());
            }
            if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPreAuthExpiryDate())) {
                passThroughExtendInfoMap.put(FacadeConstants.VALIDITY_END_DATE, flowTransBean.getWorkFlowBean()
                        .getPreAuthExpiryDate());
            }

            String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
            String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
            channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);

        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            throw new PaytmValidationException(e);
        }

    }

    public void ceatePassThroughExtendedInfoNativeUPIIntent(WorkFlowTransactionBean flowTransBean,
            Map<String, String> channelInfo) throws PaytmValidationException {
        if (flowTransBean.getWorkFlowBean().isDeepLinkFromInsta()) {
            try {
                LOGGER.info("Setting passthrough ExtendInfo,deep Link required from insta");
                Map<String, String> passThroughExtendInfoMap = new HashMap<>();
                passThroughExtendInfoMap.put(TXN_NOTE, flowTransBean.getWorkFlowBean().getTxnNote());
                passThroughExtendInfoMap.put(REF_URL, flowTransBean.getWorkFlowBean().getRefUrl());
                passThroughExtendInfoMap.put(NATIVE_JSON_REQUEST, "true");
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getSubscriptionID())) {
                    passThroughExtendInfoMap.put(FacadeConstants.UPI_RECURRING_KEY, "UPI_SUBSCRIPTION_"
                            + flowTransBean.getWorkFlowBean().getPaytmMID() + "_"
                            + flowTransBean.getWorkFlowBean().getOrderID());
                }
                if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPreAuthExpiryDate())) {
                    passThroughExtendInfoMap.put(FacadeConstants.VALIDITY_END_DATE, flowTransBean.getWorkFlowBean()
                            .getPreAuthExpiryDate());
                }

                // for deeplink pspApp and osType info
                if (Boolean.parseBoolean(ConfigurationUtil.getProperty(SEND_DEEPLINK_INFO_INSTA, "false"))) {
                    if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getPspApp())) {
                        passThroughExtendInfoMap.put(PSP_APP, flowTransBean.getWorkFlowBean().getPspApp());
                    }

                    if (StringUtils.isNotBlank(flowTransBean.getWorkFlowBean().getOsType())) {
                        passThroughExtendInfoMap.put(OS_TYPE, flowTransBean.getWorkFlowBean().getOsType());
                    }
                }

                // setting custId and mobileNo
                if (flowTransBean.getUserDetails() != null) {
                    passThroughExtendInfoMap.put(FacadeConstants.MOBILE_NO, flowTransBean.getUserDetails()
                            .getMobileNo());
                    passThroughExtendInfoMap.put(FacadeConstants.CUSTID, flowTransBean.getUserDetails().getUserId());
                }

                String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
                String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
                channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
            } catch (FacadeUncheckedException | FacadeCheckedException e) {
                throw new PaytmValidationException(e);
            }
        }
    }
}
