package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.enums.DirectChannelBank;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.integration.enums.SupportRegion;
import com.paytm.pgplus.pgproxycommon.enums.CardType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.mapper.TheiaCashierMapper;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.TheiaCashierUtil;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.MagicConstant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class PaymentRequestValidationTest {

    @InjectMocks
    PaymentRequestValidation paymentRequestValidation;

    @Mock
    EmiBinValidationUtil emiBinValidationUtil;

    @Mock
    TheiaCashierMapper theiaCashierMapper;

    @Mock
    TheiaCashierUtil theiaCashierUtil;

    @Mock
    private CardUtils cardUtils;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void prepareCardRequestForISOCARD() throws PaytmValidationException, CashierCheckedException {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "123456");
        request.setAttribute(PaymentRequestParam.EXPIRY_MONTH.getValue(), "09");
        request.setAttribute(PaymentRequestParam.EXPIRY_YEAR.getValue(), "2024");
        request.setAttribute(PaymentRequestParam.ICICI_IDEBIT.getValue(), "Y");
        request.setAttribute(PaymentRequestParam.TXN_MODE.getValue(),
                com.paytm.pgplus.common.enums.PayMethod.EMI.getOldName());
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        entityPaymentOptions.setDcEnabled(true);
        entityPaymentOptions.setCcEnabled(true);
        entityPaymentOptions.setDirectServiceInsts(Collections.singleton(DirectChannelBank.ICICIIDEBIT + "@"
                + CardType.CREDIT_CARD.getValue()));
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName("visa");
        bankInfo.setSupportCountries(Collections.singletonList(SupportRegion.INTL.name()));
        entityPaymentOptions.setCompleteDcList(Collections.singletonList(bankInfo));
        CashierWorkflow cashierWorkflow = CashierWorkflow.ISOCARD;
        CashierRequest.CashierRequestBuilder cashierRequestBuilder = new CashierRequest.CashierRequestBuilder(
                "acquireId", true);
        CardInfo cardInfo = new CardInfo();
        PayBillOptions payBillOptions = new PayBillOptions(new PayBillOptions.PayBillOptionsBuilder(123l, 23l,
                Collections.singletonMap(PayMethod.BALANCE, "balance")).setChannelInfo(new HashMap<>()).setExtendInfo(
                Collections.singletonMap("key", "value")));
        CashierEnvInfo cashierEnvInfo = new CashierEnvInfo(new CashierEnvInfo.CashierEnvInfoBuilder("clientIp",
                TerminalType.APP));
        PaymentRequest paymentRequest = new PaymentRequest(new PaymentRequest.PaymentRequestBuilder(
                PaymentType.ONLY_WALLET, "transId", TransType.REQUEST_MONEY, "requestId", payBillOptions,
                cashierEnvInfo).setExtendInfo(Collections.singletonMap("key", "value")).setRiskExtendInfo(
                Collections.singletonMap("key", "value")));
        BankCardRequest bankCardRequest = new BankCardRequest("123456", "542", "2024", "09", "debit", "scheme");
        when(theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest)).thenReturn(
                new CardRequest("123456", bankCardRequest));
        BinDetail binDetail = new BinDetail();
        binDetail.setCardName("visa");
        binDetail.setCardType(CardType.CREDIT_CARD.getValue());
        when(cardUtils.fetchBinDetails(theiaPaymentRequest.getCardNo().substring(0, 6))).thenReturn(binDetail);
        doNothing().when(cardUtils).validateExpiryDate(theiaPaymentRequest.getExpiryMonth(),
                theiaPaymentRequest.getExpiryYear(), binDetail.getCardName(), StringUtils.EMPTY);
        doNothing().when(cardUtils).validateCVV(theiaPaymentRequest.getCvv(), binDetail.getCardName(),
                StringUtils.EMPTY);
        when(emiBinValidationUtil.isValidEmiCardDetailsEntered(theiaPaymentRequest, entityPaymentOptions, binDetail))
                .thenReturn(true);
        binDetail.setIsIndian(false);
        binDetail.setBankCode("ICICI");
        paymentRequestValidation.prepareCardRequest(theiaPaymentRequest, entityPaymentOptions, cashierWorkflow,
                cashierRequestBuilder, cardInfo, paymentRequest);

    }

    @Test
    public void prepareCardRequestForIMPSandUPI() throws PaytmValidationException, CashierCheckedException {

        PayBillOptions payBillOptions = new PayBillOptions(new PayBillOptions.PayBillOptionsBuilder(123l, 23l,
                Collections.singletonMap(PayMethod.BALANCE, "balance")).setChannelInfo(new HashMap<>()).setExtendInfo(
                Collections.singletonMap("key", "value")));
        CashierEnvInfo cashierEnvInfo = new CashierEnvInfo(new CashierEnvInfo.CashierEnvInfoBuilder("clientIp",
                TerminalType.APP));
        PaymentRequest paymentRequest = new PaymentRequest(new PaymentRequest.PaymentRequestBuilder(
                PaymentType.ONLY_WALLET, "transId", TransType.REQUEST_MONEY, "requestId", payBillOptions,
                cashierEnvInfo).setExtendInfo(Collections.singletonMap("key", "value")).setRiskExtendInfo(
                Collections.singletonMap("key", "value")));
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(new MockHttpServletRequest());
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        CashierRequest.CashierRequestBuilder cashierRequestBuilder = new CashierRequest.CashierRequestBuilder(
                "acquireId", true);
        doNothing().when(cardUtils).validateIMPSCardRequest(any(), any(), any(), any());
        when(theiaCashierMapper.preapreImpsCardRequest(theiaPaymentRequest)).thenReturn(
                new CardRequest("request", new BankCardRequest("123456", "542", "2024", "09", "debit", "scheme")));
        paymentRequestValidation.prepareCardRequest(theiaPaymentRequest, entityPaymentOptions, CashierWorkflow.IMPS,
                cashierRequestBuilder, new CardInfo(), paymentRequest);
        when(theiaCashierMapper.prepareUPIRequest(theiaPaymentRequest)).thenReturn(
                new CardRequest("request", new BankCardRequest("123456", "542", "2024", "09", "debit", "scheme")));
        paymentRequestValidation.prepareCardRequest(theiaPaymentRequest, entityPaymentOptions, CashierWorkflow.UPI,
                cashierRequestBuilder, new CardInfo(), paymentRequest);

    }

    @Test
    public void prepareCardRequestForISOCARDWorkflowWithsavedcard() throws PaytmValidationException,
            CashierCheckedException {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PaymentRequestParam.SAVED_CARD_ID.getValue(), "12234");
        request.setAttribute(PaymentRequestParam.CARD_NUMBER.getValue(), "123456");
        request.setAttribute(PaymentRequestParam.EXPIRY_MONTH.getValue(), "09");
        request.setAttribute(PaymentRequestParam.EXPIRY_YEAR.getValue(), "2024");
        request.setAttribute(PaymentRequestParam.ICICI_IDEBIT.getValue(), "N");
        request.setAttribute(PaymentRequestParam.TXN_MODE.getValue(),
                com.paytm.pgplus.common.enums.PayMethod.EMI.getOldName());
        TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        entityPaymentOptions.setDcEnabled(true);
        entityPaymentOptions.setCcEnabled(true);
        entityPaymentOptions.setDirectServiceInsts(Collections.singleton(DirectChannelBank.ICICIIDEBIT + "@"
                + CardType.CREDIT_CARD.getValue()));
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName("visa");
        bankInfo.setSupportCountries(Collections.singletonList(SupportRegion.INTL.name()));
        entityPaymentOptions.setCompleteDcList(Collections.singletonList(bankInfo));
        CashierWorkflow cashierWorkflow = CashierWorkflow.ISOCARD;
        CashierRequest.CashierRequestBuilder cashierRequestBuilder = new CashierRequest.CashierRequestBuilder(
                "acquireId", true);
        PayBillOptions payBillOptions = new PayBillOptions(new PayBillOptions.PayBillOptionsBuilder(123l, 23l,
                Collections.singletonMap(PayMethod.BALANCE, "balance")).setChannelInfo(new HashMap<>()).setExtendInfo(
                Collections.singletonMap("key", "value")));
        CashierEnvInfo cashierEnvInfo = new CashierEnvInfo(new CashierEnvInfo.CashierEnvInfoBuilder("clientIp",
                TerminalType.APP));
        PaymentRequest paymentRequest = new PaymentRequest(new PaymentRequest.PaymentRequestBuilder(
                PaymentType.ONLY_WALLET, "transId", TransType.REQUEST_MONEY, "requestId", payBillOptions,
                cashierEnvInfo).setExtendInfo(Collections.singletonMap("key", "value")).setRiskExtendInfo(
                Collections.singletonMap("key", "value")));
        CardRequest cardRequest = new CardRequest(new SavedCardRequest("scid", "422", "credit", "scheme"));
        CardInfo cardInfo = new CardInfo();
        cardInfo.setSavedCardMap(Collections.singletonMap(cardRequest.getSavedCardRequest().getSavedCardId(),
                new SavedCardInfo()));
        when(theiaCashierMapper.prepareCardRequest(cardInfo, theiaPaymentRequest)).thenReturn(cardRequest);
        BinDetail binDetail = new BinDetail();
        binDetail.setCardName("visa");
        binDetail.setCardType(CardType.CREDIT_CARD.getValue());
        doNothing().when(cardUtils).validateCVV(any(), any(), any());
        when(cardUtils.fetchBinDetails(any())).thenReturn(binDetail);
        when(cardUtils.fetchBinDetails(theiaPaymentRequest.getCardNo().substring(0, 6))).thenReturn(binDetail);
        doNothing().when(cardUtils).validateExpiryDate(theiaPaymentRequest.getExpiryMonth(),
                theiaPaymentRequest.getExpiryYear(), binDetail.getCardName(), StringUtils.EMPTY);
        doNothing().when(cardUtils).validateCVV(theiaPaymentRequest.getCvv(), binDetail.getCardName(),
                StringUtils.EMPTY);
        when(emiBinValidationUtil.isValidEmiCardDetailsEntered(theiaPaymentRequest, entityPaymentOptions, binDetail))
                .thenReturn(true);
        binDetail.setIsIndian(false);
        binDetail.setBankCode("ICICI");
        paymentRequestValidation.prepareCardRequest(theiaPaymentRequest, entityPaymentOptions, cashierWorkflow,
                cashierRequestBuilder, cardInfo, paymentRequest);

    }

}