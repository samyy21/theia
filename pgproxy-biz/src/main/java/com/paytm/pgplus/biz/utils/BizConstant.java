/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.utils;

import java.math.BigDecimal;

/**
 * Created by prashant on 18/2/16.
 */
public class BizConstant {

    public static final String STORED_CARD = "savedCardId";

    public static final String BASIC_KYC = "Basic";

    public static final String PRIMITIVE_KYC = "Primitive Account";

    public static enum STATUS_VALUES {
        ACTIVE, INACTIVE
    };

    public static final class ExtendedInfoKeys {

        public static final String TOTAL_TXN_AMOUNT = "totalTxnAmount";
        public static final String ISSUING_BANK_NAME = "issuingBankName";
        public static final String PAYTM_USER_ID = "PAYTM_USER_ID";
        public static final String USER_MOBILE = "userMobile";
        public static final String USER_EMAIL = "userEmail";
        public static final String RETRY_COUNT = "retryCount";
        public static final String TXNTYPE = "txnType";
        public static final String MERCHANT_NAME = "merchantName";
        public static final String MCC_CODE = "mccCode";
        public static final String PRODUCT_CODE = "productCode";
        public static final String EMI_PLAN_ID = "emiPlanId";
        public static final String TOPUP_AND_PAY = "topupAndPay";
        public static final String MERCHANT_TRANS_ID = "merchantTransId";
        public static final String PAYTM_MERCHANT_ID = "paytmMerchantId";
        public static final String SSO_TOKEN = "ssoToken";
        public static final String ISSUING_BANK_ID = "issuingBankId";
        public static final String ALIPAY_MERCHANT_ID = "alipayMerchantId";
        public static final String ADD_MONEY_DESTINATION = "addMoneyDestination";
        public static final String TARGET_PHONE_NO = "targetPhoneNo";
        public static final String MERCHANT_ADDRESS_1 = "merchantAddress.address1";
        public static final String CALL_BACK_URL = "callBackURL";
        public static final String SUBWALLET_AMOUNT_DETAILS = "subwalletWithdrawMaxAmountDetails";
        public static final String CARD_INDEX_NUMBER = "cardIndexNo";
        public static final String CARD_HASH = "cardHash";
        public static final String PROMO_CODE = "promoCode";
        public static final String PROMO_RESPONSE_CODE = "promoResponseCode";
        public static final String PROMO_APPLY_RESULT_STATUS = "promoApplyResultStatus";
        public static final String CHANNEL_INFO_PASS_THROUGH_KEY = "directPassThroughInfo";
        public static final String MERCHANT_DISPLAY_NAME = "merchantDisplayName";
        public static final String MERCHANT_TYPE = "merchantType";
        public static final String IS_ENHANCED_NATIVE = "isEnhancedNative";
        public static final String MERCHANT_LOGO = "merchantLogo";
        public static final String REQUEST_TYPE = "requestType";
        public static final String WORKFLOW = "workFlow";
        public static final String GUEST_TOKEN = "guestToken";
        public static final String IS_CARD_TOKEN_REQUIRED = "cardTokenRequired";
        public static final String CHECKOUT_JS_FLAG = "checkoutJsFlag";
        public static final String PAYMENT_MODE = "paymentMode";
        public static final String PAYER_NAME = "payerName";
        public static final String PAYER_PSP = "payerPSP";
        public static final String UDF1 = "udf1";
        public static final String CURRENT_SUBSCRIPTION_ID = "currentSubscriptionId";
        public static final String ADD_MONEY_SOURCE = "source";
        public static final String DUMMY_ORDER_ID = "dummyOrderId";
        public static final String DUMMY_MERCHANT_ID = "dummyMerchantId";
        public static final String SUBS_FREQ_UNIT = "subsFreqUnit";
        public static final String BANK_ID = "bankId";
        public static final String PREPAID_CARD = "prepaidCard";
        public static final String CORPORATE_CARD = "corporateCard";
        public static final String BIN_NUMBER = "binNumber";
        public static final String FROMAOAMERCHANT = "fromAoaMerchant";
        public static final String FROMAOAREQUEST = "fromAOARequest";
        public static final String IS_SAVED_CARD = "isSavedCard";
        public static final String MERCHANT_VPA = "merchantVpa";
        public static final String USER_ID = "userId";
        public static final String CUST_ID = "custId";
        public static final String EMI_INFO = "emiInfo";
        public static final String TOTAL_PAYMENT_COUNT = "totalPaymentCount";

        public static final class ConsultResponse {
            public static final String ADDANDPAY_ENABLED_KEY = "isSupportAddPay";
            public static final String ADDANDPAY_ENABLED_YES = "Y";
            public static final String HYBRID_ENABLED_KEY = "isSupportHybridPayment";
            public static final String HYBRID_ENABLED_YES = "Y";

        }

        public static final class PassThroughKeys {
            public static final String MCC = "mcc";
            public static final String CUSTOMER_EMAIL_ID = "customerEmailID";
            public static final String CUSTOMER_PHONE_NO = "customerPhoneNo";
            public static final String IP_ADDRESS = "ipAddr";
            public static final String PAYMENT_TIMEOUT_UPI_MINS = "paymentTimeoutInMinsForUPI";
            public static final String IS_OFFLINE_TXN = "isOfflineTxn";

        }

        public static final class MerchantTypeValues {
            public static final String ONUS = "Onus";
            public static final String OFFUS = "Offus";
        }

        public static final String ACCOUNT_RANGE_CARD_BIN = "accountRangeCardBin";
        public static final String IS_COFT_BIN_ELIGIBLE = "IS_COFT_BIN_ELIGIBLE";
        public static final String LPV_ROUTE = "LPV_ROUTE";

    }

    public static final String PERSISTENCE_MODEL_PACKAGE = "com.paytm.pgplus.biz.persistence.model";
    public static final String PERSISTENCE_PACKAGE = "com.paytm.pgplus.biz.persistence";

    public static final String MERCHANT_STATUS = "merchantStatus";
    public static final String BLOCKED = "blocked";
    public static final String REQUEST_DATA = "requestData";

    // BizModule Constants

    public static final String UBER_REQ = "UBER";
    public static final String MARKET_PLACE_REQ = "MP";
    public static final String OAUTH_INVALID_TOKEN = "410";

    // Generic ReqMap keys
    public static final String FUND_ORDER_ID_KEY = "FUND_ORDER_ID";
    public static final String FUND_ORDER_REQ_ID_KEY = "FUND_ORDER_REQ_ID";
    public static final String WALLET_TYPE = "BALANCE";

    // ExtendedInfo Params
    public static final String CALL_BACK_URL = "callbackURL";
    public static final String PEON_URL = "peonURL";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String CLIENT_IP = "clientIp";
    public static final String WEBSITE = "website";
    public static final String PROMO_CODE = "promoCode";
    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phoneNo";
    public static final String PROMO_RESPONSE_CODE = "promoResponseCode";

    public static final String NICKNAME = "nickname";
    // ----------------

    public static final String INVALID_ENC_KEY = "INVALID_KEY";

    // Generic request bean params
    public static final String LOG_ENTRY = "Entering in to ";
    public static final String LOG_EXIT = "Exiting from ";
    public static final String MSISDN = "MSISDN";
    public static final String MOBILE_NO = "MOBILE_NO";
    public static final String PAN_CARD = "PAN_CARD";
    public static final String ISEMAILVERIFIED = "ISEMAILVERIFIED";
    public static final String VERIFIED_BY = "VERIFIED_BY";
    public static final String DOB = "DOB";
    public static final String ADDRESS_1 = "ADDRESS_1";
    public static final String ADDRESS_2 = "ADDRESS_2";
    public static final String CITY = "CITY";
    public static final String PINCODE = "PINCODE";
    public static final String MASTER_KEY_ID = "MASTER_KEY_ID";
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public static final String COMMENTS = "COMMENTS";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String REQUEST_TYPE = "REQUEST_TYPE";
    public static final String NORMAL_TXN = "DEFAULT";
    public static final String PAYMENT_TYPE_ID = "PAYMENT_TYPE_ID";
    public static final String PAYMENT_MODE_ONLY = "PAYMENT_MODE_ONLY";
    public static final String BANK_CODE = "bankCode";
    public static final String ORDER_DETAILS = "ORDER_DETAILS";
    public static final String INDUSTRY_TYPE_ID = "INDUSTRY_TYPE_ID";
    public static final String CALLBACK_URL = "CALLBACK_URL";
    public static final String CHECKSUMHASH = "CHECKSUMHASH";
    public static final String MID = "MID";
    public static final String CUSTID = "CUST_ID";
    public static final String STATE = "STATE";
    public static final String DL_NUMBER = "DL_NUMBER";
    public static final String TXN_AMOUNT = "TXN_AMOUNT";
    public static final String SUBS_ID = "SUBS_ID";
    public static final String AUTH_MODE = "AUTH_MODE";
    public static final String SENTTOMINASERVER = "SENTTOMINASERVER";
    public static final String DISPLAYEDTHECCPAGE = "DISPLAYEDTHECCPAGE";
    public static final String SALE = "SALE";
    public static final String NB = "NB";
    public static final String CHEQUE = "CHEQUE";
    public static final String DD = "DD";
    public static final String NEFT = "NEFT";
    public static final String COD = "COD";
    public static final String ATM = "ATM";
    public static final String AMEX = "AMEX";
    public static final String RUPAY = "RUPAY";
    public static final String CC = "CC";
    public static final String DC = "DC";
    public static final String CLW = "CLW";
    public static final String HYBRID = "HYBRID";
    public static final String SSO_TOKEN = "SSO_TOKEN";
    public static final String PAYTM_TOKEN = "PAYTM_TOKEN";
    public static final String LOGIN_FLAG = "LOGIN_FLAG";
    public static final String TOKEN_TYPE = "TOKEN_TYPE";
    public static final String COOKIE_NAME = "COOKIE_NAME";
    public static final String SCW_MASTER = "SCW_MASTER";
    public static final String WALLET_BALANCE = "WALLET_BALANCE";
    public static final String COOKIE_WALLET_BALANCE = "COOKIE_WALLET_BALANCE";
    public static final String WALLET_BALANCE_INFO = "WALLET_BALANCE_INFO";
    public static final String SAVED_CARD_INFO = "SAVED_CARD_INFO";
    public static final String SSO_WALLET_BALANCE = "SSO_WALLET_BALANCE";
    public static final String WALLET_ADMIN_TOKEN = "WALLET_ADMIN_TOKEN";
    public static final String OAUTH_SSOID = "OAUTH_SSOID";
    public static final String OTP_VERIFIED_FLAG = "OTP_VERIFIED_FLAG";
    public static final String EMI_BANK_MAP = "EMI_BANK_MAP";
    public static final String EMI_HYBRID_BANK_MAP = "EMI_HYBRID_BANK_MAP";
    public static final String EMI_CHANNEL_ID = "emiChannelId";
    public static final String IMPS = "IMPS";
    public static final String CASHCARD = "CASHCARD";
    public static final String PPI = "PPI";
    public static final String TELCO = "Telco";
    public static final String EMI = "EMI";
    public static final String REWARDS = "REWARDS";
    public static final String COMMA = ",";
    public static final int LAST_FOUR_DIGIT_CARD_NO = 4;
    public static final String MINA_SERVER_DOWN = "ERROR";
    public static final String REQ_MID = "MID";
    public static final String REQ_CHANNEL_ID = "CHANNEL_ID";
    public static final String REQ_WEBSITE = "WEBSITE";
    public static final String REQ_USER_AGENT = "User-Agent";
    public static final String REQ_ORDER_ID = "ORDER_ID";
    public static final String PAYMENT_TYPE = "PaymentType";
    public static final String EQUAL = "=";
    public static final String TXN_MODE = "txnMode";
    public static final String STORED_DC_CARD = "saveDCardList";

