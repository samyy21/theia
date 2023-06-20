package com.paytm.pgplus.theia.services.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;

/**
 * Created by Naman on 03/07/17.
 */
@Service("serviceHelper")
public class ServiceHelper {

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHelper.class);

    public boolean checkIfBizResponseResponseFailed(final GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean) {

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            return true;
        }

        return false;
    }

    public PageDetailsResponse returnFailureResponseToMerchant(final PaymentRequestBean requestData,
            ResponseConstants responseConstants) {

        String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, responseConstants);

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(false);
        pageDetailsResponse.setHtmlPage(htmlPage);
        return pageDetailsResponse;
    }

    public GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            IWorkFlow workFlow, IBizService bizService) {
        return bizService.processWorkFlow(workFlowRequestBean, workFlow);
    }

}
