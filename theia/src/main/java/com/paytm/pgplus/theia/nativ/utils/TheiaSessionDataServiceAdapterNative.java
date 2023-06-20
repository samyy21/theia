package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Set;

@Service("theiaSessionDataServiceAdapterNative")
public class TheiaSessionDataServiceAdapterNative implements ITheiaSessionDataService {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4861579896524123509L;
    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Override
    public ThemeInfo setAndGetThemeInfoInSesion(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThemeInfo getThemeInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThemeInfo getThemeInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoginInfo getLoginInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoginInfo getLoginInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionInfo getTxnInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionInfo getTxnInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionConfig getTxnConfigFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionConfig getTxnConfigFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MerchantInfo getMerchantInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MerchantInfo getMerchantInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    private PayMethod getMerchantPayMethod(EPayMethod ePayMethod, String txnToken) {
        if (ePayMethod == null || StringUtils.isEmpty(txnToken)) {
            return null;
        }
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
        if (cashierInfoResponse != null && cashierInfoResponse.getBody() != null) {
            PayOption payMethodViews = cashierInfoResponse.getBody().getMerchantPayOption();
            if (payMethodViews != null && payMethodViews.getPayMethods() != null
                    && !payMethodViews.getPayMethods().isEmpty()) {
                for (PayMethod payMethod : payMethodViews.getPayMethods()) {
                    if (ePayMethod.getMethod().equals(payMethod.getPayMethod())) {
                        return payMethod;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public WalletInfo getWalletInfoFromSession(HttpServletRequest request) {
        String txnToken = getTxnToken(request);
        PayMethod payMethod = getMerchantPayMethod(EPayMethod.BALANCE, txnToken);
        if (payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
            AccountInfo balanceInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
            if (balanceInfo != null && balanceInfo.getAccountBalance() != null
                    && !StringUtils.isEmpty(balanceInfo.getAccountBalance().getValue())) {
                WalletInfo walletInfo = new WalletInfo();
                walletInfo.setWalletBalance(Double.parseDouble(balanceInfo.getAccountBalance().getValue()));
                return walletInfo;
            }
        }
        return null;
    }

    @Override
    public WalletInfo getWalletInfoFromSession(HttpServletRequest request, boolean createNew) {
        WalletInfo walletInfo = getWalletInfoFromSession(request);
        if (walletInfo == null && createNew) {
            walletInfo = new WalletInfo();
        }
        return walletInfo;
    }

    @Override
    public CardInfo getCardInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CardInfo getCardInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request) {
        String txnToken = getTxnToken(request);
        return nativeSessionUtil.getEntityPaymentOptions(txnToken);
    }

    private String getTxnToken(HttpServletRequest request) {
        return request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN);
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request, boolean createNew) {
        EntityPaymentOptionsTO entityPaymentOptionsTO = getEntityPaymentOptions(request);
        if (entityPaymentOptionsTO == null && createNew) {
            entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        }
        return entityPaymentOptionsTO;
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRedirectPageInSession(HttpServletRequest request, String redirectPage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRedirectPageInSession(HttpServletRequest request, String redirectPage, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUPIAccepted(HttpServletRequest request, boolean status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUPIAccepted(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSessionExists(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validateSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtendedInfoRequestBean(HttpServletRequest request, ExtendedInfoRequestBean extendedInfoRequestBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnvInfoRequestBean(HttpServletRequest request, EnvInfoRequestBean envInfoRequestBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnvInfoRequestBean getEnvInfoRequestBean(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RetryPaymentInfo getRetryPaymentInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validateSession(HttpServletRequest request, boolean isRetry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributeFromSession(HttpServletRequest request,
            TheiaConstant.SessionDataAttributes sessionDataAttributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUPITransactionInfoInSession(HttpServletRequest request, UPITransactionInfo upiTransactionInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UPITransactionInfo getUPUpiTransactionInfoFromSession(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageInfo getMessageInfoFromSession(HttpServletRequest request, boolean createNew) {
        return new MessageInfo();
    }

    @Override
    public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SavingsAccountInfo getSavingsAccountInfoFromSession(HttpServletRequest request, boolean createNew) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectChannelEnabled(String bankCode, String cardType, Set<String> directServiceInsts,
            boolean appendInstsAndCardType, Set<String> supportAtmPins) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getChannel(HttpServletRequest request, boolean ignoreRequestChannelId) {
        return null;
    }

    @Override
    public void updateBeneficiaryDetailsInMerchantInfo(HttpServletRequest request,
            UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        return;
    }

    @Override
    public SarvatraVPAMapInfo getSarvatraVPAInfoFromSession(HttpServletRequest request, boolean createNew) {
        // TODO Auto-generated method stub
        return null;
    }

}