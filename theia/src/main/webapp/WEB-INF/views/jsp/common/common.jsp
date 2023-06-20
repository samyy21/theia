<input type="hidden" id="pgTrackUrlManual" value="${sessionScope.pgTrackUrlManual}" />
<input type="hidden" id="pgTrackUrlAuto" value="${sessionScope.pgTrackUrlAuto}" />
<div id="queryStringForSession" data-value="${queryStringForSession}"/>
<div id="jvmRoute" data-value="${requestScope.jvmRoute}"/>

<!-- CURRENCY SIGN SET --> 
<c:if test="${txnInfo.currencyCode eq 'INR'}">
	<c:set var="CURRENCY_CLASS" value="WebRupee"></c:set>
	<c:set var="CURRENCY_TXT" value="Rs."></c:set>
</c:if>

<c:if test="${txnInfo.currencyCode eq 'USD'}">
	<c:set var="CURRENCY_CLASS" value="usd"></c:set>
	<c:set var="CURRENCY_TXT" value="USD"></c:set>
</c:if>

<c:if test="${txnInfo.currencyCode eq 'AED'}">
	<c:set var="CURRENCY_CLASS" value="aed"></c:set>
	<c:set var="CURRENCY_TXT" value="AED"></c:set>
</c:if>
<c:set var="merchantModeAtleastOneSelected" value="false"></c:set>
<c:if test="${cardInfo.saveCardEnabled || entityInfo.dcEnabled || entityInfo.ccEnabled || entityInfo.netBankingEnabled ||
				entityInfo.atmEnabled || entityInfo.impsEnabled || entityInfo.emiEnabled || entityInfo.codEnabled}">
	<c:set var="merchantModeAtleastOneSelected" value="true"></c:set>				
</c:if>
<c:set var="addModeAtleastOneSelected" value="false"></c:set>
<c:if test="${cardInfo.addAndPayViewSaveCardEnabled || entityInfo.addDcEnabled || entityInfo.addCcEnabled || entityInfo.addNetBankingEnabled ||
				entityInfo.addAtmEnabled || entityInfo.addImpsEnabled || entityInfo.addEmiEnabled || entityInfo.addCodEnabled}">
	<c:set var="addModeAtleastOneSelected" value="true"></c:set>				
</c:if>
<script>
document.body.oncopy = function() { return false; }
document.body.oncut = function() { return false; }
document.body.onpaste = function() { return false; }
</script>
