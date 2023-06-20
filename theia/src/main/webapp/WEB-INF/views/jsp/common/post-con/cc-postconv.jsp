<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.CC != null}">
		You will be charged  <span style="font-weight: 600; font-size: 11px;">
			<span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.CC.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;">
			<span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.CC.totalConvenienceCharges }
		</span>
		as charges for paying through Credit Card.
		
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.CC.totalTransactionAmount}</c:set>
	</c:if>
</div>