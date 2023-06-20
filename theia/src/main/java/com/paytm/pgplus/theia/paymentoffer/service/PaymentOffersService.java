package com.paytm.pgplus.theia.paymentoffer.service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoServiceResponseBase;
import com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.SearchPaymentOffersServiceResponseV2;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchAllPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserIdRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyItemLevelPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyPromoResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchAllPaymentOffersResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchUserIdResponse;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_THEIA_CHANGE_REDEMPTION_TYPE;

@Service("paymentOffersService")
public class PaymentOffersService implements IPaymentOffersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersService.class);

    @Autowired
    @Qualifier("paymentOffersServiceHelper")
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("userMappingService")
    IUserMapping userMapping;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    FF4JUtil ff4JUtil;

    @Override
    public ApplyPromoResponse applyPromo(ApplyPromoRequest request, String version, String referenceId) {
        /* version is kept for different handling of response */

        promoValidations(request, referenceId);
        PromoServiceResponseBase promoServiceResponse = getApplyPromoResponse(request, referenceId);
        if (promoServiceResponse instanceof ApplyPromoServiceResponse) {
            return paymentOffersServiceHelper.prepareResponse(promoServiceResponse, request, version);
        } else {
            return paymentOffersServiceHelperV2.prepareResponse(promoServiceResponse, request, version);
        }
    }

    private void promoValidations(ApplyPromoRequest request, String referenceId) {
        PaymentOfferUtils.validate(request);
        PaymentOfferUtils.validateChannelId(request.getHead());

        // add cardTokenInfo validations
        validateMidInQueryParam(request.getBody().getMid());
        if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(request.getHead().getToken(), request
                    .getHead().getTokenType(), request.getBody(), request.getBody().getMid());
            if (StringUtils.isBlank(request.getBody().getCustId())) {
                request.getBody().setCustId(userDetailsBiz.getUserId());
            }
            request.getBody().setPaytmUserId(userDetailsBiz.getUserId());
        } else if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
            if (StringUtils.isBlank(request.getBody().getCustId())) {
                throw RequestValidationException.getException("CustId can't be blank for tokenType "
                        + request.getHead().getTokenType());
            }
        } else if (TokenType.ACCESS == request.getHead().getTokenType()) {
            CreateAccessTokenServiceRequest accessTokenServiceRequest = accessTokenUtils.validateAccessToken(request
                    .getBody().getMid(), referenceId, request.getHead().getToken());
            if (StringUtils.isBlank(request.getBody().getCustId())) {
                request.getBody().setCustId(accessTokenServiceRequest.getCustId());
            }
            if (StringUtils.isNotBlank(accessTokenServiceRequest.getPaytmSsoToken())) {
                UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(accessTokenServiceRequest
                        .getPaytmSsoToken(), TokenType.SSO, request.getBody(), request.getBody().getMid());
                request.getBody().setPaytmUserId(userDetailsBiz.getUserId());
                if (StringUtils.isBlank(request.getBody().getCustId())) {
                    request.getBody().setCustId(userDetailsBiz.getUserId());
                }
            }
            if (StringUtils.isBlank(request.getBody().getCustId())
                    && StringUtils.isBlank(request.getBody().getPaytmUserId())) {
                throw RequestValidationException.getException("CustId and paytm user id can't be blank for tokenType "
                        + request.getHead().getTokenType());
            }
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType())) {
            InitiateTransactionRequestBody orderDetails = nativeSessionUtil
                    .getOrderDetail(request.getHead().getToken());
            paymentOffersServiceHelper.setCustIdForTxnTokenFlow(request, orderDetails);
            String simplifiedPromoCode = paymentOffersServiceHelper.validateAndGetSimplifiedPromoCode(orderDetails,
                    request.getBody().getMid(), request.getBody().getOrderId());
            paymentOffersServiceHelper.validatePromoCode(simplifiedPromoCode, request.getBody().getPromocode());
            setPromoAmountIfPresentInSimplifiedPaymentOffers(orderDetails, request);

            request.getBody().setCartDetails(orderDetails.getSimplifiedPaymentOffers().getCartDetails());
            if (ff4JUtil.isFeatureEnabled(FEATURE_THEIA_CHANGE_REDEMPTION_TYPE, request.getBody().getMid())) {
                request.getBody().setChangeRedemptionTypetoCashBack(true);
            }

        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void setPromoAmountIfPresentInSimplifiedPaymentOffers(InitiateTransactionRequestBody orderDetails,
            ApplyPromoRequest request) {
        if (orderDetails != null && orderDetails.getSimplifiedPaymentOffers() != null) {
            String promoAmount = orderDetails.getSimplifiedPaymentOffers().getPromoAmount();
            if (StringUtils.isNotBlank(promoAmount) && request.getBody().getPaymentOptions() != null
                    && request.getBody().getPaymentOptions().size() == 1) {
                request.getBody().setTotalTransactionAmount(promoAmount);
                request.getBody().getPaymentOptions().get(0).setTransactionAmount(promoAmount);
            }
        }
    }

    private void validateMidInQueryParam(String requestMid) {
        if (!requestMid.equals(OfflinePaymentUtils.gethttpServletRequest().getParameter(
                TheiaConstant.RequestParams.Native.MID))) {
            throw BaseException.getException();
        }
    }

    @Override
    public FetchAllPaymentOffersResponse fetchAllPaymentOffers(FetchAllPaymentOffersRequest request, String referenceId) {
        PaymentOfferUtils.validate(request);
        PaymentOfferUtils.validateChannelId(request.getHead());
        validateMidInQueryParam(request.getBody().getMid());
        String simplifiedPromoCode = null;
        String paytmUserId = null;
        if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType())) {
            InitiateTransactionRequestBody orderDetails = nativeSessionUtil
                    .getOrderDetail(request.getHead().getToken());
            simplifiedPromoCode = paymentOffersServiceHelper.validateAndGetSimplifiedPromoCode(orderDetails, request
                    .getBody().getMid(), request.getBody().getOrderId());
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            accessTokenUtils.validateAccessToken(request.getBody().getMid(), referenceId, request.getHead().getToken());
        } else if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(request.getHead().getToken(), request
                    .getHead().getTokenType(), request.getBody(), request.getBody().getMid());
            paytmUserId = userDetailsBiz.getUserId();
        } else {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
        }
        if (!ff4JUtil.isMigrateBankOffersPromo(request.getBody().getMid())) {
            SearchPaymentOffersServiceResponse promoResp = paymentOffersServiceHelper.searchPaymentOffers(request,
                    paytmUserId);
            return paymentOffersServiceHelper.prepareResponse(promoResp, request, simplifiedPromoCode);
        } else {
            SearchPaymentOffersServiceResponseV2 promoResp = paymentOffersServiceHelperV2.searchPaymentOffers(request,
                    paytmUserId);
            return paymentOffersServiceHelperV2.prepareResponse(promoResp, request, simplifiedPromoCode);
        }
    }

    @Override
    public PromoServiceResponseBase getApplyPromoResponse(ApplyPromoRequest request, String referenceId) {

        if (!ff4JUtil.isMigrateBankOffersPromo(request.getBody().getMid())) {

            return paymentOffersServiceHelper.applyPromo(request);
        } else {
            return paymentOffersServiceHelperV2.applyPromoV2(request, referenceId);
        }
    }

    @Override
    public ApplyItemLevelPromoResponse applyItemLevelPromo(ApplyPromoRequest request, String referenceId) {

        promoValidations(request, referenceId);
        PromoServiceResponseBase promoServiceResponse = paymentOffersServiceHelperV2.applyPromoV2(request, null);
        return paymentOffersServiceHelperV2.prepareResponse(promoServiceResponse, request);

    }

    @Override
    public FetchUserIdResponse fetchUserId(FetchUserIdRequest request, String referenceId) throws Exception {

        PaymentOfferUtils.validate(request);

        if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {

            return paymentOffersServiceHelperV2.getVpaValidateResponse(request, referenceId);

        } else {

            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());

            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);

        }

    }

}
