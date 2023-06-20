<c:set var="addMoneySaveCardEnabled" value="false"/>

<c:if test="${(entityInfo.dcEnabled) or (entityInfo.ccEnabled)}">
	<%-- <c:if test="${!empty cardInfo.savedCardsList}"> --%>
	<c:if test="${!empty cardInfo.merchantViewSavedCardsList}">
		<c:set var="addMoneySaveCardEnabled" value="true"/>
	</c:if>
</c:if>

<c:set var="saveCardOption" value="false" />
<c:if test="${cardInfo.cardStoreOption}">
	<c:set var="saveCardOption" value="true" />
</c:if>


<c:set var="errorsPresent" value="false" />
<c:if test="${!empty requestScope.validationErrors}">
	<c:set var="errorsPresent" value="true"></c:set>
</c:if>

<c:set var="submitBtnClass" value="add-money-btn"/>
<c:if test="${addModeAtleastOneSelected}">
<div id='payment-user-msg' class="medium">
	<h2 class="">
		<span class="addMoneyText hide msg">
				<span class="add-msg">Select an option to 
					<span class="b">
						add <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class="addMoneyAmount b">${txnInfo.txnAmount}</span> to paytm
					</span>
					& complete this order
				</span>
		</span>
	</h2>
</div>
</c:if>
<c:set var="existAddMoneyTab" value="true" />
<div class="relative">
	<div class="blur-overlay top-overlay"></div>
	
	<!-- Payment mode cards -->
	<div class="cards-content">
		<c:if test="${cardInfo.addAndPayViewSaveCardEnabled}">
			<%@ include file="savedCard.jsp" %>
		</c:if>				
		<c:if test="${entityInfo.addDcEnabled}">
			<%@ include file="dc.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addCcEnabled}">
			<%@ include file="cc.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addNetBankingEnabled}">
			<%@ include file="nb.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addAtmEnabled}">
			<%@ include file="atm.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addImpsEnabled}">
			<%@ include file="imps.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addEmiEnabled}">
			<%@ include file="emi.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addCodEnabled}">
			<%@ include file="cod.jsp" %>
		</c:if>
		<c:if test="${entityInfo.addUpiEnabled}">
			<%@ include file="upi.jsp" %>
		</c:if>
	</div>
	
	<!-- Cards Controls -->
	<div class="cards-control relative">
		<span class=" hide other-pm-msg">SELECT ANOTHER MODE</span>
		<!-- <div class="selection hide">
			<div class="fr img img-arw-dwn"></div>
			<a href="#" class="default-name">Select Method</a>
			<a href="#" class="tab-name hide"></a>
		</div> -->
		<ul class="grid grey-text2">
			<%@ include file="tab.jsp" %>
		</ul>
		<div class="blur-overlay"></div>
	</div>
	<!-- Cards Controls -->
	
	<div class="clear"></div>
</div>
