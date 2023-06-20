<c:set var="saveCardOption" value="false"/>
<c:if test="${cardInfo.cardStoreOption}">
	<c:set var="saveCardOption" value="true"/>
</c:if>

<c:set var="submitBtnText" value="Pay now"/>

<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors}">
	<c:set var="errorsPresent" value="true"></c:set>
</c:if>

<c:set var="dcEnabled" value="${entityInfo.dcEnabled}"/>
<c:set var="ccEnabled" value="${entityInfo.ccEnabled}"/>
<c:set var="saveCardEnabled" value="${cardInfo.saveCardEnabled}"/>
<c:set var="netBankingEnabled" value="${entityInfo.netBankingEnabled}"/>
<c:set var="atmEnabled" value="${entityInfo.atmEnabled}"/>
<c:set var="impsEnabled" value="${entityInfo.impsEnabled}"/>
<%--     <c:set var="cashcardEnabled" value="${txnConfig.cashcardEnabled}"/> --%>
<c:set var="codEnabled" value="${entityInfo.codEnabled}"/>
<c:set var="emiEnabled" value="${entityInfo.emiEnabled}"/>
<c:set var="chequeDDNeftEnabled" value="${txnConfig.chequeDDNeftEnabled}"/>
<c:set var="upiEnabled" value="${entityInfo.upiEnabled}"></c:set>

	
<%-- <div id='payment-user-msg'>
	<div class="hr"></div>
	<h2 class="mt10">
		<span class="addMoneyText large hide msg">SELECT AN OPTION TO ADD <b><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b> IN PAYTM WALLET TO PAY <b><span class="WebRupee">&#x20B9;</span> <span class="totalAmountSpan b">${txnInfo.txnAmount}</span></b></span>
		<span class="bankPaymentText large msg">SELECT A PAYMENT METHOD</span>
		<span class="fullWalletDeduct large hide msg">UNCHECK PAYTM WALLET TO PAY USING OTHER OPTIONS</span>
	</h2>	
</div> --%>
			

<%-- payment mode tab --%>
<%-- <c:if test="${empty ppi || isHybrid || (!empty sessionScope.promoCode and empty promoShowAllModes)}"> --%>
	<%@ include file="payment-mode-tabs.jsp"%>
<%-- </c:if> --%>

<div class="pt7" style="padding: 5px 15px; color: #8e8e8e; margin-top: -7px; font-size: 12px;">

	We will redirect you to bank website to authorize payments.
</div>

<%-- other modes links --%>

<div id="otherpaymentModeHeading" class="heading bottom_border non-promocode-options">Other payment modes</div>
<div id="othermodesLinks" style="border-top:#ccc solid 1px;">
<%@ include file="other-modes-links.jsp"%>
</div>

