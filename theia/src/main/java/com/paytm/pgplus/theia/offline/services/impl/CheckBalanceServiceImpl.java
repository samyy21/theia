package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.nativ.PostpaidServiceHelper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.FetchBalanceInfoException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.payview.DigitalCreditBalanceInfo;
import com.paytm.pgplus.theia.offline.model.request.DigitalCreditCheckBalanceRequest;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.DigitalCreditCheckBalanceResponse;
import com.paytm.pgplus.theia.offline.model.response.DigitalCreditCheckBalanceResponseBody;
import com.paytm.pgplus.theia.offline.model.response.DigitalCreditOnboardingInfo;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.services.ICheckBalanceService;
import com.paytm.pgplus.theia.offline.utils.AuthUtil;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;

/**
 * Created by rahulverma on 17/4/18.
 */
@Service("checkBalanceService")
public class CheckBalanceServiceImpl implements ICheckBalanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckBalanceServiceImpl.class);

    @Autowired
    private PostpaidServiceHelper postPaidServiceHelper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public DigitalCreditCheckBalanceResponse checkDigitalCreditBalance(DigitalCreditCheckBalanceRequest request) {
        validateRequest(request);
        try {
            PaytmDigitalCreditResponse paytmDigitalCreditResponse = postPaidServiceHelper.checkBalance(
                    getServiceRequest(request), getCustomerId(request.getHead().getToken()));
            return mapServiceResponse(request, paytmDigitalCreditResponse, request.getHead());
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception : {}", ExceptionUtils.getStackTrace(e));
            throw FetchBalanceInfoException.getException();
        }
    }

    private String getCustomerId(String token) throws FacadeCheckedException {
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz = authUtil.fetchUserDetails(token);
        if (!userDetailsBiz.isSuccessfullyProcessed() || userDetailsBiz.getResponse() == null) {
            throw new FacadeCheckedException();
        }
        return userDetailsBiz.getResponse().getUserId();
    }

    private DigitalCreditCheckBalanceResponse mapServiceResponse(DigitalCreditCheckBalanceRequest request,
            PaytmDigitalCreditResponse paytmDigitalCreditResponse, RequestHeader requestHeader) {
        DigitalCreditCheckBalanceResponse response = new DigitalCreditCheckBalanceResponse();
        DigitalCreditCheckBalanceResponseBody body = new DigitalCreditCheckBalanceResponseBody();
        response.setHead(new ResponseHeader(requestHeader));
        response.setBody(body);
        if (paytmDigitalCreditResponse.getStatusCode() == 0) {
            setDigitalCreditCheckBalanceResponseBody(body, paytmDigitalCreditResponse, request);
        } else {
            ResultInfo resultInfo = new ResultInfo(ResultCode.ACCOUNT_QUERY_FAIL.getResultStatus(),
                    ResultCode.ACCOUNT_QUERY_FAIL.getCode(), ResultCode.ACCOUNT_QUERY_FAIL.getCode(),
                    paytmDigitalCreditResponse.getStatusMessage());
            body.setResultInfo(resultInfo);
        }
        return response;
    }

    private void setDigitalCreditCheckBalanceResponseBody(DigitalCreditCheckBalanceResponseBody body,
            PaytmDigitalCreditResponse paytmDigitalCreditResponse, DigitalCreditCheckBalanceRequest request) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfoForSuccess();
        body.setResultInfo(resultInfo);
        body.setBalanceInfo(getBalanceInfo(paytmDigitalCreditResponse));
        body.setOnboardingInfo(getOnboardingInfo(paytmDigitalCreditResponse));
        setAccountInfo(request, body, paytmDigitalCreditResponse);

    }

    private void setAccountInfo(DigitalCreditCheckBalanceRequest request, DigitalCreditCheckBalanceResponseBody body,
            PaytmDigitalCreditResponse paytmDigitalCreditResponse) {
        List<CheckBalanceResponse> responseList = paytmDigitalCreditResponse.getResponse();
        if (responseList != null && !responseList.isEmpty()) {
            CheckBalanceResponse checkBalanceResponse = responseList.get(0);
            body.setAccountStatus(checkBalanceResponse.getAccountStatus());
            body.setDisplayMessage(checkBalanceResponse.getDisplayMessage());
            body.setInfoButtonMessage(checkBalanceResponse.getInfoButtonMessage());
            body.setEnable(getStatus(checkBalanceResponse, request));
        }
    }

    private DigitalCreditOnboardingInfo getOnboardingInfo(PaytmDigitalCreditResponse paytmDigitalCreditResponse) {
        List<CheckBalanceResponse> responseList = paytmDigitalCreditResponse.getResponse();
        if (responseList != null && !responseList.isEmpty()) {
            DigitalCreditOnboardingInfo onboardingInfo = new DigitalCreditOnboardingInfo();
            CheckBalanceResponse checkBalanceResponse = responseList.get(0);
            onboardingInfo.setKycCode(checkBalanceResponse.getKycCode());
            onboardingInfo.setKycSetName(checkBalanceResponse.getKycSetName());
            onboardingInfo.setKycVersion(checkBalanceResponse.getKycVersion());
            onboardingInfo.setMictLines(checkBalanceResponse.getMictLines());
            onboardingInfo.setFullTnCDetails(checkBalanceResponse.getFullTnCDetails());
            return onboardingInfo;
        }
        return null;
    }

    private PaytmDigitalCreditRequest getServiceRequest(DigitalCreditCheckBalanceRequest request) {
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
        paytmDigitalCreditRequest.setPgmid(request.getHead().getMid());
        paytmDigitalCreditRequest.setAmount(Double.parseDouble(request.getBody().getAmount()));
        paytmDigitalCreditRequest.setFlowType(PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        return paytmDigitalCreditRequest;
    }

    private DigitalCreditCheckBalanceResponse getErrorResponse(RequestHeader requestHeader) {
        DigitalCreditCheckBalanceResponse response = new DigitalCreditCheckBalanceResponse();
        DigitalCreditCheckBalanceResponseBody body = new DigitalCreditCheckBalanceResponseBody();
        response.setHead(new ResponseHeader(requestHeader));
        body.setResultInfo(OfflinePaymentUtils.resultInfo(ResultCode.ACCOUNT_QUERY_FAIL));
        return response;
    }

    private DigitalCreditBalanceInfo getBalanceInfo(PaytmDigitalCreditResponse paytmDigitalCreditResponse) {
        List<CheckBalanceResponse> responseList = paytmDigitalCreditResponse.getResponse();
        if (responseList != null || !responseList.isEmpty()) {
            CheckBalanceResponse checkBalanceResponse = responseList.get(0);
            Map<String, String> extendInfo = new HashMap<>();
            extendInfo.put(LENDER, checkBalanceResponse.getLender());
            extendInfo.put(LENDER_DESCRIPTION, checkBalanceResponse.getLenderDescription());
            extendInfo.put(OTP_REQUIRED, String.valueOf(checkBalanceResponse.isOtpRequired()));
            extendInfo.put(PASSCODE_REQUIRED, String.valueOf(checkBalanceResponse.isPasscodeRequired()));
            extendInfo.put(DISPLAY, String.valueOf(checkBalanceResponse.isDisplay()));
            return new DigitalCreditBalanceInfo(checkBalanceResponse.getAccountID(), new Money(
                    String.valueOf(checkBalanceResponse.getAmount())), extendInfo, false);
        }
        return null;
    }

    private boolean getStatus(CheckBalanceResponse checkBalanceResponse, DigitalCreditCheckBalanceRequest request) {
        switch (checkBalanceResponse.getAccountStatus()) {
        case PaytmDigitalCreditConstant.ACCOUNT_STATUS_DEACTIVE:
            return false;
        case PaytmDigitalCreditConstant.ACCOUNT_STATUS_FROZEN:
            return false;
        case PaytmDigitalCreditConstant.ACCOUNT_STATUS_ON_HOLD:
            return false;
        case PaytmDigitalCreditConstant.ACCOUNT_STATUS_NOT_ACTIVE:
            return true;
        case PaytmDigitalCreditConstant.ACCOUNT_STATUS_ACTIVE:
            return compareAndRetun(Double.valueOf(request.getBody().getAmount()), checkBalanceResponse.getAmount());
        default:
            return false;
        }
    }

    private boolean compareAndRetun(Double requestAmount, Double postpaidBalance) {
        return postpaidBalance.compareTo(requestAmount) >= 0;
    }

    private void validateRequest(DigitalCreditCheckBalanceRequest request) {
        LOGGER.info("Validating request {} ..", request);
        GenericFlowRequestBeanValidator<DigitalCreditCheckBalanceRequest> validator = new GenericFlowRequestBeanValidator<>(
                request);
        ValidationResultBean validationResultBean = validator.validate();
        if (!validationResultBean.isSuccessfullyProcessed() || !isSSOToken(request.getHead().getTokenType())) {
            LOGGER.error("Request validation failed ... {}", validationResultBean);
            LOGGER.error("Error = {}", validator.getErrorMessage());
            LOGGER.error("Property Path = {}", validator.getPropertyPath());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);

        }
        LOGGER.info("Validation successful");
    }

    private boolean isSSOToken(TokenType tokenType) {
        return TokenType.SSO.equals(tokenType);
    }
}
