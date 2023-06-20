package com.paytm.pgplus.biz.paymode;

import com.paytm.pgplus.biz.core.model.request.ChannelAccountQueryResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paraschawla on 10/7/18.
 */
@Service("loyaltyPointAutoDebitPayMode")
public class LoyaltyPointAutoDebitPayMode {

    public static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyPointAutoDebitPayMode.class);

    public GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> addLoyaltyPointsInfoInExtendedInfo(
            WorkFlowRequestBean flowRequestBean, WorkFlowHelper workFlowHelper,
            WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = getLoyaltyPointInfo(
                flowRequestBean, workFlowHelper, workFlowTransBean);
        if (channelAccountQueryResponse != null && channelAccountQueryResponse.isSuccessfullyProcessed()) {
            Map<String, String> loyaltyPointMap = getRequiredLoyaltyPointInfo(workFlowTransBean,
                    channelAccountQueryResponse);
            if (MapUtils.isNotEmpty(loyaltyPointMap))
                setExtendInfo(workFlowTransBean, loyaltyPointMap);
        } else if (channelAccountQueryResponse != null && !channelAccountQueryResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(channelAccountQueryResponse.getFailureMessage());
        }
        return null;
    }

    public void addLoyaltyPointsInfoInExtInfoWithoutChannelAccQuery(WorkFlowTransactionBean workFlowTransBean) {

        String exchangeRate = getExchangeRateForLoyaltyPoint(workFlowTransBean);
        if (StringUtils.isEmpty(exchangeRate)) {
            return;
        }
        try {
            Map<String, String> loyaltyPointMap = new HashMap<>();
            String pointNumber = String.valueOf(Double.parseDouble(exchangeRate)
                    * (Double.parseDouble(workFlowTransBean.getWorkFlowBean().getTxnAmount()) / 100));
            loyaltyPointMap.put("exchangeRate", exchangeRate);
            loyaltyPointMap.put("pointNumber", pointNumber);
            if (MapUtils.isNotEmpty(loyaltyPointMap))
                setExtendInfo(workFlowTransBean, loyaltyPointMap);
        } catch (Exception e) {
            LOGGER.error("Invalid exchangeRate for Loyalty Point : {} ", e);
        }
    }

    private String getExchangeRateForLoyaltyPoint(WorkFlowTransactionBean workFlowTransBean) {
        if (workFlowTransBean == null || workFlowTransBean.getWorkFlowBean() == null
                || workFlowTransBean.getWorkFlowBean().getPaymentRequestBean() == null) {
            return null;
        } else {
            return workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getExchangeRate();
        }
    }

    private GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> getLoyaltyPointInfo(
            WorkFlowRequestBean flowRequestBean, WorkFlowHelper workFlowHelper,
            WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = null;
        if (EPayMethod.LOYALTY_POINT.getMethod().equals(flowRequestBean.getPaymentTypeId())) {
            LOGGER.info("Loyalty_Point enabled on user with token {} , mid {}", workFlowTransBean.getUserDetails()
                    .getUserToken(), workFlowTransBean.getWorkFlowBean().getPaytmMID());
            channelAccountQueryResponse = workFlowHelper.channelAccountQuery(workFlowTransBean);
        }
        return channelAccountQueryResponse;
    }

    private Map<String, String> getRequiredLoyaltyPointInfo(WorkFlowTransactionBean workFlowTransBean,
            GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse) {
        Map<String, String> loyaltyPointMap = new HashMap<>();
        if (channelAccountQueryResponse.isSuccessfullyProcessed() && channelAccountQueryResponse != null
                && channelAccountQueryResponse.getResponse() != null) {
            String exchangeRate = channelAccountQueryResponse.getResponse().getChannelAccountViews().get(0)
                    .getChannelAccounts().get(0).getExchangeRate();
            String pointNumber = String.valueOf(Double.parseDouble(exchangeRate)
                    * (Double.parseDouble(workFlowTransBean.getWorkFlowBean().getTxnAmount()) / 100));
            loyaltyPointMap.put("exchangeRate", exchangeRate);
            loyaltyPointMap.put("pointNumber", pointNumber);
            return loyaltyPointMap;
        }
        return Collections.emptyMap();
    }

    private void setExtendInfo(WorkFlowTransactionBean workFlowTransBean, Map<String, String> loyaltyPointMap) {
        ExtendedInfoRequestBean extendedInfoRequestBean = workFlowTransBean.getWorkFlowBean().getExtendInfo();
        extendedInfoRequestBean.setExchangeRate(loyaltyPointMap.get("exchangeRate"));
        extendedInfoRequestBean.setPointNumber(loyaltyPointMap.get("pointNumber"));
        workFlowTransBean.getWorkFlowBean().setExtendInfo(extendedInfoRequestBean);
        LOGGER.debug("Loyalty Point info added successfully in extendedInfo, loyaltyPointMap size {}",
                loyaltyPointMap.size());
    }

}
