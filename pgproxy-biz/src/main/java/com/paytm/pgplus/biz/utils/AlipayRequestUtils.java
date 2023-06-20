/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import com.paytm.pgplus.facade.common.model.*;
import com.paytm.pgplus.facade.common.model.EnvInfo.EnvInfoBuilder;
import com.paytm.pgplus.facade.consume.enums.PaymentType;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;

import com.paytm.pgplus.facade.enums.BusinessFlow;
import com.paytm.pgplus.facade.enums.PaymentFlow;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author amitdubey
 */
public class AlipayRequestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlipayRequestUtils.class);
    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    @SuppressWarnings("unchecked")
    public static Map<String, String> getExtendeInfoMap(final ExtendedInfoRequestBean extInfoReqBean) {
        long inTime = System.currentTimeMillis();
        try {
            Map<String, Object> map = extInfoReqBean != null ? JsonMapper.convertValueIncludeTransient(extInfoReqBean,
                    Map.class) : Collections.emptyMap();
            Map<String, String> flatMap = CommonUtils.flattenMap(map, null);
            encodeCommentInAdditionalInfo(flatMap);
            encodeMerchantInfoAddress1(flatMap);
            parseAndPopulateKeyValFromAdditionalInfo(flatMap);
            return flatMap;

        } catch (Exception e) {
            LOGGER.error("Exception occurred : ", e);
            LOGGER.debug("Time taken in  AlipayRequestUtils.getExtendeInfoMap is {}", System.currentTimeMillis()
                    - inTime);
        }
        return Collections.emptyMap();
    }

    private static void parseAndPopulateKeyValFromAdditionalInfo(Map<String, String> extendInfoMap) {
        if (extendInfoMap == null) {
            return;
        }
        Map<String, String> addtionalInfoMap = parseAdditionalInfo(extendInfoMap);
        if (addtionalInfoMap != null) {
            extendInfoMap.putAll(addtionalInfoMap);
        }
    }

    private static Map<String, String> parseAdditionalInfo(Map<String, String> extendInfoMap) {
        if (extendInfoMap != null
                && StringUtils.isNotEmpty(extendInfoMap.get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO))) {
            String additionalInfoStr = extendInfoMap.get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO);

            LOGGER.debug("AdditionalInfo : {}", additionalInfoStr);

            String[] additionalInfoKeyValArray = additionalInfoStr.split(Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            Map<String, String> additionalInfoMap = new HashMap<>(additionalInfoKeyValArray.length);
            for (String keyVal : additionalInfoKeyValArray) {
                String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                if (keyValSplit.length == 2) {
                    if (StringUtils.equals(keyValSplit[0].trim(), TheiaConstant.RequestParams.OFFLINE_APP_MODE)) {
                        additionalInfoMap.put(TheiaConstant.RequestParams.MODE, keyValSplit[1].trim());
                    } else {
                        additionalInfoMap.put(keyValSplit[0].trim(), keyValSplit[1].trim());
                    }
                }
            }
            return additionalInfoMap;
        }
        return null;
    }

    private static void encodeCommentInAdditionalInfo(Map<String, String> extendInfoMap) {
        if (extendInfoMap != null
                && StringUtils.isNotEmpty(extendInfoMap.get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO))) {
            String additionalInfoStr = extendInfoMap.get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO);
            extendInfoMap.put(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO,
                    encodeDecodeCommentInAdditionalInfo(additionalInfoStr, (val) -> encode(val)));
        }
    }

    private static void encodeMerchantInfoAddress1(Map<String, String> extendInfoMap) {
        if (extendInfoMap != null
                && StringUtils.isNotEmpty(extendInfoMap.get(BizConstant.ExtendedInfoKeys.MERCHANT_ADDRESS_1))) {
            String address1 = extendInfoMap.get(BizConstant.ExtendedInfoKeys.MERCHANT_ADDRESS_1);
            extendInfoMap.put(BizConstant.ExtendedInfoKeys.MERCHANT_ADDRESS_1, encode(address1));
        }
    }

    private static String encode(String value) {
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("Value to be encoded is blank returning same value");
            return value;
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Exception in URL encoding");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return value;
        }
    }

    public static String decodeCommentInAdditionalInfo(String additionalInfoStr) {
        return encodeDecodeCommentInAdditionalInfo(additionalInfoStr, (val) -> decode(val));
    }

    private static String encodeDecodeCommentInAdditionalInfo(String additionalInfoStr, Function<String, String> f) {
        if (StringUtils.isBlank(additionalInfoStr)) {
            return additionalInfoStr;
        }
        String splitPattern = "comment" + ADDITIONAL_INFO_KEY_VAL_SEPARATOR;
        String[] splitFromComment = additionalInfoStr.split(Pattern.quote(splitPattern));
        if (splitFromComment.length == 2) {
            String[] commentValueAndRemaining = splitFromComment[1].split(Pattern.quote(ADDITIONAL_INFO_DELIMITER), 2);
            String commentValue = commentValueAndRemaining[0];
            String remainingStr = (commentValueAndRemaining.length == 2) ? ADDITIONAL_INFO_DELIMITER
                    + commentValueAndRemaining[1] : "";
            String result = splitFromComment[0] + splitPattern + f.apply(commentValue) + remainingStr;
            LOGGER.debug("resulted additionalInfo = {}", result);
            return result;
        }
        LOGGER.debug("sending back additionalInfoStr as response");
        return additionalInfoStr;
    }

    public static String decode(String value) {
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("Value to be decoded is blank returning same value");
            return value;
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Exception in URL decoding");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return value;
        }
    }

    public static EnvInfo createEnvInfo(final EnvInfoRequestBean envInfoRequestBean)
            throws FacadeInvalidParameterException {

        final String clientIp = envInfoRequestBean.getClientIp();
        TerminalType terminalType = null;
        if (envInfoRequestBean.getTerminalType() != null
                && TerminalType.getTerminalTypeByTerminal(envInfoRequestBean.getTerminalType().toString()) != null) {
            terminalType = TerminalType.getTerminalTypeByTerminal(envInfoRequestBean.getTerminalType().toString());
        }
        final EnvInfo.EnvInfoBuilder envBuilder = new EnvInfoBuilder(clientIp, terminalType);

        populateEnvInfoData(envInfoRequestBean, terminalType, envBuilder);
        return envBuilder.build();
    }

    public static Money getMoney(final String currency, final String amount) throws FacadeInvalidParameterException {
        return new Money(currency, amount);
    }

    public static Map<String, String> selectRiskExtendedInfo(final UserDetailsBiz userDetailsBiz) {
        Map<String, String> riskExtendedInfo = new HashMap<>();

        if (userDetailsBiz != null) {
            riskExtendedInfo.put(BizConstant.CUSTOMER_TYPE, String.valueOf(userDetailsBiz.isKYC()));
        }
        return riskExtendedInfo;
    }

    public static HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private static void populateEnvInfoData(EnvInfoRequestBean envInfoRequestBean, TerminalType terminalType,
            EnvInfoBuilder envBuilder) throws FacadeInvalidParameterException {
        envBuilder.sessionId(envInfoRequestBean.getSessionId());
        envBuilder.tokenId(envInfoRequestBean.getTokenId());
        envBuilder.websiteLanguage(envInfoRequestBean.getWebsiteLanguage());
        envBuilder.osType(envInfoRequestBean.getOsType());
        envBuilder.appVersion(envInfoRequestBean.getAppVersion());
        envBuilder.sdkVersion(envInfoRequestBean.getSdkVersion());
        envBuilder.clientKey(envInfoRequestBean.getClientKey());
        envBuilder.orderTerminalType(envInfoRequestBean.getOrderTerminalType());
        envBuilder.orderOsType(envInfoRequestBean.getOrderOsType());
        envBuilder.merchantAppVersion(envInfoRequestBean.getMerchantAppVersion());
        envBuilder.browserType(envInfoRequestBean.getBrowserType());
        envBuilder.browserVersion(envInfoRequestBean.getBrowserVersion());
        envBuilder.clientKey(envInfoRequestBean.getClientKey());
        envBuilder.deviceId(envInfoRequestBean.getDeviceId());
        envBuilder.deviceIMEI(envInfoRequestBean.getDeviceIMEI());
        envBuilder.deviceManufacturer(envInfoRequestBean.getDeviceManufacturer());
        envBuilder.deviceModel(envInfoRequestBean.getDeviceModel());
        envBuilder.deviceType(envInfoRequestBean.getDeviceType());
        envBuilder.extendInfo(envInfoRequestBean.getExtendInfo());
        envBuilder.gender(envInfoRequestBean.getGender());
        envBuilder.hybridPlatform(envInfoRequestBean.getHybridPlatform());
        envBuilder.hybridPlatformVersion(envInfoRequestBean.getHybridPlatformVersion());
        envBuilder.ICCIDNumber(envInfoRequestBean.getICCIDNumber());
        envBuilder.language(envInfoRequestBean.getLanguage());
        envBuilder.merchantAppVersion(envInfoRequestBean.getMerchantAppVersion());
        envBuilder.orderTerminalId(envInfoRequestBean.getOrderTerminalId());
        envBuilder.osVersion(envInfoRequestBean.getOsVersion());
        envBuilder.platform(envInfoRequestBean.getPlatform());
        envBuilder.productCode(envInfoRequestBean.getProductCode());
        envBuilder.routerMac(envInfoRequestBean.getRouterMac());
        envBuilder.screenResolution(envInfoRequestBean.getScreenResolution());
        envBuilder.sdkVersion(envInfoRequestBean.getSdkVersion());
        envBuilder.sessionId(envInfoRequestBean.getSessionId());
        envBuilder.timeZone(envInfoRequestBean.getTimeZone());
        envBuilder.userAgent(envInfoRequestBean.getUserAgent());
        envBuilder.longitude(envInfoRequestBean.getLongitude());
        envBuilder.latitude(envInfoRequestBean.getLatitude());
        envBuilder.merchantType(envInfoRequestBean.getMerchantType());
        // added some more fields
        Map<String, String> extendedInfo = envInfoRequestBean.getExtendInfo();
        if (!TerminalType.WEB.equals(terminalType)) {
            if (extendedInfo == null)
                extendedInfo = new HashMap<>();

            if (!extendedInfo.containsKey(BizConstant.DataEnrichmentKey.DEVICE_ID)) {
                extendedInfo.put("deviceId", envInfoRequestBean.getTokenId());
            }
        }
        envBuilder.extendInfo(extendedInfo);
    }

    public static PaymentBizInfo getPaymentBizinfo(WorkFlowTransactionBean flowTransBean) {
        WorkFlowRequestBean workFlowRequestBean = flowTransBean.getWorkFlowBean();

        if (workFlowRequestBean == null) {
            LOGGER.info("workFlowRequestBean is null");
            return null;
        }

        Map<String, String> riskExtendInfo = workFlowRequestBean.getRiskExtendedInfo();

        PaymentBizInfo paymentBizInfo = new PaymentBizInfo();

        populateFlowInPaymentBizInfo(riskExtendInfo, paymentBizInfo);
        populatePaymentFlowInPaymentBizInfo(workFlowRequestBean, paymentBizInfo);
        populatePaymentAuthenticationFlowInPaymentBizInfo(flowTransBean, paymentBizInfo);
        populatePaymentMethodDetailsInPaymentBizInfo(flowTransBean, paymentBizInfo);
        populatePaymentTypeInPaymentBizInfo(flowTransBean, paymentBizInfo);

        return paymentBizInfo;
    }

    private static void populatePaymentTypeInPaymentBizInfo(WorkFlowTransactionBean flowTransBean,
            PaymentBizInfo paymentBizInfo) {
        PaymentType paymentType = PaymentType.ONE_TIME;
        if (flowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && flowTransBean.getWorkFlowBean().getPaymentRequestBean().isAutoDebitRequest()) {
            paymentType = PaymentType.AUTO_DEBIT;
        } else if (flowTransBean.getWorkFlowBean().isPreAuth()) {
            paymentType = PaymentType.PAY_CONFIRM;
        } else {
            if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || ERequestType.SUBSCRIBE.getType().equals(flowTransBean.getWorkFlowBean().getRequestType())
                    || ERequestType.NATIVE_MF_SIP.getType().equals(flowTransBean.getWorkFlowBean().getRequestType())) {
                paymentType = PaymentType.RECUR;
            }
        }
        paymentBizInfo.setPaymentType(paymentType);
    }

    private static void populateFlowInPaymentBizInfo(Map<String, String> riskExtendInfo, PaymentBizInfo paymentBizData) {
        try {
            if (riskExtendInfo != null) {
                BusinessFlow flow = BusinessFlow.getBusinessFlowBy1Name(riskExtendInfo.get(BizConstant.FLOW));
                paymentBizData.setBusinessFlow(flow);
            }
        } catch (Exception e) {
            LOGGER.error("DATA_ENRICHMENT : error while populating business flow :{}", e.getMessage());
        }
    }

    private static void populatePaymentFlowInPaymentBizInfo(WorkFlowRequestBean workFlowRequestBean,
            PaymentBizInfo paymentBizData) {
        PaymentFlow paymentFlow = PaymentFlow.DEFAULT;
        try {
            PaymentRequestBean paymentRequestBean = workFlowRequestBean.getPaymentRequestBean();

            if (StringUtils.equals(paymentRequestBean.getIsAddMoney(), "1")) {
                paymentFlow = PaymentFlow.ADD_N_PAY;
            } else if (StringUtils.equals(paymentRequestBean.getIsAddMoney(), "0")) {
                paymentFlow = PaymentFlow.HYBRID_PAY;
            }
        } catch (Exception e) {
            LOGGER.error("DATA_ENRICHMENT : error while populating payment flow :{}", e.getMessage());
        }

        paymentBizData.setPaymentFlow(paymentFlow);
    }

    private static void populatePaymentAuthenticationFlowInPaymentBizInfo(WorkFlowTransactionBean flowTransactionBean,
            PaymentBizInfo paymentBizInfo) {
        try {
            String payMode = flowTransactionBean.getWorkFlowBean().getPaymentTypeId();
            if (StringUtils.isBlank(payMode)) {
                return;
            }
            List<PaymentAuthenticationFlow> paymentAuthenticationFlows = new ArrayList<>();
            PaymentAuthenticationFlow paymentAuthFlow = null;
            PayMethod payMethod = PayMethod.getPayMethodByOldName(payMode);

            switch (payMode) {
            case BizConstant.DataEnrichmentKey.PAYMODE_UPI:
                paymentAuthFlow = new PaymentAuthenticationFlow();
                paymentAuthFlow.setPayMethod(payMethod);
                if (isUPICollect(flowTransactionBean)) {
                    paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_COLLECT);
                } else if (isUPIIntent(flowTransactionBean)) {
                    paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_INTENT);
                } else {
                    paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_PUSH);
                }
                break;
            case BizConstant.DataEnrichmentKey.PAYMODE_NB:
                paymentAuthFlow = new PaymentAuthenticationFlow();
                paymentAuthFlow.setPayMethod(payMethod);
                paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_REDIRECT_TO_BANK);
                if (EPayMethod.PPBL.getOldName().equals(flowTransactionBean.getWorkFlowBean().getBankCode())) {
                    paymentAuthFlow.setPayMethod(PayMethod.PPBL);
                    if (BizConstant.ENHANCED_CASHIER_FLOW.equals(flowTransactionBean.getWorkFlowBean().getWorkFlow())
                            || BizConstant.CHECKOUT.equals(flowTransactionBean.getWorkFlowBean().getWorkFlow())
                            || BizConstant.PCF_FLOW.equals(flowTransactionBean.getWorkFlowBean().getWorkFlow())) {
                        paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_PASSCODE_ON_PG_PAGE);
                    } else {
                        paymentAuthFlow.setAuthFlow(BizConstant.AUTH_FLOW_PASSCODE_ON_PAYTM_APP);
                    }
                }
                break;
            case BizConstant.DataEnrichmentKey.PAYMODE_PPI:
            case BizConstant.DataEnrichmentKey.PAYMODE_PAYTM_DIGITAL_CREDIT:
                paymentAuthFlow = new PaymentAuthenticationFlow();
                paymentAuthFlow.setPayMethod(payMethod);
                paymentAuthFlow.setAuthFlow(flowTransactionBean.getWorkFlowBean().getWalletPostpaidAuthorizationMode());
                break;
            }
            if (paymentAuthFlow != null) {
                paymentAuthenticationFlows.add(paymentAuthFlow);
            }
            paymentBizInfo.setPaymentAuthenticationFlows(paymentAuthenticationFlows);
        } catch (Exception e) {
            LOGGER.error("DATA_ENRICHMENT : error while populating authentication flow :{}", e.getMessage());
        }
    }

    private static void populatePaymentMethodDetailsInPaymentBizInfo(WorkFlowTransactionBean workFlowTransactionBean,
            PaymentBizInfo paymentBizInfo) {
        List<PayMethodDetail> payMethodDetails = new ArrayList<>();
        PayMethodDetail payMethodDetail = null;
        Map<String, String> payMethodDetailsMap = new HashMap<>();
        try {
            String payMode = workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId();
            PayMethod payMethod = PayMethod.getPayMethodByOldName(payMode);
            if (payMethod != null
                    && (PaymentTypeIdEnum.CC.value.equals(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId()))
                    || (PaymentTypeIdEnum.DC.value.equals(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId()))
                    || (PaymentTypeIdEnum.EMI.value
                            .equals(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId()))) {
                payMethodDetail = new PayMethodDetail();
                payMethodDetail.setPayMethod(payMethod.getMethod());
                if (workFlowTransactionBean.getSavedCard() != null) {
                    boolean isSaveCard = workFlowTransactionBean.getWorkFlowBean().getIsSavedCard();
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.CARD_NETOWK, workFlowTransactionBean
                            .getWorkFlowBean().getCardScheme());
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.CARD_ISSAVECARD, String.valueOf(isSaveCard));
                }
                boolean isInternational = workFlowTransactionBean.getWorkFlowBean().isInternationalCard();

                if (isInternational)
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.IS_INTERNATIONAL_CARD, "true");
                else
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.IS_INTERNATIONAL_CARD, "false");

                Map<String, String> channelInfo = workFlowTransactionBean.getWorkFlowBean().getChannelInfo();
                if (channelInfo != null) {
                    String planID = channelInfo.get(BizConstant.DataEnrichmentKey.EMI_PLANID);
                    String tenure = channelInfo.get(BizConstant.DataEnrichmentKey.EMI_TENUREID);
                    String interestRate = channelInfo.get(BizConstant.DataEnrichmentKey.EMI_INTEREST);
                    if (workFlowTransactionBean.getWorkFlowBean().getEmiSubventionOfferCheckoutReqData() != null) {
                        ValidateResponse validateResponse = workFlowTransactionBean.getWorkFlowBean()
                                .getEmiSubventionOfferCheckoutReqData();
                        if (validateResponse != null) {

                            planID = validateResponse.getPlanId();
                            tenure = String.valueOf(validateResponse.getInterval());
                            String gratificationType = "";
                            String gratificationDiscount = "";
                            for (Gratification gratification : validateResponse.getGratifications()) {
                                if (gratification.getType().equals(GratificationType.DISCOUNT)) {
                                    gratificationType = BizConstant.DataEnrichmentKey.EMI_DISCOUNT;
                                    gratificationDiscount = String.valueOf(gratification.getValue());
                                }
                                if (gratification.getType().equals(GratificationType.CASHBACK)) {
                                    gratificationType = BizConstant.DataEnrichmentKey.EMI_CASHBACK;
                                    gratificationDiscount = String.valueOf(gratification.getValue());
                                }
                            }
                            payMethodDetailsMap.put(gratificationType, gratificationDiscount);
                        }
                    }
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.EMI_PLAN_ID, planID);
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.EMI_INTREST_RATE, interestRate);
                    payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.EMI_TENURE, tenure);
                }
            } else if (payMethod != null
                    && PaymentTypeIdEnum.UPI.value.equals(workFlowTransactionBean.getWorkFlowBean().getPaymentTypeId())) {
                payMethodDetail = new PayMethodDetail();
                payMethodDetail.setPayMethod(payMethod.getMethod());
                String pspApp = workFlowTransactionBean.getWorkFlowBean().getPspApp();
                String virtualPaymentAdd = workFlowTransactionBean.getWorkFlowBean().getVirtualPaymentAddress();
                if (StringUtils.isNotEmpty(virtualPaymentAdd)) {
                    if (StringUtils.isNotEmpty(virtualPaymentAdd)) {
                        String[] virtualAdd = virtualPaymentAdd.split(Pattern.quote("@"));
                        if (virtualAdd.length > 0) {
                            String vpaChannel = virtualAdd[1];
                            payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.VIRTUAL_PAYMENT_ADDRESS, vpaChannel);
                        }
                    }
                }
                payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.UPI_PSPAPP, pspApp);
                payMethodDetailsMap.put(BizConstant.BANK_CODE, workFlowTransactionBean.getWorkFlowBean().getBankCode());
                payMethodDetailsMap.put(BizConstant.DataEnrichmentKey.UPI_BANK_NAME, workFlowTransactionBean
                        .getWorkFlowBean().getBankName());
            }
            if (payMethodDetail != null) {
                payMethodDetail.setPayMethodDetails(payMethodDetailsMap);
                payMethodDetails.add(payMethodDetail);
            }
            paymentBizInfo.setPayMethodDetails(payMethodDetails);

        } catch (Exception e) {
            LOGGER.error("DATA_ENRICHMENT : In ApliPayRequestUtils.setPaymentMethodDetails() {} ", e.getMessage());
        }
    }

    private static boolean isUPICollect(WorkFlowTransactionBean flowTransBean) {
        return (flowTransBean.getWorkFlowBean() != null
                && PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                && !flowTransBean.getWorkFlowBean().isUpiPushFlow() && !flowTransBean.getWorkFlowBean()
                .isNativeDeepLinkReqd());
    }

    private static boolean isUPIIntent(WorkFlowTransactionBean flowTransBean) {
        return (flowTransBean.getWorkFlowBean() != null
                && PaymentTypeIdEnum.UPI.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())
                && !flowTransBean.getWorkFlowBean().isUpiPushFlow() && flowTransBean.getWorkFlowBean()
                .isNativeDeepLinkReqd());
    }

    public static String getAdditionalInfoValueForKey(String additionalInfo, String key) {
        String value = null;
        if (StringUtils.isNotEmpty(additionalInfo)) {
            String[] additionalInfoKeyValArray = additionalInfo.split(Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String keyVal : additionalInfoKeyValArray) {
                String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                if (keyValSplit.length == 2) {
                    if (StringUtils.equals(keyValSplit[0].trim(), key)) {
                        value = keyValSplit[1].trim();
                        break;
                    }
                }
            }
        }
        return value;
    }
}