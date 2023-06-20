package com.paytm.pgplus.biz.core.cachecard.service;

import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author manojpal
 *
 */
public interface ICacheCardInfoService {

    public GenericCoreResponseBean<CacheCardResponseBean> cacheCardInfo(CacheCardRequestBean cacheCardRequest,
            WorkFlowTransactionBean flowTransBean);

}
