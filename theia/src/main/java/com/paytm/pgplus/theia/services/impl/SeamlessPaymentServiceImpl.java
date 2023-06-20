/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.exception.BizSubventionOfferCheckoutException;
import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.PG2Utilities;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.impl.CoftPaymentService;
import com.paytm.pgplus.biz.workflow.service.impl.EcomTokenService;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.biz.workflow.service.util.LinkPaymentConsultUtil;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.LinkBasedMerchantInfo;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.bankForm.model.RiskContent;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.link.PaymentConsultResponseBody;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.CoftUtils;
import com.paytm.pgplus.pgproxycommon.utils.EcomTokenUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PromoCodeType;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.DccPaymentHelper;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPageMerchantInfo;
import com.paytm.pgplus.theia.nativ.model.enhancenative.*;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayOption;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.promo.IPromoHelper;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MerchantRedirectRequestException;
import com.paytm.pgplus.theia.offline.exceptions.PassCodeValidationException;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.s2s.utils.BankFormParser;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.session.utils.MerchantInfoSessionUtil;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.workflow.BMService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PUSH_APP_DATA;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TRANSACTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.LINK_BASED_PAYMENT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_NOT_SUPPORTED_MESSAGE;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@SuppressWarnings("Duplicates")
@Service("seamlessPaymentService")
public class SeamlessPaymentServiceImpl extends AbstractPaymentService {
    private static final long serialVersionUID = -7108619587740163359L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessPaymentServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SeamlessPaymentServiceImpl.class);

    @Autowired
    @Qualifier("seamlessflowservice")
    private IWorkFlow seamlessflowservice;

    @Autowired
    @Qualifier("upiPushFlowService")
    private IWorkFlow upiPushFlowService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    @Qualifier("promoHelperImpl")
    private IPromoHelper nativePromoHelper;

    @Autowired
    @Qualifier("upiPushExpressFlowService")
    private IWorkFlow upiPushExpressFlowService;

    @Autowired
    @Qualifier("enhancedCashierPageService")
    EnhancedCashierPageServiceImpl enhancedCashierPageService;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("pg2Utilities")
    private PG2Utilities pg2Utilities;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("nativeAddMoneyFlow")
    private IWorkFlow nativeAddMoneyFlow;

    @Autowired
    @Qualifier("merchantInfoSessionUtil")
    private MerchantInfoSessionUtil merchantInfoSessionUtil;

    @Autowired
    @Qualifier("bmService")
    private BMService bmService;

    @Autowired
    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Autowired
    private RiskVerificationUtil riskVerificationUtil;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    EcomTokenUtils ecomTokenUtils;

    @Autowired
    @Qualifier("ecomTokenService")
    EcomTokenService ecomTokenService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private DccPaymentHelper dccPaymentHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private BankFormParser bankFormParser;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    @Qualifier("bankTransferWorkflow")
    private IWorkFlow bankTransferWorkFlow;

    @Autowired
    @Qualifier("coftPaymentService")
    CoftPaymentService coftPaymentService;

    @Autowired
    CoftUtils coftUtils;

    @Autowired
    AOAUtils aoaUtils;

    // @Autowired
    // @Qualifier("aoabmService")
    // AOABMService aoabmService;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    LinkPaymentConsultUtil linkPaymentConsultUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel) {

        LOGGER.info("Processing payment request for seamless flow, order id :{}", requestData.getOrderId());

        setCallbackUrlForLink(requestData);
        boolean isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow = nativePaymentUtil
                .isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow(requestData);
        boolean isNativeJsonRequest = isNativeJsonRequest(requestData);

        try {
            if (isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow && !isBMCreation(requestData)) {
                try {
                    enhancedCashierPageService.setNativePaymentsDataForRetry(requestData);
                } catch (Exception e) {
                    LOGGER.info("processPaymentRequest threw exception to set retry data in native flow. {}", e);
                }
                return processNativeEnhanceSeamlessWorkflow(requestData);

            } else if (isNativeJsonRequest && !isBMCreation(requestData)) {
                try {
                    NativePaymentRequestBody nativePaymentRequestBody = retryServiceHelper
                            .getNativePaymentRequestBodyByRequest(requestData);
                    if (isCheckoutJsWebviewRequest(nativePaymentRequestBody, requestData)) {
                        retryServiceHelper.setNativeCheckOutJsPaymentsDataForRetry(requestData,
                                nativePaymentRequestBody);
                    }
                } catch (Exception e) {
                    LOGGER.error("processPaymentRequest threw exception to set retry data in native JSON flow. {}", e);
                }
                return processNativeJsonRequestSeamlessWorkflow(requestData);
            } else if (isBMCreation(requestData)) {
                if (aoaUtils.isAOAMerchant(requestData.getMid())) {
                    LOGGER.error("AOA subscription client call is being used");
                    // return aoabmService.createBM(requestData);
                }
                return bmService.createBM(requestData);
            } else {
                return processSeamlessWorkflow(requestData);
            }
        } catch (BizMerchantVelocityBreachedException e) {
            throw throwMerchantLimitBreachedException(e.getLimitType(), e.getLimitDuration(), requestData,
                    isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow, e);
        } catch (MerchantLimitBreachedException e) {
            throw throwMerchantLimitBreachedException(e.getLimitType(), e.getLimitDuration(), requestData,
                    isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow, e);
        } catch (BizPaymentOfferCheckoutException e) {
            NativeFlowException.ExceptionBuilder nfeBuilder = new NativeFlowException.ExceptionBuilder(
                    ResponseConstants.PAYMENT_OFFER_CHECKOUT_FAILURE);
            String promoTextMsg = getPromoOfferCheckoutFailureMessage(e);
            nfeBuilder.isRetryAllowed(true).setRetryMsg(promoTextMsg).setMsg(promoTextMsg);
            setRetryflag(isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow, isNativeJsonRequest, nfeBuilder);
            throw nfeBuilder.build();
        } catch (BizSubventionOfferCheckoutException e) {
            NativeFlowException.ExceptionBuilder nfeBuilder = new NativeFlowException.ExceptionBuilder(
                    ResponseConstants.SUBVENTION_CHECKOUT_FAILURE);
            String subventionTextMsg = getSubventionOfferCheckoutFailureMessage(e);
            nfeBuilder.isRetryAllowed(false).setRetryMsg(subventionTextMsg).setMsg(subventionTextMsg);
            setRetryflag(isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow, isNativeJsonRequest, nfeBuilder);
            throw nfeBuilder.build();
        } catch (EdcLinkBankAndBrandEmiCheckoutException e) {
            throw new EdcLinkBankAndBrandEmiCheckoutException(e.getMessage(), e.getResultCode());
        }
    }

    public boolean isCheckoutJsWebviewRequest(NativePaymentRequestBody nativePaymentRequestBody,
            PaymentRequestBean requestData) {
        return nativePaymentRequestBody != null && Native.CHECKOUT.equalsIgnoreCase(requestData.getWorkflow())
                && StringUtils.isNotBlank(nativePaymentRequestBody.getCheckoutJsConfig());
    }

    private void setRetryflag(boolean isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow,
            boolean isNativeJsonRequest, NativeFlowException.ExceptionBuilder nfeBuilder) {

        if (isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow) {
            nfeBuilder.isHTMLResponse(false);
        } else if (isNativeJsonRequest) {
            nfeBuilder.isNativeJsonRequest(true);
            nfeBuilder.isHTMLResponse(false);
        } else {
            nfeBuilder.isHTMLResponse(true);
        }
    }

    private void setCallbackUrlForLink(PaymentRequestBean requestData) {
        if ((StringUtils.isNotBlank(requestData.getLinkId()) || StringUtils.isNotBlank(requestData.getInvoiceId()))
                && requestData.getLinkDetailsData() == null) {
            String callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
            LOGGER.info("Setting callback url for Link {}", callbackUrl);
            if (StringUtils.isEmpty(callbackUrl)) {
                LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                throw new TheiaServiceException("CallbackUrl is not configured for offline request");
            }
            callbackUrl = callbackUrl + requestData.getOrderId();
            requestData.setCallbackUrl(callbackUrl);
        }
    }

    public PageDetailsResponse processNativeJsonRequestSeamlessWorkflow(PaymentRequestBean requestData) {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = createWorkFlowRequestBean(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("SYSTEM_ERROR : {}", tdme.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(tdme.getResponseConstant() != null ? tdme.getResponseConstant()
                    .getCode() : null, tdme.getMessage(), null, true);
            throwExceptionForNativeJsonRequest(tdme.getResponseConstant(), requestData);
        } catch (PaytmValidationException e) {
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CARD_NO.getCode(),
                    ResponseConstants.INVALID_CARD_NO.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_CARD_NO).isHTMLResponse(false)
                    .isRetryAllowed(true).setRetryMsg(CARD_NOT_SUPPORTED_MESSAGE).build();
        } catch (PaymentRequestValidationException e) {
            if (e.getResponseConstants() == ResponseConstants.INVALID_WALLET_2FA_PASSCODE) {
                failureLogUtil.setFailureMsgForDwhPush(e.getResponseConstants().getCode(), e.getResponseConstants()
                        .getMessage(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENT_DETAILS)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false)
                        .setMsg(ResponseConstants.INVALID_WALLET_2FA_PASSCODE.getMessage()).build();
            }
            throw e;
        }

        /*
         * check if Native retry is possible
         */
        checkForNativeRetry(requestData, workFlowRequestBean);

        setTransIdIfOrderCreatedDuringSubscriptionCreation(requestData, workFlowRequestBean);

        setTransIdInSessionForPreAuth(workFlowRequestBean);

        /*
         * for PCF Flow
         */
        setDataForPCF(workFlowRequestBean, requestData);

        /*
         * apply PromoCode
         */
        PromoCodeResponse promoCodeResponse = getPromoCodeResponse(requestData, workFlowRequestBean);
        setPromoCodeResponse(requestData, workFlowRequestBean, promoCodeResponse);

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && dccPaymentHelper.dccPageTobeRendered(workFlowRequestBean)) {
            NativeJsonResponse dccBankForm = dccPaymentHelper.getNativePlusJsonDccBankform(workFlowRequestBean);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
            pageDetailsResponse.setS2sResponse(getJsonString(dccBankForm));
            LOGGER.info("Final Response sent for dcc in native plus : {}", dccBankForm);
            return pageDetailsResponse;
        }

        workFlowRequestBean.setPG2PreferenceEnabledOnMid(true);
        workFlowRequestBean.setPG2EnabledPaymodesPayoptions(StringUtils.EMPTY);
        workFlowRequestBean.setFullPg2TrafficEnabled(true);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (bizResponseBean.getResponseConstant() != null
                && ResponseConstants.GV_CONSENT_REQUIRED.equals(bizResponseBean.getResponseConstant())) {
            return addMoneyToGvConsentUtil.showConsentPageForNativePlus(requestData.getMid(), requestData.getOrderId());
        }

        updateForPromoCode(promoCodeResponse, bizResponseBean, workFlowRequestBean, requestData);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (bizResponseBean != null
                && bizResponseBean.getResponseConstant() != null
                && (ResponseConstants.NEED_RISK_CHALLENGE.equals(bizResponseBean.getResponseConstant()) || ResponseConstants.RISK_VERIFICATION
                        .equals(bizResponseBean.getResponseConstant()))) {
            String transId = bizResponseBean.getAcquirementId();
            if (requestData.isNativeAddMoney() || StringUtils.isBlank(transId)) {
                transId = workFlowRequestBean.getTransID();
            }
            return riskVerificationUtil.handleRiskVerificationForNativePlus(requestData, transId);
        }
        boolean isSuccessfullyProcessedBizResponseBean = checkBizResponseBeanSuccessfullyProcessed(requestData,
                workFlowRequestBean, bizResponseBean);
        if (!isSuccessfullyProcessedBizResponseBean) {
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant() != null ? bizResponseBean
                    .getResponseConstant().getCode() : null, bizResponseBean.getFailureDescription(), null, true);
            throwExceptionForNativeJsonRequest(bizResponseBean.getResponseConstant(), requestData);
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                failureLogUtil.setFailureMsgForDwhPush(
                        bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                                : null,
                        BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                                + bizResponseBean.getRiskRejectUserMessage(), null, true);
                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRetryAllowed(true).setMsg(bizResponseBean.getRiskRejectUserMessage())
                        .build();
            }
            failureLogUtil.setFailureMsgForDwhPush(
                    bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                            : null,
                    BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                            + bizResponseBean.getFailureDescription(), null, true);
            throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant()).isHTMLResponse(false)
                    .setMsg(bizResponseBean.getFailureDescription()).build();
        }

        if (bizResponseBean.isSuccessfullyProcessed() && null != promoCodeResponse) {
            workFlowResponseBean.setPromoCodeResponse(promoCodeResponse);
        }

        if (isUpiPushExpressPaymentFailed(workFlowRequestBean, workFlowResponseBean)
                || checkRetryAllowedBasedOnPaymentStatus(workFlowRequestBean, workFlowResponseBean, requestData)) {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean,
                    ResponseConstants.PROCESS_FAIL);
        } else {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean, null);
        }

        // Correct Mapping of risk reject codes-PGP-8934
        if (bizResponseBean.getRiskRejectUserMessage() != null) {
            requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
        }

        setUPIPollDataForNativeJsonRequest(requestData, workFlowRequestBean, bizResponseBean);

        merchantInfoSessionUtil.setMerchantInfoIntoSession(requestData, workFlowResponseBean);

        boolean isFundOrder = requestData.isNativeAddMoney();
        String isUPIIntentPayment = workFlowRequestBean.isNativeDeepLinkReqd() ? "true" : "false";
        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), isFundOrder, requestData.getRequestType(), isUPIIntentPayment);

        /*
         * this is returning NativeJsonResponse since the request was also in
         * Json
         */
        NativeJsonResponse nativeJsonResponse = theiaResponseGenerator.getNativeJsonResponse(workFlowResponseBean,
                requestData, workFlowRequestBean);
        if (nativeJsonResponse != null && nativeJsonResponse.getBody() != null
                && workFlowResponseBean.getRiskResult() != null) {
            nativeJsonResponse.getBody().setRiskContent(new RiskContent());
            nativeJsonResponse.getBody().getRiskContent()
                    .setEventLinkId(workFlowResponseBean.getRiskResult().getEventLinkId());
        }

        setCacheDataForOtpInjectDirectForms(workFlowRequestBean, workFlowResponseBean.getCashierRequestId());

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setS2sResponse(getJsonString(nativeJsonResponse));
        pageDetailsResponse.setSuccessfullyProcessed(true);

        LOGGER.info("Final Response sent in NativeJsonResponse : {}", nativeJsonResponse);

        setCashierRequestIdInCache(workFlowRequestBean, workFlowResponseBean);
        setPaymentTypeIdInCache(workFlowRequestBean);
        setPaymentOptionInCache(workFlowRequestBean);
        invalidateSessionIfRequired(workFlowRequestBean, requestData, workFlowResponseBean, null);
        return pageDetailsResponse;
    }

    private void setTransIdInSessionForPreAuth(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean.isPreAuth()) {
            workFlowHelper.setTransIdForPreAuth(workFlowRequestBean);
            nativeSessionUtil.setTxnId(workFlowRequestBean.getTxnToken(), workFlowRequestBean.getTransID());
        }
    }

    private boolean isBMCreation(PaymentRequestBean requestData) {
        if ((ERequestType.isSubscriptionCreationRequest(requestData.getRequestType()))
                && SubsPaymentMode.valueOf(requestData.getSubsPaymentMode()) == SubsPaymentMode.BANK_MANDATE)
            return true;
        else
            return false;
    }

    private PageDetailsResponse processNativeEnhanceSeamlessWorkflow(PaymentRequestBean requestData) {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = createWorkFlowRequestBean(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("SYSTEM_ERROR : {}", tdme.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(tdme.getResponseConstant() != null ? tdme.getResponseConstant()
                    .getCode() : null, tdme.getMessage(), null, true);
            throw MerchantRedirectRequestException.getException();
        } catch (PaytmValidationException e) {
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CARD_NO.getCode(),
                    ResponseConstants.INVALID_CARD_NO.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_CARD_NO).isHTMLResponse(false)
                    .isRedirectEnhanceFlow(false).isRetryAllowed(true).setMsg(CARD_NOT_SUPPORTED_MESSAGE).build();

        } catch (PaymentRequestValidationException e) {
            if (e.getResponseConstants() == ResponseConstants.INVALID_WALLET_2FA_PASSCODE) {
                failureLogUtil.setFailureMsgForDwhPush(e.getResponseConstants().getCode(), e.getResponseConstants()
                        .getMessage(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENT_DETAILS)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false)
                        .setMsg(ResponseConstants.INVALID_WALLET_2FA_PASSCODE.getMessage()).build();
            }
            throw e;
        }

        if (ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(requestData.getRequestType())
                || ERequestType.NATIVE_MF_SIP.getType().equals(requestData.getRequestType())) {
            if (!nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
                LOGGER.error("Retry count breached for Native payment. Sending response to merchant");
                nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, null,
                        ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getCode(),
                        "Retry count breached for Native payment", null, true);

                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(true)
                        .setMsg(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage()).build();
            } else {
                if (!isAcquirementIdPresent(workFlowRequestBean)) {
                    // set transId from Cache for pay apply
                    workFlowRequestBean.setTransID(nativeRetryUtil.getTxnId(requestData.getTxnToken()));
                    // If first payment request , then set callback url in cache
                    // for
                    // txnToken
                    if (workFlowRequestBean.getNativeTotalPaymentCount() == 1) {
                        nativeRetryUtil.setCallbackUrl(requestData.getTxnToken(), workFlowRequestBean.getCallBackURL());
                    }
                }
            }
        }

        workFlowHelper.setTransIdForPreAuth(workFlowRequestBean);

        PromoCodeResponse promoCodeResponse = null;
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())) {
            promoCodeResponse = getPromoCodeResponse(requestData, workFlowRequestBean);
            setPromoCodeResponse(requestData, workFlowRequestBean, promoCodeResponse);
        }

        // setting pcf enabled/disbaled
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())
                || isSlabBasedMdr || isDynamicFeeMerchant)
                && !workFlowRequestBean.isNativeAddMoney()) {
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setDynamicFeeMerchant(isDynamicFeeMerchant);
        }

        if (!merchantPreferenceService.isAddMoneyPcfDisabled(workFlowRequestBean.getPaytmMID(), false)) {
            workFlowRequestBean.setAddMoneyPcfEnabled(true);
        }

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && dccPaymentHelper.dccPageTobeRendered(workFlowRequestBean)) {
            BankRedirectionDetail body = dccPaymentHelper.getNativeEnhanceJsonDccBankform(workFlowRequestBean);
            BankFormData bankFormData = new BankFormData(body);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
            pageDetailsResponse.setS2sResponse(getJsonString(bankFormData));
            LOGGER.info("Final Response sent for dcc in nativeEnhancedFlow : {}", bankFormData);
            return pageDetailsResponse;
        }
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        updateForPromoCode(promoCodeResponse, bizResponseBean, workFlowRequestBean, requestData);

        updateMerchantInfoInRedisForMFOrSTFlow(bizResponseBean, requestData);

        /**
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        BaseResponse response = null;
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()
                && !bizResponseBean.getResponseConstant().equals(ResponseConstants.INVALID_PAYMENT_DETAILS)) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, bizResponseBean.getResponse(),
                    bizResponseBean.getResponseConstant());
            if (bizResponseBean.getRiskRejectUserMessage() != null) {
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
            }

            if (bizResponseBean.getResponse() != null) {
                response = generateEnhancedResponse(requestData, workFlowRequestBean, bizResponseBean);
            }

        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (ResponseConstants.RISK_VERIFICATION.equals(bizResponseBean.getResponseConstant())) {
            return riskVerificationUtil.handleRiskVerificationForEnhance(requestData, workFlowRequestBean.getTransID());
        }

        if (workFlowResponseBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant() != null ? bizResponseBean
                    .getResponseConstant().getCode() : null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL,
                    null, true);
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                        .setMsg(bizResponseBean.getRiskRejectUserMessage()).build();

            } else if (null != bizResponseBean && null != bizResponseBean.getResponseConstant()) {
                if (bizResponseBean.getResponseConstant().equals(ResponseConstants.INVALID_PAYMENT_DETAILS)) {
                    com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
                    resultInfo.setRedirect(false);
                    resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
                    resultInfo.setResultMsg(bizResponseBean.getFailureDescription());
                    throw new PassCodeValidationException(resultInfo);
                }

                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRedirectEnhanceFlow(true).build();
            }
        }

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && bizResponseBean.isSuccessfullyProcessed() && null != promoCodeResponse) {
            workFlowResponseBean.setPromoCodeResponse(promoCodeResponse);
        }
        if (isUpiPushExpressPaymentFailed(workFlowRequestBean, workFlowResponseBean)
                || checkRetryAllowedBasedOnPaymentStatus(workFlowRequestBean, workFlowResponseBean, requestData)) {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean,
                    ResponseConstants.PROCESS_FAIL);

        } else {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean, null);
        }

        if (PaymentTypeIdEnum.UPI.getValue().equals(workFlowRequestBean.getPaymentTypeId())) {
            response = getResponseForUpiCollectOrIntent(response, requestData, workFlowRequestBean, bizResponseBean);
        }
        // Correct Mapping of risk reject codes-PGP-8934
        if (bizResponseBean.getRiskRejectUserMessage() != null) {
            requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
        }

        if (workFlowResponseBean.isPaymentDone()) {
            String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
            response = sendEnhancedNativeRedirectionResponse(responsePage, workFlowRequestBean, requestData,
                    workFlowResponseBean);
        } else {

            String responsePage = theiaResponseGenerator.getBankPage(workFlowResponseBean, requestData);
            LOGGER.debug("Response page for seamless is : {}", responsePage);

            if (!PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())) {
                response = sendEnhancedNativeRedirectionResponse(responsePage, workFlowRequestBean, requestData,
                        workFlowResponseBean);

                /*
                 * this is for wallet/cod/digital-credit... paymodes
                 */
                createResponseIfRetryAllowedForPaymodes(response, requestData, workFlowRequestBean);
            }

            if (isUpiPushExpressPaymentFailed(workFlowRequestBean, workFlowResponseBean)) {
                // LOGGER.info("Preparing response for Failed UPI Push Transaction");
                response = sendEnhancedNativeRedirectionResponse(responsePage, workFlowRequestBean, requestData,
                        workFlowResponseBean);

                response = sendEnhanceNativeRedirectResponseForUPIPush(response, requestData, workFlowRequestBean,
                        bizResponseBean);
                LOGGER.info("UPIPushResponse :{}", response);
            }
        }

        setCacheDataForOtpInjectDirectForms(workFlowRequestBean, workFlowResponseBean.getCashierRequestId());
        setCashierRequestIdInCache(workFlowRequestBean, workFlowResponseBean);
        setPaymentTypeIdInCache(workFlowRequestBean);
        setPaymentOptionInCache(workFlowRequestBean);
        invalidateSessionIfRequired(workFlowRequestBean, requestData, workFlowResponseBean, response);

        return getPageDetailsResponseAndCacheTransInfo(requestData, workFlowRequestBean, response, workFlowResponseBean);
    }

    public PageDetailsResponse getPageDetailsResponseAndCacheTransInfo(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, BaseResponse response, WorkFlowResponseBean workFlowResponseBean) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        try {
            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(response));
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            return pageDetailsResponse;
        }
        boolean isFundOrder = requestData.isNativeAddMoney();
        String upiIntentPayment = workFlowRequestBean.isNativeDeepLinkReqd() ? "true" : "false";
        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), isFundOrder, requestData.getRequestType(), upiIntentPayment);
        pageDetailsResponse.setSuccessfullyProcessed(true);
        return pageDetailsResponse;
    }

    private Map<String, String> setRequestAttributesForLinkPayments(PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean) {
        Map<String, String> data = new HashMap<>();
        QueryPaymentStatus paymentResponse = workFlowResponseBean.getQueryPaymentStatus();
        if (paymentResponse != null) {
            LOGGER.info("Generating final txn status page for Link based payments for transaction ID = {}",
                    paymentResponse.getTransId());

            data.put(TheiaConstant.ExtraConstants.REQUEST_TYPE, requestData.getRequestType());
            data.put(LINK_BASED_PAYMENT, "true");
            data.put(PAYMENT_STATUS, paymentResponse.getPaymentStatusValue().toUpperCase());
            Date date = paymentResponse.getPaidTime();
            if (date == null) {
                date = workFlowResponseBean.getQueryTransactionStatus().getCreatedTime();
            }
            data.put(TXN_DATE, LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
            data.put(TRANSACTION_ID, paymentResponse.getTransId());
            data.put(TXN_AMOUNT, paymentResponse.getTransAmountValue());
            data.put(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
            LOGGER.info("Fetching merchant info from Redis for Link based payment for  = {}",
                    paymentResponse.getTransId());
            LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                    .get(LINK_BASED_KEY + paymentResponse.getTransId());

            if (linkBasedMerchantInfo != null) {
                data.put(MERCHANT_NAME, linkBasedMerchantInfo.getMerchantName());
                data.put(MERCHANT_IMAGE, linkBasedMerchantInfo.getMerchantImage());
            }

            data.put(ORDER_ID, workFlowResponseBean.getQueryTransactionStatus().getMerchantTransId());
            ResponseCodeDetails respCodeDetails = null;
            String instErrorCode = workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode();
            String transactionStatus = getTransactionStatus(workFlowResponseBean, requestData);
            data.put(ERROR_MESSAGE,
                    theiaResponseGenerator.getResponseMessage(workFlowResponseBean, respCodeDetails, transactionStatus));
            data.put(ERROR_CODE,
                    theiaResponseGenerator.getResponseCode(instErrorCode, respCodeDetails, transactionStatus));
        }
        return data;
    }

    private String getTransactionStatus(WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData) {
        if (requestData.getRequestType().equals(ERequestType.OFFLINE.name())
                || requestData.getRequestType().equals(ERequestType.DYNAMIC_QR.name())) {
            return (MapperUtils.getTransactionStatusForResponseForOffline(
                    workFlowResponseBean.getQueryTransactionStatus(), workFlowResponseBean.getQueryPaymentStatus()));
        } else {
            return (MapperUtils.getTransactionStatusForResponse(workFlowResponseBean.getQueryTransactionStatus(),
                    workFlowResponseBean.getQueryPaymentStatus()));
        }
    }

    private PageDetailsResponse returnErrorResponseNative(PaymentRequestBean requestData, ResponseConstants rc) {
        String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, rc);
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(false);
        pageDetailsResponse.setHtmlPage(htmlPage);
        return pageDetailsResponse;
    }

    private WorkFlowRequestBean createWorkFlowRequestBean(PaymentRequestBean requestData)
            throws TheiaDataMappingException, PaytmValidationException {
        WorkFlowRequestBean workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        // Todo: check here for aoa emi
        if (ERequestType.NATIVE.name().equals(requestData.getRequestType())
                && (workFlowRequestBean.getCardNo() != null || workFlowRequestBean.getIsSavedCard())) {
            internatinalBinValidation(workFlowRequestBean, requestData.getTxnToken());
        }

        MerchantInfo merchantInfo = merchantInfoSessionUtil.getMerchantInfo(requestData, workFlowRequestBean,
                new WorkFlowResponseBean());
        workFlowRequestBean.setMerchantLogo(merchantInfo.getMerchantImage());

        setIfPRNEnabled(requestData, workFlowRequestBean);
        LOGGER.debug("WorkFlowRequestBean : {}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            if (nativePaymentUtil.isNativeEnhanceFlow(requestData)) {
                throw MerchantRedirectRequestException
                        .getException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
            } else if (isNativeJsonRequest(requestData)) {
                throwExceptionForNativeJsonRequest(ResultCode.FAILED, requestData);
            } else {
                throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
            }
        }
        return workFlowRequestBean;
    }

    private PageDetailsResponse processSeamlessWorkflow(PaymentRequestBean requestData) {
        WorkFlowRequestBean workFlowRequestBean = null;
        PageDetailsResponse response = null;

        try {
            workFlowRequestBean = createWorkFlowRequestBean(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : {}", e.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(e.getResponseConstant() != null ? e.getResponseConstant().getCode()
                    : null, e.getMessage(), null, true);
            return returnErrorResponseNative(requestData, e.getResponseConstant());
        } catch (PaytmValidationException e) {
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CARD_NO.getCode(),
                    ResponseConstants.INVALID_CARD_NO.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_CARD_NO).isHTMLResponse(true)
                    .isRetryAllowed(true).setRetryMsg(CARD_NOT_SUPPORTED_MESSAGE).build();
        } catch (PaymentRequestValidationException e) {
            if (e.getResponseConstants() == ResponseConstants.INVALID_WALLET_2FA_PASSCODE) {
                failureLogUtil.setFailureMsgForDwhPush(e.getResponseConstants().getCode(), e.getResponseConstants()
                        .getMessage(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENT_DETAILS)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false)
                        .setMsg(ResponseConstants.INVALID_WALLET_2FA_PASSCODE.getMessage()).build();
            }
            throw e;
        }

        /*
         * check if Native retry is possible
         */
        response = checkForNativeRetry(requestData, workFlowRequestBean);
        if (response != null) {
            /*
             * if response is not null, here, it means its an error!
             */
            return response;
        }
        setTransIdIfOrderCreatedDuringSubscriptionCreation(requestData, workFlowRequestBean);

        // For PCF Flow
        setDataForPCF(workFlowRequestBean, requestData);

        // apply promo with temporary mid|orderId
        PromoCodeResponse promoCodeResponse = null;
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                || ERequestType.SEAMLESS.getType().equals(requestData.getRequestType())) {
            promoCodeResponse = getPromoCodeResponse(requestData, workFlowRequestBean);
            response = setPromoCodeResponse(requestData, workFlowRequestBean, promoCodeResponse);
            if (response != null) {
                /*
                 * if response is not null, here, it means its an error!
                 */
                return response;
            }
        }
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && dccPaymentHelper.dccPageTobeRendered(workFlowRequestBean)) {
            String dccHtmlPage = ConfigurationUtil.getDccPage();
            try {
                String dccPageData = JsonMapper.mapObjectToJson(workFlowRequestBean.getDccPageData());
                if (StringUtils.isNotBlank(dccPageData)) {
                    LOGGER.info("PUSH_APP_DATA for Native dcc {}", dccPageData);
                    dccHtmlPage = dccHtmlPage.replace(PUSH_APP_DATA, dccPageData);
                }
            } catch (Exception e) {
                LOGGER.error("Something bad happennned");
            }
            requestData.getRequest().setAttribute(TheiaConstant.DccConstants.DCC_PAGE_RENDER, true);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(true);
            pageDetailsResponse.setHtmlPage(dccHtmlPage);
            return pageDetailsResponse;
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (bizResponseBean.getResponseConstant() != null
                && ResponseConstants.GV_CONSENT_REQUIRED.equals(bizResponseBean.getResponseConstant())) {
            return addMoneyToGvConsentUtil.showConsentPageForRedirection(requestData.getMid(),
                    requestData.getOrderId(), false);
        }

        updateForPromoCode(promoCodeResponse, bizResponseBean, workFlowRequestBean, requestData);

        /**
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */

        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }

        // to handle risk reject cases in offline flow
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getRiskRejectUserMessage() != null
                && (ERequestType.OFFLINE.getType().equals(requestData.getRequestType()))) {

            LOGGER.error("Risk reject in offline pay flow");
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant() != null ? bizResponseBean
                    .getResponseConstant().getCode() : null, bizResponseBean.getFailureDescription(), null, true);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant(), bizResponseBean.getRiskRejectUserMessage());
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        if (!bizResponseBean.isSuccessfullyProcessed()
                && bizResponseBean.getResponseConstant() != null
                && (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                        || ERequestType.NATIVE_MF.getType().equals(requestData.getRequestType())
                        || ERequestType.NATIVE_MF.getType().equals(requestData.getSubRequestType())
                        || ERequestType.NATIVE_ST.getType().equals(requestData.getSubRequestType())
                        || bizResponseBean.getResponseConstant().isResponseToMerchant() || ERequestType.NATIVE_ST
                        .getType().equals(requestData.getRequestType()))) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            if (ResponseConstants.NEED_RISK_CHALLENGE.equals(bizResponseBean.getResponseConstant())
                    || ResponseConstants.RISK_VERIFICATION.equals(bizResponseBean.getResponseConstant())) {
                String transId = bizResponseBean.getAcquirementId();

                if (requestData.isNativeAddMoney() || StringUtils.isBlank(transId)) {
                    transId = workFlowRequestBean.getTransID();
                }
                return riskVerificationUtil.handleRiskVerificationForRedirection(requestData, transId);
            }
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, bizResponseBean.getResponse(),
                    bizResponseBean.getResponseConstant());
            if (bizResponseBean.getRiskRejectUserMessage() != null) {
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
            }
            PageDetailsResponse pageResponse = handleRiskVerifiedEnhanceRetry(requestData);
            if (pageResponse != null) {
                return pageResponse;
            }
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant());
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            pageDetailsResponse.setData(new HashMap<>());
            pageDetailsResponse.getData().put(TheiaConstant.ResponseConstants.RESPONSE_CODE,
                    bizResponseBean.getResponseConstant().getAlipayResultMsg());
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                failureLogUtil.setFailureMsgForDwhPush(
                        bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                                : null,
                        BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                                + bizResponseBean.getRiskRejectUserMessage(), null, true);
                throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                        + bizResponseBean.getRiskRejectUserMessage());
            }
            failureLogUtil.setFailureMsgForDwhPush(
                    bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                            : null,
                    BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                            + bizResponseBean.getFailureDescription(), null, true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                    + bizResponseBean.getFailureDescription());
        }

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && bizResponseBean.isSuccessfullyProcessed() && null != promoCodeResponse) {
            workFlowResponseBean.setPromoCodeResponse(promoCodeResponse);
        }

        setUPIDataInSession(requestData, workFlowRequestBean, bizResponseBean, workFlowResponseBean);
        /**
         * For providing merchant name etc info on UPI poll page in Native
         */
        merchantInfoSessionUtil.setMerchantInfoIntoSession(requestData, workFlowResponseBean);

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && isUpiPushExpressPaymentFailed(workFlowRequestBean, workFlowResponseBean)
                && Boolean.parseBoolean(ConfigurationUtil.getProperty("native.upipush.retry", "true"))) {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean,
                    ResponseConstants.PROCESS_FAIL);

        } else if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && checkRetryAllowedBasedOnPaymentStatus(workFlowRequestBean, workFlowResponseBean, requestData)) {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean,
                    ResponseConstants.PROCESS_FAIL);
        } else {
            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, workFlowResponseBean, null);
        } // Correct Mapping of risk reject codes-PGP-8934
        if (bizResponseBean.getRiskRejectUserMessage() != null) {
            requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
        }
        PageDetailsResponse pageResponse = handleRiskVerifiedEnhanceRetry(requestData);
        if (pageResponse != null) {
            return pageResponse;
        }
        workFlowResponseBean.setWorkFlowRequestBean(workFlowRequestBean);
        String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
        LOGGER.debug("Response page for seamless is : {}", responsePage);
        // Generate Json response for enhanced native flow
        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);
        boolean isFundOrder = requestData.isNativeAddMoney();
        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), isFundOrder, requestData.getRequestType());

        setCashierRequestIdInCache(workFlowRequestBean, workFlowResponseBean);
        setPaymentTypeIdInCache(workFlowRequestBean);
        setPaymentOptionInCache(workFlowRequestBean);
        invalidateSessionIfRequired(workFlowRequestBean, requestData, workFlowResponseBean, null);

        return new PageDetailsResponse(true);
    }

    private PageDetailsResponse handleRiskVerifiedEnhanceRetry(PaymentRequestBean requestData) {
        if (requestData.isRiskVerifiedEnhanceFlow() && requestData.isNativeRetryEnabled()) {
            EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                    .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(requestData.getMid(),
                            requestData.getOrderId()));
            if (enhanceCashierPageCachePayload != null
                    && enhanceCashierPageCachePayload.getEnhancedCashierPage() != null) {
                return riskVerificationUtil.handleRiskVerifiedEnhanceRetry(requestData,
                        enhanceCashierPageCachePayload.getEnhancedCashierPage());
            }
        }
        return null;
    }

    private void throwExceptionForNativeJsonRequest(ResultCode rc, PaymentRequestBean requestBean) {
        throw new NativeFlowException.ExceptionBuilder(rc).isHTMLResponse(false).isNativeJsonRequest(true)
                .isRetryAllowed(requestBean.isNativeRetryEnabled()).build();
    }

    private void throwExceptionForNativeJsonRequest(ResponseConstants rc, PaymentRequestBean requestBean) {
        throw new NativeFlowException.ExceptionBuilder(rc).isHTMLResponse(false).isNativeJsonRequest(true)
                .isRetryAllowed(requestBean.isNativeRetryEnabled()).build();
    }

    private String getJsonString(Object obj) {
        String jsonString = "{}";
        try {
            jsonString = JsonMapper.mapObjectToJson(obj);
        } catch (FacadeCheckedException fce) {
            LOGGER.info("failed mapping object to Json");
        }
        return jsonString;
    }

    private PageDetailsResponse setPromoCodeResponse(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, PromoCodeResponse promoCodeResponse) {
        PageDetailsResponse pageDetailsResponse = null;

        // Set the response in Extended Info
        if (null != promoCodeResponse && null != promoCodeResponse.getPromoResponseCode()) {
            ExtendedInfoRequestBean extInforequestBean = workFlowRequestBean.getExtendInfo();
            extInforequestBean.setPromoResponseCode(promoCodeResponse.getPromoResponseCode());
            extInforequestBean.setPromoApplyResultStatus(promoCodeResponse.getResultStatus());
            workFlowRequestBean.setExtendInfo(extInforequestBean);

            // fail transaction if promo is Restricted|Discount and promo is
            // not success
            final String promoCodeTypeName = (null == promoCodeResponse)
                    || (null == promoCodeResponse.getPromoCodeDetail()) ? null : promoCodeResponse.getPromoCodeDetail()
                    .getPromocodeTypeName();
            if (PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName)
                    && !StringUtils.isEmpty(promoCodeResponse.getPromoResponseCode())
                    && !ResponseCodeConstant.PROMO_APPLIED.equals(promoCodeResponse.getPromoResponseCode())) {
                // fail this transaction since discount promo is not
                // successfully applied
                LOGGER.error("Restricted | Discount Promo code is not valid for this payment request");

                if (nativePaymentUtil.isNativeEnhanceFlow(requestData)) {
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PROMOCODE).isHTMLResponse(
                            false).build();
                } else if (requestData.isNativeJsonRequest()) {
                    throwExceptionForNativeJsonRequest(ResponseConstants.INVALID_PROMOCODE, requestData);
                } else {
                    String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                            ResponseConstants.INVALID_PROMOCODE);
                    pageDetailsResponse = new PageDetailsResponse();
                    pageDetailsResponse.setSuccessfullyProcessed(false);
                    pageDetailsResponse.setHtmlPage(htmlPage);
                    return pageDetailsResponse;
                }
            }
        }
        return pageDetailsResponse;
    }

    private void updateForPromoCode(PromoCodeResponse promoCodeResponse,
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean, WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        // Update promo code txn details for native
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType()) && null != bizResponseBean
                && null != bizResponseBean.getResponse()
                && !StringUtils.isEmpty(bizResponseBean.getResponse().getTransID()) && promoCodeResponse != null) {
            // update transId in promo txn details via promo service
            // Replace MidOrderIdKey with transactionId
            nativePromoHelper.updatePromoCode(workFlowRequestBean,
                    requestData.getMid() + "|" + requestData.getOrderId() + "|"
                            + bizResponseBean.getResponse().getTransID(), requestData.getTxnAmount());
            // workFlowResponseBean.setPromoCodeResponse(promoCodeResponse);
        }
    }

    private void updateMerchantInfoInRedisForMFOrSTFlow(GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean,
            PaymentRequestBean requestData) {

        if (null != bizResponseBean && null != bizResponseBean.getResponse()
                && !StringUtils.isEmpty(bizResponseBean.getResponse().getTransID()) && requestData != null
                && (requestData.getLinkDetailsData() != null || StringUtils.isNotBlank(requestData.getSubsLinkId()))) {
            EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                    .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(requestData.getMid(),
                            requestData.getOrderId()));
            if (enhanceCashierPageCachePayload != null
                    && enhanceCashierPageCachePayload.getEnhancedCashierPage() != null
                    && enhanceCashierPageCachePayload.getEnhancedCashierPage().getMerchant() != null) {
                EnhancedCashierPageMerchantInfo merchant = enhanceCashierPageCachePayload.getEnhancedCashierPage()
                        .getMerchant();
                LOGGER.info("Setting merchant info in redis for Link based MF, ST payment for MID = {}",
                        merchant.getMid());
                LinkBasedMerchantInfo linkBasedMerchantInfo = new LinkBasedMerchantInfo(merchant.getMid(),
                        merchant.getName(), merchant.getLogo());
                theiaTransactionalRedisUtil.set(LINK_BASED_KEY + bizResponseBean.getResponse().getTransID(),
                        linkBasedMerchantInfo);

            }
        }
    }

    private void setDataForPCF(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData) {
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())
                || isSlabBasedMdr || isDynamicFeeMerchant)
                && !workFlowRequestBean.isNativeAddMoney()) {
            if (!ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                    && requestData.getTxnToken() != null) {
                NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(requestData
                        .getTxnToken());
                workFlowRequestBean.setConsultFeeResponse(cashierInfoResponse.getBody().getConsultFeeResponse());
            }
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setDynamicFeeMerchant(isDynamicFeeMerchant);
        }

        if (!merchantPreferenceService.isAddMoneyPcfDisabled(workFlowRequestBean.getPaytmMID(), false)) {
            workFlowRequestBean.setAddMoneyPcfEnabled(true);
        }

    }

    private PageDetailsResponse checkForNativeRetry(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean) {
        PageDetailsResponse pageDetailsResponse = null;
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                || ERequestType.UNI_PAY.getType().equals(requestData.getRequestType())) {
            if (!nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
                LOGGER.error("Retry count breached for Native payment. Sending response to merchant");
                nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, null,
                        ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getCode(),
                        ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage(), null, true);
                if (requestData.isNativeJsonRequest()) {
                    throwExceptionForNativeJsonRequest(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED, requestData);
                }

                return returnErrorResponseNative(requestData, ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);

            } else {
                // set transId from Cache for pay apply
                workFlowRequestBean.setTransID(nativeRetryUtil.getTxnId(requestData.getTxnToken()));
                // If first payment request , then set callback url in cache for
                // txnToken
                if (workFlowRequestBean.getNativeTotalPaymentCount() == 1) {
                    if (!TokenType.SSO.getType().equals(requestData.getTokenType())) {
                        nativeRetryUtil.setCallbackUrl(requestData.getTxnToken(), workFlowRequestBean.getCallBackURL());
                    }
                }
            }
        }

        if (ERequestType.NATIVE_MF.getType().equals(requestData.getRequestType())
                && EPayMethod.BANK_TRANSFER.getMethod().equals(workFlowRequestBean.getPayMethod())) {
            // set transId from Cache for pay apply; For CO then PAY
            if (workFlowRequestBean.getTransID() == null) {
                workFlowRequestBean.setTransID(nativeRetryUtil.getTxnId(requestData.getTxnToken()));
            }
        }
        return pageDetailsResponse;
    }

    private boolean checkBizResponseBeanSuccessfullyProcessed(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {

            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());

            if (ResponseConstants.INVALID_PAYMENT_DETAILS.equals(bizResponseBean.getResponseConstant())) {
                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).setMsg(bizResponseBean.getFailureDescription()).isRetryAllowed(true)
                        .setRetryMsg(bizResponseBean.getFailureDescription()).build();
            }

            nativeRetryUtil.checkForNativeRetry(requestData, workFlowRequestBean, bizResponseBean.getResponse(),
                    bizResponseBean.getResponseConstant());
            if (bizResponseBean.getRiskRejectUserMessage() != null) {
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
            }
            return false;
        }
        return true;
    }

    private void setUPIDataInSession(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean,
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean, WorkFlowResponseBean workFlowResponseBean) {

        if (!ERequestType.UNI_PAY.getType().equals(requestData.getRequestType())) {
            upiInfoSessionUtil.generateUPISessionData(workFlowRequestBean, workFlowResponseBean, requestData,
                    bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                            .getPaymentStatusValue());
        }
    }

    private boolean isNativeJsonRequest(PaymentRequestBean requestData) {
        return ((ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF_SIP.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_ST.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_ST.getType().equals(requestData.getSubRequestType()) || ERequestType.UNI_PAY
                .getType().equals(requestData.getRequestType())) && requestData.isNativeJsonRequest());
    }

    private UPIPollResponse getEnhancedFlowResponseForUPIPoll(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        return upiInfoSessionUtil.generateUPIPollResponse(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue(), bizResponseBean.getResponseConstant());
    }

    private UPIIntentResponse getEnhancedFlowResponseForUPIIntent(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        return upiInfoSessionUtil.generateUPIIntentResponse(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue(), bizResponseBean.getResponseConstant());
    }

    private UPIPollResponse getUPIPollDataForNativeJsonRequest(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        return upiInfoSessionUtil.generateUPIPollResponse(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue(), bizResponseBean.getResponseConstant());
    }

    private EnhancedResponse generateEnhancedResponse(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        return upiInfoSessionUtil.generateEnhancedResponse(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue(), bizResponseBean.getResponseConstant());
    }

    private void setIfPRNEnabled(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (!workFlowRequestBean.isNativeAddMoney() && prnUtils.checkIfPRNEnabled(requestData.getMid())) {
            workFlowRequestBean.setPrnEnabled(true);
        }
    }

    private ResultInfo getFailureResultInfo(String message, String code) {
        return new ResultInfo("F", code, message, true);
    }

    private UPIPushResponse sendEnhanceNativeRedirectResponseForUPIPush(BaseResponse response,
            PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean,
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        return upiInfoSessionUtil.generateUPIPushResponse(response, workFlowRequestBean, workFlowResponseBean,
                requestData, bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse()
                        .getQueryPaymentStatus().getPaymentStatusValue(), bizResponseBean.getResponseConstant());

    }

    public BankFormData sendEnhancedNativeRedirectionResponse(String responsePage,
            WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean) {
        BankRedirectionDetail body = new BankRedirectionDetail();
        BankFormData bankFormData = new BankFormData(body);
        if (StringUtils.isBlank(responsePage)) {
            LOGGER.error("Parameter validation failed : {}",
                    PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
            String errorMessage = PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg();
            body.setResultInfo(getFailureResultInfo(errorMessage, null));
        } else {
            EXT_LOGGER.customInfo("parsing html from native paymode: {}", requestData.getPaymentMode());
            bankFormData.setBody(bankFormParser.parseHTMLFormNative(responsePage));
            nativeDirectBankPageHelper.changeContractFromInstaToTheiaForEnhance(workFlowResponseBean,
                    workFlowRequestBean, bankFormData.getBody().getBankForm());
            com.paytm.pgplus.common.model.ResultInfo resultInfo = OfflinePaymentUtils.resultInfoForSuccess();
            bankFormData.getBody().setResultInfo(
                    new ResultInfo(resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo
                            .getResultMsg(), resultInfo.isRedirect()));
            if (workFlowResponseBean != null && workFlowResponseBean.getRiskResult() != null) {
                bankFormData.getBody().setRiskContent(new RiskContent());
                bankFormData.getBody().getRiskContent()
                        .setEventLinkId(workFlowResponseBean.getRiskResult().getEventLinkId());
            }
            checkBankFormRetrievalFailed(bankFormData, workFlowRequestBean, requestData);

        }

        if (bankFormData.getBody() != null) {
            merchantResponseService.addRegionalFieldInPTCResponse(bankFormData.getBody().getResultInfo());
        }
        if (workFlowResponseBean != null && workFlowResponseBean.isBankFormFetchFailed()) {
            bankFormData.getBody().getResultInfo().setRedirect(false);
            bankFormData.getBody().getResultInfo().setResultStatus(ResultCode.FAILED.getResultStatus());
            bankFormData.getBody().getResultInfo()
                    .setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
        }
        if (bankFormData != null && bankFormData.getBody() != null) {
            EventUtils.logResponseCode(V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, bankFormData.getBody().getResultInfo()
                    .getResultCode(), bankFormData.getBody().getResultInfo().getResultMsg());
        }
        return bankFormData;
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData) {
        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {

        GenericCoreResponseBean<WorkFlowResponseBean> linkConsultResponse = getLinkConsultResponse(workFlowRequestBean);
        if (linkConsultResponse != null) {
            return linkConsultResponse;
        }

        if (workFlowRequestBean.isNativeAddMoney()) {
            return bizService.processWorkFlow(workFlowRequestBean, nativeAddMoneyFlow);
        }
        if (workFlowRequestBean.isUpiPushFlow()) {
            if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                    || ERequestType.isSubscriptionRequest(workFlowRequestBean.getRequestType().getType())
                    || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())) {
                workFlowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
                workFlowRequestBean.setUpiPushExpressSupported(true);
                return bizService.processWorkFlow(workFlowRequestBean, upiPushExpressFlowService);
            }
            return bizService.processWorkFlow(workFlowRequestBean, upiPushFlowService);
        }
        if (workFlowRequestBean.isEcomTokenTxn()) {
            return bizService.processWorkFlow(workFlowRequestBean, ecomTokenService);
        }
        if (workFlowRequestBean.isCoftTokenTxn()) {
            return bizService.processWorkFlow(workFlowRequestBean, coftPaymentService);
        }
        if (EPayMethod.BANK_TRANSFER.getMethod().equals(workFlowRequestBean.getPayMethod())) {
            return bizService.processWorkFlow(workFlowRequestBean, bankTransferWorkFlow);
        }
        if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.equals(workFlowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(workFlowRequestBean.getRequestType())) {
            /*
             * if transId/AcquirementId is not blank, this means createOrder has
             * been done, so now we need to call pay API, i.e.
             * NativeRetryFlowService
             */
            if (StringUtils.isNotBlank(workFlowRequestBean.getTransID())) {
                return bizService.processWorkFlow(workFlowRequestBean, getRetryWorkFlowService());
            }
        }
        return bizService.processWorkFlow(workFlowRequestBean, getWorkFlowService());
    }

    public IWorkFlow getWorkFlowService() {
        return seamlessflowservice;
    }

    public IWorkFlow getRetryWorkFlowService() {
        throw new TheiaServiceException("Retry is not supported for seamless flow");
    }

    private boolean isAcquirementIdPresent(WorkFlowRequestBean workFlowRequestBean) {
        if (StringUtils.isNotBlank(workFlowRequestBean.getTxnToken())) {
            // String acqId =
            // nativeSessionUtil.getAcquirementIdCreateOrder(workFlowRequestBean.getTxnToken());
            String acqId = nativeSessionUtil.getTxnId(workFlowRequestBean.getTxnToken());

            if (StringUtils.isNotBlank(acqId)) {
                workFlowRequestBean.setTransID(acqId);
                return true;
            }
        }
        return false;
    }

    private boolean isUpiPushExpressPaymentFailed(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean) {
        return !workFlowResponseBean.isPaymentDone()
                && PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                && workFlowRequestBean.isUpiPushExpressSupported();
    }

    /*
     * cc and dc added to allow retries for ecomToken txn
     */

    private boolean checkRetryAllowedBasedOnPaymentStatus(WorkFlowRequestBean flowRequestBean,
            WorkFlowResponseBean flowResponseBean, PaymentRequestBean requestData) {

        if (isRetryAllowedForPaymode(flowRequestBean.getPaymentTypeId(), flowRequestBean)) {
            QueryPaymentStatus queryPaymentStatus = flowResponseBean.getQueryPaymentStatus();
            if (queryPaymentStatus != null) {
                String paymentStatus = queryPaymentStatus.getPaymentStatusValue();
                if (StringUtils.isNotBlank(paymentStatus) && PaymentStatus.FAIL.name().equals(paymentStatus)) {
                    if (StringUtils.isNotBlank(queryPaymentStatus.getErrorMessage())) {
                        requestData.setNativeRetryErrorMessage(queryPaymentStatus.getErrorMessage());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRetryAllowedForPaymode(String payMode, WorkFlowRequestBean flowRequestBean) {
        return (StringUtils.isNotBlank(payMode)
                && payMode.equals(PaymentTypeIdEnum.IMPS.value)
                || PaymentTypeIdEnum.PPI.value.equals(payMode)
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(payMode)
                || PaymentTypeIdEnum.COD.value.equals(payMode)
                || EPayMethod.MP_COD.getMethod().equalsIgnoreCase(payMode)
                || PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value.equals(payMode)
                || (PaymentTypeIdEnum.DC.value.equals(payMode) || PaymentTypeIdEnum.CC.value.equals(payMode)
                        && flowRequestBean.isEcomTokenTxn()) || (PaymentTypeIdEnum.DC.value.equals(payMode) || PaymentTypeIdEnum.CC.value
                .equals(payMode) && flowRequestBean.isCoftTokenTxn()));
    }

    public void createResponseIfRetryAllowedForPaymodes(BaseResponse baseResponse, PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) {

        BankFormData bankFormData = (BankFormData) baseResponse;
        if (bankFormData != null && bankFormData.getBody() != null && requestData != null
                && bankFormData.getBody().getContent() != null) {
            String payMode = bankFormData.getBody().getContent().get(TheiaConstant.ResponseConstants.PAYMENT_MODE);

            if (isRetryAllowedForPaymode(payMode, flowRequestBean)) {
                com.paytm.pgplus.common.model.ResultInfo resultInfoFail = OfflinePaymentUtils
                        .resultInfo(ResultCode.FAILED);
                ResultInfo resultInfo = new ResultInfo(resultInfoFail.getResultStatus(),
                        resultInfoFail.getResultCodeId(), resultInfoFail.getResultMsg(), resultInfoFail.isRedirect());

                if (requestData.isNativeRetryEnabled()) {
                    resultInfo.setResultMsg(requestData.getNativeRetryErrorMessage());
                    resultInfo.setRedirect(false);
                } else {
                    /*
                     * this lets UI direct the response to merchant
                     */
                    resultInfo.setRedirect(true);
                }

                bankFormData.getBody().setResultInfo(resultInfo);
            }
        }
    }

    private void invalidateSessionIfRequired(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean, BaseResponse response) {

        if (workFlowRequestBean.getNativeTotalPaymentCount() == 1
                || workFlowRequestBean.getNativeRetryCount() <= workFlowRequestBean.getMaxAllowedOnMerchant()) {
            // When totalpayment count is 1 , it is first payment call hence not
            // invalidating

        } else {
            // Invalidate only when isNativeRetryEnabled = false and the flow is
            // not GUEST flow
            if (StringUtils.isBlank(requestData.getGuestToken())) {
                nativeRetryUtil.invalidateSessionByRequestType(requestData, ERequestType.NATIVE);
            }
        }

        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                || ERequestType.UNI_PAY.getType().equals(requestData.getRequestType())) {

            if (nativePaymentUtil.isNativeEnhanceFlow(requestData)) {
                BankFormData bankFormData = null;
                if (response instanceof BankFormData) {
                    bankFormData = (BankFormData) response;
                }

                if (workFlowResponseBean.isPaymentDone()
                        || (bankFormData != null && bankFormData.getBody() != null
                                && bankFormData.getBody().getResultInfo() != null && BooleanUtils
                                    .toBoolean(bankFormData.getBody().getResultInfo().isRedirect()))) {

                    enhancedCashierPageServiceHelper.invalidateEnhancedNativeData(requestData.getTxnToken(),
                            requestData.getMid(), requestData.getOrderId());
                }
            } else if (isNativeJsonRequest(requestData)) {
                if (workFlowResponseBean.isPaymentDone()) {
                    nativePaymentUtil.invalidateNativeJsonRequestSessionData(requestData.getTxnToken(),
                            requestData.getMid(), requestData.getOrderId());
                }
            } else {
                if (workFlowResponseBean.isPaymentDone()) {
                    nativePaymentUtil.invalidateNativeSessionData(requestData.getTxnToken(), requestData.getMid(),
                            requestData.getOrderId());
                }
            }

        }
    }

    private void checkBankFormRetrievalFailed(BankFormData bankFormData, WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        if (NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED.equals(workFlowRequestBean.getPaymentFailureType())
                && requestData.isNativeRetryEnabled()) {
            bankFormData.getBody().getResultInfo().setRedirect(false);
            bankFormData.getBody().getResultInfo().setResultStatus(ResultCode.FAILED.getResultStatus());
            bankFormData
                    .getBody()
                    .getResultInfo()
                    .setResultMsg(bankFormData.getBody().getContent().get(TheiaConstant.ResponseConstants.RESPONSE_MSG));

        }

    }

    private void internatinalBinValidation(WorkFlowRequestBean workFlowRequestBean, String token)
            throws PaytmValidationException {

        NativeCashierInfoResponse nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(token);
        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null) {
            PayOption payOption = null;
            BinDetail binDetail = null;
            if (EPayMode.ADDANDPAY.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
                payOption = nativeCashierInfoResponse.getBody().getAddMoneyPayOption();

            } else {
                payOption = nativeCashierInfoResponse.getBody().getMerchantPayOption();
            }
            if (payOption != null) {
                if (workFlowRequestBean.getIsSavedCard() && payOption.getSavedInstruments() != null) {
                    final String savedCardId = workFlowRequestBean.getSavedCardID();
                    SavedCard savedInstrument = (SavedCard) payOption
                            .getSavedInstruments()
                            .stream()
                            .filter(savedCard -> ((SavedCard) savedCard).getCardDetails().getCardId()
                                    .equals(savedCardId)).findAny().orElse(null);
                    // for CIN fetchBin Details from cache
                    if (workFlowRequestBean.isTxnFromCardIndexNo()) {
                        binDetail = workFlowRequestBean.getBinDetail();
                    } else if (savedInstrument != null && savedInstrument.getCardDetails() != null) {
                        // required for savedcardId, will be removed later
                        binDetail = cardUtils.fetchBinDetails(savedInstrument.getCardDetails().getFirstSixDigit());
                    }
                } else {
                    if (workFlowRequestBean.isEcomTokenTxn()) {
                        binDetail = ecomTokenUtils.fetchTokenDetails(workFlowRequestBean.getCardNo());
                    } else if (workFlowRequestBean.isCoftTokenTxn()) {
                        binDetail = coftUtils.fetchTokenDetails(workFlowRequestBean.getCardNo());
                    } else {
                        binDetail = cardUtils.fetchBinDetails(workFlowRequestBean.getCardNo());
                    }
                }
                if (binDetail != null) {
                    if (!binDetail.getIsIndian()) {
                        nativePaymentUtil.validateInternationalCard(payOption, binDetail.getCardName(),
                                binDetail.getCardType());
                    }
                }
            }
        }
    }

    private void setUPIPollDataForNativeJsonRequest(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        if (!PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            return;
        }

        if (!workFlowRequestBean.isUpiPushExpressSupported() && !workFlowRequestBean.isNativeDeepLinkReqd()) {

            NativeUpiData nativeUpiData = new NativeUpiData();

            UPIPollResponse upiPollData = getUPIPollDataForNativeJsonRequest(requestData, workFlowRequestBean,
                    bizResponseBean);
            nativeUpiData.setUpiPollResponse(upiPollData);

            MerchantInfo merchantInfo = merchantInfoSessionUtil.getMerchantInfo(requestData, workFlowRequestBean,
                    bizResponseBean.getResponse());
            nativeUpiData.setMerchantInfo(merchantInfo);

            nativeSessionUtil.setNativeUpiData(requestData.getTxnToken(), nativeUpiData);
        }
    }

    private PromoCodeResponse getPromoCodeResponse(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean) {
        PromoCodeResponse promoCodeResponse = null;
        promoCodeResponse = nativePromoHelper.applyPromoCode(workFlowRequestBean, requestData.getMid() + "|"
                + requestData.getOrderId(), requestData.getTxnAmount());
        return promoCodeResponse;
    }

    private RuntimeException throwMerchantLimitBreachedException(String limitType, String limitDuration,
            PaymentRequestBean requestData, boolean isNativeEnhanceFlow, RuntimeException e) {
        if (isNativeEnhanceFlow || isNativeJsonRequest(requestData)) {
            return new NativeFlowException.ExceptionBuilder(ResponseConstants.MERCHANT_VELOCITY_LIMIT_BREACH)
                    .isHTMLResponse(false).isRedirectEnhanceFlow(false)
                    .setMsg(MapperUtils.getResultCodeForMerchantBreached(limitType, limitDuration).getResultMsg())
                    .build();
        } else {
            return e;
        }
    }

    private BaseResponse getResponseForUpiCollectOrIntent(BaseResponse responseReceived,
            PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean,
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {
        BaseResponse response = responseReceived;
        if (workFlowRequestBean.isNativeDeepLinkReqd()) {
            response = getEnhancedFlowResponseForUPIIntent(requestData, workFlowRequestBean, bizResponseBean);
        } else if (!workFlowRequestBean.isUpiPushExpressSupported()) {
            response = getEnhancedFlowResponseForUPIPoll(requestData, workFlowRequestBean, bizResponseBean);
        }
        return response;
    }

    private void setCashierRequestIdInCache(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean) {
        if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(workFlowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())) {
            nativeSessionUtil.setCashierRequestId(workFlowRequestBean.getTxnToken(),
                    workFlowResponseBean.getCashierRequestId());
        }
    }

    private void setPaymentTypeIdInCache(WorkFlowRequestBean workFlowRequestBean) {
        if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(workFlowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())) {
            nativeSessionUtil.setPaymentTypeId(workFlowRequestBean.getTxnToken(),
                    workFlowRequestBean.getPaymentTypeId());
        }
    }

    private void setPaymentOptionInCache(WorkFlowRequestBean workFlowRequestBean) {
        if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(workFlowRequestBean.getRequestType())
                || ERequestType.UNI_PAY.equals(workFlowRequestBean.getRequestType())) {
            nativeSessionUtil.setPaymentOption(workFlowRequestBean.getTxnToken(), workFlowRequestBean.getPayOption());
        }
    }

    private String getPromoOfferCheckoutFailureMessage(BizPaymentOfferCheckoutException e) {
        if (StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        }
        return ResponseConstants.PAYMENT_OFFER_CHECKOUT_FAILURE.getMessage();
    }

    private String getSubventionOfferCheckoutFailureMessage(BizSubventionOfferCheckoutException e) {
        if (StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        }
        return ResponseConstants.SUBVENTION_CHECKOUT_FAILURE.getMessage();
    }

    private void setTransIdIfOrderCreatedDuringSubscriptionCreation(PaymentRequestBean requestData,
            WorkFlowRequestBean workFlowRequestBean) {
        if (ERequestType.NATIVE_SUBSCRIPTION.equals(workFlowRequestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP.equals(workFlowRequestBean.getRequestType())) {
            String transId = nativeSessionUtil.getTxnId(requestData.getTxnToken());
            if (StringUtils.isNotEmpty(transId)) {
                workFlowRequestBean.setTransID(transId);
            }
        }
    }

    private void setCacheDataForOtpInjectDirectForms(WorkFlowRequestBean flowRequestBean, String cashierRequestId) {
        LOGGER.info("flowRequestBean.isInstaDirectForm() {}", flowRequestBean.isInstaDirectForm());

        if (flowRequestBean.isInstaDirectForm()
                && (!flowRequestBean.isDirectBankCardFlow())
                && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowRequestBean.getPaymentTypeId()))) {
            LOGGER.info("Saving Data For DirectBankCard Flow for OTP Inject");
            theiaSessionRedisUtil.set(
                    com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty("directBankRequestBeanKey")
                            + cashierRequestId, flowRequestBean, Long
                            .parseLong(com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty(
                                    "directBankRedisTimeOut", "300")));
        }
    }

    @Nullable
    private GenericCoreResponseBean<WorkFlowResponseBean> getLinkConsultResponse(WorkFlowRequestBean workFlowRequestBean) {

        if (ff4jUtils.isFeatureEnabled(
                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_ENABLED,
                false)
                && workFlowRequestBean != null
                && workFlowRequestBean.getPaymentRequestBean() != null
                && (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(
                        workFlowRequestBean.getPaymentRequestBean().getRequestType())
                        || ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(
                                workFlowRequestBean.getPaymentRequestBean().getRequestType())
                        || StringUtils.isNotBlank(workFlowRequestBean.getPaymentRequestBean().getLinkId()) || (StringUtils
                        .isNotBlank(workFlowRequestBean.getPaymentRequestBean().getInvoiceId()) && ff4jUtils
                        .isFeatureEnabled(
                                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_INVOICE_ENABLED,
                                false))) && !workFlowRequestBean.getPaymentRequestBean().isOfflineTxnFlow()) {
            PaymentConsultResponseBody paymentConsultResponseBody = linkPaymentConsultUtil
                    .getLinkPaymentConsultResponse(workFlowRequestBean.getPaymentRequestBean());
            if (paymentConsultResponseBody == null) {
                return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                        ResponseConstants.SYSTEM_ERROR);
            } else if (paymentConsultResponseBody.getResultInfo() != null
                    && FacadeConstants.FAIL.equalsIgnoreCase(paymentConsultResponseBody.getResultInfo()
                            .getResultStatus())) {
                if (ResponseConstants.LINK_PAYMENT_ALREADY_PROCESSED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_ALREADY_PROCESSED);
                } else if (ResponseConstants.LINK_PAYMENT_IN_PROCESS.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_IN_PROCESS);
                } else if (ResponseConstants.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT);
                } else if (ResponseConstants.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED);
                } else {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_CONSULT_FAILURE);
                }
            }
        }
        return null;
    }

}
