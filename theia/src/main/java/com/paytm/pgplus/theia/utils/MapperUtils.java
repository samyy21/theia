package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.fund.enums.FundOrderStatus;
import com.paytm.pgplus.facade.merchantlimit.constants.Constants;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MapperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapperUtils.class);
    private static final Map<String, ResultCode> merchantLimitBreachedExceptionMap;

    public static List<String> mapOldPayModesToNew(String[] payModes) {
        List<String> newPayModes = new ArrayList<>();
        for (String payMode : payModes) {

            EPayMethod payMethod = EPayMethod.getPayMethodByOldName(payMode);
            if (null == payMethod) {
                LOGGER.warn("Invalid value of Disabled Pay Method passed :: {}", payMethod);
            } else {
                newPayModes.add(payMethod.toString());
            }
        }
        return newPayModes;
    }

    public static String getTransactionStatusForResponse(CashierTransactionStatus transactionStatus,
            CashierPaymentStatus paymentStatus) {

        if (PaymentStatus.SUCCESS.toString().equals(paymentStatus.getPaymentStatusValue())) {
            return getTransactionStatusForResponse(transactionStatus);
        } else if (PaymentStatus.FAIL.toString().equals(paymentStatus.getPaymentStatusValue())) {
            return ExternalTransactionStatus.TXN_FAILURE.name();
        }
        return ExternalTransactionStatus.PENDING.name();
    }

    public static String getTransactionStatusForResponse(QueryTransactionStatus transactionStatus,
            QueryPaymentStatus paymentStatus) {
        if (PaymentStatus.SUCCESS.name().equals(paymentStatus.getPaymentStatusValue())) {
            return getTransactionStatusForResponse(transactionStatus);
        } else if (PaymentStatus.FAIL.name().equals(paymentStatus.getPaymentStatusValue())) {
            return ExternalTransactionStatus.TXN_FAILURE.toString();
        } else {
            if (transactionStatus != null) {
                return getTransactionStatusForResponse(transactionStatus);
            }
        }

        return ExternalTransactionStatus.PENDING.name();
    }

    public static String getTransactionStatusForResponseForOffline(QueryTransactionStatus transactionStatus,
            QueryPaymentStatus paymentStatus) {
        if (PaymentStatus.SUCCESS.name().equals(paymentStatus.getPaymentStatusValue())) {
            return getTransactionStatusForResponse(transactionStatus);
        }
        if (PaymentStatus.FAIL.name().equals(paymentStatus.getPaymentStatusValue())) {
            return ExternalTransactionStatus.TXN_FAILURE.toString();
        }
        return getTransactionStatusForResponse(transactionStatus);
    }

    public static String getTransactionStatusForResponse(CashierFundOrderStatus fundOrderStatus,
            CashierPaymentStatus paymentStatus) {
        if (PaymentStatus.SUCCESS.toString().equals(paymentStatus.getPaymentStatusValue())) {
            return getTransactionStatusForResponse(fundOrderStatus);
        }
        if (PaymentStatus.FAIL.toString().equals(paymentStatus.getPaymentStatusValue())) {
            return ExternalTransactionStatus.TXN_FAILURE.toString();
        }
        return ExternalTransactionStatus.PENDING.toString();
    }

    private static String getTransactionStatusForResponse(CashierTransactionStatus transactionStatus) {
        return mapTransactionStatusForResponse(transactionStatus.getStatusDetailType());
    }

    private static String getTransactionStatusForResponse(QueryTransactionStatus transactionStatus) {
        return mapTransactionStatusForResponse(transactionStatus.getStatusDetailType());
    }

    static {
        merchantLimitBreachedExceptionMap = new HashMap<>();
        merchantLimitBreachedExceptionMap.put(Constants.MONTH, ResultCode.MERCHANT_MONTHLY_VELOCITY_LIMIT_BREACH);
        merchantLimitBreachedExceptionMap.put(Constants.WEEK, ResultCode.MERCHANT_WEEKLY_VELOCITY_LIMIT_BREACH);
        merchantLimitBreachedExceptionMap.put(Constants.DAY, ResultCode.MERCHANT_DAILY_VELOCITY_LIMIT_BREACH);
        merchantLimitBreachedExceptionMap.put(Constants.EDC_DAILY, ResultCode.EDC_MERCHANT_DAILY_VELOCITY_LIMIT_BREACH);
        merchantLimitBreachedExceptionMap.put(Constants.EDC_MONTHLY,
                ResultCode.EDC_MERCHANT_MONTHLY_VELOCITY_LIMIT_BREACH);
        merchantLimitBreachedExceptionMap.put(Constants.LINK_MONTHLY, ResultCode.LINK_MONTHLY_AMOUNT_LIMIT_EXCEED);
        merchantLimitBreachedExceptionMap.put(Constants.ADD_N_PAY_MONTHLY,
                ResultCode.ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED);
        merchantLimitBreachedExceptionMap.put(Constants.TXN, ResultCode.MERCHANT_AMOUNT_EXCEED_TXN_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.LINK_TXN,
                ResultCode.MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_TXN_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.LINK_DAILY,
                ResultCode.MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.LINK_WEEKLY,
                ResultCode.MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_WEEKLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_DAILY,
                ResultCode.DC_DOM_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_MONTHLY,
                ResultCode.DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_MAX_SINGLE,
                ResultCode.DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_RUPAY_DAILY,
                ResultCode.DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_RUPAY_MONTHLY,
                ResultCode.DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_RUPAY_MAX_SINGLE,
                ResultCode.DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_VISA_DAILY,
                ResultCode.DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_VISA_MONTHLY,
                ResultCode.DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DOM_VISA_MAX_SINGLE,
                ResultCode.DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_VISA_DAILY,
                ResultCode.CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_VISA_MONTHLY,
                ResultCode.CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_VISA_MAX_SINGLE,
                ResultCode.CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_MASTER_DAILY,
                ResultCode.CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_MASTER_MONTHLY,
                ResultCode.CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DOM_MASTER_MAX_SINGLE,
                ResultCode.CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_DAILY,
                ResultCode.CC_INTL_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_MONTHLY,
                ResultCode.CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_MAX_SINGLE,
                ResultCode.CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_VISA_DAILY,
                ResultCode.CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_VISA_MONTHLY,
                ResultCode.CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_VISA_MAX_SINGLE,
                ResultCode.CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_MASTER_DAILY,
                ResultCode.CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_MASTER_MONTHLY,
                ResultCode.CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_INTL_MASTER_MAX_SINGLE,
                ResultCode.CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.WALLET_DAILY,
                ResultCode.BALANCE_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.WALLET_MONTHLY,
                ResultCode.BALANCE_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.WALLET_MAX_SINGLE,
                ResultCode.BALANCE_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_DAILY, ResultCode.UPI_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_MONTHLY,
                ResultCode.UPI_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_MAX_SINGLE,
                ResultCode.UPI_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_COLLECT_DAILY,
                ResultCode.UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_COLLECT_MONTHLY,
                ResultCode.UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_COLLECT_MAX_SINGLE,
                ResultCode.UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_INTENT_DAILY,
                ResultCode.UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_INTENT_MONTHLY,
                ResultCode.UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_INTENT_MAX_SINGLE,
                ResultCode.UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_PUSH_DAILY,
                ResultCode.UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_PUSH_MONTHLY,
                ResultCode.UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_PUSH_MAX_SINGLE,
                ResultCode.UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.NB_DAILY, ResultCode.NB_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap
                .put(Constants.NB_MONTHLY, ResultCode.NB_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.NB_MAX_SINGLE,
                ResultCode.NB_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.POSTPAID_DAILY,
                ResultCode.POSTPAID_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.POSTPAID_MONTHLY,
                ResultCode.POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.POSTPAID_MAX_SINGLE,
                ResultCode.POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_DAILY, ResultCode.DC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap
                .put(Constants.DC_MONTHLY, ResultCode.DC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.DC_MAX_SINGLE,
                ResultCode.DC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_DAILY, ResultCode.CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap
                .put(Constants.CC_MONTHLY, ResultCode.CC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.CC_MAX_SINGLE,
                ResultCode.CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_CC_DAILY,
                ResultCode.UPI_CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_CC_MONTHLY,
                ResultCode.UPI_CC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT);
        merchantLimitBreachedExceptionMap.put(Constants.UPI_CC_SINGLE,
                ResultCode.UPI_CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT);

    }

    private static String getTransactionStatusForResponse(CashierFundOrderStatus fundOrderStatus) {
        String status = fundOrderStatus.getFundOrderStatus();
        if (FundOrderStatus.CLOSE.name().equals(status)) {
            return ExternalTransactionStatus.TXN_FAILURE.name();
        }
        if (FundOrderStatus.FAILED.name().equals(status)) {
            return ExternalTransactionStatus.TXN_FAILURE.name();
        }
        if (FundOrderStatus.SUCCESS.name().equals(status)) {
            return ExternalTransactionStatus.TXN_SUCCESS.name();
        }
        return ExternalTransactionStatus.PENDING.name();
    }

    public static String getResponseCode(String status, String instErrorCode, String bankName) {
        String responseCode = ResponseConstants.ResponseCodes.PAGE_OPEN_RESPONSE_CODE;

        if (ExternalTransactionStatus.TXN_FAILURE.toString().equals(status)) {
            if (StringUtils.isNotBlank(instErrorCode)) {
                responseCode = instErrorCode;
            }
        } else if (ExternalTransactionStatus.TXN_SUCCESS.toString().equals(status)) {
            responseCode = com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SUCCESS_RESPONSE_CODE.getCode();
        } else {
            if (StringUtils.isNotBlank(bankName)) {
                responseCode = ResponseConstants.ResponseCodes.ABANDON_AT_BANK_PAGE;
            } else {
                responseCode = ResponseConstants.ResponseCodes.ABANDON_AT_CASHIER_PAGE;
            }
        }
        return responseCode;
    }

    public static String getBizPatternForPayMethod(PayMethod payMethod) {

        if (payMethod == null) {
            LOGGER.error("PayMethod can't be null");
            return StringUtils.EMPTY;
        }

        switch (payMethod) {
        case CREDIT_CARD:
            return "CC";
        case DEBIT_CARD:
            return "DC";
        case NET_BANKING:
            return "NETBANKING";
        case ATM:
            return "ATM";
        case IMPS:
            return "IMPS";
        case EMI:
            return "EMI";
        case UPI:
            return "UPI";
        case BALANCE:
        case BANK_EXPRESS:
        case COD:
        case HYBRID_PAYMENT:
        case MP_COD:
        case PAYTM_DIGITAL_CREDIT:
        default:
            return StringUtils.EMPTY;
        }
    }

    private static String mapTransactionStatusForResponse(String status) {
        if (AcquirementStatusType.CLOSED.name().equals(status)) {
            return ExternalTransactionStatus.TXN_FAILURE.name();
        }
        if (AcquirementStatusType.SUCCESS.name().equals(status)) {
            return ExternalTransactionStatus.TXN_SUCCESS.name();
        }
        if (AcquirementStatusType.PAYING.name().equals(status)) {
            return ExternalTransactionStatus.TXN_SUCCESS.name();
        }

        return ExternalTransactionStatus.PENDING.name();
    }

    public static ResultCode getResultCodeForMerchantBreached(String limitType, String limitDuration) {
        if (Constants.AMOUNT.equals(limitType)) {
            if (merchantLimitBreachedExceptionMap.containsKey(limitDuration)) {
                return merchantLimitBreachedExceptionMap.get(limitDuration);
            }
        }
        return ResultCode.MERCHANT_VELOCITY_LIMIT_BREACH;
    }
}
