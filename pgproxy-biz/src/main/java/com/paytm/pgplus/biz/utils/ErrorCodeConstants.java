package com.paytm.pgplus.biz.utils;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

/*
 * This code contains copyright information which is the proprietary property
 * 
 * of Tarang Software Technologies Pvt Ltd . No part of this code may be
 * reproduced, stored or transmitted in any form without the prior written
 * permission.
 * 
 * Copyright (C) Tarang Software Technologies Pvt Ltd 2012. All rights reserved.
 * -----------------------------------------------------------------------------
 * - Author : Description:
 * -----------------------------------------------------------------------------
 * - Change History
 * -----------------------------------------------------------------------------
 * -
 * 
 * -----------------------------------------------------------------------------
 * -
 */

/**
 * This Interface will all layer of Error Code Constants.
 */
public class ErrorCodeConstants {

    public static final String GET_TRANSIENT_TXN_INFO = "DAER-TX125";
    public static final String GET_NETBANKING_BANKS = "DAER-TX101";
    public static final String GET_OFFLINE_BANKS = "DAER-TX131";
    public static final String GET_DEBIT_CARDS_LIST = "DAER-TX121";
    public static final String GET_STATE_LIST = "DAER-TX102";
    public static final String TRANSIENT_INSERT_DAEXCEPTION = "DAER-TX103";
    public static final String TXNUSER_INSERT = "DAER-TX104";
    public static final String TRANSIENT_BANK_RESONSE_UPDATE = "DAER-TX105";
    public static final String TRANSIENT_UPDATE = "DAER-TX106";
    public static final String TXN_USER_INFO = "DAER-TX107";
    public static final String ORACLE_ERROR = "DAORER-TX108";
    public static final String TXN_VELOCITY_CHECK_CUSTID = "DAER-TX109";
    public static final String TXN_VELOCITY_CHECK_CARDNUM = "DAER-TX110";
    public static final String TXN_VELOCITY_NB_LIMITS = "DAER-TX111";
    public static final String GET_SAVED_CARD_DETAILS = "DAER-TX112";
    public static final String GET_SAVED_CARD_LIST = "DAER-TX113";
    public static final String GET_PARAM_FIELDS = "DAER-TX114";
    public static final String VERIFY_BLOCKS_IP_CUSTID_MISISDN = "DAER-TX115";
    public static final String VERIFY_BLOCKS_CARDNUMBER = "DAER-TX116";
    public static final String INSER_BLOCK_GRAY_ALERT = "DAER-TX117";
    public static final String GET_FSS_ACTION_INFO = "DAER-TX118";
    public static final String UPDATE_FSS_ALERT_COUNT = "DAER-TX119";
    public static final String GET_FSS_ALERT_COUNT = "DAER-TX120";
    public static final String MERCHANT_DETAILS = "DAER-TX121";

    // FSS Error Codes
    public static final String FSS_JSON_REQ = "FSS-TX101";
    public static final String FSS_CONNECT_RESP = "FSS-TX102";
    public static final String PARSE_FSS_RESP = "FSS-TX103";

    // Service Error Code Constants.
    public static final String SLER_S1 = "SLER-D1";

    public static final String UNSUPPORTED_ENCODING_EXCEPTION = "CMER-USEC101";
    public static final String IO_PROBLEM = "CMER-IOE1";
    public static final String FILE_NOT_FOUND = "CMER-FNE2";
    public static final String URL_PATH_NOT_FOUND = "CMER-UNFE2";
    public static final String CLASS_NOT_FOUND = "CMER-CNFE3";

    public static final String TRANS_FAILED = "CMER-CE102";

    // Crypto Error Codes
    public static final String NO_SUCH_ALGORITHM = "SE-E1";
    public static final String NO_SUCH_PADDING = "SE-E2";
    public static final String INVALID_KEY = "SE-E3";
    public static final String ILLEGAL_BLOCK_SIZE = "SE-E4";
    public static final String BAD_PADDING = "SE-E5";
    public static final String INVALID_KEY_SPEC = "SE-E6";
    public static final String KEYSTORE_PROBLEM = "SE-E7";
    public static final String CERTIFICATE_PROBLEM = "SE-E8";
    public static final String UNRECOVERABLE_KEY = "SE-E9";
    public static final String KEYSTORE_NOT_FOUND = "SE-E10";
    public static final String KEYSTORE_PASS_PROBLEM = "SE-E11";

    // Common Error Codes
    public static String MERCHANT_NOT_ASSOCIATED = "PG-1001";
    public static String INVALID_CARDNMBR = "PG-1002";
    public static String INVALID_CARDEXPIRY = "PG-1003";
    public static String UNKNOWN_EXCEPTION = "00000";
    public static String INVALID_CVVNUMBER = "PG-1004";
    public static String INVALID_ADDRESS = "PG-1005";
    public static String INVALID_CITY = "PG-1006";
    public static String INVALID_PINCODE = "PG-1007";
    public static String BANKNAME_MANDATORY = "PG-1008";
    public static String BANKCODE_MANDATORY = "PG-1009";
    public static final String VALIDATION_CARD_DETAILS = "SLER-E15";
    public static String INVALID_PROMO_DETAILS = "PG-1010";
    public static String INVALID_CARD_TYPE = "PG-1011";
    public static final String INVALID_ENC_PARAMS = "PG-1012";

    public static final String CHECKSUM_INVALID = "330";
    public static final String CHECKSUM_MISMATCH = "VAER-CS102";
    public static final String CHECKSUM_REQ = "VAER-CS103";

    public static final String MID_NOT_FOUND = "Mid Not Found";

    private static ImmutableMap<String, ResponseConstants> alipayResponseImmutableMap;

    static {
        Builder<String, ResponseConstants> responseCodeMapBuilder = ImmutableMap.builder();
        for (ResponseConstants responseConstants : ResponseConstants.values()) {
            if (StringUtils.isNotBlank(responseConstants.getAlipayResultCode())) {
                responseCodeMapBuilder.put(responseConstants.getAlipayResultCode(), responseConstants);
            }
        }
        alipayResponseImmutableMap = responseCodeMapBuilder.build();
    }

    public static ImmutableMap<String, ResponseConstants> getAlipayResponseImmutableMap() {
        return alipayResponseImmutableMap;
    }

}
