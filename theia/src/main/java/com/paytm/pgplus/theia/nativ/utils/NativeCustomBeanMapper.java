package com.paytm.pgplus.theia.nativ.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.enums.BankTransferCheckoutFlow;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BinRestrictedCard;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.common.util.PayMethodUtility;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.user.enums.CardType;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.wallet.models.SubWalletDetailsList;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.models.MandateAccountDetails;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.TwoFARespData;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.enums.PaymentBankAccountStatus;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.model.payview.response.UPI;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.offline.enums.CountryCode;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.model.response.ConvertToAddNPayOfferDetails;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.DEFAULT_INVESTMENT_FUNDING_TNC_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.INVESTMENT_AS_FUNDING_SOURCE_TNC_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.E_MANDATE;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component("nativeCustomBeanMapper")
public class NativeCustomBeanMapper implements ICustomBeanMapper<NativeCashierInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCustomBeanMapper.class);
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
    @Qualifier("emiUtil")
    private EmiUtil emiUtil;

    @Autowired
    @Qualifier("hybridDisablingUtil")
    private HybridDisablingUtil hybridDisablingUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("prepaidCardValidationUtil")
    protected PrepaidCardValidationUtil prepaidCardValidationUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("paymentOffersServiceHelper")
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Autowired
    @Qualifier("subscriptionNativeValidationService")
    private ISusbcriptionNativeValidationService susbcriptionNativeValidationService;

    public static final BigDecimal BIG_DECIMAL_1200 = new BigDecimal("1200");

    public static final BigDecimal BIG_DECIMAL_1 = new BigDecimal("1");
    public static final String COFT_DUMMY_FIRST_SIX = "000000";

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("emiBinValidationUtil")
    EmiBinValidationUtil emiBinValidationUtil;

    @Autowired
    @Qualifier("nativeCodUtils")
    NativeCODUtils nativeCodUtils;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private CorporateCardUtil corporateCardUtil;

    @Autowired
    private CoftTokenDataService coftTokenDataService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public NativeCashierInfoResponse getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest) {
        return getCashierInfoResponse(workFlowResponseBean, cashierInfoRequest, false);
    }

    @Override
    public NativeCashierInfoResponse getCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest, boolean disableWallet) {
        LOGGER.debug("Mapping WorkflowResponseBean to NativePayOptionResponse ...");
        NativeCashierInfoResponseBody reponseBody = new NativeCashierInfoResponseBody();
        EPayMode paymentFlow = workFlowResponseBean.getAllowedPayMode();
        reponseBody.setPaymentFlow(paymentFlow);
        if (isVersionAllowed(workFlowResponseBean)) {
            reponseBody.setIconBaseUrl(commonFacade.getBaseIconUrl());
        }
        reponseBody.setQrCodeDetailsResponse(workFlowResponseBean.getQrCodeDetails());
        reponseBody.setProductCode(workFlowResponseBean.getProductCode());
        LitePayviewConsultResponseBizBean merchnatLiteViewResponse = workFlowResponseBean.getMerchnatLiteViewResponse();
        LitePayviewConsultResponseBizBean addAndPayLiteViewResponse = workFlowResponseBean
                .getAddAndPayLiteViewResponse();
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        EChannelId eChannelId = cashierInfoRequest.getBody().getChannelId();
        PayOption merchantPayOption = new PayOption();
        PayOption addMoneyPayOption = new PayOption();

        String mobileNo = (userDetailsBiz != null && StringUtils.isNotBlank(userDetailsBiz.getMobileNo())) ? userDetailsBiz
                .getMobileNo() : null;
        if (StringUtils.isBlank(mobileNo) && cashierInfoRequest.getBody().getUserInfo() != null
                && StringUtils.isNotBlank(cashierInfoRequest.getBody().getUserInfo().getMobile())) {
            mobileNo = cashierInfoRequest.getBody().getUserInfo().getMobile();
        }

        boolean isPrepaidCardFeatureEnabled = iPgpFf4jClient.checkWithdefault(
                TheiaConstant.PrepaidCard.FF4J_PREPAID_CARD_STRING, new HashMap<>(), false);

        MandateMode mandateType = cashierInfoRequest.getBody().getMandateType() != null ? cashierInfoRequest.getBody()
                .getMandateType()
                : (cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody() != null ? MandateMode
                        .getByName(cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody()
                                .getMandateType()) : null);

        if (merchnatLiteViewResponse != null) {
            setPostpaidEnabledOnMerchantAndDisabledOnUser(workFlowResponseBean, reponseBody);
            merchantPayOption.setPayMethods(getPayMethods(cashierInfoRequest.getBody().getOrderAmount(),
                    merchnatLiteViewResponse.getPayMethodViews(), eChannelId, userDetailsBiz, paymentFlow,
                    workFlowResponseBean, mandateType, false, isPrepaidCardFeatureEnabled, cashierInfoRequest.getBody()
                            .getSubscriptionTransactionRequestBody(), mobileNo));
            reponseBody.setWalletOnly(merchnatLiteViewResponse.isWalletOnly());
        }

        if (addAndPayLiteViewResponse != null && !disableWallet) {
            reponseBody.setAddAndPayMerchant(true);
        }
        if (addAndPayLiteViewResponse != null
                && (EPayMode.ADDANDPAY.equals(workFlowResponseBean.getAllowedPayMode()) || (cashierInfoRequest
                        .getBody().isFetchAddMoneyOptions() && merchantPreferenceService.convertTxnToAddNPayEnabled(
                        workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID(), false)))) {
            List<PayMethod> addMoneyPayMethods = getPayMethods(cashierInfoRequest.getBody().getOrderAmount(),
                    addAndPayLiteViewResponse.getPayMethodViews(), eChannelId, userDetailsBiz, paymentFlow,
                    workFlowResponseBean, mandateType, true, isPrepaidCardFeatureEnabled, cashierInfoRequest.getBody()
                            .getSubscriptionTransactionRequestBody(), mobileNo);
            addMoneyPayOption.setPayMethods(addMoneyPayMethods);
            if (ff4jUtils.isFeatureEnabledOnMid(workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID(),
                    TheiaConstant.FF4J.REMOVE_ADDNPAY_CC_PAYMODE, false)) {
                List<PayMethod> addMoneyPaymode = addMoneyPayMethods
                        .stream()
                        .filter(paymode -> (StringUtils.equalsIgnoreCase(EPayMethod.CREDIT_CARD.getNewDisplayName(),
                                paymode.getDisplayName()))).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(addMoneyPaymode)) {
                    addMoneyPayMethods.removeAll(addMoneyPaymode);
                }
            }
            reponseBody.setWalletOnly(merchnatLiteViewResponse.isWalletOnly());
        }
        String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
        if (userDetailsBiz != null
                && !TheiaConstant.RequestTypes.UNI_PAY.equalsIgnoreCase(cashierInfoRequest.getBody().getRequestType())) {
            merchantPayOption.setSavedInstruments(getSavedInstruments(userDetailsBiz.getMerchantViewSavedCardsList(),
                    eChannelId, merchantPayOption.getPayMethods(), cashierInfoRequest.getBody(), false,
                    workFlowResponseBean, isPrepaidCardFeatureEnabled, mid,
                    workFlowResponseBean.getMerchnatLiteViewResponse(),
                    workFlowResponseBean.getAddAndPayLiteViewResponse()));
            addMoneyPayOption.setSavedInstruments(getSavedInstruments(userDetailsBiz.getAddAndPayViewSavedCardsList(),
                    eChannelId, addMoneyPayOption.getPayMethods(), cashierInfoRequest.getBody(), true,
                    workFlowResponseBean, isPrepaidCardFeatureEnabled, mid,
                    workFlowResponseBean.getMerchnatLiteViewResponse(),
                    workFlowResponseBean.getAddAndPayLiteViewResponse()));
            if (MandateMode.E_MANDATE.equals(mandateType) && workFlowResponseBean.getUpiProfileV4() != null
                    && workFlowResponseBean.getUpiProfileV4().getRespDetails() != null
                    && workFlowResponseBean.getUpiProfileV4().getRespDetails().getProfileDetail() != null) {
                merchantPayOption.setSavedMandateBanks(getSavedMandateBanks(merchantPayOption.getPayMethods(),
                        workFlowResponseBean.getUpiProfileV4()));
            }
        }
        // for not logged in flow
        if (null == userDetailsBiz) {
            List<CardBeanBiz> merchantCustomeCardList = workFlowResponseBean.getmIdCustIdCardBizDetails() != null ? workFlowResponseBean
                    .getmIdCustIdCardBizDetails().getMerchantCustomerCardList() : null;
            merchantPayOption.setSavedInstruments((getSavedInstruments(merchantCustomeCardList, eChannelId,
                    merchantPayOption.getPayMethods(), cashierInfoRequest.getBody(), false, workFlowResponseBean,
                    isPrepaidCardFeatureEnabled, mid, workFlowResponseBean.getMerchnatLiteViewResponse(),
                    workFlowResponseBean.getAddAndPayLiteViewResponse())));
        }
        reponseBody.setMerchantPayOption(merchantPayOption);
        reponseBody.setAddMoneyPayOption(addMoneyPayOption);
        reponseBody.setOnTheFlyKYCRequired(workFlowResponseBean.isOnTheFlyKYCRequired());
        reponseBody.setRiskConvenienceFee(workFlowResponseBean.getRiskConvenienceFee());

        // if (merchnatLiteViewResponse != null
        // && merchnatLiteViewResponse.getPayMethodViews() != null
        // && merchnatLiteViewResponse.getPayMethodViews().get(0) != null
        // &&
        // merchnatLiteViewResponse.getPayMethodViews().get(0).getPayChannelOptionViews()
        // != null
        // &&
        // merchnatLiteViewResponse.getPayMethodViews().get(0).getPayChannelOptionViews().get(0)
        // != null
        // &&
        // merchnatLiteViewResponse.getPayMethodViews().get(0).getPayChannelOptionViews().get(0)
        // .getExternalAccountInfos() != null
        // &&
        // merchnatLiteViewResponse.getPayMethodViews().get(0).getPayChannelOptionViews().get(0)
        // .getExternalAccountInfos().get(0) != null
        // &&
        // merchnatLiteViewResponse.getPayMethodViews().get(0).getPayChannelOptionViews().get(0)
        // .getExternalAccountInfos().get(0).getExtendInfo() != null) {
        // reponseBody.setInfoButtonUpdateMessage(merchnatLiteViewResponse.getPayMethodViews().get(0)
        // .getPayChannelOptionViews().get(0).getExternalAccountInfos().get(0).getExtendInfo());
        // }
        reponseBody.setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        response.setBody(reponseBody);
        if (isVersionAllowed(workFlowResponseBean)) {
            response.setHead(new ResponseHeader(workFlowResponseBean.getApiVersion()));
        } else {
            response.setHead(new ResponseHeader());
        }
        if (response.getBody() != null) {
            if (response.getBody().getMerchantPayOption() != null) {
                makePaytmPaymentBankAsSeparatePayMethod(response.getBody().getMerchantPayOption(), workFlowResponseBean);
            }
            if (response.getBody().getAddMoneyPayOption() != null) {
                makePaytmPaymentBankAsSeparatePayMethod(response.getBody().getAddMoneyPayOption(), workFlowResponseBean);
            }
        }

        String txnAmount = StringUtils.isNotBlank(workFlowResponseBean.getWorkFlowRequestBean().getTxnAmount()) ? workFlowResponseBean
                .getWorkFlowRequestBean().getTxnAmount() : workFlowResponseBean.getWorkFlowRequestBean()
                .getAmountForWalletConsultInRisk();

        if (txnAmount != null
                && ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equals(
                        workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID())
                && !workFlowResponseBean.getWorkFlowRequestBean().isInternalFetchPaymentOptions()
                && Double.valueOf(AmountUtils.getTransactionAmountInRupee(txnAmount)) < Double
                        .valueOf(ConfigurationUtil.getProperty(MINIMUM_AMOUNT_FOR_NB_AS_PAYMODE, "0"))) {
            removeNetBankingAsPaymode(response.getBody().getMerchantPayOption(), workFlowResponseBean);
        } else if (txnAmount != null
                && workFlowResponseBean.getWorkFlowRequestBean().isNativeAddMoney()
                && !workFlowResponseBean.getWorkFlowRequestBean().isInternalFetchPaymentOptions()
                && Double.valueOf(AmountUtils.getTransactionAmountInRupee(txnAmount)) < Double
                        .valueOf(ConfigurationUtil.getProperty(MINIMUM_AMOUNT_FOR_NB_AS_PAYMODE, "0"))
                && !(PaymentTypeIdEnum.NB.getValue().equals(
                        workFlowResponseBean.getWorkFlowRequestBean().getPaymentTypeId()) && "YES"
                        .equals(workFlowResponseBean.getWorkFlowRequestBean().getPayModeOnly()))) {
            removeNetBankingAsPaymode(response.getBody().getMerchantPayOption(), workFlowResponseBean);
        }

        /*
         * populate mandate account details in response if received in
         * subscription create request for Bank Mandates
         */
        if (cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody() != null
                && cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody().getMandateAccountDetails() != null) {
            MandateAccountDetails mandateAccountDetails = cashierInfoRequest.getBody()
                    .getSubscriptionTransactionRequestBody().getMandateAccountDetails();
            populateMandateBankAccountDetailsInResponse(merchantPayOption.getPayMethods(), mandateAccountDetails,
                    response);
        }

        boolean convertTxnToAddNPayPreferenceEnabled = merchantPreferenceService.convertTxnToAddNPayEnabled(
                cashierInfoRequest.getHead().getMid(), false);
        if (convertTxnToAddNPayPreferenceEnabled && !disableWallet) {
            populateUpiToAddNPayTxnDetails(reponseBody);
        } else if (convertTxnToAddNPayPreferenceEnabled) {
            LOGGER.info("removing upitoaddnpay details due to disabled wallet");
        }
        LOGGER.debug("Mapping WorkflowResponseBean to NativePayOptionResponse done");
        return response;
    }

    private void setPostpaidEnabledOnMerchantAndDisabledOnUser(WorkFlowResponseBean workFlowResponseBean,
            NativeCashierInfoResponseBody responseBody) {
        LitePayviewConsultResponseBizBean merchantLitePayViewResponse = workFlowResponseBean
                .getMerchnatLiteViewResponse();
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
        if (userDetailsBiz == null || userDetailsBiz.isPaytmCCEnabled() || merchantLitePayViewResponse == null) {
            return;
        }
        for (PayMethodViewsBiz payMethodViewsBiz : merchantLitePayViewResponse.getPayMethodViews()) {
            if (payMethodViewsBiz.getPayMethod().equalsIgnoreCase(PAYTM_DIGITAL_CREDIT)) {
                if (merchantPreferenceService.isPostpaidEnabledOnMerchant(mid, false)
                        && !userDetailsBiz.isPaytmCCEnabled()) {
                    responseBody.setIsPostpaidEnabledOnMerchantAndDisabledOnUser(true);
                }
            }
        }
    }

    private void populateMandateBankAccountDetailsInResponse(List<PayMethod> payMethods,
            MandateAccountDetails mandateAccountDetails, NativeCashierInfoResponse response) {

        List<PayChannelBase> payOptionChannels = new ArrayList<>();

        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.BANK_MANDATE.getMethod().equals(payMethod.getPayMethod())) {
                payOptionChannels = payMethod.getPayChannelOptions();
            }
        }
        Iterator<PayChannelBase> bankMandateIterator = payOptionChannels.iterator();
        while (bankMandateIterator.hasNext()) {
            BankMandate bankMandate = (BankMandate) bankMandateIterator.next();
            if (bankMandate.getInstId().equals(mandateAccountDetails.getBankCode())) {
                List<MandateAuthMode> authModeFromMapping = bankMandate.getMandateAuthMode();
                if (CollectionUtils.isNotEmpty(authModeFromMapping)) {
                    List<String> mandateAuthMode = new ArrayList<>();
                    for (MandateAuthMode authMode : authModeFromMapping) {
                        mandateAuthMode.add(authMode.name());
                    }
                    mandateAccountDetails.setMandateAuthMode(mandateAuthMode);
                }
                mandateAccountDetails.setBank(bankMandate.getInstName());
                mandateAccountDetails.setIconUrl(bankMandate.getIconUrl());
                break;
            }
        }

        response.getBody().setMandateAccountDetails(mandateAccountDetails);

    }

    private void makePaytmPaymentBankAsSeparatePayMethod(PayOption payOption, WorkFlowResponseBean workFlowResponseBean) {
        if (null != payOption && null != payOption.getPayMethods()) {
            int netBankingIndex = 0;
            int payMethodCounter = 0;
            // Above variables to save NET_BANKING payMethod index in payOption
            boolean isNetBankingEmpty = false;
            boolean paymentsBankEnabled = false;
            boolean isPPBLSupported = true;
            PayMethod ppblPayMethod = null;
            PayMethod payMethodPPBL = new PayMethod();
            for (PayMethod payMethod : payOption.getPayMethods()) {
                if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())) {
                    for (Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator(); payChannelBaseIterator
                            .hasNext();) {
                        PayChannelBase nextPayChannelBase = payChannelBaseIterator.next();
                        if (EPayMethod.PPBL.getMethod().equals(nextPayChannelBase.getPayChannelOption())) {
                            paymentsBankEnabled = true;
                            nextPayChannelBase.setBankLogoUrl(commonFacade.getBankLogo("PPBL"));
                            if (payMethodPPBL != null) {
                                payMethodPPBL.setPayMethod(EPayMethod.PPBL.getOldName());
                                payMethodPPBL.setDisplayName(EPayMethod.PPBL.getNewDisplayName());
                                payMethodPPBL.setPayChannelOptions(new ArrayList<>());
                                BalanceChannel balanceChannel = balanceChannel(payMethodPPBL, nextPayChannelBase,
                                        getPPBLAccountInfo(workFlowResponseBean.getPpblAccountResponse()),
                                        workFlowResponseBean.getUserDetails());
                                if (workFlowResponseBean.getWorkFlowRequestBean().isReturnDisabledChannelInFpo()
                                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason()
                                                .equals(nextPayChannelBase.getIsDisabled().getMsg())) {
                                    BalanceStatusInfo balanceStatusInfo = (BalanceStatusInfo) balanceChannel
                                            .getIsDisabled();
                                    StatusInfo newStatusInfo = new BalanceStatusInfo(
                                            balanceStatusInfo.getUserAccountExist(),
                                            balanceStatusInfo.getMerchantAccept(), balanceStatusInfo.getStatus(),
                                            balanceStatusInfo.getMsg());
                                    newStatusInfo.setShowDisabled(Boolean.TRUE);
                                    newStatusInfo.setDisplayMsg(ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE
                                            .getMessage());
                                    balanceChannel.setIsDisabled(newStatusInfo);
                                }
                                payMethodPPBL.getPayChannelOptions().add(balanceChannel);
                                payMethodPPBL.setRemainingLimit(payMethod.getRemainingLimit());
                                payMethodPPBL.setPayOptionRemainingLimits(payMethod.getPayOptionRemainingLimits());
                            }
                            payChannelBaseIterator.remove();
                            if (payMethod.getPayChannelOptions().isEmpty()) {
                                isNetBankingEmpty = true;
                            }
                            break;
                        }

                    }
                    netBankingIndex = payMethodCounter;
                    // hack to support subscription flow
                } else if (EPayMethod.PPBL.name().equals(payMethod.getPayMethod())) {
                    isPPBLSupported = isPPBLSupported(payMethod, workFlowResponseBean.getUserDetails());
                    if (payMethod.getPayChannelOptions() == null) {
                        payMethod.setPayChannelOptions(new ArrayList<>());
                    }
                    Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                    BalanceChannel balanceChannel = null;
                    while (payChannelBaseIterator.hasNext()) {
                        PayChannelBase payChannelBase = payChannelBaseIterator.next();
                        if (EPayMethod.PPBL.getOldName().equals(payChannelBase.getPayChannelOption())) {
                            payChannelBase.setBankLogoUrl(commonFacade.getBankLogo("PPBL"));
                            balanceChannel = balanceChannel(payMethodPPBL, payChannelBase, null,
                                    workFlowResponseBean.getUserDetails());
                            payChannelBaseIterator.remove();
                            break;
                        }
                    }
                    payMethod.getPayChannelOptions().add(balanceChannel);
                    ppblPayMethod = payMethod;
                }
                payMethodCounter++;
            }
            if (paymentsBankEnabled) {
                payOption.getPayMethods().add(payMethodPPBL);
            }
            if (!isPPBLSupported) {
                payOption.getPayMethods().remove(ppblPayMethod);
            }
            hideNetBankingWhenDisabled(workFlowResponseBean, payOption, netBankingIndex, isNetBankingEmpty);
        }
    }

    // Method to remove NET_BANKING payMethod from payOption when no banks are
    // available
    private void hideNetBankingWhenDisabled(WorkFlowResponseBean workFlowResponseBean, PayOption payOption,
            int netBankingIndex, boolean isNetBankingEmpty) {
        if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null
                && StringUtils.isNotBlank(workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID())) {
            String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
            boolean hideNetBanking = merchantPreferenceService.hideNetBankingBlank(mid, false);
            if (hideNetBanking && isNetBankingEmpty) {
                payOption.getPayMethods().remove(netBankingIndex);
            }
        }
    }

    private AccountInfo getPPBLAccountInfo(FetchAccountBalanceResponse accountBalanceResponse) {
        com.paytm.pgplus.theia.utils.ConfigurationUtil conf = new com.paytm.pgplus.theia.utils.ConfigurationUtil();
        if (accountBalanceResponse == null || accountBalanceResponse.getStatus().equals("FAILURE"))
            return null;
        PPBLAccountInfo accountInfo = new PPBLAccountInfo();
        if (accountBalanceResponse.getAccountState() != null
                && (PaymentBankAccountStatus.DEBIT_FREEZED.name().equals(accountBalanceResponse.getAccountState())
                        || PaymentBankAccountStatus.TOTAL_FREEZED.name().equals(
                                accountBalanceResponse.getAccountState()) || PaymentBankAccountStatus.CLOSED.name()
                        .equals(accountBalanceResponse.getAccountState()))) {
            accountInfo
                    .setDisplayMessage(conf
                            .getProperty(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.PAYMENT_BANK_FROZEN_ACCOUNT_MESSAGE));
        }

        accountInfo.setRedeemableInvestmentBalance(accountBalanceResponse.getRedeemableInvestmentBalance());
        accountInfo.setIsRedemptionAllowed(accountBalanceResponse.isRedemptionAllowed());
        accountInfo.setPartnerBankBalances(accountBalanceResponse.getJsonPartnerBankBalances());
        accountInfo.setInvestmentTnCUrl(com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(
                INVESTMENT_AS_FUNDING_SOURCE_TNC_URL, DEFAULT_INVESTMENT_FUNDING_TNC_URL));
        accountInfo.setAccountStatus(accountBalanceResponse.getAccountState());
        accountInfo.setAccountBalance(new Money(String.valueOf(accountBalanceResponse.getEffectiveBalance())));
        return accountInfo;
    }

    private void removeNetBankingAsPaymode(PayOption payOption, WorkFlowResponseBean workFlowResponseBean) {
        Iterator<PayMethod> payMethodIterator = payOption.getPayMethods().iterator();
        while (payMethodIterator.hasNext()) {
            if (com.paytm.pgplus.enums.PayMethod.NET_BANKING.getMethod()
                    .equals(payMethodIterator.next().getPayMethod())) {
                LOGGER.info("Removing NET_BANKING as Paymethod as the amount is below minimum amount for NB as paymode");
                payMethodIterator.remove();
                break;
            }
        }
    }

    private boolean isPPBLSupported(PayMethod payMethod, UserDetailsBiz userDetailsBiz) {
        if (payMethod.getIsDisabled() != null && userDetailsBiz != null) {
            try {
                return (!Boolean.parseBoolean(payMethod.getIsDisabled().getStatus()))
                        && userDetailsBiz.isSavingsAccountRegistered();
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    private List<PayChannelBase> getSavedInstruments(List<CardBeanBiz> cardBeanBizs, EChannelId eChannelId,
            List<PayMethod> payMethods, CashierInfoRequestBody cashierInfoRequestBody, boolean addnPay,
            WorkFlowResponseBean flowResponseBean, boolean isPrepaidFeatureEnabled, String mid,
            LitePayviewConsultResponseBizBean merchantLitePayviewConsultResponse,
            LitePayviewConsultResponseBizBean AddNPayLitePayviewConsultResponse) {
        List<PayChannelBase> savedCards = new ArrayList<>();
        if (cardBeanBizs == null)
            return savedCards;

        for (CardBeanBiz cardBeanBiz : cardBeanBizs) {
            String payChannelOption = cardBeanBiz.getCardType() + "_" + cardBeanBiz.getCardScheme();
            PayChannelBase payChannelBase = getPayChannelOptionFromPayMethods(payMethods, cardBeanBiz.getCardType(),
                    payChannelOption);
            if (payChannelBase instanceof BankCard) {
                if (validPayChannelBaseForSavedCard(cardBeanBiz, (BankCard) payChannelBase, cashierInfoRequestBody,
                        addnPay, flowResponseBean, isPrepaidFeatureEnabled)
                        && validPayChannelForCardSubTypeSavedCard(cardBeanBiz, payChannelBase)) {
                    SavedCard savedCard = getSavedCard(cardBeanBiz, payChannelOption, (BankCard) payChannelBase,
                            payMethods, eChannelId, flowResponseBean, isPrepaidFeatureEnabled);
                    setExtraInfoInSavedCard(savedCard, cardBeanBiz, cashierInfoRequestBody, mid,
                            merchantLitePayviewConsultResponse, AddNPayLitePayviewConsultResponse);
                    savedCards.add(savedCard);
                }
            }
        }

        return savedCards;
    }

    private boolean validPayChannelForCardSubTypeSavedCard(CardBeanBiz cardBeanBiz, PayChannelBase payChannelBase) {

        boolean validPayChannel = true;

        List<String> supportedCardSubType = corporateCardUtil.prepareCardSubTypeListFromCardBeanz(cardBeanBiz);

        if (CollectionUtils.isNotEmpty(supportedCardSubType)) {

            if (CollectionUtils.isEmpty(payChannelBase.getSupportedCardSubTypes())) {
                validPayChannel = false;
            } else {
                if (!payChannelBase.getSupportedCardSubTypes().containsAll(supportedCardSubType)) {
                    validPayChannel = false;
                }
            }
        }

        return validPayChannel;
    }

    private void setExtraInfoInSavedCard(SavedCard savedCard, CardBeanBiz cardBeanBiz,
            CashierInfoRequestBody cashierInfoRequestBody, String mid,
            LitePayviewConsultResponseBizBean merchantLitePayviewConsultResponse,
            LitePayviewConsultResponseBizBean addNPayLitePayviewConsultResponse) {
        String bankCode = cardBeanBiz.getInstId();
        String networkCode = cardBeanBiz.getCardScheme();
        PayCardOptionViewBiz payCardOptionViewBiz = null;
        try {

            /**
             * PG will start sending CIN and bin 8 alias to promo in OR Flow
             * once <theia.sendCINAnd8BinHashToPromo> FF4J Flag get enable. So
             * fetching CIN AND bin8Hash from Litepayview response
             */
            String cardNo = cardBeanBiz.getCardNumber();
            String expiryDate = cardBeanBiz.getExpiryDate();
            boolean cinToPromoEnable = ff4JUtil.isFeatureEnabledForPromo(mid);
            if (cashierInfoRequestBody.isCardHashRequired()) {
                if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GCIN_ON_COFT_PROMO, false)) {
                    setCardHashForCoft(mid, savedCard);
                } else {
                    payCardOptionViewBiz = setCardHash(savedCard, cardBeanBiz, merchantLitePayviewConsultResponse,
                            addNPayLitePayviewConsultResponse, cardNo, expiryDate, cinToPromoEnable);
                }
            }
            if (cashierInfoRequestBody.isEightDigitBinRequired()
                    && !(BinRestrictedCard.BIN_8_RESTRICTED_BANKS.contains(bankCode) || BinRestrictedCard.BIN_8_RESTRICTED_NETWORKS
                            .contains(networkCode))) {
                if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GCIN_ON_COFT_PROMO, false)) {
                    setEightDigitBinHashForCoft(cardBeanBiz, savedCard);
                } else {
                    setEightDigitBinHash(savedCard, cardBeanBiz, merchantLitePayviewConsultResponse,
                            addNPayLitePayviewConsultResponse, payCardOptionViewBiz, cardNo, expiryDate,
                            cinToPromoEnable);
                }
            }

        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error in generating hash of card number: {}", e.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Error while setting cardHash/eightBinDigit: {}", ex.getMessage());
        }

    }

    private void setEightDigitBinHashForCoft(CardBeanBiz cardBeanBiz, SavedCard savedCard) throws Exception {
        String eightDigitBinHash = null;
        if (savedCard.isCardCoft()) {
            String accountRangeCardBin = savedCard.getAccountRangeCardBin();
            if (StringUtils.isNotEmpty(accountRangeCardBin)) {
                CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(accountRangeCardBin.substring(
                        0, 8));
                if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                    eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
                }
            }
        } else {
            eightDigitBinHash = cardBeanBiz.getEightDigitBinHash();
        }

        if (StringUtils.isNotEmpty(eightDigitBinHash)) {
            savedCard.getCardDetails().setFirstEightDigit(eightDigitBinHash);
        } else {
            LOGGER.error("Unable to fetch eightDigitBinHash from Platform");
            throw new Exception("Unable to fetch eightDigitBinHash from Platform");
        }
    }

    private void setEightDigitBinHash(SavedCard savedCard, CardBeanBiz cardBeanBiz,
            LitePayviewConsultResponseBizBean merchantLitePayviewConsultResponse,
            LitePayviewConsultResponseBizBean addNPayLitePayviewConsultResponse,
            PayCardOptionViewBiz payCardOptionViewBiz, String cardNo, String expiryDate, boolean cinToPromoEnable)
            throws Exception {
        if (StringUtils.isNotEmpty(cardBeanBiz.getEightDigitBinHash())) {
            savedCard.getCardDetails().setFirstEightDigit(cardBeanBiz.getEightDigitBinHash());
        } else if (cinToPromoEnable) {
            if (null == payCardOptionViewBiz) {
                payCardOptionViewBiz = paymentOffersServiceHelper.getSavedCardInfoFromLitePayViewResponse(
                        merchantLitePayviewConsultResponse, addNPayLitePayviewConsultResponse, cardNo, expiryDate);
                if (null != payCardOptionViewBiz) {
                    savedCard
                            .getCardDetails()
                            .setFirstEightDigit(
                                    payCardOptionViewBiz
                                            .getExtendInfo()
                                            .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH));
                } else {
                    CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNo.substring(0, 8));
                    if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                        savedCard.getCardDetails().setFirstEightDigit(
                                cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash());
                    } else {
                        throw new Exception("Unable to fetch eightDigitBinHash from Platform");
                    }
                }
            } else {
                savedCard
                        .getCardDetails()
                        .setFirstEightDigit(
                                payCardOptionViewBiz
                                        .getExtendInfo()
                                        .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH));
            }
        } else {
            savedCard.getCardDetails().setFirstEightDigit(StringUtils.substring(cardBeanBiz.getCardNumber(), 0, 8));
        }
    }

    private void setCardHashForCoft(String mid, SavedCard savedCard) throws Exception {
        String merchantCoftConfig = coftTokenDataService.getMerchantConfig(mid);

        String cardHash = null;
        if (merchantCoftConfig.equals("GCIN")) {
            cardHash = savedCard.getGcin();
        } else if (merchantCoftConfig.equals("PAR")) {
            cardHash = savedCard.getPar();
        }

        if (StringUtils.isEmpty(cardHash)) {
            LOGGER.error("Unable to fetch unique identifier from Platform");
            throw new Exception("Unable to fetch unique identifier from Platform");
        }

        savedCard.getCardDetails().setcardHash(cardHash);
    }

    private PayCardOptionViewBiz setCardHash(SavedCard savedCard, CardBeanBiz cardBeanBiz,
            LitePayviewConsultResponseBizBean merchantLitePayviewConsultResponse,
            LitePayviewConsultResponseBizBean addNPayLitePayviewConsultResponse, String cardNo, String expiryDate,
            boolean cinToPromoEnable) throws Exception {
        PayCardOptionViewBiz payCardOptionViewBiz = null;
        if (cinToPromoEnable && StringUtils.isNotEmpty(cardBeanBiz.getCardIndexNo())) {
            savedCard.getCardDetails().setcardHash(cardBeanBiz.getCardIndexNo());
        } else if (cinToPromoEnable) {
            payCardOptionViewBiz = paymentOffersServiceHelper.getSavedCardInfoFromLitePayViewResponse(
                    merchantLitePayviewConsultResponse, addNPayLitePayviewConsultResponse, cardNo, expiryDate);
            if (null != payCardOptionViewBiz) {
                savedCard.getCardDetails().setcardHash(payCardOptionViewBiz.getCardIndexNo());
            } else {
                /**
                 * ***Temporary code - This will occur when any card from saved
                 * card PG DB is not present in Platform response and
                 * <theia.sendCINAnd8BinHashToPromo> FF4J Flag is true, then we
                 * have to send CIN to Promo.
                 */
                String cardIndexNumber = workFlowHelper.getCardIndexNoFromCardNumber(cardNo);
                if (null != cardIndexNumber) {
                    savedCard.getCardDetails().setcardHash(cardIndexNumber);
                } else {
                    LOGGER.error("Unable to fetch cardIndexNumber from Platform");
                    throw new Exception("Unable to fetch cardIndexNumber from Platform");
                }
            }
        } else {
            String cardHash = SignatureUtilWrapper.signApiRequest(cardBeanBiz.getCardNumber());
            savedCard.getCardDetails().setcardHash(cardHash);
        }
        return payCardOptionViewBiz;
    }

    private SavedCard getSavedCard(CardBeanBiz cardBeanBiz, String payChannelOption, BankCard bankCard,
            List<PayMethod> payMethods, EChannelId eChannelId, WorkFlowResponseBean flowResponseBean,
            boolean isPrepaidFeatureEnabled) {
        CardDetails cardDetails = new CardDetails();
        SavedCard savedCard = new SavedCard(cardDetails);
        if (cardBeanBiz.getCardId() != null) {
            cardDetails.setCardId(String.valueOf(cardBeanBiz.getCardId()));
        } else {
            cardDetails.setCardId(cardBeanBiz.getCardIndexNo());
        }
        cardDetails.setCardType(cardBeanBiz.getCardType());
        cardDetails.setExpiryDate(cardBeanBiz.getExpiryDate());
        savedCard.setPayChannelOption(payChannelOption);
        savedCard.setPayMethod(cardBeanBiz.getCardType());
        cardDetails.setCreated_on("");
        cardDetails.setUpdated_on("");
        savedCard.setSavedCardEmisubventionDetail(cardBeanBiz.getSavedCardEmiSubventionDetails());
        cardDetails.setUserId(cardBeanBiz.getUserId());
        cardDetails.setStatus(String.valueOf(cardBeanBiz.getStatus()));
        savedCard.setIssuingBank(cardBeanBiz.getInstId());
        cardDetails.setIndian(cardBeanBiz.isIndian());

        savedCard.setInstId(bankCard.getInstId());
        savedCard.setInstName(bankCard.getInstName());
        savedCard.setIsDisabled(bankCard.getIsDisabled());
        savedCard.setIconUrl(commonFacade.getLogoUrl(bankCard.getInstId(), eChannelId));

        savedCard.setBankLogoUrl(commonFacade.getBankLogo(cardBeanBiz.getInstId()));
        savedCard.setHasLowSuccess(bankCard.getHasLowSuccess());

        cardDetails.setFirstSixDigit(cardBeanBiz.getFirstSixDigit().equals(0L) ? COFT_DUMMY_FIRST_SIX : String
                .valueOf(cardBeanBiz.getFirstSixDigit()));
        cardDetails.setLastFourDigit(NativePaymentUtil.getLastFourDigits(cardBeanBiz.getLastFourDigit()));
        cardDetails.setCvvLength(cardUtils.getCardSchemeInfo(bankCard.getInstId()).getCvvLength());
        cardDetails
                .setCvvRequired(Boolean.valueOf(cardUtils.getCardSchemeInfo(bankCard.getInstId()).getIsCVVRequired()));
        savedCard.setSupportedCountries(((BankCard) bankCard).getSupportedCountries());

        EmiChannel emiChannel = null;

        if (Boolean.parseBoolean(ConfigurationUtil.getProperty("debitcard.emi.enabled", "false"))
                || !CardType.DEBIT_CARD.getValue().equalsIgnoreCase(cardDetails.getCardType())) {
            if (cardBeanBiz.getFirstSixDigit() != null
                    && cardUtils.getEmiEnableFlagByBin(String.valueOf(cardBeanBiz.getFirstSixDigit()))) {
                emiChannel = getEmiChannel(savedCard, payMethods, EmiType.CREDIT_CARD);
            }
        }

        if (CardType.DEBIT_CARD.getValue().equalsIgnoreCase(cardDetails.getCardType())) {
            emiChannel = getEmiChannel(savedCard, payMethods, EmiType.DEBIT_CARD);
        }

        boolean isPrepaidCard = false;
        if (cardBeanBiz.isPrepaidCard() && isPrepaidFeatureEnabled) {
            isPrepaidCard = true;
        }
        savedCard.setCorporateCard(cardBeanBiz.isCorporateCard());

        if (null != emiChannel && !Boolean.valueOf(emiChannel.getIsDisabled().getStatus()) && !isPrepaidCard) {
            if (((!cardBeanBiz.isCardCoft()) || (ff4jUtils.isFeatureEnabledOnMid(flowResponseBean
                    .getWorkFlowRequestBean().getPaytmMID(), TheiaConstant.FF4J.ENABLE_EMI_PAYMODE_ON_COFT_CARD, false) && isCoftCardEmiEligible(cardBeanBiz)))) {
                savedCard.setIsEmiAvailable(true);
                savedCard.setMinAmount(emiChannel.getMinAmount());
                savedCard.setMaxAmount(emiChannel.getMaxAmount());
                savedCard.setEmiDetails(emiChannel);
                savedCard.setEmiHybridDisabled(emiChannel.isHybridDisabled());
            }
        }

        boolean isDirectPayOption = getIsDirectPayOption(savedCard, payMethods);
        List<String> authModes = new ArrayList<String>();
        authModes.add(AuthMode.OTP.getType());
        if (isDirectPayOption) {
            authModes.add(AuthMode.PIN.getType());
        }
        savedCard.setAuthModes(authModes);

        /*
         * Support For Visa Single Click
         */
        if (BooleanUtils.isTrue(cardBeanBiz.getOneClickSupported())
                && BooleanUtils.isTrue(bankCard.getOneClickSupported())) {
            if (((!cardBeanBiz.isCardCoft()) || (ff4jUtils
                    .isFeatureEnabledOnMid(flowResponseBean.getWorkFlowRequestBean().getPaytmMID(),
                            TheiaConstant.FF4J.ENABLE_ONECLICK_TXN_ON_COFT_CARD, false)))) {
                savedCard.setOneClickSupported(true);
            } else {
                savedCard.setOneClickSupported(false);
            }
        } else {
            savedCard.setOneClickSupported(false);
        }
        setEventLogForOneClickSupported(savedCard);

        if (isPrepaidFeatureEnabled) {
            savedCard.setPrepaidCard(cardBeanBiz.isPrepaidCard());
            savedCard.setPrepaidCardSupported(null);
        }
        // setting list for cardSubType
        List<String> supportedCardSubTypes = corporateCardUtil.prepareCardSubTypeListFromCardBeanz(cardBeanBiz);
        if (CollectionUtils.isNotEmpty(supportedCardSubTypes)) {
            savedCard.setSupportedCardSubTypes(supportedCardSubTypes);
        }
        savedCard.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(savedCard.getPayMethod(),
                savedCard.getInstId()) || hybridDisablingUtil.isHybridDisabledForBank(savedCard.getIssuingBank()));
        savedCard.setDisplayName((cardBeanBiz.getDisplayName()) + " "
                + EPayMethod.getPayMethodByMethod(cardBeanBiz.getCardType()).getNewDisplayName());

        savedCard.setBankName(cardBeanBiz.getDisplayName());
        savedCard.setCardType(EPayMethod.getPayMethodByMethod(cardBeanBiz.getCardType()).getNewDisplayName());
        savedCard.getCardDetails().setZeroSuccessRate(cardBeanBiz.getZeroSuccessRate());
        savedCard.getCardDetails().setInstName(cardBeanBiz.getInstName());
        savedCard.getCardDetails().setCountryCode(cardBeanBiz.getCountryCode());
        savedCard.getCardDetails().setCountry(cardBeanBiz.getCountry());
        savedCard.getCardDetails().setCountryCodeIso(cardBeanBiz.getCountryCodeIso());
        savedCard.getCardDetails().setCurrency(cardBeanBiz.getCurrency());
        savedCard.getCardDetails().setCurrencyCode(cardBeanBiz.getCurrencyCode());
        savedCard.getCardDetails().setCurrencyCodeIso(cardBeanBiz.getCurrencyCodeIso());
        savedCard.getCardDetails().setCountryCode(cardBeanBiz.getCountryCode());
        savedCard.getCardDetails().setSymbol(cardBeanBiz.getSymbol());
        savedCard.getCardDetails().setCurrencyPrecision(cardBeanBiz.getCurrencyPrecision());
        savedCard.getCardDetails().setCategory(cardBeanBiz.getCategory());
        savedCard.getCardDetails().setCardScheme(cardBeanBiz.getCardScheme());
        savedCard.setPreAuthDetails(bankCard.getPreAuthDetails());

        savedCard.setCardCoft(cardBeanBiz.isCardCoft());
        savedCard.setEligibleForCoft(cardBeanBiz.isEligibleForCoft());
        savedCard.setCoftPaymentSupported(cardBeanBiz.isCoftPaymentSupported());
        savedCard.setPar(cardBeanBiz.getPar());
        savedCard.setTokenStatus(cardBeanBiz.getTokenStatus());
        savedCard.setFingerPrint(cardBeanBiz.getFingerPrint());
        savedCard.setGcin(cardBeanBiz.getGcin());
        savedCard.setAccountRangeCardBin(cardBeanBiz.getAccountRangeCardBin());
        savedCard.setRemainingLimit(cardBeanBiz.getRemainingLimit());
        return savedCard;
    }

    private boolean isCoftCardEmiEligible(CardBeanBiz cardBeanBiz) {
        String cardType = cardBeanBiz.getCardType();
        if (CardType.DEBIT_CARD.getValue().equalsIgnoreCase(cardType)) {
            String coftEmiEligibleIssuersDCString = ff4jUtils.getPropertyAsStringWithDefault(
                    COFT_EMI_ELIGIBLE_ISSUERS_DC, StringUtils.EMPTY);
            Set<String> coftEmiEligibleIssuersDC = new HashSet<>(Arrays.asList(coftEmiEligibleIssuersDCString
                    .split(",")));
            return coftEmiEligibleIssuersDC.contains(cardBeanBiz.getInstId());
        } else if (CardType.CREDIT_CARD.getValue().equalsIgnoreCase(cardType)) {
            String coftEmiEligibleIssuersCCString = ff4jUtils.getPropertyAsStringWithDefault(
                    COFT_EMI_ELIGIBLE_ISSUERS_CC, StringUtils.EMPTY);
            Set<String> coftEmiEligibleIssuersCC = new HashSet<>(Arrays.asList(coftEmiEligibleIssuersCCString
                    .split(",")));
            return coftEmiEligibleIssuersCC.contains(cardBeanBiz.getInstId());
        }
        return false;
    }

    private void setEventLogForOneClickSupported(SavedCard savedCard) {

        Map<String, String> metaData = new HashMap<>();

        metaData.put("Message", "oneClickSupported:" + savedCard.getOneClickSupported());
        if (null != savedCard.getCardDetails()) {
            metaData.put("BIN", savedCard.getCardDetails().getFirstSixDigit());
        }
        EventUtils.pushTheiaEvents(EventNameEnum.ONE_CLICK_SUPPORTED, metaData);
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

    private EmiChannel getEmiChannel(SavedCard savedCard, List<PayMethod> payMethods, EmiType type) {
        PayMethod payMethod = payMethods.stream().filter(s -> s.getPayMethod().equals(EPayMethod.EMI.getMethod()))
                .findAny().orElse(null);
        if (payMethod != null && !Boolean.valueOf(payMethod.getIsDisabled().getStatus())) {
            EmiChannel emiChannel = payMethod.getPayChannelOptions().stream().map(s -> (EmiChannel) s)
                    .filter(s -> s.getInstId().equals(savedCard.getIssuingBank()) && type.equals(s.getEmiType()))
                    .findAny().orElse(null);
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

    private List<PayMethod> getPayMethods(Money orderAmount, List<PayMethodViewsBiz> payMethodViewsBizs,
            EChannelId eChannelId, UserDetailsBiz userDetails, EPayMode paymentFlow,
            WorkFlowResponseBean workFlowResponseBean, MandateMode mandateMode, boolean isAddMoneyPaymodes,
            boolean isPrepaidCardFeatureEnabled, SubscriptionTransactionRequestBody subscriptionTransactionRequestBody,
            String mobileNo) {
        List<PayMethod> payMethods = new ArrayList<>();
        if (payMethodViewsBizs == null)
            return payMethods;

        SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {
            EPayMethod payMethodEnum = PayMethodUtility.getPayMethodByMethod(payMethodViewsBiz.getPayMethod());

            // Removing PPBL as a paymode for product code
            if (StringUtils.equals(payMethodEnum.getMethod(), EPayMethod.RENEW_PPBL.getMethod())
                    && (workFlowResponseBean != null && workFlowResponseBean.getExtendedInfo() != null && !StringUtils
                            .equals(workFlowResponseBean.getExtendedInfo().get("productCode"),
                                    ProductCodes.RecurringAcquiringProd.getProductCode()))) {
                continue;
            }
            if (EPayMethod.BANK_MANDATE == payMethodEnum || EPayMethod.UPI == payMethodEnum) {
                if (subscriptionTransactionRequestBody != null
                        && susbcriptionNativeValidationService
                                .isTxnAmountGreaterThanMaxOrRenewalAmt(subscriptionTransactionRequestBody)) {
                    continue;
                }
                if (subscriptionTransactionRequestBody != null
                        && AmountType.getEnumByName(subscriptionTransactionRequestBody.getSubscriptionAmountType())
                                .equals(AmountType.FIX)
                        && !(EPayMethod.BANK_MANDATE == payMethodEnum && isTxnAmountZero(subscriptionTransactionRequestBody))
                        && susbcriptionNativeValidationService
                                .isTxnAmountLessThanMaxOrRenewalAmt(subscriptionTransactionRequestBody)) {
                    continue;
                }
            }
            PayMethod payMethod = new PayMethod();
            payMethod.setIsDisabled(new StatusInfo(Boolean.FALSE.toString(), ""));

            if (EPayMethod.MP_COD.equals(payMethodEnum)) {
                payMethod.setPayMethod(payMethodEnum.getOldName());
            } else {
                payMethod.setPayMethod(payMethodEnum.getMethod());
            }
            if (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodEnum.getMethod())) {
                LOGGER.info("Setting storeFront url for MGV");
                String storeFrontUrl = ConfigurationUtil.getProperty(
                        TheiaConstant.ExtraConstants.MERCHANT_STORE_FRONT_URL, "");
                String kybId = workFlowResponseBean.getExtendedInfo().get(
                        TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_KYB_ID);
                payMethod.setStoreFrontUrl(storeFrontUrl + "?kybid=" + kybId + "&isMGV=true");
                String merchDisplayName = getMerchantDisplayNameForMgv(workFlowResponseBean);
                if (StringUtils.isNotBlank(merchDisplayName)) {
                    // Please check getPayMethodByNewDisplayName() of EPayMethod
                    // enum in common
                    payMethod.setDisplayName(merchDisplayName + " Voucher");
                }

                payMethod.setNewUser(payMethodViewsBiz.getNewUser());

            }
            if (StringUtils.isBlank(payMethod.getDisplayName())) {
                payMethod.setDisplayName(payMethodEnum.getNewDisplayName());
            }
            payMethod.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(), ""));
            if (EPayMethod.BANK_MANDATE == payMethodEnum) {
                payMethod.setPayChannelOptions(populateMandateBanks(mandateMode, payMethodEnum));
            } else {
                payMethod.setPayChannelOptions(getPayChannelOptions(payMethod, orderAmount,
                        payMethodViewsBiz.getPayChannelOptionViews(), payMethodViewsBiz.getPayMethod(), eChannelId,
                        successRateCacheModel, userDetails, paymentFlow, workFlowResponseBean, isAddMoneyPaymodes,
                        isPrepaidCardFeatureEnabled, mobileNo));
            }
            payMethod.setOnboarding(PayMethodOnboardingUtil.getOnboarding(
                    EPayMethod.getPayMethodByMethod(payMethodViewsBiz.getPayMethod()), userDetails));
            payMethod.setRemainingLimit(payMethodViewsBiz.getRemainingLimit());
            payMethod.setPayOptionRemainingLimits(payMethodViewsBiz.getPayOptionRemainingLimits());
            payMethods.add(payMethod);
        }
        // TODO For UPI-LITE
        if (workFlowResponseBean.getUpiProfileV4() != null
                && workFlowResponseBean.getUpiProfileV4().getRespDetails() != null
                && workFlowResponseBean.getUpiProfileV4().getRespDetails().getProfileDetail() != null
                && workFlowResponseBean.getUpiProfileV4().getRespDetails().getProfileDetail().getLrnDetails() != null) {
            Optional<PayChannelBase> upiPush = payMethods.stream()
                    .filter(payMethod -> EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod()))
                    .map(PayMethod::getPayChannelOptions).flatMap(List::stream)
                    .filter(payChannelBase -> UPI_PUSH_EXPRESS.equals(((Bank) payChannelBase).getInstId())).findFirst();

            if (upiPush.isPresent()) {
                PayMethod payMethod = new PayMethod();
                payMethod.setPayMethod(EPayMethod.UPI_LITE.getMethod());
                payMethod.setDisplayName(workFlowResponseBean.getUpiProfileV4().getRespDetails().getProfileDetail()
                        .getLrnDetails().getTitle());
                payMethod.setIsDisabled(new StatusInfo(Boolean.FALSE.toString(), ""));
                payMethod.setPayChannelOptions(Collections.<PayChannelBase>emptyList());
                payMethod.setLrnDetails(workFlowResponseBean.getUpiProfileV4().getRespDetails().getProfileDetail()
                        .getLrnDetails());
                payMethods.add(payMethod);
            }
        }
        // clubbing emi and emi dc methods
        mergeEMIAndEMIDC(payMethods);
        return payMethods;
    }

    private boolean isTxnAmountZero(SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        if (subscriptionTransactionRequestBody.getTxnAmount() != null
                && StringUtils.isNotBlank(subscriptionTransactionRequestBody.getTxnAmount().getValue())) {
            if (Double.parseDouble(subscriptionTransactionRequestBody.getTxnAmount().getValue()) == 0) {
                return true;
            }
        }
        return false;
    }

    private String getMerchantDisplayNameForMgv(WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null) {
            String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
            String aggMid = null;
            if (StringUtils.isNotBlank(mid)) {
                aggMid = merchantDataUtil.getAggregatorMid(mid);
                return (StringUtils.isNotBlank(aggMid)) ? merchantExtendInfoUtils.getMerchantName(aggMid)
                        : merchantExtendInfoUtils.getMerchantName(mid);
            }
        }
        return null;
    }

    private List<PayChannelBase> getPayChannelOptions(PayMethod payMethod, Money orderAmount,
            List<PayChannelOptionViewBiz> payChannelOptionViewBizs, String payMethodStr, EChannelId eChannelId,
            SuccessRateCacheModel successRateCacheModel, UserDetailsBiz userDetailsBiz, EPayMode paymentFlow,
            WorkFlowResponseBean workFlowResponseBean, boolean isAddMoneyPaymodes, boolean isPrepaidCardFeatureEnabled,
            String mobileNo) {
        boolean prepaidCardSupportedForPayMethod = false;
        List<PayChannelBase> payChannelOptions = new ArrayList<>();
        if (payChannelOptionViewBizs == null)
            return payChannelOptions;

        boolean checkForAddAndPay = (!isAddMoneyPaymodes || !EPayMode.ADDANDPAY.equals(paymentFlow));
        for (PayChannelOptionViewBiz payChannelOptionViewBiz : payChannelOptionViewBizs) {
            PayChannelBase payChannelBase = null;
            StatusInfo isDisabled = new StatusInfo(String.valueOf(!payChannelOptionViewBiz.isEnableStatus()),
                    payChannelOptionViewBiz.getDisableReason());

            boolean isPrepaidCardSupported = (payChannelOptionViewBiz.isPrepaidCardChannel() && checkForAddAndPay);

            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethodStr)) {
                CreditCard creditCard = new CreditCard();
                creditCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                creditCard.setInstId(payChannelOptionViewBiz.getInstId());
                creditCard.setInstName(payChannelOptionViewBiz.getInstName());
                creditCard.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        creditCard.getInstId()));
                creditCard.setOneClickSupported(payChannelOptionViewBiz.getOneClickChannel());
                if (isPrepaidCardFeatureEnabled) {
                    creditCard.setPrepaidCardSupported(isPrepaidCardSupported);
                }
                // payMethod.setPrepaidCardSupported(false);
                prepaidCardSupportedForPayMethod = false;
                creditCard.setDccServiceInstIds(payChannelOptionViewBiz.getDccServiceInstIds());
                creditCard.setSupportedCardSubTypes(payChannelOptionViewBiz.getSupportPayOptionSubTypes());
                creditCard.setPreAuthDetails(getPreAuthDetails(payChannelOptionViewBiz));
                payChannelBase = creditCard;
            } else if (EPayMethod.DEBIT_CARD.getMethod().equals(payMethodStr)) {
                DebitCard debitCard = new DebitCard();
                debitCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                debitCard.setInstId(payChannelOptionViewBiz.getInstId());
                debitCard.setInstName(payChannelOptionViewBiz.getInstName());
                debitCard.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        debitCard.getInstId()));
                debitCard.setOneClickSupported(payChannelOptionViewBiz.getOneClickChannel());
                if (isPrepaidCardFeatureEnabled) {
                    debitCard.setPrepaidCardSupported(false);
                    if (isPrepaidCardSupported
                            && (orderAmount == null || orderAmount.getValue() == null || prepaidCardValidationUtil
                                    .isPrepaidCardLimitValid(orderAmount.getValue(), false))) {
                        debitCard.setPrepaidCardSupported(true);
                        prepaidCardSupportedForPayMethod = true;
                        workFlowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
                    }
                }
                debitCard.setSupportedCardSubTypes(payChannelOptionViewBiz.getSupportPayOptionSubTypes());
                debitCard.setDccServiceInstIds(payChannelOptionViewBiz.getDccServiceInstIds());
                debitCard.setPreAuthDetails(getPreAuthDetails(payChannelOptionViewBiz));
                payChannelBase = debitCard;
            } else if (EPayMethod.NET_BANKING.getMethod().equals(payMethodStr)) {
                NetBanking netBanking = new NetBanking();
                netBanking.setInstId(payChannelOptionViewBiz.getInstId());
                netBanking.setInstName(payChannelOptionViewBiz.getInstName());
                netBanking.setInstDispCode(payChannelOptionViewBiz.getInstDispCode());
                netBanking.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        netBanking.getInstId()));
                payChannelBase = netBanking;
            } else if (EPayMethod.UPI.getMethod().equals(payMethodStr)) {
                UPI upi = new UPI();
                upi.setInstId(payChannelOptionViewBiz.getInstId());
                upi.setInstName(payChannelOptionViewBiz.getInstName());
                upi.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        upi.getInstId()));
                payChannelBase = upi;
            } else if (EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod().equals(payMethodStr)) {
                AdvanceDepositAccount advanceDepositAccount = new AdvanceDepositAccount();
                AccountInfo accountInfo = getBalanceInfoFromExternalAccount(payChannelOptionViewBiz
                        .getExternalAccountInfos());
                advanceDepositAccount.setBalanceInfo(accountInfo);
                List<String> userTypes = null;
                boolean isAdvanceDepositUser = Boolean.FALSE;
                if (userDetailsBiz != null) {
                    userTypes = userDetailsBiz.getUserTypes();
                }
                if (userTypes != null && !userTypes.isEmpty()) {
                    for (String type : userTypes) {
                        if ("ADVANCE_DEPOSIT_USER".equals(type)) {
                            isAdvanceDepositUser = Boolean.TRUE;
                            break;
                        }
                    }
                }
                /**
                 * advance deposit Available is set by checking corporate CustId
                 * This is not marked at Oauth, So setting in manually and
                 * externally
                 */
                if (workFlowResponseBean.isAdvanceDepositAvailable()) {
                    isAdvanceDepositUser = Boolean.TRUE;
                }
                isDisabled = isDisabled(payMethodStr, payChannelOptionViewBiz.isEnableStatus(), isAdvanceDepositUser);
                payMethod.setIsDisabled(isDisabled);
                advanceDepositAccount.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(
                        payMethod.getPayMethod(), ""));

                payChannelBase = advanceDepositAccount;
            } else if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodStr)) {
                DigitalCredit digitalCredit = new DigitalCredit();
                AccountInfo accountInfo = getDigitalCreditAccountInfo(
                        payChannelOptionViewBiz.getExternalAccountInfos(), workFlowResponseBean);
                digitalCredit.setBalanceInfo(accountInfo);
                isDisabled = isDisabled(payMethodStr, payChannelOptionViewBiz.isEnableStatus(),
                        null != userDetailsBiz ? userDetailsBiz.isPaytmCCEnabled() : false);
                payMethod.setIsDisabled(isDisabled);
                digitalCredit.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(
                        payMethod.getPayMethod(), ""));

                payChannelBase = digitalCredit;
            } else if (EPayMethod.BALANCE.getMethod().equals(payMethodStr)) {
                PPI ppi = new PPI();
                AccountInfo accountInfo = getBalanceInfo(payChannelOptionViewBiz.getBalanceChannelInfos(),
                        workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID());
                mapSubWalletDetails(payChannelOptionViewBiz, accountInfo, workFlowResponseBean.getWorkFlowRequestBean()
                        .getPaytmMID());
                ppi.setBalanceInfo(accountInfo);
                isDisabled = isDisabled(payMethodStr, payChannelOptionViewBiz.isEnableStatus(),
                        null != accountInfo ? true : false);
                payMethod.setIsDisabled(isDisabled);
                ppi.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(), ""));
                ppi.setTwoFAConfig(getTwoFAConfigData(payChannelOptionViewBiz.getBalanceChannelInfos()));
                payChannelBase = ppi;
            } else if (EPayMethod.WALLET.getMethod().equals(payMethodStr)) {
                Wallet wallet = new Wallet();
                wallet.setInstId(payChannelOptionViewBiz.getInstId());
                wallet.setInstName("Paytm");
                wallet.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(), ""));
                payChannelBase = wallet;
            } else if (EPayMethod.EMI.getMethod().equals(payMethodStr)) {
                EmiChannel emiChannel = new EmiChannel();
                emiChannel.setEmiChannelInfos(getEmiChannelInfo(payChannelOptionViewBiz.getEmiChannelInfos(),
                        payChannelOptionViewBiz.getInstId(), orderAmount));
                /*
                 * emiChannel.setEmiHybridChannelInfos(
                 * getEmiChannelInfo(payChannelOptionViewBiz.
                 * getEmiHybridChannelInfos(),
                 * payChannelOptionViewBiz.getInstId()));
                 */
                getMinAndMaxAmount(emiChannel);
                payChannelBase = emiChannel;
                emiChannel.setInstName(payChannelOptionViewBiz.getInstName());
                emiChannel.setInstId(payChannelOptionViewBiz.getInstId());
                emiChannel.setInstDispCode(payChannelOptionViewBiz.getInstDispCode());
                prepaidCardSupportedForPayMethod = false;
                emiChannel.setEmiType(EmiType.CREDIT_CARD);
                // Adding supportedCard SubTypes
                emiChannel.setSupportedCardSubTypes(payChannelOptionViewBiz.getSupportPayOptionSubTypes());

                if (CollectionUtils.isEmpty(filterEmiWithAmount(orderAmount, emiChannel.getEmiChannelInfos(),
                        paymentFlow))) {
                    // If there are no plans under selected bank, remove that
                    // bank
                    payChannelBase = null;
                    isDisabled.setStatus(String.valueOf(Boolean.TRUE));
                }
                emiChannel.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        emiChannel.getInstId()));

            } else if (EPayMethod.EMI_DC.getMethod().equals(payMethodStr) && mobileNo != null) {
                /*
                 * filtering out paychannels (banks) for which user is not
                 * eligible for EMI on DC.
                 */
                if (!emiUtil.isUserEligibleforEmiOnDc(mobileNo, payChannelOptionViewBiz.getInstId())) {
                    continue;
                }
                EmiChannel emiChannel = new EmiChannel();
                emiChannel.setEmiChannelInfos(getEmiChannelInfo(payChannelOptionViewBiz.getEmiChannelInfos(),
                        payChannelOptionViewBiz.getInstId(), orderAmount));
                getMinAndMaxAmount(emiChannel);
                payChannelBase = emiChannel;
                emiChannel.setInstName(payChannelOptionViewBiz.getInstName());
                emiChannel.setInstId(payChannelOptionViewBiz.getInstId());
                emiChannel.setInstDispCode(payChannelOptionViewBiz.getInstDispCode());
                prepaidCardSupportedForPayMethod = false;
                emiChannel.setEmiType(EmiType.DEBIT_CARD);
                // Adding supportedCard SubTypes
                emiChannel.setSupportedCardSubTypes(payChannelOptionViewBiz.getSupportPayOptionSubTypes());
                if (CollectionUtils.isEmpty(filterEmiWithAmount(orderAmount, emiChannel.getEmiChannelInfos(),
                        paymentFlow))) {
                    // If there are no plans under selected bank, remove that
                    // bank
                    payChannelBase = null;
                    isDisabled.setStatus(String.valueOf(Boolean.TRUE));
                }
                emiChannel.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        emiChannel.getInstId()));
            } else if (EPayMethod.COD.getMethod().equals(payMethodStr)
                    || EPayMethod.MP_COD.getMethod().equals(payMethodStr)) {
                // For COD : Add one channel with minimum COD amount
                payChannelBase = getCODChannel();
                payChannelBase.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(
                        payMethod.getPayMethod(), ""));
            } else if (EPayMethod.GIFT_VOUCHER.getMethod().equals(payMethodStr)) {
                MerchantGiftVoucher mgv = new MerchantGiftVoucher();
                AccountInfo accountInfo = getMgvBalanceInfo(payChannelOptionViewBiz.getBalanceChannelInfos());
                mgv.setBalanceInfo(accountInfo);
                isDisabled = isDisabled(payMethodStr, payChannelOptionViewBiz.isEnableStatus(),
                        null != accountInfo ? true : false);
                payMethod.setIsDisabled(isDisabled);
                // In Phase 1 , hybrid is not allowed for MGV
                mgv.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(), ""));
                mgv.setTemplateId(payChannelOptionViewBiz.getTemplateId());
                payChannelBase = mgv;
            } else if (EPayMethod.PPBL.getOldName().equals(payMethodStr)) {
                NetBanking netBanking = new NetBanking();
                netBanking.setInstId(payChannelOptionViewBiz.getInstId());
                netBanking.setInstName(payChannelOptionViewBiz.getInstName());
                netBanking.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getPayMethod(),
                        netBanking.getInstId()));
                payChannelBase = netBanking;
            } else if (EPayMethod.BANK_TRANSFER.getMethod().equals(payMethodStr)) {
                BankTransfer bankTransfer = new BankTransfer();
                bankTransfer.setInstId(payChannelOptionViewBiz.getInstId());
                bankTransfer.setInstName(payChannelOptionViewBiz.getInstName());
                bankTransfer.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(
                        payMethod.getPayMethod(), bankTransfer.getInstId()));
                payChannelBase = bankTransfer;
            }
            if (payChannelBase == null)
                continue;
            boolean successRateFlag = getSuccessRateFlag(payMethodStr, successRateCacheModel, payChannelOptionViewBiz);
            // To display low success message with bank name for net_banking
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethodStr)) {
                if (Routes.PG2.getName().equalsIgnoreCase(
                        workFlowResponseBean.getMerchnatLiteViewResponse().getSourceSystem()))
                    payChannelBase.setHasLowSuccess(new StatusInfo(String.valueOf(payChannelOptionViewBiz
                            .isHasLowSuccessRate()), NativePaymentUtil.successRateMsg(
                            payChannelOptionViewBiz.isHasLowSuccessRate(), payChannelOptionViewBiz.getInstName())));
                else
                    payChannelBase.setHasLowSuccess(new StatusInfo(String.valueOf(successRateFlag), NativePaymentUtil
                            .successRateMsg(successRateFlag, payChannelOptionViewBiz.getInstName())));
            } else {
                payChannelBase.setHasLowSuccess(new StatusInfo(String.valueOf(successRateFlag), OfflinePaymentUtils
                        .successRateMsg(successRateFlag)));
            }
            if (workFlowResponseBean.getWorkFlowRequestBean().isReturnDisabledChannelInFpo()
                    && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                            payChannelOptionViewBiz.getDisableReason()) && isDisabled instanceof BalanceStatusInfo) {
                BalanceStatusInfo oldBalanceStatusInfo = (BalanceStatusInfo) isDisabled;
                BalanceStatusInfo newBalanceStatusInfo = new BalanceStatusInfo(
                        oldBalanceStatusInfo.getUserAccountExist(), oldBalanceStatusInfo.getUserAccountExist(),
                        oldBalanceStatusInfo.getStatus(), oldBalanceStatusInfo.getMsg());
                newBalanceStatusInfo.setDisplayMsg(ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getMessage());
                newBalanceStatusInfo.setShowDisabled(Boolean.TRUE);
                payChannelBase.setIsDisabled(newBalanceStatusInfo);
            } else {
                payChannelBase.setIsDisabled(isDisabled);
            }
            payChannelBase.setPayChannelOption(payChannelOptionViewBiz.getPayOption());
            payChannelBase.setPayMethod(payMethodStr);
            if (!EPayMethod.CREDIT_CARD.getMethod().equals(payMethodStr)
                    && !EPayMethod.DEBIT_CARD.getMethod().equals(payMethodStr)
                    && !EPayMethod.COD.getMethod().equals(payMethodStr)
                    && !EPayMethod.MP_COD.getMethod().equals(payMethodStr)) {

                if (!isEnhanceRequest(workFlowResponseBean) && isVersionAllowed(workFlowResponseBean)) {
                    payChannelBase.setIconUrl(commonFacade.getLogoNameV2(payChannelOptionViewBiz.getInstId()));
                } else if (CHECKOUT.equals(workFlowResponseBean.getWorkFlowRequestBean().getWorkFlow())) {
                    payChannelBase.setIconUrl(commonFacade.getLogoNameV2(payChannelOptionViewBiz.getInstId()));
                } else {
                    payChannelBase.setIconUrl(commonFacade.getLogoNameV1(payChannelOptionViewBiz.getInstId()));
                }
            }
            payChannelBase.setBankLogoUrl(commonFacade.getBankLogo(payChannelOptionViewBiz.getInstId()));
            payChannelBase.setDirectServiceInsts(payChannelOptionViewBiz.getDirectServiceInsts());
            payChannelBase.setSupportAtmPins(payChannelOptionViewBiz.getSupportAtmPins());
            payChannelOptions.add(payChannelBase);
        }
        if (isPrepaidCardFeatureEnabled) {
            setPrepaidCardForPayMethod(payMethod, payMethodStr, prepaidCardSupportedForPayMethod);
        }
        return payChannelOptions;
    }

    private AccountInfo getDigitalCreditAccountInfo(List<ExternalAccountInfoBiz> externalAccountInfos,
            WorkFlowResponseBean workFlowResponseBean) {
        DigitalCreditAccountInfo accountInfo = (DigitalCreditAccountInfo) getBalanceInfoFromExternalAccount(externalAccountInfos);
        PaytmDigitalCreditResponse digitalCreditResponse = workFlowResponseBean.getPaytmCCResponse();
        if (digitalCreditResponse == null || digitalCreditResponse.getStatusCode() != 0
                || CollectionUtils.isEmpty(digitalCreditResponse.getResponse())) {
            return accountInfo;
        }
        if (accountInfo != null) {
            CheckBalanceResponse checkBalanceResponse = digitalCreditResponse.getResponse().get(0);
            accountInfo.setDisplayMessage(checkBalanceResponse.getDisplayMessage());
            accountInfo.setInfoButtonMessage(checkBalanceResponse.getInfoButtonMessage());
            accountInfo.setAccountStatus(checkBalanceResponse.getAccountStatus());
            accountInfo.setMictLines(checkBalanceResponse.getMictLines());
            accountInfo.setPasscodeRequired(checkBalanceResponse.isPasscodeRequired());
            accountInfo.setFullTnCDetails(checkBalanceResponse.getFullTnCDetails());
            accountInfo.setKycVersion(checkBalanceResponse.getKycVersion());
            accountInfo.setKycCode(checkBalanceResponse.getKycCode());
            if (checkBalanceResponse.getMonthlySanctionLimit() != null) {
                accountInfo.setMonthlySanctionLimit(new Money(com.paytm.pgplus.enums.EnumCurrency.INR, String
                        .valueOf(checkBalanceResponse.getMonthlySanctionLimit())));
            }
            if (checkBalanceResponse.getMonthlyAvailableSanctionLimit() != null) {
                accountInfo.setMonthlyAvailableSanctionLimit(new Money(com.paytm.pgplus.enums.EnumCurrency.INR, String
                        .valueOf(checkBalanceResponse.getMonthlyAvailableSanctionLimit())));
            }
            if (StringUtils
                    .equals(checkBalanceResponse.getAccountStatus(),
                            com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_ACTIVE)
                    || StringUtils
                            .equals(checkBalanceResponse.getAccountStatus(),
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_NOT_ACTIVE)) {
                accountInfo.setEnable(true);
            } else {
                accountInfo.setEnable(false);
                if (StringUtils.isBlank(checkBalanceResponse.getInfoButtonMessage())) {
                    accountInfo
                            .setInfoButtonMessage(com.paytm.pgplus.theia.utils.ConfigurationUtil
                                    .getProperty(
                                            com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.DEFAULT_INFO_BUTTON_MESSAGE,
                                            "We are facing some issue with postpaid, please use other payment options"));
                }
            }
        }
        return accountInfo;
    }

    private void setPrepaidCardForPayMethod(PayMethod payMethod, String payMethodStr,
            boolean prepaidCardSupportedForPayMethod) {
        switch (payMethodStr) {
        case CREDIT_CARD:
        case EMI_DC:
        case EMI:
            payMethod.setPrepaidCardSupported(prepaidCardSupportedForPayMethod);
            break;
        case DEBIT_CARD:
            payMethod.setPrepaidCardSupported(prepaidCardSupportedForPayMethod);
            break;
        default:
            return;
        }

    }

    private boolean isBankTransferDisabled(WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null
                && StringUtils.isNotBlank(workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID())) {
            String mid = workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID();
            String bankTransferCheckoutFlow = merchantPreferenceService.getBankTransferCheckoutFlow(mid);
            if (StringUtils.isBlank(bankTransferCheckoutFlow)
                    || BankTransferCheckoutFlow.DISABLED.getValue().equals(bankTransferCheckoutFlow)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnhanceRequest(WorkFlowResponseBean workFlowResponseBean) {
        return (workFlowResponseBean != null)
                && ((workFlowResponseBean.getWorkFlowRequestBean() != null && workFlowResponseBean
                        .getWorkFlowRequestBean().isEnhancedCashierPageRequest()) || ((workFlowResponseBean
                        .getExtendedInfo() != null) && (StringUtils.equalsIgnoreCase("true", workFlowResponseBean
                        .getExtendedInfo().get("isEnhancedNative")))));
    }

    private boolean isVersionAllowed(WorkFlowResponseBean workFlowResponseBean) {
        String allowedVersions[] = com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS).split(
                ",");
        return (workFlowResponseBean != null)
                && Arrays.stream(allowedVersions).anyMatch(
                        n -> StringUtils.equalsIgnoreCase(workFlowResponseBean.getApiVersion(), n));

    }

    private void mapSubWalletDetails(PayChannelOptionViewBiz payChannelOptionViewBiz, AccountInfo accountInfo,
            String mid) {
        if (accountInfo != null && payChannelOptionViewBiz != null
                && MapUtils.isNotEmpty(payChannelOptionViewBiz.getExtendInfo())) {
            try {
                List<SubWalletDetailsList> subWalletDetails = OBJECT_MAPPER.readValue(payChannelOptionViewBiz
                        .getExtendInfo().get("subWalletDetailsList"), OBJECT_MAPPER.getTypeFactory()
                        .constructCollectionType(List.class, SubWalletDetailsList.class));
                List<SubWalletDetailsList> finalSubWalletDetails = new ArrayList<>();
                String totalBalance = payChannelOptionViewBiz.getExtendInfo().get("totalBalance");
                if (merchantPreferenceService.isSubWalletSegregationEnabled(mid)) {
                    for (SubWalletDetailsList subWalletDetail : subWalletDetails) {
                        if (subWalletDetail.getStatus() == TheiaConstant.Status.ACTIVE) {
                            finalSubWalletDetails.add(subWalletDetail);
                        }
                    }
                    accountInfo.setSubWalletDetails(mapToResponseObjectV2(finalSubWalletDetails));
                } else {
                    for (SubWalletDetailsList subWalletDetail : subWalletDetails) {
                        if ((subWalletDetail.getSubWalletType() == TheiaConstant.SubWalletType.PAYTM_BALANCE | subWalletDetail
                                .getSubWalletType() == TheiaConstant.SubWalletType.GIFT_VOUCHER)
                                && subWalletDetail.getStatus() == TheiaConstant.Status.ACTIVE) {
                            finalSubWalletDetails.add(subWalletDetail);
                        }
                    }
                    accountInfo.setSubWalletDetails(mapToResponseObject(finalSubWalletDetails, totalBalance));
                }

            } catch (Exception e) {
                LOGGER.warn("Exception occured while mapping subWalletDetails: {}", e);
            }
        }

    }

    private List<SubWalletDetails> mapToResponseObjectV2(List<SubWalletDetailsList> filteredSubWalletDetails) {

        if (CollectionUtils.isNotEmpty(filteredSubWalletDetails)) {
            List<SubWalletDetails> subWalletDetails = new ArrayList<>();
            for (SubWalletDetailsList subWalletDetailsList : filteredSubWalletDetails) {
                SubWalletDetails subWalletDetail = new SubWalletDetails();
                subWalletDetail.setBalance(changeAmtToRupee(Double.toString(subWalletDetailsList.getBalance() * 100D)));
                subWalletDetail.setDisplayName(subWalletDetailsList.getDisplayName());
                subWalletDetail.setImageUrl(subWalletDetailsList.getImageUrl());
                subWalletDetails.add(subWalletDetail);
            }
            return subWalletDetails;
        }
        return null;
    }

    private List<SubWalletDetails> mapToResponseObject(List<SubWalletDetailsList> filteredSubWalletDetails,
            String totalBalance) {
        if (CollectionUtils.isNotEmpty(filteredSubWalletDetails)) {
            Optional<SubWalletDetailsList> optionalSubWalletDetailsList = filteredSubWalletDetails.stream()
                    .filter(subWallet -> subWallet.getSubWalletType() == TheiaConstant.SubWalletType.GIFT_VOUCHER)
                    .findAny();
            if (optionalSubWalletDetailsList.isPresent()) {
                double gvBalance = optionalSubWalletDetailsList.get().getBalance();
                double effectivePaytmBalance = Double.parseDouble(totalBalance) - gvBalance;
                List<SubWalletDetails> subWalletDetails = new ArrayList<>();
                for (SubWalletDetailsList subWalletDetailsList : filteredSubWalletDetails) {
                    SubWalletDetails subWalletDetail = new SubWalletDetails();
                    if (subWalletDetailsList.getSubWalletType() == TheiaConstant.SubWalletType.PAYTM_BALANCE) {
                        subWalletDetail.setBalance(changeAmtToRupee(Double.toString(effectivePaytmBalance * 100D)));
                    } else {
                        subWalletDetail
                                .setBalance(changeAmtToRupee(Double.toString(subWalletDetailsList.getBalance() * 100D)));
                    }
                    subWalletDetail.setDisplayName(subWalletDetailsList.getDisplayName());
                    subWalletDetail.setImageUrl(subWalletDetailsList.getImageUrl());
                    subWalletDetails.add(subWalletDetail);
                }
                return subWalletDetails;
            }
        }
        return null;
    }

    private StatusInfo isDisabled(String payMethod, boolean isEnableStatus, boolean userAccountExist) {
        StatusInfo isDisabled;
        boolean merchantAccept = isEnableStatus ? true : enableMerchantAcceptFlag(payMethod);
        boolean isDisabledStatus = !(userAccountExist && merchantAccept);
        isDisabled = new BalanceStatusInfo(String.valueOf(userAccountExist), String.valueOf(merchantAccept),
                String.valueOf(isDisabledStatus), !isDisabledStatus ? null : merchantAccept ? "Please create account"
                        : "Merchant is not accepting payment through " + payMethod);
        return isDisabled;
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

    private TwoFARespData getTwoFAConfigData(List<BalanceChannelInfoBiz> balanceChannelInfoBizs) {
        if (CollectionUtils.isNotEmpty(balanceChannelInfoBizs))
            return balanceChannelInfoBizs.get(0).getTwoFAConfig();
        return null;
    }

    private AccountInfo getBalanceInfo(List<BalanceChannelInfoBiz> balanceChannelInfoBizs, String mid) {
        // TODO:check
        if (balanceChannelInfoBizs == null || balanceChannelInfoBizs.isEmpty() || balanceChannelInfoBizs.get(0) == null)
            return null;
        if (StringUtils.isEmpty(balanceChannelInfoBizs.get(0).getPayerAccountNo())
                && BooleanUtils.isFalse(ff4jUtils.isFeatureEnabledOnMid(mid,
                        THEIA_DISABLE_PAYER_ACCOUNT_NO_CHECK_ON_WALLET, false)))
            return null;
        AccountInfo balanceInfo = new AccountInfo(balanceChannelInfoBizs.get(0).getPayerAccountNo(), new Money(
                changeAmtToRupee(balanceChannelInfoBizs.get(0).getAccountBalance())));
        return balanceInfo;
    }

    private List<EMIChannelInfo> getEmiChannelInfo(List<EMIChannelInfoBiz> emiChannelInfos, String instId,
            Money orderAmount) {
        List<EMIChannelInfo> result = new ArrayList<>();
        for (EMIChannelInfoBiz emiChannelInfo : emiChannelInfos) {
            EMIChannelInfo channelInfo = new EMIChannelInfo(instId + "|" + emiChannelInfo.getOfMonths(),
                    emiChannelInfo.getTenureId(), emiChannelInfo.getInterestRate(), emiChannelInfo.getOfMonths(),
                    new Money(changeAmtToRupee(emiChannelInfo.getMinAmount())), new Money(
                            changeAmtToRupee(emiChannelInfo.getMaxAmount())), CardAcquiringMode.valueOf(emiChannelInfo
                            .getCardAcquiringMode()), emiChannelInfo.getPerInstallment(), calculateEmiAmount(
                            orderAmount, emiChannelInfo, instId));
            if (channelInfo.getEmiAmount() != null && StringUtils.isNotBlank(channelInfo.getEmiAmount().getValue())) {
                channelInfo.setTotalAmount(new Money(emiBinValidationUtil.calculateTotalAmount(
                        channelInfo.getEmiAmount().getValue(), emiChannelInfo.getOfMonths()).toString()));
            }
            result.add(channelInfo);
        }
        return result;
    }

    private Money calculateEmiAmount(Money orderAmount, EMIChannelInfoBiz emiChannelInfo, String instId) {
        if (orderAmount != null && StringUtils.isNotBlank(orderAmount.getValue())) {
            return new Money(emiBinValidationUtil.calculateEmiAmount(orderAmount.getValue(),
                    emiChannelInfo.getInterestRate(), emiChannelInfo.getOfMonths(), instId).toString());
        }
        return null;
    }

    private List<EMIChannelInfo> filterEmiWithAmount(Money orderAmount, List<EMIChannelInfo> emiChannelInfos,
            EPayMode paymentFlow) {
        if (orderAmount == null || StringUtils.isBlank(orderAmount.getValue())) {
            return emiChannelInfos;
        }
        List<EMIChannelInfo> result = new ArrayList<>();
        Iterator iterator = emiChannelInfos.iterator();
        while (iterator.hasNext()) {
            EMIChannelInfo emiChannelInfo = (EMIChannelInfo) iterator.next();
            if (Double.valueOf(orderAmount.getValue()) >= Double.valueOf(emiChannelInfo.getMinAmount().getValue())
                    && (!EPayMode.NONE.equals(paymentFlow) || Double.valueOf(orderAmount.getValue()) <= Double
                            .valueOf(emiChannelInfo.getMaxAmount().getValue()))) {
                result.add(emiChannelInfo);
                continue;
            }
            iterator.remove();
        }
        return result;
    }

    private AccountInfo getBalanceInfoFromExternalAccount(List<ExternalAccountInfoBiz> externalAccountInfoBizs) {
        if (externalAccountInfoBizs == null || externalAccountInfoBizs.isEmpty()
                || null == externalAccountInfoBizs.get(0))
            return null;

        if (StringUtils.isEmpty(externalAccountInfoBizs.get(0).getExternalAccountNo()))
            return null;
        AccountInfo balanceInfo = new DigitalCreditAccountInfo(externalAccountInfoBizs.get(0).getExternalAccountNo(),
                new Money(changeAmtToRupee(externalAccountInfoBizs.get(0).getAccountBalance())),
                externalAccountInfoBizs.get(0).getExtendInfo());
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

    private BalanceChannel balanceChannel(PayMethod payMethod, PayChannelBase payChannelBase, AccountInfo accountInfo,
            UserDetailsBiz userDetailsBiz) {
        if (payChannelBase == null)
            return new BalanceChannel();
        StatusInfo isDisabled = isDisabled(payMethod.getPayMethod(), !Boolean.valueOf(payChannelBase.getIsDisabled()
                .getStatus()), null == userDetailsBiz ? false : userDetailsBiz.isSavingsAccountRegistered());
        payMethod.setIsDisabled(isDisabled);
        BalanceChannel balanceChannel = new BalanceChannel(payChannelBase.getPayMethod(),
                payChannelBase.getPayChannelOption(), isDisabled, payChannelBase.getHasLowSuccess(),
                payChannelBase.getIconUrl(), accountInfo);
        balanceChannel.setBankLogoUrl(payChannelBase.getBankLogoUrl());
        return balanceChannel;
    }

    private String changeAmtToRupee(String amountInPaise) {
        if (StringUtils.isNotBlank(amountInPaise)) {
            String amtStr = AmountUtils.getTransactionAmountInRupee(amountInPaise);

            if (StringUtils.isNotBlank(amtStr) && amtStr.startsWith(".")) {
                amtStr = "0" + amtStr;
            }
            return amtStr;
        }
        return amountInPaise;
    }

    private EmiChannel getMinAndMaxAmount(EmiChannel emiChannel) {
        List<EMIChannelInfo> emiChannelInfos = emiChannel.getEmiChannelInfos();
        if (emiChannelInfos != null && !emiChannelInfos.isEmpty()) {
            double minValue = Double.MAX_VALUE;
            double maxValue = Double.MIN_VALUE;
            for (EMIChannelInfo emiChannelInfo : emiChannelInfos) {
                if (minValue > Double.valueOf(emiChannelInfo.getMinAmount().getValue())) {
                    minValue = Double.valueOf(emiChannelInfo.getMinAmount().getValue());
                }
                if (maxValue < Double.valueOf(emiChannelInfo.getMaxAmount().getValue())) {
                    maxValue = Double.valueOf(emiChannelInfo.getMaxAmount().getValue());
                }
            }

            emiChannel.setMaxAmount(new Money(String.valueOf(maxValue)));
            emiChannel.setMinAmount(new Money(String.valueOf(minValue)));
        }
        return emiChannel;
    }

    private CODChannel getCODChannel() {

        String codMinAmount = nativeCodUtils.getMinimumCodAmount();
        if (codMinAmount != null) {
            return new CODChannel(EPayMethod.COD.getMethod(), EPayMethod.COD.getMethod(), null, null, null, new Money(
                    codMinAmount));
        }

        return null;
    }

    private void mergeEMIAndEMIDC(List<PayMethod> payMethods) {
        PayMethod emiDC = payMethods.stream()
                .filter(payMethod -> EPayMethod.EMI_DC.getMethod().equals(payMethod.getPayMethod())).findAny()
                .orElse(null);
        PayMethod emi = payMethods.stream()
                .filter(payMethod -> EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())).findAny()
                .orElse(null);
        if (emiDC == null) {
            return;
        }

        if (CollectionUtils.isEmpty(emiDC.getPayChannelOptions())) {
            removeEMIDC(payMethods);
            return;
        }

        if (emi == null) {
            emiDC.setPayMethod(EPayMethod.EMI.getMethod());
            emiDC.setDisplayName(EPayMethod.EMI.getNewDisplayName());
            return;
        }
        emi.getPayChannelOptions().addAll(emiDC.getPayChannelOptions());
        removeEMIDC(payMethods);

    }

    private void removeEMIDC(List<PayMethod> payMethods) {
        Iterator iterator = payMethods.iterator();
        while (iterator.hasNext()) {
            PayMethod payMethod = (PayMethod) iterator.next();
            if (EPayMethod.EMI_DC.getMethod().equals(payMethod.getPayMethod())) {
                iterator.remove();
                break;
            }
        }
    }

    private AccountInfo getMgvBalanceInfo(List<BalanceChannelInfoBiz> balanceChannelInfoBizs) {
        // TODO:check
        if (balanceChannelInfoBizs == null || balanceChannelInfoBizs.isEmpty() || balanceChannelInfoBizs.get(0) == null)
            return null;
        AccountInfo balanceInfo = new AccountInfo(balanceChannelInfoBizs.get(0).getPayerAccountNo(), new Money(
                changeAmtToRupee(balanceChannelInfoBizs.get(0).getAccountBalance())));
        return balanceInfo;
    }

    private List<PayChannelBase> populateMandateBanks(MandateMode mandateMode, EPayMethod payMethod) {
        List<PayChannelBase> payOptionChannels = new ArrayList<>();

        List<BankMasterDetails> mandateBanks = null;
        try {
            mandateBanks = workFlowHelper.getMandateBanks(mandateMode);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Unable to get mandate bank list", e);
            return payOptionChannels;
        }

        // TODO : verify values instId, instName
        if (CollectionUtils.isNotEmpty(mandateBanks)) {
            SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

            // Sort Banks on the basis of DISPLAY_ORDER column from DB
            mandateBanks.sort(Comparator.comparingLong(BankMasterDetails::getDisplayOrder));

            for (BankMasterDetails mandatebank : mandateBanks) {
                BankMandate mandate = new BankMandate();
                boolean successRateFlag = commonFacade.hasLowSuccessRate(mandatebank.getBankCode(),
                        EPayMethod.BANK_MANDATE.getMethod(), successRateCacheModel);
                List<MandateAuthMode> mandateAuthModes = getMandateAuthModes(mandatebank);

                mandate.setMandateMode(MandateMode.getByMappingName(mandatebank.getBankMandate()));
                // standard bank codes are the ones which are at npci end as
                // well
                mandate.setMandateBankCode(mandatebank.getStandardBankCode());
                mandate.setMandateAuthMode(CollectionUtils.isNotEmpty(mandateAuthModes) ? mandateAuthModes : null);
                mandate.setInstId(mandatebank.getBankCode());
                mandate.setInstName(mandatebank.getBankDisplayName());
                mandate.setInstDispCode(mandatebank.getBankShortName());
                mandate.setHybridDisabled(hybridDisablingUtil.isHybridDisabledForPayMethod(payMethod.getMethod(),
                        mandatebank.getBankCode()));
                mandate.setHasLowSuccess(new StatusInfo(String.valueOf(successRateFlag), NativePaymentUtil
                        .successRateMsg(successRateFlag, mandatebank.getBankName())));
                String bankDisabledMsg = !mandatebank.isStatus() ? "Bank is disabled" : "";
                mandate.setIsDisabled(new StatusInfo(String.valueOf(!mandatebank.isStatus()), bankDisabledMsg));
                mandate.setPayChannelOption(mandatebank.getBankCode());
                mandate.setPayMethod(payMethod.getMethod());
                mandate.setIconUrl(commonFacade.getLogoNameV1(mandatebank.getBankCode()));
                mandate.setBankLogoUrl(commonFacade.getBankLogo(mandatebank.getBankCode()));
                payOptionChannels.add(mandate);
            }
        }
        return payOptionChannels;
    }

    private List<MandateAuthMode> getMandateAuthModes(BankMasterDetails mandatebank) {
        List<MandateAuthMode> mandateAuthModes = new ArrayList<>();
        if (mandatebank.isMandateDebitCard()) {
            mandateAuthModes.add(MandateAuthMode.DEBIT_CARD);
        }

        if (mandatebank.isMandateNetBanking()) {
            mandateAuthModes.add(MandateAuthMode.NET_BANKING);
        }

        return mandateAuthModes;
    }

    boolean validPayChannelBaseForSavedCard(CardBeanBiz cardBeanBiz, BankCard bankCard,
            CashierInfoRequestBody cashierInfoRequestBody, boolean addnPay, WorkFlowResponseBean flowResponseBean,
            boolean isPrepaidFeatureEnabled) {

        boolean validSavedPrepaidCard = false;

        if (!cardBeanBiz.isPrepaidCard() || !isPrepaidFeatureEnabled)
            return true;

        if (addnPay)
            return false;
        if (BooleanUtils.isTrue(bankCard.isPrepaidCardSupported())
                && (cashierInfoRequestBody.getOrderAmount() == null || prepaidCardValidationUtil
                        .isPrepaidCardLimitValid(cashierInfoRequestBody.getOrderAmount().getValue(), false))) {
            validSavedPrepaidCard = true;
            flowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
        }
        return validSavedPrepaidCard;

    }

    private List<SavedMandateBank> getSavedMandateBanks(List<PayMethod> payMethods,
            UserProfileSarvatraV4 userProfileSarvatraV4) {

        LOGGER.info("Setting saved mandate banks in FPO response");

        List<SavedMandateBank> savedMandateBanks = new ArrayList<>();
        List<PayChannelBase> payOptionChannels = new ArrayList<>();

        Map<String, BankMandate> bankCodeMapping = new HashMap<>();

        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.BANK_MANDATE.getMethod().equals(payMethod.getPayMethod())) {
                payOptionChannels = payMethod.getPayChannelOptions();
            }
        }

        Iterator<PayChannelBase> bankMandateIterator = payOptionChannels.iterator();
        while (bankMandateIterator.hasNext()) {
            BankMandate bankMandate = (BankMandate) bankMandateIterator.next();
            bankCodeMapping.put(bankMandate.getInstId(), bankMandate);
        }

        if (MapUtils.isNotEmpty(bankCodeMapping)) {

            List<UpiBankAccountV4> upiBankAccountV4List = userProfileSarvatraV4.getRespDetails().getProfileDetail()
                    .getBankAccounts();

            Iterator<UpiBankAccountV4> upiBankAccountV4Iterator = upiBankAccountV4List.iterator();

            while (upiBankAccountV4Iterator.hasNext()) {

                SavedMandateBank savedBank = new SavedMandateBank();

                UpiBankAccountV4 upiBankAccount = upiBankAccountV4Iterator.next();
                if (bankCodeMapping.containsKey(upiBankAccount.getPgBankCode())) {
                    BankMandate bankMandate = bankCodeMapping.get(upiBankAccount.getPgBankCode());
                    savedBank.setAccountHolderName(upiBankAccount.getName());
                    savedBank.setMaskedAccountNumber(upiBankAccount.getMaskedAccountNumber());
                    savedBank.setAccountType(upiBankAccount.getAccountType());
                    savedBank.setAccRefId(upiBankAccount.getAccRefId());
                    savedBank.setInstId(bankMandate.getInstId());
                    savedBank.setInstName(bankMandate.getInstName());
                    savedBank.setMandateAuthMode(bankMandate.getMandateAuthMode());
                    savedBank.setMandateBankCode(bankMandate.getMandateBankCode());
                    savedBank.setMandateMode(bankMandate.getMandateMode());
                    savedBank.setIfsc(upiBankAccount.getIfsc());
                    savedBank.setDisplayName(bankMandate.getInstId() + " - " + E_MANDATE);
                    savedBank.setIconUrl(bankMandate.getIconUrl());
                    savedBank.setBankLogoUrl(commonFacade.getBankLogo(savedBank.getInstId()));
                    savedMandateBanks.add(savedBank);
                }
            }
        }
        return savedMandateBanks;

    }

    private List<PreAuthDetails> getPreAuthDetails(PayChannelOptionViewBiz payChannelOptionViewBiz) {
        if (StringUtils.isBlank(payChannelOptionViewBiz.getPayConfirmFlowType())) {
            return null;
        }
        PreAuthDetails preAuthDetails = new PreAuthDetails();
        if (!payChannelOptionViewBiz.isEnableStatus()) {
            preAuthDetails.setIsDisabled(true);
        } else {
            preAuthDetails.setIsDisabled(false);
        }
        preAuthDetails.setPreAuthType(EPreAuthType.valueOf(payChannelOptionViewBiz.getPayConfirmFlowType()));
        preAuthDetails.setMaxBlockSeconds(Long.parseLong(payChannelOptionViewBiz.getBlockPeriodInSeconds()));
        preAuthDetails.setExpressCapturePercentage(payChannelOptionViewBiz.getExcessCapturePercentage());
        if (Objects.nonNull(payChannelOptionViewBiz.getMaxBlockAmount())) {
            String amount = AmountUtils.getTransactionAmountInRupee(payChannelOptionViewBiz.getMaxBlockAmount()
                    .getValue());
            preAuthDetails.setMaxBlockAmount(new Money(payChannelOptionViewBiz.getMaxBlockAmount().getCurrency(),
                    amount));
        }
        return Collections.singletonList(preAuthDetails);
    }

    private void populateUpiToAddNPayTxnDetails(NativeCashierInfoResponseBody responseBody) {
        try {
            String convertTxnToAddNPayOfferDetailsJson = ConfigurationUtil
                    .getProperty(BizConstant.CONVERT_TXN_TO_ADDNPAY_OFFER_DETAILS);
            ConvertToAddNPayOfferDetails convertToAddNPayOfferDetails = JsonMapper.mapJsonToObject(
                    convertTxnToAddNPayOfferDetailsJson, ConvertToAddNPayOfferDetails.class);
            responseBody.setConvertToAddNPayOfferDetails(convertToAddNPayOfferDetails);
        } catch (Exception ex) {
            LOGGER.error("Exception while setting Offer Context for UPI to ADDANDPAY Transaction {}", ex.getMessage());
        }
    }

    public boolean enableMerchantAcceptFlag(String payMethod) {
        if (ff4jUtils.isFeatureEnabled(SET_MERCHANT_ACCEPT_FOR_SUPPORTED_PAYMODE, false)) {
            return EPayMethod.WALLET.getMethod().equals(payMethod)
                    || EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethod)
                    || EPayMethod.BALANCE.getMethod().equals(payMethod);
        }
        return false;
    }
}