<c:if test="${txnConfig.addMoneyFlag}">
<c:set var="dcEnabled" value="${entityInfo.dcEnabled}"></c:set>
<c:set var="ccEnabled" value="${entityInfo.ccEnabled}"></c:set>
<c:set var="netBankingEnabled" value="${entityInfo.netBankingEnabled}"></c:set>
<c:set var="atmEnabled" value="${entityInfo.atmEnabled}"></c:set>
<c:set var="impsEnabled" value="${entityInfo.impsEnabled}"></c:set>
<%--<c:set var="cashcardEnabled" value="${entityInfo.cashcardEnabled}"></c:set>--%>
</c:if>

<c:set var="saveCardEnabled" value="false"/>

<c:if test="${(dcEnabled or ccEnabled) and !empty cardInfo.savedCardsList}">
	<c:set var="saveCardEnabled" value="true"/>
</c:if>

<%-- for wallet only mode with add money - open first tab --%>
<c:if test="${paymentType eq 7 }">
	<c:if test="${impsEnabled}">
		<c:set var="paymentType" value="6"></c:set>
	</c:if>
	<c:if test="${atmEnabled}">
		<c:set var="paymentType" value="8"></c:set>
	</c:if>
	<c:if test="${netBankingEnabled}">
		<c:set var="paymentType" value="3"></c:set>
	</c:if>
	<c:if test="${dcEnabled}">
		<c:set var="paymentType" value="2"></c:set>
	</c:if>
	<c:if test="${ccEnabled}">
		<c:set var="paymentType" value="1"></c:set>
	</c:if>
	<c:if test="${saveCardEnabled}">
		<c:set var="paymentType" value="5"></c:set>
	</c:if>
	
	<script>
		paymentType = "${paymentType}";
	</script>
</c:if>



<c:set var="saveCardOption" value="false"/>
<c:if test="${cardInfo.cardStoreOption}">
	<c:set var="saveCardOption" value="true" />
</c:if>


<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors && txnConfig.addMoneyFlag}">
	<c:set var="errorsPresent" value="true"></c:set>
</c:if>

<c:set var="submitBtnText" value="Add & Pay"/>

<div>
	<h2 class="mt10">
		<span class="addMoneyText large hide msg">
			<span>To complete payment, add to Paytm Cash</span>
			<b class="fr mr20"><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b>
			<div class="hr"></div>
			<span>SELECT A PAYMENT METHOD</span>
		</span>
	</h2>    		
</div>


<%-- payment mode tab --%>
<%-- <c:if test="${empty ppi || isHybrid || (!empty sessionScope.promoCode and empty promoShowAllModes)}"> --%>
	<%@ include file="payment-mode-tabs.jsp"%>
<%-- </c:if> --%>

<div class="heading non-promocode-options">Other payment modes</div>
<%-- other modes links --%>
<%@ include file="other-modes-links.jsp"%>