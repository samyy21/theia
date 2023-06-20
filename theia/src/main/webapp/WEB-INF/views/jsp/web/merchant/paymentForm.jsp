<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
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
		<%String useMinifiedAssets=ConfigurationUtil.getProperty("context.useMinifiedAssets");%>
		<script>
				var PG_ANALYTICS={}, RESTART_CHECK_JS=false,URL_RESTART_CHECK='';
				PG_ANALYTICS.mId='${txnInfo.mid}';
				PG_ANALYTICS.orderId="${txnInfo.orderId}";
				PG_ANALYTICS.logToConsole="${txnConfig.uiEventLogToConsole}";
				PG_ANALYTICS.logToServer="${txnConfig.uiEventLogToServer}";
				PG_ANALYTICS.theme="merchant";
		</script>
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
			<%-- TODO put this js in the end 	 --%>
			<c:if test="${txnConfig.uiEventLogToServer=='true'}">
			<%--<script type="text/javascript" src="${ptm:stResPath()}js/analytic.js"></script>--%>
   		    <script>
                 window.URL_RESTART_CHECK='${ptm:stResPath()}js/web/merchant/functions.js';
            </script>

			</c:if>
	<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant/style.min.css" />
		<!--[if lte IE 9 ]>
			    <script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
			<![endif]-->
			
			<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant/functions.min.js"></script>
			<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
			<c:if test="${txnConfig.uiEventLogToServer=='true'}">
			<%--<script type="text/javascript" src="${ptm:stResPath()}js/analytic.js"></script>--%>
			<script>
                 window.URL_RESTART_CHECK='${ptm:stResPath()}js/web/merchant/functions.min.js';
            </script>
			</c:if>
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
		
		<c:if test="${themeInfo.subTheme eq 'ndtv'}">
			<style>
				.cards-control .card.active {
					border-color: #da0000;
					background: #da0000;
				}

				.card.active {
					border-color: #da0000;
				}

				.btn-submit input.btn-normal {
					background: #da0000 !Important;
				}

				.cancel, .blue-text, .blue-text * {
					color: #da0000;
				}

				a:hover, a.active {
					color: #da0000;
				}

				#header {
					border-bottom: 2px #da0000 solid;
				}

				.text-input:focus {
					border-color: #da0000;
					box-shadow: 0px 0px 2px #da0000;
				}

				#login-box .alert-info {
					background: #f3f3f3;
					border: 1px solid #D1D1D1;
				}

				#merchant-logo {
					margin-top: 15px;
				}

				.md-modal .md-content {
					border: 1px solid #da0000;
				}

				.md-modal .closePop {
					background-image: url(${ ptm : stResPath()}images/web/merchant/sprite_ndtv.png);
				}
		</style>
	</c:if>


		<c:if test="${themeInfo.subTheme eq 'dishtv'}">
	<style>
.tick {
	background:
		url("${ptm:stResPath()}images/web/merchant/sprite_dishtv.png")
		no-repeat -209px -67px;
}
.cb-icon-check {
    background-position: -209px -40px;
}
.md-modal .closePop {
	background:
		url("${ptm:stResPath()}images/web/merchant/sprite_dishtv.png")
		no-repeat;
	background-position: -168px -69px;
}

.blue-text, .blue-text * {
	color: #f15a22;
}

.blue-text-2, .blue-text-2 * {
	color: #f15a22;
}

.cancel {
	color: #f15a22;
}

#header, .card.active {
	border-color: #f15a22;
}

.cards-control .card.active {
	border-color: #f15a22;
	background: #f15a22;
}

a:hover, a.active {
	color: #f15a22;
}

.text-input:focus {
	border-color: #f15a22;
	box-shadow: 0px 0px 2px #f15a22;
}
</style>
</c:if>
<c:choose>
	<c:when
		test="${themeInfo.subTheme eq 'indiamart' || themeInfo.subTheme eq 'justeat'}">
		<style>
#login-box {
	display: none;
}

#paytm-wallet_tab {
	display: block;
}
</style>
	</c:when>
	<c:otherwise>
		<style>
#login-box {
	display: block;
}

#paytm-wallet_tab {
	display: none;
}
</style>
	</c:otherwise>
</c:choose>
<c:if test="${themeInfo.subTheme  eq 'goair'}">
	<style>
.container-background {
	background:
		url("${ptm:stResPath()}images/web/merchant/bg_without_plane_new.jpg")
		no-repeat;
}

.container-pad {
	padding: 1px 15px 15px 15px;
	background-color: rgba(255, 255, 255, 0.46);
}

.mb6 {
	margin-bottom: 6px;
}

.cards-control .card.active {
	border-color: #21307e;
	background: #21307e;
}

