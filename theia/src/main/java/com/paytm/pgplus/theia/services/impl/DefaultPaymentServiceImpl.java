/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeBaseRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PromoServiceConsants;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.PaymentOTPService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;

/**
 * @author kesari
 * @createdOn 27-Mar-2016
 */
@Service("defaultPaymentService")
public class DefaultPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = -3059884766173716078L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPaymentServiceImpl.class);

    @Autowired
    @Qualifier("defaultLoggedInFlow")
    private IWorkFlow defaultLoggedInFlow;

    @Autowired
    @Qualifier("defaultUserNotLoggedFlow")
    private IWorkFlow defaultUserNotLoggedFlow;

    @Autowired
    @Qualifier("buyerPaysChargeFlow")
    private IWorkFlow buyerPaysChargeFlow;

    @Autowired
    @Qualifier("buyerPaysChargeUserLoggedInFlow")
    private IWorkFlow buyerPaysChargeUserLoggedInFlow;

    @Autowired
    private PaymentOTPService paymentOTPUtil;

    @Autowired
    @Qualifier("promoServiceHelper")
    private IPromoServiceHelper promoServiceHelper;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("dynamicQRPreScanFlow")
    private IWorkFlow dynamicQRPreScanFlow;

    @Autowired
    @Qualifier("dynamicQRPreScanFlow2FAWithPCF")
    private IWorkFlow dynamicQRPreScanFlow2FAWithPCF;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("defaultLoggedInFlowLiteView")
    private IWorkFlow defaultLoggedInFlowLiteView;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(final PaymentRequestBean requestData, final Model responseModel)
            throws TheiaServiceException {
        validatePromoCode(requestData);

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("Error while getting workflow response bean due to : {}", tdme.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(null, tdme.getMessage(), null, true);

            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    tdme.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        LOGGER.debug("WorkFlowRequestBean  CREATED : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }
        if (!bizResponseBean.isSuccessfullyProcessed()) {
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant() != null ? bizResponseBean
                    .getResponseConstant().getCode() : null, bizResponseBean.getFailureDescription(), null, true);

            if (bizResponseBean.getResponseConstant() != null
                    && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
                LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to : {}",
                        bizResponseBean.getFailureDescription());
                String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                        bizResponseBean.getResponseConstant());

                PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                pageDetailsResponse.setSuccessfullyProcessed(false);
                pageDetailsResponse.setHtmlPage(htmlPage);
                return pageDetailsResponse;
            } else if (bizResponseBean.getFailureDescription() != null
                    && bizResponseBean.getFailureDescription().contains(
                            TheiaConstant.ResponseConstants.MERCHANT_LIMIT_BREACHED)) {
                throw new BizMerchantVelocityBreachedException(bizResponseBean.getFailureDescription());
            }

        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            failureLogUtil.setFailureMsgForDwhPush(
                    bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                            : null,
                    BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                            + bizResponseBean.getFailureDescription(), null, true);

            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                    + bizResponseBean.getFailureDescription());
        }

        bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData, workFlowResponseBean);
        LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(requestData.getRequest());
        // Hack
        if (ERequestType.DYNAMIC_QR_2FA == workFlowRequestBean.getRequestType()) {
            WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(requestData.getRequest(), false);
            if (walletInfo != null) {
                walletInfo.setWalletOnly(true);
                walletInfo.setWalletEnabled(true);
            }
        }

        /** Generate paymentOTP if user loggedin */
        try {
            if (loginInfo != null && loginInfo.isLoginFlag()) {
                paymentOTPUtil.generateIfPaymentOTP(requestData);
            }
        } catch (PaytmValidationException e1) {
            LOGGER.error("Exception In generating Payment OTP", e1);
        }

        retryServiceHelper.setRequestDataInCache(workFlowResponseBean.getTransID(), requestData);

        return new PageDetailsResponse(true);
    }

    /**
     * @param requestData
     */
    private void validatePromoCode(final PaymentRequestBean requestData) {
        if (StringUtils.isBlank(requestData.getPromoCampId())) {
            return;
        }

        if (StringUtils.isNotBlank(requestData.getPromoCampId())
                && ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                        TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT, false)) {
            return;
        }

        final PromoCodeResponse promoCodeResponse;
        LOGGER.info("Applying Promo code : {}", requestData.getPromoCampId());

        MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(requestData.getMid());

        if (mappingMerchantData != null) {
            requestData.setAlipayMerchantId(mappingMerchantData.getAlipayId());
        }

        final PromoCodeBaseRequest promoCodeBaseRequest = getPromoCodeRequestData(requestData);

        promoCodeResponse = promoServiceHelper.validatePromoCode(promoCodeBaseRequest);
        LOGGER.debug("Response received after validating promo code : {}", promoCodeBaseRequest.getPromoCode());

        if (promoCodeResponse == null
                || !ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse.getPromoResponseCode())) {
            requestData.setPromoCodeResponse(promoCodeResponse);
            requestData.setPromoCodeValid(false);
            LOGGER.debug("Final request data : {}", requestData);
            return;
        }

        promoCodeResponse.setCheckPromoValidityURL(PromoServiceConsants.PROMO_CHECK_VALIDITY_PATH.getValue());

        if (promoCodeResponse.getPromoCodeDetail() != null
                && !promoCodeResponse.getPromoCodeDetail().getPaymentModes().isEmpty()) {
            promoCodeResponse.setPaymentModes(StringUtils.join(
                    promoCodeResponse.getPromoCodeDetail().getPaymentModes(), ","));
        }

        if (promoCodeResponse.getPromoCodeDetail() != null
                && !promoCodeResponse.getPromoCodeDetail().getNbBanks().isEmpty()) {
            promoCodeResponse.setNbBanks(StringUtils.join(promoCodeResponse.getPromoCodeDetail().getNbBanks(), ","));
        }

        requestData.setPromoCodeResponse(promoCodeResponse);
        requestData.setPromoCodeValid(true);

        LOGGER.debug("Final request data : {}", requestData);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(final PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (!merchantSpecificValidation(requestData)) {
            return ValidationResults.MERCHANT_SPECIFIC_VALIDATION_FAILURE;
        }
        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private boolean merchantSpecificValidation(final PaymentRequestBean requestData) {
        if (ConfigurationUtil.getProperty("msedcl.mid", "").equals(requestData.getMid())) {
            if (StringUtils.isBlank(requestData.getMerchUniqueReference())) {
                return false;
            }
        }
        return true;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workFlow = fetchWorkflow(workFlowRequestBean);
        String defaultTaskFlowMids = "ALL";
        String pcfTaskFlowMids = ConfigurationUtil.getProperty("task.flow.pcf.mids", "NONE");
        String taskFlowMids = workFlowRequestBean.isPostConvenience() ? pcfTaskFlowMids : defaultTaskFlowMids;
        if (!dynamicQRPreScanFlow.equals(workFlow)
                && TaskFlowUtils.isMidEligibleForTaskFlow(workFlowRequestBean.getPaytmMID(), taskFlowMids)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            GenericCoreResponseBean<WorkFlowResponseBean> responseBean = taskExecutor.execute(workFlowRequestBean);
            stopWatch.stop();
            LOGGER.info("TASK Executor,Total time {}", stopWatch.getTotalTimeMillis());
            return responseBean;
        }
        return bizService.processWorkFlow(workFlowRequestBean, workFlow);
    }

    private IWorkFlow fetchWorkflow(final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workflow;
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        if (merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID()) || isSlabBasedMdr) {
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setPostConvenience(true);
            if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
                workflow = buyerPaysChargeUserLoggedInFlow;
            } else {
                workflow = buyerPaysChargeFlow;
            }

            boolean isQR2FAEnabledWithPCF = merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(workFlowRequestBean
                    .getPaytmMID());

            if (isQR2FAEnabledWithPCF) {

                workflow = dynamicQRPreScanFlow2FAWithPCF;
                workFlowRequestBean.setRequestType(ERequestType.DYNAMIC_QR_2FA);

                if (null != workFlowRequestBean.getExtendInfo()) {
                    workFlowRequestBean.getExtendInfo().setRequestType(ERequestType.DYNAMIC_QR_2FA.getType());
                }
            }

        } else {

            if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
                workflow = defaultLoggedInFlowLiteView;
            } else {
                workflow = defaultUserNotLoggedFlow;
            }

            boolean isQREnabled = merchantPreferenceService.isQRCodePaymentEnabled(workFlowRequestBean.getPaytmMID());
            if (isQREnabled) {

                workFlowRequestBean.setRequestType(ERequestType.DYNAMIC_QR);
                workflow = dynamicQRPreScanFlow;

                if (null != workFlowRequestBean.getExtendInfo()) {
                    workFlowRequestBean.getExtendInfo().setRequestType(ERequestType.DYNAMIC_QR.getType());
                }

            }

        }

        /*
         * private void setRequestDataInCache(String transId, PaymentRequestBean
         * requestData) { StringBuilder sb = new StringBuilder();
         * sb.append(RetryConstants.RETRY_PAYMENT_).append(transId);
         * RedisClientService.getInstance().set(sb.toString(), requestData); }
         */

        // final PromoCodeBaseRequest promoCodeBaseRequest =
        // getPromoCodeRequestData(requestData);

        return workflow;
    }

    private PromoCodeBaseRequest getPromoCodeRequestData(final PaymentRequestBean requestData) {
        PromoCodeBaseRequest promoCodeRequest = new PromoCodeBaseRequest();

        try {
            promoCodeRequest.setMerchantId(requestData.getAlipayMerchantId());
            promoCodeRequest.setPromoCode(requestData.getPromoCampId());
        } catch (Exception ex) {
            LOGGER.error("SYSTEM_ERROR : ", ex);
        }

        return promoCodeRequest;
    }
}