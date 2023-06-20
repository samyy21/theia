package com.paytm.pgplus.theia.services.upiAccount.impl;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponse;
import com.paytm.pgplus.theia.services.upiAccount.helper.CheckUPIAccountServiceHelper;
import com.paytm.pgplus.theiacommon.exception.BaseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class checkUPIAccountServiceImplTest {

    @InjectMocks
    private checkUPIAccountServiceImpl checkUPIAccountService = new checkUPIAccountServiceImpl();

    @Mock
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Mock
    private CheckUPIAccountServiceHelper checkUPIAccountServiceHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCheckIfUPIAccountExistForUser() {
        CheckUPIAccountRequest request = new CheckUPIAccountRequest();
        when(checkUPIAccountServiceHelper.getUserIdFromOAuth(any())).thenReturn("userIdOAuth");
        when(checkUPIAccountServiceHelper.fetchUserProfileVpaV4(any(), any())).thenReturn(
                new GenericCoreResponseBean<UserProfileSarvatraV4>(new UserProfileSarvatraV4()));
        when(checkUPIAccountServiceHelper.validateAndProcessUpiResponse(any())).thenReturn(
                new CheckUPIAccountResponse());
        when(sarvatraVpaDetails.fetchUserProfileVpaV4(any())).thenReturn(
                new GenericCoreResponseBean<UserProfileSarvatraV4>(new UserProfileSarvatraV4()));
        checkUPIAccountService.checkIfUPIAccountExistForUser(request);

        exceptionRule.expect(BaseException.class);
        when(checkUPIAccountServiceHelper.getUserIdFromOAuth(any())).thenReturn(null);
        checkUPIAccountService.checkIfUPIAccountExistForUser(request);
    }
}