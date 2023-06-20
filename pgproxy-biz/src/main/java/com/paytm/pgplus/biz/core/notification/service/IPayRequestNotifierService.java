package com.paytm.pgplus.biz.core.notification.service;

import com.paytm.pgplus.biz.core.model.request.BizPayRequest;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayRequestBean;

public interface IPayRequestNotifierService {

    public void pushPayRequestToKafkaTopic(BizPayRequest bizPayRequest);

    public void pushCopRequestToKafkaTopic(CreateOrderAndPayRequestBean createOrderAndPayRequestBean);

    public void pushPayloadToKafkaTopic(Object request);
}
