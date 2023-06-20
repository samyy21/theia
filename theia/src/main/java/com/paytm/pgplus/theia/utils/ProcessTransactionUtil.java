/*
 * @Dev - Amit Dubey
 * @Date - 10/02/18
 */

package com.paytm.pgplus.theia.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.ObjectMapperUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.cache.enums.ChannelType;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.enums.PaymentMode;
import com.paytm.pgplus.checksum.crypto.nativ.NativePaymentDetailsCryptoFactory;
import com.paytm.pgplus.checksum.crypto.nativ.enums.NativeCryptoType;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.EcomTokenInfo;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.model.OneClickInfo;
import com.paytm.pgplus.common.model.nativ.NativeConsultDetails;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.enums.PreferredOtpPage;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.risk.enums.RiskResultEnum;
import com.paytm.pgplus.facade.risk.models.response.RiskResponse;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.VpaDetailV4;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseRequest;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoRequestData;
import com.paytm.pgplus.facade.wallet.services.IWalletQRCodeDetailsService;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.merchant.status.utils.ConstantsUtil;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.*;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.InitiateTransactionResponseBody;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.datamapper.helper.BizRequestResponseMapperHelper;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.exceptions.RiskRejectException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.models.NativeJsonRequest;
import com.paytm.pgplus.theia.models.UserAgentInfo;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.enums.PayModeType;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPageWalletInfo;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.enhancenative.AppInvokeRedirectionUrlData;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequest;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.PaymentFlow;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.DuplicatePaymentRequestException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.enums.AddMoneySourceEnum.*;
import static com.paytm.pgplus.biz.utils.BizConstant.COLLECT_API_INVOKE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.common.enums.CardTypeEnum.DINERS;
import static com.paytm.pgplus.payloadvault.merchant.status.utils.ConstantsUtil.PARAMETERS.REQUEST.EXTENDINFO;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.PAYER_CMID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.TRUE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.INITIATE_TRANS_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ASSAM_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MANIPUR_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.CUSTOMER_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ACCOUNT_NUMBER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ADDITIONAL_INFO;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.AGG_MID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ALLOW_UNVERIFIED_ACCOUNT;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.APP_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.BANK_NAME;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CHANNEL_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CREDIT_BLOCK;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.DEVICE_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.EMI_SUBVENTION_INFO;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.EMI_TYPE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.GUEST_TOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.LINK_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.LINK_NAME;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MPIN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.IS_RISK_VERIFIED_ENHANCE_FLOW;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ORDER_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ORIGIN_CHANNEL;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.REQUEST_TYPE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.TXN_TOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.UPI_ACC_REF_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.VALIDATE_ACCOUNT_NUMBER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.PAYMENT_FLOW_EXPECTED;
import static com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.INVALID_PAYMENTMODE;
import static com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.RISK_REJECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.VALIDATE_EMI_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.VALIDATE_EMI_RESPONSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ErrorMsgs.CC_ON_UPI_MSG;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay.PRODUCT_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.WALLET_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.MAX_DEFICIT_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.ONE_CLICK_MAX_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RedisKeysConstant.NativeSession.NATIVE_TXN_INITIATE_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.EMI_SUBVENTION_CUSTOMER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CARD_PRE_AUTH_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.NATIVE_JSON_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.PRE_AUTH_BLOCK_SECONDS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.WALLET_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SUBSCRIPTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_NOT_ALLOWD_ON_MERCHANT;

