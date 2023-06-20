/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @createdOn 28-Mar-2016
 * @author kesari
 */
public class ThemeInfo implements Serializable {

    private static final long serialVersionUID = 6333951476992720904L;
    @Tag(value = 1)
    private String channel;
    @Tag(value = 2)
    private String theme;
    @Tag(value = 3)
    private String subTheme;

    // Added by Naman
    @Tag(value = 4)
    private String loginTheme;

    public String getLoginTheme() {
        return loginTheme;
    }

    public void setLoginTheme(String loginTheme) {
        this.loginTheme = loginTheme;
    }

    /**
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * @param channel
     *            the channel to set
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme
     *            the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the subTheme
     */
    public String getSubTheme() {
        return subTheme;
    }

    /**
     * @param subTheme
     *            the subTheme to set
     */
    public void setSubTheme(String subTheme) {
        this.subTheme = subTheme;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ThemeInfo [channel=").append(channel).append(", theme=").append(theme).append(", subTheme=")
                .append(subTheme).append(", loginTheme=").append(loginTheme).append("]");
        return builder.toString();
    }

}
