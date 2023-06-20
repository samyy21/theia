package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.SubWalletDetails;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IWalletInfoService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import mockit.MockUp;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class WalletInfoSessionUtilTest extends AOAUtilsTest {

    @InjectMocks
    WalletInfoSessionUtil walletInfoSessionUtil;

    @Mock
    PaymentRequestBean requestData;

    @Mock
    WorkFlowResponseBean responseBean;

    @Mock
    UserDetailsBiz userDetailsBiz;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    WalletInfo walletInfo;

    @Mock
    LoginInfo loginInfo;

    @Mock
    ConsultPayViewResponseBizBean consultPayViewResponseBizBean;

    @Mock
    LitePayviewConsultResponseBizBean payviewConsultResponseBizBean;

    @Mock
    PayMethodViewsBiz payMethodViewsBiz;

    @Mock
    PayChannelOptionViewBiz payChannel;

    @Mock
    BalanceChannelInfoBiz balanceChannelInfoBiz;

    @Mock
    IWalletInfoService walletInfoService;

    @Mock
    SubWalletDetails subWalletDetails;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    IConfigurationDataService configurationDataService;

    @Test
    public void testSetWalletInfoIntoSessionWhenUserDetailsNotNull() throws MappingServiceClientException {
        List<Map<String, Object>> subWallets = new ArrayList<>();
        Map<String, Object> subWallet = new HashMap<>();
        subWallet.put("status", "1");
        subWallet.put("subWalletType", 0);
        subWallet.put("balance", 5);
        subWallets.add(subWallet);
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public List<Map<String, Object>> getListOfMapFromJson(String value) {
                return subWallets;
            }
        };
        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(consultPayViewResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(true);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);
        when(balanceChannelInfoBiz.getAccountBalance()).thenReturn("100");
        when(balanceChannelInfoBiz.getPayerAccountNo()).thenReturn("1234567890");
        when(consultPayViewResponseBizBean.isWalletOnly()).thenReturn(true);
        when(consultPayViewResponseBizBean.isWalletFailed()).thenReturn(true);
        when(walletInfoService.getSubWalletDetails(anyString())).thenReturn(subWalletDetails);
        when(subWalletDetails.getSubWalletWebLogo()).thenReturn("test");
        when(subWalletDetails.getSubWalletWapLogo()).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(responseBean.getAllowedPayMode()).thenReturn(EPayMode.NONE);
        when(merchantPreferenceService.isAddMoneyEnabled(anyString())).thenReturn(true);
        when(requestData.getTxnAmount()).thenReturn("50");
        when(walletInfo.getWalletBalance()).thenReturn(10.0);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSessionWhenUserSetailNull() {
        when(responseBean.getUserDetails()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(consultPayViewResponseBizBean.isWalletOnly()).thenReturn(true);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSessionWhenEnableStatusFalse() throws MappingServiceClientException {

        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(consultPayViewResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(false);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);

        when(payChannel.getDisableReason()).thenReturn("ACCOUNT_BALANCE_QUERY_FAIL");
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSessionWhenDisableReasonDefault() throws MappingServiceClientException {

        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(consultPayViewResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(false);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);
        when(payChannel.getDisableReason()).thenReturn("test");
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSession() throws MappingServiceClientException {
        List<Map<String, Object>> subWallets = new ArrayList<>();
        Map<String, Object> subWallet = new HashMap<>();
        subWallet.put("status", "1");
        subWallet.put("subWalletType", 0);
        subWallet.put("balance", 5);
        subWallets.add(subWallet);
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public List<Map<String, Object>> getListOfMapFromJson(String value) throws FacadeCheckedException {
                throw new FacadeCheckedException("test");

            }
        };
        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(null);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(payviewConsultResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(true);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);
        when(balanceChannelInfoBiz.getAccountBalance()).thenReturn("100");
        when(balanceChannelInfoBiz.getPayerAccountNo()).thenReturn("1234567890");
        when(consultPayViewResponseBizBean.isWalletOnly()).thenReturn(true);
        when(consultPayViewResponseBizBean.isWalletFailed()).thenReturn(true);
        when(walletInfoService.getSubWalletDetails(anyString())).thenReturn(subWalletDetails);
        when(subWalletDetails.getSubWalletWebLogo()).thenReturn("test");
        when(subWalletDetails.getSubWalletWapLogo()).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(responseBean.getAllowedPayMode()).thenReturn(EPayMode.NONE);
        when(merchantPreferenceService.isAddMoneyEnabled(anyString())).thenReturn(true);
        when(requestData.getTxnAmount()).thenReturn("50");
        when(walletInfo.getWalletBalance()).thenReturn(10.0);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSessionWhenWalletInfoServiceThrowsException() throws MappingServiceClientException {
        List<Map<String, Object>> subWallets = new ArrayList<>();
        Map<String, Object> subWallet = new HashMap<>();
        subWallet.put("status", "1");
        subWallet.put("subWalletType", 0);
        subWallet.put("balance", 5);
        subWallets.add(subWallet);
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public List<Map<String, Object>> getListOfMapFromJson(String value) {
                return subWallets;
            }
        };
        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(consultPayViewResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(true);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);
        when(balanceChannelInfoBiz.getAccountBalance()).thenReturn("100");
        when(balanceChannelInfoBiz.getPayerAccountNo()).thenReturn("1234567890");
        when(consultPayViewResponseBizBean.isWalletOnly()).thenReturn(true);
        when(consultPayViewResponseBizBean.isWalletFailed()).thenReturn(true);
        // when(walletInfoService.getSubWalletDetails(anyString())).thenReturn(subWalletDetails);
        doThrow(MappingServiceClientException.class).when(walletInfoService).getSubWalletDetails(anyString());
        when(subWalletDetails.getSubWalletWebLogo()).thenReturn("test");
        when(subWalletDetails.getSubWalletWapLogo()).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(responseBean.getAllowedPayMode()).thenReturn(EPayMode.NONE);
        when(merchantPreferenceService.isAddMoneyEnabled(anyString())).thenReturn(true);
        when(requestData.getTxnAmount()).thenReturn("50");
        when(walletInfo.getWalletBalance()).thenReturn(49.5);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

    @Test
    public void testSetWalletInfoIntoSessionWhenPayMethodSizeMoreThen2() throws MappingServiceClientException {
        List<Map<String, Object>> subWallets = new ArrayList<>();
        Map<String, Object> subWallet = new HashMap<>();
        subWallet.put("status", "1");
        subWallet.put("subWalletType", 12);
        subWallet.put("balance", 5);
        subWallet.put("issuerMetadata", "test");
        subWallets.add(subWallet);
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public List<Map<String, Object>> getListOfMapFromJson(String value) {
                return subWallets;
            }
        };
        List<PayMethodViewsBiz> payMethods = new ArrayList<>();
        payMethods.add(payMethodViewsBiz);
        payMethods.add(payMethodViewsBiz);
        List<PayChannelOptionViewBiz> testList = new ArrayList<>();
        testList.add(payChannel);
        List<BalanceChannelInfoBiz> listBalanceChannelInfoBiz = new ArrayList<>();
        listBalanceChannelInfoBiz.add(balanceChannelInfoBiz);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("subWalletDetailsList", "test");

        when(responseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(theiaSessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(walletInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(null);
        when(responseBean.getMerchnatViewResponse()).thenReturn(consultPayViewResponseBizBean);
        when(responseBean.getMerchnatLiteViewResponse()).thenReturn(payviewConsultResponseBizBean);
        when(responseBean.getSubsType()).thenReturn(SubsTypes.DC_ONLY);
        when(consultPayViewResponseBizBean.getPayMethodViews()).thenReturn(payMethods);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testList);
        when(payChannel.isEnableStatus()).thenReturn(true);
        when(payChannel.getBalanceChannelInfos()).thenReturn(listBalanceChannelInfoBiz);
        when(payChannel.getExtendInfo()).thenReturn(testMap);
        when(balanceChannelInfoBiz.getAccountBalance()).thenReturn("100");
        when(balanceChannelInfoBiz.getPayerAccountNo()).thenReturn("1234567890");
        when(consultPayViewResponseBizBean.isWalletOnly()).thenReturn(true);
        when(consultPayViewResponseBizBean.isWalletFailed()).thenReturn(true);
        // when(walletInfoService.getSubWalletDetails(anyString())).thenReturn(subWalletDetails);
        doThrow(MappingServiceClientException.class).when(walletInfoService).getSubWalletDetails(anyString());
        when(subWalletDetails.getSubWalletWebLogo()).thenReturn("test");
        when(subWalletDetails.getSubWalletWapLogo()).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(responseBean.getAllowedPayMode()).thenReturn(EPayMode.NONE);
        when(merchantPreferenceService.isAddMoneyEnabled(anyString())).thenReturn(true);
        when(requestData.getTxnAmount()).thenReturn("50");
        when(walletInfo.getWalletBalance()).thenReturn(49.5);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        walletInfoSessionUtil.setWalletInfoIntoSession(requestData, responseBean);
        verify(theiaSessionDataService, atMost(1)).getWalletInfoFromSession(any());
    }

}