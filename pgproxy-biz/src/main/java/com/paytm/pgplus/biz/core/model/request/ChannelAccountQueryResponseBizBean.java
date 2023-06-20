package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;

/**
 * @author charu
 *
 */

public class ChannelAccountQueryResponseBizBean implements Serializable {

    private static final long serialVersionUID = -8161924767935752187L;

    private List<ChannelAccountView> channelAccountViews;

    public List<ChannelAccountView> getChannelAccountViews() {
        return channelAccountViews;
    }

    public void setChannelAccountViews(List<ChannelAccountView> channelAccountViews) {
        this.channelAccountViews = channelAccountViews;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelAccountQueryResponseBizBean{");
        sb.append("channelAccountViews=").append(channelAccountViews);
        sb.append('}');
        return sb.toString();
    }
}
