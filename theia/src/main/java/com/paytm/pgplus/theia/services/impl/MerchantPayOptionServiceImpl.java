package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.payment.models.PayMethodView;
import com.paytm.pgplus.facade.payment.models.request.LitePayviewConsultRequest;
import com.paytm.pgplus.facade.payment.models.request.LitePayviewConsultRequestBody;
import com.paytm.pgplus.facade.payment.models.response.LitePayviewConsultResponse;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.payloadvault.theia.request.PayOptionsRequest;
import com.paytm.pgplus.payloadvault.theia.response.PayOptionsResponse;
import com.paytm.pgplus.payloadvault.theia.response.PaymentChannel;
import com.paytm.pgplus.payloadvault.theia.response.PaymentMethod;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.services.IMerchantPayOptionService;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.*;

/**
 * Created by ankitgupta on 10/8/17.
 */
@Service("merchantPayOptionService")
public class MerchantPayOptionServiceImpl implements IMerchantPayOptionService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MerchantPayOptionServiceImpl.class);

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    private ICashier cashier;

    @Override
    public PayOptionsResponse processPayMethodsRequest(PayOptionsRequest payOptionsRequest)
            throws FacadeCheckedException {
        MDC.put(TheiaConstant.RequestParams.MID, payOptionsRequest.getMid());
        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(payOptionsRequest.getRequest());
        String productCode = ProductCodes.StandardDirectPayAcquiringProd.getId();
        String merchantId = this.fetchAlipayMerchantId(payOptionsRequest.getMid());
        if (null == merchantId) {
            return null;
        }
        final LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder body = new LitePayviewConsultRequestBody.LitePayviewConsultRequestBodyBuilder(
                productCode, EnvInfoUtil.createEnvInfo(envInfo));
        body.merchantId(merchantId);
        final LitePayviewConsultRequest litePayviewConsultRequest = new LitePayviewConsultRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.CONSULT_LITEPAYVIEW), body.build());

        final LitePayviewConsultResponse payViewConsultResponse = cashier.litePayviewConsult(litePayviewConsultRequest);

        if (payViewConsultResponse == null || payViewConsultResponse.getBody() == null
                || payViewConsultResponse.getBody().getResultInfo() == null) {
            return new PayOptionsResponse(UNKNOWN_ERROR_CODE, UNKNOWN_ERROR_MSG);
        }
        if (!SUCCESS.equals(payViewConsultResponse.getBody().getResultInfo().getResultCode())) {
            return new PayOptionsResponse(payViewConsultResponse.getBody().getResultInfo().getResultCodeId(),
                    payViewConsultResponse.getBody().getResultInfo().getResultMsg());
        }
        return mapPayOptionsResponse(payViewConsultResponse);
    }

    private PayOptionsResponse mapPayOptionsResponse(LitePayviewConsultResponse payViewConsultResponse) {
        PayOptionsResponse payOptionsResponse = new PayOptionsResponse("0", payViewConsultResponse.getBody()
                .getResultInfo().getResultMsg());

        if (payViewConsultResponse.getBody().getPayMethodViews() != null) {
            List<PaymentMethod> paymentMethods = new ArrayList<>();
            for (PayMethodView payMethodView : payViewConsultResponse.getBody().getPayMethodViews()) {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setPaymentMethod(payMethodView.getPayMethod().getMethod());
                if (payMethodView.getPayChannelOptionViews() != null) {
                    List<PaymentChannel> paymentChannels = new ArrayList<>();
                    for (PayChannelOptionView payChannelOptionView : payMethodView.getPayChannelOptionViews()) {
                        PaymentChannel paymentChannel = new PaymentChannel();
                        paymentChannel.setEnableStatus(String.valueOf(payChannelOptionView.isEnableStatus()));
                        paymentChannel.setInstId(payChannelOptionView.getInstId());
                        paymentChannel.setInstName(payChannelOptionView.getInstName());
                        paymentChannel.setPayOption(payChannelOptionView.getPayOption());
                        if (StringUtils.isNotBlank(payChannelOptionView.getInstId())
                                && PayMethod.NET_BANKING.equals(payMethodView.getPayMethod())) {
                            BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(payChannelOptionView
                                    .getInstId());
                            if (bankInfoData != null) {
                                paymentChannel.setBankLogo(bankInfoData.getBankWebLogo());
                            }
                        }
                        paymentChannels.add(paymentChannel);
                    }
                    paymentMethod.setPaymentChannels(paymentChannels);
                }
                paymentMethods.add(paymentMethod);
            }
            payOptionsResponse.setPaymentMethods(paymentMethods);
        }

        return payOptionsResponse;
    }

    private String fetchAlipayMerchantId(String mId) {
        String alipayMerchantId = StringUtils.EMPTY;
        try {
            MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(mId);
            if (mappingMerchantData != null) {
                alipayMerchantId = mappingMerchantData.getAlipayId();
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching Merchant mapping data from Redis/Mapping Service for "
                    + "merchantId : {}", mId, e);
        }
        if (StringUtils.isBlank(alipayMerchantId)) {
            throw new TheiaServiceException("Could not map merchant id, due to merchant id is null or blank ");
        }
        return alipayMerchantId;
    }
}
