package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitCard extends BankCard {

    private static final long serialVersionUID = 1059769898671078817L;
}
