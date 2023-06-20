package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.request.BanksRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.facade.emisubvention.utils.SubventionUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponse;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_ID;

@Service("banksEmiSubventionProcessor")
public class BanksEmiSubventionProcessor
        extends
        AbstractRequestProcessor<EmiBanksRequest, EmiBanksResponse, BanksRequest, GenericEmiSubventionResponse<BanksResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BanksEmiSubventionProcessor.class);

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    private ISubventionEmiService subventionEmiService;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private EmiSubventionUtils emiSubventionUtils;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected BanksRequest preProcess(EmiBanksRequest request) {
        validateRequest(request);
        if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(request.getHead().getToken(), request
                    .getHead().getTokenType(), request.getBody(), request.getBody().getMid());
            if (StringUtils.isBlank(request.getBody().getCustomerId())) {
                request.getBody().setCustomerId(userDetailsBiz.getUserId());
            }
        } else if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            boolean isValidated = checksumValidator.validateChecksum(checksumValidator.getBodyString(), request
                    .getBody().getMid(), request.getHead().getToken());
            if (!isValidated) {
                throw new RequestValidationException(
                        new ResultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                                ResponseMessage.INVALID_CHECKSUM));
            }
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            emiSubventionUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                    request.getHead().getToken());
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType()) && null != request.getHead().getToken()) {
            InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                    .getToken());
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                    ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
        }
        MDC.put(REQUEST_ID, request.getHead().getRequestId());

        BanksRequest banksRequest = null;
        if (!CollectionUtils.isEmpty(request.getBody().getItems())) {
            banksRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(request);
        } else if (request.getBody().getSubventionAmount() > 0.0 && request.getBody().getPrice() > 0.0) {
            banksRequest = subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(request);
        }
        return banksRequest;
    }

    @Override
    protected GenericEmiSubventionResponse<BanksResponse> onProcess(EmiBanksRequest request, BanksRequest serviceRequest)
            throws Exception {
        return subventionEmiService.fetchEmiSubventionBanks(serviceRequest);
    }

    @Override
    protected EmiBanksResponse postProcess(EmiBanksRequest request, BanksRequest serviceRequest,
            GenericEmiSubventionResponse<BanksResponse> serviceResponse) throws Exception {
        EmiBanksResponse emiBanksResponse = subventionEmiServiceHelper
                .prepareBanksEmiResponse(serviceResponse, request);
        emiBanksResponse.getBody().setResultInfo(
                new com.paytm.pgplus.response.ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS
                        .getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
        return emiBanksResponse;
    }

    private void validateRequest(EmiBanksRequest request) {

        if (request.getBody().getMid() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_MID));
        }
        if (!CollectionUtils.isEmpty(request.getBody().getItems())
                && (request.getBody().getSubventionAmount() > 0.0 || request.getBody().getPrice() > 0.0)) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.BOTH_ITEM_LIST_AMOUNT_NON_EMPTY));
        }
        if ((request.getBody().getItems() == null || request.getBody().getItems().size() == 0)
                && (StringUtils.isBlank(String.valueOf(request.getBody().getSubventionAmount())) || StringUtils
                        .isBlank(String.valueOf(request.getBody().getPrice())))) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.BOTH_LISTINGPRICE_PRICE_REQUIRED));
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())
                && StringUtils.isNotBlank(String.valueOf(request.getBody().getSubventionAmount()))
                && (StringUtils.isNotBlank(String.valueOf(request.getBody().getPrice())))
                && ((request.getBody().getSubventionAmount() <= 0.0) || (request.getBody().getPrice() <= 0.0))) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                    ResponseMessage.INVALID_AMOUNT_DETAILS));

        }

        if ((request.getBody().getSubventionAmount() <= 0.0) && (request.getBody().getPrice() <= 0.0)
                && (request.getBody().getItems() == null || request.getBody().getItems().size() == 0)) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.BOTH_ITEM_LIST_AMOUNT_EMPTY));
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())
                && (request.getBody().getSubventionAmount() > request.getBody().getPrice())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                    ResponseMessage.SUBVENTION_AMOUNT_GREATER));
        }
        subventionEmiServiceHelper.checkIfMidExist(request.getBody().getMid());
        if (request.getHead().getRequestId() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_REQUEST_ID));
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())) {
            request.getBody().setPrice(SubventionUtils.formatAmount(request.getBody().getPrice()));
            request.getBody()
                    .setSubventionAmount(SubventionUtils.formatAmount(request.getBody().getSubventionAmount()));
        }

    }

}