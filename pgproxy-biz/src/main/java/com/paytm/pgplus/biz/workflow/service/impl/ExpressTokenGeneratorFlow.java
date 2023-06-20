package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_PROMO;

@Service("expressTokenGeneratorFlow")
public class ExpressTokenGeneratorFlow implements IWorkFlow {
    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        /*
         * Here CacheCard API call will be processed like Seamless cacheCard
         * API, Will be fetching details from requestBean itself.
         */
        final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper.cacheCard(
                workFlowTransBean, CacheCardType.SEAMLESS);
        if (!createCacheCardResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createCacheCardResponse.getFailureMessage(),
                    createCacheCardResponse.getResponseConstant());
        }
        workFlowResponseBean.setCacheCardResponseBean(createCacheCardResponse.getResponse());

        if (ff4jUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(), ENABLE_GCIN_ON_COFT_PROMO, false)) {
            workFlowResponseBean.setWorkFlowRequestBean(workFlowTransBean.getWorkFlowBean());
        }

        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }
}
