package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;

import java.util.Map;

public interface IPayviewConsultServiceHelper<Req, Res> {
    Res transformResponse(WorkFlowResponseBean serviceRes, CashierInfoRequest serviceReq, Req request);

    EntityPaymentOptionsTO getEntityPaymentOption(WorkFlowResponseBean serviceRes, String txnToken);

    void trimCcDcPayChannels(Res response);

    void trimByTopNBChannels(Res response);

    void trimEmiChannelInfo(Res response);

    void populateZestData(Res response);

    void trimConsultFeeResponse(Res response);

    void filterPayChannelinUPINative(Res response);

    void filterZestFromNBInNative(Res response);

    void setMerchantOfferMessage(NativeCashierInfoRequest request, CashierInfoRequest serviceRequest,
            NativeCashierInfoResponse response);

    void setMerchantLimit(WorkFlowResponseBean workFlowResponseBean, NativeCashierInfoResponse nativeCashierInfoResponse);

    Map<String, Object> getPayViewConsultCacheInfo(WorkFlowResponseBean workFlowResponseBean);

    public void trimAdditionalInfoForSavedAssets(NativeCashierInfoResponse response);

}
