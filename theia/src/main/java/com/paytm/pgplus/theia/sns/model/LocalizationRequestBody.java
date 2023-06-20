package com.paytm.pgplus.theia.sns.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalizationRequestBody {

    private String apiName;
    private Set<String> unTranslatedText;

}
