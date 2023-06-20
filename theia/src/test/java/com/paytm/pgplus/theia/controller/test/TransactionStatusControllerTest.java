package com.paytm.pgplus.theia.controller.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.paytm.pgplus.theia.controllers.TransactionStatusController;
import com.paytm.pgplus.theia.interceptors.SignatureInterceptor;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class TransactionStatusControllerTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    TransactionStatusController controller;

    @Autowired
    SignatureInterceptor interceptor;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    private MockMvc mockMvc;
    private MockHttpServletResponse response;

    @SuppressWarnings("unused")
    private MockHttpServletRequest request;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        transactionCacheUtils.putTransInfoInCache("20170613111212800110166869000008982", "SCWMER90619707098260", "abc",
                true);
    }

    @Test
    public void testTxnStatusV1() throws Exception {
        MvcResult mvcresult = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/transactionStatus").with(new RequestPostProcessor() {

                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setParameter("paymentMode", "CC");
                        request.setParameter("merchantId", "SCWMER90619707098260");
                        request.setParameter("cashierRequestId",
                                "clientIdd8ed1a36904348daa21296530790d950197nodnb16146");
                        request.setParameter("transId", "20170613111212800110166869000008982");
                        request.setParameter("signature",
                                "86eca4baa3fda4e648ac2d811f9257cb9e2eddf7cd5ce6c720fb7257d97179fd");
                        return request;
                    }
                })).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        response = mvcresult.getResponse();
        Assert.assertTrue(HttpStatus.OK.value() == response.getStatus());
    }

    @Test
    public void testTxnStatus() throws Exception {
        MvcResult mvcresult = mockMvc
                .perform(MockMvcRequestBuilders.get("/transactionStatus").with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setParameter("paymentMode", "CC");
                        request.setParameter("merchantId", "SCWMER90619707098260");
                        request.setParameter("cashierRequestId",
                                "clientIdd8ed1a36904348daa21296530790d950197nodnb16146");
                        request.setParameter("transId", "20170613111212800110166869000008982");
                        request.setParameter("signature",
                                "86eca4baa3fda4e648ac2d811f9257cb9e2eddf7cd5ce6c720fb7257d97179fd");
                        return request;
                    }
                })).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        response = mvcresult.getResponse();
        Assert.assertTrue(HttpStatus.OK.value() == response.getStatus());
    }

}
