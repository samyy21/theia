package com.paytm.pgplus.biz.fetchBulkCardIndexNumber;

import com.paytm.pgplus.biz.core.cachecard.utils.CacheCardInfoHelper;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.validator.service.ICardValidator;
import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("NativeFetchSavedCardIndexNumberHelper")
public class NativeFetchSavedCardIndexNumberHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchSavedCardIndexNumberHelper.class);
    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetFacade;

    @Autowired
    @Qualifier("seamlessvalidator")
    private ICardValidator seamlessvalidator;

    @Autowired
    private CoftUtil coftUtil;

    public CacheCardRequestBean mapSavedCardtoCacheCardRequestBean(CardBeanBiz cardBeanBiz) {
        CacheCardRequestBean cacheCardRequestBean = new CacheCardRequestBean.CacheCardRequestBeanBuilder(
                cardBeanBiz.getCardNumber(), cardBeanBiz.getInstNetworkType(), cardBeanBiz.getInstNetworkCode(),
                cardBeanBiz.getCardType(), cardBeanBiz.getCvv2(), cardBeanBiz.getCardScheme(),
                cardBeanBiz.getExpiryYear(), cardBeanBiz.getExpiryMonth()).build();
        return cacheCardRequestBean;
    }

    public CacheCardResponse callAPlusForCardIdxNo(CacheCardRequest cacheCardReq) {
        try {
            coftUtil.updateCacheCardRequest(cacheCardReq);
            return assetFacade.cacheCard(cacheCardReq);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured in cache card info in /fetchCardIndexNo: ", e);
        }
        return null;
    }

    public CacheCardRequest createCacheCardRequest(final CacheCardRequestBean cacheCardRequestBean) {
        GenericCoreResponseBean<CacheCardRequest> cacheCardReq = CacheCardInfoHelper
                .createCacheCardRequestForCardPayment(cacheCardRequestBean, InstNetworkType.ISOCARD);
        if (!cacheCardReq.isSuccessfullyProcessed()) {
            LOGGER.error("error in creating cacheCardRequest for card");
        }
        return cacheCardReq.getResponse();
    }

}
