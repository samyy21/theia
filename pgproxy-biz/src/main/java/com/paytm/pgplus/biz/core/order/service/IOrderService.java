/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.order.service;

import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdResponseBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDRequestBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.BizCancelFundOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayRequestBean;
import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayResponseBean;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.QueryByAcquirementIdRequestBean;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequest;
import com.paytm.pgplus.facade.acquiring.models.response.OrderModifyResponse;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;

public interface IOrderService {

    GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUp(CreateTopUpRequestBizBean createTopUpRequestBean);

    GenericCoreResponseBean<BizCreateOrderResponse> createOrder(BizCreateOrderRequest createOrderRequest);

    GenericCoreResponseBean<BizCancelOrderResponse> closeOrder(BizCancelOrderRequest cancelAcquiringOrderRequest);

    GenericCoreResponseBean<BizCancelOrderResponse> closeFundOrder(
            final BizCancelFundOrderRequest cancelFundOrderRequest);

    GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay(
            CreateOrderAndPayRequestBean cacheCardRequest);

    GenericCoreResponseBean<QueryTransactionStatus> createQueryByAcquirementId(
            QueryByAcquirementIdRequestBean queryByAcquirementIdRequestBean);

    GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> createQueryByMerchantTransId(
            QueryByMerchantTransIDRequestBizBean queryByAcquirementIdRequestBean, boolean fromAoaMerchant);

    GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> createQueryByMerchantRequestId(
            QueryByMerchantRequestIdBizBean queryByMerchantRequestIdRequestBean);

    void modifyOrder(OrderModifyRequest createOrderRequest) throws FacadeCheckedException;
}
