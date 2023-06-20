package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferDetail implements Serializable {

    private String termsUrl;

    private String termsTitle;

    @LocaleField
    private String title;

    @LocaleField
    private String text;

    private String icon;

    public String getTermsUrl() {
        return termsUrl;
    }

    public void setTermsUrl(String termsUrl) {
        this.termsUrl = termsUrl;
    }

    public String getTermsTitle() {
        return termsTitle;
    }

    public void setTermsTitle(String termsTitle) {
        this.termsTitle = termsTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OfferDetail{");
        sb.append(", termsUrl").append(termsUrl);
        sb.append(", termsTitle").append(termsTitle);
        sb.append(", title").append(title);
        sb.append(", text").append(text);
        sb.append(", icon").append(icon);
        sb.append('}');
        return sb.toString();
    }
}
