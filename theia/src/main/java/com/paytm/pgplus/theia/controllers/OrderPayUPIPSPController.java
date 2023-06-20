package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.enums.SettlementType;
import com.paytm.pgplus.biz.enums.UPIPSPEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.UPIPSPResponseBody;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.datamapper.helper.BizRequestResponseMapperHelper;
import com.paytm.pgplus.theia.exceptions.UPIPSPGenericException;
import com.paytm.pgplus.theia.models.UPIPSPBody;
import com.paytm.pgplus.theia.models.UPIPSPRequest;
import com.paytm.pgplus.theia.models.UPIPSPResponse;
import com.paytm.pgplus.theia.models.UPIPSPResponseHeader;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPIPSPKeys.QR_SUBSCRIPTION;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPIPSPKeys.UPI_CREDIT_CARD;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.UPI_PSP_URI;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_ENABLE_LINK_FLOW_ON_DQR;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.PGP_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID;

/**
 * @author Santosh chourasia
 *
 */
@RestController
public class OrderPayUPIPSPController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPayUPIPSPController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(OrderPayUPIPSPController.class);

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("UPIPSPWorkflow")
    private IWorkFlow bizWorkFlow;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("walletQRCodeServiceImpl")
    private IWalletQRCodeService walletQRCodeService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    NativeRetryUtil nativeRetryUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("bizRequestResponseMapperHelper")
    private BizRequestResponseMapperHelper bizRequestResponseMapperHelper;

    @Autowired
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private AWSStatsDUtils statsDUtil;

    private static Pattern UPI_QR_CODE_PREFIX_MATCHER = Pattern.compile(ConfigurationUtil.getProperty(
            com.paytm.pgplus.theia.constants.TheiaConstant.UpiConfiguration.UPI_QR_CODE_PREFIX_REGEX, "^(paytmqr).*"));

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(value = "/v1/order/pay/upipsp")
    @ResponseBody
    public ResponseEntity<UPIPSPResponse> orderPayUPIPSP(@RequestBody String requestPayload,
            HttpServletRequest httpServletRequest) {
        final long startTime = System.currentTimeMillis();
        UPIPSPRequest upiPSPrequest = null;
        try {
            upiPSPrequest = JsonMapper.mapJsonToObject(requestPayload, UPIPSPRequest.class);
            if (!beanValidation(upiPSPrequest)) {
                return ResponseEntity.ok(prepareUpiPSPResponse(new WorkFlowResponseBean(), startTime));
            }
            setMDC(upiPSPrequest.getBody().getMid(), upiPSPrequest.getHeader().getRequestMsgId(), httpServletRequest);
            LOGGER.info("Received request for UPIPSP : {}", upiPSPrequest);

            if (!validateChecksum(upiPSPrequest)) {
                return ResponseEntity.ok(prepareUpiPSPResponse(new WorkFlowResponseBean(), startTime));
            }

            if (null != upiPSPrequest.getBody().getPayerPaymentInstrument()
                    && UPI_CREDIT_CARD.equals(upiPSPrequest.getBody().getPayerPaymentInstrument())
                    && !bizRequestResponseMapperHelper.validateAndCheckCCOnUpi(upiPSPrequest.getBody().getMid())) {
                LOGGER.error("CC On UPI is not enabled");
                return ResponseEntity.ok(prepareUpiPSPResponse(new WorkFlowResponseBean(), startTime));
            }

            checkIfMerchantInactiveOrBlocked(upiPSPrequest.getBody().getMid());
            /**
             * This was done to prevent direct money transfer to addMoney@paytm
             * VPA. By doing this User transferring P2P money and doing add
             * Money. PGP-14894
             */
            boolean onus = merchantExtendInfoUtils.isMerchantOnPaytm(upiPSPrequest.getBody().getMid());
            EXT_LOGGER
                    .customInfo("Merchnat Onus Status {} and Order Id {}", onus, upiPSPrequest.getBody().getOrderId());
            if (onus && StringUtils.isBlank(upiPSPrequest.getBody().getOrderId())) {
                EXT_LOGGER.customInfo("Merchant is Onus, Hence Rejecting this Request");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            WorkFlowResponseBean workFlowResponseBean;
            /**
             * This is being done for AOA DQR transactions, PG mid is being
             * converted to AOA Mid
             */
            if (merchantPreferenceService.isAOAsPaytmPgMID(upiPSPrequest.getBody().getMid(), false)
                    && !"AOA_INSTA".equals(upiPSPrequest.getHeader().getClientId())) {
                String aoaMid = aoaUtils.getAOAMidForPGMid(upiPSPrequest.getBody().getMid());
                boolean aoaRequest = false;
                if (!ff4JUtils.isFeatureEnabledOnMid(aoaMid, BizConstant.THEIA_AOAMID_TO_PGMID_ENABLED, false)) {
                    upiPSPrequest.getBody().setMid(aoaMid);
                    upiPSPrequest.getBody().setRequestType(ERequestType.UNI_PAY.getType());
                    setMercUnqRef(upiPSPrequest);
                    aoaRequest = true;
                }
                LOGGER.info("aoaRequest Received : {}", aoaRequest);
                workFlowResponseBean = processUPIPSPRequest(upiPSPrequest, httpServletRequest, aoaRequest);
            } else {
                workFlowResponseBean = processUPIPSPRequest(upiPSPrequest, httpServletRequest, false);
            }
            LOGGER.info("Total time taken by orderPayUPIPSP API : {}ms", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(prepareUpiPSPResponse(workFlowResponseBean, startTime));
        } catch (NativeFlowException nfe) {
            LOGGER.error("Merchant is either blocked or inactive");
            final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
            UPIPSPResponseBody upiPspResponseBody = new UPIPSPResponseBody();
            upiPspResponseBody.setOrderId(upiPSPrequest.getBody().getOrderId());
            upiPspResponseBody.setTxnAmount(upiPSPrequest.getBody().getTxnAmount());
            updateUpiPSPResponseForQrSubscription(upiPspResponseBody, upiPSPrequest);
            upiPspResponseBody.setMid(upiPSPrequest.getBody().getMid());
            upiPspResponseBody.setRequestMsgId(upiPSPrequest.getHeader().getRequestMsgId());
            upiPspResponseBody.setResultCode(UPIPSPEnum.FAIL.getResultCode());
            upiPspResponseBody.setResultCodeId(UPIPSPEnum.FAIL.getResultCodeId());
            if (nfe.getResultInfo() != null) {
                upiPspResponseBody.setResultMsg(nfe.getResultInfo().getResultMsg());
            }
            workFlowResponseBean.setUpiPSPResponse(upiPspResponseBody);
            return ResponseEntity.ok(prepareUpiPSPResponse(workFlowResponseBean, startTime));
        } catch (Exception e) {
            LOGGER.error("Error While Parsing Request : {}", requestPayload);
            return ResponseEntity.ok(prepareUpiPSPResponse(new WorkFlowResponseBean(), startTime));
        }
    }

    private WorkFlowResponseBean updateUpiPSPRequestForQrSubscription(UPIPSPRequest upiPSPrequest) {

        if (QR_SUBSCRIPTION.equals(upiPSPrequest.getBody().getType())) {
            upiPSPrequest.getBody().setOrderAmount(upiPSPrequest.getBody().getTxnAmount());
            upiPSPrequest.getBody().setSubscriptionId(upiPSPrequest.getBody().getOrderId()); // for
            // UpiPsp
            // Upi
            // team
            // is
            // sending
            // subId
            // as
            // orderId.Hence,
            // retrieving
            // actualo
            // orderId
            // from
            // redis
            String key = getSubscriptionKeyFromRedis(upiPSPrequest.getBody().getOrderId());
            String subsKey = "";
            SubscriptionResponse subscriptionResponse = null;
            if (key != null) {
                subsKey = key.split("[||].")[0];
                subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
            }
            if (subscriptionResponse != null) {
                upiPSPrequest.getBody().setOrderId(subscriptionResponse.getOrderId());
                upiPSPrequest.getBody().setTxnAmount(
                        AmountUtils.getTransactionAmountInRupee(subscriptionResponse.getTxnAmount()));
            } else {
                WorkFlowRequestBean tempFlowRequestBean = new WorkFlowRequestBean();
                tempFlowRequestBean.setOrderID(upiPSPrequest.getBody().getOrderId());
                tempFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInRupee(upiPSPrequest.getBody()
                        .getTxnAmount()));
                tempFlowRequestBean.setAlipayMID(upiPSPrequest.getBody().getMid());
                return workFlowHelper.returnResponseForOrderNotFound(tempFlowRequestBean);
            }
        }

        return null;
    }

    private void setMercUnqRef(UPIPSPRequest upiPSPrequest) {
        String txnToken = nativeSessionUtil.getTxnToken(upiPSPrequest.getBody().getMid(), upiPSPrequest.getBody()
                .getOrderId());
        if (StringUtils.isNotBlank(txnToken)) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                if (orderDetail != null && orderDetail.getExtendInfo() != null
                        && StringUtils.isNotBlank(orderDetail.getExtendInfo().getMercUnqRef())) {
                    if (upiPSPrequest.getBody().getExtendInfo() == null) {
                        upiPSPrequest.getBody().setExtendInfo(new HashMap<>());
                    }
                    upiPSPrequest.getBody().getExtendInfo()
                            .put(Native.MERC_UNQ_REF, orderDetail.getExtendInfo().getMercUnqRef());
                }
            }
        }
    }

    private WorkFlowResponseBean processUPIPSPRequest(UPIPSPRequest request, HttpServletRequest httpServletRequest,
            boolean aoaRequest) throws UPIPSPGenericException {
        boolean isOrderIdPresentInRequest = true;

        if (StringUtils.isBlank(request.getBody().getOrderId()) && request.getBody().getType() == null) {

            String aggregatorMid = merchantDataUtil.getAggregatorMid(request.getBody().getMid());

            request.getBody().setOrderId(OfflinePaymentUtils.generateOrderId(aggregatorMid));
            isOrderIdPresentInRequest = false;
            LOGGER.info("Order-Id generated for the request is {} :", request.getBody().getOrderId());
        }
        SubscriptionResponse subscriptionResponse = null;

        WorkFlowResponseBean errorResponseForOrderNotFound = updateUpiPSPRequestForQrSubscription(request);
        if (errorResponseForOrderNotFound != null)
            return errorResponseForOrderNotFound;

        PaymentRequestBean paymentRequestBean = preparePaymentRequestbean(request, httpServletRequest);
        GenericCoreResponseBean<WorkFlowResponseBean> workflowResponse = null;
        try {
            paymentRequestBean.setSessionRequired(false);
            WorkFlowRequestBean workFlowRequestBean = bizRequestResponseMapper
                    .mapWorkFlowRequestData(paymentRequestBean);
            if (aoaRequest) {
                workFlowRequestBean.setFromAOARequest("true");
            }
            if (workFlowRequestBean.getExtendInfo() != null && paymentRequestBean != null) {
                workFlowRequestBean.getExtendInfo().setPaytmUserId(paymentRequestBean.getCustId());
                updateExtendInfoForOfflineIntent(request, workFlowRequestBean);
            }
            if (StringUtils.isNotBlank(request.getBody().getUpiOrderTimeOutInSeconds())) {
                if (NumberUtils.isNumber(request.getBody().getUpiOrderTimeOutInSeconds())) {
                    workFlowRequestBean.setUpiOrderTimeOutInSeconds(request.getBody().getUpiOrderTimeOutInSeconds());
                } else {
                    LOGGER.info("UPI order timeout is not a numeric string :{}", request.getBody()
                            .getUpiOrderTimeOutInSeconds());
                }
            }
            if (request.getHeader() != null && StringUtils.isNotBlank(request.getHeader().getRequestMsgId())) {
                workFlowRequestBean.setUpiPspReqMsgId(request.getHeader().getRequestMsgId());
            }

            if (StringUtils.isNotBlank(request.getBody().getSettlementType())) {
                if (SettlementType.DIRECT_SETTLEMENT.getValue().equals(request.getBody().getSettlementType())
                        || SettlementType.DEFERRED_SETTLEMENT.getValue().equals(request.getBody().getSettlementType())) {
                    workFlowRequestBean.setSettleType(request.getBody().getSettlementType());
                } else {
                    LOGGER.error("Invalid Settle Type");
                    throw new UPIPSPGenericException("Invalid Settle Type");
                }
            }

            // Treating upipsp request as offline.
            if (workFlowRequestBean.getFeeRateFactors() != null) {
                workFlowRequestBean.getFeeRateFactors().setApi(false);
                workFlowRequestBean.getFeeRateFactors().setQr(true);
            }
            workFlowRequestBean.setVirtualPaymentAddress(paymentRequestBean.getVirtualPaymentAddr());
            workFlowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH);
            workFlowRequestBean.setOrderPSPRequest(true);
            /*
             * setting below flag for AOA explicitly as req-type is changed to
             * 'UNI_PAY' in AOA
             */
            if (aoaUtils.isAOAMerchant(request.getBody().getMid())
                    && !ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(),
                            THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR, false)) {
                workFlowRequestBean.setPushDataToDynamicQR(true);
            }
            populateDataForRequest(request, workFlowRequestBean);
            enrichExtendedInfo(request, workFlowRequestBean);
            setPcfDataForMerchant(workFlowRequestBean);
            workFlowRequestBean.setOffusBasedOrderFound(merchantPreferenceService
                    .isOffusOrderNotFound(workFlowRequestBean.getPaytmMID()));
            if (ff4JUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED, false)
                    && isOrderIdPresentInRequest) {
                try {
                    checkForRetry(paymentRequestBean, workFlowRequestBean);
                } catch (NativeFlowException nfe) {
                    return returnResponseRetryCountBreached(request.getBody().getOrderId(), nfe).getResponse();
                }
            }
            if (QR_SUBSCRIPTION.equals((request.getBody().getType()))) {
                workFlowRequestBean.setSubsMaxAmount(request.getBody().getOrderAmount());
            }
            processTransactionUtil.setBankFormOptimizationFlow(workFlowRequestBean, paymentRequestBean);
            workflowResponse = bizWorkFlow.process(workFlowRequestBean);
        } catch (Exception e) {
            LOGGER.error("Exception occured while processing UPIPSPRequest {} :", e);
            throw new UPIPSPGenericException(e.getMessage());
        }
        return (null != workflowResponse ? workflowResponse.getResponse() : null);
    }

    private void updateExtendInfoForOfflineIntent(UPIPSPRequest request, WorkFlowRequestBean workFlowRequestBean) {
        final UPIPSPBody body = request.getBody();
        if (StringUtils.isNotBlank(body.getPayerName())) {
            workFlowRequestBean.getExtendInfo().setPayerName(body.getPayerName());
        }
        if (StringUtils.isNotBlank(body.getPayerPSP())) {
            workFlowRequestBean.getExtendInfo().setPayerPSP(body.getPayerPSP());
            LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
            metaData.put("PayerPSP", body.getPayerPSP());
            EventUtils
                    .pushTheiaEvents(body.getMid(), body.getOrderId(), EventNameEnum.UPI_PSP_PAYER_PSP_NAME, metaData);
            statsDUtil.pushResponse("/v1/order/pay/upipsp", metaData);
        }
    }

    private void setPcfDataForMerchant(WorkFlowRequestBean flowRequestBean) {
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(flowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(flowRequestBean.getPaytmMID()) || isSlabBasedMdr || flowRequestBean
                .isDynamicFeeMerchant())) {
            flowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            flowRequestBean.setPostConvenience(true);
        }
    }

    private void enrichExtendedInfo(UPIPSPRequest request, WorkFlowRequestBean workFlowRequestBean) {
        /**
         * This adds posId and MerchantContactNo in extendInfo to pass all
         * alternate number to notification service to send message to.
         * PGP-16398
         */
        if (QR_SUBSCRIPTION.equals(request.getBody().getType())) {
            workFlowRequestBean.getExtendInfo().setCurrentSubscriptionId(request.getBody().getSubscriptionId());
            String key = getSubscriptionKeyFromRedis(request.getBody().getSubscriptionId());
            String subsKey = "";
            SubscriptionResponse subscriptionResponse = null;
            if (key != null) {
                subsKey = key.split("[||].")[0];
                subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
            }
            if (subscriptionResponse != null) {
                workFlowRequestBean.getExtendInfo().setCustID(subscriptionResponse.getCustId());
            }

            if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                    && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                workFlowRequestBean.getExtendInfo().setActualMid(request.getBody().getMid());
            }
        }
        if (null != request.getBody().getPayeeVpa()) {
            Matcher prefixMatcher = UPI_QR_CODE_PREFIX_MATCHER.matcher(request.getBody().getPayeeVpa());
            QRCodeInfoBaseResponse qrResponse = null;
            String qrCodeId = null;
            if (prefixMatcher.matches()) {
                qrCodeId = StringUtils.substringBetween(request.getBody().getPayeeVpa(), prefixMatcher.group(1),
                        "@paytm");
            } else {
                qrCodeId = request.getBody().getPayeeVpa();
            }
            qrCodeId = qrCodeId.toUpperCase();
            EXT_LOGGER.customInfo("Getting QR info for QR Code Id {}", qrCodeId);
            workFlowRequestBean.getExtendInfo().setQrCodeId(qrCodeId);

            qrResponse = walletQRCodeService.getQRCodeInfoByQrCodeId(qrCodeId);
            if (qrResponse.getResponse() == null || !"SUCCESS".equalsIgnoreCase(qrResponse.getStatus())) {
                LOGGER.info("QR Response {} , status {}", qrResponse.getResponse(), qrResponse.getStatus());
                return;
            }
            EXT_LOGGER.customInfo("Adding POSID and MerchantContactNo in ExtendInfo");
            workFlowRequestBean.getExtendInfo().setPosId(qrResponse.getResponse().getPosId());
            workFlowRequestBean.getExtendInfo().setUdf1(qrResponse.getResponse().getPosId());
            workFlowRequestBean.getExtendInfo().setMerchantContactNo(qrResponse.getResponse().getMerchantContactNo());

        }
    }

    private void populateDataForRequest(UPIPSPRequest request, WorkFlowRequestBean workFlowRequestBean) {
        if (null != request.getBody().getPayeeVpa()
                && UPI_QR_CODE_PREFIX_MATCHER.matcher(request.getBody().getPayeeVpa()).matches()) {
            // This is later used to fail txns if UPI pcf is > 0 for static QR
            workFlowRequestBean.setStaticQrUpiPayment(true);
            LOGGER.info("UPI_QR_CODE flow, add REQUEST_TYPE");
            String reqTypeUPIQR = "REQUEST_TYPE:UPI_QR_CODE";
            if (null != workFlowRequestBean.getExtendInfo().getAdditionalInfo()) {
                workFlowRequestBean.getExtendInfo().setAdditionalInfo(
                        StringUtils.join(workFlowRequestBean.getExtendInfo().getAdditionalInfo(), "|", reqTypeUPIQR));
            } else {
                workFlowRequestBean.getExtendInfo().setAdditionalInfo(reqTypeUPIQR);
            }
        }
    }

    private boolean validateChecksum(UPIPSPRequest request) {

        if (!verifyJwtToken(request)) {
            LOGGER.error("JWT Validation failed returning response");
            return false;
        }
        EXT_LOGGER.customInfo("JWT validated successfully");
        return true;
    }

    private boolean verifyJwtToken(UPIPSPRequest request) {

        Map<String, String> jwtMap = new HashMap<>();
        jwtMap.put(TheiaConstant.UPIPSPKeys.MID, request.getBody().getMid());
        if (StringUtils.isNotBlank(request.getBody().getMobileNo())) {
            jwtMap.put(TheiaConstant.UPIPSPKeys.MOBILE_NO, request.getBody().getMobileNo());
        }
        if (StringUtils.isNotBlank(request.getBody().getOrderId())) {
            jwtMap.put(TheiaConstant.UPIPSPKeys.ORDER_ID, request.getBody().getOrderId());
        }
        jwtMap.put(TheiaConstant.UPIPSPKeys.PAYEE_VPA, request.getBody().getPayerVpa());
        jwtMap.put(TheiaConstant.UPIPSPKeys.REQUEST_TYPE, request.getBody().getRequestType());
        jwtMap.put(TheiaConstant.UPIPSPKeys.TXN_AMOUNT, request.getBody().getTxnAmount());

        return JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, request.getHeader().getSignature());
    }

    private PaymentRequestBean preparePaymentRequestbean(UPIPSPRequest request, HttpServletRequest httpServletRequest) {
        PaymentRequestBean payRequestBean = new PaymentRequestBean();
        payRequestBean.setRequest(httpServletRequest);
        payRequestBean.setMobileNo(request.getBody().getMobileNo());
        payRequestBean.setMid(request.getBody().getMid());
        payRequestBean.setOrderId(request.getBody().getOrderId());
        String key = getSubscriptionKeyFromRedis(request.getBody().getSubscriptionId());
        String requestType = "";
        if (key != null && QR_SUBSCRIPTION.equals(request.getBody().getType())) {
            requestType = key.split("[||].")[1];
            payRequestBean.setRequestType(requestType);
        } else {
            payRequestBean.setRequestType(request.getBody().getRequestType());
        }
        payRequestBean.setTxnAmount(request.getBody().getTxnAmount());
        payRequestBean.setPaymentDetails(request.getBody().getPayerVpa());
        payRequestBean.setVirtualPaymentAddr(request.getBody().getPayerVpa());
        payRequestBean.setPaymentTypeId(PaymentTypeIdEnum.UPI.name());
        payRequestBean.setCustId(request.getBody().getCustID());
        if (QR_SUBSCRIPTION.equals(request.getBody().getType())) {
            payRequestBean.setType(request.getBody().getType());
            payRequestBean.setSubscriptionID(request.getBody().getSubscriptionId());
        }
        if (null != request.getBody().getExtendInfo() && !request.getBody().getExtendInfo().isEmpty()) {
            payRequestBean.setAdditionalInfo(request.getBody().getExtendInfo()
                    .get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO));
            payRequestBean.setMerchUniqueReference(request.getBody().getExtendInfo().get(Native.MERC_UNQ_REF));
        }

        try {
            if (QR_SUBSCRIPTION.equals(request.getBody().getType())) {
                SubscriptionResponse subscriptionResponse = null;
                key = getSubscriptionKeyFromRedis(request.getBody().getSubscriptionId());
                String subsKey = "";
                if (key != null) {
                    subsKey = key.split("[||].")[0];
                    subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
                }
                if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                        && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                    payRequestBean.setAutoRefund(true);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while setting auto refund to true");
        }

        if (UPI_CREDIT_CARD.equals(request.getBody().getPayerPaymentInstrument())) {
            payRequestBean.setCCOnUPI(true);
        }

        setLinkId(payRequestBean);
        payRequestBean.setOrderPSPRequest(true);
        return payRequestBean;

    }

    private UPIPSPResponse prepareUpiPSPResponse(WorkFlowResponseBean workFlowResponseBean, long startTime) {
        UPIPSPResponse upiPSPResponse = new UPIPSPResponse();
        UPIPSPResponseHeader header = new UPIPSPResponseHeader();
        UPIPSPResponseBody body = new UPIPSPResponseBody();
        header.setResponseTimestamp(Long.valueOf(System.currentTimeMillis()));
        header.setVersion(TheiaConstant.UPIPSPKeys.VERSION);
        if (null == workFlowResponseBean.getUpiPSPResponse()) {
            body.setResultCode(UPIPSPEnum.FAIL.getResultCode());
            body.setResultCodeId(UPIPSPEnum.FAIL.getResultCodeId());
            body.setResultMsg(UPIPSPEnum.FAIL.getResultMsg());
            body.setExternalSerialNo("");
        } else {
            body = workFlowResponseBean.getUpiPSPResponse();
        }
        upiPSPResponse.setHead(header);
        upiPSPResponse.setBody(body);
        LOGGER.info("Reponse returned for UPIPSP is {} :", upiPSPResponse);
        EventUtils.logResponseCode(UPI_PSP_URI, EventNameEnum.RESPONSE_CODE_SENT, body.getResultCode(),
                body.getResultMsg());
        pushEventsLog(body, workFlowResponseBean.isBankFormOptimizedFlow(), startTime);
        return upiPSPResponse;
    }

    public boolean beanValidation(UPIPSPRequest upiPSPrequest) {

        final GenericFlowRequestBeanValidator<UPIPSPRequest> bean = new GenericFlowRequestBeanValidator<UPIPSPRequest>(
                upiPSPrequest);
        ValidationResultBean validationResultBean = bean.validate();
        if (!validationResultBean.isSuccessfullyProcessed()) {
            String failureDescription = StringUtils.isNotBlank(bean.getErrorMessage()) ? bean.getErrorMessage()
                    : "Validation Failed";
            LOGGER.error("Parameter Validation failed due to : {}", failureDescription);
            return false;
        }
        if (!ERequestType.SEAMLESS_3D_FORM.getType().equals(upiPSPrequest.getBody().getRequestType())
                && !ERequestType.isSubscriptionCreationRequest(upiPSPrequest.getBody().getRequestType())) {
            LOGGER.error("Request type passed in the request is invalid {}", upiPSPrequest.getBody().getRequestType());
            return false;
        }

        if (QR_SUBSCRIPTION.equals(upiPSPrequest.getBody().getType())) {
            String key = getSubscriptionKeyFromRedis(upiPSPrequest.getBody().getOrderId());
            if (key != null) {
                try {
                    String[] subscriptionRequestKeyArr = key.split("[||].");
                    String subscriptionMaxAmount = subscriptionRequestKeyArr[4];
                    String subsRenewalAmount = subscriptionRequestKeyArr[5];
                    String subsAmountType = subscriptionRequestKeyArr[6];
                    String subsKey = subscriptionRequestKeyArr[0];
                    SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil
                            .get(subsKey);

                    if (subscriptionResponse == null) {
                        LOGGER.error("Subscription response from redis is null for subsKey {} ", subsKey);
                        return false;
                    }
                    String subsTxnAmount = subscriptionResponse.getTxnAmount();

                    String maxAmount = "";
                    if (AmountType.FIX.getName().equals(subsAmountType)) {
                        if (StringUtils.isBlank(subsRenewalAmount)) {
                            maxAmount = subsTxnAmount;
                        } else {
                            maxAmount = subsRenewalAmount;
                        }
                    }

                    if (AmountType.VARIABLE.getName().equals(subsAmountType)) {
                        maxAmount = subscriptionMaxAmount;
                    }

                    if (StringUtils.isBlank(maxAmount)) {
                        LOGGER.error("maxAmount cant be empty {} ,{} ,{} ,{} ", subsAmountType, subscriptionMaxAmount,
                                subsRenewalAmount, subsTxnAmount);
                        return false;
                    }
                    if (StringUtils.isBlank(upiPSPrequest.getBody().getTxnAmount())) {
                        LOGGER.error("Txn Amount cant be empty");
                        return false;
                    }

                    if (Double.parseDouble(upiPSPrequest.getBody().getTxnAmount()) != Double.parseDouble(maxAmount)) {
                        LOGGER.error(
                                "Txn Amount passed in the request is mismatched with subsrenewal Amount or max Amount {} , {} ",
                                upiPSPrequest.getBody().getTxnAmount(), maxAmount);
                        return false;
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception in Parsing the subscriptionMaxamount ", e);
                }
            }
        }

        return true;
    }

    private void setMDC(String mid, String reqId, HttpServletRequest request) {
        MDC.clear();
        MDC.put(TheiaConstant.UPIPSPKeys.MID, mid);
        MDC.put(TheiaConstant.UPIPSPKeys.REQMSGID, reqId);
        String pgpId = request.getHeader(X_PGP_UNIQUE_ID);
        if (StringUtils.isNotBlank(pgpId)) {
            MDC.put(PGP_ID, pgpId);
        }
    }

    private void checkForRetry(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
            nativeRetryUtil.increaseRetryCount(workFlowRequestBean.getTxnToken(), requestData.getMid(),
                    requestData.getOrderId());

        } else {
            LOGGER.error("Retry count breached! orderPayUpiPspCOntroller");
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED).build();
        }
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseRetryCountBreached(String orderId,
            NativeFlowException nativeFlowException) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        UPIPSPResponseBody upiPspResponse = new UPIPSPResponseBody();
        upiPspResponse.setOrderId(orderId);
        upiPspResponse.setResultCode(UPIPSPEnum.FAIL.getResultCode());
        upiPspResponse.setResultCodeId(UPIPSPEnum.FAIL.getResultCodeId());
        upiPspResponse.setResultMsg(UPIPSPEnum.FAIL.getResultMsg());
        if (nativeFlowException.getResultInfo() != null) {
            upiPspResponse.setResultMsg(nativeFlowException.getResultInfo().getResultMsg());
        }
        workFlowResponseBean.setUpiPSPResponse(upiPspResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void checkIfMerchantInactiveOrBlocked(String mid) throws NativeFlowException {
        if (merchantExtendInfoUtils.isMerchantActiveOrBlocked(mid)) {
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.MERCHANT_BLOCKED).build();
        }
    }

    private String getSubscriptionKeyFromRedis(String orderId) {
        String key = (String) theiaTransactionalRedisUtil.get(workFlowHelper.getSubscriptionKey(orderId));
        return key;
    }

    private void updateUpiPSPResponseForQrSubscription(UPIPSPResponseBody response, UPIPSPRequest upiPSPrequest) {
        if (QR_SUBSCRIPTION.equals(upiPSPrequest.getBody().getType())) {
            response.setOrderId(upiPSPrequest.getBody().getSubscriptionId());
            LOGGER.info("upiPSPrequest.getBody().getOrderAmount() , {}", upiPSPrequest.getBody().getOrderAmount());
            response.setTxnAmount(upiPSPrequest.getBody().getOrderAmount());
        }
    }

    private void setLinkId(PaymentRequestBean paymentRequestBean) {
        String txnToken = nativeSessionUtil.getTxnToken(paymentRequestBean.getMid(), paymentRequestBean.getOrderId());
        if (StringUtils.isNotBlank(txnToken)) {
            String linkId = null;
            String invoiceId = null;
            EXT_LOGGER.customInfo("Initiate Token fetched from redis :{} ", txnToken);
            linkId = nativeSessionUtil.getLinkId(txnToken);
            invoiceId = nativeSessionUtil.getInvoiceId(txnToken);
            EXT_LOGGER.customInfo("LinkID for Redirection link UPI-PSP flow : {}", linkId);
            if (ff4JUtils.isFeatureEnabledOnMid(paymentRequestBean.getMid(), THEIA_ENABLE_LINK_FLOW_ON_DQR, false)
                    && (StringUtils.isBlank(linkId))) {
                String token = nativeSessionUtil.getMidOrderIdToken(paymentRequestBean.getMid(),
                        paymentRequestBean.getOrderId());
                EXT_LOGGER.customInfo("Initiate Token fetched from redis :{}", token);
                linkId = nativeSessionUtil.getLinkIdForQR(token);
                invoiceId = nativeSessionUtil.getInvoiceIdForQR(token);
                EXT_LOGGER.customInfo("LinkID for DQR UPI-PSP flow : {}", linkId);
            }
            if (StringUtils.isNotBlank(linkId)) {
                // To fetch and set linkId from redis for standard
                // Re-directional flow
                paymentRequestBean.setUpiIntentLinkId(linkId);
                LOGGER.info("Link_Id Fetched from redis is : {}", linkId);
            } else if (StringUtils.isNotBlank(invoiceId)) {
                // To fetch and set InvoiceId from redis for standard
                // Re-directional flow
                paymentRequestBean.setInvoiceId(invoiceId);
                LOGGER.info("Invoice_Id Fetched from redis is : {}", invoiceId);
            } else {
                // To fetch and set LinkId from OrderDetails for pure JS
                // checkout flow
                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
                if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                    InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                    if (orderDetail != null && orderDetail.getLinkDetailsData() != null
                            && StringUtils.isNotBlank(orderDetail.getLinkDetailsData().getLinkId())) {
                        paymentRequestBean.setUpiIntentLinkId(orderDetail.getLinkDetailsData().getLinkId());
                        LOGGER.info("Link_Id Fetched from Order Details is : {}",
                                paymentRequestBean.getUpiIntentLinkId());
                    }
                }
            }
        }
    }

    private void pushEventsLog(UPIPSPResponseBody responseBody, boolean isBankFormOptimizedFlow, long startTime) {
        try {
            LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
            metaData.put("api", UPI_PSP_URI);
            metaData.put("bankFormOptimizedFlow", String.valueOf(isBankFormOptimizedFlow));
            metaData.put("executionTime", String.valueOf(System.currentTimeMillis() - startTime));
            metaData.put("resultCode", responseBody.getResultCode());
            metaData.put("resultMsg", responseBody.getResultMsg());
            EventUtils.pushTheiaEvents(responseBody.getMid(), responseBody.getOrderId(),
                    EventNameEnum.UPI_PSP_ORDER_PAY, metaData);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while pushing Events Log for UPI PSP controller : {}", ex.getMessage());
        }
    }
}
