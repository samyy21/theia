package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.cachecard.service.ICacheCardInfoService;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.fetchBulkCardIndexNumber.NativeFetchSavedCardIndexNumberHelper;
import com.paytm.pgplus.biz.fetchBulkCardIndexNumber.model.NativeCardIndexNumberResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;

import java.util.concurrent.Callable;

public class NativeFetchBulkCardIndexNumberTask implements Callable<NativeCardIndexNumberResponse> {

    private CacheCardRequestBean cacheCardRequestBean;

    private WorkFlowTransactionBean workFlowTransactionBean;

    private ICacheCardInfoService cacheCardInfoService;

    private NativeFetchSavedCardIndexNumberHelper nativeFetchSavedCardIndexNumberHelper;

    public NativeFetchBulkCardIndexNumberTask() {
    }

    public NativeFetchBulkCardIndexNumberTask(CacheCardRequestBean cacheCardRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean) {
        this.cacheCardRequestBean = cacheCardRequestBean;
        this.workFlowTransactionBean = workFlowTransactionBean;
    }

    @Override
    public NativeCardIndexNumberResponse call() throws Exception {
        CacheCardRequest cacheCardRequest = nativeFetchSavedCardIndexNumberHelper
                .createCacheCardRequest(cacheCardRequestBean);
        final CacheCardResponse cacheCardFacadeResponse = nativeFetchSavedCardIndexNumberHelper
                .callAPlusForCardIdxNo(cacheCardRequest);

        NativeCardIndexNumberResponse servResp = null;

        if (cacheCardFacadeResponse != null && cacheCardFacadeResponse.getBody() != null) {
            servResp = new NativeCardIndexNumberResponse();
            servResp.setCacheCardResponseBody(cacheCardFacadeResponse.getBody());
        }

        return servResp;

    }

    public ICacheCardInfoService getCacheCardInfoService() {
        return cacheCardInfoService;
    }

    public void setCacheCardInfoService(ICacheCardInfoService cacheCardInfoService) {
        this.cacheCardInfoService = cacheCardInfoService;
    }

    public NativeFetchSavedCardIndexNumberHelper getNativeFetchSavedCardIndexNumberHelper() {
        return nativeFetchSavedCardIndexNumberHelper;
    }

    public void setNativeFetchSavedCardIndexNumberHelper(
            NativeFetchSavedCardIndexNumberHelper nativeFetchSavedCardIndexNumberHelper) {
        this.nativeFetchSavedCardIndexNumberHelper = nativeFetchSavedCardIndexNumberHelper;
    }

    public CacheCardRequestBean getCacheCardRequestBean() {
        return cacheCardRequestBean;
    }

    public void setCacheCardRequestBean(CacheCardRequestBean cacheCardRequestBean) {
        this.cacheCardRequestBean = cacheCardRequestBean;
    }

    public WorkFlowTransactionBean getWorkFlowTransactionBean() {
        return workFlowTransactionBean;
    }

    public void setWorkFlowTransactionBean(WorkFlowTransactionBean workFlowTransactionBean) {
        this.workFlowTransactionBean = workFlowTransactionBean;
    }
}
