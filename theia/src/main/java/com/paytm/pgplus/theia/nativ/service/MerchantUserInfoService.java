package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.FetchMerchantInfoException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS;

@Service("merchantUserInfoService")
public class MerchantUserInfoService implements
        IMerchantUserInfoService<MerchantUserInfoServiceRequest, MerchantUserInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantUserInfoService.class);

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public MerchantUserInfoResponse fetchMerchantUserInfo(MerchantUserInfoServiceRequest request) {
        MerchantUserInfoResponseBody body = new MerchantUserInfoResponseBody();

        MerchantBussinessLogoInfo merchantBusinessLogoInfo = getMerchantInfo(request);

        UserInfo userInfo = getUserInfo(request);

        if (null != merchantBusinessLogoInfo
                && merchantBusinessLogoInfo.getResponse() != null
                && ResultCode.SUCCESS.getResultStatus()
                        .equals(merchantBusinessLogoInfo.getResponse().getResultStatus())) {
            body = convertToMerchantUserInfoResponse(merchantBusinessLogoInfo, userInfo, body);
        } else {
            throw FetchMerchantInfoException.getException();
        }
        return new MerchantUserInfoResponse(new ResponseHeader(), body);
    }

    private MerchantBussinessLogoInfo getMerchantInfo(MerchantUserInfoServiceRequest request) {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = null;

        String pickLogoFromNewLocation = ConfigurationUtil.getProperty("useNewImagePathForMerchant", "false");
        try {
            if ("true".equalsIgnoreCase(pickLogoFromNewLocation)) {
                merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMidV2(request.getMid(),
                        true);
            } else {
                merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMid(request.getMid());
            }
            LOGGER.info("Merchant Business Info from MS: {} ", merchantBussinessLogoInfo);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Error in MerchantUserInfoService getMerchantInfo" + e);
        }
        return merchantBussinessLogoInfo;
    }

    private UserInfo getUserInfo(MerchantUserInfoServiceRequest request) {
        UserInfo userInfo = null;
        if (TokenType.TXN_TOKEN.name().equals(request.getTokenType())) {

            NativeInitiateRequest initiateTxnBody = null;
            initiateTxnBody = (NativeInitiateRequest) theiaSessionRedisUtil.hget(request.getTxnToken(), "orderDetail");

            if (initiateTxnBody == null) {
                if (!ff4jUtils.isFeatureEnabledOnMid(MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL,
                        THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
                    LOGGER.info("operation on static redis, {}", MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL);
                    initiateTxnBody = (NativeInitiateRequest) theiaTransactionalRedisUtil.hget(request.getTxnToken(),
                            "orderDetail");

                }
            }

            if (initiateTxnBody != null && initiateTxnBody.getInitiateTxnReq() != null
                    && initiateTxnBody.getInitiateTxnReq().getBody().getUserInfo() != null) {
                LOGGER.info("UserInfo found in InitiateTxnReqBody");
                userInfo = initiateTxnBody.getInitiateTxnReq().getBody().getUserInfo();
            }
        }
        return userInfo;
    }

    private MerchantUserInfoResponseBody convertToMerchantUserInfoResponse(
            MerchantBussinessLogoInfo merchantBusinessLogoInfo, UserInfo userInfo, MerchantUserInfoResponseBody body) {
        MerchantInfoResp merchantInfoResp = getMerchantInfoResp(merchantBusinessLogoInfo);
        UserInfoResp userInfoResp = getUserInfoResp(userInfo);

        body.setMerchantInfoResp(merchantInfoResp);
        body.setUserInfoResp(userInfoResp);

        return body;
    }

    private MerchantInfoResp getMerchantInfoResp(MerchantBussinessLogoInfo merchantBusinessLogoInfo) {

        MerchantInfoResp merchantInfoResp = new MerchantInfoResp();

        merchantInfoResp.setMerBusName(merchantBusinessLogoInfo.getMerchantBusinessName());
        merchantInfoResp.setMerDispname(merchantBusinessLogoInfo.getMerchantDisplayName());
        merchantInfoResp.setMerLogoUrl(merchantBusinessLogoInfo.getMerchantImageName());
        boolean p2pDisabled = merchantPreferenceService.isP2pDisabled(merchantBusinessLogoInfo.getPaytmMid(), false);
        merchantInfoResp.setP2pDisabled(p2pDisabled);

        return merchantInfoResp;
    }

    private UserInfoResp getUserInfoResp(UserInfo userInfo) {

        UserInfoResp userInfoResp = new UserInfoResp();

        userInfoResp.setUserInfo(userInfo);
        return userInfoResp;
    }

}