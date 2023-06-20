<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.EMI != null}">
		You will be charged a total amount of 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.EMI.totalTransactionAmount }
		including 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.EMI.totalConvenienceCharges }
		as charges for paying through EMI.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.EMI.totalTransactionAmount}</c:set>
	</c:if>
</div>