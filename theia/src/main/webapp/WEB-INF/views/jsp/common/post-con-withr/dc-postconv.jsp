<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.DC != null}">
		You will be charged a total amount of 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.DC.totalTransactionAmount }
		including 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.DC.totalConvenienceCharges }
		as charges for paying through debit card.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.DC.totalTransactionAmount}</c:set>
	</c:if>
</div>