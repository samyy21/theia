/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.order.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdResponseBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDRequestBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.BizResultInfo;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.acquiring.models.Order.OrderBuilder;
import com.paytm.pgplus.facade.acquiring.models.request.*;
import com.paytm.pgplus.facade.acquiring.models.response.*;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.enums.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.enums.FundType;
import com.paytm.pgplus.facade.fund.models.FundOrder;
import com.paytm.pgplus.facade.fund.models.request.*;
import com.paytm.pgplus.facade.fund.models.response.CloseFundResponse;
import com.paytm.pgplus.facade.fund.models.response.CreateTopupFromMerchantResponse;
import com.paytm.pgplus.facade.fund.models.response.CreateTopupResponse;
import com.paytm.pgplus.facade.fund.models.response.FundUserOrderQueryByMerchantRequestIdResponse;
import com.paytm.pgplus.facade.merchantlimit.utils.MerchantLimitUtil;
import com.paytm.pgplus.facade.payment.models.ChannelPreference;
import com.paytm.pgplus.facade.payment.models.PayOptionBill;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.payment.models.PreferenceValue;
import com.paytm.pgplus.facade.payment.models.response.PayResponse;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.PaymentAdapterUtil;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.MoneyData;
import com.paytm.pgplus.pgproxycommon.models.PayOption;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus.QueryTransactionStatusBuilder;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.RiskRejectInfoCodesAndMessages;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static com.paytm.pgplus.facade.enums.ProductCodes.EDCPayConfirmAcquiringProd;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_EXTENDINFO_PROFILING_IN_ORDER;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_EXTENDINFO_PROFILING_IN_PAYOPTION;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_STATIC_QR_FLOW_EXTENDINFO_PROFILING;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_UPIPSP_FLOW_EXTENDINFO_PROFILING;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_EXTEND_INFO_KEYS_TO_REMOVE;

/**
 * @author amitdubey
 * @date Jan 17, 2017
 */
@Component("orderHelper")
public class OrderHelper {

    @Autowired
    private Ff4jUtils ff4jUtil;

    @Autowired
    private FailureLogUtil failureLogUtil;

    private static FailureLogUtil failureLogUtils;

    private static Ff4jUtils ff4jUtils;

    @PostConstruct
    private void init() {
        ff4jUtils = this.ff4jUtil;
        failureLogUtils = this.failureLogUtil;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHelper.class);

    private static final String TXN_TYPE_ADDNPAY = "ADDNPAY";

