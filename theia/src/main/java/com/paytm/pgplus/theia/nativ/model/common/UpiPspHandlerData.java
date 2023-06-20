package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpiPspHandlerData implements Serializable {

    private static long serialVersionUID = 1963173967131702182L;

    private String name;

    private String displayName;

    private List<String> handlerList;

    public UpiPspHandlerData(String name, String displayName, List<String> handlerList) {
        this.name = name;
        this.displayName = displayName;
        this.handlerList = handlerList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getHandlerList() {
        return handlerList;
    }

    public void setHandlerList(List<String> handlerList) {
        this.handlerList = handlerList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
