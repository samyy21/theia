package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import static org.junit.Assert.*;

public class FF4JHelperTest {

    @InjectMocks
    private FF4JHelper ff4JHelper = new FF4JHelper();

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(FF4JHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testIsFF4JFeatureForMidEnabled() {
        ff4JHelper.isFF4JFeatureForMidEnabled("featureName", "");
        when(iPgpFf4jClient.checkWithdefault(any(), any(), anyBoolean())).thenReturn(true);
        ff4JHelper.isFF4JFeatureForMidEnabled("featureName", "mid");

        when(iPgpFf4jClient.checkWithdefault(any(), any(), anyBoolean())).thenReturn(false);
        ff4JHelper.isFF4JFeatureForMidEnabled("featureName", "mid");
    }
}