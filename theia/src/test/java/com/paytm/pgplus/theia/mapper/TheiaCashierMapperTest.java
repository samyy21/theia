package com.paytm.pgplus.theia.mapper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.DigitalCreditRequest;
import com.paytm.pgplus.cashier.models.UPIPushRequest;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.promotion.models.response.TemplateByMerchantIDResponse;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.requestidclient.IdManager;
import com.paytm.pgplus.requestidclient.IdStore;
import com.paytm.pgplus.requestidclient.enums.Groups;
import com.paytm.pgplus.requestidclient.models.SubscriberMetaInfo;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import com.rabbitmq.client.AMQP;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.TestException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.AssertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SAVED_CARD_ENABLE_ALT;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TheiaCashierMapperTest {

    @InjectMocks
    TheiaCashierMapper theiaCashierMapper;

    @Mock
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private CardUtils cardUtils;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    ITheiaSessionDataService sessionDataService;

    @Mock
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testExceptionsforCashierWorkFlow() throws PaytmValidationException {
        try {
            theiaCashierMapper.getCashierWorkflow(null, null, null);
        } catch (PaytmValidationException e) {

        }
        TransactionInfo txnInfo = new TransactionInfo();
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(new MockHttpServletRequest());
        try {
            txnInfo.setRequestType("NoRequest");
            theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, null);
        } catch (PaymentRequestValidationException e) {

        }
        try {
            txnInfo.setRequestType("RESELLER");
            theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, null);
        } catch (PaytmValidationException e) {

        }
        try {
            txnInfo.setRequestType("RESELLER");
            theiaPaymentRequest.setTxnMode(TheiaConstant.RequestTypes.ADD_MONEY);
            theiaPaymentRequest.setPaymentMode(null);
            theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, null);
        } catch (PaytmValidationException e) {

        }
        try {
            txnInfo.setRequestType("RESELLER");
            theiaPaymentRequest.setTxnMode(TheiaConstant.RequestTypes.ADD_MONEY);
            theiaPaymentRequest.setPaymentMode("P");
            theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, null);
        } catch (PaytmValidationException e) {

        }
        try {
            txnInfo.setRequestType("ADD_MONEY");
            theiaPaymentRequest.setTxnMode(" ");
            theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, null);
        } catch (PaytmValidationException e) {

        }

    }

    @Test
    public void getCashierWorkflowForNonAddMoney() throws PaytmValidationException {

        HttpServletRequest request = setUpHttpRequest();
        TransactionInfo txnInfo = new TransactionInfo();
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        txnInfo.setRequestType(TheiaConstant.RequestTypes.RESELLER);
        theiaPaymentRequest.setTxnMode("PPI");
        assertEquals(CashierWorkflow.WALLET,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("PAYTM_DIGITAL_CREDIT");
        assertEquals(CashierWorkflow.DIGITAL_CREDIT_PAYMENT,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("DC");
        entityPaymentOptionsTO.setAddDcEnabled(true);
        assertEquals(CashierWorkflow.ISOCARD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("CC");
        entityPaymentOptionsTO.setAddCcEnabled(true);
        assertEquals(CashierWorkflow.ISOCARD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("EMI");
        assertEquals(CashierWorkflow.ISOCARD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("ATM");
        entityPaymentOptionsTO.setAddAtmEnabled(true);
        assertEquals(CashierWorkflow.ATM,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("PPBL");
        when(sessionDataService.getSavingsAccountInfoFromSession(theiaPaymentRequest.getRequest(), false)).thenReturn(
                new SavingsAccountInfo());
        entityPaymentOptionsTO.setPaymentsBankEnabled(true);
        entityPaymentOptionsTO.setAddPaymentsBankEnabled(true);
        assertEquals(CashierWorkflow.NB,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("NB");
        entityPaymentOptionsTO.setAddNetBankingEnabled(true);
        assertEquals(CashierWorkflow.NB,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        entityPaymentOptionsTO.setAddNetBankingEnabled(false);
        when(sessionDataService.getSavingsAccountInfoFromSession(theiaPaymentRequest.getRequest(), false)).thenReturn(
                new SavingsAccountInfo());
        entityPaymentOptionsTO.setPaymentsBankEnabled(true);
        entityPaymentOptionsTO.setAddPaymentsBankEnabled(true);
        assertEquals(CashierWorkflow.NB,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("COD");
        entityPaymentOptionsTO.setCodEnabled(true);
        assertEquals(CashierWorkflow.COD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setTxnMode("UPI");
        entityPaymentOptionsTO.setAddUpiEnabled(true);
        assertEquals(CashierWorkflow.UPI,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));

        theiaPaymentRequest.setTxnMode("");
        expectedException.expect(PaytmValidationException.class);
        theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO);

    }

    @Test
    public void getCashierWorkflowForAddMoney() throws PaytmValidationException {
        HttpServletRequest request = setUpHttpRequest();
        TransactionInfo txnInfo = new TransactionInfo();
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        txnInfo.setRequestType(TheiaConstant.RequestTypes.ADD_MONEY);
        theiaPaymentRequest.setTxnMode("txnMode");
        theiaPaymentRequest.setPaymentMode("DC");
        entityPaymentOptionsTO.setDcEnabled(true);
        assertEquals(CashierWorkflow.ADD_MONEY_ISOCARD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("CC");
        entityPaymentOptionsTO.setCcEnabled(true);
        assertEquals(CashierWorkflow.ADD_MONEY_ISOCARD,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("NB");
        entityPaymentOptionsTO.setNetBankingEnabled(true);
        assertEquals(CashierWorkflow.ADD_MONEY_NB,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("IMPS");
        assertEquals(CashierWorkflow.ADD_MONEY_IMPS,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("ATM");
        assertEquals(CashierWorkflow.ADD_MONEY_ATM,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("UPI");
        entityPaymentOptionsTO.setUpiEnabled(true);
        assertEquals(CashierWorkflow.ADD_MONEY_UPI,
                theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO));
        theiaPaymentRequest.setPaymentMode("UPI");
        entityPaymentOptionsTO.setUpiEnabled(false);
        expectedException.expect(PaytmValidationException.class);
        theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest, entityPaymentOptionsTO);

    }

    private HttpServletRequest setUpHttpRequest() {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.ADD_MONEY.getValue(), "1");
        request.setAttribute(PaymentRequestParam.BANK_CODE.getValue(), "PPBL");
        return request;
    }

    @Test
    public void preparePaymentRequest() throws PaytmValidationException, CashierCheckedException,
            IllegalAccessException {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.DEVICE_ID.getValue(), "deviceId");
        request.setAttribute(PaymentRequestParam.STORE_CARD_FLAG.getValue(), SAVED_CARD_ENABLE_ALT);
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        PaymentInfo paymentInfo = new PaymentInfo();
        TransactionInfo txnInfo = new TransactionInfo();
        TransactionConfig txnConfig = new TransactionConfig();
        OAuthUserInfo userInfo = new OAuthUserInfo();
        Map<String, String> extendedInfo = new HashMap<>();
        MerchantInfo merchInfo = new MerchantInfo();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        EnvInfoRequestBean envInfo = new EnvInfoRequestBean();
        Map<String, String> riskExtendedInfo = new HashMap<>();
        DigitalCreditRequest digitalCreditRequest = new DigitalCreditRequest("1000", "extAccNo", "lenderId",
                "passCode", 1);
        UPIPushRequest upiPushRequest = new UPIPushRequest();
        BinDetail binDetail = new BinDetail();
        binDetail.setCardName("cardName");
        txnInfo.setTxnId("txnId");
        txnConfig.setTxnType("SEND_MONEY");
        paymentInfo.setServiceAmount(800L);
        paymentInfo.setTopupAndPay(true);
        userInfo.setPayerAccountNumber("payerAccNo");
        theiaPaymentRequest.setPaymentMode("EMI");
        theiaPaymentRequest.setVpa("vpa");
        BankInfo bankInfo = new BankInfo();
        EMIInfo emiInfo = new EMIInfo();
        emiInfo.setInstId("instId");
        emiInfo.setPlanId("planId");
        emiInfo.setAggregator(true);
        bankInfo.setEmiInfo(Collections.singletonList(emiInfo));
        entityPaymentOptions.setCompleteEMIInfoList(Collections.singletonList(bankInfo));
        theiaPaymentRequest.setBankCode("instId");
        theiaPaymentRequest.setEmiPlanID("planId");
        extendedInfo.put(BizConstant.ExtendedInfoKeys.MCC_CODE, "MCC_CODE");
        userInfo.setMobileNumber("mobileNo");
        userInfo.setEmailId("emailId");
        envInfo.setClientIp("clientIp");
        merchInfo.setMid("mid");
        when(merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid())).thenReturn(true);
        merchInfo.setMerchantName("merchant");
        theiaPaymentRequest.setTxnMode(TheiaConstant.BasicPayOption.UPI);
        upiPushRequest.setUpiPushTxn(true);
        upiPushRequest.setUpiPushExpressSupported(true);
        upiPushRequest.setSarvatraVpaDetails(new SarvatraVpaDetails(new PaytmBanksVpaDefaultDebitCredit()));
        upiPushRequest.setAppId("appId");
        when(merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid())).thenReturn(true);
        txnInfo.setRequestType(TheiaConstant.RequestTypes.DEFAULT);
        extendedInfo.put(TheiaConstant.ExtendedInfoPay.SAVED_CARD_ID, "savedCard");
        paymentInfo.setWalletBalance(800L);
        paymentInfo.setChargeFeeAmount(900L);
        envInfo.setTerminalType(ETerminalType.APP);
        txnInfo.setCustID("custId");
        txnInfo.setSubwalletAmount(new HashMap<>());
        txnInfo.setAddMoneyDestination("wallet");
        paymentInfo.setPaymentType(PaymentType.ADDNPAY);
        SubscriberMetaInfo subscriberMetaInfo = new SubscriberMetaInfo(Groups.BACKOFFICE, "workerId");
        IdManager.initialize(subscriberMetaInfo);
        theiaCashierMapper.preparePaymentRequest(theiaPaymentRequest, paymentInfo, txnInfo, txnConfig, userInfo,
                extendedInfo, merchInfo, entityPaymentOptions, envInfo, riskExtendedInfo, digitalCreditRequest,
                upiPushRequest, binDetail);
        entityPaymentOptions.setCompleteEMIInfoList(null);
        entityPaymentOptions.setHybridEMIInfoList(Collections.singletonList(bankInfo));
        when(merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid())).thenReturn(true);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid())).thenReturn(true);
        upiPushRequest.setUpiPushTxn(false);
        upiPushRequest.setUpiPushExpressSupported(false);
        when(upiInfoSessionUtil.getPaymentTimeoutinMinsForUpi(merchInfo.getMid())).thenReturn("timeout");
        theiaCashierMapper.preparePaymentRequest(theiaPaymentRequest, paymentInfo, txnInfo, txnConfig, userInfo,
                extendedInfo, merchInfo, entityPaymentOptions, envInfo, riskExtendedInfo, digitalCreditRequest,
                upiPushRequest, binDetail);
        theiaPaymentRequest.setTxnMode(PayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        digitalCreditRequest.setExternalAccountNo("externalAccNo");
        digitalCreditRequest.setLenderId("lenderId");
        theiaPaymentRequest.setPaymentMode(com.paytm.pgplus.facade.enums.PayMethod.UPI.getOldName());
        theiaPaymentRequest.setMpin("mpin");
        txnInfo.setRequestType(TheiaConstant.RequestTypes.SUBSCRIPTION);
        theiaCashierMapper.preparePaymentRequest(theiaPaymentRequest, paymentInfo, txnInfo, txnConfig, userInfo,
                extendedInfo, merchInfo, entityPaymentOptions, envInfo, riskExtendedInfo, digitalCreditRequest,
                upiPushRequest, binDetail);

    }

    @Test
    public void prepareCashierMerchant() throws CashierCheckedException {

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMid("mid");
        merchantInfo.setNumberOfRetries(1);
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setRetryCount(1);
        assertNotNull(theiaCashierMapper.prepareCashierMerchant(null, transactionConfig, merchantInfo));
    }

    @Test
    public void prepareCardRequestForSavedCard() throws PaytmValidationException, CashierCheckedException {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.ADD_MONEY.getValue(), "1");
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "savedCardId");
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        CardInfo cardInfo = new CardInfo();
        cardInfo.setAddAnPaySavedCardMap(Collections.singletonMap("savedCardId", new SavedCardInfo()));
        theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest);

    }

    @Test
    public void prepareCardRequest() throws PaytmValidationException, CashierCheckedException {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.EXPIRY_YEAR.getValue(), "2019");
        request.setAttribute(PaymentRequestParam.EXPIRY_MONTH.getValue(), "09");
        request.setAttribute(PaymentRequestParam.ADD_MONEY.getValue(), "1");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "1234567");
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        CardInfo cardInfo = new CardInfo();
        cardInfo.setAddAnPaySavedCardMap(Collections.singletonMap("savedCardId", new SavedCardInfo()));
        theiaPaymentRequest.setTxnMode(com.paytm.pgplus.facade.enums.PayMethod.EMI.getOldName());
        when(cardUtils.fetchCardSchemeUsingCustomLogic(any())).thenReturn("cardScheme");
        theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest);
        theiaPaymentRequest.setTxnMode(PayMethod.DEBIT_CARD.getMethod());
        when(cardUtils.fetchCardSchemeUsingCustomLogic(any())).thenReturn("cardScheme");
        theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest);
        theiaPaymentRequest.setTxnMode("txnMode");
        when(cardUtils.fetchCardSchemeUsingCustomLogic(any())).thenReturn("cardScheme");
        theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest);

    }

    @Test
    public void testPreapreImpsCardRequest() throws CashierCheckedException {

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "savedCardId");
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        theiaPaymentRequest.setOtp("otp");
        assertNotNull(theiaCashierMapper.preapreImpsCardRequest(theiaPaymentRequest));
        request = new MockHttpServletRequest();
        request.setAttribute(TheiaConstant.RequestParams.IMPS_MOBILE_NO, "9932389892");
        theiaPaymentRequest = new TheiaPaymentRequest(request);
        theiaPaymentRequest.setOtp("otp");
        theiaPaymentRequest.setMmid("mmid");
        assertNotNull(theiaCashierMapper.preapreImpsCardRequest(theiaPaymentRequest));

    }

    @Test
    public void prepareValidationRequest() throws PaytmValidationException {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.ADD_MONEY.getValue(), "1");
        request.setAttribute(PaymentRequestParam.CVV.getValue(), "123");
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "12345678911");
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMid("mid");
        when(merchantExtendInfoUtils.getEntityIDCorrespodingToMerchant("mid")).thenReturn("entityId", "entityId",
                "entityId", "entityId", "entityId", "entityId");
        theiaPaymentRequest.setBankCode("-1");
        try {
            theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest, merchantInfo, null, null);
            fail();
        } catch (PaytmValidationException e) {

        }
        theiaPaymentRequest.setBankCode("bankCode");
        when(merchantBankInfoDataService.getBankInfo(theiaPaymentRequest.getBankCode())).thenReturn(null,
                new BankInfoData(), new BankInfoData());
        try {
            theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest, merchantInfo, null, null);
            fail();
        } catch (PaytmValidationException e) {

        }
        theiaPaymentRequest.setVpa("vpa@vpa");
        assertNotNull(theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest, merchantInfo,
                CashierWorkflow.UPI, null));
        theiaPaymentRequest.setBankCode(null);
        BinDetail binDetail = new BinDetail();
        binDetail.setBankCode("bankCode");
        assertNotNull(theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest, merchantInfo,
                CashierWorkflow.ADD_MONEY_UPI, binDetail));
        assertNotNull(theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest, merchantInfo,
                CashierWorkflow.ADD_MONEY_UPI, null));
    }

    @Test
    public void testPrepareUPIRequest() throws CashierCheckedException {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "savedCard");
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        theiaPaymentRequest.setVpa("vpa@vpa");
        assertNotNull(theiaCashierMapper.prepareUPIRequest(theiaPaymentRequest));
        theiaPaymentRequest = new TheiaPaymentRequest(new MockHttpServletRequest());
        theiaPaymentRequest.setVpa("vpa@vpa");
        assertNotNull(theiaCashierMapper.prepareUPIRequest(theiaPaymentRequest));

    }
}