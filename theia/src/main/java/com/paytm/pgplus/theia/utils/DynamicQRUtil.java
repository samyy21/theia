package com.paytm.pgplus.theia.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.QueryByMerchantTransIDResponseBizBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.payloadvault.theia.constant.CommonConstants;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestQrException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.SUBWALLET_AMOUNT_DETAILS;

@Component("dynamicQRUtil")
public class DynamicQRUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRUtil.class);

    private static final TypeReference<HashMap<UserSubWalletType, BigDecimal>> SUBWALLET_TYPE_REF = new TypeReference<HashMap<UserSubWalletType, BigDecimal>>() {
    };

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    public boolean isDynamicQREdcRequest(PaymentRequestBean paymentRequestBean) {
        String channelId = AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean,
                TheiaConstant.RequestParams.CHANNEL_ID);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(channelId)
                && TheiaConstant.RequestParams.EDC.equals(channelId)) {
            return true;
        }
        return false;
    }

    public String getTransId(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        String transId = (String) theiaTransactionalRedisUtil.get(getQrOrderKey(requestData.getMid(),
                requestData.getOrderId()));

        if (StringUtils.isBlank(transId)) {
            transId = requestData.getTxnId();
        }

        workRequestCreator.getRoute(workFlowRequestBean, "queryByAcquirementId");

        if (StringUtils.isBlank(transId)) {
            WorkFlowTransactionBean flowTransBean = new WorkFlowTransactionBean();
            flowTransBean.setWorkFlowBean(workFlowRequestBean);
            GenericCoreResponseBean<QueryByMerchantTransIDResponseBizBean> queryByMerchantIDResponse = workFlowHelper
                    .queryByMerchantTransID(flowTransBean, true);
            if (!queryByMerchantIDResponse.isSuccessfullyProcessed()
                    || org.apache.commons.lang.StringUtils.isBlank(queryByMerchantIDResponse.getResponse()
                            .getAcquirementId())) {
                LOGGER.error("Query by Merchant TransID status is Invalid : {}", queryByMerchantIDResponse);
            } else {
                if (AcquirementStatusType.CLOSED.equals(queryByMerchantIDResponse.getResponse().getStatusDetail()
                        .getAcquirementStatus())) {
                    LOGGER.error("Order is closed so terminating transaction  ");
                    // throw new TheiaServiceException("Order is closed");
                    throw new PaymentRequestQrException(ResultCode.QR_EXPIRED, ResponseConstants.QR_EXPIRED_ERROR);
                }
                transId = queryByMerchantIDResponse.getResponse().getAcquirementId();
                workFlowRequestBean.setOrderAmount(queryByMerchantIDResponse.getResponse().getAmountDetail()
                        .getOrderAmount().getAmountInRs());

                Map<String, String> extendInfo = queryByMerchantIDResponse.getResponse().getExtendInfo();
                if (extendInfo != null
                        && (extendInfo.get("extraParamsMap.headAccount") != null || extendInfo
                                .get("extraParamsMap.remitterName") != null)) {
                    if (workFlowRequestBean.getExtendInfo() != null) {
                        if (workFlowRequestBean.getExtendInfo().getExtraParamsMap().isEmpty())
                            workFlowRequestBean.getExtendInfo().setExtraParamsMap(new HashMap<>());
                        workFlowRequestBean
                                .getExtendInfo()
                                .getExtraParamsMap()
                                .put("challanIdNum",
                                        workFlowHelper
                                                .generateCIN(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.BSR_CODE));
                        workFlowRequestBean
                                .getExtendInfo()
                                .getExtraParamsMap()
                                .put("bsrCode",
                                        com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.BSR_CODE);
                    } else {
                        LOGGER.error("workFlowRequestBean.getExtendInfo() is null {}", workFlowRequestBean);
                    }
                }

                // to support subwallet in All in one payment request link
                setSubWalletAmountToWorkFlowBean(workFlowRequestBean, queryByMerchantIDResponse.getResponse());

                checkPreferenceEnabledForCobrandedCards(workFlowRequestBean);
            }
        }
        return transId;
    }

    private void checkPreferenceEnabledForCobrandedCards(WorkFlowRequestBean flowRequestBean) {
        BinDetail binDetail = processTransactionUtil.getBinDetail(flowRequestBean);
        LOGGER.info("binDetail for Cobranded card is {}", binDetail);
        if (binDetail != null) {
            String cardBin = String.valueOf(binDetail.getBin());
            if (StringUtils.isNotBlank(cardBin)
                    && merchantPreferenceService.isCobrandedCardBinPreferenceEnabled(flowRequestBean.getPaytmMID(),
                            cardBin)) {
                flowRequestBean.setCobarandPreferenceEnabled(true);
            }
        }
    }

    private void setSubWalletAmountToWorkFlowBean(WorkFlowRequestBean workFlowRequestBean,
            QueryByMerchantTransIDResponseBizBean queryByMerchantIDResponse) {
        if (Objects.nonNull(workFlowRequestBean)
                && Objects.isNull(workFlowRequestBean.getSubWalletOrderAmountDetails())) {
            try {
                Map<String, String> extendInfo = queryByMerchantIDResponse.getExtendInfo();

                if (Objects.nonNull(extendInfo) && StringUtils.isNotBlank(extendInfo.get(SUBWALLET_AMOUNT_DETAILS))) {
                    workFlowRequestBean.setSubWalletOrderAmountDetails(OBJECT_MAPPER.readValue(
                            extendInfo.get(SUBWALLET_AMOUNT_DETAILS), SUBWALLET_TYPE_REF));
                }
            } catch (Exception exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }
    }

    private String getQrOrderKey(String mid, String orderId) {
        return CommonConstants.RedisKeyPrefix.QR_ORDER + mid + orderId;
    }

    public boolean isOrderAlreadyCreated(PaymentRequestBean paymentRequestBean) {
        String orderAlreadyCreatedFlag = AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean,
                TheiaConstant.RequestParams.ORDER_ALREADY_CREATED);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(orderAlreadyCreatedFlag)) {
            return Boolean.valueOf(orderAlreadyCreatedFlag);
        }
        return false;
    }

    public String getWebsiteName(PaymentRequestBean paymentRequestBean) {
        return AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean, "website");
    }

    public String getCallbackUrl(PaymentRequestBean paymentRequestData) {
        return AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestData, "callbackUrl");
    }

    public String getPeonUrl(PaymentRequestBean paymentRequestBean) {
        return AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean, "peonUrl");
    }

    public String parseChargeAmountFromAdditionalInfo(String additionalInfo) {
        if (StringUtils.isNotBlank(additionalInfo)) {
            String[] pairArr = additionalInfo.split(Pattern.quote("|"));
            for (String keyVal : pairArr) {
                String[] split = keyVal.split(Pattern.quote(":"));
                if (split.length == 2 && "chargeAmount".equals(split[0])) {
                    return AmountUtils.getTransactionAmountInPaise(split[1]);
                }
            }
        }
        return null;
    }

    public boolean checkIsEdcRequest(PaymentRequestBean requestData) {
        if (StringUtils.isBlank(requestData.getMid()) || StringUtils.isBlank(requestData.getOrderId())
                || StringUtils.isBlank(requestData.getQrCodeId())) {
            return false;
        }
        String token = requestData.getMid() + requestData.getOrderId() + requestData.getQrCodeId();
        return Boolean.parseBoolean(nativeSessionUtil.getIsEdcRequest(token));
    }

    public boolean isAoaDqrOrder(PaymentRequestBean paymentRequestBean) {
        String qrCodeId = AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean,
                TheiaConstant.RequestParams.IS_QR_CODE_ID);
        String skipOrderCreationFlag = AdditionalInfoUtil.getValueFromAdditionalInfo(paymentRequestBean,
                TheiaConstant.RequestParams.IS_SKIP_ORDER_CREATION);
        if (null != paymentRequestBean && org.apache.commons.lang3.StringUtils.isNotBlank(qrCodeId)
                && org.apache.commons.lang3.StringUtils.isNotBlank(skipOrderCreationFlag)) {
            return Boolean.valueOf(skipOrderCreationFlag);
        }
        return false;
    }
}
