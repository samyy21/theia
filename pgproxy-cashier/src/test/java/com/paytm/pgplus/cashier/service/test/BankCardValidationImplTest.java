package com.paytm.pgplus.cashier.service.test;

import com.paytm.pgplus.cashier.validator.service.impl.BankCardValidationImpl;

import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.service.IBankCardValidation;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

public class BankCardValidationImplTest {

    private BankCardValidationImpl bankCardValidationImpl = new BankCardValidationImpl();

    @Mocked
    CardUtils cardUtils;

    @Test
    public void testValidCard() throws CashierCheckedException, PaytmValidationException, IllegalAccessException {

        FieldUtils.writeField(bankCardValidationImpl, "cardUtils", cardUtils, true);

        new Expectations() {
            {
                cardUtils.validateCVV(anyString, anyString, anyString);
            }
        };

        try {
            final CompleteCardRequest completeCardRequest = new CompleteCardRequest(InstNetworkType.ISOCARD);

            BankCardRequest bankCardRequest = new BankCardRequest("4160210903741737", "000", "2028", "10", "DEBIT_CARD");

            BinDetail binDetail = new BinDetail();
            binDetail.setActive(true);
            binDetail.setBank("HDFC Bank");
            binDetail.setBankCode("HDFC");
            binDetail.setBin((long) 416021);
            binDetail.setCardName("VISA");
            binDetail.setCardType("DEBIT_CARD");
            binDetail.setIsIndian(true);
            final BinCardRequest binCardRequest = new BinCardRequest(binDetail);

            completeCardRequest.setBankCardRequest(bankCardRequest);
            completeCardRequest.setBinCardRequest(binCardRequest);
            completeCardRequest.setSavedDataRequest(false);
            completeCardRequest.setImpsCardRequest(null);

            bankCardValidationImpl.validateCard(completeCardRequest, binCardRequest);

        } catch (PaytmValidationException e) {
            Assert.fail("Valid Card test failed due to : " + e.getErrorMessage());
        }
    }

    @Test(expected = PaytmValidationException.class)
    public void testInvalidCard() throws PaytmValidationException, CashierCheckedException, IllegalAccessException {
        FieldUtils.writeField(bankCardValidationImpl, "cardUtils", cardUtils, true);

        final CompleteCardRequest completeCardRequest = new CompleteCardRequest(InstNetworkType.ISOCARD);
        BankCardRequest bankCardRequest = new BankCardRequest("4160210903741736", "000", "2028", "10", "DEBIT_CARD");

        BinDetail binDetail = new BinDetail();
        binDetail.setActive(true);
        binDetail.setBank("HDFC Bank");
        binDetail.setBankCode("HDFC");
        binDetail.setBin((long) 416021);
        binDetail.setCardName("VISA");
        binDetail.setCardType("DEBIT_CARD");
        binDetail.setIsIndian(true);
        final BinCardRequest binCardRequest = new BinCardRequest(binDetail);

        completeCardRequest.setBankCardRequest(bankCardRequest);
        completeCardRequest.setBinCardRequest(binCardRequest);
        completeCardRequest.setSavedDataRequest(false);
        completeCardRequest.setImpsCardRequest(null);
        completeCardRequest.setDirectInstService(false);

        bankCardValidationImpl.validateCard(completeCardRequest, binCardRequest);
    }

}
