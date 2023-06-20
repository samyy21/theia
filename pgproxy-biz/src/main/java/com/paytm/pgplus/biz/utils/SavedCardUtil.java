package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SavedCardUtil {

    public SavedAssetInfo fetchCardInfoFromQueryAsset(Map<String, List<SavedAssetInfo>> savedCardsListWithPayMethod,
            String cardIndexNumber) {
        SavedAssetInfo savedAssetInfo = null;

        if (savedCardsListWithPayMethod != null && savedCardsListWithPayMethod.size() > 0) {
            List<SavedAssetInfo> savedCardList = savedCardsListWithPayMethod.values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList());
            savedAssetInfo = checkForCardIndexNumberPresent(savedCardList, cardIndexNumber);
        }
        return savedAssetInfo;
    }

    private SavedAssetInfo checkForCardIndexNumberPresent(List<SavedAssetInfo> assetInfos, String cardIndexNumber) {
        SavedAssetInfo savedAssetInfo = assetInfos.stream()
                .filter(assetInfo -> cardIndexNumber.equals(assetInfo.getCardIndexNo())).findAny().orElse(null);
        return savedAssetInfo;
    }
}
