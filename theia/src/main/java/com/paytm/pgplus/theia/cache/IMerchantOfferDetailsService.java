/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsInput;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsResponse;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantOfferDetailsService {

    MerchantOfferDetailsResponse getMerchantOfferDetails(MerchantOfferDetailsInput merchantUrlInput);

}
