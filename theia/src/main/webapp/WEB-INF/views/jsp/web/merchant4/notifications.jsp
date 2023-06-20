<c:if test="${!empty errorMsg}">
	<div class="notification alert alert-danger mt10 mb10">
		<c:out value="${errorMsg}" escapeXml="true" />
		<script>pushGAData(false, 'exception_visible', '${errorMsg}');</script>
	</div>
</c:if>

<c:if test="${!empty txnInfo.displayMsg}">
	<div class="notification alert mt10 mb10">
		<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
	</div>
</c:if>

<%-- <c:if test="${txnInfo.retry}">
	<div class="notification alert mt10">
		<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
	</div>
</c:if> --%>

<c:if test="${!empty txnInfo.retry && txnInfo.retry eq 'true' && !empty retryPaymentInfo.errorMessage}">
	<div class="notification alert mt10 mb10">
		<c:out value="${retryPaymentInfo.errorMessage}" escapeXml="true" />
		<script>pushGAData(false, 'exception_visible', '${retryPaymentInfo.errorMessage}');</script>
	</div>
</c:if>

<c:if test="${!empty txnInfo.offerMessage}">
	<div class="notification alert mt10 mb10">
		<c:out value="${txnInfo.offerMessage}" escapeXml="true" />
	</div>
</c:if>

<c:if test ="${loginInfo.loginFlag}">

	<c:if test="${(walletInfo.walletFailed || walletInfo.walletInactive) && !empty walletInfo.walletFailedMsg}">
		<div class="notification alert alert-danger mt10 mb10">

			<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
			<script>pushGAData(false, 'exception_visible', '${walletInfo.walletFailedMsg}');</script>
		</div>
	</c:if>
</c:if>

<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
<div class="notification alert mt20 mb20">
	<div class="promocode-options-msg">
			${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
			<script>pushGAData(false, 'exception_visible', '${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}');</script>
	</div>
	
	<c:if test="${!empty txnInfo.promoCodeResponse && txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName eq 'CASHBACK'}">
		<%--<c:if test="${empty promoShowAllModes}">
			<a href = "javascript:void()" id="show-other-options" onclick="showAllPaymentModes();" class="fr blue-text">Show other options to pay</a> 
		</c:if>	--%>
		<div class="promocode-options-msg-2 hide">
			<%--If you pay using other option you will not get benefits of your promocode.
			<br>--%>
			${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
			<script>pushGAData(false, 'exception_visible', '${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}');</script>
		</div>
	</c:if>
	
</div>
</c:if>

<div id="tlsWarnMsgId" class="notification alert hide mt10 mb10"></div>