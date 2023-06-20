/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import java.util.List;

import com.paytm.pgplus.theia.merchant.models.BankInfoData;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantBankInfoDataService {

    BankInfoData getBankInfo(String bankId);

    public List<BankInfoData> getBankInfoDataListFromBankCodes(List<String> bankCodeList);

}
