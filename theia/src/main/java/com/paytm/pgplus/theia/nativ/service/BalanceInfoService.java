package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.log.EventLogger;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fundService.models.response.CheckLoyaltyBalanceResponse;
import com.paytm.pgplus.facade.payment.models.ChannelAccount;
import com.paytm.pgplus.facade.payment.models.ChannelAccountView;
import com.paytm.pgplus.facade.payment.models.PayMethodInfo;
import com.paytm.pgplus.facade.payment.models.request.ChannelAccountQueryBody;
import com.paytm.pgplus.facade.payment.models.request.ChannelAccountQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.ChannelAccountQueryResponse;
import com.paytm.pgplus.facade.payment.models.response.ChannelAccountQueryResponseBody;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceRequest;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.ppb.models.FetchPPBLUserBalanceRequest;
import com.paytm.pgplus.facade.ppb.models.FetchPPBLUserBalanceResponse;
import com.paytm.pgplus.facade.ppb.services.IPaymentsBankAccountQuery;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.requestidclient.utils.JsonMapper;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.enums.PaymentBankAccountStatus;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.*;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.FetchBalanceInfoException;
import com.paytm.pgplus.theia.offline.exceptions.OperationNotSupportedException;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_FALLBACK_PBBL_V4_ENABLED;
import static com.paytm.pgplus.common.enums.ETerminalType.getTerminalTypeByTerminal;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.ISREDEMPTIONALLOWED;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_ACCOUNT_STATUS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_SYSTEM_UNAVAILABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DEFAULT_INVESTMENT_FUNDING_TNC_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.INVESTMENT_AS_FUNDING_SOURCE_TNC_URL;