    public static final String REQ_CARD_NUMBER = "cardNumber";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String REQ_EXP_MONTH_DATE = "ccExpiryMonth";
    public static final String REQ_EXP_YEAR_DATE = "ccExpiryYear";
    public static final String REQ_EXP_MONTHYEAR_DATE = "ccExpiryMonthYear";

    public static final String CVV_NUMBERE = "cvvNumber";
    public static final String CARD_STORE = "storeCardFlag";
    public static final String REQ_ADDRESS_1 = "address1";
    public static final String REQ_ADDRESS_2 = "address2";
    public static final String REQ_CITY = "city";
    public static final String REQ_STATE = "state";
    public static final String REQ_PINCODE = "pincode";
    public static final String REQ_BANKNAME = "bankname";
    public static final String REQ_BANKCODE = "bankCode";
    public static final String REQ_MMID = "mmid";
    public static final String REQ_MOBILE_NO = "mobileNo";
    public static final String REQ_OTP = "otp";

    public static final String REQ_ITZCARDNUMBER = "itzCashNumber";
    // public static final String REQ_ITZPWD = "itzPwd";

    public static final String W_USER_ID = "walletUserId";
    // public static final String W_PASSWORD = "password";

    public static final String REQUEST_BANKCODE = "BANK_CODE";
    public static final String REQ_TXN_TRANSIENT_ID = "txnTransientId";
    public static final String REQ_ENTITY_ID = "entityId";
    public static final String REQ_TXN_AMOUNT = "txnAmount";
    public static final String REQ_INDUSTRY_ID = "industryId";
    public static final String REQ_CURRENCY_ID = "currencyId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String IS_ELIGIBLE_FOR_COFT = "isEligibleForCoft";

    public final String lineSeparator = System.getProperty("line.separator");
    public static final String LOG_ENTRY_ID = "Unique Id:";
    public static final String LOG_ENTRY_VALUE = "None";
    public final String logRepeater = "#";
    public static final String MERCHANT_TXNIPUT_VALIDATION = "MERCHANT_TXNIPUT_VALIDATION";
    public static final String MERCHANT_TXNINPUT_VALIDATION_ERROR = "140";
    public static final String TRANSIENTINFO = "TRANSIENTINFO";
    public static final String BLOCK_INPUT = "BLOCK_INPUT";
    public static final String FSS_REQUEST = "FSS_REQUEST";
    public static final String OR_STR = "|";
    public static final String USERID = "USERID";
    public static final String CARD_NUMBER = "CARD NUMBER";
    public static final String BIN_NUMBER = "BIN NUMBER";
    public static final String IP_ADDRESS = "IP_ADDRESS";

    public static final String GRAY = "GRAY";
    public static final String BLACK = "BLACK";
    public static final String WHITE = "WHITE";
    public static final String TRUE = "TRUE";

    public static final String ALERT = "ALERT";
    public static final String STOP_AFTER_X_TIMES = "Stop after X times";
    public static final String STOP_AND_ALERT = "STOP & ALERT";
    public static final String PASS = "PASS";

    public static final String MATCHING = "MATCHING";
    public static final String NOT_MATCHING = "NOT_MATCHING";

    public static final String TXN_PG_GRAY_ALERT = "TXN_PG_GRAY_ALERT";
    public static final String TXN_PG_BLOCKED_ALERT = "TXN_PG_BLOCKED_ALERT";
    public static final String TXN_PG_BLOCKED = "TXN_PG_BLOCKED";

    public static final String TXN_FSS_GRAY_ALERT = "TXN_FSS_GRAY_ALERT";
    public static final String TXN_FSS_BLOCKED_ALERT = "TXN_FSS_BLOCKED_ALERT";
    public static final String TXN_FSS_BLOCKED = "TXN_FSS_BLOCKED";
    public static final String MERCHANT_HTML_CNTNT = "merchantErrHtml";
    public static final String FSS_SERVER_IP = "fss.server.ip";
    public static final String INVALID_TXN_AMOUNT = "433"; // Please
    // change
    // the
    // code
    // acccordingly
    public static final String INVALID_CHECKSUM = "330";
    public static final String RISK_INFO = "riskInfo";

    public static final String FSS_SERVER_PORT = "fss.server.port";

    public static final String WEB = "WEB";
    public static final String WEB_REQUEST = "WEB_REQUEST";

    public static final String ACTION_REQUEST = "ActionRequest";

    public static final String REQUEST = "REQUEST";
    public static final String UTF_8 = "UTF-8";
    public static final String RANDOM_NUMBER = "randomNumber";

    public static final String STR_Y = "Y";
    public static final String STR_N = "N";
    public static final String STR_EMPTY = "";

    public static final String STR_YES = "YES";
    public static final String STR_NO = "NO";
    public static final String SESS_FSS_TXN_ID = "fssTxnId";
    public static final String SESS_CUST_PROF_ID = "custProfileId";
    public static final String STR_ZERO = "0";

    public static final String SESS_FSS_ENABLED = "isFssEnanled";
    public static final String SESS_TXN_MODE = "txnMode";
    public static final String SESS_ENTITY_ID = "entityId";
    public static final String SESS_WALLET_TYPE = "walletType";
    public static final String SESS_CHANNEL_ID = "channelId";
    public static final String SESS_COMM_MODE = "comMode";
    public static final String SESS_INDUSTRY_ID = "industryId";
    public static final String SESS_TXN_AMOUNT = "txnAmount";
    public static final String SESS_CURRENCY_ID = "currencyId";
    public static final String SESS_TRNSNT_TXN_ID = "txnTransientId";
    public static final String SESS_TXN_STATUS = "txnStatus";
    public static final String SESS_CARD_STORE_OPTION = "cardStoreOption";
    public static final String SESS_TXN_USER_INFO = "txnUserInfo";
    public static final String SESS_ORDER_ID = "orderId";
    public static final String SESS_OCP_ENABALED = "isOcpEnabled";
    public static final String SESS_REQUEST_TYPE = "requestType";
    public static final String SESS_PAYMENT_MODE_ONLY = "PAYMENT_MODE_ONLY";
    public static final String THEME = "theme";
    public static final String LOGIN_THEME = "LOGIN_THEME";
    public static final String SUB_THEME = "sub_theme";

    public static final String SESS_BANK_CODE = "bankCode";

    public static final String SESS_SAVECARD_OPTIONS = "saveCardOptions";

    public static final String SESS_SAVED_CARD_LIST = "savedCardsList";
    public static final String SESS_SAVED_CARD_LIST_EC = "savedCardsListEC";
    public static final String SESS_SAVED_CARD_TYPE = "savedCardsType";
    public static final String SESS_BALANCE_TYPE = "balanceType";

    public static final String SESS_SAVED_DC_CARD_LIST = "savedDCCardsList";

    public static final String SESS_STATE_LIST = "stateList";

    public static final String ON = "on";

    public static final String SESS_BANK_LIST = "MerchBankList";
    public static final String SESS_MERCHANT_BANK_LIST = "merchBankList";
    public static final String SESS_DEBIT_CARD_LIST = "DebitCardList";
    public static final String SESS_MERCHANT_DEBIT_CARD_LIST = "debitCardList";
    public static final String SESS_ATM_CARD_LIST = "ATMCardList";
    public static final String SESS_MERCHANT_ATM_CARD_LIST = "aTMCardList";

    public static final String NEGATIVE_DB = "NEGATIVE_DB";

    public static final String FSS_SCORE = "FSS_SCORE";

    public static final String IP_LOCATION_MAPPING = "IP_LOCATION_MAPPING";

    public static final String TXN_BLOCKED = "Transaction is Blocked";
    public static final String COD_PAYMENT = "COD pending to be collected";

    public static final String FILE_PATH_RESPONSE_MSG_PROP = "/responseCode.properties";
    // Statd Constant
    public static final String ACTIVE = "statsd.active";
    public static final String HOST = "statsd.host";
    public static final String PORT = "statsd.port";
    public static final String NO_OF_PG_WEB_HITS = "NEWPG.HITS.WEB.";
    public static final String SCW_URI = "scw.uri";
    public static final String COOKIE_EXPIRY = "cookie.expiry";
    public static final String WALLET_BAL_SUFFIX = "/wallet-web/checkBalance";
    public static final String WALLET_LIMIT_SUFFIX = "/wallet-web/walletLimits";
    // Hybrid Transaction
    public static final String PARENT_TXN = "PARENT_TXN";
    public static final String FIRST_CHILD = "FIRST_CHILD";
    public static final String SECOND_CHILD = "SECOND_CHILD";
    public static final String NO_OF_DB_ERROR = "NEWPGDB.ERROR";

    public static final String PAYTM_MID = "paytm.MID";

    public static final String SCW_PAYTM_MID = "scw.mid";
    public static final String SCW_PAYTM_INDUSTRY = "scw.paytm.industry";
    public static final String SCW_PAYTM_WEBSITE = "scw.paytm.website";

    public static final String PRE_AUTH_MODE_3D = "PA3D";

    public static final String CASHBACK_PROMOCODE = "CASHBACK";
    public static final String DISCOUNT_PROMOCODE = "DISCOUNT";
    public static final String REQ_PROMO_CAMP_ID = "PROMO_CAMP_ID";
    public static final String INVALID_PROMOCODE = "Invalid Promocode";
    public static final String INACTIVE_PROMOCODE = "Promocode is not applicable for the merchant";
    public static final String EXPIRED_PROMOCODE = "Promocode is expired.";
    public static final String PROMO_FAILURE = "PROMO_FAILURE";
    public static final String PROMO_SUCCESS = "PROMO_SUCCESS";
    public static final String PROMO_CAMP_ID = "PROMO_CAMP_ID";
    public static final String PROMOCODE = "promoCode";
    public static final String PROMOCODE_TYPE = "promocodeType";
    public static final String PROMO_CHECK_VALIDITY = "checkPromoValidity";
    public static final String PROMO_BIN_LIST = "promoBinList";
    public static final String PROMO_NB_LIST = "promoNbList";
    public static final String PROMO_CARD_BANK_LIST = "promoCardBankList";
    public static final String PROMO_PAYMENT_MODES = "promoPaymentModes";
    public static final String PROMO_CARD_BANKS = "promoCardBanks";
    public static final String PROMO_NB_BANKS = "promoNbBanks";
    public static final String PROMO_CARDTYPE = "promoCardType";
    public static final String PROMO_MSG = "promoMSG";
    public static final String PROMO_ERROR_MSG = "promoErrorMSG";
    public static final String USRPWD = "USRPWD";
    public static final String OTP = "OTP";
    public static final String SESS_ADD_MONEY_ENABLED = "addMoneyEnabled";
    public static final String SESS_HYBRID_ALLOWED = "hybridAllowed";
    public static final String SESS_CUST_ID = "CUST_ID";
    public static final String SIGN_IN_HITS = "PG.SIGNIN.HITS";
    public static final String SESS_ADD_MONEY_MODES = "addMoneyPayModes";
    public static final String SESS_ADD_MONEY_INFO = "addMoneyTxnInfo";
    public static final String SESS_MERCHANT_NAME = "merchantName";
    public static final String LOYLTY_MBID = "loylty.mbid";
    public static final String LOYLTY_MERCHANT_CODE = "loylty.merchant.code";
    public static final String LOYLTY_MERCHANT_PASSWORD = "loylty.merchant.password";
    public static final String LOYLTY_INIT_PAYMENT_URL = "loylty.init.payment.url";
    public static final String LOYLTY_CHECK_BALANCE_URL = "loylty.check.balance.url";
    public static final String REWARDS_CHECK_BALANCE_REQ = "REWARDS_CHECK_BALANCE";
    public static final String REWARDS_INIT_PAYMENT_REQ = "REWARDS_INIT_PAYMENT";
    public static final String REWARDS_CONFIRM_PAYMENT_REQ = "REWARDS_CONFIRM_PAYMENT";
    public static final String SESS_REWARDS_TOKEN = "REWARDS_TOKEN";
    public static final String SESS_REWARDS_REDEEM_AMOUNT = "REWARDS_REDEEM_AMOUNT";
    public static final String SESS_CARD_NUMBER = "CARD_NUMBER";
    public static final String SESS_ENCRYPTED_CARD_NUMBER = "ENCRYPTED_CARD_NUMBER";
    public static final String LOYLTY_TXN_SUCCESS = "100";
    public static final String SESS_MID = "MID";
    public static final String OPERATION_ADD_MONEY = "ADD_MONEY";
    public static final String REQUEST_ADD_MONEY = "ADD_MONEY";
    public static final String TXN_TYPE_ADD_MONEY = "ADDMONEY";
    public static final String ADD_TO_SCW = "ADD_TO_SCW";
    public static final String SCW_TO_BANK = "SCW_TO_BANK";
    public static final String OPERATION_BANK_WITHDRAW = "INIT_ADD_MONEY";
    public static final String OPERATION_WALLET_WITHDRAW = "WITHDRAW";
    public static final String USER_ACTION_CARD_INPUT = "CARD_INPUT";
    public static final String USER_ACTION_OTP_INPUT = "OTP_INPUT";
    public static final String LOYLTY_CARD_NOT_SUPPORTED = "101";
    public static final String LOYLTY_CARD_NOT_AVAILABLE = "109";
    public static final String LOYLTY_INVALID_MOBILE = "123";
    public static final String REQ_TYPE = "REQUEST_TYPE";
    public static final String INVALID_MERCHANT_ID = "Invalid Merchant Id";
    public static final String BLANK_MERCHANT_ID = "Invalid Merchant Id";
    public static final String MERCHANT_NOT_ASSOCIATED_WITH_REQ_TYPE = "Merchant is not associated with any req type";
    public static final String MANDATORY_PARAM_NOT_EXIST = "Mandatory parameter does not exist with name";
    public static final String INVALID_DATA_IN_PARAM = "Invalid Data in Parameter";
    public static final String REQ_TYPE_DOES_NOT_EXIST = "Invalid Request Type Parameter";
    public static final String MANDATORY_PARAM_IS_EMPTY = "Mandatory Param is Empty";

