package com.paytm.pgplus.theia.nativ.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiPayChannelRequest;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiPayChannelResponse;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiPayChannelResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NativeFetchEmiPayChannelRequestProcessor
        extends
        AbstractRequestProcessor<FetchEmiPayChannelRequest, FetchEmiPayChannelResponse, FetchEmiPayChannelRequest, FetchEmiPayChannelResponse> {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Override
    protected FetchEmiPayChannelRequest preProcess(FetchEmiPayChannelRequest request) {
        validate(request);
        return request;
    }

    @Override
    protected FetchEmiPayChannelResponse onProcess(FetchEmiPayChannelRequest request,
            FetchEmiPayChannelRequest serviceRequest) throws Exception {
        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(request.getHead().getTxnToken());
        NativeCashierInfoResponse cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead()
                .getTxnToken());
        if (cacheResponse == null) {
            NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
            nativeCashierInfoRequest.setHead(request.getHead());
            // populate cache, can't use this to get cacheResponse as response
            // returned is trimmed
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                    nativeCashierInfoRequest);
            nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
            cacheResponse = nativeSessionUtil.getCashierInfoResponse(request.getHead().getTxnToken());
        }
        FetchEmiPayChannelResponseBody responseBody = new FetchEmiPayChannelResponseBody();
        PayOption merchantPayOption = cacheResponse.getBody().getMerchantPayOption();
        responseBody.setPayMethod(filterEmiWithAmount(merchantPayOption,
                null == request.getBody().getAmount() ? orderDetail.getTxnAmount() : request.getBody().getAmount()));
        return new FetchEmiPayChannelResponse(new ResponseHeader(), responseBody);
    }

    private PayMethod filterEmiWithAmount(PayOption merchantPayOption, Money orderAmount) {
        if (null != merchantPayOption && null != merchantPayOption.getPayMethods()) {
            for (PayMethod payMethod : merchantPayOption.getPayMethods()) {
                if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())
                        && null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {
                    List<PayChannelBase> newEmiChannels = new ArrayList<PayChannelBase>();
                    PayMethod emiPayMethod = new PayMethod();
                    for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                        EmiChannel emiChannel = (EmiChannel) payChannelBase;
                        EmiChannel newEmiChannel = new EmiChannel();
                        List<EMIChannelInfo> result = new ArrayList<>();
                        for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                            if (Double.valueOf(orderAmount.getValue()) <= Double.valueOf(emiChannelInfo.getMaxAmount()
                                    .getValue())
                                    && Double.valueOf(emiChannelInfo.getMinAmount().getValue()) <= Double
                                            .valueOf(orderAmount.getValue())) {
                                result.add(emiChannelInfo);
                            }
                        }
                        newEmiChannel.setEmiChannelInfos(result);
                        newEmiChannels.add(newEmiChannel);
                    }
                    emiPayMethod.setPayChannelOptions(newEmiChannels);
                    return emiPayMethod;
                }
            }
        }
        return null;
    }

    @Override
    protected FetchEmiPayChannelResponse postProcess(FetchEmiPayChannelRequest request,
            FetchEmiPayChannelRequest serviceRequest, FetchEmiPayChannelResponse serviceResponse) {
        serviceResponse.getBody().setResultInfo(NativePaymentUtil.resultInfoForSuccess());
        return serviceResponse;
    }

    private void validate(FetchEmiPayChannelRequest request) {
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
    }
}