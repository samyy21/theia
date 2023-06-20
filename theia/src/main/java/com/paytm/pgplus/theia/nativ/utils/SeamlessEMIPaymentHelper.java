package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.sessiondata.EMIInfo;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component("seamlessEMIPaymentHelper")
public class SeamlessEMIPaymentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessEMIPaymentHelper.class);

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    public void builSeamlessEMIPaymentRequest(Map<String, String> channelInfo, PaymentRequestBean paymentRequestBean,
            EntityPaymentOptionsTO entityPaymentOptions) {
        buildChannelInfo(channelInfo, entityPaymentOptions, paymentRequestBean);
    }

    public void buildSeamlessEMIPaymentRequest(Map<String, String> channelInfo, PaymentRequestBean paymentRequestBean) {

        LOGGER.info("Building SeamlessEMIPaymentRequest");

        if (null != paymentRequestBean.getEmiChannelInfo()) {
            channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_EMI, "Y");
            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, paymentRequestBean.getEmiChannelInfo()
                    .getPlanId());

            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, paymentRequestBean.getEmiChannelInfo()
                    .getTenureId());
            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, paymentRequestBean.getEmiChannelInfo()
                    .getCardAcquiringMode().getCardAcquiringMode());
            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, paymentRequestBean.getEmiChannelInfo()
                    .getOfMonths());
            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, paymentRequestBean.getEmiChannelInfo()
                    .getInterestRate());
            if (null != paymentRequestBean.getEmiChannelInfo().getMinAmount()) {
                channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, paymentRequestBean.getEmiChannelInfo()
                        .getMinAmount().getAmount());
            }
            LOGGER.info("channelInfo is : {}", channelInfo);
            return;
        }
    }

    private void buildChannelInfo(Map<String, String> channelInfo, EntityPaymentOptionsTO entityPaymentOptions,
            PaymentRequestBean paymentRequestBean) {
        channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_EMI, "Y");
        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, paymentRequestBean.getEmiPlanID());

        if (!CollectionUtils.isEmpty(entityPaymentOptions.getCompleteEMIInfoList())) {
            for (BankInfo bankInfo : entityPaymentOptions.getCompleteEMIInfoList()) {
                for (EMIInfo eMIInfo : bankInfo.getEmiInfo()) {
                    if (eMIInfo.getInstId().equals(paymentRequestBean.getBankCode())
                            && eMIInfo.getPlanId().equals(paymentRequestBean.getEmiPlanID())) {

                        if (eMIInfo.isAggregator()) {
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, eMIInfo.getAggregatorPlanId());
                        }

                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, eMIInfo.getTenureId());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, eMIInfo.getCardAcquiringMode());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, eMIInfo.getOfMonths());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, eMIInfo.getInterestRate());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, eMIInfo.getEmiAmount());
                        return;
                    }
                }
            }
        } else if (!CollectionUtils.isEmpty(entityPaymentOptions.getHybridEMIInfoList())) {
            for (BankInfo bankInfo : entityPaymentOptions.getHybridEMIInfoList()) {
                for (EMIInfo eMIInfo : bankInfo.getEmiInfo()) {
                    if (eMIInfo.getInstId().equals(paymentRequestBean.getBankCode())
                            && eMIInfo.getPlanId().equals(paymentRequestBean.getEmiPlanID())) {
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, eMIInfo.getTenureId());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, eMIInfo.getCardAcquiringMode());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, eMIInfo.getOfMonths());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, eMIInfo.getInterestRate());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, eMIInfo.getEmiAmount());
                        return;
                    }
                }
            }
        }
    }

    public void buildSeamlessEMIPaymentRequestNative(Map<String, String> channelInfo, PaymentRequestBean requestData,
            NativeCashierInfoResponse cashierInfoResponse) {
        channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_EMI, "Y");
        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, requestData.getEmiPlanID());
        PayMethod payMethod = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(paymethod -> EPayMethod.EMI.getMethod().equals(paymethod.getPayMethod())).findAny()
                .orElse(null);
        EmiType emiType = EmiType.CREDIT_CARD;
        if (StringUtils.isNotBlank(requestData.getEmitype())
                && requestData.getEmitype().equals(EmiType.DEBIT_CARD.getType())) {
            emiType = EmiType.DEBIT_CARD;
        }
        if (payMethod != null && !CollectionUtils.isEmpty(payMethod.getPayChannelOptions())) {
            for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                EmiChannel emiChannel = (EmiChannel) payChannelBase;
                if (emiChannel.getIsDisabled() != null
                        && TheiaConstant.ExtraConstants.TRUE.equals(emiChannel.getIsDisabled().getStatus())
                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                emiChannel.getIsDisabled().getMsg())) {
                    continue;
                }
                for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                    if (emiChannel.getInstId().equals(requestData.getBankCode())
                            && emiChannelInfo.getPlanId().equals(requestData.getEmiPlanID())
                            && emiType.equals(emiChannel.getEmiType())) {
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, emiChannelInfo.getTenureId());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, emiChannelInfo.getCardAcquiringMode()
                                .getCardAcquiringMode());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, emiChannelInfo.getOfMonths());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, emiChannelInfo.getInterestRate());
                        if (null != emiChannelInfo.getEmiAmount()) {
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, emiChannelInfo.getEmiAmount()
                                    .getValue());
                        }
                        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARDHOLDER_ADD1,
                                requestData.getAddress1());
                        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARDHOLDER_ADD2,
                                requestData.getPincode());
                        return;
                    }
                }
            }
        }
    }
}