    // Subscription java.com.paytm.pgplus.constants
    public static final String ENTITY_ID = "entityId";
    public static final String SERVICE_ID = "serviceId";
    public static final String CUST_ID = "custId";
    public static final String FREQUENCY = "frequency";
    public static final String FREQUENCY_UNIT = "frequencyUnit";
    public static final String START_DATE = "startDate";
    public static final String RENEW_DATE = "renewDate";
    public static final String GRACE_DAYS = "graceDays";
    public static final String RETRY_ENABLED = "retryEnabled";
    public static final String RETRY_COUNT = "retryCount";
    public static final String EXPIRY_DATE = "expiryDate";
    public static final String AMOUNT = "amount";
    public static final String AMOUNT_TYPE = "amountType";
    /*
     * public static final String CYCLE_COUNT = "cycleCount"; public static
     * String CREATED_DATE = "createdDate"; public static final String
     * MODIFIED_DATE = "modifiedDate";
     */
    public static final String VALIDATE_SUBSCRIPTION_URL = "subs.validate.url";
    public static final String SUBS_RESPONSE_SUCCESS = "SUCCESS";
    public static final String SUBS_RESPONSE_FAILURE = "FAILURE";
    public static final String STATUS = "status";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MSG = "errorMsg";
    public static final String SUBS_SERVICE_ID = "SUBS_SERVICE_ID";
    public static final String SUBS_AMOUNT_TYPE = "SUBS_AMOUNT_TYPE";
    public static final String SUBS_MAX_AMOUNT = "SUBS_MAX_AMOUNT";
    public static final String SUBS_FREQUENCY = "SUBS_FREQUENCY";
    public static final String SUBS_FREQUENCY_UNIT = "SUBS_FREQUENCY_UNIT";
    public static final String SUBS_START_DATE = "SUBS_START_DATE";
    public static final String SUBS_GRACE_DAYS = "SUBS_GRACE_DAYS";
    public static final String SUBS_ENABLE_RETRY = "SUBS_ENABLE_RETRY";
    public static final String SUBS_RETRY_COUNT = "SUBS_RETRY_COUNT";
    public static final String SUBS_EXPIRY_DATE = "SUBS_EXPIRY_DATE";
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String SUBS_MIN_TXN_AMT = "subs.min.txn.amt";
    public static final String SUBS_MIN_AMOUNT = "subsMinAmount";
    public static final String SUBSCRIBE = "SUBSCRIBE";
    public static final String RENEW_SUBSCRIPTION = "RENEW_SUBSCRIPTION";
    public static final String SEAMLESS = "SEAMLESS";
    public static final String SEAMLESS_NATIVE = "SEAMLESS_NATIVE";
    public static final String SUBS_PPI_ONLY = "SUBS_PPI_ONLY";
    public static final String SUBS_INFO = "SUBS_INFO";

    public static final String SCW_INDUSTRY = "scw.industry";
    public static final String SCW_WEBSITE = "scw.website";
    public static final String SUBS = "SUBS";
    public static final String NB_LIST = "nbList";
    public static final String DC_LIST = "dcList";
    public static final String DC_ATM_LIST = "dcAtmList";
    public static final String OTHER_BANK = "otherBank";
    public static final String AMEX_INFO = "amexInfo";
    public static final String SUBS_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SUBS_SERVER_URL = "subscription.server.url";
    public static final String MINA_SERVER_URL = "mina.server.url";
    public static final String THEME_SEPRATOR = "|";
    public static final String SUBS_WALLET_WITHDRAW = "SUBS_WALLET_WITHDRAW";
    public static final String SUBS_BANK_WITHDRAW = "SUBS_BANK_WITHDRAW";
    public static final String CREDIT = "CREDIT";
    public static final String DEBIT = "DEBIT";
    public static final String PREPAID = "PREPAID";

    // Response codes
    public static final String ACCEPTED_SUCCESS = "ACCEPTED_SUCCESS";
    public static final String PROCESS_FAIL = "PROCESS_FAIL";
    public static final String PARAM_ILLEGAL = "PARAM_ILLEGAL";
    public static final String SUCCESS = "SUCCESS";
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String MERCHANT_RELATIONSHIP_ILLEGAL = "MERCHANT_RELATIONSHIP_ILLEGAL";
    public static final String SPLIT_SIZE_ILLEGAL = "SPLIT_SIZE_ILLEGAL";
    public static final String SPLIT_METHOD_NOT_EQUALS = "SPLIT_METHOD_NOT_EQUALS";
    public static final String ORDER_STATUS_INVALID = "ORDER_STATUS_INVALID";
    public static final String ORDER_NOT_EXISTS = "ORDER_NOT_EXISTS";
    public static final String REPEAT_REQUEST = "REPEAT_REQUEST";
    public static final String MUTEX_OPERATION_IN_PROCESSING = "MUTEX_OPERATION_IN_PROCESSING";

    public static final String OAUTH = "OAUTH";
    public static final String OAUTH_ADMIN_TOKEN = "oauth.admin.token";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String PAYMENT_DETAILS = "PAYMENT_DETAILS";
    public static final String TXN_FAILURE = "TXN_FAILURE";
    public static final String TXN_SUCCESS = "TXN_SUCCESS";
    public static final String SUCCESS_RESULT_STATUS = "S";
    public static final String FAILURE_RESULT_STATUS = "F";

    // , , , ,
    // Table Column Name
    public static final String BANK_RESP_AT = "BANK_RESP_AT";
    public static final String TO_BANK_AT = "TO_BANK_AT";
    public static final String TO_MINA_AT = "TO_MINA_AT";
    public static final String CANCEL_AT = "CANCEL_AT";
    public static final String PAGE_LOAD_AT = "PAGE_LOAD_AT";
    public static final String WALLET_UNAUTHORIZED_ACCESS = "403";
    public static final String OAUTH_ADMIN_USERNAME = "oauth.admin.username";
    public static final String OAUTH_ADMIN_PASSWORD = "oauth.admin.password";
    public static final String COOKIE_DISABLED_THEME = "IFRAME";

    public static final String OFFER_MSG = "offerMsg";

    public static final String WALLET_UP_PROPERTY = "wallet.up";
    public static final String WHITE_LIST_MID_INDUSTRY = "white.list.mid.industry";

    public static final String LIMIT_FAIL_RBI_WALLETPG_SUFF = "limitFail.rbi.walletPG.sufficient";
    public static final String LIMIT_FAIL_RBI_WALLETONLY_SUFF = "limitFail.rbi.walletOnly.sufficient";
    public static final String LIMIT_FAIL_RBI_WALLETPG_INSUFF = "limitFail.rbi.walletPG.insufficient";
    public static final String LIMIT_FAIL_RBI_WALLETONLY_INSUFF = "limitFail.rbi.walletOnly.insufficient";

    public static final String LIMIT_FAIL_SCW_WALLETONLY = "limitFail.scw.walletOnly";
    public static final String LIMIT_FAIL_SCW_WALLETPG = "limitFail.scw.walletPG";
    public static final String WALLET_BALANCE_NOT_AVAILABLE_WALLET_ONLY = "balance.not.available.walletOnly";
    public static final String WALLET_BALANCE_NOT_AVAILABLE_WALLETPG = "balance.not.available.walletPG";

    public static final String LIMIT_FAIL_SSO_WALLETONLY = "limitFail.sso.walletOnly";
    public static final String LIMIT_FAIL_SSO_WALLETPG = "limitFail.sso.walletPG";

    /*
     * public static final String WALLET_NOT_EXITS ="236"; public static String
     * TRY_AGAIN ="237"; public static final String INVALID_CURR ="238"; public
     * static String INVALID_MERCHANT ="239"; public static final String
     * INVALID_AMOUNT ="240"; public static final String FIRST_CHILD_FAILED
     * ="241"; public static String CHILD_TXN_AUTO_REFUND ="242"; public static
     * String TXN_WALLET_NOT_CREATED ="243"; public static final String
     * WALLET_NOT_ACTIVATED ="244"; public static final String
     * REDEMPTION_NOT_ALLOWED = "245"; public static String
     * OTP_VALIDATION_ATTEMPTS_EXCEEDED = "246"; public static String
     * DUPLICATE_ID ="223"; public static final String LOW_BALANCE ="235"; //
     * Wallet velocity limits public static final String PER_TXN_AMT ="155";
     * public static String PER_DAY_TXN_AMT ="156"; public static String
     * PER_WEEK_TXN_AMT ="157"; public static final String PER_MON_TXN_AMT
     * ="158"; public static final String PER_DAY_TXN_COUNT ="159"; public
     * static String PER_WEEK_TXN_COUNT ="160"; public static final String
     * PER_MON_TXN_COUNT ="161"; public static final String MAX_WRONG_ATTAMPTS
     * ="162"; public static String THROUGHPUT_LIMIT_FAILED ="164";
     */

    public static final String VELOCITY_FAILED = "163";
    public static final String PROMO_MSG_OLD_CARD_TRANSACTED = "promo.old.card.txn";
    public static final String PROMO_MSG_MAX_COUNT_EXCEEDED = "promo.max.card.count.crossed";
    public static final String PROMO_MSG_MAX_AMOUNT_EXCEEDED = "promo.max.card.amount.crossed";
    public static final String PROMO_MSG_OLD_CUST_TRANSACTED = "promo.old.cust.transacted";
    public static final String AUTO_CREATE = "AUTO_CREATE";
    public static final String RUPAY_CARD_ERROR_MSG = "rupay.card.error.msg";

