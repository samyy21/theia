package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.core.model.request.CreateOrderAndPayRequestBean;
import com.paytm.pgplus.facade.routingEngine.BinDetail;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.payment.models.ChannelPreference;
import com.paytm.pgplus.facade.payment.models.PayOptionBill;
import com.paytm.pgplus.facade.payment.models.PreferenceValue;
import com.paytm.pgplus.facade.routingEngine.CostPreferredGateway;
import com.paytm.pgplus.facade.routingEngine.FetchCostAcquirerInfoRequestBody;
import com.paytm.pgplus.facade.routingEngine.FetchCostAcquirerInfoResponse;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.routingEngineClient.service.impl.RoutingEngineServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.facade.enums.PayMethod.CREDIT_CARD;

@Component
public class CostBasedPreferenceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CostBasedPreferenceUtil.class);

    private static RoutingEngineServiceImpl routingEngineService;

    private static final String MERCHANT_BASELINE_VALUE = "merchantBaselineValue";

    private static final String MERCHANT_DEVIATION_RATIO = "merchantDynamicDeviationRatio";

    public static final String THEIA_VSC_REQUEST_FOR_CBR = "theia.vscRequestForCBR";

    @Autowired
    public CostBasedPreferenceUtil(RoutingEngineServiceImpl routingEngineService) {
        this.routingEngineService = routingEngineService;
    }

    @Autowired
    private Ff4jUtils ff4jUtil;

    private static Ff4jUtils ff4jUtils;

    @PostConstruct
    private void init() {
        ff4jUtils = this.ff4jUtil;
    }

    public static void setCostPreferredGateways(List<PayOptionBill> payOptionBills, boolean isAddnPay,
            boolean isTopUpnpay, CreateOrderAndPayRequestBean createOrderAndPayRequest) {
        try {
            for (PayOptionBill payOption : payOptionBills) {
                if ((CREDIT_CARD.getMethod().equals(payOption.getPayMethod().getMethod()) || PayMethod.DEBIT_CARD
                        .getMethod().equals(payOption.getPayMethod().getMethod()))
                        || (PayMethod.EMI.getMethod().equals(payOption.getPayMethod().getMethod()) && CREDIT_CARD
                                .getMethod().equals(
                                        payOption.getExtendInfo().get(TheiaConstant.RequestParams.CARD_TYPE)))) {
                    FetchCostAcquirerInfoRequestBody fetchCostAcquirerInfoRequest = CostBasedPreferenceUtil
                            .getFetchAcquirerInfoRequest(payOption, isAddnPay, isTopUpnpay, createOrderAndPayRequest);
                    FetchCostAcquirerInfoResponse fetchCostAcquirerInfoResponse = routingEngineService
                            .getCostPreferredGateways(fetchCostAcquirerInfoRequest);
                    if (fetchCostAcquirerInfoResponse != null && fetchCostAcquirerInfoResponse.getBody() != null
                            && !CollectionUtils.isEmpty(fetchCostAcquirerInfoResponse.getBody().getAcquirers())) {
                        LOGGER.info("Setting costacquirerInfoResponse in channelPreference");
                        CostBasedPreferenceUtil.setChannelPreference(payOption, fetchCostAcquirerInfoResponse);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occurred while adding cost preferred gateways", e);
        }
    }

    public static FetchCostAcquirerInfoRequestBody getFetchAcquirerInfoRequest(PayOptionBill payOptionBill,
            boolean isAddnPay, boolean isTopUpnpay, CreateOrderAndPayRequestBean createOrderAndPayRequest) {
        FetchCostAcquirerInfoRequestBody requestBody = new FetchCostAcquirerInfoRequestBody();
        requestBody.setAmount(payOptionBill.getTransAmount().getAmount());
        requestBody.setPayMode(payOptionBill.getPayMethod().getMethod());
        requestBody.setMid(MDC.get(TheiaConstant.RequestParams.MID));
        requestBody.setOrderId(MDC.get(TheiaConstant.RequestParams.ORDER_ID));
        requestBody.setAddAndPay(isAddnPay);
        requestBody.setTopUpAndPay(isTopUpnpay);

        boolean vscRequest = ff4jUtils.isFeatureEnabledOnMid(MDC.get(TheiaConstant.RequestParams.MID),
                THEIA_VSC_REQUEST_FOR_CBR, false);
        if (vscRequest
                && ("ONE_CLICK_ENROLL".equals(payOptionBill.getPayMode()) || "ONE_CLICK_PAY".equals(payOptionBill
                        .getPayMode()))) {
            requestBody.setVscRequest(true);
        }
        BinDetail binDetail = new BinDetail();
        binDetail.setCardName(payOptionBill.getExtendInfo().get(TheiaConstant.RequestParams.CARD_NAME));
        binDetail.setBankCode(payOptionBill.getExtendInfo().get(TheiaConstant.RequestParams.BANK_CODE_CBR));
        binDetail
                .setIsActive(Boolean.valueOf(payOptionBill.getExtendInfo().get(TheiaConstant.RequestParams.IS_ACTIVE)));
        binDetail
                .setIsIndian(Boolean.valueOf(payOptionBill.getExtendInfo().get(TheiaConstant.RequestParams.IS_INDIAN)));
        binDetail.setCorporateCard(Boolean.valueOf(payOptionBill.getExtendInfo().get(
                TheiaConstant.RequestParams.CORPORATE_CARD)));
        binDetail.setPrepaidCard(Boolean.valueOf(payOptionBill.getExtendInfo().get(
                TheiaConstant.RequestParams.PREPAID_CARD)));
        requestBody.setBinDetail(binDetail);

        // setting orderID from bean if not found in MDC
        if (StringUtils.isBlank(requestBody.getOrderId())) {
            if (null != createOrderAndPayRequest && createOrderAndPayRequest.getOrder() != null
                    && StringUtils.isNotBlank(createOrderAndPayRequest.getOrder().getOrderId())) {
                requestBody.setOrderId(createOrderAndPayRequest.getOrder().getOrderId());
            }
        }
        return requestBody;
    }

    public static void setChannelPreference(PayOptionBill payOptionBill,
            FetchCostAcquirerInfoResponse fetchCostAcquirerInfoResponse) {
        ChannelPreference channelPreference = new ChannelPreference();
        List<PreferenceValue> preferenceValueList = new ArrayList<>();
        Map<String, String> extendInfo = new HashMap<String, String>();
        for (CostPreferredGateway costPreferredGateway : fetchCostAcquirerInfoResponse.getBody().getAcquirers()) {
            PreferenceValue preferenceValue = new PreferenceValue(costPreferredGateway.getServiceInstId(),
                    Integer.valueOf(costPreferredGateway.getScore()));
            preferenceValueList.add(preferenceValue);
        }
        if (StringUtils.isNotBlank(fetchCostAcquirerInfoResponse.getBody().getMerchantBaselineValue())) {
            extendInfo.put(MERCHANT_BASELINE_VALUE, fetchCostAcquirerInfoResponse.getBody().getMerchantBaselineValue());
        }
        if (StringUtils.isNotBlank(fetchCostAcquirerInfoResponse.getBody().getMerchantDynamicDeviationRatio())) {
            extendInfo.put(MERCHANT_DEVIATION_RATIO, fetchCostAcquirerInfoResponse.getBody()
                    .getMerchantDynamicDeviationRatio());
        }
        channelPreference.setPreferenceValues(preferenceValueList);
        if (MapUtils.isNotEmpty(extendInfo)) {
            channelPreference.setExtendInfo(extendInfo);
        }
        payOptionBill.setChannelPreference(channelPreference);
    }
}
