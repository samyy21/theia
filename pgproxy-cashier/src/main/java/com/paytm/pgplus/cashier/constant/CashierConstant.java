/**
 *
 */
package com.paytm.pgplus.cashier.constant;

/**
 * @author amit.dubey
 *
 */
public class CashierConstant {
    public static final String FUND_ORDER_STATUS_SUCCESS_MESSAGE = "SUCCESS";
    public static final String SUCCESS_RESULT_CODE = "SUCCESS";
    public static final String ACCEPTED_SUCCESS_CODE = "ACCEPTED_SUCCESS";

    // 1Rs
    public static final Long MIN_WALLET_BALANCE = 100L;
    public static final String UNSUPPORTED_OPERATION = "Operation NOT Supported";

    public static final String PAYTM_MASTER_KEY = "PAYTM_MASTER_KEY";

    public static final String ISOCARD_KEY = "ISOCARD_";
    public static final String PAYMENT_RETRY_KEY = "PAYMENT_RETRY_";
    public static final String RISK_RETRY_KEY = "RISK_RETRY_KEY_";
    public static final boolean isPaymentRetryPossible = true;

    public static final String ISSUING_BANK_NAME = "issuingBankName";
    public static final String ISSUING_BANK_ID = "issuingBankId";

    public static final String CREDIT_CARD_PAY_OPTION = "CREDIT_CARD_";
    public static final String DEBIT_CARD_PAY_OPTION = "DEBIT_CARD_";

    public static final String SAVED_CARD_IMPS_INST_NETWORK_TYPE = "IMPS";

    public static final String EXT_INFO_ALIPAY_MERCH_ID = "alipayMerchantId";
    public static final String MAESTRO_CARD = "MAESTRO";

    public static final String MAESTRO_CARD_EXPIRY_YEAR = "2049";
    public static final String MAESTRO_CARD_EXPIRY_MONTH = "12";
    public static final String MAESTRO_CARD_CVV = "123";

    public static final String PAYTM_USER_ID = "PAYTM_USER_ID";
    public static final String ZERO = "0";

    public static final String RETRY_COUNT = "RETRY_COUNT_";
    public static final boolean SAVE_INTERNATIONAL_CARD = true;
    public static final String AUTHORIZATION_TOKEN = "authorizationToken";

    public static final String BAJAJFN = "BAJAJFN";
    public static final String BAJAJ_CARD = "BAJAJ";
    public static final String CUST_ID = "CUST_ID";
    public static final String STORE_CARD_PREFERENCE = "STORE_CARD_PREFERENCE";

    public static final String BAJAJ_FINSERV_EMI_CARD = "Bajaj Finserv EMI Card";

    public static final String MASKED_CARD_NO = "maskedCardNo";
    public static final String CARD_INDEX_NO = "cardIndexNo";

    public static final String ADD_MONEY_DESTINATION = "addMoneyDestination";
    public static final String INTERNAL_RETRY_BLACKLIST_CODES = "internal.retry.blacklist.codes";
    public static final String TARGET_PHONE_NO = "targetPhoneNo";

    // Flag for UPICollect Sync API
    public static final String STOP_POLLING_FOR_UPI_COLLECT = "stop.polling.for.upi.collect";

    public static final String DUMMY_MERCHANT_ID = "dummyMerchantId";
    public static final String ACTUAL_ORDER_ID = "actualOrderId";
    public static final String DUMMY_ALIPAY_MID = "dummyAlipayMid";

    public static final String STOP_FETCHING_ACQ_STATUS_IN_UPI_COLLECT_POLLING = "theia.stop.fetching.acqStatus.inUpiCollectPolling";

}
