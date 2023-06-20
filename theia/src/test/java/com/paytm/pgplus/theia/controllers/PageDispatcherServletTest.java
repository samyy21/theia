package com.paytm.pgplus.theia.controllers;

import org.glassfish.grizzly.servlet.DispatchedHttpServletResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import wiremock.org.apache.http.util.Asserts;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * @createdOn 05/07/21
 * @author Aman Shevkar.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class PageDispatcherServletTest {

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    RequestDispatcher requestDispatcher;

    @InjectMocks
    PageDispatcherServlet pageDispatcherServlet;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        transactionCacheUtils.putTransInfoInCache("20170613111212800110166869000008982", "SCWMER90619707098260", "abc",
                true);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Test
    public void testDoGet() throws Exception {
        request.setPathInfo("Path_Information");
        pageDispatcherServlet.doGet(request, response);
    }

    @Test
    public void testDoGet_NoForbiddenCharacters() throws Exception {
        // To test functionality when no forbidden characters exists.
        request.setPathInfo("Path_Information");
        String[] value = { "value1 " };
        request.setParameter("Protocol", value);
        pageDispatcherServlet.doGet(request, response);
    }

    @Test
    public void testDoGet_ForbiddenCharacterExists() throws Exception {
        // To test functionality when the forbidden characters exists.
        request.setPathInfo("Path_Information");
        String[] value = { "< ", "'" };
        request.setParameter("Protocol", value);
        pageDispatcherServlet.doGet(request, response);
    }

    @Test(expected = NullPointerException.class)
    public void testDoGet_Exception2() throws Exception {
        // To test functionality when exception occurs while forwarding request.
        PageDispatcherServlet pgx = new PageDispatcherServlet();
        pgx.doGet(httpServletRequest, response);

    }

}
