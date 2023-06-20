package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.facade.user.models.CardBinDigestDetailInfo;
import com.paytm.pgplus.facade.user.models.request.CardBinHashRequest;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("cardBinHelper")
public class CardBinHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(CardBinHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(CardBinHelper.class);

    @Autowired
    private Environment environment;

    private static String CARD_BIN_HASH_SEC_KEY = "card.bin.hash.salt.sec.key";

    public CardBinHashResponse generateCardBinHashResponse(CardBinHashRequest cardBinHashRequest) {
        String bin = cardBinHashRequest.getCardBin();
        String eightDigitBinHash = generateCardBinHash(bin);
        return populateResponse(eightDigitBinHash, bin);

    }

    public String generateCardBinHash(String bin) {
        if (StringUtils.isEmpty(bin) || !StringUtils.isNumeric(bin) || bin.length() != 8)
            throw new IllegalArgumentException("bin is not valid");

        String hashSalt = environment.getProperty(CARD_BIN_HASH_SEC_KEY);
        String eightDigitBinHash = DigestUtils.sha256Hex(bin + hashSalt);
        EXT_LOGGER.customInfo("Generated eight digit has {}", eightDigitBinHash);
        return eightDigitBinHash;

    }

    public CardBinHashResponse populateResponse(String eightDigitBinHash, String bin) {
        CardBinHashResponse cardBinHashResponse = new CardBinHashResponse();
        cardBinHashResponse.setCardBinDigestDetailInfo(new CardBinDigestDetailInfo());
        cardBinHashResponse.getCardBinDigestDetailInfo().setEightDigitBinHash(eightDigitBinHash);
        cardBinHashResponse.getCardBinDigestDetailInfo().setCardBin(bin);
        return cardBinHashResponse;
    }

}
