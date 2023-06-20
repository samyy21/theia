/**
 * 
 */
package com.paytm.pgplus.theia.constants;

/**
 * @author namanjain
 *
 */
public class ResponseCodeConstant {

    public static final String PAYMENT_SUCCESS = "01";
    public static final String REFUND_SUCCESS = "10";
    public static final String OTP_SUCCESS = "09";
    public static final String WALLET_TIMEOUT = "1101";
    // Failure for fraud control
    public static final String SUSPICIUSS = "100";
    public static final String CANCEL_AT_LOGIN = "142";
    public static final String CANCEL_BEFORE_LOGIN = "143";
    public static final String CANCEL_AFTER_LIMIT_FAIL = "144";
    // Velocity code
    /*
     * String SUSPICIUSs ="101"; String SUSPICIUSs ="102"; String SUSPICIUSs
     * ="103"; String SUSPICIUSs ="104"; String SUSPICIUSs ="105"; String
     * SUSPICIUSs ="106"; String SUSPICIUSs ="107" String SUSPICIUSs ="108"
     * String SUSPICIUSs ="109"
     */

    // Velocity breach on Card
    public static final String CC_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN = "114";
    public static final String CC_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN = "115";
    public static final String CC_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN = "116";
    public static final String CC_USER_EXCEEDS_THE_DAILY_TXN_LIMIT = "111";
    public static final String CC_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT = "112";
    public static final String CC_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT = "113";
    public static final String CC_USER_EXCEEDS_THE_PER_TXN_LIMIT = "110";

    public static final String CC_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0114";
    public static final String CC_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0115";
    public static final String CC_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0116";
    public static final String CC_USER_EXCEEDS_THE_DAILY_TXN_LIMIT_PENDING = "0111";
    public static final String CC_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT_PENDING = "0112";
    public static final String CC_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT_PENDING = "0113";
    public static final String CC_USER_EXCEEDS_THE_PER_TXN_LIMIT_PENDING = "0110";

    // Velocity breach on Overall
    public static final String OVERALL_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN = "120";
    public static final String OVERALL_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN = "121";
    public static final String OVERALL_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN = "122";
    public static final String OVERALL_USER_EXCEEDS_THE_DAILY_TXN_LIMIT = "107";
    public static final String OVERALL_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT = "108";
    public static final String OVERALL_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT = "109";
    public static final String OVERALL_USER_EXCEEDS_THE_PER_TXN_LIMIT = "106";

    public static final String OVERALL_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0120";
    public static final String OVERALL_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0121";
    public static final String OVERALL_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0122";
    public static final String OVERALL_USER_EXCEEDS_THE_DAILY_TXN_LIMIT_PENDING = "0107";
    public static final String OVERALL_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT_PENDING = "0108";
    public static final String OVERALL_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT_PENDING = "0109";
    public static final String OVERALL_USER_EXCEEDS_THE_PER_TXN_LIMIT_PENDING = "0106";

    // Velocity breach on NB
    public static final String NB_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN = "127";
    public static final String NB_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN = "128";
    public static final String NB_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN = "129";
    public static final String NB_USER_EXCEEDS_THE_DAILY_TXN_LIMIT = "124";
    public static final String NB_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT = "125";
    public static final String NB_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT = "126";
    public static final String NB_USER_EXCEEDS_THE_PER_TXN_LIMIT = "123";

    public static final String NB_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0127";
    public static final String NB_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0128";
    public static final String NB_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0129";
    public static final String NB_USER_EXCEEDS_THE_DAILY_TXN_LIMIT_PENDING = "0124";
    public static final String NB_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT_PENDING = "0125";
    public static final String NB_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT_PENDING = "0126";
    public static final String NB_USER_EXCEEDS_THE_PER_TXN_LIMIT_PENDING = "0123";

