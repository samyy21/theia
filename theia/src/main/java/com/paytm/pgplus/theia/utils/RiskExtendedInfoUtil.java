/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.core.merchant.service.IMerchantMappingService;
import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ankit.singhal
 */

@Component
public class RiskExtendedInfoUtil {

    @Autowired
    @Qualifier("userMappingServiceImpl")
    IUserMappingService userMappingService;

    @Autowired
    @Qualifier("merchantMappingServiceImpl")
    IMerchantMappingService merchantMappingService;

    public static final Logger LOGGER = LoggerFactory.getLogger(RiskExtendedInfoUtil.class);

    public Map<String, String> selectRiskExtendedInfo(final LoginInfo loginInfo) {

        Map<String, String> riskExtendedInfo = new HashMap<>();

        if (loginInfo != null && loginInfo.getUser() != null) {
            riskExtendedInfo.put(ExtendedInfoPay.CUSTOMER_TYPE, String.valueOf(loginInfo.getUser().isKYC()));
        }
        return riskExtendedInfo;
    }

    public void setMerchantUserIdInRiskExtendInfo(String paytmMerchantId, Map<String, String> riskExtendInfo) {
        UserInfo userInfo = null;
        LOGGER.debug("Set merchant-user-id in risk-extendinfo for merchant: {}", paytmMerchantId);
        if (StringUtils.isNotBlank(paytmMerchantId)) {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantMappingService
                    .getMerchantInfoResponse(paytmMerchantId);
            if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null
                    && merchantExtendedInfoResponse.getExtendedInfo().getUserId() != null) {
                String userId = merchantExtendedInfoResponse.getExtendedInfo().getUserId();
                try {
                    userInfo = userMappingService.getUserData(userId, UserDataMappingInput.UserOwner.PAYTM);
                } catch (Exception e) {
                    LOGGER.info("Error Occured while fetching user-info from mapping-service for userId- {}", userId);
                }

            }
        }

        // As mentioned on Jira-12257
        if (userInfo != null && StringUtils.isNotBlank(userInfo.getAlipayId())) {
            riskExtendInfo.put("userMerchant", userInfo.getAlipayId());
        } else {
            riskExtendInfo.put("userMerchant", "");
        }
    }
}
