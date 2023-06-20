package com.paytm.pgplus.theia.controllers;

import com.google.gson.Gson;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.theia.models.ProcessedBmResponse;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.workflow.BMService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.BasicPayOption.PPBL;

@Controller
public class MandateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MandateController.class);
    private static Gson gson = new Gson();
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Autowired
    private BMService bmService;

    // @Autowired
    // private AOABMService aoaBMService;

    @Autowired
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @RequestMapping(value = "process/mandate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void processNpciResponse(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse)
            throws ServletException, IOException, IllegalAccessException {
        ProcessedMandateRequest processedMandateRequest = new ProcessedMandateRequest(servletRequest);
        LOGGER.info("Mandate Response received: {}", processedMandateRequest);

        PageDetailsResponse pageDetailsResponse = bmService.processBM(processedMandateRequest);
        ProcessedBmResponse bmResponse = gson.fromJson(pageDetailsResponse.getS2sResponse(), ProcessedBmResponse.class);

        String merchantResponseHtml = merchantResponseService.getResponseForMandateMerchant(
                pageDetailsResponse.getRedirectionUrl(), bmResponse, bmResponse.getResultInfo(), null);
        if (StringUtils.isNotBlank(merchantResponseHtml)) {
            servletResponse.getOutputStream().print(merchantResponseHtml);
            servletResponse.setContentType("text/html");
            return;
        } else if (null != bmResponse) {
            TransactionResponse mandateResponse = getMerchantMandateResponse(bmResponse);
            servletRequest.setAttribute("processedResponse", mandateResponse);
            servletRequest.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnNpciResPage() + ".jsp")
                    .forward(servletRequest, servletResponse);
            return;
        }
        servletRequest.getRequestDispatcher(
                VIEW_BASE + theiaViewResolverService.returnOOPSPage(servletRequest) + ".jsp").forward(servletRequest,
                servletResponse);
        return;
    }

    // @RequestMapping(value = "aoa/process/mandate", method =
    // RequestMethod.POST, consumes =
    // MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    // public void processAOAMandateResponse(final HttpServletRequest
    // servletRequest, final HttpServletResponse servletResponse)
    // throws ServletException, IOException, IllegalAccessException {
    //
    // AoaMandateCallbackRequest aoaMandateCallbackRequest =
    // getAoaMandateCallbackRequestFromServletRequest(servletRequest);
    //
    // LOGGER.info("Mandate request received for api aoa/process/mandate : {}",
    // aoaMandateCallbackRequest);
    //
    // PageDetailsResponse pageDetailsResponse =
    // aoaBMService.processBM(aoaMandateCallbackRequest);
    // ProcessedBmResponse bmResponse =
    // gson.fromJson(pageDetailsResponse.getS2sResponse(),
    // ProcessedBmResponse.class);
    //
    // String merchantResponseHtml =
    // merchantResponseService.getResponseForMandateMerchant(
    // pageDetailsResponse.getRedirectionUrl(), bmResponse,
    // bmResponse.getResultInfo(), null);
    // if (StringUtils.isNotBlank(merchantResponseHtml)) {
    // servletResponse.getOutputStream().print(merchantResponseHtml);
    // servletResponse.setContentType("text/html");
    // return;
    // } else if (null != bmResponse) {
    // TransactionResponse mandateResponse =
    // getMerchantMandateResponse(bmResponse);
    // servletRequest.setAttribute("processedResponse", mandateResponse);
    // servletRequest.getRequestDispatcher(VIEW_BASE +
    // theiaViewResolverService.returnNpciResPage() + ".jsp")
    // .forward(servletRequest, servletResponse);
    // return;
    // }
    // servletRequest.getRequestDispatcher(
    // VIEW_BASE + theiaViewResolverService.returnOOPSPage(servletRequest) +
    // ".jsp").forward(servletRequest,
    // servletResponse);
    // return;
    // }

    private TransactionResponse getMerchantMandateResponse(ProcessedBmResponse bmResponse) {
        TransactionResponse merchantMandateResponse = new TransactionResponse();
        merchantMandateResponse.setPaymentMode(SubsPaymentMode.BANK_MANDATE.getePayMethodName());
        merchantMandateResponse.setMandateType(bmResponse.getMandateType());
        merchantMandateResponse.setMid(bmResponse.getMid());
        merchantMandateResponse.setMerchantCustId(bmResponse.getMerchantCustId());
        merchantMandateResponse.setOrderId(bmResponse.getOrderId());
        merchantMandateResponse.setSubsId(bmResponse.getSubscriptionId());
        merchantMandateResponse.setAccepted(bmResponse.getIsAccepted());
        merchantMandateResponse.setRejectedBy(bmResponse.getRejectedBy());
        merchantMandateResponse.setAcceptedRefNo(bmResponse.getAcceptedRefNo());
        merchantMandateResponse
                .setGateway(org.apache.commons.lang3.StringUtils.isBlank(bmResponse.getGatewayCode()) ? PPBL
                        : bmResponse.getGatewayCode());

        ResultInfo resultInfo = bmResponse.getResultInfo();

        if (null != resultInfo) {
            merchantMandateResponse.setResponseCode(resultInfo.getResultCode());
            merchantMandateResponse.setResponseMsg(resultInfo.getResultMsg());

            if (bmResponse.isAoa()) {
                if (bmResponse.getIsAccepted())
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
                else
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
            } else {
                switch (resultInfo.getResultStatus()) {
                case "S":
                case "A":
                case "SUCCESS":
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
                case "F":
                case "U":
                case "FAILURE":
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
                default:
                    merchantMandateResponse.setTransactionStatus(resultInfo.getResultStatus());
                }
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        merchantMandateResponse.setTxnDate(simpleDateFormat.format(new Date()));
        return merchantMandateResponse;
    }

    // private AoaMandateCallbackRequest
    // getAoaMandateCallbackRequestFromServletRequest(final HttpServletRequest
    // servletRequest){
    // AoaMandateCallbackRequest aoaMandateCallbackRequest = new
    // AoaMandateCallbackRequest();
    //
    // String mandateResponseString = servletRequest.getParameter("msg");
    // String merchantCode = servletRequest.getParameter("tpsl_mrct_cd");
    //
    // aoaMandateCallbackRequest.setMandateResponseString(mandateResponseString);
    // aoaMandateCallbackRequest.setMerchantCode(merchantCode);
    //
    // return aoaMandateCallbackRequest;
    // }
}