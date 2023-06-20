<div data-role="content" data-theme="i" id="imps-container" <c:if test="${'6' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
    		 <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
				<li>${requestScope.validationErrors['INVALID_MMID']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
				<li>${requestScope.validationErrors['INVALID_MOBILE']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
				<li>${requestScope.validationErrors['INVALID_OTP']}</li>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="payment/request/submit" data-ajax="false">
	      
	      <div data-role="fieldcontain">
	        <label for="mobileNo" class="ui-input-text">Mobile</label>
	        <input autocomplete="off" name="mobileNo" type="tel" maxlength="10">
	      </div>
	      
	      <div data-role="fieldcontain"> 
	        <label for="mmid" class="ui-input-text">MMID</label>
	        <input autocomplete="off" type="tel" name="mmid" maxlength="7">
	         <a href="#positionWindow" data-rel="popup" data-position-to="window">
	         	<img src="images/wap/iphone/info_icon.png" alt="help" title="Help" class="help" />
	         </a>
	      </div>
	      
	      <div data-role="fieldcontain">
	        <label for="otp" class="ui-input-text">OTP </label>
	        <input autocomplete="off" type="tel" name="otp" maxlength="6">
	        <a href="#positionWindow1" data-rel="popup" data-position-to="window">
	        	<img src="images/wap/iphone/info_icon.png" alt="help" title="Help" class="help" />
	        </a>
	      </div>
	       <c:if test="${saveCardOption}">
		      <div class="pad mt5 mb10" id="card">
		        <div class="save-card-option">
		          <input name="storeCardFlag" type="checkbox" class="styled" value="Y">
		        </div>
		        <div class="label">
		          <label for="card">Save this card for future transactions</label>
		        </div>
		      </div>
	      </c:if>
	      <div class="clear"></div>
	      
	      <p class="pad txt14">
				IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks
				<a class="ui-link" target="_blank" href="http://www.npci.org.in/impsmerpay10.aspx"> click here</a>
		   </p>
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
		  <div class="clear" style="margin-bottom: 50px"></div>
		  
		  <div data-role="footer" data-id="foo1" data-position="fixed">
		  	<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
      </form>
    </section>
</div>

<div data-role="popup" id="positionWindow" class="txt14">
	<p><span class="bold">What is MMID?</span><br />
   	MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transaction.</p>
    <p><span class="bold"><a href="http://imps.npci.org.in/P2M_PB.asp" target="_blank">How to get MMID?</a></span></p>
</div>
 
<div data-role="popup" id="positionWindow1" class="txt14">
     <p><span class="bold">What is OTP?</span><br />
     OTP is one-time password issued by bank to customer for payment transaction through IMPS.</p>				
     <p><span class="bold"><a href="http://imps.npci.org.in/P2M_PB.asp" target="_blank">How to generate OTP?</a></span></p>
</div>