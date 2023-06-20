package com.paytm.pgplus.biz.model.aoaorderlookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AOAOrderLookUpRequest {

    @NotBlank(message = "mid cannot be blank")
    @Length(max = 64, message = "mid cannot be more than 64 characters")
    private String mid;

    @NotBlank(message = "orderId cannot be blank")
    private String orderId;

    @NotBlank(message = "inquirer cannot be blank")
    private String inquirer;
}
