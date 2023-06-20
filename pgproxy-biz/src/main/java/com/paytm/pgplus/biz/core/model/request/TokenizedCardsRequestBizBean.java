package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.payment.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
public class TokenizedCardsRequestBizBean implements Serializable {

    private static final long serialVersionUID = 2556819078042358379L;

    private UserType targetType;

    private String merchantId;

    private String externalUserId;

    private String userId;

    private boolean includeExpiredTokens;

    private boolean includeExpiredCards;

    private boolean addNPayGlobalVaultCards;

    private boolean globalVaultCoftPreference;

    private String mid;

    private boolean isNativeAddMoney;

    private boolean isStaticQrCode;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TokenizedCardsRequestBizBean that = (TokenizedCardsRequestBizBean) o;
        return Objects.equals(targetType, that.targetType) && Objects.equals(merchantId, that.merchantId)
                && externalUserId.equals(that.externalUserId) && userId.equals(that.userId)
                && includeExpiredTokens == that.includeExpiredTokens && includeExpiredCards == that.includeExpiredCards
                && addNPayGlobalVaultCards == that.addNPayGlobalVaultCards
                && globalVaultCoftPreference == that.globalVaultCoftPreference && mid == that.mid
                && isNativeAddMoney == that.isNativeAddMoney && isStaticQrCode == that.isStaticQrCode;
    }
}
