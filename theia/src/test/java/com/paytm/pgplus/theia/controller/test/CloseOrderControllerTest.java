package com.paytm.pgplus.theia.controller.test;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.theia.models.CancelTransResponse;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;

/**
 * @author kartik
 * @date 11-07-2017
 */
@RestController
public class CloseOrderControllerTest extends AbstractControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSuccessCloseFundOrder() throws Exception {

        // Mock Platform+
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final AlipayServiceUrl api, final Class<T> clazz) {
                String alipayResponse = "";

                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_QUERY_BY_MERCHANT_TRANS_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.acquiring.order.queryByMerchantTransId\",\"clientId\":\"clientId\",\"reqMsgId\":\"e9324e4209404bcba94f06aada4446b1administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:20:42+05:30\"},\"body\":{\"resultInfo\":{\"resultMsg\":\"aqcPreGlobalUniqueDO[buyerUserId] is null, merchantTransId =PARCEL236974,merchantId =216820000000141015249\",\"resultStatus\":\"F\",\"resultCodeId\":\"00000020\",\"resultCode\":\"TARGET_NOT_FOUND\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_USER_ORDER_QUERY_BY_MERCHANT_REQUEST_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.user.order.queryByMerchantRequestId\",\"clientId\":\"clientId\",\"reqMsgId\":\"2c62197ab5ca4e57b5ea8092d8a1c061administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:00+05:30\"},\"body\":{\"fundOrder\":{\"actualFundAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"modifiedTime\":\"2017-07-11T14:57:35+05:30\",\"taxAmount\":{\"currency\":\"INR\",\"value\":\"0\"},\"terminalType\":\"WEB\",\"fundOrderStatus\":\"CLOSE\",\"fundOrderId\":\"2017071120121481010100166012300020421\",\"extendInfo\":\"{\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"peonURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"theme\\\":\\\"merchant\\\",\\\"website\\\":\\\"retail\\\",\\\"promoCode\\\":\\\"\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL236974\\\",\\\"paytmMerchantId\\\":\\\"HYBADD50520222544592\\\",\\\"alipayMerchantId\\\":\\\"216820000000141015249\\\",\\\"ssoToken\\\":\\\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"requestType\\\":\\\"ADD_MONEY\\\",\\\"productCode\\\":\\\"51051000100000000002\\\",\\\"totalTxnAmount\\\":\\\"24\\\",\\\"merchantName\\\":\\\"HYB-AD\\\",\\\"topupAndPay\\\":false}\",\"chargeAmount\":{\"value\":\"0\",\"currency\":\"INR\"},\"paidTotalAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"fundAmount\":{\"currency\":\"INR\",\"value\":\"2400\"},\"actorUserId\":\"216810000000139523012\",\"productCode\":\"51053000100000000011\",\"invokerId\":\"216820000000141015249\",\"payeeIdentifier\":{\"userId\":\"216810000000139523012\"},\"createdTime\":\"2017-07-11T14:55:57+05:30\",\"requestId\":\"PARCEL236974\",\"payeeAccountNo\":\"20070000000006436016\",\"payExpiryTime\":\"2017-07-14T14:55:57+05:30\",\"fundType\":\"TOPUP_MULTIPAY_MODE\"},\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCodeId\":\"00000000\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_ORDER_CLOSE)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.order.close\",\"clientId\":\"clientId\",\"reqMsgId\":\"9e9f698c42694a5e9082eff85b21af37administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:17+05:30\"},\"body\":{\"fundOrderId\":\"2017071120121481010100166012300020421\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\"}}},\"signature\":\"no_signature\"}";
                }

                Gson gson = new Gson();
                JsonElement json = gson.fromJson(alipayResponse, JsonElement.class);
                String alipayResponseAsJsonString = gson.toJson(json);

                String responseString = null;
                try {
                    responseString = testResource.getObjectMapper().readTree(alipayResponseAsJsonString)
                            .get("response").toString();
                } catch (JsonProcessingException e) {
                    LOGGER.error("{}", e);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                final T response = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), responseString,
                        clazz);
                return response;
            }
        };

        String json = "{\"ORDER_ID\":\"PARCEL236974\",\"MID\":\"HYBADD50520222544592\",\"USER_TOKEN\":\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/closeOrder").with(new RequestPostProcessor() {

            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setParameter("X-Forwarded-For", "157.49.0.80,49.44.115.108");
                request.setParameter("User-Agent", "DummyUserAgent");
                request.setParameter("CHANNEL_ID", "WEB");
                request.setParameter("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308");
                return request;
            }
        }).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        CancelTransResponse closeResponse = objectMapper.readValue(response, CancelTransResponse.class);
        Assert.assertNotNull(closeResponse.getStatus());
    }

    @Test
    public void testFundOrderAlreadyClose() throws Exception {

        // Mock Platform+
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final AlipayServiceUrl api, final Class<T> clazz) {
                String alipayResponse = "";

                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_QUERY_BY_MERCHANT_TRANS_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.acquiring.order.queryByMerchantTransId\",\"clientId\":\"clientId\",\"reqMsgId\":\"e9324e4209404bcba94f06aada4446b1administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:20:42+05:30\"},\"body\":{\"resultInfo\":{\"resultMsg\":\"aqcPreGlobalUniqueDO[buyerUserId] is null, merchantTransId =PARCEL236974,merchantId =216820000000141015249\",\"resultStatus\":\"F\",\"resultCodeId\":\"00000020\",\"resultCode\":\"TARGET_NOT_FOUND\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_USER_ORDER_QUERY_BY_MERCHANT_REQUEST_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.user.order.queryByMerchantRequestId\",\"clientId\":\"clientId\",\"reqMsgId\":\"2c62197ab5ca4e57b5ea8092d8a1c061administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:00+05:30\"},\"body\":{\"fundOrder\":{\"actualFundAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"modifiedTime\":\"2017-07-11T14:57:35+05:30\",\"taxAmount\":{\"currency\":\"INR\",\"value\":\"0\"},\"terminalType\":\"WEB\",\"fundOrderStatus\":\"CLOSE\",\"fundOrderId\":\"2017071120121481010100166012300020421\",\"extendInfo\":\"{\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"peonURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"theme\\\":\\\"merchant\\\",\\\"website\\\":\\\"retail\\\",\\\"promoCode\\\":\\\"\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL236974\\\",\\\"paytmMerchantId\\\":\\\"HYBADD50520222544592\\\",\\\"alipayMerchantId\\\":\\\"216820000000141015249\\\",\\\"ssoToken\\\":\\\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"requestType\\\":\\\"ADD_MONEY\\\",\\\"productCode\\\":\\\"51051000100000000002\\\",\\\"totalTxnAmount\\\":\\\"24\\\",\\\"merchantName\\\":\\\"HYB-AD\\\",\\\"topupAndPay\\\":false}\",\"chargeAmount\":{\"value\":\"0\",\"currency\":\"INR\"},\"paidTotalAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"fundAmount\":{\"currency\":\"INR\",\"value\":\"2400\"},\"actorUserId\":\"216810000000139523012\",\"productCode\":\"51053000100000000011\",\"invokerId\":\"216820000000141015249\",\"payeeIdentifier\":{\"userId\":\"216810000000139523012\"},\"createdTime\":\"2017-07-11T14:55:57+05:30\",\"requestId\":\"PARCEL236974\",\"payeeAccountNo\":\"20070000000006436016\",\"payExpiryTime\":\"2017-07-14T14:55:57+05:30\",\"fundType\":\"TOPUP_MULTIPAY_MODE\"},\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCodeId\":\"00000000\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_ORDER_CLOSE)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.order.close\",\"clientId\":\"clientId\",\"reqMsgId\":\"9e9f698c42694a5e9082eff85b21af37administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:17+05:30\"},\"body\":{\"resultInfo\":{\"resultMsg\":\"fundOrder has already been closed triggered by system close\",\"resultCodeId\":\"00000012\",\"resultCode\":\"SUCCESS_IDEMPOTENT_ERROR\",\"resultStatus\":\"S\"}}},\"signature\":\"no_signature\"}";
                }
                String responseString = null;
                try {
                    responseString = testResource.getObjectMapper().readTree(alipayResponse).get("response").toString();
                } catch (JsonProcessingException e) {
                    LOGGER.error("{}", e);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                final T response = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), responseString,
                        clazz);
                return response;
            }
        };

        String json = "{\"ORDER_ID\":\"PARCEL236974\",\"MID\":\"HYBADD50520222544592\",\"USER_TOKEN\":\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/closeOrder").with(new RequestPostProcessor() {

            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setParameter("X-Forwarded-For", "157.49.0.80,49.44.115.108");
                request.setParameter("User-Agent", "DummyUserAgent");
                request.setParameter("CHANNEL_ID", "WEB");
                request.setParameter("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308");
                return request;
            }
        }).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        CancelTransResponse closeResponse = objectMapper.readValue(response, CancelTransResponse.class);
        Assert.assertTrue(("F".equalsIgnoreCase(closeResponse.getStatus())));
    }

    @Test
    public void testFundOrderInvalidStatus() throws Exception {

        // Mock Platform+
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final AlipayServiceUrl api, final Class<T> clazz) {
                String alipayResponse = "";

                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_QUERY_BY_MERCHANT_TRANS_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.acquiring.order.queryByMerchantTransId\",\"clientId\":\"clientId\",\"reqMsgId\":\"e9324e4209404bcba94f06aada4446b1administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:20:42+05:30\"},\"body\":{\"resultInfo\":{\"resultMsg\":\"aqcPreGlobalUniqueDO[buyerUserId] is null, merchantTransId =PARCEL236974,merchantId =216820000000141015249\",\"resultStatus\":\"F\",\"resultCodeId\":\"00000020\",\"resultCode\":\"TARGET_NOT_FOUND\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_USER_ORDER_QUERY_BY_MERCHANT_REQUEST_ID)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.user.order.queryByMerchantRequestId\",\"clientId\":\"clientId\",\"reqMsgId\":\"2c62197ab5ca4e57b5ea8092d8a1c061administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:00+05:30\"},\"body\":{\"fundOrder\":{\"actualFundAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"modifiedTime\":\"2017-07-11T14:57:35+05:30\",\"taxAmount\":{\"currency\":\"INR\",\"value\":\"0\"},\"terminalType\":\"WEB\",\"fundOrderStatus\":\"CLOSE\",\"fundOrderId\":\"2017071120121481010100166012300020421\",\"extendInfo\":\"{\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"peonURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"theme\\\":\\\"merchant\\\",\\\"website\\\":\\\"retail\\\",\\\"promoCode\\\":\\\"\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL236974\\\",\\\"paytmMerchantId\\\":\\\"HYBADD50520222544592\\\",\\\"alipayMerchantId\\\":\\\"216820000000141015249\\\",\\\"ssoToken\\\":\\\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"requestType\\\":\\\"ADD_MONEY\\\",\\\"productCode\\\":\\\"51051000100000000002\\\",\\\"totalTxnAmount\\\":\\\"24\\\",\\\"merchantName\\\":\\\"HYB-AD\\\",\\\"topupAndPay\\\":false}\",\"chargeAmount\":{\"value\":\"0\",\"currency\":\"INR\"},\"paidTotalAmount\":{\"value\":\"2400\",\"currency\":\"INR\"},\"fundAmount\":{\"currency\":\"INR\",\"value\":\"2400\"},\"actorUserId\":\"216810000000139523012\",\"productCode\":\"51053000100000000011\",\"invokerId\":\"216820000000141015249\",\"payeeIdentifier\":{\"userId\":\"216810000000139523012\"},\"createdTime\":\"2017-07-11T14:55:57+05:30\",\"requestId\":\"PARCEL236974\",\"payeeAccountNo\":\"20070000000006436016\",\"payExpiryTime\":\"2017-07-14T14:55:57+05:30\",\"fundType\":\"TOPUP_MULTIPAY_MODE\"},\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCodeId\":\"00000000\"}}},\"signature\":\"no_signature\"}";
                }
                if (api.equals(AlipayServiceUrl.FUND_ORDER_CLOSE)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.fund.order.close\",\"clientId\":\"clientId\",\"reqMsgId\":\"9e9f698c42694a5e9082eff85b21af37administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-07-11T18:21:17+05:30\"},\"body\":{\"resultInfo\":{\"resultMsg\":\"fundOrder.status=SUCCESS\",\"resultCodeId\":\"12007202\",\"resultCode\":\"FUND_ORDER_STATUS_INVALID\",\"resultStatus\":\"F\"}}},\"signature\":\"no_signature\"}";
                }
                String responseString = null;
                try {
                    responseString = testResource.getObjectMapper().readTree(alipayResponse).get("response").toString();
                } catch (JsonProcessingException e) {
                    LOGGER.error("{}", e);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                final T response = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), responseString,
                        clazz);
                return response;
            }
        };

        String json = "{\"ORDER_ID\":\"PARCEL236974\",\"MID\":\"HYBADD50520222544592\",\"USER_TOKEN\":\"a5f7aa49-209f-4a58-a996-b5ca0f998c8b\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/closeOrder").with(new RequestPostProcessor() {

            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setParameter("X-Forwarded-For", "157.49.0.80,49.44.115.108");
                request.setParameter("User-Agent", "DummyUserAgent");
                request.setParameter("CHANNEL_ID", "WEB");
                request.setParameter("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308");
                return request;
            }
        }).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        CancelTransResponse closeResponse = objectMapper.readValue(response, CancelTransResponse.class);
        Assert.assertTrue(("F".equalsIgnoreCase(closeResponse.getStatus())));
    }

    @Test
    public void testCloseInvalidRequest() throws Exception {

        String json = "{\"ORDER_ID\":\"PARCEL236974\",\"MID\":\"HYBADD50520222544592\",\"USER_TOKEN\":\"\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/closeOrder").with(new RequestPostProcessor() {

            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setParameter("X-Forwarded-For", "157.49.0.80,49.44.115.108");
                request.setParameter("User-Agent", "DummyUserAgent");
                request.setParameter("CHANNEL_ID", "WEB");
                request.setParameter("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308");
                return request;
            }
        }).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        CancelTransResponse closeResponse = objectMapper.readValue(response, CancelTransResponse.class);
        Assert.assertTrue(("F".equalsIgnoreCase(closeResponse.getStatus())));
    }
}
