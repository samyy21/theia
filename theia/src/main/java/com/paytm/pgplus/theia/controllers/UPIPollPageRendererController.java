package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.UPIHandleInfo;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.enhancenative.NativeUpiData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.UPIPollResponse;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import com.paytm.pgplus.theia.utils.LocalizationUtil;
import com.paytm.pgplus.theia.utils.UPIPollPageUtil;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.UPI_POLL_PAGE_V1;

@Controller
public class UPIPollPageRendererController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIPollPageRendererController.class);

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private LocalizationUtil localizationUtil;

    @Autowired
    private UPIPollPageUtil upiPollPageUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @RequestMapping(value = "/upiPollPage", method = { RequestMethod.GET, RequestMethod.POST })
    public void renderPollPage(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final long startTime = System.currentTimeMillis();

        try {

            String cacheKey = request.getParameter("cacheKey");

            if (StringUtils.isBlank(cacheKey)) {
                LOGGER.error("Cache Key For UPI Data blank or null");
                returnOOpsPage(request, response);
                return;
            }

            Map<String, String> dataMap = (Map<String, String>) theiaTransactionalRedisUtil.get(cacheKey);

            if (MapUtils.isEmpty(dataMap)) {
                LOGGER.error("Map Data for cache key is Null or Empty");
                returnOOpsPage(request, response);
                return;
            }

            putParamsInSession(request, dataMap);

            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnUPIPollPage() + ".jsp").forward(
                    request, response);

            return;

        } catch (Exception exception) {

            LOGGER.error("SYSTEM_ERROR : ", exception);

        } finally {

            LOGGER.info("Total time taken for ProcessTransactionController is {} ms", System.currentTimeMillis()
                    - startTime);
        }

        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(
                request, response);

        return;
    }

    @LocaleAPI(apiName = UPI_POLL_PAGE_V1, responseClass = UPITransactionInfo.class, isResponseObjectType = false)
    @RequestMapping(value = "/api/v1/upiPollPage", method = { RequestMethod.POST })
    public void showUPIPollPage(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        LOGGER.info("Request received for loading UPI Poll page");

        NativeInitiateRequest nativeInitiateRequest = null;
        /*
         * In ssoToken type flow, we don't have orderdetail as there is no hit
         * for initiateTransaction
         */
        try {
            String txnToken = request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN);
            if (!TokenType.SSO.getType().equals(request.getParameter(TheiaConstant.RequestParams.Native.TOKEN_TYPE))) {
                nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
                String cachedMid = nativeInitiateRequest.getInitiateTxnReq().getBody().getMid();
                String cachedOrderId = nativeInitiateRequest.getInitiateTxnReq().getBody().getOrderId();

                nativeValidationService.validateMidOrderId(cachedMid, cachedOrderId);
            }
            NativeUpiData nativeUpiData = nativeSessionUtil.getNativeUpiData(txnToken);

            setUPIPollPageDataInResponseAndSession(request, nativeUpiData, response);
        } catch (Exception e) {
            LOGGER.error("Exception in /api/v1/upiPollPage {}", e);
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                    .forward(request, response);
            return;
        }
    }

    private void setUPIPollPageDataInResponseAndSession(final HttpServletRequest request,
            final NativeUpiData nativeUpiData, HttpServletResponse response) throws IOException, ServletException {

        if (nativeUpiData == null) {
            LOGGER.error("nativeUpiData=null, returning oops page");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(false).build();
        }

        final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request, true);
        MerchantInfo merchInfo = theiaSessionDataService.getMerchantInfoFromSession(request, true);

        if (txnInfo == null || merchInfo == null) {
            LOGGER.error("Unable to get txnInfo or merchInfo from Session , returning oops page");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isRetryAllowed(false).build();
        }

        UPIPollResponse cachedUpiPollData = nativeUpiData.getUpiPollResponse();
        MerchantInfo cachedMerchantInfo = nativeUpiData.getMerchantInfo();

        String mid = cachedUpiPollData.getBody().getContent().getMid();
        String orderId = cachedUpiPollData.getBody().getContent().getOrderId();
        String acquirementId = cachedUpiPollData.getBody().getContent().getTxnId();
        String cashierRequestId = cachedUpiPollData.getBody().getContent().getCashierRequestId();
        String txnAmount = cachedUpiPollData.getBody().getContent().getTxnAmount();
        String statusInterval = cachedUpiPollData.getBody().getContent().getTimeInterval();
        String statusTimeout = cachedUpiPollData.getBody().getContent().getTimeOut();
        String userVPA = cachedUpiPollData.getBody().getContent().getVpaID();
        boolean isPaytmVPA = cachedUpiPollData.getBody().getContent().isSelfPush();
        String merchantVPA = cachedUpiPollData.getBody().getContent().getMerchantVPA();
        MerchantVpaTxnInfo merchantVpaTxnInfo = cachedUpiPollData.getBody().getContent().getMerchantVpaTxnInfo();
        UPIHandleInfo upiHandleInfo = cachedUpiPollData.getBody().getContent().getUpiHandleInfo();

        txnInfo.setMid(mid);
        txnInfo.setOrderId(orderId);
        txnInfo.setTxnId(acquirementId);
        txnInfo.setVPA(merchantVPA);
        txnInfo.setTxnAmount(txnAmount);

        merchInfo.setMid(cachedMerchantInfo.getMid());
        merchInfo.setMerchantName(cachedMerchantInfo.getMerchantName());
        merchInfo.setMerchantImage(cachedMerchantInfo.getMerchantImage());
        merchInfo.setUseNewImagePath(cachedMerchantInfo.isUseNewImagePath());

        UPITransactionInfo upiTransactionInfo = new UPITransactionInfo();
        upiTransactionInfo.setStatusInterval(statusInterval);
        upiTransactionInfo.setStatusTimeOut(statusTimeout);
        upiTransactionInfo.setCashierRequestId(cashierRequestId);
        upiTransactionInfo.setPaytmVpa(isPaytmVPA);
        upiTransactionInfo.setTransactionAmount(txnAmount);
        upiTransactionInfo.setMerchantVpaTxnInfo(merchantVpaTxnInfo);
        upiTransactionInfo.setVpaID(userVPA);
        upiTransactionInfo.getMerchantVpaTxnInfo().setMaskedMerchantVpa(
                VPAHelper.setMaskedMerchantVpa(merchantVPA, merchInfo.getMid()));
        upiTransactionInfo.setUpiHandleInfo(upiHandleInfo);

        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = null;
        if (!TokenType.SSO.getType().equals(request.getParameter(TheiaConstant.RequestParams.Native.TOKEN_TYPE))) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request
                    .getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN));
            ultimateBeneficiaryDetails = nativeInitiateRequest.getInitiateTxnReq().getBody()
                    .getUltimateBeneficiaryDetails();
        }
        theiaSessionDataService.setUPITransactionInfoInSession(request, upiTransactionInfo);
        if (upiPollPageUtil.isUPIPollPageEnabledOnMid(txnInfo.getMid())
                && upiPollPageUtil.returnUPIHTMLPollPage(txnInfo, upiTransactionInfo, merchInfo, response,
                        ultimateBeneficiaryDetails)) {
            return;
        }
        theiaSessionDataService.updateBeneficiaryDetailsInMerchantInfo(request, ultimateBeneficiaryDetails);
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnUPIPollPage() + ".jsp").forward(
                request, response);
    }

    void returnOOpsPage(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(
                request, response);
    }

    void putParamsInSession(final HttpServletRequest request, final Map<String, String> dataMap) {

        String cacheKey = request.getParameter("cacheKey");

        String mid = dataMap.get("mid");
        String orderId = dataMap.get("orderId");
        String acquirementId = dataMap.get("acquirementId");
        String cashierRequestId = dataMap.get("cashierRequestId");
        String txnAmount = dataMap.get("txnAmount");
        String statusInterval = dataMap.get("statusInterval");
        String statusTimeout = dataMap.get("statusTimeout");
        String userVpa = dataMap.get("userVpa");
        boolean isPaytmVPA = Boolean.valueOf(dataMap.get("isPaytmVPA"));
        String merchantVPA = dataMap.get("merchantVPA");

        final TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request, true);

        txnInfo.setMid(mid);
        txnInfo.setOrderId(orderId);
        txnInfo.setTxnId(acquirementId);
        txnInfo.setVPA(merchantVPA);
        txnInfo.setTxnAmount(txnAmount);

        MerchantInfo merchInfo = theiaSessionDataService.getMerchantInfoFromSession(request, true);
        merchInfo.setMid(mid);

        UPITransactionInfo upiTransactionInfo = new UPITransactionInfo();
        upiTransactionInfo.setStatusInterval(statusInterval);
        upiTransactionInfo.setStatusTimeOut(statusTimeout);
        upiTransactionInfo.setCashierRequestId(cashierRequestId);
        upiTransactionInfo.setPaytmVpa(isPaytmVPA);
        upiTransactionInfo.setTransactionAmount(txnAmount);
        upiTransactionInfo.setVpaID(userVpa);
        MerchantVpaTxnInfo merchantVpaTxnInfo = new MerchantVpaTxnInfo();
        merchantVpaTxnInfo.setMaskedMerchantVpa(VPAHelper.setMaskedMerchantVpa(merchantVPA, merchInfo.getMid()));
        merchantVpaTxnInfo.setVpa(merchantVPA);
        upiTransactionInfo.setMerchantVpaTxnInfo(merchantVpaTxnInfo);

        theiaSessionDataService.setUPITransactionInfoInSession(request, upiTransactionInfo);
    }
}
