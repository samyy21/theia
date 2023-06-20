package com.paytm.pgplus.cashier.service.test;

import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.service.impl.CashierCardServiceImpl;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.cashier.validator.service.IBankCardValidation;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import org.junit.Assert;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.cachecard.service.ICashierCardService;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.service.test.builder.CashierRequestBuilderBankcard;
import com.paytm.pgplus.cashier.service.test.builder.CashierRequestBuilderSavecard;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;

/**
 * @author Vivek Kumar
 */

public class CashierCardServiceImplTest {
    @InjectMocks
    ICashierCardService cachecardServiceImpl = new CashierCardServiceImpl();

    CashierRequestBuilderBankcard cashierRequestBankcardBuilder = new CashierRequestBuilderBankcard();

    CashierRequestBuilderSavecard cashierRequestSavedCardBuilder = new CashierRequestBuilderSavecard();

    @org.mockito.Mock
    IFacadeService facadeServiceImpl;

    @org.mockito.Mock
    CashierUtilService cashierUtilService;

    @org.mockito.Mock
    IBankCardValidation bankCardValidation;

    @org.mockito.Mock
    BankCardRequest bankCardRequest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CashierCardServiceImplTest.class);

    @Test(expected = NullPointerException.class)
    public void testValidBankCardRequest() {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"f91ab50a5e4f49b2a94fa2005d0ec725administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-05-31T12:53:58+05:30\"},\"body\":{\"maskedCardNo\":\"416021******1737\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"2017053104511c2fe9a215b04c826b1bd35fdbc5c3a5b\",\"cardIndexNo\":\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {

            CashierRequest cashierRequest = cashierRequestBankcardBuilder.getBankCardRequest();
            CacheCardResponseBody cacheCardResponse = cachecardServiceImpl.submitCacheCard(cashierRequest,
                    InstNetworkType.ISOCARD);
            String ccToken = cacheCardResponse.getTokenId();
            Assert.assertEquals("2017053104511c2fe9a215b04c826b1bd35fdbc5c3a5b", ccToken);

        } catch (CashierCheckedException | PaytmValidationException e) {
            LOGGER.error("Exception Occurred : {} ", e);
        }
    }

    @Test
    public void testValidSavedCardRequest() {
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"84da7ed6d1444bb7a88e1d70df0e0d98administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-05-31T17:42:57+05:30\"},\"body\":{\"maskedCardNo\":\"471865******0336\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"20170531041119469b2d3bd8950161ad582bb3f38d78b\",\"cardIndexNo\":\"20160527001105c1d21bf9369fc5349d3d50fc8e3582c\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {

            CashierRequest cashierRequest = cashierRequestSavedCardBuilder.getSavedCardRequest();

            CacheCardResponseBody cacheCardResponse = cachecardServiceImpl.submitCacheCard(cashierRequest,
                    InstNetworkType.ISOCARD);
            String ccToken = cacheCardResponse.getTokenId();
            System.out.println(ccToken);
            Assert.assertEquals("20170531041119469b2d3bd8950161ad582bb3f38d78b", ccToken);

        } catch (CashierCheckedException | PaytmValidationException e) {
            // e.printStackTrace();
        }

    }

    @Test(expected = NullPointerException.class)
    public void testEmptyToken() throws PaytmValidationException, CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"f91ab50a5e4f49b2a94fa2005d0ec725administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-05-31T12:53:58+05:30\"},\"body\":{\"maskedCardNo\":\"416021******1737\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"\",\"cardIndexNo\":\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierRequestBankcardBuilder.getBankCardRequest();
        cachecardServiceImpl.submitCacheCard(cashierRequest, InstNetworkType.ISOCARD);
    }

}
