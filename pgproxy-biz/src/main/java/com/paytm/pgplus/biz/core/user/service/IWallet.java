/**
 * 
 */
package com.paytm.pgplus.biz.core.user.service;

import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author naman
 *
 */
public interface IWallet {

    GenericCoreResponseBean<Money> fetchWalletBalance(String userId, String adminToken, String orderId);

    GenericCoreResponseBean<Money> fetchWalletBalance(String userId, String token, String orderId, boolean isAdmin);

}
