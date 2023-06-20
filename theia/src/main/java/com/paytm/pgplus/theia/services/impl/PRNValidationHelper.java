package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.facade.acquiring.models.request.ValidatePRNRequest;
import com.paytm.pgplus.facade.acquiring.models.request.ValidatePRNRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.ValidatePRNResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringTicket;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.models.PRNValidationRequest;
import com.paytm.pgplus.theia.models.PRNValidationResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Service("prnValidationHelper")
public class PRNValidationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PRNValidationHelper.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("acquiringTicketImpl")
    private IAcquiringTicket acquiringTicket;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private static final PRNValidationResponse FAIL_RESPONSE_WITHOUT_RETRY = new PRNValidationResponse("FAIL", false);
    private static final PRNValidationResponse FAIL_RESPONSE_WITH_RETRY = new PRNValidationResponse("FAIL", true);

    private static final PRNValidationResponse SUCCESS_RESPONSE_WITHOUT_RETRY = new PRNValidationResponse("SUCCESS",
            false);

    public PRNValidationResponse validatePRNWithPlatformPlus(HttpServletRequest request,
            PRNValidationRequest validationRequest) {

        TransactionInfo transactionInfo;
        Object object = theiaTransactionalRedisUtil.get(getPRNkey(validationRequest));
        if (object == null) {
            LOGGER.info("Transaction Info Not Found for PRN Verification. First PRN Validation Request");
            transactionInfo = new TransactionInfo();
        } else {
            transactionInfo = (TransactionInfo) object;
            LOGGER.info("Transaction Info found for PRN {}", transactionInfo);
        }

        boolean isRetriable = isRetriable(transactionInfo);

        if (!validatePayload(validationRequest) || !validateRetryCount(transactionInfo))
            return isRetriable ? FAIL_RESPONSE_WITH_RETRY : FAIL_RESPONSE_WITHOUT_RETRY;

        ValidatePRNRequest facadeRequest = null;

        try {

            facadeRequest = fetchFacadeRequest(validationRequest, transactionInfo);

            ValidatePRNResponse facadeResponse = acquiringTicket.validatePRN(facadeRequest);

            if (Objects.nonNull(facadeResponse) && Objects.nonNull(facadeResponse.getBody())
                    && Objects.nonNull(facadeResponse.getBody().getPrnValidationDetail())
                    && StringUtils.equals("true", facadeResponse.getBody().getPrnValidationDetail().getStatus())) {
                transactionInfo.setPrnValidationSuccessful(true);
                return SUCCESS_RESPONSE_WITHOUT_RETRY;
            }

        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while creating PRN validation request  {}", e);
        }

        return isRetriable ? FAIL_RESPONSE_WITH_RETRY : FAIL_RESPONSE_WITHOUT_RETRY;
    }

    private String getPRNkey(PRNValidationRequest validationRequest) {
        return "PRN_" + validationRequest.getOrderId();
    }

    private boolean isRetriable(TransactionInfo transactionInfo) {

        boolean retriable = true;

        if (transactionInfo.getPrnValidationCount() >= 3)
            retriable = false;

        return retriable;
    }

    private ValidatePRNRequest fetchFacadeRequest(PRNValidationRequest validationRequest,
            TransactionInfo transactionInfo) throws FacadeCheckedException {

        String mid = validationRequest.getMid();

        String prnCode = validationRequest.getPrnCode();

        String requestId = RequestIdGenerator.generateRequestId();

        String merchantPosId = validationRequest.getOrderId();

        String alipayMid = null;

        String retryFLag = "false";

        int count = transactionInfo.getPrnValidationCount();

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(mid);

        if (merchantMappingResponse.isSuccessfullyProcessed() && Objects.nonNull(merchantMappingResponse.getResponse())) {
            alipayMid = merchantMappingResponse.getResponse().getAlipayId();
        }

        if (count > 0)
            retryFLag = "true";

        transactionInfo.setPrnValidationCount(++count);

        ValidatePRNRequestBody validatePRNRequestBody = new ValidatePRNRequestBody(alipayMid, requestId, merchantPosId,
                prnCode, retryFLag);

        AlipayExternalRequestHeader header = RequestHeaderGenerator.getHeader(ApiFunctions.PRN_VALIDARE);

        ValidatePRNRequest validatePRNRequest = new ValidatePRNRequest(header, validatePRNRequestBody);

        theiaTransactionalRedisUtil.set(getPRNkey(validationRequest), transactionInfo);

        return validatePRNRequest;
    }

    boolean validatePayload(PRNValidationRequest validationRequest) {

        if (Objects.isNull(validationRequest) || StringUtils.isBlank(validationRequest.getMid())
                || StringUtils.isBlank(validationRequest.getOrderId())
                || StringUtils.isBlank(validationRequest.getPrnCode())) {
            LOGGER.error("Payload validation failed for PRN validation validationRequest :: {}", validationRequest);
            return false;
        }

        return true;
    }

    boolean validateRetryCount(final TransactionInfo transactionInfo) {

        if (transactionInfo.getPrnValidationCount() >= 3) {
            LOGGER.error("Hit received for breached retryCount");
            return false;
        }

        return true;
    }

}
