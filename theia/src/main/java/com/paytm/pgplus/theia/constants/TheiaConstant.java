/**
 *
 */
package com.paytm.pgplus.theia.constants;

import com.paytm.pgplus.pgproxycommon.constants.CommonConstants.CommonCacheNames;

/**
 * @createdOn 17-Mar-2016
 * @author kesari
 */
public final class TheiaConstant {

    public static class RequestHeaders {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String REFERER = "Referer";
        public static final String X_PGP_UNIQUE_ID = "X-PGP-Unique-ID";
        public static final String TRACK_TLS = "TRACK-TLS1-0";
        public static final String NETWORK_TYPE = "NETWORK_TYPE";
        public static final String ISP = "ISP";
        public static final String ZIPCODE = "ZIPCODE";
        public static final String CITY = "CITY";
        public static final String STATE = "STATE";
        public static final String COUNTRY = "COUNTRY";
        public static final String ACCEPT = "Accept";
        public static final String PGP_ID = "PGP_ID";
        public static final String Version_V1 = "v1";
        public static final String Version_V2 = "v2";
        public static final String Version_V3 = "v3";
        public static final String SUPERGW_VERSION = "v4";
        public static final String Version_V5 = "v5";

    }

    public static class RedisKeysConstant {
        public static final String DIRECT_BANK_CARD_PAYMENT = "DIRECT_BANK_CARD_PAYMENT_";
        public static final String DIRECT_BANK_KEY = "directBankRequestBeanKey";
        public static final String REDIS_KEY_PREFIX = "redis.key.prefix";
        public static final String CARD_EXPIRY_DETAILS = "cardExpiryDetails";
        public static final String MASKED_CARD_NO = "maskedCardNo";
        public static final String MID_ORDER_TXN_TOKEN_WORKFLOW = "Mid_OrderId_TxnToken_WorkFlow";
        public static final String FETCH_BIN_DETAIL = "FETCH_BIN_DETAIL";

        public static class NativeSession {
            public static final String NATIVE_TXN_INITIATE_REQUEST = "NativeTxnInitiateRequest";
            public static final String CASHIER_INFO = "cashierInfo";
            public static final String ENTITY_PAYMENT_OPTION = "entityPaymentOption";
            public static final String USER_DETAILS = "userDetails";
            public static final String EXTEND_INFO = "extendInfo";
            public static final String INITIATE_TXN_RESPONSE = "initiateTxnResponse";
            public static final String FLOW_TYPE = "flowType";
            public static final String QR_LINK_ID = "qrLinkId";
            public static final String QR_INVOICE_ID = "qrInvoiceId";
        }
    }

    public static class SessionAttributes {
        public static final String OAUTH_RES_SERVER_URI = "oauth.response.server.url";
        public static final String OAUTH_RES_SERVER_URI_ENHANCED_CASHIER = "oauth.response.server.url.enhanced.cashier";
        public static final String AUTH_INFO = "oauth_info";
        public static final String AUTH_INFO_HOST = "oauth_info_host";
        public static final String AUTH_INFO_CLIENT_ID = "oauth_info_client_id";
        public static final String AUTH_INFO_RETURN_URL = "oauth_info_return_url";
        public static final String OAUTH_RES_SERVER_URI_OLD_PG = "oauth.response.server.url.old.pg";
    }

    public static class MerchantPreference {

        public static class PreferenceKeys {
            public static final String WITHOUT_CONVENIENCE_FEES = "WithoutConvenienceFee";
            public static final String BIN_IN_RESPONSE = "BIN_IN_RESPONSE";
            public static final String CHECKSUM_ENABLED = "CHECKSUM_ENABLED";
            public static final String ADD_MONEY_ENABLED = "ADD_MONEY_ENABLED";
            public static final String AUTO_CREATE_USER = "AUTO_CREATE_USER";
            public static final String ENC_PARAMS_ENABLED = "ENCPARAMS_ENABLED";
            public static final String HYBRID_ALLOWED = "HYBRID_ALLOWED";
            public static final String LINKED_PLATFORM = "platformType";
            public static final String PWP_CATEGORY = "pwpCategory";
            public static final String SLAB_BASED_MDR = "SlabBasedMerchant";
            public static final String ORDER_EXPIRY = "ORDER_EXPIRY";
            public static final String PAYMODE_PRIORITY_LIST = "PAYMODE_PRIORITY_LIST";

            public static final String QR_PAY_ENABLED = "DYNAMIC_QR_CASHIER";
            public static final String PRN_ENABLED = "prnEnabled";
            public static final String ENHANCED_CASHIER_PAGE_ENABLED = "ENHANCED_CASHIER_PAGE_ENABLED";

            public static final String STORE_CARD_DETAILS = "STORE CARD DETAILS";
            public static final String DYNAMIC_QR_2FA = "DYNAMIC_QR_2FA";
            public static final String IS_AOA_MERCHANT = "isAOAMerchant";

            public static final String LOGIN_DISABLED = "LOGIN_DISABLED";
            public static final String LOCALE_PREF = "LOCALE_ENABLE";
            public static final String CHECKOUT_JS_ON_ENHANCED_FLOW = "ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW";

            public static final String DEFAULT_FF_WEBSITE_ENABLED = "DEFAULT_FF_WEBSITE_ENABLED";
            public static final String IS_MOCK_MERCHANT = "IS_MOCK_MERCHANT";

            public static final String LOGO_AVAILABLE_ON_NEW_LOCATION = "LOGO_URL";
            public static final String IS_API_DISABLED = "API_DISABLED";
            public static final String PG_AUTOLOGIN_ENABLED = "PG_AUTOLOGIN_ENABLED";
            public static final String PG_AUTOLOGIN_DISABLED = "PG_AUTOLOGIN_DISABLED";
            public static final String PG_MOBILE_NON_EDITABLE = "PG_MOBILE_NON_EDITABLE";
            public static final String APP_INVOKE_ALLOWED = "appInvokeAllowed";

            public static final String AES256_ENC_PARAMS_ENABLED = "AES256_ENC_PARAMS_ENABLED";

            public static final String ENHANCE_QR_DISABLED = "ENHANCE_QR_DISABLED";

            public static final String NATIVE_JSON_REQUEST = "nativeJsonRequest";
            public static final String NATIVE_OTP_SUPPORTED = "nativeOtpSupported";
            public static final String NATIVE_JSON_REQUEST_APP_VERSION = "native.json.request.blocked.app.version";
            public static final String NATIVE_JSON_REQUEST_SDK_VERSION = "native.json.request.blocked.sdk.version";
            public static final String NATIVE_OTP_SUPPORTED_PROPERTY = "native.otp.supported";
            public static final String ONE_CLICK_MAX_AMOUNT = "native.oneClick.maxAmount";

            public static final String WEB_ENHANCED_CASHIER_PAGE_ENABLED = "WEB_ENHANCED_CASHIER_PAGE_ENABLED";
            public static final String PCF_FEE_INFO = "PCF_FEE_INFO";
            public static final String THEMATIC_FEATURE_USED = "THEMATIC_FEATURE_USED";
            public static final String SUBSCRIPTION_WALLET_LIMIT_ENABLED = "SUBSCRIPTION_WALLET_LIMIT_ENABLED";
            public static final String DISABLED_LOGIN_STRIP = "DISABLED_LOGIN_STRIP";
            public static final String ALLOW_LOGIN_ON_DESKTOP = "ALLOW_LOGIN_ON_DESKTOP";

            public static final String SAVED_CARD_ID_SUPPORTED = "SAVED_CARD_ID_SUPPORTED";
            public static final String RETURN_USER_VPA_IN_RESPONSE = "RETURN_USER_VPA_IN_RESPONSE";
            public static final String DYNAMIC_CHARGE_TARGET = "DYNAMIC_CHARGE_TARGET";
            public static final String RENT_PAYMENT_MERCHANT = "RENT_PAYMENT_MERCHANT";

            public static final String MINIMAL_PROMO_MERCHANT = "MINIMAL_PROMO_MERCHANT";
            public static final String MINIMAL_SUBVENTION_MERCHANT = "MINIMAL_SUBVENTION_MERCHANT";

            public static final String SHOW_PAYTM_PAYMODE_OFFERS = "SHOW_PAYTM_PAYMODE_OFFERS";

            public static final String PAY_WITH_PAYTM = "PAY_WITH_PAYTM";
            public static final String RETURN_PREPAID_IN_RESPONSE = "PREPAID_CARD_PEON";
            public static final String OFFLINE_SNP_DESC_FLAG = "OFFLINE_SNP_DESC_FLAG";
            public static final String OFFLINE_SNP_DESC_TXT = "OFFLINE_SNP_DESC_TXT";

            public static final String SEND_CARD_SCHEME_ENC_PARAMS_ENABLED = "SEND_CARD_SCHEME_ENC_PARAMS_ENABLED";
            public static final String SEND_PCF_DETAILS_ENC_PARAMS_ENABLED = "SEND_PCF_DETAILS_ENC_PARAMS_ENABLED";
            public static final String SEND_COUNTRY_CODE_PARAM_ENABLED = "SEND_COUNTRY_CODE_PARAM_ENABLED";
            public static final String AUTO_APP_INVOKE_ALLOWED = "AUTO_APP_INVOKE_ALLOWED";
            public static final String CHECK_UPI_ACCOUNT_EXISTS = "CHECK_UPI_ACCOUNT_EXISTS";
            public static final String ADDANDPAY_WITH_UPI_COLLECT = "ADDANDPAY_WITH_UPI_COLLECT";
            public static final String UPI_COLLECT_BOX_ON_ADDANDPAY = "UPI_COLLECT_BOX_ON_ADDANDPAY";
            public static final String COST_BASED_PREFERENCE_ENABLED = "COST_BASED_PREFERENCE_ENABLED";
            public static final String UPI_APPS_PAY_MODE_DISABLED = "UPI_APPS_PAY_MODE_DISABLED";

            public static final String CO_BRANDED_CARD_IDENTIFIER = "CO_BRANDED_CARD_IDENTIFIER";
            public static final String PP_2FA = "PP_2FA";
            public static final String ENABLE_SUBSCRIPTION_QR_FLOW = "ENABLE_SUBSCRIPTION_QR_FLOW";
            public static final String BLACKLIST_SUPERCASH_INLINE_OFFERS = "BLACKLIST_SUPERCASH_INLINE_OFFERS";

            public static final String BLOCK_CHECKOUT_JS = "BLOCK_CHECKOUT_JS";

            public static final String ALLOW_REGISTERED_USER_ONLY = "ALLOW_REGISTERED_USER_ONLY";

            public static final String PENNY_DROP_AUTO_REFUND_FLOW = "PENNY_DROP_AUTO_REFUND_FLOW";
            public static final String CUSTOMER_FEEDBACK_ENABLED = "CUSTOMER_FEEDBACK_ENABLED";

            public static final String BLOCK_BULK_APPLY_PROMO = "BLOCK_BULK_APPLY_PROMO";
            public static final String FLEXI_SUBSCRIPTION_ENABLED = "FLEXI_SUBSCRIPTION_ENABLED";
            public static final String DCC_ENABLED = "DCC_ENABLED";
            public static final String BRAND_EMI = "SUBVENTED_EMI_RATE";
            public static final String LOCATION_PERMISSION = "LOCATION_PERMISSION";
            public static final String BANK_TRANSFER_CHECKOUT_FLOW = "BANK_TRANSFER_CHECKOUT_FLOW";
            public static final String RETURN_TXN_PAID_TIME_IN_RESPONSE = "RETURN_TXN_PAID_TIME_IN_RESPONSE";
            public static final String OFF_US_ORDER_NOT_FOUND_FAIL = "OFF_US_ORDER_NOT_FOUND_FAIL";
            public static final String ENCRYPTED_CARD_PAYMENT = "ENCRYPTED_CARD_PAYMENT";

            public static final String BLOCK_LINK_FPO_DISABLE_PAYMODE = "BLOCK_LINK_FPO_DISABLE_PAYMODE";
            public static final String WalletOnlyMerchant = "WalletOnlyMerchant";
            public static final String SUBWALLET_SEGREGATION_ENABLED = "SUBWALLET_SEGREGATION_ENABLED";
            public static final String ENABLE_IDEMPOTENCY_ON_UI = "ENABLE_IDEMPOTENCY_ON_UI";
            public static final String GROUPED_PAYMODES_ENABLED = "GROUPED_PAYMODES_ENABLED";
            public static final String UN_GROUPED_PAYMODES_DISABLED = "UN_GROUPED_PAYMODES_DISABLED";
            public static final String DISABLE_UPI_COLLECT_FOR_NON_LOGGEDIN = "DISABLE_UPI_COLLECT_FOR_NON_LOGGEDIN";
            public static final String CONVERT_TO_ADDANDPAY_TXN = "CONVERT_TO_ADDANDPAY_TXN";
            public static final String ENABLE_UPI_COLLECT_FROM_ADDNPAY = "ENABLE_UPI_COLLECT_FROM_ADDNPAY";
            public static final String MAX_DEFICIT_AMOUNT = "max.deficit.amount";
            public static final String POSTPAID_ONLY_MERCHANT = "POSTPAID_ONLY_MERCHANT";
            public static final String BIN_ELIGIBLE_COFT = "IS_COFT_BIN_ELIGIBLE";
            public static final String ENABLE_ACCOUNT_RANGE_CARD_BIN = "ENABLE_ACCOUNT_RANGE_CARD_BIN";
            public static final String OVERRIDE_ADDNPAY_BEHAVIOUR = "OVERRIDE_ADDNPAY_BEHAVIOUR";
            public static final String POSTPAID_ENABLED_ON_MERCHANT = "POSTPAID_ENABLED_ON_MERCHANT";
            public static final String TPV_MULTIPLE_MBID_ENABLED = "TPV_MULTIPLE_MBID_ENABLED";
            public static final String AOA_PAYTM_PG_MID = "AOA_PAYTM_PG_MID";
            public static final String UPI_COLLECT_WHITELISTED = "UPI_COLLECT_WHITELISTED";
            public static final String CHECK_USER_POSTPAID_STATUS_ENABLED = "CHECK_USER_POSTPAID_STATUS_ENABLED";
            public static final String FETCH_POSTPAID_BALANCE = "FETCH_POSTPAID_BALANCE";
            public static final String MASK_MOBILE_ON_CASHIER_PAGE_ENABLED = "MASK_MOBILE_ON_CASHIER_PAGE_ENABLED";
            public static final String RETURN_USER_INFO_IN_V5_FPO_RESPONSE = "RETURN_USER_INFO_IN_V5_FPO_RESPONSE";
            public static final String RETURN_USER_INFO_IN_V2_FPO_RESPONSE = "RETURN_USER_INFO_IN_V2_FPO_RESPONSE";
            public static final String SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD = "subscription.maximum.grace.days.debit.card";
            public static final String SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD = "subscription.maximum.grace.days.credit.card";
            public static final String ENABLE_COFT_PROMO_PAR_CONFIG = "ENABLE_COFT_PROMO_PAR_CONFIG";
            public static final String GLOBAL_VAULT_COFT = "GLOBAL_VAULT_COFT";
            public static final String POSTPAID_ONBOARDING_ENABLED = "POSTPAID_ONBOARDING_ENABLED";
            public static final String POSTPAID_2FA_ENABLED = "theia.enable.postpaid.2FA";
            public static final String INSTANT_SETTLEMENT = "INSTANT_SETTLEMENT";
            public static final String DISABLE_LIMIT_CC_ADD_N_PAY = "disableLimitCCAddNPay";

            public static final String HIDE_NET_BANKING = "HIDE_NET_BANKING";// preference
            // flag
            // toh
            // hide
            // NB
            // payment
            // method

            public static final String OFFLINE_DEALS = "OFFLINE_DEALS"; // preference
            // flag
            // for
            // fetching
            // deals

            public static final String FF4J_FETCH_DEALS_ENABLED = "theia.fetch.deals.enable"; // ff4j
            // flag
            // for
            // fetching
            // deals

            public static final String POPULATE_BANK_RESULT_INFO = "POPULATE_BANK_RESULT_INFO";
            public static final String ENABLE_BIN_IDENTIFIER_IN_RESPONSE = "ENABLE_BIN_IDENTIFIER_IN_RESPONSE";
            public static final String BYPASS_CALLBACKURL_IN_REPEAT_REQUEST = "BYPASS_CALLBACKURL_IN_REPEAT_REQUEST";
            public static final String UPI_DIRECT_SETTLEMENT = "UPI_DIRECT_SETTLEMENT";
            public static final String CC_ON_UPI_RAILS_ENABLED = "CC_ON_UPI_RAILS_ENABLED";
            public static final String P2PM_MERCHANT = "P2PM_MERCHANT";
            public static final String UPI_LITE_DISABLED = "UPI_LITE_DISABLED"; // ff4j
            // flag
            // for
            // fetching
            // deals
            public static final String CUSTOM_TRANSACTION_WEBHOOK_URL_ENABLED = "customTransactionWebhookUrlEnabled";
            public static final String DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE = "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE";
            public static final String IS_3P_ADD_MONEY = "3P_ADD_MONEY";
            public static final String P2P_DISABLED = "P2P_DISABLED";
            public static final String ALLOW_TXN_DECLINE_RESPONSE = "ALLOW_TXN_DECLINE_RESPONSE";
            public static final String ADD_MONEY_SURCHARGE_EXEMPTION = "ADD_MONEY_SURCHARGE_EXEMPTION";
            public static final String ALLOW_PP_LIMIT_CHECK_IN_ELIGIBILITY_API = "ALLOW_PP_LIMIT_CHECK_IN_ELIGIBILITY_API";
        }

        public static class PreferenceValues {
            public static final String WITHOUT_CONVENIENCE_FEES_ENABLED = "Y";
            public static final String BIN_IN_RESPONSE_ENABLED = "Y";
            public static final String CHECKSUM_ENABLED_ENABLED = "Y";
            public static final String AUTO_CREATE_USER_ENABLED = "Y";
            public static final String ENC_PARAMS_ENABLED = "Y";
            public static final String ADD_MONEY_ENABLED = "Y";
            public static final String HYBRID_ALLOWED = "Y";
            public static final String SLAB_BASED_ALLOWED = "Y";
            public static final boolean PAYMENT_OTP_ENABLED = true;
            public static final int INITIALIZE_INVALID_ATTEMPTS = 0;
            public static final int INITIALIZE_RESEND_OTP = 0;
            public static final int INCREMENT = 1;
            public static final int MAX_OTP_ATTEMPTS = 3;
            public static final int MAX_OTP_SEND_CONT = 3;
            public static final String LINKED_PLATFORM_FACEBOOK = "facebook";
            public static final String QR_PAY_ENABLED = "Y";
            public static final String STORE_CARD_DETAILS = "YES";

