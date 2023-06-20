<div id="ppi-container" <c:if test="${'7' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="imps-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="PPI" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="PPI" />
		<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		<div class="query">
			<div class="padd">
				<div class="fr lock left">
					<div class="fl">
						<img title="Secured" alt="Secured" src="images/web/lock.png">
					</div>
					<div class="fl width195">
						<span class="gry">Your details are secured via 128 Bit encryption by Verisign.</span>
					</div>
					<div class="clear"></div>
				</div>
				<div class="clear"></div>
			</div>
		
			<div class="gray">
				<div class="fl width-imps">
					<div>
						<label>User Id</label>
						<input type="text" name="walletUserId" class="FI" maxlength="45"/>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_USER_ID']}">
						<div class="error">${requestScope.validationErrors['INVALID_USER_ID']}</div>
					</c:if>
					<div class="padd2 mt6">
						<label>Password</label>
						<input type="password" id="ppipassword" name="password" class="FI" maxlength="45"/>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PASSOWRD']}">
						<div class="error">${requestScope.validationErrors['INVALID_PASSOWRD']}</div>
					</c:if>
				</div>
				<div class="clear"></div>
			</div>
			<div class="padd-button">
				<input type="submit" class="submit" title="Pay Now" value="Pay Now" onclick="fnencrypt();"/>
				<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
			</div>
			<div class="clear"></div>
		</div>
	</form>
</div>