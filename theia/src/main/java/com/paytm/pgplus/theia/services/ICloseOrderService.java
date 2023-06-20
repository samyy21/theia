package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.theia.enums.CloseOrderStatus;
import com.paytm.pgplus.theia.models.CancelTransRequest;
import com.paytm.pgplus.theia.models.CancelTransResponse;

/**
 * @author kartik
 * @date 06-07-2017
 */
public interface ICloseOrderService {

    public CancelTransResponse processCancelOrderRequest(final CancelTransRequest cancelTransRequest,
            final EnvInfoRequestBean envInfo);

    public CancelTransResponse generateCloseOrderResponse(CloseOrderStatus result);

}
