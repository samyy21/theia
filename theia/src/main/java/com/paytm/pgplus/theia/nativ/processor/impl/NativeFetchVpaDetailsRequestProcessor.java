package com.paytm.pgplus.theia.nativ.processor.impl;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.remove;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.nativ.model.vpa.details.FetchVpaDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.vpa.details.VpaDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.vpa.details.VpaDetailsResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;

@Service
public class NativeFetchVpaDetailsRequestProcessor
        extends
        AbstractRequestProcessor<FetchVpaDetailsRequest, VpaDetailsResponse, FetchVpaDetailsRequest, VpaDetailsResponse> {

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("vpaHelper")
    private VPAHelper vpaHelper;

    @Override
    protected FetchVpaDetailsRequest preProcess(FetchVpaDetailsRequest request) {
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            validateWithSsoToken(request);

        } else {
            validate(request);
        }
        return request;
    }

    @Override
    protected VpaDetailsResponse onProcess(FetchVpaDetailsRequest request, FetchVpaDetailsRequest serviceRequest) {
        VpaDetailsResponseBody responseBody = new VpaDetailsResponseBody();
        String paytmSsoToken = null;
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            paytmSsoToken = request.getHead().getToken();
        } else {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(serviceRequest.getHead()
                    .getTxnToken());
            paytmSsoToken = orderDetail.getPaytmSsoToken();
        }
        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest(paytmSsoToken);
        if (serviceRequest.getBody() != null) {
            fetchUserPaytmVpaRequest.setUserId(serviceRequest.getBody().getUserId());
        }
        GenericCoreResponseBean<UserProfileSarvatra> userProfileSarvatra = sarvatraVpaDetails
                .fetchUserProfileVpa(fetchUserPaytmVpaRequest);
        if (null != userProfileSarvatra && userProfileSarvatra.getResponse() != null
                && "SUCCESS".equalsIgnoreCase(userProfileSarvatra.getResponse().getStatus())) {
            responseBody.setSarvatraUserProfile(userProfileSarvatra.getResponse());
            responseBody.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(userProfileSarvatra.getResponse()
                    .getResponse()));
        }
        return new VpaDetailsResponse(new ResponseHeader(), responseBody);
    }

    @Override
    protected VpaDetailsResponse postProcess(FetchVpaDetailsRequest request, FetchVpaDetailsRequest serviceRequest,
            VpaDetailsResponse serviceResponse) {
        serviceResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());

        // Populate BankAccounts in vpa response
        if (request.getBody().isMultiAccForVpaSupported() && serviceResponse.getBody().getSarvatraUserProfile() != null) {
            vpaHelper.populateVPALinkedBankAccounts(serviceResponse.getBody().getSarvatraUserProfile());
        }
        return serviceResponse;
    }

    private void validate(FetchVpaDetailsRequest request) {

        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request.getHead().getTxnToken());
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();

        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        UserDetailsBiz userDetailsBiz = validateSsoToken(nativeInitiateRequest, orderDetail);
        if (request.getBody() != null && userDetailsBiz != null) {
            request.getBody().setUserId(userDetailsBiz.getUserId());
        }
    }

    private UserDetailsBiz validateSsoToken(NativeInitiateRequest nativeInitiateRequest,
            InitiateTransactionRequestBody orderDetail) {
        if (!isEmpty(orderDetail.getPaytmSsoToken())) {
            /*
             * This is the case when ssoToken has already been validated in
             * /initiateTxn API, so not validating again
             */
            if (nativeInitiateRequest.getNativePersistData() == null
                    || nativeInitiateRequest.getNativePersistData().getUserDetails() == null) {
                UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(
                        orderDetail.getPaytmSsoToken(), orderDetail.getMid());
                return userDetailsBiz;
            }
            return nativeInitiateRequest.getNativePersistData().getUserDetails();
        }
        return null;
    }

    private void validateWithSsoToken(FetchVpaDetailsRequest request) {
        nativeValidationService.validateMid(request.getBody().getMid());
        UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(request.getHead().getToken(), request
                .getBody().getMid());
        request.getHead().setTxnToken(
                nativeSessionUtil.createTokenForMidSSOFlow(request.getHead().getToken(), request.getBody().getMid()));
        if (request.getBody() != null && userDetailsBiz != null) {
            request.getBody().setUserId(userDetailsBiz.getUserId());
        }
    }

}