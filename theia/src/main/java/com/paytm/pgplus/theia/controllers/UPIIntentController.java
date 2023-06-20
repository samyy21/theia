package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.validator.GenericFlowRequestBeanValidator;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.enums.UPIPSPEnum;
import com.paytm.pgplus.biz.workflow.model.FetchDeepLinkResponseBody;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.application.MappingServiceClient;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.controllers.helper.UPIIntentControllerHelper;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.models.FetchDeepLinkRequest;
import com.paytm.pgplus.theia.models.FetchDeepLinkRequestBody;
import com.paytm.pgplus.theia.models.FetchDeepLinkResponse;
import com.paytm.pgplus.theia.models.FetchDeepLinkResponseHeader;
import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsAndAccountCheckRequestBody;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.request.UpiAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponse;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsAndAccountCheckResponseBody;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import com.paytm.pgplus.theia.models.upiAccount.response.UpiAccountResponse;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsRequest;
import com.paytm.pgplus.theia.nativ.model.fetchpspapps.FetchPspAppsResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.upiAccount.CheckUPIAccountService;
import com.paytm.pgplus.theia.services.upiAccount.FetchUpiOptionsService;
import com.paytm.pgplus.theia.services.upiAccount.UpiIntentService;
import com.paytm.pgplus.theia.session.utils.UPIIntentUtil;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@NativeControllerAdvice
public class UPIIntentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIIntentController.class);

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("UPIPSPWorkflow")
    private IWorkFlow bizWorkFlow;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    private UPIIntentUtil upiIntentUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    MappingServiceClient mappingServiceClient;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private CheckUPIAccountService checkUPIAccountService;

    @Autowired
    private FetchUpiOptionsService fetchUpiOptionsService;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    @Qualifier("upiIntentControllerHelper")
    private UPIIntentControllerHelper upiIntentControllerHelper;

    @Autowired
    @Qualifier("upiIntentServiceImpl")
    private UpiIntentService upiIntentService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(value = "/v1/createUPILink")
    @ResponseBody
    public FetchDeepLinkResponse fetchDeepLink(@RequestBody String requestPayload, HttpServletRequest httpServletRequest)
            throws FacadeCheckedException, TheiaDataMappingException, IOException, GeneralSecurityException {
        final long startTime = System.currentTimeMillis();
        FetchDeepLinkRequest fetchDeepLinkRequest = null;
        fetchDeepLinkRequest = JsonMapper.mapJsonToObject(requestPayload, FetchDeepLinkRequest.class);
        FetchDeepLinkRequestBody fetchDeepLinkRequestBody = fetchDeepLinkRequest.getBody();

        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil
                .validate(fetchDeepLinkRequestBody.getTxnToken());
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();

        validateFetchDeepLinkRequest(fetchDeepLinkRequestBody, orderDetail);
        fetchDeepLinkRequestBody.setAdditionalInfo(orderDetail.getAdditionalInfo());
        fetchDeepLinkRequestBody.setTxnAmount(orderDetail.getTxnAmount().getValue());
        LOGGER.info("Received request for fetchDeepLink : {}", fetchDeepLinkRequest);
        if (!beanValidation(fetchDeepLinkRequest)) {
            return prepareFetchDeepLinkResponse(fetchDeepLinkRequest, new WorkFlowResponseBean());
        }
        if (ERequestType.NATIVE_MF.getType().equals(orderDetail.getRequestType())
                || (orderDetail.getLinkDetailsData() != null
                        && StringUtils.isNotEmpty(orderDetail.getLinkDetailsData().getSubRequestType()) && ERequestType.NATIVE_MF
                        .getType().equals(orderDetail.getLinkDetailsData().getSubRequestType()))) {
            fetchDeepLinkRequestBody.setRequestType(ERequestType.NATIVE_MF.getType());
        } else if (ERequestType.NATIVE_ST.getType().equals(orderDetail.getRequestType())
                || (orderDetail.getLinkDetailsData() != null
                        && StringUtils.isNotEmpty(orderDetail.getLinkDetailsData().getSubRequestType()) && ERequestType.NATIVE_ST
                        .getType().equals(orderDetail.getLinkDetailsData().getSubRequestType()))) {
            fetchDeepLinkRequestBody.setRequestType(ERequestType.NATIVE_ST.getType());
        } else {
            fetchDeepLinkRequestBody.setRequestType(ERequestType.SEAMLESS_3D_FORM.getType());
        }

        WorkFlowResponseBean workFlowResponseBean = processFetchDeepLinkRequest(fetchDeepLinkRequest,
                httpServletRequest, orderDetail);
        LOGGER.info("Total time taken by fetchDeepLink API : {}ms", System.currentTimeMillis() - startTime);
        return prepareFetchDeepLinkResponse(fetchDeepLinkRequest, workFlowResponseBean);
    }

    private void validateFetchDeepLinkRequest(FetchDeepLinkRequestBody fetchDeepLinkRequestBody,
            InitiateTransactionRequestBody orderDetail) {
        if (!StringUtils.equals(fetchDeepLinkRequestBody.getMid(), orderDetail.getMid())) {
            LOGGER.error("mid in token and request are different");
            throw RequestValidationException.getException();
        }
        if (!StringUtils.equals(fetchDeepLinkRequestBody.getOrderId(), orderDetail.getOrderId())) {
            LOGGER.error("orderid in token and request are different");

            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(false).isRetryAllowed(false)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }
        String aggMid = fetchDeepLinkRequestBody.getAggMid();
        if (StringUtils.isNotBlank(aggMid) && !StringUtils.equals(orderDetail.getAggMid(), aggMid)) {
            LOGGER.error("aggMid in token and request are different");
            throw RequestValidationException.getException();
        }
    }

    private boolean beanValidation(FetchDeepLinkRequest upiPSPrequest) {

        final GenericFlowRequestBeanValidator<FetchDeepLinkRequest> bean = new GenericFlowRequestBeanValidator<FetchDeepLinkRequest>(
                upiPSPrequest);
        ValidationResultBean validationResultBean = bean.validate();
        if (!validationResultBean.isSuccessfullyProcessed()) {
            String failureDescription = StringUtils.isNotBlank(bean.getErrorMessage()) ? bean.getErrorMessage()
                    : "Validation Failed";
            LOGGER.error("Parameter Validation failed due to : {}", failureDescription);
            return false;
        }
        return true;
    }

    private FetchDeepLinkResponse prepareFetchDeepLinkResponse(FetchDeepLinkRequest fetchDeepLinkRequest,
            WorkFlowResponseBean workFlowResponseBean) throws IOException, GeneralSecurityException {
        FetchDeepLinkResponse fetchDeepLinkResponse = new FetchDeepLinkResponse();
        FetchDeepLinkResponseHeader header = new FetchDeepLinkResponseHeader();
        FetchDeepLinkResponseBody body = new FetchDeepLinkResponseBody();
        header.setResponseTimestamp(Long.valueOf(System.currentTimeMillis()));
        header.setVersion(TheiaConstant.UPIPSPKeys.VERSION);
        if (null == workFlowResponseBean.getUpiPSPResponse()) {
            body.setResultCode(UPIPSPEnum.FAIL.getResultCode());
            body.setResultCodeId(UPIPSPEnum.FAIL.getResultCodeId());
            body.setResultMsg(UPIPSPEnum.FAIL.getResultMsg());
        } else {
            body.setResultCode(workFlowResponseBean.getUpiPSPResponse().getResultCode());
            body.setResultCodeId(workFlowResponseBean.getUpiPSPResponse().getResultCodeId());
            body.setResultMsg(workFlowResponseBean.getUpiPSPResponse().getResultMsg());
            if (StringUtils.isNotBlank(workFlowResponseBean.getUpiPSPResponse().getExternalSerialNo())) {
                body.setDeepLink(upiIntentUtil.createDeepLink(fetchDeepLinkRequest.getBody(), workFlowResponseBean));
                body.setOrderId(workFlowResponseBean.getUpiPSPResponse().getOrderId());
            }
            body.setTransId(workFlowResponseBean.getTransID());
            body.setCashierRequestId(workFlowResponseBean.getCashierRequestId());
        }
        fetchDeepLinkResponse.setHead(header);
        fetchDeepLinkResponse.setBody(body);
        LOGGER.info("Reponse returned for fetchDeepLink is {} :", fetchDeepLinkResponse);
        return fetchDeepLinkResponse;
    }

    private WorkFlowResponseBean processFetchDeepLinkRequest(FetchDeepLinkRequest request,
            HttpServletRequest httpServletRequest, InitiateTransactionRequestBody orderDetail)
            throws FacadeCheckedException, TheiaDataMappingException {

        if (StringUtils.isBlank(request.getBody().getOrderId())) {

            String aggregatorMid = merchantDataUtil.getAggregatorMid(request.getBody().getMid());

            request.getBody().setOrderId(OfflinePaymentUtils.generateOrderId(aggregatorMid));

            LOGGER.info("Order-Id generated for the request is {} :", request.getBody().getOrderId());
        }

        PaymentRequestBean paymentRequestBean = preparePaymentRequestbean(request, httpServletRequest, orderDetail);
        GenericCoreResponseBean<WorkFlowResponseBean> workflowResponse = null;
        try {
            paymentRequestBean.setSessionRequired(false);
            WorkFlowRequestBean workFlowRequestBean = bizRequestResponseMapper
                    .mapWorkFlowRequestData(paymentRequestBean);
            workFlowRequestBean.setVirtualPaymentAddress(paymentRequestBean.getVirtualPaymentAddr());
            workFlowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH);
            workFlowRequestBean.setIsDeepLinkReq("true");
            workflowResponse = bizWorkFlow.process(workFlowRequestBean);
            transactionCacheUtils.putTransInfoInCache(workflowResponse.getResponse().getTransID(),
                    workFlowRequestBean.getPaytmMID(), workFlowRequestBean.getOrderID(), false,
                    paymentRequestBean.getRequestType());
        } catch (Exception e) {
            LOGGER.error("Exception occured while processing fetchDeepLink Request {} :", e);
            throw e;
        }
        return (null != workflowResponse ? workflowResponse.getResponse() : null);
    }

    private PaymentRequestBean preparePaymentRequestbean(FetchDeepLinkRequest request,
            HttpServletRequest httpServletRequest, InitiateTransactionRequestBody orderDetail)
            throws FacadeCheckedException {
        PaymentRequestBean payRequestBean = new PaymentRequestBean();
        payRequestBean.setRequest(httpServletRequest);
        payRequestBean.setMid(request.getBody().getMid());
        payRequestBean.setOrderId(request.getBody().getOrderId());
        payRequestBean.setRequestType(request.getBody().getRequestType());
        payRequestBean.setTxnAmount(request.getBody().getTxnAmount());
        payRequestBean.setPaymentDetails("dummyvpa@upi");
        payRequestBean.setVirtualPaymentAddr("dummyvpa@upi");
        payRequestBean.setPaymentTypeId(PaymentTypeIdEnum.UPI.name());
        payRequestBean.setCallbackUrl(orderDetail.getCallbackUrl());
        payRequestBean.setWebsite(orderDetail.getWebsiteName());
        try {
            payRequestBean.setAdditionalInfoMF(JsonMapper.mapObjectToJson(request.getBody().getAdditionalInfo()));
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while setting AdditionalInfoMF {}", e);
            throw e;
        }
        payRequestBean.setAccountNumber(request.getBody().getAccountNumber());
        payRequestBean.setValidateAccountNumber(orderDetail.getValidateAccountNumber());
        payRequestBean.setAllowUnverifiedAccount(orderDetail.getAllowUnverifiedAccount());
        if (null != request.getBody().getExtendInfo() && request.getBody().getExtendInfo().isEmpty()) {
            payRequestBean.setAdditionalInfo(request.getBody().getExtendInfo().toString());
        }
        return payRequestBean;
    }

    @Produces(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @Consumes(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/checkUPIAccountExist")
    @Deprecated
    public CheckUPIAccountResponse checkIfUPIAccountExist(
            @ApiParam(required = true) @RequestBody CheckUPIAccountRequest request) {

        LOGGER.info("Request For checkUPIAccountExist Api Received :{}", request);

        CheckUPIAccountResponse response = checkUPIAccountService.checkIfUPIAccountExistForUser(request);

        LOGGER.info("Response For checkUPIAccountExist Api Generated :{}", response);

        return response;
    }

    @Produces(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @Consumes(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/fetchUPIOptions")
    @Deprecated
    public FetchUpiOptionsResponse fetchUpiOptions(
            @ApiParam(required = true) @RequestBody FetchUpiOptionsRequest request) {

        LOGGER.info("Request For FetchUpiOptions Api Received :{}", request);

        FetchUpiOptionsResponse response = fetchUpiOptionsService.fetchUpiOptions(request);

        LOGGER.info("Response For fetchUpiOptions Api Generated :{}", response);

        return response;
    }

    @Produces(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @Consumes(value = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/fetchUPIOptionsWithAccountCheck")
    public UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> checkIfUPIAccountExistAndFetchUpiOptions(
            @ApiParam(required = true) @RequestBody UpiAccountRequest<FetchUpiOptionsAndAccountCheckRequestBody> request) {

        LOGGER.info("Request For checkUPIAccountExist Api Received :{}", request);

        upiIntentControllerHelper.validateRequest(request);

        UpiAccountResponse<FetchUpiOptionsAndAccountCheckResponseBody> response = upiIntentService
                .getUpiIntent(request);

        LOGGER.info("Response For fetchUPIOptionsWithAccountCheck Api Generated :{}", response);

        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(value = "api/v1/fetchPspApps")
    @ResponseBody
    public FetchPspAppsResponse getPspApps(@RequestBody FetchPspAppsRequest request) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.info("Request received for API: /fetchPspApps is: {}", request);
            IRequestProcessor<FetchPspAppsRequest, FetchPspAppsResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_PSP_APPS);
            FetchPspAppsResponse response = requestProcessor.process(request);
            LOGGER.info("Response returned for API: /fetchPspApps is: {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for getPspApps API is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
