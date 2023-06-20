<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.ATM != null}">
		You will be charged <span style="font-weight: 600; font-size: 11px;">
		<span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.ATM.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;">
			<span class="WebRupee">Rs</span> 
			${txnConfig.paymentCharges.ATM.totalConvenienceCharges }
		</span>
		as charges for paying through ATM.
		
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.ATM.totalTransactionAmount}</c:set>
	</c:if>
</div>