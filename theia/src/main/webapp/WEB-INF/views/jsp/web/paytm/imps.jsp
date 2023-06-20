<div class = 'tab-pane fade <c:if test="${6 == paymentType}">in active</c:if>' id = "impsContent">
	<div class="fl">
		<form autocomplete="off" name="imps-form" method="post" action="submitTransaction" id="card">
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="WEB" />
			<input type="hidden" name="txn_Mode" value="IMPS" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
			<ul>
				<li>
					<label>Mobile number</label>
					<p>
						<input type="text" name="mobileNo" size="25" maxlength="10" id="mobileNo"/>
					</p>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
						<div class="error">${requestScope.validationErrors['INVALID_MOBILE']}</div>
						<script type="text/javascript">
							$("#mobileNo").addClass("error1");
						</script>
					</c:if>
				</li>
				
				<li class="ml10">
					<label>MMID</label>
					<p>
						<input type="text" name="mmid" maxlength="7" id="mmid" size="7"/>
					</p>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
						<div class="error">${requestScope.validationErrors['INVALID_MMID']}</div>
						<script type="text/javascript">
							$("#mmid").addClass("error1");
						</script>
					</c:if>
				</li>
				
				<li class="ml10">
					<label>OTP</label>
					<p>
						<input type="text" name="otp" maxlength="6" id="otp" size="6"/>
					</p>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
						<div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
						<script type="text/javascript">
							$("#otp").addClass("error1");
						</script>
					</c:if>
				</li>
			</ul>
			
			<p class="clear">
	           	<input name="" type="submit" class="gry-btn" value="Proceed securely" id="impsSubmit">
	           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
	        </p>
	        <div class="secure">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
	        </div>
		</form>
	</div>
	<div class="fr txt12 width190">
    	<p class="font600">What is IMPS?</p>
        <p class="mt10">IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks, <a target="_blank" href="http://www.npci.org.in/impsmerpay10.aspx" class = "blue">click here</a>.</p>
        <p class="mt10 font600">What is MMID?</p>
        <p class="mt10">MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transactions.
        	<br>
			<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class = "blue">How to get MMID?</a>
		</p>
        <p class="mt10 font600">What is OTP?</p>
        <p class="mt10">OTP is one-time password issued by bank to customer for payment transaction through IMPS
        	<br>
			<a target="_blank" href="http://www.npci.org.in/merchant.aspx" class = "blue">How to generate OTP?</a>
        </p>
    </div>
    <div class="clear"></div>
</div>