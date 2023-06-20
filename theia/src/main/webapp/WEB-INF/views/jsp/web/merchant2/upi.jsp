<div class = 'card content ${15 eq paymentType ? "active" : ""}' id = "upi-mode">
	<div class="card-header xsmall grey-text mb10 relative">
		
		<div class="clear"></div>
	</div>
	<form autocomplete="off" name="upi-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}"  id="card" class="upi-form validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="UPI" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<input type="hidden" name="walletAmount" value="0" />
		<c:if test="${cardInfo.cardStoreMandatory}">
			<input type="hidden" name="storeCardFlag"  value="on" />
		</c:if>
		<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		
		<ul class="grid">
        	<li class="mb20 card-wrapper">
            	<label class="mb10" for="VIRTUAL_PAYMENT_ADDRESS" >Enter your Virtual Payment Address (<span style="font-weight:bold;">VPA</span>)</label>
            	<c:set var="ccErrorInputClass"></c:set>
            	<%---TODO:  NEED TO CHECK if this is really intended it should be something similar to  --%>
            	<c:if test="${'1' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD']}">
            		<c:set var="ccErrorInputClass">error1</c:set>
            	</c:if>
            	
            	<p class="cd">           		
            		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && sessionScope.paymentInfo.paymentMode eq 'CC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
            		<c:set var="defaultCardNumberFormatted" value="${ defaultCardNumber ne ''  ? sessionScope.paymentInfo.formattedCardNumber : '' }"></c:set>
            		<input type="text" name="VIRTUAL_PAYMENT_ADDRESS" class="upiPayMode text-input large-input required <c:if test="${!empty requestScope.validationErrors['INVALID_VPA']}">error1</c:if>" placeholder="yourname@bank"  style="width: 278px; font-size:16px;"  required />
            	 	<p style="margin-top:10px;"><i style="font-size:12px; line-height:18px;">VPA is a unique payment address that can be linked to a person's bank accounts<br> to make payments.</i></p>
				</p>
				
				<div class="clear"></div>
                	<c:if test="${!empty requestScope.validationErrors['INVALID_VPA']}">
					<div class="error error2 mt10">${requestScope.validationErrors['INVALID_VPA']}</div>
				</c:if>
              
			</li>
		
		</ul>

		<div class="storeCardWrapper">
			<c:if test="${saveCardOption}">
				<div id = "ccStoreCardWrapper" class="fl mb20">
					<div class="fl" id="ccSaveCardLabel">
						<input type="checkbox"  class="pcb checkbox" name="storeCardFlag" checked="checked">
					</div>
					<label for="card1" class="save fl mt8">Save this VPA for faster checkout</label>
				</div>
			</c:if>
			<div class="clear"></div>
		</div>


		<c:set var="submitBtnText" value="Pay now"></c:set>
		 <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null}">
	  		<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion hybrid-payment" style="display:none;margin-top: -10px; margin-bottom: 20px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee white'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
			</div>
	  	</c:if>
		<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null}">
	  		 <c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion add-money-payment" id="addnpay-post-con" style="display:none;margin-top: -10px; margin-bottom: 20px;" data-post-bal = "Pay <span class='WebRupee white'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
			</div>
	   </c:if>
		<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.UPI != null}">
				<div class="post-conv-inclusion default-payment" style="margin-top: -10px; margin-bottom: 20px;" data-post-bal = "Pay <span class='WebRupee white'>Rs</span> ${txnConfig.paymentCharges.UPI.totalTransactionAmount}">
					<%@ include file="../../common/post-con/upi-postconv.jsp" %>
				</div>
		</c:if>
		
        <div >
        	<div class="btn-submit ${submitBtnClass} fl">
				<input name="" type="submit" class="gry-btn fr btn-normal" value="Pay now" id = "upiSubmit">
<!-- 	           	<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id = "upiSubmit"> -->
	         </div>
	         <a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
	         <div class="clear"></div>
        </div>
        
        

	</form>
	<c:if test="${themeInfo.subTheme eq 'bob'}">
   		<span class="mt10 b show" style="color:#fe5d27;">This transaction will be processed by<img src="images/web/merchant/bob_logo.png" style="position: relative;top: 13px;display: inline;left: 10px;"></span>
   	</c:if>
</div>