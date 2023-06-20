<div class="blur-overlay"></div>
<!--<div class="titletext">



						<div class="fl ">
							<p class="titletext hide ml10 lightTxt" id="fullWalletPayTxt">Awesome! You have sufficient balance in your <strong class="titletext">Paytm</strong></p>
							<p class="titletext hide ml10 lightTxt" id="defultPayTxt">Use <strong class="titletext">Paytm</strong> <storng id="walletAmountRem" class="hide titletext"> (<span class="titletext">Rs</span> <span class = 'walletAmountRemTxt titletext'></span>)</storng></p>
						</div>


				</div>
				<div class="clear"></div>
				-->

<div class="mt5">

	<div id="hybrid-mode-paybox" class="card paybox fl hide">
		<div class="small mt10">Payment to be made</div>
		<div class=" titletext mt6  b"><span class="titletext b WebRupee">Rs</span>

			<span class="totalPcfAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span>

		</div>
		<!-- <div class="bal  small mt6" id="remWalletBal">Remaining balance <span class=" "><span class="">Rs</span> <span class = 'amt'></span></span></div> -->
	</div>

<div id="hybrid-mode-sign" class="sign hide"><span class="minus">-</span></div>

<div class="card paytmcash-card paybox fl">
	<div class="blur-overlay"></div>
    <div >
		<ul class="grid" style="overflow:visible;">
	        <li>
				<div class="fl text-box">
		          	<div class="bal  small mt10" id="yourBal" style = "display:none">
		          		 Money in Your <span class="">Paytm </span>
		          		 
		          		 
		          		<div class=" mt6 lightTxt ">
		          			<span class="relative"><span class="titletext b WebRupee">Rs</span> <span class = 'amt titletext'> </span>

		          			
		          			</span>
		          			
		          			 

		          		</div>

		          	</div>
		          	<div class="clear"></div>

				</div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	       <%--  <li class="fr balance-used-box hide">
	        	<div class="large b mt6">
	        		- <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
	        		<span id="balance-used"></span>
	        	</div>
	        </li> --%>
      	</ul>
	</div>

    <div class="clear"></div>
	<div class="bal  small mt6 remWalletBal lightTxt" id="remWalletBal" >Remaining balance <span class=" "><span class="WebRupee">Rs</span> <span class = 'amt'></span></span></div>
	 
</div>
<div id="sign-hybrid" class="sign"><span class="minus">-</span></div>
<div id="paybox-hybrid" class="card paybox fl">
<div class="small mt10">Payment to be made</div>
<div class=" titletext mt6  b"><span class="titletext b WebRupee">Rs</span> <span id="usedWalletAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></div>
<!-- <div class="bal  small mt6" id="remWalletBal">Remaining balance <span class=" "><span class="">Rs</span> <span class = 'amt'></span></span></div> -->
</div>


<!--  Total payment -->


<c:set var="submitBtnText">Pay Now</c:set>


<div class="fullWalletDeduct paybox card fl">
<form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
    	<input type="hidden"  name="txnMode" value="PPI" />
		<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
		<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
		
	    <div class="btn-submit mt10 ml50">
<!-- 				<input class="btn-fullLen " type="submit" value="Pay now" name=""> -->
				<button name="" type="submit" class="btn-fullLen" data-txnmode="PAYTMWALLET" onclick="pushGAData(this, 'pay_now_clicked')">${submitBtnText}</button>
			</div>
    </form>
	    	
			<div class="clear"></div>
			
	    </div>
	    
				
				<div id='payment-user-msg' class="exactPayment hide">
				<div class="sign"><span class="equal">=</span></div>
				<div class="paybox card fl">
					<h2 class="small mt10">
						<span class="addMoneyText hide msg">
						<div>Additional money needed in Paytm</div>
						<div class="mt6 b"><span class="titletext WebRupee">Rs</span> <span class="addMoneyAmount titletext">${txnInfo.txnAmount}</span></div> <%-- <b><span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class="totalAmountSpan b">${txnInfo.txnAmount}</span></b> --%></span>
						
						<span class="hybridPaymentText hide msg"> 
						<div class="small">Select an option to pay balance </div>
						<b class="fl mt6 mr20 titletext b"><span class="titletext WebRupee">Rs</span> <span class="hybridMoneyAmount  titletext"></span></b></span>
				
						<span class="fullWalletText hide msg">Uncheck Paytm to pay using other options</span>
					</h2>
				    <div class="mt20"></div>
				    <br class="hybridPaymentText hide"/>
				    <div class="clear small mt6 lightTxt" style="margin-left: -20px;">
						 <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
					   		*convenience charges extra
					   	</c:if>	
				   	</div>
				    </div>	
				   
				</div>
</div>