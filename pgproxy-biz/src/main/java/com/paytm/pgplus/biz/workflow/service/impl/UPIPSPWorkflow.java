package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.enums.UPIPSPEnum;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.UPIPSPResponseBody;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.biz.workflow.service.util.LinkPaymentConsultUtil;
import com.paytm.pgplus.cache.enums.TransactionType;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.common.model.link.PaymentConsultResponseBody;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.AOA_DQR;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.SUBSCRIPTION_MIN_AMOUNT;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPIPSPKeys.QR_SUBSCRIPTION;

/**
 * @author Santosh chourasia
 *
 */
@Service("UPIPSPWorkflow")
public class UPIPSPWorkflow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(UPIPSPWorkflow.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(UPIPSPWorkflow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    IMerchantDataService merchantDataService;

    @Autowired
    private IAcquiringOrder acquiringOrder;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    private WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private UPIPSPUtil uPIPSPUtil;

    @Autowired
    LinkPaymentConsultUtil linkPaymentConsultUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        workFlowTransBean.getWorkFlowBean().getEnvInfoReqBean().setTerminalType(ETerminalType.WAP);

        String cashierRequestId = null;
        String acquirementId = null;
        boolean orderExists = false;
        QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = null;
        try {

            SubscriptionResponse subscriptionResponse = null;
            if (QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
                String key = getSubscriptionKeyFromRedis(flowRequestBean.getSubscriptionID());
                String subsKey = "";
                if (key != null) {
                    subsKey = key.split("[||].")[0];
                    subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
                }
                if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                        && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                    flowRequestBean.getExtendInfo().setDummyMerchantId(subscriptionResponse.getPaymentMid());
                    flowRequestBean.getExtendInfo().setDummyOrderId(subscriptionResponse.getPaymentOrderId());
                    flowRequestBean.getExtendInfo().setPaytmMerchantId(subscriptionResponse.getPaymentMid());
                }
            }

            queryByMerchantTransIdResponse = queryByMerchantTransId(flowRequestBean,
                    ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType()));
            if (null != queryByMerchantTransIdResponse && null != queryByMerchantTransIdResponse.getBody()
                    && "S".equalsIgnoreCase(queryByMerchantTransIdResponse.getBody().getResultInfo().getResultStatus())) {
                orderExists = true;
                if (flowRequestBean.isPostConvenience()) {
                    Integer txnAmtInRequest = Integer.valueOf(workFlowTransBean.getWorkFlowBean().getTxnAmount());
                    Integer orderAmount = Integer.valueOf(queryByMerchantTransIdResponse.getBody().getAmountDetail()
                            .getOrderAmount().getAmount());
                    LOGGER.info("Updating txnAmount {} with orderAmount {} ", txnAmtInRequest, orderAmount);
                    workFlowTransBean.getWorkFlowBean().setTxnAmount(String.valueOf(orderAmount));
                    boolean isTxnAndChargeAmtValid = validateTxnAmtForPcfMerchant(workFlowTransBean,
                            queryByMerchantTransIdResponse, txnAmtInRequest);
                    if (!isTxnAndChargeAmtValid) {
                        LOGGER.error("Invalid txn or charge amount for PCF merchant");
                        return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                    }
                }
            } else {
                if (ff4JUtils.isFeatureEnabledOnMid(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                        BizConstant.AOA_2_PG_MIDS, false)) {

                    if (!uPIPSPUtil.orderPresentOnAOA(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                            flowRequestBean.getOrderID())) {
                        LOGGER.error("Dynamic QR Order does not exist on AOA. Aborting.");
                        return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                    }
                    LOGGER.info("Setting txnFlow = AOA_DQR in flowRequestBean");
                    flowRequestBean.setTxnFlow(AOA_DQR);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred during queryByMerchantTransId ", e);
            return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
        }

        // Consult link service incase of link payment
        if (ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_ENABLED, false)
                && workFlowTransBean.getWorkFlowBean() != null
                && workFlowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
                && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean()
                        .getUpiIntentLinkId())) {
            PaymentConsultResponseBody paymentConsultResponseBody = linkPaymentConsultUtil
                    .getLinkPaymentConsultResponse(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean());
            if (paymentConsultResponseBody == null) {
                LOGGER.info("Link Consult failure");
                return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
            } else if (paymentConsultResponseBody.getResultInfo() != null
                    && FacadeConstants.FAIL.equalsIgnoreCase(paymentConsultResponseBody.getResultInfo()
                            .getResultStatus())) {
                if (ResponseConstants.LINK_PAYMENT_ALREADY_PROCESSED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    LOGGER.info("Link Consult failure");
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                } else if (ResponseConstants.LINK_PAYMENT_IN_PROCESS.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    LOGGER.info("Link Consult failure");
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                } else if (ResponseConstants.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    LOGGER.info("Link Consult failure");
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                } else if (ResponseConstants.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    LOGGER.info("Link Consult failure");
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                } else {
                    LOGGER.info("Link Consult failure");
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                }
            }
        }

        if (orderExists) {
            acquirementId = queryByMerchantTransIdResponse.getBody().getAcquirementId();
            String requestType = null;
            if (flowRequestBean.isOrderPSPRequest()) {
                requestType = MapUtils.isNotEmpty(queryByMerchantTransIdResponse.getBody().getExtendInfo())
                        && StringUtils.isNotBlank(queryByMerchantTransIdResponse.getBody().getExtendInfo()
                                .get(TheiaConstant.UpiConfiguration.SP_REQUEST_TYPE)) ? queryByMerchantTransIdResponse
                        .getBody().getExtendInfo().get(TheiaConstant.UpiConfiguration.SP_REQUEST_TYPE) : null;
            }
            enhanceWorkFlowTransBean(flowRequestBean, workFlowTransBean, acquirementId, requestType);
            LOGGER.info("Pay called from UPIPSPWorkFlow");
            final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
            if (!payResponse.isSuccessfullyProcessed()) {
                return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
            }
            cashierRequestId = payResponse.getResponse().getCashierRequestID();
            workFlowTransBean.setCashierRequestId(cashierRequestId);
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            /**
             * Commented due to Issue with EDC
             * dynamicQRCoreService.pushPostPaymentPayload
             * (workFlowTransBean,flowRequestBean);
             */

            Map<String, String> extendInfo = queryByMerchantTransIdResponse.getBody().getExtendInfo();
            if (extendInfo != null
                    && (extendInfo.get("extraParamsMap.headAccount") != null || extendInfo
                            .get("extraParamsMap.remitterName") != null)) {
                if (flowRequestBean.getExtendInfo() != null) {
                    if (flowRequestBean.getExtendInfo().getExtraParamsMap().isEmpty())
                        flowRequestBean.getExtendInfo().setExtraParamsMap(new HashMap<>());
                    flowRequestBean.getExtendInfo().getExtraParamsMap()
                            .put("challanIdNum", workFlowHelper.generateCIN(TheiaConstant.RequestParams.BSR_CODE));
                    flowRequestBean.getExtendInfo().getExtraParamsMap()
                            .put("bsrCode", TheiaConstant.RequestParams.BSR_CODE);
                } else {
                    LOGGER.error("flowRequestBean.getExtendInfo() is null {}", flowRequestBean);
                }
            }

        } else {
            flowRequestBean.setRoute(null);
            boolean onus = flowRequestBean.getChannelInfo().containsValue(
                    BizConstant.ExtendedInfoKeys.MerchantTypeValues.ONUS);
            boolean offusOrderNotFound = flowRequestBean.isoffusOrderNotFound();
            if ((onus || offusOrderNotFound) && !AOA_DQR.equals(flowRequestBean.getTxnFlow()))
                return returnResponseForOrderNotFound(flowRequestBean, orderExists);
            if (flowRequestBean.isPostConvenience()) {
                LOGGER.info("Merchant is PCF , fetching fee details");
                GenericCoreResponseBean<ConsultFeeResponse> consultBossResponse = workFlowHelper
                        .consultBulkFeeResponseForPay(workFlowTransBean, EPayMethod.UPI);
                workFlowTransBean.setConsultFeeResponse(consultBossResponse.getResponse());
                workFlowTransBean.getWorkFlowBean().setChargeAmount(
                        workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(workFlowTransBean));
                if (!consultBossResponse.isSuccessfullyProcessed()
                        || checkIfPcfApplicableForStaticQr(workFlowTransBean.getWorkFlowBean())) {
                    return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
                }
            }
            LOGGER.info("createOrderAndPay called from UPIPSPWorkflow");
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);
            if (!createOrderAndPay.isSuccessfullyProcessed()) {
                LOGGER.error("CreateOrderAndPay failed due to : {}", createOrderAndPay.getFailureMessage());
                return returnResponseForOrderNotCreated(flowRequestBean, orderExists);
            }
            acquirementId = createOrderAndPay.getResponse().getAcquirementId();
            cashierRequestId = createOrderAndPay.getResponse().getCashierRequestId();
            if (createOrderAndPay.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(createOrderAndPay.getResponse().getSecurityPolicyResult()
                        .getRiskResult());
            }
        }

        SubscriptionResponse subscriptionResponse = null;

        if (flowRequestBean.getType() != null && flowRequestBean.getType().equals(QR_SUBSCRIPTION)) {
            LOGGER.info("inside fetching redis subscription response");
            String key = getSubscriptionKeyFromRedis(flowRequestBean.getSubscriptionID());
            String subsKey = "";
            if (key != null) {
                subsKey = key.split("[||].")[0];
                subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
            }
            EXT_LOGGER.info("subscription response is : {}", subscriptionResponse);
        }

        workFlowTransBean.setCashierRequestId(cashierRequestId);
        workFlowTransBean.setTransID(acquirementId);
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        dynamicQRCoreService.putCashierRequestIdAndPaymentTypeIdInCache(workFlowTransBean);

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        if (workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams() != null) {
            workFlowResponseBean.setBankFormOptimizedFlow(workFlowTransBean.getWorkFlowBean()
                    .getBankFormOptimizationParams().isBankFormOptimizedFlow());
        }
        GenericCoreResponseBean<WorkFlowResponseBean> genericWorkFlowResponse = fetchBankForm(workFlowTransBean,
                orderExists);
        UPIPSPResponseBody response = new UPIPSPResponseBody();
        String webFormContext = workFlowTransBean.getQueryPaymentStatus().getWebFormContext();
        MerchantVpaTxnInfo merchantVpaTxnInfo = null;

        String externalSerialNo = "";
        String merchantVpa = "";
        String mcc = "";
        try {
            if (StringUtils.isNotBlank(webFormContext)) {
                // webFormContext holds ESN in BankFormOptimizedFlow
                if (workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams() != null
                        && workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams()
                                .isBankFormOptimizedFlow()) {
                    externalSerialNo = webFormContext;
                } else {
                    merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext, MerchantVpaTxnInfo.class);
                    externalSerialNo = (null != merchantVpaTxnInfo) ? merchantVpaTxnInfo.getExternalSrNo() : null;
                    if (null != merchantVpaTxnInfo) {
                        merchantVpa = merchantVpaTxnInfo.getVpa();
                        mcc = merchantVpaTxnInfo.getMcc();
                    }
                }
            } else {
                LOGGER.error("Got empty webFormContext:");
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error while parsing json of webformcontext : {}", webFormContext);
        }
        if (StringUtils.isNotBlank(genericWorkFlowResponse.getFailureMessage())) {
            setUPIPSPResponse(UPIPSPEnum.FAIL, response, UPIPSPEnum.FAIL.getResultMsg());
        } else if (PaymentStatus.REDIRECT.name().equals(
                workFlowTransBean.getQueryPaymentStatus().getPaymentStatusValue())
                || (workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams() != null
                        && workFlowTransBean.getWorkFlowBean().getBankFormOptimizationParams()
                                .isBankFormOptimizedFlow() && StringUtils.isNotBlank(externalSerialNo))) {
            setUPIPSPResponse(UPIPSPEnum.SUCCESS, response, UPIPSPEnum.SUCCESS.getResultMsg());
        } else {
            setUPIPSPResponse(UPIPSPEnum.FAIL, response, UPIPSPEnum.FAIL.getResultMsg());
        }
        if (StringUtils.isBlank(externalSerialNo)) {
            response.setExternalSerialNo("");
        }
        response.setExternalSerialNo(externalSerialNo);

        if (flowRequestBean.getType() != null && QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
            response.setSubscriptionId("PAYTMSUBS" + externalSerialNo);
            response.setCallbackUrl(ConfigurationUtil.getProperty(BizConstant.QR_SUBSCRIPTION_CALLBACK_URL));
            try {
                if (subscriptionResponse != null) {
                    response.setDebitAmount(AmountUtils.getTransactionAmountInRupee(subscriptionResponse.getTxnAmount()));
                }
                if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                        && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                    BigDecimal subsMinAmount = new BigDecimal(ConfigurationUtil.getProperty(SUBSCRIPTION_MIN_AMOUNT,
                            "1")).setScale(2);
                    response.setDebitAmount(subsMinAmount.toString());
                }
            } catch (Exception e) {
                LOGGER.error("Exception in calculating in rupees ", e);
            }

            String key = getSubscriptionKeyFromRedis(flowRequestBean.getSubscriptionID());
            String[] subscriptionRequestKeyArr = key == null ? null : key.split("[||].");
            if (subscriptionRequestKeyArr != null && subscriptionRequestKeyArr.length >= 4
                    && ERequestType.NATIVE_MF_SIP.getType().equals(subscriptionRequestKeyArr[1])) {
                response.setPayerAccountDetails(subscriptionRequestKeyArr[3]);
                response.setTxnAllowed(Boolean.parseBoolean(subscriptionRequestKeyArr[2]));
            }
        }
        workFlowResponseBean.setMerchantVpa(merchantVpa);
        workFlowResponseBean.setMcc(mcc);

        if (workFlowTransBean.getWorkFlowBean().getType() != null
                && workFlowTransBean.getWorkFlowBean().getType().equals(QR_SUBSCRIPTION)) {
            updateUpiPSPResponseForQrSubscription(response, workFlowTransBean);
        } else {
            response.setOrderId(workFlowTransBean.getWorkFlowBean().getOrderID());
        }
        response.setRequestMsgId(workFlowTransBean.getWorkFlowBean().getUpiPspReqMsgId());
        response.setTxnAmount(getTxnAmountforResponse(workFlowTransBean.getWorkFlowBean(), orderExists));
        response.setMid(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        workFlowResponseBean.setUpiPSPResponse(response);
        setTransactionInfoInCache(workFlowTransBean);
        LOGGER.info("Returning Response Bean From UPIPSP request, trans Id : {} and cashierReqId : {}",
                workFlowTransBean.getTransID(), workFlowResponseBean.getCashierRequestId());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private String getSubscriptionKeyFromRedis(String subsId) {
        String key = (String) theiaTransactionalRedisUtil.get(workFlowHelper.getSubscriptionKey(subsId));
        return key;
    }

    private String getTxnAmountforResponse(final WorkFlowRequestBean flowRequestBean, boolean orderExistOnAlipay) {
        String txnAmountInPaise = null;
        if (flowRequestBean.isPostConvenience() && orderExistOnAlipay) {
            if (StringUtils.isNotBlank(flowRequestBean.getChargeAmount())
                    && StringUtils.isNotBlank(flowRequestBean.getTxnAmount())) {
                int orderAmount;
                if (null != flowRequestBean.getType() && QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
                    orderAmount = Integer.parseInt(flowRequestBean.getSubsMaxAmount());
                } else {
                    orderAmount = Integer.parseInt(flowRequestBean.getTxnAmount());
                }
                int chargeAmount = Integer.parseInt(flowRequestBean.getChargeAmount());
                txnAmountInPaise = String.valueOf(orderAmount + chargeAmount);
            } else {
                try {
                    if (null != flowRequestBean.getType() && QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
                        return flowRequestBean.getSubsMaxAmount();
                    } else {
                        txnAmountInPaise = flowRequestBean.getTxnAmount();
                    }
                } catch (Exception e) {
                    LOGGER.error("Converting in paise", e);
                }

                EXT_LOGGER.customInfo("Charge Amount is null");
            }
        } else {
            if (null != flowRequestBean.getType() && QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
                return flowRequestBean.getSubsMaxAmount();
            } else {
                txnAmountInPaise = flowRequestBean.getTxnAmount();
            }
        }
        return AmountUtils.getTransactionAmountInRupee(txnAmountInPaise);
    }

    private boolean validateTxnAmtForPcfMerchant(WorkFlowTransactionBean workFlowTransBean,
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse, Integer txnAmtInRequest) {
        if (null != workFlowTransBean && null != workFlowTransBean.getWorkFlowBean()) {
            Integer orderAmount = Integer.valueOf(queryByMerchantTransIdResponse.getBody().getAmountDetail()
                    .getOrderAmount().getAmount());

            LOGGER.info("Merchant is PCF , fetching fee details");
            GenericCoreResponseBean<ConsultFeeResponse> consultBulkFeeResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, EPayMethod.UPI);

            if (!consultBulkFeeResponse.isSuccessfullyProcessed()) {
                LOGGER.error("Fee consult failed due to : {} ", consultBulkFeeResponse.getFailureDescription());
                return false;
            }
            workFlowTransBean.setConsultFeeResponse(consultBulkFeeResponse.getResponse());
            String chargeAmount = workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(workFlowTransBean);
            workFlowTransBean.getWorkFlowBean().setChargeAmount(chargeAmount);
            Integer chargeAmt = Integer.valueOf(chargeAmount);
            Integer actualTxnAmount = orderAmount + chargeAmt;
            return txnAmtInRequest.equals(actualTxnAmount);
        }
        return false;
    }

    private boolean checkIfPcfApplicableForStaticQr(WorkFlowRequestBean workFlowBean) {
        if (workFlowBean != null && StringUtils.isNotBlank(workFlowBean.getChargeAmount())
                && (Double.parseDouble(workFlowBean.getChargeAmount()) > 0.0) && workFlowBean.isStaticQrUpiPayment()) {
            LOGGER.error("PCF > 0 for UPI for static QR txns , failing txn..");
            return true;
        }

        return false;
    }

    private void setTransactionInfoInCache(WorkFlowTransactionBean workFlowTransBean) {
        TransactionInfo transInfo = new TransactionInfo();
        transInfo.setTransactionType(TransactionType.ACQUIRING);
        transInfo.setPaymentMode(PayMethod.UPI.getMethod());
        transInfo.setMid(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        transInfo.setOrderId(workFlowTransBean.getWorkFlowBean().getOrderID());
        transInfo.setTransId(workFlowTransBean.getTransID());
        transInfo.setPaymentId(workFlowTransBean.getCashierRequestId());
        transInfo.setRequestType(ERequestType.SEAMLESS_3D_FORM.getType());
        if (null != workFlowTransBean.getWorkFlowBean().getRequestType()) {
            if (!ERequestType.isSubscriptionCreationRequest(workFlowTransBean.getWorkFlowBean().getRequestType()
                    .getType())) {
                transInfo.setRequestType(ERequestType.SEAMLESS_3D_FORM.getType());
            } else {
                transInfo.setRequestType(workFlowTransBean.getWorkFlowBean().getRequestType().getType());
            }
        }

        theiaTransactionalRedisUtil.set(Constants.TXN_TYPE_KEY_PREFIX + transInfo.getTransId(), transInfo);
    }

    private void enhanceWorkFlowTransBean(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean, String acquirementId, String requestType) {
        workFlowTransBean.setTransID(acquirementId);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setUserDetails(flowRequestBean.getUserDetailsBiz());
        if (StringUtils.isNotBlank(requestType)) {
            if (StringUtils.isNotBlank(flowRequestBean.getExtendInfo().getAdditionalInfo())) {
                if (flowRequestBean.getExtendInfo().getAdditionalInfo()
                        .contains(TheiaConstant.UpiConfiguration.REQUEST_TYPE)) {
                    String additionalInfo = flowRequestBean
                            .getExtendInfo()
                            .getAdditionalInfo()
                            .replaceAll("(?<a>REQUEST_TYPE:)(?<b>.[^\\|]*)(?<c>\\|.*|$)", "${a}" + requestType + "${c}");
                    flowRequestBean.getExtendInfo().setAdditionalInfo(additionalInfo);
                } else
                    flowRequestBean.getExtendInfo().setAdditionalInfo(
                            org.apache.commons.lang3.StringUtils.join(flowRequestBean.getExtendInfo()
                                    .getAdditionalInfo(), "|", TheiaConstant.UpiConfiguration.REQUEST_TYPE
                                    .concat(requestType)));
            } else {
                flowRequestBean.getExtendInfo().setAdditionalInfo(
                        TheiaConstant.UpiConfiguration.REQUEST_TYPE.concat(requestType));

            }

        }
        Map<String, String> channelInfo = flowRequestBean.getChannelInfo();
        if (null == channelInfo) {
            channelInfo = new HashMap<>();
            flowRequestBean.setChannelInfo(channelInfo);
        }
        if (null == channelInfo.get(BizConstant.BROWSER_USER_AGENT)) {
            if (null != flowRequestBean.getEnvInfoReqBean()) {
                channelInfo.put(BizConstant.BROWSER_USER_AGENT, flowRequestBean.getEnvInfoReqBean()
                        .getBrowserUserAgent());
            }
        }
        channelInfo.put(BizConstant.VIRTUAL_PAYMENT_ADDRESS, flowRequestBean.getVirtualPaymentAddress());
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setVirtualPaymentAddr(flowRequestBean.getVirtualPaymentAddress());
        buildDefaultChannelInfo(channelInfo);

        SubscriptionResponse subscriptionResponse = null;
        if (null != flowRequestBean.getRequestType()
                && ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())) {
            String key = getSubscriptionKeyFromRedis(flowRequestBean.getSubscriptionID());
            String subsKey = "";
            if (key != null) {
                subsKey = key.split("[||].")[0];
                subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
            }
            if (subscriptionResponse != null) {
                workFlowTransBean.setSubscriptionServiceResponse(subscriptionResponse);
            } else {
                LOGGER.error("Subscription Response from redis is null");
            }
        }
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForOrderNotCreated(
            final WorkFlowRequestBean flowRequestBean, boolean orderExistOnAlipay) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        UPIPSPResponseBody upiPspResponse = new UPIPSPResponseBody();
        upiPspResponse.setOrderId(flowRequestBean.getOrderID());
        upiPspResponse.setResultCode(UPIPSPEnum.FAIL.getResultCode());
        upiPspResponse.setResultCodeId(UPIPSPEnum.FAIL.getResultCodeId());
        upiPspResponse.setResultMsg(UPIPSPEnum.FAIL.getResultMsg());
        upiPspResponse.setTxnAmount(getTxnAmountforResponse(flowRequestBean, orderExistOnAlipay));
        upiPspResponse.setRequestMsgId(flowRequestBean.getUpiPspReqMsgId());
        upiPspResponse.setMid(flowRequestBean.getPaytmMID());
        workFlowResponseBean.setUpiPSPResponse(upiPspResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> returnResponseForOrderNotFound(
            final WorkFlowRequestBean flowRequestBean, boolean orderExistOnAlipay) {
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        UPIPSPResponseBody upiPspResponse = new UPIPSPResponseBody();
        upiPspResponse.setOrderId(flowRequestBean.getOrderID());
        upiPspResponse.setResultCode(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultCode());
        upiPspResponse.setResultCodeId(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultCodeId());
        upiPspResponse.setResultMsg(UPIPSPEnum.ORDER_NOT_FOUND_FAIL.getResultMsg());
        upiPspResponse.setTxnAmount(getTxnAmountforResponse(flowRequestBean, orderExistOnAlipay));
        upiPspResponse.setRequestMsgId(flowRequestBean.getUpiPspReqMsgId());
        upiPspResponse.setMid(flowRequestBean.getPaytmMID());
        workFlowResponseBean.setUpiPSPResponse(upiPspResponse);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> fetchBankForm(WorkFlowTransactionBean workFlowTransBean,
            boolean orderExistOnAlipay) {
        String customLooperTimeOut = ff4JUtils.getPropertyAsStringWithDefault(BizConstant.LOOPER_CUSTOM_TIMEOUT, null);
        if ("0".equals(customLooperTimeOut))
            customLooperTimeOut = null;
        workFlowTransBean.setCustomLooperTimeout(customLooperTimeOut);
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = workFlowHelper
                .fetchBankForm(workFlowTransBean);
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Error while fetching bankform due to : {}", queryPayResultResponse.getFailureMessage());
            GenericCoreResponseBean responseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                    queryPayResultResponse.getFailureMessage(), queryPayResultResponse.getResponseConstant());
            if (queryPayResultResponse.getResponse() != null) {
                responseBean.setInternalErrorCode(queryPayResultResponse.getResponse().getInstErrorCode());
            }
            return responseBean;
        }
        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        workFlowTransBean.setPaymentDone(false);
        // Need to close order in case paymentStatus is FAIL
        if (PaymentStatus.FAIL.toString().equals(queryPayResultResponse.getResponse().getPaymentStatusValue())) {
            LOGGER.info("Closing order due to payment status : {}", queryPayResultResponse.getResponse()
                    .getPaymentStatusValue());
            closeOrderAfterCheck(workFlowTransBean, orderExistOnAlipay);
        } else if (StringUtils.isBlank(queryPayResultResponse.getResponse().getWebFormContext())) {
            LOGGER.info("Closing order due to webFormContext is blank");
            closeOrderAfterCheck(workFlowTransBean, orderExistOnAlipay);
        }
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private void closeOrder(final WorkFlowTransactionBean workFlowTransBean) {
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }

    private void closeOrderAfterCheck(final WorkFlowTransactionBean workFlowTransBean, boolean orderExistOnAlipay) {
        if (ff4JUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED, false)
                && orderExistOnAlipay && nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean)) {
            LOGGER.info("Not closing order for dynamic QR  :: mid={} , orderId ={},orderExistOnAlipay = {} ",
                    workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                    workFlowTransBean.getWorkFlowBean().getOrderID(), orderExistOnAlipay);
            return;
        }
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
    }

    private void setUPIPSPResponse(UPIPSPEnum resultEnum, UPIPSPResponseBody body, String msg) {
        body.setResultCode(resultEnum.getResultCode());
        body.setResultCodeId(resultEnum.getResultCodeId());
        body.setResultMsg(msg);
    }

    private QueryByMerchantTransIdResponse queryByMerchantTransId(WorkFlowRequestBean flowRequestBean,
            boolean fromAOAMerchant) throws FacadeCheckedException {

        SubscriptionResponse subscriptionResponse = null;
        if (QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
            String key = getSubscriptionKeyFromRedis(flowRequestBean.getSubscriptionID());
            String subsKey = "";
            if (key != null) {
                subsKey = key.split("[||].")[0];
                subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(subsKey);
            }

            if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                    && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                flowRequestBean.setAlipayMID(subscriptionResponse.getPaymentMid());
                flowRequestBean.setOrderID(subscriptionResponse.getPaymentOrderId());
            }
        }

        QueryByMerchantTransIdRequestBody requestBody = new QueryByMerchantTransIdRequestBody(
                flowRequestBean.getAlipayMID(), flowRequestBean.getOrderID(), true, fromAOAMerchant);
        ApiFunctions apiFunction = ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID;
        if (fromAOAMerchant) {
            apiFunction = ApiFunctions.AOA_QUERY_BY_MERCHANT_TRANS_ID;
        }
        QueryByMerchantTransIdRequest request = new QueryByMerchantTransIdRequest(
                RequestHeaderGenerator.getHeader(apiFunction), requestBody);

        if (Routes.PG2.equals(workFlowRequestCreationHelper.getRoute(flowRequestBean, "queryByMerchantTransId"))) {
            request.getHead().setMerchantId(flowRequestBean.getPaytmMID());
            request.getBody().setRoute(Routes.PG2);
        }

        if (QR_SUBSCRIPTION.equals(flowRequestBean.getType())) {
            if (subscriptionResponse != null && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                    && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                request.getHead().setMerchantId(subscriptionResponse.getPaymentMid());
            }
        }

        QueryByMerchantTransIdResponse response = acquiringOrder.queryByMerchantTransId(request);
        return response;
    }

    private void buildDefaultChannelInfo(Map<String, String> channelInfo) {

        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARD_HOLDER_NAME,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.CARD_HOLDER_NAME);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.MOBILE_NO,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.MOBILE_NO);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2);

    }

    private void updateUpiPSPResponseForQrSubscription(UPIPSPResponseBody response,
            WorkFlowTransactionBean workFlowTransBean) {
        response.setOrderId(workFlowTransBean.getWorkFlowBean().getSubscriptionID());
        try {
            response.setTxnAmount(workFlowTransBean.getWorkFlowBean().getSubsMaxAmount());
        } catch (Exception e) {
            LOGGER.error("Exception in calculating in rupees {}", e);
        }

    }

}
