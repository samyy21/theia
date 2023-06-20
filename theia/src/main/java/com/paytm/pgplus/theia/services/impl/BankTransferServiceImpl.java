/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.BankTransferHelper;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service("bankTransferService")
public class BankTransferServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransferServiceImpl.class);

    @Autowired
    @Qualifier("bankTransferWorkflow")
    private IWorkFlow bankTransferWorkflow;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier(value = "bankTransferHelper")
    private BankTransferHelper bankTransferHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        return null;
    }

    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean isValid = validateJwtToken(requestData);
        if (isValid) {
            return ValidationResults.VALIDATION_SUCCESS;
        }
        return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
    }

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        LOGGER.info("Processing payment request for internalProcessTransaction, order id :{}", requestData.getOrderId());
        WorkFlowResponseBean workFlowResponseBean = null;

        ValidationResults validationResult = validatePaymentRequest(requestData);

        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            throw RequestValidationException.getException(ResultCode.TOKEN_VALIDATION_EXCEPTION);
        case VALIDATION_SUCCESS:
            workFlowResponseBean = processValidRequest(requestData);
            break;
        default:
            break;
        }
        return workFlowResponseBean;
    }

    private WorkFlowResponseBean processValidRequest(PaymentRequestBean requestData) {

        WorkFlowRequestBean workFlowRequestBean = null;
        WorkFlowResponseBean workFlowResponseBean = null;

        workFlowRequestBean = bankTransferHelper.getWorkFlowRequestBean(requestData);

        // Setting PCF Details
        setDataForPCF(workFlowRequestBean, requestData);

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        Assert.notNull(workFlowRequestBean, "Request Bean Generated Cannot Be Null");

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Unsuccessful Biz call due to : {}", bizResponseBean.getFailureDescription());
        }

        workFlowResponseBean = bizResponseBean.getResponse();
        if (null == workFlowResponseBean) {
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRetryAllowed(true).setMsg(bizResponseBean.getRiskRejectUserMessage())
                        .build();
            }
            throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant()).isHTMLResponse(false)
                    .setMsg(bizResponseBean.getFailureDescription()).build();
        }
        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);

        HttpSession session = requestData.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return workFlowResponseBean;
    }

    private void setDataForPCF(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData) {
        // setting pcf enabled/disbaled
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())
                || isSlabBasedMdr || isDynamicFeeMerchant)
                && !workFlowRequestBean.isNativeAddMoney()) {
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setDynamicFeeMerchant(isDynamicFeeMerchant);
        }
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, bankTransferWorkflow);
    }

    public boolean validateJwtToken(PaymentRequestBean request) {

        if (null == request)
            return false;

        String clientId = request.getClientId();
        String signature = request.getJwtToken();

        String mid = request.getMid();
        String orderId = request.getOrderId();
        String txnAmount = request.getTxnAmount();

        if (StringUtils.isBlank(clientId) || !StringUtils.equals("JWT", request.getTokenType())) {
            LOGGER.error("JWT validation skipped: invalid request");
            return false;
        }

        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("iss", clientId);
        jwtClaims.put("mid", mid);
        jwtClaims.put("orderId", orderId);
        jwtClaims.put("txnAmount", txnAmount);

        boolean isValid = false;
        try {
            String secretKey = environment.getProperty("bank.transfer.jwt.key." + clientId);
            if (StringUtils.isNotBlank(secretKey)) {
                isValid = JWTWithHmacSHA256.verifyJsonWebToken(jwtClaims, signature, secretKey, clientId);
            } else {
                LOGGER.error("Key not found in vault : {} ", secretKey);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while validating JWT token", e.getMessage());
        }

        if (isValid) {
            LOGGER.info("JWT validation successful");
        } else {
            LOGGER.error("JWT validation failed");
        }
        return isValid;
    }
}
