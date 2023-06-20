package com.paytm.pgplus.theia.datamapper.validator.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.theia.datamapper.dto.EMIPayMethodDTO;
import com.paytm.pgplus.theia.datamapper.validator.Validator;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;

/**
 * 
 * @author ruchikagarg
 *
 */
@Component
public class EMIPayMethodValidatorImpl implements Validator<EMIPayMethodDTO> {

    @Override
    public void validate(EMIPayMethodDTO obj) throws TheiaServiceException {
        if (obj == null) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: Object is null");
        } else if (obj.getPaymentRequestBean() == null || obj.getWorkFlowRequestBean() == null) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: object params is null");
        } else if (EPayMode.ADDANDPAY == obj.getWorkFlowRequestBean().getPaytmExpressAddOrHybrid()) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: AddnPay is not allowed");
        } else if (!PaymentTypeIdEnum.EMI.value.equals(obj.getPaymentRequestBean().getPaymentTypeId())) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: Only EMI is allowed");
        } else if (StringUtils.isEmpty(obj.getPaymentRequestBean().getEmiPlanID())) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: Invalid PlanId");
        }
    }

}
