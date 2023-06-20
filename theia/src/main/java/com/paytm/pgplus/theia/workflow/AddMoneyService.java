/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("addMoneyService")
public class AddMoneyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier(value = "addMoneyPaymentService")
    private IPaymentService addMoneyPaymentService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    public PageDetailsResponse processAddMoneyRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        validationResult = addMoneyPaymentService.validatePaymentRequest(paymentRequestData);

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
            processed = addMoneyPaymentService.processPaymentRequest(paymentRequestData, model);

            if (processed.isSuccessfullyProcessed()) {
                jspName = theiaViewResolverService.returnPaymentPage(paymentRequestData.getRequest());
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
}
