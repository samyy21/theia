package com.paytm.pgplus.biz.workflow.service;

import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface IPay {
    GenericCoreResponseBean<BizPayResponse> processPayRequest(WorkFlowTransactionBean workFlowTransactionBean);

    String serviceType();

}
