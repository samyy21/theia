<div id="itz-container" <c:if test="${'10' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="itz-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="CASHCARD" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="CASHCARD" />
		<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			
		<div class="query">
		
			<div class="gray">
				<div class="fl">
					<div class="padd2 mt6">
						<label>Itz account no.</label>
						<input type="text" name="itzCashNumber" maxlength="12" id="itzCashNumber" class="FI"/>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<div class="error">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
					</c:if>
					<div>
						<label>Password</label>
						<input type="password" name="itzPwd" maxlength="20" id="itzPwd" class="FI"/>
					</div>
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PWD']}">
						<div class="error">${requestScope.validationErrors['INVALID_PWD']}</div>
					</c:if>
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