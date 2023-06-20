/**
 * 
 */
package com.paytm.pgplus.biz.core.validator.service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author namanjain
 *
 */
public interface IValidator {

    @Loggable(state = TxnState.BIZ_VALIDATION)
    GenericCoreResponseBean<Boolean> validate(WorkFlowRequestBean workFlowBean);

}
