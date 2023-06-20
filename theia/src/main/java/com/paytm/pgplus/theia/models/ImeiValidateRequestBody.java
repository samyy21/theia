package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImeiValidateRequestBody implements Serializable {
    private String mid;
    private String orderId;
    private String skuCode;
    private String brandId;
    private String categoryId;
    private String imei;
    private String action;
}