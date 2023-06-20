<c:set var="saveCardOption" value="false"/>
<c:if test="${cardInfo.cardStoreOption}">
	<c:set var="saveCardOption" value="true"/>
</c:if>

<c:set var="submitBtnText" value="Proceed securely"/>

<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors && !txnConfig.addMoneyFlag}">
	<c:set var="errorsPresent" value="true"></c:set>
</c:if>

	
<%-- <div id='payment-user-msg'>
	<div class="hr"></div>
	<h2 class="mt10">
		<span class="addMoneyText large hide msg">SELECT AN OPTION TO ADD <b><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b> IN PAYTM CASH TO PAY <b><span class="WebRupee">&#x20B9;</span> <span class="totalAmountSpan b">${txnInfo.txnAmount}</span></b></span>
		<span class="bankPaymentText large msg">SELECT A PAYMENT METHOD</span>
		<span class="fullWalletDeduct large hide msg">UNCHECK PAYTM CASH TO PAY USING OTHER OPTIONS</span>
	</h2>	
</div> --%>
			

<%-- payment mode tab --%>
<%-- <c:if test="${empty ppi || isHybrid || (!empty sessionScope.promoCode and empty promoShowAllModes)}"> --%>
	<%@ include file="payment-mode-tabs.jsp"%>
<%-- </c:if> --%>

<%-- other modes links --%>
<div class="heading bottom_border non-promocode-options">Other payment modes</div>
<%@ include file="other-modes-links.jsp"%>

