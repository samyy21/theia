package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author manojpal
 *
 */
public class CacheCardResponseBean implements Serializable {

    private static final long serialVersionUID = 1626943587731338995L;

    private String tokenId;
    private String maskedCardNo;
    private String cardIndexNo;

    public CacheCardResponseBean(String tokenId, String maskedCardNo) {
        this.tokenId = tokenId;
        this.maskedCardNo = maskedCardNo;
    }

    public CacheCardResponseBean(String tokenId, String maskedCardNo, String cardIndexNo) {
        this.tokenId = tokenId;
        this.maskedCardNo = maskedCardNo;
        this.cardIndexNo = cardIndexNo;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    public void setMaskedCardNo(String maskedCardNo) {
        this.maskedCardNo = maskedCardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

}
