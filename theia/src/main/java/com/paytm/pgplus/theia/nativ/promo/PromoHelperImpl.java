package com.paytm.pgplus.theia.nativ.promo;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.promo.service.client.model.PromoCodeApplyRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeBaseRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.model.PromoCodeValidateWithPaymentModeRequest;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.UPSHelper;
import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailRequest;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("promoHelperImpl")
public class PromoHelperImpl implements IPromoHelper {

    @Autowired
    @Qualifier("promoServiceV1Helper")
    private IPromoServiceHelper promoServiceHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginService;

    @Autowired
    private IAuthentication authFacade;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    public static final String PROMO_SUCCESS = "01";
    public static final String PROMO_APPLIED = "700";
    public static final String ADD_MONEY_FLAG_VALUE = "1";
    // In case of bugs/issues on Promo Service's end
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

    public static final Logger LOGGER = LoggerFactory.getLogger(PromoHelperImpl.class);

    public PromoCodeResponse applyPromoCode(WorkFlowRequestBean workFlowRequestBean, String transId, String txnAmount) {

        PromoCodeResponse applyPromoCodeResponse = new PromoCodeResponse();
        PromoCodeResponse promoValidationResponse = new PromoCodeResponse();
        PromoCodeResponse promoPaymentModeValidationResponse = new PromoCodeResponse();
        if (StringUtils.isEmpty(workFlowRequestBean.getPromoCampId()) || StringUtils.isEmpty(transId)
                || StringUtils.isEmpty(workFlowRequestBean.getPaymentTypeId())) {
            return null;
        }

        if (StringUtils.isNotBlank(workFlowRequestBean.getPromoCampId())
                && ff4jUtils.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(),
                        TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT, false)) {
            return null;
        }

        try {
            promoValidationResponse = validatePromoCode(workFlowRequestBean.getPromoCampId(),
                    workFlowRequestBean.getPaytmMID());
            if (promoValidationResponse == null) {
                return buildFailedPromoReponse();
            } else if (!PROMO_SUCCESS.equals(promoValidationResponse.getPromoResponseCode())) {
                return promoValidationResponse;
            }
            // If promo validated successfully , validate payment mode for this
            // promo
            String cardNum = workFlowRequestBean.getCardNo();
            if (StringUtils.isNotBlank(workFlowRequestBean.getSavedCardID())
                    && StringUtils.isNumeric(workFlowRequestBean.getSavedCardID())) {
                cardNum = getSavedCardNum(workFlowRequestBean);
            }

            String txnTypeOldName = workFlowRequestBean.getPaymentTypeId();
            if (null != workFlowRequestBean.getPaytmExpressAddOrHybrid()
                    && EPayMode.ADDANDPAY.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
                txnTypeOldName = EPayMethod.BALANCE.getOldName();
            }

            promoPaymentModeValidationResponse = validatePromoCodePaymentMode(workFlowRequestBean.getPaytmMID(),
                    txnTypeOldName, workFlowRequestBean.getPromoCampId(), cardNum, workFlowRequestBean.getBankCode());

            LOGGER.info("promoCodeResponse for validate promo with pay Mode for {}", promoPaymentModeValidationResponse);
            if (null == promoPaymentModeValidationResponse
                    || null == promoPaymentModeValidationResponse.getPromoCodeDetail()
                    || null == promoPaymentModeValidationResponse.getPromoCodeDetail().getPromoCode()) {
                promoPaymentModeValidationResponse = buildFailedPromoReponse();
                promoPaymentModeValidationResponse.setPromoCodeDetail(promoValidationResponse.getPromoCodeDetail());
            } else if (promoPaymentModeValidationResponse != null
                    && PROMO_SUCCESS.equals(promoPaymentModeValidationResponse.getPromoResponseCode())) {
                // Apply only in case the promo is valid
                PromoCodeApplyRequest promoCodeApplyRequest = createPromoCodeApplyRequest(workFlowRequestBean, transId,
                        txnAmount);
                applyPromoCodeResponse = promoServiceHelper.applyPromoCode(promoCodeApplyRequest);
                LOGGER.info("applyPromoCodeResponse for apply promo with pay Mode for {}", applyPromoCodeResponse);
            }
        } catch (final Exception ex) {
            LOGGER.info("Unable to apply promo code in flow{}", ex);
            promoPaymentModeValidationResponse = buildFailedPromoReponse();
            if (promoValidationResponse != null && promoValidationResponse.getPromoCodeDetail() != null) {
                promoPaymentModeValidationResponse.setPromoCodeDetail(promoValidationResponse.getPromoCodeDetail());
            }
            return promoPaymentModeValidationResponse;
        }

