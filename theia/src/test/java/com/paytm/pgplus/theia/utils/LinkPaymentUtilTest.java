package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.common.model.link.LinkPaymentRiskInfo;
import com.paytm.pgplus.common.model.link.SplitSettlementInfo;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.model.link.PaymentFormDetails;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.impl.LinkService;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class LinkPaymentUtilTest extends AOAUtilsTest {

    @InjectMocks
    LinkPaymentUtil linkPaymentUtil;

    @Mock
    IConfigurationService configurationServiceImpl;

    @Mock
    ResponseCodeUtil responseCodeUtil;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    LinkService linkService;

    @Mock
    IPgpFf4jClient iPgpFf4jClient;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    Enumeration<String> en;

    @Test
    public void testSetpageDetailsResponseForLinkBasedPayment() throws MappingServiceClientException {

        PaymentRequestBean requestData = new PaymentRequestBean();
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantDisplayName("test");
        merchantBussinessLogoInfo.setMerchantImageName("testImage");

        when(configurationServiceImpl.getMerchantlogoInfoFromMidV2(anyString())).thenReturn(merchantBussinessLogoInfo);
        when(responseCodeUtil.getResponseMsg(any())).thenReturn("test");

        requestData.setRequestType("LINK_BASED_PAYMENT_INVOICE");
        requestData.setMid("test");
        requestData.setTxnAmount("100");
        linkPaymentUtil.setpageDetailsResponseForLinkBasedPayment(pageDetailsResponse, requestData, "test", "test");
        Assert.assertNotNull(pageDetailsResponse.getData());
    }

    @Test
    public void testValidateLinkAndSetCallBackURL() throws FacadeCheckedException {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("test", "test");
        PaymentRequestBean requestData = new PaymentRequestBean();
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo();
        LinkDetailResponseBody linkDetailResponseBody = new LinkDetailResponseBody();
        PaymentFormDetails paymentFormDetails = new PaymentFormDetails();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus("S");
        linkDetailResponseBody.setPaymentFormDetails(paymentFormDetails);
        linkDetailResponseBody.setResultInfo(resultInfo);
        linkDetailResponseBody.setStatusCallBackURL("test");
        linkDetailResponseBody.setPaymentFormId("test");
        linkDetailResponseBody.setResellerId("test");
        linkDetailResponseBody.setResellerName("test");
        linkDetailResponseBody.setMerchantReferenceId("test");
        linkDetailResponseBody.setExtendInfo(testMap);
        linkDetailResponseBody.setDisplayWarningMessage(true);
        linkDetailResponseBody.setSplitSettlementInfo(splitSettlementInfo);
        linkDetailResponseBody.setAmount(100.00);
        linkDetailResponseBody.setLinkDescription("test");
        linkDetailResponseBody.setAmount(100.00);

        when(nativeSessionUtil.getKey(anyString())).thenReturn(null);
        when(linkService.getLinkDetail(any())).thenReturn(linkDetailResponseBody);
        // doNothing().when(nativeSessionUtil).setKey("test",any(),any());
        when(iPgpFf4jClient.checkWithdefault(anyString(), any(), anyBoolean())).thenReturn(true);

        requestData.setMid("test");
        requestData.setLinkId("test");
        requestData.setInvoiceId("test");
        requestData.setPaymentFormId("test");
        requestData.setOrderId("test");
        requestData.setTxnAmount("100");
        requestData.setLinkDescription("test");
        Assert.assertEquals("", linkPaymentUtil.validateLinkAndSetCallBackURL(requestData, true));
    }

    @Test
    public void testGetPaymentFormDetailsWhenReturnNotNUll() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setPaymentFormId("test");
        when(nativeSessionUtil.getKey(anyString())).thenReturn(new PaymentFormDetails());
        Assert.assertNotNull(linkPaymentUtil.getPaymentFormDetails(paymentRequestBean));
    }

    @Test
    public void testGetPaymentFormDetailsWhenReturnNUll() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setPaymentFormId("");
        when(nativeSessionUtil.getKey(anyString())).thenReturn(new PaymentFormDetails());
        Assert.assertNull(linkPaymentUtil.getPaymentFormDetails(paymentRequestBean));
    }

    @Test
    public void testGetLinkPaymentRiskInfoWhenReturnNotNull() {
        LinkDetailResponseBody linkDetailResponseBody = new LinkDetailResponseBody();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        LinkPaymentRiskInfo linkPaymentRiskInfo = new LinkPaymentRiskInfo();
        linkDetailResponseBody.setLinkPaymentRiskInfo(linkPaymentRiskInfo);
        when(nativeSessionUtil.getKey(anyString())).thenReturn(new LinkDetailResponseBody());
        Assert.assertNull(linkPaymentUtil.getLinkPaymentRiskInfo(paymentRequestBean));
    }

    @Test
    public void testAddLinkDetailsInPaymentRequestBeanInPayment() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        com.paytm.pgplus.common.model.link.LinkDetailResponseBody linkDetailResponseBody = new com.paytm.pgplus.common.model.link.LinkDetailResponseBody();
        linkDetailResponseBody.setAmount(100.00);
        linkDetailResponseBody.setLinkDescription("test");
        linkDetailResponseBody.setPaymentFormId("test");
        linkDetailResponseBody.setStatusCallBackURL("test");
        linkDetailResponseBody.setResellerId("test");
        linkDetailResponseBody.setResellerName("test");
        paymentRequestBean.setTxnAmount("100.00");
        paymentRequestBean.setLinkDescription("test");

        paymentRequestBean.setLinkDetailsData(linkDetailResponseBody);

        linkPaymentUtil.addLinkDetailsInPaymentRequestBeanInPayment(paymentRequestBean);
        Assert.assertEquals("test", paymentRequestBean.getResellerName());
    }

    @Test
    public void testGetLinkDetailCachedResponseWhenReturnNull() {
        when(nativeSessionUtil.getKey(anyString())).thenReturn(null);
        Assert.assertNull(linkPaymentUtil.getLinkDetailCachedResponse(httpServletRequest));
    }

    @Test
    public void testGetLinkDetailCachedResponseWhenReturnNotNull() {
        when(nativeSessionUtil.getKey(anyString())).thenReturn(new LinkDetailResponseBody());
        Assert.assertNotNull(linkPaymentUtil.getLinkDetailCachedResponse(httpServletRequest));
    }

    @Test
    public void testRequestParamsToJSON() {
        when(httpServletRequest.getAttributeNames()).thenReturn(en);
        Assert.assertNotNull(linkPaymentUtil.requestParamsToJSON(httpServletRequest));
    }
}