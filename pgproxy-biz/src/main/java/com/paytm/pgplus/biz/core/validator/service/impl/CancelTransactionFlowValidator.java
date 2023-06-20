/**
 * 
 */
package com.paytm.pgplus.biz.core.validator.service.impl;

import com.paytm.pgplus.common.enums.ETransType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author vaishakhnair
 *
 */
@Service("cancelTransactionValidator")
public class CancelTransactionFlowValidator implements IValidator {

    @Override
    public GenericCoreResponseBean<Boolean> validate(WorkFlowRequestBean workFlowBean) {

        String errorMessage = null;
        ResponseConstants responseConstant = null;

        if (workFlowBean == null
                || StringUtils.isBlank(workFlowBean.getTransID())
                || (StringUtils.isBlank(workFlowBean.getAlipayMID()) && !(ETransType.TOP_UP).equals(workFlowBean
                        .getTransType()))) {
            errorMessage = "TransID or Alipay Mid is blank";
            responseConstant = ResponseConstants.INVALID_PARAM;
            return new GenericCoreResponseBean<>(errorMessage, responseConstant);
        }

        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

}
