package com.paytm.pgplus.biz.workflow.walletconsult.impl;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.biz.workflow.walletconsult.WalletConsultRequestBuilder;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.paytm.pgplus.biz.enums.AddMoneySourceEnum.*;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_WALLET;

@Service("addMoneyAndAddNPayWalletConsultRequestBuilder")
public class AddMoneyAndAddNPayWalletConsultRequestBuilderImpl implements WalletConsultRequestBuilder {

    @Autowired
    private Ff4jUtils ff4jUtils;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AddMoneyAndAddNPayWalletConsultRequestBuilderImpl.class);

    @Override
    public ConsultWalletLimitsRequest buildWalletConsultRequest(WorkFlowTransactionBean workFlowTransactionBean) {
        try {

            Double addMoneyAmount;
            String userId = null;
            if (workFlowTransactionBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY) {
                String walletAmount = workFlowTransactionBean.getWorkFlowBean().getWalletAmount();
                Double walletAmountToDeduct = StringUtils.isNotBlank(walletAmount) ? Double.valueOf(walletAmount) : 0;
                addMoneyAmount = Double.valueOf(workFlowTransactionBean.getWorkFlowBean().getTxnAmount())
                        - walletAmountToDeduct;
                if (BooleanUtils.isTrue(workFlowTransactionBean.getWorkFlowBean().getAddOneRupee())
                        && addMoneyAmount < 100D) {
                    addMoneyAmount = 100D;
                }
            } else {
                addMoneyAmount = Double.valueOf(workFlowTransactionBean.getWorkFlowBean().getTxnAmount());
            }
            if (workFlowTransactionBean.getUserDetails() != null) {
                userId = workFlowTransactionBean.getUserDetails().getUserId();
            }
            final ConsultWalletLimitsRequest walletConsultRequest = new ConsultWalletLimitsRequest(addMoneyAmount,
                    userId, workFlowTransactionBean.getWorkFlowBean().getOrderID());
            walletConsultRequest.setTargetPhoneNo(workFlowTransactionBean.getWorkFlowBean().getTargetPhoneNo());
            walletConsultRequest.setGvFlag(workFlowTransactionBean.getWorkFlowBean().isGvFlag());
            walletConsultRequest.setTransitWallet(workFlowTransactionBean.getWorkFlowBean().isTransitWallet());
            walletConsultRequest.setTotalTxnAmount(Double.valueOf(workFlowTransactionBean.getWorkFlowBean()
                    .getTxnAmount()));
            walletConsultRequest
                    .setAddAndPay(workFlowTransactionBean.getWorkFlowBean().getPaytmExpressAddOrHybrid() == EPayMode.ADDANDPAY ? true
                            : false);
            walletConsultRequest.setCardHash(workFlowTransactionBean.getWorkFlowBean().getCardHash());
            if (StringUtils.equals(EPayMethod.NET_BANKING.getMethod(), workFlowTransactionBean.getWorkFlowBean()
                    .getPayMethod())) {
                walletConsultRequest.setCardIndexNo(workFlowTransactionBean.getWorkFlowBean().getBankCode());
            } else {
                String payMethod = workFlowTransactionBean.getWorkFlowBean().getPayMethod();
                if ((EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)
                        || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod)
                        || EPayMethod.EMI.getMethod().equals(payMethod) || EPayMethod.EMI_DC.getMethod().equals(
                        payMethod))
                        && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_GCIN_ON_COFT_WALLET,
                                workFlowTransactionBean.getWorkFlowBean().getPaytmMID(), null, userId, false)) {
                    walletConsultRequest.setCardIndexNo(workFlowTransactionBean.getWorkFlowBean().getGcin());
                } else {
                    walletConsultRequest.setCardIndexNo(workFlowTransactionBean.getWorkFlowBean().getCardIndexNo());
                }
            }
            walletConsultRequest.setPaymentMode(workFlowTransactionBean.getWorkFlowBean().getPayMethod());
            walletConsultRequest.setSource(THIRD_PARTY.getValue());
            walletConsultRequest.setBankId(workFlowTransactionBean.getWorkFlowBean().getBankName());
            if (workFlowTransactionBean.getWorkFlowBean().getBinDetail() != null) {
                walletConsultRequest.setCorporateCard(workFlowTransactionBean.getWorkFlowBean().getBinDetail()
                        .isCorporateCard());
                walletConsultRequest.setPrepaidCard(workFlowTransactionBean.getWorkFlowBean().getBinDetail()
                        .isPrepaidCard());
                walletConsultRequest
                        .setBinNumber(workFlowTransactionBean.getWorkFlowBean().getBinDetail().getBin() != null ? String
                                .valueOf(workFlowTransactionBean.getWorkFlowBean().getBinDetail().getBin()) : null);
            }
            LOGGER.debug("Created request for consult wallet limit as ::{}", walletConsultRequest);
            return walletConsultRequest;
        } catch (Exception e) {
            LOGGER.error("Error While creating walletConsultRequest");
        }
        return null;
    }

    private String getSourceForWalletConsult(WorkFlowRequestBean workFlowRequestBean) {
        String addMoneySource = THIRD_PARTY.getValue();
        return addMoneySource;
    }

}