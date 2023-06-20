package com.paytm.pgplus.theia.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cache.model.DynamicWrapperConfig;
import com.paytm.pgplus.cache.model.DynamicWrapperConfigList;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ankitgupta on 9/8/17.
 */
public class TheiaTestUtil {

    public static DynamicWrapperConfigList getDynamicWrapperConfig() throws Exception {
        String wrapperConfigPath = "src/test/resources/";
        if (StringUtils.isEmpty(wrapperConfigPath))
            throw new Exception("dynamicwrapper config path is not configured properly");
        File wrapperConfigFolder = new File(wrapperConfigPath);

        if (wrapperConfigFolder != null && wrapperConfigFolder.exists() && wrapperConfigFolder.isDirectory()) {
            File[] files = wrapperConfigFolder.listFiles();
            ObjectMapper om = new ObjectMapper();
            List<DynamicWrapperConfig> configs = new ArrayList<>();
            for (File file : files) {
                String fileName = file.getName();
                String[] nameArr = fileName.split("\\.");
                if (nameArr != null && nameArr.length == 2 && nameArr[1].equals("json")) {
                    String merchantIdFromPath = nameArr[0];
                    DynamicWrapperConfig dynamicWrapperConfig = null;
                    try {
                        dynamicWrapperConfig = new DynamicWrapperConfig(merchantIdFromPath, om.readValue(file,
                                Map.class));
                    } catch (IOException e) {
                        throw e;
                    }
                    configs.add(dynamicWrapperConfig);
                }
            }
            DynamicWrapperConfigList dynamicWrapperConfigList = new DynamicWrapperConfigList();
            dynamicWrapperConfigList.setDynamicWrapperConfigs(configs);
            return dynamicWrapperConfigList;
        }
        return null;
    }
}