            public static final String Y = "Y";
            public static final String PRN_ENABLED = "Y";
            public static final String LOGIN_DISABLED = "Y";
            public static final String ENHANCED_CASHIER_PAGE_ENABLED = "Y";
            public static final String API_DISABLED = "Y";
            public static final String IS_MOCK_MERCHANT = "Y";
            public static final String RETURN_USER_VPA_ENABLED = "Y";
            public static final String REFUND = "REFUND";
            public static final String HIDE_NET_BANKING = "Y";

        }

        public static enum Status {
            ACTIVE, INACTIVE
        }
    }

    public static class MerchantExtendedInfo {

        public static class MerchantExtendInfoKeys {
            public static final String MERCHANT_KEY = "entityKey";
            public static final String S2S_CALLBACK_ENABLED = "s2sCallbackEnabled";
            public static final String PEON_ENABLE = "isPeonEnable";
            public static final String BUSINESS_NAME = "businessName";
            public static final String NUMBER_OF_RETRY = "numberOfRetry";
            public static final String ENTITY_ID = "entityId";
            public static final String MERCHANT_NAME = "merchantName";
            public static final String OTP_LOGIN_ENABLED = "isOtpThemeEnabled";
            public static final String MERCHANT_PRIVATE_KEY = "merchantPrivateKey";
            public static final String ON_PAYTM = "ONPAYTM";
            public static final String AGG_MID = "aggregatorMid";
            public static final String RESELLER_PARENT_MID = "resellerParentMid";
            public static final String MERCHANT_INACTIVE = "status";
            public static final String MERCHANT_BLOCKED = "blocked";
            public static final String KYB_ID = "kybId";
            // For Merchant Gift Voucher
            public static final String MERCHANT_KYB_ID = "merchantKybId";
            public static final String INTENT_RETRY_PAYTM = "INTENT_RETRY_PAYTM";
        }

        public static class MerchantExtendInfoValues {
            public static final String S2S_CALLBACK_ENABLED = "true";
            public static final String PEON_ENABLE = "true";
            public static final String OTP_LOGIN_ENABLED = "1";
            public static final String MERCHANT_INACTIVE = "INACTIVE";
            public static final String MERCHANT_BLOCKED = "true";
        }
    }

    public static final class PrepaidCard {
        public static final String FF4J_PREPAID_CARD_STRING = "prepaidCard";
        public static final String PREPAID_CARD_MAX_AMOUNT = "prepaid.card.max.amount";
    }

    public static final class EmiSubvention {

        /*
         * EMI subvention redis key
         */
        public static final String ITEM_LIST = "ITEM_LIST";
        public static final String VALIDATE_EMI_RESPONSE = "VALIDATE_EMI_RESPONSE";
        public static final String VALIDATE_EMI_REQUEST = "VALIDATE_EMI_REQUEST";
        public static final String EMI_SUBVENTED_TRANSACTION_AMOUNT = "EMI_SUBVENTED_TRANSACTION_AMOUNT";
    }

    public static final class RequestParams {
        public static final class Native {
            public static final String MID = "mid";
            public static final String ORDER_ID = "orderId";
            public static final String REFERENCE_ID = "referenceId";
            public static final String CHANNEL_ID = "channelId";
            public static final String TXN_TOKEN = "txnToken";
            public static final String PAYMENT_CALL_DCC = "paymentCallFromDccPage";
            public static final String DCC_SELECTED_BY_USER = "dccSelectedByUser";
            public static final String PAYMENT_MODE = "paymentMode";
            public static final String CARD_INFO = "cardInfo";
            public static final String AUTH_MODE = "authMode";
            public static final String CHANNEL_CODE = "channelCode";
            public static final String PAYER_ACCOUNT = "payerAccount";
            public static final String CREDIT_BLOCK_MAP = "creditBlock";
            public static final String MPIN = "mpin";
            public static final String STORE_INSTRUMENT = "storeInstrument";
            public static final String PAYMENT_FLOW = "paymentFlow";
            public static final String ENCRYPTED_CARD_INFO = "encCardInfo";
            public static final String PLAN_ID = "planId";
            public static final String WALLET_TYPE = "walletType";
            public static final String ACCOUNT_NUMBER = "account_number";
            public static final String BANK_NAME = "bank_name";
            public static final String CREDIT_BLOCK = "credit_block";
            public static final String SEQ_NUMBER = "seq_number";
            public static final String DEVICE_ID = "device_id";
            public static final String EMI_TYPE = "EMI_TYPE";
            public static final String AGG_MID = "aggMid";
            public static final String TXN_NOTE = "txnNote";
            public static final String CARD_HASH = "cardHash";
            public static final String REF_URL = "refUrl";
            public static final String ADDITIONAL_INFO = "additional_info";
            public static final String EXTEND_INFO = "extend_info";
            public static final String ALLOW_UNVERIFIED_ACCOUNT = "allowUnverifiedAccount";
            public static final String VALIDATE_ACCOUNT_NUMBER = "validateAccountNumber";
            public static final String IFSC = "ifsc";
            public static final String APP_ID = "appId";
            public static final String NATIVE_JSON_REQUEST = "nativeJsonRequest";
            public static final String TOKEN_TYPE = "tokenType";
            public static final String REQUEST_ID = "requestId";
            public static final String RISK_EXTENDED_INFO = "risk_extended_info";
            public static final String MERC_UNQ_REF = "mercUnqRef";
            public static final String PAYMENT_OFFERS_APPLIED = "PAYMENT_OFFERS_APPLIED";
            public static final String LINK_ID = "linkId";
            public static final String LINK_INVOICE_ID = "linkInvoiceId";
            public static final String LINK_DESC = "linkDesc";
            public static final String LINK_NAME = "linkName";
            public static final String LINK_DETAILS = "linkDetails";
            public static final String IS_ORDER_ID_NEED_TO_BE_GENERATED = "IS_ORDER_ID_NEED_TO_BE_GENERATED";
            public static final String MGV_TEMPLATE_ID = "templateId";
            public static final String APP_VERSION = "appVersion";
            public static final String CLIENT = "client";
            public static final String WORKFLOW = "workFlow";
            public static final String HEADER_WORKFLOW = "headerWorkFlow";
            public static final String GUEST_TOKEN = "guestToken";
            public static final String ACCESS_TOKEN = "accessToken";
            public static final String CHECKOUT = "checkout";
            public static final String TOKEN = "token";
            public static final String ONE_CLICK_INFO = "oneClickInfo";
            public static final String ECOM_TOKEN_INFO = "ecomTokenInfo";
            public static final String UPI_ACC_REF_ID = "upiAccRefId";
            public static final String ORIGIN_CHANNEL = "originChannel";
            public static final String OFFLINE_EXTEND_INFO = "offline_extend_info";
            public static final String REF_ID = "refId";
            public static final String SDK_TYPE = "sdkType";
            public static final String EMI_SUBVENTION_INFO = "emiSubventionInfo";
            public static final String CARD_PRE_AUTH_TYPE = "cardPreAuthType";
            public static final String PRE_AUTH_BLOCK_SECONDS = "PreAuthBlockSeconds";
            public static final String PREFERRED_OTP_PAGE = "preferredOtpPage";
            public static final String AOA_SUBS_ON_PGMID = "aoaSubsOnPgMid";
            public static final String ADD_ONE_RUPEE = "addOneRupee";
            public static final String TXN_AMOUNT_IN_FETCH_PAYMODE_STATUS_API = "txnAmount";

            // Constants for Bank Mandates
            public static final String USER_NAME = "USER_NAME";
            public static final String ACCOUNT_TYPE = "ACCOUNT_TYPE";
            public static final String BANK_IFSC = "bankIfsc";
            public static final String MANDATE_AUTH_MODE = "mandateAuthMode";
            public static final String MANDATE_TYPE = "mandateType";
            public static final String MANDATE_REQ_DOC = "MandateReqDoc";
            public static final String CHECKSUM_VAL = "CheckSumVal";
            public static final String BANK_ID = "BankID";
            public static final String MERCHANTID = "MerchantID";
            public static final String AUTHMODE = "AuthMode";
            public static final String SPID = "SPID";
            public static final String POST = "POST";
            public static final String MANDATE_ORDER_TIMEOUT_FOR_UPFRONT_PAYMENT = "mandate.order.timeout.days.for.upfront.amount.payment";
            public static final String POLL_STATUS = "POLL_STATUS";
            public static final String AUTO_REFUND = "isAutoRefund";
            public static final String BLOCK_NON_CC_DC_PAYMODES = "blockNonCCDCPaymodes";
            public static final String AUTO_REFUND_MAX_ACCEPTABLE_AMOUNT = "autoRefund.max.acceptable.amount";
            public static final String CONVERT_TO_ADDANDPAY_TXN = "convertToAddAndPayTxn";

            // Added Data Enrichment Fields
            // public static final String RISK_EXTENDED_INFO ="riskExtended"

            // Constants for Bank Mandates

            public static final String REDIRECTION_URL = "redirectionUrl";
            public static final String PAYERCMID = "payerCmid";
            public static final String QRCODE_ID = "qrCodeId";

            public static final String DEAL_FLOW = "DEAL_FLOW";
            public static final String ADDMONEY = "ADD_MONEY";
            public static final String ADDANDPAY = "ADDANDPAY";
        }

        public static final String TXN_AMOUNT_IN_REQUEST = "TXN_AMOUNT_IN_REQUEST";
        public static final String TXN_AMOUNT_FROM_QR_SERVICE = "TXN_AMOUNT_FROM_QR_SERVICE";
        public static final String ORDER_ALREADY_CREATED = "orderAlreadyCreated";
        public static final String EDC = "EDC";
        public static final String SAVED_CARD_ENABLE = "ON";
        public static final String SAVED_CARD_ENABLE_ALT = "Y";
        public static final String ID = "id";
        public static final String REQUEST_TYPE = "REQUEST_TYPE";
        public static final String REQUEST_ID = "REQUEST_ID";
        public static final String MID = "MID";
        public static final String REFERENCE_ID = "REFERENCE_ID";
        public static final String IS_MOCK_REQUEST = "IS_MOCK_REQUEST";
        public static final String IS_QR_CODE_ID = "qrCodeId";
        public static final String IS_SKIP_ORDER_CREATION = "skipOrderCreation";

        /**
         * Hack to Support all merchant
         */
        public static final String MID_LOWER_CASE = "mid";
        public static final String ORDERID_LOWER_CASE = "orderid";
        public static final String ORDER_ID_LOWER_CASE = "order_id";
        public static final String MID1 = "Mid";
        public static final String MID2 = "MId";
        public static final String ORDER_ID = "ORDER_ID";
        public static final String CUST_ID = "CUST_ID";
        public static final String TXN_ID = "TXN_ID";
        public static final String TXN_AMOUNT = "TXN_AMOUNT";
        public static final String TIP_AMOUNT = "TIP_AMOUNT";
        public static final String CHANNEL_ID = "CHANNEL_ID";
        public static final String INDUSTRY_TYPE_ID = "INDUSTRY_TYPE_ID";
        public static final String WEBSITE = "WEBSITE";
        public static final String CHECKSUMHASH = "CHECKSUMHASH";
        public static final String MOBILE_NO = "MOBILE_NO";
        public static final String EMAIL = "EMAIL";
        public static final String PAYMENT_MODE_ONLY = "PAYMENT_MODE_ONLY";
        public static final String AUTH_MODE = "AUTH_MODE";
        public static final String PAYMENT_TYPE_ID = "PAYMENT_TYPE_ID";
        public static final String CARD_TYPE = "CARD_TYPE";
        public static final String BANK_CODE = "BANK_CODE";
        public static final String PROMO_CAMP_ID = "PROMO_CAMP_ID";
        public static final String ORDER_DETAILS = "ORDER_DETAILS";
        public static final String COMMENTS = "COMMENTS";
        public static final String DOB = "DOB";
        public static final String PAN_CARD = "PAN_CARD";
        public static final String DL_NUMBER = "DL_NUMBER";
        public static final String MSISDN = "MSISDN";
        public static final String VERIFIED_BY = "VERIFIED_BY";
        public static final String IS_USER_VERIFIED = "IS_USER_VERIFIED";
        public static final String ADDRESS_1 = "ADDRESS_1";
        public static final String ADDRESS_2 = "ADDRESS_2";
        public static final String CITY = "CITY";
        public static final String STATE = "STATE";
        public static final String PINCODE = "PINCODE";
        public static final String LOGIN_THEME = "LOGIN_THEME";
        public static final String CALLBACK_URL = "CALLBACK_URL";
        public static final String IS_CALLBACK_URL_PRESENT = "IS_CALLBACK_URL_PRESENT";
        public static final String THEME = "THEME";
        public static final String MERCH_UNQ_REF = "MERC_UNQ_REF";
        public static final String UDF_1 = "UDF_1";
        public static final String UDF_2 = "UDF_2";
        public static final String UDF_3 = "UDF_3";
        public static final String ADDITIONAL_INFO = "ADDITIONAL_INFO";
        public static final String CONNECTION_TYPE = "CONNECTION_TYPE";
        public static final String ENC_PARAMS = "ENC_DATA";
        public static final String CVV = "CVV";
        public static final String OTP = "otp";
        public static final String WALLET_AMOUNT = "WALLET_AMOUNT";
        public static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";
        public static final String RESEND_COUNT = "RESEND_COUNT";
        public static final String CANCEL_POINT = "TARGET";
        public static final String SCW_PAYTM_MID = "scw.mid";
        public static final String SSO_TOKEN = "SSO_TOKEN";
        public static final String PAYTM_TOKEN = "PAYTM_TOKEN";
        public static final String TXN_MODE = "txnMode";
        public static final String PAYMENT_MODE_DISABLED = "PAYMENT_MODE_DISABLE";
        public static final String OAUTH_SSOID = "OAUTH_SSOID"; // AuthCode
        public static final String SAVED_CARD_ID = "SAVED_CARD_ID";
        public static final String PAYMENT_DETAILS = "PAYMENT_DETAILS";
        public static final String PEON_URL = "PEON_URL";
        public static final String PROMOCODE = "promoCode";
        public static final String PROMO_RESPONSE_CODE = "promoCode_response";
        public static final String INVOICE_PAYMENT_TYPE = "INVOICE_PAYMENT_TYPE";
        public static final String ORDER_TIMEOUT_MILLI_SECOND = "ORDER_TIMEOUT_MILLI_SECOND";
        public static final String OAUTH_STATE = "state";
        public static final String DIRECT_PAYMODE_TYPE = "DIRECT_PAYMODE_TYPE";
        public static final String UNIQUE_ID = "uniqueId";
        public static final String PARTNER_ID = "partnerId";

        // Subscription
        public static final String SUBS_PAYMENT_MODE = "SUBS_PAYMENT_MODE";
        public static final String SUBS_PPI_ONLY = "SUBS_PPI_ONLY";
        public static final String SUBSCRIPTION_SERVICE_ID = "SUBS_SERVICE_ID";
        public static final String SUBSCRIPTION_ID = "SUBS_ID";
        public static final String SUBSCRIPTION_AMOUNT = "amount";
        public static final String SUBSCRIPTION_AMOUNT_TYPE = "SUBS_AMOUNT_TYPE";
        public static final String SUBS_FREQUENCY = "SUBS_FREQUENCY";
        public static final String SUBS_FREQUENCY_UNIT = "SUBS_FREQUENCY_UNIT";
        public static final String SUBS_START_DATE = "SUBS_START_DATE";
        public static final String SUBS_EXPIRY_DATE = "SUBS_EXPIRY_DATE";
        public static final String SUBS_GRACE_DAYS = "SUBS_GRACE_DAYS";
        public static final String SUBS_ENABLE_RETRY = "SUBS_ENABLE_RETRY";
        public static final String SUBS_RETRY_COUNT = "SUBS_RETRY_COUNT";
        public static final String SUBS_MAX_AMOUNT = "SUBS_MAX_AMOUNT";
        public static final String SUBSCRIPTION_INITIATE_TRANSACTION_URL = "/theia/api/v1/subscription/create";
        public static final String CREATE_ORDER_IN_NATIVE_SUBSCRIPTION_CREATION = "theia.createOrderInNativeSubscriptionCreation";
        public static final String BLACKLIST_CREATE_ORDER_IN_NATIVE_SUBSCRIPTION_INITIATE = "theia.blackListCreateOrderInNativeSubscriptionInitiate";
        public static final String NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE = "NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE";

        public static final String STORE_CARD = "STORE_CARD";

        public static final String VIRTUAL_PAYMENT_ADDRESS = "VIRTUAL_PAYMENT_ADDRESS";
        // Seamless specific
        public static final String CARD_NUMBER = "CARD_NUMBER";
        public static final String EXPIRY_DATE = "EXPIRY_DATE";
        public static final String EXPIRY_YEAR = "EXPIRY_YEAR";
        public static final String EXPIRY_MONTH = "EXPIRY_MONTH";
        public static final String FIRST_SIX_DIGIT = "FIRST_SIX_DIGIT";
        public static final String LAST_FOUR_DIGIT = "LAST_FOUR_DIGIT";
        public static final String CVV2 = "CVV2";
        public static final String CARD_SCHEME = "cardScheme";

        public static final String AUTH_CODE = "code";
        public static final String IMPS_MMID = "mmid";
        public static final String IMPS_MOBILE_NO = "mobileNo";
        public static final String IMPS_OTP = "otp";
        // Promo Service Related
        public static final String CARD_NO = "CARD_NO";

        // Added parameters for risk.
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String DEVICE_SOURCE = "DEVICE_SOURCE";
        public static final String GOODS_INFO = "GOODS_INFO";
        public static final String SHIPPING_INFO = "SHIPPING_INFO";
        public static final String DEVICE_NAME = "deviceName";
        public static final String IMEI = "IMEI";
        public static final String DEVICE_MANUFACTURE = "deviceManufacturer";
        public static final String DEVICE_LANGUAGE = "deviceLanguage";
        public static final String LANGUAGE = "language";

        /*
         * Paytm Express Specific
         */
        public static final String IS_SAVED_CARD = "IS_SAVED_CARD";
        public static final String ADD_MONEY = "addMoney";

        /*
         * Auto-Debit Params
         */
        public static final String AUTO_DEBIT_URL = "/theia/HANDLER_IVR/CLW_APP_PAY";
        public static final String AUTO_DEBIT_DATA = "JsonData";
        public static final String AUTO_DEBIT_MID = "MId";
        public static final String AUTO_DEBIT_REQUEST_TYPE = "ReqType";
        public static final String AUTO_DEBIT_ORDER_ID = "OrderId";

