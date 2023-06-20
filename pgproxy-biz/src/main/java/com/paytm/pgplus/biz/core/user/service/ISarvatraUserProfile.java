package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;

/**
 * Created by charuaggarwal on 28/9/17.
 */
public interface ISarvatraUserProfile {

    GenericCoreResponseBean<UserProfileSarvatra> fetchUserProfileVpa(FetchUserPaytmVpaRequest request);

    GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserProfileVpaV4(FetchUserPaytmVpaRequest request);
}
