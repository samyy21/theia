<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil,com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="../../common/config.jsp"%>


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
			window.scanNpayURL="${ptm:stScanPath()}";
            window.transactionStatusEndpoint = "${ConfigurationUtil.getProperty('transaction.status.endpoint')}";
            window.isQRCodeEnabled = "${txnInfo.qrDetails.isQREnabled}" === "true";
            window.scanPayTimeout = parseInt("${ConfigurationUtil.getProperty('scanpay.transaction.timeout')}");

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
			
		</script>
		<script>
		var MERCHANT_USER_ID='';
		</script>
		<c:if test="${not empty loginInfo.user}">
			<script>
			var MERCHANT_USER_ID='${loginInfo.user.payerUserID}';
			</script>
		</c:if>
		<%String useMinifiedAssets=ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
	<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="/theia/resources/css/web/merchant3/style.css" />
		
			<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}vendor/socket.io/socket.io.min.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant3/functions.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
	<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant3/style.min.css" />
		<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
		<script type="text/javascript" src="${ptm:stResPath()}vendor/socket.io/socket.io.min.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant3/functions.min.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
<% } %>

		<%-- custom css for different merchants --%>
		<c:if test="${themeInfo.subTheme eq 'airtel'}">
			<style>
				.btn-submit input {
					background-image: url("${ptm:stResPath()}images/web/merchant/sprite_compressed_airtel.png") !important;
				}
			</style>
		</c:if>

		<c:if test="${themeInfo.subTheme eq 'paytmIframe'}">
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant/style.paytmIframe.css" />
		</c:if>

		<c:if test="${themeInfo.subTheme eq 'recharge'}">
			<style>
				.deleteCard {
					display: block;
				}

				.c {
					background-image:url(${ptm:stResPath()}images/web/merchant/cc-paytm.png);
					background-position:287px;				
				}
			</style>
		</c:if>
		
		<c:if test="${themeInfo.subTheme eq 'dishtv'}">
			<style>
				
				.tick{background: url("${ptm:stResPath()}images/web/merchant/sprite_dishtv.png") no-repeat  -209px -67px;}
				.md-modal .closePop {background: url("${ptm:stResPath()}images/web/merchant/sprite_dishtv.png") no-repeat; background-position: -168px -69px;}
				
				.blue-text, .blue-text * {
					color : #f15a22;
				}
				
				.blue-text-2, .blue-text-2 * {
					color : #f15a22;
				}
				
				.cancel { 
					color : #f15a22;
				}
				
				#header, .card.active {
					border-color : #f15a22;
				}
				
				.cards-control .card.active {
					border-color: #f15a22;
					background: #f15a22;
				}
				
				a:hover, a.active {
					color : #f15a22;
				}
				
				.text-input:focus {
					border-color: #f15a22;
					box-shadow: 0px 0px 2px #f15a22;
				}
				
				
				
			</style>
		</c:if>
		
		
		<c:if test="${themeInfo.subTheme eq 'torrent'}">
			<style>
			#header{padding-bottom:0; border-bottom: 2px #FC9500 solid;}
			.blue-text{
			color:#FC9500;
			}
			.cards-control .card.active {
				    border-color: #EA8B02;
				    background: #FC9500;
				}
				.cancel, a:hover, a.active {
				    color: #FC9500;
				}
				.btn-submit input.btn-normal{
					background: #FC9500 !important;
				}
				.card.active {
				    border-color: #FC9500;
				}
				.alert-info{
				    border-color: rgb(255, 239, 158);
				    background-color: rgb(255, 239, 158);
				    color: #FC9500;
				}
		</style>
		</c:if>
		<div id="csrf" data-value="${csrfToken.token}"></div>
	</head>

	<body onload="backButtonOverride();">
	
		<%@ include file="../../common/common.jsp"%>
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
			data-hybrid-allowed="${txnConfig.hybridAllowed}"
			data-idebit-ic ="${entityInfo.iDebitEnabled}"
			></div>
		<div id="addMoney-details"
		data-available="${txnConfig.addAndPayAllowed}"
		data-selected="${txnConfig.addMoneyFlag}"></div>
		<div id="login-details"
		data-value='${loginInfo.loginFlag}'
		data-isautologin='${loginInfo.autoLoginCreate eq "N" ? false : true}'></div>
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
				data-promocode="${txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
			</div>
		</c:if>
		<div id="auth-js-details" 
		data-mid="${txnInfo.mid}"
		data-orderid="${txnInfo.orderId}"
		<%--data-txnInfo="${txnInfo}"--%>
		<%--data-loginInfo="${loginInfo}"--%>
		data-theme="${themeInfo.loginTheme}"
		data-retrycount="${loginInfo.loginRetryCount}"></div>

		<c:if test="${loginInfo.loginWithOtp eq true}">
			<div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>
		</c:if>




		<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>"></div>
		<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>
		<div id="paymentCharges" data-value="${!empty txnConfig.paymentCharges ? true : false}"></div>
		<div id="ir-addMoneyCharge" data-iramount="${txnConfig.paymentCharges.PPI.totalTransactionAmount}"></div>
		
		<div id="irctcDefaultFee" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
		<div id="irctcOnlyWallet" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
		<div id="irctcHybridWallet" data-bm="${txnConfig.paymentCharges.HYBRID.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.HYBRID.text}" data-tm="${txnConfig.paymentCharges.HYBRID.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.HYBRID.totalConvenienceCharges}">	</div>
		<!-- Configurations --> 

		<!-- Main Container -->
		<div class="main-container">
		
		<!-- ----START----    FOR WITHOUT SIGNUP SCREENS CHECK -->
		
		 <c:if test="${merchInfo.mid ne 'IRCTCe231206309' && merchInfo.mid ne 'IRCTCw862137775'}">
			
		<c:set var="isSignup" value="true" />
		<!-- FOR OAUTH SIDE CHECK  --->
		<div id="isSignup" data-issignup="true"></div>
			
		</c:if> 
		
		<!---  ---END----  FOR WITHOUT SIGNUP SCREENS CHECK -->
		
		
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

		<!--Middle Container Start-->
		<div class="container">
			
			<!-- <h1 class="otherText">Great, let's get the payment done!</h1>
			<h1 class="fullWalletDeduct" style = "display:none">Just confirm to pay through Paytm</h1> -->
			
			<!-- notifications -->
			 <div class="notification-container">
				<%@ include file="notifications.jsp" %>
			</div>
			<c:choose>
			      <c:when test="${!empty txnConfig.paymentCharges}">
			      
			      	<%@ include file="paymentCharges.jsp" %>
			      </c:when>
			      <c:otherwise>
			      	<%@ include file="summary.jsp" %>
			      </c:otherwise>
			</c:choose>
		</div>

			<div class="container">
					<!-- summary card -->

					<!-- Login notification -->
					<c:if
						test="${!txnInfo.qrDetails.isQREnabled && !loginInfo.loginFlag}">
						<%@ include file="login.jsp"%>
					</c:if>
					<!-- Login notification -->


					<!-- paytm cash card -->
					<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
						<%@ include file="paytmCash.jsp"%>
					</c:if>
					<!-- paytm cash card -->

				<!-- Scan pay -->
					<%@ include file="scannpay.jsp"%>
				<!-- Scan pay -->

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

					<div id="footer-placeholder" class="hide"></div>
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