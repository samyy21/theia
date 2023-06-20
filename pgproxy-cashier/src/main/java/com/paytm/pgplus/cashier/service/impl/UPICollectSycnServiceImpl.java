package com.paytm.pgplus.cashier.service.impl;

import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierLopperMapper;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.CashierResponseCodeDetails;
import com.paytm.pgplus.cashier.models.validator.IResponseValidator;
import com.paytm.pgplus.cashier.service.IUPICollectSyncService;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fund.models.FundOrder;
import com.paytm.pgplus.facade.fund.models.response.QueryByFundOrderIdResponse;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;

@Component("UPICollectSycnServiceImpl")
public class UPICollectSycnServiceImpl implements IUPICollectSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPICollectSycnServiceImpl.class);

    @Autowired
    private IFacadeService facadeServiceImpl;

    @Autowired
    private CashierUtilService cashierUtilService;

    @Autowired
    private IResponseValidator<PayResultQueryResponse> payResultQueryResponseValidator;

    @Autowired
    private IResponseValidator<QueryByFundOrderIdResponse> queryByFundOrderIdResponseValidator;

    public CashierPaymentStatus fetchPaymentStatus(final String acquirementId, final CashierRequest cashierRequest)
            throws CashierCheckedException {
        try {
            // TODO: to be removed in future, require for testing purpose
            LOGGER.info("Received Request, Not in Looper service:{},{}", acquirementId, cashierRequest);
            PayResultQueryResponse payResultQueryResponse = facadeServiceImpl
                    .fetchPayResultQueryResponse(cashierRequest);
            payResultQueryResponseValidator.validInput(payResultQueryResponse);
            LOGGER.debug("PayResultQueryResponse:{}", payResultQueryResponse);

            ResultInfo resultInfo = payResultQueryResponse.getBody().getResultInfo();
            LOGGER.trace("ResultInfo:{}", resultInfo);

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                LOGGER.trace("PaymentStatus :{}", payResultQueryResponse.getBody().getPaymentStatus());
                if (PaymentStatus.FAIL == payResultQueryResponse.getBody().getPaymentStatus()) {
                    CashierResponseCodeDetails cashierResponseCodeDetails = cashierUtilService
                            .getMerchantResponseCode(payResultQueryResponse.getBody().getInstErrorCode());

                    if (cashierResponseCodeDetails.isRetry()) {
                        return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody(),
                                cashierResponseCodeDetails);
                    }
                    if (cashierRequest.isFundOrder()) {
                        String fundOrderId = cashierRequest.getLooperRequest().getFundOrderId();
                        try {
                            facadeServiceImpl.closeFundOrder(fundOrderId, cashierRequest.getEnvInfo());
                        } catch (FacadeCheckedException e) {
                            LOGGER.error("Unable to close the order for the fundOrderId : {}", fundOrderId);
                        }
                    } else {
                        try {
                            facadeServiceImpl.closeOrder(cashierRequest.getPaytmMerchantId(), acquirementId,
                                    cashierRequest.isFromAoaMerchant());
                        } catch (FacadeCheckedException e) {
                            LOGGER.error("Unable to close the order for the acquirementId : {}", acquirementId);
                        }
                    }
                    CashierPaymentStatus cashierPaymentStatus = CashierLopperMapper.buildCashierPaymentStatus(
                            payResultQueryResponse.getBody(), cashierResponseCodeDetails);

                    return cashierPaymentStatus;
                }
                return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody());
            } else {
                String resultInfoStr = JsonMapper.mapObjectToJson(resultInfo);
                throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
            }

        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching payment status", e);
        }
    }

    @Override
    public CashierFundOrderStatus fetchFundOrderStatus(String fundOrderId, CashierRequest cashierRequest)
            throws CashierCheckedException {
        try {
            LOGGER.info("Received Request for fetching fund order status:{}", fundOrderId);
            QueryByFundOrderIdResponse fundOrderIdResponse = facadeServiceImpl.queryByFundOrderId(fundOrderId,
                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
            LOGGER.debug("QueryByFundOrderIdResponse:{}", fundOrderIdResponse);

            queryByFundOrderIdResponseValidator.validInput(fundOrderIdResponse);
            ResultInfo resultInfo = fundOrderIdResponse.getBody().getResultInfo();

            LOGGER.trace("ResultInfo:{}", resultInfo);
            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                FundOrder fundOrder = fundOrderIdResponse.getBody().getFundOrder();
                return CashierLopperMapper.buildCashierFundOrderStatus(fundOrder);
            } else {
                String resultInfoStr = JsonMapper.mapObjectToJson(resultInfo);
                throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
            }
        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Facade : Exeception occurred while working with looper for fetching fund order status", e);
        }
    }

    @Override
    public CashierTransactionStatus fetchTrasactionStatus(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAOAMerchant) throws CashierCheckedException {
        // need diff method for further changes
        LOGGER.info("Received Request for fetching transaction status:: mid:{},acquirementId:{}", merchantId,
                acquirementId);
        return facadeServiceImpl.queryByAcquirementId(merchantId, acquirementId, needFullInfo, isFromAOAMerchant);
    }

    @Override
    public CashierTransactionStatus fetchTrasactionStatus(String merchantId, String acquirementId,
            boolean needFullInfo, boolean isFromAOAMerchant, String paytmMerchantId, Routes route)
            throws CashierCheckedException {
        if (StringUtils.isBlank(paytmMerchantId)) {
            return fetchTrasactionStatus(merchantId, acquirementId, needFullInfo, isFromAOAMerchant);
        }
        LOGGER.info("Received Request for fetching transaction status:: mid:{},acquirementId:{}", merchantId,
                acquirementId);
        return facadeServiceImpl.queryByAcquirementId(merchantId, acquirementId, needFullInfo, isFromAOAMerchant,
                paytmMerchantId, route);
    }
}
