///**
// *
// */
//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
//import com.paytm.pgplus.biz.utils.Ff4jUtils;
//import com.paytm.pgplus.cache.model.BinDetail;
//import com.paytm.pgplus.cache.model.BinDetailWithDisplayName;
//import com.paytm.pgplus.common.enums.EventNameEnum;
//import com.paytm.pgplus.common.enums.PayMethod;
//import com.paytm.pgplus.common.model.EnvInfoRequestBean;
//import com.paytm.pgplus.enums.EChannelId;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
//import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
//import com.paytm.pgplus.pgproxycommon.utils.BinHelper;
//import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
//import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
//import com.paytm.pgplus.promo.service.client.model.PromoCodeValidateCardRequest;
//import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
//import com.paytm.pgplus.theia.cache.IMerchantMappingService;
//import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
//import com.paytm.pgplus.theia.enums.PaymentRequestParam;
//import com.paytm.pgplus.theia.helper.PaymentRequestValidation;
//import com.paytm.pgplus.theia.models.BinDetailsTheia;
//import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
//import com.paytm.pgplus.theia.models.bin.BinDetailsRequest;
//import com.paytm.pgplus.theia.offline.enums.ResultCode;
//import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
//import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
//import com.paytm.pgplus.theia.offline.model.common.BinData;
//import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
//import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
//import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
//import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
//import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
//import com.paytm.pgplus.theia.utils.BinUtils;
//import com.paytm.pgplus.theia.utils.EnvInfoUtil;
//import com.paytm.pgplus.theia.utils.EventUtils;
//import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
//import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.REMOVE_INTERNAL_PROMO_SUPPORT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CARD_NOT_SUPPORTED_MESSAGE;
//
///**
// * @author Naman
// *
// */
//
//@RestController
//@RequestMapping("bin")
//public class BinFetchController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(BinFetchController.class);
//
//    @Autowired
//    @Qualifier("cardUtils")
//    private CardUtils cardUtils;
//
//    @Autowired
//    @Qualifier("successRateUtils")
//    private SuccessRateUtils successRateUtils;
//
//    @Autowired
//    @Qualifier("theiaSessionDataService")
//    private ITheiaSessionDataService theiaSessionDataService;
//
//    @Autowired
//    @Qualifier("promoServiceHelper")
//    private IPromoServiceHelper promoService;
//
//    @Autowired
//    @Qualifier("merchantMappingService")
//    private IMerchantMappingService merchantMappingService;
//
//    @Autowired
//    @Qualifier("binUtils")
//    private BinUtils binUtils;
//
//    @Autowired
//    @Qualifier("binDetailService")
//    private BinDetailService binDetailService;
//
//    @Autowired
//    @Qualifier("commonFacade")
//    private ICommonFacade commonFacade;
//
//    @Autowired
//    private PaymentRequestValidation paymentRequestValidation;
//
//    @Autowired
//    private BinHelper binHelper;
//
//    @Autowired
//    Ff4jUtils ff4JUtils;
//
//    @RequestMapping(value = "/checkIciciDebitBin", method = RequestMethod.GET)
//    public String checkIciciDebitCard(HttpServletRequest request, @RequestParam(value = "bin") String bin) {
//
//        if (StringUtils.isNotBlank(bin) && (bin.length() >= 6) && StringUtils.isNumeric(bin)) {
//
//            BinDetail binDetail = null;
//
//            try {
//
//                binDetail = cardUtils.fetchBinDetails(bin);
//
//                if (binDetail == null)
//                    return "false";
//
//            } catch (PaytmValidationException exception) {
//                LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", bin, exception);
//                return "false";
//            }
//
//            if ("DEBIT_CARD".equals(binDetail.getCardType()) && isDirectChannelEnabled(request, binDetail)) {
//                // returning false as of now as no direct channel needs to be
//                // supported for new card
//                return "false";
//            }
//        }
//
//        return "false";
//    }
//
//    private BinDetail fetchBinCore(String bin) {
//        BinDetail binDetail = null;
//        try {
//            binDetail = cardUtils.fetchBinDetails(bin);
//        } catch (PaytmValidationException e) {
//            LOGGER.error("Error occurred while fetching bin details {}, Reason being {}", bin, e);
//        }
//        return binDetail;
//    }
//
//    @RequestMapping(value = "/v1/fetchBinDetails", method = RequestMethod.POST)
//    public String getBinDetailsWithDirectChannel(HttpServletRequest servletRequest,
//            @RequestBody BinDetailsRequest binDetailsRequest) throws Exception {
//
//        try {
//            binDetailService.validateBinDetails(binDetailsRequest);
//        } catch (BinDetailException e) {
//            LOGGER.error("Error occurred while validating bin details for reason {}", e);
//            return binDetailService.generateResponseForExceptionCases(binDetailsRequest, e);
//        }
//
//        String bin = binDetailsRequest.getBody().getBin();
//        LOGGER.info("Request received for fetchBinDetails With Direct Channel Enabled, bin : {}",
//                binHelper.logMaskedBinnumber(bin));
//
//        BinDetail binDetail;
//        try {
//            binDetail = cardUtils.fetchBinDetails(bin);
//        } catch (PaytmValidationException exception) {
//            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}",
//                    binHelper.logMaskedBinnumber(bin), exception);
//            return binDetailService.generateResponseForExceptionCases(binDetailsRequest,
//                    BinDetailException.getException());
//        }
//
//        BinDetailResponse response = new BinDetailResponse();
//
//        ResponseHeader responseHeader = new ResponseHeader();
//        response.setHead(responseHeader);
//
//        BinDetailResponseBody responseBody = new BinDetailResponseBody();
//        response.setBody(responseBody);
//
//        BinData binData = binDetailService.getBinData(responseBody, binDetail);
//        responseBody.setBinDetail(binData);
//
//        binDetailService.populateSuccessRate(binData, responseBody, false);
//
//        binDetailService.checkAndAddIDebitOption(servletRequest, binDetailsRequest, response);
//
//        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(servletRequest);
//
//        responseBody.setIconUrl(commonFacade.getLogoUrl(binData.getChannelName(),
//                EChannelId.getEChannelIdByValue(envInfo.getTerminalType().name())));
//
//        responseBody.setResultInfo(OfflinePaymentUtils.resultInfoForSuccess());
//
//        responseHeader.setResponseTimestamp(System.currentTimeMillis());
//
//        return JsonMapper.mapObjectToJson(response);
//    }
//
//    @RequestMapping(value = "/fetchBinDetails", method = RequestMethod.POST)
//    public BinDetailsTheia checkIfSuccessRateMessageRequired(HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        if (session == null) {
//            LOGGER.error("Session Does not exist");
//            return null;
//        }
//        String bin = request.getParameter("bin");
//        LOGGER.info("Request received for check the success rate, bin : {}", bin);
//
//        if (!binUtils.validateBinDetails(bin)) {
//            LOGGER.warn("Unable to check the success rate");
//            return null;
//        }
//
//        BinDetailWithDisplayName binDetailWithDisplayName;
//        binDetailWithDisplayName = binUtils.retrieveBinDetailsWithDisplayName(bin);
//        BinDetail binDetail;
//        binDetail = buildBinDetail(binDetailWithDisplayName, bin);
//
//        if (binDetail == null) {
//            return null;
//        }
//        BinDetailsTheia binDetailsTheia = new BinDetailsTheia(binDetail);
//        if ("DEBIT_CARD".equals(binDetail.getCardType()) && isDirectChannelEnabled(request, binDetail)) {
//            binDetail.setiDebitEnabled(true);
//        }
//
//        if ("CREDIT_CARD".equals(binDetail.getCardType()) && isDirectChannelEnabled(request, binDetail)) {
//            binDetail.setCcDirectEnabled(true);
//        }
//        binDetail.setCardEnabled(binUtils.checkIfCardEnabled(request, binDetail));
//
//        // TODO: showing improper message on cashier page as this feature is
//        // already disabled on UI
//        /*
//         * if (!binDetail.isCardEnabled()) {
//         * binDetailsTheia.setCardStatusMessage
//         * ("This merchant does not accept payment through " +
//         * binDetail.getBankCode() + " " + binDetail.getCardType()); }
//         */
//
//        if (binDetailWithDisplayName != null && binDetailWithDisplayName.isZeroSuccessRate()) {
//            binDetailsTheia
//                    .setCardStatusMessage("We are observing high failures on transacting through this type of debit/credit card right now."
//                            + " We strongly recommend using a different card or payment method for completing this payment.");
//            Map<String, String> metaData = new HashMap<>();
//            metaData.put("BIN", String.valueOf(binDetail.getBin()));
//            EventUtils.pushTheiaEvents(EventNameEnum.ZERO_SR_BIN_FETCHED, metaData);
//        }
//
//        if (!binDetail.getIsIndian() && !isInternationalCardAllowedOnMerchant(request, binDetail)) {
//            binDetailsTheia.setCardStatusMessage(CARD_NOT_SUPPORTED_MESSAGE);
//        }
//
//        if (successRateUtils.checkIfLowSuccessRate(binDetail.getCardName(),
//                PayMethod.getPayMethodByMethod(binDetail.getCardType()))) {
//            binDetailsTheia.setCardSchemeLowSuccessRate(true);
//            return binDetailsTheia;
//        }
//
//        if (successRateUtils.checkIfLowSuccessRate(binDetail.getBankCode(),
//                PayMethod.getPayMethodByMethod(binDetail.getCardType()))) {
//            binDetailsTheia.setIssuerLowSuccessRate(true);
//        }
//
//        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
//        if (txnInfo != null && null != txnInfo.getPromoCodeResponse()) {
//            binDetailsTheia.setPromoResultMessage(promoCardValidation(request, txnInfo));
//        }
//
//        return binDetailsTheia;
//    }
//
//    private boolean isDirectChannelEnabled(HttpServletRequest request, BinDetail binDetail) {
//        EntityPaymentOptionsTO entityPaymentoptions = theiaSessionDataService.getEntityPaymentOptions(request);
//
//        if (null == entityPaymentoptions) {
//            LOGGER.error("Unable to get required attribute from session");
//            return false;
//        }
//
//        boolean directChannelEnabled = theiaSessionDataService.isDirectChannelEnabled(binDetail.getBankCode(),
//                binDetail.getCardType(), entityPaymentoptions.getDirectServiceInsts(), false,
//                entityPaymentoptions.getSupportAtmPins());
//
//        if (!directChannelEnabled) {
//            return false;
//        }
//
//        /*
//         * final String merchantID = request.getParameter("MID"); final String
//         * directChannelBlockedMID =
//         * ConfigurationUtil.getProperty("DIRECT.CHANNEL.BLOCKED.MID", "");
//         * final String directChannelBlockedBankCodes =
//         * ConfigurationUtil.getProperty("DIRECT.CHANNEL.BLOCKED.BANK.CODES",
//         * "");
//         *
//         * if (directChannelBlockedMID.equalsIgnoreCase(merchantID) &&
//         * directChannelBlockedBankCodes.contains(binDetail.getBankCode())) {
//         * LOGGER
//         * .warn("Direct channel disabled for ADD_MONEY, for bank :{} & MID :{}"
//         * , binDetail.getBankCode(), merchantID); return false; }
//         */
//        return true;
//    }
//
//    private String promoCardValidation(HttpServletRequest request, TransactionInfo txnInfo) {
//
//        final TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(request);
//        PromoCodeValidateCardRequest promoCardVaildationRequest = new PromoCodeValidateCardRequest();
//        String message = null;
//        boolean isAddAndPay = TheiaConstant.ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(request
//                .getParameter(PaymentRequestParam.ADD_MONEY.getValue()));
//        if (ResponseCodeConstant.PROMO_SUCCESS.equals(txnInfo.getPromoCodeResponse().getPromoResponseCode())
//                && !isAddAndPay) {
//            promoCardVaildationRequest.setCardNumber(request.getParameter("bin"));
//            MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(txnInfo.getMid());
//            if (mappingMerchantData != null) {
//                promoCardVaildationRequest.setMerchantId(mappingMerchantData.getAlipayId());
//            }
//            String promoCode = txnInfo.getPromoCodeResponse().getPromoCodeDetail() != null ? txnInfo
//                    .getPromoCodeResponse().getPromoCodeDetail().getPromoCode() : null;
//            promoCardVaildationRequest.setPromoCode(promoCode);
//            promoCardVaildationRequest.setTxnMode(theiaPaymentRequest.getTxnMode());
//
//            if (StringUtils.isNotBlank(promoCode)
//                    && ff4JUtils.isFeatureEnabledOnMid(txnInfo.getMid(), REMOVE_INTERNAL_PROMO_SUPPORT, false)) {
//                return message;
//            }
//            PromoCodeResponse promoResponse = promoService.validateCardPromoCode(promoCardVaildationRequest);
//            if (null != promoResponse) {
//                LOGGER.info("Promo validation : {} for promocode : {} and bin : {}", promoResponse.getResultStatus(),
//                        promoCode, request.getParameter("bin"));
//                if (!ResponseCodeConstant.PROMO_SUCCESS.equals(promoResponse.getPromoResponseCode())
//                        && promoResponse.getPromoCodeDetail() != null) {
//                    message = promoResponse.getPromoCodeDetail().getPromoErrorMsg();
//                }
//            }
//        }
//        return message;
//    }
//
//    BinDetail buildBinDetail(BinDetailWithDisplayName binDetailWithDisplayName, String bin) {
//
//        BinDetail binDetail;
//        if (binDetailWithDisplayName != null) {
//            binDetail = new BinDetail();
//            binDetail.setId(binDetailWithDisplayName.getId());
//            binDetail.setCardEnabled(binDetailWithDisplayName.isCardEnabled());
//            binDetail.setActive(binDetailWithDisplayName.isActive());
//            binDetail.setBin(binDetailWithDisplayName.getBin());
//            binDetail.setIsIndian(binDetailWithDisplayName.getIsIndian());
//            binDetail.setBank(binDetailWithDisplayName.getBank());
//            binDetail.setCardType(binDetailWithDisplayName.getCardType());
//            binDetail.setCardName(binDetailWithDisplayName.getCardName());
//            binDetail.setBankCode(binDetailWithDisplayName.getBankCode());
//            binDetail.setInstId(binDetailWithDisplayName.getInstId());
//            binDetail.setiDebitEnabled(binDetailWithDisplayName.isiDebitEnabled());
//            binDetail.setCcDirectEnabled(binDetailWithDisplayName.isCcDirectEnabled());
//            return binDetail;
//        } else
//            return binUtils.retrieveBinDetails(bin);
//    }
//
//    private boolean isInternationalCardAllowedOnMerchant(HttpServletRequest request, BinDetail binDetail) {
//        EntityPaymentOptionsTO entityPaymentoptions = theiaSessionDataService.getEntityPaymentOptions(request);
//
//        if (null == entityPaymentoptions) {
//            LOGGER.error("Unable to get required attribute from session");
//            return false;
//        }
//        try {
//            paymentRequestValidation.validateInternationalCard(entityPaymentoptions, binDetail.getCardName(),
//                    binDetail.getCardType(), false);
//        } catch (PaytmValidationException e) {
//            LOGGER.info("given card is not supported on merchant, bin:{}", binDetail);
//            return false;
//        }
//        return true;
//    }
// }
