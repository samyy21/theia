package com.paytm.pgplus.biz.workflow.service.util;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.link.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.impl.LinkService;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LinkPaymentConsultUtil {

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Autowired
    private LinkService linkService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkPaymentConsultUtil.class);

    public PaymentConsultResponseBody getLinkPaymentConsultResponse(PaymentRequestBean paymentRequestData) {

        PaymentConsultRequestBody paymentConsultRequestBody = new PaymentConsultRequestBody();
        paymentConsultRequestBody.setMid(paymentRequestData.getMid());
        if (StringUtils.isNotBlank(paymentRequestData.getLinkId()))
            paymentConsultRequestBody.setLinkId(paymentRequestData.getLinkId());
        else if (StringUtils.isNotBlank(paymentRequestData.getInvoiceId()))
            paymentConsultRequestBody.setInvoiceId(paymentRequestData.getInvoiceId());
        else if (StringUtils.isNotBlank(paymentRequestData.getUpiIntentLinkId()))
            paymentConsultRequestBody.setLinkId(paymentRequestData.getUpiIntentLinkId());
        else if (StringUtils.isNotBlank(paymentRequestData.getDqrLinkId())) {
            paymentConsultRequestBody.setLinkId(paymentRequestData.getDqrLinkId());
        } else if (StringUtils.isNotBlank(paymentRequestData.getDqrInvoiceId())) {
            paymentConsultRequestBody.setInvoiceId(paymentRequestData.getDqrInvoiceId());
        }
        paymentConsultRequestBody.setOrderId(paymentRequestData.getOrderId());
        paymentConsultRequestBody.setAmount(paymentRequestData.getTxnAmount());
        try {
            return linkService.linkPaymentConsult(paymentConsultRequestBody);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception in calling linkPaymentConsult API", e);
        }
        return null;
    }
}