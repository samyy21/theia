package com.paytm.pgplus.theia.workflow;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.common.enums.MandateAuthMode;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.acquiring.models.request.OrderModifyRequest;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.upi.IUPIAccountService;
import com.paytm.pgplus.facade.upi.model.upiRequest.AccountDetailsByAcoountIdResquest;
import com.paytm.pgplus.facade.upi.model.upiResponse.AccountDetailsByAcoountIdResponse;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.payloadvault.subscription.enums.AccountType;
import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.exceptions.MandateException;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.RouterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_URL_INFO_WEBSITE_FOR_BM;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MANDATE_ORDER_TIMEOUT_FOR_UPFRONT_PAYMENT;

/**
 * mandate service responsible for --> creation and validation of mandate. -->
 * processing of the response received after authentication on destination bank.
 */

@Service("bmService")
public class BMService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BMService.class);

    @Autowired
    @Qualifier("createBmService")
    private CreateBmService createBmService;

    @Autowired
    @Qualifier("processedBmService")
    private ProcessedBmService processedBMService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantResponseService merchantResponseService;

    @Autowired
    private IBankInfoDataService bankInfoDataService;

    @Autowired
    @Qualifier("GetUPIAccountService")
    IUPIAccountService getUPIAccountService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderServiceImpl;

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    @Autowired
    private RouterUtil routerUtil;

    /**
     * creation and validation of mandate.
     * 
     * @param requestBean
     * @return
     */
    public PageDetailsResponse createBM(final PaymentRequestBean requestBean) {
        validateRequest(requestBean);
        try {
            BankMasterDetails bankMasterDetails = getBankInfo(requestBean);
            validateModeNSetCode(requestBean, bankMasterDetails);
            if (StringUtils.isBlank(requestBean.getAccountNumber())
                    && StringUtils.isNotBlank(requestBean.getUpiAccRefId())) {
                AccountDetailsByAcoountIdResponse accountDetailsByAcoountIdResponse = fetchAccountDetailsFromUPI(requestBean);
                if (accountDetailsByAcoountIdResponse != null
                        && accountDetailsByAcoountIdResponse.getAccountDetails() != null) {
                    requestBean.setAccountNumber(accountDetailsByAcoountIdResponse.getAccountDetails().getAccountNo());
                }
            }
            if (StringUtils.isNotBlank(requestBean.getSsoToken()) && StringUtils.isBlank(requestBean.getPaytmCustId())) {
                UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(requestBean.getTxnToken());

                if (userDetailsBiz == null) {
                    throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                            ResponseConstants.SESSION_EXPIRY, true).setRequestBean(requestBean).build();
                } else {
                    requestBean.setPaytmCustId(userDetailsBiz.getUserId());
                }
            }

            // fetch transactionId from session if order is already created
            String transactionId = nativeSessionUtil.getTxnId(requestBean.getTxnToken());

            BigDecimal txnAmount = new BigDecimal(requestBean.getTxnAmount());
            String alipayMid = null;
            String paytmMid = requestBean.getMid();
            if (StringUtils.isNotBlank(requestBean.getPaymentOrderId())
                    && StringUtils.isNotBlank(requestBean.getPaymentMid())) {
                paytmMid = requestBean.getPaymentMid();
            }
            final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                    .fetchMerchanData(paytmMid);
            if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
                alipayMid = merchantMappingResponse.getResponse().getAlipayId();
            }

            if (StringUtils.isNotBlank(transactionId) && txnAmount.doubleValue() == 0d) {
                closeOrderForZeroAmountMandates(alipayMid, transactionId, paytmMid, requestBean.getOrderId());
                LOGGER.info("Order closed succesfully for Zero Amount Mandate Case");

            } else if (StringUtils.isNotBlank(transactionId) && txnAmount.doubleValue() > 0d) {
                modifyOrderForMandateUpfrontPayment(alipayMid, transactionId, requestBean.getOrderId(), paytmMid);
                LOGGER.info("Order Modified succesfully, mandate payment will be initiated on a later date");

            }

            nativeSessionUtil.setPaymentTypeId(requestBean.getTxnToken(), requestBean.getPaymentTypeId());

            nativeSessionUtil.setTxnTokenAndWorkflowOnMidOrderId(requestBean.getMid(), requestBean.getOrderId(),
                    requestBean.getTxnToken(), requestBean.getWorkflow());

            if (requestBean.isEnhancedCashierPaymentRequest()) {
                return createBmService.createBMForEnhancedFlow(requestBean);
            } else if (requestBean.isNativeJsonRequest()) {
                return createBmService.createBMForNativeJsonFlow(requestBean);
            }
            return createBmService.create(requestBean, bankMasterDetails.getBankMandate());
        } catch (MandateException e) {
            // LOGGER.error("Mandate Exception occured while creating mandate",
            // e);
            LOGGER.error("Mandate Exception occured while creating mandate",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            throw e;
        } catch (Throwable e) {
            LOGGER.error("Exception while creating mandate", e);
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(), ResponseConstants.SYSTEM_ERROR,
                    true).setRequestBean(requestBean).build();
        }
    }

    /**
     * processing response received after acceptance on destination bank.
     * 
     * @param processedMandateRequest
     * @return
     */
    public PageDetailsResponse processBM(ProcessedMandateRequest processedMandateRequest) {
        return processedBMService.process(processedMandateRequest);
    }

    /**
     * validation of create mandate request.
     * 
     * @param requestBean
     */
    private void validateRequest(PaymentRequestBean requestBean) {
        InitiateTransactionRequestBody initiateTransactionRequestBody = nativeSessionUtil.getOrderDetail(requestBean
                .getTxnToken());

        /*
         * preference is given to the call back url sent in request If the
         * callback url is derived from website, then preference will be given
         * to registered static callback url if it is empty then the registered
         * call back url for bank mandate would be used.
         */

        String callBackUrl = requestBean.getCallbackUrl();

        if (StringUtils.isBlank(callBackUrl)
                || (initiateTransactionRequestBody.getExtendInfo() != null && Boolean
                        .valueOf(initiateTransactionRequestBody.getExtendInfo().getIsCallbackWebsiteDerived()))) {
            callBackUrl = merchantResponseService.getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM,
                    initiateTransactionRequestBody.getMid());
            if (StringUtils.isNotBlank(callBackUrl)) {
                requestBean.setCallbackUrl(callBackUrl);
            }
        }
        // skip amount validation as upfront amount greater than zero is allowed
        // now

        /*
         * if (StringUtils.isNotEmpty(requestBean.getTxnAmount())) { BigDecimal
         * txnAmount = new BigDecimal(requestBean.getTxnAmount());
         * 
         * if (!txnAmount.equals(BigDecimal.ZERO)) { throw new
         * MandateException.ExceptionBuilder(callBackUrl,
         * ResponseConstants.INVALID_TXN_AMOUNT, true)
         * .setRequestBean(requestBean).build(); }
         * 
         * }
         */

        if (StringUtils.isBlank(requestBean.getAccountNumber()) && StringUtils.isBlank(requestBean.getUpiAccRefId())) {
            LOGGER.error("Account number is mandatory:: {}", requestBean.getAccountNumber());
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.ACCOUNT_NUMBER_NOT_EXIST, true).setRequestBean(requestBean).build();
        }

        if (StringUtils.isBlank(requestBean.getBankCode())) {
            LOGGER.error("Bank Code is mandatory:: {}", requestBean.getBankCode());
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.INVALID_CHANNEL, true).setRequestBean(requestBean).build();
        }

        if (StringUtils.isBlank(requestBean.getBankIFSC())) {
            LOGGER.error("Bank IFSC is mandatory:: {}", requestBean.getBankIFSC());
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(), ResponseConstants.IFSC_NOT_EXIST,
                    true).setRequestBean(requestBean).build();
        }

        if (StringUtils.isBlank(requestBean.getUserName())) {
            LOGGER.error("Account Holder Name is mandatory:: {}", requestBean.getUserName());
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.ACCOUNT_HOLDER_NAME_NOT_EXIST, true).setRequestBean(requestBean).build();
        }

        if (StringUtils.isBlank(requestBean.getAccountType())
                || (StringUtils.isNotBlank(requestBean.getAccountType()) && null == AccountType
                        .getAccountType(requestBean.getAccountType()))) {
            LOGGER.error("Account Type is mandatory:: {}", requestBean.getAccountType());
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.ACCOUNT_TYPE_NOT_EXIST, true).setRequestBean(requestBean).build();
        }

        validateFrequency(initiateTransactionRequestBody, requestBean, callBackUrl);
    }

    /**
     * validating bankcode and authentication mode as per following conditions :
     *
     * --> validating mandate type as well; if bank is not supporting any then
     * exception would be thrown. --> if auth mode is not blank then set bank
     * code would be set from bankmasterdetails; has already been fetched using
     * channel code. --> auth mode should be in correspondance to
     * MandateAuthMode enum.
     * 
     * @param requestBean
     * @param bankMasterDetails
     */
    private void validateModeNSetCode(PaymentRequestBean requestBean, BankMasterDetails bankMasterDetails) {
        // validating mandate type supported by bank
        if (null == MandateMode.getByMappingName(bankMasterDetails.getBankMandate())) {
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.INVALID_MANDATE_MODE, true).setRequestBean(requestBean).build();
        }

        // validating auth mode and setting npci bank code.
        if (MandateMode.getByMappingName(bankMasterDetails.getBankMandate()) == MandateMode.E_MANDATE
                && StringUtils.isNotBlank(requestBean.getMandateAuthMode())) {
            MandateAuthMode mandateAuthMode = MandateAuthMode.getByName(requestBean.getMandateAuthMode());
            if (null == mandateAuthMode) {
                throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                        ResponseConstants.INVALID_MANDATE_AUTH_MODE, true).setRequestBean(requestBean).build();
            }

            if (MandateAuthMode.NET_BANKING == mandateAuthMode && !bankMasterDetails.isMandateNetBanking()) {
                throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                        ResponseConstants.INVALID_MANDATE_AUTH_MODE, true).setRequestBean(requestBean).build();
            }

            if (MandateAuthMode.DEBIT_CARD == mandateAuthMode && !bankMasterDetails.isMandateDebitCard()) {
                throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                        ResponseConstants.INVALID_MANDATE_AUTH_MODE, true).setRequestBean(requestBean).build();
            }
            requestBean.setMandateBankCode(bankMasterDetails.getStandardBankCode());
        }
    }

    /**
     * frequency validation
     * 
     * @param initiateTransactionRequestBody
     * @param requestBean
     * @param callBackUrl
     */
    private void validateFrequency(InitiateTransactionRequestBody initiateTransactionRequestBody,
            PaymentRequestBean requestBean, String callBackUrl) {
        if (null != initiateTransactionRequestBody
                && initiateTransactionRequestBody instanceof SubscriptionTransactionRequestBody) {
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = (SubscriptionTransactionRequestBody) initiateTransactionRequestBody;
            if (!subscriptionTransactionRequestBody.isFlexiSubscription()
                    && StringUtils.isNotBlank(subscriptionTransactionRequestBody.getSubscriptionFrequency())
                    && Integer.valueOf(subscriptionTransactionRequestBody.getSubscriptionFrequency()) > 1) {
                throw new MandateException.ExceptionBuilder(callBackUrl, ResponseConstants.INVALID_FREQUENCY_VALUE,
                        true).setRequestBean(requestBean).build();
            }
        }
    }

    /**
     * fetching callback url for website "BM" registered specifically for
     * mandate.
     * 
     * @param requestBean
     * @return
     * @throws MappingServiceClientException
     */
    private BankMasterDetails getBankInfo(PaymentRequestBean requestBean) throws MappingServiceClientException {
        BankMasterDetails bankMasterDetails = bankInfoDataService.getBankInfoData(String.valueOf(requestBean
                .getBankCode()));
        LOGGER.info("Response received from mapping service for bank code {} is {}", requestBean.getBankCode(),
                bankMasterDetails);
        return bankMasterDetails;
    }

    private AccountDetailsByAcoountIdResponse fetchAccountDetailsFromUPI(PaymentRequestBean requestBean) {

        AccountDetailsByAcoountIdResquest accountDetailsAcoountIdResquest = new AccountDetailsByAcoountIdResquest();

        // fetch user details from session to get the paytm user cust-id
        UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(requestBean.getTxnToken());

        if (userDetailsBiz == null) {
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(), ResponseConstants.SESSION_EXPIRY,
                    true).setRequestBean(requestBean).build();
        }
        accountDetailsAcoountIdResquest.setAccountId(requestBean.getUpiAccRefId());
        accountDetailsAcoountIdResquest.setCustId(userDetailsBiz.getUserId());
        requestBean.setPaytmCustId(userDetailsBiz.getUserId());
        // create JWT Token for UPI
        Map<String, String> jwtClaims = new HashMap<String, String>();
        jwtClaims.put("custId", userDetailsBiz.getUserId());
        jwtClaims.put("accountId", requestBean.getUpiAccRefId());

        AccountDetailsByAcoountIdResponse facadeResponse = null;
        try {
            String jwtToken = JWTWithHmacSHA256.createJsonWebToken(jwtClaims, "UpiPG");
            if ((jwtToken == null)) {
                LOGGER.info("jwtToken not created successfully");
                throw BaseException.getException();
            }
            facadeResponse = getUPIAccountService.getUpiAccountsByAccountIdCustId(accountDetailsAcoountIdResquest,
                    jwtToken);
        } catch (JWTCreationException | FacadeCheckedException | HttpCommunicationException | IllegalPayloadException e) {
            LOGGER.error("Error occured while executing getUPIAccount", e.getMessage(), e);
            throw BaseException.getException();
        }
        if (null == facadeResponse || null == facadeResponse.getAccountDetails()) {
            throw new MandateException.ExceptionBuilder(requestBean.getCallbackUrl(),
                    ResponseConstants.ACCOUNT_NUMBER_NOT_EXIST, true).setRequestBean(requestBean).build();
        }
        return facadeResponse;
    }

    public void closeOrderForZeroAmountMandates(String alipayMid, String transId, String merchantId, String orderId) {

        BizCancelOrderRequest bizCancelOrderRequest = new BizCancelOrderRequest(alipayMid, transId,
                "No Payment required for Zero Amount Mandate", false);
        Routes routes = routerUtil.getRoute(merchantId, orderId, transId, null, "closeOrder");
        bizCancelOrderRequest.setPaytmMerchantId(merchantId);
        bizCancelOrderRequest.setRoute(routes);
        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = orderServiceImpl
                .closeOrder(bizCancelOrderRequest);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Close/Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }

    }

    public void modifyOrderForMandateUpfrontPayment(String alipayMid, String transId, String orderId, String mid) {

        Long orderTimeout = Long.valueOf(ConfigurationUtil.getProperty(MANDATE_ORDER_TIMEOUT_FOR_UPFRONT_PAYMENT, "9")) * 24 * 60 * 60;
        Routes routes = routerUtil.getRoute(mid, orderId, transId, null, "modifyOrder");
        OrderModifyRequest orderModifyRequest = workRequestCreator.getOrderModifyRequestForExpiry(alipayMid, transId,
                orderId, orderTimeout, routes, mid);
        try {
            orderServiceImpl.modifyOrder(orderModifyRequest);
        } catch (FacadeCheckedException exception) {
            LOGGER.error("Exception Occurred while modifying order :", exception);
        }
    }

}
