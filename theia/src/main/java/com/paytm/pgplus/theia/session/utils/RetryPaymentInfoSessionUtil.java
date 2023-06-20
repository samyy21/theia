package com.paytm.pgplus.theia.session.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.RetryPaymentInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;

@Component("retryPaymentInfoSessionUtil")
public class RetryPaymentInfoSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryPaymentInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    public void setRetryPaymentInfoIntoSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {

        LOGGER.debug("WorkFLowResponseBeana :{}", responseData);
        final RetryPaymentInfo retryPaymentInfo = theiaSessionDataService.getRetryPaymentInfoFromSession(
                requestData.getRequest(), true);
        LOGGER.debug("Found data in session, retryPaymentInfo :{}", retryPaymentInfo);

        String cardNumber = (String) requestData.getRequest().getAttribute(RetryConstants.CARD_NUMBER);
        String emiBankName = (String) requestData.getRequest().getAttribute(RetryConstants.ISSUING_BANK_NAME);
        String emiPlanId = (String) requestData.getRequest().getAttribute(RetryConstants.EMI_PLAN_ID);
        String paymentMode = requestData.getRequest().getParameter(RetryConstants.PAYMENT_MODE);
        String payMethod = requestData.getRequest().getParameter(ExtraConstants.PAY_METHOD);
        String isSavedCardTxn = (String) requestData.getRequest().getAttribute(RetryConstants.SAVED_CARD_TXN);
        // Setting payment details
        if (RetryConstants.NO.equals(isSavedCardTxn)) {
            switch (payMethod) {
            case ExtraConstants.CREDIT_CARD:
            case ExtraConstants.DEBIT_CARD:
            case ExtraConstants.WALLET_TYPE:
                setCardNumber(retryPaymentInfo, cardNumber);
                retryPaymentInfo.setPaymentMode(paymentMode.toLowerCase());
                break;
            case ExtraConstants.EMI:
                retryPaymentInfo.setEmiBankName(emiBankName);
                retryPaymentInfo.setEmiPlanId(emiPlanId);
                retryPaymentInfo.setPaymentMode(RetryConstants.EMI_PAYMENT_MODE);
                setCardNumber(retryPaymentInfo, cardNumber);
                break;
            case ExtraConstants.UPI:
                retryPaymentInfo.setPaymentMode(RetryConstants.UPI);
                TransactionInfo transactionInfo = theiaSessionDataService.getTxnInfoFromSession(requestData
                        .getRequest());
                if (transactionInfo != null)
                    transactionInfo.setPaymentTypeId(RetryConstants.UPI);
                break;
            default:
                break;
            }

        } else if (RetryConstants.YES.equals(isSavedCardTxn)) {
            retryPaymentInfo.setPaymentMode(RetryConstants.SC_PAYMENT_MODE);
        } else if (ExtraConstants.NET_BANKING.equals(payMethod)) {
            retryPaymentInfo.setPaymentMode(paymentMode.toLowerCase());
        } else if (ExtraConstants.UPI.equals(payMethod)) {
            retryPaymentInfo.setPaymentMode(paymentMode.toLowerCase());
            TransactionInfo transactionInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());
            if (transactionInfo != null)
                transactionInfo.setPaymentTypeId(RetryConstants.UPI);
        }
        // End

        // Setting error message
        String errorMessageDecoded = "";
        try {
            if (StringUtils.isNotBlank(requestData.getRequest().getParameter(RetryConstants.ERROR_MESSAGE)))
                errorMessageDecoded = URLDecoder.decode(
                        requestData.getRequest().getParameter(RetryConstants.ERROR_MESSAGE), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Exception occured while decoding query parameters", e);
        }

        retryPaymentInfo.setErrorMessage(errorMessageDecoded);

        final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());
        LOGGER.debug("Found data in session, txnInfo :{}", txnInfo);
        txnInfo.setRetry(true);

    }

    private void setCardNumber(final RetryPaymentInfo retryPaymentInfo, String cardNumber) {
        retryPaymentInfo.setCardNumberWithoutFormatting(cardNumber);
        retryPaymentInfo.setRetryCardNumber(formatCardNumber(cardNumber));
    }

    private String formatCardNumber(String cardNumber) {
        // Fetching data from retry request & setting this into session
        StringBuilder formattedCardNumber = new StringBuilder();

        // Formatting card Number
        int digitsCount = 0;
        for (int i = 0; i < cardNumber.length(); i++) {
            formattedCardNumber.append(cardNumber.charAt(i));
            digitsCount++;
            if (digitsCount < cardNumber.length() && digitsCount % 4 == 0) {
                formattedCardNumber.append(" ");
            }
        }
        return formattedCardNumber.toString();
    }

}
