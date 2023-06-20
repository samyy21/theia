<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.CC != null}">
		You will be charged a total amount of 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.CC.totalTransactionAmount }
		including 
		<span class ="WebRupee">Rs</span>${txnConfig.paymentCharges.CC.totalConvenienceCharges }
		as charges for paying through credit card.
		
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span> ${txnConfig.paymentCharges.CC.totalTransactionAmount}</c:set>
	</c:if>
</div>