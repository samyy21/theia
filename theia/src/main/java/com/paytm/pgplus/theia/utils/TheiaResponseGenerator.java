/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.google.gson.Gson;
import com.paytm.pg.merchant.checksum.EmitraChecksumUtility.EmitraChecksumUtility;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.model.request.QueryPayResultRequestBean;
import com.paytm.pgplus.biz.core.payment.utils.PaymentHelper;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierLopperMapper;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.PayOption;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.utils.AggregatorMidKeyUtil;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DeepLink;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.UpiLiteResponseData;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.common.util.PaymentModeMapperUtil;
import com.paytm.pgplus.dynamicwrapper.exceptions.WrapperServiceException;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import com.paytm.pgplus.dynamicwrapper.utils.JSONUtils;
import com.paytm.pgplus.enums.AggregatorType;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.enums.MerchantUserRequestType;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.*;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.merchant.status.enums.ApiResponse;
import com.paytm.pgplus.payloadvault.merchant.status.response.BankResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.MerchantInfo;
import com.paytm.pgplus.payloadvault.theia.response.*;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.DateUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.constant.StatsDConstants;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantExtendedInfoDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.*;
import com.paytm.pgplus.theia.dynamicWrapper.enums.PaymentCollectionTypes;
import com.paytm.pgplus.theia.dynamicWrapper.model.LogoModel;
import com.paytm.pgplus.theia.dynamicWrapper.model.WrapperTransitionPage;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.s2s.utils.BankFormParser;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.helper.TheiaResponseGeneratorHelper;
import net.minidev.json.JSONValue;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.IS_ENHANCED_NATIVE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.biz.utils.BizConstant.RISK_INFO;
import static com.paytm.pgplus.biz.utils.BizConstant.TXN_FAILURE;
import static com.paytm.pgplus.cache.model.txnstatus.utils.TxnStatusConstants.*;
import static com.paytm.pgplus.payloadvault.theia.constant.EnumValueToMask.SSOTOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoKeys.MERCHANT_SPLIT_SETTLEMENT_INFO;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.stats.constant.StatsDConstants.REQUEST_TYPE;
import static com.paytm.pgplus.stats.constant.StatsDConstants.RESULT_STATUS;
import static com.paytm.pgplus.stats.constant.StatsDConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.ADDRESS_1;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.ADDRESS_2;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.MERCHANT_NAME;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.PHONE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.AGG_MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.RESELLER_PARENT_MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.CARD_SCHEME;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.ERROR_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.PAYMENT_MODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TRANSACTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.LINK_BASED_PAYMENT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.OFFLINE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.AGGREGATOR_MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.PAYMENT_RETRY_INFO;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPCODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPMSG;
import static com.paytm.pgplus.theia.models.UserAgentInfo.deviceIos;

/**
 * @author vaishakhnair, AmitDubey
 *
 */
