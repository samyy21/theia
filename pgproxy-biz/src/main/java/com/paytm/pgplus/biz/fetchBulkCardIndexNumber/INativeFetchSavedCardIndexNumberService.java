package com.paytm.pgplus.biz.fetchBulkCardIndexNumber;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface INativeFetchSavedCardIndexNumberService {
    GenericCoreResponseBean<Boolean> fetchCardIndexNotLoggedIn(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean);

    GenericCoreResponseBean<Boolean> fetchCardIndexLoggedIn(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean);
}
