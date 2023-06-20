package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.cache.model.BinDetail;

import java.io.Serializable;

/**
 * Created by Naman on 10/04/17.
 */
public class BinDetailsTheia implements Serializable {

    private static final long serialVersionUID = 1281487505469042663L;

    private BinDetail binDetail;

    private boolean issuerLowSuccessRate;

    private boolean cardSchemeLowSuccessRate;

    private boolean isOnMaintenance;

    private String promoResultMessage;

    private String cardStatusMessage;

    public BinDetailsTheia() {

    }

    /**
     * @param binDetail
     */
    public BinDetailsTheia(BinDetail binDetail) {
        this.binDetail = binDetail;
    }

    public boolean isCardSchemeLowSuccessRate() {
        return cardSchemeLowSuccessRate;
    }

    public void setCardSchemeLowSuccessRate(boolean cardSchemeLowSuccessRate) {
        this.cardSchemeLowSuccessRate = cardSchemeLowSuccessRate;
    }

    public BinDetail getBinDetail() {
        return binDetail;
    }

    public void setBinDetail(BinDetail binDetail) {
        this.binDetail = binDetail;
    }

    public boolean isIssuerLowSuccessRate() {
        return issuerLowSuccessRate;
    }

    public void setIssuerLowSuccessRate(boolean issuerLowSuccessRate) {
        this.issuerLowSuccessRate = issuerLowSuccessRate;
    }

    public boolean isOnMaintenance() {
        return isOnMaintenance;
    }

    public void setOnMaintenance(boolean onMaintenance) {
        isOnMaintenance = onMaintenance;
    }

    public String getPromoResultMessage() {
        return promoResultMessage;
    }

    public void setPromoResultMessage(String promoResultMessage) {
        this.promoResultMessage = promoResultMessage;
    }

    public String getCardStatusMessage() {
        return cardStatusMessage;
    }

    public void setCardStatusMessage(String cardStatusMessage) {
        this.cardStatusMessage = cardStatusMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinDetailsTheia{");
        sb.append("binDetail=").append(binDetail);
        sb.append(", issuerLowSuccessRate=").append(issuerLowSuccessRate);
        sb.append(", cardSchemeLowSuccessRate=").append(cardSchemeLowSuccessRate);
        sb.append(", isOnMaintenance=").append(isOnMaintenance);
        sb.append(" , promoResultMessage=").append(promoResultMessage);
        sb.append(" , cardStatusMessage=").append(cardStatusMessage);
        sb.append('}');
        return sb.toString();
    }
}