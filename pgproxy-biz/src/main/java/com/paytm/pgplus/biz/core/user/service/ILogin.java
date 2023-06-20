/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginRequestBizBean;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

import java.util.Map;

public interface ILogin {

    /*
     * GenericCoreResponseBean<UserDetailsBiz> fetchUserDetails(final String
     * token, boolean fetchSavedCards, String clientId, String clientSecret);
     */

    GenericCoreResponseBean<VerifyLoginResponseBizBean> verfifyLogin(VerifyLoginRequestBizBean verifyLoginReqBean);

    GenericCoreResponseBean<UserDetailsBiz> fetchUserDetails(String token, boolean fetchSavedCards,
            WorkFlowRequestBean workFlowBean);

    public void fetchSavedCardDetails(UserDetailsBiz userDetails, String mId, String custId);;

    GenericCoreResponseBean<UserDetailsBiz> fetchUserDetailsNoSavedCards(WorkFlowRequestBean requestBean, String token);

    void fetchSavedCards(WorkFlowRequestBean requestBean, UserDetailsBiz userDetailsBiz, boolean fetchSavedCards);

    public void updatePostpaidStatusAndCCEnabledFlag(String token, boolean isPostpaidOnboardingSupported,
            String clientId, String clientSecret, UserDetailsBiz userDetails) throws FacadeCheckedException;

    public Map<String, Map<String, String>> fetchUserTypeAttributesDetails(String token, String userType,
            String clientId, String clientSecret) throws FacadeCheckedException;
}