        /*
         * FastForward Params
         */
        public static final String FAST_FORWARD_URL = "/theia/HANDLER_IVR/CLW_APP_PAY/APP";

        /*
         * CC Bill Payment
         */
        public static final String CC_BILL_NO = "CC_BILL_NO";

        /*
         * Paytm CC/Pay later
         */
        public static final String PASS_CODE = "PASS_CODE";
        public static final String POSTPAID_ONBOARDING_SUPPORTED = "postpaidOnboardingSupported";

        public static final String CUSTOMIZE_CODE = "CUSTOMIZE_CODE";
        public static final String TXN_TOKEN = "TXN_TOKEN";

        /*
         * EMI subvention redis key
         */
        public static final String ITEM_LIST = "ITEM_LIST";
        public static final String VALIDATE_EMI_RESPONSE = "VALIDATE_EMI_RESPONSE";
        public static final String VALIDATE_EMI_REQUEST = "VALIDATE_EMI_REQUEST";
        public static final String EMI_SUBVENTED_TRANSACTION_AMOUNT = "EMI_SUBVENTED_TRANSACTION_AMOUNT";
        public static final long EMI_SUBVENTION_EXPIRY_TIME = 300;

        /*
         * EMI subvention constants
         */
        public static final String EMI_SUBVENTION_CUSTOMER_ID = "customer-id";
        public static final String PG_MERCHANT_ID = "pg-mid";
        public static final String EMI_SUBVENTION_SITEID = "1";
        public static final String USER_ID = "user_id";
        public static final String SUBVENTION = "SUBVENTION";
        public static final String SUBVENTION_PRODUCT = "SUB";
        public static final String STANDARD_PRODUCT = "STAN";
        public static final String EMI_SUBVENTION_NOT_APPLICABLE = "emi subvention not applicable";

        // BajajFn EMI Options
        public static final String EMI_OPTIONS = "EMI_OPTIONS";
        public static final String BAJAJFN_DROP = "BAJAJFN-DROP_ALL";

        public static final String SHOW_VIEW_FLAG = "SHOW_VIEW_FLAG";
        public static final String MERCHANT_IMAGE = "MERCHANT_IMAGE";
        public static final String PAYMENT_SCREEN = "PAYMENT_SCREEN";
        public static final String MERCHANT_LIMIT_ERROR_SCREEN = "MERCHANT_LIMIT_ERROR_SCREEN";
        public static final String TRANSACTION_ID = "TRANSACTION_ID";
        public static final String LINK_BASED_KEY = "LINK_BASED_KEY_";
        public static final String KYC_VALIDATE_FLAG = "KYC.VALIDATE";
        public static final String KYC_ALLOWED_ALL_FLAG = "KYC.ALLOWED.ALL";
        public static final String KYC_ALLOWED_LIST = "KYC.ALLOWED.LIST";
        public static final String UPI_MANDATE_APP_LIST = "UPI.MANDATE.APP.LIST";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String KYC_NATIVE_VALIDATE_FLAG = "KYC.NATIVE.VALIDATE";
        public static final String ERROR_CODE = "ERROR_CODE";
        public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

        // App invoke
        public static final String IS_APP_INVOKE = "isAppInvoke";
        public static final String IS_APP_LINK = "isAppLink";
        /*
         * upi psp App List
         */
        public static final String UPI_PSP_NAME = "upi.options.psp.names";
        public static final String UPI_PSP_ICON_BASE_PATH = "native/psp/";

        public static final String COFT_CONSENT = "coftConsent";
        public static final String CARDTOKEN_INFO = "cardTokenInfo";

        public static final String PAYTM_SSO_TOKEN = "paytmSsoToken";
        public static final String CARD_INFO = "cardInfo";
        public static final String SOURCE = "source";

    }

    public static final class RequestTypes {
        public static final String PAY_MODE_NOT_PRESENT_FOR_PROMO = "PAY_MODE_NOT_PRESENT_FOR_PROMO";
        public static final String INVOICING = "INVOICING";
        public static final String EMAIL_INVOICING = "EMAIL_INVOICING";
        public static final String SMS_INVOICING = "SMS_INVOICING";
        public static final String EMAIL_INVOICE = "EMAIL_INVOICE";
        public static final String SMS_INVOICE = "SMS_INVOICE";
        public static final String DEFAULT = "DEFAULT";
        public static final String SUBSCRIPTION = "SUBSCRIBE";
        public static final String RENEW_SUBSCRIPTION = "RENEW_SUBSCRIPTION";
        public static final String PARTIAL_RENEW_SUBSCRIPTION = "PARTIAL_RENEW_SUBSCRIPTION";
        public static final String SEAMLESS = "SEAMLESS";
        public static final String SEAMLESS_NATIVE = "SEAMLESS_NATIVE";
        public static final String ADD_MONEY = "ADD_MONEY";
        public static final String AUTO_DEBIT = "CLW_APP_PAY";
        public static final String PAYTM_EXPRESS = "PAYTM_EXPRESS";
        public static final String TOPUP_EXPRESS = "TOPUP_EXPRESS";
        public static final String CC_BILL_PAYMENT = "CC_BILL_PAYMENT";
        public static final String SEAMLESS_ACS = "SEAMLESS_ACS";
        public static final String LINK_BASED_PAYMENT = "LINK_BASED_PAYMENT";
        public static final String ADDMONEY_EXPRESS = "EXPRESS_ADD_MONEY";
        public static final String STOCK_TRADE = "STOCK_TRADE";
        public static final String OFFLINE = "OFFLINE";
        public static final String MOTO_CHANNEL = "MOTO_CHANNEL";
        public static final String DYNAMIC_QR = "DYNAMIC_QR";
        public static final String NATIVE = "NATIVE";
        public static final String SEAMLESS_S2S = "SEAMLESS_S2S";
        public static final String LINK_BASED_PAYMENT_INVOICE = "LINK_BASED_PAYMENT_INVOICE";
        public static final String SEAMLESS_3D_FORM = "SEAMLESS_3D_FORM";
        public static final String UNI_PAY = "UNI_PAY";
        public static final String RESELLER = "RESELLER";
        public static final String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
        public static final String SEAMLESS_NB = "SEAMLESS_NB";
        public static final String DYNAMIC_QR_2FA = "DYNAMIC_QR_2FA";
        public static final String DEFAULT_MF = "DEFAULT_MF";
        public static final String NATIVE_MF = "NATIVE_MF";
        public static final String FETCH_PAYMENT_INSTRUMENT = "FETCH_PAYMENT_INSTRUMENT";
        public static final String NATIVE_ST = "NATIVE_ST";
        public static final String NATIVE_MF_SIP = "NATIVE_MF_SIP";
    }

    public static final class RetryConstants {
        public static final String TRANS_ID = "TRANS_ID";
        public static final String RETRY_PAYMENT_ = "RETRY_PAYMENT_";
        public static final String PAYTM_MID = "paytmMerchantId";
        public static final String IS_RETRY = "isRetry";
        public static final String YES = "YES";
        public static final String NO = "NO";
        public static final String MID = "MID";
        public static final String ORDER_ID = "ORDER_ID";
        public static final String RESPONSE_PAGE = "responsePage";
        public static final String CASHIER_REQUEST_ID = "CASHIER_REQUEST_ID";
        public static final String PAYMENT_MODE = "PAYMENT_MODE";
        public static final String CARD_NUMBER = "cardNumber";
        public static final String JSP_PATH = "/WEB-INF/views/jsp/";
        public static final String RETRY_SUCCESS_PAGE = "paymentForm.jsp";
        public static final String ERROR_PAGE = "/WEB-INF/views/jsp/error.jsp";
        public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
        public static final String SAVED_CARD_TXN = "SAVED_CARD_TXN";
        public static final String ISSUING_BANK_NAME = "issuingBankName";
        public static final String EMI_PLAN_ID = "emiPlanId";
        public static final String EMI_PAYMENT_MODE = "emi";
        public static final String UPI = "UPI";
        public static final String IS_REQUEST_ALREADY_DISPATCH = "IS_REQUEST_ALREADY_DISPATCH";
        public static final String SC_PAYMENT_MODE = "SC";
        public static final String RETRY_INITIATED = "retryInitiated";
        public static final String OFFLINE_RETRY_ALL = "offline.retry.all";
        public static final String MAX_RETRY_ALLOWED_ON_OFFLINE_MERCHANT = "maxRetryAllowedOnOfflineMerchant";
        public static final String ACTUAL_MID = "actualMid";
    }

    public static final class ExtraConstants {
        // PGP-36172
        public static final String ENABLE_IPV6 = "enable.ipv6";
        public static final String RETRY_DATA_MERCHANT_CONFIG = "retryDataAndMerchantConfig";
        public static final String CHECKOUT_JS_FLAG = "checkoutJsFlag";
        public static final String TOP_NB_CHANNEL_SIZE = "top.nb.channel.size";
        public static final int TOP_NB_CHANNEL_SIZE_DEFAULT = 5;
        public static final String NB_CHANNEL_PPBL = "PYTM";
        public static final String OFFLINE_PAYMODES = "offline.paymode.order";
        public static final String OFFLINE_ADD_MONEY_PAYMODES = "offline.addmoney.paymode.order";
        public static final String UIREVAMP_PAYMODES = "uirevamp.paymode.order";
        public static final String UIREVAMP_ADDMONEY_PAYMODES = "uirevamp.addmoney.paymode.order";
        public static final String NATIVE_PAYMODES = "native.paymode.order";
        public static final String NATIVE_ONLINE_PAYMODES = "native.online.paymode.order";
        public static final String UIREVAMP_PAYMODE_GROUPS = "uirevamp.paymode.group.order";
        public static final String UIREVAMP_ADDMONEY_PAYMODE_GROUPS = "uirevamp.addmoney.paymode.group.order";
        public static final String NATIVE_PAYMODE_GROUPS = "native.paymode.group.order";
        public static final String NATIVE_ONLINE_PAYMODE_GROUPS = "native.online.paymode.group.order";
        public static final String SIGNATURE_REQUIRED = "Y";
        public static final String SIGNATURE = "signature";
        public static final String LOGIN = "login";
        public static final String PRE_LOGIN = "pre-login";
        public static final String WALLET_TYPE = "BALANCE";
        public static final String CREDIT_CARD = "CREDIT_CARD";
        public static final String CC = "CC";
        public static final String DC = "DC";
        public static final String NET_BANKING = "NET_BANKING";
        public static final String NET_BANKING_ZEST = "NET_BANKING_ZEST";
        public static final String NB = "NB";
        public static final String DEBIT_CARD = "DEBIT_CARD";
        public static final String IMPS = "IMPS";
        public static final String ATM = "ATM";
        public static final String EMI = "EMI";
        public static final String EMI_DC = "EMI_DC";
        public static final String UPI = "UPI";
        public static final String UPI_COLLECT = "UPI_COLLECT";
        public static final String UPI_PUSH = "UPIPUSH";
        public static final String UPI_PUSH_EXPRESS = "UPIPUSHEXPRESS";
        public static final String PAYTM_DIGITAL_CREDIT = "PAYTM_DIGITAL_CREDIT";
        public static final String ADVANCE_DEPOSIT_ACCOUNT = "ADVANCE_DEPOSIT_ACCOUNT";
        public static final String MP_COD = "MP_COD";
        public static final String GIFT_VOUCHER = "GIFT_VOUCHER";
        public static final String HYBRID_PAYMENT = "HYBRID_PAYMENT";
        public static final String BANKCARD = "BANKCARD";
        public static final String TXN_SUCCESS = "TXN_SUCCESS";
        public static final String PROCESSING = "PROCESSING";
        public static final String PENDING = "PENDING";
        public static final String TXN_CANCEL = "CANCEL";
        public static final String SUCCESS = "Success";
        public static final String TYPE = "paytm";
        public static final String ALIPAY_MERCHANT_ID = "alipayMerchantId";
        public static final String MERCHANT_KEY = "merchantKey";
        public static final String P2M = "P2M";
        public static final String CURRENT_COUNT = "currentCount";
        public static final String MAX_COUNT = "maxCount";
        public static final String CREATE_ORDER_INT_TXN = "createOrderinIntTxn";
        public static final String BLACKLIST_CREATE_ORDER_INT_TXN = "theia.blacklistCreateOrderInIntTxn";
        public static final String POPULATE_CHECKOUT_PROMO_CODE_IN_CREATE_ORDER = "theia.populateCheckoutPromoCodeInCreateOrder";
        public static final String ACCESS_TOKEN_INVALID_SSO_RESPONSE_CODE = "theia.updateAccessTokenInvalidSSORespCode";
        public static final String MERCHANT_MAPPING = "MERCHANT_MAPPING";
        public static final String MERCHANT_DATA = "MERCHANT_DATA";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
        public static final boolean BOOLEAN_TRUE = true;
        public static final boolean BOOLEAN_FALSE = false;
        public static final String CHECKSUM_ENABLED = "CHECKSUM_ENABLED";
        /* PGP-37628 */
        public static final String SUBSCRIPTION_PAYMODE_FOR_AOA_TO_PG_CONVERSION = "subscription.paymode.for.aoa.to.pg.conversion";
        public static final String DISABLE_CREATE_ORDER_ENHANCED_TXN = "theia.disableCreateOrderinEnhancedTxn";
        /* PGP-38890 */
        public static final String REMOVE_INTERNAL_PROMO_SUPPORT = "theia.removeInternalPromoSupport";
        public static final String REMOVE_INTERNAL_PROMO_SUPPORT_BY_SETTING_PROMO_VALUE_NULL = "theia.removeInternalPromoSupportBySettingPromoValueNull";

        public static final String MERCHANT_ID = "merchantId";
        public static final String CASHIER_REQUEST_ID = "cashierRequestId";
        public static final String TRANSACTION_STATUS_JSON_REQUEST = "transactionStatusJsonRequest";
        public static final String TRANS_ID = "transId";
        public static final String PAYMENT_MODE = "paymentMode";
        public static final String PAY_METHOD = "payMethod";
        public static final String PENDING_STATUS = "PENDING";
        public static final String REQUEST_TYPE = "requestType";
        public static final String RESPONSE_DESTINATION = "responseDestination";
        public static final String MAX_PAYMENT_COUNT = "max.payment.count";
        public static final String SUBSCRIPTION_ID = "subscriptionId";
        public static final String TXN_TOKEN = "txnToken";

        public static final String SUBMIT = "submit";
        public static final String REQUEST_JSON_TYPE = "application/json;charset=UTF-8";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String REDIRECT = "redirect";
        public static final String PAYTM = "Paytm";

        public static final String ORDER_ID = "orderId";
        public static final String M_ID = "mId";
        public static final String TRANSACTION_ID = "transactionId";
        public static final String AMOUNT = "amount";
        public static final String PAYME_MODE = "paymentMode";
        public static final String CURRENCY = "currency";
        public static final String TRANSACTION_DATE = "transactionDate";
        public static final String CUSTOMER_ID = "customerId";
        public static final String RESPONSE_CODE = "responseCode";
        public static final String CHECKSUM = "checksum";
        public static final String NATIVE_ENHANCED = "NATIVE_ENHANCED";
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
        public static final String OAUTH_ADMIN_TOKEN = "oauth.admin.token";
        public static final String MERCHANT3_WHITELISTED_MIDS = "merchant3.theme.enabled.mids";
        public static final String OAUTH_WHITELISTED_CLIENT_ID = "oauth.whitelisted.client.id";
        public static final String OAUTH_WHITELISTED_CLIENT_SECRET_KEY = "oauth.whitelisted.client.secret.key";

        public static final String PAYM_PROPERTIES_CACHE_KEY = "PAYTM_PROPERTIES";

        public static final String ADDRESS1 = "ADDRESS1";
        public static final String ADDRESS2 = "ADDRESS2";
        public static final String ORDERID = "ORDERID";
        public static final String OAUTH_RETURN_URL = "return_url";
        public static final String LOGINTHEME = "login_theme";
        public static final String AUTO_SIGNUP = "auto_signup";
        public static final String OAUTH_LOGOUT_PROPERTY = "oauth.logoutUrl";
        public static final String OAUTH_LOGOUT_URL = "https://accounts.paytm.com/oauth2/logout";
        public static final String OAUTH_LOGOUT_RETURN_URL = "logoutUrl";
        public static final String OAUTH_LOGOUT_RETURN_URL_OLD_PG = "logoutUrl.old.pg";
        public static final String OAUTH_AUTHORIZE_URL = "oauth2/authorize";

        public static final String OAUTH_BASE_URL = "oauth.base.url";
        public static final String ONE_CLICK_PAYMENT = "ONE_CLICK_PAYMENT";
        public static final String OAUTH_INTRA_BASE_URL = "oauth-int.base.url";

        // OAuth JSON Params
        public static final String JSON_WEB_CLIENT_ID = "client_id";
        public static final String JSON_WAP_CLIENT_ID = "wap_client_id";
        public static final String JSON_OAUTH_HOST = "oAuthBaseUrl";

        // OAuth logout Variables
        public static final String AUTH_LOGOUT_RETURN_URL = "AUTH_LOGOUT_RETURN_URL";
        public static final String AUTH_LOGOUT_URL = "AUTH_LOGOUT_URL";
        public static final String PAYTM_TOKEN = "PAYTM_TOKEN";
        public static final String AUTO_LOGIN = "autoLogin";
        public static final String AUTO_LOGIN_VALUE = "M";

        public static final String EXTENDED_INFO_KEY_CALLBACK_URL = "callBackURL";
        public static final String EXTENDED_INFO_KEY_TOPUPANDPAY = "topupAndPay";
        public static final String EXTENDED_INFO_VALUE_TOPUPANDPAY = "true";
        public static final String EMPTY_STRING = "";
        public static final long TRANSACTION_TYPE_KEY_EXPIRY = 600;
        public static final String YES_STRING = "YES";

        public static final String COD_MIN_AMOUNT = "cod.min.amount";
        public static final String MIN_AMOUNT_FOR_ADD_MONEY_VOUCHER = "min.amount.for.add.money.voucher";
        public static final String SUBSCRIPTION_MIN_AMOUNT = "subscription.min.amount";
        public static final String DEFAULT_RETRY_COUNT = "100";
        public static final String ADD_MONEY_FLAG_VALUE = "1";
        public static final String RISK_SESSION_COOKIE = "PAYTM_PG";
        public static final String DEVICE_ID = "deviceId";
        public static final String SUBS_BM_MODE = "SUBS_BM_MODE";
        public static final String MERCHANT_URL_INFO_WEBSITE_FOR_BM = "BM";

