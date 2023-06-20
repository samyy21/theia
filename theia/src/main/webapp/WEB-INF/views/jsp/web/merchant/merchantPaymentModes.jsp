<c:set var="saveCardOption" value="false"></c:set>
<c:if test="${cardInfo.cardStoreOption}">
    <c:set var="saveCardOption" value="true"></c:set>
</c:if>

<c:set var="submitBtnClass" value=""/>

<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors}">
    <c:set var="errorsPresent" value="true"></c:set>
</c:if>

<c:if test="${merchantModeAtleastOneSelected}">
<div id='payment-user-msg' class="large">
	
	<h2 class="mt10">
		<span class="addMoneyText hide msg">SELECT AN OPTION TO ADD 
		  <b>
			<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> 
			<span class="addMoneyAmount b">${txnInfo.txnAmount}</span>
		  </b>
		   IN PAYTM TO PAY
		  <b>
			<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
			<span class="totalAmountSpan b">${txnInfo.txnAmount}</span>
		  </b>
 		</span>
		
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
		      		<span class="bankPaymentText msg">Select an option to complete payment</span>
		      </c:when>
		      <c:otherwise>
		      		<span class="bankPaymentText msg">Select a payment method</span>
				  <span class="msg no-walletTextUpdate hide">Please select an option to pay</span>

		      </c:otherwise>
		</c:choose>
		
		<span class="hybridPaymentText hide msg">
			Select an option to pay balance 
			<b class="ml5 fr mr20">
				<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> 
				<span class="hybridMoneyAmount b"></span>
			</b>
		</span>

		<span class="fullWalletText hide msg">
			Uncheck Paytm to pay using other options
		</span>
	</h2>
    <div class="hr mt20"></div>		
</div>

</c:if>
<div class="pt20 relative">

	<div class="blur-overlay top-overlay"></div>
	<!-- Cards Controls -->
	<div class="cards-control relative grey-text">
		<ul class="grid">
			<%@ include file="tab.jsp" %>
		</ul>
		<div class="blur-overlay"></div>
	</div>
	<!-- Cards Controls -->
	
	<!-- Payment mode cards -->
	<div class="cards-content">
		<c:if test="${cardInfo.saveCardEnabled == 'true'}">
			<%@ include file="savedCard.jsp" %>
		</c:if>					
		<c:if test="${entityInfo.dcEnabled}">
			<%@ include file="dc.jsp" %>
		</c:if>
		<c:if test="${entityInfo.ccEnabled}">
			<%@ include file="cc.jsp" %>
		</c:if>
		<c:if test="${entityInfo.netBankingEnabled}">
			<%@ include file="nb.jsp" %>
		</c:if>
		<c:if test="${entityInfo.atmEnabled}">
			<%@ include file="atm.jsp" %>
		</c:if>
		<c:if test="${entityInfo.impsEnabled}">
			<%@ include file="imps.jsp" %>
		</c:if>

		<c:if test="${entityInfo.emiEnabled}">
			<%@ include file="emi.jsp" %>
		</c:if>
		<c:if test="${entityInfo.codEnabled}">
		   <%@ include file="cod.jsp" %>
		</c:if>
		<c:if test="${entityInfo.upiEnabled}">
			<%@ include file="upi.jsp" %>
		</c:if>
	</div>
	
	<div class="clear"></div>
</div>
