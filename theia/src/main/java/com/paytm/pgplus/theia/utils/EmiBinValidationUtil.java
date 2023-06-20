package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.core.payment.utils.PaymentHelper;
import com.paytm.pgplus.biz.mapping.models.EMIValidBinsData;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.datamapper.dto.EMIDetailMethodDTO;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.payview.response.EMIChannelInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.EmiChannel;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.sessiondata.EMIInfo;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service("emiBinValidationUtil")
public class EmiBinValidationUtil {

    public enum CardAcquiringMode {
        OFUS, ONUS, AGGREGATOR;
    }

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    public static final BigDecimal BIG_DECIMAL_1200 = new BigDecimal("1200");

    public static final BigDecimal BIG_DECIMAL_1 = new BigDecimal("1");

    private static final Logger LOGGER = LoggerFactory.getLogger(EmiBinValidationUtil.class);

    public boolean isValidEmiCardDetailsEntered(TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptions, BinDetail binDetail) throws PaytmValidationException {
        return isValidEmiCardDetailsEntered(entityPaymentOptions.getCompleteEMIInfoList(), binDetail,
                theiaPaymentRequest.getEmiPlanID());
    }

    public boolean isValidEmiCardDetailsEntered(List<BankInfo> completeEMIInfoList, BinDetail binDetail,
            String emiPlanId) {
        if (binDetail == null || binDetail.getBin() == null) {
            return false;
        }
        String planID = null;
        String isOfus = null;

        String bankID = StringUtils.substringBefore(emiPlanId, "|");
        String eMIMonths = StringUtils.substringAfter(emiPlanId, "|");
        String binNumber = String.valueOf(binDetail.getBin());

        if (null != completeEMIInfoList) {

            for (BankInfo bankInfo : completeEMIInfoList) {

                if (bankInfo != null && bankInfo.getBankName().equals(bankID)) {

                    for (EMIInfo emiInfo : bankInfo.getEmiInfo()) {

                        if (emiInfo.getOfMonths().equals(eMIMonths)) {

                            if (StringUtils.isNotBlank(emiInfo.getAggregatorPlanId())
                                    && StringUtils.isNotBlank(emiInfo.getTenureId())) {
                                planID = emiInfo.getAggregatorPlanId();
                                isOfus = "0";
                            } else {
                                BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(bankInfo
                                        .getBankName());

                                if (bankInfoData == null) {
                                    return false;
                                }
                                planID = String.valueOf(bankInfoData.getBankId());
                                isOfus = "1";

                                if (CardAcquiringMode.ONUS.toString().equals(emiInfo.getCardAcquiringMode())) {
                                    isOfus = "2";
                                }

                            }

                            StringBuilder sb = new StringBuilder();
                            sb.append(planID).append("|").append(isOfus);

                            EMIValidBinsData validBins = configurationDataService.getEmiValidBins(sb.toString());

                            if (null != validBins && !validBins.getValidBins().isEmpty()) {

                                if (validBins.getValidBins().contains(binNumber)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public BigDecimal calculateEmiAmount(String transactionAmount, String interestRate, String months, String instId) {
        LOGGER.info("Transaction Amount : {} , Intereset Rate : {} , Months : {}", transactionAmount, interestRate,
                months);
        BigDecimal serviceAmount = new BigDecimal(transactionAmount);
        BigDecimal emiValue = new BigDecimal(0);
        try {
            BigDecimal rate = new BigDecimal(interestRate).divide(BIG_DECIMAL_1200, 10, RoundingMode.HALF_UP);
            if (rate.doubleValue() < 0) {
                throw new RuntimeException("EMI Rate configured is too low :" + rate.toPlainString());
            } else if (rate.doubleValue() == 0) {
                return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
            }
            if (TheiaConstant.ExtraConstants.IDBI.equalsIgnoreCase(instId)) {
                LOGGER.info("Calculating emi for IDBI bank with instId {}", instId);
                emiValue = PaymentHelper.calculateIdBIEmi(transactionAmount, interestRate, months);
            } else {
                BigDecimal noOfMonths = new BigDecimal(months);
                emiValue = serviceAmount
                        .multiply(rate)
                        .multiply(rate.add(BIG_DECIMAL_1).pow(noOfMonths.intValue()))
                        .divide((rate.add(BIG_DECIMAL_1).pow(noOfMonths.intValue())).subtract(BIG_DECIMAL_1), 2,
                                RoundingMode.HALF_UP);
            }
            return emiValue;
        } catch (Exception e) {
            LOGGER.error("Exception Occurred , " + e.getMessage());
            // LOGGER.info("Returning EMI Amount as trans amount divided by number of months");
            return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal calculateTotalAmount(String emiAmount, String months) {
        LOGGER.info("Emi Amount : {} , Months : {}", emiAmount, months);
        BigDecimal emiValue = new BigDecimal(emiAmount);
        BigDecimal noOfMonths = new BigDecimal(months);
        BigDecimal totalAmount = emiValue.multiply(noOfMonths);
        return totalAmount;
    }

    public boolean isValidEmiCardDetailsEnteredNative(List<PayChannelBase> emiChannels, BinDetail binDetail,
            String emiPlanId, String emiType) {
        if (binDetail == null || binDetail.getBin() == null) {
            return false;
        }
        String planID = null;
        String isOfus = null;

        String bankID = StringUtils.substringBefore(emiPlanId, "|");
        String eMIMonths = StringUtils.substringAfter(emiPlanId, "|");
        String binNumber = String.valueOf(binDetail.getBin());

        if (null != emiChannels) {

            for (PayChannelBase payChannelBase : emiChannels) {

                EmiChannel emiChannel = (EmiChannel) payChannelBase;
                if (emiChannel.getIsDisabled() != null
                        && TheiaConstant.ExtraConstants.TRUE.equals(emiChannel.getIsDisabled().getStatus())
                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                emiChannel.getIsDisabled().getMsg())) {
                    continue;
                }
                if (emiType != null && !emiChannel.getEmiType().getType().equals(emiType)) {
                    continue;
                }

                if (emiChannel != null && emiChannel.getInstId().equals(bankID)) {

                    for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {

                        if (emiChannelInfo.getOfMonths().equals(eMIMonths)) {

                            if (StringUtils.isNotBlank(emiChannelInfo.getPlanId())
                                    && StringUtils.isNotBlank(emiChannelInfo.getTenureId())) {
                                planID = emiChannelInfo.getPlanId();
                                isOfus = "0";
                            } else {
                                BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(emiChannel
                                        .getInstId());

                                if (bankInfoData == null) {
                                    return false;
                                }
                                planID = String.valueOf(bankInfoData.getBankId());
                                isOfus = "1";

                                if (CardAcquiringMode.ONUS.toString().equals(
                                        emiChannelInfo.getCardAcquiringMode().getCardAcquiringMode())) {
                                    isOfus = "2";
                                }

                            }

                            StringBuilder sb = new StringBuilder();
                            sb.append(planID).append("|").append(isOfus);

                            if (EmiType.CREDIT_CARD.equals(emiChannel.getEmiType())) {
                                EMIValidBinsData validBins = configurationDataService.getEmiValidBins(sb.toString());

                                if (null != validBins && !validBins.getValidBins().isEmpty()) {
                                    if (validBins.getValidBins().contains(binNumber)) {
                                        return true;
                                    }
                                    LOGGER.error("Bin {} not eligible for EMI", binNumber);
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void validateEmiDetailsForPaytmExpress(String bankCode, String bankName,
            EMIDetailMethodDTO emiDetailMethodDTO) throws TheiaServiceException {
        if (bankName == null || emiDetailMethodDTO == null) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: No record found for bin");
        }

        if (!bankName.equals(emiDetailMethodDTO.getBankCode()) && !bankCode.equals(emiDetailMethodDTO.getBankCode())) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for emi transaction: Card number is not valid for given bank code");
        }

        return;
    }

}
