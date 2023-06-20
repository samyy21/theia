package com.paytm.pgplus.theia.offline.enums;

/**
 * Created by rahulverma on 30/8/17.
 */
public enum ResultCode {

    SUCCESS("0000", "S", "Success", "SUCCESS"), REQUEST_PARAMS_VALIDATION_EXCEPTION("1001", "F",
            "Request parameters are not valid", "REQUEST_PARAMS_VALIDATION_EXCEPTION"), TOKEN_VALIDATION_EXCEPTION(
            "1002", "F", "Token validation failed", "TOKEN_VALIDATION_EXCEPTION"), BIN_NUMBER_EXCEPTION("1003", "F",
            "Bin number is not valid", "BIN_NUMBER_EXCEPTION"), ORDER_GENERATION_EXCEPTION("1004", "F",
            "Order id generation exception", "ORDER_GENERATION_EXCEPTION"), DUPLICATE_PAYMENT_REQUEST_EXCEPTION("1005",
            "F", "Duplicate payment request exception", "DUPLICATE_PAYMENT_REQUEST_EXCEPTION"), SESSION_EXPIRED_EXCEPTION(
            "1006", "F", "Your Session has expired.", "SESSION_EXPIRED_EXCEPTION"), MISSING_MANDATORY_ELEMENT("1007",
            "F", "Missing mandatory element", "MISSING_MANDATORY_ELEMENT"), PIPE_CHAR_IS_NOT_ALLOWED("1008", "F",
            "Pipe character is not allowed", "PIPE_CHAR_IS_NOT_ALLOWED"), PROMO_CODE_VALIDATION_EXCEPTION("1009", "F",
            "promo code request is not valid", "PROMO_CODE_VALIDATION_EXCEPTION"), PROMO_CODE_INVALID("1010", "F",
            "promo code is not valid", "PROMO_CODE_INVALID"), PAYMENT_PROMO_NOT_EXISTS_ON_MERCHANT("1012", "F",
            "No promo code currently active on the merchant", "PAYMENT_PROMO_NOT_EXISTS_ON_MERCHANT"), TXN_ALREADY_IN_PROCESS(
            "1111", "F", "transaction already in process", "TXN_ALREADY_IN_PROCESS"), UPDATE_TXN_DETAIL_FAILED("1112",
            "F", "update transaction detail failed", "UPDATE_TXN_DETAIL_FAILED"), LARGE_ADDITIONAL_INFO("1113", "F",
            "additionalInfo is too large", "LARGE_ADDITIONAL_INFO"), INVALID_KYC("1114", "F",
            "KYC is not done for User.", "INVALID_KYC"), BIN_DETAIL_EXCEPTION("2001", "F",
            "Not able to fetch bin Details", "BIN_DETAIL_EXCEPTION"), BANK_DETAIL_EXCEPTION("2002", "F",
            "Not able to fetch bank Details", "BANK_DETAIL_EXCEPTION"), ORDER_ID_GENERATION_EXCEPTION("2003", "F",
            "Order id generation exception", "ORDER_ID_GENERATION_EXCEPTION"), INVALID_SSO_TOKEN("2004", "F",
            "SSO Token is invalid", "SSO_TOKEN_VALIDATION_EXCEPTION"), INVALID_SSO_TOKEN_FF("2004", "F",
            "SSO Token is invalid", "SSO_TOKEN_VALIDATION_EXCEPTION_FF"), INVALID_CHECKSUM("2005", "F",
            "Checksum provided is invalid", "CHECKSUM_VALIDATION_EXCEPTION"), INVALID_MID("2006", "F",
            "Mid is invalid", "INVALID_MID"), INVALID_TXN_AMOUNT("2007", "F", "Txn amount is invalid",
            "INVALID_TXN_AMOUNT"), FETCH_BALANCE_INFO_EXCEPTION("2008", "F", "System Error",
            "FETCH_BALANCE_INFO_EXCEPTION"), DUPLICATE_REQUEST("2009", "F",
            "Duplicate request, with same orderId is already in progress", "DUPLICATE_REQUEST"), PAYMENT_NOT_ALLOWED_FOR_BIN(
            "2011", "F", "Payment not allowed presently on your card. Please try paying using other cards/options",
            "PAYMENT_NOT_ALLOWED_FOR_BIN"), INVALID_ORDERID("2012", "F", "OrderId is invalid", "INVALID_ORDERID"), PAYMENT_REQUEST_PROCESSING_EXCEPTION(
            "3001", "F", "Exception in payment request processing", "PAYMENT_REQUEST_PROCESSING_EXCEPTION"), PAYMENT_REQUEST_QR_EXCEPTION(
            "3002", "F", "Exception in payment request processing", "PAYMENT_REQUEST_QR_EXCEPTION"), UNKNOWN_ERROR(
            "9999", "F", "Something went wrong", "UNKNOWN_ERROR"), INVALID_PASS_CODE("PVC-044", "F",
            "Invalid passcode. Please try again.", "INVALID_PASS_CODE"), MERCHANT_VELOCITY_LIMIT_BREACH(
            "2010",
            "F",
            "Payment failed as merchant has crossed his payment acceptance limit. Please ask the merchant to reach out to Paytm helpdesk",
            "MERCHANT_VELOCITY_LIMIT_BREACH"), MERCHANT_MONTHLY_VELOCITY_LIMIT_BREACH(
            "2010",
            "F",
            "Payment failed as merchant has crossed his Monthly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.",
            "MERCHANT_VELOCITY_LIMIT_BREACH"), MERCHANT_WEEKLY_VELOCITY_LIMIT_BREACH(
            "2010",
            "F",
            "Payment failed as merchant has crossed his weekly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.",
            "MERCHANT_VELOCITY_LIMIT_BREACH"), MERCHANT_DAILY_VELOCITY_LIMIT_BREACH(
            "2010",
            "F",
            "Payment failed as merchant has crossed his daily acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.",
            "MERCHANT_VELOCITY_LIMIT_BREACH"), OPERATAION_NOT_SUPPORTED("2012", "F", "Operation is not supported",
            "OPERATAION_NOT_SUPPORTED"), MID_DOES_NOT_MATCH("2013", "F",
            "Mid in the query param doesn't match with the Mid send in the request", "MID_DOES_NOT_MATCH"), ORDER_ID_DOES_NOT_MATCH(
            "2014", "F", "OrderId in the query param doesn't match with the OrderId send in the request",
            "ORDER_ID_DOES_NOT_MATCH"), VPA_NOT_FOUND_EXCEPTION("3000", "F", "Vpa not found", "VPA_NOT_FOUND_EXCEPTION"), FETCH_MERCHANT_INFO_EXCEPTION(
            "2020", "F", "Merchant Info doesn't exist", "MERCHANT_INFO_EXCPETION"), INVALID_TOKEN_TYPE_EXCEPTION(
            "2021", "F", "Invalid token type", "TOKEN_TYPE_EXCEPTION"), INVALID_SUBS_PAYMNT_MODE("2022", "F",
            "Invalid Subs payment mode", "SUBS_PAYMENT_MODE_EXCEPTION"), REPEAT_REQUEST_INCONSISTENT("2023", "F",
            "Repeat Request Inconsistent", "REPEAT_REQUEST_INCONSISTENT"), ACCOUNT_QUERY_FAIL("3002", "F",
            "Account query failed", "ACCOUNT_QUERY_FAIL"), MERCHANT_REDIRECT_REQUEST_EXCEPTION("810", "F",
            "System Error", "SYSTEM_ERROR"), PPBL_SYSTEM_UNAVAILABLE("3003", "F",
            "We are unable to fetch your balance, kindly try after sometime", "PPBL_SYSTEM_UNAVILABLE"), EMI_NOT_CONFIGURED_ON_MERCHANT(
            "3004", "F", "EMI not configured on merchant", "EMI_NOT_CONFIGURED_ON_MERCHANT"), PLATFORM_SYSTEM_UNAVAILABLE(
            "3005", "F", "We are unable to fetch your balance, kindly try after sometime",
            "PLATFORM_SYSTEM_UNAVAILABLE"), FAILED("0001", "F", "FAILED", "FAILED"), QR_EXPIRED("0001", "F",
            "QR Code has been expired, Please ask merchant to share the new QR code", "FAILED"), SUCCESS_IDEMPOTENT_ERROR(
            "0002", "S", "Success Idempotent", "SUCCESS_IDEMPOTENT_ERROR"), CONFIGURATION_MISMATCH_SHADOW_REQUEST(
            "9999", "F", "There is a configuration mismatch for shadow request",
            "SHADOW_REQUEST_CONFIGURATION_MISMATCH"), CAST_TO_SUBSCRIPTION_EXCEPTION("2024", "F",
            "Invalid Subscription Request", "SUBS_CAST_EXCEPTION"), INVALID_SUBS_PAY_MODE_GREATER_TXN_AMOUNT("2022",
            "F", "Paymode selected is not applicable when txn amount is less than the renewal amount",
            "SUBS_PAYMENT_MODE_EXCEPTION"), FETCH_POST_PAID_BALANCE_INFO_EXCEPTION("2008", "F",
            "We are facing some issue with postpaid, please use other payment options",
            "FETCH_POST_PAID_BALANCE_INFO_EXCEPTION"), UNABLE_TO_FETCH_BALANCE("3006", "F",
            "Unable to fetch your balance. Please try a different method to complete payment",
            "UNABLE_TO_FETCH_BALANCE"), SUBSCRIPTION_VALIDATION_FAILURE("4001", "F",
            "Subscription Validation Failure.", "SUBSCRIPTION_VALIDATION_FAILURE"), INVALID_DO_VIEW_REQUEST("4002",
            "F", "Invalid Request", "INVALID_DO_VIEW_REQUEST"), INVALID_DO_VERIFY_REQUEST("4003", "F",
            "Invalid Request", "INVALID_DO_VIEW_REQUEST"), INVALID_PRN("4002", "F", "Invalid PRN", "Invalid PRN"), INVALID_LINK_AMT_OR_DESC(
            "308", "F", "Link amount or description not matched", "INVALID_LINK_AMT_OR_DESC"), UNAUTHORISED_ACCESS(
            "403", "F", "INVALID SSO TOKEN", "INVALID_SSO_TOKEN"), SUBSCRIPTION_AMOUNT_LIMIT_FAILURE("4004", "F",
            "Max amount is greater than the permissible amount limit for available paymodes",
            "SUBSCRIPTION_AMOUNT_LIMIT_FAILURE"), SUBSCRIPTION_NOT_IN_AUTHORISED_STATE("4005", "F",
            "Subscription is not in authorised state", "SUBSCRIPTION_NOT_IN_AUTHORISED_STATE"), SUBSCRIPTION_NOT_ALLOWED_FOR_NORMAL_PAYMODE(
            "4006", "F", "Add and Pay Payments are not allowed for this subscription",
            "SUBSCRIPTION_NOT_ALLOWED_FOR_NORMAL_PAYMODE"), INVALID_PROMO_PARAM("1011", "F", "Invalid Promo Param",
            "INVALID_PROMO_PARAM"), INVALID_SUBSCRIPTION_PAYMENT_MODE("2022", "F",
            "Paymode selected is not enabled for your merchant account. Please reach out to support teams to enable",
            "SUBS_PAYMENT_MODE_EXCEPTION"), INVALID_PAYMODE_WHEN_TXN_AMOUNT_GT_RENEWAL_AMOUNT("2022", "F",
            "Paymode selected is not applicable when txn amount is greater than the renewal amount",
            "SUBS_PAYMENT_MODE_EXCEPTION"), INVALID_SUBSCRIPTION_AMOUNT("00000900", "U",
            "Transaction amount cannot be greater than the max amount set against the subscription",
            "INVALID_SUBSCRIPTION_AMOUNT_EXCEPTION"), INVALID_PROMO_AMOUNT("1012", "F",
            "Promo amount cannot be more than transaction amount", "INVALID_PROMO_AMOUNT"), INVALID_TXN_AMOUNT_AGAINST_FIX_SUB_AMOUNT_TYPE(
            "00000900", "U", "Transaction amount is not equal to max amount set against the subscription",
            "INVALID_SUBSCRIPTION_AMOUNT_EXCEPTION"), INVALID_SUBSCRIPTION_MAX_AMOUNT("00000900", "U",
            "SubMaxAmount cannot be zero set against the subscription", "INVALID_SUBSCRIPTION_AMOUNT_EXCEPTION"), RATE_LIMITING_EXCEPTION(
            "429", "F", "Rate Limiting breached, not proceeding with the request", "RATE_LIMITING_EXCEPTION"), RATE_LIMIT_BREACHED_EXCEPTION(
            "429", "F", "Rate Limiting breached, not proceeding with the request", "RATE_LIMIT_BREACHED_EXCEPTION"), INVALID_LINK_DETAILS_IN_INITIATE_REQUEST(
            "2100", "F", "Link details are not valid", "INVALID_LINK_DETAILS_IN_INITIATE_REQUEST"), INVALID_WALLET_PAYMENT_MODE(
            "0001", "F",
            "Paytm Balance is not allowed for this payment, kindly use another payment instrument to make payment",
            "PAYTM_BALANCE_PAYMENT_MODE_EXCEPTION"), TIMESTAMP_EXPIRY(
            "2015",
            "F",
            "Payment could not be completed because the currency conversion rate provided by our banking partner has expired. Please retry the payment.",
            "TIMESTAMP_EXPIRY"), EDC_MERCHANT_DAILY_VELOCITY_LIMIT_BREACH("2010", "F",
            "Transaction decline as daily limit exceeded for accepting payments on merchant's device",
            "EDC_MERCHANT_VELOCITY_LIMIT_BREACH"), EDC_MERCHANT_MONTHLY_VELOCITY_LIMIT_BREACH("2010", "F",
            "Transaction decline as monthly limit exceeded for accepting payments on merchant's device",
            "EDC_MERCHANT_VELOCITY_LIMIT_BREACH"), CUSTOMER_NOT_FOUND("2028", "F", "Customer Not Found",
            "CUSTOMER_NOT_FOUND"), LINK_MONTHLY_AMOUNT_LIMIT_EXCEED(
            "2030",
            "F",
            "Payment failed because the merchant crossed their monthly transaction limit for link payments. Please ask the merchant to reach out to Paytm Helpdesk",
            "LINK_MONTHLY_AMOUNT_LIMIT_EXCEED"), ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED(
            "2010",
            "F",
            "You can add up to Rs. 4000 per month using Credit Card in your wallet. Please use UPI or Debit Card payment option or go to the Paytm App to continue using credit card.",
            "ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED"), INVALID_JWT("2051", "F", "Token not valid", "INVALID_JWT"), JWT_HASH_MISMATCH(
            "2052", "F", "Hash mismatch", "JWT_HASH_MISMATCH"), JWT_MISSING("2053", "F", "JWT is missing in header",
            "JWT_MISSING"), EXPIRED_JWT("2054", "F", "Expired Token", "EXPIRED_JWT"), CURRENCY_NOT_SUPPORTED("2055",
            "F", "Currency is not supported", "CURRENCY_NOT_SUPPORTED"), EXCHANGE_RATE_INVALID("00000050", "F",
            "Exchange Rate verification Failed", "EXCHANGE_RATE_INVALID"), MERCHANT_AMOUNT_EXCEED_TXN_LIMIT("2010",
            "F", "Payment declined as the merchant transaction limit got breached here",
            "MERCHANT_AMOUNT_EXCEED_TXN_LIMIT"), MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_DAILY_LIMIT(
            "2010",
            "F",
            "Payment failed because the merchant crossed their daily transaction limit for link payments. Please ask the merchant to reach out to Paytm Helpdesk",
            "MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_DAILY_LIMIT"), MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_WEEKLY_LIMIT(
            "2010",
            "F",
            "Payment failed because the merchant crossed their weekly transaction limit for link payments. Please ask the merchant to reach out to Paytm Helpdesk",
            "MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_WEEKLY_LIMIT"), MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_MONTHLY_LIMIT(
            "2010",
            "F",
            "Payment failed because the merchant crossed their monthly transaction limit for link payments. Please ask the merchant to reach out to Paytm Helpdesk",
            "MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_MONTHLY_LIMIT"), MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_TXN_LIMIT(
            "2010",
            "F",
            "Payment failed because the merchant crossed their amount transaction limit for link payments. Please ask the merchant to reach out to Paytm Helpdesk",
            "MERCHANT_LINK_PAYMENT_AMOUNT_EXCEED_TXN_LIMIT"), MAXIMUM_GRACE_DAYS("2022", "F",
            "Maximum Grace Days supported on Credit Card is %s and Debit Card is %s ", "GRACE_DAYS_ERROR"), INVALID_COFT_CONSENT(
            "2016", "F", "Tokenisation consent is mandatory for setting up SI", "INVALID_COFT_CONSENT"), PAYMENT_IN_PROCESS(
            "308",
            "F",
            "Your payment is still processing. If any amount was deducted it shall be adjusted once the payment is confirmed. Please wait for some time or try again later.",
            "PAYMENT_IN_PROCESS"), LINK_PAYMENT_IN_PROCESS(
            "5057",
            "F",
            "A payment on this link is already under process. If any amount was deducted it shall be adjusted once the payment is confirmed. Please wait for some time or try again later.",
            "LINK_PAYMENT_IN_PROCESS"), LINK_PAYMENT_ALREADY_PROCESSED("5055", "F",
            "The Link is already Paid. Please reach out to the merchant in case of any query.",
            "LINK_PAYMENT_ALREADY_PROCESSED"), TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT(
            "9015",
            "F",
            "You are trying to make a payment which exceeds the total payment amount allowed on this link. Try making another payment not exceeding the total payment amount. Please reach out to the merchant in case of any query.",
            "TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT"),

    TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED(
            "9016",
            "F",
            "Total payment amount allowed on this link is reached. Please reach out to the merchant in case of any query.",
            "TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED"), INVALID_QRCODEID("2017", "F", "QrCodeId is invalid",
            "INVALID_QRCODEID"),

    DC_DOM_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant Debit card daily acceptance limit exceeded, please try with another payment option",
            "DC_DOM_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant Debit card monthly acceptance limit exceeded, please try with another payment option",
            "DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010",
            "F", "Merchant Debit card transaction limit exceeded due to which transaction won't processed",
            "DC_DOM_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant Rupay Debit Card daily acceptance limit exceeded, please try with another payment option",
            "DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant Rupay Debit Card monthly acceptance limit exceeded, please try with another payment option",
            "DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F",
            "Merchant Rupay Debit Card transaction limit exceeded due to which transaction won't processed",
            "DC_DOM_RUPAY_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant VISA Debit Card daily acceptance limit exceeded, please try with another payment option",
            "DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant VISA Debit Card monthly acceptance limit exceeded, please try with another payment option",
            "DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F",
            "Merchant VISA Debit Card transaction limit exceeded due to which transaction won't processed",
            "DC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant VISA Credit Card daily acceptance limit exceeded, please try with another payment option",
            "CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant VISA Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F",
            "Merchant VISA Credit Card transaction limit exceeded due to which transaction won't processed",
            "CC_DOM_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant Credit Card daily acceptance limit exceeded, please try with another payment option",
            "CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant Credit Card transaction limit exceeded due to which transaction won't processed",
            "CC_DOM_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_INTL_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant Credit Card daily acceptance limit exceeded, please try with another payment option",
            "CC_INTL_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010",
            "F", "Merchant Credit Card transaction limit exceeded due to which transaction won't processed",
            "CC_INTL_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant VISA Credit Card daily acceptance limit exceeded, please try with another payment option",
            "CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant VISA Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant Credit Card transaction limit exceeded due to which transaction won't processed",
            "CC_INTL_VISA_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F",
            "Merchant Master Credit Card daily acceptance limit exceeded, please try with another payment option",
            "CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F",
            "Merchant Master Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant Credit Card transaction limit exceeded due to which transaction won't processed",
            "CC_INTL_MASTER_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), BALANCE_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F", "Merchant Wallet daily acceptance limit exceeded, please try with another payment option",
            "BALANCE_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), BALANCE_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant Wallet monthly acceptance limit exceeded, please try with another payment option",
            "BALANCE_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), BALANCE_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010",
            "F", "Merchant Wallet transaction limit exceeded due to which transaction won't processed",
            "BALANCE_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F", "Merchant UPI daily acceptance limit exceeded, please try with another payment option",
            "UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F", "Merchant UPI monthly acceptance limit exceeded, please try with another payment option",
            "UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant UPI transaction limit exceeded due to which transaction won't processed",
            "UPI_COLLECT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F", "Merchant UPI daily acceptance limit exceeded, please try with another payment option",
            "UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010",
            "F", "Merchant UPI monthly acceptance limit exceeded, please try with another payment option",
            "UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant UPI transaction limit exceeded due to which transaction won't processed",
            "UPI_INTENT_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT(
            "2010", "F", "Merchant UPI daily acceptance limit exceeded, please try with another payment option",
            "UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010",
            "F", "Merchant UPI monthly acceptance limit exceeded, please try with another payment option",
            "UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant UPI transaction limit exceeded due to which transaction won't processed",
            "UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), UPI_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant UPI daily acceptance limit exceeded, please try with another payment option",
            "UPI_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant UPI monthly acceptance limit exceeded, please try with another payment option",
            "UPI_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), UPI_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010", "F",
            "Merchant UPI transaction limit exceeded due to which transaction won't processed",
            "UPI_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), NB_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant NetBanking daily acceptance limit exceeded, please try with another payment option",
            "NB_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), NB_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant NetBanking monthly acceptance limit exceeded, please try with another payment option",
            "NB_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), NB_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010", "F",
            "Merchant NetBanking transaction limit exceeded due to which transaction won't processed",
            "NB_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), POSTPAID_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant Postpaid daily acceptance limit exceeded, please try with another payment option",
            "POSTPAID_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010",
            "F", "Merchant Postpaid monthly acceptance limit exceeded, please try with another payment option",
            "POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "Merchant Postpaid transaction limit exceeded due to which transaction won't processed",
            "POSTPAID_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), DC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant Debit card daily acceptance limit exceeded. Try with different payment option",
            "DC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), DC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant Debit Card monthly acceptance limit exceeded, please try with another payment option",
            "DC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), DC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010", "F",
            "Merchant Debit Card transaction limit exceeded, please choose another payment option",
            "DC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "Merchant Credit card daily acceptance limit exceeded. Try with different payment option",
            "CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), CC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT("2010", "F",
            "Merchant Credit Card monthly acceptance limit exceeded, please try with another payment option",
            "CC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT"), CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT("2010", "F",
            "Merchant Credit Card transaction limit exceeded, please choose another payment option",
            "CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), UPI_CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT("2010", "F",
            "upi cc limit check amount over daily limit", "UPI_CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_CC_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT(
            "2010", "F", "upi cc limit check amount over monthly limit", "UPI_CC_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT"), UPI_CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT(
            "2010", "F", "upi cc limit check amount over max single limit",
            "UPI_CC_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT"), VALIDATION_API_FAILURE("3009", "F",
            "Internal System Error", "FAILED"), VALIDATION_FAIL_REFUND_TXN("3010", "F",
            "Can't perform BLOCK operation for REFUNDED TXN", "FAILED"), VALIDATION_FAIL_FAILED_TXN("3011", "F",
            "Can't perform BLOCK operation for FAILED TXN", "FAILED"), MISSING_MANDATORY_ELEMENT_MID("1007", "F",
            "mid can't be blank", "MISSING_MANDATORY_ELEMENT"), MISSING_MANDATORY_ELEMENT_ORDERID("1007", "F",
            "orderId can't be blank", "MISSING_MANDATORY_ELEMENT"), MISSING_MANDATORY_ELEMENT_SKUCODE("1007", "F",
            "skuCode can't be blank", "MISSING_MANDATORY_ELEMENT"), MISSING_MANDATORY_ELEMENT_IMEI("1007", "F",
            "imei can't be blank", "MISSING_MANDATORY_ELEMENT"), MISSING_MANDATORY_ELEMENT_BRANDID("1007", "F",
            "brandId can't be blank", "MISSING_MANDATORY_ELEMENT"), INVALID_ACTION("1007", "F",
            "Invalid action sent in request", "INVALID_ACTION");

    private String resultCodeId;
    private String resultStatus;
    private String resultMsg;
    private String code;

    /**
     * @param resultCodeId
     * @param resultStatus
     * @param resultMsg
     * @param resultCode
     */
    private ResultCode(String resultCodeId, String resultStatus, String resultMsg, String resultCode) {
        this.resultCodeId = resultCodeId;
        this.resultStatus = resultStatus;
        this.resultMsg = resultMsg;
        this.code = resultCode;
    }

    /**
     * @return the resultCodeId
     */
    public String getResultCodeId() {
        return resultCodeId;
    }

    /**
     * @return the resultStatus
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the resultMsg
     */
    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * @return the resultCode
     */
    public String getCode() {
        return code;
    }

}
