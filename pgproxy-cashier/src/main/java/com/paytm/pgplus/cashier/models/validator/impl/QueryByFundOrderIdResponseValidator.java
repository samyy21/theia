package com.paytm.pgplus.cashier.models.validator.impl;

import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.validator.IResponseValidator;
import com.paytm.pgplus.facade.fund.models.response.QueryByFundOrderIdResponse;

@Component
public class QueryByFundOrderIdResponseValidator implements IResponseValidator<QueryByFundOrderIdResponse> {

    @Override
    public void validInput(QueryByFundOrderIdResponse fundOrderIdResponse) throws CashierCheckedException {
        String errorMsg = null;
        if (isEmpty(fundOrderIdResponse)) {
            errorMsg = "Process failed : fundOrderIdResponse received as null";
        } else if (isEmpty(fundOrderIdResponse.getBody())) {
            errorMsg = "Process failed : fundOrderIdResponse body received as null";
        } else if (isEmpty(fundOrderIdResponse.getBody().getResultInfo())) {
            errorMsg = "Process failed : fundOrderIdResponse body Result Info received as null";
        }

        if (errorMsg != null)
            throw new CashierCheckedException(errorMsg);
    }

}
