package com.paytm.pgplus.biz.workflow.edc.Service.impl;

import com.paytm.pgplus.biz.core.model.request.BizAoaPayRequest;
import com.paytm.pgplus.biz.core.model.request.BizPayRequest;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.core.promo.PromoCheckoutFeatureFF4jData;
import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.workflow.edc.Service.IEdcLinkService;
import com.paytm.pgplus.biz.workflow.model.BankEmiResponse;
import com.paytm.pgplus.biz.workflow.model.BrandEmiResponse;
import com.paytm.pgplus.biz.workflow.model.OfferCheckoutResponse;
import com.paytm.pgplus.biz.workflow.service.IPay;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.factory.PayAndCopImplEnum;
import com.paytm.pgplus.biz.workflow.service.helper.SimplifiedSubventionHelper;
import com.paytm.pgplus.biz.workflow.service.util.PaymentProcessorUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("edcLinkPayProcessor")
public class EdcLinkPayProcessor implements IPay {

    public static final Logger LOGGER = LoggerFactory.getLogger(EdcLinkPayProcessor.class);

    @Autowired
    @Qualifier("paymentProcessorUtil")
    private PaymentProcessorUtil paymentProcessorUtil;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("edcLinkServiceImpl")
    IEdcLinkService edcLinkService;

    @Override
    public GenericCoreResponseBean<BizPayResponse> processPayRequest(WorkFlowTransactionBean workFlowTransactionBean) {
        LOGGER.debug("WorkFlowTransactionBean : {}", workFlowTransactionBean);
        BizPayRequest payRequest = null;
        BizAoaPayRequest aoaPayRequest = null;
        handlePayPreValidation(workFlowTransactionBean);
        try {
            Object payRequestBean = paymentProcessorUtil.buildPaymentRequest(workFlowTransactionBean);
            if (payRequestBean instanceof BizAoaPayRequest)
                aoaPayRequest = (BizAoaPayRequest) payRequestBean;
            else
                payRequest = (BizPayRequest) payRequestBean;
        } catch (PaytmValidationException pve) {
            paymentProcessorUtil.rollbackMerchantVelocityLimit(workFlowTransactionBean);
            return new GenericCoreResponseBean<>(paymentProcessorUtil.getErrorMsgToReturnPaytmValidationExp(pve),
                    ResponseConstants.INVALID_PAYMENT_DETAILS);
        } catch (BaseException | MappingServiceClientException e) {
            LOGGER.error("Error occurred while creating Pay Request: {}", e);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
        GenericCoreResponseBean<BizPayResponse> payResponse = executePay(payRequest, aoaPayRequest,
                workFlowTransactionBean);
        LOGGER.debug("Pay response is : {}", payResponse);
        return handlePayPostValidation(payResponse, workFlowTransactionBean);

    }

    @Override
    public String serviceType() {
        return PayAndCopImplEnum.EDL_LINK_PAYMENT_PAY.getValue();
    }

    private void handlePayPreValidation(WorkFlowTransactionBean workFlowTransactionBean) {
        paymentProcessorUtil.setMerchantVeloctyLimitInExtendInfo(workFlowTransactionBean.getWorkFlowBean());
        paymentProcessorUtil.applyEdcLinkTxnOffers(workFlowTransactionBean);
    }

    private GenericCoreResponseBean<BizPayResponse> executePay(BizPayRequest payRequest,
            BizAoaPayRequest aoaPayRequest, WorkFlowTransactionBean workFlowTransactionBean) {

        if (workFlowTransactionBean.getWorkFlowBean().isFromAoaMerchant())
            return bizPaymentService.aoaPay(aoaPayRequest);
        else {
            enrichNonAoaPayRequestBean(payRequest, workFlowTransactionBean);
            paymentProcessorUtil.pushPayOrCopRequestToCheckoutKafka(payRequest, workFlowTransactionBean
                    .getWorkFlowBean().getPaytmMID());
            return bizPaymentService.pay(payRequest);
        }
    }

    private void enrichNonAoaPayRequestBean(BizPayRequest payRequest, WorkFlowTransactionBean workFlowTransactionBean) {
        if (EPayMethod.DEBIT_CARD.getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod())
                || EPayMethod.CREDIT_CARD.getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod())
                || EPayMethod.EMI.getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod())) {
            paymentProcessorUtil.enrichPayOrCopRequestBeanForInternationalCardPayment(payRequest,
                    workFlowTransactionBean);
        }
        paymentProcessorUtil.enrichPayRequestBeanForDirectChannelTxn(workFlowTransactionBean, payRequest);
    }

    private GenericCoreResponseBean<BizPayResponse> handlePayPostValidation(
            GenericCoreResponseBean<BizPayResponse> payResponse, WorkFlowTransactionBean workFlowTransactionBean) {
        if (!payResponse.isSuccessfullyProcessed()) {
            paymentProcessorUtil.rollbackMerchantVelocityLimit(workFlowTransactionBean);
            LOGGER.error("Pay Failed Due to Reason :{}", payResponse.getFailureMessage());
            return paymentProcessorUtil.handlePayRiskRejectOrRiskVerifyFailure(payResponse, workFlowTransactionBean);
        }
        LOGGER.info("Cashier pay is successfully processed");
        return payResponse;
    }

}
