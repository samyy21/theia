/**
 *
 */
package com.paytm.pgplus.theia.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.mapping.models.MappingOuterResponse;
import com.paytm.pgplus.biz.mapping.models.MappingResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.models.TransientTxnModel;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;

/**
 * @author namanjain
 *
 */
@Service("requestGeneratorHelper")
public class FlowDataMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapper.class);

    private static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    public WorkFlowRequestBean createErrorFlowRequest(TransientTxnModel trasnTXNModel) {
        WorkFlowRequestBean errorFlowRequestBean = new WorkFlowRequestBean();
        errorFlowRequestBean.setPaytmMID(trasnTXNModel.getPaymentRequestModel().getMid());
        // errorFlowRequestBean.se
        return errorFlowRequestBean;
    }

    public static <T> T parseJsonData(String jsonData, Class<T> requiredResponseFormat) throws JsonParseException,
            JsonMappingException, IOException {
        LOGGER.info("JsonResponse {} & required format {}", jsonData, requiredResponseFormat);
        JavaType javaType = objectMapper.getTypeFactory().constructType(MappingOuterResponse.class,
                requiredResponseFormat);
        MappingOuterResponse<T> mappingOuterResponse = objectMapper.readValue(jsonData, javaType);
        if (mappingOuterResponse.getResponse() != null) {
            LOGGER.info(" ----> {}", mappingOuterResponse.getResponse().getBody().getResponse());

            final MappingResponse<T> mappingResponse = mappingOuterResponse.getResponse();

            if ((mappingResponse != null) && (mappingResponse.getBody() != null)
                    && (mappingResponse.getBody().getResponse() != null)) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                final T requiredResponseData = objectMapper.convertValue(mappingOuterResponse.getResponse().getBody()
                        .getResponse(), requiredResponseFormat);
                LOGGER.debug("Required response data format :: {}", requiredResponseData);
                return requiredResponseData;
            }
        }

        return null;
    }

    public WorkFlowRequestBean createCancelTransactionRequest(TransactionInfo txnData, MerchantInfo merchantInfo,
            PaymentRequestBean paymentRequestData, String closeReason) {
        return createCancelTransactionRequest(txnData.getTxnId(), merchantInfo.getInternalMid(), closeReason);
    }

    public WorkFlowRequestBean createTopupCancelTransactionRequest(TransactionInfo txnData, EnvInfoRequestBean envInfo) {
        return createTopupCancelTransactionRequest(txnData.getTxnId(), envInfo);
    }

    public WorkFlowRequestBean createCancelTransactionRequest(String txnId, String merchantId, String orderCloseReason) {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setTransID(txnId);
        workFlowRequestBean.setAlipayMID(merchantId);
        workFlowRequestBean.setCloseReason(orderCloseReason);
        workFlowRequestBean.setTransType(ETransType.ACQUIRING);
        return workFlowRequestBean;
    }

    public WorkFlowRequestBean createTopupCancelTransactionRequest(String txnId, EnvInfoRequestBean envInfo) {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setTransID(txnId);
        workFlowRequestBean.setEnvInfoReqBean(envInfo);
        workFlowRequestBean.setTransType(ETransType.TOP_UP);
        return workFlowRequestBean;
    }

}
