package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.aoatimeoutcenter.helper.AoaTimeoutCenterServiceHelper;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.link.EdcEmiBankOfferDetails;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.model.link.PaymentFormDetails;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.*;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByAcquirementIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByAcquirementIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.models.CCBillPayment;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.MerchantRedirectRequestException;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.LinkPaymentUtil;
import com.paytm.pgplus.theia.utils.PRNUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.IMEI_KEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MID_BLOCKED_CREATE_ORDER_INITIATE_TXN_API;

@Service("nativeInitiateUtil")
public class NativeInitiateUtil implements Serializable {

    private static final long serialVersionUID = -2119816182763459643L;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private BizRequestResponseMapperImpl bizRequestResponseMapper;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    @Qualifier("bizService")
    private IBizService bizService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeCreateOrderFlow")
    private IWorkFlow nativeCreateOrderFlow;

    @Autowired
    @Qualifier("nativeCreateTopupFlow")
    private IWorkFlow nativeCreateTopupFlow;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private IAcquiringOrder acquiringOrder;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    private AoaTimeoutCenterServiceHelper aoaTimeoutCenterServiceHelper;

    @Autowired
    private FF4JUtil ff4JUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeInitiateUtil.class);

    public void createOrder(InitiateTransactionResponse initiateTransactionResponse,
            PaymentRequestBean paymentRequestBean) throws FacadeCheckedException {
        createOrder(initiateTransactionResponse, paymentRequestBean, null);
    }

    public void createOrder(InitiateTransactionResponse initiateTransactionResponse,
            PaymentRequestBean paymentRequestBean, String paymentPromoCheckoutDataPromoCode)
            throws FacadeCheckedException {

        String txnToken = initiateTransactionResponse.getBody().getTxnToken();

        WorkFlowRequestBean workFlowRequestBean = createWorkFlowBeanFromPaymentReqBean(paymentRequestBean);
        workFlowRequestBean.setPaymentPromoCheckoutDataPromoCode(paymentPromoCheckoutDataPromoCode);
        // Setting the flag that determine whether to show store save card
        // option in Enhanced Native.
        if (StringUtils.isBlank(paymentRequestBean.getSsoToken())) {
            if (!workFlowRequestBean.isStoreCardPrefEnabled()) {
                paymentRequestBean.setShowSavecard(false);
            } else {
                if (StringUtils.isBlank(paymentRequestBean.getCustId())) {
                    paymentRequestBean.setShowSavecard(false);
                }
            }
        }

        workFlowRequestBean.setPostConvenience(merchantPreferenceService
                .isPostConvenienceFeesEnabled(paymentRequestBean.getMid()));
        setUserDetailWorkFlowRequestBean(workFlowRequestBean, initiateTransactionResponse, paymentRequestBean);

        if (org.apache.commons.lang.StringUtils.isNotEmpty(workFlowRequestBean.getExtendInfo()
                .getMerchantUniqueReference())
                && (workFlowRequestBean.getExtendInfo().getMerchantUniqueReference().startsWith("LI_") || workFlowRequestBean
                        .getExtendInfo().getMerchantUniqueReference().startsWith("INV_"))) {
            LOGGER.info("Setting Link Based params in create order");
            UserDetailsBiz userDetailsBiz = workFlowRequestBean.getUserDetailsBiz();
            if (userDetailsBiz != null) {
                workFlowRequestBean.getExtendInfo().setUserEmail(userDetailsBiz.getEmail());
                workFlowRequestBean.getExtendInfo().setUserMobile(userDetailsBiz.getMobileNo());
                workFlowRequestBean.getExtendInfo().setPhoneNo(userDetailsBiz.getMobileNo());
                workFlowRequestBean.getExtendInfo().setEmail(userDetailsBiz.getEmail());
            }

            workFlowRequestBean.getExtendInfo().setLinkNotes(paymentRequestBean.getLinkNotes());
            populateEdcEmiPaymentInfo(workFlowRequestBean.getExtendInfo(), workFlowRequestBean);
            populateValidationModelInfo(workFlowRequestBean.getExtendInfo(), workFlowRequestBean);
            /* For Payment Forms , populate extended Info with form details */
            if (StringUtils.isNotEmpty(paymentRequestBean.getPaymentFormId())) {
                LOGGER.info("Setting payment form in ExtendedInfo for link based payments");
                PaymentFormDetails paymentFormDetails;
                paymentFormDetails = linkPaymentUtil.getPaymentFormDetails(paymentRequestBean);
                if (paymentFormDetails == null && paymentRequestBean.getLinkDetailsData() != null) {
                    paymentFormDetails = paymentRequestBean.getLinkDetailsData().getPaymentFormDetails();
                }
                if (paymentFormDetails == null) {
                    LOGGER.info("Payment form not found. Apparently, redis key expired. Sending failure.");
                    throw new PaymentRequestValidationException("Payment form not found",
                            ResponseConstants.SESSION_EXPIRY);
                }

                try {
                    workFlowRequestBean.getExtendInfo().setPaymentForms(
                            JsonMapper.mapObjectToJson(paymentFormDetails.getPaymentForm()));
                } catch (FacadeCheckedException e) {
                    LOGGER.error("IOException occurred for setting paymentFormDetails in extendInfo", e);
                }
                /*
                 * For Skip Login Flow , populate user details from User filled
                 * form details
                 */
                if (paymentFormDetails.getSkipLoginEnabled() != null && paymentFormDetails.getSkipLoginEnabled()
                        && userDetailsBiz == null) {
                    workFlowRequestBean.getExtendInfo().setUserEmail(paymentFormDetails.getEmailId());
                    workFlowRequestBean.getExtendInfo().setUserMobile(paymentFormDetails.getMobileNo());
                    workFlowRequestBean.getExtendInfo().setPhoneNo(paymentFormDetails.getMobileNo());
                    workFlowRequestBean.getExtendInfo().setEmail(paymentFormDetails.getEmailId());
                    workFlowRequestBean.getExtendInfo().setCustomerName(paymentFormDetails.getCustomerName());
                }
            }
            LOGGER.info("Link based params added here: {}", workFlowRequestBean);
        }

        workFlowRequestBean
                .setCreateOrderForInitiateTxnRequest(paymentRequestBean.isCreateOrderForInitiateTxnRequest());
        workFlowRequestBean.setTxnToken(txnToken);
        GenericCoreResponseBean<WorkFlowResponseBean> createOrderResponse = bizService.processWorkFlow(
                workFlowRequestBean, nativeCreateOrderFlow);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            if (ResponseConstants.SUCCESS_IDEMPOTENT_ERROR.equals(createOrderResponse.getResponseConstant())) {
                String acquirementId = createOrderResponse.getAcquirementId();
                StatusDetail statusDetail = getStatusDetail(workFlowRequestBean, acquirementId);
                if (AcquirementStatusType.INIT.equals(statusDetail.getAcquirementStatus())) {
                    LOGGER.info("Order state : {}", statusDetail.getAcquirementStatus());
                    setAcquirementIdToCache(txnToken, createOrderResponse.getAcquirementId());
                    return;
                }
            }
            throw new NativeFlowException.ExceptionBuilder(createOrderResponse.getResponseConstant()).isHTMLResponse(
                    true).build();

        } else {
            setAcquirementIdToCache(txnToken, createOrderResponse.getResponse().getTransID());
            if (ERequestType.isSubscriptionCreationRequest(paymentRequestBean.getRequestType())) {
                setDummyPamentMidToCache(createOrderResponse.getResponse().getTransID(),
                        paymentRequestBean.getPaymentMid());
            }

            if (workFlowRequestBean.isFromAoaMerchant()
                    && StringUtils.isNotBlank(workFlowRequestBean.getPaytmMID())
                    && ff4JUtil.isFeatureEnabled(PERSIST_ORDERINFO_AT_AOATIMEOUTCENTER,
                            workFlowRequestBean.getPaytmMID())) {
                try {
                    aoaTimeoutCenterServiceHelper.persistOrderInfoAtAoaTimeoutCenter(workFlowRequestBean,
                            createOrderResponse.getResponse().getTransID());
                } catch (FacadeCheckedException e) {
                    LOGGER.error("Error while calling aoa timeout center");
                }
            }
        }
    }

    private StatusDetail getStatusDetail(WorkFlowRequestBean workFlowRequestBean, String acquirementId)
            throws FacadeCheckedException {
        QueryByAcquirementIdRequestBody orderQueryRequestBody = new QueryByAcquirementIdRequestBody(
                workFlowRequestBean.getAlipayMID(), acquirementId, false);
        orderQueryRequestBody.setRoute(Routes.PG2);
        orderQueryRequestBody.setPaytmMerchantId(workFlowRequestBean.getPaytmMID());
        QueryByAcquirementIdRequest OrderQueryRequest = new QueryByAcquirementIdRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_ACQUIREMENTID), orderQueryRequestBody);
        QueryByAcquirementIdResponse queryByAcquirementIdResponse = acquiringOrder
                .queryByAcquirementId(OrderQueryRequest);
        StatusDetail statusDetail = queryByAcquirementIdResponse.getBody().getStatusDetail();
        return statusDetail;

    }

    public void createTopup(String txnToken, PaymentRequestBean paymentRequestBean) {

        WorkFlowRequestBean workFlowRequestBean = createWorkFlowBeanFromPaymentReqBean(paymentRequestBean);

        GenericCoreResponseBean<WorkFlowResponseBean> createTopupResponse = bizService.processWorkFlow(
                workFlowRequestBean, nativeCreateTopupFlow);
        if (!createTopupResponse.isSuccessfullyProcessed()) {
            throw new NativeFlowException.ExceptionBuilder(createTopupResponse.getResponseConstant()).isHTMLResponse(
                    true).build();

        } else {
            setTransIdToCache(txnToken, createTopupResponse.getResponse().getTransID());
        }
    }

    private void setAcquirementIdToCache(String txnToken, String acqId) {
        if (StringUtils.isNotBlank(txnToken) && StringUtils.isNotBlank(acqId)) {
            nativeSessionUtil.setTxnId(txnToken, acqId);
        }
    }

    private void setDummyPamentMidToCache(String acqId, String paymentMid) {
        if (StringUtils.isNotBlank(acqId) && StringUtils.isNotBlank(paymentMid)) {
            nativeSessionUtil.setFieldAndkey(acqId,
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.PAYMENT_MID, paymentMid);
        }
    }

    private void setTransIdToCache(String txnToken, String transId) {
        if (StringUtils.isNotBlank(txnToken) && StringUtils.isNotBlank(transId)) {
            nativeSessionUtil.setTxnId(txnToken, transId);
        }
    }

    private WorkFlowRequestBean createWorkFlowBeanFromPaymentReqBean(PaymentRequestBean paymentRequestBean) {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {

            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(paymentRequestBean);
            setIfPRNEnabled(paymentRequestBean, workFlowRequestBean);
            LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

            if (workFlowRequestBean == null) {
                throw MerchantRedirectRequestException.getException("WorkFlowRequestBean is null");
            }
        } catch (TheiaDataMappingException e) {
            LOGGER.error("Exception while mapping PaymentRequestBean data to WorkflowRequestBean - {}", e.getMessage());
            throw MerchantRedirectRequestException.getException();
        }
        return workFlowRequestBean;
    }

    private void setIfPRNEnabled(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (!workFlowRequestBean.isNativeAddMoney() && prnUtils.checkIfPRNEnabled(requestData.getMid())) {
            workFlowRequestBean.setPrnEnabled(true);
        }
    }

    public InitiateTransactionRequestBody fetchInitiateReqBodyFromCache(final String txnToken) {
        return nativeValidationService.validateTxnToken(txnToken);
    }

    public void validateMidOrderIdAppInvoke(final String mid, final String orderId) {
        try {
            nativeValidationService.validateMidOrderId(mid, orderId);
        } catch (MidDoesnotMatchException midException) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.MID_DOES_NOT_MATCH).isHTMLResponse(true).build();
        } catch (OrderIdDoesnotMatchException oidException) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.ORDER_ID_DOES_NOT_MATCH).isHTMLResponse(true)
                    .build();
        }
    }

    public void transformInitReqBodyToPaymentReqBean(final InitiateTransactionRequestBody initTxnReqBody,
            PaymentRequestBean paymentRequestBean) throws Exception {

        paymentRequestBean.setMid(initTxnReqBody.getMid());
        paymentRequestBean.setOrderId(initTxnReqBody.getOrderId());
        paymentRequestBean.setSsoToken(initTxnReqBody.getPaytmSsoToken());

        paymentRequestBean.setWebsite(initTxnReqBody.getWebsiteName());
        paymentRequestBean.setAuthMode("3D");
        paymentRequestBean.setCallbackUrl(initTxnReqBody.getCallbackUrl());

        /*
         * set Txn amount
         */
        if (initTxnReqBody.getTxnAmount() != null) {
            paymentRequestBean.setTxnAmount(initTxnReqBody.getTxnAmount().getValue());
        }

        /*
         * set UserInfo details
         */
        if (initTxnReqBody.getUserInfo() != null) {
            paymentRequestBean.setCustId(initTxnReqBody.getUserInfo().getCustId());
            paymentRequestBean.setEmail(initTxnReqBody.getUserInfo().getEmail());
            paymentRequestBean.setMobileNo(initTxnReqBody.getUserInfo().getMobile());
            paymentRequestBean.setMsisdn(initTxnReqBody.getUserInfo().getMobile());
            paymentRequestBean.setAddress1(initTxnReqBody.getUserInfo().getAddress());
            paymentRequestBean.setPincode(initTxnReqBody.getUserInfo().getPincode());
            paymentRequestBean.setUserInfo(initTxnReqBody.getUserInfo());
        }

        paymentRequestBean.setPromoCampId(initTxnReqBody.getPromoCode());
        paymentRequestBean.setEmiOption(initTxnReqBody.getEmiOption());

        /*
         * set extendedInfo details
         */
        if (initTxnReqBody.getExtendInfo() != null) {
            paymentRequestBean.setUdf1(initTxnReqBody.getExtendInfo().getUdf1());
            paymentRequestBean.setUdf2(initTxnReqBody.getExtendInfo().getUdf2());
            paymentRequestBean.setUdf3(initTxnReqBody.getExtendInfo().getUdf3());
            paymentRequestBean.setMerchUniqueReference(initTxnReqBody.getExtendInfo().getMercUnqRef());
            paymentRequestBean.setComments(initTxnReqBody.getExtendInfo().getComments());
            paymentRequestBean.setAdditionalInfo(initTxnReqBody.getExtendInfo().getComments());
            paymentRequestBean.setSubwalletAmount(initTxnReqBody.getExtendInfo().getSubwalletAmount());
            paymentRequestBean.setExtendInfo(initTxnReqBody.getExtendInfo());
        }

        paymentRequestBean.setExtraParamsMap(initTxnReqBody.getExtraParamsMap());

        paymentRequestBean.setGoodsInfo(JsonMapper.mapObjectToJson(initTxnReqBody.getGoods()));
        paymentRequestBean.setShippingInfo(JsonMapper.mapObjectToJson(initTxnReqBody.getShippingInfo()));
        paymentRequestBean.setAdditionalInfoMF(JsonMapper.mapObjectToJson(initTxnReqBody.getAdditionalInfo()));

        paymentRequestBean.setIndustryTypeId(getIndustryTypeId(paymentRequestBean.getMid()));

        /*
         * set CC Bill payment Data
         */
        setCCBillPaymentDetailsToPaymentReqBean(initTxnReqBody, paymentRequestBean);

        if (StringUtils.isNotBlank(initTxnReqBody.getAggMid())) {
            paymentRequestBean.setAggMid(initTxnReqBody.getAggMid());
        }

        if (StringUtils.isNotBlank(initTxnReqBody.getPEON_URL())) {
            paymentRequestBean.setPeonURL(initTxnReqBody.getPEON_URL());
        }
        if (initTxnReqBody.getSplitSettlementInfoData() != null) {
            paymentRequestBean.setSplitSettlementInfoData(initTxnReqBody.getSplitSettlementInfoData());
        }
        paymentRequestBean.setNativeAddMoney(initTxnReqBody.isNativeAddMoney());
        paymentRequestBean.setAggType(initTxnReqBody.getAggType());
        paymentRequestBean.setOrderPricingInfo(initTxnReqBody.getOrderPricingInfo());
        paymentRequestBean.setOfflineTxnFlow(initTxnReqBody.isOfflineFlow());
        paymentRequestBean.setPreAuth(TxnType.AUTH.equals(initTxnReqBody.getTxnType())
                || TxnType.ESCROW.equals(initTxnReqBody.getTxnType()));
        LinkDetailResponseBody linkDetailResponseBody = initTxnReqBody.getLinkDetailsData();
        if (linkDetailResponseBody != null) {
            paymentRequestBean.setUserInfo(initTxnReqBody.getUserInfo());
            paymentRequestBean.setInvoiceId(initTxnReqBody.getLinkDetailsData().getInvoiceId());
            paymentRequestBean.setAccountNumber(initTxnReqBody.getAccountNumber());
            paymentRequestBean.setLinkId(linkDetailResponseBody.getLinkId());
            paymentRequestBean.setLinkName(linkDetailResponseBody.getLinkName());
            paymentRequestBean.setLinkDescription(linkDetailResponseBody.getLinkDescription());
            paymentRequestBean.setLinkDetailsData(linkDetailResponseBody);
            paymentRequestBean.setLongUrl(linkDetailResponseBody.getLongUrl());
            paymentRequestBean.setResellerId(linkDetailResponseBody.getResellerId());
            paymentRequestBean.setResellerName(linkDetailResponseBody.getResellerName());
            paymentRequestBean.setMerchantLinkRefId(linkDetailResponseBody.getMerchantReferenceId());
            paymentRequestBean.setPaymentFormId(linkDetailResponseBody.getPaymentFormId());
            paymentRequestBean.setLinkNotes(linkDetailResponseBody.getLinkNotes());
            paymentRequestBean.setDisplayWarningMessageForLink(linkDetailResponseBody.isDisplayWarningMessage());
            paymentRequestBean.setLinkOrderExtendInfo(linkDetailResponseBody.getExtendInfo());
            if (linkDetailResponseBody.getSplitSettlementInfo() != null) {
                paymentRequestBean.setSplitSettlementInfoData(JsonMapper.convertValue(
                        linkDetailResponseBody.getSplitSettlementInfo(), SplitSettlementInfoData.class));
            }
        }
        LOGGER.info("Transformed InitiateTransactionRequestBody to PaymentRequestBean : {}", paymentRequestBean);
    }

    public void setCCBillPaymentDetailsToPaymentReqBean(InitiateTransactionRequestBody orderDetail,
            PaymentRequestBean paymentRequestBean) {
        CCBillPayment ccBillPayment = orderDetail.getCcBillPayment();
        if (ccBillPayment != null && StringUtils.isNotBlank(ccBillPayment.getCcBillNo())) {
            LOGGER.info("CC bill payment request received");
            paymentRequestBean.setCcBillPaymentRequest(true);
            paymentRequestBean.setCreditCardBillNo(ccBillPayment.getCcBillNo());
        }
    }

    public String getIndustryTypeId(String mid) {
        return processTransactionUtil.getIndustryTypeId(mid);
    }

    private void setUserDetailWorkFlowRequestBean(WorkFlowRequestBean workFlowRequestBean,
            InitiateTransactionResponse initiateTransactionResponse, PaymentRequestBean paymentRequestBean) {
        if (initiateTransactionResponse.getBody().isAuthenticated()) {
            /*
             * This redis call could be avoided by passing NativePersistData in
             * /initiateTxn API response, but... yeah!
             */
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(initiateTransactionResponse
                    .getBody().getTxnToken());
            InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
            if (orderDetail != null && nativeInitiateRequest.getNativePersistData() != null) {
                workFlowRequestBean.setUserDetailsBiz(nativeInitiateRequest.getNativePersistData().getUserDetails());

                /*
                 * This is done because userName is to be sent to UI push-app
                 * data
                 */
                if (nativeInitiateRequest.getNativePersistData().getUserDetails() != null) {
                    paymentRequestBean.setUserName(nativeInitiateRequest.getNativePersistData().getUserDetails()
                            .getUserName());
                    paymentRequestBean.setMobileNo(nativeInitiateRequest.getNativePersistData().getUserDetails()
                            .getMobileNo());
                }
            }
        }
    }

    public boolean isMidBlockedCreateOrderInitiateTxnApi(String mid) {
        String midsBlocked = ConfigurationUtil.getProperty(MID_BLOCKED_CREATE_ORDER_INITIATE_TXN_API);
        if (midsBlocked == null) {
            return false;
        }
        return StringUtils.indexOf(midsBlocked, mid) >= 0;
    }

    private void populateEdcEmiPaymentInfo(ExtendedInfoRequestBean extendInfo, WorkFlowRequestBean flowRequestBean)
            throws FacadeCheckedException {

        if (flowRequestBean.getPaymentRequestBean() != null
                && flowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
            final EdcEmiDetails edcEmiFields = flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getEdcEmiFields();
            if (edcEmiFields != null) {
                List<EdcEmiBankOfferDetails> bankOffer = edcEmiFields.getEmiChannelDetail().getBankOfferDetails();
                if (CollectionUtils.isNotEmpty(bankOffer))
                    extendInfo.setBankOffer(JsonMapper.mapObjectToJson(bankOffer));

                extendInfo.setEmiSubventionOrderList(edcEmiFields.getProductId());
                extendInfo.setPaytmTid(flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                        .getMerchantReferenceId());
                extendInfo.setBrandName(edcEmiFields.getBrandName());
                extendInfo.setBrandId(edcEmiFields.getBrandId());
                extendInfo.setCategoryName(edcEmiFields.getCategoryName());
                extendInfo.setCategoryId(edcEmiFields.getCategoryId());
                extendInfo.setProductId(edcEmiFields.getProductId());
                extendInfo.setProductName(edcEmiFields.getProductName());
                extendInfo.setImeiKey(IMEI_KEY);
                if (org.apache.commons.lang.StringUtils.isNotBlank(edcEmiFields.getValidationKey())) {
                    extendInfo.setImei(edcEmiFields.getValidationValue());
                    extendInfo.setSerialNo(edcEmiFields.getValidationValue());
                }
                extendInfo.setEmiPlanId(edcEmiFields.getEmiChannelDetail().getPgPlanId());
                extendInfo.setEmiTenure(edcEmiFields.getEmiChannelDetail().getEmiMonths());
                extendInfo.setUserMobile(flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                        .getCustomerMobile());
                extendInfo.setEmiInterestRate(edcEmiFields.getEmiChannelDetail().getInterestRate());
                extendInfo.setEmiTotalAmount(edcEmiFields.getEmiChannelDetail().getTotalAmount().getValue());
                extendInfo.setBrandEmiAmount(edcEmiFields.getEmiChannelDetail().getEmiAmount().getValue());
                extendInfo.setEdcLinkTxn(true);
            }
        }
    }

    private void populateValidationModelInfo(ExtendedInfoRequestBean extendInfo, WorkFlowRequestBean flowRequestBean) {
        try {
            if (flowRequestBean.getPaymentRequestBean() != null
                    && flowRequestBean.getPaymentRequestBean().getLinkDetailsData() != null) {
                final EdcEmiDetails edcEmiFields = flowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                        .getEdcEmiFields();
                if (edcEmiFields != null) {
                    ValidationModelInfo validationModelInfo = new ValidationModelInfo();

                    validationModelInfo.setClientInfo(populateClientInfo(edcEmiFields, flowRequestBean));
                    validationModelInfo.setProductInfo(populateProductInfo(edcEmiFields));
                    validationModelInfo.setSerialInfo(populateSerialInfo(edcEmiFields));
                    validationModelInfo
                            .setTransactionInfo(populateEdcLinkTransactionInfo(edcEmiFields, flowRequestBean));
                    validationModelInfo
                            .setAdditionalDetails(populateEdcAdditionalDetails(edcEmiFields, flowRequestBean));
                    extendInfo.setValidationMode(edcEmiFields.getValidationMode());
                    extendInfo.setValidationModelInfo(JsonMapper.mapObjectToJson(validationModelInfo));
                    if (edcEmiFields.isValidationSkipFlag()) {
                        extendInfo.setValidationSkipFlag(true);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Exception while setting ValidationModelInfo {}", e.getMessage());
        }
    }

    private SerialInfo populateSerialInfo(EdcEmiDetails edcEmiFields) {
        SerialInfo serialInfo = new SerialInfo();
        serialInfo.setId(edcEmiFields.getValidationValue());
        serialInfo.setType(edcEmiFields.getValidationKey());
        return serialInfo;
    }

    private ProductInfo populateProductInfo(EdcEmiDetails edcEmiFields) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setBrandId(edcEmiFields.getBrandId());
        productInfo.setCategoryId(edcEmiFields.getCategoryId());
        productInfo.setProductId(edcEmiFields.getProductId());
        productInfo.setSkuCode(edcEmiFields.getSkuCode());
        return productInfo;
    }

    private ClientInfo populateClientInfo(EdcEmiDetails edcEmiFields, WorkFlowRequestBean flowRequestBean) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setKybId(edcEmiFields.getKybId());
        clientInfo.setSourceContext(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.SOURCE_CONTEXT);
        return clientInfo;
    }

    private EdcLinkTransactionInfo populateEdcLinkTransactionInfo(EdcEmiDetails edcEmiFields,
            WorkFlowRequestBean flowRequestBean) {
        EdcLinkTransactionInfo transactionInfo = new EdcLinkTransactionInfo();
        String paymentType = null;
        transactionInfo.setTxnAmount(flowRequestBean.getTxnAmount());
        if (edcEmiFields.getEmiChannelDetail().getEmiMonths() != null
                && Integer.valueOf(edcEmiFields.getEmiChannelDetail().getEmiMonths()) > 0)
            paymentType = com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_EMI;
        else
            paymentType = com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PAYMENT_TYPE_BANK_OFFER;

        EdcLinkPaymentMethod.ExtraInfo extraInfo = new EdcLinkPaymentMethod.ExtraInfo(edcEmiFields.getBankCode(),
                edcEmiFields.getEmiChannelDetail().getEmiMonths());
        List<EdcLinkPaymentMethod> edcLinkPaymentMethods = Arrays.asList(new EdcLinkPaymentMethod(paymentType,
                extraInfo));
        transactionInfo.setPaymentMethod(edcLinkPaymentMethods);
        return transactionInfo;
    }

    private Map<String, String> populateEdcAdditionalDetails(EdcEmiDetails edcEmiFields,
            WorkFlowRequestBean flowRequestBean) {
        Map<String, String> additionalDetails = new HashMap<>();
        if (edcEmiFields != null && flowRequestBean != null) {
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.ORDERID, flowRequestBean.getOrderID());
            additionalDetails
                    .put(TheiaConstant.EdcEmiAdditionalFields.INVOICE_NO, edcEmiFields.getBrandInvoiceNumber());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TXN_AMOUNT, flowRequestBean.getTxnAmount());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.LOAN_AMOUNT, edcEmiFields.getLoanAmount());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.CARD_TYPE, edcEmiFields.getCardType());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.MID, flowRequestBean.getPaytmMID());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TID, flowRequestBean.getPaymentRequestBean()
                    .getLinkDetailsData().getMerchantReferenceId());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.ISSUER_BANK, edcEmiFields.getBankName());
            additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.IS_APPLE_EXCHANGE_SUPPORTED, "true");
            if (edcEmiFields.getEmiChannelDetail() != null) {
                additionalDetails.put(TheiaConstant.EdcEmiAdditionalFields.TENURE, edcEmiFields.getEmiChannelDetail()
                        .getEmiMonths());
            }
        }
        return additionalDetails;
    }
}