        public static final String CONNECTION_TYPE_S2S = "S2S";

        public static final String CREDIT = "CREDIT";
        public static final String DEBIT = "DEBIT";
        public static final String AUTO_DEBIT_BANK_NAME = "WALLET";
        public static final String LOYALTY_POINT_BANK_NAME = "LOYALTY_POINT";
        public static final String PROMOCODE_TYPE_DISCOUNT = "DISCOUNT";
        public static final String PROMOCODE_TYPE_CASHBACK = "CASHBACK";

        public static final String UI_LOG_EVENT_TO_CONSOLE_PROPERTY = "theia.ui.log.event.console";
        public static final String UI_LOG_EVENT_TO_SERVER_PROPERTY = "theia.ui.log.event.server";

        public static final String TRANSACTION_RESPONSE_OBJECT = "transactionResponse";

        public static final String OFFLINE_FLOW = "offlineFlow";

        /*
         * For Express Payment
         */
        public static final String EXPRESS_CARD_TOKEN = "EXPRESS_CARD_TOKEN_";
        public static final String EXPRESS_PAYMENT = "EXPRESS_PAYMENT_";
        public static final String PAYTM_EXPRESS_1 = "1";
        public static final String PAYTM_EXPRESS_0 = "0";

        public static final String ALPHA_NUMERIC_PATTERN = "^[a-zA-Z0-9-|_@.-]*$";
        public static final String TOKEN_PATTERN = "^[a-zA-Z0-9_*.-]+$";
        public static final String JSON_PATTERN = "[\\{.*\\:\\{.*\\:.*\\}\\}]";
        public static final String TRANSACTION_ID_PATTERN = "[\\w-]+";
        public static final String RESPMSG_PATTERN = "[\\w\\s-,.:']+";
        public static final String HEADER_WORKFLOW_PATTERN = "[\\w_-]+";
        public static final int MID_MAX_LENGTH = 50;
        public static final int ORDER_ID_MAX_LENGTH = 50;
        public static final int CUST_ID_MAX_LENGTH = 50;
        public static final int TXN_AMOUNT_MAX_LENGTH = 50;
        public static final int INDUSTRY_TYPE_MAX_LENGTH = 50;
        public static final int WEBSITE_MAX_LENGTH = 50;
        public static final String INTERNATIONAL_BANK_ID = "INTL";

        public static final String INTERNATIONAL_BANK_CODE = "INTL";
        public static final String START_TIME = "startTime";
        public static final String FORWARD_SLASH = "/";
        public static final String QUESTION_MARK = "?";
        public static final String APMERSAND = "&";
        public static final String TRANSACTION_STATUS_URL = "/theia/transactionStatus";
        public static final String UPI_TRANSACTION_STATUS_URL = "/theia/upi/transactionStatus";
        public static final String V1_TRANSACTION_STATUS_URL = "/v1/transactionStatus";
        public static final String IS_ASYNC_TXN_STATUS_FLOW = "isAsyncTxnStatusFlow";
        public static final String PAYMENT_OPTION = "paymentOption";
        public static final String DIRECT_CARD_PAYMENT = "/theia/directBankCardPayment";
        public static final String CHANNEL_NOT_AVAILABLE = "CHANNEL_NOT_AVAILABLE";
        public static final String FETCH_PAYMENT_INSTRUMENT_URL = "/theia/fetchPaymentInstruments";
        public static final String FETCH_QR_PAYMENT_DETAILS = "/theia/api/v1/fetchQRPaymentDetails";
        public static final String FETCH_QR_PAYMENT_DETAILS_V2 = "/theia/api/v2/fetchQRPaymentDetails";
        public static final String THEIA_BASE_PATH = "theia.base.path";
        public static final String THEIA_BUISNESS_BASE_PATH = "theia.buisness.base.path";
        public static final String LINK_PAYMENT_STATUS_URL = "/linkPaymentRedirect";
        public static final String SET_LINK_CALLBACKURL = "link.setcallbackURL.enable";
        public static final String LINK_BUISNESS_URL = "link.business.path";
        public static final String FETCH_PAYMENT_OFFERS_URL = "/theia/api/v1/fetchAllPaymentOffers";
        public static final String FETCH_USER_PAYMENT_OFFERS_URL = "/theia/api/v1/fetchUserPaymentOffers";
        public static final String APPLY_PROMO_URL = "/theia/api/v1/applyPromo";
        public static final String V1_PTC = "/theia/api/v1/processTransaction";
        public static final String V1_DIRECT_BANK_REQUEST = "/theia/api/v1/directBankRequest";
        public static final String V1_ENHANCED_VALIDATE_OTP = "/theia/api/v1/enhanced/login/validate/otp";
        public static final String V1_ENHANCED_LOGOUT_USER = "/theia/api/v1/enhanced/logout/user";
        public static final String V1_TRANSACTION_STATUS = "v1/transactionStatus";
        public static final String SDK_PROCESS_TRANSACTION = "/theia/sdk/processTransaction";
        public static final String ITEM_LEVEL_PROMO_URL = "/theia/api/item/level/applyPromo";
        public static final String INITIATE_PROCESS_TRANSACTION = "/theia/initiate/processTransaction";
        public static final String UPI_TXN_STATUS_URL = "/upi/transactionStatus";
        public static final String VALIDATE_VPA_V4_URI = "/theia/api/v4/vpa/validate";
        public static final String V4_FPO_URI = "/theia/api/v4/fetchPaymentOptions";
        public static final String UPI_PSP_URI = "/theia/v1/order/pay/upipsp";

        public static final String DEFAULT_BANK_SUCCESS_RATE_THRESHOLD = "60.00";
        public static final String SUCCESS_RATE_KEY = "BANK_SUCCESS_RATE_KEY";
        public static final String BANK_SUCCESS_RATE_KEY_EXPIRY = "bank.success.rate.key.expiry";
        public static final String BANK_SUCCESS_RATE_THRESHOLD = "success.rate.threshold.";
        public static final String LOW_SUCCESS_RATE_DISPLAY_MESSAGE = "low.success.rate.display.message";
        public static final String CHANNEL_MAINTAINENCE_DISPLAY_MESSAGE = "channel.maintainence.display.message";
        public static final String FETCH_SUCCESS_RATE = "fetch.success.rate";
        public static final String ENABLE_FLAG_SUCCESS_RATE = "1";
        public static final String VPA_REGEX = "^[a-zA-Z0-9.-]*$";
        public static final String CSRF_VALIDATION_ENABLED_FLAG = "csrf.validation.enabled";

        public static final String ACS_URL_DATA_CACHE_KEY_PREFIX = "ACS_URL_DATA";
        public static final String ACS_URL_FORMAT_PROPERTY_KEY = "acs.url.format";

        public static final String EXTENDED_INFO_KEY_SUCCESS_CALLBACK_URL = "successCallBackURL";
        public static final String EXTENDED_INFO_KEY_FAILURE_CALLBACK_URL = "failureCallBackURL";
        public static final String EXTENDED_INFO_KEY_PENDING_CALLBACK_URL = "pendingCallBackURL";

        public static final String VALIDATION_SKIP_CHECKSUM = "validtion.skip.checksum";
        public static final String FAILURE = "FAILURE";
        public static final String OAUTH_SUCCESS = "SUCCESS";

        // Add money express specific
        public static final String XXX = "XXX";
        public static final String MM = "MM";
        public static final String YYYY = "YYYY";

        public static final String OTP_ACTION_TYPE = "AUTHENTICATE";
        public static final String OTP_SCOPE = "txn";
        public static final String SIGN_IN_OTP_SCOPE = "wallet";
        public static final String OAUTH_ACTION_TYPE_REGISTER = "REGISTER";
        public static final String OAUTH_EVALUATION_TYPE_OAUTH_LOGIN = "OAUTH_LOGIN";
        public static final String OAUTH_ACTION_TYPE_VERIFY_PHONE = "VERIFY_PHONE";

        public static final String OTP_GENERATION_EXCEPTION_MESSAGE = "Something went wrong while generating OTP";
        public static final String OTP_VALIDATION_EXCEPTION_MESSAGE = "Something went wrong while validating OTP";
        public static final String AUTHCODE_VALIDATION_EXCEPTION_MESSAGE = "Something went wrong while validating AuthCode";
        public static final String INVALID_VALIDATE_OTP_REQUEST = "validate OTP failed due to invalid request";
        public static final String RESEND_OTP_EXCEPTION_MESSAGE = "You have exceeded the max limit for resending OTP. Please try after sometime";
        public static final String RETRY_LIMIT_EXCEEDED_MESSAGE = "You have exceeded the max retries for sending OTP. Please try after sometime";
        public static final String OTP_LIMIT_EXCEEDED_MESSAGE = "You have exceeded the max limit for sending OTP. Please try after sometime";
        public static final String OTP_VALIDATE_LIMIT_EXCEEDED_MESSAGE = "You have reached OTP validation limit for this order. Please use any other paymode to continue.";
        public static final String USER_NOT_REGISTERED_MESSAGE = "This number is not registered with Paytm. Please use a registered number or sign up now on the Paytm app using this number.";
        public static final String SC_INTERNAL_SERVER_ERROR = "SC_INTERNAL_SERVER_ERROR";
        public static final String SC_ACCEPTED_STATUS_CODE = "202";
        public static final int MAX_RETRY_COUNT = 3;
        public static final int MAX_RESEND_OTP_COUNT = 3;
        public static final int MAX_OTP_COUNT_WITHIN_TIMEFRAME = 3;
        public static final String PAYMENT_STATUS = "PAYMENT_STATUS";
        public static final String MERCHANT_NAME = "MERCHANT_NAME";
        public static final String TXN_DATE = "TXN_DATE";
        public static final String PPBL_BALANCE_FAIL_MSG = "We are experiencing some issues while accessing your current account. Please retry this transaction after sometime.";
        public static final String PPBL_INSUFFICIENT_BALANCE = "Insufficient balance. Add money in your current account";

        // Native API constants
        public static final String OAUTH_PG_PLUS_EXPRESS_CLIENT_ID = "oauth.pgplus.express.client.id";
        public static final String OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY = "oauth.pgplus.express.client.secret.key";

        public static final String DEFAULT_WEB_LOGO_PATH = "images/web/merchant4/";
        public static final String DEFAULT_WAP_LOGO_PATH = "images/wap/merchantLow/";

        public static final String DEFAULT_WEB_LOGO_PATH_BANK = "images/web/bank/";
        public static final String DEFAULT_WAP_LOGO_PATH_BANK = "images/wap/bank/";
        public static final String DEFAULT_NATIVE_LOGO_PATH_BANK = "native/bank/";
        public static final String DEFAULT_NATIVE_LOGO_PATH_CARD = "native/card/";
        public static final String DEFAULT_NATIVE_LOGO_PATH_FAILURE = "native/failure/";
        public static final String DEFAULT_NATIVE_LOGO_SUFFIX = ".png";
        public static final String DEFAULT_TXN_TOKEN_EXPIRY_FOR_NATIVE_PAYMENT_IN_SECONDS = "900";
        public static final String DEFAULT_TXN_TOKEN_EXPIRY_FOR_ACCESS_TOKEN_IN_SECONDS = "1800";

        public static final String DEFAULT_FALLBACK_INST_ERROR_CODE = "FGW_BANK_FAIL_SPECIFIC_REASON";

        public static final int MIN_CARD_LENGTH = 10;
        public static final int MAX_CARD_LENGTH = 19;
        public static final int BIN_LENGTH = 6;
        public static final String PAYMETHOD_CATEGORY = "PAYMENT_MODE";
        public static final String AUTH_MODE_CATEGORY = "AUTH_MODE";
        public static final String USRPWD = "USRPWD";
        public static final String LINK_ID_PREFIX = "LI_";
        public static final String INVOICE_ID_PREFIX = "INV_";
        public static final String PAYMENT_BUTTONS_PREFIX = "PB_";
        public static final String PAYMENTS_BANK_CODE = "PPBL";
        public static final String PPBEX = "PPBEX";
        public static final String NATIVE_BASE_API_REGEX = ".*\\/api\\/v[1-9]\\/.*";
        public static final String RESELLER_ID_PREFIX = "resellerID-";

        public static final String ZERO_COST_EMI = "0CostEMI";
        public static final String ADD_CARD_THEME = "merchantLow|ccdc";
        public static final String OFFLINE_TXN_AMOUNT = "1";
        public static final String DEFAULT_WEBSITE_FF = "ffurl";
        public static final String NATIV_INITIATE_TRANSACTION_URL = "/theia/api/v1/initiateTransaction";
        public static final String NATIV_CUSTOM_INITIATE_TRANSACTION_URL = "/theia/api/v1/customInitTxn";
        public static final String NATIV_FETCH_PAYMENTOPTIONS_URL = "/theia/api/v1/fetchPaymentOptions";
        public static final String NATIV_FETCH_PAYMENTOPTIONS_URL_V2 = "/theia/api/v2/fetchPaymentOptions";
        public static final String NATIV_FETCH_PAYMENTOPTIONS_URL_V5 = "/theia/api/v5/fetchPaymentOptions";
        public static final String NATIV_FETCH_BIN_DETAIL_URL = "/theia/api/v1/fetchBinDetail";
        public static final String WALLET_QR_CODE = "/qrcode/v4/getQRCodeInfo";
        public static final String NATIV_PROCESS_TRANSACTION_URL = "/theia/api/v1/processTransaction";
        public static final String NATIVE_APP_INVOKE_URL = "/theia/api/v1/showPaymentPage";
        public static final String NATIVE_APP_INVOKE_URL_V2 = "/theia/api/v2/showPaymentPage";
        public static final String NATIVE_APP_INVOKE_URL_V3 = "/theia/api/v3/showPaymentPage";
        public static final String NATIVE_SHOW_LINK_PAYMENT_PAGE = "/theia/api/v1/showLinkPaymentPage";
        public static final String CUSTOM_PROCESS_TRANSACTION_URL = "/theia/customProcessTransaction";
        public static final String NATIVE_FETCH_PAYMODE_STATUS = "/theia/api/v1/fetchUserPaymentModeStatus";

        public static final String ENCODING_SCHEME_UTF_8 = "UTF-8";

        // RETRY NATIVE MESSAGES
        public static final String NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED = " is not allowed for this transaction, kindly use some other payment mode";
        public static final String NATIVE_ERROR_MESSAGE_PAYMENT_NOT_ALLOWED = " is not allowed for this transaction.";
        public static final String NATIVE_ERROR_MESSAGE_INSUFFICIENTBALANCE_MSG = "Insufficient Balance.";
        public static final String NATIVE_ERROR_MESSAGE_ISHYBRIDDISABLED_MSG = "This payment option is not allowed to pay with Paytm Wallet.";
        public static final String NATIVE_ERROR_MESSAGE_NOPAYMENTMODE = "Please choose a payment source to proceed further";

        public static final String ZERO = "0";
        public static final String UNDER_SCORE = "_";

        public static final String HYBRID_NOT_ALLOWED_MSG = " payment is not allowed with wallet, kindly unselect the wallet and try again";
        public static final String COD_MESSAGE = "Please pay <<TXN_AMOUNT>> to the courier agent at the time of delivery.";

        public static final String AUTO_LOGIN_EXPIRY_TIME = "auto.login.cookie.expiry";
        public static final String COOKIE_NOT_REQUIRED_CHARACTER_REGEX = "[^A-Za-z0-9]";

        // Agreement Autodebit Constants
        public static final String KEY = "AGREE_";
        public static final String SCOPE = "paytm";
        public static final String RESPONSE_TYPE = "code";
        public static final String THEME = "agreement";
        public static final String MERCHANT = "apple";
        public static final String THEIA_BASE_URL = "theia.base.url";
        public static final String THEIA_INTERNAL_BASE_URL = "theia.internal.base.url";
        public static final String THEIA_NEW_BASE_URL = "theia.new.base.url";

        // zest money
        public static final String ZEST_MONEY_SELECTED_TEXT = "zest.money.selected.text";

        public static final String ENHANCED_ALLOWED_CUSTID_LIST_KEY = "enhanced.allowed.custid.list";
        public static final String ENHANCED_WEB_THEME_ALLOWED_CUSTID_LIST_KEY = "enhanced.web.theme.allowed.midcustid.list";
        public static final String ENHANCED_WEB_THEME_BLOCKED_CUSTID_LIST_KEY = "enhanced.web.theme.blocked.midcustid.list";

        public static final String ZEST_MONEY_MINAMOUNT_TEXT = "zest.money.minamount";
        public static final String ZEST_MONEY_MAXAMOUNT_TEXT = "zest.money.maxamount";

        public static final String ALL = "ALL";
        public static final String NONE = "NONE";

        public static final String AggMID = "aggMid";

        public static final String TRANSACTION_STATUS_TIMEOUT = "transactionstatus.async.timeout";
        public static final String NATIVE_HYBRID_DISABLED_PAYMODES = "native.hybrid.disabled.paymodes";
        public static final String AGGREGATOR_MID_MERCHANTKEY_MAPPING = "native.aggregator.mid.merchantKey.mapping";

        public static final String BANKS = "BANKS";

        public static final String DEFAULT_WEBSITE = "DEFAULT";

        public static final String ENABLE_HTML_RESPONSE_LOGS_KEY = "enable.html.response.logs";
        public static final String PAYMENT_COUNT = "paymentCount";
        public static final String HALT_PAYMENT_REQUEST = "haltPaymentRequest";
        public static final String DUPLICATE_PAYMENT = "Payment already in progress";
        public static final String DUPLICATE_PAYMENT_REDIS_TTL = "duplicate.payment.redis.ttl";
        public static final String DUPLICATE_PAYMENT_REQUEST_CHECK = "duplicate.payment.check";
        public static final String REQUEST_METHOD_OPTIONS = "OPTIONS";
        public static final String NPCI_KEY = "npci.key";
        public static final String DEEPLINK_SIGN_PROPERT = "flag.deeplink.sign";
        public static final String MODE = "05";
        public static final String ORGID = "000000";
        public static final String DEFAULT_PURPOSE = "00";
        public static final String UPI_MCC_PREFIX = "upi.purpose.mcc.";
        public static final String REDIS_OPTIMIZED_FLOW = "redis.optimized.flow";
        public static final String INST_ERROR_CODE = "instErrorCode";

