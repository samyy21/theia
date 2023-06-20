package com.paytm.pgplus.theia.offline.facade;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.offline.model.payview.BalanceInfo;

import java.util.List;

/**
 * Created by rahulverma on 18/9/17.
 */
public interface ICommonFacade {

    String getLogoName(String bankId, EChannelId eChannelId);

    String getLogoName(BankInfoData bankInfoData, EChannelId eChannelId);

    boolean hasLowSuccessRate(String bankName, String payMethod);

    BalanceInfo getPaytmBankBalanceInfo(UserDetailsBiz userDetailsBiz);

    public String getLogoUrl(String cardScheme, EChannelId channelId);

    boolean hasLowSuccessRate(String bankName, String payMethod, SuccessRateCacheModel successRateCacheModel);

    public List<BankInfoData> getBankInfoDataListFromBankCodes(List<String> bankCodeList);

    String getLogoNameV1(String bankId);

    String getLogoName(String imageName);

    String getLogoNameV2(String bankId);

    String getBaseIconUrl();

    String getBankLogo(String bankCode);
}
