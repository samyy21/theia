package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.biz.core.model.oauth.AuthUserInfoResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface IAuthService {
    GenericCoreResponseBean<AuthUserInfoResponse> fetchUserInfoByUserId(String userId,
            final WorkFlowRequestBean workFlowBean);
}
