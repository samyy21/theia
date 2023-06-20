package com.paytm.pgplus.theia.controller.test;

import java.io.IOException;

import javax.ws.rs.core.Response;

import com.paytm.pgplus.cache.model.DynamicWrapperConfigList;
import com.paytm.pgplus.mappingserviceclient.service.impl.DynamicWrapperConfigImpl;
import com.paytm.pgplus.theia.test.util.TheiaTestUtil;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.paytm.pgplus.facade.user.helper.AuthenticationHelper;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.mappingserviceclient.application.MappingServiceClient;
import com.paytm.pgplus.mappingserviceclient.enums.MappingServiceUrl;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResource;

/**
 * @author kartik
 * @date 11-07-2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
@EnableWebMvc
public abstract class AbstractControllerTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext wac;

    protected MockMvc mockMvc;
    protected MockHttpServletRequest request;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractControllerTest.class);

    protected static TestResource testResource = TestResource.getInstance();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");

        // Mock OAuth
        new MockUp<AuthenticationHelper>() {
            @Mock
            public FetchUserDetailsResponse validateAndProcessResponseForFetchUserDetails(Response response) {
                String outhUserDetailsJson = "{\"accessToken\":{\"expiryTime\":\"1530614453093\",\"clientId\":\"1107195935\",\"scopes\":[\"wallet\"],\"userId\":1107195935},\"basicInfo\":{\"phone\":\"9997864013\",\"countryCode\":\"91\",\"firstName\":\"saurabh\",\"lastName\":\"yadav\",\"displayName\":\"saurabh\"},\"userAttributeInfo\":{},\"isKyc\":false}";
                UserDetailsV2 userDetailsV2 = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(),
                        outhUserDetailsJson, UserDetailsV2.class);
                UserDetails userDetails = new UserDetails(userDetailsV2);
                return new FetchUserDetailsResponse(userDetails);
            }
        };

        // Mock MS
        new MockUp<MappingServiceClient>() {
            @SuppressWarnings("incomplete-switch")
            @Mock
            public <T> T getData(final String redisKey, final String mappingID,
                    final MappingServiceUrl mappingServiceUrlEnum, final Class<T> clazz)
                    throws MappingServiceClientException {

                String mappingResponse = null;
                switch (mappingServiceUrlEnum) {
                case MERCHANT_MAPPING:
                    mappingResponse = "{\"paytmResultInfo\":{\"resultCode\": \"00000\",\"resultStatus\": \"S\",\"messaage\": \"Success\"},\"response\": {\"paytmId\": \"HYBADD50520222544592\",\"alipayId\": \"216820000000141015249\",\"officialName\": \"HYBADD\"}}";
                    break;
                case GET_USER_MAPPING_URL:
                    mappingResponse = "{\"paytmResultInfo\": {\"resultCode\": \"00000\",\"resultStatus\": \"S\",\"messaage\": \"Success\"},\"response\": {\"paytmId\": \"1107195935\",\"alipayId\": \"216810000000139523012\",\"paytmAccountId\": \"1107195935\",\"alipayAccountId\": \"20070000000006436016\"}}";
                    break;

                case GET_PAYTM_PROPERTY:
                    mappingResponse = "{\"name\": \"oauth.client.id\", \"value\": \"paytm-pg-client-staging\"}";
                    break;
                }
                if (mappingServiceUrlEnum.isWrapped()) {
                    try {
                        mappingResponse = testResource.getObjectMapper().readTree(mappingResponse).get("response")
                                .toString();
                    } catch (IOException e) {
                        throw new MappingServiceClientException("Exception while processing response string", e);
                    }
                }
                try {
                    return testResource.getObjectMapper().readValue(mappingResponse, clazz);
                } catch (IOException e) {
                    LOGGER.error("Unable to case mappingResponse string");
                    throw new MappingServiceClientException("Unable to case mappingResponse string");
                }
            }
        };
        new MockUp<DynamicWrapperConfigImpl>() {
            @Mock
            public DynamicWrapperConfigList getDynamicWrapperConfigs(String merchantId) throws Exception {
                return TheiaTestUtil.getDynamicWrapperConfig();
            }
        };
    }

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
}