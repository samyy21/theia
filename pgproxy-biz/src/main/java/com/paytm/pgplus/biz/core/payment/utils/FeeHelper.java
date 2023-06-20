/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.payment.utils;

import com.paytm.pgplus.biz.core.model.request.ConsultFeeRequest;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.boss.models.ConsultCondition;
import com.paytm.pgplus.facade.boss.models.ConsultDetail;
import com.paytm.pgplus.facade.boss.models.ConsultResult;
import com.paytm.pgplus.facade.boss.models.PayMethodDetail;
import com.paytm.pgplus.facade.boss.models.request.BulkChargeFeeConsultRequest;
import com.paytm.pgplus.facade.boss.models.request.BulkChargeFeeConsultRequestBody;
import com.paytm.pgplus.facade.boss.models.request.ChargeFeeConsultRequest;
import com.paytm.pgplus.facade.boss.models.request.ChargeFeeConsultRequestBody;
import com.paytm.pgplus.facade.boss.models.response.BulkChargeFeeConsultResponse;
import com.paytm.pgplus.facade.boss.models.response.ChargeFeeConsultResponse;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.EnumCurrency;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.PaymentAdapterUtil;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.collections.CollectionUtils;
import com.paytm.pgplus.biz.utils.BizConstant;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.facade.enums.ProductCodes.StandardDirectPayDynamicTargetProd;

public class FeeHelper {

    private static ChargeFeeConsultRequest getFeeConsultRequest(final String merchantId,
            final ProductCodes productCodes, final BigDecimal amount, final EPayMethod payMethod, final Routes route)
            throws Exception {
        final List<PayMethodDetail> list = new ArrayList<PayMethodDetail>();
        Money transactionAmount = new Money(EnumCurrency.INR, amount.toPlainString());
        list.add(new PayMethodDetail(PayMethod.getPayMethodByMethod(payMethod.getMethod()), transactionAmount));
        final ChargeFeeConsultRequestBody body = new ChargeFeeConsultRequestBody(merchantId, productCodes, new Money(
                EnumCurrency.INR, amount.toString()), list);
        body.setRoute(route.toString());
        return new ChargeFeeConsultRequest(body, RequestHeaderGenerator.getHeader(ApiFunctions.CHARGE_FEE_CONSULT));
    }

    @Deprecated
    public static void checkAndMapResponse(final Map<EPayMethod, ConsultDetails> responseMap,
            final EPayMethod payMethod, final ChargeFeeConsultResponse chargeFeeConsultResponse,
            BigDecimal transactionAmount) {
        if ("SUCCESS".equals(chargeFeeConsultResponse.getBody().getResultInfo().getResultCode())) {
            responseMap.put(payMethod, getConsultDetails(chargeFeeConsultResponse, payMethod, transactionAmount));
        }
    }

    @Deprecated
    private static ConsultDetails getConsultDetails(final ChargeFeeConsultResponse chargeFeeConsultResponse,
            final EPayMethod payMethod, BigDecimal transactionAmount) {
        final ConsultDetails consultDetails = new ConsultDetails();
        final List<ConsultDetail> consultDetailList = chargeFeeConsultResponse.getBody().getConsultDetailsList();
        consultDetails.setPayMethod(payMethod);
        consultDetails.setBaseTransactionAmount(new BigDecimal(AmountUtils
                .getTransactionAmountInRupee(transactionAmount.toPlainString())));

        consultDetails.setFeeAmount(getBaseFessAmount(consultDetailList));
        consultDetails.setTaxAmount(getTaxAmount(consultDetailList));
        consultDetails.setTotalConvenienceCharges(consultDetails.getFeeAmount().add(consultDetails.getTaxAmount()));
        consultDetails.setTotalTransactionAmount(consultDetails.getBaseTransactionAmount()
                .add(consultDetails.getFeeAmount()).add(consultDetails.getTaxAmount()));
        StringBuilder text = new StringBuilder().append("Rs. ").append(consultDetails.getFeeAmount().toPlainString())
                .append(" + GST as applicable.");
        consultDetails.setText(text.toString());
        return consultDetails;
    }

