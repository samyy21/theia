package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.payment.utils.AOAUtils;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.CHECKOUT;

@Service("createDynamicQRTask")
public class CreateDynamicQRTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDynamicQRTask.class);

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("aoaUtil")
    private AOAUtils aoaUtils;

    @Override
    protected GenericCoreResponseBean<QRCodeDetailsResponse> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        GenericCoreResponseBean<QRCodeDetailsResponse> dynamicQR = null;
        String mid = null;
        try {
            mid = transBean.getWorkFlowBean().getPaytmMID();
            if (input.isFromAoaMerchant()) {
                transBean.getWorkFlowBean().setPaytmMID(aoaUtils.getPgMidForAoaMid(mid));
            }
            dynamicQR = workFlowHelper.createDynamicQR(transBean);
            if (dynamicQR.isSuccessfullyProcessed()) {
                EventUtils.pushTheiaEvents(EventNameEnum.DYNAMIC_QR_GENERATED);
                LOGGER.info("Dynamic Qr is Processed Successfully");
                response.setQrCodeDetails(dynamicQR.getResponse());
            }
        } finally {
            if (input.isFromAoaMerchant()) {
                transBean.getWorkFlowBean().setPaytmMID(mid);
            }
        }
        return dynamicQR;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.CREATE_DYNAMIC_QR;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.CREATE_DYNAMIC_QR_TIME, "2000"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        // PGP-20304 No QR Code should be shown when promo code is present
        if (!ERequestType.DYNAMIC_QR_2FA.equals(inputBean.getRequestType())
                && StringUtils.isNotBlank(inputBean.getPromoCampId())) {
            return false;
        }
        // PGP-20304 End

        if (!inputBean.isEnhancedCashierPageRequest()) {
            if (EChannelId.WEB.getValue().equals(inputBean.getChannelID())
                    && CHECKOUT.equals(inputBean.getPaymentRequestBean().getWorkflow())
                    && inputBean.isDynamicQrRequired() && !inputBean.isEnhanceQrCodeDisabled()) {
                return true;
            } else {
                return false;
            }
        }

        boolean isRunnable = false;
        if (EChannelId.WEB.getValue().equals(inputBean.getChannelID()) && inputBean.isDynamicQrRequired()) {
            if (ERequestType.DYNAMIC_QR_2FA.equals(inputBean.getRequestType()))
                isRunnable = true;
            else if (ERequestType.NATIVE_PAY.equals(inputBean.getRequestType()))
                isRunnable = true;
        }

        boolean ff4jEnabled = Boolean.valueOf(ConfigurationUtil.getProperty(BizConstant.DYNAMIC_QR_FF4J_ENABLED));
        if (ff4jEnabled) {
            Map<String, Object> context = new HashMap<>();
            context.put("mid", inputBean.getPaytmMID());
            context.put("custId", inputBean.getCustID());
            if (inputBean.getUserDetailsBiz() != null) {
                context.put("userId", inputBean.getUserDetailsBiz().getUserId());
            }
            // PGP-22936 It checks if mid is present in old ff4j flag OR if it
            // is present in new one without preference to disable it
            return isRunnable
                    && (iPgpFf4jClient.checkWithdefault("ScanNPay", context, false) && !inputBean
                            .isEnhanceQrCodeDisabled());
        }
        return isRunnable && workFlowHelper.isAllowedOnMidAndCustId(inputBean);
    }

    // todo scan n pay
    /*
     * protected boolean checkForNewFF4jFlag(WorkFlowRequestBean
     * flowRequestBean) { return
     * ff4JUtil.isFeatureEnabled(ENABLE_QR_CODE_ON_ENHANCE
     * ,flowRequestBean.getPaytmMID()) &&
     * !flowRequestBean.isEnhanceQrCodeDisabled(); }
     */
}
