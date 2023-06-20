package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiDetailRequest;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiDetailResponse;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.AOA_WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.FETCH_EMI_DETAIL_JWT_SECRET_KEY_PREFIX;

@Service
public class NativeFetchEmiDetailRequestProcessor
        extends
        AbstractRequestProcessor<FetchEmiDetailRequest, FetchEmiDetailResponse, FetchEmiDetailRequest, FetchEmiDetailResponse> {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeFetchEmiDetailRequestProcessor.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("emiBinValidationUtil")
    EmiBinValidationUtil emiBinValidationUtil;

    @Autowired
    private Environment environment;

    @Override
    protected FetchEmiDetailRequest preProcess(FetchEmiDetailRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);
        } else if (TokenType.JWT.equals(request.getHead().getTokenType())) {
            validateWithJWTToken(request);
        } else {
            validate(request);
        }
        return request;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchEmiDetailRequestProcessor.class);

    @Override
    protected FetchEmiDetailResponse onProcess(FetchEmiDetailRequest request, FetchEmiDetailRequest serviceRequest)
            throws Exception {
        NativeCashierInfoResponse cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead()
                .getTxnToken());
        if (cacheResponse == null) {
            if (TokenType.SSO.equals(request.getHead().getTokenType())) {
                nativePaymentUtil.fetchPaymentOptionsWithSsoToken(request.getHead(), request.getBody().getMid(), false,
                        null);
            } else if (TokenType.JWT.equals(request.getHead().getTokenType())) {
                request.getHead().setTokenType(TokenType.GUEST);
                // TODO: remove logger post testing
                try {
                    EXT_LOGGER.customInfo("Calling fetchPaymentOption using guest token");
                    nativePaymentUtil.fetchPaymentOptionsForGuest(request.getHead(), request.getBody().getMid(), false,
                            null, null);
                } catch (Exception e) {
                    EXT_LOGGER.customError("Exception occured while calling fetchPaymentOption using guest token {}",
                            ExceptionUtils.getFullStackTrace(e));
                }
            } else {
                nativePaymentUtil.fetchPaymentOptions(request.getHead(), null, null);
            }
            cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
        }
        FetchEmiDetailResponseBody responseBody = new FetchEmiDetailResponseBody();
        PayOption merchantPayOption = cacheResponse.getBody().getMerchantPayOption();
        if (TokenType.SSO.equals(request.getHead().getTokenType())
                || TokenType.GUEST.equals(request.getHead().getTokenType())) {
            responseBody.setEmiChannel(filterEmiWithAmountAndChannelCode(merchantPayOption,
                    (AOA_WORKFLOW.equals(request.getHead().getClientId()) ? request.getBody().getAmount() : null),
                    request.getBody().getChannelCode(), request.getBody().getEmiType(),
                    request.getHead().getClientId(), request.getHead().getTokenType(), request.getBody()));
        } else {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(request.getHead()
                    .getTxnToken());
            responseBody.setEmiChannel(filterEmiWithAmountAndChannelCode(merchantPayOption, null == request.getBody()
                    .getAmount() ? orderDetail.getTxnAmount() : request.getBody().getAmount(), request.getBody()
                    .getChannelCode(), request.getBody().getEmiType(), request.getHead().getClientId(), request
                    .getHead().getTokenType(), request.getBody()));
        }

        return new FetchEmiDetailResponse(new ResponseHeader(), responseBody);
    }

    private PayOption trimEmiPlansBasedOnPlanId(PayOption merchantPayOption, String planId) {

        LOGGER.info("call to trimEmiPlansBasedOnPlanId, planId {}", planId);

        PayOption filteredMerchantPayOptions = merchantPayOption;

        if (null != filteredMerchantPayOptions && null != filteredMerchantPayOptions.getPayMethods()) {
            for (PayMethod payMethod : filteredMerchantPayOptions.getPayMethods()) {
                if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())
                        && null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {

                    for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                        EmiChannel emiChannel = (EmiChannel) payChannelBase;

                        List<EMIChannelInfo> filteredEmiChannelInfos = new ArrayList<>();

                        for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                            if (planId.equals(emiChannelInfo.getPlanId())) {
                                filteredEmiChannelInfos.add(emiChannelInfo);
                            }
                        }

                        emiChannel.setEmiChannelInfos(filteredEmiChannelInfos);
                    }
                }
            }
        }
        return filteredMerchantPayOptions;
    }

    private EmiChannel filterEmiWithAmountAndChannelCode(PayOption merchantPayOption, Money orderAmount,
            String channelCode, String emiType, String clientId, TokenType tokenType,
            FetchEmiDetailRequestBody requestBody) {
        Money temp = orderAmount;
        Set<String> channelCodes = new HashSet<>(Arrays.asList(channelCode.split(",")));
        if (null != merchantPayOption && null != merchantPayOption.getPayMethods()) {
            for (PayMethod payMethod : merchantPayOption.getPayMethods()) {
                if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())
                        && null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {
                    EmiChannel newEmiChannel = new EmiChannel();
                    List<EMIChannelInfo> result = new ArrayList<>();
                    for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                        EmiChannel emiChannel = (EmiChannel) payChannelBase;
                        if (channelCodes.contains(emiChannel.getInstId())
                                && emiType.equals(emiChannel.getEmiType().getType())) {
                            // confirmed. skip for channels disabled due to
                            // CHANNEL_NTOT_AVAILABLE reason
                            if (emiChannel.getIsDisabled() != null
                                    && TheiaConstant.ExtraConstants.TRUE.equals(emiChannel.getIsDisabled().getStatus())
                                    && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                            emiChannel.getIsDisabled().getMsg())) {
                                continue;
                            }
                            for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                                /*
                                 * setting orderAmount as originalPrice of the
                                 * order for the tenures on which offer is not
                                 * applicable
                                 */
                                if (emiChannelInfo.getCardAcquiringMode() != null && AOA_WORKFLOW.equals(clientId)) {
                                    emiChannelInfo.setEmiOnus(emiChannelInfo.getCardAcquiringMode().equals(
                                            CardAcquiringMode.ONUS));
                                }
                                if (requestBody.getOriginalAmount() != null
                                        && !CollectionUtils.isEmpty(requestBody.getApplicableTenures())
                                        && !requestBody.getApplicableTenures().contains(emiChannelInfo.getOfMonths())) {
                                    LOGGER.info("changing orderAmount {} for tenure {}",
                                            requestBody.getOriginalAmount(), emiChannelInfo.getOfMonths());
                                    orderAmount = requestBody.getOriginalAmount();
                                }
                                if (orderAmount == null) {
                                    result.add(emiChannelInfo);
                                } else if (Double.valueOf(orderAmount.getValue()) <= Double.valueOf(emiChannelInfo
                                        .getMaxAmount().getValue())
                                        && Double.valueOf(emiChannelInfo.getMinAmount().getValue()) <= Double
                                                .valueOf(orderAmount.getValue())) {
                                    if (StringUtils.isNotBlank(emiChannelInfo.getInterestRate())
                                            && StringUtils.isNotBlank(emiChannelInfo.getOfMonths())) {
                                        emiChannelInfo.setEmiAmount(new Money(emiBinValidationUtil.calculateEmiAmount(
                                                orderAmount.getValue(), emiChannelInfo.getInterestRate(),
                                                emiChannelInfo.getOfMonths(), emiChannel.getInstId()).toString()));
                                        emiChannelInfo.setTotalAmount(new Money(emiBinValidationUtil
                                                .calculateTotalAmount(emiChannelInfo.getEmiAmount().getValue(),
                                                        emiChannelInfo.getOfMonths()).toString()));
                                    }
                                    if (emiChannelInfo.getCardAcquiringMode() != null && AOA_WORKFLOW.equals(clientId)) {
                                        emiChannelInfo.setEmiOnus(emiChannelInfo.getCardAcquiringMode().equals(
                                                CardAcquiringMode.ONUS));
                                    }
                                    result.add(emiChannelInfo);
                                }
                                /*
                                 * setting orderAmount as price again.
                                 */
                                orderAmount = temp;
                            }

                        }
                    }
                    if (result.size() != 0) {
                        newEmiChannel.setEmiChannelInfos(result);
                        return newEmiChannel;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected FetchEmiDetailResponse postProcess(FetchEmiDetailRequest request, FetchEmiDetailRequest serviceRequest,
            FetchEmiDetailResponse serviceResponse) {
        serviceResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        return serviceResponse;
    }

    private void validate(FetchEmiDetailRequest request) {
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        if (request.getBody().getEmiType() == null) {
            request.getBody().setEmiType(EmiType.CREDIT_CARD.getType());
        }
    }

    private void validateWithSsoToken(FetchEmiDetailRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));
        if (request.getBody().getEmiType() == null) {
            request.getBody().setEmiType(EmiType.CREDIT_CARD.getType());
        }
    }

    private void validateWithJWTToken(FetchEmiDetailRequest request) {
        String clientId = request.getHead().getClientId();
        String mid = request.getBody().getMid();
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("iss", clientId);
        jwtClaims.put("mid", mid);
        String jwtToken = request.getHead().getToken();
        String secretKey = environment.getProperty(FETCH_EMI_DETAIL_JWT_SECRET_KEY_PREFIX + clientId);
        if (!JWTWithHmacSHA256.verifyJsonWebToken(jwtClaims, jwtToken, secretKey, clientId)) {
            throw com.paytm.pgplus.theia.offline.exceptions.RequestValidationException
                    .getException(com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_JWT);
        }
    }
}