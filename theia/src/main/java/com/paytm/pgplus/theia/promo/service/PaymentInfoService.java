package com.paytm.pgplus.theia.promo.service;

import com.paytm.pgplus.cache.CardNetworkDetailResponse;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankDetailsDataService;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.mappingserviceclient.service.ICardNetworkDataService;
import com.paytm.pgplus.mappingserviceclient.service.IPayMethodDataService;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.promo.model.*;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service("paymentInfoDataService")
public class PaymentInfoService {

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    private IBankInfoDataService bankInfoDataService;

    @Autowired
    @Qualifier("bankDetailsDataServiceImpl")
    private IBankDetailsDataService bankDetailsDataService;

    @Autowired
    @Qualifier("payMethodDataServiceImpl")
    private IPayMethodDataService payMethodDataService;

    @Autowired
    @Qualifier("cardNetworkDataServiceImpl")
    private ICardNetworkDataService cardNetworkDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInfoService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PaymentInfoService.class);
    private static final String DEFAULT_BANK_LOGO = "default.png";
    private static final String DEFAULT_CARD_LOGO = "default.png";

    /**
     * This method bankdetails, cardNetworkDetails and payMethoddetails from
     * mapping service
     * 
     * @return {@link FetchPaymentInfoResponse}
     */
    public FetchPaymentInfoResponse getPaymentPromoAttributeResponse() {
        FetchPaymentInfoResponse response = null;
        LOGGER.info("Getting Data from Mapping Service...");
        BankDetailsResponse bankDetailsResponse = null;
        try {
            bankDetailsResponse = bankDetailsDataService.getBankDetailResponse();
            EXT_LOGGER.customInfo("Mapping response - BankDetailsResponse :: {}", bankDetailsResponse);
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        PayMethodDetailsResponse payMethodResponse = null;
        try {
            payMethodResponse = payMethodDataService.getAllPayMethodInfo();
            EXT_LOGGER.customInfo("Mapping response - PayMethodDetailsResponse :: {}", payMethodResponse);
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        CardNetworkDetailResponse cardNetworResponse = null;
        try {
            cardNetworResponse = cardNetworkDataService.getCardNetworkDetails();
            EXT_LOGGER.customInfo("Mapping response - CardNetworkDetailResponse :: {}", cardNetworResponse);
        } catch (MappingServiceClientException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        if (isValidResponse(bankDetailsResponse, payMethodResponse, cardNetworResponse)) {
            response = preparePaymentInfoResponse(bankDetailsResponse, payMethodResponse, cardNetworResponse);
        } else {
            response = new FetchPaymentInfoResponse(new ResponseHeader(), new FetchPaymentInfoResponseBody());
            ResultInfo resultInfo = new ResultInfo();

            resultInfo.setResultCode(ResultCode.SYSTEM_ERROR.getCode());
            resultInfo.setResultStatus(ResultCode.SYSTEM_ERROR.getResultStatus());
            resultInfo.setResultMsg(ResultCode.SYSTEM_ERROR.getResultMsg());

            response.getBody().setResultInfo(resultInfo);
        }

        return response;
    }

    private boolean isValidResponse(BankDetailsResponse bankDetailsResponse,
            PayMethodDetailsResponse payMethodResponse, CardNetworkDetailResponse cardNetworResponse) {
        if ((bankDetailsResponse != null && bankDetailsResponse.getBankDetailsList() != null && !bankDetailsResponse
                .getBankDetailsList().isEmpty())
                && (payMethodResponse != null && payMethodResponse.getPayMethodDetailsList() != null && !payMethodResponse
                        .getPayMethodDetailsList().isEmpty())
                && (cardNetworResponse != null && cardNetworResponse.getCardNetworkDetailsList() != null && !cardNetworResponse
                        .getCardNetworkDetailsList().isEmpty())) {
            return true;
        }
        LOGGER.info("Validation Failed for  bankDetails : {}, payMethod : {}, cardNetwork :{}", bankDetailsResponse,
                payMethodResponse, cardNetworResponse);
        return false;
    }

    private FetchPaymentInfoResponse preparePaymentInfoResponse(BankDetailsResponse bankDetailsResponse,
            PayMethodDetailsResponse payMethodResponse, CardNetworkDetailResponse cardNetworResponse) {
        FetchPaymentInfoResponse response = new FetchPaymentInfoResponse(new ResponseHeader(),
                new FetchPaymentInfoResponseBody());

        bankDetailsResponse
                .getBankDetailsList()
                .stream()
                .filter(Objects::nonNull)
                .forEach(
                        object -> object.setLogoUrl(getBankLogoPath(StringUtils.isNotBlank(object.getLogoUrl()) ? object
                                .getLogoUrl() : DEFAULT_BANK_LOGO)));

        cardNetworResponse
                .getCardNetworkDetailsList()
                .stream()
                .filter(Objects::nonNull)
                .forEach(
                        object -> object.setLogoUrl(getCardNetworkLogoPath(StringUtils.isNotBlank(object.getLogoUrl()) ? object
                                .getLogoUrl() : DEFAULT_CARD_LOGO)));

        response.getBody().setBankDetails(bankDetailsResponse.getBankDetailsList());
        response.getBody().setPayMethodDetails(payMethodResponse.getPayMethodDetailsList());
        response.getBody().setCardNetworkDetails(cardNetworResponse.getCardNetworkDetailsList());

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(ResultCode.SUCCESS.getCode());
        resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
        resultInfo.setResultMsg(ResultCode.SUCCESS.getResultMsg());
        response.getBody().setResultInfo(resultInfo);

        return response;
    }

    private String getCardNetworkLogoPath(String cardNetworkLogo) {
        String staticUrlPrefix = PaytmTLD.getStaticUrlPrefix();
        String logoPath = DEFAULT_NATIVE_LOGO_PATH_CARD;
        StringBuilder logoUrl = new StringBuilder("");
        cardNetworkLogo = formatLogoName(cardNetworkLogo);
        logoUrl.append(staticUrlPrefix).append(logoPath).append(cardNetworkLogo);
        return logoUrl.toString();
    }

    private String getBankLogoPath(String bankLogo) {
        String staticUrlPrefix = PaytmTLD.getStaticUrlPrefix();
        String logoPath = DEFAULT_NATIVE_LOGO_PATH_BANK;
        StringBuilder logoUrl = new StringBuilder("");
        bankLogo = formatLogoName(bankLogo);
        logoUrl.append(staticUrlPrefix).append(logoPath).append(bankLogo);
        return logoUrl.toString();
    }

    private String formatLogoName(String logoName) {
        if (logoName != null) {
            String[] arr = logoName.split(Pattern.quote("."));
            if (arr.length == 2) {
                String bankLogoPrefix = arr[0];
                String bankLogoSuffix = arr[1];
                logoName = bankLogoPrefix.toUpperCase() + "." + bankLogoSuffix;
            }
        }
        return logoName;
    }

    public boolean isValidRequest(FetchPaymentInfoRequest request) {
        if (request != null && validateToken(request))
            return true;
        return false;
    }

    private boolean validateToken(FetchPaymentInfoRequest request) {
        if (!verifyJwtToken(request)) {
            LOGGER.error("JWT Validation failed returning response");
            return false;
        }
        EXT_LOGGER.customInfo("JWT validated successfully");
        return true;
    }

    private boolean verifyJwtToken(FetchPaymentInfoRequest request) {
        return JWTWithHmacSHA256.verifyJsonWebToken(request.getHead().getToken());
    }

}
