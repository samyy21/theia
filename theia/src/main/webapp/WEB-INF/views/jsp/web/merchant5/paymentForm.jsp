<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil,com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO"%>
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


		<link rel="dns-prefetch" href="https://pgpqa-static1.paytm.in"/>
		<link rel="dns-prefetch" href="https://pgpqa-static2.paytm.in"/>
		<link rel="dns-prefetch" href="https://pgpqa-static3.paytm.in"/>
		<link rel="dns-prefetch" href="https://pgpqa-static4.paytm.in"/>


		<c:set var="cssPathComponent" value="${mid}" />
		<c:if test="${empty header['PRX']}">
			<c:set var="cssPathComponent" value="default" />
		</c:if>
		<%@ include file="allUiAssets.jsp"%>
    </head>
     
    <body onload="backButtonOverride();">
     
        <%@ include file="../../common/common.jsp"%>
        <!-- Configurations -->
		<c:if test="${txnInfo == null}">
			<c:redirect url="/error"/>
		</c:if>

		<%@ include file="uiInfoData.jsp"%>

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
                  <c:otherwise>
                    <%@ include file="header.jsp" %>
                  </c:otherwise>
            </c:choose>
        <!--Header End-->
  
        <!--Middle Container Start-->

			<c:if test="${themeInfo.subTheme == 'goair' }">
				<c:set value="container-background" var="cssClass"></c:set>
				<c:set value="container-pad" var="pad10"></c:set>
			</c:if>

		<div class="${cssClass }">

			<div class="container ${pad10}">
             

            <!-- notifications -->
            <div class="notification-container">
                <%@ include file="notifications.jsp" %>
            </div>
            <!-- notifications -->
             
  
            <!-- summary card -->
			
			<!-- IRCTC CHANGES UPDATE -->



			<c:choose>
				<c:when test="${themeInfo.subTheme eq 'indiamart'}">
				</c:when>
				<c:when test="${!empty txnConfig.paymentCharges}">

					<%@ include file="paymentCharges.jsp" %>
				</c:when>
				<c:otherwise>
					<%@ include file="summary.jsp" %>
				</c:otherwise>
			</c:choose>



			<!-- IRCTC CHANGES UPDATE -->
			


                <c:if test ="${!txnInfo.qrDetails.isQREnabled && !loginInfo.loginFlag && !walletInfo.walletFailed}">
					<%@ include file="login.jsp" %>
				</c:if>
                
               <c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag  && walletInfo.walletEnabled}">
              	 <%@ include file="paytmCash.jsp" %>
           		</c:if>


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

		<%@ include file="subTheme.jsp"%>

		<% if(useMinifiedAssets.equals("Y")){ %>

<%--
		<script type="text/javascript" async src="${ptm:stResPath()}js/web/merchant5/functions.min.js" defer></script>
--%>
		<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>

		<% } %>
		
		<script>
            window.scanNpayURL="${ptm:stScanPath()}";
            window.transactionStatusEndpoint = "${ConfigurationUtil.getProperty('transaction.status.endpoint')}";
            window.isQRCodeEnabled = "${txnInfo.qrDetails.isQREnabled}" === "true";
            window.scanPayTimeout = parseInt("${ConfigurationUtil.getProperty('scanpay.transaction.timeout')}");
		</script>
	</body>
</html>