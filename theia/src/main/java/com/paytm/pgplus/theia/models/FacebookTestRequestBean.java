package com.paytm.pgplus.theia.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankitsinghal on 09/07/17.
 */
@JsonIgnoreProperties
public class FacebookTestRequestBean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 2529355397497997941L;
    @JsonProperty("input1")
    private String input1;
    @JsonProperty("input2")
    private String input2;

    /**
     * 
     */
    public FacebookTestRequestBean() {
        super();
    }

    /**
     * @param input1
     * @param input2
     */
    public FacebookTestRequestBean(String input1, String input2) {
        super();
        this.input1 = input1;
        this.input2 = input2;
    }

    @Override
    public String toString() {
        return input1 + input2;
    }

    /**
     * @return the input1
     */
    public String getInput1() {
        return input1;
    }

    /**
     * @param input1
     *            the input1 to set
     */
    public void setInput1(String input1) {
        this.input1 = input1;
    }

    /**
     * @return the input2
     */
    public String getInput2() {
        return input2;
    }

    /**
     * @param input2
     *            the input2 to set
     */
    public void setInput2(String input2) {
        this.input2 = input2;
    }
}
