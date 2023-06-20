/**
 * 
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("invoicePostFlow")
public class InvoicePaymentPostFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(InvoicePaymentPostFlow.class);

    @Autowired
    @Qualifier("invoicePaymentPostValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        WorkFlowTransactionBean flowTransBean = new WorkFlowTransactionBean();
        flowTransBean.setWorkFlowBean(flowRequestBean);

        // Fetch TransID
        GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantIDResponse = workFlowHelper
                .queryByMerchantTransID(flowTransBean, true);
        if (!queryByMerchantIDResponse.isSuccessfullyProcessed()
                || StringUtils.isBlank(queryByMerchantIDResponse.getResponse().getAcquirementId())) {
            return new GenericCoreResponseBean<>(queryByMerchantIDResponse.getFailureMessage());
        } else if (queryByMerchantIDResponse.getResponse().getStatusDetail() == null
                || queryByMerchantIDResponse.getResponse().getStatusDetail().getAcquirementStatus() == null
                || !queryByMerchantIDResponse.getResponse().getStatusDetail().getAcquirementStatus()
                        .equals(AcquirementStatusType.INIT)) {

            LOGGER.error("Query by Merchant TransID status is Invalid : {}", queryByMerchantIDResponse);
            return new GenericCoreResponseBean<>("Invalid Acquirement Status on Query Merchant TransID",
                    queryByMerchantIDResponse.getResponseConstant());
        }

        flowTransBean.setTransID(queryByMerchantIDResponse.getResponse().getAcquirementId());

        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultViewResponseBean = workFlowHelper
                .consultPayView(flowTransBean);
        if (!consultViewResponseBean.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultViewResponseBean.getFailureMessage(),
                    consultViewResponseBean.getResponseConstant());
        }
        flowTransBean.setMerchantViewConsult(consultViewResponseBean.getResponse());
        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(flowTransBean.getTransID());
        responseBean.setMerchnatViewResponse(flowTransBean.getMerchantViewConsult());
        responseBean.setExtendedInfo(createExtendedInfo(queryByMerchantIDResponse.getResponse()));

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "InvoicePaymentPostFlow",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }

    private Map<String, String> createExtendedInfo(QueryByMerchantTransIDResponseBizBean queryByMerchantIDResponse) {
        Map<String, String> extendedInfoMap = new HashMap<>();
        extendedInfoMap.putAll(queryByMerchantIDResponse.getExtendInfo());
        return extendedInfoMap;
    }
}