/**
 *
 */
package com.paytm.pgplus.cashier.looper.model;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus.CashierFundOrderStatusBuilder;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus.CashierPaymentStatusBuilder;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus.CashierTransactionStatusBuilder;
import com.paytm.pgplus.cashier.models.CashierResponseCodeDetails;
import com.paytm.pgplus.cashier.models.PayOption;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponseBody;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.fund.models.FundOrder;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author amit.dubey
 *
 */
public class CashierLopperMapper {
    /**
     * @param responseBody
     * @return
     */
    public static CashierPaymentStatus buildCashierPaymentStatus(PayResultQueryResponseBody responseBody) {
        CashierPaymentStatusBuilder builder = buildCommonCashierPaymentStatus(responseBody);

        return builder.build();
    }

    private static CashierPaymentStatusBuilder buildCommonCashierPaymentStatus(PayResultQueryResponseBody responseBody) {
        CashierPaymentStatusBuilder builder = new CashierPaymentStatusBuilder(responseBody.getTransId(), responseBody
                .getTransType().getType(), responseBody.getTransAmount().getCurrency().getCurrency(), responseBody
                .getTransAmount().getAmount(), responseBody.getPayerUserId(), responseBody.getPaymentStatus().name());

        builder.setWebFormContext(responseBody.getWebFormContext())
                .setResultPageRedirectURL(responseBody.getResultPageRedirectURL())
                .setPaymentErrorCode(responseBody.getPaymentErrorCode())
                .setInstErrorCode(responseBody.getInstErrorCode()).setPaidTime(responseBody.getPaidTime())
                .setExtendInfo(responseBody.getExtendInfo()).setPwpCategory(responseBody.getPwpCategory());

        List<PayOption> payOptions = new ArrayList<>();

        for (PayOptionInfo payOptionInfo : responseBody.getPayOptionInfos()) {
            PayOption payOption = new PayOption(payOptionInfo.getPayMethod().getMethod(), payOptionInfo.getPayMethod()
                    .getOldName(), payOptionInfo.getPayAmount().getCurrency().getCurrency(), payOptionInfo
                    .getPayAmount().getAmount(), payOptionInfo.getExtendInfo(),
                    payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount().getCurrency().getCurrency()
                            : payOptionInfo.getTransAmount().getCurrency().getCurrency(),
                    payOptionInfo.getTransAmount() == null ? payOptionInfo.getPayAmount().getAmount() : payOptionInfo
                            .getTransAmount().getAmount(), payOptionInfo.getChargeAmount() != null ? payOptionInfo
                            .getChargeAmount().getAmount() : null);
            if (payOptionInfo.getExtendInfo() != null
                    && payOptionInfo.getExtendInfo().containsKey(FacadeConstants.PREPAID_CARD)) {
                payOption.setPrepaidCard(payOptionInfo.getExtendInfo().get(FacadeConstants.PREPAID_CARD));
            }
            payOptions.add(payOption);
        }

        builder.setPayOptions(payOptions);
        return builder;
    }

    public static CashierPaymentStatus buildCashierPaymentStatus(PayResultQueryResponseBody responseBody,
            CashierResponseCodeDetails cashierResponseCodeDetails) {
        CashierPaymentStatusBuilder builder = buildCommonCashierPaymentStatus(responseBody);

        builder.setPaymentRetryPossible(cashierResponseCodeDetails.isRetry());

        builder.setPaytmResponseCode(cashierResponseCodeDetails.getPaytmResponseCode());
        builder.setErrorMessage(cashierResponseCodeDetails.getErrorMessage());

        return builder.build();
    }

