package com.paytm.pgplus.biz.taskengine.task.impl.PayViewConsultTask;

import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.payment.utils.LitePayViewEmiUtil;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("litePayViewConsultTask")
public class LitePayViewConsultTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    private LitePayViewEmiUtil emiUtil;

    @Override
    public boolean isMandatory(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        // if Native ADDMONEY request, then send addAndPay as true
        boolean addAndPay = workFlowRequestBean.isNativeAddMoney();
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> consultResponse = workFlowHelper
                .litePayviewPayMethodConsult(workFlowTransactionBean, addAndPay);
        GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultResponse = null;
        if (consultResponse.isSuccessfullyProcessed()) {
            // though lite pay is consult is called but setting in
            // both the consult so outer layers are not
            // affected
            merchantConsultResponse = getMerchantConsultPayViewBean(consultResponse);
            workFlowTransactionBean.setMerchantViewConsult(merchantConsultResponse.getResponse());
            workFlowTransactionBean.setMerchantLiteViewConsult(consultResponse.getResponse());
            workFlowTransactionBean.setPwpEnabled(consultResponse.getResponse().getPwpEnabled());

            // TODO:do we need to filter savedCard As well for subscrition flow
            // subscription flow
            if (((ERequestType.SUBSCRIBE.equals(workFlowRequestBean.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION_PAY
                    .equals(workFlowRequestBean.getRequestType())) && StringUtils.isNotBlank(workFlowRequestBean
                    .getToken()))
                    || (workFlowRequestBean.isSuperGwFpoApiHit() && workFlowRequestBean.getUserDetailsBiz() != null && ERequestType.NATIVE_SUBSCRIPTION_PAY
                            .equals(workFlowRequestBean.getRequestType()))) {
                workFlowHelper.filterOperationsLitePayView(workFlowTransactionBean, true);
            } else if (ERequestType.SUBSCRIBE.equals(workFlowRequestBean.getRequestType())
                    && StringUtils.isEmpty(workFlowRequestBean.getToken())) {
                // subscriptionflowusernotloggedin flow
                workFlowHelper.filterOperationsForUserNotLoggedIn_V2(workFlowTransactionBean, true);
            }

        }

        // handling of emi in user not logged flow since channel account
        // query will not be made in that case
        if (null == workFlowTransactionBean.getUserDetails()) {
            if (!workFlowTransactionBean.getWorkFlowBean().isEmiDetailsApi()
                    && StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getTxnAmount())) {
                emiUtil.setEmiInfoForLiteConsultPayview(workFlowTransactionBean, workFlowTransactionBean
                        .getWorkFlowBean().getTxnAmount());
            }
            // setting emi info in lite pay view response as well
            workFlowTransactionBean.getMerchantLiteViewConsult().setPayMethodViews(
                    workFlowTransactionBean.getMerchantViewConsult().getPayMethodViews());
        }

        // Fetch active subscriptions to use as payment mode
        if (ERequestType.NATIVE_MF_PAY.equals(workFlowTransactionBean.getWorkFlowBean().getRequestType())) {
            workFlowHelper.processToFetchActiveSubscriptions(workFlowTransactionBean);
        }

        workFlowResponseBean.setWorkFlowRequestBean(workFlowRequestBean);

        return merchantConsultResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.LITEPAYVIEW_CONSULT;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.LITEPAYVIEW_TIME, "150000"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setMerchnatViewResponse(transBean.getMerchantViewConsult());
        response.setMerchnatLiteViewResponse(transBean.getMerchantLiteViewConsult());
        response.setActiveSubscriptions(transBean.getActiveSubscriptions());
        workFlowHelper.checkUPIPUSHEnabled(transBean);
        response.setMerchantUpiPushEnabled(transBean.isMerchantUpiPushEnabled());
        response.setMerchantUpiPushExpressEnabled(transBean.isMerchantUpiPushExpressEnabled());
        response.setPwpEnabled(transBean.getPwpEnabled());
        response.setProductCode(transBean.getProductCode());
    }

    protected GenericCoreResponseBean<ConsultPayViewResponseBizBean> getMerchantConsultPayViewBean(
            GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult) {
        ConsultPayViewResponseBizBean merchantConsult = new ConsultPayViewResponseBizBean();
        merchantConsult.setPayMethodViews(litePayviewConsult.getResponse().getPayMethodViews());
        merchantConsult.setPaymentsBankSupported(litePayviewConsult.getResponse().isPaymentsBankSupported());
        merchantConsult.setExtendInfo(litePayviewConsult.getResponse().getExtendInfo());
        merchantConsult.setLoginMandatory(litePayviewConsult.getResponse().isLoginMandatory());
        merchantConsult.setWalletOnly(litePayviewConsult.getResponse().isWalletOnly());
        merchantConsult.setWalletFailed(litePayviewConsult.getResponse().isWalletFailed());
        return new GenericCoreResponseBean<>(merchantConsult);

    }
}
