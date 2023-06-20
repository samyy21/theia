package com.paytm.pgplus.biz.workflow.coftaoa;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface FetchPlatformAndTokenCardsService {

    GenericCoreResponseBean<FetchPlatformAndTokenCardsResponse> getAllPlatformAndTokenCards(
            WorkFlowRequestBean workFlowRequestBean);
}
