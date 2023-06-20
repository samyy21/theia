package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.upi.service.NPCIHealthService;
import com.paytm.pgplus.facade.user.models.NpciHealthData;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class NpciHealthUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpciHealthUtil.class);

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    NPCIHealthService npciHealthService;

    public NpciHealthData getNpciHealth() {

        NpciHealthData npciHealth = null;
        try {
            LOGGER.info("Fetching NPCI health from Cache..");
            npciHealth = getNpciHealthViaCache();
            if (npciHealth == null) {
                LOGGER.info("NPCI health absent from cache , Fetching via UPI..");
                npciHealth = getNpciHealthViaHttp();
                setNpciHealthInCache(npciHealth);
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching NpciHealth : {}", e);
        }
        return npciHealth;
    }

    private NpciHealthData getNpciHealthViaHttp() throws FacadeCheckedException {

        return npciHealthService.fetchNpciHealthFromUpi();

    }

    public NpciHealthData getNpciHealthViaCache() {
        return (NpciHealthData) theiaSessionRedisUtil.get("npciHealth");
    }

    public void setNpciHealthInCache(NpciHealthData npciHealth) {
        theiaSessionRedisUtil.set("npciHealth", npciHealth, 300);
    }

}