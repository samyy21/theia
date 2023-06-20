/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.payment.service.impl;

import com.paytm.pgplus.biz.core.looper.service.ILooperService;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.order.utils.OrderHelper;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.core.payment.utils.FeeHelper;
import com.paytm.pgplus.biz.core.payment.utils.PaymentHelper;
import com.paytm.pgplus.biz.core.pool.PoolExecutorService;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.EncryptionUtils;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.boss.models.request.BulkChargeFeeConsultRequest;
import com.paytm.pgplus.facade.boss.models.response.BulkChargeFeeConsultResponse;
import com.paytm.pgplus.facade.boss.models.response.ChargeFeeConsultResponse;
import com.paytm.pgplus.facade.boss.services.ICharge;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.fund.models.response.WalletConsultResponse;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.merchantlimit.utils.MerchantLimitUtil;
import com.paytm.pgplus.facade.payment.enums.UserType;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.payment.models.PayMethodView;
import com.paytm.pgplus.facade.payment.models.request.*;
import com.paytm.pgplus.facade.payment.models.response.*;
import com.paytm.pgplus.facade.payment.services.IBankProxyService;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.PaymentAdapterUtil;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceRequest;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceResponse;
import com.paytm.pgplus.facade.wallet.services.IFetchWalletBalanceService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.utils.RiskRejectInfoCodesAndMessages;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.paytm.pgplus.facade.utils.EventUtil;

import static com.paytm.pgplus.biz.utils.BizConstant.PENNY_DROP_DUMMY_MID;
import static com.paytm.pgplus.biz.utils.BizConstant.DEFAULT_PENNY_DROP_DUMMY_MID;
import static com.paytm.pgplus.payloadvault.theia.constant.EnumValueToMask.SSOTOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.ENABLE_SET_AND_CHECK_ROUTE_IN_REDIS_FOR_SUBSCRIPTION_ORDER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.SUBSCRIPTION_ROUTE_KEY;

/**
 * @author namanjain, AmitD
 *
 */
