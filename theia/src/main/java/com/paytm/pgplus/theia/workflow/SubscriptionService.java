/*
 * @Dev-AmitDubey
 */

package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.subscriptionClient.model.request.RenewRequestBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionRenewalResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.IPaymentService;
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

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.REDIRECT_TO_NEW_RENEW_API;

@Service("subscriptionService")
public class SubscriptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier(value = "subscriptionPaymentService")
    private IPaymentService subscriptionPaymentService;

    @Autowired
    @Qualifier(value = "subscriptionRenewalService")
    private IJsonResponsePaymentService subscriptionRenewalService;

    @Autowired
    @Qualifier("subscriptionS2SPaymentServiceImpl")
    private IJsonResponsePaymentService subscriptionS2SPaymentService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ISubscriptionService subscriptionService;

    public PageDetailsResponse processSubscriptionRequest(final PaymentRequestBean paymentRequestData, final Model model)
            throws FacadeCheckedException {
        PageDetailsResponse processed;
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;
        String htmlPage;

        boolean isS2SRequest = TheiaConstant.ExtraConstants.CONNECTION_TYPE_S2S.equals(paymentRequestData
                .getConnectiontype());

        validationResult = subscriptionPaymentService.validatePaymentRequest(paymentRequestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_FAILED_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            if (isS2SRequest) {
                pageDetailsResponse.setS2sResponse(ProcessTransactionHelper.processForS2SError(paymentRequestData,
                        model, ResponseConstants.INVALID_CHECKSUM));
            } else {
                pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                        ResponseConstants.INVALID_CHECKSUM);
            }
            break;
        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(ProcessTransactionConstant.CHECKSUM_UNKNOWN_ERROR_MSG,
                    paymentRequestData.getMid(), paymentRequestData.getOrderId());
            if (isS2SRequest) {
                pageDetailsResponse.setS2sResponse(ProcessTransactionHelper.processForS2SError(paymentRequestData,
                        model));
            } else {
                jspName = theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest());
                pageDetailsResponse.setJspName(jspName);
            }
            break;
        case VALIDATION_SUCCESS:
            if (isS2SRequest) {
                WorkFlowResponseBean workFlowResponseBean = subscriptionS2SPaymentService
                        .processPaymentRequest(paymentRequestData);

                if (workFlowResponseBean != null) {
                    String subscriptionResponse = JsonMapper.mapObjectToJson(workFlowResponseBean
                            .getSubscriptionRenewalResponse());
                    LOGGER.info("Response being returned in case of Subscription S2S call is : {} ",
                            subscriptionResponse);
                    pageDetailsResponse.setS2sResponse(subscriptionS2SPaymentService
                            .getResponseWithChecksumForJsonResponse(subscriptionResponse,
                                    paymentRequestData.getClientId()));
                }
                break;
            }
            processed = subscriptionPaymentService.processPaymentRequest(paymentRequestData, model);

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

    public PageDetailsResponse processRenewSubscriptionRequest(final PaymentRequestBean paymentRequestData,
            final Model model) throws FacadeCheckedException {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();

        ValidationResults validationResult = subscriptionRenewalService.validatePaymentRequest(paymentRequestData);

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
            String renewResponse = "";

            if (ff4jUtils.isFeatureEnabledOnMid(paymentRequestData.getMid(), REDIRECT_TO_NEW_RENEW_API, false)) {
                renewResponse = subscriptionRenewalResponse(paymentRequestData);
            } else {
                WorkFlowResponseBean workFlowResponseBean = subscriptionRenewalService
                        .processPaymentRequest(paymentRequestData);

                if (workFlowResponseBean != null) {
                    workFlowResponseBean.getSubscriptionRenewalResponse().setTxnAmount(
                            AmountUtils.getTransactionAmountInRupee(workFlowResponseBean
                                    .getSubscriptionRenewalResponse().getTxnAmount()));
                    renewResponse = JsonMapper.mapObjectToJson(workFlowResponseBean.getSubscriptionRenewalResponse());

                }
            }
            LOGGER.info("Response being returned in case of Renew is : {} ", renewResponse);
            pageDetailsResponse.setS2sResponse(subscriptionRenewalService.getResponseWithChecksumForJsonResponse(
                    renewResponse, paymentRequestData.getClientId()));
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }

    public String subscriptionRenewalResponse(PaymentRequestBean paymentRequestData) throws FacadeCheckedException {
        String renewResponse = "";
        try {
            if (paymentRequestData != null) {
                RenewRequestBean renewSubscriptionRequest = setRenewRequest(paymentRequestData);
                SubscriptionRenewalResponse renewSubsResponse = subscriptionService
                        .processRenewSubscription(renewSubscriptionRequest);
                renewResponse = JsonMapper.mapObjectToJson(renewSubsResponse);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in getting response from subscription: {}", e);
        }
        return renewResponse;
    }

    public RenewRequestBean setRenewRequest(PaymentRequestBean paymentRequestData) {
        String mid = paymentRequestData.getMid();
        String requestType = paymentRequestData.getRequestType();
        String orderId = paymentRequestData.getOrderId();
        Money txnAmount = new Money(paymentRequestData.getTxnAmount());
        String subscriptionId = paymentRequestData.getSubscriptionID();
        String checksumString = paymentRequestData.getChecksumhash();
        RenewRequestBean renewSubscriptionRequest = new RenewRequestBean(requestType, orderId, mid, txnAmount,
                subscriptionId, mid, orderId, checksumString);
        LOGGER.info("Create Request Object for Renew Subscription as : {}", renewSubscriptionRequest);
        return renewSubscriptionRequest;
    }

}
