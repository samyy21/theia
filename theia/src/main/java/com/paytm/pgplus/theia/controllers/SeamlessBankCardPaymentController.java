/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.DirectBankcardPaymentRedirectionKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.*;
import com.paytm.pgplus.theia.services.impl.SeamlessPaymentServiceImpl;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_TRANSACTION_ID_ENABLE;

/**
 * @author amitdubey
 * @date Dec 1, 2016
 */
@Controller
public class SeamlessBankCardPaymentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessBankCardPaymentController.class);

    @Autowired
    @Qualifier(value = "nativeRequestPaymentServiceImpl")
    private IPaymentService nativeRequestPaymentServiceImpl;

    @Autowired
    @Qualifier(value = "seamlessPaymentService")
    private SeamlessPaymentServiceImpl seamlessPaymentService;

    @Autowired
    ISeamlessBankCardService seamlessBankCardPaymentServiceImpl;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    private IWorkFlow nativeRetryPaymentFlowService;

    @Autowired
    @Qualifier("bizInternalPaymentRetry")
    private InternalPaymentRetryService internalPaymentRetryService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("seamlessDirectBankCardServiceImpl")
    private ISeamlessDirectBankCardService seamlessDirectBankCardService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private ValidationService validationService;

    private String htmlFileTemplate;
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @PostConstruct
    public void init() throws IOException {
        InputStream htmlFile = SeamlessBankCardPaymentController.class.getClassLoader().getResourceAsStream(
                "templates/directbankpaymentredirect.html");
        htmlFileTemplate = getFileContent(htmlFile);
    }

    private String getFileContent(InputStream inStrem) throws IOException {
        int ch;
        StringBuilder strContent = new StringBuilder();
        while ((ch = inStrem.read()) != -1) {
            strContent.append((char) ch);
        }
        return strContent.toString();
    }

    @RequestMapping(value = "directBankCardPayment", method = RequestMethod.POST)
    public void generateSessionAndRedirect(HttpServletRequest request, HttpServletResponse response) {
        processDirectBankCardPayment(request, response);
        return;
    }

    @RequestMapping(value = "v1/directBankCardPayment", method = RequestMethod.POST)
    public void generateSessionAndRedirectV1(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processBankCardPayment(request, response);
        return;
    }

    private void processDirectBankCardPayment(HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        try {
            String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
            String transactionId = request.getParameter(ExtraConstants.TRANS_ID);

            if (StringUtils.isBlank(cashierRequestId)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid Cashier Request Id : " + cashierRequestId);
            }
            if (StringUtils.isBlank(transactionId) || !StringUtils.isNumeric(transactionId)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid Transaction Id : " + transactionId);
            }
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_TRANSACTION_ID_ENABLE, false)
                    && !validationService.validateTransactionId(transactionId)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid Transaction Id : " + transactionId);
            }
            String url = ConfigurationUtil.getProperty("logoutUrl");
            if (StringUtils.isBlank(url)) {
                throw new TheiaServiceException(
                        "CRITICAL_ERROR : Invalid Redirection URL Defined(picked from logoutUrl) : " + url);
            }
            SeamlessBankCardPayRequest seamlessBankCardPayRequest = seamlessBankCardPaymentServiceImpl
                    .getSeamlessBankCardPayRequest(cashierRequestId);
            url = url.replace("logout", "directBankCardPaymentRedirect");
            String mid = seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                    .get(ExtendedInfoPay.PAYTM_MERCHANT_ID);

            if (seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                    .get(TheiaConstant.RetryConstants.ACTUAL_MID) != null) {
                mid = seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                        .get(TheiaConstant.RetryConstants.ACTUAL_MID);
            }
            String orderId = seamlessBankCardPayRequest.getPaymentRequest().getExtendInfo()
                    .get(ExtendedInfoPay.MERCHANT_TRANS_ID);
            LOGGER.info(
                    "Will redirect to URL : {} with MID : {} , Order ID : {} , Cashier Request ID : {} , Trans ID : {} ",
                    url, mid, orderId, cashierRequestId, transactionId);
            String redirectHtml = htmlFileTemplate.replace(DirectBankcardPaymentRedirectionKeys.URL, url)
                    .replace(DirectBankcardPaymentRedirectionKeys.CASHIER_REQUEST_ID, cashierRequestId)
                    .replace(DirectBankcardPaymentRedirectionKeys.MID, mid)
                    .replace(DirectBankcardPaymentRedirectionKeys.ORDER_ID, orderId)
                    .replace(DirectBankcardPaymentRedirectionKeys.TRANS_ID, transactionId);
            response.setContentType("text/html");
            response.getOutputStream().write(redirectHtml.getBytes());
            // Do not Write Anything after this line.
        } catch (Exception ex) {
            throw new TheiaControllerException("Unable to process the direct bank payment", ex);
        } finally {
            LOGGER.info("Total time taken to process the {} is {} ms", "generateSesionAndRedirect",
                    System.currentTimeMillis() - startTime);
        }
    }

    @RequestMapping(value = "directBankCardPaymentRedirect", method = RequestMethod.POST)
    public String doPayment(HttpServletRequest request, Model model, Locale locale, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();

        String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
        String transactionId = request.getParameter(ExtraConstants.TRANS_ID);

        if (StringUtils.isBlank(cashierRequestId)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid Cashier Request Id : " + cashierRequestId);
        }

        if (StringUtils.isBlank(transactionId) || !StringUtils.isNumeric(transactionId)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid Transaction Id : " + transactionId);
        }

        try {

            String bankPage = seamlessBankCardPaymentServiceImpl.doSeamlessBankCardPayment(cashierRequestId,
                    transactionId);

            if (StringUtils.isBlank(bankPage)) {
                throw new TheiaServiceException("CRITICAL_ERROR : No bank form found for cashier request id : "
                        + cashierRequestId);
            }

            LOGGER.info("Bank form received for cashierRequestId : {}", cashierRequestId);

            theiaSessionDataService.setRedirectPageInSession(request, bankPage, true);
            return theiaViewResolverService.returnForwarderPage();
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : Unable to process direct bank card payment ", e);
        } finally {
            LOGGER.info("Total time taken for Controller {} is {} ms", "SeamlessBankCardPaymentController",
                    System.currentTimeMillis() - startTime);
        }
        return theiaViewResolverService.returnOOPSPage(request);
    }

    private void processBankCardPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Direct Bank Received Request {}", request);
        String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);

        if (StringUtils.isBlank(cashierRequestId)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid Cashier Request Id : " + cashierRequestId);
        }

        LOGGER.info("cashierRequestId : {}", cashierRequestId);
        WorkFlowRequestBean flowRequestBean = null;
        try {
            flowRequestBean = (WorkFlowRequestBean) theiaSessionRedisUtil
                    .get(com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty("directBankRequestBeanKey")
                            + cashierRequestId);
            LOGGER.info("flowRequestBean : {}", flowRequestBean);
            if (flowRequestBean != null) {
                if (StringUtils.isNotBlank(flowRequestBean.getPaytmMID())) {
                    MDC.put(TheiaConstant.RequestParams.MID, flowRequestBean.getPaytmMID());
                }
                if (StringUtils.isNotBlank(flowRequestBean.getOrderID())) {
                    MDC.put(TheiaConstant.RequestParams.ORDER_ID, flowRequestBean.getOrderID());
                }
            }

            if (flowRequestBean == null) {
                SeamlessBankCardPayRequest seamlessBankCardPayRequest = new SeamlessBankCardPayRequest(cashierRequestId);
                LOGGER.info("seamlessBankCardPayRequest.getRedisKey() : {}", seamlessBankCardPayRequest.getRedisKey());
                seamlessBankCardPayRequest = (SeamlessBankCardPayRequest) theiaTransactionalRedisUtil
                        .get(seamlessBankCardPayRequest.getRedisKey());

                LOGGER.info("seamlessBankCardPayRequest : {}", seamlessBankCardPayRequest);

                if (seamlessBankCardPayRequest != null) {
                    LOGGER.info("DirectBank request for Default flow");
                    processDirectBankCardPayment(request, response);
                    return;
                } else {
                    LOGGER.error(" DIRECT_BANK_REQUEST_BEAN or DIRECT_BANK_CARD_PAYMENT_ both key not found");
                    throw new TheiaControllerException("Unable to process the direct bank payment");
                }
            } else {
                // RedisClientService.getInstance().get(flowRequestBean.getTxnToken());
                EnhanceCashierPageCachePayload value = (EnhanceCashierPageCachePayload) nativeSessionUtil
                        .getKey(flowRequestBean.getPaytmMID() + "_" + flowRequestBean.getOrderID()
                                + "_EnhancedCashierPagePayload");

                /*
                 * This nativeJsonRequest is set to false so that, instaProxy
                 * does not give bankForm again
                 */
                flowRequestBean.setNativeJsonRequest(false);
                flowRequestBean.setEnhancedCashierPaymentRequest(false);
                flowRequestBean.setForceDirectChannel("false");

                if (value == null) {
                    seamlessDirectBankCardService.processNativeRequest(request, response, flowRequestBean);
                    return;
                } else {
                    seamlessDirectBankCardService.processNativeEnhanceRequest(request, response, flowRequestBean);
                    return;
                }

            }
        } catch (Exception e) {
            LOGGER.error("Error in processBankCardPayment : ", e);
            request.getRequestDispatcher(VIEW_BASE + "error" + ".jsp").forward(request, response);
        } finally {
            LOGGER.info("Total time taken for Controller {} is {} ms", "SeamlessBankCardPaymentController",
                    System.currentTimeMillis() - startTime);
        }

    }
}