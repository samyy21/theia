package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierMerchant;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.model.PSULimit;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.enums.TransactionStatus;
import com.paytm.pgplus.common.model.EnvInfo;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.emisubvention.models.CardType;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetails;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.savedcardclient.service.ICacheCardService;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.subscriptionClient.utils.SubscriptionUtil;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;
import com.paytm.pgplus.theia.enums.TransactionMode;
import com.paytm.pgplus.theia.mapper.TheiaCashierMapper;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import mockit.MockUp;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @createdOn 1-July-2021
 * @author kalluru nanda kishore
 */
public class PaymentRequestHelperTest {

    @InjectMocks
    PaymentRequestHelper paymentRequestHelper;

    @Mock
    ITheiaSessionDataService sessionDataService;

    @Mock
    ICacheCardService cacheCardService;

    @Mock
    ISavedCardService savedCardService;

    @Mock
    ICashierCacheService cashierCacheServiceImpl;

    @Mock
    TheiaCashierMapper theiaCashierMapper;

    @Mock
    TheiaCashierUtil theiaCashierUtil;

    @Mock
    PaymentRequestValidation paymentRequestValidation;

    @Mock
    TheiaPromoUtil theiaPromoUtil;

    @Mock
    private ISubscriptionService subscriptionService;

    @Mock
    private TheiaResponseGenerator theiaResponseGenerator;

    @Mock
    private PaymentOTPService paymentOTPUtil;

    @Mock
    private SubscriptionUtil subscriptionUtil;

    @Mock
    private CardUtils cardUtils;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    CashierUtilService cashierUtilService;

    @Mock
    WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Mock
    WorkFlowHelper workFlowHelper;

