package com.paytm.pgplus.biz.taskengine.task.impl.PayViewConsultTask;

import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("addnPayLitePayViewConsultTask")
public class AddnPayLitePayViewConsultTask extends LitePayViewConsultTask {

    @Override
    public boolean isMandatory(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> consultResponse = workFlowHelper
                .litePayviewPayMethodConsult(workFlowTransactionBean, true);
        GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultResponse = null;
        if (consultResponse.isSuccessfullyProcessed()) {
            // though lite pay is consult is called but setting in
            // both the consult so outer layers are not
            // affected
            merchantConsultResponse = getMerchantConsultPayViewBean(consultResponse);
            workFlowTransactionBean.setAddAndPayViewConsult(merchantConsultResponse.getResponse());
            workFlowTransactionBean.setAddAndPayLiteViewConsult(consultResponse.getResponse());

            // subscription flow
            if (ERequestType.SUBSCRIBE.equals(workFlowRequestBean.getRequestType())
                    || ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(workFlowRequestBean.getRequestType())) {
                workFlowHelper.filterOperationsLitePayView(workFlowTransactionBean, false);
            }
            // flag added to get value from global vault.
            workFlowRequestBean.setAddNPayGlobalVaultCards(Boolean.TRUE);
        }
        return merchantConsultResponse;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        Map<String, String> extendInfoMap = transBean.getMerchantViewConsult().getExtendInfo();
        if (null != extendInfoMap && !extendInfoMap.isEmpty()) {
            String addAndPayAllowed = extendInfoMap
                    .get(BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_KEY);
            if (BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_YES.equals(addAndPayAllowed)
                    && (StringUtils.isNotBlank(input.getToken()) || input.getUserDetailsBiz() != null)) {
                return true;
            }
        }
        return false;

    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setAddAndPayViewResponse(transBean.getAddAndPayViewConsult());
        response.setAddAndPayLiteViewResponse(transBean.getAddAndPayLiteViewConsult());
        workFlowHelper.checkUPIPUSHEnabled(transBean);
        response.setAddUpiPushEnabled(transBean.isAddUpiPushEnabled());
        response.setAddUpiPushExpressEnabled(transBean.isAddUpiPushExpressEnabled());
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.ADDNPAY_LITEPAYVIEW_CONSULT;
    }
}
