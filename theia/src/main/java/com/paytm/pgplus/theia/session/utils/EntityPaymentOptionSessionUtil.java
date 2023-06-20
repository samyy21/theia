/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.core.model.request.EMIChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.models.EmiPayOption;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.services.ISuccessRateQueryService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.EMIInfo;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.MessageInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import com.paytm.pgplus.theia.utils.BeanParamValidator;
import com.paytm.pgplus.theia.utils.helper.EMIFilterUtils;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import javax.servlet.http.HttpServletRequest;

/**
 * @author amit.dubey
 *
 */
@Component("entityPaymentOptionSessionUtil")
public class EntityPaymentOptionSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPaymentOptionSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    @Qualifier("successRateQueryServiceImpl")
    private ISuccessRateQueryService successRateQueryService;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("emiFilterUtils")
    EMIFilterUtils emiFilterUtils;

    public void setEntityPaymentOption(final PaymentRequestBean paymentRequestBean,
            final WorkFlowResponseBean responseData) {
        if ((BeanParamValidator.validateInputObjectParam(responseData.getMerchnatViewResponse()) && BeanParamValidator
                .validateInputListParam(responseData.getMerchnatViewResponse().getPayMethodViews()))
                || (BeanParamValidator.validateInputObjectParam(responseData.getMerchnatLiteViewResponse()) && BeanParamValidator
                        .validateInputListParam(responseData.getMerchnatLiteViewResponse().getPayMethodViews()))
                || ERequestType.RESELLER.name().equals(paymentRequestBean.getRequestType())) {
            boolean isAddAndPay = false;
            EntityPaymentOptionsTO entityPaymentoptions = theiaSessionDataService.getEntityPaymentOptions(
                    paymentRequestBean.getRequest(), true);

            setSuccessRateMessage(paymentRequestBean);

            List<PayMethodViewsBiz> payMethodsBiz = (responseData.getMerchnatViewResponse() != null) ? responseData
                    .getMerchnatViewResponse().getPayMethodViews()
                    : (responseData.getMerchnatLiteViewResponse() != null) ? responseData.getMerchnatLiteViewResponse()
                            .getPayMethodViews() : Collections.emptyList();
            boolean isHybrid = false;
            String txnAmount = paymentRequestBean.getTxnAmount();
            Double walletBalance = 0.0;
            if (EPayMode.HYBRID.equals(responseData.getAllowedPayMode())) {
                isHybrid = true;
                WalletInfo walletInfo = theiaSessionDataService.getWalletInfoFromSession(
                        paymentRequestBean.getRequest(), false);
                if (walletInfo != null) {
                    walletBalance = walletInfo.getWalletBalance();
                }
            }
            if (StringUtils.isBlank(paymentRequestBean.getEmiOption())) {
                TransactionInfo txnInfo = theiaSessionDataService
                        .getTxnInfoFromSession(paymentRequestBean.getRequest());
                if (txnInfo != null) {
                    paymentRequestBean.setEmiOption(txnInfo.getEmiOption());
                    paymentRequestBean.setAddress1(txnInfo.getAddress1());
                    paymentRequestBean.setPincode(txnInfo.getPincode());
                }
            }
            setPaymentOptions(payMethodsBiz, entityPaymentoptions, isAddAndPay, isHybrid, txnAmount, walletBalance,
                    paymentRequestBean);
            if ((entityPaymentoptions.isAddUpiPushEnabled() || entityPaymentoptions.isUpiPushEnabled())
                    && (CollectionUtils.isEmpty(responseData.getSarvatraVpa()) || !isValidVpaExist(responseData))) {
                entityPaymentoptions.setAddUpiPushEnabled(false);
                entityPaymentoptions.setUpiPushEnabled(false);
            }

            if (responseData.getMerchnatViewResponse().isPaymentsBankSupported()
                    && responseData.getAccountBalanceResponse() != null) {
                entityPaymentoptions.setPaymentsBankEnabled(responseData.getMerchnatViewResponse()
                        .isPaymentsBankSupported());
                if (ERequestType.RESELLER.name().equals(paymentRequestBean.getRequestType())) {
                    entityPaymentoptions.setReseller(true);
                }
            }

            if (BeanParamValidator.validateInputObjectParam(responseData.getAddAndPayViewResponse())
                    && BeanParamValidator.validateInputListParam(responseData.getAddAndPayViewResponse()
                            .getPayMethodViews())) {
                payMethodsBiz = responseData.getAddAndPayViewResponse().getPayMethodViews();
                isAddAndPay = true;
                setPaymentOptions(payMethodsBiz, entityPaymentoptions, isAddAndPay, isHybrid, txnAmount, walletBalance,
                        paymentRequestBean);
                if ((entityPaymentoptions.isAddUpiPushEnabled() || entityPaymentoptions.isUpiPushEnabled())
                        && (CollectionUtils.isEmpty(responseData.getSarvatraVpa()) || !isValidVpaExist(responseData))) {
                    entityPaymentoptions.setAddUpiPushEnabled(false);
                    entityPaymentoptions.setUpiPushEnabled(false);
                }

                if (responseData.getAddAndPayViewResponse().isPaymentsBankSupported()
                        && responseData.getAccountBalanceResponse() != null) {
                    entityPaymentoptions.setAddPaymentsBankEnabled(responseData.getAddAndPayViewResponse()
                            .isPaymentsBankSupported());
                }
            }
            LOGGER.info("Final Entity Payment Options are :: {}", entityPaymentoptions);
        }
    }

    private void setPaymentOptions(List<PayMethodViewsBiz> payMethodsBiz, EntityPaymentOptionsTO entityPaymentoptions,
            boolean isAddAndPay, boolean isHybrid, String txnAmount, Double walletBalance,
            PaymentRequestBean requestBean) {

        SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

        for (PayMethodViewsBiz payMethod : payMethodsBiz) {
            if (BeanParamValidator.validateInputListParam(payMethod.getPayChannelOptionViews())) {
                checkForCreditCard(entityPaymentoptions, isAddAndPay, payMethod);
                checkForUPI(entityPaymentoptions, isAddAndPay, payMethod, successRateCacheModel);
                checkForDebitCard(entityPaymentoptions, isAddAndPay, payMethod);
                checkForNetBanking(entityPaymentoptions, isAddAndPay, payMethod, successRateCacheModel);
                checkForATM(entityPaymentoptions, isAddAndPay, payMethod, successRateCacheModel);
                checkForIMPS(entityPaymentoptions, isAddAndPay, payMethod, successRateCacheModel);
                checkForEMI(entityPaymentoptions, isAddAndPay, isHybrid, txnAmount, walletBalance, payMethod,
                        requestBean);
                checkForMPCOD(entityPaymentoptions, txnAmount, requestBean, payMethod);
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param txnAmount
     * @param requestBean
     * @param payMethod
     */
    private void checkForMPCOD(EntityPaymentOptionsTO entityPaymentoptions, String txnAmount,
            PaymentRequestBean requestBean, PayMethodViewsBiz payMethod) {
        if (PayMethod.MP_COD.getMethod().equals(payMethod.getPayMethod())) {
            final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(requestBean.getRequest(), true);
            if ((loginInfo != null) && (loginInfo.getUser() != null)
                    && StringUtils.isNotBlank(loginInfo.getUser().getPayerUserID())) {
                Double transAmount = Double.parseDouble(txnAmount);
                boolean supportCod = true;
                PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(ExtraConstants.COD_MIN_AMOUNT);
                if ((paytmProperty != null) && NumberUtils.isNumber(paytmProperty.getValue())) {
                    String value = paytmProperty.getValue();
                    double minCodAmount = Double.parseDouble(value);
                    if (minCodAmount > transAmount) {
                        LOGGER.info("CoD Not Supported because of Amount Checks");
                        supportCod = false;
                    }
                }
                entityPaymentoptions.setCodEnabled(supportCod);
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param isHybrid
     * @param txnAmount
     * @param walletBalance
     * @param payMethod
     */
    public void checkForEMI(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay, boolean isHybrid,
            String txnAmount, Double walletBalance, PayMethodViewsBiz payMethod, PaymentRequestBean requestBean) {
        if (PayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {

            List<BankInfo> eMIInfoList = new ArrayList<>();
            List<BankInfo> hybridEmiInfoList = new ArrayList<>();

            boolean isValidDetails = emiFilterUtils.filterFor0CostEMI(requestBean.getRequest(),
                    requestBean.getEmiOption(), payMethod);
            if (!isValidDetails
                    && StringUtils.equals(requestBean.getRequestType(), TheiaConstant.RequestTypes.NATIVE)
                    && StringUtils.startsWithIgnoreCase(requestBean.getEmiOption(),
                            TheiaConstant.ExtraConstants.ZERO_COST_EMI)) {
                LOGGER.info("For zeroCostEmi Invalid Payment details entered");
                throw RequestValidationException.getException();
            }

            EmiPayOption payOption = BizRequestResponseMapperImpl.emiPayOption(requestBean);
            if (!payOption.isDropAll()) {
                for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {

                    if ((payChannel.isEnableStatus() && !payOption.getDisallowedPayOption().contains(
                            payChannel.getPayOption()))
                            && (BeanParamValidator.validateInputListParam(payChannel.getEmiChannelInfos()) || BeanParamValidator
                                    .validateInputListParam(payChannel.getEmiHybridChannelInfos()))) {
                        BankInfo bankInfo = getBankInfo(payChannel, true);
                        BankInfo hybridBankInfo = getBankInfo(payChannel, true);
                        List<EMIInfo> emiInfos = new ArrayList<>();
                        List<EMIInfo> hybridEmiInfo = new ArrayList<>();

                        for (EMIChannelInfoBiz emiInfo : payChannel.getEmiChannelInfos()) {
                            EMIInfo emiInfoBean = new EMIInfo();

                            if (setEmiInfoBean(payOption, payChannel, emiInfo)) {
                                emiInfoBean.setInstId(payChannel.getInstId());
                                emiInfoBean.setCardAcquiringMode(emiInfo.getCardAcquiringMode());
                                emiInfoBean.setInterestRate(emiInfo.getInterestRate());
                                emiInfoBean.setMaxAmount(emiInfo.getMaxAmount());
                                emiInfoBean.setMinAmount(emiInfo.getMinAmount());
                                emiInfoBean.setOfMonths(emiInfo.getOfMonths());
                                emiInfoBean.setPlanId(bankInfo.getBankName() + "|" + emiInfo.getOfMonths());
                                emiInfoBean.setAggregatorPlanId(emiInfo.getPlanId());
                                emiInfoBean.setTenureId(emiInfo.getTenureId());

                                // if tenure id not null then it is aggregator
                                if (StringUtils.isNotBlank(emiInfoBean.getTenureId())) {
                                    emiInfoBean.setAggregator(true);
                                }
                                emiInfoBean.setEmiAmount(AmountUtils.getTransactionAmountInRupee(emiInfo
                                        .getPerInstallment()));
                                emiInfos.add(emiInfoBean);
                            }
                        }

                        if (!CollectionUtils.isEmpty(emiInfos)) {
                            bankInfo.setEmiInfo(emiInfos);
                            // checkBankForLowSuccessRate(successRateCacheModel,bankInfo,PayMethod.EMI);
                            eMIInfoList.add(bankInfo);
                        }

                        if (!CollectionUtils.isEmpty(payChannel.getEmiHybridChannelInfos())) {
                            for (EMIChannelInfoBiz emiHybridInfo : payChannel.getEmiHybridChannelInfos()) {
                                if (isHybrid && walletBalance < Double.parseDouble(txnAmount)) {

                                    EMIInfo hybridEmiInfoBean = new EMIInfo();

                                    hybridEmiInfoBean.setInstId(payChannel.getInstId());
                                    hybridEmiInfoBean.setCardAcquiringMode(emiHybridInfo.getCardAcquiringMode());
                                    hybridEmiInfoBean.setInterestRate(emiHybridInfo.getInterestRate());
                                    hybridEmiInfoBean.setMaxAmount(emiHybridInfo.getMaxAmount());
                                    hybridEmiInfoBean.setMinAmount(emiHybridInfo.getMinAmount());
                                    hybridEmiInfoBean.setOfMonths(emiHybridInfo.getOfMonths());
                                    hybridEmiInfoBean.setPlanId(bankInfo.getBankName() + "|"
                                            + emiHybridInfo.getOfMonths());
                                    hybridEmiInfoBean.setTenureId(emiHybridInfo.getTenureId());

                                    if (StringUtils.isNotBlank(emiHybridInfo.getTenureId())) {
                                        hybridEmiInfoBean.setAggregator(true);
                                    }

                                    hybridEmiInfoBean.setEmiAmount(AmountUtils
                                            .getTransactionAmountInRupee(emiHybridInfo.getPerInstallment()));
                                    hybridEmiInfo.add(hybridEmiInfoBean);
                                }
                            }
                        }

                        if (!CollectionUtils.isEmpty(hybridEmiInfo)) {
                            hybridBankInfo.setEmiInfo(hybridEmiInfo);
                            hybridEmiInfoList.add(hybridBankInfo);
                        }

                    } else {
                        LOGGER.debug("Disabled PayChannel in EMI due to  ::{}", payChannel.getDisableReason());
                    }
                }
            }

            if (!eMIInfoList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddEmiEnabled(true);
                    entityPaymentoptions.setAddCompleteEMIInfoList(eMIInfoList);
                } else {
                    entityPaymentoptions.setEmiEnabled(true);
                    entityPaymentoptions.setCompleteEMIInfoList(eMIInfoList);
                }
            }
            if (!hybridEmiInfoList.isEmpty()) {
                entityPaymentoptions.setEmiEnabled(true);
                entityPaymentoptions.setCompleteEMIInfoList(eMIInfoList);
                entityPaymentoptions.setHybridEMIInfoList(hybridEmiInfoList);
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForIMPS(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod, SuccessRateCacheModel successRateCacheModel) {
        if (PayMethod.IMPS.getMethod().equals(payMethod.getPayMethod())) {

            List<BankInfo> bankInfoIMPSList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()) {
                    BankInfo bankInfo = getBankInfo(payChannel, false);

                    setIFLowSuccessRate(bankInfo, PayMethod.IMPS, successRateCacheModel);

                    bankInfoIMPSList.add(bankInfo);
                } else {
                    LOGGER.debug("Disabled PayChannel in IMPS due to  ::{}", payChannel.getDisableReason());
                }
            }

            if (!bankInfoIMPSList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddImpsEnabled(true);
                    entityPaymentoptions.setAddCompleteIMPSList(bankInfoIMPSList);
                } else {
                    entityPaymentoptions.setImpsEnabled(true);
                    entityPaymentoptions.setCompleteIMPSList(bankInfoIMPSList);
                }
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForATM(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod, SuccessRateCacheModel successRateCacheModel) {
        if (PayMethod.ATM.getMethod().equals(payMethod.getPayMethod())) {

            List<BankInfo> bankInfoATMList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()) {
                    BankInfo bankInfo = getBankInfo(payChannel, true);

                    setIFLowSuccessRate(bankInfo, PayMethod.ATM, successRateCacheModel);

                    bankInfoATMList.add(bankInfo);
                } else {
                    LOGGER.debug("Disabled PayChannel in ATM due to  ::{}", payChannel.getDisableReason());
                }
            }

            if (!bankInfoATMList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddAtmEnabled(true);
                    entityPaymentoptions.setAddCompleteATMList(bankInfoATMList);
                } else {
                    entityPaymentoptions.setAtmEnabled(true);
                    entityPaymentoptions.setCompleteATMList(bankInfoATMList);
                }
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForNetBanking(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod, SuccessRateCacheModel successRateCacheModel) {
        if (PayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())) {

            List<BankInfo> bankInfoNBList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()
                        || (!payChannel.isEnableStatus() && (TheiaConstant.ExtraConstants.CHANNEL_NOT_AVAILABLE)
                                .equals(payChannel.getDisableReason()))) {
                    BankInfo bankInfo = getBankInfo(payChannel, true);

                    setIFLowSuccessRate(bankInfo, PayMethod.NET_BANKING, successRateCacheModel);

                    bankInfoNBList.add(bankInfo);
                } else {
                    LOGGER.debug("Disabled PayChannel-{} in NET BANKING due to  ::{}", payChannel.getPayOption(),
                            payChannel.getDisableReason());
                }

            }

            if (!bankInfoNBList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddNetBankingEnabled(true);
                    entityPaymentoptions.setAddCompleteNbList(bankInfoNBList);
                } else {
                    entityPaymentoptions.setNetBankingEnabled(true);
                    entityPaymentoptions.setCompleteNbList(bankInfoNBList);
                }
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForDebitCard(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod) {
        if (PayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {

            List<BankInfo> bankInfoDCList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()) {
                    BankInfo bankInfo = getBankInfo(payChannel, false);

                    bankInfoDCList.add(bankInfo);

                    if ((payChannel.getDirectServiceInsts() != null) && !payChannel.getDirectServiceInsts().isEmpty()) {
                        for (String channel : payChannel.getDirectServiceInsts()) {
                            entityPaymentoptions.getDirectServiceInsts().add(channel + "@DEBIT_CARD");
                        }
                    }

                    if ((payChannel.getSupportAtmPins() != null) && !payChannel.getSupportAtmPins().isEmpty()) {
                        entityPaymentoptions.getSupportAtmPins().addAll(payChannel.getSupportAtmPins());
                    }
                } else {
                    LOGGER.debug("Disabled PayChannel in DEBIT CARD due to : {}", payChannel.getDisableReason());
                }
            }

            if (!bankInfoDCList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddDcEnabled(true);
                    entityPaymentoptions.setAddCompleteDcList(bankInfoDCList);
                } else {
                    entityPaymentoptions.setDcEnabled(true);
                    entityPaymentoptions.setCompleteDcList(bankInfoDCList);
                }
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForUPI(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod, SuccessRateCacheModel successRateCacheModel) {
        if (PayMethod.UPI.getMethod().equals(payMethod.getPayMethod())) {
            List<BankInfo> upiBankInfoList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()) {

                    BankInfo bankInfo = getBankInfo(payChannel, false);
                    setIFLowSuccessRate(bankInfo, PayMethod.UPI, successRateCacheModel);

                    upiBankInfoList.add(bankInfo);
                    setUPIPushAsPayMethod(entityPaymentoptions, isAddAndPay, payChannel.getPayOption());
                } else {
                    LOGGER.debug("Disabled PayChannel in UPI due to : {}", payChannel.getDisableReason());
                }
            }

            if (!upiBankInfoList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddUpiEnabled(true);
                    entityPaymentoptions.setAddCompleteUpiInfoList(upiBankInfoList);
                } else {
                    entityPaymentoptions.setUpiEnabled(true);
                    entityPaymentoptions.setCompleteUPIInfoList(upiBankInfoList);
                }
            }
        }
    }

    private void setUPIPushAsPayMethod(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            String payChannel) {
        // Set flag for UI to show paytm vpa
        if (TheiaConstant.BasicPayOption.UPI_PUSH.equals(payChannel)
                || TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel)) {
            if (isAddAndPay) {
                entityPaymentoptions.setAddUpiPushEnabled(true);
                if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel)) {
                    entityPaymentoptions.setAddUpiPushExpressEnabled(true);
                }
            } else {
                entityPaymentoptions.setUpiPushEnabled(true);
                if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel)) {
                    entityPaymentoptions.setUpiPushExpressEnabled(true);
                }
            }
        }
    }

    /**
     * @param entityPaymentoptions
     * @param isAddAndPay
     * @param payMethod
     */
    private void checkForCreditCard(EntityPaymentOptionsTO entityPaymentoptions, boolean isAddAndPay,
            PayMethodViewsBiz payMethod) {
        if (PayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
            List<BankInfo> bankInfoCCList = new ArrayList<>();

            for (PayChannelOptionViewBiz payChannel : payMethod.getPayChannelOptionViews()) {
                if (payChannel.isEnableStatus()) {
                    BankInfo bankInfo = getBankInfo(payChannel, false);

                    bankInfoCCList.add(bankInfo);

                    if ((payChannel.getDirectServiceInsts() != null) && !payChannel.getDirectServiceInsts().isEmpty()) {
                        for (String channel : payChannel.getDirectServiceInsts()) {
                            entityPaymentoptions.getDirectServiceInsts().add(channel + "@CREDIT_CARD");
                        }

                    }
                    if ((payChannel.getSupportAtmPins() != null) && !payChannel.getSupportAtmPins().isEmpty()) {

                        entityPaymentoptions.getSupportAtmPins().addAll(payChannel.getSupportAtmPins());
                    }
                } else {
                    LOGGER.debug("Disabled PayChannel in CREDIT CARD due to : {}", payChannel.getDisableReason());
                }
            }

            if (!bankInfoCCList.isEmpty()) {
                if (isAddAndPay) {
                    entityPaymentoptions.setAddCcEnabled(true);
                    entityPaymentoptions.setAddCompleteCcList(bankInfoCCList);
                } else {
                    entityPaymentoptions.setCcEnabled(true);
                    entityPaymentoptions.setCompleteCcList(bankInfoCCList);
                }
            }
        }
    }

    private void setIFLowSuccessRate(BankInfo bankInfo, final PayMethod payMethod,
            SuccessRateCacheModel successRateCacheModel) {
        if (successRateUtils.checkIfLowSuccessRate(bankInfo.getBankName(), payMethod, successRateCacheModel)) {
            bankInfo.setLowPercentage(true);
        }
    }

    private BankInfo getBankInfo(PayChannelOptionViewBiz payChannel, boolean bankLookupRequired) {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName(payChannel.getInstId());
        bankInfo.setMaintainence(!payChannel.isEnableStatus());
        bankInfo.setDisplayName(payChannel.getInstName());
        bankInfo.setSupportCountries(payChannel.getSupportCountries());

        if (StringUtils.isNotBlank(payChannel.getInstId()) && bankLookupRequired) {
            BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(payChannel.getInstId());
            if (bankInfoData != null) {
                bankInfo.setBankId(bankInfoData.getBankId());
                bankInfo.setBankWapLogo(bankInfoData.getBankWapLogo());
                bankInfo.setBankWebLogo(bankInfoData.getBankWebLogo());
                bankInfo.setIssuingBankName(bankInfoData.getIssuingBankName());
                bankInfo.setSequence(bankInfoData.getBankSequence());
            }
        } else {
            LOGGER.debug("Pay Channel received with blank inst id");
        }
        return bankInfo;
    }

    private void setSuccessRateMessage(PaymentRequestBean paymentRequestBean) {
        MessageInfo messageInfo = theiaSessionDataService.getMessageInfoFromSession(paymentRequestBean.getRequest(),
                true);
        setTLSWarningMessage(messageInfo);
        messageInfo.setLowPercentageMessage(ConfigurationUtil
                .getProperty(ExtraConstants.LOW_SUCCESS_RATE_DISPLAY_MESSAGE));
        messageInfo.setMaintenanceMessage(ConfigurationUtil
                .getProperty(ExtraConstants.CHANNEL_MAINTAINENCE_DISPLAY_MESSAGE));
    }

    private void setTLSWarningMessage(MessageInfo messageInfo) {
        messageInfo.setMerchantTLSWarnMsg(getAttributeStr(TheiaConstant.ResponseConstants.TLS_WARNING_MESSAGE));
        messageInfo
                .setAddAndPayTLSWarnMsg(getAttributeStr(TheiaConstant.ResponseConstants.ADDNPAY_TLS_WARNING_MESSAGE));
    }

    private String getAttributeStr(String attributeName) {
        return httpServletRequest().getAttribute(attributeName) != null ? (String) httpServletRequest().getAttribute(
                attributeName) : "";
    }

    private HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private boolean setEmiInfoBean(EmiPayOption payOption, PayChannelOptionViewBiz payChannel, EMIChannelInfoBiz emiInfo) {
        if (payOption.isShowAll()
                || payOption.getAllowedPayOption().contains(payChannel.getPayOption())
                || payOption.getAllowedPayOption().contains(payChannel.getPayOption() + "-" + emiInfo.getOfMonths())
                || (((!payOption.getBanks().isEmpty() && !payOption.getBanks().contains(payChannel.getPayOption())) || payOption
                        .getBanks().isEmpty())
                        && !payOption.getDisallowedPayOption().contains(
                                payChannel.getPayOption() + "-" + emiInfo.getOfMonths()) && !payOption
                        .getDisallowedPayOption().contains(payChannel.getPayOption()))) {
            return true;
        }
        return false;
    }

    private boolean isValidVpaExist(WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean.getSarvatraUserProfile() != null
                && workFlowResponseBean.getSarvatraUserProfile().getResponse() != null
                && CollectionUtils.isNotEmpty(workFlowResponseBean.getSarvatraUserProfile().getResponse()
                        .getVpaDetails())) {
            for (SarvatraVpaDetails sarvatraVpaDetails : workFlowResponseBean.getSarvatraUserProfile().getResponse()
                    .getVpaDetails()) {
                if (sarvatraVpaDetails.getDefaultDebit() != null) {
                    return true;
                }
            }
        }
        return false;
    }

}