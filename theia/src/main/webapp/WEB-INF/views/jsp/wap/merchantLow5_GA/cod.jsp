<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${12 eq paymentType}">
<div class="heading">Cash on Delivery (COD)</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="COD" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
<div class="row-dot">
	<p class="hide">
		<select id="codSelect" class="mt5">
	    		<option value="-1">Select</option>
	            <option id="COD" value="COD"  selected="selected">CASH ON DELIVERY</option>
		</select>
	</p>
	
	<!-- <p class=" small">
		Press "Complete Order" button to proceed. <br>Once done, you will receive a call from us to confirm this Order.
	</p> -->
    <!-- <p id="codMsg" class=" small"> -->
    
    <c:if test="${usePaytmCash && txnConfig.codHybridAllowed}">
    	<p class="small">
			At the time of delivery, <span class="WebRupee">Rs</span> <fmt:formatNumber value="${txnInfo.txnAmount - walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /> will have to be given to the courier boy.
    	</p>
    	<p class="pt7"><input type="submit" value="Complete Order" class="blue-btn"  data-txnmode="COD" onclick="pushGAData(this, 'pay_now_clicked')"  /></p>

    </c:if>
    <c:if test="${usePaytmCash && !txnConfig.codHybridAllowed}">
     <div class="failure">
			COD is not available for this transaction
		</div>
     </c:if>
    <c:if test="${!usePaytmCash}">
    	<p class="small">
			You'll receive an automated call from us to confirm this order. And at the time of delivery, <span class="WebRupee">Rs</span> ${txnInfo.txnAmount} will have to be given to the courier boy.
    	<p class="small">
    	<p class="pt7">
    		<button type="submit" class="blue-btn" data-txnmode="COD" onclick="pushGAData(this, 'pay_now_clicked')"  >Complete Order</button>
    	</p>
    </c:if>
    
		
	</p>
    
    
</div>
</form>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'cod']);
	} catch(e){}
</script>
</c:if>
