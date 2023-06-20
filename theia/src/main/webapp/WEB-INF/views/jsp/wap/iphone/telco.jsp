<%String contextPathTelco = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProjectProperty("context.path"); %>
<div data-role="content" data-theme="i" id="telco-container" <c:if test="${'9' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
				<li>${requestScope.validationErrors['INVALID_MOBILE']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BANK']}">
				<div class="error">${requestScope.validationErrors['INVALID_BANK']}</div>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="<%=contextPathTelco %>/payment/request/submit" data-ajax="false">
	      
	      <div data-role="fieldcontain">
	        <label for="mobileNo" class="ui-input-text">Mobile</label>
	        <input autocomplete="off" name="mobileNo" value="${sessionScope.MSISDN}" type="tel" maxlength="10" <c:if test="${!empty sessionScope.OTP_VERIFIED_FLAG && sessionScope.OTP_VERIFIED_FLAG}">disabled=disabled</c:if>>
	      </div>
	      
	      <div data-role="fieldcontain" class="ui-field-contain ui-body ui-br">
	        <label for="bankCode" class="ui-input-text">Operator </label>
	        <select name="bankCode">
	        	<option value="-1">Select</option>
				<option value="Vodafone">Vodafone</option>
				<option value="Vodafone">Airtel</option>
				<option value="Vodafone">Idea</option>
				<option value="Vodafone">Aircel</option>
				<option value="Vodafone">Reliance</option>
				<option value="Vodafone">BSNL</option>
			</select>
	      </div>
	      <p class="pad txt14">
			This amount will be charged to your mobile bill.
		  </p>
	      <div class="clear"></div>
		   
			<input type="hidden" name="txnMode" value="Telco" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
		  <div class="clear" style="margin-bottom: 50px"></div>
		  
		  <div data-role="footer" data-id="foo1" data-position="fixed">
		  	<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
      </form>
    </section>
</div>