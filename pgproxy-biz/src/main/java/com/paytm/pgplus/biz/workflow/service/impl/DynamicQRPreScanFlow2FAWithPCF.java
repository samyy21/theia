package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.DynamicQRCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("Duplicates")
@Service("dynamicQRPreScanFlow2FAWithPCF")
public class DynamicQRPreScanFlow2FAWithPCF implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRPreScanFlow2FAWithPCF.class);

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
        workFlowTransBean.setPostConvenienceFeeModel(true);
        if (null != workFlowTransBean.getWorkFlowBean()) {
            workFlowTransBean.getWorkFlowBean().setRequestType(ERequestType.DYNAMIC_QR_2FA);
            if (null != workFlowTransBean.getWorkFlowBean().getExtendInfo()) {
                workFlowTransBean.getWorkFlowBean().getExtendInfo()
                        .setRequestType(ERequestType.DYNAMIC_QR_2FA.getType());
            }
        }

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

        final GenericCoreResponseBean<ConsultFeeResponse> consultFee = workFlowHelper
                .consultBulkFeeResponse(workFlowTransBean);

        if (!consultFee.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultFee.getFailureMessage(), consultFee.getResponseConstant());
        }

        workFlowTransBean.setConsultFeeResponse(consultFee.getResponse());

        GenericCoreResponseBean<Boolean> updateAmountResponse = updateTransactionAmountWithPCF(
                consultFee.getResponse(), flowRequestBean);

        if (!updateAmountResponse.isSuccessfullyProcessed()) {
            LOGGER.error("Consult Fee Response is Invalid {}", consultFee.getResponse());
            return new GenericCoreResponseBean<>("Consult Fee Response is Invalid");
        }

        LOGGER.info("flowRequestBean for dynamic QR pre scan : {}", flowRequestBean);

        if (!validToken) {
            LOGGER.info("Token not received so calling QR service");
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
        responseBean.setConsultFeeResponse(workFlowTransBean.getConsultFeeResponse());
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "DynamicQRPreScanFlow",
                responseBean.getTransID());

        // dynamicQRCoreService.pushPostOrderPayload(workFlowTransBean,
        // flowRequestBean);

        return new GenericCoreResponseBean<>(responseBean);
    }

    private static String getQrOrderKey(String mid, String orderId) {
        return "QR_ORDER_" + mid + orderId;
    }

    private GenericCoreResponseBean<Boolean> updateTransactionAmountWithPCF(
            final ConsultFeeResponse consultFeeResponse, final WorkFlowRequestBean flowRequestBean) {

        Map<EPayMethod, ConsultDetails> consultDetailsMap = consultFeeResponse.getConsultDetails();

        if (Objects.isNull(consultDetailsMap) || consultDetailsMap.size() == 0
                || !consultDetailsMap.containsKey(EPayMethod.BALANCE)) {
            LOGGER.error("Consult response is Invalid {}", consultFeeResponse);
            return new GenericCoreResponseBean("Invalid Consult Response");
        }

        ConsultDetails consultDetails = consultDetailsMap.get(EPayMethod.BALANCE);

        flowRequestBean.setTxnAmount(consultDetails.getTotalTransactionAmount().multiply(new BigDecimal(100))
                .toPlainString());
        flowRequestBean.setChargeAmount(workFlowHelper.calculateChargeAmountInPaise(consultDetails));

        return new GenericCoreResponseBean<>(true);
    }

}