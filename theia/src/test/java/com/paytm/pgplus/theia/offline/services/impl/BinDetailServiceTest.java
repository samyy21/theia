package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.cache.model.BinDetailWithDisplayName;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequest;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequestBody;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequestHead;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.test.testflow.AbstractPaymentServiceTest;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.CorporateCardUtil;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.enums.EChannelId.APP;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class BinDetailServiceTest extends AbstractPaymentServiceTest {

    @InjectMocks
    private BinDetailService binDetailService;
    @Mock
    private CardUtils cardUtils;
    @Mock
    private IPgpFf4jClient iPgpFf4jClient;
    @Mock
    private ICommonFacade commonFacade;
    @Mock
    private CorporateCardUtil corporateCardUtil;
    @Mock
    protected ChecksumService checksumService;
    @Mock
    private SuccessRateUtils successRateUtils;
    @Mock
    private IMerchantMappingService merchantMappingService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private MerchantPreferenceProvider merchantPreferenceProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(BinDetailService.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBinDetailServiceWhenHeadNull() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        request.setHead(null);
        request.setBody(null);
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Missing mandatory element");
        binDetailService.validateBinDetails(request);
    }

    @Test
    public void testBinDetailServiceWhenBodyNull() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        request.setBody(null);
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Missing mandatory element");
        binDetailService.validateBinDetails(request);
    }

    @Test
    public void testBinDetailServiceWhenBinIsBlank() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        getbinDetailsRequestBody(request);
        request.getBody().setBin(" ");
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Bin number is not valid");
        binDetailService.validateBinDetails(request);

    }

    @Test
    public void testBinDetailServiceWhenMIDisBlank() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        request.getHead().setMid(" ");
        getbinDetailsRequestBody(request);
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Mid is invalid");
        binDetailService.validateBinDetails(request);
    }

    @Test
    public void testBinDetailServiceWhenOrderIDisBlank() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        getbinDetailsRequestBody(request);
        request.getBody().setOrderId(" ");
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("OrderId is invalid");
        binDetailService.validateBinDetails(request);
    }

    @Test
    public void testBinDetailServiceWhenChecksumIsBlank() throws BinDetailException {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        getbinDetailsRequestBody(request);
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Checksum provided is invalid");
        binDetailService.validateBinDetails(request);

    }

    @Test
    public void testValidateCheckSum() {
        BinDetailsRequest request = new BinDetailsRequest();
        getbinDetailsRequestHead(request);
        getbinDetailsRequestBody(request);
        when(checksumService.validateChecksum(any(), any())).thenReturn(true);
        assertTrue(binDetailService.validateChecksum(request));
    }

    @Test
    public void testFetchBinDetails() throws PaytmValidationException {
        Map<String, Object> context = new HashMap<>();
        BinDetailRequest request = getBinDetailRequest();
        BinDetailWithDisplayName binDetailWithDisplayName = new BinDetailWithDisplayName(1234L, 34658896L, true, "XYZ",
                "CREDIT_CARD", "ABC", "011", true, "XYZ", true, true, true, "ABC", true, true, true, "dd", true, true,
                "Rupees", "India", "$", "IN", "XYZ", "", "", "", true, "CC", "Regalia", new HashMap<>());
        when(cardUtils.fetchBinDetailsWithDisplayName(any())).thenReturn(binDetailWithDisplayName);
        when(
                iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.RETURN_BIN_DISPLAY_BANK_NAME_IN_FETCHBINDETAIL,
                        context, false)).thenReturn(true);
        BinDetailResponse binDetailResponse = new BinDetailResponse();
        binDetailResponse = binDetailService.fetchBinDetails(request);
        assertNotNull(binDetailResponse);
    }

    @Test
    public void testFetchBinDetailsWithSuccessRateForThirdParty() throws PaytmValidationException {
        Map<String, Object> context = new HashMap<>();
        BinDetailRequest request = getBinDetailRequest();
        BinDetailWithDisplayName binDetailWithDisplayName = new BinDetailWithDisplayName(1234L, 34658896L, true, "XYZ",
                "CREDIT_CARD", "ABC", "011", true, "XYZ", true, true, true, "ABC", true, true, true, "dd", true, true,
                "Rupees", "India", "$", "IN", "XYZ", "", "", "", true, "CC", "Regalia", new HashMap<>());
        when(cardUtils.fetchBinDetailsWithDisplayName(any())).thenReturn(binDetailWithDisplayName);
        when(
                iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.RETURN_BIN_DISPLAY_BANK_NAME_IN_FETCHBINDETAIL,
                        context, false)).thenReturn(true);
        binDetailWithDisplayName.setZeroSuccessRate(false);
        when(successRateUtils.checkIfLowSuccessRate(anyString(), any())).thenReturn(true);
        BinDetailResponse binDetailResponse = new BinDetailResponse();
        binDetailResponse = binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(request);
        assertNotNull(binDetailResponse);
    }

    @Test
    public void testFetchBinDetailsWithDisplayNameWithInvalidBin() throws PaytmValidationException, BinDetailException {
        BinDetailRequest request = new BinDetailRequest(new RequestHeader(), new BinDetailRequestBody());
        request.getBody().setBin("34658896");
        when(cardUtils.fetchBinDetails(any())).thenThrow(
                new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH));
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Bin number is not valid");
        binDetailService.fetchBinDetails(request);
    }

    @Test
    public void testFetchBinDetailsWithDisplayNameInSuccessRate() throws PaytmValidationException, BinDetailException {
        BinDetailRequest request = new BinDetailRequest(new RequestHeader(), new BinDetailRequestBody());
        request.getBody().setBin("34658896");
        when(cardUtils.fetchBinDetails(any())).thenThrow(
                new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH));
        exceptionRule.expect(BinDetailException.class);
        exceptionRule.expectMessage("Bin number is not valid");
        binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(request);
    }

    @Test
    public void testcheckAndAddIDebitOption() throws Exception {

        BinData binData = new BinData();
        binData.setPayMethod("DEBIT_CARD");
        BinDetailResponseBody binDetailResponseBody = new BinDetailResponseBody();
        binDetailResponseBody.setBinDetail(binData);
        BinDetailResponse binDetailResponse = new BinDetailResponse();
        binDetailResponse.setBody(binDetailResponseBody);

        BinDetailsRequest binDetailsRequest = new BinDetailsRequest();
        getbinDetailsRequestHead(binDetailsRequest);
        getbinDetailsRequestHead(binDetailsRequest);
        binDetailService.checkAndAddIDebitOption(httpServletRequest, binDetailsRequest, binDetailResponse);
        assertNotNull(binDetailResponse.getBody().getAuthModes());
    }

    private void getbinDetailsRequestHead(BinDetailsRequest request) {
        BinDetailsRequestHead binDetailsRequestHead = new BinDetailsRequestHead();
        binDetailsRequestHead.setMid("M");
        request.setHead(binDetailsRequestHead);
    }

    private void getbinDetailsRequestBody(BinDetailsRequest request) {
        BinDetailsRequestBody binDetailsRequestBody = new BinDetailsRequestBody();
        binDetailsRequestBody.setBin("346596");
        binDetailsRequestBody.setOrderId("M");
        request.setBody(binDetailsRequestBody);
    }

    private BinDetailRequest getBinDetailRequest() {
        BinDetailRequest request = new BinDetailRequest(new RequestHeader(new BaseHeader("M", "", "M", "M")),
                new BinDetailRequestBody());
        request.getHead().setToken("M");
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        request.getBody().setChannelId(APP);
        request.getBody().setBin("34658896");
        request.getBody().setOrderId("1234");
        request.getBody().setDeviceId("M");
        return request;
    }
}