    public static void checkAndMapResponse(final Map<EPayMethod, ConsultDetails> responseMap,
            final BulkChargeFeeConsultResponse chargeFeeConsultResponse, BigDecimal transactionAmount) {

        List<ConsultResult> consultResults = chargeFeeConsultResponse.getBody().getConsultResults();

        for (ConsultResult consultResult : consultResults) {

            ConsultDetails consultDetailsBiz;
            ConsultCondition consultCondition = consultResult.getConsultCondition();
            List<ConsultDetail> consultDetailList = consultResult.getConsultDetails();

            /*
             * Check for Hybrid
             */
            if (consultCondition.getPayMethodDetails().size() > 1) {
                consultDetailsBiz = getConsultDetails(consultDetailList, EPayMethod.HYBRID_PAYMENT, transactionAmount);
                responseMap.put(EPayMethod.HYBRID_PAYMENT, consultDetailsBiz);
            } else {
                EPayMethod payMethod = EPayMethod.getPayMethodByMethod(consultCondition.getPayMethodDetails().get(0)
                        .getPayMethod().getMethod());
                if (EPayMethod.PPBL.getOldName().equals(consultCondition.getPayMethodDetails().get(0).getInstId())) {
                    payMethod = EPayMethod.NET_BANKING.PPBL;
                } else if (null != consultCondition.getPayMethodDetails().get(0).getFeeRateFactors()) {
                    Map<String, String> feeRateFactors = consultCondition.getPayMethodDetails().get(0)
                            .getFeeRateFactors();
                    String instId = feeRateFactors.get(BizConstant.INST_ID);
                    if (StringUtils.isNotBlank(instId) && EPayMethod.PPBL.getOldName().equals(instId)) {
                        payMethod = EPayMethod.NET_BANKING.PPBL;
                    }

                }

                consultDetailsBiz = getConsultDetails(consultDetailList, payMethod, transactionAmount);
                if (payMethod != null) {
                    consultDetailsBiz.setDisplayText(payMethod.getNewDisplayName());
                }
                responseMap.put(payMethod, consultDetailsBiz);
            }
        }
    }

    private static ConsultDetails getConsultDetails(final List<ConsultDetail> consultDetailList,
            final EPayMethod payMethod, BigDecimal transactionAmount) {
        final ConsultDetails consultDetails = new ConsultDetails();
        consultDetails.setPayMethod(payMethod);
        consultDetails.setBaseTransactionAmount(new BigDecimal(AmountUtils
                .getTransactionAmountInRupee(transactionAmount.toPlainString())));
        consultDetails.setFeeAmount(getBaseFessAmount(consultDetailList));
        consultDetails.setTaxAmount(getTaxAmount(consultDetailList));
        consultDetails.setTotalConvenienceCharges(consultDetails.getFeeAmount().add(consultDetails.getTaxAmount()));
        consultDetails.setTotalTransactionAmount(consultDetails.getBaseTransactionAmount()
                .add(consultDetails.getFeeAmount()).add(consultDetails.getTaxAmount()));
        StringBuilder text = new StringBuilder().append("Convenience fee of Rs. ")
                .append(consultDetails.getTotalConvenienceCharges().toPlainString()).append(" is applicable.");
        consultDetails.setText(text.toString());
        return consultDetails;
    }

    public static ChargeFeeConsultRequest getFeeConsultRequest(ConsultFeeRequest consultFeeRequest, EPayMethod payMethod)
            throws Exception {
        if (EPayMethod.HYBRID_PAYMENT.equals(payMethod)) {
            return getFeeConsultRequestForHybrid(consultFeeRequest);
        }
        return getFeeConsultRequest(consultFeeRequest.getMerchantId(), getproductCode(consultFeeRequest),
                consultFeeRequest.getTransactionAmount(), payMethod, consultFeeRequest.getRoute());
    }

