package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.BizProdHelper;
import com.paytm.pgplus.biz.workflow.service.helper.MerchantBizProdHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ExpressPaymentServiceHelperTest {

    @InjectMocks
    private ExpressPaymentServiceHelper expressPaymentServiceHelper = new ExpressPaymentServiceHelper();

    @Mock
    ISavedCardService savedCardService;

    @Mock
    private IMerchantMappingService merchantMappingService;

    @Mock
    private CardUtils cardUtils;

    @Mock
    private BizProdHelper bizProdHelper;

    @Mock
    private MerchantBizProdHelper merchantBizProdHelper;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentServiceHelper.class);
    private static final String MAESTRO = "MAESTRO";
    private static final String MAESTRO_YEAR = "2049";
    private static final String MAESTRO_MONTH = "12";
    private static final String MAESTRO_CVV = "123";
    private static final long EXPRESS_TOKEN_EXPIRY = 600;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testSetCardDetilsInCache() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        requestData.setCardNumber("123456789");
        requestData.setCardScheme(MAESTRO);
        requestData.setExpiryYear(MAESTRO_YEAR);
        requestData.setExpiryMonth(MAESTRO_MONTH);
        requestData.setCvv(MAESTRO_CVV);
        expressPaymentServiceHelper.setCardDetilsInCache("token", requestData);

        ExpressCardTokenRequest requestData1 = new ExpressCardTokenRequest();
        requestData1.setCardNumber("123456789");
        requestData1.setCardScheme("VISA");
        requestData1.setExpiryYear("2050");
        requestData1.setExpiryMonth("10");
        requestData1.setCvv("999");
        expressPaymentServiceHelper.setCardDetilsInCache("token", requestData1);
    }

    @Test
    public void testSetCardTokenDetailsInCache() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        CacheCardResponseBean cacheCardResponseBean = new CacheCardResponseBean("tokenId", "maskedCardNo");
        requestData.setMid("mid");
        requestData.setUserId("userId");
        requestData.setCardNumber("12345678");
        cacheCardResponseBean.setCardIndexNo("cardIndexNo");
        cacheCardResponseBean.setTokenId("tokenId");
        expressPaymentServiceHelper.setCardTokenDetailsInCache("requestId", requestData, cacheCardResponseBean, false,
                null, null);
        expressPaymentServiceHelper.setCardTokenDetailsInCache("requestId", new ExpressCardTokenRequest(),
                new CacheCardResponseBean("123", "123"), false, null, null);
    }

    @Test
    public void testFetchSavedCardDetailsAndProcess() {
        ExpressCardTokenRequest requestData1 = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response1 = new ExpressCardTokenResponse();
        SavedCardResponse<SavedCardVO> savedCardsBean1 = new SavedCardResponse<>();
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setUserId("userId");
        requestData1.setLoginUserId("loginUserId");
        requestData1.setSavedCardId("1234567");
        savedCardsBean1.setStatus(true);
        savedCardsBean1.setResponseData(savedCardVO);
        when(savedCardService.getSavedCardByCardId(any(), any())).thenReturn(savedCardsBean1);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData1, response1);
    }

    @Test
    public void testFetchSavedCardDetailsAndProcess1() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        SavedCardResponse<SavedCardVO> savedCardsBean = new SavedCardResponse<>();
        savedCardsBean.setStatus(false);
        savedCardsBean.setMessage(TheiaConstant.ResponseConstants.INVALID_REPONSE_FROM_SERVICE);
        requestData.setSavedCardId("1234567");
        when(savedCardService.getSavedCardByCardId(any(), any(), any())).thenReturn(savedCardsBean);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);

        SavedCardResponse<SavedCardVO> savedCardsBean1 = new SavedCardResponse<>();
        when(savedCardService.getSavedCardByCardId(any(), any(), any())).thenReturn(savedCardsBean1);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);
    }

    @Test
    public void testFetchSavedCardDetailsAndProcess2() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        SavedCardResponse<SavedCardVO> savedCardsBean = new SavedCardResponse<>();
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setUserId("userId");
        savedCardsBean.setStatus(true);
        requestData.setSavedCardId("1234567");
        savedCardsBean.setResponseData(savedCardVO);
        when(savedCardService.getSavedCardByCardId(any(), any())).thenReturn(savedCardsBean);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);
    }

    @Test
    public void testFetchSavedCardDetailsAndProcess3() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        SavedCardResponse<SavedCardVO> savedCardsBean = new SavedCardResponse<>();
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setUserId("userId");
        savedCardVO.setExpiryDate("112049");
        savedCardsBean.setStatus(true);
        requestData.setSavedCardId("1234567");
        savedCardsBean.setResponseData(savedCardVO);
        requestData.setLoginUserId("userId");
        when(savedCardService.getSavedCardByCardId(any(), any())).thenReturn(savedCardsBean);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);
    }

    @Test
    public void testFetchSavedCardDetailsAndProcess4() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        SavedCardResponse<SavedCardVO> savedCardsBean = new SavedCardResponse<>();
        SavedCardVO savedCardVO = new SavedCardVO();
        savedCardVO.setCustId("custId");
        savedCardsBean.setStatus(true);
        requestData.setSavedCardId("1234567");
        requestData.setUserId("userId");
        savedCardsBean.setResponseData(savedCardVO);
        when(savedCardService.getSavedCardByCardId(any(), any(), any())).thenReturn(savedCardsBean);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);
    }

    @Test
    public void testMapExpressFlowReqData() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        requestData.setMid("mid");
        requestData.setCardNumber("12345678");
        requestData.setCardScheme(MAESTRO);
        requestData.setSavedCardId("1234567891234567");
        GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = new GenericCoreResponseBean<MappingMerchantData>(
                new MappingMerchantData());
        when(merchantMappingService.fetchMerchanData(any())).thenReturn(merchantMappingResponse);
        expressPaymentServiceHelper.mapExpressFlowReqData(requestData, workFlowRequestBean);

        ExpressCardTokenRequest requestData1 = new ExpressCardTokenRequest();
        requestData1.setMid("mid");
        requestData1.setCardNumber("12345678");
        requestData1.setCardScheme("VISA");
        requestData1.setExpiryMonth("10");
        requestData1.setExpiryYear("2089");
        requestData1.setCvv("345");
        expressPaymentServiceHelper.mapExpressFlowReqData(requestData1, workFlowRequestBean);

        ExpressCardTokenRequest requestData2 = new ExpressCardTokenRequest();
        requestData2.setCardScheme("VISA");
        expressPaymentServiceHelper.mapExpressFlowReqData(requestData2, workFlowRequestBean);

        GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse1 = new GenericCoreResponseBean<MappingMerchantData>(
                "description", ResponseConstants.MERCHANT_BLOCKED);
        exceptionRule.expect(PaymentRequestValidationException.class);
        when(merchantMappingService.fetchMerchanData(any())).thenReturn(merchantMappingResponse1);
        expressPaymentServiceHelper.mapExpressFlowReqData(requestData, workFlowRequestBean);
    }

    @Test
    public void testFetchBinRelatedDetails() throws PaytmValidationException {
        when(cardUtils.fetchBinDetails(any())).thenReturn(new BinDetail());
        expressPaymentServiceHelper.fetchBinRelatedDetails("12345678");

        when(cardUtils.fetchBinDetails(any())).thenReturn(null);
        expressPaymentServiceHelper.fetchBinRelatedDetails("12345678");
    }

    @Test
    public void testFetchSavedCardDetailsAndProcessWithCardIndexNumber() {
        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest();
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcessWithCardIndexNumber(requestData, response);

        SavedAssetInfo savedAssetInfo = new SavedAssetInfo();
        savedAssetInfo.setMaskedCardNo("1234567");
        savedAssetInfo.setExpiryMonth("10");
        savedAssetInfo.setExpiryYear("2050");
        savedAssetInfo.setCardScheme("VISA");
        when(merchantBizProdHelper.getSavedCardByMidCustIdAndCardId(any(), any(), any())).thenReturn(savedAssetInfo);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcessWithCardIndexNumber(requestData, response);

        when(bizProdHelper.getSavedCardByUserIdAndCardId(any(), any())).thenReturn(savedAssetInfo);
        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcessWithCardIndexNumber(requestData, response);
    }

}