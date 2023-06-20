/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.cachecard.service.impl;

import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.cachecard.service.ICacheCardInfoService;
import com.paytm.pgplus.biz.core.cachecard.utils.CacheCardInfoHelper;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.validator.service.ICardValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ROUTE_CACHE_CARD_FOR_ALL_PAYMODES;

@Service("cachecardinfoservice")
public class CacheCardInfoServiceImpl implements ICacheCardInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheCardInfoServiceImpl.class);

    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetFacade;

    @Autowired
    @Qualifier("seamlessvalidator")
    private ICardValidator seamlessvalidator;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CACHE_CARD_INFO)
    @Override
    public GenericCoreResponseBean<CacheCardResponseBean> cacheCardInfo(final CacheCardRequestBean cacheCardRequest,
            WorkFlowTransactionBean flowTransBean) {
        LOGGER.debug("CacheCardRequestBean {}: ", cacheCardRequest);
        try {
            GenericCoreResponseBean<CacheCardRequest> createCacheCard;
            if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                    || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                    || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
                if (null != cacheCardRequest && StringUtils.isBlank(flowTransBean.getWorkFlowBean().getCardIndexNo())
                        && !flowTransBean.getWorkFlowBean().isEcomTokenTxn()
                        && !flowTransBean.getWorkFlowBean().isCoftTokenTxn()) {
                    GenericCoreResponseBean<Boolean> validateCardNumber = seamlessvalidator
                            .validateCardNumber(cacheCardRequest);
                    if (!validateCardNumber.isSuccessfullyProcessed()) {
                        return new GenericCoreResponseBean<CacheCardResponseBean>(validateCardNumber
                                .getResponseConstant().getMessage(), validateCardNumber.getResponseConstant());
                    }

                    if (!ERequestType.CC_BILL_PAYMENT.equals(flowTransBean.getWorkFlowBean().getRequestType())
                            && (flowTransBean.getWorkFlowBean().getRequestType() != null && !ERequestType
                                    .isSubscriptionRenewalRequest(flowTransBean.getWorkFlowBean().getRequestType()
                                            .getType()))
                            && !ERequestType.MOTO_CHANNEL.equals(flowTransBean.getWorkFlowBean().getRequestType())
                            && !StringUtils
                                    .equalsIgnoreCase("true", flowTransBean.getWorkFlowBean().getiDebitEnabled())) {

                        GenericCoreResponseBean<Boolean> validateCvv = seamlessvalidator.validateCvv(cacheCardRequest);
                        if (!validateCvv.isSuccessfullyProcessed()) {
                            return new GenericCoreResponseBean<CacheCardResponseBean>(validateCvv.getResponseConstant()
                                    .getMessage(), validateCvv.getResponseConstant());
                        }

                    }

                    if (!ERequestType.CC_BILL_PAYMENT.equals(flowTransBean.getWorkFlowBean().getRequestType())) {
                        GenericCoreResponseBean<Boolean> validateExpiry = seamlessvalidator
                                .validateExpiry(cacheCardRequest);
                        if (!validateExpiry.isSuccessfullyProcessed()) {
                            return new GenericCoreResponseBean<CacheCardResponseBean>(validateExpiry
                                    .getResponseConstant().getMessage(), validateExpiry.getResponseConstant());
                        }
                    }

                }
                InstNetworkType instNetworkType;
                if (flowTransBean.getWorkFlowBean().isEcomTokenTxn()) {
                    instNetworkType = InstNetworkType.ECOMTOKEN;
                } else if (flowTransBean.getWorkFlowBean().isCoftTokenTxn()) {
                    instNetworkType = InstNetworkType.COFT;
                    cacheCardRequest.setStoreInCacheOnly(false);
                    String lastFourDigits = flowTransBean.getWorkFlowBean().getCardTokenInfo().getCardSuffix();
                    cacheCardRequest.setLast4ref(lastFourDigits);
                    modifyCacheCardRequestForCoft(cacheCardRequest, flowTransBean);
                } else {
                    instNetworkType = InstNetworkType.ISOCARD;
                }

                createCacheCard = CacheCardInfoHelper.createCacheCardRequestForCardPayment(cacheCardRequest,
                        instNetworkType);
            } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
                createCacheCard = CacheCardInfoHelper.createCacheCardForNB(cacheCardRequest);
            } else if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.BANK_MANDATE.value)
                    || PaymentTypeIdEnum.BANK_TRANSFER.value.equals(flowTransBean.getWorkFlowBean().getPaymentTypeId())) {
                createCacheCard = CacheCardInfoHelper.createCacheCardForMandate(cacheCardRequest);
            } else {
                createCacheCard = CacheCardInfoHelper.createCacheCardForIMPS(cacheCardRequest);
            }

            LOGGER.debug("Create Cache Card FacadeRequestBean ::{}", createCacheCard);
            if (!createCacheCard.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<CacheCardResponseBean>("FacadeCacheCardRequestCreationFailed",
                        ResponseConstants.SYSTEM_ERROR);
            }

            if (flowTransBean.getWorkFlowBean().getRoute().equals(Routes.PG2)
                    && (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                            || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value) || flowTransBean
                            .getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))
                    || ff4jUtils.isFeatureEnabledOnMid(flowTransBean.getWorkFlowBean().getPaytmMID(),
                            ENABLE_ROUTE_CACHE_CARD_FOR_ALL_PAYMODES, false)) {
                return workFlowHelper.fetchAssetIdforPG2Request(flowTransBean, createCacheCard.getResponse());
            }

            if (Routes.PG2.equals(flowTransBean.getWorkFlowBean().getRoute())
                    && (PaymentTypeIdEnum.BANK_TRANSFER.value
                            .equals(flowTransBean.getWorkFlowBean().getPaymentTypeId()))) {
                return workFlowHelper.fetchAssetIdforPG2BankTransferRequest(flowTransBean,
                        createCacheCard.getResponse());
            }

            coftUtil.updateCacheCardRequest(createCacheCard.getResponse());
            final CacheCardResponse cacheCardFacadeResponse = assetFacade.cacheCard(createCacheCard.getResponse());
            LOGGER.debug("Cache Card FacadeResponseBean ::{}", cacheCardFacadeResponse);
            if ((cacheCardFacadeResponse == null)
                    || !cacheCardFacadeResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
                final String errorMessage = cacheCardFacadeResponse == null ? "Invalid response received"
                        : cacheCardFacadeResponse.getBody().getResultInfo().getResultMsg();
                LOGGER.debug("Invalid Result returned for Cache Card::{}", errorMessage);
                ResponseConstants responseConstants = cacheCardFacadeResponse == null ? ResponseConstants.SYSTEM_ERROR
                        : ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                                cacheCardFacadeResponse.getBody().getResultInfo().getResultCodeId());
                return new GenericCoreResponseBean<CacheCardResponseBean>(errorMessage, responseConstants);
            }

            final CacheCardResponseBean cacheCardResponseBean = CacheCardInfoHelper
                    .mapCacheCardResponse(cacheCardFacadeResponse);
            modifyCacheCardResponseForCoft(flowTransBean, cacheCardFacadeResponse);
            LOGGER.debug("Cache Card Response Biz Bean ::{}", cacheCardResponseBean);
            return new GenericCoreResponseBean<CacheCardResponseBean>(cacheCardResponseBean);
        } catch (final Exception e) {
            LOGGER.error("Exception occured in cache card info: ", e);
            return new GenericCoreResponseBean<CacheCardResponseBean>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }

    }

    private void modifyCacheCardRequestForCoft(CacheCardRequestBean cacheCardRequest,
            WorkFlowTransactionBean flowTransBean) {
        cacheCardRequest.setGlobalPanIndex(flowTransBean.getWorkFlowBean().getGcin());
        cacheCardRequest.setPar(flowTransBean.getWorkFlowBean().getCardTokenInfo().getPanUniqueReference());
    }

    private void modifyCacheCardResponseForCoft(WorkFlowTransactionBean flowTransBean,
            CacheCardResponse cacheCardFacadeResponse) {
        if (StringUtils.isNotEmpty(cacheCardFacadeResponse.getBody().getGlobalPanIndex())) {
            if (flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                    || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                    || flowTransBean.getWorkFlowBean().getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
                flowTransBean.getWorkFlowBean().setGcin(cacheCardFacadeResponse.getBody().getGlobalPanIndex());
            }
        }
    }

}
