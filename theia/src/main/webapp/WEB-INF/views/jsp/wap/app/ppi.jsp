<div data-role="content" data-theme="i" id="ppi-container" <c:if test="${'7' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_USER_ID']}">
				<li>${requestScope.validationErrors['INVALID_USER_ID']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PASSOWRD']}">
				<li>${requestScope.validationErrors['INVALID_PASSOWRD']}</li>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="payment/request/submit" data-ajax="false">
	      
	      <div data-role="fieldcontain"> 
	        <label for="walletUserId" class="ui-input-text">User Id</label>
	        <input autocomplete="off" type="text" name="walletUserId" maxlength="45">
	      </div>
	      
	      <div data-role="fieldcontain">
	        <label for="password" class="ui-input-text">Password </label>
	        <input name="password" type="password" maxlength="45">
	      </div>
	      
	      <div class="clear" style="margin-bottom: 50px"></div>
	      <input type="hidden" name="txnMode" value="PPI" />
			<input type="hidden" name="channelId" value="WEB" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		  
		  <div data-role="footer" data-id="foo1" data-position="fixed">
		  	<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
      </form>
    </section>
