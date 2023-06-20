package com.paytm.pgplus.theia.controllers.helper;

import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsAndAccountCheckRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.UpiAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsAndAccountCheckResponseBody;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import com.paytm.pgplus.theia.models.upiAccount.response.UpiAccountResponse;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.services.upiAccount.helper.CheckUPIAccountServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("upiIntentControllerHelper")
public class UPIIntentControllerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIIntentControllerHelper.class);

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    public void validateRequest(UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request) {

        final GenericFlowRequestBeanValidator<UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody>> bean = new GenericFlowRequestBeanValidator<>(
                request);
        ValidationResultBean validationResultBean = bean.validate();
        if (!validationResultBean.isSuccessfullyProcessed()) {
            String failureDescription = StringUtils.isNotBlank(bean.getErrorMessage()) ? bean.getErrorMessage()
                    : "Validation Failed";
            throw RequestValidationException.getException(failureDescription);
        }

        boolean isCheckUPIAccountSupported = merchantPreferenceService.isCheckUPIAccountSupported(request.getBody()
                .getMid());
        if (!isCheckUPIAccountSupported) {
            throw RequestValidationException.getException("check upi account preference not enabled");
        }
        if (request.getHead().getTokenType() == TokenType.TXN_TOKEN) {
            nativeValidationService.validateTxnToken(request.getHead().getTxnToken());
        } else
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
    }
}
