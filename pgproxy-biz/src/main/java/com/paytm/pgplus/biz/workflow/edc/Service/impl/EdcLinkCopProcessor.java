package com.paytm.pgplus.biz.workflow.edc.Service.impl;

import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.core.promo.PromoCheckoutFeatureFF4jData;
import com.paytm.pgplus.biz.exception.AccountMismatchException;
import com.paytm.pgplus.biz.exception.AccountNotExistsException;
import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.ICreateOrderAndPay;
import com.paytm.pgplus.biz.workflow.service.factory.PayAndCopImplEnum;
import com.paytm.pgplus.biz.workflow.service.helper.SimplifiedSubventionHelper;
import com.paytm.pgplus.biz.workflow.service.util.PaymentProcessorUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.link.EdcEmiChannelDetail;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class EdcLinkCopProcessor implements ICreateOrderAndPay {
    public static final Logger LOGGER = LoggerFactory.getLogger(EdcLinkCopProcessor.class);

    @Autowired
    @Qualifier("paymentProcessorUtil")
    private PaymentProcessorUtil paymentProcessorUtil;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderService;

    @Override
    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> processCopRequest(
            WorkFlowTransactionBean workFlowTransactionBean, boolean isRenewSubscriptionRequest) {

        CreateOrderAndPayRequestBean createOrderAndPayRequestBean = null;
        this.handlePayPreValidation(workFlowTransactionBean);
        try {
            createOrderAndPayRequestBean = paymentProcessorUtil.buildCopRequestBean(workFlowTransactionBean,
                    isRenewSubscriptionRequest);
        } catch (PaytmValidationException pve) {
            return paymentProcessorUtil.handleCopPaymentValidationException(pve);
        } catch (AccountMismatchException | AccountNotExistsException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception occured while creating createOrderAndPay request ", e);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
        GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = executeCop(
                createOrderAndPayRequestBean, workFlowTransactionBean);
        LOGGER.debug("Cop response is : {}", createOrderAndPayResponse);
        return handlePayPostValidation(createOrderAndPayResponse, workFlowTransactionBean);
    }

    @Override
    public String serviceType() {
        return PayAndCopImplEnum.EDL_LINK_PAYMENT_COP.getValue();
    }

    private void handlePayPreValidation(WorkFlowTransactionBean workFlowTransactionBean) {
        paymentProcessorUtil.applyEdcLinkTxnOffers(workFlowTransactionBean);
    }

    private GenericCoreResponseBean<CreateOrderAndPayResponseBean> executeCop(
            CreateOrderAndPayRequestBean createOrderAndPayRequestBean, WorkFlowTransactionBean workFlowTransactionBean) {
        enrichCopRequestBean(createOrderAndPayRequestBean, workFlowTransactionBean);
        paymentProcessorUtil.pushPayOrCopRequestToCheckoutKafka(createOrderAndPayRequestBean, workFlowTransactionBean
                .getWorkFlowBean().getPaytmMID());
        return orderService.createOrderAndPay(createOrderAndPayRequestBean);
    }

    private void enrichCopRequestBean(CreateOrderAndPayRequestBean createOrderAndPayRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean) {
        if ((EPayMethod.DEBIT_CARD.getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod())
                || EPayMethod.CREDIT_CARD.getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod()) || EPayMethod.EMI
                .getMethod().equals(workFlowTransactionBean.getWorkFlowBean().getPayMethod()))) {
            paymentProcessorUtil.enrichPayOrCopRequestBeanForInternationalCardPayment(createOrderAndPayRequestBean,
                    workFlowTransactionBean);
        }
        paymentProcessorUtil.setDirectChannelRequest(createOrderAndPayRequestBean, workFlowTransactionBean);
        paymentProcessorUtil.setMerchantVeloctyLimitInExtendInfo(workFlowTransactionBean.getWorkFlowBean());

    }

    private GenericCoreResponseBean<CreateOrderAndPayResponseBean> handlePayPostValidation(
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse,
            WorkFlowTransactionBean workFlowTransactionBean) {
        if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
            if (Boolean.TRUE.equals(workFlowTransactionBean.getWorkFlowBean().getEdcLinkTxn())
                    && workFlowTransactionBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null) {
                EdcEmiChannelDetail emiChannelDetail = workFlowTransactionBean.getWorkFlowBean()
                        .getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields().getEmiChannelDetail();
                if (CollectionUtils.isNotEmpty(emiChannelDetail.getOfferDetails())) {
                    paymentProcessorUtil.rollbackEdcLinkTxnBankBankOffer(workFlowTransactionBean.getWorkFlowBean());
                }
            }
            paymentProcessorUtil.rollbackMerchantVelocityLimit(workFlowTransactionBean);
            LOGGER.error("CreateOrderAndPay is failed due to reason : {}",
                    createOrderAndPayResponse.getFailureMessage());
            EventUtils.pushTheiaEvents(EventNameEnum.CREATE_ORDER_AND_PAY_FAILED, new ImmutablePair<>(
                    "CreateOrderAndPay is failed due to reason ", createOrderAndPayResponse.getFailureMessage()));
            return paymentProcessorUtil.handleCopRiskRejectOrRiskVerifyFailure(workFlowTransactionBean,
                    createOrderAndPayResponse);
        }
        LOGGER.info("Cop is successfully processed");
        return createOrderAndPayResponse;
    }

}
