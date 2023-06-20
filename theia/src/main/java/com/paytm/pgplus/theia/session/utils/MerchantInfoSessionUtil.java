/**
 * 
 */
package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.LinkBasedMerchantInfo;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoProvider;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.LINK_BASED_KEY;

/**
 * @author amit.dubey
 *
 */
@Component("merchantInfoSessionUtil")
public class MerchantInfoSessionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("merchantUrlService")
    private IMerchantUrlService merchantUrlService;

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    @Qualifier("merchantExtendInfoProvider")
    private MerchantExtendInfoProvider merchantExtendInfoProvider;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    public void setMerchantInfoIntoSession(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        LOGGER.debug("WorkFLowResponseBeana :{}", responseData);
        final MerchantInfo merchantInfo = theiaSessionDataService.getMerchantInfoFromSession(requestData.getRequest(),
                true);
        LOGGER.debug("Found data in session, merchantInfo :{}", merchantInfo);

        merchantInfo.setMerchantStoreCardPref(merchantPreferenceService.isStoreCardEnabledForMerchant(requestData
                .getMid()));

        String website = StringUtils.isNotBlank(requestData.getWebsite()) ? requestData.getWebsite() : responseData
                .getExtendedInfo() != null ? responseData.getExtendedInfo().get("website") : null;
        String mccCode = StringUtils.isNotBlank(requestData.getIndustryTypeId()) ? requestData.getIndustryTypeId()
                : responseData.getExtendedInfo() != null ? responseData.getExtendedInfo().get("mccCode") : null;
        merchantInfo.setMerchantCategoryCode(mccCode);
        merchantInfo.setMid(requestData.getMid());
        MerchantUrlInput input = new MerchantUrlInput(requestData.getMid(), MappingMerchantUrlInfo.UrlTypeId.RESPONSE,
                website);

        String pickLogoFromNewLocation = ConfigurationUtil.getProperty("useNewImagePathForMerchant", "false");
        boolean isLogoAvailableOnNewLocation = merchantPreferenceService.isLogoAvailableOnNewLocation(requestData
                .getMid());
        LOGGER.info("value of flag useNewImagePathForMerchant : {} and preference isLogoAvailableOnNewLocation : {}",
                pickLogoFromNewLocation, isLogoAvailableOnNewLocation);
        MappingMerchantUrlInfo merchantUrlInfo = null;
        try {
            if ("true".equalsIgnoreCase(pickLogoFromNewLocation) && isLogoAvailableOnNewLocation) {
                merchantUrlInfo = merchantUrlService.getMerchantUrlInfoV2(input);
                merchantInfo.setUseNewImagePath(true);
            } else {
                merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                merchantInfo.setUseNewImagePath(false);
            }
            if (merchantUrlInfo != null) {
                merchantInfo.setMerchantImage(merchantUrlInfo.getImageName());
            }
        } catch (Exception e) {
            LOGGER.info("Merchant URL Info not recieved from mapping-service");
        }

        merchantInfo.setMerchantName(merchantExtendInfoUtils.getMerchantName(requestData.getMid()));
        String internalMid = StringUtils.EMPTY;

        if (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(requestData.getRequestType())
                || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(requestData.getRequestType())
                || StringUtils.isNotBlank(requestData.getSubsLinkId())) {
            LOGGER.info("Setting merchant info in redis for Link based payment for MID = {}", merchantInfo.getMid());
            String imageName = merchantInfo.getMerchantImage();
            if (!merchantInfo.isUseNewImagePath()) {
                imageName = PaytmTLD.getStaticUrlPrefix() + "merchantInfo/" + merchantInfo.getMerchantImage();
            }
            LinkBasedMerchantInfo linkBasedMerchantInfo = new LinkBasedMerchantInfo(merchantInfo.getMid(),
                    merchantInfo.getMerchantName(), imageName);
            theiaTransactionalRedisUtil.set(LINK_BASED_KEY + responseData.getTransID(), linkBasedMerchantInfo);
        }

        try {
            MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(requestData
                    .getMid());

            if (mappingMerchantData != null) {
                internalMid = mappingMerchantData.getAlipayId();
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Exception occured while fetching merchant mapping data from Redis/Mapping Service for merchantId : {}",
                    requestData.getMid(), e);
        }
        merchantInfo.setInternalMid(internalMid);
        merchantInfo.setNumberOfRetries(merchantExtendInfoUtils.getNumberOfRetries(requestData.getMid()));
    }

    public MerchantInfo getMerchantInfo(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean) {
        MerchantInfo merchantInfo = new MerchantInfo();

        try {
            merchantInfo.setMid(requestData.getMid());
            merchantInfo.setMerchantName(merchantExtendInfoProvider.getMerchantName(requestData));

            MerchantUrlInput input = getMerchantUrlInput(requestData.getMid(),
                    MappingMerchantUrlInfo.UrlTypeId.RESPONSE, getWebsite(requestData, workFlowResponseBean));

            String pickLogoFromNewLocation = ConfigurationUtil.getProperty("useNewImagePathForMerchant", "false");
            boolean isLogoAvailableOnNewLocation = merchantPreferenceProvider.isLogoAvailableOnNewLocation(requestData);
            MappingMerchantUrlInfo merchantUrlInfo = null;
            if ("true".equalsIgnoreCase(pickLogoFromNewLocation) && isLogoAvailableOnNewLocation) {
                merchantUrlInfo = merchantUrlService.getMerchantUrlInfoV2(input);
                merchantInfo.setUseNewImagePath(true);
            } else {
                merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                merchantInfo.setUseNewImagePath(false);
            }

            if (merchantUrlInfo != null) {
                merchantInfo.setMerchantImage(merchantUrlInfo.getImageName());
            }
        } catch (Exception e) {
            LOGGER.error("Error in getting merchantInfo");
        }
        return merchantInfo;
    }

    private MerchantUrlInput getMerchantUrlInput(String mid, MappingMerchantUrlInfo.UrlTypeId urlTypeId, String website) {
        return new MerchantUrlInput(mid, MappingMerchantUrlInfo.UrlTypeId.RESPONSE, website);
    }

    private String getWebsite(PaymentRequestBean requestData, WorkFlowResponseBean responseData) {
        return StringUtils.isNotBlank(requestData.getWebsite()) ? requestData.getWebsite() : responseData
                .getExtendedInfo() != null ? responseData.getExtendedInfo().get("website") : null;
    }
}
