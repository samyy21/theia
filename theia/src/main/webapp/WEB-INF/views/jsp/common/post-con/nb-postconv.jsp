<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.NB != null}">
		You will be charged  
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.NB.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.NB.totalConvenienceCharges }</span>
		as charges for paying through Net Banking.
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.NB.totalTransactionAmount}</c:set>
	</c:if>
</div>