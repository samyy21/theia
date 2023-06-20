/**
 *
 */
package com.paytm.pgplus.cashier.facade.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.common.model.EnvInfo.EnvInfoBuilder;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.PayOptionBill;
import com.paytm.pgplus.facade.payment.models.PayOptionBill.PayOptionBillBuilder;
import com.paytm.pgplus.facade.payment.models.request.PayRequest;
import com.paytm.pgplus.facade.payment.models.request.PayRequestBody;
import com.paytm.pgplus.facade.payment.models.request.PayRequestBody.PayRequestBodyBuilder;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import org.apache.commons.lang3.StringUtils;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.BasicPayOption.UPI;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.VIRTUAL_PAYMENT_ADDRESS;

/**
 * @author amit.dubey
 *
 */
public class CashierFacadeMapper {

    public static PayRequest buildPayRequest(PaymentRequest paymentRequest,
            List<CashierPayOptionBill> cashierPayOptionBills) throws FacadeCheckedException {
        AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CASHIER_PAY);

        CashierEnvInfo cashierEnvInfo = paymentRequest.getCashierEnvInfo();

        EnvInfo envInfo = new EnvInfoBuilder(cashierEnvInfo.getClientIp(), cashierEnvInfo.getTerminalType()
                .getTerminal()).clientKey(cashierEnvInfo.getClientKey()).tokenId(cashierEnvInfo.getTokenId())
                .sessionId(cashierEnvInfo.getSessionId()).sdkVersion(cashierEnvInfo.getSdkVersion())
                .appVersion(cashierEnvInfo.getAppVersion()).websiteLanguage(cashierEnvInfo.getWebsiteLanguage())
                .osType(cashierEnvInfo.getOsType()).orderOsType(cashierEnvInfo.getOrderOsType())
                .orderTerminalType(cashierEnvInfo.getOrderTerminalType())
                .merchantAppVersion(cashierEnvInfo.getAppVersion()).extendInfo(cashierEnvInfo.getExtentInfo()).build();

        List<PayOptionBill> payOptionBills = new ArrayList<>();
        for (CashierPayOptionBill cpo : cashierPayOptionBills) {
            PayOptionBillBuilder builder = new PayOptionBillBuilder();

            builder.cardCacheToken(cpo.getCardCacheToken())
                    .channelInfo(cpo.getChannelInfo())
                    .chargeAmount(new Money(cpo.getChargeAmount().getCurrencyType(), cpo.getChargeAmount().getAmount()))
                    .channelInfo(cpo.getChannelInfo()).extendInfo(cpo.getExtendInfo()).payMethod(cpo.getPayMethod())
                    .payOption(cpo.getPayOption()).saveChannelInfoAfterPay(cpo.getSaveChannelInfoAfterPay())
                    .topupAndPay(cpo.getTopupAndPay()).issuingCountry(cpo.getIssuingCountry())
                    .transAmount(new Money(cpo.getTransAmount().getCurrencyType(), cpo.getTransAmount().getAmount()));

            if (PayMethod.UPI.equals(cpo.getPayMethod()) && UPI.equals(cpo.getPayOption())) {
                String vpaAddr = paymentRequest.getExtendInfo().get(VIRTUAL_PAYMENT_ADDRESS);
                if (StringUtils.isBlank(vpaAddr)) {
                    vpaAddr = cpo.getChannelInfo().get(VIRTUAL_PAYMENT_ADDRESS);
                }
                builder.virtualPaymentAddr(vpaAddr);
            }

            if (cpo.getPayMethod().equals(PayMethod.BALANCE)) {
                builder.payerAccountNo(cpo.getPayerAccountNo());
                Map<String, String> extendInfo = new HashMap<>(cpo.getExtendInfo());
                extendInfo.remove("issuingBankName");
                extendInfo.remove("issuingBankId");
                builder.extendInfo(extendInfo);
            }
            payOptionBills.add(builder.build());
        }

        PayRequestBody body = new PayRequestBodyBuilder(paymentRequest.getTransId(), paymentRequest.getTransType(),
                paymentRequest.getRequestId(), payOptionBills, envInfo).payerUserId(paymentRequest.getPayerUserId())
                .securityId(paymentRequest.getSecurityId()).extendInfo(paymentRequest.getExtendInfo())
                .riskExtendInfo(paymentRequest.getRiskExtendInfo())
                .paymentScenario(paymentRequest.getPaymentScenario())
                .paytmMerchantId(paymentRequest.getExtendInfo().get(PAYTM_MERCHANT_ID)).build();

        return new PayRequest(head, body);
    }
}