    public static final String WALLET_ENABLED = "walletEnabled";
    public static final String IS_RETRY = "isRetry";
    public static final String DISPLAY_MSG = "displayMsg";
    public static final String TXN_STATUS = "TXN_STATUS";
    public static final String ERROR = "ERROR";
    public static final String TXN_RETRYING = "TXN_RETRYING";
    public static final String TXN_ID = "TXN_ID";
    public static final String COMPOSITE_TXN_ID = "COMPOSITE_TXN_ID";
    public static final String PAYMENT_INFO = "paymentInfo";
    public static final String OAUTH_CLIENT_ID = "oauth.client.id";
    public static final String OAUTH_WAP_SECRET_KEY = "oauth.wap.client.secret.key";
    public static final String OAUTH_CLIENT_SECRET_KEY = "oauth.client.secret.key";
    public static final String OAUTH_CLIENT_WAP_ID = "oauth.wap.client.id";
    public static final String OAUTH_NOCOOKIE_CLIENT_SECRET_KEY = "oauth.nocookie.client.secret.key";
    public static final String OAUTH_NOCOOKIE_CLIENT_ID = "oauth.nocookie.client.id";
    public static final String OAUTH_NOCOOKIE_CLIENT_MIDS = "oauth.nocookie.client.mids";
    public static final String OAUTH_OTP_CLIENT_SECRET_KEY = "oauth.otp.client.secret.key";
    public static final String OAUTH_OTP_CLIENT_ID = "oauth.otp.client.id";
    public static final String OAUTH_OTP_CLIENT_MIDS = "oauth.otp.client.mids";
    public static final String OAUTH_HOST = "oauth.host";
    public static final String OAUTH_RES_SERVER_URI = "oauth.response.server.url";
    public static final String SESS_MERCHANT_IMAGE = "merchantImage";
    public static final String WALLET_FAILED = "walletFailed";
    public static final String CARD_TYPE = "CARD_TYPE";
    public static final String IS_WALLET_ONLY = "isWalletOnly";
    public static final String RESPONSE_URL = "responseURL";
    public static final String ORDERID_PER_MID = "orderIdPerMid";
    public static final String INFO_TO = "infoTo";
    public static final String RETRYCOUNT = "RETRY_COUNT";
    public static final String SHOPPING_CART = "shoppingCart";
    public static final String REQ_OATH = "oauth";
    public static final String THEME_UPPER = "THEME";
    public static final String TOKEN = "TOKEN";
    public static final String CART_DETAIL = "cart_detail";
    public static final String SELECTED_BANK_ID = "SELECTED_BANK_ID";
    public static final String WALLET_FAILED_MSG = "walletFailedMsg";
    public static final String WALLET_INACTIVE = "walletInactive";
    public static final String WALLET_APPLY = "applyWallet";
    public static final String INDUSTRY_TYPE = "INDUSTRY_TYPE";
    public static final String RETAIL = "Retail";
    public static final String COMPLETE_DC_LIST = "completeDcList";
    public static final String LAST_PAY_MODE = "LastPayMode";
    public static final String SAVE_CARD_ID = "SaveCardId";
    public static final String CONFIG_CHANNEL = "configuredChannels";
    public static final String FILTER_CONFIG_CHANNEL = "filteredConfiguredChannels";
    public static final String TXN_INFO = "TXN_INFO";
    public static final String AUTH_INFO = "oauth_info";
    public static final String AUTH_INFO_HOST = "oauth_info_host";
    public static final String AUTH_INFO_CLIENT_ID = "oauth_info_client_id";
    public static final String AUTH_INFO_RETURN_URL = "oauth_info_return_url";
    public static final String AUTH_LOGOUT_URL = "AUTH_LOGOUT_URL";
    public static final String AUTH_LOGOUT_RETURN_URL = "AUTH_LOGOUT_RETURN_URL";
    public static final String MERCHANT_KEY = "merchantKey";
    public static final String TRANSIENT_TXN_INFO = "transientTxnInfo";
    public static final String THREE_D = "3D";
    public static final String SUBS_ALL_MODES = "ALL";
    public static final String SUBS_PAYMENT_MODE = "SUBS_PAYMENT_MODE";
    public static final String LOGIN_RETRY_COUNT = "LOGIN_RETRY_COUNT";
    public static final String REQUEST_METHOD = "REQUEST_METHOD";

    public static final String SAVED_CARD_ID = "SAVED_CARD_ID";
    public static final String CALL_TYPE = "CONNECTION_TYPE";
    public static final String S2S = "S2S";

    public static final String ENC_PARAMS = "ENC_DATA";
    public static final String ENC_PREFERENCE = "ENCPARAMS_ENABLED";
    public static final String INVALID_ENC_PARAMS = "1201";
    public static final String BASE_AMOUNT = "BASE_AMOUNT";
    public static final String CONFIG_CARD_TYPE = "configuredCardType";
    public static final String PGTRACKPOJO = "pgTrackPojo";
    public static final String IS_SAVED_CARD = "IS_SAVED_CARD";
    public static final String STORE_CARD = "STORE_CARD";
    public static final String SSOID = "SSOID";
    public static final String PREFERENCES = "PREFERENCES";
    public static final String PREFERENCE_TYPE = "PREFERENCE_TYPE";
    public static final String WithoutConvenienceFee = "WithoutConvenienceFee";

    public static final String PAYMENT_CHARGES = "paymentCharges";
    public static final String PAYMENT_CHARGE_INFO = "paymentChargesInfo";
    public static final String MERCHANT_3 = "merchant3";

    public static final String TARGET = "target";
    public static final String ACTION_SUBMIT = "Submit";
    public static final String ACTION_ACTION = "action";
    public static final String CHANNELID = "channelId";
    public static final String EMI_PLAN_ID = "emiPlanId";
    public static final String EMI_BANK_NAME = "emiBankName";
    public static final String CARDTYPE = "cardType";
    public static final String REQ_BANK_CODE = "reqBankCode";
    public static final String ADD_MONEY = "addMoney";
    public static final String REWARD_AMT_MISMATCH = "rewardsAmountMismatch";
    public static final String NOT_AJAX_CALL = "notAjaxCall";
    public static final String BANK_HTML = "bankHTML";
    public static final String VALIDATION_ERROR = "validationErrors";
    public static final String ADD_MONEY_FLAG = "addMoneyFlag";
    public static final String WALLET_AMOUNT = "walletAmount";
    public static final String SCARD_ID = "SCARD_ID";
    public static final String SECURE_CODE = "secureCode";
    public static final String PROP_COD_MIN_AMOUNT = "cod.min.amount";
    public static final String COD_HYBRID_ALLOWED = "codHybridAllowed";
    public static final String DEFAULTFEE = "DEFAULTFEE";
    public static final String FEE = "fee";
    public static final String SHOW_REWARDS_OTP = "showRewardsOTP";
    public static final String REWARDS_BALANCE_POINTS = "rewardsBalancePoints";
    public static final String REWARDS_BALANCE_AMOUNT = "rewardsBalanceAmount";
    public static final String REWARDS_INVALID_MOBILE = "rewardsInvalidMobile";
    public static final String REWARDS_ERROR = "rewardsError";
    public static final String REWARDS_INSUFFICIENT_BALANCE = "rewardsInsufficientBalance";
    public static final String REWARDS_INVALID_CARD = "rewardsInvalidCard";
    public static final String ERROR_PAGE = "error";
    public static final String OCP_ENABLED = "OCP_ENABLED";
    public static final String CHECKSUM_ENABLED = "CHECKSUM_ENABLED";
    public static final String STORE_CARD_DETAILS = "STORE CARD DETAILS";
    public static final String ADD_MONEY_ENABLED = "ADD_MONEY_ENABLED";
    public static final String HYBRID_ALLOWED = "HYBRID_ALLOWED";
    public static final String AUTO_CREATE_USER = "AUTO_CREATE_USER";
    public static final String MERC_UNQ_REF = "MERC_UNQ_REF";
    public static final String UNQREF_REQUEST = "UNQREF_REQUEST";
    public static final String AUTO_LOGIN_FLAG = "autoLogin";
    public static final String SELLER_NAME = "SELLER_NAME";
    public static final String STORE_NAME = "STORE_NAME";
    public static final String CATEGORY_NAME = "CATEGORY_NAME";
    public static final String SKU_NAME = "SKU_NAME";
    public static final String entityId = "entityId";
    public static final String SESS_CURRENCY_CODE = "CURRENCY_CODE";
    public static final String ESCROW = "ESCROW";
    public static final String CUSTOMER_TYPE = "customerType";
    public static final String P2M = "P2M";

    // for offline payment mode
    public static final String OFFLINE_TXN_ID = "offlineTxnId";
    public static final String OFFLINE = "OFFLINE";
    public static final String OFFLINE_LIST = "offlineList";
    // offline params end

    public static final String NEFTOFF = "NEFTOFF";
    public static final String CUST_NAME = "CUST_NAME";

    // for QR Mode
    public static final String REQUEST_QR_MERCHANT = "QR_MERCHANT";
    public static final String UPI_QR_CODE = "UPI_QR_CODE";
    public static final String REQUEST_QR_ORDER = "QR_ORDER";
    public static final String IS_ALL_IN_ONE_QR_ENABLED = "theia_isAllInOneQREnabledNonPCF";
    public static final String IS_ALL_IN_ONE_QR_ENABLED_PCF = "theia_isAllInOneQREnabledPCF";
    public static final String REMOVE_CALLBACK_FROM_WALLET_QR_REQUEST = "theia.removeCallbackFromWalletQrRequest";

    public static final String COBRANDED_CARD_PREFIX = "cobranded.card.prefix";
    public static final String QR_SUBSCRIPTION_CALLBACK_URL = "qr.subscription.callback.url";
    // public static final String REQUEST_QR_PRODUCT = "QR_PRODUCT";
    // public static final String REQUEST_QR_PRICEDPRODUCT = "QR_PRICEDPRODUCT";
    // public static final String REQUEST_QR_CHECKOUT = "QR_CHECKOUT";

    // for link
    public static final String LINK_INVOICE_ID = "link_invoice_id";
    public static final String LINK_TYPE = "linkType";
    public static final String INVOICE = "INVOICE";
    public static final String UPI_POS_ORDER = "UPI_POS_ORDER";

    public static final String PRODUCT_ID = "PRODUCT_ID";
    public static final String PRODUCT_TYPE = "PRODUCT_TYPE";
    // public static final String QR_ = "QR_";

    public static final String PAYMENT_OTP_REQUIRED_SESSION_ATTRIBUTE = "isPaymentOtpRequired";
    public static final String PAYMENT_MODE_DISABLED = "PAYMENT_MODE_DISABLE";
    public static final String IS_KYC = "isKYC";

    public static final String CARDNUMBER_DISPLAY = "cardNumber";
    public static final String EXPIRYMONTH_DISPLAY = "expiryMonth";
    public static final String EXPIRYYEAR_DISPLAY = "expiryYear";
    public static final String CARD_TYPE_ERROR_MSG = "card.type.error.msg";
    public static final String COMMON_LOGIN_MID_PROP = "common.login.mid";
    public static final String IS_USER_VERIFIED = "IS_USER_VERIFIED";

    /**
     * EMI SUBVENTION TOKEN
     */
    public static final String EMI_SUBVENTION_SITE_ID = "1";
    public static final String EMI_SUBVENTION_USER_ID = "user_id";
    public static final String EMI_SUBVENTION_PG_MID = "pg-mid";
    public static final String EMI_SUBVENTION_CUST_ID = "customer-id";

    // PayMode
    public static final String NONE = "NONE";
    public static final String ADDANDPAY = "ADDANDPAY";

    public static final String PAYTM_TOKEN_SCOPE = "paytm";
    public static final String WALLET_TOKEN_SCOPE = "wallet";

