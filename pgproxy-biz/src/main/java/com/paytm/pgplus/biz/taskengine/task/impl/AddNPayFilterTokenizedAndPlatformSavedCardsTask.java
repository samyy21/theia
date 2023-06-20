package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.*;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.payment.models.response.CardInfo;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.COFT_RETURN_TOKEN_CARDS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.CREDIT_CARD;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DEBIT_CARD;

@Service("addNPayFilterTokenizedAndPlatformSavedCardsTask")
public class AddNPayFilterTokenizedAndPlatformSavedCardsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(AddNPayFilterTokenizedAndPlatformSavedCardsTask.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNPayFilterTokenizedAndPlatformSavedCardsTask.class);
    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private CoftUtil coftUtil;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy");

    @Override
    public boolean isRunnable(WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {

        return !requestBean.isFromAoaMerchant()
                && iPgpFf4jClient.checkWithdefault("theia.getTokenizedCardsInFPO", null, true);
    }

    @Override
    protected GenericCoreResponseBean<LitePayviewConsultResponseBizBean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        LitePayviewConsultResponseBizBean liteViewConsult = transBean.getAddAndPayLiteViewConsult();
        if (liteViewConsult == null || CollectionUtils.isEmpty(liteViewConsult.getPayMethodViews()))
            return new GenericCoreResponseBean<>(liteViewConsult);

        // Preprocessing to keep COFT payment and channel mapping
        Map<String, Boolean> channelCoftPaymentMapping = new HashMap<>();
        for (PayMethodViewsBiz payMethodView : liteViewConsult.getPayMethodViews()) {
            if (payMethodView != null
                    && (CREDIT_CARD.equals(payMethodView.getPayMethod()) || DEBIT_CARD.equals(payMethodView
                            .getPayMethod())) && !CollectionUtils.isEmpty(payMethodView.getPayChannelOptionViews())) {
                for (PayChannelOptionViewBiz channel : payMethodView.getPayChannelOptionViews()) {
                    if (!CollectionUtils.isEmpty(channel.getSupportPayOptionSubTypes()))
                        channelCoftPaymentMapping.put(channel.getPayOption(), channel.getSupportPayOptionSubTypes()
                                .contains("COFT"));
                }
            }
        }
        LOGGER.info("Channel and COFT payment map : {}", channelCoftPaymentMapping);

        Map<String, String> issuerTokenProcessingOnMid = coftUtil.getIssuerTokenProcessingOnMidMap();

        List<CardInfo> uniqueTokenCards = new ArrayList<>();
        TokenizedCardsResponseBizBean tokenizedCardsResponseBean = transBean.getTokenizedCards();

        // filter out non-active and without gcin tokenized cards
        if (tokenizedCardsResponseBean != null && !CollectionUtils.isEmpty(tokenizedCardsResponseBean.getCardInfos())) {
            List<CardInfo> tokenizedCards = tokenizedCardsResponseBean.getCardInfos();
            // Removing tokenized cards with non-active tokenStatus and null
            // gcin
            Iterator itr = tokenizedCards.iterator();
            while (itr.hasNext()) {
                CardInfo card = (CardInfo) itr.next();
                if (card == null || !BizConstant.STATUS_ACTIVE.equals(card.getTokenStatus())
                        || StringUtils.isEmpty(card.getGlobalPanIndex())) {
                    if (card != null && StringUtils.isEmpty(card.getGlobalPanIndex())) {
                        LOGGER.info("GCIN is not available for token card: {}, token status: {}", card.getCardId(),
                                card.getTokenStatus());
                    }
                    itr.remove();
                }
            }

            // Deduplication logic to remove tokenized duplicate cards on the
            // basis of cin
            // and considering card with farthest token expiry
            Map<CardInfo, CardInfo> tokenCardsMap = new HashMap<>();
            for (CardInfo card : tokenizedCards) {
                if (tokenCardsMap.get(card) != null) {
                    CardInfo presentCard = tokenCardsMap.get(card);
                    try {
                        if (sdf.parse(presentCard.getTokenExpiryMonth() + presentCard.getTokenExpiryYear()).compareTo(
                                sdf.parse(card.getTokenExpiryMonth() + card.getTokenExpiryYear())) < 0) {
                            tokenCardsMap.put(card, card);
                        }
                    } catch (ParseException e) {
                        LOGGER.error("Exception occurred while parsing token expiry : ", e);
                    }
                } else
                    tokenCardsMap.put(card, card);
            }
            tokenizedCards = new ArrayList<>(tokenCardsMap.values());
            LOGGER.info("Tokenized cards after removing duplicate cards : {}", tokenizedCards);

            // Deduplication logic for Tokenized Cards and LPV cards on the
            // basis of cin
            for (CardInfo tokenCard : tokenizedCards) {
                String cardType = BizConstant.CC.equalsIgnoreCase(tokenCard.getCardType()) ? CREDIT_CARD : DEBIT_CARD;
                for (PayMethodViewsBiz payMethodView : liteViewConsult.getPayMethodViews()) {
                    boolean cardTypeMatch = false;
                    if (cardType.equals(payMethodView.getPayMethod())) {
                        cardTypeMatch = true;
                        boolean cardMatch = false;
                        if (!CollectionUtils.isEmpty(payMethodView.getPayCardOptionViews())) {
                            for (PayCardOptionViewBiz platformCard : payMethodView.getPayCardOptionViews()) {
                                if (coftUtil.checkSimilarCard(tokenCard, platformCard)) {
                                    cardMatch = true;
                                    LOGGER.info("Tokenized and LPV card matched with last 4 digit : {}",
                                            tokenCard.getLastFourDigit());
                                    platformCard.setCardTokenized(true);
                                    if (!ff4JUtil.isFeatureEnabled(COFT_RETURN_TOKEN_CARDS, input.getPaytmMID())) {
                                        LOGGER.info("Return token false with card match");
                                        platformCard.setEligibleForCoft(false);
                                        platformCard.setCoftPaymentSupported(false);
                                        platformCard.setCardCoft(false);
                                        break;
                                    }
                                    boolean issuerTokenProcessingEnabled = coftUtil
                                            .isIssuerTokenProcessingEnabled(platformCard);
                                    boolean issuerBinProcessingEnabled = coftUtil
                                            .isBinTokenProcessingEnabled(tokenCard);
                                    boolean tokenPaymentProcessingCapability = issuerBinProcessingEnabled
                                            && issuerTokenProcessingEnabled;

                                    if ((channelCoftPaymentMapping.get(platformCard.getPayOption()) != null && channelCoftPaymentMapping
                                            .get(platformCard.getPayOption()))
                                            && ((MapUtils.isNotEmpty(issuerTokenProcessingOnMid) && issuerTokenProcessingOnMid
                                                    .containsKey(getIssuerString(transBean, platformCard))) || tokenPaymentProcessingCapability)) {
                                        // replace platformCard with tokenCard
                                        EXT_LOGGER.customInfo("Adding token card details in platform card");
                                        coftUtil.addTokenDetailsInPlatformCard(tokenCard, platformCard);
                                        break;
                                    }
                                    break;
                                }
                            }
                        }
                        if (!cardMatch) {
                            uniqueTokenCards.add(tokenCard);
                        }
                    }
                    if (cardTypeMatch)
                        break;
                }
            }
        }
        EXT_LOGGER.customInfo("Unique Tokenized cards after dedup logic with LPV cards : {}", uniqueTokenCards);

        // Adding unique tokenized cards to LPV response by converting them to
        // LPV cards
        if (ff4JUtil.isFeatureEnabled(COFT_RETURN_TOKEN_CARDS, input.getPaytmMID())
                && !CollectionUtils.isEmpty(uniqueTokenCards)) {
            for (CardInfo tokenCard : uniqueTokenCards) {
                PayCardOptionViewBiz platformCard = coftUtil.convertTokenCardToPlatformCard(tokenCard);

                for (PayMethodViewsBiz payMethodView : liteViewConsult.getPayMethodViews()) {
                    if (platformCard.getPayMethod().equals(payMethodView.getPayMethod())) {
                        LOGGER.info("Converting for paymethod {} ", platformCard.getPayMethod());
                        boolean issuerTokenProcessingEnabled = coftUtil.isIssuerTokenProcessingEnabled(platformCard);
                        boolean issuerBinProcessingEnabled = coftUtil.isBinTokenProcessingEnabled(tokenCard);
                        boolean coftPaymentSupported = channelCoftPaymentMapping.get(platformCard.getPayOption()) != null ? channelCoftPaymentMapping
                                .get(platformCard.getPayOption()) : false;
                        if (MapUtils.isNotEmpty(issuerTokenProcessingOnMid)
                                && issuerTokenProcessingOnMid.containsKey(getIssuerString(transBean, platformCard))) {

                            platformCard.setCoftPaymentSupported(coftPaymentSupported);

                        } else {
                            platformCard.setCoftPaymentSupported(issuerBinProcessingEnabled
                                    && issuerTokenProcessingEnabled && coftPaymentSupported);
                        }

                        if (CollectionUtils.isEmpty(payMethodView.getPayCardOptionViews())) {
                            LOGGER.info("Empty paycardoptionviews");
                            payMethodView.setPayCardOptionViews(new ArrayList<>());
                        }

                        List<PayCardOptionViewBiz> payCardOptionViewBiz = payMethodView.getPayCardOptionViews();
                        LOGGER.info("Adding tokenized platform card to LPV response {}", platformCard.getCardIndexNo());
                        payCardOptionViewBiz.add(platformCard);
                        break;
                    }
                }
            }
        }

        // Processing unique LPV cards for COFT eligibility
        // remove block issuer on mid else set coft Eligibility
        updatePayCardOptionViewBizForCoft(transBean, issuerTokenProcessingOnMid);

        LOGGER.info("LPV response (including tokenized) after applying COFT logic: {}",
                liteViewConsult.getPayMethodViews());
        liteViewConsult.setChannelCoftPayment(channelCoftPaymentMapping);
        return new GenericCoreResponseBean<>(liteViewConsult);
    }

    private void updatePayCardOptionViewBizForCoft(WorkFlowTransactionBean transBean,
            Map<String, String> issuerTokenProcessingOnMid) {
        LitePayviewConsultResponseBizBean liteViewConsult = transBean.getMerchantLiteViewConsult();
        Map<String, List<String>> coftEligibilityMerchantStatus = mappingUtil.fillCoftEligibilityMIDMap(transBean
                .getWorkFlowBean().getPaytmMID());
        if (liteViewConsult != null && !CollectionUtils.isEmpty(liteViewConsult.getPayMethodViews())) {
            Iterator<PayMethodViewsBiz> paymethodViewIterator = liteViewConsult.getPayMethodViews().iterator();
            while (paymethodViewIterator.hasNext()) {
                PayMethodViewsBiz payMethodView = paymethodViewIterator.next();
                if (payMethodView != null
                        && (payMethodView.getPayMethod().equals(CREDIT_CARD) || payMethodView.getPayMethod().equals(
                                DEBIT_CARD)) && !CollectionUtils.isEmpty(payMethodView.getPayCardOptionViews())) {
                    Iterator<PayCardOptionViewBiz> paymethodCardOptionIterator = payMethodView.getPayCardOptionViews()
                            .iterator();
                    while (paymethodCardOptionIterator.hasNext()) {
                        PayCardOptionViewBiz card = paymethodCardOptionIterator.next();
                        String issuer = getIssuerString(transBean, card);
                        String txnAmt = StringUtils.isNotEmpty(transBean.getWorkFlowBean().getTxnAmount()) ? String
                                .valueOf((Double.parseDouble(transBean.getWorkFlowBean().getTxnAmount()) / 100))
                                : transBean.getWorkFlowBean().getTxnAmount();
                        if (!coftUtil.checkTokenProcessingEnable(issuerTokenProcessingOnMid, txnAmt, issuer)) {
                            EXT_LOGGER.customInfo("Removing card {} as issuer is block on mid for :  {}",
                                    card.getCardIndexNo(), issuer);
                            paymethodCardOptionIterator.remove();
                        } else {
                            setCoftEligibility(coftEligibilityMerchantStatus, card, transBean);
                        }

                    }

                }
            }
        }
    }

    private void setCoftEligibility(Map<String, List<String>> coftEligibilityMerchantStatus, PayCardOptionViewBiz card,
            WorkFlowTransactionBean transBean) {
        if (card != null && !card.isCardTokenized() && !CollectionUtils.isEmpty(card.getExtendInfo())) {
            boolean isEligibleForCoft = Boolean
                    .parseBoolean(card.getExtendInfo().get(BizConstant.IS_ELIGIBLE_FOR_COFT));
            boolean binEligibleForCoftPref = transBean.getWorkFlowBean().isBinEligibleForCoft();
            if (binEligibleForCoftPref) {
                boolean isBinEligibleForCoft = Boolean.parseBoolean(card.getExtendInfo().get(
                        BizConstant.ExtendedInfoKeys.IS_COFT_BIN_ELIGIBLE));
                isEligibleForCoft = isEligibleForCoft && isBinEligibleForCoft;
            }
            card.setEligibleForCoft(isEligibleForCoft
                    && (ff4JUtil.isGlobalVaultEnabled(transBean, transBean.getWorkFlowBean().getPaytmMID()) || checkCardSchemeEligibility(
                            coftEligibilityMerchantStatus, transBean.getWorkFlowBean().getPaytmMID(),
                            card.getCardScheme())));
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.ADD_N_PAY_FILTER_TOKENIZED_AND_PLATFORM_CARDS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FILTER_TOKENIZED_PLATFORM_CARDS_TIME, "2000"));
    }

    public boolean checkCardSchemeEligibility(Map<String, List<String>> coftEligibilityMerchantStatus, String mid,
            String cardScheme) {
        return coftEligibilityMerchantStatus.get(mid) != null
                && coftEligibilityMerchantStatus.get(mid).contains(cardScheme);
    }

    private String getIssuerString(WorkFlowTransactionBean transBean, PayCardOptionViewBiz platformCard) {
        return transBean.getWorkFlowBean().getPaytmMID() + "." + platformCard.getPayMethod() + "."
                + platformCard.getCardScheme() + "." + platformCard.getInstId();
    }
}