    public static ChargeFeeConsultRequest getFeeConsultRequestForHybrid(ConsultFeeRequest consultFeeRequest)
            throws Exception {
        final List<PayMethodDetail> list = new ArrayList<PayMethodDetail>();
        ChargeFeeConsultRequest request = null;
        Money walletBalanceMoney = new Money(EnumCurrency.INR, consultFeeRequest.getWalletBalance().toPlainString());
        Money differenceAmountMoney = new Money(EnumCurrency.INR, consultFeeRequest.getTransactionAmount()
                .subtract(consultFeeRequest.getWalletBalance()).toPlainString());
        list.add(new PayMethodDetail(PayMethod.getPayMethodByMethod(EPayMethod.BALANCE.getMethod()), walletBalanceMoney));
        for (EPayMethod payMethods : consultFeeRequest.getPayMethods()) {
            if (!EPayMethod.HYBRID_PAYMENT.equals(payMethods) && !EPayMethod.BALANCE.equals(payMethods)) {
                list.add(new PayMethodDetail(PayMethod.getPayMethodByMethod(payMethods.getMethod()),
                        differenceAmountMoney));
                final ChargeFeeConsultRequestBody body = new ChargeFeeConsultRequestBody(
                        consultFeeRequest.getMerchantId(), getproductCode(consultFeeRequest), new Money(
                                EnumCurrency.INR, consultFeeRequest.getTransactionAmount().toString()), list);
                request = new ChargeFeeConsultRequest(body,
                        RequestHeaderGenerator.getHeader(ApiFunctions.CHARGE_FEE_CONSULT));
                break;
            }
        }
        return request;
    }

    public static BulkChargeFeeConsultRequest getBulkFeeConsultRequest(ConsultFeeRequest consultFeeRequest)
            throws FacadeCheckedException {

        List<ConsultCondition> consultConditions = getConsultConditions(consultFeeRequest);
        Money serviceMoney = new Money(EnumCurrency.INR, consultFeeRequest.getTransactionAmount().toString());
        String route = Objects.nonNull(consultFeeRequest.getRoute()) ? consultFeeRequest.getRoute().toString() : "";
        BulkChargeFeeConsultRequestBody body = new BulkChargeFeeConsultRequestBody(consultFeeRequest.getMerchantId(),
                getproductCode(consultFeeRequest), serviceMoney, consultConditions,
                consultFeeRequest.getTransCreatedTime(), route);
        if (StringUtils.isNotEmpty(consultFeeRequest.getAddNPayProductCode())) {
            body.setProductCode(consultFeeRequest.getAddNPayProductCode());
        }
        return new BulkChargeFeeConsultRequest(body,
                RequestHeaderGenerator.getHeader(ApiFunctions.CHARGE_FEE_BULK_CONSULT));
    }

    private static ProductCodes getproductCode(ConsultFeeRequest consultFeeRequest) {
        if (consultFeeRequest.isSlabBasedMDR()) {
            return ProductCodes.StandardAcquiringProdByAmountChargePayer;
        } else if (consultFeeRequest.isDynamicFeeMerchant()) {
            if (consultFeeRequest.getProductCode() != null) {
                return consultFeeRequest.getProductCode();
            }
            return StandardDirectPayDynamicTargetProd;
            // return ProductCodes.StandardDirectPayDynamicTargetProd;
        } else if (ERequestType.DYNAMIC_QR_2FA.equals(consultFeeRequest.getTransactionType())) {
            return ProductCodes.ScanNPayChargePayer;
        }
        return ProductCodes.StandardDirectPayAcquiringProdChargePayer;
    }

    public static List<String> getProductCodesForDynamicChargeSupport() {
        List<String> productCodeSupported = new ArrayList<>();
        String productCodeList = ConfigurationUtil.getProperty("dynamic.charge.target.product.codes", "");
        String[] productCodes = productCodeList.split(Pattern.quote(","));
        for (String productCode : productCodes) {
            productCodeSupported.add(productCode);
        }

        return productCodeSupported;
    }

