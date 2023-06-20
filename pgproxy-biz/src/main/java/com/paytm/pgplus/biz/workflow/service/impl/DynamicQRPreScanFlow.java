package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.utils.DynamicQRCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("dynamicQRPreScanFlow")
public class DynamicQRPreScanFlow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRPreScanFlow.class);

    @Autowired
    @Qualifier("dynamicQrPreScanRequestValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("walletQRCodeServiceImpl")
    private IWalletQRCodeService walletQRCodeService;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {
        LOGGER.info("DynamicQRPreScanFlow:WorkFlowRequestBean : {}", flowRequestBean);

        boolean validToken = false;

        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean(flowRequestBean);

        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean,
                    flowRequestBean.getToken(), true);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            validToken = true;
            // Set user details
            workFlowTransBean.setUserDetails(userDetails.getResponse());
        }

        // CREATE ORDER
        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

        // Consult PayView
        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> merchantConsultView = workFlowHelper
                .consultPayView(workFlowTransBean);
        if (!merchantConsultView.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(merchantConsultView.getFailureMessage(),
                    merchantConsultView.getResponseConstant());
        }
        workFlowTransBean.setMerchantViewConsult(merchantConsultView.getResponse());

        final GenericCoreResponseBean<MidCustIdCardBizDetails> midCustIdCardDetails = workFlowHelper
                .fetchSavedCards(workFlowTransBean);

        if (midCustIdCardDetails.isSuccessfullyProcessed()) {
            workFlowTransBean.setMidCustIdCardBizDetails(midCustIdCardDetails.getResponse());
        }

        flowRequestBean.setTransID(workFlowTransBean.getTransID());

        // Filter Saved cards based on final consult pay view
        workFlowHelper.filterSavedCards(workFlowTransBean);

        LOGGER.info("flowRequestBean for dynamic QR pre scan : {}", flowRequestBean);

        if (!validToken) {
            // LOGGER.info("Token not received so calling QR service");
            GenericCoreResponseBean<QRCodeDetailsResponse> QRDetails = walletQRCodeService
                    .fetchQRCodeDetails(workFlowTransBean);

            LOGGER.info("QRCodeDetailsResponse : {}", QRDetails);

            if (QRDetails != null && QRDetails.isSuccessfullyProcessed()) {
                QRDetails.getResponse().setIsQREnabled(true);
                responseBean.setQrCodeDetails(QRDetails.getResponse());
            }
        }

        theiaTransactionalRedisUtil.set(getQrOrderKey(flowRequestBean.getPaytmMID(), flowRequestBean.getOrderID()),
                workFlowTransBean.getTransID());

        responseBean.setTransID(workFlowTransBean.getTransID());
        responseBean.setMerchnatViewResponse(workFlowTransBean.getMerchantViewConsult());
        responseBean.setmIdCustIdCardBizDetails(workFlowTransBean.getMidCustIdCardBizDetails());
        responseBean.setAllowedPayMode(workFlowTransBean.getAllowedPayMode());
        responseBean.setAddAndPayViewResponse(workFlowTransBean.getAddAndPayViewConsult());
        responseBean.setUserDetails(workFlowTransBean.getUserDetails());
        responseBean.setAccountBalanceResponse(workFlowTransBean.getAccountBalanceResponse());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "DynamicQRPreScanFlow",
                responseBean.getTransID());

        // dynamicQRCoreService.pushPostOrderPayload(workFlowTransBean,
        // flowRequestBean);

        return new GenericCoreResponseBean<>(responseBean);
    }

    private static String getQrOrderKey(String mid, String orderId) {
        return "QR_ORDER_" + mid + orderId;
    }
}