    public static final String SUBSCRIPTION_SUCCESS_CODE = "900";
    public static final String SUBSCRIPTION_ACTIVATION_SUCCESS_CODE = "925";
    public static final String RENEWAL_SUCCESS_CODE = "900";

    public static final String RISK_RESULT_ACCEPT = "ACCEPT";

    public static final String CHANNEL_WEB = "WEB";
    public static final String CHANNEL_WAP = "WAP";

    public static final String MAPPING_USER_CACHE = "MAPPING_USER_CACHE";

    public static final String MERCHANT_MAPPING_CACHE = "MERCHANT_MAPPING_CACHE";

    public static final String MAPPING_SERVICE_URL_KEY = "mapping.service.base.url";

    public static final String WALLET_SCOPE_TOKEN = "wallet";

    public static final String CONTEXT_PATH_KEY = "context.path";

    public static final String BROWSER_USER_AGENT = "browserUserAgent";

    public static final String IS_EMI = "isEmi";

    public static final String ZERO = "0";

    // Parameter for Seamless UPI
    public static final String VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddr";
    public static final String PAYTM_VPA_HANDLE_NAME = "paytm";
    public static final String UPI_MANDATE = "isUPIMandate";
    public static final String ZERO_RUPEE_TXN = "isZeroRupeeTxn";
    public static final String ACTUAL_MID = "actualMid";
    public static final String ACTUAL_ORDERID = "actualOrderId";
    public static final String IS_ADD_AND_PAY = "isAddNPay";

    // for UPI PUSH
    public static final String UPI_ACC_REF_ID = "upiAccRefId";
    // Risk parameter
    public static final String VPA = "vpa";

    public static final int MIN_POOLSIZE = Integer.parseInt(ConfigurationUtil.getProperty("pool.minSize"));
    public static final int MAX_POOLSIZE = Integer.parseInt(ConfigurationUtil.getProperty("pool.maxSize"));
    public static final int KEEPALIVE_TIME = Integer.parseInt(ConfigurationUtil.getProperty("pool.keepAliveTime"));
    public static final String PLATFORM_PLUS_CLOSE_ORDER_TIME = ConfigurationUtil.getProperty(
            "platform.plus.close.order.timeout.second", "10");
    public static final String POST_RESPONSE_ORDER_TIMEOUT = ConfigurationUtil.getProperty(
            "post.response.order.timeout.seconds", "20");

    public static final String CUST_ID_REGEX = "^[a-zA-Z0-9-|_@.-]*$";
    public static final String SUBS_CUST_ID_REGEX = "^[a-zA-Z0-9-|_@.=-]*$";

    public static final BigDecimal BIG_DECIMAL_1200 = new BigDecimal("1200");

    public static final BigDecimal BIG_DECIMAL_1 = new BigDecimal("1");

    public static final String PAYOPTION_UPI_PUSH = "UPI_PUSH";
    public static final String PAYCHANNEL_UPI_PUSH = "UPIPUSH";
    public static final String PAYCHANNEL_UPI_PUSH_EXPRESS = "UPIPUSHEXPRESS";

    public static final String QR_ORDER = "qrOrder";
    public static final String ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE = "addAndPayLitePayviewConsultResponse";
    public static final String ADD_AND_PAY_TOKENIZED_CARDS_CONSULT_RESPONSE = "addAndPayTokenizedCardsConsultResponse";
    public static final String SUBVENTION_CHECKOUT_RESPONSE = "subventionCheck";
    public static final String SUBVENTION_CHECKOUT_WITH_ORDER_RESPONSE = "subventionCheckoutWithOrder";
    public static final String SUBVENTION_ORDERSTAMP_KEY = "subventionOrderStamp";
    public static final String IS_UPILITE_TRANSACTION = "isUpiLiteTransaction";
    public static final String UPILITE_REQUEST_DATA = "upiLiteRequestData";

    /*
     * EMI subvention constants
     */
    public static final String SUBVENTION_CUSTOMER_ID = "customer-id";
    public static final String PG_MERCHANT_ID = "pg-mid";
    public static final String EMI_SUBVENTION_SITEID = "1";
    public static final String SUBVENTION_USER_ID = "user_id";
    public static final String SUBVENTION_ORDER_ID = "order-id";
    public static final String SUBVENTION_SSO_TOKEN = "sso_token";
    public static final String SUBVENTION_ORDERSTAMP_RETRY_COUNT = "subvention.orderstamp.retry.count";
    public static final String SUBVENTION_CHECKOUT_RETRY_COUNT = "subvention.checkout.retry.count";
    public static final String SUBVENTION_OFFER_FAILURE_MESSAGE = "subvention.offer.failure.msg";
    public static final String SUBVENTION_STATUS_UPDATE_RETRY_COUNT = "subvention.orderstamp.retry.count";

    public static final String ADD_AND_PAY_LITEPAYVIEW_CONSULT_EXPIREY = "add.and.pay.litepayview.consult.cache.expiry.seconds";
    public static final String SUBVENTION_KEY_EXPIRY = "subvention.key.expiry";

    public static final String TLS_WARNING_MESSAGE = "tls.warning.message";
    public static final String QR_SUCCESS_CODE = "QR_0001";
    public static final String QR_REPEAT_SUCCESS_CODE = "QR_1020";
    public static final String UPI_QR_REPEAT_SUCCESS_CODE = "QR_1033";
    public static final String QR_SUCCESS = "SUCCESS";

    public static final String LITEPAYVIEW_CONSULT_CACHE_EXPIREY_SECONDS = "litepayview.consult.cache.expiry.seconds";
    public static final String LITEPAYVIEW_CONSULT_RESPONSE = "litePayviewConsultResponse_";
    public static final String TOKENIZEDCARDS_CONSULT_RESPONSE = "tokenizedCardsConsultResponse_";
    public static final String IS_LITEPAYVIEW_CONSULT_CACHE_ENABLED = "is.litepayview.consult.cache.enabled";

    public static final String FETCH_PAY_MODE_ERROR_MESSAGE = "Could not fetch payment modes";
    public static final String FETCH_TOKENIZED_CARDS_ERROR_MESSAGE = "Could not fetch tokenized cards";
    public static final String PAY_REQUEST_FAIL_MESSAGE = "Pay Request Generation Failed";
    public static final String TOKENIZED_CARDS_REQUEST_FAIL_MESSAGE = "Tokenized Cards Request Generation Failed";
    public static final String CHANNEL_ACCOUNT_QUERY_REQUEST_FAILED = "Channel Account Query Request Failed";
    public static final String FETCH_CHANNEL_ACCOUNT_QUERY_ERROR_MESSAGE = "Could not fetch channel account info";
    public static final String LOYALTY_POINT_NOT_ENABLED = "Loyalty Point not configured";
    public static final String LOG_EXCEPTION_MESSAGE = "Exception Occured ";
    public static final String PASS_THROUGH_EXTEND_INFO_KEY = "passThroughExtendInfo";
    public static final String userId = "userId";
    public static final String accountType = "accountType";

    public static final String IS_LITEPAYVIEW = "is.litepayview";
    // Merchant velocity limit specific
    public static final String LIMIT_DURATION_MONTH = "MONTH";
    public static final String LIMIT_TXN_TYPE_ANY = "ANY";
    public static final String VELOCITY_SERVICE_FAIL_MESSAGE = "FAIL";
    public static final String VELOCITY_LIMIT_ENABLED_FLAG_TRUE = "true";
    public static final String VELOCITY_LIMIT_ENABLED_FLAG_FALSE = "false";

    // max execution time of tasks
    public static final String FILTER_TOKENIZED_PLATFORM_CARDS_TIME = "maxtime.filterTokenizedPlatformCards";
    public static final String LITEPAYVIEW_TIME = "maxtime.litePayView";
    public static final String AUTHENTICATE_USER_TIME = "maxtime.authenticate.user";
    public static final String CREATE_ORDER_TIME = "maxtime.create.order";
    public static final String CREATE_TOPUP_TIME = "maxtime.create.topup";
    public static final String FETCH_SAVED_CARDS_TIME = "maxtime.fetch.saved.cards";
    public static final String PAYMENTS_BANK_BALANCE_TIME = "maxtime.fetch.payment.bank.balance";
    public static final String VALIDATE_REQUEST_TIME = "maxtime.validate.request";
    public static final String FETCH_VPA_TIME = "maxtime.fetch.vpa";
    public static final String PAYMODE_DECISION_MAKER_TIME = "maxtime.paymode.decision.maker";
    public static final String CHANNEL_ACCOUNT_QUERY_TIME = "maxtime.channel.account.query";
    public static final String FRESH_SUBSCRIPTION_CONTRACT_TIME = "maxtime.fresh.subscription.contract";
    public static final String CONSULT_FEE_TIME = "maxtime.consult.fee";
    public static final String FETCH_SAVED_CARDS_NO_FILTER_TIME = "maxtime.fetch.saved.cards.no.filter";
    public static final String FETCH_SAVED_CARD_LIMIT_TIME = "maxtime.fetch.saved.card.limits";
    public static final String FILTER_SAVED_CARDS_TIME = "maxtime.filter.saved.cards";
    public static final String FETCH_POSTPAID_BALANCE = "maxtime.fetch.postpaid.balance";
    public static final String FETCH_PPBL_BALANCE = "maxtime.fetch.ppbl.balance";
    public static final String FETCH_WALLET_BALANCE = "maxtime.fetch.wallet.balance";
    public static final String VALIDATE_USER_FOR_ADVANCE_DEPOSIT = "maxtime.validate.user.advance.deposit";
    public static final String CREATE_DYNAMIC_QR_TIME = "maxtime.create.dynamic.qr";
    public static final String FETCH_MGV_BALANCE = "maxtime.fetch.mgv.balance";
    public static final String FETCH_UPI_PROFILE = "maxtime.fetch.upi.profile";
    public static final String FETCH_MANDATE_BANK_AND_PSP = "maxtime.fetch.mandate.bankpsp";
    public static final String FETCH_CHANNEL_DETAILS_TIME = "maxtime.fetch.channel.details";
    public static final String SMS_APP_INVOKE_TIME = "maxtime.sms.app.invoke";
    public static final String SHORTEN_URL_TIME = "maxtime.shorten.url";
    public static final String NOTIFICATION_APP_INVOKE_TIME = "maxtime.notification.app.invoke";
    public static final String THEIA_SHORTEN_URL_TTL = "theia.shortenURL.ttl";
    public static final String THEIA_SEND_NOTIFICATION_TIMEOUT = "theia.send.notification.timeout";
    public static final String THEIA_RATE_LIMIT_SEND_NOTIFICATION = "theia.rate.limit.send.notification";
    public static final String THEIA_SEND_PUSH_NOTIFICATION_APP_INVOKE_ENABLE = "theia.send.push.notification.app.invoke.enable";
    public static final String GET_TOKENIZED_CARDS_TIME = "maxtime.tokenizedCardsTime";
    public static final String FETCH_USER_PREFERENCES_TIME = "maxtime.fetch.user.preferences";

