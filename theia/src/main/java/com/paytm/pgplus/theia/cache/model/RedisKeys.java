package com.paytm.pgplus.theia.cache.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class RedisKeys {
    String prefix;
    String delimiter;
    String valueToAppend;

    public RedisKeys(String prefix, String delimiter, String valueToAppend) {
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.valueToAppend = valueToAppend;
    }

    public String createRedisKey() {
        StringBuilder key = new StringBuilder();
        key.append(this.prefix);
        if (StringUtils.isNotBlank(this.delimiter))
            key.append(this.delimiter);
        key.append(this.valueToAppend);
        return key.toString();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getValueToAppend() {
        return valueToAppend;
    }
}
