<div class="common-conv">
	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.EMI != null}">
		You will be charged  <span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.EMI.totalTransactionAmount }</span>
		including 
		<span style="font-weight: 600; font-size: 11px;"><span class="WebRupee">Rs</span> 
		 ${txnConfig.paymentCharges.EMI.totalConvenienceCharges }
		</span>
		as charges for paying through EMI.
		
		<c:set var="submitBtnText">Pay <span class="WebRupee">Rs</span>  ${txnConfig.paymentCharges.EMI.totalTransactionAmount}</c:set>
	</c:if>
</div>