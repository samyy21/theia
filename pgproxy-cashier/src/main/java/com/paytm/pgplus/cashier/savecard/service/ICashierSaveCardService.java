/**
 * 
 */
package com.paytm.pgplus.cashier.savecard.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.pay.service.model.CashierUserCard;

/**
 * @author amit.dubey
 *
 */
public interface ICashierSaveCardService {

    boolean saveCard(CashierUserCard card) throws CashierCheckedException;

}
