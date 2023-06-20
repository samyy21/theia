package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("defaultMFService")
public class DefaultMFService {

    @Autowired
    @Qualifier(value = "defaultMFPaymentService")
    private IPaymentService defaultMFPaymentService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMFService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    public PageDetailsResponse processDefaultMFRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;
        validationResult = defaultMFPaymentService.validatePaymentRequest(paymentRequestData);
        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:// response code 330
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            break;
        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            jspName = theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest());
            pageDetailsResponse.setJspName(jspName);
            break;
        case VALIDATION_SUCCESS: // cashier page
            LOGGER.info("Validation Successful for {}", paymentRequestData.getRequestType());
            processed = defaultMFPaymentService.processPaymentRequest(paymentRequestData, model);
            if (processed.isSuccessfullyProcessed()) {
                jspName = theiaViewResolverService.returnPaymentPage(paymentRequestData.getRequest());
                pageDetailsResponse.setJspName(jspName);
                break;
            }
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
