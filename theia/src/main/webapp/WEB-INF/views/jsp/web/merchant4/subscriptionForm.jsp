<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil,com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO"%>
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
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant4/style.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
			
			<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant4/functions.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant4/style.min.css" />
			
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant4/functions.min.js"></script>			
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>			
		<% } %>
		
		<c:set var="themeInfo.subTheme" value="${themeInfo.subTheme}"></c:set>
		<%-- <c:set var="themeInfo.subTheme" value="airtel"></c:set> --%>
		
		<%-- custom css for different merchants --%>
		<c:if test="${themeInfo.subTheme eq 'airtel'}">
			<style>
				.btn-submit input {
					background-image: url("${ptm:stResPath()}images/web/merchant4/sprite_compressed_airtel.png") !important;
				}
			</style>
		</c:if>
		<c:if test="${themeInfo.subTheme  eq 'mts'}">
			<style>
				#sc-card{display:none;}
			</style>
		</c:if>
		<div id="csrf" data-value="${csrfToken.token}"></div>
		<style>
			#cart-details {
				width: 400px;
			}
		</style>
	</head> 
	<body onload="backButtonOverride();">
	<div class="bgstript"></div>
		<%@ include file="../../common/common.jsp"%>
		<!-- Configurations -->
		<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
		<div id="wallet-details" data-available="${not empty walletInfo && !walletInfo.walletFailed}" data-balance="${walletInfo.walletBalance}" data-wallet-only="${onlyWalletEnabled}"></div>
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
			data-userId = "${loginInfo.user.userID}">
		</div>

	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
		<!-- Configurations -->

	<!-- Payments bank section-->

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


		<!-- Main Container -->
		<div class="main-container">
		
		<!--Header Start-->
			<c:choose>
			      <c:when test="${themeInfo.subTheme eq 'noheader'}">
			      </c:when>
			      <c:otherwise>
			      	<!-- <%@ include file="header.jsp" %> -->
			      </c:otherwise>
			</c:choose>
		<!--Header End-->

		<!--Middle Container Start-->
		<div class="container" id="overlayBox" style="margin-top:35px;">
			
			<!-- <h1 class="otherText">Great, let's get the payment done!</h1>
			<h1 class="fullWalletDeduct" style = "display:none">Just confirm to pay through Paytm</h1> -->
			
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

			<c:if test ="${!loginInfo.loginFlag && !empty errorMsg}">
				<div class="notification-container">
						<div class="notification alert mt10 mb10">
							<c:out value="${errorMsg}" escapeXml="true" />
						</div>
				</div>	
			</c:if>

			<!-- notifications -->
			

			<!-- summary card -->
			<div class="card summary-card">
			<div id="merchant-logo" class="mt10 mr20 mb20 fl" style="margin-top:-15px;">
				<c:if test="${!empty merchInfo.merchantImage}">

					<c:choose>
						<c:when test="${merchInfo.useNewImagePath eq true}">
							<img src="${merchInfo.merchantImage}" alt="" style="position: absolute;" />
						</c:when>
						<c:otherwise>
							<img src="${ptm:stResPath()}images/web/merchant/${merchInfo.merchantImage}" alt="" style="clear:both; position: absolute;" />
						</c:otherwise>
					</c:choose>

				</c:if>
				<c:if test="${empty merchInfo.merchantImage && !empty merchInfo.merchantName}">
					
										<span style="
					font-size: 18px;
					font-weight: bold;
					margin-top: 16px;
					position: absolute;
					">${merchInfo.merchantName}</span>
				</c:if>

				<div id="cart-details" style="margin-top:58px;">
						<div class="payment-details mb10">
						   <table class="grey-text">
							  <tr>
								 <th class="show b" colspan="2" style="font-size:14px;">Payment Schedule</th>
							  </tr>
							  <tr>
								 <td class="show ">Payment Interval</td>
									<td>:${txnInfo.subscriptionFrequency}&nbsp;${txnInfo.subscriptionFrequencyUnit}</td>
							   </tr>
							 </table>
						</div>
				</div>

				</div>

			    <div id="cart-details" class="fr" style="margin-top:33px;">
					<div class="payment-details mb10">
					   <table class="grey-text">
						  <tr>
							 <th class="show mr20 b" colspan="2" style="font-size:16px; position: relative;">

									<ul class="grid mt10 mb5" style="margin-top:-1px; position: absolute; margin-top:-31px;">
											<c:set var="merchantName" value="${fn:toUpperCase(merchInfo.merchantName)}"></c:set>
											<c:set var="subsHeaderText" value="SUBSCRIPTION ON "></c:set>
											<c:if test="${themeInfo.subTheme eq 'airtel'}">
												<c:set var="subsHeaderText" value=""></c:set>
											</c:if>

											<li class="fr large b">
												<span class="WebRupee">Rs.</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
												<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
											</li>
											<li class="clear"></li>
										  </ul>
							</th>
						  </tr>

						  <tr>
							 <th class="show  b mt10" colspan="2" style="font-size:14px; text-align: left;">Recurring Amount</th>
						  </tr>
						  <tr>
							 <td class="show " style="margin-top:3px;">Amount Type</td>
							 <td> :&nbsp;${txnInfo.subscriptionAmountType}</td>
						  </tr>
						  <c:if test="${txnInfo.subscriptionAmountType ne 'FIX'}">
							<tr>
								<td class="show " style="margin-top:3px;">Maximum Amount</td>
								<td> :&nbsp;${txnInfo.subscriptionMaxAmount}</td>
							</tr>
						</c:if>
						 </table>
					</div>
				</div>



				<div class="clear"></div>
			</div>
			<!-- summary card -->
			
			<!-- Login notification -->
			<%@ include file="login.jsp" %>
			<!-- Login notification -->

			<c:set var="isSubscriptionFlow" value="true"></c:set>
			<c:if test ="${loginInfo.loginFlag && walletInfo.walletEnabled && !walletInfo.walletFailed && !empty walletInfo.walletBalance}">
				<div class="wallet-pay-text">
					<div class="hr"></div>
					<h2 class="mt20">
						<span class="medium">
							Select how would you like to pay ?
						</span>
					</h2>
				</div>
			</c:if>
			<div class="payAmount">
			<!-- paytm cash card -->
			<%--<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled && !walletInfo.walletFailed}">--%>
			<c:if test="${loginInfo.loginFlag}">
				<%@ include file="paytmWallets.jsp" %>
				<div class="clear"></div>
			</c:if>

			<!-- paytm cash card -->
			</div>
			
			
			
			
			
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
    <script>
        // scanpay
        window.scanNpayURL="${ptm:stScanPath()}";
        window.transactionStatusEndpoint = "${ConfigurationUtil.getProperty('transaction.status.endpoint')}";
        window.isQRCodeEnabled = false;
        window.scanPayTimeout = parseInt("${ConfigurationUtil.getProperty('scanpay.transaction.timeout')}");
        window.prnEnabled = false;
    </script>
	</body>
</html>