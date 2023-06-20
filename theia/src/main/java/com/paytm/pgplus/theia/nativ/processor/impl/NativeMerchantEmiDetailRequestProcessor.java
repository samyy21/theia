package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.cache.model.EmiBrandCodeDetail;
import com.paytm.pgplus.cache.model.EmiBrandPlanDetail;
import com.paytm.pgplus.cache.model.EmiBrandSubventionPlan;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IEmiBrandSubventionService;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.payview.emi.MerchantEmiDetailRequest;
import com.paytm.pgplus.theia.nativ.model.payview.emi.MerchantEmiDetailResponse;
import com.paytm.pgplus.theia.nativ.model.payview.emi.MerchantEmiDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.BrandEmiDetail;
import com.paytm.pgplus.theia.nativ.model.payview.response.MerchantEMIChannelInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.MerchantEmiDetail;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.EDCEmiSubvention.DEFAULT_BRAND_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_BRAND_EMI_FEATURE;

@Service("nativeMerchantEmiDetailRequestProcessor")
public class NativeMerchantEmiDetailRequestProcessor
        extends
        AbstractRequestProcessor<MerchantEmiDetailRequest, MerchantEmiDetailResponse, WorkFlowRequestBean, MerchantEmiDetailResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeMerchantEmiDetailRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(NativeMerchantEmiDetailRequestProcessor.class);

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("emiBrandSubventionService")
    private IEmiBrandSubventionService mappingSrvEmiBrandSubvention;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private PG2Util pg2Util;

    @Override
    protected WorkFlowRequestBean preProcess(MerchantEmiDetailRequest request) {
        validateRequest(request);
        WorkFlowRequestBean workFlowRequestBean = createWorkFlowRequest(request.getBody().getMid(), request.getBody()
                .getProductCode());
        return workFlowRequestBean;
    }

    private void validateRequest(MerchantEmiDetailRequest request) {
        LOGGER.info("Validating request");
        HttpServletRequest httpRequest = OfflinePaymentUtils.gethttpServletRequest();
        String qMid = httpRequest.getParameter(TheiaConstant.RequestParams.NATIVE_MID);
        if (StringUtils.isEmpty(qMid)) {
            LOGGER.error("mid can not be null or blank in query param");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        if (null == request || null == request.getHead() || null == request.getBody()) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        String mid = request.getBody().getMid();
        String productCode = request.getBody().getProductCode();

        if (!mid.equals(qMid)) {
            LOGGER.error("mid should be same in body and in query param");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        String tokenType = null;
        if (request.getHead().getTokenType() != null) {
            tokenType = request.getHead().getTokenType().getType();
        }
        String signature = request.getHead().getSignature();

        if (StringUtils.equals(tokenType, TokenType.CHECKSUM.getType())) {
            LOGGER.info("process CHECKSUM validation, api: /theia/api/v1/getEmiDetails");
            tokenValidationHelper.validateChecksum(signature, request.getBody(), request.getBody().getMid());
            return;
        }

        Map jwtMap = new HashMap();
        jwtMap.put("mid", mid);

        if (StringUtils.isNotBlank(productCode)) {
            jwtMap.put("productCode", productCode);
        }

        String clientId = request.getHead().getClientId();
        boolean isValid = false;

        try {
            String secretKey = environment.getProperty("emi.secret.key." + clientId);
            if (StringUtils.isNotBlank(secretKey)) {
                LOGGER.info("Successfully fetched key from vault");
                isValid = JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, signature, secretKey, clientId);
            } else {
                LOGGER.error("Key not found in vault : {} ", secretKey);
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while validating JWT token", e.getMessage());
        }
        if (!isValid) {
            LOGGER.error("invalid JWT token");
            throw RequestValidationException.getException(ResultCode.TOKEN_VALIDATION_EXCEPTION);
        }

        LOGGER.info("Request validation successfully done");

    }

    @Override
    protected MerchantEmiDetailResponse onProcess(MerchantEmiDetailRequest request,
            WorkFlowRequestBean workFlowRequestBean) throws Exception {
        if (request.getBody().getBrandCode() != null) {
            workFlowRequestBean.setBrandCodes(request.getBody().getBrandCode());
        }
        setLPVRoute(workFlowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = taskExecutor.execute(workFlowRequestBean);
        MerchantEmiDetailResponse response = mapMerchantEmiDetailResponse(request, workFlowResponseBean);
        populateBrandEmiDetails(request, response);
        return response;
    }

    private void populateBrandEmiDetails(MerchantEmiDetailRequest request, MerchantEmiDetailResponse response) {
        String mid = request.getBody().getMid();

        if (!ff4jUtils.isFeatureEnabledOnMid(mid, THEIA_BRAND_EMI_FEATURE, false)) {
            LOGGER.info("ff4j THEIA_BRAND_EMI_FEATURE not allowed");
            return;
        }

        if (!merchantPreferenceProvider.isBrandEMIEnabled(mid)) {
            LOGGER.info("pref isBrandEMIEnabled not enabled");
            return;
        }

        LOGGER.info("getting brandEMI info now");

        EmiBrandSubventionPlan emiBrandSubventionPlanResponse = null;
        List<String> requestedBrandCodes = new ArrayList<>();
        try {
            if (null != request.getBody().getBrandCode()) {
                requestedBrandCodes.addAll(request.getBody().getBrandCode());
            }
            if (!requestedBrandCodes.contains(DEFAULT_BRAND_CODE)) {
                requestedBrandCodes.add(DEFAULT_BRAND_CODE);
            }
            emiBrandSubventionPlanResponse = mappingSrvEmiBrandSubvention.getEmiBrandSubventionPlans(mid,
                    requestedBrandCodes);
            EXT_LOGGER.customInfo("Mapping response - EmiBrandSubventionPlan :: {}", emiBrandSubventionPlanResponse);
            if (emiBrandSubventionPlanResponse == null
                    || org.apache.commons.collections.CollectionUtils.isEmpty(emiBrandSubventionPlanResponse
                            .getEmiBrandSubventionPlans())) {
                LOGGER.info("null or empty response for getEmiBrandSubventionPlans from mapping-srv");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching EmiBrandSubventionPlan ", e);
            return;
        }

        List<BrandEmiDetail> brandEmiDetails = new ArrayList<>();
        BrandEmiDetail brandEmiDetail = null;

        Map<String, List<EmiBrandPlanDetail>> brandCodesPlans = emiBrandSubventionPlanResponse
                .getEmiBrandSubventionPlans().stream()
                .collect(Collectors.toMap(EmiBrandCodeDetail::getBrandCode, EmiBrandCodeDetail::getPlans));

        for (String brandCode : requestedBrandCodes) {

            List<EmiBrandPlanDetail> plans = brandCodesPlans.get(brandCode);
            brandEmiDetail = null;

            if (org.apache.commons.collections.CollectionUtils.isEmpty(plans)) {
                continue;
            }

            // createMapping for bank level plans
            Map<String, List<EmiBrandPlanDetail>> bankLevelPlans = new HashMap<>();
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(plans)) {
                for (EmiBrandPlanDetail plan : plans) {

                    if (bankLevelPlans.containsKey(plan.getBank())) {
                        List<EmiBrandPlanDetail> plansForBank = bankLevelPlans.get(plan.getBank());
                        plansForBank.add(plan);
                    } else {
                        List<EmiBrandPlanDetail> emiBrandPlanDetails = new ArrayList<>();
                        emiBrandPlanDetails.add(plan);
                        bankLevelPlans.put(plan.getBank(), emiBrandPlanDetails);
                    }
                }
            }

            List<MerchantEmiDetail> brandEmiDetailInfo = new ArrayList<>();

            for (Map.Entry<String, List<EmiBrandPlanDetail>> iterator : bankLevelPlans.entrySet()) {
                String bank = iterator.getKey();

                List<MerchantEMIChannelInfo> bankPlans = new ArrayList<>();

                for (EmiBrandPlanDetail planDetail : iterator.getValue()) {
                    MerchantEMIChannelInfo channelInfo = new MerchantEMIChannelInfo();
                    channelInfo.setPlanId(planDetail.getPlanId());
                    channelInfo.setInterestRate(String.valueOf(planDetail.getInterest()));
                    channelInfo.setOfMonths(String.valueOf(planDetail.getMonth()));
                    channelInfo.setMinAmount(new Money(String.valueOf(planDetail.getMinAmount())));
                    channelInfo.setMaxAmount(new Money(String.valueOf(planDetail.getMaxAmount())));
                    channelInfo.setEmiId(String.valueOf(planDetail.getId()));

                    bankPlans.add(channelInfo);
                }

                MerchantEmiDetail merchantEmiDetail = new MerchantEmiDetail();
                merchantEmiDetail.setChannelCode(bank);
                merchantEmiDetail.setEmiChannelInfos(bankPlans);

                brandEmiDetailInfo.add(merchantEmiDetail);
            }

            brandEmiDetail = new BrandEmiDetail();
            brandEmiDetail.setBrandCode(brandCode);
            brandEmiDetail.setBrandEmiDetailInfo(brandEmiDetailInfo);

            brandEmiDetails.add(brandEmiDetail);
        }
        response.getBody().setBrandEmiDetails(brandEmiDetails);
        if (requestedBrandCodes.size() > 1) {
            for (BrandEmiDetail brandsEmiDetail : response.getBody().getBrandEmiDetails()) {
                if (StringUtils.equals(brandsEmiDetail.getBrandCode(), DEFAULT_BRAND_CODE)) {
                    // taking union response of brand specific response and
                    // default brand response
                    populateDefaultBrandDetailsInResponse(brandsEmiDetail, response);
                    // removing the default brand response from the final
                    // response
                    if (brandCodesPlans.size() > 1) {
                        response.getBody().getBrandEmiDetails().remove(brandsEmiDetail);
                    }
                    break;
                }
            }
        }
    }

    private MerchantEmiDetailResponse mapMerchantEmiDetailResponse(MerchantEmiDetailRequest request,
            GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean) {
        LOGGER.info("Mapping  workflow response to service response");
        if (validateEmiDetails(workFlowResponseBean)) {

            Map<String, List<EMIDetails>> bankEmiDetailMap = workFlowResponseBean.getResponse().getEmiDetailList()
                    .getEmiDetails().values().parallelStream().filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(EMIDetails::getBankCode));

            List<PayMethodViewsBiz> emiPayMethodViews = workFlowResponseBean
                    .getResponse()
                    .getMerchnatLiteViewResponse()
                    .getPayMethodViews()
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .filter(p -> (p.getPayMethod().equalsIgnoreCase(PayMethod.EMI.getMethod()) || (p.getPayMethod()
                            .equalsIgnoreCase(PayMethod.EMI_DC.getMethod())))).collect(Collectors.toList());

            PayChannelOptionViewBiz zestMoneyOptionViewBiz = populateZestMoneyPayChannelOptionViewBiz(workFlowResponseBean);

            if ((CollectionUtils.isEmpty(emiPayMethodViews) || CollectionUtils.isEmpty(bankEmiDetailMap))
                    && null == zestMoneyOptionViewBiz) {
                LOGGER.error("Get empty response for emi emiPayMethodViews: {},"
                        + " bankEmiDetailMap: {}, zestMoneyOptionViewBiz: {}", emiPayMethodViews, bankEmiDetailMap,
                        zestMoneyOptionViewBiz);
                return generateErrorResponse();
            }
            MerchantEmiDetailResponse response = new MerchantEmiDetailResponse();

            MerchantEmiDetailResponseBody responseBody = new MerchantEmiDetailResponseBody();
            response.setBody(responseBody);
            List<MerchantEmiDetail> merchantEmiDetails = new ArrayList<>();
            responseBody.setEmiDetails(merchantEmiDetails);

            if (!(CollectionUtils.isEmpty(emiPayMethodViews) && CollectionUtils.isEmpty(bankEmiDetailMap))) {
                populateEmiPlanDetails(bankEmiDetailMap, emiPayMethodViews, merchantEmiDetails, request);
                if (CollectionUtils.isEmpty(merchantEmiDetails)) {
                    LOGGER.error(
                            "litepay-view emi response : {} does not match with mapping-service emi response : {}",
                            emiPayMethodViews, bankEmiDetailMap);
                }
                LOGGER.info("After response mapping emiPayMethodViews size: {},"
                        + " bankEmiDetailMap size: {}, merchantEmiDetails size: {}", emiPayMethodViews.size(),
                        bankEmiDetailMap.size(), merchantEmiDetails.size());
            }

            if (null != zestMoneyOptionViewBiz) {
                populateZestMoneyPlanDetails(merchantEmiDetails, bankEmiDetailMap);
            }

            if (CollectionUtils.isEmpty(merchantEmiDetails)) {
                return generateErrorResponse();
            }

            responseBody.setResultInfo(new ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS
                    .getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
            return response;

        } else {
            LOGGER.error("workflow response validation failed : {}", workFlowResponseBean);
        }
        return generateErrorResponse();

    }

    private void populateZestMoneyPlanDetails(List<MerchantEmiDetail> merchantEmiDetails,
            Map<String, List<EMIDetails>> bankEmiDetailMap) {
        MerchantEmiDetail merchantEmiDetail = new MerchantEmiDetail();
        merchantEmiDetails.add(merchantEmiDetail);
        merchantEmiDetail.setChannelCode(TheiaConstant.EnhancedCashierPageKeys.ZEST);
        merchantEmiDetail.setChannelName(TheiaConstant.EnhancedCashierPageKeys.ZESTMONEY);
        merchantEmiDetail.setIconUrl(commonFacade
                .getLogoUrl(TheiaConstant.EnhancedCashierPageKeys.ZEST, EChannelId.WAP));
        merchantEmiDetail.setEmiType(EmiType.NBFC.name());
        merchantEmiDetail.setMultiItemEmiSupported(false);
        List<EMIDetails> emiDetails = bankEmiDetailMap.get(TheiaConstant.EnhancedCashierPageKeys.ZEST);
        if (!CollectionUtils.isEmpty(emiDetails)) {
            populateEmiPlan(merchantEmiDetail, emiDetails, TheiaConstant.EnhancedCashierPageKeys.ZEST);
        }
    }

    private void populateEmiPlanDetails(Map<String, List<EMIDetails>> bankEmiDetailMap,
            List<PayMethodViewsBiz> emiPayMethodViews, List<MerchantEmiDetail> merchantEmiDetails,
            MerchantEmiDetailRequest request) {
        String edcProductCodes = ConfigurationUtil.getMessageProperty("edc.product.codes");
        for (PayMethodViewsBiz payViewBiz : emiPayMethodViews) {
            for (PayChannelOptionViewBiz payChannel : payViewBiz.getPayChannelOptionViews()) {
                String instId = payChannel.getInstId();
                if (PayMethod.EMI_DC.getMethod().equalsIgnoreCase(payViewBiz.getPayMethod())) {
                    /*
                     * for emi dc bank code in mapping is different from instId
                     * at alipay
                     */
                    if (StringUtils.isNotBlank(edcProductCodes)
                            && StringUtils.isNotBlank(request.getBody().getProductCode())
                            && StringUtils.contains(edcProductCodes, request.getBody().getProductCode())) {
                        LOGGER.info("changing instId with EDC Bank Code if exists");
                        instId = ConfigurationUtil.getMessageProperty("edc.emi.dc.bank.code." + instId, instId);
                    } else {
                        instId = ConfigurationUtil.getMessageProperty("emi.dc.bank.code." + instId, instId);
                    }
                }
                List<EMIDetails> emiDetails = bankEmiDetailMap.get(instId);
                if (CollectionUtils.isEmpty(emiDetails)) {
                    LOGGER.error("Got empty emiDetails for instId: {} ", instId);
                    continue;
                }
                if (!payChannel.isEnableStatus()) {
                    LOGGER.info("payChannel is disable for instId : {} ", instId);
                    continue;
                }
                MerchantEmiDetail merchantEmiDetail = new MerchantEmiDetail();
                merchantEmiDetails.add(merchantEmiDetail);
                merchantEmiDetail.setChannelCode(payChannel.getInstId());
                if (PayMethod.EMI_DC.getMethod().equalsIgnoreCase(payViewBiz.getPayMethod())) {
                    merchantEmiDetail.setEmiType(EmiType.DEBIT_CARD.name());
                    merchantEmiDetail.setChannelName(payChannel.getInstName() + " "
                            + EPayMethod.DEBIT_CARD.getDisplayName());
                } else {
                    merchantEmiDetail.setEmiType(EmiType.CREDIT_CARD.name());
                    if (CashierConstant.BAJAJFN.equals(payChannel.getInstId())) {
                        merchantEmiDetail.setChannelName(CashierConstant.BAJAJ_FINSERV_EMI_CARD);
                    } else {
                        merchantEmiDetail.setChannelName(payChannel.getInstName() + " "
                                + EPayMethod.CREDIT_CARD.getDisplayName());
                    }
                }
                merchantEmiDetail.setMultiItemEmiSupported(true);
                populateEmiPlan(merchantEmiDetail, emiDetails, payChannel.getInstId());

            }
        }
    }

    private PayChannelOptionViewBiz populateZestMoneyPayChannelOptionViewBiz(
            GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean) {
        PayMethodViewsBiz payView = workFlowResponseBean.getResponse().getMerchnatLiteViewResponse()
                .getPayMethodViews().parallelStream().filter(Objects::nonNull)
                .filter(p -> p.getPayMethod().equalsIgnoreCase(PayMethod.NET_BANKING.getMethod())).findFirst()
                .orElse(null);
        PayMethodViewsBiz payView2 = workFlowResponseBean.getResponse().getMerchnatLiteViewResponse()
                .getPayMethodViews().parallelStream().filter(Objects::nonNull)
                .filter(p -> p.getPayMethod().equalsIgnoreCase(PayMethod.EMI.getMethod())).findFirst().orElse(null);

        if (null != payView2 && !CollectionUtils.isEmpty(payView2.getPayChannelOptionViews())) {
            PayChannelOptionViewBiz optionViewBiz = payView2.getPayChannelOptionViews().stream()
                    .filter(p -> TheiaConstant.EnhancedCashierPageKeys.ZEST.equals(p.getInstId())).findAny()
                    .orElse(null);
            if (optionViewBiz != null) {
                LOGGER.info("Returning Zest Channel from EMI :{}", optionViewBiz);
                return optionViewBiz;
            }
        }

        if (null != payView && !CollectionUtils.isEmpty(payView.getPayChannelOptionViews())) {
            return payView.getPayChannelOptionViews().stream()
                    .filter(p -> TheiaConstant.EnhancedCashierPageKeys.ZEST.equals(p.getInstId())).findAny()
                    .orElse(null);
        }
        return null;
    }

    private MerchantEmiDetailResponse generateErrorResponse() {
        MerchantEmiDetailResponse response = new MerchantEmiDetailResponse();
        MerchantEmiDetailResponseBody body = new MerchantEmiDetailResponseBody();
        body.setResultInfo(new ResultInfo(ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT.getResultStatus(),
                ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT.getResultCodeId(), ResultCode.EMI_NOT_CONFIGURED_ON_MERCHANT
                        .getResultMsg()));
        response.setBody(body);
        return response;
    }

    private void populateEmiPlan(MerchantEmiDetail merchantEmiDetail, List<EMIDetails> emiDetails, String instId) {
        List<MerchantEMIChannelInfo> emiChannelInfos = new ArrayList<>();
        merchantEmiDetail.setEmiChannelInfos(emiChannelInfos);
        for (EMIDetails emiDetail : emiDetails) {
            MerchantEMIChannelInfo merchantInfo = new MerchantEMIChannelInfo();
            emiChannelInfos.add(merchantInfo);
            merchantInfo.setEmiId(String.valueOf(emiDetail.getId()));
            merchantInfo.setInterestRate(String.valueOf(emiDetail.getInterest()));
            merchantInfo.setOfMonths(String.valueOf(emiDetail.getMonth()));
            merchantInfo.setMinAmount(new Money(convertAmount(String.valueOf(emiDetail.getMinAmount()))));
            merchantInfo.setMaxAmount(new Money(convertAmount(String.valueOf(emiDetail.getMaxAmount()))));
            merchantInfo.setPlanId(instId + "|" + emiDetail.getMonth());
            merchantEmiDetail.setIconUrl(emiDetail.getLogoUrl());
            merchantInfo.setBankId(String.valueOf(emiDetail.getBankId()));
        }
    }

    private String convertAmount(String amount) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(amount) && amount.startsWith(".")) {

            return "0" + amount;
        }
        return amount;
    }

    private boolean validateEmiDetails(GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean) {
        return null != workFlowResponseBean.getResponse()
                && null != workFlowResponseBean.getResponse().getEmiDetailList()
                && !CollectionUtils.isEmpty(workFlowResponseBean.getResponse().getEmiDetailList().getEmiDetails())
                && null != workFlowResponseBean.getResponse().getMerchnatLiteViewResponse()
                && !CollectionUtils.isEmpty(workFlowResponseBean.getResponse().getMerchnatLiteViewResponse()
                        .getPayMethodViews());
    }

    private WorkFlowRequestBean createWorkFlowRequest(String mid, String productCode) {
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(mid);
        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            flowRequestBean.setAlipayMID(merchantMappingResponse.getResponse().getAlipayId());
        } else {
            final String errorMessage = merchantMappingResponse == null ? "Could not map merchant"
                    : merchantMappingResponse.getFailureMessage();
            LOGGER.error(errorMessage);
            throw RequestValidationException.getException(ResultCode.INVALID_MID);

        }
        workFlowTransBean.setWorkFlowBean(flowRequestBean);
        flowRequestBean.setEmiDetailsApi(true);
        flowRequestBean.setPaytmMID(mid);

        ERequestType requestType = ERequestType.DEFAULT;

        if (StringUtils.isNotBlank(productCode)) {
            try {
                productCode = String.valueOf(ProductCodes.getProductById(productCode));

                if (ERequestType.getByProductCode(productCode) != null) {
                    requestType = ERequestType.getByProductCode(productCode);
                }
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Could not set product code because : ", e);
            }
        }

        flowRequestBean.setRequestType(requestType);
        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(OfflinePaymentUtils.gethttpServletRequest());
        flowRequestBean.setEnvInfoReqBean(envInfo);
        LOGGER.info("flowRequestBean created successfully");
        return flowRequestBean;
    }

    public void populateDefaultBrandDetailsInResponse(BrandEmiDetail defaultBrandEmiData,
            MerchantEmiDetailResponse response) {
        for (BrandEmiDetail inputBrandEmiDetails : response.getBody().getBrandEmiDetails()) {
            if (!StringUtils.equals(inputBrandEmiDetails.getBrandCode(), DEFAULT_BRAND_CODE)) {
                // comparing default brand codes emiData with brand specific
                // emiData
                compareBrandDetailsWithDefaultBrand(defaultBrandEmiData, inputBrandEmiDetails);
            }
        }
    }

    public void compareBrandDetailsWithDefaultBrand(BrandEmiDetail defaultBrandEmiData,
            BrandEmiDetail inputBrandEmiDetails) {
        Map<String, MerchantEmiDetail> inputChannelCodeMap = new HashMap<String, MerchantEmiDetail>();
        Map<String, MerchantEmiDetail> defaultChannelCodeMap = new HashMap<String, MerchantEmiDetail>();
        for (MerchantEmiDetail inputBrandEmiInfo : inputBrandEmiDetails.getBrandEmiDetailInfo()) {
            inputChannelCodeMap.put(inputBrandEmiInfo.getChannelCode(), inputBrandEmiInfo);
        }
        for (MerchantEmiDetail defaultBrandEmiInfo : defaultBrandEmiData.getBrandEmiDetailInfo()) {
            defaultChannelCodeMap.put(defaultBrandEmiInfo.getChannelCode(), defaultBrandEmiInfo);
        }
        compareWithDefaultBrandChannelCode(inputBrandEmiDetails, defaultBrandEmiData, inputChannelCodeMap,
                defaultChannelCodeMap);
    }

    public void compareWithDefaultBrandChannelCode(BrandEmiDetail inputBrandEmiDetails,
            BrandEmiDetail defaultBrandEmiData, Map<String, MerchantEmiDetail> inputChannelCodeMap,
            Map<String, MerchantEmiDetail> defaultChannelCodeMap) {
        // comparing brand specific channels with the default brand code
        // channels and adding the unique ones in brand specific response
        for (MerchantEmiDetail defaultBrandEmiInfo : defaultBrandEmiData.getBrandEmiDetailInfo()) {
            if (!inputChannelCodeMap.containsKey(defaultBrandEmiInfo.getChannelCode())) {
                inputBrandEmiDetails.getBrandEmiDetailInfo().add(
                        defaultChannelCodeMap.get(defaultBrandEmiInfo.getChannelCode()));
            } else {
                compareEmiMonths(inputBrandEmiDetails, defaultBrandEmiInfo);
            }
        }
    }

    public void compareEmiMonths(BrandEmiDetail inputBrandEmiDetails, MerchantEmiDetail defaultBrandEmiInfo) {

        // comparing brand specific response months with the default brand
        // response months and adding the unique ones in brand specific response
        Map<String, MerchantEMIChannelInfo> inputEmiChannelInfoMap = new HashMap<String, MerchantEMIChannelInfo>();
        Map<String, MerchantEMIChannelInfo> defaultEmiChannelInfoMap = new HashMap<String, MerchantEMIChannelInfo>();
        List<MerchantEMIChannelInfo> defaultMerchantEMIChannelInfo = new ArrayList<>();
        for (MerchantEmiDetail inputBrandEmiInfo : inputBrandEmiDetails.getBrandEmiDetailInfo()) {
            for (MerchantEMIChannelInfo inputEmiChannelInfo : inputBrandEmiInfo.getEmiChannelInfos()) {
                inputEmiChannelInfoMap.put(inputEmiChannelInfo.getPlanId(), inputEmiChannelInfo);
            }
        }
        for (MerchantEMIChannelInfo defaultBrandEmiInfoData : defaultBrandEmiInfo.getEmiChannelInfos()) {
            defaultEmiChannelInfoMap.put(defaultBrandEmiInfoData.getPlanId(), defaultBrandEmiInfoData);
        }
        for (MerchantEMIChannelInfo defaultBrandEmiInfoData : defaultBrandEmiInfo.getEmiChannelInfos()) {

            for (MerchantEmiDetail inputBrandEmiInfo : inputBrandEmiDetails.getBrandEmiDetailInfo()) {

                if (inputBrandEmiInfo != null
                        && inputBrandEmiDetails.getBrandEmiDetailInfo() != null
                        && (defaultBrandEmiInfo.getChannelCode().equals(inputBrandEmiInfo.getChannelCode()) && !inputEmiChannelInfoMap
                                .containsKey(defaultBrandEmiInfoData.getPlanId()))) {
                    defaultMerchantEMIChannelInfo
                            .add(defaultEmiChannelInfoMap.get(defaultBrandEmiInfoData.getPlanId()));
                }
            }
        }
        for (MerchantEmiDetail inputBrandEmiInfo : inputBrandEmiDetails.getBrandEmiDetailInfo()) {
            if (null != inputBrandEmiInfo && null != inputBrandEmiDetails.getBrandEmiDetailInfo()
                    && ((inputBrandEmiInfo.getChannelCode()).equals(defaultBrandEmiInfo.getChannelCode()))) {
                inputBrandEmiInfo.getEmiChannelInfos().addAll(defaultMerchantEMIChannelInfo);
            }
        }
    }

    @Override
    protected MerchantEmiDetailResponse postProcess(MerchantEmiDetailRequest request,
            WorkFlowRequestBean merchantEmiDetailRequest, MerchantEmiDetailResponse merchantEmiDetailResponse)
            throws Exception {
        return merchantEmiDetailResponse;
    }

    private void setLPVRoute(WorkFlowRequestBean requestBean) {
        String mid = requestBean.getPaytmMID();
        boolean isFullPg2TrafficEnabled = merchantPreferenceService.isFullPg2TrafficEnabled(mid);
        Routes route = pg2Util.getRouteForLpvRequest(isFullPg2TrafficEnabled, mid);
        if (requestBean.getExtendInfo() == null) {
            requestBean.setExtendInfo(new ExtendedInfoRequestBean());
        }
        requestBean.getExtendInfo().setLpvRoute(route);
    }
}
