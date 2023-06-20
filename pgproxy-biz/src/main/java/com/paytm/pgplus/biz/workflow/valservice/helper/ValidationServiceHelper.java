package com.paytm.pgplus.biz.workflow.valservice.helper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnRequest;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnRequestBody;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.request.SecureRequestHeader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.*;

@Component("validationServiceHelper")
public class ValidationServiceHelper implements Serializable {
    @Autowired
    private Environment environment;

    public ValidationServicePreTxnRequest getValidationServicePreTxnRequest(
            WorkFlowTransactionBean workFlowTransactionBean) {
        ValidationServicePreTxnRequest request = new ValidationServicePreTxnRequest();
        SecureRequestHeader secureRequestHeader = new SecureRequestHeader();
        String clientId = ConfigurationUtil.getProperty(FacadeConstants.VALIDATION_SERVICE_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.VALIDATION_SERVICE_CLIENT_SECRET);
        secureRequestHeader.setClientId(clientId);
        secureRequestHeader.setSignature(clientSecret);
        request.setHead(secureRequestHeader);

        ValidationServicePreTxnRequestBody requestBody = new ValidationServicePreTxnRequestBody();
        requestBody.setMid(workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        if (StringUtils.isNotBlank(workFlowTransactionBean.getTransID())) {
            requestBody.setOrderId(workFlowTransactionBean.getTransID());
        } else if (StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getOrderID())) {
            requestBody.setOrderId(workFlowTransactionBean.getWorkFlowBean().getOrderID());
        }
        requestBody.setValidationMode(populateValidationMode(workFlowTransactionBean.getWorkFlowBean()));
        requestBody.setValidationInfo(populateValidationModelInfo(workFlowTransactionBean.getWorkFlowBean()));
        request.setBody(requestBody);
        return request;
    }

    private String populateValidationMode(WorkFlowRequestBean flowRequestBean) {
        String validationMode = null;
        if (flowRequestBean.getPaymentRequestBean() != null
                && flowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            final EdcEmiDetails edcEmiFields = flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getEdcEmiFields();
            if (edcEmiFields != null && StringUtils.isNotBlank(edcEmiFields.getValidationMode())) {
                validationMode = edcEmiFields.getValidationMode();
            }
        }
        return validationMode;
    }

    private ValidationModelInfo populateValidationModelInfo(WorkFlowRequestBean flowRequestBean) {
        ValidationModelInfo validationModelInfo = new ValidationModelInfo();
        if (flowRequestBean.getPaymentRequestBean() != null
                && flowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            final EdcEmiDetails edcEmiFields = flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getEdcEmiFields();
            if (edcEmiFields != null) {
                validationModelInfo.setClientInfo(populateClientInfo(edcEmiFields, flowRequestBean.getPaytmMID()));
                validationModelInfo.setProductInfo(populateProductInfo(edcEmiFields));
                validationModelInfo.setSerialInfo(populateSerialInfo(edcEmiFields));
                validationModelInfo.setTransactionInfo(populateEdcLinkTransactionInfo(edcEmiFields, flowRequestBean));
                validationModelInfo.setAdditionalDetails(populateEdcAdditionalDetails(edcEmiFields, flowRequestBean));
            }
        }
        return validationModelInfo;
    }

    private ClientInfo populateClientInfo(EdcEmiDetails edcEmiFields, String mid) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setKybId(edcEmiFields.getKybId());
        clientInfo.setStoreId(mid);
        clientInfo.setSourceContext(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.SOURCE_CONTEXT);
        return clientInfo;
    }

    private SerialInfo populateSerialInfo(EdcEmiDetails edcEmiFields) {
        SerialInfo serialInfo = new SerialInfo();
        serialInfo.setId(edcEmiFields.getValidationValue());
        serialInfo.setType(edcEmiFields.getValidationKey());
        return serialInfo;
    }

    private ProductInfo populateProductInfo(EdcEmiDetails edcEmiFields) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setBrandId(edcEmiFields.getBrandId());
        productInfo.setCategoryId(edcEmiFields.getCategoryId());
        productInfo.setProductId(edcEmiFields.getProductId());
        productInfo.setSkuCode(edcEmiFields.getSkuCode());
        productInfo.setBrandName(edcEmiFields.getBrandName());
        productInfo.setCategoryName(edcEmiFields.getCategoryName());
        productInfo.setProductName(edcEmiFields.getProductName());
        return productInfo;
    }

    private Map<String, String> populateEdcAdditionalDetails(EdcEmiDetails edcEmiFields,
            WorkFlowRequestBean flowRequestBean) {
        Map<String, String> additionalDetails = new HashMap<>();
        if (edcEmiFields != null && flowRequestBean != null) {
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.ORDERID, flowRequestBean.getOrderID());
            additionalDetails
                    .put(TheiaConstant.EdcEmiAdditionalFields.INVOICE_NO, edcEmiFields.getBrandInvoiceNumber());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TXN_AMOUNT, flowRequestBean.getTxnAmount());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.LOAN_AMOUNT, edcEmiFields.getLoanAmount());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.CARD_TYPE, edcEmiFields.getCardType());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.MID, flowRequestBean.getPaytmMID());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TID, flowRequestBean.getPaymentRequestBean()
                    .getLinkDetailsData().getMerchantReferenceId());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.ISSUER_BANK, edcEmiFields.getBankName());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.IS_APPLE_EXCHANGE_SUPPORTED, "true");
            if (edcEmiFields.getEmiChannelDetail() != null) {
                additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TENURE, edcEmiFields.getEmiChannelDetail()
                        .getEmiMonths());
            }
        }
        return additionalDetails;
    }

    private EdcLinkTransactionInfo populateEdcLinkTransactionInfo(EdcEmiDetails edcEmiFields,
            WorkFlowRequestBean flowRequestBean) {
        EdcLinkTransactionInfo transactionInfo = new EdcLinkTransactionInfo();
        String paymentType = null;
        transactionInfo.setTxnAmount(flowRequestBean.getTxnAmount());
        if (edcEmiFields.getEmiChannelDetail().getEmiMonths() != null
                && Integer.valueOf(edcEmiFields.getEmiChannelDetail().getEmiMonths()) > 0)
            paymentType = com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_EMI;
        else
            paymentType = com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_BANK_OFFER;

        transactionInfo.setTxnIdentifier(flowRequestBean.getTransID());
        transactionInfo.setUniqueIdentifier(flowRequestBean.getTransID());
        EdcLinkPaymentMethod.ExtraInfo extraInfo = new EdcLinkPaymentMethod.ExtraInfo(edcEmiFields.getBankCode(),
                edcEmiFields.getEmiChannelDetail().getEmiMonths());
        List<EdcLinkPaymentMethod> edcLinkPaymentMethods = Arrays.asList(new EdcLinkPaymentMethod(paymentType,
                extraInfo));
        transactionInfo.setPaymentMethod(edcLinkPaymentMethods);
        return transactionInfo;
    }

    public Map<String, String> prepareQueryParams(WorkFlowTransactionBean workFlowTransactionBean) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(BizConstant.MID, workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        return queryParams;
    }

    public MultivaluedMap<String, Object> prepareHeaderMap(WorkFlowTransactionBean workFlowTransactionBean) {
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add("content-type", MediaType.APPLICATION_JSON);
        headerMap.add("X-REQUEST-ID", UUID.randomUUID().toString());
        headerMap.add("X-CLIENT", "PG");
        headerMap.add("X-CLIENT-ID", workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        return headerMap;
    }

}
