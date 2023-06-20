package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class BankRedirectionDetail implements Serializable {

    private static final long serialVersionUID = 4218958030611582746L;

    private String url;

    private String method;

    private Map<String, String> content;

    private Map<String, String> metaData;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BankRedirectionDetail [url=").append(url).append(", method=").append(method)
                .append(", content=").append(!CollectionUtils.isEmpty(content)).append(", metaData=")
                .append(!CollectionUtils.isEmpty(metaData)).append("]");
        return builder.toString();
    }

}