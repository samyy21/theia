package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_ON_BOARDING;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_SYSTEM_UNAVAILABLE;

public class PayMethodOnboardingUtil {
    public static boolean getOnboarding(EPayMethod payMethod, UserDetailsBiz userDetails) {

        if (null == userDetails || StringUtils.isBlank(userDetails.getPostpaidStatus())) {
            return false;
        }

        switch (payMethod) {
        case PAYTM_DIGITAL_CREDIT:
            boolean isOnboarding = TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_STATUS_WHITELISTED
                    .equals(userDetails.getPostpaidStatus());
            EventUtils.pushTheiaEvents(EventNameEnum.POST_PAID_ON_BOARDING, new ImmutablePair<>(POSTPAID_ON_BOARDING,
                    String.valueOf(isOnboarding)));
            return isOnboarding;
        default:
            return false;
        }
    }
}