        LOGGER.info("promo code for Native flow response as false");
        if (applyPromoCodeResponse == null || applyPromoCodeResponse.getPromoCodeDetail() == null
                || StringUtils.isEmpty(promoValidationResponse.getPromoResponseCode())) {
            promoPaymentModeValidationResponse.setPromoCodeDetail(promoValidationResponse.getPromoCodeDetail());
            return promoPaymentModeValidationResponse;
        }
        return applyPromoCodeResponse;

    }

    @Override
    public PromoCodeResponse validatePromoCode(String promoCode, String mid) {

        if (StringUtils.isBlank(promoCode) || StringUtils.isBlank(mid)) {
            return null;
        }
        if (StringUtils.isNotBlank(promoCode)
                && ff4jUtils.isFeatureEnabledOnMid(mid, TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT,
                        false)) {
            return null;
        }
        LOGGER.info("Applying Promo code for native request : {} for mid : {}", promoCode, mid);
        PromoCodeBaseRequest promoCodeRequest = new PromoCodeBaseRequest();

        promoCodeRequest.setMerchantId(mid);
        promoCodeRequest.setPromoCode(promoCode);

        PromoCodeResponse promoCodeResponse = promoServiceHelper.validatePromoCode(promoCodeRequest);
        LOGGER.debug("Response received after validating promo code in native : {}", promoCodeResponse);

        return promoCodeResponse;
    }

    public PromoCodeResponse validatePromoCodePaymentMode(String paytmMid, String txntype, String promoCode,
            String cardNo, String bankCode) {

        final PromoCodeValidateWithPaymentModeRequest promoCodeValidateRequest = new PromoCodeValidateWithPaymentModeRequest();
        promoCodeValidateRequest.setMerchantId(paytmMid);
        promoCodeValidateRequest.setTxnMode(txntype);
        promoCodeValidateRequest.setPromoCode(promoCode);
        promoCodeValidateRequest.setCardNumber(cardNo);
        promoCodeValidateRequest.setBankName(bankCode);

        PromoCodeResponse promoCodeResponse = promoServiceHelper.validatePromoCodePaymentMode(promoCodeValidateRequest);

        return promoCodeResponse;

    }

    public PromoCodeResponse validatePromoCodePaymentMode(NativePromoCodeDetailRequest serviceReq) {

        PromoCodeResponse promoCodeResponse = null;

        String paytmMid = serviceReq.getBody().getMid();
        String txnTypeOldName = (null == EPayMethod.getPayMethodByMethod(serviceReq.getBody().getTxnType())) ? serviceReq
                .getBody().getTxnType() : EPayMethod.getPayMethodByMethod(serviceReq.getBody().getTxnType())
                .getOldName();
        String promoCode = serviceReq.getBody().getPromoCode();
        String cardNo = serviceReq.getBody().getCardNumber();
        String bankCode = serviceReq.getBody().getBankCode();

        String txnToken = serviceReq.getHead().getTxnToken();

        if (StringUtils.isNotBlank(promoCode)
                && ff4jUtils.isFeatureEnabledOnMid(paytmMid,
                        TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT, false)) {
            return buildFailedPromoReponse();
        }

        // here, the cardNo got in request is cardId because the last character
        // is '|'
        if (StringUtils.isNotBlank(cardNo) && cardNo.endsWith("|")) {
            String savedCardId = cardNo.replace("|", "");
            String savedCardNum = getSavedCardNumFromCardId(savedCardId, serviceReq);

            if (StringUtils.isNotBlank(savedCardNum)) {
                promoCodeResponse = validatePromoCodePaymentMode(paytmMid, txnTypeOldName, promoCode, savedCardNum,
                        bankCode);
            }
        } else {
            promoCodeResponse = validatePromoCodePaymentMode(paytmMid, txnTypeOldName, promoCode, cardNo, bankCode);
        }

        if (null == promoCodeResponse) {
            return buildFailedPromoReponse();
        }

        return promoCodeResponse;
    }

    private String getSavedCardNumFromCardId(final String cardId, NativePromoCodeDetailRequest request) {
        String savedCardNum = null;
        InitiateTransactionRequestBody orderDetail = new InitiateTransactionRequestBody();
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {

            orderDetail.setPaytmSsoToken(request.getHead().getToken());
            orderDetail.setMid(request.getBody().getMid());

        } else {
            orderDetail = nativeSessionUtil.getOrderDetail(request.getHead().getTxnToken());
        }

        boolean isStoreCardEnabled = false;
        boolean isCustIdPresent = false;

        if (orderDetail != null) {
            isStoreCardEnabled = merchantPreferenceService.isStoreCardEnabledForMerchant(orderDetail.getMid());

            if (orderDetail.getUserInfo() != null && StringUtils.isNotBlank(orderDetail.getUserInfo().getCustId())) {
                isCustIdPresent = true;
            }
        }

        try {
            if (StringUtils.isNotBlank(orderDetail.getPaytmSsoToken())) {
                String paytmSSOToken = orderDetail.getPaytmSsoToken();
                String clientId = configurationDataService.getPaytmProperty(
                        TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID).getValue();
                String clientSecret = configurationDataService.getPaytmProperty(
                        TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue();
                if (TokenType.SSO.equals(request.getHead().getTokenType())) {
                    savedCardNum = cashierUtilService.getCardNumer(Long.parseLong(cardId), request.getBody()
                            .getUserDetailsBiz().getUserId());
                } else {
                    final FetchUserDetailsResponse fetchUserDetailsResponse = getUserDetailsFromAuthFacadeService(
                            paytmSSOToken, clientId, clientSecret, orderDetail.getMid());

                    if ((fetchUserDetailsResponse != null) && (fetchUserDetailsResponse.getUserDetails() != null)) {
                        // get saved card number on basis of userId
                        savedCardNum = cashierUtilService.getCardNumer(Long.parseLong(cardId), fetchUserDetailsResponse
                                .getUserDetails().getUserId());
                    }
                }
            }
        } catch (Exception e1) {
            // this is when card number is not got from userid
        }

        try {
            if (StringUtils.isBlank(savedCardNum) && isStoreCardEnabled && isCustIdPresent) {
                LOGGER.info("card number not fetched on userId, now fetching on custId Mid...");

                // get cardnumber on mid custid in cashierUtilService
                SavedCardVO saveCardCustMid = cashierUtilService.getSavedCardDetailByCustMid(Long.parseLong(cardId),
                        orderDetail.getUserInfo().getCustId(), orderDetail.getMid());
                savedCardNum = saveCardCustMid.getCardNumber();
            }
        } catch (Exception e2) {
            LOGGER.error("card number not fetched on userId and custId-Mid as well");
        }

        return savedCardNum;
    }

    private FetchUserDetailsResponse getUserDetailsFromAuthFacadeService(final String paytmSSOToken,
            final String clientId, final String clientSecret, String mid) {
        try {
            final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(paytmSSOToken,
                    clientId, clientSecret, mid);
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.info("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.info("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);

            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (fetchUserDetailsResponse.getUserDetails() == null)) {
                LOGGER.error("User details fetching failed or UserDetails is null");
            } else {
                upsHelper.updateUserPostpaidAccStatusFromUPS(mid, fetchUserDetailsResponse.getUserDetails());
            }

            return fetchUserDetailsResponse;

        } catch (final Exception e) {
            LOGGER.error("Exception fetching user details from authFacade : ", e);
        }

        return null;
    }

    private PromoCodeApplyRequest createPromoCodeApplyRequest(WorkFlowRequestBean workFlowRequestBean, String transId,
            String txnAmount) throws PaytmValidationException {
        final PromoCodeApplyRequest promoCodeApplyRequest = new PromoCodeApplyRequest();
        promoCodeApplyRequest.setMerchantId(workFlowRequestBean.getPaytmMID());
        promoCodeApplyRequest.setPromoCode(workFlowRequestBean.getPromoCampId());
        promoCodeApplyRequest.setTxnAmount(new BigDecimal(txnAmount));

        promoCodeApplyRequest.setTxnId(transId);
        promoCodeApplyRequest.setCardNumber(workFlowRequestBean.getCardNo());
        promoCodeApplyRequest.setBankName(workFlowRequestBean.getBankCode());
        promoCodeApplyRequest.setUserID(workFlowRequestBean.getCustID());

        String txnTypeOldName = (null == EPayMethod.getPayMethodByMethod(workFlowRequestBean.getPaymentTypeId())) ? workFlowRequestBean
                .getPaymentTypeId() : EPayMethod.getPayMethodByMethod(workFlowRequestBean.getPaymentTypeId())
                .getOldName();
        if (EPayMode.ADDANDPAY.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
            promoCodeApplyRequest.setAddAndPay(true);
            txnTypeOldName = EPayMethod.BALANCE.getOldName();
        } else if (EPayMode.HYBRID.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
            promoCodeApplyRequest.setHybridTransaction(true);
            // txnTypeOldName = EPayMethod.BALANCE.getOldName();
        } else if (EPayMode.NONE.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())
                || EPayMode.NONE_KYC.equals(workFlowRequestBean.getPaytmExpressAddOrHybrid())) {
            // txnTypeOldName = EPayMethod.BALANCE.getOldName();
        }
        promoCodeApplyRequest.setTxnMode(txnTypeOldName);
        if (StringUtils.isNotBlank(workFlowRequestBean.getSavedCardID())
                && StringUtils.isNumeric(workFlowRequestBean.getSavedCardID())) {
            promoCodeApplyRequest.setSavedCardId(Long.parseLong(workFlowRequestBean.getSavedCardID()));
            promoCodeApplyRequest.setCardNumber(getSavedCardNum(workFlowRequestBean));
        }
        return promoCodeApplyRequest;
    }

    @Override
    public PromoCodeResponse updatePromoCode(WorkFlowRequestBean workFlowRequestBean, String transId, String txnAmount) {
        PromoCodeResponse applyPromoCodeResponse = new PromoCodeResponse();
        if (StringUtils.isEmpty(workFlowRequestBean.getPromoCampId()) || StringUtils.isEmpty(transId)
                || StringUtils.isEmpty(workFlowRequestBean.getPaymentTypeId())) {
            return null;
        }
        if (StringUtils.isNotBlank(workFlowRequestBean.getPromoCampId())
                && ff4jUtils.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(),
                        TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT, false)) {
            return null;
        }
        try {
            // Do not validate just call apply. It will update the current
            // midOrderId key with transId in promoService.
            PromoCodeApplyRequest promoCodeApplyRequest = createPromoCodeApplyRequest(workFlowRequestBean, transId,
                    txnAmount);
            applyPromoCodeResponse = promoServiceHelper.applyPromoCode(promoCodeApplyRequest);
            LOGGER.info("applyPromoCodeResponse for apply/update promo with pay Mode for {}", applyPromoCodeResponse);

            // set key in redis for close notify
            String[] txnIdParameters = StringUtils.split(transId, "|");
            if (txnIdParameters.length == 3) {
                /*
                 * In case txnId is set as mid|orderId|txnId in txnInfo, extract
                 * only txnId
                 */
                String key = new StringBuilder(CommonConstants.PROMO_CODE_KEY).append(txnIdParameters[2]).toString();
                theiaTransactionalRedisUtil.set(key, workFlowRequestBean.getPromoCampId(), Long.valueOf(3600));
            }

        } catch (final Exception ex) {
            LOGGER.info("Unable to update promo code in flow{}", ex);
        }
        return applyPromoCodeResponse;
    }

    private PromoCodeResponse buildFailedPromoReponse() {
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setResultStatus(ResponseCodeConstant.PROMO_INVALID);
        promoCodeResponse.setResultMsg(ResultCode.PROMO_CODE_INVALID.getResultMsg());
        promoCodeResponse.setPromoResponseCode(ResponseCodeConstant.PROMO_INVALID);
        return promoCodeResponse;
    }

    private String getSavedCardNum(WorkFlowRequestBean workFlowRequestBean) throws PaytmValidationException {
        String savedCardNum = null;

        boolean isStoreCardEnabled = workFlowRequestBean.isStoreCardPrefEnabled();
        boolean isCustIdPresent = false;

        if (isStoreCardEnabled && StringUtils.isNotBlank(workFlowRequestBean.getCustID())) {
            isCustIdPresent = true;
        }
        try {
            if (workFlowRequestBean.getToken() != null) {
                GenericCoreResponseBean<UserDetailsBiz> userDetails = loginService.fetchUserDetails(
                        workFlowRequestBean.getToken(), false, workFlowRequestBean);
                savedCardNum = cashierUtilService.getCardNumer(Long.parseLong(workFlowRequestBean.getSavedCardID()),
                        userDetails.getResponse().getUserId());
            }
        } catch (Exception e1) {
            // this is when card number is not got from userid
        }

        try {
            if (StringUtils.isBlank(savedCardNum) && isStoreCardEnabled && isCustIdPresent) {
                LOGGER.info("card number not fetched on userId, now fetching on custId Mid...");

                // get cardnumber on mid custid in cashierUtilService
                SavedCardVO saveCardCustMid = cashierUtilService.getSavedCardDetailByCustMid(
                        Long.parseLong(workFlowRequestBean.getSavedCardID()), workFlowRequestBean.getCustID(),
                        workFlowRequestBean.getPaytmMID());
                savedCardNum = saveCardCustMid.getCardNumber();
            }
        } catch (Exception e2) {
            LOGGER.error("card number not fetched on userId and custId-Mid as well");
        }

        return savedCardNum;
    }

}