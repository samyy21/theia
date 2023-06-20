package com.paytm.pgplus.theia.services.upiAccount.impl;

import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import com.paytm.pgplus.theia.services.upiAccount.helper.FetchUpiOptionsServiceHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class FetchUpiOptionsServiceImplTest {

    @InjectMocks
    private FetchUpiOptionsServiceImpl fetchUpiOptionsService = new FetchUpiOptionsServiceImpl();

    @Mock
    private FetchUpiOptionsServiceHelper fetchUpiOptionsServiceHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testFetchUpiOptions() {
        FetchUpiOptionsRequest fetchUpiOptionsRequest = new FetchUpiOptionsRequest();
        when(fetchUpiOptionsServiceHelper.fetchUpiOptions(any())).thenReturn(new FetchUpiOptionsResponse());
        fetchUpiOptionsService.fetchUpiOptions(fetchUpiOptionsRequest);
    }
}