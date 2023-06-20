package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskOAuthValidatedData;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponse;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponseBody;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.risk.RiskVerificationPageData;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequest;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponse;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponseBody;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequest;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.exceptions.*;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/risk")
@NativeControllerAdvice
public class NativeRiskVerifierController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRiskVerifierController.class);

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private RiskVerificationUtil riskVerificationUtil;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/doView", method = { RequestMethod.POST })
    public DoViewResponse doView(@ApiParam(required = true) @RequestBody DoViewRequest request) throws Exception {
        long startTime = System.currentTimeMillis();

        LOGGER.info("Request received for risk verifier doView: {} ", request);

        try {
            IRequestProcessor<DoViewRequest, DoViewResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.RISK_VERIFIER_DO_VIEW);
            DoViewResponse response = requestProcessor.process(request);
            LOGGER.info("Response being sent for risk verifier doView: {} ", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for risk verifier doView is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/doVerify", method = { RequestMethod.POST })
    public DoVerifyResponse doVerify(@ApiParam(required = true) @RequestBody DoVerifyRequest request) throws Exception {
        long startTime = System.currentTimeMillis();

        LOGGER.info("Request received for risk verifier doVerify: {} ", request);

        try {
            IRequestProcessor<DoVerifyRequest, DoVerifyResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.RISK_VERIFIER_DO_VERIFY);
            DoVerifyResponse response = requestProcessor.process(request);
            LOGGER.info("Response being sent for risk verifier doVerify: {} ", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for risk verifier doVerify is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/showVerificationPage", method = { RequestMethod.POST })
    public void showRiskVerficationPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Request received to show risk verification page.");
        RiskVerifierPayload riskVerifierPayload = null;

        try {
            String token = request.getParameter("token");
            riskVerifierPayload = nativeSessionUtil.validateRiskVerifierToken(token);
            String mid = riskVerifierPayload.getMid();
            String orderId = riskVerifierPayload.getOrderId();
            nativeValidationService.validateMidOrderId(mid, orderId);
            String callbackUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL) + RiskConstants.PTC_V1_URL;
            String htmlPage = riskVerificationUtil.getRiskVerifierHtmlPage(token, mid, orderId, callbackUrl);

            if (StringUtils.isBlank(htmlPage)) {
                throw new TheiaServiceException("Risk Verification html page not present!");
            }
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(htmlPage);
        } catch (Exception e) {
            LOGGER.error("Exception in /api/v1/risk/showVerificationPage {}", e);
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                    .forward(request, response);
            return;
        }
    }

    /* Using this API to return static html to close iframe */
    @RequestMapping(value = "/usr/{verifyType}/verifier")
    public void showRiskVerficationRedirectionPage(RiskOAuthValidatedData riskOAuthValidatedData,
            HttpServletRequest request, HttpServletResponse response,
            @PathVariable(value = "verifyType") String verifyType) throws ServletException, IOException {
        LOGGER.info("Request received to show risk verification page.");
        try {
            String htmlPage = riskVerificationUtil.getRiskVerifierRedirectionPage(riskOAuthValidatedData);
            if (StringUtils.isBlank(htmlPage)) {
                throw new TheiaServiceException("Risk User Verifier redirection html page not present!");
            }
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(htmlPage);
        } catch (Exception e) {
            LOGGER.error("Exception in /api/v1/risk/usr/pwd/verifier {}", e);
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                    .forward(request, response);
            return;
        }
    }
}
