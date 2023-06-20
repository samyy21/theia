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
<div id='payment-user-msg' class="medium">
	
	<h2 class="">
		
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
		      		<span class="bankPaymentText msg">Select an option to complete payment</span>
		      </c:when>
		      <c:otherwise>
		      		<span class="bankPaymentText msg">Select a payment method</span>
		      </c:otherwise>
		</c:choose>
		
		<span class="hybridPaymentText hide msg">
			Select a payment method to pay 
			<b class="ml5 fr mr20">
				<span class="WebRupee">&#x20B9;</span>
				<span class="hybridMoneyAmount b"></span>
			</b>
		</span>

		<span class="fullWalletText hide msg">
			Uncheck Paytm to pay using other options
		</span>
	</h2>
</div>

</c:if>

<div class="relative">
	<div class="blur-overlay top-overlay"></div>
	
	
	<!-- Payment mode cards -->
	<div class="cards-content">
		<c:if test="${cardInfo.saveCardEnabled}">
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
	<!-- Cards Controls -->
	<div class="cards-control relative">
		<span class=" hide other-pm-msg">SELECT ANOTHER MODE</span>
		<ul class="grid grey-text2">
			<%@ include file="tab.jsp" %>
		</ul>
		<div class="blur-overlay"></div>
	</div>
	<!-- Cards Controls -->
	<div class="clear"></div>
</div>
