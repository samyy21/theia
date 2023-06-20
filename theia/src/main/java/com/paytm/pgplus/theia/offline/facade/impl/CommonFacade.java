package com.paytm.pgplus.theia.offline.facade.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.user.service.IPaymentsBankService;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.payview.BalanceInfo;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

/**
 * Created by rahulverma on 18/9/17.
 */
@Service("commonFacade")
public class CommonFacade implements ICommonFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonFacade.class);

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    @Autowired
    @Qualifier("paymentsBankServiceImpl")
    private IPaymentsBankService paymentsBankService;

    @Override
    // @Cacheable(cacheManager = "springRedisCM", cacheNames = "offline", key =
    // "\"getLogoName\"+#bankId+\"_\"+#eChannelId.value")
    public String getLogoName(String bankId, EChannelId eChannelId) {
        if (StringUtils.isEmpty(bankId) || eChannelId == null)

            return null;
        long time = System.currentTimeMillis();
        String bankLogo = null;
        String logoPath = null;
        BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(bankId);
        if (null != bankInfoData) {
            if (EChannelId.WEB.equals(eChannelId)) {
                bankLogo = bankInfoData.getBankWebLogo();
                logoPath = ConfigurationUtil.getProperty("web.logo.path.bank", DEFAULT_WEB_LOGO_PATH_BANK);
            } else if ((EChannelId.WAP.equals(eChannelId)) || (EChannelId.APP.equals(eChannelId))) {
                bankLogo = bankInfoData.getBankWapLogo();
                logoPath = ConfigurationUtil.getProperty("wap.logo.path.bank", DEFAULT_WAP_LOGO_PATH_BANK);
            }
            LOGGER.debug("IconUrl api took {} milliseconds", System.currentTimeMillis() - time);
            if (bankLogo != null) {
                StringBuilder logoUrl = new StringBuilder();
                logoUrl.append(PaytmTLD.getStaticRandomUrlPrefix());
                logoUrl.append(logoPath).append(bankLogo).toString();
                return logoUrl.toString();
            }
        }
        return null;
    }

    @Override
    // @Cacheable(cacheManager = "springRedisCM", cacheNames = "offline", key =
    // "\"hasLowSuccessRate\"+#bankName+\"_\"+#payMethod")
    public boolean hasLowSuccessRate(String bankName, String payMethod) {
        long time = System.currentTimeMillis();
        boolean result = false;
        if (StringUtils.isEmpty(bankName) || StringUtils.isEmpty(payMethod)
                || com.paytm.pgplus.common.enums.PayMethod.BALANCE.getMethod().equals(payMethod))
            return result;
        result = successRateUtils.checkIfLowSuccessRate(bankName,
                com.paytm.pgplus.common.enums.PayMethod.getPayMethodByMethod(payMethod));
        LOGGER.debug("HasLowSuccess api took {} milliseconds", System.currentTimeMillis() - time);
        return result;
    }

    public BalanceInfo getPaytmBankBalanceInfo(UserDetailsBiz userDetailsBiz) {
        // Disabling account query
        return null;

        /*
         * if (userDetailsBiz == null || !
         * userDetailsBiz.isSavingsAccountRegistered()) { return new
         * BalanceInfo(); } GenericCoreResponseBean<AccountBalanceResponse>
         * fetchSavingsAccountBalanceResponse = paymentsBankService
         * .fetchSavingsAccountBalance(userDetailsBiz); if
         * (!fetchSavingsAccountBalanceResponse.isSuccessfullyProcessed() ||
         * fetchSavingsAccountBalanceResponse.getResponse() == null) {
         * LOGGER.error("Exception in fetchSavingsAccountBalance for user : {}",
         * (userDetailsBiz != null) ? userDetailsBiz.getUserId() :
         * "UserId null");
         * LOGGER.error(fetchSavingsAccountBalanceResponse.getFailureMessage());
         * return null; }
         * 
         * AccountBalanceResponse checkAccountBalanceResponse =
         * fetchSavingsAccountBalanceResponse.getResponse();
         * 
         * return new
         * BalanceInfo(checkAccountBalanceResponse.getAccountNumber(), new
         * Money( checkAccountBalanceResponse.getEffectiveBalance()), false);
         */
    }

    @Override
    public String getBankLogo(String bankCode) {

        if (StringUtils.isEmpty(bankCode))
            return null;
        String logo = bankCode.toUpperCase() + ".png";
        StringBuilder logoUrl = new StringBuilder();
        logoUrl.append(PaytmTLD.getStaticUrlPrefix());
        String logoPath = ConfigurationUtil.getProperty("native.logo.path.bank", DEFAULT_NATIVE_LOGO_PATH_BANK);
        logoUrl.append(logoPath).append(logo);
        return logoUrl.toString();
    }

    @Override
    public String getLogoUrl(String cardScheme, EChannelId channelId) {
        if (StringUtils.isEmpty(cardScheme) || channelId == null)
            return null;
        String logo = cardScheme.toLowerCase() + ".png";
        StringBuilder logoUrl = new StringBuilder();
        logoUrl.append(PaytmTLD.getStaticRandomUrlPrefix());
        String webLogoPath = ConfigurationUtil.getProperty("web.logo.path", DEFAULT_WEB_LOGO_PATH);
        String wapLogoPath = ConfigurationUtil.getProperty("wap.logo.path", DEFAULT_WAP_LOGO_PATH);
        if (EChannelId.WEB.equals(channelId)) {
            logoUrl.append(webLogoPath).append(logo);
        } else if ((EChannelId.WAP.equals(channelId)) || (EChannelId.APP.equals(channelId))) {
            logoUrl.append(wapLogoPath).append(logo);
        }
        return logoUrl.toString();
    }

    @Override
    public boolean hasLowSuccessRate(String bankName, String payMethod, SuccessRateCacheModel successRateCacheModel) {
        long time = System.currentTimeMillis();
        boolean result = false;
        if (StringUtils.isEmpty(bankName) || StringUtils.isEmpty(payMethod)
                || com.paytm.pgplus.common.enums.PayMethod.BALANCE.getMethod().equals(payMethod)
                || null == successRateCacheModel)
            return result;
        result = successRateUtils.checkIfLowSuccessRate(bankName,
                com.paytm.pgplus.common.enums.PayMethod.getPayMethodByMethod(payMethod), successRateCacheModel);
        LOGGER.debug("HasLowSuccess api took {} milliseconds", System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public List<BankInfoData> getBankInfoDataListFromBankCodes(List<String> bankCodeList) {
        List<BankInfoData> bankInfoList = merchantBankInfoDataService.getBankInfoDataListFromBankCodes(bankCodeList);
        return bankInfoList == null ? Collections.emptyList() : bankInfoList;
    }

    @Override
    public String getLogoName(BankInfoData bankInfoData, EChannelId eChannelId) {
        if (null == bankInfoData || eChannelId == null)
            return null;
        long time = System.currentTimeMillis();
        String bankLogo = null;
        String logoPath = null;
        if (null != bankInfoData) {
            if (EChannelId.WEB.equals(eChannelId)) {
                bankLogo = bankInfoData.getBankWebLogo();
                logoPath = ConfigurationUtil.getProperty("web.logo.path.bank", DEFAULT_WEB_LOGO_PATH_BANK);
            } else if ((EChannelId.WAP.equals(eChannelId)) || (EChannelId.APP.equals(eChannelId))) {
                bankLogo = bankInfoData.getBankWapLogo();
                logoPath = ConfigurationUtil.getProperty("wap.logo.path.bank", DEFAULT_WAP_LOGO_PATH_BANK);
            }
            LOGGER.debug("IconUrl api took {} milliseconds", System.currentTimeMillis() - time);
            if (bankLogo != null) {
                StringBuilder logoUrl = new StringBuilder();
                logoUrl.append(PaytmTLD.getStaticRandomUrlPrefix());
                logoUrl.append(logoPath).append(bankLogo).toString();
                return logoUrl.toString();
            }
        }
        return null;
    }

    @Override
    public String getLogoNameV1(String bankId) {
        if (StringUtils.isEmpty(bankId))
            return null;

        long time = System.currentTimeMillis();
        String logoPath = ConfigurationUtil.getProperty("native.logo.path.bank", DEFAULT_NATIVE_LOGO_PATH_BANK);
        String logoSufix = ConfigurationUtil.getProperty("native.logo.bank.sufix", DEFAULT_NATIVE_LOGO_SUFFIX);

        LOGGER.debug("IconUrl api took {} milliseconds", System.currentTimeMillis() - time);
        StringBuilder logoUrl = new StringBuilder();
        logoUrl.append(PaytmTLD.getStaticUrlPrefix());
        logoUrl.append(logoPath).append(bankId.toUpperCase()).append(logoSufix).toString();
        return logoUrl.toString();
    }

    @Override
    public String getLogoName(String imageName) {
        if (StringUtils.isEmpty(imageName))
            return null;

        long time = System.currentTimeMillis();
        String logoPath = ConfigurationUtil.getProperty("native.logo.path.failure", DEFAULT_NATIVE_LOGO_PATH_FAILURE);

        LOGGER.debug("IconUrl api took {} milliseconds", System.currentTimeMillis() - time);
        StringBuilder logoUrl = new StringBuilder();
        logoUrl.append(PaytmTLD.getStaticUrlPrefix());
        logoUrl.append(logoPath).append(imageName).toString();
        return logoUrl.toString();
    }

    @Override
    public String getLogoNameV2(String bankId) {
        if (StringUtils.isEmpty(bankId))
            return null;

        long time = System.currentTimeMillis();

        String logoSufix = ConfigurationUtil.getProperty("native.logo.bank.sufix", DEFAULT_NATIVE_LOGO_SUFFIX);

        LOGGER.debug("IconUrl api took {} milliseconds", System.currentTimeMillis() - time);
        StringBuilder logoUrl = new StringBuilder();
        // logoUrl.append(PaytmTLD.getStaticUrlPrefixV2());
        logoUrl.append(bankId.toUpperCase()).append(logoSufix).toString();
        return logoUrl.toString();
    }

    @Override
    public String getBaseIconUrl() {
        String logoPath = ConfigurationUtil.getProperty("native.logo.path.bank", DEFAULT_NATIVE_LOGO_PATH_BANK);
        StringBuilder baseIcon = new StringBuilder();
        return baseIcon.append(PaytmTLD.getStaticUrlPrefix()).append(logoPath).toString();
    }

}
