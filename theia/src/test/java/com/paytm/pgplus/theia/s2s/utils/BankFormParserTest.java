package com.paytm.pgplus.theia.s2s.utils;

import com.alibaba.fastjson.JSON;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DeepLink;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import jdk.nashorn.internal.parser.JSONParser;
import mockit.MockUp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.print.Doc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BankFormParserTest {

    @InjectMocks
    private BankFormParser bankFormParser;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void parseHTMLFormNative() {
        BankForm bankForm = new BankForm();
        bankForm.setRedirectForm(new FormDetail());
        String webFormContext = JSON.toJSONString(bankForm);
        assertNotNull(bankFormParser.parseHTMLFormNative(webFormContext));
        Document document = mock(Document.class);
        new MockUp<Jsoup>() {

            @mockit.Mock
            public Document parse(String html) {
                return document;
            }
        };
        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.put("action", "instaproxy/bankresponse");
        attributes.put("method", "requestMethod");
        Element element = new Element(mock(Tag.class), "baseUrl", attributes);
        elements.add(element);
        when(document.select("FORM")).thenReturn(elements);
        assertNotNull(bankFormParser.parseHTMLFormNative(webFormContext));
    }

    @Test
    public void parseHTMLForm() {
        Document document = mock(Document.class);
        String webFormContext = JSON.toJSONString(document);
        new MockUp<Jsoup>() {

            @mockit.Mock
            public Document parse(String html) {
                return document;
            }
        };
        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.put("action", "instaproxy/bankresponse");
        attributes.put("method", "requestMethod");
        Element element = new Element(mock(Tag.class), "baseUrl", attributes);
        elements.add(element);
        when(document.select("FORM")).thenReturn(elements);
        assertNotNull(bankFormParser.parseHTMLForm(webFormContext));

    }

    @Test
    public void testParseHTMLForm() {

        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setPaymentTypeId("UPI");
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        paymentRequestBean.setDeepLinkFromInsta(true);
        QueryPaymentStatus queryPaymentStatus = mock(QueryPaymentStatus.class);
        BankForm bankForm = new BankForm();
        bankForm.setDeepLink(new DeepLink());
        bankForm.getDeepLink().setUrl("url");
        when(queryPaymentStatus.getWebFormContext()).thenReturn(JSON.toJSONString(bankForm));
        workFlowResponseBean.setQueryPaymentStatus(queryPaymentStatus);
        assertNotNull(bankFormParser.parseHTMLForm(paymentRequestBean, workFlowResponseBean));
        paymentRequestBean.setDeepLinkFromInsta(false);
        MerchantVpaTxnInfo merchantVpaTxnInfo = new MerchantVpaTxnInfo();
        merchantVpaTxnInfo.setVpa("vpa");
        when(queryPaymentStatus.getWebFormContext()).thenReturn(JSON.toJSONString(merchantVpaTxnInfo));
        new MockUp<UpiInfoSessionUtil>() {

            @mockit.Mock
            public String getTransactionAmount(WorkFlowResponseBean workFlowResponseBean,
                    PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
                return "2000";
            }
        };
        doNothing().when(theiaTransactionalRedisUtil).set(anyString(), anyObject(), anyLong());
        assertNotNull(bankFormParser.parseHTMLForm(paymentRequestBean, workFlowResponseBean));

    }

    @Test
    public void checkIfDirectBankPage() {

        bankFormParser.checkIfDirectBankPage(JSON.toJSONString(new BankForm()), "reqId", new WorkFlowRequestBean());
    }
}