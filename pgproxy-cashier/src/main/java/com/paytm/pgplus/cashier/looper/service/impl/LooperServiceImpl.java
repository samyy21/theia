package com.paytm.pgplus.cashier.looper.service.impl;

import com.paytm.pgplus.cashier.util.ConfigurationUtil;
import com.paytm.pgplus.cashier.util.EventUtils;
import com.paytm.pgplus.cashier.util.RouteUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.EncryptionUtils;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.exception.RouterException;
import com.paytm.pgplus.facade.paymentrouter.service.IRouteClient;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierLopperMapper;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.looper.service.ILooperService;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.CashierResponseCodeDetails;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequest;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequestBody;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fund.models.FundOrder;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequest;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequestBody;
import com.paytm.pgplus.facade.fund.models.request.QueryByFundOrderIdRequest;
import com.paytm.pgplus.facade.fund.models.request.QueryByFundOrderIdRequestBody;
import com.paytm.pgplus.facade.fund.models.response.QueryByFundOrderIdResponse;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequestBody;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.looperclient.exception.LooperException;
import com.paytm.pgplus.looperclient.servicehandler.impl.LooperRequestHandlerImpl;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Amit.Dubey
 * @since March 9, 2016
 *
 */
@Component("looperServiceImpl")
public class LooperServiceImpl implements ILooperService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LooperServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    LooperRequestHandlerImpl looperRequestHandler;

    @Autowired
    ICashierCacheService cashierCacheServiceImpl;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    IAcquiringOrder acquiringOrder;

    @Autowired
    ITopup topupImpl;

    @Autowired
    RouteUtil routeUtil;

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_3D_BANK_FORM)
    public CashierPaymentStatus fetchBankForm(String cashierRequestId) throws CashierCheckedException {

        try {
            Map<String, String> metaData = new LinkedHashMap<>();
            LOGGER.info("Request received : fetch the bank form : cashier request id : {}", cashierRequestId);
            metaData.put("cashierRequestId", cashierRequestId);
            metaData.put("eventMsg", "Request received : fetch the bank form : cashier request ");
            EventUtils.pushTheiaEvents(EventNameEnum.REQUEST_BANK_FORM, metaData);
            ApiFunctions apiFunction = ApiFunctions.QUERY_PAYRESULT;
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
            PayResultQueryRequestBody body = new PayResultQueryRequestBody(cashierRequestId);

            PayResultQueryRequest requestData = new PayResultQueryRequest(head, body);

            PayResultQueryResponse payResultQueryResponse = looperRequestHandler.fetch3DBankForm(requestData, null);

            validatePayResultQueryResponse(payResultQueryResponse);

            ResultInfo resultInfo = payResultQueryResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                CashierPaymentStatus status = CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse
                        .getBody());
                if ((null != payResultQueryResponse.getBody())
                        && StringUtils.isNotBlank(payResultQueryResponse.getBody().getWebFormContext())) {

                    populateWebFormContext(cashierRequestId, payResultQueryResponse, status);

                } else {
                    if (status != null && !checkIfInstaCodeInNotAllowedCodes(status.getInstErrorCode())) {
                        status.setBankFormFetchFailed(true);
                    }
                    logBankFormFailureResponse(status);
                }

                return status;
            } else {
                String resultInfoStr = mapper.writeValueAsString(resultInfo);
                throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
            }
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException("Exception occurred while working with facade for fetching bank form", e);
        } catch (LooperException e) {
            throw new CashierCheckedException("Exception occurred while working with looper for fetching bank form", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Internal system error", e);
        }
    }

    private void populateWebFormContext(String cashierRequestId, PayResultQueryResponse payResultQueryResponse,
            CashierPaymentStatus status) {
        if (payResultQueryResponse.getBody().getWebFormContext().startsWith(CommonConstants.BANK_ENCRYPTED_FORM_KEY)) {
            // LOGGER.info("Got encrypted bank form for cashierRequestId : {} ",
            // cashierRequestId);
            String encryptedWebFormContext = payResultQueryResponse.getBody().getWebFormContext()
                    .substring(CommonConstants.BANK_ENCRYPTED_FORM_KEY.length());
            boolean bankFormFetched = false;
            if (StringUtils.isNotBlank(encryptedWebFormContext)) {
                String webFormContext = EncryptionUtils.decrypt(encryptedWebFormContext);
                if (StringUtils.isNotBlank(webFormContext)) {
                    bankFormFetched = true;
                    status.setWebFormContext(webFormContext);
                    Map<String, String> metaData = new LinkedHashMap<>();
                    LOGGER.info("Response received : fetch the bank form : cashier request id : {} after decryption",
                            cashierRequestId);
                    metaData.put("cashierRequestId", cashierRequestId);
                    metaData.put("eventMsg", "Response received : fetch the bank form : cashier request id : "
                            + cashierRequestId + " after decryption");
                    EventUtils.pushTheiaEvents(EventNameEnum.BANK_RESPONSE_RECEIVED, metaData);

                } else {
                    LOGGER.error("Unable to decrypt bank form : {} for cashier request id : {}", payResultQueryResponse
                            .getBody().getWebFormContext(), cashierRequestId);
                }

            } else {
                LOGGER.error("Unable to get encrypted bank form for cashier request id : {}", cashierRequestId);
            }
            if (!bankFormFetched) {
                status.setWebFormContext(null);
                LOGGER.error("Unable to receive bank form : cashier request id : {}", cashierRequestId);
                if (status != null && !checkIfInstaCodeInNotAllowedCodes(status.getInstErrorCode())) {
                    status.setBankFormFetchFailed(true);
                }
                logBankFormFailureResponse(status);
            }
        } else {
            LOGGER.info("Response received : fetch the bank form : cashier request id : {}", cashierRequestId);
            Map<String, String> metaData = new LinkedHashMap<>();
            metaData.put("cashierRequestId", cashierRequestId);
            metaData.put("eventMsg", "Response received : fetch the bank form : cashier request");
            EventUtils.pushTheiaEvents(EventNameEnum.BANK_RESPONSE_RECEIVED, metaData);
        }
    }

    private void logBankFormFailureResponse(CashierPaymentStatus status) {
        Map keyMap = new HashMap();
        if (status != null && status.isBankFormFetchFailed()) {
            keyMap.put("status", "BANK_FORM_FETCH_FAIL");
            keyMap.put("InBlackListCodes", false);
        } else {
            keyMap.put("status", "BANK_FORM_FETCH_FAIL");
            keyMap.put("InBlackListCodes", true);
        }
        keyMap.put("InstaErrorCode", status != null ? status.getInstErrorCode() : null);
        EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
    }

    private boolean checkIfInstaCodeInNotAllowedCodes(String instErrorCode) {
        String notAllowedEntries = ConfigurationUtil.getTheiaProperty(CashierConstant.INTERNAL_RETRY_BLACKLIST_CODES);
        if (StringUtils.isNotEmpty(notAllowedEntries) && StringUtils.isNotEmpty(instErrorCode)) {
            String[] codes = notAllowedEntries.split(",");
            if (codes != null && codes.length > 0)
                for (String code : codes) {
                    if (instErrorCode.equalsIgnoreCase(StringUtils.trim(code))) {
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_TRANSACTION_STATUS)
    public CashierPaymentStatus fetchPaymentStatus(String acquirementId, CashierRequest cashierRequest)
            throws CashierCheckedException {

        try {
            PayResultQueryRequest requestData = getRequestData(cashierRequest);
            PayResultQueryResponse payResultQueryResponse = looperRequestHandler.fetchPaymentStatus(requestData, null);

            validatePayResultQueryResponse(payResultQueryResponse);

            CashierPaymentStatus cashierPaymentStatus = processPayResultQueryResponse(payResultQueryResponse,
                    cashierRequest, acquirementId);
            return cashierPaymentStatus;
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching payment status", e);
        } catch (LooperException e) {
            throw new CashierCheckedException(
                    "Exeception occurred while working with looper for fetching payment status", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Internal system error : ", e);
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_TRANSACTION_STATUS)
    public void fetchPaymentStatusAsync(String acquirementId, CashierRequest cashierRequest, GenericCallBack callBack)
            throws CashierCheckedException {
        try {
            PayResultQueryRequest requestData = getRequestData(cashierRequest);
            looperRequestHandler.fetchPaymentStatusAsync(
                    requestData,
                    null,
                    payResultQueryResponse -> {
                        try {
                            if (payResultQueryResponse instanceof Exception) {
                                throw new CashierCheckedException((Exception) payResultQueryResponse);
                            }
                            validatePayResultQueryResponse((PayResultQueryResponse) payResultQueryResponse);
                            CashierPaymentStatus cashierPaymentStatus = processPayResultQueryResponse(
                                    (PayResultQueryResponse) payResultQueryResponse, cashierRequest, acquirementId);
                            callBack.processResponse(cashierPaymentStatus);
                        } catch (CashierCheckedException e) {
                            callBack.processResponse(e);
                        } catch (JsonProcessingException e) {
                            callBack.processResponse(new CashierCheckedException("Internal system error : ", e));
                        }

                    });
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching payment status", e);
        } catch (LooperException e) {
            throw new CashierCheckedException(
                    "Exeception occurred while working with looper for fetching payment status", e);
        }
    }

    private PayResultQueryRequest getRequestData(CashierRequest cashierRequest) throws FacadeCheckedException {
        ApiFunctions apiFunction = ApiFunctions.QUERY_PAYRESULT;
        if (cashierRequest.isFromAoaMerchant()) {
            apiFunction = ApiFunctions.AOA_QUERY_PAYRESULT;

        }
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
        PayResultQueryRequestBody body = new PayResultQueryRequestBody(cashierRequest.getLooperRequest()
                .getCashierRequestId(), cashierRequest.isFromAoaMerchant(), cashierRequest.getRoute());

        return new PayResultQueryRequest(head, body);
    }

    private CashierPaymentStatus processPayResultQueryResponse(PayResultQueryResponse payResultQueryResponse,
            CashierRequest cashierRequest, String acquirementId) throws CashierCheckedException,
            JsonProcessingException {
        ResultInfo resultInfo = payResultQueryResponse.getBody().getResultInfo();

        if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
            if (PaymentStatus.FAIL == payResultQueryResponse.getBody().getPaymentStatus()) {
                CashierResponseCodeDetails cashierResponseCodeDetails = cashierUtilService
                        .getMerchantResponseCode(payResultQueryResponse.getBody().getInstErrorCode());

                if (cashierResponseCodeDetails.isRetry()) {
                    return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody(),
                            cashierResponseCodeDetails);
                }
                if (cashierRequest.isFundOrder()) {
                    if (Routes.PG2.equals(cashierRequest.getRoute()))
                        closeFundOrder(cashierRequest.getPaytmMerchantId(), cashierRequest.getLooperRequest()
                                .getFundOrderId(), cashierRequest.getEnvInfo(), cashierRequest.getRoute());
                    else
                        closeFundOrder(null, cashierRequest.getLooperRequest().getFundOrderId(),
                                cashierRequest.getEnvInfo(), null);
                } else {
                    String alipayMid = payResultQueryResponse.getBody().getExtendInfo()
                            .get(CashierConstant.EXT_INFO_ALIPAY_MERCH_ID);
                    if (StringUtils.isNotBlank(cashierRequest.getDummyAlipayMid())) {
                        alipayMid = cashierRequest.getDummyAlipayMid();
                    }
                    if (Routes.PG2.equals(cashierRequest.getRoute()))
                        closeOrder(alipayMid, acquirementId, cashierRequest.isFromAoaMerchant(),
                                cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
                    else
                        closeOrder(alipayMid, acquirementId, cashierRequest.isFromAoaMerchant(), null, null);
                }
                CashierPaymentStatus cashierPaymentStatus = CashierLopperMapper.buildCashierPaymentStatus(
                        payResultQueryResponse.getBody(), cashierResponseCodeDetails);

                return cashierPaymentStatus;
            }

            return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody());
        } else {
            String resultInfoStr = mapper.writeValueAsString(resultInfo);
            throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.LOOPER_FETCH_TRANSACTION_STATUS)
    public CashierTransactionStatus fetchTrasactionStatusForAcquirementId(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAOAMerchant) throws CashierCheckedException {

        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_ACQUIREMENTID);
            QueryByAcquirementIdRequestBody body = new QueryByAcquirementIdRequestBody(merchantId, acquirementId,
                    needFullInfo, isFromAOAMerchant);

            QueryByAcquirementIdRequest requestData = new QueryByAcquirementIdRequest(head, body);

            QueryByAcquirementIdResponse queryByAcquirementIdResponse = looperRequestHandler.fetchTransactionStatus(
                    requestData, null);

            validateQueryByAcquirementIdResponse(queryByAcquirementIdResponse);
            ResultInfo resultInfo = queryByAcquirementIdResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                return CashierLopperMapper.buildCashierTrasactionStatus(queryByAcquirementIdResponse.getBody());
            } else {
                throw new CashierCheckedException("Result Info received  : " + mapper.writeValueAsString(resultInfo));
            }

        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching transaction status", e);
        } catch (LooperException e) {
            throw new CashierCheckedException(
                    "Exeception occurred while working with looper for fetching transaction status", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Process failed : Internal system error", e);
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_QUERY_FOR_FUND_ORDER_STATUS)
    public CashierFundOrderStatus fetchFundOrderStatus(String fundOrderId, String paytmMerchantId, Routes route)
            throws CashierCheckedException {

        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_FUNDSTATUS);
            QueryByFundOrderIdRequestBody body = new QueryByFundOrderIdRequestBody(fundOrderId);
            body.setPaytmMerchantId(paytmMerchantId);
            body.setRoute(Routes.PG2);
            QueryByFundOrderIdRequest requestData = new QueryByFundOrderIdRequest(body, head);

            QueryByFundOrderIdResponse fundOrderIdResponse = looperRequestHandler.queryByFundOrderId(requestData, null);

            validateFundOrderIdResponse(fundOrderIdResponse);

            ResultInfo resultInfo = fundOrderIdResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                FundOrder fundOrder = fundOrderIdResponse.getBody().getFundOrder();
                return CashierLopperMapper.buildCashierFundOrderStatus(fundOrder);
            } else {
                String resultInfoStr = mapper.writeValueAsString(resultInfo);
                throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
            }
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Facade : Exeception occurred while working with looper for fetching fund order status", e);
        } catch (LooperException e) {
            throw new CashierCheckedException(
                    "Looper : Exeception occurred while working with looper for fetching fund order status", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Internal system error : ", e);
        }
    }

    @Override
    public CashierTransactionStatus fetchTrasactionStatusForAcquirementId(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAoaMerchant, String paytmMerchantId, Routes route)
            throws CashierCheckedException {
        if (StringUtils.isBlank(paytmMerchantId)) {
            return fetchTrasactionStatusForAcquirementId(merchantId, acquirementId, needFullInfo, isFromAoaMerchant);
        }
        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_ACQUIREMENTID);
            QueryByAcquirementIdRequestBody body = new QueryByAcquirementIdRequestBody(merchantId, acquirementId,
                    needFullInfo, isFromAoaMerchant, route, paytmMerchantId);

            QueryByAcquirementIdRequest requestData = new QueryByAcquirementIdRequest(head, body);

            QueryByAcquirementIdResponse queryByAcquirementIdResponse = looperRequestHandler.fetchTransactionStatus(
                    requestData, null);

            validateQueryByAcquirementIdResponse(queryByAcquirementIdResponse);
            ResultInfo resultInfo = queryByAcquirementIdResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                return CashierLopperMapper.buildCashierTrasactionStatus(queryByAcquirementIdResponse.getBody());
            } else {
                throw new CashierCheckedException("Result Info received  : " + mapper.writeValueAsString(resultInfo));
            }

        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching transaction status", e);
        } catch (LooperException e) {
            throw new CashierCheckedException(
                    "Exeception occurred while working with looper for fetching transaction status", e);
        } catch (JsonProcessingException e) {
            throw new CashierCheckedException("Process failed : Internal system error", e);
        } catch (RouterException e) {
            throw new CashierCheckedException("Exception occured while working with facade for fetching route ", e);
        }
    }

    /**
     * @param payResultQueryResponse
     * @throws CashierCheckedException
     */
    private void validatePayResultQueryResponse(PayResultQueryResponse payResultQueryResponse)
            throws CashierCheckedException {
        if (null == payResultQueryResponse) {
            throw new CashierCheckedException("Process failed : payResultQueryResponse received as null");
        }

        if (null == payResultQueryResponse.getBody()) {
            throw new CashierCheckedException("Process failed : payResultQueryResponse body received as null");
        }

        if (null == payResultQueryResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException(
                    "Process failed : payResultQueryResponse body Result Info received as null");
        }
    }

    /**
     * @param fundOrderIdResponse
     * @throws CashierCheckedException
     */
    private void validateFundOrderIdResponse(QueryByFundOrderIdResponse fundOrderIdResponse)
            throws CashierCheckedException {
        if (null == fundOrderIdResponse) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse received as null");
        }

        if (null == fundOrderIdResponse.getBody()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body received as null");
        }

        if (null == fundOrderIdResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body Result Info received as null");
        }
    }

    /**
     * @param queryByAcquirementIdResponse
     * @throws CashierCheckedException
     */
    private void validateQueryByAcquirementIdResponse(QueryByAcquirementIdResponse queryByAcquirementIdResponse)
            throws CashierCheckedException {
        if (null == queryByAcquirementIdResponse) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse received as null");
        }

        if (null == queryByAcquirementIdResponse.getBody()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body received as null");
        }

        if (null == queryByAcquirementIdResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException("Process failed : fundOrderIdResponse body Result Info received as null");
        }
    }

    private void closeOrder(String merchantId, String acquirementId, boolean fromAoaMerchant, String paytmMerchantId,
            Routes routes) {
        String closeReason = "Payment failed for the acquirementId : " + acquirementId;

        try {
            ApiFunctions apiFunction = ApiFunctions.CLOSE_ORDER;
            if (fromAoaMerchant) {
                apiFunction = ApiFunctions.AOA_CLOSE_ORDER;
            }
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
            CloseRequestBody body = new CloseRequestBody(acquirementId, merchantId, closeReason, fromAoaMerchant);
            body.setPaytmMerchantId(paytmMerchantId);
            body.setRoute(routes);
            CloseRequest closeRequest = new CloseRequest(head, body);

            acquiringOrder.closeOrder(closeRequest);
        } catch (Exception e) {
            LOGGER.error("Unable to close the order for the acquirementId : {}", acquirementId);
        }
    }

    private void closeFundOrder(String paytmMerchantId, String fundOrderId, EnvInfoRequestBean envInfo, Routes route) {
        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CLOSE_FUND_ORDER);
            CloseFundRequestBody body = new CloseFundRequestBody(fundOrderId, envInfo);
            body.setPaytmMerchantId(paytmMerchantId);
            body.setRoute(Routes.PG2);
            CloseFundRequest closeRequest = new CloseFundRequest(head, body);
            topupImpl.closeFundOrder(closeRequest);
        } catch (Exception e) {
            LOGGER.error("Unable to close the order for the fundOrderId : {}", fundOrderId);
        }
    }
}