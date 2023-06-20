package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.payview.*;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.services.IOfflinePaymentService;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.test.testflow.AbstractPaymentServiceTest;
import com.paytm.pgplus.theia.utils.PrepaidCardValidationUtil;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class CashierServiceHelperTest {

    @InjectMocks
    private CashierServiceHelper cashierServiceHelper;
    @Mock
    private SuccessRateUtils successRateUtils;
    @Mock
    private IOfflinePaymentService offlinePaymentService;
    @Mock
    private IPgpFf4jClient iPgpFf4jClient;
    @Mock
    private ICommonFacade commonFacade;
    @Mock
    private ITheiaSessionDataService theiaSessionDataService;
    @Mock
    protected PrepaidCardValidationUtil prepaidCardValidationUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(CashierServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFilterUpiPayOptionsWhenUPI_PUSH_EXPRESS() {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod());
        payMethods.get(0).setPayMethod("UPI");
        UPI upi = new UPI();
        upi.setPayMethod("UPI");
        upi.setPayChannelOption("UPI_PUSH_EXPRESS");
        upi.setIsDisabled(new StatusInfo("false", "xyz"));
        List<PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(upi);
        payMethods.get(0).setPayChannelOptions(payChannelBase);
        cashierServiceHelper.filterUpiPayOptions(payMethods);
        Assert.assertTrue(("UPIPUSH".equals(upi.getInstId()) && "Unified Payment Interface - PUSH".equals(upi
                .getInstName())));
    }

    @Test
    public void testFilterUpiPayOptionsInNativeWhenUPI_PUSH_EXPRESS() {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod());
        payMethods.get(0).setPayMethod("UPI");
        com.paytm.pgplus.theia.nativ.model.payview.response.UPI upi = new com.paytm.pgplus.theia.nativ.model.payview.response.UPI();
        upi.setPayMethod("UPI");
        upi.setPayChannelOption("UPI_PUSH_EXPRESS");
        upi.setIsDisabled(new com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo("false", "xyz"));
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(upi);
        payMethods.get(0).setPayChannelOptions(payChannelBase);
        cashierServiceHelper.filterUpiPayOptionsInNative(payMethods);
        Assert.assertTrue(("UPIPUSH".equals(upi.getInstId()) && "Unified Payment Interface - PUSH".equals(upi
                .getInstName())));
    }

    @Test
    public void testFilterUpiPayOptionsInNativeWhenUPIAndUPI_PUSHISFALSE() {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod());
        payMethods.get(0).setPayMethod("UPI");
        com.paytm.pgplus.theia.nativ.model.payview.response.UPI upi = new com.paytm.pgplus.theia.nativ.model.payview.response.UPI();
        upi.setPayMethod("UPI");
        upi.setPayChannelOption("UPI_PUSH_EXPRESS");
        upi.setIsDisabled(new com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo("true", "xyz"));
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(upi);
        payMethods.get(0).setPayChannelOptions(payChannelBase);
        cashierServiceHelper.filterUpiPayOptionsInNative(payMethods);
        Assert.assertTrue(("UPIPUSH".equals(upi.getInstId()) && "Unified Payment Interface - PUSH".equals(upi
                .getInstName())));
    }

    @Test
    public void testCashierInfoRequestToPaymentRequestBean() {
        CashierInfoRequest cashierInfoRequest = new CashierInfoRequest(new RequestHeader(new BaseHeader("M", "M", "M",
                "M")), new CashierInfoRequestBody());
        cashierInfoRequest.getBody().setChannelId(EChannelId.APP);
        new MockUp<OfflinePaymentUtils>() {

            @mockit.Mock
            public HttpServletRequest gethttpServletRequest() {
                return new MockHttpServletRequest();
            }
        };
        assertNotNull(cashierServiceHelper.cashierInfoRequestToPaymentRequestBean(cashierInfoRequest));
    }

    private void getPayMethodViewsBizs(List<PayMethodViewsBiz> payMethodViewsBizs, String payMethod) {
        PayMethodViewsBiz payMethodViewsBiz = new PayMethodViewsBiz();
        payMethodViewsBiz.setPayMethod(payMethod);
        List<PayChannelOptionViewBiz> payChannelOptionViewBizs = new ArrayList<>();
        PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();
        payChannelOptionViewBiz.setPayOption(payMethod.concat("_PPBL"));
        payChannelOptionViewBiz.setInstId("ABC");
        payChannelOptionViewBiz.setPrepaidCardChannel(true);
        if (payMethod.equals("PAYTM_DIGITAL_CREDIT")) {
            List<ExternalAccountInfoBiz> externalAccountInfoBizs = new ArrayList<>();
            externalAccountInfoBizs.add(new ExternalAccountInfoBiz());
            externalAccountInfoBizs.get(0).setExternalAccountNo("12345");
            externalAccountInfoBizs.get(0).setAccountBalance("5000");
            externalAccountInfoBizs.get(0).setExtendInfo("xyz");
            payChannelOptionViewBiz.setExternalAccountInfos(externalAccountInfoBizs);
        }
        if (payMethod.equals("DEBIT_CARD")) {
            List<String> supportCountries = new ArrayList<>();
            supportCountries.add("AI");
            payChannelOptionViewBiz.setSupportCountries(supportCountries);
        }
        List<String> DirectServiceInsts = new ArrayList<>();
        DirectServiceInsts.add("1234");
        payChannelOptionViewBiz.setDirectServiceInsts(DirectServiceInsts);
        List<String> supportAtmPins = new ArrayList<>();
        supportAtmPins.add("1234");
        payChannelOptionViewBiz.setSupportAtmPins(supportAtmPins);
        payMethodViewsBiz.setPayChannelOptionViews(payChannelOptionViewBizs);
        payChannelOptionViewBizs.add(payChannelOptionViewBiz);
        payMethodViewsBizs.add(payMethodViewsBiz);
    }

    private void getCardBeanBiz(List<CardBeanBiz> cardBeanBizs, String CardType) {
        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        cardBeanBiz.setCardId(123456L);
        cardBeanBiz.setCardType(CardType);
        cardBeanBiz.setCardScheme("PPBL");
        cardBeanBiz.setInstId("ABC");
        cardBeanBizs.add(cardBeanBiz);
    }

    @Test
    public void testProcessPaymentRequestAndMapResponse() {
        when(iPgpFf4jClient.checkWithdefault(anyString(), anyMap(), anyBoolean())).thenReturn(true);
        when(successRateUtils.getSuccessRateCacheModel()).thenReturn(new SuccessRateCacheModel());
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setPaymentModeOnly("UPI");
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        List<PayMethodViewsBiz> payMethodViewsBizs = new ArrayList<>();
        getPayMethodViewsBizs(payMethodViewsBizs, "CREDIT_CARD");
        getPayMethodViewsBizs(payMethodViewsBizs, "DEBIT_CARD");
        getPayMethodViewsBizs(payMethodViewsBizs, "NET_BANKING");
        getPayMethodViewsBizs(payMethodViewsBizs, "UPI");
        getPayMethodViewsBizs(payMethodViewsBizs, "PAYTM_DIGITAL_CREDIT");
        getPayMethodViewsBizs(payMethodViewsBizs, "BALANCE");
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = new LitePayviewConsultResponseBizBean();
        litePayviewConsultResponseBizBean.setPayMethodViews(payMethodViewsBizs);
        workFlowResponseBean.setMerchnatLiteViewResponse(litePayviewConsultResponseBizBean);
        workFlowResponseBean.setAddAndPayLiteViewResponse(new LitePayviewConsultResponseBizBean());
        workFlowResponseBean.setAllowedPayMode(EPayMode.ADDANDPAY);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        List<CardBeanBiz> cardBeanBizs = new ArrayList<>();
        getCardBeanBiz(cardBeanBizs, "CREDIT_CARD");
        getCardBeanBiz(cardBeanBizs, "DEBIT_CARD");
        userDetailsBiz.setMerchantViewSavedCardsList(cardBeanBizs);
        workFlowResponseBean.setUserDetails(userDetailsBiz);
        workFlowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
        when(offlinePaymentService.processPaymentRequest(paymentRequestBean)).thenReturn(workFlowResponseBean);
        CashierInfoRequest cashierInfoRequest = new CashierInfoRequest(new RequestHeader(new BaseHeader("M", "M", "M",
                "M")), new CashierInfoRequestBody());
        cashierInfoRequest.getBody().setOrderId("XYZ");
        cashierInfoRequest.getBody().setChannelId(EChannelId.APP);
        assertNotNull(cashierServiceHelper.processPaymentRequestAndMapResponse(paymentRequestBean, cashierInfoRequest));

    }

    @Test
    public void testRemoveDigitalCreditIfBalanceInSufficient() {
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        PayMethodViews payMethodViews = new PayMethodViews();

        PayMethod payMethod = new PayMethod();
        payMethod.setPayMethod("PAYTM_DIGITAL_CREDIT");
        DigitalCredit digitalCredit = new DigitalCredit(new BalanceInfo("123456", new Money(EnumCurrency.INR, "2000"),
                true));
        List<PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(digitalCredit);
        payMethod.setPayChannelOptions(payChannelBase);
        List<PayMethod> merchantPayMethods = new ArrayList<>();
        merchantPayMethods.add(payMethod);
        payMethodViews.setMerchantPayMethods(merchantPayMethods);
        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);

        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setTxnAmount("2000");

        cashierServiceHelper.removeDigitalCreditIfBalanceInSufficient(cashierInfoResponse, paymentRequestBean);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods().size());
    }

    @Test
    public void testFilterDisabledPayMethods() {
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        PayMethodViews payMethodViews = new PayMethodViews();
        PayMethod payMethod = new PayMethod();
        List<PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(new BalanceChannel());
        payChannelBase.get(0).setIsDisabled(new StatusInfo("TRUE", "xyz"));
        List<PayMethod> merchantPayMethods = new ArrayList<>();
        merchantPayMethods.add(payMethod);
        payMethodViews.setMerchantPayMethods(merchantPayMethods);
        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);
        cashierServiceHelper.filterDisabledPayMethods(cashierInfoResponse);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods().size());
    }

    @Test
    public void testFilterDisabledPayMethodsIfgetPayChannelOptionisEmpty() {
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        PayMethodViews payMethodViews = new PayMethodViews();
        PayMethod payMethod = new PayMethod();
        payMethod.setIsDisabled(new StatusInfo("TRUE", "xyz"));
        List<PayChannelBase> payChannelBase = new ArrayList<>();
        payChannelBase.add(new BalanceChannel());
        payMethod.setPayChannelOptions(payChannelBase);
        List<PayMethod> merchantPayMethods = new ArrayList<>();
        merchantPayMethods.add(payMethod);
        payMethodViews.setMerchantPayMethods(merchantPayMethods);
        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);
        cashierServiceHelper.filterDisabledPayMethods(cashierInfoResponse);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods().size());
    }

    @Test
    public void testFilterDisabledSavedInstrumentsWhenSavedCards() {
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        List<SavedCard> savedCards = getSavedCards();
        savedCards.get(0).setIsDisabled(new StatusInfo("TRUE", "xyz"));
        SavedInstruments savedInstruments = new SavedInstruments();
        savedInstruments.setSavedCards(savedCards);
        PayMethodViews payMethodViews = new PayMethodViews();
        payMethodViews.setMerchantSavedInstruments(savedInstruments);
        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);
        cashierServiceHelper.filterDisabledSavedInstruments(cashierInfoResponse);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments().getSavedCards()
                .size());
    }

    @Test
    public void testFilterDisabledSavedInstrumentsWhenSavedVPAs() {
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        List<SavedVPA> savedVPAS = new ArrayList<>();
        SavedVPA savedVPA = new SavedVPA();
        savedVPA.setIsDisabled(new StatusInfo("TRUE", "xyz"));
        savedVPAS.add(savedVPA);
        List<SavedCard> savedCards = getSavedCards();
        SavedInstruments savedInstruments = new SavedInstruments(savedCards, savedVPAS);
        PayMethodViews payMethodViews = new PayMethodViews();
        payMethodViews.setMerchantSavedInstruments(savedInstruments);
        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);
        cashierServiceHelper.filterDisabledSavedInstruments(cashierInfoResponse);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments().getSavedVPAs()
                .size());
    }

    @Test
    public void testTrimResponse() {

        CashierInfoRequest cashierInfoRequest = new CashierInfoRequest(new RequestHeader(),
                new CashierInfoRequestBody());
        List<InstrumentType> instrumentTypes = new ArrayList<>();
        instrumentTypes.add(InstrumentType.NB_TOP5);
        cashierInfoRequest.getBody().setInstrumentTypes(instrumentTypes);
        cashierInfoRequest.getBody().setSavedInstrumentsTypes(instrumentTypes);

        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse(new ResponseHeader(),
                new CashierInfoResponseBody());
        PayMethodViews payMethodViews = new PayMethodViews();
        PayMethod payMethod = new PayMethod();
        payMethod.setPayMethod("DEBIT_CARD");
        List<PayMethod> merchantPayMethods = new ArrayList<>();
        merchantPayMethods.add(payMethod);

        List<SavedCard> savedCards = getSavedCards();
        List<SavedVPA> savedVPAS = getSavedVPAs();
        SavedInstruments savedInstruments = new SavedInstruments(savedCards, savedVPAS);

        payMethodViews.setMerchantPayMethods(merchantPayMethods);
        payMethodViews.setAddMoneyPayMethods(merchantPayMethods);
        payMethodViews.setAddMoneySavedInstruments(savedInstruments);
        payMethodViews.setMerchantSavedInstruments(savedInstruments);

        cashierInfoResponse.getBody().setPayMethodViews(payMethodViews);

        cashierServiceHelper.trimResponse(cashierInfoResponse, cashierInfoRequest);
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments().getSavedCards()
                .size());
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments().getSavedCards()
                .size());
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments().getSavedVPAs()
                .size());
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments().getSavedVPAs()
                .size());
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods().size());
        assertEquals(0, cashierInfoResponse.getBody().getPayMethodViews().getAddMoneyPayMethods().size());

    }

    private List<SavedCard> getSavedCards() {
        List<SavedCard> savedCards = new ArrayList<>();
        SavedCard savedCard = new SavedCard();
        savedCards.add(savedCard);
        return savedCards;
    }

    private List<SavedVPA> getSavedVPAs() {
        List<SavedVPA> savedVPAS = new ArrayList<>();
        SavedVPA savedVPA = new SavedVPA();
        savedVPAS.add(savedVPA);
        return savedVPAS;
    }

}
