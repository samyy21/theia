package com.paytm.pgplus.theia.nativ.subscription;

import com.google.zxing.WriterException;
import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.subscription.model.response.SubscriptionCheckStatusResponseBody;
import com.paytm.pgplus.subscriptionClient.model.request.FreshSubscriptionRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.QrDetail;

import java.io.IOException;
import java.util.List;

public interface INativeSubscriptionHelper {

    FreshSubscriptionRequest createFreshSubscriptionRequest(SubscriptionTransactionRequest request);

    // AoaSubscriptionCreateRequest
    // createFreshAOASubscriptionRequest(SubscriptionTransactionRequest
    // request);

    boolean subsPPIAmountLimitBreached(String subscriptionPaymentMode, String subscriptionMaxAmount, String mid);

    boolean isSubscriptionNotAuthorized(PaymentRequestBean paymentRequestData);

    void markSubscriptionAuthorized(String txnToken, InitiateTransactionRequestBody orderDetail,
            ValidateLoginOtpResponse validateLoginOtpResponse);

    public boolean invalidUpiGraceDays(String frequencyCycle, String graceDays, String frequency);

    public boolean subsUPIAmountLimitBreached(String subsAmount);

    public boolean invalidUpifrequencyCycle(String frequencyCycle);

    public boolean subsUpiMonthlyFrequencyBreach(String frequencyCycle, String subsFrequency);

    public boolean invalidSubsRetryCount(String subscriptionEnableRetry, String subscriptionRetryCount);

    public boolean invalidUpiSubsFrequency(String subsFrequency);

    boolean invalidUpiSubsStartDate(String subscriptionStartDate, String subscriptionFrequencyUnit);

    SubscriptionCheckStatusResponseBody getSubscriptionStatus(String subsId, String mid, String custId, String orderId);

    public boolean invalidSubsFrequencyUnitForBankMandate(String subscriptionFrequencyUnit,
            String subscriptionPaymentMode);

    public boolean invalidGraceDaysForCard(String graceDays);

    public List<String> getAOASubscriptionPaymodesConfigured();

    public QrDetail fetchQrDetails(String orderId, String mid, String deepLink) throws WriterException, IOException;

    public String fetchDeepLink(String orderId, String mid);

    String calculateSubsStartDate(String subscriptionStartDate, String subscriptionFrequency,
            String subscriptionFrequencyUnit);
}
