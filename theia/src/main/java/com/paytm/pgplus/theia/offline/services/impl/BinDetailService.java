package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.enums.DirectPaymentVerificationMethod;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.BinDetailWithDisplayName;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.request.CardDetail;
import com.paytm.pgplus.facade.payment.models.request.FetchCardLimitsRequest;
import com.paytm.pgplus.facade.payment.models.response.CardLimit;
import com.paytm.pgplus.facade.payment.models.response.FetchCardLimitsResponse;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.subscriptionClient.enums.SubscriptionRequestType;
import com.paytm.pgplus.subscriptionClient.utils.SubscriptionUtil;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequest;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequestBody;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequestHead;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.payview.StatusInfo;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequestBody;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.services.IBinDetailService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ISELIGLIBLEFORCOFT_LOGIC;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_PG2_CARD_LIMIT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.FALSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRUE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.ONE_CLICK_MAX_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PrepaidCard.PREPAID_CARD_MAX_AMOUNT;

@Service("binDetailService")
public class BinDetailService implements IBinDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinDetailService.class);
    private static final String ZERO_SUCCESS_RATE_DISPLAY_MESSAGE = "zero.success.rate.display.message";
    private static boolean isException = false;

    public static boolean isIsException() {
        return isException;
    }

    public static void setIsException(boolean isException) {
        BinDetailService.isException = isException;
    }

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    @Qualifier("subscriptionUtil")
    private SubscriptionUtil subscriptionUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private CorporateCardUtil corporateCardUtil;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private PG2Util pg2Util;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Override
    public BinDetailResponse fetchBinDetails(BinDetailRequest request) {

        BinDetailRequestBody requestBody = request.getBody();
        String bin = requestBody.getBin();
        boolean isZeroSR = false;

        validateBin(bin);

        BinDetail binDetail = null;
        BinDetailWithDisplayName binDetailWithDisplayName = null;
        try {
            LOGGER.info("Calling Mapping-Service for fetching bin details");
            binDetailWithDisplayName = cardUtils.fetchBinDetailsWithDisplayName(bin);
            binDetail = buildBinDetail(binDetailWithDisplayName, bin);

        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", bin, exception);
            throw BinDetailException.getException();
        }

        BinDetailResponse response = new BinDetailResponse();

        BinDetailResponseBody responseBody = new BinDetailResponseBody();
        response.setBody(responseBody);

        BinData binData = getBinData(responseBody, binDetail);
        binData = setCoftEligibilityOnBin(binData, requestBody.getMid(), binDetail);
        responseBody.setBinDetail(binData);

        if (binDetailWithDisplayName != null) {
            isZeroSR = binDetailWithDisplayName.isZeroSuccessRate();

            if (isZeroSR) {
                Map<String, String> metaData = new HashMap<>();
                metaData.put("BIN", String.valueOf(binDetail.getBin()));
                EventUtils.pushTheiaEvents(EventNameEnum.ZERO_SR_BIN_FETCHED, metaData);
            }
        }

        populateSuccessRate(binData, responseBody, isZeroSR);

        responseBody.setIconUrl(commonFacade.getLogoUrl(binData.getChannelName(), request.getBody().getChannelId()));
        validateBinForSubscriptionRequest(binDetail, response, request);

        populateOneClickData(responseBody, binDetail);
        populateCardSubTypesData(responseBody, binDetail);
        populateRemainingLimit(responseBody, request, binDetail);
        return response;
    }

    private void populateCardSubTypesData(BinDetailResponseBody responseBody, BinDetail binDetail) {
        List<String> supportedCardSubTypes = corporateCardUtil.prepareCardSubTypeList(binDetail);
        if (CollectionUtils.isNotEmpty(supportedCardSubTypes)) {
            responseBody.setSupportedCardSubTypes(supportedCardSubTypes);
        }
    }

    private void populateOneClickData(BinDetailResponseBody responseBody, BinDetail binDetail) {
        responseBody.setOneClickSupported(binDetail.isOneClickSupported());

        responseBody.setOneClickMaxAmount(ConfigurationUtil.getProperty(ONE_CLICK_MAX_AMOUNT, "2000"));

        responseBody.setPrepaidCard(binDetail.isPrepaidCard());
        responseBody.setPrepaidCardMaxAmount(ConfigurationUtil.getProperty(PREPAID_CARD_MAX_AMOUNT, "100000"));
    }

    private void validateBinForSubscriptionRequest(BinDetail binDetail, BinDetailResponse binDetailResponse,
            BinDetailRequest request) {
        if (request != null && request.getBody() != null && request.getBody().getRequestType() != null
                && ERequestType.isSubscriptionCreationRequest(request.getBody().getRequestType().getType())) {
            SubsPaymentMode subsPayMode = SubsPaymentMode.getSubsPaymentMode(request.getBody().getPayMode());
            if (subsPayMode == null) {
                subsPayMode = SubsPaymentMode.NORMAL;
            }
            if (aoaUtils.isAOAMerchant(request.getBody().getMid())) {
                if (StringUtils.isNotEmpty(subsPayMode.name())) {
                    if (!workFlowHelper.isBinValid(binDetail.getBin().toString())
                            || !Boolean.parseBoolean(binDetailResponse.getBody().getBinDetail().getIsEligibleForCoft())) {
                        binDetailResponse.getBody().setErrorMsg(
                                TheiaConstant.ErrorMsgs.BIN_NOT_SUPPORT_SUBSCRIPTION_MSG);
                        binDetailResponse.getBody().setIsSubscriptionAvailable(false);
                    } else {
                        binDetailResponse.getBody().setIsSubscriptionAvailable(true);
                    }
                }
            } else {
                if (!subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail, subsPayMode,
                        SubscriptionRequestType.CREATE)
                        || !Boolean.parseBoolean(binDetailResponse.getBody().getBinDetail().getIsEligibleForCoft())) {
                    binDetailResponse.getBody().setErrorMsg(TheiaConstant.ErrorMsgs.BIN_NOT_SUPPORT_SUBSCRIPTION_MSG);
                    binDetailResponse.getBody().setIsSubscriptionAvailable(false);
                } else {
                    binDetailResponse.getBody().setIsSubscriptionAvailable(true);
                }
            }
        }
    }

    BinDetail buildBinDetail(BinDetailWithDisplayName binDetailWithDisplayName, String bin)
            throws PaytmValidationException {
        BinDetail binDetail;
        if (binDetailWithDisplayName != null) {
            binDetail = new BinDetail();
            binDetail.setId(binDetailWithDisplayName.getId());
            binDetail.setCardEnabled(binDetailWithDisplayName.isCardEnabled());
            binDetail.setActive(binDetailWithDisplayName.isActive());
            binDetail.setBin(binDetailWithDisplayName.getBin());
            binDetail.setIsIndian(binDetailWithDisplayName.getIsIndian());
            Map<String, Object> context = new HashMap<>();
            context.put(TheiaConstant.RequestParams.Native.MID, String.valueOf(binDetailWithDisplayName.getBin()));
            if (StringUtils.isNotBlank(binDetailWithDisplayName.getDisplayBankName())
                    && iPgpFf4jClient.checkWithdefault(
                            TheiaConstant.FF4J.RETURN_BIN_DISPLAY_BANK_NAME_IN_FETCHBINDETAIL, context, false)) {
                binDetail.setBank(binDetailWithDisplayName.getDisplayBankName());
            } else {
                binDetail.setBank(binDetailWithDisplayName.getBank());
            }
            binDetail.setCardType(binDetailWithDisplayName.getCardType());
            binDetail.setCardName(binDetailWithDisplayName.getCardName());
            binDetail.setBankCode(binDetailWithDisplayName.getBankCode());
            binDetail.setInstId(binDetailWithDisplayName.getInstId());
            binDetail.setiDebitEnabled(binDetailWithDisplayName.isiDebitEnabled());
            binDetail.setCcDirectEnabled(binDetailWithDisplayName.isCcDirectEnabled());
            binDetail.setOneClickSupported(binDetailWithDisplayName.isOneClickSupported());
            binDetail.setPrepaidCard(binDetailWithDisplayName.isPrepaidCard());
            binDetail.setCorporateCard(binDetailWithDisplayName.isCorporateCard());
            binDetail.setBinTokenization(binDetailWithDisplayName.isBinTokenization());
            binDetail.setCountryCode(binDetailWithDisplayName.getCountryCode());
            binDetail.setCountry(binDetailWithDisplayName.getCountry());
            binDetail.setCountryCodeISO(binDetailWithDisplayName.getCountryCodeISO());
            binDetail.setCurrencyCode(binDetailWithDisplayName.getCurrencyCode());
            binDetail.setCurrency(binDetailWithDisplayName.getCurrency());
            binDetail.setCurrencyCodeISO(binDetailWithDisplayName.getCurrencyCodeISO());
            binDetail.setSymbol(binDetailWithDisplayName.getSymbol());
            binDetail.setBinAttributes(binDetailWithDisplayName.getBinAttributes());
            return binDetail;
        } else
            return cardUtils.fetchBinDetails(bin);
    }

    private void validateBin(String bin) {
        if (StringUtils.isBlank(bin) || (bin.length() < 6) || !StringUtils.isNumeric(bin)) {
            LOGGER.error("Error while fetching bin Information for bin {} :", bin);
            throw BinDetailException.getException();
        }
    }

    @Override
    public void validateBinDetails(BinDetailsRequest request) {
        BinDetailsRequestHead head = request.getHead();
        BinDetailsRequestBody body = request.getBody();

        if (head == null) {
            LOGGER.error("Error while validating the bin details, Request Head {}", head);
            throw BinDetailException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        if (body == null) {
            LOGGER.error("Error while validating the bin details, Request Body {}", body);
            throw BinDetailException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        if (StringUtils.isBlank(body.getBin()) || (body.getBin().length() < 6) || !StringUtils.isNumeric(body.getBin())) {
            LOGGER.error("Error while fetching bin Information for bin {} :", body.getBin());
            throw BinDetailException.getException(ResultCode.BIN_NUMBER_EXCEPTION);
        }

        if (StringUtils.isBlank(head.getMid())) {
            LOGGER.error("Error while validating MID {} for bin {} :", head.getMid(), body.getBin());
            throw BinDetailException.getException(ResultCode.INVALID_MID);
        }

        if (StringUtils.isBlank(body.getOrderId())) {
            LOGGER.error("Error while validating OrderId {} for bin {} :", body.getOrderId(), body.getBin());
            throw BinDetailException.getException(ResultCode.INVALID_ORDERID);
        }

        if (StringUtils.isBlank(body.getChecksum()) || (!validateChecksum(request))) {
            LOGGER.error("Error while checking Checksum {} for bin {} :", body.getChecksum(), body.getBin());
            throw BinDetailException.getException(ResultCode.INVALID_CHECKSUM);
        }

    }

    public boolean validateChecksum(BinDetailsRequest request) {
        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put(TheiaConstant.ResponseConstants.BIN, request.getBody().getBin());
        requestMap.put(TheiaConstant.RequestParams.MID, request.getHead().getMid());
        requestMap.put(TheiaConstant.RequestParams.ORDER_ID, request.getBody().getOrderId());
        return checksumService.validateChecksum(request, requestMap);
    }

    public void populateSuccessRate(BinData binData, BinDetailResponseBody responseBody, boolean isZeroSR) {
        if (isZeroSR) {
            String message = getZeroSuccessRateMessage(binData.getChannelName(), binData.getPayMethod());

            StatusInfo hasZeroSuccessRate = new StatusInfo("true", message);
            responseBody.setHasLowSuccessRate(hasZeroSuccessRate);
        } else if (successRateUtils.checkIfLowSuccessRate(binData.getChannelName(),
                PayMethod.getPayMethodByMethod(binData.getPayMethod()))) {
            String message = getLowSuccessRateMessage(binData.getChannelName(), binData.getPayMethod());

            StatusInfo hasLowSuccessRate = new StatusInfo("true", message);
            responseBody.setHasLowSuccessRate(hasLowSuccessRate);
        } else if (successRateUtils.checkIfLowSuccessRate(binData.getIssuingBankCode(),
                PayMethod.getPayMethodByMethod(binData.getPayMethod()))) {

            String message = getLowSuccessRateMessage(binData.getIssuingBankCode(), binData.getPayMethod());

            StatusInfo hasLowSuccessRate = new StatusInfo("true", message);
            responseBody.setHasLowSuccessRate(hasLowSuccessRate);
        } else {
            StatusInfo hasLowSuccessRate = new StatusInfo("false", "");
            responseBody.setHasLowSuccessRate(hasLowSuccessRate);
        }
    }

    private String getLowSuccessRateMessage(String bank, String method) {

        EPayMethod payMethod = EPayMethod.getPayMethodByMethod(method);

        if (null != payMethod) {
            if (StringUtils.isNotBlank(payMethod.getNewDisplayName())) {
                method = payMethod.getNewDisplayName();
            }
        }

        String message = ConfigurationUtil.getMessageProperty("low.success.rate.display.message",
                "We are observing high failures on " + bank + " " + method
                        + " You can continue or try another payment option.");

        if (message.contains("@BANK @METHOD")) {
            message = message.replace("@BANK", bank);
            message = message.replace("@METHOD", method);
        }

        return message;
    }

    public BinData getBinData(BinDetailResponseBody responseBody, BinDetail binDetail) {
        BinData binData = new BinData();

        if (binDetail == null) {
            return binData;
        }

        binData.setIsActive(String.valueOf(binDetail.isActive()));
        binData.setIssuingBank(binDetail.getBank());
        binData.setIssuingBankCode(binDetail.getBankCode());
        binData.setBin(String.valueOf(binDetail.getBin()));
        binData.setChannelName(binDetail.getCardName());
        binData.setPayMethod(binDetail.getCardType());
        binData.setChannelCode(binDetail.getInstId());
        binData.setIsIndian(String.valueOf(binDetail.getIsIndian()));
        binData.setBinTokenization(String.valueOf(binDetail.isBinTokenization()));
        binData.setCountryCode(binDetail.getCountryCode());
        binData.setCountryName(binDetail.getCountry());
        binData.setCountryNumericCode(binDetail.getCountryCodeISO());
        binData.setCurrencyCode(binDetail.getCurrencyCode());
        binData.setCurrencyName(binDetail.getCurrency());
        binData.setCurrencyNumericCode(binDetail.getCurrencyCodeISO());
        binData.setCurrencySymbol(binDetail.getSymbol());

        return binData;
    }

    private String getZeroSuccessRateMessage(String bank, String method) {
        EPayMethod payMethod = EPayMethod.getPayMethodByMethod(method);

        if (null != payMethod) {
            if (StringUtils.isNotBlank(payMethod.getNewDisplayName())) {
                method = payMethod.getNewDisplayName();
            }
        }

        String message = ConfigurationUtil
                .getMessageProperty(
                        ZERO_SUCCESS_RATE_DISPLAY_MESSAGE,
                        "We are observing high failures on transacting through this type of debit/credit card right now."
                                + " We strongly recommend using a different card or payment method for completing this payment.");

        if (message.contains("@METHOD")) {
            message = message.replace("@METHOD", method);
        }
        return message;
    }

    @Override
    public void checkAndAddIDebitOption(HttpServletRequest servletRequest, BinDetailsRequest binDetailsRequest,
            BinDetailResponse response) {
        try {
            BinDetailResponseBody body = response.getBody();
            List<String> authModes = new ArrayList<>();
            authModes.add(DirectPaymentVerificationMethod.OTP.getValue());
            if (EPayMethod.DEBIT_CARD.getMethod().equals(body.getBinDetail().getPayMethod())
                    && isDirectChannelEnabled(servletRequest, response, binDetailsRequest)) {
                authModes.add(DirectPaymentVerificationMethod.ATM.getValue());
            }
            body.setAuthModes(authModes);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private boolean isDirectChannelEnabled(HttpServletRequest servletRequest, BinDetailResponse binDetailResponse,
            BinDetailsRequest request) {
        try {
            EntityPaymentOptionsTO entityPaymentOptions = getEntityPaymentOption(servletRequest, request);
            BinData binData = binDetailResponse.getBody().getBinDetail();
            if (null == entityPaymentOptions) {
                LOGGER.error("Unable to populate direct channel for bin as data does not exist: {}", binData.getBin());
                return false;
            }
            return theiaSessionDataService.isDirectChannelEnabled(binData.getIssuingBankCode(), binData.getPayMethod(),
                    entityPaymentOptions.getDirectServiceInsts(), false, entityPaymentOptions.getSupportAtmPins());

        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private EntityPaymentOptionsTO getEntityPaymentOption(HttpServletRequest servletRequest, BinDetailsRequest request) {
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        if (StringUtils.isEmpty(request.getHead().getMid())) {
            return entityPaymentOptionsTO;
        }
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> responseBean = litePayViewConsult(servletRequest,
                request);
        if (responseBean == null) {
            return entityPaymentOptionsTO;
        }
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = responseBean.getResponse();
        if (litePayviewConsultResponseBizBean != null
                && CollectionUtils.isNotEmpty(litePayviewConsultResponseBizBean.getPayMethodViews())) {
            for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getPayMethodViews()) {
                if (PayMethod.DEBIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())
                        && CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayChannelOptionViews())) {
                    for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                        if (CollectionUtils.isNotEmpty(payChannelOptionViewBiz.getDirectServiceInsts())) {
                            for (String channel : payChannelOptionViewBiz.getDirectServiceInsts()) {
                                entityPaymentOptionsTO.getDirectServiceInsts().add(channel + "@DEBIT_CARD");
                                entityPaymentOptionsTO.getSupportAtmPins().add(channel);
                            }
                        }
                    }
                }
            }
        }
        return entityPaymentOptionsTO;
    }

    @Override
    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayViewConsult(
            HttpServletRequest servletRequest, BinDetailsRequest request) {
        BinDetailsRequestHead head = request.getHead();

        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        workFlowTransBean.setDefaultLiteViewFlow(true);
        flowRequestBean.setRequestType(ERequestType.DEFAULT);

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(head.getMid());

        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            flowRequestBean.setAlipayMID(merchantMappingResponse.getResponse().getAlipayId());
        } else {
            final String error = merchantMappingResponse == null ? "Could not map merchant" : merchantMappingResponse
                    .getFailureMessage();
            throw new PaymentRequestValidationException(error, ResponseConstants.INVALID_MID);
        }

        flowRequestBean.setPaytmMID(head.getMid());

        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(servletRequest);
        flowRequestBean.setEnvInfoReqBean(envInfo);

        final GenericCoreResponseBean<LitePayviewConsultResponseBizBean> merchantLiteConsultPayView = workFlowHelper
                .litePayviewConsult(workFlowTransBean);

        if (!merchantLiteConsultPayView.isSuccessfullyProcessed()) {
            LOGGER.debug("Error occured while litePayViewConsult {} ", merchantLiteConsultPayView.getFailureMessage());
            return null;
        }

        return merchantLiteConsultPayView;
    }

    @Override
    public String generateResponseForExceptionCases(BinDetailsRequest requestData, BaseException exception) {
        BinDetailResponse response = new BinDetailResponse();

        // Setting response body info
        BinDetailResponseBody body = new BinDetailResponseBody();
        // body.setSignature(requestData.getBody().getSignature());
        body.setResultInfo(exception.getResultInfo());

        // Preparing final response data
        ResponseHeader header = new ResponseHeader();
        header.setResponseTimestamp(System.currentTimeMillis());
        response.setHead(header);
        response.setBody(body);
        String jsonResponse = null;
        try {
            jsonResponse = JsonMapper.mapObjectToJson(response);
        } catch (FacadeCheckedException e) {
            LOGGER.error("JSON mapping exception", e);
        }

        LOGGER.info("JSON Response generated : {}", jsonResponse);
        return jsonResponse;
    }

    @Override
    public BinDetailResponse fetchBinDetailsWithSuccessRateforThirdparty(BinDetailRequest request) {
        BinDetailRequestBody requestBody = request.getBody();

        String bin = requestBody.getBin();
        boolean isZeroSR = false;

        validateBin(bin);

        BinDetail binDetail = null;
        BinDetailWithDisplayName binDetailWithDisplayName = null;
        try {
            binDetailWithDisplayName = cardUtils.fetchBinDetailsWithDisplayName(bin);
            binDetail = buildBinDetail(binDetailWithDisplayName, bin);

        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", bin, exception);
            throw BinDetailException.getException();
        }

        BinDetailResponse response = new BinDetailResponse();

        BinDetailResponseBody responseBody = new BinDetailResponseBody();
        response.setBody(responseBody);

        BinData binData = getBinData(responseBody, binDetail);
        responseBody.setBinDetail(binData);

        if (binDetailWithDisplayName != null) {
            isZeroSR = binDetailWithDisplayName.isZeroSuccessRate();

            if (isZeroSR) {
                Map<String, String> metaData = new HashMap<>();
                metaData.put("BIN", String.valueOf(binDetail.getBin()));
                EventUtils.pushTheiaEvents(EventNameEnum.ZERO_SR_BIN_FETCHED, metaData);
            }
        }
        responseBody.setZeroSuccessRate(isZeroSR);
        try {
            populateSuccessRate(binData, responseBody, isZeroSR);
        } catch (Exception e) {
            this.setIsException(true);
        }
        responseBody.setIconUrl(commonFacade.getLogoUrl(binData.getChannelName(), request.getBody().getChannelId()));
        return response;
    }

    private BinData setCoftEligibilityOnBin(BinData binData, String mid, BinDetail binDetail) {
        if (ff4JUtil.isFeatureEnabled(ENABLE_ISELIGLIBLEFORCOFT_LOGIC, mid)) {
            boolean isMerchantOnPaytm = isOnusMerchant(mid);
            boolean isEligibleForCoft = false;
            boolean isCoftBinEligible = true;
            boolean isGlobalCoftPrefrenceEnabled = merchantPreferenceService.isGlobalVaultCoft(mid);
            if (binDetail.getBinAttributes() != null) {
                if (StringUtils.isNotBlank(binDetail.getBinAttributes().get(BizConstant.IS_ELIGIBLE_FOR_COFT))) {
                    isEligibleForCoft = Boolean.valueOf(binDetail.getBinAttributes().get(
                            BizConstant.IS_ELIGIBLE_FOR_COFT));
                }
                if (StringUtils.isNotBlank(binDetail.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE))) {
                    isCoftBinEligible = Boolean.valueOf(binDetail.getBinAttributes().get(
                            BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE));
                }
            }
            if (isEligibleForCoft && isCoftBinEligible) {
                if (isMerchantOnPaytm || isGlobalCoftPrefrenceEnabled) {
                    binData.setIsEligibleForCoft(TRUE);
                } else {
                    binData.setIsEligibleForCoft(String.valueOf(mappingUtil.getCoftEligibilityForMerchant(mid,
                            binData.getChannelName())));
                }
            } else {
                binData.setIsEligibleForCoft(FALSE);
            }
        } else {
            binData.setIsEligibleForCoft(String.valueOf(iPgpFf4jClient.checkWithdefault("theia.getTokenizedCardsInFPO",
                    null, true)
                    && (binDetail.getBinAttributes() != null && Boolean.parseBoolean(binDetail.getBinAttributes().get(
                            BizConstant.IS_ELIGIBLE_FOR_COFT)))
                    && (mappingUtil.getCoftEligibilityForMerchant(mid, binData.getChannelName()) || ff4JUtil
                            .checkFf4jFeature(mid))));
            if (merchantPreferenceProvider.isBinEligibleCoft(mid)) {
                if (binDetail.getBinAttributes() != null
                        && StringUtils.isNotBlank(binDetail.getBinAttributes().get(
                                BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE))) {
                    boolean isBinEligibleCoft = Boolean.valueOf(binDetail.getBinAttributes().get(
                            BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE));
                    binData.setIsEligibleForCoft(String.valueOf(Boolean.valueOf(binData.getIsEligibleForCoft())
                            && isBinEligibleCoft));
                }
            }
        }
        if (merchantPreferenceProvider.isAccountRangeCardBinSharingEnable(mid)) {
            if (binDetail.getBinAttributes() != null
                    && StringUtils.isNotBlank(binDetail.getBinAttributes().get(
                            BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN))) {
                binData.setCardPrefix(binDetail.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN));
            }
        }
        return binData;
    }

    private void populateRemainingLimit(BinDetailResponseBody responseBody, BinDetailRequest request,
            BinDetail binDetail) {
        try {
            if (request != null && request.getBody() != null) {
                String mid = request.getBody().getMid();
                boolean isFullyMigratedMerchant = merchantPreferenceService.isFullPg2TrafficEnabled(mid);
                Routes routes = pg2Util.getRouteForLpvRequest(isFullyMigratedMerchant, mid);
                boolean pg2CardLimitEnabled = ff4JUtil.isFeatureEnabled(ENABLE_PG2_CARD_LIMIT, mid);
                if (routes.equals(Routes.PG2) && pg2CardLimitEnabled) {
                    FetchCardLimitsRequest fetchCardLimitsRequest = new FetchCardLimitsRequest();
                    FetchCardLimitsResponse fetchCardLimitsResponse = new FetchCardLimitsResponse();
                    CardDetail cardDetail = new CardDetail();
                    List<CardDetail> cardDetails = new ArrayList<>();
                    if (binDetail != null) {
                        String cardType = workFlowHelper.getCardTypeByOldName(binDetail.getCardType());
                        cardDetail.setCardType(cardType);
                        cardDetail.setScheme(binDetail.getCardName());
                        cardDetail.setDomesticCard(binDetail.getIsIndian());
                        cardDetail.setBinId(String.valueOf(binDetail.getBin()));
                        cardDetail.setPrepaidCard(responseBody.getPrepaidCard());
                        cardDetail.setCorporateCard(binDetail.isCorporateCard());
                        cardDetails.add(cardDetail);
                        fetchCardLimitsRequest.setCardDetails(cardDetails);
                        fetchCardLimitsRequest.setMerchantId(mid);
                    }
                    if (StringUtils.isNotBlank(fetchCardLimitsRequest.getMerchantId())
                            && CollectionUtils.isNotEmpty(fetchCardLimitsRequest.getCardDetails())) {
                        fetchCardLimitsResponse = bizPaymentService.fetchCardLimit(fetchCardLimitsRequest);
                    }
                    if (fetchCardLimitsResponse != null
                            && fetchCardLimitsResponse.getResultInfo() != null
                            && StringUtils.equals(BizConstant.SUCCESS, fetchCardLimitsResponse.getResultInfo()
                                    .getResultCode())) {
                        List<CardLimit> cardLimits = fetchCardLimitsResponse.getCardLimits();
                        if (CollectionUtils.isNotEmpty(cardLimits)) {
                            responseBody.setRemainingLimit(cardLimits.get(0).getRemainingLimit());
                            LOGGER.info("Remaining Limit Fetched is : {}", responseBody.getRemainingLimit());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching remaining limit for this card : {}", e.getMessage());
        }
    }

    private boolean isOnusMerchant(String mid) {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse;
        try {
            merchantExtendedInfoResponse = merchantDataService.getMerchantExtendedData(mid);
            if (Objects.nonNull(merchantExtendedInfoResponse)
                    && Objects.nonNull(merchantExtendedInfoResponse.getExtendedInfo())
                    && Objects.nonNull(merchantExtendedInfoResponse.getExtendedInfo().isOnPaytm())) {
                return merchantExtendedInfoResponse.getExtendedInfo().isOnPaytm();
            }
        } catch (MappingServiceClientException e) {
            LOGGER.error("Error occurred while fetching data from mapping service for isOnusMerchant");
        }
        return false;
    }

}