        /* wix merchant status */
        public static final String PAYMENT_ERROR_CODE = "paymentErrorCode";
        public static final String SUBSCRIPTION_TYPE = "subscriptionType";
        public static final int FIRST_ELEMENT = 0;

        public static final String PG = "pg";
        public static final String DIRECT_BANK_PAGE_INVALID_OTP = "directBankPageInvalidOtp";
        public static final String DIRECT_BANK_SUBMIT_COUNT_PROP = "native.directBankPage.submitRetryCount";
        public static final String DIRECT_BANK_RESEND_COUNT_PROP = "native.directBankPage.resendOtpRetryCount";
        public static final String MAX_OTP_RETRY_COUNT = "maxOtpRetryCount";
        public static final String MAX_OTP_RESEND_COUNT = "maxOtpResendCount";
        public static final String DIRECT_BANK_PAGE_RETRY = "directBankPageRetry";
        public static final String DIRECT_BANK_PAGE_SUBMIT_REQUEST = "directBankPageSubmitRequest";
        public static final String MIN_LENGTH_CUSTOMER_COMMENT = "min.length.customerComment";
        public static final String MAX_LENGTH_CUSTOMER_COMMENT = "max.length.customerComment";
        public static final String ENHANCE_DYNAMIC_QR_PAGE_TIMEOUT = "enhance.dynamicqr.page.timeout";
        public static final String ENHANCE_DYNAMIC_UPI_QR_DISPLAY_MESSAGE = "dynamic.upi.qr.display.message";
        public static final String SUBSCRIPTION_UPI_QR_DISPLAY_MESSAGE = "subscription.upi.qr.display.message";
        public static final String ENHANCE_DYNAMIC_PAYTM_QR_DISPLAY_MESSAGE = "dynamic.paytm.qr.display.message";
        public static final String OLD_PG_BASE_URL_SKIP = "old.pg.base.url.skip";
        public static final String NEW_PG_BASE_URL = "new.pg.base.url";

        public static final String MERCHANT_STORE_FRONT_URL = "merchant.gift.voucher.storeFront.url";
        public static final String CHECKOUT_JS_STATIC_CALLBACK_URL = "checkout.js.static.callback.url";
        public static final String WORKFLOW = "workFlow";
        public static final String ENHANCE_PUSH_APP_DATA_ENCODED = "enhance.push.app.data.encoded";
        public static final String DISABLE_MANDATORY_LOGIN_IN_LINK_BASED_MF_ST_FLOW = "disable.mandatory.login.MForST.flow";
        public static final String THEME_PREFERENCE_DETAILS = "THEME_PREFERENCE_DETAILS";
        public static final String PAY_WITH_PAYTM = "PAY_WITH_PAYTM";
        public static final String PAYTM_PAYMODE_PROMO_SUPPORTED = "native.promo.paytmPaymode.supported";

        public static final String VERSION = "version";

        public static final String INTERNATIONAL_PAYMENT_KEY = "supportCountries";
        public static final String MINIMUM_AMOUNT_FOR_NB_AS_PAYMODE = "minimum.amount.for.NB.as.paymode";

        public static final String CARD_META_DATA_CACHE_TIME = "cardMetaDataCacheTime";

        public static final String FAILURE_IMAGE_NAME = "FAILURELOGO.png";
        public static final String ONUS = "ONUS";
        public static final String ONUS_SMALL = "onus";
        public static final String OFFLINE_SMALL = "offline";
        public static final String PASSCODE = "passcode";
        public static final String WALLET_TXN = "wallet_txn";

        public static final String MID_REDIRECT_TO_SHOW_PAYMENT_PAGE_WALLET_BALANCE_NOT_ENOUGH = "mid.redirectToShowPaymentPage.walletBalanceNotEnough";
        public static final String WALLET_AMOUNT = "WALLET_AMOUNT";
        public static final String ALLOWED_REDIRECT_SHOW_PAYMENT_PAGE_WALLET_BALANCE_NOT_ENOUGH = "redirectToShowPaymentPage.walletBalanceNotEnough";
        public static final String MID_BLOCKED_CREATE_ORDER_INITIATE_TXN_API = "mid.blocked.createOrder.initiateTxnApi";
        public static final String MID_ALLOWED_FORCED_CHANNEL_ID = "mid.allowed.forced.channel.id";
        public static final String LINK_CONSULT_ENABLE = "link.consult.enable";
        public static final String EXEMPT_MID_LIST_FROM_CLOSE_ORDER = "exemptMidListFromCloseOrder";
        public static final String EXEMPT_MID_LIST_FROM_CLOSE_ORDER_OLD_API = "exemptMidListFromCloseOrderOldApi";
        public static final String EXEMPT_MID_LIST_FROM_CLOSE_ORDER_WITH_PENDING_STATUS = "exemptMidListFromCloseOrderWithPendingStatus";
        public static final String DIRECT_BANK_FORM_FOR_MERCHANT_OFFLINE_FLOW = "theia.offline.enableNativePlusDirectForms";
        public static final String APPLY_PROMO_URL_V2 = "/theia/api/v2/applyPromo";
        public static final String THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN = "theia.FailCancelTransactionWithOutToken";
        public static final String THEIA_SET_TERMINAL_TYPE_IN_DYNAMIC_QR_FAST_FORWARD = "theia.setTerminalTypeInDynamicQRFlow.fastforward";
        public static final String THEIA_UPI_CHANNEL_FILTERING = "theia.upi.channel.filtering";
        public static final String THEIA_ADD_ESCAPE_CHARACTER_IN_FINAL_HTML_RESPONSE = "theia.addEscapeCharacterInFinalRespone";
        public static final String THEIA_DISABLE_SENDING_SPLIT_DATA_IN_EXTEND_INFO = "theia.disableSendingSplitDataInExtendInfo";
        public static final String VPA_VALIDATION_LIMIT = "vpa.validation.limit";

        // mid based ff4j flag
        public static final String THEIA_AUTO_APP_INVOKE_PHASE2 = "theia.autoAppInvokePhase2";
        public static final String THEIA_DISABLE_CC_DC_PAYMODES_SUBSCRIPTION = "theia.disableCcDcPaymodesInSubscription";
        public static final String THEIA_ENABLE_CC_DC_PAYMODES_SUBSCRIPTION = "theia.enableCcDcPaymodesInSubscription";

        public static final String THEIA__CHCECKOUT_POLLING_FOR_INTENT = "theia.checkout.pollingForIntent";

        public static final String THEIA_ENABLE_PROMOTIONS_IN_PCF = "theia.enable.promotionsInPcf";
        public static final String THEIA_UPI_APPS_PAYMODE_DISABLED = "theia.upiAppsPayModeDisabled";

        /**
         * App invoke constants
         */
        public static final String APP_INVOKE_URL_CONFIG = "native.appInvoke.url";
        public static final String APP_INVOKE_CALLBACK_URL = "APP_INVOKE_CALLBACK_URL";
        public static final String APP_INVOKE_URL_CONFIG_V3 = "native.appInvoke.url.v3";

        public static final String REPLACE_DEEPLINK_REQUIRED = "replace.deeplink.required";
        public static final String GOOGLE_PAY_DEEPLINK_CONSTANT = "gpay";
        public static final String PHONEPE_DEEPLINK_CONSTANT = "phonepe";
        public static final String PAYTM_DEEPLINK_CONSTANT = "paytm";
        public static final String QR_DETAIL = "qrDetail";
        public static final String BANK_TRANSFER = "BANK_TRANSFER";
        public static final String FAST_FORWARD_API_DUPLICATE_CHECK = "FAST_FORWARD_API_DUPLICATE_CHECK";
        public static final String MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL = "MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL";
        public static final String CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE = "CACHE_SEARCH_PAYMENT_OFFERS_RESPONSE";
        public static final String UI_SERVER_CONFIG = "ui.server.config";

        public static final String DUMMY_ORDER_ID = "dummyOrderId";
        public static final String DUMMY_MERCHANT_ID = "dummyMerchantId";

        public static final String DCC_PAGE_DATA = "dccPageData";
        public static final String NATIVE_REQUEST_MAP_DCC = "NativeRequestMapDcc";
        public static final String AKAMAI_IP_ADDRESS_HEADER = "true-client-ip";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";

        public static final String FPO_DISBALE_PAYMODES = "fpo.disable.paymodes";
        public static final String FPO_DISBALE_PAYMODE_CHANNEL_PREFIX = "fpo.disable.";
        public static final String FPO_DISBALE_PAYMODE_CHANNEL_SUFFIX = ".channels";
        public static final String FPO_DISBALE_PAYMODES_ON_MERCHANT_TYPE = "fpo.disable.paymodes.on.merchant.type";

        public static final String EDC_MERCHANT_VELOCITY_LIMIT_BREACH = "EDC_MERCHANT_VELOCITY_LIMIT_BREACH";
        public static final String LINK_PAYMENT_ONLY_MERCHANT = "LINK_PAYMENT_ONLY_MERCHANT";

        public static final String ONLINE_MERCHANT = "online";
        public static final String OFFLINE_MERCHANT = "offline";
        public static final String PPBL_OFFLINE_MERCHANT = "PPBL_OFFLINE";
        public static final String PPBL_ONLINE_MERCHANT = "PPBL_ONLINE";
        public static final String MERCHANT_SOLUTION_TYPE = "merchantSolutionType";
        public static final String MERCHANT_PREFERENCE = "merchantPreference";

        public static final String PAYMODE = "paymode";
        public static final String MERCHANT_TYPE = "merchantType";
        public static final String PAYMODE_CHANNELS = "paymodeChannels";
        public static final String MERCHANT_LIMIT_TYPE = "merchantLimitType";
        public static final String FPO_DISBALE_PAYMODES_GENERIC = "fpo.disable.paymodes.generic";
        public static final String FPO_DISABLE_LOCATION_LINKS_MERCHANT_LIMIT = "fpo.disable.location.links";
        public static final String ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED = "ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED";
        public static final String DYNAMIC_WRAPPER_PATH_URI = "/dynamic-wrapper";
        public static final String POSTPAID_ONLY_MSG_BEFORE_LOGIN = "postpaid.only.message.before.login";
        public static final String POSTPAID_ONLY_MSG_AFTER_LOGIN = "postpaid.only.message.after.login";

        public static final String idempotentTransaction = "idempotentTransaction";
        public static final String LOG_API_REQUEST_PARAMS_IN_FILTER = "filter.logs";
        public static final String GET_QUERY_PARAM_FROM_QUERY_STRING_ENABLE = "query.params.from.query.string.enable";

        public static final String UPI_COLLECT_LIMIT_UNVERIFIED = "theia.upiCollectLimitUnverified";
        public static final String AOA_SUBSCRIPTION_PAYMODES = "aoa.subscription.paymodes";
        public static final String TILDE = "~";
        public static final String SUBS_PREFIX = "SUBS_";
        public static final String SUBS_JSON_FPO_RESPONSE = "fpoJsonResponse";
        public static final String MOBILE_NUMBER_UNMASK_PREFIX_LENGTH = "mobile.unmask.prefix.length";
        public static final String MOBILE_NUMBER_UNMASK_SUFFIX_LENGTH = "mobile.unmask.suffix.length";
        public static final String MOBILE_NUMBER_MASK_CHARACTER = "mobile.mask.character";

        public static final String TIN = "TIN";
        // public static final String PAR = "PAR";
        public static final String GCIN = "GCIN";
        public static final String WALLET_ACCOUNT_INACTIVE_LIMIT_FREEZE_MESSAGE = "wallet.account.inactive.limit.freeze.message";
        public static final String WALLET_LIMIT_REJECT_MESSAGE = "wallet.limit.reject.message";
        public static final String DEFAULT_WALLET_LIMIT_REJECT_MESSAGE = "This option is not available for you.";
        public static final String INVALID_VIRTUAL_ADDRESS_RESPONSE_CODE = "37";
        public static final String PP_TWO_FACTOR_AUTH_THRESHOLD_VALUE = "theia.pp.2fa.threshold.value";
        public static final String AOA_WORKFLOW = "AOA";

        public static final String MERCHANT_STATIC_CONFIG_JWT_SECRET_KEY = "merchantStaticConfig.jwt.secret.key";
        public static final String FETCH_EMI_DETAIL_JWT_SECRET_KEY_PREFIX = "fetchEmiDetail.jwt.secret.key.";

        // DC Emi eligibility banks added
        public static final String DC_EMI_ELIGIBILITY_DEFAULT_TRUE = "dc.emi.eligibility.default.true";
        public static final String DC_EMI_ELIGIBILITY_CHECK_DB = "dc.emi.eligibility.check.db";
        public static final String DC_EMI_ELIGIBILITY_CHECK_MOBILE_NUMBER = "dc.emi.eligibility.check.mobilenumber";
        public static final String DC_EMI_ELIGIBILITY_DEFAULT_FALSE = "dc.emi.eligibility.default.false";

        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String USER_LBS_LATITUDE = "userLBSLatitude";
        public static final String USER_LBS_LONGITUDE = "userLBSLongitude";

        public static final String RESULT_INFO = "resultInfo";
        public static final String RESULT_STATUS = "resultStatus";

        // UPI LITE
        public static final String UPI_LITE_PRIORITY = "upi.lite.priority";

        // masking Parameter in request-reposne log
        public static final String PARAMETER_TO_BE_MASKED_IN_REQUEST = "parameter.to.be.masked.in.request";
    }

    public static final class FF4J {
        public static final String INVALIDATE_REDIS_KEY_AND_FIELDS_ENHANCED_NATIVE = "theia-srv.EnhancedNative.Invalidate.Redis.KeyAndFields.OnTerminalState";
        public static final String INVALIDATE_REDIS_KEY_AND_FIELDS_NATIVE = "theia-srv.Native.Invalidate.Redis.KeyAndFields.OnTerminalState";
        public static final String INVALIDATE_REDIS_KEY_AND_FIELDS_NATIVE_JSON_REQUEST = "theia-srv.NativeJsonRequest.Invalidate.Redis.KeyAndFields.OnTerminalState";
        public static final String CREATE_MANDATE_CALLBACK_USING_STATUS_API = "theia-srv.Subscription.Mandate.Callback.Status.Api";
        public static final String THEIA_USE_V1_TXN_STATUS_FOR_UPI_POLLING = "theia.Use.V1.Txn.Status.For.UPI.Polling";
        public static final String RETURN_BIN_DISPLAY_BANK_NAME_IN_FETCHBINDETAIL = "theia-srv.ReturnBinDisplayBankNameInFetchBinDetail";
        public static final String NATIVE_JSON_REQUEST_REDIRECT_WALLET_BALANCE_NOT_ENOUGH = "theia-srv.NativeJsonRequest.mid.redirectToShowPaymentPage.walletBalanceNotEnough";
        public static final String REF_ID_REDIS_DEPENDENCY_REMOVAL_NEW_PATTERN = "theia-srv.refId.newPattern.redis.dependency.removal";
        public static final String REF_ID_REDIS_DEPENDENCY_REMOVAL_OLD_PATTERN = "theia-srv.refId.oldPattern.redis.dependency.removal";
        public static final String THEIA_UPI_PAY_APP_SUPPORTED = "theia.UpiAppsPayModeSupported";
        public static final String THEIA_LOCAL_STORAGE_ALLOWED_ON_MERCHANT = "theia.localStorageForLastPayModeOnMerchantAllowed";
        public static final String THEIA_ENHANCED_RETURN_DISABLED_CHANNELS = "theia.enhanced.return.disabled.channels";
        public static final String THEIA_CHECKOUT_FORWARD_HTML_RENDER_ALLOWED = "theia.checkout.forward.html.render.allowed";
        public static final String THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS = "theia.staticRedis.migration.sessionRedis";
        public static final String THEIA_GET_MERCHANT_API_URL_INFO = "theia-srv.getMerchantApiUrlInfo";
        public static final String APPLY_PROMO_SEND_RESPONSE_TNC_URL = "theia.applyPromoSendResponseTncUrl";
        public static final String THEIA_ENABLE_PROMO_USER_SEGMENTATION = "theia.enablePromoUserSegmentation";
        public static final String THEIA_USE_INVESTMENT_AS_FUNDING_SOURCE = "theia.useInvestmentAsFundingSource";
        public static final String THEIA_USE_INVESTMENT_AS_FUNDING_SOURCE_OFF_US = "theia.OffUs.useInvestmentAsFundingSource";
        public static final String REMOVE_HDFC_EMI_IN_RESPONSE = "theia.removeHDFCEMIFromResponse.fetchQrPaymentDetails";
        public static final String REMOVE_ADDNPAY_CC_PAYMODE = "theia.removeCCFromADDNPAYPaymodes";
        public static final String THEIA_ENABLE_ADD_MONEY_WALLET_LIMIT = "theia.enableAddMoneyWalletLimit";
        public static final String THEIA_DISABLE_RETRY_FOR_FETCHING_BANKFORM = "theia.disableRetryForFetchingBankForm";
        public static final String THEIA_ENHANCE_LOGIN_VIA_COOKIE_ENABLED = "theia.enhanceLoginViaCookieEnabled";
        public static final String THEIA_LOYALTY_POINTS_MIGRATION = "theia.loyaltyPointsMigration";
        public static final String THEIA_AOASUBSCRIPTIONS_MIDLISTFORAOATOPGCONVERSION = "theia.aoaSubscriptions.MidListForAOAToPGConversion";
        public static final String THEIA_LOYALTY_POINTS_CONSULT = "theia.loyaltyPointsConsult";
        public static final String THEIA_SUBSCRIPTION_QR_ENABLED = "theia.subscriptionQrEnabled";
        public static final String THEIA_CHARGE_AMOUNT_IN_PTC_RESPONSE = "theia.chargeAmountInPtcResponse";

        // UI-Microservice
        public static final String FEATURE_UI_MICROSERVICE_ENHANCED = "theia.uimicroservice.enhancedflow.feature";
        public static final String FEATURE_UI_MICROSERVICE_RISK = "theia.uimicroservice.riskflow.feature";
        public static final String FEATURE_UI_MICROSERVICE_GVCONSENT = "theia.uimicroservice.gvconsentflow.feature";
        public static final String FEATURE_THEIA_SHOW_ONLY_WALLET = "theia.enhance.show.only.wallet";
        public static final String FEATURE_THEIA_APP_INVOKE_AS_COLLECT = "theia.enhance.app.invoke.as.collect";
        public static final String FEATURE_UI_SERVER_CONFIG = "theia.ui.server.config";
        public static final String FEATURE_GOODSINFO_AMOUNT_RUPAY_TO_PAISE = "theia.goodsinfo.amount.rupaytopaise";
        public static final String FEATURE_THEIA_CHANGE_REDEMPTION_TYPE = "theia.changeRedemptionType";
        public static final String THEIA_V3_APP_INVOKE_SUPPORT = "theia.v3.appinvoke.support.feature";