    // Velocity breach on SSO
    public static final String SSO_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN = "167";
    public static final String SSO_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN = "168";
    public static final String SSO_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN = "169";
    public static final String SSO_USER_EXCEEDS_THE_DAILY_TXN_LIMIT = "170";
    public static final String SSO_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT = "171";
    public static final String SSO_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT = "172";
    public static final String SSO_USER_EXCEEDS_THE_PER_TXN_LIMIT = "173";

    public static final String SSO_DAY_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0167";
    public static final String SSO_WEEK_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0168";
    public static final String SSO_MONTH_EXCEED_THE_MAX_NUMBER_OF_TXN_PENDING = "0169";
    public static final String SSO_USER_EXCEEDS_THE_DAILY_TXN_LIMIT_PENDING = "0170";
    public static final String SSO_USER_EXCEEDS_THE_WEEKLY_TXN_LIMIT_PENDING = "0171";
    public static final String SSO_USER_EXCEEDS_THE_MONTHLY_TXN_LIMIT_PENDING = "0172";
    public static final String SSO_USER_EXCEEDS_THE_PER_TXN_LIMIT_PENDING = "0173";

    public static final String LIMIT_OF_MAX_NUMBER_OF_DIFF_CARDS = "117";
    public static final String TXN_AMT_EXCEED = "118";

    public static final String USER_BLOCKED = "130";
    public static final String CC_BLOCKED = "131";
    public static final String IP_ADRESS_BLOCKED = "132";
    public static final String MSIDN_BLOCKED = "133";
    public static final String CC_GRAY = "135";
    public static final String CANCEL_REQUEST = "141";
    public static final String CANCEL_REQUEST_AFTER_LIMITS_FAIL = "145";

    public static final String DUP_TXNID_TIME = "150";
    public static final String DUP_TXNID = "151";
    public static final String MER_NOT_ASS = "152";
    public static final String CHA_NOT_CONFIG = "153";
    // Wallet velocity limits
    public static final String PER_MON_TXN_AMT = "158";
    public static final String PER_DAY_TXN_COUNT = "159";
    public static final String PER_WEEK_TXN_COUNT = "160";
    public static final String PER_MON_TXN_COUNT = "161";
    public static final String MAX_WRONG_ATTAMPTS = "162";
    public static final String VELOCITY_FAILED = "163";
    // Failure reported by Bank
    public static final String OTP_FAILURE = "200";
    public static final String BIN_STOP_LISTED = "201";
    public static final String NOT_SUFFICIENT_FUND = "202";
    public static final String CARD_NUMBER_INVALID = "203";
    public static final String UNAUTHORIZED_USAGE = "204";
    public static final String REJECTED_BY_BANK = "205";
    public static final String DUPLICATE_TXNID = "206";
    public static final String EXPIRED_CARD = "207";
    public static final String REJECTED_BY_ACQUIRER_BANK = "208";
    public static final String INVALID_CARD_DETAILS = "209";
    public static final String LOST_CARD = "210";
    public static final String BANK_ERROR = "211";
    public static final String TXN_ON_HOLD_AT_BANK = "212";
    public static final String ENCRYPTION_ERR_AT_BANK = "213";
    public static final String SUITABLE_ACQUIRER_NOT_FOUND = "214";
    public static final String UNKNOWN_ICICI_ERROR_MSG = "215";
    public static final String BLANK_ICICI_ERROR_MSG = "216";
    public static final String GATEWAY_COMM_ERROR = "217";
    public static final String OTP_VALIDATED = "218";
    public static final String OTP_VALIDATION_FAILED = "219";
    public static final String BANK_COMM_ERROR = "220";
    public static final String BANK_CHECKSUM_NOTMATCHED = "221";
    public static final String AMOUNT_NOTMATCHED = "222";
    public static final String DUPLICATE_ID = "223";
    public static final String DELAY_FROM_BANK = "224";
    public static final String BANK_TIMEOUT = "225";
    public static final String PARSING_ERROR = "226";
    public static final String PAYMENT_FAILURE = "227";
    public static final String REJECTED_BY_ACQUIRER = "228";
    public static final String THREE_D_FAILED = "229";
    public static final String THREE_D_SUCCESS = "230";
    public static final String ONLY_AMEX_CARD = "231";
    // ValidationErrors
    public static final String INVALID_JSON_REQUEST = "301";
    public static final String INVALID_REQUEST_TYPE = "302";
    public static final String BLANK_MERCHANT_ID = "303";
    public static final String BLANK_APP_IP = "304";
    public static final String MER_NOT_REG = "305";
    public static final String MER_EXP = "306";
    public static final String KEY_EXP = "307";

