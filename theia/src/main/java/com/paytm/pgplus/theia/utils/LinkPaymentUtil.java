package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.link.LinkDetailRequestBody;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.model.link.LinkPaymentRiskInfo;
import com.paytm.pgplus.common.model.link.PaymentFormDetails;
import com.paytm.pgplus.enums.ResultStatus;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.impl.LinkService;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.LINK_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.LINK_BASED_PAYMENT;

/**
 * Created by: satyamsinghrajput at 10/7/19
 */
@Component
public class LinkPaymentUtil {

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    private LinkService linkService;

    @Autowired
    IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkPaymentUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(LinkPaymentUtil.class);

    private static final String LINK_PARTIAL_PAYMENT_FEATURE = "linkPartialPayments";

    public void setpageDetailsResponseForLinkBasedPayment(PageDetailsResponse pageDetailsResponse,
            PaymentRequestBean requestData, String errorMessage, String errorCode) {
        // errorMEssage and errorCode to be discussed

        Map<String, String> content = null;

        content = setRequestParamsForLinkPayments(requestData.getRequestType(), requestData.getMid(), errorMessage,
                errorCode, requestData.getTxnAmount());
        pageDetailsResponse.setHtmlPage(null);
        pageDetailsResponse.setData(content);
        LOGGER.info("Got pageDetailsResponse in Exception: {}", pageDetailsResponse.getData());
    }

