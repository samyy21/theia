package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeEnrollOneClickRequestBody implements Serializable {

    private static final long serialVersionUID = 4276162608636460298L;

    private Map<String, String> deEnrollContent;

    @JsonIgnore
    private String mid;

    @JsonIgnore
    private String referenceId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Map<String, String> getDeEnrollContent() {
        return deEnrollContent;
    }

    public void setDeEnrollContent(Map<String, String> deEnrollContent) {
        this.deEnrollContent = deEnrollContent;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("DeEnrollOneClickRequestBody {");
        sb.append(" deEnrollContent='").append(deEnrollContent.toString()).append('\'').append('}');
        return sb.toString();
    }

}
