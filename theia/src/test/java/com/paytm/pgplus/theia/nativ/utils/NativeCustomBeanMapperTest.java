package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ExternalAccountInfoBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetailsV4;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.UpiProfileDetailV4;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.CardSchemeInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paytm.pgplus.common.enums.MandateMode.E_MANDATE;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NativeCustomBeanMapperTest {

    @Mock
    private ICommonFacade commonFacade;

    @Mock
    private ITheiaSessionDataService theiaSessionDataService;

    @Mock
    private SuccessRateUtils successRateUtils;

    @Mock
    private EmiUtil emiUtil;

    @Mock
    private HybridDisablingUtil hybridDisablingUtil;

    @Mock
    protected WorkFlowHelper workFlowHelper;

    @Mock
    protected PrepaidCardValidationUtil prepaidCardValidationUtil;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    FF4JUtil ff4JUtil;

    @Mock
    Ff4jUtils ff4jUtils;

    @Mock
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Mock
    private ISusbcriptionNativeValidationService susbcriptionNativeValidationService;

    @Mock
    private CardUtils cardUtils;

    @Mock
    EmiBinValidationUtil emiBinValidationUtil;

    @Mock
    NativeCODUtils nativeCodUtils;

    @Mock
    private MerchantDataUtil merchantDataUtil;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    @Mock
    private CorporateCardUtil corporateCardUtil;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    CashierInfoRequest cashierInfoRequest;

    @InjectMocks
    NativeCustomBeanMapper nativeCustomBeanMapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testgetCashierInfoResponse() throws MappingServiceClientException {
        when(iPgpFf4jClient.checkWithdefault(anyString(), anyMap(), anyBoolean())).thenReturn(true);
        when(successRateUtils.getSuccessRateCacheModel()).thenReturn(new SuccessRateCacheModel());
        when(susbcriptionNativeValidationService.isTxnAmountGreaterThanMaxOrRenewalAmt(any())).thenReturn(true);
        when(hybridDisablingUtil.isHybridDisabledForPayMethod(anyString(), anyString())).thenReturn(true);
        when(commonFacade.getLogoUrl(anyString(), any())).thenReturn("XYZ");
        when(cardUtils.getCardSchemeInfo(anyString())).thenReturn(new CardSchemeInfo());
        when(cardUtils.getCardSchemeInfo(anyString())).thenReturn(new CardSchemeInfo());
        when(corporateCardUtil.prepareCardSubTypeListFromCardBeanz(any())).thenReturn(new ArrayList<>());
        when(successRateUtils.getSuccessRateCacheModel()).thenReturn(new SuccessRateCacheModel());
        when(commonFacade.hasLowSuccessRate(anyString(), anyString(), any())).thenReturn(true);
        when(commonFacade.getLogoNameV1(anyString())).thenReturn("HDFC");
        when(nativeCodUtils.getMinimumCodAmount()).thenReturn("100");
        when(ff4jUtils.isFeatureEnabledOnMid(anyString(), anyString(), anyBoolean())).thenReturn(true);
        BankMasterDetails bankMasterDetails = new BankMasterDetails();
        bankMasterDetails.setBankMandate("E_MANDATE_BANKS");
        bankMasterDetails.setBankCode("HDFC");
        ArrayList<BankMasterDetails> bankMasterDetailsArrayList = new ArrayList<>();
        bankMasterDetailsArrayList.add(bankMasterDetails);
        when(workFlowHelper.getMandateBanks(any())).thenReturn(bankMasterDetailsArrayList);
        when(theiaSessionDataService.isDirectChannelEnabled(anyString(), anyString(), anySet(), anyBoolean(), anySet()))
                .thenReturn(true);
        when(hybridDisablingUtil.isHybridDisabledForPayMethod(anyString(), anyString())).thenReturn(false);
        WorkFlowResponseBean workFlowResponseBean = getWorkFlowResponseBean();
        CashierInfoRequest cashierInfoRequest = new CashierInfoRequest(new RequestHeader(new BaseHeader("M", "M", "M",
                "M")), new CashierInfoRequestBody());
        cashierInfoRequest.getBody().setOrderId("XYZ");
        cashierInfoRequest.getBody().setChannelId(EChannelId.APP);
        cashierInfoRequest.getBody().setMandateType(E_MANDATE);
        NativeCashierInfoResponse response = nativeCustomBeanMapper.getCashierInfoResponse(workFlowResponseBean,
                cashierInfoRequest);
        assertNotNull(response);
    }

    private List<PayMethodViewsBiz> getPayMethodViewBiz() {
        List<PayMethodViewsBiz> payMethodViewsBizs = new ArrayList<>();
        getPayMethodViewsBizs(payMethodViewsBizs, "CREDIT_CARD");
        getPayMethodViewsBizs(payMethodViewsBizs, "DEBIT_CARD");
        getPayMethodViewsBizs(payMethodViewsBizs, "NET_BANKING");
        getPayMethodViewsBizs(payMethodViewsBizs, "UPI");
        getPayMethodViewsBizs(payMethodViewsBizs, "PAYTM_DIGITAL_CREDIT");
        getPayMethodViewsBizs(payMethodViewsBizs, "BALANCE");
        getPayMethodViewsBizs(payMethodViewsBizs, "BANK_MANDATE");
        getPayMethodViewsBizs(payMethodViewsBizs, "WALLET");
        getPayMethodViewsBizs(payMethodViewsBizs, "COD");
        getPayMethodViewsBizs(payMethodViewsBizs, "GIFT_VOUCHER");
        getPayMethodViewsBizs(payMethodViewsBizs, "PPBL");
        return payMethodViewsBizs;
    }

    private void getPayMethodViewsBizs(List<PayMethodViewsBiz> payMethodViewsBizs, String payMethod) {
        PayMethodViewsBiz payMethodViewsBiz = new PayMethodViewsBiz();
        payMethodViewsBiz.setPayMethod(payMethod);
        List<PayChannelOptionViewBiz> payChannelOptionViewBizs = new ArrayList<>();
        PayChannelOptionViewBiz payChannelOptionViewBiz = new PayChannelOptionViewBiz();
        payChannelOptionViewBiz.setPayOption(payMethod.concat("_PPBL"));
        payChannelOptionViewBiz.setInstId("ABC");
        payChannelOptionViewBiz.setPrepaidCardChannel(true);
        if (payMethod.equals("PAYTM_DIGITAL_CREDIT")) {
            List<ExternalAccountInfoBiz> externalAccountInfoBizs = new ArrayList<>();
            externalAccountInfoBizs.add(new ExternalAccountInfoBiz());
            externalAccountInfoBizs.get(0).setExternalAccountNo("12345");
            externalAccountInfoBizs.get(0).setAccountBalance("5000");
            externalAccountInfoBizs.get(0).setExtendInfo("xyz");
            payChannelOptionViewBiz.setExternalAccountInfos(externalAccountInfoBizs);
        }
        if (payMethod.equals("DEBIT_CARD")) {
            List<String> supportCountries = new ArrayList<>();
            supportCountries.add("AI");
            payChannelOptionViewBiz.setSupportCountries(supportCountries);
        }
        if (payMethod.equals("GIFT_VOUCHER")) {
            payChannelOptionViewBiz.setTemplateId("XYZ");
        }
        List<String> DirectServiceInsts = new ArrayList<>();
        DirectServiceInsts.add("1234");
        payChannelOptionViewBiz.setDirectServiceInsts(DirectServiceInsts);
        List<String> supportAtmPins = new ArrayList<>();
        supportAtmPins.add("1234");
        payChannelOptionViewBiz.setSupportAtmPins(supportAtmPins);
        payMethodViewsBiz.setPayChannelOptionViews(payChannelOptionViewBizs);
        payChannelOptionViewBizs.add(payChannelOptionViewBiz);
        payMethodViewsBizs.add(payMethodViewsBiz);
    }

    private void getCardBeanBiz(List<CardBeanBiz> cardBeanBizs, String CardType) {
        CardBeanBiz cardBeanBiz = new CardBeanBiz();
        cardBeanBiz.setCardId(123456L);
        cardBeanBiz.setCardType(CardType);
        cardBeanBiz.setCardScheme("PPBL");
        cardBeanBiz.setInstId("ABC");
        cardBeanBiz.setFirstSixDigit(1234556L);
        cardBeanBizs.add(cardBeanBiz);

    }

    private WorkFlowResponseBean getWorkFlowResponseBean() {
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        List<PayMethodViewsBiz> payMethodViewsBizs = getPayMethodViewBiz();
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = new LitePayviewConsultResponseBizBean();
        litePayviewConsultResponseBizBean.setPayMethodViews(payMethodViewsBizs);
        workFlowResponseBean.setMerchnatLiteViewResponse(litePayviewConsultResponseBizBean);
        workFlowResponseBean.setAddAndPayLiteViewResponse(new LitePayviewConsultResponseBizBean());
        workFlowResponseBean.setAllowedPayMode(EPayMode.ADDANDPAY);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        List<CardBeanBiz> cardBeanBizs = new ArrayList<>();
        getCardBeanBiz(cardBeanBizs, "CREDIT_CARD");
        getCardBeanBiz(cardBeanBizs, "DEBIT_CARD");
        userDetailsBiz.setMerchantViewSavedCardsList(cardBeanBizs);
        userDetailsBiz.setMobileNo("8922072855");
        workFlowResponseBean.setUserDetails(userDetailsBiz);
        workFlowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
        workFlowResponseBean.setApiVersion("v2");
        workFlowResponseBean.setProductCode("102");
        workFlowResponseBean.setQrCodeDetails(new QRCodeDetailsResponse());
        workFlowResponseBean.setOnTheFlyKYCRequired(true);
        workFlowResponseBean.setRiskConvenienceFee(new ArrayList<>());
        HashMap<String, String> extendInfo = new HashMap<>();
        extendInfo.put("RecurringAcquiringProd", "51051000100000000004");
        workFlowResponseBean.setExtendedInfo(extendInfo);
        workFlowResponseBean.setWorkFlowRequestBean(new WorkFlowRequestBean());
        workFlowResponseBean.getWorkFlowRequestBean().setTxnAmount("1000");
        workFlowResponseBean.getWorkFlowRequestBean().setPaytmMID(BizConstant.MP_ADD_MONEY_MID);
        workFlowResponseBean.getWorkFlowRequestBean().setInternalFetchPaymentOptions(true);
        workFlowResponseBean.getWorkFlowRequestBean().setNativeAddMoney(true);
        workFlowResponseBean.getWorkFlowRequestBean().setPaymentTypeId(PaymentTypeIdEnum.NB.getValue());
        workFlowResponseBean.getWorkFlowRequestBean().setPayModeOnly("YES");
        UpiProfileDetailV4 upiProfileDetailV4 = new UpiProfileDetailV4();
        List<UpiBankAccountV4> bankAccountsV4s = new ArrayList<>();
        UpiBankAccountV4 bankAccountV4 = new UpiBankAccountV4();
        bankAccountV4.setPgBankCode("HDFC");
        bankAccountV4.setName("HDFC");
        bankAccountV4.setMaskedAccountNumber("123456");
        bankAccountV4.setAccountType("SAVING ACCOUNT");
        bankAccountV4.setAccRefId("CDCEWFC");
        bankAccountV4.setIfsc("1233HDFC");
        bankAccountsV4s.add(bankAccountV4);
        upiProfileDetailV4.setBankAccounts(bankAccountsV4s);
        PaytmVpaDetailsV4 respDetails = new PaytmVpaDetailsV4();
        respDetails.setProfileDetail(upiProfileDetailV4);
        UserProfileSarvatraV4 userProfileSarvatraV4 = new UserProfileSarvatraV4();
        userProfileSarvatraV4.setRespDetails(respDetails);
        workFlowResponseBean.setUpiProfileV4(userProfileSarvatraV4);
        return workFlowResponseBean;
    }

    /*
     * Case-1: enableMerchantAcceptFlag method of class NativeCustomBeanMapper
     * will return true when FF4J flag is enabled and payMethod is WALLET
     */

    @Test
    public void testEnableMerchantAcceptFlag_WhenFlagEnabledWithWalletPaymode() {
        when(ff4jUtils.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(true);
        boolean expected = nativeCustomBeanMapper.enableMerchantAcceptFlag("WALLET");
        assertTrue(expected);
    }

    /*
     * Case-1: enableMerchantAcceptFlag method of class NativeCustomBeanMapper
     * will return true when FF4J flag is enabled and payMethod is
     * PAYTM_DIGITAL_CREDIT
     */

    @Test
    public void testEnableMerchantAcceptFlag_WhenFlagEnabledWithPostpaidPaymode() {
        when(ff4jUtils.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(true);
        boolean expected = nativeCustomBeanMapper.enableMerchantAcceptFlag("PAYTM_DIGITAL_CREDIT");
        assertTrue(expected);
    }

    /*
     * Case-1: enableMerchantAcceptFlag method of class NativeCustomBeanMapper
     * will return true when FF4J flag is enabled and payMethod is BALANCE
     */

    @Test
    public void testEnableMerchantAcceptFlag_WhenFlagEnabledWithBALANCEPaymode() {
        when(ff4jUtils.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(true);
        boolean expected = nativeCustomBeanMapper.enableMerchantAcceptFlag("BALANCE");
        assertTrue(expected);
    }

    /*
     * Case-2: enableMerchantAcceptFlag method of class NativeCustomBeanMapper
     * will return false when FF4J flag is disabled
     */
    @Test
    public void testEnableMerchantAcceptFlag_WhenFlagDisabledAndExpectedPaymode() {
        when(ff4jUtils.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(false);
        boolean expected = nativeCustomBeanMapper.enableMerchantAcceptFlag("WALLET");
        assertFalse(expected);
    }

    /*
     * Case-3: enableMerchantAcceptFlag method of class NativeCustomBeanMapper
     * will return false when FF4J flag is enabled but payMethod is other than
     * WALLET, PAYTM_DIGITAL_CREDIT, BALANCE
     */
    @Test
    public void testEnableMerchantAcceptFlag_WhenFlagEnabledAndUnExpectedPaymode() {
        when(ff4jUtils.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(true);
        boolean expected = nativeCustomBeanMapper.enableMerchantAcceptFlag("BANK_EXPRESS");
        assertFalse(expected);
    }
}
