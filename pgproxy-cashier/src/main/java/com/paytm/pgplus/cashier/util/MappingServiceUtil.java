package com.paytm.pgplus.cashier.util;

import java.util.List;

import com.paytm.pgplus.cashier.redis.IPgProxyCashierTransactionalRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MappingServiceUtil {

    @Autowired
    private IPgProxyCashierTransactionalRedisUtil pgProxyCashierTransactionalRedisUtil;

    public String getSCWMID() {
        return getValue("SCW.MID");
    }

    public String getValue(String key) {
        return (String) pgProxyCashierTransactionalRedisUtil.get(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> getBankListForNBCap() {
        return (List<String>) pgProxyCashierTransactionalRedisUtil.get("NB.BANK.CAP.LIST");
    }

    @SuppressWarnings("unchecked")
    public List<String> getBankListForBankCardCap() {
        return (List<String>) pgProxyCashierTransactionalRedisUtil.get("CARD.BANK.CAP.LIST");
    }

    @SuppressWarnings("unchecked")
    public List<String> getMerchantListForCappedAmount() {
        return (List<String>) pgProxyCashierTransactionalRedisUtil.get("MERCHANT.CAP.LIST");
    }

}