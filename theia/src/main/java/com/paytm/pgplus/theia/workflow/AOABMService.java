//package com.paytm.pgplus.theia.workflow;
//
////import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaMandateCallbackRequest;
////import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaMandateCreateRequest;
////import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaMandateCreateResponse;
//import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
//import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
//import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
//import com.paytm.pgplus.biz.core.order.service.IOrderService;
//import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
//import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
//import com.paytm.pgplus.common.bankForm.model.BankForm;
//import com.paytm.pgplus.common.bankForm.model.FormDetail;
//import com.paytm.pgplus.common.enums.MandateAuthMode;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.upi.IUPIAccountService;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
//import com.paytm.pgplus.payloadvault.subscription.enums.AccountType;
//import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
//import com.paytm.pgplus.request.InitiateTransactionRequestBody;
//import com.paytm.pgplus.response.ResponseHeader;
//import com.paytm.pgplus.theia.cache.IMerchantMappingService;
//import com.paytm.pgplus.theia.exceptions.MandateException;
//import com.paytm.pgplus.theia.models.NativeJsonResponse;
//import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
//import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
//import com.paytm.pgplus.theia.nativ.processor.AOAMandateProcessor;
//import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
//import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
//import com.paytm.pgplus.theia.offline.enums.ResultCode;
//import com.paytm.pgplus.theia.utils.MerchantResponseService;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_URL_INFO_WEBSITE_FOR_BM;
//
///**
// * mandate service responsible for --> creation and validation of mandate. -->
// * processing of the response received after authentication on destination bank.
// */
//
//@Service("aoabmService")
//public class AOABMService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(AOABMService.class);
//
//    @Autowired
//    @Qualifier("createBmService")
//    private CreateBmService createBmService;
//
//    @Autowired
//    @Qualifier("processedBmService")
//    private ProcessedBmService processedBMService;
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Autowired
//    private MerchantResponseService merchantResponseService;
//
//    @Autowired
//    private IBankInfoDataService bankInfoDataService;
//
//    @Autowired
//    @Qualifier("GetUPIAccountService")
//    IUPIAccountService getUPIAccountService;
//
//    @Autowired
//    @Qualifier("merchantMappingService")
//    private IMerchantMappingService merchantMappingService;
//
//    @Autowired
//    @Qualifier("orderService")
//    private IOrderService orderServiceImpl;
//
//    @Autowired
//    @Qualifier("commonFlowHelper")
//    private WorkFlowRequestCreationHelper workRequestCreator;
//
//    @Autowired
//    private AOAMandateProcessor aoamandateProcessor;
//
//    /**
//     * creation and validation of mandate.
//     *
//     * @param requestBean
//     * @return
//     */
//    public PageDetailsResponse createBM(final PaymentRequestBean requestBean) {
//        validateRequest(requestBean);
//        try {
//
//            if (StringUtils.isNotBlank(requestBean.getSsoToken()) && StringUtils.isBlank(requestBean.getPaytmCustId())) {
//                UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(requestBean.getTxnToken());
//
//                if (userDetailsBiz == null) {
//                    throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(), ResponseConstants.SESSION_EXPIRY,
//                            true).setRequestBean(requestBean).build();
//                } else {
//                    requestBean.setPaytmCustId(userDetailsBiz.getUserId());
//                }
//            }
//
//            // fetch transactionId from session if order is already created
//            String transactionId = nativeSessionUtil.getTxnId(requestBean.getTxnToken());
//
//            BigDecimal txnAmount = new BigDecimal(requestBean.getTxnAmount());
//            String alipayMid = null;
//            String aoaMid = requestBean.getMid();
//            if (StringUtils.isNotBlank(requestBean.getPaymentOrderId()) && StringUtils.isNotBlank(requestBean.getPaymentMid())) {
//                aoaMid = requestBean.getPaymentMid();
//            }
//            final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
//                    .fetchMerchanData(aoaMid);
//            if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
//                alipayMid = merchantMappingResponse.getResponse().getAlipayId();
//            }
//
//            if (StringUtils.isNotBlank(transactionId) && txnAmount.doubleValue() == 0d) {
//                closeOrderForZeroAmountMandates(alipayMid, transactionId);
//                LOGGER.info("Order closed succesfully for Zero Amount Mandate Case");
//            }
//
//            nativeSessionUtil.setPaymentTypeId(requestBean.getTxnToken(), requestBean.getPaymentTypeId());
//
//            nativeSessionUtil.setTxnTokenAndWorkflowOnMidOrderId(requestBean.getMid(), requestBean.getOrderId(),
//                    requestBean.getTxnToken(), requestBean.getWorkflow());
//
//            return createBMForNativeJsonFlow(requestBean);
//        } catch (MandateException e) {
//            LOGGER.error("Mandate Exception occured while creating mandate", e);
//            throw e;
//        } catch (Throwable e) {
//            LOGGER.error("Exception while creating mandate", e);
//            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(), ResponseConstants.SYSTEM_ERROR,
//                    true).setRequestBean(requestBean).build();
//        }
//    }
//
//
//
//    /**
//     * processing response received after acceptance on destination bank.
//     *
//     * @param aoaMandateCallbackRequest
//     * @return
//     */
//    public PageDetailsResponse processBM(AoaMandateCallbackRequest aoaMandateCallbackRequest) {
//        return processedBMService.processAoaMandate(aoaMandateCallbackRequest);
//    }
//
//
//    /**
//     * validation of create mandate request.
//     *
//     * @param requestBean
//     */
//    private void validateRequest(PaymentRequestBean requestBean) {
//        InitiateTransactionRequestBody initiateTransactionRequestBody = nativeSessionUtil.getOrderDetail(requestBean
//                .getTxnToken());
//
//        /*
//         * preference is given to the call back url sent in request
//         * if it is empty then the registered call back url for bank mandate would be used.
//         */
//
//        String callBackUrl = requestBean.getCallbackUrl();
//
//        if (StringUtils.isBlank(callBackUrl)) {
//            callBackUrl = merchantResponseService.getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM,
//                    initiateTransactionRequestBody.getMid());
//            requestBean.setCallbackUrl(callBackUrl);
//        }
//
//    }
//
//    public void closeOrderForZeroAmountMandates(String alipayMid, String transId) {
//
//        BizCancelOrderRequest bizCancelOrderRequest = new BizCancelOrderRequest(alipayMid, transId,
//                "No Payment required for Zero Amount Mandate", true);
//        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = orderServiceImpl
//                .closeOrder(bizCancelOrderRequest);
//        if (!cancelOrder.isSuccessfullyProcessed()) {
//            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
//        }
//
//    }
//
//    /* BM for Native Json */
//    public PageDetailsResponse createBMForNativeJsonFlow(PaymentRequestBean requestBean) {
//
////        LOGGER.info("Entered Mandate creation for AOA NativeJson flow");
//
//        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
//
//        AoaMandateCreateRequest request = generateMandateRequest(requestBean);
//        AoaMandateCreateResponse mandateCreateResp = processAOAMandateRequest(request, requestBean);
//
//        LOGGER.info("Mandate creation response from AOA Subscription service : ", mandateCreateResp);
//
//        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
//        NativeJsonResponseBody body = new NativeJsonResponseBody();
//        body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
//        BankForm bankForm = new BankForm();
//        bankForm.setPageType("redirect");
//
//        FormDetail formDetail = new FormDetail();
//        formDetail.setActionUrl(mandateCreateResp.getRedirectionUrl());
//        formDetail.setMethod("POST");
//        formDetail.setType("redirect");
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/x-www-form-urlencoded");
//        formDetail.setHeaders(headers);
//
//        Map<String, String> content = new HashMap<>();
//        formDetail.setContent(content);
//
//        bankForm.setRedirectForm(formDetail);
//        body.setBankForm(bankForm);
//
//        nativeJsonResponse.setHead(new ResponseHeader());
//        nativeJsonResponse.setBody(body);
//
//
//        try {
//            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(nativeJsonResponse));
//            LOGGER.info("Create mandate request successfully processed");
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("Exception occurred while converting mandate request obj to json : {}", e);
//            pageDetailsResponse.setSuccessfullyProcessed(false);
//        }
//        return pageDetailsResponse;
//    }
//
//    /**
//     * generating request for mandate creation
//     *
//     * @param requestBean
//     * @return
//     */
////    private AoaMandateCreateRequest generateMandateRequest(PaymentRequestBean requestBean) {
////        AoaMandateCreateRequest request = new AoaMandateCreateRequest();
////        request.setSubscriptionId(requestBean.getSubscriptionID());
////        request.setOrderId(requestBean.getOrderId());
////        request.setMid(requestBean.getMid());
////        request.setTxnAmount(requestBean.getTxnAmount());
////        request.setMandateType(requestBean.getMandateType());
////        request.setBankCode(requestBean.getBankCode());
////        request.setAccountNumber(requestBean.getAccountNumber());
////        request.setAccountHolderName(requestBean.getUserName());
////        request.setAccountType(requestBean.getAccountType());
////        request.setMandateAuthMode(requestBean.getMandateAuthMode());
////
////        return request;
////    }
//
//    /**
//     * creating mandate.
//     *
//     * @param request
//     * @param requestBean
//     * @return
//     */
//    private AoaMandateCreateResponse processAOAMandateRequest(AoaMandateCreateRequest request, PaymentRequestBean requestBean) {
//        AoaMandateCreateResponse mandateCreateReq = aoamandateProcessor.createMandate(request, requestBean.getCallbackUrl(),
//                requestBean);
//        return mandateCreateReq;
//    }
//
//}
//
