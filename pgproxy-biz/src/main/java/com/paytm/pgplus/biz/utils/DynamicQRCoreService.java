/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.enums.TransactionType;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.rabbitmq.notification.models.QROrderResponse;
import com.paytm.pgplus.theia.kafka.service.IKafkaService;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.WORKFLOW;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID;

@Service
public class DynamicQRCoreService {

    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRCoreService.class);
    public static final String TOPIC_POST_TRANSACTION = "DYNAMIC_QR";
    public static final String TOPIC_POST_PAYMENT = "DYNAMIC_QR";
    public static final String TOPIC_POST_ORDER = "CREATE_ORDER";

    @Autowired
    private IKafkaService kafkaService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    public TransactionInfo getTransInfo(WorkFlowTransactionBean workFlowTransBean) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setMid(workFlowTransBean.getWorkFlowBean().getPaytmMID());
        transactionInfo.setOrderId(workFlowTransBean.getWorkFlowBean().getOrderID());
        transactionInfo.setTransactionType(TransactionType.ACQUIRING);
        transactionInfo.setRequestType(workFlowTransBean.getWorkFlowBean().getRequestType().getType());
        transactionInfo.setPaymentId(workFlowTransBean.getCashierRequestId());
        transactionInfo.setPaymentMode(workFlowTransBean.getWorkFlowBean().getPayMethod());

        LOGGER.info("TransactionInfo : {}", transactionInfo);
        return transactionInfo;
    }

    // public void pushPostPaymentPayload(final WorkFlowTransactionBean
    // workFlowTransBean,
    // final WorkFlowRequestBean flowRequestBean) {
    // QROrderResponse qrOrderResponse = getQROrderDetails(workFlowTransBean,
    // flowRequestBean);
    // qrOrderResponse.setQrTimeout(ConfigurationUtil.getProperty("dynamicQrSessionTimeoutInMS",
    // "300000"));
    // qrOrderResponse.setTopicName(TOPIC_POST_PAYMENT);
    // qrOrderResponse.setStatus("PROCESSING");
    // LOGGER.info("Pushing data to KAFKA" + TOPIC_POST_PAYMENT);
    // pushInKafka(qrOrderResponse);
    // }

    private QROrderResponse getQROrderDetails(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowRequestBean flowRequestBean) {
        QROrderResponse qrOrderResponse = new QROrderResponse();
        qrOrderResponse.setAcquirementId(workFlowTransBean.getTransID());
        qrOrderResponse.setCashierRequestId(workFlowTransBean.getCashierRequestId());
        qrOrderResponse.setPaymentMode(workFlowTransBean.getWorkFlowBean().getPayMethod());
        qrOrderResponse.setMerchantId(flowRequestBean.getPaytmMID());
        qrOrderResponse.setOrderId(flowRequestBean.getOrderID());
        if (workFlowTransBean.getQueryPaymentStatus() != null) {
            qrOrderResponse.setStatus(workFlowTransBean.getQueryPaymentStatus().getPaymentStatusValue());
        }
        return qrOrderResponse;
    }

    public void pushInKafka(QROrderResponse qrOrderResponse) {
        LOGGER.info("Pushing in KAFKA with data : {}", qrOrderResponse);

        try {
            kafkaService.pushData(qrOrderResponse.getTopicName(), qrOrderResponse);
        } catch (Exception e) {
            LOGGER.error("Exception occurred in pushing payload in Kafka:", e);
        } finally {
            LOGGER.info("Pushed in KAFKA for topic : {}", qrOrderResponse.getTopicName());
        }
    }

    // public void pushPostTransactionPayload(WorkFlowTransactionBean
    // workFlowTransBean,
    // WorkFlowRequestBean flowRequestBean) {
    //
    // String payloadKey = "DYNAMIC_QR_KAFKA_PAYLOAD_" +
    // workFlowTransBean.getTransID();
    //
    // QROrderResponse qrOrderResponse = (QROrderResponse)
    // theiaTransactionalRedisUtil.get(payloadKey);
    //
    // if (qrOrderResponse == null) {
    // LOGGER.warn("Could not find QrOrderResponse data in cache , creating the same.");
    // qrOrderResponse = getQROrderDetails(workFlowTransBean, flowRequestBean);
    // } else {
    // if (workFlowTransBean.getQueryPaymentStatus() != null) {
    // qrOrderResponse.setStatus(workFlowTransBean.getQueryPaymentStatus().getPaymentStatusValue());
    // theiaTransactionalRedisUtil.set(payloadKey, qrOrderResponse);
    // }
    // }
    // qrOrderResponse.setQrTimeout(ConfigurationUtil.getProperty("dynamicQrSessionTimeoutInMS",
    // "480000"));
    // qrOrderResponse.setTopicName(TOPIC_POST_TRANSACTION);
    //
    // LOGGER.info("Pushing data to KAFKA : " + TOPIC_POST_TRANSACTION);
    // pushInKafka(qrOrderResponse);
    // }

    // public void pushPostTransactionPayload(String txnID, String paymentMode,
    // String orderId, String mid,
    // String cashierRequestId, String paymentStatus) {
    // String payloadKey = "DYNAMIC_QR_KAFKA_PAYLOAD_" + txnID;
    // LOGGER.info("PayloadKey : {}", payloadKey);
    //
    // QROrderResponse qrOrderResponse = (QROrderResponse)
    // theiaTransactionalRedisUtil.get(payloadKey);
    // LOGGER.info("QROrderResponse : {}", qrOrderResponse);
    //
    // if (qrOrderResponse != null) {
    // LOGGER.warn("Found QrOrderResponse data in cache");
    // } else {
    // LOGGER.warn("Could not find QrOrderResponse data in cache , creating the same.");
    // qrOrderResponse = new QROrderResponse();
    // qrOrderResponse.setCashierRequestId(cashierRequestId);
    // qrOrderResponse.setAcquirementId(txnID);
    // qrOrderResponse.setPaymentMode(paymentMode);
    // qrOrderResponse.setMerchantId(mid);
    // qrOrderResponse.setOrderId(orderId);
    // qrOrderResponse.setQrTimeout(ConfigurationUtil.getProperty("dynamicQrSessionTimeoutInMS",
    // "480000"));
    // }
    //
    // qrOrderResponse.setStatus(paymentStatus);
    // qrOrderResponse.setTopicName(TOPIC_POST_TRANSACTION);
    //
    // pushInKafka(qrOrderResponse);
    // }

    public void putQRDataINCache(WorkFlowTransactionBean workFlowTransBean, WorkFlowResponseBean workFlowResponseBean) {
        String txnId = workFlowResponseBean.getTransID();
        String txnKey = getTransTypeKey(txnId);

        try {
            theiaTransactionalRedisUtil.set(txnKey, getTransInfo(workFlowTransBean), 1200);
            theiaTransactionalRedisUtil.set(getPayQrResponseKey(txnId), workFlowResponseBean.getQueryPaymentStatus(),
                    1200);
            theiaTransactionalRedisUtil.set(getTransQrResponseKey(txnId),
                    workFlowResponseBean.getQueryTransactionStatus(), 1200);
        } catch (Exception e) {
            LOGGER.error("Unable to push data to REDIS", e);
        }
    }

    public void putTransTypeDataInCache(WorkFlowTransactionBean workFlowTransBean,
            WorkFlowResponseBean workFlowResponseBean) {
        String txnKey = Constants.TXN_TYPE_KEY_PREFIX + workFlowResponseBean.getTransID();
        TransactionInfo transInfo = getTransInfo(workFlowTransBean);

        LOGGER.info("Changing the request type to DYNAMIC_QR_TXN_STATUS");
        transInfo.setRequestType("DYNAMIC_QR_TXN_STATUS"); // set empty to avoid
                                                           // NPE at TSC call
        theiaTransactionalRedisUtil.set(txnKey, transInfo, 1200);

    }

    // public void pushPostOrderPayload(final WorkFlowTransactionBean
    // workFlowTransBean,
    // final WorkFlowRequestBean flowRequestBean) {
    // QROrderResponse qrOrderResponse = new QROrderResponse();
    // qrOrderResponse.setAcquirementId(workFlowTransBean.getTransID());
    // qrOrderResponse.setPaymentMode(workFlowTransBean.getWorkFlowBean().getPayMethod());
    // qrOrderResponse.setMerchantId(flowRequestBean.getPaytmMID());
    // qrOrderResponse.setOrderId(flowRequestBean.getOrderID());
    // qrOrderResponse.setQrTimeout(ConfigurationUtil.getProperty("dynamicQrSessionTimeoutInMS",
    // "480000"));
    // qrOrderResponse.setTopicName(TOPIC_POST_ORDER);
    // LOGGER.info("Pushing data to KAFKA " + TOPIC_POST_ORDER);
    // pushInKafka(qrOrderResponse);
    // }

    public GenericCoreResponseBean<WorkFlowResponseBean> cacheBankCardInfo(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {
        return seamlessCoreService.cacheBankCardInfo(flowRequestBean, workFlowTransBean);
    }

    public void cacheCardInRedis(WorkFlowRequestBean flowRequestBean, WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<UserDetailsBiz> userDetails, boolean isSavedCardTxn, boolean storeCard) {
        seamlessCoreService
                .cacheCardInRedis(flowRequestBean, workFlowTransBean, userDetails, isSavedCardTxn, storeCard);
    }

    public String getTransTypeKey(String transId) {
        return Constants.TXN_TYPE_KEY_PREFIX + transId;
    }

    public String getPayQrResponseKey(String transId) {
        return "PAY_QR_" + transId;
    }

    public String getTransQrResponseKey(String transId) {
        return "TRANS_QR_" + transId;
    }

    public void putCashierRequestIdAndPaymentTypeIdInCache(final WorkFlowTransactionBean workFlowTransBean) {
        String txnToken = getTxnToken(workFlowTransBean);

        HttpServletRequest httpServletRequest = AlipayRequestUtils.httpServletRequest();
        if (StringUtils.isNotBlank(txnToken) && httpServletRequest != null
                && httpServletRequest.getAttribute(WORKFLOW) != null
                && CHECKOUT.equals(httpServletRequest.getAttribute(WORKFLOW))) {
            theiaSessionRedisUtil.hsetIfExist(txnToken, CASHIER_REQUEST_ID, workFlowTransBean.getCashierRequestId());
            theiaSessionRedisUtil.hsetIfExist(txnToken, PAYMENT_TYPE_ID, workFlowTransBean.getWorkFlowBean()
                    .getPaymentTypeId());
        }
    }

    public String getTxnToken(final WorkFlowTransactionBean workFlowTransBean) {
        WorkFlowRequestBean flowRequestBean = workFlowTransBean.getWorkFlowBean();
        PaymentRequestBean paymentRequestBean = flowRequestBean.getPaymentRequestBean();

        String txnToken = "";

        if (StringUtils.isNotBlank(paymentRequestBean.getInitiateTransId())) {
            txnToken = paymentRequestBean.getInitiateTransId();
        } else {
            StringBuilder sb = new StringBuilder(NATIVE_TXN_INITIATE_REQUEST);
            sb.append(flowRequestBean.getPaytmMID()).append("_").append(flowRequestBean.getOrderID());

            txnToken = (String) theiaSessionRedisUtil.get(sb.toString());
        }

        return txnToken;
    }

}
