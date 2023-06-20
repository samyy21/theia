package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;

public interface ICustomBeanMapper<T> {

    T getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean, CashierInfoRequest cashierInfoRequest);

    T getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean, CashierInfoRequest cashierInfoRequest,
            boolean disableWallet);

}
