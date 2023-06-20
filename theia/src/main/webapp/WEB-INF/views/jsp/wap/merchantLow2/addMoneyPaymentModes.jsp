<c:if test="${txnConfig.addMoneyFlag}">
	<c:set var="dcEnabled" value="${entityInfo.addDcEnabled}"></c:set>
	<c:set var="ccEnabled" value="${entityInfo.addCcEnabled}"></c:set>
	<c:set var="netBankingEnabled" value="${entityInfo.addNetBankingEnabled}"></c:set>
	<c:set var="atmEnabled" value="${entityInfo.addAtmEnabled}"></c:set>
	<c:set var="impsEnabled" value="${entityInfo.addImpsEnabled}"></c:set>
	<c:set var="emiEnabled" value="${entityInfo.addEmiEnabled}"></c:set>
	<c:set var="codEnabled" value="${entityInfo.addCodEnabled}"></c:set>
</c:if>

<c:set var="saveCardEnabled" value="false"/>

 <c:if test="${(dcEnabled or ccEnabled) and !empty cardInfo.addAndPayViewCardsList}"> 
 	<c:set var="saveCardEnabled" value="true"/> 
 </c:if>

<%-- for wallet only mode with add money - open first tab --%>
<c:if test="${paymentType eq 7 }">
	<c:if test="${codEnabled}">
		<c:set var="paymentType" value="12"></c:set>
	</c:if>
	<c:if test="${emiEnabled}">
		<c:set var="paymentType" value="13"></c:set>
	</c:if>
<%-- 	<c:if test="${rewardsEnabled }"> -->
<!-- 		<c:set var="paymentType" value="11"></c:set> -->
<!-- 	</c:if> -->
<-- 	<c:if test="${cashcardEnabled }"> -->
<!-- 		<c:set var="paymentType" value="10"></c:set> -->
<!-- 	</c:if> --%>
	<c:if test="${impsEnabled }">
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
	<c:set var="saveCardOption" value="true"/>
</c:if>


<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors && txnConfig.addMoneyFlag}">
	<c:set var="errorsPresent" value="true"></c:set>
</c:if>

<c:set var="submitBtnText" value="Pay now"/>

<%-- <div>
	<h2 class="mt10">
		<span class="addMoneyText large hide msg">
			<span>To complete payment, add to Paytm</span>
			<b class="fr mr20"><span class="WebRupee">&#x20B9;</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span></b>
			<div class="hr"></div>
			<span>SELECT A PAYMENT METHOD</span>
		</span>
	</h2>    		
</div> --%>

<c:set var="existAddMoneyTab" value="true" />
<%-- payment mode tab --%>
<%-- <c:if test="${empty ppi || isHybrid || (!empty sessionScope.promoCode and empty promoShowAllModes)}"> --%>
	<%@ include file="payment-mode-tabs.jsp"%>
<%-- </c:if> --%>

<div class="other-modes-wrapper">
	<div id="otherpaymentModeHeading" class="non-promocode-options">Other payment modes</div>
	<%-- other modes links --%>
	<div id="othermodesLinks">
			
		<%@ include file="other-modes-links.jsp"%>
	</div>
</div>
