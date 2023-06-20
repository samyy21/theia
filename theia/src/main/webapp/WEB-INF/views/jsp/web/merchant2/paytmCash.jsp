<div class="operator-new fl">
	<!-- <span class="img img-minus large"></span> -->
	<span>-</span>
</div>

<div class="card paytmcash-card fl" style="width: 28%;">
	<div class="blur-overlay"></div>
    <div>
    	<div id="pc" class="green-tick fl">
	  		<input type="checkbox" class="pcb checkbox f-hide" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
		</div>
		
		<span class="money-text dark">Payment from Paytm</span>
		<span class="money-text hide dark">Money in your Paytm</span>
		<br>
		<br>
		<span class="large">
			<span class="newWebRupeeSymbol">&#8377 </span><span class="newWebRupee1 balance-used"></span>
       		<!-- <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
       		<span class="balance-used"></span> -->
       	</span>
       	<div class="remaining-bal">
	       	<span class="bal grey-text">
	       		(Remaining balance <span class="WebRupee">&#8377</span> <span class="remBal"></span>)
	       	</span>
	   	</div>
	</div>

    
    
</div>

<div class="operator-new fl">
	<!-- <span class="img img-equals large b"></span> -->
	<span>=</span>
</div>

<div class="paytmcash-pay-card fl">
	<form autocomplete="off" name="creditcard-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" class="hide fullWalletDeduct validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
    	<input type="hidden"  name="txnMode" value="PPI" />
		<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
		<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
	    <div class="">
	    	<div class="btn-submit fl paytm-cash-btn">
				<input class="" type="submit" value="Pay now" name="">
			</div>
			<div class="clear"></div>
	    </div>
    </form>
    <div class="addToWallet hide card active">
    	<span class="dark">Additional money needed</span>
    	<br>
    	<br>
    	<span class="large b">
    		<span class="newWebRupeeSymbol">&#8377 </span><span class="newWebRupee1 hybridMoneyAmount addMoneyAmount"></span>
    		<!-- <span class="WebRupee">Rs</span> <span class='hybridMoneyAmount addMoneyAmount'></span> -->
    	</span>
    </div>
</div>