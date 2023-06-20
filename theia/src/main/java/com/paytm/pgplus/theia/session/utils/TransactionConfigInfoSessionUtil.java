/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.WALLET_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import com.paytm.pgplus.theia.utils.BeanParamValidator;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.MapperUtils;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;

/**
 * @author amit.dubey
 *
 */
@Component("transactionConfigInfoSessionUtil")
public class TransactionConfigInfoSessionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConfigInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    /**
     * @param requestData
     * @param responseData
     */
    public void setTransactionConfigIntoSessionForPostLogin(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {

        final TransactionConfig transConfig = theiaSessionDataService.getTxnConfigFromSession(requestData.getRequest(),
                true);
        setCodHybridAllowed(requestData, responseData, transConfig);
        setPaymentCharges(responseData, transConfig);
        setPgPlusWalletDecisions(responseData, transConfig, requestData);
        setWalletOnly(responseData, transConfig);

        LOGGER.debug("Transaction config for post login set to session is :: {}", transConfig);
    }

    /**
     * @param requestData
     * @param responseData
     */
    public void setTransactionConfigIntoSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {
        final TransactionConfig transConfig = theiaSessionDataService.getTxnConfigFromSession(requestData.getRequest(),
                true);
        setCodHybridAllowed(requestData, responseData, transConfig);
        setEnvInfo(requestData, transConfig);
        setTxnType(requestData, transConfig);
        setIsSubscription(requestData, transConfig);
        setPgPlusWalletDecisions(responseData, transConfig, requestData);
        setPaymentCharges(responseData, transConfig);
        setWalletOnly(responseData, transConfig);
        setPaymentModeFilters(requestData, transConfig);
        setProductCode(requestData, transConfig);
        transConfig.setCommMode(requestData.getChannelId());
        setSubsType(requestData, transConfig);
        setUILogFlags(transConfig);
        setMinAmountForAddMoneyVoucher(transConfig);
    }

    private void setMinAmountForAddMoneyVoucher(TransactionConfig transConfig) {
        try {
            com.paytm.pgplus.cache.model.PaytmProperty paytmProperty = configurationDataService
                    .getPaytmProperty(ExtraConstants.MIN_AMOUNT_FOR_ADD_MONEY_VOUCHER);
            if (paytmProperty != null && StringUtils.isNotBlank(paytmProperty.getValue())) {
                transConfig.setMinAmountForAddMoneyVoucher(paytmProperty.getValue());
            } else {
                transConfig.setMinAmountForAddMoneyVoucher("50");
            }
            return;
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Min Amount For Add Money Voucher ", e);
        }
        transConfig.setMinAmountForAddMoneyVoucher("50");
    }

    private void setUILogFlags(TransactionConfig transConfig) {
        transConfig.setUiEventLogToConsole(Boolean.parseBoolean(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.UI_LOG_EVENT_TO_CONSOLE_PROPERTY, "false")));
        transConfig.setUiEventLogToServer(Boolean.parseBoolean(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.UI_LOG_EVENT_TO_SERVER_PROPERTY, "false")));
    }

    private void setSubsType(final PaymentRequestBean requestData, final TransactionConfig transConfig) {
        if (requestData.getRequestType().equals(RequestTypes.SUBSCRIPTION)) {
            if ("PPI".equals(requestData.getSubsPaymentMode()) || StringUtils.isBlank(requestData.getSubsPaymentMode())) {
                if ("Y".equals(requestData.getSubsPPIOnly())) {
                    transConfig.setSubsTypes(SubsTypes.PPI_ONLY);
                } else {
                    transConfig.setSubsTypes(SubsTypes.NORMAL);
                }
            } else if ("CC".equals(requestData.getSubsPaymentMode())) {
                transConfig.setSubsTypes(SubsTypes.CC_ONLY);
            } else if ("DC".equals(requestData.getSubsPaymentMode())) {
                transConfig.setSubsTypes(SubsTypes.DC_ONLY);
            } else if ("PPBL".equals(requestData.getSubsPaymentMode())) {
                transConfig.setSubsTypes(SubsTypes.PPBL_ONLY);
            }
        }
    }

    private void setProductCode(PaymentRequestBean requestData, TransactionConfig transConfig) {
        if (merchantPreferenceService.isSlabBasedMDREnabled(requestData.getMid())) {
            transConfig.setProductCode(ProductCodes.StandardAcquiringProdByAmountChargePayer.getId());
        } else if (merchantPreferenceService.isPostConvenienceFeesEnabled(requestData.getMid())) {
            transConfig.setProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer.getId());
        } else {
            ERequestType requestType = ERequestType.valueOf(requestData.getRequestType());
            try {
                transConfig.setProductCode(ProductCodes.getProductByProductCode(requestType.getProductCode()).getId());
            } catch (FacadeInvalidParameterException e) {
                LOGGER.error("Could not set product code because : ", e);
            }
        }
    }

    private void setPaymentModeFilters(final PaymentRequestBean requestData, final TransactionConfig transConfig) {
        transConfig.setPaymentModeOnly(requestData.getPaymentModeOnly());
        if (StringUtils.isNotBlank(requestData.getPaymentTypeId()) && "YES".equals(transConfig.getPaymentModeOnly())) {
            String[] payModeEnable = requestData.getPaymentTypeId().split(",");
            transConfig.setAllowedPayModes(MapperUtils.mapOldPayModesToNew(payModeEnable));
        }

        if (StringUtils.isNotBlank(requestData.getDisabledPaymentMode())) {
            String[] payModeDisable = requestData.getDisabledPaymentMode().split(",");
            transConfig.setDisallowedPayModes(MapperUtils.mapOldPayModesToNew(payModeDisable));
        }

        if (StringUtils.isBlank(requestData.getPaymentTypeId())
                || !requestData.getPaymentTypeId().contains(PayMethod.MP_COD.getOldName().toString())) {
            List<String> disabledOptions = transConfig.getDisallowedPayModes();
            if (disabledOptions.isEmpty()) {
                disabledOptions = new ArrayList<>();
            }
            if (!ERequestType.NATIVE_PAY.getType().equals(requestData.getRequestType())
                    && !ERequestType.NATIVE.getType().equals(requestData.getRequestType())) {
                disabledOptions.add(PayMethod.MP_COD.toString());
            }
            transConfig.setDisallowedPayModes(disabledOptions);
        }
    }

    private void setIsSubscription(final PaymentRequestBean requestData, final TransactionConfig transConfig) {
        if (requestData.getRequestType().equals(TheiaConstant.RequestTypes.SUBSCRIPTION)) {
            transConfig.setSubscriptionTxn(true);
        }
    }

    private void setPgPlusWalletDecisions(final WorkFlowResponseBean responseData, final TransactionConfig transConfig,
            PaymentRequestBean requestBean) {
        if (EPayMode.ADDANDPAY.equals(responseData.getAllowedPayMode())) {
            transConfig.setAddAndPayAllowed(true);
            transConfig.setAddMoneyFlag(true);
        } else if (EPayMode.HYBRID.equals(responseData.getAllowedPayMode())) {
            EntityPaymentOptionsTO entityPaymentOptionsTO = theiaSessionDataService.getEntityPaymentOptions(requestBean
                    .getRequest());

            if (entityPaymentOptionsTO.isAtmEnabled() || entityPaymentOptionsTO.isCcEnabled()
                    || entityPaymentOptionsTO.isEmiEnabled() || entityPaymentOptionsTO.isDcEnabled()
                    || entityPaymentOptionsTO.isNetBankingEnabled() || transConfig.isCodHybridAllowed()) {
                transConfig.setHybridAllowed(true);
            }
        }

        transConfig.setOnTheFlyKYCRequired(responseData.isOnTheFlyKYCRequired());

    }

    private void setTxnType(final PaymentRequestBean requestData, final TransactionConfig transConfig) {

        if (requestData.getRequestType().equals(TheiaConstant.RequestTypes.ADD_MONEY)) {
            transConfig.setTxnType(ETransType.TOP_UP.getType());
        } else {
            transConfig.setTxnType(ETransType.ACQUIRING.getType());
        }

        /*
         * For UI to Open PRN validation screen, for dynamicQR IRCTC
         */
        boolean isQRWith2FAEnabled = merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(requestData.getMid());
        transConfig.setDynamicQR2FA(isQRWith2FAEnabled);
    }

    private void setEnvInfo(final PaymentRequestBean requestData, final TransactionConfig transConfig) {
        transConfig.setEnvInfo(EnvInfoUtil.fetchEnvInfo(requestData.getRequest()));
    }

    private void setWalletOnly(final WorkFlowResponseBean responseData, final TransactionConfig transConfig) {
        /*
         * This is setting is walletOnly , if only BALANCE is in ConsultPayView
         * payMethod
         */
        if (BeanParamValidator.validateInputObjectParam(responseData.getMerchnatViewResponse())
                && BeanParamValidator
                        .validateInputListParam(responseData.getMerchnatViewResponse().getPayMethodViews())
                && (responseData.getMerchnatViewResponse().getPayMethodViews().size() == 1)) {

            if (responseData.getMerchnatViewResponse().getPayMethodViews().get(0).getPayMethod().equals(WALLET_TYPE)) {
                transConfig.setWalletOnly(true);
            }
        }
    }

    private void setPaymentCharges(final WorkFlowResponseBean responseData, final TransactionConfig transConfig) {
        if (BeanParamValidator.validateInputObjectParam(responseData.getConsultFeeResponse())
                && BeanParamValidator.validateInputMapParam(responseData.getConsultFeeResponse().getConsultDetails())) {
            Map<String, ConsultDetails> paymentCharges = new HashMap<>();
            Map<String, String> paymentChargesInfo = new HashMap<>();
            for (Entry<EPayMethod, ConsultDetails> consultDetails : responseData.getConsultFeeResponse()
                    .getConsultDetails().entrySet()) {
                paymentCharges.put(consultDetails.getKey().getOldName(), consultDetails.getValue());
                StringBuilder feeInfo = new StringBuilder();
                feeInfo.append("For ").append(consultDetails.getKey().getDisplayName()).append(" ")
                        .append(consultDetails.getValue().getText());
                paymentChargesInfo.put(consultDetails.getKey().getOldName(), feeInfo.toString());
            }
            transConfig.setPaymentCharges(paymentCharges);
            transConfig.setPaymentChargesInfo(paymentChargesInfo);
        }
    }

    private void setCodHybridAllowed(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData,
            final TransactionConfig transConfig) {
        EntityPaymentOptionsTO entityPaymentOptionSessionUtil = theiaSessionDataService
                .getEntityPaymentOptions(requestData.getRequest());
        WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(requestData.getRequest());
        TransactionInfo transInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest());
        if ((transInfo != null) && EPayMode.HYBRID.equals(responseData.getAllowedPayMode())
                && entityPaymentOptionSessionUtil.isCodEnabled()) {
            String txnAmount = transInfo.getTxnAmount();
            if (NumberUtils.isNumber(txnAmount)) {
                boolean supportCodHybrid = true;
                PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(ExtraConstants.COD_MIN_AMOUNT);
                if ((paytmProperty != null) && NumberUtils.isNumber(paytmProperty.getValue())) {
                    String value = paytmProperty.getValue();
                    double minCodAmount = Double.parseDouble(value);
                    if (minCodAmount > (Double.parseDouble(txnAmount) - walletInfo.getWalletBalance())) {
                        LOGGER.info("CoD Hybrid Not Supported because of Amount Checks");
                        supportCodHybrid = false;
                    }
                }
                transConfig.setCodHybridAllowed(supportCodHybrid);
            }
        }
    }
}
