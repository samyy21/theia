package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.cache.model.BankMasterDetailsList;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MerchantBankInfoDataServiceImplTest {

    @InjectMocks
    MerchantBankInfoDataServiceImpl merchantBankInfoDataService;

    @Mock
    private IBankInfoDataService bankInfoDataService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getBankInfo() throws MappingServiceClientException {

        when(bankInfoDataService.getBankInfoData("id")).thenThrow(new MappingServiceClientException("")).thenReturn(
                null, new BankMasterDetails());
        assertNull(merchantBankInfoDataService.getBankInfo("id"));
        assertNull(merchantBankInfoDataService.getBankInfo("id"));
        assertNotNull(merchantBankInfoDataService.getBankInfo("id"));
    }

    @Test
    public void getBankInfoDataListFromBankCodes() throws MappingServiceClientException {

        List<String> bankCodeList = new ArrayList<>();
        BankMasterDetailsList bankMasterDetailsList = new BankMasterDetailsList();
        bankMasterDetailsList.setBankMasterDetailsList(Collections.singletonList(new BankMasterDetails()));
        when(bankInfoDataService.getBankListInfoDataFromBankCodes(bankCodeList)).thenThrow(
                new MappingServiceClientException("")).thenReturn(null, bankMasterDetailsList);
        assertNull(merchantBankInfoDataService.getBankInfoDataListFromBankCodes(bankCodeList));
        assertNull(merchantBankInfoDataService.getBankInfoDataListFromBankCodes(bankCodeList));
        assertNotNull(merchantBankInfoDataService.getBankInfoDataListFromBankCodes(bankCodeList));

    }
}