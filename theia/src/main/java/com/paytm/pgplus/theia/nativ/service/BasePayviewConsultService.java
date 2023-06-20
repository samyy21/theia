package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.dynamicwrapper.utils.JSONUtils;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.GoodsInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.KybServiceException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.CustomBeanMapper;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.PayviewConsultServiceHelper;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.PRNUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADD_MONEY_SURCHARGE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.FETCH_LRN_DETAILS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;

public abstract class BasePayviewConsultService implements IPayviewConsultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePayviewConsultService.class);

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("customBeanMapper")
    private CustomBeanMapper customBeanMapper;

    @Autowired
    @Qualifier("payviewConsultServiceHelper")
    private PayviewConsultServiceHelper payviewConsultServiceHelper;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    AWSStatsDUtils statsDUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Override
    public void validate(CashierInfoContainerRequest cashierInfoContainerRequest) throws RequestValidationException {
        CashierInfoRequest cashierInfoRequest = cashierInfoContainerRequest.getCashierInfoRequest();
        LOGGER.info("Validating request {} ..", cashierInfoRequest);
        GenericFlowRequestBeanValidator<CashierInfoRequest> validator = new GenericFlowRequestBeanValidator<>(
                cashierInfoRequest);
        ValidationResultBean validationResultBean = validator.validate();
        if (!validationResultBean.isSuccessfullyProcessed()) {
            LOGGER.error("Request validation failed ... {}", validationResultBean);
            LOGGER.error("Error = {}", validator.getErrorMessage());
            LOGGER.error("Property Path = {}", validator.getPropertyPath());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        LOGGER.info("Validation successful");
    }

    @Override
    public WorkFlowResponseBean process(CashierInfoContainerRequest serviceRequest) {
        PaymentRequestBean payviewRequest = requestTransform(serviceRequest);
        WorkFlowResponseBean response = process(serviceRequest.getCashierInfoRequest(), payviewRequest);
        return response;
    }

    private PaymentRequestBean getPaymentRequestBean(CashierInfoContainerRequest cashierInfoContainerRequest,
            HttpServletRequest httpServletRequest) {
        PaymentRequestBean paymentRequestBean = cashierInfoContainerRequest.getPaymentRequestBean();
        if (paymentRequestBean != null && paymentRequestBean.isEnhancedCashierPageRequest()) {

            paymentRequestBean.setCustomizeCode(getCustomizeCode(cashierInfoContainerRequest.getCashierInfoRequest()));
            if (paymentRequestBean.getRequest() == null) {
                paymentRequestBean.setRequest(httpServletRequest);
            }
            if (StringUtils.isBlank(paymentRequestBean.getSsoToken())) {
                paymentRequestBean.setSsoToken((String) httpServletRequest
                        .getAttribute(TheiaConstant.RequestParams.SSO_TOKEN));
            }
        } else {
            paymentRequestBean = new PaymentRequestBean(httpServletRequest, true);
        }
        CashierInfoRequest cashierInfoRequest = cashierInfoContainerRequest.getCashierInfoRequest();
        if (cashierInfoRequest != null && cashierInfoRequest.getBody() != null) {
            paymentRequestBean.setReturnDisabledChannelInFpo(cashierInfoRequest.getBody().isReturnDisabledChannels());
        }
        return paymentRequestBean;
    }

    protected PaymentRequestBean requestTransform(CashierInfoContainerRequest cashierInfoContainerRequest) {
        CashierInfoRequest cashierInfoRequest = cashierInfoContainerRequest.getCashierInfoRequest();

        String requestType = cashierInfoRequest.getBody().getRequestType();

        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        makeBackwardCompatibleHttpServletRequest(httpServletRequest, cashierInfoRequest);
        PaymentRequestBean paymentRequestBean = getPaymentRequestBean(cashierInfoContainerRequest, httpServletRequest);

        paymentRequestBean.setAccountNumber(cashierInfoRequest.getBody().getAccountNumber());
        paymentRequestBean.setValidateAccountNumber(cashierInfoRequest.getBody().getValidateAccountNumber());
        paymentRequestBean.setAllowUnverifiedAccount(cashierInfoRequest.getBody().getAllowUnverifiedAccount());

        if (ObjectUtils.notEqual(cashierInfoRequest.getBody().getUltimateBeneficiaryDetails(), null)
                && ObjectUtils.notEqual(cashierInfoRequest.getBody().getUltimateBeneficiaryDetails()
                        .getUltimateBeneficiaryName(), null)
                && StringUtils.isNotBlank(cashierInfoRequest.getBody().getUltimateBeneficiaryDetails()
                        .getUltimateBeneficiaryName())) {
            UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
            ultimateBeneficiaryDetails.setUltimateBeneficiaryName(cashierInfoRequest.getBody()
                    .getUltimateBeneficiaryDetails().getUltimateBeneficiaryName());
            paymentRequestBean.setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails);
        }

        // hack to support task workflow decision making
        if (paymentRequestBean.getEmiOption() == null) {
            paymentRequestBean.setEmiOption((String) httpServletRequest.getAttribute(EMI_OPTIONS));
        }
        // hack to support task workflow decision making
        paymentRequestBean.setRequestType(ERequestType.NATIVE_PAY.getType());
        paymentRequestBean.setDealsFlow(cashierInfoRequest.getBody().isDealsFlow());

        boolean isSubscription = false;
        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestType)
                && cashierInfoContainerRequest.getCashierInfoRequest().getBody().isSuperGwFpoApiHit()) {
            isSubscription = true;
            paymentRequestBean.setRequestType(ERequestType.NATIVE_SUBSCRIPTION_PAY.getType());
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = cashierInfoRequest.getBody()
                    .getSubscriptionTransactionRequestBody();
            if (subscriptionTransactionRequestBody != null) {
                setSubscriptionDetails(paymentRequestBean, subscriptionTransactionRequestBody);
            }
        } else if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestType)
                || ERequestType.SUBSCRIBE.getType().equals(requestType)) {
            isSubscription = true;
            paymentRequestBean.setRequestType(ERequestType.NATIVE_SUBSCRIPTION_PAY.getType());
        } else if (ERequestType.NATIVE_MF_SIP.getType().equals(requestType)) {
            isSubscription = true;
            paymentRequestBean.setRequestType(ERequestType.NATIVE_MF_SIP_PAY.getType());
        } else if (aoaUtils.isAOAMerchant(cashierInfoRequest.getHead().getMid())) {
            // set UNI_PAY here only for non-subscription use-cases
            paymentRequestBean.setRequestType(ERequestType.UNI_PAY_PAY.getType());
        }

        if (ERequestType.NATIVE_MF.getType().equals(cashierInfoRequest.getBody().getRequestType())) {
            paymentRequestBean.setRequestType(ERequestType.NATIVE_MF_PAY.getType());
        }

        if (ERequestType.NATIVE_ST.getType().equals(cashierInfoRequest.getBody().getRequestType())) {
            paymentRequestBean.setRequestType(ERequestType.NATIVE_ST_PAY.getType());
        }

        paymentRequestBean.setDisabledPaymentMode(getDisabledPaymentModeString(cashierInfoRequest.getBody()
                .getDisabledInstrumentTypes(), paymentRequestBean.getDisabledPaymentMode()));
        if (MapUtils.isNotEmpty(cashierInfoRequest.getBody().getExtendInfo())) {
            paymentRequestBean.getExtraParamsMap().putAll(cashierInfoRequest.getBody().getExtendInfo());
        }
        paymentRequestBean.setSubwalletAmount(cashierInfoRequest.getBody().getSubwalletAmount());
        paymentRequestBean.setSubscription(isSubscription);
        // setting flag for native add money request
        paymentRequestBean.setNativeAddMoney(cashierInfoRequest.getBody().isNativeAddMoney());
        paymentRequestBean.setAppVersion(cashierInfoRequest.getBody().getAppVersion());
        paymentRequestBean.setVersion(cashierInfoRequest.getHead().getVersion());
        paymentRequestBean.setOriginChannel(cashierInfoRequest.getBody().getOriginChannel());

        /*
         * set pwp category
         */
        if (merchantPreferenceService.isPwpEnabled(paymentRequestBean.getMid())) {
            paymentRequestBean.setPwpCategory(payviewConsultServiceHelper.getCategoryForPWPMerchant(paymentRequestBean
                    .getMid()));
        }

        if (StringUtils.isBlank(cashierInfoRequest.getBody().getOrderId())) {
            paymentRequestBean.setSessionRequired(false);
        }
        // setting amount for getting trust factor from wallet consult and
        // passing it to risk for add money
        paymentRequestBean.setAmountForWalletConsultInRisk(cashierInfoRequest.getBody()
                .getAmountForWalletConsultInRisk());
        paymentRequestBean.setAmountForPaymentFlow(cashierInfoRequest.getBody().getAmountForPaymentFlow());

        /*
         * emi Subvention related data
         */
        paymentRequestBean.setItems(cashierInfoRequest.getBody().getItems());
        if (cashierInfoRequest.getBody().getSubventionDetails() != null
                && cashierInfoRequest.getBody().getSubventionDetails().getStrategy() != null) {
            paymentRequestBean.setEmiSubventionStratergy(String.valueOf(cashierInfoRequest.getBody()
                    .getSubventionDetails().getStrategy()));
        }
        paymentRequestBean.setEmiSubventedTransactionAmount(cashierInfoRequest.getBody()
                .getEmiSubventedTransactionAmount());
        paymentRequestBean.setEmiSubventionCustomerId(cashierInfoRequest.getBody().getEmiSubventionCustomerId());
        paymentRequestBean.setEmiSubventionRequired(cashierInfoRequest.getBody().isEmiSubventionRequired());

        paymentRequestBean.setInternalFetchPaymentOptions(cashierInfoRequest.getBody().isInternalFetchPaymentOptions());
        paymentRequestBean.setExternalFetchPaymentOptions(cashierInfoRequest.getBody().isExternalFetchPaymentOptions());
        paymentRequestBean.setProductCode(cashierInfoRequest.getBody().getProductCode());
        paymentRequestBean.setAccessToken(cashierInfoRequest.getBody().getAccessToken());

        if (StringUtils.isNotBlank(cashierInfoRequest.getBody().getCustId())) {
            paymentRequestBean.setCustId(cashierInfoRequest.getBody().getCustId());
        }

        if (cashierInfoRequest.getBody().isMlvSupported()) {
            paymentRequestBean.setMlvSupported(cashierInfoRequest.getBody().isMlvSupported());
            paymentRequestBean.setAppVersion(cashierInfoRequest.getBody().getAppVersion());
            paymentRequestBean.setqRCodeInfo(cashierInfoRequest.getBody().getqRCodeInfo());
        }

        if (StringUtils.isBlank(paymentRequestBean.getWorkflow())) {
            paymentRequestBean.setWorkflow((String) httpServletRequest.getAttribute(WORKFLOW));
        }

        paymentRequestBean.setPreAuth(cashierInfoRequest.getBody().isPreAuth());
        try {
            if (cashierInfoRequest.getBody() != null && cashierInfoRequest.getBody().getGoodsInfo() != null) {
                paymentRequestBean
                        .setGoodsInfo(JsonMapper.mapObjectToJson(cashierInfoRequest.getBody().getGoodsInfo()));
            }
        } catch (FacadeCheckedException ex) {
            LOGGER.error("Exception in converting Goods info");
        }

        if (cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody() != null)
            paymentRequestBean.setAccountNumber(cashierInfoRequest.getBody().getSubscriptionTransactionRequestBody()
                    .getAccountNumber());

        if (paymentRequestBean.isPreAuth()) {
            if (Objects.nonNull(cashierInfoRequest.getBody())
                    && Objects.nonNull(cashierInfoRequest.getBody().getCardPreAuthType())) {
                paymentRequestBean.setCardPreAuthType(cashierInfoRequest.getBody().getCardPreAuthType().name());
            }
            paymentRequestBean.setPreAuthBlockSeconds(cashierInfoRequest.getBody().getPreAuthBlockSeconds());
        }
        paymentRequestBean.setAddNPayOnPostpaidSupported(cashierInfoRequest.getBody().isAddNPayOnPostpaidSupported());
        paymentRequestBean.setAppInvoke(cashierInfoRequest.getBody().isAppInvoke());
        paymentRequestBean.setReturnToken(cashierInfoRequest.getBody().isReturnToken());
        if ((null == cashierInfoRequest.getHead().getTokenType() || TokenType.TXN_TOKEN.equals(cashierInfoRequest
                .getHead().getTokenType())) && cashierInfoRequest.getHead().getToken() != null) {
            paymentRequestBean.setTxnToken(cashierInfoRequest.getHead().getToken());
        }
        setOrderDetails(paymentRequestBean);
        paymentRequestBean.setTwoFADetails(cashierInfoRequest.getBody().getTwoFADetails());
        // TODO: get is LiteEligible from cashierinfoRequest
        paymentRequestBean.setUpiLite(setUPILiteEligible(cashierInfoRequest));
        paymentRequestBean.setSuperGwFpoApiHit(cashierInfoContainerRequest.getCashierInfoRequest().getBody()
                .isSuperGwFpoApiHit());
        return paymentRequestBean;
    }

    private void setSubscriptionDetails(PaymentRequestBean paymentRequestBean,
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        paymentRequestBean.setSubsPPIOnly(subscriptionTransactionRequestBody.getSubsPPIOnly());
        paymentRequestBean.setSubsPaymentMode(subscriptionTransactionRequestBody.getSubscriptionPaymentMode());
        paymentRequestBean.setSubscriptionAmountType(subscriptionTransactionRequestBody.getSubscriptionAmountType());
        paymentRequestBean.setSubscriptionMaxAmount(subscriptionTransactionRequestBody.getSubscriptionMaxAmount());
        paymentRequestBean.setSubscriptionFrequency(subscriptionTransactionRequestBody.getSubscriptionFrequency());
        paymentRequestBean.setSubscriptionFrequencyUnit(subscriptionTransactionRequestBody
                .getSubscriptionFrequencyUnit());
        paymentRequestBean.setSubscriptionExpiryDate(subscriptionTransactionRequestBody.getSubscriptionExpiryDate());
        paymentRequestBean.setSubscriptionEnableRetry(subscriptionTransactionRequestBody.getSubscriptionEnableRetry());
        paymentRequestBean.setSubscriptionGraceDays(subscriptionTransactionRequestBody.getSubscriptionGraceDays());
        paymentRequestBean.setSubscriptionStartDate(subscriptionTransactionRequestBody.getSubscriptionStartDate());
        paymentRequestBean.setSubscriptionRetryCount(subscriptionTransactionRequestBody.getSubscriptionRetryCount());
        paymentRequestBean.setMandateType(subscriptionTransactionRequestBody.getMandateType());
        paymentRequestBean.setAutoRenewal(subscriptionTransactionRequestBody.isAutoRenewal());
        paymentRequestBean.setAutoRetry(subscriptionTransactionRequestBody.isAutoRetry());
        paymentRequestBean.setCommunicationManager(subscriptionTransactionRequestBody.isCommunicationManager());
        paymentRequestBean.setRenewalAmount(subscriptionTransactionRequestBody.getRenewalAmount());
        paymentRequestBean.setSubsGoodsInfo(subscriptionTransactionRequestBody.getSubsGoodsInfo());
        paymentRequestBean.setSubscriptionPurpose(subscriptionTransactionRequestBody.getSubscriptionPurpose());
        paymentRequestBean.setFlexiSubscription(subscriptionTransactionRequestBody.isFlexiSubscription());
        if (ObjectUtils.notEqual(subscriptionTransactionRequestBody.getTxnAmount(), null)) {
            paymentRequestBean.setTxnAmount(subscriptionTransactionRequestBody.getTxnAmount().getValue());
        }
    }

    protected WorkFlowResponseBean process(CashierInfoRequest serviceRequest, PaymentRequestBean payviewRequest) {
        LOGGER.debug("Processing pay view request ...");
        LOGGER.debug("PaymentRequestBean {} ", payviewRequest);

        WorkFlowRequestBean workFlowRequestBean;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(payviewRequest);
            if (serviceRequest != null && serviceRequest.getBody() != null) {
                if (serviceRequest.getBody().getSubscriptionTransactionRequestBody() != null)
                    setSubsSpecificField(serviceRequest, workFlowRequestBean);
            }
            setIfPRNEnabled(payviewRequest, workFlowRequestBean);

        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("Error while getting workflow response bean due to : {}", tdme.getMessage());
            LOGGER.error("Exception stack: ", tdme);
            if (payviewRequest.isCcBillPaymentRequest()) {
                throw PaymentRequestProcessingException.getException(tdme.getResponseConstant());
            } else {
                throw PaymentRequestProcessingException.getException(ExceptionUtils.getMessage(tdme));
            }
        }

        LOGGER.debug("WorkFlowRequestBean  CREATED : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw PaymentRequestProcessingException.getException("WorkFlowRequestBean is null");
        }

        workFlowRequestBean.setFetchPostpaidBalance(merchantPreferenceService.fetchPostpaidBalance(workFlowRequestBean
                .getPaytmMID()));

        // for MDR+PCF Merchant
        if (merchantPreferenceService.isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID())) {
            workFlowRequestBean.setDynamicFeeMerchant(true);
        }

        // For PCF Flow
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        if ((merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())
                || isSlabBasedMdr || workFlowRequestBean.isDynamicFeeMerchant())
                && !(workFlowRequestBean.isNativeAddMoney())) {
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setPostConvenience(true);
            if (merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(workFlowRequestBean.getPaytmMID())
                    && payviewRequest.isEnhancedCashierPageRequest()) {
                workFlowRequestBean.setRequestType(ERequestType.DYNAMIC_QR_2FA);
                workFlowRequestBean.getExtendInfo().setRequestType(ERequestType.DYNAMIC_QR_2FA.getType());
                workFlowRequestBean.getExtendInfo().setProductCode(ProductCodes.ScanNPayChargePayer.getId());
            }
        }

        if (StringUtils.isBlank(payviewRequest.getSsoToken())) {
            if (!workFlowRequestBean.isStoreCardPrefEnabled()) {
                payviewRequest.setShowSavecard(false);
            } else {
                if (StringUtils.isBlank(payviewRequest.getCustId())) {
                    payviewRequest.setShowSavecard(false);
                }
            }
        }

        workFlowRequestBean.setReferenceId(serviceRequest.getBody().getReferenceId());
        // Set static QR code
        workFlowRequestBean.setStaticQrCode(serviceRequest.getBody().isStaticQrCode());
        /*
         * This is done so that outh would not be called again because we have
         * already done it in initiateTxn API
         */
        if (serviceRequest != null && serviceRequest.getBody() != null) {
            if (serviceRequest.getBody().getNativePersistData() != null) {
                workFlowRequestBean.setUserDetailsBiz(serviceRequest.getBody().getNativePersistData().getUserDetails());
            }
            workFlowRequestBean.setAddMoneyFeeAppliedOnWallet(serviceRequest.getBody().isAddMoneyFeeAppliedOnWallet());
            workFlowRequestBean.setInitialAddMoneyAmount(serviceRequest.getBody().getInitialAddMoneyAmount());
            workFlowRequestBean.setFetchPaytmInstrumentsBalance(serviceRequest.getBody()
                    .isFetchPaytmInstrumentsBalance());
        }

        workFlowRequestBean.setAddNPayDisabledForZeroBalance(merchantPreferenceService.isAddnPayDisabledForZeroBalance(
                workFlowRequestBean.getPaytmMID(), false));

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (!bizResponseBean.isSuccessfullyProcessed() || bizResponseBean.getResponse() == null) {
            if (bizResponseBean.getResponseConstant() == ResponseConstants.KYB_NO_RELATION_FOUND
                    || bizResponseBean.getResponseConstant() == ResponseConstants.KYB_NOT_PERMITTED) {
                throw new KybServiceException(bizResponseBean.getResponseConstant());

            }
            LOGGER.info("Workflow processing error {}", bizResponseBean.getFailureDescription());
            statsDUtil.pushException("Workflow processing error " + bizResponseBean.getFailureDescription());
            if (bizResponseBean.getResponseConstant() != null) {
                if (bizResponseBean.getResponseConstant() == ResponseConstants.PAYEE_ACCOUNT_NOT_EXIST_TOPUP
                        || bizResponseBean.getResponseConstant() == ResponseConstants.REPEAT_REQ_INCONSISTENT) {
                    throw PaymentRequestProcessingException
                            .getExceptionWithResultInfoAndResponseConstant(bizResponseBean.getResponseConstant());
                }

                throw PaymentRequestProcessingException.getException(bizResponseBean.getResponseConstant());
            }
            throw PaymentRequestProcessingException.getException(ResponseConstants.INTERNAL_PROCESSING_ERROR
                    .getMessage());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        workFlowResponseBean.setUltimateBeneficiaryDetails(workFlowRequestBean.getUltimateBeneficiaryDetails());

        if ((!merchantPreferenceService.isAddMoneyPcfDisabled(workFlowRequestBean.getPaytmMID(), false))
                && Objects.nonNull(workFlowResponseBean.getUserDetails())
                && StringUtils.isNotBlank(workFlowResponseBean.getUserDetails().getUserId())
                && ff4JUtils.isFeatureEnabledOnMidAndCustIdOrUserId(ENABLE_ADD_MONEY_SURCHARGE,
                        workFlowRequestBean.getPaytmMID(), null, workFlowResponseBean.getUserDetails().getUserId(),
                        false)) {
            workFlowRequestBean.setAddMoneyPcfEnabled(true);
        }

        // Setting extended info in workflowResponseBean for setting
        // extendedInfo in session
        if (ERequestType.NATIVE_PAY.equals(workFlowRequestBean.getRequestType())) {
            workFlowResponseBean.setExtendedInfo(AlipayRequestUtils.getExtendeInfoMap(workFlowRequestBean
                    .getExtendInfo()));
        }
        if (ERequestType.NATIVE_PAY.equals(workFlowRequestBean.getRequestType())
                || workFlowRequestBean.isEnhancedCashierPageRequest()) {
            workFlowResponseBean.setPcfEnabled(workFlowRequestBean.isPostConvenience());
            workFlowResponseBean.setAddMoneyPcfEnabled(workFlowRequestBean.isAddMoneyPcfEnabled());
        }

        workFlowResponseBean.setApiVersion(workFlowRequestBean.getApiVersion());
        if (StringUtils.isEmpty(workFlowResponseBean.getProductCode())) {
            workFlowResponseBean.setProductCode(workFlowRequestBean.getProductCode());
        }
        workFlowResponseBean.setAccessToken(workFlowRequestBean.getAccessToken());
        LOGGER.debug("Processing payment request done successfully");
        LOGGER.debug("WorkFlowResponseBean {}", workFlowResponseBean);
        return workFlowResponseBean;
    }

    private void setIfPRNEnabled(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (!workFlowRequestBean.isNativeAddMoney() && prnUtils.checkIfPRNEnabled(requestData.getMid())) {
            workFlowRequestBean.setPrnEnabled(true);
        }
    }

    private void setSubsSpecificField(CashierInfoRequest requestData, WorkFlowRequestBean workFlowRequestBean) {
        workFlowRequestBean.setSubsRenew(requestData.getBody().getSubscriptionTransactionRequestBody().isAutoRenewal());
        workFlowRequestBean.setSubsRetry(requestData.getBody().getSubscriptionTransactionRequestBody().isAutoRetry());
        workFlowRequestBean.setCommunicationManager(requestData.getBody().getSubscriptionTransactionRequestBody()
                .isCommunicationManager());
    }

    private String getDisabledPaymentModeString(List<InstrumentType> paymentModes, String disabledPaymodes) {
        if (paymentModes == null || paymentModes.isEmpty()) {
            if (StringUtils.isNotBlank(disabledPaymodes) && (disabledPaymodes.contains(InstrumentType.UPI.getType()))) {
                return InstrumentType.UPI.getType();
            } else {
                return null;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        boolean empty = true;
        for (InstrumentType paymentMode : paymentModes) {
            if (InstrumentType.ALL == paymentMode || InstrumentType.NB_TOP5 == paymentMode
                    || InstrumentType.PPBL == paymentMode) {
                LOGGER.warn(paymentMode.getPayMethod() + " is not supported in disabledPaymentModes");
                continue;
            }
            EPayMethod ePayMethod = paymentMode.getEPayMethod();
            if (ePayMethod != null) {
                if (empty) {
                    empty = false;
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append(ePayMethod.getOldName());
            }
        }
        return stringBuilder.toString();
    }

    private boolean isMidForTaskFlowNative(String mid) {
        String nativeTaskFlowMids = "ALL";
        return TaskFlowUtils.isMidEligibleForTaskFlow(mid, nativeTaskFlowMids);
    }

    private boolean isMidForTaskFlowNativeEnhanced(String mid) {
        String nativeEnhanceTaskFlowMids = "ALL";
        return TaskFlowUtils.isMidEligibleForTaskFlow(mid, nativeEnhanceTaskFlowMids);
    }

    private boolean runParallelizationForEnhancedOrNative(WorkFlowRequestBean workFlowRequestBean) {

        boolean isEnhancedCashierPageRequest = workFlowRequestBean.isEnhancedCashierPageRequest();
        boolean isParallelizationFlow = false;
        if (isEnhancedCashierPageRequest) {
            if (isMidForTaskFlowNativeEnhanced(workFlowRequestBean.getPaytmMID())) {
                LOGGER.info("RUN PARALLELIZATION FOR ENHANCED OR NATIVE APP INVOKE");
                isParallelizationFlow = true;
            }
        } else {
            if (isMidForTaskFlowNative(workFlowRequestBean.getPaytmMID())) {
                LOGGER.info("RUN PARALLELIZATION FOR NATIVE");
                isParallelizationFlow = true;
            }
        }
        return isParallelizationFlow;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {

        if (runParallelizationForEnhancedOrNative(workFlowRequestBean)) {
            return taskExecutor.execute(workFlowRequestBean);
        }
        return bizService.processWorkFlow(workFlowRequestBean, fetchWorkflow(workFlowRequestBean));
    }

    public abstract IWorkFlow fetchWorkflow(WorkFlowRequestBean workFlowRequestBean);

    /*
     * TODO, Add request Type as AOA's
     */
    protected void makeBackwardCompatibleHttpServletRequest(HttpServletRequest httpServletRequest,
            CashierInfoRequest cashierInfoRequest) {
        RequestHeader requestHeader = cashierInfoRequest.getHead();
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoRequest.getBody();
        httpServletRequest.setAttribute(MID, requestHeader.getMid());
        httpServletRequest.setAttribute(INDUSTRY_TYPE_ID, cashierInfoRequestBody.getIndustryTypeId());
        httpServletRequest.setAttribute(ORDER_ID, cashierInfoRequest.getBody().getOrderId());
        httpServletRequest.setAttribute(REQUEST_TYPE, TheiaConstant.RequestTypes.DEFAULT);
        httpServletRequest.setAttribute(DEVICE_ID, cashierInfoRequestBody.getDeviceId());
        httpServletRequest.setAttribute(CHANNEL_ID, cashierInfoRequestBody.getChannelId().getValue());
        httpServletRequest.setAttribute(CUSTOMIZE_CODE, getCustomizeCode(cashierInfoRequest));
        if (null != cashierInfoRequestBody.getOrderAmount()) {
            httpServletRequest.setAttribute(TXN_AMOUNT, cashierInfoRequestBody.getOrderAmount().getValue());
        }
        if (null != cashierInfoRequestBody.getCustId()) {
            httpServletRequest.setAttribute(TheiaConstant.RequestParams.CUST_ID, cashierInfoRequestBody.getCustId());
        }
        if (null != cashierInfoRequestBody.getEmiOption()) {
            httpServletRequest.setAttribute(EMI_OPTIONS, cashierInfoRequestBody.getEmiOption());
        }
        if (null != cashierInfoRequestBody.getSubscriptionPaymentMode()) {
            httpServletRequest.setAttribute(SUBS_PAYMENT_MODE, cashierInfoRequestBody.getSubscriptionPaymentMode());
        }
        httpServletRequest.setAttribute(POSTPAID_ONBOARDING_SUPPORTED,
                cashierInfoRequestBody.isPostpaidOnboardingSupported());
        httpServletRequest.setAttribute(GOODS_INFO, toJsonString(cashierInfoRequestBody.getGoodsInfo()));
    }

    private String getCustomizeCode(CashierInfoRequest cashierInfoRequest) {
        // TODO: prepare customize code based on instrument type and saved
        // instrument type
        String costomizeCode = "";
        return costomizeCode;
    }

    private String toJsonString(List<GoodsInfo> goodsInfoList) {
        if (CollectionUtils.isEmpty(goodsInfoList)) {
            return null;
        }
        try {
            return JSONUtils.toJson(goodsInfoList);
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private boolean setUPILiteEligible(CashierInfoRequest cashierInfoRequest) {
        return cashierInfoRequest.getBody().isUpiLiteEligible()
                && merchantPreferenceService.isFullPg2TrafficEnabled(cashierInfoRequest.getHead().getMid())
                && !merchantPreferenceService.isUpiLiteDisabled(cashierInfoRequest.getHead().getMid(), false)
                && ff4JUtils.isFeatureEnabledOnMid(cashierInfoRequest.getHead().getMid(), FETCH_LRN_DETAILS, false);

    }

    private void setOrderDetails(PaymentRequestBean requestData) {
        try {
            if ((StringUtils.isEmpty(requestData.getOrderDetails()) || !JsonMapper.isValidJsonString(requestData
                    .getOrderDetails())) && requestData.getTxnToken() != null) {
                InitiateTransactionRequestBody orderDetail = nativeSessionUtil
                        .getOrderDetail(requestData.getTxnToken());
                requestData.setOrderDetails(JsonMapper.mapObjectToJson(orderDetail));
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error while fetching OrderDetails from redis");
        }
    }
}