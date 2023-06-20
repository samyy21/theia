package com.paytm.pgplus.theia.emiSubvention.helper;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.ExpressCardModel;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.facade.emisubvention.enums.EmiType;
import com.paytm.pgplus.facade.emisubvention.enums.GratificationType;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.TenuresResponse;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.SimplifiedSubvention;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.emiSubvention.model.PaymentDetails;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequestBody;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.response.CardDetails;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class SubventionEmiServiceHelperTest {

    @InjectMocks
    private SubventionEmiServiceHelper subventionEmiServiceHelper = new SubventionEmiServiceHelper();

    @Mock
    private IBinFetchService binFetchService;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private BizRequestResponseMapperImpl bizRequestResponseMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubventionEmiServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPrepareQueryParams() {
        subventionEmiServiceHelper.prepareQueryParams("mid", "custId", "orderId");
        subventionEmiServiceHelper.prepareQueryParams("mid", "", "orderId");
    }

    @Test
    public void testPrepareEmiServiceRequest() throws MappingServiceClientException {
        ValidateEmiRequestBody validateEmiRequestBody = new ValidateEmiRequestBody();
        validateEmiRequestBody.setPrice(200.00);
        validateEmiRequestBody.setSubventionAmount(400.00);
        validateEmiRequestBody.setMid("mid");
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTotalTransactionAmount(700.00);
        paymentDetails.setCardNumber(null);
        paymentDetails.setCardBin6("234567777");
        validateEmiRequestBody.setPaymentDetails(paymentDetails);
        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("CC");
        binDetail.setBankCode("123456");
        binDetail.setCardName("cardName");
        when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail);
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);

        paymentDetails.setCardNumber("123456789");
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);

        paymentDetails.setCardNumber("cardNUmber");
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);

        paymentDetails.setCardNumber("1234");
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);

        validateEmiRequestBody.setPrice(600.00);
        validateEmiRequestBody.setSubventionAmount(300.00);
        validateEmiRequestBody.setPaymentDetails(new PaymentDetails());
        ExpressCardModel expressCardModel = new ExpressCardModel();
        expressCardModel.setCardBin("1234566");
        expressCardModel.setCardIndexNo("6666666");
        when((ExpressCardModel) theiaTransactionalRedisUtil.get(any())).thenReturn(expressCardModel);
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);

        try {
            BinDetail binDetail1 = new BinDetail();
            binDetail1.setCardType("CC");
            when(binFetchService.getCardBinDetail(any())).thenReturn(binDetail1);
            subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);
        } catch (PaymentRequestValidationException e) {
        }

        try {
            when((ExpressCardModel) theiaTransactionalRedisUtil.get(any())).thenReturn(null);
            subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);
        } catch (PaymentRequestValidationException e) {
        }

        try {
            PaymentDetails paymentDetails1 = new PaymentDetails();
            paymentDetails1.setCardNumber("99|134|234|88888888");
            validateEmiRequestBody.setPaymentDetails(paymentDetails1);
            validateEmiRequestBody.setCustomerId("custId");
            subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);
        } catch (Exception e) {
        }

        try {
            PaymentDetails paymentDetails1 = new PaymentDetails();
            paymentDetails1.setCardNumber("99");
            validateEmiRequestBody.setPaymentDetails(paymentDetails1);
            subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "txnToken", null);
        } catch (RequestValidationException e) {
        }

    }

    @Test
    public void testPrepareEmiServiceRequest1() {
        ValidateEmiRequestBody validateEmiRequestBody = new ValidateEmiRequestBody();
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setSavedInstrumentId("instrumentId");
        paymentDetails.setTotalTransactionAmount(400.0);
        validateEmiRequestBody.setPaymentDetails(paymentDetails);
        validateEmiRequestBody.setItems(Collections.singletonList(new Item()));
        SavedCard savedCard = new SavedCard();
        savedCard.setIssuingBank("PPBL");
        CardDetails cardDetails = new CardDetails();
        cardDetails.setCardType("VISA");
        cardDetails.setFirstSixDigit("123456");
        savedCard.setInstId("instId");
        savedCard.setCardDetails(cardDetails);
        when(nativeSessionUtil.getCashierInfoResponse(any())).thenReturn(new NativeCashierInfoResponse());
        when(bizRequestResponseMapper.getSavedCardDetails(any(), any())).thenReturn(savedCard);
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "token", null);

        SavedCard savedCard1 = new SavedCard();
        savedCard1.setIssuingBank(null);
        savedCard1.setCardDetails(new CardDetails());
        exceptionRule.expect(PaymentRequestValidationException.class);
        when(bizRequestResponseMapper.getSavedCardDetails(any(), any())).thenReturn(savedCard1);
        subventionEmiServiceHelper.prepareEmiServiceRequest(validateEmiRequestBody, "token", null);
    }

    @Test
    public void testPrepareEmiServiceRequestBankRequest() {
        EmiBanksRequest emiBanksRequest = new EmiBanksRequest();
        EmiBanksRequestBody body = new EmiBanksRequestBody();
        body.setMid("mid");
        body.setItems(Collections.singletonList(new Item()));
        emiBanksRequest.setBody(body);
        subventionEmiServiceHelper.prepareEmiServiceRequest(emiBanksRequest);
    }

    @Test
    public void testPrepareEmiServiceRequestAmountBased() {
        EmiBanksRequest emiBanksRequest = new EmiBanksRequest();
        EmiBanksRequestBody body = new EmiBanksRequestBody();
        body.setPrice(400.0);
        body.setSubventionAmount(200.0);
        body.setMid("mid");
        emiBanksRequest.setBody(body);
        subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(emiBanksRequest);
    }

    @Test
    public void testPrepareItemListForAmountBasedSubvention() {
        InitiateTransactionRequestBody orderDetail = new InitiateTransactionRequestBody();
        Money money = new Money();
        money.setValue("500");
        orderDetail.setTxnAmount(money);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention();
        simplifiedSubvention.setSubventionAmount(200.0);
        orderDetail.setSimplifiedSubvention(simplifiedSubvention);
        orderDetail.setMid("mid");
        subventionEmiServiceHelper.prepareItemListForAmountBasedSubvention(orderDetail);

        money.setValue("100");
        subventionEmiServiceHelper.prepareItemListForAmountBasedSubvention(orderDetail);
    }

    @Test
    public void testPrepareEmiServiceRequestTenuresRequest() {
        EmiTenuresRequest emiTenuresRequest = new EmiTenuresRequest();
        EmiTenuresRequestBody body = new EmiTenuresRequestBody();
        body.setItems(Collections.singletonList(new Item()));
        body.setFilters(new Filter());
        body.setMid("mid");
        emiTenuresRequest.setBody(body);
        subventionEmiServiceHelper.prepareEmiServiceRequest(emiTenuresRequest);
    }

    @Test
    public void testPrepareEmiServiceRequestAmountBasedTenuresRequest() {
        EmiTenuresRequest emiTenuresRequest = new EmiTenuresRequest();
        EmiTenuresRequestBody body = new EmiTenuresRequestBody();
        body.setMid("mid");
        body.setSubventionAmount(400.0);
        body.setPrice(600.0);
        body.setFilters(new Filter());
        emiTenuresRequest.setBody(body);
        subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(emiTenuresRequest);

        body.setSubventionAmount(400.0);
        body.setPrice(200.0);
        subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(emiTenuresRequest);
    }

    @Test
    public void testPrepareBanksEmiResponse() {
        GenericEmiSubventionResponse<BanksResponse> banksResponse = new GenericEmiSubventionResponse<>();
        EmiBanksRequest request = new EmiBanksRequest();
        TokenRequestHeader head = new TokenRequestHeader();
        head.setRequestId("requestId");
        request.setHead(head);
        new MockUp<EmiSubventionUtils>() {
            @mockit.Mock
            public ResponseHeader createResponseHeader() {
                return new ResponseHeader();
            }
        };
        BanksResponse banksResponse1 = new BanksResponse();
        banksResponse1.setEmiTypes(Collections.singletonList(new BankDetailsByEmiType()));
        banksResponse.setStatus(1);
        banksResponse.setData(banksResponse1);
        subventionEmiServiceHelper.prepareBanksEmiResponse(banksResponse, request);

        banksResponse.setStatus(0);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode("code");
        errorResponse.setMessage("message");
        banksResponse.setError(errorResponse);
        exceptionRule.expect(BaseException.class);
        subventionEmiServiceHelper.prepareBanksEmiResponse(banksResponse, request);
    }

    @Test
    public void testPrepareTenuresEmiResponse() {
        GenericEmiSubventionResponse<TenuresResponse> tenuresResponse = new GenericEmiSubventionResponse<>();
        EmiTenuresRequest request = new EmiTenuresRequest();
        TokenRequestHeader head = new TokenRequestHeader();
        head.setRequestId("requestId");
        request.setHead(head);
        new MockUp<EmiSubventionUtils>() {
            @mockit.Mock
            public ResponseHeader createResponseHeader() {
                return new ResponseHeader();
            }
        };
        tenuresResponse.setStatus(1);
        TenuresResponse tenuresResponse1 = new TenuresResponse();
        tenuresResponse1.setBankCode("bankCode");
        tenuresResponse1.setBankName("PPBL");
        tenuresResponse1.setCardType("CC");
        tenuresResponse1.setBankLogoUrl("logoUrl");
        PlanDetail planDetail = new PlanDetail();
        ItemBreakUp itemBreakUp = new ItemBreakUp();
        itemBreakUp.setId("SUB");
        planDetail.setEmiType(EmiType.STANDARD);
        planDetail.setItemBreakUp(Collections.singletonList(itemBreakUp));
        List<PlanDetail> planDetails = new ArrayList<>();
        planDetails.add(planDetail);
        tenuresResponse1.setPlanDetails(planDetails);
        tenuresResponse.setData(tenuresResponse1);
        EmiTenuresRequestBody body = new EmiTenuresRequestBody();
        body.setPrice(100.0);
        request.setBody(body);
        subventionEmiServiceHelper.prepareTenuresEmiResponse(tenuresResponse, request);

        itemBreakUp.setId("STAN");
        planDetail.setEmiType(EmiType.SUBVENTION);
        planDetail.setGratifications(Collections.singletonList(new Gratification()));
        subventionEmiServiceHelper.prepareTenuresEmiResponse(tenuresResponse, request);

        Gratification gratification = new Gratification();
        gratification.setType(GratificationType.DISCOUNT);
        gratification.setValue(200.0);
        planDetail.setGratifications(Collections.singletonList(gratification));
        subventionEmiServiceHelper.prepareTenuresEmiResponse(tenuresResponse, request);

        Item item = new Item();
        item.setPrice(300.0);
        request.getBody().setItems(Collections.singletonList(item));
        subventionEmiServiceHelper.prepareTenuresEmiResponse(tenuresResponse, request);

        tenuresResponse.setStatus(0);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode("code");
        errorResponse.setMessage("message");
        tenuresResponse.setError(errorResponse);
        exceptionRule.expect(BaseException.class);
        subventionEmiServiceHelper.prepareTenuresEmiResponse(tenuresResponse, request);
    }

    @Test
    public void testPrepareValidateEmiResponse() {
        GenericEmiSubventionResponse<ValidateResponse> validateEmiServiceResponse = new GenericEmiSubventionResponse<>();
        ValidateEmiRequest request = new ValidateEmiRequest();
        ValidateRequest validateRequest = new ValidateRequest();
        TokenRequestHeader head = new TokenRequestHeader();
        head.setRequestId("requestId");
        request.setHead(head);
        new MockUp<EmiSubventionUtils>() {
            @mockit.Mock
            public ResponseHeader createResponseHeader() {
                return new ResponseHeader();
            }
        };
        validateEmiServiceResponse.setStatus(1);
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setCardType("CC");
        validateResponse.setBankCode("bankCode");
        validateResponse.setBankId("bankId");
        validateResponse.setBankName("PPBL");
        validateResponse.setBankLogoUrl("logoUrl");
        validateResponse.setPgPlanId("pgPlanId");
        validateResponse.setPlanId("planId");
        validateResponse.setRate(100.0);
        validateResponse.setInterval(new Integer(10));
        validateResponse.setEmi(200.0);
        validateResponse.setInterest(100.0);
        validateResponse.setEmiType(EmiType.SUBVENTION);
        validateResponse.setEmiLabel("emiLabel");
        validateResponse.setGratifications(Collections.singletonList(new Gratification()));
        ItemBreakUp itemBreakUp = new ItemBreakUp();
        itemBreakUp.setId("SUB");
        validateResponse.setItemBreakUp(Collections.singletonList(itemBreakUp));
        validateEmiServiceResponse.setData(validateResponse);
        ValidateEmiRequestBody body = new ValidateEmiRequestBody();
        body.setPrice(600.0);
        request.setBody(body);
        subventionEmiServiceHelper.prepareValidateEmiResponse(validateEmiServiceResponse, request, validateRequest);

        itemBreakUp.setId("STAN");
        body.setItems(Collections.singletonList(new Item()));
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTotalTransactionAmount(400.0);
        body.setPaymentDetails(paymentDetails);
        body.setGenerateTokenForIntent(false);
        subventionEmiServiceHelper.prepareValidateEmiResponse(validateEmiServiceResponse, request, validateRequest);

        body.setGenerateTokenForIntent(true);
        subventionEmiServiceHelper.prepareValidateEmiResponse(validateEmiServiceResponse, request, validateRequest);

        try {
            validateResponse.setEmiType(EmiType.STANDARD);
            subventionEmiServiceHelper.prepareValidateEmiResponse(validateEmiServiceResponse, request, validateRequest);
        } catch (RequestValidationException e) {
        }

        validateEmiServiceResponse.setStatus(0);
        exceptionRule.expect(BaseException.class);
        ErrorResponse response = new ErrorResponse();
        response.setMessage("message");
        response.setCode("code");
        validateEmiServiceResponse.setError(response);
        subventionEmiServiceHelper.prepareValidateEmiResponse(validateEmiServiceResponse, request, validateRequest);
    }

    @Test
    public void testPrepareHeaderMapForValidateEmiRequest() {
        ValidateEmiRequestBody validateEmiRequestBody = new ValidateEmiRequestBody();
        validateEmiRequestBody.setCustomerId("custId");
        subventionEmiServiceHelper.prepareHeaderMapForValidateEmiRequest(validateEmiRequestBody);
    }

    @Test
    public void testCheckIfMidExist() {
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn("merchantKey");
        subventionEmiServiceHelper.checkIfMidExist("mid");

        exceptionRule.expect(RequestValidationException.class);
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn(null);
        subventionEmiServiceHelper.checkIfMidExist("mid");
    }

    @Test
    public void testValidateSimplifiedEmiRequest() {
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention();
        simplifiedSubvention.setPlanId("planId");
        simplifiedSubvention.setMid("mid");
        simplifiedSubvention.setCustomerId("custId");
        simplifiedSubvention.setOrderId("orderId");
        simplifiedSubvention.setCacheCardToken("cardToken");
        com.paytm.pgplus.models.Item item = new com.paytm.pgplus.models.Item();
        item.setPrice(700.0);
        simplifiedSubvention.setItems(Collections.singletonList(item));
        com.paytm.pgplus.models.PaymentDetails paymentDetails = new com.paytm.pgplus.models.PaymentDetails();
        paymentDetails.setTotalTransactionAmount(700.0);
        simplifiedSubvention.setPaymentDetails(paymentDetails);
        simplifiedSubvention.setOfferDetails(new com.paytm.pgplus.models.OfferDetail());
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn("merchantKey");
        subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);

        try {
            simplifiedSubvention.setMid(null);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setMid("mid");
            simplifiedSubvention.setPaymentDetails(null);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setMid("mid");
            com.paytm.pgplus.models.PaymentDetails paymentDetails1 = new com.paytm.pgplus.models.PaymentDetails();
            simplifiedSubvention.setPaymentDetails(paymentDetails1);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setMid("mid");
            com.paytm.pgplus.models.PaymentDetails paymentDetails1 = new com.paytm.pgplus.models.PaymentDetails();
            paymentDetails1.setTotalTransactionAmount(0d);
            simplifiedSubvention.setPaymentDetails(paymentDetails1);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setMid("mid");
            com.paytm.pgplus.models.PaymentDetails paymentDetails1 = new com.paytm.pgplus.models.PaymentDetails();
            paymentDetails1.setTotalTransactionAmount(100.0);
            simplifiedSubvention.setPaymentDetails(paymentDetails1);
            simplifiedSubvention.setItems(null);
            simplifiedSubvention.setPrice(200.0);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

    }

    @Test
    public void testValidateSimplifiedEmiRequest1() {
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention();
        simplifiedSubvention.setPlanId("planId");
        simplifiedSubvention.setMid("mid");
        simplifiedSubvention.setCustomerId("custId");
        simplifiedSubvention.setOrderId("orderId");
        simplifiedSubvention.setCacheCardToken("cardToken");
        com.paytm.pgplus.models.Item item = new com.paytm.pgplus.models.Item();
        item.setPrice(700.0);
        simplifiedSubvention.setItems(Collections.singletonList(item));
        com.paytm.pgplus.models.PaymentDetails paymentDetails = new com.paytm.pgplus.models.PaymentDetails();
        paymentDetails.setTotalTransactionAmount(700.0);
        simplifiedSubvention.setPaymentDetails(paymentDetails);
        simplifiedSubvention.setOfferDetails(new com.paytm.pgplus.models.OfferDetail());
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn("merchantKey");
        subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);

        try {
            simplifiedSubvention.setPrice(100.0);
            simplifiedSubvention.setSubventionAmount(200.0);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setItems(null);
            simplifiedSubvention.setPrice(700.0);
            simplifiedSubvention.setSubventionAmount(900.0);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setItems(null);
            com.paytm.pgplus.models.PaymentDetails paymentDetails1 = new com.paytm.pgplus.models.PaymentDetails();
            paymentDetails1.setTotalTransactionAmount(200.0);
            simplifiedSubvention.setPaymentDetails(paymentDetails1);
            simplifiedSubvention.setPrice(200.0);
            simplifiedSubvention.setSubventionAmount(null);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

        try {
            simplifiedSubvention.setItems(null);
            com.paytm.pgplus.models.PaymentDetails paymentDetails1 = new com.paytm.pgplus.models.PaymentDetails();
            paymentDetails1.setTotalTransactionAmount(10.0);
            simplifiedSubvention.setPaymentDetails(paymentDetails1);
            simplifiedSubvention.setPrice(10.0);
            simplifiedSubvention.setSubventionAmount(0.0);
            subventionEmiServiceHelper.validateSimplifiedEmiRequest(simplifiedSubvention);
        } catch (RequestValidationException e) {
        }

    }

    @Test
    public void testValidateRequestBody() {
        ValidateEmiRequestBody body = new ValidateEmiRequestBody();
        when(merchantExtendInfoUtils.getMerchantKey(any())).thenReturn("merchantKey");
        body.setMid("mid");
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTotalTransactionAmount(100.0);
        body.setItems(null);
        body.setPrice(100.0);
        body.setSubventionAmount(20.0);
        body.setPaymentDetails(paymentDetails);
        paymentDetails.setCardNumber("1234567");
        body.setCacheCardToken("token");
        subventionEmiServiceHelper.validateRequestBody(body);

        paymentDetails.setCardNumber(null);
        body.setCacheCardToken(null);
        exceptionRule.expect(RequestValidationException.class);
        subventionEmiServiceHelper.validateRequestBody(body);
    }

    @Test
    public void testTransform() {
        com.paytm.pgplus.models.Item item = new com.paytm.pgplus.models.Item();
        SubventionEmiServiceHelper.transform(item);

        SubventionEmiServiceHelper.transform((com.paytm.pgplus.models.Item) null);
    }
}