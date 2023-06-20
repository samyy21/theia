<div data-role="collapsible" id="itzcash" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>ITZ CASH</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<form autocomplete="off" name="cashCard-form" method="post" action="/payment/request/submit" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="CASHCARD" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
			<div id="ITZ" title="ITZ CASH" class="bank bank-select" style="width : 100px;">
				<span class="bank-logo" alt="ITZ" style="background : url(images/wap/bank/itz.png) no-repeat;"></span>
			</div>
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input type="text" name="itzCashNumber" size="25" maxlength="12" id="itzCashNumber" class="ui-input-text ui-body-c" placeholder="Account no."/>
				</div>
				<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
					<div class="error">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#itzCashNumber").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
			
			
			
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input type="password" name="itzPwd" maxlength="20" id="itzPwd" size="12" class="ui-input-text ui-body-c" placeholder="Password"/>
				</div>
				<div id="impsMmidShowHideDiv" class="txt12" style="display:none;">MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transactions. <br /><a href="http://www.npci.org.in/merchant.aspx" target="_blank">How to get MMID?</a></div>
		        <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PWD']}">
					<div class="error">${requestScope.validationErrors['INVALID_PWD']}</div>
					<script type="text/javascript">
						$(document).ready(function() {
							$("#itzPwd").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
	        <c:if test="${walletInfo.walletFailed}">
	       		<span>
        		By proceeding you agree to our <a href="#terms" data-rel="dialog">Terms &amp; Conditions</a> 
        		and that you have read our <a href="#privacy" data-rel="dialog"> Privacy Policy.</a>
        		</span>
        	</c:if>
	 		
	 		<div class="load-btn">
	  	<button type="submit"  class="submitButton" value="Proceed Securely" data-icon="ldr" data-iconpos="right">Proceed Securely</button>
	  </div>
	  
		</form>
	</div>
</div>