    @Mock
    RiskExtendedInfoUtil riskExtendedInfoUtil;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void prepareCashierRequest() throws PaytmValidationException, CashierCheckedException {

        HttpServletRequest request = new MockHttpServletRequest();

        setUpRequest(request);
        request.setAttribute("cacheCardToken", "dsd");
        assertNotNull(paymentRequestHelper.prepareCashierRequest(request));
        setUpRequest(request);
        assertNotNull(paymentRequestHelper.prepareCashierRequest(request));
        setUpRequest(request).setSubsTypes(null);
        doNothing().when(paymentOTPUtil).validateIfPaymentOTP(any());
        when(theiaPromoUtil.applyPromocode(any(), any(), any(), any())).thenReturn(new PromoCodeResponse());
        when(theiaCashierMapper.getCashierWorkflow(any(), any(), any())).thenReturn(CashierWorkflow.ADD_MONEY_UPI);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentType(PaymentType.ONLY_WALLET);
        paymentInfo.setServiceAmount(50l);
        when(theiaCashierUtil.computeRequestTypeAndAmount(any(), any(), any(), any())).thenReturn(paymentInfo);
        new MockUp<ExtendedInfoUtil>() {

            @mockit.Mock
            public Map<String, String> selectExtendedInfo(final TheiaPaymentRequest paymentRequest,
                    final MerchantInfo merchantInfo, final TransactionInfo txnInfo, final PaymentInfo paymentInfo,
                    final CashierWorkflow cashierWorkflow, final TransactionConfig txnConfig,
                    final LoginInfo loginInfo, final PromoCodeResponse applyPromoCodeResponse,
                    ExtendedInfoRequestBean extendedInfoRequestBean) {
                return new HashMap<>();
            }
        };
        when(riskExtendedInfoUtil.selectRiskExtendedInfo(any())).thenReturn(new HashMap<>());
        when(workFlowRequestCreationHelper.isAddMoneyToWallet(any(), any())).thenReturn(true);
        doNothing().when(workFlowRequestCreationHelper).updateRiskExtendInfoForAddMoney(any(), any(), any(), any(),
                any(), any());
        doNothing().when(riskExtendedInfoUtil).setMerchantUserIdInRiskExtendInfo(any(), any());
        when(cashierCacheServiceImpl.getPaymentRetryCountFromCache(any())).thenReturn(10);
        when(sessionDataService.getDigitalCreditInfoFromSession(anyObject(), anyBoolean())).thenReturn(
                new DigitalCreditInfo());
        TransactionInfo transInfo = new TransactionInfo();
        transInfo.setSsoToken("sso");
        when(sessionDataService.getTxnInfoFromSession(anyObject(), anyBoolean())).thenReturn(transInfo);
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID)).thenReturn(
                "clientId");
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY))
                .thenReturn("clientSecret");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "123456");
        when(cardUtils.fetchBinDetails(request.getParameter(PaymentRequestParam.CARD_NUMBER.getValue()))).thenReturn(
                new BinDetail());
        when(theiaCashierMapper.prepareValidationRequest(any(), any(), any(), any())).thenReturn(
                new ValidationRequest(TransactionMode.NB.getMode(), Collections.EMPTY_LIST, TransactionMode.PPBL
                        .getMode(), true, "entity", "bankid", new PSULimit())).thenReturn(
                new ValidationRequest("txnMode", Collections.emptyList(), "bank", true, "entity", "bankid",
                        new PSULimit()));
        when(sessionDataService.getSavingsAccountInfoFromSession(anyObject(), anyBoolean())).thenReturn(
                new SavingsAccountInfo());
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID)).thenReturn(
                "clientId");
        when(configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY))
                .thenReturn("clientSecret");
        when(theiaCashierMapper.prepareCashierMerchant(any(), any(), any()))
                .thenReturn(new CashierMerchant("merchant"));
        SarvatraVPAMapInfo sarvatraVPAMapInfo = new SarvatraVPAMapInfo();
        sarvatraVPAMapInfo.setSarvatraVpaMapInfo(Collections.singletonMap("vpa", "upi"));
        PaytmVpaDetails paytmVpaDetails = mock(PaytmVpaDetails.class);
        sarvatraVPAMapInfo.setUserProfileSarvatra(new UserProfileSarvatra("ok", paytmVpaDetails));
        SarvatraVpaDetails sarvatraVpaDetails = new SarvatraVpaDetails();
        sarvatraVpaDetails.setName("vpa");
        when(paytmVpaDetails.getVpaDetails()).thenReturn(Collections.singletonList(sarvatraVpaDetails));
        when(sessionDataService.getSarvatraVPAInfoFromSession(anyObject(), anyBoolean()))
                .thenReturn(sarvatraVPAMapInfo);
        request.setAttribute(PaymentRequestParam.TXN_MODE.getValue(), TheiaConstant.BasicPayOption.UPI);
        request.setAttribute(PaymentRequestParam.MPIN.getValue(), "mpin");
        request.setAttribute(PaymentRequestParam.DEVICE_ID.getValue(), "deviceId");
        request.setAttribute(PaymentRequestParam.VIRTUAL_PAYMENT_ADDRESS.getValue(), "vpa");
        request.setAttribute(PaymentRequestParam.ADD_MONEY.getValue(),
                TheiaConstant.ExtraConstants.ADD_MONEY_FLAG_VALUE);
        request.setAttribute(PaymentRequestParam.TXN_MDE.getValue(), "SC");
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "savedCard");
        CardInfo cardInfo = new CardInfo();
        SavedCardInfo savedCardInfo = new SavedCardInfo();
        savedCardInfo.setCardNumber("vpa");
        cardInfo.setSavedCardMap(Collections.singletonMap("savedCard", savedCardInfo));
        when(sessionDataService.getCardInfoFromSession(any())).thenReturn(cardInfo);
        PayBillOptions payBillOptions = new PayBillOptions(new PayBillOptions.PayBillOptionsBuilder(123l, 23l,
                Collections.singletonMap(PayMethod.BALANCE, "balance")).setChannelInfo(
                Collections.singletonMap("key", "value")).setExtendInfo(Collections.singletonMap("key", "value")));
        CashierEnvInfo cashierEnvInfo = new CashierEnvInfo(new CashierEnvInfo.CashierEnvInfoBuilder("clientIp",
                TerminalType.APP));
        PaymentRequest paymentRequest = new PaymentRequest(new PaymentRequest.PaymentRequestBuilder(
                PaymentType.ONLY_WALLET, "transId", TransType.REQUEST_MONEY, "requestId", payBillOptions,
                cashierEnvInfo).setExtendInfo(Collections.singletonMap("key", "value")).setRiskExtendInfo(
                Collections.singletonMap("key", "value")));

        when(
                theiaCashierMapper.preparePaymentRequest(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any())).thenReturn(paymentRequest);
        when(sessionDataService.getMerchantInfoFromSession(any())).thenReturn(new MerchantInfo());
        doNothing().when(paymentRequestValidation).prepareCardRequest(any(), any(), any(), any(), any(), any());
        doNothing().when(theiaTransactionalRedisUtil).set(anyString(), anyObject(), anyLong());
        assertNotNull(paymentRequestHelper.prepareCashierRequest(request));
    }

    private TransactionConfig setUpRequest(HttpServletRequest request) throws PaytmValidationException {
        request.setAttribute("addMoney", "1");
        request.setAttribute("cacheCardToken", "cacheCard");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "1234567891011");
        request.setAttribute(PaymentRequestParam.TXN_MDE.getValue(), "");
        request.setAttribute(PaymentRequestParam.TXN_MODE.getValue(), PayMethod.CREDIT_CARD.getOldName());
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "1234567");
        TransactionInfo info = new TransactionInfo();
        info.setRequestType(TheiaConstant.RequestTypes.LINK_BASED_PAYMENT_INVOICE);
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setZeroRupeesSubscription(true);
        when(sessionDataService.getTxnInfoFromSession(request)).thenReturn(transactionInfo);
        CashierRequest.CashierRequestBuilder builder = new CashierRequest.CashierRequestBuilder("accquirementId", false);
        when(theiaTransactionalRedisUtil.get(any())).thenReturn(new CashierRequest(builder));
        request.setAttribute("cacheCardToken", "");
        // setParameters(request);
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setHybridAllowed(true);
        transactionConfig.setAddMoneyFlag(true);
        transactionConfig.setSubsTypes(SubsTypes.CC_ONLY);
        when(sessionDataService.getTxnConfigFromSession(request)).thenReturn(transactionConfig);
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setNumberOfRetries(1);
        when(sessionDataService.getMerchantInfoFromSession(request)).thenReturn(merchantInfo);
        WalletInfo walletInfo = new WalletInfo();
        walletInfo.setWalletBalance(90D);
        walletInfo.setWalletEnabled(true);
        when(sessionDataService.getWalletInfoFromSession(request)).thenReturn(walletInfo);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUser(new OAuthUserInfo());
        when(sessionDataService.getLoginInfoFromSession(request)).thenReturn(loginInfo);
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        entityPaymentOptionsTO.setAddUpiPushEnabled(true);
        when(sessionDataService.getEntityPaymentOptions(request)).thenReturn(entityPaymentOptionsTO);
        EnvInfoRequestBean envInfoRequestBean = new EnvInfoRequestBean();
        envInfoRequestBean.setTokenId("tokenId");
        envInfoRequestBean.setExtendInfo(Collections.singletonMap(TheiaConstant.ExtraConstants.DEVICE_ID, "deviceId"));
        when(sessionDataService.getEnvInfoRequestBean(any())).thenReturn(envInfoRequestBean);
        EnvInfoRequestBean envInfoRequestBean1 = (sessionDataService.getEnvInfoRequestBean(request));
        new MockUp<EnvInfoUtil>() {

            @mockit.Mock
            public EnvInfoRequestBean fetchEnvInfo(HttpServletRequest request) {
                return envInfoRequestBean;
            }
        };
        CardInfo cardInfo = new CardInfo();
        SavedCardInfo savedCardInfo = new SavedCardInfo();
        savedCardInfo.setFirstSixDigit(123456l);
        cardInfo.setSavedCardMap(Collections.singletonMap(
                request.getParameter(PaymentRequestParam.SAVED_CARD_ID.getValue()), savedCardInfo));
        when(sessionDataService.getCardInfoFromSession(any())).thenReturn(cardInfo);
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setClientId("clientId");
        when(sessionDataService.geExtendedInfoRequestBean(any())).thenReturn(extendedInfoRequestBean);
        doNothing().when(workFlowHelper).validateAndUpdateMerchantVelocity(anyString(), anyString(), anyObject(),
                anyBoolean());
        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("BALANCE");
        when(cardUtils.fetchBinDetails(any())).thenReturn(binDetail);
        new MockUp<PayMethod>() {

            @mockit.Mock
            public PayMethod valueOf(String cardType) {
                return PayMethod.BALANCE;
            }
        };
        when(cardUtils.fetchBinDetails("123456")).thenReturn(binDetail);
        when(subscriptionUtil.isBinBoundToSubscriptionFlow(any(), any())).thenReturn(true);
        SubscriptionResponse response = new SubscriptionResponse();
        response.setStatus(TransactionStatus.TXN_SUCCESS);
        when(subscriptionService.activateSubscription(any())).thenReturn(response);
        transactionConfig.setOnTheFlyKYCRequired(true);
        transactionInfo.setTxnId("1234");
        return transactionConfig;
    }

    @Test
    public void incrementRetryCount() {

    }

    @Test
    public void prepareKycRetryData() {
        HttpServletRequest request = new MockHttpServletRequest();
        when(sessionDataService.getTxnInfoFromSession(request)).thenReturn(new TransactionInfo());
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setOnTheFlyKYCRequired(true);
        when(sessionDataService.getTxnConfigFromSession(request)).thenReturn(transactionConfig);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUser(new OAuthUserInfo());
        doNothing().when(theiaTransactionalRedisUtil).set(any(), any());
        when(sessionDataService.getLoginInfoFromSession(request)).thenReturn(loginInfo);
        when(theiaTransactionalRedisUtil.get(any())).thenReturn(2);
        assertTrue(paymentRequestHelper.prepareKycRetryData(request));

    }
}