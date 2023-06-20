package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput.UserOwner;

/**
 * @author santoshchourasia
 *
 */
public interface IUserMappingService {

    UserInfo getUserData(String userId, UserOwner userOwner) throws MappingServiceClientException;

    MerchantExtendedInfoResponse getUserMerchantInfoResponse(String userId);
}
