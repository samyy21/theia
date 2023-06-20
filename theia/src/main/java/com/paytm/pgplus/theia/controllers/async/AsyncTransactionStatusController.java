package com.paytm.pgplus.theia.controllers.async;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.UPIPollStatus;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

@Controller
@Path("")
public class AsyncTransactionStatusController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(com.paytm.pgplus.theia.controllers.TransactionStatusController.class);

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    private static final JSONObject RETRY = new JSONObject();
    private static final JSONObject STOP_POLLING = new JSONObject();

    static {
        RETRY.put("POLL_STATUS", UPIPollStatus.POLL_AGAIN);
        STOP_POLLING.put("POLL_STATUS", UPIPollStatus.STOP_POLLING);
    }

    @Path(value = "/transactionStatusAsyncJ")
    @POST
    public void transactionStatus(@Suspended AsyncResponse asyncResponse, @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Async Request received for transactionStatus");
        try {
            processAsyncTxnStatusRequest(asyncResponse, request, response);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            asyncResponse.resume(Response.temporaryRedirect(
                    URI.create(theiaViewResolverService.returnErrorPage(request))).build());
        }
    }

    @Path(value = "v1/transactionStatusAsyncJ")
    @POST
    public void transactionStatusV1(@Suspended AsyncResponse asyncResponse, @Context HttpServletRequest request,
            @Context HttpServletResponse response, @Context UriInfo uriInfo) throws ServletException, IOException {
        LOGGER.info("Async Request received for v1/transactionStatus");
        try {
            processAsyncTxnStatusRequest(asyncResponse, request, response);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            asyncResponse.resume(Response.temporaryRedirect(
                    URI.create(theiaViewResolverService.returnErrorPage(request))).build());
        }
    }

    private void processAsyncTxnStatusRequest(AsyncResponse asyncResponse, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        transactionStatusServiceImpl.getAsyncCashierResponseWrapper(request, response, responseData -> {
            try {
                if (responseData == null) {
                    throw new TheiaControllerException("Cashier Payment Response data not fetched");
                } else if (TheiaConstant.RetryConstants.YES.equals(responseData
                        .get(TheiaConstant.RetryConstants.IS_REQUEST_ALREADY_DISPATCH))) {
                    return;
                } else if (TheiaConstant.RetryConstants.YES.equals(responseData
                        .get(TheiaConstant.RetryConstants.IS_RETRY))) {
                    String retryRequest = transactionStatusServiceImpl.generateRetryRequest(request, responseData);
                    // response.sendRedirect(retryRequest);
                asyncResponse.resume(Response.temporaryRedirect(URI.create(retryRequest)).build());
                return;
            } else {
                // response.getOutputStream().write(responseData.get(TheiaConstant.RetryConstants.RESPONSE_PAGE).getBytes());
                asyncResponse.resume(responseData.get(TheiaConstant.RetryConstants.RESPONSE_PAGE).getBytes());
                return;
            }
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
        } finally {
            LOGGER.info("Total time taken for {} is {} ms", "TransactionStatusController", System.currentTimeMillis()
                    - startTime);
        }
        // request.getRequestDispatcher(TheiaConstant.RetryConstants.ERROR_PAGE).forward(request,
        // response);
                asyncResponse.resume(Response.temporaryRedirect(
                        URI.create(theiaViewResolverService.returnErrorPage(request))).build());
            });
    }

    /*
     * @RequestMapping(value = "/upi/transactionStatus") public void
     * upiTransactionStatus(HttpServletRequest request, HttpServletResponse
     * response) throws IOException { long startTime =
     * System.currentTimeMillis(); response.setHeader("Content-Type",
     * "application/json"); response.setContentType("application/json");
     * 
     * try { Map<String, String> data =
     * transactionStatusServiceImpl.getUpiCashierResponse(request);
     * 
     * if (data == null) { response.getOutputStream().print(RETRY.toString());
     * return; }
     * 
     * LOGGER.info("UPI polling status : {}", data.get("POLL_STATUS")); if (null
     * != data.get("POLL_STATUS") &&
     * UPIPollStatus.POLL_AGAIN.getMessage().equalsIgnoreCase
     * (data.get("POLL_STATUS"))) {
     * response.getOutputStream().print(RETRY.toString()); return; }
     * 
     * response.getOutputStream().print(STOP_POLLING.toString()); return; }
     * catch (Exception e) { LOGGER.error("SYSTEM_ERROR : ", e);
     * response.getOutputStream().print(RETRY.toString()); return; } finally {
     * LOGGER.info("Total time taken to fetch UpiTransaction is {} ms",
     * System.currentTimeMillis() - startTime); } }
     * 
     * @RequestMapping(value = "/processRetry") public void
     * processRetryPayment(HttpServletRequest request, Locale locale,
     * HttpServletResponse response) throws ServletException, IOException { long
     * startTime = System.currentTimeMillis();
     * response.setContentType("text/html");
     * 
     * try { theiaSessionDataService.validateSession(request, true); boolean
     * processed = transactionStatusServiceImpl.getRetryResponse(request); if
     * (processed) { String viewName =
     * theiaViewResolverService.returnPaymentPage(request); StringBuilder path =
     * new StringBuilder();
     * path.append(TheiaConstant.RetryConstants.JSP_PATH).append
     * (viewName).append(".jsp");
     * request.getRequestDispatcher(path.toString()).forward(request, response);
     * return; } } catch (Exception e) { LOGGER.error("SYSTEM_ERROR :", e); }
     * finally { LOGGER.info(
     * "Total time taken for TransactionStatusController.processRetryPayment is {} ms"
     * , System.currentTimeMillis() - startTime); }
     * LOGGER.error("Something went wrong. Redirecting to Error Page");
     * request.getRequestDispatcher
     * (TheiaConstant.RetryConstants.ERROR_PAGE).forward(request, response);
     * return; }
     * 
     * @RequestMapping(value = "/abandonTransaction", method = {
     * RequestMethod.GET, RequestMethod.POST }) public String
     * abandonTransaction(HttpServletRequest request, HttpServletResponse
     * response, Model model, Locale locale) { long startTime =
     * System.currentTimeMillis(); try { PaymentRequestBean paymentRequestData =
     * new PaymentRequestBean(request);
     * LOGGER.info("Request received for cancel transaction : {}",
     * paymentRequestData);
     * 
     * if (!theiaSessionDataService.isSessionExists(request)) { LOGGER.warn(
     * "Session does not contains a valid transaction id queryString :{}",
     * request.getQueryString()); throw new
     * SessionExpiredException("Session does not contains a valid transaction id."
     * ); } TransactionInfo txnData =
     * theiaSessionDataService.getTxnInfoFromSession(request); MerchantInfo
     * merchantInfo =
     * theiaSessionDataService.getMerchantInfoFromSession(request);
     * Assert.notNull(txnData, "TransactionInfo obtained from session is null");
     * Assert.notNull(merchantInfo,
     * "MerchantInfo obtained from session is null"); ExtendedInfoRequestBean
     * extendedInfoRequestBean = theiaSessionDataService
     * .geExtendedInfoRequestBean(request);
     * 
     * String responsePage =
     * transactionStatusServiceImpl.fetchResponsePageForAbandonTransaction
     * (request, txnData, merchantInfo, extendedInfoRequestBean); if
     * (StringUtils.isNotBlank(responsePage)) {
     * theiaSessionDataService.setRedirectPageInSession(request, responsePage);
     * return theiaViewResolverService.returnForwarderPage(); }
     * LOGGER.error("Could not fetch redirect page"); } catch (Exception e) {
     * LOGGER.error("Exception Occurred while abandon transaction ", e); }
     * finally { LOGGER.info(
     * "Total time taken for Controller TransactionStatusController.abandonTransaction is {} ms"
     * , System.currentTimeMillis() - startTime); } return
     * theiaViewResolverService.returnOOPSPage(request); }
     * 
     * @RequestMapping(value = "/session-timeout", method = { RequestMethod.GET
     * }) public String scanAndPayTimeout(HttpServletRequest request,
     * HttpServletResponse response) { return
     * theiaViewResolverService.returnScanAndPayTimeout(request); }
     */
}