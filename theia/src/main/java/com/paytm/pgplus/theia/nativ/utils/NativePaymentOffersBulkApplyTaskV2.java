package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.BulkApplyPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.PromoSaving;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.*;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.service.NativePaymentBulkOffersServiceV2;
import com.paytm.pgplus.theia.paymentoffer.enums.RedemptionType;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class NativePaymentOffersBulkApplyTaskV2 implements Callable<BulkApplyPromoServiceResponseV2> {

    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePaymentBulkOffersServiceV2.class);

    private BulkApplyPromoServiceRequestV2 bulkApplyPromoServiceRequest;
    private String mid;
    private String custId;
    private String orderId;

    private NativeCashierInfoRequest nativeCashierInfoRequest;
    private NativeCashierInfoResponse cashierInfoResponse;
    private WorkFlowResponseBean workFlowResponseBean;
    private boolean offerOnTotalAmount;
    private String paytmUserId;

    public NativePaymentOffersBulkApplyTaskV2() {
    }

    @Override
    public BulkApplyPromoServiceResponseV2 call() throws Exception {
        this.custId = nativeCashierInfoRequest.getBody().getCustId();

        if (StringUtils.isBlank(this.custId)) {
            this.custId = workFlowResponseBean.getUserDetails() != null ? workFlowResponseBean.getUserDetails()
                    .getUserId() : null;
        }
        this.mid = nativeCashierInfoRequest.getBody().getMid();
        this.paytmUserId = workFlowResponseBean.getUserDetails() != null ? workFlowResponseBean.getUserDetails()
                .getUserId() : null;

        this.bulkApplyPromoServiceRequest = paymentOffersServiceHelperV2.prepareApplyPromoServiceRequest(
                nativeCashierInfoRequest, cashierInfoResponse, workFlowResponseBean, offerOnTotalAmount, custId,
                paytmUserId);

        if (bulkApplyPromoServiceRequest != null) {
            if (CollectionUtils.isEmpty(bulkApplyPromoServiceRequest.getPaymentDetailsBulk().getPaymentOptionsBulk())) {
                LOGGER.info("paymentOptionsBulk in bulkApplyPromoServiceRequest is null, hence returning null");
                return null;
            }
            BulkApplyPromoServiceResponseV2 bulkApplyPromoServiceRes = paymentOffersServiceHelperV2.bulkApplyPromoV2(
                    bulkApplyPromoServiceRequest, mid, custId, orderId, paytmUserId);

            if (paymentOffersServiceHelperV2.isPromoServiceSuccessResponse(bulkApplyPromoServiceRes)
                    && CollectionUtils.isNotEmpty(bulkApplyPromoServiceRes.getData())) {
                populateBulkApplyPromoServiceResponse(bulkApplyPromoServiceRes, this.cashierInfoResponse,
                        this.offerOnTotalAmount);
            }
        }

        return null;
    }

    private void populateBulkApplyPromoServiceResponse(BulkApplyPromoServiceResponseV2 bulkApplyPromoServiceResponse,
            NativeCashierInfoResponse response, boolean offerOnTotalAmount) {
        List<PayChannelBase> merchantPayList = response.getBody().getMerchantPayOption().getSavedInstruments();
        List<ApplyPromoResponseDataV2> applyPromoList = bulkApplyPromoServiceResponse.getData();
        if (nativeCashierInfoRequest.getBody().getApplyItemOffers() != null
                && nativeCashierInfoRequest.getBody().getApplyItemOffers().getPromoContext() != null) {
            injectDataV2(merchantPayList, applyPromoList, offerOnTotalAmount);
        } else {
            injectData(merchantPayList, applyPromoList, offerOnTotalAmount, response.getBody());
        }
    }

    private void injectData(List<PayChannelBase> savedInstrumentsList, List<ApplyPromoResponseDataV2> applyPromoList,
            boolean offerOnTotalAmount, NativeCashierInfoResponseBody responseBody) {
        int promoIndex = 0;
        LOGGER.info("Size of SaveCard {} , Promo {} ", savedInstrumentsList.size(), applyPromoList.size());
        for (int i = 0; i < savedInstrumentsList.size(); i++) {
            PayChannelBase payChannelBase = savedInstrumentsList.get(i);
            ApplyPromoResponseDataV2 promoResponseData = applyPromoList.get(promoIndex++);
            if (promoResponseData != null && promoResponseData.getPromoResponse() != null
                    && promoResponseData.getPromoResponse().size() > 0) {
                for (Map.Entry<String, PromoResponseData> entry : promoResponseData.getPromoResponse().entrySet()) {
                    PaymentOfferDetails paymentOfferDetails = new PaymentOfferDetails(entry.getKey(),
                            promoResponseData.getPromotext(),
                            com.paytm.pgplus.enums.PayMethod.getPayMethodByMethod(payChannelBase.getPayMethod()));
                    paymentOfferDetails.setPromoVisibility(Boolean.valueOf(promoResponseData.isPromoVisibility())
                            .toString());
                    paymentOfferDetails.setResponseCode(promoResponseData.getResponseCode());

                    if (entry.getValue() != null && MapUtils.isNotEmpty(entry.getValue().getItems())) {
                        if (nativeCashierInfoRequest.getBody().getApplyItemOffers() != null
                                && nativeCashierInfoRequest.getBody().getApplyItemOffers().getCartDetails() != null) {
                            PromoCartOfferDetail cartOfferDetail = new PromoCartOfferDetail();
                            List<PromoItemOffer> itemOffers = new ArrayList<>();
                            getItemOffer(entry, promoResponseData, paymentOfferDetails, cartOfferDetail, itemOffers);
                        } else if (entry.getValue().getItems().get("item001") != null) {
                            Items items = entry.getValue().getItems().get("item001");
                            if (CollectionUtils.isNotEmpty(items.getUsage_data())) {
                                for (PromoUsageData promoUsageData : items.getUsage_data()) {
                                    if (RedemptionType.CASHBACK.getType().equalsIgnoreCase(
                                            promoUsageData.getRedemptionType())) {
                                        paymentOfferDetails.setCashbackAmount(PaymentOfferUtils
                                                .getAmountInRupees(promoUsageData.getAmount()));
                                    }
                                    if (RedemptionType.DISCOUNT.getType().equalsIgnoreCase(
                                            promoUsageData.getRedemptionType())) {
                                        paymentOfferDetails.setInstantDiscount(PaymentOfferUtils
                                                .getAmountInRupees(promoUsageData.getAmount()));
                                    }
                                    if (RedemptionType.PAYTM_CASHBACK.getType().equalsIgnoreCase(
                                            promoUsageData.getRedemptionType())) {
                                        paymentOfferDetails.setPaytmCashbackAmount(PaymentOfferUtils
                                                .getAmountInRupees(promoUsageData.getAmount()));
                                    }
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
        }
    }

    private void getItemOffer(Map.Entry<String, PromoResponseData> entry, ApplyPromoResponseDataV2 promoResponseData,
            PaymentOfferDetails paymentOfferDetails, PromoCartOfferDetail cartOfferDetail,
            List<PromoItemOffer> itemOffers) {
        if (entry.getValue() != null && MapUtils.isNotEmpty(entry.getValue().getItems())) {
            PromoItemOffer item = new PromoItemOffer();
            item.setPromocode(entry.getKey());
            item.setItems(getItemOfferDetail(entry.getValue().getItems()));
            itemOffers.add(item);

        }
        cartOfferDetail.setItemOffers(itemOffers);

        paymentOfferDetails.setcartOfferDetail(cartOfferDetail);

        if (promoResponseData.getSavings() != null) {
            for (PromoSaving promoSaving : promoResponseData.getSavings()) {
                if (RedemptionType.CASHBACK.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                    paymentOfferDetails
                            .setCashbackAmount(PaymentOfferUtils.getAmountInRupees(promoSaving.getSavings()));
                }
                if (RedemptionType.DISCOUNT.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                    paymentOfferDetails
                            .setInstantDiscount(PaymentOfferUtils.getAmountInRupees(promoSaving.getSavings()));
                }
                if (RedemptionType.PAYTM_CASHBACK.getType().equalsIgnoreCase(promoSaving.getRedemptionType())) {
                    paymentOfferDetails.setPaytmCashbackAmount(PaymentOfferUtils.getAmountInRupees(promoSaving
                            .getSavings()));
                }
            }
        }
    }

    private List<PromoItemOfferDetail> getItemOfferDetail(Map<String, Items> items) {
        List<PromoItemOfferDetail> offerDetails = new ArrayList<>();
        for (Map.Entry<String, Items> itemsEntry : items.entrySet()) {
            PromoItemOfferDetail offerDetail = new PromoItemOfferDetail();
            offerDetail.setId(itemsEntry.getKey());
            offerDetail.setMetaData(getMetaData(itemsEntry.getValue()));
            offerDetails.add(offerDetail);
        }
        return offerDetails;
    }

    private List<PromoItemUsageData> getMetaData(Items value) {
        List<PromoItemUsageData> metaData = new ArrayList<>();
        for (PromoUsageData usageData : value.getUsage_data()) {
            if (usageData != null) {
                PromoItemUsageData itemUsageData = new PromoItemUsageData();
                itemUsageData.setAmount(usageData.getAmount());
                itemUsageData.setCampaign(usageData.getCampaign());
                itemUsageData.setPromocode(usageData.getPromocode());
                itemUsageData.setFlags(usageData.getFlags());
                itemUsageData.setCustomText(usageData.getCustom_text());
                itemUsageData.setFraud1(usageData.getFraud1());
                itemUsageData.setPromoGratificationData(usageData.getPromoGratificationData());
                itemUsageData.setFulfillmentStatus(usageData.getFulfillmentStatus());
                itemUsageData.setSiteId(usageData.getSiteId());
                itemUsageData.setUserId(usageData.getUserId());
                itemUsageData.setPromocodeId(usageData.getPromocodeId());
                itemUsageData.setStatus(usageData.getStatus());
                itemUsageData.setRedemptionType(usageData.getRedemptionType());
                metaData.add(itemUsageData);
            }
        }
        return metaData;
    }

    private void injectDataV2(List<PayChannelBase> savedInstrumentsList, List<ApplyPromoResponseDataV2> applyPromoList,
            Boolean offerOnTotalAmount) {
        int promoIndex = 0;
        LOGGER.info("Size of SaveCard {} , Promo {} ", savedInstrumentsList.size(), applyPromoList.size());
        for (int i = 0; i < savedInstrumentsList.size(); i++) {
            PayChannelBase payChannelBase = savedInstrumentsList.get(i);
            ApplyPromoResponseDataV2 promoResponseData = applyPromoList.get(promoIndex++);
            if (promoResponseData != null) {
                if (promoResponseData.getPromoContext() != null) {
                    // TODO: Can extract common fields into a separate class
                    ItemLevelPaymentOffer paymentOffer = new ItemLevelPaymentOffer();
                    paymentOffer.setVerificationCode(promoResponseData.getVerificationCode());
                    paymentOffer.setStatus(promoResponseData.getStatus());
                    paymentOffer.setTncUrl(promoResponseData.getTncUrl());
                    paymentOffer.setPromotext(promoResponseData.getPromotext());
                    paymentOffer.setPrePromoText(promoResponseData.getPrePromoText());
                    paymentOffer.setEffectivePromoSaving(PaymentOfferUtils.getAmountInRupees(promoResponseData
                            .getEffectivePromoSaving()));
                    paymentOffer.setEffectivePromoDeduction(PaymentOfferUtils.getAmountInRupees(promoResponseData
                            .getEffectivePromoDeduction()));
                    paymentOffer.setPayText(promoResponseData.getPayText());
                    paymentOffer.setPromoVisibility(Boolean.valueOf(promoResponseData.isPromoVisibility()).toString());
                    paymentOffer.setPromoContext(promoResponseData.getPromoContext());
                    paymentOffer.setResponseCode(promoResponseData.getResponseCode());
                    paymentOffer.setPromoCode(promoResponseData.getPromocode());
                    if (CollectionUtils.isNotEmpty(promoResponseData.getSavings())) {
                        List<ItemLevelPaymentOffer.PromoSaving> savings = new ArrayList<>();
                        for (PromoSaving saving : promoResponseData.getSavings()) {
                            savings.add(new ItemLevelPaymentOffer.PromoSaving(PaymentOfferUtils
                                    .getAmountInRupees(saving.getSavings()), saving.getRedemptionType()));
                        }
                        paymentOffer.setSavings(savings);
                    }
                    if (payChannelBase instanceof SavedCard) {
                        ((SavedCard) payChannelBase).setPaymentOfferDetailsV2(paymentOffer);
                    } else if (payChannelBase instanceof SavedVPA) {
                        ((SavedVPA) payChannelBase).setPaymentOfferDetailsV2(paymentOffer);
                    }
                }
            }
        }
    }

    public PaymentOffersServiceHelperV2 getPaymentOffersServiceHelper() {
        return paymentOffersServiceHelperV2;
    }

    public void setPaymentOffersServiceHelper(PaymentOffersServiceHelperV2 paymentOffersServiceHelper) {
        this.paymentOffersServiceHelperV2 = paymentOffersServiceHelper;
    }

    public BulkApplyPromoServiceRequestV2 getBulkApplyPromoServiceRequest() {
        return bulkApplyPromoServiceRequest;
    }

    public void setBulkApplyPromoServiceRequest(BulkApplyPromoServiceRequestV2 bulkApplyPromoServiceRequest) {
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