        public static final String THEIA_BRAND_EMI_FEATURE = "theia.brandEmi.feature";

        public static final String USE_PROMO_V1 = "usePromoV1";
        public static final String EXPLICIT_JSON_CONVERSION_KAFKA_PAYLOAD = "theia.explicit.json.conversion.kafkapayload";
        // THis below ff4j is used to block pospaid in FPO response for
        // apps(appINvoke=true) in case param :
        // addNPayOnPostpaidSupported comes false in fpo request body
        public static final String BLOCK_POSPAID_IN_ADDDMONEYPAYMODES = "theia.blockPospaidInAddMoneyPayModes";
        public static final String ENABLE_EMI_PAYMODE_ON_COFT_CARD = "theia.enableEmiPaymodeOnCoft";
        public static final String ENABLE_ONECLICK_TXN_ON_COFT_CARD = "theia.enableOnclickTxnOnCoft";

        public static final String ADDNPAY_UPI_ACC_REF_ID_ALLOWED = "theia.addnpay.allow.upiAccRefId";
        public static final String FEATURE_ENABLE_UPI_COLLECT_ON_ADDNPAY = "theia.enableUPICollectOnADDNPAY";
        public static final String FEATURE_ENABLE_PAYMODE_FILTER_ON_BOSS = "theia.enablePayModeFilterOnBoss";
        public static final String DISABLE_2FA_FOR_SUBSONWALLET_IN_APPHOSTEDFLOW = "disable.2fa.forSubsOnWallet.InApphostedFlow";
        public static final String ENABLE_CACHING_FOR_FPO_IN_FETCH_BIN_DETAIL = "theia.enableCachingCashierInfoResOnMIDInFetchBinDetailApi";
        public static final String ENABLE_PPBL_NB_CHANNEL_ORDERING = "theia.enablePpblNBChannelOrderingOnTop";
        public static final String BLOCK_USER_POSTPAID_STATUS_CHECK_ON_MID = "theia.blockUserPostpaidStatusCheckOnMID";
        public static final String CONVERT_PG_TO_AOA_MID_FOR_SUBS_SAVECARD_TXN = "theia.convertPgToAoaMidForSubsSaveCardTxn";
        public static final String REDIRECT_TO_NEW_RENEW_API = "theia.redirectToNewRenewAPI";
        public static final String SET_MERCHANT_ACCEPT_FOR_SUPPORTED_PAYMODE = "theia.setMerchantAcceptForSupportedPaymode";
        public static final String SUPPORT_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW = "theia.supportAddMoneyPayOptionInOfflineFlow";
        public static final String ENABLE_ADDANDPAY_ONE_RUPEE = "theia.enable.addnpay.one.rupee";

        public static final String THEIA_SUBSCRIPTION_ENABLE_COFT = "theia.enableSubscriptionsOnCoft";
        public static final String ENABLE_GCIN_ON_COFT_PROMO = "theia.enableGcinOnCoftPromo";
        public static final String ENABLE_GCIN_ON_COFT_WALLET = "theia.enableGcinOnCoftWallet";
        public static final String ENABLE_GCIN_ON_COFT_RISK = "theia.enableGcinOnCoftRisk";
        public static final String THEIA_SUBSCRIPTION_DISABLE_COFT = "theia.disableSubscriptionOnCoft";

        // For sending encrypted response in Direct Otp flow and Close order
        public static final String THEIA_ENCRYPTED_RESPONSE_TO_JSON = "theia.encryptedResponseToJson";
        public static final String COFT_EMI_ELIGIBLE_ISSUERS_DC = "coft.emi.eligible.issuers.dc";
        public static final String COFT_EMI_ELIGIBLE_ISSUERS_CC = "coft.emi.eligible.issuers.cc";

        public static final String SIX_DIGIT_BIN_LOGGING = "theia.sixDigitBinLogging";
        public static final String ENABLE_TPV_FOR_ALL_REQUEST_TYPES = "theia.enableTPVForAllRequestTypes";

        // ff4j flags for validation
        public static final String THEIA_VALIDATE_MID_ENABLE = "theia.validate.mid.enable";
        public static final String THEIA_VALIDATE_ORDER_ID_ENABLE = "theia.validate.orderId.enable";
        public static final String THEIA_VALIDATE_TOKEN_ENABLE = "theia.validate.token.enable";
        public static final String THEIA_VALIDATE_KYC_MID_ENABLE = "theia.validate.kycMid.enable";
        public static final String THEIA_VALIDATE_TRANSACTION_ID_ENABLE = "theia.validate.transactionId.enable";
        public static final String THEIA_VALIDATE_RESPMSG_ENABLE = "theia.validate.respMsg.enable";
        public static final String THEIA_VALIDATE_HEADER_WORKFLOW_ENABLE = "theia.validate.headerWorkflow.enable";
        public static final String THEIA_VALIDATE_TXN_TOKEN_ENABLE = "theia.validate.txnToken.enable";

        public static final String DISABLE_PARALLEL_EXEC_BULK_APPLY = "theia.disable.parallel.exec.bulk.apply";
        public static final String COTP_REDIRECTION = "theia.cotp.redirection";
        public static final String ENABLE_ZEST_LOGIC = "theia.enableZestLogic";

        public static final String UPI_ON_CC_BLACKLIST = "theia.upi.on.cc.blacklist";
        public static final String UPI_ON_CC_ONUS = "theia.upi.on.cc.onus";
        public static final String UPI_ON_CC_GLOBAL = "theia.upi.on.cc.global";
        public static final String UPI_CC_FEE_FACTOR_ENABLED = "theia.upiCcFeeFactorEnabled";
        public static final String disableLiteDynamicOrdering = "disableUpiLiteDynamicOrdering";
        public static final String UPI_CC_ADDNPAY_ENABLED = "theia.upiCcAddNPayEnabled";
        public static final String UPI_CC_SUBS_ENABLED = "theia.upiCcSubsEnabled";
        public static final String UPI_CC_NATIVE_ADDMONEY_ENABLED = "theia.upiCcNativeAddMoneyEnabled";
        public static final String UPI_CC_ALLOWED_ON_KEY_ENABLED = "theia.upiCcAllowedOnKeyEnabled";

        public static final String BFF_LAYER_ENABLED = "theia.bff.layer.enabled";

        public static final String THEIA_ENABLE_LIMIT_CONVERSION = "theia.enable.limit.conversion";
        public static final String BANK_FORM_OPTIMIZED_FLOW = "theia.bankFormOptimizedFlow";
        public static final String THEIA_WAIT_TIME_FOR_BANK_FORM_OPTIMIZATION = "theia.waitTimeForBankFormOptimization";
        public static final String THEIA_CHECK_PAYMENT_STATUS_IN_FORM_OPTIMIZED_FLOW = "theia.checkPaymentStatusInFormOptimizedFlow";
        public static final String THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR = "theia.disable.setting.pushDataToDynamicQR";
        public static final String THEIA_DISABLE_PAYER_ACCOUNT_NO_CHECK_ON_WALLET = "theia.disablePayerAccNoCheckOnWalletPayMode";

        public static final String THEIA_CHECK_XSS_BLOCKED_PATHS = "theia.checkXSSBlockedPaths";
        public static final String THEIA_PARAMETER_MASKING_IN_REQUEST = "theia.parameter.masking.in.request";
        public static final String THEIA_ENABLE_LINK_FLOW_ON_DQR = "theia.enable.link.flow.on.dqr";
        public static final String DISABLE_CARDS_FOR_STATIC_QR_FQR = "theia.disableCardsForStaticQR.FQR";
        public static final String DISABLE_CARDS_FOR_STATIC_QR_FPO = "theia.disableCardsForStaticQR.FPO";

    }

    public static final class EDCEmiSubvention {
        public static final String BRAND_NAME = "brandName";
        public static final String SUBVENTION = "subvention";
        public static final String CATEGORY_NAME = "categoryName";
        public static final String SERAIL_NO = "serialNo";
        public static final String MODEL_NAME = "modelName";
        public static final String SUBVENTION_AMOUNT = "subventionAmount";
        public static final String EMI_TYPE = "emiType";
        public static final String BRAND_EMI_TYPE = "brandEmi";
        public static final String BANK_EMI_TYPE = "bankEmi";
        public static final String ADDITIONAL_CASH_BACK = "additionalCashBack";
        public static final String ADDITIONAL_CASH_BACK_AMOUNT = "additionalCashBackAmount";
        public static final String DEFAULT_BRAND_CODE = "DEFAULT";
    }

    public static final class TxnType {
        public static final String ACQUIRING = "ACQUIRING";
        public static final String FUND = "FUND";
    }

    public static final class SubscriptionResponseConstant {
        public static final String TXN_ID = "TXNID";
        public static final String ORDER_ID = "ORDERID";
        public static final String TXN_AMOUNT = "TXNAMOUNT";
        public static final String STATUS = "STATUS";
        public static final String RESPCODE = "RESPCODE";
        public static final String RESPMSG = "RESPMSG";
        public static final String MID = "MID";
        public static final String SUBS_ID = "SUBS_ID";
    }

    public static final class ResponseConstants {
        public static final String MERCHANT_LIMIT_BREACHED = "Merchant limit is breached";
        public static final String ORDER_ID = "ORDERID";
        public static final String M_ID = "MID";
        public static final String TRANSACTION_ID = "TXNID";
        public static final String AMOUNT = "TXNAMOUNT";
        public static final String PAYMENT_MODE = "PAYMENTMODE";
        public static final String CURRENCY = "CURRENCY";
        public static final String TRANSACTION_DATE = "TXNDATE";
        public static final String CUSTOMER_ID = "CUST_ID";
        public static final String RESPONSE_CODE = "RESPCODE";
        public static final String RESPONSE_MSG = "RESPMSG";
        // public static final String RESPONSE_MSG_REGIONAL = "RESPMSGREGIONAL";
        public static final String SUBS_ID = "SUBS_ID";
        public static final String MERCH_UNQ_REF = "MERC_UNQ_REF";
        public static final String UDF_1 = "UDF_1";
        public static final String UDF_2 = "UDF_2";
        public static final String UDF_3 = "UDF_3";
        public static final String ADDITIONAL_INFO = "ADDITIONAL_INFO";
        public static final String CHECKSUM = "CHECKSUMHASH";
        public static final String BANKTXNID = "BANKTXNID";
        public static final String BIN = "BIN";
        public static final String LASTFOURDIGITS = "LASTFOURDIGITS";
        public static final String GATEWAY = "GATEWAYNAME";
        public static final String GATEWAYNAME = "gatewayName";
        public static final String RESPONSE_KEY_POSTPARAMS = "{posthiddenparams}";
        public static final String RESPONSE_KEY_RESPURL = "{url}";
        public static final String RESPONSE_KEY_METHODTYPE = "{action_method}";
        public static final String RESPONSE_KEY_oneClickInfo = "{oneclickinfo}";
        public static final String RESPONSE_KEY_retryInfo = "{retryInfo}";
        public static final String RESPONSE_BANK_HTML_KEY = "bankHTML";
        public static final String STATUS = "STATUS";
        public static final String CHILDTXNLIST = "CHILDTXNLIST";
        public static final String BANK_RESULT_INFO = "BANKRESULTINFO";
        public static final String BANKNAME = "BANKNAME";
        public static final String PROMO_CAMP_ID = "PROMO_CAMP_ID";
        public static final String PROMO_STATUS = "PROMO_STATUS";
        public static final String PROMO_RESPCODE = "PROMO_RESPCODE";
        public static final String INAVLID_PROMOCODE = "invalidPromoCode";
        public static final String MASKED_CARD_NO = "maskedCardNo";
        public static final String CARD_INDEX_NO = "cardIndexNo";
        public static final String CARD_HASH = "cardHash";
        public static final String ENC_PARAMS = "ENC_DATA";
        public static final String ADDITIONAL_PARAM = "ADDITIONAL_PARAM";
        public static final String PRN = "PRN";
        public static final String TXN_TOKEN = "TXN_TOKEN";
        public static final String JWT_TOKEN = "JWT_TOKEN";
        public static final String VPA = "VPA";
        public static final String ACCOUNTNUMBERVALIDATED = "ACCNUMVERSUCCESS";
        public static final String VIRTUAL_PAYMENT_ADDR = "virtualPaymentAddr";
        public static final String UPI_ACCEPTED = "UPI_ACCEPTED";
        public static final String TRANS_ID = "transId";
        public static final String CASHIER_REQUEST_ID = "cashierRequestId";
        public static final String ORDERID = "orderId";
        public static final String DEEP_LINK = "deepLink";
        public static final String ERROR_CODE = "errorCode";
        public static final String MERCHANT_ID = "merchantId";
        public static final String UPI_STATUS_TIMEOUT = "STATUS_TIMEOUT";
        public static final String UPI_STATUS_INTERVAL = "STATUS_INTERVAL";
        public static final String UPI_STATUS_TIMEOUT_DISPLAYFIELD = "statusTimeout";
        public static final String UPI_STATUS_INTERVAL_DISPLAYFIELD = "statusInterval";
        public static final String UPI_STATUS_URL = "upiStatusUrl";
        public static final String PAY_MODE = "paymentMode";
        public static final String LINK_TYPE = "LINK_TYPE";
        public static final String CHARGE_AMOUNT = "CHARGEAMOUNT";
        public static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";
        public static final String PAY_RESULT_EXTEND_INFO = "payResultExtendInfo";
        public static final String AGGREGATOR_MID = "aggregatorMid";
        public static final String RETRY_INFO = "RETRYINFO";
        public static final String PAYMENT_RETRY_INFO = "paymentRetryInfo";
        public static final String CARD_SCHEME = "cardScheme";
        public static final String PCF_DETAILS = "PCFDetails";
        public static final String COUNTRY_CODE = "COUNTRY_CODE";
        public static final String COUNTRY_CODE_INDIA = "india";

        public static final String PREPAID_CARD = "PREPAIDCARD";

        // Email Failure Code
        public static final String INVOIVE_FAILURE_MESSAGE = "SYSTEM_ERROR";
        public static final String CHECKSUM_FAILURE_MESSAGE = "CHECKSUM_VALIDATION_FAILURE";
        public static final String INVOIVE_FAILURE_CODE = "15000";
        public static final String SUCCESS = "success";
        public static final String FAIL = "fail";
        public static final String MERCHANT_VELOCITY_LIMIT_BREACH_AUTODEBIT_MESSAGE = "Merchant velocity limit breached";
        public static final String USER_EMAIL = "USER_EMAIL";
        public static final String USER_MOBILE = "USER_MOBILE";
        public static final String TLS_WARNING_MESSAGE = "TLS_WARNING_MESSAGE";
        public static final String ADDNPAY_TLS_WARNING_MESSAGE = "ADDNPAY_TLS_WARNING_MESSAGE";
        public static final String CARD_DETAILS_NOT_FOUND = "Card details do not exist for given param";
        public static final String INVALID_REPONSE_FROM_SERVICE = "Invalid Response From Service";
        public static final String CARD_NOT_SUPPORTED_MESSAGE = "This card is not supported. Please use another card.";

        public static final String CARD_NOT_ALLOWD_ON_MERCHANT = "Payment with this card is not allowed on this merchant.";
        public static final String CARD_BLOCKED_MESSAGE = "This card does not support online or POS payments. Please use another card or payment option.";
        public static final String SUBSCRIPTION_PENDING_MESSAGE = "We are unable to get the final status from your bank. Please check after some time.";

        // Mandate Response Codes
        public static final String RESPONSE_STATUS = "RESPONSE_STATUS";
        public static final String IS_ACCEPTED = "IS_ACCEPTED";
        public static final String ACCEPTED_REF_NO = "ACCEPTED_REF_NO";
        public static final String REJECTED_BY = "REJECTED_BY";
        public static final String MERCHANT_CUST_ID = "MERCHANT_CUST_ID";
        public static final String MANDATE_TYPE = "MANDATE_TYPE";
        public static final String E_MANDATE = "E-mandate";

        public static final String SAVED_CARD_ID = "SAVED_CARD_ID";
        public static final String RISK_INFO = "riskInfo";

        public static final String VIRTUAL_PAYMENT_ADDRESS = "VIRTUAL_PAYMENT_ADDRESS";

        public static final String AUTH_SERVICE_OTP_LIMIT_BREACH_CODE = "708";

        public static final String TXN_PAID_TIME = "TXNPAIDTIME";

        /*
         * wix transaction status
         */
        public static final String API_RESPONSE = "apiResponse";
        public static final String SYSTEM_RESPCODE = "systemResponseCode";

        public static final String V1_PTC = "V1_PTC";

        public static final class ResponseCodes {
            public static final String USER_CLOSED_RESPONSE_CODE = "141";
            public static final String PAGE_OPEN_RESPONSE_CODE = "810";
            public static final String SUCCESS_RESPONSE_CODE = "01";
            public static final String CHECKSUM_FAILURE_CODE = "330";
            public static final String POLL_PAGE_ABANDONED_CODE = "294";
            public static final String SC_INTERNAL_SERVER_ERROR = "500";
            public static final String MERCHANT_VELOCITY_LIMIT_BREACH_AUTODEBIT = "235";
            public static final String ABANDON_AT_BANK_PAGE = "400";
            public static final String ABANDON_AT_CASHIER_PAGE = "402";
            public static final String OTP_LIMIT_BREACHED = "531";

            public static final String SUBSCRIPTION_SUCCESS_RESPONSE_CODE = "3006";
            public static final String SUBSCRIPTION_PENDING_RESPONSE_CODE = "402";
        }

        public static final class ResponseCodeMessages {
            public static final String ABANDON_AT_BANK_PAGE = "Transaction status not confirmed not yet.";
            public static final String ABANDON_AT_CASHIER_PAGE = "User has not completed transaction.";
            public static final String USER_CLOSED_RESPONSE_MSG = "Cancel Request by Customer.";
            public static final String REFERENCE_ID_LENGTH_VALIDATION_MSG = "ReferenceId should fulfill: Minimum length of 10 and maximum length of 20.";

            public static final String SEND_OTP_FAILURE_MESSAGE = "Your request failed due to a technical issue. Please retry again.";
        }

    }

    /**
     *
     * @createdOn 01-Apr-2016
     * @author kesari
     */
    public static enum SessionDataAttributes {
        loginInfo, txnInfo, txnConfig, themeInfo, merchInfo, walletInfo, cardInfo, entityInfo, extendedInfo, orderIdMid, envInfo, retryPaymentInfo, upiTransactionInfo, messageInfo, csrfToken, digitalCreditInfo, savingsAccountInfo, sarvatraVpainfo
    }

