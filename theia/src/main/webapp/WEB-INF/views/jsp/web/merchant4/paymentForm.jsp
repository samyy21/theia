<%@ page import="com.paytm.pgplus.theia.utils.ConfigurationUtil" %>

<%@ include file="../../common/config.jsp" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Paytm Secure Online Payment Gateway</title>
	<meta charset="utf-8">
	<meta name="pagepath" content="${pagepath}" />
	<meta name="robots" content="noindex,nofollow" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Expires" content="-1" />
	<meta http-equiv="x-ua-compatible" content="IE=9" />
	<meta name="HandheldFriendly" content="True">
	<meta name="MobileOptimized" content="320">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta http-equiv="cleartype" content="on">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">

	<c:set var="cssPathComponent" value="${mid}" />
	<c:if test="${empty header['PRX']}">
		<c:set var="cssPathComponent" value="default" />
	</c:if>
	<link type="image/x-icon" rel="shortcut icon" href="${ptm:stResPath()}images/web/Paytm.ico" />

	<script type="text/javascript">
        function backButtonOverride() {
            setTimeout("backButtonOverrideBody()", 1);
        }

        function backButtonOverrideBody() {
            try {
                history.forward();
            }
            catch (e) {
            }
            setTimeout("backButtonOverrideBody()", 500);
        }

        history.forward(0);

		//Added GA Function
		function pushGAData(ele, actionType, data){
			var authConfig=JSON.parse($("#auth-config")[0].getAttribute('data-value'));
			var eventLabel=ele ? $(ele)[0].getAttribute('data-txnmode') : null;

			if( actionType === 'exception_visible' ) {
				eventLabel = data;
			}

			// if(dataLayer && authConfig){
			// 	dataLayer.push({
			// 		"event": "custom_event",
			// 		"event_category" : 'payment_gateway_idea',
			// 		"event_action" : actionType,
			// 		"event_label" : eventLabel,
			// 		"order_id" : authConfig.ORDERID,
			// 		"mid" : authConfig.MID,
			// 		"user_agent" : navigator.userAgent,
			// 		"vendor" : 'idea',
			// 		"vertical_name" : "payment_gateway_idea"
			// 	});

			// }
		}
	</script>

	<c:set var="showQRCode" value="${txnInfo.qrDetails.isQREnabled eq true || txnConfig.dynamicQR2FA eq true}"></c:set>
	<c:if test="${showQRCode ne true}">

		<style>
			.container{
				width:1200px !important;
			}
		</style>
	</c:if>
	<script>
        var MERCHANT_USER_ID='';
	</script>
	<c:if test="${not empty loginInfo.user}">
		<script>
            var MERCHANT_USER_ID='${loginInfo.user.payerUserID}';
		</script>
	</c:if>

	<script>
        var PG_ANALYTICS={}, RESTART_CHECK_JS=false,URL_RESTART_CHECK='';
        PG_ANALYTICS.mId='${txnInfo.mid}';
        PG_ANALYTICS.orderId="${txnInfo.orderId}";
        PG_ANALYTICS.promoCodeResponseMsg="${txnInfo.promoCodeResponse.resultMsg}";
        PG_ANALYTICS.logToConsole="${txnConfig.uiEventLogToConsole}";
        PG_ANALYTICS.logToServer="${txnConfig.uiEventLogToServer}";
        PG_ANALYTICS.theme="merchant4";
	</script>
	<%String useMinifiedAssets=ConfigurationUtil.getProperty("context.useMinifiedAssets");%>
	<% if(useMinifiedAssets.equals("N")){ %>
	<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant4/style.css" />

	<!--[if lte IE 9 ]>
	<script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
	<![endif]-->

	<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
	<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
	<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
	<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>
	<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant4/functions.js"></script>
	<!-- <script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js" defer></script> -->
	<%-- TODO put this js in the end 	 --%>
	<c:if test="${txnConfig.uiEventLogToServer=='true'}">
		<%--<script type="text/javascript" src="${ptm:stResPath()}js/analytic.js"></script>--%>
		<script>

            window.URL_RESTART_CHECK='${ptm:stResPath()}js/web/merchant4/functions.js';
		</script>
	</c:if>
	<% } else { %>
	<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant4/style.min.css" />

	<!--[if lte IE 9 ]>
	<script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
	<![endif]-->

	<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant4/functions.min.js"></script>
	<!-- <script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js" defer></script> -->
	<c:if test="${txnConfig.uiEventLogToServer=='true'}">
		<%--<script type="text/javascript" src="${ptm:stResPath()}js/analytic.js"></script>--%>
		<script>

            window.URL_RESTART_CHECK='${ptm:stResPath()}js/web/merchant4/functions.min.js';
		</script>
	</c:if>
	<% } %>

	<c:if test="${showQRCode}">
		<script type="text/javascript" src="${ptm:stResPath()}vendor/socket.io/socket.io.min.js"></script>
	</c:if>
	<c:set var="themeInfo.subTheme" value="${themeInfo.subTheme}"></c:set>
	<%-- custom css for different merchants --%>
	<c:if test="${themeInfo.subTheme eq 'airtel'}">
		<style>
			.btn-submit input {
				background-image: url("${ptm:stResPath()}images/web/merchant4/sprite_compressed_airtel.png") !important;
			}
		</style>
	</c:if>

	<c:if test="${themeInfo.subTheme eq 'paytmIframe'}">
		<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant4/style.paytmIframe.css" />
	</c:if>

	<c:if test="${themeInfo.subTheme eq 'recharge'}">
		<style>
			.deleteCard {
				display: block;
			}

			.c {
				background-image:url(${ptm:stResPath()}images/web/merchant4/cc-paytm.png);
				background-position:287px;
			}
		</style>
	</c:if>

	<c:if test="${themeInfo.subTheme  eq 'mts'}">
		<style>
			#sc-card,#ccStoreCardWrapper,#dcStoreCardWrapper

			{display:none;}
		</style>
	</c:if>

	<c:if test="${!txnInfo.onus}">
		<!-- Offus merchant style -->
		<style>
			.myaccount{
				padding:22px !important;
			}
			#overlayBox
			{
				border-radius:0;
				-webkit-border-radius:0;
				-moz-border-radius:0;
			}
			.offus-mobile-bg{
				color: black !important;
			}

			.main-container{
				width: 1198px;
				margin:  50px auto 0;
				border: 1px solid #ccc;
			}

			.container{
				width: 1198px;
				margin:-1px auto 0 ;
				border-radius: 0;
				border-left:0;
				border-right: 0;
			}

			#header{
				background-color:white;
				margin-bottom: -10px;
			}

			.rm-border{
				border:0 !important;
			}

			.offus-bold{
				font-size: 18px !important;
				font-weight: bold;
			}
		</style>
	</c:if>
	<c:if test="${merchInfo.mid ne 'IRCTCe231206309' && merchInfo.mid ne 'IRCTCw862137775'}">
		<c:set var="isSignup" value="true" />
		<!-- FOR OAUTH SIDE CHECK  -->
		<div id="isSignup" data-issignup="true"></div>
	</c:if>
	<div id="csrf" data-value="${csrfToken.token}"></div>
