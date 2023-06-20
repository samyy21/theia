///**
// *
// */
//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
//import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
//import com.paytm.pgplus.cache.model.LinkBasedMerchantInfo;
//import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
//import com.paytm.pgplus.cache.model.ResponseCodeDetails;
//import com.paytm.pgplus.common.enums.ERequestType;
//import com.paytm.pgplus.common.enums.EventNameEnum;
//import com.paytm.pgplus.common.model.EnvInfoRequestBean;
//import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
//import com.paytm.pgplus.common.util.PaymentModeMapperUtil;
//import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
//import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
//import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
//import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
//import com.paytm.pgplus.facade.enums.ApiFunctions;
//import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
//import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
//import com.paytm.pgplus.models.ExtendInfo;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
//import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
//import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
//import com.paytm.pgplus.request.InitiateTransactionRequestBody;
//import com.paytm.pgplus.theia.cache.IMerchantMappingService;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.exceptions.CoreSessionExpiredException;
//import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
//import com.paytm.pgplus.theia.exceptions.SessionExpiredException;
//import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
//import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
//import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
//import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
//import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
//import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
//import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
//import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.services.helper.FF4JHelper;
//import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
//import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
//import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
//import com.paytm.pgplus.theia.utils.*;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import redis.clients.jedis.exceptions.JedisDataException;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Locale;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.THEIA_BUISNESS_BASE_PATH;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.LINK_BASED_KEY;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.PAYMENT_SCREEN;
//
///**
// * @createdOn 21-Mar-2016
// * @author kesari
// */
//@Controller
//public class CancelTransactionController {
//    private static final Logger LOGGER = LoggerFactory.getLogger(CancelTransactionController.class);
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier(value = "theiaSessionDataService")
//    private ITheiaSessionDataService theiaSessionDataService;
//
//    @Autowired
//    private FlowDataMapper requestGeneratorHelper;
//
//    @Autowired
//    @Qualifier("cancelTransactionFlow")
//    private IWorkFlow cancelTransactionFlow;
//
//    @Autowired
//    private TheiaResponseGenerator theiaResponseGenerator;
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Autowired
//    @Qualifier("merchantMappingService")
//    private IMerchantMappingService merchantMappingService;
//
//    @Autowired
//    @Qualifier("merchantResponseService")
//    private MerchantResponseService merchantResponseService;
//
//    @Autowired
//    ResponseCodeUtil responseCodeUtil;
//
//    @Autowired
//    @Qualifier("merchantDataServiceImpl")
//    private IMerchantDataService merchantDataServiceImpl;
//
//    @Autowired
//    @Qualifier("acquiringOrderImpl")
//    private IAcquiringOrder acquiringOrder;
//
//    @Autowired
//    @Qualifier("aoaUtils")
//    private AOAUtils aoaUtils;
//
//    @Autowired
//    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;
//
//    @Autowired
//    private FF4JHelper ff4JHelper;
//
//    @Autowired
//    private NativePaymentUtil nativePaymentUtil;
//
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    @RequestMapping(value = "/cancelTransaction", method = { RequestMethod.GET, RequestMethod.POST })
//    public String cancelTransaction(HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
//        long startTime = System.currentTimeMillis();
//        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
//        try {
//            if (Boolean.TRUE.toString()
//                    .equals(request.getParameter(TheiaConstant.GvConsent.IS_EXPRESS_ADD_MONEY_TO_GV))) {
//                return handleCancelTxnForExpressAddMoneyToGv(request);
//            }
//            PaymentRequestBean paymentRequestData = new PaymentRequestBean(request);
//            LOGGER.info("Request received for cancel transaction : {}", paymentRequestData);
//
//            TransactionInfo txnData = theiaSessionDataService.getTxnInfoFromSession(request);
//            if (txnData == null || StringUtils.isBlank(txnData.getTxnId())) {
//                // Checking for Native flow
//                String txnToken = request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN);
//                String midOrderIdKey = nativeSessionUtil.getMidOrderIdKeyForRedis(paymentRequestData.getMid(),
//                        paymentRequestData.getOrderId());
//                String txnTokenFromMidOrderId = (String) nativeSessionUtil.getKey(midOrderIdKey);
//                boolean failCancelTransactionWithOutToken = ff4JHelper.isFF4JFeatureForMidEnabled(
//                        TheiaConstant.ExtraConstants.THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN,
//                        paymentRequestData.getMid());
//                if (failCancelTransactionWithOutToken && StringUtils.isBlank(txnToken)) {
//                    EventUtils.pushTheiaEvents(paymentRequestData.getMid(), paymentRequestData.getOrderId(),
//                            EventNameEnum.INVALID_CANCEL_TRANSACTION_REQUEST, null);
//                    String responsePage = theiaResponseGenerator.getFinalHtmlResponse(
//                            ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL)
//                                    + paymentRequestData.getOrderId(),
//                            getStaticFailCancelTransactionResponse(paymentRequestData), null, null, null);
//                    theiaSessionDataService.setRedirectPageInSession(request, responsePage);
//                    return theiaViewResolverService.returnForwarderPage();
//                } else if (failCancelTransactionWithOutToken && !(txnToken.equals(txnTokenFromMidOrderId))) {
//                    LOGGER.error("Could not fetch redirect page");
//                    return theiaViewResolverService.returnOOPSPage(request);
//                } else {
//                    txnToken = txnTokenFromMidOrderId;
//                }
//                if (StringUtils.isNotBlank(txnToken)) {
//                    try {
//                        String responsePage = cancelNativeTransaction(paymentRequestData, txnToken, midOrderIdKey,
//                                envInfo);
//                        theiaSessionDataService.setRedirectPageInSession(request, responsePage);
//                        return theiaViewResolverService.returnForwarderPage();
//                    } catch (Exception e) {
//                        LOGGER.error("Error while cancelling native request");
//                        throw e;
//                    }
//                }
//                LOGGER.warn("Session does not contains a valid transaction id queryString :{}",
//                        request.getQueryString());
//                throw new SessionExpiredException("Session does not contains a valid transaction id.");
//
//            }
//
//            MerchantInfo merchantInfo = theiaSessionDataService.getMerchantInfoFromSession(request);
//            TransactionConfig txnConfig = theiaSessionDataService.getTxnConfigFromSession(request);
//            ExtendedInfoRequestBean extendedInfoRequestBean = theiaSessionDataService
//                    .geExtendedInfoRequestBean(request);
//
//            if (merchantInfo == null) {
//                throw new TheiaControllerException("MerchantInfo is null");
//            }
//
//            if (txnConfig == null) {
//                throw new TheiaControllerException("TransactionConfig is null");
//            }
//
//            if ((TheiaConstant.TxnType.ACQUIRING).equals(txnConfig.getTxnType())) {
//                String closeReason = "closeReason*141";
//                WorkFlowRequestBean errorWorkFlowReqBean = requestGeneratorHelper.createCancelTransactionRequest(
//                        txnData, merchantInfo, paymentRequestData, closeReason);
//                GenericCoreResponseBean<WorkFlowResponseBean> errorResponse = cancelTransactionFlow
//                        .process(errorWorkFlowReqBean);
//                if (!errorResponse.isSuccessfullyProcessed()) {
//                    String errorMessage = "Could not cancel acquiring transaction because :: "
//                            + errorResponse.getFailureMessage();
//                    LOGGER.error(errorMessage);
//                    throw new Exception(errorMessage);
//                }
//            }
//
//            else if ((TheiaConstant.TxnType.FUND).equals(txnConfig.getTxnType())) {
//                WorkFlowRequestBean errorTopupWorkFlowReqBean = requestGeneratorHelper
//                        .createTopupCancelTransactionRequest(txnData, envInfo);
//                GenericCoreResponseBean<WorkFlowResponseBean> errorResponse = cancelTransactionFlow
//                        .process(errorTopupWorkFlowReqBean);
//                if (!errorResponse.isSuccessfullyProcessed()) {
//                    String errorMessage = "Could not cancel fund transaction because :: "
//                            + errorResponse.getFailureMessage();
//                    LOGGER.error(errorMessage);
//                    throw new Exception(errorMessage);
//                }
//            }
//            TransactionResponse transactionResponse = new TransactionResponse();
//            transactionResponse.setMid(merchantInfo.getMid());
//            transactionResponse.setTxnId(txnData.getTxnId());
//            transactionResponse.setOrderId(txnData.getOrderId());
//            transactionResponse.setCustId(txnData.getCustID());
//            transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
//            transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnData.getTxnAmount()));
//            transactionResponse.setCallbackUrl(getCallbackUrl(request));
//            transactionResponse.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(txnData
//                    .getPaymentTypeId()));
//            if (extendedInfoRequestBean != null) {
//                transactionResponse.setMerchUniqueReference(extendedInfoRequestBean.getMerchantUniqueReference());
//                transactionResponse.setExtraParamsMap(extendedInfoRequestBean.getExtraParamsMap());
//                if (StringUtils.isNotBlank(extendedInfoRequestBean.getClientId()))
//                    transactionResponse.setClientId(extendedInfoRequestBean.getClientId());
//            } else {
//                transactionResponse.setExtraParamsMap(Collections.emptyMap());
//            }
//            transactionResponse.setRequestType(txnData.getRequestType());
//
//            // Set Response-Code and Message
//            responseCodeUtil
//                    .setRespMsgeAndCode(transactionResponse, null, SystemResponseCode.USER_CLOSED_RESPONSE_CODE);
//
//            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
//            if (StringUtils.isNotBlank(responsePage)) {
//                theiaSessionDataService.setRedirectPageInSession(request, responsePage);
//                return theiaViewResolverService.returnForwarderPage();
//            }
//            LOGGER.error("Could not fetch redirect page");
//        } catch (Exception e) {
//            throw new CoreSessionExpiredException("Exception Occurred while cancel transcation ");
//        } finally {
//            LOGGER.info("Total time taken for Controller {} is {} ms", "CancelTransactionController",
//                    System.currentTimeMillis() - startTime);
//
//        }
//        return theiaViewResolverService.returnOOPSPage(request);
//    }
//
//    private String getCallbackUrl(HttpServletRequest request) {
//        ExtendedInfoRequestBean extendInfo = theiaSessionDataService.geExtendedInfoRequestBean(request);
//
//        if (null != extendInfo && StringUtils.isNotBlank(extendInfo.getCallBackURL())) {
//            return extendInfo.getCallBackURL();
//        }
//
//        LOGGER.error("Could not fetch callback Url");
//        throw new TheiaControllerException("Could not fetch callback Url");
//    }
//
//    private String cancelNativeTransaction(PaymentRequestBean paymentRequestData, String txnToken,
//            String midOrderIDToken, EnvInfoRequestBean envInfo) throws Exception {
//        String transID = nativeSessionUtil.getTxnId(txnToken);
//        if (transID == null) {
//            String errorMessage = "transID is null";
//            LOGGER.error(errorMessage);
//            throw new TheiaControllerException(errorMessage);
//        }
//
//        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
//        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
//
//        MappingMerchantData merchantMapping = null;
//        try {
//            merchantMapping = merchantMappingService.getMappingMerchantData(paymentRequestData.getMid());
//        } catch (PaymentRequestValidationException e) {
//            LOGGER.error("Exception occured while fetching merchant mapping {}", e);
//            throw e;
//        }
//
//        String closeReason = "closeReason*141";
//        WorkFlowRequestBean errorWorkFlowReqBean = null;
//        if (orderDetail.isNativeAddMoney()) {
//            errorWorkFlowReqBean = requestGeneratorHelper.createTopupCancelTransactionRequest(transID, envInfo);
//        } else {
//            errorWorkFlowReqBean = requestGeneratorHelper.createCancelTransactionRequest(transID,
//                    merchantMapping.getAlipayId(), closeReason);
//        }
//        if (aoaUtils.isAOAMerchant(paymentRequestData)) {
//            errorWorkFlowReqBean.setFromAoaMerchant(true);
//        }
//        GenericCoreResponseBean<WorkFlowResponseBean> errorResponse = cancelTransactionFlow
//                .process(errorWorkFlowReqBean);
//        if (!errorResponse.isSuccessfullyProcessed()) {
//            String errorMessage = "Could not cancel transaction because :: " + errorResponse.getFailureMessage();
//            LOGGER.error(errorMessage);
//            throw new Exception(errorMessage);
//        }
//
//        // Removing txnToken from redis
//        try {
//            LOGGER.info("Deleting keys {} , {}", midOrderIDToken, txnToken);
//            nativeSessionUtil.deleteKey(midOrderIDToken, txnToken);
//        } catch (JedisDataException e) {
//            LOGGER.error("Session Invalidate Exception in case of deleting keys", e);
//            throw com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException.getException();
//        }
//
//        try {
//
//            TransactionResponse transactionResponse = theiaResponseGenerator
//                    .createTransactionResponseForNativeCloseOrder(orderDetail, paymentRequestData, transID);
//
//            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
//            if (StringUtils.isNotBlank(responsePage)) {
//                return responsePage;
//            } else {
//                String errorMessage = "Could not fetch redirect page";
//                LOGGER.error(errorMessage);
//                throw new Exception(errorMessage);
//            }
//
//        } catch (Exception e) {
//            throw e;
//        }
//
//    }
//
//    public String handleCancelTxnForExpressAddMoneyToGv(HttpServletRequest request) {
//        addMoneyToGvConsentUtil.setAttributesForGvConsentFlow(request);
//        PaymentRequestBean requestBean = new PaymentRequestBean(request, true);
//        PageDetailsResponse pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(requestBean,
//                ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
//        theiaSessionDataService.setRedirectPageInSession(request, pageDetailsResponse.getHtmlPage(), true);
//        addMoneyToGvConsentUtil.expireGvConsentFlowSession(request.getParameter(TheiaConstant.GvConsent.TOKEN));
//        return theiaViewResolverService.returnForwarderPage();
//    }
//
//    public StringBuilder getStaticFailCancelTransactionResponse(PaymentRequestBean paymentRequestBean) {
//        StringBuilder stringBuilder = new StringBuilder();
//        putData(paymentRequestBean.getOrderId(), stringBuilder, TheiaConstant.ResponseConstants.ORDER_ID);
//        putData(paymentRequestBean.getMid(), stringBuilder, TheiaConstant.ResponseConstants.M_ID);
//        ResponseCodeDetails responseCodeDetails = responseCodeUtil
//                .getResponseCodeDetails(SystemResponseCode.DEFAULT_PENDING_CODE);
//        if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
//            putData(responseCodeDetails.getResponseCode(), stringBuilder, TheiaConstant.ResponseConstants.RESPONSE_CODE);
//            putData(responseCodeUtil.getResponseMsg(responseCodeDetails), stringBuilder,
//                    TheiaConstant.ResponseConstants.RESPONSE_MSG);
//        }
//        return stringBuilder;
//    }
//
//    public void putData(String data, StringBuilder strBuilder, String responseConstantName) {
//        if (StringUtils.isNotBlank(data)) {
//            strBuilder.append(replace(responseConstantName, data));
//        }
//    }
//
//    private String replace(String name, String value) {
//        StringBuilder str = new StringBuilder(TheiaConstant.HTMLBuilder.INPUT_FORM_TEMPLATE);
//        if ((name != null) && (value != null)) {
//            int index = str.indexOf(TheiaConstant.HTMLBuilder.NAME_TEMPLATE);
//            if (index >= 0) {
//                str.replace(index, index + TheiaConstant.HTMLBuilder.NAME_TEMPLATE.length(), name);
//            }
//            index = str.indexOf(TheiaConstant.HTMLBuilder.VALUE_TEMPLATE);
//            if (index >= 0) {
//                str.replace(index, index + TheiaConstant.HTMLBuilder.VALUE_TEMPLATE.length(), value);
//            }
//            return str.toString();
//        }
//        return "";
//    }
// }
