package com.paytm.pgplus.theia.nativ;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;

public interface IOAuthHelper {

    public UserDetailsBiz validateSSOToken(final String token, InitiateTransactionRequestBody body);

    public UserDetailsBiz validateSSOToken(InitiateTransactionRequestBody body);

    public UserDetailsBiz validateSSOToken(final String token);

    public UserDetailsBiz validateSSOToken(final String token, String mid);

    UserDetailsV2 fetchUserDetailsViaPhoneOrUserId(String mobileNo, String userId) throws FacadeCheckedException;

    String fetchUserIdViaPhone(String mobileNo) throws FacadeCheckedException;
}
