/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.order.service.impl;

import com.paytm.pgplus.biz.core.looper.service.ILooperService;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantRequestIdResponseBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDRequestBizBean;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.order.utils.OrderHelper;
import com.paytm.pgplus.biz.enums.BuyerUserIdEnum;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.acquiring.models.request.*;
import com.paytm.pgplus.facade.acquiring.models.response.*;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.MerchantBlockedException;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequest;
import com.paytm.pgplus.facade.fund.models.request.CreateTopupFromMerchantRequest;
import com.paytm.pgplus.facade.fund.models.request.FundUserOrderQueryByMerchantRequestIdRequest;
import com.paytm.pgplus.facade.fund.models.response.CloseFundResponse;
import com.paytm.pgplus.facade.fund.models.response.CreateTopupFromMerchantResponse;
import com.paytm.pgplus.facade.fund.models.response.FundUserOrderQueryByMerchantRequestIdResponse;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author namanjain
 *
 */
@Service("orderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    @Qualifier("topupImpl")
    private ITopup topUpFacade;

    @Autowired
    private IAcquiringOrder acquiringOrder;

    @Autowired
    private ITopup topupImpl;

    @Autowired
    @Qualifier("looperservice")
    private ILooperService looperService;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_CREATE_ORDER)
    @Override
    public GenericCoreResponseBean<BizCreateOrderResponse> createOrder(final BizCreateOrderRequest request) {
        try {
            final CreateOrderRequest createOrderRequest = OrderHelper.getCreateOrderRequest(request);
            if (null != request.getExtendInfo() && StringUtils.isNotBlank(request.getExtendInfo().getPaytmMerchantId())) {
                createOrderRequest.getHead().setMerchantId(request.getExtendInfo().getPaytmMerchantId());
            }
            final CreateOrderResponse createOrderResponse = acquiringOrder
                    .createOrderAfterMerchantCheck(createOrderRequest);
            Assert.notNull(createOrderResponse, "Response Received from Create Order is Null");
            return OrderHelper.verifyAndReturnResponse(createOrderResponse);
        } catch (MerchantBlockedException mbe) {
            LOGGER.error("Unable to create order as merchant is blocked");
            return new GenericCoreResponseBean<BizCreateOrderResponse>(mbe.getMessage(),
                    ResponseConstants.MERCHANT_BLOCKED);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred while creating order : ", e);
            return new GenericCoreResponseBean<BizCreateOrderResponse>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_CREATE_ORDER_AND_PAY)
    @Override
    public GenericCoreResponseBean<CreateOrderAndPayResponseBean> createOrderAndPay(
            final CreateOrderAndPayRequestBean createOrderAndPayRequest) {
        try {
            final CreateOrderAndPayRequest createOrderAndPay = OrderHelper
                    .createOrderAndPayRequest(createOrderAndPayRequest);
            if (null != createOrderAndPayRequest.getExtendInfo()
                    && StringUtils.isNotBlank(createOrderAndPayRequest.getExtendInfo().getPaytmMerchantId())) {
                createOrderAndPay.getHead()
                        .setMerchantId(createOrderAndPayRequest.getExtendInfo().getPaytmMerchantId());
            }

            if (!createOrderAndPayRequest.isWillUserChange()) {
                createOrderAndPay.getBody().setWhetherBuyerUserIdChange(BuyerUserIdEnum.FALSE.getValue());
            }
            final CreateOrderAndPayResponse createOrderAndPayResponse = acquiringOrder
                    .createOrderAndPayAfterMerchantCheck(createOrderAndPay);
            return OrderHelper.verifyAndReturnResponse(createOrderAndPayResponse, createOrderAndPayRequest);
        } catch (MerchantBlockedException mbe) {
            LOGGER.error("Exception Occurred while creating order : {}", mbe.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.MERCHANT_BLOCKED.getAlipayResultCode(),
                    mbe.getMessage(), TheiaConstant.ExtraConstants.ACQUIRING_ORDER_CREATEORDER_AND_PAY, false);
            return new GenericCoreResponseBean<>(mbe.getMessage(), ResponseConstants.MERCHANT_BLOCKED);
        } catch (final MerchantLimitBreachedException e) {
            LOGGER.error("MerchantLimitBreachedException occurred in Create Order And Pay: ",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(),
                    TheiaConstant.ExtraConstants.ACQUIRING_ORDER_CREATEORDER_AND_PAY, false);
            throw e;
        } catch (FacadeCheckedException e) {
            LOGGER.error("FacadeCheckedException occurred in Create Order And Pay: ",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.SYSTEM_ERROR.getAlipayResultCode(),
                    e.getMessage(), TheiaConstant.ExtraConstants.ACQUIRING_ORDER_CREATEORDER_AND_PAY, false);
            return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        } catch (final Exception e) {
            LOGGER.error("Exception occurred in Create Order And Pay: ", e);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.SYSTEM_ERROR.getAlipayResultCode(),
                    e.getMessage(), TheiaConstant.ExtraConstants.ACQUIRING_ORDER_CREATEORDER_AND_PAY, false);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_QUERY_BY_ID)
    @Override
    public GenericCoreResponseBean<QueryTransactionStatus> createQueryByAcquirementId(
            final QueryByAcquirementIdRequestBean queryByAcquirementIdRequestBean) {
        try {
            final QueryByAcquirementIdRequest queryByAcquirementIdRequest = OrderHelper
                    .createQueryByAcquirementIdRequest(queryByAcquirementIdRequestBean,
                            queryByAcquirementIdRequestBean.getRoutes());
            final QueryByAcquirementIdResponse queryByAcquirementIdResponse = looperService
                    .fetchTransactionStatus(queryByAcquirementIdRequest);
            Assert.notNull(queryByAcquirementIdResponse, "Looper Response from Fetch Transaction Status Is Null");
            return OrderHelper.verifyAndReturnResponse(queryByAcquirementIdResponse);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while Query_By_AcquirementId: ", e);
            return new GenericCoreResponseBean<QueryTransactionStatus>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_CLOSE_ORDER)
    @Override
    public GenericCoreResponseBean<BizCancelOrderResponse> closeOrder(
            final BizCancelOrderRequest cancelAcquiringOrderRequest) {
        try {
            final CloseRequest closeRequest = OrderHelper.getCloseRequest(cancelAcquiringOrderRequest);
            closeRequest.getBody().setRoute(cancelAcquiringOrderRequest.getRoute());
            closeRequest.getBody().setPaytmMerchantId(cancelAcquiringOrderRequest.getPaytmMerchantId());
            final CloseResponse closeResponse = acquiringOrder.closeOrder(closeRequest);
            Assert.notNull(closeResponse, "Response received from Close Order is Null");
            return OrderHelper.verifyAndReturnResponse(closeResponse);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred while closing acquiring order :: ,", e);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.FUND_CLOSE_ORDER)
    @Override
    public GenericCoreResponseBean<BizCancelOrderResponse> closeFundOrder(
            final BizCancelFundOrderRequest cancelFundOrderRequest) {
        try {
            final CloseFundRequest closeFundRequest = OrderHelper.getCloseFundRequest(cancelFundOrderRequest);
            closeFundRequest.getBody().setRoute(Routes.PG2);
            closeFundRequest.getBody().setPaytmMerchantId(cancelFundOrderRequest.getPaytmMerchantId());
            final CloseFundResponse closeFundResponse = topUpFacade.closeFundOrder(closeFundRequest);
            return OrderHelper.verifyAndReturnResponse(closeFundResponse);
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred while closing fund order :: ,", e);
            return new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    /**
     *
     * @Description: underline implementation has been change for api version
     *               1.1.4 now we will call createTopupFromMerchant api for
     *               create topup.
     * @Modified :
     *
     * */
    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_CREATE_TOPUP)
    @Override
    public GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUp(
            final CreateTopUpRequestBizBean createTopUpRequestBean) {
        try {
            final GenericCoreResponseBean<CreateTopupFromMerchantRequest> createTopupRequest = OrderHelper
                    .createTopUpFromMerchantRequest(createTopUpRequestBean);
            Assert.isTrue(createTopupRequest.isSuccessfullyProcessed(),
                    "Facade Topup Request from Merchant Creation Failed");

            if (null != createTopUpRequestBean.getExtInfoReqBean()
                    && StringUtils.isNotBlank(createTopUpRequestBean.getExtInfoReqBean().getPaytmMerchantId())) {
                createTopupRequest.getResponse().getHead()
                        .setMerchantId(createTopUpRequestBean.getExtInfoReqBean().getPaytmMerchantId());
            }
            final CreateTopupFromMerchantResponse topUpResponse = topUpFacade
                    .createTopupFromMerchant(createTopupRequest.getResponse());
            Assert.notNull(topUpResponse, "TopupFromMerchantResponse Received is null");
            return OrderHelper.verifyAndReturnResponse(topUpResponse);
        } catch (final Exception ex) {
            LOGGER.error("Exception in creating topup : {}", ex);
            return new GenericCoreResponseBean<CreateTopUpResponseBizBean>(ex.getMessage(),
                    ResponseConstants.SYSTEM_ERROR);
        }

    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.ACQUIRING_QUERY_BY_MERCHANT_TRANS_ID)
    @Override
    public GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> createQueryByMerchantTransId(
            QueryByMerchantTransIDRequestBizBean queryByAcquirementIdRequestBean, boolean fromAoaMerchant) {
        try {
            final QueryByMerchantTransIdRequest queryByMerchantTransIdRequest = OrderHelper
                    .createQueryByMerchantTransIdRequest(queryByAcquirementIdRequestBean, fromAoaMerchant);
            queryByMerchantTransIdRequest.getBody().setRoute(queryByAcquirementIdRequestBean.getRoute());

            final QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = acquiringOrder
                    .queryByMerchantTransId(queryByMerchantTransIdRequest);
            Assert.notNull(queryByMerchantTransIdResponse, "Response Received from Query by Merchant Trans Id is Null");
            return OrderHelper.verifyAndReturnResponse(queryByMerchantTransIdResponse);
        } catch (Exception ex) {
            LOGGER.error("Exception in creating topup : {}", ex);
            return new GenericCoreResponseBean<>(ex.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.FUND_QUERY_BY_MERCHANT_REQUEST_ID)
    @Override
    public GenericCoreResponseBean<QueryByMerchantRequestIdResponseBizBean> createQueryByMerchantRequestId(
            QueryByMerchantRequestIdBizBean queryByMerchantRequestIdRequestBean) {
        try {
            FundUserOrderQueryByMerchantRequestIdRequest queryByMerchantRequestIdRequest = OrderHelper
                    .createQueryByMerchantRequestIdRequest(queryByMerchantRequestIdRequestBean);
            FundUserOrderQueryByMerchantRequestIdResponse queryByMerchantRequestIdResponse = topupImpl
                    .queryByFundUserOrderId(queryByMerchantRequestIdRequest);
            Assert.notNull(queryByMerchantRequestIdResponse,
                    "Respose Received from Query by Merchant Request Id is Null");
            return OrderHelper.verifyAndReturnResponse(queryByMerchantRequestIdResponse);
        } catch (Exception ex) {
            LOGGER.error("Exception in query by merchant request id : {}", ex);
            return new GenericCoreResponseBean<>(ex.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Override
    public void modifyOrder(OrderModifyRequest orderModifyRequest) throws FacadeCheckedException {
        acquiringOrder.orderModify(orderModifyRequest);
    }

}