.paytmcash-card.active, .card {
	border-color: #fff;
	background-color: #fff;
}

.control-group.card.active {
	border-color: #00d7ff;
}

.control-group.card {
	border-color: #cccccc;
}

.btn-submit input.btn-normal {
	background: #21307e !important;
}
</style>
</c:if>
		<c:if test="${themeInfo.subTheme  eq 'limeroad'}">

			<style>
		.cards-control .card.active, .btn-submit input.btn-normal {
			background: #c13c61 !Important;
			border: #c13c61 1px solid;
		}

		.card.active {
			border: #c13c61 1px solid;
		}

		.btn-show-payment-details, .btn-hide-payment-details {
			color: #c13c61;
		}

		.text-input:focus {
			border-color: #c13c61;
			box-shadow: 0px 0px 2px #c13c61;
		}
		</style>
		</c:if>
		<c:if test="${themeInfo.subTheme  eq 'mts'}">
			<style>
		#sc-card, #ccStoreCardWrapper, #dcStoreCardWrapper {
			display: none;
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
		<div id="emiPlanId" 
			data-value='${retryPaymentInfo.emiPlanId}'>
		</div>
		
		<div id="wallet-details"
			data-available="${!walletInfo.walletFailed}"
			data-balance="${walletInfo.walletBalance}"
			data-wallet-only="${onlyWalletEnabled}"
			data-wallet-used="${walletUsed}">
		</div>
		<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-save-card-mandatory="${cardInfo.cardStoreMandatory}"></div>
		
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
			data-value='${loginInfo.loginFlag}'>
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

		<div id="isAddNPayAllowId" data-value="${txnConfig.addAndPayAllowed}"></div>
		<div id="tlsWarnMsgDivId" data-value="${messageInfo.merchantTLSWarnMsg}"></div>
		<div id="tlsWarnMsgAddNPayDivId" data-value="${messageInfo.addAndPayTLSWarnMsg}"></div>


	<%--<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>--%>
		<!-- Configurations --> 

		<!-- Main Container -->
		<div class="main-container">
		
		<!--Header Start-->
			<c:choose>

			      <c:when test="${themeInfo.subTheme eq 'noheader'}">
			      </c:when>
			      <c:when test="${themeInfo.subTheme eq 'paytmIframe'}">
			      </c:when>
				<c:when test="${themeInfo.subTheme eq 'idea'}">
					<%@ include file="ideaHeader.jsp" %>
				</c:when>
				<c:when test="${themeInfo.subTheme eq 'bigBasket'}">
					<%@ include file="bigBasket.jsp" %>
				</c:when>
			      <c:otherwise>
			      	<%@ include file="header.jsp" %>
			      </c:otherwise>
			</c:choose>
		<!--Header End-->
	 
	   
		<c:if test="${themeInfo.subTheme == 'goair' }">
			<c:if test="${loginInfo.loginFlag && !empty loginInfo.user && loginInfo.logoutAllowed}">
				<div class="user-view container">
					<div>
						<div class="logout">
							<div class="user-name">

								<div class="blue-text mt6 mb6" id="logout-btn-container">
									<span style="color: black;">${loginInfo.user.mobileNumber }</span>
									<a href="#" id="logout-btn"> || Login</a> as a different user
								</div>
							</div>
							<iframe id="logout-iframe" class="hide" src=""></iframe>
							<div class="clear"></div>
						</div>
					</div>
				</div>
			</c:if>
		</c:if>

		<c:if test="${themeInfo.subTheme == 'goair' }">
			<c:set value="container-background" var="cssClass"></c:set>
			<c:set value="container-pad" var="pad10"></c:set>
		</c:if>

		<div class="${cssClass }">
			<!--Middle Container Start-->
			<div class="container ${pad10 }">



				<!-- notifications -->
				<div class="notification-container">
					<%@ include file="notifications.jsp"%>
				</div>
				<!-- notifications -->


				<!-- summary card -->
				<c:choose>
					<c:when test="${themeInfo.subTheme eq 'indiamart'}">

					</c:when>
					<c:otherwise>
						<%@ include file="summary.jsp"%>
					</c:otherwise>
				</c:choose>
				<!-- summary card -->


				<!-- Login notification -->
				<c:if
					test="${!loginInfo.loginFlag && !walletInfo.walletFailed && !loginInfo.loginDisabled}">
					<%@ include file="login.jsp"%>
				</c:if>
				<!-- Login notification -->

				<!-- paytm cash card -->
				<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
					<%@ include file="paytmCash.jsp"%>
				</c:if>
				<!-- paytm cash card -->



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
		</div>
		
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
		<script>
		<%--Check if js is properly loaded --%>
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
	</body>
</html>