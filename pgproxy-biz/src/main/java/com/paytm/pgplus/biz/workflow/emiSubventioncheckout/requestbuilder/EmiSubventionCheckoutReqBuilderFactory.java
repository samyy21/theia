package com.paytm.pgplus.biz.workflow.emiSubventioncheckout.requestbuilder;

import com.paytm.pgplus.biz.exception.BizPaymentOfferCheckoutException;
import com.paytm.pgplus.biz.utils.BinRestrictedCard;
import com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder.BaseCheckoutPaymentPromoReqBuilder;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.consume.enums.SettleStatus;
import com.paytm.pgplus.facade.emisubvention.models.*;
import com.paytm.pgplus.facade.emisubvention.models.request.CheckoutRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.CheckoutWithOrderRequest;
import com.paytm.pgplus.facade.emisubvention.models.request.OrderStampRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.CheckOutResponse;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitAccumulateVo;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmiSubventionCheckoutReqBuilderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmiSubventionCheckoutReqBuilderFactory.class);

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    public CheckoutRequest getSubventionCheckoutReq(WorkFlowRequestBean workFlowRequestBean) {
        CheckoutRequest checkoutRequest = new CheckoutRequest(
                workFlowRequestBean.getEmiSubventionValidateRequestData(),
                workFlowRequestBean.getEmiSubventionOfferCheckoutReqData());
        return checkoutRequest;
    }

    public CheckoutWithOrderRequest getSubventionCheckoutWithOrderReq(WorkFlowRequestBean workFlowRequestBean) {
        return new CheckoutWithOrderRequest(workFlowRequestBean.getEmiSubventionValidateRequestData(),
                workFlowRequestBean.getEmiSubventionOfferCheckoutReqData());
    }

    public OrderStampRequest getSubventionOrderStampRequest(WorkFlowRequestBean workFlowRequestBean,
            CheckoutRequest checkoutRequest, CheckOutResponse checkOutResponse) {
        List<Item> items = checkoutRequest.getItems();
        List<ItemBreakUp> orderStampItemBreakUpList = new ArrayList<>();
        List<ItemBreakUp> checkoutItemBreakUpList = checkOutResponse.getItemBreakUp();
        StringBuilder sb = new StringBuilder("");
        for (ItemBreakUp itemBreakUp : checkoutItemBreakUpList) {

            if (itemBreakUp.getSubventionType() != null) {
                Item item = workFlowRequestBean.getEmiSubventionValidateRequestData().getItems().stream()
                        .filter(a -> a.getId().equals(itemBreakUp.getId())).collect(Collectors.toList()).get(0);
                itemBreakUp.setItemDetails(item);
                itemBreakUp.setOrderItemId(item.getId());
                orderStampItemBreakUpList.add(itemBreakUp);
                sb.append(item.getId()).append("|");
            }
        }
        OrderStampRequest orderStampRequest = new OrderStampRequest(checkoutRequest, checkOutResponse);
        orderStampRequest.setItemBreakUp(orderStampItemBreakUpList);
        workFlowRequestBean.setSubventionOrderList(sb.toString());
        return orderStampRequest;
    }

    public OrderStampRequest getSubventionCheckoutOrderStampRequest(WorkFlowRequestBean workFlowRequestBean,
            CheckoutWithOrderRequest checkoutRequest, CheckOutResponse checkOutResponse) {
        List<ItemBreakUp> orderStampItemBreakUpList = new ArrayList<>();
        List<ItemBreakUp> checkoutItemBreakUpList = checkOutResponse.getItemBreakUp();
        StringBuilder sb = new StringBuilder("");
        for (ItemBreakUp itemBreakUp : checkoutItemBreakUpList) {

            if (itemBreakUp.getSubventionType() != null) {
                Item item = workFlowRequestBean.getEmiSubventionValidateRequestData().getItems().stream()
                        .filter(a -> a.getId().equals(itemBreakUp.getId())).collect(Collectors.toList()).get(0);
                itemBreakUp.setItemDetails(item);
                itemBreakUp.setOrderItemId(item.getId());
                orderStampItemBreakUpList.add(itemBreakUp);
                sb.append(item.getId()).append("|");
            }
        }
        OrderStampRequest orderStampRequest = new OrderStampRequest(checkoutRequest, checkOutResponse);
        orderStampRequest.setItemBreakUp(orderStampItemBreakUpList);
        workFlowRequestBean.setSubventionOrderList(sb.toString());
        return orderStampRequest;
    }
}
