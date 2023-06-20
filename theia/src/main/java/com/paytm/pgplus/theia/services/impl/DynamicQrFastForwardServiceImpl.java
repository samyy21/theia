package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.UPIPSPUtil;
import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.DynamicQRUtil;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.AOA_DQR;
import static com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_WALLET_PAYMENT_MODE;

@Service("dynamicQrFastForwardService")
public class DynamicQrFastForwardServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicQrFastForwardServiceImpl.class);

    @Autowired
    @Qualifier("dynamicQrFastForwardFlow")
    private IWorkFlow dynamicQrFastForwardFlow;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    private AutoDebitCoreService autoDebitCoreService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private DynamicQRUtil dynamicQRUtil;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private UPIPSPUtil upipspUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) {
        LOGGER.info("Processing payment request for dynamicQrFastForwardFlow, order id :{}", requestData.getOrderId());
        validateFetchPaymentOptions(requestData);

        // Code for AOA2.0 DynamicQR
        if (ff4JUtils.isFeatureEnabledOnMid(requestData.getMid(), BizConstant.THEIA_AOAMID_TO_PGMID_ENABLED, false)
                && aoaUtils.isAOAMerchant(requestData.getMid())) {
            String mid = aoaUtils.getPgMidForAoaMid(requestData.getMid());
            requestData.setMid(mid);
            LOGGER.info("AOA mid converted to PG mid for AOA2.O : {}", requestData.getMid());
        }

        WorkFlowRequestBean workFlowRequestBean;
        try {
            /**
             * This requestType swapping is done for AOA transaction
             */
            String requestType = requestData.getRequestType();
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
            workFlowRequestBean.setRequestType(ERequestType.getByRequestType(requestType));
        } catch (TheiaDataMappingException e) {
            throw new TheiaServiceException(e);
        }

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }
        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        setTokenInCache(workFlowRequestBean);

        if (workFlowRequestBean.isFromAoaMerchant()) {
            workFlowRequestBean.setPayMethod(EPayMethod.WALLET.getMethod());
        }

        if (dynamicQRUtil.isDynamicQREdcRequest(requestData) || dynamicQRUtil.checkIsEdcRequest(requestData)) {
            checkForRetry(requestData, workFlowRequestBean);
            workFlowRequestBean.setDynamicQREdcRequest(true);
        }

        workFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(requestData.getTxnAmount()));
        String transId = dynamicQRUtil.getTransId(requestData, workFlowRequestBean);

        boolean dynamicQROrderOnAOA = false;
        if (transId == null || StringUtils.isBlank(transId)) {
            String mid = requestData.getMid();
            String orderId = requestData.getOrderId();
            try {
                dynamicQROrderOnAOA = upipspUtil.orderPresentOnAOA(mid, orderId);
                if (dynamicQROrderOnAOA) {
                    workFlowRequestBean.setTxnFlow(AOA_DQR);
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("AOA Order Status Look-up failed: ", e);
            }
        }
        LOGGER.info("DynamicQrFastForwardFlow:TransID : {}", transId);

        workFlowRequestBean.setTransID(transId);
        if (ff4JHelper.isFF4JFeatureForMidEnabled(
                TheiaConstant.ExtraConstants.THEIA_SET_TERMINAL_TYPE_IN_DYNAMIC_QR_FAST_FORWARD, requestData.getMid())) {
            workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.APP);
        } else {
            workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);
        }
        if (!StringUtils.isBlank(requestData.getChannelId())) {
            workFlowRequestBean.setChannelID(requestData.getChannelId());
        } else {
            workFlowRequestBean.setChannelID(workFlowRequestBean.getEnvInfoReqBean().getTerminalType().getTerminal());
        }

        // Hack to process payment request for dynamic QR from APP
        if (StringUtils.equals(AOA_DQR, workFlowRequestBean.getTxnFlow()))
            workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);

        if (StringUtils.isNotBlank(requestData.getAppIp())) {
            workFlowRequestBean.setClientIP(requestData.getAppIp());
            workFlowRequestBean.getEnvInfoReqBean().setClientIp(requestData.getAppIp());
            workFlowRequestBean.getExtendInfo().setClientIP(requestData.getAppIp());
        }
        if (ERequestType.DYNAMIC_QR_2FA == workFlowRequestBean.getRequestType()) {
            String chargeAmount = dynamicQRUtil.parseChargeAmountFromAdditionalInfo(workFlowRequestBean.getExtendInfo()
                    .getAdditionalInfo());
            if (StringUtils.isNotBlank(chargeAmount)) {
                workFlowRequestBean.setChargeAmount(chargeAmount);
                workFlowRequestBean.setTxnAmount(String.valueOf(Integer.parseInt(workFlowRequestBean.getTxnAmount())
                        - Integer.parseInt(workFlowRequestBean.getChargeAmount())));
            }
        }

        setPcfDataForMerchant(workFlowRequestBean);
        LOGGER.info("Modified WorkFlowRequestBean for dynamicQrFastForwardFlow:{}", workFlowRequestBean);

        return processBizWorkFlow(workFlowRequestBean, requestData).getResponse();
    }

    private void setTokenInCache(WorkFlowRequestBean workFlowRequestBean) {
        try {
            String txnToken = workFlowRequestBean.getTxnToken();
            if (!nativeSessionUtil.isExist(txnToken)) {
                nativeSessionUtil.setField(txnToken, "token",
                        workFlowRequestBean.getPaytmMID() + workFlowRequestBean.getOrderID(), 900);
            }
        } catch (Exception e) {
            LOGGER.error("error in setting token in cache for dynamicQrFastForwardFlow");
        }
    }

    private void setPcfDataForMerchant(WorkFlowRequestBean flowRequestBean) {
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(flowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(flowRequestBean.getPaytmMID()) || isSlabBasedMdr || flowRequestBean
                .isDynamicFeeMerchant())) {
            flowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            flowRequestBean.setPostConvenience(true);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (checksumService.validateChecksum(requestData)) {
            return ValidationResults.VALIDATION_SUCCESS;
        }
        return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
    }

    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        throw new TheiaServiceException("This operation is not implemented yet");
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = bizService.processWorkFlow(workFlowRequestBean,
                dynamicQrFastForwardFlow);

        AutoDebitResponse autoDebitResponse = new AutoDebitResponse();
        autoDebitCoreService.setResponseData(bizResponseBean, autoDebitResponse, requestData);
        LOGGER.debug("dynamicQrFastForwardFlow data : {}", autoDebitResponse);
        return autoDebitCoreService.generateAutoDebitResponse(bizResponseBean, autoDebitResponse);
    }

    private String getQrOrderKey(String mid, String orderId) {
        return "QR_ORDER_" + mid + orderId;
    }

    private void checkForRetry(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (!nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
            LOGGER.error("Retry count breached! dynamicQr");
            throw new TheiaServiceException(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage());

        } else {
            nativeRetryUtil.increaseRetryCount(workFlowRequestBean.getTxnToken(), requestData.getMid(),
                    requestData.getOrderId());
        }
    }

    private void validateFetchPaymentOptions(PaymentRequestBean paymentRequestBean) {
        String token = paymentRequestBean.getMid() + paymentRequestBean.getOrderId();
        NativeCashierInfoResponse cashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(token);
        if (cashierInfoResponse == null) {
            return;
        }
        if (cashierInfoResponse.getBody() != null
                && cashierInfoResponse.getBody().getMerchantPayOption() != null
                && !CollectionUtils.isEmpty(cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods())
                && cashierInfoResponse
                        .getBody()
                        .getMerchantPayOption()
                        .getPayMethods()
                        .stream()
                        .filter(p -> {
                            if (("BALANCE".equals(p.getPayMethod()) || EPayMethod.WALLET.getMethod().equals(
                                    p.getPayMethod()))
                                    && (p.getIsDisabled() == null || Boolean.valueOf(p.getIsDisabled().getStatus()) == false)) {
                                return true;
                            } else {
                                return false;
                            }
                        }).count() > 0) {

            return;
        }
        throw new PaymentRequestProcessingException().getException(INVALID_WALLET_PAYMENT_MODE);
    }
}
