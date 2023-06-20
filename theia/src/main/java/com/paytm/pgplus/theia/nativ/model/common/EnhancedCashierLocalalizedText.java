package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierLocalalizedText implements Serializable {

    private static final long serialVersionUID = -8041644663254815134L;

    private String lang;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