@Service("bizPaymentService")
public class BizPaymentServiceImpl implements IBizPaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizPaymentServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BizPaymentServiceImpl.class);

    @Autowired
    private ITopup topUpServices;

    @Autowired
    private ICashier cashier;

    @Autowired
    private ICharge chargeImpl;

    @Autowired
    @Qualifier("looperservice")
    private ILooperService looperService;

    @Autowired
    private PoolExecutorService poolExecutorService;

    @Autowired
    private PaymentHelper paymentHelper;

    @Autowired
    @Qualifier("bankProxyServiceImpl")
    private IBankProxyService bankProxyServiceImpl;

    @Autowired
    private IFetchWalletBalanceService fetchWalletBalanceService;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_PAYVIEW)
    @Override
    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView(
            final ConsultPayViewRequestBizBean consutRequestBean) {
        try {
            final GenericCoreResponseBean<PayviewConsultRequest> payviewConsultRequest = paymentHelper
                    .consultPayViewRequest(consutRequestBean);
            Assert.isTrue(payviewConsultRequest.isSuccessfullyProcessed(), BizConstant.PAY_REQUEST_FAIL_MESSAGE);

            final PayviewConsultResponse payViewConsultresponse = cashier.payviewConsult(payviewConsultRequest
                    .getResponse());
            return paymentHelper.mapResponseForConsult(payViewConsultresponse);
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_PAYVIEW)
    @Override
    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult(
            final LitePayviewConsultRequestBizBean consutRequestBean) {
        try {
            final GenericCoreResponseBean<LitePayviewConsultRequest> litePayViewConsultRequest = paymentHelper
                    .litePayViewConsultRequest(consutRequestBean);
            Assert.isTrue(litePayViewConsultRequest.isSuccessfullyProcessed(), BizConstant.PAY_REQUEST_FAIL_MESSAGE);

            final LitePayviewConsultResponse litePayviewConsultResponse = cashier
                    .litePayviewConsult(litePayViewConsultRequest.getResponse());

            checkAndSetOtherWalletsInAoa(litePayviewConsultResponse);

            return paymentHelper.mapResponseForConsult(litePayviewConsultResponse,
                    consutRequestBean.isDefaultLitePayView());
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    private void checkAndSetOtherWalletsInAoa(LitePayviewConsultResponse litePayviewConsultResponse)
            throws FacadeInvalidParameterException {
        List<PayChannelOptionView> othersPayChannelOptionViewList = new ArrayList<PayChannelOptionView>();
        for (PayMethodView payMethodView : litePayviewConsultResponse.getBody().getPayMethodViews()) {
            if (PayMethod.WALLET == payMethodView.getPayMethod()) {
                if (payMethodView.getPayChannelOptionViews().size() > 1) {
                    List<PayChannelOptionView> payChannelOptionView = new ArrayList<PayChannelOptionView>();
                    for (PayChannelOptionView otherPayChannelOptionView : payMethodView.getPayChannelOptionViews()) {
                        if (otherPayChannelOptionView.getInstId() != "PAYTMPG") {
                            othersPayChannelOptionViewList.add(otherPayChannelOptionView);
                        } else {
                            payChannelOptionView.add(otherPayChannelOptionView);
                        }
                    }
                    payMethodView.setPayChannelOptionViews(payChannelOptionView);
                    break;
                }
                break;
            }
        }
        if (CollectionUtils.isNotEmpty(othersPayChannelOptionViewList)) {
            PayMethodView othersPayMethodView = new PayMethodView(PayMethod.WALLET, othersPayChannelOptionViewList);
            litePayviewConsultResponse.getBody().getPayMethodViews().add(othersPayMethodView);

        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_PAYVIEW)
    @Override
    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewPayMethodConsult(
            final LitePayviewConsultRequestBizBean consutRequestBean) {
        try {
            final GenericCoreResponseBean<LitePayviewConsultRequest> litePayViewConsultRequest = paymentHelper
                    .litePayViewPayMethodConsultRequest(consutRequestBean);
            Assert.isTrue(litePayViewConsultRequest.isSuccessfullyProcessed(), BizConstant.PAY_REQUEST_FAIL_MESSAGE);

            LitePayviewConsultResponse litePayviewConsultResponse;
            if (consutRequestBean.getExtendInfo() != null
                    && Routes.PG2.getName().equals(
                            consutRequestBean.getExtendInfo().get(BizConstant.ExtendedInfoKeys.LPV_ROUTE))) {
                final LitePayviewConsultResponseBody litePayviewConsultResponseBody = cashier
                        .litePayviewConsult(litePayViewConsultRequest.getResponse().getBody());
                litePayviewConsultResponse = new LitePayviewConsultResponse(litePayviewConsultResponseBody);
            } else
                litePayviewConsultResponse = cashier.litePayviewConsult(litePayViewConsultRequest.getResponse());
            return paymentHelper.mapResponseForLitePayViewConsult(litePayviewConsultResponse,
                    consutRequestBean.isDefaultLitePayView());
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_CLOSE_ORDER)
    // todo add state in common branch
    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> fetchTokenizedCards(
            TokenizedCardsRequestBizBean requestBean) {
        try {
            final GenericCoreResponseBean<FetchTokenizedCardsRequest> tokenizedCardsRequest = paymentHelper
                    .fetchTokenizeCardsRequest(requestBean);
            LOGGER.info("fetching tokenized cards from card service: {}", tokenizedCardsRequest.getResponse());
            Assert.isTrue(tokenizedCardsRequest.isSuccessfullyProcessed(),
                    BizConstant.TOKENIZED_CARDS_REQUEST_FAIL_MESSAGE);
            // todo abhishek, saqib ask product for aoa integration

            if (isInvalidFetchTokenizedCardsRequest(tokenizedCardsRequest)) {
                LOGGER.info("Invalid fetch tokenized cards request, returning empty response");
                TokenizedCardsResponseBizBean tokenizedCardsResponseBizBean = new TokenizedCardsResponseBizBean(
                        new ArrayList<>());
                return new GenericCoreResponseBean<>(tokenizedCardsResponseBizBean);
            }

            final FetchTokenizedCardsResponse tokenizedCardsResponse = cashier.userCardsFetchAll(tokenizedCardsRequest
                    .getResponse());
            return paymentHelper.mapResponseForTokenizedCards(tokenizedCardsResponse);
        } catch (final Exception e) {
            // LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, ExceptionLogUtils.limitLengthOfStackTrace(e));
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    private boolean isInvalidFetchTokenizedCardsRequest(
            GenericCoreResponseBean<FetchTokenizedCardsRequest> tokenizedCardsRequest) {
        FetchTokenizedCardsRequest fetchTokenizedCardsRequest = tokenizedCardsRequest.getResponse();
        return (fetchTokenizedCardsRequest.getTargetType() == UserType.PAYTM_USER_CARD && StringUtils
                .isBlank(fetchTokenizedCardsRequest.getUserId()))
                || (fetchTokenizedCardsRequest.getTargetType() == UserType.MERCHANT_USER_CARD && (StringUtils
                        .isBlank(fetchTokenizedCardsRequest.getMerchantId()) || StringUtils
                        .isBlank(fetchTokenizedCardsRequest.getExternalUserId())));
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    @Override
    public GenericCoreResponseBean<BizPayResponse> pay(BizPayRequest bizPayRequest) {
        try {
            final GenericCoreResponseBean<PayRequest> payRequest = paymentHelper.payRequest(bizPayRequest);
            Assert.isTrue(payRequest.isSuccessfullyProcessed(), BizConstant.PAY_REQUEST_FAIL_MESSAGE);

            final PayResponse payResponse = cashier.pay(payRequest.getResponse());

            // Set route in Redis in case of PLATFORM
            if (null != bizPayRequest.getExtInfo()
                    && StringUtils.isNotBlank(bizPayRequest.getExtInfo().get("dummyMerchantId"))) {
                String dummyMid = bizPayRequest.getExtInfo().get("dummyMerchantId");
                String cashierRequestId = payResponse.getBody().getCashierRequestId();
                if (!PaymentAdapterUtil.eligibleForPG2(bizPayRequest.getExtInfo())
                        && StringUtils.equals(
                                ConfigurationUtil.getProperty(PENNY_DROP_DUMMY_MID, DEFAULT_PENNY_DROP_DUMMY_MID),
                                dummyMid)
                        && ff4jUtils.isFeatureEnabledOnMid(dummyMid,
                                ENABLE_SET_AND_CHECK_ROUTE_IN_REDIS_FOR_SUBSCRIPTION_ORDER, false)) {
                    LOGGER.info("Setting route as platform in redis");
                    theiaTransactionalRedisUtil.set(SUBSCRIPTION_ROUTE_KEY + cashierRequestId,
                            Routes.PLATFORM.getName(), 900);
                }
            }

            EventUtil.payLog(payResponse, payRequest.getResponse());
            MerchantLimitUtil.checkIfMerchantLimitBreached(payResponse.getBody().getResultInfo().getResultCodeId());
            if (!payResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.ACCEPTED_SUCCESS)) {
                String errorMessage = payResponse.getBody().getResultInfo().getResultMsg();
                ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                        payResponse.getBody().getResultInfo().getResultCodeId());
                failureLogUtil.setFailureMsgForDwhPush(payResponse.getBody().getResultInfo().getResultCodeId(),
                        errorMessage, TheiaConstant.ExtraConstants.PAYMENT_CASHIER_PAY, false);

                if (ResponseConstants.RISK_REJECT.getAlipayResultCode().equals(responseConstants.getAlipayResultCode())
                        && payResponse.getBody().getSecurityPolicyResult() != null
                        && payResponse.getBody().getSecurityPolicyResult().getRiskResult() != null) {

                    String riskRejectResponse = payResponse.getBody().getSecurityPolicyResult().getRiskResult()
                            .getRiskInfo();

                    Map<String, String> infoCodeMessageMap = RiskRejectInfoCodesAndMessages
                            .fetchUserMessageAccToPriorityFromInfoCodeListWithCode(riskRejectResponse);
                    String failureMessageForUser = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.MESSAGE);
                    String internalErrorCode = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.INFOCODE);

                    if (!StringUtils.isEmpty(failureMessageForUser)) {
                        failureMessageForUser = payResponse.getBody().getSecurityPolicyResult().getRiskResult()
                                .getRiskExtendedInfo()
                                .containsKey(TheiaConstant.ExtraConstants.RISKREJECT_EXTENDEDINFO_KEY) ? payResponse
                                .getBody().getSecurityPolicyResult().getRiskResult().getRiskExtendedInfo()
                                .get(TheiaConstant.ExtraConstants.RISKREJECT_EXTENDEDINFO_KEY) : failureMessageForUser;
                        GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(errorMessage,
                                responseConstants, failureMessageForUser);
                        responseBean.setInternalErrorCode(internalErrorCode);
                        return responseBean;
                    }
                    return new GenericCoreResponseBean<>(errorMessage, responseConstants);
                } else if (ResponseConstants.RISK_VERIFICATION.getAlipayResultCode().equals(
                        responseConstants.getAlipayResultCode())) {
                    final BizPayResponse bizPayResponse = OrderHelper.mapPayResponse(payResponse);
                    GenericCoreResponseBean<BizPayResponse> genericCoreResponseBean = new GenericCoreResponseBean<BizPayResponse>(
                            bizPayResponse, errorMessage, responseConstants);
                    return genericCoreResponseBean;
                }
                MerchantLimitUtil.checkIfMerchantLimitBreached(payResponse.getBody().getResultInfo().getResultCodeId());
                return new GenericCoreResponseBean<>(errorMessage, responseConstants);
            }
            final BizPayResponse bizPayResponse = OrderHelper.mapPayResponse(payResponse);
            return new GenericCoreResponseBean<>(bizPayResponse);
        } catch (MerchantLimitBreachedException e) {
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(),
                    TheiaConstant.ExtraConstants.PAYMENT_CASHIER_PAY, false);
            throw e;
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.SYSTEM_ERROR.getAlipayResultCode(),
                    e.getMessage(), TheiaConstant.ExtraConstants.PAYMENT_CASHIER_PAY, false);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }

    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    @Override
    public GenericCoreResponseBean<BizPayResponse> aoaPay(BizAoaPayRequest bizAoaPayRequest) {

        try {
            final GenericCoreResponseBean<AoaPayRequest> aoaPayRequest = paymentHelper.aoaPayRequest(bizAoaPayRequest);
            Assert.isTrue(aoaPayRequest.isSuccessfullyProcessed(), BizConstant.PAY_REQUEST_FAIL_MESSAGE);

            final PayResponse payResponse = cashier.aoaPay(aoaPayRequest.getResponse());
            MerchantLimitUtil.checkIfMerchantLimitBreached(payResponse.getBody().getResultInfo().getResultCodeId());
            if (!payResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.ACCEPTED_SUCCESS)) {
                String errorMessage = payResponse.getBody().getResultInfo().getResultMsg();
                ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                        payResponse.getBody().getResultInfo().getResultCodeId());
                if (ResponseConstants.RISK_REJECT.getAlipayResultCode().equals(responseConstants.getAlipayResultCode())
                        && payResponse.getBody().getSecurityPolicyResult().getRiskResult() != null) {

                    String riskRejectResponse = payResponse.getBody().getSecurityPolicyResult().getRiskResult()
                            .getRiskInfo();

                    Map<String, String> infoCodeMessageMap = RiskRejectInfoCodesAndMessages
                            .fetchUserMessageAccToPriorityFromInfoCodeListWithCode(riskRejectResponse);
                    String failureMessageForUser = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.MESSAGE);
                    String internalErrorCode = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.INFOCODE);

                    if (!StringUtils.isEmpty(failureMessageForUser)) {
                        GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(errorMessage,
                                responseConstants, failureMessageForUser);
                        responseBean.setInternalErrorCode(internalErrorCode);
                        return responseBean;
                    }
                    return new GenericCoreResponseBean<>(errorMessage, responseConstants);
                }
                return new GenericCoreResponseBean<>(errorMessage, responseConstants);
            }
            final BizPayResponse bizPayResponse = OrderHelper.mapPayResponse(payResponse);
            return new GenericCoreResponseBean<>(bizPayResponse);
        } catch (MerchantLimitBreachedException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }

    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_QUERY_FOR_BANK_FORM)
    @Override
    public GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForBankForm(
            final QueryPayResultRequestBean queryPayResultRequestBean, String modifiedLooperTimeout,
            BankFormOptimizationParams bankFormOptimizationParams) {
        try {
            final GenericCoreResponseBean<PayResultQueryRequest> payResultQueryRequest = paymentHelper
                    .queryPayResultRequest(queryPayResultRequestBean);
            payResultQueryRequest.getResponse().getBody().setBankFormFetchCall(true);
            final PayResultQueryResponse payResultQueryResponse = looperService.fetch3DBankForm(
                    payResultQueryRequest.getResponse(), modifiedLooperTimeout, bankFormOptimizationParams);

            if ((payResultQueryResponse == null)
                    || !payResultQueryResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
                final String errorMessage = payResultQueryResponse == null ? "Invalid response received"
                        : payResultQueryResponse.getBody().getResultInfo().getResultMsg();
                LOGGER.error("Query Payy Result failed due to : {}", errorMessage);
                return new GenericCoreResponseBean<QueryPaymentStatus>(errorMessage, ResponseConstants.SYSTEM_ERROR);
            }

            QueryPaymentStatus queryPayResultResponseBean = null;
            if (bankFormOptimizationParams != null && bankFormOptimizationParams.isBankFormOptimizedFlow()) {
                QueryPaymentStatus.QueryPaymentStatusBuilder builder = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                        null, null, null, null, null, null);
                builder.setWebFormContext(payResultQueryResponse.getBody().getWebFormContext());
                queryPayResultResponseBean = builder.build();
            } else {
                queryPayResultResponseBean = paymentHelper.mapQueryPayResult(payResultQueryResponse);
            }
            populateWebFormContext(payResultQueryRequest.getResponse().getBody().getCashierRequestId(),
                    payResultQueryResponse, queryPayResultResponseBean);
            return new GenericCoreResponseBean<QueryPaymentStatus>(queryPayResultResponseBean);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while query pay result: ", e);
            return new GenericCoreResponseBean<QueryPaymentStatus>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    private void populateWebFormContext(String cashierRequestId, PayResultQueryResponse payResultQueryResponse,
            QueryPaymentStatus status) {

        if (StringUtils.isNotBlank(payResultQueryResponse.getBody().getWebFormContext())
                && payResultQueryResponse.getBody().getWebFormContext()
                        .startsWith(CommonConstants.BANK_ENCRYPTED_FORM_KEY)) {
            // LOGGER.info("Got encrypted bank form for cashierRequestId : {} ",
            // cashierRequestId);
            boolean bankFormFetched = false;
            String encryptedWebFormContext = payResultQueryResponse.getBody().getWebFormContext()
                    .substring(CommonConstants.BANK_ENCRYPTED_FORM_KEY.length());
            if (org.apache.commons.lang3.StringUtils.isNotBlank(encryptedWebFormContext)) {
                String webFormContext = EncryptionUtils.decrypt(encryptedWebFormContext);
                if (org.apache.commons.lang3.StringUtils.isNotBlank(webFormContext)) {
                    bankFormFetched = true;
                    status.setWebFormContext(webFormContext);
                    LOGGER.info("Successfully decrypted the bank form for cashierRequestId : {} ", cashierRequestId);
                } else {
                    LOGGER.error("Unable to decrypt bank form : {} for cashier request id : {}", payResultQueryResponse
                            .getBody().getWebFormContext(), cashierRequestId);

                }

            } else {
                LOGGER.error("Unable to get encrypted bank form for cashier request id : {}", cashierRequestId);
            }
            if (!bankFormFetched) {
                status.setWebFormContext(null);
            }
        }

    }

    @Override
    public GenericCoreResponseBean<UPIPushInitiateResponse> initiateUpiPushTransaction(
            final UPIPushInitiateRequestBean initiateRequestBean) {
        try {
            final UPIPushInitiateRequest initiateRequest = paymentHelper.mapUpiPushInitiateRequest(initiateRequestBean);
            final UPIPushInitiateResponse initiateResponse = bankProxyServiceImpl
                    .initiateUpiPushTransaction(initiateRequest);
            return new GenericCoreResponseBean<UPIPushInitiateResponse>(initiateResponse);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while initiating Upi Push transaction : ", e);
            return new GenericCoreResponseBean<UPIPushInitiateResponse>(e.getMessage(),
                    ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED);
        }
    }

    @Override
    public GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQuery(
            final ChannelAccountQueryRequestBizBean channelAccountQueryRequest) {
        try {
            final GenericCoreResponseBean<ChannelAccountQueryRequest> channelAccQueryRequest = paymentHelper
                    .channelAccountQueryRequest(channelAccountQueryRequest);
            Assert.isTrue(channelAccQueryRequest.isSuccessfullyProcessed(),
                    BizConstant.CHANNEL_ACCOUNT_QUERY_REQUEST_FAILED);
            ChannelAccountQueryResponse channelAccountQueryResponse = cashier
                    .channelAccountQuery(channelAccQueryRequest.getResponse());
            return paymentHelper.mapChannelAccountQueryResponse(channelAccountQueryResponse);
        } catch (final Exception e) {
            LOGGER.error(BizConstant.LOG_EXCEPTION_MESSAGE, e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_QUERY_FOR_PAYMENT_STATUS)
    @Override
    public GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForPaymentStatus(
            final QueryPayResultRequestBean queryPayResultRequestBean) {
        try {

            final GenericCoreResponseBean<PayResultQueryRequest> payResultQueryRequest = paymentHelper
                    .queryPayResultRequest(queryPayResultRequestBean);
            final PayResultQueryResponse payResultQueryResponse = looperService
                    .fetchPaymentStatus(payResultQueryRequest.getResponse());
            Assert.notNull(payResultQueryResponse, "LooperResponse received was Null");

            if (!payResultQueryResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
                final String errorMessage = payResultQueryResponse.getBody().getResultInfo().getResultMsg();
                LOGGER.error("Query Pay Result failed due to : {}", errorMessage);
                ResponseConstants responseConstants = ResponseConstants.SYSTEM_ERROR;
                return new GenericCoreResponseBean<QueryPaymentStatus>(errorMessage, responseConstants);
            }
            LOGGER.info(
                    "PayResultQueryResponse received : {}",
                    payResultQueryResponse.getBody(),
                    MaskingUtil.maskObject(payResultQueryResponse.getBody(), SSOTOKEN.getFieldName(),
                            SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));
            final QueryPaymentStatus queryPayResultResponseBean = paymentHelper
                    .mapQueryPayResult(payResultQueryResponse);
            return new GenericCoreResponseBean<QueryPaymentStatus>(queryPayResultResponseBean);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while query pay result: ", e);
            return new GenericCoreResponseBean<QueryPaymentStatus>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_QUERY_FOR_PAYMENT_STATUS)
    @Override
    public GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForPaymentStatusInOneCall(
            final QueryPayResultRequestBean queryPayResultRequestBean) {
        try {

            final GenericCoreResponseBean<PayResultQueryRequest> payResultQueryRequest = paymentHelper
                    .queryPayResultRequest(queryPayResultRequestBean);

            final PayResultQueryResponse payResultQueryResponse = cashier.payResultQuery(payResultQueryRequest
                    .getResponse());

            if (payResultQueryResponse == null || payResultQueryResponse.getBody() == null
                    || payResultQueryResponse.getBody().getResultInfo() == null) {
                LOGGER.error("Unable to execute payResultQueryResponse");
                return new GenericCoreResponseBean<QueryPaymentStatus>("Something went wrong",
                        ResponseConstants.SYSTEM_ERROR);
            }

            if (!BizConstant.SUCCESS.equals(payResultQueryResponse.getBody().getResultInfo().getResultCode())) {
                final String errorMessage = payResultQueryResponse.getBody().getResultInfo().getResultMsg();
                LOGGER.error("Query Pay Result failed due to : {}", errorMessage);
                ResponseConstants responseConstants = ResponseConstants.SYSTEM_ERROR;
                return new GenericCoreResponseBean<QueryPaymentStatus>(errorMessage, responseConstants);
            }
            final QueryPaymentStatus queryPayResultResponseBean = paymentHelper
                    .mapQueryPayResult(payResultQueryResponse);
            return new GenericCoreResponseBean<QueryPaymentStatus>(queryPayResultResponseBean);
        } catch (final Exception e) {
            LOGGER.error("Exception occurred while query pay result: ", e);
            return new GenericCoreResponseBean<QueryPaymentStatus>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_WALLET_LIMIT)
    @Override
    public boolean walletLimitsConsult(final ConsultWalletLimitsRequest walletConsultRequestBean) {
        boolean result = false;
        try {
            result = topUpServices.consultWalletLimits(walletConsultRequestBean).isWalletLimitsAllow();
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            // LOGGER.error("Exception Occurred {}", e);
            LOGGER.error("Exception Occurred {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        LOGGER.debug("Add Money Allowed By Wallet Result : {}", result);
        return result;
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_WALLET_LIMIT)
    @Override
    public BizWalletConsultResponse walletLimitsConsultV2(ConsultWalletLimitsRequest walletConsultRequestBean) {
        try {
            EXT_LOGGER.customInfo("Consulting wallet for add money using v2/walletLimits");
            WalletConsultResponse facadeAddMoneyResponse = topUpServices
                    .consultWalletLimitsV2(walletConsultRequestBean);
            return JsonMapper.convertValue(facadeAddMoneyResponse, BizWalletConsultResponse.class);
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            // LOGGER.error("Exception Occurred {}", e);
            LOGGER.error("Exception Occurred {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        LOGGER.error("Add Money consult failed");
        return new BizWalletConsultResponse("FAILURE");
    }

    @SuppressWarnings("unchecked")
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_CONVENIENCE_FEES)
    @Override
    @Deprecated
    public GenericCoreResponseBean<ConsultFeeResponse> consultConvenienceFees(final ConsultFeeRequest consultFeeRequest) {
        LOGGER.debug("Request object sent is :: {}", consultFeeRequest);
        long startTime = System.currentTimeMillis();
        if (consultFeeRequest.getPayMethods().isEmpty()) {
            return new GenericCoreResponseBean<ConsultFeeResponse>("Empty List of Pay Methods were sent to Consult",
                    ResponseConstants.SYSTEM_ERROR);
        }
        Map<String, String> mapMdc = MDC.getCopyOfContextMap();
        final Map<EPayMethod, ConsultDetails> responseMap = new HashMap<EPayMethod, ConsultDetails>();
        Map<EPayMethod, Future<ChargeFeeConsultResponse>> futureMap = new HashMap<EPayMethod, Future<ChargeFeeConsultResponse>>();
        try {

            for (final EPayMethod payMethod : consultFeeRequest.getPayMethods()) {
                ChargeFeeService request = new ChargeFeeService(payMethod, consultFeeRequest, chargeImpl, mapMdc);
                Future<ChargeFeeConsultResponse> futureResponse = poolExecutorService.submitJob(request);
                futureMap.put(payMethod, futureResponse);
            }

            for (Map.Entry<EPayMethod, Future<ChargeFeeConsultResponse>> entry : futureMap.entrySet()) {
                EPayMethod payMethod = entry.getKey();
                Future<ChargeFeeConsultResponse> futureResponse = entry.getValue();
                ChargeFeeConsultResponse chargeFeeConsultResponse = futureResponse.get(30, TimeUnit.SECONDS);
                if (!chargeFeeConsultResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
                    final String errorMessage = chargeFeeConsultResponse.getBody().getResultInfo().getResultMsg();
                    String errorCodeId = chargeFeeConsultResponse.getBody().getResultInfo().getResultCodeId();
                    LOGGER.error("consult Conivenience fee failed::{}", errorMessage);
                    ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                            errorCodeId);
                    return new GenericCoreResponseBean<>(errorMessage, responseConstants);
                }
                LOGGER.debug("Response for PayMethod {} is ::{}", payMethod, chargeFeeConsultResponse);
                FeeHelper.checkAndMapResponse(responseMap, payMethod, chargeFeeConsultResponse,
                        consultFeeRequest.getTransactionAmount());
            }
            return new GenericCoreResponseBean<ConsultFeeResponse>(new ConsultFeeResponse(responseMap));
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ::", e);
        } finally {
            LOGGER.info("Total time taken to fetch ChargeFeeConsultResponse is::{} ms", System.currentTimeMillis()
                    - startTime);
        }
        return new GenericCoreResponseBean<ConsultFeeResponse>("Internal Processing error",
                ResponseConstants.SYSTEM_ERROR);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CONSULT_CONVENIENCE_FEES)
    @Override
    public GenericCoreResponseBean<ConsultFeeResponse> consultBulkConvenienceFees(
            final ConsultFeeRequest consultFeeRequest) {

        LOGGER.debug("Request object sent is :: {}", consultFeeRequest);
        long startTime = System.currentTimeMillis();
        if (consultFeeRequest.getPayMethods().isEmpty()) {
            return new GenericCoreResponseBean<ConsultFeeResponse>("Empty List of Pay Methods were sent to Consult",
                    ResponseConstants.SYSTEM_ERROR);
        }

        try {
            BulkChargeFeeConsultRequest request = FeeHelper.getBulkFeeConsultRequest(consultFeeRequest);
            BulkChargeFeeConsultResponse response = chargeImpl.feeConsult(request);

            final Map<EPayMethod, ConsultDetails> responseMap = new HashMap<EPayMethod, ConsultDetails>();

            if (!BizConstant.SUCCESS.equals(response.getBody().getResultInfo().getResultCode())) {
                final String errorMessage = response.getBody().getResultInfo().getResultMsg();
                String errorCodeId = response.getBody().getResultInfo().getResultCodeId();
                LOGGER.error("Consult batch Conivenience fee failed, due to reason: {}", errorMessage);
                ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                        errorCodeId);
                return new GenericCoreResponseBean<>(errorMessage, responseConstants);
            }

            FeeHelper.checkAndMapResponse(responseMap, response, consultFeeRequest.getTransactionAmount());

            return new GenericCoreResponseBean<ConsultFeeResponse>(new ConsultFeeResponse(responseMap));
        } catch (Exception e) {
            LOGGER.error("Exception Occurred ::", e);
        } finally {
            LOGGER.info("Total time taken to fetch ChargeFeeConsultResponse is::{} ms", System.currentTimeMillis()
                    - startTime);
        }

        return new GenericCoreResponseBean<ConsultFeeResponse>("Internal Processing error",
                ResponseConstants.SYSTEM_ERROR);
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.FETCH_WALLET_BALANCE)
    public WalletBalanceResponse fetchWalletBalance(WalletBalanceRequest fetchWalletBalanceRequest, String userId) {
        try {
            EXT_LOGGER.customInfo("Fetching wallet balance");
            return fetchWalletBalanceService.fetchWalletBalance(fetchWalletBalanceRequest, userId);
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            // LOGGER.error("Exception while fetching wallet balance {}", e);
            LOGGER.error("Exception while fetching wallet balance {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        WalletBalanceResponse walletBalanceResponse = new WalletBalanceResponse(BizConstant.FAILURE);
        walletBalanceResponse.setStatusMessage("Fetching wallet balance failed");
        return walletBalanceResponse;

    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.FETCH_CARDS_LIMIT)
    public FetchCardLimitsResponse fetchCardLimit(final FetchCardLimitsRequest fetchCardLimitsRequest) {
        try {
            FetchCardLimitsRequest request = new FetchCardLimitsRequest();
            request.setRequestId(RequestIdGenerator.generateRequestId());
            request.setMerchantId(fetchCardLimitsRequest.getMerchantId());
            request.setCardDetails(fetchCardLimitsRequest.getCardDetails());
            FetchCardLimitsResponse response = cashier.fetchCardLimits(request);
            return response;
        } catch (FacadeCheckedException | FacadeUncheckedException e) {
            LOGGER.error("Error occurred while fetching card limits : {}", e.getMessage());
        }
        return new FetchCardLimitsResponse(BizConstant.FAILURE);
    }

}