    public static final String INVALID_AMT = "308";
    public static final String INVALID_ORDERID = "309";
    public static final String INVALID_CURRENCY = "310";
    public static final String INVALID_DEVICEID = "311";
    public static final String INVALID_CARDNO = "312";
    public static final String INVALID_CARDID = "313";
    public static final String INVALID_MONTH = "314";
    public static final String INVALID_YEAR = "315";
    public static final String INVALID_CVV = "316";
    public static final String INVALID_PAYMENT_MODE = "317";
    public static final String INVALID_CUSTID = "318";
    public static final String INVALID_INDS_TYPE = "319";
    public static final String INVALID_CARD_STORE_FLAG = "320";
    public static final String INVALID_CARD_DEL_FLAG = "321";
    public static final String CARD_DETAILS_NOT_FOUND = "322";
    public static final String TXN_USR_BLOCKED = "323";
    public static final String DUPILCATE_CUST_ID = "324";
    public static final String DUPILCATE_ORDER_ID = "325";
    public static final String MERCHANT_NOT_ASSOSCIATED = "326";
    public static final String CHANNEL_NOT_ASSOSCIATED = "327";
    public static final String INVALID_PAYTM_ID = "328";
    public static final String INVALID_OTP = "329";
    public static final String INVALID_CHECKSUM = "330";
    public static final String RECORD_NOT_FOUND = "331";
    public static final String ORDERID_LENGTH = "332";
    public static final String CUSTID_LENGTH = "333";
    public static final String INVALID_MMID = "339";
    public static final String INVALID_MOBILE_NUMBER = "340";
    public static final String INVALID_TOKEN = "341";

    public static final String UNABLE_TO_INSERT_ORDER = "343";

    // StatusQry
    public static final String TXN_NOT_CONFIRMED = "400";
    public static final String INVALID_TXN = "401";
    public static final String ABONDONED_TXN = "402";

    // System Error.
    public static final String SYSTEM_ERROR = "501";
    public static final String JSON_ENCODING_ERROR = "502";
    public static final String BANK_FEE_NOT_CONFIGURED = "503";
    // Refund
    public static final String REFUND_PENDING = "601";
    public static final String REFUND_FAILURE = "602";

    public static final String ALREADY_REFUND = "603";
    public static final String NOT_SETTLED = "605";
    public static final String CHECKSUM_FAILED = "606";
    public static final String CANCLLED_TXN = "607";
    public static final String INVAIL_AUTH_CODE = "608";
    public static final String REJECTED_TXN = "609";
    public static final String FULLY_REFUNDED = "610";

    public static final String PROMO_APPLIED = "700";
    public static final String PROMO_SUCCESS = "01";
    public static final String PROMO_INVALID = "701";
    public static final String PROMO_INACTIVE = "702";
    public static final String PROMO_EXPIRED = "703";
    public static final String PROMO_INVALID_CARD_NUMBER = "704";
    public static final String PROMO_INVALID_NETBANKING = "705";
    public static final String PROMO_WALLET_TXN = "706";
    public static final String PROMO_INVALID_AMOUNT = "707";
    public static final String PROMO_MAX_AMOUNT_EXCEEDED = "708";
    public static final String PROMO_MAX_COUNT_EXCEEDED = "709";
    public static final String PROMO_OLD_CARD_TRANSACTED = "710";
    public static final String PROMO_OLD_CUST_TRANSACTED = "711";
    public static final String PROMO_INVALID_PAYMENT_MODE = "714";
    public static final String PROMO_SAVED_CARD_NOT_SELECTED = "715";
    public static final String INVALID_MERCHANT_ID = "801";
    public static final String MERCHANT_NOT_ASSOCIATED_WITH_REQUEST_TYPE = "802";
    public static final String INVALID_NO_OF_MANDATORY_PARAMETERS = "803";
    public static final String INVALID_DATA_PARAM = "804";
    public static final String REQ_TYPE_DOES_NOT_EXIST = "805";
    public static final String INVALID_MANDATORY_PARAMETER_VALUE = "806";

