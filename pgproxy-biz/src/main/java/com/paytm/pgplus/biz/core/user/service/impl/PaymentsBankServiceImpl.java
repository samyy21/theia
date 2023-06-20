package com.paytm.pgplus.biz.core.user.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.user.service.IPaymentsBankService;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceRequest;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.ppb.services.IPaymentsBankAccountQuery;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author kartik
 * @date 17-Sep-2017
 */
@Service("paymentsBankServiceImpl")
public class PaymentsBankServiceImpl implements IPaymentsBankService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsBankServiceImpl.class);

    @Autowired
    @Qualifier("paymentsBankAccountQueryImpl")
    private IPaymentsBankAccountQuery paymentsBankAccountQuery;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Override
    public GenericCoreResponseBean<AccountBalanceResponse> fetchSavingsAccountBalance(
            WorkFlowTransactionBean workFlowTransBean) {

        if (workFlowTransBean == null) {
            return new GenericCoreResponseBean<>("Unable to fetch savings account details ! null workFlowTransBean.");
        }
        UserDetailsBiz userDetailsBiz = workFlowTransBean.getUserDetails();
        FetchAccountBalanceRequest fetchAccountBalanceRequest = new FetchAccountBalanceRequest();
        fetchAccountBalanceRequest.setToken(userDetailsBiz.getUserToken());
        fetchAccountBalanceRequest.setUserId(userDetailsBiz.getUserId());
        fetchAccountBalanceRequest.setAccountRefId(workFlowTransBean.getWorkFlowBean().getAccountRefId());
        if (ERequestType.RESELLER.name().equals(workFlowTransBean.getWorkFlowBean().getRequestType().name())) {
            fetchAccountBalanceRequest.setAccountType(RequestParams.CURRENT_ACCOUNT);
        } else {
            fetchAccountBalanceRequest.setAccountType(RequestParams.SAVING_ACCOUNT);
        }
        return fetchSavingsAccountBalance(fetchAccountBalanceRequest);
    }

    @Override
    public GenericCoreResponseBean<AccountBalanceResponse> fetchSavingsAccountBalance(FetchAccountBalanceRequest request) {
        if (request == null || StringUtils.isEmpty(request.getToken())) {
            return new GenericCoreResponseBean<>("Unable to fetch savings account details ! Blank token found.");
        }
        try {
            final FetchAccountBalanceResponse bmwResponse = paymentsBankAccountQuery
                    .queryAccountDetailsFromBankMiddleware(request);
            if (!"SUCCESS".equalsIgnoreCase(bmwResponse.getStatus())) {
                return new GenericCoreResponseBean<>(
                        "Unable to fetch savings account details ! Received FAILURE status from Bank Middleware");
            }
            AccountBalanceResponse accountBalanceResponse = mappingUtil.mapSavingAccountDetails(bmwResponse);
            return new GenericCoreResponseBean<AccountBalanceResponse>(accountBalanceResponse);
        } catch (FacadeCheckedException | FacadeUncheckedException e) {
            LOGGER.error("Exception occurred while fetching savings account details for userId : {} ",
                    request.getUserId(), e);
        }

        return new GenericCoreResponseBean<>("Exception occurred while fetching savings account details for userId : "
                + request.getUserId());

    }

}
