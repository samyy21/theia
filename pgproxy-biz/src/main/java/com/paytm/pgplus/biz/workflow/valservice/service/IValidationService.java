package com.paytm.pgplus.biz.workflow.valservice.service;

import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface IValidationService {
    GenericCoreResponseBean<ValidationServicePreTxnResponse> executePreTxnValidationModel(
            WorkFlowTransactionBean workFlowTransactionBean) throws EdcLinkBankAndBrandEmiCheckoutException;
}