    private static List<ConsultCondition> getConsultConditions(ConsultFeeRequest consultFeeRequest)
            throws FacadeInvalidParameterException {

        List<ConsultCondition> consultConditions = new ArrayList<>();
        Money totalTransactionAmount = new Money(EnumCurrency.INR, consultFeeRequest.getTransactionAmount()
                .toPlainString());
        if (consultFeeRequest.isAddMoneyPcfEnabled()) {
            return getConsultConditionsForAddMoney(consultConditions, consultFeeRequest);
        }
        int i = 0;
        for (EPayMethod payMethod : consultFeeRequest.getPayMethods()) {

            final List<PayMethodDetail> list = new ArrayList<PayMethodDetail>();

            /*
             * Adding Balance and other PayMethod for Hybrid
             */
            if (EPayMethod.HYBRID_PAYMENT.equals(payMethod)) {

                BigDecimal walletBalance = consultFeeRequest.getWalletBalance();
                BigDecimal differenceAmount = consultFeeRequest.getTransactionAmount().subtract(walletBalance);
                Money walletBalanceMoney = new Money(EnumCurrency.INR, walletBalance.toPlainString());
                Money differenceAmountMoney = new Money(EnumCurrency.INR, differenceAmount.toPlainString());

                PayMethodDetail payMethodDetail = new PayMethodDetail(PayMethod.getPayMethodByMethod(EPayMethod.BALANCE
                        .getMethod()), walletBalanceMoney);
                for (EPayMethod payMethodForHybrid : consultFeeRequest.getPayMethods()) {
                    if (!EPayMethod.HYBRID_PAYMENT.equals(payMethodForHybrid)
                            && !EPayMethod.BALANCE.equals(payMethodForHybrid)) {
                        final List<PayMethodDetail> hybridList = new ArrayList<PayMethodDetail>();
                        hybridList.add(payMethodDetail);
                        hybridList.add(new PayMethodDetail(PayMethod.getPayMethodByMethod(payMethodForHybrid
                                .getMethod()), differenceAmountMoney));
                        consultConditions.add(new ConsultCondition(hybridList));
                    }
                }

                continue;

            }

            /*
             * For Other than hybrid PayMethods
             */
            PayMethodDetail payMethodDetail = new PayMethodDetail(
                    PayMethod.getPayMethodByMethod(payMethod.getMethod()), totalTransactionAmount);
            if (consultFeeRequest.getInstId() != null && !(consultFeeRequest.getInstId().isEmpty())) {
                payMethodDetail.setInstId(consultFeeRequest.getInstId().get(i));
                if (CollectionUtils.isNotEmpty(consultFeeRequest.getFeeRateFactors())
                        && i < consultFeeRequest.getFeeRateFactors().size()
                        && consultFeeRequest.getFeeRateFactors().get(i) != null) {
                    payMethodDetail.setFeeRateFactors(consultFeeRequest.getFeeRateFactors().get(i)
                            .getFeeRateFactorsMap());
                }
                i++;
            }
            list.add(payMethodDetail);
            consultConditions.add(new ConsultCondition(list));

        }

        return consultConditions;
    }

    private static BigDecimal getBaseFessAmount(List<ConsultDetail> consultDetailList) {
        BigDecimal feeAmount = BigDecimal.ZERO;
        if (consultDetailList != null && consultDetailList.size() > 0) {
            for (ConsultDetail consultDetail : consultDetailList) {
                feeAmount = feeAmount.add(new BigDecimal(AmountUtils.getTransactionAmountInRupee(consultDetail
                        .getBaseFeeAmount().getAmount())));
            }
        }
        return feeAmount;
    }

    private static BigDecimal getTaxAmount(List<ConsultDetail> consultDetailList) {
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (consultDetailList != null && consultDetailList.size() > 0) {
            for (ConsultDetail consultDetail : consultDetailList) {
                taxAmount = taxAmount.add(new BigDecimal(AmountUtils.getTransactionAmountInRupee(consultDetail
                        .getTaxAmount().getAmount())));
            }
        }
        return taxAmount;
    }

