package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theiacommon.exception.TheiaServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PUSH_APP_DATA;

@RestController
public class Wallet2FASetPasscodeController {

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    private static final Logger LOGGER = LoggerFactory.getLogger(Wallet2FASetPasscodeController.class);

    @RequestMapping(value = TheiaConstant.ExtraConstants.WALLET_2FA_SET_PASSCODE_REDIRECT, method = {
            RequestMethod.POST, RequestMethod.GET })
    public void verifyCallback(final HttpServletRequest request, final HttpServletResponse response) {
        String respMsg = request.getParameter("statusMsg");
        String respStatus = request.getParameter("status");
        String txnToken = request.getParameter("state");

        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(txnToken);
        if (orderDetail == null) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid txnToken : " + txnToken);
        }

        LOGGER.info("Received hit at set TwoFA passcode Callback respMsg : {}, respStatus : {}, txnToken : {}",
                respMsg, respStatus, txnToken);
        String htmlPage = ConfigurationUtil.getWallet2FABankOauthSetPasscodePage();
        try {

            if (StringUtils.isNotBlank(htmlPage)) {
                Map<String, String> setWallet2FAPasscodeData = new HashMap<>();
                setWallet2FAPasscodeData.put("respMsg", respMsg);
                setWallet2FAPasscodeData.put("respStatus", respStatus);

                String setWallet2FAPasscodeDataConfigString = JsonMapper.mapObjectToJson(setWallet2FAPasscodeData);
                if (StringUtils.isNotBlank(setWallet2FAPasscodeDataConfigString)) {
                    htmlPage = htmlPage.replace(PUSH_APP_DATA, setWallet2FAPasscodeDataConfigString);
                }

                response.setContentType("text/html");
                response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
                return;
            }
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnCheckOutJsPage() + ".jsp").forward(
                    request, response);
            return;
        } catch (Exception e) {
            LOGGER.error("Something went wrong in SetTwoFAWalletPasscode Controller {} ", e);
        }
    }
}
