package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service("hybridDisablingUtil")
public class HybridDisablingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HybridDisablingUtil.class);

    private HashMap<String, Set<String>> hybridDisabledPayMethodMap;

    @PostConstruct
    private void loadHybridDisabledPayMethodsFromProperty() {

        hybridDisabledPayMethodMap = new HashMap<>();
        String hybridDisabledProperty = ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.NATIVE_HYBRID_DISABLED_PAYMODES, "");
        if (StringUtils.isNotBlank(hybridDisabledProperty)) {
            List<String> hybridDisabledPayMethodList = Arrays.asList(StringUtils.split(hybridDisabledProperty, ";"));
            for (String hybridDisabledPayMethod : hybridDisabledPayMethodList) {
                // Split the property into pay method and list of paychannels
                List<String> listOfPayModePayChannel = Arrays.asList(StringUtils.split(hybridDisabledPayMethod, ":"));
                if (listOfPayModePayChannel.size() == 2) {
                    String payMode = listOfPayModePayChannel.get(0);
                    String payChannelList = listOfPayModePayChannel.get(1);
                    hybridDisabledPayMethodMap.put(payMode,
                            new HashSet<String>(Arrays.asList(StringUtils.split(payChannelList, ","))));
                }
            }
        }
    }

    public boolean isHybridDisabledForPayMethod(String payMethod, String payChannel) {

        boolean hybridDisabled = false;

        if (hybridDisabledPayMethodMap.containsKey(payMethod)) {
            Set<String> hybridDisabledPayChannels = hybridDisabledPayMethodMap.getOrDefault(payMethod,
                    SetUtils.EMPTY_SET);

            if (StringUtils.isNotBlank(payChannel) && (hybridDisabledPayChannels.contains(payChannel))) {
                hybridDisabled = true;
                LOGGER.debug("Hybrid flow is disabled as per the property for paymenthod and paychannel : ", payMethod,
                        payChannel);
            }

            if (hybridDisabledPayChannels.size() == 1) {
                if (hybridDisabledPayChannels.contains(TheiaConstant.ExtraConstants.ALL)) {
                    hybridDisabled = true;
                    LOGGER.debug("Hybrid flow is disabled as per the property for paymethod : ", payMethod);

                }
                if (hybridDisabledPayChannels.contains(TheiaConstant.ExtraConstants.NONE)) {
                    hybridDisabled = false;
                }
            }
        }
        if (!hybridDisabled && StringUtils.isNotBlank(payChannel)) {
            hybridDisabled = isHybridDisabledForBank(payChannel);
        }
        return hybridDisabled;
    }

    public boolean isHybridDisabledForBank(String bank) {
        Set<String> hybridDisabledBanks = hybridDisabledPayMethodMap.getOrDefault(TheiaConstant.ExtraConstants.BANKS,
                SetUtils.EMPTY_SET);
        if (StringUtils.isNotBlank(bank) && hybridDisabledBanks.contains(bank)) {
            LOGGER.debug("Hybrid flow is disabled as per the property for the bank : ", bank);
            return true;
        }
        return false;
    }

}
