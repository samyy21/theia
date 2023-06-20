/**
 * 
 */
package com.paytm.pgplus.theia.services;

import java.io.Serializable;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @createdOn 04-Apr-2016
 * @author kesari
 */
public interface IBizService extends Serializable {

    GenericCoreResponseBean<WorkFlowResponseBean> processWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            IWorkFlow workFlow);

}
