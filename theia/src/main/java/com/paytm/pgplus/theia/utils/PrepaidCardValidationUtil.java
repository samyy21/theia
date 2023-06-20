/*
 * @Dev - Udit Verma
 * @Date - 01/04/20
 */

package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.payview.response.BankCard;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PrepaidCard.PREPAID_CARD_MAX_AMOUNT;

@Service("prepaidCardValidationUtil")
public class PrepaidCardValidationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepaidCardValidationUtil.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    private static String PREPAID_CARD_NOT_SUPPORTED_MSG = "Prepaid Card"
            + NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;
    private static String PREPAID_CARD_AMOUNT_LIMIT_EXCEED_MSG = "Prepaid Card transaction amount Limit Breach";

    private void isPrepaidCardEnabledForMerchant(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean,
            BinDetail binDetail) throws TheiaDataMappingException {
        if (requestData.getTxnToken() != null) {
            NativeCashierInfoResponse nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(requestData
                    .getTxnToken());
            if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                    && nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods() != null) {
                boolean isPrepaidCardSupported = false;
                for (PayMethod payMethod : nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
                    if (payMethod.getPayMethod() != null && payMethod.getPayMethod().equals(binDetail.getCardType())
                            && payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
                        for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                            if (payChannelBase.getIsDisabled() != null
                                    && TheiaConstant.ExtraConstants.TRUE.equals(payChannelBase.getIsDisabled()
                                            .getStatus())
                                    && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                            payChannelBase.getIsDisabled().getMsg())) {
                                continue;
                            }
                            if (payChannelBase instanceof BankCard) {
                                BankCard bankCard = (BankCard) payChannelBase;
                                if (bankCard.getInstId().equals(binDetail.getCardName())) {
                                    isPrepaidCardSupported = BooleanUtils.isTrue(bankCard.isPrepaidCardSupported());
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!isPrepaidCardSupported) {
                    LOGGER.error(PREPAID_CARD_NOT_SUPPORTED_MSG);
                    throw new TheiaDataMappingException(PREPAID_CARD_NOT_SUPPORTED_MSG,
                            ResponseConstants.INVALID_PAYMENTMODE);
                }
            }
        }
    }

    public void validateAndCheckIfPrepaidCard(final PaymentRequestBean requestData,
            final WorkFlowRequestBean flowRequestBean, BinDetail binDetail) throws TheiaDataMappingException {
        if (flowRequestBean.getPaymentTypeId() != null
                && (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                        || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value) || flowRequestBean
                        .getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
            try {
                if (iPgpFf4jClient.checkWithdefault(TheiaConstant.PrepaidCard.FF4J_PREPAID_CARD_STRING,
                        new HashMap<>(), false)) {
                    if (binDetail != null && binDetail.isPrepaidCard()) {
                        flowRequestBean.setPrepaidCard(true);
                        requestData.setPrepaidCard(true);
                        // Blocking Add Money request, in case of Prepaid card.
                        // Blocking Add And Pay request, in case of Prepaid
                        // card.
                        // Blocking EMI request in case of Prepaid card.
                        if (flowRequestBean.isNativeAddMoney()
                                || (flowRequestBean.getPaytmExpressAddOrHybrid() != null && EPayMode.ADDANDPAY
                                        .equals(flowRequestBean.getPaytmExpressAddOrHybrid()))
                                || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
                            LOGGER.error(PREPAID_CARD_NOT_SUPPORTED_MSG);
                            throw new TheiaDataMappingException(PREPAID_CARD_NOT_SUPPORTED_MSG,
                                    ResponseConstants.INVALID_PAYMENTMODE);
                        }

                        // Checking if Prepaid Card is enabled on Merchant.
                        isPrepaidCardEnabledForMerchant(requestData, flowRequestBean, binDetail);

                        // Checking Txn Amount for prepaid card.
                        if (flowRequestBean.getTxnAmount() != null) {
                            if (!isPrepaidCardLimitValid(flowRequestBean.getTxnAmount(), true)) {
                                LOGGER.error(PREPAID_CARD_AMOUNT_LIMIT_EXCEED_MSG);
                                throw new TheiaDataMappingException(PREPAID_CARD_AMOUNT_LIMIT_EXCEED_MSG,
                                        ResponseConstants.PREPAID_AMOUNT_LIMIT_BREACH);
                            }
                        }
                    }
                }
            } catch (TheiaDataMappingException ex) {
                if (processTransactionUtil.isNativeEnhancedRequest(requestData)) {
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                            .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                            .setRetryMsg(ex.getMessage()).setMsg(ex.getMessage()).build();
                }

                if (processTransactionUtil.isNativeJsonRequest(requestData)) {
                    throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                            .isHTMLResponse(false).isRetryAllowed(true).setMsg(ex.getMessage())
                            .setRetryMsg(ex.getMessage()).build();
                }
                throw ex;
            }
        }
    }

    public boolean isPrepaidCardLimitValid(String txnAmount, boolean amountInPaisa) {
        if (StringUtils.isNotEmpty(txnAmount)) {
            String ppAmount = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(PREPAID_CARD_MAX_AMOUNT,
                    "100000");
            if (amountInPaisa) {
                ppAmount = AmountUtils.getTransactionAmountInPaise(ppAmount);
            }
            Double maxAmount = Double.parseDouble(ppAmount);
            if (Double.parseDouble(txnAmount) > maxAmount) {
                return false;
            }
        }
        return true;
    }
}