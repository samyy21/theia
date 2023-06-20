package com.paytm.pgplus.theia.workflow;

import com.google.gson.Gson;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.enums.MandateAuthMode;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.payloadvault.subscription.enums.AccountType;
import com.paytm.pgplus.payloadvault.subscription.request.MandateRequest;
import com.paytm.pgplus.payloadvault.subscription.request.PaperMandateCreateRequest;
import com.paytm.pgplus.payloadvault.subscription.response.MandateResponse;
import com.paytm.pgplus.payloadvault.subscription.response.PaperMandateCreateResponse;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankFormData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankRedirectionDetail;
import com.paytm.pgplus.theia.nativ.processor.MandateProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;

/**
 * service class for creation of mandate.
 */
@Service("createBmService")
public class CreateBmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateBmService.class);
    private static final Gson gson = new Gson();
    private static final String NPCI_URL = ConfigurationUtil.getProperty("npci.redirection.url");

    @Autowired
    private MandateProcessor mandateProcessor;

    @Autowired
    private IBankInfoDataService bankInfoDataService;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private MerchantResponseService merchantResponseService;

    /**
     * mandate creation based on mandate mode passed.
     *
     * @param requestBean
     * @param mandateMode
     * @return
     */
    public PageDetailsResponse create(PaymentRequestBean requestBean, String mandateMode) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        Map<String, String> data = new HashMap<>();

        // LOGGER.info("Mandate mode for the bank is {}", mandateMode);
        if (StringUtils.isNotBlank(mandateMode) && MandateMode.getByMappingName(mandateMode) == MandateMode.E_MANDATE) {
            LOGGER.info("Entered e mandate creation for Native Flow");
            MandateRequest request = generateEMandateRequest(requestBean);
            MandateResponse npciReq = getNPCIEMandateRequest(request, requestBean);
            data.put(TheiaConstant.ExtraConstants.SUBS_BM_MODE, MandateMode.E_MANDATE.name());
            requestBean.setMandateType(MandateMode.E_MANDATE.name());
            pageDetailsResponse.setS2sResponse(gson.toJson(npciReq.getMandateResponseBody()));
            pageDetailsResponse.setRedirectionUrl(NPCI_URL);
            pageDetailsResponse.setJspName(theiaViewResolverService.returnNpciReqPage());
        } else if (StringUtils.isNotBlank(mandateMode)
                && MandateMode.getByMappingName(mandateMode) == MandateMode.PAPER_MANDATE) {
            LOGGER.info("Entered physical mandate creation flow");
            data.put(TheiaConstant.ExtraConstants.SUBS_BM_MODE, MandateMode.PAPER_MANDATE.name());
            PaperMandateCreateRequest paperMandateCreateRequest = generatePaperMandateRequest(requestBean);
            PaperMandateCreateResponse paperMandateCreateResponse = createPaperMandate(paperMandateCreateRequest,
                    requestBean);
            requestBean.setMandateType(MandateMode.PAPER_MANDATE.name());
            String merchantResponseHtml = merchantResponseService.getResponseForMandateMerchant(requestBean
                    .getCallbackUrl(), null, getConvertedResultInfo(paperMandateCreateResponse.getBody()
                    .getResultInfo()), requestBean);
            pageDetailsResponse.setHtmlPage(merchantResponseHtml);
        }
        pageDetailsResponse.setData(data);
        // LOGGER.info("Create mandate request successfully processed");
        return pageDetailsResponse;
    }

    /*
     * handled only E-mandate requests here as Paper Mandate will not be
     * supported for Redirection Flow, already filtered in APP_DATA
     */
    public PageDetailsResponse createBMForEnhancedFlow(PaymentRequestBean requestBean) {

        // LOGGER.info("Entered E-mandate creation for Enhanced flow");

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();

        MandateRequest request = generateEMandateRequest(requestBean);
        MandateResponse npciReq = getNPCIEMandateRequest(request, requestBean);

        BankFormData bankFormData = new BankFormData();
        BankRedirectionDetail bankRedirectionDetail = new BankRedirectionDetail();
        bankRedirectionDetail.setMethod(POST);

        Map<String, String> content = getContentForBM(npciReq);
        bankRedirectionDetail.setContent(content);
        bankRedirectionDetail.setCallbackUrl(NPCI_URL);
        bankFormData.setHead(new ResponseHeader());
        bankFormData.setBody(bankRedirectionDetail);
        try {
            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(bankFormData));
            LOGGER.info("Create mandate request successfully processed");
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  NPCI request obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
        }
        return pageDetailsResponse;
    }

    /* BM for Native Json */
    public PageDetailsResponse createBMForNativeJsonFlow(PaymentRequestBean requestBean) {

        // LOGGER.info("Entered E-mandate creation for NativeJson flow");

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();

        MandateRequest request = generateEMandateRequest(requestBean);
        MandateResponse npciReq = getNPCIEMandateRequest(request, requestBean);

        Map<String, String> content = getContentForBM(npciReq);
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        NativeJsonResponseBody body = new NativeJsonResponseBody();
        body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        BankForm bankForm = new BankForm();
        bankForm.setPageType("redirect");

        FormDetail formDetail = new FormDetail();
        formDetail.setActionUrl(NPCI_URL);
        formDetail.setMethod("POST");
        formDetail.setType("redirect");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        formDetail.setHeaders(headers);
        formDetail.setContent(content);

        bankForm.setRedirectForm(formDetail);
        body.setBankForm(bankForm);

        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);

        try {
            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(nativeJsonResponse));
            LOGGER.info("Create mandate request successfully processed");
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  NPCI request obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
        }
        return pageDetailsResponse;
    }

    /**
     * creating e mandate.
     *
     * @param request
     * @param requestBean
     * @return
     */
    private MandateResponse getNPCIEMandateRequest(MandateRequest request, PaymentRequestBean requestBean) {
        MandateResponse npciCreateReq = mandateProcessor.createEMandate(request, requestBean.getCallbackUrl(),
                requestBean);
        return npciCreateReq;
    }

    /**
     * creating physical/paper mandate.
     *
     * @param request
     * @param requestBean
     * @return
     */
    private PaperMandateCreateResponse createPaperMandate(PaperMandateCreateRequest request,
            PaymentRequestBean requestBean) {
        PaperMandateCreateResponse response = mandateProcessor.createPaperMandate(request,
                requestBean.getCallbackUrl(), requestBean);
        return response;
    }

    /**
     * generating request for e mandate creation
     *
     * @param requestBean
     * @return
     */
    private MandateRequest generateEMandateRequest(PaymentRequestBean requestBean) {
        MandateRequest request = new MandateRequest();
        request.setAccountNumber(requestBean.getAccountNumber());
        request.setAccountHolderName(requestBean.getUserName());
        request.setSubscriptionId(requestBean.getSubscriptionID());
        request.setBankIFSCCode(requestBean.getBankIFSC());
        request.setBankName(requestBean.getBankName());
        request.setMandateBankCode(requestBean.getMandateBankCode());
        request.setMid(requestBean.getMid());
        request.setMandateAuthMode(MandateAuthMode.getAuthModeByName(requestBean.getMandateAuthMode()));
        request.setChannelId(requestBean.getBankCode());
        request.setTxnAmount(requestBean.getTxnAmount());
        request.setPaytmCustId(requestBean.getPaytmCustId());
        request.setMandateCallbackUrl(requestBean.getCallbackUrl());
        return request;
    }

    /**
     * generating request for paper mandate creation.
     *
     * @param requestBean
     * @return
     */
    private PaperMandateCreateRequest generatePaperMandateRequest(PaymentRequestBean requestBean) {
        PaperMandateCreateRequest paperMandateCreateRequest = new PaperMandateCreateRequest();
        paperMandateCreateRequest.setSubscriptionId(requestBean.getSubscriptionID());
        paperMandateCreateRequest.setAccountNumber(requestBean.getAccountNumber());
        paperMandateCreateRequest.setAccountType(AccountType.getAccountType(requestBean.getAccountType()));
        paperMandateCreateRequest.setBankName(requestBean.getBankName());
        paperMandateCreateRequest.setBankIFSCCode(requestBean.getBankIFSC());
        paperMandateCreateRequest.setCustomerName(requestBean.getUserName());
        paperMandateCreateRequest.setChannelId(requestBean.getBankCode());
        paperMandateCreateRequest.setTxnAmount(requestBean.getTxnAmount());
        paperMandateCreateRequest.setPaytmCustId(requestBean.getPaytmCustId());
        return paperMandateCreateRequest;
    }

    private ResultInfo getConvertedResultInfo(com.paytm.pgplus.payloadvault.subscription.response.ResultInfo resultInfo) {
        return new ResultInfo(resultInfo.getStatus(), resultInfo.getCode(), resultInfo.getMessage());
    }

    private Map<String, String> getContentForBM(MandateResponse npciReq) {
        Map<String, String> content = new HashMap<>();
        if (npciReq != null && npciReq.getMandateResponseBody() != null) {
            content.put(MERCHANTID, npciReq.getMandateResponseBody().getMerchantId());
            content.put(MANDATE_REQ_DOC, npciReq.getMandateResponseBody().getMandateReqDoc());
            content.put(CHECKSUM_VAL, npciReq.getMandateResponseBody().getCheckSumVal());
            content.put(BANK_ID, npciReq.getMandateResponseBody().getBankId());
            content.put(AUTHMODE, npciReq.getMandateResponseBody().getAuthMode());
            content.put(SPID, npciReq.getMandateResponseBody().getSpId());
        }
        return content;
    }
}
