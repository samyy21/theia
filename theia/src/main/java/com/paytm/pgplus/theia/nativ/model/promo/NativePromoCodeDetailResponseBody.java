package com.paytm.pgplus.theia.nativ.model.promo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.response.BaseResponseBody;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePromoCodeDetailResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -7415732875931254475L;

    private PromoCodeData promoCodeDetail;
    private String checkPromoValidityURL;
    private String paymentModes;
    private String nbBanks;

    public PromoCodeData getPromoCodeDetail() {
        return promoCodeDetail;
    }

    public void setPromoCodeDetail(PromoCodeData promoCodeDetail) {
        this.promoCodeDetail = promoCodeDetail;
    }

    public String getCheckPromoValidityURL() {
        return checkPromoValidityURL;
    }

    public void setCheckPromoValidityURL(String checkPromoValidityURL) {
        this.checkPromoValidityURL = checkPromoValidityURL;
    }

    public String getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(String paymentModes) {
        this.paymentModes = paymentModes;
    }

    public String getNbBanks() {
        return nbBanks;
    }

    public void setNbBanks(String nbBanks) {
        this.nbBanks = nbBanks;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativePromoCodeDetailResponseBody{");
        sb.append("promoCodeDetail=").append(promoCodeDetail);
        sb.append(", checkPromoValidityURL='").append(checkPromoValidityURL).append('\'');
        sb.append(", paymentModes='").append(paymentModes).append('\'');
        sb.append(", nbBanks='").append(nbBanks).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
