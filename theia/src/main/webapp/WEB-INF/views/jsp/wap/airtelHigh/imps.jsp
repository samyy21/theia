<div data-role="collapsible" id="imps" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>IMPS</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<form autocomplete="off" name="imps-form" method="post" action="submitTransaction" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			<input type="hidden" name="walletAmount" id="walletAmountCC" value="0" />
			
			<p class="txt12">
				IMPS is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks, 
				<a href="http://www.npci.org.in/impsmerpay10.aspx" target="_blank">click here</a>.
			</p>
			
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input type="tel" name="mobileNo" id="mobileNo"  maxlength="10" placeholder="Mobile Number" class="ui-input-text ui-body-c">
				</div>
				<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
					<div class="error">${requestScope.validationErrors['INVALID_MOBILE']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#mobileNo").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
			
			
			
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input type="tel" name="mmid" id="mmid" maxlength="7" placeholder="MMID" class="ui-input-text ui-body-c"/>
				</div>
				<div id="impsMmidShowHideDiv" class="txt12" style="display:none;">MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transactions. <br /><a href="http://www.npci.org.in/merchant.aspx" target="_blank">How to get MMID?</a></div>
		        <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
					<div class="error">${requestScope.validationErrors['INVALID_MMID']}</div>
					<script type="text/javascript">
						$(document).ready(function() {
							$("#mmid").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
	        
			
			 <p>
			 	<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
			 		<input type="tel" name="otp" id="otp" placeholder="OTP" maxlength="6" class="ui-input-text ui-body-c"/>
			 	</div>
			 	<div id="impsOtpshowHideDiv" class="txt12" style="display:none;">OTP is one-time password issued by bank to customer for payment transaction through IMPS. <br /><a href="http://www.npci.org.in/merchant.aspx" target="_blank">How to generate OTP?</a></div>
		         <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
					<div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#otp").parent().addClass("error");
						});
					</script>
		 		</c:if>
			 </p>
	         
	 		
	 		<div class="load-btn">
	  	<input type="submit"  class="submitButton" value="Proceed Securely" data-icon="ldr" data-iconpos="right">
	  </div>
	  
		</form>
	</div>
</div>