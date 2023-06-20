<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.DC != null}">
		You will be charged  
		<span style="font-weight: 600; font-size: 11px;">
			<span class="WebRupee">Rs</span> 
			${txnConfig.paymentCharges.DC.totalTransactionAmount }
		</span>
		including 
		<span style="font-weight: 600; font-size: 11px;">
			<span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.DC.totalConvenienceCharges }</span>
		as charges for paying through Debit Card.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.DC.totalTransactionAmount}</c:set>
	</c:if>
</div>