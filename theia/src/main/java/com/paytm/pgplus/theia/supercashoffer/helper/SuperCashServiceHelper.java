package com.paytm.pgplus.theia.supercashoffer.helper;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.supercashoffers.enums.PIIPayModes;
import com.paytm.pgplus.facade.supercashoffers.models.SuperCashOfferPaymode;
import com.paytm.pgplus.facade.supercashoffers.models.SuperCashOfferServiceRequest;
import com.paytm.pgplus.facade.supercashoffers.models.SuperCashOfferServiceResponse;
import com.paytm.pgplus.facade.supercashoffers.services.ISupercashSevice;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;
import com.paytm.pgplus.theia.nativ.PostpaidServiceHelper;
import com.paytm.pgplus.theia.nativ.model.common.SsoTokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PPI;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.supercashoffer.enums.OrderSource;
import com.paytm.pgplus.theia.supercashoffer.enums.SupercashResponseConstants;
import com.paytm.pgplus.theia.supercashoffer.exceptions.SupercashException;
import com.paytm.pgplus.theia.supercashoffer.exceptions.SupercashException.SupercashIllegalParamException;
import com.paytm.pgplus.theia.supercashoffer.model.*;
import com.paytm.pgplus.theia.supercashoffer.service.SuperCashOffersService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ON_PAYTM;

