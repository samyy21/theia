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
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant/style.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
			
			<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant/functions.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant/style.min.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant/functions.min.js"></script>			
						<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>	
		<% } %>
		
		<c:set var="themeInfo.subTheme" value="${themeInfo.subTheme}"></c:set>
		<%-- <c:set var="themeInfo.subTheme" value="airtel"></c:set> --%>
		
		<%-- custom css for different merchants --%>
		<c:if test="${themeInfo.subTheme eq 'airtel'}">
			<style>
				.btn-submit input {
					background-image: url("${ptm:stResPath()}images/web/merchant/sprite_compressed_airtel.png") !important;
				}
			</style>
		</c:if>
		<c:if test="${themeInfo.subTheme  eq 'mts'}">
			<style>
				#sc-card{display:none;}
			</style>
		</c:if>
		
	</head> 
	<body onload="backButtonOverride();">
		<%@ include file="../../common/common.jsp"%>
		<!-- Configurations -->
		<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
		<div id="wallet-details" data-available="${walletInfo.walletFailed==false || walletInfo.walletFailed=='false'}" data-balance="${walletInfo.walletBalance}" data-wallet-only="${onlyWalletEnabled}"></div>
		<div id="payment-details" data-amount="${txnInfo.txnAmount}" data-save-card-amount="${txnInfo.subscriptionMinAmount}"></div>
		<div id="addMoney-details" data-available="${txnConfig.addAndPayAllowed}" data-selected="${txnConfig.addMoneyFlag}"></div>
		<div id="login-details" data-value="${loginInfo.loginFlag}"></div>
		<div id="current-payment-type" data-value='${paymentType}'></div>
		<div id="error-details" 
			data-available="${ !empty requestScope.validationErrors ? true : false }"
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}"
		></div>
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

	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
		<!-- Configurations -->
		
		<!-- Main Container -->
		<div class="main-container">
		
		<!--Header Start-->
			<c:choose>
			      <c:when test="${themeInfo.subTheme eq 'noheader'}">
			      </c:when>
			      <c:otherwise>
			      	<%@ include file="header.jsp" %>
			      </c:otherwise>
			</c:choose>
		<!--Header End-->

		<!--Middle Container Start-->
		<div class="container">
			
			<!-- <h1 class="otherText">Great, let's get the payment done!</h1>
			<h1 class="fullWalletDeduct" style = "display:none">Just confirm to pay through Paytm Wallet</h1> -->
			
			<!-- notifications -->
			<%-- <c:if test ="${loginInfo.loginFlag ne 'Y'}">
				<div class="notification alert mt10">
					Please Sign In to complete this transaction 
				</div>
			</c:if> --%>
			<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user}">
				<div class="notification-container">
					<%@ include file="notifications.jsp" %>
				</div>
			</c:if>
			<!-- notifications -->
			

			<!-- summary card -->
			<div class="card summary-card">
				<c:if test="${!empty merchInfo.merchantImage}">
				<div id="merchant-logo" class="fl mt10 mr20 mb20">
					<img src="${ptm:stResPath()}images/web/merchant/${merchInfo.merchantImage}" alt="" height="20"/>
				</div>
				</c:if>

				<ul class="grid mt10 mb5">
				  <c:set var="merchantName" value="${fn:toUpperCase(merchInfo.merchantName)}"></c:set>
				  <c:set var="subsHeaderText" value="SUBSCRIPTION ON "></c:set>
				  <c:if test="${themeInfo.subTheme eq 'airtel'}">
				  	<c:set var="subsHeaderText" value=""></c:set>
				  </c:if>
			      <li class="merchant-name medium b">${subsHeaderText}${merchInfo.merchantName}</li>
			      <li class="fr large b">
			      	<span class="WebRupee">Rs.</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
			      	<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
			      </li>
			      <li class="clear"></li>
			    </ul>
			    
			    <div id="cart-details" class="fl">
					<div class="payment-details mb10 hide">
						<table class="grey-text">
					      	<tr>
						      <td class="show mr20">${txnInfo.orderDetails }</td>
						      <c:set var="frequencyLabel" value="Frequency"></c:set>
						      <c:if test="${themeInfo.subTheme eq 'airtel'}">
						      	<c:set var="frequencyLabel" value="Duration"></c:set>
						      </c:if>
						      <td>${frequencyLabel} : ${txnInfo.subscriptionFrequency}&nbsp;${txnInfo.subscriptionFrequencyUnit}</td>
						    </tr>
							<tr>
						      <td class="show mr20">Start Date : ${txnInfo.subscriptionStartDate}</td>
						       <c:set var="endDateLabel" value="End Date"></c:set>
						       <c:if test="${themeInfo.subTheme eq 'airtel'}">
						      	<c:set var="endDateLabel" value="Renewal End Date"></c:set>
						      </c:if>
						      <td>${endDateLabel} : ${txnInfo.subscriptionExpiryDate}</td>
						    </tr>
				      	</table>
						<span class="show mt10">Maximum amount to be debited per subscription : Rs ${txnInfo.subscriptionMaxAmount}</span>
					</div>
					
					<div class="blue-text">
						<a href="#" class="btn-show-payment-details small b">SHOW MORE</a>
						<a href="#" class="btn-hide-payment-details hide small b">SHOW LESS</a>
					</div>
				</div>
				<div class="clear"></div>    
			</div>
			<!-- summary card -->
			
			<!-- Login notification -->
			<%@ include file="login.jsp" %>
			<!-- Login notification -->
			
			<c:if test ="${loginInfo.loginFlag && walletInfo.walletEnabled && (walletInfo.walletFailed==false || walletInfo.walletFailed=='false') && !empty walletInfo.walletBalance}">
				<div class="wallet-pay-text">
					<div class="hr"></div>
					<h2 class="mt20">
						<span class="medium">
							Select how would you like to pay ?
						</span>
					</h2>
				</div>
			</c:if>
			
			<!-- paytm cash card -->
			<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled && !walletInfo.walletFailed}">
				<%@ include file="paytmCash.jsp" %>
		    </c:if>
			<!-- paytm cash card -->
			
			
			
			
			
			<div class="subs-msgs">
				<h2 class="mt20 mb20">
					<span class="subs-text subs-default-msg hide large">
						<span>Select a payment method for subsequent transactions.</span>
					</span>
				</h2>
			</div>
			
			<!-- Merchant Payment Modes -->
			<c:set var="paymentConfig" value="${sessionScope}" />
			<c:if test="${!onlyWalletEnabled && (txnInfo.subscriptionPaymentMode eq 'CC' || txnInfo.subscriptionPaymentMode eq 'DC' )}">
				<div id="merchant-payment-modes" class="cards-tabs relative">
					<%@ include file="merchantPaymentModes.jsp" %>
				</div>
			</c:if>
			<!-- Merchant Payment Modes -->
			
			
			<!-- Subscription messages -->
			<div class="subs-msgs" >
				<h2 class="mt20 mb20">
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
			
			
			<!-- Add Money Payment Modes -->
			<%-- <span>${sessionScope.addMoneyPayModes}</span> --%>
			<c:if test="${txnConfig.addAndPayAllowed && ((txnInfo.subscriptionPaymentMode eq 'PPI' && walletInfo.walletBalance < txnInfo.txnAmount) || (txnInfo.subscriptionPaymentMode eq 'NORMAL'))}">
				<c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
				<c:set var="isAddMoneyAvailable" value="true"/>
				<div id="add-money-payment-modes" class="cards-tabs mt20">
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
			<%@ include file="footer.jsp" %>
		<!-- Footer End -->
		
		</div>
		<!-- Main Container -->
		
	</body>
</html>