</head>
<body onload="backButtonOverride();">
	<c:set var="isSubscriptionFlow" value="false"></c:set>
<div class="bgstript"></div>
<%@ include file="../../common/common.jsp"%>


		<!-- Paytm Wallet and Cards-->

		<c:set var="paymentBankEnabled" value="${savingsAccountInfo.savingsAccountEnabled}"></c:set>
		<c:set var="paytmBankCode" value="${savingsAccountInfo.paymentsBankCode}"></c:set>
		<c:set var="paymentBankInactive" value="${savingsAccountInfo.savingsAccountInactive}"></c:set>
		<c:set var="paymentBankBalance" value="${savingsAccountInfo.effectiveBalance}"></c:set>
		<c:set var="paymentBankInvalidPassCodeMessage" value="${savingsAccountInfo.invalidPassCodeMessage}"></c:set>
		<c:set var="blankPassCodeMsg" value="${requestScope.validationErrors['INVALID_PASS_CODE_BLANK']}"></c:set>
		<c:set var="isPPBL_addMoneyEnable" value="${entityInfo.addPaymentsBankEnabled}"></c:set>
		<c:set var="isPPBL_merchantPayModeEnable" value="${entityInfo.paymentsBankEnabled}"></c:set>

		<div id="isPPBLE_AddMoney" data-value="${entityInfo.addPaymentsBankEnabled}"></div>
		<div id="isPPBLE_merchantPaymodes" data-value="${entityInfo.paymentsBankEnabled}"></div>

		<%--<c:set var="paymentBankEnabled" value="false"></c:set>--%>

<c:if test="${isPPBL_addMoneyEnable  || isPPBL_merchantPayModeEnable}">
	<div id="paytmBank" data-bankenabled="${savingsAccountInfo.savingsAccountEnabled}" data-bankinactive="${savingsAccountInfo.savingsAccountInactive}" data-paytmbankbalance="${savingsAccountInfo.effectiveBalance}" data-paytmpasscdemsg="${savingsAccountInfo.invalidPassCodeMessage}"></div>

