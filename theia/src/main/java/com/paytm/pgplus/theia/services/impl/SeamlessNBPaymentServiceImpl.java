package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author Kunal Maini
 * @createdOn 21-Feb-2018
 */
@SuppressWarnings("Duplicates")
@Service("seamlessNBPaymentService")
public class SeamlessNBPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = -3059884766173716078L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessNBPaymentServiceImpl.class);

    @Autowired
    @Qualifier("seamlessNBflow")
    private IWorkFlow seamlessNBflow;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("promoServiceHelper")
    private IPromoServiceHelper promoServiceHelper;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(final PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }

    @Override
    public PageDetailsResponse processPaymentRequest(final PaymentRequestBean requestData, final Model responseModel)
            throws TheiaServiceException {

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            return theiaResponseGenerator.getPageDetailsResponse(requestData, e.getResponseConstant());
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        setDataForPCF(workFlowRequestBean, requestData);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            return theiaResponseGenerator.getPageDetailsResponse(requestData, bizResponseBean.getResponseConstant());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                LOGGER.info("WorkFlowResponseBean is null, Reason : {}", bizResponseBean.getRiskRejectUserMessage());
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
                return theiaResponseGenerator.getPageDetailsResponse(requestData,
                        ResponseConstants.MERCHANT_RISK_REJECT);
            } else {
                throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                        + bizResponseBean.getFailureDescription());
            }
        }

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
        LOGGER.debug("Response page for seamless is : {}", responsePage);

        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);

        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), false);
        return new PageDetailsResponse(true);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, seamlessNBflow);
    }

    private void setDataForPCF(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData) {
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())
                || isSlabBasedMdr || isDynamicFeeMerchant)
                && !workFlowRequestBean.isNativeAddMoney()) {
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setDynamicFeeMerchant(isDynamicFeeMerchant);
        }
    }

}
