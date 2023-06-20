package com.paytm.pgplus.theia.nativ.supergw.helper;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailV4Request;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailV4RequestBody;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SuperGwBinDetailHelperTest {

    @InjectMocks
    private SuperGwBinDetailHelper helper;

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    @Mock
    private Environment environment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void validateBin() {
        NativeBinDetailV4Request request = new NativeBinDetailV4Request();
        exception.expect(BinDetailException.class);
        helper.validateBin(request);
    }

    @Test
    public void createBinDetailRequest() {

        NativeBinDetailV4Request request = new NativeBinDetailV4Request();
        request.setHead(new TokenRequestHeader());
        request.getHead().setChannelId(EChannelId.APP);
        request.setBody(new NativeBinDetailV4RequestBody());
        request.getBody().setMerchantUserInfo(new UserInfo());
        assertNotNull(helper.createBinDetailRequest(request));

    }

    @Test
    public void validateJwt() {

        NativeBinDetailV4Request request = new NativeBinDetailV4Request();
        request.setHead(new TokenRequestHeader());
        request.getHead().setClientId("client");
        NativeBinDetailV4RequestBody requestBody = new NativeBinDetailV4RequestBody();
        requestBody.setMid("TestMid");
        requestBody.setBin("TestBin");
        requestBody.setisEMIDetail(true);
        request.setBody(requestBody);
        when(environment.getProperty(Mockito.any())).thenReturn("client secret");
        new MockUp<SuperGwValidationUtil>() {

            @mockit.Mock
            public void validateJwtToken(Map<String, String> claims, String clientId, String clientSecret) {
                return;
            }
        };
        helper.validateJwt(request);
    }
}