package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.QueryNonSensitiveAssetInfoRequest;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.facade.user.services.ICardInfoQueryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by charu on 28/01/20.
 *
 * The role of this class is to call platform's card center api only
 */

@Service("cardCenterHelper")
public class CardCenterHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(CardCenterHelper.class);

    @Autowired
    @Qualifier("cardInfoQueryServiceImpl")
    ICardInfoQueryService cardInfoQueryService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    MappingUtil mappingUtil;

    public QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfo(String cacheCardToken, String cardIndexNumber) {
        try {

            if (StringUtils.isBlank(cacheCardToken) && StringUtils.isBlank(cardIndexNumber)) {
                return null;
            }
            QueryNonSensitiveAssetInfoRequest request = new QueryNonSensitiveAssetInfoRequest(cardIndexNumber,
                    cacheCardToken);
            QueryNonSensitiveAssetInfoResponse response = cardInfoQueryService.queryNonSensitiveAssetInfo(request);
            if (!FacadeConstants.OpenAPIConstants.SUCCESS.equals(response.getStatus())) {
                return null;
            }
            return response;
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception fetching card details", e);
            return null;
        }
    }
}
