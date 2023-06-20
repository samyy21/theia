<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null}">
		You will be charged  <span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.PPI.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.PPI.totalConvenienceCharges }</span>
		as charges for paying through Wallet.
		
		<c:set var="submitBtnText">Pay <span class="WebRupee white">Rs</span>  ${txnConfig.paymentCharges.PPI.totalTransactionAmount}</c:set>
	</c:if>
</div>