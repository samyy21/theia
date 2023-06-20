<div id="imps-container" <c:if test="${'6' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="imps-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="IMPS" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="IMPS" />
		<input type="hidden" name="AUTH_MODE" value="OTP" />
		<div class="query">
			<div class="margin-auto1">
				<p class="label top-txt">
					IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile.
					<br>
					For list of participating Banks, <a class="blu" target="new" href="http://www.npci.org.in/impsmerpay10.aspx">click here</a>
				</p>
			</div>
		
			<div class="gray">
				<div class="fl">
					<div class="padd2 mt6">
						<label>Mobile</label>
						<input type="text" name="mobileNo" class="FI" maxlength="10"/>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
						<div class="error">${requestScope.validationErrors['INVALID_MOBILE']}</div>
					</c:if>
					<div>
						<label>MMID</label>
						<input type="text" name="mmid" class="FI" maxlength="7"/>
						<a href="http://www.npci.org.in/merchant.aspx" class="blue" target="_blank">How to get MMID?</a>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
						<div class="error">${requestScope.validationErrors['INVALID_MMID']}</div>
					</c:if>
					<div class="padd2 mt6">
						<label>OTP</label>
						<input type="text" name="otp" class="FI" maxlength="6"/> <a href="http://www.npci.org.in/merchant.aspx" target="_blank" class="blue">How to generate OTP?</a>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
						<div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
					</c:if>
				</div>
				<div class="fr lock left">
					<div class="fl">
						<img title="Secured" alt="Secured" src="images/web/lock.png">
					</div>
					<div class="fl widthh">
						<span class="gry">Your details are secured via 128 Bit encryption by Verisign.</span>
						<br>
						<div class="blue mt6">What is MMID?</div>
						<span class="gry">MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transactions</span>
						<br>
						<div class="blue mt6">What is OTP?</div>
						<span class="gry">OTP is one-time password issued by bank to customer for payment transaction through IMPS</span>
					</div>
					<div class="clear"></div>
				</div>
				<div class="clear"></div>
			</div>
			<div class="padd-button">
				<input type="submit" class="submit" title="Pay Now" value="Pay Now" />
				<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
			</div>
			<div class="clear"></div>
		</div>
	</form>
</div>