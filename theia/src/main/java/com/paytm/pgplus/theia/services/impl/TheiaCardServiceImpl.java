package com.paytm.pgplus.theia.services.impl;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.services.ITheiaCardService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;

/**
 * @createdOn 25-Nov-2016
 * @author Santosh
 */
@Service("theiaCardService")
public class TheiaCardServiceImpl implements ITheiaCardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaCardServiceImpl.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private ISavedCardService savedCardService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Override
    public String processDeleteCard(final HttpServletRequest request, String savedCardId, boolean deleteFromDB) {
        try {
            if (!theiaSessionDataService.isSessionExists(request)) {
                LOGGER.error("Session not available for delete card");
                return "fail";
            }
            if (StringUtils.isBlank(savedCardId)) {
                LOGGER.error("Saved card id not received");
                return "fail";
            }

            long cardId = Long.parseLong(savedCardId);
            String merchantId = request.getParameter("MID");
            TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
            LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request);
            String userId = null;

            if (txnInfo != null && StringUtils.isNotBlank(txnInfo.getCustID()) && StringUtils.isNotBlank(merchantId)) {

                if (loginInfo == null || loginInfo.getUser() == null) {
                    LOGGER.info("UserInfo not available for delete card");
                } else {
                    userId = loginInfo.getUser().getUserID();
                }

                if (deleteFromDB) {
                    savedCardService.deactivateSavedCard(cardId, merchantId, txnInfo.getCustID(), userId);
                }

            } else {
                if (loginInfo == null || loginInfo.getUser() == null) {
                    LOGGER.error("UserInfo not available for delete card");
                    return "fail";
                }
                userId = loginInfo.getUser().getUserID();
                if (StringUtils.isBlank(userId)) {
                    LOGGER.error("User ID not received");
                    return "fail";
                }
                if (deleteFromDB) {
                    savedCardService.deactivateSavedCard(cardId, userId);
                }

            }

            final CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(request);

            for (Iterator<SavedCardInfo> addNPayIter = cardInfo.getAddAndPayViewCardsList().iterator(); addNPayIter
                    .hasNext();) {
                SavedCardInfo savedCard = addNPayIter.next();

                if (savedCard.getCardId().equals(cardId)) {
                    cardInfo.getAddAnPaySavedCardMap().remove(savedCardId);
                    addNPayIter.remove();
                    break;
                }
            }

            for (Iterator<SavedCardInfo> merchantIter = cardInfo.getMerchantViewSavedCardsList().iterator(); merchantIter
                    .hasNext();) {
                SavedCardInfo savedCard = merchantIter.next();

                if (savedCard.getCardId().equals(cardId)) {
                    cardInfo.getSavedCardMap().remove(savedCardId);
                    merchantIter.remove();
                    break;
                }
            }

            if (cardInfo.getAddAndPayViewCardsList().isEmpty()) {
                cardInfo.setAddAndPayViewSaveCardEnabled(false);
            }

            if (cardInfo.getMerchantViewSavedCardsList().isEmpty()) {
                cardInfo.setSaveCardEnabled(false);
            }

        } catch (Exception e) {
            LOGGER.error("Unable to delete saved card due to error : ", e);
            return "fail";
        }

        return "success";
    }

}
