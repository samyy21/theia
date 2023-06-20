package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.request.BalanceChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.AccountInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.session.utils.EntityPaymentOptionSessionUtil;
import com.paytm.pgplus.theia.session.utils.TransactionInfoSessionUtil;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePayviewConsultServiceHelper<Req, Res> implements IPayviewConsultServiceHelper<Req, Res> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePayviewConsultServiceHelper.class);

    @Autowired
    private EntityPaymentOptionSessionUtil entityPaymentOptionSessionUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("transactionInfoSessionUtil")
    private TransactionInfoSessionUtil transactionInfoSessionUtil;

    @Override
    public EntityPaymentOptionsTO getEntityPaymentOption(WorkFlowResponseBean workFlowResponseBean, String txnToken) {
        EntityPaymentOptionsTO entityPaymentOptionsTO = new EntityPaymentOptionsTO();
        if (workFlowResponseBean.getMerchnatLiteViewResponse() != null
                && CollectionUtils.isNotEmpty(workFlowResponseBean.getMerchnatLiteViewResponse().getPayMethodViews())) {
            for (PayMethodViewsBiz payMethodViewsBiz : workFlowResponseBean.getMerchnatLiteViewResponse()
                    .getPayMethodViews()) {
                if (PayMethod.DEBIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())
                        && CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayChannelOptionViews())) {
                    for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                        if (CollectionUtils.isNotEmpty(payChannelOptionViewBiz.getDirectServiceInsts())) {
                            LOGGER.debug(
                                    "Adding directServiceInsts to entityPaymentOptionsTo in BasePayViewConsultServiceHelper :{}",
                                    payChannelOptionViewBiz.getDirectServiceInsts());
                            for (String channel : payChannelOptionViewBiz.getDirectServiceInsts()) {
                                entityPaymentOptionsTO.getDirectServiceInsts().add(channel + "@DEBIT_CARD");
                            }
                        }
                        if (CollectionUtils.isNotEmpty(payChannelOptionViewBiz.getSupportAtmPins())) {
                            LOGGER.debug(
                                    "Adding supportAtmPins to entityPaymentOptionsTo in BasePayViewConsultServiceHelper :{}",
                                    payChannelOptionViewBiz.getSupportAtmPins());
                            entityPaymentOptionsTO.getSupportAtmPins().addAll(
                                    payChannelOptionViewBiz.getSupportAtmPins());
                        }
                    }
                }
            }
        }
        return entityPaymentOptionsTO;
    }

    @Override
    public Map<String, Object> getPayViewConsultCacheInfo(WorkFlowResponseBean workFlowResponseBean) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(workFlowResponseBean.getPwpEnabled())) {
            map.put("pwpEnabled", workFlowResponseBean.getPwpEnabled());
        }

        return map;

    }

    private String getEmiOptions(InitiateTransactionRequestBody orderDetail) {
        List<PaymentMode> enablePaymentModes = orderDetail.getEnablePaymentMode();
        List<PaymentMode> disablePaymentModes = orderDetail.getDisablePaymentMode();
        StringBuilder emiOptions = new StringBuilder("");
        if (!StringUtils.isEmpty(orderDetail.getEmiOption())
                && StringUtils.startsWithIgnoreCase(orderDetail.getEmiOption(),
                        TheiaConstant.ExtraConstants.ZERO_COST_EMI)) {
            return orderDetail.getEmiOption();
        }
        if (isEmiEnabled(enablePaymentModes)) {
            return "SHOW_ALL";
        } else if (isEmiDisabled(disablePaymentModes)) {
            return "DROP_ALL";
        } else {
            if (enablePaymentModes != null) {
                for (PaymentMode paymentMode : enablePaymentModes) {
                    if (PayMethod.EMI.getMethod().equals(paymentMode.getMode())) {
                        for (String channel : paymentMode.getChannels()) {
                            emiOptions.append(channel).append("-SHOW_ALL").append(";");
                        }
                    }
                }
            }
            if (disablePaymentModes != null) {
                for (PaymentMode paymentMode : disablePaymentModes) {
                    if (PayMethod.EMI.getMethod().equals(paymentMode.getMode())) {
                        for (String channel : paymentMode.getChannels()) {
                            emiOptions.append(channel).append("-DROP_ALL").append(";");
                        }
                    }
                }
            }
        }
        return emiOptions.toString();
    }

    private boolean isEmiDisabled(List<PaymentMode> disablePaymentModes) {
        if (null != disablePaymentModes) {
            for (PaymentMode paymentMode : disablePaymentModes) {
                if (PayMethod.EMI.getMethod().equals(paymentMode.getMode())
                        && (null == paymentMode.getChannels() || paymentMode.getChannels().isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmiEnabled(List<PaymentMode> enablePaymentModes) {
        if (null == enablePaymentModes) {
            return true;
        } else {
            for (PaymentMode paymentMode : enablePaymentModes) {
                if (PayMethod.EMI.getMethod().equals(paymentMode.getMode())
                        && (null == paymentMode.getChannels() || paymentMode.getChannels().isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Double getWalletBalance(WorkFlowResponseBean workFlowResponseBean) {
        for (PayMethodViewsBiz payMethodViewsBiz : workFlowResponseBean.getMerchnatLiteViewResponse()
                .getPayMethodViews()) {
            if (PayMethod.BALANCE.getMethod().equals(payMethodViewsBiz.getPayMethod())
                    && CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayChannelOptionViews())) {
                for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                    AccountInfo accountInfo = getBalanceInfo(payChannelOptionViewBiz.getBalanceChannelInfos());
                    if (null != accountInfo && null != accountInfo.getAccountBalance()
                            && StringUtils.isNotEmpty(accountInfo.getAccountBalance().getValue())) {
                        return Double.valueOf(accountInfo.getAccountBalance().getValue());
                    }
                }
            }
        }
        return null;
    }

    private AccountInfo getBalanceInfo(List<BalanceChannelInfoBiz> balanceChannelInfoBizs) {
        if (balanceChannelInfoBizs == null || balanceChannelInfoBizs.isEmpty() || balanceChannelInfoBizs.get(0) == null)
            return null;
        if (StringUtils.isEmpty(balanceChannelInfoBizs.get(0).getPayerAccountNo()))
            return null;
        AccountInfo balanceInfo = new AccountInfo(balanceChannelInfoBizs.get(0).getPayerAccountNo(), new Money(
                balanceChannelInfoBizs.get(0).getAccountBalance()));
        return balanceInfo;
    }

    @Override
    public void trimCcDcPayChannels(Res response) {
        // BLANK
    }

    @Override
    public void trimByTopNBChannels(Res response) {
        // BLANK
    }

    @Override
    public void trimEmiChannelInfo(Res response) {
        // BLANK
    }

    @Override
    public void populateZestData(Res response) {
        // BLANK
    }

    @Override
    public void filterPayChannelinUPINative(Res response) {
        // BLANK
    }

    @Override
    public void filterZestFromNBInNative(Res response) {
        // BLANK
    }

    @Override
    public void setMerchantLimit(WorkFlowResponseBean workFlowResponseBean,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        // BLANK
    }

    @Override
    public void setMerchantOfferMessage(NativeCashierInfoRequest request, CashierInfoRequest serviceRequest,
            NativeCashierInfoResponse response) {
        String mid = serviceRequest.getHead().getMid();
        String channelId = request.getHead().getChannelId().toString();
        String website = serviceRequest.getBody().getWebsite();
        String message = transactionInfoSessionUtil.getOfferMessage(mid, website, channelId);
        response.getBody().setMerchantOfferMessage(message);
    }

    @Override
    public void trimAdditionalInfoForSavedAssets(NativeCashierInfoResponse response) {

    }
}
