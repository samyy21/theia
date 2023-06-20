package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.SearchPaymentOffersServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.userBase.SearchUserPaymentOffersResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.userBase.SearchUserPaymentOffersServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.services.IPaymentPromoSevice;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequestBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserPaymentOffersRequest;
import com.paytm.pgplus.theia.paymentoffer.model.request.FetchUserPaytmOfferServiceRequest;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchUserPaymentOffersResponse;
import com.paytm.pgplus.theia.paymentoffer.model.response.FetchUserPaymentOffersResponseBody;
import com.paytm.pgplus.theia.paymentoffer.model.response.PaymentOffersData;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.services.upiAccount.helper.CheckUPIAccountServiceHelper;
import com.paytm.pgplus.theiacommon.exception.BaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PAYTM_PAYMODE_PROMO_SUPPORTED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.REFERENCE_ID;
import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@Service
public class fetchUserPaymentOffersRequestProcessor
        extends
        AbstractRequestProcessor<FetchUserPaymentOffersRequest, FetchUserPaymentOffersResponse, FetchUserPaytmOfferServiceRequest, SearchUserPaymentOffersServiceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(fetchUserPaymentOffersRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(fetchUserPaymentOffersRequestProcessor.class);

    @Autowired
    private CheckUPIAccountServiceHelper checkUPIAccountServiceHelper;

    @Autowired
    @Qualifier("paymentPromoService")
    private IPaymentPromoSevice paymentPromoSevice;

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("paymentOffersServiceHelper")
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Override
    protected FetchUserPaytmOfferServiceRequest preProcess(FetchUserPaymentOffersRequest request) {
        PaymentOfferUtils.validateChannelId(request.getHead());
        FetchUserPaytmOfferServiceRequest fetchUserPaytmOfferServiceRequest = new FetchUserPaytmOfferServiceRequest();
        validateParams(request, fetchUserPaytmOfferServiceRequest);
        return fetchUserPaytmOfferServiceRequest;
    }

    @Override
    protected SearchUserPaymentOffersServiceResponse onProcess(FetchUserPaymentOffersRequest request,
            FetchUserPaytmOfferServiceRequest serviceReq) throws Exception {

        String userIdOAuth = fetchPaytmUserId(request);
        if (StringUtils.isBlank(userIdOAuth)) {
            throw com.paytm.pgplus.theia.offline.exceptions.BaseException.getException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        SearchUserPaymentOffersServiceResponse promoResp = searchPaymentOffers(request, request.getBody().getMid(),
                userIdOAuth);
        return promoResp;
    }

    @Override
    protected FetchUserPaymentOffersResponse postProcess(FetchUserPaymentOffersRequest request,
            FetchUserPaytmOfferServiceRequest serviceReq, SearchUserPaymentOffersServiceResponse serviceRes)
            throws Exception {

        FetchUserPaymentOffersResponse response = new FetchUserPaymentOffersResponse();
        response.setHead(PaymentOfferUtils.createResponseHeader());
        response.setBody(new FetchUserPaymentOffersResponseBody());
        response.getHead().setRequestId(request.getHead().getRequestId());
        response.getBody().setPaymentOffers(
                getPaymentOffers(serviceRes, request, serviceReq.getSimplifiedPromoCode(), response));
        return response;
    }

    private String fetchPaytmUserId(FetchUserPaymentOffersRequest request) {
        CheckUPIAccountRequest checkUPIAccountRequest = new CheckUPIAccountRequest();
        checkUPIAccountRequest.setBody(new CheckUPIAccountRequestBody(request.getBody().getMid(), request.getBody()
                .getMobileNo()));
        return checkUPIAccountServiceHelper.getUserIdFromOAuth(checkUPIAccountRequest);
    }

    public SearchUserPaymentOffersServiceResponse searchPaymentOffers(FetchUserPaymentOffersRequest request,
            String mid, String paytmUserId) throws BaseException {

        try {
            SearchPaymentOffersServiceRequestV2 searchPaymentOffersRequest = paymentOffersServiceHelperV2
                    .prepareSearchPaymentOffersServiceRequest();

            SearchUserPaymentOffersServiceResponse response = paymentPromoSevice.searchUserPaymentOffers(
                    searchPaymentOffersRequest,
                    paymentOffersServiceHelperV2.prepareQueryParamsV2(mid, paytmUserId, true));

            if (isValidResponse(response)) {
                return response;
            } else {
                LOGGER.error("Error in getting SearchUserPaymentOffersServiceResponse");
                throw BaseException.getException();
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new BaseException();
        }

    }

    private boolean isValidResponse(SearchUserPaymentOffersServiceResponse promoResp) {
        if (promoResp != null && CollectionUtils.isNotEmpty(promoResp.getItems())
                && promoResp.getItems().get(0) != null
                && CollectionUtils.isNotEmpty(promoResp.getItems().get(0).getData())) {
            return true;
        }
        return false;
    }

    private void validateParams(FetchUserPaymentOffersRequest request,
            FetchUserPaytmOfferServiceRequest fetchUserPaytmOfferServiceRequest) {
        String referenceId = httpServletRequest().getParameter(REFERENCE_ID);
        if (!request
                .getBody()
                .getMid()
                .equals(OfflinePaymentUtils.gethttpServletRequest()
                        .getParameter(TheiaConstant.RequestParams.Native.MID))) {
            throw com.paytm.pgplus.theia.offline.exceptions.BaseException.getException();
        }

        if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType())) {
            InitiateTransactionRequestBody orderDetails = nativeSessionUtil
                    .getOrderDetail(request.getHead().getToken());
            String simplifiedPromoCode = paymentOffersServiceHelper.validateAndGetSimplifiedPromoCode(orderDetails,
                    request.getBody().getMid(), request.getBody().getOrderId());
            fetchUserPaytmOfferServiceRequest.setSimplifiedPromoCode(simplifiedPromoCode);
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            accessTokenUtils.validateAccessToken(request.getBody().getMid(), referenceId, request.getHead().getToken());
        } else {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
        }
    }

    private List<PaymentOffersData> getPaymentOffers(SearchUserPaymentOffersServiceResponse serviceRes,
            FetchUserPaymentOffersRequest request, String simplifiedPromoCode,
            FetchUserPaymentOffersResponse fetchUserPaymentOffersResponse) {

        boolean onlyPaytmPaymodesOffer = merchantPreferenceService.showOnlyPaytmPaymodePaymentOffers(request.getBody()
                .getMid());
        List<String> configuredPaytmPaymodes = getPaytmPaymodesForBankOffer();

        List<SearchUserPaymentOffersResponseData> searchPaymentOffersResponseData = serviceRes.getItems().get(0)
                .getData();
        if (org.apache.commons.lang.StringUtils.isNotBlank(simplifiedPromoCode)) {
            searchPaymentOffersResponseData = searchPaymentOffersResponseData.parallelStream().filter(Objects::nonNull)
                    .filter(p -> (p.getPromocode().equalsIgnoreCase(simplifiedPromoCode))).collect(Collectors.toList());
        }

        List<PaymentOffersData> paymentOffersData = new ArrayList<>(searchPaymentOffersResponseData.size());

        for (SearchUserPaymentOffersResponseData offersResponseData : searchPaymentOffersResponseData) {

            if (onlyPaytmPaymodesOffer && !isDisjointPaymodes(offersResponseData, configuredPaytmPaymodes)) {
                EXT_LOGGER.customInfo("Filtering non paytm payment paymode");
                fetchUserPaymentOffersResponse.getBody().setPaytmOffersAvailable(true);
                continue;
            }
            PaymentOffersData data = new PaymentOffersData();
            data.setPromocode(offersResponseData.getPromocode());
            data.setOffer(offersResponseData.getOffer());
            data.setIsPromoVisible(offersResponseData.getIsPromoVisible());
            data.setValidFrom(PaymentOfferUtils.dateStringToMillis(offersResponseData.getValidFrom(), DATE_FORMAT));
            data.setValidUpto(PaymentOfferUtils.dateStringToMillis(offersResponseData.getValidUpto(), DATE_FORMAT));
            data.setTermsTitle(offersResponseData.getTermsTitle());
            data.setTermsUrl(offersResponseData.getTerms());
            paymentOffersData.add(data);

        }

        // setting flag if paytm paymode promo offers available
        if (!onlyPaytmPaymodesOffer) {
            setPaytmOffersFlagForNonPreferenceMerchants(searchPaymentOffersResponseData, configuredPaytmPaymodes,
                    fetchUserPaymentOffersResponse);
        }

        return paymentOffersData;
    }

    private boolean isDisjointPaymodes(SearchUserPaymentOffersResponseData offersResponseData,
            List<String> configuredPaytmPaymodes) {

        if (offersResponseData.getPaymentDetails() != null
                && CollectionUtils.isNotEmpty(offersResponseData.getPaymentDetails().getPayMethods())) {

            return offersResponseData.getPaymentDetails().getPayMethods().stream()
                    .anyMatch(element -> configuredPaytmPaymodes.contains(element.getMethod()));
        }

        return false;

    }

    private List<String> getPaytmPaymodesForBankOffer() {
        List<String> paytmPaymodes = new ArrayList<>();
        String configuredPaymodes = com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                PAYTM_PAYMODE_PROMO_SUPPORTED, "");
        paytmPaymodes = Arrays.asList(configuredPaymodes.split(Pattern.quote(",")));
        return paytmPaymodes;
    }

    private void setPaytmOffersFlagForNonPreferenceMerchants(
            List<SearchUserPaymentOffersResponseData> searchPaymentOffersResponseData,
            List<String> configuredPaytmPaymodes, FetchUserPaymentOffersResponse fetchUserPaymentOffersResponse) {

        for (SearchUserPaymentOffersResponseData offersResponseData : searchPaymentOffersResponseData) {

            if (isDisjointPaymodes(offersResponseData, configuredPaytmPaymodes)) {
                fetchUserPaymentOffersResponse.getBody().setPaytmOffersAvailable(true);
                break;
            }
        }
    }

}
