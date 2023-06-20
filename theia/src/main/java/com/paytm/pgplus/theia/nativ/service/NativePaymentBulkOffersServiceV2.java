package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.BulkApplyPromoServiceResponseV2;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentOffersBulkApplyTaskV2;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.DISABLE_PARALLEL_EXEC_BULK_APPLY;

@Service("nativePaymentBulkOffersServiceV2")
public class NativePaymentBulkOffersServiceV2 {

    @Autowired
    @Qualifier("paymentOffersServiceHelperV2")
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePaymentBulkOffersServiceV2.class);

    private static ExecutorService taskExecutor = Executors.newFixedThreadPool(2);

    public void processBulkPaymentOffers(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse response, WorkFlowResponseBean workFlowResponse) {
        try {
            if (ff4jUtils.isFeatureEnabled(DISABLE_PARALLEL_EXEC_BULK_APPLY, false)) {
                LOGGER.info("Sequentially executing bulkApply v2");
                NativePaymentOffersBulkApplyTaskV2 bulkApply = new NativePaymentOffersBulkApplyTaskV2();
                bulkApply.setNativeCashierInfoRequest(nativeCashierInfoRequest);
                bulkApply.setCashierInfoResponse(response);
                bulkApply.setOfferOnTotalAmount(false);
                bulkApply.setWorkFlowResponseBean(workFlowResponse);
                bulkApply.setPaymentOffersServiceHelper(paymentOffersServiceHelperV2);
                bulkApply.call();
            } else {
                List<Callable<BulkApplyPromoServiceResponseV2>> tasks = new LinkedList<>();

                EPayMode paymentFlow = response.getBody().getPaymentFlow();
                boolean offerOnTotalAmount = false;

                NativePaymentOffersBulkApplyTaskV2 bulkApplyTask1 = new NativePaymentOffersBulkApplyTaskV2();
                bulkApplyTask1.setNativeCashierInfoRequest(nativeCashierInfoRequest);
                bulkApplyTask1.setCashierInfoResponse(response);
                bulkApplyTask1.setOfferOnTotalAmount(false);
                bulkApplyTask1.setWorkFlowResponseBean(workFlowResponse);
                bulkApplyTask1.setPaymentOffersServiceHelper(paymentOffersServiceHelperV2);

                LOGGER.info("Added bulkApplyTask1");
                tasks.add(bulkApplyTask1);

                if (paymentFlow == EPayMode.HYBRID && isOrderAmountAllowedForHybrid(nativeCashierInfoRequest, response)) {
                    LOGGER.info("hybrid | will be sending offerOnTotalAmount also | payment offers");
                    offerOnTotalAmount = true;
                }

                String flag = ConfigurationUtil.getProperty("allowedCallPromoServiceNonHybridTxnAmnt", "false");
                LOGGER.info("allowedCallPromoServiceNonHybridTxnAmnt={}", flag);
                boolean allowedCallPromoServiceNonHybridTxnAmnt = BooleanUtils.toBoolean(flag);

                NativePaymentOffersBulkApplyTaskV2 bulkApplyTask2 = null;
                if (allowedCallPromoServiceNonHybridTxnAmnt && offerOnTotalAmount) {
                    bulkApplyTask2 = new NativePaymentOffersBulkApplyTaskV2();
                    bulkApplyTask2.setNativeCashierInfoRequest(nativeCashierInfoRequest);
                    bulkApplyTask2.setCashierInfoResponse(response);
                    bulkApplyTask2.setOfferOnTotalAmount(true);
                    bulkApplyTask2.setWorkFlowResponseBean(workFlowResponse);
                    bulkApplyTask2.setPaymentOffersServiceHelper(paymentOffersServiceHelperV2);

                    LOGGER.info("Added bulkApplyTask2");
                    tasks.add(bulkApplyTask2);
                }

                taskExecutor.invokeAll(tasks);
            }
        } catch (Exception ie) {
            LOGGER.error("Error in calling bulkPromo parallel");
        }

        return;
    }

    private boolean isOrderAmountAllowedForHybrid(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        long orderAmount = Long.parseLong(AmountUtils.getTransactionAmountInPaise(nativeCashierInfoRequest.getBody()
                .getOrderAmount()));
        long walletBalance = Long.parseLong(AmountUtils.getTransactionAmountInPaise(paymentOffersServiceHelperV2
                .getWalletBalance(nativeCashierInfoResponse)));
        if (orderAmount > walletBalance) {
            LOGGER.info("Valid hybrid case applying payment offer on total orderAmount");
            return true;
        }
        return false;
    }

}
