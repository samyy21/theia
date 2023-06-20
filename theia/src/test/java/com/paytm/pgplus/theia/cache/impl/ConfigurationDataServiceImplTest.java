package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cache.util.vault.VaultReadUtil;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.theia.exceptions.DisasterException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ConfigurationDataServiceImplTest {

    @InjectMocks
    ConfigurationDataServiceImpl configurationDataServiceImpl;

    @Mock
    IConfigurationService configurationService;

    @Mock
    VaultReadUtil vaultReadUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPaytmProperty() throws MappingServiceClientException {

        when(vaultReadUtil.getPaytmProperty("prop")).thenReturn(null, null, new PaytmProperty());
        when(configurationService.getPaytmProperty("prop")).thenThrow(new MappingServiceClientException(""))
                .thenReturn(new PaytmProperty());
        configurationDataServiceImpl.getPaytmProperty("prop");
        assertNotNull(configurationDataServiceImpl.getPaytmProperty("prop"));
        assertNotNull(configurationDataServiceImpl.getPaytmProperty("prop"));

    }

    @Test
    public void testGetPaytmProperty() throws MappingServiceClientException {
        when(vaultReadUtil.getPaytmProperty("prop")).thenReturn(null, new PaytmProperty());
        when(configurationService.getPaytmProperty("prop")).thenReturn(null);
        assertNotNull(configurationDataServiceImpl.getPaytmProperty("prop", "default"));
        assertNull(configurationDataServiceImpl.getPaytmProperty("prop", "defaultValue"));

    }

    @Test
    public void getPaytmPropertyValue() throws MappingServiceClientException {
        when(vaultReadUtil.getPaytmProperty("prop")).thenReturn(null, new PaytmProperty());
        when(configurationService.getPaytmProperty("prop")).thenReturn(null);
        assertEquals(StringUtils.EMPTY, configurationDataServiceImpl.getPaytmPropertyValue("prop"));
        assertNull(StringUtils.EMPTY, configurationDataServiceImpl.getPaytmPropertyValue("prop"));

    }

    @Test
    public void getPaytmDefaultValues() throws MappingServiceClientException {

        when(configurationService.getPaytmDefaultValues("fieldName")).thenThrow(new MappingServiceClientException(""))
                .thenReturn(null, new PaytmDefaultValues());
        assertNull(configurationDataServiceImpl.getPaytmDefaultValues("fieldName"));
        assertNull(configurationDataServiceImpl.getPaytmDefaultValues("fieldName"));
        assertNotNull(configurationDataServiceImpl.getPaytmDefaultValues("fieldName"));
    }

    @Test
    public void testGetPaytmDefaultValues() throws MappingServiceClientException {
        PaytmDefaultValuesList paytmDefaultValuesList = new PaytmDefaultValuesList();
        paytmDefaultValuesList.setPaytmDefaultValuesList(Collections.singletonList(new PaytmDefaultValues()));
        when(configurationService.getPaytmDefaultValuesListV2(any())).thenThrow(new MappingServiceClientException(""))
                .thenReturn(null, new PaytmDefaultValuesList(), paytmDefaultValuesList);
        assertNotNull(configurationDataServiceImpl.getPaytmDefaultValues(new ArrayList<String>()));
        configurationDataServiceImpl.getPaytmDefaultValues(new ArrayList<String>());
        configurationDataServiceImpl.getPaytmDefaultValues(new ArrayList<String>());
        assertNotNull(configurationDataServiceImpl.getPaytmDefaultValues(new ArrayList<String>()));

    }

    @Test
    public void getEmiValidBins() throws MappingServiceClientException {
        EMIValidBins emiValidBins = new EMIValidBins();
        emiValidBins.setValidBins(Collections.singletonList("validBin"));
        when(configurationService.getEmiValidBins("key")).thenThrow(new MappingServiceClientException("")).thenReturn(
                null, new EMIValidBins(), emiValidBins);
        assertNull(configurationDataServiceImpl.getEmiValidBins("key"));
        assertNull(configurationDataServiceImpl.getEmiValidBins("key"));
        assertNull(configurationDataServiceImpl.getEmiValidBins("key"));
        assertNotNull(configurationDataServiceImpl.getEmiValidBins("key"));

    }

    @Test
    public void getEntityIgnoreParams() throws MappingServiceClientException {

        String entityId = "12345";
        EntityIgnoreParamsResponse entityIgnoreParamsResponse = new EntityIgnoreParamsResponse();
        entityIgnoreParamsResponse.setParamsList(Collections
                .singletonList(new EntityIgnoreParamsResponse.EntityIgnoreParams()));
        when(configurationService.getEntityIgnoreParams(12345l)).thenThrow(new MappingServiceClientException(""))
                .thenReturn(null, entityIgnoreParamsResponse);
        assertNull(configurationDataServiceImpl.getEntityIgnoreParams(entityId));
        assertNull(configurationDataServiceImpl.getEntityIgnoreParams(entityId));
        assertNotNull(configurationDataServiceImpl.getEntityIgnoreParams(entityId));
    }
}