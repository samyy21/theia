package com.paytm.pgplus.theia.s2s.utils;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DeepLink;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankRedirectionDetail;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kartik
 * @date 07-Apr-2018
 */
@SuppressWarnings("Duplicates")
@Component
public class BankFormParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankFormParser.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BankFormParser.class);

    private static final String ACTION_URL = "action";
    private static final String REQUEST_METHOD = "method";
    private static final String INSTA_PAGE_URL = "instaproxy/bankresponse";

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    public BankRedirectionDetail parseHTMLFormNative(String webFormContext) {
        Document doc = Jsoup.parse(webFormContext);
        Element element = doc.select("FORM").first();

        BankRedirectionDetail bankRequest = new BankRedirectionDetail();

        if (element == null) {
            BankForm bankForm = new Gson().fromJson(webFormContext, BankForm.class);
            bankRequest.setCallbackUrl(bankForm.getRedirectForm().getActionUrl());
            bankRequest.setMethod(bankForm.getRedirectForm().getMethod());
            bankRequest.setContent(bankForm.getRedirectForm().getContent());
            bankRequest.setBankForm(bankForm);
            return bankRequest;
        }
        EXT_LOGGER.customInfo("bankform element non null");
        Attributes attributes = element.attributes();
        for (Attribute attr : attributes) {
            if (ACTION_URL.equalsIgnoreCase(attr.getKey())) {
                bankRequest.setCallbackUrl(attr.getValue());
            }
            if (REQUEST_METHOD.equalsIgnoreCase(attr.getKey())) {
                bankRequest.setMethod(attr.getValue().toUpperCase());
            }
        }
        Map<String, String> params = new HashMap<String, String>();
        Iterator<Element> it = element.children().iterator();
        while (it.hasNext()) {
            Element inputTag = it.next();
            if (StringUtils.isNotBlank(inputTag.attr("name"))) {
                params.put(inputTag.attr("name"), inputTag.attr("value"));
            }
        }
        bankRequest.setContent(params);
        if (StringUtils.containsIgnoreCase(bankRequest.getCallbackUrl(), INSTA_PAGE_URL)) {
            // This is the case of Insta bank forms for Enhanced
            bankRequest.setDirectHtml(webFormContext);
        }
        return bankRequest;
    }

    public com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail parseHTMLForm(
            final PaymentRequestBean paymentRequestBean, final WorkFlowResponseBean workFlowResponseBean) {

        final String paymentTypeId = paymentRequestBean.getPaymentTypeId();

        com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail bankRequest = null;

        if (StringUtils.isNoneBlank(paymentTypeId) && paymentTypeId.equals("UPI")) {

            bankRequest = createBankRedirectionDetailForUPI(paymentRequestBean, workFlowResponseBean);

        } else {

            bankRequest = parseHTMLForm(workFlowResponseBean.getQueryPaymentStatus().getWebFormContext());
        }

        return bankRequest;
    }

    public com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail parseHTMLForm(String webFormContext) {
        Document doc = Jsoup.parse(webFormContext);
        Element element = doc.select("FORM").first();
        Attributes attributes = element.attributes();
        com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail bankRequest = new com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail();
        for (Attribute attr : attributes) {
            if (ACTION_URL.equalsIgnoreCase(attr.getKey())) {
                bankRequest.setUrl(attr.getValue());
            }
            if (REQUEST_METHOD.equalsIgnoreCase(attr.getKey())) {
                bankRequest.setMethod(attr.getValue().toUpperCase());
            }
        }
        Map<String, String> params = new HashMap<String, String>();
        Iterator<Element> it = element.children().iterator();
        while (it.hasNext()) {
            Element inputTag = it.next();
            params.put(inputTag.attr("name"), inputTag.attr("value"));
        }
        bankRequest.setContent(params);
        return bankRequest;
    }

    public com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail createBankRedirectionDetailForUPI(
            final PaymentRequestBean paymentRequestBean, final WorkFlowResponseBean workFlowResponseBean) {

        com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail bankRequest = new com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail();

        String merchantVPA = null;
        Map<String, String> contentMap = new HashMap<>();
        String acquirementId = workFlowResponseBean.getTransID();
        String cashierRequestId = workFlowResponseBean.getCashierRequestId();
        String mid = paymentRequestBean.getMid();
        String orderId = paymentRequestBean.getOrderId();

        contentMap.put("acquirementId", acquirementId);
        contentMap.put("cashierRequestId", cashierRequestId);
        contentMap.put("mid", mid);
        contentMap.put("orderId", orderId);

        try {

            String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
            if (paymentRequestBean.isDeepLinkFromInsta()) {
                BankForm bankForm = new Gson().fromJson(webFormContext, BankForm.class);
                DeepLink deepLink = bankForm != null ? bankForm.getDeepLink() : null;
                contentMap.put("deepLink",
                        (deepLink != null && StringUtils.isNotBlank(deepLink.getUrl())) ? deepLink.getUrl() : "");
                bankRequest.setContent(contentMap);
                return bankRequest;
            } else {
                MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext,
                        MerchantVpaTxnInfo.class);

                merchantVPA = merchantVpaTxnInfo.getVpa();
            }

        } catch (FacadeCheckedException facadeCheckedException) {
            LOGGER.error("Exception occurred while fetching merchant VPA, stack trace {}", facadeCheckedException);
            merchantVPA = "paytm@icici";
        }

        String txnAmount = UpiInfoSessionUtil.getTransactionAmount(workFlowResponseBean, paymentRequestBean,
                workFlowResponseBean.getWorkFlowRequestBean());
        String statusInterval = ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL);
        String statusTimeout = ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_TIMEOUT);
        String userVpa = paymentRequestBean.getPaymentDetails();
        String payMode = "UPI";
        String isPaytmVPA = String.valueOf(UpiInfoSessionUtil.isPaytmVpa(workFlowResponseBean.getWorkFlowRequestBean(),
                paymentRequestBean.getVirtualPaymentAddr()));

        contentMap.put("txnAmount", txnAmount);
        contentMap.put("statusInterval", statusInterval);
        contentMap.put("statusTimeout", statusTimeout);
        contentMap.put("userVpa", userVpa);
        contentMap.put("payMode", payMode);
        contentMap.put("isPaytmVPA", isPaytmVPA);
        contentMap.put("merchantVPA", merchantVPA);

        String key = new StringBuilder("UPIPollPage").append("_").append(mid).append("_").append(orderId).toString();

        theiaTransactionalRedisUtil.set(key, contentMap, 120);

        Map<String, String> keyMap = new HashMap<>();

        keyMap.put("cacheKey", key);

        bankRequest.setContent(keyMap);

        return bankRequest;
    }

    public void checkIfDirectBankPage(String bankform, String cashierRequestId, WorkFlowRequestBean workFlowRequestBean) {
        Document doc = Jsoup.parse(bankform);
        Element element = doc.select("FORM").first();

        if (element == null) {
            // LOGGER.info("Received direct bank page");
            if (workFlowRequestBean != null
                    && !workFlowRequestBean.isDirectBankCardFlow()
                    && (PaymentTypeIdEnum.CC.value.equals(workFlowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                            .equals(workFlowRequestBean.getPaymentTypeId()))) {
                LOGGER.info("Saving Data For DirectBankCard Flow for OTP Inject");
                theiaSessionRedisUtil.set(
                        com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty("directBankRequestBeanKey")
                                + cashierRequestId, workFlowRequestBean, Long
                                .parseLong(com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty(
                                        "directBankRedisTimeOut", "300")));
            } else {
                LOGGER.info("Not setting DirectBankCard Flow data in redis");
            }
        }
    }
}
