/**
 *
 */
package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.theia.constants.TheiaConstant.SessionDataAttributes;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Set;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
public interface ITheiaSessionDataService extends Serializable {
    ThemeInfo setAndGetThemeInfoInSesion(HttpServletRequest request);

    ThemeInfo getThemeInfoFromSession(HttpServletRequest request);

    ThemeInfo getThemeInfoFromSession(HttpServletRequest request, boolean createNew);

    LoginInfo getLoginInfoFromSession(HttpServletRequest request);

    LoginInfo getLoginInfoFromSession(HttpServletRequest request, boolean createNew);

    TransactionInfo getTxnInfoFromSession(HttpServletRequest request);

    TransactionInfo getTxnInfoFromSession(HttpServletRequest request, boolean createNew);

    TransactionConfig getTxnConfigFromSession(HttpServletRequest request);

    TransactionConfig getTxnConfigFromSession(HttpServletRequest request, boolean createNew);

    MerchantInfo getMerchantInfoFromSession(HttpServletRequest request);

    MerchantInfo getMerchantInfoFromSession(HttpServletRequest request, boolean createNew);

    WalletInfo getWalletInfoFromSession(HttpServletRequest request);

    WalletInfo getWalletInfoFromSession(HttpServletRequest request, boolean createNew);

    CardInfo getCardInfoFromSession(HttpServletRequest request);

    CardInfo getCardInfoFromSession(HttpServletRequest request, boolean createNew);

    EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request);

    EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request, boolean createNew);

    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session, boolean createNew);

    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session);

    ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request, boolean createNew);

    void setRedirectPageInSession(HttpServletRequest request, String redirectPage);

    void setRedirectPageInSession(HttpServletRequest request, String redirectPage, boolean createNew);

    void setUPIAccepted(HttpServletRequest request, boolean status);

    boolean isUPIAccepted(HttpServletRequest request);

    boolean isSessionExists(HttpServletRequest request);

    boolean validateSession(HttpServletRequest request);

    ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request);

    void setExtendedInfoRequestBean(HttpServletRequest request, ExtendedInfoRequestBean extendedInfoRequestBean);

    void setEnvInfoRequestBean(HttpServletRequest request, EnvInfoRequestBean envInfoRequestBean);

    EnvInfoRequestBean getEnvInfoRequestBean(HttpServletRequest request);

    RetryPaymentInfo getRetryPaymentInfoFromSession(HttpServletRequest request, boolean createNew);

    boolean validateSession(HttpServletRequest request, boolean isRetry);

    void removeAttributeFromSession(HttpServletRequest request, SessionDataAttributes sessionDataAttributes);

    void setUPITransactionInfoInSession(HttpServletRequest request, UPITransactionInfo upiTransactionInfo);

    UPITransactionInfo getUPUpiTransactionInfoFromSession(HttpServletRequest request);

    MessageInfo getMessageInfoFromSession(HttpServletRequest request, boolean createNew);

    DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew);

    SavingsAccountInfo getSavingsAccountInfoFromSession(HttpServletRequest request, boolean createNew);

    SarvatraVPAMapInfo getSarvatraVPAInfoFromSession(HttpServletRequest request, boolean createNew);

    boolean isDirectChannelEnabled(String bankCode, String cardType, Set<String> directServiceInsts,
            boolean appendInstsAndCardType, Set<String> supportAtmPins);

    String getChannel(HttpServletRequest request, boolean ignoreRequestChannelId);

    void updateBeneficiaryDetailsInMerchantInfo(HttpServletRequest request,
            UltimateBeneficiaryDetails ultimateBeneficiaryDetails);
}
