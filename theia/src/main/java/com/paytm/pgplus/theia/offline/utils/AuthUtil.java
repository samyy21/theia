package com.paytm.pgplus.theia.offline.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    public GenericCoreResponseBean<UserDetailsBiz> fetchUserDetails(String ssoToken) {
        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String clientSecret = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        WorkFlowTransactionBean workFlowTransactionBean = new WorkFlowTransactionBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setOauthClientId(clientId);
        workFlowRequestBean.setOauthSecretKey(clientSecret);
        workFlowTransactionBean.setWorkFlowBean(workFlowRequestBean);
        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransactionBean,
                ssoToken, false);
        if (!userDetails.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
        }
        return userDetails;
    }

    public boolean isUserValid(String ssoToken) {
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz = fetchUserDetails(ssoToken);
        if (!userDetailsBiz.isSuccessfullyProcessed() || userDetailsBiz.getResponse() == null) {
            return false;
        }
        return true;
    }
}
