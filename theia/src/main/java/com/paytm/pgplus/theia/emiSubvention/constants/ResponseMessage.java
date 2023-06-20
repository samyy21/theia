package com.paytm.pgplus.theia.emiSubvention.constants;

public class ResponseMessage {
    public static final String MERCHANT_NOT_FOUND = "No merchant link found.";

    public static final String EMPTY_MID = "empty merchant Id.";

    public static final String EMPTY_CUST_ID = "customer id is missing";
    public static final String EMPTY_REQUEST_ID = "requestId cannot be null";

    public static final String TOKEN_TYPE_NOT_SUPPORTED = "token type not supported";
    public static final String CUST_ID_CANNOT_BE_BLANK = "CustId cannot be blank for tokenType";
    public static final String INVALID_CHECKSUM = "Checksum provided is invalid";
    public static final String INVALID_CHANNEL_ID = "Channel Id provided is invalid";

    public static final String AMOUNT_MISMATCH = "items total amount  does not match the totalTransaction Amount";
    public static final String ITEM_LIST_MISSING = "list of item is missing";
    public static final String OFFER_DETAILS_MISSING = "offer details are missing";
    public static final String PAYMENT_DETAILS_MISSING = "Payment Details is missing";
    public static final String INVALID_TOTAL_TRANSACTION_AMOUNT = "Invalid total transaction amount";
    public static final String TOTAL_TRANSACTION_AMOUNT_MISSING = "Total transaction amount is missing";
    public static final String CARD_DETAILS_MISSING = "Card Details are missing";
    public static final String CUST_ID_BLANK = "CustId can't be blank for tokenType";

    public static final String BOTH_ITEM_LIST_AMOUNT_NON_EMPTY = "Both item list and amount cannot be non empty in request";
    public static final String BOTH_ITEM_LIST_AMOUNT_EMPTY = "Both item list and amount both cannot be empty in request";
    public static final String BOTH_LISTINGPRICE_PRICE_REQUIRED = "Both listing price and subvention amount is mandatory";
    public static final String SUBVENTION_AMOUNT_GREATER = "Subvention amount cannot be greater than total price";
    public static final String INVALID_AMOUNT_DETAILS = "Invalid Amount Details";

    public static final String CACHE_CARD_TOKEN_NOT_PRESENT = "cache card not found";

    public static final String INVALID_CARD_DETAILS = "card details are not valid";

    public static final String VALIDATE_EMI_PROCESSED_SUCCESSFULLY = "validate emi processed successfully";

    public static final String TENURES_EMI_PROCESSED_SUCCESSFULLY = "SUCCESS";

    public static final String BANKS_EMI_PROCESSED_SUCCESSFULLY = "SUCCESS";

    public static final String FILTERS_MISSING = "filters list is missing";
}
