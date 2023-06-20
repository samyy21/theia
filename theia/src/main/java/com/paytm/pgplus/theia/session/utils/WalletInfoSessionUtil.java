/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.SubWalletDetails;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.service.IWalletInfoService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import com.paytm.pgplus.theia.sessiondata.SubWalletInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import com.paytm.pgplus.theia.utils.BeanParamValidator;

/**
 * @author amit.dubey
 *
 */
@Component("walletInfoSessionUtil")
public class WalletInfoSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletInfoSessionUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(WalletInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("walletInfoServiceImpl")
    private IWalletInfoService walletInfoService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    public void setWalletInfoIntoSession(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        if (BeanParamValidator.validateInputObjectParam(responseData.getUserDetails())) {
            WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(requestData.getRequest(), true);
            final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(requestData.getRequest(), true);
            final OAuthUserInfo userInfo = loginInfo.getUser() != null ? loginInfo.getUser() : new OAuthUserInfo();

            if (BeanParamValidator.validateInputObjectParam(responseData.getMerchnatViewResponse())
                    || BeanParamValidator.validateInputObjectParam(responseData.getMerchnatLiteViewResponse())) {

                if (SubsTypes.CC_ONLY.equals(responseData.getSubsType())
                        || SubsTypes.DC_ONLY.equals(responseData.getSubsType())) {
                    walletInfo.setWalletFailed(true);
                }

                List<PayMethodViewsBiz> payMethods = (responseData.getMerchnatViewResponse() != null) ? responseData
                        .getMerchnatViewResponse().getPayMethodViews()
                        : (responseData.getMerchnatLiteViewResponse() != null) ? responseData
                                .getMerchnatLiteViewResponse().getPayMethodViews() : Collections.emptyList();
                for (PayMethodViewsBiz payMethod : payMethods) {
                    if (PayMethod.BALANCE.name().equals(payMethod.getPayMethod())) {
                        if (BeanParamValidator.validateInputListParam(payMethod.getPayChannelOptionViews())) {
                            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                                // Need to convert amount into Rupees
                                if (payChannel.isEnableStatus()) {
                                    setWalletDetails(responseData, walletInfo, userInfo, payChannel);
                                } else {
                                    setWalletFailureMessage(walletInfo, payChannel);
                                }
                            }

                        }
                    }
                }
                setLimitFailureMessages(requestData, responseData, walletInfo, payMethods);
            }
        } else if (responseData.getUserDetails() == null && responseData.getMerchnatViewResponse() != null
                && responseData.getMerchnatViewResponse().isWalletOnly()) {
            WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(requestData.getRequest(), true);
            walletInfo.setWalletFailed(false);
            walletInfo.setWalletOnly(true);
        }
    }

    private void setWalletDetails(final WorkFlowResponseBean responseData, WalletInfo walletInfo,
            final OAuthUserInfo userInfo, PayChannelOptionViewBiz payChannel) {
        Double walletBalanceRupees = Double.valueOf(AmountUtils.getTransactionAmountInRupee(payChannel
                .getBalanceChannelInfos().get(0).getAccountBalance()));
        if (responseData.getMerchnatViewResponse() != null) {
            walletInfo.setWalletOnly(responseData.getMerchnatViewResponse().isWalletOnly());
            walletInfo.setWalletFailed(responseData.getMerchnatViewResponse().isWalletFailed());
        }
        walletInfo.setWalletBalance(walletBalanceRupees);
        userInfo.setPayerAccountNumber(payChannel.getBalanceChannelInfos().get(0).getPayerAccountNo());
        walletInfo.setWalletEnabled(true);

        walletInfo.setWalletInactive(false);
        walletInfo.setDisplayName(EPayMethod.BALANCE.getNewDisplayName());

        if ((payChannel.getExtendInfo() != null) && !payChannel.getExtendInfo().isEmpty()) {
            String subWalletList = payChannel.getExtendInfo().get("subWalletDetailsList");
            if (StringUtils.isNotBlank(subWalletList)) {
                setSubWalletAmount(walletInfo, subWalletList);
            }
        }

        /*
         * We don't want to query for subwallets, as we don't show subwallet
         * details in any flow.
         */

        /*
         * if ((payChannel.getExtendInfo() != null) &&
         * !payChannel.getExtendInfo().isEmpty()) { String subWalletList =
         * payChannel.getExtendInfo().get("subWalletDetailsList"); if
         * (StringUtils.isNotBlank(subWalletList)) {
         * setSubWalletList(walletInfo, subWalletList); } }
         */

    }

    private void setSubWalletAmount(WalletInfo walletInfo, String subWalletList) {
        List<SubWalletInfo> finalSubWalletList = parseAndGetSubWalletList(subWalletList);
        for (SubWalletInfo subWalletInfo : finalSubWalletList) {
            if (Integer.parseInt(subWalletInfo.getSubWalletType()) == TheiaConstant.SubWalletType.PAYTM_BALANCE) {
                walletInfo.setPaytmWalletAmount(subWalletInfo.getSubWalletBalance().doubleValue());
            }
            if (Integer.parseInt(subWalletInfo.getSubWalletType()) == TheiaConstant.SubWalletType.GIFT_VOUCHER) {
                walletInfo.setGiftVoucherAmount(subWalletInfo.getSubWalletBalance().doubleValue());
            }
        }
    }

    private void setSubWalletList(WalletInfo walletInfo, String subWalletList) {
        List<SubWalletInfo> finalSubWalletList = parseAndGetSubWalletList(subWalletList);
        if (!finalSubWalletList.isEmpty()) {
            walletInfo.setAreSubWalletsEnabled(true);
            walletInfo.setSubWalletDetails(finalSubWalletList);
        }
    }

    private List<SubWalletInfo> parseAndGetSubWalletList(String subWalletList) {
        List<SubWalletInfo> finalSubWalletList = new ArrayList<>();
        try {
            List<Map<String, Object>> subWallets = JsonMapper.getListOfMapFromJson(subWalletList);
            for (Map<String, Object> subWallet : subWallets) {
                SubWalletInfo subWalletInfo = constructSubWalletInfo(subWallet);
                if (Integer.parseInt(subWalletInfo.getStatus()) == TheiaConstant.Status.ACTIVE) {
                    finalSubWalletList.add(subWalletInfo);
                }
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred while parsing subWalletList", e);
        }
        return finalSubWalletList;
    }

    private SubWalletInfo constructSubWalletInfo(Map<String, Object> subWallet) {
        SubWalletInfo subWalletInfo = new SubWalletInfo();
        subWalletInfo.setDisplayMessage(subWallet.containsKey("displayName") ? subWallet.get("displayName").toString()
                : null);
        subWalletInfo.setExpiryDate(subWallet.containsKey("expiry") ? subWallet.get("expiry").toString() : null);
        subWalletInfo.setId(subWallet.containsKey("id") ? subWallet.get("id").toString() : null);
        subWalletInfo.setIssuerMetadata(subWallet.containsKey("issuerMetadata") ? subWallet.get("issuerMetadata")
                .toString() : null);
        subWalletInfo.setSubWalletBalance(subWallet.containsKey("balance") ? new BigDecimal(AmountUtils
                .getTransactionAmountInRupee(subWallet.get("balance").toString())) : null);
        subWalletInfo.setSubWalletType(subWallet.containsKey("subWalletType") ? subWallet.get("subWalletType")
                .toString() : null);
        subWalletInfo
                .setWalletType(subWallet.containsKey("walletType") ? subWallet.get("walletType").toString() : null);
        subWalletInfo.setStatus(subWallet.containsKey("status") ? subWallet.get("status").toString() : null);

        try {
            SubWalletDetails subWalletDetails = walletInfoService.getSubWalletDetails(subWalletInfo.getSubWalletType());
            EXT_LOGGER.customInfo("Mapping response - SubWalletDetails :: {}", subWalletDetails);
            if (subWalletDetails != null) {
                subWalletInfo.setWapLogo(subWalletDetails.getSubWalletWapLogo());
                subWalletInfo.setWebLogo(subWalletDetails.getSubWalletWebLogo());
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching subwallet details : ", e);
        }

        return subWalletInfo;
    }

    private void setLimitFailureMessages(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData,
            WalletInfo walletInfo, List<PayMethodViewsBiz> payMethods) {
        if (walletInfo.isWalletEnabled() && EPayMode.NONE.equals(responseData.getAllowedPayMode())
                && merchantPreferenceService.isAddMoneyEnabled(requestData.getMid())) {
            Double txnAmount = Double.parseDouble(requestData.getTxnAmount());
            double difference = txnAmount - walletInfo.getWalletBalance();
            if (difference > 0) {
                walletInfo.setWalletFailed(true);
                if (difference >= 1) {
                    if (payMethods.size() > 1) {
                        walletInfo.setWalletFailedMsg(configurationDataService
                                .getPaytmPropertyValue("limitFail.rbi.walletPG.insufficient"));
                    } else {
                        walletInfo.setWalletFailedMsg(configurationDataService
                                .getPaytmPropertyValue("limitFail.rbi.walletOnly.insufficient"));
                    }
                } else {
                    if (payMethods.size() > 1) {
                        walletInfo
                                .setWalletFailedMsg("You have insufficient balance for this transaction. Please pay using other payment modes.");
                    } else {
                        walletInfo.setWalletFailedMsg("You have insufficient balance for this transaction.");
                    }
                }
            }
        }
    }

    private void setWalletFailureMessage(WalletInfo walletInfo, PayChannelOptionViewBiz payChannel) {
        String disableReason = payChannel.getDisableReason();
        String errorMessage = "";
        switch (disableReason) {
        case "ACCOUNT_BALANCE_QUERY_FAIL":
            walletInfo.setWalletFailed(true);
            walletInfo.setWalletInactive(false);
            errorMessage = configurationDataService.getPaytmPropertyValue("balance.not.available.walletPG");
            if (StringUtils.isBlank(errorMessage)) {
                errorMessage = "Your Paytm Cash Balance is not available right now, you may proceed to pay with other payment options or try again later.";
            }
            walletInfo.setWalletFailedMsg(errorMessage);
            break;
        default:
            walletInfo.setWalletFailed(false);
            walletInfo.setWalletInactive(true);
            errorMessage = "Your Paytm Cash is not activated. Activate your Paytm Cash account after this order for faster checkout in future";
            walletInfo.setWalletFailedMsg(errorMessage);
            break;
        }
    }
}
