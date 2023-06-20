/**
 * 
 */
package com.paytm.pgplus.cashier.payoption;

import com.paytm.pgplus.cashier.enums.PaymentType;

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillGeneratorFactory {
    public IPayOptionBillGenerator getPayOptionBillGenerator(PaymentType paymentType) {

        switch (paymentType) {
        case ONLY_WALLET:
            return new PayOptionBillGeneratorWalletOnly();
        case HYBRID:
            return new PayOptionBillGeneratorHybrid();
        case ADDNPAY:
            return new PayOptionBillGeneratorAddNPay();
        case ONLY_PG:
            return new PayOptionBillGeneratorPGOnly();
        case ONLY_COD:
            return new PayOptionBillGeneratorCODOnly();
        case HYBRID_COD:
            return new PayOptionBillGeneratorHybrid();
        case OTHER:
        default:
            return new PayOptionBillGeneratorOther();
        }

    }

}
