/*
 * AmitD
 *
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static com.paytm.pgplus.biz.utils.BizConstant.FAILURE_RESULT_STATUS;
import static com.paytm.pgplus.biz.utils.BizConstant.SUCCESS_RESULT_STATUS;

@Service("expressService")
public class ExpressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier(value = "paytmExpressSerivice")
    private IPaymentService paytmExpressSerivice;

    @Autowired
    @Qualifier(value = "topupExpressService")
    private IPaymentService topupExpressService;

    @Autowired
    @Qualifier(value = "addMoneyExpressService")
    private IPaymentService addMoneyExpressService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    public PageDetailsResponse processPaytmExpressRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;
        String failureHtml;

        validationResult = paytmExpressSerivice.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            // PGP-8771
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CHECKSUM.getCode(),
                    ResponseConstants.INVALID_CHECKSUM.getMessage(), null, true);
            break;

        case INVALID_REQUEST:
            failureHtml = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                    ResponseConstants.INVALID_REQUEST);
            pageDetailsResponse.setHtmlPage(failureHtml);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_REQUEST.getCode(),
                    ResponseConstants.INVALID_REQUEST.getMessage(), null, true);
            break;

        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.VALIDATION_FAILURE, paymentRequestData.getMid());
            paymentRequestData.setValidationError(ValidationResults.UNKNOWN_VALIDATION_FAILURE.toString());
            jspName = ProcessTransactionHelper.processForError(paymentRequestData, model);
            theiaResponseGenerator.pushPtcResultStatusToStatsD(FAILURE_RESULT_STATUS);
            pageDetailsResponse.setJspName(jspName);
            failureLogUtil.setFailureMsgForDwhPush(null, ProcessTransactionConstant.VALIDATION_FAILURE, null, true);
            break;

        case VALIDATION_SUCCESS:

            // limiting number of retry with same orderId
            if (!nativeSessionUtil.isRetryPossibleForPaytmExpress(paymentRequestData)) {
                failureLogUtil.setFailureMsgForDwhPush(null, "Retry not Possible For Paytm Express", null, true);
                failureHtml = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                        ResponseConstants.SYSTEM_ERROR);
                pageDetailsResponse.setHtmlPage(failureHtml);
                pageDetailsResponse.setSuccessfullyProcessed(false);
                break;
            }
            processed = paytmExpressSerivice.processPaymentRequest(paymentRequestData, model);

            if (processed.isSuccessfullyProcessed()) {
                if (PaymentTypeIdEnum.UPI.value.equals(paymentRequestData.getPaymentTypeId())
                        && theiaSessionDataService.isUPIAccepted(paymentRequestData.getRequest())) {
                    theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                    jspName = theiaViewResolverService.returnUPIPollPage();
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

    public PageDetailsResponse processTopupExpressRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        LOGGER.info("Request received for validating checksum for TOPUP_EXPRESS");
        validationResult = topupExpressService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.info(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            // PGP-8771
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CHECKSUM.getCode(),
                    ResponseConstants.INVALID_CHECKSUM.getMessage(), null, true);
            break;

        case INVALID_REQUEST:
            String html = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                    ResponseConstants.INVALID_REQUEST);
            pageDetailsResponse.setHtmlPage(html);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_REQUEST.getCode(),
                    ResponseConstants.INVALID_REQUEST.getMessage(), null, true);
            break;

        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.info(ProcessTransactionConstant.VALIDATION_FAILURE, paymentRequestData.getMid());
            paymentRequestData.setValidationError(ValidationResults.UNKNOWN_VALIDATION_FAILURE.toString());
            jspName = ProcessTransactionHelper.processForError(paymentRequestData, model);
            theiaResponseGenerator.pushPtcResultStatusToStatsD(FAILURE_RESULT_STATUS);
            pageDetailsResponse.setJspName(jspName);
            failureLogUtil.setFailureMsgForDwhPush(null, ProcessTransactionConstant.VALIDATION_FAILURE, null, true);
            break;
        case VALIDATION_SUCCESS:
            processed = topupExpressService.processPaymentRequest(paymentRequestData, model);

            if (processed.isSuccessfullyProcessed()) {
                if (PaymentTypeIdEnum.UPI.value.equals(paymentRequestData.getPaymentTypeId())
                        && theiaSessionDataService.isUPIAccepted(paymentRequestData.getRequest())) {
                    theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                    jspName = theiaViewResolverService.returnUPIPollPage();
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

    public PageDetailsResponse processAddMoneyExpressRequest(final PaymentRequestBean paymentRequestData,
            final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        LOGGER.info("Request received for validating checksum for ADDMONEY_EXPRESS request, merchant id {}",
                paymentRequestData.getMid());
        validationResult = addMoneyExpressService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.info(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_CHECKSUM.getCode(),
                    ResponseConstants.INVALID_CHECKSUM.getMessage(), null, true);
            break;

        case INVALID_REQUEST:
            String html = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                    ResponseConstants.INVALID_REQUEST);
            pageDetailsResponse.setHtmlPage(html);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_REQUEST.getCode(),
                    ResponseConstants.INVALID_REQUEST.getMessage(), null, true);
            break;

        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.info(ProcessTransactionConstant.VALIDATION_FAILURE, paymentRequestData.getMid());
            paymentRequestData.setValidationError(ValidationResults.UNKNOWN_VALIDATION_FAILURE.toString());
            jspName = ProcessTransactionHelper.processForError(paymentRequestData, model);
            theiaResponseGenerator.pushPtcResultStatusToStatsD(FAILURE_RESULT_STATUS);
            pageDetailsResponse.setJspName(jspName);
            failureLogUtil.setFailureMsgForDwhPush(null, ProcessTransactionConstant.VALIDATION_FAILURE, null, true);
            break;
        case VALIDATION_SUCCESS:
            processed = addMoneyExpressService.processPaymentRequest(paymentRequestData, model);
            pageDetailsResponse.setAddMoneyToGvConsentKey(processed.getAddMoneyToGvConsentKey());

            if (processed.isSuccessfullyProcessed()) {
                jspName = theiaViewResolverService.returnForwarderPage();
                theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                pageDetailsResponse.setJspName(jspName);
                break;
            } else if (processed.isKycPageRequired()) {
                return processed;
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
}
