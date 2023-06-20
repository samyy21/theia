package com.paytm.pgplus.biz.core.risk;

public class RiskConstants {

    public static final String DO_VIEW_CACHE_KEY_PREFIX = "RISK_VERIFIER_";
    public static final String RISK_REJECT_MESSAGE = "Please try with lower amount or different payment source. Transaction limits will revise as you continue using Paytm";
    public static final String RISK_VERIFIER_UI_KEY = "RISK_VERIFY_FLOW";
    public static final String IS_RISK_VERIFICATION_REQUIRED = "isRiskVerificationRequired";
    public static final String TRANS_ID = "transId";
    public static final String TRUE = "true";
    public static final String RISK_VERIFICATION_KEY_PREFIX = "RISK_VERIFY_";
    public static final String TOKEN = "token";
    public static final String MID = "MID";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String THEIA_BASE_URL = "theia.base.url";
    public static final String SUBMIT_URL = "/theia/payment/request/submit?MID=";
    public static final String PTC_V1_URL = "/theia/api/v1/processTransaction";
    public static final String IS_RISK_VERIFIED_PREFIX = "IS_RISK_VERIFIED_";
    public static final String SHOW_VERIFICATION_PAGE_URL = "/theia/api/v1/risk/showVerificationPage";
    public static final String CANCEL_TXN_URL = "/theia/cancelTransaction";
    public static final String PUSH_APP_DATA = "PUSH_APP_DATA";
    public static final String ACCOUNT_ENCRYPT_PUB_KEY = "account_encrypt_pubkey";
    public static final String ACCOUNT_ENCRYPT_SALT = "account_encrypt_salt";
    public static final String SECURITY_ID = "securityId";
    public static final String RETRY_ERROR_MSG = "Something went wrong. Please try again!";
    public static final String ERROR_MSG = "Something went wrong!";
    public static final String VERIFICATION_FAILED = "Verification failed!";
    public static final String DO_VIEW_RETRY_COUNT = "doView_retry_count_";
    public static final String DO_VERIFY_RETRY_COUNT = "doVerify_retry_count_";
    public static final String RISK_VERIFIER_ID_KEY_PREFIX = "RISK_VERIFIER_ID_";
    public static final String OPEN_IFRAME = "open_iframe";
    public static final String IFRAME_URL = "iframe_url";
    public static final String OAUTH_PASSWORD_VERIFY_URL = "/oauth2/challenge/view";
    public static final String RISK_STATE_PARAM = "state";
    public static final String PASSWORD_DOES_NOT_EXIST_CODE = "BE1426014";
    public static final String PASSWORD_LIMIT_BREACHED_CODE = "BE1426012";
    public static final String USER_BLOCKED_CODE = "BE1424001";
    public static final String PASSWORD_DOES_NOT_EXIST_MESSAGE = "It seems your password does not exist in our system. Please set your new password, or raise a query to paytm.com/care. ";
    public static final String PASSWORD_LIMIT_BREACHED_MESSAGE = "Transaction declined because you have reached the limit on password retries. Please raise a query to paytm.com/care.";
    public static final String USER_BLOCKED_MESSAGE = "We have found suspicious activity from this number. Therefore, we have blocked your account. Please raise a request at paytm.com/care to unblock your account.";
    public static final String RISK_RESPONSE_CODE_PARAM = "responseCode";

    public static class RiskVerifyMethod {
        public static final String PASSWORD = "PASSWORD";

    }
}
