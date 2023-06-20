/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.models.UserProfile;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.theia.cache.IMerchantOfferDetailsService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.enums.SavedCardType;
import com.paytm.pgplus.theia.merchant.models.MappingMerchantOfferDetails;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsInput;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsResponse;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.BeanParamValidator;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PPBL_BALANCE_FAIL_MSG;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PPBL_INSUFFICIENT_BALANCE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.*;

/**
 * @author amit.dubey
 *
 */
@Component("transactionInfoSessionUtil")
public class TransactionInfoSessionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantOfferDetailsService")
    private IMerchantOfferDetailsService merchantOfferDetailsService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    public void setTransactionInfoIntoSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {
        LOGGER.debug("WorkFlowResponseBean :{}", responseData);
        final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(requestData.getRequest(), true);
        LOGGER.debug("Found data in session, txnInfo :{}", txnInfo);

        setTxnInfoSubscription(requestData, responseData, txnInfo);

        txnInfo.setSubwalletAmount(requestData.getSubwalletAmount());

        txnInfo.setCurrencyCode(responseData.getMerchnatViewResponse().getCurrency());
        txnInfo.setMid(requestData.getMid());
        txnInfo.setSecurityID(responseData.getMerchnatViewResponse().getSecurityId());
        if (responseData.getAddAndPayViewResponse() != null) {
            txnInfo.setAddAndPaySecurityId(responseData.getAddAndPayViewResponse().getSecurityId());
        }
        txnInfo.setOrderDetails(requestData.getOrderDetails());
        txnInfo.setOrderId(requestData.getOrderId());
        txnInfo.setPaymentTypeId(requestData.getPaymentTypeId());

        txnInfo.setRequestType(requestData.getRequestType());
        if (ERequestType.RESELLER.name().equals(requestData.getRequestType())) {
            if (null == responseData.getAccountBalanceResponse()
                    || StringUtils.isBlank(responseData.getAccountBalanceResponse().getEffectiveBalance())) {
                txnInfo.setDisplayMsg(PPBL_BALANCE_FAIL_MSG);
            } else if (null != responseData.getAccountBalanceResponse()
                    && StringUtils.isBlank(responseData.getAccountBalanceResponse().getEffectiveBalance())
                    && Long.valueOf(requestData.getTxnAmount()) > Long.valueOf(responseData.getAccountBalanceResponse()
                            .getEffectiveBalance())) {
                txnInfo.setDisplayMsg(PPBL_INSUFFICIENT_BALANCE);
            }
        }
        txnInfo.setPromoCodeValid(requestData.isPromoCodeValid());
        txnInfo.setTransCreatedTime(responseData.getTransCreatedTime());

        if (responseData.getUserDetails() != null) {
            final UserDetailsBiz userDetails = responseData.getUserDetails();
            LOGGER.debug("UserDetailsBiz :{}", userDetails);
            final ConsultPayViewResponseBizBean getAddAndPayViewResponse = responseData.getAddAndPayViewResponse();
            LOGGER.debug("GetAddAndPayViewResponse :{}", getAddAndPayViewResponse);

            if (requestData.getRequestType().equalsIgnoreCase(SEAMLESS)) {
                txnInfo.setMobileno(userDetails.getMobileNo());
                txnInfo.setEmailId(userDetails.getEmail());
                txnInfo.setAddress1(requestData.getAddress1());
                txnInfo.setAddress2(requestData.getAddress2());
                txnInfo.setCity(requestData.getCity());
                txnInfo.setState(requestData.getState());
                txnInfo.setPincode(requestData.getPincode());
            }
            txnInfo.setMobileno(userDetails.getMobileNo());
            if (StringUtils.isBlank(txnInfo.getPaymentTypeId()) && (userDetails.getUserProfile() != null)) {
                UserProfile userProfile = userDetails.getUserProfile();
                PayMethod payMethod = PayMethod.valueOf(userProfile.getPayMethod());
                if (payMethod != null) {
                    txnInfo.setPaymentTypeId(payMethod.getOldName());
                    txnInfo.setSelectedBank(userProfile.getSpecificBank());
                }
            }
        }

        // Setting Offer Message
        setOfferMessage(requestData, responseData, txnInfo);
        mapConsultFeesResponse(responseData, txnInfo);

        /*
         * Setting TxnAmount in Rs.
         */
        String amount;
        if (NumberUtils.isNumber(requestData.getTxnAmount())) {
            amount = TheiaConstant.RequestTypes.SUBSCRIPTION.equals(requestData.getRequestType()) ? requestData
                    .getRequest().getParameter(RequestParams.TXN_AMOUNT) : requestData.getTxnAmount();
        } else {
            amount = AmountUtils.getTransactionAmountInRupee(responseData.getMerchnatViewResponse().getTransAmount());
        }

        double roundOff = (double) Math.round(Double.parseDouble(amount) * 100) / 100;
        txnInfo.setTxnAmount(String.valueOf(roundOff));

        txnInfo.setTxnId(responseData.getTransID());
        if (responseData.getUserDetails() != null) {
            txnInfo.setSsoID(responseData.getUserDetails().getUserId());
            txnInfo.setSsoToken(responseData.getUserDetails().getUserToken());
        }
        txnInfo.setCustID(requestData.getCustId());

        // EMI
        txnInfo.setAddress1(requestData.getAddress1());
        txnInfo.setPincode(requestData.getPincode());
        txnInfo.setEmiOption(requestData.getEmiOption());

        mapPromoCodeResponse(requestData, txnInfo);
        txnInfo.setWebsite(requestData.getWebsite());
        txnInfo.setCardTokenRequired(requestData.getCardTokenRequired());
        txnInfo.setCartValidationRequired(requestData.isCartValidationRequired());
        txnInfo.setQrDetails(responseData.getQrCodeDetails());

        // Addmoney destination to be sent to Wallet
        if ((ADD_MONEY.equals(requestData.getRequestType()) || DEFAULT.equals(requestData.getRequestType()))
                && StringUtils.isNotBlank(responseData.getAddMoneyDestination())) {
            txnInfo.setAddMoneyDestination(responseData.getAddMoneyDestination());
            txnInfo.setTargetPhoneNo(responseData.getTargetPhoneNo());
        }

        boolean onus = merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid());
        txnInfo.setOnus(onus);

        /*
         * adding trustfactor for riskextendedInfo
         */
        txnInfo.setTrustFactor(responseData.getTrustFactor());

        LOGGER.debug("Updated session TransactionInfo data :{}", txnInfo);
    }

    private void setTxnInfoSubscription(PaymentRequestBean requestData, WorkFlowResponseBean responseData,
            TransactionInfo txnInfo) {
        if (requestData.getRequestType().equals(SUBSCRIPTION)
                || requestData.getRequestType().equals(RENEW_SUBSCRIPTION)) {
            txnInfo.setSubscriptionAmountType(requestData.getSubscriptionAmountType());
            txnInfo.setSubscriptionEnableRetry(requestData.getSubscriptionEnableRetry());
            txnInfo.setSubscriptionExpiryDate(requestData.getSubscriptionExpiryDate());
            txnInfo.setSubscriptionFrequency(requestData.getSubscriptionFrequency());
            txnInfo.setSubscriptionFrequencyUnit(requestData.getSubscriptionFrequencyUnit());
            txnInfo.setSubscriptionGraceDays(requestData.getSubscriptionGraceDays());

            if (requestData.getRequestType().equals(SUBSCRIPTION)) {
                txnInfo.setSubscriptionServiceID(responseData.getSubscriptionID());
            } else {
                txnInfo.setSubscriptionServiceID(requestData.getSubscriptionServiceID());
            }

            txnInfo.setSubscriptionStartDate(requestData.getSubscriptionStartDate());
            txnInfo.setSubscriptionMaxAmount(requestData.getSubscriptionMaxAmount());
            final String subsMinAmount = ConfigurationUtil.getProperty(ExtraConstants.SUBSCRIPTION_MIN_AMOUNT, "1");
            txnInfo.setSubscriptionMinAmount(subsMinAmount);
            setSubsType(requestData, txnInfo);

            txnInfo.setSubscriptionPPIOnly(requestData.getSubsPPIOnly());
            if ("0".equals(requestData.getRequest().getParameter(RequestParams.TXN_AMOUNT))) {
                txnInfo.setZeroRupeesSubscription(true);
            }
        }
    }

    private void mapPromoCodeResponse(final PaymentRequestBean requestData, final TransactionInfo txnInfo) {
        if (null == requestData.getPromoCodeResponse()) {
            return;
        }
        txnInfo.setPromoCodeResponse(requestData.getPromoCodeResponse());
        mapPromoCardBins(txnInfo, requestData.getPromoCodeResponse());
        checkForDiscount(requestData);
    }

    private void checkForDiscount(PaymentRequestBean requestData) {
        if (ExtraConstants.PROMOCODE_TYPE_DISCOUNT.equals(requestData.getPromoCodeResponse().getPromoCodeDetail()
                .getPromocodeTypeName())) {
            EntityPaymentOptionsTO entityInfo = theiaSessionDataService.getEntityPaymentOptions(requestData
                    .getRequest());
            entityInfo.resetAllPaymentOptionsEnabled();
            if (requestData.getPromoCodeResponse().getPromoCodeDetail().getPaymentModes() != null) {
                resetFlagsForPaymentModes(requestData.getPromoCodeResponse(), entityInfo);
                removeLists(entityInfo, requestData);

                if (entityInfo.isCcEnabled() || entityInfo.isDcEnabled()) {

                    Set<Integer> binSet = requestData.getPromoCodeResponse().getPromoCodeDetail().getCardBins()
                            .keySet();
                    filterSavedCardsBasedOnBins(binSet, requestData);
                }

            }
        }
    }

    private void mapPromoCardBins(final TransactionInfo txnInfo, PromoCodeResponse promoCodeResponse) {
        if (promoCodeResponse.getPromoCodeDetail() == null
                || promoCodeResponse.getPromoCodeDetail().getCardBins() == null) {
            LOGGER.warn("PromoCode details is null or bin list is null");
            return;
        }
        List<Integer> lst = new ArrayList<>();
        for (int bin : promoCodeResponse.getPromoCodeDetail().getCardBins().keySet()) {
            lst.add(bin);
        }

        txnInfo.setPromoBins(lst);
    }

    private void removeLists(EntityPaymentOptionsTO entityInfo, PaymentRequestBean paymentRequestBean) {
        if (!entityInfo.isCcEnabled()) {
            entityInfo.setCompleteCcList(Collections.emptyList());
        }
        if (!entityInfo.isDcEnabled()) {
            entityInfo.setCompleteDcList(Collections.emptyList());
        }
        if (!entityInfo.isAtmEnabled()) {
            entityInfo.setCompleteATMList(Collections.emptyList());
        }
        if (!entityInfo.isNetBankingEnabled()) {
            entityInfo.setCompleteNbList(Collections.emptyList());
        }
        if (!entityInfo.isImpsEnabled()) {
            entityInfo.setCompleteIMPSList(Collections.emptyList());
        }
        if (!entityInfo.isEmiEnabled()) {
            entityInfo.setCompleteEMIInfoList(Collections.emptyList());
        }
        if (!entityInfo.isUpiEnabled()) {
            entityInfo.setCompleteUPIInfoList(Collections.emptyList());
        }
        if (!entityInfo.isCcEnabled() && !entityInfo.isDcEnabled()) {
            CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(paymentRequestBean.getRequest());
            if (cardInfo != null) {
                cardInfo.setMerchantViewSavedCardsList(Collections.emptyList());
                cardInfo.setSaveCardEnabled(false);
            }

        }

    }

    private void resetFlagsForPaymentModes(final PromoCodeResponse promoCodeResponse, EntityPaymentOptionsTO entityInfo) {
        for (String payMethod : promoCodeResponse.getPromoCodeDetail().getPaymentModes()) {
            PayMethod paymentMode = PayMethod.getPayMethodByOldName(payMethod.toUpperCase());
            switch (paymentMode) {
            case ATM:
                entityInfo.setAtmEnabled(true);
                break;
            case BALANCE:
            case BANK_EXPRESS:
            case HYBRID_PAYMENT:
            case PAYTM_DIGITAL_CREDIT:
                break;
            case COD:
            case MP_COD:
                break;
            case CREDIT_CARD:
                entityInfo.setCcEnabled(true);
                break;
            case DEBIT_CARD:
                entityInfo.setDcEnabled(true);
                break;
            case EMI:
                entityInfo.setEmiEnabled(true);
                break;
            case IMPS:
                entityInfo.setImpsEnabled(true);
                break;
            case NET_BANKING:
                entityInfo.setNetBankingEnabled(true);
                filterPayModes(promoCodeResponse, entityInfo);
                break;
            case UPI:
                entityInfo.setUpiEnabled(true);
                break;
            default:
                break;
            }
        }
    }

    private void filterSavedCardsBasedOnBins(Set<Integer> bins, PaymentRequestBean requestData) {

        CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(requestData.getRequest());

        if (Objects.isNull(cardInfo))
            return;

        List<SavedCardInfo> merchantCards = cardInfo.getMerchantViewSavedCardsList();

        List<SavedCardInfo> addAndPayCards = cardInfo.getAddAndPayViewCardsList();

        if ((Objects.isNull(merchantCards) || merchantCards.isEmpty())
                && (Objects.isNull(addAndPayCards) || addAndPayCards.isEmpty())) {
            return;
        }

        if (!merchantCards.isEmpty()) {
            cardInfo.setMerchantViewSavedCardsList(filterCards(bins, merchantCards));
        }

        if (!addAndPayCards.isEmpty()) {
            cardInfo.setAddAndPayViewCardsList(filterCards(bins, addAndPayCards));
        }

    }

    private List<SavedCardInfo> filterCards(Set<Integer> bins, List<SavedCardInfo> cardInfoList) {

        if (Objects.isNull(bins) || Objects.isNull(cardInfoList) || cardInfoList.isEmpty())
            return Collections.EMPTY_LIST;

        List<SavedCardInfo> filteredList = new ArrayList<>();

        for (SavedCardInfo cardInfo : cardInfoList) {

            if (SavedCardType.UPI.getCardType().equals(cardInfo.getCardType()) || cardInfo.getFirstSixDigit() == null) {
                continue;
            }
            int currentBin = cardInfo.getFirstSixDigit().intValue();

            if (bins.contains(currentBin)) {
                filteredList.add(cardInfo);
            } else {
                LOGGER.info("Filtered out card for promo filtering, cardInfo ::{}", cardInfo);
            }
        }

        return filteredList;
    }

    private void filterPayModes(final PromoCodeResponse promoCodeResponse, EntityPaymentOptionsTO entityInfo) {

        /*
         * Filter NB Banks
         */
        if (!promoCodeResponse.getPromoCodeDetail().getNbBanks().isEmpty()) {
            List<BankInfo> completeNbList = entityInfo.getCompleteNbList();
            List<BankInfo> filteredCompeletNbList = new ArrayList<>();
            for (Long bankID : promoCodeResponse.getPromoCodeDetail().getNbBanks()) {
                for (BankInfo bankInfo : completeNbList) {
                    if (bankInfo.getBankId().equals(bankID)) {
                        filteredCompeletNbList.add(bankInfo);
                    }
                }

            }

            if (!filteredCompeletNbList.isEmpty()) {
                entityInfo.setCompleteNbList(filteredCompeletNbList);
            } else {
                entityInfo.setNetBankingEnabled(false);
            }
        }

    }

    private void mapConsultFeesResponse(final WorkFlowResponseBean responseData, final TransactionInfo txnInfo) {
        if (BeanParamValidator.validateInputObjectParam(responseData.getConsultFeeResponse())
                && BeanParamValidator.validateInputMapParam(responseData.getConsultFeeResponse().getConsultDetails())) {
            for (final Entry<EPayMethod, ConsultDetails> entry : responseData.getConsultFeeResponse()
                    .getConsultDetails().entrySet()) {

                switch (entry.getKey()) {
                case BALANCE:
                    txnInfo.setChargeFeeAmountBALANCE(entry.getValue().getFeeAmount().longValue());
                    break;
                case PAYTM_DIGITAL_CREDIT:
                    txnInfo.setChargeFeeAmountDigitalCredit(entry.getValue().getFeeAmount().longValue());
                    break;
                case ATM:
                    txnInfo.setChargeFeeAmountATM(entry.getValue().getFeeAmount().longValue());
                    break;
                case BANK_EXPRESS:
                    txnInfo.setChargeFeeAmountBankExpress(entry.getValue().getFeeAmount().longValue());
                    break;
                case COD:
                    txnInfo.setChargeFeeAmountCOD(entry.getValue().getFeeAmount().longValue());
                    break;
                case CREDIT_CARD:
                    txnInfo.setChargeFeeAmountCOD(entry.getValue().getFeeAmount().longValue());
                    break;
                case DEBIT_CARD:
                    txnInfo.setChargeFeeAmountDC(entry.getValue().getFeeAmount().longValue());
                    break;
                case EMI:
                    txnInfo.setChargeFeeAmountEMI(entry.getValue().getFeeAmount().longValue());
                    break;
                case IMPS:
                    txnInfo.setChargeFeeAmountIMPS(entry.getValue().getFeeAmount().longValue());
                    break;
                case NET_BANKING:
                    txnInfo.setChargeFeeAmountNetBanking(entry.getValue().getFeeAmount().longValue());
                    break;
                case UPI:
                    txnInfo.setChargeFeeAmountUPI(entry.getValue().getFeeAmount().longValue());
                    break;
                case HYBRID_PAYMENT:
                    txnInfo.setChargeFeeAmountHybrid(entry.getValue().getFeeAmount().longValue());
                    break;
                case PPBL:
                    LOGGER.info("No Fee applicable for PPBL");
                    break;
                case MP_COD:
                    LOGGER.info("No Fee applicable for COD");
                    break;

                }
            }
        }
    }

    private void setOfferMessage(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData,
            final TransactionInfo txnInfo) {
        try {
            final String website = StringUtils.isNotBlank(requestData.getWebsite()) ? requestData.getWebsite()
                    : responseData.getExtendedInfo().get("website");
            String channelID;
            if (StringUtils.isNotBlank(requestData.getChannelId())) {
                channelID = requestData.getChannelId();
            } else {
                final UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
                final ReadableUserAgent userAgent = parser.parse(requestData.getRequest().getHeader(
                        RequestHeaders.USER_AGENT));
                channelID = EnvInfoUtil.getTerminalTypeFromUserAgent(userAgent).getTerminal();
            }
            String message = getOfferMessage(requestData.getMid(), website, channelID);
            txnInfo.setOfferMessage(message);
        } catch (Exception e) {
            LOGGER.error("Could Not fetch offer Message as :: ", e);
        }
    }

    public String getOfferMessage(String mid, String website, String channelId) {
        String offerMessage = null;
        try {
            final MerchantOfferDetailsInput merchantOfferDetailsInput = new MerchantOfferDetailsInput(mid,
                    MappingMerchantOfferDetails.Channel.valueOf(channelId), website);
            final MerchantOfferDetailsResponse offerDetails = merchantOfferDetailsService
                    .getMerchantOfferDetails(merchantOfferDetailsInput);
            if ((offerDetails != null) && offerDetails.isSuccessfullyProcessed()) {
                LOGGER.debug("Obtained Offer Details mapping from cache :: {}", offerDetails);
                final Date currentDate = new Date();
                try {
                    if (offerDetails.getMappingMerchantOfferDetails() != null) {
                        if ((currentDate.compareTo(offerDetails.getMappingMerchantOfferDetails().getValidFrom()) > 0)
                                && (currentDate.compareTo(offerDetails.getMappingMerchantOfferDetails().getValidTo()) < 0)
                                && MappingMerchantOfferDetails.Status.ACTIVE.equals(offerDetails
                                        .getMappingMerchantOfferDetails().getStatus())) {
                            offerMessage = offerDetails.getMappingMerchantOfferDetails().getMessage();
                        } else {
                            LOGGER.debug("Obtained offer message is invalid. Not setting for display");
                        }
                    }

                } catch (final Exception e) {
                    LOGGER.error("exception occured while fetching offerDetails:::{}", offerDetails);
                }

            }
        } catch (Exception e) {
            LOGGER.error("Could Not fetch offer Message as :: ", e);
        }
        return offerMessage;
    }

    public void setTransactionInfoIntoSessionForPostLogin(final PaymentRequestBean requestBean,
            final WorkFlowResponseBean responseBean) {
        final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(requestBean.getRequest());
        mapConsultFeesResponse(responseBean, txnInfo);
        txnInfo.setSsoID(responseBean.getUserDetails().getUserId());
        txnInfo.setSsoToken(responseBean.getUserDetails().getUserToken());
        txnInfo.setMobileno(responseBean.getUserDetails().getMobileNo());
        txnInfo.setSecurityID(responseBean.getMerchnatViewResponse().getSecurityId());
        mapPostLoginPromocodeDetails(requestBean);
    }

    private void mapPostLoginPromocodeDetails(final PaymentRequestBean requestBean) {
        TransactionInfo transactionInfo = theiaSessionDataService.getTxnInfoFromSession(requestBean.getRequest());
        if (null == transactionInfo.getPromoCodeResponse()) {
            return;
        }

        if (TheiaConstant.ExtraConstants.PROMOCODE_TYPE_DISCOUNT.equals(transactionInfo.getPromoCodeResponse()
                .getPromoCodeDetail().getPromocodeTypeName())) {
            EntityPaymentOptionsTO entityInfo = theiaSessionDataService.getEntityPaymentOptions(requestBean
                    .getRequest());
            entityInfo.resetAllPaymentOptionsEnabled();
            if (transactionInfo.getPromoCodeResponse().getPromoCodeDetail().getPaymentModes() != null) {
                resetFlagsForPaymentModes(transactionInfo.getPromoCodeResponse(), entityInfo);
                removeLists(entityInfo, requestBean);
            }
        }
    }

    private void setSubsType(final PaymentRequestBean requestData, final TransactionInfo transactionInfo) {
        if (requestData.getRequestType().equals(TheiaConstant.RequestTypes.SUBSCRIPTION)) {
            if ("PPI".equals(requestData.getSubsPaymentMode()) || StringUtils.isBlank(requestData.getSubsPaymentMode())) {
                if ("Y".equals(requestData.getSubsPPIOnly())) {
                    transactionInfo.setSubscriptionPaymentMode(SubsPaymentMode.PPI.toString());
                } else {
                    transactionInfo.setSubscriptionPaymentMode(SubsPaymentMode.NORMAL.toString());
                }
            } else if ("CC".equals(requestData.getSubsPaymentMode())) {
                transactionInfo.setSubscriptionPaymentMode(SubsPaymentMode.CC.toString());
            } else if ("DC".equals(requestData.getSubsPaymentMode())) {
                transactionInfo.setSubscriptionPaymentMode(SubsPaymentMode.DC.toString());
            } else if ("PPBL".equals(requestData.getSubsPaymentMode())) {
                transactionInfo.setSubscriptionPaymentMode(SubsPaymentMode.PPBL.toString());
            }
        }
    }

}