    /**
     *
     * @createdOn 01-Apr-2016
     * @author kesari
     */
    public static enum ModelViewAttributes {
        pagepath, useMinifiedAssets, staticResourceVersion, bankHTML, jvmRoute
    }

    public static final class CyberSource {
        public static final String UNKNOWN = "U";
        public static final String FAILURE = "F";
        public static final String VISA_SUCCESS_STATUS_CODE = "201";
        public static final int VISA_FAILURE_STATUS_CODE = 400;
        public static final int VISA_SYSTEM_ERROR_STATUS_CODE = 502;
        public static final int VISA_INTERNAL_SERVER_ERROR = 500;
        public static final String SERVER_ERROR = "SERVER_ERROR";
        public static final String INVALID_REQUEST_400 = "INVALID_REQUEST_400";
        public static final String INVALID_CREDENTIAL_FILE = "INVALID_CREDENTIAL_FILE";
        public static final int SOCKET_TIMEOUT = 0;
        public static final String SUCCESS = "S";
        public static final String EXPIRED_MESSAGE = "EXPIRED CARD";
        public static final String ZERO = "0";
        public static final String INDIAN_CURRENCY = "INR";
        public static final String SUCCESSFUL_STATUS = "AUTHORIZED";
        public static final String DECLINED = "DECLINED";
        public static final String INVALID_REQUEST = "INVALID_REQUEST";
        public static final String RUPAY = "RUPAY";
        public static final String EXPIRED = "EXPIRED";
        public static final String STATUS = "status";
        public static final String REASON = "reason";
        public static final String API = "api";
        public static final String CARD_VALIDATION_API = "/theia/api/v1/cardNumberValidation";
        public static final String RESPONSE_CODE_CARD_VALIDATION = "responseCode";
        public static final String CYBER_SOURCE_KEYS_DIRECTORY = "cyber.source.keys.directory";
        public static final String CYBER_SOURCE_TIME = "cyber.source.time";
        public static final String CYBER_SOURCE_Mbid = "cyber.source.mbid";
        public static final String CYBER_SOURCE_ENVIRONMENT = "cyber.source.environment";
    }

    public static final class CacheName {

        public static final String MERCHANT_MAPPING_CACHE = CommonCacheNames.MERCHANT_MAPPING_CACHE;
        public static final String MERCHANT_DATA_CACHE = "MERCHANT_DATA_CACHE";
        public static final String MERCHANT_PREFERENCE_CACHE = "MERCHANT_PREFERENCE_CACHE";
        public static final String BANK_INFO_CACHE = "BANK_INFO_CACHE";
        public static final String MERCHANT_URL_INFO = CommonCacheNames.MERCHANT_URL_INFO;
        public static final String MERCHANT_OFFER_DETAILS = "MERCHANT_OFFER_DETAILS";
        public static final String RESPONSE_CODE_DETAILS_CACHE = CommonCacheNames.RESPONSE_CODE_DETAILS_CACHE;
        public static final String PAYTM_RESPONSE_CODE_DETAILS_CACHE = CommonCacheNames.PAYTM_RESPONSE_CODE_DETAILS_CACHE;
        public static final String THEMECOUNTER_CACHE = "ThemeCounterCache";
        public static final String THEMECOUNTER_WEB = "ThemeCounterWeb";
        public static final String THEMECOUNTER_WAP = "ThemeCounterWap";
        public static final String CHANNEL_WEB = "WEB";
        public static final String CHANNEL_WAP = "WAP";
    }

    public static final class ExtendedInfoKeys {
        public static final String TAG_LINE = "tagLine";
        public static final String MERCH_UNQ_REF = "merchantUniqueReference";
        public static final String UDF_1 = "udf1";
        public static final String UDF_2 = "udf2";
        public static final String UDF_3 = "udf3";
        public static final String ADDITIONAL_INFO = "additionalInfo";
        public static final String LINK_DESCRIPTION = "LINK_DESCRIPTION";
        public static final String PEON_URL = "PEON_URL";
        public static final String IS_LINK_INVOICE_PAYMENT = "linkBasedInvoicePayment";
        public static final String IS_CARD_TOKEN_REQUIRED = "cardTokenRequired";
        public static final String SUBS_LINK_INFO = "subsLinkInfo";

        public static final class ChannelInfoKeys {
            public static final String BROWSER_USER_AGENT = "browserUserAgent";
            public static final String IS_EMI = "isEmi";
            public static final String CARD_HOLDER_NAME = "cardHoldName";
            public static final String MOBILE_NO = "mobileNo";
            public static final String SHIPPING_ADDR_1 = "shippingAddr1";
            public static final String SHIPPING_ADDR_2 = "shippingAddr2";
            public static final String VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddr";
            public static final String PAYTM_CC_ID = "paytmCCId";
            public static final String LENDER_ID = "lenderId";
            public static final String AUTHORIZATION_TOKEN = "authorizationToken";
            public static final String CARDHOLDER_ADD1 = "cardHolderAddr1";
            public static final String CARDHOLDER_ADD2 = "cardHolderAddr2";
            public static final String PASS_THROUGH_EXTEND_INFO_KEY = "passThroughExtendInfo";
        }

        public static final class ChannelInfoDefaultValues {
            public static final String CARD_HOLDER_NAME = "cardHoldName";
            public static final String MOBILE_NO = "9876543210";
        }

        public static final class PaymentStatusKeys {
            public static final String GATEWAY = "serviceInstId";
            public static final String GATEWAY_NAME = "gatewayName";
            public static final String BANK_TXN_ID = "referenceNo";
            public static final String MASKED_CARD_NUMBER = "maskedCardNo";
            public static final String ISSUING_BANK_NAME = "issuingBankName";
            public static final String IS_FUND_SOURCE_VERIFICATION_SUCCESS = "isFundSourceVerificationSuccess";
        }

        public static final String USER_EMAIL = "email";
        public static final String USER_MOBILE = "phoneNo";
    }

    public static final class SeessionConstant {
        public static final String PAY_OPTION = "PAY_OPTION";
        public static final String PAY_METHOD = "PAY_METHOD";
    }

    public static final class ChannelInfoKeys {
        public static final String IS_EMI = "isEmi";
        public static final String EMI_TENUREID = "emiTenureId";
        public static final String EMI_PLANID = "emiPlanId";
        public static final String EMI_ONUS = "cardAcquiringMode";
        public static final String EMI_MONTHS = "emiMonths";
        public static final String EMI_INTEREST = "emiInterestRate";
        public static final String TO_USE_DIRECT_PAYMENT = "toUseDirectPayment";
        // PGP-34467
        public static final String EMI_AMOUNT = "emiAmount";
        public static final int SIZE_LIMIT_ON_BRANDID_AND_CATEGORYLIST = 6;
        public static final int SIZE_LIMIT_ON_MODEL_AND_SERIAL = 10;

        public static final String VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddr";
        public static final String VERIFICATION_METHOD = "verificationMethod";
    }

    public static final class ExtendedInfoPay {
        public static final String PROMO_RESPONSE_CODE = "promoResponseCode";
        public static final String PROMO_CODE = "promoCode";
        public static final String PROMO_CODE_APPLY_RESULT_STATUS = "promoApplyResultStatus";
        public static final String TXN_TYPE = "txnType";
        public static final String TXN_TYPE_ONLY_WALLET = "ONLY_WALLET";
        public static final String TXN_TYPE_ADDNPAY = "ADDNPAY";
        public static final String MERCHANT_TRANS_ID = "merchantTransId";
        public static final String MCC_CODE = "mccCode";
        public static final String SSO_TOKEN = "ssoToken";
        public static final String PAYTM_MERCHANT_ID = "paytmMerchantId";
        public static final String ALIPAY_MERCHANT_ID = "alipayMerchantId";
        public static final String SAVED_CARD_ID = "savedCardId";
        public static final String PRODUCT_CODE = "productCode";
        public static final String TOTAL_TXN_AMOUNT = "totalTxnAmount";
        public static final String RETRY_COUNT = "retryCount";
        public static final String MERCHANT_NAME = "merchantName";
        public static final String VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddr";
        public static final String CUSTOMER_TYPE = "customerType";
        public static final String INAVLID_PROMOCODE = "invalidPromoCode";
        public static final String VPA = "vpa";
        public static final String CLIENT_ID = "clientId";
        public static final String IS_MERCHANT_LIMIT_ENABLED_FOR_PAY = "isMerchantLimitEnabledForPay";
        public static final String IS_MERCHANT_LIMIT_UPDATED_FOR_PAY = "isMerchantLimitUpdatedForPay";

    }

    // extendinfo from channelaccountquery in fetchBalanceInfo
    public static final class ExtendedInfoChannelAccount {
        public static final String VIRTUAL_ACCOUNT_NO = "virtualAccountNo";
        public static final String TEMPLATE_NAME = "templateName";
        public static final String TEMPLATE_ID = "templateId";
    }

    public static final class BasicPayOption {
        public static final String DEBIT_CARD = "DEBIT_CARD_";
        public static final String CREDIT_CARD = "CREDIT_CARD_";
        public static final String ATM = "ATM_";
        public static final String NET_BANKING = "NET_BANKING_";
        public static final String IMPS = "IMPS";
        public static final String MP_COD = "MP_COD_CODMOCK";
        public static final String EMI = "EMI_";
        public static final String UPI = "UPI";
        public static final String PAYTM_DIGITAL_CREDIT = "PAYTM_DIGITAL_CREDIT";
        public static final String UPI_PUSH = "UPI_PUSH";
        public static final String UPI_PUSH_EXPRESS = "UPI_PUSH_EXPRESS";
    }

    public static final class ExtendedInfoSubscription {
        public static final String SUBS_ID = "subscriptionId";
        public static final String CURRENT_SUBS_ID = "currentSubscriptionId";
        public static final String SUBS_EXPIRY = "subsExpiryDate";
        public static final String SUBS_PAYER_USER_ID = "payerUserID";
        public static final String SUBS_ACCOUNT_NUMBER = "payerAccountNumber";
        public static final String SUBS_REQUEST_TYPE = "requestType";
        public static final String SUBS_CUST_ID = "custID";
        public static final String SUBS_ORDER_ID = "orderID";
        public static final String SUBS_PPI_ONLY = "SUBS_PPI_ONLY";
        public static final String SAVED_CARD_ID = "savedCardId";
        public static final String SUBS_PAY_MODE = "SUBS_PAY_MODE";
        public static final String SUBS_CAPPED_AMOUNT = "subsCappedAmount";
        public static final String SUBS_TYPE = "subscriptionType";
        public static final String USER_EMAIL = "userEmail";
        public static final String USER_MOBILE = "userMobile";
        public static final String SUBS_FREQUENCY = "subsFreq";
        public static final String SUBS_FREQUENCY_UNIT = "subsFreqUnit";
        public static final String AMOUNT_TO_REFUNDED = "amountToBeRefunded";
        public static final String ACCOUNT_TYPE = "accountType";
    }

    public static final String THREAD_NAME_SEPERATOR = "_";

    public static final class PSULimit {
        public static final String NB_LIMIT_BANK_LIST = "nb.limit.bank.list";
        public static final String NB_LIMIT_MAX_AMOUNT = "nb.limit.max.amount";
        public static final String NB_LIMIT_MERCHANT_LIST = "nb.limit.merchant.list";
        public static final String NB_LIMITS_APPLICABLE = "nb.limits.applicable";
        public static final String CARD_LIMIT_MAX_AMOUNT = "card.limit.max.amount";
        public static final String CARD_LIMITS_APPLICABLE = "card.limits.applicable";
        public static final String CARD_LIMIT_BANK_LIST = "card.limit.bank.list";
        public static final String SBI_CARD_BLOCKED = "sbi.block.enable";
    }

    public static final class UpiConfiguration {
        public static final int DEFAULT_UPI_COLLECT_PAYMENT_TIMEOUT = 30;
        public static final String STATUS_QUERY_TIMEOUT = "upi.statusquery.timeout";
        public static final String STATUS_QUERY_INTERVAL = "upi.statusquery.interval";
        public static final String MERCHANT_STATUS_SERVICE_BASE_URL = "merchantstatus.service.base.url";
        public static final String MERCHANT_STATUS_SERVICE_API = "merchantstatus.service.txnstatus.url";
        public static final String UPI_POLL_BASE_URL = "upipoll.base.path";
        public static final String UPI_QR_CODE_PREFIX_REGEX = "upi.qr.code.prefix.regex";
    }

    public static final class DirectBankcardPaymentRedirectionKeys {
        public static final String CASHIER_REQUEST_ID = "${cashierRequestId}";
        public static final String TRANS_ID = "${transId}";
        public static final String MID = "${MID}";
        public static final String ORDER_ID = "${ORDER_ID}";
        public static final String URL = "${URL}";
    }

    public static final class DigitalCreditConfiguration {
        public static final int MAX_PASS_CODE_ATTEMPTS = 3;
        public static final int PASS_CODE_INVALID_ATTEMPTS = 0;
    }

    public static final class PaymentsBankConfiguration {
        public static final int MAX_PASS_CODE_ATTEMPS = 3;
        public static final int PASS_CODE_INVALID_ATTEMPTS = 0;
    }

    public static final class AdditionalCallBackParam {
        public static final String MERCHANT_NAME = "merchantName";
        public static final String ADDRESS_1 = "merchantAddress.address1";
        public static final String ADDRESS_2 = "merchantAddress.address2";
        public static final String AREA_NAME = "merchantAddress.areaName";
        public static final String CITY_NAME = "merchantAddress.cityName";
        public static final String STATE_NAME = "merchantAddress.stateName";
        public static final String ZIP_CODE = "merchantAddress.zipCode";
        public static final String COUNTRY_NAME = "merchantAddress.countryName";
        public static final String INST_ID = "instId";
        public static final String PHONE = "merchantPhone";
        public static final String MCC_CODE = "mccCode";
        public static final String QR_DISPLAY_NAME = "qrDisplayName";
        public static final String CATEGORY = "merchantCategory";
        public static final String SUB_CATEGORY = "merchantSubCategory";
        public static final String LOGO_URL = "logoURL";
    }

    public static final class PaymentFlag {
        public static final String ADD_MONEY_FLAG = "addMoneyFlag";
    }

    public static final class QueueConstants {
        public static final String QR_ORDER = "QROrder";
    }

    public static final class PaytmPropertyConstants {

        public static final String MERCHANT_SPECIFIC_RESPONSE = "merchant.specific.response";
        public static final String MERCHANT_THEME_LB_REDIS_FLAG = "merchantThemeLB.redis.enabled";
        public static final String UNMASKED_VPA_MERCHANT = "unmasked.vpa.merchant";
        public static final String MERCHANT_THEME_LB_FLAG = "merchantThemeLB.enabled";
        public static final String MERCHANTS_ELIGIBLE_FOR_THEME_LB = "list.merchants.enabledThemeLB";
        public static final String ALLOWED_REQUEST_TYPES_FOR_ENHANCED_NATIVE = "allowed.request.types.for.enhanced.native";
        public static final String NOT_ALLOWED_REQUEST_TYPES_FOR_ENHANCED_NATIVE = "not.allowed.request.types.for.enhanced.native";
        public static final String IS_LINK_ON_NEW_OTP_APIS = "isLinkOnNewOtpApis";
        public static final String IS_ORDER_ID_ENABLE_FOR_OTPS = "isOrderIdEnableForOTPs";
        public static final String QR_AMOUNT_MISMATCH_SECURITY_FIX_ENABLE = "qr.amount.mismatch.security.fix.enable";
        public static final String PAY_CHANNEL_NOT_AVAILABLE_MSG = "pay.channel.unavailble.error.message";
    }

    public static final class EnhancedCashierFlow {
        public static final String ENHANCED_CASHIER_FLOW = "enhancedCashierFlow";
        public static final String WORKFLOW = "workFlow";
        public static final String MERCHANT_ERROR_RESPONSE_CODE = "810";

    }

    public static final class CacheConstant {

        public static final String LOCALIZATION_KEYS_CACHE = "LocalizationKeysCache";
        public static final String REDIS_KEYS_CACHE = "RedisKeysCache";
        public static final String MAXIMUM_SIZE = "maximum.size.guavacache.inKeys";
        public static final String EXPIRY_TIME = "expirytime.guavacache.inSeconds";
    }

    public static enum SubscriptionAmountType {
        FIXED, FIX, VARIABLE
    }

    public static final class ThemeConstants {
        public static final String DEFAULT_WAP_THEME = "merchantLow5";
        public static final String DEFAULT_WEB_THEME = "merchant4";
    }

    public static final class BankFormFetchPaymentRetryFlow {
        public static final String BANK_FORM_FETCH_PAYMENT_RETRY_COUNT = "bank.form.fetch.payment.retry.count";
        public static final String BANK_FORM_FETCH_PAYMENT_RETRY_ENABLED = "bank.form.fetch.payment.retry.enabled";
    }

    public static final class SpecialAesConstants {
        public static final String SBI_AES_CHECKSUM_KEY = "sbi.256bit.cksumkey";
        public static final String SBI_AES_ENC_DEC_KEY = "sbi.256bit.enc.dec.key";
    }

    public static final class SubWalletType {
        public static final int PAYTM_BALANCE = 0;
        public static final int GIFT_VOUCHER = 12;
    }

    public static final class SubWalletName {
        public static final String PAYTM_WALLET = "Paytm Wallet";
        public static final String GIFT_VOUCHER = "Gift Voucher";
    }

    public static final class Status {
        public static final int ACTIVE = 1;
        public static final int INACTIVE = 0;
    }

    public static final class Bank {
        public static final String BBK = "bbk";
    }

    public static final class ErrorMsgs {
        public static final String BIN_NOT_SUPPORT_SUBSCRIPTION_MSG = "This card is not supported for subscription payments";
        public static final String CC_ON_UPI_MSG = "CC On Upi is Not Enabled";
    }

    public static final class StagingValidation {
        public static final String ENABLED_URL = "param.validation.error.url";

        public static final String CHANNEL_ID = "CHANNEL_ID";
        public static final String ORDER_ID = "ORDER_ID";
        public static final String INDUSTRY_TYPE_ID = "INDUSTRY_TYPE_ID";
        public static final String CALLBACK_URL = "CALLBACK_URL";
        public static final String CHECKSUMHASH = "CHECKSUMHASH";
        public static final String MID = "MID";
        public static final String CUSTID = "CUST_ID";
        public static final String TXN_AMOUNT = "TXN_AMOUNT";
        public static final String WEBSITE = "WEBSITE";

