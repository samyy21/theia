package com.paytm.pgplus.theia.controller.test;

import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.theia.services.impl.MerchantPayOptionServiceImpl;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ankitgupta on 11/8/17.
 */
@RestController
public class MerchantPayoptionsControllerTest extends AbstractControllerTest {

    @Test
    public void testGetPayMethods_Success() throws Exception {
        mockPlatformMerchantId();
        mockMerchantExtendedInfo();

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/payment/getPayMethods")
                                .header("X-Forwarded-For", "157.49.0.80,49.44.115.108")
                                .header("User-Agent",
                                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36")
                                .param("CHANNEL_ID", "WEB").param("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308")
                                .param("DEVICE_SOURCE", "PGPLUS")
                                .content("{\"request_id\": \"FB1\", \"merchant_account_id\": \"someRandomMerchant\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    private void mockPlatformMerchantId() {
        new MockUp<MerchantPayOptionServiceImpl>() {
            @Mock
            private String fetchAlipayMerchantId(String mId) {
                return "216820000000143510094";
            }
        };
    }

    private void mockMerchantExtendedInfo() {
        new MockUp<MerchantDataServiceImpl>() {
            @Mock
            public MerchantExtendedInfoResponse getMerchantExtendedData(final String merchantId)
                    throws MappingServiceClientException {
                MerchantExtendedInfoResponse merchantExtendedInfoResponse = new MerchantExtendedInfoResponse();

                merchantExtendedInfoResponse.setResultInfo(new MappingServiceResultInfo());
                merchantExtendedInfoResponse.setMerchantId(merchantId);
                MerchantExtendedInfoResponse.MerchantExtendedInfo merchantExtendedInfo = new MerchantExtendedInfoResponse.MerchantExtendedInfo();
                merchantExtendedInfoResponse.setExtendedInfo(merchantExtendedInfo);
                return merchantExtendedInfoResponse;
            }
        };
    }
}
