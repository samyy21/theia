<%String contextPathcc = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProjectProperty("context.path"); %>
<div data-role="content" data-theme="i" id="creditcard-container" <c:if test="${'1' ne paymentType}">style="display:none;"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
    		 <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
				<li>${requestScope.validationErrors['INVALID_CARD']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<li>${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
				<li>${requestScope.validationErrors['INVALID_CVV']}</li>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="<%=contextPathcc %>/payment/request/submit" data-ajax="false">
	      
	      <div data-role="fieldcontain"> 
	        <label for="creditCard" class="ui-input-text">Credit card no.</label>
	        <input autocomplete="off" type="tel" name="cardNumber" maxlength="19">
	      </div>
	      
	      <div data-role="fieldcontain">
	        <label for="expiryDate" class="ui-input-text">Expiry date (MMYY) </label>
	        <input name="ccExpiryMonthYear" type="tel" maxlength="4">
	      </div>
	      
	      <div data-role="fieldcontain">
	        <label for="cvv" class="ui-input-text">CVV no. </label>
	        <input autocomplete="off" type="tel" name="cvvNumber" maxlength="4">
	      </div>
	      <c:if test="${saveCardOption}">
		      <div class="pad mt5 mb10" id="card">
		        <div class="save-card-option">
		          <input name="storeCardFlag" type="checkbox" class="styled" value="N">
		        </div>
		        <div class="label">
		          <label for="card">Save this card for future transactions</label>
		        </div>
		      </div>
	      </c:if>
	      <div class="clear" style="margin-bottom: 50px"></div>
		  <input type="hidden" name="txnMode" value="CC" />
		  <input type="hidden" name="channelId" value="WAP" />
		  <input type="hidden" name="AUTH_MODE" value="3D" />
		  
		  <div data-role="footer" data-id="foo1" data-position="fixed">
		  	<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
      </form>
    </section>
</div>
