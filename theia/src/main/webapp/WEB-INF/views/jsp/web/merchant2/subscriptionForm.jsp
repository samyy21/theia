<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil, com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO"%>
<%@ include file="../../common/config.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta charset="utf-8">
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
		 
	<c:set var="cssPathComponent" value="${merchInfo.mid}" />
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
				catch (e) {}
				setTimeout("backButtonOverrideBody()", 500);
			}
			
			history.forward(0);
			
			
		</script>
		
		
		<%String useMinifiedAssets = ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant2/style.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
			
			<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant2/functions.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant2/style.min.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant2/functions.min.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
		<% } %>

		<div id="csrf" data-value="${csrfToken.token}"></div>
	</head>
	<body onload="backButtonOverride();">
		<%@ include file="../../common/common.jsp"%>
		
		<!-- Configurations -->
		<c:set var="isSubscription" value="true"/>
		<c:set var="wltAmnt" value='<%=request.getParameter("walletAmount") %>' /> 
		<c:set var="walletUsed" value="${wltAmnt > 0 ? true  : false}" />
		<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
		<div id="wallet-details" data-available="${!walletInfo.walletFailed}" data-balance="${walletInfo.walletBalance}" data-wallet-only="${onlyWalletEnabled}" data-wallet-used="${walletUsed}"></div>
		<div id="payment-details" data-amount="${txnInfo.txnAmount}" data-save-card-amount="${txnInfo.subscriptionMinAmount}" data-hybrid-allowed="${txnConfig.hybridAllowed}" data-cod-hybrid-allowed="${txnConfig.codHybridAllowed}"></div>
		<div id="addMoney-details" data-available="${txnConfig.addAndPayAllowed}" data-selected="${txnConfig.addMoneyFlag}"></div>
		<div id="login-details" data-value='${loginInfo.loginFlag}'></div>
		<div id="current-payment-type" data-value='${paymentType}'></div>
		<div id="error-details" 
			data-available="${ !empty requestScope.validationErrors ? true : false }"
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}"
		></div>
		<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
			<div id="promocode-details" 
				data-type="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}" 
				data-paymentmodes="${txnInfo.promoCodeResponse.promoCodeDetail.paymentModes}" 
				data-showallmodes="${promoShowAllModes}" 
				data-nblist="${txnInfo.promoCodeResponse.promoCodeDetail.nbBanks}" 
				data-cardlist="${txnInfo.promoCodeResponse.promoCodeDetail.cardBins}" 
				data-cardtypelist="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}" 
				data-errormsg="${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}" 
				data-checkPromoValidity="${txnInfo.promoCodeResponse.checkPromoValidityURL}"></div>
		</c:if>
		<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-subscription="true" data-subs-mode="${txnInfo.subscriptionPaymentMode}" data-subs-ppi-only="${txnInfo.subscriptionPPIOnly}"></div>
		<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>
			<div id="auth-js-details" 
		data-mid="${txnInfo.mid}"
		data-orderid="${txnInfo.orderId}"
		<%--data-txnInfo="${txnInfo}"--%>
		<%--data-loginInfo="${loginInfo}"--%>
		data-theme="${themeInfo.loginTheme}"
		data-retrycount="${loginInfo.loginRetryCount}"
	></div>
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
		
		<!-- cart section -->
		<%@ include file="cartSection.jsp" %>
		<!-- cart section -->

		
		<!-- notifications -->
		<div class="container notification-container">
			<%@ include file="notifications.jsp" %>
			
			<!-- Login notification -->
			<c:if test="${walletInfo.walletEnabled}">
				<%@ include file="login.jsp" %>
				</c:if>
			<!-- Login notification -->
		</div>
		<!-- notifications -->
		
		<!-- Subscription messages -->
		<div class="subs-msgs" >
			<h2 class="container mt20 mb20">
				<span class="subs-text subs-select-card hide">
					<span class="medium">Select and save your <b class="b">Credit Card</b> for subsequent transactions.</span>
					<div class="subs-min-amt-msg hide">
						<span class="grey-text small show mt6 ml20">(Rs <span class="saveCardAmount">${txnInfo.subscriptionMinAmount}</span> will be charged on your card to verify details)</span>
					</div>
				</span>
				<span class="subs-text subs-save-card hide">
					<span class="medium">Save your <b class="b">Credit Card</b> for subsequent transactions.</span>
					<br>
					<span class="grey-text small show mt6">(Rs <span class="saveCardAmount">${txnInfo.subscriptionMinAmount}</span> will be charged on your card to verify details)</span>
				</span>
				<span class="subs-text subs-complete-payment medium hide">
					<b class="fr mr20 ml10 large"><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b>
					Select an option to add money in <b class="b">Paytm</b> and complete payment for subsequent subscription transactions
				</span>
				<span class="subs-text subs-ppi-only-complete-payment medium hide">
					<b class="fr mr20 ml10 large"><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b>
					Select an option to add money in <b class="b">Paytm</b> and complete payment.<br>
					<span class="show mt6">For subsequent transaction, amount will be debited from your Paytm</span>
				</span>
				
				<span class="subs-text subs-only-full-wallet medium hide">
					For subsequent transaction, amount will be debited from your Paytm
				</span>
			</h2>
			<div class="hr"></div>
		</div>
		<!-- Subscription messages -->
			

		<!--Middle Container Start-->
		<div class="container mb50" id="payment-modes">
			
			<!-- <h1 class="otherText">Great, let's get the payment done!</h1>
			<h1 class="fullWalletDeduct" style = "display:none">Just confirm to pay through Paytm</h1> -->
			
			
			
			<!-- Merchant Payment Modes -->
			<c:set var="paymentConfig" value="${sessionScope}" />
			<c:if test="${!onlyWalletEnabled && (txnInfo.subscriptionPaymentMode eq 'CC' || txnInfo.subscriptionPaymentMode eq 'DC' )}">
				<div id="merchant-payment-modes" class="cards-tabs relative">
					<%@ include file="merchantPaymentModes.jsp" %>
				</div>
			</c:if>
			<!-- Merchant Payment Modes -->
			
			<!-- Add Money Payment Modes -->
			<%-- <span>${sessionScope.addMoneyPayModes}</span> --%>
			<c:if test="${txnConfig.addAndPayAllowed && txnInfo.subscriptionPaymentMode eq 'PPI'}">
				<c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
				<c:set var="isAddMoneyAvailable" value="true"/>
				<div id="add-money-payment-modes" class="cards-tabs">
						<%@ include file="addMoneyPaymentModes.jsp" %>
				</div>
			</c:if>
			<!-- Add Money Payment Modes -->

			<div id="footer-placeholder" class="hide">
			</div>			
		</div>
		<!--Middle Container End-->
		
		
		<!-- Footer Start -->
			<%@ include file="modals.jsp" %>
			<c:choose>
			      <c:when test="${themeInfo.subTheme eq 'nofooter'}">
			      </c:when>
			      <c:otherwise>
			      	<%@ include file="footer.jsp" %>
			      </c:otherwise>
			</c:choose>
		<!-- Footer End -->
		
		</div>
		<!-- Main Container -->
		
	</body>
</html>