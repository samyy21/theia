package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitRequest;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponse;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponseBody;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PostTransactionSplitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostTransactionSplitController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "/postTransactionSplit", method = { RequestMethod.POST })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    public PostTransactionSplitResponse postTransactionSplit(
            @ApiParam(required = true) @RequestBody PostTransactionSplitRequest request,
            HttpServletRequest serverRequest) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /postTransactionSplit is: {}", serverRequest);
            setServerRequest(request, serverRequest);
            IRequestProcessor<PostTransactionSplitRequest, PostTransactionSplitResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.POST_TRANSACTION_SPLIT_REQUEST);
            PostTransactionSplitResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /postTransactionSplit is: {}", response);
            return response;
        } catch (Exception e) {
            LOGGER.error("Exception occured at API: /postTransactionSplit: {}", e.getMessage());
            PostTransactionSplitResponseBody responseBody;
            if (e instanceof BaseException && ((BaseException) e).getResultInfo() != null) {
                responseBody = new PostTransactionSplitResponseBody(new ResultInfo(((BaseException) e).getResultInfo()
                        .getResultStatus(), ((BaseException) e).getResultInfo().getResultCodeId(), ((BaseException) e)
                        .getResultInfo().getResultMsg()));
            } else {
                responseBody = new PostTransactionSplitResponseBody(new ResultInfo("F",
                        ResponseConstants.SYSTEM_ERROR.getCode(), ResponseConstants.SYSTEM_ERROR.getMessage()));
            }
            return new PostTransactionSplitResponse(new ResponseHeader(request.getHead().getVersion()), responseBody);
        } finally {
            LOGGER.info("Total time taken for /postTransactionSplit is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void setServerRequest(PostTransactionSplitRequest request, HttpServletRequest serverRequest) {
        if (StringUtils.isNotBlank(serverRequest.getParameter("mid"))) {
            request.getBody().setMid(serverRequest.getParameter("mid"));
        }
        if (StringUtils.isNotBlank(serverRequest.getParameter("orderId"))) {
            request.getBody().setOrderId(serverRequest.getParameter("orderId"));
        }
    }
}
