package com.paytm.pgplus.biz.workflow.walletconsult.impl;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.fund.models.response.WalletConsultResponse;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.biz.workflow.walletconsult.WalletConsultRequestBuilder;
import com.paytm.pgplus.biz.workflow.walletconsult.WalletConsultService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("walletConsultServiceImpl")
public class WalletConsultServiceImpl implements WalletConsultService {

    public static final Logger LOGGER = LoggerFactory.getLogger(WalletConsultServiceImpl.class);
    @Autowired
    private ITopup topUpServices;

    @Autowired
    @Qualifier("addMoneyAndAddNPayWalletConsultRequestBuilder")
    private WalletConsultRequestBuilder walletConsultRequestBuilder;

    @Override
    public GenericCoreResponseBean<BizWalletConsultResponse> doWalletConsult(
            WorkFlowTransactionBean workFlowTransactionBean) {
        BizWalletConsultResponse bizWalletConsultResponse;
        try {
            ConsultWalletLimitsRequest consultWalletLimitsRequest = walletConsultRequestBuilder
                    .buildWalletConsultRequest(workFlowTransactionBean);
            WalletConsultResponse walletConsultResponse = topUpServices
                    .consultWalletLimitsV2(consultWalletLimitsRequest);
            bizWalletConsultResponse = JsonMapper.convertValue(walletConsultResponse, BizWalletConsultResponse.class);

            if (bizWalletConsultResponse.getStatusCode().equalsIgnoreCase("SUCCESS")
                    && bizWalletConsultResponse.isLimitApplicable())
                return new GenericCoreResponseBean<>("AddMoney Not Allowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
            else if (bizWalletConsultResponse.getStatusCode().equalsIgnoreCase("FAILURE"))
                return new GenericCoreResponseBean<>("AddMoney Not Allowed or Failed",
                        ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);

            return new GenericCoreResponseBean<>(bizWalletConsultResponse);
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            LOGGER.error("Exception Occurred {}", e);
        }
        LOGGER.error("Wallet Consult failed");
        return new GenericCoreResponseBean<>("AddMoney Not Allowed or Failed",
                ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
    }

    @Override
    public GenericCoreResponseBean<WorkFlowTransactionBean> applyFeeIfApplicableForAddNPayTransaction(
            BizWalletConsultResponse bizWalletConsultResponse, WorkFlowTransactionBean workFlowTransactionBean) {
        if (bizWalletConsultResponse.isLimitApplicable()) {
            return new GenericCoreResponseBean<>("AddMoney NotAllowed or Failed",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        } else {
            List<Map<String, Object>> feeDetails = bizWalletConsultResponse.getFeeDetails();
            if (feeDetails != null && !workFlowTransactionBean.isAddMoneyPcfEnabled()) {
                // populateFeeDetails(feeDetails, workFlowResponseBean,
                // workFlowTransactionBean);
                return markTransactionAsFailForCCPayMode(workFlowTransactionBean, feeDetails);
            }
        }
        return new GenericCoreResponseBean<>(workFlowTransactionBean);
    }

    private GenericCoreResponseBean<WorkFlowTransactionBean> markTransactionAsFailForCCPayMode(
            WorkFlowTransactionBean workFlowTransactionBean, List<Map<String, Object>> feeDetails) {
        String payMethod = workFlowTransactionBean.getWorkFlowBean().getPayMethod();
        if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod)) {
            for (Map<String, Object> feeItem : feeDetails) {
                if (feeItem != null && !feeItem.isEmpty()) {
                    PayMethod payMethod1 = PayMethod.getPayMethodByMethod((String) feeItem.get("payMethod"));
                    if (payMethod1.getMethod().equalsIgnoreCase(EPayMethod.CREDIT_CARD.getMethod())
                            && StringUtils.isNotBlank((String) feeItem.get("feePercent"))) {

                        String errorMessage = (String) feeItem.get("rejectMsg");
                        if (StringUtils.isNotBlank(errorMessage)) {
                            LOGGER.info("Falling Transaction as add Money is Not allowed for AddNPay beyond free limit");
                            return new GenericCoreResponseBean<>("Add MoneyNotAllowed",
                                    ResponseConstants.ADD_MONEY_WALLET_LIMIT_BREACH, errorMessage);
                        }
                    }
                }
            }
        }
        return new GenericCoreResponseBean<>(workFlowTransactionBean);
    }

    /*
     * private void populateFeeDetails(List<Map<String, Object>> feeDetails,
     * WorkFlowResponseBean workFlowResponseBean, WorkFlowTransactionBean
     * workFlowTransactionBean) { List<RiskConvenienceFee>
     * riskConvenienceFeeList = new LinkedList<>(); for (Map<String, Object>
     * feeItem : feeDetails) { if (feeItem != null && !feeItem.isEmpty()) {
     * 
     * RiskConvenienceFee riskConvenienceFee = new RiskConvenienceFee();
     * riskConvenienceFee.setPayMethod(PayMethod.getPayMethodByMethod((String)
     * feeItem.get("payMethod"))); riskConvenienceFee.setFeePercent((String)
     * feeItem.get("feePercent")); riskConvenienceFee.setReason((String)
     * feeItem.get("msg")); riskConvenienceFee.setFeeDetails(feeItem);
     * 
     * riskConvenienceFeeList.add(riskConvenienceFee); } } if
     * (CollectionUtils.isNotEmpty(riskConvenienceFeeList)) {
     * workFlowResponseBean.setRiskConvenienceFee(riskConvenienceFeeList);
     * LOGGER.info("Risk Convenience Fee set in response {} ",
     * riskConvenienceFeeList); } return new
     * GenericCoreResponseBean<>("AddMoneyNotAllowed or Failed",
     * ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED); }
     */
}
