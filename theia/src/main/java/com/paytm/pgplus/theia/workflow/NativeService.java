/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.cashier.enums.PaymentMode;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.common.model.ResultInfo;

import java.util.Objects;

import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.NativeCODValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;

import static com.paytm.pgplus.biz.utils.BizConstant.SUCCESS_RESULT_STATUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_ERROR_MESSAGE_NOPAYMENTMODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;
import static com.paytm.pgplus.theia.offline.enums.ResultCode.*;

@SuppressWarnings("Duplicates")
@Service("nativeService")
public class NativeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier(value = "nativeRequestPaymentServiceImpl")
    private IPaymentService nativeRequestPaymentServiceImpl;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("nativeCODValidationService")
    private NativeCODValidationService nativeCODValidationService;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    public PageDetailsResponse processNativeRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        validationResult = nativeRequestPaymentServiceImpl.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CHECKSUM.getCode(),
                    ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, null, true);
            break;
        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            jspName = theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest());
            pageDetailsResponse.setJspName(jspName);
            failureLogUtil.setFailureMsgForDwhPush(null, ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG, null,
                    true);

            break;
        case VALIDATION_SUCCESS:

            String payMethod = paymentRequestData.getPaymentTypeId();
            // to support showing of retry error message in case of 'PPBL',
            // since payMethod becomes 'NB'
            if (StringUtils.isNotBlank(paymentRequestData.getBankCode())
                    && paymentRequestData.getBankCode().equals(EPayMethod.PPBL.toString())
                    && PaymentTypeIdEnum.NB.getValue().equals(payMethod)) {
                payMethod = EPayMethod.PPBL.toString();
            }

            if (!processTransactionUtil.isPayMethodAllowed(payMethod, paymentRequestData.getTxnToken(),
                    paymentRequestData.isAddMoney(), paymentRequestData.isBlockNonCCDCPaymodes(),
                    paymentRequestData.getPaymentFlowExpectedNative(), paymentRequestData.getMid())
                    || ((StringUtils.equalsIgnoreCase(EPayMethod.MP_COD.getMethod(),
                            paymentRequestData.getPaymentTypeId()) || StringUtils.equalsIgnoreCase(
                            EPayMethod.COD.getMethod(), paymentRequestData.getPaymentTypeId())) && !nativeCODValidationService
                            .isValidPaymentFlowAndMode(paymentRequestData))) {

                LOGGER.error("Invalid payment mode: {}", paymentRequestData.getPaymentTypeId());

                EPayMethod ePaymentMethod = EPayMethod.getPayMethodByOldName(payMethod);
                if (ePaymentMethod == null) {
                    ePaymentMethod = EPayMethod.getPayMethodByMethod(payMethod);
                }
                if (ePaymentMethod != null && StringUtils.isNotBlank(ePaymentMethod.getNewDisplayName())) {
                    payMethod = ePaymentMethod.getNewDisplayName();
                }

                String errorMsg;
                if (StringUtils.isBlank(payMethod)) {
                    errorMsg = NATIVE_ERROR_MESSAGE_NOPAYMENTMODE;
                } else {
                    errorMsg = payMethod + NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;
                }

                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_PAYMENTMODE.getCode(),
                        ResponseConstants.INVALID_PAYMENTMODE.getMessage(), null, true);

                if (processTransactionUtil.isNativeEnhancedRequest(paymentRequestData)) {
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                            .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                            .setRetryMsg(errorMsg).setMsg(errorMsg).build();
                }

                if (processTransactionUtil.isNativeJsonRequest(paymentRequestData)) {
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                            .isHTMLResponse(false).isRetryAllowed(true).setMsg(errorMsg).setRetryMsg(errorMsg).build();
                }

                htmlPage = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                        ResponseConstants.INVALID_PAYMENTMODE);
                pageDetailsResponse.setSuccessfullyProcessed(false);
                pageDetailsResponse.setHtmlPage(htmlPage);
                break;
            }

            if (BooleanUtils.isFalse(aoaUtils.isAOAMerchant(paymentRequestData.getMid()))
                    && ERequestType.isSubscriptionCreationRequest(paymentRequestData.getRequestType())) {
                validateSubscriptionDetails(paymentRequestData);
            }
            processed = nativeRequestPaymentServiceImpl.processPaymentRequest(paymentRequestData, model);
            pageDetailsResponse.setS2sResponse(processed.getS2sResponse());
            pageDetailsResponse.setData(processed.getData());
            pageDetailsResponse.setAddMoneyToGvConsentKey(processed.getAddMoneyToGvConsentKey());

            pageDetailsResponse.setRiskVerificationRequired(processed.isRiskVerificationRequired());
            pageDetailsResponse.setTransId(processed.getTransId());
            if (StringUtils.isNotBlank(processed.getRedirectionUrl())) {
                pageDetailsResponse.setRedirectionUrl(processed.getRedirectionUrl());
            }

            if (MapUtils.isNotEmpty(processed.getData())
                    && processed.getData().containsKey(TheiaConstant.ExtraConstants.SUBS_BM_MODE)
                    && processed.getData().get(TheiaConstant.ExtraConstants.SUBS_BM_MODE)
                            .equalsIgnoreCase(MandateMode.E_MANDATE.name())) {
                pageDetailsResponse.setJspName(theiaViewResolverService.returnNpciReqPage());
            }

            if (processTransactionUtil.isNativeEnhancedRequest(paymentRequestData)) {
                if (StringUtils.isNotBlank(processed.getS2sResponse())) {
                    pageDetailsResponse.setS2sResponse(processed.getS2sResponse());
                    pageDetailsResponse.setSuccessfullyProcessed(true);
                } else {
                    failureLogUtil.setFailureMsgForDwhPush(ResultCode.FAILED.getResultCodeId(),
                            ResultCode.FAILED.getResultMsg(), null, true);

                    throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                            .isRedirectEnhanceFlow(true).build();

                    // throw MerchantRedirectRequestException.getException();
                }
                break;
            }
            if (processTransactionUtil.isNativeJsonRequest(paymentRequestData)) {
                processTransactionUtil.setS2SResponseForNativeJsonRequest(paymentRequestData, processed,
                        pageDetailsResponse);
                break;
            }

            if (processed.isSuccessfullyProcessed()) {
                if (checkToReturnUPIPollPage(paymentRequestData)) {
                    theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                    jspName = theiaViewResolverService.returnUPIPollPage();
                } else if (StringUtils.isNotBlank(processed.getHtmlPage())
                        && BooleanUtils.isTrue((Boolean) paymentRequestData.getRequest().getAttribute(
                                TheiaConstant.DccConstants.DCC_PAGE_RENDER))) {
                    htmlPage = processed.getHtmlPage();
                    pageDetailsResponse.setHtmlPage(htmlPage);
                    break;

                } else {
                    theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                    jspName = theiaViewResolverService.returnForwarderPage();
                }

                pageDetailsResponse.setJspName(jspName);
                break;
            }
            /* if Html Page is Returned */
            if (StringUtils.isNotBlank(processed.getHtmlPage())) {
                htmlPage = processed.getHtmlPage();
                pageDetailsResponse.setHtmlPage(htmlPage);
                break;
            }
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }

    private void validateSubscriptionDetails(final PaymentRequestBean paymentRequestData) {
        ResultCode errorCode = null;
        ResultInfo resultInfo = null;
        if (nativeSubscriptionHelper.subsPPIAmountLimitBreached(paymentRequestData.getSubsPaymentMode(),
                paymentRequestData.getSubscriptionMaxAmount(), paymentRequestData.getMid())) {
            resultInfo = new ResultInfo(SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getResultStatus(),
                    SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getResultCodeId(), SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getCode(),
                    SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getResultMsg());
        } else if (nativeSubscriptionHelper.isSubscriptionNotAuthorized(paymentRequestData)) {
            resultInfo = new ResultInfo(SUBSCRIPTION_NOT_IN_AUTHORISED_STATE.getResultStatus(),
                    SUBSCRIPTION_NOT_IN_AUTHORISED_STATE.getResultCodeId(),
                    SUBSCRIPTION_NOT_IN_AUTHORISED_STATE.getCode(), SUBSCRIPTION_NOT_IN_AUTHORISED_STATE.getResultMsg());
        } else if (PaymentMode.UPI.getMode().equalsIgnoreCase(paymentRequestData.getPaymentTypeId())) {
            if (nativeSubscriptionHelper.subsUPIAmountLimitBreached(paymentRequestData.getTxnAmount())) {
                resultInfo = new ResultInfo(SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getResultStatus(),
                        SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getResultCodeId(),
                        SUBSCRIPTION_AMOUNT_LIMIT_FAILURE.getCode(), "Subscription Amount Limit For UPI Breached");
            }

            if (!FrequencyUnit.ONDEMAND.name().equals(paymentRequestData.getSubscriptionFrequencyUnit())
                    && !paymentRequestData.isFlexiSubscription()) {
                if (nativeSubscriptionHelper.invalidUpiGraceDays(paymentRequestData.getSubscriptionFrequencyUnit(),
                        paymentRequestData.getSubscriptionGraceDays(), paymentRequestData.getSubscriptionFrequency())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid Subscription grace days");
                } else if (nativeSubscriptionHelper.subsUpiMonthlyFrequencyBreach(
                        paymentRequestData.getSubscriptionFrequencyUnit(),
                        paymentRequestData.getSubscriptionFrequency())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid Subscription frequency");
                } else if (nativeSubscriptionHelper.invalidUpifrequencyCycle(paymentRequestData
                        .getSubscriptionFrequencyUnit())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid subscriptionFrequencyUnit");
                } else if (nativeSubscriptionHelper
                        .invalidSubsRetryCount(paymentRequestData.getSubscriptionEnableRetry(),
                                paymentRequestData.getSubscriptionRetryCount())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid Subscription retry count");
                } else if (nativeSubscriptionHelper.invalidUpiSubsStartDate(
                        paymentRequestData.getSubscriptionStartDate(),
                        paymentRequestData.getSubscriptionFrequencyUnit())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid Subscription start date");
                } else if (nativeSubscriptionHelper.invalidUpiSubsFrequency(paymentRequestData
                        .getSubscriptionFrequency())) {
                    resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(),
                            SUBSCRIPTION_VALIDATION_FAILURE.getCode(), "Invalid Subscription Frequency");
                }
            }
        } else
        // Disabling BANK_MANDATE paymode for subs frequency unit FORTNIGHT
        if (nativeSubscriptionHelper.invalidSubsFrequencyUnitForBankMandate(
                paymentRequestData.getSubscriptionFrequencyUnit(), paymentRequestData.getSubsPaymentMode())) {
            resultInfo = new ResultInfo(SUBSCRIPTION_VALIDATION_FAILURE.getResultStatus(),
                    SUBSCRIPTION_VALIDATION_FAILURE.getResultCodeId(), SUBSCRIPTION_VALIDATION_FAILURE.getCode(),
                    "Invalid Subscription Frequency Unit");
        }

        if (Objects.nonNull(resultInfo)) {
            if (isNativeEnhancedRequest(paymentRequestData)) {
                throw new NativeFlowException.ExceptionBuilder(resultInfo).isHTMLResponse(false)
                        .isRedirectEnhanceFlow(true).build();
            } else if (processTransactionUtil.isNativeJsonRequest(paymentRequestData)) {
                throw new NativeFlowException.ExceptionBuilder(resultInfo).isHTMLResponse(false).build();
            } else {
                throw new NativeFlowException.ExceptionBuilder(resultInfo).isHTMLResponse(true).build();
            }
        }
    }

    private boolean checkToReturnUPIPollPage(PaymentRequestBean paymentRequestData) {
        return !ERequestType.UNI_PAY.name().equals(paymentRequestData.getRequestType())
                && PaymentTypeIdEnum.UPI.value.equals(paymentRequestData.getPaymentTypeId())
                && theiaSessionDataService.isUPIAccepted(paymentRequestData.getRequest());
    }

    private boolean isNativeEnhancedRequest(PaymentRequestBean paymentRequestData) {

        if (paymentRequestData.getRequest() != null
                && paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))) {
            return true;
        }

        return false;
    }
}
