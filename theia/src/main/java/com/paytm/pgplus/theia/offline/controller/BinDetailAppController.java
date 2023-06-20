package com.paytm.pgplus.theia.offline.controller;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.offline.annotations.OfflineControllerAdvice;
import com.paytm.pgplus.theia.offline.enums.AuthMode;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.services.IBinDetailService;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.offline.validation.IBinDetailRequestValidatorService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@OfflineControllerAdvice
@RestController
@RequestMapping("bin")
public class BinDetailAppController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinDetailAppController.class);

    @Autowired
    @Qualifier("binDetailService")
    private IBinDetailService binDetailService;

    @Autowired
    @Qualifier("binDetailRequestValidatorService")
    private IBinDetailRequestValidatorService requestValidatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(value = "/fetchBinAppDetails")
    public String fetchBinAppDetails(@ApiParam(required = true) @RequestBody BinDetailRequest binDetailRequest) {
        long startTime = System.currentTimeMillis();

        LOGGER.info("Request offline received  for fetching bin details:  {} ", binDetailRequest);
        String response = fetchBinDetails(binDetailRequest);
        LOGGER.info("Response offline generated for bin fetch api: {}", response);

        LOGGER.info("Total time taken for fetching bin details  is {} ms", System.currentTimeMillis() - startTime);
        return response;

    }

    public String fetchBinDetails(BinDetailRequest binDetailRequest) {
        if (binDetailRequest == null || binDetailRequest.getHead() == null || binDetailRequest.getBody() == null) {
            throw new TheiaControllerException("BinDetailRequest can't be null");
        }

        OfflinePaymentUtils.setMDC(binDetailRequest.getHead().getMid(), binDetailRequest.getBody().getOrderId(),
                binDetailRequest.getHead().getRequestId());

        BinDetailResponse response = null;
        try {
            LOGGER.info("Request recieved  for fetching bin details {}", binDetailRequest);
            binDetailRequest.getBody().setMid(binDetailRequest.getHead().getMid());
            String errorMessage = requestValidatorService.validate(binDetailRequest);
            if (StringUtils.isNotBlank(errorMessage)) {
                return generateResponseForExceptionCases(binDetailRequest, errorMessage);
            }
            response = binDetailService.fetchBinDetails(binDetailRequest);
            checkAndAddIDebitOption(binDetailRequest, response);
            generateResponseHeader(binDetailRequest.getHead(), response);

            response.getHead().setResponseTimestamp(System.currentTimeMillis());

            LOGGER.info("Response generated for bin fetch api: {}", response);
            return JsonMapper.mapObjectToJson(response);

        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR ", e);
        }

        return generateResponseForExceptionCases(binDetailRequest, null);

    }

    private void checkAndAddIDebitOption(BinDetailRequest binDetailRequest, BinDetailResponse response) {
        try {
            BinDetailResponseBody body = response.getBody();
            List<String> authModes = new ArrayList<String>();
            authModes.add(AuthMode.OTP.getType());
            if (EPayMethod.DEBIT_CARD.getMethod().equals(body.getBinDetail().getPayMethod())
                    && isDirectChannelEnabled(response, binDetailRequest)) {
                authModes.add(AuthMode.PIN.getType());
            }
            body.setAuthModes(authModes);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private EntityPaymentOptionsTO getEntityPaymentOption(String mid) {
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        if (StringUtils.isEmpty(mid)) {
            return entityPaymentOptionsTO;
        }
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> responseBean = workFlowHelper
                .getLitePayViewConsultResponse(mid);
        if (responseBean == null) {
            return entityPaymentOptionsTO;
        }
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = responseBean.getResponse();
        if (litePayviewConsultResponseBizBean != null
                && CollectionUtils.isNotEmpty(litePayviewConsultResponseBizBean.getPayMethodViews())) {
            for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getPayMethodViews()) {
                if (PayMethod.DEBIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())
                        && CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayChannelOptionViews())) {
                    for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                        if (CollectionUtils.isNotEmpty(payChannelOptionViewBiz.getDirectServiceInsts())) {
                            for (String channel : payChannelOptionViewBiz.getDirectServiceInsts()) {
                                entityPaymentOptionsTO.getDirectServiceInsts().add(channel + "@DEBIT_CARD");
                            }
                        }
                    }
                }
            }
        }
        return entityPaymentOptionsTO;
    }

    private boolean isDirectChannelEnabled(BinDetailResponse binDetailResponse, BinDetailRequest request) {
        try {
            EntityPaymentOptionsTO entityPaymentOptions = getEntityPaymentOption(request.getHead().getMid());
            BinData binData = binDetailResponse.getBody().getBinDetail();
            if (null == entityPaymentOptions) {
                LOGGER.error("Unable to populate direct channel for bin as data does not exist: {}", binData.getBin());
                return false;
            }
            return theiaSessionDataService.isDirectChannelEnabled(binData.getIssuingBankCode(), binData.getPayMethod(),
                    entityPaymentOptions.getDirectServiceInsts(), false, entityPaymentOptions.getSupportAtmPins());

        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private String generateResponseForExceptionCases(BinDetailRequest requestData, String errorMessage) {
        BinDetailResponse response = new BinDetailResponse();

        // Setting response body info
        BinDetailResponseBody body = new BinDetailResponseBody();
        body.setSignature(requestData.getBody().getSignature());

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (StringUtils.isBlank(errorMessage)) {
            // Any exception occurred in system
            resultInfo.setResultCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getCode());
            resultInfo.setResultMsg(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getMessage());
        } else {
            // When any input validation failed
            resultInfo
                    .setResultCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.INVALID_JSON_DATA.getCode());
            resultInfo.setResultMsg(errorMessage);
        }
        body.setResultInfo(resultInfo);

        // Preparing final response data
        response.setHead(generateResponseHeader(requestData));
        response.setBody(body);
        String jsonResponse = null;
        try {
            jsonResponse = JsonMapper.mapObjectToJson(response);
        } catch (FacadeCheckedException e) {
            LOGGER.error("JSON mapping exception", e);
        }

        LOGGER.info("JSON Response generated : {}", jsonResponse);
        return jsonResponse;
    }

    private ResponseHeader generateResponseHeader(RequestHeader requestHeader, BinDetailResponse response) {
        ResponseHeader head = new ResponseHeader();
        response.setHead(head);
        head.setMid(requestHeader.getMid());
        head.setVersion(requestHeader.getVersion());
        head.setClientId(requestHeader.getClientId());
        head.setRequestId(requestHeader.getRequestId());
        head.setResponseTimestamp(System.currentTimeMillis());

        return head;

    }

    private static ResponseHeader generateResponseHeader(BinDetailRequest requestData) {

        ResponseHeader head = new ResponseHeader();
        head.setVersion(requestData.getHead().getVersion());
        head.setClientId(requestData.getHead().getClientId());
        head.setRequestId(requestData.getHead().getRequestId());
        head.setResponseTimestamp(System.currentTimeMillis());

        return head;
    }

}
