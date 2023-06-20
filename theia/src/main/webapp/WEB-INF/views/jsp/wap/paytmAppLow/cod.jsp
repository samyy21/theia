<c:if test="${12 eq paymentType}">
<div class="heading">Cash on Delivery (COD)</div>
<form autocomplete="off" method="post" action="/payment/request/submit" onsubmit = "submitForm()">
	
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
    
    <c:if test="${usePaytmCash}">
    	<p class="small">
    		At the time of delivery, Rs ${txnInfo.txnAmount - walletInfo.walletBalance} will have to be given to the courier boy.
    	</p>
    </c:if>
    <c:if test="${!usePaytmCash}">
    	<p class="small">
    		You'll receive an automated call from us to confirm this order. And at the time of delivery, Rs ${txnInfo.txnAmount} will have to be given to the courier boy.
    	<p class="small">
    </c:if>
		
	</p>
    <p class="pt7"><input type="submit" value="Complete Order" class="blue-btn" /></p> 
    
    <!--Lock image-->
    <div class="pt7 hide">
    	<div class="fl image"><img src="/images/wap/paytmLow/lock.png" alt="" title="" /></div>
        <div class="fl small"> Your card details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    
</div>
</form>
</c:if>
