<div id="cart-section">
	<div id="full-wallet-text" class="hide">Awesome! You have sufficient balance for this order.</div>
	
	<div id="cart-container-web" class="container">
		
		<!-- summary card -->
		<%@ include file="summary.jsp" %>
		<!-- summary card -->
		
		
		<!-- paytm cash card -->
		<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
			<%@ include file="paytmCash.jsp" %>
	    </c:if>
		<!-- paytm cash card -->
		
		<div class="clear"></div>
	</div>
	
	<div id="cart-container-wap" class="container hide">
		<%@ include file="summaryWap.jsp" %>
		
		<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
			<%@ include file="paytmCashWap.jsp" %>
	    </c:if>
	</div>
</div>