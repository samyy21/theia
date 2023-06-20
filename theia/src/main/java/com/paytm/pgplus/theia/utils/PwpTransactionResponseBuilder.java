package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.common.constant.CommonConstant.PWP_PAYMODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.EMPTY_STRING;

@Service("pwpTransactionResponseBuilder")
public class PwpTransactionResponseBuilder {

    public void changeTransactionResponse(TransactionResponse response) {
        response.setPaymentMode(PWP_PAYMODE);
        response.setGateway(PWP_PAYMODE);
        response.setBankName(PWP_PAYMODE);
        response.setCardIndexNo(null);
        response.setMaskedCardNo(null);
        response.setCardHash(null);
        if (StringUtils.isBlank(response.getBankTxnId()) && response.getChildTxnList() != null) {
            response.setBankTxnId(response.getChildTxnList().get(0).getBankTxnId());
        }
        response.setChildTxnList(null);
        response.setCardScheme(null);
        response.setBinNumber(null);
        response.setLastFourDigits(null);
        if (response.getVpa() != null) {
            response.setVpa(null);
        }

    }

}
