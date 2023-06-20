package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;

/**
 * Created by chitrasinghal on 12/4/18.
 */
public interface ISusbcriptionNativeValidationService {

    void validate(SubscriptionTransactionRequest request);

    void validateAOASubscriptionRequest(SubscriptionTransactionRequest request);

    void validateSubsStartDate(SubscriptionTransactionRequest request);

    void validateSubsMinAmount(SubscriptionTransactionRequest request);

    void validateSubsMaxAmount(SubscriptionTransactionRequest request);

    boolean isTxnAmountGreaterThanMaxOrRenewalAmt(SubscriptionTransactionRequestBody requestBody);

    boolean isTxnAmountLessThanMaxOrRenewalAmt(SubscriptionTransactionRequestBody requestBody);

    boolean isDailySubscription(SubscriptionTransactionRequest request);

    boolean isDailySubscription(SubscriptionTransactionRequestBody request);

    boolean checkIfTxnAmountLessThanMinAmt(SubscriptionTransactionRequestBody requestBody);
}
