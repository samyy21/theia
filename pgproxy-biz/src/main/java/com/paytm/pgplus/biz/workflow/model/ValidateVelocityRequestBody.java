package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateVelocityRequestBody implements Serializable {

    private static final long serialVersionUID = -2770175577011975491L;

    private String amount;
    private String mid;
    private String planId;
    private String pgPlanId;
    private String emiAmount;
    private String emiTotalAmount;
    private String interestRate;
    private String brandId;
    private String brandName;
    private String categoryId;
    private String categoryName;
    private String productId;
    private String productName;
    private String model;
    private String modelName;
    private String ean;
    private String quantity;
    private String verticalId;
    private String isEmiEnabled;
    private String offerId;
    private String skuCode;
    private String offerAmount;
    private String tenure;
    private String tid;
    private String time;
    private String year;
    private String date;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String cardIndexNumber;
    private String bin;
    private String productCode;
}
