/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static com.paytm.pgplus.biz.utils.BizConstant.SUCCESS_RESULT_STATUS;

@Service("seamlessService")
public class SeamlessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier(value = "seamlessPaymentService")
    private IPaymentService seamlessPaymentService;

    @Autowired
    @Qualifier(value = "seamlessACSPaymentService")
    private IJsonResponsePaymentService seamlessACSPaymentService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    public PageDetailsResponse processSeamlessNativeRequest(final PaymentRequestBean paymentRequestData,
            final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        validationResult = seamlessPaymentService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            // PGP-8771
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
            processed = seamlessPaymentService.processPaymentRequest(paymentRequestData, model);

            if (processed.isSuccessfullyProcessed()) {
                if (PaymentTypeIdEnum.UPI.value.equals(paymentRequestData.getPaymentTypeId())
                        && theiaSessionDataService.isUPIAccepted(paymentRequestData.getRequest())
                        && StringUtils.isBlank(paymentRequestData.getMpin())) {
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
                pageDetailsResponse.setData(processed.getData());
                break;
            }
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }

    public PageDetailsResponse processSeamlessACSRequest(final PaymentRequestBean paymentRequestData, final Model model)
            throws FacadeCheckedException {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();

        ValidationResults validationResult = seamlessACSPaymentService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            // PGP-8771
            pageDetailsResponse.setS2sResponse(ProcessTransactionHelper.processForS2SError(paymentRequestData, model,
                    ResponseConstants.INVALID_CHECKSUM));
            break;
        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            pageDetailsResponse.setS2sResponse(ProcessTransactionHelper.processForS2SError(paymentRequestData, model));
            break;
        case VALIDATION_SUCCESS:
            WorkFlowResponseBean workFlowResponseBean = seamlessACSPaymentService
                    .processPaymentRequest(paymentRequestData);

            if (workFlowResponseBean != null) {
                String seamlessResponse = JsonMapper.mapObjectToJson(workFlowResponseBean
                        .getSeamlessACSPaymentResponse());
                LOGGER.info("Response being returned in case of Seamless ACS is : {} ", seamlessResponse);
                pageDetailsResponse.setS2sResponse(seamlessACSPaymentService.getResponseWithChecksumForJsonResponse(
                        seamlessResponse, paymentRequestData.getClientId()));
                break;
            }
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }
}
