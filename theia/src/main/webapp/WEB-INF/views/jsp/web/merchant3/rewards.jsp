<div class = 'card content ${11 eq paymentType ? "active" : ""}' id = "rewards-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your payment details are secured via<br/>128 Bit encryption by Verisign</i></span>
	        </div>
		</div>
		<div class="clear"></div>
	</div>
	
	<form autocomplete="off" class="rewards-form validated" name="rewards-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="REWARDS" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="OTP" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />			
		<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
		<c:choose>
			<c:when test="${showRewardsOTP eq 1}">
				<input type="hidden" name="action" value="OTP_INPUT" id="rewardsAction"/>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="action" value="CARD_INPUT" id="rewardsAction"/>
			</c:otherwise>
		</c:choose>
		<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		<c:set var="showRewardsOTP" value="${requestScope.showRewardsOTP}"></c:set>

		<div class="rewards-card-form <c:if test='${showRewardsOTP eq 1}'>hide</c:if>">
		<ul class="grid">
			<label class="rewardsProgram mb10">Select Reward Program</label>
			<li class="rewards-program">
				<select class="selectpicker required" id = "rewardsProgram" name="bankCode" data-size="10">
						<option value="-1">Select</option>
						<option value="REWARDS">PNB Rewardz</option>
						<option value="REWARDS">Federal Bank Utsav Rewards</option>
						<option value="REWARDS">Union Rewardz</option>
						<option value="REWARDS">Krur Vysya Bank - Anmol Rewardz</option>
					    <option value="REWARDS">CBI Anmol Rewardz</option>
						<option value="REWARDS">BOI - Star Rewardz</option>
						<option value="REWARDS">State Bank Group - Freedom Rewardz</option>
				</select>
			</li>
        	<li>
            	<label for="cardNumber" class="mb10 mt20">Card Number</label>
            	<c:set var="rewardsCardErrorInputClass"></c:set>
            	<c:if test="${'11' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD']}">
            		<c:set var="rewardsCardErrorInputClass">error1</c:set>
            	</c:if>
            	<p class="cd">
					<input autocomplete="off" type="tel" name="" class="${rewardsCardErrorInputClass} d text-input large-input cardInput rewardsCardNumber" id="rewardsCardNo" type="text" size="19" maxlength="19" style="width: 278px">
					<input type="hidden" name="cardNumber" value="" class="required">
				</p>
				<c:if test="${rewardsCardErrorInputClass eq 'error1'}">
					<div class="error error2 clear mb10" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD']}</div>
				</c:if>
			</li>
            <li class="">
                <label for="mobileNo" class="mb10">Mobile Number</label>
                <c:set var="rewardsMobileErrorClass"></c:set>
				<c:if test="${'11' eq paymentType && errorsPresent && !empty requestScope.validationErrors['MOBILE_NO']}">
					<c:set var="rewardsMobileErrorClass">error1</c:set>
				</c:if>
                <div>
                	<input type="text" name="mobileNo" size="25" maxlength="10" id="rewardsMobile" class="${rewardsMobileErrorClass} text-input mb10 required"/>
	                <c:if test="${rewardsMobileErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['MOBILE_NO']}</div>
					</c:if>
                	<br>
                	<span class="rewards-otp-msg lh14 small">You will get One-time password (OTP) on this number.</span>
                </div>
                
            </li>
        </ul>
		<c:if test="${requestScope.rewardsInsufficientBalance eq 1}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}. You have insufficient points for this order, select another option to pay.
			</div>
		</c:if>
		<c:if test="${requestScope.rewardsInvalidCard eq 1}">
		 	<div class="alert alert-danger mt10">
				You have entered invalid card number.
			</div>
		</c:if>
		 <c:if test="${requestScope.rewardsError eq 1}">
		 	<div class="alert alert-danger mt10">
				Request failed due to technical error at bank. Retry with different payment mode.
			</div>
		</c:if>
        <div class="mt20">
			<div class="btn-submit ${submitBtnClass} fl">
           		<input name="" type="submit" class="gry-btn btn-normal" value="Check available points" id = "rewardsSubmit">	          
           	</div>

           	<div class="clear"></div>
        </div>
		</div>  
		
		<div class="rewards-otp-form <c:if test="${showRewardsOTP ne 1}">hide</c:if>">
       	<ul class="grid">
            <li class="">
                <label for="otp" class="mb10">Enter OTP</label>
                <c:set var="rewardsOTPErrorClass"></c:set>
				<c:if test="${errorsPresent eq 1 && !empty requestScope.validationErrors['INVALID_OTP']}">
					<c:set var="rewardsOTPErrorClass">error1</c:set>
				</c:if>
                <input type="text" name="otp" size="6" maxlength="6" id="rewardsOTP" class="${rewardsOTPErrorClass} text-input required"/>
                <c:if test="${rewardsOTPErrorClass eq 'error1'}">
					<div class="error error2">${requestScope.validationErrors['INVALID_OTP']}</div>
				</c:if>
            </li>
            <li>
            	<a href="#" class="btn-change-rewards-details mt50 ml10 show blue-text">Change details</a>
           	</li>
		</ul>
		<c:if test="${showRewardsOTP eq 1}">
			<div class="alert alert-info mt10">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}.
			</div>
		</c:if>
        <div class="mt20">
			<div class="btn-submit ${submitBtnClass} fl">
           		<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id = "rewardsOTPSubmit">	          
           	</div>

           	<div class="clear"></div>
        </div>
        
		</div>
       	<div class="clear"></div>
	</form>
</div>