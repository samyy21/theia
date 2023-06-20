package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.LitePayviewConsultType;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.PayMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Created  by  charu  on  07/02/20.
 */

/**
 * Role : to perform reconcilation of saved asset for saved card service and
 * platform
 */

@Service
public class SavedAssetMigrationReconUtil {

    private static final Logger STATS_LOGGER = LoggerFactory.getLogger("STATS_LOGGER");

    private static final Logger LOGGER = LoggerFactory.getLogger(SavedAssetMigrationReconUtil.class);

    public void assetRecon(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean flowResponseBean) {
        // logged in flow
        if (transBean.getUserDetails() != null) {
            if (flowResponseBean.getMerchnatLiteViewResponse() != null
                    && !CollectionUtils.isEmpty(flowResponseBean.getMerchnatLiteViewResponse().getPayMethodViews())) {
                setReconStatus(transBean, flowResponseBean.getUserDetails().getMerchantViewSavedCardsList(),
                        flowResponseBean.getMerchnatLiteViewResponse().getPayMethodViews(),
                        LitePayviewConsultType.MerchantLitePayViewConsult);
            }
            if (transBean.isAssetReconcilationStatus() && flowResponseBean.getAddAndPayLiteViewResponse() != null
                    && !CollectionUtils.isEmpty(flowResponseBean.getAddAndPayLiteViewResponse().getPayMethodViews()))
                setReconStatus(transBean, flowResponseBean.getUserDetails().getAddAndPayViewSavedCardsList(),
                        flowResponseBean.getAddAndPayLiteViewResponse().getPayMethodViews(),
                        LitePayviewConsultType.AddnPayLitePayViewConsult);
        }
        // not logged in flow
        else {

            /**
             * if assets from savedcardservice couldn't be fetched due to
             * exception, then we initiate MidCustIdCardBizDetails and
             * setMerchantCustomerCardList with new list in order to check recon
             * status.
             */

            if (null == flowResponseBean.getmIdCustIdCardBizDetails()) {
                MidCustIdCardBizDetails midCustIdCardBizDetails = new MidCustIdCardBizDetails();
                midCustIdCardBizDetails.setMerchantCustomerCardList(new ArrayList<CardBeanBiz>());
                flowResponseBean.setmIdCustIdCardBizDetails(midCustIdCardBizDetails);
            }
            setReconStatus(transBean, flowResponseBean.getmIdCustIdCardBizDetails().getMerchantCustomerCardList(),
                    flowResponseBean.getMerchnatLiteViewResponse().getPayMethodViews(),
                    LitePayviewConsultType.MerchantLitePayViewConsult);
        }

    }

