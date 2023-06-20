<c:if test="${'IMPS' eq paymentTypeId}">
	<div class="divider"></div>
	<div class="form-container">
		<form autocomplete="off" method="post" action="/payment/request/submit">
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			
			<p class="mt15">
				<label>Mobile</label> <br /> 
	            <input autocomplete="off" name="mobileNo" type="tel" maxlength="10" placeholder="Enter">
	            <div class="error">${requestScope.validationErrors['INVALID_MOBILE']}</div>
			</p>
				
			<p class="mt15">
				<label>MMID </label><br /> 
				<input autocomplete="off" type="tel" name="mmid" maxlength="7" placeholder="Enter" /> <br /> 
	            <a href="http://imps.npci.org.in/P2M_PB.asp" class="blue pad-lr">How to get MMID?</a>
	            <div class="error">${requestScope.validationErrors['INVALID_MMID']}</div>
			</p>
			
			<p class="mt15">
				<label>OTP</label> <br />
				<input autocomplete="off" type="tel" name="otp" maxlength="6" placeholder="Enter"/> <br /> 
	            <a href="http://imps.npci.org.in/P2M_PB.asp" class="blue pad-lr">How to generate OTP?</a>
	            <div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
			</p>
			
			<p class="pad txt14">
				IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks
				<a class="ui-link" target="_blank" href="http://www.npci.org.in/impsmerpay10.aspx"> click here</a>
		   </p>
			
			<div class="mt10 margin">
				 <input	name="Submit" type="submit" value="Pay Now" class="button" />
			</div> 
			<div class="margin mb15">
					<input id="cancelButton" name="cancleBtn" onClick="cancelTxn();" type="button" class="cancel" value="Cancel" />
			</div>
		</form>
	</div>
</c:if>