package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.coftaoa.FetchPlatformAndTokenCardsService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsResponse;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION;

@Service("getPlatformAndTokenizedCardsTask")
public class GetPlatformAndTokenizedCardsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPlatformAndTokenizedCardsTask.class);

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    @Qualifier("fetchPlatformAndTokenCardsServiceImpl")
    private FetchPlatformAndTokenCardsService platformAndTokenCardsService;

    @Override
    protected GenericCoreResponseBean<FetchPlatformAndTokenCardsResponse> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) throws BaseException {

        LOGGER.info("Starting getPlatformAndTokenizedCardsTask task");
        final GenericCoreResponseBean<FetchPlatformAndTokenCardsResponse> consultResponse = platformAndTokenCardsService
                .getAllPlatformAndTokenCards(input);
        if (consultResponse != null && consultResponse.isSuccessfullyProcessed()
                && consultResponse.getResponse() != null) {
            LOGGER.info("Successfully processed getPlatformAndTokenizedCardsTask task : {}", consultResponse
                    .getResponse().getResponse());
            transBean.setPlatformAndTokenCards(consultResponse.getResponse().getResponse());
        } else {
            LOGGER.error("Error while processing platform and tokenized cards response : {}", consultResponse);
        }
        return consultResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.GET_PLATFORM_AND_TOKENIZED_CARDS;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {

        if (input.isFromAoaMerchant()
                && iPgpFf4jClient.checkWithdefault("theia.getTokenizedCardsInFPO", null, true)
                && (mappingUtil.isLocalVaultEnabled(input.getPaytmMID(), input.getCustID()) || ff4JUtil
                        .isGlobalVaultEnabled(transBean, input.getPaytmMID()))) {
            return coftUtil.isCardPaymethodDisabled(input) ? false : true;
        }
        return false;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.GET_PLATFORM_AND_TOKENIZED_CARDS_TIME,
                "20000"));
    }
}
