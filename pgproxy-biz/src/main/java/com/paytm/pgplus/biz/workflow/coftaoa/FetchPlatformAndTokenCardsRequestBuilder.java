package com.paytm.pgplus.biz.workflow.coftaoa;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsRequest;

public interface FetchPlatformAndTokenCardsRequestBuilder {

    FetchPlatformAndTokenCardsRequest buildRequest(WorkFlowRequestBean workFlowRequestBean)
            throws FacadeCheckedException;
}