</c:if>

<!-- Configurations -->
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>

<c:set var="wltAmnt" value="${txnInfo.walletAmount}" />
<c:set var="walletUsed" value="${txnInfo.walletAmount > 0 ? true  : false}" />
<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>

<!-- For payment retry purpose -->
<div id="retryPaymentMode"
	 data-value='${retryPaymentInfo.paymentMode}'>
</div>

<div id="wallet-details"
	 data-available="${walletInfo.walletFailed==false || walletInfo.walletFailed=='false'}"
	 data-balance="${walletInfo.walletBalance}"
	 data-wallet-only="${onlyWalletEnabled}"
	 data-wallet-used="${walletUsed}">
</div>
<div id="payment-details"
	 data-amount="${txnInfo.txnAmount}"
	 data-limeroad="${themeInfo.subTheme  eq 'limeroad' ? true : false}"
	 data-hybrid-allowed="${txnConfig.hybridAllowed}"
	 data-cod-hybrid-allowed="${txnConfig.codHybridAllowed}"
	 data-offline-paymode="${chequeDDNeftEnabled}"
	 data-idebit-ic ="${entityInfo.iDebitEnabled}"
></div>

<div id="addMoney-details"
	 data-available="${txnConfig.addAndPayAllowed}"
	 data-selected="${txnConfig.addMoneyFlag}"></div>
<div id="login-details"
	 data-value='${loginInfo.loginFlag}' data-autoLogin="${loginInfo.autoLoginAttempt}">
</div>
<div id="current-payment-type" data-value='${paymentType}'></div>
<div id="error-details"
	 data-available="${ !empty requestScope.validationErrors ? true : false }"
	 data-maintenancemsg="${messageInfo.maintenanceMessage}"
	 data-lowerrormsg="${messageInfo.lowPercentageMessage}"
></div>
<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
	<div id="promocode-details"
		 data-type="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}"
		 data-paymentmodes="${txnInfo.promoCodeResponse.paymentModes}"
		 data-showallmodes="${promoShowAllModes}"
		 data-nblist="${txnInfo.promoCodeResponse.nbBanks}"
		 data-cardlist="${txnInfo.promoCodeResponse.promoCodeDetail.cardBins}"
		 data-cardtypelist="${txnInfo.promoCodeResponse.promoCodeDetail.promoCardType}"
		 data-errormsg="${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}"
		 data-checkPromoValidity="${txnInfo.promoCodeResponse.checkPromoValidityURL}"
		 data-mid="${merchInfo.mid}"
		 data-txn="${txnInfo.paymentTypeId}"
		 data-promocode="${txnInfo.promoCodeResponse.promoCodeDetail.promoCode}"
		 data-promoBins="${txnInfo.promoBins}">
	</div>
</c:if>
<div id="auth-js-details"
	 data-mid="${txnInfo.mid}"
	 data-orderid="${txnInfo.orderId}"
	 data-txnInfo="${txnInfo}"
	 data-loginInfo="${loginInfo}"
	 data-theme="${themeInfo.loginTheme}"
	 data-retrycount="${loginInfo.loginRetryCount}"
	 data-userId = "${loginInfo.user.userID}"></div>
<c:if test="${digitalCreditInfo.digitalCreditEnabled eq true}">
	<div id="paytmCC-errorMsg" data-iserror="${not empty digitalCreditInfo.invalidPassCodeMessage}"></div>
	<div id="paytmCard" data-usepaytmCard="${digitalCreditInfo.digitalCreditEnabled}" data-inactivepaytmcard="${digitalCreditInfo.digitalCreditInactive}"></div>
</c:if>


<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-save-card-mandatory="${cardInfo.cardStoreMandatory}"></div>
<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>


<div id="paymentCharges" data-value="${!empty txnConfig.paymentCharges ? true : false}"></div>
<div id="ir-addMoneyCharge" data-iramount="${txnConfig.paymentCharges.PPI.totalTransactionAmount}"></div>

<div id="irctcDefaultFee" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
<div id="irctcOnlyWallet" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
<div id="irctcHybridWallet" data-bm="${txnConfig.paymentCharges.HYBRID.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.HYBRID.text}" data-tm="${txnConfig.paymentCharges.HYBRID.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.HYBRID.totalConvenienceCharges}">	</div>
<!-- Configurations -->