@Service("balanceInfoService")
public class BalanceInfoService implements
        IBalanceInfoService<FetchBalanceInfoRequest, BalanceInfoServiceRequest, BalanceInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceInfoService.class);
    private static final String PASS_CODE_REQUIRED = "passcodeRequired";
    private static final String PPBL_FAILURE_STATUS = "FAILURE";
    @Autowired
    @Qualifier("paymentsBankAccountQueryImpl")
    private IPaymentsBankAccountQuery paymentsBankAccountQuery;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    ICashier cashier;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private FundService fundService;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public BalanceInfoResponse fetchBalance(FetchBalanceInfoRequest request, BalanceInfoServiceRequest serviceRequest) {
        long startTime = System.currentTimeMillis();
        BalanceInfoResponseBody body = null;
        NativeCashierInfoResponse nativeCashierInfoResponse = fetchNativeCashierInfoResponse(request);

        UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(serviceRequest.getTxnToken());
        if (userDetailsBiz == null) {
            LOGGER.error("Unable to find user details");
            throw new FetchBalanceInfoException(
                    OfflinePaymentUtils.resultInfo(ResultCode.FETCH_POST_PAID_BALANCE_INFO_EXCEPTION));
        }
        String customerId = userDetailsBiz.getUserId();

        LOGGER.info("Calling fetch balance API for User with userId={} and custId={}", userDetailsBiz.getUserId(),
                customerId);

        if (EPayMethod.PPBL.getOldName().equals(serviceRequest.getPaymentMode())) {

            body = fetchPPBLBalance(serviceRequest, request);
            LOGGER.info("Total time taken for fetch PPBL balance is {} ms", System.currentTimeMillis() - startTime);

        } else if (EPayMethod.BALANCE.getMethod().equals(serviceRequest.getPaymentMode())) {
            PayMethod payMethod = getAccountBalance(EPayMethod.BALANCE.getMethod(), nativeCashierInfoResponse);
            if (null != payMethod) {
                if (null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {
                    BalanceChannel payChannelBase = (BalanceChannel) payMethod.getPayChannelOptions().get(0);
                    AccountInfo accountInfo = payChannelBase.getBalanceInfo();
                    if ("true".equals(payChannelBase.getIsDisabled().getStatus())) {
                        throw OperationNotSupportedException.getException();
                    } else if (null == accountInfo) {
                        throw FetchBalanceInfoException.getException();
                    }
                    body = convertToBalanceInfoResponse(accountInfo);
                    body.setWalletTwoFAConfig(payChannelBase.getTwoFAConfig());
                }
            } else {
                throw OperationNotSupportedException.getException();
            }
        } else if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(serviceRequest.getPaymentMode())) {
            PaytmDigitalCreditRequest paytmDigitalCreditRequest;
            if (TokenType.SSO.equals(request.getHead().getTokenType())
                    || TokenType.GUEST.equals(request.getHead().getTokenType())
                    || TokenType.ACCESS.equals(request.getHead().getTokenType())) {
                paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
                paytmDigitalCreditRequest.setPgmid(request.getBody().getMid());
                paytmDigitalCreditRequest.setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
            } else {
                paytmDigitalCreditRequest = getPaytmDigitalCreditRequest(serviceRequest.getTxnToken());
            }

            try {
                // TODO : Reverting changes for PGP-37201 - Until LMS contract
                // changes are live in prod.
                // boolean addNpayEnabledOnMerchant =
                // merchantPreferenceProvider.isAddMoneyEnabled(request.getBody().getMid());
                // if (addNpayEnabledOnMerchant) {
                // paytmDigitalCreditRequest.setServiceType("ADD_PAY");
                // }
                PaytmDigitalCreditResponse digitalCreditResponse = workFlowHelper.getPaytmDigitalCreditBalanceResponse(
                        paytmDigitalCreditRequest, customerId);
                body = transformToBalanceInfoResponse(digitalCreditResponse, serviceRequest, request);
                if (ResultCode.SUCCESS.getResultStatus().equals(body.getResultInfo().getResultStatus())
                        && CollectionUtils.isNotEmpty(digitalCreditResponse.getResponse())) {
                    updatePostPaidBalanceInCashierResponse(digitalCreditResponse.getResponse().get(0), serviceRequest);
                }

            } catch (FacadeCheckedException e) {
                body = new BalanceInfoResponseBody();
                // LOGGER.error("Exception fetching Postpaid Balance", e);
                LOGGER.error("Exception fetching Postpaid Balance", ExceptionLogUtils.limitLengthOfStackTrace(e));
                EventUtils.pushTheiaEvents(EventNameEnum.POST_PAID_CHECK_BALANCE, new ImmutablePair<>(
                        POSTPAID_ACCOUNT_STATUS, POSTPAID_SYSTEM_UNAVAILABLE));
                ResultInfo resultInfo = getFetchBalanceErrorResultInfo(ResultCode.FETCH_POST_PAID_BALANCE_INFO_EXCEPTION);
                body.setResultInfo(resultInfo);
            }

        } else if (EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod().equals(serviceRequest.getPaymentMode())) {

            PayMethod payMethod = getAccountBalance(EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod(),
                    nativeCashierInfoResponse);

            if (null != payMethod) {
                ChannelAccountQueryResponse channelAccountQueryResponse = null;
                ChannelAccountQueryRequest channelAccountQueryRequest = getChannelAccountRequest(request,
                        serviceRequest, payMethod);
                if (merchantPreferenceService.isFullPg2TrafficEnabled(serviceRequest.getMid())) {
                    channelAccountQueryRequest.getBody().setRoute(Routes.PG2);
                    channelAccountQueryRequest.getBody().setMerchantId(serviceRequest.getMid());
                    channelAccountQueryRequest.getBody().setPayerUserId(customerId);
                }
                try {
                    channelAccountQueryResponse = cashier.channelAccountQuery(channelAccountQueryRequest);
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Exception fetching Advance Deposit Balance", e);
                    throw FetchBalanceInfoException.getException();
                }
                if (channelAccountQueryResponse == null || channelAccountQueryResponse.getBody() == null) {
                    throw FetchBalanceInfoException.getException();
                }
                body = convertToBalanceInfoResponse(channelAccountQueryResponse, serviceRequest.getTxnToken(),
                        serviceRequest.getPaymentMode());

                if (body.getBalanceInfo() != null && StringUtils.isNotBlank(body.getBalanceInfo().getValue())
                        && body.getBalanceInfo().getValue().startsWith(".")) {
                    body.getBalanceInfo().setValue("0" + body.getBalanceInfo().getValue());
                }

                if (ResultCode.SUCCESS.getResultStatus().equals(body.getResultInfo().getResultStatus())) {
                    updateADABalanceInCashierResponse(channelAccountQueryResponse, serviceRequest,
                            nativeCashierInfoResponse);
                }
            } else {
                throw OperationNotSupportedException.getException();
            }

        } else if (EPayMethod.GIFT_VOUCHER.getMethod().equals(serviceRequest.getPaymentMode())) {

            PayMethod payMethod = getAccountBalance(EPayMethod.GIFT_VOUCHER.getMethod(), nativeCashierInfoResponse);

            if (null != payMethod) {

                ChannelAccountQueryResponse channelAccountQueryResponse = null;
                ChannelAccountQueryRequest channelAccountQueryRequest = getChannelAccountRequest(request,
                        serviceRequest, payMethod);
                try {
                    channelAccountQueryResponse = cashier.channelAccountQuery(channelAccountQueryRequest);
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Exception fetching Merchant Gift Voucher Balance", e);
                    throw FetchBalanceInfoException.getException();
                }
                if (channelAccountQueryResponse == null || channelAccountQueryResponse.getBody() == null) {
                    throw FetchBalanceInfoException.getException();
                }
                body = convertToBalanceInfoResponse(channelAccountQueryResponse, serviceRequest.getTxnToken(),
                        serviceRequest.getPaymentMode());

                for (DetailedBalanceInfo mgvBalanceInfo : body.getDetailedBalanceInfos()) {
                    if (mgvBalanceInfo != null && mgvBalanceInfo.getBalanceInfo() != null
                            && StringUtils.isNotBlank(mgvBalanceInfo.getBalanceInfo().getValue())
                            && mgvBalanceInfo.getBalanceInfo().getValue().startsWith(".")) {
                        mgvBalanceInfo.getBalanceInfo().setValue("0" + mgvBalanceInfo.getBalanceInfo().getValue());
                    }
                }
                if (body.getBalanceInfo() != null && StringUtils.isNotBlank(body.getBalanceInfo().getValue())
                        && body.getBalanceInfo().getValue().startsWith(".")) {
                    body.getBalanceInfo().setValue("0" + body.getBalanceInfo().getValue());
                }
            } else {
                throw OperationNotSupportedException.getException();
            }

        } else if (EPayMethod.LOYALTY_POINT.getMethod().equals(serviceRequest.getPaymentMode())) {

            PayMethod payMethod = getAccountBalance(EPayMethod.LOYALTY_POINT.getMethod(), nativeCashierInfoResponse);

            if (null != payMethod) {
                Map<String, Object> context = new HashMap<>();
                context.put("mid", serviceRequest.getMid());
                CheckLoyaltyBalanceResponse response = null;
                String rootUserId = request.getBody().getRootUserId();
                if (iPgpFf4jClient.checkWithdefault(THEIA_LOYALTY_POINTS_CONSULT, context, true)
                        && StringUtils.isNotBlank(request.getBody().getExchangeRate())) {
                    if (StringUtils.isNotBlank(rootUserId)) {
                        customerId = rootUserId;
                    }
                    response = fundService.getAccountBalanceFromFundServiceV2(request, customerId);
                    LOGGER.info("Converting loyaltypoints to balanceInfo response");
                    body = fundService.mapLoyaltyPointsResponseToBody(response);
                } else if (iPgpFf4jClient.checkWithdefault(THEIA_LOYALTY_POINTS_MIGRATION, context, true)) {
                    response = fundService.getAccountBalanceFromFundService(serviceRequest);
                    LOGGER.info("Converting loyaltypoints to balanceInfo response");
                    body = fundService.mapLoyaltyPointsResponseToBody(response);
                } else {
                    ChannelAccountQueryResponse channelAccountQueryResponse = null;
                    ChannelAccountQueryRequest channelAccountQueryRequest = getChannelAccountRequest(request,
                            serviceRequest, payMethod);
                    try {
                        channelAccountQueryResponse = cashier.channelAccountQuery(channelAccountQueryRequest);
                    } catch (FacadeCheckedException e) {
                        LOGGER.error("Exception fetching Loyalty Point Balance", e);
                        throw FetchBalanceInfoException.getException();
                    }
                    if (channelAccountQueryResponse == null || channelAccountQueryResponse.getBody() == null) {
                        throw FetchBalanceInfoException.getException();
                    }
                    body = convertToBalanceInfoResponse(channelAccountQueryResponse, serviceRequest.getTxnToken(),
                            serviceRequest.getPaymentMode());
                }
            } else {
                throw OperationNotSupportedException.getException();
            }
        }
        return new BalanceInfoResponse(new ResponseHeader(), body);
    }

    private BalanceInfoResponseBody transformToBalanceInfoResponse(PaytmDigitalCreditResponse digitalCreditResponse,
            BalanceInfoServiceRequest serviceRequest, FetchBalanceInfoRequest request) {
        BalanceInfoResponseBody balanceInfoResponse = new BalanceInfoResponseBody();
        if (digitalCreditResponse == null || digitalCreditResponse.getStatusCode() != 0
                || CollectionUtils.isEmpty(digitalCreditResponse.getResponse())) {
            LOGGER.error("Exception while fetching Postpaid Balance");
            EventUtils.pushTheiaEvents(EventNameEnum.POST_PAID_CHECK_BALANCE, new ImmutablePair<>(
                    POSTPAID_ACCOUNT_STATUS, POSTPAID_SYSTEM_UNAVAILABLE));
            ResultInfo resultInfo = getFetchBalanceErrorResultInfo(ResultCode.FETCH_POST_PAID_BALANCE_INFO_EXCEPTION);
            balanceInfoResponse.setResultInfo(resultInfo);
            return balanceInfoResponse;
        }
        CheckBalanceResponse checkBalanceResponse = digitalCreditResponse.getResponse().get(0);
        balanceInfoResponse.setDisplayMessage(checkBalanceResponse.getDisplayMessage());
        balanceInfoResponse.setInfoButtonMessage(checkBalanceResponse.getInfoButtonMessage());
        balanceInfoResponse.setAccountStatus(checkBalanceResponse.getAccountStatus());
        balanceInfoResponse.setMictLines(checkBalanceResponse.getMictLines());
        balanceInfoResponse.setPasscodeRequired(checkBalanceResponse.isPasscodeRequired());
        balanceInfoResponse.setFullTnCDetails(checkBalanceResponse.getFullTnCDetails());
        balanceInfoResponse.setKycVersion(checkBalanceResponse.getKycVersion());
        balanceInfoResponse.setKycCode(checkBalanceResponse.getKycCode());
        Money accountBalance = new Money(EnumCurrency.INR, String.valueOf(checkBalanceResponse.getAmount()));
        if (checkBalanceResponse.getMonthlySanctionLimit() != null) {
            balanceInfoResponse.setMonthlySanctionLimit(new Money(EnumCurrency.INR, String.valueOf(checkBalanceResponse
                    .getMonthlySanctionLimit())));
        }
        if (checkBalanceResponse.getMonthlyAvailableSanctionLimit() != null) {
            balanceInfoResponse.setMonthlyAvailableSanctionLimit(new Money(EnumCurrency.INR, String
                    .valueOf(checkBalanceResponse.getMonthlyAvailableSanctionLimit())));
        }
        if (StringUtils.equals(checkBalanceResponse.getAccountStatus(),
                TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_ACTIVE)
                || StringUtils.equals(checkBalanceResponse.getAccountStatus(),
                        TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_NOT_ACTIVE)) {
            balanceInfoResponse.setEnable(true);
        } else {
            balanceInfoResponse.setEnable(false);
            /**
             * Hack For Native App Backend Error Handling for Deactive and
             * Frozen Account
             */
            /**
             * Commented after management Decision
             */
            /*
             * if
             * (!EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equalsIgnoreCase(request
             * .getHead().getWorkFlow())) { LOGGER.info(
             * "Setting Balance as 0 for Postpaid balance for Frozen and Deative Account"
             * ); accountBalance.setValue("0");
             * digitalCreditResponse.getResponse().get(0).setAmount(0.0);
             * ResultInfo resultInfo =
             * getFetchBalanceErrorResultInfo(ResultCode.
             * FETCH_POST_PAID_BALANCE_INFO_EXCEPTION);
             * resultInfo.setResultMsg(checkBalanceResponse
             * .getInfoButtonMessage());
             * balanceInfoResponse.setResultInfo(resultInfo); }
             */
            if (StringUtils.isBlank(checkBalanceResponse.getInfoButtonMessage())) {
                balanceInfoResponse.setInfoButtonMessage(ConfigurationUtil.getProperty(
                        TheiaConstant.PaytmDigitalCreditConstant.DEFAULT_INFO_BUTTON_MESSAGE,
                        "We are facing some issue with postpaid, please use other payment options"));
            }
        }
        EventUtils.pushTheiaEvents(EventNameEnum.POST_PAID_CHECK_BALANCE, new ImmutablePair<>(POSTPAID_ACCOUNT_STATUS,
                checkBalanceResponse.getAccountStatus()));
        balanceInfoResponse.setBalanceInfo(accountBalance);

        return balanceInfoResponse;
    }

    private ResultInfo getFetchBalanceErrorResultInfo(ResultCode resultCode) {

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultMsg(resultCode.getResultMsg());
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getResultCodeId());
        return resultInfo;
    }

    private void updatePostPaidBalanceInCashierResponse(CheckBalanceResponse checkBalanceResponse,
            BalanceInfoServiceRequest serviceRequest) {

        nativeSessionUtil.setPostPaidMPinRequired(serviceRequest.getTxnToken(),
                Boolean.valueOf(checkBalanceResponse.isPasscodeRequired()));

        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(serviceRequest
                .getTxnToken());
        PayMethod payMethod = null;
        PayMethod addMoneyPayMethod = null;

        payMethod = processTransactionUtil.getPayMethod(cashierInfoResponse,
                EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());

        if (cashierInfoResponse != null && cashierInfoResponse.getBody() != null
                && EPayMode.ADDANDPAY.equals(cashierInfoResponse.getBody().getPaymentFlow())) {
            addMoneyPayMethod = processTransactionUtil.getPayMethodForAddnPay(cashierInfoResponse,
                    EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        }

        String extendInfo = workFlowHelper.getExtendInfoForDigitalCreditBalanceResponse(checkBalanceResponse);

        Money accountBalance = new Money(EnumCurrency.INR, String.valueOf(checkBalanceResponse.getAmount()));
        DigitalCreditAccountInfo digitalCreditAccountInfo = new DigitalCreditAccountInfo(
                checkBalanceResponse.getAccountID(), accountBalance, extendInfo);
        List<PayChannelBase> payChannelBases = new ArrayList<>();
        BalanceChannel balanceChannel = new BalanceChannel();
        balanceChannel.setBalanceInfo(digitalCreditAccountInfo);
        payChannelBases.add(balanceChannel);
        if (payMethod != null) {
            LOGGER.info("Adding account info in pospaid merchant payment mode");
            payMethod.setPayChannelOptions(payChannelBases);
        }
        if (addMoneyPayMethod != null) {
            LOGGER.info("Adding account info in pospaid add money payment mode");
            addMoneyPayMethod.setPayChannelOptions(payChannelBases);
        }
        nativeSessionUtil.setCashierInfoResponse(serviceRequest.getTxnToken(), cashierInfoResponse);
    }

    private PaytmDigitalCreditRequest getPaytmDigitalCreditRequest(String txnToken) {
        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(txnToken);
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
        paytmDigitalCreditRequest.setPgmid(orderDetail.getMid());
        paytmDigitalCreditRequest.setAmount(Double.parseDouble(orderDetail.getTxnAmount().getValue()));
        paytmDigitalCreditRequest.setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        return paytmDigitalCreditRequest;
    }

    private void updatePDCBalanceInCashierResponse(ChannelAccountQueryResponse channelAccountQueryResponse,
            BalanceInfoServiceRequest serviceRequest) {
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(serviceRequest
                .getTxnToken());
        PayMethod payMethod = processTransactionUtil.getPayMethod(cashierInfoResponse,
                EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        ChannelAccount channelAccount = channelAccountQueryResponse.getBody().getChannelAccountViews().get(0)
                .getChannelAccounts().get(0);
        Money accountBalance = new Money(EnumCurrency.INR, AmountUtils.getTransactionAmountInRupee(channelAccount
                .getAccountBalance().getValue()));
        DigitalCreditAccountInfo digitalCreditAccountInfo = new DigitalCreditAccountInfo(channelAccount.getAccountNo(),
                accountBalance, channelAccount.getExtendInfo());
        List<PayChannelBase> payChannelBases = new ArrayList<>();
        BalanceChannel balanceChannel = new BalanceChannel();
        balanceChannel.setBalanceInfo(digitalCreditAccountInfo);
        payChannelBases.add(balanceChannel);
        payMethod.setPayChannelOptions(payChannelBases);
        nativeSessionUtil.setCashierInfoResponse(serviceRequest.getTxnToken(), cashierInfoResponse);
    }

    private void updateADABalanceInCashierResponse(ChannelAccountQueryResponse channelAccountQueryResponse,
            BalanceInfoServiceRequest serviceRequest, NativeCashierInfoResponse cashierInfoResponse) {
        PayMethod payMethod = processTransactionUtil.getPayMethod(cashierInfoResponse,
                EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod());
        ChannelAccount channelAccount = channelAccountQueryResponse.getBody().getChannelAccountViews().get(0)
                .getChannelAccounts().get(0);
        Money accountBalance = new Money(EnumCurrency.INR, AmountUtils.getTransactionAmountInRupee(channelAccount
                .getAccountBalance().getValue()));
        AdvanceDepositAccountInfo advanceDepositAccountInfo = new AdvanceDepositAccountInfo(
                channelAccount.getAccountNo(), accountBalance, channelAccount.getExtendInfo());
        List<PayChannelBase> payChannelBases = new ArrayList<>();
        BalanceChannel balanceChannel = new BalanceChannel();
        balanceChannel.setBalanceInfo(advanceDepositAccountInfo);
        payChannelBases.add(balanceChannel);
        payMethod.setPayChannelOptions(payChannelBases);
        nativeSessionUtil.setCashierInfoResponse(serviceRequest.getTxnToken(), cashierInfoResponse);
    }

    private ChannelAccountQueryRequest getChannelAccountRequest(FetchBalanceInfoRequest request,
            BalanceInfoServiceRequest serviceRequest, PayMethod payMethod) {
        UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(serviceRequest.getTxnToken());
        if (userDetailsBiz == null) {
            // in case user details biz is null we will show user the message
            // that currently we cannot fetch your balance message
            throw new FetchBalanceInfoException(OfflinePaymentUtils.resultInfo(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE));
        }
        try {
            String userId = userDetailsBiz.getInternalUserId();
            EnvInfoRequestBean envInfoRequestBean = getEnvInfoBean(request.getHead().getChannelId().getValue());
            String productCode = ERequestType.NATIVE_PAY.getProductCode();
            String productCodeId = ProductCodes.getProductByProductCode(productCode).getId();
            String mid;
            final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                    .fetchMerchanData(serviceRequest.getMid());
            if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
                mid = merchantMappingResponse.getResponse().getAlipayId();
            } else {
                throw FetchBalanceInfoException.getException();
            }
            List<PayMethodInfo> payMethodInfoList = new ArrayList<>();
            Map<String, String> extendInfo = nativeSessionUtil.getExtendInfo(request.getHead().getTxnToken());
            String strExtendInfo = null;
            if (extendInfo != null && !extendInfo.isEmpty()) {
                strExtendInfo = JsonMapper.mapObjectToJson(extendInfo);
            }
            EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(envInfoRequestBean);
            payMethodInfoList.add(new PayMethodInfo(payMethod.getPayMethod(), strExtendInfo));
            final ChannelAccountQueryBody.ChannelAccountQueryBuilder body = new ChannelAccountQueryBody.ChannelAccountQueryBuilder(
                    productCodeId, envInfo).merchantId(mid).payerUserId(userId).payMethodInfos(payMethodInfoList);
            return new ChannelAccountQueryRequest(RequestHeaderGenerator.getHeader(ApiFunctions.CHANNEL_ACCOUNT_QUERY),
                    body.build());
        } catch (Exception e) {
            LOGGER.error("Exception occured forming channel account query request {} ", e);
            throw FetchBalanceInfoException.getException();
        }
    }

    private EnvInfoRequestBean getEnvInfoBean(String channel) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        EnvInfoRequestBean envInfoRequestBean = EnvInfoUtil.fetchEnvInfo(request);
        ETerminalType terminalType = getTerminalTypeByTerminal(channel);
        if (envInfoRequestBean != null && terminalType != null) {
            envInfoRequestBean.setTerminalType(terminalType);
        }
        return envInfoRequestBean;
    }

    private BalanceInfoResponseBody convertToBalanceInfoResponse(AccountInfo accountInfo) {
        BalanceInfoResponseBody body = new BalanceInfoResponseBody();
        body.setBalanceInfo(accountInfo.getAccountBalance());
        return body;
    }

    private BalanceInfoResponseBody convertToBalanceInfoResponse(
            ChannelAccountQueryResponse channelAccountQueryResponse, String txnToken, String payMethod) {
        BalanceInfoResponseBody body = new BalanceInfoResponseBody();
        ChannelAccountQueryResponseBody channelAccountQueryResponseBody = channelAccountQueryResponse.getBody();
        body.setResultInfo(new ResultInfo(ResultCode.SUCCESS.getResultStatus(), String.valueOf(ResultCode.SUCCESS
                .getResultCodeId()), ResultCode.SUCCESS.getResultMsg()));

        if (!TheiaConstant.ExtraConstants.SUCCESS.equals(channelAccountQueryResponseBody.getResultInfo()
                .getResultCode())) {
            body.setResultInfo(new ResultInfo(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultStatus(), String
                    .valueOf(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultCodeId()),
                    ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultMsg()));
            return body;
        }

        if (CollectionUtils.isEmpty(channelAccountQueryResponseBody.getChannelAccountViews())) {
            throw OperationNotSupportedException.getException();
        }

        ChannelAccountView channelAccountView = channelAccountQueryResponseBody.getChannelAccountViews().get(0);

        if (!channelAccountView.isEnableStatus()) {
            body.setResultInfo(new ResultInfo(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultStatus(), String
                    .valueOf(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultCodeId()),
                    ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultMsg()));
            return body;
        }

        if (CollectionUtils.isEmpty(channelAccountView.getChannelAccounts())) {
            throw OperationNotSupportedException.getException();
        }

        /*
         * As MGV is the only Paymode with multiple instances of balances, we
         * create BalanceInfo differently
         */
        if (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethod)) {
            return createAndReturnBalanceInfoResponseForMgv(body, channelAccountView);
        }

        ChannelAccount channelAccount = channelAccountView.getChannelAccounts().get(0);
        if (channelAccount.isStatus()) {
            Money accountInfo = new Money(EnumCurrency.INR, AmountUtils.getTransactionAmountInRupee(channelAccount
                    .getAccountBalance().getValue()));
            body.setBalanceInfo(accountInfo);
            // Adding exchange rate and loyalty points in response
            setDetailsForLoyaltyPoints(channelAccount, body, payMethod);
            if (channelAccount.getAccountNo() != null && channelAccount.getExtendInfo() != null) {
                nativeSessionUtil.setAdvanceDepositDetails(txnToken, channelAccount);
            }
        } else {
            body.setResultInfo(new ResultInfo(ResultCode.UNABLE_TO_FETCH_BALANCE.getResultStatus(), String
                    .valueOf(ResultCode.UNABLE_TO_FETCH_BALANCE.getResultCodeId()), ResultCode.UNABLE_TO_FETCH_BALANCE
                    .getResultMsg()));
        }

        return body;
    }

    private void setDetailsForLoyaltyPoints(ChannelAccount channelAccount, BalanceInfoResponseBody body,
            String payMethod) {
        if (EPayMethod.LOYALTY_POINT.getMethod().equals(payMethod)) {
            if (StringUtils.isNotEmpty(channelAccount.getAvailablePoints())) {
                body.setAvailablePoints(channelAccount.getAvailablePoints());
            }
            if (StringUtils.isNotEmpty(channelAccount.getExchangeRate())) {
                body.setExchangeRate(channelAccount.getExchangeRate());
            }
        }
    }

    private PayMethod getAccountBalance(String ePayMethod, NativeCashierInfoResponse cashierInfoResponse) {
        return processTransactionUtil.getPayMethod(cashierInfoResponse, ePayMethod);
    }

    private BalanceInfoResponseBody convertToBalanceInfoResponse(FetchAccountBalanceResponse accountBalanceResponse,
            boolean investmentAsFundSourceFlag) {
        ConfigurationUtil conf = new ConfigurationUtil();
        BalanceInfoResponseBody body = new BalanceInfoResponseBody();
        body.setResultInfo(new ResultInfo(ResultCode.SUCCESS.getResultStatus(), String.valueOf(ResultCode.SUCCESS
                .getResultCodeId()), ResultCode.SUCCESS.getResultMsg()));

        if (accountBalanceResponse == null || PPBL_FAILURE_STATUS.equalsIgnoreCase(accountBalanceResponse.getStatus())) {
            body.setResultInfo(new ResultInfo(ResultCode.PPBL_SYSTEM_UNAVAILABLE.getResultStatus(), String
                    .valueOf(ResultCode.PPBL_SYSTEM_UNAVAILABLE.getResultCodeId()), ResultCode.PPBL_SYSTEM_UNAVAILABLE
                    .getResultMsg()));
            body.setBalanceInfo(null);
        } else {
            if (body != null
                    && accountBalanceResponse.getAccountState() != null
                    && (PaymentBankAccountStatus.DEBIT_FREEZED.name().equals(accountBalanceResponse.getAccountState())
                            || PaymentBankAccountStatus.TOTAL_FREEZED.name().equals(
                                    accountBalanceResponse.getAccountState()) || PaymentBankAccountStatus.CLOSED.name()
                            .equals(accountBalanceResponse.getAccountState()))) {
                body.setDisplayMessage(conf
                        .getProperty(TheiaConstant.ExtraConstants.PAYMENT_BANK_FROZEN_ACCOUNT_MESSAGE));
            }
            if (investmentAsFundSourceFlag) {
                body.setRedeemableInvestmentBalance(accountBalanceResponse.getRedeemableInvestmentBalance());
                body.setIsRedemptionAllowed(accountBalanceResponse.isRedemptionAllowed());
                body.setPartnerBankBalances(accountBalanceResponse.getJsonPartnerBankBalances());
                body.setInvestmentTnCUrl(ConfigurationUtil.getProperty(INVESTMENT_AS_FUNDING_SOURCE_TNC_URL,
                        DEFAULT_INVESTMENT_FUNDING_TNC_URL));
                EventLogger.pushEventLog(MDC.get("MID"), MDC.get("ORDER_ID"),
                        EventNameEnum.USE_INVESTMENT_AS_FUNDING_SOURCE, new HashMap<String, String>() {
                            {
                                put(ISREDEMPTIONALLOWED, String.valueOf(accountBalanceResponse.isRedemptionAllowed()));
                            }
                        });
            }
            body.setAccountStatus(accountBalanceResponse.getAccountState());
            body.setBalanceInfo(new Money(String.valueOf(accountBalanceResponse.getEffectiveBalance())));
        }
        return body;
    }

    public boolean isFeatureEnabledOnMid(String mid) {
        if (mid == null) {
            return false;
        }
        boolean isOnUsMerchant = merchantExtendInfoUtils.isMerchantOnPaytm(mid);
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        if (isOnUsMerchant) {
            return iPgpFf4jClient.checkWithdefault(THEIA_USE_INVESTMENT_AS_FUNDING_SOURCE, context, false);
        } else {
            return iPgpFf4jClient.checkWithdefault(THEIA_USE_INVESTMENT_AS_FUNDING_SOURCE_OFF_US, context, false);
        }
    }

    private NativeCashierInfoResponse fetchNativeCashierInfoResponse(FetchBalanceInfoRequest request) {

        NativeCashierInfoResponse cashierInfoResponse = null;
        try {
            cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
            if (cashierInfoResponse == null) {
                if (TokenType.SSO.equals(request.getHead().getTokenType())) {
                    nativePaymentUtil.fetchPaymentOptionsWithSsoToken(request.getHead(), request.getBody().getMid(),
                            false, request.getBody().getTwoFADetails());
                } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
                    nativePaymentUtil.fetchPaymentOptionsForGuest(request.getHead(), request.getBody().getMid(), false,
                            request.getBody().getReferenceId(), request.getBody().getTwoFADetails());
                } else {
                    nativePaymentUtil.fetchPaymentOptions(request.getHead(), null, request.getBody().getTwoFADetails());
                }
                cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());

            }
        } catch (PaymentRequestProcessingException e) {
            LOGGER.error("Error in fetching Native Cashier Info Response");

            if (TokenType.SSO.equals(request.getHead().getTokenType())
                    && ResultCode.INVALID_SSO_TOKEN.getCode().equals(e.getResultInfo().getResultCode())) {
                throw RequestValidationException.getException(ResultCode.INVALID_SSO_TOKEN);
            }

        } catch (Exception e) {
            LOGGER.error("Error in fetching Native Cashier Info Response");
        }
        return cashierInfoResponse;
    }

    private BalanceInfoResponseBody createAndReturnBalanceInfoResponseForMgv(
            BalanceInfoResponseBody balanceInfoResponseBody, ChannelAccountView channelAccountView) {

        balanceInfoResponseBody.setDetailedBalanceInfos(new ArrayList<>());
        Map<String, DetailedBalanceInfo> templateTobalanceMap = new HashMap<>();
        Money totalMgvBalanceInfo = new Money("0.00");

        for (ChannelAccount channelAccount : channelAccountView.getChannelAccounts()) {

            if (channelAccountView.isEnableStatus()) {
                // Total Balance of all the GVs
                totalMgvBalanceInfo = getUpdatedBalanceInInr(totalMgvBalanceInfo, channelAccount.getAvailableBalance());

                if (templateTobalanceMap.containsKey(channelAccount.getTemplateId())) {
                    // If already exist , update balance for that templateId
                    DetailedBalanceInfo detailedBalanceInfo = templateTobalanceMap.get(channelAccount.getTemplateId());
                    detailedBalanceInfo.setBalanceInfo(getUpdatedBalanceInInr(detailedBalanceInfo.getBalanceInfo(),
                            channelAccount.getAvailableBalance()));
                    templateTobalanceMap.put(channelAccount.getTemplateId(), detailedBalanceInfo);
                } else {
                    // If not present , create new MgvBalanceInfo for new
                    // templateId
                    String templateId = channelAccount.getTemplateId();
                    Money balance = new Money(AmountUtils.getTransactionAmountInRupee(channelAccount
                            .getAvailableBalance().getValue()));
                    DetailedBalanceInfo detailedBalanceInfo = new DetailedBalanceInfo(templateId, balance,
                            channelAccount.getTemplateName(), null);
                    templateTobalanceMap.put(templateId, detailedBalanceInfo);
                    balanceInfoResponseBody.getDetailedBalanceInfos().add(detailedBalanceInfo);
                }
            } else {
                balanceInfoResponseBody.setResultInfo(new ResultInfo(ResultCode.UNABLE_TO_FETCH_BALANCE
                        .getResultStatus(), String.valueOf(ResultCode.UNABLE_TO_FETCH_BALANCE.getResultCodeId()),
                        ResultCode.UNABLE_TO_FETCH_BALANCE.getResultMsg()));
                return balanceInfoResponseBody;
            }
        }

        balanceInfoResponseBody.setBalanceInfo(totalMgvBalanceInfo);
        return balanceInfoResponseBody;
    }

    private Money getUpdatedBalanceInInr(Money firstAmount, com.paytm.pgplus.facade.common.model.Money secondAmount) {
        // Here firstAmount is in Rupees and second is in Paise
        Double totalBalance = Double.parseDouble(AmountUtils.getTransactionAmountInPaise(firstAmount.getValue()))
                + Double.parseDouble(secondAmount.getValue());
        String value = AmountUtils.getTransactionAmountInRupee(totalBalance.toString());
        return new Money(value);
    }

    public BalanceInfoResponseBody fetchPPBLBalance(BalanceInfoServiceRequest serviceRequest,
            FetchBalanceInfoRequest request) {
        BalanceInfoResponseBody balanceInfoResponseBody = new BalanceInfoResponseBody();
        if (isFeatureEnabledOnMid(THEIA_FALLBACK_PBBL_V4_ENABLED, request.getBody().getMid(), false)) {
            balanceInfoResponseBody = fetchPPBLBalanceV4(serviceRequest, request);
        } else {
            balanceInfoResponseBody = fetchPPBLBalanceV5(serviceRequest, request);
        }
        return balanceInfoResponseBody;
    }

    public BalanceInfoResponseBody fetchPPBLBalanceV4(BalanceInfoServiceRequest serviceRequest,
            FetchBalanceInfoRequest request) {
        try {
            BalanceInfoResponseBody balanceInfoResponseBody = null;
            FetchAccountBalanceRequest fetchAccountBalanceRequest = new FetchAccountBalanceRequest();
            fetchAccountBalanceRequest.setAccountType(TheiaConstant.RequestParams.SAVING_ACCOUNT);
            fetchAccountBalanceRequest.setToken(serviceRequest.getSsoToken());
            boolean investmentAsFundSourceFlag = isFeatureEnabledOnMid(request.getBody().getMid());
            FetchAccountBalanceResponse accountBalanceResponse = null;
            if (investmentAsFundSourceFlag) {
                accountBalanceResponse = paymentsBankAccountQuery
                        .queryAccountDetailsFromBankMiddlewareV4(fetchAccountBalanceRequest);
            } else {
                accountBalanceResponse = paymentsBankAccountQuery
                        .queryAccountDetailsFromBankMiddleware(fetchAccountBalanceRequest);
            }
            nativeSessionUtil.setAccountBalanceResponseInCache(request.getHead().getTxnToken(), accountBalanceResponse);
            balanceInfoResponseBody = convertToBalanceInfoResponse(accountBalanceResponse, investmentAsFundSourceFlag);
            return balanceInfoResponseBody;
        } catch (FacadeCheckedException e) {
            throw FetchBalanceInfoException.getException(e);
        }
    }

    public BalanceInfoResponseBody fetchPPBLBalanceV5(BalanceInfoServiceRequest serviceRequest,
            FetchBalanceInfoRequest request) {
        try {
            BalanceInfoResponseBody balanceInfoResponseBody = null;
            FetchAccountBalanceResponse fetchAccountBalanceResponse = null;
            FetchPPBLUserBalanceRequest fetchPPBLUserBalanceRequest = new FetchPPBLUserBalanceRequest();
            fetchPPBLUserBalanceRequest.setUserToken(serviceRequest.getSsoToken());
            fetchPPBLUserBalanceRequest.setIsFdBalanceRequired(true);
            Boolean investmentAsFundSourceFlag = isFeatureEnabledOnMid(request.getBody().getMid());
            FetchPPBLUserBalanceResponse fetchPPBLUserBalanceResponse = paymentsBankAccountQuery
                    .queryAccountDetailsFromBankV5(fetchPPBLUserBalanceRequest);
            fetchAccountBalanceResponse = workFlowHelper.fetchUserAccountDetails(fetchPPBLUserBalanceResponse, request
                    .getBody().getMid());
            balanceInfoResponseBody = convertToBalanceInfoResponse(fetchAccountBalanceResponse,
                    investmentAsFundSourceFlag);
            nativeSessionUtil.setAccountBalanceResponseInCache(request.getHead().getTxnToken(),
                    fetchAccountBalanceResponse);
            return balanceInfoResponseBody;
        } catch (FacadeCheckedException e) {
            throw FetchBalanceInfoException.getException(e);
        }
    }

    private boolean isFeatureEnabledOnMid(String feature, String mid, Boolean defaultValue) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(feature)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        return iPgpFf4jClient.checkWithdefault(feature, context, defaultValue);
    }
}
