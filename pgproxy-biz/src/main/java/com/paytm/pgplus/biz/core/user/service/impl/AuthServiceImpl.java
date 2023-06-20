package com.paytm.pgplus.biz.core.user.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.AuthUserInfoResponse;
import com.paytm.pgplus.biz.core.user.service.IAuthService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.OAuthServiceUrl;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service("authServiceImpl")
public class AuthServiceImpl implements IAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public GenericCoreResponseBean<AuthUserInfoResponse> fetchUserInfoByUserId(String userId,
            WorkFlowRequestBean workFlowBean) {

        if (StringUtils.isBlank(userId)) {
            return new GenericCoreResponseBean<>("UserId can't be null", ResponseConstants.SYSTEM_ERROR);
        }
        if (StringUtils.isBlank(workFlowBean.getOauthClientId())
                || StringUtils.isBlank(workFlowBean.getOauthSecretKey())) {
            return new GenericCoreResponseBean<>("oAuth client Key or secretKey can't be null",
                    ResponseConstants.SYSTEM_ERROR);
        }

        HttpRequestPayload<String> httpRequestPayload = AuthServiceHelper.generateRequestPayload(userId,
                workFlowBean.getOauthClientId(), workFlowBean.getOauthSecretKey());
        LogUtil.logPayload(ExternalEntity.OAUTH, httpRequestPayload.getTarget(), Type.REQUEST,
                String.valueOf(httpRequestPayload.getHeaders()));
        Response response;
        try {
            response = JerseyHttpClient.sendHttpGetRequest(httpRequestPayload);
            LogUtil.logPayload(ExternalEntity.OAUTH, httpRequestPayload.getTarget(), Type.RESPONSE,
                    String.valueOf(response.getEntity()));
            if (AuthServiceHelper.isValidResponse(response)) {
                final String jsonResponse = response.readEntity(String.class);
                if ((jsonResponse != null) && StringUtils.isNotBlank(jsonResponse)) {
                    AuthUserInfoResponse authUserInfoResponse = JsonMapper.mapJsonToObject(jsonResponse,
                            AuthUserInfoResponse.class);
                    return new GenericCoreResponseBean<AuthUserInfoResponse>(authUserInfoResponse);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get Basic UserInfo from OAuth");
        }
        return new GenericCoreResponseBean<>("FAILED");
    }
}
