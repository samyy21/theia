package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateCardRequestBody implements Serializable {

    private static final long serialVersionUID = -2594758377789201214L;
    @Mask(prefixNoMaskLen = 6)
    @NotBlank(message = "bin cannot be blank")
    @Length(min = 6, message = "bin cannot be less than 6 characters")
    private String bin;
    private String mid;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    /*
     * @JsonProperty("mid") private String mid;
     */

    public ValidateCardRequestBody() {
    }

    /*
     * public String getMid() { return mid; }
     * 
     * public void setMid(String mid) { this.mid = mid; }
     */

    public ValidateCardRequestBody(String bin) {
        this.bin = bin;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
