/**
 *
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.DirectChannelBank;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.constants.TheiaConstant.SessionDataAttributes;
import com.paytm.pgplus.theia.enums.EnumChannel;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import com.paytm.pgplus.theia.services.AbstractSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.UserAgentInfo;
import com.paytm.pgplus.theia.utils.helper.MerchantThemeLoadBalancer;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @createdOn 29-Mar-2016
 * @author kesari
 */
@Service("theiaSessionDataService")
public class TheiaSessionDataServiceImpl extends AbstractSessionDataService implements ITheiaSessionDataService {
    private static final long serialVersionUID = 6506202840967640486L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaSessionDataServiceImpl.class);

    private static final String DEFAULT_WAP_CHANNEL = "WAP";
    private static final String DEFAULT_WEB_CHANNEL = "WEB";
    private static final String DEFAULT_POST_CON_THEME = "merchant3";

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private PreRedisCacheHelper preRedisCacheHelper;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private MerchantThemeLoadBalancer merchantThemeLoadBalancer;

    @Override
    public ThemeInfo getThemeInfoFromSession(HttpServletRequest request) {
        return getThemeInfoFromSession(request, false);
    }

    @Override
    public ThemeInfo getThemeInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String themeInfoVal = TheiaConstant.SessionDataAttributes.themeInfo.name();

        if (session == null) {
            throw new TheiaServiceException("ThemeInfo does not available as the session does not exist");
        }

        ThemeInfo themeInfo = session.getAttribute(themeInfoVal) == null ? null : ThemeInfo.class.cast(session
                .getAttribute(themeInfoVal));

        if ((themeInfo == null) && createNew) {
            themeInfo = new ThemeInfo();
            session.setAttribute(themeInfoVal, themeInfo);
        }

        return themeInfo;
    }

    @Override
    public LoginInfo getLoginInfoFromSession(HttpServletRequest request) {
        return getLoginInfoFromSession(request, false);
    }

    @Override
    public LoginInfo getLoginInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String loginInfoVal = TheiaConstant.SessionDataAttributes.loginInfo.name();

        if (session == null) {
            throw new TheiaServiceException("LoginInfo does not available due to the session does not exist");
        }

        LoginInfo loginInfo = session.getAttribute(loginInfoVal) == null ? null : LoginInfo.class.cast(session
                .getAttribute(loginInfoVal));
        if ((loginInfo == null) && createNew) {
            loginInfo = new LoginInfo();
            session.setAttribute(loginInfoVal, loginInfo);
        }

        if (loginInfo == null) {
            LOGGER.error("LoginInfo fetched from session is null");
        }
        return loginInfo;
    }

    @Loggable(logLevel = Loggable.DEBUG, state = TxnState.SESSION_FETCH_TXNINFO)
    @Override
    public TransactionInfo getTxnInfoFromSession(HttpServletRequest request) {
        return getTxnInfoFromSession(request, false);
    }

    @Override
    public TransactionInfo getTxnInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String txnInfoVal = TheiaConstant.SessionDataAttributes.txnInfo.name();

        if (session == null) {
            throw new TheiaServiceException("TxnInfo does not available due to the session not exist");
        }

        TransactionInfo txnInfo = session.getAttribute(txnInfoVal) == null ? null : TransactionInfo.class.cast(session
                .getAttribute(txnInfoVal));
        if ((txnInfo == null) && createNew) {
            txnInfo = new TransactionInfo(request);
            session.setAttribute(txnInfoVal, txnInfo);
        }

        if (txnInfo == null) {
            LOGGER.error("txnInfo fetched from session is null");
        }

        return txnInfo;
    }

    @Loggable(logLevel = Loggable.DEBUG, state = TxnState.SESSION_FETCH_TXNCONFIG)
    @Override
    public TransactionConfig getTxnConfigFromSession(HttpServletRequest request) {
        return getTxnConfigFromSession(request, false);
    }

    @Override
    public TransactionConfig getTxnConfigFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String txnConfigVal = TheiaConstant.SessionDataAttributes.txnConfig.name();

        if (session == null) {
            throw new TheiaServiceException("TransactionConfig does not available due to the session not exist");
        }

        TransactionConfig txnConfig = session.getAttribute(txnConfigVal) == null ? null : TransactionConfig.class
                .cast(session.getAttribute(txnConfigVal));
        if ((txnConfig == null) && createNew) {
            txnConfig = new TransactionConfig();
            session.setAttribute(txnConfigVal, txnConfig);
        }

        if (txnConfig == null) {
            LOGGER.error("TxnConfig fetched from session is null");
        }
        return txnConfig;
    }

    @Loggable(logLevel = Loggable.DEBUG, state = TxnState.SESSION_FETCH_WALLET_INFO)
    @Override
    public WalletInfo getWalletInfoFromSession(HttpServletRequest request) {
        return getWalletInfoFromSession(request, false);
    }

    @Override
    public WalletInfo getWalletInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String walletInfoVal = TheiaConstant.SessionDataAttributes.walletInfo.name();

        if (session == null) {
            throw new TheiaServiceException("WalletInfo does not available due to the session not exist");
        }

        WalletInfo walletInfo = session.getAttribute(walletInfoVal) == null ? null : WalletInfo.class.cast(session
                .getAttribute(walletInfoVal));
        if ((walletInfo == null) && createNew) {
            walletInfo = new WalletInfo();
            session.setAttribute(walletInfoVal, walletInfo);
        }
        if (walletInfo == null) {
            LOGGER.error("WalletInfo fetched from session is null");
        }
        return walletInfo;
    }

    @Loggable(logLevel = Loggable.DEBUG, state = TxnState.SESSION_FETCH_MERCHANT_INFO)
    @Override
    public MerchantInfo getMerchantInfoFromSession(HttpServletRequest request) {
        return getMerchantInfoFromSession(request, false);
    }

    @Override
    public MerchantInfo getMerchantInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String merchantInfoVal = TheiaConstant.SessionDataAttributes.merchInfo.name();

        if (session == null) {
            throw new TheiaServiceException("MerchantInfo does not available due to the session not exist");
        }

        MerchantInfo merchInfo = session.getAttribute(merchantInfoVal) == null ? null : MerchantInfo.class.cast(session
                .getAttribute(merchantInfoVal));
        if ((merchInfo == null) && createNew) {
            merchInfo = new MerchantInfo();
            session.setAttribute(merchantInfoVal, merchInfo);
        }
        if (merchInfo == null) {
            LOGGER.error("MerchantInfo fetched from session is null");
        }
        return merchInfo;
    }

    @Override
    public void updateBeneficiaryDetailsInMerchantInfo(HttpServletRequest request,
            UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        if (ObjectUtils.notEqual(ultimateBeneficiaryDetails, null)
                && StringUtils.isNotBlank(ultimateBeneficiaryDetails.getUltimateBeneficiaryName())) {
            HttpSession session = request.getSession();
            if (session != null) {
                String merchantInfoVal = TheiaConstant.SessionDataAttributes.merchInfo.name();
                MerchantInfo merchInfo = session.getAttribute(merchantInfoVal) == null ? null : MerchantInfo.class
                        .cast(session.getAttribute(merchantInfoVal));
                if (merchInfo != null) {
                    merchInfo.setMerchantName(ultimateBeneficiaryDetails.getUltimateBeneficiaryName());
                }
            }
        }
    }

    @Override
    public RetryPaymentInfo getRetryPaymentInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String retryPaymentInfoVal = TheiaConstant.SessionDataAttributes.retryPaymentInfo.name();

        if (session == null) {
            throw new TheiaServiceException("RetryPaymentInfo does not available due to the session not exist");
        }

        RetryPaymentInfo retryPaymentInfo = session.getAttribute(retryPaymentInfoVal) == null ? null
                : RetryPaymentInfo.class.cast(session.getAttribute(retryPaymentInfoVal));

        if ((retryPaymentInfo == null) && createNew) {
            retryPaymentInfo = new RetryPaymentInfo();
            session.setAttribute(retryPaymentInfoVal, retryPaymentInfo);
        }
        if (retryPaymentInfo == null) {
            LOGGER.error("RetryPaymentInfo fetched from session is null");
        }
        return retryPaymentInfo;
    }

    @Loggable(logLevel = Loggable.DEBUG, state = TxnState.SESSION_FETCH_CARD_INFO)
    @Override
    public CardInfo getCardInfoFromSession(HttpServletRequest request) {
        return getCardInfoFromSession(request, false);
    }

    @Override
    public CardInfo getCardInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String cardInfoVal = TheiaConstant.SessionDataAttributes.cardInfo.name();

        if (session == null) {
            throw new TheiaServiceException("CardInfo does not available due to the session not exist");
        }

        CardInfo cardInfo = session.getAttribute(cardInfoVal) == null ? null : CardInfo.class.cast(session
                .getAttribute(cardInfoVal));
        if ((cardInfo == null) && createNew) {
            cardInfo = new CardInfo();
            session.setAttribute(cardInfoVal, cardInfo);
        }
        if (cardInfo == null) {
            LOGGER.error("CardInfo fetched from session is null");
        }
        return cardInfo;
    }

    @Override
    public ThemeInfo setAndGetThemeInfoInSesion(HttpServletRequest request) {
        HttpSession session = request.getSession();

        if (session == null) {
            throw new TheiaServiceException("ThemeInfo for Set/Get does not available due to the session not exist");
        }

        Object themeInSession = session.getAttribute(SessionDataAttributes.themeInfo.name());
        if (null != themeInSession) {
            return (ThemeInfo) themeInSession;
        }

        ThemeInfo themeInfo = new ThemeInfo();

        // Set channel
        setChannel(request, themeInfo);

        String mid = getAttributeOrParameter(request, RequestParams.MID);

        if (ConfigurationUtil.getProperty(TheiaConstant.PaytmPropertyConstants.MERCHANT_THEME_LB_FLAG).equals("true")
                && isThemeLBEnabledForMerchant(mid)) {
            prepareAndSetTheme(merchantThemeLoadBalancer.getTheme(getChannel(request, false), mid), themeInfo);
        } else {
            // Set Theme and Sub theme
            prepareAndSetTheme(getAttributeOrParameter(request, RequestParams.THEME), themeInfo);

            // This method call is required to override the request theme with a
            // configured value for the merchant
            overrideThemeIfRequired(mid, themeInfo);

            updateWithCustomTheme(themeInfo);
        }

        session.setAttribute(SessionDataAttributes.themeInfo.name(), themeInfo);

        LOGGER.debug("Final ThemeInfo : {}", themeInfo);
        return themeInfo;
    }

    private boolean isThemeLBEnabledForMerchant(String mid) {
        String[] merchantIds = ConfigurationUtil.getProperty(
                TheiaConstant.PaytmPropertyConstants.MERCHANTS_ELIGIBLE_FOR_THEME_LB).split(",");
        for (int i = 0; i < merchantIds.length; i++) {
            if (mid.equals(merchantIds[i]))
                return true;
        }
        return false;
    }

    public String getChannel(HttpServletRequest request, boolean ignoreRequestChannelId) {
        String channel = getAttributeOrParameter(request, RequestParams.CHANNEL_ID);
        UserAgentInfo userAgentInfo = new UserAgentInfo(request);

        if (ignoreRequestChannelId) {
            channel = null;
        }

        if (userAgentInfo.detectSmartphone() || StringUtils.equals(ETerminalType.WAP.getTerminal(), channel)) {
            channel = DEFAULT_WAP_CHANNEL;
        }

        if (StringUtils.isNotBlank(channel)) {
            return channel;
        } else {
            ETerminalType terminalType = EnvInfoUtil.getTerminalType(request);
            switch (terminalType) {
            case APP:
            case WAP:
                channel = DEFAULT_WAP_CHANNEL;
                break;
            case SYSTEM:
            case WEB:
            default:
                channel = DEFAULT_WEB_CHANNEL;
                break;
            }
        }
        return channel;
    }

    private void overrideThemeIfRequired(String mid, ThemeInfo themeInfo) {
        String configuredTheme = getConfiguredTheme(mid, themeInfo);
        if (StringUtils.isNotBlank(configuredTheme)) {
            setTheme(themeInfo, configuredTheme);
        }
    }

    private String getConfiguredTheme(String mid, ThemeInfo themeInfo) {
        try {
            EChannelId channel = EChannelId.valueOf(themeInfo.getChannel().toUpperCase());
            MerchantExtendedInfoResponse extendInfo = preRedisCacheHelper.getMerchantExtendedData(mid);
            if ((channel != null) && (extendInfo != null) && (extendInfo.getExtendedInfo() != null)) {
                switch (channel) {
                case SYSTEM:
                    break;
                case WAP:

                    if (StringUtils.isBlank(extendInfo.getExtendedInfo().getMerchantWapForcedTheme())) {
                        return TheiaConstant.ThemeConstants.DEFAULT_WAP_THEME;
                    }

                    return extendInfo.getExtendedInfo().getMerchantWapForcedTheme();
                case WEB:

                    if (StringUtils.isBlank(extendInfo.getExtendedInfo().getMerchantWebForcedTheme())) {
                        return TheiaConstant.ThemeConstants.DEFAULT_WEB_THEME;
                    }

                    return extendInfo.getExtendedInfo().getMerchantWebForcedTheme();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while building the theme", e);
        }
        return TheiaConstant.ExtraConstants.EMPTY_STRING;
    }

    private String getAttributeOrParameter(HttpServletRequest request, String paramName) {
        String paramValue = request.getParameter(paramName);
        return StringUtils.isNotBlank(paramValue) ? paramValue : request.getAttribute(paramName) != null ? request
                .getAttribute(paramName).toString() : null;
    }

    private void setChannel(HttpServletRequest request, ThemeInfo themeInfo) {
        String channel = getAttributeOrParameter(request, RequestParams.CHANNEL_ID);
        UserAgentInfo userAgentInfo = new UserAgentInfo(request);

        if (userAgentInfo.detectSmartphone() || DEFAULT_WAP_CHANNEL.equals(channel)) {
            channel = DEFAULT_WAP_CHANNEL;
        } else {
            channel = DEFAULT_WEB_CHANNEL;
        }

        if (StringUtils.isNotBlank(channel)) {
            themeInfo.setChannel(channel);
        } else {
            ETerminalType terminalType = EnvInfoUtil.getTerminalType(request);
            switch (terminalType) {
            case APP:
            case WAP:
                themeInfo.setChannel(DEFAULT_WAP_CHANNEL);
                break;
            case SYSTEM:
            case WEB:
            default:
                themeInfo.setChannel(DEFAULT_WEB_CHANNEL);
                break;
            }
        }
    }

    private String getPaytmProperty(String propertyName) {
        PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(propertyName);
        if (paytmProperty != null) {
            return paytmProperty.getValue();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request) {
        return getEntityPaymentOptions(request, false);
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        return getEntityPaymentOptions(session, createNew);

    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session) {
        return getEntityPaymentOptions(session, false);
    }

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOptions(HttpSession session, boolean createNew) {

        String entityPaymentOptionsVal = TheiaConstant.SessionDataAttributes.entityInfo.name();

        if (session == null) {
            throw new TheiaServiceException("EntityPaymentOptionsTO does not available due to the session not exist");
        }

        EntityPaymentOptionsTO entityPaymentOptionInfo = session.getAttribute(entityPaymentOptionsVal) == null ? null
                : EntityPaymentOptionsTO.class.cast(session.getAttribute(entityPaymentOptionsVal));

        if ((entityPaymentOptionInfo == null) && createNew) {
            entityPaymentOptionInfo = new EntityPaymentOptionsTO();
            session.setAttribute(entityPaymentOptionsVal, entityPaymentOptionInfo);
        }

        if (entityPaymentOptionInfo == null) {
            LOGGER.error("EntityPaymentInfo fetched from session is null");
        }
        return entityPaymentOptionInfo;
    }

    /**
     * @param requestTheme
     * @param viewInfo
     */
    private void prepareAndSetTheme(String requestTheme, ThemeInfo viewInfo) {
        String themeForView = StringUtils.isBlank(requestTheme) ? "merchant" : requestTheme;

        switch (themeForView) {
        case "paytmHigh":
            themeForView = "merchantLow";
            break;
        case "paytmLow":
            themeForView = "merchantLow|paytmLow";
            break;
        case "paytmLow|ssl":
            themeForView = "merchantLow|paytmLowSSL";
            break;
        case "paytmAppLow":
            themeForView = "merchantLow|paytmApp";
            break;
        case "paytmAppLow|ssl":
            themeForView = "merchantLow|paytmAppSSL";
            break;
        case "merchant|tastykhana":
            themeForView = "merchant2|tastykhana";
            break;
        case "airtelLow":
            themeForView = "merchantLow|airtel";
            break;
        case "merchant3":
            themeForView = "merchant3";
            break;
        }

        setTheme(viewInfo, themeForView);
    }

    private void setTheme(ThemeInfo viewInfo, String themeForView) {
        if (StringUtils.contains(themeForView, "|")) {
            String[] themes = themeForView.split("\\|");
            viewInfo.setTheme(themes[0]);
            viewInfo.setSubTheme(themes[1]);
        } else {
            viewInfo.setTheme(themeForView);
        }
    }

    /**
     * @param viewInfo
     */
    private void updateWithCustomTheme(ThemeInfo viewInfo) {

        switch (viewInfo.getTheme()) {
        case "airtelLow":
            viewInfo.setTheme("paytmLow");
            break;
        case "merchant":
        case "merchant2":
        case "merchant3":
        case "merchant5":
            if (StringUtils.equals(viewInfo.getChannel(), EnumChannel.WAP.name())) {
                viewInfo.setChannel(EnumChannel.WEB.name());
            }
            break;
        case "javas":
            viewInfo.setTheme("merchantLow");
            break;
        }
    }

    @Override
    public boolean isUPIAccepted(HttpServletRequest request) {
        Object data = request.getSession(false).getAttribute(TheiaConstant.ResponseConstants.UPI_ACCEPTED);
        if (data == null) {
            return false;
        }
        return (boolean) data;
    }

    @Override
    public void setUPIAccepted(HttpServletRequest request, boolean status) {
        request.getSession(false).setAttribute(TheiaConstant.ResponseConstants.UPI_ACCEPTED, status);
    }

    @Override
    public ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request) {
        return geExtendedInfoRequestBean(request, false);
    }

    @Override
    public ExtendedInfoRequestBean geExtendedInfoRequestBean(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        String extendedInfoVal = TheiaConstant.SessionDataAttributes.extendedInfo.name();

        if (session == null) {
            throw new TheiaServiceException("ExtendedInfoRequestBean does not available due to the session not exist");
        }

        ExtendedInfoRequestBean extendedInfo = session.getAttribute(extendedInfoVal) == null ? null
                : ExtendedInfoRequestBean.class.cast(session.getAttribute(extendedInfoVal));
        if ((extendedInfo == null) && createNew) {
            extendedInfo = new ExtendedInfoRequestBean();
            session.setAttribute(extendedInfoVal, extendedInfo);
        }

        if (extendedInfo == null) {
            LOGGER.error("ExtendedInfo fetched from session is null");
        }
        return extendedInfo;
    }

    @Override
    public void setExtendedInfoRequestBean(HttpServletRequest request, ExtendedInfoRequestBean extendedInfoRequestBean) {
        HttpSession session = request.getSession();

        if (session == null) {
            throw new TheiaServiceException("Unable to set ExtendedInfoRequestBean due to the Session does not exist");
        }

        if (null == session.getAttribute(TheiaConstant.SessionDataAttributes.extendedInfo.name())) {
            session.setAttribute(TheiaConstant.SessionDataAttributes.extendedInfo.name(), extendedInfoRequestBean);
        }
    }

    @Override
    public void setEnvInfoRequestBean(HttpServletRequest request, EnvInfoRequestBean envInfo) {
        HttpSession session = request.getSession();

        if (session == null) {
            throw new TheiaServiceException("Unable to set EnvInfoRequestBean due to the Session does not exist");
        }

        if (null == session.getAttribute(TheiaConstant.SessionDataAttributes.envInfo.name())) {
            session.setAttribute(TheiaConstant.SessionDataAttributes.envInfo.name(), envInfo);
        }
    }

    @Override
    public EnvInfoRequestBean getEnvInfoRequestBean(HttpServletRequest request) {
        return isSessionExists(request) ? (EnvInfoRequestBean) request.getSession(false).getAttribute(
                TheiaConstant.SessionDataAttributes.envInfo.name()) : null;
    }

    @Override
    public UPITransactionInfo getUPUpiTransactionInfoFromSession(HttpServletRequest request) {
        return isSessionExists(request) ? (UPITransactionInfo) request.getSession(false).getAttribute(
                SessionDataAttributes.upiTransactionInfo.name()) : null;
    }

    @Override
    public void setUPITransactionInfoInSession(HttpServletRequest request, UPITransactionInfo upiTransactionInfo) {
        HttpSession session = request.getSession();

        if (session == null) {
            throw new TheiaServiceException("Unable to set UPITransactionInfo due to the Session does not exist");
        }

        session.setAttribute(SessionDataAttributes.upiTransactionInfo.name(), upiTransactionInfo);
    }

    @Override
    public MessageInfo getMessageInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession();
        if (session == null) {
            throw new TheiaServiceException("Session does not exist");
        }
        MessageInfo messageInfo = session.getAttribute(TheiaConstant.SessionDataAttributes.messageInfo.name()) == null ? null
                : (MessageInfo) session.getAttribute(TheiaConstant.SessionDataAttributes.messageInfo.name());

        if ((messageInfo == null) && createNew) {
            messageInfo = new MessageInfo();
            session.setAttribute(TheiaConstant.SessionDataAttributes.messageInfo.name(), messageInfo);
        }

        if (messageInfo == null) {
            LOGGER.error("MessageInfo fetched from session is null");
        }
        return messageInfo;
    }

    @Override
    public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new TheiaServiceException("DigitalCreditInfo is not available due to session not exist");
        }
        DigitalCreditInfo digitalCreditInfo = session
                .getAttribute(TheiaConstant.SessionDataAttributes.digitalCreditInfo.name()) == null ? null
                : (DigitalCreditInfo) session
                        .getAttribute(TheiaConstant.SessionDataAttributes.digitalCreditInfo.name());

        if ((digitalCreditInfo == null) && createNew) {
            digitalCreditInfo = new DigitalCreditInfo();
            session.setAttribute(TheiaConstant.SessionDataAttributes.digitalCreditInfo.name(), digitalCreditInfo);
        }
        if (digitalCreditInfo == null) {
            LOGGER.error("DigitalCreditInfo fetched from session is null");
        }
        return digitalCreditInfo;
    }

    @Override
    public SavingsAccountInfo getSavingsAccountInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new TheiaServiceException("SavingsAccountInfo is not available due to session not exist");
        }
        SavingsAccountInfo savingsAccountInfo = session
                .getAttribute(TheiaConstant.SessionDataAttributes.savingsAccountInfo.name()) == null ? null
                : (SavingsAccountInfo) session.getAttribute(TheiaConstant.SessionDataAttributes.savingsAccountInfo
                        .name());

        if ((savingsAccountInfo == null) && createNew) {
            savingsAccountInfo = new SavingsAccountInfo();
            session.setAttribute(TheiaConstant.SessionDataAttributes.savingsAccountInfo.name(), savingsAccountInfo);
        }
        if (savingsAccountInfo == null) {
            LOGGER.error("SavingsAccountInfo fetched from session is null");
        }
        return savingsAccountInfo;

    }

    @Override
    public boolean isDirectChannelEnabled(String bankCode, String cardType, Set<String> directServiceInsts,
            boolean appendInstsAndCardType, Set<String> supportAtmPins) {
        try {
            if (appendInstsAndCardType) {
                directServiceInsts = directServiceInsts.stream().map(s -> s + "@" + cardType)
                        .collect(Collectors.toSet());
            }

            if (supportAtmPins == null) {
                LOGGER.info("SupportAtmPins field is null, returning false");
                return false;
            }
            for (DirectChannelBank directChannel : DirectChannelBank.values()) {
                if (bankCode.equals(directChannel.getBankCode())
                        && directServiceInsts.contains(directChannel + "@" + cardType)
                        && supportAtmPins.contains(directChannel.toString())) {
                    return true;
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error while fetching or parsing direct-channels list", e.getMessage());
        }
        return false;
    }

    @Override
    public SarvatraVPAMapInfo getSarvatraVPAInfoFromSession(HttpServletRequest request, boolean createNew) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new TheiaServiceException("sarvatraVpainfo is not available due to session not exist");
        }
        SarvatraVPAMapInfo sarvatraVpainfo = session.getAttribute(TheiaConstant.SessionDataAttributes.sarvatraVpainfo
                .name()) == null ? null : (SarvatraVPAMapInfo) session
                .getAttribute(TheiaConstant.SessionDataAttributes.sarvatraVpainfo.name());

        if ((sarvatraVpainfo == null) && createNew) {
            sarvatraVpainfo = new SarvatraVPAMapInfo();
            session.setAttribute(TheiaConstant.SessionDataAttributes.sarvatraVpainfo.name(), sarvatraVpainfo);
        }
        return sarvatraVpainfo;
    }

}