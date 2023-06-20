package com.paytm.pgplus.biz.core.user.service.impl;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput.UserOwner;

/**
 * @author santosh
 *
 */
@Service("userMappingServiceImpl")
public class UserMappingServiceImpl implements IUserMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMappingServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(UserMappingServiceImpl.class);

    @Autowired
    @Qualifier("userMappingService")
    IUserMapping userMapping;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    IMerchantDataService merchantDataService;

    public UserInfo getUserData(String userId, UserOwner userOwner) throws MappingServiceClientException {
        UserInfo data = null;

        switch (userOwner) {
        case ALIPAY:
            data = userMapping.getAlipayUserMapping(userId, userOwner.name());
            break;
        case PAYTM:
            data = userMapping.getPaytmUserMapping(userId, userOwner.name());
            break;
        }

        EXT_LOGGER.customInfo("Mapping response - UserInfo :: {} for user-owner :: {}", data, userOwner.name());
        LOGGER.debug("User mapping recieved from redis or mapping service is:{}", data);
        return data;
    }

    public MerchantExtendedInfoResponse getUserMerchantInfoResponse(String userId) {
        if (StringUtils.isBlank(userId)) {
            LOGGER.error("userId is blank!");
            return null;
        }
        MerchantExtendedInfoResponse response = null;
        try {
            response = merchantDataService.getUserMerchantInfoResponse(userId);
            EXT_LOGGER.customInfo("Mapping response - MerchantExtendedInfoResponse :: {}", response);
            if (response == null || response.getExtendedInfo() == null) {
                LOGGER.error("response or response.extendInfo() is null from mapping-service for userId {}", userId);
                return null;
            }
            if (!StringUtils.equals(userId, response.getExtendedInfo().getUserId())) {
                LOGGER.error("mapping-service response userId:{} does not match with request userId:{}", response
                        .getExtendedInfo().getUserId(), userId);
                return null;
            }
        } catch (Exception e) {
            if (e instanceof MappingServiceClientException) {
                try {
                    MappingServiceClientException exception = (MappingServiceClientException) e;

                    if (exception != null && StringUtils.isNotBlank(exception.getErrorMessage())
                            && exception.getErrorMessage().contains("EMPTY_RESPONSE")) {

                        LOGGER.warn("something went wrong while fetching merchantDetails for userId {},  {}", userId, e);

                    } else {
                        LOGGER.error("Exception occurred while fetching merchantDetails for userId {},  {}", userId, e);
                    }
                } catch (Exception e1) {
                    LOGGER.error("Exception occurred while fetching merchantDetails for userId {},  {}", userId, e);
                }

            } else {
                LOGGER.error("Exception occurred while fetching merchantDetails for userId {},  {}", userId, e);
            }

        }
        return response;
    }

}