    private void setReconStatus(WorkFlowTransactionBean transBean, List<CardBeanBiz> savedCardsFromService,
            List<PayMethodViewsBiz> litePayviewConsultList, LitePayviewConsultType litePayviewConsultType) {
        if (CollectionUtils.isEmpty(litePayviewConsultList))
            return;
        List<CardBeanBiz> enabledSavedCardsFromService = CollectionUtils.isEmpty(savedCardsFromService) ? new ArrayList<>()
                : savedCardsFromService.stream().filter(cardBeanBiz -> !cardBeanBiz.isDisabled())
                        .collect(Collectors.toList());
        List<PayMethodViewsBiz> creditCardPayMethodsInfo = litePayviewConsultList.stream()
                .filter(payView -> PayMethod.CREDIT_CARD.getMethod().equals(payView.getPayMethod()))
                .collect(Collectors.toList());
        List<PayMethodViewsBiz> debitCardPayMethodsInfo = litePayviewConsultList.stream()
                .filter(payView -> PayMethod.DEBIT_CARD.getMethod().equals(payView.getPayMethod()))
                .collect(Collectors.toList());
        List<PayCardOptionViewBiz> assetsFromPlatform = new LinkedList<>();
        if (!CollectionUtils.isEmpty(debitCardPayMethodsInfo)) {
            assetsFromPlatform.addAll(debitCardPayMethodsInfo.get(0).getPayCardOptionViews().stream()
                    .filter(payCard -> payCard.isEnableStatus()).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(creditCardPayMethodsInfo)) {
            assetsFromPlatform.addAll(creditCardPayMethodsInfo.get(0).getPayCardOptionViews().stream()
                    .filter(payCard -> payCard.isEnableStatus()).collect(Collectors.toList()));
        }
        if (!doReconciliation(enabledSavedCardsFromService, assetsFromPlatform, litePayviewConsultType, transBean)) {
            transBean.setAssetReconcilationStatus(false);

        }
    }

    private boolean doReconciliation(List<CardBeanBiz> assetsFromSC, List<PayCardOptionViewBiz> savedCardsFromPlatform,
            LitePayviewConsultType litePayviewConsultType, WorkFlowTransactionBean transBean) {

        boolean reconcilationStatus = true;
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null;
        String mid = null != transBean.getWorkFlowBean().getPaytmMID() ? transBean.getWorkFlowBean().getPaytmMID()
                : null;
        String custId = null != transBean.getWorkFlowBean().getCustID() ? transBean.getWorkFlowBean().getCustID()
                : null;
        if (CollectionUtils.isEmpty(savedCardsFromPlatform)) {
            if (CollectionUtils.isEmpty(assetsFromSC)) {
                LOGGER.info(
                        "{} recon_success, but no assets returned from platform and savedCardService for for userId {}, mid {}, custId {} ",
                        litePayviewConsultType.getValue(), userId, mid, custId);
                return true;
            }
            LOGGER.error("{} recon_failed no assets returned from platform for userId {}, mid {}, custId {} ",
                    litePayviewConsultType.getValue(), userId, mid, custId);
            STATS_LOGGER.error("{}, NUM_CARD_MISMATCH ,{}, {}, {}-{}, {}", litePayviewConsultType.getValue(), 0,
                    userId, mid, custId);
            return false;

        } else if (CollectionUtils.isEmpty(assetsFromSC)) {
            LOGGER.error("{} recon_failed no assets returned from service for userId {}, mid {}, custId {} ",
                    litePayviewConsultType.getValue(), userId, mid, custId);
            STATS_LOGGER.error("{}, NUM_CARD_MISMATCH ,{}, {}, {}-{}, {}", litePayviewConsultType.getValue(),
                    savedCardsFromPlatform.size(), 0, userId, mid, custId);
            return false;
        } else if (savedCardsFromPlatform.size() != assetsFromSC.size()) {
            LOGGER.error(
                    "{} recon_failed , number of assets from savedCardService {} , from platform {}, for userId {}, mid {}, custId {} ",
                    litePayviewConsultType.getValue(), assetsFromSC.size(), savedCardsFromPlatform.size(), userId, mid,
                    custId);
            STATS_LOGGER.error("{}, NUM_CARD_MISMATCH ,{}, {}, {}-{}, {}", litePayviewConsultType.getValue(),
                    savedCardsFromPlatform.size(), assetsFromSC.size(), userId, mid, custId);
            return false;
        }
        for (CardBeanBiz cardBeanBiz : assetsFromSC) {
            if (!checkAndCompareData(cardBeanBiz, savedCardsFromPlatform, litePayviewConsultType)) {

                reconcilationStatus = false;
            }
        }
        return reconcilationStatus;
    }

    private boolean checkAndCompareData(CardBeanBiz cardBeanBiz, List<PayCardOptionViewBiz> assetListFromPlatform,
            LitePayviewConsultType litePayviewConsultType) {
        for (PayCardOptionViewBiz fromPlatform : assetListFromPlatform) {
            if (StringUtils.equals(cardBeanBiz.getCardIndexNo(), fromPlatform.getCardIndexNo())) {
                // doFieldWiseOperation
                if (compareFields(String.valueOf(cardBeanBiz.getFirstSixDigit()), fromPlatform.getMaskedCardNo()
                        .substring(0, 6), "CARD_NUM_MISMATCH", cardBeanBiz, litePayviewConsultType)
                        && compareFields(getLastFourDigits(cardBeanBiz.getLastFourDigit()), fromPlatform
                                .getMaskedCardNo().substring(fromPlatform.getMaskedCardNo().length() - 4),
                                "CARD_NUM_MISMATCH", cardBeanBiz, litePayviewConsultType)
                        && compareFields(cardBeanBiz.getExpiryDate(),
                                fromPlatform.getExpiryMonth() + fromPlatform.getExpiryYear(), "EXPIRY_MISMATCH",
                                cardBeanBiz, litePayviewConsultType)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        LOGGER.error("{}, recon_failed Not found cardIndexNumber {}, for  mid {}, custId {}, userId {} ",
                litePayviewConsultType.getValue(), cardBeanBiz.getCardIndexNo(), cardBeanBiz.getmId(),
                cardBeanBiz.getCustId(), cardBeanBiz.getUserId());
        STATS_LOGGER.error("{}, recon_failed CARDINDEX_NOT_FOUND , ,{}, {}-{}, {}", litePayviewConsultType.getValue(),
                cardBeanBiz.getCardIndexNo(), cardBeanBiz.getmId(), cardBeanBiz.getCustId(), cardBeanBiz.getUserId());
        return false;

    }

    private String getLastFourDigits(Long lastFourDigit) {
        if (lastFourDigit > 999) {
            return lastFourDigit.toString();
        }
        String lastFourDigitsPadded = new StringBuilder().append("0000").append(lastFourDigit).toString();
        return lastFourDigitsPadded.substring(lastFourDigitsPadded.length() - 4);
    }

    private boolean compareFields(String s1, String s2, String event, CardBeanBiz cardBeanBiz,
            LitePayviewConsultType litePayviewConsultType) {
        if (StringUtils.equals(s1, s2)) {
            return true;
        }
        LOGGER.error("{} recon_failed {} for  cardIndexNumber {}, for  mid {}, custId {}, userId {} ",
                litePayviewConsultType.getValue(), event, cardBeanBiz.getCardIndexNo(), cardBeanBiz.getmId(),
                cardBeanBiz.getCustId(), cardBeanBiz.getUserId());
        STATS_LOGGER.error("{}, recon_failed {}, {},  ,{}-{}, {}", litePayviewConsultType.getValue(), event,
                cardBeanBiz.getCardIndexNo(), cardBeanBiz.getmId(), cardBeanBiz.getCustId(), cardBeanBiz.getUserId());

        return false;

    }
}