    // Subscription code java.com.paytm.pgplus.constants
    public static final String SUBS_TXN_ACCEPTED = "900";
    public static final String SUBSCRIPTION_NOT_AVAILABLE = "901";
    public static final String SUBS_INVALID_SERVICE_ID = "902";
    public static final String SUBS_SAVED_CARD_NOT_FOUND = "903";
    public static final String SUBS_INVALID_FREQUENCY = "904";
    public static final String SUBS_INVALID_AMOUNT = "905";
    public static final String SUBS_INVALID_GRACE_DAYS = "906";
    public static final String SUBS_INVALID_RENEW_DATE = "907";
    public static final String SUBSCRIPTION_EXPIRED = "908";
    public static final String SUBSCRIPTION_INACTIVE = "909";
    public static final String SUBSCRIPTION_NOT_CONFIGURED = "910";
    public static final String SUBS_INVALID_FREQUENCY_UNIT = "911";
    public static final String SUBS_INVALID_EXPIRY_DATE = "912";
    public static final String SUBS_INVALID_AMOUNT_TYPE = "913";
    public static final String SUBS_INVALID_START_DATE = "914";
    public static final String SUBS_INVALID_RETRY_FLAG = "915";
    public static final String SUBS_INVALID_CAPPED_AMOUNT = "916";
    public static final String SUBS_DUPLICATE_RENEW_REQUEST = "917";
    public static final String SUBS_WALLET_LIMIT_FAILED = "918";
    public static final String SUBS_TXN_PENDING = "920";
    public static final String INVALID_PAYMENT_TYPE_ID = "930";
    public static final String INVALID_PAYMENT_DETAILS = "931";
    public static final String INVALID_AUTH_MODE_ID = "932";
    public static final String INVALID_PARAMETER = "933";
    public static final String INVALID_EMAIL_INVOICING_URL = "1001";
    public static final String PAYMENT_LINK_EXPIRED = "1002";
    public static final String MAX_RETRY_ACHIEVED = "1003";
    public static final String ON_GOING_TRANSACTION = "1004";
    public static final String WITHDRAW_FAILURE = "19";
    public static final String INVALID_PARAMS = "10010";
    public static final String INVALID_SAVED_CARD_ID = "950";
    public static final String INVALID_SSOTOKEN = "951";
    public static final String CARD_NOT_CONFIGURED_FOR_MERCHANT = "952";
    public static final String NOT_CC_CARD = "953";
    public static final String SUBS_INSUFICIENT_BALANCE = "924";
    public static final String INVALID_AUTH_MODE = "10020";
    public static final String BANK_DOWN = "10070";
    public static final String PAGE_OPENED = "810";
    public static final String PAGE_OPENED_WITH_LIMITS_FAIL = "811";
    public static final String COULD_NOT_GET_USER_DETAILS = "110010";

    public static final String PER_TXN_AMT = "155";
    public static final String PER_DAY_TXN_AMT = "156";
    public static final String PER_WEEK_TXN_AMT = "157";

    public static final String CANCEL_REQ_WEB_PRE_LOGIN = "14111";
    public static final String CANCEL_REQ_WEB_POST_LOGIN_SUFF_BAL = "14112";
    public static final String CANCEL_REQ_WEB_POST_LOGIN_INSUFF_BAL = "14113";
    public static final String CANCEL_REQ_WEB_ADD_MONEY = "14114";

    public static final String QR_EXPIRY_RESPONSE_CODE = "QR_1018";

}