<div id="post-conv-details"
	 data-enabled= "${txnConfig != null &&  txnConfig.paymentCharges != null}"
	 data-CREDIT_CARD-fee = "Pay <span class='WebRupee white'>Rs</span> ${txnConfig.paymentCharges.CC.totalTransactionAmount}"
	 data-DEBIT_CARD-fee = "Pay <span class='WebRupee white'>Rs</span> ${txnConfig.paymentCharges.DC.totalTransactionAmount }"
	 data-UPI-fee = "Pay <span class='WebRupee white'>Rs</span> ${txnConfig.paymentCharges.UPI.totalTransactionAmount }"
	 data-ppi-fee = "${txnConfig.paymentCharges.PPI.totalTransactionAmount }"
></div>


<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-save-card-mandatory="${cardInfo.cardStoreMandatory}"></div>
<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>

<c:if test="${loginInfo.loginWithOtp eq true}">
	<div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>
</c:if>

<c:if test="${merchInfo.mid ne 'IRCTCe231206309' && merchInfo.mid ne 'IRCTCw862137775'}">

	<c:set var="isSignup" value="true" />
	<!-- FOR OAUTH SIDE CHECK  --->
	<div id="isSignup" data-issignup="true"></div>

</c:if>

<div id="isAddNPayAllowId" data-value="${txnConfig.addAndPayAllowed}"></div>
<div id="tlsWarnMsgDivId" data-value="${messageInfo.merchantTLSWarnMsg}"></div>
<div id="tlsWarnMsgAddNPayDivId" data-value="${messageInfo.addAndPayTLSWarnMsg}"></div>

<div id="merchantDetail" data-name="${merchInfo.merchantName}"></div>
<!-- Configurations -->

<!-- Main Container -->
<div class="main-container">

	<!--Header Start-->
	<c:choose>

		<c:when test="${themeInfo.subTheme eq 'noheader'}">
		</c:when>
		<c:when test="${themeInfo.subTheme eq 'paytmIframe'}">
		</c:when>
		<c:otherwise>
			<%@ include file="header.jsp" %>
		</c:otherwise>
	</c:choose>
	<!--Header End-->





	<!--  for PCF Merchants-->

	<!-- notifications -->
	<div class="notification-container mt10">
		<div class="container" >
			<%@ include file="notifications.jsp"%>
			<div class="clear"></div>
		</div>
	</div>
	<!-- notifications -->

	<c:if test="${!empty txnConfig.paymentCharges}">
		<div class="container layout-box" >
			<%@ include file="paymentCharges.jsp" %>
			<div class="clear"></div>
		</div>
	</c:if>

	<c:if test="${empty txnConfig.paymentCharges}">
			<div class="container layout-box" >
				<%@ include file="summary.jsp" %>
				<div class="clear"></div>
			</div>
	</c:if>

	<c:if test="${showQRCode && !loginInfo.loginFlag}">
		<!-- Scan pay -->
		<div class="container layout-box relative rm-border">
			<%@ include file="scannpay.jsp"%>
		</div>
		<!-- Scan pay -->
	</c:if>

	<c:if test="${loginInfo.loginFlag && (walletInfo.walletEnabled || (isPPBL_addMoneyEnable || isPPBL_merchantPayModeEnable) || digitalCreditInfo.digitalCreditEnabled)}">
	<!--Middle Container Start-->
		<div class="container" id="overlayBox">
				<%@ include file="paytmWallets.jsp" %>
				<div class="clear"></div>
		</div>
	</c:if>

	<div class="container margin-top-20" id="overlayBox" style="margin-top:20px;">
        <c:if test="${isPgPaymodesAvaiable eq true}">
            <c:if test="${showQRCode && !loginInfo.loginFlag && !onlyWalletEnabled}">
                <!-- Scanpay  -->
                <div  id="scanpayToggleAccordion" class="mt10">
                    <h2>
                        <span>
                            <div class="row medium" style="font-size: 18px;">
                                or Pay using other payment options
                                <span id="downArrowId" class="hide" style="float:right;"><img src="${ptm:stResPath()}images/down-arrow.png" width="20px" height="20px"/></span>
                                <span id="rightArrowId"  style="float:right;"><img src="${ptm:stResPath()}images/right-arrow.png" width="20px" height="20px"/></span>
                            </div>
                        </span>
                    </h2>
                </div>
            </c:if>
            <c:if test="${entityInfo.reseller eq false && ((showQRCode && loginInfo.loginFlag) || !showQRCode )}">
                    <div  id="no-walletTxt" class="large mt20 no-walletTextUpdate hide">
                        <h2 class="mt10">
                                            <span class="addMoneyText msg">
                                                <span class="medium"><div class="row medium" style="
                                        font-size: 18px;
                                    ">Please select a payment method</div>
                                              </span>
                                              </span></h2>
                        <div class="topBorderPaytmCC mt20" style="
                                        margin-left: -21px;
                                        width: 100%;
                                        padding-left: 42px;
                                        border-top: 1px solid rgb(239, 239, 239);
                                    "></div>
                    </div>
            </c:if>
        </c:if>

		<!-- paytm cash card -->


		<!-------------------END  Payment Amount ----------------- -->

		<!-- Login notification -->
		<c:if test="${!showQRCode && !loginInfo.loginFlag && !loginInfo.loginDisabled}">
			<%@ include file="login.jsp" %>
		</c:if>
		<!-- Login notification -->

        <c:if test="${isPgPaymodesAvaiable eq true}">
            <div id="paymentOptionId" >
                <!-- Merchant Payment Modes -->
            <c:set var="paymentConfig" value="${sessionScope}" />
            <c:if test="${!onlyWalletEnabled}">
                <div id="merchant-payment-modes" class="cards-tabs relative">
                    <%@ include file="merchantPaymentModes.jsp"%>
                </div>
            </c:if>
            <!-- Merchant Payment Modes -->

            <!-- Add Money Payment Modes -->
            <c:if test="${txnConfig.addAndPayAllowed}">
                <c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
                <c:set var="isAddMoneyAvailable" value="true" />
                <div id="add-money-payment-modes" class="cards-tabs">
                    <%@ include file="addMoneyPaymentModes.jsp"%>
                </div>
            </c:if>
            <!-- Add Money Payment Modes -->
            </div>
        </c:if>
		<div id="footer-placeholder" class="hide"></div>
		<!-- Footer Start -->
		<c:choose>
			<c:when test="${themeInfo.subTheme eq 'nofooter'}">
			</c:when>
			<c:otherwise>
				<%@ include file="footer.jsp" %>
			</c:otherwise>
		</c:choose>
		<!-- Footer End -->
	</div>
	<!--Middle Container End-->

	<%@ include file="modals.jsp" %>


