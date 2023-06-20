<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.ATM != null}">
		You will be charged a total amount of 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.ATM.totalTransactionAmount }
		including 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.ATM.totalConvenienceCharges }
		as charges for paying through ATM.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.ATM.totalTransactionAmount}</c:set>
	</c:if>
</div>