    public static GenericCoreResponseBean<CreateTopupRequest> createTopUpRequest(
            final CreateTopUpRequestBizBean createTopUpRequestBizBean) {

        final String currency = createTopUpRequestBizBean.getCurrency();
        final String amount = createTopUpRequestBizBean.getTxnAmount();
        final String payerUserId = createTopUpRequestBizBean.getPayerUserId();
        final String requestId = createTopUpRequestBizBean.getRequestId();
        final String notificationUrl = createTopUpRequestBizBean.getNotificationUrl();
        final String merchantId = createTopUpRequestBizBean.getMerchantId();
        final Map<String, String> extendInfo = AlipayRequestUtils.getExtendeInfoMap(createTopUpRequestBizBean
                .getExtInfoReqBean());

        try {
            final FundType fundType = FundType.getFundTypeByType(createTopUpRequestBizBean.getFundType().getValue());
            final AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CREATE_TOPUP);
            final Money fundAmount = AlipayRequestUtils.getMoney(currency, amount);
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(createTopUpRequestBizBean.getEnvInfoBean());
            final CreateTopupRequestBody body = extendInfo == null ? new CreateTopupRequestBody(fundType, fundAmount,
                    payerUserId, requestId, notificationUrl, merchantId, envInfo) : new CreateTopupRequestBody(
                    fundType, fundAmount, payerUserId, requestId, notificationUrl, merchantId, envInfo, extendInfo);
            final CreateTopupRequest createTopupRequest = new CreateTopupRequest(head, body);
            LOGGER.debug("Create TopUP Request Object : {}", createTopupRequest);
            return new GenericCoreResponseBean<CreateTopupRequest>(createTopupRequest);
        } catch (final Exception e) {
            return new GenericCoreResponseBean<CreateTopupRequest>(e.getMessage());
        }
    }

    public static GenericCoreResponseBean<CreateTopupFromMerchantRequest> createTopUpFromMerchantRequest(
            final CreateTopUpRequestBizBean createTopUpRequestBizBean) {
        final String currency = createTopUpRequestBizBean.getCurrency();
        final String amount = createTopUpRequestBizBean.getTxnAmount();
        final String payerUserId = createTopUpRequestBizBean.getPayerUserId();
        final String requestId = createTopUpRequestBizBean.getRequestId();
        final String notificationUrl = createTopUpRequestBizBean.getNotificationUrl();
        final String merchantId = createTopUpRequestBizBean.getMerchantId();
        final Map<String, String> extendInfo = AlipayRequestUtils.getExtendeInfoMap(createTopUpRequestBizBean
                .getExtInfoReqBean());

        try {
            final FundType fundType = FundType.getFundTypeByType(createTopUpRequestBizBean.getFundType().getValue());
            final AlipayExternalRequestHeader head = RequestHeaderGenerator
                    .getHeader(ApiFunctions.CREATE_TOPUP_FROM_MERCHANT);
            final Money fundAmount = AlipayRequestUtils.getMoney(currency, amount);
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(createTopUpRequestBizBean.getEnvInfoBean());
            final CreateTopupFromMerchantRequestBody body = extendInfo == null ? new CreateTopupFromMerchantRequestBody(
                    fundType, fundAmount, payerUserId, requestId, notificationUrl, merchantId, envInfo)
                    : new CreateTopupFromMerchantRequestBody(fundType, fundAmount, payerUserId, requestId,
                            notificationUrl, merchantId, envInfo, extendInfo);
            body.setPayeeAccountNo(payerUserId);
            body.setRoute(createTopUpRequestBizBean.getExtInfoReqBean().getRoute());
            final CreateTopupFromMerchantRequest createTopupRequest = new CreateTopupFromMerchantRequest(head, body);
            LOGGER.debug("Create TopUP from Merchant Request Object : {}", createTopupRequest);
            return new GenericCoreResponseBean<CreateTopupFromMerchantRequest>(createTopupRequest);
        } catch (final Exception e) {
            return new GenericCoreResponseBean<CreateTopupFromMerchantRequest>(e.getMessage());
        }

    }

    public static CloseRequest getCloseRequest(final BizCancelOrderRequest cancelAcquiringOrderRequest)
            throws FacadeInvalidParameterException, Exception {
        ApiFunctions apiFunction = ApiFunctions.CLOSE_ORDER;
        if (cancelAcquiringOrderRequest.isFromAoaMerchant()) {
            apiFunction = ApiFunctions.AOA_CLOSE_ORDER;
        }
        return new CloseRequest(RequestHeaderGenerator.getHeader(apiFunction),
                OrderHelper.getCloseRequestBody(cancelAcquiringOrderRequest));
    }

    public static CloseFundRequest getCloseFundRequest(final BizCancelFundOrderRequest cancelFundOrderRequest)
            throws FacadeInvalidParameterException, Exception {
        return new CloseFundRequest(RequestHeaderGenerator.getHeader(ApiFunctions.CLOSE_FUND_ORDER),
                OrderHelper.getCloseFundRequestBody(cancelFundOrderRequest));

    }

    public static CreateOrderRequest getCreateOrderRequest(final BizCreateOrderRequest request)
            throws FacadeCheckedException, Exception {
        if (request.isFromAoaMerchant()) {
            CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.AOA_CREATE_ORDER), getCreateOrderRequestBody(request));
            createOrderRequest.getBody().setFromAoaMerchant(request.isFromAoaMerchant());
            return createOrderRequest;
        }
        return new CreateOrderRequest(RequestHeaderGenerator.getHeader(ApiFunctions.CREATE_ORDER),
                getCreateOrderRequestBody(request));
    }

    public static CreateOrderAndPayRequest createOrderAndPayRequest(final CreateOrderAndPayRequestBean request)
            throws FacadeInvalidParameterException, Exception {

        ApiFunctions apiFunction = ApiFunctions.CREATE_ORDER_AND_PAY;
        if (request.isFromAoaMerchant()) {
            apiFunction = ApiFunctions.AOA_CREATE_ORDER_AND_PAY;
        }
        return new CreateOrderAndPayRequest(RequestHeaderGenerator.getHeader(apiFunction),
                createOrderAndPayRequestBody(request));
    }

    public static QueryByAcquirementIdRequest createQueryByAcquirementIdRequest(
            final QueryByAcquirementIdRequestBean queryByAcquirementIdRequestBean, final Routes route)
            throws FacadeInvalidParameterException, Exception {
        return new QueryByAcquirementIdRequest(RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_ACQUIREMENTID),
                new QueryByAcquirementIdRequestBody(queryByAcquirementIdRequestBean.getMerchantId(),
                        queryByAcquirementIdRequestBean.getAcquirementId(), true,
                        queryByAcquirementIdRequestBean.isFromAoaMerchant(), route,
                        queryByAcquirementIdRequestBean.getPaytmMerchantId()));
    }

    public static BizCreateOrderResponse mapCreateOrderResponseObject(final CreateOrderResponse createOrderResponse) {
        return new BizCreateOrderResponse(createOrderResponse.getBody().getMerchantTransId(), createOrderResponse
                .getBody().getAcquirementId());
    }

    public static CreateOrderAndPayResponseBean mapCreateOrderAndPayResponseObject(
            final CreateOrderAndPayResponse createOrderAndPayResponse) {
        return new CreateOrderAndPayResponseBean(createOrderAndPayResponse.getBody().getAcquirementId(),
                createOrderAndPayResponse.getBody().getCashierRequestId(), createOrderAndPayResponse.getBody()
                        .getSecurityPolicyResult());
    }

    public static QueryTransactionStatus mapQueryByAcquirementIdResponseObject(
            final QueryByAcquirementIdResponse queryByAcquirementIdResponse) {
        QueryByAcquirementIdResponseBody responseBody = queryByAcquirementIdResponse.getBody();
        QueryTransactionStatusBuilder builder = new QueryTransactionStatusBuilder(responseBody.getAcquirementId(),
                responseBody.getMerchantTransId(), responseBody.getOrderTitle(), responseBody.getExtendInfo());

        if (responseBody.getAmountDetail() != null) {
            AmountDetail amountDetail = responseBody.getAmountDetail();

            MoneyData moneyData = null;
            if (amountDetail.getChargeAmount() != null) {
                moneyData = new MoneyData(amountDetail.getChargeAmount().getCurrency().getCurrency(), amountDetail
                        .getChargeAmount().getAmount());
                builder.setChargeAmount(moneyData);
            }

            if (amountDetail.getChargebackAmount() != null) {
                moneyData = new MoneyData(amountDetail.getChargebackAmount().getCurrency().getCurrency(), amountDetail
                        .getChargebackAmount().getAmount());
                builder.setChargebackAmount(moneyData);
            }

            if (amountDetail.getOrderAmount() != null) {
                moneyData = new MoneyData(amountDetail.getOrderAmount().getCurrency().getCurrency(), amountDetail
                        .getOrderAmount().getAmount());
                builder.setOrderAmount(moneyData);
            }

            if (amountDetail.getPayAmount() != null) {
                moneyData = new MoneyData(amountDetail.getPayAmount().getCurrency().getCurrency(), amountDetail
                        .getPayAmount().getAmount());
                builder.setPayAmount(moneyData);
            }

            if (amountDetail.getRefundAmount() != null) {
                moneyData = new MoneyData(amountDetail.getRefundAmount().getCurrency().getCurrency(), amountDetail
                        .getRefundAmount().getAmount());
                builder.setRefundAmount(moneyData);
            }
        }

        if (responseBody.getTimeDetail() != null) {
            TimeDetail timeDetail = responseBody.getTimeDetail();
            builder.setTimeDetail(timeDetail.getCreatedTime(), timeDetail.getPaidTime(), timeDetail.getConfirmedTime(),
                    timeDetail.getExpiryTime());
        }

        if ((responseBody.getProductCode() != null) && (responseBody.getStatusDetail() != null)) {
            StatusDetail statusDetail = responseBody.getStatusDetail();
            String productCode = responseBody.getProductCode();
            if (statusDetail.getAcquirementStatus() != null) {
                if (AcquirementStatusType.PAYING == statusDetail.getAcquirementStatus()
                        && (StringUtils.equals(productCode, EDCPayConfirmAcquiringProd.getId()))) {
                    statusDetail.getAcquirementStatus().setStatusType("SUCCESS");
                }
            }
        }

        if (responseBody.getStatusDetail() != null) {
            StatusDetail statusDetail = responseBody.getStatusDetail();
            builder.setStatusDetail(statusDetail.getAcquirementStatus().getStatusType(), statusDetail.isFrozen());
        }
        List<PayOption> payOptions = new ArrayList<>();
        try {
            if (responseBody.getPaymentViews() != null) {

                List<PaymentView> paymentViews = responseBody.getPaymentViews();
                for (PaymentView paymentView : paymentViews) {
                    if (paymentView != null && paymentView.getPayOptionInfos() != null) {
                        for (PayOptionInfo payOptionInfo : paymentView.getPayOptionInfos()) {
                            try {
                                if (payOptionInfo != null) {
                                    PayOption payOption = new PayOption(payOptionInfo.getPayMethod().getMethod(),
                                            payOptionInfo.getPayMethod().getOldName(), payOptionInfo.getPayAmount()
                                                    .getCurrency().getCurrency(), payOptionInfo.getPayAmount()
                                                    .getAmount(), payOptionInfo.getExtendInfo(),
                                            payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount()
                                                    .getCurrency().getCurrency() : payOptionInfo.getTransAmount()
                                                    .getCurrency().getCurrency(),
                                            payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount()
                                                    .getAmount() : payOptionInfo.getTransAmount().getAmount(),
                                            payOptionInfo.getChargeAmount() != null ? payOptionInfo.getChargeAmount()
                                                    .getAmount() : null, payOptionInfo.getPayChannelInfo());

                                    payOptions.add(payOption);
                                }
                            } catch (Exception e) {
                                LOGGER.warn("Something went wrong while mapping payOption : ", e);
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.warn("Something went wrong while mapping paymentView : ", e);
        }
        if (responseBody.getSplitCommandInfoList() != null) {
            builder.setSplitCommandInfoList(responseBody.getSplitCommandInfoList());
        }
        builder.setPayOptions(payOptions);

        builder.setInputUserInfoBuyer(responseBody.getBuyer().getUserId(), responseBody.getBuyer().getExternalUserId());

        builder.setCurrentTxnCount(responseBody.getCurrentTxnCount());

        builder.setOrderModifyExtendInfo(responseBody.getOrderModifyExtendInfo());
        if (responseBody.getTimeDetail() != null)
            builder.setPaidTimesForTimeDetails(responseBody.getTimeDetail().getPaidTimes());

        return builder.build();
    }

    public static QueryByMerchantTransIdRequest createQueryByMerchantTransIdRequest(
            QueryByMerchantTransIDRequestBizBean queryByMerchantTransIDRequestBizBean, boolean fromAoaMerchant)
            throws FacadeCheckedException {

        ApiFunctions apiFunction = ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID;
        if (fromAoaMerchant) {
            apiFunction = ApiFunctions.AOA_QUERY_BY_MERCHANT_TRANS_ID;
        }
        AlipayExternalRequestHeader header = RequestHeaderGenerator.getHeader(apiFunction);
        header.setMerchantId(queryByMerchantTransIDRequestBizBean.getPaytmMID());
        QueryByMerchantTransIdRequestBody body = new QueryByMerchantTransIdRequestBody(
                queryByMerchantTransIDRequestBizBean.getMerchantID(),
                queryByMerchantTransIDRequestBizBean.getMerchantTransID(),
                queryByMerchantTransIDRequestBizBean.isNeedFullInfoRequired(), fromAoaMerchant);
        return new QueryByMerchantTransIdRequest(header, body);
    }

    public static FundUserOrderQueryByMerchantRequestIdRequest createQueryByMerchantRequestIdRequest(
            QueryByMerchantRequestIdBizBean queryByMerchantRequestIdBizBean) throws FacadeCheckedException {
        FundUserOrderQueryByMerchantRequestIdRequestBody fundUserOrderQueryByMerchantRequestIdRequestBody = new FundUserOrderQueryByMerchantRequestIdRequestBody(
                queryByMerchantRequestIdBizBean.getRequestId(), queryByMerchantRequestIdBizBean.getMerchantId());
        fundUserOrderQueryByMerchantRequestIdRequestBody.setRoute(queryByMerchantRequestIdBizBean.getRoute());
        return new FundUserOrderQueryByMerchantRequestIdRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_REQUEST_ID),
                fundUserOrderQueryByMerchantRequestIdRequestBody);
    }

    public static BizCancelOrderResponse getCancelResponse(final CloseResponse closeResponse) {
        BizResultInfo bizResultInfo = new BizResultInfo(closeResponse.getBody().getResultInfo().getResultStatus(),
                closeResponse.getBody().getResultInfo().getResultCodeId(), closeResponse.getBody().getResultInfo()
                        .getResultCode(), closeResponse.getBody().getResultInfo().getResultMsg());

        return new BizCancelOrderResponse(isCloseProcessed(closeResponse), bizResultInfo);
    }

    private static boolean isCloseProcessed(final CloseResponse closeResponse) {
        return closeResponse.getBody().getResultInfo().getResultCode()
                .equals(ECreateOrderResponses.SUCCESS.getInternalResultCode());
    }

    public static BizCancelOrderResponse getCancelResponse(final CloseFundResponse closeFundResponse) {
        BizResultInfo bizResultInfo = new BizResultInfo(closeFundResponse.getBody().getResultInfo().getResultStatus(),
                closeFundResponse.getBody().getResultInfo().getResultCodeId(), closeFundResponse.getBody()
                        .getResultInfo().getResultCode(), closeFundResponse.getBody().getResultInfo().getResultMsg());

        return new BizCancelOrderResponse(isCloseFundProcessed(closeFundResponse), bizResultInfo);
    }

    private static boolean isCloseFundProcessed(final CloseFundResponse closeFundResponse) {
        return closeFundResponse.getBody().getResultInfo().getResultCode()
                .equals(ECreateOrderResponses.SUCCESS.getInternalResultCode());
    }

    public static CloseRequestBody getCloseRequestBody(final BizCancelOrderRequest cancelAcquiringOrderRequest)
            throws FacadeInvalidParameterException {
        return StringUtils.isBlank(cancelAcquiringOrderRequest.getCloseReason()) ? new CloseRequestBody(
                cancelAcquiringOrderRequest.getAcquirementId(), cancelAcquiringOrderRequest.getMerchantId(),
                cancelAcquiringOrderRequest.isFromAoaMerchant()) : new CloseRequestBody(
                cancelAcquiringOrderRequest.getAcquirementId(), cancelAcquiringOrderRequest.getMerchantId(),
                cancelAcquiringOrderRequest.getCloseReason(), cancelAcquiringOrderRequest.isFromAoaMerchant());
    }

    public static CloseFundRequestBody getCloseFundRequestBody(final BizCancelFundOrderRequest cancelFundOrderRequest)
            throws FacadeInvalidParameterException {
        if (StringUtils.isBlank(cancelFundOrderRequest.getCloseReason())) {
            return new CloseFundRequestBody(cancelFundOrderRequest.getFundOrderId(),
                    cancelFundOrderRequest.getEnvInfo());
        } else {
            return new CloseFundRequestBody(cancelFundOrderRequest.getFundOrderId(),
                    cancelFundOrderRequest.getEnvInfo(), cancelFundOrderRequest.getCloseReason());
        }
    }

    public static CreateOrderRequestBody getCreateOrderRequestBody(final BizCreateOrderRequest createOrderRequest)
            throws FacadeCheckedException {
        final EnvInfo envInfo = getEnvInfo(createOrderRequest);

        final Order order = getOrder(createOrderRequest);
        Map<String, String> orderExtendInfo = AlipayRequestUtils.getExtendeInfoMap(createOrderRequest.getExtendInfo());
        if (MapUtils.isNotEmpty(createOrderRequest.getAdditionalOrderExtendInfo())) {
            orderExtendInfo.putAll(createOrderRequest.getAdditionalOrderExtendInfo());
        }

        if (null != orderExtendInfo.get(BizConstant.ExtendedInfoKeys.DUMMY_ORDER_ID)) {
            order.setMerchantTransId(orderExtendInfo.get(BizConstant.ExtendedInfoKeys.DUMMY_ORDER_ID));
        }

        Map<String, String> detailExtendInfoMap = getDetailExtendInfoMap(createOrderRequest.getEmiDetailInfo());

        return new CreateOrderRequestBody.CreateOrderRequestBodyBuilder(order,
                createOrderRequest.getInternalMerchantId(), getProductCode(createOrderRequest),
                createOrderRequest.getIndustryCode(), detailExtendInfoMap).envInfo(envInfo).extendInfo(orderExtendInfo)
                .build();
    }

    public static ProductCodes getProductCode(BizCreateOrderRequest request) throws FacadeInvalidParameterException {

        if (request.isDealsTransaction())
            return ProductCodes.StandardDirectPayDealAcquiringProd;
        else if ((StringUtils.isNotBlank(request.getExtendInfo().getDummyMerchantId()) && StringUtils
                .isNotBlank(request.getExtendInfo().getDummyOrderId()))
                || ProductCodes.StandardDirectPayPennyDropProd.getId().equals(request.getExtendInfo().getProductCode())) {
            return ProductCodes.StandardDirectPayPennyDropProd;
        } else if (request.isSlabBasedMDR()) {
            return ProductCodes.StandardAcquiringProdByAmountChargePayer;
        } else if (request.isDynamicFeeMerchant() && request.isDefaultDynamicFeeMerchantPayment()) {
            return ProductCodes.StandardDirectPayDynamicTargetProd;
        } else if (request.isPostConvenienceFee()) {
            if (ERequestType.DYNAMIC_QR_2FA.equals(request.getRequestType())) {
                return ProductCodes.ScanNPayChargePayer;
            } else {
                return ProductCodes.StandardDirectPayAcquiringProdChargePayer;
            }
        } else {
            return ProductCodes.getProductByProductCode(request.getRequestType().getProductCode());
        }
    }

    public static CreateOrderAndPayRequestBody createOrderAndPayRequestBody(
            final CreateOrderAndPayRequestBean createOrderAndPayRequest) throws FacadeCheckedException {
        // Creating Order details
        final Order order = getOrder(createOrderAndPayRequest.getOrder());

        // Creating PayOptionBills
        final List<PayOptionBill> payOptionBills = new ArrayList<>();
        boolean isPg2Request = false;
        if (PaymentAdapterUtil.eligibleForPG2(createOrderAndPayRequest.getExtendInfo().getRoute())) {
            isPg2Request = true;
        }
        for (final BizPayOptionBill bizPayOptionBill : createOrderAndPayRequest.getPaymentInfo().getPayOptionBills()) {

            String payOption = bizPayOptionBill.getPayOption();
            PayMethod payMethod = PayMethod.getPayMethodByMethod(bizPayOptionBill.getPayMethod().getMethod());
            Money transAmount = new Money(EnumCurrency.INR, bizPayOptionBill.getTransAmount());

            Money chargeAmount = new Money(EnumCurrency.INR,
                    bizPayOptionBill.getChargeAmount() != null ? bizPayOptionBill.getChargeAmount() : "0");

            String cacheCardToken = bizPayOptionBill.getCardCacheToken();
            Map<String, String> channelInfo = bizPayOptionBill.getChannelInfo();
            Map<String, String> extendedInfo = bizPayOptionBill.getExtendInfo();
            String payerAccountNumber = bizPayOptionBill.getPayerAccountNo();
            String payMode = bizPayOptionBill.getPayMode();
            List<String> mgvTemplateIds = bizPayOptionBill.getTemplateIds();
            String vpa = bizPayOptionBill.getVirtualPaymentAddr();

            filterExtendedInfoForPaymentInfo(extendedInfo, createOrderAndPayRequest.getPaytmMerchantId());

            final PayOptionBill payOptionBill = new PayOptionBill.PayOptionBillBuilder().payOption(payOption)
                    .payMethod(payMethod).transAmount(transAmount).chargeAmount(chargeAmount)
                    .cardCacheToken(cacheCardToken).channelInfo(channelInfo).extendInfo(extendedInfo)
                    .payerAccountNo(payerAccountNumber).topupAndPay(bizPayOptionBill.isTopupAndPay()).payMode(payMode)
                    .mgvTemplateIds(mgvTemplateIds).virtualPaymentAddr(vpa)
                    .saveAssetForMerchant(bizPayOptionBill.isSaveAssetForMerchant())
                    .saveCardAfterPay(bizPayOptionBill.isSaveCardAfterPay())
                    .saveAssetForUser(bizPayOptionBill.isSaveAssetForUser())
                    .prepaidCard(bizPayOptionBill.isPrepaidCard())
                    .feeRateFactorsInfo(bizPayOptionBill.getFeeRateFactorsInfo())
                    .dccPaymentInfo(bizPayOptionBill.getDccPaymentInfo())
                    .lastFourDigits(bizPayOptionBill.getLastFourDigits()).settleType(bizPayOptionBill.getSettleType())
                    .isDeepLinkFlow(bizPayOptionBill.isDeepLinkFlow()).isUpiCC(bizPayOptionBill.isUpiCC()).build();
            if (isPg2Request) {
                payOptionBill.setVanInfo(bizPayOptionBill.getVanInfo());
            }
            payOptionBills.add(payOptionBill);

        }

        boolean isAddNPayTransaction = isAddNPayTransaction(createOrderAndPayRequest);

        // Adding cost preferred merchant gateways for cost based routing
        if (isAddNPayTransaction || createOrderAndPayRequest.isCostBasedPreferenceEnabled()) {
            CostBasedPreferenceUtil.setCostPreferredGateways(payOptionBills, isAddNPayTransaction, false,
                    createOrderAndPayRequest);
        }

        // Creating PaymentInfo
        final PaymentInfo paymentInfo = new PaymentInfo(createOrderAndPayRequest.getPaymentInfo().getContractId(),
                payOptionBills, createOrderAndPayRequest.getPaymentInfo().getPwpCategory(),
                String.valueOf(createOrderAndPayRequest.getPaymentInfo().isAddAndPayMigration()));

        if (ObjectUtils.notEqual(createOrderAndPayRequest.getPaymentInfo().getUltimateBeneficiaryDetails(), null)) {
            paymentInfo.setUltimateBeneficiaryDetails(createOrderAndPayRequest.getPaymentInfo()
                    .getUltimateBeneficiaryDetails());
        }

        /* Identifier for link flow */
        if (StringUtils.isNotEmpty(createOrderAndPayRequest.getRequestFlow()))
            paymentInfo.setRequestFlow(createOrderAndPayRequest.getRequestFlow());

        if (createOrderAndPayRequest.isFromAoaMerchant()
                && createOrderAndPayRequest.getPaymentInfo().getChannelPreference() != null) {
            ChannelPreference channelPreference = new ChannelPreference();
            List<PreferenceValue> preferenceValues = new ArrayList<>();
            for (BizPreferenceValue bizPreferenceValue : createOrderAndPayRequest.getPaymentInfo()
                    .getChannelPreference().getPreferenceValues()) {
                preferenceValues.add(new PreferenceValue(bizPreferenceValue.getServiceInstId(), bizPreferenceValue
                        .getScore()));
            }
            channelPreference.setPreferenceValues(preferenceValues);
            channelPreference.setExtendInfo(createOrderAndPayRequest.getPaymentInfo().getChannelPreference()
                    .getExtendInfo());
            paymentInfo.setChannelPreference(channelPreference);
        }

        if (StringUtils.isNotBlank(createOrderAndPayRequest.getPaymentInfo().getVerificationType())) {
            paymentInfo.setVerificationType(createOrderAndPayRequest.getPaymentInfo().getVerificationType());
            LOGGER.info("Verification type added in Payment Info");
        }

        // Creating EnvironmentInfo

        /*
         * TerminalType terminalType =
         * TerminalType.getTerminalTypeByTerminal(createOrderAndPayRequest
         * .getEnvInfo() .getTerminalType().getTerminal()); Map<String, String>
         * extendedInfo = createOrderAndPayRequest.getEnvInfo().getExtendInfo();
         * EnvInfo.EnvInfoBuilder envInfoBuilder = new
         * EnvInfo.EnvInfoBuilder(createOrderAndPayRequest.getEnvInfo()
         * .getClientIp(),
         * terminalType).osType(createOrderAndPayRequest.getEnvInfo
         * ().getOsType()).extendInfo( extendedInfo); String tokenId =
         * createOrderAndPayRequest.getEnvInfo().getTokenId(); if
         * (StringUtils.isNotBlank(tokenId)) { envInfoBuilder.tokenId(tokenId);
         * if (!TerminalType.WEB.equals(terminalType)) { if (extendedInfo ==
         * null) { extendedInfo = new HashMap<>(); }
         * extendedInfo.put("deviceId", tokenId); } }
         */
        final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(createOrderAndPayRequest.getEnvInfo());
        // final EnvInfo envInfo = envInfoBuilder.build();

        // Creating CreateOrderAndPay request data
        Map<String, String> orderExtendInfo = AlipayRequestUtils.getExtendeInfoMap(createOrderAndPayRequest
                .getExtendInfo());
        if (MapUtils.isNotEmpty(createOrderAndPayRequest.getOrder().getAdditionalOrderExtendInfo())) {
            orderExtendInfo.putAll(createOrderAndPayRequest.getOrder().getAdditionalOrderExtendInfo());
        }

        if (null != orderExtendInfo.get(BizConstant.ExtendedInfoKeys.DUMMY_ORDER_ID)) {
            order.setMerchantTransId(orderExtendInfo.get(BizConstant.ExtendedInfoKeys.DUMMY_ORDER_ID));
        }

        orderExtendInfo.put(BizConstant.ExtendedInfoKeys.FROMAOAMERCHANT,
                String.valueOf(createOrderAndPayRequest.isFromAoaMerchant()));

        filterExtendedInfoForOrder(orderExtendInfo, createOrderAndPayRequest.getPaytmMerchantId());

        if (null != createOrderAndPayRequest.getOrder()
                && MapUtils.isNotEmpty(createOrderAndPayRequest.getOrder().getEmiDetailInfo())) {
            Map<String, String> detailExtendInfoMap = createOrderAndPayRequest.getDetailExtendInfo();
            if (MapUtils.isNotEmpty(detailExtendInfoMap)) {
                detailExtendInfoMap.put("EMI_DETAIL_INFO",
                        JsonMapper.mapObjectToJson(createOrderAndPayRequest.getOrder().getEmiDetailInfo()));
            } else {
                detailExtendInfoMap = new HashMap<>();
                detailExtendInfoMap.put("EMI_DETAIL_INFO",
                        JsonMapper.mapObjectToJson(createOrderAndPayRequest.getOrder().getEmiDetailInfo()));
            }
            createOrderAndPayRequest.setDetailExtendInfo(detailExtendInfoMap);
        }

        // adding PaymentBizData Object in CreateOrderAndPayRequestBody
        return new CreateOrderAndPayRequestBody.CreateOrderAndPayRequestBodyBuilder(order,
                createOrderAndPayRequest.getMerchantId(), ProductCodes.getProductByProductCode(createOrderAndPayRequest
                        .getProductCode()), createOrderAndPayRequest.getMcc(), createOrderAndPayRequest.getRequestId(),
                paymentInfo).extendInfo(orderExtendInfo).envInfo(envInfo)
                .riskExtendInfo(createOrderAndPayRequest.getRiskExtendInfo())
                .fromAoaMerchant(createOrderAndPayRequest.isFromAoaMerchant())
                .paymentBizInfo(createOrderAndPayRequest.getPaymentBizInfo())
                .paymtmMerchantId(createOrderAndPayRequest.getPaytmMerchantId())
                .detailExtendInfo(createOrderAndPayRequest.getDetailExtendInfo()).build();

    }

    /***
     * Method does the profiling for extendedInfo under PayOptionBill in
     * PaymentInfo object. This is done based on the list of keys which are not
     * necessary in COP request. Currently profiling is done only for the Static
     * QR flow & UPI PSP Flow.
     * 
     * @param paymentInfoExtendInfo
     * @param mid
     */
    private static void filterExtendedInfoForPaymentInfo(Map<String, String> paymentInfoExtendInfo, String mid) {
        if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_STATIC_QR_FLOW_EXTENDINFO_PROFILING, false)) {
            if (paymentInfoExtendInfo != null
                    && Boolean.TRUE.toString().equals(paymentInfoExtendInfo.get("offlineFlow"))
                    && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_EXTENDINFO_PROFILING_IN_PAYOPTION, false)) {
                removeKeysFromExtendInfoMap(paymentInfoExtendInfo);
            }
        }
        if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_UPIPSP_FLOW_EXTENDINFO_PROFILING, false)) {
            if (paymentInfoExtendInfo != null && StringUtils.isNotBlank(paymentInfoExtendInfo.get("qrCodeId"))
                    && Boolean.FALSE.toString().equals(paymentInfoExtendInfo.get("offlineFlow"))
                    && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_EXTENDINFO_PROFILING_IN_PAYOPTION, false)) {
                removeKeysFromExtendInfoMap(paymentInfoExtendInfo);
            }
        }
    }

    /***
     * Method does the profiling for extendedInfo in
     * CreateOrderAndPayRequestBody. This is done based on the list of keys
     * which are not necessary in COP request. Currently profiling is done only
     * for the Static QR flow & UPI PSP Flow.
     * 
     * @param orderExtendInfo
     * @param mid
     */
    private static void filterExtendedInfoForOrder(Map<String, String> orderExtendInfo, String mid) {
        if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_STATIC_QR_FLOW_EXTENDINFO_PROFILING, false)) {
            if (orderExtendInfo != null && Boolean.TRUE.toString().equals(orderExtendInfo.get("offlineFlow"))
                    && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_EXTENDINFO_PROFILING_IN_ORDER, false)) {
                removeKeysFromExtendInfoMap(orderExtendInfo);
            }
        }
        if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_UPIPSP_FLOW_EXTENDINFO_PROFILING, false)) {
            if (orderExtendInfo != null && StringUtils.isNotBlank(orderExtendInfo.get("qrCodeId"))
                    && orderExtendInfo != null && Boolean.FALSE.toString().equals(orderExtendInfo.get("offlineFlow"))
                    && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_EXTENDINFO_PROFILING_IN_ORDER, false)) {
                removeKeysFromExtendInfoMap(orderExtendInfo);
            }
        }
    }

    /***
     * Method to remove keys from the ExtendInfo Map.
     * 
     * @param extendInfo
     */
    private static void removeKeysFromExtendInfoMap(Map<String, String> extendInfo) {
        String keysToRemove = ff4jUtils.getPropertyAsStringWithDefault(THEIA_EXTEND_INFO_KEYS_TO_REMOVE, null);
        if (!StringUtils.isBlank(keysToRemove)) {
            List<String> keysListToRemove = new ArrayList<>(Arrays.asList(keysToRemove.split(",")));
            keysListToRemove.forEach(extendInfo::remove);
        }
    }

    private static boolean isAddNPayTransaction(CreateOrderAndPayRequestBean createOrderAndPayRequest) {
        boolean isAddnPay = false;
        if (createOrderAndPayRequest.getExtendInfo() != null) {
            // check for AddnPay
            if (StringUtils.isNotEmpty(createOrderAndPayRequest.getExtendInfo().getTxnType())) {
                if (createOrderAndPayRequest.getExtendInfo().getTxnType().equalsIgnoreCase(TXN_TYPE_ADDNPAY)) {
                    isAddnPay = true;
                }
            }
        }
        return isAddnPay;
    }

    public static CreateTopUpResponseBizBean mapCreateTopUp(final CreateTopupFromMerchantResponse createTopupResponse) {
        return new CreateTopUpResponseBizBean(createTopupResponse.getBody().getFundOrderId(), createTopupResponse
                .getBody().getRequestId());
    }

    public static CreateTopUpResponseBizBean mapCreateTopUp(final CreateTopupResponse createTopupResponse) {
        return new CreateTopUpResponseBizBean(createTopupResponse.getBody().getFundOrderId(), createTopupResponse
                .getBody().getRequestId());
    }

    public static BizPayResponse mapPayResponse(PayResponse payResponse) {
        return new BizPayResponse(payResponse.getBody().getCashierRequestId(), payResponse.getBody()
                .getSecurityPolicyResult());
    }

    public static QueryByMerchantTransIDResponseBizBean mapQueryByMerchantResponseBean(
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {

        String acquirementID = queryByMerchantTransIdResponse.getBody().getAcquirementId();
        String merchantTransID = queryByMerchantTransIdResponse.getBody().getMerchantTransId();
        AmountDetail amountDetail = queryByMerchantTransIdResponse.getBody().getAmountDetail();
        TimeDetail timeDetail = queryByMerchantTransIdResponse.getBody().getTimeDetail();
        StatusDetail statusDetail = queryByMerchantTransIdResponse.getBody().getStatusDetail();
        String productCode = queryByMerchantTransIdResponse.getBody().getProductCode();
        if ((productCode != null) && (statusDetail != null)) {
            if (statusDetail.getAcquirementStatus() != null) {
                String statusType = statusDetail.getAcquirementStatus().getStatusType();
                if (StringUtils.equals(statusType, "PAYING")
                        && (StringUtils.equals(productCode, EDCPayConfirmAcquiringProd.getId()))) {
                    statusDetail.getAcquirementStatus().setStatusType("SUCCESS");
                }
            }
        }
        Map<String, String> extendedInfo = queryByMerchantTransIdResponse.getBody().getExtendInfo();

        QueryByMerchantTransIDResponseBizBean queryByMerchantTransIDResponseBizBean = new QueryByMerchantTransIDResponseBizBean();
        queryByMerchantTransIDResponseBizBean.setAcquirementId(acquirementID);
        queryByMerchantTransIDResponseBizBean.setMerchantTransId(merchantTransID);
        queryByMerchantTransIDResponseBizBean.setAmountDetail(amountDetail);
        queryByMerchantTransIDResponseBizBean.setTimeDetail(timeDetail);
        queryByMerchantTransIDResponseBizBean.setStatusDetail(statusDetail);
        queryByMerchantTransIDResponseBizBean.setExtendInfo(extendedInfo);

        return queryByMerchantTransIDResponseBizBean;
    }

    public static QueryByMerchantRequestIdResponseBizBean mapQueryByMerchantRequestIdResponseBean(
            FundUserOrderQueryByMerchantRequestIdResponse queryByRequestIdResponse) {
        QueryByMerchantRequestIdResponseBizBean queryByMerchantRequestIdResponseBean = new QueryByMerchantRequestIdResponseBizBean();

        if ((queryByRequestIdResponse.getBody() != null) && (queryByRequestIdResponse.getBody().getFundOrder() != null)) {
            FundOrder fundOrderDetails = queryByRequestIdResponse.getBody().getFundOrder();
            queryByMerchantRequestIdResponseBean.setFundOrderId(fundOrderDetails.getFundOrderId());
            queryByMerchantRequestIdResponseBean.setRequestId(fundOrderDetails.getRequestId());
        }
        return queryByMerchantRequestIdResponseBean;
    }

    public static Map<String, String> getDetailExtendInfoMap(Map<String, String> emiDetailInfoMap) {
        Map<String, String> detailExtendInfoMap = new HashMap<>();
        try {
            if (MapUtils.isNotEmpty(emiDetailInfoMap)) {
                detailExtendInfoMap.put("EMI_DETAIL_INFO", JsonMapper.mapObjectToJson(emiDetailInfoMap));
            }
        } catch (Exception e) {
            LOGGER.error("error in creating detailExtendInfo ", e);
        }
        return detailExtendInfoMap;
    }

    public static Order getOrder(final BizCreateOrderRequest createOrderRequest) throws FacadeInvalidParameterException {
        String nickname = "";
        if (createOrderRequest != null && createOrderRequest.getFirstName() != null) {
            nickname = createOrderRequest.getFirstName();
        }

        final InputUserInfo buyer = new InputUserInfo("", createOrderRequest.getExternalUserId(),
                createOrderRequest.getInternalUserId(), nickname);
        // TODO info for seller
        OrderBuilder orderBuilder = new Order.OrderBuilder(getOrderDescription(createOrderRequest), new Money(
                createOrderRequest.getCurrency().toString(), createOrderRequest.getOrderAmount().toString()),
                createOrderRequest.getOrderId(), new Date()).buyer(buyer);
        if (createOrderRequest.getOrderTimeoutInMilliseconds() != null) {
            orderBuilder.expiryTime(new Date(System.currentTimeMillis()
                    + createOrderRequest.getOrderTimeoutInMilliseconds()));
        }
        if (CollectionUtils.isNotEmpty(createOrderRequest.getSplitCommandInfoList())) {
            orderBuilder.setSplitCommandInfoList(createOrderRequest.getSplitCommandInfoList());
        }
        if (CollectionUtils.isNotEmpty(createOrderRequest.getTimeoutConfigRuleList())) {
            orderBuilder.timeoutConfigRuleList(createOrderRequest.getTimeoutConfigRuleList());
        }
        if (StringUtils.isNotBlank(createOrderRequest.getPaymentPendingTimeout())) {
            orderBuilder.paymentPendingTimeout(createOrderRequest.getPaymentPendingTimeout());
        }
        if (MapUtils.isNotEmpty(createOrderRequest.getEmiDetailInfo())) {
            try {
                orderBuilder.emiDetailInfo(createOrderRequest.getEmiDetailInfo());
            } catch (Exception e) {
                LOGGER.error("Error in converting emiDetailInfo map to json string: {}", e.getMessage());
            }
        }
        try {
            if (!Strings.isNullOrEmpty(createOrderRequest.getGoodsInfo())) {
                List<Goods> goodsList = null;
                if (ERequestType.NATIVE_PAY == createOrderRequest.getRequestType()
                        || ERequestType.NATIVE == createOrderRequest.getRequestType()
                        || createOrderRequest.isCreateOrderForInitiateTxnRequest()) {
                    goodsList = ObjectMapperUtil.getListOfObject(createOrderRequest.getGoodsInfo(), Goods.class);

                }
                if (goodsList == null) {
                    goodsList = ObjectMapperUtil.getListOfObject(
                            URLDecoder.decode(createOrderRequest.getGoodsInfo(), "UTF-8"), Goods.class);
                }
                LOGGER.debug("GoodsInfo parsed successfully: {}", (goodsList != null ? true : false));
                orderBuilder.goods(goodsList);
            }
            if (!Strings.isNullOrEmpty(createOrderRequest.getShippingInfo())) {
                List<ShippingInfo> shippingInfos = null;
                if (ERequestType.NATIVE == createOrderRequest.getRequestType()
                        || createOrderRequest.isCreateOrderForInitiateTxnRequest()) {
                    shippingInfos = ObjectMapperUtil.getObjectMapper().readValue(createOrderRequest.getShippingInfo(),
                            new TypeReference<Collection<ShippingInfo>>() {
                            });

                } else {
                    String shippingInfo = URLDecoder.decode(createOrderRequest.getShippingInfo(), "UTF-8");
                    shippingInfos = ObjectMapperUtil.getObjectMapper().readValue(shippingInfo,
                            new TypeReference<Collection<ShippingInfo>>() {
                            });
                }
                for (ShippingInfo shipingInfo : shippingInfos) {
                    String shippingChargeAmount = shipingInfo.getChargeAmount().getValue();
                    validateShippingAmount(shippingChargeAmount);
                    shipingInfo.getChargeAmount().setValue(
                            AmountUtils.getTransactionAmountInPaise(shippingChargeAmount));
                }
                orderBuilder.shippingInfo(shippingInfos);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Exception Occurred While mapping Goods/Shipping Info ", e);
        } catch (IOException e) {
            LOGGER.error("Exception Occurred While mapping Shipping Info ", e);
        }
        orderBuilder.orderPricingInfo(createOrderRequest.getOrderPricingInfo());
        orderBuilder.splitType(createOrderRequest.getSplitType());
        return orderBuilder.build();
    }

    private static EnvInfo getEnvInfo(final BizCreateOrderRequest createOrderRequest)
            throws FacadeInvalidParameterException {
        Map<String, String> extendInfo = createOrderRequest.getEnvInfoRequestBean().getExtendInfo();
        if (extendInfo == null) {
            extendInfo = new HashMap<>();
        }
        createOrderRequest.getEnvInfoRequestBean().setExtendInfo(extendInfo);
        TerminalType terminalType = createOrderRequest.getRequestType().equals(ERequestType.SUBSCRIPTION_RENEWAL) ? TerminalType.SYSTEM
                : getTerminalFromChannel(createOrderRequest.getChannel());
        if (!TerminalType.WEB.equals(terminalType)) {
            extendInfo.put("deviceId", createOrderRequest.getRiskToken());
        }
        return new EnvInfo.EnvInfoBuilder(createOrderRequest.getClientIp(), terminalType)
                .osType(createOrderRequest.getOsType()).tokenId(createOrderRequest.getRiskToken())
                .sessionId(createOrderRequest.getEnvInfoRequestBean().getSessionId()).extendInfo(extendInfo)
                .appVersion(createOrderRequest.getEnvInfoRequestBean().getAppVersion())
                .sdkVersion(createOrderRequest.getEnvInfoRequestBean().getSdkVersion()).build();
    }

    private static String getOrderDescription(final BizCreateOrderRequest createOrderRequest) {
        return StringUtils.isBlank(createOrderRequest.getOrderDescription()) ? createOrderRequest.getOrderId()
                : createOrderRequest.getOrderDescription();
    }

    private static TerminalType getTerminalFromChannel(final EChannelId channel) {
        switch (channel) {
        case WAP:
            return TerminalType.WAP;
        case WEB:
            return TerminalType.WEB;
        case SYSTEM:
            return TerminalType.SYSTEM;
        case APP:
            return TerminalType.APP;
        default:
        }
        return null;
    }

    public static GenericCoreResponseBean<BizCreateOrderResponse> verifyAndReturnResponse(
            final CreateOrderResponse createOrderResponse) throws Exception {
        if (!ECreateOrderResponses.SUCCESS.getInternalResultCode().equals(
                createOrderResponse.getBody().getResultInfo().getResultCode())) {
            final String errorMessage = createOrderResponse.getBody().getResultInfo().getResultMsg();
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    createOrderResponse.getBody().getResultInfo().getResultCodeId());
            if (ResponseConstants.SUCCESS_IDEMPOTENT_ERROR.equals(responseConstants)) {
                return new GenericCoreResponseBean<BizCreateOrderResponse>(errorMessage, createOrderResponse.getBody()
                        .getAcquirementId(), responseConstants);
            }
            LOGGER.error("Invalid Result returned for Create Order : {}", errorMessage);
            return new GenericCoreResponseBean<>(errorMessage, responseConstants);
        }

        return new GenericCoreResponseBean<>(OrderHelper.mapCreateOrderResponseObject(createOrderResponse));
    }

    public static GenericCoreResponseBean<CreateOrderAndPayResponseBean> verifyAndReturnResponse(
            final CreateOrderAndPayResponse createOrderAndPayResponse,
            final CreateOrderAndPayRequestBean createOrderAndPayRequest) {
        if (BizConstant.ACCEPTED_SUCCESS.equals(createOrderAndPayResponse.getBody().getResultInfo().getResultCode())) {
            return new GenericCoreResponseBean<>(
                    OrderHelper.mapCreateOrderAndPayResponseObject(createOrderAndPayResponse));
        }
        MerchantLimitUtil.checkIfMerchantLimitBreached(createOrderAndPayResponse.getBody().getResultInfo()
                .getResultCodeId());
        String riskFailureMessage = null;
        String internalErrorCode = null;
        Map<String, String> infoCodeMessageMap;
        String failureMessage = createOrderAndPayResponse.getBody().getResultInfo().getResultMsg();
        LOGGER.error("Failure response returned for CreateOrderAndPay : {}", failureMessage);
        ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                createOrderAndPayResponse.getBody().getResultInfo().getResultCodeId());

        failureLogUtils.setFailureMsgForDwhPush(createOrderAndPayResponse.getBody().getResultInfo().getResultCodeId(),
                failureMessage, TheiaConstant.ExtraConstants.ACQUIRING_ORDER_CREATEORDER_AND_PAY, false);

        if (ResponseConstants.NEED_RISK_CHALLENGE.getAlipayResultCode().equals(responseConstants.getAlipayResultCode())) {
            return new GenericCoreResponseBean<>(
                    OrderHelper.mapCreateOrderAndPayResponseObject(createOrderAndPayResponse), failureMessage,
                    responseConstants);
        }
        /*
         * if merchant does not support ecomtoken txn, for prepaid and corporate
         * handling is done at biz layer
         */

        if (ResponseConstants.ECOMTOKEN_TRANSACTION_NOT_SUPPORTED.getAlipayResultCode().equals(
                responseConstants.getAlipayResultCode())) {
            return new GenericCoreResponseBean<>(
                    OrderHelper.mapCreateOrderAndPayResponseObject(createOrderAndPayResponse), failureMessage,
                    responseConstants);
        }

        GenericCoreResponseBean<CreateOrderAndPayResponseBean> responseBean = null;

        if (ResponseConstants.RISK_REJECT.getAlipayResultCode().equals(responseConstants.getAlipayResultCode())) {
            LOGGER.warn("Transaction has been failed due to RISK_REJECT");

            if (createOrderAndPayResponse.getBody().getSecurityPolicyResult() != null
                    && createOrderAndPayResponse.getBody().getSecurityPolicyResult().getRiskResult() != null) {
                String riskRejectResponse = createOrderAndPayResponse.getBody().getSecurityPolicyResult()
                        .getRiskResult().getRiskInfo();
                infoCodeMessageMap = RiskRejectInfoCodesAndMessages
                        .fetchUserMessageAccToPriorityFromInfoCodeListWithCode(riskRejectResponse);
                riskFailureMessage = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.MESSAGE);
                internalErrorCode = infoCodeMessageMap.get(RiskRejectInfoCodesAndMessages.INFOCODE);
                riskFailureMessage = createOrderAndPayResponse.getBody().getSecurityPolicyResult().getRiskResult()
                        .getRiskExtendedInfo().containsKey(TheiaConstant.ExtraConstants.RISKREJECT_EXTENDEDINFO_KEY) ? createOrderAndPayResponse
                        .getBody().getSecurityPolicyResult().getRiskResult().getRiskExtendedInfo()
                        .get(TheiaConstant.ExtraConstants.RISKREJECT_EXTENDEDINFO_KEY)
                        : riskFailureMessage;
            }
        }

        if (StringUtils.isNotBlank(riskFailureMessage)) {
            responseBean = new GenericCoreResponseBean<>(createOrderAndPayResponse.getBody().getResultInfo()
                    .getResultMsg(), responseConstants, riskFailureMessage);
            responseBean.setInternalErrorCode(internalErrorCode);
        } else {
            responseBean = new GenericCoreResponseBean<>(createOrderAndPayResponse.getBody().getResultInfo()
                    .getResultMsg(), responseConstants);
        }

        if (StringUtils.isNotBlank(createOrderAndPayResponse.getBody().getAcquirementId())) {
            responseBean.setAcquirementId(createOrderAndPayResponse.getBody().getAcquirementId());
        }

        return responseBean;
    }

    public static GenericCoreResponseBean<QueryTransactionStatus> verifyAndReturnResponse(
            final QueryByAcquirementIdResponse queryByAcquirementIdResponse) {
        if (!queryByAcquirementIdResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
            LOGGER.info("Invalid Result returned for QueryByAcquirementId : {}", queryByAcquirementIdResponse.getBody()
                    .getResultInfo());
            ResponseConstants responseConstants = ResponseConstants.SYSTEM_ERROR;
            return new GenericCoreResponseBean<>(queryByAcquirementIdResponse.getBody().getResultInfo().getResultMsg(),
                    responseConstants);
        }
        return new GenericCoreResponseBean<>(
                OrderHelper.mapQueryByAcquirementIdResponseObject(queryByAcquirementIdResponse));
    }

    public static GenericCoreResponseBean<BizCancelOrderResponse> verifyAndReturnResponse(
            final CloseResponse closeResponse) {
        if (!closeResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
            LOGGER.info("Unable to complete close Order call due to : {}", closeResponse.getBody().getResultInfo()
                    .getResultMsg());
        }

        return new GenericCoreResponseBean<>(OrderHelper.getCancelResponse(closeResponse));
    }

    public static GenericCoreResponseBean<BizCancelOrderResponse> verifyAndReturnResponse(
            final CloseFundResponse closeFundResponse) {

        if (!closeFundResponse.getBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
            LOGGER.info("Error occurred while closing Order : {}", closeFundResponse.getBody().getResultInfo()
                    .getResultMsg());
        }

        return new GenericCoreResponseBean<>(OrderHelper.getCancelResponse(closeFundResponse));
    }

    @Deprecated
    public static GenericCoreResponseBean<CreateTopUpResponseBizBean> verifyAndReturnResponse(
            final CreateTopupResponse topUpResponse) {
        if (!topUpResponse.getBody().getResultInfo().getResultCode()
                .equals(ECreateOrderResponses.SUCCESS.getInternalResultCode())) {
            LOGGER.info("Invalid Result returned for CreateTopUP : {}", topUpResponse.getBody().getResultInfo()
                    .getResultMsg());
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    topUpResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(topUpResponse.getBody().getResultInfo().getResultMsg(),
                    responseConstants);
        }
        final CreateTopUpResponseBizBean createTopupResponseBizBean = OrderHelper.mapCreateTopUp(topUpResponse);
        LOGGER.debug("CreateTopUP Response Biz Bean ::{}", createTopupResponseBizBean);
        return new GenericCoreResponseBean<>(createTopupResponseBizBean);
    }

    public static GenericCoreResponseBean<CreateTopUpResponseBizBean> verifyAndReturnResponse(
            final CreateTopupFromMerchantResponse topUpResponse) {
        if (!topUpResponse.getBody().getResultInfo().getResultCode()
                .equals(ECreateOrderResponses.SUCCESS.getInternalResultCode())) {
            LOGGER.info("Invalid Result returned for CreateTopUPFromMerchant : {}", topUpResponse.getBody()
                    .getResultInfo().getResultMsg());
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    topUpResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(topUpResponse.getBody().getResultInfo().getResultMsg(),
                    responseConstants);
        }
        final CreateTopUpResponseBizBean createTopupResponseBizBean = OrderHelper.mapCreateTopUp(topUpResponse);
        LOGGER.debug("CreateTopUPFromMerchant Response Biz Bean : {}", createTopupResponseBizBean);
        return new GenericCoreResponseBean<>(createTopupResponseBizBean);
    }

    public static GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> verifyAndReturnResponse(
            final QueryByMerchantTransIdResponse queryByMerchantTransIdResponse) {
        if (!queryByMerchantTransIdResponse.getBody().getResultInfo().getResultCode()
                .equals(ECreateOrderResponses.SUCCESS.getInternalResultCode())) {
            LOGGER.info("Invalid Result returned for CreateTopUP : {}", queryByMerchantTransIdResponse.getBody()
                    .getResultInfo().getResultMsg());
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    queryByMerchantTransIdResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(queryByMerchantTransIdResponse.getBody().getResultInfo()
                    .getResultMsg(), responseConstants);
        }
        final QueryByMerchantTransIDResponseBizBean queryByMerchantResponseBizBean = OrderHelper
                .mapQueryByMerchantResponseBean(queryByMerchantTransIdResponse);
        return new GenericCoreResponseBean<>(queryByMerchantResponseBizBean);
    }

    public static GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> verifyAndReturnResponse(
            final FundUserOrderQueryByMerchantRequestIdResponse queryByRequestIdResponse) {
        if ((queryByRequestIdResponse.getBody() != null)
                && (queryByRequestIdResponse.getBody().getResultInfo() != null)
                && !ECreateOrderResponses.SUCCESS.getInternalResultCode().equals(
                        queryByRequestIdResponse.getBody().getResultInfo().getResultCode())) {
            LOGGER.info("Invalid Result returned for Query by Merchant Request Id : {}", queryByRequestIdResponse
                    .getBody().getResultInfo().getResultMsg());
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    queryByRequestIdResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(queryByRequestIdResponse.getBody().getResultInfo().getResultMsg(),
                    responseConstants);
        }
        QueryByMerchantRequestIdResponseBizBean queryByRequestIdResponseBizBean = OrderHelper
                .mapQueryByMerchantRequestIdResponseBean(queryByRequestIdResponse);
        return new GenericCoreResponseBean<>(queryByRequestIdResponseBizBean);
    }

    private static void validateShippingAmount(String txnAmountReq) {
        if (StringUtils.isBlank(txnAmountReq)) {
            return;
        }
        if (NumberUtils.isNumber(txnAmountReq)) {
            String[] amountSplit = txnAmountReq.split("\\.");
            if (amountSplit.length == 1) {
                validateMantissa(amountSplit);
                return;
            } else if (amountSplit.length == 2) {
                validateMantissa(amountSplit);
                validateExponent(amountSplit);
                return;
            }
        }
        throw new IllegalArgumentException("invalid chargeAmount in shippingInfo");
    }

    private static void validateExponent(String[] amountSplit) {
        if (amountSplit[1].length() > 2) {
            throw new IllegalArgumentException("invalid chargeAmount in shippingInfo" + amountSplit[1]);
        }
    }

    private static void validateMantissa(String[] amountSplit) {
        int mantissa = Integer.parseInt(amountSplit[0]);
        if (mantissa < 0) {
            throw new IllegalArgumentException("ChargeAmount is negative");
        }
    }
}
