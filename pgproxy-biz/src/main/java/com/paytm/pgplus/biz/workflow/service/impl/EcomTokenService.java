package com.paytm.pgplus.biz.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizPayResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.redis.SessionRedisClientJedisService;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("ecomTokenService")
public class EcomTokenService implements IWorkFlow {
    public static final Logger LOGGER = LoggerFactory.getLogger(EcomTokenService.class);

    private static final SessionRedisClientJedisService SESSION_REDIS_CLIENT_SERVICE = SessionRedisClientJedisService
            .getInstance();

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("seamlessvalidator")
    private IValidator validatorService;

    @Autowired
    private SeamlessCoreService seamlessCoreService;

    @Autowired
    private NativeRetryPaymentUtil nativeRetryPaymentUtil;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    NativeRetryPaymentFlowService nativeRetryPaymentFlowService;

    @Autowired
    MappingUtil mapUtils;

    @Autowired
    private RedisUtil redisUtil;

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {
        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);

        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // If merchant sent token in request then fetch userDetails from OAuth
        GenericCoreResponseBean<UserDetailsBiz> userDetails = null;

        if (!StringUtils.isBlank(flowRequestBean.getToken())) {

            // fetch UserDetails
            if (flowRequestBean.getUserDetailsBiz() != null) {
                userDetails = new GenericCoreResponseBean<>(flowRequestBean.getUserDetailsBiz());
            } else {
                userDetails = workFlowHelper.fetchUserDetails(workFlowTransBean, flowRequestBean.getToken(), false);
            }

            if (!userDetails.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<>(userDetails.getFailureMessage(), userDetails.getResponseConstant());
            }
            workFlowTransBean.setUserDetails(userDetails.getResponse());
            flowRequestBean.setUserDetailsBiz(userDetails.getResponse());
        }

        workFlowHelper.createCardBeanSeamless(workFlowTransBean, flowRequestBean);

        // Cache CC card/IMPS info
        // Cache NB/Mandate In case Of NATIVE_MF AND NATIVE_ST
        final GenericCoreResponseBean<WorkFlowResponseBean> createCacheCardResponse = seamlessCoreService
                .cacheBankCardInfo(flowRequestBean, workFlowTransBean);

        if (createCacheCardResponse != null)
            return createCacheCardResponse;

        workFlowHelper.enrichRequestBeanExtendInfo(workFlowTransBean);
        // Setting pcf
        if (flowRequestBean.isPostConvenience()) {
            GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper
                    .consultBulkFeeResponseForPay(workFlowTransBean, null);
            workFlowTransBean.setPostConvenienceFeeModel(true);
            workFlowTransBean.setConsultFeeResponse(consultFeeResponse.getResponse());
        }