    private static List<ConsultCondition> getConsultConditionsForAddMoney(List<ConsultCondition> consultConditions,
            ConsultFeeRequest consultFeeRequest) throws FacadeInvalidParameterException {
        final List<PayMethodDetail> list = new ArrayList<PayMethodDetail>();
        ConsultCondition consultCondition = new ConsultCondition();
        for (EPayMethod payMethod : consultFeeRequest.getPayMethods()) {
            PayMethodDetail payMethodDetail;
            if (StringUtils.equals(payMethod.getMethod(), EPayMethod.BALANCE.getMethod())
                    && !Objects.equals(consultFeeRequest.getTransactionAmount(), consultFeeRequest.getAddMoneyAmount())) {
                BigDecimal walletBalance = consultFeeRequest.getTransactionAmount().subtract(
                        consultFeeRequest.getAddMoneyAmount());
                Money walletBalanceMoney = new Money(EnumCurrency.INR, walletBalance.toPlainString());
                payMethodDetail = new PayMethodDetail(PayMethod.getPayMethodByMethod(EPayMethod.BALANCE.getMethod()),
                        walletBalanceMoney);
                list.add(payMethodDetail);
            } else if (StringUtils.equals(payMethod.getMethod(), EPayMethod.BALANCE.getMethod())) {
                continue;
            } else {
                if (TheiaConstant.RequestTypes.ADD_MONEY.equals(consultFeeRequest.getTxnType())) {
                    Money totalTransactionAmount = new Money(EnumCurrency.INR, consultFeeRequest.getTransactionAmount()
                            .toPlainString());
                    payMethodDetail = new PayMethodDetail(PayMethod.getPayMethodByMethod(payMethod.getMethod()),
                            totalTransactionAmount);
                    consultFeeRequest.setAddNPayProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer
                            .getId());
                    consultFeeRequest.setMerchantId(ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID));
                } else {
                    Money addMoneyAmount = new Money(EnumCurrency.INR, consultFeeRequest.getAddMoneyAmount()
                            .toPlainString());
                    payMethodDetail = new PayMethodDetail(PayMethod.getPayMethodByMethod(payMethod.getMethod()),
                            addMoneyAmount);
                    Map<String, String> extendInfo = new HashMap<>();
                    extendInfo.put(BizConstant.ADD_MONEY_SURCHARGE, BizConstant.ADD_MONEY_SURCHARGE_FLAG_TRUE);
                    consultCondition.setExtendInfo(extendInfo);
                }
                if (CollectionUtils.isNotEmpty(consultFeeRequest.getInstId())) {
                    if (CollectionUtils.isNotEmpty(consultFeeRequest.getFeeRateFactors())) {
                        for (FeeRateFactors feeRateFactors : consultFeeRequest.getFeeRateFactors()) {
                            if (feeRateFactors != null && StringUtils.isNotBlank(feeRateFactors.getInstId())) {
                                payMethodDetail.setFeeRateFactors(feeRateFactors.getFeeRateFactorsMap());
                            }
                        }
                    }
                }
                if (MapUtils.isEmpty(payMethodDetail.getFeeRateFactors())) {
                    Map<String, String> feeRateFactors = new HashMap<>();
                    feeRateFactors.put(BizConstant.FEE_RATE_CODE, consultFeeRequest.getFeeRateCode());
                    payMethodDetail.setFeeRateFactors(feeRateFactors);
                } else {
                    Map<String, String> feeRateFactors = payMethodDetail.getFeeRateFactors();
                    feeRateFactors.put(BizConstant.FEE_RATE_CODE, consultFeeRequest.getFeeRateCode());
                }
                list.add(payMethodDetail);
            }
        }
        consultCondition.setPayMethodDetails(list);
        consultConditions.add(consultCondition);
        return consultConditions;
    }
}