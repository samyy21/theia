package com.paytm.pgplus.biz.core.user.service.impl;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.user.services.ISarvatraVpaDetails;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charuaggarwal on 28/9/17. updated by santosh chourasia
 */
@Service("userProfileSarvatra")
public class UserProfileSarvatraImpl implements ISarvatraUserProfile {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileSarvatraImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(UserProfileSarvatraImpl.class);

    @Autowired
    @Qualifier("sarvatraVpaDetails")
    ISarvatraVpaDetails sarvatraVpaDetails;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Override
    public GenericCoreResponseBean<UserProfileSarvatra> fetchUserProfileVpa(
            FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest) {

        String newFetchVPA = ConfigurationUtil.getTheiaProperty("new.fetch.vpa.api", "FALSE");

        if (fetchUserPaytmVpaRequest != null) {
            fetchUserPaytmVpaRequest.setRequestId(RequestIdGenerator.generateRequestId());
        }

        if (StringUtils.isBlank(fetchUserPaytmVpaRequest.getUserToken())) {
            LOGGER.error("Token is blank");
            return new GenericCoreResponseBean<UserProfileSarvatra>("InvalidTokenProvided",
                    ResponseConstants.SYSTEM_ERROR);
        }

        UserProfileSarvatra fetchUserPaytmVpaResponse;
        try {
            if (StringUtils.equals(newFetchVPA, "TRUE")) {
                LOGGER.debug("Calling sarvatra V2 with requestId : {}", fetchUserPaytmVpaRequest);
                fetchUserPaytmVpaResponse = sarvatraVpaDetails.fetchSarvatraDetailsV2(fetchUserPaytmVpaRequest);
            } else {
                LOGGER.debug("Calling sarvatra V1 with requestId : {}", fetchUserPaytmVpaRequest.getRequestId());
                fetchUserPaytmVpaResponse = sarvatraVpaDetails.fetchSarvatraDetails(fetchUserPaytmVpaRequest);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching Paytm Vpa", e);
            return new GenericCoreResponseBean<UserProfileSarvatra>("Could not Fetch User Paytm Vpa Handles",
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (StringUtils.isBlank(fetchUserPaytmVpaResponse.getStatus())) {
            return new GenericCoreResponseBean<UserProfileSarvatra>("Failure response received");
        }
        return new GenericCoreResponseBean<UserProfileSarvatra>(fetchUserPaytmVpaResponse);
    }

    @Override
    public GenericCoreResponseBean<UserProfileSarvatraV4> fetchUserProfileVpaV4(
            FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest) {

        UserProfileSarvatraV4 fetchUserPaytmVpaResponse;
        try {
            EXT_LOGGER.customInfo("Calling sarvatra V4 with requestId : {}", fetchUserPaytmVpaRequest);
            fetchUserPaytmVpaResponse = sarvatraVpaDetails.fetchSarvatraDetailsV4(fetchUserPaytmVpaRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", fetchUserPaytmVpaResponse.getRespMessage());
                responseMap.put("RESPONSE_MESSAGE", fetchUserPaytmVpaResponse.getStatus());
                statsDUtils.pushResponse("FETCH_UPI_USER_PROFILE_V4", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "FETCH_UPI_USER_PROFILE_V4" + "to grafana",
                        exception);
            }
        } catch (Exception e) {
            // LOGGER.error("Exception occured while fetching Paytm Vpa V4", e);
            LOGGER.error("Exception occured while fetching Paytm Vpa V4", ExceptionLogUtils.limitLengthOfStackTrace(e));
            return new GenericCoreResponseBean<UserProfileSarvatraV4>("Could not Fetch User Paytm Vpa Handles",
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (StringUtils.isBlank(fetchUserPaytmVpaResponse.getStatus())) {
            return new GenericCoreResponseBean<UserProfileSarvatraV4>("Failure response received");
        }
        checkUpiLiteStatusAndUpdate(fetchUserPaytmVpaResponse);
        return new GenericCoreResponseBean<UserProfileSarvatraV4>(fetchUserPaytmVpaResponse);

    }

    private void checkUpiLiteStatusAndUpdate(UserProfileSarvatraV4 fetchUserPaytmVpaResponse) {
        try {
            if (fetchUserPaytmVpaResponse.getRespDetails() != null
                    && fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail() != null
                    && fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail().getLrnDetails() != null) {
                String status = fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail().getLrnDetails()
                        .getStatus();
                String[] allowedStatus = ConfigurationUtil.getTheiaProperty(
                        TheiaConstant.RequestParams.UPI_LITE_ALLOWED_STATUS).split(",");
                boolean isStatusAllowed = Arrays.asList(allowedStatus).contains(status);
                if (!isStatusAllowed)
                    fetchUserPaytmVpaResponse.getRespDetails().getProfileDetail().setLrnDetails(null);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check upi lite status ", e);
        }
    }
}