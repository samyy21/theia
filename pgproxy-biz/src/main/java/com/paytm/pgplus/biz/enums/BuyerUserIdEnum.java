package com.paytm.pgplus.biz.enums;/*
 *  @author prakharsangal
 *  @version BuyerUserIdEnum.java: , v0.1 14/09/20 prakharsangal Exp $$
 *
 */

public enum BuyerUserIdEnum {

    /***
     * TRUE - User will change in the Transaction flow" False - User will not
     * change in the Transaction flow" FETCH_FROM_CONTRACT - Behaviour will be
     * based on merchant contract"
     */
    TRUE("TRUE"), FALSE("FALSE"), FETCH_FROM_CONTRACT("FETCH_FROM_CONTRACT");

    String value = "";

    BuyerUserIdEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
