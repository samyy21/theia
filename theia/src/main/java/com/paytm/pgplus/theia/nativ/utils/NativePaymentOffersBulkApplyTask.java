package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.paymentpromotion.models.request.BulkApplyPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.response.ApplyPromoResponseData;
import com.paytm.pgplus.facade.paymentpromotion.models.response.BulkApplyPromoServiceResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.models.PaymentOfferDetails;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedCard;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedVPA;
import com.paytm.pgplus.theia.nativ.service.NativePaymentBulkOffersService;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class NativePaymentOffersBulkApplyTask implements Callable<BulkApplyPromoServiceResponse> {

    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePaymentBulkOffersService.class);

    private BulkApplyPromoServiceRequest bulkApplyPromoServiceRequest;
    private String mid;
    private String custId;
    private String orderId;

    private NativeCashierInfoRequest nativeCashierInfoRequest;
    private NativeCashierInfoResponse cashierInfoResponse;
    private WorkFlowResponseBean workFlowResponseBean;
    private boolean offerOnTotalAmount;
    private String paytmUserId;

    public NativePaymentOffersBulkApplyTask() {
    }

    @Override
    public BulkApplyPromoServiceResponse call() throws Exception {

        this.bulkApplyPromoServiceRequest = paymentOffersServiceHelper.prepareApplyPromoServiceRequest(
                nativeCashierInfoRequest, cashierInfoResponse, workFlowResponseBean, offerOnTotalAmount);

        this.custId = nativeCashierInfoRequest.getBody().getCustId();

        if (StringUtils.isBlank(this.custId)) {
            this.custId = workFlowResponseBean.getUserDetails() != null ? workFlowResponseBean.getUserDetails()
                    .getUserId() : null;
        }
        this.mid = nativeCashierInfoRequest.getBody().getMid();
        this.paytmUserId = workFlowResponseBean.getUserDetails() != null ? workFlowResponseBean.getUserDetails()
                .getUserId() : null;

        if (bulkApplyPromoServiceRequest != null) {
            BulkApplyPromoServiceResponse bulkApplyPromoServiceRes = paymentOffersServiceHelper.bulkApplyPromo(
                    bulkApplyPromoServiceRequest, mid, custId, orderId, paytmUserId);

            if (paymentOffersServiceHelper.isPromoServiceSeccessResponse(bulkApplyPromoServiceRes)
                    && CollectionUtils.isNotEmpty(bulkApplyPromoServiceRes.getBulkData())) {
                populateBulkApplyPromoServiceResponse(bulkApplyPromoServiceRes, this.cashierInfoResponse,
                        this.offerOnTotalAmount);
            }
        }

        return null;
    }

    private void populateBulkApplyPromoServiceResponse(BulkApplyPromoServiceResponse bulkApplyPromoServiceResponse,
            NativeCashierInfoResponse response, boolean offerOnTotalAmount) {
        List<PayChannelBase> merchantPayList = response.getBody().getMerchantPayOption().getSavedInstruments();
        List<ApplyPromoResponseData> applyPromoList = bulkApplyPromoServiceResponse.getBulkData();
        injectData(merchantPayList, applyPromoList, offerOnTotalAmount);
    }

    private void injectData(List<PayChannelBase> savedInstrumentsList, List<ApplyPromoResponseData> applyPromoList,
            boolean offerOnTotalAmount) {
        for (int i = 0; i < savedInstrumentsList.size(); i++) {
            PayChannelBase payChannelBase = savedInstrumentsList.get(i);
            ApplyPromoResponseData promoResponseData = applyPromoList.get(i);
            if (promoResponseData != null) {
                PaymentOfferDetails paymentOfferDetails = new PaymentOfferDetails(promoResponseData.getPromocode(),
                        promoResponseData.getPromotext(),
                        com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(payChannelBase.getPayMethod()));
                paymentOfferDetails.setPromoVisibility(Boolean.valueOf(promoResponseData.getPromoVisibility())
                        .toString());
                paymentOfferDetails.setResponseCode(promoResponseData.getResponseCode());
                if (CollectionUtils.isNotEmpty(promoResponseData.getSavings())) {
                    for (PromoSaving promoSaving : promoResponseData.getSavings()) {
                        if (RedemptionType.CASHBACK.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                            paymentOfferDetails.setCashbackAmount(PaymentOfferUtils.getAmountInRupees(promoSaving
                                    .getSavings()));
                        }
                        if (RedemptionType.DISCOUNT.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                            paymentOfferDetails.setInstantDiscount(PaymentOfferUtils.getAmountInRupees(promoSaving
                                    .getSavings()));
                        }
                        if (RedemptionType.PAYTM_CASHBACK.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                            paymentOfferDetails.setPaytmCashbackAmount(PaymentOfferUtils.getAmountInRupees(promoSaving
                                    .getSavings()));
                        }
                    }
                }
                if (payChannelBase instanceof SavedCard) {
                    if (offerOnTotalAmount) {
                        ((SavedCard) payChannelBase).setNonHybridPaymentOfferDetails(paymentOfferDetails);
                    } else {
                        ((SavedCard) payChannelBase).setPaymentOfferDetails(paymentOfferDetails);
                    }
                } else if (payChannelBase instanceof SavedVPA) {
                    if (offerOnTotalAmount) {
                        ((SavedVPA) payChannelBase).setNonHybridPaymentOfferDetails(paymentOfferDetails);
                    } else {
                        ((SavedVPA) payChannelBase).setPaymentOfferDetails(paymentOfferDetails);
                    }
                }
            }
        }
    }

    public PaymentOffersServiceHelper getPaymentOffersServiceHelper() {
        return paymentOffersServiceHelper;
    }

    public void setPaymentOffersServiceHelper(PaymentOffersServiceHelper paymentOffersServiceHelper) {
        this.paymentOffersServiceHelper = paymentOffersServiceHelper;
    }

    public BulkApplyPromoServiceRequest getBulkApplyPromoServiceRequest() {
        return bulkApplyPromoServiceRequest;
    }

    public void setBulkApplyPromoServiceRequest(BulkApplyPromoServiceRequest bulkApplyPromoServiceRequest) {
        this.bulkApplyPromoServiceRequest = bulkApplyPromoServiceRequest;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public NativeCashierInfoRequest getNativeCashierInfoRequest() {
        return nativeCashierInfoRequest;
    }

    public void setNativeCashierInfoRequest(NativeCashierInfoRequest nativeCashierInfoRequest) {
        this.nativeCashierInfoRequest = nativeCashierInfoRequest;
    }

    public NativeCashierInfoResponse getCashierInfoResponse() {
        return cashierInfoResponse;
    }

    public void setCashierInfoResponse(NativeCashierInfoResponse cashierInfoResponse) {
        this.cashierInfoResponse = cashierInfoResponse;
    }

    public WorkFlowResponseBean getWorkFlowResponseBean() {
        return workFlowResponseBean;
    }

    public void setWorkFlowResponseBean(WorkFlowResponseBean workFlowResponseBean) {
        this.workFlowResponseBean = workFlowResponseBean;
    }

    public boolean isOfferOnTotalAmount() {
        return offerOnTotalAmount;
    }

    public void setOfferOnTotalAmount(boolean offerOnTotalAmount) {
        this.offerOnTotalAmount = offerOnTotalAmount;
    }

    public String getPaytmUserId() {
        return paytmUserId;
    }

    public void setPaytmUserId(String paytmUserId) {
        this.paytmUserId = paytmUserId;
    }
}
