<div class="paytmcash-card relative" <c:if test="${true eq onlyWalletEnabled}">id="onlyWalletTxt" </c:if> >
	<div class="blur-overlay"></div>
    <div>
		<ul class="grid">
	        <li>
				<div id="pc" class="fl green-tick">
			  		<input type="checkbox" class="pcb checkbox <c:if test="${true eq onlyWalletEnabled}"> f-hide</c:if>" id="paytm-cash-checkbox"  value="pc" style = "display:none"/>
				</div>
				<div class="fl ml10 mt6 text-box">
		          	
		          	<c:if test="${true eq onlyWalletEnabled && walletInfo.walletBalance> txnInfo.txnAmount}">
						<label for="pc" class="text mb5  medium sufficentTxt" >Awesome! You have sufficient balance for this order</label>
					</c:if>
					<c:if test="${true ne onlyWalletEnabled}">
						<label for="pc" class="text mb5  medium">Pay using Paytm Wallet</label>
					</c:if>
		          		
		          		
		          	<div class="clear"></div>
					</div>
					
	        </li>
	        
      	</ul>
	</div>



</div>

<div class="clear"></div>
<div id="no-walletTextUpdate" class="notification no-walletTextUpdate alert hide fl" style="padding: 4px;font-weight: 400;border:none;color: #222;font-size: 12px;margin-top: 4px;padding-left: 10px;padding-right: 10px;background:#f9ffcf;margin-left: 0px;">You do not have sufficient balance for this transaction</div>
<div class="clear"></div>




<div class="card paytmcash-card">
	<div class="blur-overlay"></div>
    <div class="relative">
		<ul class="grid" style="padding-top:4px;">
	        <li id="leftTxt">
				
				<div class="fl  text-box relative">
		          <div class="relative">
		          	<div class="bal grey-text medium" id="yourBal" style = "display:none">
		          		 Available Paytm Balance &nbsp;<span class="text b rightAmt">
		          			 <span class="${CURRENCY_CLASS}"> ${CURRENCY_TXT}</span> <span class = 'amt'></span>
		          		</span>
		          		
		          	</div>
		          	<div class="clear"></div>
					<div class="bal grey-text medium mt6 hide" id="remBal">
					Remaining balance <span class="text b">
					<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span></span></div>
					
					<%--<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
						<span class="arrow-up subWallets" id="showpaytmWallets"></span>
					</c:if> --%>
					
					</div>

				<%--	<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
			          			
								<div id="WalletsWEBView">
									<div class="paymentCards hide" >
									
									 <ul class="paymentCradsList">
									 <c:forEach var="subwallets"  varStatus="status" items="${walletInfo.subWalletDetails }">
									 	<li><span class="wallet-icn"><img src="${ptm:stResPath()}images/web/bank/${subwallets.webLogo}" alt="wallet Icons" height="16"/></span>  ${subwallets.displayMessage} <strong><span class="WebRupee"> &#8377 </span>  <fmt:formatNumber value=" ${subwallets.subWalletBalance}" maxFractionDigits="2" />  </strong></li>
									 </c:forEach>
									 </ul>
									 <div class="clear"></div>
									</div>
								    <div class="clear"></div>
							    </div>
					   		</c:if>
					   		
					   		
					   		<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
								<div id="WalletsWAPView">
									<div class="paymentCards hide">
									
									 <ul class="paymentCradsList">
									 <c:forEach var="subwallets"  varStatus="status" items="${walletInfo.subWalletDetails }">
									 	<li><span class="wallet-icn"><img src="${ptm:stResPath()}images/web/bank/${subwallets.wapLogo}" alt="wallet Icons" height="16"/></span>  ${subwallets.displayMessage} <strong><span class="WebRupee"> &#8377 </span>   ${subwallets.subWalletBalance} </strong></li>
									 </c:forEach>
									 </ul>
									 <div class="clear"></div>
									</div>
								    <div class="clear"></div>
							    </div>
					   		</c:if>
					--%>
					
				</div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	        <li class="fr balance-used-box hide">
	        	<div class="large b mt6">
	        		 <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
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
		
	    <div class="fullWalletDeduct relative mt20">
			<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
				<div class="foodWalletBorder clear"></div>
			</c:if>
	    	<div class="btn-submit fl">
				<input class="btn-normal" type="submit" value="Pay now" name="">
			</div>

			<div class="clear"></div>
	    </div>
    </form>
    <div class="clear"></div>
</div>