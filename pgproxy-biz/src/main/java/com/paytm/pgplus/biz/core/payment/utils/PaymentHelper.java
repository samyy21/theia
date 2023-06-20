/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.payment.utils;

import com.paytm.pgplus.biz.core.model.request.BalanceChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.BizAoaPayOptionBill;
import com.paytm.pgplus.biz.core.model.request.BizAoaPayRequest;
import com.paytm.pgplus.biz.core.model.request.BizPayOptionBill;
import com.paytm.pgplus.biz.core.model.request.BizPayRequest;
import com.paytm.pgplus.biz.core.model.request.BizPreferenceValue;
import com.paytm.pgplus.biz.core.model.request.ChannelAccount;
import com.paytm.pgplus.biz.core.model.request.ChannelAccountQueryRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.ChannelAccountQueryResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.ChannelAccountView;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.EMIChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.ExternalAccountInfoBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.model.request.QueryPayResultRequestBean;
import com.paytm.pgplus.biz.core.model.request.RiskResultBiz;
import com.paytm.pgplus.biz.core.model.request.UPIPushInitiateRequestBean;
import com.paytm.pgplus.biz.core.model.request.UPIPushInitiateResponseBean;
import com.paytm.pgplus.biz.core.model.request.UPIPushTxnRequestParams;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.CostBasedPreferenceUtil;
import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.responsecode.models.CommonResponseCode;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.common.model.RiskResult;
import com.paytm.pgplus.facade.enums.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.payment.models.AoaPayOptionBill;
import com.paytm.pgplus.facade.payment.models.BalanceChannelInfo;
import com.paytm.pgplus.facade.payment.models.ChannelPreference;
import com.paytm.pgplus.facade.payment.models.EMIChannelInfo;
import com.paytm.pgplus.facade.payment.models.ExternalAccountInfo;
import com.paytm.pgplus.facade.payment.models.PayCardOptionView;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.payment.models.PayMethodInfo;
import com.paytm.pgplus.facade.payment.models.PayMethodView;
import com.paytm.pgplus.facade.payment.models.PayOptionBill;
import com.paytm.pgplus.facade.payment.models.PayOptionBill.PayOptionBillBuilder;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.payment.models.PreferenceValue;
import com.paytm.pgplus.facade.payment.models.request.AoaPayRequest;
import com.paytm.pgplus.facade.payment.models.request.AoaPayRequestBody;
import com.paytm.pgplus.facade.payment.models.request.ChannelAccountQueryBody;
import com.paytm.pgplus.facade.payment.models.request.ChannelAccountQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.FetchTokenizedCardsRequest;
import com.paytm.pgplus.facade.payment.models.request.LitePayviewConsultRequest;
import com.paytm.pgplus.facade.payment.models.request.LitePayviewConsultRequestBody;
import com.paytm.pgplus.facade.payment.models.request.PayRequest;
import com.paytm.pgplus.facade.payment.models.request.PayRequestBody;
import com.paytm.pgplus.facade.payment.models.request.PayRequestBody.PayRequestBodyBuilder;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequestBody;
import com.paytm.pgplus.facade.payment.models.request.PayviewConsultRequest;
import com.paytm.pgplus.facade.payment.models.request.PayviewConsultRequestBody;
import com.paytm.pgplus.facade.payment.models.request.PayviewConsultRequestBody.PayviewConsultRequestBodyBuilder;
import com.paytm.pgplus.facade.payment.models.request.UPIPushInitiateRequest;
import com.paytm.pgplus.facade.payment.models.response.*;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.*;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.PayOption;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus.QueryPaymentStatusBuilder;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.WALLET_TYPE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.TXN_TYPE;

import com.paytm.pgplus.facade.payment.enums.UserType;

/**
 * @author namanjain
 *
 */
@Service
public class PaymentHelper {
    @Autowired
    private MerchantResponseUtil merchantResponseUtil;

