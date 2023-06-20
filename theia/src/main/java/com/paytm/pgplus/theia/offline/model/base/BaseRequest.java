/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseRequest implements Serializable {

    private static final long serialVersionUID = -9165702917193734540L;
}
