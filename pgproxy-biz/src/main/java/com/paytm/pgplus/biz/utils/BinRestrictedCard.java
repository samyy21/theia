package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BinRestrictedCard {
    public static final Set<String> BIN_8_RESTRICTED_BANKS;
    public static final Set<String> BIN_8_RESTRICTED_NETWORKS;

    private BinRestrictedCard() {

    }

    static {
        String bin8RestrictedBanks = ConfigurationUtil.getProperty("bin8.restricted.banks", "AMEX");
        String bin8RestrictedNetworks = ConfigurationUtil.getProperty("bin8.restricted.networks", "AMEX");
        BIN_8_RESTRICTED_BANKS = Arrays.stream(bin8RestrictedBanks.split(Pattern.quote(","))).collect(
                Collectors.toSet());
        BIN_8_RESTRICTED_NETWORKS = Arrays.stream(bin8RestrictedNetworks.split(Pattern.quote(","))).collect(
                Collectors.toSet());
    }
}
