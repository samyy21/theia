<div class = 'card content ${10 eq paymentType ? "active" : ""}' id = "itz-card">
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
		<form autocomplete="off" class="itz-form validated" name="itz-form" method="post" action="submitTransaction?${queryStringForSession}" id="card">
			<input type="hidden" name="txnMode" value="CASHCARD" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />

			<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			
			<ul class="cash-card-panel pt20 pbanks grid banks-panel mb10">		
				<li>
					<div id="ITZ" title="ITZ CASH" class="radio" style="background-position: center 0px;">
						<input type="radio" class="bankRadio pcb checkbox fl" value="ITZ" name="bank" autocomplete="off" checked="checked">
						<label class="fl">
							<span class="bank-logo" alt="ITZ" style="background : url(images/web/bank/itz.png) no-repeat;"></span>
						</label>
					</div>
				</li>
			</ul>
					
			<ul class="">
				<li>
					<label for="itzCashNumber" class="mb10">Itz account no.</label>
					<c:set var="impsMobileErrorClass"></c:set>
					<c:if test="${errorsPresent  && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<c:set var="itzCardNumberErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="text-input ${itzCardNumberErrorClass} required" name="itzCashNumber" size="25" maxlength="12" id="itzCashNumber"/>
					</p>
					<c:if test="${itzCardNumberErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
					</c:if>
				</li>
				
				<li class="mt20">
					<label for="itzPwd" class="mb10">Password</label>
					<c:set var="itzPasswordErrorClass"></c:set>
					<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_PWD']}">
						<c:set var="itzPasswordErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="password" class="text-input ${itzPasswordErrorClass} required" name="itzPwd" maxlength="20" id="itzPwd" size="12"/>
					</p>
					<c:if test="${itzPasswordErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_PWD']}</div>
					</c:if>
				</li>
				
			</ul>
			
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id="itzCashSubmit">	          
	           	</div>

	           	<div class="clear"></div>
	        </div>
	       
		</form>
	</div>
	
    <div class="clear"></div>
</div>