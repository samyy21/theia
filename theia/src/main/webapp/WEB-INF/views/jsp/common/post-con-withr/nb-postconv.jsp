<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.NB != null}">
		You will be charged a total amount of 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.NB.totalTransactionAmount }
		including 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.NB.totalConvenienceCharges }
		as charges for paying through net banking.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.NB.totalTransactionAmount}</c:set>
	</c:if>
</div>