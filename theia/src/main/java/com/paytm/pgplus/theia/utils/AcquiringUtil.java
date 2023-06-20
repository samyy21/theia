package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("acquiringUtil")
public class AcquiringUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcquiringUtil.class);

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private IAcquiringOrder acquiringOrder;

    public QueryByMerchantTransIdResponse queryByMerchantTransId(String mid, String orderId)
            throws FacadeCheckedException {

        // GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse
        // = merchantMappingService
        // .fetchMerchanData(mid);
        // String alipayId = null;
        // if ((merchantMappingResponse != null) &&
        // (merchantMappingResponse.getResponse() != null)) {
        // alipayId = merchantMappingResponse.getResponse().getAlipayId();
        // } else {
        // final String error = merchantMappingResponse == null ?
        // "Could not map merchant" : merchantMappingResponse
        // .getFailureMessage();
        // throw new PaymentRequestValidationException(error,
        // ResponseConstants.INVALID_MID);
        // }

        QueryByMerchantTransIdRequestBody requestBody = new QueryByMerchantTransIdRequestBody(mid, orderId, true);
        requestBody.setRoute(Routes.PG2);
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID);
        head.setMerchantId(mid);
        QueryByMerchantTransIdRequest request = new QueryByMerchantTransIdRequest(head, requestBody);
        QueryByMerchantTransIdResponse response = acquiringOrder.queryByMerchantTransId(request);
        return response;
    }

}
