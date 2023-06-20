package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.offline.enums.AuthMode;
import com.paytm.pgplus.theia.offline.enums.CountryCode;
import com.paytm.pgplus.theia.offline.enums.PaymentFlow;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.payview.*;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by rahulverma on 12/10/17.
 */
@Component("customBeanMapper")
public class CustomBeanMapper implements ICustomBeanMapper<CashierInfoResponse> {

    private static final String VPA = "VPA";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomBeanMapper.class);
    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    public void makePaytmPaymentBankAsSeparatePayMethod(CashierInfoResponse cashierInfoResponse,
            WorkFlowResponseBean workFlowResponseBean) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null
                || cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods() == null) {
            return;
        }

        boolean paymentsBankEnabled = false;
        PayMethod payMethodPPBL = new PayMethod();
        for (PayMethod payMethod : cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods()) {
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())) {
                for (Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator(); payChannelBaseIterator
                        .hasNext();) {
                    PayChannelBase nextPayChannelBase = payChannelBaseIterator.next();
                    if (EPayMethod.PPBL.getMethod().equals(nextPayChannelBase.getPayChannelOption())) {
                        paymentsBankEnabled = true;
                        if (payMethodPPBL != null) {
                            payMethodPPBL.setPayMethod(EPayMethod.PPBL.getMethod());
                            payMethodPPBL.setDisplayName(EPayMethod.PPBL.getDisplayName());
                            payMethodPPBL.setIsDisabled(nextPayChannelBase.getIsDisabled());
                            payMethodPPBL.setPayChannelOptions(new ArrayList<>());
                            payMethodPPBL.getPayChannelOptions().add(
                                    balanceChannel(nextPayChannelBase,
                                            getBalanceInfoForPPBL(workFlowResponseBean.getUserDetails())));
                        }

                        payChannelBaseIterator.remove();
                        break;
                    }

                }
            }
        }

        if (paymentsBankEnabled) {
            cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods().add(payMethodPPBL);
        }

    }

    @Override
    public CashierInfoResponse getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest) {
        return getCashierInfoResponse(workFlowResponseBean, cashierInfoRequest, false);
    }

    @Override
    public CashierInfoResponse getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest, boolean disableWallet) {
        LOGGER.info("Mapping WorkflowResponseBean to CasheirInfo Response ...");
        CashierInfoResponseBody cashierInfoResponseBody = new CashierInfoResponseBody();
        cashierInfoResponseBody.setEnabledFlows(getEnabledFlows(workFlowResponseBean.getAllowedPayMode()));
        cashierInfoResponseBody.setOrderId(cashierInfoRequest.getBody().getOrderId());
        PayMethodViews payMethodViews = new PayMethodViews();
        LitePayviewConsultResponseBizBean merchnatLiteViewResponse = workFlowResponseBean.getMerchnatLiteViewResponse();
        LitePayviewConsultResponseBizBean addAndPayLiteViewResponse = workFlowResponseBean
                .getAddAndPayLiteViewResponse();
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        EChannelId eChannelId = cashierInfoRequest.getBody().getChannelId();
        List<PayMethod> merchantPayMethods = null;
        List<PayMethod> addMoneyPayMethods = null;
        if (merchnatLiteViewResponse != null) {
            cashierInfoResponseBody.setPostConvenienceFee(new PostConvenienceFee(String
                    .valueOf(merchnatLiteViewResponse.isChargePayer())));
            merchantPayMethods = getPayMethods(cashierInfoRequest.getBody().getOrderAmount(),
                    merchnatLiteViewResponse.getPayMethodViews(), eChannelId);
            payMethodViews.setMerchantPayMethods(merchantPayMethods);
        }
        if (addAndPayLiteViewResponse != null) {
            addMoneyPayMethods = getPayMethods(cashierInfoRequest.getBody().getOrderAmount(),
                    addAndPayLiteViewResponse.getPayMethodViews(), eChannelId);
            payMethodViews.setAddMoneyPayMethods(getPayMethods(cashierInfoRequest.getBody().getOrderAmount(),
                    addAndPayLiteViewResponse.getPayMethodViews(), eChannelId));
        }
        if (userDetailsBiz != null) {
            payMethodViews.setMerchantSavedInstruments(getSavedInstruments(
                    userDetailsBiz.getMerchantViewSavedCardsList(), eChannelId, merchantPayMethods));
            payMethodViews.setAddMoneySavedInstruments(getSavedInstruments(
                    userDetailsBiz.getAddAndPayViewSavedCardsList(), eChannelId, addMoneyPayMethods));
        }
        cashierInfoResponseBody.setPayMethodViews(payMethodViews);

        // TODO:to Check extend info
        cashierInfoResponseBody.setExtendInfoString(workFlowResponseBean.getExtendedInfo());
        cashierInfoResponseBody.setResultInfo(OfflinePaymentUtils.resultInfoForSuccess());
        cashierInfoResponseBody.setSignature("");
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse();
        cashierInfoResponse.setBody(cashierInfoResponseBody);
        cashierInfoResponse.setHead(new ResponseHeader(cashierInfoRequest.getHead()));
        makePaytmPaymentBankAsSeparatePayMethod(cashierInfoResponse, workFlowResponseBean);
        LOGGER.info("Mapping WorkflowResponseBean to CasheirInfo Response done");
        LOGGER.debug("CashierInfoResponse {}", cashierInfoResponse);

        return cashierInfoResponse;
    }

    private SavedInstruments getSavedInstruments(List<CardBeanBiz> cardBeanBizs, EChannelId eChannelId,
            List<PayMethod> payMethods) {
        List<SavedCard> savedCards = new ArrayList<>();
        // TODO:Empty for now
        List<SavedVPA> savedVPAs = new ArrayList<>();
        SavedInstruments savedInstruments = new SavedInstruments(savedCards, savedVPAs);
        if (cardBeanBizs == null)
            return savedInstruments;

        for (CardBeanBiz cardBeanBiz : cardBeanBizs) {
            String payChannelOption = null;
            if (VPA.equals(cardBeanBiz.getCardScheme())) {
                payChannelOption = cardBeanBiz.getCardType();
            } else {
                payChannelOption = cardBeanBiz.getCardType() + "_" + cardBeanBiz.getCardScheme();
            }
            PayChannelBase payChannelBase = getPayChannelOptionFromPayMethods(payMethods, cardBeanBiz.getCardType(),
                    payChannelOption);
            if (payChannelBase instanceof BankCard) {
                SavedCard savedCard = getSavedCard(cardBeanBiz, payChannelOption, (BankCard) payChannelBase,
                        payMethods, eChannelId);
                savedCards.add(savedCard);
            } else if (payChannelBase instanceof UPI) {
                SavedVPA savedVPA = getSavedVPA(cardBeanBiz, payChannelOption, (UPI) payChannelBase, payMethods,
                        eChannelId);
                savedVPAs.add(savedVPA);
            }
        }
        return savedInstruments;
    }

    private SavedVPA getSavedVPA(CardBeanBiz cardBeanBiz, String payChannelOption, UPI upi, List<PayMethod> payMethods,
            EChannelId eChannelId) {
        VPADetails vpaDetails = new VPADetails();
        SavedVPA savedVPA = new SavedVPA();
        vpaDetails.setVpa(cardBeanBiz.getCardNumber());
        savedVPA.setPayChannelOption(payChannelOption);
        savedVPA.setPayMethod(cardBeanBiz.getCardType());
        savedVPA.setInstId(upi.getInstId());
        savedVPA.setInstName(upi.getInstName());
        savedVPA.setIsDisabled(upi.getIsDisabled());
        savedVPA.setIconUrl(commonFacade.getLogoUrl(upi.getInstId(), eChannelId));

        savedVPA.setBankLogoUrl(commonFacade.getBankLogo(cardBeanBiz.getInstId()));
        savedVPA.setHasLowSuccess(upi.getHasLowSuccess());
        savedVPA.setVpaDetails(vpaDetails);
        return savedVPA;
    }

    private SavedCard getSavedCard(CardBeanBiz cardBeanBiz, String payChannelOption, BankCard bankCard,
            List<PayMethod> payMethods, EChannelId eChannelId) {
        CardDetails cardDetails = new CardDetails();
        SavedCard savedCard = new SavedCard(cardDetails);
        cardDetails.setCardId(String.valueOf(cardBeanBiz.getCardId()));
        cardDetails.setCardType(cardBeanBiz.getCardType());
        savedCard.setPayChannelOption(payChannelOption);
        savedCard.setPayMethod(cardBeanBiz.getCardType());
        cardDetails.setCreated_on("");
        cardDetails.setUpdated_on("");

        cardDetails.setUserId(cardBeanBiz.getUserId());
        cardDetails.setStatus(String.valueOf(cardBeanBiz.getStatus()));
        savedCard.setIssuingBank(cardBeanBiz.getInstId());

        savedCard.setInstId(bankCard.getInstId());
        savedCard.setInstName(bankCard.getInstName());
        savedCard.setIsDisabled(bankCard.getIsDisabled());
        savedCard.setIconUrl(commonFacade.getLogoUrl(bankCard.getInstId(), eChannelId));

        savedCard.setBankLogoUrl(commonFacade.getBankLogo(cardBeanBiz.getInstId()));
        savedCard.setHasLowSuccess(bankCard.getHasLowSuccess());

        cardDetails.setFirstSixDigit(String.valueOf(cardBeanBiz.getFirstSixDigit()));
        cardDetails.setLastFourDigit(NativePaymentUtil.getLastFourDigits(cardBeanBiz.getLastFourDigit()));
        savedCard.setSupportedCountries(((BankCard) bankCard).getSupportedCountries());
        savedCard.setEmiDetails(getEmiChannel(savedCard, payMethods));
        boolean isDirectPayOption = getIsDirectPayOption(savedCard, payMethods);
        if (isDirectPayOption) {
            List<String> iDebitOptionsList = new ArrayList<String>();
            iDebitOptionsList.add(AuthMode.PIN.getType());
            iDebitOptionsList.add(AuthMode.OTP.getType());
            savedCard.setAuthModes(iDebitOptionsList);
        }
        return savedCard;
    }

    private boolean getIsDirectPayOption(SavedCard savedCard, List<PayMethod> payMethods) {
        PayMethod payMethod = payMethods.stream().filter(s -> s.getPayMethod().equals(savedCard.getPayMethod()))
                .findAny().orElse(null);
        if (payMethod != null) {

            BankCard bankCard = payMethod.getPayChannelOptions().stream().map(s -> (BankCard) s)
                    .filter(s -> s.getInstId().equals(savedCard.getInstId())).findAny().orElse(null);
            if (bankCard != null && !isEmpty(bankCard.getDirectServiceInsts())
                    && !isEmpty(bankCard.getSupportAtmPins())) {
                return theiaSessionDataService.isDirectChannelEnabled(savedCard.getIssuingBank(), savedCard
                        .getCardDetails().getCardType(), new HashSet<>(bankCard.getDirectServiceInsts()), true,
                        new HashSet<>(bankCard.getSupportAtmPins()));

            }
        }
        return false;
    }

    private EmiChannel getEmiChannel(SavedCard savedCard, List<PayMethod> payMethods) {
        PayMethod payMethod = payMethods.stream().filter(s -> s.getPayMethod().equals(EPayMethod.EMI.getMethod()))
                .findAny().orElse(null);
        if (payMethod != null) {
            EmiChannel emiChannel = payMethod.getPayChannelOptions().stream().map(s -> (EmiChannel) s)
                    .filter(s -> s.getInstId().equals(savedCard.getIssuingBank())).findAny().orElse(null);
            return emiChannel;
        }
        return null;
    }

    private PayChannelBase getPayChannelOptionFromPayMethods(List<PayMethod> payMethods, String payMethod,
            String payChannelOption) {
        if (payMethods == null || payMethods.isEmpty() || StringUtils.isEmpty(payMethod)
                || StringUtils.isEmpty(payChannelOption))
            return null;
        for (PayMethod payMethod1 : payMethods) {
            if (payMethod.equals(payMethod1.getPayMethod())) {
                if (payMethod1.getPayChannelOptions() == null || payMethod1.getPayChannelOptions().isEmpty()) {
                    break;
                }
                for (PayChannelBase payChannelBase : payMethod1.getPayChannelOptions()) {
                    if (payChannelOption.equals(payChannelBase.getPayChannelOption())) {
                        return payChannelBase;
                    }
                }
            }
        }
        return null;
    }

    private List<PaymentFlow> getEnabledFlows(EPayMode ePayMode) {
        List<PaymentFlow> flows = getDefaultPaymentFlows();
        flows.add(PaymentFlow.paymentFlowByEPayMode(ePayMode));
        return flows;
    }

    // TODO:This is disabled as per discussion
    // now payment flow can have only hybrid or addandpay
    private List<PaymentFlow> getDefaultPaymentFlows() {
        List<PaymentFlow> paymentFlows = new ArrayList<>();
        // Disabling for now
        /*
         * paymentFlows.add(PaymentFlow.PG_ONLY);
         * paymentFlows.add(PaymentFlow.PPI);
         */
        return paymentFlows;
    }

    private List<PayMethod> getPayMethods(Money orderAmount, List<PayMethodViewsBiz> payMethodViewsBizs,
            EChannelId eChannelId) {
        List<PayMethod> payMethods = new ArrayList<>();
        if (payMethodViewsBizs == null)
            return payMethods;

        SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {
            PayMethod payMethod = new PayMethod();
            payMethod.setPayChannelOptions(getPayChannelOptions(orderAmount,
                    payMethodViewsBiz.getPayChannelOptionViews(), payMethodViewsBiz.getPayMethod(), eChannelId,
                    successRateCacheModel));

            EPayMethod payMethodEnum = EPayMethod.getPayMethodByMethod(payMethodViewsBiz.getPayMethod());
            if (EPayMethod.MP_COD.equals(payMethodEnum)) {
                payMethod.setPayMethod(payMethodEnum.getOldName());
            } else {
                payMethod.setPayMethod(payMethodEnum.getMethod());
            }
            payMethod.setDisplayName(payMethodEnum.getDisplayName());
            payMethod.setIsDisabled(new StatusInfo(Boolean.FALSE.toString(), ""));
            payMethods.add(payMethod);

        }
        return payMethods;
    }

    private List<PayChannelBase> getPayChannelOptions(Money orderAmount,
            List<PayChannelOptionViewBiz> payChannelOptionViewBizs, String payMethod, EChannelId eChannelId,
            SuccessRateCacheModel successRateCacheModel) {
        List<PayChannelBase> payChannelOptions = new ArrayList<>();
        if (payChannelOptionViewBizs == null)
            return payChannelOptions;
        for (PayChannelOptionViewBiz payChannelOptionViewBiz : payChannelOptionViewBizs) {
            PayChannelBase payChannelBase = null;
            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)) {
                CreditCard creditCard = new CreditCard();
                creditCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                creditCard.setInstId(payChannelOptionViewBiz.getInstId());
                creditCard.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = creditCard;
            }
            if (EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)) {
                DebitCard debitCard = new DebitCard();
                debitCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                debitCard.setInstId(payChannelOptionViewBiz.getInstId());
                debitCard.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = debitCard;
            }
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod)) {
                NetBanking netBanking = new NetBanking();
                netBanking.setInstId(payChannelOptionViewBiz.getInstId());
                netBanking.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = netBanking;
            }
            if (EPayMethod.UPI.getMethod().equals(payMethod)) {
                UPI upi = new UPI();
                upi.setInstId(payChannelOptionViewBiz.getInstId());
                upi.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = upi;
            }
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethod)) {
                DigitalCredit digitalCredit = new DigitalCredit();
                digitalCredit.setBalanceInfo(getBalanceInfoFromExternalAccount(payChannelOptionViewBiz
                        .getExternalAccountInfos()));
                payChannelBase = digitalCredit;
            }
            if (EPayMethod.BALANCE.getMethod().equals(payMethod)) {
                Wallet wallet = new Wallet();
                wallet.setBalanceInfo(getBalanceInfo(payChannelOptionViewBiz.getBalanceChannelInfos()));
                payChannelBase = wallet;
            }
            if (EPayMethod.EMI.getMethod().equals(payMethod)) {
                EmiChannel emiChannel = new EmiChannel();
                emiChannel.setEmiChannelInfos(filterEmiWithAmount(orderAmount,
                        getEmiChannelInfo(payChannelOptionViewBiz.getEmiChannelInfos())));
                emiChannel.setEmiHybridChannelInfos(filterEmiWithAmount(orderAmount,
                        getEmiChannelInfo(payChannelOptionViewBiz.getEmiHybridChannelInfos())));
                payChannelBase = emiChannel;
                emiChannel.setInstName(payChannelOptionViewBiz.getInstName());
                emiChannel.setInstId(payChannelOptionViewBiz.getInstId());
            }
            if (payChannelBase == null)
                continue;
            boolean successRateFlag = getSuccessRateFlag(payMethod, successRateCacheModel, payChannelOptionViewBiz);
            payChannelBase.setHasLowSuccess(new StatusInfo(String.valueOf(successRateFlag), OfflinePaymentUtils
                    .successRateMsg(successRateFlag)));
            payChannelBase.setIsDisabled(new StatusInfo(String.valueOf(!payChannelOptionViewBiz.isEnableStatus()),
                    payChannelOptionViewBiz.getDisableReason()));
            payChannelBase.setPayChannelOption(payChannelOptionViewBiz.getPayOption());
            payChannelBase.setPayMethod(payMethod);
            if (!EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                    && !EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)) {
                payChannelBase.setIconUrl(commonFacade.getLogoName(payChannelOptionViewBiz.getInstId(), eChannelId));
                payChannelBase.setBankLogoUrl(commonFacade.getBankLogo(payChannelOptionViewBiz.getInstId()));
            }
            payChannelBase.setDirectServiceInsts(payChannelOptionViewBiz.getDirectServiceInsts());
            payChannelBase.setSupportAtmPins(payChannelOptionViewBiz.getSupportAtmPins());
            payChannelOptions.add(payChannelBase);
        }
        return payChannelOptions;
    }

    private boolean getSuccessRateFlag(String payMethod, SuccessRateCacheModel successRateCacheModel,
            PayChannelOptionViewBiz payChannelOptionViewBiz) {
        if (null == successRateCacheModel) {
            return false;
        }
        return hasLowSuccessRate(payChannelOptionViewBiz, payMethod, successRateCacheModel);
    }

    private boolean hasLowSuccessRate(PayChannelOptionViewBiz payChannelOptionViewBiz, String payMethod,
            SuccessRateCacheModel successRateCacheModel) {
        if (payChannelOptionViewBiz == null || StringUtils.isEmpty(payChannelOptionViewBiz.getInstId())
                || StringUtils.isEmpty(payMethod))
            return false;
        return commonFacade.hasLowSuccessRate(payChannelOptionViewBiz.getInstId(), payMethod, successRateCacheModel);
    }

    private BalanceInfo getBalanceInfo(List<BalanceChannelInfoBiz> balanceChannelInfoBizs) {
        // TODO:check
        if (balanceChannelInfoBizs == null || balanceChannelInfoBizs.isEmpty() || balanceChannelInfoBizs.get(0) == null)
            return null;
        BalanceInfo balanceInfo = new BalanceInfo(balanceChannelInfoBizs.get(0).getPayerAccountNo(), new Money(
                balanceChannelInfoBizs.get(0).getAccountBalance()), true);
        return balanceInfo;
    }

    private List<EMIChannelInfo> getEmiChannelInfo(List<EMIChannelInfoBiz> emiChannelInfos) {
        List<EMIChannelInfo> result = new ArrayList<>();
        for (EMIChannelInfoBiz emiChannelInfo : emiChannelInfos) {
            EMIChannelInfo channelInfo = new EMIChannelInfo(emiChannelInfo.getPlanId(), emiChannelInfo.getTenureId(),
                    emiChannelInfo.getInterestRate(), emiChannelInfo.getOfMonths(), new Money(
                            emiChannelInfo.getMinAmount()), new Money(emiChannelInfo.getMaxAmount()),
                    CardAcquiringMode.valueOf(emiChannelInfo.getCardAcquiringMode()),
                    emiChannelInfo.getPerInstallment());
            result.add(channelInfo);
        }
        return result;
    }

    private List<EMIChannelInfo> filterEmiWithAmount(Money orderAmount, List<EMIChannelInfo> emiChannelInfos) {
        List<EMIChannelInfo> result = new ArrayList<>();
        for (EMIChannelInfo emiChannelInfo : emiChannelInfos) {
            if (Double.valueOf(orderAmount.getValue()) < Double.valueOf(emiChannelInfo.getMaxAmount().getValue())
                    && Double.valueOf(emiChannelInfo.getMinAmount().getValue()) < Double
                            .valueOf(orderAmount.getValue())) {
                result.add(emiChannelInfo);
            }
        }
        return result;
    }

    private BalanceInfo getBalanceInfoFromExternalAccount(List<ExternalAccountInfoBiz> externalAccountInfoBizs) {
        if (externalAccountInfoBizs == null || externalAccountInfoBizs.isEmpty())
            return null;

        ExternalAccountInfoBiz externalAccountInfoBiz = externalAccountInfoBizs.get(0);
        if (externalAccountInfoBiz == null || StringUtils.isEmpty(externalAccountInfoBiz.getAccountBalance())
                || StringUtils.isEmpty(externalAccountInfoBiz.getExternalAccountNo()))
            return null;
        BalanceInfo balanceInfo = new DigitalCreditBalanceInfo(externalAccountInfoBiz.getExternalAccountNo(),
                new Money(externalAccountInfoBiz.getAccountBalance()), externalAccountInfoBiz.getExtendInfo(), true);
        return balanceInfo;
    }

    private List<CountryCode> toListOfCountryCodes(List<String> countries) {
        if (countries == null)
            return Collections.emptyList();
        List<CountryCode> countryCodes = new ArrayList<>();
        for (String country : countries) {
            countryCodes.add(CountryCode.valueOf(country));
        }
        return countryCodes;
    }

    private BalanceChannel balanceChannel(PayChannelBase payChannelBase, BalanceInfo balanceInfo) {
        if (payChannelBase == null)
            return new BalanceChannel();
        return new BalanceChannel(payChannelBase.getPayMethod(), payChannelBase.getPayChannelOption(),
                payChannelBase.getIsDisabled(), payChannelBase.getHasLowSuccess(), payChannelBase.getIconUrl(),
                balanceInfo);
    }

    private BalanceInfo getBalanceInfoForPPBL(UserDetailsBiz userDetailsBiz) {
        return commonFacade.getPaytmBankBalanceInfo(userDetailsBiz);
    }

}