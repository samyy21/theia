package com.paytm.pgplus.theia.workflow;

import com.google.gson.Gson;
import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
import com.paytm.pgplus.theia.models.ProcessedBmResponse;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.processor.MandateProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.CHECKOUT_JS_STATIC_CALLBACK_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_URL_INFO_WEBSITE_FOR_BM;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.WORKFLOW;

@Service("processedBmService")
public class ProcessedBmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedBmService.class);

    private Gson gson;

    @Autowired
    private MandateProcessor mandateProcessor;

    // @Autowired
    // private AOAMandateProcessor aoamandateProcessor;

    @Autowired
    private MerchantResponseService merchantResponseService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    private Gson getGson() {
        if (null == gson) {
            gson = new Gson();
        }
        return gson;
    }

    public PageDetailsResponse process(ProcessedMandateRequest processedMandateRequest) {
        ProcessedBmResponse processedMandateResponse = mandateProcessor.processMandate(processedMandateRequest);
        setCallBackURLIfEmpty(processedMandateResponse);
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setS2sResponse(getGson().toJson(processedMandateResponse));
        pageDetailsResponse.setRedirectionUrl(processedMandateResponse.getMerchantRedirectionUrl());
        return pageDetailsResponse;
    }

    // public PageDetailsResponse processAoaMandate(AoaMandateCallbackRequest
    // aoaMandateCallbackRequest) {
    // ProcessedBmResponse processedMandateResponse =
    // aoamandateProcessor.processMandate(aoaMandateCallbackRequest);
    // setCallBackURLIfEmpty(processedMandateResponse);
    // PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
    // pageDetailsResponse.setS2sResponse(getGson().toJson(processedMandateResponse));
    // pageDetailsResponse.setRedirectionUrl(processedMandateResponse.getMerchantRedirectionUrl());
    // return pageDetailsResponse;
    // }

    private void setCallBackURLIfEmpty(ProcessedBmResponse processedMandateResponse) {
        String workflow = nativeSessionUtil.getTxnTokenAndWorkflowOnMidOrderId(nativeSessionUtil
                .getMidOrderIdKeyForTxnTokenWorkflow(processedMandateResponse.getMid(),
                        processedMandateResponse.getOrderId()), WORKFLOW);

        if (StringUtils.isBlank(processedMandateResponse.getMerchantRedirectionUrl())) {
            processedMandateResponse.setMerchantRedirectionUrl(merchantResponseService.getCallbackUrl(
                    MERCHANT_URL_INFO_WEBSITE_FOR_BM, processedMandateResponse.getMid()));
            if (CHECKOUT.equalsIgnoreCase(workflow)) {
                String callbackUrl = ConfigurationUtil.getProperty(CHECKOUT_JS_STATIC_CALLBACK_URL)
                        + processedMandateResponse.getOrderId() + "&MID=" + processedMandateResponse.getMid();
                processedMandateResponse.setMerchantRedirectionUrl(callbackUrl);
            }
        }
    }

}