@Component
public class TheiaResponseGenerator {
    private final Logger LOGGER = LoggerFactory.getLogger(TheiaResponseGenerator.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(TheiaResponseGenerator.class);

    private String htmlFileTemplate;
    private static final String INPUT_FORM_TEMPLATE = "<input type='hidden' name='{name}' value='{value}'>\n";
    private static final String NAME_TEMPLATE = "{name}";
    private static final String VALUE_TEMPLATE = "{value}";
    private static final String WALLET = "WALLET";
    private static final String SUCCESS = "01";

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private TheiaResponseGeneratorHelper theiaResponseGeneratorHelper;

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    private DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier("wrapperImpl")
    IWrapperService wrapperService;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    @Qualifier("theiaSessionDataService")
    ITheiaSessionDataService sessionDataService;

    @Autowired
    @Qualifier("theiaViewResolverService")
    ITheiaViewResolverService viewResolverService;

    @Autowired
    @Qualifier("merchantExtendedInfoDataService")
    IMerchantExtendedInfoDataService merchantExtendedInfoDataService;

    @Autowired
    @Qualifier("aoaUtils")
    AOAUtils aoaUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private PaymentHelper paymentHelper;

    @Autowired
    private ICashier cashier;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    ResponseCodeUtil responseCodeUtil;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    IBinFetchService binFetchService;

    @Autowired
    private PwpTransactionResponseBuilder pwpTransactionResponseBuilder;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataServiceImpl;

    @Autowired
    @Qualifier("acquiringOrderImpl")
    private IAcquiringOrder acquiringOrder;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private BankFormParser bankFormParser;

    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    private IBankInfoDataService bankInfoDataService;

    @Autowired
    private RouterUtil routerUtil;

    @Autowired
    @Qualifier("merchantQueryServiceImpl")
    private IMerchantQueryService merchantQueryService;

    @PostConstruct
    public void init() {
        try {

            InputStream htmlFile = TransactionStatusServiceImpl.class.getClassLoader().getResourceAsStream(
                    "templates/transactionstatus.html");
            htmlFileTemplate = getFileContent(htmlFile);
        } catch (Exception e) {
            LOGGER.error("Exception occurred in reading resource files", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private String getFileContent(InputStream inStrem) throws IOException {
        int ch;
        StringBuilder strContent = new StringBuilder("");
        while ((ch = inStrem.read()) != -1) {
            strContent.append((char) ch);
        }
        return strContent.toString();
    }

    public String getBankPage(GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse) {
        if ((null == initiatePaymentResponse) || !initiatePaymentResponse.isSuccessfullyProcessed()) {
            String errorMessage = null == initiatePaymentResponse ? "Null Response received" : initiatePaymentResponse
                    .getFailureMessage();
            throw new TheiaControllerException("Invalid Response received for initiate payment because : "
                    + errorMessage);
        }
        return getBankPage(initiatePaymentResponse.getResponse());
    }

    public String getMerchantResponse(GenericCoreResponseBean<DoPaymentResponse> doPaymentResponse) {
        if ((null == doPaymentResponse) || !doPaymentResponse.isSuccessfullyProcessed()) {
            String errorMessage = null == doPaymentResponse ? "Null Response received" : doPaymentResponse
                    .getFailureMessage();
            LOGGER.error("Invalid Response received for initiate payment because {}", errorMessage);
            return TheiaConstant.ExtraConstants.EMPTY_STRING;
        }
        TransactionResponse transactionResponse = getMerchantResponse(doPaymentResponse.getResponse());
        String responseHtml = getFinalHtmlResponse(transactionResponse);
        return responseHtml;
    }

    public String getBankPage(InitiatePaymentResponse initiatePaymentResponse) {
        if ((null != initiatePaymentResponse) && (null != initiatePaymentResponse.getCashierPaymentStatus())) {
            String bankForm = initiatePaymentResponse.getCashierPaymentStatus().getWebFormContext();
            if (StringUtils.isNotBlank(bankForm)) {
                return bankForm;
            }
        }
        StatisticsLogger.logForXflush(MDC.get("MID"), null, null, "response", "Could not get bank redirection form",
                null);
        LOGGER.error("Could not get bank redirection form");
        return ExtraConstants.EMPTY_STRING;
    }

    public TransactionResponse getMerchantResponse(DoPaymentResponse doPaymentResponse, String callBackUrl) {
        return doPaymentResponse.getFundOrderStatus() != null ? getResponseForFundOrder(
                doPaymentResponse.getPaymentStatus(), doPaymentResponse.getFundOrderStatus(), callBackUrl)
                : getResponseForAcquiringOrder(doPaymentResponse.getPaymentStatus(),
                        doPaymentResponse.getTransactionStatus(), callBackUrl);
    }

    public TransactionResponse getMerchantResponse(DoPaymentResponse doPaymentResponse) {
        return doPaymentResponse.getFundOrderStatus() != null ? getResponseForFundOrder(
                doPaymentResponse.getPaymentStatus(), doPaymentResponse.getFundOrderStatus())
                : getResponseForAcquiringOrder(doPaymentResponse.getPaymentStatus(),
                        doPaymentResponse.getTransactionStatus());
    }

    public TransactionResponse getResponseForFundOrder(CashierPaymentStatus paymentStatus,
            CashierFundOrderStatus fundOrderStatus, String callBackUrl) {
        TransactionResponse response = getTransactionResponseForFund(paymentStatus, fundOrderStatus);
        if (!StringUtils.isBlank(callBackUrl)) {
            response.setCallbackUrl(callBackUrl);
        }
        return response;
    }

    public TransactionResponse getResponseForFundOrder(CashierPaymentStatus paymentStatus,
            CashierFundOrderStatus fundOrderStatus) {
        TransactionResponse response = getTransactionResponseForFund(paymentStatus, fundOrderStatus);
        return response;
    }

    private TransactionResponse getTransactionResponseForFund(CashierPaymentStatus paymentStatus,
            CashierFundOrderStatus fundOrderStatus) {
        TransactionResponse response = new TransactionResponse();
        String merchantId = getMerchantId(paymentStatus);
        response.setOrderId(fundOrderStatus.getRequestId());
        response.setTxnId(fundOrderStatus.getFundOrderId());
        response.setTxnAmount(getTransactionAmount(paymentStatus));
        response.setMid(merchantId);
        response.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(getPaymentMode(paymentStatus)));
        response.setCurrency(paymentStatus.getTransAmountCurrencyType());
        response.setTxnDate(DateUtils.format(fundOrderStatus.getCreatedTime()));
        response.setCustId(fundOrderStatus.getPayerLoginId());
        response.setTransactionStatus(MapperUtils.getTransactionStatusForResponse(fundOrderStatus, paymentStatus));
        response.setCallbackUrl(getCallbackUrl(fundOrderStatus.getExtendInfo(),
                ExternalTransactionStatus.valueOf(response.getTransactionStatus()), paymentStatus.getExtendInfo()));
        response.setMerchUniqueReference(getMerchUniqueReference(fundOrderStatus, paymentStatus));
        response.setUdf1(getUdf1(fundOrderStatus));
        response.setUdf2(getUdf2(fundOrderStatus));
        response.setUdf3(getUdf3(fundOrderStatus));
        response.setAdditionalInfo(getAdditionalInfo(fundOrderStatus));
        response.setGateway(getGateway(paymentStatus));
        response.setBankTxnId(getBankTxnId(paymentStatus));

        String bankName = getBankName(paymentStatus);
        response.setBankName(bankName);
        // setResponseMessageAndCode(response,
        // paymentStatus.getPaytmResponseCode(), bankName, paymentStatus);

        if (merchantPreferenceService.isBinInResponseEnabled(merchantId)) {
            response.setBinNumber(getBinNumber(paymentStatus));
            response.setLastFourDigits(getLastFourDigits(paymentStatus));
            if ((paymentStatus.getPayOptions() != null) && (paymentStatus.getPayOptions().size() == 1)) {
                if ((PayMethod.CREDIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.DEBIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    response.setCardScheme(paymentStatus.getPayOptions().get(0).getExtendInfo().get(INST_ID));
                }
                if ((PayMethod.EMI.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.EMI_DC.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    response.setCardScheme(getCardSchemeForEMI(paymentStatus.getExtendInfo().get("binNumber")));
                }
            }
        }

        if (merchantPreferenceService.isAllowTxnDeclineResponseEnabled(merchantId)) {
            response.setDeclineReason(paymentStatus.getInstErrorCode());
        }

        if (merchantExtendInfoUtils.isMerchantOnPaytm(merchantId)) {
            LOGGER.info("getTransactionResponseForFund: setting VPA in TransactionResponse");
            response.setVpa(getVirtualPaymentAddr(paymentStatus.getExtendInfo()));
        }
        response.setExtraParamsMap(CommonUtils.getExtraParamsMapFromExtendedInfo(fundOrderStatus.getExtendInfo()));
        if (ExternalTransactionStatus.TXN_SUCCESS.name().equals(response.getTransactionStatus())) {
            if (isCardTokenRequired(paymentStatus)) {
                response.setCardIndexNo(getCardIndexNo(paymentStatus.getExtendInfo()));
            }
            response.setCardHash(getCardHash(paymentStatus.getExtendInfo()));
        }
        response.setUserEmail(getUserEmailFromExtendInfo(fundOrderStatus.getExtendInfo()));
        response.setUserPhone(getUserMobileFromExtendInfo(fundOrderStatus.getExtendInfo()));
        setClientIdIfNotEmpty(paymentStatus, response);

        // Set Response-Code and Message
        responseCodeUtil.setRespMsgeAndCode(response, paymentStatus.getInstErrorCode(), null);
        return response;
    }

    private boolean isCardTokenRequired(CashierPaymentStatus paymentStatus) {
        if (paymentStatus != null && paymentStatus.getExtendInfo() != null
                && (TRUE.equals(paymentStatus.getExtendInfo().get(IS_CARD_TOKEN_REQUIRED)))) {
            return true;
        }

        return false;
    }

    public TransactionResponse getResponseForAcquiringOrder(CashierPaymentStatus paymentStatus,
            CashierTransactionStatus transactionStatus, String callBackUrl) {
        TransactionResponse response = getTransactionResponseInternal(paymentStatus, transactionStatus);
        /*
         * additional check is to ensure static callback url in case of
         * checkoutjs flow when request comes from insta
         */
        if (!StringUtils.isBlank(callBackUrl)
                && !(MapUtils.isNotEmpty(paymentStatus.getExtendInfo())
                        && CHECKOUT.equals(paymentStatus.getExtendInfo().get(ExtraConstants.WORKFLOW)) && !CHECKOUT
                            .equals(transactionStatus.getExtendInfo().get(HEADER_WORKFLOW)))) {
            response.setCallbackUrl(callBackUrl);
        }

        return response;
    }

    public String getErrorMsgForNativePaymentFailure(DoPaymentResponse doPaymentResponse) {
        CashierPaymentStatus paymentStatus = doPaymentResponse.getPaymentStatus();
        CashierTransactionStatus transactionStatus = doPaymentResponse.getTransactionStatus();
        String txnStatus = MapperUtils.getTransactionStatusForResponse(transactionStatus, paymentStatus);
        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(
                paymentStatus.getInstErrorCode(), null, txnStatus);
        return responseCodeUtil.getResponseMsg(responseCodeDetails);
    }

    public String getErrorCodeForNativePaymentFaliure(DoPaymentResponse doPaymentResponse) {
        CashierPaymentStatus paymentStatus = doPaymentResponse.getPaymentStatus();
        CashierTransactionStatus transactionStatus = doPaymentResponse.getTransactionStatus();
        String txnStatus = MapperUtils.getTransactionStatusForResponse(transactionStatus, paymentStatus);
        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(
                paymentStatus.getInstErrorCode(), null, txnStatus);
        return responseCodeDetails.getPaytmResponseCode().toString();
    }

    private TransactionResponse getTransactionResponseInternal(CashierPaymentStatus paymentStatus,
            CashierTransactionStatus transactionStatus) {
        TransactionResponse response = new TransactionResponse();
        if (AcquirementStatusType.SUCCESS.toString().equals(transactionStatus.getStatusDetailType())
                && (PaymentStatus.FAIL.toString().equals(paymentStatus.getPaymentStatusValue()))) {
            CashierPaymentStatus paymentStatusSuccess = getPaymentStatusForSuccessfulTransaction(transactionStatus);
            if (!isNull(paymentStatusSuccess)) {
                paymentStatus = paymentStatusSuccess;
            }
        }

        SystemResponseCode systemResponseCode = null;
        String merchantId = getMerchantId(paymentStatus);
        response.setOrderId(transactionStatus.getMerchantTransId());
        response.setTxnId(paymentStatus.getTransId());
        if (transactionStatus.getExtendInfo() != null && transactionStatus.getExtendInfo().get(DUMMY_ORDER_ID) != null) {
            response.setTxnAmount("0.00");
        } else {
            response.setTxnAmount(getTransactionAmount(paymentStatus));
        }
        response.setMid(merchantId);
        response.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(getPaymentMode(paymentStatus)));
        response.setCurrency(paymentStatus.getTransAmountCurrencyType());
        response.setTxnDate(DateUtils.format(transactionStatus.getCreatedTime()));
        response.setCustId(transactionStatus.getBuyerExternalUserId());

        response.setTransactionStatus(MapperUtils.getTransactionStatusForResponse(transactionStatus, paymentStatus));
        response.setCallbackUrl(getCallbackUrl(transactionStatus.getExtendInfo(),
                ExternalTransactionStatus.valueOf(response.getTransactionStatus()), paymentStatus.getExtendInfo()));
        response.setMerchUniqueReference(getMerchUniqueReference(transactionStatus, paymentStatus));
        response.setUdf1(getUdf1(transactionStatus));
        response.setUdf2(getUdf2(transactionStatus));
        response.setUdf3(getUdf3(transactionStatus));
        response.setAdditionalInfo(getAdditionalInfo(transactionStatus));
        response.setGateway(getGateway(paymentStatus));
        String bankTxnId = StringUtils.isNotBlank(getBankTxnId(paymentStatus)) ? getBankTxnId(paymentStatus)
                : getBankTxnId(transactionStatus, paymentStatus);
        response.setBankTxnId(bankTxnId);
        response.setSubsId(getSubsId(paymentStatus));

        String bankName = getBankName(paymentStatus);
        response.setBankName(bankName);
        response.setBankName(getBankName(paymentStatus));
        if (transactionStatus.getChargeAmount() != null) {
            response.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(transactionStatus.getChargeAmount()
                    .getAmount()));
        } else { // FAILURE and PENDING case
            if (paymentStatus.getPayOptions() != null) {
                List<PayOption> payOptionList = paymentStatus.getPayOptions();
                if (payOptionList.size() == 1) {
                    response.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(payOptionList.get(0)
                            .getChargeAmountValue()));
                } else {
                    Iterator<PayOption> payOptionIterator = payOptionList.iterator();
                    while (payOptionIterator.hasNext()) {
                        PayOption payOption = payOptionIterator.next();
                        if (payOption.getPayMethodName().equals(PayMethod.BALANCE)) {
                            response.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(payOption
                                    .getChargeAmountValue()));
                        }
                    }
                }
            }
        }
        // setBankDetailsForAOAResponse(paymentStatus, response, merchantId);
        if (TheiaConstant.RequestTypes.SEAMLESS.equals(transactionStatus.getRequestType())
                && AcquirementStatusType.CLOSED.toString().equals(transactionStatus.getStatusDetailType())) {
            // setResponseMessageAndCode(response,
            // ResponseConstants.ResponseCodes.PAGE_OPEN_RESPONSE_CODE,
            // bankName,paymentStatus);
            systemResponseCode = SystemResponseCode.PAGE_OPEN_RESPONSE_CODE;
        }

        if (isOfflineFlow(transactionStatus)) {
            response.setAdditionalParam(getAdditionalParam(paymentStatus, transactionStatus.getCurrentTxnCount()));
        }
        if ((MapUtils.isNotEmpty(transactionStatus.getExtendInfo())
                && AggregatorType.ORDER_CREATOR.name().equals(transactionStatus.getExtendInfo().get(AGG_TYPE)) && AggregatorMidKeyUtil
                    .isMidEnabledForMLVAggregatorMid(transactionStatus.getExtendInfo().get(AGGREGATOR_MID)))) {

            response.setAdditionalParam(getAdditionalParam(paymentStatus, transactionStatus.getCurrentTxnCount()));

            MerchantInfo merchantInfo = response.getAdditionalParam().getMerchantInfo();
            if (null == merchantInfo) {
                merchantInfo = new MerchantInfo();
                response.getAdditionalParam().setMerchantInfo(merchantInfo);
            }
            merchantInfo.setCurrentTxnCount(transactionStatus.getCurrentTxnCount());
            merchantInfo.setLogoUrl(transactionStatus.getExtendInfo().get(LOGO_URL));
        }

        if (merchantPreferenceService.isBinInResponseEnabled(merchantId)) {
            response.setBinNumber(getBinNumber(paymentStatus));
            response.setLastFourDigits(getLastFourDigits(paymentStatus));
            if ((paymentStatus.getPayOptions() != null) && (paymentStatus.getPayOptions().size() == 1)) {
                if ((PayMethod.CREDIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.DEBIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    response.setCardScheme(paymentStatus.getPayOptions().get(0).getExtendInfo().get(INST_ID));
                }
                if ((PayMethod.EMI.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.EMI_DC.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    String binNumber = paymentStatus.getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        response.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        response.setCardScheme(getCardSchemeForEMI(transactionStatus));
                }
            }
        }

        if (merchantExtendInfoUtils.isMerchantOnPaytm(merchantId) && null != paymentStatus
                && null != paymentStatus.getExtendInfo()) {
            LOGGER.info("getTransactionResponseInternal: setting VPA in TransactionResponse - ONUS");
            response.setVpa(getVirtualPaymentAddr(paymentStatus.getExtendInfo()));
        } else if (merchantPreferenceService.isReturnVPAEnabled(merchantId) && null != paymentStatus
                && null != paymentStatus.getExtendInfo()) {
            LOGGER.info("getTransactionResponseInternal: setting VPA in TransactionResponse - OFFUS");
            String returnableVpa = getVPA(transactionStatus, paymentStatus.getExtendInfo(), response.getCustId());
            response.setVpa(((StringUtils.isBlank(returnableVpa)) ? "" : returnableVpa));
        }
        if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(response.getPaymentMode())
                && !isOfflineFlow(transactionStatus)) {
            response.setChildTxnList(getChildTxnList(paymentStatus, transactionStatus, merchantId));
        }
        setPromoCodeDetails(response, paymentStatus);
        response.setExtraParamsMap(CommonUtils.getExtraParamsMapFromExtendedInfo(transactionStatus.getExtendInfo()));
        if (ExternalTransactionStatus.TXN_SUCCESS.name().equals(response.getTransactionStatus())) {
            if (isCardTokenRequired(paymentStatus)) {
                response.setCardIndexNo(getCardIndexNo(paymentStatus.getExtendInfo()));
            }
            response.setCardHash(getCardHash(paymentStatus.getExtendInfo()));
        }
        response.setUserEmail(getUserEmailFromExtendInfo(transactionStatus.getExtendInfo()));
        response.setUserPhone(getUserMobileFromExtendInfo(transactionStatus.getExtendInfo()));
        setRequestType(response, merchantId, transactionStatus.getMerchantTransId(), transactionStatus.getRequestType());
        response.setFundSourceVerificationSuccess(getFundSourceVerificationSuccess(paymentStatus));

        // Set Response-Code and Response-Message
        responseCodeUtil.setRespMsgeAndCode(response, paymentStatus.getInstErrorCode(), systemResponseCode);

        overrideStatusAndCodesForPrnValidationFailure(response, transactionStatus);
        setClientIdIfNotEmpty(paymentStatus, response);

        String offlineFlow = transactionStatus.getExtendInfo().get(OFFLINE_FLOW);
        // set for non offline only
        if ((!ERequestType.OFFLINE.getType().equalsIgnoreCase(transactionStatus.getRequestType()) || !Boolean
                .valueOf(offlineFlow))
                && StringUtils.isNotEmpty(response.getMerchUniqueReference())
                && (response.getMerchUniqueReference().startsWith(TheiaConstant.ExtraConstants.LINK_ID_PREFIX)
                        || response.getMerchUniqueReference()
                                .startsWith(TheiaConstant.ExtraConstants.INVOICE_ID_PREFIX) || response
                        .getMerchUniqueReference().startsWith(TheiaConstant.ExtraConstants.PAYMENT_BUTTONS_PREFIX))) {
            LOGGER.info("Going to set Transaction response for Link Based Payments for non bank page mode");
            setRequestAttributesForLinkPaymentsForBanKPage(response, paymentStatus, transactionStatus);
            if (getIsLinkInvoicePayment(transactionStatus)) {
                response.setLinkType(TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
            }
            EventUtils.pushLinkBasedPaymentCompletedEvent(response, null);
        }
        // setting SettlementInfo from extended info
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(transactionStatus.getSplitCommandInfoList())) {
            try {
                LOGGER.info("setting split settlement data from splitCommandInfo list");
                response.setSplitSettlementInfo(JsonMapper.mapObjectToJson(SplitSettlementHelper
                        .convertSplitCommandInfoListToSplitSettlementInfoData(transactionStatus
                                .getSplitCommandInfoList())));
            } catch (FacadeCheckedException e) {
                LOGGER.error("TheiaResponseGenerator.getTxnResponsePaymentNotDone | Exception : {}", e);
            }
        } else if (paymentStatus.getExtendInfo() != null
                && paymentStatus.getExtendInfo().containsKey(MERCHANT_SPLIT_SETTLEMENT_INFO)) {
            response.setSplitSettlementInfo(paymentStatus.getExtendInfo().get(MERCHANT_SPLIT_SETTLEMENT_INFO));
        }

        String oneClickInfo = null;

        if (paymentStatus.getPayOptions() != null) {
            for (PayOption payOption : paymentStatus.getPayOptions()) {
                if (payOption.getExtendInfo() != null
                        && payOption.getExtendInfo().get(ResponseConstants.PAY_RESULT_EXTEND_INFO) != null) {
                    oneClickInfo = payOption.getExtendInfo().get(ResponseConstants.PAY_RESULT_EXTEND_INFO);
                }
                if (merchantPreferenceService.isReturnPrepaidEnabled(merchantId)
                        && StringUtils.isNotBlank(payOption.getPrepaidCard())) {
                    response.setPrepaidCard(payOption.getPrepaidCard());
                }
            }
        }

        if (oneClickInfo != null) {
            LOGGER.info("OneClick getTransactionResponseInternal oneClickInfo is " + oneClickInfo);
            response.setOneClickInfo(oneClickInfo);
        }

        if (!CollectionUtils.isEmpty(transactionStatus.getPaymentViews())) {
            String aggregatorMid = null;
            PaymentView paymentView = transactionStatus.getPaymentViews().get(0);
            if (paymentView != null) {
                if (paymentView.getPayRequestExtendInfo() != null) {
                    aggregatorMid = paymentView.getPayRequestExtendInfo().get(AGGREGATOR_MID);
                }
                if (merchantPreferenceService.isReturnPrepaidEnabled(merchantId)) {
                    if (!CollectionUtils.isEmpty(paymentView.getPayOptionInfos())) {
                        for (PayOptionInfo payOptionInfo : paymentView.getPayOptionInfos()) {
                            if (payOptionInfo.getExtendInfo() != null
                                    && StringUtils.isNotEmpty(payOptionInfo.getExtendInfo().get(
                                            FacadeConstants.PREPAID_CARD))) {
                                response.setPrepaidCard(payOptionInfo.getExtendInfo().get(FacadeConstants.PREPAID_CARD));
                            }
                        }
                    }
                }
            }
            response.setAggMid(aggregatorMid);
        }

        String savedCardId = getSavedCardId(transactionStatus);

        if (StringUtils.isNotBlank(savedCardId) && merchantPreferenceService.isSavedCardIdSupported(merchantId)) {
            LOGGER.info("Received transaction from saved card and merchant has enabled its preference to get saved card id");
            response.setSavedCardId(savedCardId);
        }

        if (isOfflineFlow(transactionStatus)) {
            response.setOfflineRequest(Boolean.TRUE);
        }

        // Setting risk Info in callback for rent Merchants
        if (merchantPreferenceService.isOnusRentPaymentMerchant(merchantId)
                && transactionStatus.getOrderModifyExtendInfo() != null) {
            Map<String, String> orderModifyExtendInfo = null;
            try {
                orderModifyExtendInfo = JsonMapper.mapJsonToObject(transactionStatus.getOrderModifyExtendInfo(),
                        Map.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Not able to cast orderModifyExtendInfo for onus rent merchants");
            }

            if (orderModifyExtendInfo != null && orderModifyExtendInfo.containsKey(RISK_INFO)) {
                response.setRiskInfo(orderModifyExtendInfo.get(RISK_INFO));
            }

        }

        if (paymentStatus != null && StringUtils.isNotBlank(paymentStatus.getPwpCategory())) {
            changeResponseForPwpMerchant(response);
        }

        // setting data for link redirect callback for subscription links
        setResponseForSubsLinkPayment(transactionStatus.getExtendInfo(), response, transactionStatus.getAcquirementId());

        if (response.isSubsLinkInfo()) {
            Date date = paymentStatus.getPaidTime();
            if (date == null) {
                date = transactionStatus.getCreatedTime();
            }
            response.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        }
        // addRegionalFieldInPTCResponse(response);
        if (merchantPreferenceService.isTxnPaidTimePreferenceEnabled(merchantId, false)) {
            setTxnPaidTime(response, transactionStatus);
        }
        response.setRequestedTimestamp(transactionStatus.getExtendInfo().get("ptInfo1"));
        response.setPaymodeIdentifier(paymentStatus.getExtendInfo().get("paymodeIdentifier"));
        if (!CollectionUtils.isEmpty(transactionStatus.getPaymentViews())
                && transactionStatus.getPaymentViews().get(0).getPayRequestExtendInfo() != null)
            response.setChallanIdNum(transactionStatus.getPaymentViews().get(0).getPayRequestExtendInfo()
                    .get("extraParamsMap.challanIdNum"));

        String extIfscCode = null;
        if (isPaymodeNBCCDC(paymentStatus) && isWrapperAllowed(transactionStatus)) {
            try {
                if (paymentStatus.getPayOptions().get(0).getExtendInfo() != null
                        && StringUtils.isNotBlank(paymentStatus.getPayOptions().get(0).getExtendInfo().get("bankAbbr"))) {
                    extIfscCode = getIfscCodeFromMapping(paymentStatus.getPayOptions().get(0).getExtendInfo()
                            .get("bankAbbr"));
                } else if (paymentStatus.getPayOptions().get(0).getExtendInfo() != null
                        && StringUtils.isNotBlank(paymentStatus.getPayOptions().get(0).getExtendInfo()
                                .get("serviceInstId"))) {
                    extIfscCode = getIfscCodeFromMapping(paymentStatus.getPayOptions().get(0).getExtendInfo()
                            .get("serviceInstId"));
                }
                LOGGER.info("extIfscCode : {}", extIfscCode);
            } catch (Exception e) {
                LOGGER.error("Error while fetching extIfscCode {}", e.getMessage());
            }
        }
        if (StringUtils.isNotBlank(extIfscCode) && extIfscCode.length() >= 4) {
            Map<String, Object> extraParamMap = response.getExtraParamsMap();
            if (extraParamMap.equals(Collections.emptyMap()))
                extraParamMap = new HashMap<>();
            extraParamMap.put("extIfscCode", extIfscCode.substring(0, 4));
            response.setExtraParamsMap(extraParamMap);
        }

        if (EPayMethod.UPI.getOldName().equals(response.getPaymentMode())) {
            LOGGER.info("Setting BankResultInfo for UPI");
            if (merchantPreferenceService.isPopulateBankResultInfoEnabled(merchantId)) {
                response.setBankResultInfo(populateBankResultInfo(paymentStatus));
                LOGGER.info("BankResultInfo for UPI transaction : {}", response.getBankResultInfo());
            }
        }

        if (merchantPreferenceService.isAllowTxnDeclineResponseEnabled(merchantId)) {
            response.setDeclineReason(paymentStatus.getInstErrorCode());
        }

        return response;
    }

    private void setRequestAttributesForLinkPaymentsForBanKPage(TransactionResponse response,
            CashierPaymentStatus paymentStatus, CashierTransactionStatus transactionStatus) {
        Date date = paymentStatus.getPaidTime();
        if (date == null) {
            date = transactionStatus.getCreatedTime();
        }

        response.setShowViewFlag(PAYMENT_SCREEN);
        response.setLinkBasedPayment("true");
        response.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        LOGGER.info("Fetching merchant info from Redis for Link based payment for  = {}",
                transactionStatus.getAcquirementId());
        LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                .get(LINK_BASED_KEY + transactionStatus.getAcquirementId());

        if (linkBasedMerchantInfo != null) {
            response.setMerchantName(linkBasedMerchantInfo.getMerchantName());
            response.setMerchantImage(linkBasedMerchantInfo.getMerchantImage());
        }
        TransactionResponse tranResponse = new TransactionResponse();
        if (AcquirementStatusType.CLOSED.toString().equals(transactionStatus.getStatusDetailType())) {
            setResponseMessageAndCode(tranResponse,
                    TheiaConstant.ResponseConstants.ResponseCodes.PAGE_OPEN_RESPONSE_CODE, null, paymentStatus);
        } else {
            setResponseMessageAndCode(tranResponse, paymentStatus.getPaytmResponseCode(), null, paymentStatus);
        }

    }

    private void setRequestType(TransactionResponse response, String mid, String orderId, String requestType) {
        if (ERequestType.NATIVE.getType().equals(requestType)) {
            EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                    .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId));
            if (enhanceCashierPageCachePayload != null) {
                response.setRequestType(enhanceCashierPageCachePayload.getMerchantRequestData().getRequestType());
            }
        } else {
            response.setRequestType(requestType);
        }
    }

    private void overrideStatusAndCodesForPrnValidationFailure(final TransactionResponse response,
            final CashierTransactionStatus transactionStatus) {

        boolean overridePRNValidationResults = Boolean.valueOf(ConfigurationUtil.getProperty(
                "override.merchant.response.for.prnValidationFailure", "false"));

        boolean prnValidationStatusSuccess = transactionStatus.isPrnValidationStatusSuccess();

        boolean dynamicQR2faEnabled = ((merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(response.getMid()) && ERequestType.DYNAMIC_QR_2FA
                .getType().equals(transactionStatus.getRequestType())));

        if (dynamicQR2faEnabled && !prnValidationStatusSuccess && overridePRNValidationResults
                && SUCCESS.equals(response.getResponseCode())) {
            response.setResponseCode("1001");
            response.setResponseMsg("3D Secure Verification failed.");
            response.setTransactionStatus("TXN_FAILURE");
        }
    }

    private void setBankDetailsForAOAResponse(CashierPaymentStatus paymentStatus, TransactionResponse response,
            String merchantId) {

        if (aoaUtils.isAOAMerchant(merchantId)) {

            response.setGateway(getGateway(paymentStatus.getPayOptions().get(0).getExtendInfo()));
            response.setBankTxnId(getBankTxnId(paymentStatus.getPayOptions().get(0).getExtendInfo()));

            if (!paymentStatus.getPayOptions().get(0).getPayMethodName().equals("UPI"))
                response.setBankName(paymentStatus.getPayOptions().get(0).getExtendInfo().get(INST_ID));
        }
    }

    public TransactionResponse getResponseForAcquiringOrder(CashierPaymentStatus paymentStatus,
            CashierTransactionStatus transactionStatus) {
        TransactionResponse response = getTransactionResponseInternal(paymentStatus, transactionStatus);
        return response;
    }

    private AdditionalParam getAdditionalParam(CashierPaymentStatus paymentStatus, String currentTxnCount) {

        AdditionalParam additionalParam = new AdditionalParam();

        if (!CollectionUtils.isEmpty(paymentStatus.getExtendInfo())) {
            additionalParam.setMerchantInfo(getMerchantInfo(paymentStatus.getExtendInfo(), currentTxnCount));
        }
        if (!CollectionUtils.isEmpty(paymentStatus.getPayOptions())) {
            additionalParam.setPaymentInfoList(getPaymentInfo(paymentStatus.getPayOptions()));
        }
        return additionalParam;
    }

    private List<PaymentInfo> getPaymentInfo(List<PayOption> payOptions) {
        List<PaymentInfo> paymentInfo = new ArrayList<>();
        for (PayOption payOption : payOptions) {
            PaymentInfo payInfo = new PaymentInfo();
            payInfo.setPaymentMode(payOption.getPayMethodOldName());
            payInfo.setTxnAmount(AmountUtils.getTransactionAmountInRupee(payOption.getTransAmountValue()));
            payInfo.setCardNo(payOption.getExtendInfo().get(ResponseConstants.MASKED_CARD_NO));
            payInfo.setInstId(payOption.getExtendInfo().get(INST_ID));
            paymentInfo.add(payInfo);
        }
        return paymentInfo;
    }

    private MerchantInfo getMerchantInfo(Map<String, String> extendedInfoMap, String currentTxnCount) {
        MerchantInfo merchantData = new MerchantInfo();
        merchantData.setName(extendedInfoMap.get(MERCHANT_NAME));
        AddressInfo address = new AddressInfo(extendedInfoMap.get(ADDRESS_1), extendedInfoMap.get(ADDRESS_2),
                extendedInfoMap.get(AREA_NAME), extendedInfoMap.get(CITY_NAME), extendedInfoMap.get(ZIP_CODE),
                extendedInfoMap.get(STATE_NAME), extendedInfoMap.get(COUNTRY_NAME));
        merchantData.setAddress(address);
        merchantData.setPhone(extendedInfoMap.get(PHONE));
        merchantData.setMccCode(extendedInfoMap.get(MCC_CODE));
        merchantData.setCategory(extendedInfoMap.get(CATEGORY));
        merchantData.setSubCategory(extendedInfoMap.get(SUB_CATEGORY));
        merchantData.setQrDisplayName(extendedInfoMap.get(QR_DISPLAY_NAME));
        merchantData.setCurrentTxnCount(currentTxnCount);
        return merchantData;
    }

    private boolean isOfflineFlow(CashierTransactionStatus transactionStatus) {
        return transactionStatus != null
                && !CollectionUtils.isEmpty(transactionStatus.getExtendInfo())
                && ((null != transactionStatus.getExtendInfo().get(ExtraConstants.REQUEST_TYPE) && transactionStatus
                        .getExtendInfo().get(ExtraConstants.REQUEST_TYPE).equals(ERequestType.OFFLINE.name())) || (null != transactionStatus
                        .getExtendInfo().get(ExtraConstants.OFFLINE_FLOW) && transactionStatus.getExtendInfo()
                        .get(ExtraConstants.OFFLINE_FLOW).equals(TRUE)));
    }

    private String getSubsId(CashierPaymentStatus paymentStatus) {
        return paymentStatus.getExtendInfo().get(ExtendedInfoSubscription.SUBS_ID);
    }

    private String getMerchantId(CashierPaymentStatus paymentStatus) {
        if (paymentStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID) != null) {
            return paymentStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID);
        }
        return paymentStatus.getExtendInfo().get(PAYTM_MERCHANT_ID);
    }

    private String getMaskedCardNo(Map<String, String> extendInfo) {
        return extendInfo.get(ResponseConstants.MASKED_CARD_NO);
    }

    private String getCardIndexNo(Map<String, String> extendInfo) {
        return extendInfo.get(ResponseConstants.CARD_INDEX_NO);
    }

    private String getCardHash(Map<String, String> extendInfo) {
        return extendInfo.get(ResponseConstants.CARD_HASH);
    }

    private String getVirtualPaymentAddr(Map<String, String> extendInfo) {
        LOGGER.info("Getting VPA from ExtendInfo");
        return extendInfo.get(ResponseConstants.VIRTUAL_PAYMENT_ADDR);
    }

    private List<ChildTransaction> getChildTxnList(CashierPaymentStatus paymentStatus,
            CashierTransactionStatus transactionStatus, String merchantId) {
        List<ChildTransaction> childTxnList = new ArrayList<>();
        for (PayOption payOption : paymentStatus.getPayOptions()) {
            ChildTransaction childTransaction = new ChildTransaction();
            childTransaction.setBankTxnId(getBankTxnId(payOption.getExtendInfo()));
            if (merchantPreferenceService.isReturnPrepaidEnabled(merchantId)) {
                childTransaction.setPrepaidCard(payOption.getPrepaidCard());
            }
            childTransaction.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(payOption
                    .getPayMethodOldName()));
            childTransaction.setGateway(getGateway(payOption));
            childTransaction.setBankName(getBankName(payOption, paymentStatus));
            childTransaction.setTxnAmount(AmountUtils.getTransactionAmountInRupee(payOption.getTransAmountValue()));
            childTransaction.setTxnId(paymentStatus.getTransId());
            childTransaction.setStatus(MapperUtils.getTransactionStatusForResponse(transactionStatus, paymentStatus));
            String maskedCardNumber = payOption.getExtendInfo().get(
                    ExtendedInfoKeys.PaymentStatusKeys.MASKED_CARD_NUMBER);
            if (StringUtils.isNotBlank(maskedCardNumber)
                    && merchantPreferenceService.isBinInResponseEnabled(merchantId)) {
                childTransaction.setBin(maskedCardNumber.substring(0, 6));
                childTransaction.setLastFourDigit(maskedCardNumber.substring(maskedCardNumber.length() - 4,
                        maskedCardNumber.length()));
                if (DEBIT_CARD.equals(payOption.getPayMethodName()) || CREDIT_CARD.equals(payOption.getPayMethodName())) {
                    childTransaction.setCardScheme(payOption.getExtendInfo().get(INST_ID));
                }
                if (EMI.equals(payOption.getPayMethodName()) || EMI_DC.equals(payOption.getPayMethodName())) {
                    String binNumber = paymentStatus.getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        childTransaction.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        childTransaction.setCardScheme(getCardSchemeForEMI(transactionStatus));
                }
            }
            childTxnList.add(childTransaction);
        }
        return childTxnList;
    }

    private String getGateway(CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        } else if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            return ExtraConstants.EMPTY_STRING;
        }

        if (paymentStatus.getPayOptions() != null && !paymentStatus.getPayOptions().isEmpty()) {
            return getGateway(paymentStatus.getPayOptions().get(0).getExtendInfo());
        }
        return StringUtils.EMPTY;
    }

    private String getGateway(String paymentMode, WorkFlowResponseBean workFlowResponseBean) {

        List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptions = null;

        if (workFlowResponseBean != null && workFlowResponseBean.getQueryPaymentStatus() != null) {
            payOptions = workFlowResponseBean.getQueryPaymentStatus().getPayOptions();
        }

        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        } else if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            return ExtraConstants.EMPTY_STRING;
        }

        if (payOptions != null && !payOptions.isEmpty()) {
            return getGateway(payOptions.get(0).getExtendInfo());
        }

        return StringUtils.EMPTY;
    }

    private String getBankName(String paymentMode, WorkFlowResponseBean workFlowResponseBean) {

        Map<String, String> extendInfo = null;
        List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptions = null;

        if (workFlowResponseBean != null && workFlowResponseBean.getQueryPaymentStatus() != null) {
            extendInfo = workFlowResponseBean.getQueryPaymentStatus().getExtendInfo();
            payOptions = workFlowResponseBean.getQueryPaymentStatus().getPayOptions();
        }

        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        } else if (EPayMethod.DEBIT_CARD.getOldName().equals(paymentMode)
                || EPayMethod.CREDIT_CARD.getOldName().equals(paymentMode)) {
            if (extendInfo != null) {
                return getBankName(extendInfo);
            }
        } else if (EPayMethod.EMI.getOldName().equals(paymentMode)) {
            if (extendInfo != null) {
                return getBankName(extendInfo);
            }
        } else if (EPayMethod.UPI.getOldName().equals(paymentMode)) {
            return null;
        } else if ((EPayMethod.NET_BANKING.getOldName().equals(paymentMode) || EPayMethod.HYBRID_PAYMENT.getOldName()
                .equals(paymentMode)) && extendInfo != null && StringUtils.isNotBlank(getBankName(extendInfo))) {
            // added for extracting bank name for hybrid paymode
            return getBankName(extendInfo);
        }

        if (payOptions != null && !payOptions.isEmpty()) {
            return getGateway(payOptions.get(0).getExtendInfo());
        }

        return StringUtils.EMPTY;
    }

    private String getBankName(CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        } else if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            return ExtraConstants.EMPTY_STRING;
        } else if (EPayMethod.DEBIT_CARD.getOldName().equals(paymentMode)
                || EPayMethod.CREDIT_CARD.getOldName().equals(paymentMode)) {
            return getBankName(paymentStatus.getExtendInfo());
        } else if (EPayMethod.EMI.getOldName().equals(paymentMode)
                || EPayMethod.EMI_DC.getOldName().equals(paymentMode)) {
            return getBankName(paymentStatus.getExtendInfo());
        } else if (EPayMethod.UPI.getOldName().equals(paymentMode)) {
            return null;
        } else if (EPayMethod.NET_BANKING.getOldName().equals(paymentMode) && paymentStatus.getExtendInfo() != null
                && StringUtils.isNotBlank(getBankName(paymentStatus.getExtendInfo()))) {
            return getBankName(paymentStatus.getExtendInfo());
        }

        if (paymentStatus.getPayOptions() != null && !paymentStatus.getPayOptions().isEmpty()) {
            return getGateway(paymentStatus.getPayOptions().get(0).getExtendInfo());
        }
        return StringUtils.EMPTY;

    }

    private String getBankName(Map<String, String> extendInfo) {
        return extendInfo.get(ExtendedInfoKeys.PaymentStatusKeys.ISSUING_BANK_NAME);
    }

    // Method used to form child transaction data
    private String getBankName(PayOption payOption, CashierPaymentStatus paymentStatus) {
        String paymentMode = payOption.getPayMethodOldName();
        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        } else if (EPayMethod.DEBIT_CARD.getOldName().equals(paymentMode)
                || EPayMethod.CREDIT_CARD.getOldName().equals(paymentMode)) {
            return getBankName(paymentStatus.getExtendInfo());
        } else if (EPayMethod.EMI.getOldName().equals(paymentMode)) {
            return getBankName(paymentStatus.getExtendInfo());
        } else if (EPayMethod.UPI.getOldName().equals(paymentMode)) {
            return null;
        } else if (EPayMethod.NET_BANKING.getOldName().equals(paymentMode) && paymentStatus.getExtendInfo() != null
                && StringUtils.isNotBlank(getBankName(paymentStatus.getExtendInfo()))) {
            return getBankName(paymentStatus.getExtendInfo());
        }
        return getGateway(payOption.getExtendInfo());
    }

    private String getBankTxnId(CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            return ExtraConstants.EMPTY_STRING;
        }
        if (paymentStatus != null && !CollectionUtils.isEmpty(paymentStatus.getPayOptions())
                && paymentStatus.getPayOptions().get(0).getExtendInfo() != null) {
            return getBankTxnId(paymentStatus.getPayOptions().get(0).getExtendInfo());
        }
        return ExtraConstants.EMPTY_STRING;
    }

    private String getBankTxnId(CashierTransactionStatus cashierTransactionStatus, CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            return ExtraConstants.EMPTY_STRING;
        }
        if (cashierTransactionStatus != null
                && !CollectionUtils.isEmpty(cashierTransactionStatus.getPaymentViews())
                && !CollectionUtils.isEmpty(cashierTransactionStatus.getPaymentViews().get(0).getPayOptionInfos())
                && !CollectionUtils.isEmpty(cashierTransactionStatus.getPaymentViews().get(0).getPayOptionInfos()
                        .get(0).getExtendInfo())
                && cashierTransactionStatus.getPaymentViews().get(0).getExtendInfo() != null) {
            return getBankTxnId(cashierTransactionStatus.getPaymentViews().get(0).getPayOptionInfos().get(0)
                    .getExtendInfo());
        }
        return ExtraConstants.EMPTY_STRING;
    }

    private String getBinNumber(CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.CREDIT_CARD.getOldName().equals(paymentMode)
                || EPayMethod.DEBIT_CARD.getOldName().equals(paymentMode)) {
            String maskedCardNumber = paymentStatus.getPayOptions().get(0).getExtendInfo()
                    .get(ExtendedInfoKeys.PaymentStatusKeys.MASKED_CARD_NUMBER);
            if (StringUtils.isNotBlank(maskedCardNumber) && (maskedCardNumber.length() > 10)) {
                return maskedCardNumber.substring(0, 6);
            }
        } else if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            for (PayOption payOption : paymentStatus.getPayOptions()) {
                String childPaymenMode = payOption.getPayMethodName();
                if (EPayMethod.CREDIT_CARD.getOldName().equals(childPaymenMode)
                        || EPayMethod.DEBIT_CARD.getOldName().equals(childPaymenMode)) {
                    String maskedCardNumber = payOption.getExtendInfo().get(
                            ExtendedInfoKeys.PaymentStatusKeys.MASKED_CARD_NUMBER);
                    if (StringUtils.isNotBlank(maskedCardNumber) && (maskedCardNumber.length() > 10)) {
                        return maskedCardNumber.substring(0, 6);
                    }
                }

            }
        }
        return ExtraConstants.EMPTY_STRING;
    }

    private String getLastFourDigits(CashierPaymentStatus paymentStatus) {
        String paymentMode = getPaymentMode(paymentStatus);
        if (EPayMethod.CREDIT_CARD.getOldName().equals(paymentMode)
                || EPayMethod.DEBIT_CARD.getOldName().equals(paymentMode)) {
            String maskedCardNumber = paymentStatus.getPayOptions().get(0).getExtendInfo()
                    .get(ExtendedInfoKeys.PaymentStatusKeys.MASKED_CARD_NUMBER);
            if (StringUtils.isNotBlank(maskedCardNumber) && (maskedCardNumber.length() > 10)) {
                return maskedCardNumber.substring(maskedCardNumber.length() - 4, maskedCardNumber.length());
            }
        } else if (EPayMethod.HYBRID_PAYMENT.getOldName().equals(paymentMode)) {
            for (PayOption payOption : paymentStatus.getPayOptions()) {
                String childPaymenMode = payOption.getPayMethodName();
                if (EPayMethod.CREDIT_CARD.getOldName().equals(childPaymenMode)
                        || EPayMethod.DEBIT_CARD.getOldName().equals(childPaymenMode)) {
                    String maskedCardNumber = payOption.getExtendInfo().get(
                            ExtendedInfoKeys.PaymentStatusKeys.MASKED_CARD_NUMBER);
                    if (StringUtils.isNotBlank(maskedCardNumber) && (maskedCardNumber.length() > 10)) {
                        return maskedCardNumber.substring(maskedCardNumber.length() - 4, maskedCardNumber.length());
                    }
                }

            }
        }
        return ExtraConstants.EMPTY_STRING;
    }

    private String getGateway(Map<String, String> extendInfo) {
        String gatewayName = extendInfo.get(ExtendedInfoKeys.PaymentStatusKeys.GATEWAY_NAME);
        if (StringUtils.isNotBlank(gatewayName)) {
            return gatewayName;
        }
        return extendInfo.get(ExtendedInfoKeys.PaymentStatusKeys.GATEWAY);
    }

    private String getGateway(PayOption payOption) {
        String paymentMode = payOption.getPayMethodOldName();
        if (EPayMethod.BALANCE.getOldName().equals(paymentMode)) {
            return WALLET;
        }
        return payOption.getExtendInfo() != null ? getGateway(payOption.getExtendInfo()) : ExtraConstants.EMPTY_STRING;
    }

    private String getBankTxnId(Map<String, String> extendInfo) {

        String bankTxnId = "";

        if (StringUtils.isNotBlank(extendInfo.get(ExtendedInfoKeys.PaymentStatusKeys.BANK_TXN_ID))) {
            bankTxnId = extendInfo.get(ExtendedInfoKeys.PaymentStatusKeys.BANK_TXN_ID);
        }

        return bankTxnId;
    }

    public String getFinalHtmlResponse(TransactionResponse response) {
        // addRegionalFieldInPTCResponse(response);
        // changing response of ZestMoney from NB to EMI
        changeResponseforZestMoney(response);
        StringBuilder strBuilder = new StringBuilder();

        // if
        // (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(response.getRequestType())
        // ||
        // ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(response.getRequestType()))
        // {
        //
        // String theiaRedirectionPath =
        // ConfigurationUtil.getProperty("theia.base.path");
        //
        // Map<String, Object> linkBasedResponseParams = new HashMap<String,
        // Object>();
        //
        // if
        // (ExternalTransactionStatus.PENDING.name().equals(response.getTransactionStatus()))
        // {
        // linkBasedResponseParams.put(PAYMENT_STATUS, CANCEL_TXN_STATUS);
        // } else {
        // linkBasedResponseParams.put(PAYMENT_STATUS,
        // response.getTransactionStatus());
        // }
        // linkBasedResponseParams.put(TXN_DATE, response.getTxnDate());
        // linkBasedResponseParams.put(TRANSACTION_ID, response.getTxnId());
        // linkBasedResponseParams.put(TXN_AMOUNT, response.getTxnAmount());
        // linkBasedResponseParams.put(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
        // linkBasedResponseParams.put(ORDER_ID, response.getOrderId());
        //
        // if
        // (ExternalTransactionStatus.PENDING.name().equals(response.getTransactionStatus()))
        // {
        // if (theiaRedirectionPath != null) {
        // response.setCallbackUrl(theiaRedirectionPath +
        // "/linkPaymentRedirect");
        // } else {
        // LOGGER.error("Theia redirection path not found in properties file for link based payment");
        // }
        // }
        // return getFinalHtmlResponse(response,
        // getHtmlfromPramsMap(linkBasedResponseParams));
        // }

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(response.getMid());
        if (merchantPreferenceService.isEncRequestEnabled(response.getMid()) || isAES256Encrypted) {
            strBuilder = theiaResponseGeneratorHelper.encryptedResponse(response, isAES256Encrypted, false);
        } else {
            // This is already done
            /*
             * String respMsg = ""; if
             * (StringUtils.isNotBlank(response.getResponseCode()) &&
             * StringUtils.isNumeric(response.getResponseCode())) { respMsg =
             * getResponseMessageFromCode(response.getResponseCode());
             * response.setResponseMsg(respMsg); }
             */
            // Hard coding api name as all request on theia would return same
            // response TransactionResponse
            String respMsg;
            Map<String, Object> extraParamsMap = response.getExtraParamsMap();
            if (dynamicWrapperUtil.isDynamicWrapperEnabled()
                    && (dynamicWrapperUtil.isDynamicWrapperConfigPresent(response.getMid(), API.PROCESS_TRANSACTION,
                            PayloadType.RESPONSE) || (extraParamsMap != null
                            && (StringUtils.equals((String) extraParamsMap.get(WRAPPER_NAME), SBMOPS_WRAPPER)) && dynamicWrapperUtil
                                .isDynamicWrapperConfigPresent((String) extraParamsMap.get(AGG_MID_WRAPPER),
                                        API.PROCESS_TRANSACTION, PayloadType.RESPONSE)))) {
                LOGGER.info("Sending Respnse through dynamic wrapper");
                try {
                    String mid = null;
                    if (extraParamsMap == null) {
                        EXT_LOGGER.customInfo("extraParamsMap is null getting it from theiaTransactionalRedisUtil");
                        Object object = theiaTransactionalRedisUtil
                                .get(response.getMid() + "#" + response.getOrderId());
                        if (object != null) {
                            extraParamsMap = (Map<String, Object>) object;
                            EXT_LOGGER.customInfo("theiaTransactionalRedisUtil returns extraParamsMap");
                        }
                    }
                    if (StringUtils.equals((String) extraParamsMap.get(WRAPPER_NAME), SBMOPS_WRAPPER)) {
                        mid = (String) extraParamsMap.get(AGG_MID_WRAPPER);
                    } else {
                        mid = response.getMid();
                    }
                    Map<String, Object> merchantResponseParams = wrapperService.wrapResponse(response, mid,
                            API.PROCESS_TRANSACTION);

                    if (response.getMid().equals(ConfigurationUtil.getProperty("emitra.mid"))) {
                        response.setCallbackUrl((String) merchantResponseParams.get("respUrl"));
                        merchantResponseParams = transformResponseFromWrapper(merchantResponseParams,
                                response.getMid(), response.getClientId());

                    } else if (response.getMid().equals(ConfigurationUtil.getProperty("odisha.mid"))) {
                        return getCustomPaymentConfirmationPage(merchantResponseParams, response.getMid());
                    } else if (extraParamsMap != null && extraParamsMap.get("transitionPageRequired") != null
                            && Boolean.parseBoolean((String) extraParamsMap.get("transitionPageRequired"))) {
                        String wrapperTransitionPageEncodedJson = wrapperTransitionPageEncodedJson(merchantResponseParams);
                        String htmlPage = wrapperTransitionHtmlPage(wrapperTransitionPageEncodedJson);
                        /*
                         * String theiaBasePath =
                         * ConfigurationUtil.getProperty("theia.base.path");
                         * htmlPage = htmlPage.replace("{action}", theiaBasePath
                         * + "/dynamic-wrapper") .replace("{json}",
                         * wrapperTransitionPageEncodedJson);
                         */
                        return htmlPage;
                    }
                    StringBuilder sb = getHtmlfromPramsMap(merchantResponseParams);
                    putPRNIfApplicable(response, sb, null);
                    return getFinalHtmlResponse(response, sb);
                } catch (WrapperServiceException e) {
                    throw new TheiaControllerException("Exception in wrapping response");
                }
            } else if (extraParamsMap != null
                    && (StringUtils.isNotEmpty((String) extraParamsMap.get(SHOPIFY_DOMAIN)) || (StringUtils.equals(
                            (String) extraParamsMap.get(WRAPPER_NAME), WIX_WRAPPER)))
                    && dynamicWrapperUtil.isDynamicWrapperConfigPresent(
                            (String) extraParamsMap.get(RESELLER_PARENT_MID), API.PROCESS_TRANSACTION,
                            PayloadType.RESPONSE)) {
                LOGGER.info("Sending Response through dynamic wrapper");
                try {
                    response.setTxnToken(retryServiceHelper.getTxnToken(response.getMid(), response.getOrderId()));
                    Map<String, Object> merchantResponseParams = wrapperService.wrapResponse(response,
                            (String) extraParamsMap.get(RESELLER_PARENT_MID), API.PROCESS_TRANSACTION);
                    if (StringUtils.equals((String) extraParamsMap.get(WRAPPER_NAME), WIX_WRAPPER)) {
                        StringBuilder sb = getHtmlfromPramsMap(merchantResponseParams);
                        String wixCallbackUrl = (String) merchantResponseParams.get("callbackUrl") + "|"
                                + WIX_CALLBACK_ACTION_METHOD;
                        response.setCallbackUrl(wixCallbackUrl);
                        return getFinalHtmlResponse(response, sb);
                    } else {
                        String wrapperTransitionPageEncodedJson = wrapperTransitionPageEncodedJson(merchantResponseParams);
                        String htmlPage = wrapperTransitionHtmlPage(wrapperTransitionPageEncodedJson);
                        String theiaBasePath = ConfigurationUtil.getProperty("theia.base.path");
                        htmlPage = htmlPage.replace("{action}", theiaBasePath + "/dynamic-wrapper").replace("{json}",
                                wrapperTransitionPageEncodedJson);
                        return htmlPage;
                    }
                } catch (WrapperServiceException e) {
                    throw new TheiaControllerException("Exception in wrapping response");
                }
            }
            Map<String, String> paramMap = new TreeMap<>();
            fillTransactionResponseMap(response, strBuilder, paramMap);
        }

        // Added for analysis.
        return getFinalHtmlResponse(response, strBuilder);
    }

    public String wrapperTransitionHtmlPage(String wrapperTransitionPageEncodedJson) {
        String htmlPage = null;
        try {
            htmlPage = ConfigurationUtil.getWrapperTransitionPage();
            htmlPage = htmlPage.replace("PUSH_JSON_DATA", wrapperTransitionPageEncodedJson);
        } catch (Exception e) {
            LOGGER.error("error while generating wrapperTransitionHtmlPage", e);
        }
        return htmlPage;
    }

    private String getIfscCodeFromMapping(String issuingBankCode) {
        if (issuingBankCode == null)
            return null;
        String ifscCode = null;
        try {
            BankMasterDetails bankMasterDetails = bankInfoDataService.getBankInfoData(issuingBankCode);
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetails :: {} for issuingBankCode : {}",
                    bankMasterDetails, issuingBankCode);
            if (bankMasterDetails != null && StringUtils.isNotBlank(bankMasterDetails.getExtIfscCode()))
                ifscCode = bankMasterDetails.getExtIfscCode();
        } catch (Exception e) {
            LOGGER.info("Error occured while fetching ifsc code from mapping for raj nic merchant {} ", e);
        }
        return ifscCode;
    }

    public String wrapperTransitionPageEncodedJson(Map<String, Object> merchantResponseParams) {

        WrapperTransitionPage transitionPage = new WrapperTransitionPage();

        transitionPage.setOrderId((String) merchantResponseParams.get("orderId"));
        transitionPage.setTxnToken((String) merchantResponseParams.get("txnToken"));
        transitionPage.setTxnAmount((String) merchantResponseParams.get("txnAmount"));
        transitionPage.setTxnTime((String) merchantResponseParams.get("txnDate"));

        transitionPage.setMid((String) merchantResponseParams.get("mid"));
        transitionPage.setTxnStatus((String) merchantResponseParams.get("transactionStatus"));
        transitionPage.setCallbackUrl((String) merchantResponseParams.get("callbackUrl"));

        // Whether the trasition page has a dowload Receipt option
        transitionPage.setDownloadReceiptEnable(Boolean.parseBoolean((String) merchantResponseParams
                .get("downloadReceiptEnable")));
        // Whether the trasition page has a print Receipt option
        transitionPage.setPrintReceiptEnable(Boolean.parseBoolean((String) merchantResponseParams
                .get("printReceiptEnable")));
        // Whether the trasition page has a time counter for page expiry then
        // posting to merchat callback
        transitionPage.setTimeCounterEnable(Boolean.parseBoolean((String) merchantResponseParams
                .get("timeCounterEnable")));
        // Value of the page expiry
        if (StringUtils.isNotEmpty((String) merchantResponseParams.get("timeCounter"))) {
            transitionPage.setTimeCounter(Integer.parseInt((String) merchantResponseParams.get("timeCounter")));
        }

        // Field which needs to be posted on merchant callback
        Map<String, String> txnInfo = new HashMap<>();
        String callbackFields = (String) merchantResponseParams.get("callbackFields");
        if (StringUtils.isNotEmpty(callbackFields)) {
            String[] splittedValues = callbackFields.split(Pattern.quote(","));
            for (String splittedValue : splittedValues) {
                txnInfo.put(splittedValue, (String) merchantResponseParams.get(splittedValue));
            }
        }
        transitionPage.setTxnInfo(txnInfo);

        // Field which needs to be displayed on Transition Page
        if (merchantResponseParams.get("displayFields") != null) {
            Map<String, String> displayInfo = new LinkedHashMap<>(); // For
            // Insertion
            // Order
            String displayFields = (String) merchantResponseParams.get("displayFields");
            String[] displaySplittedValues = displayFields.split(Pattern.quote(","));
            for (String displaySplittedValue : displaySplittedValues) {
                String[] splittedDisplaySplittedValue = displaySplittedValue.split(Pattern.quote("|"));
                displayInfo.put(splittedDisplaySplittedValue[0],
                        (String) merchantResponseParams.get(splittedDisplaySplittedValue[1]));
            }
            transitionPage.setDisplayInfo(displayInfo);
        }

        // Merchant Logo taken from merchant config not dynamic wrapper config
        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(transitionPage.getMid());
        MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService.getMerchantInfo(merchantDetailsRequest);
        String merchantLogo1 = (String) merchantResponseParams.get("merchantLogo");
        if (StringUtils.isNotEmpty(merchantLogo1)) {
            String[] merchantLogoSplitted = merchantLogo1.split(Pattern.quote(","));

            // 0th index has logo Url, 1st has height and 2nd has width
            LogoModel merchantLogo = new LogoModel(merchantDetailsResponse.getMerchantImageName(),
                    Integer.parseInt(merchantLogoSplitted[1]), Integer.parseInt(merchantLogoSplitted[2]));
            transitionPage.setMerchantLogo(merchantLogo);
        }

        String secondaryLogo1 = (String) merchantResponseParams.get("secondaryLogo");
        if (StringUtils.isNotEmpty(secondaryLogo1)) {
            String[] secondaryLogoSplitted = secondaryLogo1.split(Pattern.quote(","));

            // 0th index has logo Url, 1st has height and 2nd has width
            LogoModel secondaryLogo = new LogoModel(secondaryLogoSplitted[0],
                    Integer.parseInt(secondaryLogoSplitted[1]), Integer.parseInt(secondaryLogoSplitted[2]));
            transitionPage.setSecondaryLogo(secondaryLogo);
        }

        transitionPage.setMerchantName((String) merchantResponseParams.get("merchantName"));

        if (merchantResponseParams.get("AdditionalInfo5") != null) {
            transitionPage.setPaymentType(PaymentCollectionTypes.findNameByValue((String) merchantResponseParams
                    .get("AdditionalInfo5")));
        }

        String transitionPageJson = null;
        try {
            transitionPageJson = JsonMapper.mapObjectToJson(transitionPage);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception in converting the object to json");
        }
        LOGGER.info("Wrapper transition Page {}", transitionPageJson);

        return new String(Base64.getEncoder().encode((transitionPageJson).getBytes()));
    }

    private void changeResponseforZestMoney(TransactionResponse response) {
        if (response.getMid() == null || response.getOrderId() == null) {
            LOGGER.info("mid or order id null in Transaction response to the merchant!");
            return;
        }
        boolean isZestRequest = false;
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        if (servletRequestAttributes.getRequest() != null
                && ZEST.equals(servletRequestAttributes.getRequest().getParameter(Native.CHANNEL_CODE))) {
            isZestRequest = true;
        }
        List<ChildTransaction> childTransactions = response.getChildTxnList();
        if (childTransactions != null && childTransactions.size() > 1) {
            for (ChildTransaction childTransaction : childTransactions) {
                String bankName = childTransaction.getBankName();
                String gatewayName = childTransaction.getGateway();
                if ((bankName != null && bankName.equals(ZEST)) || (gatewayName != null && gatewayName.equals(ZEST))) {
                    childTransaction.setPaymentMode(EPayMethod.EMI.toString());
                }
            }
        } else if ((response.getBankName() != null && response.getBankName().equals(ZEST))
                || (response.getGateway() != null && response.getGateway().equals(ZEST))) {
            response.setPaymentMode(EPayMethod.EMI.toString());
        } else if (isZestRequest) {
            response.setPaymentMode(EPayMethod.EMI.toString());
        }
    }

    public void fillTransactionResponseMap(TransactionResponse response, StringBuilder strBuilder,
            Map<String, String> paramMap) {
        String mid = response.getMid();
        // NOTE - only for staging team - max life hack - not to deploy on prod
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        EXT_LOGGER.customInfo("checking whether aoa mid in txn status response = {}", mid);
        // if
        // (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))
        // && StringUtils.isNotEmpty(mid)
        // && (mid.equals("216820000004063983655") ||
        // mid.equals("216820000004179111306"))) {
        // mid = aoaUtils.getPgMidForAoaMid(mid);
        // LOGGER.info("new mid for checksum and in response = {}", mid);
        // response.setMid(mid);
        // }
        putOrderId(strBuilder, paramMap, response.getOrderId());
        putMerchantId(response.getMid(), strBuilder, paramMap);
        putTxnId(strBuilder, paramMap, response.getTxnId());
        putAmount(strBuilder, paramMap, response.getTxnAmount());
        putPaymentMode(strBuilder, paramMap, response.getPaymentMode());
        putCurrency(strBuilder, paramMap, response.getCurrency(), response.getMandateType());
        putTxnDate(strBuilder, paramMap, response.getTxnDate());
        putStatus(strBuilder, paramMap, response.getTransactionStatus());
        putResponseCode(strBuilder, paramMap, response.getResponseCode());
        putResponseMessage(strBuilder, paramMap, response.getResponseMsg());
        // putRegionalResponseMessage(strBuilder, paramMap,
        // response.getResponseMsgRegional());
        putMerchUniqueRef(strBuilder, paramMap, response.getMerchUniqueReference());
        putUdf1(strBuilder, paramMap, response.getUdf1());
        putUdf2(strBuilder, paramMap, response.getUdf2());
        putUdf3(strBuilder, paramMap, response.getUdf3());
        putAdditionalInfo(strBuilder, paramMap, response.getAdditionalInfo());
        putGateway(strBuilder, paramMap, response.getGateway());
        putBankTxnId(strBuilder, paramMap, response.getBankTxnId(), response.getMandateType());
        putBinNumber(strBuilder, paramMap, response.getBinNumber(), mid);
        putLastFourDigits(strBuilder, paramMap, response.getLastFourDigits());
        putChildTransactionList(strBuilder, paramMap, response.getChildTxnList());
        putBankResultInfo(strBuilder, paramMap, response.getBankResultInfo());
        putSubsId(strBuilder, paramMap, response.getSubsId());
        putBankName(strBuilder, paramMap, response.getBankName());
        putPromoCode(strBuilder, paramMap, response.getPromoCode());
        putPromoResponseCode(strBuilder, paramMap, response.getPromoResponseCode());
        putPromoApplyResultStatus(strBuilder, paramMap, response.getPromoApplyResultStatus());
        putPRNIfApplicable(response, strBuilder, paramMap);

        // parameters specific to mandate
        putAcceptedRefNo(strBuilder, paramMap, response.getAcceptedRefNo());
        putIsAccepted(strBuilder, paramMap, response.getIsAccepted());
        putRejectedBy(strBuilder, paramMap, response.getRejectedBy());
        putMerchantCustId(strBuilder, paramMap, response.getMerchantCustId());
        putMandateType(strBuilder, paramMap, response.getMandateType());

        // populate cardId which has been populated based on the preference
        // called "SAVED_CARD_ID_SUPPORTED" and would be part of checksum.
        putsavedCardId(strBuilder, paramMap, response.getSavedCardId());
        putRiskInfo(strBuilder, paramMap, response.getRiskInfo());

        boolean isDQRRequest = false;
        if (StringUtils.isNotBlank(response.getRequestType())) {
            isDQRRequest = (response.getRequestType().equalsIgnoreCase(RequestTypes.DYNAMIC_QR) || response
                    .getRequestType().equalsIgnoreCase(RequestTypes.DYNAMIC_QR_2FA)) ? true : false;
        }
        if (StringUtils.isNotBlank(response.getMid())
                && (merchantPreferenceService.isPostConvenienceFeesEnabled(response.getMid())
                        && (merchantPreferenceService.isPcfFeeInfoEnabled(response.getMid()) || (response
                                .isQrIdFlowOnly() && isDQRRequest && ff4jUtils.isFeatureEnabled(
                                THEIA_CHARGE_AMOUNT_IN_PTC_RESPONSE, false))) || merchantPreferenceService
                            .isDynamicFeeMerchant(response.getMid()))) {
            putPcfAmount(strBuilder, paramMap, response.getChargeAmount());
        }
        if (merchantPreferenceService.isReturnPrepaidEnabled(response.getMid())) {
            putPrepaidCard(strBuilder, paramMap, response.getPrepaidCard());
        }
        if (isAnyUPI(response) && StringUtils.isNotBlank(response.getVpa())) {
            putVPA(strBuilder, paramMap, response.getVpa());
        }
        if (StringUtils.isNotBlank(response.getFundSourceVerificationSuccess())) {
            putFundSourceVerificationSuccess(strBuilder, paramMap, response.getFundSourceVerificationSuccess());
        }

        if (response.getCardScheme() != null) {
            strBuilder.append(replace(CARD_SCHEME, response.getCardScheme()));
            paramMap.put(CARD_SCHEME, response.getCardScheme());
        }

        if (ERequestType.NATIVE_MF.name().equals(response.getRequestType())) {
            String tmpMid = (merchantExtendInfoUtils.getKeyFromExtendInfo(response.getMid(), AGG_MID));
            if (StringUtils.isNotEmpty(tmpMid)) {
                mid = tmpMid;
            }
        }

        /*
         * Setting aggMid for aggregator merchant from redis for normal
         * transactions and from payment response in case of payment retries
         * where session is already invalidated
         */
        String txnToken = retryServiceHelper.getTxnToken(mid, response.getOrderId());
        String aggregatorMid = null;
        InitiateTransactionRequestBody orderDetail = null;
        if (StringUtils.isNotBlank(txnToken)) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                aggregatorMid = orderDetail.getAggMid();
                if (StringUtils.isNotBlank(aggregatorMid)
                        && ERequestType.NATIVE_MF.name().equals(orderDetail.getRequestType())) {
                    mid = aggregatorMid;
                }
            }
        }

        if (StringUtils.isBlank(aggregatorMid) && StringUtils.isNotBlank(response.getAggMid())) {
            aggregatorMid = response.getAggMid();
        }
        putTxnPaidTime(strBuilder, paramMap, response.getTxnPaidTime());
        boolean isChecksumEnable = merchantPreferenceService.isChecksumEnabled(mid);

        if (StringUtils.isNotBlank(aggregatorMid) && AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(aggregatorMid)) {
            mid = aggregatorMid;
        }

        if (isChecksumEnable
                && !(ResponseConstants.ResponseCodes.CHECKSUM_FAILURE_CODE.equals(response.getResponseCode()))) {

            putChecksum(strBuilder, paramMap, merchantExtendInfoUtils.getMerchantKey(mid, response.getClientId()));
        }

        // todo Commenting for now , will be fixed and picked in Sprint35
        /*
         * // PGP- 22936 Adding chargeAmount outside checksum to be used by Apps
         * for scan n pay flow if (paramMap.get(ResponseConstants.CHARGE_AMOUNT)
         * == null && StringUtils.isNotBlank(response.getChargeAmount()) &&
         * (StringUtils.isNotBlank(response.getMid()) &&
         * (merchantPreferenceService
         * .isPostConvenienceFeesEnabled(response.getMid()) ||
         * merchantPreferenceService .isDynamicFeeMerchant(response.getMid()))))
         * { putPcfAmount(strBuilder, paramMap, response.getChargeAmount()); }
         */

        String merchantSepcificResponse = getPaytmProperty(TheiaConstant.PaytmPropertyConstants.MERCHANT_SPECIFIC_RESPONSE);
        if (!StringUtils.isEmpty(merchantSepcificResponse)) {
            List<String> merchantSepcificResponseList = stringToList(merchantSepcificResponse, ",");
            if (merchantSepcificResponseList.contains(response.getMid())) {
                putUserEmail(strBuilder, paramMap, response.getUserEmail());
                putUserMobile(strBuilder, paramMap, response.getUserPhone());
                putCustId(strBuilder, paramMap, response.getCustId());
            }
        }
        putMaskedCardNo(strBuilder, paramMap, response.getMaskedCardNo());
        putCardIndexNo(strBuilder, paramMap, response.getCardIndexNo());
        putAdditionalParam(strBuilder, paramMap, response.getAdditionalParam());
        putLinkType(strBuilder, paramMap, response.getLinkType());
        if (StringUtils.isNotBlank(response.getLinkBasedPayment()) && Boolean.valueOf(response.getLinkBasedPayment())) {
            LOGGER.info("Inside fillTransactionResponseMap from TransactionResponse for Link Based Payments");
            putDataForLinkPayment(response, strBuilder, paramMap);
        }
        putDataForLinkPaymentInAppOnlineFlow(response, strBuilder, paramMap, orderDetail);
        // setting splitInfo if present
        if (response.getSplitSettlementInfo() != null) {
            if (ff4JHelper.isFF4JFeatureForMidEnabled(
                    TheiaConstant.ExtraConstants.THEIA_ADD_ESCAPE_CHARACTER_IN_FINAL_HTML_RESPONSE, response.getMid())
                    || (orderDetail != null && orderDetail.getLinkDetailsData() != null && LinkBasedParams.APP
                            .equalsIgnoreCase(orderDetail.getLinkDetailsData().getChannelId()))) {
                strBuilder.append(replace(MERCHANT_SPLIT_SETTLEMENT_INFO,
                        JSONValue.escape(response.getSplitSettlementInfo())));
                paramMap.put(MERCHANT_SPLIT_SETTLEMENT_INFO, JSONValue.escape(response.getSplitSettlementInfo()));
            } else {
                strBuilder.append(replace(MERCHANT_SPLIT_SETTLEMENT_INFO, response.getSplitSettlementInfo()));
                paramMap.put(MERCHANT_SPLIT_SETTLEMENT_INFO, response.getSplitSettlementInfo());
            }
        }

        if (processTransactionUtil.isAddMoneyMerchant(mid)) {
            putCardHash(strBuilder, paramMap, response.getCardHash());
        }

        if (response.isSubsLinkInfo()) {
            LOGGER.info("Inside fillTransactionResponseMap from TransactionResponse for Subscription Link Based Payments");
            putshowLinkPromotion(strBuilder, paramMap);
            putDataForLinkPayment(response, strBuilder, paramMap);
        }
    }

    private void putshowLinkPromotion(StringBuilder strBuilder, Map<String, String> paramMap) {
        strBuilder.append(replace(LinkBasedParams.SHOW_LINK_PROMOTION, FALSE));
        paramMap.put(LinkBasedParams.SHOW_LINK_PROMOTION, FALSE);
    }

    private void putLinkType(StringBuilder strBuilder, Map<String, String> paramMap, String linkType) {
        if (StringUtils.isNotBlank(linkType)) {
            strBuilder.append(replace(ResponseConstants.LINK_TYPE, linkType));
            paramMap.put(ResponseConstants.LINK_TYPE, linkType);
        }
    }

    private void putResponseStatus(StringBuilder strBuilder, Map<String, String> paramMap, String responseStatus) {
        if (StringUtils.isNotBlank(responseStatus)) {
            strBuilder.append(replace(ResponseConstants.RESPONSE_STATUS, responseStatus));
            paramMap.put(ResponseConstants.RESPONSE_STATUS, responseStatus);
        }
    }

    private void putIsAccepted(StringBuilder strBuilder, Map<String, String> paramMap, Boolean isAccepted) {
        if (null != isAccepted) {
            strBuilder.append(replace(ResponseConstants.IS_ACCEPTED, String.valueOf(isAccepted)));
            paramMap.put(ResponseConstants.IS_ACCEPTED, String.valueOf(isAccepted));
        }
    }

    private void putAcceptedRefNo(StringBuilder strBuilder, Map<String, String> paramMap, String acceptedRefNo) {
        if (StringUtils.isNotBlank(acceptedRefNo)) {
            strBuilder.append(replace(ResponseConstants.ACCEPTED_REF_NO, acceptedRefNo));
            paramMap.put(ResponseConstants.ACCEPTED_REF_NO, acceptedRefNo);
        }
    }

    private void putRejectedBy(StringBuilder strBuilder, Map<String, String> paramMap, String rejectedBy) {
        if (StringUtils.isNotBlank(rejectedBy)) {
            strBuilder.append(replace(ResponseConstants.REJECTED_BY, rejectedBy));
            paramMap.put(ResponseConstants.REJECTED_BY, rejectedBy);
        }
    }

    private void putMerchantCustId(StringBuilder strBuilder, Map<String, String> paramMap, String merchantCustId) {
        if (StringUtils.isNotBlank(merchantCustId)) {
            strBuilder.append(replace(ResponseConstants.MERCHANT_CUST_ID, merchantCustId));
            strBuilder.append(replace(ResponseConstants.CUSTOMER_ID, merchantCustId));
            paramMap.put(ResponseConstants.MERCHANT_CUST_ID, merchantCustId);
            paramMap.put(ResponseConstants.CUSTOMER_ID, merchantCustId);
        }
    }

    private void putMandateType(StringBuilder strBuilder, Map<String, String> paramMap, String mandateType) {
        if (StringUtils.isNotBlank(mandateType)) {
            strBuilder.append(replace(ResponseConstants.MANDATE_TYPE, mandateType));
            paramMap.put(ResponseConstants.MANDATE_TYPE, mandateType);
        }
    }

    private void putsavedCardId(StringBuilder strBuilder, Map<String, String> paramMap, String savedCardId) {
        if (StringUtils.isNotBlank(savedCardId)) {
            strBuilder.append(replace(ResponseConstants.SAVED_CARD_ID, savedCardId));
            paramMap.put(ResponseConstants.SAVED_CARD_ID, savedCardId);
        }
    }

    private void putRiskInfo(StringBuilder strBuilder, Map<String, String> paramMap, String riskInfo) {
        if (StringUtils.isNotBlank(riskInfo)) {
            strBuilder.append(replace(ResponseConstants.RISK_INFO, riskInfo));
            paramMap.put(ResponseConstants.RISK_INFO, riskInfo);
        }
    }

    private void putPcfAmount(StringBuilder strBuilder, Map<String, String> paramMap, String pcfAmount) {
        if (StringUtils.isNotBlank(pcfAmount)) {
            strBuilder.append(replace(ResponseConstants.CHARGE_AMOUNT, pcfAmount));
            paramMap.put(ResponseConstants.CHARGE_AMOUNT, pcfAmount);
        }
    }

    private void putDataForSubsLinkPayment(TransactionResponse response, StringBuilder strBuilder,
            Map<String, String> paramMap) {

        if (StringUtils.isNotBlank(response.getTransactionStatus())) {
            strBuilder.append(replace(PAYMENT_STATUS, response.getTransactionStatus()));
            paramMap.put(PAYMENT_STATUS, response.getTransactionStatus());
        }
        putMerchantImage(strBuilder, paramMap, response.getMerchantImage());
        putMerchantName(strBuilder, paramMap, response.getMerchantName());

    }

    private void putDataForLinkPayment(TransactionResponse response, StringBuilder strBuilder,
            Map<String, String> paramMap) {
        putShowViewFlag(strBuilder, paramMap, response.getShowViewFlag());
        putMerchantImage(strBuilder, paramMap, response.getMerchantImage());
        putMerchantName(strBuilder, paramMap, response.getMerchantName());
        putErrorMsg(strBuilder, paramMap, response.getResponseMsg());
        putErrorCode(strBuilder, paramMap, response.getResponseCode());
        if (StringUtils.isNotBlank(response.getLinkBasedPayment())) {
            strBuilder.append(replace(LINK_BASED_PAYMENT, response.getLinkBasedPayment()));
            paramMap.put(LINK_BASED_PAYMENT, response.getLinkBasedPayment());
        }
        if (StringUtils.isNotBlank(response.getOrderId())) {
            strBuilder.append(replace(ORDER_ID, response.getOrderId()));
            paramMap.put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ORDER_ID,
                    response.getOrderId());
        }
        if (StringUtils.isNotBlank(response.getTransactionStatus())) {
            strBuilder.append(replace(PAYMENT_STATUS, response.getTransactionStatus()));
            paramMap.put(PAYMENT_STATUS, response.getTransactionStatus());
        }
        if (StringUtils.isNotBlank(response.getTxnAmount())) {
            strBuilder.append(replace(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.TXN_AMOUNT,
                    response.getTxnAmount()));
            paramMap.put(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.TXN_AMOUNT,
                    response.getTxnAmount());
        }
        if (StringUtils.isNotBlank(response.getTxnDate())) {
            strBuilder.append(replace(TXN_DATE, response.getTxnDate()));
            paramMap.put(TXN_DATE, response.getTxnDate());
        }
        if (StringUtils.isNotBlank(response.getTxnId())) {
            strBuilder.append(replace(TRANSACTION_ID, response.getTxnId()));
            paramMap.put(TRANSACTION_ID, response.getTxnId());
        }
        if (StringUtils.isNotBlank(response.getMid())
                && merchantPreferenceService.isPostConvenienceFeesEnabled(response.getMid())) {
            putPcfAmount(strBuilder, paramMap, response.getChargeAmount());
        }
    }

    private boolean isAnyUPI(TransactionResponse response) {
        return (TheiaConstant.BasicPayOption.UPI.equals(response.getPaymentMode())
                || TheiaConstant.BasicPayOption.UPI_PUSH.equals(response.getPaymentMode()) || TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS
                    .equals(response.getPaymentMode()));
    }

    private void putPRNIfApplicable(TransactionResponse response, StringBuilder strBuilder, Map<String, String> paramMap) {
        if (ExternalTransactionStatus.TXN_SUCCESS.toString().equals(response.getTransactionStatus())
                && prnUtils.checkIfPRNEnabled(response.getMid())) {
            String prnCode = prnUtils.fetchPRN(response.getMid(), response.getTxnId());
            strBuilder.append(replace(ResponseConstants.PRN, prnCode));
            if (paramMap != null) {
                paramMap.put(ResponseConstants.PRN, prnCode);
            }
        }
    }

    public String createFailureMerchantResponse(final HttpServletRequest httpServletRequest) {

        final TransactionInfo transactionInfo = sessionDataService.getTxnInfoFromSession(httpServletRequest);
        final ExtendedInfoRequestBean extendedInfoRequestBean = sessionDataService
                .geExtendedInfoRequestBean(httpServletRequest);
        String kycFlowKey = "KYC_FLOW_" + httpServletRequest.getParameter(KYC_TXN_ID);
        CashierRequest cashierRequest = (CashierRequest) theiaTransactionalRedisUtil.get(kycFlowKey);

        if (isNull(transactionInfo) || isNull(extendedInfoRequestBean)
                || isNull(extendedInfoRequestBean.getCallBackURL()) || isNull(cashierRequest)) {
            return viewResolverService.returnOOPSPage(httpServletRequest);
        }

        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(null, null,
                ExternalTransactionStatus.TXN_FAILURE.toString());

        String responseMsg = null;

        if (!isNull(responseCodeDetails)) {
            responseMsg = responseCodeUtil.getResponseMsg(responseCodeDetails);
        }

        String maskedCardNumber = null;
        String cardIndexNumber = null;

        if (!isNull(cashierRequest) || !isNull(cashierRequest.getPaymentRequest())
                || !isNull(isNull(cashierRequest.getPaymentRequest().getPayBillOptions()))) {
            maskedCardNumber = cashierRequest.getPaymentRequest().getPayBillOptions().getMaskedCardNo();
            cardIndexNumber = cashierRequest.getPaymentRequest().getPayBillOptions().getCardIndexNo();
        }

        String mid = transactionInfo.getMid();
        String orderId = transactionInfo.getOrderId();
        String txnAmount = transactionInfo.getTxnAmount();
        String callbackUrl = extendedInfoRequestBean.getCallBackURL();
        String txnId = transactionInfo.getTxnId();
        String responseCode = "810";
        String custId = transactionInfo.getCustID();
        String txnDate = cashierRequest.getTransCreatedtime();
        String status = "FAILURE";
        String userEmail = transactionInfo.getEmailId();
        String userMobile = transactionInfo.getMobileno();
        /* Merchant Client ID needed to calculate resposne checksum */
        String clientId = extendedInfoRequestBean.getClientId();

        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> paramMap = new TreeMap<>();

        putMerchantId(mid, stringBuilder, paramMap);
        putOrderId(stringBuilder, paramMap, orderId);
        putAmount(stringBuilder, paramMap, txnAmount);
        putTxnId(stringBuilder, paramMap, txnId);
        putTxnDate(stringBuilder, paramMap, txnDate);
        putStatus(stringBuilder, paramMap, status);
        putMaskedCardNo(stringBuilder, paramMap, maskedCardNumber);
        putCardIndexNo(stringBuilder, paramMap, cardIndexNumber);
        putCustId(stringBuilder, paramMap, custId);
        putResponseCode(stringBuilder, paramMap, responseCode);
        putResponseMessage(stringBuilder, paramMap, responseMsg);

        if (merchantPreferenceService.isChecksumEnabled(mid)) {
            putChecksum(stringBuilder, paramMap, merchantExtendInfoUtils.getMerchantKey(mid, clientId));
        }

        String merchantSepcificResponse = getPaytmProperty(TheiaConstant.PaytmPropertyConstants.MERCHANT_SPECIFIC_RESPONSE);
        if (!StringUtils.isEmpty(merchantSepcificResponse)) {
            List<String> merchantSepcificResponseList = stringToList(merchantSepcificResponse, ",");
            if (merchantSepcificResponseList.contains(mid)) {
                putUserEmail(stringBuilder, paramMap, userEmail);
                putUserEmail(stringBuilder, paramMap, userMobile);
            }
        }

        return getFinalHtmlResponse(callbackUrl, stringBuilder, null, null, null);
    }

    private <T> boolean isNull(T object) {

        return null == object;
    }

    public StringBuilder getHtmlfromPramsMap(Map<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty())
            return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
            Object val = entry.getValue();
            if (val == null)
                continue;
            String stringVal = null;
            if (entry.getValue() instanceof String) {
                stringVal = val.toString();
            } else {
                try {
                    stringVal = JSONUtils.toJsonString(val);
                } catch (IOException e) {
                    LOGGER.error("Exception in converting value to json String");
                    stringVal = String.valueOf(val);
                }
            }
            if (StringUtils.isEmpty(stringVal))
                continue;
            stringBuilder.append(replace(entry.getKey(), stringVal));
        }
        return stringBuilder;
    }

    private void putPromoApplyResultStatus(StringBuilder strBuilder, Map<String, String> paramMap,
            String promoApplyResultStatus) {
        if (StringUtils.isNotBlank(promoApplyResultStatus)) {
            strBuilder.append(replace(ResponseConstants.PROMO_STATUS, promoApplyResultStatus));
            paramMap.put(ResponseConstants.PROMO_STATUS, promoApplyResultStatus);
        }
    }

    private void putPromoResponseCode(StringBuilder strBuilder, Map<String, String> paramMap, String promoResponseCode) {
        if (StringUtils.isNotBlank(promoResponseCode)) {
            strBuilder.append(replace(ResponseConstants.PROMO_RESPCODE, promoResponseCode));
            paramMap.put(ResponseConstants.PROMO_RESPCODE, promoResponseCode);
        }
    }

    private void putPromoCode(StringBuilder strBuilder, Map<String, String> paramMap, String promoCode) {
        if (StringUtils.isNotBlank(promoCode)) {
            strBuilder.append(replace(ResponseConstants.PROMO_CAMP_ID, promoCode));
            paramMap.put(ResponseConstants.PROMO_CAMP_ID, promoCode);
        }
    }

    private void putChecksum(StringBuilder strBuilder, Map<String, String> paramMap, String merchantKey) {
        try {
            String checksum = ValidateChecksum.getInstance().getRespCheckSumValue(merchantKey, paramMap)
                    .get("CHECKSUMHASH");
            if (StringUtils.isNotBlank(checksum)) {
                strBuilder.append(replace(ResponseConstants.CHECKSUM, checksum));
                paramMap.put(ResponseConstants.CHECKSUM, checksum);
            }
        } catch (Exception e) {
            LOGGER.error("Exception Occurred while calculating response checksum", e);
        }
    }

    private void putChildTransactionList(StringBuilder strBuilder, Map<String, String> paramMap,
            List<ChildTransaction> childTxnList) {
        try {
            if ((childTxnList != null) && !childTxnList.isEmpty()) {
                String childTxnListString = JsonMapper.mapObjectToJson(childTxnList);
                strBuilder.append(replace(ResponseConstants.CHILDTXNLIST, childTxnListString));
                paramMap.put(ResponseConstants.CHILDTXNLIST, childTxnListString);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred , ", e);
        }
    }

    private void putBankResultInfo(StringBuilder strBuilder, Map<String, String> paramMap, BankResultInfo bankResultInfo) {
        try {
            if (bankResultInfo != null) {
                String bankResultInfoString = JsonMapper.mapObjectToJson(bankResultInfo);
                strBuilder.append(replace(ResponseConstants.BANK_RESULT_INFO, bankResultInfoString));
                paramMap.put(ResponseConstants.BANK_RESULT_INFO, bankResultInfoString);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred , ", e);
        }
    }

    private void putResponseCode(StringBuilder strBuilder, Map<String, String> paramMap, String responseCode) {
        if (StringUtils.isNotBlank(responseCode)) {
            strBuilder.append(replace(ResponseConstants.RESPONSE_CODE, responseCode));
            paramMap.put(ResponseConstants.RESPONSE_CODE, responseCode);
        }
    }

    private void putResponseMessage(StringBuilder strBuilder, Map<String, String> paramMap, String respMsg) {

        if (respMsg == null) {
            respMsg = StringUtils.EMPTY;
        } else {
            respMsg = respMsg.replaceAll("'", " ");
        }
        strBuilder.append(replace(ResponseConstants.RESPONSE_MSG, respMsg));
        paramMap.put(ResponseConstants.RESPONSE_MSG, respMsg);
    }

    // private void putRegionalResponseMessage(StringBuilder strBuilder,
    // Map<String, String> paramMap, String respMsgRegional) {
    //
    // if (respMsgRegional == null) {
    // LOGGER.info("respMsgRegional found as null");
    // respMsgRegional = StringUtils.EMPTY;
    // } else {
    // LOGGER.info("Replacing singleQuote with space in respMsgRegional - " +
    // respMsgRegional);
    // respMsgRegional = respMsgRegional.replaceAll("'", " ");
    // }
    // strBuilder.append(replace(ResponseConstants.RESPONSE_MSG_REGIONAL,
    // respMsgRegional));
    // paramMap.put(ResponseConstants.RESPONSE_MSG_REGIONAL, respMsgRegional);
    // }

    private String getResponseMessageFromCode(String responseCode) {
        ResponseCodeDetails responseCodeDetails;
        try {
            responseCodeDetails = responseCodeService.getPaytmResponseCodeDetails(responseCode);
            EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for responseCode : {}",
                    responseCodeDetails, responseCode);
            if (responseCodeDetails != null) {
                return StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                        .getDisplayMessage() : responseCodeDetails.getRemark();
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Exception occurred while fetching ResponseCodeDetails", e);
        }
        return null;
    }

    private void putSubsId(StringBuilder strBuilder, Map<String, String> paramMap, String subsId) {
        if (StringUtils.isNotBlank(subsId)) {
            strBuilder.append(replace(ResponseConstants.SUBS_ID, subsId));
            paramMap.put(ResponseConstants.SUBS_ID, subsId);
        }
    }

    private void putBankName(StringBuilder strBuilder, Map<String, String> paramMap, String bankName) {
        if (StringUtils.isNotBlank(bankName)) {
            strBuilder.append(replace(ResponseConstants.BANKNAME, bankName));
            paramMap.put(ResponseConstants.BANKNAME, bankName);
        }
    }

    private void putMerchUniqueRef(StringBuilder strBuilder, Map<String, String> paramMap, String merchUniqueRef) {
        if (StringUtils.isNotBlank(merchUniqueRef)) {
            strBuilder.append(replace(ResponseConstants.MERCH_UNQ_REF, merchUniqueRef));
            paramMap.put(ResponseConstants.MERCH_UNQ_REF, merchUniqueRef);
        }
    }

    private void putUdf1(StringBuilder strBuilder, Map<String, String> paramMap, String udf1) {
        if (StringUtils.isNotBlank(udf1)) {
            strBuilder.append(replace(ResponseConstants.UDF_1, udf1));
            paramMap.put(ResponseConstants.UDF_1, udf1);
        }
    }

    private void putUdf2(StringBuilder strBuilder, Map<String, String> paramMap, String udf2) {
        if (StringUtils.isNotBlank(udf2)) {
            strBuilder.append(replace(ResponseConstants.UDF_2, udf2));
            paramMap.put(ResponseConstants.UDF_2, udf2);
        }
    }

    private void putUdf3(StringBuilder strBuilder, Map<String, String> paramMap, String udf3) {
        if (StringUtils.isNotBlank(udf3)) {
            strBuilder.append(replace(ResponseConstants.UDF_3, udf3));
            paramMap.put(ResponseConstants.UDF_3, udf3);
        }
    }

    private void putAdditionalInfo(StringBuilder strBuilder, Map<String, String> paramMap, String additionalInfo) {
        if (StringUtils.isNotBlank(additionalInfo)) {
            strBuilder.append(replace(ResponseConstants.ADDITIONAL_INFO, additionalInfo));
            paramMap.put(ResponseConstants.ADDITIONAL_INFO, additionalInfo);
        }
    }

    // bankTxnId would be populated only if the flow is not of mandate creation.
    private void putBankTxnId(StringBuilder strBuilder, Map<String, String> paramMap, String bankTxnId,
            String mandateType) {
        if (StringUtils.isBlank(mandateType)) {
            if (bankTxnId == null) {
                bankTxnId = StringUtils.EMPTY;
            }

            strBuilder.append(replace(ResponseConstants.BANKTXNID, bankTxnId));
            paramMap.put(ResponseConstants.BANKTXNID, bankTxnId);
        }
    }

    private void putBinNumber(StringBuilder strBuilder, Map<String, String> paramMap, String binNumber, String mid) {
        if (StringUtils.isNotBlank(binNumber)
                && ff4jUtils.isFeatureEnabledOnMid(mid, "theia.enableBinInResponse", false)) {
            strBuilder.append(replace(ResponseConstants.BIN, binNumber));
            paramMap.put(ResponseConstants.BIN, binNumber);
        }
    }

    private void putLastFourDigits(StringBuilder strBuilder, Map<String, String> paramMap, String lastFourDigits) {
        if (StringUtils.isNotBlank(lastFourDigits)) {
            strBuilder.append(replace(ResponseConstants.LASTFOURDIGITS, lastFourDigits));
            paramMap.put(ResponseConstants.LASTFOURDIGITS, lastFourDigits);
        }
    }

    private void putGateway(StringBuilder strBuilder, Map<String, String> paramMap, String gateway) {
        if (StringUtils.isNotBlank(gateway)) {
            strBuilder.append(replace(ResponseConstants.GATEWAY, gateway));
            paramMap.put(ResponseConstants.GATEWAY, gateway);
        }
    }

    private void putStatus(StringBuilder strBuilder, Map<String, String> paramMap, String status) {
        if (StringUtils.isNotBlank(status)) {
            strBuilder.append(replace(ResponseConstants.STATUS, status));
            paramMap.put(ResponseConstants.STATUS, status);
        }
    }

    private void putPrepaidCard(StringBuilder strBuilder, Map<String, String> paramMap, String prepaidCard) {
        if (StringUtils.isNotBlank(prepaidCard)) {
            strBuilder.append(replace(ResponseConstants.PREPAID_CARD, prepaidCard));
            paramMap.put(ResponseConstants.PREPAID_CARD, prepaidCard);
        }
    }

    @SuppressWarnings("unused")
    private void putCustId(StringBuilder strBuilder, Map<String, String> paramMap, String custId) {
        if (StringUtils.isNotBlank(custId)) {
            strBuilder.append(replace(ResponseConstants.CUSTOMER_ID, custId));
            paramMap.put(ResponseConstants.CUSTOMER_ID, custId);
        }
    }

    private void putTxnDate(StringBuilder strBuilder, Map<String, String> paramMap, String txnDate) {
        if (StringUtils.isNotBlank(txnDate)) {
            strBuilder.append(replace(ResponseConstants.TRANSACTION_DATE, txnDate));
            paramMap.put(ResponseConstants.TRANSACTION_DATE, txnDate);
        }
    }

    // currency would not be added in case it is the response for mandate
    // creation flow
    private void putCurrency(StringBuilder strBuilder, Map<String, String> paramMap, String currency, String mandateType) {
        if (StringUtils.isBlank(mandateType)) {
            if (StringUtils.isNotBlank(currency)) {
                strBuilder.append(replace(ResponseConstants.CURRENCY, currency));
                paramMap.put(ResponseConstants.CURRENCY, currency);
            }
        }
    }

    private void putPaymentMode(StringBuilder strBuilder, Map<String, String> paramMap, String paymentMode) {
        if (StringUtils.isNotBlank(paymentMode)) {
            strBuilder.append(replace(ResponseConstants.PAYMENT_MODE, paymentMode));
            paramMap.put(ResponseConstants.PAYMENT_MODE, paymentMode);
        }
    }

    private void putShowViewFlag(StringBuilder strBuilder, Map<String, String> paramMap, String showViewFlag) {
        if (StringUtils.isNotBlank(showViewFlag)) {
            strBuilder.append(replace(SHOW_VIEW_FLAG, showViewFlag));
            paramMap.put(SHOW_VIEW_FLAG, showViewFlag);
        }
    }

    private void putMerchantName(StringBuilder strBuilder, Map<String, String> paramMap, String merchantName) {
        if (StringUtils.isNotBlank(merchantName)) {
            strBuilder.append(replace(ExtraConstants.MERCHANT_NAME, merchantName));
            paramMap.put(ExtraConstants.MERCHANT_NAME, merchantName);
        }
    }

    private void putMerchantImage(StringBuilder strBuilder, Map<String, String> paramMap, String merchantImage) {
        if (StringUtils.isNotBlank(merchantImage)) {
            strBuilder.append(replace(MERCHANT_IMAGE, merchantImage));
            paramMap.put(MERCHANT_IMAGE, merchantImage);
        }
    }

    private void putErrorMsg(StringBuilder strBuilder, Map<String, String> paramMap, String errorMsg) {
        if (StringUtils.isNotBlank(errorMsg)) {
            strBuilder.append(replace(ERROR_MESSAGE, errorMsg));
            paramMap.put(ERROR_MESSAGE, errorMsg);
        }
    }

    private void putErrorCode(StringBuilder strBuilder, Map<String, String> paramMap, String errorCode) {
        if (StringUtils.isNotBlank(errorCode)) {
            strBuilder.append(replace(ERROR_CODE, errorCode));
            paramMap.put(ERROR_CODE, errorCode);
        }
    }

    private void putAmount(StringBuilder strBuilder, Map<String, String> paramMap, String txnAmount) {
        if (StringUtils.isNotBlank(txnAmount)) {
            strBuilder.append(replace(ResponseConstants.AMOUNT, txnAmount));
            paramMap.put(ResponseConstants.AMOUNT, txnAmount);
        }
    }

    private void putTxnId(StringBuilder strBuilder, Map<String, String> paramMap, String txnId) {
        if (StringUtils.isNotBlank(txnId)) {
            strBuilder.append(replace(ResponseConstants.TRANSACTION_ID, txnId));
            paramMap.put(ResponseConstants.TRANSACTION_ID, txnId);
        }
    }

    private void putMerchantId(String merchantId, StringBuilder strBuilder, Map<String, String> paramMap) {
        if (StringUtils.isNotBlank(merchantId)) {
            strBuilder.append(replace(ResponseConstants.M_ID, merchantId));
            paramMap.put(ResponseConstants.M_ID, merchantId);
        }
    }

    private void putOrderId(StringBuilder strBuilder, Map<String, String> paramMap, String orderId) {
        if (StringUtils.isNotBlank(orderId)) {
            strBuilder.append(replace(ResponseConstants.ORDER_ID, orderId));
            paramMap.put(ResponseConstants.ORDER_ID, orderId);
        }
    }

    private String getMerchUniqueReference(CashierTransactionStatus transactionStatus,
            CashierPaymentStatus paymentStatus) {
        String merchantUniqueRef = transactionStatus.getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF);
        if (StringUtils.isNotEmpty(merchantUniqueRef)) {
            return merchantUniqueRef;
        }
        return paymentStatus.getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF);
    }

    public boolean getIsLinkInvoicePayment(CashierTransactionStatus transactionStatus) {
        String isLinkInvoice = transactionStatus.getExtendInfo().get(ExtendedInfoKeys.IS_LINK_INVOICE_PAYMENT);
        if (StringUtils.isNotEmpty(isLinkInvoice)) {
            return Boolean.valueOf(isLinkInvoice);
        }
        return false;
    }

    public boolean getIsLinkInvoicePayment(QueryTransactionStatus transactionStatus) {
        String isLinkInvoice = transactionStatus.getExtendInfo().get(ExtendedInfoKeys.IS_LINK_INVOICE_PAYMENT);
        if (StringUtils.isNotEmpty(isLinkInvoice)) {
            return Boolean.valueOf(isLinkInvoice);
        }
        return false;
    }

    private String getMerchUniqueReference(QueryTransactionStatus transactionStatus) {
        if (!transactionStatus.getExtendInfo().isEmpty()
                && null != transactionStatus.getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF)) {
            return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF);
        }
        return getMerchantUniqueReference(transactionStatus.getPaymentViews());

    }

    private String getMerchantUniqueReference(List<PaymentView> paymentViews) {
        if (!CollectionUtils.isEmpty(paymentViews) && null != paymentViews.get(0).getPayRequestExtendInfo()) {
            return paymentViews.get(0).getPayRequestExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF);
        }
        return null;
    }

    private String getMerchUniqueReference(CashierFundOrderStatus fundOrderStatus, CashierPaymentStatus paymentStatus) {
        if (fundOrderStatus != null && MapUtils.isNotEmpty(fundOrderStatus.getExtendInfo())
                && null != fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF)) {
            return fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF);
        }
        return paymentStatus.getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF);
    }

    private String getUdf1(CashierTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_1);
    }

    private String getUdf1(QueryTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        if (!transactionStatus.getExtendInfo().isEmpty()) {
            return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_1);
        }
        return null;

    }

    private String getUdf1(CashierFundOrderStatus fundOrderStatus) {
        if (fundOrderStatus == null || MapUtils.isEmpty(fundOrderStatus.getExtendInfo()))
            return null;
        return fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.UDF_1);
    }

    private String getUdf2(CashierTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_2);
    }

    private String getUdf2(QueryTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        if (!transactionStatus.getExtendInfo().isEmpty()) {
            return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_2);
        }
        return null;

    }

    private String getUdf2(CashierFundOrderStatus fundOrderStatus) {
        if (fundOrderStatus == null || MapUtils.isEmpty(fundOrderStatus.getExtendInfo()))
            return null;
        return fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.UDF_2);
    }

    private String getUdf3(CashierTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_3);
    }

    private String getUdf3(QueryTransactionStatus transactionStatus) {
        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;
        if (!transactionStatus.getExtendInfo().isEmpty()) {
            return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.UDF_3);
        }
        return null;

    }

    private String getUdf3(CashierFundOrderStatus fundOrderStatus) {
        if (fundOrderStatus == null || MapUtils.isEmpty(fundOrderStatus.getExtendInfo()))
            return null;
        return fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.UDF_3);
    }

    private String getAdditionalInfo(CashierTransactionStatus transactionStatus) {

        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;

        return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.ADDITIONAL_INFO);
    }

    private String getAdditionalInfo(QueryTransactionStatus transactionStatus) {

        if (transactionStatus == null || transactionStatus.getExtendInfo() == null)
            return null;

        if (!transactionStatus.getExtendInfo().isEmpty()) {
            return transactionStatus.getExtendInfo().get(ExtendedInfoKeys.ADDITIONAL_INFO);
        }
        return null;

    }

    private String getAdditionalInfo(CashierFundOrderStatus fundOrderStatus) {
        if (fundOrderStatus == null || MapUtils.isEmpty(fundOrderStatus.getExtendInfo()))
            return null;
        return fundOrderStatus.getExtendInfo().get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO);
    }

    private String replace(String name, String value) {
        StringBuilder str = new StringBuilder(INPUT_FORM_TEMPLATE);
        if ((name != null) && (value != null)) {
            int index = str.indexOf(NAME_TEMPLATE);
            if (index >= 0) {
                str.replace(index, index + NAME_TEMPLATE.length(), name);
            }
            index = str.indexOf(VALUE_TEMPLATE);
            if (index >= 0) {
                str.replace(index, index + VALUE_TEMPLATE.length(), value);
            }
            return str.toString();
        }
        return "";
    }

    private String replace(String htmlfile, String placeHolder, String value) {
        StringBuilder str = new StringBuilder(htmlfile);
        if ((placeHolder != null) && (value != null)) {
            int index = str.indexOf(placeHolder);
            if (index >= 0) {
                str.replace(index, index + placeHolder.length(), value);
            }
        }
        return str.toString();
    }

    private String getCallbackUrl(Map<String, String> extendInfo, ExternalTransactionStatus externalTransactionStatus,
            Map<String, String> paymentExtndInfo) {
        String callbackUrl;
        /*
         * set callback url as merchant's callback when txnStatus API is hit
         * from UI and has workflow as checkout in header otherwise set dummy
         * url for checkoutjs flow
         */
        String paytmMerchantId = extendInfo.get(EXTENDED_INFO_PAYTM_MERCHANT_ID);
        if (extendInfo.get(ACTUAL_MID) != null) {
            paytmMerchantId = extendInfo.get(ACTUAL_MID);
        }
        String txnToken = retryServiceHelper.getTxnToken(paytmMerchantId, extendInfo.get(EXTENDED_INFO_ORDER_ID));
        NativePaymentRequestBody nativePaymentRequestBody = null;
        if (StringUtils.isNotBlank(txnToken)) {
            nativePaymentRequestBody = nativeSessionUtil.getRetryDataAndMerchantConfigForCheckOutJs(txnToken);
        }
        if (MapUtils.isNotEmpty(paymentExtndInfo) && CHECKOUT.equals(paymentExtndInfo.get(ExtraConstants.WORKFLOW))
                && !CHECKOUT.equals(extendInfo.get(HEADER_WORKFLOW))) {
            LOGGER.info("Setting dummy callback Url for CheckOut Js flow");
            callbackUrl = ConfigurationUtil.getProperty(CHECKOUT_JS_STATIC_CALLBACK_URL)
                    + extendInfo.get(EXTENDED_INFO_ORDER_ID) + "&MID=" + paytmMerchantId;
        } else {
            callbackUrl = getCallbackUrlFromExtendInfo(extendInfo, externalTransactionStatus);
            /**
             * This is done for preAuth Cases where callback Url is not set in
             * create order as it is done on payment service
             */
            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = paymentExtndInfo.get(ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
            }
        }

        if (StringUtils.isBlank(callbackUrl)) {
            throw new TheiaControllerException("Could not fetch merchant callback URL");
        }
        return callbackUrl;
    }

    private String getCallbackUrl(Map<String, String> extendInfo) {
        return getCallbackUrlFromExtendInfo(extendInfo);
    }

    /**
     * This is done for preAuth Cases where callback Url is not set in create
     * order as it is done on payment service
     *
     * @param orderExtendInfo
     * @param paymentExtendInfo
     * @return
     */
    private String getCallbackUrl(Map<String, String> orderExtendInfo, Map<String, String> paymentExtendInfo) {
        if (!CollectionUtils.isEmpty(orderExtendInfo)) {
            String appInvokeCallBackURL = getAppInvokeCallbackUrl(orderExtendInfo);
            if (StringUtils.isNotBlank(appInvokeCallBackURL)) {
                return appInvokeCallBackURL;
            }
            String callbackUrl = orderExtendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
            if (StringUtils.isBlank(callbackUrl) && !CollectionUtils.isEmpty(paymentExtendInfo)) {
                callbackUrl = paymentExtendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
            }
            if (StringUtils.isNotBlank(callbackUrl)) {
                return callbackUrl;
            }
        }
        LOGGER.error("Could not fetch callback URL");
        throw new TheiaControllerException("Could not fetch merchant callback URL");
    }

    private String getCallbackUrlFromExtendInfo(Map<String, String> extendInfo) {
        if (!extendInfo.isEmpty()) {
            String callbackUrl = extendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
            if (StringUtils.isNotBlank(callbackUrl)) {
                return callbackUrl;
            }
        }

        LOGGER.error("Could not fetch callback URL");
        throw new TheiaControllerException("Could not fetch merchant callback URL");
    }

    public String getCallbackUrlFromExtendInfo(Map<String, String> extendInfo,
            ExternalTransactionStatus externalTransactionStatus) {
        if (!extendInfo.isEmpty()) {
            String callbackUrl = extendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
            String statusCallbackUrl = getStatusCallbackUrl(extendInfo, externalTransactionStatus);
            String appInvokeCallBackURL = getAppInvokeCallbackUrl(extendInfo);
            if (StringUtils.isNotBlank(appInvokeCallBackURL)) {
                return appInvokeCallBackURL;
            }
            if (StringUtils.isNotBlank(statusCallbackUrl)) {
                return statusCallbackUrl;
            }
            if (StringUtils.isNotBlank(callbackUrl)) {
                return callbackUrl;
            }
        }

        LOGGER.error("Could not fetch callback URL");
        return StringUtils.EMPTY;
    }

    private String getAppInvokeCallbackUrl(Map<String, String> extendInfo) {
        String paytmMid = extendInfo.get(PAYTM_MERCHANT_ID);
        if (extendInfo.get(RetryConstants.ACTUAL_MID) != null) {
            paytmMid = extendInfo.get(RetryConstants.ACTUAL_MID);
        }
        String appInvokeCallBackURLKey = extendInfo.get(ExtendedInfoPay.MERCHANT_TRANS_ID).concat("_").concat(paytmMid);
        String appInvokeCallBackURL = (String) nativeSessionUtil.getField(appInvokeCallBackURLKey,
                TheiaConstant.ExtraConstants.APP_INVOKE_CALLBACK_URL);
        return appInvokeCallBackURL;
    }

    private String getStatusCallbackUrl(Map<String, String> extendInfo,
            ExternalTransactionStatus externalTransactionStatus) {
        switch (externalTransactionStatus) {
        case PENDING:
        case TXN_ACCEPTED:
            return extendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_PENDING_CALLBACK_URL);
        case TXN_FAILURE:
            return extendInfo.get(ExtraConstants.EXTENDED_INFO_KEY_FAILURE_CALLBACK_URL);
        case TXN_SUCCESS:
            return extendInfo.get(TheiaConstant.ExtraConstants.EXTENDED_INFO_KEY_SUCCESS_CALLBACK_URL);
        default:
            LOGGER.warn("Returning empty callback URl : {}", externalTransactionStatus);
            return StringUtils.EMPTY;
        }
    }

    private String getFinalHtmlResponse(TransactionResponse response, StringBuilder strBuilder) {
        LOGGER.info("Final Response sent to merchant is  : {}", response);
        Map<String, String> metaData = new LinkedHashMap<>();
        metaData.put("eventMsg", "Final Response sent to merchant is  : " + response.getResponseMsg());
        EventUtils.pushTheiaEvents(EventNameEnum.FINAL_TRANSACTION_STATUS, metaData);
        EventUtils.pushTheiaEvents(EventNameEnum.PAYMENT_MODE_WITH_STATUS,
                new ImmutablePair<>(response.getPaymentMode(), response.getTransactionStatus()));
        EventUtils.logResponseCode(V1_PTC, EventNameEnum.TXNINFO_CODE_SENT, response.getResponseCode(),
                response.getResponseMsg());
        pushPtcResultStatusToStatsD(response.getTransactionStatus());
        String retryInfo = null;
        if (response.getRetryInfo() != null && isPaymentRetryInfoEnabled(response)) {
            retryInfo = response.getRetryInfo().toString();
        }
        return getFinalHtmlResponse(response.getCallbackUrl(), strBuilder, response.getOneClickInfo(), retryInfo,
                response);

    }

    public String getFinalHtmlResponse(String callbackUrl, StringBuilder strBuilder, String oneClickInfo,
            String paymentRetryInfo, TransactionResponse response) {
        String htmlFile = htmlFileTemplate;
        String callBackData = strBuilder.toString();

        if (StringUtils.isNotBlank(callbackUrl) && callbackUrl.contains(WIX_CALLBACK_ACTION_METHOD)) {
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_METHODTYPE, "GET");
            callbackUrl = callbackUrl.substring(0, callbackUrl.lastIndexOf("|"));
        } else if (StringUtils.isNotBlank(callbackUrl) && callbackUrl.contains(SEND_AS_GET_REQUEST_PARAM)) {
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_METHODTYPE, "GET");
        } else
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_METHODTYPE, "POST");
        htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_POSTPARAMS, callBackData);
        htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_RESPURL, callbackUrl);
        if (StringUtils.isNotBlank(oneClickInfo)) {
            String oneClick = replace("oneClickInfo", oneClickInfo);
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_oneClickInfo, oneClick);
        } else {
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_oneClickInfo, "");
        }
        if (StringUtils.isNotBlank(paymentRetryInfo)) {
            String retryInfo = replace("retryInfo", paymentRetryInfo);
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_retryInfo, retryInfo);
        } else {
            htmlFile = replace(htmlFile, ResponseConstants.RESPONSE_KEY_retryInfo, "");
        }
        LOGGER.info("Callback data send to merchant : {} at callBackUrl: {}", callBackData, callbackUrl);

        if (StringUtils.isBlank(callbackUrl) || callbackUrl.contains("{url}")) {

            HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();
            if (httpServletRequest != null) {
                httpServletRequest.setAttribute(TheiaConstant.RequestParams.IS_CALLBACK_URL_PRESENT, false);
            }
        }

        if (Objects.nonNull(response) && ff4jUtils.isFeatureEnabled(THEIA_SUBSCRIPTION_ENABLE_COFT, false)) {
            if ((ERequestType.isSubscriptionRequest(response.getRequestType()) || ERequestType.SUBSCRIBE.getType()
                    .equalsIgnoreCase(response.getRequestType()))
                    && (EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(response.getPaymentMode()) || EPayMethod.DEBIT_CARD
                            .getMethod().equalsIgnoreCase(response.getPaymentMode()))) {
                htmlFile = replace(htmlFile, "{isSubsFlow}", "Please wait as it may take upto 5 seconds");
            }
        } else {
            htmlFile = replace(htmlFile, "{isSubsFlow}", "");
        }

        String enableHTMLResponseLogs = ConfigurationUtil.getProperty(ExtraConstants.ENABLE_HTML_RESPONSE_LOGS_KEY,
                "false");

        if (StringUtils.equalsIgnoreCase(enableHTMLResponseLogs, ExtraConstants.TRUE)) {
            LOGGER.info("Final HTML Response sent to merchant is  :: {}", htmlFile.replaceAll("\n", ""));
        } else {
            LOGGER.debug("Final HTML Response sent to merchant is  :: {}", htmlFile.replaceAll("\n", ""));
        }

        return htmlFile;
    }

    private String getPaymentMode(CashierPaymentStatus paymentStatus) {
        List<PayOption> payOtions = paymentStatus.getPayOptions();
        if ((payOtions == null) || payOtions.isEmpty()) {
            return "";
        }

        if (payOtions.size() == 1) {
            return payOtions.get(0).getPayMethodOldName();
        }
        String paymentMode = EPayMethod.BALANCE.getOldName();
        for (PayOption payOption : payOtions) {
            if (payOption.getExtendInfo() != null) {
                String topupAndPay = payOption.getExtendInfo().get(ExtraConstants.EXTENDED_INFO_KEY_TOPUPANDPAY);
                if (ExtraConstants.EXTENDED_INFO_VALUE_TOPUPANDPAY.equals(topupAndPay)) {
                    return paymentMode;
                }
            }
        }
        return EPayMethod.HYBRID_PAYMENT.getOldName();
    }

    private String getPaymentMode(QueryPaymentStatus paymentStatus, QueryTransactionStatus queryTransactionStatus) {
        List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOtions = paymentStatus.getPayOptions();
        if ((payOtions == null) || (payOtions.isEmpty())) {
            if (queryTransactionStatus != null) {
                payOtions = queryTransactionStatus.getPayOptions();
            }
        }
        if ((payOtions == null) || payOtions.isEmpty()) {
            return "";
        }

        if (payOtions.size() == 1) {
            return payOtions.get(0).getPayMethodOldName();
        }
        String paymentMode = EPayMethod.BALANCE.getOldName();
        for (com.paytm.pgplus.pgproxycommon.models.PayOption payOption : payOtions) {
            if (payOption.getExtendInfo() != null) {
                String topupAndPay = payOption.getExtendInfo().get(ExtraConstants.EXTENDED_INFO_KEY_TOPUPANDPAY);
                if (ExtraConstants.EXTENDED_INFO_VALUE_TOPUPANDPAY.equals(topupAndPay)) {
                    return paymentMode;
                }
            }
        }
        return EPayMethod.HYBRID_PAYMENT.getOldName();

    }

    private String getTransactionAmount(CashierPaymentStatus paymentStatus) {
        return AmountUtils.getTransactionAmountInRupee(paymentStatus.getTransAmountValue());
    }

    private String getTransactionAmount(QueryPaymentStatus paymentStatus) {
        return AmountUtils.getTransactionAmountInRupee(paymentStatus.getTransAmountValue());
    }

    public String generateResponseForSeamless(WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData) {
        if (workFlowResponseBean.isPaymentDone()) {
            return generateMerchantReponseForSeamless(workFlowResponseBean, requestData);
        }
        return getBankPage(workFlowResponseBean, requestData);
    }

    public String generateResponseForPaytmExpress(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        if (workFlowResponseBean.isPaymentDone()) {
            return generateMerchantReponseForPaytmExpress(workFlowResponseBean, requestData);
        }
        return getBankPage(workFlowResponseBean, requestData);
    }

    private String generateMerchantReponseForPaytmExpress(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        TransactionResponse response = new TransactionResponse();
        response.setMid(requestData.getMid());
        response.setOrderId(workFlowResponseBean.getQueryTransactionStatus().getMerchantTransId());
        response.setTxnId(workFlowResponseBean.getQueryTransactionStatus().getAcquirementId());
        response.setTxnAmount(getTransactionAmount(workFlowResponseBean.getQueryPaymentStatus()));
        response.setPaymentMode(getPaymentMode(workFlowResponseBean.getQueryPaymentStatus(),
                workFlowResponseBean.getQueryTransactionStatus()));
        response.setCurrency(workFlowResponseBean.getQueryPaymentStatus().getTransAmountCurrencyType());
        if (workFlowResponseBean.getQueryTransactionStatus().getCreatedTime() != null) {
            response.setTxnDate(DateUtils.format(workFlowResponseBean.getQueryTransactionStatus().getCreatedTime()));
        }
        if (workFlowResponseBean.getQueryTransactionStatus().getBuyerExternalUserId() != null) {
            response.setCustId(workFlowResponseBean.getQueryTransactionStatus().getBuyerExternalUserId());
        }
        response.setTransactionStatus(MapperUtils.getTransactionStatusForResponse(
                workFlowResponseBean.getQueryTransactionStatus(), workFlowResponseBean.getQueryPaymentStatus()));

        String instErrorCode = workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode();

        // Set Response-Code and Response-Message
        responseCodeUtil.setRespMsgeAndCode(response, instErrorCode, null);

        response.setCallbackUrl(getCallbackUrl(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo(),
                ExternalTransactionStatus.valueOf(response.getTransactionStatus()), workFlowResponseBean
                        .getQueryPaymentStatus().getExtendInfo()));
        response.setMerchUniqueReference(getMerchUniqueReference(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf1(getUdf1(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf2(getUdf2(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf3(getUdf3(workFlowResponseBean.getQueryTransactionStatus()));
        response.setAdditionalInfo(getAdditionalInfo(workFlowResponseBean.getQueryTransactionStatus()));

        setPrepaidCardInTransactionResponse(workFlowResponseBean, response, requestData);

        if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() > 1)) {
            response.setChildTxnList(getChildTxnList(workFlowResponseBean.getQueryPaymentStatus(),
                    workFlowResponseBean.getQueryTransactionStatus(), requestData.getMid()));
        }
        if (merchantPreferenceService.isBinInResponseEnabled(response.getMid())) {
            if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                    && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() == 1)) {
                if ((PayMethod.CREDIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.DEBIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    response.setCardScheme(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0)
                            .getExtendInfo().get(INST_ID));
                }
                if ((PayMethod.EMI.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.EMI_DC.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    String binNumber = workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        response.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        response.setCardScheme(getCardSchemeForEMI(workFlowResponseBean.getQueryTransactionStatus()));
                }
            }
        }
        response.setExtraParamsMap(CommonUtils.getExtraParamsMapFromExtendedInfo(workFlowResponseBean
                .getQueryTransactionStatus().getExtendInfo()));
        response.setUserEmail(getUserEmailFromExtendInfo(workFlowResponseBean.getQueryTransactionStatus()
                .getExtendInfo()));
        response.setUserPhone(getUserMobileFromExtendInfo(workFlowResponseBean.getQueryTransactionStatus()
                .getExtendInfo()));
        return getFinalHtmlResponse(response);
    }

    public NativeJsonResponse getNativeJsonResponse(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        NativeJsonResponse nativeJsonResponse;
        if (workFlowResponseBean.isPaymentDone()) {
            nativeJsonResponse = getNativeJsonResponseForPaymentDone(workFlowResponseBean, requestData);
        } else if (isVisaSingleClickPayment(workFlowResponseBean)) {
            LOGGER.info("Processing VISA Single Click payment...");
            nativeJsonResponse = visaSingleClickPaymentResponse(workFlowResponseBean, requestData);
        } else {
            nativeJsonResponse = getNativeJsonBankForm(workFlowResponseBean, requestData, workFlowRequestBean);
        }
        if (nativeJsonResponse != null && nativeJsonResponse.getBody() != null) {
            addRegionalFieldInPTCResponse(nativeJsonResponse.getBody());
            if (nativeJsonResponse.getBody().getResultInfo() != null) {
                EventUtils.logResponseCode(V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, nativeJsonResponse.getBody()
                        .getResultInfo().getResultCode(), nativeJsonResponse.getBody().getResultInfo().getResultMsg());
            }
            if (nativeJsonResponse.getBody().getTxnInfo() != null) {
                EventUtils.logResponseCode(V1_PTC, EventNameEnum.TXNINFO_CODE_SENT, nativeJsonResponse.getBody()
                        .getTxnInfo().get(RESPCODE), nativeJsonResponse.getBody().getTxnInfo().get(RESPMSG));
            }
        }
        return nativeJsonResponse;
    }

    private NativeJsonResponse visaSingleClickPaymentResponse(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        Map<String, String[]> txnStatusparamMap = new HashMap<>();
        NativeJsonResponse nativeJsonResponse = null;

        txnStatusparamMap.put(CASHIER_REQUEST_ID, new String[] { workFlowResponseBean.getCashierRequestId() });
        txnStatusparamMap.put(TRANS_ID, new String[] { workFlowResponseBean.getTransID() });
        txnStatusparamMap.put(MERCHANT_ID, new String[] { requestData.getMid() });
        txnStatusparamMap.put(PAYMENT_MODE, new String[] { requestData.getPaymentTypeId() });

        ModifiableHttpServletRequest modifiableHttpServletRequest = new ModifiableHttpServletRequest(request,
                txnStatusparamMap);

        Map<String, String> data = transactionStatusServiceImpl.getCashierResponse(modifiableHttpServletRequest);

        try {
            TransactionResponse txnResp = JsonMapper.mapJsonToObject(data.get("transactionResponse"),
                    TransactionResponse.class);

            nativeJsonResponse = generateNativeJsonResponseFromTxnResponse(workFlowResponseBean, txnResp, requestData);

            String oneClickInfo = txnResp.getOneClickInfo();
            if (null != nativeJsonResponse && null != nativeJsonResponse.getBody()
                    && StringUtils.isNotBlank(oneClickInfo)) {
                /*
                 * Know the object from
                 */
                Map<String, String> oneClickContent = JsonMapper.mapJsonToObject(oneClickInfo, Map.class);
                nativeJsonResponse.getBody().setOneClickInfo(oneClickContent);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while mapping oneClickInfo Map to JSON :: {}", e);
        }

        return nativeJsonResponse;
    }

    private Boolean isVisaSingleClickPayment(WorkFlowResponseBean workFlowResponseBean) {

        if (null == workFlowResponseBean || null == workFlowResponseBean.getQueryPaymentStatus()) {
            return Boolean.FALSE;
        }

        String webFormContext = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();

        return StringUtils.equals(webFormContext, ONE_CLICK_PAYMENT);
    }

    private Map<String, String> getDeepLinkInfo(WorkFlowResponseBean workFlowResponseBean, DeepLink deepLink,
            PaymentRequestBean requestData) {
        String deepLinkUrl = deepLink != null ? (StringUtils.isNotBlank(deepLink.getUrl()) ? deepLink.getUrl() : "")
                : "";
        String orderId = requestData.getOrderId();
        String transId = StringUtils.isNotBlank(workFlowResponseBean.getTransID()) ? workFlowResponseBean.getTransID()
                : nativeSessionUtil.getTxnId(requestData.getTxnToken());
        String cashierRequestId = StringUtils.isNotBlank(workFlowResponseBean.getCashierRequestId()) ? workFlowResponseBean
                .getCashierRequestId() : "";
        if (Boolean.parseBoolean(com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(REPLACE_DEEPLINK_REQUIRED,
                "false"))) {
            deepLinkUrl = updatedDeepLink(deepLinkUrl, requestData.getOsType(), requestData.getPspApp());
        }
        Map<String, String> deepLinkInfo = new LinkedHashMap<>();
        deepLinkInfo.put(ResponseConstants.DEEP_LINK, deepLinkUrl);
        deepLinkInfo.put(ResponseConstants.ORDERID, orderId);
        deepLinkInfo.put(ResponseConstants.CASHIER_REQUEST_ID, cashierRequestId);
        deepLinkInfo.put(ResponseConstants.TRANS_ID, transId);
        LOGGER.info("DEEP LINK INFO GENERATED ; {} ", deepLinkInfo.toString());
        return deepLinkInfo;
    }

    private String updatedDeepLink(String deepLinkUrl, String os, String pspApp) {
        if (StringUtils.isBlank(deepLinkUrl)) {
            return deepLinkUrl;
        }
        if (deviceIos.equalsIgnoreCase(os)) {
            String baseurl;
            if (StringUtils.isNotBlank(pspApp)) {
                pspApp = pspApp.trim().toLowerCase();
            } else {
                // default pspApp is paytm
                pspApp = PAYTM_DEEPLINK_CONSTANT;
            }
            switch (pspApp) {
            case PHONEPE_DEEPLINK_CONSTANT:
                baseurl = getDeeplinkBaseUrl(PHONEPE_DEEPLINK_CONSTANT);
                break;
            case GOOGLE_PAY_DEEPLINK_CONSTANT:
                baseurl = getDeeplinkBaseUrl(GOOGLE_PAY_DEEPLINK_CONSTANT);
                break;
            default:
                baseurl = getDeeplinkBaseUrl(PAYTM_DEEPLINK_CONSTANT);
                break;
            }
            // splitting deeplink to extract params
            String[] deeplinkarr = deepLinkUrl.split("\\?");
            StringBuilder sb = new StringBuilder();
            // condition is applied as deeplik should be of type upi://{type}
            // and not handling type as upi://mandate
            if (deeplinkarr.length == 1) {
                LOGGER.info("received deeplink without params");
                sb.append(baseurl);
            } else if (deeplinkarr.length == 2) {
                sb.append(baseurl);
                sb.append(deeplinkarr[1]);
            }
            if (StringUtils.isNotBlank(sb.toString())) {
                deepLinkUrl = sb.toString();
            }
        }
        return deepLinkUrl;
    }

    private String getDeeplinkBaseUrl(String pspApp) {
        String baseurllocation = pspApp;
        baseurllocation += ".deeplink.base.url";
        String baseurl = ConfigurationUtil.getProperty(baseurllocation, PAYTM_DEEPLINK_CONSTANT);
        return baseurl;
    }

    private NativeJsonResponse getNativeJsonBankForm(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {

        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        nativeJsonResponse.setHead(new ResponseHeader());

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        nativeJsonResponse.setBody(body);

        if (null != workFlowResponseBean.getQueryPaymentStatus()) {
            String bankFormJson = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
            if (StringUtils.isNotBlank(bankFormJson)) {
                /*
                 * bankForm got from instaProxy is in JSON
                 */

                body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
                BankForm bankForm = new Gson().fromJson(bankFormJson, BankForm.class);

                if (workFlowRequestBean.isNativeDeepLinkReqd()) {
                    LOGGER.info("Setting deepLink info in response body");
                    DeepLink deepLink = bankForm != null ? bankForm.getDeepLink() : null;
                    body.setDeepLinkInfo(getDeepLinkInfo(workFlowResponseBean, deepLink, requestData));
                    if (StringUtils.isNotBlank(workFlowRequestBean.getPaytmMID())) {
                        if (ff4JHelper.isFF4JFeatureForMidEnabled(THEIA__CHCECKOUT_POLLING_FOR_INTENT,
                                workFlowRequestBean.getPaytmMID())) {
                            setContentForUpiIntentPoll(body, workFlowRequestBean);
                        }
                    }
                    String parser = deepLink.getUrl();
                    String esn = StringUtils.substringBetween(parser, "&tr=", "&");
                    nativeSessionUtil.setPaymentRequestBeanAgainstEsn(esn, requestData, 900);
                } else {
                    setContentForUPIPoll(workFlowRequestBean, bankForm, requestData);
                    nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(workFlowResponseBean,
                            workFlowRequestBean, bankForm);
                    body.setBankForm(bankForm);
                }
                /*
                 * set gateway related info in additionalInfo field in response
                 * along with bank-form
                 */
                if (aoaUtils.isAOAMerchant(requestData.getMid())) {
                    setAdditionalInfoForAOA(workFlowResponseBean, body);
                }
                nativeJsonResponse.setBody(body);
                return nativeJsonResponse;
            }
            LOGGER.error("Could not get NativeJson bankForm");
            return generateMerchantNativeJsonResponsePaymentNotDone(workFlowResponseBean, requestData);
        }
        nativeJsonResponse.getBody().setResultInfo(NativePaymentUtil.resultInfo(ResultCode.FAILED));
        return nativeJsonResponse;
    }

    private void setAdditionalInfoForAOA(WorkFlowResponseBean workFlowResponseBean, NativeJsonResponseBody responseBody) {
        if (responseBody != null) {
            Map<String, String> payExtendInfo = getPayExtendInfo(workFlowResponseBean.getQueryPaymentStatus());
            Map<String, String> additionalInfoMap = getAdditionalInfoMap(payExtendInfo);
            if (MapUtils.isNotEmpty(additionalInfoMap)) {
                if (MapUtils.isEmpty(responseBody.getAdditionalInfo())) {
                    responseBody.setAdditionalInfo(additionalInfoMap);
                    return;
                }
                responseBody.getAdditionalInfo().putAll(additionalInfoMap);
            }
        }
    }

    private Map<String, String> getPayExtendInfo(QueryPaymentStatus queryPaymentStatus) {
        return !CollectionUtils.isEmpty(queryPaymentStatus.getPayOptions()) ? queryPaymentStatus.getPayOptions().get(0)
                .getExtendInfo() : null;
    }

    private Map<String, String> getAdditionalInfoMap(Map<String, String> extendInfo) {
        if (MapUtils.isNotEmpty(extendInfo)) {
            Map<String, String> map = new HashMap<>();
            map.put(ResponseConstants.GATEWAYNAME, getGateway(extendInfo));
            return map;
        }
        return null;
    }

    private void setContentForUpiIntentPoll(NativeJsonResponseBody nativeJsonResponseBody,
            WorkFlowRequestBean workFlowRequestBean) {
        String statusInterval = ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL,
                "30000");
        String statusTimeout = upiInfoSessionUtil.getStatusTimeout(workFlowRequestBean.getPaymentTimeoutInMinsForUpi());
        String upiStatusUrl = getUpiStatusUrl(workFlowRequestBean);
        if (MapUtils.isNotEmpty(nativeJsonResponseBody.getDisplayField())) {
            nativeJsonResponseBody.getDisplayField().put(
                    TheiaConstant.ResponseConstants.UPI_STATUS_TIMEOUT_DISPLAYFIELD, statusTimeout);
            nativeJsonResponseBody.getDisplayField().put(
                    TheiaConstant.ResponseConstants.UPI_STATUS_INTERVAL_DISPLAYFIELD, statusInterval);
            nativeJsonResponseBody.getDisplayField().put(TheiaConstant.ResponseConstants.UPI_STATUS_URL, upiStatusUrl);
        } else {
            Map<String, String> map = new HashMap<>();
            map.put(TheiaConstant.ResponseConstants.UPI_STATUS_TIMEOUT_DISPLAYFIELD, statusTimeout);
            map.put(TheiaConstant.ResponseConstants.UPI_STATUS_INTERVAL_DISPLAYFIELD, statusInterval);
            map.put(TheiaConstant.ResponseConstants.UPI_STATUS_URL, upiStatusUrl);
            nativeJsonResponseBody.setDisplayField(map);
        }
    }

    private void setContentForUPIPoll(WorkFlowRequestBean workFlowRequestBean, BankForm bankForm,
            PaymentRequestBean requestBean) {

        if (!PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            return;
        }
        if (!workFlowRequestBean.isUpiPushExpressSupported() && !workFlowRequestBean.isNativeDeepLinkReqd()) {
            /*
             * This is for UPI collect
             */
            if (bankForm != null) {
                if (bankForm.getRedirectForm() != null) {
                    FormDetail formDetail = bankForm.getRedirectForm();

                    updateUpiPollPageUrlForAddMoneyMerchant(requestBean, formDetail);

                    if (formDetail.getContent() != null) {
                        // formDetail.getContent().clear();
                        formDetail.getContent().put(TheiaConstant.RequestParams.Native.TXN_TOKEN,
                                workFlowRequestBean.getTxnToken());
                        if (requestBean.getTokenType() != null)
                            formDetail.getContent().put(TOKEN_TYPE, requestBean.getTokenType());
                    }
                }
                // set upi direct form for polling txn status on v1/txnStatus
                if (!aoaUtils.isAOAMerchant(requestBean.getMid())
                        && ff4jUtils.isFeatureEnabledOnMid(requestBean.getMid(),
                                TheiaConstant.FF4J.THEIA_USE_V1_TXN_STATUS_FOR_UPI_POLLING, false)) {
                    bankForm.setUpiDirectForm(updateUPIDirectForm(requestBean));
                    String statusInterval = ConfigurationUtil.getProperty(
                            TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL, "30000");
                    String statusTimeout = upiInfoSessionUtil.getStatusTimeout(workFlowRequestBean
                            .getPaymentTimeoutInMinsForUpi());
                    if (MapUtils.isNotEmpty(bankForm.getDisplayField())) {
                        bankForm.getDisplayField().put(TheiaConstant.ResponseConstants.UPI_STATUS_TIMEOUT_DISPLAYFIELD,
                                statusTimeout);
                        bankForm.getDisplayField().put(
                                TheiaConstant.ResponseConstants.UPI_STATUS_INTERVAL_DISPLAYFIELD, statusInterval);
                    } else {
                        Map<String, String> map = new HashMap<>();
                        map.put(TheiaConstant.ResponseConstants.UPI_STATUS_TIMEOUT_DISPLAYFIELD, statusTimeout);
                        map.put(TheiaConstant.ResponseConstants.UPI_STATUS_INTERVAL_DISPLAYFIELD, statusInterval);
                        bankForm.setDisplayField(map);
                    }
                }
            }
        }
    }

    private FormDetail updateUPIDirectForm(PaymentRequestBean requestBean) {
        FormDetail formDetail = new FormDetail();
        formDetail.setHeaders(new HashMap<>());
        formDetail.setEncType(null);
        formDetail.setContent(new HashMap<>());
        String theiaBasePath = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_PATH);
        if (StringUtils.isNotBlank(theiaBasePath)) {
            StringBuilder queryBuilder = new StringBuilder(theiaBasePath);
            formDetail.setActionUrl(queryBuilder + V1_TRANSACTION_STATUS_URL);
            formDetail.setType(SUBMIT);
            formDetail.setMethod(POST);
            formDetail.getHeaders().put(CONTENT_TYPE, REQUEST_JSON_TYPE);
            if (StringUtils.isNotBlank(requestBean.getTxnToken())) {
                formDetail.getContent().put(TheiaConstant.RequestParams.Native.TOKEN_TYPE, ResponseConstants.TXN_TOKEN);
                formDetail.getContent().put(TheiaConstant.RequestParams.Native.TOKEN, requestBean.getTxnToken());
            }
            formDetail.getContent().put(MID, requestBean.getMid());
            formDetail.getContent().put(Native.ORDER_ID, requestBean.getOrderId());
            formDetail.getContent().put(ExtraConstants.VERSION, Version_V2);
        }
        return formDetail;
    }

    private void updateUpiPollPageUrlForAddMoneyMerchant(PaymentRequestBean requestBean, FormDetail formDetail) {
        if (formDetail == null) {
            return;
        }
        if (requestBean.isNativeAddMoney() || StringUtils.equals(requestBean.isAddMoney(), "1")) {

            String actionUrl = formDetail.getActionUrl();
            String addMoneyMid = ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID);

            if (StringUtils.containsIgnoreCase(actionUrl, addMoneyMid)) {

                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("?mid=").append(requestBean.getMid());
                queryBuilder.append("&orderId=").append(requestBean.getOrderId());

                String host = StringUtils.substringBefore(actionUrl, "?");
                formDetail.setActionUrl(host + queryBuilder);
            }
        }
    }

    private NativeJsonResponse getNativeJsonResponseForPaymentDone(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        TransactionResponse txnResp = getTransactionResponseForSeamless(workFlowResponseBean, requestData);
        /**
         * Map<String, String> paramMap = new TreeMap<>();
         * merchantResponseService.makeReponseToMerchantEnhancedNative(txnResp,
         * paramMap);
         *
         * processTransactionUtil.pushNativeJsonResponseEvent(txnResp);
         *
         * ResultInfo resultInfo =
         * NativePaymentUtil.resultInfo(ResultCode.SUCCESS); if
         * (!StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(),
         * txnResp.getTransactionStatus())) { resultInfo =
         * NativePaymentUtil.resultInfo(ResultCode.FAILED);
         * resultInfo.setResultMsg(txnResp.getResponseMsg()); }
         *
         * if (requestData.isNativeRetryEnabled()) { resultInfo.setRetry(true);
         * } else { resultInfo.setRetry(false); }
         *
         * NativeJsonResponseBody body = new NativeJsonResponseBody();
         * body.setResultInfo(resultInfo); body.setTxnInfo(paramMap);
         * body.setCallBackUrl(txnResp.getCallbackUrl());
         *
         * NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
         * nativeJsonResponse.setHead(new ResponseHeader());
         * nativeJsonResponse.setBody(body);
         **/
        // return nativeJsonResponse;
        NativeJsonResponse nativeJsonResponse = generateNativeJsonResponseFromTxnResponse(workFlowResponseBean,
                txnResp, requestData);
        String mid = "";
        if (requestData.getMid() != null) {
            mid = requestData.getMid();
        }
        if (StringUtils.isBlank(mid) && txnResp != null) {
            mid = txnResp.getMid();
        }
        if (StringUtils.isNotBlank(mid)) {
            boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);
            boolean encRequestEnabled = merchantPreferenceService.isEncRequestEnabled(mid);
            Map<String, String> txnInfo = new TreeMap<>();
            if ((isAES256Encrypted || encRequestEnabled)
                    && ff4jUtils.isFeatureEnabledOnMid(mid, THEIA_ENCRYPTED_RESPONSE_TO_JSON, false)) {
                LOGGER.info("Feature encryptedResponseToJson is enabled getNativeJsonResponseForPaymentDone()");
                if (txnResp.getMid() != null) {
                    merchantResponseService.encryptedResponseJson(mid, txnInfo, txnResp, isAES256Encrypted,
                            encRequestEnabled);
                    nativeJsonResponse.getBody().setTxnInfo(txnInfo);
                }
            }
        } else {
            LOGGER.error("mid is blank in both PaymentRequestBean and txnResp");
        }
        return nativeJsonResponse;
    }

    private NativeJsonResponse generateNativeJsonResponseFromTxnResponse(WorkFlowResponseBean workFlowResponseBean,
            TransactionResponse txnResp, PaymentRequestBean requestData) {
        Map<String, String> paramMap = new TreeMap<>();
        merchantResponseService.makeReponseToMerchantEnhancedNative(txnResp, paramMap);

        processTransactionUtil.pushNativeJsonResponseEvent(txnResp);

        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
        UpiLiteResponseData upiLiteResponseData = populateUpiLiteRespionseData(workFlowResponseBean);
        if (!StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(), txnResp.getTransactionStatus())) {
            if (ff4jUtils.isFeatureEnabled(COTP_REDIRECTION, false)) {
                if (!StringUtils.equals(com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS,
                        txnResp.getTransactionStatus())) {
                    resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
                    resultInfo.setResultMsg(txnResp.getResponseMsg());
                }
            } else {
                resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
                resultInfo.setResultMsg(txnResp.getResponseMsg());
            }
        }
        addRegionalFieldInPTCResponse(resultInfo);
        if (requestData.isNativeRetryEnabled()) {
            resultInfo.setRetry(true);
        } else {
            resultInfo.setRetry(false);
        }

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        body.setResultInfo(resultInfo);
        body.setTxnInfo(paramMap);
        body.setCallBackUrl(txnResp.getCallbackUrl());
        body.setUpiLiteResponseData(upiLiteResponseData);
        if (merchantPreferenceService.isIdempotencyEnabledOnUi(requestData.getMid(), false)) {
            if (body.getAdditionalInfo() == null)
                body.setAdditionalInfo(new HashMap<>());
            body.getAdditionalInfo().put(TheiaConstant.ExtraConstants.idempotentTransaction,
                    String.valueOf(workFlowResponseBean.isIdempotent()));
        }

        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);

        return nativeJsonResponse;
    }

    private UpiLiteResponseData populateUpiLiteRespionseData(WorkFlowResponseBean workFlowResponseBean) {
        QueryPaymentStatus queryPaymentStatus = workFlowResponseBean.getQueryPaymentStatus();
        if (queryPaymentStatus != null) {
            List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOption = queryPaymentStatus.getPayOptions();
            if (!CollectionUtils.isEmpty(payOption)) {
                Map<String, String> extendedInfo = payOption.get(0).getExtendInfo();
                if (!extendedInfo.isEmpty()) {
                    String response = extendedInfo.get("upiLiteResponseData");
                    if (StringUtils.isNotEmpty(response)) {
                        try {
                            return JsonMapper.mapJsonToObject(response, UpiLiteResponseData.class);
                        } catch (Exception ex) {
                            LOGGER.warn("Failed to parse upiLiteResponseData", ex);
                        }
                    }
                }
            }
        }
        return null;
    }

    private NativeJsonResponse generateMerchantNativeJsonResponsePaymentNotDone(
            WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData) {
        TransactionResponse txnResp = getTxnResponsePaymentNotDone(workFlowResponseBean, requestData);
        Map<String, String> paramMap = new TreeMap<>();
        merchantResponseService.makeReponseToMerchantEnhancedNative(txnResp, paramMap);
        processTransactionUtil.pushNativeJsonResponseEvent(txnResp);

        ResultInfo resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
        resultInfo.setResultMsg(txnResp.getResponseMsg());
        BankResultInfo bankResultInfo = null;
        boolean isMerchantOfflineType = false;
        boolean isMerchantOnPaytm = merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid());
        String merchantSolutionType = getMerchantSolutionType(requestData.getMid());
        if (StringUtils.isNotBlank(merchantSolutionType) && merchantSolutionType.equalsIgnoreCase(OFFLINE_MERCHANT)) {
            isMerchantOfflineType = true;
        }
        if ((isMerchantOfflineType && !isMerchantOnPaytm && ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                BizConstant.Ff4jFeature.POPULATE_BANK_RESULT_INFO_OFFLINE, false))
                || ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                        BizConstant.Ff4jFeature.POPULATE_BANK_RESULT_INFO_ONLINE, false)) {
            if (workFlowResponseBean.getWorkFlowRequestBean() != null
                    && PaymentTypeIdEnum.UPI.value.equals(workFlowResponseBean.getWorkFlowRequestBean()
                            .getPaymentTypeId())) {
                bankResultInfo = populateBankResultInfo(workFlowResponseBean);
                LOGGER.info("BankResultInfo for UPI transaction : {}", bankResultInfo);
            }
        }

        addRegionalFieldInPTCResponse(resultInfo);
        if (requestData.isNativeRetryEnabled()) {
            resultInfo.setRetry(true);
        } else {
            resultInfo.setRetry(false);
        }

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        body.setResultInfo(resultInfo);
        body.setTxnInfo(paramMap);
        body.setCallBackUrl(txnResp.getCallbackUrl());
        body.setBankResultInfo(bankResultInfo);
        setDeepLinkInfoForFailure(body, workFlowResponseBean, null, requestData);
        setRetryInfo(body, requestData, workFlowResponseBean, txnResp);
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);
        LOGGER.info("NativeJsonResponce : {}", nativeJsonResponse.getBody());
        return nativeJsonResponse;
    }

    private void setDeepLinkInfoForFailure(NativeJsonResponseBody body, WorkFlowResponseBean flowResponseBean,
            DeepLink deepLink, PaymentRequestBean requestData) {
        if (requestData.isDeepLinkRequired()) {
            LOGGER.info("Setting deep Link Info for Bank Form failure");
            body.setDeepLinkInfo(getDeepLinkInfo(flowResponseBean, null, requestData));
        }
    }

    private void setRetryInfo(NativeJsonResponseBody body, PaymentRequestBean requestBean,
            WorkFlowResponseBean workFlowResponseBean, TransactionResponse transactionResponse) {
        if (!transactionResponse.isOfflineRequest()
                && (workFlowResponseBean.getWorkFlowRequestBean() != null && workFlowResponseBean
                        .getWorkFlowRequestBean().isOfflineFlow())) {
            transactionResponse.setOfflineRequest(Boolean.TRUE);
        }
        if (isPaymentRetryInfoEnabled(transactionResponse)) {
            LOGGER.info("Setting Retry Info in native json response");
            body.setRetryInfo(transactionResponse.getRetryInfo());

        }
    }

    public boolean isPaymentRetryInfoEnabled(TransactionResponse response) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", response.getMid());
        if (response.isOfflineRequest()) {
            context.put("merchantType", OFFLINE);
        } else if (merchantExtendInfoUtils.isMerchantOnPaytm(response.getMid())) {
            context.put("merchantType", ONUS);
        }
        context.put("custId", response.getCustId());
        if (iPgpFf4jClient.checkWithdefault(PAYMENT_RETRY_INFO, context, false)) {
            LOGGER.info("Feature paymentRetryInfo is enabled");
            return Boolean.TRUE;

        }
        return Boolean.FALSE;
    }

    public String generateResponseForValidationFailure(PaymentRequestBean requestData,
            com.paytm.pgplus.common.model.ResultInfo resultInfo) {
        TransactionResponse response = getTransactionResponseForValidationFailure(requestData, resultInfo);
        return getFinalHtmlResponse(response);
    }

    private TransactionResponse getTransactionResponseForValidationFailure(PaymentRequestBean requestData,
            com.paytm.pgplus.common.model.ResultInfo resultInfo) {

        TransactionResponse transactionResponse = new TransactionResponse();
        addRegionalFieldInPTCResponse(resultInfo);
        if (StringUtils.isNotEmpty(requestData.getClientId()))
            transactionResponse.setClientId(requestData.getClientId());
        transactionResponse.setLinkBasedPayment("true");
        transactionResponse.setMid(requestData.getMid());
        transactionResponse.setOrderId(requestData.getOrderId());
        transactionResponse.setResponseCode(resultInfo.getResultCode());
        transactionResponse.setResponseMsg(resultInfo.getResultMsg());
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        transactionResponse.setTxnAmount(requestData.getTxnAmount());
        // Set Response-Code and Response-Message
        if (transactionResponse.getResponseCode() == null) {
            responseCodeUtil.setRespMsgeAndCode(transactionResponse,
                    StringUtils.isNotBlank(requestData.getInternalErrorCode()) ? requestData.getInternalErrorCode()
                            : resultInfo != null ? resultInfo.getResultCode() : "", SystemResponseCode.SYSTEM_ERROR);
        }
        transactionResponse.setCallbackUrl(requestData.getCallbackUrl());
        CommonUtils.setExtraParamsMapFromReqToResp(requestData, transactionResponse);
        return transactionResponse;
    }

    private String generateMerchantReponseForSeamless(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        TransactionResponse response = getTransactionResponseForSeamless(workFlowResponseBean, requestData);
        return getFinalHtmlResponse(response);
    }

    public TransactionResponse getTransactionResponseForSeamless(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        TransactionResponse response = new TransactionResponse();
        if (workFlowResponseBean.getQueryTransactionStatus() != null) {
            response.setOrderId(workFlowResponseBean.getQueryTransactionStatus().getMerchantTransId());
            response.setTxnId(workFlowResponseBean.getQueryTransactionStatus().getAcquirementId());
        }
        if (StringUtils.isNotBlank(requestData.getPaymentMid())
                && StringUtils.isNotBlank(requestData.getPaymentOrderId())) {
            response.setTxnAmount("0.00");
        } else {
            if (workFlowResponseBean.getQueryPaymentStatus() != null) {
                response.setTxnAmount(getTransactionAmount(workFlowResponseBean.getQueryPaymentStatus()));
            }
        }
        response.setMid(requestData.getMid());
        setRequestType(response, requestData.getMid(), requestData.getOrderId(), requestData.getRequestType());
        response.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(getPaymentMode(
                workFlowResponseBean.getQueryPaymentStatus(), workFlowResponseBean.getQueryTransactionStatus())));
        response.setCurrency(workFlowResponseBean.getQueryPaymentStatus().getTransAmountCurrencyType());
        response.setQrIdFlowOnly(requestData.isQRIdFlowOnly());
        if (workFlowResponseBean.getQueryTransactionStatus() != null
                && workFlowResponseBean.getQueryTransactionStatus().getChargeAmount() != null) {
            response.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(workFlowResponseBean
                    .getQueryTransactionStatus().getChargeAmount().getAmount()));
        } else { // FAILURE and PENDING cases
            if (workFlowResponseBean.getQueryPaymentStatus() != null
                    && !CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getPayOptions())) {
                com.paytm.pgplus.pgproxycommon.models.PayOption payOption = workFlowResponseBean
                        .getQueryPaymentStatus().getPayOptions().get(0);
                if (payOption != null) {
                    response.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(payOption
                            .getChargeAmountValue()));
                }
            }
        }
        // setting splitPayInfo in txnResponse
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(workFlowResponseBean.getQueryTransactionStatus()
                .getSplitCommandInfoList())) {
            try {
                LOGGER.info("setting split settlement data from splitCommandInfo list");
                response.setSplitSettlementInfo(JsonMapper.mapObjectToJson(SplitSettlementHelper
                        .convertSplitCommandInfoListToSplitSettlementInfoData(workFlowResponseBean
                                .getQueryTransactionStatus().getSplitCommandInfoList())));
            } catch (FacadeCheckedException e) {
                LOGGER.error("TheiaResponseGenerator.getTransactionResponseForSeamless | Exception : {}", e);
            }
        } else if (workFlowResponseBean.getQueryPaymentStatus().getExtendInfo() != null
                && workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()
                        .containsKey(MERCHANT_SPLIT_SETTLEMENT_INFO)) {
            response.setSplitSettlementInfo(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()
                    .get(MERCHANT_SPLIT_SETTLEMENT_INFO));
        }
        if (EPayMethod.BALANCE.getOldName().equals(requestData.getPaymentTypeId())) {
            response.setBankName(WALLET);
            response.setGateway(WALLET);
        }
        if (workFlowResponseBean.getQueryTransactionStatus().getCreatedTime() != null) {
            response.setTxnDate(DateUtils.format(workFlowResponseBean.getQueryTransactionStatus().getCreatedTime()));
        }
        if (workFlowResponseBean.getQueryTransactionStatus().getBuyerExternalUserId() != null) {
            response.setCustId(workFlowResponseBean.getQueryTransactionStatus().getBuyerExternalUserId());
        }
        if (requestData.getRequestType().equals(ERequestType.OFFLINE.name())
                || requestData.getRequestType().equals(ERequestType.DYNAMIC_QR.name())
                || isRequestOfflineFlow(workFlowResponseBean.getQueryPaymentStatus())) {
            response.setTransactionStatus(MapperUtils.getTransactionStatusForResponseForOffline(
                    workFlowResponseBean.getQueryTransactionStatus(), workFlowResponseBean.getQueryPaymentStatus()));
            response.setAdditionalParam(getAdditionalParam(workFlowResponseBean));
        } else {
            response.setTransactionStatus(MapperUtils.getTransactionStatusForResponse(
                    workFlowResponseBean.getQueryTransactionStatus(), workFlowResponseBean.getQueryPaymentStatus()));
        }
        if (MapUtils.isNotEmpty(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo())
                && ((AggregatorType.ORDER_CREATOR.name().equals(
                        workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get(AGG_TYPE)) && AggregatorMidKeyUtil
                        .isMidEnabledForMLVAggregatorMid(workFlowResponseBean.getQueryTransactionStatus()
                                .getExtendInfo().get(AGGREGATOR_MID))) || (OFFLINE.equalsIgnoreCase(requestData
                        .getSource()) && ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                        SET_ADDITIONAL_PARAM_IN_RESPONSE, false)))) {
            response.setAdditionalParam(getAdditionalParam(workFlowResponseBean));

            MerchantInfo merchantInfo = response.getAdditionalParam().getMerchantInfo();
            if (null == merchantInfo) {
                merchantInfo = new MerchantInfo();
                response.getAdditionalParam().setMerchantInfo(merchantInfo);
            }
            merchantInfo.setCurrentTxnCount(workFlowResponseBean.getQueryTransactionStatus().getCurrentTxnCount());
            merchantInfo.setLogoUrl(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get(LOGO_URL));

            if (null != merchantInfo
                    && (OFFLINE.equalsIgnoreCase(requestData.getSource()) && ff4jUtils.isFeatureEnabledOnMid(
                            requestData.getMid(), SET_ADDITIONAL_PARAM_IN_RESPONSE, false))) {
                MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(requestData.getMid());
                MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService
                        .getMerchantInfo(merchantDetailsRequest);

                if (merchantDetailsResponse != null & merchantDetailsResponse.getMerchantImageName() != null)
                    merchantInfo.setLogoUrl(merchantDetailsResponse.getMerchantImageName());
            }
        }

        String instErrorCode = workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode();

        // Set Response-Code and Response-message
        if (ExternalTransactionStatus.PENDING.name().equals(response.getTransactionStatus())) {
            responseCodeUtil.setRespMsgeAndCode(response, instErrorCode, SystemResponseCode.ABANDON_AT_CASHIER_PAGE);
        } else {
            responseCodeUtil.setRespMsgeAndCode(response, instErrorCode, null);
        }

        if (isRequestOfflineFlow(workFlowResponseBean.getQueryPaymentStatus())) {
            response.setOfflineRequest(Boolean.TRUE);
        }
        response.setCallbackUrl(getCallbackUrl(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo(),
                workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()));
        // hack for Dynamic QR as callback set is the one sent during order
        // creation
        if (ERequestType.DYNAMIC_QR.name().equals(requestData.getRequestType())) {
            String callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
            if (StringUtils.isEmpty(callbackUrl)) {
                LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                throw new TheiaControllerException("CallbackUrl is not configured for offline reruest");

            }

            callbackUrl = callbackUrl + requestData.getOrderId();
            response.setCallbackUrl(callbackUrl);

        }
        response.setGateway(getGateway(response.getPaymentMode(), workFlowResponseBean));
        response.setBankName(getBankName(response.getPaymentMode(), workFlowResponseBean));
        response.setMerchUniqueReference(getMerchUniqueReference(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf1(getUdf1(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf2(getUdf2(workFlowResponseBean.getQueryTransactionStatus()));
        response.setUdf3(getUdf3(workFlowResponseBean.getQueryTransactionStatus()));
        response.setAdditionalInfo(getAdditionalInfo(workFlowResponseBean.getQueryTransactionStatus()));

        setPrepaidCardInTransactionResponse(workFlowResponseBean, response, requestData);

        if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() > 1)) {
            response.setChildTxnList(getChildTxnList(workFlowResponseBean.getQueryPaymentStatus(),
                    workFlowResponseBean.getQueryTransactionStatus(), requestData.getMid()));
        }
        if (merchantPreferenceService.isBinInResponseEnabled(response.getMid())) {
            if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                    && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() == 1)) {
                if ((PayMethod.CREDIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.DEBIT_CARD.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    response.setCardScheme(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0)
                            .getExtendInfo().get(INST_ID));
                }
                if ((PayMethod.EMI.getOldName()).equalsIgnoreCase(response.getPaymentMode())
                        || (PayMethod.EMI_DC.getOldName()).equalsIgnoreCase(response.getPaymentMode())) {
                    String binNumber = workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        response.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        response.setCardScheme(getCardSchemeForEMI(workFlowResponseBean.getQueryTransactionStatus()));
                }
            }
        }
        response.setExtraParamsMap(CommonUtils.getExtraParamsMapFromExtendedInfo(workFlowResponseBean
                .getQueryTransactionStatus().getExtendInfo()));
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && null != workFlowResponseBean.getPromoCodeResponse()
                && null != workFlowResponseBean.getPromoCodeResponse().getPromoCodeDetail()) {

            LOGGER.info("PROMO_CODE_RESPONSE  for native : {}", workFlowResponseBean.getPromoCodeResponse());

            if (StringUtils.isNotBlank(workFlowResponseBean.getPromoCodeResponse().getPromoCodeDetail().getPromoCode())) {
                response.setPromoCode(workFlowResponseBean.getPromoCodeResponse().getPromoCodeDetail().getPromoCode());
            }

            if (StringUtils.isNotBlank(workFlowResponseBean.getPromoCodeResponse().getPromoResponseCode())
                    && StringUtils.isNotBlank(workFlowResponseBean.getPromoCodeResponse().getResultStatus())) {
                response.setPromoResponseCode(workFlowResponseBean.getPromoCodeResponse().getPromoResponseCode());
                response.setPromoApplyResultStatus(workFlowResponseBean.getPromoCodeResponse().getResultStatus());
                response.setPromoCode(requestData.getPromoCampId());
            }
        }
        response.setUserEmail(getUserEmailFromExtendInfo(workFlowResponseBean.getQueryTransactionStatus()
                .getExtendInfo()));
        response.setUserPhone(getUserMobileFromExtendInfo(workFlowResponseBean.getQueryTransactionStatus()
                .getExtendInfo()));

        if (merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid())) {
            LOGGER.info("getTransactionResponseForSeamless: setting VPA in TransactionResponse - ONUS");
            response.setVpa(getVpaFromExtendInfo(workFlowResponseBean));
        } else if (merchantPreferenceService.isReturnVPAEnabled(requestData.getMid())) {
            LOGGER.info("getTransactionResponseForSeamless: setting VPA in TransactionResponse - OFFUS");
            String userId = null;
            if (null != workFlowResponseBean.getUserDetails()) {
                userId = workFlowResponseBean.getUserDetails().getUserId();
            }
            response.setVpa(getVpa(workFlowResponseBean, userId));
        }
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                && workFlowResponseBean != null
                && workFlowResponseBean.getQueryPaymentStatus() != null
                && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null && !workFlowResponseBean
                        .getQueryPaymentStatus().getPayOptions().isEmpty())
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0) != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo() != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo()
                        .get("referenceNo") != null) {

            response.setBankTxnId(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo()
                    .get("referenceNo"));
        }

        setClientIdIfNotNull(response, workFlowResponseBean);
        if (workFlowResponseBean.getQueryTransactionStatus() != null
                && workFlowResponseBean.getQueryTransactionStatus().getExtendInfo() != null
                && workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get(ExtendedInfoKeys.MERCH_UNQ_REF) != null) {
            String merchUniqRef = workFlowResponseBean.getQueryTransactionStatus().getExtendInfo()
                    .get(ExtendedInfoKeys.MERCH_UNQ_REF);
            if ((StringUtils.isNotEmpty(merchUniqRef) && (merchUniqRef
                    .startsWith(TheiaConstant.ExtraConstants.LINK_ID_PREFIX) || merchUniqRef
                    .startsWith(TheiaConstant.ExtraConstants.INVOICE_ID_PREFIX)))) {
                LOGGER.info(
                        "Setting callback url for Link in other than OFFLINE flow(APP) for merchUniqRef{} callback{}",
                        merchUniqRef, response.getCallbackUrl());
                setLinkPaymentDataInTransactionResponse(workFlowResponseBean, response, requestData);
                EventUtils.pushLinkBasedPaymentCompletedEvent(response, requestData);
            }
        }

        // Set Response-Code and Response-message
        if (ExternalTransactionStatus.PENDING.name().equals(response.getTransactionStatus())) {
            responseCodeUtil.setRespMsgeAndCode(response, workFlowResponseBean.getQueryPaymentStatus()
                    .getInstErrorCode(), SystemResponseCode.ABANDON_AT_CASHIER_PAGE);
        } else {
            responseCodeUtil.setRespMsgeAndCode(response, workFlowResponseBean.getQueryPaymentStatus()
                    .getInstErrorCode(), null);
        }

        // set subscription-id
        response.setSubsId(requestData.getSubscriptionID());

        if (merchantPreferenceService.isOnusRentPaymentMerchant(requestData.getMid())
                && workFlowResponseBean.getQueryTransactionStatus().getOrderModifyExtendinfo() != null) {
            Map<String, String> orderModifyExtendInfo = null;
            try {
                orderModifyExtendInfo = JsonMapper.mapJsonToObject(workFlowResponseBean.getQueryTransactionStatus()
                        .getOrderModifyExtendinfo(), Map.class);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Not able to cast orderModifyExtendInfo for onus rent merchants");
            }

            if (orderModifyExtendInfo != null && orderModifyExtendInfo.containsKey(RISK_INFO)) {
                response.setRiskInfo(orderModifyExtendInfo.get(RISK_INFO));
            }

        }

        if (workFlowResponseBean.getQueryTransactionStatus() != null) {
            setResponseForSubsLinkPayment(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo(), response,
                    workFlowResponseBean.getQueryTransactionStatus().getAcquirementId());
        }

        if (response.isSubsLinkInfo() && workFlowResponseBean.getQueryPaymentStatus() != null) {
            Date date = workFlowResponseBean.getQueryPaymentStatus().getPaidTime();
            if (date == null && workFlowResponseBean.getQueryTransactionStatus() != null) {
                date = workFlowResponseBean.getQueryTransactionStatus().getCreatedTime();
            }
            response.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        }

        /*
         * Change response for PWP merchant
         */
        if (workFlowResponseBean.getQueryPaymentStatus() != null
                && StringUtils.isNotBlank(workFlowResponseBean.getQueryPaymentStatus().getPwpCategory())) {
            changeResponseForPwpMerchant(response);
        }
        // addRegionalFieldInPTCResponse(response);
        if (merchantPreferenceService.isTxnPaidTimePreferenceEnabled(requestData.getMid(), false)) {
            setTxnPaidTime(response, workFlowResponseBean.getQueryTransactionStatus());
        }
        return response;
    }

    private void setResponseForSubsLinkPayment(Map<String, String> extendInfo, TransactionResponse response,
            String acquirementId) {
        if (extendInfo != null && extendInfo.get(ExtendedInfoKeys.SUBS_LINK_INFO) != null) {
            LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                    .get(LINK_BASED_KEY + acquirementId);

            if (linkBasedMerchantInfo != null) {
                response.setMerchantName(linkBasedMerchantInfo.getMerchantName());
                response.setMerchantImage(linkBasedMerchantInfo.getMerchantImage());
            }

            response.setSubsLinkInfo(true);
        }

    }

    private void setPrepaidCardInTransactionResponse(WorkFlowResponseBean workFlowResponseBean,
            TransactionResponse response, PaymentRequestBean requestData) {
        if (merchantPreferenceService.isReturnPrepaidEnabled(requestData.getMid())) {
            if (workFlowResponseBean.getQueryPaymentStatus() != null
                    && !CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getPayOptions())
                    && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0) != null
                    && StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0)
                            .getExtendInfo().get(FacadeConstants.PREPAID_CARD))) {
                response.setPrepaidCard(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0)
                        .getExtendInfo().get(FacadeConstants.PREPAID_CARD));
            }
            if (workFlowResponseBean.getQueryTransactionStatus() != null
                    && workFlowResponseBean.getQueryTransactionStatus().getPayOptions() != null) {
                List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOtions = workFlowResponseBean
                        .getQueryTransactionStatus().getPayOptions();
                if (payOtions != null && !payOtions.isEmpty()) {
                    for (com.paytm.pgplus.pgproxycommon.models.PayOption payOption : payOtions) {
                        if (payOption.getExtendInfo() != null
                                && StringUtils.isNotEmpty(payOption.getExtendInfo().get(FacadeConstants.PREPAID_CARD))) {
                            response.setPrepaidCard(payOption.getExtendInfo().get(FacadeConstants.PREPAID_CARD));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setLinkPaymentDataInTransactionResponse(WorkFlowResponseBean workFlowResponseBean,
            TransactionResponse response, PaymentRequestBean requestData) {
        response.setLinkBasedPayment(Boolean.TRUE.toString());
        // String theiaRedirectionPath =
        // ConfigurationUtil.getProperty(THEIA_BASE_PATH);
        String theiaRedirectionPath = ConfigurationUtil.getProperty(THEIA_BUISNESS_BASE_PATH);

        QueryPaymentStatus paymentResponse = workFlowResponseBean.getQueryPaymentStatus();
        String offlineFlow = null;
        if (workFlowResponseBean.getQueryTransactionStatus() != null) {
            if (workFlowResponseBean.getQueryTransactionStatus().getExtendInfo() != null) {
                offlineFlow = workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get(OFFLINE_FLOW);
            }
            if (getIsLinkInvoicePayment(workFlowResponseBean.getQueryTransactionStatus())) {
                response.setLinkType(TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
            }
            Date date = paymentResponse != null ? paymentResponse.getPaidTime() : null;
            if (date == null) {
                date = workFlowResponseBean.getQueryTransactionStatus().getCreatedTime();
            }
            response.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        }

        if (!ERequestType.OFFLINE.getType().equals(response.getRequestType())
                && !(ERequestType.NATIVE.getType().equals(requestData.getRequestType()) && Boolean
                        .parseBoolean(offlineFlow))) {
            if (theiaRedirectionPath != null) {
                response.setCallbackUrl(theiaRedirectionPath + LINK_PAYMENT_STATUS_URL);
            }
            if (paymentResponse != null) {
                response.setTransactionStatus(paymentResponse.getPaymentStatusValue());
            }
        }
        LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                .get(LINK_BASED_KEY + paymentResponse.getTransId());
        if (linkBasedMerchantInfo != null) {
            response.setMerchantName(linkBasedMerchantInfo.getMerchantName());
            response.setMerchantImage(linkBasedMerchantInfo.getMerchantImage());
        }
        response.setShowViewFlag(PAYMENT_SCREEN);
        // response.setTxnAmount(paymentResponse.getTransAmountValue());
    }

    public String getResponseCode(String instErrorCode, ResponseCodeDetails respCodeDetails, String transactionStatus) {
        if (StringUtils.isNotEmpty(instErrorCode) && !StringUtils.isNumeric(instErrorCode)) {
            /*
             * Here, instErrorCode is "FGW_*..."
             */
            respCodeDetails = getResponseCodeDetailsFromInstErrorCode(instErrorCode);

            /*
             * This instErrorCode, is now updated to paytmErrorCode
             */
            instErrorCode = getPaytmRespCodeFromResponseCodeDetail(respCodeDetails);
        }
        return MapperUtils.getResponseCode(transactionStatus, instErrorCode, null);
    }

    public String getResponseMessage(WorkFlowResponseBean workFlowResponseBean, ResponseCodeDetails respCodeDetails,
            String transactionStatus) {
        String responseMsg = getResponseMsgFromResponseCodeDetail(workFlowResponseBean, respCodeDetails);
        if (ExternalTransactionStatus.TXN_SUCCESS.toString().equals(transactionStatus)) {
            responseMsg = com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SUCCESS_RESPONSE_CODE.getMessage();
        } else if (ExternalTransactionStatus.PENDING.toString().equals(transactionStatus)) {
            try {
                respCodeDetails = responseCodeService
                        .getPaytmResponseCodeDetails(ResponseConstants.ResponseCodes.ABANDON_AT_CASHIER_PAGE);
                EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for responseCode : {}",
                        respCodeDetails, ResponseConstants.ResponseCodes.ABANDON_AT_CASHIER_PAGE);
                if (respCodeDetails != null) {
                    responseMsg = StringUtils.isNotBlank(respCodeDetails.getDisplayMessage()) ? respCodeDetails
                            .getDisplayMessage() : respCodeDetails.getRemark();
                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while fetching ResponseCodeDetails :: {}", e);
            }

        }
        return responseMsg;
    }

    private void setClientIdIfNotNull(TransactionResponse response, WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean.getQueryPaymentStatus().getExtendInfo() != null
                && workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get("clientId") != null)
            response.setClientId(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get("clientId"));
    }

    private AdditionalParam getAdditionalParam(WorkFlowResponseBean workFlowResponseBean) {
        AdditionalParam additionalParam = new AdditionalParam();

        if (null != workFlowResponseBean.getQueryPaymentStatus()
                && !CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo())) {
            String currentTxnCount = null;
            if (null != workFlowResponseBean.getQueryTransactionStatus()) {
                currentTxnCount = workFlowResponseBean.getQueryTransactionStatus().getCurrentTxnCount();
            }
            additionalParam.setMerchantInfo(getMerchantInfo(workFlowResponseBean.getQueryPaymentStatus()
                    .getExtendInfo(), currentTxnCount));
        }

        if (!CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getPayOptions())) {
            additionalParam.setPaymentInfoList(getPaymentInfo(workFlowResponseBean.getQueryPaymentStatus()));
        }
        return additionalParam;
    }

    private List<PaymentInfo> getPaymentInfo(QueryPaymentStatus queryPaymentStatus) {
        List<PaymentInfo> paymentInfo = new ArrayList<>();
        for (com.paytm.pgplus.pgproxycommon.models.PayOption payOption : queryPaymentStatus.getPayOptions()) {
            PaymentInfo payInfo = new PaymentInfo();
            payInfo.setPaymentMode(payOption.getPayMethodOldName());
            payInfo.setTxnAmount(AmountUtils.getTransactionAmountInRupee(payOption.getPayAmountValue()));
            payInfo.setCardNo(payOption.getExtendInfo().get(ResponseConstants.MASKED_CARD_NO));
            payInfo.setInstId(payOption.getExtendInfo().get(INST_ID));
            paymentInfo.add(payInfo);
        }
        return paymentInfo;
    }

    private String getCardSchemeForEMI(String binNumber) {
        BinDetail binDetail = null;

        try {
            BinUtils.logSixDigitBinLength(binNumber);
            binDetail = binFetchService.getCardBinDetail(Long.parseLong(binNumber));
            EXT_LOGGER.customInfo("Mapping response - BinDetail :: {}", binDetail);
            return binDetail.getCardName();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Bin {}, due to", binNumber, e);
        }
        return null;
    }

    private String getCardSchemeForEMI(QueryTransactionStatus transactionStatus) {
        if (transactionStatus != null) {
            List<PaymentView> paymentViews = transactionStatus.getPaymentViews();
            if (paymentViews != null) {
                List<PayOptionInfo> payOptionInfos = paymentViews.get(0).getPayOptionInfos();
                if (payOptionInfos != null) {
                    Map<String, String> payOptionBillExtendInfo = payOptionInfos.get(0).getPayOptionBillExtendInfo();
                    if (payOptionBillExtendInfo != null) {
                        String binNumber = payOptionBillExtendInfo.get("binNumber");
                        if (binNumber != null) {
                            return getCardSchemeForEMI(binNumber);
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getCardSchemeForEMI(CashierTransactionStatus transactionStatus) {
        if (transactionStatus != null) {
            List<PaymentView> paymentViews = transactionStatus.getPaymentViews();
            if (paymentViews != null) {
                List<PayOptionInfo> payOptionInfos = paymentViews.get(0).getPayOptionInfos();
                if (payOptionInfos != null) {
                    Map<String, String> payOptionBillExtendInfo = payOptionInfos.get(0).getPayOptionBillExtendInfo();
                    if (payOptionBillExtendInfo != null) {
                        String binNumber = payOptionBillExtendInfo.get("binNumber");
                        if (binNumber != null) {
                            return getCardSchemeForEMI(binNumber);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void setResponseForSubsLinkPaymentNotDone(TransactionResponse response, String acquirementId) {
        LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                .get(LINK_BASED_KEY + acquirementId);

        if (linkBasedMerchantInfo != null) {
            response.setMerchantName(linkBasedMerchantInfo.getMerchantName());
            response.setMerchantImage(linkBasedMerchantInfo.getMerchantImage());
        }

        response.setSubsLinkInfo(true);

    }

    private List<ChildTransaction> getChildTxnList(QueryPaymentStatus paymentStatus,
            QueryTransactionStatus queryTransactionStatus, String mid) {
        List<ChildTransaction> childTxnList = new ArrayList<>();
        for (com.paytm.pgplus.pgproxycommon.models.PayOption payOption : paymentStatus.getPayOptions()) {
            ChildTransaction childTransaction = new ChildTransaction();
            childTransaction.setBankTxnId(getBankTxnId(payOption.getExtendInfo()));
            childTransaction.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(payOption
                    .getPayMethodOldName()));

            if (merchantPreferenceService.isReturnPrepaidEnabled(mid) && payOption.getExtendInfo() != null) {
                if (StringUtils.isNotEmpty(payOption.getExtendInfo().get(FacadeConstants.PREPAID_CARD))) {
                    childTransaction.setPrepaidCard(payOption.getExtendInfo().get(FacadeConstants.PREPAID_CARD));
                }
            }
            childTransaction.setGateway(getGateway(payOption.getExtendInfo()));
            // added for extracting bank name for childTxn paymodes
            Map<String, String> paymentStatusExtendInfo = paymentStatus.getExtendInfo();
            if (!StringUtils.equals(EPayMethod.BALANCE.getMethod(), payOption.getPayMethodName())
                    && paymentStatusExtendInfo != null && StringUtils.isNotBlank(getBankName(paymentStatusExtendInfo))) {
                childTransaction.setBankName(getBankName(paymentStatusExtendInfo));
            } else {
                childTransaction.setBankName(getGateway(payOption.getExtendInfo()));
            }
            childTransaction.setTxnAmount(AmountUtils.getTransactionAmountInRupee(payOption.getPayAmountValue()));
            childTransaction.setTxnId(paymentStatus.getTransId());
            String paytmMid = paymentStatus.getExtendInfo().get("paytmMerchantId");
            if (paymentStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID) != null) {
                paytmMid = paymentStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID);
            }

            if (merchantPreferenceService.isBinInResponseEnabled(paytmMid)) {
                if (DEBIT_CARD.equals(payOption.getPayMethodName()) || CREDIT_CARD.equals(payOption.getPayMethodName())) {
                    childTransaction.setCardScheme(payOption.getExtendInfo().get(INST_ID));
                }
                if (EMI.equals(payOption.getPayMethodName()) || EMI_DC.equals(payOption.getPayMethodName())) {
                    String binNumber = paymentStatus.getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        childTransaction.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        childTransaction.setCardScheme(getCardSchemeForEMI(queryTransactionStatus));
                }
            }
            childTxnList.add(childTransaction);
        }
        return childTxnList;
    }

    public String getBankPage(WorkFlowResponseBean workFlowResponseBean, PaymentRequestBean requestData) {
        if (null != workFlowResponseBean.getQueryPaymentStatus()) {
            String bankForm = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
            if (StringUtils.isNotBlank(bankForm)) {
                bankFormParser.checkIfDirectBankPage(bankForm, workFlowResponseBean.getCashierRequestId(),
                        workFlowResponseBean.getWorkFlowRequestBean());
                return bankForm;
            }
            return generateMerchantReponseFromPaymentStatus(workFlowResponseBean, requestData);
        }
        StatisticsLogger.logForXflush(MDC.get("MID"), null, null, "response", "Could not get bank redirection form",
                null);
        LOGGER.error("Could not get bank redirection form");
        return ExtraConstants.EMPTY_STRING;
    }

    public TransactionResponse getTxnResponsePaymentNotDone(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        LOGGER.info("WorkFlowResponseBean : {}", workFlowResponseBean);

        TransactionResponse transactionResponse = new TransactionResponse();

        if (Boolean.parseBoolean(ConfigurationUtil.getProperty(
                ALLOWED_REDIRECT_SHOW_PAYMENT_PAGE_WALLET_BALANCE_NOT_ENOUGH, "false"))
                && redirectToShowPaymentPage(requestData, workFlowResponseBean)) {
            createResponseToRedirectToCashierPage(transactionResponse, requestData.getMid(), requestData.getOrderId(),
                    requestData.getTxnToken());
            return transactionResponse;
        }
        if (requestData.isEnhancedCashierPageRequest()) {
            workFlowResponseBean.setBankFormFetchFailed(true);
        }

        transactionResponse.setOrderId(requestData.getOrderId());
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (MapUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo())
                && StringUtils.isNotBlank(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()
                        .get(TheiaConstant.ExtendedInfoPay.TOTAL_TXN_AMOUNT)))
            transactionResponse.setTxnAmount(AmountUtils.getTransactionAmountInRupee(workFlowResponseBean
                    .getQueryPaymentStatus().getExtendInfo().get(TheiaConstant.ExtendedInfoPay.TOTAL_TXN_AMOUNT)));
        else
            transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(requestData.getTxnAmount()));

        if (StringUtils.isNotBlank(requestData.getPaymentMid())
                && StringUtils.isNotBlank(requestData.getPaymentOrderId())) {
            transactionResponse.setTxnAmount("0.00");
        }
        transactionResponse.setTxnId(workFlowResponseBean.getQueryPaymentStatus().getTransId());
        transactionResponse.setMid(requestData.getMid());
        transactionResponse.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(getPaymentMode(
                workFlowResponseBean.getQueryPaymentStatus(), workFlowResponseBean.getQueryTransactionStatus())));
        transactionResponse.setCurrency(workFlowResponseBean.getQueryPaymentStatus().getTransAmountCurrencyType());
        setRequestType(transactionResponse, requestData.getMid(), requestData.getOrderId(),
                requestData.getRequestType());

        transactionResponse.setGateway(getGateway(transactionResponse.getPaymentMode(), workFlowResponseBean));
        transactionResponse.setBankName(getBankName(transactionResponse.getPaymentMode(), workFlowResponseBean));

        if (workFlowResponseBean.getQueryPaymentStatus().getPaidTime() != null) {
            transactionResponse
                    .setTxnDate(DateUtils.format(workFlowResponseBean.getQueryPaymentStatus().getPaidTime()));
        }
        transactionResponse.setCustId(requestData.getCustId());
        LOGGER.info(
                "ExtendInfo : {}",
                MaskingUtil.maskObject(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo(),
                        SSOTOKEN.getFieldName(), SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));

        setPrepaidCardInTransactionResponse(workFlowResponseBean, transactionResponse, requestData);

        if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() > 1)) {
            transactionResponse.setChildTxnList(getChildTxnList(workFlowResponseBean.getQueryPaymentStatus(),
                    workFlowResponseBean.getQueryTransactionStatus(), requestData.getMid()));
        }
        if (merchantPreferenceService.isBinInResponseEnabled(transactionResponse.getMid())) {
            if ((workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null)
                    && (workFlowResponseBean.getQueryPaymentStatus().getPayOptions().size() == 1)) {
                if ((PayMethod.CREDIT_CARD.getOldName()).equalsIgnoreCase(transactionResponse.getPaymentMode())
                        || (PayMethod.DEBIT_CARD.getOldName()).equalsIgnoreCase(transactionResponse.getPaymentMode())) {
                    transactionResponse.setCardScheme(workFlowResponseBean.getQueryPaymentStatus().getPayOptions()
                            .get(0).getExtendInfo().get(INST_ID));
                }
                if ((PayMethod.EMI.getOldName()).equalsIgnoreCase(transactionResponse.getPaymentMode())
                        || (PayMethod.EMI_DC.getOldName()).equalsIgnoreCase(transactionResponse.getPaymentMode())) {
                    String binNumber = workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get("binNumber");
                    if (binNumber != null)
                        transactionResponse.setCardScheme(getCardSchemeForEMI(binNumber));
                    else
                        transactionResponse.setCardScheme(getCardSchemeForEMI(workFlowResponseBean
                                .getQueryTransactionStatus()));
                }
            }
        }

        /*
         * this error code is got from instaProxy
         */
        String errorCode = workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode();

        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestData.getRequestType())
                && Objects.isNull(errorCode)
                && Objects.nonNull(workFlowResponseBean.getQueryPaymentStatus().getPaymentErrorCode())) {
            errorCode = new StringBuilder("SUBS_").append(
                    workFlowResponseBean.getQueryPaymentStatus().getPaymentErrorCode()).toString();
        } else {
            String paymentErrorCode = workFlowResponseBean.getQueryPaymentStatus().getPaymentErrorCode();
            errorCode = (errorCode == null) ? paymentErrorCode : errorCode;
        }

        transactionResponse.setQrIdFlowOnly(requestData.isQRIdFlowOnly());
        // set charge amount for mdr pcf merchant
        if (workFlowResponseBean.getQueryPaymentStatus() != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null) {
            List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptionList = workFlowResponseBean
                    .getQueryPaymentStatus().getPayOptions();
            if (payOptionList.size() == 1) {
                transactionResponse.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(payOptionList.get(0)
                        .getChargeAmountValue()));
            } else {
                Iterator<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptionIterator = payOptionList.iterator();
                while (payOptionIterator.hasNext()) {
                    com.paytm.pgplus.pgproxycommon.models.PayOption payOption = payOptionIterator.next();
                    if (payOption.getPayMethodName().equals(PayMethod.BALANCE)) {
                        transactionResponse.setChargeAmount(AmountUtils.getPaddedTransactionAmountInRupee(payOption
                                .getChargeAmountValue()));
                    }
                }
            }
        }

        // Set Merchant Response-Code and Response-Message
        responseCodeUtil.setRespMsgeAndCode(transactionResponse, errorCode, null);

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(requestData.getMid());

        String callbackUrl = getCallbackUrlFromExtendInfo(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo(),
                ExternalTransactionStatus.valueOf(transactionResponse.getTransactionStatus()));
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())) {
            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = nativeRetryUtil.getCallbackUrl(requestData.getTxnToken());
            }
            boolean isEnhancePaymentCall = (requestData != null) && requestData.isEnhancedCashierPaymentRequest();
            boolean isEnhanceCashierPageRequest = (requestData != null) && requestData.isEnhancedCashierPageRequest();
            StringBuilder sb = new StringBuilder(callbackUrl);
            if (!isAES256Encrypted && !isEnhancePaymentCall && !isEnhanceCashierPageRequest) {
                if (sb.indexOf("?") != -1) {
                    sb.append("&");
                } else {
                    sb.append("?");
                }
                sb.append("retryAllowed=");
                sb.append(requestData.isNativeRetryEnabled());
                if (StringUtils.isBlank(requestData.getNativeRetryErrorMessage())) {
                    sb.append("&errorMessage=").append(transactionResponse.getResponseMsg());
                } else {
                    sb.append("&errorMessage=").append(requestData.getNativeRetryErrorMessage());
                }
                sb.append("&errorCode=").append(transactionResponse.getResponseCode());
            }
            callbackUrl = sb.toString();
        } else if (ERequestType.DYNAMIC_QR.name().equals(requestData.getRequestType())) {
            callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
            if (StringUtils.isEmpty(callbackUrl)) {
                LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                throw new TheiaControllerException("CallbackUrl is not configured for offline reruest");
            }
            callbackUrl = callbackUrl + requestData.getOrderId();
        } else if (StringUtils.isBlank(callbackUrl)) {
            throw new TheiaControllerException("Could not fetch merchant callback URL");
        }
        transactionResponse.setCallbackUrl(callbackUrl);
        if (workFlowResponseBean.getQueryTransactionStatus() != null
                && workFlowResponseBean.getQueryTransactionStatus().getExtendInfo() != null) {
            transactionResponse.setExtraParamsMap(CommonUtils.getExtraParamsMapFromExtendedInfo(workFlowResponseBean
                    .getQueryTransactionStatus().getExtendInfo()));
            transactionResponse.setUserEmail(getUserEmailFromExtendInfo(workFlowResponseBean
                    .getQueryTransactionStatus().getExtendInfo()));
            transactionResponse.setUserPhone(getUserMobileFromExtendInfo(workFlowResponseBean
                    .getQueryTransactionStatus().getExtendInfo()));
            if (StringUtils
                    .isNotBlank(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get("clientId")))
                transactionResponse.setClientId(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo()
                        .get("clientId"));
        }
        // Setting splitSettlementInfo in txnResponse
        if (requestData.getSplitSettlementInfoData() != null) {
            try {
                transactionResponse.setSplitSettlementInfo(JsonMapper.mapObjectToJson(requestData
                        .getSplitSettlementInfoData()));
            } catch (FacadeCheckedException e) {
                LOGGER.error("TheiaResponseGenerator.getTxnResponsePaymentNotDone | Exception : {}", e);
            }
        } else if (workFlowResponseBean.getQueryPaymentStatus().getExtendInfo() != null
                && workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()
                        .containsKey(MERCHANT_SPLIT_SETTLEMENT_INFO)) {
            transactionResponse.setSplitSettlementInfo(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()
                    .get(MERCHANT_SPLIT_SETTLEMENT_INFO));
        }
        transactionResponse.setOfflineRequest(isRequestOfflineFlow(workFlowResponseBean.getQueryPaymentStatus()));

        if (merchantPreferenceService.isReturnVPAEnabled(requestData.getMid())) {
            String userId = null;
            if (null != workFlowResponseBean.getUserDetails()) {
                userId = workFlowResponseBean.getUserDetails().getUserId();
            }
            transactionResponse.setVpa(getVpa(workFlowResponseBean, userId));
        }

        if (StringUtils.isNotBlank(requestData.getSubsLinkId())) {
            setResponseForSubsLinkPaymentNotDone(transactionResponse, workFlowResponseBean.getTransID());
        }

        if (transactionResponse.isSubsLinkInfo() && workFlowResponseBean.getQueryPaymentStatus() != null) {
            Date date = workFlowResponseBean.getQueryPaymentStatus().getPaidTime();
            if (date == null && workFlowResponseBean.getQueryTransactionStatus() != null) {
                date = workFlowResponseBean.getQueryTransactionStatus().getCreatedTime();
            }
            transactionResponse.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
        }

        if (workFlowResponseBean.getQueryPaymentStatus() != null
                && StringUtils.isNotBlank(workFlowResponseBean.getQueryPaymentStatus().getPwpCategory())) {
            changeResponseForPwpMerchant(transactionResponse);
        }

        if (OFFLINE.equalsIgnoreCase(requestData.getSource())
                && ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(), SET_ADDITIONAL_PARAM_IN_RESPONSE, false)) {
            transactionResponse.setAdditionalParam(getAdditionalParam(workFlowResponseBean));

            MerchantInfo merchantInfo = transactionResponse.getAdditionalParam().getMerchantInfo();
            if (null == merchantInfo) {
                merchantInfo = new MerchantInfo();
                transactionResponse.getAdditionalParam().setMerchantInfo(merchantInfo);
            }

            if (null != workFlowResponseBean.getQueryTransactionStatus()
                    && MapUtils.isNotEmpty(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo())) {
                merchantInfo.setCurrentTxnCount(workFlowResponseBean.getQueryTransactionStatus().getCurrentTxnCount());
                merchantInfo.setLogoUrl(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo().get(LOGO_URL));
            } else if (null != workFlowResponseBean.getQueryPaymentStatus()
                    && !CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo())) {
                merchantInfo.setLogoUrl(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo().get(LOGO_URL));
            }

            if (null != merchantInfo) {
                MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(requestData.getMid());
                MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService
                        .getMerchantInfo(merchantDetailsRequest);

                if (merchantDetailsResponse != null & merchantDetailsResponse.getMerchantImageName() != null)
                    merchantInfo.setLogoUrl(merchantDetailsResponse.getMerchantImageName());
            }

        }
        // addRegionalFieldInPTCResponse(transactionResponse);
        return transactionResponse;
    }

    private String getVpa(WorkFlowResponseBean workFlowResponseBean, String custId) {

        String vpa = getVpaFromPayChannelInfo(workFlowResponseBean);
        if (StringUtils.isBlank(vpa)) {
            vpa = getVpaFromExtendInfo(workFlowResponseBean);
        }
        if (StringUtils.isBlank(vpa)) {
            vpa = "";
        }
        return vpa;
    }

    private String getVpaFromExtendInfo(WorkFlowResponseBean workFlowResponseBean) {

        String vpaFromTxnStatus = Optional.ofNullable(workFlowResponseBean)
                .map(t -> workFlowResponseBean.getQueryTransactionStatus())
                .map(t -> workFlowResponseBean.getQueryTransactionStatus().getExtendInfo())
                .map(t -> getVirtualPaymentAddr(workFlowResponseBean.getQueryTransactionStatus().getExtendInfo()))
                .orElse(null);

        if (StringUtils.isNotBlank(vpaFromTxnStatus)) {
            return vpaFromTxnStatus;
        }

        return Optional.ofNullable(workFlowResponseBean).map(t -> workFlowResponseBean.getQueryPaymentStatus())
                .map(t -> workFlowResponseBean.getQueryPaymentStatus().getExtendInfo())
                .map(t -> getVirtualPaymentAddr(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo()))
                .orElse(null);
    }

    private String getVpaFromPayChannelInfo(WorkFlowResponseBean workFlowResponseBean) {
        String vpa = getVpaFromQueryPaymentStatus(workFlowResponseBean);
        if (StringUtils.isBlank(vpa)) {
            vpa = getVpaFromQueryTxnStatus(workFlowResponseBean);
        }
        return vpa;
    }

    private String getVpaFromQueryTxnStatus(WorkFlowResponseBean workFlowResponseBean) {
        List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptions = null;

        if (workFlowResponseBean != null && workFlowResponseBean.getQueryTransactionStatus() != null) {
            payOptions = workFlowResponseBean.getQueryTransactionStatus().getPayOptions();
        }
        return getVPAFromPayOptions(payOptions);
    }

    private String getVpaFromQueryPaymentStatus(WorkFlowResponseBean workFlowResponseBean) {
        List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptions = null;

        if (workFlowResponseBean != null && workFlowResponseBean.getQueryPaymentStatus() != null) {
            payOptions = workFlowResponseBean.getQueryPaymentStatus().getPayOptions();
        }
        return getVPAFromPayOptions(payOptions);
    }

    private String getVPAFromPayOptions(List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOptions) {

        if (CollectionUtils.isEmpty(payOptions) || null == payOptions.get(0)
                || null == payOptions.get(0).getPayChannelInfo()) {
            return null;
        }
        return payOptions.get(0).getPayChannelInfo().getVirtualPaymentAddr();
    }

    private String generateMerchantReponseFromPaymentStatus(WorkFlowResponseBean workFlowResponseBean,
            PaymentRequestBean requestData) {
        LOGGER.info("WorkFlowResponseBean : {}", workFlowResponseBean);
        TransactionResponse transactionResponse = getTxnResponsePaymentNotDone(workFlowResponseBean, requestData);
        if (ERequestType.LINK_BASED_PAYMENT.name().equals(transactionResponse.getRequestType())) {
            setLinkPaymentDataInTransactionResponse(workFlowResponseBean, transactionResponse, requestData);
        }
        String responsePage = getFinalHtmlResponse(transactionResponse);
        if (StringUtils.isNotBlank(responsePage)) {
            return responsePage;
        }
        LOGGER.error("Could not fetch redirect page");
        throw new TheiaServiceException("Unable to Send Response to Merchant");
    }

    public void setResponseMessageAndCode(TransactionResponse response, String paytmResponseCode, String bankName,
            CashierPaymentStatus paymentStatus) {

        String responseCode = MapperUtils.getResponseCode(response.getTransactionStatus(), paytmResponseCode, null);
        response.setResponseCode(responseCode);
        try {
            if (StringUtils.isNotBlank(paymentStatus.getErrorMessage())) {
                response.setResponseMsg(paymentStatus.getErrorMessage());
            } else {
                ResponseCodeDetails responseCodeDetails = responseCodeService.getPaytmResponseCodeDetails(responseCode);
                EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for responseCode : {}",
                        responseCodeDetails, responseCode);
                if (responseCodeDetails != null) {
                    response.setResponseMsg(StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                            .getDisplayMessage() : responseCodeDetails.getRemark());
                }
                if (responseCodeDetails == null) {
                    String responseMsg = ConfigurationUtil.getProperty(responseCode,
                            ApiResponse.SALE_PENDING_WITHOUT_BANKNAME.getResponseMessage());
                    response.setResponseMsg(responseMsg);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching ResponseCodeDetails:::{}", e);
        }
    }

    private ResponseCodeDetails fetchResponseMessageAndCode(String status) {

        String responseCode = MapperUtils.getResponseCode(status, null, null);

        ResponseCodeDetails responseCodeDetails = null;

        try {

            responseCodeDetails = responseCodeService.getPaytmResponseCodeDetails(responseCode);
            EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for responseCode : {}",
                    responseCodeDetails, responseCode);

        } catch (Exception e) {

            LOGGER.error("Exception occured while fetching ResponseCodeDetails:::{}", e);
        }

        return responseCodeDetails;
    }

    private void setPromoCodeDetails(final TransactionResponse response, final CashierPaymentStatus paymentStatus) {
        String promoCode = paymentStatus.getExtendInfo().get(ExtendedInfoPay.PROMO_CODE) != null ? paymentStatus
                .getExtendInfo().get(ExtendedInfoPay.PROMO_CODE) : paymentStatus.getExtendInfo().get(
                ExtendedInfoPay.INAVLID_PROMOCODE);
        response.setPromoCode(promoCode);
        response.setPromoResponseCode(paymentStatus.getExtendInfo().get(ExtendedInfoPay.PROMO_RESPONSE_CODE));
        response.setPromoApplyResultStatus(paymentStatus.getExtendInfo().get(
                ExtendedInfoPay.PROMO_CODE_APPLY_RESULT_STATUS));
    }

    private void putMaskedCardNo(StringBuilder strBuilder, Map<String, String> paramMap, String maskedCardNo) {
        if (StringUtils.isNotBlank(maskedCardNo)) {
            strBuilder.append(replace(ResponseConstants.MASKED_CARD_NO, maskedCardNo));
            paramMap.put(ResponseConstants.MASKED_CARD_NO, maskedCardNo);
        }
    }

    private void putAdditionalParam(StringBuilder strBuilder, Map<String, String> paramMap,
            AdditionalParam callBackParams) {
        if (null != callBackParams) {
            strBuilder.append(replace(ResponseConstants.ADDITIONAL_PARAM, callBackParams.toString()));
            paramMap.put(ResponseConstants.ADDITIONAL_PARAM, callBackParams.toString());
        }
    }

    private void putTxnToken(StringBuilder strBuilder, Map<String, String> paramMap, String txnToken) {
        if (null != txnToken) {
            strBuilder.append(replace(ResponseConstants.TXN_TOKEN, txnToken.toString()));
            paramMap.put(ResponseConstants.TXN_TOKEN, txnToken.toString());
        }
    }

    private void putCardIndexNo(StringBuilder strBuilder, Map<String, String> paramMap, String cardIndexNo) {
        if (StringUtils.isNotBlank(cardIndexNo)) {
            strBuilder.append(replace(ResponseConstants.CARD_INDEX_NO, cardIndexNo));
            paramMap.put(ResponseConstants.CARD_INDEX_NO, cardIndexNo);
        }
    }

    private void putCardHash(StringBuilder strBuilder, Map<String, String> paramMap, String cardHash) {
        if (StringUtils.isNotBlank(cardHash)) {
            strBuilder.append(replace(ResponseConstants.CARD_HASH, cardHash));
            paramMap.put(ResponseConstants.CARD_HASH, cardHash);
        }
    }

    private String getUserEmailFromExtendInfo(Map<String, String> extendInfo) {
        String userEmail = null;
        if (!extendInfo.isEmpty()) {
            userEmail = extendInfo.get(TheiaConstant.ExtendedInfoKeys.USER_EMAIL);

        }
        return userEmail;
    }

    private String getUserMobileFromExtendInfo(Map<String, String> extendInfo) {
        String userMobile = null;
        if (!extendInfo.isEmpty()) {
            userMobile = extendInfo.get(TheiaConstant.ExtendedInfoKeys.USER_MOBILE);

        }
        return userMobile;
    }

    private String getPaytmProperty(String propertyName) {
        PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(propertyName);
        EXT_LOGGER.customInfo("Mapping response - PaytmProperty :: {}", paytmProperty);
        if (paytmProperty != null) {
            return paytmProperty.getValue();
        }
        return StringUtils.EMPTY;
    }

    private static List<String> stringToList(String toSplit, String delimiter) {
        String[] arr = toSplit.split(delimiter);
        return Arrays.asList(arr);
    }

    private void putUserEmail(StringBuilder strBuilder, Map<String, String> paramMap, String userEmail) {
        if (StringUtils.isNotBlank(userEmail)) {
            strBuilder.append(replace(ResponseConstants.USER_EMAIL, userEmail));
            paramMap.put(ResponseConstants.USER_EMAIL, userEmail);
        }
    }

    private void putUserMobile(StringBuilder strBuilder, Map<String, String> paramMap, String userMobile) {
        if (StringUtils.isNotBlank(userMobile)) {
            strBuilder.append(replace(ResponseConstants.USER_MOBILE, userMobile));
            paramMap.put(ResponseConstants.USER_MOBILE, userMobile);
        }
    }

    private Map<String, Object> transformResponseFromWrapper(Map<String, Object> merchantResponseParams, String mid,
            String clientId) {

        Map<String, Object> merchantResponse = new HashMap<>();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version='1.0' encoding='utf-8'?><EmitraData ")
                .append("PRN='" + merchantResponseParams.get("PRN") + "' ")
                .append("AMT='" + merchantResponseParams.get("AMT") + "' ")
                .append("UNIQUE_TRANSACTION_ID='" + merchantResponseParams.get("UNIQUE_TRANSACTION_ID") + "' ")
                .append("BANKTIMESTAMP='" + merchantResponseParams.get("BANKTIMESTAMP") + "' ")
                .append("STATUS='" + merchantResponseParams.get("STATUS") + "' ")
                .append("STATUSDESC='" + merchantResponseParams.get("STATUSDESC") + "' ")
                .append("MODE='" + merchantResponseParams.get("MODE") + "' ")
                .append("EMITRATIMESTAMP='" + merchantResponseParams.get("EMITRATIMESTAMP") + "' ")
                .append("MODE_BID='" + merchantResponseParams.get("MODE_BID") + "' ")
                .append("MODE_BANK_NAME='" + merchantResponseParams.get("MODE_BANK_NAME") + "'").append(" />");

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
        String encrypted;

        try {
            byte[] key = EmitraChecksumUtility.setKey(CryptoUtils.decrypt(merchantKey));
            encrypted = EmitraChecksumUtility.encryptAES(xml.toString().replaceAll("\"", ""), key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            LOGGER.error("Error while setting merchant key for encrypting response ");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return null;
        } catch (Exception e) {
            LOGGER.error("Error while encrypting response ");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return null;
        }

        merchantResponse.put("encdata", encrypted);

        return merchantResponse;
    }

    private CashierPaymentStatus getPaymentStatusForSuccessfulTransaction(CashierTransactionStatus transactionStatus) {
        for (PaymentView paymentView : transactionStatus.getPaymentViews()) {
            if (paymentView.getExtendInfo() != null
                    && PaymentStatus.SUCCESS.toString().equals(paymentView.getExtendInfo().get("paymentStatus"))) {
                try {
                    QueryPayResultRequestBean queryPayResultRequestBean = new QueryPayResultRequestBean(
                            paymentView.getCashierRequestId());
                    queryPayResultRequestBean.setRoute(Routes.PG2);
                    final GenericCoreResponseBean<PayResultQueryRequest> payResultQueryRequest = paymentHelper
                            .queryPayResultRequest(queryPayResultRequestBean);
                    final PayResultQueryResponse payResultQueryResponse = cashier.payResultQuery(payResultQueryRequest
                            .getResponse());
                    return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody());
                } catch (FacadeCheckedException e) {
                    LOGGER.error("JSON mapping exception :", e);
                }
            }
        }
        return null;
    }

    public PageDetailsResponse getPageDetailsResponse(PaymentRequestBean requestData,
            com.paytm.pgplus.pgproxycommon.enums.ResponseConstants responseConstant) {
        String html = merchantResponseService.processMerchantFailResponse(requestData, responseConstant);

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setHtmlPage(html);
        pageDetailsResponse.setSuccessfullyProcessed(false);
        return pageDetailsResponse;
    }

    public String getErrorMsgForNativeEnhancedPaymentFailure(DoPaymentResponse doPaymentResponse) {
        CashierPaymentStatus paymentStatus = doPaymentResponse.getPaymentStatus();
        CashierTransactionStatus transactionStatus = doPaymentResponse.getTransactionStatus();
        String txnStatus = MapperUtils.getTransactionStatusForResponse(transactionStatus, paymentStatus);
        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(
                paymentStatus.getInstErrorCode(), null, txnStatus);
        return responseCodeUtil.getResponseMsg(responseCodeDetails);
    }

    public void setClientIdIfNotEmpty(CashierPaymentStatus paymentStatus, TransactionResponse response) {
        if (paymentStatus.getExtendInfo() != null
                && StringUtils.isNotBlank(paymentStatus.getExtendInfo().get(ExtendedInfoPay.CLIENT_ID)))
            response.setClientId(paymentStatus.getExtendInfo().get(ExtendedInfoPay.CLIENT_ID));
    }

    private void putVPA(StringBuilder strBuilder, Map<String, String> paramMap, String vpa) {

        if (vpa == null) {
            vpa = StringUtils.EMPTY;
        }
        strBuilder.append(replace(ResponseConstants.VPA, vpa));
        paramMap.put(ResponseConstants.VPA, vpa);
    }

    private void putFundSourceVerificationSuccess(StringBuilder stringBuilder, Map<String, String> paramMap,
            String isFundSourceVerificationSuccess) {
        if (isFundSourceVerificationSuccess == null) {
            isFundSourceVerificationSuccess = StringUtils.EMPTY;
        }
        stringBuilder.append(replace(ResponseConstants.ACCOUNTNUMBERVALIDATED, isFundSourceVerificationSuccess));
        paramMap.put(ResponseConstants.ACCOUNTNUMBERVALIDATED, isFundSourceVerificationSuccess);
    }

    private String getPaytmResponseCode(String instErrorCode) {
        String paytmResponseCode = null;
        ResponseCodeDetails responseCodeDetails = null;
        try {
            responseCodeDetails = responseCodeService.getResponseCodeDetails(instErrorCode);
            EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for instErrorCode : {}",
                    responseCodeDetails, instErrorCode);

            if ((responseCodeDetails != null) && (responseCodeDetails.getPaytmResponseCode() != null)) {
                paytmResponseCode = String.valueOf(responseCodeDetails.getPaytmResponseCode());
            }

        } catch (Exception e) {
            LOGGER.error("Unable to find paytm response code ", e);
        }
        return paytmResponseCode;
    }

    private ResponseCodeDetails getResponseCodeDetailsFromInstErrorCode(String instErrorCode) {
        ResponseCodeDetails responseCodeDetails = null;
        try {
            responseCodeDetails = responseCodeService.getResponseCodeDetails(instErrorCode);
            EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for instErrorCode : {}",
                    responseCodeDetails, instErrorCode);

            if (responseCodeDetails == null) {
                /*
                 * this is fallback case
                 */
                responseCodeDetails = responseCodeService.getResponseCodeDetails(DEFAULT_FALLBACK_INST_ERROR_CODE);
                EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for instErrorCode : {}",
                        responseCodeDetails, DEFAULT_FALLBACK_INST_ERROR_CODE);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to find paytm response code ", e);
        }
        // making every failure retriable
        responseCodeDetails.setRetryPossible(true);
        return responseCodeDetails;
    }

    private String getPaytmRespCodeFromResponseCodeDetail(ResponseCodeDetails responseCodeDetails) {
        String paytmResponseCode = null;
        if ((responseCodeDetails != null) && (responseCodeDetails.getPaytmResponseCode() != null)) {
            paytmResponseCode = String.valueOf(responseCodeDetails.getPaytmResponseCode());
        }
        return paytmResponseCode;
    }

    private String getResponseMsgFromResponseCodeDetail(WorkFlowResponseBean workFlowResponseBean,
            ResponseCodeDetails respCodeDetails) {

        if (StringUtils.isNotEmpty(workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode())
                && StringUtils.isNumeric(workFlowResponseBean.getQueryPaymentStatus().getInstErrorCode())
                && StringUtils.isNotBlank(workFlowResponseBean.getQueryPaymentStatus().getErrorMessage())) {
            return workFlowResponseBean.getQueryPaymentStatus().getErrorMessage();
        }

        try {

            if (respCodeDetails != null && StringUtils.isNotBlank(respCodeDetails.getDisplayMessage())) {
                return respCodeDetails.getDisplayMessage();
            } else if (respCodeDetails != null && StringUtils.isNotBlank(respCodeDetails.getRemark())) {
                return respCodeDetails.getRemark();
            } else {
                /*
                 * this is Fallback case
                 */
                LOGGER.info("getting ResponseCodeDetails for fallback case");
                ResponseCodeDetails responseCodeDetails = responseCodeService
                        .getResponseCodeDetails(DEFAULT_FALLBACK_INST_ERROR_CODE);
                EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for instErrorCode : {}",
                        responseCodeDetails, DEFAULT_FALLBACK_INST_ERROR_CODE);

                if (responseCodeDetails != null) {
                    return StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                            .getDisplayMessage() : responseCodeDetails.getRemark();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching ResponseCodeDetails :: {}", e);
        }
        return "Something went wrong";
    }

    private String getFundSourceVerificationSuccess(CashierPaymentStatus paymentStatus) {
        String isFundSourceVerificationSuccess = null;
        if (paymentStatus != null && paymentStatus.getPayOptions() != null) {
            for (PayOption po : paymentStatus.getPayOptions()) {
                if (po != null && po.getExtendInfo() != null && ExtraConstants.UPI.equals(po.getPayMethodName())) {
                    isFundSourceVerificationSuccess = po.getExtendInfo().get(
                            TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys.IS_FUND_SOURCE_VERIFICATION_SUCCESS);
                }
            }
        }
        return isFundSourceVerificationSuccess;
    }

    public NativeJsonResponse returnNativeKycJsonPage(HttpServletRequest request, HttpServletResponse response,
            Map<String, String[]> additionalParams) {

        String kycKey = "Native_KYC_" + request.getAttribute(TheiaConstant.RequestParams.Native.TXN_TOKEN);
        nativeSessionUtil.setKey(kycKey, additionalParams, 900);

        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        BankForm bankForm = new BankForm();
        bankForm.setPageType("redirect");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("?mid=").append(request.getParameter(MID));
        queryBuilder.append("&orderId=").append(request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID));

        FormDetail formDetail = new FormDetail();
        formDetail.setActionUrl(ConfigurationUtil.getProperty("theia.base.url") + "/theia/api/v1/nativeKycPage"
                + queryBuilder);
        formDetail.setMethod("POST");
        formDetail.setType("redirect");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> content = new HashMap<>();
        content.put(KYC_TXN_ID, (String) request.getAttribute(TheiaConstant.RequestParams.Native.TXN_TOKEN));
        content.put(KYC_FLOW, "YES");
        content.put(KYC_MID, request.getParameter(MID));
        content.put(TheiaConstant.RequestParams.ORDER_ID,
                request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID));

        formDetail.setHeaders(headers);
        formDetail.setContent(content);

        bankForm.setRedirectForm(formDetail);
        body.setBankForm(bankForm);

        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);

        return nativeJsonResponse;
    }

    private boolean isRequestOfflineFlow(QueryPaymentStatus transactionStatus) {
        if (transactionStatus != null && transactionStatus.getExtendInfo() != null
                && StringUtils.equals(TRUE, transactionStatus.getExtendInfo().get(OFFLINE_FLOW))) {
            return true;
        }
        return false;

    }

    private String getSavedCardId(CashierTransactionStatus transactionStatus) {
        try {
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(transactionStatus.getPaymentViews())
                    && org.apache.commons.collections.CollectionUtils.isNotEmpty(transactionStatus.getPaymentViews()
                            .get(0).getPayOptionInfos())) {
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(transactionStatus.getPaymentViews()
                        .get(0).getPayOptionInfos())) {
                    for (PayOptionInfo payOptionInfo : transactionStatus.getPaymentViews().get(0).getPayOptionInfos()) {
                        if (PayMethod.CREDIT_CARD.getMethod()
                                .equalsIgnoreCase(payOptionInfo.getPayMethod().getMethod())
                                || PayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(
                                        payOptionInfo.getPayMethod().getMethod())) {
                            Map<String, String> payOptionBillExtendInfo = payOptionInfo.getPayOptionBillExtendInfo();
                            if (MapUtils.isNotEmpty(payOptionBillExtendInfo)
                                    && payOptionBillExtendInfo.containsKey("savedCardId")) {
                                return payOptionBillExtendInfo.get("savedCardId");
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Exception occured while fetching saved card id", e);
        }
        LOGGER.info("Found nothing so returning null as savedCardId");
        return null;
    }

    private boolean redirectToShowPaymentPage(PaymentRequestBean requestData, WorkFlowResponseBean workFlowResponseBean) {
        /*
         * If the Native txn has failed and paymentMode is PPI/wallet,
         * paymentErrorCode=BALANCE_NOT_ENOUGH and retry is allowed, we direct
         * txn to /showPaymentPage API, so that the user can do addAndPay txn
         */
        return workFlowResponseBean.getQueryPaymentStatus() != null
                && (StringUtils.equals(workFlowResponseBean.getQueryPaymentStatus().getPaymentErrorCode(),
                        "BALANCE_NOT_ENOUGH") || StringUtils.equals(workFlowResponseBean.getQueryPaymentStatus()
                        .getInstErrorCode(), "BALANCE_NOT_ENOUGH"))
                && (ERequestType.NATIVE.getType().equals(requestData.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION
                        .getType().equals(requestData.getRequestType())) && requestData.isNativeRetryEnabled()
                && StringUtils.equals(EPayMode.ADDANDPAY.getValue(), requestData.getPaymentFlowExpectedNative())
                && StringUtils.equals(requestData.getPaymentTypeId(), PaymentTypeIdEnum.PPI.value)
                && !processTransactionUtil.isNativeJsonRequest(requestData)
                && !processTransactionUtil.isEnhancedNativeFlow(requestData.getRequest());
    }

    private void createResponseToRedirectToCashierPage(TransactionResponse transactionResponse, String mid,
            String orderId, String txnToken) {

        /*
         * Here, the callBackUrl is /showPaymentPage API
         */
        StringBuilder callBackUrl = new StringBuilder(nativeDirectBankPageHelper.getEnhanceNativeCallBackUrl(mid,
                orderId));
        callBackUrl.append("&txnToken=").append(txnToken);

        transactionResponse.setMid(mid);
        transactionResponse.setOrderId(orderId);
        transactionResponse.setCallbackUrl(callBackUrl.toString());

        LOGGER.info("setting callback to showPaymentPage API");
    }

    public void setDataRedirectToShowPaymentPageAPI(Map<String, String[]> additionalParams, HttpServletResponse response)
            throws IOException {
        if (additionalParams == null) {
            return;
        }
        String mid = additionalParams.get(RequestParams.MID)[0];
        String orderId = additionalParams.get(ORDER_ID)[0];
        String txnToken = additionalParams
                .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.TXN_TOKEN)[0];

        TransactionResponse transactionResponse = new TransactionResponse();
        createResponseToRedirectToCashierPage(transactionResponse, mid, orderId, txnToken);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", transactionResponse.getTransactionStatus());
            responseMap.put("RESPONSE_MESSAGE", transactionResponse.getResponseMsg());
            statsDUtils.pushResponse("api/v1/processTransaction", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "api/v1/processTransaction" + "to grafana", exception);
        }

        if (processTransactionUtil.isNativeJsonRequest(((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest())) {
            createResponseForNativeJsonRequestRedirectToShowPaymentPageAPI(transactionResponse, response);
            return;
        }

        String responsePage = getFinalHtmlResponse(transactionResponse);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().print(responsePage);
    }

    private void createResponseForNativeJsonRequestRedirectToShowPaymentPageAPI(
            TransactionResponse transactionResponse, HttpServletResponse response) {
        NativeJsonResponseBody body = new NativeJsonResponseBody();
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();

        com.paytm.pgplus.response.ResultInfo nativeResultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
        addRegionalFieldInPTCResponse(nativeResultInfo);
        Map<String, String> paramMap = new TreeMap<>();

        merchantResponseService.makeReponseToMerchantEnhancedNative(transactionResponse, paramMap);

        body.setResultInfo(nativeResultInfo);
        body.setTxnInfo(paramMap);
        body.setCallBackUrl(transactionResponse.getCallbackUrl());

        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(getJSONString(nativeJsonResponse));
        } catch (IOException e) {
            LOGGER.error("error in setting json response");
        }
    }

    private String getJSONString(Object obj) {
        String responseJson = "{}";
        try {
            responseJson = JsonMapper.mapObjectToJson(obj);
        } catch (FacadeCheckedException fce) {
            LOGGER.info("failed mapping object to Json");
        }
        return responseJson;
    }

    private String getVPA(CashierTransactionStatus transactionStatus, Map<String, String> extendInfo, String custId) {

        if (null == transactionStatus || CollectionUtils.isEmpty(transactionStatus.getPaymentViews())) {
            return getVirtualPaymentAddr(extendInfo);
        }

        List<PaymentView> paymentViews = transactionStatus.getPaymentViews();

        for (PaymentView view : paymentViews) {
            if (null == view || CollectionUtils.isEmpty(view.getPayOptionInfos())) {
                continue;
            }
            for (PayOptionInfo payOptionInfo : view.getPayOptionInfos()) {
                if (null != payOptionInfo.getPayChannelInfo()
                        && StringUtils.isNotBlank(payOptionInfo.getPayChannelInfo().getVirtualPaymentAddr())) {
                    LOGGER.info("Getting VPA from transactionStatus response");
                    return payOptionInfo.getPayChannelInfo().getVirtualPaymentAddr();
                }
            }
        }
        return getVirtualPaymentAddr(extendInfo);
    }

    public boolean checkIfEnhanceCashierPageRequest(boolean isFundOrder,
            CashierTransactionStatus cashierTransactionStatus, CashierFundOrderStatus cashierFundOrderStatus) {
        Map<String, String> extendInfo = null;
        if (isFundOrder) {
            extendInfo = Optional.ofNullable(cashierFundOrderStatus).map(CashierFundOrderStatus::getExtendInfo)
                    .orElse(null);
        } else {
            extendInfo = Optional.ofNullable(cashierTransactionStatus).map(CashierTransactionStatus::getExtendInfo)
                    .orElse(null);
        }
        return (null != extendInfo) && TRUE.equals(extendInfo.get(IS_ENHANCED_NATIVE));
    }

    public String getCustomPaymentConfirmationPage(Map<String, Object> merchantResponseParams, String mid) {
        JSONObject replaceJson = new JSONObject();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String OdishaTxnDate = null;

        try {
            Date txndate = simpleDateFormat.parse(merchantResponseParams.get("BKTRNTIME").toString());
            OdishaTxnDate = simpleDateFormat2.format(txndate);
        } catch (Exception e) {
            LOGGER.error("Exception ocurred while parsing date");
        }

        replaceJson.put("msg", merchantResponseParams.get("msg"));
        replaceJson.put("RU", merchantResponseParams.get("RU"));
        replaceJson.put("PAYEENM", merchantResponseParams.get("PAYEENM"));
        replaceJson.put("CHLNREFNO", merchantResponseParams.get("CHLNREFNO"));
        replaceJson.put("BKTRNID", merchantResponseParams.get("BKTRNID"));
        replaceJson.put("BKTRNTIME", OdishaTxnDate);
        replaceJson.put("TOTAMT", merchantResponseParams.get("TOTAMT"));
        replaceJson.put("TIMECOUNTER", merchantResponseParams.get("TIMECOUNTER"));
        replaceJson.put("MERCHANTNAME", merchantResponseParams.get("MERCHANTNAME"));
        replaceJson.put("DISPLAYMESSAGE", merchantResponseParams.get("BKTRNMSG"));
        String txnStatusCode = "0";
        if (TXN_SUCCESS.equalsIgnoreCase(merchantResponseParams.get("BKTRNSTS").toString())) {
            txnStatusCode = "1";
        } else if (PENDING_STATUS.equalsIgnoreCase(merchantResponseParams.get("BKTRNSTS").toString())) {
            txnStatusCode = "2";
        }
        replaceJson.put("TXNSTATUS", txnStatusCode);

        LOGGER.info("Replaced json for Odisha IMFS {}", replaceJson);
        String responseJson = new String(Base64.getEncoder().encode((replaceJson).toString().getBytes()));

        String htmlPage = ConfigurationUtil.getWrapperConfirmationPage();
        String theiaBasePath = ConfigurationUtil.getProperty("theia.base.path");
        htmlPage = htmlPage.replace("{action}", theiaBasePath + "/odisha")
                .replace("{msg}", merchantResponseParams.get("msg").toString()).replace("{json}", responseJson)
                .replace("{merchantCode}", mid);

        if (StringUtils.isNotBlank(htmlPage)) {
            htmlPage = htmlPage.replace("PUSH_JSON_DATA", responseJson);
        } else {
            throw new TheiaServiceException("Custom Payment page html page not present!");
        }
        return htmlPage;
    }

    private void changeResponseForPwpMerchant(TransactionResponse response) {
        LOGGER.info("Transforming response for PWP merchant");
        pwpTransactionResponseBuilder.changeTransactionResponse(response);

    }

    public TransactionResponse createTransactionResponseForNativeCloseOrder(InitiateTransactionRequestBody orderDetail,
            PaymentRequestBean paymentRequestData, String transID) throws Exception {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setMid(orderDetail.getMid());
        transactionResponse.setTxnId(transID);
        transactionResponse.setOrderId(orderDetail.getOrderId());
        if (orderDetail.getUserInfo() != null) {
            transactionResponse.setCustId(orderDetail.getUserInfo().getCustId());
        }

        // change for cancel
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(orderDetail.getTxnAmount()
                .getValue()));

        ExtendInfo extendedInfo = orderDetail.getExtendInfo();
        if (extendedInfo != null) {
            if (StringUtils.isNotBlank(extendedInfo.getMercUnqRef()))
                transactionResponse.setMerchUniqueReference(extendedInfo.getMercUnqRef());
            if (StringUtils.isNotBlank(paymentRequestData.getClientId()))
                transactionResponse.setClientId(paymentRequestData.getClientId());
        }

        if (paymentRequestData.getExtraParamsMap() != null) {
            transactionResponse.setExtraParamsMap(paymentRequestData.getExtraParamsMap());
        } else {
            transactionResponse.setExtraParamsMap(Collections.emptyMap());
        }

        transactionResponse.setRequestType(orderDetail.getRequestType());

        MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantDataServiceImpl
                .getMerchantExtendedData(orderDetail.getMid());
        EXT_LOGGER.customInfo("Mapping response - MerchantExtendedInfoResponse :: {}", merchantExtendedInfoResponse);
        String alipayMerchantId = merchantExtendedInfoResponse.getExtendedInfo().getAlipayMid();
        String merchantOrderId = orderDetail.getOrderId();

        QueryByMerchantTransIdRequestBody orderQueryRequestBody = new QueryByMerchantTransIdRequestBody(
                alipayMerchantId, merchantOrderId, true);
        orderQueryRequestBody.setRoute(routerUtil.getRoute(orderDetail.getMid(), orderDetail.getOrderId(),
                "QueryByMerchantTransId-CloseOrder"));
        QueryByMerchantTransIdRequest queryByMerchantTransIdRequest = new QueryByMerchantTransIdRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), orderQueryRequestBody);
        queryByMerchantTransIdRequest.getHead().setMerchantId(orderDetail.getMid());
        QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = acquiringOrder
                .queryByMerchantTransId(queryByMerchantTransIdRequest);

        if (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(orderDetail.getRequestType())
                || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(orderDetail.getRequestType())) {
            String theiaRedirectionPath = com.paytm.pgplus.theia.utils.ConfigurationUtil
                    .getProperty(THEIA_BUISNESS_BASE_PATH);
            if (theiaRedirectionPath != null) {
                transactionResponse.setCallbackUrl(theiaRedirectionPath + "/linkPaymentRedirect");
            } else {
                LOGGER.error("Theia redirection path not found in properties file for link based payment");
            }
            transactionResponse.setLinkBasedPayment("true");

            LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                    .get(LINK_BASED_KEY + transactionResponse.getTxnId());
            if (linkBasedMerchantInfo != null) {
                transactionResponse.setMerchantName(linkBasedMerchantInfo.getMerchantName());
                transactionResponse.setMerchantImage(linkBasedMerchantInfo.getMerchantImage());
            }

            transactionResponse.setShowViewFlag(PAYMENT_SCREEN);

            if (queryByMerchantTransIdResponse.getBody() != null
                    && queryByMerchantTransIdResponse.getBody().getTimeDetail() != null) {
                Date orderCreatedTime = queryByMerchantTransIdResponse.getBody().getTimeDetail().getCreatedTime();
                transactionResponse.setTxnDate(LinkBasedPaymentHelper.getLinkPaymentStatusDate(orderCreatedTime));
            }
            transactionResponse.setTransactionStatus("CANCEL");

        } else {
            String callBackUrl = merchantResponseService.checkCallback(orderDetail.getCallbackUrl(),
                    orderDetail.getWebsiteName(), orderDetail.getChannelId(), orderDetail.getMid());
            transactionResponse.setCallbackUrl(callBackUrl);
            if (queryByMerchantTransIdResponse.getBody() != null
                    && !CollectionUtils.isEmpty(queryByMerchantTransIdResponse.getBody().getPaymentViews())
                    && queryByMerchantTransIdResponse.getBody().getPaymentViews().get(0) != null
                    && queryByMerchantTransIdResponse.getBody().getPaymentViews().get(0).getPayRequestExtendInfo() != null)
                transactionResponse.setChallanIdNum(queryByMerchantTransIdResponse.getBody().getPaymentViews().get(0)
                        .getPayRequestExtendInfo().get("extraParamsMap.challanIdNum"));
        }

        if (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(orderDetail.getRequestType())
                || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(orderDetail.getRequestType())) {
            // set Response-code and Response-Message
            responseCodeUtil.setRespMsgeAndCode(transactionResponse, null,
                    SystemResponseCode.LINK_USER_CLOSED_RESPONSE_CODE);
            if (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(orderDetail.getRequestType())) {
                transactionResponse.setLinkType(TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
            } else if (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(orderDetail.getRequestType())) {
                transactionResponse.setLinkType(TheiaConstant.LinkRiskParams.LINK_TYPE_NON_INVOICE);
            }
        } else {
            responseCodeUtil
                    .setRespMsgeAndCode(transactionResponse, null, SystemResponseCode.USER_CLOSED_RESPONSE_CODE);

        }

        return transactionResponse;

    }

    public void addRegionalFieldInPTCResponse(Object response) {
        if (processTransactionUtil.isRequestOfType(PTR_URL)) {
            localeFieldAspect.addLocaleFieldsInObject(response, PTR_URL);
        } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
            localeFieldAspect.addLocaleFieldsInObject(response, V1_PTC);
        }
    }

    private void setTxnPaidTime(TransactionResponse response, Object transactionStatus) {
        CashierTransactionStatus cashierTransactionStatus = null;
        QueryTransactionStatus queryTransactionStatus = null;
        List<Date> paidTimes = null;
        try {
            if (transactionStatus instanceof CashierTransactionStatus) {
                cashierTransactionStatus = (CashierTransactionStatus) transactionStatus;
                paidTimes = cashierTransactionStatus.getPaidTimes();
            } else if (transactionStatus instanceof QueryTransactionStatus) {
                queryTransactionStatus = (QueryTransactionStatus) transactionStatus;
                paidTimes = queryTransactionStatus.getPaidTimes();
            }

            if (!CollectionUtils.isEmpty(paidTimes)) {
                Date txnPaidTime = paidTimes.get(0);
                response.setTxnPaidTime(DateUtils.format(txnPaidTime));
            }
        } catch (Exception e) {
            LOGGER.error("Error while setting txnPaidTime : {} ", e.getMessage());
        }

    }

    private void putTxnPaidTime(StringBuilder strBuilder, Map<String, String> paramMap, String txnPaidTime) {
        if (StringUtils.isNotBlank(txnPaidTime)) {
            strBuilder.append(replace(ResponseConstants.TXN_PAID_TIME, txnPaidTime));
            paramMap.put(ResponseConstants.TXN_PAID_TIME, txnPaidTime);
        }
    }

    private boolean isPaymodeNBCCDC(CashierPaymentStatus paymentStatus) {
        if (!CollectionUtils.isEmpty(paymentStatus.getPayOptions())) {
            String payMethodName = paymentStatus.getPayOptions().get(0).getPayMethodName();
            if (StringUtils.equals(payMethodName, PayMethod.NET_BANKING.getMethod())
                    || StringUtils.equals(payMethodName, PayMethod.DEBIT_CARD.getMethod())
                    || StringUtils.equals(payMethodName, PayMethod.CREDIT_CARD.getMethod()))
                return true;
        }
        return false;
    }

    private boolean isWrapperAllowed(CashierTransactionStatus transactionStatus) {
        return transactionStatus != null
                && !CollectionUtils.isEmpty(transactionStatus.getExtendInfo())
                && transactionStatus.getExtendInfo().get("extraParamsMap.wrapperName") != null
                && (StringUtils
                        .equals("Rajasthan", transactionStatus.getExtendInfo().get("extraParamsMap.wrapperName"))
                        || StringUtils.equals(MANIPUR_WRAPPER,
                                transactionStatus.getExtendInfo().get("extraParamsMap.wrapperName")) || StringUtils
                            .equals(ASSAM_WRAPPER, transactionStatus.getExtendInfo().get("extraParamsMap.wrapperName")));
    }

    private void putDataForLinkPaymentInAppOnlineFlow(TransactionResponse response, StringBuilder strBuilder,
            Map<String, String> paramMap, InitiateTransactionRequestBody orderDetail) {

        if (orderDetail != null && orderDetail.getLinkDetailsData() != null
                && LinkBasedParams.APP.equalsIgnoreCase(orderDetail.getLinkDetailsData().getChannelId())) {
            LinkDetailResponseBody linkDetailResponseBody = orderDetail.getLinkDetailsData();
            LOGGER.info("Inside putDataForLinkPaymentInAppOnlineFlow for app");
            if (TXN_SUCCESS.equalsIgnoreCase(response.getTransactionStatus())
                    || SUCCESS.equalsIgnoreCase(response.getTransactionStatus())
                    || (ff4jUtils.isFeatureEnabled(COTP_REDIRECTION, false) && com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS
                            .equalsIgnoreCase(response.getTransactionStatus()))) {
                if (StringUtils.isNotBlank(linkDetailResponseBody.getCustomPaymentSuccessMessage())) {
                    strBuilder.append(replace(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_SUCCESS_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentSuccessMessage()));
                    paramMap.put(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_SUCCESS_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentSuccessMessage());
                }
                if (StringUtils.isNotBlank(linkDetailResponseBody.getRedirectionUrlSuccess())) {
                    strBuilder.append(replace(TheiaConstant.LinkBasedParams.REDIRECTION_URL_SUCCESS,
                            linkDetailResponseBody.getRedirectionUrlSuccess()));
                    paramMap.put(TheiaConstant.LinkBasedParams.REDIRECTION_URL_SUCCESS,
                            linkDetailResponseBody.getRedirectionUrlSuccess());
                }
            } else if (TXN_FAILURE.equalsIgnoreCase(response.getTransactionStatus())
                    || FAILURE.equalsIgnoreCase(response.getTransactionStatus())
                    || TXN_CANCEL.equalsIgnoreCase(response.getTransactionStatus())) {
                if (StringUtils.isNotBlank(linkDetailResponseBody.getCustomPaymentFailureMessage())) {
                    strBuilder.append(replace(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_FAILURE_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentFailureMessage()));
                    paramMap.put(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_FAILURE_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentFailureMessage());
                }
                if (StringUtils.isNotBlank(linkDetailResponseBody.getRedirectionUrlFailure())) {
                    strBuilder.append(replace(TheiaConstant.LinkBasedParams.REDIRECTION_URL_FAILURE,
                            linkDetailResponseBody.getRedirectionUrlFailure()));
                    paramMap.put(TheiaConstant.LinkBasedParams.REDIRECTION_URL_FAILURE,
                            linkDetailResponseBody.getRedirectionUrlFailure());
                }
            } else if (PENDING.equalsIgnoreCase(response.getTransactionStatus())
                    || PROCESSING.equalsIgnoreCase(response.getTransactionStatus())) {
                if (StringUtils.isNotBlank(linkDetailResponseBody.getCustomPaymentPendingMessage())) {
                    strBuilder.append(replace(LinkBasedParams.CUSTOM_PAYMENT_PENDING_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentPendingMessage()));
                    paramMap.put(LinkBasedParams.CUSTOM_PAYMENT_PENDING_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentPendingMessage());
                }
                if (StringUtils.isNotBlank(linkDetailResponseBody.getRedirectionUrlPending())) {
                    strBuilder.append(replace(LinkBasedParams.REDIRECTION_URL_PENDING,
                            linkDetailResponseBody.getRedirectionUrlPending()));
                    paramMap.put(LinkBasedParams.REDIRECTION_URL_PENDING,
                            linkDetailResponseBody.getRedirectionUrlPending());
                }
            }
        }
    }

    private BankResultInfo populateBankResultInfo(WorkFlowResponseBean workFlowResponseBean) {
        BankResultInfo bankResultInfo = new BankResultInfo();
        QueryPaymentStatus queryPaymentStatus = workFlowResponseBean.getQueryPaymentStatus();
        if (queryPaymentStatus != null) {
            List<com.paytm.pgplus.pgproxycommon.models.PayOption> payOption = queryPaymentStatus.getPayOptions();
            if (!CollectionUtils.isEmpty(payOption)) {
                Map<String, String> extendedInfo = payOption.get(0).getExtendInfo();
                if (!extendedInfo.isEmpty()) {
                    String response = extendedInfo.get("bankResponseCode");
                    if (StringUtils.isNotEmpty(response)) {
                        String parser = response.replaceAll("[{}]", "");
                        if (parser.startsWith("ResultInfo")) {
                            parser = parser.substring(parser.indexOf("(") + 1, parser.lastIndexOf(")"));
                        }
                        String[] token = parser.split(",");
                        HashMap<String, String> bankResultInfoMap = new HashMap<>();
                        String previousKey = null;
                        for (int i = 0; i <= token.length - 1; i++) {
                            if (token[i].contains("=")) {
                                String[] v = token[i].split("=");
                                bankResultInfoMap.put(v[0].trim(), v[1].trim());
                                previousKey = v[0].trim();
                            } else {
                                if (i > 0) {
                                    String value = bankResultInfoMap.get(previousKey) + "," + token[i];
                                    bankResultInfoMap.put(previousKey, value);
                                }
                            }
                        }
                        bankResultInfo.setBankResultCode(bankResultInfoMap.get("resultCodeId"));
                        bankResultInfo.setBankResultMsg(bankResultInfoMap.get("resultMsg"));
                        return bankResultInfo;
                    }
                }
            }
        }
        return null;
    }

    private BankResultInfo populateBankResultInfo(CashierPaymentStatus cashierPaymentStatus) {
        BankResultInfo bankResultInfo = new BankResultInfo();
        if (cashierPaymentStatus != null) {
            List<PayOption> payOption = cashierPaymentStatus.getPayOptions();
            if (!CollectionUtils.isEmpty(payOption)) {
                Map<String, String> extendedInfo = payOption.get(0).getExtendInfo();
                if (!extendedInfo.isEmpty()) {
                    String response = extendedInfo.get("bankResponseCode");
                    if (StringUtils.isNotEmpty(response)) {
                        String parser = response.replaceAll("[{}]", "");
                        if (parser.startsWith("ResultInfo")) {
                            parser = parser.substring(parser.indexOf("(") + 1, parser.lastIndexOf(")"));
                        }
                        String[] token = parser.split(",");
                        HashMap<String, String> bankResultInfoMap = new HashMap<>();
                        String previousKey = null;
                        for (int i = 0; i <= token.length - 1; i++) {
                            if (token[i].contains("=")) {
                                String[] v = token[i].split("=");
                                bankResultInfoMap.put(v[0].trim(), v[1].trim());
                                previousKey = v[0].trim();
                            } else {
                                if (i > 0) {
                                    String value = bankResultInfoMap.get(previousKey) + "," + token[i];
                                    bankResultInfoMap.put(previousKey, value);
                                }
                            }
                        }
                        bankResultInfo.setBankResultCode(bankResultInfoMap.get("resultCodeId"));
                        bankResultInfo.setBankResultMsg(bankResultInfoMap.get("resultMsg"));
                        return bankResultInfo;
                    }
                }
            }
        }
        return null;
    }

    private String getUpiStatusUrl(WorkFlowRequestBean workFlowRequestBean) {
        String theiaBasePath = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_PATH);
        String upiTxnStatusUrl = TheiaConstant.ExtraConstants.UPI_TXN_STATUS_URL;
        return theiaBasePath + upiTxnStatusUrl + "?MID=" + workFlowRequestBean.getPaytmMID() + "&ORDER_ID="
                + workFlowRequestBean.getOrderID();
    }

    private String getMerchantSolutionType(String mid) {
        String merchantAttributeResponse = null;
        Map<String, Object> merchantAttributeResponseMap;
        String merchantType = OFFLINE_MERCHANT;

        try {
            merchantAttributeResponse = merchantQueryService.getMerchantAttribute(mid, MERCHANT_PREFERENCE,
                    MerchantUserRequestType.PAYTM.getValue());
            EXT_LOGGER.customInfo("Mapping response - MerchantAttributeResponse :: {}", merchantAttributeResponse);
            if (merchantAttributeResponse != null) {
                merchantAttributeResponseMap = JsonMapper.mapJsonToObject(merchantAttributeResponse, Map.class);
                if (merchantAttributeResponseMap != null) {
                    if (merchantAttributeResponseMap.get(MERCHANT_SOLUTION_TYPE) != null) {
                        merchantType = (String) merchantAttributeResponseMap.get(MERCHANT_SOLUTION_TYPE);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching Solution Type for mid :{} ,error :{}", mid, e.getMessage());
            return null;
        }
        return merchantType;
    }

    public void pushPtcResultStatusToStatsD(String resultStatus) {
        if (StringUtils.equals(OfflinePaymentUtils.gethttpServletRequest().getRequestURI(), V1_PTC)
                && ff4jUtils.isFeatureEnabled(ENABLE_API_PERFORMANCE_LOGGER, false)) {
            Map<String, String> statsDInputMap = new LinkedHashMap<>();
            statsDInputMap.put(API_NAME, V1_PTC);
            HttpServletRequest request = OfflinePaymentUtils.gethttpServletRequest();
            String paymentMode = StringUtils
                    .isNotBlank(request.getParameter(TheiaConstant.ExtraConstants.PAYMENT_MODE)) ? request
                    .getParameter(TheiaConstant.ExtraConstants.PAYMENT_MODE) : request
                    .getParameter(TheiaConstant.EventLogConstants.PAYMENT_MODE);
            pushDataInStatsDMap(statsDInputMap, StatsDConstants.PAYMENT_MODE, paymentMode,
                    ENABLE_PUSH_PAYMENT_MODE_TO_STATSD);
            String requestType = StringUtils.isNotBlank(request
                    .getParameter(TheiaConstant.EventLogConstants.REQUEST_TYPE)) ? request
                    .getParameter(TheiaConstant.EventLogConstants.REQUEST_TYPE) : request
                    .getParameter(TheiaConstant.ExtraConstants.REQUEST_TYPE);
            pushDataInStatsDMap(statsDInputMap, REQUEST_TYPE, requestType, ENABLE_PUSH_REQUEST_TYPE_TO_STATSD);
            pushMidToStatsDMap(request, statsDInputMap);
            statsDInputMap.put(RESULT_STATUS, resultStatus);
            statsDUtils.pushFormPtcSrDetails(statsDInputMap);
        }
    }

    public void pushMidToStatsDMap(HttpServletRequest requestContext, Map<String, String> statsDInputMap) {
        String mid = StringUtils.isNotBlank(requestContext.getParameter(TheiaConstant.RequestParams.MID)) ? requestContext
                .getParameter(TheiaConstant.RequestParams.MID) : requestContext
                .getParameter(TheiaConstant.RequestParams.Native.MID);
        if (ObjectUtils.notEqual(ff4jUtils, null) && StringUtils.isNotBlank(mid)
                && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_PUSH_MID_TO_STATSD, false)) {
            statsDInputMap.put(StatsDConstants.MID, mid);
        }
    }

    public void pushDataInStatsDMap(Map<String, String> statsDInputMap, String key, String value, String propertyName) {
        if (StringUtils.isNotBlank(value)) {
            String propertyValue = ff4jUtils.getPropertyAsStringWithDefault(propertyName, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(propertyValue)) {
                List<String> propertyValueList = new ArrayList<>(Arrays.asList(propertyValue.split(",")));
                if (propertyValueList.contains(value)) {
                    statsDInputMap.put(key, value);
                }
            }
        }
    }

}
