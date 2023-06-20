/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.constant.CommonConstant;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.cart.IPaymentValidationService;
import com.paytm.pgplus.facade.cart.model.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeamlessCoreService {
    public static final Logger LOGGER = LoggerFactory.getLogger(SeamlessCoreService.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Autowired
    @Qualifier("paymentValidationService")
    IPaymentValidationService paymentValidationService;

    public GenericCoreResponseBean<WorkFlowResponseBean> cacheBankCardInfo(WorkFlowRequestBean flowRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
            final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.SEAMLESS);
            if (!createCacheCardResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(createCacheCardResponse.getFailureMessage(),
                        createCacheCardResponse.getResponseConstant());
            }

            // validation for cart in enchanced native
            if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                    && flowRequestBean.isCartValidationRequired()) {
                try {
                    validatePaymentRequestFromCart(createCacheCardResponse.getResponse(), flowRequestBean);
                } catch (PaytmValidationException pve) {
                    // validation for cart failed
                    LOGGER.error("validation for cart failed due to: ", pve);
                    String failureMsg;

                    if (pve.getType() != null) {
                        failureMsg = pve.getType().getValidationFailedMsg();
                    } else {
                        failureMsg = pve.getMessage();
                    }
                    return new GenericCoreResponseBean<WorkFlowResponseBean>(failureMsg, ResponseConstants.SYSTEM_ERROR);
                }
            }

            workFlowTransBean.setCacheCardToken(createCacheCardResponse.getResponse().getTokenId());
            flowRequestBean.setCardIndexNo(createCacheCardResponse.getResponse().getCardIndexNo());
            if (workFlowHelper.isAddMoneyOnCCfeePhase2Enabled() && !flowRequestBean.isTxnFromCardIndexNo()) {
                addCardHash(flowRequestBean);
            }
            // Add card index number if isCardTokenRequired is true and Native
            // flow
            if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType()) && flowRequestBean.isCardTokenRequired()) {
                ExtendedInfoRequestBean extendedInfoRequestBean = null;
                if (null == workFlowTransBean.getWorkFlowBean()
                        || null == workFlowTransBean.getWorkFlowBean().getExtendInfo()) {
                    extendedInfoRequestBean = new ExtendedInfoRequestBean();
                } else {
                    extendedInfoRequestBean = workFlowTransBean.getWorkFlowBean().getExtendInfo();
                }
                if (!StringUtils.isEmpty(createCacheCardResponse.getResponse().getCardIndexNo())) {
                    extendedInfoRequestBean.setCardIndexNo(createCacheCardResponse.getResponse().getCardIndexNo());
                    extendedInfoRequestBean.setCardTokenRequired(flowRequestBean.isCardTokenRequired());
                }
                workFlowTransBean.getWorkFlowBean().setExtendInfo(extendedInfoRequestBean);
            }

        } else if (PaymentTypeIdEnum.NB.value.equals(flowRequestBean.getPaymentTypeId())
                && !(ERequestType.NATIVE_MF.equals(flowRequestBean.getRequestType())
                        || ERequestType.NATIVE_ST.equals(flowRequestBean.getRequestType())
                        || ERequestType.NATIVE_MF.equals(flowRequestBean.getSubRequestType()) || ERequestType.NATIVE_ST
                            .equals(flowRequestBean.getSubRequestType()))
                && StringUtils.isNotBlank(flowRequestBean.getAccountNumber())) {
            workFlowTransBean.getWorkFlowBean().setPaymentDetails(flowRequestBean.getAccountNumber());
            final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.STOCK_TRADING);
            if (!createCacheCardResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(createCacheCardResponse.getFailureMessage(),
                        createCacheCardResponse.getResponseConstant());
            }
            workFlowTransBean.setCacheCardToken(createCacheCardResponse.getResponse().getTokenId());
        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.BANK_MANDATE.value)) {
            GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                    workFlowTransBean, CacheCardType.MANDATE);
            if (!createCacheCardResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(createCacheCardResponse.getFailureMessage(),
                        createCacheCardResponse.getResponseConstant());
            }
            workFlowTransBean.setCacheCardToken(createCacheCardResponse.getResponse().getTokenId());
        }
        return null;
    }

    private void addCardHash(WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isBlank(flowRequestBean.getCardHash()) && StringUtils.isNotBlank(flowRequestBean.getCardNo())) {
            try {
                flowRequestBean.setCardHash(SignatureUtilWrapper.signApiRequest(flowRequestBean.getCardNo()));
            } catch (Exception e) {
                LOGGER.error("Error in setting cardHash:{}", e);
            }
        }
    }

    private void validatePaymentRequestFromCart(final CacheCardResponseBean cacheCardResponse,
            final WorkFlowRequestBean flowRequestBean) throws PaytmValidationException {

        if (StringUtils.isBlank(flowRequestBean.getToken())) {
            LOGGER.info("sso token is empty in request with isCartValidationRequired true");
            throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
        }

        if (cacheCardResponse != null && StringUtils.isNotBlank(cacheCardResponse.getCardIndexNo())
                && StringUtils.isNotBlank(flowRequestBean.getCardType())
                && StringUtils.isNotBlank(flowRequestBean.getOrderID())) {
            PaymentValidationRequest pvr = getPaymentValidationRequest(flowRequestBean.getCardType(),
                    cacheCardResponse.getCardIndexNo(), flowRequestBean.getOrderID());
            try {
                // LOGGER.info("validating request from cart");

                PaymentValidationResponse paymentValidationResponse = paymentValidationService.validate(pvr,
                        flowRequestBean.getToken());

                if (paymentValidationResponse == null || paymentValidationResponse.getStatus() != Status.success) {
                    if (StringUtils.isEmpty(paymentValidationResponse.getMessage())) {
                        throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
                    }
                    throw new PaytmValidationException(paymentValidationResponse.getCode(),
                            paymentValidationResponse.getMessage());
                }
                LOGGER.info("validation from cart successful");
            } catch (FacadeCheckedException e) {
                LOGGER.info("FacadeCheckedException occured : ");
                throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
            }
        }
    }

    private PaymentValidationRequest getPaymentValidationRequest(final String cardType, final String cardIndexNo,
            final String orderId) {
        PaymentDetail paymentDetail = new PaymentDetail(Method.fromCardType(cardType), cardIndexNo);
        List<PaymentDetail> paymentDetails = new ArrayList<>();
        paymentDetails.add(paymentDetail);
        return new PaymentValidationRequest(orderId, paymentDetails);
    }

    public void cacheCardInRedis(WorkFlowRequestBean flowRequestBean, WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<UserDetailsBiz> userDetails, boolean isSavedCardTxn, boolean storeCard) {

        if (workFlowTransBean.isPaymentDone())
            return;

        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
            /*
             * Encrypt card data & then caching card details in Redis & in
             * UserDetailsBiz also
             */
            if (!isSavedCardTxn && storeCard) {
                if (userDetails != null) {
                    userDetails = savedCardsService.cacheCardDetails(flowRequestBean, userDetails.getResponse(),
                            workFlowTransBean.getTransID());
                } else if (StringUtils.isNotBlank(flowRequestBean.getCustID())
                        && StringUtils.isNotBlank(flowRequestBean.getPaytmMID())) {
                    userDetails = savedCardsService.cacheCardDetails(flowRequestBean, null,
                            workFlowTransBean.getTransID());
                }
                if (userDetails != null && !userDetails.isSuccessfullyProcessed()) {
                    LOGGER.error("Exception occured while saving card details in cache: ",
                            userDetails.getFailureMessage());
                }
            }
        }
    }
}
