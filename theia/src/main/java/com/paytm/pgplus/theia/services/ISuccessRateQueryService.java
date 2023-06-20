package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.facade.fluxnet.models.request.SuccessRateQueryRequest;
import com.paytm.pgplus.facade.fluxnet.models.response.SuccessRateQueryResponse;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.models.SuccessRateQueryRequestBean;
import com.paytm.pgplus.theia.models.response.SuccessRateQueryResponseBean;

/**
 * @author kartik
 * @date 09-03-2017
 */
public interface ISuccessRateQueryService {

    SuccessRateQueryResponseBean getSuccessRatesForPayMethod(SuccessRateQueryRequestBean successRateQueryRequest);

    SuccessRateQueryResponseBean getSuccessRatesForPayMethod();

    SuccessRateQueryResponseBean mapFacadeResponseToTheiaResponseModel(
            SuccessRateQueryResponse facadeSuccessRateResponse);

    SuccessRateQueryRequest mapRequestBeanToFacadeRequest(SuccessRateQueryRequestBean successRateQueryRequest);

    SuccessRateCacheModel getSuccessRateModelFromCache();

    void setSuccessRateModelInCache(SuccessRateCacheModel successRatesCacheModel);

    SuccessRateCacheModel checkAndFetchSuccessRateModel();

}
