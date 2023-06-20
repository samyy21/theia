/**
 * 
 */
package com.paytm.pgplus.theia.cache.impl;

import java.util.List;

import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.cache.model.BankMasterDetailsList;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;

/**
 * @author namanjain
 *
 */
@Service("merchantBankInfoDataService")
public class MerchantBankInfoDataServiceImpl implements IMerchantBankInfoDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantBankInfoDataServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantBankInfoDataServiceImpl.class);

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    private IBankInfoDataService bankInfoDataService;

    @Override
    public BankInfoData getBankInfo(final String bankId) {
        try {
            BankMasterDetails bankMasterDetails = bankInfoDataService.getBankInfoData(bankId);
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetails :: {}", bankMasterDetails);

            if (bankMasterDetails != null) {
                return JsonMapper.convertValue(bankMasterDetails, BankInfoData.class);
            }
            LOGGER.warn("Null response returned from mapping service for bankId : {}", bankId);
            return null;
        } catch (Exception errorCause) {
            LOGGER.error("BankInfoData not found for : {}, from MS, Reason : {}", bankId, errorCause.getMessage());
            return null;
        }
    }

    @Override
    public List<BankInfoData> getBankInfoDataListFromBankCodes(List<String> bankCodeList) {
        long time = System.currentTimeMillis();
        try {
            LOGGER.debug("Number of bank codes send to mapping service is : {}", bankCodeList.size());
            BankMasterDetailsList bankMasterDetails = bankInfoDataService
                    .getBankListInfoDataFromBankCodes(bankCodeList);
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetailsList :: {}", bankMasterDetails);
            if (bankMasterDetails != null) {
                LOGGER.warn("No BankInfoData found for {} bankCodes from mapping service",
                        bankMasterDetails.getNotFound());
                if (CollectionUtils.isNotEmpty(bankMasterDetails.getBankMasterDetailsList())) {
                    List<BankInfoData> list = JsonMapper.convertValue(bankMasterDetails.getBankMasterDetailsList(),
                            new TypeReference<List<BankInfoData>>() {
                            });
                    LOGGER.debug("Number of BankInfoData received from mapping service is : {}", list.size());
                    return list;
                }
            }
            LOGGER.warn("No BankInfoData response returned from mapping service for bankCodeList : {}", bankCodeList);
            return null;
        } catch (Exception errorCause) {
            LOGGER.error("Unable to find  BankMasterDetailsList for : {}, from MS, Reason : {}", bankCodeList,
                    errorCause.getMessage());
            return null;
        } finally {
            LOGGER.debug("fetching BankInfoData List  api took {} milliseconds", System.currentTimeMillis() - time);
        }
    }

}