    @Autowired
    private FF4JUtil ff4JUtil;

    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PaymentHelper.class);
    public static final Double MIN_WALLET_BALANCE = 100D;
    private static final String TXN_TYPE_ADDNPAY = "ADDNPAY";

    public GenericCoreResponseBean<PayviewConsultRequest> consultPayViewRequest(
            final ConsultPayViewRequestBizBean reqBean) {

        final String payerUserId = reqBean.getPayerUserId();// From User Details
        final String transId = reqBean.getTransID();// Fund OrderID

        final EnvInfoRequestBean envInfoReqBean = reqBean.getEnvInfoReqBean();

        // Product code is corresponding to Request Type
        final ProductCodes productCode = getProductCode(reqBean);
        try {
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(envInfoReqBean);
            final TransType transType = TransType.getTransTypeByType(reqBean.getTransType().toString());
            final PayviewConsultRequestBody.PayviewConsultRequestBodyBuilder body = new PayviewConsultRequestBodyBuilder(
                    transId, transType, envInfo);
            body.payerUserId(payerUserId).productCode(productCode);
            body.riskExtendedInfo(reqBean.getRiskExtendInfo());
            body.extendInfo(reqBean.getExtendInfo()).exclusionPayMethods(reqBean.getExclusionPayMethods());

            final PayviewConsultRequest payviewConsultRequest = new PayviewConsultRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.CONSULT_PAYVIEW), body.build());
            return new GenericCoreResponseBean<PayviewConsultRequest>(payviewConsultRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ", e);
            return new GenericCoreResponseBean<PayviewConsultRequest>(e.getMessage());
        }
    }

    public GenericCoreResponseBean<LitePayviewConsultRequest> litePayViewConsultRequest(
            final LitePayviewConsultRequestBizBean reqBean) {

        final String payerUserId = reqBean.getPayerUserId();// From User Details
        final String pwpCategory = reqBean.getPwpCategory();
        final EnvInfoRequestBean envInfoReqBean = reqBean.getEnvInfoRequestBean();
        // Product code is corresponding to Request Type
        final ProductCodes productCode = getProductCode(reqBean);
        try {
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(envInfoReqBean);
            final LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder body = new LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder(
                    productCode.getId(), envInfo);
            body.payerUserId(payerUserId).merchantId(reqBean.getPlatformMid())
                    .customizeCode(reqBean.getCustomizeCode()).exclusionPayMethods(reqBean.getExclusionPayMethods())
                    .extendInfo(extendInfoStr(reqBean.getExtendInfo())).fromAoaMerchant(reqBean.isFromAoaMerchant())
                    .externalUserId(reqBean.getExternalUserId()).fetchSavedAsset(reqBean.getFetchSavedAsset())
                    .fetchSavedAssetForMerchant(reqBean.getFetchSavedAssetForMerchant())
                    .fetchSavedAssetForUser(reqBean.getFetchSavedAssetForUser())
                    .setIncludeDisabledAssets(reqBean.isIncludeDisabledAssets())
                    .verificationType(reqBean.getVerificationType());
            if (ERequestType.NATIVE_PAY.getType().equals(reqBean.getRequestType())) {
                body.excludeChannelAccountInfo(true);
            }
            ApiFunctions apiFunction = ApiFunctions.CONSULT_LITEPAYVIEW;
            if (reqBean.isFromAoaMerchant()) {
                apiFunction = ApiFunctions.AOA_CONSULT_LITEPAYVIEW;
            }
            final LitePayviewConsultRequest litePayviewConsultRequest = new LitePayviewConsultRequest(
                    RequestHeaderGenerator.getHeader(apiFunction), body.build());
            return new GenericCoreResponseBean<>(litePayviewConsultRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    public GenericCoreResponseBean<LitePayviewConsultRequest> litePayViewPayMethodConsultRequest(
            final LitePayviewConsultRequestBizBean reqBean) {

        final String payerUserId = reqBean.getPayerUserId();// From User Details
        final String pwpCategory = reqBean.getPwpCategory();
        final EnvInfoRequestBean envInfoReqBean = reqBean.getEnvInfoRequestBean();
        // Product code is corresponding to Request Type
        ProductCodes productCode;
        if (reqBean.isDealsFlow() && !reqBean.isAddAndPayRequest())
            productCode = ProductCodes.StandardDirectPayDealAcquiringProd;
        else
            productCode = getProductCode(reqBean);
        reqBean.setLpvProductCode(productCode.getId());
        String route = CollectionUtils.isEmpty(reqBean.getExtendInfo()) ? null : reqBean.getExtendInfo().get(
                BizConstant.ExtendedInfoKeys.LPV_ROUTE);
        try {
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(envInfoReqBean);
            final LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder body = new LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder(
                    productCode.getId(), envInfo);
            body.payerUserId(payerUserId).merchantId(reqBean.getPlatformMid());
            if (!Routes.PG2.getName().equals(route)) {
                body.customizeCode(reqBean.getCustomizeCode()).pwpCategory(pwpCategory)
                        .externalUserId(reqBean.getExternalUserId()).fetchSavedAsset(reqBean.getFetchSavedAsset())
                        .fetchSavedAssetForMerchant(reqBean.getFetchSavedAssetForMerchant())
                        .fetchSavedAssetForUser(reqBean.getFetchSavedAssetForUser())
                        .setIncludeDisabledAssets(reqBean.isIncludeDisabledAssets());
            } else {
                body.paytmMerchantId(reqBean.getPaytmMerchantId()).paytmUserId(reqBean.getPaytmUserId())
                        .requestId(RequestIdGenerator.generateRequestId()).isAddNPay(reqBean.isAddAndPayRequest())
                        .emiPlanParam(reqBean.getEmiPlanParam());
            }
            body.exclusionPayMethods(reqBean.getExclusionPayMethods())
                    .extendInfo(extendInfoStr(reqBean.getExtendInfo())).excludeChannelAccountInfo(true)
                    .fromAoaMerchant(reqBean.isFromAoaMerchant()).addAndPayMigration(reqBean.getAddAndPayMigration())
                    .payConfirmFlowType(reqBean.getPayConfirmFlowType())
                    .blockPeriodInSeconds(reqBean.getBlockPeriodInSeconds())
                    .verificationType(reqBean.getVerificationType());

            ApiFunctions apiFunction = ApiFunctions.CONSULT_LITEPAYVIEW;
            if (reqBean.isFromAoaMerchant()) {
                apiFunction = ApiFunctions.AOA_CONSULT_LITEPAYVIEW;
            }
            final LitePayviewConsultRequest litePayviewConsultRequest = new LitePayviewConsultRequest(
                    RequestHeaderGenerator.getHeader(apiFunction), body.build());
            return new GenericCoreResponseBean<>(litePayviewConsultRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    public GenericCoreResponseBean<FetchTokenizedCardsRequest> fetchTokenizeCardsRequest(
            final TokenizedCardsRequestBizBean reqBean) {

        try {
            FetchTokenizedCardsRequest fetchTokenizedCardsRequest = null;
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;

            if (ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.GLOBAL_VAULT_COFT, reqBean.getMid())
                    && !reqBean.isGlobalVaultCoftPreference()) {
                if ((Boolean.TRUE.equals(reqBean.isAddNPayGlobalVaultCards()))) {
                    if (ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.BLACKLIST_GLOBAL_VAULT_ADDNPAY,
                            reqBean.getMid())) {
                        fetchTokenizedCardsRequest = FetchTokenizedCardsRequest.builder()
                                .targetType(UserType.MERCHANT_USER_CARD).externalUserId(reqBean.getExternalUserId())
                                .merchantId(reqBean.getMerchantId()).build();
                    } else {
                        fetchTokenizedCardsRequest = FetchTokenizedCardsRequest.builder()
                                .targetType(UserType.PAYTM_USER_CARD).userId(reqBean.getUserId()).build();
                    }
                } else {
                    merchantExtendedInfoResponse = MappingServiceClient.getMerchantExtendedInfo(reqBean.getMid());
                    Boolean onPaytm = null;
                    if (merchantExtendedInfoResponse != null && merchantExtendedInfoResponse.getExtendedInfo() != null) {
                        onPaytm = merchantExtendedInfoResponse.getExtendedInfo().isOnPaytm();
                    }

                    boolean isPaytmAddMoneyMid = (StringUtils.equals(reqBean.getMid(),
                            ConfigurationUtil.getTheiaProperty(BizConstant.MP_ADD_MONEY_MID)));

                    if ((BooleanUtils.isTrue(onPaytm) || (isPaytmAddMoneyMid || reqBean.isNativeAddMoney()) || reqBean
                            .isStaticQrCode())
                            && !ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.BLACKLIST_GLOBAL_VAULT,
                                    reqBean.getMid())) {
                        fetchTokenizedCardsRequest = FetchTokenizedCardsRequest.builder()
                                .targetType(UserType.PAYTM_USER_CARD).userId(reqBean.getUserId()).build();
                    } else {
                        fetchTokenizedCardsRequest = FetchTokenizedCardsRequest.builder()
                                .targetType(UserType.MERCHANT_USER_CARD).externalUserId(reqBean.getExternalUserId())
                                .merchantId(reqBean.getMerchantId()).build();
                    }
                }
            } else {
                fetchTokenizedCardsRequest = FetchTokenizedCardsRequest.builder().targetType(reqBean.getTargetType())
                        .externalUserId(reqBean.getExternalUserId()).merchantId(reqBean.getMerchantId())
                        .userId(reqBean.getUserId()).build();
            }
            return new GenericCoreResponseBean<FetchTokenizedCardsRequest>(fetchTokenizedCardsRequest);

        } catch (final Exception e) {
            LOGGER.error(
                    "Exception Occurred while creating tokenized Cards request for MID, cust ID, user ID {}, {}, {}",
                    reqBean.getMerchantId(), reqBean.getExternalUserId(), reqBean.getUserId(), e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    private String extendInfoStr(Map<String, String> extendInfo) throws FacadeCheckedException {
        return (extendInfo != null) && !extendInfo.isEmpty() ? JsonMapper.mapObjectToJson(extendInfo) : null;
    }

    private ProductCodes getProductCode(final ConsultPayViewRequestBizBean reqBean) {
        if (ERequestType.ADD_AND_PAY.equals(reqBean.getProductCode())) {
            return ProductCodes.valueOf(reqBean.getProductCode().getProductCode());
        } else if (ERequestType.DYNAMIC_QR_2FA.equals(reqBean.getProductCode())) {
            return ProductCodes.ScanNPayChargePayer;
        } else if (reqBean.isSlabBasedMDR()) {
            return ProductCodes.StandardAcquiringProdByAmountChargePayer;
        } else if (reqBean.isPostConvenienceFee()) {
            return ProductCodes.StandardDirectPayAcquiringProdChargePayer;
        } else {
            return ProductCodes.valueOf(reqBean.getProductCode().getProductCode());
        }
    }

    private ProductCodes getProductCode(final LitePayviewConsultRequestBizBean reqBean) {
        ProductCodes productCode = null;
        if (reqBean.isDynamicFeeMerchant()) {
            productCode = ((reqBean.isDefaultDynamicFeeMerchantPayment()) ? ProductCodes.StandardDirectPayDynamicTargetProd
                    : ProductCodes.valueOf(reqBean.getProductCode().getProductCode()));
        } else if (reqBean.isPostConvenienceFee()) {
            productCode = ProductCodes.StandardDirectPayAcquiringProdChargePayer;
        } else {
            productCode = ProductCodes.valueOf(reqBean.getProductCode().getProductCode());
        }
        return ERequestType.ADD_AND_PAY.equals(reqBean.getProductCode()) ? ProductCodes.valueOf(reqBean
                .getProductCode().getProductCode()) : productCode;
    }

    public GenericCoreResponseBean<PayRequest> payRequest(final BizPayRequest bizPayRequest) {

        try {
            String requestID = bizPayRequest.getRequestID();
            TransType transType = TransType.valueOf(bizPayRequest.getTransType().getType());
            EnvInfo envInfo = bizPayRequest.getEnvInfo();

            if (!TerminalType.WEB.equals(envInfo.getTerminalType())) {
                Map<String, String> extendedInfo = envInfo.getExtendInfo();
                if (extendedInfo == null) {
                    extendedInfo = new HashMap<>();
                }
                extendedInfo.put("deviceId", envInfo.getTokenId());
            }

            boolean isPG2Route = PaymentAdapterUtil.eligibleForPG2(bizPayRequest.getExtInfo());

            List<PayOptionBill> payOptionBill = generatePayOptionBill(bizPayRequest.getPayOptionBills(), transType,
                    isPG2Route);

            PayRequestBody.PayRequestBodyBuilder bodyBulider = new PayRequestBodyBuilder(bizPayRequest.getTransID(),
                    transType, requestID, payOptionBill, envInfo);

            if (TransType.TOP_UP.equals(transType) && isPG2Route) {
                if (MapUtils.isNotEmpty(bizPayRequest.getExtInfo()))
                    bodyBulider.payerUserId(bizPayRequest.getExtInfo().getOrDefault(
                            BizConstant.ExtendedInfoKeys.PAYTM_USER_ID, ""));
            } else {
                bodyBulider.payerUserId(bizPayRequest.getPayerUserID());
            }

            bodyBulider.securityId(bizPayRequest.getSecurityId());
            bodyBulider.extendInfo(bizPayRequest.getExtInfo());
            bodyBulider.riskExtendInfo(bizPayRequest.getRiskExtendInfo());
            bodyBulider.paymentScenario(bizPayRequest.getPaymentScenario());
            bodyBulider.pwpCategory(bizPayRequest.getPwpCategory());
            bodyBulider.addAndPayMigration(String.valueOf(bizPayRequest.isAddAndPayMigration()));
            bodyBulider.paymentBizInfo(bizPayRequest.getPaymentBizInfo());
            bodyBulider.paytmMerchantId(bizPayRequest.getPaytmMerchantId());
            bodyBulider.paymentFlow(bizPayRequest.getRequestFlow());
            bodyBulider.ultimateBeneficiaryDetails(bizPayRequest.getUltimateBeneficiaryDetails());
            bodyBulider.verificationType(bizPayRequest.getVerificationType());
            PayRequest payRequest = new PayRequest(RequestHeaderGenerator.getHeader(ApiFunctions.CASHIER_PAY),
                    bodyBulider.build());

            boolean isAddNPayTransaction = isAddNPayTransaction(payRequest);
            boolean isTopUpTransaction = isTopUpTransaction(payRequest);
            // Adding cost preferred merchant gateways for cost based routing
            if (isAddNPayTransaction || isTopUpTransaction || bizPayRequest.isCostBasedPreferenceEnabled()) {
                CostBasedPreferenceUtil.setCostPreferredGateways(payOptionBill, isAddNPayTransaction,
                        isTopUpTransaction, null);
            }

            LOGGER.debug("PayRequest Generated : {}", payRequest);
            return new GenericCoreResponseBean<>(payRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occurred while creating PayRequest, {}", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }

    }

    private boolean isAddNPayTransaction(PayRequest payRequest) {
        boolean isAddnPay = false;
        if (payRequest.getBody() != null && payRequest.getBody().getExtendInfo() != null) {
            // check for AddnPay
            if (payRequest.getBody().getExtendInfo().containsKey(TXN_TYPE)) {
                String txntype = payRequest.getBody().getExtendInfo().get(TXN_TYPE);
                if (StringUtils.isNotEmpty(txntype)) {
                    if (txntype.equalsIgnoreCase(TXN_TYPE_ADDNPAY)) {
                        isAddnPay = true;
                    }
                }
            }
        }
        return isAddnPay;
    }

    private boolean isTopUpTransaction(PayRequest payRequest) {
        boolean isTopUpnpay = false;
        // check for topup
        TransType transtype = payRequest.getBody().getTransType();
        if (transtype != null && transtype.equals(TransType.TOP_UP)) {
            isTopUpnpay = true;
        }
        return isTopUpnpay;
    }

    public GenericCoreResponseBean<AoaPayRequest> aoaPayRequest(final BizAoaPayRequest bizAoaPayRequest) {

        try {
            String requestID = bizAoaPayRequest.getRequestID();
            EnvInfo envInfo = bizAoaPayRequest.getEnvInfo();

            if (!TerminalType.WEB.equals(envInfo.getTerminalType())) {
                Map<String, String> extendedInfo = envInfo.getExtendInfo();
                if (extendedInfo == null) {
                    extendedInfo = new HashMap<>();
                }
                extendedInfo.put("deviceId", envInfo.getTokenId());
            }

            List<AoaPayOptionBill> aoaPayOptionBill = generateAoaPayOptionBill(bizAoaPayRequest.getPayOptionBills());

            AoaPayRequestBody aoaPayRequestBody = new AoaPayRequestBody();
            aoaPayRequestBody.setPayOptionBills(aoaPayOptionBill);
            aoaPayRequestBody.setTransId(bizAoaPayRequest.getTransID());
            aoaPayRequestBody.setExtendInfo(JsonMapper.mapObjectToJson(bizAoaPayRequest.getExtInfo()));
            aoaPayRequestBody.setEnvInfo(envInfo);
            aoaPayRequestBody.setRequestId(requestID);

            ChannelPreference channelPreference = null;
            List<PreferenceValue> preferenceValues = null;
            if (null != bizAoaPayRequest && null != bizAoaPayRequest.getChannelPreference()) {
                channelPreference = new ChannelPreference();
                preferenceValues = new ArrayList<>();
                for (BizPreferenceValue bizPreferenceValue : bizAoaPayRequest.getChannelPreference()
                        .getPreferenceValues()) {
                    preferenceValues.add(new PreferenceValue(bizPreferenceValue.getServiceInstId(), bizPreferenceValue
                            .getScore()));
                }
                channelPreference.setPreferenceValues(preferenceValues);
                channelPreference.setExtendInfo(bizAoaPayRequest.getChannelPreference().getExtendInfo());
            }

            aoaPayRequestBody.setChannelPreference(channelPreference);

            AoaPayRequest aoaPayRequest = new AoaPayRequest(aoaPayRequestBody,
                    RequestHeaderGenerator.getHeader(ApiFunctions.AOA_CASHIER_PAY));

            LOGGER.debug("PayRequest Generated : {}", aoaPayRequest);
            return new GenericCoreResponseBean<>(aoaPayRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception occurred while creating PayRequest, {}", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }

    }

    public GenericCoreResponseBean<PayResultQueryRequest> queryPayResultRequest(
            final QueryPayResultRequestBean queryPayResultRequestBean) {
        try {
            ApiFunctions apiFunction = ApiFunctions.QUERY_PAYRESULT;
            if (queryPayResultRequestBean.isFromAoaMerchant()) {
                apiFunction = ApiFunctions.AOA_QUERY_PAYRESULT;
            }
            final PayResultQueryRequest payResultQueryRequest = new PayResultQueryRequest(
                    RequestHeaderGenerator.getHeader(apiFunction), new PayResultQueryRequestBody(
                            queryPayResultRequestBean.getCashierRequestId(),
                            queryPayResultRequestBean.isFromAoaMerchant(), queryPayResultRequestBean.getRoute()));
            return new GenericCoreResponseBean<PayResultQueryRequest>(payResultQueryRequest);
        } catch (Exception e) {
            return new GenericCoreResponseBean<PayResultQueryRequest>(e.getMessage());
        }
    }

    public UPIPushInitiateRequest mapUpiPushInitiateRequest(UPIPushInitiateRequestBean requestBean) {
        UPIPushInitiateRequest initiateRequest = new UPIPushInitiateRequest();
        UPIPushTxnRequestParams txnParams = requestBean.getUpiPushTransactionParams();
        initiateRequest.setAmount(requestBean.getAmount());
        initiateRequest.setExternalSrNo(requestBean.getExternalSrNo());
        initiateRequest.setUrl(requestBean.getRequestUrl());
        initiateRequest.setAccountNumber(txnParams.getAccountNumber());
        initiateRequest.setBankName(txnParams.getBankName());
        initiateRequest.setCredBlock(txnParams.getCredBlock());
        initiateRequest.setDeviceId(txnParams.getDeviceId());
        initiateRequest.setIfsc(txnParams.getIfsc());
        initiateRequest.setMobileNo(txnParams.getMobileNo());
        initiateRequest.setMpin(txnParams.getMpin());
        initiateRequest.setPayerVpa(txnParams.getPayerVpa());
        initiateRequest.setSeqNo(txnParams.getSeqNo());
        initiateRequest.setAppId(txnParams.getAppId());
        return initiateRequest;
    }

    public GenericCoreResponseBean<UPIPushInitiateResponseBean> mapUpiPushInitiateResponse(
            UPIPushInitiateResponse response) {
        if (!response.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<UPIPushInitiateResponseBean>(response.getResponseMessage());
        }
        // TODO
        UPIPushInitiateResponseBean responseBean = new UPIPushInitiateResponseBean();
        return new GenericCoreResponseBean<UPIPushInitiateResponseBean>(responseBean);
    }

    public QueryPaymentStatus mapQueryPayResult(PayResultQueryResponse PayResultQueryResponse) {
        PayResultQueryResponseBody responseBody = PayResultQueryResponse.getBody();

        QueryPaymentStatusBuilder builder = new QueryPaymentStatusBuilder(responseBody.getTransId(), responseBody
                .getTransType().getType(), responseBody.getTransAmount().getCurrency().getCurrency(), responseBody
                .getTransAmount().getAmount(), responseBody.getPayerUserId(), responseBody.getPaymentStatus().name());
        builder.setActualInstErrorCode(responseBody.getInstErrorCode());

        // Set response-details
        if (StringUtils.isNotBlank(responseBody.getInstErrorCode())) {
            CommonResponseCode commonResponseCode = new CommonResponseCode();
            commonResponseCode.setAlipayRespCode(responseBody.getInstErrorCode());
            ResponseCodeDetails responseCodeDetails = merchantResponseUtil
                    .getMerchantRespCodeDetails(commonResponseCode);
            if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
                builder.setPaytmResponseCode(responseCodeDetails.getResponseCode()).setErrorMessage(
                        StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                                .getDisplayMessage()
                                : StringUtils.isNotBlank(responseCodeDetails.getRemark()) ? responseCodeDetails
                                        .getRemark() : "");
            }
        } else if (StringUtils.isNotBlank(responseBody.getPaymentErrorCode())) {
            CommonResponseCode commonResponseCode = new CommonResponseCode();
            commonResponseCode.setAlipayRespCode(responseBody.getPaymentErrorCode());
            ResponseCodeDetails responseCodeDetails = merchantResponseUtil
                    .getMerchantRespCodeDetails(commonResponseCode);
            if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
                builder.setErrorMessage(StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                        .getDisplayMessage()
                        : StringUtils.isNotBlank(responseCodeDetails.getRemark()) ? responseCodeDetails.getRemark()
                                : "");
            }
        }

        builder.setWebFormContext(responseBody.getWebFormContext())
                .setResultPageRedirectURL(responseBody.getResultPageRedirectURL())
                .setPaymentErrorCode(responseBody.getPaymentErrorCode())
                .setInstErrorCode(responseBody.getInstErrorCode()).setPaidTime(responseBody.getPaidTime())
                .setExtendInfo(responseBody.getExtendInfo()).setPwpCategory(responseBody.getPwpCategory());

        List<PayOption> payOptions = new ArrayList<>();

        for (PayOptionInfo payOptionInfo : responseBody.getPayOptionInfos()) {
            PayOption payOption = new PayOption(payOptionInfo.getPayMethod().getMethod(), payOptionInfo.getPayMethod()
                    .getOldName(), payOptionInfo.getPayAmount().getCurrency().getCurrency(), payOptionInfo
                    .getPayAmount().getAmount(), payOptionInfo.getExtendInfo(),
                    payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount().getCurrency().getCurrency()
                            : payOptionInfo.getTransAmount().getCurrency().getCurrency(),
                    payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount().getAmount() : payOptionInfo
                            .getTransAmount().getAmount(), payOptionInfo.getChargeAmount() != null ? payOptionInfo
                            .getChargeAmount().getAmount() : null, payOptionInfo.getPayChannelInfo());
            payOptions.add(payOption);
        }

        builder.setPayOptions(payOptions);

        return builder.build();

    }

    public ConsultWalletLimitsRequest createConsultWalletLimitRequest(final Double addMoneyAmount, final String userId,
            final String orderId) {
        return new ConsultWalletLimitsRequest(addMoneyAmount, userId, orderId);
    }

    private List<PayOptionBill> generatePayOptionBill(List<BizPayOptionBill> payOptionBiz, TransType transType,
            boolean isPG2Route) {

        if (CollectionUtils.isEmpty(payOptionBiz) || payOptionBiz.get(0) == null) {
            EXT_LOGGER.customInfo("payOptionBiz is empty");
            return Collections.emptyList();
        }

        List<PayOptionBill> payOptionBill = new ArrayList<>();
        PayOptionBill.PayOptionBillBuilder payOptionBuilder = new PayOptionBillBuilder();

        Iterator<BizPayOptionBill> payOptionBillIterator = payOptionBiz.iterator();
        while (payOptionBillIterator.hasNext()) {
            BizPayOptionBill bizPayOptionBill = payOptionBillIterator.next();
            try {
                String chargeAmount = bizPayOptionBill.getChargeAmount();
                if (StringUtils.isEmpty(chargeAmount)) {
                    chargeAmount = "0";
                }

                payOptionBuilder.payOption(bizPayOptionBill.getPayOption());
                payOptionBuilder.payMode(bizPayOptionBill.getPayMode());
                payOptionBuilder.payMethod(PayMethod.getPayMethodByMethod(bizPayOptionBill.getPayMethod().toString()));
                payOptionBuilder.transAmount(new Money(EnumCurrency.INR, bizPayOptionBill.getTransAmount()));
                payOptionBuilder.chargeAmount(new Money(EnumCurrency.INR, chargeAmount));
                payOptionBuilder.channelInfo(bizPayOptionBill.getChannelInfo());
                payOptionBuilder.extendInfo(bizPayOptionBill.getExtendInfo());
                payOptionBuilder.cardCacheToken(bizPayOptionBill.getCardCacheToken());
                payOptionBuilder.payerAccountNo(bizPayOptionBill.getPayerAccountNo());
                payOptionBuilder.extendInfo(bizPayOptionBill.getExtendInfo());
                payOptionBuilder.channelInfo(bizPayOptionBill.getChannelInfo());
                payOptionBuilder.topupAndPay(bizPayOptionBill.isTopupAndPay());
                payOptionBuilder.mgvTemplateIds(bizPayOptionBill.getTemplateIds());
                payOptionBuilder.prepaidCard(bizPayOptionBill.isPrepaidCard());
                payOptionBuilder.feeRateFactorsInfo(bizPayOptionBill.getFeeRateFactorsInfo());
                payOptionBuilder.dccPaymentInfo(bizPayOptionBill.getDccPaymentInfo());
                payOptionBuilder.virtualPaymentAddr(bizPayOptionBill.getVirtualPaymentAddr());
                payOptionBuilder.saveCardAfterPay(bizPayOptionBill.isSaveCardAfterPay());
                payOptionBuilder.saveAssetForMerchant(bizPayOptionBill.isSaveAssetForMerchant());
                payOptionBuilder.saveAssetForUser(bizPayOptionBill.isSaveAssetForUser());
                payOptionBuilder.vanInfo(bizPayOptionBill.getVanInfo());
                payOptionBuilder.tpvInfos(bizPayOptionBill.getTpvInfos());
                payOptionBuilder.payConfirmFlowType(bizPayOptionBill.getPayConfirmFlowType());
                payOptionBuilder.lastFourDigits(bizPayOptionBill.getLastFourDigits());
                payOptionBuilder.settleType(bizPayOptionBill.getSettleType());
                payOptionBuilder.isDeepLinkFlow(bizPayOptionBill.isDeepLinkFlow());
                payOptionBuilder.isUpiCC(bizPayOptionBill.isUpiCC());

                if (TransType.TOP_UP.equals(transType) && isPG2Route) {
                    if (MapUtils.isNotEmpty(bizPayOptionBill.getExtendInfo()))
                        payOptionBuilder.payerAccountNo(bizPayOptionBill.getExtendInfo().getOrDefault(
                                BizConstant.ExtendedInfoKeys.PAYTM_USER_ID, ""));
                    payOptionBuilder.vpaAddress(bizPayOptionBill.getVirtualPaymentAddr());
                }

                payOptionBill.add(payOptionBuilder.build());

            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Facade invalid parameter exception while generating payOptions :", e);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Facade checked exception while generating payOptions :", e);
            }
        }
        if (payOptionBill.isEmpty()) {
            return Collections.emptyList();
        } else {
            return payOptionBill;
        }
    }

    private List<AoaPayOptionBill> generateAoaPayOptionBill(List<BizAoaPayOptionBill> aoaPayOptionBiz) {

        if (CollectionUtils.isEmpty(aoaPayOptionBiz) || aoaPayOptionBiz.get(0) == null) {
            EXT_LOGGER.customInfo("payOptionBiz is empty");
            return Collections.emptyList();
        }

        List<AoaPayOptionBill> aoaPayOptionBill = new ArrayList<>();
        Iterator<BizAoaPayOptionBill> aoaPayOptionBillIterator = aoaPayOptionBiz.iterator();
        AoaPayOptionBill aoaPayOptionBills = null;
        while (aoaPayOptionBillIterator.hasNext()) {
            BizAoaPayOptionBill aoaBizPayOptionBill = aoaPayOptionBillIterator.next();
            try {
                aoaPayOptionBills = new AoaPayOptionBill();
                aoaPayOptionBills.setPayOption(aoaBizPayOptionBill.getPayOption());
                aoaPayOptionBills.setPayMethod(PayMethod.getPayMethodByMethod(aoaBizPayOptionBill.getPayMethod()
                        .toString()));
                aoaPayOptionBills.setTransAmount(new Money(EnumCurrency.INR, aoaBizPayOptionBill.getTransAmount()));
                aoaPayOptionBills.setChannelInfo(JsonMapper.mapObjectToJson(aoaBizPayOptionBill.getChannelInfo()));
                aoaPayOptionBills.setExtendInfo(JsonMapper.mapObjectToJson(aoaBizPayOptionBill.getExtendInfo()));
                aoaPayOptionBills.setCardCacheToken(aoaBizPayOptionBill.getCardCacheToken());
                aoaPayOptionBills.setPrepaidCard(aoaBizPayOptionBill.isPrepaidCard());
                aoaPayOptionBills.setFeeRateFactorsInfo(JsonMapper.mapObjectToJson(aoaBizPayOptionBill
                        .getFeeRateFactorsInfo()));

                aoaPayOptionBill.add(aoaPayOptionBills);

            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Facade invalid parameter exception while generating payOptions :", e);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Facade checked exception while generating payOptions :", e);
            }
        }
        if (aoaPayOptionBill.isEmpty()) {
            return Collections.emptyList();
        } else {
            return aoaPayOptionBill;
        }
    }

    public GenericCoreResponseBean<ConsultPayViewResponseBizBean> mapResponseForConsult(
            final PayviewConsultResponse payViewConsultresponse) {
        if ((payViewConsultresponse == null) || (payViewConsultresponse.getBody() == null)
                || (payViewConsultresponse.getBody().getResultInfo() == null)) {
            return new GenericCoreResponseBean<ConsultPayViewResponseBizBean>(BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE,
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (!BizConstant.SUCCESS.equals(payViewConsultresponse.getBody().getResultInfo().getResultCode())) {
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    payViewConsultresponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<ConsultPayViewResponseBizBean>(BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE,
                    responseConstants);
        }
        // This service maps facade response to Biz Module Bean
        final ConsultPayViewResponseBizBean consultBean = mapconsultPayView(payViewConsultresponse);
        return new GenericCoreResponseBean<ConsultPayViewResponseBizBean>(consultBean);
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> mapResponseForConsult(
            final LitePayviewConsultResponse litePayviewConsultResponse, boolean defaultLitePayView) {
        if ((litePayviewConsultResponse == null) || (litePayviewConsultResponse.getBody() == null)
                || (litePayviewConsultResponse.getBody().getResultInfo() == null)) {
            return new GenericCoreResponseBean<LitePayviewConsultResponseBizBean>(
                    BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE, ResponseConstants.SYSTEM_ERROR);
        }
        if (!BizConstant.SUCCESS.equals(litePayviewConsultResponse.getBody().getResultInfo().getResultCode())) {
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    litePayviewConsultResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<LitePayviewConsultResponseBizBean>(
                    BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE, responseConstants);
        }
        // This service maps facade response to Biz Module Bean
        final LitePayviewConsultResponseBizBean consultBean = mapconsultPayView(litePayviewConsultResponse,
                defaultLitePayView);
        return new GenericCoreResponseBean<LitePayviewConsultResponseBizBean>(consultBean);
    }

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> mapResponseForLitePayViewConsult(
            final LitePayviewConsultResponse litePayviewConsultResponse, boolean defaultLitePayView) {
        if ((litePayviewConsultResponse == null) || (litePayviewConsultResponse.getBody() == null)
                || (litePayviewConsultResponse.getBody().getResultInfo() == null)) {
            return new GenericCoreResponseBean<>(BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE,
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (!BizConstant.SUCCESS.equals(litePayviewConsultResponse.getBody().getResultInfo().getResultCode())) {
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    litePayviewConsultResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(BizConstant.FETCH_PAY_MODE_ERROR_MESSAGE, responseConstants);
        }
        // This service maps facade response to Biz Module Bean
        final LitePayviewConsultResponseBizBean consultBean = mapLiteConsultPayView(litePayviewConsultResponse,
                defaultLitePayView);
        return new GenericCoreResponseBean<>(consultBean);
    }

    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> mapResponseForTokenizedCards(
            final FetchTokenizedCardsResponse tokenizedCardsResponse) {
        LOGGER.info("mapping tokenized cards response{}", tokenizedCardsResponse);

        if (tokenizedCardsResponse == null) {
            return new GenericCoreResponseBean<>(BizConstant.FETCH_TOKENIZED_CARDS_ERROR_MESSAGE,
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (!BizConstant.SUCCESS.equals(tokenizedCardsResponse.getStatus())) {
            ResponseConstants responseConstant = ResponseConstants
                    .fetchResponseConstantByCardServiceRespCode(tokenizedCardsResponse.getResponseCode());
            return new GenericCoreResponseBean<>(BizConstant.FETCH_TOKENIZED_CARDS_ERROR_MESSAGE, responseConstant);
        }
        // This service maps facade response to Biz Module Bean
        final TokenizedCardsResponseBizBean tokenizedResponse = mapTokenizedResponse(tokenizedCardsResponse);
        return new GenericCoreResponseBean<TokenizedCardsResponseBizBean>(tokenizedResponse);
    }

    public ConsultPayViewResponseBizBean mapconsultPayView(final PayviewConsultResponse consultPayView) {

        final PayviewConsultResponseBody consultPayViewBody = consultPayView.getBody();
        // Map Elements of facade object to local bean transInfoBean
        ConsultPayViewResponseBizBean consultPayViewResponseBean = new ConsultPayViewResponseBizBean();

        consultPayViewResponseBean.setTransDesc(consultPayViewBody.getTransDesc());
        consultPayViewResponseBean.setTransAmount(consultPayViewBody.getTransAmount().getAmount());
        consultPayViewResponseBean.setProductCode(ERequestType.getByProductCode(consultPayViewBody.getProductCode()
                .getProductCode()));
        consultPayViewResponseBean.setSecurityId(consultPayViewBody.getSecurityId());
        consultPayViewResponseBean.setChargePayer(consultPayViewBody.isChargePayer());
        consultPayViewResponseBean.setTransCreatedTime(consultPayViewBody.getTransCreatedTime());

        // Map payMethodViews
        consultPayViewResponseBean = mapPayMethodViews(consultPayViewResponseBean, consultPayViewBody);
        // Map ExtendedInfo
        if (!consultPayViewBody.getExtendInfo().isEmpty()) {
            mapExtendedInfo(consultPayViewResponseBean, consultPayViewBody);
        }

        return consultPayViewResponseBean;
    }

    public LitePayviewConsultResponseBizBean mapconsultPayView(
            final LitePayviewConsultResponse litePayviewConsultResponse, boolean defaultLitePayViewFlow) {

        final LitePayviewConsultResponseBody litePayviewConsultResponseBody = litePayviewConsultResponse.getBody();
        // Map Elements of facade object to local bean transInfoBean
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = new LitePayviewConsultResponseBizBean();
        litePayviewConsultResponseBizBean.setChargePayer(litePayviewConsultResponseBody.isChargePayer());
        setPayMethodViews(litePayviewConsultResponseBizBean, litePayviewConsultResponseBody, defaultLitePayViewFlow);
        litePayviewConsultResponseBizBean.setExtendInfo(extendInfoStrToMap(litePayviewConsultResponseBody
                .getContractExtendInfo()));
        litePayviewConsultResponseBizBean.setTolerableErrorCodes(litePayviewConsultResponseBody
                .getTolerableErrorCodes());
        return litePayviewConsultResponseBizBean;
    }

    private LitePayviewConsultResponseBizBean mapLiteConsultPayView(
            final LitePayviewConsultResponse litePayviewConsultResponse, boolean defaultLitePayView) {

        final LitePayviewConsultResponseBody litePayviewConsultResponseBody = litePayviewConsultResponse.getBody();
        // Map Elements of facade object to local bean transInfoBean
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = new LitePayviewConsultResponseBizBean();
        litePayviewConsultResponseBizBean.setChargePayer(litePayviewConsultResponseBody.isChargePayer());
        setPayMethodViewsOfLitePayView(litePayviewConsultResponseBizBean, litePayviewConsultResponseBody,
                defaultLitePayView);
        litePayviewConsultResponseBizBean.setExtendInfo(extendInfoStrToMap(litePayviewConsultResponseBody
                .getContractExtendInfo()));
        litePayviewConsultResponseBizBean.setTolerableErrorCodes(litePayviewConsultResponseBody
                .getTolerableErrorCodes());
        litePayviewConsultResponseBizBean.setPwpEnabled(litePayviewConsultResponseBody.getPwpEnabled());
        litePayviewConsultResponseBizBean.setSourceSystem(litePayviewConsultResponseBody.getSourceSystem());
        litePayviewConsultResponseBizBean.setMerchantRemainingLimits(litePayviewConsultResponseBody
                .getMerchantRemainingLimits());
        return litePayviewConsultResponseBizBean;
    }

    private TokenizedCardsResponseBizBean mapTokenizedResponse(
            final FetchTokenizedCardsResponse fetchTokenizedCardsResponse) {
        // Map Elements of facade object to local bean
        LOGGER.info("Mapping tokenized response to biz bean {}", fetchTokenizedCardsResponse);
        return new TokenizedCardsResponseBizBean(fetchTokenizedCardsResponse.getCardInfos());
    }

    private ConsultPayViewResponseBizBean mapPayMethodViews(
            final ConsultPayViewResponseBizBean consultPayViewresponseBean,
            final PayviewConsultResponseBody consultPayViewBody) {

        // Alipay facade object
        final List<PayMethodView> payMethodViewsAlipay = consultPayViewBody.getPayMethodViews();
        // Need to fetch wallet balance for EMI hybrid
        String serviceAmountForEmiHybrid = null;
        for (final PayMethodView payViewAlipay : payMethodViewsAlipay) {
            if (payViewAlipay.getPayMethod().getMethod().equals(PayMethod.BALANCE.getMethod())
                    && payViewAlipay.getPayChannelOptionViews().get(0).isEnableStatus()) {

                List<BalanceChannelInfo> balanceChannelInfo = payViewAlipay.getPayChannelOptionViews().get(0)
                        .getBalanceChannelInfos();
                if (!CollectionUtils.isEmpty(balanceChannelInfo)) {
                    Double walletBalance = Double
                            .parseDouble(balanceChannelInfo.get(0).getAccountBalance().getAmount());

                    /** For the Hybrid transaction to pure PG transaction */
                    if (walletBalance < MIN_WALLET_BALANCE) {
                        walletBalance = 0D;
                    }

                    Double txnAmount = Double.parseDouble(consultPayViewBody.getTransAmount().getAmount());
                    if (txnAmount > walletBalance) {
                        Double differenceAmount = txnAmount - walletBalance;
                        serviceAmountForEmiHybrid = String.valueOf(differenceAmount);
                    }
                }
            }
        }

        // Local Object
        final List<PayMethodViewsBiz> payMethodViewBizList = new ArrayList<PayMethodViewsBiz>();

        for (final PayMethodView payViewAlipay : payMethodViewsAlipay) {
            final PayMethodViewsBiz payMethodViewBiz = new PayMethodViewsBiz();
            payMethodViewBiz.setPayMethod(payViewAlipay.getPayMethod().getMethod());
            payMethodViewBiz.setRiskResult(mapRiskResult(payViewAlipay.getRiskResult()));

            payMethodViewBiz.setPayChannelOptionViews(mapPayChannelOptionView(payViewAlipay.getPayChannelOptionViews(),
                    consultPayViewBody.getTransAmount().getAmount(), serviceAmountForEmiHybrid));

            if (payViewAlipay.getPayMethod().equals(PayMethod.BALANCE)
                    && !payMethodViewBiz.getPayChannelOptionViews().get(0).isEnableStatus()) {
                consultPayViewresponseBean.setWalletFailed(true);
            }
            payMethodViewBiz.setPayCardOptionViews(getPayCardOptionView(payViewAlipay.getPayCardOptionViews()));// //
            // Create

            payMethodViewBizList.add(payMethodViewBiz);
        }

        // For Wallet Only login is mandatory
        if ((payMethodViewsAlipay.size() == 1) && WALLET_TYPE.equals(payMethodViewBizList.get(0).getPayMethod())) {
            consultPayViewresponseBean.setWalletOnly(true);
        } else if (payMethodViewsAlipay.isEmpty()) {
            consultPayViewresponseBean.setLoginMandatory(true);
        }

        // Check if PPB is enabled as NB channel on merchant
        // TODO
        consultPayViewresponseBean.setPaymentsBankSupported(checkPpbNbIsEnabled(payMethodViewBizList));

        consultPayViewresponseBean.setPayMethodViews(payMethodViewBizList);
        return consultPayViewresponseBean;
    }

    private LitePayviewConsultResponseBizBean setPayMethodViews(
            final LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean,
            final LitePayviewConsultResponseBody litePayviewConsultResponseBody, boolean defaultLitePayViewFlow) {

        // Alipay facade object
        final List<PayMethodView> payMethodViewsAlipay = litePayviewConsultResponseBody.getPayMethodViews();

        // Local Object
        final List<PayMethodViewsBiz> payMethodViewBizList = new ArrayList<PayMethodViewsBiz>();

        for (final PayMethodView payViewAlipay : payMethodViewsAlipay) {
            if (defaultLitePayViewFlow && PayMethod.EMI_DC.equals(payViewAlipay.getPayMethod())) {
                continue;
            }
            final PayMethodViewsBiz payMethodViewBiz = new PayMethodViewsBiz();
            payMethodViewBiz.setPayMethod(payViewAlipay.getPayMethod().getMethod());
            payMethodViewBiz.setRiskResult(mapRiskResult(payViewAlipay.getRiskResult()));

            payMethodViewBiz
                    .setPayChannelOptionViews(mapPayChannelOptionView(payViewAlipay.getPayChannelOptionViews()));

            if (PayMethod.BALANCE.equals(payViewAlipay.getPayMethod())
                    && !payMethodViewBiz.getPayChannelOptionViews().get(0).isEnableStatus()) {
                litePayviewConsultResponseBizBean.setWalletFailed(true);
            }

            payMethodViewBiz.setPayCardOptionViews(getPayCardOptionView(payViewAlipay.getPayCardOptionViews()));
            payMethodViewBizList.add(payMethodViewBiz);
        }
        if ((payMethodViewsAlipay.size() == 1) && WALLET_TYPE.equals(payMethodViewBizList.get(0).getPayMethod())) {
            litePayviewConsultResponseBizBean.setWalletOnly(true);
        } else if (payMethodViewsAlipay.isEmpty()) {
            litePayviewConsultResponseBizBean.setLoginMandatory(true);
        }

        if (defaultLitePayViewFlow) {
            for (PayMethodViewsBiz payMethodView : payMethodViewBizList) {
                if (!EPayMethod.NET_BANKING.getMethod().equals(payMethodView.getPayMethod())) {
                    continue;
                }

                List<PayChannelOptionViewBiz> payChannelOptionViews = payMethodView.getPayChannelOptionViews();
                for (Iterator<PayChannelOptionViewBiz> iterator = payChannelOptionViews.iterator(); iterator.hasNext();) {
                    PayChannelOptionViewBiz payChannelOptionView = iterator.next();
                    if (StringUtils.isNotBlank(payChannelOptionView.getInstId())
                            && payChannelOptionView.getInstId().equals("PPBL")) {
                        if (payChannelOptionView.isEnableStatus()) {
                            litePayviewConsultResponseBizBean.setPaymentsBankSupported(true);
                        }
                        iterator.remove();
                        continue;
                    }
                    if (StringUtils.isNotBlank(payChannelOptionView.getInstId())
                            && ("ZEST").equals(payChannelOptionView.getInstId())) {
                        EXT_LOGGER.customInfo("Removing Zest from Net Banking List");
                        iterator.remove();
                    }
                }
            }
        }
        litePayviewConsultResponseBizBean.setPayMethodViews(payMethodViewBizList);
        return litePayviewConsultResponseBizBean;
    }

    private void setPayMethodViewsOfLitePayView(
            final LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean,
            final LitePayviewConsultResponseBody litePayviewConsultResponseBody, boolean defaultLitePayView) {

        // Alipay facade object
        final List<PayMethodView> payMethodViewsAlipay = litePayviewConsultResponseBody.getPayMethodViews();

        // Local Object
        final List<PayMethodViewsBiz> payMethodViewBizList = new ArrayList<>();

        // filter savedCards based on their lastSuccessfull time(maximum 10
        // cards)
        filterSavedCardFromPlatform(litePayviewConsultResponseBody.getPayMethodViews());

        for (final PayMethodView payViewAlipay : payMethodViewsAlipay) {
            if (defaultLitePayView && PayMethod.EMI_DC.equals(payViewAlipay.getPayMethod())) {
                continue;
            }
            final PayMethodViewsBiz payMethodViewBiz = new PayMethodViewsBiz();
            payMethodViewBiz.setPayMethod(payViewAlipay.getPayMethod().getMethod());
            payMethodViewBiz.setRiskResult(mapRiskResult(payViewAlipay.getRiskResult()));
            payMethodViewBiz.setPayChannelOptionViews(mapPayChannelOptionViewForLitePayView(payViewAlipay
                    .getPayChannelOptionViews()));
            payMethodViewBiz.setPayCardOptionViews(getPayCardOptionView(payViewAlipay.getPayCardOptionViews()));
            payMethodViewBiz.setRemainingLimit(payViewAlipay.getRemainingLimit());
            payMethodViewBiz.setPayOptionRemainingLimits(payViewAlipay.getPayOptionRemainingLimits());
            payMethodViewBizList.add(payMethodViewBiz);
        }

        // For Wallet Only login is mandatory
        if ((payMethodViewsAlipay.size() == 1) && WALLET_TYPE.equals(payMethodViewBizList.get(0).getPayMethod())) {
            litePayviewConsultResponseBizBean.setWalletOnly(true);
        } else if (payMethodViewsAlipay.isEmpty()) {
            litePayviewConsultResponseBizBean.setLoginMandatory(true);
        }

        if (defaultLitePayView) {
            litePayviewConsultResponseBizBean.setPaymentsBankSupported(checkPpbNbIsEnabled(payMethodViewBizList));
            // removing ADVANCE_DEPOSIT_ACCOUNT from default flow
            removeAdvanceDepositForDefaultFlow(payMethodViewBizList);
        }
        litePayviewConsultResponseBizBean.setPayMethodViews(payMethodViewBizList);
    }

    private List<PayChannelOptionViewBiz> mapPayChannelOptionView(
            final List<PayChannelOptionView> payChannelOptionViews, String serviceAmount,
            String serviceAmountForEmiHybrid) {

        final List<PayChannelOptionViewBiz> payChanelOptionsBizList = new ArrayList<PayChannelOptionViewBiz>();
        StringBuilder optionsDisabled = new StringBuilder();
        for (final PayChannelOptionView payChannelOptionView : payChannelOptionViews) {
            final PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();

            payChannelOptionViewBiz.setDisableReason(payChannelOptionView.getDisableReason());
            payChannelOptionViewBiz.setEnableStatus(payChannelOptionView.isEnableStatus());
            payChannelOptionViewBiz.setInstId(payChannelOptionView.getInstId());
            payChannelOptionViewBiz.setInstName(payChannelOptionView.getInstName());
            payChannelOptionViewBiz.setPayOption(payChannelOptionView.getPayOption());
            payChannelOptionViewBiz.setSupportCountries(payChannelOptionView.getSupportCountries());
            payChannelOptionViewBiz.setDirectServiceInsts(payChannelOptionView.getDirectServiceInsts());
            payChannelOptionViewBiz.setSupportAtmPins(payChannelOptionView.getSupportAtmPins());
            if (payChannelOptionViewBiz.isEnableStatus()) {
                if (PayMethod.BALANCE == payChannelOptionView.getPayMethod()) {
                    payChannelOptionViewBiz.setBalanceChannelInfos(getBalanceChannelInfoList(payChannelOptionView
                            .getBalanceChannelInfos()));
                }

                if (PayMethod.PAYTM_DIGITAL_CREDIT == payChannelOptionView.getPayMethod()) {
                    payChannelOptionViewBiz.setExternalAccountInfos(getExternalAccountInfoList(payChannelOptionView
                            .getExternalAccountInfos()));
                }

                payChannelOptionViewBiz.setEmiChannelInfos(getEMIChannelInfoList(
                        payChannelOptionView.getEmiChannelInfos(), serviceAmount, payChannelOptionView.getInstId()));
                if (StringUtils.isNotBlank(serviceAmountForEmiHybrid)) {
                    payChannelOptionViewBiz.setEmiHybridChannelInfos(getEMIChannelInfoList(
                            payChannelOptionView.getEmiChannelInfos(), serviceAmountForEmiHybrid,
                            payChannelOptionView.getInstId()));
                }
            } else {
                optionsDisabled.append(payChannelOptionViewBiz.getPayOption()).append(", ");
            }

            payChannelOptionViewBiz.setExtendInfo(payChannelOptionView.getExtendInfo());
            payChanelOptionsBizList.add(payChannelOptionViewBiz);
        }
        if (optionsDisabled.length() != 0)
            EXT_LOGGER.customInfo("PayChannel Option disabled : {}", optionsDisabled.toString());
        return payChanelOptionsBizList;
    }

    private List<PayChannelOptionViewBiz> mapPayChannelOptionView(final List<PayChannelOptionView> payChannelOptionViews) {

        final List<PayChannelOptionViewBiz> payChanelOptionsBizList = new ArrayList<PayChannelOptionViewBiz>();
        StringBuilder optionsDisabled = new StringBuilder();
        for (final PayChannelOptionView payChannelOptionView : payChannelOptionViews) {
            final PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();

            payChannelOptionViewBiz.setDisableReason(payChannelOptionView.getDisableReason());
            payChannelOptionViewBiz.setEnableStatus(payChannelOptionView.isEnableStatus());
            payChannelOptionViewBiz.setInstId(payChannelOptionView.getInstId());
            payChannelOptionViewBiz.setInstName(payChannelOptionView.getInstName());
            payChannelOptionViewBiz.setPayOption(payChannelOptionView.getPayOption());
            payChannelOptionViewBiz.setSupportCountries(payChannelOptionView.getSupportCountries());
            payChannelOptionViewBiz.setDirectServiceInsts(payChannelOptionView.getDirectServiceInsts());
            payChannelOptionViewBiz.setSupportAtmPins(payChannelOptionView.getSupportAtmPins());

            if (payChannelOptionViewBiz.isEnableStatus()) {
                if (PayMethod.BALANCE == payChannelOptionView.getPayMethod()) {
                    payChannelOptionViewBiz.setBalanceChannelInfos(getBalanceChannelInfoList(payChannelOptionView
                            .getBalanceChannelInfos()));
                }

                if (payChannelOptionViewBiz.isEnableStatus()
                        && (PayMethod.PAYTM_DIGITAL_CREDIT == payChannelOptionView.getPayMethod())) {
                    payChannelOptionViewBiz.setExternalAccountInfos(getExternalAccountInfoList(payChannelOptionView
                            .getExternalAccountInfos()));
                }
                payChannelOptionViewBiz.setEmiChannelInfos(getEMIChannelInfoList(payChannelOptionView
                        .getEmiChannelInfos()));
                // TODO:To set ?
                payChannelOptionViewBiz.setEmiHybridChannelInfos(getEMIChannelInfoList(payChannelOptionView
                        .getEmiChannelInfos()));
            } else {
                optionsDisabled.append(payChannelOptionViewBiz.getPayOption()).append(", ");
            }

            payChannelOptionViewBiz.setExtendInfo(payChannelOptionView.getExtendInfo());
            payChanelOptionsBizList.add(payChannelOptionViewBiz);
        }
        if (optionsDisabled.length() != 0)
            EXT_LOGGER.customInfo("PayChannel Option disabled : {}", optionsDisabled.toString());
        return payChanelOptionsBizList;
    }

    private List<PayChannelOptionViewBiz> mapPayChannelOptionViewForLitePayView(
            final List<PayChannelOptionView> payChannelOptionViews) {
        final List<PayChannelOptionViewBiz> payChanelOptionsBizList = new ArrayList<PayChannelOptionViewBiz>();
        StringBuilder optionsDisabled = new StringBuilder();
        for (final PayChannelOptionView payChannelOptionView : payChannelOptionViews) {
            final PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();

            payChannelOptionViewBiz.setDisableReason(payChannelOptionView.getDisableReason());
            payChannelOptionViewBiz.setEnableStatus(payChannelOptionView.isEnableStatus());
            payChannelOptionViewBiz.setInstId(payChannelOptionView.getInstId());
            payChannelOptionViewBiz.setInstName(payChannelOptionView.getInstName());
            payChannelOptionViewBiz.setInstDispCode(payChannelOptionView.getInstDispCode());
            payChannelOptionViewBiz.setPayOption(payChannelOptionView.getPayOption());
            payChannelOptionViewBiz.setSupportCountries(payChannelOptionView.getSupportCountries());
            payChannelOptionViewBiz.setDirectServiceInsts(payChannelOptionView.getDirectServiceInsts());
            payChannelOptionViewBiz.setSupportAtmPins(payChannelOptionView.getSupportAtmPins());
            payChannelOptionViewBiz.setPrepaidCardChannel(payChannelOptionView.isPrepaidCardChannel());
            payChannelOptionViewBiz.setDccServiceInstIds(payChannelOptionView.getDccServiceInstIds());
            /* setOneClickSupported */
            payChannelOptionViewBiz.setOneClickChannel(payChannelOptionView.isOneClickChannel());
            payChannelOptionViewBiz
                    .setEmiChannelInfos(getEMIChannelInfoList(payChannelOptionView.getEmiChannelInfos()));
            payChannelOptionViewBiz.setEmiHybridChannelInfos(getEMIChannelInfoList(payChannelOptionView
                    .getEmiChannelInfos()));
            if (!payChannelOptionViewBiz.isEnableStatus()) {
                optionsDisabled.append(payChannelOptionViewBiz.getPayOption()).append(", ");
            }
            payChannelOptionViewBiz.setExtendInfo(payChannelOptionView.getExtendInfo());

            payChannelOptionViewBiz.setSupportPayOptionSubTypes(payChannelOptionView.getSupportPayOptionSubTypes());
            payChannelOptionViewBiz.setBlockPeriodInSeconds(payChannelOptionView.getBlockPeriodInSeconds());
            payChannelOptionViewBiz.setPayConfirmFlowType(payChannelOptionView.getPayConfirmFlowType());
            payChannelOptionViewBiz.setMaxBlockAmount(payChannelOptionView.getMaxBlockAmount());
            payChannelOptionViewBiz.setExcessCapturePercentage(payChannelOptionView.getExcessCapturePercentage());
            payChannelOptionViewBiz.setHasLowSuccessRate(payChannelOptionView.isHasLowSuccessRate());

            payChanelOptionsBizList.add(payChannelOptionViewBiz);
        }
        if (optionsDisabled.length() != 0)
            EXT_LOGGER.customInfo("PayChannel Option disabled : {}", optionsDisabled.toString());
        return payChanelOptionsBizList;
    }

    private List<BalanceChannelInfoBiz> getBalanceChannelInfoList(final List<BalanceChannelInfo> balanceChannelInfos) {

        if ((balanceChannelInfos != null) && !balanceChannelInfos.isEmpty()) {
            final List<BalanceChannelInfoBiz> balanceChannelInfoListBiz = new ArrayList<BalanceChannelInfoBiz>();
            for (final BalanceChannelInfo balanceChannelInfoAlipay : balanceChannelInfos) {
                final BalanceChannelInfoBiz balanceChannelInfoBiz = new BalanceChannelInfoBiz();

                String walletBalance = balanceChannelInfoAlipay.getAccountBalance().getAmount();
                Double walletAmount = Double.parseDouble(walletBalance);

                if (walletAmount < MIN_WALLET_BALANCE) {
                    balanceChannelInfoBiz.setAccountBalance("0");
                } else {
                    balanceChannelInfoBiz.setAccountBalance(walletBalance);
                }

                balanceChannelInfoBiz.setPayerAccountNo(balanceChannelInfoAlipay.getPayerAccountNo());
                balanceChannelInfoListBiz.add(balanceChannelInfoBiz);
            }
            return balanceChannelInfoListBiz;
        }
        return Collections.emptyList();
    }

    private List<ExternalAccountInfoBiz> getExternalAccountInfoList(final List<ExternalAccountInfo> externalAccountInfos) {

        if (!CollectionUtils.isEmpty(externalAccountInfos)) {
            final List<ExternalAccountInfoBiz> externalAccountInfoList = new ArrayList<ExternalAccountInfoBiz>();

            for (final ExternalAccountInfo externalAccountInfo : externalAccountInfos) {
                final ExternalAccountInfoBiz externalAccountInfoBiz = new ExternalAccountInfoBiz();
                externalAccountInfoBiz.setAccountBalance(externalAccountInfo.getAccountBalance().getAmount());
                externalAccountInfoBiz.setExternalAccountNo(externalAccountInfo.getExternalAccountNo());
                externalAccountInfoBiz.setExtendInfo(externalAccountInfo.getExtendInfo());

                externalAccountInfoList.add(externalAccountInfoBiz);
            }
            return externalAccountInfoList;
        }
        return Collections.emptyList();
    }

    private List<EMIChannelInfoBiz> getEMIChannelInfoList(final List<EMIChannelInfo> emiChannelInfos,
            String serviceAmount, String instId) {

        if ((emiChannelInfos != null) && !emiChannelInfos.isEmpty()) {
            List<EMIChannelInfoBiz> emiChannelInfoBizs = new ArrayList<>();

            for (final EMIChannelInfo emiChannelInfo : emiChannelInfos) {
                final EMIChannelInfoBiz emiChannelInfoBiz = new EMIChannelInfoBiz();

                if (isTransactionEligible(emiChannelInfo, serviceAmount)) {
                    emiChannelInfoBiz
                            .setCardAcquiringMode(emiChannelInfo.getCardAcquiringMode().getCardAcquiringMode());
                    emiChannelInfoBiz.setInterestRate(emiChannelInfo.getInterestRate());
                    emiChannelInfoBiz.setMaxAmount(emiChannelInfo.getMaxAmount().getAmount());
                    emiChannelInfoBiz.setMinAmount(emiChannelInfo.getMinAmount().getAmount());

                    // in PG2 FPO instead of ofMonths we will be getting tenure
                    // object
                    if (null != emiChannelInfo.getTenure() && null != emiChannelInfo.getTenure().getValue()) {
                        emiChannelInfoBiz.setOfMonths(emiChannelInfo.getTenure().getValue());
                    } else {
                        emiChannelInfoBiz.setOfMonths(emiChannelInfo.getOfMonths());
                    }
                    emiChannelInfoBiz.setPlanId(emiChannelInfo.getPlanId());
                    emiChannelInfoBiz.setTenureId(emiChannelInfo.getTenureId());

                    BigDecimal emi = calculateEmi(serviceAmount, emiChannelInfoBiz.getInterestRate(),
                            emiChannelInfoBiz.getOfMonths(), instId);
                    emiChannelInfoBiz.setPerInstallment(emi.toPlainString());
                    emiChannelInfoBizs.add(emiChannelInfoBiz);
                }
            }

            Collections.sort(emiChannelInfoBizs, new EmiMonthComparator());
            return emiChannelInfoBizs;
        }
        return Collections.emptyList();
    }

    private List<EMIChannelInfoBiz> getEMIChannelInfoList(final List<EMIChannelInfo> emiChannelInfos) {

        if ((emiChannelInfos != null) && !emiChannelInfos.isEmpty()) {
            List<EMIChannelInfoBiz> emiChannelInfoBizs = new ArrayList<>();

            for (final EMIChannelInfo emiChannelInfo : emiChannelInfos) {
                final EMIChannelInfoBiz emiChannelInfoBiz = new EMIChannelInfoBiz();

                emiChannelInfoBiz.setCardAcquiringMode(emiChannelInfo.getCardAcquiringMode().getCardAcquiringMode());
                emiChannelInfoBiz.setInterestRate(emiChannelInfo.getInterestRate());
                emiChannelInfoBiz.setMaxAmount(emiChannelInfo.getMaxAmount().getAmount());
                emiChannelInfoBiz.setMinAmount(emiChannelInfo.getMinAmount().getAmount());

                // in PG2 FPO instead of ofMonths we will be getting tenure
                // object
                if (null != emiChannelInfo.getTenure() && null != emiChannelInfo.getTenure().getValue()) {
                    emiChannelInfoBiz.setOfMonths(emiChannelInfo.getTenure().getValue());
                } else {
                    emiChannelInfoBiz.setOfMonths(emiChannelInfo.getOfMonths());
                }
                emiChannelInfoBiz.setPlanId(emiChannelInfo.getPlanId());
                emiChannelInfoBiz.setTenureId(emiChannelInfo.getTenureId());
                BigDecimal emi = new BigDecimal(0);
                emiChannelInfoBiz.setPerInstallment(emi.toPlainString());
                emiChannelInfoBizs.add(emiChannelInfoBiz);
            }

            Collections.sort(emiChannelInfoBizs, new EmiMonthComparator());
            return emiChannelInfoBizs;
        }
        return Collections.emptyList();
    }

    private boolean isTransactionEligible(EMIChannelInfo emiChannelInfo, String serviceAmount) {
        double txnAmount = Double.parseDouble(serviceAmount);
        double minAmountEMI = Double.parseDouble(emiChannelInfo.getMinAmount().getAmount());
        double maxAmountEMI = Double.parseDouble(emiChannelInfo.getMaxAmount().getAmount());

        if ((txnAmount >= minAmountEMI) && (txnAmount <= maxAmountEMI)) {
            return true;
        }

        LOGGER.debug("Emi Plan :: {} not eligible due to amount range", emiChannelInfo);

        return false;
    }

    private BigDecimal calculateEmi(String transactionAmount, String interestRate, String months, String instId) {
        LOGGER.debug("Transaction Amount : {} , Intereset Rate : {} , Months : {}", transactionAmount, interestRate,
                months);
        BigDecimal serviceAmount = new BigDecimal(transactionAmount);
        try {
            BigDecimal rate = new BigDecimal(interestRate).divide(BizConstant.BIG_DECIMAL_1200, 10,
                    RoundingMode.HALF_UP);
            if (rate.doubleValue() <= 0) {
                LOGGER.warn("EMI Rate configured is too low : {}", rate.toPlainString());
                return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
            }
            if (TheiaConstant.ExtraConstants.IDBI.equalsIgnoreCase(instId)) {
                EXT_LOGGER.customInfo("Calculating emi for IDBI bank with instId {}", instId);
                return calculateIdBIEmi(transactionAmount, interestRate, months);
            }
            BigDecimal noOfMonths = new BigDecimal(months);
            return serviceAmount
                    .multiply(rate)
                    .multiply(rate.add(BizConstant.BIG_DECIMAL_1).pow(noOfMonths.intValue()))
                    .divide((rate.add(BizConstant.BIG_DECIMAL_1).pow(noOfMonths.intValue()))
                            .subtract(BizConstant.BIG_DECIMAL_1),
                            2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            LOGGER.error("Returning EMI Amount as trans amount divided by number of months :{}", e.getMessage());
            return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        }
    }

    private List<PayCardOptionViewBiz> getPayCardOptionView(final List<PayCardOptionView> payCardOptionViews) {

        if ((payCardOptionViews != null) && !payCardOptionViews.isEmpty()) {

            final List<PayCardOptionViewBiz> payCardOptionViewBizs = new ArrayList<PayCardOptionViewBiz>();

            PayCardOptionViewBiz payCardOptionViewBiz;

            for (PayCardOptionView payCardOptionView : payCardOptionViews) {
                payCardOptionViewBiz = new PayCardOptionViewBiz();
                payCardOptionViewBiz.setCardHolderName(payCardOptionView.getCardHolderName());
                payCardOptionViewBiz.setCardIndexNo(payCardOptionView.getCardIndexNo());
                payCardOptionViewBiz.setCardNoLength(payCardOptionView.getCardNoLength());
                payCardOptionViewBiz.setCardScheme(payCardOptionView.getCardScheme());
                payCardOptionViewBiz.setDefaultCard(payCardOptionView.isDefaultCard());
                payCardOptionViewBiz.setDisableReason(payCardOptionView.getDisableReason());
                payCardOptionViewBiz.setEnableStatus(payCardOptionView.isEnableStatus());
                payCardOptionViewBiz.setFirstName(payCardOptionView.getFirstName());
                payCardOptionViewBiz.setInstId(payCardOptionView.getInstId());
                payCardOptionViewBiz.setInstName(payCardOptionView.getInstName());
                payCardOptionViewBiz.setLastName(payCardOptionView.getLastName());
                payCardOptionViewBiz.setMaskedCardNo(payCardOptionView.getMaskedCardNo());
                payCardOptionViewBiz.setPayerAccountNo(payCardOptionView.getPayerAccountNo());
                payCardOptionViewBiz.setPayOption(payCardOptionView.getPayOption());
                payCardOptionViewBiz.setExpiryMonth(payCardOptionView.getExpiryMonth());
                payCardOptionViewBiz.setExpiryYear(payCardOptionView.getExpiryYear());
                payCardOptionViewBiz.setPayMethod(payCardOptionView.getPayMethod().getMethod());
                payCardOptionViewBiz.setLastSuccessfulUsedTime(payCardOptionView.getLastSuccessfulUsedTime());
                payCardOptionViewBiz.setExtendInfo(payCardOptionView.getExtendInfo());
                payCardOptionViewBiz.setCardBin(payCardOptionView.getCardBin());
                payCardOptionViewBiz.setAssetSubTypes(payCardOptionView.getAssetSubTypes());
                payCardOptionViewBiz.setGcin(payCardOptionView.getGlobalCardIndex());
                payCardOptionViewBizs.add(payCardOptionViewBiz);
            }
            return payCardOptionViewBizs;
        }
        return Collections.emptyList();
    }

    private RiskResultBiz mapRiskResult(final RiskResult riskResult) {
        final RiskResultBiz riskResultBiz = new RiskResultBiz();
        if (riskResult == null)
            return riskResultBiz;

        riskResultBiz.setResult(riskResult.getResult().toString());
        if (!riskResultBiz.getResult().equals(BizConstant.RISK_RESULT_ACCEPT)) {
            riskResultBiz.setVerificationMethod(riskResult.getVerificationMethod().toString());
            riskResultBiz.setVerificationPriority(riskResult.getVerificationPriority());
        }

        return riskResultBiz;
    }

    private ConsultPayViewResponseBizBean mapExtendedInfo(ConsultPayViewResponseBizBean bizConsult,
            PayviewConsultResponseBody facadeBody) {
        Map<String, String> map = null;
        try {
            map = facadeBody.getExtendInfo();
        } catch (Exception e) {
            map = null;
            LOGGER.error("Exception occurred :", e);
        }

        bizConsult.setExtendInfo(map);
        return bizConsult;
    }

    private Map<String, String> extendInfoStrToMap(String extendInfo) {
        try {
            if (!StringUtils.isBlank(extendInfo)) {
                return JsonMapper.mapJsonToObject(extendInfo, Map.class);
            } else {
                return Collections.emptyMap();
            }
        } catch (final FacadeCheckedException e) {
            return Collections.emptyMap();
        }
    }

    private List<PayMethodViewsBiz> getPayMethodViewsBizList(List<PayMethodView> payMethodViewsAlipay,
            ConsultPayViewResponseBizBean consultPayViewresponseBean, PayviewConsultResponseBody consultPayViewBody,
            String serviceAmountForEmiHybrid) {
        final List<PayMethodViewsBiz> payMethodViewBizList = new ArrayList<PayMethodViewsBiz>();

        for (final PayMethodView payViewAlipay : payMethodViewsAlipay) {
            final PayMethodViewsBiz payMethodViewBiz = new PayMethodViewsBiz();
            payMethodViewBiz.setPayMethod(payViewAlipay.getPayMethod().getMethod());
            payMethodViewBiz.setRiskResult(mapRiskResult(payViewAlipay.getRiskResult()));

            payMethodViewBiz.setPayChannelOptionViews(mapPayChannelOptionView(payViewAlipay.getPayChannelOptionViews(),
                    consultPayViewBody.getTransAmount().getAmount(), serviceAmountForEmiHybrid));

            if (payViewAlipay.getPayMethod().getMethod().equals(PayMethod.BALANCE.getMethod())
                    && !payMethodViewBiz.getPayChannelOptionViews().get(0).isEnableStatus()) {
                consultPayViewresponseBean.setWalletFailed(true);
            }
            payMethodViewBiz.setPayCardOptionViews(getPayCardOptionView(payViewAlipay.getPayCardOptionViews()));// //
            // Create

            payMethodViewBizList.add(payMethodViewBiz);
        }

        return payMethodViewBizList;
    }

    private boolean checkPpbNbIsEnabled(List<PayMethodViewsBiz> payMethodViewBizList) {
        boolean paymentsBankSupported = false;
        for (PayMethodViewsBiz payMethodView : payMethodViewBizList) {
            if (!EPayMethod.NET_BANKING.getMethod().equals(payMethodView.getPayMethod())) {
                continue;
            }

            List<PayChannelOptionViewBiz> payChannelOptionViews = payMethodView.getPayChannelOptionViews();
            for (Iterator<PayChannelOptionViewBiz> iterator = payChannelOptionViews.iterator(); iterator.hasNext();) {
                PayChannelOptionViewBiz payChannelOptionView = iterator.next();
                if (StringUtils.isNotBlank(payChannelOptionView.getInstId())
                        && payChannelOptionView.getInstId().equals("PPBL")) {
                    if (payChannelOptionView.isEnableStatus()) {
                        LOGGER.info("Paytm Payments Bank is configured as an acquiring channel on the merchant");
                        paymentsBankSupported = true;
                    }
                    iterator.remove();
                    continue;
                }
                if (StringUtils.isNotBlank(payChannelOptionView.getInstId())
                        && ("ZEST").equals(payChannelOptionView.getInstId())) {
                    LOGGER.info("Removing Zest from Net Banking List");
                    iterator.remove();
                }
            }
        }
        return paymentsBankSupported;
    }

    private void removeAdvanceDepositForDefaultFlow(List<PayMethodViewsBiz> payMethodViewBizList) {
        Iterator<PayMethodViewsBiz> payMethodIterator = payMethodViewBizList.iterator();

        while (payMethodIterator.hasNext()) {
            PayMethodViewsBiz payMethodView = payMethodIterator.next();
            if (EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod().equals(payMethodView.getPayMethod())) {
                LOGGER.info("Removing Advance Deposit from default flow");
                payMethodIterator.remove();
            }
        }
    }

    public GenericCoreResponseBean<ChannelAccountQueryRequest> channelAccountQueryRequest(
            ChannelAccountQueryRequestBizBean channelAccountQueryRequest) {
        final String payerUserId = channelAccountQueryRequest.getUserId();
        final EnvInfoRequestBean envInfoReqBean = channelAccountQueryRequest.getEnvInfoRequestBean();
        final String productCode = ERequestType.getByRequestType(channelAccountQueryRequest.getRequestType().getType())
                .getProductCode();
        String merchantId = channelAccountQueryRequest.getMerchantId();
        List<com.paytm.pgplus.biz.core.model.request.PayMethodInfo> payMethodInfos = channelAccountQueryRequest
                .getPayMethodInfos();
        try {
            final EnvInfo envInfo = AlipayRequestUtils.createEnvInfo(envInfoReqBean);
            final String productCodeId = ProductCodes.getProductByProductCode(productCode).getId();
            final ChannelAccountQueryBody.ChannelAccountQueryBuilder body = new ChannelAccountQueryBody.ChannelAccountQueryBuilder(
                    productCodeId, envInfo).merchantId(merchantId).payerUserId(payerUserId)
                    .payMethodInfos(getPaymethodInfoList(payMethodInfos));

            final ChannelAccountQueryRequest finalChannelAccRequest = new ChannelAccountQueryRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.CHANNEL_ACCOUNT_QUERY), body.build());
            return new GenericCoreResponseBean<>(finalChannelAccRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred ", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }

    private List<PayMethodInfo> getPaymethodInfoList(
            List<com.paytm.pgplus.biz.core.model.request.PayMethodInfo> payMethodInfos) throws FacadeCheckedException {
        List<PayMethodInfo> payMethodInfoList = new ArrayList<>();
        for (com.paytm.pgplus.biz.core.model.request.PayMethodInfo payMethodInfo : payMethodInfos) {
            PayMethodInfo payMethodData = new PayMethodInfo(payMethodInfo.getPayMethod(),
                    extendInfoStr(payMethodInfo.getExtendInfo()));
            payMethodInfoList.add(payMethodData);
        }
        return payMethodInfoList;
    }

    public GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> mapChannelAccountQueryResponse(
            ChannelAccountQueryResponse channelAccountQueryResponse) {
        if ((channelAccountQueryResponse == null) || (channelAccountQueryResponse.getBody() == null)
                || (channelAccountQueryResponse.getBody().getResultInfo() == null)) {
            return new GenericCoreResponseBean<>(BizConstant.FETCH_CHANNEL_ACCOUNT_QUERY_ERROR_MESSAGE,
                    ResponseConstants.SYSTEM_ERROR);
        }
        if (!BizConstant.SUCCESS.equals(channelAccountQueryResponse.getBody().getResultInfo().getResultCode())) {
            ResponseConstants responseConstants = ErrorCodeConstants.getAlipayResponseImmutableMap().get(
                    channelAccountQueryResponse.getBody().getResultInfo().getResultCodeId());
            return new GenericCoreResponseBean<>(BizConstant.FETCH_CHANNEL_ACCOUNT_QUERY_ERROR_MESSAGE,
                    responseConstants);
        }
        if (!channelAccountQueryResponse.getBody().getChannelAccountViews().get(0).isEnableStatus()
                && PayMethod.LOYALTY_POINT.getMethod().equals(
                        channelAccountQueryResponse.getBody().getChannelAccountViews().get(0).getPayMethod()
                                .getMethod())) {
            return new GenericCoreResponseBean<>(BizConstant.LOYALTY_POINT_NOT_ENABLED, ResponseConstants.SYSTEM_ERROR);
        }
        // maps facade object to biz object
        return mapChannelAccountViewInfo(channelAccountQueryResponse);
    }

    private GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> mapChannelAccountViewInfo(
            ChannelAccountQueryResponse channelAccountQueryResponse) {
        ChannelAccountQueryResponseBizBean channelAccountQueryResponseBizBean = new ChannelAccountQueryResponseBizBean();
        List<ChannelAccountView> channelAccountViewBizList = new ArrayList<>();
        List<com.paytm.pgplus.facade.payment.models.ChannelAccountView> channelAccountViews = channelAccountQueryResponse
                .getBody().getChannelAccountViews();
        if (!CollectionUtils.isEmpty(channelAccountViews)) {
            for (com.paytm.pgplus.facade.payment.models.ChannelAccountView channelAccountView : channelAccountViews) {
                ChannelAccountView channelAccountViewBiz = new ChannelAccountView();
                channelAccountViewBiz.setPayMethod(channelAccountView.getPayMethod().getMethod());
                channelAccountViewBiz.setDisableReason(channelAccountView.getDisableReason());
                channelAccountViewBiz.setEnableStatus(channelAccountView.isEnableStatus());
                channelAccountViewBiz.setChannelAccounts(mapChannelAccount(channelAccountView.getChannelAccounts(),
                        channelAccountView.getPayMethod()));
                channelAccountViewBiz.setNoInternalAssetAvailable(channelAccountView.getNoInternalAssetAvailable());
                channelAccountViewBizList.add(channelAccountViewBiz);
            }
            channelAccountQueryResponseBizBean.setChannelAccountViews(channelAccountViewBizList);
        }
        return new GenericCoreResponseBean<>(channelAccountQueryResponseBizBean);
    }

    private List<ChannelAccount> mapChannelAccount(
            List<com.paytm.pgplus.facade.payment.models.ChannelAccount> channelAccounts, PayMethod payMethod) {
        List<ChannelAccount> channelAccountBizList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(channelAccounts)) {
            for (com.paytm.pgplus.facade.payment.models.ChannelAccount channelAccount : channelAccounts) {
                ChannelAccount channelAccountBiz = new ChannelAccount();
                channelAccountBiz.setAvailableBalance(channelAccount.getAvailableBalance().getAmount());
                if (PayMethod.BALANCE.equals(payMethod)) {
                    setBalanceForWallet(channelAccountBiz);
                }
                channelAccountBiz.setAccountNo(channelAccount.getAccountNo());
                channelAccountBiz.setDisableReason(channelAccount.getDisableReason());
                channelAccountBiz.setStatus(channelAccount.isStatus());
                channelAccountBiz.setExtendInfo(channelAccount.getExtendInfo());
                if (PayMethod.LOYALTY_POINT.equals(payMethod)) {
                    channelAccountBiz.setAccountBalance(channelAccount.getAccountBalance().getAmount());
                    channelAccountBiz.setAvailablePoints(channelAccount.getAvailablePoints());
                    channelAccountBiz.setExchangeRate(channelAccount.getExchangeRate());
                }
                if (PayMethod.GIFT_VOUCHER.getMethod().equals(payMethod.getMethod())) {
                    channelAccountBiz.setTemplateId(channelAccount.getTemplateId());
                }
                channelAccountBizList.add(channelAccountBiz);
            }
        }
        return channelAccountBizList;
    }

    /**
     * This is the function to calulate emi for IDBI bank
     *
     * @param transactionAmount
     * @param interestRate
     * @param months
     * @return
     */
    public static BigDecimal calculateIdBIEmi(String transactionAmount, String interestRate, String months) {
        BigDecimal rateInterest = new BigDecimal(interestRate);
        BigDecimal principalAmount = new BigDecimal(transactionAmount);
        BigDecimal monthsTenure = new BigDecimal(months);
        BigDecimal rateInterestByMonth = rateInterest.divide(new BigDecimal(12), 3, RoundingMode.HALF_UP).divide(
                new BigDecimal(100));

        BigDecimal emiPart1 = principalAmount.divide(monthsTenure, 3, RoundingMode.HALF_UP);
        BigDecimal emiPart2 = (principalAmount.multiply(monthsTenure).multiply(rateInterestByMonth)).divide(
                monthsTenure, 3, RoundingMode.HALF_UP);

        return emiPart1.add(emiPart2).setScale(0, RoundingMode.HALF_UP);
    }

    private void setBalanceForWallet(ChannelAccount channelAccountBiz) {
        String walletBalance = channelAccountBiz.getAvailableBalance();
        Double walletAmount = Double.parseDouble(walletBalance);
        if (walletAmount < MIN_WALLET_BALANCE) {
            channelAccountBiz.setAvailableBalance("0");
        }
    }

    private void filterSavedCardFromPlatform(List<PayMethodView> payMethodViewsBizs) {
        if (CollectionUtils.isEmpty(payMethodViewsBizs)) {
            return;
        }
        List<PayMethodView> payMethodViewsCreditCardList = payMethodViewsBizs.stream()
                .filter(payMethods -> EPayMethod.CREDIT_CARD.getMethod().equals(payMethods.getPayMethod().getMethod()))
                .collect(Collectors.toList());
        List<PayMethodView> payMethodViewsDebitCardList = payMethodViewsBizs.stream()
                .filter(payMethods -> EPayMethod.DEBIT_CARD.getMethod().equals(payMethods.getPayMethod().getMethod()))
                .collect(Collectors.toList());
        if (cardsMoreThanLimit(payMethodViewsCreditCardList, payMethodViewsDebitCardList)) {
            sortCardsOnLastSuccessfullUsedTime(payMethodViewsCreditCardList, payMethodViewsDebitCardList);

        }
    }

    private boolean cardsMoreThanLimit(List<PayMethodView> creditCards, List<PayMethodView> debitCards) {
        int totalCards = 0;
        if (!CollectionUtils.isEmpty(creditCards)
                && !CollectionUtils.isEmpty(creditCards.get(0).getPayCardOptionViews())) {
            totalCards = creditCards.get(0).getPayCardOptionViews().size();
        }
        if (!CollectionUtils.isEmpty(debitCards) && !CollectionUtils.isEmpty(debitCards.get(0).getPayCardOptionViews())) {
            totalCards += debitCards.get(0).getPayCardOptionViews().size();
        }
        int globalCardLimit = Integer.valueOf(ConfigurationUtil
                .getProperty(BizConstant.GLOBAL_SAVED_ASSETS_LIMIT, "10"));
        if (totalCards > globalCardLimit) {
            return true;
        }
        return false;

    }

    private void sortCardsOnLastSuccessfullUsedTime(List<PayMethodView> creditCardList,
            List<PayMethodView> debitCardList) {
        List<PayCardOptionView> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(creditCardList)
                && !CollectionUtils.isEmpty(creditCardList.get(0).getPayCardOptionViews())) {
            list.addAll(creditCardList.get(0).getPayCardOptionViews());
        }
        if (!CollectionUtils.isEmpty(debitCardList)
                && !CollectionUtils.isEmpty(debitCardList.get(0).getPayCardOptionViews())) {
            list.addAll(debitCardList.get(0).getPayCardOptionViews());
        }
        Collections.sort(list, new Comparator<PayCardOptionView>() {
            @Override
            public int compare(PayCardOptionView o1, PayCardOptionView o2) {
                return o2.getLastSuccessfulUsedTime().compareTo(o1.getLastSuccessfulUsedTime());
            }
        });
        list = list.subList(0, 10);
        // set creditCardInCreditCardPayMethods ,same for debitCard
        if (!CollectionUtils.isEmpty(creditCardList)) {
            creditCardList.get(0).setPayCardOptionViews(
                    list.stream()
                            .filter(pc -> EPayMethod.CREDIT_CARD.getMethod().equals(pc.getPayMethod().getMethod()))
                            .collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(debitCardList)) {
            debitCardList.get(0).setPayCardOptionViews(
                    list.stream().filter(pc -> EPayMethod.DEBIT_CARD.getMethod().equals(pc.getPayMethod().getMethod()))
                            .collect(Collectors.toList()));
        }

    }
}

class EmiMonthComparator implements Comparator<EMIChannelInfoBiz> {

    @Override
    public int compare(EMIChannelInfoBiz o1, EMIChannelInfoBiz o2) {
        Integer month1 = Integer.valueOf(o1.getOfMonths());
        Integer month2 = Integer.valueOf(o2.getOfMonths());

        // ascending order
        return month1.compareTo(month2);
    }
}