@Service("processTransactionUtil")
public class ProcessTransactionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ProcessTransactionUtil.class);

    private static final String MAESTRO_CVV = "|123";
    private static final String BAJAJFN_CVV = "|1111";
    private static final String MAESTRO_EXPIRY = "|122049";
    private static final String MAESTRO = "MAESTRO";
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final int ETM_ENCRYPTION_CONSTANT = 7;

    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    private static final String GV_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.GV_PRODUCT_CODE);
    private static final String NCMC_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.NCMC_PRODUCT_CODE);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    @Qualifier("theiaSessionDataServiceAdapterNative")
    private ITheiaSessionDataService theiaSessionDataServiceAdapterNative;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("payviewConsultServiceHelper")
    private PayviewConsultServiceHelper payviewConsultServiceHelper;

    @Autowired
    private NativePaymentDetailsCryptoFactory nativePaymentDetailsCryptoFactory;

    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardService;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private EcomTokenUtils ecomTokenUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    BinHelper binHelper;

    @Autowired
    CoftUtils coftUtils;

    @Autowired
    NativePayviewConsultServiceHelper nativePayviewConsultServiceHelper;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    @Qualifier("cardTransactionUtil")
    private CardTransactionUtil cardTransactionUtil;

    @Autowired
    @Qualifier("walletQRCodeDetailsServiceImpl")
    private IWalletQRCodeDetailsService walletQrCodeDetailsService;

    @Autowired
    @Qualifier("bizRequestResponseMapperHelper")
    private BizRequestResponseMapperHelper bizRequestResponseMapperHelper;

    private static final String oldPGHost = com.paytm.pgplus.theia.utils.ConfigurationUtil
            .getProperty(TheiaConstant.ExtraConstants.OLD_PG_BASE_URL_SKIP);

    private String getVPAForAccRefId(NativeCashierInfoResponse cashierInfoResponse, String upiAccRefId) {
        List<VpaDetailV4> vpaDetailsList = cashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4()
                .getRespDetails().getProfileDetail().getVpaDetails();
        for (VpaDetailV4 vpaDetailV4 : vpaDetailsList) {
            if (vpaDetailV4.getDefaultDebitAccRefId().equals(upiAccRefId)) {
                return vpaDetailV4.getName();
            }
        }
        return null;
    }

    public Map<String, String[]> getAdditionalParamMap(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        Map<String, String[]> additionalParams = new HashMap<>();

        boolean isEnhancedNativeFlow = isEnhancedNativeFlow(request);
        boolean isNativeJsonRequest = isNativeJsonRequest(request);

        if (isEnhancedNativeFlow) {
            additionalParams.put(ENHANCED_CASHIER_PAYMENT_REQUEST, new String[] { Boolean.TRUE.toString() });
        }

        pushV1PtcRequestEvent(request);

        boolean sendHTML = true;
        /*
         * check if request is NativeJsonRequest or enhancedNativeFlow,
         * returnType should be json
         */
        if (isNativeJsonRequest || isEnhancedNativeFlow) {
            sendHTML = false;
        }

        String orderId = request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);

        String txnToken = request.getParameter(Native.TXN_TOKEN);
        if (StringUtils.isBlank(txnToken) && StringUtils.isNotBlank(request.getParameter(Native.GUEST_TOKEN))) {
            txnToken = request.getParameter(Native.TOKEN);
        }

        String mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        additionalParams.put(TheiaConstant.RequestParams.Native.TXN_TOKEN, new String[] { txnToken });

        additionalParams.put(DCC_SELECTED_BY_USER, new String[] { request.getParameter(DCC_SELECTED_BY_USER) });
        additionalParams.put(PAYMENT_CALL_DCC, new String[] { request.getParameter(PAYMENT_CALL_DCC) });

        request.setAttribute(Native.TXN_TOKEN, txnToken);
        String oneClickInfo = request.getParameter(ONECLICKINFO);
        if (oneClickInfo != null) {
            additionalParams.put(Native.ONE_CLICK_INFO, new String[] { oneClickInfo });
        }
        String coftConsentInfo = request.getParameter(COFT_CONSENT);
        if (coftConsentInfo != null) {
            CoftConsent coftConsent = JsonMapper.mapJsonToObject(coftConsentInfo, CoftConsent.class);
            if (coftConsent.getUserConsent() == null
                    || (coftConsent.getUserConsent() != 1 && coftConsent.getUserConsent() != 0)) {
                LOGGER.error("Invalid coftConsentInfo : {}", coftConsentInfo);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(), null, true);

                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            additionalParams.put(COFT_CONSENT, new String[] { coftConsentInfo });
        }
        String ecomTokenInfo = request.getParameter(ECOMTOKENINFO);
        String cardTokenInfo = request.getParameter(CARDTOKENINFO);

        if (StringUtils.isNotBlank(txnToken)) {
            String isCollectAppInvoke = (String) nativeSessionUtil.getField(txnToken, COLLECT_API_INVOKE);
            if (StringUtils.isNotBlank(isCollectAppInvoke) && StringUtils.equalsIgnoreCase(isCollectAppInvoke, "true")) {
                String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
                paymentMode = StringUtils.isNotBlank(paymentMode) ? paymentMode : "";
                if (isEnhancedNativeFlow) {
                    pushNativePaymentEvent(mid, orderId,
                            "M-WEB Payment by notification App Invoke flow using ".concat(paymentMode));
                } else {
                    pushNativePaymentEvent(mid, orderId,
                            "APP Payment by notification App Invoke flow using ".concat(paymentMode));
                }
            }
        }

        NativeInitiateRequest nativeInitiateRequest = null;
        try {
            if (TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE))) {
                nativeInitiateRequest = createNativeInitiateRequestForSsoFLow(request);
                additionalParams.put(Native.RISK_EXTENDED_INFO,
                        new String[] { request.getParameter(Native.RISK_EXTENDED_INFO) });

            } else {

                nativeInitiateRequest = nativeSessionUtil.validate(txnToken);

            }
        } catch (SessionExpiredException see) {
            if (isNativeJsonRequest || isEnhancedNativeFlow) {
                failureLogUtil.setFailureMsgForDwhPush(ResultCode.SESSION_EXPIRED_EXCEPTION.getResultCodeId(),
                        ResultCode.SESSION_EXPIRED_EXCEPTION.getResultMsg(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.SESSION_EXPIRED_EXCEPTION).isHTMLResponse(
                        sendHTML).build();
            } else {
                failureLogUtil.setFailureMsgForDwhPush(null, see.getMessage(), null, true);
                throw see;
            }
        }
        if (StringUtils.isNotBlank(request.getParameter(Native.RISK_EXTENDED_INFO))) {

            LOGGER.debug("DATA_ENRICHEMENT : Setting risk_extended_info in additionalParams : {} ",
                    request.getParameter(Native.RISK_EXTENDED_INFO));
            additionalParams.put(Native.RISK_EXTENDED_INFO,
                    new String[] { request.getParameter(Native.RISK_EXTENDED_INFO) });
        }

        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        addOrderDetails(orderDetail, additionalParams);

        if (orderDetail != null && orderDetail.isAoaSubsOnPgMid()) {
            additionalParams.put(AOA_SUBS_ON_PG_MID, new String[] { Boolean.TRUE.toString() });
        }

        if (orderDetail.isAutoRefund()) {
            additionalParams.put(AUTO_REFUND, new String[] { Boolean.TRUE.toString() });
            additionalParams.put(BLOCK_NON_CC_DC_PAYMODES, new String[] { Boolean.TRUE.toString() });
        }

        validateOneClickPayment(request, orderDetail);
        addFlowTypeOnTxnToken(request, txnToken);

        if (orderDetail != null && orderDetail.isOfflineFlow()) {
            LOGGER.info("Offline flow for trans :{}", orderDetail.isOfflineFlow());
            String extendInfo = request.getParameter(Native.OFFLINE_EXTEND_INFO);
            if (extendInfo != null) {
                orderDetail.setExtendInfo(getExtendInfo(request, extendInfo));
                orderDetail.getExtendInfo().setComments(
                        JsonMapper.getStringParamFromJson(extendInfo, TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO));
                nativeSessionUtil.setOrderDetail(txnToken, orderDetail);
            } else {
                LOGGER.info("extend info is null for offline mlv flow");
            }
        }

        Map<String, Object> extraParamsMap = (Map<String, Object>) theiaTransactionalRedisUtil.get(mid + "#" + orderId);
        if (extraParamsMap != null
                && (extraParamsMap.get("headAccount") != null || extraParamsMap.get("remitterName") != null)
                && !isAllowedWrapper(extraParamsMap, MANIPUR_WRAPPER)
                && !isAllowedWrapper(extraParamsMap, ASSAM_WRAPPER)) {
            additionalParams.put("challanIdNum", new String[] { workFlowHelper.generateCIN(BSR_CODE) });
        }

        // setting sdkType in extendInfo
        populateSdkTypeInExtendInfo(request, orderDetail, txnToken);

        if ((StringUtils.isNotBlank(request.getParameter(ACCESS_TOKEN)) || StringUtils.isNotBlank(request
                .getParameter(GUEST_TOKEN))) && StringUtils.isBlank(orderDetail.getPaytmSsoToken())) {
            // set sso token saved against guestToken in orderdetail
            String token = StringUtils.isNotBlank(request.getParameter(ACCESS_TOKEN)) ? request
                    .getParameter(ACCESS_TOKEN) : request.getParameter(GUEST_TOKEN);
            String ssoToken = nativeSessionUtil.getSsoToken(token);
            orderDetail.setPaytmSsoToken(ssoToken);

            if (StringUtils.isBlank(ssoToken)
                    && StringUtils.isNotBlank(request.getParameter(ACCESS_TOKEN))
                    && EPayMethod.EMI.getMethod().equalsIgnoreCase(
                            request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE))) {
                CreateAccessTokenServiceRequest accessTokenData = accessTokenUtils.getAccessTokenDetail(token);
                if (accessTokenData != null && accessTokenData.getUserInfo() != null
                        && StringUtils.isNotBlank(accessTokenData.getUserInfo().getMobile())) {
                    UserInfo userInfo = orderDetail.getUserInfo();
                    if (userInfo == null) {
                        userInfo = new UserInfo();
                        orderDetail.setUserInfo(userInfo);
                    }
                    userInfo.setMobile(accessTokenData.getUserInfo().getMobile());
                }
            }
            // update order detail in cache
            nativeSessionUtil.setOrderDetail(txnToken, orderDetail);

        }
        additionalParams.put(Native.GUEST_TOKEN, new String[] { request.getParameter(Native.GUEST_TOKEN) });
        additionalParams.put(Native.WORKFLOW, new String[] { request.getParameter(Native.WORKFLOW) });

        request.setAttribute("orderDetail", orderDetail);
        additionalParams.put(PEON_URL, new String[] { orderDetail.getPEON_URL() });
        additionalParams.put(AGG_MID, new String[] { orderDetail.getAggMid() });

        if (orderDetail.getEmiSubventionValidationResponse() != null) {
            String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
            if (!EPayMethod.EMI.getMethod().equals(paymentMode)) {
                LOGGER.error(BizConstant.FailureLogs.PAYMENT_MODE_IS_INVALID_FOR_EMI_SUBVENTION);
                if (isNativeJsonRequest(request)) {
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.PAYMENT_MODE_IS_INVALID_FOR_EMI_SUBVENTION, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }
                failureLogUtil.setFailureMsgForDwhPush(RequestValidationException.getException().getResultCode()
                        .getResultCodeId(), RequestValidationException.getException().getMessage(), null, true);
                throw RequestValidationException.getException();
            }
            ValidateRequest subventionvalidateRequest = (ValidateRequest) orderDetail
                    .getEmiSubventionValidationResponse().get(VALIDATE_EMI_REQUEST);
            ValidateResponse subventionValidateResponse = (ValidateResponse) orderDetail
                    .getEmiSubventionValidationResponse().get(VALIDATE_EMI_RESPONSE);
            String subventionCustomerId = (String) orderDetail.getEmiSubventionValidationResponse().get(
                    EMI_SUBVENTION_CUSTOMER_ID);
            paymentOptionCardIndexNoPopulated(request, subventionvalidateRequest);
            request.setAttribute(VALIDATE_EMI_REQUEST, subventionvalidateRequest);
            request.setAttribute(VALIDATE_EMI_RESPONSE, subventionValidateResponse);
            request.setAttribute(EMI_SUBVENTION_CUSTOMER_ID, subventionCustomerId);
        }

        // EMI Subvention All in one SDK Flow payment call
        if (EPayMethod.EMI.getMethod().equals(request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE))
                && null != request.getParameter(EMI_SUBVENTION_INFO)) {
            EmiSubventionInfo emiSubventionInfo = JsonMapper.mapJsonToObject(request.getParameter(EMI_SUBVENTION_INFO),
                    EmiSubventionInfo.class);

            if (StringUtils.isBlank(emiSubventionInfo.getSubventionPlanId())
                    || emiSubventionInfo.getFinalTransactionAmount() == null
                    || StringUtils.isBlank(emiSubventionInfo.getFinalTransactionAmount().getValue())
                    || CollectionUtils.isEmpty(emiSubventionInfo.getItemOfferDetails())) {
                LOGGER.error(BizConstant.FailureLogs.EMI_SUBVENTION_INFO_IS_INVALID);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        BizConstant.FailureLogs.EMI_SUBVENTION_INFO_IS_INVALID, null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            String jsonEmiSubventionInfo = JsonMapper.mapObjectToJson(emiSubventionInfo);
            additionalParams.put(EMI_SUBVENTION_INFO, new String[] { jsonEmiSubventionInfo });

        }

        if ((orderDetail.getSimplifiedPaymentOffers() != null && orderDetail.getPaymentOffersApplied() != null)
                || (orderDetail.getSimplifiedPaymentOffers() != null && orderDetail.getPaymentOffersAppliedV2() != null)
                || (orderDetail.getPaymentOffersApplied() != null && orderDetail.getPaymentOffersAppliedV2() != null)) {
            if (isNativeJsonRequest(request)) {
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(false)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }

            failureLogUtil.setFailureMsgForDwhPush(RequestValidationException.getException().getResultCode()
                    .getResultCodeId(), RequestValidationException.getException().getMessage(), null, true);
            throw RequestValidationException.getException();
        }

        additionalParams.put(AGG_TYPE, new String[] { orderDetail.getAggType() });
        if (orderDetail.getOrderPricingInfo() != null) {
            additionalParams.put(ORDER_PRICING_INFO,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getOrderPricingInfo()) });
        }
        additionalParams.put(OFFLINE_TXN_FLOW, new String[] { String.valueOf(orderDetail.isOfflineFlow()) });
        if (orderDetail.getSimplifiedPaymentOffers() != null) {
            additionalParams.put(SIMPLIFIED_PAYMENT_OFFERS,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getSimplifiedPaymentOffers()) });
        }

        if (orderDetail.getPaymentOffersApplied() != null) {
            additionalParams.put(PROMO_PAYMENT_OFFERS,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getPaymentOffersApplied()) });
        }
        LOGGER.debug("token data from cache {}: ", orderDetail);

        if (orderDetail.getPaymentOffersAppliedV2() != null) {
            additionalParams.put(PROMO_PAYMENT_OFFERS_V2,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getPaymentOffersAppliedV2()) });
        }

        if (!StringUtils.equals(mid, orderDetail.getMid())) {
            LOGGER.error("mid in token:{} and request:{} are different", orderDetail.getMid(), mid);
            throw RequestValidationException.getException();
        }
        if (!StringUtils.equals(orderId, orderDetail.getOrderId())) {
            LOGGER.error("orderid in token:{} and request:{} are different", orderDetail.getOrderId(), orderId);

            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(sendHTML).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }
        if (ERequestType.NATIVE_MF.getType().equals(orderDetail.getRequestType())
                || (orderDetail.getLinkDetailsData() != null && ERequestType.NATIVE_MF.getType().equals(
                        orderDetail.getLinkDetailsData().getSubRequestType())))
            EventUtils.pushTheiaEvents(EventNameEnum.PAYMENT_FOR_NATIVE_MF, new ImmutablePair<>("REQUEST_TYPE",
                    ERequestType.NATIVE_MF.getType()));

        String aggMid = request.getParameter(TheiaConstant.RequestParams.Native.AGG_MID);
        if (StringUtils.isNotBlank(aggMid) && !StringUtils.equals(orderDetail.getAggMid(), aggMid)) {
            LOGGER.error(BizConstant.FailureLogs.AGG_MID_IN_TOKEN_AND_REQUEST_ARE_DIFFERENT);
            failureLogUtil.setFailureMsgForDwhPush(RequestValidationException.getException().getResultCode()
                    .getResultCodeId(), BizConstant.FailureLogs.AGG_MID_IN_TOKEN_AND_REQUEST_ARE_DIFFERENT, null, true);
            throw RequestValidationException.getException();
        }

        if (orderDetail.getExtendInfo() != null && orderDetail.getExtendInfo().getSubsLinkInfo() != null
                && StringUtils.isNotBlank(orderDetail.getExtendInfo().getSubsLinkInfo().getSubsLinkId())) {
            additionalParams.put(SUBS_LINK_ID,
                    new String[] { String.valueOf(orderDetail.getExtendInfo().getSubsLinkInfo().getSubsLinkId()) });
        }

        if (!aoaUtils.isAOAMerchant(mid) && isNativeJsonRequest
                && !TokenType.SSO.getType().equals(request.getParameter(TOKEN_TYPE))) {
            boolean isNativeJsonSupported = checkIfMerchantHasPreference((orderDetail.isOfflineFlow() && StringUtils
                    .isNotEmpty(orderDetail.getAggMid())) ? orderDetail.getAggMid() : mid);
            if (!isNativeJsonSupported
                    && !checkIfMerchantHasCheckoutJSSupport(
                            (orderDetail.isOfflineFlow() && StringUtils.isNotEmpty(orderDetail.getAggMid())) ? orderDetail.getAggMid()
                                    : mid, request.getParameter(Native.WORKFLOW))) {
                LOGGER.error("Merchant does not have preference nativeJsonRequest and not checkout flow/checkoutjs blocked, failing txn!");
                failureLogUtil
                        .setFailureMsgForDwhPush(
                                RequestValidationException.getException().getResultCode().getResultCodeId(),
                                "Merchant does not have preference nativeJsonRequest and not checkout flow/checkoutjs blocked, failing txn!",
                                null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(false).build();
            }
        }
        // Setting splitSettlementInfo
        if (orderDetail.getSplitSettlementInfoData() != null) {
            additionalParams
                    .put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoKeys.MERCHANT_SPLIT_SETTLEMENT_INFO,
                            new String[] { JsonMapper.mapObjectToJson(orderDetail.getSplitSettlementInfoData()) });
        }
        // Setting link details
        if (orderDetail.getLinkDetailsData() != null) {
            additionalParams.put(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.SUB_REQUEST_TYPE,
                    new String[] { orderDetail.getLinkDetailsData().getSubRequestType() });
            additionalParams.put(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.Native.LINK_DETAILS,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getLinkDetailsData()) });
            if (orderDetail.getLinkDetailsData().getEdcEmiFields() != null) {
                additionalParams.put(IS_EDC_LINK_TXN, new String[] { TRUE });
            }
        }
        /*
         * We are using this to send it to promoService
         */
        String merchantRequestedChannelId = request.getParameter(Native.CHANNEL_ID);
        additionalParams.put("merchantRequestedChannelId", new String[] { merchantRequestedChannelId });

        String channelID = getChannelId(request);
        additionalParams.put(CHANNEL_ID, new String[] { channelID });

        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
        Map<String, String> nativeSessionUtilExtendInfo = nativeSessionUtil.getExtendInfo(txnToken);
        if (MapUtils.isNotEmpty(nativeSessionUtilExtendInfo)) {
            additionalParams.put(PRODUCT_CODE, new String[] { nativeSessionUtilExtendInfo.get(PRODUCT_CODE) });
        }
        if (MapUtils.isNotEmpty(nativeSessionUtilExtendInfo)
                && nativeSessionUtilExtendInfo.get(ADD_MONEY_DESTINATION) != null) {
            additionalParams.put(ADD_MONEY_DESTINATION,
                    new String[] { nativeSessionUtilExtendInfo.get(ADD_MONEY_DESTINATION).toString() });
        }
        /*
         * For Nativeplus and enhanced Flow, userConsentFlag is extracted from
         * extendInfo and added at first level
         */
        if (Boolean.valueOf(request.getParameter(USER_INVESTMENT_CONSENT_FLAG))) {
            additionalParams.put(USER_INVESTMENT_CONSENT_FLAG,
                    new String[] { request.getParameter(USER_INVESTMENT_CONSENT_FLAG) });
        }
        /*
         * For Native Flow, a sub param map is not generated in controller hence
         * fetching directly from request body ExtendInfo field
         */
        if (request.getParameter(EXTENDINFO) != null) {
            try {
                Map<String, String> extendInfo = JsonMapper.readValue(request.getParameter(EXTENDINFO),
                        new TypeReference<Map<String, String>>() {
                        });
                if (extendInfo != null && !extendInfo.isEmpty()
                        && Boolean.valueOf(extendInfo.get(USER_INVESTMENT_CONSENT_FLAG))) {
                    additionalParams.put(USER_INVESTMENT_CONSENT_FLAG, new String[] { TRUE });
                }
            } catch (IOException e) {
                LOGGER.error("Error in fetching field from extend_info {}", e);
            }
        }

        if (cashierInfoResponse == null) {
            String upiAccRefId = request.getParameter(TheiaConstant.RequestParams.Native.UPI_ACC_REF_ID);
            if (StringUtils.isNotBlank(upiAccRefId)) {
                cashierInfoResponse = callFetchPayOptions(txnToken, channelID, TheiaConstant.RequestHeaders.Version_V2,
                        orderDetail);
                validateAccRefIdInFetchPayV2(cashierInfoResponse, upiAccRefId);
            } else {
                cashierInfoResponse = callFetchPayOptions(txnToken, channelID, TheiaConstant.RequestHeaders.Version_V1,
                        orderDetail);
            }
            if (orderDetail != null && TxnType.ESCROW.equals(orderDetail.getTxnType())) {
                additionalParams.put(PRODUCT_CODE,
                        new String[] { ProductCodes.StandardAcquiringEscrowDelayedSettlement.getId() });
            }
        }
        if (cashierInfoResponse.getBody().isOnTheFlyKYCRequired()) {
            additionalParams.put("isOnTheFlyKYCRequired", new String[] { Boolean.TRUE.toString() });
        }

        if (cashierInfoResponse.getBody().getMerchantDetails() != null
                && StringUtils.isNotBlank(cashierInfoResponse.getBody().getMerchantDetails().getMerchantDisplayName())) {
            additionalParams.put(MERCHANT_DISPLAY_NAME, new String[] { cashierInfoResponse.getBody()
                    .getMerchantDetails().getMerchantDisplayName() });
        }

        // Validate for Fee on addMoney, this has been handled for native+ and
        // native
        // Risk reject in case of scwpay-Enhance and Third party Topups
        validateFeeForAddMoney(request, nativeInitiateRequest, additionalParams, cashierInfoResponse, txnToken);

        CCBillPayment ccBillPayment = orderDetail.getCcBillPayment();

        if (ccBillPayment != null && StringUtils.isNotBlank(ccBillPayment.getCcBillNo())) {
            LOGGER.info("CC bill payment request received");
            additionalParams.put("ccBillPaymentRequest", new String[] { Boolean.TRUE.toString() });
            additionalParams.put("CC_BILL_NO", new String[] { ccBillPayment.getCcBillNo() });
        }

        if (orderDetail.isNativeAddMoney()) {
            LOGGER.info("Native AddMoney request received");
            additionalParams.put("isNativeAddMoney", new String[] { Boolean.TRUE.toString() });
        }
        if (TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE))
                && (EPayMode.ADDANDPAY.equals(cashierInfoResponse.getBody().getPaymentFlow()) || EPayMode.HYBRID
                        .equals(cashierInfoResponse.getBody().getPaymentFlow()))) {
            WalletInfo walletInfo = theiaSessionDataServiceAdapterNative.getWalletInfoFromSession(request);
            Double txnAmount = Double.parseDouble(orderDetail.getTxnAmount().getValue());
            if (walletInfo != null && walletInfo.getWalletBalance() != null
                    && txnAmount <= walletInfo.getWalletBalance()) {
                cashierInfoResponse.getBody().setPaymentFlow(EPayMode.NONE);
            }
        }
        boolean isEligibleForUpiToAddNPay = eligibleForUpiToAddNPay(request, mid, cashierInfoResponse);
        EPayMode payMode = cashierInfoResponse.getBody().getPaymentFlow();
        // As part of PGP-33786, the transaction has been converted to ADDANDPAY
        if (isEligibleForUpiToAddNPay) {
            EventUtils.pushTheiaEvents(
                    EventNameEnum.ONLINE_NATIVE_PAYMENT_REQUEST,
                    new ImmutablePair<>("Converting UPI transaction to ADDANDPAY for paymentMode", request
                            .getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE)));
            payMode = EPayMode.ADDANDPAY;
        }

        if (payMode.equals(EPayMode.HYBRID) || payMode.equals(EPayMode.ADDANDPAY)) {
            LOGGER.info("Setting wallet balance from LitePayView Response");
            String walletBalance = getWalletBalanceFromLitePayViewResponse(cashierInfoResponse);
            additionalParams.put("WALLET_AMOUNT", new String[] { walletBalance });
        }
        additionalParams.put(PAYMENT_FLOW_EXPECTED, new String[] { payMode.getValue() });
        additionalParams.put(TWO_FA_CONFIG, new String[] { request.getParameter(TWO_FA_CONFIG) });
        additionalParams.put(SIM_SUBSCRIPTION_ID, new String[] { request.getParameter(SIM_SUBSCRIPTION_ID) });

        Map<String, Object> litePayViewCacheInfo = nativeSessionUtil.getLitepayviewCacheInfo(txnToken);
        if (litePayViewCacheInfo != null && litePayViewCacheInfo.containsKey("pwpEnabled")
                && StringUtils.equals(Boolean.TRUE.toString(), (String) litePayViewCacheInfo.get("pwpEnabled"))) {
            LOGGER.info("check value for pwp info");
            String pwpCategory = payviewConsultServiceHelper.getCategoryForPWPMerchant(mid);
            additionalParams.put("pwpCategory", new String[] { pwpCategory });
        }

        // Check for KYC in Enhanced Flow with ADD_AND_PAY
        if (isEnhancedNativeFlow && payMode.equals(EPayMode.ADDANDPAY)) {

            // Fail the transaction if kyc is still not done. Based on the flag
            // isOnTheFlyKYCRequired is true.
            if (cashierInfoResponse.getBody().isOnTheFlyKYCRequired()) {
                UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(txnToken);
                if (userDetailsBiz != null) {
                    String userKycKey = "KYC_" + userDetailsBiz.getUserId();
                    boolean hasCustomerDoneKYCRecently = nativeSessionUtil.getKey(userKycKey) != null ? true : false;

                    boolean nativeKYCValidateFlag = Boolean.valueOf(ConfigurationUtil.getProperty(
                            TheiaConstant.RequestParams.KYC_NATIVE_VALIDATE_FLAG, "false"));

                    boolean isAllowedForAllCustomers = Boolean.valueOf(com.paytm.pgplus.common.config.ConfigurationUtil
                            .getProperty(TheiaConstant.RequestParams.KYC_ALLOWED_ALL_FLAG, "false"));

                    List<String> allowedCustIds = Collections.emptyList();

                    if (StringUtils.isNotBlank(com.paytm.pgplus.common.config.ConfigurationUtil
                            .getProperty(TheiaConstant.RequestParams.KYC_ALLOWED_LIST))) {
                        allowedCustIds = Arrays.asList(ConfigurationUtil.getProperty(
                                TheiaConstant.RequestParams.KYC_ALLOWED_LIST).split(","));
                    }
                    // Set the flag as true
                    if (!hasCustomerDoneKYCRecently && nativeKYCValidateFlag
                            && (isAllowedForAllCustomers || allowedCustIds.contains(userDetailsBiz.getUserId()))) {
                        LOGGER.error(BizConstant.FailureLogs.FAILING_ADDNPAY_REQ_WITHOUT_VALID_KYC_USER);
                        failureLogUtil.setFailureMsgForDwhPush(
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                BizConstant.FailureLogs.FAILING_ADDNPAY_REQ_WITHOUT_VALID_KYC_USER, null, true);
                        throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                                .isHTMLResponse(sendHTML).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                                .setRetryMsg(ResultCode.INVALID_KYC.getResultMsg()).build();
                    }
                }
            }
        }

        // Blocking emiSubvention and promo for pwp merchant
        if ((orderDetail.getEmiSubventionToken() != null || orderDetail.getPaymentOffersApplied() != null || orderDetail
                .getPaymentOffersAppliedV2() != null) && isPWPMerchant(mid)) {
            LOGGER.error(BizConstant.FailureLogs.EMI_SUBVENTION_OR_PROMO_IS_NOT_APPLIED);
            if (isNativeJsonRequest(request)) {
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(), null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            failureLogUtil.setFailureMsgForDwhPush(RequestValidationException.getException().getResultCode()
                    .getResultCodeId(), BizConstant.FailureLogs.EMI_SUBVENTION_OR_PROMO_IS_NOT_APPLIED, null, true);
            throw RequestValidationException.getException();
        }

        setNativeTxnInProcessFlagInCache(txnToken);
        setMidOrderIdInRequestAttribute(request, mid, orderId);
        additionalParams.put(TXN_TOKEN, new String[] { txnToken });
        String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
        additionalParams.put(Native.PAYMENT_MODE, new String[] { paymentMode });
        EventUtils.pushTheiaEvents(EventNameEnum.REQUEST_WITH_PAYMENT_MODE, new ImmutablePair<>("PAYMENT_MODE",
                paymentMode));

        // for ZestMoney. Converting ZestMoney from emi channel to nb channel
        if (request.getParameter(Native.CHANNEL_CODE) != null && request.getParameter(Native.CHANNEL_CODE).equals(ZEST)) {
            paymentMode = EPayMethod.NET_BANKING.getMethod();
            additionalParams.put(Native.PAYMENT_MODE, new String[] { paymentMode });
        }

        if (EPayMethod.COD.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = EPayMethod.MP_COD.getMethod();
        }

        String paymentDetails = null;

        additionalParams.put(PRE_AUTH_EXPIRY_DATE, new String[] { orderDetail.getPreAuthExpiryDate() });
        additionalParams.put(
                PRE_AUTH,
                new String[] { String.valueOf(TxnType.AUTH.equals(orderDetail.getTxnType())
                        || TxnType.ESCROW.equals(orderDetail.getTxnType())) });

        if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode)
                || EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode)
                || EPayMethod.EMI.getMethod().equalsIgnoreCase(paymentMode)) {
            // TODO to be removed
            EXT_LOGGER.customInfo("encCardInfo: {}, cardInfo: {}", request.getParameter(Native.ENCRYPTED_CARD_INFO),
                    request.getParameter(Native.CARD_INFO));
            if (StringUtils.isNotBlank(request.getParameter(Native.ENCRYPTED_CARD_INFO))
                    && StringUtils.isNotBlank(request.getParameter(Native.CARD_INFO))) {
                LOGGER.error(BizConstant.FailureLogs.RECEIVED_CARD_INFO_IN_BOTH);
                failureLogUtil.setFailureMsgForDwhPush(RequestValidationException.getException().getResultCode()
                        .getResultCodeId(), BizConstant.FailureLogs.RECEIVED_CARD_INFO_IN_BOTH, null, true);
                throw RequestValidationException.getException();
            } else if (StringUtils.isNotBlank(request.getParameter(Native.ENCRYPTED_CARD_INFO))) {
                additionalParams
                        .put(PAYMENT_DETAILS, new String[] { request.getParameter(Native.ENCRYPTED_CARD_INFO) });
                additionalParams.put(IS_ENCRYPTED_CARD_DETAIL, new String[] { IS_ENCRYPTED_CARD_DETAIL_STATUS });
            } else if (cardTokenInfo != null) {
                // Added this to supported encryped card info for payments
                String cardInfo = request.getParameter(Native.CARD_INFO);
                if (merchantPreferenceService.isEncryptedCardMerchant(mid) && isEncryptedCardDetails(cardInfo)) {
                    cardInfo = getDecryptedCardInfo(cardInfo.trim());
                }
                String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
                if (cardDetails.length != 4 && cardDetails[2].trim().length() == 0) {
                    LOGGER.error("Invalid cardDetails length or CVV is missing: {}", cardDetails.length);
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.INVALID_CARD_DETAILS_LENGTH_OR_CVV_MISSING, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }

                String requiredCardDetails = parseCVV(cardDetails[2]);
                additionalParams.put(PAYMENT_DETAILS, new String[] { requiredCardDetails });
                additionalParams.put(CARDTOKENINFO, new String[] { cardTokenInfo });
                additionalParams.put(COFTTOKEN_TXN, new String[] { Boolean.TRUE.toString() });
            } else if (ecomTokenInfo != null) {
                EcomTokenInfo ecomTokenInfoObj = JsonMapper.mapJsonToObject(ecomTokenInfo, EcomTokenInfo.class);

                if (StringUtils.isBlank(ecomTokenInfoObj.getEcomToken())
                        || StringUtils.isBlank(ecomTokenInfoObj.getExpiryMonth())
                        || StringUtils.isBlank(ecomTokenInfoObj.getExpiryYear())
                        || StringUtils.isBlank(ecomTokenInfoObj.getAuthenticationValue())) {
                    LOGGER.error("Invalid ecomTokenInfo : {}", ecomTokenInfoObj);
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.INVALID_ECOM_TOKEN_INFO, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }

                String requiredCardDetails = null;
                requiredCardDetails = JsonMapper.mapObjectToJson(ecomTokenInfoObj);

                /*
                 * update paymode as the bin cardtype, this is for ecomToken,
                 * not savedCardId
                 */
                if ((EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode) || EPayMethod.CREDIT_CARD
                        .getMethod().equalsIgnoreCase(paymentMode))
                        && ecomTokenInfoObj.getEcomToken().trim().length() != 0) {
                    String cardType = getPaymodeForEcomToken(ecomTokenInfoObj.getEcomToken().trim());
                    if (StringUtils.isNotBlank(cardType)) {
                        EPayMethod payMeth = EPayMethod.getPayMethodByMethod(cardType);
                        if (payMeth != null) {
                            paymentMode = payMeth.getMethod();
                        }
                    }
                }
                additionalParams.put(PAYMENT_DETAILS, new String[] { requiredCardDetails });
                additionalParams.put(ECOMTOKEN_TXN, new String[] { Boolean.TRUE.toString() });
            } else {
                String cardInfo = request.getParameter(Native.CARD_INFO);

                // Below handling for Mutual Fund lumpsum payment with saved
                // subscription
                if (StringUtils.isBlank(cardInfo)
                        && ERequestType.NATIVE_MF.getType().equals(orderDetail.getRequestType())) {
                    String subsId = StringUtils.isNotBlank(request.getParameter(SUBSCRIPTION_ID)) ? request
                            .getParameter(SUBSCRIPTION_ID) : request.getParameter(SUBS_ID);
                    cardInfo = subsId.concat("|||");
                    additionalParams.put(IS_SUBS_MOTO_PAYMENT, new String[] { TRUE });
                }

                /* full card Number encrypted with etm jar PGP-32407 */
                if (merchantPreferenceService.isEncryptedCardMerchant(mid) && isEncryptedCardDetails(cardInfo)) {
                    cardInfo = getDecryptedCardInfo(cardInfo.trim());
                }

                String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
                String requiredCardDetails = "";
                if (cardDetails.length != 4) {
                    LOGGER.error("Invalid cardDetails length: {}", cardDetails.length);
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.INVALID_CARD_DETAILS_LENGTH, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }

                String cvv = parseCVV(cardDetails[2]);
                cardDetails[2] = cvv;
                if (cardDetails[0].trim().length() != 0) {
                    if (isVisaSingleClickPayment(request)) {
                        requiredCardDetails = cardDetails[0].trim() + MAESTRO_CVV;
                    } else if (cardDetails[2].trim().length() > 0) {
                        requiredCardDetails = cardDetails[0].trim() + "|" + cardDetails[2].trim();
                    } else if (Boolean.valueOf(request.getParameter(IDEBIT_OPTION))) {
                        requiredCardDetails = cardDetails[0].trim() + "|" + cardDetails[2].trim();
                    } else {
                        requiredCardDetails = cardDetails[0].trim() + MAESTRO_CVV;
                    }
                    if (cardDetails[0].trim().length() > 15 && cardDetails[0].trim().length() < 45) {
                        additionalParams.put(COFTTOKEN_TXN, new String[] { Boolean.TRUE.toString() });
                    }
                } else if (cardDetails[1].trim().length() != 0
                        && (isMaestroCardScheme(cardDetails[1].trim()) || isBajajCardScheme(cardDetails[1].trim()) || isVisaSingleClickPayment(request))) {
                    requiredCardDetails = cardDetails[1].trim() + MAESTRO_CVV + getExpiry(request, cardDetails);
                } else if (cardDetails[1].trim().length() != 0 && isBajajFnScheme(cardDetails[1].trim())) {
                    requiredCardDetails = cardDetails[1].trim() + BAJAJFN_CVV + "|" + cardDetails[3].trim();
                } else {
                    requiredCardDetails = cardDetails[1].trim() + "|" + cardDetails[2].trim() + "|"
                            + cardDetails[3].trim();
                }
                if (cardDetails[1].trim().length() != 0) {
                    paymentDetails = requiredCardDetails;
                }

                /*
                 * update paymode as the bin cardtype, this is for cardnumbers,
                 * not savedCardId
                 */
                if ((EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode) || EPayMethod.CREDIT_CARD
                        .getMethod().equalsIgnoreCase(paymentMode)) && cardDetails[1].trim().length() != 0) {
                    String cardType = getPaymodeForCardBin(requiredCardDetails);
                    if (StringUtils.isNotBlank(cardType)) {
                        EPayMethod payMeth = EPayMethod.getPayMethodByMethod(cardType);
                        if (payMeth != null) {
                            paymentMode = payMeth.getMethod();
                        }
                    }
                }
                checkIfIssuerBlockOnMid(sendHTML, mid, orderDetail, requiredCardDetails,
                        request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_FLOW));
                additionalParams.put(PAYMENT_DETAILS, new String[] { requiredCardDetails });
            }

            validateSubscriptionPayMethods(request, additionalParams, orderDetail, sendHTML);
        }
        if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.Native.AUTH_MODE))
                && (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode))) {
            String authMode = (String) request.getParameter(TheiaConstant.RequestParams.Native.AUTH_MODE);
            if (AuthMode.OTP.getType().equals(authMode)) {
                additionalParams.put(IDEBIT_OPTION, new String[] { "false" });
            } else if (AuthMode.PIN.getType().equals(authMode)) {
                additionalParams.put(IDEBIT_OPTION, new String[] { "true" });
            } else {
                LOGGER.error("Invalid authMode: {}", authMode);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        BizConstant.FailureLogs.INVALID_AUTH_MODE, null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
        }

        String paymentFlow = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_FLOW);
        if (StringUtils.isBlank(paymentFlow)) {
            paymentFlow = EPayMode.NONE.getValue();
        }

        if (!StringUtils.isBlank(paymentFlow)) {
            EPayMode paymentFlowExpected = cashierInfoResponse.getBody().getPaymentFlow();
            if (isEligibleForUpiToAddNPay) {
                paymentFlow = EPayMode.ADDANDPAY.getValue();
                paymentFlowExpected = EPayMode.ADDANDPAY;
            }
            if ((AOA_WORKFLOW).equals(request.getParameter(Native.WORKFLOW))) {
                paymentFlowExpected = EPayMode.valueOf(paymentFlow);
            }
            if (!isPaymentFlowEnable(paymentFlow, paymentFlowExpected)) {
                LOGGER.error("Invalid paymentFlow: {} but expected: {}", paymentFlow, paymentFlowExpected);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        BizConstant.FailureLogs.INVALID_PAYMENT_FLOW, null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();

            }
            if (!isPaymentFlowEnableForPayMethod(paymentFlow, paymentMode)) {
                LOGGER.error("Invalid paymentFlow: {} payment flow not supported for payment mode: {}", paymentFlow,
                        paymentMode);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        "Invalid paymentFlow, payment flow not supported for payment mode", null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }

            if (EPayMethod.EMI.getMethod().equalsIgnoreCase(paymentMode)
                    && PaymentFlow.HYBRID.getType().equals(paymentFlow)) {
                String bankCode = StringUtils.substringBefore(request.getParameter("planId"), "|");
                if (CashierConstant.BAJAJFN.equals(bankCode)) {
                    LOGGER.error(BizConstant.FailureLogs.INVALID_PAYMODE_BAJAJFN_EMI);
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.INVALID_PAYMODE_BAJAJFN_EMI, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }
            }

            if (PaymentFlow.ADD_AND_PAY.equals(PaymentFlow.paymentFlowByEPayMode(EPayMode.valueOf(paymentFlow)))) {
                additionalParams.put(ADD_MONEY, new String[] { "1" });
            } else if (PaymentFlow.HYBRID.getType().equals(paymentFlow)) {
                additionalParams.put(ADD_MONEY, new String[] { "0" });
            }
        }

        if (EPayMethod.EMI.getMethod().equalsIgnoreCase(paymentMode)) {
            boolean paymentAllowed = isPaymentAllowedForEMIWithTxnAmt(request, cashierInfoResponse,
                    orderDetail.getTxnAmount(), request.getParameter(Native.PLAN_ID),
                    request.getParameter(Native.EMI_TYPE), isEnhancedNativeFlow, paymentDetails);
            additionalParams.put(Native.PAYMENT_MODE, new String[] { paymentMode });
            additionalParams.put(Native.PLAN_ID, new String[] { request.getParameter(Native.PLAN_ID) });
            if (!paymentAllowed) {
                LOGGER.error("Invalid EmiTxnAmnt, paymentMode: {}", paymentMode);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), "Invalid EmiTxnAmnt", null,
                        true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            // validate emiId passed in inititate Transaction
            boolean isValidPlanId = isValidEmiPlanEnteredInNative(orderDetail, request.getParameter(Native.PLAN_ID));
            if (!isValidPlanId) {
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(), null, true);

                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
        }
        if (EPayMethod.PPBL.getOldName().equalsIgnoreCase(paymentMode)) {
            FetchAccountBalanceResponse fetchAccountBalanceResponse = null;
            if ((TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE)))) {
                String midSSOToken = nativeSessionUtil.createTokenForMidSSOFlow(request.getParameter(SSO_TOKEN), mid);
                fetchAccountBalanceResponse = nativeSessionUtil.getAccountBalanceResponseFromCache(midSSOToken);
            } else {
                fetchAccountBalanceResponse = nativeSessionUtil.getAccountBalanceResponseFromCache(request
                        .getParameter(Native.TXN_TOKEN));
            }
            if (fetchAccountBalanceResponse != null
                    && StringUtils.isNotBlank(fetchAccountBalanceResponse.getAccountType())) {
                additionalParams.put(PPBL_ACCOUNT_TYPE, new String[] { fetchAccountBalanceResponse.getAccountType() });
            }
        }

        if (EPayMethod.BALANCE.getMethod().equals(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.PPI.value;
        } else if (EPayMethod.PPBL.getOldName().equals(paymentMode)) {
            additionalParams.put(BANK_CODE, new String[] { paymentMode });
            if (StringUtils.isBlank(request.getParameter(TheiaConstant.RequestParams.Native.MPIN))) {
                LOGGER.error(BizConstant.FailureLogs.INVALID_MPIN_OR_ACCESS_TOKEN_FOR_PPBL);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        BizConstant.FailureLogs.INVALID_MPIN_OR_ACCESS_TOKEN_FOR_PPBL, null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            additionalParams.put(PAYMENT_DETAILS,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.MPIN) });
            paymentMode = PaymentTypeIdEnum.NB.value;
            additionalParams.put(ACCOUNT_NUMBER,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.ACCOUNT_NUMBER) });
        } else if (EPayMethod.NET_BANKING.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.NB.value;
            additionalParams.put(ACCOUNT_NUMBER,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.ACCOUNT_NUMBER) });

            String bankCode = request.getParameter(TheiaConstant.RequestParams.Native.CHANNEL_CODE);
            additionalParams.put(PAYMENT_DETAILS, new String[] { bankCode });
            additionalParams.put(BANK_CODE, new String[] { bankCode });

            PayOption merchantPayOption = cashierInfoResponse.getBody().getMerchantPayOption();

            PayMethod nbPayMethod = merchantPayOption.getPayMethods().stream()
                    .filter(payMethod -> EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())).findAny()
                    .orElse(null);

            if (StringUtils.isNotBlank(bankCode) && nbPayMethod != null
                    && CollectionUtils.isNotEmpty(nbPayMethod.getPayChannelOptions())) {
                for (PayChannelBase payChannelBase : nbPayMethod.getPayChannelOptions()) {
                    Bank bank = (Bank) payChannelBase;
                    if (bankCode.equalsIgnoreCase(bank.getInstId())) {
                        additionalParams.put(BANK_NAME, new String[] { bank.getInstName() });
                        break;
                    }
                }
            }
            verifyEnabledBankForNetBanking(nbPayMethod, bankCode, sendHTML);

        } else if (EPayMethod.UPI.getMethod().equalsIgnoreCase(paymentMode)
                || PaymentTypeIdEnum.UPI_LITE.value.equals(paymentMode)) {
            if (!PaymentTypeIdEnum.UPI_LITE.value.equals(paymentMode)
                    && ((StringUtils.isNotBlank(request.getParameter(CHANNEL_CODE)) && request.getParameter(
                            CHANNEL_CODE).equals("UPI")) || StringUtils.isBlank(request.getParameter(Native.MPIN)))) {

                Double txnAmount = Double.parseDouble(orderDetail.getTxnAmount().getValue());
                Double upiCollectLimitUnverified = Double.parseDouble(com.paytm.pgplus.theia.utils.ConfigurationUtil
                        .getProperty(UPI_COLLECT_LIMIT_UNVERIFIED, "2000"));
                boolean blockPaymentForUPICollect = txnAmount > upiCollectLimitUnverified
                        && !merchantPreferenceService.isUpiCollectWhitelisted(mid, true);
                if (blockPaymentForUPICollect) {
                    LOGGER.error("Merchant is not verified and UPI collect txn is > {} : {}",
                            upiCollectLimitUnverified, orderDetail);
                    failureLogUtil.setFailureMsgForDwhPush(RISK_REJECT.getCode(), "Merchant is not verified", null,
                            true);
                    throw new NativeFlowException.ExceptionBuilder(RISK_REJECT).isHTMLResponse(sendHTML)
                            .isRetryAllowed(false).isRedirectEnhanceFlow(true).setRetryMsg(RISK_REJECT.getMessage())
                            .build();
                }
            }

            validationForCCOnUPI(request, sendHTML);
            paymentMode = EPayMethod.UPI.getMethod();

            verifyEnabledUpiPsp(orderDetail, request, sendHTML);
            if (!EPayMode.ADDANDPAY.equals(cashierInfoResponse.getBody().getPaymentFlow())
                    && !validateUpiChannels(request, cashierInfoResponse, paymentMode, mid)) {
                // throw exception
                failureLogUtil.setFailureMsgForDwhPush(INVALID_PAYMENTMODE.getCode(), INVALID_PAYMENTMODE.getMessage(),
                        null, true);
                throw new NativeFlowException.ExceptionBuilder(INVALID_PAYMENTMODE).isHTMLResponse(sendHTML)
                        .isRetryAllowed(false).isRedirectEnhanceFlow(true)
                        .setRetryMsg(INVALID_PAYMENTMODE.getMessage()).build();
            }

            if (ff4jUtils.isFeatureEnabledOnMid(mid, FEATURE_ENABLE_UPI_COLLECT_ON_ADDNPAY, false)) {
                verifyEnableUpiCollectChannel(cashierInfoResponse, orderDetail, request, sendHTML);
            }

            if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.Native.UPI_ACC_REF_ID))) {
                String vpa = request.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT);
                if (StringUtils.isEmpty(vpa)) {
                    LOGGER.info("Getting vpa from cache");
                    vpa = getVPAForAccRefId(cashierInfoResponse, cashierInfoResponse.getBody().getPaymentFlow());
                }
                additionalParams.put(PAYMENT_DETAILS, new String[] { vpa });
            } else {
                additionalParams.put(PAYMENT_DETAILS,
                        new String[] { request.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT) });
            }
            additionalParams.put(ACCOUNT_NUMBER,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.ACCOUNT_NUMBER) });
            additionalParams.put(SEQUENCE_NO,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.SEQ_NUMBER) });
            additionalParams.put(BANK_NAME,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.BANK_NAME) });
            additionalParams.put(CREDIT_BLOCK,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.CREDIT_BLOCK) });
            additionalParams.put(MPIN, new String[] { request.getParameter(TheiaConstant.RequestParams.Native.MPIN) });
            additionalParams.put(DEVICE_ID,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.DEVICE_ID) });
            additionalParams.put(UPI_ACC_REF_ID,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.UPI_ACC_REF_ID) });
            additionalParams.put(ORIGIN_CHANNEL, new String[] { request.getParameter(ORIGIN_CHANNEL) });
            additionalParams.put(BizConstant.UPILITE_REQUEST_DATA,
                    new String[] { request.getParameter(BizConstant.UPILITE_REQUEST_DATA) });

            if (StringUtils.isNotBlank(request.getParameter(MERCHANT_VPA))) {
                additionalParams.put(MERCHANT_VPA, new String[] { request.getParameter(MERCHANT_VPA) });
            }
            paymentMode = PaymentTypeIdEnum.UPI.value;

        } else if (EPayMethod.UPI_INTENT.getMethod().equalsIgnoreCase(paymentMode)) {
            LOGGER.info("Payment Request received for UPI Intent,Setting Parameters for the same");
            if (!EPayMode.ADDANDPAY.equals(cashierInfoResponse.getBody().getPaymentFlow())
                    && !validateUpiChannels(request, cashierInfoResponse, paymentMode, mid)) {
                // throw exception
                failureLogUtil.setFailureMsgForDwhPush(INVALID_PAYMENTMODE.getCode(), INVALID_PAYMENTMODE.getMessage(),
                        null, true);
                throw new NativeFlowException.ExceptionBuilder(INVALID_PAYMENTMODE).isHTMLResponse(sendHTML)
                        .isRetryAllowed(false).isRedirectEnhanceFlow(true)
                        .setRetryMsg(INVALID_PAYMENTMODE.getMessage()).build();
            }
            if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.Native.UPI_ACC_REF_ID))) {
                String vpa = request.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT);
                if (StringUtils.isEmpty(vpa)) {
                    LOGGER.info("Getting vpa from cache");
                    vpa = getVPAForAccRefId(cashierInfoResponse, cashierInfoResponse.getBody().getPaymentFlow());
                }
                additionalParams.put(PAYMENT_DETAILS, new String[] { vpa });
            } else {
                additionalParams.put(PAYMENT_DETAILS,
                        new String[] { request.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT) });
            }
            additionalParams.put(Native.TXN_NOTE, new String[] { request.getParameter(Native.TXN_NOTE) });
            additionalParams.put(Native.REF_URL, new String[] { request.getParameter(Native.REF_URL) });
            additionalParams.put(Native.AGG_MID, new String[] { request.getParameter(AGG_MID) });
            additionalParams.put(Native.EXTEND_INFO, new String[] { request.getParameter(Native.EXTEND_INFO) });
            additionalParams.put(PAYMENT_DETAILS, new String[] { "dummyvpa@upi" });
            additionalParams.put(VIRTUAL_PAYMENT_ADDRESS, new String[] { "dummyvpa@upi" });
            additionalParams.put(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ,
                    new String[] { "true" });
            request.setAttribute(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ, "true");
            additionalParams.put(OS_TYPE, new String[] { request.getParameter(OS_TYPE) });
            additionalParams.put(PSP_APP, new String[] { request.getParameter(PSP_APP) });
            paymentMode = PaymentTypeIdEnum.UPI.value;
        } else if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.DC.value;
        } else if (EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.CC.value;
        } else if ((EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equalsIgnoreCase(paymentMode))) {
            String addMoney = null;
            if (null != additionalParams.get(ADD_MONEY)) {
                addMoney = additionalParams.get(ADD_MONEY)[0];
            }
            if (isPayMethodAllowed(paymentMode, txnToken, addMoney, false, null, mid)) {
                /*
                 * in case of native tempToken to get paytm_digital_credit data
                 * is txnToken but if data in redis is saved on mid+sso(for
                 * tokenType sso),then tempToken will be mid+sso
                 */
                String tempToken = txnToken;
                DigitalCreditAccountInfo accountInfo = (DigitalCreditAccountInfo) getAccountInfo(cashierInfoResponse,
                        paymentFlow);
                if (TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE)) && null == accountInfo) {
                    // in case of sso based flow fetch balance will save account
                    // info on mid+sso token
                    tempToken = nativeSessionUtil.createTokenForMidSSOFlow(orderDetail.getPaytmSsoToken(),
                            orderDetail.getMid());
                    accountInfo = checkAccountInfoForSSOBasedFlow(tempToken, paymentFlow);
                }

                if (null == accountInfo) {
                    /*
                     * now since in case of native we are not sure merchant will
                     * hit fetch balance api or not. So we will internally call
                     * fetch balance
                     */
                    BalanceInfoResponse balanceInfoResponse = fetchBalance(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod(),
                            txnToken, request);

                    cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
                    accountInfo = (DigitalCreditAccountInfo) getAccountInfo(cashierInfoResponse, paymentFlow);
                    if (TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE)) && null == accountInfo) {
                        // in case of sso based flow fetch balance will save
                        // account
                        // info on mid+sso token

                        accountInfo = checkAccountInfoForSSOBasedFlow(tempToken, paymentFlow);
                    }

                    if (!ResultCode.SUCCESS.getResultStatus().equals(
                            balanceInfoResponse.getBody().getResultInfo().getResultStatus())
                            || null == accountInfo) {
                        // if now also account info is null or fetch balance
                        // call fails , break the transaction.
                        LOGGER.error(BizConstant.FailureLogs.ACCOUNT_INFO_FOR_PAYTM_DIGITAL_CREDIT_IS_UNAVAILABLE);
                        failureLogUtil.setFailureMsgForDwhPush(
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                BizConstant.FailureLogs.ACCOUNT_INFO_FOR_PAYTM_DIGITAL_CREDIT_IS_UNAVAILABLE, null,
                                true);
                        throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                                .isHTMLResponse(sendHTML).isRetryAllowed(true)
                                .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                    }
                }
                String passCode = request.getParameter(TheiaConstant.RequestParams.Native.MPIN);
                if (StringUtils.isBlank(passCode) && isPostPaidMPinRequired(tempToken)) {
                    LOGGER.error(BizConstant.FailureLogs.INVALID_MPIN_OR_ACCESS_TOKEN_FOR_POST_PAID);
                    failureLogUtil.setFailureMsgForDwhPush(
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            BizConstant.FailureLogs.INVALID_MPIN_OR_ACCESS_TOKEN_FOR_POST_PAID, null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }
                StringBuilder digitalCreditPaymentDetails = new StringBuilder();

                additionalParams.put(KYC_CODE, new String[] { accountInfo.getExtendInfo().get(KYC_CODE) });
                additionalParams.put(KYC_VERSION, new String[] { accountInfo.getExtendInfo().get(KYC_VERSION) });
                additionalParams.put(FLOW_TYPE, new String[] { FLOW_TYPE_TRANSACTION });

                digitalCreditPaymentDetails.append(accountInfo.getPayerAccountNo()).append("|")
                        .append(accountInfo.getExtendInfo().get(LENDER_ID)).append("|")
                        .append(StringUtils.isNotBlank(passCode) ? passCode : "");
                additionalParams.put(PAYMENT_DETAILS, new String[] { digitalCreditPaymentDetails.toString() });
                paymentMode = PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value;
            }
        } else if (EPayMethod.EMI.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.EMI.value;
            additionalParams.put(BANK_CODE,
                    new String[] { StringUtils.substringBefore(request.getParameter("planId"), "|") });
            additionalParams.put(EMI_TYPE, new String[] { request.getParameter("EMI_TYPE") });
        } else if (EPayMethod.BANK_MANDATE.getMethod().equalsIgnoreCase(paymentMode)) {
            LOGGER.info("Request received for creating bank Mandates, setting parameters for the same");
            paymentMode = PaymentTypeIdEnum.BANK_MANDATE.value;
            additionalParams.put(ACCOUNT_NUMBER,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.ACCOUNT_NUMBER) });
            additionalParams.put(PAYMENT_DETAILS,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.CHANNEL_CODE) });
            additionalParams.put(BANK_CODE,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.CHANNEL_CODE) });
            additionalParams.put(UPI_ACC_REF_ID,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.UPI_ACC_REF_ID) });
            additionalParams.put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.USER_NAME,
                    new String[] { request.getParameter(Native.USER_NAME) });
            additionalParams.put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ACCOUNT_TYPE,
                    new String[] { request.getParameter(Native.ACCOUNT_TYPE) });
            additionalParams.put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.BANK_IFSC,
                    new String[] { request.getParameter(Native.BANK_IFSC) });
            additionalParams.put(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MANDATE_AUTH_MODE,
                    new String[] { request.getParameter(Native.MANDATE_AUTH_MODE) });
            additionalParams.put(Native.MANDATE_TYPE, new String[] { request.getParameter(Native.MANDATE_TYPE) });
            additionalParams.put(BANK_NAME,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.BANK_NAME) });
        } else if (EPayMethod.GIFT_VOUCHER.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.GIFT_VOUCHER.value;
            String templateId = request.getParameter(TheiaConstant.RequestParams.Native.MGV_TEMPLATE_ID);
            if (StringUtils.isBlank(templateId)) {
                LOGGER.error(BizConstant.FailureLogs.INVALID_TEMPLATE_ID_FOR_MGV);
                failureLogUtil.setFailureMsgForDwhPush(
                        ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                        BizConstant.FailureLogs.INVALID_TEMPLATE_ID_FOR_MGV, null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(sendHTML).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }

            additionalParams.put(TheiaConstant.RequestParams.Native.MGV_TEMPLATE_ID, new String[] { templateId });
        }

        String saveForFuture = request.getParameter(TheiaConstant.RequestParams.Native.STORE_INSTRUMENT);
        if ("1".equals(saveForFuture)) {
            additionalParams.put(TheiaConstant.RequestParams.STORE_CARD, new String[] { "1" });
        }
        String industryTypeId = getIndustryTypeId(mid);
        additionalParams.put(TheiaConstant.RequestParams.INDUSTRY_TYPE_ID, new String[] { industryTypeId });
        additionalParams.put(TheiaConstant.RequestParams.AUTH_MODE, new String[] { "3D" });

        if (aoaUtils.isAOAMerchant(mid) && !ERequestType.isSubscriptionRequest(orderDetail.getRequestType())) {
            additionalParams.put(REQUEST_TYPE, new String[] { TheiaConstant.RequestTypes.UNI_PAY });

        } else if (ERequestType.isSubscriptionRequest(orderDetail.getRequestType())) {
            additionalParams.put(REQUEST_TYPE, new String[] { orderDetail.getRequestType() });
            if (StringUtils.isNotEmpty(request.getParameter(SUBSCRIPTION_ID))) {
                additionalParams.put(SUBSCRIPTION_ID, new String[] { request.getParameter(SUBSCRIPTION_ID) });
            } else {
                StringBuilder key = new StringBuilder(orderDetail.getRequestType()).append(txnToken);
                if (aoaUtils.isAOAMerchant(mid)) {
                    LOGGER.error("AOA subscription client call is being used");
                    // AoaSubscriptionCreateResponse aoaSubscriptionResponse =
                    // (AoaSubscriptionCreateResponse)
                    // theiaTransactionalRedisUtil
                    // .get(key.toString());
                    // if (aoaSubscriptionResponse != null) {
                    // additionalParams.put(SUBSCRIPTION_ID,
                    // new String[] {
                    // aoaSubscriptionResponse.getSubscriptionId() });
                    // }
                } else {
                    SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil
                            .get(key.toString());
                    if (subscriptionResponse != null) {
                        additionalParams
                                .put(SUBSCRIPTION_ID, new String[] { subscriptionResponse.getSubscriptionId() });
                        if (StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                                && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                            additionalParams.put(DUMMY_MERCHANT_ID,
                                    new String[] { subscriptionResponse.getPaymentMid() });
                            additionalParams.put(DUMMY_ORDER_ID,
                                    new String[] { subscriptionResponse.getPaymentOrderId() });
                            additionalParams.put(AUTO_REFUND, new String[] { Boolean.TRUE.toString() });
                        }
                    }
                }
            }

            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = (SubscriptionTransactionRequestBody) orderDetail;
            if (subscriptionTransactionRequestBody != null) {
                additionalParams.put(SUBS_FREQUENCY_UNIT,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionFrequencyUnit() });
                additionalParams.put(SUBS_EXPIRY_DATE,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionExpiryDate() });

                additionalParams.put(SUBS_START_DATE,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionStartDate() });

                additionalParams.put(SUBS_GRACE_DAYS,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionGraceDays() });
                additionalParams.put(VALIDATE_ACCOUNT_NUMBER,
                        new String[] { subscriptionTransactionRequestBody.getValidateAccountNumber() });
                if (StringUtils.isNotBlank(subscriptionTransactionRequestBody.getAccountNumber()))
                    additionalParams.put(ACCOUNT_NUMBER,
                            new String[] { subscriptionTransactionRequestBody.getAccountNumber() });
                additionalParams.put(ALLOW_UNVERIFIED_ACCOUNT,
                        new String[] { subscriptionTransactionRequestBody.getAllowUnverifiedAccount() });
                additionalParams.put(SUBS_FREQUENCY,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionFrequency() });
                additionalParams.put(SUBS_ENABLE_RETRY,
                        new String[] { subscriptionTransactionRequestBody.getSubscriptionEnableRetry() });
                additionalParams.put(FLEXI_SUBSCRIPTION,
                        new String[] { String.valueOf(subscriptionTransactionRequestBody.isFlexiSubscription()) });
            }

        } else {
            additionalParams.put(REQUEST_TYPE, new String[] { TheiaConstant.RequestTypes.NATIVE });

        }

        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(
                request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE))) {
            additionalParams.put(REQUEST_TYPE, new String[] { TheiaConstant.RequestTypes.NATIVE_SUBSCRIPTION });
        }

        if ((merchantPreferenceService.isEligibleForMultipleMBIDFlow(mid) && ff4JHelper.isFF4JFeatureForMidEnabled(
                ENABLE_TPV_FOR_ALL_REQUEST_TYPES, mid))
                || (TheiaConstant.RequestTypes.NATIVE_MF.equals(orderDetail.getRequestType())
                        || TheiaConstant.RequestTypes.NATIVE_ST.equals(orderDetail.getRequestType()) || (orderDetail
                        .getLinkDetailsData() != null && (TheiaConstant.RequestTypes.NATIVE_MF.equals(orderDetail
                        .getLinkDetailsData().getSubRequestType()) || TheiaConstant.RequestTypes.NATIVE_ST
                        .equals(orderDetail.getLinkDetailsData().getSubRequestType()))))) {
            // if (merchantPreferenceService.isEligibleForMultipleMBIDFlow(mid)
            // &&
            // ff4JHelper.isFF4JFeatureForMidEnabled(ENABLE_TPV_FOR_ALL_REQUEST_TYPES,
            // mid)) {
            // additionalParams.put(REQUEST_TYPE, new
            // String[]{TheiaConstant.RequestTypes.NATIVE});
            // } else if (orderDetail.getLinkDetailsData() != null) {
            if (orderDetail.getLinkDetailsData() != null) {
                if (!isEnhancedNativeFlow) {
                    additionalParams.put(REQUEST_TYPE, new String[] { orderDetail.getLinkDetailsData()
                            .getSubRequestType() });
                }
            } else {
                if (!orderDetail.getRequestType().equals("Payment"))
                    additionalParams.put(REQUEST_TYPE, new String[] { orderDetail.getRequestType() });
            }
            LOGGER.info("Request Recieved - {}, Updated Request Type - {}", orderDetail.getRequestType(),
                    additionalParams.get(REQUEST_TYPE));

            additionalParams.put(VALIDATE_ACCOUNT_NUMBER, new String[] { "false" });
            if (StringUtils.isNotBlank(orderDetail.getValidateAccountNumber())) {
                additionalParams.put(VALIDATE_ACCOUNT_NUMBER, new String[] { orderDetail.getValidateAccountNumber() });
            }
            if (StringUtils.isNotBlank(orderDetail.getAllowUnverifiedAccount())) {
                additionalParams
                        .put(ALLOW_UNVERIFIED_ACCOUNT, new String[] { orderDetail.getAllowUnverifiedAccount() });
            }
            if (StringUtils.isNotBlank(orderDetail.getAccountNumber())) {
                LOGGER.info("Overriding account Number from txnToken");
                additionalParams.put(ACCOUNT_NUMBER, new String[] { orderDetail.getAccountNumber() });
            }
        }
        additionalParams.put(Native.TOKEN_TYPE, new String[] { request.getParameter(Native.TOKEN_TYPE) });
        additionalParams.put(TXN_AMOUNT, new String[] { orderDetail.getTxnAmount().getValue() });
        if (orderDetail.getTipAmount() != null && orderDetail.getTipAmount().getValue() != null)
            additionalParams.put(TIP_AMOUNT, new String[] { orderDetail.getTipAmount().getValue() });
        additionalParams.put(SSO_TOKEN, new String[] { orderDetail.getPaytmSsoToken() });
        additionalParams.put(CALLBACK_URL, new String[] { orderDetail.getCallbackUrl() });
        additionalParams.put(WEBSITE, new String[] { orderDetail.getWebsiteName() });
        additionalParams.put(MID, new String[] { mid });
        additionalParams.put(ORDER_ID, new String[] { orderId });
        additionalParams.put(PAYMENT_TYPE_ID, new String[] { paymentMode });

        if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.Native.DEVICE_ID))) {
            additionalParams.put(DEVICE_ID,
                    new String[] { request.getParameter(TheiaConstant.RequestParams.Native.DEVICE_ID) });
        } else {
            additionalParams.put(DEVICE_ID, new String[] { request.getParameter(DEVICE_ID_FOR_RISK) });
        }

        if (orderDetail.getExtendInfo() != null) {
            additionalParams.put(MERCH_UNQ_REF, new String[] { orderDetail.getExtendInfo().getMercUnqRef() });
            additionalParams.put(UDF_1, new String[] { orderDetail.getExtendInfo().getUdf1() });
            additionalParams.put(UDF_2, new String[] { orderDetail.getExtendInfo().getUdf2() });
            additionalParams.put(UDF_3, new String[] { orderDetail.getExtendInfo().getUdf3() });
            additionalParams.put(SUB_WALLET_AMOUNT, new String[] { orderDetail.getExtendInfo().getSubwalletAmount() });
            additionalParams.put(ADDITIONAL_INFO, new String[] { orderDetail.getExtendInfo().getComments() });

            // setting link specific variables
            additionalParams.put(LINK_ID, new String[] { orderDetail.getExtendInfo().getLinkId() });
            additionalParams.put(INVOICE_ID, new String[] { orderDetail.getExtendInfo().getLinkInvoiceId() });
            additionalParams.put(LINK_DESCRIPTION, new String[] { orderDetail.getExtendInfo().getLinkDesc() });
            additionalParams.put(LINK_NAME, new String[] { orderDetail.getExtendInfo().getLinkName() });

            if (StringUtils.isNotBlank(orderDetail.getExtendInfo().getAmountToBeRefunded())) {
                additionalParams
                        .put(AMOUNT_TO_BE_REFUNDED, new String[] { JsonMapper.mapObjectToJson(orderDetail
                                .getExtendInfo().getAmountToBeRefunded()) });
            }
        }

        // setting link details in case of link based payment of MF, ST
        if (orderDetail.getLinkDetailsData() != null) {
            additionalParams.put(LINK_ID, new String[] { orderDetail.getLinkDetailsData().getLinkId() });
            additionalParams.put(INVOICE_ID, new String[] { orderDetail.getLinkDetailsData().getInvoiceId() });
            additionalParams.put(LINK_DESCRIPTION,
                    new String[] { orderDetail.getLinkDetailsData().getLinkDescription() });
            additionalParams.put(LINK_NAME, new String[] { orderDetail.getLinkDetailsData().getLinkName() });
        }
        if (null != request.getParameter(WALLET_TYPE)) {
            additionalParams.put(WALLET_TYPE, new String[] { request.getParameter(WALLET_TYPE) });
        }

        if (orderDetail.getUserInfo() != null) {
            additionalParams.put(CUST_ID, new String[] { orderDetail.getUserInfo().getCustId() });
            additionalParams.put(MOBILE_NO, new String[] { orderDetail.getUserInfo().getMobile() });
            additionalParams.put(ADDRESS_1, new String[] { orderDetail.getUserInfo().getAddress() });
            additionalParams.put(PINCODE, new String[] { orderDetail.getUserInfo().getPincode() });
        }
        if (null != orderDetail.getShippingInfo()) {
            additionalParams.put(SHIPPING_INFO,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getShippingInfo()) });
        }
        if (null != orderDetail.getPromoCode()) {
            additionalParams.put(PROMO_CAMP_ID, new String[] { orderDetail.getPromoCode() });
        }

        if (null != orderDetail.getExtendInfo()) {
            additionalParams.put(ConstantsUtil.PARAMETERS.REQUEST.EXTENDINFO,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getExtendInfo()) });
        }

        if (null != orderDetail.getEmiOption()) {
            additionalParams.put(EMI_OPTIONS, new String[] { orderDetail.getEmiOption() });
        }

        if (!StringUtils.isEmpty(orderDetail.getCardTokenRequired())) {
            additionalParams.put(IS_CARD_TOKEN_REQUIRED, new String[] { orderDetail.getCardTokenRequired() });
        }

        // cart validation is to be done by backend
        if ((isEnhancedNativeFlow || isNativeJsonRequest)
                && StringUtils.isNotBlank(orderDetail.getCartValidationRequired())) {
            additionalParams.put(IS_CART_VALIDATION_REQUIRED, new String[] { orderDetail.getCartValidationRequired() });
        }
        if (orderDetail.getAdditionalInfo() != null) {
            additionalParams.put(ADDITIONALINFO,
                    new String[] { JsonMapper.mapObjectToJson(orderDetail.getAdditionalInfo()) });
        }

        // channelId to be put for enhanced as it was received during the call
        // to render
        if (isEnhancedNativeFlow && StringUtils.isNotBlank(orderDetail.getChannelId())) {
            additionalParams.put(CHANNEL_ID, new String[] { orderDetail.getChannelId() });
        }

        if ((isEnhancedNativeFlow || isNativeJsonRequest)
                && StringUtils.isNotBlank(request.getParameter(Native.APP_ID))) {
            additionalParams.put(APP_ID, new String[] { request.getParameter(Native.APP_ID) });
        }

        if (isNativeJsonRequest) {
            additionalParams.put(NATIVE_JSON_REQUEST,
                    new String[] { String.valueOf(request.getAttribute(NATIVE_JSON_REQUEST)) });
        }

        String deepLinkReq[] = additionalParams
                .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ);
        if (deepLinkReq != null && deepLinkReq.length > 0 && "true".equals(deepLinkReq[0])) {
            LOGGER.info("Setting Parameters for UPI Intent");
            // OVERRIDING ADDITIONAL_INFO for UPI_INTENT paymentMode
            additionalParams.put(ADDITIONAL_INFO, new String[] { request.getParameter(Native.EXTEND_INFO) });
            additionalParams.put(INSTA_DEEP_LINK, new String[] { "true" });
            additionalParams.put(ACCOUNT_NUMBER, new String[] { orderDetail.getAccountNumber() });
        }
        /**
         * for Corporate Advance Deposit
         */
        additionalParams.put(CORPORATE_CUST_ID, new String[] { orderDetail.getCorporateCustId() });

        additionalParams.put(CARD_HASH, new String[] { orderDetail.getCardHash() });

        if (orderDetail.getPayableAmount() != null) {
            /*
             * this amount will be sent to promo-service
             */
            additionalParams.put(PROMO_AMOUNT, new String[] { orderDetail.getPayableAmount().getValue() });
        }

        if (EPayMethod.BALANCE.getMethod().equals(paymentMode) || PaymentTypeIdEnum.PPI.getValue().equals(paymentMode)) {
            setFlagToRedirectToShowPaymentPage(request, additionalParams, paymentFlow);
        }

        if (TokenType.SSO.getType().equalsIgnoreCase(request.getParameter(Native.TOKEN_TYPE))) {
            additionalParams.put(TheiaConstant.DataEnrichmentKeys.IS_OFFLINE_MERCHANT, new String[] { TRUE });
        } else {
            if (orderDetail != null && orderDetail.isOfflineFlow())
                additionalParams.put(TheiaConstant.DataEnrichmentKeys.IS_OFFLINE_MERCHANT, new String[] { TRUE });
            else
                additionalParams.put(TheiaConstant.DataEnrichmentKeys.IS_OFFLINE_MERCHANT, new String[] { FALSE });
        }
        if (EPayMethod.BANK_TRANSFER.getMethod().equals(paymentMode)) {
            additionalParams.put(VAN_INFO, new String[] { JsonMapper.mapObjectToJson(orderDetail.getVanInfo()) });
            additionalParams.put(TPV_INFOS, new String[] { JsonMapper.mapObjectToJson(orderDetail.getTpvInfo()) });
        }
        String preferredOtpPage = request.getParameter(Native.PREFERRED_OTP_PAGE);
        if (StringUtils.isNotBlank(preferredOtpPage)
                && !(PreferredOtpPage.MERCHANT.getValue().equals(preferredOtpPage) || PreferredOtpPage.BANK.getValue()
                        .equals(preferredOtpPage))) {
            LOGGER.error("PreferredOtpPage Value found {} Value Could only be {} {}", preferredOtpPage,
                    PreferredOtpPage.MERCHANT.getValue(), PreferredOtpPage.BANK.getValue());
            failureLogUtil.setFailureMsgForDwhPush(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(sendHTML).isRetryAllowed(true)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }
        additionalParams.put(Native.PREFERRED_OTP_PAGE, new String[] { preferredOtpPage });

        if (request.getParameter(CARD_PRE_AUTH_TYPE) != null) {
            additionalParams.put(CARD_PRE_AUTH_TYPE, new String[] { request.getParameter(CARD_PRE_AUTH_TYPE) });
        }
        if (request.getParameter(PRE_AUTH_BLOCK_SECONDS) != null) {
            additionalParams.put(PRE_AUTH_BLOCK_SECONDS, new String[] { request.getParameter(PRE_AUTH_BLOCK_SECONDS) });
        }

        if (isEligibleForUpiToAddNPay) {
            additionalParams.put(UPI_CONVERTED_TO_ADDNPAY, new String[] { TRUE });
        }

        if (request.getParameter(Native.ADD_ONE_RUPEE) != null) {
            additionalParams.put(Native.ADD_ONE_RUPEE, new String[] { request.getParameter(Native.ADD_ONE_RUPEE) });
        }

        if (failIfWalletInactive(paymentFlow, paymentMode, cashierInfoResponse)) {
            LOGGER.error(BizConstant.FailureLogs.USER_WALLET_IS_DEACTIVATED);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.WALLET_INACTIVE_FAILURE.getCode(),
                    BizConstant.FailureLogs.USER_WALLET_IS_DEACTIVATED, null, true);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.WALLET_INACTIVE_FAILURE)
                    .isHTMLResponse(sendHTML).isRetryAllowed(false)
                    .setRetryMsg(ResponseConstants.WALLET_INACTIVE_FAILURE.getMessage()).build();

        }
        if (request.getAttribute(PAYERCMID) != null) {
            additionalParams.put(PAYER_CMID, new String[] { (String) request.getAttribute(PAYERCMID) });
        }

        additionalParams.put(VARIABLE_LENGTH_OTP_SUPPORTED,
                new String[] { request.getParameter(VARIABLE_LENGTH_OTP_SUPPORTED) });
        if (isNativeJsonRequest)
            additionalParams.put(SOURCE, new String[] { request.getHeader(SOURCE) });

        return additionalParams;
    }

    public void paymentOptionCardIndexNoPopulated(HttpServletRequest request, ValidateRequest subventionvalidateRequest) {
        if (subventionvalidateRequest.getPaymentDetails() != null
                && CollectionUtils.isNotEmpty(subventionvalidateRequest.getPaymentDetails().getPaymentOptions())
                && StringUtils.isBlank(subventionvalidateRequest.getPaymentDetails().getPaymentOptions().get(0)
                        .getCardIndexNo()))
            subventionvalidateRequest.getPaymentDetails().setPaymentOptions(
                    Collections.singletonList(subventionEmiServiceHelper.updatePaymentOptionPopulateCardIndexNo(
                            request, subventionvalidateRequest.getPaymentDetails().getPaymentOptions().get(0))));
    }

    private boolean isAllowedWrapper(Map<String, Object> extraParamsMap, String wrapperName) {
        if (extraParamsMap.get("wrapperName") != null && !StringUtils.isBlank(wrapperName)) {
            return StringUtils.equals(wrapperName, extraParamsMap.get("wrapperName").toString());
        }
        return false;
    }

    private void checkIfIssuerBlockOnMid(boolean sendHTML, String mid, InitiateTransactionRequestBody orderDetail,
            String requiredCardDetails, String paymentFlow) {
        if (StringUtils.isNotEmpty(requiredCardDetails)) {
            String[] cardDetails = requiredCardDetails.split(Pattern.quote("|"), -1);
            if (cardDetails.length == 3) {
                Map<String, String> issuerMap = coftUtil.getIssuerTokenProcessingOnMidMap();
                String cardBin = cardDetails[0].substring(0, 6);
                BinDetail binDetail = getBinDetailForCardBin(cardBin);
                if (Objects.nonNull(binDetail) && Objects.nonNull(orderDetail)) {
                    String issuer = mid + "." + binDetail.getCardType() + "." + binDetail.getCardName() + "."
                            + binDetail.getBankCode();
                    if (MapUtils.isNotEmpty(issuerMap)
                            && !coftUtil.checkTokenProcessingEnable(issuerMap, orderDetail.getTxnAmount().getValue(),
                                    issuer)) {
                        failureLogUtil.setFailureMsgForDwhPush(
                                ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultCodeId(),
                                ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultMsg(), null, true);
                        throw new NativeFlowException.ExceptionBuilder(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN)
                                .isHTMLResponse(sendHTML).isRetryAllowed(true).setRetryMsg(CARD_NOT_ALLOWD_ON_MERCHANT)
                                .build();
                    }
                    boolean isAddNPayOrAddMoneyFlow = EPayMode.ADDANDPAY.getValue().equals(paymentFlow)
                            || nativePaymentUtil.isAddMoneyFlow(mid, null, orderDetail);
                    checkIfIssuerAllowedOnMid(mid, ERequestType.getByRequestType(orderDetail.getRequestType()), issuer,
                            isAddNPayOrAddMoneyFlow, sendHTML);
                }
            }
        }
    }

    public void checkIfIssuerAllowedOnMid(String mid, ERequestType requestType, String issuerConfig,
            boolean isAddNPayOrAddMoneyFlow, boolean sendHTML) {
        if (!cardTransactionUtil.isIssuerConfigAllowedOnMid(requestType, mid, issuerConfig, isAddNPayOrAddMoneyFlow)) {
            String customErrorMsg = cardTransactionUtil.getCustomErrMsgForIssuerNotAllowed(mid);
            customErrorMsg = StringUtils.isNotBlank(customErrorMsg) ? customErrorMsg : CARD_NOT_ALLOWD_ON_MERCHANT;
            failureLogUtil.setFailureMsgForDwhPush(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultCodeId(),
                    customErrorMsg, null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN)
                    .isHTMLResponse(sendHTML).isRetryAllowed(true).setMsg(customErrorMsg).setRetryMsg(customErrorMsg)
                    .build();
        }
    }

    public void throwNativeException(HttpServletRequest request, ResponseConstants responseConstants, String msg) {
        if (isEnhancedNativeFlow(request)) {
            throw new NativeFlowException.ExceptionBuilder(responseConstants).setMsg(msg).isHTMLResponse(false)
                    .isRedirectEnhanceFlow(true).build();
        } else if (isNativeJsonRequest(request)) {
            throw new NativeFlowException.ExceptionBuilder(responseConstants).setMsg(msg).isNativeJsonRequest(true)
                    .isHTMLResponse(false).build();
        } else {
            throw new NativeFlowException.ExceptionBuilder(responseConstants).setMsg(msg).isHTMLResponse(true).build();
        }
    }

    private void populateSdkTypeInExtendInfo(HttpServletRequest request, InitiateTransactionRequestBody orderDetail,
            String txnToken) {
        if (orderDetail != null) {
            if (orderDetail.getExtendInfo() == null) {
                orderDetail.setExtendInfo(new ExtendInfo());
            }

            try {
                String extendInfo = request.getParameter(OFFLINE_EXTEND_INFO);
                // for native case check
                if (StringUtils.isBlank(extendInfo)) {
                    extendInfo = request.getParameter(TheiaConstant.RedisKeysConstant.NativeSession.EXTEND_INFO);
                }
                if (StringUtils.isNotBlank(extendInfo) && StringUtils.isBlank(orderDetail.getExtendInfo().getSdkType())) {
                    orderDetail.getExtendInfo().setSdkType(JsonMapper.getStringParamFromJson(extendInfo, SDK_TYPE));
                    nativeSessionUtil.setOrderDetail(txnToken, orderDetail);
                }

            } catch (Exception e) {
                LOGGER.error("Unable to set sdkType in orderDetails :{}", e);
            }
        }
    }

    // this method will work when native json preference is disabled
    private boolean checkIfMerchantHasCheckoutJSSupport(String mid, String workFlow) {
        if (CHECKOUT.equals(workFlow) && !merchantPreferenceService.isBlockCheckoutJS(mid)) {
            return true;
        }
        return false;
    }

    private boolean validateUpiChannels(HttpServletRequest request, NativeCashierInfoResponse cashierInfoResponse,
            String paymentMode, String mid) {

        // getting paymethod UPI from NativecashierResponse
        List<PayMethod> payMethods = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(Objects::nonNull).filter(p -> (EPayMethod.UPI.getMethod().equalsIgnoreCase(p.getPayMethod())))
                .collect(Collectors.toList());

        boolean isValid = false;

        // if empty list then break
        if (CollectionUtils.isEmpty(payMethods)) {
            return false;
        }

        // for UPI_PUSH AND COLLECT
        if (EPayMethod.UPI.getMethod().equalsIgnoreCase(paymentMode)) {
            if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.Native.MPIN))
                    || StringUtils.isNotBlank(request.getParameter(BizConstant.UPILITE_REQUEST_DATA))) {
                // UPI_PUSH_EXPRESS
                if (isValidUpiChannel(payMethods.get(0), "UPIPUSHEXPRESS")) {
                    isValid = true;
                }
            } else {
                // UPI_COLLECT
                if (isValidUpiChannel(payMethods.get(0), "UPI")) {
                    isValid = true;
                }
            }
        } else if (EPayMethod.UPI_INTENT.getMethod().equalsIgnoreCase(paymentMode)) {
            if (isValidUpiChannel(payMethods.get(0), "UPIPUSH")) {
                isValid = true;
            }
        }

        return isValid;
    }

    private boolean isValidUpiChannel(PayMethod payMethod, String channelInstId) {

        if (CollectionUtils.isNotEmpty(payMethod.getPayChannelOptions())) {
            for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                // confirmed. skip for channels disabled due to
                // CHANNEL_NTOT_AVAILABLE reason
                if (payChannelBase.getIsDisabled() != null
                        && TheiaConstant.ExtraConstants.TRUE.equals(payChannelBase.getIsDisabled().getStatus())
                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                payChannelBase.getIsDisabled().getMsg())) {
                    continue;
                }
                if (payChannelBase instanceof Bank) {
                    if (channelInstId.equalsIgnoreCase((((Bank) payChannelBase).getInstId()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getExpiry(HttpServletRequest request, String[] cardDetails) {

        String expiry = MAESTRO_EXPIRY;
        String savedCardId = cardDetails[0].trim();
        String cardExpiry = cardDetails[3].trim();

        if (StringUtils.isBlank(savedCardId) && StringUtils.isNotBlank(cardExpiry) && isVisaSingleClickPayment(request)) {
            expiry = "|" + cardExpiry;
        }
        return expiry;
    }

    private void addFlowTypeOnTxnToken(HttpServletRequest request, String txnToken) {
        if (StringUtils.isNotBlank(txnToken)) {
            String flowType = null;

            if (isEnhancedNativeFlow(request)) {
                flowType = EventNameEnum.ONLINE_ENHANCED_PAYMENT_REQUEST.getEventName();
            } else if (isOfflinePaymentRequestV1Ptc(request)) {
                boolean isRequestFromAppCache = BooleanUtils.isTrue((Boolean) request
                        .getAttribute(Native.IS_ORDER_ID_NEED_TO_BE_GENERATED));
                if (isRequestFromAppCache) {
                    flowType = EventNameEnum.OFFLINE_PAYMENT_REQUEST_CACHED.getEventName();
                } else {
                    flowType = EventNameEnum.OFFLINE_PAYMENT_REQUEST.getEventName();
                }
            } else if (isNativeJsonRequest(request)) {
                flowType = EventNameEnum.ONLINE_NATIVEPLUS_PAYMENT_REQUEST.getEventName();
            } else {
                flowType = EventNameEnum.ONLINE_NATIVE_PAYMENT_REQUEST.getEventName();
            }

            nativeSessionUtil.setFlowType(txnToken, flowType);
        }
    }

    private void validateOneClickPayment(HttpServletRequest request, InitiateTransactionRequestBody orderDetail) {
        if (null != orderDetail) {
            Double txnAmount = Double.parseDouble(orderDetail.getTxnAmount().getValue());
            Double oneClickMaxAmount = Double.parseDouble(com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(
                    ONE_CLICK_MAX_AMOUNT, "2000"));
            if (isVisaSingleClickPayment(request) && txnAmount.compareTo(oneClickMaxAmount) > 0) {
                LOGGER.info("OneClick transaction amount exceeds oneClickMaxAmount: {}", oneClickMaxAmount);
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_ONECLICK_TXN_AMOUNT.getCode(),
                        "OneClick transaction amount exceeds oneClickMaxAmount", null, true);
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_ONECLICK_TXN_AMOUNT)
                        .isNativeJsonRequest(true).build();
            }
        }
    }

    private String getVPAForAccRefId(NativeCashierInfoResponse cashierInfoResponse, EPayMode paymentFlow) {
        if (cashierInfoResponse.getBody() != null) {
            PayOption payOption = EPayMode.ADDANDPAY.equals(paymentFlow) ? cashierInfoResponse.getBody()
                    .getAddMoneyPayOption() : cashierInfoResponse.getBody().getMerchantPayOption();

            if (payOption != null && payOption.getUpiProfileV4() != null
                    && payOption.getUpiProfileV4().getRespDetails() != null
                    && payOption.getUpiProfileV4().getRespDetails().getProfileDetail() != null) {
                List<VpaDetailV4> vpaDetailsList = payOption.getUpiProfileV4().getRespDetails().getProfileDetail()
                        .getVpaDetails();
                for (VpaDetailV4 vpaDetailV4 : vpaDetailsList) {
                    if (vpaDetailV4.isPrimary()) {
                        return vpaDetailV4.getName();
                    }
                }
            } else {
                LOGGER.info("vpaDetailsList is not found");
            }
        }
        return null;
    }

    private void validateAccRefIdInFetchPayV2(NativeCashierInfoResponse nativeCashierInfoResponse, String accRefId) {

        if (nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4().getRespDetails() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4().getRespDetails()
                        .getProfileDetail() != null
                && CollectionUtils.isNotEmpty(nativeCashierInfoResponse.getBody().getMerchantPayOption()
                        .getUpiProfileV4().getRespDetails().getProfileDetail().getBankAccounts())) {

            List<UpiBankAccountV4> bankAccounts = nativeCashierInfoResponse.getBody().getMerchantPayOption()
                    .getUpiProfileV4().getRespDetails().getProfileDetail().getBankAccounts();

            for (UpiBankAccountV4 bankAccount : bankAccounts) {
                if (accRefId.equals(bankAccount.getAccRefId())) {
                    LOGGER.info("AccRefId found in FetchPayV2 response");
                    return;
                }
            }

        }
        LOGGER.error("AccRefId not found in FetchPayV2 response");
        throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                .isHTMLResponse(true).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();

    }

    private void setFlagToRedirectToShowPaymentPage(HttpServletRequest request, Map<String, String[]> additionalParams,
            String paymentFlow) {

        String mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        if (StringUtils.isBlank(mid) || !StringUtils.equals(EPayMode.NONE.getValue(), paymentFlow)) {
            return;
        }
        if (isNativeJsonRequest(request)) {
            if (!ff4JHelper.isFF4JFeatureForMidEnabled(NATIVE_JSON_REQUEST_REDIRECT_WALLET_BALANCE_NOT_ENOUGH, mid)) {
                return;
            }
        } else {
            String midAllowed = com.paytm.pgplus.theia.utils.ConfigurationUtil
                    .getProperty(MID_REDIRECT_TO_SHOW_PAYMENT_PAGE_WALLET_BALANCE_NOT_ENOUGH);
            if (midAllowed == null
                    || (!StringUtils.equals("ALL", midAllowed) && StringUtils.indexOf(midAllowed, mid) < 0)) {
                LOGGER.info("not allowed to redirect to showPaymentPage balance not enough");
                return;
            }
        }

        String[] paymentFlowExpected = additionalParams.get(PAYMENT_FLOW_EXPECTED);
        if (paymentFlowExpected != null && !StringUtils.equals(EPayMode.ADDANDPAY.getValue(), paymentFlowExpected[0])) {
            LOGGER.info("paymentFlowExpected!=ADDANDPAY");
            return;
        }

        if (additionalParams.get(WALLET_AMOUNT) == null || additionalParams.get(TXN_AMOUNT) == null) {
            return;
        }

        Double walletBalance = Double.parseDouble(additionalParams.get(WALLET_AMOUNT)[0]);
        Double txnAmount = Double.parseDouble(additionalParams.get(TXN_AMOUNT)[0]);

        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(
                request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE))) {
            if (NativeSubscriptionUtils.isZeroRupeesSubscription(additionalParams.get(TXN_AMOUNT)[0])) {
                txnAmount = 1d;
            }
        }

        if (txnAmount <= walletBalance) {
            return;
        }

        LOGGER.info("setting flag to redirect to showPaymentPage API");
        additionalParams.put("redirectShowPaymentPage", new String[] { "true" });
    }

    private DigitalCreditAccountInfo checkAccountInfoForSSOBasedFlow(String txnToken, String paymentFlow) {
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
        return (DigitalCreditAccountInfo) getAccountInfo(cashierInfoResponse, paymentFlow);
    }

    private void verifyEnabledBankForNetBanking(PayMethod paymentMethod, String bankCode, boolean sendHtml) {
        if (StringUtils.isBlank(bankCode)) {
            return;
        }
        if (paymentMethod != null) {
            List<String> enabledBanks = paymentMethod.getEnabledBanks();
            if (CollectionUtils.isNotEmpty(enabledBanks)) {
                Set<String> hSet = new HashSet<String>(enabledBanks);
                if (!hSet.contains(bankCode)) {
                    String errorMsg = bankCode
                            + " "
                            + EPayMethod.NET_BANKING.getDisplayName()
                            + com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_ERROR_MESSAGE_PAYMENT_NOT_ALLOWED;
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                            .isHTMLResponse(sendHtml).setMsg(errorMsg).build();
                }
            }
        }
    }

    private void verifyEnabledUpiPsp(InitiateTransactionRequestBody orderDetail, HttpServletRequest httpServletRequest,
            boolean sendHtml) {

        List<String> enabledUpiPsp = orderDetail.getEnableUpiPsp();
        String requestedVpa = httpServletRequest.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT);

        if (CollectionUtils.isEmpty(enabledUpiPsp) || StringUtils.isBlank(requestedVpa)) {
            return;
        }

        boolean isRequestedUpiPspAllowed = false;

        String requestedPsp = StringUtils.substringAfter(requestedVpa, "@");
        for (String upiPsp : enabledUpiPsp) {
            if (StringUtils.equalsIgnoreCase(upiPsp, requestedPsp)) {
                isRequestedUpiPspAllowed = true;
            }
        }

        if (!isRequestedUpiPspAllowed) {
            LOGGER.error("requestedVpa is not allowed for transaction!");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(sendHtml)
                    .setMsg(requestedPsp
                            + " vpa"
                            + com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_ERROR_MESSAGE_PAYMENT_NOT_ALLOWED)
                    .isRetryAllowed(true).build();
        }
    }

    private void validationForCCOnUPI(HttpServletRequest request, boolean sendHtml) {
        String accountType = "";
        try {
            Map<String, Object> creditBlockMap = JsonMapper.getMapFromJson(request.getParameter(Native.CREDIT_BLOCK));
            Map<String, String> defaultDebitMap = null;
            if (null != creditBlockMap) {
                defaultDebitMap = (Map<String, String>) creditBlockMap.get("defaultDebit");
            }
            if (null != defaultDebitMap) {
                accountType = defaultDebitMap.get("accountType");
            }
        } catch (Exception e) {
            LOGGER.info("creditBlock in received in request {}", request.getParameter(Native.CREDIT_BLOCK));
            LOGGER.error("Exception while fetching accountType from request ", e);
        }
        if (CREDIT.equalsIgnoreCase(accountType)
                && !bizRequestResponseMapperHelper.validateAndCheckCCOnUpi(request.getParameter(Native.MID))) {
            LOGGER.error("CC On Upi is Not Enabled for the mid {}", request.getParameter(Native.MID));
            failureLogUtil.setFailureMsgForDwhPush(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    CC_ON_UPI_MSG, null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(sendHtml).isRetryAllowed(true).setMsg(CC_ON_UPI_MSG).setRetryMsg(CC_ON_UPI_MSG)
                    .build();
        }
    }

    private ExtendInfo getExtendInfo(HttpServletRequest request, String extendInfoStr) throws FacadeCheckedException {
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setMercUnqRef(getMercUnqRef(request, extendInfoStr));
        extendInfo.setUdf1(JsonMapper.getStringParamFromJson(extendInfoStr, TheiaConstant.ExtendedInfoKeys.UDF_1));
        extendInfo.setUdf2(JsonMapper.getStringParamFromJson(extendInfoStr, TheiaConstant.ExtendedInfoKeys.UDF_2));
        extendInfo.setUdf3(JsonMapper.getStringParamFromJson(extendInfoStr, TheiaConstant.ExtendedInfoKeys.UDF_3));
        extendInfo.setLinkId(JsonMapper.getStringParamFromJson(extendInfoStr, Native.LINK_ID));
        extendInfo.setLinkName(JsonMapper.getStringParamFromJson(extendInfoStr, Native.LINK_NAME));
        extendInfo.setLinkDesc(JsonMapper.getStringParamFromJson(extendInfoStr, Native.LINK_DESC));
        extendInfo.setLinkInvoiceId(JsonMapper.getStringParamFromJson(extendInfoStr, Native.LINK_INVOICE_ID));
        extendInfo.setSdkType(JsonMapper.getStringParamFromJson(extendInfoStr, SDK_TYPE));
        return extendInfo;
    }

    private String getMercUnqRef(HttpServletRequest request, String extendInfoStr) throws FacadeCheckedException {
        String mid = request.getParameter(Native.MID);
        String orderId = request.getParameter(Native.ORDER_ID);
        if (aoaUtils.isAOAMerchant(mid)) {
            String txnToken = nativeSessionUtil.getTxnToken(mid, orderId);
            if (StringUtils.isNotBlank(txnToken)) {
                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
                if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                    InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                    if (orderDetail != null && orderDetail.getExtendInfo() != null
                            && StringUtils.isNotBlank(orderDetail.getExtendInfo().getMercUnqRef())) {
                        return orderDetail.getExtendInfo().getMercUnqRef();
                    }
                }
            }
        }
        return JsonMapper.getStringParamFromJson(extendInfoStr, Native.MERC_UNQ_REF);
    }

    public BinDetail getBinDetail(final WorkFlowRequestBean flowRequestBean) {
        String cardNumber = flowRequestBean.getCardNo();
        if (StringUtils.isBlank(cardNumber) && flowRequestBean.getIsSavedCard()
                && StringUtils.isNotBlank(flowRequestBean.getSavedCardID())) {
            if (flowRequestBean.getSavedCardID().length() > 15 && !flowRequestBean.isCoftTokenTxn()) {
                cardNumber = nativeSessionUtil.getMaskCardNumberFromCIN(flowRequestBean.getSavedCardID());
            } else {
                SavedCardResponse<SavedCardVO> savedCardResponse = savedCardService.getSavedCardByCardId(Long
                        .parseLong(flowRequestBean.getSavedCardID()));
                if ((savedCardResponse != null) && savedCardResponse.getStatus()) {
                    cardNumber = savedCardResponse.getResponseData().getCardNumber();
                    flowRequestBean.setCardNo(cardNumber);
                }
            }
        }
        if (flowRequestBean.isTxnFromCardIndexNo()) {
            return flowRequestBean.getBinDetail();
        } else if (StringUtils.isNotBlank(cardNumber)) {
            if (flowRequestBean.isEcomTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForEcomToken(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else if (flowRequestBean.isCoftTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForCoft(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else {
                BinDetail binDetail = getBinDetailForCardBin(cardNumber);
                validateBinDetail(binDetail);
                return binDetail;
            }
        }
        return null;
    }

    public BinDetail getBinDetailForEcomToken(final String requiredCardDetails) {

        final String binNumber = requiredCardDetails.substring(0, 9);
        BinDetail binDetails = null;

        try {
            binDetails = ecomTokenUtils.fetchTokenDetails(binNumber);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}", binNumber, e);
        }
        return binDetails;
    }

    public BinDetail getBinDetailForCoft(final String requiredCardDetails) {

        final String binNumber = requiredCardDetails.substring(0, 9);
        BinDetail binDetails = null;

        try {
            binDetails = coftUtils.fetchTokenDetails(binNumber);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}", binNumber, e);
        }
        return binDetails;
    }

    public BinDetail getBinDetailForCardBin(final String requiredCardDetails) {
        /*
         * update paymode as the bin cardtype
         */
        BinDetail binDetails = null;
        try {
            binDetails = cardUtils.fetchBinDetails(requiredCardDetails);

        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}",
                    binHelper.logMaskedBinnumber(requiredCardDetails), e);
        }
        return binDetails;
    }

    private String getPaymodeForEcomToken(final String requiredCardDetails) {
        BinDetail binDetails = getBinDetailForEcomToken(requiredCardDetails);
        if (binDetails != null && StringUtils.isNotBlank(binDetails.getCardType())) {
            return binDetails.getCardType();
        }
        return null;
    }

    private String getPaymodeForCardBin(final String requiredCardDetails) {
        BinDetail binDetails = getBinDetailForCardBin(requiredCardDetails);
        if (binDetails != null && StringUtils.isNotBlank(binDetails.getCardType())) {
            return binDetails.getCardType();
        }
        return null;
    }

    private boolean isCardValidForEMIType(final String requiredCardDetails, String instId, String cardType) {
        if (StringUtils.isEmpty(requiredCardDetails))
            return true;

        BinDetail binDetails = null;
        try {
            binDetails = cardUtils.fetchBinDetails(requiredCardDetails);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}",
                    binHelper.logMaskedBinnumber(requiredCardDetails), e);
        }
        if (binDetails != null) {
            boolean isValid = true;
            if (!instId.equals(binDetails.getBankCode())) {
                // TODO to be removed
                EXT_LOGGER.customInfo("instId from LPVC: {}, BankCode from bin: {}", instId, binDetails.getBankCode());
                isValid = false;
            }
            if (StringUtils.isNotEmpty(binDetails.getCardType()) && !binDetails.getCardType().equals(cardType)) {
                isValid = false;
            }
            return isValid;
        }
        return true;
    }

    public boolean isEnhancedNativeFlow(HttpServletRequest request) {
        return (null != request.getAttribute("NATIVE_ENHANCED_FLOW"))
                && Boolean.TRUE.equals(request.getAttribute("NATIVE_ENHANCED_FLOW"));
    }

    public boolean isDccPaymentRequest(HttpServletRequest request) {
        return (null != request.getAttribute("paymentCallFromDccPage"))
                && Boolean.TRUE.equals(request.getAttribute("paymentCallFromDccPage"));
    }

    public boolean isNativeJsonRequest(HttpServletRequest request) {
        return (request != null && null != request.getAttribute(NATIVE_JSON_REQUEST))
                && Boolean.TRUE.equals(request.getAttribute(NATIVE_JSON_REQUEST));
    }

    private void setMidOrderIdInRequestAttribute(final HttpServletRequest request, String mid, String orderId) {
        request.setAttribute(MID, mid);
        request.setAttribute(ORDER_ID, orderId);
    }

    public String getIndustryTypeId(String mid) {
        String industryTypeId = "NA";
        try {
            MerchantProfile merchantProfileInfo = merchantDataService.getMerchantProfileInfo(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantProfile :: {}", merchantProfileInfo);
            if (null != merchantProfileInfo && null != merchantProfileInfo.getMccCodes()) {
                industryTypeId = merchantProfileInfo.getMccCodes().get(0);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occurred on calling merchant profile api");
        }
        return industryTypeId;
    }

    private AccountInfo getAccountInfo(NativeCashierInfoResponse cashierInfoResponse, String paymentFlow) {
        String paymentMode = EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod();
        PayMethod payMethod = getPayMethod(cashierInfoResponse, paymentMode);

        if (EPayMode.ADDANDPAY.getValue().equalsIgnoreCase(paymentFlow)) {
            LOGGER.info("getting accountInfo from addmoney pay flow : {}, payment mode:-{}", paymentFlow, paymentMode);
            payMethod = getPayMethodForAddnPay(cashierInfoResponse, paymentMode);
        }
        if (null == payMethod) {
            return null;
        }

        if (null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {
            AccountInfo accountInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
            if (null == accountInfo) {
                return null;
            } else {
                if (accountInfo.isPayerAccountExists()) {
                    return accountInfo;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public PayMethod getPayMethod(NativeCashierInfoResponse cashierInfoResponse, String ePayMethod) {
        if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()
                && null != cashierInfoResponse.getBody().getMerchantPayOption()
                && null != cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
            List<PayMethod> payMethods = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods();
            for (PayMethod payMethod : payMethods) {
                if (ePayMethod.equals(payMethod.getPayMethod())) {
                    return payMethod;
                }
            }
        }
        return null;
    }

    public PayMethod getPayMethodForAddnPay(NativeCashierInfoResponse cashierInfoResponse, String ePayMethod) {
        if (null != cashierInfoResponse.getBody().getAddMoneyPayOption()
                && null != cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods()) {
            List<PayMethod> payMethods = cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods();
            for (PayMethod payMethod : payMethods) {
                if (ePayMethod.equals(payMethod.getPayMethod())) {
                    return payMethod;
                }
            }
        }
        return null;
    }

    private boolean isVisaSingleClickPayment(HttpServletRequest request) {

        String oneClickInfo = request.getParameter("oneClickInfo");
        try {
            OneClickInfo oneClickInfoObj = JsonMapper.mapJsonToObject(oneClickInfo, OneClickInfo.class);
            String paymode = oneClickInfoObj.getType();
            if (StringUtils.equals("ONE_CLICK_PAY", paymode)) {
                LOGGER.info("VISA Single click request received - ONE_CLICK_PAY");
                return true;
            } else if (StringUtils.equals("ONE_CLICK_ENROLL", paymode)) {
                LOGGER.info("VISA Single click request received - ONE_CLICK_ENROLL");
            }

        } catch (FacadeCheckedException e) {
            LOGGER.info("Could not map oneCLickInfo Object");
        }

        return false;
    }

    private boolean isEncryptedCardDetails(String cardInfo) {

        String[] split = cardInfo.trim().split("\\$");
        return (split.length == ETM_ENCRYPTION_CONSTANT);
    }

    private boolean isMaestroCardScheme(String cardBin) {
        BinDetail binDetail = null;
        try {
            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (MAESTRO.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    private boolean isBajajCardScheme(String cardBin) {
        BinDetail binDetail = null;
        try {

            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (CashierConstant.BAJAJ_CARD.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    private boolean isBajajFnScheme(String cardBin) {
        BinDetail binDetail = null;
        try {
            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (CashierConstant.BAJAJFN.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    private boolean isPaymentFlowEnable(String paymentFlow, EPayMode paymentFlowExpected) {

        if (paymentFlow.equals(paymentFlowExpected.getValue())) {
            return true;
        }
        if (paymentFlow.equals(EPayMode.NONE.getValue())) {
            return true;
        }
        return false;
    }

    private boolean isPaymentFlowEnableForPayMethod(String paymentFlow, String payMethod) {

        // TODO: Currently supporting only None/Hybrid flow in COD , fix this to
        // support other flows
        // ToDo: Currently supporting only NONE flow in GIFT_VOUCHER(MGV)
        if (StringUtils.equalsIgnoreCase(payMethod, EPayMethod.MP_COD.getOldName())
                || StringUtils.equalsIgnoreCase(payMethod, EPayMethod.MP_COD.getMethod())) {

            if (StringUtils.isNotBlank(paymentFlow)
                    && !StringUtils.equalsIgnoreCase(paymentFlow, EPayMode.HYBRID.getValue())
                    && !StringUtils.equalsIgnoreCase(paymentFlow, EPayMode.NONE.getValue())) {
                return false;
            }

        } else if (StringUtils.equalsIgnoreCase(payMethod, EPayMethod.GIFT_VOUCHER.getMethod())) {
            if (StringUtils.isNotBlank(paymentFlow)
                    && !StringUtils.equalsIgnoreCase(paymentFlow, EPayMode.NONE.getValue())) {
                return false;
            }
        }

        return true;
    }

    public String getWalletBalanceFromLitePayViewResponse(NativeCashierInfoResponse nativeCashierInfoResponse) {
        String walletBalance = null;
        PayOption merchantPayOption = nativeCashierInfoResponse.getBody().getMerchantPayOption();
        if (merchantPayOption == null) {
            LOGGER.info("MerchantPayOption fetched as null");
            return TheiaConstant.ExtraConstants.ZERO;
        }
        List<PayMethod> payMethods = nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods();
        if (payMethods == null) {
            LOGGER.info("PayOptions fetched as null");
            return TheiaConstant.ExtraConstants.ZERO;
        }
        for (PayMethod payMethod : payMethods) {
            if (payMethod.getPayMethod().equals(com.paytm.pgplus.common.enums.PayMethod.BALANCE.toString())
                    && !payMethod.getIsDisabled().getStatus().equals(TheiaConstant.ExtraConstants.TRUE)) {
                List<PayChannelBase> payChannelBaseList = payMethod.getPayChannelOptions();

                BalanceChannel balanceChannel = (BalanceChannel) payChannelBaseList.get(0);
                walletBalance = balanceChannel.getBalanceInfo().getAccountBalance().getValue();
                break;
            }
        }
        if (StringUtils.isNotBlank(walletBalance)) {
            return walletBalance;
        }
        return TheiaConstant.ExtraConstants.ZERO;
    }

    public Boolean isPostPaidMPinRequired(String txnToken) {

        Boolean isPasscodeRequired = nativeSessionUtil.getPostPaidMPinRequired(txnToken);
        if (isPasscodeRequired != null) {
            return isPasscodeRequired;
        }
        // Returning the default value as false - To cater default cases, when
        // the
        // postpaid balance is fetched during FPO and isPasscodeRequired in not
        // set in redis.
        return false;
    }

    private void setNativeTxnInProcessFlagInCache(String txnToken) {
        nativeSessionUtil.setNativeTxnInProcessFlag(txnToken, true);
    }

    public boolean isPayMethodAllowed(String paymentMethod, String txnTkn, String isAddMoney,
            boolean blockNonCCDCPaymodes, String paymentFlowExpectedNative, String mid) {

        // String requestedPayMethod = requestData.getPaymentTypeId();
        // String txnToken = requestData.getTxnToken();
        if (txnTkn != null && paymentMethod != null) {
            if (blockNonCCDCPaymodes
                    && !((EPayMethod.CREDIT_CARD.getOldName().equals(paymentMethod)
                            || EPayMethod.DEBIT_CARD.getOldName().equals(paymentMethod) || EPayMethod.UPI.getOldName()
                            .equals(paymentMethod)))) {
                return false;
            }

            if ("1".equals(isAddMoney) && EPayMode.ADDANDPAY.getValue().equals(paymentFlowExpectedNative)
                    && (EPayMethod.CREDIT_CARD.getOldName()).equals(paymentMethod)) {
                if (ff4jUtils.isFeatureEnabledOnMid(mid, TheiaConstant.FF4J.REMOVE_ADDNPAY_CC_PAYMODE, false)) {
                    LOGGER.info("Disabling ADDNPAY CC paymode as ff4j feature is enabled");
                    return false;
                }
            }

            NativeCashierInfoResponse cashierInfoResponse = null;
            cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnTkn);
            if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()) {
                if (null != isAddMoney && TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(isAddMoney)
                        && null != cashierInfoResponse.getBody().getAddMoneyPayOption()
                        && null != cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods()) {
                    // Do not check merchant pay options, rather check add money
                    // payoptions
                    return isPaymethoAllowedInList(paymentMethod, cashierInfoResponse.getBody().getAddMoneyPayOption()
                            .getPayMethods());
                } else if (null != cashierInfoResponse.getBody().getMerchantPayOption()
                        && null != cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
                    return isPaymethoAllowedInList(paymentMethod, cashierInfoResponse.getBody().getMerchantPayOption()
                            .getPayMethods());
                }
            }
        }
        LOGGER.info("{} not allowed as pay method not found in cashier info response", paymentMethod);
        return false;
    }

    private boolean isPaymethoAllowedInList(String paymentMethod, List<PayMethod> payMethods) {
        for (PayMethod payMethod : payMethods) {

            if (StringUtils.equalsIgnoreCase(paymentMethod, payMethod.getPayMethod())) {
                return true;
            }
            EPayMethod ePayMethod = EPayMethod.getPayMethodByOldName(paymentMethod);
            if (ePayMethod != null && StringUtils.equalsIgnoreCase(ePayMethod.getMethod(), payMethod.getPayMethod())) {
                return true;
            }
            ePayMethod = EPayMethod.getPayMethodByMethod(paymentMethod);
            if (ePayMethod != null && StringUtils.equalsIgnoreCase(ePayMethod.getMethod(), payMethod.getPayMethod())) {
                return true;
            }

            if (ePayMethod != null && StringUtils.equalsIgnoreCase(ePayMethod.getOldName(), payMethod.getPayMethod())) {
                return true;
            }
        }
        return false;
    }

    NativeCashierInfoResponse callFetchPayOptions(String txnToken, String channel, String version,
            InitiateTransactionRequestBody orderDetail) throws Exception {
        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(txnToken, channel, version);
        nativePaymentUtil.fetchPaymentOptions(tokenRequestHeader, orderDetail, null);
        return nativeSessionUtil.getCashierInfoResponse(txnToken);
    }

    public TokenRequestHeader getTokenRequestHeader(String txnToken, String channel, String version) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setVersion(version);
        tokenRequestHeader.setRequestTimestamp(Long.toString(System.currentTimeMillis()));
        tokenRequestHeader.setTxnToken(txnToken);
        if (StringUtils.isNotBlank(channel)) {
            tokenRequestHeader.setChannelId(EChannelId.valueOf(channel.toUpperCase()));
        } else {
            tokenRequestHeader.setChannelId(EChannelId.WEB);
        }
        return tokenRequestHeader;

    }

    private BalanceInfoResponse fetchBalance(String payMethod, String txnToken, HttpServletRequest request)
            throws Exception {
        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(txnToken,
                request.getParameter(Native.CHANNEL_ID), TheiaConstant.RequestHeaders.Version_V1);
        if (TokenType.SSO.getType().equals(request.getParameter(Native.TOKEN_TYPE))) {
            tokenRequestHeader.setTokenType(TokenType.SSO);
            tokenRequestHeader.setToken(request.getParameter(SSO_TOKEN));
        }
        return nativePaymentUtil.fetchBalance(tokenRequestHeader, payMethod, request.getParameter(Native.MID));
    }

    private boolean isPaymentAllowedForEMIWithTxnAmt(final HttpServletRequest request,
            final NativeCashierInfoResponse cashierInfoResponse, final Money txnAmount, final String planId,
            final String emiType, final boolean isEnhancedNativeFlow, String cardInfo) {
        if (cashierInfoResponse != null && StringUtils.isNotBlank(planId)) {
            PayOption merchantPayOption = cashierInfoResponse.getBody().getMerchantPayOption();
            EXT_LOGGER.customInfo("isPaymentAllowedForEMIWithTxnAmt {} {} {} {} {} {}", merchantPayOption,
                    cashierInfoResponse.getBody().getPaymentFlow(), txnAmount,
                    (cardInfo != null && cardInfo.length() > 6) ? cardInfo.substring(0, 6) : "", emiType, planId);
            List<PayMethod> payMethods = merchantPayOption.getPayMethods();
            if (payMethods != null && !payMethods.isEmpty()) {
                for (PayMethod payMethod : payMethods) {
                    if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                        List<PayChannelBase> channels = payMethod.getPayChannelOptions();
                        if (channels != null && !channels.isEmpty()) {
                            for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                                EmiChannel emiChannel = (EmiChannel) payChannelBase;
                                // confirmed. skip for channels disabled due to
                                // CHANNEL_NTOT_AVAILABLE reason
                                if (emiChannel.getIsDisabled() != null
                                        && TheiaConstant.ExtraConstants.TRUE.equals(emiChannel.getIsDisabled()
                                                .getStatus())
                                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason()
                                                .equals(emiChannel.getIsDisabled().getMsg())) {
                                    continue;
                                }
                                if (StringUtils.isBlank(emiType) || (emiChannel.getEmiType().getType().equals(emiType))) {
                                    for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                                        if (planId.equals(emiChannelInfo.getPlanId())
                                                && isCardValidForEMIType(cardInfo, emiChannel.getInstId(), emiChannel
                                                        .getEmiType().getType())) {
                                            EPayMode payMode = cashierInfoResponse.getBody().getPaymentFlow();
                                            Money minAmountOfMatchedPlan = emiChannelInfo.getMinAmount();
                                            Money maxAmountOfMatchedPlan = emiChannelInfo.getMaxAmount();
                                            double minAmount = Double.parseDouble(minAmountOfMatchedPlan.getValue());
                                            double transactionAmount = Double.parseDouble(txnAmount.getValue());
                                            double maxAmount = Double.parseDouble(maxAmountOfMatchedPlan.getValue());

                                            String mincurrencyOfMatchedPlan = minAmountOfMatchedPlan.getCurrency()
                                                    .getCurrency();
                                            String maxcurrencyOfMatchedPlan = maxAmountOfMatchedPlan.getCurrency()
                                                    .getCurrency();
                                            String currencyOfTxn = txnAmount.getCurrency().getCurrency();

                                            if (payMode != null && EPayMode.HYBRID.equals(payMode)) {
                                                WalletInfo walletInfo = theiaSessionDataServiceAdapterNative
                                                        .getWalletInfoFromSession(request);
                                                transactionAmount = transactionAmount - walletInfo.getWalletBalance();
                                            }
                                            if ((transactionAmount >= minAmount)
                                                    && (mincurrencyOfMatchedPlan.equals(currencyOfTxn))
                                                    && ((transactionAmount <= maxAmount) && (maxcurrencyOfMatchedPlan
                                                            .equals(currencyOfTxn)))) {
                                                return true;
                                            }
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        LOGGER.info("returning eom false");
        return false;
    }

    public void checkIfDuplicatePaymentRequest(HttpServletRequest request) {
        try {
            String key = getDuplicateRequestKey(request);
            Object haltDuplicatePayRequest = nativeSessionUtil.getKey(key);
            String ttl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.DUPLICATE_PAYMENT_REDIS_TTL, "2");
            if (haltDuplicatePayRequest == null) {
                nativeSessionUtil.setKey(key, true, Long.valueOf(ttl));
            } else {
                LOGGER.error(BizConstant.FailureLogs.DUPLICATE_PAYMENT_REQUEST);
                failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.DUPLICATE_PAYMENT_REQUEST, null,
                        true);
                throw DuplicatePaymentRequestException.getException();
            }

        } catch (BaseException e) {
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getResultCodeId()
                    : null, e.getMessage(), null, true);

            throw e;
        } catch (Exception e) {
            LOGGER.warn("Exception occured while checking for duplicate payment request");
        }
    }

    public void removeRedisKeyToAllowPayment(HttpServletRequest request) {
        try {
            nativeSessionUtil.deleteKey(getDuplicateRequestKey(request));
        } catch (Exception e) {
            LOGGER.error("Error in removing redis key");
        }
    }

    private String getDuplicateRequestKey(HttpServletRequest request) throws Exception {
        String mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        String orderId = request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) {
            throw new Exception("order id or mid empty");
        }
        return new StringBuilder().append(TheiaConstant.ExtraConstants.HALT_PAYMENT_REQUEST).append(mid).append("_")
                .append(orderId).append("_").toString();
    }

    public boolean isMaxPaymentRetryBreached(HttpServletRequest request) {
        try {
            int maxPaymentCount = Integer.valueOf(ConfigurationUtil.getProperty(MAX_PAYMENT_COUNT, "10"));
            String txntoken = (String) request.getAttribute(Native.TXN_TOKEN);
            if (StringUtils.isNotBlank(txntoken)) {
                Integer totalPaymentCount = nativeSessionUtil.getTotalPaymenCount(txntoken);
                if (totalPaymentCount == null) {
                    totalPaymentCount = 0;
                }
                totalPaymentCount = totalPaymentCount + 1;
                if (totalPaymentCount > maxPaymentCount) {
                    return true;
                }
                nativeSessionUtil.setTotalPaymentCount(txntoken, totalPaymentCount);
            }
        } catch (Exception e) {
            LOGGER.warn("something went wrong while checking isMaxPaymentRetryBreached");
        }
        return false;
    }

    private boolean checkIfMerchantHasPreference(String mid) {
        return merchantPreferenceService.isNativeJsonRequestSupported(mid);
        // if (!merchantPreferenceService.isNativeJsonRequestSupported(mid)) {
        // LOGGER.error("Merchant does not have preference nativeJsonRequest, failing txn!");
        // throw new
        // NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(false).build();
        //
        // }
    }

    public boolean isNativeKycFlow(HttpServletRequest request) {
        return StringUtils.isNotBlank(request.getParameter(KYC_FLOW))
                && request.getParameter(KYC_FLOW).equalsIgnoreCase("YES");
    }

    private NativeKYCDetailResponse doKycInNative(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOGGER.info("Request received for native kyc");
        NativeKYCDetailRequest nativeKYCDetailRequest = new NativeKYCDetailRequest();
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setTxnToken(request.getParameter(KYC_TXN_ID));
        String kycNameOnDoc = request.getParameter(KYC_NAME_ON_DOC);
        String kycDocCode = request.getParameter(KYC_DOC_CODE);
        String kycDocValue = request.getParameter(KYC_DOC_VALUE);
        NativeKYCDetailRequestBody nativeKYCDetailRequestBody = new NativeKYCDetailRequestBody(kycNameOnDoc,
                kycDocCode, kycDocValue);
        nativeKYCDetailRequest.setHead(tokenRequestHeader);
        nativeKYCDetailRequest.setBody(nativeKYCDetailRequestBody);
        IRequestProcessor<NativeKYCDetailRequest, NativeKYCDetailResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_KYC_REQUEST);
        NativeKYCDetailResponse nativeKYCDetailResponse = requestProcessor.process(nativeKYCDetailRequest);
        String kycKey = "Native_KYC_" + request.getParameter(KYC_TXN_ID);
        Map<String, String[]> additionalParams = (Map<String, String[]>) nativeSessionUtil.getKey(kycKey);
        if (additionalParams == null) {
            throw new TheiaServiceException("no data in cache for kyc in native");
        }
        if (!nativeKYCDetailResponse.getBody().isKycSuccessful()) {
            if (nativeKYCDetailResponse.getBody().getKycRetryCount() < 0) {
                PaymentRequestBean paymentRequestData = new PaymentRequestBean(new ModifiableHttpServletRequest(
                        request, additionalParams));
                LOGGER.info("Retry limit reached for KYC on boarding flow");
                String html = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE);
                response.getOutputStream().print(html);
                response.setContentType("text/html");
            } else {
                prepareKycRetryData(additionalParams, request, nativeKYCDetailResponse);
                loadNativeKycPage(request, response);
            }
        }
        return nativeKYCDetailResponse;
    }

    private void prepareKycRetryData(Map<String, String[]> additionalParams, HttpServletRequest request,
            NativeKYCDetailResponse nativeKYCDetailResponse) {

        request.setAttribute(KYC_TXN_ID, additionalParams.get(Native.TXN_TOKEN)[0]);
        request.setAttribute(KYC_FLOW, "YES");
        request.setAttribute(KYC_MID, additionalParams.get(TheiaConstant.RequestParams.MID)[0]);
        request.setAttribute(KYC_ORDER_ID, additionalParams.get(TheiaConstant.RequestParams.ORDER_ID)[0]);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("mid=").append(additionalParams.get(TheiaConstant.RequestParams.MID)[0]);
        queryBuilder.append("&orderId=").append(additionalParams.get(TheiaConstant.RequestParams.ORDER_ID)[0]);
        request.setAttribute(KYC_ERROR_MESSAGE, nativeKYCDetailResponse.getBody().getKycErrorMsg());
        request.setAttribute(KYC_RETRY_COUNT, nativeKYCDetailResponse.getBody().getKycRetryCount());
        request.setAttribute("queryStringForSession", queryBuilder.toString());
    }

    private void loadNativeKycPage(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        LOGGER.info("Loading kyc page for native request");
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnNativeKycPage() + ".jsp").forward(
                request, response);
    }

    public boolean processNativeKycFlow(HttpServletRequest request, HttpServletResponse response,
            Map<String, String[]> additionalParams) throws Exception {

        Map<String, String[]> params = null;

        NativeKYCDetailResponse nativeKYCDetailResponse = doKycInNative(request, response);
        if (!nativeKYCDetailResponse.getBody().isKycSuccessful()) {
            return false;
        }
        String txnToken = request.getParameter(KYC_TXN_ID);
        String kycKey = "Native_KYC_" + txnToken;
        params = (Map<String, String[]>) nativeSessionUtil.getKey(kycKey);
        if (params == null) {
            failureLogUtil.setFailureMsgForDwhPush(null,
                    BizConstant.FailureLogs.NO_DATA_IN_CACHE_AFTER_NATIVE_KYC_COMPLETION, null, true);

            throw new TheiaServiceException(BizConstant.FailureLogs.NO_DATA_IN_CACHE_AFTER_NATIVE_KYC_COMPLETION);
        }
        // should work as redirection
        params.remove(NATIVE_JSON_REQUEST);

        request.setAttribute(Native.TXN_TOKEN, txnToken);

        additionalParams.putAll(params);
        return true;
    }

    private NativeInitiateRequest createNativeInitiateRequestForSsoFLow(HttpServletRequest request) throws Exception {
        if (!(AOA_WORKFLOW).equals(request.getParameter(Native.WORKFLOW))) {
            nativeSessionUtil.validateMidSSOBasedTxnToken(request.getParameter(Native.TXN_TOKEN),
                    request.getParameter(SSO_TOKEN));
        }
        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequestBody.setMid(request.getParameter(TheiaConstant.RequestParams.Native.MID));
        initiateTransactionRequestBody.setOrderId(request.getParameter(Native.ORDER_ID));
        Money txnAmount = new Money(EnumCurrency.getEnumByCurrency(request.getParameter(CURRENCY)),
                request.getParameter(TXN_AMOUNT));
        initiateTransactionRequestBody.setTxnAmount(txnAmount);
        Money tipAmount = new Money(EnumCurrency.getEnumByCurrency(request.getParameter(CURRENCY)),
                request.getParameter(TIP_AMOUNT));
        initiateTransactionRequestBody.setTipAmount(tipAmount);
        String extendInfo = request.getParameter(Native.EXTEND_INFO);
        initiateTransactionRequestBody.setExtendInfo(getExtendInfo(request, extendInfo));
        initiateTransactionRequestBody.getExtendInfo().setComments(
                JsonMapper.getStringParamFromJson(extendInfo, TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO));
        initiateTransactionRequestBody.setCallbackUrl(request.getParameter(CALLBACK_URL));
        initiateTransactionRequestBody.setPaytmSsoToken(request.getParameter(SSO_TOKEN));
        initiateTransactionRequestBody.setWebsiteName(request.getParameter(WEBSITE));
        if (StringUtils.isNotBlank(request.getParameter(CUSTOMER_ID))) {
            UserInfo userInfo = new UserInfo();
            userInfo.setCustId(request.getParameter(CUSTOMER_ID));
            initiateTransactionRequestBody.setUserInfo(userInfo);
        }
        StringBuilder midOrderIdKey = new StringBuilder(NATIVE_TXN_INITIATE_REQUEST);
        midOrderIdKey.append(request.getParameter(TheiaConstant.RequestParams.Native.MID)).append("_")
                .append(request.getParameter(Native.ORDER_ID));

        // to support v1/transactionStatus API for DynamicQR Code flow
        String initiateTransId = (String) nativeSessionUtil.getKey(midOrderIdKey.toString());
        if (StringUtils.isNotBlank(initiateTransId)) {
            request.setAttribute(INITIATE_TRANS_ID, initiateTransId);
        }

        nativeSessionUtil.setKey(midOrderIdKey.toString(), request.getParameter(Native.TXN_TOKEN), 900);
        if (!((AOA_WORKFLOW).equals(request.getParameter(Native.WORKFLOW)))) {
            nativeSessionUtil.setOrderDetail(request.getParameter(Native.TXN_TOKEN), initiateTransactionRequestBody);
        } else {
            nativeSessionUtil.setOrderDetail(request.getParameter(Native.TXN_TOKEN), initiateTransactionRequestBody,
                    900);
        }
        return new NativeInitiateRequest(initiateTransactionRequestBody);
    }

    public boolean isNativeJsonRequest(PaymentRequestBean paymentRequestBean) {
        return (paymentRequestBean != null) && (paymentRequestBean.getRequest() != null)
                && isNativeJsonRequest(paymentRequestBean.getRequest());
    }

    public void setS2SResponseForNativeJsonRequest(PaymentRequestBean paymentRequestBean,
            PageDetailsResponse processed, PageDetailsResponse pageDetailsResponse) {
        if (StringUtils.isNotBlank(processed.getS2sResponse())) {
            pageDetailsResponse.setS2sResponse(processed.getS2sResponse());
            pageDetailsResponse.setSuccessfullyProcessed(true);
        } else {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                    .isRedirectEnhanceFlow(true).build();
        }
    }

    public void pushNativeEnhancedEvent(String mid, String orderId, String eventMsg, String paymentMode) {
        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        metaData.put("mid", mid);
        metaData.put("orderId", orderId);
        metaData.put("eventMsg", eventMsg);
        if (StringUtils.isNotBlank(paymentMode)) {
            metaData.put("paymentMode", paymentMode);
        }
        EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.NATIVE_ENHANCED, metaData);
    }

    public void pushNativePaymentEvent(String mid, String orderId, String eventMsg) {
        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        metaData.put("mid", mid);
        metaData.put("orderId", orderId);
        metaData.put("eventMsg", eventMsg);
        EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.NATIVE, metaData);
    }

    public void pushNativeJsonRequestEvent(Long time, NativeJsonRequest nativeJsonRequest) {

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        if (nativeJsonRequest.getBody() != null) {
            metaData.put("mid", StringUtils.defaultString(nativeJsonRequest.getBody().getMid()));
            metaData.put("orderId", StringUtils.defaultString(nativeJsonRequest.getBody().getOrderId()));
            metaData.put("paymentMode", StringUtils.defaultString(nativeJsonRequest.getBody().getPaymentMode()));
            metaData.put("channelCode", StringUtils.defaultString(nativeJsonRequest.getBody().getChannelCode()));
            metaData.put("totalTimeTaken", StringUtils.defaultString(time.toString()));
            metaData.put("timeUnit", TimeUnit.MILLISECONDS.toString());
            EventUtils.pushTheiaEvents(nativeJsonRequest.getBody().getMid(), nativeJsonRequest.getBody().getOrderId(),
                    EventNameEnum.NATIVE_JSON_RESPONSE_TIME, metaData);
        }
    }

    public void pushFetchPaymentOptionsEvent(EventNameEnum eventNameEnum) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String api = servletRequest.getRequestURI();

        String mid = MDC.get(TheiaConstant.RequestParams.MID);
        String orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        metaData.put("api", StringUtils.defaultString(api));

        EventUtils.pushTheiaEvents(mid, orderId, eventNameEnum, metaData);
    }

    public void pushPaymentEvent(EventNameEnum eventNameEnum, String paymentMode) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String api = servletRequest.getRequestURI();
        String requestId = MDC.get(TheiaConstant.RequestParams.REQUEST_ID);
        String orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        metaData.put("api", StringUtils.defaultString(api));
        metaData.put("paymentMode", StringUtils.defaultString(paymentMode));
        metaData.put(Native.REQUEST_ID, requestId);
        metaData.put(Native.ORDER_ID, orderId);

        EventUtils.pushTheiaEvents(eventNameEnum, metaData);
    }

    public void pushV1PtcRequestEvent(HttpServletRequest request) {

        String mid = request.getParameter(Native.MID);
        String orderId = request.getParameter(Native.ORDER_ID);
        if (StringUtils.isBlank(orderId)) {
            orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);
        }
        String api = request.getRequestURI();
        String paymentMode = request.getParameter(Native.PAYMENT_MODE);
        String requestId = request.getParameter(Native.REQUEST_ID);
        if (StringUtils.isBlank(requestId)) {
            requestId = (String) request.getAttribute(Native.REQUEST_ID);
        }

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        metaData.put("api", StringUtils.defaultString(api));
        metaData.put("paymentMode", StringUtils.defaultString(paymentMode));
        metaData.put(Native.REQUEST_ID, requestId);
        metaData.put(Native.ORDER_ID, orderId);

        if (isEnhancedNativeFlow(request)) {
            EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.ONLINE_ENHANCED_PAYMENT_REQUEST, metaData);
            return;
        }

        if (isQRCodeRequest(request)) {
            boolean isRequestFromAppCache = BooleanUtils.isTrue((Boolean) request
                    .getAttribute(Native.IS_ORDER_ID_NEED_TO_BE_GENERATED));
            EventUtils.pushTheiaEvents(mid, orderId,
                    isRequestFromAppCache ? EventNameEnum.OFFLINE_PAYMENT_REQUEST_CACHED
                            : EventNameEnum.OFFLINE_PAYMENT_REQUEST, metaData);
            return;
        }

        EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.ONLINE_NATIVE_AND_NATIVEPLUS_PAYMENT_REQUEST, metaData);

        // removed !isOfflinePaymentRequestV1Ptc(request) condition becuase that
        // will be true always here
        if (isNativeJsonRequest(request)) {
            EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.ONLINE_NATIVEPLUS_PAYMENT_REQUEST, metaData);
            return;
        }

        EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.ONLINE_NATIVE_PAYMENT_REQUEST, metaData);
    }

    public boolean isOfflinePaymentRequestV1Ptc(HttpServletRequest request) {

        String extendInfo = request.getParameter(Native.EXTEND_INFO);
        if (StringUtils.isNotBlank(extendInfo)
                && StringUtils.containsIgnoreCase(extendInfo, "REQUEST_TYPE:QR_MERCHANT")) {
            return true;
        }

        return false;
    }

    private boolean isQRCodeRequest(HttpServletRequest request) {
        String extendInfo = request.getParameter(Native.EXTEND_INFO);

        if (StringUtils.isNotBlank(extendInfo)) {
            String[] additionalInfoKeyValArray = extendInfo.split(Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String keyVal : additionalInfoKeyValArray) {
                if (keyVal.contains(TheiaConstant.RequestParams.REQUEST_TYPE)) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2
                            && (BizConstant.REQUEST_QR_ORDER.equals(keyValSplit[1])
                                    || BizConstant.REQUEST_QR_MERCHANT.equals(keyValSplit[1])
                                    || BizConstant.UPI_QR_CODE.equals(keyValSplit[1]) || BizConstant.UPI_POS_ORDER
                                        .equals(keyValSplit[1]))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void pushNativeJsonRequestEvent(NativeJsonRequest nativeJsonRequest) {

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
        if (nativeJsonRequest.getBody() != null) {
            metaData.put("mid", StringUtils.defaultString(nativeJsonRequest.getBody().getMid()));
            metaData.put("orderId", StringUtils.defaultString(nativeJsonRequest.getBody().getOrderId()));
            metaData.put("paymentMode", StringUtils.defaultString(nativeJsonRequest.getBody().getPaymentMode()));
            metaData.put("channelCode", StringUtils.defaultString(nativeJsonRequest.getBody().getChannelCode()));
            metaData.put("paymentFlow", StringUtils.defaultString(nativeJsonRequest.getBody().getPaymentFlow()));
            EventUtils.pushTheiaEvents(nativeJsonRequest.getBody().getMid(), nativeJsonRequest.getBody().getOrderId(),
                    EventNameEnum.NATIVE_JSON_REQUEST_INITIATED, metaData);
        }
    }

    public void checkAndGenerateOrderIdIfNeeded(HttpServletRequest request, NativeJsonRequest nativeJsonRequest)
            throws Exception {

        Boolean isOrderIdNeedToBeGenarated = (Boolean) request.getAttribute(Native.IS_ORDER_ID_NEED_TO_BE_GENERATED);
        if (isOrderIdNeedToBeGenarated != null && isOrderIdNeedToBeGenarated) {

            if (TokenType.SSO.equals(nativeJsonRequest.getHead().getTokenType())) {

                // LOGGER.info("generating orderid");
                NativeCashierInfoResponse fetchPaymentOptionsWithSsoToken = nativePaymentUtil
                        .fetchPaymentOptionsWithSsoToken(nativeJsonRequest.getHead(), nativeJsonRequest.getBody()
                                .getMid(), true, null);
                nativeJsonRequest.getBody().setOrderId(fetchPaymentOptionsWithSsoToken.getBody().getOrderId());
                LOGGER.info("orderId generated");
            }
        } else if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getQrCodeId())
                && StringUtils.isNotBlank(nativeJsonRequest.getBody().getOrderId())) {
            // This is done in case of DynamicQr flow, when qrCodeId is sent in
            // PTC and FQR is not called
            nativePaymentUtil.fetchPaymentOptionsWithSsoToken(nativeJsonRequest.getHead(), nativeJsonRequest.getBody()
                    .getMid(), false, null);
        }
        /*
         * Check PGP-16564. RefId is sent by App in case orderId is not
         * available. This refId is later user to fetch txn status in
         * merchantStatus API.
         */
        if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getRefId())) {

            if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getOrderId())) {
                String key = "REF_ID_" + nativeJsonRequest.getBody().getRefId();
                if (ff4jUtils.isFeatureEnabledOnMid(nativeJsonRequest.getBody().getMid(),
                        REF_ID_REDIS_DEPENDENCY_REMOVAL_NEW_PATTERN, false)) {
                    String newKey = key + "_ORDER_ID_MAPPING";
                    nativeSessionUtil.setRefIdOrderIdMapping(newKey, nativeJsonRequest.getBody().getOrderId(),
                            nativeJsonRequest.getBody().getMid(), nativeJsonRequest.getHead().getToken());
                }
                if (ff4jUtils.isFeatureEnabledOnMid(nativeJsonRequest.getBody().getMid(),
                        REF_ID_REDIS_DEPENDENCY_REMOVAL_OLD_PATTERN, true)) {
                    nativeSessionUtil.setKey(key, nativeJsonRequest.getBody().getOrderId(), 900);
                }
            }
        }
    }

    public void pushNativeJsonResponseEvent(TransactionResponse transactionResponse) {

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();

        if (transactionResponse != null) {

            metaData.put("mid", StringUtils.defaultString(transactionResponse.getMid()));
            metaData.put("orderId", StringUtils.defaultString(transactionResponse.getOrderId()));
            EPayMethod payMethod = EPayMethod.getPayMethodByOldName(transactionResponse.getPaymentMode());
            if (payMethod != null) {
                metaData.put("paymentMode", StringUtils.defaultString(payMethod.getMethod()));
            }
            metaData.put("bankName", StringUtils.defaultString(transactionResponse.getBankName()));
            metaData.put("gatewayName", StringUtils.defaultString(transactionResponse.getGateway()));
            metaData.put("responseCode", StringUtils.defaultString(transactionResponse.getResponseCode()));
            metaData.put("responseMsg", StringUtils.defaultString(transactionResponse.getResponseMsg()));
            metaData.put("txnStatus", StringUtils.defaultString(transactionResponse.getTransactionStatus()));
            metaData.put("txnAmount", StringUtils.defaultString(transactionResponse.getTxnAmount()));

            EventUtils.pushTheiaEvents(transactionResponse.getMid(), transactionResponse.getOrderId(),
                    EventNameEnum.NATIVE_JSON_RESPONSE, metaData);
        }
    }

    private boolean isRiskyUser(HttpServletRequest request, NativeInitiateRequest nativeInitiateRequest) {
        EnvInfoRequestBean envInfoRequestBean = EnvInfoUtil.fetchEnvInfo(request);
        String userId = null;
        UserDetailsBiz userDetails = null;
        if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
            userDetails = nativeValidationService.getUserDetails(nativeInitiateRequest.getInitiateTxnReq().getBody());
        }
        if (userDetails == null) {
            throwExceptionNativeTxn(request, ResultCode.SESSION_EXPIRED_EXCEPTION);
        }

        RiskResponse riskResponse = null;
        try {
            riskResponse = workFlowHelper.riskPolicyConsultResponse(
                    envInfoRequestBean,
                    userDetails,
                    AmountUtils.getTransactionAmountInPaise(nativeInitiateRequest.getInitiateTxnReq().getBody()
                            .getTxnAmount().getValue()), request.getParameter("orderId"));
            if (riskResponse == null) {
                return Boolean.FALSE;
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to communicate with RISK_POLICY_CONSULT API", e);
            throwExceptionNativeTxn(request, ResponseConstants.SYSTEM_ERROR);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception occured while processing json request for RISK_POLICY_CONSULT", e);
            throwExceptionNativeTxn(request, ResponseConstants.SYSTEM_ERROR);
        }
        return validateRiskResponse(riskResponse);
    }

    private void throwExceptionNativeTxn(HttpServletRequest request, ResponseConstants responseConstant) {
        if (isNativeJsonRequest(request)) {
            throw new NativeFlowException.ExceptionBuilder(responseConstant).isNativeJsonRequest(true).build();
        } else {
            throw new NativeFlowException.ExceptionBuilder(responseConstant).isHTMLResponse(true).build();
        }
    }

    private void throwRiskRejectException(String customMessage, String txnToken,
            InitiateTransactionRequestBody orderDetail) {
        nativeRetryUtil.increaseRetryCount(txnToken, orderDetail.getMid(), orderDetail.getOrderId());
        boolean isRetryAllowed = nativeRetryUtil.isRetryPossible(txnToken, orderDetail.getMid());
        throw new RiskRejectException.ExceptionBuilder().setResponseConstant(ResponseConstants.RISK_REJECT)
                .setCustomCallbackMsg(customMessage).setRetryAllowed(isRetryAllowed).build();
    }

    private void throwExceptionNativeTxn(HttpServletRequest request, ResultCode resultCode) {
        if (isNativeJsonRequest(request)) {
            throw new NativeFlowException.ExceptionBuilder(resultCode).isNativeJsonRequest(true).build();
        } else {
            throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(true).build();
        }
    }

    private boolean validateRiskResponse(RiskResponse riskResponse) {
        if (riskResponse.getBody() != null && RiskResultEnum.REJECT == riskResponse.getBody().getRiskResult()) {
            LOGGER.info("Risk applied on the provided field");
            return true;
        }
        return false;
    }

    private boolean isRiskFeeValid(String payMethod, RiskFeeDetails riskFeeDetails, String feePercentFromWallet) {
        String riskFeePercent = null;
        if (feePercentFromWallet == null) {
            riskFeePercent = ConfigurationUtil
                    .getProperty(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RiskConsultConstants.RISK_CONSULT_FEE_PERCENT
                            .replace(
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RiskConsultConstants.PAYMODE_PLACEHOLDER,
                                    payMethod));
        } else {
            riskFeePercent = feePercentFromWallet;
            if (StringUtils.isBlank(riskFeePercent)) {
                LOGGER.error("Unable to find risk fee percent for payMethod {} ", payMethod);
                return false;
            }
        }
        String riskFeeFromConfiguration = null;
        String riskFeeFromPayRequest = null;
        if (riskFeeDetails != null) {
            riskFeeFromConfiguration = decimalFormat.format(Double
                    .valueOf(riskFeeDetails.getInitialAmount().getValue()) * (Double.valueOf(riskFeePercent) / 100));
            riskFeeFromPayRequest = riskFeeDetails.getFeeAmount().getValue();
        }
        if (riskFeeDetails == null
                || Double.compare(Double.valueOf(riskFeeFromPayRequest), Double.valueOf(riskFeeFromConfiguration)) != 0) {
            return false;
        }
        return true;
    }

    private boolean isTxnAmountValid(NativeInitiateRequest nativeInitiateRequest, RiskFeeDetails riskFeeDetails) {
        Double txnAmount = Double
                .valueOf(nativeInitiateRequest.getInitiateTxnReq().getBody().getTxnAmount().getValue());
        if (riskFeeDetails != null
                && Double.compare(
                        txnAmount,
                        Double.valueOf(riskFeeDetails.getInitialAmount().getValue())
                                + Double.valueOf(riskFeeDetails.getFeeAmount().getValue())) != 0) {
            return false;
        }
        return true;
    }

    private boolean isValidEmiPlanEnteredInNative(InitiateTransactionRequestBody request, String emiPlanId) {

        String emiId = request.getEmiId();
        if (StringUtils.isNotBlank(emiId)) {
            EmiDetailRequest emiDetailRequest = new EmiDetailRequest();
            emiDetailRequest.setMid(request.getMid());
            emiDetailRequest.setStatus(Boolean.TRUE);
            emiDetailRequest.setChannelType(ChannelType.WAP);

            try {
                EMIDetailList emiDetailList = workFlowHelper.fetchEmiDetailsList(emiDetailRequest);
                if (null == emiDetailList
                        || org.springframework.util.CollectionUtils.isEmpty(emiDetailList.getEmiDetails())) {
                    LOGGER.error("Got empty emi response from mapping service hence emi not configured on mid ");
                    return false;
                }

                EMIDetails emiDetails = emiDetailList.getEmiDetails().get(Long.valueOf(emiId));

                if (null == emiDetails) {
                    LOGGER.error("emiId mapping does not exist on mapping-service");
                    return false;
                }
                String bankCode = StringUtils.substringBefore(emiPlanId, "|");
                String emiMonth = StringUtils.substringAfter(emiPlanId, "|");

                if (PaymentMode.EMI_DC.getMode().equals(emiDetails.getEmiType())) {
                    /*
                     * for emi dc bank code in mapping is different from instId
                     * at alipay
                     */
                    bankCode = com.paytm.pgplus.theia.utils.ConfigurationUtil.getMessageProperty("emi.dc.bank.code."
                            + bankCode, bankCode);
                }

                if (StringUtils.equals(emiDetails.getBankCode(), bankCode)
                        && StringUtils.equals(String.valueOf(emiDetails.getMonth()), emiMonth)) {
                    return true;
                } else {
                    LOGGER.error(
                            "Emi plan details passed in initiate transaction request and pay call mismatch emiDetails_bankCode :{}, bankCode :{},  emiDetails_months :{}, emiMonth :{}",
                            emiDetails.getBankCode(), bankCode, String.valueOf(emiDetails.getMonth()), emiMonth);
                    return false;
                }
            } catch (MappingServiceClientException e) {
                throw new TheiaServiceException("Exception occurred while fetching merchant data from mapping service");
            } catch (Exception e) {
                LOGGER.error("Exception occurred while fetching merchant data from merchant center service : {}", e);
                throw new TheiaServiceException(
                        "Exception occurred while fetching merchant data from merchant center service");
            }

        }
        return true;
    }

    private boolean ifAddMoneyInitiatedForSubWallet(InitiateTransactionRequestBody initiateTransactionRequestBody,
            String pid) {
        if (initiateTransactionRequestBody.getGoods() != null
                && initiateTransactionRequestBody.getGoods().get(0) != null) {
            GoodsInfo goodsInfo = initiateTransactionRequestBody.getGoods().get(0);
            String pId = goodsInfo.getMerchantGoodsId();
            if (StringUtils.isNotBlank(pId) && pId.equals(pid)) {
                LOGGER.info("Adding Money to subWallet, pid {} ", pid);
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public void cacheHostForOldPG(HttpServletRequest httpServletRequest, PaymentRequestBean paymentRequestBean) {
        try {
            if (paymentRequestBean != null) {
                String mid = paymentRequestBean.getMid();
                String orderId = paymentRequestBean.getOrderId();
                String host = httpServletRequest.getHeader(HttpHeaders.HOST);
                String oldPGHost = com.paytm.pgplus.theia.utils.ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.OLD_PG_BASE_URL_SKIP);
                if (host != null && host.contains(oldPGHost)) {
                    nativeSessionUtil.setHostForOldPgRequest(mid, orderId, oldPGHost);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("seomthing unexpected happended whille caching request ", e);
        }
    }

    public void setAdditionalParamsInSession(HttpServletRequest request, Map<String, String[]> additionalParams) {
        additionalParams.remove(NATIVE_JSON_REQUEST);
        additionalParams.remove(ENHANCED_CASHIER_PAYMENT_REQUEST);
        if (isEnhancedNativeFlow(request)) {
            additionalParams.put(IS_RISK_VERIFIED_ENHANCE_FLOW, new String[] { RiskConstants.TRUE });
        }
        String key = RiskConstants.RISK_VERIFICATION_KEY_PREFIX + request.getAttribute(RiskConstants.TRANS_ID);
        nativeSessionUtil.setKey(key, additionalParams, 900);
    }

    private String getChannelId(HttpServletRequest request) {

        String merchantRequestedChannelId = request.getParameter(Native.CHANNEL_ID);
        String userAgent = request.getHeader(TheiaConstant.RequestHeaders.USER_AGENT);

        LOGGER.info("merchantRequestedChannelId:{} , userAgent is : {}", merchantRequestedChannelId, userAgent);

        String mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        if (isMidAllowedForForcedChannelId(mid)) {
            if (StringUtils.equals(EChannelId.WEB.getValue(), merchantRequestedChannelId)
                    || StringUtils.equals(EChannelId.WAP.getValue(), merchantRequestedChannelId)) {
                // LOGGER.info("using merchantRequestedChannelId as it matches");
                return merchantRequestedChannelId;
            }
            LOGGER.info("using WAP, as merchant has sent something other channelId");
            return EChannelId.WAP.getValue();
        }

        if (isEnhancedNativeFlow(request)) {
            /*
             * channelId is already set based on user-agent when hit on v1/ptc
             */
            LOGGER.info("Enhanced Native | payment call | channelId:{}", merchantRequestedChannelId);
            return merchantRequestedChannelId;
        }

        if (EChannelId.APP.getValue().equals(merchantRequestedChannelId)) {
            /*
             * This is done so that native+ plus integration with offline works
             * well (apparently!)
             */
            LOGGER.info("merchantRequestedChannelId:{}, changing it to {}", merchantRequestedChannelId,
                    EChannelId.WAP.getValue());
            return EChannelId.WAP.getValue();
        }

        if (StringUtils.isBlank(userAgent)) {
            if (StringUtils.isNotBlank(merchantRequestedChannelId)) {
                LOGGER.info("User-Agent is empty, using channelId:{} sent by merchant", merchantRequestedChannelId);
                return merchantRequestedChannelId;
            } else {
                LOGGER.info("User-Agent is empty, using default channelId:{}, ", EChannelId.WAP.getValue());
                return EChannelId.WAP.getValue();
            }
        }

        EChannelId eChannelId = EnvInfoUtil.getChannelID(request.getHeader(TheiaConstant.RequestHeaders.USER_AGENT),
                request.getHeader(TheiaConstant.RequestHeaders.ACCEPT));

        if (eChannelId == null) {
            LOGGER.info("eChannelId=null, using channelId:WAP");
            return EChannelId.WAP.name();
        }

        LOGGER.info("returning channelId:{} after user-agent check", eChannelId.name());
        return eChannelId.name();

    }

    private boolean isMidAllowedForForcedChannelId(String mid) {
        String mids = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(MID_ALLOWED_FORCED_CHANNEL_ID);
        return StringUtils.contains(mids, mid);
    }

    private String parseCVV(String cvv) throws Exception {
        if (StringUtils.length(cvv) < 6) {
            return cvv;
        }
        LOGGER.info("Getting decrypted CVV");
        return getDecryptedCVV(cvv.trim());
    }

    private String getDecryptedCVV(String encryptedCVV) throws Exception {
        LOGGER.info("encryptedCVV payload : {}", encryptedCVV);

        String decryptedCVV = null;

        try {

            decryptedCVV = nativePaymentDetailsCryptoFactory.getCryptoUtil(NativeCryptoType.CVV).decrypt(encryptedCVV);

        } catch (Exception e) {
            LOGGER.info("cvv decryption failed");
            throw e;
        }
        LOGGER.info("cvv decrypted successfully");
        return decryptedCVV;
    }

    public String getDecryptedCardInfo(String encryptedCardInfo) throws Exception {
        LOGGER.info("encryptedCard payload : {}", encryptedCardInfo);

        String decryptedCardInfo = null;

        try {

            decryptedCardInfo = nativePaymentDetailsCryptoFactory.getCryptoUtil(NativeCryptoType.CARD).decrypt(
                    encryptedCardInfo);
            if (StringUtils.isBlank(decryptedCardInfo) || "|||".equals(decryptedCardInfo)) {
                LOGGER.error("Empty Card Info received: {}", decryptedCardInfo);
            }

        } catch (Exception e) {
            LOGGER.info("card decryption failed");
            throw e;
        }
        LOGGER.info("carrd decrypted successfully");
        return decryptedCardInfo;
    }

    private void setSubWalletInfo(EnhancedCashierPageWalletInfo walletInfo, AccountInfo balanceInfo) {
        if (balanceInfo != null && CollectionUtils.isNotEmpty(balanceInfo.getSubWalletDetails())) {
            walletInfo.setSubWalletDetails(balanceInfo.getSubWalletDetails());
        }
    }

    public PCFFeeCharges getPCFFeecharges(String txnToken, EPayMethod payMethod, String instId,
            FeeRateFactors feeRateFactors) throws Exception {
        PCFFeeCharges pcfFeeCharges = new PCFFeeCharges();

        Map<EPayMethod, NativeConsultDetails> consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(
                txnToken, payMethod.getMethod(), instId, feeRateFactors, null);
        if (consultFeeResponse != null && consultFeeResponse.size() > 0) {
            NativeConsultDetails consultDetails = consultFeeResponse.get(payMethod);
            pcfFeeCharges.setFeeAmount(consultDetails.getFeeAmount());
            pcfFeeCharges.setTaxAmount(consultDetails.getTaxAmount());
            pcfFeeCharges.setTotalTransactionAmount(consultDetails.getTotalTransactionAmount());

        }

        return pcfFeeCharges;
    }

    public boolean checkUserWalletSufficient(EnhancedCashierPageWalletInfo walletInfo, String txnAmount,
            NativeCashierInfoResponse nativeCashierInfoResponse, boolean isSubscriptionRequest) {

        if (isSubscriptionRequest && NativeSubscriptionUtils.isZeroRupeesSubscription(txnAmount)) {
            txnAmount = "1";
        }
        String txnType = nativeCashierInfoResponse.getBody().getPaymentFlow().getValue();
        boolean isInsufficientBalance = false;
        if (walletInfo != null && !walletInfo.isDisplay()) {
            isInsufficientBalance = true;
        } else if (walletInfo != null && walletInfo.getWalletBalance() != null) {

            if (walletInfo.getWalletBalance() > 0.0 && (Double.valueOf(txnAmount) > walletInfo.getWalletBalance())) {
                {
                    walletInfo.setEnabled(Boolean.FALSE);
                    isInsufficientBalance = true;
                    if (EPayMode.HYBRID.getValue().equals(txnType)) {
                        walletInfo.setEnabled(Boolean.TRUE);
                    }
                }
            }

            if (txnType.equals(EPayMode.ADDANDPAY.getValue())) {
                walletInfo.setEnabled(Boolean.TRUE);
            }
            if (nativeCashierInfoResponse.getBody().isPcfEnabled() && walletInfo.getPcfFeeCharges() != null) {
                if (Double.valueOf(walletInfo.getPcfFeeCharges().getTotalTransactionAmount().getValue()) > walletInfo
                        .getWalletBalance()) {
                    isInsufficientBalance = true;
                }
            }

            if (!(Double.compare(walletInfo.getWalletBalance(), 0.0) > 0)) {
                isInsufficientBalance = true;
            }
        }
        return isInsufficientBalance;
    }

    public EnhancedCashierPageWalletInfo getEnhancedCashierPageWalletInfo(PaymentRequestBean requestData,
            NativeCashierInfoResponse nativeCashierInfoResponse, String txnToken) throws Exception {
        EnhancedCashierPageWalletInfo walletInfo = new EnhancedCashierPageWalletInfo();

        PayMethod payMethod = nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(s -> EPayMethod.BALANCE.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
        if (payMethod != null && payMethod.getPayChannelOptions() != null
                && !payMethod.getPayChannelOptions().isEmpty()) {
            AccountInfo balanceInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
            TwoFARespData twoFAConfig = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getTwoFAConfig();
            if (balanceInfo != null && balanceInfo.getAccountBalance() != null
                    && !StringUtils.isEmpty(balanceInfo.getAccountBalance().getValue())) {
                walletInfo.setWalletBalance(Double.parseDouble(balanceInfo.getAccountBalance().getValue()));
            }

            setSubWalletInfo(walletInfo, balanceInfo);
            walletInfo.setWalletOnly(nativeCashierInfoResponse.getBody().isWalletOnly());
            walletInfo.setDisplay(!Boolean.valueOf(payMethod.getIsDisabled().getStatus()));
            walletInfo.setEnabled(Boolean.TRUE);
            walletInfo.setDisplayName(payMethod.getDisplayName());
            walletInfo.setName(payMethod.getDisplayName());
            walletInfo.setType(PayModeType.valueOf(payMethod.getPayMethod()).getType());
            walletInfo.setTwoFAConfig(twoFAConfig);

            if (!(walletInfo.getWalletBalance() != null && Double.compare(walletInfo.getWalletBalance(), 0.0) > 0)) {
                walletInfo.setEnabled(Boolean.FALSE);
            }

            if (requestData != null && requestData.isEnhancedCashierPageRequest()
                    && nativeCashierInfoResponse.getBody().isPcfEnabled()) {
                Money totalTransactionAmount;
                PCFFeeCharges pcfFeeCharges = null;
                pcfFeeCharges = getPCFFeecharges(txnToken, EPayMethod.BALANCE, null, null);
                totalTransactionAmount = pcfFeeCharges.getTotalTransactionAmount();
                if (EPayMode.ADDANDPAY.equals(nativeCashierInfoResponse.getBody().getPaymentFlow())
                        || (walletInfo.getWalletBalance() != null && Double
                                .valueOf((totalTransactionAmount).getValue()) <= walletInfo.getWalletBalance())) {
                    walletInfo.setPcfFeeCharges(pcfFeeCharges);
                } else {
                    walletInfo.setPcfFeeCharges(pcfFeeCharges);
                    walletInfo.setEnabled(Boolean.FALSE);
                }
            }

        }

        if (payMethod != null && payMethod.getIsActive() != null) {
            walletInfo.setIsActive(payMethod.getIsActive());
            walletInfo.setUsed(false);
        }
        /*
         * This has been hard-coded to satisfy UI needs.
         */
        walletInfo.setInsufficientBalanceMsg(NATIVE_ERROR_MESSAGE_INSUFFICIENTBALANCE_MSG);
        walletInfo.setIsHybridDisabledMsg(NATIVE_ERROR_MESSAGE_ISHYBRIDDISABLED_MSG);

        /*
         * Handle wallet info display for wallet only merchant UI needs this
         * flag to stop allowing deselection of wallet
         */
        /*
         * if (walletInfo.isWalletOnly()) { walletInfo.setEnabled(false); }
         */

        // Set KYC flag : if
        // nativeCashierInfoResponse.getBody().isOnTheFlyKYCRequired() is true
        if (nativeCashierInfoResponse.getBody().isOnTheFlyKYCRequired()) {
            UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(txnToken);
            if (userDetailsBiz != null) {
                String userKycKey = "KYC_" + userDetailsBiz.getUserId();
                boolean hasCustomerDoneKYCRecently = nativeSessionUtil.getKey(userKycKey) != null ? true : false;

                boolean isAllowedForAllCustomers = Boolean.valueOf(ConfigurationUtil.getProperty(
                        TheiaConstant.RequestParams.KYC_ALLOWED_ALL_FLAG, "false"));
                boolean nativeKYCValidateFlag = Boolean.valueOf(ConfigurationUtil.getProperty(
                        TheiaConstant.RequestParams.KYC_NATIVE_VALIDATE_FLAG, "false"));

                List<String> allowedCustIds = Collections.emptyList();

                if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(TheiaConstant.RequestParams.KYC_ALLOWED_LIST))) {
                    allowedCustIds = Arrays.asList(ConfigurationUtil.getProperty(
                            TheiaConstant.RequestParams.KYC_ALLOWED_LIST).split(","));
                }
                // Set the flag as true
                if (!hasCustomerDoneKYCRecently && nativeKYCValidateFlag
                        && (isAllowedForAllCustomers || allowedCustIds.contains(userDetailsBiz.getUserId()))) {
                    LOGGER.info("On the fly KYC required for Enhanced Native.");
                    walletInfo.setOnTheFlyKYCRequired(nativeCashierInfoResponse.getBody().isOnTheFlyKYCRequired());
                }
            }
        }

        if (payMethod != null && payMethod.getRemainingLimit() != null) {
            walletInfo.setRemainingLimit(payMethod.getRemainingLimit());
        }

        walletInfo.setShowOnlyWallet(requestData != null
                && ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(), FEATURE_THEIA_SHOW_ONLY_WALLET, false));

        if (payMethod != null && payMethod.getIsDisabled() != null
                && Boolean.valueOf(((BalanceStatusInfo) payMethod.getIsDisabled()).getMerchantAccept())) {
            walletInfo.setMerchantAccept(true);
        }
        walletInfo.setWalletLimits(nativeCashierInfoResponse.getBody().getWalletLimits());
        return walletInfo;
    }

    public boolean isAddMoneyMerchant(String mid) {
        if (StringUtils.isNotBlank(mid)) {
            return StringUtils.equals(mid, ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID));
        }
        return false;
    }

    public boolean isNativeEnhancedRequest(PaymentRequestBean paymentRequestData) {

        if (paymentRequestData.getRequest() != null
                && paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))) {
            return true;
        }

        return false;
    }

    private boolean isPWPMerchant(String mid) {
        return merchantPreferenceService.isPwpEnabled(mid);
    }

    private void validateFeeForAddMoney(HttpServletRequest request, NativeInitiateRequest nativeInitiateRequest,
            Map<String, String[]> additionalParams, NativeCashierInfoResponse cashierInfoResponse, String txnToken) {

        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        String mid = orderDetail.getMid();
        String payMode = request.getParameter("paymentMode");
        boolean isPaytmAddMoneyMid = (StringUtils.equals(mid,
                ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID)));

        if ((isPaytmAddMoneyMid || orderDetail.isNativeAddMoney())
                && workFlowHelper.isMidAllowedChargeFeeOnAddMoneyByWallet(mid)) {
            // LOGGER.info("new addMoney fee integration");

            if (isPaytmAddMoneyMid && orderDetail.isAddMoneyFeeAppliedOnWallet()) {
                LOGGER.info("Not consulting wallet, isAddMoneyFeeAppliedOnWallet=true");
                additionalParams.put(IS_ADD_MONEY_RISK_INVOLVED, new String[] { TRUE });
                additionalParams.put(Native.RISK_EXTENDED_INFO,
                        new String[] { request.getParameter(Native.RISK_EXTENDED_INFO) });
                return;
            } else {
                LOGGER.info("Consulting wallet for fee details ");
                UserDetailsBiz userDetails = nativeValidationService.getUserDetails(nativeInitiateRequest
                        .getInitiateTxnReq().getBody());
                if (userDetails == null) {
                    throwExceptionNativeTxn(request, ResultCode.SESSION_EXPIRED_EXCEPTION);
                }
                boolean isGvPurchaseFlow = ifAddMoneyInitiatedForSubWallet(orderDetail, GV_PRODUCT_CODE);
                boolean isTransitWalletFlow = ifAddMoneyInitiatedForSubWallet(orderDetail, NCMC_PRODUCT_CODE);
                String addMoneySource = getAddMoneyRequestSource(orderDetail, request);
                GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse = workFlowHelper
                        .consultWalletService(orderDetail, request, userDetails, isGvPurchaseFlow, isTransitWalletFlow,
                                addMoneySource);
                if (!walletConsultResponse.isSuccessfullyProcessed()) {
                    LOGGER.error("Wallet consult failed : {} , proceeding with Add Money without wallet consult",
                            walletConsultResponse.getFailureDescription());
                }
                if (orderDetail.isNativeAddMoney() || isEnhancedNativeFlow(request)) {
                    validateFeeForAddMoneyForEnhanceAndTopUps(walletConsultResponse.getResponse(), txnToken, request,
                            orderDetail, payMode, userDetails);
                    return;
                }
                RiskFeeDetails riskFeeDetails = orderDetail.getRiskFeeDetails();
                if (!isTxnAmountValid(nativeInitiateRequest, riskFeeDetails)) {
                    LOGGER.error("Txn Amnt doesn't match with the sum of risk fee and initial amount");
                    throwExceptionNativeTxn(request, ResponseConstants.INVALID_TXN_AMOUNT);
                }

                Map<String, Object> feeDetails = getFeeDetailIfUserRisky(walletConsultResponse.getResponse(), payMode);
                String feeRateCode = (null != feeDetails) ? (String) feeDetails.get("feeRateCode") : null;
                String errorMsg = (null != feeDetails) ? (String) feeDetails.get("rejectMsg") : null;
                boolean isRiskyUser = (feeDetails != null && StringUtils.isNotBlank(errorMsg) && !isAddMoneyPcfEnabled(
                        mid, userDetails.getUserId(), feeRateCode));
                if (!isRiskyUser) {
                    checkIfFeeAppliedForNonRiskyUser(request, riskFeeDetails);
                } else {
                    String riskRejectMessage = (String) feeDetails.get("rejectMsg");
                    String feePercent = (String) feeDetails.get("feePercent");
                    boolean isAddMoneyToGv = checkIfAddMoneyToGv(orderDetail, walletConsultResponse);
                    boolean isAddMoneyToTransitBlockWallet = checkIfAddMoneyToTransitBlockWallet(orderDetail,
                            walletConsultResponse);
                    if (isAddMoneyToGv) {
                        // making GV+charges validation configurable

                        Boolean gvPlusChargesAllowedOnCc = Boolean.parseBoolean(ConfigurationUtil.getProperty(

                        "gv.plus.charges.allowed.on.cc", "true"));

                        if (!gvPlusChargesAllowedOnCc) {

                            // Handling for add money to GV amount > 10,000

                            // (configurable at wallet's end)

                            LOGGER.info("Failing txn for Add money to GV > 10,000");

                            throwRiskRejectException(riskRejectMessage, txnToken, orderDetail);

                        }
                    }
                    if (isAddMoneyToTransitBlockWallet) {
                        LOGGER.info("Failing txn for Add money to Transit blocked wallet");
                        throwRiskRejectException(riskRejectMessage, txnToken, orderDetail);
                    }
                    if (isRiskFeeValid(request.getParameter("paymentMode"), riskFeeDetails, feePercent)) {
                        LOGGER.info("Risk fee in pay request is valid and hence proceeding for payment");
                        additionalParams.put(IS_ADD_MONEY_RISK_INVOLVED, new String[] { TRUE });
                        additionalParams.put(Native.RISK_EXTENDED_INFO,
                                new String[] { request.getParameter(Native.RISK_EXTENDED_INFO) });
                    } else {
                        // When fee applied on card not on user , old flow
                        // breaks
                        LOGGER.info("Risk fee in pay request is not equals to fee from wallet : fee applied on card");
                        throwRiskRejectException(riskRejectMessage, txnToken, orderDetail);
                    }
                }
            }
        } else if ((com.paytm.pgplus.common.enums.PayMethod.CREDIT_CARD.getMethod().equals(payMode))
                && isPaytmAddMoneyMid) {
            // LOGGER.info("old addMoney fee integration");
            LOGGER.info("Add Money Request For CC on nativePlus and native");
            RiskFeeDetails riskFeeDetails = orderDetail.getRiskFeeDetails();
            if (!isTxnAmountValid(nativeInitiateRequest, riskFeeDetails)) {
                LOGGER.error("Txn Amnt doesn't match with the sum of risk fee and initial amount");
                throwExceptionNativeTxn(request, ResponseConstants.INVALID_TXN_AMOUNT);
            }
            if (!ifAddMoneyInitiatedForSubWallet(orderDetail, GV_PRODUCT_CODE)
                    && !ifAddMoneyInitiatedForSubWallet(orderDetail, NCMC_PRODUCT_CODE)) {
                boolean isRiskyUser = isRiskyUser(request, nativeInitiateRequest);
                if (!isRiskyUser) {
                    checkIfFeeAppliedForNonRiskyUser(request, riskFeeDetails);
                } else {
                    if (isRiskFeeValid(request.getParameter("paymentMode"), riskFeeDetails, null)) {
                        LOGGER.info("Risk fee in pay request is valid and hence proceeding for payment");
                        additionalParams.put(IS_ADD_MONEY_RISK_INVOLVED, new String[] { TRUE });
                        additionalParams.put(Native.RISK_EXTENDED_INFO,
                                new String[] { request.getParameter(Native.RISK_EXTENDED_INFO) });
                    }
                }
            }
        }

    }

    private String getAddMoneyRequestSource(InitiateTransactionRequestBody orderDetail, HttpServletRequest request) {
        String addMoneySource = StringUtils.EMPTY;
        if (isAddMoneyMerchant(orderDetail.getMid())) {
            if (isEnhancedNativeFlow(request)) {
                addMoneySource = PAYTM_WEBSITE.getValue();
            } else {
                addMoneySource = PAYTM_APP.getValue();
            }
        } else if (orderDetail.isNativeAddMoney()) {
            addMoneySource = THIRD_PARTY.getValue();
        }
        return addMoneySource;
    }

    private boolean checkIfAddMoneyToGv(InitiateTransactionRequestBody orderDetail,
            GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse) {
        if (ifAddMoneyInitiatedForSubWallet(orderDetail, GV_PRODUCT_CODE)) {
            return true;
        } else if (walletConsultResponse.isSuccessfullyProcessed()) {

            return StringUtils.equals(
                    UserSubWalletType.GIFT_VOUCHER.getType(),
                    Optional.ofNullable(walletConsultResponse.getResponse())
                            .map(BizWalletConsultResponse::getAddMoneyDestination).orElse(null));

        }
        return false;
    }

    private boolean checkIfAddMoneyToTransitBlockWallet(InitiateTransactionRequestBody orderDetail,
            GenericCoreResponseBean<BizWalletConsultResponse> walletConsultResponse) {
        if (ifAddMoneyInitiatedForSubWallet(orderDetail, NCMC_PRODUCT_CODE)) {
            return true;
        } else if (walletConsultResponse.isSuccessfullyProcessed()) {

            return StringUtils.equals(
                    UserSubWalletType.TRANSIT_BLOCKED_WALLET.getType(),
                    Optional.ofNullable(walletConsultResponse.getResponse())
                            .map(BizWalletConsultResponse::getAddMoneyDestination).orElse(null));

        }
        return false;
    }

    private Map<String, Object> getFeeDetailIfUserRisky(BizWalletConsultResponse walletConsultResponse, String payMode) {
        if (walletConsultResponse != null && CollectionUtils.isNotEmpty(walletConsultResponse.getFeeDetails())) {
            List<Map<String, Object>> feeDetails = walletConsultResponse.getFeeDetails().stream()
                    .filter(a -> StringUtils.equals((String) a.get("payMethod"), payMode)).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(feeDetails)) {
                return feeDetails.get(0);
            }
        }
        return null;

    }

    private void validateFeeForAddMoneyForEnhanceAndTopUps(BizWalletConsultResponse walletConsultResponse,
            String txnToken, HttpServletRequest request, InitiateTransactionRequestBody orderDetail, String payMode,
            UserDetailsBiz userDetails) {
        Map<String, Object> feeDetails = getFeeDetailIfUserRisky(walletConsultResponse, payMode);
        String errorMessage = (null != feeDetails) ? (String) feeDetails.get("rejectMsg") : null;
        // making GV+charges validation configurable
        String feeRateCode = (null != feeDetails) ? (String) feeDetails.get("feeRateCode") : null;
        boolean isAddMoneyPcfEnabled = isAddMoneyPcfEnabled(orderDetail.getMid(), userDetails.getUserId(), feeRateCode);

        Boolean gvPlusChargesAllowedOnCc = Boolean.parseBoolean(ConfigurationUtil.getProperty(

        "gv.plus.charges.allowed.on.cc", "true"));

        if (StringUtils.isNotEmpty(errorMessage)) {

            if (!gvPlusChargesAllowedOnCc
                    || (!isAddMoneyPcfEnabled && ff4jUtils.isFeatureEnabledOnMid(orderDetail.getMid(),
                            TheiaConstant.FF4J.THEIA_ENABLE_ADD_MONEY_WALLET_LIMIT, false))) {
                throwRiskRejectException(errorMessage, txnToken, orderDetail);
            }

        }
    }

    private boolean isAddMoneyPcfEnabled(String mid, String userId, String feeRateCode) {
        if (StringUtils.isNotEmpty(feeRateCode)
                && StringUtils.isNotEmpty(userId)
                && (!merchantPreferenceService.isAddMoneyPcfDisabled(mid, false))
                && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_ADD_MONEY_SURCHARGE, mid, null, userId,
                        false)) {
            return true;
        }
        return false;
    }

    private void checkIfFeeAppliedForNonRiskyUser(HttpServletRequest request, RiskFeeDetails riskFeeDetails) {
        if (riskFeeDetails != null && !(0.0 == Double.parseDouble(riskFeeDetails.getFeeAmount().getValue()))) {
            LOGGER.error("Fee applied for non risky user , failing txn");
            throwExceptionNativeTxn(request, ResponseConstants.RISK_REJECT);
        }

    }

    public boolean isV3AppInvokeRequired(String mid) {
        return merchantPreferenceService.isSlabBasedMDREnabled(mid)
                || merchantPreferenceService.isDynamicFeeMerchant(mid)
                || merchantPreferenceService.isPostConvenienceFeesEnabled(mid);
    }

    public boolean isRequestOfType(String requestType) {
        if (((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI()
                .contains(requestType)) {
            return true;
        }
        return false;
    }

    public void validateBinDetail(final BinDetail binDetails) {
        if ((binDetails == null) || StringUtils.isEmpty(binDetails.getBank())
                || StringUtils.isEmpty(binDetails.getCardType()) || StringUtils.isEmpty(binDetails.getCardName())) {
            throw new TheiaServiceException("Mandatory data not available for the bin number");
        }
    }

    public void validateTokenDetail(final BinDetail tokenDetails) {
        if ((tokenDetails == null) || StringUtils.isEmpty(tokenDetails.getCardType())
                || StringUtils.isEmpty(tokenDetails.getCardName())) {
            throw new TheiaServiceException("Mandatory data not available for the token ");
        }
    }

    public AppInvokeRedirectionUrlData getUrlDataProvidedSourceUrlForAppInvoke(String sourceUrl,
            InitiateTransactionRequestBody initiateTransactionRequestBody,
            InitiateTransactionResponse initiateTransactionResponse, UserAgentInfo userAgentInfo)
            throws MalformedURLException {

        return getUrlDataProvidedSourceUrlForAppInvoke(sourceUrl, initiateTransactionRequestBody,
                initiateTransactionResponse, userAgentInfo, null);
    }

    public AppInvokeRedirectionUrlData getUrlDataProvidedSourceUrlForAppInvoke(String sourceUrl,
            InitiateTransactionRequestBody initiateTransactionRequestBody,
            InitiateTransactionResponse initiateTransactionResponse, UserAgentInfo userAgentInfo, String sourceName)
            throws MalformedURLException {
        String txnToken = Optional.ofNullable(initiateTransactionResponse).map(InitiateTransactionResponse::getBody)
                .map(InitiateTransactionResponseBody::getTxnToken).orElse(null);
        String mid = initiateTransactionRequestBody.getMid();
        String orderId = initiateTransactionRequestBody.getOrderId();
        String txnAmount = initiateTransactionRequestBody.getTxnAmount().getValue();
        String browserName = StringUtils.isNotBlank(sourceName) ? sourceName : BrowserUtil
                .getBrowserNameForAppLink(userAgentInfo);

        AppInvokeRedirectionUrlData.Builder builder = new AppInvokeRedirectionUrlData.Builder();
        return builder.mid(mid).orderId(orderId).txnToken(txnToken).amount(txnAmount).sourceName(browserName)
                .sourceUrl(sourceUrl).appInvokeFrom(TheiaConstant.DataEnrichmentKeys.APP_INVOKE_FROM).build();
    }

    private boolean eligibleForUpiToAddNPay(HttpServletRequest request, String mid,
            NativeCashierInfoResponse cashierInfoResponse) {
        String paymentFlow = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_FLOW);
        if (StringUtils.isBlank(paymentFlow)) {
            paymentFlow = EPayMode.NONE.getValue();
        }
        Boolean isAddNPayMerchant = cashierInfoResponse.getBody().getAddAndPayMerchant();
        if (null == isAddNPayMerchant) {
            isAddNPayMerchant = false;
        }
        String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
        boolean convertToAddAndPayTxn = Boolean.valueOf(request.getParameter(CONVERT_TO_ADDANDPAY_TXN));
        if (convertToAddAndPayTxn && isAddNPayMerchant && paymentFlow.equals(EPayMode.NONE.getValue())
                && merchantPreferenceService.convertTxnToAddNPayEnabled(mid, false)) {
            if (EPayMethod.UPI_INTENT.getMethod().equalsIgnoreCase(paymentMode)
                    || (EPayMethod.UPI.getMethod().equalsIgnoreCase(paymentMode) && StringUtils.isNotBlank(request
                            .getParameter(TheiaConstant.RequestParams.Native.MPIN)))) {
                return true;
            }
        }
        return false;
    }

    void verifyEnableUpiCollectChannel(NativeCashierInfoResponse cashierInfoResponse,
            InitiateTransactionRequestBody orderDetail, HttpServletRequest request, boolean sendHTML) {
        String paymentFlow = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_FLOW);
        String paymentFlowExpected = EPayMode.ADDANDPAY.getValue();
        boolean isNativeAddMoney = orderDetail.isNativeAddMoney();
        if ((paymentFlowExpected.equals(paymentFlow) || isNativeAddMoney)
                && StringUtils.isBlank(request.getParameter(TheiaConstant.RequestParams.Native.MPIN))) {
            if (cashierInfoResponse != null && cashierInfoResponse.getBody() == null && orderDetail != null) {
                if (merchantPreferenceService.enableUPICollectFromADDNPAY(orderDetail.getMid())) {
                    if (!enableUpiCollectChannel(cashierInfoResponse, orderDetail, request, isNativeAddMoney)) {
                        LOGGER.debug("Could not Enable UPI collect for 3P on add money and add n pay for txn upto Rs 2000");
                        throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                                .isHTMLResponse(sendHTML).isRetryAllowed(true)
                                .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                    }
                }
            }
        }
    }

    private boolean enableUpiCollectChannel(NativeCashierInfoResponse cashierInfoResponse,
            InitiateTransactionRequestBody orderDetail, HttpServletRequest request, boolean isNativeAddMoney) {
        try {
            String txnAmountStr = orderDetail.getTxnAmount().getValue();
            String walletBalanceStr = getWalletBalanceFromLitePayViewResponse(cashierInfoResponse);
            Double walletBalance = Double.valueOf(walletBalanceStr);
            Double txnAmount = Double.valueOf(txnAmountStr);
            String differenceAmount = differenceAmount(txnAmount, walletBalance);
            Double diffAmountInDouble = Double.valueOf(differenceAmount);
            Double maxDeficitAmount = Double.valueOf((ConfigurationUtil.getProperty(MAX_DEFICIT_AMOUNT, "2000")));
            String vpa = request.getParameter(TheiaConstant.RequestParams.Native.PAYER_ACCOUNT);
            if (StringUtils.isNotBlank(orderDetail.getPaytmSsoToken()) && !(orderDetail.isAppInvoke())) {
                if (StringUtils.isNotBlank(vpa)) {
                    boolean checkvpaIsPresent = checkVPAisPresent(cashierInfoResponse, vpa, isNativeAddMoney);
                    if (diffAmountInDouble <= maxDeficitAmount) {
                        if (checkvpaIsPresent) {
                            LOGGER.info("Enable UPI collect for 3P on add money and add n pay for txn upto Rs 2000");
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in enableAddNPayUpiCollect function while fetching data {}", e);
        }
        LOGGER.debug("Either deficit amount is greater or Vpa does not match with Vpa of cashierInfoResponse");
        return false;
    }

    private String differenceAmount(Double txnAmount, Double walletBalance) {
        Double diff = txnAmount - walletBalance;
        return diff.toString();
    }

    boolean checkVPAisPresent(NativeCashierInfoResponse cashierInfoResponse, String VPA, boolean isNativeAddMoney) {

        PayOption payOption = isNativeAddMoney ? cashierInfoResponse.getBody().getMerchantPayOption()
                : cashierInfoResponse.getBody().getAddMoneyPayOption();
        if (payOption != null) {
            List<VpaDetailV4> vpaList1 = nativePayviewConsultServiceHelper.fetchVPADetailsList(payOption);
            List<SarvatraVpaDetails> vpaList2 = nativePayviewConsultServiceHelper.fetchVPADetailsList2(payOption);
            if (vpaList1 != null && vpaList1.size() > 0) {
                for (VpaDetailV4 vpaDetailV4 : vpaList1) {
                    if (VPA.equals(vpaDetailV4.getName())) {
                        return true;
                    }
                }
            } else if (vpaList2 != null && vpaList2.size() > 0) {
                for (SarvatraVpaDetails sarvatraVpaDetails : vpaList2) {
                    if (VPA.equals(sarvatraVpaDetails.getName())) {
                        return true;
                    }
                }
            } else {
                LOGGER.info("vpaDetailsList is not found");
            }
        }
        return false;
    }

    public void changeMidFromAOAToPGIfApplicableForSubscription(NativeJsonRequest nativeJsonRequest) {
        // changing AOAMid if TO PG Mid if the request type is subscription
        if (aoaUtils.isAOAMerchant(nativeJsonRequest.getBody().getMid())
                && ff4JUtils.isFeatureEnabledOnMid(nativeJsonRequest.getBody().getMid(),
                        THEIA_AOASUBSCRIPTIONS_MIDLISTFORAOATOPGCONVERSION, false)) {
            if (StringUtils.isNotBlank(nativeJsonRequest.getHead().getTxnToken())) {
                InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(nativeJsonRequest
                        .getHead().getTxnToken());
                if (orderDetail != null) {
                    String requestType = orderDetail.getRequestType();
                    String subscriptionPaymentMode = nativeJsonRequest.getBody().getPaymentMode();
                    List<String> paymentModes = Arrays.asList((com.paytm.pgplus.theia.utils.ConfigurationUtil
                            .getProperty(SUBSCRIPTION_PAYMODE_FOR_AOA_TO_PG_CONVERSION).split(",")));
                    if (ERequestType.isSubscriptionCreationRequest(requestType)
                            && paymentModes.contains(subscriptionPaymentMode)) {
                        // REPLACING AOA MID WITH PG MID
                        String pgMid = aoaUtils.getPgMidForAoaMid(nativeJsonRequest.getBody().getMid());
                        LOGGER.info("Changed AOA mid {} ,to pg mid {} ", nativeJsonRequest.getBody().getMid(), pgMid);
                        if (StringUtils.isNotBlank(pgMid)) {
                            nativeJsonRequest.getBody().setMid(pgMid);
                        }
                    }
                }
            }
        }
    }

    private boolean failIfWalletInactive(String paymentFlow, String paymentMode, NativeCashierInfoResponse response) {
        if (EPayMethod.BALANCE.getMethod().equalsIgnoreCase(paymentMode)
                || EPayMethod.BALANCE.getOldName().equalsIgnoreCase(paymentMode)
                || EPayMode.ADDANDPAY.getValue().equalsIgnoreCase(paymentFlow)
                || EPayMode.HYBRID.getValue().equalsIgnoreCase(paymentFlow)) {
            if (response != null && response.getBody() != null && response.getBody().getMerchantPayOption() != null
                    && response.getBody().getMerchantPayOption().getPayMethods() != null) {
                PayMethod payMethod = response.getBody().getMerchantPayOption().getPayMethods().stream()
                        .filter(s -> EPayMethod.BALANCE.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
                if (payMethod != null && payMethod.getIsActive() != null && !payMethod.getIsActive().isStatus())
                    return true;
            }
        }
        return false;
    }

    private void validateSubscriptionPayMethods(HttpServletRequest request, Map<String, String[]> additionalParams,
            InitiateTransactionRequestBody orderDetail, boolean sendHTML) throws FacadeCheckedException {
        if (isSubscriptionCardPayModeTransaction(request, orderDetail.getRequestType())
                && ff4jUtils.isFeatureEnabled(THEIA_SUBSCRIPTION_DISABLE_COFT, false)) {

            String[] coftTokenTxn = additionalParams.get(COFTTOKEN_TXN);
            boolean isCoftTokenTxn = Objects.nonNull(coftTokenTxn) && BooleanUtils.toBoolean(coftTokenTxn[0]);
            if (isCoftTokenTxn) {
                LOGGER.error(BizConstant.FailureLogs.TIN_AND_TOKEN_OR_TAVV_PAYMENT_METHOD_NOT_SUPPORTED_FOR_SUBSCRIPTION);
                failureLogUtil.setFailureMsgForDwhPush(ResultCode.INVALID_SUBS_PAYMNT_MODE.getResultCodeId(),
                        BizConstant.FailureLogs.TIN_AND_TOKEN_OR_TAVV_PAYMENT_METHOD_NOT_SUPPORTED_FOR_SUBSCRIPTION,
                        null, true);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.INVALID_SUBS_PAYMNT_MODE)
                        .isHTMLResponse(sendHTML).isRetryAllowed(false)
                        .setRetryMsg(ResultCode.INVALID_SUBS_PAYMNT_MODE.getResultMsg()).build();
            } else {

                String coftConsentInfo = request.getParameter(COFT_CONSENT);
                if (StringUtils.isEmpty(coftConsentInfo)
                        || JsonMapper.mapJsonToObject(coftConsentInfo, CoftConsent.class).getUserConsent() != 1) {
                    LOGGER.error(BizConstant.FailureLogs.FOR_SUBSCRIPTION_COFT_CONSENT_IS_MANDATORY_FOR_NON_TOKENIZED_CARD_TXN);
                    failureLogUtil
                            .setFailureMsgForDwhPush(
                                    ResultCode.INVALID_COFT_CONSENT.getResultCodeId(),
                                    BizConstant.FailureLogs.FOR_SUBSCRIPTION_COFT_CONSENT_IS_MANDATORY_FOR_NON_TOKENIZED_CARD_TXN,
                                    null, true);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.INVALID_COFT_CONSENT)
                            .isHTMLResponse(sendHTML).isRetryAllowed(true)
                            .setRetryMsg(ResultCode.INVALID_COFT_CONSENT.getResultMsg()).build();
                }
            }
        }
    }

    private boolean isSubscriptionCardPayModeTransaction(HttpServletRequest request, String requestType) {
        String paymentMode = request.getParameter(Native.PAYMENT_MODE);
        return ((EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode) || EPayMethod.DEBIT_CARD.getMethod()
                .equalsIgnoreCase(paymentMode)) && (ERequestType.isSubscriptionCreationRequest(requestType)
                || ERequestType.isSubscriptionCreationRequest(request
                        .getParameter(TheiaConstant.ExtraConstants.REQUEST_TYPE))
                || ERequestType.SUBSCRIBE.getType().equalsIgnoreCase(requestType) || ERequestType.SUBSCRIBE.getType()
                .equalsIgnoreCase(request.getParameter(TheiaConstant.ExtraConstants.REQUEST_TYPE))));

    }

    private void addOrderDetails(InitiateTransactionRequestBody orderDetail, Map<String, String[]> additionalParams) {
        try {
            if (orderDetail != null) {
                String orderDetailString = ObjectMapperUtil.getObjectMapper().writeValueAsString(orderDetail);
                additionalParams.put(ORDER_DETAILS, new String[] { orderDetailString });
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing orderDetail = {}", orderDetail);
        }
    }

    public Boolean isValidCardTokenInfo(CardTokenInfo cardTokenInfo, String cardScheme) {
        if (StringUtils.isBlank(cardTokenInfo.getCardToken()) || StringUtils.isBlank(cardTokenInfo.getCardSuffix())) {
            LOGGER.error("Invalid cardTokenInfo : {}", cardTokenInfo);
            return Boolean.FALSE;
        } else if (DINERS.getName().equalsIgnoreCase(cardScheme)) {
            if (StringUtils.isBlank(cardTokenInfo.getTokenUniqueReference())
                    || StringUtils.isBlank(cardTokenInfo.getMerchantTokenRequestorId())) {
                LOGGER.error("Invalid cardTokenInfo for Diners Scheme : {}", cardTokenInfo);
                return Boolean.FALSE;
            }
        } else if (StringUtils.isBlank(cardTokenInfo.getTAVV())
                || StringUtils.isBlank(cardTokenInfo.getPanUniqueReference())) {
            LOGGER.error("Invalid cardTokenInfo : {}", cardTokenInfo);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public void setMidUsingQrcodeId(String merchantId, NativeJsonRequest nativeJsonRequest) {
        try {
            QRCodeInfoRequestData qrCodeInfoRequestData = new QRCodeInfoRequestData(nativeJsonRequest.getBody()
                    .getQrCodeId());
            QRCodeInfoBaseRequest qrCodeInfoBaseRequest = new QRCodeInfoBaseRequest();
            qrCodeInfoBaseRequest.setRequest(qrCodeInfoRequestData);
            QRCodeInfoBaseResponse qrResponse = walletQrCodeDetailsService
                    .getQRCodeInfoByQrCodeId(qrCodeInfoBaseRequest);
            if (qrResponse != null && qrResponse.getResponse() != null) {
                merchantId = qrResponse.getResponse().getMappingId();
            }

        } catch (Exception ex) {
            LOGGER.error("Exception occur while fetching mid from qrCodeId {}", ex);
        }
        if (nativeJsonRequest != null && nativeJsonRequest.getBody() != null) {
            nativeJsonRequest.getBody().setMid(merchantId);
        }
        MDC.put(TheiaConstant.RequestParams.MID, merchantId);
        if (StringUtils.isBlank(merchantId)) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.INVALID_QRCODEID).isHTMLResponse(false)
                    .isNativeJsonRequest(true).build();
        }
    }

    public void setQrCodeAdditionalInfo(NativeJsonRequest nativeJsonRequest) {
        String additionalInfoQrCode = "REQUEST_TYPE:UPI_QR_CODE|qr_code_id:"
                + nativeJsonRequest.getBody().getQrCodeId();
        if (nativeJsonRequest.getBody().getExtendInfo() == null) {
            nativeJsonRequest.getBody().setExtendInfo(new HashMap<>());
        }
        if (StringUtils.isEmpty(nativeJsonRequest.getBody().getExtendInfo().get(ADDITIONALINFO))) {
            nativeJsonRequest.getBody().getExtendInfo().put(ADDITIONALINFO, additionalInfoQrCode);
        } else {
            nativeJsonRequest
                    .getBody()
                    .getExtendInfo()
                    .put(ADDITIONALINFO,
                            nativeJsonRequest.getBody().getExtendInfo().get(ADDITIONALINFO) + '|'
                                    + additionalInfoQrCode);
        }
    }

    public void setBankFormOptimizationFlow(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        if ((requestData.isGenerateEsnRequest() || requestData.isOrderPSPRequest())
                && ff4JUtils.isFeatureEnabledOnMidPayModeAndPercentage(TheiaConstant.FF4J.BANK_FORM_OPTIMIZED_FLOW,
                        requestData.getMid(), flowRequestBean.getPayMethod(), flowRequestBean.getPayOption(),
                        (flowRequestBean.getUserDetailsBiz() != null) ? flowRequestBean.getUserDetailsBiz().getUserId()
                                : null, requestData.getOrderId())) {
            flowRequestBean.setBankFormOptimizationParams(new BankFormOptimizationParams());
            flowRequestBean.getBankFormOptimizationParams().setBankFormOptimizedFlow(true);
            try {
                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(
                        Long.parseLong(ff4JUtils.getPropertyAsStringWithDefault(
                                THEIA_WAIT_TIME_FOR_BANK_FORM_OPTIMIZATION, "10")));
            } catch (Exception ex) {
                LOGGER.error(
                        "Exception occured while parsing THEIA_SLEEP_TIME_BANK_FORM_OPTIMIZATION property into long {}",
                        ex);
                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(10);
            }
            flowRequestBean.getBankFormOptimizationParams().setCheckPaymentStateInFormOptimizedFlow(
                    ff4JUtils.isFeatureEnabledOnMid(requestData.getMid(),
                            THEIA_CHECK_PAYMENT_STATUS_IN_FORM_OPTIMIZED_FLOW, false));
        }
    }
}