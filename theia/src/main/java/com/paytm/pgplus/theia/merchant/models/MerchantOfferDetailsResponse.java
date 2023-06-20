package com.paytm.pgplus.theia.merchant.models;

import java.io.Serializable;

public class MerchantOfferDetailsResponse implements Serializable {

    private static final long serialVersionUID = -1335154422059874331L;

    private MappingMerchantOfferDetails mappingMerchantOfferDetails;
    private boolean successfullyProcessed;

    public MappingMerchantOfferDetails getMappingMerchantOfferDetails() {
        return mappingMerchantOfferDetails;
    }

    public void setMappingMerchantOfferDetails(MappingMerchantOfferDetails mappingMerchantOfferDetails) {
        this.mappingMerchantOfferDetails = mappingMerchantOfferDetails;
    }

    public boolean isSuccessfullyProcessed() {
        return successfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean successfullyProcessed) {
        this.successfullyProcessed = successfullyProcessed;
    }

    @Override
    public String toString() {
        return "MerchantOfferDetailsResponse [mappingMerchantOfferDetails=" + mappingMerchantOfferDetails
                + ", successfullyProcessed=" + successfullyProcessed + "]";
    }

}
