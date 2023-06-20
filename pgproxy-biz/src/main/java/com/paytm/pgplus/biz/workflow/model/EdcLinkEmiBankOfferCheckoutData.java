package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.ItemBreakUp;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.PromoResponseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdcLinkEmiBankOfferCheckoutData {

    private int status;
    private boolean promoVisibility;
    private String responseCode;
    private String promotext;
    private String prePromoText;
    private String verificationCode;
    private long effectivePromoDeduction;
    private long effectivePromoSaving;
    private String promocode;
    private String tncUrl;
    private String payText;
    private List<PromoSaving> savings;
    private Map<String, String> promoContext;
    private Map<String, PromoResponseData> promoResponse;
    private String emi;
    private String interest;
    private String emiLabel;
    private String message;
    private String emiType;
    private List<Gratification> gratification;
    private List<ItemBreakUp> itemBreakUp;

}
