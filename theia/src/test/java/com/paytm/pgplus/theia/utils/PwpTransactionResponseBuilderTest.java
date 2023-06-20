package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.payloadvault.theia.response.ChildTransaction;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PwpTransactionResponseBuilderTest extends AOAUtilsTest {

    @InjectMocks
    PwpTransactionResponseBuilder pwpTransactionResponseBuilder;

    @Test
    public void testChangeTransactionResponse() {
        TransactionResponse response = new TransactionResponse();
        List<ChildTransaction> testList = new ArrayList<>();
        ChildTransaction childTransaction = new ChildTransaction();
        childTransaction.setBankTxnId("test");
        testList.add(childTransaction);
        response.setVpa("test");
        response.setBankTxnId("");
        response.setChildTxnList(testList);
        pwpTransactionResponseBuilder.changeTransactionResponse(response);
        Assert.assertNull(response.getVpa());
    }

}