/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.theia.datamapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.enums.DirectPaymentVerificationMethod;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.enums.MerchantInfoRequest;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.checksum.utils.AggregatorMidKeyUtil;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.constant.CommonConstant;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.*;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.model.link.LinkPaymentRiskInfo;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.enums.SplitMethod;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.bankOauth.models.Wallet2FAPasscodeRequest;
import com.paytm.pgplus.facade.bankOauth.models.Wallet2FAPasscodeResponse;
import com.paytm.pgplus.facade.bankOauth.services.IBankOauthService;
import com.paytm.pgplus.facade.coft.model.UserInfo;
import com.paytm.pgplus.facade.coft.model.*;
import com.paytm.pgplus.facade.coft.service.ICoftService;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.enums.AssetTypeEnum;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.merchant.models.MerchantInfo;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.ApplyPromoServiceResponseV2;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.CardInfoResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.model.MerchantLimit;
import com.paytm.pgplus.mappingserviceclient.service.IDynamicCurrencyConverterService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantQueryService;
import com.paytm.pgplus.mappingserviceclient.service.IVendorInfoService;
import com.paytm.pgplus.mappingserviceclient.service.impl.GlobalConfigImpl;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.enums.CoftResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.LuhnAlgoImpl;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.constants.ProcessTransactionConstant;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.datamapper.PayMethodBuilder;
import com.paytm.pgplus.theia.datamapper.dto.EMIPayMethodDTO;
import com.paytm.pgplus.theia.datamapper.helper.BizRequestResponseMapperHelper;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.PaymentDetails;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequestBody;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.DccPaymentHelper;
import com.paytm.pgplus.theia.mapper.TheiaCashierMapper;
import com.paytm.pgplus.theia.models.EmiPayOption;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequest;
import com.paytm.pgplus.theia.models.banktransfer.InternalTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.SeamlessEMIPaymentHelper;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.*;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequestBody;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import com.paytm.pgplus.theia.paymentoffer.requestbuilder.ApplyPromoPaymentOptionBuilderFactory;
import com.paytm.pgplus.theia.paymentoffer.service.IPaymentOffersService;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.session.utils.*;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.EMIPaymentHelper;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.CHECKOUT;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_PROMO;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_TPV_FOR_ALL_REQUEST_TYPES;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.biz.utils.BizConstant.RiskParams.CALLBACK_URL;
import static com.paytm.pgplus.biz.utils.BizConstant.RiskParams.*;
import static com.paytm.pgplus.biz.utils.BizConstant.UPI_POS_ORDER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.BasicPayOption;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams;
import static com.paytm.pgplus.pgproxycommon.enums.CardScheme.AMEX;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.LOGO_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.VALIDATE_EMI_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.EmiSubvention.VALIDATE_EMI_RESPONSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.LinkBasedParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.REFERER;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Service("bizRequestResponseMapper")
public class BizRequestResponseMapperImpl implements IBizRequestResponseMapper {
    private static final Logger LOGGER = LogManager.getLogger(BizRequestResponseMapperImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BizRequestResponseMapperImpl.class);
    private static final Logger EVENT_LOGGER = LogManager.getLogger("EVENT_LOGGER");
    private static final String POS_ID = "posId";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String GV_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.GV_PRODUCT_CODE);
    private static final String NCMC_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.NCMC_PRODUCT_CODE);
    private static final Pattern VALID_CUSTOMER_COMMENT = Pattern.compile("^[a-zA-Z0-9&\\-' ]*$");

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    @Qualifier("loginInfoSessionUtil")
    private LoginInfoSessionUtil loginInfoSessionUtil;

    @Autowired
    @Qualifier("transactionInfoSessionUtil")
    private TransactionInfoSessionUtil transactionInfoSessionUtil;

    @Autowired
    @Qualifier("transactionConfigInfoSessionUtil")
    private TransactionConfigInfoSessionUtil transactionConfigInfoSessionUtil;

    @Autowired
    @Qualifier("merchantInfoSessionUtil")
    private MerchantInfoSessionUtil merchantInfoSessionUtil;

    @Autowired
    @Qualifier("walletInfoSessionUtil")
    private WalletInfoSessionUtil walletInfoSessionUtil;

    @Autowired
    @Qualifier("digitalCreditInfoSessionUtil")
    private DigitalCreditInfoSessionUtil digitalCreditInfoSessionUtil;

    @Autowired
    @Qualifier("oAuthInfoSessionUtil")
    private OAuthInfoSessionUtil oAuthInfoSessionUtil;

    @Autowired
    @Qualifier("entityPaymentOptionSessionUtil")
    private EntityPaymentOptionSessionUtil entityPaymentOptionSessionUtil;

    @Autowired
    @Qualifier("cardInfoSessionUtil")
    private CardInfoSessionUtil cardInfoSessionUtil;

    @Autowired
    @Qualifier("bizRequestResponseMapperHelper")
    BizRequestResponseMapperHelper bizRequestResponseMapperHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantUrlService")
    private IMerchantUrlService merchantUrlService;

    @Autowired
    private TheiaCashierMapper theiaCashierMapper;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("paymentsBankAccountSessionUtil")
    private PaymentsBankAccountSessionUtil paymentsBankAccountSessionUtil;

    @Autowired
    @Qualifier("merchantQueryServiceImpl")
    private IMerchantQueryService merchantQueryService;

    @Autowired
    @Qualifier("walletQRCodeServiceImpl")
    private IWalletQRCodeService walletQRCodeService;

    @Autowired
    @Qualifier("prepaidCardValidationUtil")
    private PrepaidCardValidationUtil prepaidCardValidationUtil;

    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    @Autowired
    @Qualifier("sarvtraVPASessionUtil")
    private SarvatraVPASessionUtil sarvtraVPASessionUtil;

    @Autowired
    @Qualifier("seamlessEMIPaymentHelper")
    private SeamlessEMIPaymentHelper seamlessEMIPaymentHelper;

    @Autowired
    @Qualifier("theiaSessionDataServiceAdapterNative")
    private ITheiaSessionDataService theiaSessionDataServiceAdapterNative;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private PayMethodBuilder<EMIPayMethodDTO> emiPayMethodbuilder;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    @Qualifier("merchantExtendInfoProvider")
    private MerchantExtendInfoProvider merchantExtendInfoProvider;

    @Autowired
    @Qualifier("paymentOffersServiceHelper")
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("vendorInfoService")
    private IVendorInfoService vendorInfoService;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    CardCenterHelper cardCenterHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    @Qualifier("corporateCardUtil")
    CorporateCardUtil corporateCardUtil;

    @Autowired
    private EcomTokenHelper ecomTokenHelper;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    private DccPaymentHelper dccPaymentHelper;

    @Autowired
    private IDynamicCurrencyConverterService dynamicCurrencyConverterService;

    @Autowired
    @Qualifier("dccPageDataHelper")
    private DccPageDataHelper dccPageDataHelper;

    @Autowired
    private DccUtil dccUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    private IPaymentOffersService paymentOffersService;

    @Autowired
    private CardTypeUtil cardTypeUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("CoftService")
    private ICoftService coftService;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginService;

    @Autowired
    @Qualifier("pg2Utilities")
    private PG2Utilities pg2Utilities;

    @Autowired
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    @Qualifier("globalConfigImpl")
    private GlobalConfigImpl globalConfig;

    @Autowired
    private IBankOauthService bankOauthService;

    private static final String USER_LATITUDE = "userLbsLatitude";
    private static final String USER_LONGITUDE = "userLbsLongitude";
    private static final String MODE = "mode";

    @Override
    public WorkFlowRequestBean mapWorkFlowRequestData(final PaymentRequestBean requestData)
            throws TheiaDataMappingException {
        final WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();

        setDealsFlow(flowRequestBean, requestData);

        setCallBackUrlForOffline(requestData);

        setCallBackUrlForJSLinks(requestData);

        flowRequestBean.setPaymentRequestBean(requestData);

        setRiskExtendedInfo(requestData, flowRequestBean);

        setPG2Preferences(flowRequestBean);

        setPromoCoftPreference(flowRequestBean);

        setEnableGcinOnCoftPromo(flowRequestBean);

        setGlobalVaultCoft(flowRequestBean);

        setUltimateBeneficiaryDetails(flowRequestBean, requestData);

        setDisableLimitCCAddNPay(flowRequestBean);

        flowRequestBean.setIs3pAddMoneyEnabled(merchantPreferenceService.is3pAddMoney(requestData.getMid(), false));

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(requestData.getMid());

        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            flowRequestBean.setAlipayMID(merchantMappingResponse.getResponse().getAlipayId());
            requestData.setAlipayMerchantId(merchantMappingResponse.getResponse().getAlipayId());
        } else {
            final String error = merchantMappingResponse == null ? "Could not map merchant" : merchantMappingResponse
                    .getFailureMessage();
            throw new PaymentRequestValidationException(error, ResponseConstants.INVALID_MID);
        }

        setPostPaidOnAddnPayFlag(flowRequestBean, requestData);
        flowRequestBean.setAddnpayPreferenceOnMerchant(checkAddNPayPreferenceOnMerchant(requestData));
        /*
         * if (StringUtils.isNotBlank(requestData.getEncParams()) &&
         * StringUtils.isNotBlank(requestData.getMid()) &&
         * merchantPreferenceService
         * .isEncRequestEnabled(requestData.getMid()))UASettlementNotificationHelper
         * { bizRequestResponseMapperHelper.decryptRequest(requestData); }
         */

        // GV specific
        flowRequestBean.setTargetPhoneNo(requestData.getTargetPhoneNo());

        setSubWalletFlag(flowRequestBean, requestData);

        // Adding securityId for risk verification cases
        flowRequestBean.setSecurityId(requestData.getSecurityId());

        if (requestData.getOneClickInfo() != null) {
            flowRequestBean.setOneClickInfo(requestData.getOneClickInfo());
        }

        if (requestData.isEcomTokenTxn()) {
            flowRequestBean.setEcomTokenTxn(requestData.isEcomTokenTxn());
        }
        // GV Consent flow
        flowRequestBean.setGvConsentFlow(requestData.isGvConsentFlow());

        flowRequestBean.setUseInvestmentAsFundingSource(requestData.getUseInvestmentAsFundingSource());

        ExtendedInfoRequestBean extInforequestBean = buildExtendedInfoRequestBean(requestData, flowRequestBean);

        if (StringUtils.isNotBlank(extInforequestBean.getMerchantCategory())) {
            Map<String, String> riskExtendInfo = new HashMap<>();
            riskExtendInfo.put(MERCHANT_CATEGORY, extInforequestBean.getMerchantCategory());
            if (MapUtils.isNotEmpty(flowRequestBean.getRiskExtendedInfo())) {
                flowRequestBean.getRiskExtendedInfo().putAll(riskExtendInfo);
            } else {
                flowRequestBean.setRiskExtendedInfo(riskExtendInfo);
            }
        }

        // setting Amount fetched from QRInfo in DQR flow
        if (StringUtils.isNotBlank(requestData.getQrTxnAmountInRupees())) {
            flowRequestBean
                    .setQrTxnAmount(AmountUtils.getTransactionAmountInPaise(requestData.getQrTxnAmountInRupees()));
        }
        // Setting splitSettlementInfo in extendInfo
        if (requestData.getSplitSettlementInfoData() != null
                && requestData.getSplitSettlementInfoData().getSplitInfo() != null)
            populateSplitCommandInfoList(requestData, extInforequestBean, flowRequestBean);

        populateEdcEmiDetailsInfo(requestData, flowRequestBean);

        if (!(TheiaConstant.RequestTypes.RENEW_SUBSCRIPTION.equals(requestData.getRequestType()) || RequestTypes.PARTIAL_RENEW_SUBSCRIPTION
                .equals(requestData.getRequestType()))) {
            if (requestData.isPreAuth()) {
                boolean isCallbackSet = setCallbackUrlForPreAuth(requestData, extInforequestBean);
                if (!isCallbackSet) {
                    LOGGER.info("Falling back to Payment Callback Url");
                    setCallbackUrls(requestData, extInforequestBean, flowRequestBean);
                }
            } else {
                setCallbackUrls(requestData, extInforequestBean, flowRequestBean);
            }
        }

        if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid())
                && (StringUtils.isNotEmpty(requestData.getLinkId()) || StringUtils.isNotEmpty(requestData
                        .getInvoiceId()))) {

            if (StringUtils.isNotEmpty(requestData.getInvoiceId())) {
                LOGGER.info("Setting Invoice Id in MerchantUniqueReference for link based Invoice payments");
                extInforequestBean.setMerchantUniqueReference(ExtraConstants.INVOICE_ID_PREFIX
                        + requestData.getInvoiceId());
            } else if (StringUtils.isNotEmpty(requestData.getLinkId())) {
                LOGGER.info("Setting linkId in MerchantUniqueReference for link based payments");
                extInforequestBean.setMerchantUniqueReference(ExtraConstants.LINK_ID_PREFIX + requestData.getLinkId());
            }
            // to support link payment flow from app as offline payments
            if (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                    && !nativePaymentUtil.isNativeEnhanceFlow(requestData)) {
                flowRequestBean.setOfflineFlow(true);
                extInforequestBean.setOfflineFlow(true);
            }

        } else {
            extInforequestBean.setMerchantUniqueReference(requestData.getMerchUniqueReference());
        }

        flowRequestBean.setNativeAddMoney(requestData.isNativeAddMoney());
        EPayMethod payMethod = null;
        if (StringUtils.isNotBlank(requestData.getPaymentTypeId())) {
            payMethod = EPayMethod.getPayMethodByMethod(requestData.getPaymentTypeId());
        }
        if (EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.equals(payMethod)) {
            setExtendInfoForAdvanceDeposit(requestData, extInforequestBean);
            com.paytm.pgplus.facade.payment.models.ChannelAccount channelAccount = nativeSessionUtil
                    .getAdvanceDepositDetails(requestData.getTxnToken());
            if (channelAccount != null && channelAccount.getAccountNo() != null) {
                flowRequestBean.setAdvanceDepositId(channelAccount.getAccountNo());
            }
        }

        flowRequestBean.setExtendInfo(extInforequestBean);

        flowRequestBean.setAdditionalOrderExtendInfo(requestData.getLinkOrderExtendInfo());

        flowRequestBean.setSubWalletOrderAmountDetails(requestData.getSubwalletAmount());

        if (ERequestType.RESELLER.name().equals(requestData.getRequestType())
                && StringUtils.isBlank(requestData.getAccount_ref_id())) {
            throw new TheiaDataMappingException("Required Params are missing for the reseller flow",
                    ResponseConstants.INVALID_REQUEST_TYPE);
        }
        flowRequestBean.setRequestType(ERequestType.getByRequestType(requestData.getRequestType()));
        flowRequestBean.setSubRequestType(ERequestType.getByRequestType(requestData.getSubRequestType()));
        flowRequestBean.setPaytmMID(requestData.getMid());
        flowRequestBean.setOrderID(requestData.getOrderId());
        flowRequestBean.setCustID(requestData.getCustId());
        flowRequestBean.setApiVersion(requestData.getVersion());
        flowRequestBean.setOriginChannel(requestData.getOriginChannel());
        flowRequestBean.setAccessToken(requestData.getAccessToken());

        setAmount(requestData, flowRequestBean);
        setAmountToBeRefundedInExtendInfo(requestData, flowRequestBean);

        flowRequestBean.setCreateOrderRequired(requestData.isCreateOrderRequired());
        flowRequestBean.setCreateTopupRequired(requestData.isCreateTopupRequired());
        flowRequestBean.setIndustryTypeID(requestData.getIndustryTypeId());
        flowRequestBean.setWebsite(requestData.getWebsite());
        flowRequestBean.setAuthMode(requestData.getAuthMode());
        flowRequestBean.setPaymentTypeId(requestData.getPaymentTypeId());
        flowRequestBean.setiDebitEnabled(requestData.getIDebitOption());
        flowRequestBean.setDeviceId(requestData.getDeviceId());
        flowRequestBean.setAccountRefId(requestData.getAccount_ref_id());
        flowRequestBean.setZeroCostEmi(StringUtils.startsWithIgnoreCase(requestData.getEmiOption(),
                TheiaConstant.ExtraConstants.ZERO_COST_EMI));

        if (ERequestType.NATIVE.getType().equalsIgnoreCase(requestData.getRequestType())
                && StringUtils.isBlank(requestData.getCallbackUrl())) {
            flowRequestBean.setCallBackURL(extInforequestBean.getCallBackURL());
        } else {
            flowRequestBean.setCallBackURL(requestData.getCallbackUrl());
        }

        if (TheiaConstant.RequestTypes.ADD_MONEY.equals(requestData.getRequestType())
                || TheiaConstant.RequestTypes.TOPUP_EXPRESS.equals(requestData.getRequestType())
                || TheiaConstant.RequestTypes.ADDMONEY_EXPRESS.equals(requestData.getRequestType())
                || requestData.isNativeAddMoney()) {
            flowRequestBean.setTransType(ETransType.TOP_UP);
        } else {
            flowRequestBean.setTransType(ETransType.ACQUIRING);
        }

        if (!TheiaConstant.RequestTypes.SEAMLESS_3D_FORM.equals(requestData.getRequestType())) {
            flowRequestBean.setClientIP(EnvInfoUtil.getClientIP(requestData.getRequest()));
        }
        flowRequestBean.setoAuthCode(requestData.getAuthCode());
        flowRequestBean.setSavedCardID(requestData.getSavedCardID());
        flowRequestBean.setCardType(requestData.getCardType());
        flowRequestBean.setOrderTimeOutInMilliSecond(requestData.getOrderTimeOutMilliSecond());
        if (PaymentTypeIdEnum.UPI.value.equals(requestData.getPaymentTypeId())) {
            flowRequestBean.setPaymentTimeoutInMinsForUpi(upiInfoSessionUtil.getPaymentTimeoutinMinsForUpi(requestData
                    .getMid()));
            flowRequestBean.setNativeDeepLinkReqd(requestData.isDeepLinkRequired());
            // set os type and pspApp
            flowRequestBean.setOsType(requestData.getOsType());
            flowRequestBean.setPspApp(requestData.getPspApp());
        }
        if (flowRequestBean.isNativeDeepLinkReqd()) {
            // RefUrl And TxnNote to be send to instaproxy for generating
            // deepLink
            // isDeepLinkFromInsta:To identify whether we need deepLink to be
            // made by insta proxy or no
            flowRequestBean.setRefUrl(requestData.getRefUrl());
            flowRequestBean.setTxnNote(requestData.getTxnNote());
            flowRequestBean.setDeepLinkFromInsta(requestData.isDeepLinkFromInsta());
        }

        EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(requestData.getRequest());

        setEnvInfoForLinkPayment(envInfo);

        if (flowRequestBean.getChannelInfo() == null) {
            flowRequestBean.setChannelInfo(new HashMap<>());
        }
        String merchantType = merchantExtendInfoProvider.isMerchantOnPaytm(requestData) ? BizConstant.ExtendedInfoKeys.MerchantTypeValues.ONUS
                : BizConstant.ExtendedInfoKeys.MerchantTypeValues.OFFUS;
        flowRequestBean.getChannelInfo().put(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE, merchantType);

        Map<String, String> riskExtendInfoMap = flowRequestBean.getRiskExtendedInfo();

        riskExtendInfoMap = populateMerchantTypeInRiskExtendInfoMap(riskExtendInfoMap, merchantType);

        if (envInfo.getExtendInfo() != null) {
            envInfo.getExtendInfo().put("deviceId", requestData.getDeviceId());
        }
        EnvInfoUtil.populateDataEnrichmentFields(riskExtendInfoMap, envInfo);

        if (requestData != null && requestData.getExtendInfo() != null
                && requestData.getExtendInfo().getLatitude() != null) {
            envInfo.getExtendInfo().put(LATITUDE, requestData.getExtendInfo().getLatitude());
        }
        if (requestData != null && requestData.getExtendInfo() != null
                && requestData.getExtendInfo().getLongitude() != null) {
            envInfo.getExtendInfo().put(LONGITUDE, requestData.getExtendInfo().getLongitude());
        }
        flowRequestBean.setEnvInfoReqBean(envInfo);
        if (requestData != null && requestData.getExtendInfo() != null
                && requestData.getExtendInfo().getUserLBSLatitude() != null) {
            flowRequestBean.getRiskExtendedInfo().put(USER_LBS_LATITUDE,
                    requestData.getExtendInfo().getUserLBSLatitude());
        }
        if (requestData != null && requestData.getExtendInfo() != null
                && requestData.getExtendInfo().getUserLBSLongitude() != null) {
            flowRequestBean.getRiskExtendedInfo().put(USER_LBS_LONGITUDE,
                    requestData.getExtendInfo().getUserLBSLongitude());
        }

        try {
            flowRequestBean.setGoodsInfo(modifyGoodsInfo(requestData.getGoodsInfo(), flowRequestBean.getPaytmMID()));
        } catch (Exception e) {
            LOGGER.info("Unable to set the price in paisa in goodsInfo");
            flowRequestBean.setGoodsInfo(requestData.getGoodsInfo());
        }
        flowRequestBean.setShippingInfo(requestData.getShippingInfo());
        /** Risk Data */

        if (StringUtils.isBlank(requestData.getChannelId())) {
            flowRequestBean.setChannelID(envInfo.getTerminalType().getTerminal());
        } else {
            flowRequestBean.setChannelID(requestData.getChannelId());
        }

        if (requestData.isSessionRequired()) {
            theiaSessionDataService.setEnvInfoRequestBean(requestData.getRequest(), envInfo);
        }

        // Setting TransID from session if available
        if (requestData.isSessionRequired()) {
            // Set extended info in session
            theiaSessionDataService.setExtendedInfoRequestBean(requestData.getRequest(), extInforequestBean);

            ExtendedInfoRequestBean sessionExtendInfo = theiaSessionDataService.geExtendedInfoRequestBean(requestData
                    .getRequest());

            // Handle callback where extInforequestBean was set in session by
            // earlier api request
            if (sessionExtendInfo != null && StringUtils.isBlank(sessionExtendInfo.getCallBackURL())
                    && StringUtils.isNotBlank(extInforequestBean.getCallBackURL())) {
                LOGGER.info("callback url was not present in session, adding the same {}",
                        extInforequestBean.getCallBackURL());
                sessionExtendInfo.setCallBackURL(extInforequestBean.getCallBackURL());
                theiaSessionDataService.setExtendedInfoRequestBean(requestData.getRequest(), sessionExtendInfo);
            }
            TransactionInfo transInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());

            if ((transInfo != null) && StringUtils.isNotBlank(transInfo.getTxnId())) {
                flowRequestBean.setTransID(transInfo.getTxnId());
            }
        }

        flowRequestBean.setPromoCampId(requestData.getPromoCampId());
        flowRequestBean.setOrderDetails(requestData.getOrderDetails());
        flowRequestBean.setDob(requestData.getDob());
        flowRequestBean.setAddress1(requestData.getAddress1());
        flowRequestBean.setAddress2(requestData.getAddress2());
        flowRequestBean.setCity(requestData.getCity());
        flowRequestBean.setState(requestData.getState());
        flowRequestBean.setPincode(requestData.getPincode());
        flowRequestBean.setHolderMobileNo(requestData.getMobileNo());
        flowRequestBean.setEmailID(requestData.getEmail());
        flowRequestBean.setKycCode(requestData.getKycCode());
        flowRequestBean.setKycVersion(requestData.getKycVersion());
        flowRequestBean.setStoreCardPrefEnabled(merchantPreferenceProvider.isStoreCardEnabledForMerchant(requestData));

        flowRequestBean.setBankCode(requestData.getBankCode());
        flowRequestBean.setStoreCard(requestData.getStoreCard());
        if (StringUtils.isNotBlank(requestData.getSsoToken())) {
            flowRequestBean.setToken(requestData.getSsoToken());
            flowRequestBean.setOauthClientId(configurationDataService
                    .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID));
            flowRequestBean.setOauthSecretKey(configurationDataService
                    .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY));
        }

        if (AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(requestData.getAggMid())
                || AggregatorMidKeyUtil.isMidEnabledForMLVAggregatorMid(requestData.getAggMid())) {
            flowRequestBean.setAggMid(requestData.getAggMid());
        }
        flowRequestBean.setSimplifiedPaymentOffers(requestData.getSimplifiedPaymentOffers());

        // prepare seam less request data
        buildSeamlessRequestData(requestData, flowRequestBean);

        // Prepare Paytm_Expess Data
        buildPaytmExpressRequestData(requestData, flowRequestBean);

        // prepare data for offline payments and native payments
        buildPaytmOfflineRequestData(requestData, flowRequestBean);

        buildEmiSubventionCheckoutReqData(flowRequestBean, requestData.getRequest());

        buildCreditCardBillPaymentRequestData(requestData, flowRequestBean);

        setLastFourDigitsForFreshCardTransactions(requestData, flowRequestBean);
        buildAddMoneyExpressRequestData(requestData, flowRequestBean);
        setDetailsForFreshCardTransaction(requestData, flowRequestBean);
        if (requestData.isCoftTokenTxn()) {
            flowRequestBean.setCoftTokenTxn(requestData.isCoftTokenTxn());
            flowRequestBean.setCardTokenInfo(requestData.getCardTokenInfo());
            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
            checktokenIndexNumberOrCardToken(flowRequestBean, requestData);
        }

        // Risk integration - plain card transaction
        setRiskExtendInfoForForPlainCardTransaction(requestData, flowRequestBean);

        buildCCDCDataForEcomToken(requestData, flowRequestBean);

        // todo coft remove log
        // LOGGER.info("Setting return token value {}",
        // requestData.isReturnToken());
        EXT_LOGGER.customInfo("Setting return token value {}", requestData.isReturnToken());

        flowRequestBean.setReturnToken(requestData.isReturnToken());
        if (StringUtils.isBlank(flowRequestBean.getPaymentDetails())
                || ERequestType.STOCK_TRADE.getType().equals(requestData.getRequestType())
                || ERequestType.SEAMLESS_3D_FORM.getType().equals(requestData.getRequestType())) {
            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
        }

        // Prepare AddMoney_Express data

        buildCCDCDataForCoftToken(requestData, flowRequestBean);
        setPaymentModeFilters(requestData, flowRequestBean);

        if (!requestData.isCreateOrderForInitiateTxnRequest()
                && (requestData.getRequestType()).equals(ERequestType.TOPUP_EXPRESS.getType())
                || requestData.getRequestType().equals(ERequestType.ADDMONEY_EXPRESS.getType())) {

            final TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(requestData.getRequest());
            final EntityPaymentOptionsTO entityPaymentOptions = theiaSessionDataService
                    .getEntityPaymentOptions(requestData.getRequest());

            flowRequestBean.setChannelInfo(theiaCashierMapper.buildChannelInfo(theiaPaymentRequest,
                    entityPaymentOptions));
            flowRequestBean.getChannelInfo().put(TheiaConstant.ChannelInfoKeys.TO_USE_DIRECT_PAYMENT, "true");
            flowRequestBean.getChannelInfo().put(TheiaConstant.ChannelInfoKeys.VERIFICATION_METHOD,
                    DirectPaymentVerificationMethod.OTP.getValue());

            if (PaymentTypeIdEnum.UPI.value.equals(requestData.getPaymentTypeId())) {
                if (!flowRequestBean.getChannelInfo().containsKey(
                        TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.VIRTUAL_PAYMENT_ADDRESS)) {
                    flowRequestBean.getChannelInfo().put(
                            TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.VIRTUAL_PAYMENT_ADDRESS,
                            flowRequestBean.getVirtualPaymentAddress());
                }

            }

            setPassThroughExtendInfo(flowRequestBean);
        }

        // Subscription specific
        if (requestData.getType() != null) {
            flowRequestBean.setType(requestData.getType());
        }
        buildSubscriptionRequestData(requestData, flowRequestBean);
        flowRequestBean.setDebitDate(requestData.getDebitDate());

        if (flowRequestBean.isDealsFlow()) {
            if (flowRequestBean.getPaytmExpressAddOrHybrid() != EPayMode.ADDANDPAY) {
                flowRequestBean.getExtendInfo().setAcquiringType(
                        TheiaConstant.MerchantPreference.PreferenceKeys.OFFLINE_DEALS);
            }
            flowRequestBean.getExtendInfo().setFlowRequestType(
                    TheiaConstant.MerchantPreference.PreferenceKeys.OFFLINE_DEALS);
        }

        // Litepayview param
        flowRequestBean.setCustomizeCode(requestData.getCustomizeCode());
        if (flowRequestBean.isUpiPushFlow()) {
            flowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH);
        }

        flowRequestBean.setCardTokenRequired(requestData.getCardTokenRequired());
        flowRequestBean.setQREnabled(requestData.isQREnabled());
        if (aoaUtils.isAOAMerchant(requestData)) {
            flowRequestBean.setFromAoaMerchant(true);
            if (!requestData.isSuperGwFpoApiHit()) {
                if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestData.getRequestType())
                        && !SubsPaymentMode.BANK_MANDATE.getePayMethodName().equals(requestData.getSubsPaymentMode())) {
                    flowRequestBean.setRequestType(ERequestType.NATIVE_SUBSCRIPTION);
                } else {
                    flowRequestBean.setRequestType(ERequestType.UNI_PAY);
                }
            }
        }

        flowRequestBean.setCartValidationRequired(requestData.isCartValidationRequired());

        // Set params for account-verification
        if (!(requestData.isCreateOrderForInitiateTxnRequest())
                && ERequestType.NATIVE_MF.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_ST.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_ST.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_ST_PAY.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_ST_PAY.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF_PAY.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF_PAY.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_MF_SIP.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF_SIP.getType().equals(requestData.getSubRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.getType().equals(requestData.getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.getType().equals(requestData.getSubRequestType())
                || (merchantPreferenceService.isEligibleForMultipleMBIDFlow(requestData.getMid()) && ff4JUtil
                        .isFeatureEnabled(ENABLE_TPV_FOR_ALL_REQUEST_TYPES, requestData.getMid()))) {
            flowRequestBean.setAggMid(requestData.getAggMid());
            flowRequestBean.setValidateAccountNumber(requestData.getValidateAccountNumber());
            flowRequestBean.setAllowUnverifiedAccount(requestData.getAllowUnverifiedAccount());
            flowRequestBean.setAdditionalInfoMF(requestData.getAdditionalInfoMF());
            flowRequestBean.setAccountNumber(requestData.getAccountNumber());
        }

        flowRequestBean.setMerchantEligibleForMultipleMBIDFlow(merchantPreferenceService
                .isEligibleForMultipleMBIDFlow(flowRequestBean.getPaytmMID()));

        flowRequestBean.setInstantSettlementEnabled(merchantPreferenceService
                .isInstantSettlementEnabled(flowRequestBean.getPaytmMID()));
        flowRequestBean.setUpiDirectSettlementEnabled(merchantPreferenceService
                .isUpiDirectSettlementEnabled(flowRequestBean.getPaytmMID()));

        // To identify create order in enhanced
        flowRequestBean.setEnhancedCashierPageRequest(requestData.isEnhancedCashierPageRequest());
        // To identify Payment request in enhanced
        flowRequestBean.setEnhancedCashierPaymentRequest(requestData.isEnhancedCashierPaymentRequest());

        if (requestData.isEnhancedCashierPageRequest()) {
            flowRequestBean.setTxnToken(requestData.getTxnToken());
        }
        flowRequestBean.setPostpaidOnboardingSupported(requestData.isPostpaidOnboardingSupported());

        // for Native+
        setDataForNativeJsonRequest(flowRequestBean, requestData);
        setLinkPaymentRiskExtendedInfo(requestData, flowRequestBean);
        // for HDFCDigiPOS Integration
        setLinkDetailsInChannelInfo(requestData, flowRequestBean);
        setCardHash(flowRequestBean, requestData);

        flowRequestBean.setTemplateId(requestData.getTemplateId());
        flowRequestBean.setbId(requestData.getbId());
        flowRequestBean.setCorporateCustId(requestData.getCorporateCustId());
        flowRequestBean.setDynamicQrRequired(requestData.isDynamicQrRequired());
        /*
         * setting amount for wallet consult to get the trust factor for risk
         * api call
         */
        if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid())
                && StringUtils.isNotBlank(requestData.getAmountForWalletConsultInRisk())) {
            flowRequestBean.setAmountForWalletConsultInRisk(AmountUtils.getTransactionAmountInPaise(requestData
                    .getAmountForWalletConsultInRisk()));
        }

        flowRequestBean.setAmountForPaymentFlow(AmountUtils.getTransactionAmountInPaise(requestData
                .getAmountForPaymentFlow()));

        LOGGER.debug("BizWorkFlow request bean : {}", flowRequestBean);
        LOGGER.debug("BizWorkFlow bean successfully created !!");

        EVENT_LOGGER.info("RequestType : {}, PayMethod : {}, BankCode : {}",
                flowRequestBean.isOfflineFlow() ? RequestTypes.OFFLINE : flowRequestBean.getRequestType(),
                flowRequestBean.getPayMethod(), flowRequestBean.getBankCode());

        flowRequestBean.setMerchantRequestedChannelId(requestData.getMerchantRequestedChannelId());

        flowRequestBean.setWorkFlow(requestData.getWorkflow());

        /*
         * set flag for app-invoke payments from checkout-js, to be used by
         * notification-service for pushing data to kafka for UI team
         */
        if (!StringUtils.equals(CHECKOUT, flowRequestBean.getWorkFlow())) {
            if (StringUtils.isNotBlank(flowRequestBean.getTxnToken())
                    && StringUtils.equals(CHECKOUT, nativeSessionUtil.getWorkflow(flowRequestBean.getTxnToken()))) {
                flowRequestBean.setCheckoutJsAppInvokePayment(true);
            }
        }

        flowRequestBean.setGuestToken(requestData.getGuestToken());

        if (requestData.isEmiSubventionRequired()) {
            flowRequestBean.setItems(requestData.getItems());
            flowRequestBean.setEmiSubventedTransactionAmount(requestData.getEmiSubventedTransactionAmount());
            flowRequestBean.setEmiSubventionCustomerId(requestData.getEmiSubventionCustomerId());
            flowRequestBean.setEmiSubventionRequired(requestData.isEmiSubventionRequired());
            flowRequestBean.setEmiSubventionStratergy(requestData.getEmiSubventionStratergy());
        }

        flowRequestBean.setInternalFetchPaymentOptions(requestData.isInternalFetchPaymentOptions());
        flowRequestBean.setExternalFetchPaymentOptions(requestData.isExternalFetchPaymentOptions());

        setPromoAmount(flowRequestBean, requestData);
        flowRequestBean.setAggType(requestData.getAggType());
        flowRequestBean.setOrderPricingInfo(requestData.getOrderPricingInfo());

        flowRequestBean.setOfflineTxnFlow(requestData.isOfflineTxnFlow());

        /*
         * set pwp category in workflowrequestBean here from paymentBean
         */
        if (StringUtils.isNotBlank(requestData.getPwpCategory())) {
            flowRequestBean.setPwpCategory(requestData.getPwpCategory());
        }

        flowRequestBean.setProductCode(requestData.getProductCode());

        // setting riskExtendInfo for khatabook
        setRiskExtendedInfoNative(flowRequestBean, requestData);

        enrichForFetchChannelDetailsTask(flowRequestBean, requestData);

        // if payment is for savedCardTransaction with cardIndexNumber

        if (flowRequestBean.getIsSavedCard() && StringUtils.isNotEmpty(flowRequestBean.getSavedCardID())
                && flowRequestBean.getSavedCardID().length() > 15 && !flowRequestBean.isCoftTokenTxn()
                && !RequestTypes.ADDMONEY_EXPRESS.equals(requestData.getRequestType())) {

            flowRequestBean.setTxnFromCardIndexNo(true);
            if (RequestTypes.OFFLINE.equals(requestData.getRequestType())) {
                fetchSavedCardFromCachedLitepayviewResponse(flowRequestBean, requestData);
                return flowRequestBean;
            }

            if (StringUtils.isEmpty(flowRequestBean.getTxnToken())) {
                flowRequestBean.setTxnToken(requestData.getTxnToken());
            }

            NativeCashierInfoResponse cashierInfoResponse = populateCashierInfoResponseFromCache(flowRequestBean,
                    requestData, flowRequestBean.getSavedCardID());
            SavedCard saveCard = null;
            if (cashierInfoResponse != null) {
                saveCard = getSavedCardDetails(cashierInfoResponse, flowRequestBean.getSavedCardID());
                // setting expire date as after removing savedCard service,we
                // will not get saveCard Details from savedcard service;
                if (saveCard != null) {
                    buildBinDetailsForPlatformSavedAssets(saveCard, flowRequestBean);
                    flowRequestBean.setCardIndexNo(flowRequestBean.getSavedCardID());
                    flowRequestBean.setExpiryYear(Short.parseShort(saveCard.getCardDetails().getExpiryDate()
                            .substring(2, 6)));
                    flowRequestBean.setExpiryMonth(Short.parseShort(saveCard.getCardDetails().getExpiryDate()
                            .substring(0, 2)));
                    flowRequestBean.setInstId(saveCard.getIssuingBank());
                    flowRequestBean.setBankName(saveCard.getIssuingBank());
                    flowRequestBean.setCardType(saveCard.getCardDetails().getCardType());
                    flowRequestBean.setCorporateCard(flowRequestBean.getBinDetail().isCorporateCard());
                    flowRequestBean.setPrepaidCard(flowRequestBean.getBinDetail().isPrepaidCard());
                    flowRequestBean.setCardActive(flowRequestBean.getBinDetail().isActive());

                    /**
                     * Needed earlier for InternationalCardPaymentFlag in
                     * COP/PAY in workflowHelper
                     */

                    flowRequestBean.setCardNo(saveCard.getCardDetails().getFirstSixDigit());
                    flowRequestBean.setCardScheme(saveCard.getInstId());
                    if (!(flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
                        flowRequestBean.setPayMethod(flowRequestBean.getCardType());
                        flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_"
                                + flowRequestBean.getCardScheme());
                    }

                    // Risk integration - coft for platform saved card
                    // transaction
                    String par = null;

                    if (enableGcinOnCoftForRisk(flowRequestBean)) {

                        String mid = (StringUtils.isNotBlank(requestData.getSgwReferenceId()) && requestData
                                .isSubscription()) ? aoaUtils.getAOAMidForPGMid(flowRequestBean.getPaytmMID())
                                : flowRequestBean.getPaytmMID();
                        FetchPanUniqueReferenceResponse tokenData = coftTokenDataService.getTokenData(mid, "CIN",
                                flowRequestBean.getSavedCardID(), true);
                        par = (Objects.nonNull(tokenData) && Objects.nonNull(tokenData.getBody())) ? tokenData
                                .getBody().getPanUniqueReference() : null;
                        flowRequestBean.setPar(par);
                    }

                    setRiskExtendInfoForCoft(flowRequestBean, flowRequestBean.getSavedCardID(), par, saveCard
                            .getCardDetails().getFirstSixDigit(), null);
                    flowRequestBean.setLastFourDigits(saveCard.getCardDetails().getLastFourDigit());
                }
            }
        }

        populateCoBrandedCustomDisplayName(requestData, flowRequestBean);
        generateDataForSimplifiedEmi(requestData, flowRequestBean);

        // EMI Subvention All-in-one SDK Flow, emiSubventionInfo received in
        // v1/ptc req
        flowRequestBean.setEmiSubventionInfo(requestData.getEmiSubventionInfo());

        buildDccData(requestData, flowRequestBean);

        PaymentOffer paymentOffer = getPaymentOffersForSimplifiedOffers(flowRequestBean, requestData.getTxnAmount(),
                requestData.getTxnToken());
        if (paymentOffer == null) {
            paymentOffer = requestData.getPaymentOffer();
        }
        buildPaymentOfferCheckoutReqData(flowRequestBean, paymentOffer);

        flowRequestBean.setPaymentOffersAppliedV2(requestData.getPaymentOfferV2());

        flowRequestBean.setQueryParams(requestData.getQueryParams());

        flowRequestBean.setLoyaltyPointRootUserId(requestData.getLoyaltyPointRootUserId());

        if (PaymentTypeIdEnum.LOYALTY_POINT.getValue().equalsIgnoreCase(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(EPayMethod.LOYALTY_POINT.getMethod());
            flowRequestBean.setPayOption(EPayMethod.LOYALTY_POINT.getMethod());
        }

        // block token txn transaction for addNPay and emiSubvention,emi txn,
        // emiSubvention,
        ecomTokenHelper.validateAndCheckIfEcomTokenTransactionAllowed(flowRequestBean);

        if (!aoaUtils.isAOAMerchant(requestData)
                && StringUtils.isNotBlank(flowRequestBean.getPaymentTypeId())
                && (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                        || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value) || flowRequestBean
                        .getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
            BinDetail binDetail = processTransactionUtil.getBinDetail(flowRequestBean);
            // TODO: CHECK HERE for aoa emi
            // Creating FeeRateFactors for Fee Consult and COP/Pay API
            if (binDetail != null) {
                cardTypeUtil.validateBlockedBinPayment(binDetail, requestData);
                FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
                if (feeRateFactors == null) {
                    feeRateFactors = new FeeRateFactors();
                    flowRequestBean.setFeeRateFactors(feeRateFactors);
                }
                feeRateFactors.setPrepaidCard(binDetail.isPrepaidCard());
                feeRateFactors.setInternationalCardPayment(!BooleanUtils.isTrue(binDetail.getIsIndian()));
                feeRateFactors.setOneClickSupported(StringUtils.isNotBlank(flowRequestBean.getOneClickInfo()));
                feeRateFactors.setCorporateCard(binDetail.isCorporateCard());
                if (StringUtils.isEmpty(flowRequestBean.getPayMethod()) && flowRequestBean.getIsSavedCard()) {
                    flowRequestBean.setSavedCardPayMethod(getSavedCardPayMethod(binDetail, flowRequestBean));
                }
            }
            // Validation for prepaid card
            prepaidCardValidationUtil.validateAndCheckIfPrepaidCard(requestData, flowRequestBean, binDetail);
            // validation for corporate cardSubType
            corporateCardUtil.validateAndCheckCardSubType(requestData, flowRequestBean, binDetail);
        }

        if (ff4JUtil.isFeatureEnabled(UPI_CC_FEE_FACTOR_ENABLED, flowRequestBean.getPaytmMID())) {
            setFeeRateFactorForCCOnUPI(flowRequestBean, requestData);
        }

        if (merchantPreferenceService.isDynamicFeeMerchant(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setDynamicFeeMerchant(true);
        }

        if (merchantPreferenceService.isEnhanceQrCodeDisabled(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setEnhanceQrCodeDisabled(true);
        }

        flowRequestBean.setSubsOriginalAmount(requestData.getTxnAmount());

        flowRequestBean.setOnusRentPaymentMerchant(merchantPreferenceService.isOnusRentPaymentMerchant(flowRequestBean
                .getPaytmMID()));
        flowRequestBean.setRiskVerifyTxnToken(requestData.getTxnToken()); /*
                                                                           * as
                                                                           * of
                                                                           * now
                                                                           * it
                                                                           * will
                                                                           * be
                                                                           * used
                                                                           * in
                                                                           * do
                                                                           * verify
                                                                           * cache
                                                                           * response
                                                                           */

        validateTransactionAmountForOfflineFlow(flowRequestBean);
        flowRequestBean.setReturnDisabledChannelInFpo(requestData.isReturnDisabledChannelInFpo());
        flowRequestBean.setRiskVerifyTxnToken(requestData.getTxnToken()); /*
                                                                           * as
                                                                           * of
                                                                           * now
                                                                           * it
                                                                           * will
                                                                           * be
                                                                           * used
                                                                           * in
                                                                           * do
                                                                           * verify
                                                                           * cache
                                                                           * response
                                                                           */
        flowRequestBean.setPreAuthExpiryDate(requestData.getPreAuthExpiryDate());
        flowRequestBean.setPreAuth(requestData.isPreAuth());
        flowRequestBean.setAddAndPayWithUPICollectSupported(merchantPreferenceService
                .isAddNPayWithUPICollectSupported(requestData.getMid()));
        if (merchantPreferenceService.isCostBasedPreferenceEnabled(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setCostBasedPreferenceEnabled(true);
        }
        populateBankTransferDetails(flowRequestBean, requestData);

        setUrlsInRiskExtendInfo(flowRequestBean, requestData);
        setWalletPostPaidAuthorizationMode(flowRequestBean, requestData);
        populateOnlineOfflineMerchantTypeForDataEnrichment(requestData, flowRequestBean);
        populateRequestTypeInRiskExtendedInfoForDataEnrichment(flowRequestBean);
        flowRequestBean.setPaymentResumed(requestData.isPaymentResumed());

        String preAuthType = requestData.getCardPreAuthType();
        if (requestData.isPreAuth() && StringUtils.isNotBlank(preAuthType)) {
            if (!EnumUtils.isValidEnum(EPreAuthType.class, preAuthType)) {
                LOGGER.error("Invalid preAuthType : {}", preAuthType);
                throw new PaymentRequestValidationException("Invalid preAuthType", ResponseConstants.INVALID_PARAM);
            }
            flowRequestBean.setCardPreAuthType(EPreAuthType.valueOf(preAuthType));
            flowRequestBean.setPreAuthBlockSeconds(requestData.getPreAuthBlockSeconds());
        }

        populateOfflineTxnIdentifier(requestData, flowRequestBean);

        setPosOrderData(flowRequestBean, requestData);
        setFeeRateFactorForAPIFlow(flowRequestBean, requestData);
        flowRequestBean.setVanInfo(requestData.getVanInfo());
        flowRequestBean.setTpvInfos(requestData.getTpvInfos());
        flowRequestBean.setFeesAmount(requestData.getFeesAmount());
        flowRequestBean.setPreferredOtpPage(requestData.getPreferredOtpPage());
        flowRequestBean.getExtendInfo().setPaymodeIdentifier(getPaymodeIdentifier(flowRequestBean));
        flowRequestBean.setFromAOARequest(requestData.getFromAOARequest());
        if (StringUtils.equals(BizConstant.CHECKOUT, requestData.getWorkflow())
                && !ff4JUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(),
                        THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR, false)) {
            flowRequestBean.setPushDataToDynamicQR(true);
        }
        flowRequestBean.setTipAmount(requestData.getTipAmount());
        flowRequestBean.setUpiConvertedToAddNPay(requestData.isUpiConvertedToAddNPay());
        if (requestData.getCoftConsent() != null) {
            flowRequestBean.setCoftConsent(requestData.getCoftConsent());
        }
        if (merchantPreferenceProvider.isBinEligibleCoft(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setBinEligibleForCoft(true);
        }
        flowRequestBean.setOverrideAddNPayBehaviour(merchantPreferenceService
                .isOverrideAddNPayBehaviourEnabled(flowRequestBean.getPaytmMID()));
        flowRequestBean.setPostpaidEnabledOnMerchant(merchantPreferenceService.isPostpaidEnabledOnMerchant(
                flowRequestBean.getPaytmMID(), false));
        flowRequestBean.setEdcLinkTxn(requestData.getEdcLinkTxn());
        flowRequestBean.setAoaSubsOnPgMid(requestData.isAoaSubsOnPgMid());
        flowRequestBean.setSubsAoaPgMidTxn(requestData.isSubsAoaPgMidTxn());
        if (requestData.getTxnToken() != null) {
            String transId = nativeSessionUtil.getTxnId(requestData.getTxnToken());
            if (transId != null) {
                flowRequestBean.setTransID(transId);
            }
        }
        flowRequestBean.setPpblAccountType(requestData.getPpblAccountType());
        flowRequestBean.setCreateNonQRDeepLink(Boolean.parseBoolean(requestData.getCreateNonQRDeepLink()));
        if (requestData.getTwoFADetails() != null) {
            flowRequestBean.setTwoFADetails(requestData.getTwoFADetails());
        } else if (ONUS.equalsIgnoreCase(flowRequestBean.getChannelInfo().get(
                BizConstant.ExtendedInfoKeys.MERCHANT_TYPE))
                || (workFlowHelper.isWebTxn(flowRequestBean) && !(TheiaConstant.RequestTypes.SUBSCRIPTION
                        .equalsIgnoreCase(requestData.getRequestType())
                        || RequestTypes.PARTIAL_RENEW_SUBSCRIPTION.equals(requestData.getRequestType()) || ERequestType
                            .isSubscriptionRequest(requestData.getRequestType())))) {
            // For ONUS Merchants, cart calls FPO where it is not sending
            // twoFADetails object currently
            // so adding this object at theia end for ONUS merchants, remove it
            // later when handling is added at card (and verticals)
            TwoFADetails twoFADetails = new TwoFADetails();
            twoFADetails.setTxnType(P2M);
            twoFADetails.setTwoFARequired(true);
            flowRequestBean.setTwoFADetails(twoFADetails);
        }

        flowRequestBean.setTwoFAConfig(requestData.getTwoFAConfig());
        boolean isAddNPayOrHybrid = "1".equals(flowRequestBean.getPaymentRequestBean().getIsAddMoney())
                || "0".equals(flowRequestBean.getPaymentRequestBean().getIsAddMoney());
        if (!workFlowHelper.isWebTxn(flowRequestBean)
                && (EPayMethod.BALANCE.getMethod().equals(flowRequestBean.getPayMethod()) || isAddNPayOrHybrid)) {
            setAdditionalParamsInTwoFAConfigData(flowRequestBean);
        }
        if (workFlowHelper.isWebTxn(flowRequestBean) && flowRequestBean.getTwoFAConfig() != null
                && !flowRequestBean.getTwoFAConfig().isFastForwardRequest()) {
            if (StringUtils.isNotBlank(flowRequestBean.getTwoFAConfig().getTwoFAPasscode())) {
                verifyWalletTwoFAPasscode(flowRequestBean);
            } else {
                throw new PaymentRequestValidationException(ResponseConstants.INVALID_WALLET_2FA_PASSCODE.getMessage(),
                        ResponseConstants.INVALID_WALLET_2FA_PASSCODE);
            }
        }
        // Calling setBankFormOptimizationFlow inside orderUPIPSPController, to
        // avoid multiple calls added this check
        if (!requestData.isOrderPSPRequest()) {
            processTransactionUtil.setBankFormOptimizationFlow(flowRequestBean, requestData);
        }

        if (bizRequestResponseMapperHelper.validateAndCheckCCOnUpi(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setCcOnUPIEnabled(true);
        }
        flowRequestBean.setUpiLite(requestData.isUpiLite());
        // TODO UPI-LITE v1/PTC
        flowRequestBean.setUpiLiteRequestData(requestData.getUpiLiteRequestData());
        if (ff4JUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(), ENABLE_ADDANDPAY_ONE_RUPEE, false)) {
            flowRequestBean.setAddOneRupee(requestData.getAddOneRupee());
        } else {
            flowRequestBean.setAddOneRupee(Boolean.FALSE);
        }
        flowRequestBean.setSuperGwFpoApiHit(requestData.isSuperGwFpoApiHit());

        if (StringUtils.equals(BasicPayOption.UPI, flowRequestBean.getPayMethod()) && requestData.isDeepLinkRequired()) {
            setUPIIntentRetriesAllowed(flowRequestBean);
        }
        flowRequestBean.setGenerateEsnRequest(requestData.isGenerateEsnRequest());

        if (null != requestData.getSplitSettlementInfo() && null != requestData.getSplitSettlementInfo().getSplitType()) {
            flowRequestBean.setPostTxnSplitTimeout(StringUtils.isNotBlank(requestData.getSplitSettlementInfo()
                    .getPostTxnSplitTimeout()) ? requestData.getSplitSettlementInfo().getPostTxnSplitTimeout() : "0");
            flowRequestBean.setSplitType(requestData.getSplitSettlementInfo().getSplitType());
        }

        return flowRequestBean;
    }

    private void verifyWalletTwoFAPasscode(WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException,
            PaymentRequestValidationException {
        String paytmUserId = getUserId(flowRequestBean);
        Wallet2FAPasscodeRequest wallet2FAPasscodeRequest = getWallet2FAWebPasscodeRequest(flowRequestBean);
        if (wallet2FAPasscodeRequest != null && StringUtils.isNotBlank(paytmUserId)) {
            Wallet2FAPasscodeResponse wallet2FAPasscodeResponse = bankOauthService.fetchWallet2FAWebPasscodeToken(
                    wallet2FAPasscodeRequest, paytmUserId);
            setWallet2FAWebPasscodeResponse(flowRequestBean, wallet2FAPasscodeResponse);
        } else {
            throw new TheiaDataMappingException(ResponseConstants.SYSTEM_ERROR);
        }
    }

    private String getUserId(WorkFlowRequestBean flowRequestBean) {
        try {
            if (flowRequestBean.getUserDetailsBiz() != null
                    && StringUtils.isNotBlank(flowRequestBean.getUserDetailsBiz().getUserId())) {
                EXT_LOGGER.customInfo("Fetching userId from flowRequestBean");
                return flowRequestBean.getUserDetailsBiz().getUserId();
            }
            if (StringUtils.isNotBlank(flowRequestBean.getTxnToken())) {
                UserDetailsBiz userDetailsBiz = null;
                userDetailsBiz = nativeSessionUtil.getUserDetails(flowRequestBean.getTxnToken());
                if (userDetailsBiz != null) {
                    return userDetailsBiz.getUserId();
                } else {
                    LOGGER.info("Fetching userId from login service");
                    GenericCoreResponseBean<UserDetailsBiz> userDetails = null;
                    userDetails = loginService.fetchUserDetails(flowRequestBean.getToken(), false, flowRequestBean);
                    if (userDetails != null) {
                        return userDetails.getResponse().getUserId();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while fetching UserId");
        }
        return flowRequestBean.getCustID();
    }

    private void setWallet2FAWebPasscodeResponse(WorkFlowRequestBean flowRequestBean,
            Wallet2FAPasscodeResponse wallet2FAPasscodeResponse) throws TheiaDataMappingException,
            PaymentRequestValidationException {
        if (wallet2FAPasscodeResponse != null && SUCCESS.equalsIgnoreCase(wallet2FAPasscodeResponse.getStatus())
                && wallet2FAPasscodeResponse.getData() != null
                && StringUtils.isNotBlank(wallet2FAPasscodeResponse.getData().getAccessToken())) {
            flowRequestBean.getTwoFAConfig().setPassCodeToken(wallet2FAPasscodeResponse.getData().getAccessToken());
        } else if (wallet2FAPasscodeResponse != null
                && ((FAILURE.equalsIgnoreCase(wallet2FAPasscodeResponse.getStatus()) && "1905"
                        .equals(wallet2FAPasscodeResponse.getResponseCode())) || (SUCCESS
                        .equalsIgnoreCase(wallet2FAPasscodeResponse.getStatus()) && "1103"
                        .equals(wallet2FAPasscodeResponse.getResponseCode())))) {
            throw new PaymentRequestValidationException(ResponseConstants.INVALID_WALLET_2FA_PASSCODE.getMessage(),
                    ResponseConstants.INVALID_WALLET_2FA_PASSCODE);
        } else {
            throw new TheiaDataMappingException(ResponseConstants.SYSTEM_ERROR);
        }
    }

    private Wallet2FAPasscodeRequest getWallet2FAWebPasscodeRequest(WorkFlowRequestBean flowRequestBean) {
        try {
            return new Wallet2FAPasscodeRequest(EncryptedParamsRequestServiceHelper.encrypt(flowRequestBean
                    .getTwoFAConfig().getTwoFAPasscode()), PASSCODE, WALLET_TXN, Boolean.TRUE);
        } catch (Exception e) {
            LOGGER.error("Failed to ecrypt wallet 2FA web passcode : ", e);
        }
        return null;
    }

    private void setDetailsForFreshCardTransaction(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        String paymentDetails = requestData.getPaymentDetails();
        if (StringUtils.isNotEmpty(paymentDetails)) {
            String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
            if (actualPaymentDetailsArray.length == 3) {
                String cardNo = actualPaymentDetailsArray[0];
                setIrctcId(flowRequestBean, cardNo);
            }
        }
    }

    private void setIrctcId(WorkFlowRequestBean flowRequestBean, String cardBin) {
        boolean enableBinIdentifierInResponse = merchantPreferenceService.enableBinIdentifierInResponse(flowRequestBean
                .getPaymentRequestBean().getMid());
        if (enableBinIdentifierInResponse && StringUtils.isNotBlank(cardBin)) {
            try {
                String irctcId = globalConfig.getIrctcIdFromBinNumber(cardBin.substring(0, 6));
                EXT_LOGGER.customInfo("Mapping response - IrctcId :: {} for Bin Number :: {}", irctcId,
                        cardBin.substring(0, 6));
                flowRequestBean.getExtendInfo().setBinIrcId(irctcId);
            } catch (Exception ex) {
                LOGGER.error("Failed to get irctc id from bin: ", ex);
            }
        }
    }

    private void setAdditionalParamsInTwoFAConfigData(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getTwoFAConfig() == null) {
            TwoFAConfig twoFAConfig = new TwoFAConfig();
            flowRequestBean.setTwoFAConfig(twoFAConfig);
        }
        Map<String, String> queryParams = getQueryParams(flowRequestBean.getQueryParams());
        if (MapUtils.isNotEmpty(queryParams)) {
            flowRequestBean.getTwoFAConfig().setPlatform(queryParams.get(FacadeConstants.CLIENT_PARAM));
            flowRequestBean.getTwoFAConfig().setAppVersion(queryParams.get(FacadeConstants.CoftAPIConstant.VERSION));
        }
        if (ONUS.equalsIgnoreCase(flowRequestBean.getChannelInfo().get(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE))) {
            flowRequestBean.getTwoFAConfig().setP2mFlow(ONUS_SMALL);
        } else if (flowRequestBean.isOfflineFlow()) {
            flowRequestBean.getTwoFAConfig().setP2mFlow(OFFLINE_SMALL);
        }

    }

    private Map<String, String> getQueryParams(String queryParamString) {
        Map<String, String> queryParams = null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(queryParamString)) {
            queryParams = new HashMap<>();
            String[] params = queryParamString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                queryParams.put(keyValue[0].trim(), keyValue.length > 1 ? keyValue[1].trim()
                        : com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.EMPTY_STRING);
            }
        }
        return queryParams;
    }

    private void setDealsFlow(WorkFlowRequestBean flowRequestBean, PaymentRequestBean paymentRequestBean) {
        if (paymentRequestBean.isDealsFlow())
            flowRequestBean.setDealsFlow(paymentRequestBean.isDealsFlow());
        else {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(paymentRequestBean
                    .getTxnToken());
            if (Objects.nonNull(nativeInitiateRequest) && Objects.nonNull(nativeInitiateRequest.getInitiateTxnReq())
                    && Objects.nonNull(nativeInitiateRequest.getInitiateTxnReq().getBody())) {
                boolean dealsFlow = paymentOffersServiceHelperV2.isDealsFlow(nativeInitiateRequest.getInitiateTxnReq()
                        .getBody().getAffordabilityInfo());
                flowRequestBean.setDealsFlow(dealsFlow);
                flowRequestBean.setInitiateTransactionRequest(nativeInitiateRequest.getInitiateTxnReq());
            }
        }
    }

    private void setPassThroughExtendInfo(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.isCoftTokenTxn()) {
            Map<String, String> passThroughChannelExtendedInfo = new HashMap<>();
            if (RequestParams.DINERS.equals(flowRequestBean.getCardScheme())) {
                passThroughChannelExtendedInfo.put(RequestParams.TOKEN_UNIQUE_REFERENCE, flowRequestBean
                        .getCardTokenInfo().getTokenUniqueReference());
                passThroughChannelExtendedInfo.put(RequestParams.TOKEN_REQUESTOR_ID, flowRequestBean.getCardTokenInfo()
                        .getMerchantTokenRequestorId());
            } else {
                passThroughChannelExtendedInfo.put(RequestParams.TAVV, flowRequestBean.getCardTokenInfo().getTAVV());
            }

            String channelPassThroughJson = StringUtils.EMPTY;
            try {
                channelPassThroughJson = JsonMapper.mapObjectToJson(passThroughChannelExtendedInfo);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while transforming passThroughChannelInfoMap: ", e);
            }
            String encodedChannelPassThrough = new String(Base64.getEncoder().encode(channelPassThroughJson.getBytes()));
            flowRequestBean.getChannelInfo().put(BizConstant.ExtendedInfoKeys.CHANNEL_INFO_PASS_THROUGH_KEY,
                    encodedChannelPassThrough);
        }
    }

    private void setLastFourDigitsForFreshCardTransactions(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) {
        String paymentDetails = requestData.getPaymentDetails();
        if (StringUtils.isNotEmpty(paymentDetails)) {
            String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
            if (actualPaymentDetailsArray.length == 3) {
                String cardNo = actualPaymentDetailsArray[0];
                flowRequestBean.setLastFourDigits(cardNo.substring(cardNo.length() - 4));
            }
        }
    }

    private String getPaymodeIdentifier(WorkFlowRequestBean flowRequestBean) {
        String paymodeIdentifier = null;
        if (EPayMethod.WALLET.getMethod().equals(flowRequestBean.getPayMethod())) {
            paymodeIdentifier = "34";
        } else if (EPayMethod.NET_BANKING.getMethod().equals(flowRequestBean.getPayMethod())) {
            paymodeIdentifier = "01";
        } else if (EPayMethod.UPI.getMethod().equals(flowRequestBean.getPayMethod())) {
            paymodeIdentifier = "33";
        } else if (EPayMethod.CREDIT_CARD.getMethod().equals(flowRequestBean.getPayMethod())) {
            if (CardScheme.VISA.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "12";
            } else if (CardScheme.MASTER.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "13";
            } else if (AMEX.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "11";
            } else if (CardScheme.RUPAY.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "15";
            } else if (CardScheme.DINERS.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "14";
            }
        } else if (EPayMethod.DEBIT_CARD.getMethod().equals(flowRequestBean.getPayMethod())) {
            if (CardScheme.VISA.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "21";
            } else if (CardScheme.MASTER.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "22";
            } else if (CardScheme.RUPAY.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "25";
            } else if (CardScheme.MAESTRO.getScheme().equals(flowRequestBean.getCardScheme())) {
                paymodeIdentifier = "23";
            }
        } else {
            paymodeIdentifier = "99";
        }
        return paymodeIdentifier;
    }

    private void setPosOrderData(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        String REQUEST_TYPE = AdditionalInfoUtil.getValueFromAdditionalInfo(requestData, "REQUEST_TYPE");
        if (UPI_POS_ORDER.equals(REQUEST_TYPE)) {
            LOGGER.info("Getting POS Order Extend info from Cache");
            Map<String, String> posOrderExtendInfo = nativeSessionUtil.getPOSOrderExtendInfo(requestData.getMid(),
                    requestData.getOrderId());
            flowRequestBean.setPosOrderData(posOrderExtendInfo);
        }
    }

    private String modifyGoodsInfo(String goodsInfo, String paytmMID) throws FacadeCheckedException, IOException {
        List<GoodsInfo> goodsInfoList = null;
        boolean ff4jSaveCheckoutData = ff4JUtil.isFeatureEnabled(FEATURE_GOODSINFO_AMOUNT_RUPAY_TO_PAISE, paytmMID);
        if (StringUtils.isEmpty(goodsInfo) || !ff4jSaveCheckoutData) {
            return goodsInfo;
        }
        goodsInfoList = ObjectMapperUtil.getObjectMapper().readValue(goodsInfo, new TypeReference<List<GoodsInfo>>() {
        });
        goodsInfoList.stream().map(goodsInfo1 -> setAmountInPaise(goodsInfo1)).collect(Collectors.toList());
        return JsonMapper.mapObjectToJson(goodsInfoList);
    }

    private GoodsInfo setAmountInPaise(GoodsInfo goodsInfo) {
        goodsInfo.getPrice().setValue(AmountUtils.getTransactionAmountInPaise(goodsInfo.getPrice().getValue()));
        return goodsInfo;
    }

    private void populateCoBrandedCustomDisplayName(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isNotBlank(flowRequestBean.getCustomDisplayName())) { // for
            // fresh
            // cards
            // or
            // tokens
            EXT_LOGGER.info("Setting Co branded Custom Display Name: {}", flowRequestBean.getCustomDisplayName());
            flowRequestBean.getExtendInfo().setCobrandedCustomDisplayName(flowRequestBean.getCustomDisplayName());
        } else if (StringUtils.isNotEmpty(flowRequestBean.getSavedCardID())
                && flowRequestBean.getSavedCardID().length() > 15) {
            NativeCashierInfoResponse cashierInfoResponse = populateCashierInfoResponseFromCache(flowRequestBean,
                    requestData, flowRequestBean.getSavedCardID());
            if (cashierInfoResponse != null) {
                SavedCard saveCard = getSavedCardDetails(cashierInfoResponse, flowRequestBean.getSavedCardID());
                if (saveCard != null) {
                    EXT_LOGGER.info("Setting Co branded Custom Display Name: {}", saveCard.getBankName());
                    flowRequestBean.getExtendInfo().setCobrandedCustomDisplayName(saveCard.getBankName());
                }
            }
        }
    }

    private void generateDataForSimplifiedEmi(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean == null || flowRequestBean.getPayMethod() == null) {
            return;
        }
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(requestData
                .getTxnToken());
        if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null
                && nativeInitiateRequest.getInitiateTxnReq().getBody() != null
                && nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedSubvention() != null) {
            /*
             * if simplified subvention object was sent in initiate request but
             * EMI paymethod was not selected on cashier page for all-in-one sdk
             * and custom checkout flow then return
             */
            if (!(EPayMethod.EMI.getMethod().equals(flowRequestBean.getPayMethod()) || EPayMethod.EMI_DC.getMethod()
                    .equals(flowRequestBean.getPayMethod()))) {
                LOGGER.info("Simplified Subvention object was sent in initiateTxn but paymethod is sent as {}",
                        flowRequestBean.getPayMethod());
                return;
            }
            /*
             * Standard EMI case
             */
            if (BooleanUtils.isTrue(nativeInitiateRequest.getInitiateTxnReq().getBody().getSimplifiedSubvention()
                    .getSelectPlanOnCashierPage())
                    && requestData.getEmiSubventionInfo() == null) {
                return;
            }
            // EMI Subvention All in one SDK Flow
            enrichSimplifiedSubventionRequestForAllInOneSDKFlow(requestData, nativeInitiateRequest.getInitiateTxnReq()
                    .getBody().getSimplifiedSubvention());
            ValidateEmiRequestBody validateEmiRequestBody = SubventionEmiServiceHelper.transform(nativeInitiateRequest
                    .getInitiateTxnReq().getBody().getSimplifiedSubvention());
            PaymentDetails paymentDetails = validateEmiRequestBody.getPaymentDetails();
            // setting the txnAmount temporarily, real txn amount set in
            // workflow helper after processing simplified promo
            paymentDetails.setTotalTransactionAmount(Double.parseDouble(AmountUtils
                    .getTransactionAmountInRupee(flowRequestBean.getTxnAmount())));
            if (null != flowRequestBean.getCardNo() && flowRequestBean.getCardNo().length() >= 6) {
                String bin6 = StringUtils.substring(flowRequestBean.getCardNo(), 0, 6);
                paymentDetails.setCardBin6(bin6);
            }
            ValidateRequest validateRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(
                    validateEmiRequestBody, null, null);
            flowRequestBean.setEmiSubventionValidateRequestData(validateRequest);
            flowRequestBean.setEmiSubventionCustomerId(validateRequest.getCustomerId());
            flowRequestBean.setProcessSimplifiedEmi(true);
            processTransactionUtil.paymentOptionCardIndexNoPopulated(requestData.getRequest(), validateRequest);
        }
    }

    private void setWalletPostPaidAuthorizationMode(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        if (PaymentTypeIdEnum.PPI.value.equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.WALLET.value.equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(requestData.getPaymentTypeId())) {
            String mode = BizConstant.DataEnrichmentKey.AUTH_MODE_SSO_TOKEN;
            String txnToken = requestData.getTxnToken();
            if (StringUtils.isNotBlank(txnToken)) {

                InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(txnToken);
                if (orderDetail != null) {
                    if (orderDetail.isSetSSOViaOptLogin()) {
                        mode = BizConstant.DataEnrichmentKey.AUTH_MODE_LOGIN;
                    }
                    if (orderDetail.isAutoLoginViaCookie()) {
                        mode = BizConstant.DataEnrichmentKey.AUTH_MODE_AUTO_LOGIN;
                    }
                }
            }
            workFlowRequestBean.setWalletPostpaidAuthorizationMode(mode);
        }
    }

    private void enrichSimplifiedSubventionRequestForAllInOneSDKFlow(PaymentRequestBean requestData,
            SimplifiedSubvention simplifiedSubvention) {
        if (BooleanUtils.isTrue(simplifiedSubvention.getSelectPlanOnCashierPage())) {
            simplifiedSubvention.setPlanId(requestData.getEmiSubventionInfo().getSubventionPlanId());

            if (CollectionUtils.isNotEmpty(requestData.getEmiSubventionInfo().getItemOfferDetails())) {
                for (ItemOfferDetail itemOfferDetail : requestData.getEmiSubventionInfo().getItemOfferDetails()) {
                    if (itemOfferDetail.getId() == null) {
                        continue;
                    }
                    if (CollectionUtils.isEmpty(simplifiedSubvention.getItems())) {
                        LOGGER.info("Enrichment Simplified Subvention Request : Inside Amount Based Subvention");
                        if (StringUtils.equals(SUBVENTION, itemOfferDetail.getId())) {
                            OfferDetail offerDetail = new OfferDetail();
                            if (itemOfferDetail.getOfferId() != null)
                                offerDetail.setOfferId(itemOfferDetail.getOfferId());
                            simplifiedSubvention.setOfferDetails(offerDetail);
                        }
                    } else {
                        LOGGER.info("Enrichment Simplified Subvention Request : Inside Item Based Subvention");
                        simplifiedSubvention.setItems(simplifiedSubvention.getItems().stream().map(item -> {
                            if (StringUtils.equals(item.getId(), itemOfferDetail.getId())) {
                                OfferDetail offerDetail = new OfferDetail();
                                if (itemOfferDetail.getOfferId() != null)
                                    offerDetail.setOfferId(itemOfferDetail.getOfferId());
                                item.setOfferDetails(offerDetail);
                            }
                            return item;
                        }).collect(Collectors.toList()));
                    }
                }
            }
        }
    }

    private void setFeeRateFactorForCCOnUPI(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        if (requestData.isCCOnUPI()) {
            if (flowRequestBean.getFeeRateFactors() == null) {
                flowRequestBean.setFeeRateFactors(new FeeRateFactors());
            }
            flowRequestBean.getFeeRateFactors().setCcOnUPIRails(true);
            flowRequestBean.setCcOnUPIEnabledForPtcTxn(true);

        }
    }

    private void populateBankTransferDetails(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData) {
        if (null == requestData.getBankTransferDTO() || null == requestData.getBankTransferDTO().get(BANK_TRANSFER)
                || !(requestData.getBankTransferDTO().get(BANK_TRANSFER) instanceof InternalTransactionRequest)) {
            return;
        }

        InternalTransactionRequestBody requestBody = ((InternalTransactionRequest) requestData.getBankTransferDTO()
                .get(BANK_TRANSFER)).getBody();

        /* For Cache Card */
        AccountDetails accountDetails = requestBody.getSourceAccountInfo();
        if (null != accountDetails) {
            workFlowRequestBean.setAccountNumber(accountDetails.getAccountNumber());
            workFlowRequestBean.setHolderName(accountDetails.getAccountHolderName());
            workFlowRequestBean.setBankName(accountDetails.getBankName());
            workFlowRequestBean.setBankCode(accountDetails.getBankCode());
            workFlowRequestBean.setIfsc(accountDetails.getIfscCode());
        }

        workFlowRequestBean.setPayOption(requestBody.getPaymentOption());
        workFlowRequestBean.setPayMethod(requestBody.getPaymentMode());
        /**/

        Map<String, String> channelInfo = new HashMap<>();
        channelInfo.put("utr", requestBody.getUtr());
        channelInfo.put("transactionRequestId", requestBody.getTransactionRequestId());
        channelInfo.put("transactionDate", requestBody.getTransactionDate());
        channelInfo.put("transferMode", requestBody.getTransferMode());
        workFlowRequestBean.setChannelInfo(channelInfo);

        Map<String, String> extendedInfo = workFlowRequestBean.getExtendedInfo();
        if (null == extendedInfo) {
            extendedInfo = new HashMap<>();
            workFlowRequestBean.setExtendedInfo(extendedInfo);
        }
        LOGGER.info("vanInfo :{}", getVanInfoString(requestBody.getVanInfo()));
        extendedInfo.put("vanInfo", getVanInfoString(requestBody.getVanInfo()));
        LOGGER.info("extendedInfo is {}", extendedInfo);
        ExtendedInfoRequestBean extendedInfoRequestBean = workFlowRequestBean.getExtendInfo();
        if (null != requestBody.getExtendInfo()) {
            extendedInfoRequestBean.setSearch1(requestBody.getExtendInfo().get("search1"));
            extendedInfoRequestBean.setSearch2(requestBody.getExtendInfo().get("search2"));
            extendedInfoRequestBean.setSearch3(requestBody.getExtendInfo().get("search3"));
            extendedInfoRequestBean.setVanIfsc(requestBody.getExtendInfo().get("vanIfsc"));
            List<CustomerDetails> customerDetails = requestBody.getVanInfo().getCustomerDetails();
            if (CollectionUtils.isNotEmpty(customerDetails)) {
                extendedInfoRequestBean.setEmail(customerDetails.get(0).getCustomerEmail());
                extendedInfoRequestBean.setPhoneNo(customerDetails.get(0).getCustomerMobile());
                extendedInfoRequestBean.setUserMobile(customerDetails.get(0).getCustomerMobile());
            }
        }

        setTerminalTypeForBankTransfer(workFlowRequestBean);

        // requestBody.getOrderExpiry() is in seconds
        workFlowRequestBean.setOrderExpiryTimeInMerchantContract(requestBody.getOrderExpiry());
    }

    private void setTerminalTypeForBankTransfer(WorkFlowRequestBean workFlowRequestBean) {

        String channelId = workFlowRequestBean.getChannelID();
        if (org.apache.commons.lang.StringUtils.isNotBlank(channelId)) {
            workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.getTerminalTypeByTerminal(channelId));
        }
    }

    private String getVanInfoString(VanInfo vanInfo) {

        ObjectMapper objectMapper = new ObjectMapper();
        String vanInfoJson = "";
        try {
            vanInfoJson = objectMapper.writeValueAsString(vanInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception occurred while serializing VanInfo {} ", e);
        }
        return vanInfoJson;
    }

    private void setUrlsInRiskExtendInfo(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestBean) {
        Map<String, String> riskExtendInfo = new HashMap<>();

        String refererURL = getRefererUrl(requestBean.getTxnToken());
        String callbackURL = flowRequestBean.getCallBackURL();

        String registeredAppURL = null;
        String registeredWebURL = null;

        List<MerchantApiUrlInfo> urls = null;

        if (ff4JUtil.isFeatureEnabled(THEIA_GET_MERCHANT_API_URL_INFO, requestBean.getMid())) {
            urls = merchantUrlService.getMerchantApiUrlInfo("paytm", requestBean.getMid());
        }

        if (urls != null) {
            for (MerchantApiUrlInfo merchantApiUrlInfo : urls) {
                if (StringUtils.equals("web", merchantApiUrlInfo.getUrlType())) {
                    registeredWebURL = merchantApiUrlInfo.getUrl();
                }
                if (StringUtils.equals("app", merchantApiUrlInfo.getUrlType())) {
                    registeredAppURL = merchantApiUrlInfo.getUrl();
                }
            }
        }

        if (StringUtils.equals(BizConstant.CHECKOUT, requestBean.getWorkflow())
                || StringUtils.equals(requestBean.getWorkflow(), BizConstant.ENHANCED_CASHIER_FLOW)
                && (StringUtils.isNotBlank(requestBean.getRiskExtendedInfo()))) {
            Map<String, String> riskExtendInfoMap = AdditionalInfoUtil.generateMapFromAdditionalInfoString(requestBean
                    .getRiskExtendedInfo());
            if (MapUtils.isNotEmpty(riskExtendInfoMap) && StringUtils.isNotBlank(riskExtendInfoMap.get(REFERER_URL))) {
                riskExtendInfo.put(REFERER_URL, riskExtendInfoMap.get(REFERER_URL));
            }
        } else {
            riskExtendInfo.put(REFERER_URL, refererURL);
        }

        riskExtendInfo.put(CALLBACK_URL, callbackURL);
        riskExtendInfo.put(REGISTERED_APP_URL, registeredAppURL);
        riskExtendInfo.put(REGISTERED_WEB_URL, registeredWebURL);

        // PGP-31371 Logs removal activity for theia
        EXT_LOGGER.customInfo("Setting urls in riskExtendInfo: {}", riskExtendInfo);

        if (MapUtils.isNotEmpty(flowRequestBean.getRiskExtendedInfo())) {
            flowRequestBean.getRiskExtendedInfo().putAll(riskExtendInfo);
        } else {
            flowRequestBean.setRiskExtendedInfo(riskExtendInfo);
        }

    }

    private String getRefererUrl(String txnToken) {
        String referalURL = null;
        if (StringUtils.isNotBlank(txnToken)) {
            referalURL = nativeSessionUtil.getRefererURL(txnToken);
        }
        if (StringUtils.isBlank(referalURL)) {
            HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();
            referalURL = httpServletRequest.getHeader(REFERER);
            // LOGGER.info("received blank referalURL from txnToken, getting from httpservletrequest: {}",
            // referalURL);
            EXT_LOGGER.customInfo("received blank referalURL from txnToken, getting from httpservletrequest: {}",
                    referalURL);
        }
        return referalURL;
    }

    private String getSavedCardPayMethod(BinDetail binDetail, WorkFlowRequestBean flowRequestBean) {
        String payMethod = binDetail.getCardType();
        if (PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId())) {
            payMethod = ExtraConstants.EMI;
            if (StringUtils.isNotBlank(flowRequestBean.getPaymentRequestBean().getEmitype())
                    && flowRequestBean.getPaymentRequestBean().getEmitype().equals(EmiType.DEBIT_CARD.getType())) {
                payMethod = ExtraConstants.EMI_DC;
            }
        }
        return payMethod;
    }

    private PaymentOffer getPaymentOffersForSimplifiedOffers(WorkFlowRequestBean requestBean, String txnAmount,
            String txnToken) {
        if (requestBean.getSimplifiedPaymentOffers() == null) {
            return null;
        }
        if (requestBean.isEcomTokenTxn()) {
            LOGGER.info("PaymentOffersForSimplifiedOffers is not applicable for ecomToken txn");
            return null;
        }
        try {
            // if pwp enabled
            if (StringUtils.isNotBlank(requestBean.getPwpCategory())) {
                LOGGER.info("SimplifiedPaymentOffers is not applicable in case of PWP merchant");
                return null;
            } else if (!requestBean.getSimplifiedPaymentOffers().isValidatePromo()
                    && requestBean.getPaytmExpressAddOrHybrid() != null
                    && EPayMode.ADDANDPAY.name().equals(requestBean.getPaytmExpressAddOrHybrid().name())) {
                LOGGER.info("SimplifiedPaymentOffers is not applicable for addnpay having disabled validate promo");
                return null;
            }
            LOGGER.info("Request to Apply promo for simplified promo");
            ApplyPromoRequest promoRequest = new ApplyPromoRequest();
            promoRequest.setHead(new TokenRequestHeader());
            ApplyPromoRequestBody promoRequestBody = new ApplyPromoRequestBody();
            promoRequest.getHead().setChannelId(EChannelId.getEChannelIdByValue(requestBean.getChannelID()));
            promoRequest.getHead().setTxnToken(txnToken);
            promoRequest.getHead().setToken(txnToken);
            promoRequest.getHead().setTokenType(TokenType.TXN_TOKEN);
            promoRequestBody.setPromocode(requestBean.getSimplifiedPaymentOffers().getPromoCode());
            promoRequestBody.setTotalTransactionAmount(getNonWalletTxnAmountIfHybrid(requestBean));
            promoRequestBody.setMid(requestBean.getPaytmMID());
            promoRequestBody.setCustId(StringUtils.isNotBlank(requestBean.getCustID()) ? requestBean.getCustID()
                    : (requestBean.getUserDetailsBiz() != null) ? requestBean.getUserDetailsBiz().getUserId() : null);
            com.paytm.pgplus.enums.PayMethod payMethod = null;
            String paymentMethod = null;
            String issuingBank = null;
            try {
                paymentMethod = requestBean.getPayMethod();
                if (StringUtils.isBlank(paymentMethod) && StringUtils.isNotBlank(requestBean.getPaymentTypeId())
                        && requestBean.getIsSavedCard()) {
                    NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
                    SavedCard saveCard = null;
                    if (cashierInfoResponse != null) {
                        saveCard = fetchSavedCardByCardId(cashierInfoResponse.getBody().getMerchantPayOption()
                                .getSavedInstruments(), requestBean.getSavedCardID());
                        if (saveCard == null) {
                            saveCard = fetchSavedCardByCardId(cashierInfoResponse.getBody().getAddMoneyPayOption()
                                    .getSavedInstruments(), requestBean.getSavedCardID());
                        }
                        if (saveCard != null) {
                            paymentMethod = saveCard.getCardDetails().getCardType();
                            issuingBank = saveCard.getIssuingBank();
                            if (PaymentTypeIdEnum.EMI.value.equals(requestBean.getPaymentTypeId())) {
                                paymentMethod = ExtraConstants.EMI;
                                if (StringUtils.isNotBlank(requestBean.getPaymentRequestBean().getEmitype())
                                        && requestBean.getPaymentRequestBean().getEmitype()
                                                .equals(EmiType.DEBIT_CARD.getType())) {
                                    paymentMethod = ExtraConstants.EMI_DC;
                                }
                            }
                        }
                    }
                }
                payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(paymentMethod);

            } catch (Exception e) {
                LOGGER.error("CheckoutPaymentPromo req builder validation failed");
                throw new BizPaymentOfferCheckoutException("CheckoutPaymentPromo req builder validation failed");
            }
            List<PromoPaymentOption> promoPaymentOptions = ApplyPromoPaymentOptionBuilderFactory
                    .getApplyPromoPaymentOptionBuilder(payMethod).buildPromoPaymentOptions(requestBean,
                            promoRequestBody.getTotalTransactionAmount(), paymentMethod, issuingBank);
            promoRequestBody.setPaymentOptions(promoPaymentOptions);
            promoRequestBody.setCartDetails(requestBean.getSimplifiedPaymentOffers().getCartDetails());
            promoRequest.setBody(promoRequestBody);
            PaymentOffer offer = null;

            PromoServiceResponseBase promoServiceResponseBase = paymentOffersService.getApplyPromoResponse(
                    promoRequest, null);
            offer = populatePaymentOfferFromPromoResponse(promoServiceResponseBase,
                    requestBean.getSimplifiedPaymentOffers(), requestBean.getPaytmExpressAddOrHybrid(), txnToken);
            offer.setEncUserId(promoRequest.getBody().getEncUserId());
            offer.setCartDetails(requestBean.getSimplifiedPaymentOffers().getCartDetails());
            LOGGER.info("Apply promo response fetched successfully");
            return offer;
        } catch (BaseException e) {
            LOGGER.error("base error while calling promo offer for simplified", e);
            if (requestBean.getSimplifiedPaymentOffers().isValidatePromo()) {
                throw e;
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("error while calling promo offer for simplified", e);
            if (requestBean.getSimplifiedPaymentOffers().isValidatePromo()) {
                throw e;
            }
            return null;
        }
    }

    private String getNonWalletTxnAmountIfHybrid(WorkFlowRequestBean workFlowRequestBean) {
        String actualTxnAmount = StringUtils.isNotBlank(workFlowRequestBean.getPromoAmount()) ? workFlowRequestBean
                .getPromoAmount() : workFlowRequestBean.getTxnAmount();

        String promoTxnAmount = (workFlowRequestBean.getSimplifiedPaymentOffers() != null && StringUtils
                .isNotBlank(workFlowRequestBean.getSimplifiedPaymentOffers().getPromoAmount())) ? AmountUtils
                .getTransactionAmountInPaise(workFlowRequestBean.getSimplifiedPaymentOffers().getPromoAmount())
                : actualTxnAmount;

        if (EPayMode.HYBRID == workFlowRequestBean.getPaytmExpressAddOrHybrid()) {
            return AmountUtils.getTransactionAmountInRupee(String.valueOf(Long.parseLong(actualTxnAmount)
                    - Long.parseLong(workFlowRequestBean.getWalletAmount())));
        }
        return AmountUtils.getTransactionAmountInRupee(promoTxnAmount);
    }

    private PaymentOffer populatePaymentOfferFromPromoResponse(PromoServiceResponseBase baseResponse,
            SimplifiedPaymentOffers simplifiedPaymentOffers, EPayMode paytmExpressAddOrHybrid, String txnToken) {
        String errorText = null;
        boolean validatePromo = simplifiedPaymentOffers.isValidatePromo();
        if (baseResponse != null && baseResponse.getStatus() == 1) {
            PaymentOffer paymentOffer = new PaymentOffer();
            if (baseResponse instanceof ApplyPromoServiceResponse) {
                ApplyPromoServiceResponse response = (ApplyPromoServiceResponse) baseResponse;
                if (response.getData() != null) {
                    if (response.getData().getStatus() == 1) {
                        paymentOffer = new PaymentOffer();
                        paymentOffer.setOfferBreakup(getPromoOfferDetails(response.getData()));
                    } else {
                        errorText = response.getData().getPromotext();
                    }
                }
            } else {
                ApplyPromoServiceResponseV2 response = (ApplyPromoServiceResponseV2) baseResponse;
                if (response.getData() != null) {
                    if (response.getData().getStatus() == 1) {
                        paymentOffer = new PaymentOffer();
                        paymentOffer.setOfferBreakup(paymentOffersServiceHelperV2.getPromoOfferDetails(
                                response.getData(), null, null));
                    } else {
                        errorText = response.getData().getPromotext();
                    }
                }
            }
            if (paymentOffer != null) {
                if (paymentOffer.getOfferBreakup() != null) {
                    paymentOffer.setTotalCashbackAmount(paymentOffer.getOfferBreakup().get(0).getCashbackAmount());
                    paymentOffer.setTotalInstantDiscount(paymentOffer.getOfferBreakup().get(0).getInstantDiscount());
                    paymentOffer.setTotalPaytmCashbackAmount(paymentOffer.getOfferBreakup().get(0)
                            .getPaytmCashbackAmount());
                }
                if (StringUtils.isNotBlank(simplifiedPaymentOffers.getPromoAmount())) {
                    if (paytmExpressAddOrHybrid != null
                            && !EPayMode.HYBRID.name().equals(paytmExpressAddOrHybrid.name())) {
                        paymentOffer.setTotalTransactionAmount(simplifiedPaymentOffers.getPromoAmount());
                    }
                }

                NativeInitiateRequest request = nativeSessionUtil.validate(txnToken);
                if (request != null && request.getInitiateTxnReq() != null
                        && request.getInitiateTxnReq().getBody() != null) {
                    LOGGER.info("Setting payment offer object in order detail");
                    request.getInitiateTxnReq().getBody().setPaymentOffersApplied(paymentOffer);
                    nativeSessionUtil.setOrderDetail(txnToken, request.getInitiateTxnReq().getBody());
                }
                return paymentOffer;
            }
        }
        if (errorText == null) {
            errorText = (baseResponse != null && baseResponse.getError() != null) ? baseResponse.getError()
                    .getMessage() : "Promo Error";
        }

        LOGGER.error("apply promo response failure :{}", errorText);
        if (validatePromo) {
            throw BaseException.getException(errorText);
        }
        return null;
    }

    private List<PaymentOfferDetails> getPromoOfferDetails(ApplyPromoResponseData responseData) {
        List<PaymentOfferDetails> toRet = new ArrayList<>();
        PaymentOfferDetails paymentOfferDetails = new PaymentOfferDetails();
        paymentOfferDetails.setPromotext(responseData.getPromotext());
        paymentOfferDetails.setPromocodeApplied(responseData.getPromocode());
        paymentOfferDetails.setCashbackAmount(getSavingAmount(responseData.getSavings(), RedemptionType.CASHBACK));
        paymentOfferDetails.setInstantDiscount(getSavingAmount(responseData.getSavings(), RedemptionType.DISCOUNT));
        paymentOfferDetails.setPaytmCashbackAmount(getSavingAmount(responseData.getSavings(),
                RedemptionType.PAYTM_CASHBACK));
        paymentOfferDetails.setPromoVisibility(Boolean.valueOf(responseData.getPromoVisibility()).toString());
        paymentOfferDetails.setResponseCode(responseData.getResponseCode());
        toRet.add(paymentOfferDetails);
        return toRet;
    }

    private String getSavingAmount(List<PromoSaving> promoSavings, RedemptionType redemptionType) {
        if (promoSavings == null)
            return null;
        for (PromoSaving promoSaving : promoSavings) {
            Optional<RedemptionType> redemptionTypeOptional = RedemptionType
                    .fromString(promoSaving.getRedemptionType());
            if (redemptionTypeOptional.isPresent() && redemptionTypeOptional.get() == redemptionType) {
                return PaymentOfferUtils.getAmountInRupees(promoSaving.getSavings());
            }
        }
        return null;
    }

    private void fetchSavedCardFromCachedLitepayviewResponse(WorkFlowRequestBean flowRequestBean,
            PaymentRequestBean requestBean) {
        PayCardOptionViewBiz payCardOptionViewBiz = workFlowHelper.getLitePayViewConsultResponse(
                flowRequestBean.getPaytmMID(), flowRequestBean.getSavedCardID());
        if (null != payCardOptionViewBiz) {
            flowRequestBean.setCardIndexNo(flowRequestBean.getSavedCardID());
            flowRequestBean.setExpiryYear(Short.parseShort(payCardOptionViewBiz.getExpiryYear()));
            flowRequestBean.setExpiryMonth(Short.parseShort(payCardOptionViewBiz.getExpiryMonth()));
            flowRequestBean.setInstId(payCardOptionViewBiz.getInstId());
            flowRequestBean.setBankName(payCardOptionViewBiz.getInstName());
            flowRequestBean.setCardType(payCardOptionViewBiz.getPayMethod());
            flowRequestBean.setCardNo(payCardOptionViewBiz.getMaskedCardNo());
            flowRequestBean.setCardScheme(payCardOptionViewBiz.getCardScheme());
            if (!(flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
                flowRequestBean.setPayMethod(flowRequestBean.getCardType());
                flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_" + flowRequestBean.getCardScheme());
            }
            flowRequestBean.setGcin(payCardOptionViewBiz.getGcin());

            // Risk integration -coft for platform saved card offline
            // transaction
            String par = null;

            if (enableGcinOnCoftForRisk(flowRequestBean)) {
                String mid = (StringUtils.isNotBlank(requestBean.getSgwReferenceId()) && requestBean.isSubscription()) ? aoaUtils
                        .getAOAMidForPGMid(flowRequestBean.getPaytmMID()) : flowRequestBean.getPaytmMID();
                FetchPanUniqueReferenceResponse tokenData = coftTokenDataService.getTokenData(mid, "CIN",
                        flowRequestBean.getSavedCardID(), true);
                par = (Objects.nonNull(tokenData) && Objects.nonNull(tokenData.getBody())) ? tokenData.getBody()
                        .getPanUniqueReference() : null;
                flowRequestBean.setPar(par);
            }

            setRiskExtendInfoForCoft(flowRequestBean, flowRequestBean.getSavedCardID(), par,
                    payCardOptionViewBiz.getCardBin(), null);
            flowRequestBean.setLastFourDigits(payCardOptionViewBiz.getMaskedCardNo().substring(
                    payCardOptionViewBiz.getMaskedCardNo().length() - 4));
        } else {
            throw new TheiaServiceException("couldn't fetch saved cards on the basis of saved cardId");
        }
    }

    private void enrichForFetchChannelDetailsTask(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {

        if (!requestData.isMlvSupported()) {
            return;
        }

        flowRequestBean.setMlvSupported(requestData.isMlvSupported());
        flowRequestBean.setAppVersion(requestData.getAppVersion());
        flowRequestBean.setqRCodeInfo(requestData.getqRCodeInfo());
    }

    private void setCallBackUrlForOffline(PaymentRequestBean requestData) {
        // Do not set static callback of offline for MLV flow.
        // isOfflineTxnFlow is true for MLV flow
        if (requestData.isOfflineTxnFlow()) {
            return;
        }

        if (ERequestType.OFFLINE.getType().equals(requestData.getRequestType())
                || BizRequestResponseMapperImpl.isQRCodeRequest(requestData)) {
            String callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
            LOGGER.info("Setting callback url for offline {}", callbackUrl);
            if (StringUtils.isEmpty(callbackUrl)) {
                LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                throw new TheiaServiceException("CallbackUrl is not configured for offline request");

            }
            callbackUrl = callbackUrl + requestData.getOrderId();
            requestData.setCallbackUrl(callbackUrl);
        }
    }

    /*
     * Setting static callback url in case of online flow for link payment
     * initiated via app
     */
    private void setCallBackUrlForJSLinks(PaymentRequestBean requestData) {
        if (requestData.getLinkDetailsData() != null
                && StringUtils.isNotEmpty(requestData.getLinkDetailsData().getChannelId())
                && requestData.getLinkDetailsData().getChannelId().equalsIgnoreCase("APP")) {
            // Do not set static callback of offline for MLV flow.
            // isOfflineTxnFlow is true for MLV flow
            if (requestData.isOfflineTxnFlow()) {
                return;
            }
            String callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.LINK_STATIC_CALLBACK_URL);
            LOGGER.info("Setting callback url for link {}", callbackUrl);
            if (StringUtils.isEmpty(callbackUrl)) {
                LOGGER.info("Please set reloadable property {}", PropertyConstrant.LINK_STATIC_CALLBACK_URL);
                throw new TheiaServiceException("CallbackUrl is not configured for link request");
            }
            callbackUrl = callbackUrl + requestData.getOrderId();
            requestData.setCallbackUrl(callbackUrl);
        }
    }

    private void setPromoAmount(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean paymentRequestBean)
            throws TheiaDataMappingException {
        String promoAmountReq = paymentRequestBean.getPromoAmount();
        TheiaRequestValidator.validateAmountIfNotBlank(promoAmountReq);
        workFlowRequestBean.setPromoAmount(AmountUtils.getTransactionAmountInPaise(promoAmountReq));
    }

    private void setCardHash(WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean paymentRequestBean) {
        workFlowRequestBean.setCardHash(paymentRequestBean.getCardHash());
    }

    private void setDataForNativeJsonRequest(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean paymentRequestBean) {
        workFlowRequestBean.setNativeJsonRequest(paymentRequestBean.isNativeJsonRequest());
    }

    private void buildPaymentOfferCheckoutReqData(WorkFlowRequestBean requestBean, PaymentOffer payOffer) {
        if (payOffer == null) {
            return;
        }
        PaymentOffer paymentOffer = payOffer;
        if (paymentOffer != null) {
            if (CollectionUtils.isNotEmpty(paymentOffer.getOfferBreakup())) {
                LOGGER.info("Setting PaymentOffer in WorkflowRequestBean = {}", paymentOffer);
                // Currently hybrid is not supported so list will have only one
                // offer
                // PaymentOfferDetails paymentOfferDetails =
                // paymentOffer.getOfferBreakup().get(0);
                // ApplyPromoResponseData applyPromoResponseData = new
                // ApplyPromoResponseData();
                // applyPromoResponseData.setPromocode(paymentOfferDetails.getPromocodeApplied());
                // applyPromoResponseData.setPromotext(paymentOfferDetails.getPromotext());
                // applyPromoResponseData
                // .setPromoVisibility(Boolean.parseBoolean(paymentOfferDetails.getPromoVisibility()));
                // applyPromoResponseData.setResponseCode(paymentOfferDetails.getResponseCode());
                // applyPromoResponseData.setSavings(getPromoSavings(paymentOfferDetails));
                // applyPromoResponseData.setStatus(1);
                requestBean.setPaymentOfferCheckoutReqData(paymentOffer);
                boolean migrateBankOffersPromo = ff4JUtil.isMigrateBankOffersPromo(requestBean.getPaytmMID());
                if (!migrateBankOffersPromo) {
                    requestBean.setBin8OfferAvailableOnMerchant(paymentOffersServiceHelper
                            .isBin8OfferAvailableOnMerchant(requestBean.getPaytmMID(),
                                    (requestBean.getUserDetailsBiz() != null) ? requestBean.getUserDetailsBiz()
                                            .getUserId() : null));
                } else {
                    requestBean.setBin8OfferAvailableOnMerchant(paymentOffersServiceHelperV2
                            .isBin8OfferAvailableOnMerchant(requestBean.getPaytmMID(),
                                    (requestBean.getUserDetailsBiz() != null) ? requestBean.getUserDetailsBiz()
                                            .getUserId() : null, requestBean.getPromoContext()));
                }

            }
        }
    }

    private void buildEmiSubventionCheckoutReqData(WorkFlowRequestBean requestBean,
            HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            return;
        }

        ValidateResponse validateResponseObject = (ValidateResponse) httpServletRequest
                .getAttribute(VALIDATE_EMI_RESPONSE);
        ValidateRequest validateRequestObject = (ValidateRequest) httpServletRequest.getAttribute(VALIDATE_EMI_REQUEST);
        String subventionCustomerId = (String) httpServletRequest.getAttribute(EMI_SUBVENTION_CUSTOMER_ID);

        if (validateResponseObject != null && validateRequestObject != null
                && validateResponseObject instanceof ValidateResponse) {
            ValidateResponse validateResponsed = (ValidateResponse) validateResponseObject;
            ValidateRequest validateRequest = (ValidateRequest) validateRequestObject;
            requestBean.setEmiSubventionOfferCheckoutReqData(validateResponsed);
            requestBean.setEmiSubventionValidateRequestData(validateRequest);
            requestBean.setEmiSubventionCustomerId(subventionCustomerId);
        }

    }

    private List<PromoSaving> getPromoSavings(PaymentOfferDetails paymentOfferDetails) {
        List<PromoSaving> promoSavings = new ArrayList<>();
        if (StringUtils.isNotBlank(paymentOfferDetails.getCashbackAmount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(RedemptionType.CASHBACK.getType());
            promoSaving.setSavings(PaymentOfferUtils.getAmountInPaise(paymentOfferDetails.getCashbackAmount()));
            promoSavings.add(promoSaving);
        }
        if (StringUtils.isNotBlank(paymentOfferDetails.getInstantDiscount())) {
            PromoSaving promoSaving = new PromoSaving();
            promoSaving.setRedemptionType(RedemptionType.DISCOUNT.getType());
            promoSaving.setSavings(PaymentOfferUtils.getAmountInPaise(paymentOfferDetails.getInstantDiscount()));
            promoSavings.add(promoSaving);
        }
        return promoSavings;
    }

    private void setRiskExtendedInfoNative(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        // added check for non-offline flow
        if (!(flowRequestBean.isScanAndPayFlow()) && StringUtils.isNotBlank(requestData.getTxnToken())) {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(requestData.getTxnToken());
            // setting riskExtendInfo for khatabook
            if (orderDetail != null && MapUtils.isNotEmpty(orderDetail.getRiskExtendInfo())) {
                // alreadySet in risk
                if (MapUtils.isNotEmpty(flowRequestBean.getRiskExtendedInfo())) {
                    flowRequestBean.getRiskExtendedInfo().putAll(orderDetail.getRiskExtendInfo());
                } else {
                    flowRequestBean.setRiskExtendedInfo(orderDetail.getRiskExtendInfo());
                }
            }
        }
    }

    private void setRiskInfoForOffline(WorkFlowRequestBean flowRequestBean, String riskInfoDataString) {

        if (StringUtils.isBlank(riskInfoDataString)) {
            LOGGER.debug("RiskextendedInfo is null or empty: {}", riskInfoDataString);
            return;
        }

        String[] riskInfoArray = riskInfoDataString.split(Pattern.quote(ADDITIONAL_INFO_DELIMITER));
        Map<String, String> riskInfoMap = new HashMap<>();

        for (String keyVal : riskInfoArray) {
            String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
            if (keyValSplit.length == 2) {
                riskInfoMap.put(keyValSplit[0].trim(), keyValSplit[1].trim());
            }
        }

        LOGGER.info("RiskExtendedInfo Map: {}", riskInfoMap);
        flowRequestBean.setUserLbsLatitude(String.valueOf(riskInfoMap.get(USER_LATITUDE)));
        flowRequestBean.setUserLbsLongitude(String.valueOf(riskInfoMap.get(USER_LONGITUDE)));
        flowRequestBean.setMode(String.valueOf(riskInfoMap.get(MODE)));
        flowRequestBean.setRiskExtendedInfo(riskInfoMap);
    }

    private void setRiskInfoForFastForward(WorkFlowRequestBean flowRequestBean, String riskInfoDataString) {

        Map<String, String> riskInfoMap = new HashMap<>();

        try {
            String mid = flowRequestBean.getPaymentRequestBean().getMid();
            if (StringUtils.isBlank(riskInfoDataString)) {
                LOGGER.debug("RiskextendedInfo is null or empty: {}", riskInfoDataString);
                return;
            }
            boolean isFeatureEnable = ff4JUtil.isFeatureEnabled(EXPLICIT_JSON_CONVERSION_KAFKA_PAYLOAD, mid);
            if (isFeatureEnable) {
                try {
                    LOGGER.info("Changing RiskInfoMap By Applying Explicit Json Conversion");
                    Map<String, Object> riskInfoTempMap = new HashMap<>();
                    riskInfoTempMap = JsonMapper.mapJsonToObject(riskInfoDataString, Map.class);
                    Set<String> keySet = riskInfoTempMap.keySet();
                    for (String key : keySet) {
                        if (riskInfoTempMap.get(key) != null && riskInfoTempMap.get(key) instanceof List) {
                            riskInfoMap.put(key, JsonMapper.mapObjectToJson(riskInfoTempMap.get(key)));
                        } else if (riskInfoTempMap.get(key) != null) {
                            riskInfoMap.put(key, String.valueOf(riskInfoTempMap.get(key)));
                        }
                    }
                    riskInfoTempMap = null;
                } catch (Exception e) {
                    boolean logStackTrace = ff4JUtil.isFeatureEnabled(
                            BizConstant.Ff4jFeature.NOTIFY_PAYMENT_DWH_STACKTRACE, mid);
                    if (logStackTrace)
                        LOGGER.error("Exception Occurred in explicit Json conversion: ", e);
                    else
                        LOGGER.error("Exception Occurred in explicit Json conversion: {}", e.getMessage());

                    riskInfoMap = JsonMapper.mapJsonToObject(riskInfoDataString, Map.class);
                }
            } else {
                riskInfoMap = JsonMapper.mapJsonToObject(riskInfoDataString, Map.class);
            }
            LOGGER.info("Risk Info Map: {}", riskInfoMap);
            flowRequestBean.setUserLbsLongitude(String.valueOf(riskInfoMap.get(USER_LONGITUDE)));
            flowRequestBean.setUserLbsLatitude(String.valueOf(riskInfoMap.get(USER_LATITUDE)));
            flowRequestBean.setMode(String.valueOf(riskInfoMap.get(MODE)));
            flowRequestBean.setRiskExtendedInfo(riskInfoMap);

        } catch (Exception e) {

            LOGGER.error("Exception Occurred in setting RiskExtendedInfo: {}", e.getMessage());
        }
    }

    private void setRiskInfoForAddMoney(WorkFlowRequestBean flowRequestBean, String riskInfoDataString) {

        if (StringUtils.isBlank(riskInfoDataString)) {
            LOGGER.debug("RiskextendedInfo is null or empty: {}", riskInfoDataString);
            return;
        }

        String[] riskInfoArray = riskInfoDataString.split(Pattern.quote(ADDITIONAL_INFO_DELIMITER));
        Map<String, String> riskInfoMap = new HashMap<>();

        for (String keyVal : riskInfoArray) {
            String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
            if (keyValSplit.length == 2) {
                riskInfoMap.put(keyValSplit[0].trim(), keyValSplit[1].trim());
            }
        }

        LOGGER.info("RiskExtendedInfo Map: {}", riskInfoMap);
        flowRequestBean.setUserLbsLatitude(String.valueOf(riskInfoMap.get(USER_LATITUDE)));
        flowRequestBean.setUserLbsLongitude(String.valueOf(riskInfoMap.get(USER_LONGITUDE)));
        flowRequestBean.setMode(String.valueOf(riskInfoMap.get(MODE)));
        flowRequestBean.setRiskExtendedInfo(riskInfoMap);
    }

    private void setAmount(final PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean)
            throws TheiaDataMappingException {
        if (!(ERequestType.SUBSCRIBE.name().equals(requestData.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION_PAY.name().equals(requestData.getRequestType())
                || ERequestType.isSubscriptionCreationRequest(requestData.getRequestType()) || ERequestType.NATIVE_MF_SIP_PAY
                .name().equals(requestData.getRequestType()))) {
            validateAndSetAmount(requestData, flowRequestBean);
            return;
        }

        if (requestData.isSuperGwFpoApiHit()
                && ERequestType.NATIVE_SUBSCRIPTION_PAY.name().equals(requestData.getRequestType())
                && StringUtils.isBlank(requestData.getTxnAmount())) {
            return;
        }

        // Below Logic for txnAmount handling in case of Subscription creation
        BigDecimal txnAmount = new BigDecimal(requestData.getTxnAmount());
        if ("S2S".equals(requestData.getConnectiontype())) {
            if (txnAmount.doubleValue() != 0d) {
                throwExceptionForAmount(txnAmount.toPlainString());
            } else {
                flowRequestBean.setTxnAmount("0");
            }
        } else {
            BigDecimal subsMinAmount = new BigDecimal(ConfigurationUtil.getProperty(
                    ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1"));
            if (txnAmount.doubleValue() == 0d) {
                flowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(subsMinAmount.toPlainString()));
                flowRequestBean.getExtendInfo().setAmountToBeRefunded(
                        AmountUtils.getTransactionAmountInPaise(subsMinAmount.toPlainString()));
            } else {
                flowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(subsMinAmount.toPlainString()));
                // Below Code is commented as part of PGP-33687
                // if (subsMinAmount.compareTo(txnAmount) > 0) {
                // flowRequestBean.getExtendInfo().setAmountToBeRefunded(
                // AmountUtils.getTransactionAmountInPaise(subsMinAmount.subtract(txnAmount).toPlainString()));
                // }
                validateAndSetAmount(requestData, flowRequestBean);
            }
        }
        //

    }

    private void setAmountToBeRefundedInExtendInfo(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (requestData.isAutoRefund()
                && ff4JUtil.isFeatureEnabled(BizConstant.Ff4jFeature.SET_AMOUNT_TO_BE_REFUNDED_FIELD,
                        requestData.getMid())
                && (ERequestType.SUBSCRIBE.name().equals(requestData.getRequestType())
                        || ERequestType.NATIVE_SUBSCRIPTION_PAY.name().equals(requestData.getRequestType())
                        || ERequestType.isSubscriptionCreationRequest(requestData.getRequestType()) || ERequestType.NATIVE_MF_SIP_PAY
                        .name().equals(requestData.getRequestType()))) {
            try {
                EXT_LOGGER.customInfo("setting AmountToBeRefunded field");
                BigDecimal subsMinAmount = new BigDecimal(ConfigurationUtil.getProperty(
                        ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1"));
                flowRequestBean.getExtendInfo().setAmountToBeRefunded(
                        AmountUtils.getTransactionAmountInPaise(subsMinAmount.toPlainString()));
            } catch (Exception e) {
                LOGGER.error("Exception in setting amountTobeRefunded in extendInfo {}", e.getMessage());
            }
        }
    }

    private void validateAndSetWalletAmount(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        String walletAmount = requestData.getWalletAmount();

        // We want to skip validation of wallet amount in case of AddAndPay and
        // Hybrid as it can be 0.00 and still valid
        if (StringUtils.isNotBlank(walletAmount)
                && (StringUtils.isNotBlank(requestData.getPaymentTypeId())
                        && "PPI".equals(requestData.getPaymentTypeId()) && !("NATIVE".equals(requestData
                        .getRequestType())))) {
            if (requestData.getRequestType().contains("SUBSCRIPTION")
                    || requestData.getRequestType().contains("SUBSCRIBE")
                    || requestData.getRequestType().contains("MF_SIP")) {
                TheiaRequestValidator.validateAmountIfNotBlank(walletAmount, true);
            } else {
                TheiaRequestValidator.validateAmountIfNotBlank(walletAmount);
            }
        }

        flowRequestBean.setWalletAmount(AmountUtils.getTransactionAmountInPaise(walletAmount));
    }

    private void validateAndSetAmount(final PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean)
            throws TheiaDataMappingException {
        String txnAmountReq = requestData.getTxnAmount();
        if (requestData.getRequestType().contains("SUBSCRIPTION") || requestData.getRequestType().contains("SUBSCRIBE")
                || requestData.getRequestType().contains("MF_SIP")) {
            TheiaRequestValidator.validateAmountIfNotBlankForSubscription(txnAmountReq);
        } else if (PayMethod.LOYALTY_POINT.getMethod().equals(requestData.getPaymentTypeId())) {
            TheiaRequestValidator.validateAmountIfNotBlankForLoyaltyPoint(txnAmountReq);
        } else {
            TheiaRequestValidator.validateAmountIfNotBlank(txnAmountReq);
        }
        flowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(txnAmountReq));
    }

    private void throwExceptionForAmount(String txnAmountReq) throws TheiaDataMappingException {
        throw new TheiaDataMappingException(new StringBuilder(txnAmountReq).append(
                " is not a valid value for TxnAmount").toString(), ResponseConstants.INVALID_TXN_AMOUNT);
    }

    private ExtendedInfoRequestBean buildExtendedInfoRequestBean(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {
        final ExtendedInfoRequestBean extInforequestBean = new ExtendedInfoRequestBean();
        /**
         * currently in case of offline there is a parameter
         * REQUEST_TYPE:QR_MERCHANT in additional flow Now when native will be
         * used for offline flows we will set this flag and will use this as
         * request type will always be native
         */
        flowRequestBean.setOfflineFlow(isQRCodeRequest(requestData));

        if (flowRequestBean.isOfflineFlow()
                || (ERequestType.OFFLINE.getType().equals(requestData.getRequestType())
                        && StringUtils.isBlank(requestData.getLinkId()) && StringUtils.isBlank(requestData
                        .getInvoiceId()))) {
            FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
            if (feeRateFactors == null) {
                feeRateFactors = new FeeRateFactors();
                flowRequestBean.setFeeRateFactors(feeRateFactors);
            }
            flowRequestBean.getFeeRateFactors().setQr(true);
        }

        flowRequestBean.setScanAndPayFlow(requestData.isScanAndPayFlow());
        extInforequestBean.setTheme(requestData.getTheme());
        extInforequestBean.setEmail(requestData.getEmail());
        extInforequestBean.setClientIP(EnvInfoUtil.getClientIP(requestData.getRequest()));
        extInforequestBean.setPhoneNo(StringUtils.isNotBlank(requestData.getMobileNo()) ? requestData.getMobileNo()
                : requestData.getMsisdn());
        extInforequestBean.setWebsite(requestData.getWebsite());
        extInforequestBean.setMerchantTransId(requestData.getOrderId());
        extInforequestBean.setPaytmMerchantId(requestData.getMid());
        extInforequestBean.setAlipayMerchantId(flowRequestBean.getAlipayMID());
        extInforequestBean.setSsoToken(requestData.getSsoToken());
        extInforequestBean.setMccCode(requestData.getIndustryTypeId());
        extInforequestBean.setMerchantName(merchantExtendInfoProvider.getMerchantName(requestData));
        extInforequestBean.setMerchantOnPaytm(merchantExtendInfoProvider.isMerchantOnPaytm(requestData));
        extInforequestBean.setMerchantKybId(merchantExtendInfoProvider.getMerchantKybId(requestData));
        extInforequestBean.setTotalTxnAmount(requestData.getTxnAmount());
        extInforequestBean.setProductCode(setProductCode(requestData, flowRequestBean));
        extInforequestBean.setPromoCode(requestData.getPromoCampId());
        extInforequestBean.setUdf2(requestData.getUdf2());
        extInforequestBean.setUdf3(requestData.getUdf3());
        extInforequestBean.setSearch1(requestData.getResellerId());
        extInforequestBean.setResellerName(requestData.getResellerName());
        extInforequestBean.setAdditionalInfo(requestData.getAdditionalInfo());
        extInforequestBean.setRequestType(requestData.getRequestType());
        extInforequestBean.setCustID(requestData.getCustId());
        extInforequestBean.setPayerDeviceId(requestData.getDeviceId());
        extInforequestBean.setMerchantLinkRefId(requestData.getMerchantLinkRefId());

        if (ERequestType.OFFLINE.getType().equals(requestData.getRequestType())) {
            if (StringUtils.isNotBlank(requestData.getLinkId())) {
                extInforequestBean.setRequestType(ERequestType.LINK_BASED_PAYMENT.getType());
            } else if (StringUtils.isNotBlank(requestData.getInvoiceId())) {
                extInforequestBean.setRequestType(ERequestType.LINK_BASED_PAYMENT_INVOICE.getType());
            }
        }
        if (requestData.getLinkDetailsData() != null && StringUtils.isNotBlank(requestData.getSubRequestType())) {
            extInforequestBean.setRequestType(requestData.getSubRequestType());
        }
        if (StringUtils.isNotBlank(flowRequestBean.getTargetPhoneNo())) {
            extInforequestBean.setTargetPhoneNo(requestData.getTargetPhoneNo());
        }
        extInforequestBean.setAddMoneyDestination(requestData.getAddMoneyDestination());

        // set extendinfo parameters for merchant limit checked
        extInforequestBean.setMerchantLimitEnabled(requestData.isMerchantLimitEnabled());

        /*
         * merchantLimitChecked will be set only when merchantLimitEnabled is
         * true
         */
        if (requestData.isMerchantLimitEnabled()) {
            extInforequestBean.setMerchantLimitUpdated(requestData.isMerchantLimitUpdated());
        }

        if (null != requestData.getPromoCodeResponse()) {
            extInforequestBean.setPromoResponseCode(requestData.getPromoCodeResponse().getPromoResponseCode());
            extInforequestBean.setPromoApplyResultStatus(requestData.getPromoCodeResponse().getResultStatus());
        }
        extInforequestBean.setExtraParamsMap(requestData.getExtraParamsMap());

        if (StringUtils.isNotEmpty(requestData.getLinkName())
                && StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid())) {
            extInforequestBean.setLinkName(requestData.getLinkName());
        }

        if (StringUtils.isNotBlank(requestData.getLinkDescription())
                && StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid())) {
            extInforequestBean.setUdf1(requestData.getLinkDescription());
            extInforequestBean.setLinkDescription(requestData.getLinkDescription());
        } else {
            extInforequestBean.setUdf1(requestData.getUdf1());
        }

        if (StringUtils.isNotEmpty(requestData.getLinkId()) || StringUtils.isNotEmpty(requestData.getInvoiceId())) {
            if (!ProcessTransactionHelper.validateStringAsPerRegex(requestData.getUdf2(), VALID_CUSTOMER_COMMENT)
                    || !ProcessTransactionHelper.checkValidLength(requestData.getUdf2(), Integer
                            .valueOf(ConfigurationUtil.getProperty(
                                    TheiaConstant.ExtraConstants.MIN_LENGTH_CUSTOMER_COMMENT,
                                    TheiaConstant.MIN_LENGTH_CUSTOMER_COMMENT_DEFAULT)), Integer
                            .valueOf(ConfigurationUtil.getProperty(
                                    TheiaConstant.ExtraConstants.MAX_LENGTH_CUSTOMER_COMMENT,
                                    TheiaConstant.MAX_LENGTH_CUSTOMER_COMMENT_DEFAULT)))) {
                extInforequestBean.setUdf2("");
            }
        }

        if (ERequestType.OFFLINE.getType().equals(requestData.getRequestType())
                || requestData.isOfflineFastForwardRequest() || requestData.isOfflineFetchPayApi()
                || flowRequestBean.isOfflineFlow()) {
            populateOfflineAdditionalParam(requestData, extInforequestBean);
            flowRequestBean.setQRIdFlowOnly(requestData.isQRIdFlowOnly());
            extInforequestBean.setComments(requestData.getComments());
            extInforequestBean.setMaskedCustomerMobileNumber(requestData.getMaskedCustomerMobileNumber());
            extInforequestBean.setPosId(requestData.getPosId());
            extInforequestBean.setUniqueReferenceLabel(requestData.getUniqueReferenceLabel());
            extInforequestBean.setUniqueReferenceValue(requestData.getUniqueReferenceValue());
            extInforequestBean.setPccCode(requestData.getPccCode());
            extInforequestBean.setPrn(requestData.getPrn());
            extInforequestBean
                    .setFlowType(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        }
        if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid())) {
            if (StringUtils.isNotBlank(requestData.getInvoiceId()))
                extInforequestBean.setLinkBasedInvoicePayment(true);
            else if (StringUtils.isNotBlank(requestData.getLinkId()))
                extInforequestBean.setLinkBasedNonInvoicePayment(true);
        }
        extInforequestBean.setMutualFundFeedInfo(requestData.getAdditionalInfoMF());
        setMerchantClientIdIfnotEmpty(extInforequestBean, requestData);

        /*
         * for addnpay txn in native, make txntype=addnpay and
         * setTopupAndPay=true
         */
        if (requestData.getRequestType().equals(ERequestType.NATIVE.getType())
                && StringUtils.equals(requestData.isAddMoney(), "1")) {
            extInforequestBean.setTxnType(PaymentType.ADDNPAY.getValue());
            extInforequestBean.setTopupAndPay(true);
        }

        if (requestData.getRequestType().equals(ERequestType.PAYTM_EXPRESS.getType())
                && StringUtils.equals(requestData.isAddMoney(), "1")) {
            extInforequestBean.setTxnType(PaymentType.ADDNPAY.getValue());
        }

        if (requestData.getRequestType().equals(ERequestType.NATIVE_SUBSCRIPTION.getType())
                && StringUtils.equals(requestData.isAddMoney(), "1")) {
            extInforequestBean.setTxnType(PaymentType.ADDNPAY.getValue());
        }

        extInforequestBean.setEnhancedNative(requestData.isEnhancedCashierPageRequest());

        if (flowRequestBean.isOfflineFlow()) {
            extInforequestBean.setOfflineFlow(flowRequestBean.isOfflineFlow());
        }

        if (requestData.getExtendInfo() != null) {
            if (requestData.getExtendInfo().getOrderAdditionalInfo() != null) {
                extInforequestBean.setOrderAdditionalInfo(requestData.getExtendInfo().getOrderAdditionalInfo());
            }
            if (StringUtils.isNotBlank(requestData.getExtendInfo().getSdkType())) {
                extInforequestBean.setSdkType(requestData.getExtendInfo().getSdkType());
            }
            if (StringUtils.isNotBlank(requestData.getExtendInfo().getHeadAccount())) {
                if (extInforequestBean.getExtraParamsMap().isEmpty())
                    extInforequestBean.setExtraParamsMap(new HashMap<>());
                extInforequestBean.getExtraParamsMap().put("headAccount", requestData.getExtendInfo().getHeadAccount());
            }

            if (StringUtils.isNotBlank(requestData.getExtendInfo().getRemitterName())) {
                if (extInforequestBean.getExtraParamsMap().isEmpty())
                    extInforequestBean.setExtraParamsMap(new HashMap<>());
                extInforequestBean.getExtraParamsMap().put("remitterName",
                        requestData.getExtendInfo().getRemitterName());
                extInforequestBean.getExtraParamsMap().put("bsrCode",
                        com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.BSR_CODE);
            }
        }

        setSubscriptionAutoRenewalParams(requestData, extInforequestBean, flowRequestBean);
        setExtendInfoForMLV(requestData, extInforequestBean);
        setExtendInfoForSubsLink(requestData, extInforequestBean);
        extInforequestBean.setPtInfo1(requestData.getRequestedTimeStamp());
        if (requestData.getExtraParamsMap() != null && requestData.getExtraParamsMap().get("challanIdNum") != null) {
            if (extInforequestBean.getExtraParamsMap().isEmpty())
                extInforequestBean.setExtraParamsMap(new HashMap<>());
            extInforequestBean.getExtraParamsMap().put("challanIdNum",
                    requestData.getExtraParamsMap().get("challanIdNum"));
        }
        extInforequestBean.setTxnToken(requestData.getTxnToken());
        extInforequestBean.setAoaSubsOnPgMid(requestData.isAoaSubsOnPgMid());
        // for HDFCDigiPOS Integration in extendInfo
        if (null != requestData.getLinkDetailsData()
                && null != requestData.getLinkDetailsData().getIsHDFCDigiPOSMerchant()) {
            extInforequestBean.setHDFCDigiPOSMerchant(requestData.getLinkDetailsData().getIsHDFCDigiPOSMerchant());
        }
        return extInforequestBean;
    }

    private void setExtendInfoForMLV(PaymentRequestBean requestData, ExtendedInfoRequestBean extInforequestBean) {

        String mid = requestData.getMid();
        String sso = requestData.getSsoToken();
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(sso)) {
            return;
        }
        String keyForMidSSO = nativeSessionUtil.createTokenForMidSSOFlow(sso, mid);
        String logoURL = (String) nativeSessionUtil.getField(keyForMidSSO, LOGO_URL);

        if (logoURL != null) {
            LOGGER.info("In ExtendInfo setting logoURL: {} ", logoURL);
        }

        extInforequestBean.setLogoURL(logoURL);
    }

    private void setExtendInfoForSubsLink(PaymentRequestBean requestData, ExtendedInfoRequestBean extInforequestBean) {

        if (requestData.getExtendInfo() != null && requestData.getExtendInfo().getSubsLinkInfo() != null) {
            try {
                extInforequestBean.setSubsLinkInfo(JsonMapper.mapObjectToJson(requestData.getExtendInfo()
                        .getSubsLinkInfo()));
            } catch (Exception e) {
                LOGGER.info("Could not cast Subscription Link Info to String");
            }
        }

    }

    private void setSubscriptionAutoRenewalParams(PaymentRequestBean requestData,
            ExtendedInfoRequestBean extInforequestBean, WorkFlowRequestBean workFlowRequestBean) {
        if (StringUtils.isNotEmpty(requestData.getAdditionalInfo())
                && StringUtils.isNotEmpty(requestData.getSubscriptionID())) {
            String[] additionalInfoKeyValList = requestData.getAdditionalInfo().split(
                    Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String additonalInfoKeyVal : additionalInfoKeyValList) {
                String[] keyVal = additonalInfoKeyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR));
                if (keyVal.length > 1) {
                    switch (keyVal[0]) {
                    case CommonConstant.AUTO_RENEWAL:
                        extInforequestBean.setAutoRenewal(Boolean.valueOf(keyVal[1]));
                        break;
                    case CommonConstant.AUTO_RETRY:
                        extInforequestBean.setAutoRetry(Boolean.valueOf(keyVal[1]));
                        break;
                    case CommonConstant.IS_COMMUNICATION_MANAGER:
                        extInforequestBean.setCommunicationManager(Boolean.valueOf(keyVal[1]));
                        break;
                    case CommonConstant.PRE_DEBIT_RENEWAL:
                        extInforequestBean.setPreDebitRenewal(Boolean.valueOf(keyVal[1]));
                        break;
                    case CommonConstant.IS_ORDER_ALREADY_CREATED:
                        extInforequestBean.setSubsRenewOrderAlreadyCreated(Boolean.valueOf(keyVal[1]));
                        break;
                    case CommonConstant.ACQUIREMENT_ID:
                        LOGGER.info("Acquirement id {} ", keyVal[1]);
                        workFlowRequestBean.setTransID(keyVal[1]);
                    case CommonConstant.SUBS_PAYMENT_TYPE:
                        LOGGER.info("SubsPaymentType {} ", keyVal[1]);
                        workFlowRequestBean.setSubsPaymentType(keyVal[1]);
                    case CommonConstant.INTERNAL_REQUEST:
                        LOGGER.info("Is Internal Request {} ", keyVal[1]);
                        workFlowRequestBean.setInternalRequestForSubsPayment(Boolean.valueOf(keyVal[1]));
                    }
                }
            }
        }
    }

    private void setExtendInfoForAdvanceDeposit(PaymentRequestBean requestData,
            ExtendedInfoRequestBean extendedInfoRequestBean) throws TheiaDataMappingException {
        try {
            com.paytm.pgplus.facade.payment.models.ChannelAccount channelAccount = nativeSessionUtil
                    .getAdvanceDepositDetails(requestData.getTxnToken());
            if (channelAccount == null) {
                TokenRequestHeader tokenRequestHeader = processTransactionUtil.getTokenRequestHeader(
                        requestData.getTxnToken(), requestData.getChannelId(), TheiaConstant.RequestHeaders.Version_V1);
                nativePaymentUtil
                        .fetchBalance(tokenRequestHeader, requestData.getPaymentTypeId(), requestData.getMid());
                channelAccount = nativeSessionUtil.getAdvanceDepositDetails(requestData.getTxnToken());
            }
            if (channelAccount != null && channelAccount.getExtendInfo() != null) {
                if (null != channelAccount.getExtendInfo().get(
                        TheiaConstant.ExtendedInfoChannelAccount.VIRTUAL_ACCOUNT_NO)) {
                    extendedInfoRequestBean.setVirtualAccountNo(channelAccount.getExtendInfo().get(
                            TheiaConstant.ExtendedInfoChannelAccount.VIRTUAL_ACCOUNT_NO));
                }
                if (null != channelAccount.getExtendInfo().get(TheiaConstant.ExtendedInfoChannelAccount.TEMPLATE_NAME)) {
                    extendedInfoRequestBean.setTemplateName(channelAccount.getExtendInfo().get(
                            TheiaConstant.ExtendedInfoChannelAccount.TEMPLATE_NAME));
                }
                if (null != channelAccount.getExtendInfo().get(TheiaConstant.ExtendedInfoChannelAccount.TEMPLATE_ID)) {
                    extendedInfoRequestBean.setTemplateId(channelAccount.getExtendInfo().get(
                            TheiaConstant.ExtendedInfoChannelAccount.TEMPLATE_ID));
                }
            }
        } catch (Exception e) {
            throw new TheiaDataMappingException(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    private void setMerchantClientIdIfnotEmpty(ExtendedInfoRequestBean extInforequestBean,
            PaymentRequestBean requestData) {
        if (StringUtils.isNotBlank(requestData.getClientId())) {
            LOGGER.info("Adding clientId {} in ExtendedInfo for mid {}", requestData.getClientId(),
                    requestData.getMid());
            extInforequestBean.setClientId(requestData.getClientId());
        }
    }

    private void populateOfflineAdditionalParam(final PaymentRequestBean requestData,
            final ExtendedInfoRequestBean extInforequestBean) throws TheiaDataMappingException {
        try {
            MerchantInfo merchantData = merchantQueryService.getMerchantProfileMappingData(requestData.getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantProfileMappingData :: {}", merchantData);
            extInforequestBean.setMerchantAddress(merchantData.getOfficeAddress());
            if (CollectionUtils.isNotEmpty(merchantData.getMccCodes())) {
                extInforequestBean.setMccCode(merchantData.getMccCodes().get(0));
            }
            if (null != merchantData.getContactMobileNo()
                    && StringUtils.isNotBlank(merchantData.getContactMobileNo().getMobileNo())) {
                extInforequestBean.setMerchantPhone(merchantData.getContactMobileNo().getMobileNo());
            } else {
                extInforequestBean.setMerchantPhone(merchantData.getOfficeTelephone());
            }
            extInforequestBean.setMerchantName(merchantData.getOfficialName());
        } catch (MappingServiceClientException e) {
            LOGGER.error("error while fetching merchant profile info for mid :{} ,error {}", requestData.getMid(), e);
            throw new TheiaDataMappingException();
        }
        QRCodeInfoResponseData qrCodeInfoResponse = getQRCodeInfoResponse(requestData);

        if (null != qrCodeInfoResponse) {
            extInforequestBean.setQrDisplayName(qrCodeInfoResponse.getName());
            extInforequestBean.setMerchantCategory(qrCodeInfoResponse.getCategory());
            extInforequestBean.setMerchantSubCategory(qrCodeInfoResponse.getSubCategory());
            if (StringUtils.isNotBlank(qrCodeInfoResponse.getMerchantContactNo())) {
                putUpdatedMerchantContactNo(extInforequestBean, qrCodeInfoResponse.getMerchantContactNo());
            } else {
                LOGGER.info("Updated Contact Not found in qrCodeInfoResponse");
            }
            if (StringUtils.isBlank(requestData.getPosId()) && StringUtils.isNotBlank(qrCodeInfoResponse.getPosId())) {
                requestData.setPosId(qrCodeInfoResponse.getPosId());
            }
            // To support subwallet in Dynamir QR flow
            if (StringUtils.isNotBlank(qrCodeInfoResponse.getSubwalletWithdrawMaxAmountDetails())
                    && requestData.getSubwalletAmount() == null) {
                requestData.setSubwalletAmount(qrCodeInfoResponse.getSubwalletWithdrawMaxAmountDetails());
            }
            if (StringUtils.isNotBlank(qrCodeInfoResponse.getIsEdcRequest())) {
                saveIsEdcRequest(qrCodeInfoResponse.getIsEdcRequest(), requestData);
            }
            if (StringUtils.isNotBlank(qrCodeInfoResponse.getTxnAmount())) {
                requestData.setQrTxnAmountInRupees(qrCodeInfoResponse.getTxnAmount());
            }
        }
    }

    private void saveIsEdcRequest(String isEdcRequest, PaymentRequestBean requestData) {
        if (StringUtils.isBlank(requestData.getMid()) || StringUtils.isBlank(requestData.getOrderId())
                || StringUtils.isBlank(requestData.getQrCodeId())) {
            return;
        }

        String token = requestData.getMid() + requestData.getOrderId() + requestData.getQrCodeId();
        LOGGER.info("setting isEDCRequest= {} for key= {} in session cache", isEdcRequest, token);
        nativeSessionUtil.setIsEdcRequest(token, isEdcRequest);
    }

    private QRCodeInfoResponseData getQRCodeInfoResponse(final PaymentRequestBean requestData) {
        String qrCodeId = null;
        if (StringUtils.isNotBlank(requestData.getAdditionalInfo())) {
            String[] additionalInfoKeyValArray = requestData.getAdditionalInfo().split(
                    Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String keyVal : additionalInfoKeyValArray) {
                if (keyVal.contains("qr_code_id") || keyVal.contains("qrCodeId")) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2) {
                        qrCodeId = keyValSplit[1].trim();
                    }
                }

                if (keyVal.contains(POS_ID)) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2) {
                        if (!StringUtils.isBlank(keyValSplit[1])) {
                            LOGGER.info("MERCHANT_POS_ID<{}>", keyValSplit[1].trim());
                        }
                    }
                }
            }
            requestData.setQrCodeId(qrCodeId);
        }

        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(requestData.getMid());
        boolean isDynamicFeeMerchant = merchantPreferenceService.isDynamicFeeMerchant(requestData.getMid());
        boolean isPostConvenience = false;
        if (merchantPreferenceService.isPostConvenienceFeesEnabled(requestData.getMid()) || isSlabBasedMdr
                || isDynamicFeeMerchant) {
            isPostConvenience = true;
        }
        if (StringUtils.isNotBlank(qrCodeId) && StringUtils.isBlank(requestData.getTipAmount())) {
            QRCodeInfoBaseResponse qrCodeResponse = walletQRCodeService.getQRCodeInfoByQrCodeId(qrCodeId);

            if (ProcessTransactionConstant.FAILURE.equals(qrCodeResponse.getStatus())
                    && ResponseCodeConstant.QR_EXPIRY_RESPONSE_CODE.equals(qrCodeResponse.getStatusCode())) {
                throw new PaymentRequestQrException(ResultCode.QR_EXPIRED, ResponseConstants.QR_EXPIRED_ERROR);
            }

            LOGGER.info("QRCode found details for qrCodeId : {}", qrCodeId);
            if (qrCodeResponse.getResponse() != null
                    && StringUtils.isNotBlank(qrCodeResponse.getResponse().getTxnAmount())
                    && !Double.valueOf(qrCodeResponse.getResponse().getTxnAmount()).equals(
                            Double.valueOf(requestData.getTxnAmount()))) {
                if (!(requestData.isQRIdFlowOnly() && isPostConvenience)) {
                    Map<String, String> metaData = new LinkedHashMap<>();
                    metaData.put(TXN_AMOUNT_FROM_QR_SERVICE, qrCodeResponse.getResponse().getTxnAmount());
                    metaData.put(TXN_AMOUNT_IN_REQUEST, requestData.getTxnAmount());
                    EventUtils.pushTheiaEvents(EventNameEnum.AMOUNT_MISMATCH, metaData);
                    if (com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                            TheiaConstant.PaytmPropertyConstants.QR_AMOUNT_MISMATCH_SECURITY_FIX_ENABLE, "false")
                            .equalsIgnoreCase("true")) {
                        throw new TheiaServiceException(
                                "Amount Mismatch  TXN_AMOUNT_FROM_QR_SERVICE and TXN_AMOUNT_IN_REQUEST");
                    }
                }
            }
            return qrCodeResponse.getResponse();
        }

        return null;
    }

    private void putUpdatedMerchantContactNo(ExtendedInfoRequestBean extendedInfoRequestBean, String merchantContactNo) {
        String additionalInfo = extendedInfoRequestBean.getAdditionalInfo();
        LOGGER.info("received additionalInfo {} for OFFLINE request ", additionalInfo);
        int indexOfMerchantContactNo = additionalInfo.indexOf("merchantContactNo");
        if (indexOfMerchantContactNo != -1) {
            int indexOfPipe = additionalInfo.indexOf("|", indexOfMerchantContactNo);
            indexOfPipe = indexOfPipe == -1 ? additionalInfo.length() : indexOfPipe;
            String updateAdditionalInfo = additionalInfo.substring(0, indexOfMerchantContactNo) + "merchantContactNo:"
                    + merchantContactNo + additionalInfo.substring(indexOfPipe);
            LOGGER.info("updated additionalInfo {} for OFFLINE request ", updateAdditionalInfo);
            extendedInfoRequestBean.setAdditionalInfo(updateAdditionalInfo);
        } else {
            LOGGER.info("merchantContactNo not found in additionalInfo for offline");
        }
    }

    private boolean setCallbackUrlForPreAuth(final PaymentRequestBean requestData,
            final ExtendedInfoRequestBean extInforequestBean) {
        extInforequestBean.setCallBackURL(requestData.getCallbackUrl());

        final boolean isPeonEnable = isCallbackRequired(requestData);

        if (StringUtils.isNotBlank(requestData.getPeonURL()) && isPeonEnable) {
            extInforequestBean.setPeonURL(requestData.getPeonURL());
        }
        if (StringUtils.isBlank(requestData.getCallbackUrl())
                || (StringUtils.isBlank(requestData.getPeonURL()) && isPeonEnable)) {

            final MerchantUrlInput input = new MerchantUrlInput(requestData.getMid(),
                    MappingMerchantUrlInfo.UrlTypeId.PAY_CONFIRM, "PAYTM");
            MappingMerchantUrlInfo merchantUrlInfo;
            try {
                merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
            } catch (PaymentRequestValidationException e) {
                LOGGER.info("Pay Confirm Callback or Peon Not Configured Against website PAYTM");
                return false;
            }
            if (StringUtils.isBlank(requestData.getCallbackUrl()) && merchantUrlInfo != null) {
                validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, false, true);
            }

            if (StringUtils.isBlank(requestData.getPeonURL()) && merchantUrlInfo != null) {
                validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, true, false);
            }
        }
        return true;
    }

    private void setCallbackUrls(final PaymentRequestBean requestData,
            final ExtendedInfoRequestBean extInforequestBean, WorkFlowRequestBean workFlowRequestBean) {
        final String callbackUrl = requestData.getCallbackUrl();
        extInforequestBean.setCallBackURL(callbackUrl);

        extInforequestBean.setSuccessCallBackURL(requestData.getSuccessCallbackUrl());
        extInforequestBean.setFailureCallBackURL(requestData.getFailureCallbackUrl());
        extInforequestBean.setPendingCallBackURL(requestData.getPendingCallbackUrl());

        final boolean isPeonEnable = isCallbackRequired(requestData);

        if (StringUtils.isNotBlank(requestData.getPeonURL()) && isPeonEnable) {
            extInforequestBean.setPeonURL(requestData.getPeonURL());
        }

        if (StringUtils.isBlank(callbackUrl) || (StringUtils.isBlank(requestData.getPeonURL()) && isPeonEnable)) {

            // Need to fetch website from session
            if (StringUtils.isBlank(requestData.getWebsite()) && requestData.isSessionRequired()) {
                TransactionInfo transInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());
                if ((transInfo != null) && StringUtils.isNotBlank(transInfo.getWebsite())) {
                    requestData.setWebsite(transInfo.getWebsite());
                }
            }

            if (StringUtils.isNotBlank(requestData.getWebsite())
                    || (ERequestType.SEAMLESS_3D_FORM.name().equals(requestData.getRequestType()))) {

                String website = StringUtils.isBlank(requestData.getWebsite()) ? TheiaConstant.ExtraConstants.DEFAULT_WEBSITE
                        : requestData.getWebsite();

                requestData.setWebsite(website);

                final MerchantUrlInput input = new MerchantUrlInput(requestData.getMid(),
                        MappingMerchantUrlInfo.UrlTypeId.RESPONSE, requestData.getWebsite());
                MappingMerchantUrlInfo merchantUrlInfo;
                try {
                    merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                } catch (PaymentRequestValidationException e) {
                    // JIRA ID:10940 - now in case default website is not
                    // configured on merchant
                    // we will still process the request .
                    if (ERequestType.SEAMLESS_3D_FORM.name().equals(requestData.getRequestType())
                            && TheiaConstant.ExtraConstants.DEFAULT_WEBSITE.equals(requestData.getWebsite())) {
                        return;
                    }
                    if (ERequestType.DYNAMIC_QR.getType().equals(requestData.getRequestType())) {
                        Map<String, String> metadata = new HashedMap();
                        metadata.put("GOT WEBSITE", workFlowRequestBean.getWebsite());
                        metadata.put("PEON ENABLE", String.valueOf(isPeonEnable));
                        EventUtils.pushTheiaEvents(EventNameEnum.WRONG_WEBSITE_DYNAMIC_QR, metadata);
                        if (!isPeonEnable) {
                            return;
                        }
                    }
                    throw e;
                }

                if (requestData.isOfflineFastForwardRequest()) {

                    if ((merchantUrlInfo == null)
                            || !MappingMerchantUrlInfo.Status.ACTIVE.equals(merchantUrlInfo.getStatus())) {
                        return;
                    }

                    if (StringUtils.isBlank(callbackUrl)) {
                        validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, false, true);
                    }
                    if (StringUtils.isBlank(requestData.getPeonURL())) {
                        validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, true, false);
                    }

                } else {
                    if ((merchantUrlInfo == null)
                            || !MappingMerchantUrlInfo.Status.ACTIVE.equals(merchantUrlInfo.getStatus())
                            || StringUtils.isBlank(merchantUrlInfo.getPostBackurl())) {

                        if (isInvoiceFirstRequest(requestData) || isPaymentBrowserRequest(requestData)) {
                            throw new TheiaServiceException("Website of merchant is not mapped");
                        }

                        return;
                    }
                    if (StringUtils.isBlank(callbackUrl)) {
                        validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, false, true);
                    }

                    if (StringUtils.isBlank(requestData.getPeonURL())) {
                        validateAndSetMerchantUrls(merchantUrlInfo, requestData, extInforequestBean, true, false);
                    }

                }

            }
        }

        if (isAggregatorMid(requestData)) {
            extInforequestBean.setPeonURL(requestData.getPeonURL());
        }
    }

    private boolean isAggregatorMid(PaymentRequestBean requestBean) {
        if (requestBean.getAggMid() != null
                && (AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(requestBean.getAggMid()) || AggregatorMidKeyUtil
                        .isMidEnabledForMLVAggregatorMid(requestBean.getAggMid()))) {
            return true;
        }
        return false;
    }

    private void validateAndSetMerchantUrls(final MappingMerchantUrlInfo merchantUrlInfo,
            final PaymentRequestBean requestData, final ExtendedInfoRequestBean extInfoRequestBean,
            boolean isPeonURLNeedToBeSent, boolean isCallBackNeedToBeSent) {

        if (isCallBackNeedToBeSent) {
            LOGGER.info("Empty callback URL received in request so setting the same");
            extInfoRequestBean.setCallBackURL(merchantUrlInfo.getPostBackurl());
        }

        if (isPeonURLNeedToBeSent) {
            LOGGER.info("Empty peon URL received in request so setting the same");
            extInfoRequestBean.setPeonURL(merchantUrlInfo.getNotificationStatusUrl());
        }
    }

    private boolean isInvoiceFirstRequest(final PaymentRequestBean requestData) {
        return requestData.getRequest().getServletPath().equals("/createInvoice");
    }

    private boolean isPaymentBrowserRequest(final PaymentRequestBean requestBean) {
        return requestBean.getRequest().getServletPath().equals("/processTransaction")
                && !(requestBean.getRequestType().equals(TheiaConstant.RequestTypes.EMAIL_INVOICE)
                        || requestBean.getRequestType().equals(TheiaConstant.RequestTypes.SMS_INVOICE)
                        || requestBean.getRequestType().equals(TheiaConstant.RequestTypes.RENEW_SUBSCRIPTION) || requestBean
                        .getRequestType().equals(RequestTypes.PARTIAL_RENEW_SUBSCRIPTION));
    }

    private boolean isCallbackRequired(final PaymentRequestBean paymentRequestBean) {
        return merchantExtendInfoProvider.isPeonEnabled(paymentRequestBean)
                || merchantExtendInfoProvider.isCallbackEnabled(paymentRequestBean)
                || merchantPreferenceService.isCustomTransactionWebhookUrlEnabled(paymentRequestBean.getMid());
    }

    public void setPaymentModeFilters(final PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean) {
        flowRequestBean.setPayModeOnly(requestData.getPaymentModeOnly());

        if (ERequestType.RESELLER.name().equals(requestData.getRequestType())) {
            List<String> allowedPayModes = new ArrayList<>();
            allowedPayModes.add(EPayMethod.PPBL.getOldName());
            flowRequestBean.setAllowedPaymentModes(allowedPayModes);
            return;
        }

        if (StringUtils.isNotBlank(requestData.getPaymentTypeId())
                && TheiaConstant.ExtraConstants.YES_STRING.equals(flowRequestBean.getPayModeOnly())) {
            final String[] payModeEnable = requestData.getPaymentTypeId().split(",");
            flowRequestBean.setAllowedPaymentModes(MapperUtils.mapOldPayModesToNew(payModeEnable));
        }

        if (StringUtils.isNotBlank(requestData.getDisabledPaymentMode())) {
            final String[] payModeDisable = requestData.getDisabledPaymentMode().split(",");
            flowRequestBean.setDisabledPaymentModes(MapperUtils.mapOldPayModesToNew(payModeDisable));
        }

        if (flowRequestBean.isZeroCostEmi()) {
            List<String> disAllowedPaymentModes = flowRequestBean.getDisabledPaymentModes();
            if (CollectionUtils.isEmpty(disAllowedPaymentModes)) {
                disAllowedPaymentModes = new ArrayList<String>();
                disAllowedPaymentModes.add(EPayMethod.BALANCE.toString());
                disAllowedPaymentModes.add(EPayMethod.PPBL.toString());
                flowRequestBean.setDisabledPaymentModes(disAllowedPaymentModes);
            }
        }

        if (StringUtils.isBlank(requestData.getPaymentTypeId())
                || !requestData.getPaymentTypeId().contains(PayMethod.MP_COD.getOldName().toString())) {
            List<String> disabledOptions = flowRequestBean.getDisabledPaymentModes();

            if (disabledOptions.isEmpty()) {
                disabledOptions = new ArrayList<>();
            }
            if (!ERequestType.NATIVE_PAY.getType().equals(requestData.getRequestType())
                    && !ERequestType.NATIVE.getType().equals(requestData.getRequestType())) {
                disabledOptions.add(PayMethod.MP_COD.toString());
            }
            flowRequestBean.setDisabledPaymentModes(disabledOptions);
        }

    }

    @Override
    public void mapWorkFlowResponseToSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {
        buildMandatorySessiondata(requestData, responseData);
        merchantInfoSessionUtil.setMerchantInfoIntoSession(requestData, responseData);
        oAuthInfoSessionUtil.setOAuthInfo(requestData, responseData);
        if (StringUtils.isNotEmpty(responseData.getTransID()))
            transactionInfoSessionUtil.setTransactionInfoIntoSession(requestData, responseData);
        transactionConfigInfoSessionUtil.setTransactionConfigIntoSession(requestData, responseData);
        digitalCreditInfoSessionUtil.setDigitalCreditInfoInSession(requestData, responseData);
        paymentsBankAccountSessionUtil.setPaymentsBankAccountInfoInSession(requestData, responseData);
        sarvtraVPASessionUtil.setSarvatraVpaInfoInSession(requestData, responseData);
    }

    @Override
    public void mapPostLoginResponseToSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {
        buildMandatorySessiondata(requestData, responseData);
        transactionInfoSessionUtil.setTransactionInfoIntoSessionForPostLogin(requestData, responseData);
        transactionConfigInfoSessionUtil.setTransactionConfigIntoSessionForPostLogin(requestData, responseData);
        oAuthInfoSessionUtil.setPostLoginOAuthInfoToSession(requestData, responseData);
        digitalCreditInfoSessionUtil.setDigitalCreditInfoInSession(requestData, responseData);
        paymentsBankAccountSessionUtil.setPaymentsBankAccountInfoInSession(requestData, responseData);
    }

    private void buildMandatorySessiondata(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        loginInfoSessionUtil.setLoginInfoIntoSession(requestData, responseData);
        cardInfoSessionUtil.setCardInfoIntoSession(requestData, responseData);
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseData);
        entityPaymentOptionSessionUtil.setEntityPaymentOption(requestData, responseData);
    }

    /**
     * @param requestData
     * @param flowRequestBean
     */
    private void buildPayMethodAndPayOption(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) {
        if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.PPI.value)) {
            flowRequestBean.setPayMethod(ExtraConstants.WALLET_TYPE);
            flowRequestBean.setPayOption(ExtraConstants.WALLET_TYPE);
        } else if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.WALLET.value)) {
            flowRequestBean.setPayMethod(EPayMethod.WALLET.getMethod());
            flowRequestBean.setPayOption(requestData.getWalletType());
        } else if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)) {
            flowRequestBean.setPayMethod(flowRequestBean.getCardType());
            flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_" + flowRequestBean.getCardScheme());
        } else if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {
            flowRequestBean.setPayMethod(ExtraConstants.IMPS);
            flowRequestBean.setPayOption(ExtraConstants.IMPS);
        } else if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
            flowRequestBean.setPayMethod(ExtraConstants.NET_BANKING);
            flowRequestBean.setPayOption(ExtraConstants.NET_BANKING + "_" + flowRequestBean.getBankCode());
        } else if (requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            flowRequestBean.setPayMethod(ExtraConstants.UPI);
            flowRequestBean.setPayOption(ExtraConstants.UPI);
        } else if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(ExtraConstants.PAYTM_DIGITAL_CREDIT);
            flowRequestBean.setPayOption(ExtraConstants.PAYTM_DIGITAL_CREDIT);
        } else if (PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value.equals(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(ExtraConstants.ADVANCE_DEPOSIT_ACCOUNT);
            flowRequestBean.setPayOption(ExtraConstants.ADVANCE_DEPOSIT_ACCOUNT);
        } else if (PaymentTypeIdEnum.BANK_TRANSFER.value.equals(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(BANK_TRANSFER);
            flowRequestBean.setPayOption(BANK_TRANSFER);
        } else if (PaymentTypeIdEnum.EMI.value.equals(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(ExtraConstants.EMI);
            if (StringUtils.isNotBlank(flowRequestBean.getCardScheme())
                    && flowRequestBean.getCardScheme().equals(AMEX.name())) {
                flowRequestBean.setPayOption(ExtraConstants.EMI.concat("_").concat(AMEX.name()));
            } else {
                flowRequestBean.setPayOption(ExtraConstants.EMI.concat("_").concat(requestData.getBankCode()));
            }
            if (StringUtils.isNotBlank(requestData.getEmitype())
                    && requestData.getEmitype().equals(EmiType.DEBIT_CARD.getType())) {
                flowRequestBean.setPayMethod(ExtraConstants.EMI_DC);
                flowRequestBean.setPayOption(ExtraConstants.EMI_DC.concat("_").concat(requestData.getBankCode()));
            }
        } else if (PaymentTypeIdEnum.COD.value.equalsIgnoreCase(requestData.getPaymentTypeId())
                || ExtraConstants.MP_COD.equalsIgnoreCase(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(ExtraConstants.MP_COD);
            flowRequestBean.setPayOption(TheiaConstant.BasicPayOption.MP_COD);
        } else if (PaymentTypeIdEnum.GIFT_VOUCHER.value.equalsIgnoreCase(requestData.getPaymentTypeId())) {
            flowRequestBean.setPayMethod(ExtraConstants.GIFT_VOUCHER);
            flowRequestBean.setPayOption(ExtraConstants.GIFT_VOUCHER);
        }
    }

    /**
     * @param requestData
     * @param flowRequestBean
     */
    private void buildSeamlessRequestData(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }

        if (requestData.getRequestType().equals(ERequestType.SEAMLESS.getType())
                || requestData.getRequestType().equals(ERequestType.SEAMLESS_NATIVE.getType())
                || requestData.getRequestType().equals(ERequestType.STOCK_TRADE.getType())
                || requestData.getRequestType().equals(ERequestType.SEAMLESS_NB.getType())) {
            buildSeamlessRequestDataForPGOnlyOptions(requestData, flowRequestBean);
            flowRequestBean.setDirectPayModeType(requestData.getDirectPayModeType());
        }

        if (requestData.getRequestType().equals(ERequestType.SEAMLESS_ACS.getType())
                || ERequestType.SEAMLESS_3D_FORM.getType().equals(requestData.getRequestType())) {

            // Validating seamless payment details

            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
            flowRequestBean.setBankCode(requestData.getBankCode());
            if (StringUtils.isBlank(flowRequestBean.getPaymentTypeId())) {
                throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENTMODE);
            }
            if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
                String bankCode = StringUtils.isNotBlank(flowRequestBean.getBankCode()) ? flowRequestBean.getBankCode()
                        : flowRequestBean.getPaymentDetails();
                if (StringUtils.isBlank(bankCode)) {
                    throw new TheiaServiceException("Invalid Seamless Payment details");
                }

                flowRequestBean.setBankCode(bankCode);
                flowRequestBean.setPaymentDetails(bankCode);
            } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
                if (StringUtils.isBlank(flowRequestBean.getPaymentDetails())) {
                    throw new TheiaServiceException("Invalid Seamless Payment details");
                }
                flowRequestBean.setVirtualPaymentAddress(requestData.getPaymentDetails());
                flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
            } else {
                try {
                    validationService.validateAndProcessSeamlessPaymentDetails(flowRequestBean.getPaymentDetails(),
                            requestData.getMid(), flowRequestBean.getIsSavedCard(), flowRequestBean, requestData);
                } catch (PaymentRequestValidationException e) {
                    throw new TheiaDataMappingException(e, e.getResponseConstants());
                }

            }

            setBinDetails(flowRequestBean);
            // Setting payMethod & payOption
            buildPayMethodAndPayOption(requestData, flowRequestBean);

        }
    }

    private void buildAddMoneyExpressRequestData(PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }
        if (requestData.getRequestType().equals(ERequestType.ADDMONEY_EXPRESS.getType())) {

            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
            flowRequestBean.setBankCode(requestData.getBankCode());
            if (StringUtils.isBlank(flowRequestBean.getPaymentTypeId())) {
                throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENTMODE);
            }
            if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                    || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)) {
                try {
                    if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.getIsSavedCard())) {
                        flowRequestBean.setIsSavedCard(true);
                    } else if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_0.equals(requestData.getIsSavedCard())) {
                        flowRequestBean.setIsSavedCard(false);
                    }
                    validationService.validateAndProcessAddMoneyExpressPaymentDetails(flowRequestBean, requestData);
                } catch (PaymentRequestValidationException ex) {
                    throw new TheiaDataMappingException(ex, ex.getResponseConstants());
                }
            }

            setBinDetails(flowRequestBean);

            buildPayMethodAndPayOption(requestData, flowRequestBean);
        }
    }

    private void buildPaytmExpressRequestData(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }

        if (ERequestType.PAYTM_EXPRESS.getType().equals(requestData.getRequestType())
                || ERequestType.TOPUP_EXPRESS.getType().equals(requestData.getRequestType())) {

            buildPaytmExpressPaymentDetails(requestData, flowRequestBean);
            validateAndSetWalletAmount(requestData, flowRequestBean);

            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());

            if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.getIsSavedCard())) {
                flowRequestBean.setIsSavedCard(true);
            }

            setAddOrHybridOption(requestData, flowRequestBean);

            if (!EPayMode.NONE.equals(flowRequestBean.getPaytmExpressAddOrHybrid())) {
                flowRequestBean.setPaytmToken(requestData.getPaytmToken());
            }
            setBinDetails(flowRequestBean);
            buildPayMethodAndPayOption(requestData, flowRequestBean);

        }

        // Build EMI data for paytmExpress
        if (requestData.getRequestType().equals(ERequestType.PAYTM_EXPRESS.getType())
                && EMIPaymentHelper.isEmi(requestData.getPaymentTypeId()).equals("Y"))
            try {
                emiPayMethodbuilder.setObj(new EMIPayMethodDTO(requestData, flowRequestBean)).build();
            } catch (RequestValidationException | PaymentRequestProcessingException e) {
                throw new TheiaDataMappingException("Invalid Request Parameter Found",
                        ResponseConstants.EMI_PROCESSING_FAILED);
            }
    }

    private void buildPaytmOfflineRequestData(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isOfflineFastForwardRequest()) {
            flowRequestBean.setOfflineFastForwardRequest(true);
        }

        if ((ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(requestData.getRequestType()) || ERequestType
                .isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(requestData.getSubRequestType()))
                && StringUtils.isNotBlank(requestData.getTxnToken())) {
            flowRequestBean.setTxnToken(requestData.getTxnToken());
            flowRequestBean.setNativeClientId(configurationDataService
                    .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID));
            flowRequestBean.setNativeSecretKey(configurationDataService
                    .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY));

        }
        if (StringUtils.isBlank(flowRequestBean.getTxnToken())
                && (ERequestType.DYNAMIC_QR.getType().equals(requestData.getRequestType()) || ERequestType.DYNAMIC_QR_2FA
                        .getType().equals(requestData.getRequestType()))) {
            flowRequestBean.setTxnToken(requestData.getMid() + requestData.getOrderId());
        }
        flowRequestBean.setOfflineFetchPayApi(requestData.isOfflineFetchPayApi());
        String requestType = requestData.getRequestType();

        if (!flowRequestBean.isEcomTokenTxn()
                && ((ERequestType.isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(ERequestType
                        .getByRequestType(requestData.getRequestType())) || (ERequestType
                        .isOfflineOrNativeOrUniOrNativeMFOrNativeSTRequest(ERequestType.getByRequestType(requestData
                                .getSubRequestType())))) && !isEnahancedAOARequest(requestData))
                || ERequestType.isSubscriptionCreationRequest(requestType)
                || ERequestType.DYNAMIC_QR.getType().equals(requestType)
                || ERequestType.DYNAMIC_QR_2FA.getType().equals(requestType)
                || ERequestType.LINK_BASED_PAYMENT.getType().equals(requestType)
                || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equals(requestType)
                || (ERequestType.SEAMLESS.getType().equals(requestType)
                        && requestData.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value) && TRUE
                            .equals(requestData.getFromAOARequest()))) {

            buildSeamlessRequestDataForPGOnlyOptions(requestData, flowRequestBean);
            validateAndSetWalletAmount(requestData, flowRequestBean);

            if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.getIsSavedCard())) {
                flowRequestBean.setIsSavedCard(true);
            }

            setAddOrHybridOption(requestData, flowRequestBean);
            buildEMIData(requestData, flowRequestBean);
        }
    }

    private boolean isEnahancedAOARequest(PaymentRequestBean requestData) {
        return (requestData.isEnhancedCashierPageRequest() && ERequestType.UNI_PAY == ERequestType
                .getByRequestType(requestData.getRequestType()));
    }

    private void buildEMIData(final PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean) {
        if ((ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                || ERequestType.DYNAMIC_QR.getType().equals(requestData.getRequestType()) || ERequestType.UNI_PAY
                .getType().equals(requestData.getRequestType())) && requestData.getIsEmi()) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(requestData
                    .getTxnToken());
            if (null != cashierInfoResponse) {
                try {
                    validationService.validateSeamlessEMIPaymentRequestNative(requestData, flowRequestBean,
                            cashierInfoResponse);
                } catch (TheiaServiceException exception) {
                    ResultInfo resultInfo = new ResultInfo();
                    resultInfo.setRedirect(false);
                    resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
                    resultInfo.setResultMsg("Please enter a valid card number to use this EMI plan");
                    throw new PassCodeValidationException(resultInfo);
                }

                if (flowRequestBean.getChannelInfo() == null) {
                    flowRequestBean.setChannelInfo(new HashMap<>());
                }
                seamlessEMIPaymentHelper.buildSeamlessEMIPaymentRequestNative(flowRequestBean.getChannelInfo(),
                        requestData, cashierInfoResponse);
            }
            return;
        }

        if (ERequestType.isNativeOrUniRequest(ERequestType.valueOf(requestData.getRequestType()))
                && requestData.getIsEmi()) {
            EntityPaymentOptionsTO entityPaymentOptions = theiaSessionDataServiceAdapterNative
                    .getEntityPaymentOptions(requestData.getRequest());
            validationService.validateSeamlessEMIPaymentRequest(requestData, flowRequestBean, entityPaymentOptions);
            if (flowRequestBean.getChannelInfo() == null) {
                flowRequestBean.setChannelInfo(new HashMap<String, String>());
            }
            seamlessEMIPaymentHelper.builSeamlessEMIPaymentRequest(flowRequestBean.getChannelInfo(), requestData,
                    entityPaymentOptions);
        }

        if (ERequestType.SEAMLESS.getType().equals(requestData.getRequestType())
                && TheiaConstant.ExtraConstants.EMI.equals(requestData.getPaymentTypeId())) {
            seamlessEMIPaymentHelper.buildSeamlessEMIPaymentRequest(flowRequestBean.getChannelInfo(), requestData);
        }
    }

    private void buildSeamlessRequestDataForPGOnlyOptions(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {
        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }
        boolean isBinDetailFetched = false;
        // Validating seamless payment details
        flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());
        flowRequestBean.setBankCode(requestData.getBankCode());
        flowRequestBean.setEncryptedCardDetail(requestData.isEncryptedCardDetail());

        if (StringUtils.isBlank(flowRequestBean.getPaymentTypeId())) {
            throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENTMODE);
        }
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.NB.value)) {
            // Needs to check PPBL in bankcode and paymentdetails because of
            // existing contract
            if (EPayMethod.PPBL.getOldName().equals(flowRequestBean.getBankCode())
                    || EPayMethod.PPBL.getOldName().equals(flowRequestBean.getPaymentDetails())) {
                String bankCode = flowRequestBean.getBankCode();
                // If user didn't send bank code in bankCode field it means he
                // sent it in payment details
                // And we expect accessToken in payment details hence exception
                if (StringUtils.isBlank(bankCode)) {
                    LOGGER.error("In valid accessToken or bankcode");
                    throw new TheiaServiceException("Invalid Offline Payment details");
                }

            } else {
                String bankCode = StringUtils.isNotBlank(flowRequestBean.getBankCode()) ? flowRequestBean.getBankCode()
                        : flowRequestBean.getPaymentDetails();
                if (StringUtils.isBlank(bankCode)) {
                    throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENT_DETAILS);
                }

                flowRequestBean.setBankCode(bankCode);
                flowRequestBean.setBankName(requestData.getBankName());
                flowRequestBean.setPaymentDetails(bankCode);
            }
        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            if (StringUtils.isBlank(flowRequestBean.getPaymentDetails())) {
                throw new TheiaServiceException("Invalid Seamless Payment details");
            }
            flowRequestBean.setVirtualPaymentAddress(requestData.getPaymentDetails());
            flowRequestBean.setPaymentDetails(requestData.getPaymentDetails());

            // checking Upi Push transaction
            if (StringUtils.isNotBlank(requestData.getMpin()) || requestData.getUpiLiteRequestData() != null) {
                flowRequestBean.setUpiPushFlow(true);

                LOGGER.info("Initiating Upi Push transaction");
                if (StringUtils.isNotBlank(requestData.getUpiAccRefId())) {
                    LOGGER.info("Setting AccRefId for UPI PUSH Flow");
                    flowRequestBean.setUpiAccRefId(requestData.getUpiAccRefId());
                    flowRequestBean.setMpin(requestData.getMpin());
                    flowRequestBean.setBankName(requestData.getBankName());// need
                    // to
                    // be
                    // confirmed
                    flowRequestBean.setCreditBlock(requestData.getCreditBlock());
                    flowRequestBean.setSeqNo(requestData.getSeqNo());
                    flowRequestBean.setTxnToken(requestData.getTxnToken());
                    flowRequestBean.setAppId(requestData.getAppId());
                    flowRequestBean.setDeviceId(requestData.getDeviceId());

                } else if (StringUtils.isNotBlank(requestData.getAccountNumber())
                        && StringUtils.isNotBlank(requestData.getBankName())
                        && StringUtils.isNotBlank(requestData.getCreditBlock())
                        && StringUtils.isNotBlank(requestData.getSeqNo())) {
                    SarvatraVpaDetails sarvatraVPADetails = getSarvatraVPADetails(requestData);
                    flowRequestBean.setAccountNumber(getAccountNumber(sarvatraVPADetails));
                    flowRequestBean.setIfsc(getIfsc(sarvatraVPADetails));
                    flowRequestBean.setMpin(requestData.getMpin());
                    flowRequestBean.setBankName(requestData.getBankName());
                    flowRequestBean.setCreditBlock(requestData.getCreditBlock());
                    flowRequestBean.setSeqNo(requestData.getSeqNo());
                    flowRequestBean.setTxnToken(requestData.getTxnToken());
                    flowRequestBean.setAppId(requestData.getAppId());

                } else {
                    LOGGER.error("Data Invalid for Upi Push flow");
                    throw new TheiaServiceException("Insufficient data for Upi Push flow");
                }
            }
        } else if (PaymentTypeIdEnum.PAYTM_DIGITAL_CREDIT.value.equals(flowRequestBean.getPaymentTypeId())) {
            boolean isValidPaymentDetails = false;
            if (StringUtils.isNotEmpty(flowRequestBean.getPaymentDetails())) {
                String[] paymentDetailsArr = flowRequestBean.getPaymentDetails().split(Pattern.quote("|"));

                if (paymentDetailsArr.length == 2 && StringUtils.isNotEmpty(paymentDetailsArr[0])
                        && StringUtils.isNotEmpty(paymentDetailsArr[1])) {
                    isValidPaymentDetails = true;
                } else if (paymentDetailsArr.length == 3 && StringUtils.isNotEmpty(paymentDetailsArr[0])
                        && StringUtils.isNotEmpty(paymentDetailsArr[1]) && StringUtils.isNotEmpty(paymentDetailsArr[2])) {
                    isValidPaymentDetails = true;
                }

            }
            if (!isValidPaymentDetails) {
                throw new TheiaServiceException("Invalid Offline Payment details for digital credit");
            }

        } else if (PaymentTypeIdEnum.GIFT_VOUCHER.value.equals(flowRequestBean.getPaymentTypeId())) {
            if (StringUtils.isBlank(requestData.getTemplateId())) {
                throw new TheiaServiceException("Invalid Offline Payment details for MGV");
            }
            // For phase 1 only one templateId is allowed , kept as list to
            // support later phases
            List<String> templateIds = new ArrayList<>();
            templateIds.add(requestData.getTemplateId());
            flowRequestBean.setMgvTemplateIds(templateIds);
        } else if (PaymentTypeIdEnum.BANK_TRANSFER.value.equals(flowRequestBean.getPaymentTypeId())) {
            /**
             * No specific Valdation for Bank Transfer
             */
            LOGGER.info("Nothing specific required for Bank Transfer");
        } else if (ff4JUtils.isCOFTEnabledOnAOA(requestData.getMid())
                && ERequestType.SEAMLESS.getType().equals(requestData.getRequestType()) && requestData.isCoftTokenTxn()) {
            CardTokenInfo cardTokenInfo = requestData.getCardTokenInfo();
            // Setting bin details initially for COFT Txns to perform validation
            // based on cardScheme.
            setBinDetails(flowRequestBean);
            isBinDetailFetched = true;
            if (StringUtils.isBlank(cardTokenInfo.getTokenExpiry())
                    || BooleanUtils.isFalse(processTransactionUtil.isValidCardTokenInfo(cardTokenInfo,
                            flowRequestBean.getCardScheme()))) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isHTMLResponse(false).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
        } else {
            try {
                validationService.validateAndProcessSeamlessPaymentDetails(flowRequestBean.getPaymentDetails(),
                        requestData.getMid(), flowRequestBean.getIsSavedCard(), flowRequestBean, requestData);
            } catch (PaymentRequestValidationException e) {
                throw new TheiaDataMappingException(e, e.getResponseConstants());
            }

        }
        if (BooleanUtils.isFalse(isBinDetailFetched)) {
            setBinDetails(flowRequestBean);
        }

        // Setting payMethod & payOption
        buildPayMethodAndPayOption(requestData, flowRequestBean);
    }

    private SarvatraVpaDetails getSarvatraVPADetails(PaymentRequestBean requestData) {
        if (requestData.getCreditBlock() == null) {
            LOGGER.info("Got Credit block null, will pay from default debit account");
            return null;
        }

        /*
         * if (!(ERequestType.OFFLINE.getType().equalsIgnoreCase(requestData.
         * getRequestType()) || ERequestType.NATIVE
         * .getType().equalsIgnoreCase(requestData.getRequestType()))) {
         * LOGGER.info(
         * "Payment flow is neither native nor offline, will pay from default debit account"
         * ); return null; }
         */
        SarvatraVpaDetails sarvatraVpaDetails = null;
        try {
            sarvatraVpaDetails = JsonMapper.mapJsonToObject(requestData.getCreditBlock(), SarvatraVpaDetails.class);
        } catch (FacadeCheckedException e) {
            LOGGER.warn("Something broke while converting credBlock into Sarvtra Vpa details ");
            return null;
        }
        return sarvatraVpaDetails;
    }

    private String getIfsc(SarvatraVpaDetails sarvatraVpaDetails) {
        if (sarvatraVpaDetails == null || sarvatraVpaDetails.getDefaultDebit() == null) {
            return null;
        }
        return sarvatraVpaDetails.getDefaultDebit().getIfsc();
    }

    private String getAccountNumber(SarvatraVpaDetails sarvatraVpaDetails) {
        if (sarvatraVpaDetails == null || sarvatraVpaDetails.getDefaultDebit() == null) {
            return null;
        }
        return sarvatraVpaDetails.getDefaultDebit().getAccount();
    }

    private void setAddOrHybridOption(final PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean) {

        if (StringUtils.isBlank(requestData.isAddMoney())) {
            flowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.NONE);

        } else if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.isAddMoney())) {

            flowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.ADDANDPAY);

        } else if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_0.equals(requestData.isAddMoney())) {

            flowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);

        } else {

            throw new TheiaServiceException("Invalid addMoney flag value in PAYTM_EXPRESS");

        }
    }

    /**
     * @param requestData
     * @param flowRequestBean
     */
    private void buildSubscriptionRequestData(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) {

        if (!ERequestType.isSubscriptionCreationRequest(requestData.getRequestType())
                && requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }

        // For Native Support of Subscription
        flowRequestBean.setSubscription(requestData.isSubscription());
        flowRequestBean.setSubscriptionID(requestData.getSubscriptionID());

        flowRequestBean.setMotoPaymentBySubscription(requestData.isMotoPaymentBySubscription());

        if (ERequestType.isSubscriptionCreationRequest(requestData.getRequestType())) {
            String orderExpiryTimeForMerchantInMillis = getOrderTimeout(flowRequestBean.getPaytmMID());
            if (StringUtils.isNotBlank(orderExpiryTimeForMerchantInMillis)) {
                flowRequestBean.setOrderExpiryTimeInMerchantContract(String.valueOf(Long
                        .valueOf(orderExpiryTimeForMerchantInMillis) * 60 * 1000));
            }
        }

        if (requestData.getRequestType().equals(RequestTypes.SUBSCRIPTION) || requestData.isSubscription()) {

            flowRequestBean.setSubsPPIOnly(requestData.getSubsPPIOnly());

            if ("PPI".equals(requestData.getSubsPaymentMode()) || StringUtils.isBlank(requestData.getSubsPaymentMode())) {
                if ("Y".equals(requestData.getSubsPPIOnly())
                        || ((ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestData.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION_PAY
                                .getType().equals(requestData.getRequestType())) && !requestData
                                .isEnhancedCashierPageRequest())) {
                    flowRequestBean.setSubsPayMode(SubsPaymentMode.PPI);
                    flowRequestBean.setSubsTypes(SubsTypes.PPI_ONLY);
                } else {
                    flowRequestBean.setSubsTypes(SubsTypes.NORMAL);
                    flowRequestBean.setSubsPayMode(SubsPaymentMode.NORMAL);
                }
            } else if ("CC".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.CC_ONLY);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.CC);
                flowRequestBean.setStoreCard("1");
            } else if ("DC".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.DC_ONLY);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.DC);
                flowRequestBean.setStoreCard("1");
            } else if ("PPBL".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.PPBL_ONLY);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.PPBL);
            } else if ("NORMAL".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.NORMAL);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.NORMAL);
                flowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.ADDANDPAY);
            } else if ("UNKNOWN".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.UNKNOWN);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.UNKNOWN);
            } else if ("BANK_MANDATE".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.BANK_MANDATE_ONLY);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.BANK_MANDATE);
            } else if ("UPI".equals(requestData.getSubsPaymentMode())) {
                flowRequestBean.setSubsTypes(SubsTypes.UPI_ONLY);
                flowRequestBean.setSubsPayMode(SubsPaymentMode.UPI);
            }
        }

        if (requestData.getRequestType().equalsIgnoreCase(TheiaConstant.RequestTypes.SUBSCRIPTION)
                || requestData.getRequestType().equals(RequestTypes.PARTIAL_RENEW_SUBSCRIPTION)
                || ERequestType.isSubscriptionRequest(requestData.getRequestType())) {

            flowRequestBean.setSubsAmountType(requestData.getSubscriptionAmountType());
            flowRequestBean.setSubsEnableRetry(requestData.getSubscriptionEnableRetry());
            flowRequestBean.setSubsExpiryDate(requestData.getSubscriptionExpiryDate());
            flowRequestBean.setSubsFrequency(requestData.getSubscriptionFrequency());
            flowRequestBean.setSubsFrequencyUnit(requestData.getSubscriptionFrequencyUnit());
            flowRequestBean.setSubsGraceDays(requestData.getSubscriptionGraceDays());
            flowRequestBean.setSubsServiceID(requestData.getSubscriptionServiceID());
            flowRequestBean.setSubsStartDate(requestData.getSubscriptionStartDate());
            flowRequestBean.setAccountNumber(requestData.getAccountNumber());
            flowRequestBean.setAllowUnverifiedAccount(requestData.getAllowUnverifiedAccount());
            flowRequestBean.setValidateAccountNumber(requestData.getValidateAccountNumber());
            flowRequestBean.setAdditionalInfoMF(requestData.getAdditionalInfoMF());

            String retryCount = StringUtils.isBlank(requestData.getSubscriptionRetryCount())
                    && requestData.getRequestType().equalsIgnoreCase(TheiaConstant.RequestTypes.SUBSCRIPTION) ? TheiaConstant.ExtraConstants.DEFAULT_RETRY_COUNT
                    : requestData.getSubscriptionRetryCount();
            flowRequestBean.setSubsRetryCount(retryCount);

            if (!ExtraConstants.CONNECTION_TYPE_S2S.equals(requestData.getConnectiontype())
                    && StringUtils.isBlank(requestData.getSubscriptionStartDate())) {
                flowRequestBean.setStartDateFlow(false);
            }

            if (StringUtils.isBlank(requestData.getSubscriptionStartDate())
                    && !StringUtils.isBlank(requestData.getSubscriptionGraceDays())) {
                throw new TheiaServiceException("Invalid Grace Days");
            }
            flowRequestBean.setAccountType(requestData.getAccountType());
            flowRequestBean.setSubsMaxAmount(AmountUtils.getTransactionAmountInPaise(requestData
                    .getSubscriptionMaxAmount()));
            flowRequestBean.setAdminToken(configurationDataService
                    .getPaytmPropertyValue(ExtraConstants.OAUTH_ADMIN_TOKEN));
            flowRequestBean.setFlexiSubscription(requestData.isFlexiSubscription());

        }

        flowRequestBean.setPaymentMid(requestData.getPaymentMid());
        flowRequestBean.setPaymentOrderId(requestData.getPaymentOrderId());
    }

    private void buildCreditCardBillPaymentRequestData(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }

        if ((TheiaConstant.RequestTypes.CC_BILL_PAYMENT.equals(requestData.getRequestType()) || requestData
                .isCcBillPaymentRequest())
                && ff4JUtil.queryNonSensitiveForCCBillPayment(flowRequestBean.getPaytmMID(),
                        flowRequestBean.getCustID())) {
            if (StringUtils.isBlank(requestData.getCreditCardBillNo())) {
                throw new TheiaDataMappingException("Invalid card token received for CC Bill Payment",
                        ResponseConstants.INVALID_CC_BILL_NO);
            }
            QueryNonSensitiveAssetInfoResponse response = cardCenterHelper.queryNonSensitiveAssetInfo(
                    requestData.getCreditCardBillNo(), null);
            if (response == null || response.getCardInfo() == null) {
                throw new PaymentRequestValidationException("Token details not found for token id : "
                        + requestData.getCreditCardBillNo(), ResponseConstants.INVALID_CARD_TOKEN);
            }
            CardInfoResponse cardInfo = response.getCardInfo();
            if (!AssetTypeEnum.CREDIT_CARD.getCode().equals(cardInfo.getCardType())) {
                throw new TheiaDataMappingException("Invalid Card No", ResponseConstants.INVALID_CARD_NO);
            }
            flowRequestBean.getExtendInfo().setCreditCardBillNo(cardInfo.getCardIndexNo());
            return;
        }

        if (TheiaConstant.RequestTypes.CC_BILL_PAYMENT.equals(requestData.getRequestType())
                || requestData.isCcBillPaymentRequest()) {
            ExpressCardModel expressCardModel = getExpressCardModelFromCache(requestData);
            if (StringUtils.isNotBlank(expressCardModel.getMid())
                    && !expressCardModel.getMid().equals(requestData.getMid())) {
                throw new TheiaDataMappingException("Invalid Mid received in request", ResponseConstants.INVALID_MID);
            }
            if (StringUtils.isNotBlank(expressCardModel.getCustId())
                    && !expressCardModel.getCustId().equals(requestData.getCustId())) {
                throw new TheiaDataMappingException("Invalid CustId received in request",
                        ResponseConstants.INVALID_CUST_ID);
            }

            BinDetail binDetail = null;

            try {

                binDetail = cardUtils.fetchBinDetails(expressCardModel.getCardBin());

            } catch (PaytmValidationException e) {
                throw new TheiaDataMappingException("Invalid Card No", ResponseConstants.INVALID_CARD_NO);
            }

            if ((binDetail != null) && !"CREDIT_CARD".equals(binDetail.getCardType())) {
                throw new TheiaDataMappingException("Invalid Card No", ResponseConstants.INVALID_CARD_NO);
            }

            flowRequestBean.getExtendInfo().setCreditCardBillNo(expressCardModel.getCardIndexNo());
        }
    }

    private String setProductCode(PaymentRequestBean requestData, final WorkFlowRequestBean flowRequestBean) {
        String productCode = null;

        if (flowRequestBean.isDealsFlow())
            return ProductCodes.StandardDirectPayDealAcquiringProd.getId();

        if (requestData.isAutoRefund()) {
            productCode = ProductCodes.StandardDirectPayPennyDropProd.getId();
            return productCode;
        }
        if (merchantPreferenceProvider.isSlabBasedMDREnabled(requestData)) {
            productCode = ProductCodes.StandardAcquiringProdByAmountChargePayer.getId();
        } else if (merchantPreferenceProvider.isDynamicFeeMerchant(requestData)) {
            if (workFlowHelper.getProductCodeForDynamicChargePayment(requestData.getProductCode()) == null) {
                productCode = ProductCodes.StandardDirectPayDynamicTargetProd.getId();
            } else {
                return getProductCodeForRequestType(requestData.getRequestType(), requestData.getProductCode());
            }
        } else if (merchantPreferenceProvider.isPostConvenienceFeesEnabled(requestData)) {
            if (ERequestType.PAYTM_EXPRESS.getType().equals(requestData.getRequestType())) {
                if (ff4JUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(),
                        ENABLE_SET_PAYTM_EXPRESS_PRODUCT_CODE_MAPPING, false)) {
                    productCode = ProductCodes.StandardDirectPayAcquiringProdChargePayer.getId();
                } else {
                    productCode = ProductCodes.StandardAcquiringProdByJSChargePayer.getId();
                }
            } else if (ERequestType.DYNAMIC_QR_2FA.getType().equals(requestData.getRequestType())) {
                productCode = ProductCodes.ScanNPayChargePayer.getId();
            } else {
                productCode = ProductCodes.StandardDirectPayAcquiringProdChargePayer.getId();
            }
        } else if ((ERequestType.OFFLINE.getType().equalsIgnoreCase(requestData.getRequestType())
                || requestData.isFastForwardRequest() || requestData.isOfflineFetchPayApi() || flowRequestBean
                    .isOfflineFlow()) && merchantPreferenceService.isPRNEnabled(requestData.getMid())) {
            productCode = ProductCodes.QrAcquiringProd.getId();
            flowRequestBean.setPrnEnabled(true);
        } else if (requestData.isNativeAddMoney()) {
            productCode = ProductCodes.UserTopupMultiPay.getId();
        } else if (requestData.getLinkDetailsData() != null
                && requestData.getLinkDetailsData().getEdcEmiFields() != null) {
            if (StringUtils.isNotBlank(requestData.getLinkDetailsData().getEdcEmiFields().getProductCode())) {
                try {
                    productCode = ProductCodes.getProductById(
                            requestData.getLinkDetailsData().getEdcEmiFields().getProductCode()).getId();
                } catch (FacadeInvalidParameterException e) {
                    LOGGER.error("Error in fetching product code from recieve product code in emi details:{}",
                            e.getMessage());
                }
            }
            if (StringUtils.isBlank(productCode)) {
                productCode = ProductCodes.StandardDirectPayAcquiringProd.getId();
            }
            requestData.getLinkDetailsData().getEdcEmiFields().setProductCode(productCode);
        } else {
            if (requestData.getLinkDetailsData() != null && StringUtils.isNotBlank(requestData.getSubRequestType())) {
                return getProductCodeForRequestType(requestData.getSubRequestType(), requestData.getProductCode());
            }
            return getProductCodeForRequestType(requestData.getRequestType(), requestData.getProductCode());
        }

        return productCode;
    }

    public void buildPaytmExpressPaymentDetails(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {

        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }

        if (PaymentTypeIdEnum.CC.value.equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.EMI.value.equals(requestData.getPaymentTypeId())) {
            String[] actualPaymentDetailsArray = buildExpressData(requestData);

            final String expiryDate = actualPaymentDetailsArray[2];
            flowRequestBean.setCardNo(actualPaymentDetailsArray[0]);
            flowRequestBean.setCvv2(actualPaymentDetailsArray[1]);
            flowRequestBean.setExpiryMonth(Short.valueOf(expiryDate.substring(0, 2)));
            flowRequestBean.setExpiryYear(Short.valueOf(expiryDate.substring(2, 6)));
            flowRequestBean.setCardType(actualPaymentDetailsArray[3]);
            flowRequestBean.setCardScheme(actualPaymentDetailsArray[4]);
            return;
        }

        if (PaymentTypeIdEnum.IMPS.value.equals(requestData.getPaymentTypeId())) {
            String[] actualPaymentDetailsArray = buildExpressData(requestData);

            flowRequestBean.setMobileNo(actualPaymentDetailsArray[0]);
            flowRequestBean.setMmid(actualPaymentDetailsArray[1]);
            flowRequestBean.setOtp(actualPaymentDetailsArray[2]);
            flowRequestBean.setCardType(actualPaymentDetailsArray[3]);
            flowRequestBean.setCardScheme(actualPaymentDetailsArray[4]);
            return;
        }

        if (PaymentTypeIdEnum.PPI.value.equals(requestData.getPaymentTypeId())
                && StringUtils.isBlank(requestData.getSsoToken())) {
            throw new PaymentRequestValidationException(
                    "Invalid PAYTM_EXPRESS Payment details, Token Is required in PPI");
        }

        if (PaymentTypeIdEnum.NB.value.equals(requestData.getPaymentTypeId())) {
            if (StringUtils.isBlank(requestData.getBankCode())) {
                throw new TheiaServiceException("Bank Code is required in PaytmExpress in NB");
            }
            flowRequestBean.setBankCode(requestData.getBankCode());
            return;
        }

        if (PaymentTypeIdEnum.UPI.value.equals(requestData.getPaymentTypeId())) {
            if (StringUtils.isBlank(requestData.getPaymentDetails())) {
                throw new TheiaServiceException("Virtual Payment Address is required in PaytmExpress in UPI");
            }
            flowRequestBean.setVirtualPaymentAddress(requestData.getPaymentDetails());
            return;
        }
    }

    private void setBinDetails(WorkFlowRequestBean flowRequestBean) {

        if (StringUtils.isNotBlank(flowRequestBean.getCardNo())) {
            try {

                BinDetail binDetails = processTransactionUtil.getBinDetail(flowRequestBean);
                flowRequestBean.setInstId(binDetails.getBankCode());
                flowRequestBean.setCardType(binDetails.getCardType());
                flowRequestBean.setCardScheme(binDetails.getCardName());
                flowRequestBean.setBankName(binDetails.getBank());
                flowRequestBean.setInternationalCard(!binDetails.getIsIndian());
                flowRequestBean.setCorporateCard(binDetails.isCorporateCard());
                flowRequestBean.setPrepaidCard(binDetails.isPrepaidCard());
                flowRequestBean.setCardActive(binDetails.isActive());
                if (flowRequestBean.getRequestType().getType().equals(ERequestType.ADDMONEY_EXPRESS.getType())) {
                    flowRequestBean.getExtendInfo().setBinNumber(String.valueOf(binDetails.getBin()));
                }
                flowRequestBean.setCustomDisplayName(binDetails.getDisplayBankName());
            } catch (Exception e) {
                throw new TheiaServiceException("Exception occured while fetching bin details : " + e.getMessage());
            }
        }
    }

    private BinDetail setTokenBinDetails(WorkFlowRequestBean flowRequestBean) {
        BinDetail binDetails = null;
        if (StringUtils.isNotBlank(flowRequestBean.getCardNo())) {
            final String binNumber = flowRequestBean.getCardNo().substring(0, 9);
            try {
                binDetails = processTransactionUtil.getBinDetail(flowRequestBean);

                flowRequestBean.setInstId(binDetails.getBankCode());
                flowRequestBean.setCardType(binDetails.getCardType());
                flowRequestBean.setCardScheme(binDetails.getCardName());
                flowRequestBean.setBankName(binDetails.getBank());
                flowRequestBean.setInternationalCard(!binDetails.getIsIndian());
                flowRequestBean.setCorporateCard(binDetails.isCorporateCard());
                flowRequestBean.setPrepaidCard(binDetails.isPrepaidCard());
                flowRequestBean.setCardActive(binDetails.isActive());
                flowRequestBean.setCustomDisplayName(binDetails.getDisplayBankName());
            } catch (Exception e) {
                throw new TheiaServiceException("Exception occured while fetching bin details : " + e.getMessage());
            }
        }
        return binDetails;
    }

    public ExpressCardModel getExpressCardModelFromCache(PaymentRequestBean requestData)
            throws TheiaDataMappingException {

        String expressTokenId = "";
        if (TheiaConstant.RequestTypes.PAYTM_EXPRESS.equals(requestData.getRequestType())
                || TheiaConstant.RequestTypes.TOPUP_EXPRESS.equals(requestData.getRequestType())) {
            if (StringUtils.isBlank(requestData.getPaymentDetails())) {
                throw new TheiaDataMappingException(
                        "Invalid PAYTM_EXPRESS Payment details, PaymentDetails field cannot be blank",
                        ResponseConstants.INVALID_CARD_TOKEN);
            }
            expressTokenId = requestData.getPaymentDetails();
        }
        if (TheiaConstant.RequestTypes.CC_BILL_PAYMENT.equals(requestData.getRequestType())
                || requestData.isCcBillPaymentRequest()) {
            if (StringUtils.isBlank(requestData.getCreditCardBillNo())) {
                throw new TheiaDataMappingException("Invalid card token received for CC Bill Payment",
                        ResponseConstants.INVALID_CC_BILL_NO);
            }
            expressTokenId = requestData.getCreditCardBillNo();
        }
        String expressTokenDetailsKey = TheiaConstant.ExtraConstants.EXPRESS_CARD_TOKEN + expressTokenId;
        ExpressCardModel tokenDetails = (ExpressCardModel) theiaTransactionalRedisUtil.get(expressTokenDetailsKey);
        if (tokenDetails == null) {
            throw new PaymentRequestValidationException("Token details not found for token id : " + expressTokenId);
        }
        return tokenDetails;
    }

    /**
     * @param requestData
     * @return
     */
    private String[] buildExpressData(final PaymentRequestBean requestData) throws TheiaDataMappingException {

        ExpressCardModel expressCardModel = getExpressCardModelFromCache(requestData);
        if (!expressCardModel.getMid().equals(requestData.getMid())) {
            throw new TheiaDataMappingException("Invalid Mid received in request", ResponseConstants.INVALID_MID);
        }
        if (!expressCardModel.getCustId().equals(requestData.getCustId())) {
            throw new TheiaDataMappingException("Invalid CustId received in request", ResponseConstants.INVALID_CUST_ID);
        }

        String key = TheiaConstant.ExtraConstants.EXPRESS_PAYMENT + expressCardModel.getTokenId();
        String paymentDetails = (String) theiaTransactionalRedisUtil.get(key);
        if (StringUtils.isBlank(paymentDetails)) {
            throw new PaymentRequestValidationException("Invalid PAYTM_EXPRESS Payment details in CC/DC/IMPS");
        }
        requestData.setPaymentDetails(expressCardModel.getTokenId());

        String[] expressData = paymentDetails.split(Pattern.quote("|"));

        // Overriding payment details for TIN
        if ((PaymentTypeIdEnum.CC.value.equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(requestData.getPaymentTypeId()) || PaymentTypeIdEnum.EMI.value
                    .equals(requestData.getPaymentTypeId()))
                && ff4JUtils.isFeatureEnabledOnMid(requestData.getMid(), ENABLE_GCIN_ON_COFT_PROMO, false)) {
            String cardIndexNo = expressCardModel.getCardIndexNo();
            if (StringUtils.isNotBlank(cardIndexNo) && (cardIndexNo.length() > 15 && cardIndexNo.length() < 45)) {
                requestData.setCoftTokenTxn(true);
                requestData.setPaymentDetails(expressCardModel.getCardIndexNo() + "|" + expressData[1]);
            }
        }

        return expressData;
    }

    // EMI PayOptions
    public static EmiPayOption emiPayOption(PaymentRequestBean requestData) {
        EmiPayOption payOption = new EmiPayOption();
        Set<String> allowedPayOption = new HashSet<String>();
        Set<String> disallowedPayOption = new HashSet<String>();
        Set<String> banks = new HashSet<String>();
        String input = requestData.getEmiOption();

        if (StringUtils.isBlank(input) || ("SHOW_ALL").equals(input)
                || StringUtils.startsWithIgnoreCase(input, ZERO_COST_EMI)) {
            payOption.setShowAll(true);
            return payOption;
        }
        if (("DROP_ALL").equals(input)) {
            payOption.setDropAll(true);
            return payOption;
        }
        String[] payOptions = input.split(";");
        for (String EmiOption : payOptions) {
            try {
                String[] bank = EmiOption.split("-");
                String emiPlan = null;
                if ("CCAvenue".equals(bank[0])) {
                    continue;
                }
                emiPlan = "EMI_" + bank[0];
                if ("SHOW_ALL".equals(bank[1])) {
                    allowedPayOption.add(emiPlan);
                    banks.add(emiPlan);
                } else if ("DROP_ALL".equals(bank[1])) {
                    disallowedPayOption.add(emiPlan);
                } else if (bank[1].startsWith("SHOW_")) {
                    String[] emiMonths = bank[1].replaceAll("SHOW_", "").replaceAll("EMI", "").split(",");
                    for (String months : emiMonths) {
                        allowedPayOption.add(emiPlan + "-" + months);
                    }
                    banks.add(emiPlan);
                } else if (bank[1].startsWith("DROP_")) {
                    String[] emiMonths = bank[1].replaceAll("DROP_", "").replaceAll("EMI", "").split(",");
                    for (String months : emiMonths) {
                        disallowedPayOption.add(emiPlan + "-" + months);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.error("Invalid format ,EMI Option received {} ", EmiOption);
                LOGGER.info("Showing all the EMI configured on Merchant");
                payOption.setShowAll(true);
                return payOption;
            }
        }

        payOption.setAllowedPayOption(allowedPayOption);
        payOption.setDisallowedPayOption(disallowedPayOption);
        payOption.setBanks(banks);
        return payOption;
    }

    @Override
    public void mapWorkFlowResponseToSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData, final SavedCardRequest savedCardRequest) {
        buildMandatorySessiondata(requestData, responseData, savedCardRequest);
        merchantInfoSessionUtil.setMerchantInfoIntoSession(requestData, responseData);
        oAuthInfoSessionUtil.setOAuthInfo(requestData, responseData);
        if (StringUtils.isNotEmpty(responseData.getTransID()))
            transactionInfoSessionUtil.setTransactionInfoIntoSession(requestData, responseData);
        transactionConfigInfoSessionUtil.setTransactionConfigIntoSession(requestData, responseData);
        digitalCreditInfoSessionUtil.setDigitalCreditInfoInSession(requestData, responseData);
        paymentsBankAccountSessionUtil.setPaymentsBankAccountInfoInSession(requestData, responseData);
        sarvtraVPASessionUtil.setSarvatraVpaInfoInSession(requestData, responseData);
    }

    private void buildMandatorySessiondata(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData, final SavedCardRequest savedCardRequest) {
        loginInfoSessionUtil.setLoginInfoIntoSession(requestData, responseData);
        cardInfoSessionUtil.setCardInfoIntoSession(requestData, responseData);
        if (savedCardRequest != null) {
            cardInfoSessionUtil.reOrderSavedCardsForUser(requestData, savedCardRequest);
        }
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseData);
        entityPaymentOptionSessionUtil.setEntityPaymentOption(requestData, responseData);
    }

    private void setRiskExtendedInfo(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (requestData.isParseRiskExtendInfoAsJson() || requestData.isAutoDebitRequest()) {
            LOGGER.info("FastForward/AutoDebit request received, risk info : {}", requestData.getRiskExtendedInfo());
            setRiskInfoForFastForward(flowRequestBean, requestData.getRiskExtendedInfo());
        } else if (ERequestType.OFFLINE.getType().equals(requestData.getRequestType())
                || flowRequestBean.isOfflineFlow()) {
            LOGGER.info("Offline request type received, risk Info : {}", requestData.getRiskExtendedInfo());
            setRiskInfoForOffline(flowRequestBean, requestData.getRiskExtendedInfo());
        } else if (requestData.isAddMoneyRiskInvolved()) {
            LOGGER.info("Add Money Request received, risk Info : {}", requestData.getRiskExtendedInfo());
            setRiskInfoForAddMoney(flowRequestBean, requestData.getRiskExtendedInfo());
        }
        // setting risk ExtendInfo for Native
        else if (StringUtils.isNotBlank(requestData.getRiskExtendedInfo())) {
            Map<String, String> riskExtendInfoMap = AdditionalInfoUtil.generateMapFromAdditionalInfoString(requestData
                    .getRiskExtendedInfo());
            if (MapUtils.isEmpty(flowRequestBean.getRiskExtendedInfo())) {
                flowRequestBean.setRiskExtendedInfo(riskExtendInfoMap);
            } else {
                flowRequestBean.getRiskExtendedInfo().putAll(riskExtendInfoMap);
            }
        } else {
            flowRequestBean.setRiskExtendedInfo(new HashMap<>());
        }
        setMerchantCategoryInRiskExtendedInfo(flowRequestBean, requestData);
    }

    private void setMerchantCategoryInRiskExtendedInfo(WorkFlowRequestBean flowRequestBean,
            PaymentRequestBean requestBean) {
        try {
            MerchantInfo merchantProfileInfo = merchantQueryService.getMerchantProfileMappingData(requestBean.getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantProfileMappingData :: {}", merchantProfileInfo);
            if (merchantProfileInfo != null && StringUtils.isNotBlank(merchantProfileInfo.getCategory())) {
                Map<String, String> riskExtendInfo = new HashMap<>();
                riskExtendInfo.put(MERCHANT_CATEGORY, merchantProfileInfo.getCategory());
                if (MapUtils.isNotEmpty(flowRequestBean.getRiskExtendedInfo())) {
                    flowRequestBean.getRiskExtendedInfo().putAll(riskExtendInfo);
                } else {
                    flowRequestBean.setRiskExtendedInfo(riskExtendInfo);
                }
            }
        } catch (Exception e) {
            LOGGER.error("error while setting merchant category for mid :{} ,error {}", requestBean.getMid(), e);
        }
    }

    private void setLinkPaymentRiskExtendedInfo(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isNotEmpty(requestData.getInvoiceId()) || StringUtils.isNotEmpty(requestData.getLinkId())) {

            FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
            if (feeRateFactors == null) {
                feeRateFactors = new FeeRateFactors();
                flowRequestBean.setFeeRateFactors(feeRateFactors);
            }
            flowRequestBean.getFeeRateFactors().setLink(true);
            flowRequestBean.setRequestFlow(TheiaConstant.LinkRiskParams.LINK_TYPE_NON_INVOICE);
            LinkPaymentRiskInfo linkPaymentRiskInfo = null;
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            if (requestData.getLinkDetailsData() != null) {
                if (requestData.getLinkDetailsData().getLinkPaymentRiskInfo() != null) {
                    linkPaymentRiskInfo = requestData.getLinkDetailsData().getLinkPaymentRiskInfo();
                }
            } else {
                linkPaymentRiskInfo = linkPaymentUtil.getLinkPaymentRiskInfo(requestData);
            }

            if (linkPaymentRiskInfo != null) {
                try {
                    Map<String, String> riskInfoMap = flowRequestBean.getRiskExtendedInfo();
                    if (riskInfoMap == null) {
                        riskInfoMap = new HashMap<>();
                    }
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.IS_LINK_BASED_PAYEMNT,
                            String.valueOf(linkPaymentRiskInfo.isLinkPaymentRequest()));
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_CREATION_TIME, linkPaymentRiskInfo
                            .getLinkCreationTime().toString());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_DESCRIPTION,
                            linkPaymentRiskInfo.getLinkDescription());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_NAME, linkPaymentRiskInfo.getLinkName());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_ORIGIN_LATITUDE,
                            linkPaymentRiskInfo.getLinkOriginLatitude());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_ORIGIN_LONGITUDE,
                            linkPaymentRiskInfo.getLinkOriginLongitude());
                    if (linkPaymentRiskInfo.getMerchantLimit() != null) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.MERCHANT_LIMIT,
                                String.valueOf(linkPaymentRiskInfo.getMerchantLimit()));
                    }
                    if (StringUtils.isNotBlank(linkPaymentRiskInfo.getResellerId())) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.RESELLER_ID, linkPaymentRiskInfo.getResellerId());
                    }
                    if (StringUtils.isNotBlank(linkPaymentRiskInfo.getResellerName())) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.RESELLER_NAME,
                                linkPaymentRiskInfo.getResellerName());
                    }
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.DEVICE_ID, flowRequestBean.getDeviceId());

                    if (StringUtils.isBlank(riskInfoMap.get(TheiaConstant.LinkRiskParams.USER_LATITUDE))) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.USER_LATITUDE,
                                httpServletRequest.getParameter(TheiaConstant.LinkBasedParams.USER_REQUEST_LATITUDE));
                    }
                    if (StringUtils.isBlank(riskInfoMap.get(TheiaConstant.LinkRiskParams.USER_LONGITUTDE))) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.USER_LONGITUTDE,
                                httpServletRequest.getParameter(TheiaConstant.LinkBasedParams.USER_REQUEST_LONGITUTDE));
                    }

                    if (StringUtils.isNotBlank(linkPaymentRiskInfo.getMerchantMode())) {
                        riskInfoMap.put(TheiaConstant.LinkRiskParams.MERCHANT_MODE,
                                linkPaymentRiskInfo.getMerchantMode());
                    }

                    riskInfoMap.put(TheiaConstant.LinkRiskParams.MERCHANT_DISPLAY_NAME, flowRequestBean.getExtendInfo()
                            .getMerchantName());
                    riskInfoMap.put(Native.LINK_ID, (requestData.getLinkId() != null) ? requestData.getLinkId()
                            : requestData.getInvoiceId());
                    riskInfoMap.put(ExtraConstants.REQUEST_TYPE, TheiaConstant.LinkBasedParams.LINK_REQUEST_TYPE);
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_REFERRER_SITE,
                            linkPaymentRiskInfo.getLinkReferrerSite());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_TYPE, linkPaymentRiskInfo.getLinkType());
                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_OPEN_TIME, linkPaymentRiskInfo.getLinkOpenTime());

                    riskInfoMap.put(TheiaConstant.LinkRiskParams.LINK_AMOUNT, flowRequestBean.getTxnAmount());
                    flowRequestBean.setRiskExtendedInfo(riskInfoMap);
                    LOGGER.info("risk Extended Info for Link Payments {}", riskInfoMap);
                } catch (Exception e) {
                    LOGGER.error("Got Exception in parsing link payment risk info for linkid/invoiceid {}/{}",
                            requestData.getLinkId(), requestData.getInvoiceId(), e);
                }
            }
        }
    }

    private void setLinkDetailsInChannelInfo(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isNotEmpty(requestData.getInvoiceId()) || StringUtils.isNotEmpty(requestData.getLinkId())) {
            com.paytm.pgplus.common.model.link.LinkDetailResponseBody linkDetails;
            if (requestData.getLinkDetailsData() != null) {
                linkDetails = requestData.getLinkDetailsData();
            } else {
                linkDetails = linkPaymentUtil.getLinkDetailCachedResponse(requestData);
            }
            if (linkDetails != null) {
                if (BooleanUtils.isTrue(linkDetails.getIsHDFCDigiPOSMerchant())) {
                    Map<String, String> channelInfo = flowRequestBean.getChannelInfo();
                    if (channelInfo == null) {
                        channelInfo = new HashMap<String, String>();
                    }
                    String tid = linkDetails.getMerchantReferenceId();
                    channelInfo.put("isHDFCDigiPOSMerchant", TRUE);
                    channelInfo.put("paytmTid", tid);
                    if (linkDetails.getLinkPaymentRiskInfo() != null) {
                        channelInfo.put("isLinkBasedPayment",
                                String.valueOf(linkDetails.getLinkPaymentRiskInfo().isLinkPaymentRequest()));
                    }
                    flowRequestBean.setChannelInfo(channelInfo);
                }
            }
        }
    }

    private String getOrderTimeout(String mid) {
        return merchantPreferenceService.getOrderExpiryTimeForMerchant(mid);
    }

    protected boolean setSubWalletFlag(PaymentRequestBean requestData, String pid, String initiateRequestPid) {

        if (StringUtils.isNotBlank(initiateRequestPid) && initiateRequestPid.equals(pid)) {
            LOGGER.info("Entered subWalletFlow");
            return true;
        }
        return false;
    }

    protected void setSubWalletFlag(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        String pid = getPid(requestData);
        if (StringUtils.isNotBlank(pid)) {
            if (setSubWalletFlag(requestData, GV_PRODUCT_CODE, pid)) {
                LOGGER.info("Entered GV Flow");
                flowRequestBean.setGvFlag(true);
            } else if (setSubWalletFlag(requestData, NCMC_PRODUCT_CODE, pid)) {
                LOGGER.info("Entered transit wallet Flow");
                flowRequestBean.setTransitWallet(true);
            }
        }
    }

    private String getPid(PaymentRequestBean requestData) {
        String pid = null;

        if (StringUtils.isBlank(requestData.getGoodsInfo())) {
            return pid;
        }

        String goodsInfoStr = requestData.getGoodsInfo();
        List<GoodsInfo> goodsInfoList;
        try {

            if (ERequestType.NATIVE_PAY.getType().equals(requestData.getRequestType())
                    || ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                    || requestData.isCreateOrderForInitiateTxnRequest()) {
                goodsInfoList = OBJECT_MAPPER.readValue(goodsInfoStr, OBJECT_MAPPER.getTypeFactory()
                        .constructCollectionType(List.class, GoodsInfo.class));
            } else {
                goodsInfoList = OBJECT_MAPPER.readValue(URLDecoder.decode(goodsInfoStr, "UTF-8"), OBJECT_MAPPER
                        .getTypeFactory().constructCollectionType(List.class, GoodsInfo.class));
            }
            GoodsInfo goodsInfo = goodsInfoList.get(0);
            pid = goodsInfo.getMerchantGoodsId();
        } catch (Exception ex) {
            LOGGER.warn("Something went wrong ", ex.getMessage());
        }
        return pid;
    }

    public static boolean isQRCodeRequest(final PaymentRequestBean requestData) {
        if (StringUtils.isNotBlank(requestData.getAdditionalInfo())) {
            String[] additionalInfoKeyValArray = requestData.getAdditionalInfo().split(
                    Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String keyVal : additionalInfoKeyValArray) {
                if (keyVal.contains(TheiaConstant.RequestParams.REQUEST_TYPE)) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2
                            && (BizConstant.REQUEST_QR_ORDER.equals(keyValSplit[1])
                                    || BizConstant.REQUEST_QR_MERCHANT.equals(keyValSplit[1])
                                    || BizConstant.UPI_QR_CODE.equals(keyValSplit[1]) || UPI_POS_ORDER
                                        .equals(keyValSplit[1]))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private MerchantInfoRequest getMerchantInfoRequest(List<SplitInfoData> splitInfoList) {
        List<String> midList = new ArrayList<>();
        if (splitInfoList != null) {
            for (SplitInfoData splitInfo : splitInfoList) {
                midList.add(splitInfo.getMid());
            }
        }
        return new MerchantInfoRequest(midList);

    }

    private void populateSplitCommandInfoList(PaymentRequestBean requestData,
            ExtendedInfoRequestBean extInforequestBean, WorkFlowRequestBean flowRequestBean) {

        SplitSettlementInfoData splitSettlementInfoData = requestData.getSplitSettlementInfoData();
        List<String> partnerIdList = new ArrayList<>();
        List<String> midList = new ArrayList<>();

        for (SplitInfoData splitInfoData : splitSettlementInfoData.getSplitInfo()) {
            if (StringUtils.isNotBlank(splitInfoData.getMid()))
                midList.add(splitInfoData.getMid());
            if (StringUtils.isNotBlank(splitInfoData.getPartnerId()))
                partnerIdList.add(splitInfoData.getPartnerId());
        }

        try {
            if (!midList.isEmpty()) {
                if (midList.size() != splitSettlementInfoData.getSplitInfo().size()) {
                    throw new PaymentRequestValidationException("Child MIDs are missing in splitInfo");
                }
                MerchantInfoRequest merchantInfoRequest = new MerchantInfoRequest(midList);
                final MerchantInfoResponse merchantInfoList = merchantDataService
                        .getMerchantInfoList(merchantInfoRequest);
                EXT_LOGGER.customInfo("Mapping response - MerchantInfoResponse :: {} for MID list :: {}",
                        merchantInfoList, midList);
                convertSplitSettlementsDetails(extInforequestBean, requestData.getSplitSettlementInfoData(),
                        merchantInfoList, flowRequestBean);
            } else if (!partnerIdList.isEmpty()) {
                if (partnerIdList.size() != splitSettlementInfoData.getSplitInfo().size()) {
                    throw new PaymentRequestValidationException("Child partnerIds are missing in splitInfo");
                }
                final VendorParentDetails vendorParentDetails = vendorInfoService.getVendorSplitDetails(
                        requestData.getMid(), partnerIdList);
                EXT_LOGGER.customInfo("Mapping response - VendorParentDetails :: {}", vendorParentDetails);
                LOGGER.info("isPartiallyMatched flag: " + vendorParentDetails.isPartiallyMatched());

                if (BooleanUtils.isTrue(vendorParentDetails.isPartiallyMatched())) {
                    LOGGER.error("Merchant data is not found for all given partnerIds from Mapping Service");
                    throw new PaymentRequestValidationException("Merchant data not found for all given partnerIds");
                }
                convertSplitSettlementsDetails(extInforequestBean, requestData.getSplitSettlementInfoData(),
                        vendorParentDetails, flowRequestBean);
            } else {
                throw new PaymentRequestValidationException("Mid or partnerId is missing from splitInfo data");
            }

        } catch (MappingServiceClientException | PaymentRequestValidationException e) {
            LOGGER.error("invalid mid in splitInfo : " + e.getMessage());
            throw new PaymentRequestValidationException(e.getMessage(), ResponseConstants.INVALID_MID);
        }
    }

    private void convertSplitSettlementsDetails(ExtendedInfoRequestBean extendedInfoRequestBean,
            SplitSettlementInfoData splitSettlementInfo, VendorParentDetails vendorParentDetails,
            WorkFlowRequestBean workFlowRequestBean) {
        List<SplitPayInfo> splitPayInfos = new ArrayList<>();
        Map<String, String> paytmAlipayMid = new HashMap<>();
        Map<String, String> partnerIdpaytmMid = new HashMap<>();
        if (vendorParentDetails != null && CollectionUtils.isNotEmpty(vendorParentDetails.getVendorDetails())) {
            try {
                for (VendorDetail vendorDetail : vendorParentDetails.getVendorDetails()) {
                    paytmAlipayMid.put(vendorDetail.getPaytmMerchantId(), vendorDetail.getAlipayMerchantId());
                    partnerIdpaytmMid.put(vendorDetail.getPartnerId(), vendorDetail.getPaytmMerchantId());
                }
                if (splitSettlementInfo.getSplitInfo() != null) {
                    List<SplitCommandInfo> splitCommandInfoList = new ArrayList<>();
                    for (SplitInfoData splitInfo : splitSettlementInfo.getSplitInfo()) {
                        MultiCurrency splitAmount = null;
                        com.paytm.pgplus.facade.common.model.Money money = null;
                        List<Goods> goodsList = SplitSettlementHelper.createGoodsListFromGoodsInfoList(splitInfo
                                .getGoods());
                        List<ShippingInfo> shippingInfosList = SplitSettlementHelper
                                .createFacadeShippingListFromShippingInfoList(splitInfo.getShippingInfo());
                        if (SplitMethod.AMOUNT.equals(splitSettlementInfo.getSplitMethod())) {
                            splitAmount = new MultiCurrency(splitInfo.getAmount().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                            money = new Money(splitInfo.getAmount().getCurrency().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                        }

                        String childMid = null;
                        if (StringUtils.isNotBlank(splitInfo.getPartnerId())) {
                            childMid = partnerIdpaytmMid.get(splitInfo.getPartnerId());
                            Map<String, String> extendInfoMap = new HashMap<>();
                            if (splitInfo.getExtendInfo() != null) {
                                extendInfoMap = new ObjectMapper().readValue(splitInfo.getExtendInfo(), Map.class);
                            }
                            extendInfoMap.put(PARTNER_ID, splitInfo.getPartnerId());
                            splitInfo.setExtendInfo(ObjectMapperUtil.getObjectMapper()
                                    .writeValueAsString(extendInfoMap));
                        }

                        SplitPayInfo splitPayInfo = new SplitPayInfo(splitSettlementInfo.getSplitMethod(),
                                paytmAlipayMid.get(childMid), splitAmount, splitInfo.getPercentage());
                        SplitCommandInfo splitCommandInfo = new SplitCommandInfo.SplitCommandInfoBuilder(
                                splitSettlementInfo.getSplitMethod(), paytmAlipayMid.get(childMid), childMid, money,
                                splitInfo.getPercentage()).setExtendInfo(splitInfo.getExtendInfo()).setGoods(goodsList)
                                .setShippingInfo(shippingInfosList).build();

                        splitPayInfos.add(splitPayInfo);
                        splitCommandInfoList.add(splitCommandInfo);
                    }
                    workFlowRequestBean.setSplitCommandInfoList(splitCommandInfoList);
                    if (!ff4JUtil.isFeatureEnabled(
                            TheiaConstant.ExtraConstants.THEIA_DISABLE_SENDING_SPLIT_DATA_IN_EXTEND_INFO,
                            workFlowRequestBean.getPaymentRequestBean().getMid())) {
                        extendedInfoRequestBean.setSplitPayInfo(JsonMapper.mapObjectToJson(splitPayInfos));
                        extendedInfoRequestBean.setSplitSettlementInfo(JsonMapper.mapObjectToJson(SplitSettlementHelper
                                .createSplitSettlementInfoFromSplitSettlementInfoData(splitSettlementInfo)));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("error in converting splitSettlement info into splitPayInfo, SPlitCommandInfoList {} ", e);
                throw new PaymentRequestValidationException(e, ResponseConstants.INVALID_REQUEST);
            }
        }

    }

    private void convertSplitSettlementsDetails(ExtendedInfoRequestBean extendedInfoRequestBean,
            SplitSettlementInfoData splitSettlementInfo, MerchantInfoResponse merchantInfoResponse,
            WorkFlowRequestBean workFlowRequestBean) {
        List<SplitPayInfo> splitPayInfos = new ArrayList<>();
        Map<String, String> paytmAlipayMid = new HashMap<>();
        if (merchantInfoResponse != null && merchantInfoResponse.getMerchantInfoList() != null) {
            for (com.paytm.pgplus.cache.model.MerchantInfo merchantInfo : merchantInfoResponse.getMerchantInfoList()) {
                paytmAlipayMid.put(merchantInfo.getPaytmId(), merchantInfo.getAlipayId());
            }
            try {
                if (splitSettlementInfo.getSplitInfo() != null) {
                    List<SplitCommandInfo> splitCommandInfoList = new ArrayList<>();
                    for (SplitInfoData splitInfo : splitSettlementInfo.getSplitInfo()) {
                        MultiCurrency splitAmount = null;
                        com.paytm.pgplus.facade.common.model.Money money = null;
                        List<Goods> goodsList = SplitSettlementHelper.createGoodsListFromGoodsInfoList(splitInfo
                                .getGoods());
                        List<ShippingInfo> shippingInfosList = SplitSettlementHelper
                                .createFacadeShippingListFromShippingInfoList(splitInfo.getShippingInfo());
                        if (SplitMethod.AMOUNT.equals(splitSettlementInfo.getSplitMethod())) {
                            splitAmount = new MultiCurrency(splitInfo.getAmount().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                            money = new Money(splitInfo.getAmount().getCurrency().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                        }
                        SplitPayInfo splitPayInfo = new SplitPayInfo(splitSettlementInfo.getSplitMethod(),
                                paytmAlipayMid.get(splitInfo.getMid()), splitAmount, splitInfo.getPercentage());
                        SplitCommandInfo splitCommandInfo = new SplitCommandInfo.SplitCommandInfoBuilder(
                                splitSettlementInfo.getSplitMethod(), paytmAlipayMid.get(splitInfo.getMid()),
                                splitInfo.getMid(), money, splitInfo.getPercentage())
                                .setExtendInfo(splitInfo.getExtendInfo()).setGoods(goodsList)
                                .setShippingInfo(shippingInfosList).build();

                        splitPayInfos.add(splitPayInfo);
                        splitCommandInfoList.add(splitCommandInfo);
                    }
                    workFlowRequestBean.setSplitCommandInfoList(splitCommandInfoList);
                    if (!ff4JUtil.isFeatureEnabled(
                            TheiaConstant.ExtraConstants.THEIA_DISABLE_SENDING_SPLIT_DATA_IN_EXTEND_INFO,
                            workFlowRequestBean.getPaymentRequestBean().getMid())) {
                        extendedInfoRequestBean.setSplitPayInfo(JsonMapper.mapObjectToJson(splitPayInfos));
                        extendedInfoRequestBean.setSplitSettlementInfo(JsonMapper.mapObjectToJson(SplitSettlementHelper
                                .createSplitSettlementInfoFromSplitSettlementInfoData(splitSettlementInfo)));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("error in converting splitSettlement info into splitPayInfo, SPlitCommandInfoList {} ", e);
                throw new PaymentRequestValidationException(e, ResponseConstants.INVALID_REQUEST);
            }
        }

    }

    void setEnvInfoForLinkPayment(EnvInfoRequestBean envInfo) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        Map<String, String> extendInfo = envInfo.getExtendInfo();
        extendInfo.put(IS_DEVICE_EVER_USED, httpServletRequest.getParameter(IS_DEVICE_EVER_USED));
        extendInfo.put(USER_AGENT, httpServletRequest.getParameter(USER_AGENT));
        extendInfo.put(FUZZY_DEVICE_ID, httpServletRequest.getParameter(FUZZY_DEVICE_ID));
        extendInfo.put(APP_VERSION, httpServletRequest.getParameter(VERSION));
        extendInfo.put(PHONE_MODEL, httpServletRequest.getParameter(DEVICE_NAME));
        extendInfo.put(IMEI, httpServletRequest.getParameter(IMEI));
        extendInfo.put(DEVICE_MANUFACTURE, httpServletRequest.getParameter(DEVICE_MANUFACTURE));
        extendInfo.put(DEVICE_LANGUAGE, httpServletRequest.getParameter(LANGUAGE));
    }

    private SavedCard fetchSavedCardByCardIndexNumber(List<PayChannelBase> savedCardList, String cardIndexNumber) {

        if (CollectionUtils.isNotEmpty(savedCardList)) {
            for (PayChannelBase payChannelBase : savedCardList) {
                SavedCard savedCard = (SavedCard) payChannelBase;
                if (cardIndexNumber.equals(savedCard.getCardDetails().getCardId())) {
                    return savedCard;
                }
            }
        }
        return null;
    }

    private SavedCard fetchSavedCardByCardId(List<PayChannelBase> savedCardList, String cardId) {
        if (CollectionUtils.isNotEmpty(savedCardList)) {
            for (PayChannelBase payChannelBase : savedCardList) {
                SavedCard savedCard = (SavedCard) payChannelBase;
                if (cardId.equals(savedCard.getCardDetails().getCardId())) {
                    return savedCard;
                }
            }
        }
        return null;

    }

    private void validateTransactionAmountForOfflineFlow(WorkFlowRequestBean flowRequestBean) {
        PayMethod payMethod = null;
        String pMethod = flowRequestBean.getPayMethod();
        if (StringUtils.isBlank(pMethod)) {
            pMethod = flowRequestBean.getSavedCardPayMethod();
        }
        if (StringUtils.isNotBlank(pMethod)) {
            payMethod = PayMethod.getPayMethodByMethod(pMethod) != null ? PayMethod.getPayMethodByMethod(pMethod)
                    : PayMethod.getPayMethodByOldName(pMethod);
        }
        if (flowRequestBean != null && payMethod != null
                && (flowRequestBean.isOfflineFlow() || flowRequestBean.isOfflineTxnFlow())
                && StringUtils.isNotBlank(flowRequestBean.getTxnToken())) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(flowRequestBean
                    .getTxnToken());
            if (cashierInfoResponse != null
                    && cashierInfoResponse.getBody() != null
                    && cashierInfoResponse.getBody().getMerchantLimitDetail() != null
                    && CollectionUtils.isNotEmpty(cashierInfoResponse.getBody().getMerchantLimitDetail()
                            .getMerchantPaymodesLimits())) {
                List<MerchantLimit> merchantLimitList = cashierInfoResponse.getBody().getMerchantLimitDetail()
                        .getMerchantPaymodesLimits();
                for (MerchantLimit merchantLimit : merchantLimitList) {
                    if (StringUtils.isNotBlank(flowRequestBean.getTxnAmount())
                            && (payMethod.getMethod().equals(merchantLimit.getPayMode()) || payMethod.getOldName()
                                    .equals(merchantLimit.getPayMode()))
                            && Double.parseDouble(AmountUtils.getTransactionAmountInRupee(flowRequestBean
                                    .getTxnAmount())) > merchantLimit.getLimit()) {
                        throw new NativeFlowException.ExceptionBuilder(
                                ResponseConstants.PPI_LIMIT_PAYMODE_AMOUNT_BREACHED).build();
                    }
                }
            }
        }
    }

    public String getProductCodeForRequestType(String requestType, String productCode) {
        ERequestType eRequestType = ERequestType.getByRequestType(requestType);
        if (StringUtils.isNotBlank(productCode)) {
            return productCode;
        }
        try {
            productCode = ProductCodes.getProductByProductCode(eRequestType.getProductCode()).getId();
        } catch (FacadeInvalidParameterException e) {
            LOGGER.error("Could not set product code because : ", e);
        }
        return productCode;
    }

    private void buildCCDCDataForEcomToken(PaymentRequestBean requestBean, WorkFlowRequestBean flowRequestBean)
            throws TheiaDataMappingException {

        if (flowRequestBean.isEcomTokenTxn()) {
            flowRequestBean.setPaymentDetails(requestBean.getPaymentDetails());
            flowRequestBean.setBankCode(requestBean.getBankCode());

            if (StringUtils.isBlank(flowRequestBean.getPaymentTypeId())) {
                throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENTMODE);
            }

            if (ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                    && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                            .equals(flowRequestBean.getPaymentTypeId()))) {
                EcomTokenInfo ecomTokenInfo = null;
                try {
                    ecomTokenInfo = JsonMapper
                            .mapJsonToObject(flowRequestBean.getPaymentDetails(), EcomTokenInfo.class);
                    validationService.populateExpiryDate(flowRequestBean, ecomTokenInfo.getExpiryMonth()
                            + ecomTokenInfo.getExpiryYear());
                } catch (Exception e) {
                    LOGGER.error("Exception occured while validating payment details for ecomToken ", e);
                    throw new TheiaDataMappingException(e.getMessage(), ResponseConstants.INVALID_PAYMENT_DETAILS);
                }
                flowRequestBean.setCardNo(ecomTokenInfo.getEcomToken());
            }

            setTokenBinDetails(flowRequestBean);
            // Setting payMethod & payOption
            buildPayMethodAndPayOption(requestBean, flowRequestBean);

            setAddOrHybridOption(requestBean, flowRequestBean);
            setFeeRateFactorForEcomToken(flowRequestBean);
        }
    }

    private void setFeeRateFactorForEcomToken(WorkFlowRequestBean flowRequestBean) {
        FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
        if (feeRateFactors == null) {
            feeRateFactors = new FeeRateFactors();
            flowRequestBean.setFeeRateFactors(feeRateFactors);
        }
        flowRequestBean.getFeeRateFactors().setEcomToken(flowRequestBean.isEcomTokenTxn());
    }

    private void setFeeRateFactorForDcc(WorkFlowRequestBean flowRequestBean) {

        FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
        if (feeRateFactors == null) {
            feeRateFactors = new FeeRateFactors();
            flowRequestBean.setFeeRateFactors(feeRateFactors);
        }
        flowRequestBean.getFeeRateFactors().setDcc(
                flowRequestBean.isDccSupported() && flowRequestBean.isDccSelectedByUser());
    }

    private Map<String, String> populateMerchantTypeInRiskExtendInfoMap(Map<String, String> riskExtendInfoMap,
            String merchantType) {
        if (riskExtendInfoMap != null
                && StringUtils.isBlank(riskExtendInfoMap.get(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE))) {
            riskExtendInfoMap.put(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE, merchantType);
        }
        return riskExtendInfoMap;
    }

    private void buildDccData(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        /*
         * 1. if bin indian 2. check in lpv if dccServiceInstIds present 3. call
         * mapping api and check if curreny present in supportedcurrency 4. call
         * insta api for fetching dcc rates if all success then dccSupported
         * flag as true
         */
        if (checkdccSupported(flowRequestBean, requestData)) {
            boolean dccSupported = false;
            BinDetail binDetails = null;
            binDetails = processTransactionUtil.getBinDetail(flowRequestBean);

            LOGGER.info("Bin Details recieved for dcc {} and mid {} ", binDetails, requestData.getMid());

            if (binDetails != null && binDetails.getIsIndian() != null && !binDetails.getIsIndian().booleanValue()) {
                List<String> dccSupportedServiceInst = dccPaymentHelper.dccEnabledAcquirersOnMerchantInLpv(requestData,
                        flowRequestBean, binDetails);
                LOGGER.info("dccSupportedServiceInst {}", dccSupportedServiceInst);
                // if payment call(for international and home currency ), then
                // return cached dccData in redis
                if (requestData.isPaymentCallFromDccPage()) {
                    DccPageData dccPageData = nativeSessionUtil.getDccPageData(flowRequestBean.getTxnToken());
                    flowRequestBean.setDccPageData(dccPageData);
                    flowRequestBean.setDccSupported(BOOLEAN_TRUE);
                    flowRequestBean.setDccSelectedByUser(requestData.isDccSelectedByUser());
                    validateTimeStampExpiryForDcc(flowRequestBean);
                    flowRequestBean.setPaymentCallFromDccPage(BOOLEAN_TRUE);
                    flowRequestBean.setDccServiceInstId(dccSupportedServiceInst.get(0));
                    setFeeRateFactorForDcc(flowRequestBean);
                    return;
                }
                if (CollectionUtils.isNotEmpty(dccSupportedServiceInst)) {
                    AcquirerCurrencyDetails acquirerCurrencyDetails;
                    try {
                        acquirerCurrencyDetails = dynamicCurrencyConverterService
                                .getSupportedCurrenciesForAcquirer(dccSupportedServiceInst.get(0));
                        LOGGER.info("acquirerCurrencyDetails {}", acquirerCurrencyDetails);
                        if (CollectionUtils.isNotEmpty(acquirerCurrencyDetails.getCurrencies())
                                && acquirerCurrencyDetails.getCurrencies().contains(binDetails.getCurrencyCode())) {
                            flowRequestBean.setDccServiceInstId(dccSupportedServiceInst.get(0));
                            DccPaymentDetail dccPaymentDetail = dccUtil.fetchDccRatesFromInsta(requestData,
                                    flowRequestBean);
                            if (dccPaymentDetail != null) {
                                LOGGER.info("dccSupported is true now for 1st v1/ptc ");
                                dccSupported = BOOLEAN_TRUE;
                                dccPageDataHelper.setDCCPageDataToCache(requestData, flowRequestBean, dccPaymentDetail);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Something bad happened while fetching dcc data Reason -> {} ", e);
                    }
                }
            }
            flowRequestBean.setDccSupported(dccSupported);

        }

    }

    private void populateOnlineOfflineMerchantTypeForDataEnrichment(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) {
        Boolean isOfflineMerchant = requestData.getOfflineMerchant();
        Map<String, String> riskExtendedInfo = flowRequestBean.getRiskExtendedInfo();
        if (riskExtendedInfo != null && isOfflineMerchant != null && isOfflineMerchant) {
            riskExtendedInfo.put(TheiaConstant.DataEnrichmentKeys.IS_OFFLINE_MERCHANT, TRUE);
            riskExtendedInfo.put(TheiaConstant.DataEnrichmentKeys.IS_ONLINE_MERCHANT, FALSE);
        } else if (riskExtendedInfo != null && isOfflineMerchant != null) {
            riskExtendedInfo.put(TheiaConstant.DataEnrichmentKeys.IS_OFFLINE_MERCHANT, FALSE);
            riskExtendedInfo.put(TheiaConstant.DataEnrichmentKeys.IS_ONLINE_MERCHANT, TRUE);
        }
    }

    private boolean checkdccSupported(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        return (ERequestType.NATIVE.equals(flowRequestBean.getRequestType()) || ERequestType.DYNAMIC_QR
                .equals(flowRequestBean.getRequestType()))
                && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.DC.value
                        .equals(flowRequestBean.getPaymentTypeId()))
                && !(EPayMode.ADDANDPAY.equals(flowRequestBean.getPaytmExpressAddOrHybrid())
                        || EPayMode.HYBRID.equals(flowRequestBean.getPaytmExpressAddOrHybrid())
                        || flowRequestBean.isNativeAddMoney() || flowRequestBean.getPaymentOfferCheckoutReqData() != null);

    }

    public void validateTimeStampExpiryForDcc(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getDccPageData() != null && flowRequestBean.getDccPageData().getDccPaymentDetails() != null
                && flowRequestBean.isDccSelectedByUser()) {
            String expiryTimeStamp = flowRequestBean.getDccPageData().getDccPaymentDetails().getExpirationTimestamp();

            /*
             * The ISO date-time formatter that formats or parses a date-time
             * with an offset, such as '2011-12-03T10:15:30+01:00'.
             */
            String bankTimeoffset = expiryTimeStamp.substring(expiryTimeStamp.length() - 6);
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(ISO_OFFSET_DATE_TIME).toFormatter();
            LocalDateTime d1 = LocalDateTime.parse(expiryTimeStamp, formatter);
            String[] hrsAndMinutes = bankTimeoffset.split(":", 2);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHoursMinutes(Integer.parseInt(hrsAndMinutes[0]),
                    Integer.parseInt(hrsAndMinutes[1])));
            LOGGER.info("Current hit for validateTimeStamp  {} and from insta {}", now + bankTimeoffset,
                    expiryTimeStamp);
            if (now.isAfter(d1)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(true)
                        .setRetryMsg(ResultCode.TIMESTAMP_EXPIRY.getResultMsg()).build();

            }
        }
    }

    public SavedCard getSavedCardDetails(NativeCashierInfoResponse cashierInfoResponse, String saveCardId) {
        if (Objects.nonNull(cashierInfoResponse) && Objects.nonNull(cashierInfoResponse.getBody())) {
            SavedCard saveCard = fetchSavedCardByCardIndexNumber(cashierInfoResponse.getBody().getMerchantPayOption()
                    .getSavedInstruments(), saveCardId);
            if (saveCard == null) {
                saveCard = fetchSavedCardByCardIndexNumber(cashierInfoResponse.getBody().getAddMoneyPayOption()
                        .getSavedInstruments(), saveCardId);
            }
            return saveCard;
        }
        return null;
    }

    private void populateRequestTypeInRiskExtendedInfoForDataEnrichment(WorkFlowRequestBean flowRequestBean) {
        Map<String, String> riskExtendedInfo = flowRequestBean.getRiskExtendedInfo();
        if (riskExtendedInfo != null) {
            if (flowRequestBean.getRequestType() != null)
                riskExtendedInfo.put(TheiaConstant.DataEnrichmentKeys.REQUEST_TYPE, flowRequestBean.getRequestType()
                        .getType());
            else
                riskExtendedInfo
                        .put(TheiaConstant.DataEnrichmentKeys.REQUEST_TYPE, TheiaConstant.DataEnrichmentKeys.NA);
        }
    }

    private PaymentOffer populatePaymentOfferFromPromoResponseV2(ApplyPromoServiceResponseV2 response, String txnToken,
            boolean validatePromo) {
        if (response != null && response.getStatus() == 1 && response.getData() != null
                && response.getData().getStatus() == 1) {
            PaymentOffer paymentOffer = new PaymentOffer();
            paymentOffer.setOfferBreakup(paymentOffersServiceHelperV2.getPromoOfferDetails(response.getData(), null,
                    null));
            paymentOffer.setTotalCashbackAmount(paymentOffer.getOfferBreakup().get(0).getCashbackAmount());
            paymentOffer.setTotalInstantDiscount(paymentOffer.getOfferBreakup().get(0).getInstantDiscount());
            paymentOffer.setTotalPaytmCashbackAmount(paymentOffer.getOfferBreakup().get(0).getPaytmCashbackAmount());
            NativeInitiateRequest request = nativeSessionUtil.validate(txnToken);
            if (request != null && request.getInitiateTxnReq() != null && request.getInitiateTxnReq().getBody() != null) {
                LOGGER.info("Setting payment offer object in order detail");
                request.getInitiateTxnReq().getBody().setPaymentOffersApplied(paymentOffer);
                nativeSessionUtil.setOrderDetail(txnToken, request.getInitiateTxnReq().getBody());
            }
            return paymentOffer;
        }
        String errorText = (response != null && response.getData() != null) ? response.getData().getPromotext()
                : (response != null && response.getError() != null) ? response.getError().getMessage() : "Promo Error";
        LOGGER.error("apply promo response failure :{}", errorText);
        if (validatePromo) {
            throw BaseException.getException(errorText);
        }
        return null;
    }

    private void populateOfflineTxnIdentifier(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {
        Boolean isOfflineMerchant = requestData.getOfflineMerchant();
        if (isOfflineMerchant != null && isOfflineMerchant)
            flowRequestBean.setIsOfflineTxn(TRUE);
        else
            flowRequestBean.setIsOfflineTxn(FALSE);
    }

    private void setFeeRateFactorForAPIFlow(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        FeeRateFactors feeRateFactors = flowRequestBean.getFeeRateFactors();
        if (feeRateFactors == null) {
            feeRateFactors = new FeeRateFactors();
            flowRequestBean.setFeeRateFactors(feeRateFactors);
        }
        if (!flowRequestBean.getFeeRateFactors().isLink() && !flowRequestBean.getFeeRateFactors().isQr()
                && !ERequestType.OFFLINE.getType().equals(requestData.getRequestType())) {
            flowRequestBean.getFeeRateFactors().setApi(true);
        }
    }

    private void buildBinDetailsForPlatformSavedAssets(SavedCard savedCard, WorkFlowRequestBean flowRequestBean) {

        /*
         * set bin details for saved card with CIN
         */
        BinDetail savedAssetBinDetail = new BinDetail();
        savedAssetBinDetail.setIsIndian(savedCard.getCardDetails().getIndian());
        savedAssetBinDetail.setBin(Long.parseLong(savedCard.getCardDetails().getFirstSixDigit()));
        savedAssetBinDetail.setCardName(savedCard.getCardDetails().getFirstSixDigit());
        savedAssetBinDetail.setOneClickSupported(BooleanUtils.toBoolean(savedCard.getOneClickSupported()));
        savedAssetBinDetail.setPrepaidCard(BooleanUtils.toBoolean(savedCard.getPrepaidCard()));
        savedAssetBinDetail.setCorporateCard(BooleanUtils.toBoolean(savedCard.getCorporateCard())); // need
        savedAssetBinDetail.setBank(savedCard.getInstName());
        savedAssetBinDetail.setZeroSuccessRate(Boolean.parseBoolean(savedCard.getCardDetails().getZeroSuccessRate()));
        savedAssetBinDetail.setCurrency(savedCard.getCardDetails().getCurrency());
        savedAssetBinDetail.setCountry(savedCard.getCardDetails().getCountry());
        savedAssetBinDetail.setSymbol(savedCard.getCardDetails().getSymbol());
        savedAssetBinDetail.setCountryCodeISO(savedCard.getCardDetails().getCountryCodeIso());
        savedAssetBinDetail.setCurrencyPrecision(savedCard.getCardDetails().getCurrencyPrecision());
        savedAssetBinDetail.setCurrencyCodeISO(savedCard.getCardDetails().getCurrencyCodeIso());
        savedAssetBinDetail.setCurrencyCode(savedCard.getCardDetails().getCurrencyCode());
        savedAssetBinDetail.setCardName(savedCard.getCardDetails().getCardScheme());
        savedAssetBinDetail.setCardType(savedCard.getCardDetails().getCardType());
        // litepaview response consists only active cards - PGP-28454
        savedAssetBinDetail.setActive(true);
        LOGGER.info("bin detail for saved asset from platform is {}", savedAssetBinDetail);
        flowRequestBean.setBinDetail(savedAssetBinDetail);

    }

    private void setPostPaidOnAddnPayFlag(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        if (requestData.isCreateOrderForInitiateTxnRequest()) {
            LOGGER.info("returning, call for create Order request");
            return;
        }
        com.paytm.pgplus.facade.merchant.models.MerchantInfo merchantInfo = null;
        try {
            merchantInfo = merchantQueryService.getMerchantProfileMappingData(requestData.getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantProfileMappingData :: {}", merchantInfo);
            if (merchantInfo.getPostPaidOnAddNPay() != null) {
                flowRequestBean.setPostPaidOnAddnPay(merchantInfo.getPostPaidOnAddNPay());
            }
            if (Boolean.valueOf(merchantInfo.getPostPaidOnAddNPay())
                    && ff4JUtils.isFeatureEnabledOnMid(requestData.getMid(),
                            TheiaConstant.FF4J.BLOCK_POSPAID_IN_ADDDMONEYPAYMODES, true) && requestData.isAppInvoke()
                    && !requestData.isAddNPayOnPostpaidSupported()) {
                LOGGER.info(
                        "blocking pospaid on addnpay isAppInvokeFlowOfAddnPayPostPaid():{}, isAddNPayOnPostpaidSupported:{}",
                        requestData.isAppInvoke(), requestData.isAddNPayOnPostpaidSupported());
                flowRequestBean.setPostPaidOnAddnPay(false);
            }
        } catch (Exception e) {
            LOGGER.error("error while fetching merchant profile info for mid :{} ,error {}", requestData.getMid(), e);
        }
    }

    private void checktokenIndexNumberOrCardToken(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData)
            throws TheiaDataMappingException {
        if (flowRequestBean.getPaymentDetails() != null) {
            String paymentDetails = flowRequestBean.getPaymentDetails();
            String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
            int paymentDetailsArrayLength = actualPaymentDetailsArray.length;
            if (paymentDetailsArrayLength == 2) {
                UserInfo userInfo = new UserInfo();
                GenericCoreResponseBean<UserDetailsBiz> userDetails = null;
                if (!org.apache.commons.lang.StringUtils.isBlank(flowRequestBean.getToken())) {
                    // fetch UserDetails
                    if (flowRequestBean.getUserDetailsBiz() != null) {
                        LOGGER.info("UserDetails :: {} ", flowRequestBean.getUserDetailsBiz());
                        userDetails = new GenericCoreResponseBean<>(flowRequestBean.getUserDetailsBiz());
                    } else {
                        LOGGER.info("Fetching UserDetails from login Service");
                        userDetails = loginService.fetchUserDetails(flowRequestBean.getToken(), false, flowRequestBean);
                    }
                    LOGGER.info("UserDetails Response {}", userDetails);
                    flowRequestBean.setUserDetailsBiz(userDetails.getResponse());
                }
                if (flowRequestBean.getUserDetailsBiz() != null
                        && flowRequestBean.getUserDetailsBiz().getUserId() != null) {
                    userInfo.setUserId(flowRequestBean.getUserDetailsBiz().getUserId());
                }
                if (flowRequestBean.getPaymentRequestBean().getUserInfo() != null) {
                    if (flowRequestBean.getPaymentRequestBean().getUserInfo().getFirstName() != null) {
                        userInfo.setFirstName(flowRequestBean.getPaymentRequestBean().getUserInfo().getFirstName());
                    }
                    if (flowRequestBean.getPaymentRequestBean().getUserInfo().getLastName() != null) {
                        userInfo.setLastName(flowRequestBean.getPaymentRequestBean().getUserInfo().getLastName());
                    }
                    if (flowRequestBean.getPaymentRequestBean().getUserInfo().getMobile() != null) {
                        userInfo.setMobileNumber(flowRequestBean.getPaymentRequestBean().getUserInfo().getMobile());
                    }
                }
                userInfo.setCustId(requestData.getCustId());
                GenerateTokenDataRequestBody generateTokenDataRequestBody = new GenerateTokenDataRequestBody("ECOM",
                        actualPaymentDetailsArray[0], userInfo);

                String mid = (StringUtils.isNotBlank(requestData.getSgwReferenceId()) && requestData.isSubscription())
                        && !aoaUtils.isAOAMerchant(requestData.getMid()) ? aoaUtils.getAOAMidForPGMid(requestData
                        .getMid()) : requestData.getMid();

                GenerateTokenDataRequest generateTokenDataRequest = new GenerateTokenDataRequest(
                        new GenerateTokenDataRequestHead(), generateTokenDataRequestBody);
                try {
                    GenerateTokenDataResponse generateTokenDataResponse = coftService.generateTokenData(
                            generateTokenDataRequest, mid);
                    try {
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("RESPONSE_STATUS", generateTokenDataResponse.getBody().getResultInfo()
                                .getResultStatus());
                        responseMap.put("RESPONSE_MESSAGE", generateTokenDataResponse.getBody().getResultInfo()
                                .getResultMsg());
                        statsDUtils.pushResponse("GENERATE_TOKEN_DATA", responseMap);
                    } catch (Exception exception) {
                        LOGGER.error("Error in pushing response message " + "GENERATE_TOKEN_DATA" + "to grafana",
                                exception);
                    }
                    if ("00".equals(generateTokenDataResponse.getBody().getResultInfo().getResultCode())) {
                        if (flowRequestBean.getCardTokenInfo() == null) {
                            flowRequestBean.setCardTokenInfo(new CardTokenInfo());
                        }
                        flowRequestBean.getCardTokenInfo().setCardToken(
                                generateTokenDataResponse.getBody().getCardToken());
                        flowRequestBean.getCardTokenInfo().setTokenExpiry(
                                generateTokenDataResponse.getBody().getTokenInfo().getTokenExpiry());

                        // Risk Integration - coft for tin transaction
                        NativeCashierInfoResponse cashierInfoResponse = populateCashierInfoResponseFromCache(
                                flowRequestBean, requestData, actualPaymentDetailsArray[0]);
                        if (Objects.nonNull(cashierInfoResponse)) {
                            SavedCard saveCard = getSavedCardDetails(cashierInfoResponse, actualPaymentDetailsArray[0]);
                            if (Objects.nonNull(saveCard)) {
                                setRiskExtendInfoForCoft(flowRequestBean, saveCard.getFingerPrint(), saveCard.getPar(),
                                        saveCard.getCardDetails().getFirstSixDigit(), saveCard.getCardDetails()
                                                .getLastFourDigit());

                            }
                        }
                        flowRequestBean.getCardTokenInfo().setCardSuffix(
                                generateTokenDataResponse.getBody().getTokenInfo().getCardSuffix());
                        flowRequestBean.setGcin(generateTokenDataResponse.getBody().getTokenInfo().getGlobalPanIndex());
                        flowRequestBean.getCardTokenInfo().setTAVV(generateTokenDataResponse.getBody().getTavv());
                        flowRequestBean.getCardTokenInfo().setPanUniqueReference(
                                generateTokenDataResponse.getBody().getTokenInfo().getPanUniqueReference());
                        flowRequestBean.getCardTokenInfo().setTokenUniqueReference(
                                generateTokenDataResponse.getBody().getTokenInfo().getTokenUniqueReference());
                        flowRequestBean.getCardTokenInfo().setMerchantTokenRequestorId(
                                generateTokenDataResponse.getBody().getTokenInfo().getTokenRequestorId());

                        String requiredCardDetails = JsonMapper.mapObjectToJson(flowRequestBean.getCardTokenInfo());
                        flowRequestBean.setPaymentDetails(requiredCardDetails);
                        flowRequestBean.setCvv2(actualPaymentDetailsArray[1]);
                        LOGGER.info("flow request bean here is {}", flowRequestBean);
                    } else {
                        throw new TheiaDataMappingException(
                                CoftResponseConstants
                                        .fetchResponseConstantByCoftServiceRespCode(generateTokenDataResponse.getBody()
                                                .getResultInfo().getResultCode()));
                    }
                } catch (TheiaDataMappingException tdme) {
                    LOGGER.error("Exception occurred while generating token details from tokenIndexNumber ", tdme);
                    throw tdme;
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while generating token details from tokenIndexNumber ", e);
                    throw new TheiaDataMappingException(ResponseConstants.SYSTEM_ERROR);
                }

            }
        }
    }

    private void buildCCDCDataForCoftToken(PaymentRequestBean requestBean, WorkFlowRequestBean flowRequestBean)
            throws TheiaDataMappingException {
        if (flowRequestBean.isCoftTokenTxn()) {
            if (StringUtils.isBlank(flowRequestBean.getPaymentTypeId())) {
                LOGGER.error("Invalid paymode found - {}", flowRequestBean.getPaymentTypeId());
                throw new TheiaDataMappingException(ResponseConstants.INVALID_PAYMENTMODE);
            }

            if ((ERequestType.NATIVE.equals(flowRequestBean.getRequestType())
                    || ERequestType.isSubscriptionCreationRequest(flowRequestBean.getRequestType().getType())
                    || ERequestType.SUBSCRIBE.equals(flowRequestBean.getRequestType())
                    || ERequestType.UNI_PAY.equals(flowRequestBean.getRequestType())
                    || ERequestType.ADDMONEY_EXPRESS.equals(flowRequestBean.getRequestType())
                    || ERequestType.PAYTM_EXPRESS.equals(flowRequestBean.getRequestType())
                    || ERequestType.TOPUP_EXPRESS.equals(flowRequestBean.getRequestType())
                    || ERequestType.DYNAMIC_QR.equals(flowRequestBean.getRequestType()) || (ERequestType.SEAMLESS
                    .equals(flowRequestBean.getRequestType()) && TRUE.equals(requestBean.getFromAOARequest())))
                    && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                            || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.EMI.value
                                .equals(flowRequestBean.getPaymentTypeId()))) {
                try {
                    validationService.populateExpiryDate(flowRequestBean, flowRequestBean.getCardTokenInfo()
                            .getTokenExpiry());
                    verifyCoftTokenExpiry(flowRequestBean);
                } catch (PaymentRequestValidationException e) {
                    LOGGER.error("Exception occured while validating payment details for coftToken ", e);
                    throw new TheiaDataMappingException(e.getMessage(), e.getResponseConstants());
                } catch (Exception e) {
                    LOGGER.error("Exception occured while validating payment details for coftToken ", e);
                    throw new TheiaDataMappingException(e.getMessage(), ResponseConstants.INVALID_PAYMENT_DETAILS);
                }
                flowRequestBean.setCardNo(flowRequestBean.getCardTokenInfo().getCardToken());

                if (StringUtils.isBlank(flowRequestBean.getCvv2())) {
                    flowRequestBean.setCvv2(flowRequestBean.getPaymentDetails());
                }
            }

            flowRequestBean.setPar(flowRequestBean.getCardTokenInfo().getPanUniqueReference());
            setPromoBinDetails(flowRequestBean);
            setIrctcId(flowRequestBean, flowRequestBean.getAccountRangeCardBin());

            BinDetail binDetail = setTokenBinDetails(flowRequestBean);
            validateCardTokenInfo(flowRequestBean);
            validateIssuerConfigAllowedOnMid(flowRequestBean.getPaymentRequestBean(), binDetail);
            // Setting payMethod & payOption
            buildPayMethodAndPayOption(requestBean, flowRequestBean);

            setAddOrHybridOption(requestBean, flowRequestBean);

            // Risk integration -coft for token/tavv transaction
            if (Objects.nonNull(flowRequestBean.getCardTokenInfo()) && !flowRequestBean.getIsSavedCard()) {

                String cardBin = null;
                if (Objects.nonNull(binDetail) && Objects.nonNull(binDetail.getBin())) {
                    cardBin = String.valueOf(binDetail.getBin());
                }

                String cin = null;

                if (Objects.nonNull(flowRequestBean.getCardTokenInfo().getPanUniqueReference())
                        && enableGcinOnCoftForRisk(flowRequestBean)) {
                    String mid = (StringUtils.isNotBlank(requestBean.getSgwReferenceId()) && requestBean
                            .isSubscription()) ? aoaUtils.getAOAMidForPGMid(flowRequestBean.getPaytmMID())
                            : flowRequestBean.getPaytmMID();
                    FetchPanUniqueReferenceResponse tokenData = coftTokenDataService.getTokenData(mid, "PAR",
                            flowRequestBean.getCardTokenInfo().getPanUniqueReference(), true);
                    if (Objects.nonNull(tokenData) && Objects.nonNull(tokenData.getBody())) {
                        cin = tokenData.getBody().getCardIndexNumber();
                        if (StringUtils.isEmpty(flowRequestBean.getGcin())) {
                            flowRequestBean.setGcin(tokenData.getBody().getGlobalPanIndex());
                        }
                    }
                }

                setRiskExtendInfoForCoft(flowRequestBean, cin, flowRequestBean.getCardTokenInfo()
                        .getPanUniqueReference(), cardBin, flowRequestBean.getCardTokenInfo().getCardSuffix());
            }
        }
    }

    private void verifyCoftTokenExpiry(WorkFlowRequestBean flowRequestBean) {
        int expiryMonth = flowRequestBean.getExpiryMonth();
        int expiryYear = flowRequestBean.getExpiryYear();
        Calendar now = Calendar.getInstance();
        if (expiryYear < now.get(Calendar.YEAR)) {
            LOGGER.error("INVALID_YEAR for year {} and month {}", expiryYear, expiryMonth);
            throw new PaymentRequestValidationException("Invalid expiry year in coft token",
                    ResponseConstants.INVALID_YEAR);
        }
        if (expiryMonth < now.get(Calendar.MONTH) + 1 && expiryYear == now.get(Calendar.YEAR)) {
            LOGGER.error("INVALID_MONTH for year {} and month {}", expiryYear, expiryMonth);
            throw new PaymentRequestValidationException("Invalid expiry month in coft token",
                    ResponseConstants.INVALID_MONTH);
        }
    }

    private void setPromoBinDetails(WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isNotBlank(flowRequestBean.getCardNo())) {
            BinDetail binDetailForCardBin = processTransactionUtil.getBinDetailForCardBin(flowRequestBean.getCardNo());
            if (binDetailForCardBin.getBinAttributes() != null) {
                String accountRangeCardBin = binDetailForCardBin.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
                flowRequestBean.setAccountRangeCardBin(accountRangeCardBin);
            }
        }
    }

    private void populateEdcEmiDetailsInfo(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean) {

        if (requestData != null && requestData.getLinkDetailsData() != null
                && requestData.getLinkDetailsData().getEdcEmiFields() != null) {
            EdcEmiDetails edcEmiDetails = requestData.getLinkDetailsData().getEdcEmiFields();
            Map<String, String> emiDetailInfo = new HashMap<String, String>();
            if (StringUtils.isNotBlank(requestData.getLinkDetailsData().getEdcEmiFields().getProductId())) {
                emiDetailInfo.put(TheiaConstant.EDCEmiSubvention.EMI_TYPE,
                        TheiaConstant.EDCEmiSubvention.BRAND_EMI_TYPE);
            } else {
                emiDetailInfo
                        .put(TheiaConstant.EDCEmiSubvention.EMI_TYPE, TheiaConstant.EDCEmiSubvention.BANK_EMI_TYPE);
            }
            if (StringUtils.isNotBlank(edcEmiDetails.getBrandName())) {
                emiDetailInfo.put(TheiaConstant.EDCEmiSubvention.BRAND_NAME, edcEmiDetails.getBrandName());
            }
            if (StringUtils.isNotBlank(edcEmiDetails.getCategoryName())) {
                emiDetailInfo.put(TheiaConstant.EDCEmiSubvention.CATEGORY_NAME, edcEmiDetails.getCategoryName());
            }
            if (StringUtils.isNotBlank(edcEmiDetails.getValidationValue())) {
                emiDetailInfo.put(TheiaConstant.EDCEmiSubvention.SERAIL_NO, edcEmiDetails.getValidationValue());
            }
            if (StringUtils.isNotBlank(edcEmiDetails.getModel())) {
                emiDetailInfo.put(TheiaConstant.EDCEmiSubvention.MODEL_NAME, edcEmiDetails.getModel());
            }
            if (edcEmiDetails.getEmiChannelDetail() != null
                    && edcEmiDetails.getEmiChannelDetail().getTotalAmount() != null
                    && StringUtils.isNotBlank(edcEmiDetails.getEmiChannelDetail().getTotalAmount().getValue())
                    && CollectionUtils.isNotEmpty(edcEmiDetails.getEmiChannelDetail().getOfferDetails())
                    && edcEmiDetails.getEmiChannelDetail().getOfferDetails().get(0).getAmount() != null
                    && StringUtils.isNotBlank(edcEmiDetails.getEmiChannelDetail().getOfferDetails().get(0).getAmount()
                            .getValue())) {
                emiDetailInfo.put(
                        TheiaConstant.EDCEmiSubvention.SUBVENTION,
                        AmountUtils.getOfferAmountInPercentage(edcEmiDetails.getEmiChannelDetail().getOfferDetails()
                                .get(0).getAmount().getValue(), edcEmiDetails.getEmiChannelDetail().getTotalAmount()
                                .getValue()));
                emiDetailInfo.put(
                        TheiaConstant.EDCEmiSubvention.SUBVENTION_AMOUNT,
                        AmountUtils.getTransactionAmountInPaise(edcEmiDetails.getEmiChannelDetail().getOfferDetails()
                                .get(0).getAmount().getValue()));
            }
            if (edcEmiDetails.getEmiChannelDetail() != null
                    && edcEmiDetails.getEmiChannelDetail().getTotalAmount() != null
                    && StringUtils.isNotBlank(edcEmiDetails.getEmiChannelDetail().getTotalAmount().getValue())
                    && CollectionUtils.isNotEmpty(edcEmiDetails.getEmiChannelDetail().getBankOfferDetails())
                    && edcEmiDetails.getEmiChannelDetail().getBankOfferDetails().get(0).getAmount() != null
                    && StringUtils.isNotBlank(edcEmiDetails.getEmiChannelDetail().getBankOfferDetails().get(0)
                            .getAmount().getValue())) {
                emiDetailInfo.put(
                        TheiaConstant.EDCEmiSubvention.ADDITIONAL_CASH_BACK,
                        AmountUtils.getOfferAmountInPercentage(edcEmiDetails.getEmiChannelDetail()
                                .getBankOfferDetails().get(0).getAmount().getValue(), edcEmiDetails
                                .getEmiChannelDetail().getTotalAmount().getValue()));
                emiDetailInfo.put(
                        TheiaConstant.EDCEmiSubvention.ADDITIONAL_CASH_BACK_AMOUNT,
                        AmountUtils.getTransactionAmountInPaise(edcEmiDetails.getEmiChannelDetail()
                                .getBankOfferDetails().get(0).getAmount().getValue()));
            }
            flowRequestBean.setEmiDetailInfo(emiDetailInfo);
        }
    }

    private NativeCashierInfoResponse populateCashierInfoResponseFromCache(WorkFlowRequestBean flowRequestBean,
            PaymentRequestBean requestData, String savedCardID) {
        NativeCashierInfoResponse cashierInfoResponse = null;
        try {

            String fpoCacheKey = null;
            if (StringUtils.isNotBlank(requestData.getSgwReferenceId())) {
                if (requestData.isSubscription()) {
                    String aoaMid = ff4JUtil.isFeatureEnabled(CONVERT_PG_TO_AOA_MID_FOR_SUBS_SAVECARD_TXN,
                            requestData.getMid()) ? aoaUtils.getAOAMidForPGMid(requestData.getMid()) : requestData
                            .getMid();
                    fpoCacheKey = SUBS_PREFIX + aoaMid + UNDER_SCORE + requestData.getSgwReferenceId();
                    LOGGER.info("CacheKey For Subs on Saved card Txn for AoaMid {} :", fpoCacheKey, aoaMid);
                    cashierInfoResponse = populateSavedInstrumentInFpoResponse(fpoCacheKey, savedCardID);
                } else {
                    // NATIVE~216820000007895999603~REF164793162050349d89223eb6e4804b5f87e0903e569c3
                    fpoCacheKey = ERequestType.NATIVE.getType() + TILDE + requestData.getMid() + TILDE
                            + requestData.getSgwReferenceId();
                    LOGGER.info("CacheKey For Subs Aoa Mid oneTime Txn  {} :", fpoCacheKey);
                    cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(fpoCacheKey);
                }
            } else {
                cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(flowRequestBean.getTxnToken());
            }

        } catch (Exception e) {
            LOGGER.error("Error in occurred populateCashierInfoResponseFromCache {} ", e.getMessage());
        }
        return cashierInfoResponse;
    }

    private NativeCashierInfoResponse populateSavedInstrumentInFpoResponse(String fpoCacheKey, String savedCardID) {
        NativeCashierInfoResponse cashierInfoResponse = new NativeCashierInfoResponse();
        try {
            String cacheResult = (String) theiaTransactionalRedisUtil.hget(fpoCacheKey, SUBS_JSON_FPO_RESPONSE);
            String savedInstruments = String.valueOf(JsonMapper.getValueFromJson(cacheResult, "savedInstruments"));
            List<SavedCard> savedCards = JsonMapper.readValue(savedInstruments, new TypeReference<List<SavedCard>>() {
            });
            Optional<SavedCard> savedCard = savedCards.stream()
                    .filter((s -> s.getCardDetails().getCardId().equals(savedCardID))).findAny();
            if (savedCard.isPresent()) {
                NativeCashierInfoResponseBody body = new NativeCashierInfoResponseBody();
                PayOption payOption = new PayOption();
                List<PayChannelBase> saveCard = new ArrayList<>();
                saveCard.add(savedCard.get());
                payOption.setSavedInstruments(saveCard);
                body.setMerchantPayOption(payOption);
                cashierInfoResponse.setBody(body);
            }

        } catch (Exception e) {
            LOGGER.error("Exception while extracting SavedCard Data {} : ", e);
        }
        return cashierInfoResponse;
    }

    private void setPG2Preferences(WorkFlowRequestBean flowRequestBean) {
        if (ObjectUtils.notEqual(flowRequestBean.getPaymentRequestBean(), null)) {
            flowRequestBean.setPG2PreferenceEnabledOnMid(true);
            flowRequestBean.setPG2EnabledPaymodesPayoptions(StringUtils.EMPTY);
            flowRequestBean.setFullPg2TrafficEnabled(true);
        }
    }

    private void setRiskExtendInfoForForPlainCardTransaction(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) {
        String paymentDetails = requestData.getPaymentDetails();
        if (StringUtils.isNotEmpty(paymentDetails)) {
            String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
            if (actualPaymentDetailsArray.length == 3) {
                String par = null;
                if (enableGcinOnCoftForRisk(flowRequestBean)
                        && LuhnAlgoImpl.validateCardNumber(actualPaymentDetailsArray[0])) {
                    String encryptedCardDetail = coftTokenDataService.encryptByPublicKey(actualPaymentDetailsArray[0]);
                    if (StringUtils.isNotEmpty(encryptedCardDetail)) {
                        String mid = (StringUtils.isNotBlank(requestData.getSgwReferenceId()) && requestData
                                .isSubscription()) ? aoaUtils.getAOAMidForPGMid(flowRequestBean.getPaytmMID())
                                : flowRequestBean.getPaytmMID();
                        FetchPanUniqueReferenceResponse tokenData = coftTokenDataService.getTokenData(mid, "PAN",
                                encryptedCardDetail, true);
                        par = (Objects.nonNull(tokenData) && Objects.nonNull(tokenData.getBody())) ? tokenData
                                .getBody().getPanUniqueReference() : null;
                        flowRequestBean.setPar(par);
                    }
                }

                setRiskExtendInfoForCoft(flowRequestBean, null, par, null, null);
            }
        }
    }

    private void setRiskExtendInfoForCoft(WorkFlowRequestBean flowRequestBean, String cin, String par, String cardBin,
            String cardSuffix) {

        if (Objects.nonNull(flowRequestBean.getRiskExtendedInfo())) {
            if (StringUtils.isNotEmpty(cin))
                flowRequestBean.getRiskExtendedInfo().put(CIN, cin);
            if (StringUtils.isNotEmpty(par))
                flowRequestBean.getRiskExtendedInfo().put(PAR, par);
            if (StringUtils.isNotEmpty(cardBin))
                flowRequestBean.getRiskExtendedInfo().put(CARD_BIN, cardBin);
            if (StringUtils.isNotEmpty(cardSuffix))
                flowRequestBean.getRiskExtendedInfo().put(LAST_FOUR_DIGITS, cardSuffix);

        }

    }

    private boolean checkAddNPayPreferenceOnMerchant(PaymentRequestBean requestData) {
        return merchantPreferenceProvider.isAddMoneyEnabled(requestData);
    }

    private void setPromoCoftPreference(WorkFlowRequestBean flowRequestBean) {
        if (ObjectUtils.notEqual(flowRequestBean.getPaymentRequestBean(), null)) {
            String merchantCoftConfig = coftTokenDataService.getMerchantConfig(flowRequestBean.getPaymentRequestBean()
                    .getMid());
            flowRequestBean.setMerchantCoftConfig(merchantCoftConfig);
        }
    }

    private void setEnableGcinOnCoftPromo(WorkFlowRequestBean flowRequestBean) {
        boolean enableGcinOnCoftPromo = ff4JUtils.isFeatureEnabledOnMid(flowRequestBean.getPaytmMID(),
                ENABLE_GCIN_ON_COFT_PROMO, false);
        flowRequestBean.setEnableGcinOnCoftPromo(enableGcinOnCoftPromo);
    }

    private boolean enableGcinOnCoftForRisk(WorkFlowRequestBean flowRequestBean) {
        boolean enableGcinOnCoftRisk = ff4JUtil.isFeatureEnabled(ENABLE_GCIN_ON_COFT_RISK,
                flowRequestBean.getPaytmMID());
        return (enableGcinOnCoftRisk && (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId()) || PaymentTypeIdEnum.EMI.value
                    .equals(flowRequestBean.getPaymentTypeId())));
    }

    private void setUltimateBeneficiaryDetails(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        if (ObjectUtils.notEqual(requestData.getUltimateBeneficiaryDetails(), null)) {
            flowRequestBean.setUltimateBeneficiaryDetails(requestData.getUltimateBeneficiaryDetails());
        } else {
            NativeInitiateRequest initiateRequest = nativeSessionUtil.getNativeInitiateRequest(requestData
                    .getTxnToken());
            if (ObjectUtils.notEqual(initiateRequest, null)
                    && ObjectUtils.notEqual(initiateRequest.getInitiateTxnReq(), null)
                    && ObjectUtils.notEqual(initiateRequest.getInitiateTxnReq().getBody(), null)
                    && ObjectUtils.notEqual(initiateRequest.getInitiateTxnReq().getBody()
                            .getUltimateBeneficiaryDetails(), null)
                    && StringUtils.isNotBlank(initiateRequest.getInitiateTxnReq().getBody()
                            .getUltimateBeneficiaryDetails().getUltimateBeneficiaryName())) {

                UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
                ultimateBeneficiaryDetails.setUltimateBeneficiaryName(initiateRequest.getInitiateTxnReq().getBody()
                        .getUltimateBeneficiaryDetails().getUltimateBeneficiaryName());
                flowRequestBean.setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails);
            }
        }
    }

    private void setGlobalVaultCoft(WorkFlowRequestBean flowRequestBean) {
        boolean globalVaultCoft = merchantPreferenceService.isGlobalVaultCoft(flowRequestBean.getPaymentRequestBean()
                .getMid());
        flowRequestBean.setGlobalVaultCoftPreference(globalVaultCoft);
    }

    private void setDisableLimitCCAddNPay(WorkFlowRequestBean flowRequestBean) {
        boolean isDisableLimitCCAddNPay = merchantPreferenceService.isDisableLimitCCAddNPay(flowRequestBean
                .getPaymentRequestBean().getMid());
        flowRequestBean.setDisableLimitCCAddNPay(isDisableLimitCCAddNPay);
    }

    private void validateCardTokenInfo(WorkFlowRequestBean flowRequestBean) {
        if (BooleanUtils.isFalse(processTransactionUtil.isValidCardTokenInfo(flowRequestBean.getCardTokenInfo(),
                flowRequestBean.getCardScheme()))) {
            boolean isNativeJsonRequest = processTransactionUtil.isNativeJsonRequest(flowRequestBean
                    .getPaymentRequestBean());
            boolean isNativeEnhanceFlow = processTransactionUtil.isNativeEnhancedRequest(flowRequestBean
                    .getPaymentRequestBean());
            boolean sendHtml = isNativeJsonRequest || isNativeEnhanceFlow ? false : true;
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(sendHtml).isRetryAllowed(true)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }
    }

    private void validateIssuerConfigAllowedOnMid(PaymentRequestBean requestData, BinDetail binDetail) {
        boolean sendHTML = !processTransactionUtil.isNativeJsonRequest(requestData)
                && !processTransactionUtil.isNativeEnhancedRequest(requestData);
        boolean isAddnPayOrAddMoneyFlow = TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.isAddMoney())
                || requestData.isNativeAddMoney()
                || (StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID)) && ConfigurationUtil
                        .getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(requestData.getMid()));
        String issuerConfig = requestData.getMid() + "." + binDetail.getCardType() + "." + binDetail.getCardName()
                + "." + binDetail.getBankCode();
        processTransactionUtil.checkIfIssuerAllowedOnMid(requestData.getMid(),
                ERequestType.getByRequestType(requestData.getRequestType()), issuerConfig, isAddnPayOrAddMoneyFlow,
                sendHTML);
    }

    public void setUPIIntentRetriesAllowed(WorkFlowRequestBean workFlowRequestBean) {
        String merchantId = workFlowRequestBean.getPaytmMID();
        String UPIIntentRetriesAllowed = null;
        try {
            if (merchantDataService.getMerchantPreferenceInfoExt(merchantId, MerchantExtendInfoKeys.INTENT_RETRY_PAYTM) != null) {
                PerfernceInfo prefInfo = merchantDataService.getMerchantPreferenceInfoExt(merchantId,
                        MerchantExtendInfoKeys.INTENT_RETRY_PAYTM);
                if (prefInfo != null && CollectionUtils.isNotEmpty(prefInfo.getMerchantPreferenceInfos())) {
                    UPIIntentRetriesAllowed = prefInfo.getMerchantPreferenceInfos().get(0).getPrefValue();
                    workFlowRequestBean.setUpiIntentAllowedRetries(UPIIntentRetriesAllowed);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Error while fetching UPI Intent Retries Allowed from merchant Data Service: {}",
                    e.getMessage());
        }
    }
}
