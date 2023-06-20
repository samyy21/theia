package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;

@Service("ecomTokenHelper")
public class EcomTokenHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EcomTokenHelper.class);

    private static String ECOMTOKEN_NOT_SUPPORTED_MSG = "EcomToken " + NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;

    public void validateAndCheckIfEcomTokenTransactionAllowed(WorkFlowRequestBean flowRequestBean)
            throws TheiaDataMappingException {
        if (flowRequestBean.isEcomTokenTxn()) {
            /*
             * 1. Blocking Add Money request, in case of ecomTokenTxn. 2.
             * Blocking Add And Pay request, in case of ecomTokenTxn card. 3.
             * Blocking EMI request in case of ecomTokenTxn
             */
            if (flowRequestBean.isNativeAddMoney()
                    || (flowRequestBean.getPaytmExpressAddOrHybrid() != null && EPayMode.ADDANDPAY
                            .equals(flowRequestBean.getPaytmExpressAddOrHybrid()))
                    || PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId())) {
                LOGGER.error(ECOMTOKEN_NOT_SUPPORTED_MSG);
                throw new TheiaDataMappingException(ECOMTOKEN_NOT_SUPPORTED_MSG, ResponseConstants.INVALID_PAYMENTMODE);
            }

        }

    }

}
