package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.request.BizPreferenceValue;
import com.paytm.pgplus.cache.model.routing.manual.BaseInfo;
import com.paytm.pgplus.cache.model.routing.manual.ManualRouting;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class AOAGatewayScoreCalculator {

    public static final Logger LOGGER = LoggerFactory.getLogger(AOAGatewayScoreCalculator.class);

    private Set<String> enableGateways;

    private Set<ManualRouting> gatewayScorePreferencesSet;

    public AOAGatewayScoreCalculator() {
    }

    public AOAGatewayScoreCalculator(Set<String> enableGateways, Set<ManualRouting> gatewayScorePreferencesSet) {
        this.enableGateways = enableGateways;
        this.gatewayScorePreferencesSet = gatewayScorePreferencesSet;
    }

    public List<BizPreferenceValue> calculate() {
        int flag = 0;
        List<BizPreferenceValue> preferenceValues = new ArrayList<>();
        Set<String> blackListedGateways = removeBlackListedGateways();
        for (ManualRouting manualRouting : gatewayScorePreferencesSet) {
            List<? extends BaseInfo> baseInfos = manualRouting.getBaseInfoList();
            for (BaseInfo baseInfo : baseInfos) {
                flag = flag + setGatewayScoreIfEnable(enableGateways, baseInfo, preferenceValues);
            }
            if (flag > 0) {
                break;
            }
        }
        for (String gateway : blackListedGateways) {
            preferenceValues.add(new BizPreferenceValue(gateway, 0));
        }
        return preferenceValues;
    }

    private Set<String> removeBlackListedGateways() {
        Set<String> blackListedGateways = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (ManualRouting manualRouting : gatewayScorePreferencesSet) {
            List<? extends BaseInfo> baseInfos = manualRouting.getBaseInfoList();
            for (BaseInfo baseInfo : baseInfos) {
                if (TheiaConstant.AOAConstants.ZERO_SCORE.equals(baseInfo.getScore())) {
                    enableGateways.remove(baseInfo.getGateway());
                    blackListedGateways.add(baseInfo.getGateway());
                    sb.append(baseInfo.getGateway());
                    sb.append(" ");
                }
            }
        }
        LOGGER.info("Black listed gateways : {}", sb.toString());
        return blackListedGateways;
    }

    private int setGatewayScoreIfEnable(Set<String> enableGateways, BaseInfo baseInfo,
            List<BizPreferenceValue> preferenceValues) {
        if (enableGateways.contains(baseInfo.getGateway())) {
            BizPreferenceValue bizPreferenceValue = new BizPreferenceValue();
            bizPreferenceValue.setServiceInstId(baseInfo.getGateway());
            bizPreferenceValue.setScore((int) (Double.valueOf(baseInfo.getScore()) * 10));
            preferenceValues.add(bizPreferenceValue);
            return 1;
        }
        return 0;
    }
}
