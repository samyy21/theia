package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.payment.models.request.CardDetail;
import com.paytm.pgplus.facade.payment.models.request.FetchCardLimitsRequest;
import com.paytm.pgplus.facade.payment.models.response.CardLimit;
import com.paytm.pgplus.facade.payment.models.response.FetchCardLimitsResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paytm.pgplus.biz.utils.BizConstant.*;

@Service("savedCardLimitWorkFlowHelper")
public class SavedCardLimitWorkFlowHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(SavedCardLimitWorkFlowHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SavedCardLimitWorkFlowHelper.class);

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    public Boolean fetchLimitsForSavedCards(WorkFlowTransactionBean transactionBean) {
        if (transactionBean.getMerchantLiteViewConsult() != null
                && transactionBean.getMerchantLiteViewConsult().getPayMethodViews() != null) {
            EXT_LOGGER.info("WorkflowTransBean before saved card limit application : {}", transactionBean);
            return getSavedCardLimits(transactionBean);
        }
        return false;
    }

    private Boolean getSavedCardLimits(WorkFlowTransactionBean flowTransactionBean) {
        try {
            List<PayMethodViewsBiz> payMethodViewsBizs = flowTransactionBean.getMerchantLiteViewConsult()
                    .getPayMethodViews();
            List<PayCardOptionViewBiz> ccSavedCardList = new ArrayList<>();
            List<PayCardOptionViewBiz> dcSavedCardList = new ArrayList<>();
            List<PayCardOptionViewBiz> savedCardList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(payMethodViewsBizs)) {
                for (PayMethodViewsBiz paymethod : payMethodViewsBizs) {
                    if (EPayMethod.CREDIT_CARD.getMethod().equals(paymethod.getPayMethod())
                            && paymethod.getPayCardOptionViews() != null) {
                        ccSavedCardList = paymethod.getPayCardOptionViews();
                    }
                    if (EPayMethod.DEBIT_CARD.getMethod().equals(paymethod.getPayMethod())
                            && paymethod.getPayCardOptionViews() != null) {
                        dcSavedCardList = paymethod.getPayCardOptionViews();
                    }
                }
                savedCardList.addAll(ccSavedCardList);
                savedCardList.addAll(dcSavedCardList);
                return fetchSavedCardsLimitsResponse(flowTransactionBean, savedCardList);
            }
        } catch (Exception e) {
            LOGGER.error("Error Occurred while fetching limits for saved card.");
        }
        return false;
    }

    private FetchCardLimitsRequest fetchSavedCardLimitsRequest(List<PayCardOptionViewBiz> savedCardList, String mid) {
        FetchCardLimitsRequest fetchCardLimitsRequest = new FetchCardLimitsRequest();
        List<CardDetail> cardDetailList = new ArrayList<>();
        BinDetail binDetail = null;
        if (CollectionUtils.isNotEmpty(savedCardList)) {
            for (PayCardOptionViewBiz savedCard : savedCardList) {
                CardDetail cardDetail = new CardDetail();
                try {
                    binDetail = cardUtils.fetchBinDetails(savedCard.getCardBin());
                    EXT_LOGGER.info("Bin details for this card is: {}", binDetail);
                    cardDetail.setScheme(savedCard.getCardScheme());
                    cardDetail.setBinId(savedCard.getCardIndexNo());
                    if (binDetail != null) {
                        if (EPayMethod.CREDIT_CARD.getMethod().equals(binDetail.getCardType())) {
                            cardDetail.setCardType(CC);
                        } else if (EPayMethod.DEBIT_CARD.getMethod().equals(binDetail.getCardType())) {
                            cardDetail.setCardType(DC);
                        }
                        cardDetail.setDomesticCard(binDetail.getIsIndian());
                        cardDetail.setPrepaidCard(binDetail.isPrepaidCard());
                        cardDetail.setCorporateCard(binDetail.isCorporateCard());
                    }
                    cardDetailList.add(cardDetail);
                } catch (PaytmValidationException e) {
                    LOGGER.error("Error while fetching card details : {}", e);
                }
            }
            fetchCardLimitsRequest.setMerchantId(mid);
            fetchCardLimitsRequest.setCardDetails(cardDetailList);
        }
        return fetchCardLimitsRequest;
    }

    private boolean fetchSavedCardsLimitsResponse(WorkFlowTransactionBean flowTransactionBean,
            List<PayCardOptionViewBiz> savedCardList) {
        String mid = flowTransactionBean.getWorkFlowBean().getPaytmMID();
        FetchCardLimitsRequest fetchCardLimitsRequest = fetchSavedCardLimitsRequest(savedCardList, mid);
        if (fetchCardLimitsRequest.getCardDetails() != null && fetchCardLimitsRequest.getMerchantId() != null) {
            FetchCardLimitsResponse fetchCardLimitsResponse = bizPaymentService.fetchCardLimit(fetchCardLimitsRequest);
            if (fetchCardLimitsResponse != null && fetchCardLimitsResponse.getResultInfo() != null
                    && StringUtils.equals(SUCCESS, fetchCardLimitsResponse.getResultInfo().getResultCode())) {
                setSavedCardRemainingLimit(fetchCardLimitsResponse, flowTransactionBean);
                return true;
            }
        }
        return false;
    }

    private void setSavedCardRemainingLimit(FetchCardLimitsResponse fetchCardLimitsResponse,
            WorkFlowTransactionBean flowTransactionBean) {
        List<CardLimit> limitList = fetchCardLimitsResponse.getCardLimits();
        HashMap<String, String> limitMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(limitList)) {
            for (CardLimit cardLimit : limitList) {
                limitMap.put(cardLimit.getBinId(), cardLimit.getRemainingLimit());
            }
        }
        if (MapUtils.isNotEmpty(limitMap)) {
            List<PayMethodViewsBiz> payMethodViewsBizs = flowTransactionBean.getMerchantLiteViewConsult()
                    .getPayMethodViews();
            if (CollectionUtils.isNotEmpty(payMethodViewsBizs)) {
                for (PayMethodViewsBiz paymethod : payMethodViewsBizs) {
                    if ((EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymethod.getPayMethod()) || EPayMethod.CREDIT_CARD
                            .getMethod().equalsIgnoreCase(paymethod.getPayMethod()))
                            && paymethod.getPayCardOptionViews() != null) {
                        for (PayCardOptionViewBiz savedCard : paymethod.getPayCardOptionViews()) {
                            if (limitMap.containsKey(savedCard.getCardIndexNo())) {
                                savedCard.setRemainingLimit(limitMap.get(savedCard.getCardIndexNo()));
                            }
                        }
                    }
                }
            }
            LOGGER.info("Remaining Limit Applied on Saved Cards : {}", limitMap.toString());
        }
    }
}
