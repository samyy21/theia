package com.paytm.pgplus.theia.validator.service;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.checksum.crypto.EncryptionFactory;
import com.paytm.pgplus.checksum.crypto.IEncryption;
import com.paytm.pgplus.checksum.crypto.impl.AddMoneyExpressDecryption;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationServiceTest {

    @InjectMocks
    private ValidationService validationService = new ValidationService();

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private EmiBinValidationUtil emiBinValidationUtil;

    @Mock
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    private static final AddMoneyExpressDecryption ADD_MONEY_EXPRESS_DECRYPTION = AddMoneyExpressDecryption
            .getInstance();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testChecksumValidatation() throws SecurityException {
        PaymentRequestBean requestBean = new PaymentRequestBean();
        requestBean.setMerchantKey("merchantKey");
        requestBean.setChecksumhash("checksumHash");
        validationService.checksumValidatation(requestBean);
    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails() throws SecurityException {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.PPI.value);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.WALLET.value);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean1, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean2 = new WorkFlowRequestBean();
        workFlowRequestBean2.setPaymentTypeId(PaymentTypeIdEnum.COD.value);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean2, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean3 = new WorkFlowRequestBean();
        workFlowRequestBean3.setPaymentTypeId(EPayMethod.MP_COD.getMethod());
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean3, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean4 = new WorkFlowRequestBean();
        workFlowRequestBean4.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean4.setPaymentTypeId(PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.value);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean4, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean5 = new WorkFlowRequestBean();
        workFlowRequestBean5.setPaymentTypeId(EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod());
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean5, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean6 = new WorkFlowRequestBean();
        workFlowRequestBean6.setPaymentTypeId(PaymentTypeIdEnum.CC.value);
        workFlowRequestBean6.setRequestType(ERequestType.SEAMLESS);
        paymentRequestBean.setClientId("clientId");
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1234567891234567891");
                return encryption;
            }
        };
        when(merchantExtendInfoUtils.getMerchantKey(any(), any())).thenReturn("merchantKey");
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean6, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean7 = new WorkFlowRequestBean();
        workFlowRequestBean7.setPaymentTypeId(PaymentTypeIdEnum.IMPS.value);
        workFlowRequestBean7.setRequestType(ERequestType.SEAMLESS);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1|2");
                return encryption;
            }
        };
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean7, paymentRequestBean);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1| ");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean7, paymentRequestBean);

    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails0() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.IMPS.value);
        workFlowRequestBean.setRequestType(ERequestType.SEAMLESS);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1|2|3");
                return encryption;
            }
        };
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1|2| ");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);
    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails1() {
        WorkFlowRequestBean workFlowRequestBean8 = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean8.setRequestType(ERequestType.NATIVE_MF_SIP);
        workFlowRequestBean8.setEncryptedCardDetail(true);
        workFlowRequestBean8.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean8, paymentRequestBean);
    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails2() {
        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean1.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean1.setEncryptedCardDetail(true);
        workFlowRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1|2");
                return encryption;
            }
        };
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean1, paymentRequestBean);

        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean.setEncryptedCardDetail(true);
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        workFlowRequestBean.setiDebitEnabled("true");
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn(" 1| ");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);
    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails3() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean.setEncryptedCardDetail(true);
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn(" 1|2|102020");
                return encryption;
            }
        };
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);

        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn(" 1|2|1020");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);
    }

    @Test
    public void testValidateAndProcessSeamlessPaymentDetails4() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        workFlowRequestBean.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean.setEncryptedCardDetail(true);
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        workFlowRequestBean.setiDebitEnabled("false");
        new MockUp<EncryptionFactory>() {
            @mockit.Mock
            public IEncryption getEncryptionInstance(String algorithmType) throws SecurityException {
                IEncryption encryption = mock(IEncryption.class);
                when(encryption.decrypt(any())).thenReturn("MerchantKey");
                when(encryption.decrypt(any(), any())).thenReturn("1| | ");
                return encryption;
            }
        };
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessSeamlessPaymentDetails("encryptedPaymentDetais", "merchantId", true,
                workFlowRequestBean, paymentRequestBean);
    }

    @Test
    public void testValidatePaytmExpressData() {
        PaymentRequestBean requestData = new PaymentRequestBean();
        requestData.setMid("mid");
        requestData.setOrderId("orderId");
        requestData.setCustId("custId");
        requestData.setTxnAmount("1000");
        requestData.setChannelId("APP");
        requestData.setIndustryTypeId("industryTypeId");
        requestData.setWebsite("website");
        requestData.setAuthMode("USRPWD");
        requestData.setPaymentTypeId("UPI");
        validationService.validatePaytmExpressData(requestData);

        PaymentRequestBean requestData1 = new PaymentRequestBean();
        requestData1.setMid("::");
        validationService.validatePaytmExpressData(requestData1);

        PaymentRequestBean requestData2 = new PaymentRequestBean();
        requestData2.setMid("mid");
        requestData2.setOrderId("::");
        validationService.validatePaytmExpressData(requestData2);

        PaymentRequestBean requestData3 = new PaymentRequestBean();
        requestData3.setMid("mid");
        requestData3.setOrderId("orderId");
        requestData3.setCustId("123456789123456789123456789123456789123456789123456789");
        validationService.validatePaytmExpressData(requestData3);

        PaymentRequestBean requestData4 = new PaymentRequestBean();
        requestData4.setMid("mid");
        requestData4.setOrderId("orderId");
        requestData4.setCustId("custId");
        requestData4.setTxnAmount("amount");
        validationService.validatePaytmExpressData(requestData4);

        PaymentRequestBean requestData5 = new PaymentRequestBean();
        requestData5.setMid("mid");
        requestData5.setOrderId("orderId");
        requestData5.setCustId("custId");
        requestData5.setTxnAmount("1000");
        requestData5.setChannelId("channelId");
        validationService.validatePaytmExpressData(requestData5);

        PaymentRequestBean requestData6 = new PaymentRequestBean();
        requestData6.setMid("mid");
        requestData6.setOrderId("orderId");
        requestData6.setCustId("custId");
        requestData6.setTxnAmount("1000");
        requestData6.setChannelId("APP");
        requestData6.setIndustryTypeId("::");
        validationService.validatePaytmExpressData(requestData6);

        PaymentRequestBean requestData7 = new PaymentRequestBean();
        requestData7.setMid("mid");
        requestData7.setOrderId("orderId");
        requestData7.setCustId("custId");
        requestData7.setTxnAmount("1000");
        requestData7.setChannelId("APP");
        requestData7.setIndustryTypeId("industryTypeId");
        requestData7.setWebsite("::");
        validationService.validatePaytmExpressData(requestData7);

        PaymentRequestBean requestData8 = new PaymentRequestBean();
        requestData8.setMid("mid");
        requestData8.setOrderId("orderId");
        requestData8.setCustId("custId");
        requestData8.setTxnAmount("1000");
        requestData8.setChannelId("APP");
        requestData8.setIndustryTypeId("industryTypeId");
        requestData8.setWebsite("website");
        requestData8.setAuthMode("authmode");
        validationService.validatePaytmExpressData(requestData8);

        PaymentRequestBean requestData9 = new PaymentRequestBean();
        requestData9.setMid("mid");
        requestData9.setOrderId("orderId");
        requestData9.setCustId("custId");
        requestData9.setTxnAmount("1000");
        requestData9.setChannelId("APP");
        requestData9.setIndustryTypeId("industryTypeId");
        requestData9.setWebsite("website");
        requestData9.setAuthMode("USRPWD");
        requestData9.setPaymentTypeId("PaymentTYpeId");
        validationService.validatePaytmExpressData(requestData9);

        PaymentRequestBean requestData10 = new PaymentRequestBean();
        requestData10.setMid("mid");
        requestData10.setOrderId("orderId");
        requestData10.setCustId("custId");
        requestData10.setTxnAmount("1000");
        requestData10.setChannelId("APP");
        requestData10.setIndustryTypeId("industryTypeId");
        requestData10.setWebsite("website");
        requestData10.setAuthMode("USRPWD");
        requestData10.setPaymentTypeId("NB");
        validationService.validatePaytmExpressData(requestData10);

        PaymentRequestBean requestData11 = new PaymentRequestBean();
        requestData11.setMid("mid");
        requestData11.setOrderId("orderId");
        requestData11.setCustId("custId");
        requestData11.setTxnAmount("1000");
        requestData11.setChannelId("APP");
        requestData11.setIndustryTypeId("industryTypeId");
        requestData11.setWebsite("website");
        requestData11.setAuthMode("USRPWD");
        requestData11.setPaymentTypeId("PPI");
        requestData11.setSsoToken("SSO");
        requestData11.setIsAddMoney(TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1);
        requestData11.setPaytmToken("token");
        when(merchantPreferenceProvider.isAddMoneyEnabled((PaymentRequestBean) any())).thenReturn(true);
        validationService.validatePaytmExpressData(requestData11);

        when(merchantPreferenceProvider.isAddMoneyEnabled((PaymentRequestBean) any())).thenReturn(false);
        validationService.validatePaytmExpressData(requestData11);

        PaymentRequestBean requestData12 = new PaymentRequestBean();
        requestData12.setMid("mid");
        requestData12.setOrderId("orderId");
        requestData12.setCustId("custId");
        requestData12.setTxnAmount("1000");
        requestData12.setChannelId("APP");
        requestData12.setIndustryTypeId("industryTypeId");
        requestData12.setWebsite("website");
        requestData12.setAuthMode("USRPWD");
        requestData12.setPaymentTypeId("EMI");
        requestData12.setSsoToken("SSO");
        requestData12.setPaytmToken("token");
        Map<String, MerchantPreference> map = new HashMap<>();
        MerchantPreference merchantPreference = new MerchantPreference();
        merchantPreference.setEnabled(true);
        map.put(TheiaConstant.MerchantPreference.PreferenceKeys.HYBRID_ALLOWED, merchantPreference);
        requestData12.setIsAddMoney(TheiaConstant.ExtraConstants.PAYTM_EXPRESS_0);
        MerchantPreferenceStore merchantPreferenceStore = new MerchantPreferenceStore();
        merchantPreferenceStore.setPreferences(map);
        when(merchantPreferenceService.getMerchantPreferenceStore(any())).thenReturn(merchantPreferenceStore);
        validationService.validatePaytmExpressData(requestData12);

        MerchantPreference merchantPreference1 = new MerchantPreference();
        merchantPreference1.setEnabled(false);
        map.put(TheiaConstant.MerchantPreference.PreferenceKeys.HYBRID_ALLOWED, merchantPreference1);
        validationService.validatePaytmExpressData(requestData12);

        PaymentRequestBean requestData13 = new PaymentRequestBean();
        requestData13.setMid("mid");
        requestData13.setOrderId("orderId");
        requestData13.setCustId("custId");
        requestData13.setTxnAmount("1000");
        requestData13.setChannelId("APP");
        requestData13.setIndustryTypeId("industryTypeId");
        requestData13.setWebsite("website");
        requestData13.setAuthMode("USRPWD");
        requestData13.setPaymentTypeId("NB");
        requestData13.setBankCode("bankCode");
        validationService.validatePaytmExpressData(requestData13);

        PaymentRequestBean requestData14 = new PaymentRequestBean();
        requestData14.setMid("mid");
        requestData14.setOrderId("orderId");
        requestData14.setCustId("custId");
        requestData14.setTxnAmount("1000");
        requestData14.setChannelId("APP");
        requestData14.setIndustryTypeId("industryTypeId");
        requestData14.setWebsite("website");
        requestData14.setAuthMode("USRPWD");
        requestData14.setPaymentTypeId("PPI");
        requestData14.setSsoToken("SSO");
        requestData14.setIsAddMoney(TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1);
        validationService.validatePaytmExpressData(requestData14);

        PaymentRequestBean requestData15 = new PaymentRequestBean();
        requestData15.setMid("mid");
        requestData15.setOrderId("orderId");
        requestData15.setCustId("custId");
        requestData15.setTxnAmount("1000");
        requestData15.setChannelId("APP");
        requestData15.setIndustryTypeId("industryTypeId");
        requestData15.setWebsite("website");
        requestData15.setAuthMode("USRPWD");
        requestData15.setPaymentTypeId("IMPS");
        requestData15.setPaymentDetails("paymentDetails");
        validationService.validatePaytmExpressData(requestData15);

        PaymentRequestBean requestData16 = new PaymentRequestBean();
        requestData16.setMid("mid");
        requestData16.setOrderId("orderId");
        requestData16.setCustId("custId");
        requestData16.setTxnAmount("1000");
        requestData16.setChannelId("APP");
        requestData16.setIndustryTypeId("industryTypeId");
        requestData16.setWebsite("website");
        requestData16.setAuthMode("USRPWD");
        requestData16.setPaymentTypeId("PPI");
        requestData16.setSsoToken("SSO");
        requestData16.setIsAddMoney(TheiaConstant.ExtraConstants.EXPRESS_PAYMENT);
        requestData16.setPaytmToken("token");
        validationService.validatePaytmExpressData(requestData16);

        PaymentRequestBean requestData17 = new PaymentRequestBean();
        requestData17.setMid("mid");
        requestData17.setOrderId("orderId");
        requestData17.setCustId("custId");
        requestData17.setTxnAmount("1000");
        requestData17.setChannelId("APP");
        requestData17.setIndustryTypeId("industryTypeId");
        requestData17.setWebsite("website");
        requestData17.setAuthMode("USRPWD");
        requestData17.setPaymentTypeId("PPI");
        validationService.validatePaytmExpressData(requestData17);
    }

    @Test
    public void testValidateEMIPayment() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        workFlowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.ADDANDPAY);
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean, workFlowRequestBean,
                entityPaymentOptions);
    }

    @Test
    public void testValidateEMIPayment1() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        workFlowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.CC.value);
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean, workFlowRequestBean,
                entityPaymentOptions);
    }

    @Test
    public void testValidateEMIPayment2() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        workFlowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean, workFlowRequestBean,
                entityPaymentOptions);
    }

    @Test
    public void testValidateSeamlessEMIPaymentRequest() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        workFlowRequestBean.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean.setEmiPlanID("emiPlanId");
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean, workFlowRequestBean,
                entityPaymentOptions);

        PaymentRequestBean paymentRequestBean1 = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaytmExpressAddOrHybrid(EPayMode.HYBRID);
        paymentRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean1.setEmiPlanID("emiPlanId");
        workFlowRequestBean1.setCardNo("1234567");
        entityPaymentOptions.setCompleteEMIInfoList(Collections.singletonList(new BankInfo()));
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean1, workFlowRequestBean1,
                entityPaymentOptions);
    }

    @Test
    public void testValidateSeamlessEMIPaymentRequest1() {
        PaymentRequestBean paymentRequestBean1 = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        EntityPaymentOptionsTO entityPaymentOptions = new EntityPaymentOptionsTO();
        workFlowRequestBean1.setPaytmExpressAddOrHybrid(EPayMode.NONE);
        paymentRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean1.setEmiPlanID("emiPlanId");
        workFlowRequestBean1.setCardNo("1234567");
        entityPaymentOptions.setCompleteEMIInfoList(Collections.singletonList(new BankInfo()));
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequest(paymentRequestBean1, workFlowRequestBean1,
                entityPaymentOptions);
    }

    @Test
    public void testValidateEMIPaymentNative() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        NativeCashierInfoResponse nativeCashierInfoResponse = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = new NativeCashierInfoResponseBody();
        body.setAccessToken("accessToken");
        nativeCashierInfoResponse.setBody(body);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean.setEmiPlanID("emiPlanId");
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequestNative(paymentRequestBean, workFlowRequestBean,
                nativeCashierInfoResponse);
    }

    @Test
    public void testValidateEMIPaymentNative1() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        NativeCashierInfoResponse nativeCashierInfoResponse = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = new NativeCashierInfoResponseBody();
        body.setAccessToken("accessToken");
        body.setMerchantPayOption(new PayOption());
        nativeCashierInfoResponse.setBody(body);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean.setEmiPlanID("emiPlanId");
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequestNative(paymentRequestBean, workFlowRequestBean,
                nativeCashierInfoResponse);
    }

    @Test
    public void testValidateSeamlessEMIPaymentRequestNative() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        NativeCashierInfoResponse nativeCashierInfoResponse = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = new NativeCashierInfoResponseBody();
        body.setAccessToken("accessToken");
        PayOption payOption = new PayOption();
        PayMethod payMethod = new PayMethod();
        payMethod.setPayMethod("EMI");
        payOption.setPayMethods(Collections.singletonList(payMethod));
        body.setMerchantPayOption(payOption);
        nativeCashierInfoResponse.setBody(body);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        paymentRequestBean.setEmiPlanID("emiPlanId");
        validationService.validateSeamlessEMIPaymentRequestNative(paymentRequestBean, workFlowRequestBean,
                nativeCashierInfoResponse);

        payOption.setPayMethods(Collections.singletonList(new PayMethod()));
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequestNative(paymentRequestBean, workFlowRequestBean,
                nativeCashierInfoResponse);
    }

    @Test
    public void testValidateSeamlessEMIPaymentRequestNative1() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        NativeCashierInfoResponse nativeCashierInfoResponse = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = new NativeCashierInfoResponseBody();
        body.setAccessToken("accessToken");
        PayOption payOption = new PayOption();
        PayMethod payMethod = new PayMethod();
        payMethod.setPayMethod("EMI");
        payMethod.setPayChannelOptions(Collections.singletonList(new BalanceChannel()));
        payOption.setPayMethods(Collections.singletonList(payMethod));
        body.setMerchantPayOption(payOption);
        nativeCashierInfoResponse.setBody(body);
        paymentRequestBean.setPaymentTypeId(PaymentTypeIdEnum.EMI.value);
        workFlowRequestBean.setCardNo("12345678");
        paymentRequestBean.setEmiPlanID("emiPlanId");
        paymentRequestBean.setEmitype("emiType");
        exceptionRule.expect(TheiaServiceException.class);
        validationService.validateSeamlessEMIPaymentRequestNative(paymentRequestBean, workFlowRequestBean,
                nativeCashierInfoResponse);
    }

    @Test
    public void testValidateAndProcessAddMoneyExpressPaymentDetails() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setPaymentDetails("1|2");
        workFlowRequestBean.setIsSavedCard(true);
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        validationService
                .validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean, new PaymentRequestBean());

        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaymentDetails("1| ");
        workFlowRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean1,
                new PaymentRequestBean());
    }

    @Test
    public void testValidateAndProcessAddMoneyExpressPaymentDetails1() {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setPaymentDetails("1|2|122049");
        workFlowRequestBean.setIsSavedCard(true);
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.CC.value);
        validationService
                .validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean, new PaymentRequestBean());

        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaymentDetails("1|2|1234");
        workFlowRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean1,
                new PaymentRequestBean());
    }

    @Test
    public void testValidateAndProcessAddMoneyExpressPaymentDetails2() {
        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaymentDetails("1|2| ");
        workFlowRequestBean1.setPaymentTypeId(PaymentTypeIdEnum.DC.value);
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean1,
                new PaymentRequestBean());
    }

    @Test
    public void testValidateAndProcessAddMoneyExpressPaymentDetails3() {
        WorkFlowRequestBean workFlowRequestBean1 = new WorkFlowRequestBean();
        workFlowRequestBean1.setPaymentDetails("1");
        workFlowRequestBean1.setIsSavedCard(true);
        workFlowRequestBean1.setStoreCard(TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1);
        exceptionRule.expect(PaymentRequestValidationException.class);
        validationService.validateAndProcessAddMoneyExpressPaymentDetails(workFlowRequestBean1,
                new PaymentRequestBean());
    }
}