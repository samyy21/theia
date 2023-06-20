package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.enums.ChannelType;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.cache.model.EmiDetailRequest;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.UPSHelper;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.payview.emi.*;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.EmiUtil;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.common.enums.ETerminalType.getTerminalTypeByTerminal;

/**
 * Created by charu on 07/10/18.
 */

@Service
public class CheckEMIEligibilityProcessor
        extends
        AbstractRequestProcessor<EMIEligibilityRequest, EMIEligibilityResponse, EMIEligibilityRequest, EMIEligibilityResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckEMIEligibilityProcessor.class);

    @Autowired
    private IAuthentication authFacade;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("emiUtil")
    private EmiUtil emiUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private PG2Util pg2Util;

    private static final List<String> excludedPayMethodList = new ArrayList<>();

    @PostConstruct
    public void prepareExclusionList() {
        for (EPayMethod payMethod : EPayMethod.values()) {
            if (!(EPayMethod.EMI_DC.equals(payMethod) || EPayMethod.EMI.equals(payMethod))) {
                excludedPayMethodList.add(payMethod.getMethod());
            }
        }
    }

    @Override
    protected EMIEligibilityRequest preProcess(EMIEligibilityRequest request) {
        validate(request);
        return request;
    }

    @Override
    protected EMIEligibilityResponse onProcess(EMIEligibilityRequest request, EMIEligibilityRequest serviceRequest) {

        String channelCode = serviceRequest.getBody().getChannelCode();
        String payMethod = serviceRequest.getBody().getPayMethod();

        // checking if a valid sso token is passed or not
        String ssoToken = null;
        if (TokenType.SSO.getType().equals(request.getHead().getTokenType())
                && StringUtils.isNotBlank(request.getHead().getToken())) {
            ssoToken = serviceRequest.getHead().getToken();
        } else {
            ssoToken = serviceRequest.getBody().getUserInfo().getSsoToken();
        }

        UserDetails userDetails = validateSSOToken(ssoToken, serviceRequest.getBody().getMid());

        // fetching merchant consult
        LitePayviewConsultResponseBizBean litePayviewConsultResponse = fetchMerchantConsult(serviceRequest, userDetails);

        if (StringUtils.isNotBlank(request.getBody().getEmiId())
                || !CollectionUtils.isEmpty(request.getBody().getEmiTypes())) {
            return populateEmiSubventionResponse(request, litePayviewConsultResponse, userDetails);
        } else {
            EPayMethod payMode = EPayMethod.getPayMethodByMethod(serviceRequest.getBody().getPayMethod());
            PayMethodViewsBiz payMethodViewsBiz = litePayviewConsultResponse.getPayMethodViews().stream()
                    .filter(paymethod -> payMode.getMethod().equals(paymethod.getPayMethod())).findAny().orElse(null);

            if (payMethodViewsBiz == null || CollectionUtils.isEmpty(payMethodViewsBiz.getPayChannelOptionViews())) {
                // paymethod received is not configured on merchant
                LOGGER.error("Emi not configured on mid {}", serviceRequest.getBody().getMid());
                throw PaymentRequestProcessingException.getException(ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT);
            }

            PayChannelOptionViewBiz payChannelOptionView = payMethodViewsBiz.getPayChannelOptionViews().stream()
                    .filter(payChannelOption -> channelCode.equals(payChannelOption.getInstId())).findAny()
                    .orElse(null);

            if (payChannelOptionView == null) {
                // no emi with channelCode in request is configured on merchant
                LOGGER.error("ChannelCode {} not configured on merchant", serviceRequest.getBody().getChannelCode());
                throw PaymentRequestProcessingException.getException(ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT);
            }

            if (EPayMethod.EMI.getMethod().equals(payMethod)) {
                // validation finished, user is eligible for emi prepare final
                // response
                return getSuccessResponse();
            }

            if (EPayMethod.EMI_DC.getMethod().equals(payMethod)
                    && emiUtil.isUserEligibleforEmiOnDc(userDetails.getMobileNo(), channelCode)) {
                return getSuccessResponse();
            }
            LOGGER.info("User not eligible for EMI");
            return getFailureResponse();
        }

    }

    private EMIEligibilityResponse populateEmiSubventionResponse(EMIEligibilityRequest request,
            LitePayviewConsultResponseBizBean litePayviewConsultResponse, UserDetails userDetails) {

        EMIEligibilityResponseBody body = new EMIEligibilityResponseBody();
        SecureResponseHeader head = new SecureResponseHeader();
        EMIEligibilityResponse response = new EMIEligibilityResponse(head, body);

        // filter for EMI and EMI_DC
        List<PayMethodViewsBiz> emiPayMethodViews = litePayviewConsultResponse
                .getPayMethodViews()
                .parallelStream()
                .filter(Objects::nonNull)
                .filter(p -> (p.getPayMethod().equalsIgnoreCase(PayMethod.EMI.getMethod()) || (p.getPayMethod()
                        .equalsIgnoreCase(PayMethod.EMI_DC.getMethod())))).collect(Collectors.toList());

        PayChannelOptionViewBiz zestMoneyOptionViewBiz = populateZestMoneyPayChannelOptionViewBiz(litePayviewConsultResponse);

        if (CollectionUtils.isEmpty(emiPayMethodViews) && null == zestMoneyOptionViewBiz) {
            // emi is not configured on merchant
            LOGGER.error("Emi not configured on mid {}", request.getBody().getMid());
            throw PaymentRequestProcessingException.getException(ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT);
        }

        List<EmiEligibility> emiEligibilities = new ArrayList<>();
        body.setEmiEligibility(emiEligibilities);
        // filter payView on channelCode

        if (!CollectionUtils.isEmpty(request.getBody().getEmiTypes())) {
            List<String> emiTypes = getEmiTypes(request);

            Map<String, List<PayChannelOptionViewBiz>> payViewMap = getPayViewMap(request.getBody().getChannelCode(),
                    emiPayMethodViews);

            if (CollectionUtils.isEmpty(payViewMap)
                    || com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST
                            .equals(request.getBody().getChannelCode())) {
                for (String emiType : emiTypes) {
                    if (EmiType.NBFC.name().equals(emiType)
                            && null != zestMoneyOptionViewBiz
                            && com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST
                                    .equals(request.getBody().getChannelCode())) {
                        EmiEligibility emiEligibility = new EmiEligibility(request.getBody().getChannelCode(), true,
                                emiType);
                        emiEligibilities.add(emiEligibility);
                        continue;
                    }

                    String msg = ConfigurationUtil.getMessageProperty("emi.not.available.on." + emiType + ".message",
                            "You are not eligible for availing EMI on your " + emiType);
                    EmiEligibility emiEligibility = new EmiEligibility(request.getBody().getChannelCode(), false,
                            emiType, msg);
                    emiEligibilities.add(emiEligibility);
                }
            } else {
                for (String emiType : emiTypes) {
                    if (EmiType.NBFC.name().equals(emiType)
                            && null != zestMoneyOptionViewBiz
                            && com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST
                                    .equals(request.getBody().getChannelCode())) {
                        EmiEligibility emiEligibility = new EmiEligibility(request.getBody().getChannelCode(), true,
                                emiType);
                        emiEligibilities.add(emiEligibility);
                        continue;
                    }
                    populateEmiEligibility(request, userDetails, emiEligibilities, payViewMap, emiType, request
                            .getBody().getChannelCode());
                }
            }

        } else {

            EMIDetails emiDetails = getEmiDetails(request.getBody().getMid(), request.getBody().getEmiId());
            if (null == emiDetails) {
                // emi is not configured on merchant
                LOGGER.error("Emi not configured on emiId {}", request.getBody().getEmiId());
                throw PaymentRequestProcessingException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            } else {

                String emiType = emiDetails.getPayMode();

                String bankCode = emiDetails.getBankCode();

                if (EPayMethod.EMI_DC.getMethod().equals(emiDetails.getPayMode())) {
                    emiType = EmiType.DEBIT_CARD.name();
                    /*
                     * for emi dc bank code in mapping is different from instId
                     * at alipay
                     */
                    bankCode = ConfigurationUtil.getMessageProperty("emi.dc.instId." + bankCode, bankCode);
                } else if (EPayMethod.EMI.getMethod().equals(emiDetails.getPayMode())) {
                    emiType = EmiType.CREDIT_CARD.name();
                }

                Map<String, List<PayChannelOptionViewBiz>> payViewMap = getPayViewMap(bankCode, emiPayMethodViews);
                if (CollectionUtils.isEmpty(payViewMap)) {
                    String msg = ConfigurationUtil.getMessageProperty("emi.not.available.on." + emiType + ".message",
                            "You are not eligible for availing EMI on your " + emiType);
                    EmiEligibility emiEligibility = new EmiEligibility(request.getBody().getChannelCode(), false,
                            emiType, msg);
                    emiEligibilities.add(emiEligibility);
                } else {
                    populateEmiEligibility(request, userDetails, emiEligibilities, payViewMap, emiType, bankCode);
                }
            }
        }

        return response;
    }

    private EMIDetails getEmiDetails(String mid, String emiId) {
        EmiDetailRequest emiDetailRequest = new EmiDetailRequest();
        emiDetailRequest.setMid(mid);
        emiDetailRequest.setStatus(Boolean.TRUE);
        emiDetailRequest.setChannelType(ChannelType.WAP);

        try {
            EMIDetailList emiDetailList = workFlowHelper.fetchEmiDetailsList(emiDetailRequest);
            if (null == emiDetailList || CollectionUtils.isEmpty(emiDetailList.getEmiDetails())) {
                LOGGER.error("Got empty emi response from mapping service hence emi not configured on mid");
                throw PaymentRequestProcessingException.getException(ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT);
            }
            return emiDetailList.getEmiDetails().get(Long.valueOf(emiId));
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching merchant data from merchant center service : {}", e);
            throw PaymentRequestProcessingException.getException(ResultCode.UNKNOWN_ERROR);
        }
    }

    private PayChannelOptionViewBiz populateZestMoneyPayChannelOptionViewBiz(
            LitePayviewConsultResponseBizBean litePayviewConsultResponse) {
        PayMethodViewsBiz payView = litePayviewConsultResponse.getPayMethodViews().parallelStream()
                .filter(Objects::nonNull)
                .filter(p -> p.getPayMethod().equalsIgnoreCase(PayMethod.NET_BANKING.getMethod())).findFirst()
                .orElse(null);

        PayMethodViewsBiz payView2 = litePayviewConsultResponse.getPayMethodViews().parallelStream()
                .filter(Objects::nonNull).filter(p -> p.getPayMethod().equalsIgnoreCase(PayMethod.EMI.getMethod()))
                .findFirst().orElse(null);

        if (null != payView2 && !CollectionUtils.isEmpty(payView2.getPayChannelOptionViews())) {
            PayChannelOptionViewBiz optionViewBiz = payView2
                    .getPayChannelOptionViews()
                    .stream()
                    .filter(p -> com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST
                            .equals(p.getInstId())).findAny().orElse(null);
            if (optionViewBiz != null) {
                LOGGER.info("Returning Zest Channel from EMI :{}", optionViewBiz);
                return optionViewBiz;
            }
        }

        if (null != payView && !CollectionUtils.isEmpty(payView.getPayChannelOptionViews())) {
            return payView
                    .getPayChannelOptionViews()
                    .stream()
                    .filter(p -> com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST
                            .equals(p.getInstId())).findAny().orElse(null);
        }
        return null;
    }

    private void populateEmiEligibility(EMIEligibilityRequest request, UserDetails userDetails,
            List<EmiEligibility> emiEligibilities, Map<String, List<PayChannelOptionViewBiz>> payViewMap,
            String emiType, String bankCode) {
        String mapKey = null;
        if (EmiType.CREDIT_CARD.name().equals(emiType)) {
            mapKey = EPayMethod.EMI.getMethod();
        } else if (EmiType.DEBIT_CARD.name().equals(emiType)) {
            mapKey = EPayMethod.EMI_DC.getMethod();
        }
        List<PayChannelOptionViewBiz> payViewBiz = payViewMap.get(mapKey);
        if (null == payViewBiz
                || CollectionUtils.isEmpty(payViewBiz)
                || (EmiType.DEBIT_CARD.name().equals(emiType) && !isUserEligibleforEmiOnDc(userDetails.getMobileNo(),
                        bankCode))) {
            String msg = ConfigurationUtil.getMessageProperty("emi.not.available.on." + emiType + ".message",
                    "You are not eligible for availing EMI on your " + emiType);
            EmiEligibility emiEligibility = new EmiEligibility(bankCode, false, emiType, msg);
            emiEligibilities.add(emiEligibility);
        } else {
            EmiEligibility emiEligibility = new EmiEligibility(bankCode, true, emiType);
            emiEligibilities.add(emiEligibility);
        }
    }

    private boolean isUserEligibleforEmiOnDc(String userMobileNumber, String channelCode) {
        boolean isUserEligibleforEmiOnDc = emiUtil.isUserEligibleforEmiOnDc(userMobileNumber, channelCode);
        LOGGER.info("isUserEligibleforEmiOnDc flag :{}", isUserEligibleforEmiOnDc);
        return isUserEligibleforEmiOnDc;
    }

    private Map<String, List<PayChannelOptionViewBiz>> getPayViewMap(String bankCode,
            List<PayMethodViewsBiz> emiPayMethodViews) {

        return emiPayMethodViews
                .parallelStream()
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toMap(PayMethodViewsBiz::getPayMethod, p -> p.getPayChannelOptionViews()
                                .parallelStream().filter(Objects::nonNull).filter(c -> c.getInstId().equals(bankCode))
                                .collect(Collectors.toList())));
    }

    private List<String> getEmiTypes(EMIEligibilityRequest request) {
        List<String> emiTypes = null;
        if (request.getBody().getEmiTypes().contains("ALL")) {
            emiTypes = new ArrayList<>();
            for (EmiType emi : EmiType.values()) {
                emiTypes.add(emi.name());
            }
        } else {
            emiTypes = request.getBody().getEmiTypes();
        }
        return emiTypes;
    }

    @Override
    protected EMIEligibilityResponse postProcess(EMIEligibilityRequest request, EMIEligibilityRequest serviceRequest,
            EMIEligibilityResponse emiEligibilityResponse) {
        return emiEligibilityResponse;
    }

    private EMIEligibilityResponse getFailureResponse() {
        EMIEligibilityResponseBody emiEligibilityResponseBody = new EMIEligibilityResponseBody();
        emiEligibilityResponseBody.setEligible(Boolean.FALSE.toString());
        // todo message needs to be configured
        emiEligibilityResponseBody.setMessage("Fail");
        emiEligibilityResponseBody.setResultInfo(NativePaymentUtil.resultInfoForFailure());
        return new EMIEligibilityResponse(new SecureResponseHeader(), emiEligibilityResponseBody);
    }

    /**
     * litepay view consult of merchant to fetch pay method
     *
     * @param serviceRequest
     * @return
     */
    private LitePayviewConsultResponseBizBean fetchMerchantConsult(EMIEligibilityRequest serviceRequest,
            UserDetails userDetails) {
        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayViewConsultResponse;
        try {
            LitePayviewConsultRequestBizBean litePayviewConsultRequestBizBean = getLitePayViewResponseBean(serviceRequest);
            setLPVRoute(litePayviewConsultRequestBizBean, userDetails, serviceRequest.getBody().getMid());
            litePayViewConsultResponse = bizPaymentService
                    .litePayviewPayMethodConsult(litePayviewConsultRequestBizBean);
        } catch (PaymentRequestProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw PaymentRequestProcessingException.getException(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        }
        if (!litePayViewConsultResponse.isSuccessfullyProcessed()) {
            throw PaymentRequestProcessingException.getException(litePayViewConsultResponse.getFailureMessage());
        }
        return litePayViewConsultResponse.getResponse();
    }

    /**
     * validate sso token received in request
     *
     * @param ssoToken
     * @return
     */

    private UserDetails validateSSOToken(String ssoToken, String mid) {
        FetchUserDetailsResponse fetchUserDetailsResponse;
        try {
            FetchUserDetailsRequest fetchUserDetailsRequest = getFetchUserDetailsRequest(ssoToken, mid);
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.info("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.info("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || fetchUserDetailsResponse.getUserDetails() == null) {
                LOGGER.info("Validation of sso Token failed : {}", fetchUserDetailsResponse.getResponseMessage());
                throw PaymentRequestProcessingException.getException(fetchUserDetailsResponse.getResponseMessage());
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception validation sso token {}", e);
            throw PaymentRequestProcessingException.getException(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        }

        upsHelper.updateUserPostpaidAccStatusFromUPS(mid, fetchUserDetailsResponse.getUserDetails());

        return fetchUserDetailsResponse.getUserDetails();
    }

    private EMIEligibilityResponse getSuccessResponse() {
        EMIEligibilityResponseBody emiEligibilityResponseBody = new EMIEligibilityResponseBody();
        emiEligibilityResponseBody.setEligible(Boolean.TRUE.toString());
        emiEligibilityResponseBody.setMessage("Success");
        emiEligibilityResponseBody.setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        return new EMIEligibilityResponse(new SecureResponseHeader(), emiEligibilityResponseBody);
    }

    private LitePayviewConsultRequestBizBean getLitePayViewResponseBean(EMIEligibilityRequest serviceRequest) {
        EnvInfoRequestBean envInfoRequestBean = getEnvInfoBean(serviceRequest.getHead().getChannelId().getValue());
        String platformMid = getPlatformMid(serviceRequest.getBody().getMid());

        return new LitePayviewConsultRequestBizBean(null, envInfoRequestBean, ERequestType.DEFAULT, null, platformMid,
                false, excludedPayMethodList, null, null, false);
    }

    private EnvInfoRequestBean getEnvInfoBean(String channel) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        EnvInfoRequestBean envInfoRequestBean = EnvInfoUtil.fetchEnvInfo(request);
        ETerminalType terminalType = getTerminalTypeByTerminal(channel);
        if (envInfoRequestBean != null && terminalType != null) {
            envInfoRequestBean.setTerminalType(terminalType);
        }
        return envInfoRequestBean;
    }

    private String getPlatformMid(String paytmMid) {
        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(paytmMid);

        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            return merchantMappingResponse.getResponse().getAlipayId();
        }
        LOGGER.error("Invalid mid {}", paytmMid);
        throw PaymentRequestProcessingException.getException(ResultCode.INVALID_MID);

    }

    private FetchUserDetailsRequest getFetchUserDetailsRequest(String ssoToken, String mid) {
        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        return new FetchUserDetailsRequest(ssoToken, clientId, secretKey, mid);
    }

    private void validate(EMIEligibilityRequest request) {
        EMIEligibilityRequestBody requestBody = request.getBody();
        HttpServletRequest httpRequest = OfflinePaymentUtils.gethttpServletRequest();
        String qMid = httpRequest
                .getParameter(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.NATIVE_MID);
        if (org.apache.commons.lang.StringUtils.isEmpty(qMid)) {
            LOGGER.error("mid can not be null or blank in query param");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        if (null == request || null == request.getHead() || null == request.getBody()) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        String mid = request.getBody().getMid();
        if (!mid.equals(qMid)) {
            LOGGER.error("mid should be same in body and in query param");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        if (((TokenType.SSO.getType().equals(request.getHead().getTokenType())) && StringUtils.isNotBlank(request
                .getHead().getToken()))
                && (StringUtils.isNotBlank(requestBody.getEmiId()) || (!CollectionUtils.isEmpty(requestBody
                        .getEmiTypes()) && isValidEmiType(requestBody.getEmiTypes()) && StringUtils
                            .isNotBlank(requestBody.getChannelCode())))) {
            return;
        }

        if (requestBody.getUserInfo() == null || StringUtils.isBlank(requestBody.getUserInfo().getSsoToken())
                || StringUtils.isBlank(requestBody.getMid()) || StringUtils.isBlank(requestBody.getChannelCode())
                || requestBody.getPayMethod() == null) {
            throw RequestValidationException.getException();
        }
    }

    private boolean isValidEmiType(List<String> emiTypes) {

        if (emiTypes.contains("ALL")) {
            return true;
        }
        for (EmiType emiType : EmiType.values()) {
            if (emiTypes.contains(emiType.name())) {
                return true;
            }
        }
        return false;
    }

    private void setLPVRoute(LitePayviewConsultRequestBizBean litePayviewConsultRequestBizBean,
            UserDetails userDetails, String mid) {
        boolean isFullPg2TrafficEnabled = merchantPreferenceService.isFullPg2TrafficEnabled(mid);
        Routes route = pg2Util.getRouteForLpvRequest(isFullPg2TrafficEnabled, mid);
        if (litePayviewConsultRequestBizBean.getExtendInfo() == null) {
            litePayviewConsultRequestBizBean.setExtendInfo(new HashMap<>());
        }
        litePayviewConsultRequestBizBean.getExtendInfo().put(BizConstant.ExtendedInfoKeys.LPV_ROUTE, route.getName());
        litePayviewConsultRequestBizBean.setPaytmMerchantId(mid);
        if (userDetails != null && userDetails.getUserId() != null) {
            litePayviewConsultRequestBizBean.setPaytmUserId(userDetails.getUserId());
        }
    }

}
