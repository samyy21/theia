package com.paytm.pgplus.theia.models.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankitsinghal on 09/07/17.
 */
@JsonIgnoreProperties
public class FacebookTestResponseBean implements Serializable {
    /**
     * 
     */
    public FacebookTestResponseBean() {
        super();
    }

    /**
     * @param output
     */
    public FacebookTestResponseBean(String output) {
        super();
        this.output = output;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 6611199951164357098L;
    @JsonProperty("output")
    private String output;

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /**
     * @param output
     *            the output to set
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FacebookTestResponseBean [output=").append(output).append("]");
        return builder.toString();
    }
}
