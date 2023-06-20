package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;

public interface IPayviewConsultService {

    void validate(CashierInfoContainerRequest cashierInfoContainerRequest) throws RequestValidationException;

    WorkFlowResponseBean process(CashierInfoContainerRequest serviceRequest);
}
