<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.IMPS != null}">
		You will be charged  
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.IMPS.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.IMPS.totalConvenienceCharges }</span>
		as charges for paying through IMPS.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.IMPS.totalTransactionAmount}</c:set>
	</c:if>
</div>