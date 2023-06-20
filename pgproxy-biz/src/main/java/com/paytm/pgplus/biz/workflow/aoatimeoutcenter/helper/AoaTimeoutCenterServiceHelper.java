package com.paytm.pgplus.biz.workflow.aoatimeoutcenter.helper;

import com.paytm.pgplus.biz.core.merchant.service.IMerchantMappingService;
import com.paytm.pgplus.biz.workflow.aoatimeoutcenter.service.impl.AoaTimeoutCenterService;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderRequest;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderResponse;
import com.paytm.pgplus.biz.workflow.model.OrderInfo;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.ContractStatus;
import com.paytm.pgplus.facade.acquiring.models.Order;
import com.paytm.pgplus.facade.boss.models.response.ContractDetailsQueryResponseBody;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PaymentOption;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantQueryService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("aoaTimeoutCenterServiceHelper")
public class AoaTimeoutCenterServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AoaTimeoutCenterServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(AoaTimeoutCenterServiceHelper.class);

    @Autowired
    private AoaTimeoutCenterService aoaTimeoutCenterService;

    @Autowired
    @Qualifier("merchantQueryServiceImpl")
    private IMerchantQueryService merchantQueryService;

    public void persistOrderInfoAtAoaTimeoutCenter(WorkFlowRequestBean workFlowRequestBean, String acquirementId)
            throws FacadeCheckedException {
        AoaTimeoutCenterOrderRequest aoaTimeoutCenterOrderRequest = prepareAoaTimeoutCenterOrderRequest(
                workFlowRequestBean, acquirementId);

        // calling aoa timeout center api
        try {
            // LOGGER.info("Calling aoa timeout center");
            AoaTimeoutCenterOrderResponse aoaTimeoutCenterOrderResponse = aoaTimeoutCenterService
                    .persistOrderInfoAtAoaTimeoutCenter(aoaTimeoutCenterOrderRequest, new HashMap<>());
            if (aoaTimeoutCenterOrderResponse != null && aoaTimeoutCenterOrderResponse.getResponse() != null
                    && aoaTimeoutCenterOrderResponse.getResponse().get(0) != null
                    && aoaTimeoutCenterOrderResponse.getResponse().get(0).getResultInfo().getResultCode().equals("S")) {
                LOGGER.info("Orderinfo at aoa timeoutcenter is successfully saved");
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private AoaTimeoutCenterOrderRequest prepareAoaTimeoutCenterOrderRequest(WorkFlowRequestBean workFlowRequestBean,
            String acquirementId) {
        AoaTimeoutCenterOrderRequest aoaTimeoutCenterOrderRequest = new AoaTimeoutCenterOrderRequest();
        OrderInfo orderInfo = new OrderInfo();
        List<OrderInfo> orderInfoList = new ArrayList<>();
        Integer orderTimeout = null;

        orderInfo.setMid(workFlowRequestBean.getPaytmMID());
        orderInfo.setAlipayMid(workFlowRequestBean.getAlipayMID());
        orderInfo.setOrderId(workFlowRequestBean.getOrderID());
        orderInfo.setPayOption(workFlowRequestBean.getPayOption());
        orderInfo.setPayMethod(workFlowRequestBean.getPayMethod());
        orderInfo.setAcquirementId(acquirementId);
        orderInfo.setStatus("INIT");
        try {
            ContractDetailsQueryResponseBody contractDetailsQueryResponseBody = merchantQueryService.getContractDetail(
                    workFlowRequestBean.getPaytmMID(), ContractStatus.EFFECTIVE,
                    ProductCodes.AOADirectPayAcquiringProd.getId());
            EXT_LOGGER.customInfo("Mapping response - ContractDetailsQueryResponseBody :: {}",
                    contractDetailsQueryResponseBody);
            if (contractDetailsQueryResponseBody != null
                    && contractDetailsQueryResponseBody.getProductCondition() != null) {
                orderTimeout = contractDetailsQueryResponseBody.getProductCondition().getOrderTimeout();
                LOGGER.info("orderTimeout {}", orderTimeout);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching merchant contract info for merchantId : {}",
                    workFlowRequestBean.getPaytmMID(), e);
        }

        if (orderTimeout != null) {
            orderInfo.setOrderExpiry(Timestamp.from(ZonedDateTime.now().plusSeconds(orderTimeout * 60).toInstant())
                    .getTime());
        }
        orderInfoList.add(orderInfo);
        aoaTimeoutCenterOrderRequest.setOrderInfo(orderInfoList);
        return aoaTimeoutCenterOrderRequest;
    }
}
