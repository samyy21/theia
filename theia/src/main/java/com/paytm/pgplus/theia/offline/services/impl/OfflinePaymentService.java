package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.services.IOfflinePaymentService;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rahulverma on 7/9/17.
 */
@Service("offlinePaymentService")
public class OfflinePaymentService implements IOfflinePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflinePaymentService.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    protected ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("userLoggedInLitePayviewConsultWorkflow")
    private IWorkFlow offlineWorkflow;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestProcessingException {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("Error while getting workflow response bean due to : {}", tdme.getMessage());
            throw PaymentRequestProcessingException.getException(ExceptionUtils.getMessage(tdme));
        }

        LOGGER.debug("WorkFlowRequestBean  CREATED : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw PaymentRequestProcessingException.getException("WorkFlowRequestBean is null");
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (!bizResponseBean.isSuccessfullyProcessed() || bizResponseBean.getResponse() == null) {
            LOGGER.info("Workflow processing error {}", bizResponseBean.getFailureDescription());
            if (bizResponseBean.getResponseConstant() != null) {
                throw PaymentRequestProcessingException.getException(
                        "Workflow processing error" + bizResponseBean.getFailureDescription(),
                        bizResponseBean.getResponseConstant());
            }
            throw PaymentRequestProcessingException.getException("Workflow processing error"
                    + bizResponseBean.getFailureDescription());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        LOGGER.debug("WorkFlowResponseBean {}", workFlowResponseBean);
        return workFlowResponseBean;
    }

    @Override
    public void validateRequestBean(CashierInfoRequest cashierInfoRequest) throws RequestValidationException {
        GenericFlowRequestBeanValidator<CashierInfoRequest> validator = new GenericFlowRequestBeanValidator<>(
                cashierInfoRequest);
        ValidationResultBean validationResultBean = validator.validate();
        if (!validationResultBean.isSuccessfullyProcessed() || !isSSOToken(cashierInfoRequest.getHead().getTokenType())) {
            LOGGER.error("Request validation failed ... {}", validationResultBean);
            LOGGER.error("Error = {}", validator.getErrorMessage());
            LOGGER.error("Property Path = {}", validator.getPropertyPath());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private boolean isSSOToken(TokenType tokenType) {
        return TokenType.SSO.equals(tokenType);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workFlow = fetchWorkflow(workFlowRequestBean);
        String offlineTaskFlowMids = "ALL";
        if (workFlow.equals(offlineWorkflow)
                && TaskFlowUtils.isMidEligibleForTaskFlow(workFlowRequestBean.getPaytmMID(), offlineTaskFlowMids)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            GenericCoreResponseBean<WorkFlowResponseBean> responseBean = taskExecutor.execute(workFlowRequestBean);
            stopWatch.stop();
            LOGGER.info("TASK Executor,Total time {}", stopWatch.getTotalTimeMillis());
            return responseBean;
        }
        return bizService.processWorkFlow(workFlowRequestBean, workFlow);
    }

    private IWorkFlow fetchWorkflow(final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workflow;

        workflow = offlineWorkflow;
        /*
         * if (merchantPreferenceService.isPostConvenienceFeesEnabled(
         * workFlowRequestBean.getPaytmMID())) { if
         * (StringUtils.isNotBlank(workFlowRequestBean.getToken())) { workflow =
         * buyerPaysChargeUserLoggedInFlow; } else { workflow =
         * buyerPaysChargeFlow; } } else { if
         * (StringUtils.isNotBlank(workFlowRequestBean.getToken())) { workflow =
         * defaultLoggedInFlow; } else { workflow = defaultUserNotLoggedFlow; }
         * }
         */
        return workflow;
    }

}
