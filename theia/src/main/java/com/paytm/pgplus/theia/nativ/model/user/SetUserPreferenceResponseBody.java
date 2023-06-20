/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.model.user;

import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

public class SetUserPreferenceResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 6188140719054367057L;

    private String message;

    public SetUserPreferenceResponseBody() {
    }

    public SetUserPreferenceResponseBody(ResultInfo resultInfo, String message) {
        super(resultInfo);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetUserPreferenceResponseBody{");
        sb.append("resultInfo=").append(super.toString());
        sb.append(", message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}