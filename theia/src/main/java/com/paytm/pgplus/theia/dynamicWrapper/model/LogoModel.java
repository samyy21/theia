package com.paytm.pgplus.theia.dynamicWrapper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogoModel implements Serializable {

    private static final long serialVersionUID = -4619916562565102923L;

    private String url;
    private int height;
    private int width;

}
