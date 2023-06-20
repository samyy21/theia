/**
 * 
 */
package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.theia.services.impl.TheiaViewResolverServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
public interface ITheiaViewResolverService extends Serializable {

    String returnOOPSPage(HttpServletRequest request);

    String returnErrorPage(HttpServletRequest request);

    String returnPaymentPage(HttpServletRequest request);

    String returnOAuthPage(HttpServletRequest request);

    String returnForwarderPage();

    String returnUPIPollPage();

    String returnRiskPaymentPage(HttpServletRequest request);

    String returnOAuthRedirectErrorPage(HttpServletRequest request);

    String returnOauthLoginRedirectPage();

    String returnLinkPaymentStatusPage(HttpServletRequest request);

    String returnKYCPage();

    String returnScanAndPayTimeout(HttpServletRequest request);

    String returnConfirmationPage(HttpServletRequest request);

    String returnNativeKycPage();

    String returnNpciReqPage();

    String returnNpciResPage();

    String returnCheckOutJsPage();

}
