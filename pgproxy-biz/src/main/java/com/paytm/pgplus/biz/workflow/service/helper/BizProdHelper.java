package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.SavedCardUtil;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.facade.merchant.enums.ContactBizTypeEnum;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.facade.user.models.request.QueryUserAssetsWithFilterRequest;
import com.paytm.pgplus.facade.user.models.response.QueryUserAssetsWithFilterResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.impl.UserMappingServiceImpl;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("BizProdHelper")
public class BizProdHelper {

    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetImpl;

    @Autowired
    @Qualifier("userMappingService")
    private UserMappingServiceImpl userMappingService;

    @Autowired
    private SavedCardUtil savedCardUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizProdHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BizProdHelper.class);

    public SavedAssetInfo getSavedCardByUserIdAndCardId(String savedCardId, String userId) {
        SavedAssetInfo savedAssetInfo = null;
        if (StringUtils.isNotBlank(userId)) {
            try {
                UserInfo userInfo = userMappingService.getAlipayUserMapping(userId,
                        UserDataMappingInput.UserOwner.PAYTM.name());
                EXT_LOGGER.customInfo("Mapping response - UserInfo :: {}", userInfo);
                if (userInfo == null) {
                    LOGGER.info("userInfo is null from mapping service for userId {}", userId);
                    return savedAssetInfo;
                }
                QueryUserAssetsWithFilterRequest request = new QueryUserAssetsWithFilterRequest();
                request.setUserId(userInfo.getAlipayId());
                request.setContactBizType(ContactBizTypeEnum.PAYMENT_ASSET.getName());
                QueryUserAssetsWithFilterResponse response = assetImpl.queryUserAssets(request);
                if (BizConstant.SUCCESS.equals(response.getStatus())) {
                    savedAssetInfo = savedCardUtil.fetchCardInfoFromQueryAsset(response.getAssetInfos(), savedCardId);
                    if (savedAssetInfo != null) {
                        LOGGER.info("card found in query by filter api");
                    }
                } else {
                    LOGGER.error("response status received in query by filter api is {}", response.getStatus());
                    return savedAssetInfo;
                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while fetching cardIndexNumber in query by filter api {}", e);
                return savedAssetInfo;

            }
        }
        return savedAssetInfo;
    }

}
