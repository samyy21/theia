/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.payment.utils;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.model.wallet.PGPlusWalletDecisionMakerRequestBizBean;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WalletLimits;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.enums.ProductCodes;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.WALLET_TYPE;

/**
 * @author amitdubey
 * @date Jan 16, 2017
 */
@Component("pgPlusWalletDecisionMaker")
public class PGPlusWalletDecisionMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PGPlusWalletDecisionMaker.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PGPlusWalletDecisionMaker.class);

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    private PaymentHelper paymentHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    public GenericCoreResponseBean<EPayMode> allowedPayMode(
            final PGPlusWalletDecisionMakerRequestBizBean allowedPayModeDecisionRequestBean,
            WorkFlowTransactionBean workFlowTransBean) {
        if (allowedPayModeDecisionRequestBean == null) {
            LOGGER.debug("Returning None as PG Plus Wallet decision as request bean is Null ");
            return new GenericCoreResponseBean<EPayMode>("InvalidAllowedPayModeRequestBean");
        }

        boolean isAddMoneyMid = StringUtils.isNotBlank(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID).equalsIgnoreCase(
                        allowedPayModeDecisionRequestBean.getPaytmMID());

        if (isAddMoneyMid || workFlowTransBean.getWorkFlowBean().isNativeAddMoney()) {
            Double txnAmt = null;
            if (isAddMoneyMid && StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getInitialAddMoneyAmount())) {
                // PGP-24240
                txnAmt = Double.parseDouble(AmountUtils.getTransactionAmountInPaise(workFlowTransBean.getWorkFlowBean()
                        .getInitialAddMoneyAmount()));
            } else {
                txnAmt = Double.valueOf(allowedPayModeDecisionRequestBean.getTxnAmount());
            }

            BizWalletConsultResponse bizWalletConsultResponse = checkWalletLimits(allowedPayModeDecisionRequestBean,
                    txnAmt);

            String customerType = bizWalletConsultResponse.getWalletRbiType();
            if (bizWalletConsultResponse.isLimitApplicable()) {

                if ("RWL_1004".equals(bizWalletConsultResponse.getLimitMessage())) {
                    EXT_LOGGER
                            .customInfo("limit is applicable on user with Limit message RWL_1004, setting EPayMode as LIMIT_REJECT");
                    WalletLimits walletLimits = new WalletLimits(bizWalletConsultResponse.isLimitApplicable(),
                            bizWalletConsultResponse.getLimitMessage(), bizWalletConsultResponse.getMessage());

                    workFlowTransBean.setWalletLimits(walletLimits);
                } else
                    EXT_LOGGER.customInfo("limit is applicable on user, setting EPayMode as LIMIT_REJECT");

                return new GenericCoreResponseBean<EPayMode>(EPayMode.LIMIT_REJECT);

            }

            workFlowTransBean.setAddMoneyDestination(bizWalletConsultResponse.getAddMoneyDestination());
            if ((StringUtils.equals(BizConstant.BASIC_KYC, customerType) || StringUtils.equals(
                    BizConstant.PRIMITIVE_KYC, customerType))
                    && StringUtils.equals(bizWalletConsultResponse.getAddMoneyDestination(),
                            BizConstant.AddMoneyDestination.MAIN)) {
                LOGGER.debug("user is {} and addMoneyDestination is {}", customerType,
                        bizWalletConsultResponse.getAddMoneyDestination());
                return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE_KYC);
            } else {
                return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE);
            }
        }

        double walletBalance = fetchWalletBalance(allowedPayModeDecisionRequestBean.getConsultPayViewResponseBean());
        // added for offline flow

        if (walletBalance < 0) {
            walletBalance = fetchWalletBalance(allowedPayModeDecisionRequestBean.getLitePayviewConsultResponseBizBean());
        }

        if (walletBalance < 0) {
            LOGGER.debug("Returning None as PG Plus Wallet decision as Wallet Balance not found in consult");
            return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE);
        }
        double trasactionAmount = Double.valueOf(allowedPayModeDecisionRequestBean.getTxnAmount());

        if (walletBalance >= trasactionAmount
                || (NumberUtils.compare(walletBalance, NumberUtils.DOUBLE_ZERO) == 0 && workFlowTransBean
                        .getWorkFlowBean().isAddNPayDisabledForZeroBalance())) {
            LOGGER.debug("Returning None as PG Plus Wallet decision as Wallet Balance is sufficient "
                    + "/ Wallet balance is zero & disable add n pay for zero balance preference is enabled");
            return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE);
        }

        if (workFlowTransBean.getWorkFlowBean().isOverrideAddNPayBehaviourEnabled()) {
            EPayMode payMode = checkPayModeAndReturn(workFlowTransBean);
            return new GenericCoreResponseBean<EPayMode>(payMode);
        }

        double differenceAmount = trasactionAmount - walletBalance;

        EPayMode payModeWithKYCConsult = checkAddAndPayAllowedWithKYCConsult(allowedPayModeDecisionRequestBean,
                differenceAmount, workFlowTransBean);

        if (payModeWithKYCConsult != null) {
            LOGGER.debug("Returning Add And Pay as PG Plus Wallet decision");
            return new GenericCoreResponseBean<EPayMode>(payModeWithKYCConsult);
        }

        if (walletBalance > 0) {
            return checkHybridAndReturn(allowedPayModeDecisionRequestBean);
        }
        LOGGER.debug("Returning None as PG Plus Wallet decision");
        return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE);
    }

    private EPayMode checkAddAndPayAllowedWithKYCConsult(
            PGPlusWalletDecisionMakerRequestBizBean allowedPayModeDecisionRequestBean, double differenceAmount,
            WorkFlowTransactionBean workFlowTransBean) {

        boolean isAddAndPayAllowedToMerchant = isAddAndPayAllowedByMerchant(allowedPayModeDecisionRequestBean);

        if (!isAddAndPayAllowedToMerchant)
            return null;

        allowedPayModeDecisionRequestBean.setAddAndPay(true);
        BizWalletConsultResponse bizWalletConsultResponse = checkWalletLimits(allowedPayModeDecisionRequestBean,
                differenceAmount);

        if (bizWalletConsultResponse.isLimitApplicable()) {
            EXT_LOGGER
                    .customInfo("isLimitApplicable is true, setting EPayMode as NONE and removing AddAndPayLiteViewConsult ");
            workFlowTransBean.setAddAndPayLiteViewConsult(null);
            workFlowTransBean.setAddAndPayViewConsult(null);

            WalletLimits walletLimits = new WalletLimits(bizWalletConsultResponse.isLimitApplicable(),
                    bizWalletConsultResponse.getLimitMessage(), bizWalletConsultResponse.getMessage());

            workFlowTransBean.setWalletLimits(walletLimits);
            return EPayMode.NONE;
        }
        String customerType = bizWalletConsultResponse.getWalletRbiType();

        workFlowTransBean.setAddMoneyDestination(bizWalletConsultResponse.getAddMoneyDestination());

        if ((StringUtils.equals(BizConstant.BASIC_KYC, customerType) || StringUtils.equals(BizConstant.PRIMITIVE_KYC,
                customerType))
                && StringUtils.equals(bizWalletConsultResponse.getAddMoneyDestination(),
                        BizConstant.AddMoneyDestination.MAIN)) {
            return EPayMode.ADDANDPAY_KYC;
        }

        return EPayMode.ADDANDPAY;

    }

    // public EPayMode check

    private GenericCoreResponseBean<EPayMode> checkHybridAndReturn(
            final PGPlusWalletDecisionMakerRequestBizBean allowedPayModeDecisionRequestBean) {
        boolean isHybridPaymentEnabled = (allowedPayModeDecisionRequestBean.getConsultPayViewResponseBean() != null) ? isHybridPaymentEnabled(allowedPayModeDecisionRequestBean
                .getConsultPayViewResponseBean()) : isHybridPaymentEnabled(allowedPayModeDecisionRequestBean
                .getLitePayviewConsultResponseBizBean());
        if (isHybridPaymentEnabled) {
            LOGGER.debug("Returning Hybrid as PG Plus Wallet decision");
            return new GenericCoreResponseBean<EPayMode>(EPayMode.HYBRID);
        }
        LOGGER.debug("Returning Add And Pay as PG Plus Wallet decision");
        return new GenericCoreResponseBean<EPayMode>(EPayMode.NONE);
    }

    private BizWalletConsultResponse checkWalletLimits(
            final PGPlusWalletDecisionMakerRequestBizBean allowedPayModeDecisionRequestBean, double differenceAmount) {
        final ConsultWalletLimitsRequest walletConsultRequest = paymentHelper.createConsultWalletLimitRequest(
                differenceAmount, allowedPayModeDecisionRequestBean.getUserID(),
                allowedPayModeDecisionRequestBean.getOrderID());
        walletConsultRequest.setGvFlag(allowedPayModeDecisionRequestBean.isGvFlag());
        walletConsultRequest.setTransitWallet(allowedPayModeDecisionRequestBean.isTransitWallet());
        walletConsultRequest.setTargetPhoneNo(allowedPayModeDecisionRequestBean.getTargetPhoneNo());
        walletConsultRequest.setAddAndPay(allowedPayModeDecisionRequestBean.isAddAndPay());
        walletConsultRequest.setTotalTxnAmount(Double.valueOf(allowedPayModeDecisionRequestBean.getTxnAmount()));
        BizWalletConsultResponse bizWalletConsultResponse = bizPaymentService
                .walletLimitsConsultV2(walletConsultRequest);
        return bizWalletConsultResponse;
    }

    private boolean isHybridPaymentEnabled(final ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        return isHybridPayEnabled(consultPayViewResponseBean)
                && arePayModesOtherThanWalletPresent(consultPayViewResponseBean);
    }

    public boolean isHybridPaymentEnabled(final LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        return isHybridPayEnabled(litePayviewConsultResponseBizBean)
                && arePayModesOtherThanWalletPresent(litePayviewConsultResponseBizBean);
    }

    private boolean isHybridPayEnabled(ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        if (consultPayViewResponseBean == null)
            return false;

        return isHybridPayEnabled(consultPayViewResponseBean.getExtendInfo());
    }

    private boolean isHybridPayEnabled(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        if (litePayviewConsultResponseBizBean == null)
            return false;

        return isHybridPayEnabled(litePayviewConsultResponseBizBean.getExtendInfo());
    }

    public boolean isHybridPayEnabled(Map<String, String> extendInfoMap) {
        if (null != extendInfoMap && !extendInfoMap.isEmpty()) {
            String addAndPayAllowed = extendInfoMap
                    .get(BizConstant.ExtendedInfoKeys.ConsultResponse.HYBRID_ENABLED_KEY);
            if (BizConstant.ExtendedInfoKeys.ConsultResponse.HYBRID_ENABLED_YES.equals(addAndPayAllowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean arePayModesOtherThanWalletPresent(final ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        if (consultPayViewResponseBean != null) {
            if ((consultPayViewResponseBean.getPayMethodViews() != null)
                    && (consultPayViewResponseBean.getPayMethodViews().size() > 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean arePayModesOtherThanWalletPresent(
            final LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        if (litePayviewConsultResponseBizBean != null) {
            if ((litePayviewConsultResponseBizBean.getPayMethodViews() != null)
                    && (litePayviewConsultResponseBizBean.getPayMethodViews().size() > 1)) {
                return true;
            }
        }
        return false;
    }

    private double fetchWalletBalance(final ConsultPayViewResponseBizBean consultPayViewResponseBean) {
        if (consultPayViewResponseBean == null) {
            return -1d;
        }
        return fetchWalletBalance(consultPayViewResponseBean.getPayMethodViews());
    }

    private double fetchWalletBalance(final LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        if (litePayviewConsultResponseBizBean == null) {
            return -1d;
        }
        return fetchWalletBalance(litePayviewConsultResponseBizBean.getPayMethodViews());
    }

    private double fetchWalletBalance(final List<PayMethodViewsBiz> payMethodViewsBizList) {
        if (payMethodViewsBizList == null) {
            return -1d;
        }
        try {
            for (final PayMethodViewsBiz paymethod : payMethodViewsBizList) {
                if (WALLET_TYPE.equals(paymethod.getPayMethod())) {
                    if (!paymethod.getPayChannelOptionViews().isEmpty()
                            && paymethod.getPayChannelOptionViews().get(0) != null
                            && !paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().isEmpty()
                            && paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().get(0) != null) {
                        String balance = paymethod.getPayChannelOptionViews().get(0).getBalanceChannelInfos().get(0)
                                .getAccountBalance();
                        if (StringUtils.isNumeric(balance)) {
                            return Double.parseDouble(balance);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception Occurred , ", e);
        }
        return -1d;
    }

    private boolean isAddAndPayAllowedByMerchant(
            final PGPlusWalletDecisionMakerRequestBizBean allowedPayModeDecisionRequestBean) {
        if (allowedPayModeDecisionRequestBean == null
                || (allowedPayModeDecisionRequestBean.getConsultPayViewResponseBean() == null && allowedPayModeDecisionRequestBean
                        .getLitePayviewConsultResponseBizBean() == null))
            return false;

        Map<String, String> extendInfoMap = (allowedPayModeDecisionRequestBean.getConsultPayViewResponseBean() != null) ? allowedPayModeDecisionRequestBean
                .getConsultPayViewResponseBean().getExtendInfo() : allowedPayModeDecisionRequestBean
                .getLitePayviewConsultResponseBizBean().getExtendInfo();
        return isAddAndPayAllowedByMerchant(extendInfoMap);
    }

    public boolean isAddAndPayAllowedByMerchant(Map<String, String> extendInfoMap) {
        if (null != extendInfoMap && !extendInfoMap.isEmpty()) {
            String addAndPayAllowed = extendInfoMap
                    .get(BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_KEY);
            if (BizConstant.ExtendedInfoKeys.ConsultResponse.ADDANDPAY_ENABLED_YES.equals(addAndPayAllowed)) {
                return true;
            }
        }
        return false;
    }

    /*
     * this method is generated for the cases when transaction amount is not
     * given so decision of payMode is done here with merchantLitePayView
     * Response only
     */
    public EPayMode allowedPayMode(WorkFlowTransactionBean workFlowTransactionBean) {
        // case when balance is disabled in merhchantPaymethod or removed (in
        // case of zeroCostEmi)
        LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean = workFlowTransactionBean
                .getMerchantLiteViewConsult();
        Double balance = fetchWalletBalance(litePayviewConsultResponseBizBean);
        if (balance < 0) {
            return EPayMode.NONE;
        }
        if (workFlowTransactionBean.getWorkFlowBean().isOverrideAddNPayBehaviourEnabled()) {
            return checkPayModeAndReturn(workFlowTransactionBean);
        }
        if (isAddAndPayAllowedByMerchant(litePayviewConsultResponseBizBean.getExtendInfo())) {
            return EPayMode.ADDANDPAY;
        }
        if (balance > 0 && isHybridPaymentEnabled(litePayviewConsultResponseBizBean)) {
            return EPayMode.HYBRID;
        }
        return EPayMode.NONE;
    }

    public EPayMode checkPayModeAndReturn(WorkFlowTransactionBean workFlowTransactionBean) {
        boolean isHybridPayEnabled = (workFlowTransactionBean.getMerchantViewConsult() != null) ? isHybridPaymentEnabled(workFlowTransactionBean
                .getMerchantViewConsult()) : isHybridPaymentEnabled(workFlowTransactionBean
                .getMerchantLiteViewConsult());

        double balance = (workFlowTransactionBean.getMerchantViewConsult() != null) ? fetchWalletBalance(workFlowTransactionBean
                .getMerchantViewConsult()) : fetchWalletBalance(workFlowTransactionBean.getMerchantLiteViewConsult());

        if (isHybridPayEnabled && balance > 0) {
            LOGGER.debug("Returning Hybrid as PG Plus Wallet decision");
            return EPayMode.HYBRID;
        }

        LOGGER.debug("Returning None as PG Plus Wallet decision");
        return EPayMode.NONE;
    }

    public EPayMode checkPayModeAndReturn(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean) {
        boolean isHybridPayEnabled = isHybridPaymentEnabled(litePayviewConsultResponseBizBean);

        if (isHybridPayEnabled) {
            LOGGER.debug("Returning Hybrid as PG Plus Wallet decision");
            return EPayMode.HYBRID;
        }

        LOGGER.debug("Returning None as PG Plus Wallet decision");
        return EPayMode.NONE;
    }
}
