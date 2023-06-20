package com.paytm.pgplus.theia.session.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.SarvatraVPAInfo;
import com.paytm.pgplus.theia.sessiondata.SarvatraVPAMapInfo;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;

/**
 * @author vivek kumar
 * @date 19-Dec-2017
 */
@Component("sarvtraVPASessionUtil")
public class SarvatraVPASessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SarvatraVPASessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    private static final String MP_ADD_MONEY_MID = "MP.ADD.MONEY.MID";

    public void setSarvatraVpaInfoInSession(PaymentRequestBean requestBean, WorkFlowResponseBean workFlowResponseBean) {

        SarvatraVPAMapInfo sarvatraVPAMapInfo = theiaSessionDataService.getSarvatraVPAInfoFromSession(
                requestBean.getRequest(), true);

        if (workFlowResponseBean.getSarvatraUserProfile() != null) {

            UserProfileSarvatra sarvatraUserprofile = workFlowResponseBean.getSarvatraUserProfile();
            SarvatraVPAInfo sarvatraVpainfo = new SarvatraVPAInfo();

            Map<String, String> mpinInfos = new HashMap<>();
            Map<String, PaytmBanksVpaDefaultDebitCredit> bankInfos = new HashMap<>();
            for (SarvatraVpaDetails sarvatraVpaDetails : sarvatraUserprofile.getResponse().getVpaDetails()) {
                if (checkInvalidVPA(sarvatraVpaDetails.getDefaultDebit())) {
                    continue;
                }
                String key = sarvatraVpaDetails.getName();
                sarvatraVpainfo.setName(sarvatraVpaDetails.getName());
                PaytmBanksVpaDefaultDebitCredit defaultDebit = new PaytmBanksVpaDefaultDebitCredit();

                defaultDebit.setCredsAllowed(sarvatraVpaDetails.getDefaultDebit().getCredsAllowed());
                defaultDebit.setMbeba(sarvatraVpaDetails.getDefaultDebit().getMbeba());
                defaultDebit.setAccount(getStringLastNDigit(sarvatraVpaDetails.getDefaultDebit().getAccount(), 4));
                defaultDebit.setBank(sarvatraVpaDetails.getDefaultDebit().getBank());
                sarvatraVpainfo.setDefaultDebit(defaultDebit);
                sarvatraVpainfo.setPrimary(sarvatraVpaDetails.isPrimary());
                String value = null;
                try {
                    value = JsonMapper.mapObjectToJson(sarvatraVpainfo);
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Exception occured while converting object to json " + e);
                }
                mpinInfos.put(key, value);
                bankInfos.put(key, defaultDebit);
            }
            sarvatraVPAMapInfo.setSarvatraVpaMapInfo(mpinInfos);

            if (workFlowResponseBean.isMerchantUpiPushExpressEnabled()) {
                sarvatraVPAMapInfo.setMerchantDetails(merchantDataUtil.getMerchantDetails(requestBean.getMid(), true));
            } else if (workFlowResponseBean.isMerchantUpiPushEnabled()) {
                sarvatraVPAMapInfo.setMerchantDetails(merchantDataUtil.getMerchantDetails(requestBean.getMid(), false));
            }

            if (workFlowResponseBean.isAddUpiPushExpressEnabled()) {
                sarvatraVPAMapInfo.setAddMoneyMerchantDetails(merchantDataUtil.getMerchantDetails(
                        ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), true));
            } else if (workFlowResponseBean.isAddUpiPushEnabled()) {
                sarvatraVPAMapInfo.setAddMoneyMerchantDetails(merchantDataUtil.getMerchantDetails(
                        ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), false));
            }
            sarvatraVPAMapInfo.setUserProfileSarvatra(workFlowResponseBean.getSarvatraUserProfile());
            sarvatraVPAMapInfo.setBankInfo(bankInfos);
        }
    }

    private String getStringLastNDigit(String inputString, int subStringLength) {
        int length = inputString.length();
        if (length <= subStringLength) {
            return inputString;
        }
        int startIndex = length - subStringLength;
        return inputString.substring(startIndex);
    }

    private boolean checkInvalidVPA(PaytmBanksVpaDefaultDebitCredit defaultDebit) {
        if (null == defaultDebit) {
            return true;
        }

        return defaultDebit.isInvalidVpa();
    }

}
