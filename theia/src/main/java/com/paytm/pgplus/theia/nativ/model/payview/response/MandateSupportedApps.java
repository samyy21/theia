package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateSupportedApps implements Serializable {

    private static final long serialVersionUID = 8623845082981714289L;
    private String name;
    private List<String> handle;
    private String packageName;
    private Integer priority;

    public MandateSupportedApps(String name, List<String> handle, String packageName, Integer priority) {
        this.name = name;
        this.handle = handle;
        this.packageName = packageName;
        this.priority = priority;
    }

    public MandateSupportedApps(String name, List<String> handle, String packageName) {
        this.name = name;
        this.handle = handle;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getHandle() {
        return handle;
    }

    public void setHandle(List<String> handle) {
        this.handle = handle;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MandateSupportedApps{");
        sb.append("name='").append(name).append('\'');
        sb.append(", handle=").append(handle);
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }
}
