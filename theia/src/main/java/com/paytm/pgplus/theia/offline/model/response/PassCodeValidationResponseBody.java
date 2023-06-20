/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by rahulverma on 13/10/17.
 */
public class PassCodeValidationResponseBody extends ResponseBody {

    private static final long serialVersionUID = -4025469846392094527L;

    @NotBlank
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PassCodeValidationResponseBody [accessToken=");
        builder.append(accessToken);
        builder.append("]");
        return builder.toString();
    }

}
