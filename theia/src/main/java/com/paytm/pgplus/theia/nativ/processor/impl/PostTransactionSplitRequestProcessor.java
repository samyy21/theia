package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitRequest;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponse;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IPostTransactionSplitService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("postTransactionSplitRequestProcessor")
public class PostTransactionSplitRequestProcessor
        extends
        AbstractRequestProcessor<PostTransactionSplitRequest, PostTransactionSplitResponse, PostTransactionSplitRequest, PostTransactionSplitResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostTransactionSplitRequestProcessor.class);

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private IPostTransactionSplitService postTransactionSplitService;

    @Override
    protected PostTransactionSplitRequest preProcess(PostTransactionSplitRequest request) throws Exception {
        validate(request);
        return request;
    }

    @Override
    protected PostTransactionSplitResponse onProcess(PostTransactionSplitRequest request,
            PostTransactionSplitRequest postTransactionSplitRequest) throws Exception {
        PostTransactionSplitResponseBody responseBody = postTransactionSplitService.acquiringSplit(request);
        return new PostTransactionSplitResponse(new ResponseHeader(request.getHead().getVersion()), responseBody);
    }

    @Override
    protected PostTransactionSplitResponse postProcess(PostTransactionSplitRequest request,
            PostTransactionSplitRequest postTransactionSplitRequest, PostTransactionSplitResponse response)
            throws Exception {
        if (null != response.getBody().getResultInfo() && null != response.getBody().getResultInfo().getResultCode()) {
            ResultInfo resultInfo = response.getBody().getResultInfo();
            if (resultInfo.getResultCode().equals(com.paytm.pgplus.common.enums.ResultCode.ACCEPTED_SUCCESS.getCode())) {
                response.getBody().setResultInfo(
                        new ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS.getResultCodeId(),
                                ResultCode.SUCCESS.getResultMsg()));
            } else {
                setResultInfoInResponse(response.getBody());
            }
        } else {
            response.getBody().setResultInfo(
                    new ResultInfo("U", ResponseConstants.SYSTEM_ERROR.getCode(), ResponseConstants.SYSTEM_ERROR
                            .getMessage()));
        }
        return response;
    }

    private void validate(PostTransactionSplitRequest request) {
        if (StringUtils.isBlank(request.getBody().getMid())) {
            LOGGER.error("Validation failed as mid missing");
            throw new RequestValidationException(
                    new com.paytm.pgplus.common.model.ResultInfo(
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getCode(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg()));
        }
        if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())) {
            boolean isValidated = checksumValidator.validateChecksum(checksumValidator.getBodyString(), request
                    .getBody().getMid(), request.getHead().getToken());
            if (!isValidated) {
                LOGGER.error("Checksum invalid");
                throw new RequestValidationException(new com.paytm.pgplus.common.model.ResultInfo(
                        ResultCode.INVALID_CHECKSUM.getResultStatus(), ResultCode.INVALID_CHECKSUM.getResultCodeId(),
                        ResultCode.INVALID_CHECKSUM.getResultMsg(), ResponseMessage.INVALID_CHECKSUM));
            }
        } else {
            LOGGER.error("TokenType: {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new com.paytm.pgplus.common.model.ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()));
        }
        if (ObjectUtils.notEqual(null, request.getBody())
                && ObjectUtils.equals(null, request.getBody().getSplitSettlementInfoData())) {
            LOGGER.error("Validation failed as empty SplitSettlementInfo received");
            throw new RequestValidationException(
                    new com.paytm.pgplus.common.model.ResultInfo(
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getCode(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg()));
        }
        if (StringUtils.isBlank(request.getBody().getAcqId()) && StringUtils.isBlank(request.getBody().getOrderId())) {
            LOGGER.error("Validation failed as both acqId and orderId missing");
            throw new RequestValidationException(
                    new com.paytm.pgplus.common.model.ResultInfo(
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getCode(),
                            ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg()));
        }

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(request.getBody().getMid());
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
    }

    private void setResultInfoInResponse(PostTransactionSplitResponseBody body) {
        ResultInfo resultInfo = body.getResultInfo();
        switch (resultInfo.getResultCode()) {
        case BizConstant.MERCHANT_RELATIONSHIP_ILLEGAL:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    BizConstant.FailureLogs.MERCHANT_RELATIONSHIP_ILLEGAL_MESSAGE));
            break;
        case BizConstant.SPLIT_SIZE_ILLEGAL:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    BizConstant.FailureLogs.SPLIT_SIZE_ILLEGAL_MESSAGE));
            break;
        case BizConstant.SPLIT_METHOD_NOT_EQUALS:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    BizConstant.FailureLogs.SPLIT_METHOD_NOT_EQUALS_MESSAGE));
            break;
        case BizConstant.REPEAT_REQUEST:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    BizConstant.FailureLogs.REPEAT_REQUEST_MESSAGE));
            break;
        case BizConstant.MUTEX_OPERATION_IN_PROCESSING:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    BizConstant.FailureLogs.MUTEX_OPERATION_IN_PROCESSING_MESSAGE));
            break;
        case BizConstant.PARAM_ILLEGAL:
        case BizConstant.ORDER_STATUS_INVALID:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    resultInfo.getResultMsg()));
            break;
        case BizConstant.ORDER_NOT_EXISTS:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResponseConstants.INVALID_ORDER_ID
                    .getCode(), ResponseConstants.INVALID_ORDER_ID.getMessage()));
            break;
        case BizConstant.SYSTEM_ERROR:
            body.setResultInfo(new ResultInfo("U", ResponseConstants.SYSTEM_ERROR.getCode(),
                    ResponseConstants.SYSTEM_ERROR.getMessage()));
            break;
        default:
            body.setResultInfo(new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                    ResultCode.FAILED.getResultMsg()));
            break;

        }
    }
}
