package com.paytm.pgplus.utils;

import java.util.ArrayList;
import java.util.List;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * Created by Naman on 05/06/17.
 */
public class MockUtils {

    public static String fetchMockResponse(String key) {

        return PropertiesUtil.getProperties().getProperty(key);
    }

    public static GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsSuccessFully() {

        List<CardBeanBiz> bizSavedCards = new ArrayList<>();

        for (int i = 0; i < 5; i++) {

            CardBeanBiz cardBeanBiz = new CardBeanBiz();
            cardBeanBiz.setCardId(2003039l);
            cardBeanBiz.setFirstSixDigit(471865l);
            cardBeanBiz.setFirstSixDigit(336l);
            cardBeanBiz.setCardType("CREDIT_CARD");
            cardBeanBiz.setInstId("HDFC");
            cardBeanBiz.setCardScheme("VISA");
            cardBeanBiz.setStatus(1);

            bizSavedCards.add(cardBeanBiz);
        }

        return new GenericCoreResponseBean<>(bizSavedCards);
    }
}
