package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.SavedCardUtil;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.facade.merchant.enums.ContactBizTypeEnum;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.facade.merchant.models.request.QueryMerchantCustomerAssetsRequest;
import com.paytm.pgplus.facade.merchant.models.response.QueryMerchantCustomerAssetsResponse;
import com.paytm.pgplus.facade.merchant.services.IMerchantAsset;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

;

@Service
public class MerchantBizProdHelper {

    @Autowired
    @Qualifier("merchantAsset")
    private IMerchantAsset merchantAssetImpl;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private MerchantDataServiceImpl merchantDataService;

    @Autowired
    private SavedCardUtil savedCardUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantBizProdHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantBizProdHelper.class);

    public SavedAssetInfo getSavedCardByMidCustIdAndCardId(String savedCardId, String merchantId, String custId) {
        SavedAssetInfo savedAssetInfo = null;
        if (StringUtils.isNotBlank(custId) && StringUtils.isNotBlank(merchantId)) {
            try {
                MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(merchantId);
                EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
                QueryMerchantCustomerAssetsRequest request = new QueryMerchantCustomerAssetsRequest(
                        merchantInfo.getAlipayId(), custId);
                request.setContactBizType(ContactBizTypeEnum.MERCHANT_CUSTOMER_ASSET.getName());
                QueryMerchantCustomerAssetsResponse response = merchantAssetImpl.queryUserAssetsOnMidCustId(request);
                if (BizConstant.SUCCESS.equals(response.getStatus())) {
                    savedAssetInfo = savedCardUtil.fetchCardInfoFromQueryAsset(response.getAssetInfos(), savedCardId);
                    if (savedAssetInfo != null) {
                        LOGGER.info("card found in query by customer api");
                    }
                } else {
                    LOGGER.error("response status received in query by customer api is {}", response.getStatus());
                    return savedAssetInfo;

                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while fetching cardIndexNumber in query by customer api {}", e);
                return savedAssetInfo;

            }
        }
        return savedAssetInfo;
    }

}
