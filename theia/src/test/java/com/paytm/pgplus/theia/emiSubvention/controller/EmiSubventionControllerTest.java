package com.paytm.pgplus.theia.emiSubvention.controller;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.request.BanksRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.TenuresRequest;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponseBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponseBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponseBody;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.nativ.controller.AbstractNativeControllerTest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.processor.impl.BanksEmiSubventionProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.TenuresEmiSubventionProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.ValidateEmiSubventionProcessor;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import mockit.MockUp;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class EmiSubventionControllerTest extends AbstractNativeControllerTest {

    @Autowired
    @InjectMocks
    BanksEmiSubventionProcessor banksEmiSubventionProcessor;

    @Autowired
    @InjectMocks
    TenuresEmiSubventionProcessor tenuresEmiSubventionProcessor;

    @Autowired
    @InjectMocks
    ValidateEmiSubventionProcessor validateEmiSubventionProcessor;

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    @Mock
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Mock
    private ISubventionEmiService subventionEmiService;

    @Before
    public void setup() throws MappingServiceClientException, FacadeCheckedException {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        new MockUp<EmiSubventionUtils>() {
            @mockit.Mock
            public void setRequestHeader(TokenRequestHeader requestHeader) {
            }
        };
    }

    @BeforeClass
    public static void setSystemProperty() {
        System.setProperty("catalina.base", "");
    }

    @Test
    public void testBanksEmi() throws Exception {
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        when(subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased((EmiBanksRequest) any())).thenReturn(
                new BanksRequest());
        when(subventionEmiService.fetchEmiSubventionBanks(any())).thenReturn(new GenericEmiSubventionResponse<>());
        EmiBanksResponse response = new EmiBanksResponse();
        EmiBanksResponseBody body = new EmiBanksResponseBody();
        response.setBody(body);
        when(subventionEmiServiceHelper.prepareBanksEmiResponse(any(), any())).thenReturn(response);
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/banks")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void testBanksEmi1() throws Exception {
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/banks")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void testTenuresEmi() throws Exception {
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        when(subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased((EmiTenuresRequest) any())).thenReturn(
                new TenuresRequest());
        when(subventionEmiService.fetchEmiSubventionTenures(any())).thenReturn(new GenericEmiSubventionResponse<>());
        EmiTenuresResponse response = new EmiTenuresResponse();
        EmiTenuresResponseBody body = new EmiTenuresResponseBody();
        response.setBody(body);
        when(subventionEmiServiceHelper.prepareTenuresEmiResponse(any(), any())).thenReturn(response);
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"filters\":{\"bankCode\":\"bankCode\",\"cardType\":\"cardType\",\"userEligible\":\"true\",\"walletAmount\":\"400.0\"},\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/tenures")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    public void testTenuresEmi1() throws Exception {
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/tenures")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    public void testValidateEmi() throws Exception {
        doNothing().when(subventionEmiServiceHelper).validateRequestBody(any());
        ValidateEmiResponse validateEmiResponse = new ValidateEmiResponse();
        ValidateEmiResponseBody body = new ValidateEmiResponseBody();
        validateEmiResponse.setBody(body);
        when(subventionEmiServiceHelper.prepareValidateEmiResponse(any(), any(), any()))
                .thenReturn(validateEmiResponse);
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/validateEmi")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    public void testValidateEmi1() throws Exception {
        String json = "{\"head\": {\"txnToken\": \"txnToken\",\"tokenType\": \"SSO\",\"token\": \"523467\",\"requestTimestamp\": \"requestTimestamp\",\"workFlow\": \"workflow\",\"version\": \"v1\",\"channelId\": \"APP\",\"requestId\": \"requestId\"},\"body\": {\"items\": [],\"mid\": \"SCWMER90619707098260\",\"customerId\": \"custId\",\"subventionAmount\": 35.01,\"price\": 74.66,\"referenceId\": \"referenceId\"}}";
        mvc.perform(
                MockMvcRequestBuilders.post("/api/v1/emiSubvention/validateEmi")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

}