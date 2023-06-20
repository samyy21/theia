package com.paytm.pgplus.biz.workflow.walletconsult;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;

public interface WalletConsultRequestBuilder {

    ConsultWalletLimitsRequest buildWalletConsultRequest(WorkFlowTransactionBean workFlowTransactionBean);

}
