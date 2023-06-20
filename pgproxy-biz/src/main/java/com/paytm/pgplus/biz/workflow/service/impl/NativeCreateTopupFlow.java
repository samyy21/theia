package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;

@Service("nativeCreateTopupFlow")
public class NativeCreateTopupFlow implements Serializable, IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(NativeCreateTopupFlow.class);

    @Autowired
    @Qualifier("addMoneyValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("savedCards")
    ISavedCards savedCardsService;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        // Request Bean Validation
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(flowRequestBean,
                validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed() || StringUtils.isEmpty(flowRequestBean.getToken())) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());

        // Fetch User Details
        if (StringUtils.isNotBlank(flowRequestBean.getToken())) {
            userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                LOGGER.info("User details could not be fetched because ::{}", userDetails.getFailureMessage());
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            workFlowTransBean.getWorkFlowBean().getExtendInfo().setClientId(userDetails.getResponse().getClientId());
        }

        // Create Topup
        final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUpResponse = workFlowHelper.createTopUp(
                workFlowTransBean, false);
        if (!createTopUpResponse.isSuccessfullyProcessed()) {
            LOGGER.info("Topup API call failed due to ::{}", createTopUpResponse.getFailureMessage());
            return new GenericCoreResponseBean<>(createTopUpResponse.getFailureMessage(),
                    createTopUpResponse.getResponseConstant());
        }
        flowRequestBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());
        workFlowTransBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());
        LOGGER.info("Topup Created with response ::{}", createTopUpResponse);

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());

        LOGGER.info("Returning Response Bean From {}, transId : {} ", "nativeCreateTopupFlow",
                responseBean.getTransID());

        return new GenericCoreResponseBean<>(responseBean);
    }

}
