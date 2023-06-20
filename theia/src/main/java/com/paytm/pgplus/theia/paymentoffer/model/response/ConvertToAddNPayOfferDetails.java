package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertToAddNPayOfferDetails implements Serializable {

    private static final long serialVersionUID = 6862759813197749080L;

    static class PayMethod implements Serializable {

        private static final long serialVersionUID = -164298575303610992L;
        private String consentText;
        private String payMethod;
        private String payChannelOption;
        private Boolean convertToAddNPayCheckBox;
        private Boolean convertToAddNPayDefaultValue;

        public PayMethod() {
        }

        public String getConsentText() {
            return consentText;
        }

        public void setConsentText(String consentText) {
            this.consentText = consentText;
        }

        public String getPayMethod() {
            return payMethod;
        }

        public void setPayMethod(String payMethod) {
            this.payMethod = payMethod;
        }

        public String getPayChannelOption() {
            return payChannelOption;
        }

        public void setPayChannelOption(String payChannelOption) {
            this.payChannelOption = payChannelOption;
        }

        public Boolean getConvertToAddNPayCheckBox() {
            return convertToAddNPayCheckBox;
        }

        public void setConvertToAddNPayCheckBox(Boolean convertToAddNPayCheckBox) {
            this.convertToAddNPayCheckBox = convertToAddNPayCheckBox;
        }

        public Boolean getConvertToAddNPayDefaultValue() {
            return convertToAddNPayDefaultValue;
        }

        public void setConvertToAddNPayDefaultValue(Boolean convertToAddNPayDefaultValue) {
            this.convertToAddNPayDefaultValue = convertToAddNPayDefaultValue;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PayMethod{");
            sb.append(", consentText").append(consentText);
            sb.append(", payMethod").append(payMethod);
            sb.append(", payChannelOption").append(payChannelOption);
            sb.append(", convertToAddNPayCheckBox").append(convertToAddNPayCheckBox);
            sb.append(", convertToAddNPayDefaultValue").append(convertToAddNPayDefaultValue);
            sb.append('}');
            return sb.toString();
        }
    }

    private List<OfferDetail> offerDetails;
    private List<PayMethod> payMethods;

    public List<OfferDetail> getOfferDetails() {
        return offerDetails;
    }

    public List<PayMethod> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<PayMethod> payMethods) {
        this.payMethods = payMethods;
    }

    public void setOfferDetails(List<OfferDetail> offerDetails) {
        this.offerDetails = offerDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConvertToAddNPayOfferDetails{");
        sb.append(", offerDetails").append(offerDetails);
        sb.append(", payMethods").append(payMethods);
        sb.append('}');
        return sb.toString();
    }
}
