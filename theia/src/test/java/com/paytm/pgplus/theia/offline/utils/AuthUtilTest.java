package com.paytm.pgplus.theia.offline.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

import mockit.MockUp;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthUtilTest {

    @InjectMocks
    private AuthUtil authUtil = new AuthUtil();

    @Mock
    private IConfigurationDataService configurationDataService;

    @Mock
    private WorkFlowHelper workFlowHelper;

    @Mock
    private WorkFlowRequestBean workFlowRequestBean;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testFetchUserDetails() {
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz = new GenericCoreResponseBean<UserDetailsBiz>(
                "responseMessage");
        when(workFlowHelper.fetchUserDetails(any(), any(), anyBoolean())).thenReturn(userDetailsBiz);
        authUtil.fetchUserDetails("ssoToken");

        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz1 = new GenericCoreResponseBean<UserDetailsBiz>(
                new UserDetailsBiz());
        when(workFlowHelper.fetchUserDetails(any(), any(), anyBoolean())).thenReturn(userDetailsBiz1);
        authUtil.fetchUserDetails("ssoToken");
    }

    @Test
    public void testIsUserValid() {
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz = new GenericCoreResponseBean<UserDetailsBiz>(
                "responseMessage");
        when(workFlowHelper.fetchUserDetails(any(), any(), anyBoolean())).thenReturn(userDetailsBiz);
        authUtil.isUserValid("ssoToken");

        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz1 = new GenericCoreResponseBean<UserDetailsBiz>(
                new UserDetailsBiz());
        when(workFlowHelper.fetchUserDetails(any(), any(), anyBoolean())).thenReturn(userDetailsBiz1);
        authUtil.isUserValid("ssoToken");
    }
}