</div>


<c:if test="${txnConfig.dynamicQR2FA eq true}">
	<div id="pnrScan" style="position: fixed;top: 0;left: 0;height: 100%;width: 100%;" class="hide"><div class="bgDisabled"></div>
		<div class="pnrCode">
			<div class="title">Enter Paytm reference number</div>
			<div class="formBox">
				<span class="payment-reference-no">Payment Reference Number</span>
				<input type="text" id="prnId" class="input" maxlength="8" name="prn" placeholder="" />
				<div id="prnValidateError" style="margin-top:10px;color:#fd5c5c;"></div>
				<input type="submit" id="prnSubmitId" class="paynow" value="Verify"/>
			</div>
			<div class="infoTxt">
				<img style="position: absolute;left: 0;" src="${ptm:stResPath()}images/web/tip.png" alt="Tip"/>
				<span style="font-size: 12px;">
					Payment Reference No is an 8 digit No displayed
					on payment confirmation screen in your APP.
				</span>
				<br />
				<a href="javascript:void(0);" class="prn-learnmore" style="color: #00b9f5;"> Learn More</a>
				<img class="prn-learnmore-image hide" src="${ptm:stResPath()}images/web/prn-learn-more.png" alt="Payment Reference Number Learn More" width="350" height="400"/>
			</div>
		</div>
	</div>
</c:if>
<!-- Main Container -->
<script>
    //Check if js is properly loaded
    var counterToCheck=10;
    var cl= setInterval(function(){
        if(window.RESTART_CHECK_JS || counterToCheck==0){
            clearInterval(cl);
        } else {
            counterToCheck--;
            if(!window.RESTART_CHECK_JS && counterToCheck!=0) {
                // load js again every three seconds
                (function(){
                    var f = document.createElement('script');
                    f.type = 'text/javascript';
                    f.async = false;

                    f.src = window.URL_RESTART_CHECK;
                    var s = document.getElementsByTagName('script')[0];
                    s.parentNode.insertBefore(f,s);
                })();
            };
        }
    },3000);


</script>
<script>
	// scanpay
    window.scanNpayURL="${ptm:stScanPath()}";
    window.transactionStatusEndpoint = "${ConfigurationUtil.getProperty('transaction.status.endpoint')}";
    window.isQRCodeEnabled = "${showQRCode}" === "true";
    window.scanPayTimeout = parseInt("${ConfigurationUtil.getProperty('scanpay.transaction.timeout')}");
    window.prnEnabled = "${txnConfig.dynamicQR2FA}" === "true";
</script>


</body>
</html>