    public static final String METRIC_REPORTER_TIME_SECONDS = "metric.reporter.time";
    public static final String TASKED_EXECUTOR_THREAD_COUNT = "tasked.executor.thread.count";
    public static final String CASHIER_REQUEST_DATA_KEY_EXPIRY = "close.order.cashier.request.expiry";
    public static final String ONE_CLICK_DATA_KEY_EXPIRY = "one.click.data.request.expiry";
    public static final String INTERNAL_RETRY_BLACKLIST_CODES = "internal.retry.blacklist.codes";
    public static final String AUTH_USER_TYPE_ATTRIBUTE_TIME = "maxtime.auth.user.type.attribute";
    public static final String RISK_POLICY_CONSULT_TIME = "maxtime.risk.policy.consult";
    public static final String MERCHANT_TOTAL_LIMIT_TIME = "maxtime.merchant.total.limit.time";
    public static final String MERCHANT_ACCUMULATED_LIMIT_TIME = "maxtime.merchant.accumulated.limit.time";
    public static final String MERCHANT_EMI_DETAILS_TIME = "maxtime.merchant.emi.details";
    public static final String EMI_SUBVENTION_BULK_FETCH_CARD_INDEX = "maxtime.bulk.fetch.card.index.emi.subvention";
    public static final String EMI_SUBVENTION_BULK_SAVECARD_TIME = "maxtime.bulk.savecard.emi.subvention";
    public static final String EMI_SUBVENTION_BANKS_TIME = "maxtime.bank.emi.subvention";
    public static final String REAL_TIME_RECON_EXECUTION_TIME = "maxtime.real.time.savedcard.recon";
    public static final String FILTER_PLATFROM_SAVED_ASSETS_EXECUTION_TIME = "maxtime.filter.platform.saved.assets";
    public static final String FILTER_PLATFROM_SAVED_ASSETS_SUBSCRIPTION_EXECUTION_TIME = "maxtime.filter.platform.saved.assets.subscription";
    public static final String MODIFIED_LOOPER_TIMEOUT_TO_FECTH_BANKFORM = "modified.looper.timeout.to.fetch.bankform";
    public static final String SUPPORTED_PAYMODE_TO_DISABLE_BANKFORM_RETRY = "supported.paymode.to.disable.bankform.retry";
    public static final String MERCHANT_ONLY_CONTRIBUTION_EMI_BANKS = "merchant.only.contribution.emi.banks";
    public static final String LOOPER_CUSTOM_TIMEOUT = "theia.looper.custom.timeout";
    public static final String FETCH_WORKFLOW_ID_WALLET_2FA_TIME = "maxtime.fetch.workflowid.wallet2fa";

    public static final String FAILURE = "FAILURE";
    public static final String QUERY_PARAMS_DATA_KEY_EXPIRY = "query.params.data.key.expiry";

    // ExtendInfo For Wallet Balance Response
    public static final String OTHER_SUB_WALLET_BALANCE = "otherSubWalletBalance";
    public static final String WALLET_GRADE = "walletGrade";
    public static final String OWNER_GUID = "ownerGuid";
    public static final String TOTAL_BALANCE = "totalBalance";
    public static final String PAYTM_WALLET_BALANCE = "paytmWalletBalance";
    public static final String SSO_ID = "ssoId";
    public static final String MP_ADD_MONEY_MID = "MP.ADD.MONEY.MID";
    public static final String DYNAMIC_QR_FF4J_ENABLED = "dynamic.qr.ff4j.enabled";
    public static final String GV_CONSENT_FLOW_FF4J_ENABLED = "gv.consent.flow.ff4j.enabled";
    public static final String ADD_MONEY_FEE_WALLET_CONSULT = "theia_addMoneyFeeWalletConsult";
    public static final String DISABLE_CHARGE_IN_DYNAMIC_QR_2FA = "dynamicQr2fa.charge.disable";
    public static final String ENABLE_QR_CODE_ON_ENHANCE = "theia.enhance.allInOneQr.enable";
    public static final String ENABLE_ADD_MONEY_SOURCE_IN_CONSULT = "theia.add.money.source.consult.enable";

    public static final String PENNY_DROP_DUMMY_MID = "PENNY.DROP.DUMMY.MID";
    public static final String DEFAULT_PENNY_DROP_DUMMY_MID = "PTMSDU36026554284925";

    // Merchant Pyaymodes limit
    public static final String PAYMODES_LIMIT_TYPE_LIST = "paymodes.limit.type.list";
    public static final String PPI_LIMIT_PREFIX = "PPI_LIMIT_";
    public static final String MERCHANT_PAYMODES_LIMIT_TIME = "maxtime.merchant.paymodes.limit.time";
    public static final String MERCHANT_PAYMODES_LIMIT_TASK_ENABLE = "merchant.paymodes.limit.task.enable";

    // For international Payments
    public static final String INTERNATIONAL_PAYMENT_KEY = "supportCountries";
    public static final String INTERNATIONAL_PAYMENT_VALUE = "INTL";
    public static final String INTERNATIONAL_CARD_PAYMENT = "internationalCardPayment";
    public static final String NATIONAL_PAYMENT_VALUE = "IN";

    // global card limit
    public static final String GLOBAL_SAVED_ASSETS_LIMIT = "global.saved.assets.limit";
    public static final String CHECKOUT = "checkout";
    public static final String CHECKOUT_JS_WORKFLOW = "checkoutJsFlow";
    public static final String COLLECT_API_INVOKE = "collectAppInvoke";

    // upi intent deeplink info
    public static final String SEND_DEEPLINK_INFO_INSTA = "send.deeplink.info.insta";

    // upi profile cache constants
    public static final String USER_PROFILE_V4_CACHE_CONSTANT = "userProfileV4_";
    public static final String USER_PROFILE_V2_CACHE_CONSTANT = "userProfileV2_";

    // Added Constant for DatEnrichment for below Flows
    public static final String ENHANCED_CASHIER_FLOW = "enhancedCashierFlow";
    public static final String PCF_FLOW = "pcf";
    public static final String AUTH_FLOW_COLLECT = "collect";
    public static final String AUTH_FLOW_INTENT = "intent";
    public static final String AUTH_FLOW_PUSH = "push";
    public static final String AUTH_FLOW_REDIRECT_TO_BANK = "redirectToBank";
    public static final String AUTH_FLOW_PASSCODE_ON_PG_PAGE = "passcodeOnPGPage";
    public static final String AUTH_FLOW_PASSCODE_ON_PAYTM_APP = "passcodeOnPaytmApp";
    public static final String APP_NEW_VERSION_FLOW = "appNewVersionRequest";

    public static final String FLOW = "businessFlow";
    public static final String PUSH_DATA_TO_DYNAMIC_QR_KEY = "pushDataToDynamicQR";

    // These constants are part of PGP-33786
    public static final String UPI_TO_ADDNPAY_PROMO_CODE_KEY = "upiToAddnPayPromoCode";
    public static final String UPI_TO_ADDNPAY_PROMO_CODE = "upi.to.addnpay.promo.code";
    public static final String CONVERT_TXN_TO_ADDNPAY_OFFER_DETAILS = "convert.txn.to.addnpay.offer.details";

    public static final String REDEMPTION_TYPE_DISCOUNT = "discount";

    public static final String CART = "cart";
    public static final String REDEMPTION_TYPE_CASHBACK = "cashback";
    public static final String REDEMPTION_TYPE_PAYTM_CASHBACK = "paytm_cashback";
    public static final String PROMO_CHECKOUT_RETRY_COUNT = "promoCheckoutRetryCount";
    public static final String DISABLE_PROMO_CHECKOUT_RETRY = "theia.disablePromoCheckoutRetry";
    public static final String CLIENT_PG = "PG";
    public static final String CLIENT_EDC = "EDC";
    public static final String EDC_LINK_ADD_SUBVENTION_AMOUNT_BANK_LIST = "edc.link.add.subvention.amount.bank.list";
    public static final String DISCOUNT = "DISCOUNT";
    public static final String EMI_CATEGORY = "emiCategory";
    public static final String BRAND_EMI = "brandEmi";
    public static final String BANK_EMI = "bankEmi";

    public static final String COFT_CONSENT_CACHE_KEY_EXPIRY = "coft.consent.cache.key.expiry";
    public static final String LP_ROOT_USER_ID = "loyaltyPointRootUserId";
    public static final String LP_EXCHANGE_RATE = "exchangeRate";
    public static final String LP_POINT = "pointNumber";
    public static final String INST_ID = "instId";
    public static final String INST_OFFICIAL_NAME = "instOfficialName";
    public static final String TOKEN_LAST_USED_TIME = "tokenLastUsedTime";
    public static final String GET_PLATFORM_AND_TOKENIZED_CARDS_TIME = "maxtime.platformAndtokenizedCardsTime";
    public static final String CARD_NO_LENGTH = "cardNoLength";
    public static final String MASK_CARD_NUMBER = "maskedCardNo";
    public static final String MIN_POSTPAID_BALANCE_AMOUNT = "min.postpaid.balance.amount";
    public static final String MAX_POSTPAID_BALANCE_AMOUNT = "max.postpaid.balance.amount";
    public static final String COBRANDED_COFT_CARD_PREFIX = "Paytm";
    public static final String COBRADED_CUSTOM_DISPLAY_NAME = "coBrandedCustomDisplayName";
    public static final String IS_MANDATE = "isMandate";
    public static final String UPI_INTENT_DIRECT_SETTLEMENT_TIMEOUT_IN_SECS = "upi.intent.direct.settlement.order.timeout.seconds";
    public static final String PPBL_FAILURE_STATUS = "FAILURE";
    public static final String ONE_CLICK_PAY = "ONE_CLICK_PAY";
    public static final String ONE_CLICK_ENROLL = "ONE_CLICK_ENROLL";
    public static final String PASSCODE_SET_W2FA_WEB = "PASSCODE_SET_W2FA_WEB";
    public static final String ADD_MONEY_SURCHARGE = "addMoneySurcharge";
    public static final String THIRD_PARTY_CONV_FEE = "is3PconvFee";
    public static final String THIRD_PARTY_CONV_FEE_AMOUNT = "3PconvFeeAmount";
    public static final String FEE_RATE_CODE = "feeRateCode";
    public static final String PAYMENT_METHOD = "payMethod";
    public static final String ADD_MONEY_SURCHARGE_FLAG_TRUE = "true";
    public static final String PG2_MERCHANT_CENTER_BASE_URL = "merchant.center.base.url";
    public static final String MERCHANT_CENTER_EMI_URL = "merchant.center.emi.url";
    public static final String IOSAPP = "iosapp";
    public static final String ANDROIDAPP = "androidapp";
    public static final String MERCHANT_SOLUTION_TYPE = "merchantSolutionType";
    public static final String MERCHANT_PREFERENCE = "merchantPreference";
    public static final String OFFLINE_MERCHANT = "offline";

    public static final class WalletRbiType {
        public static final String FULL_KYC = "Premium";
        public static final String E_KYC = "Adhaar OTP Kyc";
        public static final String MIN_KYC = "Min Kyc";
    }

    public static final class AddMoneyDestination {
        public static final String GIFT_VOUCHER = "GIFT_VOUCHER";
        public static final String MAIN = "MAIN";
    }

    public static final class MerchantVelocity {
        public static final String BYPASS_VELOCITY_MIDS = "bypass.velocity.mids";
        public static final String ALL = "ALL";
        public static final String NONE = "NONE";
        public static final String DELIMITER = ",";
    }

