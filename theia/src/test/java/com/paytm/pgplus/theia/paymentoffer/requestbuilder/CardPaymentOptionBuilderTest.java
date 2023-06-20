package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CardPaymentOptionBuilderTest {

    @InjectMocks
    private CardPaymentOptionBuilder cardPaymentOptionBuilder = new CardPaymentOptionBuilder();

    @Mock
    private IBinFetchService binFetchService;

    @Mock
    ISavedCardService savedCardService;

    @Mock
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Mock
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Mock
    FF4JUtil ff4JUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(CardPaymentOptionBuilder.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBuild() throws MappingServiceClientException {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setSavedCardId("12345678912345678");
        promoPaymentOption.setCardNo("1234567891234567891234");
        promoPaymentOption.setPayMethod(PayMethod.BALANCE);
        promoPaymentOption.setTransactionAmount("1000");
        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("BALANCE");
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail);
        cardPaymentOptionBuilder.build(promoPaymentOption, "mid");

        PromoPaymentOption promoPaymentOption2 = new PromoPaymentOption();
        promoPaymentOption2.setSavedCardId("123456");
        promoPaymentOption2.setPayMethod(PayMethod.EMI_DC);
        promoPaymentOption2.setTransactionAmount("1000");
        BinDetail binDetail2 = new BinDetail();
        binDetail2.setCardType("DEBIT_CARD");
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setCardNumber("123456789");
        SavedCardResponse<SavedCardVO> savedCardResponse = new SavedCardResponse<>();
        savedCardResponse.setStatus(true);
        savedCardResponse.setResponseData(savedCardVO);
        when(savedCardService.getSavedCardByCardId(any())).thenReturn(savedCardResponse);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail2);
        cardPaymentOptionBuilder.build(promoPaymentOption2, "mid");

        PromoPaymentOption promoPaymentOption3 = new PromoPaymentOption();
        promoPaymentOption3.setSavedCardId("123456");
        promoPaymentOption3.setPayMethod(PayMethod.EMI_DC);
        promoPaymentOption3.setTransactionAmount("1000");
        BinDetail binDetail3 = new BinDetail();
        binDetail3.setCardType("DEBIT_CARD");
        SavedCardResponse<SavedCardVO> savedCardResponse1 = new SavedCardResponse<>();
        savedCardResponse1.setStatus(false);
        exceptionRule.expect(BaseException.class);
        when(savedCardService.getSavedCardByCardId(any())).thenReturn(savedCardResponse1);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail3);
        cardPaymentOptionBuilder.build(promoPaymentOption3, "mid");

    }

    @Test
    public void testBuild0() throws MappingServiceClientException {
        PromoPaymentOption promoPaymentOption1 = new PromoPaymentOption();
        promoPaymentOption1.setSavedCardId("123456");
        promoPaymentOption1.setCardNo("12345");
        promoPaymentOption1.setPayMethod(PayMethod.EMI_DC);
        promoPaymentOption1.setTransactionAmount("1000");
        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("BALANCE");
        exceptionRule.expect(RequestValidationException.class);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail);
        cardPaymentOptionBuilder.build(promoPaymentOption1, "mid");
    }

    @Test
    public void testBuildWhensetSavedCardNull() {
        PromoPaymentOption promoPaymentOption2 = new PromoPaymentOption();
        promoPaymentOption2.setSavedCardId(null);
        promoPaymentOption2.setCardNo(null);
        promoPaymentOption2.setPayMethod(PayMethod.BALANCE);
        promoPaymentOption2.setTransactionAmount("1000");
        exceptionRule.expect(RequestValidationException.class);
        cardPaymentOptionBuilder.build(promoPaymentOption2, "mid");
    }

    @Test
    public void testBuildWhenBinDetailsIsNull() throws MappingServiceClientException {
        PromoPaymentOption promoPaymentOption3 = new PromoPaymentOption();
        promoPaymentOption3.setCardNo("cardNo");
        promoPaymentOption3.setSavedCardId("savedCardId");
        promoPaymentOption3.setPayMethod(PayMethod.BALANCE);
        promoPaymentOption3.setTransactionAmount("1000");
        exceptionRule.expect(BaseException.class);
        when(binFetchService.getCardBinDetail(any())).thenReturn(null);
        cardPaymentOptionBuilder.build(promoPaymentOption3, "mid");
    }

    @Test
    public void testBuild1() throws MappingServiceClientException {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setPayMethod(PayMethod.COD);
        promoPaymentOption.setTransactionAmount("200");
        promoPaymentOption.setBankCode("bankCode");
        promoPaymentOption.setSavedCardId("123");
        promoPaymentOption.setCardNo("123");
        BinDetail binDetail = new BinDetail();
        binDetail.setBankCode("BankCode");
        binDetail.setCardType("COD");
        exceptionRule.expect(RequestValidationException.class);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail);
        cardPaymentOptionBuilder.build(promoPaymentOption, "mid");
    }

    @Test
    public void testBuild2() throws MappingServiceClientException {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setPayMethod(PayMethod.COD);
        promoPaymentOption.setTransactionAmount("200");
        promoPaymentOption.setBankCode("bankCode");
        promoPaymentOption.setSavedCardId("12345678912345678");
        promoPaymentOption.setCardNo("123");
        promoPaymentOption.setEightDigitBinHash("12345678");
        BinDetail binDetail = new BinDetail();
        binDetail.setBankCode("bankCode");
        binDetail.setCardType("COD");
        binDetail.setCardName("cardName");
        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(true);
        when(paymentOffersServiceHelperV2.isBin8OfferAvailableOnMerchant(any(), any(), new HashMap<>())).thenReturn(
                true);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail);
        cardPaymentOptionBuilder.build(promoPaymentOption, "mid");

        PromoPaymentOption promoPaymentOption2 = new PromoPaymentOption();
        promoPaymentOption2.setPayMethod(PayMethod.COD);
        promoPaymentOption2.setTransactionAmount("200");
        promoPaymentOption2.setBankCode("bankCode");
        promoPaymentOption2.setSavedCardId("1234");
        promoPaymentOption2.setCardNo("123");
        BinDetail binDetail2 = new BinDetail();
        binDetail2.setBankCode("bankCode");
        binDetail2.setCardType("COD");
        binDetail2.setCardName("cardName");
        exceptionRule.expect(BaseException.class);
        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(true);
        when(paymentOffersServiceHelperV2.isBin8OfferAvailableOnMerchant(any(), any(), new HashMap<>())).thenReturn(
                true);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail2);
        cardPaymentOptionBuilder.build(promoPaymentOption2, "mid");

        PromoPaymentOption promoPaymentOption1 = new PromoPaymentOption();
        promoPaymentOption1.setPayMethod(PayMethod.COD);
        promoPaymentOption1.setTransactionAmount("200");
        promoPaymentOption1.setBankCode("bankCode");
        promoPaymentOption1.setSavedCardId("12345678912345678");
        promoPaymentOption1.setCardNo("123");
        BinDetail binDetail1 = new BinDetail();
        binDetail1.setBankCode("bankCode");
        binDetail1.setCardType("COD");
        binDetail1.setCardName("cardName");
        exceptionRule.expect(BaseException.class);
        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(true);
        when(paymentOffersServiceHelperV2.isBin8OfferAvailableOnMerchant(any(), any(), new HashMap<>())).thenReturn(
                true);
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail1);
        cardPaymentOptionBuilder.build(promoPaymentOption1, "mid");

    }

    @Test
    public void testBuildPromoPaymentOptions() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setCardNo("cardNo");
        workFlowRequestBean.setSavedCardID("savedCardId");
        cardPaymentOptionBuilder.buildPromoPaymentOptions(workFlowRequestBean, "txnAmount", "paymentMethod", "PPBL");
    }

}