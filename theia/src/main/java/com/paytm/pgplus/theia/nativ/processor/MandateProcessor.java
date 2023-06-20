package com.paytm.pgplus.theia.nativ.processor;

import com.paytm.pgplus.payloadvault.subscription.request.MandateRequest;
import com.paytm.pgplus.payloadvault.subscription.request.PaperMandateCreateRequest;
import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
import com.paytm.pgplus.payloadvault.subscription.response.MandateResponse;
import com.paytm.pgplus.payloadvault.subscription.response.PaperMandateCreateResponse;
import com.paytm.pgplus.payloadvault.subscription.response.ProcessedMandateResponse;
import com.paytm.pgplus.payloadvault.subscription.response.ResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.subscriptionClient.service.IMandateService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.exceptions.MandateException;
import com.paytm.pgplus.theia.models.ProcessedBmResponse;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_URL_INFO_WEBSITE_FOR_BM;

@Service
public class MandateProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MandateProcessor.class);

    @Autowired
    private IMandateService mandateService;

    @Autowired
    private IMerchantUrlService merchantUrlService;

    public MandateResponse createEMandate(MandateRequest mandateRequest, String callBackUrl,
            PaymentRequestBean requestBean) {

        MandateResponse response = mandateService.createEMandate(mandateRequest);
        if (null != response) {
            LOGGER.debug("Response received for Create E Mandate {}", response);

            if (response.getResultInfo().getStatus().equalsIgnoreCase("SUCCESS")) {
                return response;
            } else {
                ResultInfo resultInfo = response.getResultInfo();
                throw new MandateException.ExceptionBuilder(callBackUrl, resultInfo, true).setRequestBean(requestBean)
                        .build();
            }
        } else {
            LOGGER.error("Errorneous response received from create e-mandate npci request api {}", response);
            throw new MandateException.ExceptionBuilder(callBackUrl,
                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), true)
                    .setRequestBean(requestBean).build();
        }
    }

    public PaperMandateCreateResponse createPaperMandate(PaperMandateCreateRequest mandateCreateRequest,
            String callBackUrl, PaymentRequestBean requestBean) {
        PaperMandateCreateResponse response = mandateService.createPaperMandate(mandateCreateRequest);
        if (null != response) {
            LOGGER.info("Response received for Create Paper Mandate {}", response);

            if (response.getBody().getResultInfo().getStatus().equalsIgnoreCase("SUCCESS")) {
                return response;
            } else {
                ResultInfo resultInfo = response.getBody().getResultInfo();
                throw new MandateException.ExceptionBuilder(callBackUrl, resultInfo, true).setRequestBean(requestBean)
                        .build();
            }
        } else {
            LOGGER.error("Errorneous response received from create paper mandate npci request api {}", response);
            throw new MandateException.ExceptionBuilder(callBackUrl,
                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), true)
                    .setRequestBean(requestBean).build();
        }
    }

    public ProcessedBmResponse processMandate(ProcessedMandateRequest mandateRequest) {
        ProcessedMandateResponse response = mandateService.processBm(mandateRequest);
        if (null != response) {
            LOGGER.info("Response received for Process E Mandate {}", response);

            ProcessedBmResponse bmResponse = new ProcessedBmResponse(response);
            return bmResponse;
        } else {
            LOGGER.error("Errorneous response received from process npci response api {}", response);
            throw new MandateException.ExceptionBuilder(null,
                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), true).build();
        }
    }
}
