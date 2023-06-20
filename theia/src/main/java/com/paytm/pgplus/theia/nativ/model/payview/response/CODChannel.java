package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.models.Money;

import javax.validation.Valid;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.COD_MESSAGE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.HYBRID_NOT_ALLOWED_MSG;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CODChannel extends PayChannelBase {

    private static final long serialVersionUID = -2185114826536349921L;

    @Valid
    private Money minAmount;

    @Valid
    private String codHybridErrMsg;

    @Valid
    private String codMessage;

    public CODChannel() {
    }

    public CODChannel(String payMethod, String payChannelOption, StatusInfo isDisabled, StatusInfo hasLowSuccess,
            String iconUrl, Money minAmount) {
        super(payMethod, payChannelOption, isDisabled, hasLowSuccess, iconUrl);
        this.minAmount = minAmount;
        this.codHybridErrMsg = EPayMethod.COD.getNewDisplayName() + HYBRID_NOT_ALLOWED_MSG;
        this.codMessage = COD_MESSAGE;

    }

    public Money getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Money minAmount) {
        this.minAmount = minAmount;
    }

    public String getCodHybridErrMsg() {
        return codHybridErrMsg;
    }

    public void setCodHybridErrMsg(String codHybridErrMsg) {
        this.codHybridErrMsg = codHybridErrMsg;
    }

    public String getCodMessage() {
        return codMessage;
    }

    public void setCodMessage(String codMessage) {
        this.codMessage = codMessage;
    }

    @Override
    public String toString() {
        return "CODChannel{" + "minAmount=" + minAmount + ", codHybridErrMsg='" + codHybridErrMsg + '\''
                + ", codMessage='" + codMessage + '\'' + '}';
    }
}
