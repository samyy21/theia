/**
 *
 */
package com.paytm.pgplus.biz.core.user.service.utils;

import com.paytm.pgplus.biz.core.model.oauth.OAuthTokenBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.exception.FacadeToBIzMappingException;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.facade.user.models.OAuthToken;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.request.FetchAllTokensRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.request.ValidateAuthCodeRequest;
import com.paytm.pgplus.facade.user.models.response.FetchAllTokensResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.PAYTM_TOKEN_SCOPE;
import static com.paytm.pgplus.biz.utils.BizConstant.WALLET_TOKEN_SCOPE;

/**
 * @author namanjain
 *
 */
@Service
public class OAuthHelperServiceImpl {
    @Autowired
    @Qualifier("mapUtilsBiz")
    MappingUtil mappingUtil;

    public static ValidateAuthCodeRequest createOAuthRequestBean(final String oAuthCode, final String clientId,
            final String secretKey) {
        return new ValidateAuthCodeRequest(oAuthCode, clientId, secretKey);
    }

    public static FetchAllTokensRequest createfetchAllTokensRequest(final String accessToken, final String clientId,
            final String secretKey) {
        return new FetchAllTokensRequest(accessToken, clientId, secretKey);
    }

    @Deprecated
    public static FetchUserDetailsRequest createFetchUserDetailsRequest(final String token) {
        return new FetchUserDetailsRequest(token);
    }

    public static FetchUserDetailsRequest createFetchUserDetailsRequest(final String token, final String clientId,
            final String clientSecret, String mid) {
        return new FetchUserDetailsRequest(token, clientId, clientSecret, mid);
    }

    public VerifyLoginResponseBizBean createVerifyLoginResponseBean(final OAuthToken accessToken,
            final FetchAllTokensResponse tokens, final UserDetails userDetails) throws FacadeToBIzMappingException,
            MappingServiceClientException {
        final VerifyLoginResponseBizBean verifyLoginResponseBizBean = new VerifyLoginResponseBizBean();
        final OAuthTokenBiz accessTokenBiz = mappingUtil.mapOAuthToken(accessToken);
        final OAuthTokenBiz paytmToken = mappingUtil.mapOAuthToken(tokens.getToken(PAYTM_TOKEN_SCOPE));
        final OAuthTokenBiz walletToken = mappingUtil.mapOAuthToken(tokens.getToken(WALLET_TOKEN_SCOPE));
        final UserDetailsBiz userDetailsBiz = mappingUtil.mapUserDetails(userDetails);

        verifyLoginResponseBizBean.setAccessToken(accessTokenBiz);
        verifyLoginResponseBizBean.setPaytmToken(paytmToken);
        verifyLoginResponseBizBean.setWalletToken(walletToken);
        verifyLoginResponseBizBean.setUserDetails(userDetailsBiz);

        return verifyLoginResponseBizBean;
    }

}