    private Map<String, String> setRequestParamsForLinkPayments(String requestType, String mid, String errorMessage,
            String errorCode, String amount) {
        Map<String, String> data = new HashMap<>();

        data.put(TheiaConstant.ExtraConstants.REQUEST_TYPE, requestType);
        data.put(LINK_BASED_PAYMENT, "true");
        data.put(PAYMENT_STATUS, PaymentStatus.FAIL.name());
        Date date = new Date();
        data.put(TXN_DATE, LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        // data.put(TRANSACTION_ID, paymentResponse.getTransId());
        data.put(TXN_AMOUNT, amount);
        data.put(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
        if (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(requestType)) {
            LOGGER.info("setting linkType invoice");
            data.put(TheiaConstant.ResponseConstants.LINK_TYPE, TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
        }

        MerchantBussinessLogoInfo merchantBussinessLogoInfo = null;
        try {
            merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMidV2(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantBussinessLogoInfo v2 :: {}", merchantBussinessLogoInfo);
        } catch (MappingServiceClientException e) {
            LOGGER.info("Got Exception while fetching merchant logo {}", e);
        }
        if (merchantBussinessLogoInfo != null) {
            data.put(MERCHANT_NAME, merchantBussinessLogoInfo.getMerchantDisplayName());
            data.put(MERCHANT_IMAGE, merchantBussinessLogoInfo.getMerchantImageName());
        }

        // Set error-message using response-code-utility
        String errorMsg = StringUtils.isNotBlank(errorCode) ? responseCodeUtil.getResponseMsg(responseCodeUtil
                .getResponseCodeDetails(errorCode, null, PaymentStatus.FAIL.name())) : "";

        data.put(ERROR_MESSAGE, StringUtils.isNotBlank(errorMsg) ? errorMsg : errorMessage);
        data.put(ERROR_CODE, errorCode);

        return data;
    }

    public static boolean isLinkBasedPayment(HttpServletRequest servletRequest,
            InitiateTransactionRequestBody orderDetail) {
        // check for requesttype ,linkid in request parameter , if null then
        // check in orderdetail
        try {
            String reqType = servletRequest.getParameter(REQUEST_TYPE) == null ? (orderDetail == null ? null
                    : orderDetail.getRequestType()) : servletRequest.getParameter(REQUEST_TYPE);
            String linkId = servletRequest.getParameter(LINK_ID) == null ? (orderDetail == null ? null : (orderDetail
                    .getLinkDetailsData() == null ? null : orderDetail.getLinkDetailsData().getLinkId()))
                    : servletRequest.getParameter(LINK_ID);
            LOGGER.info("Request type is :{} , Link ID is : {}", reqType, linkId);
            return (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(reqType)
                    || ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(reqType) || StringUtils
                        .isNotBlank(linkId));
        } catch (Exception ex) {
            LOGGER.error("error occured while checking for link based payment ", ex);
        }
        // default to false
        return false;

    }

    public String validateLinkAndSetCallBackURL(PaymentRequestBean paymentRequestData, boolean isOffLineFlow) {

        LinkDetailResponseBody linkDetailResponseBody = getLinkDetailResponse(paymentRequestData, isOffLineFlow);

        // Validate request and Set callBackURL for Link Payment
        if (linkDetailResponseBody != null
                && ResultStatus.SUCCESS.getValue().equals(linkDetailResponseBody.getResultInfo().getResultStatus())) {
            LOGGER.info(
                    "Details from Link :Amount {}, Description {}, Details from PaymentRequest :Amount {}, Description {}, paymentFormId {}",
                    linkDetailResponseBody.getAmount(), linkDetailResponseBody.getLinkDescription(),
                    paymentRequestData.getTxnAmount(), paymentRequestData.getLinkDescription(),
                    linkDetailResponseBody.getPaymentFormId());
            if (iPgpFf4jClient.checkWithdefault(LINK_PARTIAL_PAYMENT_FEATURE, new HashMap<>(), true)) {
                if (linkDetailResponseBody.getAmount() != null
                        && Double.parseDouble(paymentRequestData.getTxnAmount()) != linkDetailResponseBody.getAmount()) {
                    LOGGER.error("Link amount not matched");
                    return "Link amount not matched";
                }
            }
            if (StringUtils.isNotBlank(paymentRequestData.getLinkDescription())
                    && !paymentRequestData.getLinkDescription().equals(linkDetailResponseBody.getLinkDescription())) {
                LOGGER.error("Link description not matched");
                return "Link description not matched";
            }
            String callBackURL = linkDetailResponseBody.getStatusCallBackURL();
            if (StringUtils.isNotBlank(callBackURL)) {
                paymentRequestData.setPeonURL(callBackURL);
                LOGGER.debug("Setting callBackURL for link :{}", callBackURL);
            }
            paymentRequestData.setLinkNotes(linkDetailResponseBody.getLinkNotes());

            if (StringUtils.isNotEmpty(linkDetailResponseBody.getPaymentFormId())) {
                LOGGER.debug("Setting PaymentFormId for link in payment request bean : {}",
                        linkDetailResponseBody.getPaymentFormId());
                paymentRequestData.setPaymentFormId(linkDetailResponseBody.getPaymentFormId());
            }
            if (StringUtils.isNotBlank(linkDetailResponseBody.getResellerId())) {
                LOGGER.debug("Setting ResellerID {} for link in PaymentRequestBean",
                        linkDetailResponseBody.getResellerId());
                paymentRequestData.setResellerId(RESELLER_ID_PREFIX + linkDetailResponseBody.getResellerId());
            }
            if (StringUtils.isNotBlank(linkDetailResponseBody.getResellerName())) {
                LOGGER.debug("Setting ResellerName {} for link in PaymentRequestBean",
                        linkDetailResponseBody.getResellerName());
                paymentRequestData.setResellerName(linkDetailResponseBody.getResellerName());
            }
            if (StringUtils.isNotBlank(linkDetailResponseBody.getMerchantReferenceId())) {
                LOGGER.debug("Setting MerchantReferenceId {} for link in PaymentRequestBean",
                        linkDetailResponseBody.getMerchantReferenceId());
                paymentRequestData.setMerchantLinkRefId(linkDetailResponseBody.getMerchantReferenceId());
            }
            if (MapUtils.isNotEmpty(linkDetailResponseBody.getExtendInfo())) {
                LOGGER.debug("Setting ExtendInfo for link in PaymentRequestBean");
                paymentRequestData.setLinkOrderExtendInfo(linkDetailResponseBody.getExtendInfo());
            }
            paymentRequestData.setDisplayWarningMessageForLink(linkDetailResponseBody.isDisplayWarningMessage());
            if (linkDetailResponseBody.getSplitSettlementInfo() != null) {
                try {
                    paymentRequestData.setSplitSettlementInfoData(JsonMapper.convertValue(
                            linkDetailResponseBody.getSplitSettlementInfo(), SplitSettlementInfoData.class));
                } catch (Exception e) {
                    LOGGER.error("Exception while parsing split settlement info from linkk details {}", e);
                }
            }
        } else {
            if (linkDetailResponseBody != null && linkDetailResponseBody.getResultInfo() != null) {
                return linkDetailResponseBody.getResultInfo().getResultMsg();
            }
        }
        return "";
    }

    public LinkDetailResponseBody getLinkDetailCachedResponse(PaymentRequestBean paymentRequestData) {
        LinkDetailResponseBody cachedLinkDetailResponseBody = (LinkDetailResponseBody) nativeSessionUtil
                .getKey(getCacheKeyNameForLinkDetail(paymentRequestData));
        if (cachedLinkDetailResponseBody != null) {
            return cachedLinkDetailResponseBody;
        }
        return null;
    }

    public LinkDetailResponseBody getLinkDetailCachedResponse(HttpServletRequest request) {
        LinkDetailResponseBody cachedLinkDetailResponseBody = (LinkDetailResponseBody) nativeSessionUtil
                .getKey(getCacheKeyNameForLinkDetail(request));
        if (cachedLinkDetailResponseBody != null) {
            return cachedLinkDetailResponseBody;
        }
        return null;
    }

    public void addLinkDetailsInPaymentRequestBeanInPayment(PaymentRequestBean paymentRequestData) {

        LinkDetailResponseBody linkDetailResponseBody = paymentRequestData.getLinkDetailsData();

        // Validate request and Set callBackURL for Link Payment
        LOGGER.info(
                "addLinkDetailsInPaymentRequestBeanInMFAndSTPayment | Details from Link :Amount {}, Description {}, Details from PaymentRequest :Amount {}, Description {}, paymentFormId {}",
                linkDetailResponseBody.getAmount(), linkDetailResponseBody.getLinkDescription(),
                paymentRequestData.getTxnAmount(), paymentRequestData.getLinkDescription(),
                linkDetailResponseBody.getPaymentFormId());

        String callBackURL = linkDetailResponseBody.getStatusCallBackURL();
        if (StringUtils.isNotBlank(callBackURL)) {
            paymentRequestData.setPeonURL(callBackURL);
            LOGGER.info("addLinkDetailsInPaymentRequestBeanInMFAndSTPayment|Setting callBackURL for link :{}",
                    callBackURL);
        }
        paymentRequestData.setLinkNotes(linkDetailResponseBody.getLinkNotes());

        if (StringUtils.isNotEmpty(linkDetailResponseBody.getPaymentFormId())) {
            LOGGER.info("Setting PaymentFormId for link in payment request bean : {}",
                    linkDetailResponseBody.getPaymentFormId());
            paymentRequestData.setPaymentFormId(linkDetailResponseBody.getPaymentFormId());
        }
        if (StringUtils.isNotBlank(linkDetailResponseBody.getResellerId())) {
            LOGGER.info("Setting ResellerID {} for link in PaymentRequestBean", linkDetailResponseBody.getResellerId());
            paymentRequestData.setResellerId(RESELLER_ID_PREFIX + linkDetailResponseBody.getResellerId());
        }
        if (StringUtils.isNotBlank(linkDetailResponseBody.getResellerName())) {
            LOGGER.info("Setting ResellerName {} for link in PaymentRequestBean",
                    linkDetailResponseBody.getResellerName());
            paymentRequestData.setResellerName(linkDetailResponseBody.getResellerName());
        }
        if (linkDetailResponseBody.getSplitSettlementInfo() != null) {
            paymentRequestData.setSplitSettlementInfoData(JsonMapper.convertValue(
                    linkDetailResponseBody.getSplitSettlementInfo(), SplitSettlementInfoData.class));
        }
        paymentRequestData.setInvoiceId(linkDetailResponseBody.getInvoiceId());
        paymentRequestData.setLinkId(linkDetailResponseBody.getLinkId());
        paymentRequestData.setLinkName(linkDetailResponseBody.getLinkName());
        paymentRequestData.setLinkDescription(linkDetailResponseBody.getLinkDescription());
        paymentRequestData.setLongUrl(linkDetailResponseBody.getLongUrl());
        paymentRequestData.setShortUrl(linkDetailResponseBody.getShortUrl());
        paymentRequestData.setMerchantLinkRefId(linkDetailResponseBody.getMerchantReferenceId());
        paymentRequestData.setLinkOrderExtendInfo(linkDetailResponseBody.getExtendInfo());
        paymentRequestData.setDisplayWarningMessageForLink(linkDetailResponseBody.isDisplayWarningMessage());

        // LOGGER.info("Setting Link Detail Response in Cache");
        if (StringUtils.isNotEmpty(linkDetailResponseBody.getLinkId())) {
            nativeSessionUtil.setLinkId(paymentRequestData.getTxnToken(), linkDetailResponseBody.getLinkId());
        }

        if (StringUtils.isNotEmpty(linkDetailResponseBody.getInvoiceId())) {
            nativeSessionUtil.setInvoiceId(paymentRequestData.getTxnToken(), linkDetailResponseBody.getInvoiceId());
        }
        setCacheKeyNameForLinkDetail(linkDetailResponseBody, paymentRequestData.getMid(),
                paymentRequestData.getOrderId());
    }

    private String getCacheKeyNameForLinkDetail(PaymentRequestBean paymentRequestData) {
        StringBuilder sb = new StringBuilder();
        sb.append("GET_LINK_DETAIL_MID_").append(paymentRequestData.getMid()).append("_ORDER_ID_")
                .append(paymentRequestData.getOrderId());
        if (StringUtils.isNotBlank(paymentRequestData.getLinkId())) {
            sb.append("_LINK_ID_").append(paymentRequestData.getLinkId());
        }
        if (StringUtils.isNotBlank(paymentRequestData.getInvoiceId())) {
            sb.append("_INVOICE_ID_").append(paymentRequestData.getInvoiceId());
        }
        return sb.toString();
    }

    private String getCacheKeyNameForLinkDetail(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String mid = request.getParameter("MID");
        String orderid = request.getParameter("ORDERID");
        if (orderid == null) {
            orderid = request.getParameter("ORDER_ID");
        }
        String linkid = nativeSessionUtil.getLinkId(nativeSessionUtil.getTxnToken(mid, orderid));
        String invoiceId = nativeSessionUtil.getInvoiceId(nativeSessionUtil.getTxnToken(mid, orderid));
        sb.append("GET_LINK_DETAIL_MID_").append(mid).append("_ORDER_ID_").append(orderid);
        if (StringUtils.isNotBlank(linkid)) {
            sb.append("_LINK_ID_").append(linkid);
        }
        if (StringUtils.isNotBlank(invoiceId)) {
            sb.append("_INVOICE_ID_").append(invoiceId);
        }
        return sb.toString();
    }

    public void cacheGetLinkDetailResponse(PaymentRequestBean paymentRequestData,
            LinkDetailResponseBody linkDetailResponseBody) {

        if (linkDetailResponseBody == null) {
            LOGGER.info("linkDetailResponseBody is null");
            return;
        }

        String paymentFormId = StringUtils.isNotBlank(linkDetailResponseBody.getPaymentFormId()) ? linkDetailResponseBody
                .getPaymentFormId() : paymentRequestData.getPaymentFormId();

        if (StringUtils.isNotBlank(paymentFormId) && linkDetailResponseBody.getPaymentFormDetails() != null) {
            nativeSessionUtil.setKey(paymentFormId, linkDetailResponseBody.getPaymentFormDetails(), 900);
        }

        /*
         * this is done becuase we are already caching above.
         */
        linkDetailResponseBody.setPaymentFormDetails(null);

        nativeSessionUtil.setKey(getCacheKeyNameForLinkDetail(paymentRequestData), linkDetailResponseBody, 900);
    }

    public LinkPaymentRiskInfo getLinkPaymentRiskInfo(PaymentRequestBean paymentRequestData) {
        LinkDetailResponseBody linkDetailResponseBody = getLinkDetailCachedResponse(paymentRequestData);
        if (linkDetailResponseBody == null) {
            return null;
        }
        return linkDetailResponseBody.getLinkPaymentRiskInfo();
    }

    public PaymentFormDetails getPaymentFormDetails(PaymentRequestBean paymentRequestData) {
        if (StringUtils.isNotEmpty(paymentRequestData.getPaymentFormId())) {
            return (PaymentFormDetails) nativeSessionUtil.getKey(paymentRequestData.getPaymentFormId());
        }
        return null;
    }

    public LinkDetailResponseBody getLinkDetailResponse(PaymentRequestBean paymentRequestData, boolean isOffLineFlow) {

        LinkDetailResponseBody cachedLinkDetailResponseBody = getLinkDetailCachedResponse(paymentRequestData);
        if (cachedLinkDetailResponseBody != null) {
            return cachedLinkDetailResponseBody;
        }

        LinkDetailRequestBody linkDetailRequestBody = new LinkDetailRequestBody();
        linkDetailRequestBody.setMid(paymentRequestData.getMid());
        if (StringUtils.isNotBlank(paymentRequestData.getLinkId()))
            linkDetailRequestBody.setLinkId(paymentRequestData.getLinkId());
        if (StringUtils.isNotBlank(paymentRequestData.getInvoiceId()))
            linkDetailRequestBody.setInvoiceId(paymentRequestData.getInvoiceId());
        if (StringUtils.isNotBlank(paymentRequestData.getPaymentFormId()))
            linkDetailRequestBody.setPaymentFormId(paymentRequestData.getPaymentFormId());
        linkDetailRequestBody.setOrderId(paymentRequestData.getOrderId());
        linkDetailRequestBody.setAmount(paymentRequestData.getTxnAmount());

        if (ff4jUtils.isFeatureEnabled(
                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_ENABLED,
                false)
                && (paymentRequestData.getqRCodeInfo() != null || paymentRequestData.isOfflineTxnFlow() || isOffLineFlow)) {
            linkDetailRequestBody.setOfflineFlow(true);
            paymentRequestData.setOfflineTxnFlow(true);
        }

        try {

            LinkDetailResponseBody linkDetailResponseBody = linkService.getLinkDetail(linkDetailRequestBody);
            cacheGetLinkDetailResponse(paymentRequestData, linkDetailResponseBody);
            return linkDetailResponseBody;

        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception in calling getLinkDetail API", e);
        }
        return null;
    }

    public JSONObject requestParamsToJSON(HttpServletRequest req) {
        JSONObject jsonObj = new JSONObject();
        Enumeration en = req.getAttributeNames();
        while (en.hasMoreElements()) {
            String attributeName = en.nextElement().toString();
            jsonObj.put(attributeName, req.getAttribute(attributeName));
        }
        return jsonObj;
    }

    /**
     * This method is related to move links to js checkout and online flow
     *
     * @param linkDetailResponseBody
     * @param mid
     * @param orderid
     */
    public void setCacheKeyNameForLinkDetail(
            com.paytm.pgplus.common.model.link.LinkDetailResponseBody linkDetailResponseBody, String mid, String orderid) {
        StringBuilder sb = new StringBuilder();
        String linkid = linkDetailResponseBody.getLinkId();
        String invoiceId = linkDetailResponseBody.getInvoiceId();
        sb.append("GET_LINK_DETAIL_MID_").append(mid).append("_ORDER_ID_").append(orderid);
        if (StringUtils.isNotBlank(linkid)) {
            sb.append("_LINK_ID_").append(linkid);
        }
        if (StringUtils.isNotBlank(invoiceId)) {
            sb.append("_INVOICE_ID_").append(invoiceId);
        }
        LOGGER.info("redis key for SET_LINK_DETAIL:{}", sb.toString());
        nativeSessionUtil.setKey(sb.toString(), linkDetailResponseBody, 900);
    }
}
