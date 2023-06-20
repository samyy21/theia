<c:set var="saveCardOption" value="false"></c:set>
<c:if test="${cardInfo.cardStoreOption}">
    <c:set var="saveCardOption" value="true"></c:set>
</c:if>

<c:set var="submitBtnClass" value=""/>

<c:set var="errorsPresent" value="false"></c:set>
<c:if test="${!empty requestScope.validationErrors}">
    <c:set var="errorsPresent" value="true"></c:set>
</c:if>

	



<div class="pt20 relative">
	<div class="blur-overlay top-overlay"></div>
	<!-- Cards Controls -->
	<div class="cards-control relative grey-text" id="leftTab">
		<ul class="grid">
			<%@ include file="tab.jsp" %>
		</ul>
		<div class="blur-overlay"></div>
	</div>
	<!-- Cards Controls -->
	
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
	
	<div class="clear"></div>
</div>
