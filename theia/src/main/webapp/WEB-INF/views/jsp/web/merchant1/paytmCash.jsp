<div class="card paytmcash-card">
	<div class="blur-overlay"></div>
    <div >
		<ul class="grid">
	        <li>
				<div id="pc" class="fl green-tick">
			  		<input type="checkbox" class="pcb checkbox" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
				</div>
				<div class="fl ml20 text-box">
		          	<label for="pc" class="text mb5 mt6 b">Use Paytm</label>
		          	<div class="bal grey-text small" id="yourBal" style = "display:none">
		          		 Your current balance is  
		          		<span class="text b">
		          			<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span>
		          		</span>
		          		
		          	</div>
		          	<div class="clear"></div>
					<div class="bal grey-text small mt6 hide" id="remBal">Remaining balance <span class="text b"><span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span></span></div>
				</div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	        <li class="fr balance-used-box hide">
	        	<div class="large b mt6">
	        		- <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
	        		<span id="balance-used"></span>
	        	</div>
	        </li>
      	</ul>
	</div>

    <div class="clear"></div>
    
    <form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
    	<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
    	<input type="hidden"  name="txnMode" value="PPI" />
		<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
		<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
		<input type="hidden" name="storeCardFlag"  value="off" />
		
	    <div class="fullWalletDeduct ml50 mt20">
	    	<div class="btn-submit fl">
				<input class="btn-normal" type="submit" value="Pay now" name="">
			</div>
			<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
			<div class="clear"></div>
	    </div>
    </form>
    <div class="clear"></div>
</div>