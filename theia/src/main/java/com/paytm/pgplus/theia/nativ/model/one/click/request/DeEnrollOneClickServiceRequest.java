package com.paytm.pgplus.theia.nativ.model.one.click.request;

import java.io.Serializable;
import java.util.Map;

public class DeEnrollOneClickServiceRequest implements Serializable {

    private static final long serialVersionUID = 403211314314398045L;

    private Map<String, String> deEnrollContent;

    public DeEnrollOneClickServiceRequest() {

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Map<String, String> getDeEnrollContent() {
        return deEnrollContent;
    }

    public void setDeEnrollContent(Map<String, String> deEnrollContent) {
        this.deEnrollContent = deEnrollContent;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeEnrollOneClickServiceRequest{");
        sb.append("deEnrollContent=").append(deEnrollContent);
        sb.append('}');
        return sb.toString();
    }
}