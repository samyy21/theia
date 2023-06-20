package com.paytm.pgplus.theia.paymentoffer.util;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIRequest;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIServiceReq;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIServiceRes;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeBinCardHashRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/*
 *  @author prakharsangal
 *  @version FetchCardDetailsUtility.java: , v0.1 09/05/20 prakharsangal Exp $$
 *
 */

@Service("fetchCardDetailsUtility")
public class FetchCardDetailsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCardDetailsUtil.class);

    @Autowired
    @Qualifier("nativeBinCardHashRequestProcessor")
    private NativeBinCardHashRequestProcessor nativeBinCardHashRequestProcessor;

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    @Autowired
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    /**
     * @param serviceReq
     * @param serviceRes
     * @param request
     * @throws Exception
     */
    public void processForCardNumber(NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes, NativeBinCardHashAPIRequest request) throws Exception {

        String cardNo = request.getBody().getCardNumber();
        NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor
                .getNativeBinDetailRequest(request, cardNo);

        NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);
        serviceRes.setBinDetailResponse(binDetailResponse);

        /**
         * Calling cachecard API to fetch CIN in case of plain Card Number.
         */

        String cardIndexNumber = workFlowHelper.getCardIndexNoFromCardNumber(cardNo);
        if (StringUtils.isNotEmpty(cardIndexNumber)) {
            serviceRes.setCardHash(cardIndexNumber);
        } else {
            throw new Exception("Unable to set CIN");
        }
        if (request.getBody().isEightDigitBinRequired()) {
            CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNo.substring(0, 8));
            if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                serviceRes.getBinDetailResponse().getBody().getBinDetail()
                        .setBin(cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash());
            } else {
                throw new Exception("Unable to set bin8Hash");
            }
        }
    }

    /**
     * @param serviceReq
     * @param serviceRes
     * @param request
     * @throws Exception
     */
    public void processForSavedCardId(NativeBinCardHashAPIServiceReq serviceReq,
            NativeBinCardHashAPIServiceRes serviceRes, NativeBinCardHashAPIRequest request) throws Exception {

        String savedCardId = request.getBody().getSavedCardId();
        String cardHash = null;
        String cardNumber = null;
        String eightDigitBinHash = null;
        if (savedCardId.length() > 15) {

            /**
             * Fetching bin 8 alias by calling cardCenter queryNonsensitive API.
             */

            if (request.getBody().isEightDigitBinRequired()) {
                QueryNonSensitiveAssetInfoResponse response = cardCenterHelper.queryNonSensitiveAssetInfo(null,
                        savedCardId);
                if (null != response) {

                    /**
                     * Saving Masked CardNo as we have to fetch bin details.
                     */

                    cardNumber = response.getCardInfo().getCardBin();
                    eightDigitBinHash = response.getCardInfo().getExtendInfo()
                            .get((TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH));
                    cardHash = savedCardId;
                } else {
                    throw new Exception("Unable to set bin8Hash");
                }
            }
        } else if (ff4JUtil.isFeatureEnabledForPromo(request.getBody().getMid())) {

            /**
             * Fetching CIN (CardHash) by calling cache card token API + 8 bin
             * alias by calling Platform getBinHash API.
             */

            cardNumber = nativeBinCardHashRequestProcessor.getCardNumber(savedCardId);
            cardHash = workFlowHelper.getCardIndexNoFromCardNumber(cardNumber);
            if (request.getBody().isEightDigitBinRequired()) {
                CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNumber.substring(0, 8));
                if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                    eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
                } else {
                    throw new Exception("Unable to set bin8Hash");
                }
            }
        }

        NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(request,
                cardNumber);
        NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);

        serviceRes.setCardHash(cardHash);
        serviceRes.setBinDetailResponse(binDetailResponse);
        serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(eightDigitBinHash);

    }

    public void processForCoftToken(NativeBinCardHashAPIRequest request, NativeBinCardHashAPIServiceRes serviceRes,
            String mid, String merchantCoftConfig) throws Exception {
        CardTokenInfo cardTokenInfo = request.getBody().getCardTokenInfo();
        String cardNumber = null;
        String eightDigitBinHash = null;

        if (merchantCoftConfig.equals("PAR")) {
            serviceRes.setCardHash(cardTokenInfo.getPanUniqueReference());
        } else {
            String savedId = coftTokenDataService.getTokenData(mid, cardTokenInfo.getPanUniqueReference(), "PAR",
                    merchantCoftConfig);
            if (StringUtils.isNotEmpty(savedId)) {
                serviceRes.setCardHash(savedId);
            } else {
                throw new Exception("Unable to fetch Saved Card ID for Token");
            }
        }

        BinDetail binDetail = coftTokenDataService.getCardBinDetails(cardTokenInfo.getCardToken());

        if (Objects.nonNull(binDetail)
                && Objects.nonNull(binDetail.getBinAttributes())
                && StringUtils.isNotEmpty(binDetail.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN))) {
            cardNumber = binDetail.getBinAttributes().get(BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
        }

        if (request.getBody().isEightDigitBinRequired()) {
            if (StringUtils.isNotEmpty(cardNumber)) {
                eightDigitBinHash = getEightDigitBinHash(cardNumber);
            }
        }

        if (request.getBody().isEightDigitBinRequired() && StringUtils.isEmpty(eightDigitBinHash)) {
            throw new Exception("Unable to set bin8Hash");
        }

        if (StringUtils.isNotEmpty(cardNumber)) {
            NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(request,
                    cardNumber);
            NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);
            serviceRes.setBinDetailResponse(binDetailResponse);
            serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(eightDigitBinHash);
        } else {
            throw new Exception("Unable to set bin details");
        }
    }

    public void processForCoftTokenCardId(NativeBinCardHashAPIRequest request,
            NativeBinCardHashAPIServiceRes serviceRes, String mid, String merchantCoftConfig) throws Exception {
        String savedCardId = request.getBody().getSavedCardId();
        String cardNumber = null;
        String eightDigitBinHash = null;

        String txnToken = getUpdatedTxnToken(request);
        if (StringUtils.isNotEmpty(txnToken)) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
            SavedCard savedCard = coftTokenDataService.fetchTokenDataFromFPO(savedCardId, cashierInfoResponse);
            if (Objects.nonNull(savedCard)) {
                if (merchantCoftConfig.equals("PAR")) {
                    serviceRes.setCardHash(savedCard.getPar());
                } else if (merchantCoftConfig.equals("GCIN")) {
                    serviceRes.setCardHash(savedCard.getGcin());
                }

                if (StringUtils.isNotEmpty(serviceRes.getCardHash())) {
                    cardNumber = savedCard.getCardDetails().getFirstSixDigit();
                    if (request.getBody().isEightDigitBinRequired()) {
                        String accountRangeCardBin = savedCard.getAccountRangeCardBin();
                        if (StringUtils.isNotEmpty(accountRangeCardBin)) {
                            eightDigitBinHash = getEightDigitBinHash(accountRangeCardBin);
                        }
                    }
                }
            }
        }

        if (StringUtils.isEmpty(serviceRes.getCardHash())) {
            String savedId = coftTokenDataService.getTokenData(mid, savedCardId, "TIN", merchantCoftConfig);
            if (StringUtils.isNotEmpty(savedId)) {
                serviceRes.setCardHash(savedId);
            } else {
                throw new Exception("Unable to fetch Saved Card ID for TIN");
            }

            String tokenBin = coftTokenDataService.fetchTokenDetail(mid, savedCardId);
            if (StringUtils.isNotEmpty(tokenBin)) {
                BinDetail binDetail = coftTokenDataService.getCardBinDetails(tokenBin);

                if (Objects.nonNull(binDetail)
                        && Objects.nonNull(binDetail.getBinAttributes())
                        && StringUtils.isNotEmpty(binDetail.getBinAttributes().get(
                                BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN))) {
                    cardNumber = binDetail.getBinAttributes().get(BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
                }

                if (request.getBody().isEightDigitBinRequired()) {
                    if (StringUtils.isNotEmpty(cardNumber)) {
                        eightDigitBinHash = getEightDigitBinHash(cardNumber);
                    }
                }
            }
        }

        if (request.getBody().isEightDigitBinRequired() && StringUtils.isEmpty(eightDigitBinHash)) {
            throw new Exception("Unable to set bin8Hash");
        }

        if (StringUtils.isNotEmpty(cardNumber)) {
            NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(request,
                    cardNumber);
            NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);
            serviceRes.setBinDetailResponse(binDetailResponse);
            serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(eightDigitBinHash);
        } else {
            throw new Exception("Unable to set bin details");
        }
    }

    public void processForCoftSavedCardId(NativeBinCardHashAPIRequest request,
            NativeBinCardHashAPIServiceRes serviceRes, String mid, String merchantCoftConfig) throws Exception {
        String savedCardId = request.getBody().getSavedCardId();
        String cardNumber = null;
        String eightDigitBinHash = null;

        String txnToken = getUpdatedTxnToken(request);
        if (StringUtils.isNotEmpty(txnToken)) {
            NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(txnToken);
            SavedCard savedCard = coftTokenDataService.fetchTokenDataFromFPO(savedCardId, cashierInfoResponse);
            if (Objects.nonNull(savedCard)) {
                if (merchantCoftConfig.equals("PAR")) {
                    serviceRes.setCardHash(savedCard.getPar());
                } else if (merchantCoftConfig.equals("GCIN")) {
                    serviceRes.setCardHash(savedCard.getGcin());
                }

                if (StringUtils.isNotEmpty(serviceRes.getCardHash())) {
                    cardNumber = savedCard.getCardDetails().getFirstSixDigit();
                    if (request.getBody().isEightDigitBinRequired()) {
                        if (StringUtils.isNotEmpty(savedCard.getAccountRangeCardBin())) {
                            eightDigitBinHash = savedCard.getAccountRangeCardBin();
                        }
                    }
                }
            }
        }

        if (StringUtils.isEmpty(serviceRes.getCardHash())) {
            QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                    .queryNonSensitiveAssetInfo(null, savedCardId);
            String savedId = coftTokenDataService.getSavedCardIdFromCardIndexNumber(mid, merchantCoftConfig,
                    savedCardId, queryNonSensitiveAssetInfoResponse);
            if (StringUtils.isNotEmpty(savedId)) {
                serviceRes.setCardHash(savedId);
            } else {
                throw new Exception("Unable to fetch Saved Card ID for CIN");
            }

            if (Objects.nonNull(queryNonSensitiveAssetInfoResponse)) {
                cardNumber = queryNonSensitiveAssetInfoResponse.getCardInfo().getCardBin();
            }

            if (request.getBody().isEightDigitBinRequired()) {
                if (Objects.nonNull(queryNonSensitiveAssetInfoResponse)) {
                    eightDigitBinHash = queryNonSensitiveAssetInfoResponse.getCardInfo().getExtendInfo()
                            .get((TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH));
                }
            }
        }

        if (request.getBody().isEightDigitBinRequired() && StringUtils.isEmpty(eightDigitBinHash)) {
            throw new Exception("Unable to set bin8Hash");
        }

        if (StringUtils.isNotEmpty(cardNumber)) {
            NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(request,
                    cardNumber);
            NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);
            serviceRes.setBinDetailResponse(binDetailResponse);
            serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(eightDigitBinHash);
        } else {
            throw new Exception("Unable to set bin details");
        }
    }

    public void processForCoftCardNumber(NativeBinCardHashAPIRequest request,
            NativeBinCardHashAPIServiceRes serviceRes, String mid, String merchantCoftConfig) throws Exception {
        String cardNo = request.getBody().getCardNumber();
        String eightDigitBinHash = null;

        String savedId = coftTokenDataService.getSavedCardIdFromCardNumber(mid, cardNo, merchantCoftConfig);
        if (StringUtils.isNotEmpty(savedId)) {
            serviceRes.setCardHash(savedId);
        } else {
            throw new Exception("Unable to fetch Saved Card ID for Card Number");
        }

        if (request.getBody().isEightDigitBinRequired()) {
            CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNo.substring(0, 8));
            if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                eightDigitBinHash = cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
            }
        }

        if (request.getBody().isEightDigitBinRequired() && StringUtils.isEmpty(eightDigitBinHash)) {
            throw new Exception("Unable to set bin8Hash");
        }

        NativeBinDetailRequest binRequest = nativeBinCardHashRequestProcessor
                .getNativeBinDetailRequest(request, cardNo);
        NativeBinDetailResponse binDetailResponse = nativeBinCardHashRequestProcessor.getBinDetails(binRequest);
        serviceRes.setBinDetailResponse(binDetailResponse);
        serviceRes.getBinDetailResponse().getBody().getBinDetail().setBin(eightDigitBinHash);
    }

    private String getUpdatedTxnToken(NativeBinCardHashAPIRequest request) {
        String txnToken = request.getHead().getToken();
        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            String mid = request.getBody().getMid();
            txnToken = nativeSessionUtil.createTokenForMidSSOFlow(txnToken, mid);
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType()) && null == txnToken) {
            txnToken = request.getHead().getTxnToken();
        }
        return txnToken;
    }

    private String getEightDigitBinHash(String cardNo) {
        if (StringUtils.isNotEmpty(cardNo)) {
            CardBinHashResponse cardBinHashResponse = workFlowHelper.getCardBinHash(cardNo.substring(0, 8));
            if (null != cardBinHashResponse && null != cardBinHashResponse.getCardBinDigestDetailInfo()) {
                return cardBinHashResponse.getCardBinDigestDetailInfo().getEightDigitBinHash();
            }
        }
        return null;
    }

}