        public static final String MISSING = "missing";
        public static final String INVALID = "invalid";
        public static final String CORRECT = "correct";
        public static final String MISSING_MSG = "This attribute is not passed. It is a mandatory attribute";
        public static final String INVALID_MSG = "Special character not allowed";
        public static final String MID_INCORRECT = "Syntax of MID is incorrect. No special character allowed";
        public static final String MID_INVALID = "This MID is not available on our staging environment";
        public static final String ORDERID_INVALID = "Syntax of Order ID is incorrect. Special characters allowed in Order ID are | - . @ _";
        public static final String CHANNELID_INVALID = "Channel ID is invalid. Only allowed values are APP, WEB, WAP, SYSTEM";
        public static final String WEBSITE_INVALID = "As its a staging Environment, use its value WEBSTAGING";
        public static final String CALLBACKURL_MISSING = "Callback URL is not passed or is not present in your configuration. Kindly pass a callback URL where response can be posted";
        public static final String CALLBACKURL_INVALID = "Syntax of callback URL is incorrect. Sample callback URL is as 'https://merchant.com/callback/'";
        public static final String CUSTID_INVALID = "Syntax of Customer ID is incorrect. Special characters allowed in Cust_ID are | - . @ _";
        public static final String AMOUNT_INVALID = "Syntax of transaction amount is incorrect.  Should contain digits up to two decimal points";
        public static final String CHECKSUM_INVALID = "Syntax of CHECKSUMHASH is incorrect. Length of checksumhash is 108 characters and cannot consist of spaces";
    }

    public static final String GV_PRODUCT_CODE = "gift.voucher.merchantGoodsId";
    public static final String NCMC_PRODUCT_CODE = "transit.block.wallet.productCode";

    public static final class LinkRiskParams {
        public static final String IS_LINK_BASED_PAYEMNT = "isLinkBasedPayment";
        public static final String LINK_NAME = "linkName";
        public static final String LINK_DESCRIPTION = "linkDescription";
        public static final String LINK_CREATION_TIME = "linkCreationTime";
        public static final String LINK_ORIGIN_LATITUDE = "linkOriginLatitude";
        public static final String LINK_ORIGIN_LONGITUDE = "linkOriginLongitude";
        public static final String MERCHANT_LIMIT = "merchantLimit";
        public static final String DEVICE_ID = "deviceId";
        public static final String USER_LATITUDE = "userLBSLatitude";
        public static final String USER_LONGITUTDE = "userLBSLongitude";
        public static final String MERCHANT_DISPLAY_NAME = "merchantDisplayName";
        public static final String LINK_INVOICE_TYPE = "INVOICE";
        public static final String LINK_TYPE_NON_INVOICE = "LINK";
        public static final String LINK_ID = "linkId";
        public static final String LINK_AMOUNT = "linkAmount";
        public static final String LINK_OPEN_TIME = "linkOpenTime";
        public static final String LINK_REFERRER_SITE = "linkReferrerSite";
        public static final String LINK_TYPE = "LinkType";
        public static final String RESELLER_ID = "resellerId";
        public static final String RESELLER_NAME = "resellerName";
        // merchant mode like offline or online
        public static final String MERCHANT_MODE = "merchantMode";

    }

    public static final class LinkBasedParams {
        public static final String LINK_ID = "LINK_ID";
        public static final String FAIL = "FAIL";
        public static final String INVOICE_ID = "INVOICE_ID";
        public static final String LINK_DESCRITPTION = "LINK_DECSRIPTION";
        public static final String REQUEST_TYPE = "REQUEST_TYPE";
        public static final String SUB_REQUEST_TYPE = "SUB_REQUEST_TYPE";
        public static final String TXN_AMOUNT = "TXN_AMOUNT";
        public static final String TXN_STATUS = "TXN_STATUS";
        public static final String RESPONSE_CODE = "RESPONSE_CODE";
        public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";
        public static final String PAYMENT_MODE = "PAYMENT_MODE";
        public static final String BANK_NAME = "BANK_NAME";
        public static final String TXN_ID = "TXN_ID";
        public static final String MERC_UNIQ_REF = "MERC_UNIQ_REF";
        public static final String VALIDATE_END_POINT = "VALIDATE_END_POINT";
        public static final String VALIDATE_STATUS = "VALIDATE_STATUS";
        public static final String LINK_INVOICE_CHANGE_NUMBER = "?source=CHANGE_NUMBER";
        public static final String SOURCE = "SOURCE";
        public static final String LINK_TYPE = "LINK_TYPE";
        public static final String USER_REQUEST_LATITUDE = "lat";
        public static final String USER_REQUEST_LONGITUTDE = "long";
        public static final String LINK_REQUEST_TYPE = "LinkPayment";
        public static final String IS_DEVICE_EVER_USED = "isDeviceEverUsed";
        public static final String USER_AGENT = "userAgent";
        public static final String FUZZY_DEVICE_ID = "fuzzyDeviceId";
        public static final String APP_VERSION = "appVersion";
        public static final String PHONE_MODEL = "phoneModel";
        public static final String CUSTOM_PAYMENT_FAILURE_MESSAGE = "customPaymentFailureMessage";
        public static final String CUSTOM_PAYMENT_SUCCESS_MESSAGE = "customPaymentSuccessMessage";
        public static final String CUSTOM_PAYMENT_PENDING_MESSAGE = "customPaymentPendingMessage";
        public static final String REDIRECTION_URL_SUCCESS = "redirectionUrlSuccess";
        public static final String REDIRECTION_URL_FAILURE = "redirectionUrlFailure";
        public static final String REDIRECTION_URL_PENDING = "redirectionUrlPending";
        public static final String APP = "APP";
        public static final String SHOW_LINK_PROMOTION = "showLinkPromotion";

    }

    // link payments constans
    public static final String MIN_LENGTH_CUSTOMER_COMMENT_DEFAULT = "3";
    public static final String MAX_LENGTH_CUSTOMER_COMMENT_DEFAULT = "30";
    public static final String LINK_OTP_TIMEOUT = "1800";
    public static final String LINK_OTP_TIMEOUT_CONSTANT = "link.otp.constant";
    public static final String LINK_RESEND_OTP_LIMIT = "link.resend.otp.limit";
    public static final String LINK_RESEND_OTP_MAX_COUNT = "3";
    public static final String LINK_RESEND_OTP_IN_TIMEFRAME_LIMIT = "link.resend.otp.in.timeframe.limit";
    public static final String LINK_RESEND_OTP_IN_TIMEFRAME_MAX_COUNT = "3";
    public static final String LINK_VALIDATE_OTP_LIMIT = "link.validate.otp.limit";
    public static final String LINK_VALIDATE_OTP_MAX_COUNT = "3";

    public static final String ENHANCED_VALIDATE_OTP_MAX_COUNT = "5";
    public static final String ENHANCED_VALIDATE_OTP_LIMIT = "enhanced.validate.otp.limit";
    public static final String INVESTMENT_AS_FUNDING_SOURCE_TNC_URL = "tnc.url.investment.funding.source";
    public static final String DEFAULT_INVESTMENT_FUNDING_TNC_URL = "https://kyc.paytmbank.com/kyc/tnc/get/1046";

    public static final class GvConsent {
        public static final String UNDERSCORE = "_";
        public static final String PTC_V1_URL = "/theia/api/v1/processTransaction";
        public static final String PTR_URL = "/theia/processTransaction";
        public static final String UPI_POLL_PAGE_V1 = "/theia/api/v1/upiPollPage";
        public static final String PUSH_APP_DATA = "PUSH_APP_DATA";
        public static final String SHOW_ADD_MONEY_TO_GV_CONSENT_PAGE_URL = "/theia/api/v1/showConsentPage";
        public static final String ADD_MONEY_TO_GV_CONSENT_KEY = "addMoneyToGvConsentKey";
        public static final String GV_CONSENT_FLOW = "GV_CONSENT_FLOW";
        public static final String TOKEN = "token";
        public static final String CANCEL_TXN_URL = "/theia/cancelTransaction";
        public static final String GV_CONSENT_ENHANCE_MSG = "As per RBI guidelines, this amount cannot be added to your Paytm wallet. The amount will be added to your Paytm Gift Voucher which can be used to recharge, pay bills, shop online or pay at any store. Paytm Gift Voucher, once purchased, cannot be refunded, transferred to a bank account or any Paytm user.";
        public static final String GV_CONSENT_ENHANCE_MSK_KEY = "gv.consent.enhance.msg";
        public static final String BYPASS_GV_CONSENT_PAGE = "bypass.gv.consent.page";
        public static final String IS_EXPRESS_ADD_MONEY_TO_GV = "expressAddMoneyToGv";
    }

    public static final class TheiaInstaConstants {
        public static final String INSTAPROXY_BASE_URL = "instaproxy.base.url";
        public static final String INSTAPROXY_ONECLICK_DENROLL_URL = "instaproxy.oneClick.deEnroll.url";
        public static final String INSTAPROXY_ENROLL_STATUS_CHECK_URL = "instaproxy.enroll.status.check.url";
        public static final String INSTAPROXY_FETCH_DCC_RATES_URL = "/instaproxy/dcc/convertToUserHomeCurrency";
    }

    public static class EventLogConstants {

        public static final String PAYMENT_MODE = "PAYMENT_MODE";
        public static final String CITY = "CITY";
        public static final String MODE = "MODE";
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String DEVICE_CATEGORY = "DEVICE_CATEGORY";
        public static final String OS_VERSION = "OS_VERSION";
        public static final String DEVICE_NAME = "DEVICE_NAME";
        public static final String NETWORK_TYPE = "NETWORK_TYPE";
        public static final String MID = "MID";
        public static final String CUST_ID = "CUST_ID";
        public static final String APP_VERSION = "APP_VERSION";
        public static final String SSO_TOKEN = "SSO_TOKEN";
        public static final String REQUEST_ID = "REQUEST_ID";
        public static final String QR_CODE_ID = "QR_CODE_ID";
        public static final String ORDER_ID = "ORDER_ID";
        public static final String FETCH_MLV_INFO = "FETCH_MLV_INFO";
        public static final String MLV_INFO_PRESENT = "MLV_INFO_PRESENT";
        public static final String REQUEST_TYPE = "REQUEST_TYPE";
        public static final String PLATFORM = "PLATFORM";
        public static final String PLATFORM_VERSION = "PLATFORM_VERSION";
        public static final String PAYMENT_OFFERS_APPLIED_EXISTS = "PAYMENT_OFFERS_APPLIED_EXISTS";
    }

    public static class SarvatraV4QueryParamConstrants {

        public static final String DEVICE_ID = "device-id";
        public static final String DEVICE_NAME = "device-name";
        public static final String OS_VERSION = "os-version";
        public static final String CLIENT = "client";

    }

    public static class OfflineQueryParamConstrants {

        public static final String DEVICE_IDENTIFIER = "deviceIdentifier";
        public static final String DEVICE_NAME = "deviceName";
        public static final String OS_VERSION = "osVersion";
        public static final String NETWORK_TYPE = "networkType";
    }

    public static final class HTMLBuilder {
        public static final String INPUT_FORM_TEMPLATE = "<input type='hidden' name='{name}' value='{value}'>\n";
        public static final String NAME_TEMPLATE = "{name}";
        public static final String VALUE_TEMPLATE = "{value}";
    }

    public static final class DccConstants {
        public static final String INVALID_REQUEST = "request is Invalid";
        public static final String EMPTY_VERSION = "version is Invalid";
        public static final String EMPTY_REQUEST_TIMESTAMP = "request time Stamp is Invalid";
        public static final String INVALID_MID = "mid is Invalid";
        public static final String EMPTY_CLIENT = "client is Invalid";
        public static final String EMPTY_ORDER_ID = "orderId is invalid";
        public static final String EMPTY_BANK_CODE = "bank Code is Invalid";
        public static final int SIZE_OF_BIN = 6;
        public static final String INVALID_BIN = "bin is Invalid";
        public static final String INVALID_PAY_MODE = "pay Mode is Invalid";
        public static final String INVALID_AMOUNT = "amount is Invalid";
        public static final String DCC = "dcc";
        public static final String METHOD = "method";
        public static final String CANCEL = "cancel";
        public static final String TYPE = "type";
        public static final String CONTENT_VALUE = "application/x-www-form-urlencoded";
        public static final String HEADERS = "headers";
        public static final String TXN_TOKEN = "txnToken";
        public static final String ACTION_URL = "actionUrl";
        public static final String PAY = "pay";
        public static final String BODY = "body";

        public static final String CHECKOUT_CANCEL = "checkoutCancel";
        public static final String MERCHANT_LOGO_FLAG = "merchantLogoFlag";
        public static final String MERCHANT_LOGO_URL = "merchantLogoUrl";
        public static final String AMOUNT = "amount";
        public static final String ONUS = "isOnus";
        public static final String DCC_PAGE_BACK_BUTTON_MESSAGE = "Please retry using the same or any other payment method.";
        public static final String THEIA = "Theia";
        public static final String NA = "NA";
        public static final String PAYMENT_CALL_DCC = "paymentCallFromDccPage";
        public static final String DCC_PAGE_RENDER = "dccPageTobeRendered";
        public static final String DCC_PAGE_URL = "/theia/fetchDccPage";

    }

    public static final class DataEnrichmentKeys {
        public static final String APP_VERSION_KEY = "appVersion";
        public static final int APP_VERSION_VAL_LENGTH = 16;
        public static final String BROWSER_VERSION_KEY = "browserVersion";
        public static final int BROWSER_VERSION_VAL_LENGTH = 32;
        public static final String USER_AGENT_KEY = "userAgent";
        public static final int USER_AGENT_VAL_LENGTH = 256;
        public static final String CLIENTIP_KEY = "clientIp";
        public static final int CLIENTIP_VAL_LENGTH = 128;
        public static final String CLIENTKEY_KEY = "clientKey";
        public static final int CLIENTKEY_VAL_LENGTH = 128;
        public static final String BROWSER_TYPE = "browserType";
        public static final int BROWSER_TYPE_VAL_LENGTH = 32;
        public static final String DEVICE_ID_KEY = "deviceId";
        public static final int DEVICE_ID_VAL_LENGTH = 64;
        public static final String DEVICE_IMEI_KEY = "deviceIMEI";
        public static final int DEVICE_IMEI_VAL_LENGTH = 16;
        public static final String DEVICE_MANUFACTURER_KEY = "deviceManufacturer";
        public static final int DEVICE_MANUFACTURER_VAL_LENGTH = 32;
        public static final String DEVICE_MODEL_KEY = "deviceModel";
        public static final int DEVICE_MODEL_VAL_LENGTH = 128;
        public static final String DEVICE_TYPE_KEY = "deviceType";
        public static final int DEVICE_TYPE_VAL_LENGTH = 128;
        public static final String GENDER_KEY = "gender";
        public static final int GENDER_VAL_LENGTH = 8;
        public static final String HYBRID_PLATFORM_KEY = "hybridPlatform";
        public static final int HYBRID_PLATFORM_VAL_LENGTH = 32;
        public static final String HYBRID_PLATFORM_VERSION_KEY = "hybridPlatformVersion";
        public static final int HYBRID_PLATFORM_VERSION_VAL_LENGTH = 32;
        public static final String ICCIDNUMBER_KEY = "ICCIDNumber";
        public static final int ICCIDNUMBER_VAL_LENGTH = 32;
        public static final String LANGUAGE_KEY = "language";
        public static final int LANGUAGE_VAL_LENGTH = 16;
        public static final String MERCHANT_APP_VERSION_KEY = "merchantAppVersion";
        public static final int MERCHANT_APP_VERSION_VAL_LENGTH = 128;
        public static final String ORDER_OS_TYPE_KEY = "orderOsType";
        public static final int ORDER_OS_TYPE_VAL_LENGTH = 128;
        public static final String ORDER_TERMINAL_ID_KEY = "orderTerminalId";
        public static final int ORDER_TERMINAL_ID_VAL_LENGTH = 128;
        public static final String ORDER_TERMINAL_TYPE_KEY = "orderTerminalType";
        public static final int ORDER_TERMINAL_TYPE_VAL_LENGTH = 32;
        public static final String OS_VERSION_TYPE_KEY = "osVersion";
        public static final int OS_VERSION_TYPE_VAL_LENGTH = 16;
        public static final String PLATFORM_KEY = "platform";
        public static final int PLATFORM_VAL_LENGTH = 8;
        public static final String PRODUCT_CODE_KEY = "productCode";
        public static final int PRODUCT_CODE_VALUE_LENGTH = 128;
        public static final String OS_TYPE_KEY = "osType";
        public static final int OS_TYPE_VAL_LENGTH = 16;
        public static final String ROUTER_MAC_KEY = "routerMac";
        public static final int ROUTER_MAC_VAL_LENGTH = 32;
        public static final String SCREEN_RESOLUTION_KEY = "screenResolution";
        public static final int SCREEN_RESOLUTION_VALUE_LENGTH = 16;
        public static final String SDK_VERSION_KEY = "sdkVersion";
        public static final int SDK_VERSION_VAL_LENGTH = 128;
        public static final String SESSIONID_KEY = "sessionId";
        public static final int SESSIONID_VAL_LENGTH = 128;
        public static final String TIMEZONE_KEY = "timeZone";
        public static final int TIMEZONE_VAL_LENGTH = 32;
        public static final String TOKENID_KEY = "tokenId";
        public static final int TOKENID_VAL_LENGTH = 128;
        public static final String USER_LATITUDE_KEY = "userLBSLatitude";
        public static final int USER_LATITUDE_VAL_LENGTH = 16;
        public static final String USER_LONGITUDE_KEY = "userLBSLongitude";
        public static final int USER_LONGITUDE_VAL_LENGTH = 16;
        public static final int VERSION_CODE_VAL_LENGTH = 128;
        public static final String WEBSITE_LANGUAGE_KEY = "websiteLanguage";
        public static final int WEBSITE_LANGUAGE_VAL_LENGTH = 16;
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String MERCHANT_TYPE_KEY = "merchantType";
        public static final int MERCHANT_TYPE_VAL_LENGTH = 32;
        public static final String APP_INVOKE_FROM = "REDIRECTION_APPINVOKE";
        public static final String IS_OFFLINE_MERCHANT = "isOfflineMerchant";
        public static final String IS_ONLINE_MERCHANT = "isOnlineMerchant";
        public static final String REQUEST_TYPE = "requestType";
        public static final String NA = "NA";
    }
}
