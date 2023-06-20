package com.paytm.pgplus.biz.core.user.service;

import java.util.List;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
public interface ISavedCards {

    GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByUserId(String userId);

    GenericCoreResponseBean<UserDetailsBiz> fetchSavedCardsByCardId(String cardId, UserDetailsBiz userDetails,
            WorkFlowRequestBean flowRequestBean);

    GenericCoreResponseBean<UserDetailsBiz> cacheCardDetails(WorkFlowRequestBean flowRequestBean,
            UserDetailsBiz userDetails, String transactionId);

    GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByMidCustIdUserId(String mId, String custId, String userId);

}