/**
 * this utility fetches saved cards corresponding to paytm userID
 */
package com.paytm.pgplus.biz.core.user.service.impl;

import java.util.*;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.common.enums.CardTypeEnum;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.CardTypeEnum;
import com.paytm.pgplus.common.enums.StatusEnum;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ICacheCardService;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author namanjain
 *
 */
@Service("savedCards")
public class SavedCardsImpl implements ISavedCards {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavedCardsImpl.class);

    @Autowired
    @Qualifier("savedCardService")
    ISavedCardService savedCardService;

    @Autowired
    @Qualifier("cacheCardService")
    ICacheCardService cacheCardService;

    @Autowired
    MappingUtil mapUtils;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SAVEDCARD_FETCH_SAVEDCARD_BY_USERID)
    @Override
    public GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByUserId(String userID) {
        SavedCardResponse<List<SavedCardVO>> savedCardsBean = savedCardService.getAllSavedCardsByUserIdAndStatus(
                userID, StatusEnum.ACTIVE);
        LOGGER.debug("Saved Cards Response returned as :: {}", savedCardsBean);

        if (!savedCardsBean.getStatus() || !BizParamValidator.validateInputObjectParam(savedCardsBean)) {
            LOGGER.info("No Saved Cards found for User ID : {} ", userID);
            return new GenericCoreResponseBean<>(savedCardsBean.getMessage());
        }

        List<CardBeanBiz> bizSavedCards = mapUtils.mapListSavedCards(savedCardsBean.getResponseData());
        if (!BizParamValidator.validateInputObjectParam(bizSavedCards))
            return new GenericCoreResponseBean<>("SavedCardNull");

        return new GenericCoreResponseBean<>(bizSavedCards);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SAVEDCARD_FETCH_SAVEDCARD_BY_CARDID)
    @Override
    public GenericCoreResponseBean<UserDetailsBiz> fetchSavedCardsByCardId(String cardId, UserDetailsBiz userDetails,
            WorkFlowRequestBean flowRequestBean) {

        SavedCardResponse<SavedCardVO> savedCardsBean = null;

        if (userDetails == null) {
            userDetails = new UserDetailsBiz();
        }

        String custId = flowRequestBean.getCustID();
        String mId = flowRequestBean.getPaytmMID();
        if (flowRequestBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(userDetails.getUserId())
                && StringUtils.isNotEmpty(mId) && StringUtils.isNotEmpty(custId)) {
            savedCardsBean = savedCardService.getSavedCardByCardId(Long.parseLong(cardId), userDetails.getUserId(),
                    custId, mId);
        } else if (flowRequestBean.isStoreCardPrefEnabled() && StringUtils.isNotEmpty(mId)
                && StringUtils.isNotEmpty(custId)) {
            savedCardsBean = savedCardService.getSavedCardByCardId(Long.parseLong(cardId), custId, mId);

        } else if (StringUtils.isNotBlank(userDetails.getUserId())) {
            savedCardsBean = savedCardService.getSavedCardByCardId(Long.valueOf(cardId), userDetails.getUserId());
        }
        LOGGER.debug("Saved Cards Response returned as:::{}", savedCardsBean);

        if (savedCardsBean == null || !savedCardsBean.getStatus()) {
            StatisticsLogger.logForXflush("PGPLUS", "SAVEDCARD", null, "response", "saved card fetching failed", null);
            LOGGER.error("Error occured while fetching savedCards");
            return new GenericCoreResponseBean<>(savedCardsBean.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }

        /*
         * Decrypt card details fetched from DB , using MASTER key not merchant
         * key. These will be needed in CacheCard API
         */
        String decryptedCardNumber = savedCardsBean.getResponseData().getCardNumber();
        String decryptedExpiryDate = savedCardsBean.getResponseData().getExpiryDate();

        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
            flowRequestBean.setCardNo(decryptedCardNumber);
            flowRequestBean.setExpiryMonth(Short.valueOf(decryptedExpiryDate.substring(0, 2)));
            flowRequestBean.setExpiryYear(Short.valueOf(decryptedExpiryDate.substring(2, 6)));

            // Need to fetch BIN dedtails for this cardId
            BinDetail binDetails = mapUtils.getBinDetail(savedCardsBean.getResponseData().getFirstSixDigit());
            if (binDetails != null) {
                flowRequestBean.setInstId(binDetails.getBankCode());
                flowRequestBean.setBankName(binDetails.getBank());
                flowRequestBean.setCardType(binDetails.getCardType());
                flowRequestBean.setCardScheme(binDetails.getCardName());
                if (!(flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
                    flowRequestBean.setPayMethod(flowRequestBean.getCardType());
                    flowRequestBean.setPayOption(flowRequestBean.getCardType() + "_" + flowRequestBean.getCardScheme());
                }
            } else {
                return new GenericCoreResponseBean<>("Exception occured while fetching BIN details",
                        ResponseConstants.SYSTEM_ERROR);
            }

        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {
            /*
             * MMID will be sent as cardNumber to alipay in cache card. While in
             * paytm DB , phoneNumber stored as cardNumber
             */
            flowRequestBean.setMmid(decryptedExpiryDate);
            flowRequestBean.setHolderMobileNo(decryptedCardNumber);
        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            flowRequestBean.setVirtualPaymentAddress(decryptedCardNumber);
        }

        CardBeanBiz bizSavedCard = mapUtils.mapSavedCards(savedCardsBean.getResponseData(), flowRequestBean);
        bizSavedCard.setCardNumber(decryptedCardNumber);
        bizSavedCard.setExpiryDate(decryptedExpiryDate);
        bizSavedCard.setCardId(Long.valueOf(cardId));

        List<CardBeanBiz> savedCardsList = new ArrayList<>();
        savedCardsList.add(bizSavedCard);
        userDetails.setMerchantViewSavedCardsList(savedCardsList);
        if (!BizParamValidator.validateInputObjectParam(bizSavedCard))
            return new GenericCoreResponseBean<>("SavedCardNull", ResponseConstants.SYSTEM_ERROR);

        return new GenericCoreResponseBean<>(userDetails);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.SAVEDCARD_CACHE_SAVEDCARD_DETAILS)
    @Override
    public GenericCoreResponseBean<UserDetailsBiz> cacheCardDetails(WorkFlowRequestBean flowRequestBean,
            UserDetailsBiz userDetails, String transactionId) {
        if (userDetails == null) {
            userDetails = new UserDetailsBiz();
        }
        SavedCardVO savedCardVO = mapUtils.createSavedCardVO(flowRequestBean, userDetails);
        if (savedCardVO == null) {
            return new GenericCoreResponseBean<>("Exception occured while creating data for cache card service");
        }
        SavedCardResponse<Boolean> response = cacheCardService.saveCardDetailsInCache(transactionId, savedCardVO);

        LOGGER.debug("Cache Cards Response returned as:::{}", response);
        if (!response.getStatus() || !BizParamValidator.validateInputObjectParam(response.getResponseData())) {
            LOGGER.error("Error occured while caching card details into Redis : {}", response.getMessage());
        }
        CardBeanBiz cardBeanBiz = mapUtils.mapSavedCards(savedCardVO, flowRequestBean);

        // Setting non-encrypted Card details
        if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
            cardBeanBiz.setCardNumber(flowRequestBean.getCardNo());
            cardBeanBiz.setExpiryDate(String.valueOf(flowRequestBean.getExpiryMonth())
                    + String.valueOf(flowRequestBean.getExpiryYear()));

        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {
            cardBeanBiz.setCardNumber(flowRequestBean.getHolderMobileNo() == null ? flowRequestBean.getMobileNo()
                    : flowRequestBean.getHolderMobileNo());
            cardBeanBiz.setExpiryDate(flowRequestBean.getMmid());

        } else if (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.UPI.value)) {
            cardBeanBiz.setCardNumber(flowRequestBean.getVirtualPaymentAddress());
        }

        List<CardBeanBiz> savedCardsList = new ArrayList<>();
        savedCardsList.add(cardBeanBiz);
        userDetails.setMerchantViewSavedCardsList(savedCardsList);

        return new GenericCoreResponseBean<>(userDetails);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.FETCH_SAVEDCARD_BY_MID_CUSTID_USERID)
    @Override
    public GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByMidCustIdUserId(String mId, String custId,
            String userId) {
        SavedCardResponse<List<SavedCardVO>> savedCardsBean = savedCardService.getAllSavedCardsByMidCustIdUserId(mId,
                custId, userId);
        LOGGER.debug("Saved Cards Response returned as :: {}", savedCardsBean);
        List<SavedCardVO> listSavedCardsBean = savedCardsBean.getResponseData();

        // Showing only Unique VPAs- To handle Existing VPAs which already
        // exist.
        if (Boolean.parseBoolean(ConfigurationUtil.getProperty("show.unique.vpa")) && savedCardsBean != null
                && listSavedCardsBean != null && !listSavedCardsBean.isEmpty()) {
            for (int i = 0; i < listSavedCardsBean.size(); i++) {
                SavedCardVO savedCardVO = listSavedCardsBean.get(i);
                if (savedCardVO.getCardType().getName().equals(CardTypeEnum.VPA.getName())) {
                    for (int j = i + 1; j < listSavedCardsBean.size(); j++) {
                        SavedCardVO nextSavedCardVO = listSavedCardsBean.get(j);
                        if (!savedCardVO.getCardNumber().isEmpty() && !nextSavedCardVO.getCardNumber().isEmpty()
                                && savedCardVO.getCardNumber().equalsIgnoreCase(nextSavedCardVO.getCardNumber())) {
                            listSavedCardsBean.remove(nextSavedCardVO);
                            j--;
                            LOGGER.info("Already same VPA exist, hence Not Showing" + nextSavedCardVO.getCardNumber());
                        }
                    }
                }
            }
        }
        if (!savedCardsBean.getStatus() || !BizParamValidator.validateInputObjectParam(savedCardsBean)) {
            LOGGER.info("No Saved Cards found on params mId {}, custId {}, userId {}", mId, custId, userId);
            return new GenericCoreResponseBean<>(savedCardsBean.getMessage());
        }

        List<CardBeanBiz> bizSavedCards = mapUtils.mapListSavedCards(listSavedCardsBean);
        if (!BizParamValidator.validateInputObjectParam(bizSavedCards))
            return new GenericCoreResponseBean<>("SavedCardNull");

        return new GenericCoreResponseBean<>(bizSavedCards);

    }

}
