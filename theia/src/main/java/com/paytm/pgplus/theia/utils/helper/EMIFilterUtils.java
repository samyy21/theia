package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.biz.core.model.request.EMIChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.cache.model.MBIDLimitMappingDetails;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMbidLimitDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * @author Naman
 * @date 22/02/18
 */

@Service("emiFilterUtils")
public class EMIFilterUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMIFilterUtils.class);

    @Autowired
    @Qualifier("mbidLimitDataServiceImpl")
    IMbidLimitDataService mbidLimitDataService;

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    IBankInfoDataService bankInfoDataService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    public boolean filterFor0CostEMI(final HttpServletRequest httpServletRequest, final String emiOptionRequestParam,
            final PayMethodViewsBiz payMethod) {

        if (StringUtils.isNotBlank(emiOptionRequestParam)
                && StringUtils.startsWithIgnoreCase(emiOptionRequestParam, TheiaConstant.ExtraConstants.ZERO_COST_EMI)) {

            String[] bank_RecordId = StringUtils.substringAfter(emiOptionRequestParam, ":").split("_");

            if (bank_RecordId.length < 2 || StringUtils.isBlank(bank_RecordId[0])
                    || StringUtils.isBlank(bank_RecordId[1])) {
                return false;
            }

            final String bank = bank_RecordId[0];

            final String id = bank_RecordId[1];

            try {

                final BankMasterDetails bankMasterDetails = bankInfoDataService
                        .getBankListInfoDataFromBankIds(Collections.singletonList(bank)).getBankMasterDetailsList()
                        .get(0);

                final MBIDLimitMappingDetails mbidLimitMappingDetails = mbidLimitDataService.getMbidLimitInfoData(id);

                if (mbidLimitMappingDetails == null || bankMasterDetails == null) {
                    throw new MappingServiceClientException("Null data fetched");
                }

                filterEMI(bankMasterDetails.getBankCode(), mbidLimitMappingDetails, payMethod);

                if (httpServletRequest != null)
                    disableLogin(httpServletRequest);

                if (payMethod == null || payMethod.getPayChannelOptionViews() == null
                        || payMethod.getPayChannelOptionViews().isEmpty()
                        || payMethod.getPayChannelOptionViews().get(0) == null
                        || payMethod.getPayChannelOptionViews().get(0).getEmiChannelInfos() == null
                        || payMethod.getPayChannelOptionViews().get(0).getEmiChannelInfos().isEmpty()) {
                    return false;
                }

                return true;

            } catch (Exception e) {
                LOGGER.error(
                        "Exception occurred while fetching Data from Mapping Service for BankID {} EMI ID {} and reason is {} ::",
                        bank, id, e);
            }

        }

        return false;
    }

    void filterEMI(final String bankCode, final MBIDLimitMappingDetails mbidLimitMappingDetails,
            PayMethodViewsBiz payMethod) {

        List<PayChannelOptionViewBiz> payChannelOptionViews = payMethod.getPayChannelOptionViews();

        PayChannelOptionViewBiz payChannelOptionViewBiz = fetchPayChannelOptionView(payChannelOptionViews, bankCode);

        if (payChannelOptionViewBiz == null) {
            payMethod.setPayChannelOptionViews(null);
            return;
        }

        payMethod.setPayChannelOptionViews(Collections.singletonList(payChannelOptionViewBiz));

        EMIChannelInfoBiz emiChannelInfoBiz = fetchEMIChannelInfo(mbidLimitMappingDetails,
                payChannelOptionViewBiz.getEmiChannelInfos());

        if (emiChannelInfoBiz == null) {
            payChannelOptionViewBiz.setEmiChannelInfos(null);
            return;
        }

        payChannelOptionViewBiz.setEmiChannelInfos(Collections.singletonList(emiChannelInfoBiz));
    }

    /*
     * filter bank for which we have to show the emi
     */
    private PayChannelOptionViewBiz fetchPayChannelOptionView(List<PayChannelOptionViewBiz> payChannelOptionViews,
            final String bankCode) {

        for (PayChannelOptionViewBiz payChannelOptionViewBiz : payChannelOptionViews) {

            if (bankCode.equals(payChannelOptionViewBiz.getInstId())) {
                return payChannelOptionViewBiz;
            }
        }

        return null;
    }

    /*
     * filter plan for which we have to show the emi
     */
    private EMIChannelInfoBiz fetchEMIChannelInfo(final MBIDLimitMappingDetails mbidLimitMappingDetails,
            List<EMIChannelInfoBiz> emiChannelInfoList) {

        Double interest = mbidLimitMappingDetails.getInterest();

        Long months = mbidLimitMappingDetails.getMonth();

        for (EMIChannelInfoBiz emiChannelInfoBiz : emiChannelInfoList) {

            if (emiChannelInfoBiz.getInterestRate().equals(String.valueOf(interest))
                    && emiChannelInfoBiz.getOfMonths().equals(String.valueOf(months))) {
                return emiChannelInfoBiz;
            }
        }

        return null;
    }

    private void disableLogin(final HttpServletRequest httpServletRequest) {

        final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(httpServletRequest, true);

        loginInfo.setLoginFlag(true);
        loginInfo.setAutoLoginCreate(false);
        loginInfo.setLoginMandatory(false);
        loginInfo.setLoginRetryCount(0);
    }

}
