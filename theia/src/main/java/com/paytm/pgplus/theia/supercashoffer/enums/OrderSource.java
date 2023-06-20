package com.paytm.pgplus.theia.supercashoffer.enums;

public enum OrderSource {
    PG("PG"), ORDER("ORDER");// order - onus, PG - offus

    private String value;

    OrderSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
