<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.UPI != null}">
		You will be charged  
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.UPI.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.UPI.totalConvenienceCharges }</span>
		as charges for paying through UPI.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.UPI.totalTransactionAmount}</c:set>
	</c:if>
</div>