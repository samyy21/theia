package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.SavingsAccountInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;

/**
 * @author kartik
 * @date 20-Sep-2017
 */
@Component("paymentsBankAccountSessionUtil")
public class PaymentsBankAccountSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsBankAccountSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    private static final String PPB_BANK_CODE = "PPBL";

    public void setPaymentsBankAccountInfoInSession(PaymentRequestBean requestBean,
            WorkFlowResponseBean workFlowResponseBean) {

        SavingsAccountInfo savingsAccountInfo = theiaSessionDataService.getSavingsAccountInfoFromSession(
                requestBean.getRequest(), true);
        if (workFlowResponseBean.getUserDetails() != null && workFlowResponseBean.getAccountBalanceResponse() != null) {
            AccountBalanceResponse accountBalanceResponse = workFlowResponseBean.getAccountBalanceResponse();
            savingsAccountInfo.setSavingsAccountEnabled(true);
            savingsAccountInfo.setPaymentsBankCode(PPB_BANK_CODE);
            savingsAccountInfo.setAccountNumber(accountBalanceResponse.getAccountNumber());
            savingsAccountInfo.setEffectiveBalance(accountBalanceResponse.getEffectiveBalance());
            savingsAccountInfo.setSlfdBalance(accountBalanceResponse.getSlfdBalance());
            savingsAccountInfo.setPaymentRetryCount(TheiaConstant.PaymentsBankConfiguration.MAX_PASS_CODE_ATTEMPS);
            savingsAccountInfo.setAccountRefId(requestBean.getAccount_ref_id());

            // Check with PaymentType & TxnAmount to Activate PPB
            EPayMode allowedPayMode = workFlowResponseBean.getAllowedPayMode();
            final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(requestBean.getRequest());
            Double txnAmount = Double.parseDouble(txnInfo.getTxnAmount());
            // PPB configured as a NB channel -> Consider fee configured
            // corresponding to NB
            Double chargeFeeAmount = Double.parseDouble(String.valueOf(txnInfo.getChargeFeeAmountNetBanking()));
            Double savingsAccountBalance = Double.parseDouble(savingsAccountInfo.getEffectiveBalance());

            WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(requestBean.getRequest());
            boolean savingsAccountInactive = false;
            if (allowedPayMode != null) {
                switch (allowedPayMode) {
                case NONE:
                    // If PPB balance < Txn Amount -> Inactive
                    savingsAccountInactive = (txnAmount + chargeFeeAmount) > savingsAccountBalance ? true : false;
                    break;
                case HYBRID:
                case ADDANDPAY:
                    if (walletInfo != null && walletInfo.isWalletEnabled()) {
                        Double walletBalance = walletInfo.getWalletBalance();
                        Double differenceAmount = txnAmount + chargeFeeAmount - walletBalance;
                        savingsAccountInactive = differenceAmount > savingsAccountBalance ? true : false;
                    }
                    break;
                }
            }
            savingsAccountInfo.setSavingsAccountInactive(savingsAccountInactive);
            if (savingsAccountInactive) {
                savingsAccountInfo
                        .setPaymentRetryCount(TheiaConstant.PaymentsBankConfiguration.PASS_CODE_INVALID_ATTEMPTS);
            }
        }
    }

}