@Component("superCashServiceHelper")
public class SuperCashServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuperCashServiceHelper.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SuperCashServiceHelper.class);

    @Autowired
    @Qualifier("supercashService")
    private ISupercashSevice supercashService;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    @Qualifier("preRedisCacheServiceImpl")
    private IPreRedisCacheService preRedisCacheServiceImpl;

    @Autowired
    @Qualifier("SuperCashOffersService")
    private SuperCashOffersService superCashOffersService;

    @Autowired
    private PostpaidServiceHelper postPaidServiceHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    public void validateRequest(SuperCashOfferRequest superCashOfferRequest, boolean offlineFlows)
            throws SupercashException {

        EXT_LOGGER.customInfo("supercash request in validateRequest :: {}", superCashOfferRequest);

        if (superCashOfferRequest == null || superCashOfferRequest.getBody() == null
                || superCashOfferRequest.getHead() == null) {
            LOGGER.error("request, head and body are mandatory!");
            throw new SupercashIllegalParamException(SupercashResponseConstants.ILLEGAL_PARAMS.getMessage());
        }
        if (isInvalidParamInReq(superCashOfferRequest, offlineFlows)) {
            LOGGER.error("Parameters are not correct, please check!");
            throw new SupercashIllegalParamException(SupercashResponseConstants.ILLEGAL_PARAMS.getMessage());
        }
        if (!ff4JUtil.isSuperCashEnabledMid(superCashOfferRequest.getBody().getMid()) && !offlineFlows) {
            LOGGER.error("MID is not eligible for supercash offers : {}", superCashOfferRequest.getBody().getMid());
            throw new SupercashException.SupercashMerchantNotEligibleException(
                    SupercashResponseConstants.MERCHANT_NOT_ELIGIBLE.getMessage());
        }
    }

    private boolean isInvalidParamInReq(SuperCashOfferRequest superCashOfferRequest, boolean offlineFlows)
            throws SupercashException.SupercashMerchantDetailsFetchMappingException, SupercashIllegalParamException {

        if (StringUtils.isNotBlank(superCashOfferRequest.getBody().getSource())
                && StringUtils.isNotBlank(superCashOfferRequest.getBody().getMid())) {

            if (CollectionUtils.isEmpty(superCashOfferRequest.getBody().getPayModes())
                    || (OrderSource.ORDER.name().equalsIgnoreCase(superCashOfferRequest.getBody().getSource()) && (superCashOfferRequest
                            .getBody().getPromoContext() == null || superCashOfferRequest.getBody().getPromoContext()
                            .isEmpty()))
                    || (superCashOfferRequest.getBody().getAmount() == null || !NumberUtils
                            .isNumber(superCashOfferRequest.getBody().getAmount()))
                    || StringUtils.isBlank(superCashOfferRequest.getBody().getMid())
                    || StringUtils.isBlank(superCashOfferRequest.getBody().getUserId())) {
                return true;
            }

            if ((superCashOfferRequest.getBody().getSource().equalsIgnoreCase("PG") && isOnusMerchant(superCashOfferRequest
                    .getBody().getMid()))
                    || (superCashOfferRequest.getBody().getSource().equalsIgnoreCase("ORDER") && !isOnusMerchant(superCashOfferRequest
                            .getBody().getMid()))) {
                LOGGER.error("source invalid for mid {}", superCashOfferRequest.getBody().getMid());
                return true;

            }

        }

        Double orderAmt = null;
        if (!offlineFlows && CollectionUtils.isNotEmpty(superCashOfferRequest.getBody().getPayModes())) {
            try {
                orderAmt = Double.parseDouble(superCashOfferRequest.getBody().getAmount());
                for (SuperCashOfferPaymode paymode : superCashOfferRequest.getBody().getPayModes()) {
                    if (Double.parseDouble(paymode.getAmount()) > orderAmt) {
                        return true;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while checking the amount of paymodes in supercash");
                return true;
            }
        }
        return false;
    }

    public SuperCashOfferServiceRequest transformServiceRequest(SuperCashOfferRequest superCashOfferRequest)
            throws SupercashException {

        SuperCashOfferServiceRequest serviceRequest = new SuperCashOfferServiceRequest();
        try {
            serviceRequest.setAmount(superCashOfferRequest.getBody().getAmount());
            serviceRequest.setHybrid(superCashOfferRequest.getBody().isHybrid());
            serviceRequest.setPayModes(trimAndValidatePIIPaymodes(superCashOfferRequest.getBody().getPayModes()));
            serviceRequest.setPgMid(superCashOfferRequest.getBody().getMid());
            serviceRequest.setSource(superCashOfferRequest.getBody().getSource());
            serviceRequest.setPromoContext(superCashOfferRequest.getBody().getPromoContext());

            if (StringUtils.isBlank(serviceRequest.getSource())) {
                if (isOnusMerchant(serviceRequest.getPgMid())) {
                    if ((superCashOfferRequest.getBody().getPromoContext() == null || superCashOfferRequest.getBody()
                            .getPromoContext().isEmpty())) {
                        LOGGER.error("Promo context is mandatory in case of onus merchant");
                        throw new SupercashException.SupercashMerchantDetailsFetchMappingException(
                                "Exception while fetching MerchantExtendedData");
                    }
                    serviceRequest.setSource(OrderSource.ORDER.getValue());
                } else {
                    serviceRequest.setSource(OrderSource.PG.getValue());
                }
            }
        } catch (SupercashException exp) {
            LOGGER.error("Exception occurred while preparing service request for supercash : {}", exp);
            throw exp;
        }
        return serviceRequest;
    }

    private List<SuperCashOfferPaymode> trimAndValidatePIIPaymodes(List<SuperCashOfferPaymode> paymodes)
            throws SupercashIllegalParamException {
        List<SuperCashOfferPaymode> updatedList = new ArrayList<>();
        List<String> piipaymodes = Arrays.stream(PIIPayModes.values()).map(Enum::name).collect(Collectors.toList());
        updatedList = paymodes.stream().filter(x -> piipaymodes.contains(x.getPaymentMode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(updatedList)) {
            LOGGER.error("No valid Paymodes are available.");
            throw new SupercashIllegalParamException(SupercashResponseConstants.ILLEGAL_PARAMS.getMessage());
        }
        return updatedList;
    }

    public Boolean isOnusMerchant(String mid) throws SupercashException.SupercashMerchantDetailsFetchMappingException {
        try {
            TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = JsonMapper.convertValue(
                    preRedisCacheServiceImpl.getMerchantExtendedDataByCache(mid),
                    TheiaMerchantExtendedDataResponse.class);
            if (theiaMerchantExtendedDataResponse != null
                    && theiaMerchantExtendedDataResponse.getExtendedInfo() != null) {
                String value = theiaMerchantExtendedDataResponse.getExtendedInfo().get(ON_PAYTM);
                LOGGER.debug("Value for key {} is {}.", ON_PAYTM, value);
                if (StringUtils.isNotBlank(value) && Boolean.parseBoolean(value)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while fetching if merchant is on us : {}", e);
            throw new SupercashException.SupercashMerchantDetailsFetchMappingException(
                    SupercashResponseConstants.MAPPING_SERVICE_ERROR.getMessage());
        }
        return false;
    }

    public SuperCashOfferServiceResponse getSuperCashOffers(SuperCashOfferServiceRequest serviceRequest, String userId)
            throws SupercashException {
        final long startTime = System.currentTimeMillis();
        try {
            return supercashService.serviceApplySupercashOffers(serviceRequest, userId);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error in hitting supercash api {}", e);
            throw new SupercashException.SupercashServiceException(
                    SupercashResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        } finally {
            LOGGER.info("Total time taken to call supercash api : {}", System.currentTimeMillis() - startTime);
        }
    }

    public SuperCashOfferResponse transformServiceResponse(SuperCashOfferServiceResponse serviceResponse)
            throws SupercashException {
        SuperCashOfferResponse response = new SuperCashOfferResponse();
        if (serviceResponse != null
                && (CollectionUtils.isNotEmpty(serviceResponse.getErrors()) || StringUtils.isNotBlank(serviceResponse
                        .getMessage()))) {
            LOGGER.error("Error received from supercash service : {}", serviceResponse.getErrors());
            throw new SupercashException.SupercashServiceException(serviceResponse.getMessage());
        }
        try {
            SuperCashOfferResponseBody body = new SuperCashOfferResponseBody();
            List<SuperCashPaymodes> paymodes = new ArrayList<>();

            if (serviceResponse.getPAYTM_DIGITAL_CREDIT() != null) {
                SuperCashPaymodes paymode = new SuperCashPaymodes();
                paymode.setPaymode(PIIPayModes.PAYTM_DIGITAL_CREDIT);
                paymode.setOffers(serviceResponse.getPAYTM_DIGITAL_CREDIT());
                paymodes.add(paymode);
            }
            if (serviceResponse.getBALANCE() != null) {
                SuperCashPaymodes paymode = new SuperCashPaymodes();
                paymode.setPaymode(PIIPayModes.BALANCE);
                paymode.setOffers(serviceResponse.getBALANCE());
                paymodes.add(paymode);
            }
            body.setSupercashPayModes(paymodes);
            response.setBody(body);
        } catch (Exception e) {
            LOGGER.error("Exception while transforming response :{}", e);
            throw new SupercashException.SupercashServiceException(
                    SupercashResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }
        return response;
    }

    public void searchSuperCashOffers(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse, WorkFlowResponseBean workFlowResponseBean,
            boolean merchantSolutionTypeOffline, String orderAmount) {
        try {

            String walletBalance = getWalletBalance(nativeCashierInfoResponse);
            EPayMode paymentFlow = nativeCashierInfoResponse.getBody().getPaymentFlow();
            boolean callApplySuperCash = false;

            if (EPayMode.ADDANDPAY == paymentFlow || EPayMode.HYBRID == paymentFlow) {
                if (checkValidAddnPayOrHybrid(orderAmount, walletBalance) && !merchantSolutionTypeOffline) {
                    callApplySuperCash = true;
                }
            }
            SuperCashOfferRequest superCashOfferRequest = new SuperCashOfferRequest();
            if (merchantSolutionTypeOffline) {
                Double postpaidBalance = getPaytmCCBalance(workFlowResponseBean, nativeCashierInfoRequest.getBody()
                        .getMid());
                superCashOfferRequest = prepareRequestForOfflineFlowSearchOffers(nativeCashierInfoRequest,
                        workFlowResponseBean.getUserDetails().getUserId(), walletBalance, postpaidBalance, orderAmount);
            } else {
                superCashOfferRequest = prepareRequestForSearchOffers(nativeCashierInfoRequest,
                        nativeCashierInfoResponse, workFlowResponseBean, walletBalance, orderAmount);
            }
            SuperCashOfferResponse superCashOfferResponse = fetchSuperCashOffers(superCashOfferRequest,
                    merchantSolutionTypeOffline);
            if (superCashOfferResponse != null && superCashOfferResponse.getStatus()) {
                SuperCashOffers superCashOffers = prepareResponseForSearchOffers(superCashOfferResponse,
                        callApplySuperCash);

                if (CollectionUtils.isNotEmpty(superCashOffers.getSupercashPayModes())) {
                    nativeCashierInfoResponse.getBody().setSuperCashOffers(superCashOffers);
                }
            } else {
                LOGGER.error("Error in getting SuperCashOffersServiceResponse: {}", superCashOfferResponse);
            }
        } catch (SupercashException e) {
            LOGGER.error("ERROR while searching supercashOffers in superCashServiceHelper",
                    ExceptionUtils.getStackTrace(e));
            throw new BaseException();
        }
    }

    private SuperCashOffers prepareResponseForSearchOffers(SuperCashOfferResponse superCashOfferResponse,
            boolean callApplySuperCash) throws SupercashException {
        SuperCashOffers superCashOffers = new SuperCashOffers();
        if (CollectionUtils.isNotEmpty(superCashOfferResponse.getBody().getSupercashPayModes())) {
            superCashOffers.setFetchSupercashOffers(callApplySuperCash);
            superCashOffers.setSupercashPayModes(superCashOfferResponse.getBody().getSupercashPayModes());
        }
        return superCashOffers;
    }

    private SuperCashOfferRequest prepareRequestForOfflineFlowSearchOffers(
            NativeCashierInfoRequest nativeCashierInfoRequest, String paytmUserId, String walletBalance,
            Double postpaidBalance, String orderAmount)
            throws SupercashException.SupercashMerchantDetailsFetchMappingException {

        EXT_LOGGER.customInfo("Preparing the request for offline flows");

        SuperCashOfferRequest superCashOfferRequest = new SuperCashOfferRequest();
        SuperCashOfferRequestBody superCashOfferRequestBody = new SuperCashOfferRequestBody();
        SsoTokenRequestHeader ssoTokenRequestHeader = new SsoTokenRequestHeader();
        Boolean isMerchantOnPaytm = isOnusMerchant(nativeCashierInfoRequest.getBody().getMid());
        setAmount(superCashOfferRequestBody, orderAmount);
        superCashOfferRequestBody.setMid(nativeCashierInfoRequest.getBody().getMid());
        superCashOfferRequestBody.setUserId(paytmUserId);
        if (isMerchantOnPaytm) {
            superCashOfferRequestBody.setSource(OrderSource.ORDER.getValue());
            if (nativeCashierInfoRequest.getBody().getApplyItemOffers() != null
                    && nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext() != null) {
                superCashOfferRequestBody.setPromoContext(nativeCashierInfoRequest.getBody().getApplyItemOffers()
                        .getPromoContext());
            }
        } else {
            superCashOfferRequestBody.setSource(OrderSource.PG.getValue());
        }
        List<SuperCashOfferPaymode> paymodesList = new ArrayList<>();

        String orderAmt = superCashOfferRequestBody.getAmount();
        EXT_LOGGER.customInfo("postpaid balance :: {} and wallet balance :: {}", postpaidBalance, walletBalance);

        if (walletBalance != null) {
            SuperCashOfferPaymode balancePaymodeOfferRequest = new SuperCashOfferPaymode();
            balancePaymodeOfferRequest.setPaymentMode(PIIPayModes.BALANCE.name());
            balancePaymodeOfferRequest.setAmount(orderAmt);
            paymodesList.add(balancePaymodeOfferRequest);
        }

        if (postpaidBalance != null && Double.parseDouble(orderAmt) <= postpaidBalance) {
            SuperCashOfferPaymode postpaidPaymodeOfferRequest = new SuperCashOfferPaymode();
            postpaidPaymodeOfferRequest.setPaymentMode(PIIPayModes.PAYTM_DIGITAL_CREDIT.name());
            postpaidPaymodeOfferRequest.setAmount(orderAmt);
            paymodesList.add(postpaidPaymodeOfferRequest);
        }

        superCashOfferRequestBody.setPayModes(paymodesList);
        superCashOfferRequest.setHead(ssoTokenRequestHeader);
        superCashOfferRequest.setBody(superCashOfferRequestBody);
        return superCashOfferRequest;
    }

    private SuperCashOfferRequest prepareRequestForSearchOffers(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse, WorkFlowResponseBean workFlowResponseBean,
            String walletBalance, String orderAmount)
            throws SupercashException.SupercashMerchantDetailsFetchMappingException {

        EXT_LOGGER.customInfo("Preparing the request for online flows");

        SuperCashOfferRequest superCashOfferRequest = new SuperCashOfferRequest();
        SuperCashOfferRequestBody superCashOfferRequestBody = new SuperCashOfferRequestBody();
        SsoTokenRequestHeader ssoTokenRequestHeader = new SsoTokenRequestHeader();

        // String txnAmount = orderAmount;
        Boolean isMerchantOnPaytm = isOnusMerchant(nativeCashierInfoRequest.getBody().getMid());
        superCashOfferRequestBody.setAmount(orderAmount);
        superCashOfferRequestBody.setMid(nativeCashierInfoRequest.getBody().getMid());
        superCashOfferRequestBody.setUserId(workFlowResponseBean.getUserDetails().getUserId());
        if (isMerchantOnPaytm) {
            superCashOfferRequestBody.setSource(OrderSource.ORDER.getValue());
            if (nativeCashierInfoRequest.getBody().getApplyItemOffers() != null
                    && nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext() != null) {
                superCashOfferRequestBody.setPromoContext(nativeCashierInfoRequest.getBody().getApplyItemOffers()
                        .getPromoContext());
            }
        } else {
            superCashOfferRequestBody.setSource(OrderSource.PG.getValue());
        }
        EPayMode paymentFlow = nativeCashierInfoResponse.getBody().getPaymentFlow();
        if (EPayMode.HYBRID == paymentFlow || EPayMode.ADDANDPAY == paymentFlow) {
            if (!checkValidAddnPayOrHybrid(orderAmount, walletBalance)) {
                paymentFlow = EPayMode.NONE;
            }
        }
        switch (paymentFlow) {
        case HYBRID:
            SuperCashOfferPaymode superCashOfferHybrid = new SuperCashOfferPaymode();
            superCashOfferHybrid.setPaymentMode(PIIPayModes.BALANCE.name());
            superCashOfferHybrid.setAmount(walletBalance);
            List<SuperCashOfferPaymode> payModes = new ArrayList<>();
            payModes.add(superCashOfferHybrid);
            superCashOfferRequestBody.setPayModes(payModes);
            superCashOfferRequestBody.setHybrid(true);
            break;
        case ADDANDPAY:
            SuperCashOfferPaymode superCashOfferAddnPay = new SuperCashOfferPaymode();
            superCashOfferAddnPay.setPaymentMode(PIIPayModes.BALANCE.name());
            superCashOfferAddnPay.setAmount(orderAmount);
            List<SuperCashOfferPaymode> balanceAddnPay = new ArrayList<>();
            balanceAddnPay.add(superCashOfferAddnPay);
            superCashOfferRequestBody.setPayModes(balanceAddnPay);
            break;
        case NONE:
            List<SuperCashOfferPaymode> nonePaymodes = new ArrayList<>();
            SuperCashOfferPaymode balancePaymodeOfferRequest = new SuperCashOfferPaymode();
            balancePaymodeOfferRequest.setPaymentMode(PIIPayModes.BALANCE.name());
            balancePaymodeOfferRequest.setAmount(orderAmount);
            nonePaymodes.add(balancePaymodeOfferRequest);

            if (workFlowResponseBean.getUserDetails() != null
                    && workFlowResponseBean.getUserDetails().isPaytmCCEnabled()) {
                SuperCashOfferPaymode postpaidPaymodeOfferRequest = new SuperCashOfferPaymode();
                postpaidPaymodeOfferRequest.setPaymentMode(PIIPayModes.PAYTM_DIGITAL_CREDIT.name());
                postpaidPaymodeOfferRequest.setAmount(orderAmount);
                nonePaymodes.add(postpaidPaymodeOfferRequest);
            }
            superCashOfferRequestBody.setPayModes(nonePaymodes);
            break;
        default:
            LOGGER.info("PaymentFlow not supported for supercash {} : " + paymentFlow.getValue());
            return superCashOfferRequest;
        }
        superCashOfferRequest.setHead(ssoTokenRequestHeader);
        superCashOfferRequest.setBody(superCashOfferRequestBody);
        return superCashOfferRequest;
    }

    private void setAmount(SuperCashOfferRequestBody superCashOfferRequestBody, String orderAmount) {
        if (NumberUtils.isNumber(orderAmount))
            superCashOfferRequestBody.setAmount(orderAmount);
        else
            superCashOfferRequestBody.setAmount("0.0");
    }

    public SuperCashOfferResponse fetchSuperCashOffers(SuperCashOfferRequest superCashOfferRequest,
            boolean merchantSolutionTypeOffline) {
        try {
            validateRequest(superCashOfferRequest, merchantSolutionTypeOffline);
            SuperCashOfferServiceRequest serviceRequest = transformServiceRequest(superCashOfferRequest);
            SuperCashOfferServiceResponse serviceResponse = getSuperCashOffers(serviceRequest, superCashOfferRequest
                    .getBody().getUserId());
            SuperCashOfferResponse response = transformServiceResponse(serviceResponse);
            response.setStatus(true);
            return response;
        } catch (Exception e) {
            SuperCashOfferResponse response = new SuperCashOfferResponse();
            response.setStatus(false);
            response.setError(e.getMessage());
            return response;
        }
    }

    public String getWalletBalance(NativeCashierInfoResponse nativeCashierInfoResponse)
            throws SupercashException.SupercashServiceException {
        for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : nativeCashierInfoResponse
                .getBody().getMerchantPayOption().getPayMethods()) {
            if (PayMethod.BALANCE.getMethod().equals(payMethod.getPayMethod())) {
                for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                    if (payChannelBase instanceof PPI) {
                        if (((PPI) payChannelBase).getBalanceInfo() != null
                                && ((PPI) payChannelBase).getBalanceInfo().getAccountBalance() != null) {
                            return ((PPI) payChannelBase).getBalanceInfo().getAccountBalance().getValue();
                        } else {
                            LOGGER.error("BalanceInfo received is NULL ");
                            throw new SupercashException.SupercashServiceException(
                                    SupercashResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
                        }
                    }
                }
            }
        }
        return null;
    }

    private Boolean checkValidAddnPayOrHybrid(String orderAmount, String walletBalance) {
        if (StringUtils.isNotBlank(orderAmount) && StringUtils.isNotBlank(walletBalance)) {
            long orderAmountInPaise = Long.parseLong(AmountUtils.getTransactionAmountInPaise(orderAmount));
            long walletBalanceInPaise = Long.parseLong(AmountUtils.getTransactionAmountInPaise(walletBalance));
            return walletBalanceInPaise < orderAmountInPaise;
        }
        return false;
    }

    public Double getPaytmCCBalance(WorkFlowResponseBean workFlowResponseBean, String mid) {
        if (workFlowResponseBean != null && workFlowResponseBean.getUserDetails() != null
                && workFlowResponseBean.getUserDetails().isPaytmCCEnabled()) {

            if (workFlowResponseBean.getPaytmCCResponse() != null
                    && CollectionUtils.isNotEmpty(workFlowResponseBean.getPaytmCCResponse().getResponse())) {
                return workFlowResponseBean.getPaytmCCResponse().getResponse().get(0).getAmount();
            }
            if (merchantPreferenceService.isPostpaidEnabledOnMerchant(mid, false)) {
                PaytmDigitalCreditRequest request = getPaytmDigitalCreditRequest(workFlowResponseBean
                        .getWorkFlowRequestBean().getPaytmMID(), null);
                try {
                    PaytmDigitalCreditResponse digitalCreditResponse = postPaidServiceHelper.checkBalance(request,
                            workFlowResponseBean.getUserDetails().getUserId());
                    if (digitalCreditResponse != null
                            && CollectionUtils.isNotEmpty(digitalCreditResponse.getResponse())
                            && digitalCreditResponse.getStatusCode() == 0
                            && "ACTIVE".equalsIgnoreCase(digitalCreditResponse.getResponse().get(0).getAccountStatus())) {
                        return digitalCreditResponse.getResponse().get(0).getAmount();
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception while fetching digital credit balance ", ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return null;
    }

    private PaytmDigitalCreditRequest getPaytmDigitalCreditRequest(String mid, String amount) {
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
        paytmDigitalCreditRequest.setPgmid(mid);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(amount)) {
            paytmDigitalCreditRequest.setAmount(Double.parseDouble(amount));
        }
        paytmDigitalCreditRequest
                .setFlowType(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        return paytmDigitalCreditRequest;
    }
}
