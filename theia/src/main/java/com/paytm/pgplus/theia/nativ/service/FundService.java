package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.facade.enums.FundServiceUrl;
import com.paytm.pgplus.facade.fundService.models.request.*;
import com.paytm.pgplus.facade.fundService.models.response.CheckLoyaltyBalanceResponse;
import com.paytm.pgplus.facade.utils.FundServiceClient;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponseBody;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.FetchBalanceInfoException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FundService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundService.class);

    @Autowired
    FundServiceClient fundServiceClient;

    public CheckLoyaltyBalanceResponse getAccountBalanceFromFundService(BalanceInfoServiceRequest serviceRequest) {
        LOGGER.info("Creating request for loyalty points balance");
        CheckLoyaltyBalanceRequest balanceRequest = new CheckLoyaltyBalanceRequest(new CheckLoyaltyBalanceRequestBody());
        if (StringUtils.isBlank(serviceRequest.getSsoToken())) {
            LOGGER.error("SSO token required for fetching loyalty points balance");
            throw new FetchBalanceInfoException(OfflinePaymentUtils.resultInfo(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE));
        }
        try {
            CheckLoyaltyBalanceResponse response = fundServiceClient.execute(balanceRequest,
                    FundServiceUrl.CHECK_USER_BALANCE, CheckLoyaltyBalanceResponse.class, serviceRequest.getSsoToken());
            if (response == null || response.getResult() == null) {
                LOGGER.error("Invalid response from fund service");
                throw FetchBalanceInfoException.getException();
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Exception fetching Loyalty Point Balance", e);
            throw FetchBalanceInfoException.getException();
        }
    }

    public CheckLoyaltyBalanceResponse getAccountBalanceFromFundServiceV2(FetchBalanceInfoRequest serviceRequest,
            String customerId) {
        LOGGER.info("Creating request for loyalty points balance");
        CheckLoyaltyConsultRequest balanceRequest = new CheckLoyaltyConsultRequest(new CheckLoyaltyConsultBaseRequest(
                new CheckLoyaltyConsultRequestBody()));
        balanceRequest.getRequest().getBody().setPgMID(serviceRequest.getBody().getMid());
        balanceRequest.getRequest().getBody().setExchangeRate((serviceRequest.getBody().getExchangeRate()));
        balanceRequest.getRequest().getBody().setUserId(customerId);

        try {
            CheckLoyaltyBalanceResponse response = fundServiceClient.executeV2(balanceRequest,
                    FundServiceUrl.CHECK_USER_BALANCE_V2, CheckLoyaltyBalanceResponse.class);
            if (response == null || response.getResult() == null) {
                LOGGER.error("Invalid response from fund service");
                throw FetchBalanceInfoException.getException();
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Exception fetching Loyalty Point Balance", e);
            throw FetchBalanceInfoException.getException();
        }
    }

    public BalanceInfoResponseBody mapLoyaltyPointsResponseToBody(CheckLoyaltyBalanceResponse response) {
        BalanceInfoResponseBody body = new BalanceInfoResponseBody();

        LOGGER.info("Mapping loyalty points response to fetch balance response {}", response);
        if (!TheiaConstant.ExtraConstants.SUCCESS.equals(response.getResult().getResultCode())) {
            if (!TheiaConstant.ExtraConstants.EXCHANGE_RATE_INVALID.equals(response.getResult().getResultCode())) {
                body.setResultInfo(new ResultInfo(ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultStatus(),
                        ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultCodeId(),
                        ResultCode.PLATFORM_SYSTEM_UNAVAILABLE.getResultMsg()));
                return body;
            } else {
                body.setResultInfo(new ResultInfo(ResultCode.EXCHANGE_RATE_INVALID.getResultStatus(),
                        ResultCode.EXCHANGE_RATE_INVALID.getResultCodeId(), ResultCode.EXCHANGE_RATE_INVALID
                                .getResultMsg()));
                return body;
            }
        }
        body.setResultInfo(new ResultInfo(ResultCode.SUCCESS.getResultStatus(), java.lang.String
                .valueOf(ResultCode.SUCCESS.getResultCodeId()), ResultCode.SUCCESS.getResultMsg()));
        body.setAvailablePoints(response.getActiveBalance());
        body.setExchangeRate(response.getExchangeRate());
        String balanceAmount = getBalanceAmountFromLP(response.getActiveBalance(), response.getExchangeRate());
        if (StringUtils.isNotBlank(balanceAmount)) {
            Money accountInfo = new Money(EnumCurrency.INR, balanceAmount);
            body.setBalanceInfo(accountInfo);
        }
        return body;
    }

    public String getBalanceAmountFromLP(String activeLPBalance, String exchangeRate) {
        try {
            if (!NumberUtils.isNumber(activeLPBalance) || !NumberUtils.isNumber(exchangeRate)
                    || !(Double.parseDouble(exchangeRate) > 0)) {
                LOGGER.error("Received invalid Loyalty Point Balance : {} or Exchange Rate : {}", activeLPBalance,
                        exchangeRate);
                return null;
            }
            double LoyaltyPointBalance = Double.parseDouble(activeLPBalance) / Double.parseDouble(exchangeRate);
            BigDecimal bd = new BigDecimal(LoyaltyPointBalance).setScale(2, RoundingMode.DOWN);
            double balanceAmount = bd.doubleValue();
            String formattedBalanceAmount = String.valueOf(balanceAmount);
            LOGGER.info("Formatted Amount is :: {}", formattedBalanceAmount);
            return formattedBalanceAmount;
        } catch (Exception e) {
            LOGGER.error("Received invalid Loyalty Point Balance : {} or Exchange Rate : {}", activeLPBalance,
                    exchangeRate);
        }
        return null;
    }
}
