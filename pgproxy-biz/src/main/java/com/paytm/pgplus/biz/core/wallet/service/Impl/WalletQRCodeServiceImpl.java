package com.paytm.pgplus.biz.core.wallet.service.Impl;

import com.google.zxing.WriterException;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.DeepLinkFields;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.util.UPIIntentUtility;
import com.paytm.pgplus.facade.enums.QrType;
import com.paytm.pgplus.facade.wallet.enums.OperationType;
import com.paytm.pgplus.facade.wallet.enums.PlatformType;
import com.paytm.pgplus.facade.wallet.enums.RequestType;
import com.paytm.pgplus.facade.wallet.models.*;
import com.paytm.pgplus.facade.wallet.services.IWalletQRCodeDetailsService;
import com.paytm.pgplus.http.client.utils.HttpUtils;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.*;

@Service("walletQRCodeServiceImpl")
public class WalletQRCodeServiceImpl implements IWalletQRCodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletQRCodeServiceImpl.class);

    @Autowired
    @Qualifier("walletQRCodeDetailsServiceImpl")
    IWalletQRCodeDetailsService walletQrCodeDetailsService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Override
    public GenericCoreResponseBean<QRCodeDetailsResponse> fetchQRCodeDetails(
            final WorkFlowTransactionBean workFlowTransBean) {
        final WorkFlowRequestBean workFlowRequestBean = workFlowTransBean.getWorkFlowBean();
        boolean isAllInOneQREnabledNonPCF = isAllInOneQREnabledNonPCF(workFlowRequestBean.getPaytmMID());
        boolean isAllInOneQREnabledPCF = isAllInOneEnabledPCF(workFlowRequestBean.getPaytmMID());
        if (ERequestType.DYNAMIC_QR_2FA.equals(workFlowRequestBean.getRequestType())
                || (!workFlowRequestBean.isPostConvenience() && isAllInOneQREnabledNonPCF)
                || (workFlowRequestBean.isPostConvenience() && isAllInOneQREnabledPCF)) {
            return fetchQRCodeDetailsFromWallet(workFlowTransBean);
        } else if (workFlowRequestBean.isEnhancedDynamicUPIQRCodeAllowed()) {
            String qr = null;
            try {
                String amountInRupee = null;
                if (workFlowRequestBean.isPostConvenience()) {
                    amountInRupee = getTotalTxnAmountWithUPIPcfCharges(workFlowTransBean);
                } else {
                    amountInRupee = AmountUtils.getTransactionAmountInRupee(workFlowRequestBean.getTxnAmount());
                }
                qr = QRCreator.createQRCode(createDeepLink(workFlowRequestBean, amountInRupee));
                return new GenericCoreResponseBean<>(new QRCodeDetailsResponse(qr, "", true, QrType.UPI_QR));
            } catch (WriterException | IOException e) {
                LOGGER.error("Error Occurred while creating UPI QR");
            }
            return new GenericCoreResponseBean<>("Unable To fetch QR Code", ResponseConstants.SYSTEM_ERROR);
        }
        return new GenericCoreResponseBean<>("UPI payment mode not available , QR code generation skipped");
    }

    private GenericCoreResponseBean<QRCodeDetailsResponse> fetchQRCodeDetailsFromWallet(
            WorkFlowTransactionBean workFlowTransBean) {
        final WorkFlowRequestBean workFlowRequestBean = workFlowTransBean.getWorkFlowBean();
        CreateDynamicQRResponse createDynamicQRResponse = null;

        try {

            CreateDynamicQRRequest createDynamicQRRequest = new CreateDynamicQRRequest();

            CreateDynamicQRBaseRequest createDynamicQRBaseRequest = new CreateDynamicQRBaseRequest(
                    RequestType.QR_ORDER.getValue(), workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getOrderID(), workFlowRequestBean.getEmailID(),
                    AmountUtils.getTransactionAmountInRupee(workFlowRequestBean.getTxnAmount()),
                    workFlowRequestBean.getChannelID(), workFlowRequestBean.getIndustryTypeID(),
                    workFlowRequestBean.getTransID());

            Map<String, String> additionalInfo = new HashMap<>();
            additionalInfo.put(TheiaConstant.ExtraConstants.ORDER_ALREADY_CREATED, "true");
            additionalInfo.put("website", workFlowRequestBean.getWebsite());
            if (!ff4JUtil.isFeatureEnabled(REMOVE_CALLBACK_FROM_WALLET_QR_REQUEST, workFlowRequestBean.getPaytmMID())) {
                additionalInfo.put("callbackUrl", workFlowRequestBean.getCallBackURL());
            }
            if (StringUtils.isNotBlank(workFlowRequestBean.getExtendInfo().getPeonURL())) {
                additionalInfo.put("peonUrl", workFlowRequestBean.getExtendInfo().getPeonURL());
            }
            // Hack - after wallet QR code changes
            if (!(TRUE.equals(ConfigurationUtil.getTheiaProperty(DISABLE_CHARGE_IN_DYNAMIC_QR_2FA, "false")))
                    && ERequestType.DYNAMIC_QR_2FA.equals(workFlowRequestBean.getRequestType())) {
                additionalInfo.put("chargeAmount",
                        AmountUtils.getTransactionAmountInRupee(workFlowRequestBean.getChargeAmount()));
            }
            if (StringUtils.isBlank(createDynamicQRBaseRequest.getOrderDetails())) {
                createDynamicQRBaseRequest.setOrderDetails(workFlowRequestBean.getOrderID());
            }
            if (workFlowRequestBean.isEnhancedDynamicUPIQRCodeAllowed()) {
                LOGGER.debug("Creating Dynamic UPI QR Code");
                String amountInRupee = null;
                if (workFlowRequestBean.isPostConvenience()) {
                    amountInRupee = getTotalTxnAmountWithUPIPcfCharges(workFlowTransBean);
                } else {
                    amountInRupee = AmountUtils.getTransactionAmountInRupee(workFlowRequestBean.getTxnAmount());
                }
                if (StringUtils.isNotBlank(amountInRupee)) {
                    createDynamicQRBaseRequest.setRequestType(RequestType.UPI_QR_CODE.getValue());
                    String handle = createDeepLink(workFlowRequestBean, amountInRupee);
                    createDynamicQRBaseRequest.setQrInfo(new QrInfo(handle));
                }
            }
            createDynamicQRBaseRequest.setAdditionalInfo(additionalInfo);
            createDynamicQRBaseRequest.setImageRequired(true);

            createDynamicQRRequest.setOperationType(OperationType.QR_CODE.getValue());
            createDynamicQRRequest.setPlatformName(PlatformType.PAYTM.getValue());
            createDynamicQRRequest.setIpAddress(xForwardedIP());
            createDynamicQRRequest.setRequest(createDynamicQRBaseRequest);

            LOGGER.info("CreateDynamicQRRequest : {}", createDynamicQRRequest);
            createDynamicQRResponse = walletQrCodeDetailsService.generateDynamicQRCode(createDynamicQRRequest);
            LOGGER.info("CreateDynamicQRResponse : {}", createDynamicQRResponse);

            if (null != createDynamicQRResponse
                    && BizConstant.QR_SUCCESS.equalsIgnoreCase(createDynamicQRResponse.getStatus())
                    && BizConstant.QR_SUCCESS_CODE.equalsIgnoreCase(createDynamicQRResponse.getStatusCode())
                    || BizConstant.QR_REPEAT_SUCCESS_CODE.equalsIgnoreCase(createDynamicQRResponse.getStatusCode())
                    || BizConstant.UPI_QR_REPEAT_SUCCESS_CODE.equalsIgnoreCase(createDynamicQRResponse.getStatusCode())) {
                QRCodeDetailsResponse QRCodeDetailsResponse = mappingUtil.mapQRCodeDetails(
                        createDynamicQRResponse.getResponse(), createDynamicQRRequest);
                return new GenericCoreResponseBean<>(QRCodeDetailsResponse);
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Wallet QR Code", e);
        }
        return new GenericCoreResponseBean<>(createDynamicQRResponse == null ? "Unable To fetch QR Code"
                : createDynamicQRResponse.getStatusCode(), ResponseConstants.SYSTEM_ERROR);
    }

    private String getTotalTxnAmountWithUPIPcfCharges(WorkFlowTransactionBean workFlowTransBean) {
        GenericCoreResponseBean<ConsultFeeResponse> consultFeeResponse = workFlowHelper.consultBulkFeeResponseForPay(
                workFlowTransBean, EPayMethod.UPI);
        if (!consultFeeResponse.isSuccessfullyProcessed()) {
            LOGGER.warn("Unable to fetch UPI PCF for QR deeplink generation {}",
                    consultFeeResponse.getFailureDescription());
            return null;
        }
        Map<EPayMethod, ConsultDetails> consultDetailsMap = consultFeeResponse.getResponse().getConsultDetails();

        ConsultDetails consultDetails = consultDetailsMap.get(EPayMethod.UPI);
        return consultDetails.getTotalTransactionAmount().toPlainString();
    }

    // amountInRupee is used for creating deeplink with UPI PCF charges
    private String createDeepLink(WorkFlowRequestBean workFlowRequestBean, String amountInRupee) {
        LOGGER.debug("Creating DeepLink for Dynamic QR");
        Map<String, String> map = new LinkedHashMap<>();
        map.put(DeepLinkFields.MERCHANT_VPA.value, workFlowRequestBean.getMerchantVPA());
        map.put(DeepLinkFields.MERCHANT_NAME.value, workFlowRequestBean.getExtendInfo().getMerchantName());
        map.put(DeepLinkFields.CURRENCY_CODE.value, "INR");
        map.put(DeepLinkFields.MCC.value,
                StringUtils.isNotBlank(workFlowRequestBean.getMcc()) ? workFlowRequestBean.getMcc() : "0000");
        map.put(DeepLinkFields.TXN_REF_ID.value, workFlowRequestBean.getOrderID());
        map.put(DeepLinkFields.TXN_AMMOUNT.value, amountInRupee);
        String deeplink = UPIIntentUtility.getDeepLinkFromMap(map).toString();
        LOGGER.info("DeepLink For Dynamic QR code is {} ", deeplink);
        return deeplink;
    }

    private synchronized String xForwardedIP() {
        try {
            InetAddress ia = HttpUtils.getLocalHostLANAddress();
            if (ia != null) {
                return ia.getHostAddress();
            }
        } catch (UnknownHostException e) {
            LOGGER.debug("Unable to get ip address", e);
        }
        return null;
    }

    @Override
    public String getQRDisplayNameByQrCodeId(String qrCodeId) {
        LOGGER.debug("Request received for fetching   QR code Display name  for qrCodeId : {} ", qrCodeId);
        QRCodeInfoBaseResponse qrCodeResponse = getQRCodeInfoByQrCodeId(qrCodeId);
        if (null != qrCodeResponse && null != qrCodeResponse.getResponse()) {
            QRCodeInfoResponseData response = qrCodeResponse.getResponse();
            return response.getName();
        }
        LOGGER.error("Unable to get Merchant Display name for qrCodeId : {} ", qrCodeId);
        return StringUtils.EMPTY;
    }

    @Override
    public QRCodeInfoBaseResponse getQRCodeInfoByQrCodeId(String qrCodeId) {
        final long startTime = System.currentTimeMillis();
        LOGGER.debug("Request received for fetching  QRCodeResponse for qrCodeId : {} ", qrCodeId);
        QRCodeInfoRequestData requestData = new QRCodeInfoRequestData(qrCodeId);
        QRCodeInfoBaseRequest request = new QRCodeInfoBaseRequest();
        request.setRequest(requestData);
        request.setOperationType(OperationType.QR_CODE.getValue());
        request.setPlatformName(PlatformType.PAYTM.getValue());
        request.setIpAddress(xForwardedIP());
        request.setRequest(requestData);

        QRCodeInfoBaseResponse response = new QRCodeInfoBaseResponse();
        try {
            response = walletQrCodeDetailsService.getQRCodeInfoByQrCodeId(request);
        } catch (Exception e) {
            LOGGER.error("Error occured while fetching Wallet QR Code info for qrCodeId : {}  with exception::{}",
                    qrCodeId, e);
        } finally {
            LOGGER.info("Total time taken for fetching  QRCodeResponse is {} ms", System.currentTimeMillis()
                    - startTime);
        }
        return response;
    }

    public boolean isAllInOneQREnabledNonPCF(String mid) {
        LOGGER.debug("checking if isAllInOneQREnabledNonPCF for mid:{}", mid);
        return ff4JUtil.isFeatureEnabled(BizConstant.IS_ALL_IN_ONE_QR_ENABLED, mid);
    }

    public boolean isAllInOneEnabledPCF(String mid) {
        LOGGER.debug("checking if isAllInOneEnabledPCF for mid:{}", mid);
        return ff4JUtil.isFeatureEnabled(BizConstant.IS_ALL_IN_ONE_QR_ENABLED_PCF, mid);
    }
}
