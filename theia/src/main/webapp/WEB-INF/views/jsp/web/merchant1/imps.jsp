<div class = 'card content ${6 eq paymentType ? "active" : ""}' id = "imps-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your payment details are secured via<br/>128 Bit encryption by Verisign</i></span>
	        </div>
		</div>
		<div class="clear"></div>
	</div>
	<div class="fl">
		<form autocomplete="off" class="imps-form validated" name="imps-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			<ul class="grid imps-grid">
				<li>
					<label for="mobileNo" class="mb10">MOBILE NUMBER</label>
					<c:set var="impsMobileErrorClass"></c:set>
					<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_MOBILE']}">
						<c:set var="impsMobileErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="tel" class="text-input ${impsMobileErrorClass} required" name="mobileNo" size="25" maxlength="10" id="mobileNo"/>
					</p>
					<c:if test="${impsMobileErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_MOBILE']}</div>
					</c:if>
				</li>
				
				<li class="clear">
					<label for="mmid" class="mb10">MMID</label>
					<c:set var="impsMMIDErrorClass"></c:set>
					<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_MMID']}">
						<c:set var="impsMMIDErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="text-input ${impsMMIDErrorClass} required" name="mmid" maxlength="7" id="mmid" size="7"/>
					</p>
					<c:if test="${impsMMIDErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_MMID']}</div>
					</c:if>
					<p>
						<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class="active">How to get MMID?</a>
					</p>
				</li>
				
				<li class="ml10">
					<label for="otp" class="mb10">OTP</label>
					<c:set var="impsOTPErrorClass"></c:set>
					<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_OTP']}">
						<c:set var="impsOTPErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="${impsOTPErrorClass} text-input required" name="otp" maxlength="8" id="otp" size="8"/>
					</p>
					<c:if test="${impsOTPErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_OTP']}</div>
					</c:if>
					<p>
						<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class="active">How to generate OTP?</a>
					</p>
				</li>
				
			</ul>
			<c:if test="${saveCardOption}">
			<ul class="grid">
				<li>
					<div id = "ccStoreCardWrapper" class="fl mt20">
		              <div class="fl" id="ccSaveCardLabel">
		        	    <input type="checkbox"  class="pcb checkbox" name="storeCardFlag" checked="checked">
		              </div>
		             <label for="card1" class="save fl mt8">Save this Mobile No. for faster checkout</label>
		           </div>
				</li>
			</ul>
			</c:if>
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id="impsSubmit">	          
	           	</div>
	           	<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
	           	<div class="clear"></div>
	        </div>
	       
		</form>
	</div>
	<div class="fr txt12 width190 grey-text mt20 lh14">
    	<p class="font600">What is IMPS?</p>
        <p class="mt10 small">
        	IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks, <a target="_blank" href="http://www.npci.org.in/impsmerpay10.aspx" class = "active mt6">click here</a>
        </p>
        <p class="mt10 font600">What is MMID?</p>
        <p class="mt10 small">MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transactions.
			<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class = "active">How to get MMID?</a>
		</p>
        <p class="mt10 font600">What is OTP?</p>
        <p class="mt10 small">OTP is one-time password issued by bank to customer for payment transaction through IMPS
			<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class = "active">How to generate OTP?</a>
        </p>
    </div>
    <div class="clear"></div>
</div>