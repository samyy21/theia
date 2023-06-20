package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceRequest;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author kartik
 * @date 17-Sep-2017
 */
public interface IPaymentsBankService {

    GenericCoreResponseBean<AccountBalanceResponse> fetchSavingsAccountBalance(WorkFlowTransactionBean workFlowTransBean);

    GenericCoreResponseBean<AccountBalanceResponse> fetchSavingsAccountBalance(
            FetchAccountBalanceRequest fetchAccountBalanceRequest);
}
