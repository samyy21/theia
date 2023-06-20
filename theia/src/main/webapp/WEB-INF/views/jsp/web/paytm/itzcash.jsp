<div class = 'tab-pane fade <c:if test="${10 == paymentType}">in active</c:if>' id = "cashCardContent">
	<div class="fl">
		<form autocomplete="off" name="cashCard-form" method="post" action="submitTransaction" id="card">
			<input type="hidden" name="txnMode" value="CASHCARD" />
			<input type="hidden" name="channelId" value="WEB" />
			<input type="hidden" name="txn_Mode" value="CASHCARD" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
			<ul class="pbanks">
				<li>
					<div id="" title="ITZ CARD" class="radio has-pretty-child" style="background-position: center 0px;">
						<div class="clearfix prettyradio labelright fl1 blue "><input type="radio" class="bankRadio pcb" value="ITZ" name="bank" style="display: none;" checked="checked">
							<label for="undefined"></label></div>
								<label class="fl ml5">
									<span class="bank-logo" alt="ITZ" style="background : url(images/web/bank/itz.png) no-repeat;"></span>
						</label>
					</div>
				</li>
			</ul>
			<div class="clear"></div>
			<br>
			<ul>
				<li> 
					<label>Itz account no.</label>
					<p> 
						<input type="text" name="itzCashNumber" size="25" maxlength="12" id="itzCashNumber"/>
					</p>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<div class="error">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
						<script type="text/javascript">
							$("#itzCashNumber").addClass("error1");
						</script>
					</c:if>
				</li>
				
				<li class="ml10">
					<label>Password</label>
					<p>
						<input type="password" name="itzPwd" maxlength="20" id="itzPwd" size="12"/>
					</p>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PWD']}">
						<div class="error">${requestScope.validationErrors['INVALID_PWD']}</div>
						<script type="text/javascript">
							$("#itzPwd").addClass("error1");
						</script>
					</c:if>
				</li>
				
				
			</ul>
			
			<p class="clear">
	           	<input name="" type="submit" class="gry-btn" value="Proceed Securely" id="itzCashSubmit">
	           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
	        </p>
	        <div class="secure">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
	        </div>
		</form>
	</div>
    <div class="clear"></div>
</div>