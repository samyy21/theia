package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequest;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_KYC_MID_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_ORDER_ID_ENABLE;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeKYCController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeKYCController.class);

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private ValidationService validationService;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/submitKYC", method = { RequestMethod.POST })
    public NativeKYCDetailResponse validateKYC(@ApiParam(required = true) @RequestBody NativeKYCDetailRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /submitKYC is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeKYCDetailRequest, NativeKYCDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_KYC_REQUEST);
            NativeKYCDetailResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /submitKYC is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeKYCController is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/nativeKycPage", method = { RequestMethod.POST })
    public void renderNativeKycPage(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final long startTime = System.currentTimeMillis();

        try {

            HttpSession session = request.getSession();

            if (session == null) {
                throw new TheiaServiceException("Unable to set KYC-DATA due to the Session does not exist");
            }

            String _KYC_TXN_ID = request.getParameter(KYC_TXN_ID);
            String _KYC_FLOW = request.getParameter(KYC_FLOW);
            String _KYC_MID = request.getParameter(KYC_MID);
            String _KYC_ORDER_ID = request.getParameter(ORDER_ID);

            boolean mandatoryParamsMissing = StringUtils.isBlank(_KYC_TXN_ID) || StringUtils.isBlank(_KYC_FLOW)
                    || StringUtils.isBlank(_KYC_MID) || StringUtils.isBlank(_KYC_ORDER_ID);

            if (mandatoryParamsMissing) {
                LOGGER.error("mandatoryParamsMissing, returning oops page");
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(false).build();
            }

            request.setAttribute(KYC_TXN_ID, _KYC_TXN_ID);
            request.setAttribute(KYC_FLOW, _KYC_FLOW);
            request.setAttribute(KYC_MID, _KYC_MID);
            request.setAttribute(KYC_ORDER_ID, _KYC_ORDER_ID);

            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
                    && !validationService.validateOrderId(request.getParameter(ORDER_ID))) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid ORDER_ID : " + request.getParameter(ORDER_ID));
            }
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_KYC_MID_ENABLE, false)
                    && !validationService.validateKycMid(request.getParameter(KYC_MID))) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid KYC_MID : " + request.getParameter(KYC_MID));
            }

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("mid=").append(request.getParameter(KYC_MID));
            queryBuilder.append("&orderId=").append(request.getParameter(ORDER_ID));

            request.setAttribute("queryStringForSession", queryBuilder.toString());

            LOGGER.info("Loading kyc page for native json request in /api/v1/nativeKycPage");

            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnNativeKycPage() + ".jsp").forward(
                    request, response);
            nativePaymentUtil.logNativeResponse(null);
            return;

        } catch (Exception e) {
            LOGGER.error("Exception in /api/v1/nativeKycPage {}", e);
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                    .forward(request, response);
            return;
        } finally {
            LOGGER.info("Total time taken for /api/v1/nativeKycPage is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}