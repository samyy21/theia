package com.paytm.pgplus.biz.workflow.walletconsult;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface WalletConsultService {

    GenericCoreResponseBean<BizWalletConsultResponse> doWalletConsult(WorkFlowTransactionBean workFlowTransactionBean);

    GenericCoreResponseBean<WorkFlowTransactionBean> applyFeeIfApplicableForAddNPayTransaction(
            BizWalletConsultResponse bizWalletConsultResponse, WorkFlowTransactionBean workFlowTransactionBean);
}
