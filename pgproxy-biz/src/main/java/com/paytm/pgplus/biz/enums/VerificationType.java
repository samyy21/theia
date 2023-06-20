package com.paytm.pgplus.biz.enums;

public enum VerificationType {
    TPV("TPV", "third party verification"), NON_TPV("NON_TPV", "non third party verification");

    private String type;
    private String description;

    VerificationType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VerificationType{");
        sb.append("type='").append(type).append('\'');
        sb.append(",");
        sb.append("description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
