/**
 * 
 */
package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.InternalPaymentRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author namanjain
 *
 */
@Service("bizService")
public class BizServiceImpl implements IBizService {

    private static final long serialVersionUID = -8438427119847624865L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizServiceImpl.class);

    @Autowired
    @Qualifier("bizInternalPaymentRetry")
    private InternalPaymentRetryService internalPaymentRetryService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> processWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            IWorkFlow workFlow) {
        LOGGER.debug("workFlow  RECEIVED : {}", workFlow);

        internalPaymentRetryService.setRetryParams(workFlowRequestBean, 0);
        GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean = workFlow.process(workFlowRequestBean);

        if (internalPaymentRetryService.isInternalPaymentRetryRequired(flowResponseBean)) {
            GenericCoreResponseBean<WorkFlowResponseBean> retryFlowResponseBean = (GenericCoreResponseBean<WorkFlowResponseBean>) internalPaymentRetryService
                    .retryBankFormFetchWithPayment(workFlowRequestBean, workFlow);
            if (retryFlowResponseBean != null) {
                flowResponseBean = retryFlowResponseBean;
            }
        }

        return flowResponseBean;
    }
}