package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.coft.model.CoftSavedCards;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.CREDIT_CARD;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DEBIT_CARD;

@Service("filterTokenizedAndPlatformCardsForAoaTask")
public class FilterTokenizedAndPlatformCardsForAoaTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(FilterTokenizedAndPlatformCardsForAoaTask.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterTokenizedAndPlatformCardsForAoaTask.class);

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private CoftUtil coftUtil;

    final SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy");

    @Override
    protected GenericCoreResponseBean<LitePayviewConsultResponseBizBean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) throws BaseException {

        LitePayviewConsultResponseBizBean liteViewConsult = transBean.getMerchantLiteViewConsult();
        if (liteViewConsult == null || CollectionUtils.isEmpty(liteViewConsult.getPayMethodViews()))
            return new GenericCoreResponseBean<>(liteViewConsult);

        // Preprocessing to keep COFT payment and channel mapping
        Map<String, Boolean> channelCoftPaymentMapping = new HashMap<>();
        for (PayMethodViewsBiz payMethodView : liteViewConsult.getPayMethodViews()) {
            if (payMethodView != null
                    && (CREDIT_CARD.equals(payMethodView.getPayMethod()) || DEBIT_CARD.equals(payMethodView
                            .getPayMethod())) && !CollectionUtils.isEmpty(payMethodView.getPayChannelOptionViews())) {
                LOGGER.info("paymethod is {} and paychanneloptions is {}", payMethodView.getPayMethod(),
                        payMethodView.getPayChannelOptionViews());
                for (PayChannelOptionViewBiz channel : payMethodView.getPayChannelOptionViews()) {
                    if (!CollectionUtils.isEmpty(channel.getSupportPayOptionSubTypes())) {
                        EXT_LOGGER.customInfo("Coft supported for channel is {}", channel.getSupportPayOptionSubTypes()
                                .contains("COFT"));
                        channelCoftPaymentMapping.put(channel.getPayOption(), channel.getSupportPayOptionSubTypes()
                                .contains("COFT"));
                    }
                }
            }
        }
        List<CoftSavedCards> platformAndTokenCards = transBean.getPlatformAndTokenCards();
        applyDedupOnCinAndUpdateSaveCardDetailsInLitePayview(platformAndTokenCards, transBean,
                channelCoftPaymentMapping);
        liteViewConsult.setChannelCoftPayment(channelCoftPaymentMapping);
        return new GenericCoreResponseBean<>(liteViewConsult);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FILTER_AOA_TOKENIZED_AND_PLATFORM_CARDS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FILTER_TOKENIZED_PLATFORM_CARDS_TIME, "2000"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {

        return requestBean.isFromAoaMerchant()
                && iPgpFf4jClient.checkWithdefault("theia.getTokenizedCardsInFPO", null, true);
    }

    private void applyDedupOnCinAndUpdateSaveCardDetailsInLitePayview(List<CoftSavedCards> platformAndTokenCards,
            WorkFlowTransactionBean input, Map<String, Boolean> channelCoftPaymentMapping) {
        if (platformAndTokenCards != null) {
            Map<String, CoftSavedCards> tokenCards = new HashMap<>(); // contain
                                                                      // both
                                                                      // token
                                                                      // and
                                                                      // platform
                                                                      // card.
            List<CoftSavedCards> platformCard = new ArrayList<>();
            for (int i = 0; i < platformAndTokenCards.size(); i++) {
                CoftSavedCards c = platformAndTokenCards.get(i);
                if (c.isCardCoft() && "ACTIVE".equals(c.getTokenStatus())) {
                    if (tokenCards.containsKey(c.getGlobalPanIndex())) {
                        updateTokenCardBasedOnExpiry(c, tokenCards.get(c.getGlobalPanIndex()), tokenCards);
                    } else {
                        tokenCards.put(c.getGlobalPanIndex(), c);
                    }
                } else if (!c.isCardCoft()) {
                    platformCard.add(c);
                }
            }
            Map<String, CoftSavedCards> uniquePlatFormAndTokenCard = comparePlatformAndTokenCards(platformCard,
                    tokenCards, channelCoftPaymentMapping, input);
            updateSaveCardDetailsInLitePayview(uniquePlatFormAndTokenCard, tokenCards, input, channelCoftPaymentMapping);
        }

    }

    private Map<String, CoftSavedCards> comparePlatformAndTokenCards(List<CoftSavedCards> platformCards,
            Map<String, CoftSavedCards> tokenCards, Map<String, Boolean> channelCoftPaymentMapping,
            WorkFlowTransactionBean input) {
        Map<String, CoftSavedCards> uniquePlatFormAndTokenCard = new HashMap<>();
        boolean returnTokenCards;
        if (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(input.getWorkFlowBean().getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.equals(input.getWorkFlowBean().getRequestType())) {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION, input
                    .getWorkFlowBean().getPaytmMID());
        } else {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS, input.getWorkFlowBean()
                    .getPaytmMID());
        }
        for (CoftSavedCards platformCard : platformCards) {
            if (tokenCards.containsKey(platformCard.getGlobalPanIndex())) {
                if (!returnTokenCards) {
                    platformCard.setEligibleForCoft(false);
                    uniquePlatFormAndTokenCard.put(platformCard.getGlobalPanIndex(), platformCard);
                } else {
                    CoftSavedCards p = prioritizeCardBasedOnCoftPaymentSupport(platformCard,
                            tokenCards.get(platformCard.getGlobalPanIndex()), channelCoftPaymentMapping, input
                                    .getWorkFlowBean().getPaytmMID());
                    p.setEligibleForCoft(false);
                    uniquePlatFormAndTokenCard.put(platformCard.getGlobalPanIndex(), p);
                }
            } else {
                uniquePlatFormAndTokenCard.put(platformCard.getGlobalPanIndex(), platformCard);
            }
        }

        if (MapUtils.isNotEmpty(tokenCards) && returnTokenCards) {
            for (String tin : tokenCards.keySet()) {
                if (!uniquePlatFormAndTokenCard.containsKey(tin)) {
                    uniquePlatFormAndTokenCard.put(tin, tokenCards.get(tin));
                }
            }
        }

        return uniquePlatFormAndTokenCard;
    }

    private CoftSavedCards prioritizeCardBasedOnCoftPaymentSupport(CoftSavedCards platFormCard,
            CoftSavedCards tokenCard, Map<String, Boolean> channelCoftPaymentMapping, String paytmMID) {
        if (checkForCoftPaymentSupport(channelCoftPaymentMapping, tokenCard, paytmMID))
            return tokenCard;
        else
            return platFormCard;
    }

    private boolean checkForCoftPaymentSupport(Map<String, Boolean> channelCoftPaymentMapping, CoftSavedCards card,
            String paytmMID) {
        String cardType = BizConstant.CC.equalsIgnoreCase(card.getCardType()) ? CREDIT_CARD : DEBIT_CARD;
        String payOption = cardType + "_" + card.getCardScheme();
        if (channelCoftPaymentMapping.get(payOption) != null && channelCoftPaymentMapping.get(payOption)) {
            return true;
        }
        return false;
    }

    private void updateTokenCardBasedOnExpiry(CoftSavedCards fromList, CoftSavedCards fromMap,
            Map<String, CoftSavedCards> uniqueCards) {
        try {
            if (!StringUtils.isEmpty(fromMap.getExpiryDate()) && !StringUtils.isEmpty(fromList.getExpiryDate())) {
                if (sdf.parse(fromMap.getExpiryDate()).compareTo(sdf.parse(fromList.getExpiryDate())) < 0) {
                    uniqueCards.put(fromList.getGlobalPanIndex(), fromList);
                }
            }
        } catch (ParseException e) {
            LOGGER.error("Exception occurred while parsing token expiry : ", e);
        }
    }

    private void updateSaveCardDetailsInLitePayview(Map<String, CoftSavedCards> uniquePlatFormAndTokenCard,
            Map<String, CoftSavedCards> tokenCards, WorkFlowTransactionBean input,
            Map<String, Boolean> channelCoftPaymentMapping) {
        PayMethodViewsBiz payMethodViewCC = null;
        PayMethodViewsBiz payMethodViewDC = null;
        for (PayMethodViewsBiz payMethodView : input.getMerchantLiteViewConsult().getPayMethodViews()) {
            if (CREDIT_CARD.equals(payMethodView.getPayMethod())) {
                payMethodViewCC = payMethodView;
                List<PayCardOptionViewBiz> payCardOptionViews = new ArrayList<>();
                payMethodViewCC.setPayCardOptionViews(payCardOptionViews);
            }
            if (DEBIT_CARD.equals(payMethodView.getPayMethod())) {
                payMethodViewDC = payMethodView;
                List<PayCardOptionViewBiz> payCardOptionViews = new ArrayList<>();
                payMethodViewDC.setPayCardOptionViews(payCardOptionViews);
            }
        }
        boolean returnTokenCards;
        if (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(input.getWorkFlowBean().getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.equals(input.getWorkFlowBean().getRequestType())) {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION, input
                    .getWorkFlowBean().getPaytmMID());
        } else {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS, input.getWorkFlowBean()
                    .getPaytmMID());
        }
        Map<String, List<String>> coftEligibilityMerchantStatus = mappingUtil.fillCoftEligibilityMIDMap(input
                .getWorkFlowBean().getPaytmMID());
        for (Map.Entry<String, CoftSavedCards> entry : uniquePlatFormAndTokenCard.entrySet()) {
            CoftSavedCards card = entry.getValue();
            if (card != null && !card.isCardCoft() && !tokenCards.containsKey(card.getGlobalPanIndex())) { // not
                // tokenized
                checkCoftEligiblity(input, coftEligibilityMerchantStatus, card);
            }
            populateDetailsOfCards(card, payMethodViewCC, payMethodViewDC, tokenCards, channelCoftPaymentMapping, input);
            /*
             * if (!returnTokenCards) { // means all card are platform card if
             * (card != null && !card.isCardCoft() &&
             * !tokenCards.containsKey(card.getCin())) { // not tokenized
             * checkCoftEligiblity(input, coftEligibilityMerchantStatus, card);
             * populateDetailsOfCards(card, payMethodViewCC, payMethodViewDC,
             * tokenCards, channelCoftPaymentMapping); } } else { // check card
             * not coft populateDetailsOfCards(card, payMethodViewCC,
             * payMethodViewDC, tokenCards, channelCoftPaymentMapping); }
             */
        }
    }

    private void populateDetailsOfCards(CoftSavedCards card, PayMethodViewsBiz payMethodViewCC,
            PayMethodViewsBiz payMethodViewDC, Map<String, CoftSavedCards> tokenCards,
            Map<String, Boolean> channelCoftPaymentMappings, WorkFlowTransactionBean input) {
        PayCardOptionViewBiz payCardOptionViewBiz = coftUtil.populateDetailsOfTokenOrPlatformCard(card);
        if (tokenCards.containsKey(card.getGlobalPanIndex())) {
            payCardOptionViewBiz.setCardTokenized(true);
        } else {
            payCardOptionViewBiz.setCardTokenized(false);
        }
        payCardOptionViewBiz.setCoftPaymentSupported(checkForCoftPaymentSupport(channelCoftPaymentMappings, card, input
                .getWorkFlowBean().getPaytmMID()));
        if (BizConstant.CC.equalsIgnoreCase(card.getCardType()) && payMethodViewCC != null) {
            payMethodViewCC.getPayCardOptionViews().add(payCardOptionViewBiz);
        } else if (BizConstant.DC.equalsIgnoreCase(card.getCardType()) && payMethodViewDC != null) {
            payMethodViewDC.getPayCardOptionViews().add(payCardOptionViewBiz);
        }
    }

    private void checkCoftEligiblity(WorkFlowTransactionBean input,
            Map<String, List<String>> coftEligibilityMerchantStatus, CoftSavedCards pCard) {
        boolean isEligibleForCoft = pCard.isEligibleForCoft();
        boolean binEligibleForCoftPref = input.getWorkFlowBean().isBinEligibleForCoft();
        if (binEligibleForCoftPref && null != pCard.getExtendInfo()) {
            boolean isBinEligibleForCoft = Boolean.parseBoolean(pCard.getExtendInfo().get(
                    BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE));
            isEligibleForCoft = isEligibleForCoft && isBinEligibleForCoft;
            pCard.setEligibleForCoft(isEligibleForCoft
                    && (ff4JUtil.isGlobalVaultEnabled(input, input.getWorkFlowBean().getPaytmMID()) || checkCardSchemeEligibility(
                            coftEligibilityMerchantStatus, input.getWorkFlowBean().getPaytmMID(), pCard.getCardScheme())));
        }
    }

    public boolean checkCardSchemeEligibility(Map<String, List<String>> coftEligibilityMerchantStatus, String mid,
            String cardScheme) {
        return coftEligibilityMerchantStatus.get(mid) != null
                && coftEligibilityMerchantStatus.get(mid).contains(cardScheme);
    }

}
