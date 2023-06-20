<c:if test="${empty irctcData}">
	<c:set var="irctcData" value="${txnConfig.paymentCharges.DEFAULTFEE}"></c:set>
</c:if>


<div class="irctcData" 
	data-bm="${irctcData.baseTransactionAmount}" 
	data-ct="${irctcData.text}"  
	data-tm="${irctcData.totalTransactionAmount}" 
	data-cf="${irctcData.totalConvenienceCharges}">
</div>