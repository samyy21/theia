/**
 * 
 */
package com.paytm.pgplus.biz.core.user.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.user.service.IWallet;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.account.models.request.CheckBalanceRequest;
import com.paytm.pgplus.facade.account.models.response.CheckBalanceResponse;
import com.paytm.pgplus.facade.account.services.IAccountQuery;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author naman
 *
 */
@Service("walletServiceImpl")
public class WalletServiceImpl implements IWallet {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final String OAUTH = "OAUTH";

    @Autowired
    @Qualifier("accountQueryImpl")
    IAccountQuery accountQuery;

    @Loggable(state = TxnState.AUTH_FETCH_WALLET_BALANCE)
    @Override
    public GenericCoreResponseBean<Money> fetchWalletBalance(String userId, String adminToken, String orderId) {

        return fetchWalletBalance(userId, adminToken, orderId, true);
    }

    @Loggable(state = TxnState.AUTH_FETCH_WALLET_BALANCE)
    @Override
    public GenericCoreResponseBean<Money> fetchWalletBalance(String userId, String adminToken, String orderId,
            boolean isAdminToken) {

        CheckBalanceResponse response = null;

        try {

            CheckBalanceRequest request;

            if (isAdminToken) {
                request = new CheckBalanceRequest(adminToken, userId, orderId);
            } else {
                request = new CheckBalanceRequest(adminToken, OAUTH, false, userId, orderId);
            }

            response = accountQuery.checkBalance(request);
            LOGGER.info("Wallet Balance retrieved is :: {}", response);
            if (null != response && response.isSuccessfullyProcessed()) {
                return new GenericCoreResponseBean<Money>(response.getWalletBalance());
            }

        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching Wallet Balance due to ::{}", e);
        }
        return new GenericCoreResponseBean<Money>(response == null ? "Unable To fetch wallet balance"
                : response.getResponseMsg(), ResponseConstants.SYSTEM_ERROR);
    }

}
