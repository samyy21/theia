package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.DynamicQRCoreService;
import com.paytm.pgplus.biz.utils.NativeRetryPaymentUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.biz.workflow.service.util.LinkPaymentConsultUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.link.PaymentConsultResponseBody;
import com.paytm.pgplus.common.model.link.PaymentConsultResponseBody;
import com.paytm.pgplus.common.util.LinkPaymentUtil;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.AOA_DQR;

@SuppressWarnings("Duplicates")
@Service("dynamicQrFastForwardFlow")
public class DynamicQRFastForwardFlow implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRFastForwardFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("dynamicQrFastForwardRequestValidator")
    private IValidator dynamicQrFastForwardRequestValidator;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    private WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    LinkPaymentConsultUtil linkPaymentConsultUtil;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setEnvInfoReqBean(flowRequestBean.getEnvInfoReqBean());
        workFlowTransBean.setChannelInfo(flowRequestBean.getChannelInfo());
        workFlowTransBean.setTransID(flowRequestBean.getTransID());
        workFlowTransBean.setProductCode(flowRequestBean.getProductCode());

        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, dynamicQrFastForwardRequestValidator);
        LOGGER.info("dynamicQrFastForward Request Validated {} ", flowRequestBean.getRequestType());
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        if (flowRequestBean.isPostConvenience()
                && StringUtils.isBlank(workFlowTransBean.getWorkFlowBean().getChargeAmount())) {
            LOGGER.info("Merchant is PCF , fetching fee details");
            GenericCoreResponseBean<ConsultFeeResponse> consultBossResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, EPayMethod.BALANCE);
            if (!consultBossResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(consultBossResponse.getFailureMessage());
            }
            workFlowTransBean.setConsultFeeResponse(consultBossResponse.getResponse());
            workFlowTransBean.getWorkFlowBean().setChargeAmount(
                    workFlowRequestCreationHelper.fetchChargeAmountIfApplicable(workFlowTransBean));
        }
        // If merchant sent token in request then fetch userDetails from OAuth
        if (!StringUtils.isBlank(flowRequestBean.getToken())) {
            // fetch UserDetails
            final GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetails(
                    workFlowTransBean, flowRequestBean.getToken(), false);
            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            LOGGER.info("User Data for dynamicQrFastForward {} ", userDetails);
            workFlowTransBean.setUserDetails(userDetails.getResponse());

        } else {
            return new GenericCoreResponseBean<>("Invalid request data", ResponseConstants.SYSTEM_ERROR);
        }

        // Consult link service incase of link payment
        // if(workFlowTransBean.getWorkFlowBean() != null &&
        // workFlowTransBean.getWorkFlowBean().getPaymentRequestBean() != null
        // &&
        // (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getRequestType())
        // ||
        // ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getRequestType())
        // ||
        // StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean().getLinkId())))
        // {
        //
        // PaymentConsultResponseBody linkPaymentConsultResponseBody =
        // linkPaymentConsultUtil.getLinkPaymentConsultResponse(workFlowTransBean.getWorkFlowBean().getPaymentRequestBean());
        // if (linkPaymentConsultResponseBody != null &&
        // FacadeConstants.FAILED.equalsIgnoreCase(linkPaymentConsultResponseBody.getResultInfo().getResultStatus())){
        // return new
        // GenericCoreResponseBean<>(linkPaymentConsultResponseBody.getResultInfo().getResultMsg(),
        // ResponseConstants.SYSTEM_ERROR);
        // }
        // }

        // Pay
        long startTime = System.currentTimeMillis();
        // Create Order And Pay if transId was not found on PG2/P+ but DQR order
        // was created on AOA.
        if (StringUtils.equals(AOA_DQR, flowRequestBean.getTxnFlow())) {
            LOGGER.info("Create Order And Pay called from DynamicQRFastForwardFlow for AOA Dynamic QR");
            final GenericCoreResponseBean<CreateOrderAndPayResponseBean> copResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);
            LOGGER.info("CreateOrderAndPayResponseBean: {}", copResponse);

            if (!copResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Create Order And Pay call failed: {}", copResponse.getFailureMessage());
                GenericCoreResponseBean<WorkFlowResponseBean> responseBean = new GenericCoreResponseBean<>(
                        copResponse.getFailureMessage(), copResponse.getResponseConstant());
                responseBean.setInternalErrorCode(copResponse.getInternalErrorCode());
                return responseBean;
            }

            workFlowTransBean.setCashierRequestId(copResponse.getResponse().getCashierRequestId());
            if (copResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(copResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            workFlowTransBean.setTransID(copResponse.getResponse().getAcquirementId());
            // workFlowResponseBean.setTransID(flowRequestBean.getTransID());
            workFlowHelper.pushDynamicQrPaymentEvent(flowRequestBean.getPayMethod());
            LOGGER.info("Create Order And Pay API called successfully");
        } else {
            if (StringUtils.isNotBlank(flowRequestBean.getTipAmount()))
                workFlowTransBean.setModifyOrderRequired(true);
            LOGGER.info("Pay called from DynamicQRFastForwardFlow");
            final GenericCoreResponseBean<BizPayResponse> payResponse = workFlowHelper.pay(workFlowTransBean);
            if (!payResponse.isSuccessfullyProcessed()) {
                LOGGER.info("Pay API call failed due to : {}", payResponse.getFailureMessage());
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(payResponse.getFailureMessage(),
                        payResponse.getResponseConstant());
                responseBean.setInternalErrorCode(payResponse.getInternalErrorCode());
                return responseBean;
            }
            workFlowTransBean.setCashierRequestId(payResponse.getResponse().getCashierRequestID());
            if (payResponse.getResponse().getSecurityPolicyResult() != null) {
                workFlowTransBean.setRiskResult(payResponse.getResponse().getSecurityPolicyResult().getRiskResult());
            }
            workFlowTransBean.setTransID(flowRequestBean.getTransID());
            workFlowHelper.pushDynamicQrPaymentEvent(flowRequestBean.getPayMethod());

            LOGGER.info("Pay API called successfully with response : {}", payResponse);
        }

        /**
         * Commented due to issue with EDC
         * dynamicQRCoreService.pushPostPaymentPayload(workFlowTransBean,
         * flowRequestBean);
         */

        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = workFlowHelper
                .fetchPaymentStatus(workFlowTransBean);
        if (!queryPayResultResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(queryPayResultResponse.getFailureMessage(),
                    queryPayResultResponse.getResponseConstant());
        }
        workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());
        String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

        // Need to close order in case paymentStatus is FAIL
        if (PaymentStatus.FAIL.toString().equals(paymentStatus)) {
            if (flowRequestBean.isDynamicQREdcRequest() && nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean)) {
                LOGGER.info("not closing order isDynamicQREdcRequest PaymentStatus.FAIL");
            } else {
                LOGGER.info("Close order will be call as Payment Status is FAIL");

                final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                        .closeOrder(workFlowTransBean);
                if (!cancelOrder.isSuccessfullyProcessed()) {
                    LOGGER.error("Close/Cancel order failed due to : {}", cancelOrder.getFailureMessage());
                }
            }
            workFlowTransBean.getQueryPaymentStatus().setPaymentStatusValue(PaymentStatus.FAIL.name());
        } else if (PaymentStatus.PROCESSING.toString().equals(paymentStatus)) {
            if (flowRequestBean.isDynamicQREdcRequest() && nativeRetryPaymentUtil.canPaymentRetry(workFlowTransBean)) {
                LOGGER.info("not closing orderisDynamicQREdcRequest PaymentStatus.PROCESSING");
            } else {
                // LOGGER.info("Close order will be call as Payment Status is PROCESSING");

                if (flowRequestBean.isOfflineFastForwardRequest()
                        && System.currentTimeMillis() - startTime > Long
                                .parseLong(BizConstant.PLATFORM_PLUS_CLOSE_ORDER_TIME) * 1000) {
                    LOGGER.info("Calling close order");
                    workFlowHelper.triggerCloseOrderPulses(workFlowTransBean);
                }
            }
        }

        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = workFlowHelper
                .fetchTransactionStatus(workFlowTransBean);

        LOGGER.info("queryByAcquirementIdResponse : {}", queryByAcquirementIdResponse);

        if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getFailureMessage(),
                    queryByAcquirementIdResponse.getResponseConstant());
        }

        workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());
        workFlowTransBean.setPaymentDone(true);

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setRiskResult(workFlowTransBean.getRiskResult());
        dynamicQRCoreService.putQRDataINCache(workFlowTransBean, workFlowResponseBean);

        // to support v1/transactionStatus API
        dynamicQRCoreService.putCashierRequestIdAndPaymentTypeIdInCache(workFlowTransBean);
        // dynamicQRCoreService.pushPostTransactionPayload(workFlowTransBean,
        // flowRequestBean);
        workFlowResponseBean.setWorkFlowRequestBean(flowRequestBean);
        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "dynamicQrFastForwardFlow", workFlowResponseBean);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }
}
