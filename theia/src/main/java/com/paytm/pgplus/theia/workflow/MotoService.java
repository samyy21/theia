/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("motoService")
public class MotoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MotoService.class);

    @Autowired
    @Qualifier(value = "motoPaymentService")
    private IJsonResponsePaymentService motoPaymentService;

    public PageDetailsResponse processMotoRequest(final PaymentRequestBean paymentRequestData, final Model model)
            throws FacadeCheckedException {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();

        ValidationResults validationResult = motoPaymentService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
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
            WorkFlowResponseBean workFlowResponseBean = motoPaymentService.processPaymentRequest(paymentRequestData);

            if (workFlowResponseBean != null) {
                workFlowResponseBean.getMotoResponse().setTxnAmount(
                        AmountUtils.getTransactionAmountInRupee(workFlowResponseBean.getMotoResponse().getTxnAmount()));
                String motoResponse = JsonMapper.mapObjectToJson(workFlowResponseBean.getMotoResponse());
                LOGGER.info("Response being returned in case of Moto is : {} ", motoResponse);
                pageDetailsResponse.setS2sResponse(motoPaymentService.getResponseWithChecksumForJsonResponse(
                        motoResponse, paymentRequestData.getClientId()));
                break;
            }
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }
}
