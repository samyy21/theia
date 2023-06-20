//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
//import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
//import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
//import com.paytm.pgplus.common.config.ConfigurationUtil;
//import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.pgproxycommon.exception.KycValidationException;
//import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
//import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
//import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
//import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
//import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.services.IUserKycService;
//import com.paytm.pgplus.theia.utils.MerchantResponseService;
//import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
//import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
//
//@Controller
//@RequestMapping("addMoneyExpress/kyc")
//public class AddMoneyExpressSubmitKYCController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyExpressSubmitKYCController.class);
//    private static final String VIEW_BASE_WITH_JSP = "/WEB-INF/views/jsp/common/addMoneyExpressKycUser.jsp";
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    @Autowired
//    @Qualifier("workFlowHelper")
//    WorkFlowHelper workFlowHelper;
//
//    @Autowired
//    @Qualifier("userKycServiceImpl")
//    private IUserKycService userKycServiceImpl;
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier("merchantResponseService")
//    private MerchantResponseService merchantResponseService;
//
//    @Autowired
//    private TheiaResponseGenerator theiaResponseGenerator;
//
//    @Autowired
//    @Qualifier("theiaSessionDataService")
//    protected ITheiaSessionDataService theiaSessionDataService;
//
//    @Autowired
//    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;
//
//    @Autowired
//    private TransactionCacheUtils transactionCacheUtils;
//
//    @RequestMapping(value = "/submit", method = RequestMethod.POST)
//    public void processAfterKyc(final HttpServletRequest request, final HttpServletResponse response)
//            throws IOException, ServletException {
//        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
//        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
//        WorkFlowTransactionBean workFlowTransBean;
//        WorkFlowRequestBean flowRequestBean;
//        String jspName;
//        String htmlPage;
//
//        String transId = request.getParameter(KYC_TXN_ID);
//        String transKycKey = "TRANS_KYC_" + transId;
//        workFlowTransBean = (WorkFlowTransactionBean) theiaTransactionalRedisUtil.get(transKycKey);
//
//        if (workFlowTransBean == null) {
//            throw new TheiaServiceException("workFlowTransBean is null, no data in cache");
//        }
//
//        int count = Integer.valueOf(request.getParameter(KYC_RETRY_COUNT));
//
//        if (count >= 3) {
//            LOGGER.info("Retry limit reached for KYC on boarding flow");
//            String html = merchantResponseService.processMerchantFailResponse(workFlowTransBean.getWorkFlowBean()
//                    .getPaymentRequestBean(), ResponseConstants.MERCHANT_FAILURE_RESPONSE);
//            pageDetailsResponse = new PageDetailsResponse();
//            pageDetailsResponse.setHtmlPage(html);
//            pageDetailsResponse.setSuccessfullyProcessed(false);
//            response.getOutputStream().print(pageDetailsResponse.getHtmlPage());
//            response.setContentType("text/html");
//            return;
//        }
//
//        count++;
//        String userId = request.getParameter(KYC_USER_ID);
//        try {
//            userKycServiceImpl.doKYC(request, userId);
//        } catch (KycValidationException e) {
//            LOGGER.error("User KYC failed");
//            String kycErrMsg = ConfigurationUtil.getProperty(KYC_ERROR_MESSAGE,
//                    "We could not validate your ID. Please try again with a different document ID");
//            try {
//                request.setAttribute(KYC_MID, workFlowTransBean.getWorkFlowBean().getPaytmMID());
//                request.setAttribute(KYC_ORDER_ID, workFlowTransBean.getWorkFlowBean().getOrderID());
//                request.setAttribute(KYC_FLOW, "YES");
//                request.setAttribute(KYC_TXN_ID, workFlowTransBean.getTransID());
//                request.setAttribute(KYC_USER_ID, userId);
//                request.setAttribute(KYC_RETRY_COUNT, count);
//                request.setAttribute(KYC_ERROR_MESSAGE, kycErrMsg);
//
//                StringBuilder queryBuilder = new StringBuilder();
//                queryBuilder.append("MID=").append(workFlowTransBean.getWorkFlowBean().getPaytmMID());
//                queryBuilder.append("&ORDER_ID=").append(workFlowTransBean.getWorkFlowBean().getOrderID());
//                queryBuilder.append("&route=");
//
//                request.setAttribute("queryStringForSession", queryBuilder.toString());
//                request.getRequestDispatcher(VIEW_BASE_WITH_JSP).forward(request, response);
//
//            } catch (Exception ex) {
//                LOGGER.error("Unable to redirect to kyc page after KYC failure");
//            }
//            return;
//        }
//
//        LOGGER.info("Success response received from the KYC service");
//        String userKycKey = "KYC_" + userId;
//        long timeOut = (long) 30 * 60;
//        theiaTransactionalRedisUtil.set(userKycKey, true, timeOut);
//
//        flowRequestBean = workFlowTransBean.getWorkFlowBean();
//
//        // Fetch bank form using Query_PayResult API in case of CC/DC
//        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
//
//        if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
//                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())
//                || PaymentTypeIdEnum.NB.value.equals(flowRequestBean.getPaymentTypeId())) {
//
//            queryPayResultResponse = workFlowHelper.fetchBankForm(workFlowTransBean);
//            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
//                if (queryPayResultResponse.getResponse() != null) {
//                    workFlowTransBean.getWorkFlowBean().getPaymentRequestBean()
//                            .setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
//                }
//                LOGGER.error("Query pay result failed due to : {}", queryPayResultResponse.getFailureMessage());
//                try {
//                    LOGGER.info("Sending response to merchant , Biz call is unsuccessful because : {}",
//                            queryPayResultResponse.getFailureMessage());
//                    String html = merchantResponseService.processMerchantFailResponse(workFlowTransBean
//                            .getWorkFlowBean().getPaymentRequestBean(), queryPayResultResponse.getResponseConstant());
//                    pageDetailsResponse.setHtmlPage(html);
//                    pageDetailsResponse.setSuccessfullyProcessed(false);
//                } catch (Exception e) {
//                    LOGGER.error("Unable to redirect to OOPS page from AddMoneyExpressSubmitKYCController after queryPayResultResponse");
//                }
//            }
//            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
//
//            // Need to close order in case of payment fail
//            if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
//                closeFundOrder(workFlowTransBean);
//            }
//        }
//
//        if (!pageDetailsResponse.isSuccessfullyProcessed()) {
//            /* if Html Page is Returned */
//            if (StringUtils.isNotBlank(pageDetailsResponse.getHtmlPage())) {
//                htmlPage = pageDetailsResponse.getHtmlPage();
//                pageDetailsResponse.setHtmlPage(htmlPage);
//            }
//            request.getRequestDispatcher(VIEW_BASE + pageDetailsResponse.getJspName() + ".jsp").forward(request,
//                    response);
//        } else {
//            // Setting data in response
//            workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
//            workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
//            workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
//            workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
//            workFlowResponseBean.setExtendedInfo(workFlowTransBean.getExtendInfo());
//            workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
//            LOGGER.debug("WorkFlowResponseBean : {} ", workFlowResponseBean);
//            String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean,
//                    flowRequestBean.getPaymentRequestBean());
//            theiaSessionDataService.setRedirectPageInSession(request, responsePage);
//
//            transactionCacheUtils.putTransInfoInCache(workFlowTransBean.getTransID(), flowRequestBean.getPaytmMID(),
//                    flowRequestBean.getOrderID(), true);
//
//            if (pageDetailsResponse.isSuccessfullyProcessed()) {
//                jspName = theiaViewResolverService.returnForwarderPage();
//                pageDetailsResponse.setJspName(jspName);
//            }
//            /* if Html Page is Returned */
//            if (StringUtils.isNotBlank(pageDetailsResponse.getHtmlPage())) {
//                htmlPage = pageDetailsResponse.getHtmlPage();
//                pageDetailsResponse.setHtmlPage(htmlPage);
//            }
//            request.getRequestDispatcher(VIEW_BASE + pageDetailsResponse.getJspName() + ".jsp").forward(request,
//                    response);
//        }
//
//    }
//
//    private void closeFundOrder(final WorkFlowTransactionBean workFlowTransBean) {
//        final GenericCoreResponseBean<BizCancelOrderResponse> cancelFundOrder = workFlowHelper
//                .closeFundOrder(workFlowTransBean);
//        if (!cancelFundOrder.isSuccessfullyProcessed()) {
//            LOGGER.error("Close or cancel order failed due to :{} ", cancelFundOrder.getFailureMessage());
//        }
//    }
// }