    /**
     * @param responseBody
     * @return
     */
    public static CashierTransactionStatus buildCashierTrasactionStatus(QueryByAcquirementIdResponseBody responseBody) {
        Map<String, String> extendInfo = responseBody.getExtendInfo();
        if (extendInfo != null && null != extendInfo.get(CashierConstant.ACTUAL_ORDER_ID)) {
            responseBody.setMerchantTransId(extendInfo.get(CashierConstant.ACTUAL_ORDER_ID));
        }
        CashierTransactionStatusBuilder builder = new CashierTransactionStatusBuilder(responseBody.getAcquirementId(),
                responseBody.getMerchantTransId(), responseBody.getOrderTitle(), extendInfo);
        if (responseBody.getAmountDetail() != null) {
            if (responseBody.getAmountDetail().getChargeAmount() != null) {
                builder.setChargeAmount(new CashierMoney(responseBody.getAmountDetail().getChargeAmount().getCurrency()
                        .getCurrency(), responseBody.getAmountDetail().getChargeAmount().getAmount()));
            }
            if (responseBody.getAmountDetail().getChargebackAmount() != null) {
                builder.setChargebackAmount(new CashierMoney(responseBody.getAmountDetail().getChargebackAmount()
                        .getCurrency().getCurrency(), responseBody.getAmountDetail().getChargebackAmount().getAmount()));
            }
            if (responseBody.getAmountDetail().getOrderAmount() != null) {
                builder.setOrderAmount(new CashierMoney(responseBody.getAmountDetail().getOrderAmount().getCurrency()
                        .getCurrency(), responseBody.getAmountDetail().getOrderAmount().getAmount()));
            }
            if (responseBody.getAmountDetail().getPayAmount() != null) {
                builder.setPayAmount(new CashierMoney(responseBody.getAmountDetail().getPayAmount().getCurrency()
                        .getCurrency(), responseBody.getAmountDetail().getPayAmount().getAmount()));
            }
            if (responseBody.getAmountDetail().getRefundAmount() != null) {
                builder.setRefundAmount(new CashierMoney(responseBody.getAmountDetail().getRefundAmount().getCurrency()
                        .getCurrency(), responseBody.getAmountDetail().getRefundAmount().getAmount()));
            }
        }
        if (responseBody.getPaymentViews() != null) {
            builder.setPaymentViews(responseBody.getPaymentViews());
        }
        if (responseBody.getSplitCommandInfoList() != null) {
            builder.setSplitCommandInfoList(responseBody.getSplitCommandInfoList());
        }
        builder.setTimeDetail(responseBody.getTimeDetail().getCreatedTime(),
                responseBody.getTimeDetail().getPaidTime(), responseBody.getTimeDetail().getConfirmedTime(),
                responseBody.getTimeDetail().getExpiryTime());

        builder.setStatusDetail(responseBody.getStatusDetail().getAcquirementStatus().getStatusType(), responseBody
                .getStatusDetail().isFrozen());
        builder.setInputUserInfoBuyer(responseBody.getBuyer().getUserId(), responseBody.getBuyer().getExternalUserId());

        builder.setCurrentTxnCount(responseBody.getCurrentTxnCount());

        builder.setOrderModifyExtendInfo(responseBody.getOrderModifyExtendInfo());

        if (responseBody.getTimeDetail() != null)
            builder.setPaidTimesForTimeDetails(responseBody.getTimeDetail().getPaidTimes());

        return builder.build();
    }

    /**
     * @param fundOrder
     * @return
     */
    public static CashierFundOrderStatus buildCashierFundOrderStatus(FundOrder fundOrder) {
        CashierFundOrderStatusBuilder builder = new CashierFundOrderStatusBuilder(fundOrder.getFundOrderId(),
                fundOrder.getActorUserId(), fundOrder.getInvokerId(), fundOrder.getRequestId());
        builder.setFundType(fundOrder.getFundType().getType())
                .setProductCode(fundOrder.getProductCode().getProductCode(), fundOrder.getProductCode().getId())
                .setTerminalType(fundOrder.getTerminalType().getTerminal())
                .setFundAmount(fundOrder.getFundAmount().getCurrency().getCurrency(),
                        fundOrder.getFundAmount().getAmount())
                .setChargeAmount(fundOrder.getChargeAmount().getCurrency().getCurrency(),
                        fundOrder.getChargeAmount().getAmount())
                .setTaxAmount(fundOrder.getTaxAmount().getCurrency().getCurrency(),
                        fundOrder.getTaxAmount().getAmount())
                .setPaidtotalAmount(fundOrder.getPaidTotalAmount().getCurrency().getCurrency(),
                        fundOrder.getTaxAmount().getAmount())
                .setActualFundAmount(fundOrder.getActualFundAmount().getCurrency().getCurrency(),
                        fundOrder.getActualFundAmount().getAmount())
                .setFundOrderStatus(fundOrder.getFundOrderStatus().name())
                .setExtendedInfo(fundOrder.getExtendInfo())
                .setDates(fundOrder.getAcceptedTime(), fundOrder.getPaidTime(), fundOrder.getAcceptExpiryTime(),
                        fundOrder.getPayExpiryTime(), fundOrder.getSuccessTime(), fundOrder.getCreatedTime(),
                        fundOrder.getModifiedTime());
        if (null != fundOrder.getPayerIdentifier()) {
            builder.setPayerUser(fundOrder.getPayerIdentifier().getUserId(), fundOrder.getPayerIdentifier()
                    .getLoginId(), fundOrder.getPayerIdentifier().getLoginIdType(), fundOrder.getPayerAccountNo());
        }

        if (null != fundOrder.getPayeeIdentifier()) {
            builder.setPayeeUser(fundOrder.getPayeeIdentifier().getUserId(), fundOrder.getPayeeIdentifier()
                    .getLoginId(), fundOrder.getPayeeIdentifier().getLoginIdType(), fundOrder.getPayeeAccountNo());
        }

        return builder.build();
    }
}
