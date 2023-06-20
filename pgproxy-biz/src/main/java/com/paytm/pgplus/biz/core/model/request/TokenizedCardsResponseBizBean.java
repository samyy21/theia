package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.payment.models.response.CardInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class TokenizedCardsResponseBizBean implements Serializable {

    private static final long serialVersionUID = 2556819078042358379L;

    private List<CardInfo> cardInfos;
}
