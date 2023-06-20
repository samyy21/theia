<ul class="grid pad10">
	<li class="pad10">
		<div id="pc" class="green-tick fl">
	  		<input type="checkbox" class="pcb checkbox f-hide" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
		</div>
		<span class="medium show mb10">Money in paytm</span>
		<div class="remaining-bal">
	       	<span class="bal grey-text small">
	       		(Remaining balance  <span class="WebRupee">&#8377</span> <span class="remBal"></span>)
	       	</span>
	   	</div>
	</li>
	<li class="fr pad10 bt">
		<span class="large show pad10">
       		<span class="WebRupee">(-) &#8377</span>
       		<span class="balance-used"></span>
       	</span>
	</li>
</ul>

<ul class="addToWallet hide grid mt20 bt pad10">
	<li class="mt20">
		<span class="medium show mb10">Additional money needed</span>
	</li>
	<li class="fr pad10">
		<span class="large b">
       		<span class="WebRupee">&#8377</span>
       		<span class='hybridMoneyAmount addMoneyAmount'></span>
       	</span>
	</li>
</ul>

<form autocomplete="off" name="creditcard-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" class="hide fullWalletDeduct" id="wallet-btn-wap">
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