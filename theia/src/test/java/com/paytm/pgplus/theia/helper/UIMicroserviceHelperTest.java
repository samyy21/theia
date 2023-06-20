package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.uimicroservice.service.IUIMicroservice;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static com.paytm.pgplus.facade.enums.UIMicroserviceUrl.RISK_VERIFICATION_URL;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UIMicroserviceHelperTest {

    @InjectMocks
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Mock
    private IUIMicroservice uiMicroService;

    @Mock
    private FF4JHelper ff4JHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        new MockUp<EnvInfoUtil>() {

            @mockit.Mock
            public HttpServletRequest httpServletRequest() {
                return httpServletRequest;
            }
        };
        when(httpServletRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(Collections.singletonList("nanda")));

    }

    @Test
    public void getUiMicroServiceRequestJson() throws FacadeCheckedException, IOException {

        assertNotNull(uiMicroserviceHelper.getUiMicroServiceRequestJson(JsonMapper.mapObjectToJson(new HashMap<>()),
                null, "true"));

    }

    @Test
    public void testGetUiMicroServiceRequestJson() throws FacadeCheckedException, IOException {

        assertNotNull(uiMicroserviceHelper.getUiMicroServiceRequestJson(JsonMapper.mapObjectToJson(new HashMap<>())));
    }

    @Test
    public void getHtmlPageFromUI() throws FacadeCheckedException {

        UIMicroserviceRequest request = new UIMicroserviceRequest();
        request.setJsonPayload(JsonMapper.mapObjectToJson(new HashMap<>()));
        request.setUiMicroServiceUrl(RISK_VERIFICATION_URL);
        when(uiMicroService.getHtmlPageFromUiMicroService(any(), any())).thenReturn("htmlPage");
        when(ff4JHelper.isFF4JFeatureForMidEnabled(any(), any())).thenReturn(true).thenReturn(true);
        assertNotNull(uiMicroserviceHelper.getHtmlPageFromUI(request, "feature", "mid"));

    }
}