        /*
         * if TransId/AcquirementId is present, this means order has been
         * created for this payment, so now we'll call Pay API
         */
        if (StringUtils.isNotBlank(flowRequestBean.getTransID())) {
            /*
             * call pay API
             */
            LOGGER.info("pay from ECOMTokenSvc");
            final GenericCoreResponseBean<BizPayResponse> payResponse = nativeRetryPaymentFlowService.callPayAPI(
                    flowRequestBean, workFlowTransBean);
            GenericCoreResponseBean<WorkFlowResponseBean> validatePayResponse = nativeRetryPaymentFlowService
                    .validatePayAPIResponse(payResponse, flowRequestBean, workFlowTransBean);
            /*
             * if validatePayResponse is not null, it means Pay API was not
             * processed successfully, so return the error received
             */
            if (validatePayResponse != null) {
                return validatePayResponse;
            }
            nativeRetryPaymentFlowService.setCashierRequestIdInWorkFlowTransBean(payResponse, flowRequestBean,
                    workFlowTransBean);

        } else {
            // Create Order And Pay
            GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPayResponse = workFlowHelper
                    .createOrderAndPay(workFlowTransBean);

            if (!createOrderAndPayResponse.isSuccessfullyProcessed()) {
                LOGGER.error("createOrderAndPay failed: {}", createOrderAndPayResponse.getFailureMessage());

                if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType()))
                        && createOrderAndPayResponse.getResponseConstant() != null
                        && ResponseConstants.RISK_REJECT.equals(createOrderAndPayResponse.getResponseConstant())) {
                    workFlowTransBean.getWorkFlowBean().setPaymentFailureType(NativePaymentFailureType.RISK_REJECT);
                    if (StringUtils.isNotBlank(createOrderAndPayResponse.getAcquirementId())) {
                        nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(),
                                createOrderAndPayResponse.getAcquirementId());
                    }
                }
                if (StringUtils.isNotBlank(createOrderAndPayResponse.getRiskRejectUserMessage())) {
                    GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                            createOrderAndPayResponse.getFailureMessage(),
                            createOrderAndPayResponse.getResponseConstant(),
                            createOrderAndPayResponse.getRiskRejectUserMessage());
                    responseBean.setInternalErrorCode(createOrderAndPayResponse.getInternalErrorCode());
                    return responseBean;
                }
                GenericCoreResponseBean responseBean = new GenericCoreResponseBean<>(
                        createOrderAndPayResponse.getFailureMessage(), createOrderAndPayResponse.getResponseConstant());
                if (ResponseConstants.NEED_RISK_CHALLENGE.getAlipayResultCode().equals(
                        createOrderAndPayResponse.getResponseConstant().getAlipayResultCode())) {
                    nativeRetryPaymentUtil.setTransIdInCache(workFlowTransBean.getWorkFlowBean().getTxnToken(),
                            createOrderAndPayResponse.getAcquirementId());
                    responseBean.setAcquirementId(createOrderAndPayResponse.getAcquirementId());
                }
                return responseBean;
            }

            workFlowTransBean.setCashierRequestId(createOrderAndPayResponse.getResponse().getCashierRequestId());
            workFlowTransBean.setTransID(createOrderAndPayResponse.getResponse().getAcquirementId());
        }

        flowRequestBean.setTransID(workFlowTransBean.getTransID());
        redisUtil
                .pushCashierIdForAcquirementId(workFlowTransBean.getTransID(), workFlowTransBean.getCashierRequestId());

        // Set TxnId in Cache for native Txn
        if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())) {
            nativeRetryPaymentUtil.setTransIdInCache(flowRequestBean.getTxnToken(), workFlowTransBean.getTransID());
        }

        // Fetch Bank Form using Query_PayResult API in case of CC/DC &
        // NetBanking
        GenericCoreResponseBean<QueryPaymentStatus> queryPayResultResponse = null;
        GenericCoreResponseBean<QueryTransactionStatus> queryByAcquirementIdResponse = null;

        if (PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())) {
            queryPayResultResponse = workFlowHelper.fetchPaymentStatus(workFlowTransBean);
            if (!queryPayResultResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(queryPayResultResponse.getFailureMessage(),
                        queryPayResultResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryPaymentStatus(queryPayResultResponse.getResponse());

            String paymentStatus = queryPayResultResponse.getResponse().getPaymentStatusValue();

            if (!PaymentStatus.FAIL.name().equals(paymentStatus)) {
                workFlowTransBean.setPaymentDone(true);
            }
            queryByAcquirementIdResponse = workFlowHelper.fetchTransactionStatus(workFlowTransBean);
            if (!queryByAcquirementIdResponse.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<WorkFlowResponseBean>(
                        queryByAcquirementIdResponse.getFailureMessage(),
                        queryByAcquirementIdResponse.getResponseConstant());
            }
            workFlowTransBean.setQueryTransactionStatus(queryByAcquirementIdResponse.getResponse());

        }

        workFlowResponseBean.setUserDetails(workFlowTransBean.getUserDetails());
        workFlowResponseBean.setQueryPaymentStatus(workFlowTransBean.getQueryPaymentStatus());
        workFlowResponseBean.setQueryTransactionStatus(workFlowTransBean.getQueryTransactionStatus());
        workFlowResponseBean.setPaymentDone(workFlowTransBean.isPaymentDone());
        workFlowResponseBean.setTransID(workFlowTransBean.getTransID());
        workFlowResponseBean.setCashierRequestId(workFlowTransBean.getCashierRequestId());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "EcomTokenFlowService",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean, queryPayResultResponse.getRetryStatus());
    }

}
