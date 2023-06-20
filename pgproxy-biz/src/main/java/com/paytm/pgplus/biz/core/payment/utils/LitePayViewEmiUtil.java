package com.paytm.pgplus.biz.core.payment.utils;

import com.paytm.pgplus.biz.core.model.request.BalanceChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.EMIChannelInfoBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.cache.model.EmiDetailRequest;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IEMIDetails;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service("litePayViewEmiUtil")
public class LitePayViewEmiUtil {

    @Autowired
    @Qualifier("aoaUtil")
    private AOAUtils aoaUtils;

    @Autowired
    private IEMIDetails emiDetailsClient;

    public static final Logger LOGGER = LoggerFactory.getLogger(LitePayViewEmiUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(LitePayViewEmiUtil.class);

    private static final Double MIN_WALLET_BALANCE = 100D;

    public void setEmiInfoForLiteConsultPayview(WorkFlowTransactionBean transBean, String serviceAmout) {
        String serviceAmountForEmiHybrid = null;
        List<PayMethodViewsBiz> payMethodViewsBizList = transBean.getMerchantViewConsult() != null ? transBean
                .getMerchantViewConsult().getPayMethodViews() : transBean.getMerchantLiteViewConsult()
                .getPayMethodViews();
        boolean isAoaMerchant = transBean.getWorkFlowBean().isFromAoaMerchant();
        EMIDetailList emiDetailList = null;
        List<PayChannelOptionViewBiz> tempPayChannelOptionViewBiz = new ArrayList<>();

        for (final PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {
            if (PayMethod.BALANCE.getMethod().equals(payMethodViewsBiz.getPayMethod())
                    && payMethodViewsBiz.getPayChannelOptionViews().get(0).isEnableStatus()) {
                List<BalanceChannelInfoBiz> balanceChannelInfo = payMethodViewsBiz.getPayChannelOptionViews().get(0)
                        .getBalanceChannelInfos();
                if (!CollectionUtils.isEmpty(balanceChannelInfo)) {
                    Double walletBalance = Double.parseDouble(balanceChannelInfo.get(0).getAccountBalance());

                    // For the Hybrid transaction to pure PG transaction
                    if (walletBalance < MIN_WALLET_BALANCE) {
                        walletBalance = 0D;
                    }

                    Double txnAmount = Double.parseDouble(serviceAmout);
                    if (txnAmount > walletBalance) {
                        Double differenceAmount = txnAmount - walletBalance;
                        serviceAmountForEmiHybrid = String.valueOf(differenceAmount);
                    }
                }
            }
        }
        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {
            if (!PayMethod.EMI.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                continue;
            }
            if (isAoaMerchant) {
                String aoaMid = transBean.getWorkFlowBean().getPaytmMID();
                String paytmMerchantMid = aoaUtils.getPgMidForAoaMid(aoaMid);
                EmiDetailRequest emiDetailRequest = new EmiDetailRequest();
                emiDetailRequest.setMid(paytmMerchantMid);
                emiDetailRequest.setStatus(Boolean.TRUE);
                try {
                    emiDetailList = fetchEmiDetailsList(emiDetailRequest);
                    if (null == emiDetailList || CollectionUtils.isEmpty(emiDetailList.getEmiDetails())) {
                        LOGGER.info("Emi not configured on merchant get empty response from mapping-service");
                    }
                } catch (MappingServiceClientException e) {
                    LOGGER.error("Exception occurred while fetching emi details :{} error : {} ", emiDetailRequest, e);
                }
                Map<String, PayChannelOptionViewBiz> payChannelOptionViewBizMap = new HashMap<String, PayChannelOptionViewBiz>();
                for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                    payChannelOptionViewBizMap.put((payChannelOptionViewBiz.getInstId()), payChannelOptionViewBiz);
                }
                for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                    for (final Map.Entry<Long, EMIDetails> emiChannelInfo : emiDetailList.getEmiDetails().entrySet()) {
                        if (emiChannelInfo.getValue().getBankCode().equals(payChannelOptionViewBiz.getInstId())) {
                            if (!tempPayChannelOptionViewBiz.contains(payChannelOptionViewBizMap.get(emiChannelInfo
                                    .getValue().getBankCode())))
                                tempPayChannelOptionViewBiz.add(payChannelOptionViewBizMap.get(emiChannelInfo
                                        .getValue().getBankCode()));
                        }
                    }
                }
                payMethodViewsBiz.setPayChannelOptionViews(tempPayChannelOptionViewBiz);
            }
            for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                if (!PayMethod.EMI.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                    continue;
                }
                if (isAoaMerchant) {
                    payChannelOptionViewBiz.setEmiChannelInfos(getEMIChannelInfoListForAOA(
                            emiDetailList.getEmiDetails(), serviceAmout, payChannelOptionViewBiz.getInstId()));

                } else {
                    payChannelOptionViewBiz.setEmiChannelInfos(getEMIChannelInfoList(
                            payChannelOptionViewBiz.getEmiChannelInfos(), serviceAmout,
                            payChannelOptionViewBiz.getInstId()));
                    if (StringUtils.isNotBlank(serviceAmountForEmiHybrid)) {
                        payChannelOptionViewBiz.setEmiHybridChannelInfos(getEMIChannelInfoList(
                                payChannelOptionViewBiz.getEmiHybridChannelInfos(), serviceAmountForEmiHybrid,
                                payChannelOptionViewBiz.getInstId()));
                    }
                }
            }

        }
    }

    private List<EMIChannelInfoBiz> getEMIChannelInfoList(final List<EMIChannelInfoBiz> emiChannelInfos,
            String serviceAmount, String instId) {

        if ((emiChannelInfos != null) && !emiChannelInfos.isEmpty()) {
            List<EMIChannelInfoBiz> finalEmiChannelInfoBizs = new ArrayList<>();

            for (final EMIChannelInfoBiz emiChannelInfo : emiChannelInfos) {
                final EMIChannelInfoBiz emiChannelInfoBiz = new EMIChannelInfoBiz();

                if (isTransactionEligible(emiChannelInfo, serviceAmount)) {
                    emiChannelInfoBiz.setCardAcquiringMode(emiChannelInfo.getCardAcquiringMode());
                    emiChannelInfoBiz.setInterestRate(emiChannelInfo.getInterestRate());
                    emiChannelInfoBiz.setMaxAmount(emiChannelInfo.getMaxAmount());
                    emiChannelInfoBiz.setMinAmount(emiChannelInfo.getMinAmount());
                    emiChannelInfoBiz.setOfMonths(emiChannelInfo.getOfMonths());
                    emiChannelInfoBiz.setPlanId(emiChannelInfo.getPlanId());
                    emiChannelInfoBiz.setTenureId(emiChannelInfo.getTenureId());

                    BigDecimal emi = calculateEmi(serviceAmount, emiChannelInfoBiz.getInterestRate(),
                            emiChannelInfoBiz.getOfMonths(), instId);
                    emiChannelInfoBiz.setPerInstallment(emi.toPlainString());
                    finalEmiChannelInfoBizs.add(emiChannelInfoBiz);
                }
            }

            Collections.sort(finalEmiChannelInfoBizs, new EmiMonthComparator());
            return finalEmiChannelInfoBizs;
        }
        return Collections.emptyList();
    }

    private boolean isTransactionEligible(EMIChannelInfoBiz emiChannelInfoBiz, String serviceAmount) {
        double txnAmount = Double.parseDouble(serviceAmount);
        double minAmountEMI = Double.parseDouble(emiChannelInfoBiz.getMinAmount());
        double maxAmountEMI = Double.parseDouble(emiChannelInfoBiz.getMaxAmount());

        if ((txnAmount >= minAmountEMI) && (txnAmount <= maxAmountEMI)) {
            return true;
        }

        LOGGER.debug("Emi Plan :: {} not eligible due to amount range", emiChannelInfoBiz);

        return false;
    }

    private BigDecimal calculateEmi(String transactionAmount, String interestRate, String months, String instId) {
        LOGGER.debug("Transaction Amount : {} , Intereset Rate : {} , Months : {}", transactionAmount, interestRate,
                months);
        BigDecimal serviceAmount = new BigDecimal(transactionAmount);
        try {
            BigDecimal rate = new BigDecimal(interestRate).divide(BizConstant.BIG_DECIMAL_1200, 10,
                    RoundingMode.HALF_UP);
            if (rate.doubleValue() <= 0) {
                EXT_LOGGER.customInfo("Returning EMI Amount as trans amount divided by number of months");
                return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
            }

            if (TheiaConstant.ExtraConstants.IDBI.equalsIgnoreCase(instId)) {
                EXT_LOGGER.customInfo("Calculating emi for IDBI bank with instId {}", instId);
                return PaymentHelper.calculateIdBIEmi(transactionAmount, interestRate, months);
            }

            BigDecimal noOfMonths = new BigDecimal(months);
            return serviceAmount
                    .multiply(rate)
                    .multiply(rate.add(BizConstant.BIG_DECIMAL_1).pow(noOfMonths.intValue()))
                    .divide((rate.add(BizConstant.BIG_DECIMAL_1).pow(noOfMonths.intValue()))
                            .subtract(BizConstant.BIG_DECIMAL_1),
                            2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            LOGGER.error("Returning EMI Amount as trans amount divided by number of months");
            return serviceAmount.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        }
    }

    public EMIDetailList fetchEmiDetailsList(EmiDetailRequest emiDetailRequest) throws MappingServiceClientException {
        EMIDetailList emiDetailList = emiDetailsClient.getEMIByMid(emiDetailRequest);
        EXT_LOGGER.customInfo("Mapping response - EMIDetailList :: {}", emiDetailList);
        return emiDetailList;
    }

    private List<EMIChannelInfoBiz> getEMIChannelInfoListForAOA(final Map<Long, EMIDetails> emiChannelInfos,
            String serviceAmount, String instId) {

        if ((emiChannelInfos != null) && !emiChannelInfos.isEmpty()) {
            List<EMIChannelInfoBiz> finalEmiChannelInfoBizsAOA = new ArrayList<>();
            for (final Map.Entry<Long, EMIDetails> emiChannelInfo : emiChannelInfos.entrySet()) {
                if (!PayMethod.EMI.getMethod().equals(emiChannelInfo.getValue().getPayMode())) {
                    continue;
                }
                if (!emiChannelInfo.getValue().getBankCode().equals(instId)) {
                    continue;
                }
                final EMIChannelInfoBiz emiChannelInfoBiz = new EMIChannelInfoBiz();
                if (isTransactionEligibleForAOAEMI(emiChannelInfo, serviceAmount)) {
                    if (emiChannelInfo.getValue().getIsSelfBank()) {
                        emiChannelInfoBiz.setCardAcquiringMode("OFFUS");
                    } else {
                        emiChannelInfoBiz.setCardAcquiringMode("ONUS");
                    }
                    emiChannelInfoBiz.setInterestRate(String.valueOf(emiChannelInfo.getValue().getInterest()));
                    emiChannelInfoBiz.setMaxAmount(String.valueOf(AmountUtils.getAmountInPaise(emiChannelInfo
                            .getValue().getMaxAmount())));
                    emiChannelInfoBiz.setMinAmount(String.valueOf(AmountUtils.getAmountInPaise(emiChannelInfo
                            .getValue().getMinAmount())));
                    emiChannelInfoBiz.setOfMonths(String.valueOf(emiChannelInfo.getValue().getMonth()));
                    emiChannelInfoBiz.setPlanId(emiChannelInfo.getValue().getBankCode() + "|"
                            + emiChannelInfo.getValue().getMonth());

                    BigDecimal emi = calculateEmi(serviceAmount, emiChannelInfoBiz.getInterestRate(),
                            emiChannelInfoBiz.getOfMonths(), instId);
                    emiChannelInfoBiz.setPerInstallment(emi.toPlainString());
                    finalEmiChannelInfoBizsAOA.add(emiChannelInfoBiz);
                }
            }

            Collections.sort(finalEmiChannelInfoBizsAOA, new EmiMonthComparator());
            LOGGER.info("finalEmiChannelInfoBizs is : {}", finalEmiChannelInfoBizsAOA);
            return finalEmiChannelInfoBizsAOA;
        }
        return Collections.emptyList();
    }

    private boolean isTransactionEligibleForAOAEMI(Map.Entry<Long, EMIDetails> emiChannelInfo, String serviceAmount) {
        double txnAmount = Double.parseDouble(serviceAmount);
        double minAmountEMI = AmountUtils.getAmountInPaise(emiChannelInfo.getValue().getMinAmount());
        double maxAmountEMI = AmountUtils.getAmountInPaise(emiChannelInfo.getValue().getMaxAmount());

        if ((txnAmount >= minAmountEMI) && (txnAmount <= maxAmountEMI)) {
            return true;
        }

        LOGGER.info("Emi Plan :: {} not eligible due to amount range", emiChannelInfo);

        return false;
    }
}