    public static final class Ff4jFeature {
        public static final String AUTO_APP_INVOKE = "theia.autoAppInvokeAllowed";
        public static final String BLACKLIST_THEIA_SEND_CIN_AND_8BINHASH_PROMO = "theia.blackListSendCINAnd8BinHashToPromo";
        public static final String STORE_UPI_PROFILE_RESPONSE_IN_CACHE = "theia.storeUpiProfileInCache";
        public static final String BLACKLIST_INTERNAL_FETCH_UPI_PROFILE_V1_PTC = "theia.blacklistInternalFetchUpiProfileV1PTC";
        public static final String BLACKLIST_INTERNAL_FETCH_UPI_PROFILE_V2_FPO = "theia.blacklistInternalFetchUpiProfileV2FPO";
        public static final String BLACKLIST_LPV_FPOV2_WITH_ACCESS_TOKEN = "theia.blacklistLPVfromFPOV2WithAccessToken";
        public static final String DISABLE_STATIC_SENTINEL_FOR_LPV = "theia.disableStaticSentinelForLPV";
        public static final String DISABLE_STATIC_SENTINEL_FOR_LPV_MID_BASED = "theia.disableStaticSentinelForLPVMidBased";
        public static final String BLACKLIST_SAVED_CARDS_IN_FPO_V2_WITH_ACCESS_TOKEN = "theia.blacklistSavedCardsInFPOV2WithAccessToken";
        public static final String ENABLE_ADDNPAY_ON_DYNAMIC_QR = "enable.addnpay.on.dynamic.qr";
        public static final String ALLOW_PROMO_DATA_IN_MERCHANT_STATUS_SERVICE = "theia.promoDataInMerchantStatusService";
        public static final String NOTIFY_PAYMENT_REQUEST_DATA = "theia.notifyPaymentRequestData";
        public static final String MIGRATE_BANK_OFFERS_PROMO = "theia.migrateBankOffersPromo";
        public static final String NOTIFY_PAYMENT_DWH_STACKTRACE = "theia.notifyDwhStackTrace";
        public static final String DISABLE_CHECKING_AUTOLOGIN_ENABLE_PREF = "theia.disableCheckingAutologinEnablePreference";
        public static final String PASS_USERINFO_COMMENT_TO_ACQUIRING = "theia.passUserInfoAndCommentToAcquiring";
        public static final String ADD_MONEY_FEE_ON_ADDNPAY_TXN = "theia.enableAddMoneyFeeOnAddnPayTxn";
        public static final String COFT_GLOBAL_VAULT_ENABLED = "theia.coftGlobalVaultEnabled";
        public static final String COFT_RETURN_TOKEN_CARDS = "theia.returnTokenCards";
        public static final String DISABLE_CUSTOM_VALVE_TO_ADD_ATTRIBUTES = "theia.disable.custom.valve";
        public static final String ENABLE_FETCH_USER_PREFERENCE_TASK = "theia.enable.fetchUserPreferenceTask";
        public static final String EMI_SUBVENTION_ITEM_DETAILS_WITH_CHAR_RESTRICTIONS = "theia.emi.subvention.itemDetailsWithCharRestrictions";
        public static final String POPULATE_BANK_RESULT_INFO = "theia.populate.bank.result.info";
        public static final String POPULATE_BANK_RESULT_INFO_OFFLINE = "theia.populate.bank.result.info.for.offline";
        public static final String POPULATE_BANK_RESULT_INFO_ONLINE = "theia.populate.bank.result.info.for.online";
        public static final String ENABLE_FETCH_USER_PREFERENCE_TASK_IN_ONETIME_PAYFLOW = "theia.enableFetchUserPreferenceTaskInOneTimePayFlow";
        public static final String ENABLE_FETCH_USER_PREFERENCE_TASK_IN_SUBSCRIPTION_PAYFLOW = "theia.enableFetchUserPreferenceTaskInSubscriptionPayFlow";
        private static final String DISABLE_PROMO_CHECKOUT_RETRY = "theia.disablePromoCheckoutRetry";
        private static final String PROMO_CHECKOUT_RETRY_COUNT = "promoCheckoutRetryCount";
        private static final String SAVE_PROMO_CHECKOUT_DATA = "theia.savePromoCheckoutData";
        private static final String PROMO_CHECKOUT_RESPONSE_DETAILS = "promoCheckoutResponseDetails";
        public static final String FEATURE_ENABLE_UPI_ONBOARDING_FOR_NEW_BANKACCT = "theia.enableUPIOnboardingForNewBankAcct";
        public static final String ENABLE_EXTENDINFO_PROFILING_IN_ORDER = "theia.extendinfo.profiling.order";
        public static final String ENABLE_GLOBAL_VAULT_ON_STATIC_QR = "theia.enableGlobalVaultOnStaticQR";
        public static final String ENABLE_EXTENDINFO_PROFILING_IN_PAYOPTION = "theia.extendinfo.profiling.payoption";
        public static final String ENABLE_STATIC_QR_FLOW_EXTENDINFO_PROFILING = "theia.staticqr.flow.extendinfo.profiling";
        public static final String ENABLE_PUBLISH_TO_SNS_FOR_TRANSLATION = "theia.publish.to.sns.for.translation";

        public static final String ENABLE_UPIPSP_FLOW_EXTENDINFO_PROFILING = "theia.upipsp.flow.extendinfo.profiling";
        public static final String THEIA_EXTEND_INFO_KEYS_TO_REMOVE = "theia.extendInfo.remove.keys";
        public static final String COFT_AOA_RETURN_TOKEN_CARDS = "theia.returnTokenCardsForAoa";
        public static final String SUPERCASH_MID = "theia.supercashMid";
        public static final String SUPERCASH_OFFLINE = "theia.supercashOffline";
        public static final String SUPERCASH_OFFLINE_MLV = "theia.supercashOfflineMlv";
        public static final String DISABLE_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW = "theia.disableAddMoneyPayOptionInOfflineFlow";
        public static final String FETCH_LRN_DETAILS = "theia.fetchLRNDetails";

        public static final String COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION = "theia.returnTokenCardsForAoaSubscription";
        public static final String PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER = "theia.persistOrderInfoAtAoaTimeoutCenter";
        public static final String THEIA_STACKTRACE_LIMIT = "theia.stacktraceLimit";
        public static final String DISABLE_PHASE_3_PG2 = "pg2.phase3.disabled";
        public static final String DISABLE_PG2_INTERNAL_RETRY = "theia.DISABLE_PG2_INTERNAL_RETRY";
        public static final String DISABLE_INTERNAL_RETRY = "theia.DISABLE_INTERNAL_RETRY";
        public static final String BLOCKED_ISSUERS_ON_COFT = "theia.blockedIssuersOnCoft";

        public static final String BLOCKED_ISSUERS_ON_COFT_AT_MID = "theia.blockIssuerOnMidAndAmtCoft";
        public static final String ALLOWED_ISSUERS_AT_MID = "theia.allowIssuerOnMid";
        public static final String BLOCKED_BINS_ON_COFT = "theia.blockedBinsOnCoft";
        public static final String ENABLE_GET_USER_POSTPAID_STATUS_FROM_UPS = "theia.enableGetUserPostpaidStatusFromUPS";
        public static final String SET_POSTPAID_BALANCE_TO_ZERO_FOR_INACTIVE_USER = "theia.setPostpaidBalanceToZeroForInActivePostpaidUser";

        public static final String ENABLE_GCIN_ON_COFT_PROMO = "theia.enableGcinOnCoftPromo";
        public static final String ENABLE_BIN_LENGTH_CHANGE_6_TO_9 = "theia.enableBinLengthChange6To9";
        public static final String ENABLE_STORE_IN_CACHE_ONLY = "theia.enableStoreInCacheOnly";
        public static final String DISABLE_CARD_INDEX_NUMBER = "theia.disableCardIndexNumber";
        public static final String ENABLE_GCIN_ON_COFT_WALLET = "theia.enableGcinOnCoftWallet";
        public static final String DISABLE_VERIFY_CARD_HASH = "theia.disableVerifyCardHash";
        public static final String VERIFY_CARD_HASH_ON_TIN = "theia.verifyCardHashOnTIN";
        public static final String UPDATE_CARD_NO_EMI_SUBVENTION = "theia.updateCardNoEmiSubvention";

        public static final String IS_COFT_SUPPORT_ENABLED_ON_AOA = "theia.coft.aoa.enabled";
        public static final String GLOBAL_VAULT_COFT = "theia.globalVaultCoft";
        public static final String LINK_NATIVE_PAYMENT_IN_PROCESS_ERROR = "theia.link.error.paymentInProcess";
        public static final String USE_NEW_THEIA_BASED_URL = "theia.use.new.base.url";
        public static final String ENABLE_THEIA_REQUEST_RESPONSE_FILTER = "enable.theia.request.response.filter";
        public static final String ENABLE_ADDITIONAL_LOGGER_IN_FILTER = "enable.theia.additional.log.in.filter";
        public static final String THEIA_ENABLE_VELOCITY_REVAMP_FLOW = "theia.velocity.revamp.flow";
        public static final String ENABLE_SUBSCRIPTION_ON_AOAMID = "theia.enableSubscriptionsOnAOAMid";
        public static final String THEIA_ENABLE_RESPONSE_COMPRESSION = "theia.enable.response.compression";
        public static final String THEIA_FPO_RESPONSE_COMPRESSION = "theia.enable.fpo.response.compression";
        public static final String ENABLE_ORDER_EXPIRY_LOGIC_FOR_P2PM = "theia.enableOrderExpiryLogicForP2PM";
        public static final String ENABLE_PG2_ADD_N_PAY = "theia.pg2.enableAddNPay";
        public static final String THEIA_FALLBACK_PBBL_V4_ENABLED = "theia.fallback.ppbl.v4.enabled";
        public static final String THEIA_PPBL_ACCOUNT_TYPE_STRATEGY = "theia.ppbl.account.type.strategy";
        public static final String THEIA_PPBL_ACCOUNT_TYPE_VALUE = "theia.ppbl.account.type.value";
        public static final String ENABLE_FAILURE_LOG_TO_DWH_KAFKA = "theia.enable.failure.log.to.dwh.kafka";
        public static final String SIX_DIGIT_BIN_LOGGING = "theia.sixDigitBinLogging";
        public static final String ENABLE_TPV_FOR_ALL_REQUEST_TYPES = "theia.enableTPVForAllRequestTypes";
        public static final String DISABLE_ENHANCED_ON_SEAMLESS_NB = "theia.disableEnhancedOnSeamlessNB";

        public static final String ENABLE_PUSH_MID_TO_STATSD = "theia.enable.push.mid.to.statsD";
        public static final String ENABLE_PUSH_PAYMENT_MODE_TO_STATSD = "theia.enable.push.payment.mode.to.statsD";
        public static final String ENABLE_PUSH_REQUEST_TYPE_TO_STATSD = "theia.enable.push.request.type.to.statsD";
        public static final String ENABLE_API_PERFORMANCE_LOGGER = "theia.enable.api.performance.logger";

        public static final String THEIA_CREATE_NON_QR_DEEPLINK = "theia.create.non.qr.deeplink";
        public static final String QR_DEEPLINK_BLACKLISTED_MIDS = "theia.qr.deeplink.blacklisted.mids";

        public static final String BLACKLIST_GLOBAL_VAULT = "theia.blacklistGlobalVault";
        public static final String BLACKLIST_GLOBAL_VAULT_ADDNPAY = "theia.blacklistGlobalVaultAddNPay";
        public static final String ENABLE_PG2_CARD_LIMIT = "theia.enable.pg2.card.limit";
        public static final String DEPRECATE_CACHE_CARD_API = "theia.deprecate.cache.card.api";
        public static final String ENABLE_THEIA_EMI_ICB = "theia.enable.emi.icb";
        public static final String ENABLE_THEIA_EDC_EMI_ICB = "theia.enable.edc.emi.icb";

        public static final String ENABLE_SET_PAYTM_EXPRESS_PRODUCT_CODE_MAPPING = "theia.enableSetPaytmExpressProductCodeMapping";
        public static final String ENABLE_ROUTE_CACHE_CARD_FOR_ALL_PAYMODES = "theia.routeCacheCardForAllPayModes";
        public static final String ENABLE_ROUTE_CACHE_CARD_FOR_FETCH_CARD_INDEX = "theia.routeCacheCardForFetchCardIndex";
        public static final String SET_AMOUNT_TO_BE_REFUNDED_FIELD = "theia.setAmountToBeRefundedField";
        public static final String SET_ADDITIONAL_PARAM_IN_RESPONSE = "theia.setAdditionalParamInResponse";
        public static final String ENABLE_ISELIGLIBLEFORCOFT_LOGIC = "theia.enable.isEligibleForCoft.logic";
        public static final String FETCH_EMI_DETAILS_FROM_MERCHANT_CENTER = "theia.fetchEMIDetailsFromMerchantCenter";

