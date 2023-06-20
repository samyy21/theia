package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.CardSubType;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service("corporateCardUtil")
public class CorporateCardUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorporateCardUtil.class);

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    private static String CARD_NOT_SUPPORTED_MSG = "Corporate Card" + NATIVE_RETRY_ERROR_MESSAGE_PAYMETHOD_NOT_ALLOWED;

    public List<String> prepareCardSubTypeList(BinDetail binDetail) {
        List<String> supportedCardSubType = new ArrayList<>();

        if (binDetail != null) {
            if (binDetail.isCorporateCard()) {
                supportedCardSubType.add(CardSubType.CORPORATE_CARD.getCardSubType());
            }
        }

        return supportedCardSubType;
    }

    public void validateAndCheckCardSubType(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean,
            BinDetail binDetail) throws TheiaDataMappingException {

        if (flowRequestBean.getPaymentTypeId() != null
                && (flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                        || flowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value) || flowRequestBean
                        .getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value))) {
            try {
                List<String> supportedCardSubTypes = prepareCardSubTypeList(binDetail);
                if (binDetail != null && CollectionUtils.isNotEmpty(supportedCardSubTypes)) {
                    // Checking if CardSubType is enabled on Merchant.
                    isCardSubTypeEnabledForMerchant(requestData, supportedCardSubTypes, binDetail, flowRequestBean);
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

    private com.paytm.pgplus.enums.PayMethod getPaymentMethod(WorkFlowRequestBean flowRequestBean, BinDetail binDetail) {
        com.paytm.pgplus.enums.PayMethod payMethod = null;
        if (StringUtils.isNotBlank(flowRequestBean.getPayMethod())) {
            payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(flowRequestBean.getPayMethod());
            if (payMethod == null) {
                payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByOldName(flowRequestBean.getPayMethod());
            }
        } else if (flowRequestBean.getIsSavedCard()) {
            String pMethod = binDetail.getCardType();
            if (PaymentTypeIdEnum.EMI.value.equals(flowRequestBean.getPaymentTypeId())) {
                pMethod = TheiaConstant.ExtraConstants.EMI;
                if (StringUtils.isNotBlank(flowRequestBean.getPaymentRequestBean().getEmitype())
                        && flowRequestBean.getPaymentRequestBean().getEmitype().equals(EmiType.DEBIT_CARD.getType())) {
                    pMethod = TheiaConstant.ExtraConstants.EMI_DC;
                }
            }
            payMethod = com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(pMethod);
        }
        return payMethod;
    }

    private void isCardSubTypeEnabledForMerchant(PaymentRequestBean requestData, List<String> supportedCardSubTypes,
            BinDetail binDetail, WorkFlowRequestBean flowRequestBean) throws TheiaDataMappingException {
        if (requestData.getTxnToken() != null) {
            NativeCashierInfoResponse nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(requestData
                    .getTxnToken());
            boolean isCardSubTypeSupported = false;
            com.paytm.pgplus.enums.PayMethod payMethod = getPaymentMethod(flowRequestBean, binDetail);
            LOGGER.info("bin to be checked for corporate is :{} and paymethod :{}", binDetail.getBin(),
                    (payMethod != null ? payMethod.getMethod() : null));
            if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                    && nativeCashierInfoResponse.getBody().getMerchantPayOption() != null) {
                if (com.paytm.pgplus.enums.PayMethod.CREDIT_CARD.equals(payMethod)
                        || com.paytm.pgplus.enums.PayMethod.DEBIT_CARD.equals(payMethod)) {
                    isCardSubTypeSupported = checkIfCardSupported(nativeCashierInfoResponse.getBody()
                            .getMerchantPayOption(), supportedCardSubTypes, binDetail);
                } else if (com.paytm.pgplus.enums.PayMethod.EMI.equals(payMethod)
                        || com.paytm.pgplus.enums.PayMethod.EMI_DC.equals(payMethod)) {
                    isCardSubTypeSupported = checkIfCardSupportedEMIcase(nativeCashierInfoResponse.getBody()
                            .getMerchantPayOption(), supportedCardSubTypes, binDetail, payMethod, flowRequestBean);
                }
            }
            // check for addMoney and AddNpay case
            if ((flowRequestBean.getPaytmExpressAddOrHybrid() != null && EPayMode.ADDANDPAY.equals(flowRequestBean
                    .getPaytmExpressAddOrHybrid()))) {
                if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                        && nativeCashierInfoResponse.getBody().getAddMoneyPayOption() != null) {
                    isCardSubTypeSupported = checkIfCardSupported(nativeCashierInfoResponse.getBody()
                            .getAddMoneyPayOption(), supportedCardSubTypes, binDetail);
                }
            }

            if (!isCardSubTypeSupported) {
                LOGGER.error(CARD_NOT_SUPPORTED_MSG);
                throw new TheiaDataMappingException(CARD_NOT_SUPPORTED_MSG, ResponseConstants.INVALID_PAYMENTMODE);
            }
        }
    }

    private boolean checkIfCardSupportedEMIcase(PayOption payOption, List<String> supportedCardSubTypes,
            BinDetail binDetail, com.paytm.pgplus.enums.PayMethod payMethod, WorkFlowRequestBean flowRequestBean) {
        boolean isCardSubTypeSupported = false;
        if (CollectionUtils.isNotEmpty(payOption.getPayMethods())) {
            for (PayMethod pMethod : payOption.getPayMethods()) {
                if (pMethod.getPayMethod() != null
                        && (pMethod.getPayMethod().equals(payMethod.getMethod()) || pMethod.getPayMethod().equals(EMI))
                        && pMethod.getPayChannelOptions() != null && !pMethod.getPayChannelOptions().isEmpty()) {
                    for (PayChannelBase payChannelBase : pMethod.getPayChannelOptions()) {
                        if (payChannelBase instanceof EmiChannel) {
                            EmiChannel emiChannel = (EmiChannel) payChannelBase;
                            if (emiChannel.getIsDisabled() != null
                                    && TheiaConstant.ExtraConstants.TRUE.equals(emiChannel.getIsDisabled().getStatus())
                                    && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                            emiChannel.getIsDisabled().getMsg())) {
                                continue;
                            }
                            String emiType = flowRequestBean.getPaymentRequestBean().getEmitype();
                            if (StringUtils.isBlank(emiType)) {
                                emiType = CREDIT_CARD;
                            }
                            if (emiChannel.getInstId().equals(binDetail.getBankCode())
                                    && emiChannel.getEmiType().getType().equals(emiType)) {
                                isCardSubTypeSupported = checkIfPayChannelSupported(supportedCardSubTypes,
                                        emiChannel.getSupportedCardSubTypes());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return isCardSubTypeSupported;
    }

    private boolean checkIfCardSupported(PayOption payOption, List<String> supportedCardSubTypes, BinDetail binDetail) {
        boolean isCardSubTypeSupported = false;
        if (CollectionUtils.isNotEmpty(payOption.getPayMethods())) {
            for (PayMethod payMethod : payOption.getPayMethods()) {
                if (payMethod.getPayMethod() != null && payMethod.getPayMethod().equals(binDetail.getCardType())
                        && payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
                    for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                        if (payChannelBase.getIsDisabled() != null
                                && TheiaConstant.ExtraConstants.TRUE.equals(payChannelBase.getIsDisabled().getStatus())
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannelBase.getIsDisabled().getMsg())) {
                            continue;
                        }
                        if (payChannelBase instanceof BankCard) {
                            BankCard bankCard = (BankCard) payChannelBase;
                            if (bankCard.getInstId().equals(binDetail.getCardName())) {
                                isCardSubTypeSupported = checkIfPayChannelSupported(supportedCardSubTypes,
                                        bankCard.getSupportedCardSubTypes());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return isCardSubTypeSupported;
    }

    private boolean checkIfPayChannelSupported(List<String> supportedCardSubTypesBin,
            List<String> supportedCardSubTypesPayChannel) {

        if (CollectionUtils.isEmpty(supportedCardSubTypesPayChannel)) {
            return false;
        }

        if (!supportedCardSubTypesPayChannel.containsAll(supportedCardSubTypesBin)) {
            return false;
        }

        return true;
    }

    public List<String> prepareCardSubTypeListFromCardBeanz(CardBeanBiz cardBeanBiz) {
        List<String> supportedCardSubType = new ArrayList<>();

        if (cardBeanBiz != null) {
            if (cardBeanBiz.isCorporateCard()) {
                supportedCardSubType.add(CardSubType.CORPORATE_CARD.getCardSubType());
            }
        }

        return supportedCardSubType;
    }
}
