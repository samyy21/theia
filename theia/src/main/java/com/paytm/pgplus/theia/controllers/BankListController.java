//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.common.model.EnvInfoRequestBean;
//import com.paytm.pgplus.theia.helper.BankListRequestHelper;
//import com.paytm.pgplus.theia.models.BankListRequest;
//import com.paytm.pgplus.theia.services.IBankListService;
//import com.paytm.pgplus.theia.viewmodel.BankInfo;
//import org.apache.http.HttpStatus;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//
//@Controller
//public class BankListController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(BankListController.class);
//
//    private static final String INVALID_JSON_CODE = "CMER-JSE101";
//    private static final String UNSUPPORTED_ENCODING_CODE = "CMER-USEC101";
//    private static final String INTERNAL_PROCESSING_ERROR_CODE = "CMER-IP101";
//
//    @Autowired
//    @Qualifier("bankListService")
//    private IBankListService bankListService;
//
//    @RequestMapping(value = "/HANDLER_INTERNAL/BANK_LIST", method = { RequestMethod.GET, RequestMethod.POST })
//    public void fetchBankList(final HttpServletRequest request, final HttpServletResponse httpServletResponse)
//            throws IOException {
//
//        long startTime = System.currentTimeMillis();
//        String errorCode = null;
//        String errorMsg = null;
//        String responseData = null;
//        try {
//
//            BankListRequest bankListRequest = BankListRequestHelper.createBankListRequest(request);
//
//            if (bankListRequest != null && bankListRequest.getErrorCode() == null) {
//                LOGGER.info("Request received at FetchBankListController for MID : ", bankListRequest.getMid());
//                EnvInfoRequestBean envInfoRequestBean = BankListRequestHelper.createEnvInfo(request,
//                        bankListRequest.getChannel());
//                List<BankInfo> bankInfoList = bankListService.fetchAvailableBankList(bankListRequest,
//                        envInfoRequestBean);
//                responseData = BankListRequestHelper.getBankList(bankInfoList);
//            } else if (bankListRequest != null) {
//                errorCode = bankListRequest.getErrorCode();
//                errorMsg = bankListRequest.getErrorMsg();
//            }
//
//        } catch (JSONException e) {
//            LOGGER.error("Error: ", e);
//            errorCode = INVALID_JSON_CODE;
//            errorMsg = "Invalid JSON Data";
//        } catch (UnsupportedEncodingException e) {
//            LOGGER.error("Error: ", e);
//            errorCode = UNSUPPORTED_ENCODING_CODE;
//            errorMsg = "Invalid Request Format";
//        } catch (Exception e) {
//            LOGGER.error("Error: ", e);
//            errorCode = INTERNAL_PROCESSING_ERROR_CODE;
//            errorMsg = "Internal Processing Error";
//        } finally {
//            LOGGER.info("Total time taken for fetchBankList Controller is {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//
//        httpServletResponse.setContentType("text/plain");
//
//        if (errorCode != null) {
//            LOGGER.info("API error: ", errorCode);
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("ErrorCode", errorCode);
//            jsonObject.put("ErrorMsg", errorMsg);
//            httpServletResponse.getWriter().print(jsonObject);
//
//        } else {
//            if (responseData == null)
//                httpServletResponse.setStatus(HttpStatus.SC_REQUEST_TIMEOUT);
//            httpServletResponse.getWriter().print(responseData);
//        }
//
//        httpServletResponse.getWriter().close();
//
//    }
//
// }