        public static final String IS_ORDER_ELIGIBLE_FOR_RETRY = "theia.eligibleOrdersForRetry";
        public static final String ENABLE_ADD_MONEY_SURCHARGE = "theia.enable.addMoney.surcharge";

        public static final String ENABLE_STATSD_FOR_DWH_KAFKA = "theia.enableStatsdForDWHKafka";

    }

    public static final class RiskParams {
        public static final String REFERER_URL = "refererURL";
        public static final String CALLBACK_URL = "callbackURL";
        public static final String REGISTERED_APP_URL = "registeredAppURL";
        public static final String REGISTERED_WEB_URL = "registeredWebURL";

        public static final String CIN = "cin";
        public static final String PAR = "par";
        public static final String CARD_BIN = "cardBin";
        public static final String LAST_FOUR_DIGITS = "lastFourDigits";
        public static final String MERCHANT_CATEGORY = "merchantCategory";
    }

    public static final class DataEnrichmentKey {
        public static final String CARD_NETOWK = "network";
        public static final String CARD_ISInternational = "isInternational";
        public static final String CARD_ISSAVECARD = "isSavedCard";
        public static final String EMI_LOANPATNER = "loanPartner";
        public static final String EMI_LAN = "LAN";
        public static final String EMI_TENURE = "tenure";
        public static final String EMI_PLAN_ID = "planID";
        public static final String EMI_INTREST_RATE = "InterestRate";
        public static final String UPI_PSPAPP = "PSPApp";
        public static final String EMI_AMOUNT = "emiAmount";
        // PGP-34467
        public static final String MERCHANT_NAME = "merchantName";
        public static final String CARD_TYPE = "cardType";
        public static final String MASKED_CARD_NO = "cardNo";
        public static final String CARD_ISSUER = "cardIssuer";
        public static final String AQUIRING_BANK = "bank";
        public static final String CARD_TOKEN = "cardToken"; // coft related
                                                             // token;

        public static final String EMI_TENUREID = "emiTenureId";
        public static final String EMI_PLANID = "emiPlanId";
        public static final String EMI_ONUS = "cardAcquiringMode";
        public static final String EMI_MONTHS = "emiMonths";
        public static final String EMI_INTEREST = "emiInterestRate";
        public static final String TO_USE_DIRECT_PAYMENT = "toUseDirectPayment";

        public static final String VIRTUAL_PAYMENT_ADDRESS = "vpaChannel";
        public static final String VERIFICATION_METHOD = "verificationMethod";
        public static final String GRATIFICATION_TYPE_DISCOUNT = "DISCOUNT";
        public static final String GRATIFICATION_TYPE_CASHBACK = "CASHBACK";
        public static final String UPI_BANK_NAME = "bankName";
        public static final String IS_INTERNATIONAL_CARD = "isInternational";
        public static final String EMI_DISCOUNT = "discount";
        public static final String EMI_CASHBACK = "cashback";
        public static final String PAYMODE_UPI = "UPI";
        public static final String PAYMODE_NB = "NB";
        public static final String PAYMODE_PPI = "PPI";
        public static final String PAYMODE_PAYTM_DIGITAL_CREDIT = "PAYTM_DIGITAL_CREDIT";
        public static final String AUTH_MODE_SSO_TOKEN = "ssoToken";
        public static final String AUTH_MODE_AUTO_LOGIN = "autoLogin";
        public static final String AUTH_MODE_LOGIN = "login";
        public static final String DEVICE_ID = "deviceId";
    }

    public static final class EdcLinkEmiTxn {
        public static final String BRAND_EMI_EXEMPTED_BANK_LIST = "bard.emi.exempted.bankList";
        public static final String BANK_EMI_VALIDATION_FAIL_MESSAGE = "Validation Faliure in Bank Emi APi Response";
        public static final String BRAND_EMI_VALIDATION_FAIL_MESSAGE = "Validation Faliure in Brand Emi APi Response";
        public static final String BANK_EMI_API_FAILURE_RESPONSE_MESSAGE = "Failure response received from Bank Emi Api";
        public static final String BRAND_EMI_API_FAILURE_RESPONSE_MESSAGE = "Failure response received from Brand Emi Api";
        public static final String BRAND_BANK_EMI_CHECKOUT_FAILURE_RESPONSE_MESSAGE = "Failure response received from Bank/Brand Checkout Api";
        public static final String BANK_EMI_API_SUCCESSFUL_RESPONSE = "Successfully processed Bank Emi Request";
        public static final String BRAND_EMI_API_SUCCESSFUL_RESPONSE = "Successfully processed Brand Emi Request";
        public static final String BANK_BRAND_CHECKOUT_API_SUCCESSFUL_RESPONSE = "Successfully processed Brand Emi Request";
        public static final String EXCEPTION_MESSAGE_BANK_API = "Exception occured in Bank Api Excution";
        public static final String EXCEPTION_MESSAGE_BRAND_API = "Exception occured in Brand Api Excution";
        public static final String EXCEPTION_MESSAGE_BANK_BRAND_CHECKOUT_API = "Exception occured in Bank/Brand Checkout Api Excution";
        public static final String VALIDATION_API_FAILURE_MESSAGE = "Failure response received from vlidation Api";
        public static final String EXCEPTION_MESSAGE_VALIDATION_API = "Exception occured in Validation Api Excution";
        public static final String EDC_LINK_API_DATE_FORMATE = "kkmmss";
        public static final String EDC_OFFER_CHECKOUT_DATE_FORMAT = "MMdd";
        public static final String EDC_VALIDATE_VELOCITY_FAILURE_RESPONSE_MESSAGE = "Failure response received from Validate Velocity API";
        public static final String EDC_VALIDATE_VELOCITY_API_SUCCESSFUL_RESPONSE = "Successfully processed Validate Velocity API";
        public static final String EMI_RESPONSE_MISMATCH_MESSAGE = "Offer changed or usage limit expired on this card";
    }

    public static final class FailureLogs {
        public static final String PROMO_CHECKOUT_LIMIT_BREACHED = "promo checkout limit breached";
        public static final String PAYMENT_OFFER_CHECKOUT_FAILURE = "Payment Offer checkout failure";
        public static final String PAYMENT_CARD_BIN_DOES_NOT_MATCH = "Payment card bin does not match with bin used in validate subvention request";
        public static final String SUBVENTION_OFFER_CHECKOUT_FAILURE = "Subvention Offer checkout failure";
        public static final String SUBVENTION_OFFER_ORDER_STAMPING_FAILURE = "Subvention Offer order Stamping failure";
        public static final String ADDMONEY_NOT_ALLOWED_OR_FAILED = "AddMoney Not Allowed or Failed";
        public static final String IS_ORDER_ID_NEED_TO_BE_GENERATED = "IS_ORDER_ID_NEED_TO_BE_GENERATED";
        public static final String FAILURE_LOG_BEAN = "failureLogBean";
        public static final String DELIMITER = "_";
        public static final String ACCOUNT_NUMBER_FOR_DEFAULT_MF_IS_EMPTY = "Account Number received for DEFAULT_MF is empty";
        public static final String ACCOUNT_NUMBER_MISMATCH_FOR_DEFAULT_MF = "Account Number Mismatch for DEFAULT_MF";
        public static final String NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_GV_CONSENT = "No data found in cache for resuming gv consent flow payment request";
        public static final String NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_RISK_VERIFIED_REQUEST = "No data found in cache for resuming risk verified payment request";
        public static final String WORKFLOW_REQUEST_BEAN_IS_NULL = "WorkFlowRequestBean is null";
        public static final String WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS = "WorkFlowResponseBean is null, Reason : ";
        public static final String PAYMENT_MODE_IS_INVALID_FOR_EMI_SUBVENTION = "Payment Mode is invalid for EMI Subvention transaction";
        public static final String EMI_SUBVENTION_INFO_IS_INVALID = "EMI Subvention info from UI/App is not valid";
        public static final String AGG_MID_IN_TOKEN_AND_REQUEST_ARE_DIFFERENT = "aggMid in token and request are different";
        public static final String FAILING_ADDNPAY_REQ_WITHOUT_VALID_KYC_USER = "Failing Enhanced ADDANDPAY request without valid KYC user";
        public static final String EMI_SUBVENTION_OR_PROMO_IS_NOT_APPLIED = "EMI Subvention or Promo is not applied for pwp merchant";
        public static final String RECEIVED_CARD_INFO_IN_BOTH = "Received card Info in both encCardInfo and cardInfo";
        public static final String INVALID_CARD_DETAILS_LENGTH_OR_CVV_MISSING = "Invalid cardDetails length or CVV is missing";
        public static final String INVALID_ECOM_TOKEN_INFO = "Invalid ecomTokenInfo";
        public static final String INVALID_CARD_DETAILS_LENGTH = "Invalid cardDetails length";
        public static final String INVALID_AUTH_MODE = "Invalid authMode";
        public static final String INVALID_PAYMENT_FLOW = "Invalid paymentFlow";
        public static final String INVALID_PAYMODE_BAJAJFN_EMI = "Invalid paymode BAJAJFN EMI";
        public static final String INVALID_MPIN_OR_ACCESS_TOKEN_FOR_PPBL = "Invalid MPIN / Access token for PPBL";
        public static final String ACCOUNT_INFO_FOR_PAYTM_DIGITAL_CREDIT_IS_UNAVAILABLE = "AccountInfo for PAYTM_DIGITAL_CREDIT is not available";
        public static final String INVALID_MPIN_OR_ACCESS_TOKEN_FOR_POST_PAID = "Invalid MPIN / Access token for POST PAID";
        public static final String INVALID_TEMPLATE_ID_FOR_MGV = "Invalid TemplateId for MGV";
        public static final String USER_WALLET_IS_DEACTIVATED = "User Wallet is deactivated. Cannot proceed for Payment";
        public static final String DUPLICATE_PAYMENT_REQUEST = "Duplicate Payment Request";
        public static final String NO_DATA_IN_CACHE_AFTER_NATIVE_KYC_COMPLETION = "no data in cache after Native kyc successful completion";
        public static final String TIN_AND_TOKEN_OR_TAVV_PAYMENT_METHOD_NOT_SUPPORTED_FOR_SUBSCRIPTION = "tin and token/tavv payment method are not supported for subscription request type";
        public static final String FOR_SUBSCRIPTION_COFT_CONSENT_IS_MANDATORY_FOR_NON_TOKENIZED_CARD_TXN = "For Subscription, Coft Consent is Mandatory for non-tokenized card transactions";
        public static final String ALLOWED_ISSUERS_ON_MERCHANT_ERROR_MESSAGE = "Only {0} are allowed for payment.";

        // postTransactionSplit failure response messages
        public static final String MERCHANT_RELATIONSHIP_ILLEGAL_MESSAGE = "Parent merchant and child merchant relationship illegal";
        public static final String SPLIT_SIZE_ILLEGAL_MESSAGE = "SplitSettlementInfo list size cannot be greater than 50";
        public static final String SPLIT_METHOD_NOT_EQUALS_MESSAGE = "Split methods are not equal";
        public static final String REPEAT_REQUEST_MESSAGE = "Duplicate request exception";
        public static final String MUTEX_OPERATION_IN_PROCESSING_MESSAGE = "Split already in progress";
        public static final String ORDER_STATUS_INVALID_MESSAGE = "Order status is not confirmed yet.";
    }

    public static final String AOA_2_PG_MIDS = "theia.aoa2.mapped.pg.mids";
    public static final String THEIA_AOAMID_TO_PGMID_ENABLED = "theia.aoamid.to.pgmid.enable";
    public static final String THEIA_AOA_DQR_EMI_PASS_THROUGH_ENABLED = "theia.aoa.dqr.emi.pass.through.enabled";

}
