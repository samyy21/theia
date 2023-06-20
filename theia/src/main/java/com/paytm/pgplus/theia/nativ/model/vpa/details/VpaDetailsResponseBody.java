package com.paytm.pgplus.theia.nativ.model.vpa.details;

import java.util.List;

import com.paytm.pgplus.response.BaseResponseBody;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpaDetailsResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;

    @JsonProperty("sarvatraUserProfile")
    private UserProfileSarvatra sarvatraUserProfile;

    @ApiModelProperty(required = true)
    @NotEmpty
    private List<String> sarvatraVpa;

    public UserProfileSarvatra getSarvatraUserProfile() {
        return sarvatraUserProfile;
    }

    public void setSarvatraUserProfile(UserProfileSarvatra sarvatraUserProfile) {
        this.sarvatraUserProfile = sarvatraUserProfile;
    }

    public List<String> getSarvatraVpa() {
        return sarvatraVpa;
    }

    public void setSarvatraVpa(List<String> sarvatraVpa) {
        this.sarvatraVpa = sarvatraVpa;
    }

    public VpaDetailsResponseBody() {
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VpaDetailsResponseBody [sarvatraUserProfile=").append(sarvatraUserProfile)
                .append(super.toString()).append("]");
        return builder.toString();
    }

}
