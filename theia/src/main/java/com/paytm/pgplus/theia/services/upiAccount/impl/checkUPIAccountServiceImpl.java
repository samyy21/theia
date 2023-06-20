package com.paytm.pgplus.theia.services.upiAccount.impl;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponse;
import com.paytm.pgplus.theia.services.upiAccount.CheckUPIAccountService;
import com.paytm.pgplus.theia.services.upiAccount.helper.CheckUPIAccountServiceHelper;
import com.paytm.pgplus.theiacommon.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class checkUPIAccountServiceImpl implements CheckUPIAccountService {

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    private CheckUPIAccountServiceHelper checkUPIAccountServiceHelper;

    @Override
    public CheckUPIAccountResponse checkIfUPIAccountExistForUser(CheckUPIAccountRequest checkUPIAccountRequest) {

        // validate request
        checkUPIAccountServiceHelper.validateRequest(checkUPIAccountRequest);
        return getUpiAccountResponse(checkUPIAccountRequest);
    }

    private CheckUPIAccountResponse getUpiAccountResponse(CheckUPIAccountRequest checkUPIAccountRequest) {
        // getUserId from oauth team
        String userIdOAuth = checkUPIAccountServiceHelper.getUserIdFromOAuth(checkUPIAccountRequest);

        if (StringUtils.isBlank(userIdOAuth)) {
            throw BaseException.getException("Unable to retrieve userId for this mobile number");
        }
        GenericCoreResponseBean<UserProfileSarvatraV4> fetchUpiProfileResponse = null;

        fetchUpiProfileResponse = checkUPIAccountServiceHelper.fetchUserProfileVpaV4(checkUPIAccountRequest,
                userIdOAuth);

        CheckUPIAccountResponse response = checkUPIAccountServiceHelper
                .validateAndProcessUpiResponse(fetchUpiProfileResponse);

        return response;
    }
}
